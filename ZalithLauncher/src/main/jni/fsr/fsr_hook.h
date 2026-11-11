#pragma once

#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "FSRHook"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

void fsr_init(int qualityPreset);
void fsr_apply();
void fsr_set_quality(int qualityPreset);
void fsr_destroy();

#ifdef __cplusplus
}
#endif