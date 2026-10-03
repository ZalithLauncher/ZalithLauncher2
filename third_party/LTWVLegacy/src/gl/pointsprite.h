#ifndef _GL4ES_POINTSPRITE_H_
#define _GL4ES_POINTSPRITE_H_

#include "gles.h"

typedef struct {
    GLfloat size;
    GLfloat sizeMin;
    GLfloat sizeMax;
    GLfloat fadeThresholdSize;
    GLfloat distance[3];
    GLenum  coordOrigin;
} pointsprite_t;

void APIENTRY_GL4ES gl4es_glPointParameteri(GLenum pname, GLint param);
void APIENTRY_GL4ES gl4es_glPointParameteriv(GLenum pname, const GLint * params);
void APIENTRY_GL4ES gl4es_glPointParameterf(GLenum pname, GLfloat param);
void APIENTRY_GL4ES gl4es_glPointParameterfv(GLenum pname, const GLfloat * params);

void APIENTRY_GL4ES gl4es_glPointSize(GLfloat size);

#endif // _GL4ES_POINTSPRITE_H_
