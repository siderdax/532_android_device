LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)


LOCAL_LDLIBS:= -L$(SYSROOT)/usr/lib -llog

LOCAL_SRC_FILES:=  \
	main.c console.c ui.c font_CN16x32.c event.c \
	board_test.c process.c event_queue.c

LOCAL_C_INCLUDES := \
	config.h  ui.h event.h board_test.h  process.h 

LOCAL_C_INCLUDES += system/core/include/cutils

LOCAL_CFLAGS := -D_POSIX_SOURCE -DLINUX
LOCAL_SHARED_LIBRARIES := \
               libcutils \
                          libutils    \
                          libhardware \
                          libhardware_legacy

LOCAL_MODULE_TAGS := eng
LOCAL_MULTILIB := 64
LOCAL_MODULE:= engmode
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)

LOCAL_SRC_FILES:=  \
	test.c

LOCAL_C_INCLUDES := \
	config.h  ui.h event.h board_test.h  process.h 

LOCAL_CFLAGS := -D_POSIX_SOURCE -DLINUX
LOCAL_SHARED_LIBRARIES := \
               libcutils
LOCAL_MODULE_TAGS := eng
LOCAL_MULTILIB := 64

LOCAL_MODULE:= eng_dev_test
include $(BUILD_EXECUTABLE)

