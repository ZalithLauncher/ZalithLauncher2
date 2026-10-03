#ifndef _GL4ES_LINE_H
#define _GL4ES_LINE_H

#include "gles.h"
#include "list.h"

void APIENTRY_GL4ES gl4es_glLineStipple(GLuint factor, GLushort pattern);
GLfloat *gen_stipple_tex_coords(GLfloat *vert, GLushort *sindices, modeinit_t *modes, int stride, int length, GLfloat* noalloctex);
void bind_stipple_tex();

#endif // _GL4ES_LINE_H
