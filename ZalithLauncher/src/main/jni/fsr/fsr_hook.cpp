#include "fsr_hook.h"
#include "fsr_shader.h"

#include <cmath>
#include <jni.h>

static bool g_initialized = false;
static bool g_active = false;
static int g_qualityPreset = 2;
static int g_initAttempts = 0;

static GLuint g_renderFBO = 0;
static GLuint g_renderTexture = 0;
static GLuint g_depthStencilRBO = 0;
static GLuint g_targetFBO = 0;
static GLuint g_targetTexture = 0;
static GLuint g_quadVAO = 0;
static GLuint g_quadVBO = 0;
static GLuint g_fsrProgram = 0;

static GLsizei g_renderWidth = 0;
static GLsizei g_renderHeight = 0;
static GLsizei g_targetWidth = 0;
static GLsizei g_targetHeight = 0;


// GL 入口在运行时解析：pojavexec 不直接链接 libGLESv2 的 GL 符号
#define FSR_GL_LIST(X) \
    X(glGetError) \
    X(glCreateShader) \
    X(glShaderSource) \
    X(glCompileShader) \
    X(glGetShaderiv) \
    X(glGetShaderInfoLog) \
    X(glDeleteShader) \
    X(glGetIntegerv) \
    X(glCreateProgram) \
    X(glAttachShader) \
    X(glLinkProgram) \
    X(glGetProgramiv) \
    X(glGetProgramInfoLog) \
    X(glDeleteProgram) \
    X(glGenVertexArrays) \
    X(glGenBuffers) \
    X(glBindVertexArray) \
    X(glBindBuffer) \
    X(glBufferData) \
    X(glVertexAttribPointer) \
    X(glEnableVertexAttribArray) \
    X(glGenTextures) \
    X(glBindTexture) \
    X(glTexImage2D) \
    X(glTexParameteri) \
    X(glGenRenderbuffers) \
    X(glBindRenderbuffer) \
    X(glRenderbufferStorage) \
    X(glGenFramebuffers) \
    X(glBindFramebuffer) \
    X(glFramebufferTexture2D) \
    X(glFramebufferRenderbuffer) \
    X(glUseProgram) \
    X(glViewport) \
    X(glActiveTexture) \
    X(glClearColor) \
    X(glClear) \
    X(glUniform1i) \
    X(glGetUniformLocation) \
    X(glUniform4fv) \
    X(glUniform2fv) \
    X(glDrawArrays) \
    X(glBlitFramebuffer) \
    X(glDeleteVertexArrays) \
    X(glDeleteBuffers) \
    X(glDeleteFramebuffers) \
    X(glDeleteTextures) \
    X(glDeleteRenderbuffers)

#define FSR_GL_DECL(n) static decltype(&::n) p_##n = nullptr;
FSR_GL_LIST(FSR_GL_DECL)
#undef FSR_GL_DECL

static bool loadGLFunctions() {
    static bool resolved = false;
    static bool ok = false;
    if (resolved) return ok;
    resolved = true;

    void* handle = dlopen("libGLESv2.so", RTLD_NOW | RTLD_GLOBAL);
    if (!handle) handle = dlopen("libGLESv2_adreno.so", RTLD_NOW | RTLD_GLOBAL);

    bool all = true;
#define FSR_GL_LOAD(n)                                                        \
    p_##n = (decltype(&::n)) (handle ? dlsym(handle, #n) : nullptr);          \
    if (!p_##n) p_##n = (decltype(&::n)) dlsym(RTLD_DEFAULT, #n);             \
    if (!p_##n) { LOGE("missing GL symbol %s", #n); all = false; }
    FSR_GL_LIST(FSR_GL_LOAD)
#undef FSR_GL_LOAD

    ok = all;
    return ok;
}

static void checkError(const char* tag) {
    GLenum err = p_glGetError();
    if (err != GL_NO_ERROR) {
        LOGE("%s: GL error 0x%x", tag, err);
    }
}

static void calcRenderResolution(int targetW, int targetH, int preset, int* outW, int* outH) {
    float scale;
    switch (preset) {
        case 1: scale = 1.3f; break;
        case 2: scale = 1.5f; break;
        case 3: scale = 1.7f; break;
        case 4: scale = 2.0f; break;
        default: scale = 1.5f; break;
    }
    *outW = (int)(targetW / scale);
    *outH = (int)(targetH / scale);
    *outW = (*outW + 1) & ~1;
    *outH = (*outH + 1) & ~1;
}

static GLuint compileShader(GLenum type, const char* source) {
    GLuint shader = p_glCreateShader(type);
    p_glShaderSource(shader, 1, &source, nullptr);
    p_glCompileShader(shader);
    GLint status;
    p_glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
    if (!status) {
        char log[512];
        p_glGetShaderInfoLog(shader, 512, nullptr, log);
        LOGE("Shader compile error (%s): %s", type == GL_VERTEX_SHADER ? "VS" : "FS", log);
        p_glDeleteShader(shader);
        return 0;
    }
    return shader;
}

static bool initFSRResources() {
    GLint prevProgram, prevVAO, prevArrayBuffer, prevTexture, prevFBO;
    p_glGetIntegerv(GL_CURRENT_PROGRAM, &prevProgram);
    p_glGetIntegerv(GL_VERTEX_ARRAY_BINDING, &prevVAO);
    p_glGetIntegerv(GL_ARRAY_BUFFER_BINDING, &prevArrayBuffer);
    p_glGetIntegerv(GL_TEXTURE_BINDING_2D, &prevTexture);
    p_glGetIntegerv(GL_FRAMEBUFFER_BINDING, &prevFBO);

    GLuint vs = compileShader(GL_VERTEX_SHADER, FSR_VSSource);
    if (!vs) { LOGE("Failed to compile FSR vertex shader"); return false; }

    GLuint fs = compileShader(GL_FRAGMENT_SHADER, FSR_FSSource);
    if (!fs) { LOGE("Failed to compile FSR fragment shader"); p_glDeleteShader(vs); return false; }

    g_fsrProgram = p_glCreateProgram();
    p_glAttachShader(g_fsrProgram, vs);
    p_glAttachShader(g_fsrProgram, fs);
    p_glLinkProgram(g_fsrProgram);

    GLint status;
    p_glGetProgramiv(g_fsrProgram, GL_LINK_STATUS, &status);
    if (!status) {
        char log[512];
        p_glGetProgramInfoLog(g_fsrProgram, 512, nullptr, log);
        LOGE("FSR program link error: %s", log);
        p_glDeleteShader(vs); p_glDeleteShader(fs);
        p_glDeleteProgram(g_fsrProgram); g_fsrProgram = 0;
        return false;
    }
    p_glDeleteShader(vs); p_glDeleteShader(fs);

    const float quadVertices[] = {
        -1.0f,  1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f,
         1.0f, -1.0f, 1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f, 1.0f,
         1.0f, -1.0f, 1.0f, 0.0f,
         1.0f,  1.0f, 1.0f, 1.0f
    };

    p_glGenVertexArrays(1, &g_quadVAO);
    p_glGenBuffers(1, &g_quadVBO);
    p_glBindVertexArray(g_quadVAO);
    p_glBindBuffer(GL_ARRAY_BUFFER, g_quadVBO);
    p_glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), quadVertices, GL_STATIC_DRAW);
    p_glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)0);
    p_glEnableVertexAttribArray(0);
    p_glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));
    p_glEnableVertexAttribArray(1);
    p_glBindBuffer(GL_ARRAY_BUFFER, 0);
    p_glBindVertexArray(0);

    p_glGenTextures(1, &g_renderTexture);
    p_glBindTexture(GL_TEXTURE_2D, g_renderTexture);
    p_glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, g_renderWidth, g_renderHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    p_glGenRenderbuffers(1, &g_depthStencilRBO);
    p_glBindRenderbuffer(GL_RENDERBUFFER, g_depthStencilRBO);
    p_glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, g_renderWidth, g_renderHeight);

    p_glGenFramebuffers(1, &g_renderFBO);
    p_glBindFramebuffer(GL_FRAMEBUFFER, g_renderFBO);
    p_glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, g_renderTexture, 0);
    p_glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, g_depthStencilRBO);

    p_glGenTextures(1, &g_targetTexture);
    p_glBindTexture(GL_TEXTURE_2D, g_targetTexture);
    p_glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, g_targetWidth, g_targetHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    p_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    p_glGenFramebuffers(1, &g_targetFBO);
    p_glBindFramebuffer(GL_FRAMEBUFFER, g_targetFBO);
    p_glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, g_targetTexture, 0);

    checkError("initFSRResources");

    p_glUseProgram(prevProgram);
    p_glBindVertexArray(prevVAO);
    p_glBindBuffer(GL_ARRAY_BUFFER, prevArrayBuffer);
    p_glBindTexture(GL_TEXTURE_2D, prevTexture);
    p_glBindFramebuffer(GL_FRAMEBUFFER, prevFBO);

    LOGD("FSR initialized: render %dx%d target %dx%d", g_renderWidth, g_renderHeight, g_targetWidth, g_targetHeight);
    return true;
}

extern "C" void fsr_init(int qualityPreset) {
    g_qualityPreset = qualityPreset;
    g_initialized = false;
    g_active = false;

    if (!loadGLFunctions()) return;

    EGLDisplay display = eglGetCurrentDisplay();
    EGLSurface surface = eglGetCurrentSurface(EGL_DRAW);
    if (display == EGL_NO_DISPLAY || surface == EGL_NO_SURFACE) {
        // 还没有 GL 上下文：留待第一次交换缓冲时再初始化
        LOGD("FSR init deferred (no current context)");
        return;
    }

    EGLint targetW = 0, targetH = 0;
    eglQuerySurface(display, surface, EGL_WIDTH, &targetW);
    eglQuerySurface(display, surface, EGL_HEIGHT, &targetH);
    if (targetW <= 0 || targetH <= 0) {
        LOGE("FSR: invalid surface dimensions %dx%d", targetW, targetH);
        return;
    }

    g_targetWidth = targetW;
    g_targetHeight = targetH;
    calcRenderResolution(g_targetWidth, g_targetHeight, g_qualityPreset, &g_renderWidth, &g_renderHeight);

    if (g_renderWidth <= 0 || g_renderHeight <= 0) {
        LOGE("FSR: invalid render dimensions %dx%d", g_renderWidth, g_renderHeight);
        return;
    }

    if (g_renderWidth >= g_targetWidth && g_renderHeight >= g_targetHeight) {
        LOGD("FSR: render res >= target, skipping");
        return;
    }

    if (!initFSRResources()) {
        LOGE("FSR resource init failed");
        return;
    }

    g_active = true;
    LOGD("FSR active: render %dx%d target %dx%d preset %d", g_renderWidth, g_renderHeight, g_targetWidth, g_targetHeight, g_qualityPreset);

    p_glViewport(0, 0, g_renderWidth, g_renderHeight);
    p_glBindFramebuffer(GL_FRAMEBUFFER, g_renderFBO);
}

extern "C" void fsr_apply() {
    if (!g_active) {
        if (g_initAttempts >= 3) {
            return;
        }
        ++g_initAttempts;
        fsr_init(g_qualityPreset);
        if (!g_active) {
            return;
        }
    }

do_fsr:
    {
        GLint prevProgram, prevVAO, prevArrayBuffer, prevActiveTexture, prevTexture;
        GLint prevReadFBO, prevDrawFBO, prevRenderbuffer;
        p_glGetIntegerv(GL_CURRENT_PROGRAM, &prevProgram);
        p_glGetIntegerv(GL_VERTEX_ARRAY_BINDING, &prevVAO);
        p_glGetIntegerv(GL_ARRAY_BUFFER_BINDING, &prevArrayBuffer);
        p_glGetIntegerv(GL_ACTIVE_TEXTURE, &prevActiveTexture);
        p_glActiveTexture(GL_TEXTURE0);
        p_glGetIntegerv(GL_TEXTURE_BINDING_2D, &prevTexture);
        p_glGetIntegerv(GL_READ_FRAMEBUFFER_BINDING, &prevReadFBO);
        p_glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &prevDrawFBO);
        p_glGetIntegerv(GL_RENDERBUFFER_BINDING, &prevRenderbuffer);

        p_glBindFramebuffer(GL_FRAMEBUFFER, g_targetFBO);
        p_glViewport(0, 0, g_targetWidth, g_targetHeight);
        p_glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        p_glClear(GL_COLOR_BUFFER_BIT);

        p_glUseProgram(g_fsrProgram);

        p_glActiveTexture(GL_TEXTURE0);
        p_glBindTexture(GL_TEXTURE_2D, g_renderTexture);

        float const0[4] = {
            (float)g_renderWidth / (float)g_targetWidth,
            (float)g_renderHeight / (float)g_targetHeight,
            1.0f / (float)g_targetWidth,
            1.0f / (float)g_targetHeight
        };
        float viewportSize[2] = { (float)g_renderWidth, (float)g_renderHeight };

        p_glUniform1i(p_glGetUniformLocation(g_fsrProgram, "uInputTex"), 0);
        p_glUniform4fv(p_glGetUniformLocation(g_fsrProgram, "uConst0"), 1, const0);
        p_glUniform2fv(p_glGetUniformLocation(g_fsrProgram, "uViewportSize"), 1, viewportSize);

        p_glBindVertexArray(g_quadVAO);
        p_glDrawArrays(GL_TRIANGLES, 0, 6);
        p_glBindVertexArray(0);

        p_glBindFramebuffer(GL_READ_FRAMEBUFFER, g_targetFBO);
        p_glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        p_glBlitFramebuffer(0, 0, g_targetWidth, g_targetHeight, 0, 0, g_targetWidth, g_targetHeight,
                          GL_COLOR_BUFFER_BIT, GL_LINEAR);

        p_glBindFramebuffer(GL_FRAMEBUFFER, g_renderFBO);
        p_glViewport(0, 0, g_renderWidth, g_renderHeight);

        p_glUseProgram(prevProgram);
        p_glBindVertexArray(prevVAO);
        p_glBindBuffer(GL_ARRAY_BUFFER, prevArrayBuffer);
        p_glActiveTexture(prevActiveTexture);
        p_glBindTexture(GL_TEXTURE_2D, prevTexture);
        p_glBindRenderbuffer(GL_RENDERBUFFER, prevRenderbuffer);
        p_glBindFramebuffer(GL_READ_FRAMEBUFFER, prevReadFBO);
        p_glBindFramebuffer(GL_DRAW_FRAMEBUFFER, prevDrawFBO);

        checkError("fsr_apply");
    }
}

extern "C" void fsr_set_quality(int qualityPreset) {
    g_qualityPreset = qualityPreset;
}

extern "C" void fsr_destroy() {
    g_active = false;
    g_initialized = false;
    if (g_fsrProgram) { p_glDeleteProgram(g_fsrProgram); g_fsrProgram = 0; }
    if (g_quadVAO) { p_glDeleteVertexArrays(1, &g_quadVAO); g_quadVAO = 0; }
    if (g_quadVBO) { p_glDeleteBuffers(1, &g_quadVBO); g_quadVBO = 0; }
    if (g_renderFBO) { p_glDeleteFramebuffers(1, &g_renderFBO); g_renderFBO = 0; }
    if (g_renderTexture) { p_glDeleteTextures(1, &g_renderTexture); g_renderTexture = 0; }
    if (g_depthStencilRBO) { p_glDeleteRenderbuffers(1, &g_depthStencilRBO); g_depthStencilRBO = 0; }
    if (g_targetFBO) { p_glDeleteFramebuffers(1, &g_targetFBO); g_targetFBO = 0; }
    if (g_targetTexture) { p_glDeleteTextures(1, &g_targetTexture); g_targetTexture = 0; }
    LOGD("FSR destroyed");
}

extern "C" JNIEXPORT void JNICALL
Java_com_movtery_zalithlauncher_bridge_ZLBridge_fsrInit(JNIEnv*, jclass, jint qualityPreset) {
    fsr_init(qualityPreset);
}
