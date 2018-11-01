/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "audio_hw_primary"
#define LOG_NDEBUG 0

#include <errno.h>
#include <pthread.h>
#include <stdint.h>
#include <stdlib.h>
#include <sys/time.h>
#include <fcntl.h>
#include <dlfcn.h>

#include <cutils/log.h>
#include <cutils/properties.h>
#include <cutils/str_parms.h>
#include <cutils/list.h>

#include <hardware/audio.h>
#include <hardware/hardware.h>

#include <videodev2.h>
#include <videodev2_exynos_media.h>

#include <system/audio.h>

#include <tinyalsa/asoundlib.h>
#include <tinycompress/tinycompress.h>
#include <compress_params.h>

#include <audio_utils/resampler.h>

#include "audio_route.h"

#define PCM_CARD                0
#define PCM_CARD_SPDIF          1 // Need to fix
#define PCM_TOTAL               2

#define PCM_DEVICE              0
#define PCM_DEVICE_DEEP         1
#define PCM_DEVICE_HDMI         0 // hdmi -> Internal speaker
#define PCM_DEVICE_VOICE        3 // Need to fix
#define PCM_DEVICE_SCO          4 // Need to fix

/* duration in ms of volume ramp applied when starting capture to remove plop */
#define CAPTURE_START_RAMP_MS 100

/* default sampling for HDMI multichannel output */
#define HDMI_MULTI_DEFAULT_SAMPLING_RATE  48000
/* maximum number of channel mask configurations supported. Currently the primary
 * output only supports 1 (stereo) and the multi channel HDMI output 2 (5.1 and 7.1) */
#define MAX_SUPPORTED_CHANNEL_MASKS 2

#define ARRAY_SIZE(a) (sizeof(a) / sizeof((a)[0]))

#define LOG_OFFLOAD 0
#if LOG_OFFLOAD
#define OFFLOG(...) ALOGE(__VA_ARGS__)
#else
#define OFFLOG(...) ((void)0)
#endif
extern void set_hdmi_channels(struct pcm_config *config);

struct pcm_config pcm_config = {
    .channels = 2,
    .rate = 48000,
    .period_size = 512,
    .period_count = 2,
    .format = PCM_FORMAT_S16_LE,
};

// add from 5260
struct pcm_config pcm_config_voice_call= {
	.channels = 2,
	.rate = 8000,
	.period_size = 1024,
	.period_count = 2,
	.format = PCM_FORMAT_S16_LE,
};

struct pcm_config pcm_config_in = {
    .channels = 2,
    .rate = 48000,
    .period_size = 960,
    .period_count = 2,
    .format = PCM_FORMAT_S16_LE,
};

struct pcm_config pcm_config_sco = {
    .channels = 1,
    .rate = 8000,
    .period_size = 128,
    .period_count = 2,
    .format = PCM_FORMAT_S16_LE,
};

struct pcm_config pcm_config_deep = {
    .channels = 2,
    .rate = 48000,
    /* FIXME This is an arbitrary number, may change.
     * Dynamic configuration based on screen on/off is not implemented;
     * let's see what power consumption is first to see if necessary.
     */
    .period_size = 4096,
    .period_count = 2,
    .format = PCM_FORMAT_S16_LE,
};

struct pcm_config pcm_config_hdmi_multi = {
    .channels = 6, /* changed when the stream is opened */
    .rate = HDMI_MULTI_DEFAULT_SAMPLING_RATE,
    .period_size = 960,
    .period_count = 4,
    .format = PCM_FORMAT_S16_LE,
};

enum output_type {
    OUTPUT_DEEP_BUF,      // deep PCM buffers output stream
    OUTPUT_LOW_LATENCY,   // low latency output stream
    OUTPUT_HDMI,          // HDMI multi channel
    OUTPUT_TOTAL
};

struct audio_device {
    struct audio_hw_device hw_device;

    pthread_mutex_t lock; /* see note below on mutex acquisition order */
	// add from 5260
	audio_mode_t mode;
    audio_devices_t out_device; /* "or" of stream_out.device for all active output streams */
    audio_devices_t in_device;
    bool mic_mute;
    struct audio_route *ar;
    audio_source_t input_source;
    int cur_route_id;     /* current route ID: combination of input source
                           * and output device IDs */
	int hdmi_drv_fd;
	// add from 5260
	int in_call;
	float voice_volume;
    struct pcm *pcm_voice_out;
    struct pcm *pcm_sco_out;
    struct pcm *pcm_voice_in;
    struct pcm *pcm_sco_in;
	int voice_on_count;
	int sco_on_count;
	struct stream_in *active_input;
    struct stream_out *outputs[OUTPUT_TOTAL];
};

typedef enum {
    CMD_WAIT_WRITE,
    CMD_WAIT_DRAIN,
    CMD_WAIT_PARTIAL_DRAIN,
    CMD_EXIT,
    CMD_INVALID,
} offload_cmd;

struct offload_node {
    struct listnode node;
    offload_cmd cmd;
};

struct stream_out {
    struct audio_stream_out stream;

    pthread_mutex_t lock; /* see note below on mutex acquisition order */
    struct pcm *pcm[PCM_TOTAL];
    struct pcm_config config;
    struct compress     *compr;
    struct compr_config compr_config;
    unsigned int pcm_device;
    bool standby; /* true if all PCMs are inactive */
    audio_devices_t device;
    /* FIXME: when HDMI multichannel output is active, other outputs must be disabled as
     * HDMI and WM1811 share the same I2S. This means that notifications and other sounds are
     * silent when watching a 5.1 movie. */
    bool disabled;
    audio_channel_mask_t channel_mask;
    /* Array of supported channel mask configurations. +1 so that the last entry is always 0 */
    audio_channel_mask_t supported_channel_masks[MAX_SUPPORTED_CHANNEL_MASKS + 1];
    uint64_t written; /* total frames written, not cleared when entering standby */

    struct audio_device *dev;

    bool offload;
    bool avail_write;
    bool playback_started;
    stream_callback_t offload_callback;
    void *offload_cookie;

    pthread_cond_t offload_cond;
    pthread_t offload_thread;
    struct listnode offload_cmd_list;
    struct compr_gapless_mdata offload_metadata;
};

struct stream_in {
    struct audio_stream_in stream;

    pthread_mutex_t lock; /* see note below on mutex acquisition order */
    struct pcm *pcm;
    bool standby;

    unsigned int requested_rate;
    struct resampler_itfe *resampler;
    struct resampler_buffer_provider buf_provider;
    int16_t *buffer;
    size_t frames_in;
    int read_status;
    audio_source_t input_source;
    audio_io_handle_t io_handle;
    audio_devices_t device;
    uint16_t ramp_vol;
    uint16_t ramp_step;
    size_t  ramp_frames;

    struct audio_device *dev;
};

#define STRING_TO_ENUM(string) { #string, string }

struct string_to_enum {
    const char *name;
    uint32_t value;
};

const struct string_to_enum out_channels_name_to_enum_table[] = {
    STRING_TO_ENUM(AUDIO_CHANNEL_OUT_STEREO),
    STRING_TO_ENUM(AUDIO_CHANNEL_OUT_5POINT1),
    STRING_TO_ENUM(AUDIO_CHANNEL_OUT_7POINT1),
};

enum {
    OUT_DEVICE_SPEAKER,
    OUT_DEVICE_HEADSET,
    OUT_DEVICE_HEADPHONES,
    OUT_DEVICE_BT_SCO,
    OUT_DEVICE_SPEAKER_AND_HEADSET,
    OUT_DEVICE_TAB_SIZE,           /* number of rows in route_configs[][] */
    OUT_DEVICE_NONE,
    OUT_DEVICE_CNT
};

enum {
    IN_SOURCE_MIC,
    IN_SOURCE_CAMCORDER,
    IN_SOURCE_VOICE_RECOGNITION,
    IN_SOURCE_VOICE_COMMUNICATION,
    IN_SOURCE_TAB_SIZE,            /* number of lines in route_configs[][] */
    IN_SOURCE_NONE,
    IN_SOURCE_CNT
};

int get_output_device_id(audio_devices_t device)
{
    if (device == AUDIO_DEVICE_NONE)
        return OUT_DEVICE_NONE;

    if (popcount(device) == 2) {
        if ((device == (AUDIO_DEVICE_OUT_SPEAKER |
                       AUDIO_DEVICE_OUT_WIRED_HEADSET)) ||
                (device == (AUDIO_DEVICE_OUT_SPEAKER |
                        AUDIO_DEVICE_OUT_WIRED_HEADPHONE)))
            return OUT_DEVICE_SPEAKER_AND_HEADSET;
        else
            return OUT_DEVICE_NONE;
    }

    if (popcount(device) != 1)
        return OUT_DEVICE_NONE;

    switch (device) {
    case AUDIO_DEVICE_OUT_SPEAKER:
        return OUT_DEVICE_SPEAKER;
    case AUDIO_DEVICE_OUT_WIRED_HEADSET:
        return OUT_DEVICE_HEADSET;
    case AUDIO_DEVICE_OUT_WIRED_HEADPHONE:
        return OUT_DEVICE_HEADPHONES;
    case AUDIO_DEVICE_OUT_BLUETOOTH_SCO:
    case AUDIO_DEVICE_OUT_BLUETOOTH_SCO_HEADSET:
    case AUDIO_DEVICE_OUT_BLUETOOTH_SCO_CARKIT:
        return OUT_DEVICE_BT_SCO;
    default:
		// add from 5260
        //return OUT_DEVICE_NONE;
		return OUT_DEVICE_SPEAKER;
    }
}

int get_input_source_id(audio_source_t source)
{
    switch (source) {
    case AUDIO_SOURCE_DEFAULT:
        return IN_SOURCE_NONE;
    case AUDIO_SOURCE_MIC:
        return IN_SOURCE_MIC;
	// add from 5260
	case AUDIO_SOURCE_VOICE_CALL:
		return IN_SOURCE_VOICE_COMMUNICATION;
    case AUDIO_SOURCE_CAMCORDER:
        return IN_SOURCE_CAMCORDER;
    case AUDIO_SOURCE_VOICE_RECOGNITION:
        return IN_SOURCE_VOICE_RECOGNITION;
    case AUDIO_SOURCE_VOICE_COMMUNICATION:
		// add from 5260
        //return IN_SOURCE_VOICE_COMMUNICATION;
		return IN_SOURCE_VOICE_RECOGNITION;
    default:
        return IN_SOURCE_NONE;
    }
}

struct route_config {
    const char * const output_route;
    const char * const input_route;
};

const struct route_config media_speaker = {
    "media-speaker",
    "media-main-mic",
};

const struct route_config media_headphones = {
    "media-headphones",
    "media-main-mic",
};

const struct route_config media_headset = {
    "media-headphones",
    "media-headset-mic",
};

const struct route_config camcorder_speaker = {
    "media-speaker",
    "media-second-mic",
};

const struct route_config camcorder_headphones = {
    "media-headphones",
    "media-second-mic",
};

const struct route_config voice_rec_speaker = {
    "voice-rec-speaker",
    "voice-rec-main-mic",
};

const struct route_config voice_rec_headphones = {
    "voice-rec-headphones",
    "voice-rec-main-mic",
};

const struct route_config voice_rec_headset = {
    "voice-rec-headphones",
    "voice-rec-headset-mic",
};

const struct route_config communication_speaker = {
    "communication-speaker",
    "communication-main-mic",
};

const struct route_config communication_headphones = {
    "communication-headphones",
    "communication-main-mic",
};

const struct route_config communication_headset = {
    "communication-headphones",
    "communication-headset-mic",
};

const struct route_config speaker_and_headphones = {
    "speaker-and-headphones",
    "media-second-mic",
};

const struct route_config bluetooth_sco = {
    "bt-sco-headset",
    "bt-sco-mic",
};

// add from 5260
const struct route_config communication_bt_sco = {
	"communication-bt-sco-headset",
	"communication-bt-sco-mic",
};

const struct route_config * const route_configs[IN_SOURCE_TAB_SIZE]
                                               [OUT_DEVICE_TAB_SIZE] = {
    {   /* IN_SOURCE_MIC */
        &media_speaker,             /* OUT_DEVICE_SPEAKER */
        &media_headset,             /* OUT_DEVICE_HEADSET */
        &media_headphones,          /* OUT_DEVICE_HEADPHONES */
        &bluetooth_sco,             /* OUT_DEVICE_BT_SCO */
        &speaker_and_headphones     /* OUT_DEVICE_SPEAKER_AND_HEADSET */
    },
    {   /* IN_SOURCE_CAMCORDER */
        &camcorder_speaker,         /* OUT_DEVICE_SPEAKER */
        &camcorder_headphones,      /* OUT_DEVICE_HEADSET */
        &camcorder_headphones,      /* OUT_DEVICE_HEADPHONES */
        &bluetooth_sco,             /* OUT_DEVICE_BT_SCO */
        &speaker_and_headphones     /* OUT_DEVICE_SPEAKER_AND_HEADSET */
    },
    {   /* IN_SOURCE_VOICE_RECOGNITION */
        &voice_rec_speaker,         /* OUT_DEVICE_SPEAKER */
        &voice_rec_headset,         /* OUT_DEVICE_HEADSET */
        &voice_rec_headphones,      /* OUT_DEVICE_HEADPHONES */
        &bluetooth_sco,             /* OUT_DEVICE_BT_SCO */
        &speaker_and_headphones     /* OUT_DEVICE_SPEAKER_AND_HEADSET */
    },
    {   /* IN_SOURCE_VOICE_COMMUNICATION */
        &communication_speaker,     /* OUT_DEVICE_SPEAKER */
        &communication_headset,     /* OUT_DEVICE_HEADSET */
        &communication_headphones,  /* OUT_DEVICE_HEADPHONES */
		// add from 5260
        //&bluetooth_sco,             /* OUT_DEVICE_BT_SCO */
		&communication_bt_sco,		/* OUT_DEVICE_BT_SCO */
        &speaker_and_headphones     /* OUT_DEVICE_SPEAKER_AND_HEADSET */
    }
};

static void start_bt_sco(struct audio_device *adev);
static int do_in_standby(struct stream_in *in);
static int set_voice_volume(struct audio_device *adev);
static int do_out_standby(struct stream_out *out);

static int load(const char *id,
        const char *path,
        const struct hw_module_t **pHmi)
{
    int status;
    void *handle;
    struct hw_module_t *hmi;

    /*
     * load the symbols resolving undefined symbols before
     * dlopen returns. Since RTLD_GLOBAL is not or'd in with
     * RTLD_NOW the external symbols will not be global
     */
    handle = dlopen(path, 0x02);
    if (handle == NULL) {
        char const *err_str = dlerror();
        ALOGE("load: module=%s\n%s", path, err_str?err_str:"unknown");
        status = -EINVAL;
        goto done;
    }

    /* Get the address of the struct hal_module_info. */
    const char *sym = HAL_MODULE_INFO_SYM_AS_STR;
    hmi = (struct hw_module_t *)dlsym(handle, sym);
    if (hmi == NULL) {
        ALOGE("load: couldn't find symbol %s", sym);
        status = -EINVAL;
        goto done;
    }

    /* Check that the id matches */
    if (strcmp(id, hmi->id) != 0) {
        ALOGE("load: id=%s != hmi->id=%s", id, hmi->id);
        status = -EINVAL;
        goto done;
    }

    hmi->dso = handle;

    /* success */
    status = 0;

    done:
    if (status != 0) {
        hmi = NULL;
        if (handle != NULL) {
            dlclose(handle);
            handle = NULL;
        }
    } else {
        ALOGV("loaded HAL id=%s path=%s hmi=%p handle=%p",
                id, path, *pHmi, handle);
    }

    *pHmi = hmi;

    return status;
}

// add from 5260
static int start_call(struct audio_device *adev)
{ int err;
	if (adev->voice_on_count++ > 0)
        return 0;
    /* Open modem PCM channels */
    if (adev->pcm_voice_out== NULL) {

        /* Output path open */
        adev->pcm_voice_out = pcm_open(0, PCM_DEVICE_VOICE, PCM_OUT, &pcm_config_voice_call);
        if (!pcm_is_ready(adev->pcm_voice_out)) {
            ALOGE("cannot open pcm_voice_out stream: %s", pcm_get_error(adev->pcm_voice_out));
            goto err_open_out;
        }

         /* Input path open */
        adev->pcm_voice_in = pcm_open(0, PCM_DEVICE_VOICE, PCM_IN, &pcm_config_voice_call);
        if (!pcm_is_ready(adev->pcm_voice_in)) {
            ALOGE("cannot open pcm_voice_in stream: %s", pcm_get_error(adev->pcm_voice_in));
            //goto err_open_out;
        }
    }
	ALOGE("start_call\n");
	
    err = pcm_start(adev->pcm_voice_out);
	ALOGW("pcm_start(adev->pcm_voice_out) err = %d\n", err);
	
    err = pcm_start(adev->pcm_voice_in);
	ALOGW("pcm_start(adev->pcm_voice_in) err = %d\n",err);
	return 0;

err_open_out:
    pcm_close(adev->pcm_voice_out);
    adev->pcm_voice_out = NULL;

    return -ENOMEM;
}
// add from 5260
static void end_call(struct audio_device *adev)
{
    ALOGV("%s:adev->voice_on_count(%d)", __func__, adev->voice_on_count);
    if (adev->voice_on_count == 0 || --adev->voice_on_count > 0)
        return;

    pcm_stop(adev->pcm_voice_out);
    pcm_stop(adev->pcm_voice_in);

    pcm_close(adev->pcm_voice_out);
    pcm_close(adev->pcm_voice_in);
    adev->pcm_voice_out = NULL;
}
// add from 5260
static void force_all_standby(struct audio_device *adev)
{
	struct stream_in *in;
    struct stream_out *out;

    /* only needed for low latency output streams as other streams are not used
     * for voice use cases */

    if (adev->outputs[OUTPUT_LOW_LATENCY] != NULL &&
            !adev->outputs[OUTPUT_LOW_LATENCY]->standby) {
        out = adev->outputs[OUTPUT_LOW_LATENCY];
        pthread_mutex_lock(&out->lock);
        do_out_standby(out);
        pthread_mutex_unlock(&out->lock);
    }

    if (adev->active_input) {
        in = adev->active_input;
        pthread_mutex_lock(&in->lock);
        do_in_standby(in);
        pthread_mutex_unlock(&in->lock);
    }

}
/**
 * NOTE: when multiple mutexes have to be acquired, always respect the
 * following order: hw device > in stream > out stream
 */

static void select_devices(struct audio_device *adev)
{
    int output_device_id = get_output_device_id(adev->out_device);
    int input_source_id = get_input_source_id(adev->input_source);
    const char *output_route = NULL;
    const char *input_route = NULL;
    int new_route_id;
	// add from 5260
#if 1
    struct mixer *mixer1;
    int card = 0;
    int count=0;
    int dist_rout_id = 0;
#endif
    reset_mixer_state(adev->ar);

    new_route_id = (1 << (input_source_id + OUT_DEVICE_CNT)) + (1 << output_device_id);
    if (new_route_id == adev->cur_route_id)
        return;
    adev->cur_route_id = new_route_id;

    if (input_source_id != IN_SOURCE_NONE) {
        if (output_device_id != OUT_DEVICE_NONE) {
            input_route =
                    route_configs[input_source_id][output_device_id]->input_route;
            output_route =
                    route_configs[input_source_id][output_device_id]->output_route;
        } else {
            switch (adev->in_device) {
            case AUDIO_DEVICE_IN_WIRED_HEADSET:
                output_device_id = OUT_DEVICE_HEADSET;
                break;
            case AUDIO_DEVICE_IN_BLUETOOTH_SCO_HEADSET:
                output_device_id = OUT_DEVICE_BT_SCO;
                break;
            default:
                output_device_id = OUT_DEVICE_SPEAKER;
                break;
            }
            input_route =
                    route_configs[input_source_id][output_device_id]->input_route;
        }
    } else {
        if (output_device_id != OUT_DEVICE_NONE) {
            output_route =
                    route_configs[IN_SOURCE_MIC][output_device_id]->output_route;
        }
    }

    ALOGV("select_devices() devices %#x input src %d output route %s input route %s",
          adev->out_device, adev->input_source,
          output_route ? output_route : "none",
          input_route ? input_route : "none");

    if (output_route)
        audio_route_apply_path(adev->ar, output_route);
    if (input_route)
        audio_route_apply_path(adev->ar, input_route);

    update_mixer_state(adev->ar);
}

// add from 5260
static void select_mode(struct audio_device *adev){
	struct stream_out *out;
    if (adev->mode == AUDIO_MODE_IN_CALL) {
        if (!adev->in_call) {

			audio_devices_t temp_out_device = adev->out_device;
			audio_devices_t temp_in_device = adev->input_source;
ALOGV("===select_mode, in_call=%d, mode=%d,  adev->out_device=%d, adev->input_source=%d\n", 
	adev->in_call, adev->mode,adev->out_device, adev->input_source);
		    force_all_standby(adev);
			
            /* force earpiece route for in call state if speaker is the
            only currently selected route. This prevents having to tear
            down the modem PCMs to change route from speaker to earpiece
            after the ringtone is played, but doesn't cause a route
            change if a headset or bt device is already connected. If
            speaker is not the only thing active, just remove it from
            the route. We'll assume it'll never be used initially during
            a call. This works because we're sure that the audio policy
            manager will update the output device after the audio mode
            change, even if the device selection did not change. */
ALOGV("===select_mode, in_call=%d, mode=%d,  adev->out_device=%d, adev->input_source=%d\n", 
		adev->in_call, adev->mode,adev->out_device, adev->input_source);


             adev->out_device &= ~AUDIO_DEVICE_OUT_SPEAKER;
            // XYS: this value maybe used for voice record, we use it temparily.
            adev->input_source = AUDIO_SOURCE_VOICE_CALL;
ALOGV("===select_mode, in_call=%d, mode=%d,  adev->out_device=%d, adev->input_source=%d\n", 
		adev->in_call, adev->mode,adev->out_device, adev->input_source);
			select_devices(adev);
			start_call(adev);
			adev->in_call = 1;
			
			set_voice_volume(adev);
			
        }
    } else {
        ALOGV("Leaving IN_CALL state, in_call=%d, mode=%d",
             adev->in_call, adev->mode);
        if (adev->in_call) {
            adev->in_call = 0;
            end_call(adev);

            force_all_standby(adev);
			if (adev->out_device == AUDIO_DEVICE_OUT_SPEAKER){

				adev->out_device = AUDIO_DEVICE_OUT_SPEAKER;
				adev->input_source = AUDIO_SOURCE_DEFAULT;
			}else{
				adev->out_device = AUDIO_DEVICE_OUT_WIRED_HEADSET;
				adev->input_source = AUDIO_SOURCE_DEFAULT;
			}

			ALOGV("D:adev->out_device:%d,adev->input_source:%d",adev->out_device,adev->input_source);

            select_devices(adev);
        }
    }

}

static void force_non_hdmi_out_standby(struct audio_device *adev)
{
    enum output_type type;
    struct stream_out *out;

    for (type = 0; type < OUTPUT_TOTAL; ++type) {
        out = adev->outputs[type];
        if (type == OUTPUT_HDMI || !out)
            continue;
        pthread_mutex_lock(&out->lock);
        do_out_standby(out);
        pthread_mutex_unlock(&out->lock);
    }
}

/* must be called with hw device and output stream mutexes locked */
static int start_output_stream(struct stream_out *out)
{
    struct audio_device *adev = out->dev;
    int type;

    if (out == adev->outputs[OUTPUT_HDMI]) {
        force_non_hdmi_out_standby(adev);
    } else if (adev->outputs[OUTPUT_HDMI] && !adev->outputs[OUTPUT_HDMI]->standby) {
        out->disabled = true;
        return 0;
    }

    out->disabled = false;

    ALOGV("%s, out->device : %08X",__func__,out->device);
    OFFLOG("\e[1;31m %s offload %d\e[0m",__func__,out->offload);

    if (out->offload) {
        out->compr = compress_open(PCM_CARD, out->pcm_device,
                                   COMPRESS_IN, &out->compr_config);
        if (out->compr && !is_compress_ready(out->compr)) {
            ALOGE("compress_open(PCM_CARD) failed: %s",
                  compress_get_error(out->compr));
            compress_close(out->compr);
            return -ENOMEM;
        }
        if (out->offload_callback)
            compress_nonblock(out->compr, 1);
        goto end;
    }

    if (out->device & (AUDIO_DEVICE_OUT_AUX_DIGITAL)) {
	    out->pcm_device = PCM_DEVICE_HDMI;

		// change from 5260
		dump_stream_out(out);
	    out->pcm[PCM_CARD] = pcm_open(PCM_CARD, out->pcm_device,
			    PCM_OUT | PCM_MONOTONIC, &out->config);

	    if (out->pcm[PCM_CARD] && !pcm_is_ready(out->pcm[PCM_CARD])) {
		    ALOGE("pcm_open(PCM_CARD) failed: %s",
				    pcm_get_error(out->pcm[PCM_CARD]));
		    pcm_close(out->pcm[PCM_CARD]);
		    return -ENOMEM;
	    }
    }

    if (out->device & (AUDIO_DEVICE_OUT_SPEAKER |
                       AUDIO_DEVICE_OUT_WIRED_HEADSET |
                       AUDIO_DEVICE_OUT_WIRED_HEADPHONE |
                       AUDIO_DEVICE_OUT_ALL_SCO)) {

        if (out->config.period_size >= pcm_config_deep.period_size)
            out->pcm_device = PCM_DEVICE_DEEP;
        else
            out->pcm_device = PCM_DEVICE;

        out->pcm[PCM_CARD] = pcm_open(PCM_CARD, out->pcm_device,
                                      PCM_OUT | PCM_MONOTONIC, &out->config);

        if (out->pcm[PCM_CARD] && !pcm_is_ready(out->pcm[PCM_CARD])) {
            ALOGE("pcm_open(PCM_CARD) failed: %s",
                  pcm_get_error(out->pcm[PCM_CARD]));
            pcm_close(out->pcm[PCM_CARD]);
            return -ENOMEM;
        }
    }

    if (out->device & AUDIO_DEVICE_OUT_DGTL_DOCK_HEADSET) {
        out->pcm[PCM_CARD_SPDIF] = pcm_open(PCM_CARD_SPDIF, out->pcm_device,
                                            PCM_OUT | PCM_MONOTONIC, &out->config);

        if (out->pcm[PCM_CARD_SPDIF] &&
                !pcm_is_ready(out->pcm[PCM_CARD_SPDIF])) {
            ALOGE("pcm_open(PCM_CARD_SPDIF) failed: %s",
                  pcm_get_error(out->pcm[PCM_CARD_SPDIF]));
            pcm_close(out->pcm[PCM_CARD_SPDIF]);
            return -ENOMEM;
        }
    }

end:
    adev->out_device |= out->device;
    select_devices(adev);

    if (out->device & AUDIO_DEVICE_OUT_ALL_SCO)
        start_bt_sco(adev);

    if (out->device & AUDIO_DEVICE_OUT_AUX_DIGITAL)
        set_hdmi_channels(&out->config);

    return 0;
}

/* must be called with hw device and input stream mutexes locked */
static int start_input_stream(struct stream_in *in)
{
    struct audio_device *adev = in->dev;

    in->pcm = pcm_open(PCM_CARD, PCM_DEVICE, PCM_IN, &pcm_config_in);

    if (in->pcm && !pcm_is_ready(in->pcm)) {
        ALOGE("pcm_open() failed: %s", pcm_get_error(in->pcm));
        pcm_close(in->pcm);
        return -ENOMEM;
    }

    /* if no supported sample rate is available, use the resampler */
    if (in->resampler)
        in->resampler->reset(in->resampler);

    in->frames_in = 0;
    adev->input_source = in->input_source;
    adev->in_device = in->device;
	ALOGV("start_input_stream adev->in_device:%x,adev->input_source:%d",adev->in_device,adev->input_source);
    select_devices(adev);

    if (in->device & AUDIO_DEVICE_IN_BLUETOOTH_SCO_HEADSET)
        start_bt_sco(adev);

    /* initialize volume ramp */
    in->ramp_frames = (CAPTURE_START_RAMP_MS * in->requested_rate) / 1000;
    in->ramp_step = (uint16_t)(USHRT_MAX / in->ramp_frames);
    in->ramp_vol = 0;;

    return 0;
}

/* must be called with the hw device mutex locked, OK to hold other mutexes */
static void start_bt_sco(struct audio_device *adev) {

	// change from 5260
	ALOGV("%s:adev->sco_on_count(%d)", __func__, adev->sco_on_count);
    if (adev->sco_on_count++ > 0)
        return;


	// change from 5260
//    adev->pcm_voice_out = pcm_open(PCM_CARD, PCM_DEVICE_VOICE, PCM_OUT|PCM_MMAP,
    #if 0 //mask by viola for testing 2014.0814 
	adev->pcm_voice_out = pcm_open(PCM_CARD, PCM_DEVICE_VOICE, PCM_OUT | PCM_MONOTONIC, &pcm_config_voice_call);
    ALOGE("===00\n");
    if (adev->pcm_voice_out && !pcm_is_ready(adev->pcm_voice_out)) {
        ALOGE("pcm_open(VOICE_OUT) failed: %s", pcm_get_error(adev->pcm_voice_out));
        goto err_voice_out;
    }
	#endif
//    adev->pcm_sco_out = pcm_open(PCM_CARD, PCM_DEVICE_SCO, PCM_OUT|PCM_MMAP,
    adev->pcm_sco_out = pcm_open(PCM_CARD, PCM_DEVICE_SCO, PCM_OUT | PCM_MONOTONIC,
                            &pcm_config_sco);
    if (adev->pcm_sco_out && !pcm_is_ready(adev->pcm_sco_out)) {
        ALOGE("pcm_open(SCO_OUT) failed: %s", pcm_get_error(adev->pcm_sco_out));
        goto err_sco_out;
    }
	
	// change from 5260
    //adev->pcm_voice_in = pcm_open(PCM_CARD, PCM_DEVICE_VOICE, PCM_IN,
    //                             &pcm_config_sco);
    if (adev->pcm_voice_in && !pcm_is_ready(adev->pcm_voice_in)) {
        ALOGE("pcm_open(VOICE_IN) failed: %s", pcm_get_error(adev->pcm_voice_in));
        goto err_voice_in;
    }
    adev->pcm_sco_in = pcm_open(PCM_CARD, PCM_DEVICE_SCO, PCM_IN,
                               &pcm_config_sco);
    if (adev->pcm_sco_in && !pcm_is_ready(adev->pcm_sco_in)) {
        ALOGE("pcm_open(SCO_IN) failed: %s", pcm_get_error(adev->pcm_sco_in));
        goto err_sco_in;
    }
	
	// change from 5260
    #if 0 //mask by viola for testing 2014.0814 
    ALOGE("===0\n");
    pcm_start(adev->pcm_voice_out);
    #endif

	// change from 5260
    pcm_start(adev->pcm_sco_out);
  //  pcm_start(adev->pcm_voice_in);
    pcm_start(adev->pcm_sco_in);
    ALOGE("===start_bt_sco\n");
    return;

err_sco_in:
    pcm_close(adev->pcm_sco_in);
err_voice_in:
    pcm_close(adev->pcm_voice_in);
err_sco_out:
    pcm_close(adev->pcm_sco_out);
err_voice_out:
    pcm_close(adev->pcm_voice_out);
}

/* must be called with the hw device mutex locked, OK to hold other mutexes */
static void stop_bt_sco(struct audio_device *adev) {
	ALOGV("%s:adev->sco_on_count(%d)", __func__, adev->sco_on_count);
    if (adev->sco_on_count == 0 || --adev->sco_on_count > 0)
        return;
  //  pcm_stop(adev->pcm_voice_out);
    pcm_stop(adev->pcm_sco_out);
//    pcm_stop(adev->pcm_voice_in);
    pcm_stop(adev->pcm_sco_in);

//    pcm_close(adev->pcm_voice_out);
    pcm_close(adev->pcm_sco_out);
//    pcm_close(adev->pcm_voice_in);
    pcm_close(adev->pcm_sco_in);
}

static size_t get_input_buffer_size(unsigned int sample_rate,
                                    audio_format_t format,
                                    unsigned int channel_count)
{
    size_t size;

    /*
     * take resampling into account and return the closest majoring
     * multiple of 16 frames, as audioflinger expects audio buffers to
     * be a multiple of 16 frames
     */
    size = (pcm_config_in.period_size * sample_rate) / pcm_config_in.rate;
    size = ((size + 15) / 16) * 16;

    return size * channel_count * audio_bytes_per_sample(format);
}

static int get_next_buffer(struct resampler_buffer_provider *buffer_provider,
                                   struct resampler_buffer* buffer)
{
    struct stream_in *in;
    size_t i;

    if (buffer_provider == NULL || buffer == NULL)
        return -EINVAL;

    in = (struct stream_in *)((char *)buffer_provider -
                                   offsetof(struct stream_in, buf_provider));

    if (in->pcm == NULL) {
        buffer->raw = NULL;
        buffer->frame_count = 0;
        in->read_status = -ENODEV;
        return -ENODEV;
    }

    if (in->frames_in == 0) {
        in->read_status = pcm_read(in->pcm,
                                   (void*)in->buffer,
                                   pcm_frames_to_bytes(in->pcm, pcm_config_in.period_size));
        if (in->read_status != 0) {
            ALOGE("get_next_buffer() pcm_read error %d", in->read_status);
            buffer->raw = NULL;
            buffer->frame_count = 0;
            return in->read_status;
        }

        in->frames_in = pcm_config_in.period_size;

        /* Do stereo to mono conversion in place by discarding right channel */
        for (i = 1; i < in->frames_in; i++)
            in->buffer[i] = in->buffer[i * 2];
    }

    buffer->frame_count = (buffer->frame_count > in->frames_in) ?
                                in->frames_in : buffer->frame_count;
    buffer->i16 = in->buffer + (pcm_config_in.period_size - in->frames_in);

    return in->read_status;

}

static void release_buffer(struct resampler_buffer_provider *buffer_provider,
                                  struct resampler_buffer* buffer)
{
    struct stream_in *in;

    if (buffer_provider == NULL || buffer == NULL)
        return;

    in = (struct stream_in *)((char *)buffer_provider -
                                   offsetof(struct stream_in, buf_provider));

    in->frames_in -= buffer->frame_count;
}

/* read_frames() reads frames from kernel driver, down samples to capture rate
 * if necessary and output the number of frames requested to the buffer specified */
static ssize_t read_frames(struct stream_in *in, void *buffer, ssize_t frames)
{
    ssize_t frames_wr = 0;
    size_t frame_size = audio_stream_frame_size(&in->stream.common);

    while (frames_wr < frames) {
        size_t frames_rd = frames - frames_wr;
        if (in->resampler != NULL) {
            in->resampler->resample_from_provider(in->resampler,
                    (int16_t *)((char *)buffer +
                            frames_wr * frame_size),
                    &frames_rd);
        } else {
            struct resampler_buffer buf = {
                    { raw : NULL, },
                    frame_count : frames_rd,
            };
            get_next_buffer(&in->buf_provider, &buf);
            if (buf.raw != NULL) {
                memcpy((char *)buffer +
                           frames_wr * frame_size,
                        buf.raw,
                        buf.frame_count * frame_size);
                frames_rd = buf.frame_count;
            }
            release_buffer(&in->buf_provider, &buf);
        }
        /* in->read_status is updated by getNextBuffer() also called by
         * in->resampler->resample_from_provider() */
        if (in->read_status != 0)
            return in->read_status;

        frames_wr += frames_rd;
    }
    return frames_wr;
}

/* API functions */

static uint32_t out_get_sample_rate(const struct audio_stream *stream)
{
    struct stream_out *out = (struct stream_out *)stream;

    if (out->offload){
        OFFLOG("\e[1;32m 111 %d \e[0m",out->compr_config.codec->sample_rate);
        return out->compr_config.codec->sample_rate;
    } else {
        OFFLOG("\e[1;32m 222 %d \e[0m",out->config.rate);
        return out->config.rate;
    }
}

static int out_set_sample_rate(struct audio_stream *stream, uint32_t rate)
{
    return -ENOSYS;
}

static size_t out_get_buffer_size(const struct audio_stream *stream)
{
    struct stream_out *out = (struct stream_out *)stream;

    return out->config.period_size *
            audio_stream_frame_size((struct audio_stream *)stream);
}

static audio_channel_mask_t out_get_channels(const struct audio_stream *stream)
{
    struct stream_out *out = (struct stream_out *)stream;

    return out->channel_mask;
}

static audio_format_t out_get_format(const struct audio_stream *stream)
{
    struct stream_out *out = (struct stream_out *)stream;

    if (out->offload)
        return out->compr_config.codec->format;
    else
        return AUDIO_FORMAT_PCM_16_BIT;
}

static int out_set_format(struct audio_stream *stream, audio_format_t format)
{
    return -ENOSYS;
}

/* Return the set of output devices associated with active streams
 * other than out.  Assumes out is non-NULL and out->dev is locked.
 */
static audio_devices_t output_devices(struct stream_out *out)
{
    struct audio_device *dev = out->dev;
    enum output_type type;
    audio_devices_t devices = AUDIO_DEVICE_NONE;

    for (type = 0; type < OUTPUT_TOTAL; ++type) {
        struct stream_out *other = dev->outputs[type];
        if (other && (other != out) && !other->standby) {
            /* safe to access other stream without a mutex,
             * because we hold the dev lock,
             * which prevents the other stream from being closed
             */
            devices |= other->device;
        }
    }

    return devices;
}

static int do_out_standby(struct stream_out *out)
{
    struct audio_device *adev = out->dev;
    int i;

    OFFLOG("\e[1;32m %s \e[0m",__func__);

    if (!out->standby) {
        if (out->offload) {
            compress_stop(out->compr);
            out->playback_started = false;
            compress_close(out->compr);
            out->compr = NULL;
            out->offload = false;
        } else {
            for (i = 0; i < PCM_TOTAL; i++) {
                if (out->pcm[i]) {
                    pcm_close(out->pcm[i]);
                    out->pcm[i] = NULL;
                }
            }
        }
        out->standby = true;

        if (out == adev->outputs[OUTPUT_HDMI]) {
            /* force standby on low latency output stream so that it can reuse HDMI driver if
             * necessary when restarted */
            force_non_hdmi_out_standby(adev);
        }

        if (out->device & AUDIO_DEVICE_OUT_ALL_SCO)
            stop_bt_sco(adev);

		// add from 5260
ALOGW("do_out_standby(): adev->mode=%d\n",adev->mode);
        /* re-calculate the set of active devices from other streams */
        if (adev->mode != AUDIO_MODE_IN_CALL) {
            adev->out_device = output_devices(out);
            select_devices(adev);
		}
    }

    return 0;
}

static int out_standby(struct audio_stream *stream)
{
    struct stream_out *out = (struct stream_out *)stream;
    int ret;

    OFFLOG("\e[1;32m %s \e[0m",__func__);

    pthread_mutex_lock(&out->dev->lock);
    pthread_mutex_lock(&out->lock);

    ret = do_out_standby(out);

    pthread_mutex_unlock(&out->lock);
    pthread_mutex_unlock(&out->dev->lock);

    return ret;
}

void dump_stream_out(const struct audio_stream *stream)
{
    struct stream_out *out = (struct stream_out *)stream;
    ALOGV("out_supported_channel_masks[0]: %d", out->supported_channel_masks[0]);
    ALOGV("out_channel_mask: %d", out->channel_mask);
    ALOGV("out_device: %d", out->device);
    ALOGV("out_config.channels: %d", out->config.channels);
    ALOGV("out_config.rate: %d", out->config.rate);
    ALOGV("out_pcm_device: %d", out->pcm_device);		
}

static int out_dump(const struct audio_stream *stream, int fd)
{
    return 0;
}

static int out_set_parameters(struct audio_stream *stream, const char *kvpairs)
{
    struct stream_out *out = (struct stream_out *)stream;
    struct audio_device *adev = out->dev;
    struct str_parms *parms;
    char value[32];
    int ret;
    unsigned int val;
    OFFLOG("\e[1;32m %s \e[0m",__func__);

    parms = str_parms_create_str(kvpairs);

    ret = str_parms_get_str(parms, AUDIO_PARAMETER_STREAM_ROUTING,
                            value, sizeof(value));
    pthread_mutex_lock(&adev->lock);
    pthread_mutex_lock(&out->lock);
    if (ret >= 0) {
        val = atoi(value);
        if ((out->device != val) && (val != 0)) {
            /* Force standby if moving to/from SPDIF or if the output
             * device changes when in SPDIF mode */
            if (((val & AUDIO_DEVICE_OUT_DGTL_DOCK_HEADSET) ^
                 (adev->out_device & AUDIO_DEVICE_OUT_DGTL_DOCK_HEADSET)) ||
                (adev->out_device & AUDIO_DEVICE_OUT_DGTL_DOCK_HEADSET)) {
                do_out_standby(out);
            }

            /* force output standby to start or stop SCO pcm stream if needed */
            if ((val & AUDIO_DEVICE_OUT_ALL_SCO) ^
                    (out->device & AUDIO_DEVICE_OUT_ALL_SCO)) {
                do_out_standby(out);
            }

            if (!out->standby && (out == adev->outputs[OUTPUT_HDMI] ||
                    !adev->outputs[OUTPUT_HDMI] ||
                    adev->outputs[OUTPUT_HDMI]->standby)) {
                adev->out_device = output_devices(out) | val;
                select_devices(adev);
                do_out_standby(out);
            }
		out->device = val;
        	}
	
	// add from 5260
			/*
			 * add by yaowei.li@samsung.com
			 * change from different device
			 */
			if (adev->in_call){
				if (val ^ adev->out_device){
					switch (val) {
					case AUDIO_DEVICE_OUT_SPEAKER:
						adev->out_device = AUDIO_DEVICE_OUT_SPEAKER;
						adev->input_source = AUDIO_SOURCE_VOICE_CALL;	
						break;
					case AUDIO_DEVICE_OUT_WIRED_HEADSET:
						adev->out_device = AUDIO_DEVICE_OUT_WIRED_HEADSET;
						adev->input_source = AUDIO_SOURCE_VOICE_CALL;
						break;
					case AUDIO_DEVICE_OUT_BLUETOOTH_SCO:
					case AUDIO_DEVICE_OUT_BLUETOOTH_SCO_HEADSET:
                    case AUDIO_DEVICE_OUT_BLUETOOTH_SCO_CARKIT:
						adev->out_device = AUDIO_DEVICE_OUT_BLUETOOTH_SCO;
						adev->input_source = AUDIO_SOURCE_VOICE_CALL;
						break;
					default:
						adev->out_device = AUDIO_DEVICE_OUT_SPEAKER;
						adev->input_source = AUDIO_SOURCE_VOICE_CALL;
						break;
					}
					ALOGV("adev->in_call out_device: %d,input_source: %d",adev->out_device,adev->input_source);
					select_devices(adev);

				}
			}


           
     //   }
    }
    pthread_mutex_unlock(&out->lock);
    pthread_mutex_unlock(&adev->lock);

    str_parms_destroy(parms);
    return ret;
}

static char * out_get_parameters(const struct audio_stream *stream, const char *keys)
{
    const int buf_size = 256;
    struct stream_out *out = (struct stream_out *)stream;
    struct str_parms *query = str_parms_create_str(keys);
    char *str;
    char value[buf_size];
    struct str_parms *reply = str_parms_create();
    size_t i, j;
    int ret;
    bool first = true;

    ret = str_parms_get_str(query, AUDIO_PARAMETER_STREAM_SUP_CHANNELS, value, sizeof(value));
    if (ret >= 0) {
        value[0] = '\0';
        i = 0;
        /* the last entry in supported_channel_masks[] is always 0 */
        while (out->supported_channel_masks[i] != 0) {
            for (j = 0; j < ARRAY_SIZE(out_channels_name_to_enum_table); j++) {
                if (out_channels_name_to_enum_table[j].value == out->supported_channel_masks[i]) {
                    if (!first) {
                        strncat(value, "|", 2);
                    }
                    strncat(value, out_channels_name_to_enum_table[j].name, buf_size - 1 - strlen(value));
                    first = false;
                    break;
                }
            }
            i++;
        }
        str_parms_add_str(reply, AUDIO_PARAMETER_STREAM_SUP_CHANNELS, value);
        str = str_parms_to_str(reply);
    } else {
        str = strdup(keys);
    }

    str_parms_destroy(query);
    str_parms_destroy(reply);
    return str;
}

static uint32_t out_get_latency(const struct audio_stream_out *stream)
{
    struct stream_out *out = (struct stream_out *)stream;

    return (out->config.period_size * out->config.period_count * 1000) /
            out->config.rate;
}

static int out_set_volume(struct audio_stream_out *stream, float left,
                          float right)
{
    return -ENOSYS;
}

/* Must be called with out->lock held for list */
static void add_cmd(struct audio_stream_out *stream, offload_cmd cmd) {
    struct stream_out *out = (struct stream_out *)stream;
    struct offload_node *node = (struct offload_node*)calloc(1, sizeof(struct offload_node));

    node->cmd = cmd;
    list_add_tail(&out->offload_cmd_list, &node->node);
    pthread_cond_signal(&out->offload_cond);
}

static ssize_t out_write(struct audio_stream_out *stream, const void* buffer,
                         size_t bytes)
{
    int ret;
    struct stream_out *out = (struct stream_out *)stream;
    struct audio_device *adev = out->dev;
    int i;

	// add from 5260
	if (adev->in_call) return 0;
	
    char* buf = (char*)buffer;
    if (out->offload) {
        OFFLOG("\e[1;32m %s %d bytes %X %X %X %X \e[0m",__func__, bytes, *(buf),*(buf+1),*(buf+2),*(buf+3));
    }
    /*
     * acquiring hw device mutex systematically is useful if a low
     * priority thread is waiting on the output stream mutex - e.g.
     * executing out_set_parameters() while holding the hw device
     * mutex
     */
    pthread_mutex_lock(&adev->lock);
    pthread_mutex_lock(&out->lock);
    if (out->standby) {
        ret = start_output_stream(out);
        if (ret != 0) {
            pthread_mutex_unlock(&adev->lock);
            goto exit;
        }
        out->standby = false;
    }
    pthread_mutex_unlock(&adev->lock);

    if (out->disabled) {
        ret = -EPIPE;
        goto exit;
    }

    /* Write to all active PCMs */
    if (out->offload) {
        ret = compress_write(out->compr, buffer, bytes);
        if (out->playback_started != true) {
            out->playback_started = true;
            ALOGE("\e[1;31m compress_start \e[0m");
            compress_start(out->compr);
            out->avail_write = 1;
        }
        add_cmd(stream, CMD_WAIT_WRITE);

        pthread_mutex_unlock(&out->lock);
        return ret;

    } else {
        for (i = 0; i < PCM_TOTAL; i++)
            if (out->pcm[i]) {
                ret = pcm_write(out->pcm[i], (void *)buffer, bytes);
                if (ret != 0)
                    break;
            }
    }
    if (ret == 0)
        out->written += bytes / (out->config.channels * sizeof(short));

exit:
    pthread_mutex_unlock(&out->lock);

    if (ret != 0) {
        usleep(bytes * 1000000 / audio_stream_frame_size(&stream->common) /
               out_get_sample_rate(&stream->common));
    }

    return bytes;
}

static int out_get_render_position(const struct audio_stream_out *stream,
                                   uint32_t *dsp_frames)
{
    struct stream_out *out = (struct stream_out *)stream;
    OFFLOG("\e[1;37m %s \e[0m",__func__);

    if (out->compr != NULL) {
        compress_get_tstamp(out->compr, (unsigned long*)dsp_frames,
                            &out->compr_config.codec->sample_rate);
        OFFLOG( "\e[1;31m time %u \e[0m \n", (*dsp_frames) * 10 / 441);
    }

    return 0;
}

static int out_add_audio_effect(const struct audio_stream *stream, effect_handle_t effect)
{
    return 0;
}

static int out_remove_audio_effect(const struct audio_stream *stream, effect_handle_t effect)
{
    return 0;
}

static int out_set_callback(struct audio_stream_out *stream,
            stream_callback_t callback, void *cookie)
{
    struct stream_out *out = (struct stream_out *)stream;
    int ret;

    out->offload_callback = callback;
    out->offload_cookie = cookie;

    OFFLOG("\e[1;32m %s \e[0m",__func__);
    return 0;
}

static int out_pause(struct audio_stream_out* stream)
{
    struct stream_out *out = (struct stream_out *)stream;
    int ret;

//    pthread_mutex_lock(&out->lock);
//    if (out->compr) {
        ret = compress_pause(out->compr);
        if (ret != 0)
            ALOGE("\e[1;31m %s\e[0m",(char*)(out->compr) + 8);
//    }
//    pthread_mutex_unlock(&out->lock);
    OFFLOG("\e[1;32m %s \e[0m",__func__);
    return 0;
}

static int out_resume(struct audio_stream_out* stream)
{
    struct stream_out *out = (struct stream_out *)stream;
    int ret;

    //pthread_mutex_lock(&out->lock);
    ret = compress_resume(out->compr);
        if (ret != 0)
            ALOGE("\e[1;31m %s\e[0m",(char*)(out->compr) + 8);
   // pthread_mutex_unlock(&out->lock);
    OFFLOG("\e[1;32m %s \e[0m",__func__);
    return 0;
}

static int out_drain(struct audio_stream_out* stream, audio_drain_type_t type )
{
    struct stream_out *out = (struct stream_out *)stream;
    int ret;

    OFFLOG("\e[1;35m DRAIN IN!!! type : %s\e[0m", (int)type == AUDIO_DRAIN_EARLY_NOTIFY ? "partial" : "full");
    pthread_mutex_lock(&out->lock);
    if (out->compr) {
        if (type == AUDIO_DRAIN_EARLY_NOTIFY) {
            add_cmd(stream, CMD_WAIT_PARTIAL_DRAIN);
        } else {
            add_cmd(stream, CMD_WAIT_DRAIN);
        }
    }
    pthread_mutex_unlock(&out->lock);

    OFFLOG("\e[1;35m DRAIN OUT!! \e[0m");
    return 0;
}

static int out_flush(struct audio_stream_out* stream)
{
    struct stream_out *out = (struct stream_out *)stream;
    int ret;

    //pthread_mutex_lock(&out->lock);
    if (out->compr) {
        ret = compress_stop(out->compr);
        if (ret != 0)
            ALOGE("\e[1;31m %s\e[0m",(char*)(out->compr) + 8);
        out->playback_started = false;
    }
    //pthread_mutex_unlock(&out->lock);

    OFFLOG("\e[1;32m %s \e[0m",__func__);
    return 0;
}

static int out_get_presentation_position(const struct audio_stream_out *stream,
                                            uint64_t *frames, struct timespec *timestamp)
{
    struct stream_out *out = (struct stream_out *)stream;
    unsigned long samples;
    unsigned int sampling_rate;
    int ret = -1;

    pthread_mutex_lock(&out->lock);
    if (out->offload) {
        compress_get_tstamp(out->compr, &samples, &sampling_rate);
        ret = 0;
    } else {
        int i;
        // There is a question how to implement this correctly when there is more than one PCM stream.
        // We are just interested in the frames pending for playback in the kernel buffer here,
        // not the total played since start.  The current behavior should be safe because the
        // cases where both cards are active are marginal.
        for (i = 0; i < PCM_TOTAL; i++)
            if (out->pcm[i]) {
                size_t avail;
                if (pcm_get_htimestamp(out->pcm[i], &avail, timestamp) == 0) {
                    size_t kernel_buffer_size = out->config.period_size * out->config.period_count;
                    // FIXME This calculation is incorrect if there is buffering after app processor
                    int64_t signed_frames = out->written - kernel_buffer_size + avail;
                    // It would be unusual for this value to be negative, but check just in case ...
                    if (signed_frames >= 0) {
                        *frames = signed_frames;
                        ret = 0;
                    }
                    break;
                }
            }
    }

    pthread_mutex_unlock(&out->lock);
    OFFLOG("\e[1;32m %s \e[0m",__func__);
    return ret;
}

static int out_get_next_write_timestamp(const struct audio_stream_out *stream,
                                        int64_t *timestamp)
{
    return -EINVAL;
}

/** audio_stream_in implementation **/
static uint32_t in_get_sample_rate(const struct audio_stream *stream)
{
    struct stream_in *in = (struct stream_in *)stream;

    return in->requested_rate;
}

static int in_set_sample_rate(struct audio_stream *stream, uint32_t rate)
{
    return 0;
}

static audio_channel_mask_t in_get_channels(const struct audio_stream *stream)
{
    return AUDIO_CHANNEL_IN_MONO;
}


static size_t in_get_buffer_size(const struct audio_stream *stream)
{
    struct stream_in *in = (struct stream_in *)stream;

    return get_input_buffer_size(in->requested_rate,
                                 AUDIO_FORMAT_PCM_16_BIT,
                                 popcount(in_get_channels(stream)));
}

static audio_format_t in_get_format(const struct audio_stream *stream)
{
    return AUDIO_FORMAT_PCM_16_BIT;
}

static int in_set_format(struct audio_stream *stream, audio_format_t format)
{
    return -ENOSYS;
}

// add from 5260
static int do_in_standby(struct stream_in* in) {
    struct audio_device *adev = in->dev;

	if (!in->standby) {
        pcm_close(in->pcm);
        in->pcm = NULL;

        if (in->device & AUDIO_DEVICE_IN_BLUETOOTH_SCO_HEADSET)
            stop_bt_sco(adev);

        in->dev->input_source = AUDIO_SOURCE_DEFAULT;
        in->dev->in_device = AUDIO_DEVICE_NONE;
        select_devices(in->dev);
        in->standby = true;

    }
	return 0;
}

static int in_standby(struct audio_stream *stream)
{
	int ret;
    struct stream_in *in = (struct stream_in *)stream;

    pthread_mutex_lock(&in->dev->lock);
    pthread_mutex_lock(&in->lock);

//    if (!in->standby) {
//        pcm_close(in->pcm);
//        in->pcm = NULL;
//        in->dev->input_source = AUDIO_SOURCE_DEFAULT;
//        in->dev->in_device = AUDIO_DEVICE_NONE;
//        select_devices(in->dev);
//        in->standby = true;
//    }
	ret = do_in_standby(in);

    pthread_mutex_unlock(&in->lock);
    pthread_mutex_unlock(&in->dev->lock);

    return ret;
}

static int in_dump(const struct audio_stream *stream, int fd)
{
    return 0;
}

static int in_set_parameters(struct audio_stream *stream, const char *kvpairs)
{
    struct stream_in *in = (struct stream_in *)stream;
    struct audio_device *adev = in->dev;
    struct str_parms *parms;
    char value[32];
    int ret;
    unsigned int val;
    bool apply_now = false;

    parms = str_parms_create_str(kvpairs);

    pthread_mutex_lock(&adev->lock);
    pthread_mutex_lock(&in->lock);
    ret = str_parms_get_str(parms, AUDIO_PARAMETER_STREAM_INPUT_SOURCE,
                            value, sizeof(value));
    if (ret >= 0) {
        val = atoi(value);
        /* no audio source uses val == 0 */
        if ((in->input_source != val) && (val != 0)) {
            in->input_source = val;
            apply_now = !in->standby;
        }
    }

    ret = str_parms_get_str(parms, AUDIO_PARAMETER_STREAM_ROUTING,
                            value, sizeof(value));
    if (ret >= 0) {
        val = atoi(value);
        /* no audio device uses val == 0 */
        if ((in->device != val) && (val != 0)) {
            /* force output standby to start or stop SCO pcm stream if needed */
            if ((val & AUDIO_DEVICE_IN_BLUETOOTH_SCO_HEADSET) ^
                    (in->device & AUDIO_DEVICE_IN_BLUETOOTH_SCO_HEADSET)) {
                do_in_standby(in);
            }
            in->device = val;
            apply_now = !in->standby;
        }
    }

    if (apply_now) {
        adev->input_source = in->input_source;
        adev->in_device = in->device;
        select_devices(adev);
    }

    pthread_mutex_unlock(&in->lock);
    pthread_mutex_unlock(&adev->lock);

    str_parms_destroy(parms);
    return ret;
}

static char * in_get_parameters(const struct audio_stream *stream,
                                const char *keys)
{
    return strdup("");
}

static int in_set_gain(struct audio_stream_in *stream, float gain)
{
    return 0;
}

static void in_apply_ramp(struct stream_in *in, int16_t *buffer, size_t frames)
{
    size_t i;
    uint16_t vol = in->ramp_vol;
    uint16_t step = in->ramp_step;

    frames = (frames < in->ramp_frames) ? frames : in->ramp_frames;

    for (i = 0; i < frames; i++)
    {
        buffer[i] = (int16_t)((buffer[i] * vol) >> 16);
        vol += step;
    }

    in->ramp_vol = vol;
    in->ramp_frames -= frames;
}

static ssize_t in_read(struct audio_stream_in *stream, void* buffer,
                       size_t bytes)
{
    int ret = 0;
    struct stream_in *in = (struct stream_in *)stream;
    struct audio_device *adev = in->dev;
    size_t frames_rq = bytes / audio_stream_frame_size(&stream->common);

    /*
     * acquiring hw device mutex systematically is useful if a low
     * priority thread is waiting on the input stream mutex - e.g.
     * executing in_set_parameters() while holding the hw device
     * mutex
     */
    pthread_mutex_lock(&adev->lock);
    pthread_mutex_lock(&in->lock);
    if (in->standby) {
        ret = start_input_stream(in);
        if (ret == 0)
            in->standby = 0;
    }
    pthread_mutex_unlock(&adev->lock);

    if (ret < 0)
        goto exit;

    /*if (in->num_preprocessors != 0)
        ret = process_frames(in, buffer, frames_rq);
      else */
    ret = read_frames(in, buffer, frames_rq);

    if (ret > 0)
        ret = 0;

    if (in->ramp_frames > 0)
        in_apply_ramp(in, buffer, frames_rq);

    /*
     * Instead of writing zeroes here, we could trust the hardware
     * to always provide zeroes when muted.
     */
    if (ret == 0 && adev->mic_mute)
        memset(buffer, 0, bytes);

exit:
    if (ret < 0)
        usleep(bytes * 1000000 / audio_stream_frame_size(&stream->common) /
               in_get_sample_rate(&stream->common));

    pthread_mutex_unlock(&in->lock);
    return bytes;
}

static uint32_t in_get_input_frames_lost(struct audio_stream_in *stream)
{
    return 0;
}

static int in_add_audio_effect(const struct audio_stream *stream,
                               effect_handle_t effect)
{
    struct stream_in *in = (struct stream_in *)stream;
    effect_descriptor_t descr;
    if ((*effect)->get_descriptor(effect, &descr) == 0) {

        pthread_mutex_lock(&in->dev->lock);
        pthread_mutex_lock(&in->lock);

        pthread_mutex_unlock(&in->lock);
        pthread_mutex_unlock(&in->dev->lock);
    }

    return 0;
}

static int in_remove_audio_effect(const struct audio_stream *stream,
                                  effect_handle_t effect)
{
    struct stream_in *in = (struct stream_in *)stream;
    effect_descriptor_t descr;
    if ((*effect)->get_descriptor(effect, &descr) == 0) {

        pthread_mutex_lock(&in->dev->lock);
        pthread_mutex_lock(&in->lock);

        pthread_mutex_unlock(&in->lock);
        pthread_mutex_unlock(&in->dev->lock);
    }

    return 0;
}

/* Must be called with out->lock held for list */
static offload_cmd pop_and_free(struct listnode *offload_cmd_list) {
    struct listnode *head = list_head(offload_cmd_list);
    struct offload_node *node = node_to_item(head, struct offload_node, node);
    offload_cmd cmd = node->cmd;
    list_remove(head);
    free(node);

    return cmd;
}

static void *offload_thread_loop(void *context)
{
    struct stream_out *out = (struct stream_out *) context;
    offload_cmd cmd;

    while (true) {
        cmd = CMD_INVALID;

        pthread_mutex_lock(&out->lock);

        if (list_empty(&out->offload_cmd_list))
            pthread_cond_wait(&out->offload_cond, &out->lock);

        cmd = pop_and_free(&out->offload_cmd_list);
        OFFLOG("\e[1;32m %s CMD %d\e[0m", __func__, cmd);

        pthread_mutex_unlock(&out->lock);

        int ret = 0;
        switch (cmd) {
        case CMD_WAIT_WRITE:
            ret = compress_wait(out->compr, -1);
            out->offload_callback(STREAM_CBK_EVENT_WRITE_READY, NULL, out->offload_cookie);
            break;
        case CMD_WAIT_DRAIN:
            ret = compress_drain(out->compr);
            out->offload_callback(STREAM_CBK_EVENT_DRAIN_READY, NULL, out->offload_cookie);
            break;
        case CMD_WAIT_PARTIAL_DRAIN:
            ret = compress_set_gapless_metadata(out->compr, &out->offload_metadata);
            if (ret != 0)
                ALOGE("\e[1;31m %s\e[0m",(char*)(out->compr) + 8);
            ret = compress_next_track(out->compr);
            if (ret != 0)
                ALOGE("\e[1;31m %s\e[0m",(char*)(out->compr) + 8);
            ret = compress_partial_drain(out->compr);
            out->offload_callback(STREAM_CBK_EVENT_DRAIN_READY, NULL, out->offload_cookie);
            break;
        case CMD_EXIT:
            goto loop_end;
        default:
            ALOGE("%s WRONG OFFLOAD_CMD", __func__);
        }
        if (ret != 0)
            ALOGE("\e[1;31m %s\e[0m",(char*)(out->compr) + 8);
    }
loop_end:
    pthread_mutex_lock(&out->lock);
    while (!list_empty(&out->offload_cmd_list))
        pop_and_free(&out->offload_cmd_list);
    pthread_mutex_unlock(&out->lock);
    return 0;
}

static int create_offload_callback_thread(struct stream_out *out)
{
    list_init(&out->offload_cmd_list);
    pthread_cond_init(&out->offload_cond, (const pthread_condattr_t *) NULL);
    pthread_create(&out->offload_thread, (const pthread_attr_t *) NULL,
            offload_thread_loop, out);
    out->offload_metadata.encoder_delay = 0;
    out->offload_metadata.encoder_padding = 0;
    return 0;
}

static int adev_open_output_stream(struct audio_hw_device *dev,
                                   audio_io_handle_t handle,
                                   audio_devices_t devices,
                                   audio_output_flags_t flags,
                                   struct audio_config *config,
                                   struct audio_stream_out **stream_out)
{
    struct audio_device *adev = (struct audio_device *)dev;
    struct stream_out *out;
    int ret;
    enum output_type type;

    out = (struct stream_out *)calloc(1, sizeof(struct stream_out));
    if (!out)
        return -ENOMEM;

    out->supported_channel_masks[0] = AUDIO_CHANNEL_OUT_STEREO;
    out->channel_mask = AUDIO_CHANNEL_OUT_STEREO;
    if (devices == AUDIO_DEVICE_NONE)
        devices = AUDIO_DEVICE_OUT_SPEAKER;
    out->device = devices;

    if (flags & AUDIO_OUTPUT_FLAG_DIRECT &&
                   devices == AUDIO_DEVICE_OUT_AUX_DIGITAL) {
        config->sample_rate = HDMI_MULTI_DEFAULT_SAMPLING_RATE;
        config->channel_mask = AUDIO_CHANNEL_OUT_5POINT1;
        out->channel_mask = config->channel_mask;
        out->config = pcm_config_hdmi_multi;
        out->config.rate = config->sample_rate;
        out->config.channels = popcount(config->channel_mask);
        out->pcm_device = PCM_DEVICE_HDMI;
        type = OUTPUT_HDMI;
    } else if (flags & AUDIO_OUTPUT_FLAG_DEEP_BUFFER) {
        out->config = pcm_config_deep;
        out->pcm_device = PCM_DEVICE_DEEP;
        type = OUTPUT_DEEP_BUF;
    } else if (flags & AUDIO_OUTPUT_FLAG_COMPRESS_OFFLOAD) {
        out->compr_config.fragment_size = 0;
        out->compr_config.fragments = 0;
        out->compr_config.codec = (struct snd_codec *)calloc(1, sizeof(struct snd_codec));
        switch(config->format) {
        case AUDIO_FORMAT_MP3:
            out->compr_config.codec->id = SND_AUDIOCODEC_MP3;
            break;
        case AUDIO_FORMAT_AAC:
            out->compr_config.codec->id = SND_AUDIOCODEC_AAC;
            break;
        default:
            out->compr_config.codec->id = SND_AUDIOCODEC_PCM;
        }
        out->compr_config.codec->ch_in = 2;
        out->compr_config.codec->ch_out = 2;
        out->compr_config.codec->sample_rate = config->sample_rate;
        out->compr_config.codec->bit_rate = config->offload_info.bit_rate;
        out->compr_config.codec->format = config->format;
        OFFLOG("\e[1;32m SR %d format %d\e[0m", config->offload_info.sample_rate, config->format);

        out->stream.set_callback = out_set_callback;
        out->stream.pause = out_pause;
        out->stream.resume = out_resume;
        out->stream.drain = out_drain;
        out->stream.flush = out_flush;

        out->offload = true;
        create_offload_callback_thread(out);

        out->config = pcm_config_deep;
        out->pcm_device = PCM_DEVICE_DEEP;
        type = OUTPUT_DEEP_BUF;
    } else {
        out->config = pcm_config;
        out->pcm_device = PCM_DEVICE;
        type = OUTPUT_LOW_LATENCY;
    }

    out->stream.common.get_sample_rate = out_get_sample_rate;
    out->stream.common.set_sample_rate = out_set_sample_rate;
    out->stream.common.get_buffer_size = out_get_buffer_size;
    out->stream.common.get_channels = out_get_channels;
    out->stream.common.get_format = out_get_format;
    out->stream.common.set_format = out_set_format;
    out->stream.common.standby = out_standby;
    out->stream.common.dump = out_dump;
    out->stream.common.set_parameters = out_set_parameters;
    out->stream.common.get_parameters = out_get_parameters;
    out->stream.common.add_audio_effect = out_add_audio_effect;
    out->stream.common.remove_audio_effect = out_remove_audio_effect;
    out->stream.get_latency = out_get_latency;
    out->stream.set_volume = out_set_volume;
    out->stream.write = out_write;
    out->stream.get_render_position = out_get_render_position;
    out->stream.get_next_write_timestamp = out_get_next_write_timestamp;
    out->stream.get_presentation_position = out_get_presentation_position;

    out->dev = adev;

    config->format = out_get_format(&out->stream.common);
    config->channel_mask = out_get_channels(&out->stream.common);
    config->sample_rate = out_get_sample_rate(&out->stream.common);

    out->standby = true;

    pthread_mutex_lock(&adev->lock);
    if (adev->outputs[type]) {
        pthread_mutex_unlock(&adev->lock);
        ret = -EBUSY;
        goto err_open;
    }
    adev->outputs[type] = out;
    pthread_mutex_unlock(&adev->lock);

    *stream_out = &out->stream;

    return 0;

err_open:
    free(out);
    *stream_out = NULL;
    return ret;
}

static void adev_close_output_stream(struct audio_hw_device *dev,
                                     struct audio_stream_out *stream)
{
    struct audio_device *adev;
    enum output_type type;
    struct stream_out *out = (struct stream_out *)stream;

    OFFLOG("\e[1;32m %s \e[0m",__func__);

    out_standby(&stream->common);
    adev = (struct audio_device *)dev;
    pthread_mutex_lock(&adev->lock);
    for (type = 0; type < OUTPUT_TOTAL; ++type) {
        if (adev->outputs[type] == (struct stream_out *) stream) {
            adev->outputs[type] = NULL;
            break;
        }
    }
    pthread_mutex_unlock(&adev->lock);

    if (out->offload) {
        pthread_mutex_lock(&out->lock);
        add_cmd(stream, CMD_EXIT);
        pthread_mutex_unlock(&out->lock);
        pthread_join(out->offload_thread, (void**)NULL);
        pthread_cond_destroy(&out->offload_cond);
        free(out->compr_config.codec);
    }
    free(stream);
}

static int adev_set_parameters(struct audio_hw_device *dev, const char *kvpairs)
{
    return 0;
}

static char * adev_get_parameters(const struct audio_hw_device *dev,
                                  const char *keys)
{
    struct audio_device *adev = (struct audio_device *)dev;
    struct str_parms *parms = str_parms_create_str(keys);
    char value[32];
    int ret = str_parms_get_str(parms, "ec_supported", value, sizeof(value));
    char *str;

    str_parms_destroy(parms);
    if (ret >= 0) {
        parms = str_parms_create_str("ec_supported=yes");
        str = str_parms_to_str(parms);
        str_parms_destroy(parms);
        return str;
    }
    return strdup("");
}

static int adev_init_check(const struct audio_hw_device *dev)
{
    return 0;
}

static int set_voice_volume(struct audio_device *adev){
	// add from 5260
	float mVoiceVol = adev->voice_volume;
	struct mixer_ctl *ctl;
	struct mixer *mMixer = mixer_open(0);

	int int_volume = (int)(adev->voice_volume * 100);
	char *mixer_name = NULL;
	switch(adev->out_device) {
		case AUDIO_DEVICE_OUT_SPEAKER:
			ALOGV("### speaker call volume");
			mixer_name = "Speaker Playback Volume";
			break;
			
		case AUDIO_DEVICE_OUT_BLUETOOTH_SCO:
		case AUDIO_DEVICE_OUT_BLUETOOTH_SCO_HEADSET:
		case AUDIO_DEVICE_OUT_BLUETOOTH_SCO_CARKIT:
			ALOGV("### bluetooth call volume");
			break;
			
		case AUDIO_DEVICE_OUT_WIRED_HEADSET:
		case AUDIO_DEVICE_OUT_WIRED_HEADPHONE:
       		ALOGV("### headset call volume");
			mixer_name = "HP Playback Volume";
			break;

		default:
			ALOGV("Call volume setting error!");
			break;
	}

	if (NULL == mixer_name){
		ALOGV("mixer_name is empty.");
		return 0;
	}
	ctl = mixer_get_ctl_by_name(mMixer, mixer_name);

	if (NULL != ctl) {
		unsigned int idx;
        for (idx = 0 ; mixer_ctl_get_num_values(ctl) > idx ; ++idx)
            mixer_ctl_set_percent(ctl, idx, int_volume);
        ALOGV("write() wakeup setting %s(%d)", mixer_name, int_volume);
    } else {
        ALOGV("failed get mixer control");
    }
    mixer_close(mMixer);    	
	return 0;
}
static int adev_set_voice_volume(struct audio_hw_device *dev, float volume)
{
	// add from 5260
	struct audio_device *adev = (struct audio_device *) dev;

	adev->voice_volume = volume;
    pthread_mutex_lock(&adev->lock);

    if (adev->mode == AUDIO_MODE_IN_CALL)
		set_voice_volume(adev);

    pthread_mutex_unlock(&adev->lock);
	return 0;
}

static int adev_set_master_volume(struct audio_hw_device *dev, float volume)
{
    return -ENOSYS;
}

static int adev_set_mode(struct audio_hw_device *dev, audio_mode_t mode)
{
	// add from 5260
	struct audio_device *adev = (struct audio_device*) dev;
	pthread_mutex_lock(&adev->lock);
	if (adev->mode != mode) {
        adev->mode = mode;
        select_mode(adev);
    }
    pthread_mutex_unlock(&adev->lock);
	return 0;
}

static int adev_set_mic_mute(struct audio_hw_device *dev, bool state)
{
    struct audio_device *adev = (struct audio_device *)dev;

    adev->mic_mute = state;

    return 0;
}

static int adev_get_mic_mute(const struct audio_hw_device *dev, bool *state)
{
    struct audio_device *adev = (struct audio_device *)dev;

    *state = adev->mic_mute;

    return 0;
}

static size_t adev_get_input_buffer_size(const struct audio_hw_device *dev,
                                         const struct audio_config *config)
{

    return get_input_buffer_size(config->sample_rate, config->format,
                                 popcount(config->channel_mask));
}

static int adev_open_input_stream(struct audio_hw_device *dev,
                                  audio_io_handle_t handle,
                                  audio_devices_t devices,
                                  struct audio_config *config,
                                  struct audio_stream_in **stream_in)
{
    struct audio_device *adev = (struct audio_device *)dev;
    struct stream_in *in;
    int ret;

    *stream_in = NULL;

    /* Respond with a request for mono if a different format is given. */
    if (config->channel_mask != AUDIO_CHANNEL_IN_MONO) {
        config->channel_mask = AUDIO_CHANNEL_IN_MONO;
        return -EINVAL;
    }

    in = (struct stream_in *)calloc(1, sizeof(struct stream_in));
    if (!in)
        return -ENOMEM;

    in->stream.common.get_sample_rate = in_get_sample_rate;
    in->stream.common.set_sample_rate = in_set_sample_rate;
    in->stream.common.get_buffer_size = in_get_buffer_size;
    in->stream.common.get_channels = in_get_channels;
    in->stream.common.get_format = in_get_format;
    in->stream.common.set_format = in_set_format;
    in->stream.common.standby = in_standby;
    in->stream.common.dump = in_dump;
    in->stream.common.set_parameters = in_set_parameters;
    in->stream.common.get_parameters = in_get_parameters;
    in->stream.common.add_audio_effect = in_add_audio_effect;
    in->stream.common.remove_audio_effect = in_remove_audio_effect;
    in->stream.set_gain = in_set_gain;
    in->stream.read = in_read;
    in->stream.get_input_frames_lost = in_get_input_frames_lost;

    in->dev = adev;
    in->standby = true;
    in->requested_rate = config->sample_rate;
    in->input_source = AUDIO_SOURCE_DEFAULT;
    in->device = devices;
    in->io_handle = handle;

    in->buffer = malloc(pcm_config_in.period_size * pcm_config_in.channels
                                               * audio_stream_frame_size(&in->stream.common));

    if (!in->buffer) {
        ret = -ENOMEM;
        goto err_malloc;
    }

    if (in->requested_rate != pcm_config_in.rate) {
        in->buf_provider.get_next_buffer = get_next_buffer;
        in->buf_provider.release_buffer = release_buffer;

        ret = create_resampler(pcm_config_in.rate,
                               in->requested_rate,
                               1,
                               RESAMPLER_QUALITY_DEFAULT,
                               &in->buf_provider,
                               &in->resampler);
        if (ret != 0) {
            ret = -EINVAL;
            goto err_resampler;
        }
    }

    *stream_in = &in->stream;
    return 0;

err_resampler:
    free(in->buffer);
err_malloc:
    free(in);
    return ret;
}

static void adev_close_input_stream(struct audio_hw_device *dev,
                                   struct audio_stream_in *stream)
{
    struct stream_in *in = (struct stream_in *)stream;

    in_standby(&stream->common);
    if (in->resampler) {
        release_resampler(in->resampler);
        in->resampler = NULL;
    }
    free(in->buffer);
    free(stream);
}

static int adev_dump(const audio_hw_device_t *device, int fd)
{
    return 0;
}

static int adev_close(hw_device_t *device)
{
    struct audio_device *adev = (struct audio_device *)device;

    audio_route_free(adev->ar);

    free(device);
    return 0;
}

static int adev_open(const hw_module_t* module, const char* name,
                     hw_device_t** device)
{
    struct audio_device *adev;
    int ret;

    if (strcmp(name, AUDIO_HARDWARE_INTERFACE) != 0)
        return -EINVAL;

    if (access("/dev/snd/pcmC0D0p", F_OK)) {
        ALOGE("pcmC0D0p not available. Default Audio HAL will be loaded");
        load(AUDIO_HARDWARE_MODULE_ID, "/system/lib/hw/audio.primary.default.so", &module);
        return module->methods->open(module, AUDIO_HARDWARE_INTERFACE, device);
    }

    adev = calloc(1, sizeof(struct audio_device));
    if (!adev)
        return -ENOMEM;

    adev->hw_device.common.tag = HARDWARE_DEVICE_TAG;
    adev->hw_device.common.version = AUDIO_DEVICE_API_VERSION_2_0;
    adev->hw_device.common.module = (struct hw_module_t *) module;
    adev->hw_device.common.close = adev_close;

    adev->hw_device.init_check = adev_init_check;
    adev->hw_device.set_voice_volume = adev_set_voice_volume;
    adev->hw_device.set_master_volume = adev_set_master_volume;
    adev->hw_device.set_mode = adev_set_mode;
    adev->hw_device.set_mic_mute = adev_set_mic_mute;
    adev->hw_device.get_mic_mute = adev_get_mic_mute;
    adev->hw_device.set_parameters = adev_set_parameters;
    adev->hw_device.get_parameters = adev_get_parameters;
    adev->hw_device.get_input_buffer_size = adev_get_input_buffer_size;
    adev->hw_device.open_output_stream = adev_open_output_stream;
    adev->hw_device.close_output_stream = adev_close_output_stream;
    adev->hw_device.open_input_stream = adev_open_input_stream;
    adev->hw_device.close_input_stream = adev_close_input_stream;
    adev->hw_device.dump = adev_dump;

    adev->ar = audio_route_init();
    adev->input_source = AUDIO_SOURCE_DEFAULT;
    /* adev->cur_route_id initial value is 0 and such that first device
     * selection is always applied by select_devices() */

	adev->sco_on_count = 0;
	adev->voice_on_count = 0;
    adev->hdmi_drv_fd = -1;
    *device = &adev->hw_device.common;

    return 0;
}

static struct hw_module_methods_t hal_module_methods = {
    .open = adev_open,
};

struct audio_module HAL_MODULE_INFO_SYM = {
    .common = {
        .tag = HARDWARE_MODULE_TAG,
        .module_api_version = AUDIO_MODULE_API_VERSION_0_1,
        .hal_api_version = HARDWARE_HAL_API_VERSION,
        .id = AUDIO_HARDWARE_MODULE_ID,
        .name = "H532B audio HW HAL",
        .author = "The Android Open Source Project",
        .methods = &hal_module_methods,
    },
};
