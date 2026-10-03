#ifndef _EGL_EGL_H_
#define _EGL_EGL_H_

#ifdef NOEGL
# error Building EGL wrapper without EGL
#endif

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <stdbool.h>
#include <stdlib.h>

EGLint gl4es_eglGetError(void);
EGLDisplay gl4es_eglGetDisplay(EGLNativeDisplayType display_id);
EGLBoolean gl4es_eglInitialize(EGLDisplay dpy, EGLint *major, EGLint *minor);
EGLBoolean gl4es_eglTerminate(EGLDisplay dpy);
const char * gl4es_eglQueryString(EGLDisplay dpy, EGLint name);
EGLBoolean gl4es_eglGetConfigs(EGLDisplay dpy, EGLConfig *configs, EGLint config_size, EGLint *num_config);
EGLBoolean gl4es_eglChooseConfig(EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config);
EGLBoolean gl4es_eglGetConfigAttrib(EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value);

EGLSurface gl4es_eglCreateWindowSurface(EGLDisplay dpy, EGLConfig config, EGLNativeWindowType win, const EGLint *attrib_list);
EGLSurface gl4es_eglCreatePbufferSurface(EGLDisplay dpy, EGLConfig config, const EGLint *attrib_list);
EGLSurface gl4es_eglCreatePixmapSurface(EGLDisplay dpy, EGLConfig config, EGLNativePixmapType pixmap, const EGLint *attrib_list);
EGLBoolean gl4es_eglDestroySurface(EGLDisplay dpy, EGLSurface surface);
EGLBoolean gl4es_eglQuerySurface(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint *value);
EGLBoolean gl4es_eglBindAPI(EGLenum api);
EGLenum gl4es_eglQueryAPI(void);

EGLBoolean gl4es_eglWaitClient(void);
EGLBoolean gl4es_eglReleaseThread(void);
EGLSurface gl4es_eglCreatePbufferFromClientBuffer(EGLDisplay dpy, EGLenum buftype, EGLClientBuffer buffer, EGLConfig config, const EGLint *attrib_list);
EGLBoolean gl4es_eglSurfaceAttrib(EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint value);
EGLBoolean gl4es_eglBindTexImage(EGLDisplay dpy, EGLSurface surface, EGLint buffer);
EGLBoolean gl4es_eglReleaseTexImage(EGLDisplay dpy, EGLSurface surface, EGLint buffer);
EGLBoolean gl4es_eglSwapInterval(EGLDisplay dpy, EGLint interval);
EGLContext gl4es_eglCreateContext(EGLDisplay dpy, EGLConfig config, EGLContext share_context, const EGLint *attrib_list);
EGLBoolean gl4es_eglDestroyContext(EGLDisplay dpy, EGLContext ctx);
EGLBoolean gl4es_eglMakeCurrent(EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx);
EGLContext gl4es_eglGetCurrentContext(void);
EGLSurface gl4es_eglGetCurrentSurface(EGLint readdraw);
EGLDisplay gl4es_eglGetCurrentDisplay(void);
EGLDisplay gl4es_eglGetPlatformDisplay(EGLenum platform, void *native_display, const EGLAttrib *attrib_list);
EGLBoolean gl4es_eglQueryContext(EGLDisplay dpy, EGLContext ctx, EGLint attribute, EGLint *value);
EGLBoolean gl4es_eglWaitGL(void);
EGLBoolean gl4es_eglWaitNative(EGLint engine);
EGLBoolean gl4es_eglSwapBuffers(EGLDisplay dpy, EGLSurface surface);
EGLBoolean gl4es_eglCopyBuffers(EGLDisplay dpy, EGLSurface surface, EGLNativePixmapType target);
void* gl4es_eglGetProcAddress(const char *name);

EGLSyncKHR gl4es_eglCreateSyncKHR(EGLDisplay dpy, EGLenum type, const EGLint *attrib_list);
EGLSyncKHR gl4es_eglDestroySyncKHR(EGLDisplay dpy, EGLSyncKHR sync);
EGLint gl4es_eglClientWaitSyncKHR(EGLDisplay dpy, EGLSyncKHR sync, EGLint flags, EGLTimeKHR timeout);

// Undocumented libmali internals, needed for ODROID Go Ultra
NativePixmapType gl4es_egl_create_pixmap_ID_mapping(void *pixmap);
NativePixmapType gl4es_egl_destroy_pixmap_ID_mapping(int id);

#endif // _EGL_EGL_H

