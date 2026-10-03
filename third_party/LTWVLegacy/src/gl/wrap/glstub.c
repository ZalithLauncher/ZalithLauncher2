#include "stub.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../attributes.h"
#include "../init.h"

#define STUB(ret, def, args)\
ret APIENTRY_GL4ES gl4es_ ## def args {\
    if(!globals4es.silentstub)\
        printf("stub: %s;\n", #def);\
} \
AliasExport(ret,def,,args);

/*STUB(void,glFogCoordd,(GLdouble coord));
STUB(void,glFogCoordf,(GLfloat coord));
STUB(void,glFogCoorddv,(const GLdouble *coord));
STUB(void,glFogCoordfv,(const GLfloat *coord));*/
#ifdef BCMHOST
STUB(void,glDiscardFramebufferEXT,(GLenum target, GLsizei numAttachments, const GLenum *attachments));
#endif

/*
STUB(void glBlendEquationSeparatei(GLuint buf, GLenum modeRGB, GLenum modeAlpha))
STUB(void glBlendFuncSeparatei(GLuint buf, GLenum srcRGB, GLenum dstRGB, GLenum srcAlpha, GLenum dstAlpha))
*/
STUB(void,glClearAccum,(GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha));
//STUB(void,glColorMaterial,(GLenum face, GLenum mode));
STUB(void,glCopyPixels,(GLint x, GLint y, GLsizei width, GLsizei height, GLenum type));
STUB(void,glDrawBuffer,(GLenum mode));
STUB(void,glEdgeFlag,(GLboolean flag));
STUB(void,glIndexf,(GLfloat c));
STUB(void,glPolygonStipple,(const GLubyte *mask));
STUB(void,glReadBuffer,(GLenum mode));
//STUB(void glSecondaryColor3f(GLfloat r, GLfloat g, GLfloat b));
STUB(void,glColorTable,(GLenum target, GLenum internalformat, GLsizei width, GLenum format, GLenum type, const GLvoid *table));

STUB(void,glAccum,(GLenum op, GLfloat value));
STUB(void,glPrioritizeTextures,(GLsizei n, const GLuint *textures, const GLclampf *priorities));
//STUB(void,glPixelMapfv,(GLenum map, GLsizei mapsize, const GLfloat *values));
//STUB(void,glPixelMapuiv,(GLenum map,GLsizei mapsize, const GLuint *values));
//STUB(void,glPixelMapusv,(GLenum map,GLsizei mapsize, const GLushort *values));
STUB(void,glPassThrough,(GLfloat token));
STUB(void,glIndexMask,(GLuint mask));
//STUB(void,glGetPixelMapfv,(GLenum map, GLfloat *data));
//STUB(void,glGetPixelMapuiv,(GLenum map, GLuint *data));
//STUB(void,glGetPixelMapusv,(GLenum map, GLushort *data));
STUB(void,glClearIndex,(GLfloat c));
STUB(void,glGetPolygonStipple,(GLubyte *pattern));
STUB(void,glFeedbackBuffer,(GLsizei size, GLenum type, GLfloat *buffer));
STUB(void,glEdgeFlagv,(GLboolean *flag));
//STUB(void glIndexPointer(GLenum  type,  GLsizei  stride,  const GLvoid *  pointer));
#undef STUB
