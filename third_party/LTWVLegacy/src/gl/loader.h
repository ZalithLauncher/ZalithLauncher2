#ifndef _GL4ES_LOADER_H_
#define _GL4ES_LOADER_H_

#include <stdbool.h>
#include "gl4es.h"
#include "gles.h"
#include "logs.h"

#ifndef NOEGL
//Typedef for egl to be able to call LOAD_EGL...
#include <EGL/egl.h>
#include <EGL/eglext.h>

typedef EGLBoolean (*eglBindAPI_PTR)(EGLenum api);
typedef EGLBoolean (*eglBindTexImage_PTR)(EGLDisplay dpy, EGLSurface surface, EGLint buffer);
typedef EGLBoolean (*eglChooseConfig_PTR)(EGLDisplay dpy, const EGLint * attrib_list, EGLConfig * configs, EGLint config_size, EGLint * num_config);
typedef EGLBoolean (*eglCopyBuffers_PTR)(EGLDisplay dpy, EGLSurface surface, EGLNativePixmapType target);
typedef EGLContext (*eglCreateContext_PTR)(EGLDisplay dpy, EGLConfig config, EGLContext share_context, const EGLint * attrib_list);
typedef EGLSurface (*eglCreatePbufferFromClientBuffer_PTR)(EGLDisplay dpy, EGLenum buftype, EGLClientBuffer buffer, EGLConfig config, const EGLint * attrib_list);
typedef EGLSurface (*eglCreatePbufferSurface_PTR)(EGLDisplay dpy, EGLConfig config, const EGLint * attrib_list);
typedef EGLSurface (*eglCreatePixmapSurface_PTR)(EGLDisplay dpy, EGLConfig config, EGLNativePixmapType pixmap, const EGLint * attrib_list);
typedef EGLSurface (*eglCreatePlatformWindowSurface_PTR)(EGLDisplay display, EGLConfig config, void * native_window, const EGLint * attrib_list);
typedef EGLSurface (*eglCreateWindowSurface_PTR)(EGLDisplay dpy, EGLConfig config, EGLNativeWindowType win, const EGLint * attrib_list);
typedef EGLBoolean (*eglDestroyContext_PTR)(EGLDisplay dpy, EGLContext ctx);
typedef EGLBoolean (*eglDestroySurface_PTR)(EGLDisplay dpy, EGLSurface surface);
typedef EGLBoolean (*eglGetConfigAttrib_PTR)(EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint * value);
typedef EGLBoolean (*eglGetConfigs_PTR)(EGLDisplay dpy, EGLConfig * configs, EGLint config_size, EGLint * num_config);
typedef EGLContext (*eglGetCurrentContext_PTR)();
typedef EGLDisplay (*eglGetCurrentDisplay_PTR)();
typedef EGLSurface (*eglGetCurrentSurface_PTR)(EGLint readdraw);
typedef EGLDisplay (*eglGetDisplay_PTR)(EGLNativeDisplayType display_id);
typedef EGLDisplay (*eglGetPlatformDisplay_PTR)(EGLenum platform, void * native_display, const EGLint * attrib_list);
typedef EGLint (*eglGetError_PTR)();
typedef __eglMustCastToProperFunctionPointerType (*eglGetProcAddress_PTR)(const char * procname);
typedef EGLBoolean (*eglInitialize_PTR)(EGLDisplay dpy, EGLint * major, EGLint * minor);
typedef EGLBoolean (*eglMakeCurrent_PTR)(EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx);
typedef EGLenum (*eglQueryAPI_PTR)();
typedef EGLBoolean (*eglQueryContext_PTR)(EGLDisplay dpy, EGLContext ctx, EGLint attribute, EGLint * value);
typedef const char * (*eglQueryString_PTR)(EGLDisplay dpy, EGLint name);
typedef EGLBoolean (*eglQuerySurface_PTR)(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint * value);
typedef EGLBoolean (*eglReleaseTexImage_PTR)(EGLDisplay dpy, EGLSurface surface, EGLint buffer);
typedef EGLBoolean (*eglReleaseThread_PTR)();
typedef EGLBoolean (*eglSurfaceAttrib_PTR)(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint value);
typedef EGLBoolean (*eglSwapBuffers_PTR)(EGLDisplay dpy, EGLSurface surface);
typedef EGLBoolean (*eglSwapBuffersWithDamageEXT_PTR)(EGLDisplay dpy, EGLSurface surface, EGLint * rects, EGLint n_rects);
typedef EGLBoolean (*eglSwapInterval_PTR)(EGLDisplay dpy, EGLint interval);
typedef EGLBoolean (*eglTerminate_PTR)(EGLDisplay dpy);
typedef EGLBoolean (*eglUnlockSurfaceKHR_PTR)(EGLDisplay display, EGLSurface surface);
typedef EGLBoolean (*eglWaitClient_PTR)();
typedef EGLBoolean (*eglWaitGL_PTR)();
typedef EGLBoolean (*eglWaitNative_PTR)(EGLint engine);
typedef EGLSyncKHR (*eglCreateSyncKHR_PTR)(EGLDisplay dpy, EGLenum type, const EGLint * attrib_list);
typedef EGLSyncKHR (*eglDestroySyncKHR_PTR)(EGLDisplay dpy, EGLSyncKHR sync);
typedef EGLint (*eglClientWaitSyncKHR_PTR)(EGLDisplay dpy, EGLSyncKHR sync, EGLint flags, EGLTimeKHR timeout);

typedef NativePixmapType (*egl_create_pixmap_ID_mapping_PTR)(void *pixmap);
typedef NativePixmapType (*egl_destroy_pixmap_ID_mapping_PTR)(int id);
#ifdef TEXSTREAM
typedef EGLSurface (*eglCreatePixmapSurfaceHI_PTR)(EGLDisplay dpy, EGLConfig config, struct EGLClientPixmapHI * pixmap);
typedef EGLBoolean (*eglDestroyImageKHR_PTR)(EGLDisplay dpy, EGLImageKHR image);
typedef EGLBoolean (*eglDestroyStreamKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream);
typedef EGLImageKHR (*eglCreateImageKHR_PTR)(EGLDisplay dpy, EGLContext ctx, EGLenum target, EGLClientBuffer buffer, const EGLint * attrib_list);
typedef EGLStreamKHR (*eglCreateStreamFromFileDescriptorKHR_PTR)(EGLDisplay dpy, EGLNativeFileDescriptorKHR file_descriptor);
typedef EGLStreamKHR (*eglCreateStreamKHR_PTR)(EGLDisplay dpy, const EGLint * attrib_list);
typedef EGLSyncKHR (*eglDestroySyncKHR_PTR)(EGLDisplay dpy, EGLSyncKHR sync);
typedef EGLBoolean (*eglSignalSyncKHR_PTR)(EGLDisplay dpy, EGLSyncKHR sync, EGLenum mode);
typedef EGLBoolean (*eglGetSyncAttribKHR_PTR)(EGLDisplay dpy, EGLSyncKHR sync, EGLint attribute, EGLint * value);
typedef EGLBoolean (*eglStreamAttribKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream, EGLenum attribute, EGLint value);
typedef EGLBoolean (*eglStreamConsumerAcquireKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream);
typedef EGLBoolean (*eglStreamConsumerGLTextureExternalKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream);
typedef EGLBoolean (*eglStreamConsumerReleaseKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream);
typedef EGLBoolean (*eglLockSurfaceKHR_PTR)(EGLDisplay display, EGLSurface surface, const EGLint * attrib_list);
typedef EGLNativeFileDescriptorKHR (*eglGetStreamFileDescriptorKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream);
typedef EGLBoolean (*eglQueryStreamKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream, EGLenum attribute, EGLint * value);
typedef EGLBoolean (*eglQueryStreamTimeKHR_PTR)(EGLDisplay dpy, EGLStreamKHR stream, EGLenum attribute, EGLTimeKHR * value);
typedef EGLint (*eglWaitSyncKHR_PTR)(EGLDisplay dpy, EGLSyncKHR sync, EGLint flags);
typedef EGLSurface (*eglCreateStreamProducerSurfaceKHR_PTR)(EGLDisplay dpy, EGLConfig config, EGLStreamKHR stream, const EGLint * attrib_list);
#endif // TEXSTREAM
#endif // NOEGL

#ifdef AMIGAOS4
#include "../agl/amigaos.h"
#elif !defined(_WIN32)
#include <dlfcn.h>
#else
#ifndef _WINBASE_
typedef struct HISTANCE__* HISTANCE;
typedef intptr_t (__stdcall* FPROC)();
__declspec(dllimport)
FPROC __stdcall GetProcAddress(HISTANCE, const char*);
#endif
#ifdef _MSC_VER
__forceinline
#elif defined(__GNUC__)
__attribute__((always_inline)) __inline
#endif
static void* dlsym(void* __restrict handle, const char* __restrict symbol)
{ return (void*)GetProcAddress(handle, symbol); }
#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "../glx/hardext.h"
extern void* (APIENTRY_GL4ES *gles_getProcAddress)(const char *name);
extern void (APIENTRY_GL4ES *gl4es_getMainFBSize)(GLint* width, GLint* height);
NonAliasExportDecl(void*,proc_address,(void *lib, const char *name));
// will become references to dlopen'd gles and egl
extern void *gles, *bcm_host, *vcos, *gbm, *drm;
EXPORT extern void *egl;
#if defined __APPLE__ || defined __EMSCRIPTEN__
#define NO_LOADER
#endif

#define WARN_NULL(name) if (name == NULL) LOGD("warning, %s line %d function %s: " #name " is NULL\n", __FILE__, __LINE__, __func__);

#define MSVC_SPC(MACRO, ARGS) MACRO ARGS

#define PUSH_IF_COMPILING_EXT(nam, ...)             \
    if (glstate->list.active) {                     \
        if (!glstate->list.pending) { \
            NewStage(glstate->list.active, STAGE_GLCALL);   \
            MSVC_SPC(push_##nam, (__VA_ARGS__));            \
            noerrorShim();							\
            return (nam##_RETURN)0;                 \
        }                                           \
        else gl4es_flush();                               \
    }

#define PUSH_IF_COMPILING(name) PUSH_IF_COMPILING_EXT(name, name##_ARG_NAMES)

#if defined(_WIN32) || defined(_WIN64)
  #define THREAD_LOCAL __declspec(thread)
#elif (__STDC_VERSION__ > 201710L) // > C23
  #define THREAD_LOCAL thread_local
#elif (__STDC_VERSION__ >= 201112L) // >= C11
  #define THREAD_LOCAL _Thread_local
#elif defined (__GCC__) || defined(__clang__)
  #define THREAD_LOCAL __thread
#else
  #define THREAD_LOCAL 
#endif

#define DEFINE_RAW(lib, name) static name##_PTR lib##_##name = NULL
#define LOAD_RAW(lib, name, ...) \
    { \
        static THREAD_LOCAL bool first = true; \
        if (first) { \
            first = false; \
            if (lib != NULL) { \
                lib##_##name = (name##_PTR)__VA_ARGS__; \
            } \
            WARN_NULL(lib##_##name); \
        } \
    }

#define LOAD_RAW_3(lib, name, fnc1, fnc2, ...) \
    { \
        static THREAD_LOCAL bool first = true; \
        if (first) { \
            first = false; \
            if (lib != NULL) { \
                lib##_##name = (name##_PTR)fnc1; \
                if(! lib##_##name) \
                    lib##_##name = (name##_PTR)fnc2; \
                if(! lib##_##name) { \
                    __VA_ARGS__ \
                } \
            } \
        } \
    }

#define LOAD_RAW_SILENT(lib, name, ...) \
    { \
        static THREAD_LOCAL bool first = true; \
        if (first) { \
            first = false; \
            if (lib != NULL) { \
                lib##_##name = (name##_PTR)__VA_ARGS__; \
            } \
        } \
    }

#define LOAD_RAW_ALT(lib, alt, name, ...) \
    { \
        static THREAD_LOCAL bool first = true; \
        if (first) { \
            first = false; \
            if (lib != NULL) { \
                lib##_##name = (name##_PTR)__VA_ARGS__; \
            } \
            if(lib##_##name == NULL) \
                lib##_##name = alt##_##name; \
        } \
    }

#define LOAD_LIB(lib, name) DEFINE_RAW(lib, name); LOAD_RAW(lib, name, proc_address(lib, #name))
#define LOAD_LIB_SILENT(lib, name) DEFINE_RAW(lib, name); LOAD_RAW_SILENT(lib, name, proc_address(lib, #name))
#define LOAD_LIB_ALT(lib, alt, name) DEFINE_RAW(lib, name); LOAD_RAW_ALT(lib, alt, name, proc_address(lib, #name))

#define LOAD_GLES(name)         LOAD_LIB(gles, name)
#define LOAD_GLES2(name)        LOAD_LIB_SILENT(gles, name)
#define LOAD_GLES_OR_FPE(name)  LOAD_LIB_ALT(gles, fpe, name)

#define LOAD_GLES_FPE(name) \
    DEFINE_RAW(gles, name); \
    if(hardext.esversion==1) { \
        LOAD_RAW(gles, name, proc_address(gles, #name)); \
    } else { \
        gles_##name = fpe_##name; \
    }

#define LOAD_EGL(name) LOAD_LIB(egl, name)

#define LOAD_GBM(name) LOAD_LIB(gbm, name)

#if defined(AMIGAOS4) || defined(NOEGL) || defined(__EMSCRIPTEN__)
#define LOAD_GLES_OES(name) \
    DEFINE_RAW(gles, name); \
    { \
        LOAD_RAW(gles, name, proc_address(gles, #name"OES")); \
    }

#define LOAD_GLES_EXT(name) \
    DEFINE_RAW(gles, name); \
    { \
        LOAD_RAW(gles, name, proc_address(gles, #name"EXT")); \
    }

#define LOAD_GLES2_OR_OES(name) \
    DEFINE_RAW(gles, name); \
    { \
        LOAD_RAW_SILENT(gles, name, proc_address(gles, #name)); \
    }

#else // defined(AMIGAOS4) || defined(NOEGL)

#define LOAD_EGL_EXT(name) \
    DEFINE_RAW(egl, name); \
    LOAD_RAW_3(egl, name, proc_address(egl, #name), proc_address(egl, #name "EXT"), LOAD_EGL(eglGetProcAddress); LOAD_RAW(egl, name, egl_eglGetProcAddress(#name "EXT")); )

#define LOAD_GLES_OES(name) \
    DEFINE_RAW(gles, name); \
    { \
        LOAD_EGL(eglGetProcAddress); \
        LOAD_RAW(gles, name, egl_eglGetProcAddress(#name"OES")); \
    }

#define LOAD_GLES_EXT(name) \
    DEFINE_RAW(gles, name); \
    { \
        LOAD_EGL(eglGetProcAddress); \
        LOAD_RAW(gles, name, egl_eglGetProcAddress(#name"EXT")); \
    }

#define LOAD_GLES2_OR_OES(name) \
    DEFINE_RAW(gles, name); \
    { \
        LOAD_EGL(eglGetProcAddress); \
        LOAD_RAW_SILENT(gles, name, ((hardext.esversion==1)?((void*)egl_eglGetProcAddress(#name"OES")):((void*)dlsym(gles, #name)))); \
    }
#endif // defined(AMIGAOS4) || defined(NOEGL)

#endif // _GL4ES_LOADER_H_
