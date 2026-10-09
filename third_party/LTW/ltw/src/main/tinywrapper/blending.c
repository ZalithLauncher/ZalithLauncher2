//
// Created by whbex on 22.11.2025.
//

#include <GL/gl.h>
#include <GL/glext.h>
#include "egl.h"
#include "proc.h"

#define GL_BLEND_FUNC(name, func, args, ...) \
void name args {                          \
    if(!current_context || !current_context->blending.available) \
        return;                                         \
    current_context->blending.func(__VA_ARGS__);                                         \
}

GL_BLEND_FUNC(glBlendEquationi, blendequationi, (GLuint buf, GLenum mode), buf, mode)
GL_BLEND_FUNC(glBlendEquationSeparatei, blendequationseparatei, (GLuint buf, GLenum modeRGB, GLenum modeAlpha), buf, modeRGB, modeAlpha)
GL_BLEND_FUNC(glBlendFunci, blendfunci, (GLuint buf, GLenum src, GLenum dst), buf, src, dst)
GL_BLEND_FUNC(glBlendFuncSeparatei, blendfuncseparatei, (GLuint buf, GLenum srcRGB, GLenum dstRGB, GLenum srcAlpha, GLenum dstAlpha),
              buf, srcRGB, dstRGB, srcAlpha, dstAlpha)
GL_BLEND_FUNC(glColorMaski, colormaski, (GLuint index, GLboolean r, GLboolean g, GLboolean b, GLboolean a), index, r, g, b, a)
GL_BLEND_FUNC(glBlendEquationiARB, blendequationi, (GLuint buf, GLenum mode), buf, mode)
GL_BLEND_FUNC(glBlendEquationSeparateiARB, blendequationseparatei, (GLuint buf, GLenum modeRGB, GLenum modeAlpha), buf, modeRGB, modeAlpha)
GL_BLEND_FUNC(glBlendFunciARB, blendfunci, (GLuint buf, GLenum src, GLenum dst), buf, src, dst)
GL_BLEND_FUNC(glBlendFuncSeparateiARB, blendfuncseparatei, (GLuint buf, GLenum srcRGB, GLenum dstRGB, GLenum srcAlpha, GLenum dstAlpha),
              buf, srcRGB, dstRGB, srcAlpha, dstAlpha)