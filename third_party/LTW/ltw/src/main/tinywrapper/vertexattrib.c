/**
 * Created by: artDev
 * Copyright (c) 2025 artDev, SerpentSpirale, CADIndie.
 * For use under LGPL-3.0
 */

#include <proc.h>
#include <egl.h>

/* glVertexAttribXs family */
void glVertexAttrib1s( 	GLuint index,
                          GLshort v0) {
    es3_functions.glVertexAttrib1f(index, (GLfloat) v0);
}

void glVertexAttrib2s( 	GLuint index,
                          GLshort v0,
                          GLshort v1) {
    es3_functions.glVertexAttrib2f(index, (GLfloat) v0, (GLfloat) v1);
}
void glVertexAttrib3s( 	GLuint index,
                          GLshort v0,
                          GLshort v1,
                          GLshort v2) {
    es3_functions.glVertexAttrib3f(index, (GLfloat) v0, (GLfloat) v1, (GLfloat) v2);
}
void glVertexAttrib4s( 	GLuint index,
                          GLshort v0,
                          GLshort v1,
                          GLshort v2,
                          GLshort v3) {
    es3_functions.glVertexAttrib4f(index, (GLfloat) v0, (GLfloat) v1, (GLfloat) v2, (GLfloat) v3);
}

/* glVertexAttribXsv family */
void glVertexAttrib1sv( 	GLuint index,
                           const GLshort *v) {
    glVertexAttrib1s(index, v[0]);
}
void glVertexAttrib2sv( 	GLuint index,
                           const GLshort *v) {
    glVertexAttrib2s(index, v[0], v[1]);
}
void glVertexAttrib3sv( 	GLuint index,
                           const GLshort *v) {
    glVertexAttrib3s(index, v[0], v[1], v[2]);
}
void glVertexAttrib4sv( 	GLuint index,
                           const GLshort *v) {
    glVertexAttrib4s(index, v[0], v[1], v[2], v[3]);
}

/* glVertexAttribXd family */
void glVertexAttrib1d( 	GLuint index,
                          GLdouble v0) {
    es3_functions.glVertexAttrib1f(index, (GLfloat) v0);
}

void glVertexAttrib2d( 	GLuint index,
                          GLdouble v0,
                          GLdouble v1) {
    es3_functions.glVertexAttrib2f(index, (GLfloat) v0, (GLfloat) v1);
}
void glVertexAttrib3d( 	GLuint index,
                          GLdouble v0,
                          GLdouble v1,
                          GLdouble v2) {
    es3_functions.glVertexAttrib3f(index, (GLfloat) v0, (GLfloat) v1, (GLfloat) v2);
}
void glVertexAttrib4d( 	GLuint index,
                          GLdouble v0,
                          GLdouble v1,
                          GLdouble v2,
                          GLdouble v3) {
    es3_functions.glVertexAttrib4f(index, (GLfloat) v0, (GLfloat) v1, (GLfloat) v2, (GLfloat) v3);
}

/* glVertexAttribXdv family */
void glVertexAttrib1dv( 	GLuint index,
                           const GLdouble *v) {
    glVertexAttrib1d(index, v[0]);
}
void glVertexAttrib2dv( 	GLuint index,
                           const GLdouble *v) {
    glVertexAttrib2d(index, v[0], v[1]);
}
void glVertexAttrib3dv( 	GLuint index,
                           const GLdouble *v) {
    glVertexAttrib3d(index, v[0], v[1], v[2]);
}
void glVertexAttrib4dv( 	GLuint index,
                           const GLdouble *v) {
    glVertexAttrib4d(index, v[0], v[1], v[2], v[3]);
}

/* glVertexAttribIXi family */
void glVertexAttribI1i( 	GLuint index,
                           GLint v0) {
    es3_functions.glVertexAttribI4i(index, v0, 0, 0, 1);
}
void glVertexAttribI2i( 	GLuint index,
                           GLint v0,
                           GLint v1) {
    es3_functions.glVertexAttribI4i(index, v0, v1, 0, 1);
}
void glVertexAttribI3i( 	GLuint index,
                           GLint v0,
                           GLint v1,
                           GLint v2) {
    es3_functions.glVertexAttribI4i(index, v0, v1, v2, 1);
}
// glVertexAttribI4i is natively implemented in OpenGL ES

/* glVertexAttribIXiv family */
void glVertexAttribI1iv( 	GLuint index,
                            const GLint *v) {
    glVertexAttribI1i(index, v[0]);
}
void glVertexAttribI2iv( 	GLuint index,
                            const GLint *v) {
    glVertexAttribI2i(index, v[0], v[1]);
}
void glVertexAttribI3iv( 	GLuint index,
                            const GLint *v) {
    glVertexAttribI3i(index, v[0], v[1], v[2]);
}
// glVertexAttribI4iv is natively implemented in OpenGL ES

/* glVertexAttribI4XYv family */
void glVertexAttribI4bv( 	GLuint index,
                            const GLbyte *v) {
    es3_functions.glVertexAttribI4i(index, (GLint)v[0], (GLint)v[1], (GLint)v[2], (GLint)v[3]);
}
void glVertexAttribI4ubv( 	GLuint index,
                            const GLubyte *v) {
    es3_functions.glVertexAttribI4ui(index, (GLuint)v[0], (GLuint)v[1], (GLuint)v[2], (GLuint)v[3]);
}
void glVertexAttribI4sv( 	GLuint index,
                            const GLshort *v) {
    es3_functions.glVertexAttribI4i(index, (GLint)v[0], (GLint)v[1], (GLint)v[2], (GLint)v[3]);
}
void glVertexAttribI4usv( 	GLuint index,
                             const GLushort *v) {
    es3_functions.glVertexAttribI4ui(index, (GLuint)v[0], (GLuint)v[1], (GLuint)v[2], (GLuint)v[3]);
}

/* glVertexAttribIXui family */
void glVertexAttribI1ui( 	GLuint index,
                            GLuint v0) {
    es3_functions.glVertexAttribI4ui(index, v0, 0, 0, 1);
}
void glVertexAttribI2ui( 	GLuint index,
                            GLuint v0,
                            GLuint v1) {
    es3_functions.glVertexAttribI4ui(index, v0, v1, 0, 1);
}
void glVertexAttribI3ui( 	GLuint index,
                            GLuint v0,
                            GLuint v1,
                            GLuint v2) {
    es3_functions.glVertexAttribI4ui(index, v0, v1, v2, 1);
}
// glVertexAttibI4ui is natively implemented in OpenGL ES

/* glVertexAttribIXuiv family */
void glVertexAttribI1uiv( 	GLuint index,
                            const GLuint *v) {
    glVertexAttribI1ui(index, v[0]);
}
void glVertexAttribI2uiv( 	GLuint index,
                            const GLuint *v) {
    glVertexAttribI2ui(index, v[0], v[1]);
}
void glVertexAttribI3uiv( 	GLuint index,
                            const GLuint *v) {
    glVertexAttribI3ui(index, v[0], v[1], v[2]);
}
// glVertexAttribI4uiv is natively implemented in OpenGL ES

/* glVertexAttrib4N*** family */
void glVertexAttrib4Nub( 	GLuint index,
                            GLubyte v0,
                            GLubyte v1,
                            GLubyte v2,
                            GLubyte v3) {
    GLfloat fv0 = ((GLfloat)v0 / 255.0f);
    GLfloat fv1 = ((GLfloat)v1 / 255.0f);
    GLfloat fv2 = ((GLfloat)v2 / 255.0f);
    GLfloat fv3 = ((GLfloat)v3 / 255.0f);
    es3_functions.glVertexAttrib4f(index, fv0, fv1, fv2, fv3);
}

void glVertexAttrib4Nubv( 	GLuint index,
                             const GLubyte *v) {
    glVertexAttrib4Nub(index, v[0], v[1], v[2], v[3]);
}

void glVertexAttrib4Nusv( 	GLuint index,
                             const GLushort *v) {
    GLfloat fv0 = ((GLfloat)v[0] / 32767.0f);
    GLfloat fv1 = ((GLfloat)v[1] / 32767.0f);
    GLfloat fv2 = ((GLfloat)v[2] / 32767.0f);
    GLfloat fv3 = ((GLfloat)v[3] / 32767.0f);
    es3_functions.glVertexAttrib4f(index, fv0, fv1, fv2, fv3);
}

void glVertexAttrib4Nuiv( 	GLuint index,
                             const GLuint *v) {
    // GLfloat has a precision limit of 24 bits, so truncate the input integers to it.
    GLfloat fv0 = ((GLfloat)v[0] / 4294967295.0f);
    GLfloat fv1 = ((GLfloat)v[1] / 4294967295.0f);
    GLfloat fv2 = ((GLfloat)v[2] / 4294967295.0f);
    GLfloat fv3 = ((GLfloat)v[3] / 4294967295.0f);
    es3_functions.glVertexAttrib4f(index, fv0, fv1, fv2, fv3);
}

static float bnormalize(GLbyte x) {
    return x < 0 ? (GLfloat)x/128.0f : (GLfloat)x/127.0f;
}
static float snormalize(GLshort x) {
    return x < 0 ? (GLfloat) x / 32768.0f : (GLfloat) x / 32767.0f;
}
static float inormalize(GLint x) {
    return x < 0 ? (GLfloat)x/2147483648.0f : (GLfloat)x/2147483647.0f;
}

void glVertexAttrib4Nbv( 	GLuint index,
                            const GLbyte *v) {
    es3_functions.glVertexAttrib4f(
            index,
            bnormalize(v[0]),
            bnormalize(v[1]),
            bnormalize(v[2]),
            bnormalize(v[3])
    );
}

void glVertexAttrib4Nsv( 	GLuint index,
                            const GLshort *v) {
    es3_functions.glVertexAttrib4f(
            index,
            snormalize(v[0]),
            snormalize(v[1]),
            snormalize(v[2]),
            snormalize(v[3])
    );
}

void glVertexAttrib4Niv( 	GLuint index,
                            const GLint *v) {
    es3_functions.glVertexAttrib4f(
            index,
            inormalize(v[0]),
            inormalize(v[1]),
            inormalize(v[2]),
            inormalize(v[3])
    );
}
