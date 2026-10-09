/**
 * Created by: artDev
 * Copyright (c) 2025 artDev, SerpentSpirale, CADIndie.
 * For use under LGPL-3.0
 */
void glClearDepth(double depth);
void* glMapBuffer(GLenum target, GLenum access);
void glGetTexLevelParameteriv(GLenum target, GLint level, GLenum pname, GLint *params);
void glDebugMessageControl( 	GLenum source,
                               GLenum type,
                               GLenum severity,
                               GLsizei count,
                               const GLuint *ids,
                               GLboolean enabled);
void glMultiDrawElementsBaseVertex( 	GLenum mode,
                                       const GLsizei *count,
                                       GLenum type,
                                       const void * const *indices,
                                       GLsizei drawcount,
                                       const GLint *basevertex);
void glBindFragDataLocation(GLuint program,
                            GLuint colorNumber,
                            const char * name);
void glGetTexImage( 	GLenum target,
                       GLint level,
                       GLenum format,
                       GLenum type,
                       void * pixels);

void glGetQueryObjectiv( 	GLuint id,
                            GLenum pname,
                            GLint * params);

void glDepthRange(GLdouble nearVal,
                  GLdouble farVal);

GLESOVERRIDE(glClearDepth)
GLESOVERRIDE(glMapBuffer)
GLESOVERRIDE(glGetTexLevelParameteriv)
GLESOVERRIDE(glGetTexLevelParameterfv)
GLESOVERRIDE(glCreateShader)
GLESOVERRIDE(glDeleteShader)
GLESOVERRIDE(glCreateProgram)
GLESOVERRIDE(glDeleteProgram)
GLESOVERRIDE(glLinkProgram)
GLESOVERRIDE(glAttachShader)
GLESOVERRIDE(glGetShaderiv)
GLESOVERRIDE(glShaderSource)
GLESOVERRIDE(glTexImage2D)
GLESOVERRIDE(glDebugMessageControl)
GLESOVERRIDE(glGetString)
GLESOVERRIDE(glEnable)
GLESOVERRIDE(glMultiDrawArrays)
GLESOVERRIDE(glMultiDrawElements)
GLESOVERRIDE(glMultiDrawElementsBaseVertex)
GLESOVERRIDE(glDrawElementsBaseVertex)
GLESOVERRIDE(glBindBufferBase)
GLESOVERRIDE(glBindBufferRange)
GLESOVERRIDE(glBindBuffer)
GLESOVERRIDE(glUseProgram)
GLESOVERRIDE(glGetIntegerv)
GLESOVERRIDE(glBindFramebuffer)
GLESOVERRIDE(glGenFramebuffers)
GLESOVERRIDE(glDeleteFramebuffers)
GLESOVERRIDE(glFramebufferTexture2D)
GLESOVERRIDE(glFramebufferTextureLayer)
GLESOVERRIDE(glFramebufferRenderbuffer)
GLESOVERRIDE(glGetFramebufferAttachmentParameteriv)
GLESOVERRIDE(glDrawBuffers)
GLESOVERRIDE(glDrawBuffer)
GLESOVERRIDE(glClearBufferiv)
GLESOVERRIDE(glClearBufferuiv)
GLESOVERRIDE(glClearBufferfv)
GLESOVERRIDE(glCheckFramebufferStatus)
GLESOVERRIDE(glReadPixels)
GLESOVERRIDE(glTexSubImage2D)
GLESOVERRIDE(glCopyTexSubImage2D)
GLESOVERRIDE(glTexParameteri)
GLESOVERRIDE(glBindFragDataLocation)
GLESOVERRIDE(glGetTexImage)
GLESOVERRIDE(glGetQueryObjectiv)
GLESOVERRIDE(glGetQueryObjecti64v)
GLESOVERRIDE(glGetQueryObjectui64v)
GLESOVERRIDE(glDepthRange)
GLESOVERRIDE(glVertexAttrib1d)
GLESOVERRIDE(glVertexAttrib1dv)
GLESOVERRIDE(glVertexAttrib1s)
GLESOVERRIDE(glVertexAttrib1sv)
GLESOVERRIDE(glVertexAttrib2d)
GLESOVERRIDE(glVertexAttrib2dv)
GLESOVERRIDE(glVertexAttrib2s)
GLESOVERRIDE(glVertexAttrib2sv)
GLESOVERRIDE(glVertexAttrib3d)
GLESOVERRIDE(glVertexAttrib3dv)
GLESOVERRIDE(glVertexAttrib3s)
GLESOVERRIDE(glVertexAttrib3sv)
GLESOVERRIDE(glVertexAttrib4d)
GLESOVERRIDE(glVertexAttrib4dv)
GLESOVERRIDE(glVertexAttrib4s)
GLESOVERRIDE(glVertexAttrib4sv)
GLESOVERRIDE(glVertexAttrib4Nbv)
GLESOVERRIDE(glVertexAttrib4Niv)
GLESOVERRIDE(glVertexAttrib4Nsv)
GLESOVERRIDE(glVertexAttrib4Nub)
GLESOVERRIDE(glVertexAttrib4Nubv)
GLESOVERRIDE(glVertexAttrib4Nuiv)
GLESOVERRIDE(glVertexAttrib4Nusv)
GLESOVERRIDE(glVertexAttribI1i)
GLESOVERRIDE(glVertexAttribI1iv)
GLESOVERRIDE(glVertexAttribI1ui)
GLESOVERRIDE(glVertexAttribI1uiv)
GLESOVERRIDE(glVertexAttribI2i)
GLESOVERRIDE(glVertexAttribI2iv)
GLESOVERRIDE(glVertexAttribI2ui)
GLESOVERRIDE(glVertexAttribI2uiv)
GLESOVERRIDE(glVertexAttribI3i)
GLESOVERRIDE(glVertexAttribI3iv)
GLESOVERRIDE(glVertexAttribI3ui)
GLESOVERRIDE(glVertexAttribI3uiv)
GLESOVERRIDE(glVertexAttribI4bv)
GLESOVERRIDE(glVertexAttribI4ubv)
GLESOVERRIDE(glVertexAttribI4sv)
GLESOVERRIDE(glVertexAttribI4usv)
GLESOVERRIDE(glBufferStorage)
GLESOVERRIDE(glGetStringi)
GLESOVERRIDE(glTexParameterf)
GLESOVERRIDE(glTexParameteri)
GLESOVERRIDE(glTexParameterfv)
GLESOVERRIDE(glTexParameteriv)
GLESOVERRIDE(glTexParameterIiv)
GLESOVERRIDE(glTexParameterIuiv)
GLESOVERRIDE(glRenderbufferStorage)
GLESOVERRIDE(glGetError)
GLESOVERRIDE(glTexBuffer)
GLESOVERRIDE(glTexBufferRange)
GLESOVERRIDE(glMapBufferRange)
GLESOVERRIDE(glFlushMappedBufferRange)
GLESOVERRIDE(glMultiDrawElementsIndirect)
GLESOVERRIDE(glMultiDrawArraysIndirect)