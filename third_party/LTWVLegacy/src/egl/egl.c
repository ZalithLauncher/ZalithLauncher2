
#include "egl.h"
#include "../gl/loader.h"
#include "../glx/hardext.h"

#ifndef AliasExport
#define AliasExport(name)   __attribute__((alias(name))) __attribute__((visibility("default")))
#endif

static EGLint egl_context_attrib_es2[] = {
    EGL_CONTEXT_CLIENT_VERSION, 2,
    EGL_NONE
};

static EGLint egl_context_attrib[] = {
    EGL_NONE
};

EGLint gl4es_eglGetError(void) {
    LOAD_EGL(eglGetError);
    return egl_eglGetError();
}

EGLDisplay gl4es_eglGetDisplay(EGLNativeDisplayType display_id) {
    LOAD_EGL(eglGetDisplay);
    return egl_eglGetDisplay(display_id);
}

EGLBoolean gl4es_eglInitialize(EGLDisplay dpy, EGLint *major, EGLint *minor) {
    LOAD_EGL(eglInitialize);
    return egl_eglInitialize(dpy, major, minor);
}

EGLBoolean gl4es_eglTerminate(EGLDisplay dpy) {
    LOAD_EGL(eglTerminate);
    return egl_eglTerminate(dpy);
}

const char * gl4es_eglQueryString(EGLDisplay dpy, EGLint name) {
    LOAD_EGL(eglQueryString);
    return egl_eglQueryString(dpy, name);
}

EGLBoolean gl4es_eglGetConfigs(EGLDisplay dpy, EGLConfig *configs, EGLint config_size, EGLint *num_config) {
    LOAD_EGL(eglGetConfigs);
    return egl_eglGetConfigs(dpy, configs, config_size, num_config);
}

EGLBoolean gl4es_eglChooseConfig(EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config) {
    LOAD_EGL(eglChooseConfig);
    return egl_eglChooseConfig(dpy, attrib_list, configs, config_size, num_config);
}

EGLBoolean gl4es_eglGetConfigAttrib(EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value) {
    EGLint old_value = 0;
    LOAD_EGL(eglGetConfigAttrib);
    EGLBoolean ret = egl_eglGetConfigAttrib(dpy, config, attribute, &old_value);
    switch (attribute) {
        case EGL_RENDERABLE_TYPE:
            *value = old_value | EGL_OPENGL_BIT;
        break;
        default:
            *value = old_value;
    }
    return ret;
}

EGLSurface gl4es_eglCreateWindowSurface(EGLDisplay dpy, EGLConfig config, EGLNativeWindowType win, const EGLint *attrib_list) {
    LOAD_EGL(eglCreateWindowSurface);
    return egl_eglCreateWindowSurface(dpy, config, win, attrib_list);
}

EGLSurface gl4es_eglCreatePbufferSurface(EGLDisplay dpy, EGLConfig config, const EGLint *attrib_list) {
    LOAD_EGL(eglCreatePbufferSurface);
    return egl_eglCreatePbufferSurface(dpy, config, attrib_list);
}

EGLSurface gl4es_eglCreatePixmapSurface(EGLDisplay dpy, EGLConfig config, EGLNativePixmapType pixmap, const EGLint *attrib_list) {
    LOAD_EGL(eglCreatePixmapSurface);
    return egl_eglCreatePixmapSurface(dpy, config, pixmap, attrib_list);
}

EGLBoolean gl4es_eglDestroySurface(EGLDisplay dpy, EGLSurface surface) {
    LOAD_EGL(eglDestroySurface);
    return egl_eglDestroySurface(dpy, surface);
}

EGLBoolean gl4es_eglQuerySurface(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint *value) {
    LOAD_EGL(eglQuerySurface);
    return egl_eglQuerySurface(dpy, surface, attribute, value);
}

EGLBoolean gl4es_eglBindAPI(EGLenum api) {
    LOAD_EGL(eglBindAPI);
    return egl_eglBindAPI(EGL_OPENGL_ES_API);
}

EGLenum gl4es_eglQueryAPI(void) {
    return EGL_OPENGL_API;
}

EGLBoolean gl4es_eglWaitClient(void) {
    LOAD_EGL(eglWaitClient);
    return egl_eglWaitClient();
}

EGLBoolean gl4es_eglReleaseThread(void) {
    LOAD_EGL(eglReleaseThread);
    return egl_eglReleaseThread();
}

EGLSurface gl4es_eglCreatePbufferFromClientBuffer(EGLDisplay dpy, EGLenum buftype, EGLClientBuffer buffer, EGLConfig config, const EGLint *attrib_list) {
    LOAD_EGL(eglCreatePbufferFromClientBuffer);
    return egl_eglCreatePbufferFromClientBuffer(dpy, buftype, buffer, config, attrib_list);
}

EGLBoolean gl4es_eglSurfaceAttrib(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint value) {
    LOAD_EGL(eglSurfaceAttrib);
    return egl_eglSurfaceAttrib(dpy, surface, attribute, value);
}

EGLBoolean gl4es_eglBindTexImage(EGLDisplay dpy, EGLSurface surface, EGLint buffer) {
    LOAD_EGL(eglBindTexImage);
    return egl_eglBindTexImage(dpy, surface, buffer);
}

EGLBoolean gl4es_eglReleaseTexImage(EGLDisplay dpy, EGLSurface surface, EGLint buffer) {
    LOAD_EGL(eglReleaseTexImage);
    return egl_eglReleaseTexImage(dpy, surface, buffer);
}

EGLBoolean gl4es_eglSwapInterval(EGLDisplay dpy, EGLint interval) {
    LOAD_EGL(eglSwapInterval);
    return egl_eglSwapInterval(dpy, interval);
}

EGLContext gl4es_eglCreateContext(EGLDisplay dpy, EGLConfig config, EGLContext share_context, const EGLint *attrib_list) {
    LOAD_EGL(eglCreateContext);
    return egl_eglCreateContext(dpy, config, share_context, (hardext.esversion == 1) ? egl_context_attrib : egl_context_attrib_es2);
}

EGLBoolean gl4es_eglDestroyContext(EGLDisplay dpy, EGLContext ctx) {
    LOAD_EGL(eglDestroyContext);
    return egl_eglDestroyContext(dpy, ctx);
}

EGLBoolean gl4es_eglMakeCurrent(EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx) {
    LOAD_EGL(eglMakeCurrent);
    return egl_eglMakeCurrent(dpy, draw, read, ctx);
}

EGLContext gl4es_eglGetCurrentContext(void) {
    LOAD_EGL(eglGetCurrentContext);
    return egl_eglGetCurrentContext();
}

EGLSurface gl4es_eglGetCurrentSurface(EGLint readdraw) {
    LOAD_EGL(eglGetCurrentSurface);
    return egl_eglGetCurrentSurface(readdraw);
}

EGLDisplay gl4es_eglGetCurrentDisplay(void) {
    LOAD_EGL(eglGetCurrentDisplay);
    return egl_eglGetCurrentDisplay();
}

EGLDisplay gl4es_eglGetPlatformDisplay(EGLenum platform, void *native_display, const EGLAttrib *attrib_list) {
    LOAD_EGL_EXT(eglGetPlatformDisplay);
    if (egl_eglGetPlatformDisplay)
        return egl_eglGetPlatformDisplay(platform, native_display, attrib_list);
    else {
        LOAD_EGL(eglGetDisplay);
        return egl_eglGetDisplay((EGLNativeDisplayType)native_display);
    }
}

EGLBoolean gl4es_eglQueryContext(EGLDisplay dpy, EGLContext ctx, EGLint attribute, EGLint *value) {
    LOAD_EGL(eglQueryContext);
    return egl_eglQueryContext(dpy, ctx, attribute, value);
}

EGLBoolean gl4es_eglWaitGL(void) {
    LOAD_EGL(eglWaitGL);
    return egl_eglWaitGL();
}

EGLBoolean gl4es_eglWaitNative(EGLint engine) {
    LOAD_EGL(eglWaitNative);
    return egl_eglWaitNative(engine);
}

EGLBoolean gl4es_eglSwapBuffers(EGLDisplay dpy, EGLSurface surface) {
    LOAD_EGL(eglSwapBuffers);
    return egl_eglSwapBuffers(dpy, surface);
}

EGLBoolean gl4es_eglCopyBuffers(EGLDisplay dpy, EGLSurface surface, EGLNativePixmapType target) {
    LOAD_EGL(eglCopyBuffers);
    return egl_eglCopyBuffers(dpy, surface, target);
}

EGLSyncKHR gl4es_eglCreateSyncKHR(EGLDisplay dpy, EGLenum type, const EGLint *attrib_list) {
    LOAD_EGL(eglCreateSyncKHR);
    return egl_eglCreateSyncKHR(dpy, type, attrib_list);
}

EGLSyncKHR gl4es_eglDestroySyncKHR(EGLDisplay dpy, EGLSyncKHR sync) {
    LOAD_EGL(eglDestroySyncKHR);
    return egl_eglDestroySyncKHR(dpy, sync);
}

EGLint gl4es_eglClientWaitSyncKHR(EGLDisplay dpy, EGLSyncKHR sync, EGLint flags, EGLTimeKHR timeout) {
    LOAD_EGL(eglClientWaitSyncKHR);
    return egl_eglClientWaitSyncKHR(dpy, sync, flags, timeout);
}

NativePixmapType gl4es_egl_create_pixmap_ID_mapping(void *pixmap)
{
    LOAD_EGL(egl_create_pixmap_ID_mapping);
    return egl_egl_create_pixmap_ID_mapping(pixmap);
}

NativePixmapType gl4es_egl_destroy_pixmap_ID_mapping(int id)
{
    LOAD_EGL(egl_destroy_pixmap_ID_mapping);
    return egl_egl_destroy_pixmap_ID_mapping(id);
}

AliasExport(EGLint, eglGetError,,(void));
AliasExport(EGLDisplay, eglGetDisplay,,(EGLNativeDisplayType display_id));
AliasExport(EGLBoolean, eglInitialize,,(EGLDisplay dpy, EGLint *major, EGLint *minor));
AliasExport(EGLBoolean, eglTerminate,,(EGLDisplay dpy));
AliasExport(const char *, eglQueryString,,(EGLDisplay dpy, EGLint name));
AliasExport(EGLBoolean, eglGetConfigs,,(EGLDisplay dpy, EGLConfig *configs, EGLint config_size, EGLint *num_config));
AliasExport(EGLBoolean, eglChooseConfig,,(EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config));
AliasExport(EGLBoolean, eglGetConfigAttrib,,(EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value));
AliasExport(EGLSurface, eglCreateWindowSurface,,(EGLDisplay dpy, EGLConfig config, EGLNativeWindowType win, const EGLint *attrib_list));
AliasExport(EGLSurface, eglCreatePbufferSurface,,(EGLDisplay dpy, EGLConfig config, const EGLint *attrib_list));
AliasExport(EGLSurface, eglCreatePixmapSurface,,(EGLDisplay dpy, EGLConfig config, EGLNativePixmapType pixmap, const EGLint *attrib_list));
AliasExport(EGLBoolean, eglDestroySurface,,(EGLDisplay dpy, EGLSurface surface));
AliasExport(EGLBoolean, eglQuerySurface,,(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint *value));
AliasExport(EGLBoolean, eglBindAPI,,(EGLenum api));
AliasExport(EGLenum, eglQueryAPI,,(void));
AliasExport(EGLBoolean, eglWaitClient,,(void));
AliasExport(EGLBoolean, eglReleaseThread,,(void));
AliasExport(EGLSurface, eglCreatePbufferFromClientBuffer,,(EGLDisplay dpy, EGLenum buftype, EGLClientBuffer buffer, EGLConfig config, const EGLint *attrib_list));
AliasExport(EGLBoolean, eglSurfaceAttrib,,(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint value));
AliasExport(EGLBoolean, eglBindTexImage,,(EGLDisplay dpy, EGLSurface surface, EGLint buffer));
AliasExport(EGLBoolean, eglReleaseTexImage,,(EGLDisplay dpy, EGLSurface surface, EGLint buffer));
AliasExport(EGLBoolean, eglSwapInterval,,(EGLDisplay dpy, EGLint interval));
AliasExport(EGLContext, eglCreateContext,,(EGLDisplay dpy, EGLConfig config, EGLContext share_context, const EGLint *attrib_list));
AliasExport(EGLBoolean, eglDestroyContext,,(EGLDisplay dpy, EGLContext ctx));
AliasExport(EGLBoolean, eglMakeCurrent,,(EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx));
AliasExport(EGLContext, eglGetCurrentContext,,(void));
AliasExport(EGLSurface, eglGetCurrentSurface,,(EGLint readdraw));
AliasExport(EGLDisplay, eglGetCurrentDisplay,,(void));
AliasExport(EGLDisplay, eglGetPlatformDisplay,, (EGLenum platform, void *native_display, const EGLAttrib *attrib_list));
AliasExport(EGLDisplay, eglGetPlatformDisplay, EXT, (EGLenum platform, void *native_display, const EGLAttrib *attrib_list));
AliasExport(EGLBoolean, eglQueryContext,,(EGLDisplay dpy, EGLContext ctx, EGLint attribute, EGLint *value));
AliasExport(EGLBoolean, eglWaitGL,,(void));
AliasExport(EGLBoolean, eglWaitNative,,(EGLint engine));
AliasExport(EGLBoolean, eglSwapBuffers,,(EGLDisplay dpy, EGLSurface surface));
AliasExport(EGLBoolean, eglCopyBuffers,,(EGLDisplay dpy, EGLSurface surface, EGLNativePixmapType target));
AliasExport(EGLSyncKHR, eglCreateSyncKHR,,(EGLDisplay dpy, EGLenum type, const EGLint *attrib_list));
AliasExport(EGLSyncKHR, eglDestroySyncKHR,,(EGLDisplay dpy, EGLSyncKHR sync));
AliasExport(EGLint, eglClientWaitSyncKHR,,(EGLDisplay dpy, EGLSyncKHR sync, EGLint flags, EGLTimeKHR timeout));

AliasExport(NativePixmapType, egl_create_pixmap_ID_mapping,,(void *pixmap));
AliasExport(NativePixmapType, egl_destroy_pixmap_ID_mapping,,(int id));