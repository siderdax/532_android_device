LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
        boost.c

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= boost

include $(BUILD_EXECUTABLE)

