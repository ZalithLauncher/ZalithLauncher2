#ifndef _GL4ES_BLEND_H_
#define _GL4ES_BLEND_H_

#include "gles.h"

void APIENTRY_GL4ES gl4es_glBlendColor(GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha);
void APIENTRY_GL4ES gl4es_glBlendFuncSeparate(GLenum sfactorRGB, GLenum dfactorRGB, GLenum sfactorAlpha, GLenum dfactorAlpha);
void APIENTRY_GL4ES gl4es_glBlendEquationSeparate(GLenum modeRGB, GLenum modeA);
void APIENTRY_GL4ES gl4es_glBlendFunc(GLenum sfactor, GLenum dfactor);
void APIENTRY_GL4ES gl4es_glBlendEquation(GLenum mode);

#endif //_GL4ES_BLEND_H_
