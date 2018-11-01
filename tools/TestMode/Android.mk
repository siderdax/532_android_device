# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_JAVA_LIBRARIES += telephony-common mms-common
LOCAL_STATIC_JAVA_LIBRARIES += android-common jsr305
#LOCAL_STATIC_JAVA_LIBRARIES += android-common-chips

LOCAL_PACKAGE_NAME := TestMode
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)
