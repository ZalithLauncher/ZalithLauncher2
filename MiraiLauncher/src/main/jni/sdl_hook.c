// Reference AAMC: https://github.com/AngelAuraMC/Amethyst-Android/blob/360d708262ff703d9b52782d20cd348410a33df5/app_pojavlauncher/src/main/jni/native_hooks/sdl_hook.c

#include <stdbool.h>
#include <stdint.h>

#include "environ/environ.h"
#include "utils.h"
#include "logger/logger.h"
#include "sdl_hook.h"

#include <bytehook.h>
#include <dlfcn.h>
#include <jni.h>
#include <stdlib.h>
#include <string.h>

// --- 最小 SDL3 声明（仅 hook 所需；完整 headers 由 lwjgl-sdl 绑定侧提供） ---
typedef uint32_t SDL_InitFlags;
typedef struct SDL_Window SDL_Window;
typedef struct SDL_Rect { int x, y, w, h; } SDL_Rect;

bool SDL_InitSubSystem(SDL_InitFlags flags);
bool SDL_SetHint(const char *name, const char *value);
bool SDL_SetTextInputArea(SDL_Window *window, const SDL_Rect *rect, int cursor);
void SDL_SetError(const char *fmt, ...);
const char *SDL_GetError(void);
SDL_Window *SDL_GetWindowFromEvent(const void *event);
SDL_Window *SDL_GetWindowFromID(uint32_t id);
bool SDL_GL_SetAttribute(int attr, int value);
void *SDL_LoadObject(const char *path);
void SDL_UnloadObject(void *handle);
void *SDL_LoadFunction(void *handle, const char *name);
SDL_Window *SDL_CreateWindow(const char *title, int w, int h, uint32_t flags);
SDL_Window *SDL_CreateWindowWithProperties(uint32_t props);
void SDL_DestroyWindow(SDL_Window *window);
void *SDL_EGL_GetProcAddress(const char *proc);

// egl_bridge.c（libpojavexec.so），SDL 路径下经 EGL 交换代理计帧
void calculateFPS(void);

// input_bridge_v3.c（libpojavexec.so），SDL 主窗口创建/销毁时同步指针，供文本输入通道判定渲染路径
void sdlBridgeSetPrimaryWindow(struct SDL_Window *window);

DECL_DLSYM(SDL_InitSubSystem)
DECL_DLSYM(SDL_SetHint);
DECL_DLSYM(SDL_SetTextInputArea);
DECL_DLSYM(SDL_SetError);
DECL_DLSYM(SDL_GetError);
DECL_DLSYM(SDL_GetWindowFromEvent)
DECL_DLSYM(SDL_GetWindowFromID)
DECL_DLSYM(SDL_GL_SetAttribute)
DECL_DLSYM(SDL_LoadObject)
DECL_DLSYM(SDL_UnloadObject)
DECL_DLSYM(SDL_LoadFunction)
DECL_DLSYM(SDL_CreateWindow)
DECL_DLSYM(SDL_CreateWindowWithProperties)
DECL_DLSYM(SDL_DestroyWindow)
DECL_DLSYM(SDL_EGL_GetProcAddress)

typedef void *EGLDisplay;
typedef void *EGLConfig;
typedef int EGLint;
typedef int EGLBoolean;
typedef EGLBoolean (*eglChooseConfig_t)(EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs,
                                        EGLint config_size, EGLint *num_config);
typedef void *(*eglCreateContext_t)(EGLDisplay dpy, EGLConfig config, void *share, const EGLint *attrib_list);
typedef EGLBoolean (*eglSwapBuffers_t)(EGLDisplay dpy, void *surface);

// EGL 常量（EGL/egl.h），避免引入完整 EGL 头
#define EGL_NONE              0x3038
#define EGL_RENDERABLE_TYPE   0x3040
#define EGL_OPENGL_BIT        0x0008
#define EGL_OPENGL_ES2_BIT    0x0004
#define EGL_OPENGL_ES3_BIT    0x0040
#define EGL_CONTEXT_CLIENT_VERSION    0x3098
#define EGL_CONTEXT_MAJOR_VERSION_KHR 0x30FB
#define EGL_CONTEXT_MINOR_VERSION_KHR 0x30FC



// --- SDL 事件窗口解析修正 ---

// SDL 的 Android 后端中，鼠标焦点（mouse->focus）会被 SDL_UpdateMouseFocus 的坐标越界
// 判定意外清除（虚拟鼠标坐标经分辨率缩放后可超过 SDL window 尺寸）
// Android 同一时刻只会有一个窗口，事件解析失败时回落为之前成功解析出的唯一窗口

static SDL_Window *sdlLastEventWindow = NULL;

static SDL_Window *custom_SDL_GetWindowFromEvent_Func(const void *event) {
    SDL_Window *window = BYTEHOOK_CALL_PREV(custom_SDL_GetWindowFromEvent_Func, SDL_GetWindowFromEvent_t, event);
    if (window != NULL) {
        sdlLastEventWindow = window;
    } else if (sdlLastEventWindow != NULL) {
        window = sdlLastEventWindow;
    }
    BYTEHOOK_POP_STACK();
    return window;
}

static SDL_Window *custom_SDL_GetWindowFromID_Func(uint32_t id) {
    SDL_Window *window = BYTEHOOK_CALL_PREV(custom_SDL_GetWindowFromID_Func, SDL_GetWindowFromID_t, id);
    if (window != NULL) {
        sdlLastEventWindow = window;
    } else if (sdlLastEventWindow != NULL) {
        window = sdlLastEventWindow;
    }
    BYTEHOOK_POP_STACK();
    return window;
}

// --- SDL 事件窗口解析修正 ---

// --- 移动渲染器（ES 实现）下 SDL 创建 GL 上下文的宿主 EGL 兼容 ---

// 部分宿主 libEGL 不接受 RENDERABLE_TYPE 携带 ES3_BIT/OPENGL_BIT，
// 归一化为 ES2_BIT；仅用于首选请求失败后的兼容重试
static EGLBoolean normalizeEglChooseConfigList(const EGLint *attrib_list, EGLint *fixed, int cap) {
    if (attrib_list == NULL) return 0;
    int n = 0;
    for (int i = 0; n < cap - 2; i += 2) {
        EGLint attr = attrib_list[i];
        EGLint val = attrib_list[i + 1];
        if (attr == EGL_NONE) {
            fixed[n] = EGL_NONE;
            fixed[n + 1] = 0;
            n += 2;
            break;
        }
        if (attr == EGL_RENDERABLE_TYPE) {
            // 归一化为 ES2_BIT
            if ((val & (EGL_OPENGL_ES3_BIT | EGL_OPENGL_BIT)) != 0 && (val & EGL_OPENGL_ES2_BIT) == 0) {
                val = (val & ~(EGL_OPENGL_ES3_BIT | EGL_OPENGL_BIT)) | EGL_OPENGL_ES2_BIT;
            }
        }
        fixed[n] = attr;
        fixed[n + 1] = val;
        n += 2;
    }
    return n > 0;
}

// 剔除宿主不识别的 KHR 版本属性，生成兼容重试表；返回请求的主版本号（无则 0）
static int normalizeEglContextAttribs(const EGLint *attrib_list, EGLint *fixed, int cap, bool esSemantics) {
    int version = 0;
    bool hasClientVersion = false;
    if (attrib_list == NULL) return 0;
    int n = 0;
    for (int i = 0; n < cap - 2; i += 2) {
        EGLint attr = attrib_list[i];
        EGLint val = attrib_list[i + 1];
        if (attr == EGL_NONE) break;
        if (attr == EGL_CONTEXT_MAJOR_VERSION_KHR) { // 记录主版本后剔除
            if (version == 0) version = val;
            continue;
        }
        if (attr == EGL_CONTEXT_MINOR_VERSION_KHR) continue;
        if (attr == EGL_CONTEXT_CLIENT_VERSION) {
            hasClientVersion = true;
            if (version == 0) version = val;
        }
        if (n >= cap - 2) return 0;
        fixed[n++] = attr;
        fixed[n++] = val;
    }
    // 仅 ES 语义下补写 CLIENT_VERSION（避免退化成驱动默认版本）；
    // desktop 语义不补写——桌面 context 不使用 CLIENT_VERSION
    if (esSemantics && version > 0 && !hasClientVersion) {
        if (n >= cap - 2) return 0;
        fixed[n++] = EGL_CONTEXT_CLIENT_VERSION;
        fixed[n++] = version;
    }
    if (n >= cap - 2) return 0;
    fixed[n++] = EGL_NONE;
    fixed[n++] = 0;
    return version;
}

static bool isMobileGluesEgl(void) {
    const char *egl = getenv("POJAVEXEC_EGL");
    if (egl == NULL) return false;
    const char *base = strrchr(egl, '/');
    return strcmp(base != NULL ? base + 1 : egl, "libmobileglues.so") == 0;
}

// GLES 兼容层（强制 ES profile、RENDERABLE_TYPE 归一化、CV=2 兜底）
// 仅对移动 ES 渲染器生效，桌面/OSMesa 路径不得被 ES 化
static bool sdlGlesCompatEnabled(void) {
    const char *renderer = getenv("POJAV_RENDERER");
    if (renderer == NULL) return isMobileGluesEgl();
    if (strstr(renderer, "desktopgl") != NULL) return false;
    if (strncmp(renderer, "gallium_", 8) == 0) return false; // OSMesa 系
    if (strcmp(renderer, "custom_gallium") == 0 || strcmp(renderer, "vulkan_zink") == 0) return false;
    if (strncmp(renderer, "opengles", 8) == 0) return true; // 内置 GL4ES/NGGL4ES
    return isMobileGluesEgl(); // MobileGlues
}

static bool sForcedEsProfile = false;

static bool shouldReusePrimaryWindow(void) {
    const char *value = getenv("POJAV_SDL_REUSE_WINDOW");
    if (value != NULL) return strcmp(value, "1") == 0;
    return true;
}

// --- SDL 的 EGL 函数解析接管 ---
// 原始指针首次解析成功后固定，避免重复解析不同 handle 时跨 loader 调用
static eglChooseConfig_t sOrigEglChooseConfig = NULL;
static eglCreateContext_t sOrigEglCreateContext = NULL;
static eglSwapBuffers_t sOrigEglSwapBuffers = NULL;

static void *proxyEglCreateContext(EGLDisplay dpy, EGLConfig config, void *share, const EGLint *attrib_list) {
    if (sOrigEglCreateContext == NULL) {
        LOG_TO_E("SDL_Hook: eglCreateContext was not resolved");
        return NULL;
    }

    void *ctx = sOrigEglCreateContext(dpy, config, share, attrib_list);
    if (ctx != NULL || !sdlGlesCompatEnabled()) return ctx;

    bool esSemantics = sForcedEsProfile;
    EGLint fixed[64];
    int version = normalizeEglContextAttribs(attrib_list, fixed, 64, esSemantics);
    if (version == 0) return ctx;

    LOG_TO_W("SDL_Hook: retrying eglCreateContext without KHR version attrs (CV=%d)", version);
    ctx = sOrigEglCreateContext(dpy, config, share, fixed);
    if (ctx != NULL || !esSemantics || version <= 2) return ctx; // CV=2 为移动端最后兜底

    LOG_TO_W("SDL_Hook: retrying eglCreateContext with CV=2 after CV=%d failed", version);
    EGLint es2[3] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    return sOrigEglCreateContext(dpy, config, share, es2);
}

static EGLBoolean proxyEglChooseConfig(EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs,
                                       EGLint config_size, EGLint *num_config) {
    if (sOrigEglChooseConfig == NULL) {
        LOG_TO_E("SDL_Hook: eglChooseConfig was not resolved");
        return 0;
    }

    EGLBoolean result = sOrigEglChooseConfig(dpy, attrib_list, configs, config_size, num_config);
    if (result && num_config != NULL && *num_config > 0) return result;
    if (!sdlGlesCompatEnabled()) return result; // 兼容 fallback 仅限移动 ES 渲染器

    EGLint fixed[64];
    if (!normalizeEglChooseConfigList(attrib_list, fixed, 64)) return result;
    EGLint fallbackCount = 0;
    EGLBoolean fallbackResult = sOrigEglChooseConfig(dpy, fixed, configs, config_size, &fallbackCount);
    if (fallbackResult && num_config != NULL) *num_config = fallbackCount;
    LOG_TO_W("SDL_Hook: eglChooseConfig compatibility fallback result=%d count=%d", fallbackResult, fallbackCount);
    return fallbackResult;
}

static EGLBoolean proxyEglSwapBuffers(EGLDisplay dpy, void *surface) {
    if (sOrigEglSwapBuffers == NULL) {
        LOG_TO_E("SDL_Hook: eglSwapBuffers was not resolved");
        return 0;
    }
    calculateFPS();
    return sOrigEglSwapBuffers(dpy, surface);
}

// EGL 函数解析出口统一注入代理（bytehook 层与 dlsym 代理层共用）
static void *injectEglProxy(const char *name, void *resolved) {
    if (name == NULL) return resolved;
    if (strcmp(name, "eglChooseConfig") == 0) {
        if (resolved != NULL && sOrigEglChooseConfig == NULL) sOrigEglChooseConfig = (eglChooseConfig_t) resolved; // 首次解析后固定
        if (sOrigEglChooseConfig != NULL && resolved != (void *) proxyEglChooseConfig) resolved = (void *) proxyEglChooseConfig;
    } else if (strcmp(name, "eglCreateContext") == 0) {
        if (resolved != NULL && sOrigEglCreateContext == NULL) sOrigEglCreateContext = (eglCreateContext_t) resolved;
        if (sOrigEglCreateContext != NULL && resolved != (void *) proxyEglCreateContext) resolved = (void *) proxyEglCreateContext;
    } else if (strcmp(name, "eglSwapBuffers") == 0) {
        if (resolved != NULL && sOrigEglSwapBuffers == NULL) sOrigEglSwapBuffers = (eglSwapBuffers_t) resolved;
        if (sOrigEglSwapBuffers != NULL && resolved != (void *) proxyEglSwapBuffers) resolved = (void *) proxyEglSwapBuffers;
    }
    return resolved;
}

// SDL 经 SDL_LoadFunction 解析 EGL 函数后直接调用，注入代理使兼容重试生效
static void *custom_SDL_LoadFunction_Func(void *handle, const char *name) {
    void *r = BYTEHOOK_CALL_PREV(custom_SDL_LoadFunction_Func, SDL_LoadFunction_t, handle, name);
    BYTEHOOK_POP_STACK();
    return injectEglProxy(name, r);
}

// SDL 公共 EGL 解析入口，可绕过 SDL_LoadFunction；补齐同样的代理
static void *custom_SDL_EGLGetProcAddress_Func(const char *proc) {
    void *r = BYTEHOOK_CALL_PREV(custom_SDL_EGLGetProcAddress_Func, SDL_EGL_GetProcAddress_t, proc);
    BYTEHOOK_POP_STACK();
    if (proc == NULL || r == NULL) return r;
    if (strcmp(proc, "eglChooseConfig") == 0) {
        if (sOrigEglChooseConfig == NULL) sOrigEglChooseConfig = (eglChooseConfig_t) r;
        if (r != (void *) proxyEglChooseConfig) r = (void *) proxyEglChooseConfig;
    } else if (strcmp(proc, "eglCreateContext") == 0) {
        if (sOrigEglCreateContext == NULL) sOrigEglCreateContext = (eglCreateContext_t) r;
        if (r != (void *) proxyEglCreateContext) r = (void *) proxyEglCreateContext;
    } else if (strcmp(proc, "eglSwapBuffers") == 0) {
        if (sOrigEglSwapBuffers == NULL) sOrigEglSwapBuffers = (eglSwapBuffers_t) r;
        if (r != (void *) proxyEglSwapBuffers) r = (void *) proxyEglSwapBuffers;
    }
    return r;
}

// --- Vulkan 加载器一致性 ---
// 启动器经 EGLBridge 将 LWJGL 的 Vulkan 句柄重定向到私有命名空间中的
// 加载器副本（Turnip 链路，句柄记录于 VULKAN_PTR 环境变量）。
// MC 26.3 起 RenderPearl 要求 SDL 与 LWJGL 使用同一加载器实例
// （校验 vkGetInstanceProcAddr 指针一致），而 SDL 仅能按路径加载，
// 无法触及该私有实例。此处在 SDL 加载 Vulkan loader 时改还 VULKAN_PTR
// 句柄；对应句柄的引用计数由启动器持有，忽略 SDL 侧的卸载。
static void *custom_SDL_LoadObject_Func(const char *path) {
    if (path != NULL && strstr(path, "libvulkan") != NULL) {
        const char *vkptr = getenv("VULKAN_PTR");
        if (vkptr != NULL && vkptr[0] != '\0') {
            void *handle = (void *) (uintptr_t) strtoull(vkptr, NULL, 16);
            if (handle != NULL) return handle;
        }
    }
    void *r = BYTEHOOK_CALL_PREV(custom_SDL_LoadObject_Func, SDL_LoadObject_t, path);
    BYTEHOOK_POP_STACK();
    return r;
}

static void custom_SDL_UnloadObject_Func(void *handle) {
    const char *vkptr = getenv("VULKAN_PTR");
    if (vkptr != NULL && vkptr[0] != '\0') {
        void *vulkan_handle = (void *) (uintptr_t) strtoull(vkptr, NULL, 16);
        if (handle == vulkan_handle) return;
    }
    BYTEHOOK_CALL_PREV(custom_SDL_UnloadObject_Func, SDL_UnloadObject_t, handle);
    BYTEHOOK_POP_STACK();
}

// 首个成功创建的 SDL 窗口，后续创建请求将重定向到它
static SDL_Window *sPrimaryWindow = NULL;
// 主窗口的逻辑引用计数：真实窗口创建时为 1，每次复用递增
static unsigned int sPrimaryWindowRefs = 0;

// 释放主窗口的一个逻辑引用；返回 true 表示需要执行真实销毁。
// 复用窗口仍持有引用时跳过真实销毁，防止复用窗口被提前销毁导致 use-after-destroy 崩溃
static bool releasePrimaryWindow(SDL_Window *window) {
    if (window == sPrimaryWindow) {
        if (sPrimaryWindowRefs > 0) {
            sPrimaryWindowRefs--;
            LOG_TO_I("SDL_Hook: releasing logical window %p, refs=%u", window, sPrimaryWindowRefs);
            if (sPrimaryWindowRefs > 0) {
                if (window == sdlLastEventWindow) sdlLastEventWindow = NULL;
                return false;
            }
            sPrimaryWindow = NULL;
            sdlBridgeSetPrimaryWindow(NULL);
        }
        if (window == sdlLastEventWindow) sdlLastEventWindow = NULL;
    } else if (window == sdlLastEventWindow) {
        sdlLastEventWindow = NULL;
    }
    return true;
}

static void custom_SDL_DestroyWindow_Func(SDL_Window *window) {
    if (releasePrimaryWindow(window)) {
        BYTEHOOK_CALL_PREV(custom_SDL_DestroyWindow_Func, SDL_DestroyWindow_t, window);
    }
    BYTEHOOK_POP_STACK();
}

// SDL_InitSubSystem 的 launcher 集成准备，bytehook 层与 dlsym 代理层共用
static bool sdlInitSubSystemPrepare(SDL_InitFlags flags) {
    // Call notifyLauncher on SDL_InitSubSystem, this sets up all the JNI stuff needed by SDL.
    TRY_ATTACH_ENV(dvm_env, pojav_environ->dalvikJavaVMPtr, "SDL_InitSubSystem failed!",
            SET_DLSYM_PTR(dlopen("libSDL3.so", RTLD_NOLOAD), SDL_SetError);
            if (SDL_SetError_p) SDL_SetError_p("Failed to load SDL launcher integration android-side. This is not an SDL bug, please contact the launcher developer.");
            return false;
            );

    // Just in case of bozo
    jint safeFlags;
    if (flags > INT32_MAX) {
        safeFlags = -1;
    } else safeFlags = (jint)flags;

    notifyLauncher(dvm_env, NOTIF_TYPE_SDL, (int[]){ACTION_INIT_LAUNCHER_INTEGRATION, safeFlags}, 2);

    // This is the normal for the launcher, the default in SDL is false.
    SET_DLSYM_PTR(dlopen("libSDL3.so", RTLD_NOLOAD), SDL_SetHint);
    if (SDL_SetHint_p) SDL_SetHint_p("SDL_RETURN_KEY_HIDES_IME", "true");
    // FIXME: MobileGlues has issues with passing in the proper EGL params to make this work
    if (isMobileGluesEgl()) {
        SDL_SetHint_p("SDL_OPENGL_FORCE_SRGB_FRAMEBUFFER", "0");
    }
    // MC 按桌面惯例设置 SDL_ENABLE_SCREEN_KEYBOARD=0 来禁用平台软键盘（改用自绘 IME UI），
    // 但移动端依赖 SDL 唤起输入法；MC 在 SDL_Init 之前设置此 hint，
    // 本 hook 于 SDL_Init 时执行，此处覆盖回启用。
    if (SDL_SetHint_p) SDL_SetHint_p("SDL_ENABLE_SCREEN_KEYBOARD", "1");
    return true;
}

static bool custom_SDL_InitSubSystem_Func(SDL_InitFlags flags) {
    if (!sdlInitSubSystemPrepare(flags)) return false;

    // Call original func after doing all the needed setup
    bool r = BYTEHOOK_CALL_PREV(custom_SDL_InitSubSystem_Func, SDL_InitSubSystem_t, flags);
    if (!r){
        SET_DLSYM_PTR(dlopen("libSDL3.so", RTLD_NOLOAD), SDL_GetError);
        LOG_TO_E("SDL_Hook: SDL_InitSubsystem Error: %s", SDL_GetError_p());
    }
    BYTEHOOK_POP_STACK();
    return r;
}

// 移动渲染器均为 OpenGL ES 实现，而游戏按桌面 GL 惯例初始化 SDL，
// 非 ES 的 profile 请求会被宿主拒绝。因此在每次窗口创建前
// 将 GL profile 强制为 ES。
static void forceEglProfileEs(void) {
    if (!sdlGlesCompatEnabled()) return;
    SET_DLSYM_PTR(dlopen("libSDL3.so", RTLD_NOLOAD), SDL_GL_SetAttribute);
    if (SDL_GL_SetAttribute_p) {
        SDL_GL_SetAttribute_p(20 /* SDL_GL_CONTEXT_PROFILE_MASK */, 4 /* SDL_GL_CONTEXT_PROFILE_ES */);
        sForcedEsProfile = true;
    }
}

// --- Android 单窗口约束下的主窗口复用 ---

// SDL Android 后端同一进程内只支持一个窗口，而 MC 26.3 ss9 起 RenderPearl 在设备
// 初始化时会先创建一个隐藏工具窗口（GL 上下文依附其上），随后的主窗口创建将被拒绝
// 销毁工具窗口又会使其上的 GL surface 失效。故将后续创建请求重定向到首个窗口。

// 尺寸与方向均无需额外处理：
// 前者由 Android Surface 决定（创建时即取 Surface 尺寸，与请求值无关）
// 后者由 SDL_ORIENTATIONS hint 统一控制。
// 每次复用即签发一个逻辑引用，真实窗口存活至全部引用释放（见 releasePrimaryWindow）
static SDL_Window *reusePrimaryWindow(void) {
    ++sPrimaryWindowRefs;
    LOG_TO_I("SDL_Hook: reusing primary window %p, refs=%u", sPrimaryWindow, sPrimaryWindowRefs);
    return sPrimaryWindow;
}

static SDL_Window *custom_SDL_CreateWindow_Func(const char *title, int w, int h, uint32_t flags) {
    forceEglProfileEs();
    const bool reuse = shouldReusePrimaryWindow();
    LOG_TO_I("SDL_Hook: primary window reuse=%s", reuse ? "enabled" : "disabled");
    if (reuse && sPrimaryWindow != NULL) {
        return reusePrimaryWindow();
    }
    SDL_Window *wnd = BYTEHOOK_CALL_PREV(custom_SDL_CreateWindow_Func, SDL_CreateWindow_t, title, w, h, flags);
    if (reuse && wnd != NULL) {
        sPrimaryWindow = wnd;
        sPrimaryWindowRefs = 1;
        sdlBridgeSetPrimaryWindow(wnd);
    }
    BYTEHOOK_POP_STACK();
    return wnd;
}

static SDL_Window *custom_SDL_CreateWindowWithProperties_Func(uint32_t props) {
    forceEglProfileEs();
    const bool reuse = shouldReusePrimaryWindow();
    LOG_TO_I("SDL_Hook: primary window reuse=%s", reuse ? "enabled" : "disabled");
    if (reuse && sPrimaryWindow != NULL) {
        return reusePrimaryWindow();
    }
    SDL_Window *wnd = BYTEHOOK_CALL_PREV(custom_SDL_CreateWindowWithProperties_Func, SDL_CreateWindowWithProperties_t, props);
    if (reuse && wnd != NULL) {
        sPrimaryWindow = wnd;
        sPrimaryWindowRefs = 1;
        sdlBridgeSetPrimaryWindow(wnd);
    }
    BYTEHOOK_POP_STACK();
    return wnd;
}

// ---------- dlsym 层代理 ----------
// LWJGL 的 org.lwjgl.sdl 绑定等消费方经 dlopen+dlsym 解析 SDL 函数指针后直接
// 调用，bytehook 的 hook_all（GOT 导入补丁）拦截不到这类调用，仅靠上方 hook 时
// 主窗口复用/销毁跟踪对这类调用方不生效。customDlsym（sdl_dlopen_hook.c）在
// dlsym 出口把下表符号换成这里的代理：代理直接调用缓存的真实 SDL 函数，与
// hook 层共用主窗口复用/引用计数/launcher 集成逻辑，不依赖 bytehook 调用上下文。
// 参考 FCL-Team/FoldCraftLauncher 的 sdl_hook.c（https://github.com/FCL-Team/FoldCraftLauncher/blob/e398181caaccf186247057dfa1ba0cf4e5cbc83e/FCL/src/main/jni/native_hooks/sdl_hook.c）

typedef bool (*sdlInitSubSystem_t)(SDL_InitFlags);
typedef SDL_Window *(*sdlCreateWindow_t)(const char *, int, int, uint32_t);
typedef SDL_Window *(*sdlCreateWindowWithProperties_t)(uint32_t);
typedef void (*sdlDestroyWindow_t)(SDL_Window *);
typedef SDL_Window *(*sdlGetWindowFromEvent_t)(const void *);
typedef SDL_Window *(*sdlGetWindowFromID_t)(uint32_t);
typedef void *(*sdlLoadObject_t)(const char *);
typedef void *(*sdlLoadFunction_t)(void *, const char *);
typedef void (*sdlUnloadObject_t)(void *);

static sdlInitSubSystem_t realSdlInitSubSystem = NULL;
static sdlCreateWindow_t realSdlCreateWindow = NULL;
static sdlCreateWindowWithProperties_t realSdlCreateWindowWithProperties = NULL;
static sdlDestroyWindow_t realSdlDestroyWindow = NULL;
static sdlGetWindowFromEvent_t realSdlGetWindowFromEvent = NULL;
static sdlGetWindowFromID_t realSdlGetWindowFromID = NULL;
static sdlLoadObject_t realSdlLoadObject = NULL;
static sdlLoadFunction_t realSdlLoadFunction = NULL;
static sdlUnloadObject_t realSdlUnloadObject = NULL;

static bool proxy_SDL_InitSubSystem(SDL_InitFlags flags) {
    if (!sdlInitSubSystemPrepare(flags)) return false;
    bool r = realSdlInitSubSystem(flags);
    if (!r) {
        SET_DLSYM_PTR(dlopen("libSDL3.so", RTLD_NOLOAD), SDL_GetError);
        LOG_TO_E("SDL_Hook: SDL_InitSubsystem Error: %s", SDL_GetError_p());
    }
    return r;
}

static SDL_Window *proxy_SDL_CreateWindow(const char *title, int w, int h, uint32_t flags) {
    forceEglProfileEs();
    if (shouldReusePrimaryWindow() && sPrimaryWindow != NULL) return reusePrimaryWindow();
    SDL_Window *window = realSdlCreateWindow(title, w, h, flags);
    if (window != NULL && shouldReusePrimaryWindow()) {
        sPrimaryWindow = window;
        sPrimaryWindowRefs = 1;
        sdlBridgeSetPrimaryWindow(window);
    }
    return window;
}

static SDL_Window *proxy_SDL_CreateWindowWithProperties(uint32_t props) {
    forceEglProfileEs();
    if (shouldReusePrimaryWindow() && sPrimaryWindow != NULL) return reusePrimaryWindow();
    SDL_Window *window = realSdlCreateWindowWithProperties(props);
    if (window != NULL && shouldReusePrimaryWindow()) {
        sPrimaryWindow = window;
        sPrimaryWindowRefs = 1;
        sdlBridgeSetPrimaryWindow(window);
    }
    return window;
}

static void proxy_SDL_DestroyWindow(SDL_Window *window) {
    if (releasePrimaryWindow(window)) realSdlDestroyWindow(window);
}

static SDL_Window *proxy_SDL_GetWindowFromEvent(const void *event) {
    SDL_Window *window = realSdlGetWindowFromEvent(event);
    if (window != NULL) {
        sdlLastEventWindow = window;
    } else if (sdlLastEventWindow != NULL) {
        window = sdlLastEventWindow;
    }
    return window;
}

static SDL_Window *proxy_SDL_GetWindowFromID(uint32_t id) {
    SDL_Window *window = realSdlGetWindowFromID(id);
    if (window != NULL) {
        sdlLastEventWindow = window;
    } else if (sdlLastEventWindow != NULL) {
        window = sdlLastEventWindow;
    }
    return window;
}

static void *proxy_SDL_LoadObject(const char *path) {
    if (path != NULL && strstr(path, "libvulkan") != NULL) {
        const char *vkptr = getenv("VULKAN_PTR");
        if (vkptr != NULL && vkptr[0] != '\0') {
            void *handle = (void *) (uintptr_t) strtoull(vkptr, NULL, 16);
            if (handle != NULL) return handle;
        }
    }
    return realSdlLoadObject(path);
}

static void *proxy_SDL_LoadFunction(void *handle, const char *name) {
    return injectEglProxy(name, realSdlLoadFunction(handle, name));
}

static void proxy_SDL_UnloadObject(void *handle) {
    const char *vkptr = getenv("VULKAN_PTR");
    if (vkptr != NULL && vkptr[0] != '\0') {
        void *vulkanHandle = (void *) (uintptr_t) strtoull(vkptr, NULL, 16);
        if (handle == vulkanHandle) return;
    }
    realSdlUnloadObject(handle);
}

void create_sdl_hooks(bytehook_stub_t (*bytehook_hook_all_p)(const char *callee_path_name, const char *sym_name, void *new_func,
                                                             bytehook_hooked_t hooked, void *hooked_arg)) {
    // Don't set callee_path_name to anything besides NULL or else it won't be able to find the symbol
    bytehook_stub_t stub_SDL_InitSubSystem = bytehook_hook_all_p(NULL, "SDL_InitSubSystem", &custom_SDL_InitSubSystem_Func, NULL, NULL);
    bytehook_stub_t stub_SDL_GetWindowFromEvent = bytehook_hook_all_p(NULL, "SDL_GetWindowFromEvent", &custom_SDL_GetWindowFromEvent_Func, NULL, NULL);
    bytehook_stub_t stub_SDL_GetWindowFromID = bytehook_hook_all_p(NULL, "SDL_GetWindowFromID", &custom_SDL_GetWindowFromID_Func, NULL, NULL);
    // 窗口创建前强制 ES profile（覆盖 SDL3 的两种窗口创建入口）
    bytehook_stub_t stub_SDL_CreateWindow = bytehook_hook_all_p(NULL, "SDL_CreateWindow", &custom_SDL_CreateWindow_Func, NULL, NULL);
    bytehook_stub_t stub_SDL_CreateWindowWithProperties = bytehook_hook_all_p(NULL, "SDL_CreateWindowWithProperties", &custom_SDL_CreateWindowWithProperties_Func, NULL, NULL);
    // 接管 SDL 的 EGL 函数解析，注入归一化代理
    bytehook_stub_t stub_SDL_LoadFunction = bytehook_hook_all_p(NULL, "SDL_LoadFunction", &custom_SDL_LoadFunction_Func, NULL, NULL);
    // SDL 公共 EGL 解析入口补齐同样的代理（backend callback 可绕过 LoadFunction）
    bytehook_stub_t stub_SDL_EGLGetProcAddress = bytehook_hook_all_p(NULL, "SDL_EGL_GetProcAddress", &custom_SDL_EGLGetProcAddress_Func, NULL, NULL);
    // Vulkan 加载器一致性：SDL 侧改用启动器重定向的加载器句柄
    bytehook_stub_t stub_SDL_LoadObject = bytehook_hook_all_p(NULL, "SDL_LoadObject", &custom_SDL_LoadObject_Func, NULL, NULL);
    bytehook_stub_t stub_SDL_UnloadObject = bytehook_hook_all_p(NULL, "SDL_UnloadObject", &custom_SDL_UnloadObject_Func, NULL, NULL);
    // 主窗口销毁跟踪，配合窗口复用（见 custom_SDL_DestroyWindow_Func）
    bytehook_stub_t stub_SDL_DestroyWindow = bytehook_hook_all_p(NULL, "SDL_DestroyWindow", &custom_SDL_DestroyWindow_Func, NULL, NULL);
    LOG_TO_I("SDL_Hook: Successfully initialized SDL hooks, stubs: InitSubSystem=%p GetWindowFromEvent=%p GetWindowFromID=%p LoadFunction=%p CreateWindow=%p CreateWindowWithProps=%p LoadObject=%p UnloadObject=%p DestroyWindow=%p", stub_SDL_InitSubSystem, stub_SDL_GetWindowFromEvent, stub_SDL_GetWindowFromID, stub_SDL_LoadFunction, stub_SDL_CreateWindow, stub_SDL_CreateWindowWithProperties, stub_SDL_LoadObject, stub_SDL_UnloadObject, stub_SDL_DestroyWindow);
}

/** customDlsym 出口：把 SDL 符号的解析结果换成 dlsym 层代理；返回 NULL 表示不拦截 */
void *sdlDlsymProxy(const char *symbol, void *real) {
    if (strcmp(symbol, "SDL_InitSubSystem") == 0) {
        if (realSdlInitSubSystem == NULL) realSdlInitSubSystem = (sdlInitSubSystem_t) real;
        return (void *) proxy_SDL_InitSubSystem;
    }
    if (strcmp(symbol, "SDL_CreateWindow") == 0) {
        if (realSdlCreateWindow == NULL) realSdlCreateWindow = (sdlCreateWindow_t) real;
        return (void *) proxy_SDL_CreateWindow;
    }
    if (strcmp(symbol, "SDL_CreateWindowWithProperties") == 0) {
        if (realSdlCreateWindowWithProperties == NULL) realSdlCreateWindowWithProperties = (sdlCreateWindowWithProperties_t) real;
        return (void *) proxy_SDL_CreateWindowWithProperties;
    }
    if (strcmp(symbol, "SDL_DestroyWindow") == 0) {
        if (realSdlDestroyWindow == NULL) realSdlDestroyWindow = (sdlDestroyWindow_t) real;
        return (void *) proxy_SDL_DestroyWindow;
    }
    if (strcmp(symbol, "SDL_GetWindowFromEvent") == 0) {
        if (realSdlGetWindowFromEvent == NULL) realSdlGetWindowFromEvent = (sdlGetWindowFromEvent_t) real;
        return (void *) proxy_SDL_GetWindowFromEvent;
    }
    if (strcmp(symbol, "SDL_GetWindowFromID") == 0) {
        if (realSdlGetWindowFromID == NULL) realSdlGetWindowFromID = (sdlGetWindowFromID_t) real;
        return (void *) proxy_SDL_GetWindowFromID;
    }
    if (strcmp(symbol, "SDL_LoadObject") == 0) {
        if (realSdlLoadObject == NULL) realSdlLoadObject = (sdlLoadObject_t) real;
        return (void *) proxy_SDL_LoadObject;
    }
    if (strcmp(symbol, "SDL_LoadFunction") == 0) {
        if (realSdlLoadFunction == NULL) realSdlLoadFunction = (sdlLoadFunction_t) real;
        return (void *) proxy_SDL_LoadFunction;
    }
    if (strcmp(symbol, "SDL_UnloadObject") == 0) {
        if (realSdlUnloadObject == NULL) realSdlUnloadObject = (sdlUnloadObject_t) real;
        return (void *) proxy_SDL_UnloadObject;
    }
    return NULL;
}
