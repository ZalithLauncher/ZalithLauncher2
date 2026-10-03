#ifndef _GL4ES_RENDER_H_
#define _GL4ES_RENDER_H_

#include "buffers.h"
#include "gles.h"

GLint APIENTRY_GL4ES gl4es_glRenderMode(GLenum mode);
void APIENTRY_GL4ES gl4es_glInitNames(void);
void APIENTRY_GL4ES gl4es_glPopName(void);
void APIENTRY_GL4ES gl4es_glPushName(GLuint name);
void APIENTRY_GL4ES gl4es_glLoadName(GLuint name);
void APIENTRY_GL4ES gl4es_glSelectBuffer(GLsizei size, GLuint *buffer);

void select_glDrawElements(const vertexattrib_t* vtx, GLenum mode, GLuint count, GLenum type, GLvoid * indices);
void select_glDrawArrays(const vertexattrib_t* vtx, GLenum mode, GLuint first, GLuint count);

#endif // _GL4ES_RENDER_H_
