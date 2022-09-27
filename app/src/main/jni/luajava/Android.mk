LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_CFLAGS := -Wno-pointer-to-int-cast -Wno-int-to-pointer-cast
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../lua
LOCAL_CFLAGS += -std=c17

CAL_ARM_MODE := arm
TARGET_PLATFORM := arm64-v8a
TARGET_ABI := android-30-arm64

LOCAL_MODULE     := luajava
LOCAL_SRC_FILES  := luajava.c
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog -ldl
LOCAL_STATIC_LIBRARIES := liblua

include $(BUILD_SHARED_LIBRARY)
