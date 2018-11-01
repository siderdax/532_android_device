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

ifeq ($(TARGET_PREBUILT_KERNEL),)
LOCAL_KERNEL := device/samsung/h532b/kernel
else
LOCAL_KERNEL := $(TARGET_PREBUILT_KERNEL)
endif

# Don't use options in BoardConfig.mk, it's included after device.mk
# If options really needed, define here
# Storage options
BOARD_USES_SDMMC_BOOT := false
BOARD_USES_UFS_BOOT := false
#include $(LOCAL_PATH)/BoardConfig.mk
TARGET_BOOTLOADER_BOARD_NAME := h532b

#BOARD_USES_AUDIO_CODEC := wm8998
# March hotplug
BOARD_USES_MARCH_HOTPLUG := true
# Janeps booster
BOARD_USES_JANUARY_BOOSTER := true
# These are for the multi-storage mount.
DEVICE_PACKAGE_OVERLAYS := \
	device/samsung/h532b/overlay-emmcboot

DEVICE_PACKAGE_OVERLAYS += \
        device/samsung/h532b/overlay_displaysettings


# Init files
ifeq ($(BOARD_USES_SDMMC_BOOT),true)
PRODUCT_COPY_FILES += \
	device/samsung/h532b/conf/init.h532b.rc:root/init.samsungexynos7420.rc \
	device/samsung/h532b/conf/init.h532b.usb.rc:root/init.samsungexynos7420.usb.rc \
	device/samsung/h532b/conf/fstab.h532b.sdboot:root/fstab.samsungexynos7420
else
ifeq ($(BOARD_USES_UFS_BOOT),true)
PRODUCT_COPY_FILES += \
	device/samsung/h532b/conf/init.h532b.rc:root/init.samsungexynos7420.rc \
	device/samsung/h532b/conf/init.h532b.usb.rc:root/init.samsungexynos7420.usb.rc \
	device/samsung/h532b/conf/fstab.h532b.ufs:root/fstab.samsungexynos7420
else
PRODUCT_COPY_FILES += \
	device/samsung/h532b/conf/init.h532b.rc:root/init.samsungexynos7420.rc \
	device/samsung/h532b/conf/init.h532b.usb.rc:root/init.samsungexynos7420.usb.rc \
	device/samsung/h532b/conf/fstab.h532b:root/fstab.samsungexynos7420
endif
endif

PRODUCT_COPY_FILES += \
	device/samsung/h532b/conf/ueventd.h532b.rc:root/ueventd.samsungexynos7420.rc

# Filesystem management tools
PRODUCT_PACKAGES += \
	e2fsck \
	make_ext4fs \
	setup_fs
# audio
PRODUCT_PACKAGES += \
	audio.primary.$(TARGET_BOOTLOADER_BOARD_NAME) \
	audio.a2dp.default \
	audio.usb.default \
	audio.r_submix.default

# audio mixer paths
PRODUCT_COPY_FILES += \
	device/samsung/h532b/mixer_paths.xml:system/etc/mixer_paths.xml

ifeq ($(BOARD_USE_OFFLOAD_AUDIO),true)
PRODUCT_COPY_FILES += \
	device/samsung/h532b/audio_policy_offload.conf:system/etc/audio_policy.conf
else
PRODUCT_COPY_FILES += \
	device/samsung/h532b/audio_policy.conf:system/etc/audio_policy.conf
endif

# power HAL
PRODUCT_PACKAGES += \
	power.h532b

PRODUCT_PACKAGES += \
		    charger_res_images

ifeq ($(BOARD_USES_JANUARY_BOOSTER),true)
PRODUCT_PACKAGES += \
        january_booster
endif

ifeq ($(BOARD_USES_JANUARY_BOOSTER),true)
PRODUCT_COPY_FILES += \
	device/samsung/h532b/power/january_booster:system/bin/january_booster
endif


ifeq ($(BOARD_USES_MARCH_HOTPLUG),true)
PRODUCT_PACKAGES += \
	march_hotplug
endif

ifeq ($(BOARD_USES_MARCH_HOTPLUG),true)
PRODUCT_COPY_FILES += \
	device/samsung/h532b/power/march_hotplug:system/bin/march_hotplug
endif

# busybox firmware
PRODUCT_COPY_FILES += \
	device/samsung/h532b/tools/logkit/busybox:system/xbin/busybox
# logkit
# WARNNING: logkit needs busybox to provide 'tar' cmd
# so make sure there is busybox in the system.img
PRODUCT_COPY_FILES += \
	device/samsung/h532b/tools/logkit/logkit:system/bin/logkit \
	device/samsung/h532b/tools/logkit/logcat_plus:system/bin/logcat_plus \
	device/samsung/h532b/tools/logkit/klog:system/bin/klog

# Libs
PRODUCT_PACKAGES += \
	com.android.future.usb.accessory

# for now include gralloc here. should come from hardware/samsung_slsi/exynos3
PRODUCT_PACKAGES += \
	gralloc.exynos5

PRODUCT_PACKAGES += \
	libion \
	libcsc


# PRODUCT_PACKAGES += \
# 	libexynoscamera	\
# 	camera.$(TARGET_BOOTLOADER_BOARD_NAME)

PRODUCT_PACKAGES += \
	libhwjpeg

#libemoji.so from frameworks/opt/emoji
PRODUCT_PACKAGES += \
	libemoji

# Video Editor
PRODUCT_PACKAGES += \
	VideoEditorGoogle

# Misc other modules
PRODUCT_PACKAGES += \
	lights.h532b

# Input device calibration files
PRODUCT_COPY_FILES += \
	device/samsung/h532b/melfas-ts.idc:system/usr/idc/melfas-ts.idc \
        device/samsung/h532b/gpio-keys.kl:system/usr/keylayout/gpio_keys.33.kl \
        device/samsung/h532b/gpio-keys.kl:system/usr/keylayout/gpio_keys.kl

#  MobiCore setup
PRODUCT_PACKAGES += \
	libMcClient \
	libMcRegistry \
	libgdmcprov \
	mcDriverDaemon

# WideVine DASH modules
PRODUCT_PACKAGES += \
	libwvdrmengine

# WideVine modules
PRODUCT_PACKAGES += \
	com.google.widevine.software.drm.xml \
	com.google.widevine.software.drm \
	WidevineSamplePlayer \
	libdrmwvmplugin \
	libwvm \
	libWVStreamControlAPI_L1 \
	libwvdrm_L1

# SecureDRM modules
PRODUCT_PACKAGES += \
	secdrv \
	tlwvdrm \
	tlsecdrm \
	liboemcrypto_modular

# KeyManager/AES modules
PRODUCT_PACKAGES += \
	tlkeyman

# Dump Log System
PRODUCT_PACKAGES += \
	DumpLog

# Exyrngd module
PRODUCT_PACKAGES += \
	exyrngd

# Keymaster
PRODUCT_PACKAGES += \
	keystore.exynos7420 \
	tlkeymaster

PRODUCT_PACKAGES += \
	libwpa_client \
	hostapd \
	dhcpcd.conf \
	wpa_supplicant \
	wpa_supplicant.conf

# OpenMAX IL configuration files
PRODUCT_COPY_FILES += \
	frameworks/av/media/libstagefright/data/media_codecs_google_audio.xml:system/etc/media_codecs_google_audio.xml \
	frameworks/av/media/libstagefright/data/media_codecs_google_telephony.xml:system/etc/media_codecs_google_telephony.xml \
	frameworks/av/media/libstagefright/data/media_codecs_google_video.xml:system/etc/media_codecs_google_video.xml \
	device/samsung/h532b/media_profiles.xml:system/etc/media_profiles.xml \
        device/samsung/h532b/media_codecs.xml:system/etc/media_codecs.xml \

PRODUCT_COPY_FILES += \
        frameworks/native/data/etc/android.hardware.location.gps.xml:system/etc/permissions/android.hardware.location.gps.xml \
	frameworks/native/data/etc/android.hardware.sensor.accelerometer.xml:system/etc/permissions/android.hardware.sensor.accelerometer.xml \
	frameworks/native/data/etc/android.hardware.sensor.ambient_temperature.xml:system/etc/permissions/android.hardware.sensor.ambient_temperature.xml \
	frameworks/native/data/etc/android.hardware.sensor.barometer.xml:system/etc/permissions/android.hardware.sensor.barometer.xml \
	frameworks/native/data/etc/android.hardware.sensor.compass.xml:system/etc/permissions/android.hardware.sensor.compass.xml \
	frameworks/native/data/etc/android.hardware.sensor.gyroscope.xml:system/etc/permissions/android.hardware.sensor.gyroscope.xml \
	frameworks/native/data/etc/android.hardware.sensor.light.xml:system/etc/permissions/android.hardware.sensor.light.xml \
	frameworks/native/data/etc/android.hardware.sensor.proximity.xml:system/etc/permissions/android.hardware.sensor.proximity.xml \
	frameworks/native/data/etc/handheld_core_hardware.xml:system/etc/permissions/handheld_core_hardware.xml \
	frameworks/native/data/etc/android.software.sip.voip.xml:system/etc/permissions/android.software.sip.voip.xml \
	frameworks/native/data/etc/android.hardware.wifi.xml:system/etc/permissions/android.hardware.wifi.xml \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml \
	frameworks/native/data/etc/android.hardware.touchscreen.multitouch.jazzhand.xml:system/etc/permissions/android.hardware.touchscreen.multitouch.jazzhand.xml \
	frameworks/native/data/etc/android.hardware.usb.accessory.xml:system/etc/permissions/android.hardware.usb.accessory.xml \
	frameworks/native/data/etc/android.hardware.bluetooth_le.xml:system/etc/permissions/android.hardware.bluetooth_le.xml \
	frameworks/native/data/etc/android.hardware.ethernet.xml:system/etc/permissions/android.hardware.ethernet.xml \
	frameworks/native/data/etc/android.hardware.consumerir.xml:system/etc/permissions/android.hardware.consumerir.xml
	#     frameworks/native/data/etc/android.hardware.camera.flash-autofocus.xml:system/etc/permissions/android.hardware.camera.flash-autofocus.xml \
    #     frameworks/native/data/etc/android.hardware.camera.front.xml:system/etc/permissions/android.hardware.camera.front.xml \


PRODUCT_COPY_FILES += \
	device/samsung/h532b/conf/Atmel_maXTouch_Touchscreen.kl:system/usr/keylayout/Atmel_maXTouch_Touchscreen.kl \
	device/samsung/h532b/conf/atmel_mxt_ts_T100_touchscreen.kl:system/usr/keylayout/atmel_mxt_ts_T100_touchscreen.kl

PRODUCT_COPY_FILES += \
	device/samsung/h532b/st_fts.bin:system/vendor/firmware/st_fts.bin \
	device/samsung/h532b/82_2C_3.0_AA.RAW:system/vendor/firmware/82_2C_3.0_AA.RAW \
	device/samsung/h532b/init.atmel_ts.sh:system/bin/init.atmel_ts.sh
# memtester
PRODUCT_COPY_FILES += \
       device/samsung/h532b/tools/memtester-4.3.0/memtester:system/bin/memtester

PRODUCT_PROPERTY_OVERRIDES := \
	ro.opengles.version=196609 \
	debug.hwc.force_gpu=0 \
	debug.hwc.winupdate=1
# GPS
PRODUCT_COPY_FILES += \
        hardware/gps/gps.conf:system/etc/gps.conf \
	hardware/gps/u-blox.conf:system/etc/u-blox.conf

PRODUCT_PACKAGES += \
	gps.h532b

# Set default USB interface
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
	persist.sys.usb.config=mass_storage
	# YIKIM 2015.12.08

ifneq (,$(filter user userdebug,$(TARGET_BUILD_VARIANT)))	
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
	persist.sys.logcontrol=0
endif

# WideVine DRM setup
PRODUCT_PROPERTY_OVERRIDES += \
     drm.service.enabled=true

ifeq ($(BOARD_USES_NAVIGATION_BAR), false)
PRODUCT_PROPERTY_OVERRIDES += \
      qemu.hw.mainkeys=1
endif

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
      ro.product.locale.language=ko \
      ro.product.locale.region=KR \
      persist.sys.timezone=Asia/Seoul \
      persist.sys.country=KR \
      persist.sys.language=ko \



PRODUCT_PROPERTY_OVERRIDES += \
	ro.product.locale.language=ko \
	ro.product.locale.region=KR \
	persist.sys.timezone=Asia/Seoul \
	persist.sys.country=KR \
	persist.sys.language=ko \



PRODUCT_PROPERTY_OVERRIDES += \
      ro.sf.lcd_density=320


PRODUCT_CHARACTERISTICS := phone


PRODUCT_AAPT_CONFIG := large xhdpi hdpi xxhdpi
PRODUCT_AAPT_PREF_CONFIG := xhdpi

# setup dalvik vm configs.
$(call inherit-product, frameworks/native/build/phone-xhdpi-1024-dalvik-heap.mk)

PRODUCT_TAGS += dalvik.gc.type-precise

# Bluetooth
PRODUCT_COPY_FILES += \
        device/samsung/h532b/bluetooth/bcmdhd.cal:system/etc/wifi/bcmdhd.cal \
        device/samsung/h532b/bluetooth/BCM4358A3.hcd:system/vendor/firmware/bcm4358.hcd 
	
# Multimedia FFMPEG is a another 3rd media plug-in
USE_FFMPEG := false
ifeq ($(strip $(USE_FFMPEG)), true)
# yyd- 140814, add for seperate file type  
PRODUCT_PROPERTY_OVERRIDES += ro.mm.ffmpeg=1 
PRODUCT_COPY_FILES += \
	device/samsung/h532b/ffmpeg_config.cfg:system/etc/ffmpeg_config.cfg \
	device/samsung/h532b/media_codecs_ffmpeg.xml:system/etc/media_codecs.xml
endif

$(call inherit-product, hardware/samsung_slsi/exynos5/exynos5.mk)
$(call inherit-product-if-exists, hardware/samsung_slsi/exynos7420/exynos7420.mk)
$(call inherit-product-if-exists, hardware/samsung_slsi/exynos7420/firmware/exynos7420-fimc-is.mk)
$(call inherit-product, vendor/samsung_slsi/exynos7420/exynos7420-vendor.mk)


#$(call inherit-product-if-exists, hardware/broadcom/wlan/bcmdhd/firmware/bcm4358/device-bcm.mk)
$(call inherit-product-if-exists, hardware/marvell/firmware/device-wlan8887.mk)
$(call inherit-product-if-exists, hardware/marvell/module/module-wlan8887.mk)
# Telephony
#$(call inherit-product-if-exists, hardware/broadcom/wlan/bcmdhd/firmware/bcm43241/device-bcm.mk)

# Network  & Codec tool
PRODUCT_PACKAGES += \
	iwconfig \
	iwlist \
	iwpriv \
	max98095test

PRODUCT_PACKAGES += \
	consumerir.default

# NFC (for test)
# PRODUCT_PACKAGES += \
#     NfcNci \
#     Tag

# [H532B] add
$(call inherit-product-if-exists, device/samsung/h532b/infrawaretech.mk)


