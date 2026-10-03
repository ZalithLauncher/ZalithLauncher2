#ifndef _GL4ES_QUERIES_H_
#define _GL4ES_QUERIES_H_

#include "khash.h"
#include "gles.h"

void APIENTRY_GL4ES gl4es_glBeginQuery(GLenum target, GLuint id);
void APIENTRY_GL4ES gl4es_glEndQuery(GLenum target);
void APIENTRY_GL4ES gl4es_glGenQueries(GLsizei n, GLuint * ids);
void APIENTRY_GL4ES gl4es_glDeleteQueries(GLsizei n, const GLuint* ids);
GLboolean APIENTRY_GL4ES gl4es_glIsQuery(GLuint id);
void APIENTRY_GL4ES gl4es_glGetQueryiv(GLenum target, GLenum pname, GLint* params);
void APIENTRY_GL4ES gl4es_glGetQueryObjectiv(GLuint id, GLenum pname, GLint* params);
void APIENTRY_GL4ES gl4es_glGetQueryObjectuiv(GLuint id, GLenum pname, GLuint* params);
void APIENTRY_GL4ES gl4es_glQueryCounter(GLuint id, GLenum target);
void APIENTRY_GL4ES gl4es_glGetQueryObjecti64v(GLuint id, GLenum pname, GLint64 * params);
void APIENTRY_GL4ES gl4es_glGetQueryObjectui64v(GLuint id, GLenum pname, GLuint64 * params);

unsigned long long get_clock();

#endif // _GL4ES_QUERIES_H_
