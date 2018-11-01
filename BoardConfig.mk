#
# Copyright (C) 2011 The Android Open-Source Project
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
#

# These two variables are set first, so they can be overridden
# by BoardConfigVendor.mk
BOARD_USES_GENERIC_AUDIO := true

TARGET_LINUX_KERNEL_VERSION := 3.10

TARGET_BOARD_INFO_FILE := device/samsung/h532b/board-info.txt

# HACK : To fix up after bring up multimedia devices.
TARGET_SOC := exynos7420

TARGET_ARCH := arm64
TARGET_ARCH_VARIANT := armv8-a
TARGET_CPU_ABI := arm64-v8a
TARGET_CPU_VARIANT := generic

TARGET_2ND_ARCH := arm
TARGET_2ND_ARCH_VARIANT := armv7-a-neon
TARGET_2ND_CPU_ABI := armeabi-v7a
TARGET_2ND_CPU_ABI2 := armeabi
TARGET_2ND_CPU_VARIANT := cortex-a15
TARGET_CPU_SMP := true

TARGET_NO_BOOTLOADER := true
TARGET_NO_KERNEL := false
TARGET_NO_RADIOIMAGE := true
TARGET_BOARD_PLATFORM := exynos5
TARGET_BOOTLOADER_BOARD_NAME := h532b

# bionic libc options
#ARCH_ARM_USE_MEMCPY_ALIGNMENT := true
#BOARD_MEMCPY_ALIGNMENT := 64
#BOARD_MEMCPY_ALIGN_BOUND := 32768
BOARD_MEMCPY_AARCH32 := true

# SMDK common modules
BOARD_SMDK_COMMON_MODULES := liblight

# Independent libhealthd
TARGET_PROVIDES_LIBHEALTHD := true

#OVERRIDE_RS_DRIVER := libRSDriverArm.so

BOARD_EGL_CFG := device/samsung/h532b/conf/egl.cfg
#BOARD_USES_HGL := true
USE_OPENGL_RENDERER := true
NUM_FRAMEBUFFER_SURFACE_BUFFERS := 3
# Storage options
#BOARD_USES_SDMMC_BOOT := false
#BOARD_USES_UFS_BOOT := false

TARGET_USERIMAGES_USE_EXT4 := true
BOARD_SYSTEMIMAGE_PARTITION_SIZE := 838860800
#EMMC 32G
BOARD_USERDATAIMAGE_PARTITION_SIZE := 27917287424
#EMMC 64G
#BOARD_USERDATAIMAGE_PARTITION_SIZE := 60129542144
BOARD_CACHEIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_CACHEIMAGE_PARTITION_SIZE := 838860800
BOARD_FLASH_BLOCK_SIZE := 4096
BOARD_MOUNT_SDCARD_RW := true

# Samsung OpenMAX Video
BOARD_USE_STOREMETADATA := true
BOARD_USE_METADATABUFFERTYPE := true
BOARD_USE_DMA_BUF := true
BOARD_USE_ANB_OUTBUF_SHARE := true
BOARD_USE_IMPROVED_BUFFER := true
BOARD_USE_NON_CACHED_GRAPHICBUFFER := true
BOARD_USE_GSC_RGB_ENCODER := true
BOARD_USE_CSC_HW := false
BOARD_USE_QOS_CTRL := false
BOARD_USE_S3D_SUPPORT := true
BOARD_USE_TIMESTAMP_REORDER_SUPPORT := true
BOARD_USE_VP8ENC_SUPPORT := true
BOARD_USE_HEVCDEC_SUPPORT := true
BOARD_USE_HEVCENC_SUPPORT := true
BOARD_USE_HEVC_HWIP := false
BOARD_USE_VP9DEC_SUPPORT := true
BOARD_USE_CUSTOM_COMPONENT_SUPPORT := true
BOARD_USE_VIDEO_EXT_FOR_WFD_HDCP := true

# AUDIO
BOARD_USE_ALP_AUDIO := true
BOARD_USE_SEIREN_AUDIO := true
#BOARD_USE_OFFLOAD_AUDIO := true

# HACK : to fixup build
WPA_SUPPLICANT_VERSION := VER_0_8_X

# CAMERA
# by eric.seo 
# libcamera/common/ExynosCameraInterface.cpp
#
BOARD_FRONT_ONLY_CAMERA := true 

BOARD_BACK_CAMERA_ROTATION := 270
ifeq ($(strip $(BOARD_FRONT_ONLY_CAMERA)), true)
BOARD_BACK_CAMERA_ROTATION := 90
endif
BOARD_FRONT_CAMERA_ROTATION := 90
BOARD_BACK_CAMERA_SENSOR := SENSOR_NAME_S5K2P8
#BOARD_BACK_CAMERA_SENSOR := SENSOR_NAME_S5K4H5
#BOARD_FRONT_CAMERA_SENSOR := SENSOR_NAME_S5K5E2
BOARD_FRONT_CAMERA_SENSOR := SENSOR_NAME_S5K6B2
BOARD_CAMERA_GED_FEATURE := true

# HWComposer
BOARD_USES_VPP := true
BOARD_USES_WINDOW_UPDATE := true

# HWCServices
BOARD_USES_HWC_SERVICES := true

# SCALER
BOARD_USES_SCALER := true
BOARD_USES_SCALER_M2M1SHOT := true

# HDMI
BOARD_USES_GSC_VIDEO := true
BOARD_USES_CEC := true

# WiFiDisplay
BOARD_USES_VIRTUAL_DISPLAY := true
BOARD_SUPPORT_DQ_Q_SEQUENCE := false
BOARD_SUPPORT_Q_DQ_SEQUENCE := true
BOARD_USES_WFD_SERVICE := true
BOARD_USES_WIFI_DISPLAY:= true
BOARD_USES_VIRTUAL_DISPLAY_DECON_EXT_WB :=true
BOARD_USE_VIDEO_EXT_FOR_WFD_DRM := false
BOARD_USES_VDS_BGRA8888 := true
BOARD_VIRTUAL_DISPLAY_DISABLE_IDMA_G0 := false

BOARD_USES_EXYNOS5_COMMON_GRALLOC := true

TARGET_RUNNING_WITHOUT_SYNC_FRAMEWORK := true

# FIMG2D
BOARD_USES_SKIA_FIMGAPI := true
BOARD_USES_FIMGAPI_V4L2 := false
BOARD_FIMG2D_USES_LEVELING := true
BOARD_USES_SK_SLSI_OPT := true

#SKIA
BOARD_USES_SKIA_PREBUILT_LIBRARY := true

# PLATFORM LOG
TARGET_USES_LOGD := false

#FMP
BOARD_USES_FMP_DM_CRYPT := true

# EXYRNG
BOARD_USES_FIPS_COMPLIANCE_RNG_DRV := true

# Bluetooth
# BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := device/samsung/h532b/bluetooth
BOARD_HAVE_BLUETOOTH := true
# YIKIM 2015.12.08
# BOARD_HAVE_BLUETOOTH_BCM := true
BOARD_HAVE_BLUETOOTH_MRVL := true

# ART
BOARD_EXYNOS_ART_OPT := true
ifeq ($(BOARD_EXYNOS_ART_OPT),true)
  ADDITIONAL_BUILD_PROPERTIES += dalvik.vm.image-dex2oat-filter=speed
  ADDITIONAL_BUILD_PROPERTIES += dalvik.vm.dex2oat-filter=speed
endif

# Enable dex-preoptimization to speed up first boot sequence
ifeq ($(HOST_OS),linux)
  ifeq ($(TARGET_BUILD_VARIANT),user)
    ifeq ($(WITH_DEXPREOPT),)
      WITH_DEXPREOPT := true
    endif
  endif
endif


# -- Wifi related defines

BOARD_WPA_SUPPLICANT_DRIVER := NL80211
WPA_SUPPLICANT_VERSION      := VER_0_8_X
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd 
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd
BOARD_WLAN_DEVICE           := bcmdhd

# BOARD_WPA_SUPPLICANT_DRIVER := NL80211
# WPA_SUPPLICANT_VERSION      := VER_0_8_X
# BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_sd8887 
# BOARD_HOSTAPD_DRIVER        := NL80211
# BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_sd8887
# BOARD_WLAN_DEVICE           := sd8887

# WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/bcmdhd/parameters/firmware_path"
# WIFI_DRIVER_FW_PATH_STA     := "/vendor/firmware/fw_bcmdhd.bin"
# WIFI_DRIVER_FW_PATH_AP      := "/vendor/firmware/fw_bcmdhd_apsta.bin"

# YIKIM 2016.01.07 : Setted for libhardware_legacy/wifi
WIFI_DRIVER_MODULE_PATH		  := "/system/vendor/modules/sd8xxx.ko"
WIFI_DRIVER_MODULE_NAME		  := "sd8xxx"

#SELinux Policy
BOARD_SEPOLICY_DIRS += \
	device/samsung/h532b/sepolicy

BOARD_SEPOLICY_UNION += \
	file_contexts \
	service_contexts \
	domain.te \
	zygote.te \
	logkit.te \
	system_app.te \
	surfaceflinger.te \
	netd.te \
	mediaserver.te \
	record_panic.te \
	march_hotplug.te \
	january_booster.te \
	bluetooth.te \
	kernel.te \
	vold.te
