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

static void checkError(const char* tag) {
    GLenum err = glGetError();
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
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    GLint status;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
    if (!status) {
        char log[512];
        glGetShaderInfoLog(shader, 512, nullptr, log);
        LOGE("Shader compile error (%s): %s", type == GL_VERTEX_SHADER ? "VS" : "FS", log);
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

static bool initFSRResources() {
    GLint prevProgram, prevVAO, prevArrayBuffer, prevTexture, prevFBO;
    glGetIntegerv(GL_CURRENT_PROGRAM, &prevProgram);
    glGetIntegerv(GL_VERTEX_ARRAY_BINDING, &prevVAO);
    glGetIntegerv(GL_ARRAY_BUFFER_BINDING, &prevArrayBuffer);
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &prevTexture);
    glGetIntegerv(GL_FRAMEBUFFER_BINDING, &prevFBO);

    GLuint vs = compileShader(GL_VERTEX_SHADER, FSR_VSSource);
    if (!vs) { LOGE("Failed to compile FSR vertex shader"); return false; }

    GLuint fs = compileShader(GL_FRAGMENT_SHADER, FSR_FSSource);
    if (!fs) { LOGE("Failed to compile FSR fragment shader"); glDeleteShader(vs); return false; }

    g_fsrProgram = glCreateProgram();
    glAttachShader(g_fsrProgram, vs);
    glAttachShader(g_fsrProgram, fs);
    glLinkProgram(g_fsrProgram);

    GLint status;
    glGetProgramiv(g_fsrProgram, GL_LINK_STATUS, &status);
    if (!status) {
        char log[512];
        glGetProgramInfoLog(g_fsrProgram, 512, nullptr, log);
        LOGE("FSR program link error: %s", log);
        glDeleteShader(vs); glDeleteShader(fs);
        glDeleteProgram(g_fsrProgram); g_fsrProgram = 0;
        return false;
    }
    glDeleteShader(vs); glDeleteShader(fs);

    const float quadVertices[] = {
        -1.0f,  1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f,
         1.0f, -1.0f, 1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f, 1.0f,
         1.0f, -1.0f, 1.0f, 0.0f,
         1.0f,  1.0f, 1.0f, 1.0f
    };

    glGenVertexArrays(1, &g_quadVAO);
    glGenBuffers(1, &g_quadVBO);
    glBindVertexArray(g_quadVAO);
    glBindBuffer(GL_ARRAY_BUFFER, g_quadVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), quadVertices, GL_STATIC_DRAW);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));
    glEnableVertexAttribArray(1);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    glGenTextures(1, &g_renderTexture);
    glBindTexture(GL_TEXTURE_2D, g_renderTexture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, g_renderWidth, g_renderHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glGenRenderbuffers(1, &g_depthStencilRBO);
    glBindRenderbuffer(GL_RENDERBUFFER, g_depthStencilRBO);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, g_renderWidth, g_renderHeight);

    glGenFramebuffers(1, &g_renderFBO);
    glBindFramebuffer(GL_FRAMEBUFFER, g_renderFBO);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, g_renderTexture, 0);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, g_depthStencilRBO);

    glGenTextures(1, &g_targetTexture);
    glBindTexture(GL_TEXTURE_2D, g_targetTexture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, g_targetWidth, g_targetHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glGenFramebuffers(1, &g_targetFBO);
    glBindFramebuffer(GL_FRAMEBUFFER, g_targetFBO);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, g_targetTexture, 0);

    checkError("initFSRResources");

    glUseProgram(prevProgram);
    glBindVertexArray(prevVAO);
    glBindBuffer(GL_ARRAY_BUFFER, prevArrayBuffer);
    glBindTexture(GL_TEXTURE_2D, prevTexture);
    glBindFramebuffer(GL_FRAMEBUFFER, prevFBO);

    LOGD("FSR initialized: render %dx%d target %dx%d", g_renderWidth, g_renderHeight, g_targetWidth, g_targetHeight);
    return true;
}

extern "C" void fsr_init(int qualityPreset) {
    g_qualityPreset = qualityPreset;
    g_initialized = false;
    g_active = false;

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

    glViewport(0, 0, g_renderWidth, g_renderHeight);
    glBindFramebuffer(GL_FRAMEBUFFER, g_renderFBO);
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
        glGetIntegerv(GL_CURRENT_PROGRAM, &prevProgram);
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, &prevVAO);
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, &prevArrayBuffer);
        glGetIntegerv(GL_ACTIVE_TEXTURE, &prevActiveTexture);
        glActiveTexture(GL_TEXTURE0);
        glGetIntegerv(GL_TEXTURE_BINDING_2D, &prevTexture);
        glGetIntegerv(GL_READ_FRAMEBUFFER_BINDING, &prevReadFBO);
        glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &prevDrawFBO);
        glGetIntegerv(GL_RENDERBUFFER_BINDING, &prevRenderbuffer);

        glBindFramebuffer(GL_FRAMEBUFFER, g_targetFBO);
        glViewport(0, 0, g_targetWidth, g_targetHeight);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(g_fsrProgram);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, g_renderTexture);

        float const0[4] = {
            (float)g_renderWidth / (float)g_targetWidth,
            (float)g_renderHeight / (float)g_targetHeight,
            1.0f / (float)g_targetWidth,
            1.0f / (float)g_targetHeight
        };
        float viewportSize[2] = { (float)g_renderWidth, (float)g_renderHeight };

        glUniform1i(glGetUniformLocation(g_fsrProgram, "uInputTex"), 0);
        glUniform4fv(glGetUniformLocation(g_fsrProgram, "uConst0"), 1, const0);
        glUniform2fv(glGetUniformLocation(g_fsrProgram, "uViewportSize"), 1, viewportSize);

        glBindVertexArray(g_quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, g_targetFBO);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBlitFramebuffer(0, 0, g_targetWidth, g_targetHeight, 0, 0, g_targetWidth, g_targetHeight,
                          GL_COLOR_BUFFER_BIT, GL_LINEAR);

        glBindFramebuffer(GL_FRAMEBUFFER, g_renderFBO);
        glViewport(0, 0, g_renderWidth, g_renderHeight);

        glUseProgram(prevProgram);
        glBindVertexArray(prevVAO);
        glBindBuffer(GL_ARRAY_BUFFER, prevArrayBuffer);
        glActiveTexture(prevActiveTexture);
        glBindTexture(GL_TEXTURE_2D, prevTexture);
        glBindRenderbuffer(GL_RENDERBUFFER, prevRenderbuffer);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, prevReadFBO);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, prevDrawFBO);

        checkError("fsr_apply");
    }
}

extern "C" void fsr_set_quality(int qualityPreset) {
    g_qualityPreset = qualityPreset;
}

extern "C" void fsr_destroy() {
    g_active = false;
    g_initialized = false;
    if (g_fsrProgram) { glDeleteProgram(g_fsrProgram); g_fsrProgram = 0; }
    if (g_quadVAO) { glDeleteVertexArrays(1, &g_quadVAO); g_quadVAO = 0; }
    if (g_quadVBO) { glDeleteBuffers(1, &g_quadVBO); g_quadVBO = 0; }
    if (g_renderFBO) { glDeleteFramebuffers(1, &g_renderFBO); g_renderFBO = 0; }
    if (g_renderTexture) { glDeleteTextures(1, &g_renderTexture); g_renderTexture = 0; }
    if (g_depthStencilRBO) { glDeleteRenderbuffers(1, &g_depthStencilRBO); g_depthStencilRBO = 0; }
    if (g_targetFBO) { glDeleteFramebuffers(1, &g_targetFBO); g_targetFBO = 0; }
    if (g_targetTexture) { glDeleteTextures(1, &g_targetTexture); g_targetTexture = 0; }
    LOGD("FSR destroyed");
}

extern "C" JNIEXPORT void JNICALL
Java_com_movtery_zalithlauncher_bridge_ZLBridge_fsrInit(JNIEnv*, jclass, jint qualityPreset) {
    fsr_init(qualityPreset);
}
