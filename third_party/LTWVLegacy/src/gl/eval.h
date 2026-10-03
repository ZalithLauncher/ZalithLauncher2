#ifndef _GL4ES_EVAL_H_
#define _GL4ES_EVAL_H_

#include "const.h"
#include "gles.h"

void APIENTRY_GL4ES gl4es_glMap1d(GLenum target, GLdouble u1, GLdouble u2, GLint stride, GLint order, const GLdouble *points);
void APIENTRY_GL4ES gl4es_glMap1f(GLenum target, GLfloat u1, GLfloat u2, GLint stride, GLint order, const GLfloat *points);
void APIENTRY_GL4ES gl4es_glMap2d(GLenum target, GLdouble u1, GLdouble u2, GLint ustride, GLint uorder, GLdouble v1, GLdouble v2, GLint vstride, GLint vorder, const GLdouble *points);
void APIENTRY_GL4ES gl4es_glMap2f(GLenum target, GLfloat u1, GLfloat u2, GLint ustride, GLint uorder, GLfloat v1, GLfloat v2, GLint vstride, GLint vorder, const GLfloat *points);

void APIENTRY_GL4ES gl4es_glEvalCoord1d(GLdouble u);
void APIENTRY_GL4ES gl4es_glEvalCoord1f(GLfloat u);
void APIENTRY_GL4ES gl4es_glEvalCoord2d(GLdouble u, GLdouble v);
void APIENTRY_GL4ES gl4es_glEvalCoord2f(GLfloat u, GLfloat v);

void APIENTRY_GL4ES gl4es_glEvalMesh1(GLenum mode, GLint i1, GLint i2);
void APIENTRY_GL4ES gl4es_glEvalMesh2(GLenum mode, GLint i1, GLint i2, GLint j1, GLint j2);
void APIENTRY_GL4ES gl4es_glEvalPoint1(GLint i);
void APIENTRY_GL4ES gl4es_glEvalPoint2(GLint i, GLint j);
void APIENTRY_GL4ES gl4es_glMapGrid1d(GLint un, GLdouble u1, GLdouble u2);
void APIENTRY_GL4ES gl4es_glMapGrid1f(GLint un, GLfloat u1, GLfloat u2);
void APIENTRY_GL4ES gl4es_glMapGrid2d(GLint un, GLdouble u1, GLdouble u2, GLint vn, GLdouble v1, GLdouble v2);
void APIENTRY_GL4ES gl4es_glMapGrid2f(GLint un, GLfloat u1, GLfloat u2, GLint vn, GLfloat v1, GLfloat v2);
void APIENTRY_GL4ES gl4es_glGetMapdv(GLenum target, GLenum query, GLdouble *v);
void APIENTRY_GL4ES gl4es_glGetMapfv(GLenum target, GLenum query, GLfloat *v);
void APIENTRY_GL4ES gl4es_glGetMapiv(GLenum target, GLenum query, GLint *v);

typedef struct {
    GLenum type;
} map_state_t;

typedef struct {
    GLdouble _1, _2, d;
    GLint order;
} mapcoordd_t;

typedef struct {
    GLfloat _1, _2, d;
    GLint order;
} mapcoordf_t;

typedef struct {
    GLenum type;
    GLint dims, width;
    mapcoordd_t u, v;
    const GLdouble *points;
} map_stated_t;

typedef struct {
    GLenum type;
    GLint dims, width;
    mapcoordf_t u, v;
    const GLfloat *points;
} map_statef_t;

typedef struct {
    GLfloat _1, _2;
    GLfloat d;
    GLint n;
} map_grid_t;

static const GLsizei get_map_width(GLenum target) {
    switch (target) {
        case GL_MAP1_COLOR_4:         return 4;
        case GL_MAP1_INDEX:           return 3;
        case GL_MAP1_NORMAL:          return 3;
        case GL_MAP1_TEXTURE_COORD_1: return 1;
        case GL_MAP1_TEXTURE_COORD_2: return 2;
        case GL_MAP1_TEXTURE_COORD_3: return 3;
        case GL_MAP1_TEXTURE_COORD_4: return 4;
        case GL_MAP1_VERTEX_3:        return 3;
        case GL_MAP1_VERTEX_4:        return 4;
        case GL_MAP2_COLOR_4:         return 4;
        case GL_MAP2_INDEX:           return 3;
        case GL_MAP2_NORMAL:          return 3;
        case GL_MAP2_TEXTURE_COORD_1: return 1;
        case GL_MAP2_TEXTURE_COORD_2: return 2;
        case GL_MAP2_TEXTURE_COORD_3: return 3;
        case GL_MAP2_TEXTURE_COORD_4: return 4;
        case GL_MAP2_VERTEX_3:        return 3;
        case GL_MAP2_VERTEX_4:        return 4;
    }
    return 0;
}

#endif // _GL4ES_EVAL_H_
