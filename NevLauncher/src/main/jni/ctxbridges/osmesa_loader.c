//
// Modifiled by Vera-Firefly on 15.01.2025.
//
#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include "environ/environ.h"
#include "br_loader.h"
#include "osmesa_loader.h"
#include "renderer_config.h"

GLboolean (*OSMesaMakeCurrent_p) (OSMesaContext ctx, void *buffer, GLenum type, GLsizei width, GLsizei height);
OSMesaContext (*OSMesaGetCurrentContext_p) (void);
OSMesaContext (*OSMesaCreateContext_p) (GLenum format, OSMesaContext sharelist);
void (*OSMesaDestroyContext_p) (OSMesaContext ctx);
void (*OSMesaFlushFrontbuffer_p) ();
void (*OSMesaPixelStore_p) (GLint pname, GLint value);
GLubyte* (*glGetString_p) (GLenum name);
void (*glFinish_p) (void);
void (*glClearColor_p) (GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha);
void (*glClear_p) (GLbitfield mask);
void (*glReadPixels_p) (GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, void* data);
void (*glReadBuffer_p) (GLenum mode);

bool is_renderer_vulkan() {
    return (pojav_environ->config_renderer == RENDERER_VK_ZINK
         || pojav_environ->config_renderer == RENDERER_VIRGL);
}

char* construct_main_path(const char* mesa_name, const char* pojav_native_dir) {
    char* main_path = NULL;if (mesa_name != NULL && strncmp(mesa_name, "/data", 5) == 0) {
        main_path = strdup(mesa_name);
    } else {
        if (asprintf(&main_path, "%s/%s", pojav_native_dir, mesa_name) == -1) {
            return NULL;
        }
    }
    return main_path;
}

void dlsym_OSMesa() {
    if (!is_renderer_vulkan()) return;

    char* mesa_name = getenv("LIB_MESA_NAME");
    char* pojav_native_dir = getenv("POJAV_NATIVEDIR");

    char* main_path = construct_main_path(mesa_name, pojav_native_dir);
    if (!main_path) {
        fprintf(stderr, "Error: Failed to construct main path.\n");
        abort();
    }

    void* dl_handle = dlopen(main_path, RTLD_LOCAL | RTLD_LAZY);
    free(main_path);
    if (!dl_handle) {
        fprintf(stderr, "Error: Failed to open library: %s\n", dlerror());
        abort();
    }

    OSMesaMakeCurrent_p = OSMGetProcAddress(dl_handle, "OSMesaMakeCurrent");
    OSMesaGetCurrentContext_p = OSMGetProcAddress(dl_handle, "OSMesaGetCurrentContext");
    OSMesaCreateContext_p = OSMGetProcAddress(dl_handle, "OSMesaCreateContext");
    OSMesaDestroyContext_p = OSMGetProcAddress(dl_handle, "OSMesaDestroyContext");
    OSMesaFlushFrontbuffer_p = OSMGetProcAddress(dl_handle, "OSMesaFlushFrontbuffer");
    OSMesaPixelStore_p = OSMGetProcAddress(dl_handle, "OSMesaPixelStore");
    glGetString_p = OSMGetProcAddress(dl_handle, "glGetString");
    glClearColor_p = OSMGetProcAddress(dl_handle, "glClearColor");
    glClear_p = OSMGetProcAddress(dl_handle, "glClear");
    glFinish_p = OSMGetProcAddress(dl_handle, "glFinish");
    glReadPixels_p = OSMGetProcAddress(dl_handle, "glReadPixels");
    glReadBuffer_p = OSMGetProcAddress(dl_handle, "glReadBuffer");

}