LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
        memdump.c

LOCAL_MODULE_TAGS := eng
LOCAL_MODULE:= pd

include $(BUILD_EXECUTABLE)

