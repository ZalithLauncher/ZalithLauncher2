//
// Created by maks on 21.09.2022.
//
#include <stddef.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include "br_loader.h"
#include "egl_loader.h"

EGLBoolean (*eglMakeCurrent_p) (EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx);
EGLBoolean (*eglDestroyContext_p) (EGLDisplay dpy, EGLContext ctx);
EGLBoolean (*eglDestroySurface_p) (EGLDisplay dpy, EGLSurface surface);
EGLBoolean (*eglTerminate_p) (EGLDisplay dpy);
EGLBoolean (*eglReleaseThread_p) (void);
EGLContext (*eglGetCurrentContext_p) (void);
EGLDisplay (*eglGetDisplay_p) (NativeDisplayType display);
EGLBoolean (*eglInitialize_p) (EGLDisplay dpy, EGLint *major, EGLint *minor);
EGLBoolean (*eglChooseConfig_p) (EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config);
EGLBoolean (*eglGetConfigAttrib_p) (EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value);
EGLBoolean (*eglBindAPI_p) (EGLenum api);
EGLSurface (*eglCreatePbufferSurface_p) (EGLDisplay dpy, EGLConfig config, const EGLint *attrib_list);
EGLSurface (*eglCreateWindowSurface_p) (EGLDisplay dpy, EGLConfig config, NativeWindowType window, const EGLint *attrib_list);
EGLBoolean (*eglSwapBuffers_p) (EGLDisplay dpy, EGLSurface draw);
EGLint (*eglGetError_p) (void);
EGLContext (*eglCreateContext_p) (EGLDisplay dpy, EGLConfig config, EGLContext share_list, const EGLint *attrib_list);
EGLBoolean (*eglSwapInterval_p) (EGLDisplay dpy, EGLint interval);
EGLSurface (*eglGetCurrentSurface_p) (EGLint readdraw);
EGLBoolean (*eglQuerySurface_p)(EGLDisplay display, EGLSurface surface, EGLint attribute, EGLint * value);

void dlsym_EGL() {
    void* dl_handle = NULL;
    char* eglName = NULL;
    char* gles = getenv("LIBGL_GLES");

    if (gles && !strncmp(gles, "libGLESv2_angle.so", 18))
    {
        eglName = "libEGL_angle.so";
    } else {
        char* execEgl = getenv("POJAVEXEC_EGL");
        eglName = gles ? gles : (execEgl ? execEgl : "libEGL.so");
    }

    if (eglName)
        dl_handle = dlopen(eglName, RTLD_LOCAL | RTLD_LAZY);

    if (dl_handle == NULL)
        dl_handle = dlopen("libEGL.so", RTLD_LOCAL | RTLD_LAZY);

    if (dl_handle == NULL) abort();

    eglBindAPI_p = GLGetProcAddress(dl_handle, "eglBindAPI");
    eglChooseConfig_p = GLGetProcAddress(dl_handle, "eglChooseConfig");
    eglCreateContext_p = GLGetProcAddress(dl_handle, "eglCreateContext");
    eglCreatePbufferSurface_p = GLGetProcAddress(dl_handle, "eglCreatePbufferSurface");
    eglCreateWindowSurface_p = GLGetProcAddress(dl_handle, "eglCreateWindowSurface");
    eglDestroyContext_p = GLGetProcAddress(dl_handle, "eglDestroyContext");
    eglDestroySurface_p = GLGetProcAddress(dl_handle, "eglDestroySurface");
    eglGetConfigAttrib_p = GLGetProcAddress(dl_handle, "eglGetConfigAttrib");
    eglGetCurrentContext_p = GLGetProcAddress(dl_handle, "eglGetCurrentContext");
    eglGetDisplay_p = GLGetProcAddress(dl_handle, "eglGetDisplay");
    eglGetError_p = GLGetProcAddress(dl_handle, "eglGetError");
    eglInitialize_p = GLGetProcAddress(dl_handle, "eglInitialize");
    eglMakeCurrent_p = GLGetProcAddress(dl_handle, "eglMakeCurrent");
    eglSwapBuffers_p = GLGetProcAddress(dl_handle, "eglSwapBuffers");
    eglReleaseThread_p = GLGetProcAddress(dl_handle, "eglReleaseThread");
    eglSwapInterval_p = GLGetProcAddress(dl_handle, "eglSwapInterval");
    eglTerminate_p = GLGetProcAddress(dl_handle, "eglTerminate");
    eglGetCurrentSurface_p = GLGetProcAddress(dl_handle,"eglGetCurrentSurface");
    eglQuerySurface_p = GLGetProcAddress(dl_handle, "eglQuerySurface");
}
