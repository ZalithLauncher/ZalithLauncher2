#ifndef _GL4ES_SAMPLERS_H_
#define _GL4ES_SAMPLERS_H_

#include "gles.h"

void gl4es_glGenSamplers(GLsizei n, GLuint *ids);
void gl4es_glBindSampler(GLuint unit, GLuint sampler);
void gl4es_glDeleteSamplers(GLsizei n, const GLuint* samplers);
GLboolean gl4es_glIsSampler(GLuint id);
void gl4es_glSamplerParameterf(GLuint sampler, GLenum pname, GLfloat param);
void gl4es_glSamplerParameteri(GLuint sampler, GLenum pname, GLint param);
void gl4es_glSamplerParameterfv(GLuint sampler, GLenum pname, GLfloat *params);
void gl4es_glSamplerParameteriv(GLuint sampler, GLenum pname, GLint *params);
void gl4es_glSamplerParameterIiv(GLuint sampler, GLenum pname, GLint *params);
void gl4es_glSamplerParameterIuiv(GLuint sampler, GLenum pname, GLuint *params);
void gl4es_glGetSamplerParameterfv(GLuint sampler, GLenum pname, GLfloat * params);
void gl4es_glGetSamplerParameteriv(GLuint sampler, GLenum pname, GLfloat * params);
void gl4es_glGetSamplerParameterIiv(GLuint sampler, GLenum pname, GLint * params);
void gl4es_glGetSamplerParameterIuiv(GLuint sampler, GLenum pname, GLuint * params);

#endif //_GL4ES_SAMPLERS_H_