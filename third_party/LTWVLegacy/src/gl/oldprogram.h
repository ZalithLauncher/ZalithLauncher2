#ifndef _GL4ES_OLD_PROGRAM_H_
#define _GL4ES_OLD_PROGRAM_H_

#include "gles.h"
#include "buffers.h"
#include "shader.h"
#include "uniform.h"
#include "program.h"

typedef struct kh_oldprograms_s kh_oldprograms_t;
typedef struct glstate_s glstate_t;
typedef struct shader_s shader_t;

typedef struct oldprogram_s {
    GLuint      id;
    GLenum      type;
    char*       string;
    shader_t*   shader;
    int         max_local_params;
    float*      prog_local_params;
    int         max_env_params;
    float*      prog_env_params;    // link to global array
    // tracking of the shader uniforms
    int         min_loc;
    int         max_loc;
    int*        locals;
    int         min_env;
    int         max_env;
    int*        envs;
} oldprogram_t;

void InitOldProgramMap(glstate_t* glstate);
void FreeOldProgramMap(glstate_t* glstate);

oldprogram_t* getOldProgram(GLuint id);

// ARB_vertex_program
// VertexAttrib are the same as ARB_vertex_shader

void APIENTRY_GL4ES gl4es_glProgramStringARB(GLenum target, GLenum format, GLsizei len, const GLvoid *string); 

void APIENTRY_GL4ES gl4es_glBindProgramARB(GLenum target, GLuint program);

void APIENTRY_GL4ES gl4es_glDeleteProgramsARB(GLsizei n, const GLuint *programs);

void APIENTRY_GL4ES gl4es_glGenProgramsARB(GLsizei n, GLuint *programs);

void APIENTRY_GL4ES gl4es_glProgramEnvParameter4dARB(GLenum target, GLuint index, GLdouble x, GLdouble y, GLdouble z, GLdouble w);
void APIENTRY_GL4ES gl4es_glProgramEnvParameter4dvARB(GLenum target, GLuint index, const GLdouble *params);
void APIENTRY_GL4ES gl4es_glProgramEnvParameter4fARB(GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w);
void APIENTRY_GL4ES gl4es_glProgramEnvParameter4fvARB(GLenum target, GLuint index, const GLfloat *params);

void APIENTRY_GL4ES gl4es_glProgramLocalParameter4dARB(GLenum target, GLuint index, GLdouble x, GLdouble y, GLdouble z, GLdouble w);
void APIENTRY_GL4ES gl4es_glProgramLocalParameter4dvARB(GLenum target, GLuint index, const GLdouble *params);
void APIENTRY_GL4ES gl4es_glProgramLocalParameter4fARB(GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w);
void APIENTRY_GL4ES gl4es_glProgramLocalParameter4fvARB(GLenum target, GLuint index, const GLfloat *params);

void APIENTRY_GL4ES gl4es_glGetProgramEnvParameterdvARB(GLenum target, GLuint index, GLdouble *params);
void APIENTRY_GL4ES gl4es_glGetProgramEnvParameterfvARB(GLenum target, GLuint index, GLfloat *params);

void APIENTRY_GL4ES gl4es_glGetProgramLocalParameterdvARB(GLenum target, GLuint index, GLdouble *params);
void APIENTRY_GL4ES gl4es_glGetProgramLocalParameterfvARB(GLenum target, GLuint index, GLfloat *params);

void APIENTRY_GL4ES gl4es_glGetProgramivARB(GLenum target, GLenum pname, GLint *params);

void APIENTRY_GL4ES gl4es_glGetProgramStringARB(GLenum target, GLenum pname, GLvoid *string);

void APIENTRY_GL4ES gl4es_glGetVertexAttribdvARB(GLuint index, GLenum pname, GLdouble *params);
void APIENTRY_GL4ES gl4es_glGetVertexAttribfvARB(GLuint index, GLenum pname, GLfloat *params);
void APIENTRY_GL4ES gl4es_glGetVertexAttribivARB(GLuint index, GLenum pname, GLint *params);

void APIENTRY_GL4ES gl4es_glGetVertexAttribPointervARB(GLuint index, GLenum pname, GLvoid **pointer);

GLboolean APIENTRY_GL4ES gl4es_glIsProgramARB(GLuint program);

void APIENTRY_GL4ES gl4es_glProgramEnvParameters4fvEXT(GLenum target, GLuint index, GLsizei count, const GLfloat *params);
void APIENTRY_GL4ES gl4es_glProgramLocalParameters4fvEXT(GLenum target, GLuint index, GLsizei count, const GLfloat *params);

#endif //_GL4ES_OLD_PROGRAM_H_