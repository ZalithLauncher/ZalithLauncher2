#ifndef _GL4ES_FRAMEBUFFERS_H_
#define _GL4ES_FRAMEBUFFERS_H_

#include "gles.h"

void APIENTRY_GL4ES gl4es_glGenerateMipmap(GLenum target);
void APIENTRY_GL4ES gl4es_glGenFramebuffers(GLsizei n, GLuint *ids);
void APIENTRY_GL4ES gl4es_glDeleteFramebuffers(GLsizei n, GLuint *framebuffers);
GLboolean APIENTRY_GL4ES gl4es_glIsFramebuffer(GLuint framebuffer);
GLenum APIENTRY_GL4ES gl4es_glCheckFramebufferStatus(GLenum target);
void APIENTRY_GL4ES gl4es_glBindFramebuffer(GLenum target, GLuint framebuffer);
void APIENTRY_GL4ES gl4es_glFramebufferTexture2D(GLenum target, GLenum attachment, GLenum textarget, GLuint texture,	GLint level);
void APIENTRY_GL4ES gl4es_glGenRenderbuffers(GLsizei n, GLuint *renderbuffers);
void APIENTRY_GL4ES gl4es_glFramebufferRenderbuffer(GLenum target, GLenum attachment, GLenum renderbuffertarget, GLuint renderbuffer);
void APIENTRY_GL4ES gl4es_glDeleteRenderbuffers(GLsizei n, GLuint *renderbuffers);
void APIENTRY_GL4ES gl4es_glRenderbufferStorage(GLenum target, GLenum internalformat, GLsizei width, GLsizei height);
void APIENTRY_GL4ES gl4es_glBindRenderbuffer(GLenum target, GLuint renderbuffer);
GLboolean APIENTRY_GL4ES gl4es_glIsRenderbuffer(GLuint renderbuffer);
void APIENTRY_GL4ES gl4es_glGenerateMipmap(GLenum target);
void APIENTRY_GL4ES gl4es_glGetFramebufferAttachmentParameteriv(GLenum target, GLenum attachment, GLenum pname, GLint *params);
void APIENTRY_GL4ES gl4es_glGetRenderbufferParameteriv(GLenum target, GLenum pname, GLint * params);

void APIENTRY_GL4ES gl4es_glFramebufferTexture1D(GLenum target, GLenum attachment, GLenum textarget, GLuint texture,	GLint level);   // naive Wrap
void APIENTRY_GL4ES gl4es_glFramebufferTexture3D(GLenum target, GLenum attachment, GLenum textarget, GLuint texture,	GLint level, GLint layer);   // naive Wrap
void APIENTRY_GL4ES gl4es_glRenderbufferStorageMultisample(GLenum target, GLsizei samples, GLenum internalformat, GLsizei width, GLsizei height);    //STUB
void APIENTRY_GL4ES gl4es_glFramebufferTextureLayer(	GLenum target, GLenum attachment, GLuint texture, GLint level, GLint layer);    // naive Wrap
void APIENTRY_GL4ES gl4es_glBlitFramebuffer(GLint srcX0, GLint srcY0, GLint srcX1, GLint srcY1, GLint dstX0, GLint dstY0, GLint dstX1, GLint dstY1, GLbitfield mask, GLenum filter);

void APIENTRY_GL4ES gl4es_glDrawBuffers(GLsizei n, const GLenum *bufs);
void APIENTRY_GL4ES gl4es_glNamedFramebufferDrawBuffers(GLuint framebuffer, GLsizei n, const GLenum *bufs);

void APIENTRY_GL4ES gl4es_glClearBufferiv(GLenum buffer, GLint drawbuffer, const GLint * value);
void APIENTRY_GL4ES gl4es_glClearBufferuiv(GLenum buffer, GLint drawbuffer, const GLuint * value);
void APIENTRY_GL4ES gl4es_glClearBufferfv(GLenum buffer, GLint drawbuffer, const GLfloat * value);
void APIENTRY_GL4ES gl4es_glClearBufferfi(GLenum buffer, GLint drawbuffer, GLfloat depth, GLint stencil);

void APIENTRY_GL4ES gl4es_glClearNamedFramebufferiv(GLuint framebuffer, GLenum buffer, GLint drawbuffer, const GLint *value);
void APIENTRY_GL4ES gl4es_glClearNamedFramebufferuiv(GLuint framebuffer, GLenum buffer, GLint drawbuffer, const GLuint *value);
void APIENTRY_GL4ES gl4es_glClearNamedFramebufferfv(GLuint framebuffer, GLenum buffer, GLint drawbuffer, const GLfloat *value);
void APIENTRY_GL4ES gl4es_glClearNamedFramebufferfi(GLuint framebuffer, GLenum buffer, GLint drawbuffer, GLfloat depth, GLint stencil);

void APIENTRY_GL4ES gl4es_glColorMaskIndexed(GLuint framebuffer, GLboolean red, GLboolean green, GLboolean blue, GLboolean alpha);

// In case of LIBGL_FB=2, let's create an FBO for everything, that is then blitted just before the SwapBuffer
void createMainFBO(int width, int height);
void blitMainFBO(int x, int y, int width, int height);
void deleteMainFBO(void* state);
void bindMainFBO();
void unbindMainFBO();

void readfboBegin();
void readfboEnd();

GLuint gl4es_getCurrentFBO();
void gl4es_setCurrentFBO();

void gl4es_saveCurrentFBO();
void gl4es_restoreCurrentFBO();

#endif // _GL4ES_FRAMEBUFFERS_H_
