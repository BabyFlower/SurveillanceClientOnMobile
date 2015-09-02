LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := main

SDL_PATH := ../SDL
FFMPEG_PATH = ../ffmpeg

LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(SDL_PATH)/include \
					$(LOCAL_PATH)/$(FFMPEG_PATH)/include \
					$(LOCAL_PATH)/tutk

# Add your application source files here...
LOCAL_SRC_FILES := $(SDL_PATH)/src/main/android/SDL_android_main.cpp anvizplayer_android.c \
AVAPIs_Client.c nativeJNI.c

LOCAL_CFLAGS += -DANDROID
LOCAL_SHARED_LIBRARIES := SDL

LOCAL_LDLIBS := -lGLESv1_CM -llog 
LOCAL_LDLIBS += $(LOCAL_PATH)/"libffmpeg.so"
LOCAL_LDLIBS += $(LOCAL_PATH)/"libAVAPIs.so"
LOCAL_LDLIBS += $(LOCAL_PATH)/"libIOTCAPIs.so"
LOCAL_LDLIBS += $(LOCAL_PATH)/"libRDTAPIs.so"

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := AVAPIs
LOCAL_SRC_FILES := libAVAPIs.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := IOTCAPIs
LOCAL_SRC_FILES := libIOTCAPIs.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := RDTAPIs
LOCAL_SRC_FILES := libRDTAPIs.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeg
LOCAL_SRC_FILES := libffmpeg.so
include $(PREBUILT_SHARED_LIBRARY)
