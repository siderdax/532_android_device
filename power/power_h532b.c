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

#include <dirent.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
//#define LOG_NDEBUG 0

#define LOG_TAG "MV7420PowerHAL"
#include <utils/Log.h>

#include <hardware/hardware.h>
#include <hardware/power.h>

struct h532b_power_module {
    struct power_module base;
    pthread_mutex_t lock;
    int boostpulse_fd;
    int boostpulse_warned;
};

static void sysfs_write(const char *path, char *s)
{
    char buf[80];
    int len;
    int fd = open(path, O_WRONLY);

    if (fd < 0) {
        strerror_r(errno, buf, sizeof(buf));
        ALOGE("Error opening %s: %s\n", path, buf);
        return;
    }

    len = write(fd, s, strlen(s));
    if (len < 0) {
        strerror_r(errno, buf, sizeof(buf));
        ALOGE("Error writing to %s: %s\n", path, buf);
    }

    close(fd);
}

static void power_init(struct power_module *module)
{
    struct h532b_power_module *h532b = (struct h532b_power_module *) module;
    struct dirent **namelist;
    int n;

    /*
     * called when system initialize
     */

    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/target_loads",
                "75 800000:85 1100000:95");
    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/min_sample_time",
                "40000");
    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/hispeed_freq",
                "800000");
    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/go_hispeed_load",
                "85");
    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/above_hispeed_delay",
                "19000 1000000:39000");
    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/timer_rate",
                "20000");
    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/timer_slack",
                "20000");
    sysfs_write("/sys/devices/system/cpu/cpu0/cpufreq/interactive/boostpulse_duration",
                "40000");

    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/target_loads",
                "80 1000000:81 1400000:87 1700000:90");
    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/min_sample_time",
                "40000");
    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/hispeed_freq",
                "1000000");
    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/go_hispeed_load",
                "89");
    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/above_hispeed_delay",
                "59000 1200000:119000 1700000:19000");
    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/timer_rate",
                "20000");
    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/timer_slack",
                "20000");
    sysfs_write("/sys/devices/system/cpu/cpu4/cpufreq/interactive/boostpulse_duration",
                "40000");


    /* TODO : You can initialize the January booster here for your own customization
     * For examples :
     * If you want to set the boosting frequency as
     *  1 GHz, 1.5GHz, 543MHz, 200MHz, 420MHz for big/Little/MIF/INT/G3D and
     *  minimum boosting running core number as
     *  1 and 0 for big/LITTLE  and
     *  Adaptive adjusting range as
     *  100MHz and 300MHz for big/LITTLE
     *
     *
     * sysfs_write("/sys/devices/january_booster.10/tunnables/params",
     *				"1000 1500 543 200 420 1 0 100 300");
     *
     *	sysfs_write("/sys/devices/january_booster.10/tunnables/command",
     *				"F");
    */
}

static void power_set_interactive(struct power_module *module, int on)
{
    struct h532b_power_module *h532b = (struct h532b_power_module *) module;
    char buf[80];
    int ret;

    ALOGV("power_set_interactive: %d\n", on);

    /*
     * called when screen is on/off.
     */

    ALOGV("power_set_interactive: %d done\n", on);
}

static void h532b_power_hint(struct power_module *module, power_hint_t hint,
                             void *data)
{
    struct h532b_power_module *h532b = (struct h532b_power_module *) module;
    char buf[80];
    int len;

    switch (hint) {
    case POWER_HINT_INTERACTION:
        break;

    case POWER_HINT_VSYNC:
        break;

    default:
            break;
    }
}

static struct hw_module_methods_t power_module_methods = {
    .open = NULL,
};

struct h532b_power_module HAL_MODULE_INFO_SYM = {
    base: {
        common: {
            tag: HARDWARE_MODULE_TAG,
            module_api_version: POWER_MODULE_API_VERSION_0_2,
            hal_api_version: HARDWARE_HAL_API_VERSION,
            id: POWER_HARDWARE_MODULE_ID,
            name: "H532B Power HAL",
            author: "The Android Open Source Project",
            methods: &power_module_methods,
        },

        init: power_init,
        setInteractive: power_set_interactive,
        powerHint: h532b_power_hint,
    },

    lock: PTHREAD_MUTEX_INITIALIZER,
    boostpulse_fd: -1,
    boostpulse_warned: 0,
};
