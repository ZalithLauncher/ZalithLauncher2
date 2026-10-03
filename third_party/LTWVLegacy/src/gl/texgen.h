#ifndef _GL4ES_TEXGEN_H_
#define _GL4ES_TEXGEN_H_

#include "gles.h"

void APIENTRY_GL4ES gl4es_glTexGenfv(GLenum coord, GLenum pname, const GLfloat *params);
void APIENTRY_GL4ES gl4es_glTexGeni(GLenum coord, GLenum pname, GLint param);
void gen_tex_coords(GLfloat *verts, GLfloat *norm, GLfloat **coords, GLint count, GLint *needclean, int texture, GLushort* indices, GLuint ilen);
void gen_tex_clean(GLint cleancode, int texture);
void APIENTRY_GL4ES gl4es_glGetTexGenfv(GLenum coord,GLenum pname,GLfloat *params);

void APIENTRY_GL4ES gl4es_glLoadTransposeMatrixf(const GLfloat *m);
void APIENTRY_GL4ES gl4es_glLoadTransposeMatrixd(const GLdouble *m);
void APIENTRY_GL4ES gl4es_glMultTransposeMatrixd(const GLdouble *m);
void APIENTRY_GL4ES gl4es_glMultTransposeMatrixf(const GLfloat *m);

#endif // _GL4ES_TEXGEN_H_
