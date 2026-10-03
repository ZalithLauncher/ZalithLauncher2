#ifndef _GL4ES_GL4ES_H_
#define _GL4ES_GL4ES_H_

#include "khash.h"

#ifdef __ARM_NEON__
#include <arm_neon.h>
#endif // __ARM_NEON__

#include "wrap/gles.h"
#include "gles.h"
#include "glstate.h"

packed_call_t* APIENTRY_GL4ES glCopyPackedCall(const packed_call_t *packed);

#define checkError(code)                          \
    {int error; while ((error = glGetError())) {} \
    code                                          \
    if ((error = glGetError()))                   \
        printf(#code " -> %i\n", error);}

#define printError(file, line)              \
    {int error; if ((error = glGetError())) \
        printf(file ":%i -> %i\n", line, error);}

#define FLUSH_BEGINEND if(glstate->list.pending) gl4es_flush()

#define ERROR_IN_BEGIN if(glstate->list.begin) {errorShim(GL_INVALID_OPERATION); return;}

const GLubyte* APIENTRY_GL4ES gl4es_glGetString(GLenum name);
void APIENTRY_GL4ES gl4es_glGetIntegerv(GLenum pname, GLint *params);
void APIENTRY_GL4ES gl4es_glGetFloatv(GLenum pname, GLfloat *params);
void APIENTRY_GL4ES gl4es_glEnable(GLenum cap);
void APIENTRY_GL4ES gl4es_glDisable(GLenum cap);
void APIENTRY_GL4ES gl4es_glEnableClientState(GLenum cap);
void APIENTRY_GL4ES gl4es_glDisableClientState(GLenum cap);
GLboolean APIENTRY_GL4ES gl4es_glIsEnabled(GLenum cap);
void APIENTRY_GL4ES gl4es_glDrawElements(GLenum mode, GLsizei count, GLenum type, const GLvoid *indices);
void APIENTRY_GL4ES gl4es_glDrawArrays(GLenum mode, GLint first, GLsizei count);
void APIENTRY_GL4ES gl4es_glInterleavedArrays(GLenum format, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GL4ES gl4es_glBegin(GLenum mode);
void APIENTRY_GL4ES gl4es_glEnd(void);
void APIENTRY_GL4ES gl4es_glNormal3f(GLfloat nx, GLfloat ny, GLfloat nz);
void APIENTRY_GL4ES gl4es_glVertex4f(GLfloat x, GLfloat y, GLfloat z, GLfloat w);
void APIENTRY_GL4ES gl4es_glColor4f(GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha);
void APIENTRY_GL4ES gl4es_glSecondaryColor3f(GLfloat r, GLfloat g, GLfloat b);
void APIENTRY_GL4ES gl4es_glMaterialfv(GLenum face, GLenum pname, const GLfloat *params);
void APIENTRY_GL4ES gl4es_glMaterialf(GLenum face, GLenum pname, const GLfloat param);
void APIENTRY_GL4ES gl4es_glTexCoord4f(GLfloat s, GLfloat t, GLfloat r, GLfloat q);
void APIENTRY_GL4ES gl4es_glMultiTexCoord4f(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q);
void APIENTRY_GL4ES gl4es_glArrayElement(GLint i);
void APIENTRY_GL4ES gl4es_glLockArrays(GLint first, GLsizei count);
void APIENTRY_GL4ES gl4es_glUnlockArrays(void);
GLuint APIENTRY_GL4ES gl4es_glGenLists(GLsizei range);
void APIENTRY_GL4ES gl4es_glNewList(GLuint list, GLenum mode);
void APIENTRY_GL4ES gl4es_glEndList(void);
void APIENTRY_GL4ES gl4es_glCallList(GLuint list);
void APIENTRY_GL4ES gl4es_glCallLists(GLsizei n, GLenum type, const GLvoid *lists);
void APIENTRY_GL4ES gl4es_glDeleteLists(GLuint list, GLsizei range);
void APIENTRY_GL4ES gl4es_glListBase(GLuint base);
GLboolean APIENTRY_GL4ES gl4es_glIsList(GLuint list);
void APIENTRY_GL4ES gl4es_glPolygonMode(GLenum face, GLenum mode);
GLenum APIENTRY_GL4ES gl4es_glGetError(void);

void APIENTRY_GL4ES gl4es_glSecondaryColorPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GL4ES gl4es_glIndexPointer(GLenum type, GLsizei stride, const GLvoid * pointer);
void APIENTRY_GL4ES gl4es_glEdgeFlagPointer(GLsizei stride, const GLvoid * pointer);
void APIENTRY_GL4ES gl4es_glGetPointerv(GLenum pname, GLvoid* *params);
void APIENTRY_GL4ES gl4es_glFlush(void);
void APIENTRY_GL4ES gl4es_glFinish(void);
void APIENTRY_GL4ES gl4es_glFogfv(GLenum pname, const GLfloat* params);

void APIENTRY_GL4ES gl4es_glStencilMaskSeparate(GLenum face, GLuint mask);

void APIENTRY_GL4ES gl4es_glMultiDrawArrays(GLenum mode, const GLint *first, const GLsizei *count, GLsizei primcount);
void APIENTRY_GL4ES gl4es_glMultiDrawElements( GLenum mode, GLsizei *count, GLenum type, const void * const *indices, GLsizei primcount);
void APIENTRY_GL4ES gl4es_glMultiDrawElementsBaseVertex( GLenum mode, GLsizei *count, GLenum type, const void * const *indices, GLsizei primcount, const GLint * basevertex);

void APIENTRY_GL4ES gl4es_glDrawElementsBaseVertex(GLenum mode, GLsizei count, GLenum type, const void *indices, GLint basevertex);
void APIENTRY_GL4ES gl4es_glDrawRangeElementsBaseVertex(GLenum mode, GLuint start, GLuint end, GLsizei count, GLenum type, const void *indices, GLint basevertex);

void APIENTRY_GL4ES gl4es_glDrawArraysInstanced(GLenum mode, GLint first, GLsizei count, GLsizei primcount);
void APIENTRY_GL4ES gl4es_glDrawElementsInstanced(GLenum mode, GLsizei count, GLenum type, const void *indices, GLsizei primcount);
void APIENTRY_GL4ES gl4es_glDrawElementsInstancedBaseVertex(GLenum mode, GLsizei count, GLenum type, const void *indices, GLsizei primcount, GLint basevertex);

const GLubyte* APIENTRY_GL4ES gl4es_glGetStringi(GLenum name, GLuint index);

void APIENTRY_GL4ES gl4es_glClampColor(GLenum target, GLenum clamp);

void gl4es_flush(void);

int adjust_vertices(GLenum mode, int nb);

extern glstate_t *glstate;

void fpe_Init(glstate_t *glstate);       // defined in fpe.c
void fpe_Dispose(glstate_t *glstate);    // defined in fpe.c

// glGetError() return last error, but that error is not reset until read
// So if 2 operations generate an error, 
//  the 2nd error is lost if glGetError has not been called after 1st op
static inline void errorGL() {	// next glGetError will be from GL 
    if(glstate->type_error && glstate->shim_error==GL_NO_ERROR)
	    glstate->type_error = 0;
    else if(glstate->type_error==2)
        glstate->type_error = 1;    // will need to read glGetError...
}
static inline void errorShim(GLenum error) {	// next glGetError will be "error" from gl4es
    if(glstate->type_error && glstate->shim_error==GL_NO_ERROR)
	    glstate->type_error = 1;
    if(glstate->shim_error == GL_NO_ERROR)
	    glstate->shim_error = error;
}
static inline void noerrorShim() {
    if(glstate->type_error && glstate->shim_error==GL_NO_ERROR)
	    glstate->type_error = 1;
}

static inline void noerrorShimNoPurge() {
    // doing nothing
}

void gl4es_scratch(int alloc);
void gl4es_scratch_vertex(int alloc);
void gl4es_scratch_indices(int alloc);
void gl4es_use_scratch_vertex(int use);
void gl4es_use_scratch_indices(int use);

void ToBuffer(int first, int count);
void UnBuffer();

GLboolean APIENTRY_GL4ES glIsList(GLuint list);
GLuint APIENTRY_GL4ES glGenLists(GLsizei range);
void APIENTRY_GL4ES glActiveTextureARB(GLenum texture);
void APIENTRY_GL4ES glArrayElement(GLint i);
void APIENTRY_GL4ES glBegin(GLenum mode);
void APIENTRY_GL4ES glCallList(GLuint list);
void APIENTRY_GL4ES glCallLists(GLsizei n, GLenum type, const GLvoid *lists);
void APIENTRY_GL4ES glClearDepth(GLdouble depth);
void APIENTRY_GL4ES glDeleteList(GLuint list);
void APIENTRY_GL4ES glDeleteLists(GLuint list, GLsizei range);
void APIENTRY_GL4ES glDrawArrays(GLenum mode, GLint first, GLsizei count);
void APIENTRY_GL4ES glEnd(void);
void APIENTRY_GL4ES glEndList(void);
void APIENTRY_GL4ES glFrustum(GLdouble left, GLdouble right, GLdouble bottom, GLdouble top, GLdouble Near, GLdouble Far);
void APIENTRY_GL4ES glGetDoublev(GLenum pname, GLdouble *params);
void APIENTRY_GL4ES glIndexf(GLfloat i);
void APIENTRY_GL4ES glInterleavedArrays(GLenum format, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GL4ES glListBase(GLuint base);
void APIENTRY_GL4ES glLockArraysEXT(GLint first, GLsizei count);
void APIENTRY_GL4ES glNewList(GLuint list, GLenum mode);
void APIENTRY_GL4ES glOrtho(GLdouble left, GLdouble right, GLdouble bottom, GLdouble top, GLdouble Near, GLdouble Far);
void APIENTRY_GL4ES glSecondaryColor3f(GLfloat r, GLfloat g, GLfloat b);
void APIENTRY_GL4ES glTexCoord2f(GLfloat s, GLfloat t);
void APIENTRY_GL4ES glUnlockArraysEXT(void);
void APIENTRY_GL4ES glVertex2f(GLfloat x, GLfloat y);
void APIENTRY_GL4ES glVertex2i(GLint x, GLint y);
void APIENTRY_GL4ES glVertex3f(GLfloat x, GLfloat y, GLfloat z);
GLenum APIENTRY_GL4ES glGetError(void);

// custom functions
void APIENTRY_GL4ES glPushCall(void *call);

#endif // _GL4ES_GL4ES_H_
