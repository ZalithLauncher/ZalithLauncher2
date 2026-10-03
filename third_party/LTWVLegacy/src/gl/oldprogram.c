
#include "oldprogram.h"

#include "debug.h"
#include "fpe.h"
#include "gl4es.h"
#include "glstate.h"
#include "loader.h"
#include "shaderconv.h"
#include "vertexattrib.h"
#include "arbconverter.h"
#include "debug.h"


//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

// Implement "Old program" handling: so ARB_vertex_program and ARB_fragment_program extensions
// the core of this is a conversion between ARB ASM-like syntax to GLSL, then using regular functions
// Note that a program in this context is in fact a shader in ARB_vertex_shader (and GLSL) extension

#define kh_oldprograms_t kh_oldprograms_s
KHASH_MAP_INIT_INT(oldprograms, oldprogram_t *);
#undef kh_oldprograms_t

void freeOldProgram(oldprogram_t* old)
{
    if(!old)
        return;
    if(old->string)
        free(old->string);
    if(old->shader)
        gl4es_glDeleteShader(old->shader->id);
    if(old->prog_local_params)
        free(old->prog_local_params);
    free(old);
}

void InitOldProgramMap(glstate_t* glstate)
{
    if(glstate->glsl) {
        glstate->glsl->oldprograms = kh_init(oldprograms);
        glstate->glsl->error_msg = NULL;
        glstate->glsl->error_ptr = -1;
    }
}
void FreeOldProgramMap(glstate_t* glstate)
{
    if(!glstate->glsl)
        return;
    if(glstate->glsl->error_msg)
        free(glstate->glsl->error_msg);
    oldprogram_t* old;
    kh_foreach_value(glstate->glsl->oldprograms, old, freeOldProgram(old));
    kh_destroy(oldprograms, glstate->glsl->oldprograms);
}

GLuint getUniqueProgramID(GLuint last) {
    // avoid recycling prog id (that may messup FPE Cache)
    static GLuint upper = 0;
    if(last>upper) last = upper;
    khint_t k;
    do {
        ++last;
        k = kh_get(oldprograms, glstate->glsl->oldprograms, last);
    } while(k!=kh_end(glstate->glsl->oldprograms));
    upper = last;
    return last;
}

oldprogram_t* getOldProgram(GLuint program)
{
    kh_oldprograms_t * oldprograms = glstate->glsl->oldprograms;
    if(program) {
        khint_t k = kh_get(oldprograms, oldprograms, program);
        if(k != kh_end(oldprograms))
            return kh_value(oldprograms, k);
    }
    return NULL;
}


// Vertex function are also defined with ARB_vertex_shader
/*
void APIENTRY_GL4ES gl4es_glVertexAttrib1sARB(GLuint index, GLshort x)
void APIENTRY_GL4ES gl4es_glVertexAttrib1fARB(GLuint index, GLfloat x);
void APIENTRY_GL4ES gl4es_glVertexAttrib1dARB(GLuint index, GLdouble x);
void APIENTRY_GL4ES gl4es_glVertexAttrib2sARB(GLuint index, GLshort x, GLshort y);
void APIENTRY_GL4ES gl4es_glVertexAttrib2fARB(GLuint index, GLfloat x, GLfloat y);
void APIENTRY_GL4ES gl4es_glVertexAttrib2dARB(GLuint index, GLdouble x, GLdouble y);
void APIENTRY_GL4ES gl4es_glVertexAttrib3sARB(GLuint index, GLshort x, GLshort y, GLshort z);
void APIENTRY_GL4ES gl4es_glVertexAttrib3fARB(GLuint index, GLfloat x, GLfloat y, GLfloat z);
void APIENTRY_GL4ES gl4es_glVertexAttrib3dARB(GLuint index, GLdouble x, GLdouble y, GLdouble z);
void APIENTRY_GL4ES gl4es_glVertexAttrib4sARB(GLuint index, GLshort x, GLshort y, GLshort z, GLshort w);
void APIENTRY_GL4ES gl4es_glVertexAttrib4fARB(GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w);
void APIENTRY_GL4ES gl4es_glVertexAttrib4dARB(GLuint index, GLdouble x, GLdouble y, GLdouble z, GLdouble w);
void APIENTRY_GL4ES gl4es_glVertexAttrib4NubARB(GLuint index, GLubyte x, GLubyte y, GLubyte z, GLubyte w);
void APIENTRY_GL4ES gl4es_glVertexAttrib1svARB(GLuint index, const GLshort *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib1fvARB(GLuint index, const GLfloat *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib1dvARB(GLuint index, const GLdouble *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib2svARB(GLuint index, const GLshort *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib2fvARB(GLuint index, const GLfloat *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib2dvARB(GLuint index, const GLdouble *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib3svARB(GLuint index, const GLshort *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib3fvARB(GLuint index, const GLfloat *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib3dvARB(GLuint index, const GLdouble *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4bvARB(GLuint index, const GLbyte *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4svARB(GLuint index, const GLshort *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4ivARB(GLuint index, const GLint *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4ubvARB(GLuint index, const GLubyte *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4usvARB(GLuint index, const GLushort *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4uivARB(GLuint index, const GLuint *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4fvARB(GLuint index, const GLfloat *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4dvARB(GLuint index, const GLdouble *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4NbvARB(GLuint index, const GLbyte *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4NsvARB(GLuint index, const GLshort *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4NivARB(GLuint index, const GLint *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4NubvARB(GLuint index, const GLubyte *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4NusvARB(GLuint index, const GLushort *v);
void APIENTRY_GL4ES gl4es_glVertexAttrib4NuivARB(GLuint index, const GLuint *v);
void APIENTRY_GL4ES gl4es_glVertexAttribPointerARB(GLuint index, GLint size, GLenum type, GLboolean normalized, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GL4ES gl4es_glEnableVertexAttribArrayARB(GLuint index);
void APIENTRY_GL4ES gl4es_glDisableVertexAttribArrayARB(GLuint index);
*/

void APIENTRY_GL4ES gl4es_glProgramStringARB(GLenum target, GLenum format, GLsizei len, const GLvoid *string) {
    DBG(printf("glProgramStringARB(%s, %s, %d, %p), source is\n%s\n=======\n", PrintEnum(target), PrintEnum(format), len, string, (const char*)string);)
    oldprogram_t* old = NULL;
    int vertex;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            vertex = 1;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            vertex = 0;
            break;
        default:
            errorShim(GL_INVALID_VALUE);
            return;
    }
    if(format!=GL_PROGRAM_FORMAT_ASCII_ARB) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    if(old->string)
        free(old->string);
    // grab the new program
    old->string = calloc(1, len + 1);
    memcpy(old->string, string, len);
    // check if a shader is actually attached
    if(!old->shader) {
        DBG(printf("Error, no shader attached but glProgramStringARB(...) called\n");)
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    // Convert to GLSL
    const GLchar * p[1] = {0};
    p[0] = gl4es_convertARB(old->string, vertex, &glstate->glsl->error_msg, &glstate->glsl->error_ptr);
    if((!p[0]) || (glstate->glsl->error_ptr!=-1)) {
        DBG(printf("Error with ARB->GLSL conversion\nsource is:\n%s\n======\n", old->shader->source);)
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    gl4es_glShaderSource(old->shader->id, 1, p , NULL);
    DBG(printf("converted source is:\n%s\n======\n", old->shader->source?old->shader->source:"**error**");)
    if (!old->shader->source) {
        DBG(printf("Error with ARB->GLSL conversion\n");)
        errorShim(GL_INVALID_OPERATION);
        if (glstate->glsl->error_msg) free(glstate->glsl->error_msg);
        glstate->glsl->error_msg = strdup("Error with ARB->GLSL conversion");
        glstate->glsl->error_ptr = 0;
        return;
    }
    if (!old->shader->converted) {
        DBG(printf("Error with GLSL->GLSL:ES conversion\n");)
        errorShim(GL_INVALID_OPERATION);
        if (glstate->glsl->error_msg) free(glstate->glsl->error_msg);
        glstate->glsl->error_msg = strdup("Error with GLSL->GLSL:ES conversion");
        glstate->glsl->error_ptr = 0;
        return;
    }
    gl4es_glCompileShader(old->shader->id);
    GLint res = 0;
    gl4es_glGetShaderiv(old->shader->id, GL_COMPILE_STATUS, &res);
    if(res!=GL_TRUE) {
        DBG(printf("Error with Compile shader\n");)
        errorShim(GL_INVALID_OPERATION);
        if (glstate->glsl->error_msg) free(glstate->glsl->error_msg);
        glstate->glsl->error_msg = strdup("Error with Compile shader");
        glstate->glsl->error_ptr = 0;
        return;
    }
}

void APIENTRY_GL4ES gl4es_glBindProgramARB(GLenum target, GLuint program) {
    DBG(printf("glBindProgramARB(%s, %d)\n", PrintEnum(target), program);)
    khint_t k;
    oldprogram_t* old = NULL; 
    kh_oldprograms_t * oldprograms = glstate->glsl->oldprograms;
    if(program) {
        k = kh_get(oldprograms, oldprograms, program);
        if(k == kh_end(oldprograms)) {
            // if program as not be generated it's fine, create a new one on-the-fly
            int ret;
            k = kh_put(oldprograms, oldprograms, program, &ret);
            old = kh_value(oldprograms, k) =(oldprogram_t*)calloc(1, sizeof(oldprogram_t));
            old->id = program;
        } else {
            old = kh_value(oldprograms, k);
            if(old->type!=0 && old->type!=target) {
                errorShim(GL_INVALID_OPERATION);
                return;
            }
        }
    }
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            if(program) {
                noerrorShimNoPurge();
                if(glstate->fpe_state)
                    glstate->fpe_state->vertex_prg_id = program;
                glstate->glsl->vtx_prog = old;
                if(!old->type) {
                    // create an empty shader
                    old->type = target;
                    GLuint shader = gl4es_glCreateShader(GL_VERTEX_SHADER);
                    shader_t *glshader = NULL;
                    khash_t(shaderlist) *shaders = glstate->glsl->shaders;
                    k = kh_get(shaderlist, shaders, shader);
                    glshader = kh_value(shaders, k);
                    old->shader = glshader;
                    // alloc memory for locals
                    old->max_local_params = MAX_VTX_PROG_LOC_PARAMS;
                    old->prog_local_params = (float*)calloc(MAX_VTX_PROG_LOC_PARAMS*4, sizeof(float));
                    old->max_env_params = MAX_VTX_PROG_ENV_PARAMS;
                    old->prog_env_params = glstate->glsl->vtx_env_params;
                    old->max_loc = -1;
                    old->max_env = -1;
                    old->min_loc = MAX_VTX_PROG_LOC_PARAMS;
                    old->min_env = MAX_VTX_PROG_ENV_PARAMS;
                }
            } else {
                noerrorShimNoPurge();
                glstate->glsl->vtx_prog = NULL;
                if(glstate->fpe_state)
                    glstate->fpe_state->vertex_prg_id = 0;
            }
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            if(program) {
                noerrorShimNoPurge();
                if(glstate->fpe_state)
                    glstate->fpe_state->fragment_prg_id = program;
                glstate->glsl->frg_prog = old;
                if(!old->type) {
                    // create an empty shader
                    old->type = target;
                    GLuint shader = gl4es_glCreateShader(GL_FRAGMENT_SHADER);
                    shader_t *glshader = NULL;
                    khash_t(shaderlist) *shaders = glstate->glsl->shaders;
                    k = kh_get(shaderlist, shaders, shader);
                    glshader = kh_value(shaders, k);
                    old->shader = glshader;
                    // alloc memory for locals
                    old->max_local_params = MAX_FRG_PROG_LOC_PARAMS;
                    old->prog_local_params = (float*)calloc(MAX_FRG_PROG_LOC_PARAMS*4, sizeof(float));
                    old->max_env_params = MAX_FRG_PROG_ENV_PARAMS;
                    old->prog_env_params = glstate->glsl->frg_env_params;
                    old->max_loc = -1;
                    old->max_env = -1;
                    old->min_loc = MAX_FRG_PROG_LOC_PARAMS;
                    old->min_env = MAX_FRG_PROG_ENV_PARAMS;
                }
            } else {
                noerrorShimNoPurge();
                glstate->glsl->frg_prog = NULL;
                if(glstate->fpe_state)
                    glstate->fpe_state->fragment_prg_id = 0;
            }
            break;
        default:
            errorShim(GL_INVALID_ENUM);
    }
}

void APIENTRY_GL4ES gl4es_glDeleteProgramsARB(GLsizei n, const GLuint *programs) {
    DBG(printf("glDeleteProgramsARB(%d, %p)\n", n, programs);)
    //TODO, unbind if binded?
    khint_t k;
    kh_oldprograms_t * oldprograms = glstate->glsl->oldprograms;
    for (int i=0; i<n; ++i) {
        GLuint id = programs[i];
        k = kh_get(oldprograms, oldprograms, id);
        if(k!=kh_end(oldprograms)) {
            freeOldProgram(kh_value(oldprograms, k));
            kh_del(oldprograms, oldprograms, k);
        }
    }
}

void APIENTRY_GL4ES gl4es_glGenProgramsARB(GLsizei n, GLuint *programs) {
    DBG(printf("glGenProgramsARB(%d, %p)\n", n, programs);)
    GLuint last = 0;
    khint_t k;
    kh_oldprograms_t * oldprograms = glstate->glsl->oldprograms;
    for (int i=0; i<n; ++i) {
        programs[i] = last = getUniqueProgramID(last);
        int ret;
        k = kh_put(oldprograms, oldprograms, last, &ret);
        oldprogram_t *old = kh_value(oldprograms, k) =(oldprogram_t*)calloc(1, sizeof(oldprogram_t));
        old->id = last;
    }
    noerrorShimNoPurge();
}

void APIENTRY_GL4ES gl4es_glProgramEnvParameter4dARB(GLenum target, GLuint index, GLdouble x, GLdouble y, GLdouble z, GLdouble w) {
    DBG(printf("glProgramEnvParameter4dARB(%s, %u, %f, %f, %f, %f)\n", PrintEnum(target), index, x, y, z, w);)
    float *f = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            if(index<MAX_VTX_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->vtx_env_params+index*4;
            }
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            if(index<MAX_FRG_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->frg_env_params+index*4;
            } else
                errorShim(GL_INVALID_VALUE);
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(f) {
        f[0] = x;
        f[1] = y;
        f[2] = z;
        f[3] = w;
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glProgramEnvParameter4dvARB(GLenum target, GLuint index, const GLdouble *params) {
    DBG(printf("glProgramEnvParameter4dvARB(%s, %u, %p[%f/%f/%f/%f])\n", PrintEnum(target), index, params, params[0], params[1], params[2], params[3]);)
    float *f = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            if(index<MAX_VTX_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->vtx_env_params+index*4;
            }
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            if(index<MAX_FRG_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->frg_env_params+index*4;
            }
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(f) {
        f[0] = params[0];
        f[1] = params[1];
        f[2] = params[2];
        f[3] = params[3];
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glProgramEnvParameter4fARB(GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w) {
    DBG(printf("glProgramEnvParameter4fARB(%s, %u, %f, %f, %f, %f)\n", PrintEnum(target), index, x, y, z, w);)
    float *f = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            if(index<MAX_VTX_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->vtx_env_params+index*4;
            }
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            if(index<MAX_FRG_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->frg_env_params+index*4;
            }
            break;
        default:
            errorShim(GL_INVALID_ENUM);
    }
    if(f) {
        f[0] = x;
        f[1] = y;
        f[2] = z;
        f[3] = w;
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glProgramEnvParameter4fvARB(GLenum target, GLuint index, const GLfloat *params)  {
    DBG(printf("glProgramEnvParameter4fvARB(%s, %u, %p[%f/%f/%f/%f])\n", PrintEnum(target), index, params, params[0], params[1], params[2], params[3]);)
    float *f = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            if(index<MAX_VTX_PROG_ENV_PARAMS) {
                f = glstate->glsl->vtx_env_params+index*4;
            }
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            if(index<MAX_FRG_PROG_ENV_PARAMS) {
                f = glstate->glsl->frg_env_params+index*4;
            }
            break;
        default:
            errorShim(GL_INVALID_ENUM);
    }
    if(f) {
        noerrorShimNoPurge();
        memcpy(f, params, 4*sizeof(float));
    } else
        errorShim(GL_INVALID_VALUE);
}

void APIENTRY_GL4ES gl4es_glProgramLocalParameter4dARB(GLenum target, GLuint index, GLdouble x, GLdouble y, GLdouble z, GLdouble w) {
    DBG(printf("glProgramLocalParameter4dARB(%s, %u, %f, %f, %f, %f)\n", PrintEnum(target), index, x, y, z, w);)
    oldprogram_t *old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(index<old->max_local_params) {
        noerrorShimNoPurge();
        float* f = old->prog_local_params+index*4;
        f[0] = x;
        f[1] = y;
        f[2] = z;
        f[3] = w;
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glProgramLocalParameter4dvARB(GLenum target, GLuint index, const GLdouble *params) {
    DBG(printf("glProgramLocalParameter4dvARB(%s, %u, %p[%f/%f/%f/%f])\n", PrintEnum(target), index, params, params[0], params[1], params[2], params[3]);)
    oldprogram_t *old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(index<old->max_local_params) {
        noerrorShimNoPurge();
        float* f = old->prog_local_params+index*4;
        f[0] = params[0];
        f[1] = params[1];
        f[2] = params[2];
        f[3] = params[3];
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glProgramLocalParameter4fARB(GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w) {
    DBG(printf("glProgramLocalParameter4fARB(%s, %u, %f, %f, %f, %f)\n", PrintEnum(target), index, x, y, z, w);)
    oldprogram_t *old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(index<old->max_local_params) {
        noerrorShimNoPurge();
        float* f = old->prog_local_params+index*4;
        f[0] = x;
        f[1] = y;
        f[2] = z;
        f[3] = w;
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glProgramLocalParameter4fvARB(GLenum target, GLuint index, const GLfloat *params) {
    DBG(printf("glProgramLocalParameter4fvARB(%s, %u, %p[%f/%f/%f/%f])\n", PrintEnum(target), index, params, params[0], params[1], params[2], params[3]);)
    oldprogram_t *old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(index<old->max_local_params) {
        noerrorShimNoPurge();
        memcpy(old->prog_local_params+index*4, params, 4*sizeof(float));
    } else
        errorShim(GL_INVALID_VALUE);
}

void APIENTRY_GL4ES gl4es_glGetProgramEnvParameterdvARB(GLenum target, GLuint index, GLdouble *params)  {
    DBG(printf("glGetProgramEnvParameterdvARB(%s, %u, %p)\n", PrintEnum(target), index, params);)
    float * f = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            if(index<MAX_VTX_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->vtx_env_params+index*4;
            }
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            if(index<MAX_FRG_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                f = glstate->glsl->frg_env_params+index*4;
            }
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(f) {
        params[0] = f[0];
        params[1] = f[1];
        params[2] = f[2];
        params[3] = f[3];
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glGetProgramEnvParameterfvARB(GLenum target, GLuint index, GLfloat *params) {
    DBG(printf("glGetProgramEnvParameterfvARB(%s, %u, %p)\n", PrintEnum(target), index, params);)
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            if(index<MAX_VTX_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                memcpy(params, glstate->glsl->vtx_env_params+index*4, 4*sizeof(float));
            } else
                errorShim(GL_INVALID_VALUE);
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            if(index<MAX_FRG_PROG_ENV_PARAMS) {
                noerrorShimNoPurge();
                memcpy(params, glstate->glsl->frg_env_params+index*4, 4*sizeof(float));
            } else
                errorShim(GL_INVALID_VALUE);
            break;
        default:
            errorShim(GL_INVALID_ENUM);
    }
}

void APIENTRY_GL4ES gl4es_glGetProgramLocalParameterdvARB(GLenum target, GLuint index, GLdouble *params) {
    DBG(printf("glGetProgramLocalParameterdvARB(%s, %u, %p)\n", PrintEnum(target), index, params);)
    oldprogram_t *old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(index<old->max_local_params) {
        noerrorShimNoPurge();
        float* f = old->prog_local_params+index*4;
        params[0] = f[0];
        params[1] = f[1];
        params[2] = f[2];
        params[3] = f[3];
    } else
        errorShim(GL_INVALID_VALUE);
}
void APIENTRY_GL4ES gl4es_glGetProgramLocalParameterfvARB(GLenum target, GLuint index, GLfloat *params) {
    DBG(printf("glGetProgramLocalParameterfvARB(%s, %u, %p)\n", PrintEnum(target), index, params);)
    oldprogram_t *old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(index<old->max_local_params) {
        noerrorShimNoPurge();
        memcpy(params, old->prog_local_params+index*4, 4*sizeof(float));
    } else
        errorShim(GL_INVALID_VALUE);
}

void APIENTRY_GL4ES gl4es_glGetProgramivARB(GLenum target, GLenum pname, GLint *params) {
    DBG(printf("glGetProgramivARB(%s, %s, %p)\n", PrintEnum(target), PrintEnum(pname), params);)
    oldprogram_t* old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_VALUE);
            return;
    }
    switch(pname) {
        case GL_PROGRAM_LENGTH_ARB:
            if(old) {
                noerrorShimNoPurge();
                *params = old->string?(strlen(old->string)+1):0;
            } else
                errorShim(GL_INVALID_OPERATION);
            break;
        case GL_PROGRAM_FORMAT_ARB:
            if(old) {
                noerrorShimNoPurge();
                *params = GL_PROGRAM_FORMAT_ASCII_ARB;
            } else
                errorShim(GL_INVALID_OPERATION);
            break;
        case GL_PROGRAM_BINDING_ARB:
            if(old) {
                noerrorShimNoPurge();
                *params = old->id;
            } else
                errorShim(GL_INVALID_OPERATION);
            break;
        case GL_MAX_PROGRAM_LOCAL_PARAMETERS_ARB:
            *params = (target==GL_VERTEX_PROGRAM_ARB)?MAX_VTX_PROG_LOC_PARAMS:MAX_FRG_PROG_LOC_PARAMS;
            break;
        case GL_MAX_PROGRAM_ENV_PARAMETERS_ARB:
            *params = (target==GL_VERTEX_PROGRAM_ARB)?MAX_VTX_PROG_ENV_PARAMS:MAX_FRG_PROG_ENV_PARAMS;
            break;
        case GL_MAX_PROGRAM_NATIVE_ATTRIBS_ARB:
        case GL_MAX_PROGRAM_ATTRIBS_ARB:
            *params = hardext.maxvattrib;
            break;
        // arbritrary settings...
        case GL_MAX_PROGRAM_NATIVE_INSTRUCTIONS_ARB:
        case GL_MAX_PROGRAM_INSTRUCTIONS_ARB:
            *params = 4096;
            break;
        case GL_MAX_PROGRAM_NATIVE_TEMPORARIES_ARB:
        case GL_MAX_PROGRAM_TEMPORARIES_ARB:
            *params = 64;
            break;
        case GL_MAX_PROGRAM_NATIVE_PARAMETERS_ARB:
        case GL_MAX_PROGRAM_PARAMETERS_ARB:
            *params = 64;
            break;
        case GL_MAX_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB:
        case GL_MAX_PROGRAM_ADDRESS_REGISTERS_ARB:
            *params = 4;
            break;
        case GL_MAX_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB:
        case GL_MAX_PROGRAM_ALU_INSTRUCTIONS_ARB:
            *params = 1024;
            break;
        case GL_MAX_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB:
        case GL_MAX_PROGRAM_TEX_INSTRUCTIONS_ARB:
            *params = 32;
            break;
        case GL_MAX_PROGRAM_TEX_INDIRECTIONS_ARB:
        case GL_MAX_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB:
            *params = 8;
            break;
        /*
        // bounded program stats...
        case GL_PROGRAM_NATIVE_INSTRUCTIONS_ARB:
        case GL_PROGRAM_INSTRUCTIONS_ARB:
        case GL_PROGRAM_NATIVE_TEMPORARIES_ARB:
        case GL_PROGRAM_TEMPORARIES_ARB:
        case GL_PROGRAM_NATIVE_PARAMETERS_ARB:
        case GL_PROGRAM_PARAMETERS_ARB:
        case GL_PROGRAM_NATIVE_ATTRIBS_ARB:
        case GL_PROGRAM_ATTRIBS_ARB:
        case GL_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB:
        case GL_PROGRAM_ADDRESS_REGISTERS_ARB:
        case GL_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB:
        case GL_PROGRAM_ALU_INSTRUCTIONS_ARB:
        case GL_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB:
        case GL_PROGRAM_TEX_INSTRUCTIONS_ARB:
        case GL_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB:
        case GL_PROGRAM_TEX_INDIRECTIONS_ARB:
        */
        case GL_PROGRAM_UNDER_NATIVE_LIMITS_ARB:
            *params = 1;    // always return OK for now
            break;
        default:
            errorShim(GL_INVALID_ENUM);
    }
}

void APIENTRY_GL4ES gl4es_glGetProgramStringARB(GLenum target, GLenum pname, GLvoid *string) {
    DBG(printf("glGetProgramStringARB(%s, %u, %p)\n", PrintEnum(target), pname, string);)
    oldprogram_t* old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_VALUE);
            return;
    }
    if(pname!=GL_PROGRAM_STRING_ARB) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(old->string)
        strcpy(string, old->string);
    noerrorShimNoPurge();
}

/*
// same as GLSL version
void APIENTRY_GL4ES gl4es_glGetVertexAttribdvARB(GLuint index, GLenum pname, GLdouble *params);
void APIENTRY_GL4ES gl4es_glGetVertexAttribfvARB(GLuint index, GLenum pname, GLfloat *params);
void APIENTRY_GL4ES gl4es_glGetVertexAttribivARB(GLuint index, GLenum pname, GLint *params);

void APIENTRY_GL4ES gl4es_glGetVertexAttribPointervARB(GLuint index, GLenum pname, GLvoid **pointer);
*/

GLboolean APIENTRY_GL4ES gl4es_glIsProgramARB(GLuint program) {
    DBG(printf("glIsProgramARB(%u)\n", program);)
    khint_t k = kh_get(oldprograms, glstate->glsl->oldprograms, program);
    return (k==kh_end(glstate->glsl->oldprograms))?GL_FALSE:GL_TRUE;
}


void APIENTRY_GL4ES gl4es_glProgramEnvParameters4fvEXT(GLenum target, GLuint index, GLsizei count, const GLfloat *params)
{
    DBG(printf("glProgramEnvParameters4fvEXT(%s, %u, %i, %p)\n", PrintEnum(target), index, count, params);)
    float *f = NULL;
    int nmax = 0;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            f = glstate->glsl->vtx_env_params+index*4;
            nmax = MAX_VTX_PROG_ENV_PARAMS;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            f = glstate->glsl->frg_env_params+index*4;
            nmax = MAX_FRG_PROG_ENV_PARAMS;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
    }
    if(f && index+count<=nmax && count>=0) {
        noerrorShimNoPurge();
        memcpy(f, params, count*4*sizeof(float));
    } else
        errorShim(GL_INVALID_VALUE);
}

void APIENTRY_GL4ES gl4es_glProgramLocalParameters4fvEXT(GLenum target, GLuint index, GLsizei count, const GLfloat *params)
{
    DBG(printf("glProgramLocalParameters4fvEXT(%s, %u, %i, %p)\n", PrintEnum(target), index, count, params);)
    float *f = NULL;
    oldprogram_t *old = NULL;
    switch(target) {
        case GL_VERTEX_PROGRAM_ARB:
            old = glstate->glsl->vtx_prog;
            break;
        case GL_FRAGMENT_PROGRAM_ARB:
            old = glstate->glsl->frg_prog;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if(!old) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    if(index+count<old->max_local_params && count>=0) {
        noerrorShimNoPurge();
        memcpy(old->prog_local_params+index*4, params, count*4*sizeof(float));
    } else
        errorShim(GL_INVALID_VALUE);
}

// Mappers for ARB_vertex_program
AliasExport(void,glProgramStringARB,,(GLenum target, GLenum format, GLsizei len, const GLvoid *string));
AliasExport(void,glBindProgramARB,,(GLenum target, GLuint program));
AliasExport(void,glDeleteProgramsARB,,(GLsizei n, const GLuint *programs));
AliasExport(void,glGenProgramsARB,,(GLsizei n, GLuint *programs));
AliasExport_M(void,glProgramEnvParameter4dARB,,(GLenum target, GLuint index, GLdouble x, GLdouble y, GLdouble z, GLdouble w),40);
AliasExport(void,glProgramEnvParameter4dvARB,,(GLenum target, GLuint index, const GLdouble *params));
AliasExport(void,glProgramEnvParameter4fARB,,(GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w));
AliasExport(void,glProgramEnvParameter4fvARB,,(GLenum target, GLuint index, const GLfloat *params));
AliasExport_M(void,glProgramLocalParameter4dARB,,(GLenum target, GLuint index, GLdouble x, GLdouble y, GLdouble z, GLdouble w),40);
AliasExport(void,glProgramLocalParameter4dvARB,,(GLenum target, GLuint index, const GLdouble *params));
AliasExport(void,glProgramLocalParameter4fARB,,(GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w));
AliasExport(void,glProgramLocalParameter4fvARB,,(GLenum target, GLuint index, const GLfloat *params));
AliasExport(void,glGetProgramEnvParameterdvARB,,(GLenum target, GLuint index, GLdouble *params));
AliasExport(void,glGetProgramEnvParameterfvARB,,(GLenum target, GLuint index, GLfloat *params));
AliasExport(void,glGetProgramLocalParameterdvARB,,(GLenum target, GLuint index, GLdouble *params));
AliasExport(void,glGetProgramLocalParameterfvARB,,(GLenum target, GLuint index, GLfloat *params));
AliasExport(void,glGetProgramivARB,,(GLenum target, GLenum pname, GLint *params));
AliasExport(void,glGetProgramStringARB,,(GLenum target, GLenum pname, GLvoid *string));
AliasExport(GLboolean,glIsProgramARB,,(GLuint program));

AliasExport(void,glProgramEnvParameters4fvEXT,,(GLenum target, GLuint index, GLsizei count, const GLfloat *params));
AliasExport(void,glProgramLocalParameters4fvEXT,,(GLenum target, GLuint index, GLsizei count, const GLfloat *params));