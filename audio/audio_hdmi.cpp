#include <sys/types.h>
#include <system/audio.h>
#include <cutils/log.h>
#include <utils/Errors.h>
#include <utils/RefBase.h>
#include <utils/String16.h>
#include <utils/Singleton.h>
#include <utils/StrongPointer.h>
#include <tinyalsa/asoundlib.h>
#include <binder/IServiceManager.h>

#include "ExynosHWCService.h"

namespace android {

extern "C" {

void set_hdmi_channels(struct pcm_config *config)
{

    uint32_t val = 0;
        android::ExynosHWCService *mHWCService;
        sp<IServiceManager> sm = defaultServiceManager();
        sp<android::IExynosHWCService> hwc = interface_cast<android::IExynosHWCService>(sm->getService(String16("Exynos.HWCService")));

    if (hwc == NULL) {
        ALOGE("ERROR GET HWCService");
        return;
    }

    switch (config->rate) {
    case 32000:
        val |= 0x1 << 19;
        break;
    case 44100:
        val |= 0x1 << 20;
        break;
    case 48000:
        val |= 0x1 << 21;
        break;
    case 88000:
        val |= 0x1 << 22;
        break;
    case 96000:
        val |= 0x1 << 23;
        break;
    case 176000:
        val |= 0x1 << 24;
        break;
    case 192000:
        val |= 0x1 << 25;
        break;
    default: //48Khz
        val |= 0x1 << 21;
    }

    switch (config->format) {
    case PCM_FORMAT_S16_LE:
        val |= 0x01 << 16;
        break;
    case PCM_FORMAT_S24_LE:
        val |= 0x01 << 18;
        break;
    default: //16bit
        val |= 0x01 << 16;
        break;
    }

    switch (config->channels) {
    case AUDIO_CHANNEL_OUT_MONO:
        val |= 0x1 << 0;
        break;
    case AUDIO_CHANNEL_OUT_STEREO:
        val |= 0x1 << 1;
        break;
    case AUDIO_CHANNEL_OUT_QUAD:
        val |= 0x1 << 3;
        break;
    case AUDIO_CHANNEL_OUT_SURROUND:
        val |= 0x1 << 3;
        break;
    case AUDIO_CHANNEL_OUT_5POINT1:
        val |= 0x1 << 5;
        break;
    case AUDIO_CHANNEL_OUT_7POINT1:
        val |= 0x1 << 7;
        break;
    default: //2 channel
        val |= 0x1 << 1;
        break;
    }

   hwc->setHdmiAudioChannel(val);

    return;
}
};
};
