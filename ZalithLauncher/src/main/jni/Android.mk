LOCAL_PATH := $(call my-dir)
HERE_PATH := $(LOCAL_PATH)

# include $(HERE_PATH)/crash_dump/libbase/Android.mk
# include $(HERE_PATH)/crash_dump/libbacktrace/Android.mk
# include $(HERE_PATH)/crash_dump/debuggerd/Android.mk


LOCAL_PATH := $(HERE_PATH)

$(call import-module,prefab/bytehook)
LOCAL_PATH := $(HERE_PATH)


include $(CLEAR_VARS)
LOCAL_LDLIBS := -ldl -llog -landroid
LOCAL_MODULE := pojavexec
LOCAL_SHARED_LIBRARIES := driver_helper
LOCAL_CFLAGS += -rdynamic
LOCAL_SRC_FILES := \
    bigcoreaffinity.c \
    egl_bridge.c \
    ctxbridges/br_loader.c \
    ctxbridges/gl_bridge.c \
    ctxbridges/osm_bridge.c \
    ctxbridges/egl_loader.c \
    ctxbridges/osmesa_loader.c \
    ctxbridges/swap_interval_no_egl.c \
    ctxbridges/virgl_bridge.c \
    environ/environ.c \
    logger/logger.c \
    input_bridge_v3.c \
    jre_launcher.c \
    utils.c \
    stdio_is.c \
    java_exec_hooks.c \
    lwjgl_dlopen_hook.c \

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_CFLAGS += -DADRENO_POSSIBLE
LOCAL_LDLIBS += -lEGL -lGLESv2
endif
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_LDLIBS := -ldl -llog
LOCAL_MODULE := vulkan_check
LOCAL_SHARED_LIBRARIES := driver_helper
LOCAL_SRC_FILES := vulkan_checker.c
include $(BUILD_SHARED_LIBRARY)

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_CFLAGS += -DADRENO_POSSIBLE
endif


include $(CLEAR_VARS)
LOCAL_MODULE := exithook
LOCAL_LDLIBS := -ldl -llog
LOCAL_SHARED_LIBRARIES := bytehook pojavexec
LOCAL_SRC_FILES := exit_hook.c \
    sdl_hook.c \
    sdl_dlopen_hook.c
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_LDLIBS := -ldl -llog -landroid
LOCAL_MODULE := driver_helper
LOCAL_SRC_FILES := \
    driver_helper/driver_helper.c \
    driver_helper/nsbypass.c
LOCAL_CFLAGS += -g -rdynamic

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_CFLAGS += -DADRENO_POSSIBLE
LOCAL_LDLIBS += -lEGL -lGLESv2
endif
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := linkerhook
LOCAL_SRC_FILES := \
    linkerhook/linkerhook.cpp \
    linkerhook/linkerns.c
LOCAL_LDFLAGS := -z global
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := pojavexec_awt
LOCAL_SRC_FILES := \
    awt_bridge.c
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := awt_headless
include $(BUILD_SHARED_LIBRARY)


LOCAL_PATH := $(HERE_PATH)/awt_xawt
include $(CLEAR_VARS)
LOCAL_MODULE := awt_xawt
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SHARED_LIBRARIES := awt_headless
LOCAL_SRC_FILES := xawt_fake.c
include $(BUILD_SHARED_LIBRARY)


# delete fake libs after linked
$(info $(shell (rm $(HERE_PATH)/../jniLibs/*/libawt_headless.so)))


# 模块：jsound - OpenJDK libjsound 核心 + OpenAL 平台后端
# 为四套 JRE（jre8/17/21/25）提供 javax.sound.sampled 的原生实现，
# 核心 .c/.h 文件 vendor 自 openjdk/jdk17u
# （https://github.com/openjdk/jdk17u/blob/master/src/java.desktop/share/native/libjsound，
# GPL-2.0 + Classpath 异常，与本项目 GPL-3.0 兼容；8u 与 17u 的该目录字节级一致，
# 故一套源码可同时服务两代 ABI）。PORT/MIDI 子系统关闭（桩返回 0 设备），
# 音频路径由 jsound_openal.c 桥接 OpenAL，JDK8 的 Platform 额外 native 由 jdk8_compat.c 补齐
LOCAL_PATH := $(HERE_PATH)
include $(CLEAR_VARS)
LOCAL_MODULE := jsound
LOCAL_CFLAGS += \
    -DX_PLATFORM=X_LINUX \
    -D_LITTLE_ENDIAN \
    -DUSE_DAUDIO=TRUE \
    -DUSE_PORTS=FALSE \
    -DUSE_PLATFORM_MIDI_OUT=FALSE \
    -DUSE_PLATFORM_MIDI_IN=FALSE
LOCAL_C_INCLUDES := $(HERE_PATH)/jsound
LOCAL_LDLIBS := -llog -ldl
LOCAL_SRC_FILES := \
    jsound/Utilities.c \
    jsound/Platform.c \
    jsound/DirectAudioDevice.c \
    jsound/DirectAudioDeviceProvider.c \
    jsound/PortMixer.c \
    jsound/PortMixerProvider.c \
    jsound/MidiOutDevice.c \
    jsound/MidiOutDeviceProvider.c \
    jsound/MidiInDevice.c \
    jsound/MidiInDeviceProvider.c \
    jsound/PlatformMidi.c \
    jsound/jsound_openal.c \
    jsound/jdk8_compat.c
include $(BUILD_SHARED_LIBRARY)


LOCAL_PATH := $(HERE_PATH)/flite

include $(CLEAR_VARS)
LOCAL_MODULE := flite
LOCAL_SRC_FILES := flite_bridge.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := fliteWrapper
LOCAL_SRC_FILES := flite_wrapper.c
LOCAL_SHARED_LIBRARIES := flite
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := flite_cmu_us_kal16
LOCAL_SRC_FILES := flite_cmu_us_kal16.c
include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(HERE_PATH)
