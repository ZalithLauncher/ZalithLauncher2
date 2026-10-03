#include "loader.h"

void (APIENTRY_GL4ES *gl4es_getMainFBSize)(GLint* width, GLint* height);

#if defined NO_LOADER

void *gles = (void*)(~(uintptr_t)0);
void *egl = (void*)(~(uintptr_t)0);
void *open_lib(const char **names, const char *override) {
    return (void*)(~(uintptr_t)0);
}
void load_libs() {
}

#elif defined AMIGAOS4
#include "../agl/amigaos.h"
void *gles;
void load_libs() {
    os4OpenLib(&gles);
}

void *open_lib(const char **names, const char *override) {
    return (void*)(~(uintptr_t)0);
}


#else
#ifndef _WIN32
// PATH_MAX
#ifdef __linux__
#include <linux/limits.h>
#else
#include <limits.h>
#endif
#else
__declspec(dllimport)
struct HINSTANCE__* __stdcall LoadLibraryW(const wchar_t*);
#endif
#include "logs.h"
#include "init.h"
#include "envvars.h"

#ifndef DEFAULT_GLES
#define DEFAULT_GLES NULL
#endif
#ifndef DEFAULT_EGL
#define DEFAULT_EGL NULL
#endif

void *gles = NULL, *egl = NULL, *bcm_host = NULL, *vcos = NULL, *gbm = NULL, *drm = NULL;
#ifndef _WIN32
#ifndef NO_GBM
static const char *drm_lib[] = {
    "libdrm",
    NULL
};
static const char *gbm_lib[] = {
    "libgbm",
    NULL
};
#endif

static const char *path_prefix[] = {
    "",
    "/opt/vc/lib/",
    "/usr/local/lib/",
    "/usr/lib/",
    NULL,
};

static const char *lib_ext[] = {
#ifndef NO_GBM
    "so.19",
#endif
    "so",
    "so.1",
    "so.2",
    "dylib",
    "dll",
    NULL,
};

static const char *gles2_lib[] = {
    #if defined(BCMHOST)
    "libbrcmGLESv2",
    #endif
    "libGLESv2_CM",
    "libGLESv2",
    NULL
};

static const char *gles_lib[] = {
    #if defined(BCMHOST)
    "libbrcmGLESv1_CM",
    #endif
    #if !defined(PYRA)
    "libGLESv1_CM",
    #endif
    "libGLES_CM",
    NULL
};

static const char *egl_lib[] = {
    #if defined(BCMHOST)
    "libbrcmEGL",
    #endif
    "libEGL",
    NULL
};

void *open_lib(const char **names, const char *override) {
    void *lib = NULL;

    char path_name[PATH_MAX + 1];
    int flags = RTLD_LOCAL | RTLD_NOW;
#if defined(RTLD_DEEPBIND)
    static int totest = 1;
    static int sanitizer = 0;
    if(totest) {
        totest = 0;
        char *p = getenv("LD_PRELOAD");
        if(p && strstr(p, "libasan.so"))
            sanitizer = 1;
    }
    // note: breaks address sanitizer
    if(!sanitizer && globals4es.deepbind)
        flags |= RTLD_DEEPBIND;
#endif
    if (override) {
        if ((lib = dlopen(override, flags))) {
            strncpy(path_name, override, PATH_MAX);
            if(!globals4es.nobanner) LOGD("LIBGL:loaded: %s\n", path_name);
            return lib;
        } else {
            LOGE("LIBGL_GLES override failed: %s\n", dlerror());
        }
    }
    for (int p = 0; path_prefix[p]; p++) {
        for (int i = 0; names[i]; i++) {
            for (int e = 0; lib_ext[e]; e++) {
                snprintf(path_name, PATH_MAX, "%s%s.%s", path_prefix[p], names[i], lib_ext[e]);
                if ((lib = dlopen(path_name, flags))) {
                    if(!globals4es.nobanner) LOGD("loaded: %s\n", path_name);
                    return lib;
                }
            }
        }
    }
    return lib;
}
#else  // _WIN32
void* open_lib(const wchar_t* envvar, const wchar_t* dll)
{
    const wchar_t* name = _wgetenv(envvar);
    return LoadLibraryW(name?name:dll);
}
#endif

void load_libs() {
    static int first = 1;
    if (! first) return;
    first = 0;
#ifndef _WIN32
    const char *gles_override = GetEnvVar("LIBGL_GLES");
    if (!gles_override) {
        gles_override = DEFAULT_GLES;
#if defined(BCMHOST) && !defined(ANDROID)
        // optimistically try to load the raspberry pi libs
        if (! gles_override) {
            const char *bcm_host_name[] = {"libbcm_host", NULL};
            const char *vcos_name[] = {"libvcos", NULL};
            bcm_host = open_lib(bcm_host_name, NULL);
            vcos = open_lib(vcos_name, NULL);
        }
#endif
    }
    gles = open_lib((globals4es.es==1)?gles_lib:gles2_lib, gles_override);
#else
    gles = open_lib(L"LIBGL_GLES", L"libGLESv2.dll");
#endif
    WARN_NULL(gles);

#ifdef NOEGL
    egl = gles;
#elif !defined(_WIN32)
    const char *egl_override = GetEnvVar("LIBGL_EGL");
    if (!egl_override) {
        egl_override = DEFAULT_EGL;
    }
    egl = open_lib(egl_lib, egl_override);
#else
    egl = open_lib(L"LIBGL_EGL", L"libEGL.dll");
#endif
    WARN_NULL(egl);

#ifndef NO_GBM
    const char *gbm_override = GetEnvVar("LIBGL_GBM");
    gbm = open_lib(gbm_lib, gbm_override);
    const char *drm_override = GetEnvVar("LIBGL_DRM");
    drm = open_lib(drm_lib, drm_override);
#endif
}
#endif

// user-defined getProcAddress
void* (APIENTRY_GL4ES *gles_getProcAddress)(const char *name);

void* APIENTRY_GL4ES proc_address(void *lib, const char *name) {
    if (gles_getProcAddress)
        return gles_getProcAddress(name);
#ifdef AMIGAOS4
    return os4GetProcAddress(name);
#elif defined __EMSCRIPTEN__
    void *emscripten_GetProcAddress(const char *name);
    return emscripten_GetProcAddress(name);
#elif defined __APPLE__
    // apple code seems to use RTLD_NEXT which is usually ((void*)-1)
    // remove if it not needed
    return dlsym((void*)(~(uintptr_t)0), name);
#elif !defined NO_LOADER
    return dlsym(lib, name);
#else
    return NULL;
#endif
}
