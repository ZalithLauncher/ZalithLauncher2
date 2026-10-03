#ifndef _GL4ES_TEXENV_H_
#define _GL4ES_TEXENV_H_

#include "gles.h"

typedef struct {
    GLenum          mode;
    GLfloat         color[4];
    GLenum          combine_rgb;
    GLenum          combine_alpha;
    GLfloat         rgb_scale;
    GLfloat         alpha_scale;
    GLenum          src0_rgb;
    GLenum          src1_rgb;
    GLenum          src2_rgb;
    GLenum          src3_rgb;
    GLenum          src0_alpha;
    GLenum          src1_alpha;
    GLenum          src2_alpha;
    GLenum          src3_alpha;
    GLenum          op0_rgb;
    GLenum          op1_rgb;
    GLenum          op2_rgb;
    GLenum          op3_rgb;
    GLenum          op0_alpha;
    GLenum          op1_alpha;
    GLenum          op2_alpha;
    GLenum          op3_alpha;
} texenv_t;

typedef struct {
    GLenum          lod_bias;
} texfilter_t;

void APIENTRY_GL4ES gl4es_glTexEnvf(GLenum target, GLenum pname, GLfloat param);
void APIENTRY_GL4ES gl4es_glTexEnvi(GLenum target, GLenum pname, GLint param);
void APIENTRY_GL4ES gl4es_glTexEnvfv(GLenum target, GLenum pname, const GLfloat *param);
void APIENTRY_GL4ES gl4es_glTexEnviv(GLenum target, GLenum pname, const GLint *param);
void APIENTRY_GL4ES gl4es_glGetTexEnvfv(GLenum target, GLenum pname, GLfloat * params);
void APIENTRY_GL4ES gl4es_glGetTexEnviv(GLenum target, GLenum pname, GLint * params);

#endif // _GL4ES_TEXENV_H_
