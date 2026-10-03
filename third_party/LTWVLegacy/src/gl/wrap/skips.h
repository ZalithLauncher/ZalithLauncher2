#ifndef _GL4ES_SKIPS_H_
#define _GL4ES_SKIPS_H_

// don't auto-wrap these functions
// gl4es.c
#define skip_glColor4f
#define skip_glColor4ub
#define skip_glDisable
#define skip_glEnable
#define skip_glIsEnabled
#define skip_glNormal3f

#define skip_glBindBuffer
#define skip_glBufferData
#define skip_glBufferSubData
#define skip_glDeleteBuffers
#define skip_glGenBuffers
#define skip_glIsBuffer
#define skip_glGetBufferPointerv
#define skip_glMapBuffer
#define skip_glUnmapBuffer
#define skip_glGetBufferParameteriv
#define skip_glGetBufferSubData

#define skip_glBlendColor
#define skip_glBlendFunc
#define skip_glBlendEquation
#define skip_glBlendEquationSeparate
#define skip_glBlendEquationSeparatei
#define skip_glBlendFuncSeparate
#define skip_glBlendFuncSeparatei

#define skip_glShadeModel

#define skip_glAlphaFunc
#define skip_glLogicOp

#define skip_glColorMask
#define skip_glClear

// depth.c
#define skip_glDepthFunc
#define skip_glDepthMask
#define skip_glDepthRangef
#define skip_glClearDepthf

// face.c
#define skip_glCullFace
#define skip_glFrontFace

// fog.c
#define skip_glFogfv
#define skip_glFogf
#define skip_glFogCoordf
#define skip_glFogCoordfv
#define skip_glFogCoordPointer

// getter.c
#define skip_glGetError
#define skip_glGetPointerv
#define skip_glGetBooleanv
#define skip_glGetIntegerv
#define skip_glGetFloatv
#define skip_glGetString
#define skip_glGetLightfv
#define skip_glGetMaterialfv
#define skip_glGetClipPlanef

// hint.c
#define skip_glHint

// light.c
#define skip_glLightModelf
#define skip_glLightModelfv
#define skip_glLightfv
#define skip_glLightf
#define skip_glMaterialfv
#define skip_glMaterialf

// raster.c
#define skip_glViewport
#define skip_glScissor

// texture.c
#define skip_glIsTexture
#define skip_glBindTexture
#define skip_glCopyTexImage2D
#define skip_glCopyTexSubImage2D
#define skip_glGenTextures
#define skip_glDeleteTextures
#define skip_glPixelStorei
#define skip_glPixelStoref
#define skip_glTexImage2D
#define skip_glTexParameteri
#define skip_glTexParameterf
#define skip_glTexParameterfv
#define skip_glTexParameteriv
#define skip_glTexSubImage2D
#define skip_glActiveTexture
#define skip_glClientActiveTexture
#define skip_glMultiTexCoord4f
#define skip_glTexGeni
#define skip_glTexGenfv
#define skip_glTexEnvf
#define skip_glTexEnvi
#define skip_glTexEnvfv
#define skip_glTexEnviv
#define skip_glGetTexEnvfv
#define skip_glGetTexEnviv
#define skip_glReadPixels
#define skip_glCompressedTexImage2D
#define skip_glCompressedTexSubImage2D
#define skip_glGetTexParameterfv
#define skip_glGetTexParameteriv

// glDrawArrays
#define skip_glDrawArrays
#define skip_glDrawElements
#define skip_glVertexPointer
#define skip_glColorPointer
#define skip_glNormalPointer
#define skip_glTexCoordPointer
#define skip_glDisableClientState
#define skip_glEnableClientState

// Framebuffers
#define skip_glGenFramebuffers
#define skip_glDeleteFramebuffers
#define skip_glIsFramebuffer
#define skip_glCheckFramebufferStatus
#define skip_glBindFramebuffer
#define skip_glFramebufferTexture2D
#define skip_glGenRenderbuffers
#define skip_glFramebufferRenderbuffer
#define skip_glDeleteRenderbuffers
#define skip_glRenderbufferStorage
#define skip_glRenderbufferStorageMultisample
#define skip_glBindRenderbuffer
#define skip_glIsRenderbuffer
#define skip_glGenerateMipmap
#define skip_glGetFramebufferAttachmentParameteriv
#define skip_glGetRenderbufferParameteriv
#define skip_glDrawBuffers

#define skip_glFlush
#define skip_glFinish

// matrix.c
#define skip_glPushMatrix
#define skip_glPopMatrix
#define skip_glLoadMatrixf
#define skip_glMultMatrixf
#define skip_glMatrixMode
#define skip_glLoadIdentity
#define skip_glTranslatef
#define skip_glScalef
#define skip_glRotatef
#define skip_glOrthof
#define skip_glFrustumf

// planes.c
#define skip_glClipPlanef

// MultiDrawArrays
#define skip_glMultiDrawArrays
#define skip_glMultiDrawElements
// this is to avoid a warning. I don't Push those anyway
#define direct_glMultiDrawArrays
#define direct_glMultiDrawElements

// pointsprite.c
#define skip_glPointSize
#define skip_glPointParameterfv
#define skip_glPointParameterf

// buffer.c
#define skip_glGenBuffers
#define skip_glBindBuffer
#define skip_glBufferData
#define skip_glBufferSubData
#define skip_glDeleteBuffers
#define skip_glIsBuffer
#define skip_glGetBufferParameteriv
#define skip_glMapBuffer
#define skip_glUnmapBuffer
#define skip_glGetBufferPointerv
#define skip_glGetBufferSubData
#define skip_glGenVertexArrays
#define skip_glBindVertexArray
#define skip_glDeleteVertexArrays
#define skip_glIsVertexArray

// shader.c
#define skip_glCreateShader
#define skip_glDeleteShader
#define skip_glCompileShader
#define skip_glShaderSource
#define skip_glGetShaderSource
#define skip_glIsShader
#define skip_glGetShaderInfoLog
#define skip_glGetShaderiv
#define skip_glGetShaderPrecisionFormat
#define skip_glShaderBinary
#define skip_glReleaseShaderCompiler

// vertexattrib.c
#define skip_glVertexAttribPointer
#define skip_glVertexAttribIPointer
#define skip_glEnableVertexAttribArray
#define skip_glDisableVertexAttribArray
#define skip_glVertexAttrib1f
#define skip_glVertexAttrib2f
#define skip_glVertexAttrib3f
#define skip_glVertexAttrib4f
#define skip_glVertexAttrib1fv
#define skip_glVertexAttrib2fv
#define skip_glVertexAttrib3fv
#define skip_glVertexAttrib4fv
#define skip_glGetVertexAttribfv
#define skip_glGetVertexAttribiv
#define skip_glGetVertexAttribPointerv

// program.c
#define skip_glAttachShader
#define skip_glBindAttribLocation
#define skip_glCreateProgram
#define skip_glDeleteProgram
#define skip_glDetachShader
#define skip_glGetActiveAttrib
#define skip_glGetActiveUniform
#define skip_glGetAttachedShaders
#define skip_glGetAttribLocation
#define skip_glGetProgramInfoLog
#define skip_glGetProgramiv
#define skip_glGetUniformLocation
#define skip_glIsProgram
#define skip_glLinkProgram
#define skip_glUseProgram
#define skip_glValidateProgram
#define skip_glProgramBinary
#define skip_glGetProgramBinary

// stencil.c
#define skip_glStencilFunc
#define skip_glStencilFuncSeparate
#define skip_glStencilMask
#define skip_glStencilMaskSeparate
#define skip_glStencilOp
#define skip_glStencilOpSeparate
#define skip_glClearStencil

//uniform.c
#define skip_glGetUniformfv
#define skip_glGetUniformiv
#define skip_glUniform1f
#define skip_glUniform2f
#define skip_glUniform3f
#define skip_glUniform4f
#define skip_glUniform1i
#define skip_glUniform2i
#define skip_glUniform3i
#define skip_glUniform4i
#define skip_glUniform1fv
#define skip_glUniform2fv
#define skip_glUniform3fv
#define skip_glUniform4fv
#define skip_glUniform1iv
#define skip_glUniform2iv
#define skip_glUniform3iv
#define skip_glUniform4iv
#define skip_glUniformMatrix2fv
#define skip_glUniformMatrix3fv
#define skip_glUniformMatrix4fv

// other aliased function
#define skip_glSampleCoverage

// don't compile these into display lists
#define direct_glColorPointer
#define direct_glDeleteLists
#define direct_glDisableClientState
#define direct_glEdgeFlagPointer
#define direct_glEnableClientState
//#define direct_glClientActiveTexture  // will use it in Batch mode
#define direct_glFeedbackBuffer
#define direct_glGenLists
#define direct_glIndexPointer
#define direct_glInterleavedArrays
#define direct_glIsEnabled
#define direct_glIsList
#define direct_glNormalPointer
#define direct_glPopClientAttrib
#define direct_glPixelStorei
#define direct_glPixelStoref
#define direct_glPushClientAttrib
#define direct_glRenderMode
#define direct_glSelectBuffer
#define direct_glTexCoordPointer
#define direct_glVertexPointer
#define direct_glGenTextures
#define direct_glGetError

#endif // _GL4ES_SKIPS_H_
