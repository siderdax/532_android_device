# Copyright (C) 2012 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
ifneq ($(BOARD_USES_AUDIO_CODEC),wm8998)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := audio.primary.$(TARGET_BOOTLOADER_BOARD_NAME)
LOCAL_MODULE_RELATIVE_PATH := hw
LOCAL_SRC_FILES := \
	audio_hw.c \
	audio_route.c \
	audio_hdmi.cpp

LOCAL_C_INCLUDES += \
	external/tinyalsa/include \
	external/tinycompress/include \
	external/kernel-headers/original/uapi/sound \
	external/expat/lib \
	hardware/samsung_slsi/exynos/include \
	$(TOP)/hardware/samsung_slsi/exynos/libhwcService \
	$(TOP)/hardware/samsung_slsi/$(TARGET_BOARD_PLATFORM)/include \
	$(TOP)/hardware/samsung_slsi/$(TARGET_SOC)/include \
	$(TOP)/hardware/samsung_slsi/$(TARGET_SOC)/libhwcmodule \
	$(TOP)/hardware/samsung_slsi/exynos/libexynosutils \
	$(TOP)/system/core/libsync/include \
	$(call include-path-for, audio-utils)

LOCAL_SHARED_LIBRARIES := \
	liblog \
	libcutils \
	libtinyalsa \
	libtinycompress \
	libaudioutils \
	libexpat \
	libExynosHWCService \
	libui \
	libbinder \
	libutils \
	libdl \

LOCAL_MODULE_TAGS := optional

ifeq ($(BOARD_TV_PRIMARY), true)
LOCAL_C_INCLUDES += \
	$(TOP)/hardware/samsung_slsi/$(TARGET_BOARD_PLATFORM)/libhwc_tvprimary
else
LOCAL_C_INCLUDES += \
	$(TOP)/hardware/samsung_slsi/exynos/libhwc
endif

include $(BUILD_SHARED_LIBRARY)

endif
