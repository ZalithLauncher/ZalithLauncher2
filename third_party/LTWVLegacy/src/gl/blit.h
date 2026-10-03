#ifndef _GL4ES_BLIT_H_
#define _GL4ES_BLIT_H_

#include "gles.h"

#define BLIT_ALPHA      0
#define BLIT_OPAQUE     1
#define BLIT_COLOR      2

void gl4es_blitTexture(GLuint texture, 
    GLfloat sx, GLfloat sy,
    GLfloat width, GLfloat height, 
    GLfloat nwidth, GLfloat nheight, 
    GLfloat zoomx, GLfloat zoomy, 
    GLfloat vpwidth, GLfloat vpheight, 
    GLfloat x, GLfloat y, GLint mode);

#endif // _GL4ES_BLIT_H_
