#include "program.h"

#include <stdlib.h>
#include <string.h>
#include "../glx/hardext.h"
#include "debug.h"
#include "fpe.h"
#include "gl4es.h"
#include "glstate.h"
#include "loader.h"
#include "shaderconv.h"
#include "fpe_shader.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

//KH Map implementations
KHASH_MAP_IMPL_INT(attribloclist, attribloc_t *);
KHASH_MAP_IMPL_INT(uniformlist, uniform_t *);
KHASH_MAP_IMPL_INT(programlist, program_t *);


void APIENTRY_GL4ES gl4es_glAttachShader(GLuint program, GLuint shader) {
    DBG(printf("glAttachShader(%d, %d)\n", program, shader);)
    FLUSH_BEGINEND;
    // sanity tests
    CHECK_PROGRAM(void, program)
    CHECK_SHADER(void, shader)

    // add shader reference to program
    if(glprogram->attach_cap == glprogram->attach_size) {
        glprogram->attach_cap += 4;
        glprogram->attach = (GLuint*)realloc(glprogram->attach, sizeof(GLuint)*glprogram->attach_cap);
    }
    glprogram->attach[glprogram->attach_size++] = glshader->id;
    glshader->attached++;
    // save last vertex or fragment attached
    if(glshader->type==GL_VERTEX_SHADER && !glprogram->last_vert)
        glprogram->last_vert = glshader;
    else if(glshader->type==GL_FRAGMENT_SHADER && !glprogram->last_frag)
        glprogram->last_frag = glshader;
    // send to hadware
    LOAD_GLES2(glAttachShader);
    if(gles_glAttachShader) {
        gles_glAttachShader(glprogram->id, glshader->id);
        errorGL();
    } else
        noerrorShim();
}

void APIENTRY_GL4ES gl4es_glBindAttribLocation(GLuint program, GLuint index, const GLchar *name) {
    DBG(printf("glBindAttribLocation(%d, %d, \"%s\")\n", program, index, name);)
    FLUSH_BEGINEND;
    // sanity tests
    CHECK_PROGRAM(void, program)
    // look / create attribloc at index
    attribloc_t *attribloc = NULL;
    {
        khint_t k;
        int ret;
        khash_t(attribloclist) *attribloclist = glprogram->attribloc;
        k = kh_get(attribloclist, attribloclist, index);
        if (k != kh_end(attribloclist)) {
            attribloc = kh_value(attribloclist, k);
        } else {
            k = kh_put(attribloclist, attribloclist, index, &ret);
            attribloc = kh_value(attribloclist, k) = malloc(sizeof(attribloc_t));
            memset(attribloc, 0, sizeof(attribloc_t));
            attribloc->real_index = -1;
            attribloc->index = index;
        }
    }
    // update name
    if(attribloc->name) free(attribloc->name);
    attribloc->name = strdup(name);
    attribloc->glname = attribloc->name;
    // send to hardware
    LOAD_GLES2(glBindAttribLocation);
    if(gles_glBindAttribLocation) {
        gles_glBindAttribLocation(glprogram->id, index, attribloc->name);
        errorGL();
    } else
        noerrorShim();
}

GLuint APIENTRY_GL4ES gl4es_glCreateProgram(void) {
    DBG(printf("glCreateProgram()\n");)
    FLUSH_BEGINEND;
    static GLuint lastprogram = 0;
    GLuint program;
    // create the program
    LOAD_GLES2(glCreateProgram);
    if(gles_glCreateProgram) {
        program = gles_glCreateProgram();
        if(!program) {
            errorGL();
            return 0;
        }
    } else {
        program = ++lastprogram;
        noerrorShim();
    }
    // store the new empty shader in the list for later use
   	khint_t k;
   	int ret;
	khash_t(programlist) *programs = glstate->glsl->programs;
    k = kh_get(programlist, programs, program);
    program_t *glprogram = NULL;
    if (k == kh_end(programs)){
        k = kh_put(programlist, programs, program, &ret);
        glprogram = kh_value(programs, k) = (program_t*)calloc(1, sizeof(program_t));
    } else {
        glprogram = kh_value(programs, k);
        if(glprogram->attribloc) {
            attribloc_t *m;
            kh_foreach_value(glprogram->attribloc, m,
                free(m->name); free(m);
            )
            kh_destroy(attribloclist, glprogram->attribloc);
            glprogram->attribloc = NULL;
        }
        memset(glprogram, 0, sizeof(program_t));
    }
    glprogram->id = program;
    // initialize attribloc hashmap
    khash_t(attribloclist) *attribloc = glprogram->attribloc = kh_init(attribloclist);
    // initialize uniform hashmap
    khash_t(uniformlist) *uniform = glprogram->uniform = kh_init(uniformlist);
    // all done
    return program;
}

void actually_deleteshader(GLuint shader);
void actually_detachshader(GLuint shader);

void deleteProgram(program_t *glprogram, khint_t k_program) {
    free(glprogram->attach);
    // clean attribloc
    if(glprogram->attribloc) {
        attribloc_t *m;
        kh_foreach_value(glprogram->attribloc, m,
            free(m->name); free(m);
        )
        kh_destroy(attribloclist, glprogram->attribloc);
        glprogram->attribloc = NULL;
    }
    // clean uniform list
    if(glprogram->uniform) {
        uniform_t *m;
        kh_foreach_value(glprogram->uniform, m,
            free(m->name); free(m);
        )
        kh_destroy(uniformlist, glprogram->uniform);
        glprogram->uniform = NULL;
    }
    // clean cache
    if(glprogram->cache.cache)
        free(glprogram->cache.cache);
    // clean fpe cache if it exist
    if(glprogram->fpe_cache)
        fpe_disposeCache((fpe_cache_t*)glprogram->fpe_cache, 1);
    // delete program
    kh_del(programlist, glstate->glsl->programs, k_program);
    free(glprogram);
}

void APIENTRY_GL4ES gl4es_glDeleteProgram(GLuint program) {
    DBG(printf("glDeleteProgram(%d)\n", program);)
    if(!glstate) return;    // in case a program delete shaders after deleteing all context
    FLUSH_BEGINEND;
    CHECK_PROGRAM(void, program)
    // send to hardware
    LOAD_GLES2(glDeleteProgram);
    if(gles_glDeleteProgram) {
        gles_glDeleteProgram(glprogram->id);
        errorGL();
    } else
        noerrorShim();
    // TODO: check GL ERROR to not clean in case of error?
    // clean attached shaders
    for (int i=0; i<glprogram->attach_size; i++) {
        actually_detachshader(glprogram->attach[i]); // auto delete if marked as delete!
    }
    deleteProgram(glprogram, k_program);
}

void APIENTRY_GL4ES gl4es_glDetachShader(GLuint program, GLuint shader) {
    DBG(printf("glDetachShader(%d, %d)\n", program, shader);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(void, program)
    CHECK_SHADER(void, shader)
    // is shader attached?
    int f = 0;
    while(f<glprogram->attach_size && glprogram->attach[f]!=shader)
        f++;
    // if program is linked, don't try anything....
    if(glprogram->linked) {
        noerrorShim();
        return;
    }
    // send to hardware
    LOAD_GLES2(glDetachShader);
    if(gles_glDetachShader) {
        gles_glDetachShader(glprogram->id, glshader->id);
        errorGL();
    } else
        noerrorShim();
    // marked as detached
    actually_detachshader(shader);
}

void APIENTRY_GL4ES gl4es_glGetActiveAttrib(GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size, GLenum *type, GLchar *name) {
    DBG(printf("glGetActiveAttrib(%d, %d, %d, %p, %p, %p, %p)\n", program, index, bufSize, length, size, type, name);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(void, program)

    if(glprogram->attribloc) {
        attribloc_t *attribloc;
        kh_foreach_value(glprogram->attribloc, attribloc,
            if(attribloc->real_index == index) {
                if(type) *type = attribloc->type;
                if(size) *size = attribloc->size;
                if(length) *length = strlen(attribloc->glname);
                if(bufSize && name) {
                    strncpy(name, attribloc->glname, bufSize-1);
                    name[bufSize-1] = '\0';
                }
                DBG(printf("found, type=%s, size=%d, name=%s/%s\n", PrintEnum(attribloc->type), attribloc->size, attribloc->name, attribloc->glname);)
                noerrorShim();
                return;
            }
        );
    }
    DBG(printf("not found\n");)
    errorShim(GL_INVALID_VALUE);    
}

void APIENTRY_GL4ES gl4es_glGetAttachedShaders(GLuint program, GLsizei maxCount, GLsizei *count, GLuint *shaders) {
    DBG(printf("glGetAttachedShaders(%d, %d, %p, %p)\n", program, maxCount, count, shaders);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(void, program)

    int n = glprogram->attach_size;
    if(n>maxCount) n = maxCount;

    if (count) *count=n;
    if (shaders)
        for (int i=0; i<n; i++)
            shaders[i] = glprogram->attach[i];

    noerrorShim();
}

GLint APIENTRY_GL4ES gl4es_glGetAttribLocation(GLuint program, const GLchar *name) {
    DBG(printf("glGetAttribLocation(%d, %s)\n", program, name));
    FLUSH_BEGINEND;
    CHECK_PROGRAM(GLint, program);

    if(!glprogram->linked) {
        errorShim(GL_INVALID_OPERATION);
        DBG(printf(" program unlinked: -1\n"));
        return -1;
    }
    noerrorShim();
    if(strncmp(name, "gl_", 3)==0) {
        DBG(printf(" internal attrib: -1\n"));
        return -1;
    }

    int rloc = -1;
    int loc = -1;
    // look in already created attribloc
    if(glprogram->attribloc) {
        attribloc_t *m;
        kh_foreach_value(glprogram->attribloc, m,
            if(strcmp(m->glname, name)==0) {
                loc = m->index;
                rloc = m->real_index;
            }
        )
    }
    // if found, just return the value, it's done...
    if(loc!=-1) {
        DBG(printf(" found in cache: %d\n", loc);)
        return loc;
    }
    // end
    DBG(printf(" asked hardware: %d\n", loc);)
    return loc;
}

void APIENTRY_GL4ES gl4es_glGetActiveUniform(GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size, GLenum *type, GLchar *name) {
    DBG(printf("glGetActiveUniform(%d, %d, %d, %p, %p, %p, %p)\n", program, index, bufSize, length, size, type, name);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(GLvoid, program);

    if(!glprogram->linked) {
        errorShim(GL_INVALID_OPERATION);
        DBG(printf(" not linked\n");)
        return;
    }
    noerrorShim();
    if(strncmp(name, "gl_", 3)==0) {
        DBG(printf(" internal uniform\n");)
        return;
    }

    // look in uniform cache, that is filled when program is linked
    if(glprogram->uniform) {
        uniform_t *m;
        kh_foreach_value(glprogram->uniform, m,
            if(m->internal_id == index) {
                if(type) *type = m->type;
                if(size) *size = m->size;
                if(length) *length = strlen(m->name);
                if(bufSize && name) {
                    strncpy(name, m->name, bufSize-1);
                    name[bufSize-1] = '\0';
                }
                DBG(printf(" found %s (%zd), type=%s, size=%d\n", m->name, strlen(m->name), PrintEnum(m->type), m->size);)
                return;
            }
        );
    }
    // end
    DBG(printf(" not found\n");)
    errorShim(GL_INVALID_VALUE);
}

static const char* notlinked = "Program not linked";
static const char* linked = "Program linked, but no shader support";
static const char* validated = "Program validated, but no shader support";
const char* getFakeProgramInfo(program_t* glprogram) {
    return (glprogram->linked)?((glprogram->validated)?validated:linked):notlinked;
}

void APIENTRY_GL4ES gl4es_glGetProgramInfoLog(GLuint program, GLsizei maxLength, GLsizei *length, GLchar *infoLog) {
    DBG(printf("glGetProgramInfoLog(%d, %d, %p, %p)\n", program, maxLength, length, infoLog);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(void, program)

    if(maxLength<0) {
        errorShim(GL_INVALID_VALUE);
        return;
    }
    if(!maxLength) {
        noerrorShim();
        return;
    }

    LOAD_GLES2(glGetProgramInfoLog);
    if(gles_glGetProgramInfoLog) {
        gles_glGetProgramInfoLog(glprogram->id, maxLength, length, infoLog);
        errorGL();
    } else {
        const char* res = getFakeProgramInfo(glprogram);
        int l = strlen(res)+1;
        if (l>maxLength) l = maxLength;
        if(length) *length = l-1;
        if(infoLog) strncpy(infoLog, res, l);
        noerrorShim();
    }
}

void APIENTRY_GL4ES gl4es_glGetProgramiv(GLuint program, GLenum pname, GLint *params) {
    DBG(printf("glGetProgramiv(%d, %s, %p)\n", program, PrintEnum(pname), params);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(void, program)

    LOAD_GLES2(glGetProgramiv);
    noerrorShim();
    switch(pname) {
        case GL_DELETE_STATUS:
            if(gles_glGetProgramiv) {
                gles_glGetProgramiv(glprogram->id, pname, params);
                errorGL();
            } else
                *params = GL_FALSE;
            break;
        case GL_LINK_STATUS:
            *params = glprogram->linked?GL_TRUE:GL_FALSE;
            break;
        case GL_VALIDATE_STATUS:
            *params = glprogram->valid_result;
            break;
        case GL_INFO_LOG_LENGTH:
            if(gles_glGetProgramiv) {
                gles_glGetProgramiv(glprogram->id, pname, params);
                errorGL();
            } else
                *params = strlen(getFakeProgramInfo(glprogram));
            break;
        case GL_ATTACHED_SHADERS:
            *params = glprogram->attach_size;
            break;
        case GL_ACTIVE_ATTRIBUTES:
        case GL_ACTIVE_ATTRIBUTE_MAX_LENGTH:
            if(gles_glGetProgramiv) {
                gles_glGetProgramiv(glprogram->id, pname, params);
                errorGL();
            } else
                *params = 0;
            break;
        case GL_ACTIVE_UNIFORMS:
            *params = (glprogram->uniform)?glprogram->num_uniform:0;
            break;
        case GL_ACTIVE_UNIFORM_MAX_LENGTH:
            {
                int l = 0;
                uniform_t *m;
                kh_foreach_value(glprogram->uniform, m,
                    if(l<strlen(m->name)+1)
                        l = strlen(m->name)+1;
                )
                *params = l;
            }
            break;
        case GL_PROGRAM_BINARY_LENGTH:
            // TODO: check if extension is present
            if(gles_glGetProgramiv) {
                gles_glGetProgramiv(glprogram->id, pname, params);
                errorGL();
            } else
                errorShim(GL_INVALID_ENUM);
            break;
            
        default:
            if(gles_glGetProgramiv) {
                gles_glGetProgramiv(glprogram->id, pname, params);
                errorGL();
            } else
                errorShim(GL_INVALID_ENUM);
            break;
    }
}

GLint APIENTRY_GL4ES gl4es_glGetUniformLocation(GLuint program, const GLchar *name) {
    DBG(printf("glGetUniformLocation(%d, %s)\n", program, name);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(GLint, program)

    noerrorShim();
    int res = -1;
    if(strncmp(name, "gl_", 3)==0) {
        DBG(printf(" internal uniform: -1\n");)
        return res;
    }

    int index = 0;
    int l = strlen(name);
    // get array (only if end with ])
    if(name[l-1]==']') {
        char * p = strrchr(name, '[');
        l = p-name;
        p++;
        while(p && *p>='0' && *p<='9') {
            index = index*10 + *(p++)-'0';
        }
    }
    if(glprogram->uniform) {
        uniform_t *m;
        kh_foreach_value(glprogram->uniform, m,
            if(strlen(m->name)==l && strncmp(m->name, name, l)==0) {
                res = m->id;
                if(index>m->size) {
                    res = -1;   // too big !
                } else
                    res += index;
                break;
            }
        )
    }
    DBG(printf(" location: %d\n", res);)
    return res;
}

GLboolean APIENTRY_GL4ES gl4es_glIsProgram(GLuint program) {
    DBG(printf("glIsProgram(%d)\n", program);)
    FLUSH_BEGINEND;
    noerrorShim();
    if(!program) {
        return GL_FALSE;
    }
    program_t *glprogram = NULL;
    khint_t k;
    khash_t(programlist) *programs = glstate->glsl->programs;
    k = kh_get(programlist, programs, program);
    if (k != kh_end(programs))
        return GL_TRUE;
    return GL_FALSE;
}

static void clear_program(program_t *glprogram)
{
    // clear all Attrib location cache
    if(glprogram->attribloc) {
        attribloc_t *m;
        khint_t k;
        int ret;
        //attribloc->glname must not be freed
        kh_foreach(glprogram->attribloc, k, m,
            free(m->name); free(m);
            kh_del(attribloclist, glprogram->attribloc, k);
        )
        kh_destroy(attribloclist, glprogram->attribloc);
        khash_t(attribloclist) *attribloc = glprogram->attribloc = kh_init(attribloclist);
        kh_put(attribloclist, attribloc, 1, &ret);
        kh_del(attribloclist, attribloc, 1);
    }
    // clear all Uniform cache
    glprogram->num_uniform = 0;
    if(glprogram->uniform) {
        uniform_t *m;
        khint_t k;
        kh_foreach(glprogram->uniform, k, m,
            free(m->name); free(m);
            kh_del(uniformlist, glprogram->uniform, k);
        )
    }
    glprogram->cache.size = 0;  // reset cache buffer
}

static void fill_program(program_t *glprogram)
{
    LOAD_GLES(glGetError);
    LOAD_GLES2(glGetProgramiv);
    LOAD_GLES2(glGetActiveUniform);
    LOAD_GLES2(glGetUniformLocation);
    LOAD_GLES2(glGetActiveAttrib);
    LOAD_GLES2(glGetAttribLocation);
    int n=0;
    int maxsize=0;
    khint_t k;
    int ret;
    DBG(GLenum e2;)

    // init bluitin emulation first
    builtin_Init(glprogram);
    // Grab all Uniform
    gles_glGetProgramiv(glprogram->id, GL_ACTIVE_UNIFORMS, &n);
    gles_glGetProgramiv(glprogram->id, GL_ACTIVE_UNIFORM_MAX_LENGTH, &maxsize);
    khash_t(uniformlist) *uniforms = glprogram->uniform;
    uniform_t *gluniform = NULL;
    int uniform_cache = 0;
    GLint size = 0;
    GLenum type = 0;
    GLchar *name = (char*)malloc(maxsize);
    int tu_idx = 0;
    for (int i=0; i<n; i++) {
        gles_glGetActiveUniform(glprogram->id, i, maxsize, NULL, &size, &type, name);
        DBG(e2=gles_glGetError();)
        DBG(if(e2==GL_NO_ERROR))
        {
            // remove any ending "[]" that could be present
            if(name[strlen(name)-1]==']' && strrchr(name, '[')) (*strrchr(name, '['))='\0';
            GLint id = gles_glGetUniformLocation(glprogram->id, name);
            if(id!=-1) {
                for (int j = 0; j<size; j++) {
                    k = kh_put(uniformlist, uniforms, id, &ret);
                    gluniform = kh_value(uniforms, k) = malloc(sizeof(uniform_t));
                    memset(gluniform, 0, sizeof(uniform_t));
                    if(j) {
                        gluniform->name = malloc(strlen(name)+1+5);
                        sprintf(gluniform->name, "%s[%d]", name, j);
                    } else
                        gluniform->name = strdup(name);
                    gluniform->id = id;
                    gluniform->internal_id = i;
                    gluniform->size = size-j;
                    gluniform->type = type;
                    gluniform->cache_offs = uniform_cache+j*uniformsize(type);
                    gluniform->cache_size = uniformsize(type)*(size-j);
                    gluniform->builtin = builtin_CheckUniform(glprogram, name, id, size-j);
                    // TextureUnit grabbing...
                    if(type==GL_SAMPLER_CUBE) {
                        glprogram->texunits[tu_idx].id = id;
                        glprogram->texunits[tu_idx].type=TU_CUBE;
                        glprogram->texunits[tu_idx].req_tu = glprogram->texunits[tu_idx].act_tu = 0;
                        ++tu_idx;
                    } else if (type==GL_SAMPLER_2D) {
                        glprogram->texunits[tu_idx].id = id;
                        glprogram->texunits[tu_idx].type=TU_TEX2D;
                        glprogram->texunits[tu_idx].req_tu = glprogram->texunits[tu_idx].act_tu = 0;
                        ++tu_idx;
                    }
                    DBG(printf(" uniform #%d : \"%s\"%s type=%s size=%d\n", id, gluniform->name, gluniform->builtin?" (builtin) ":"", PrintEnum(gluniform->type), gluniform->size);)
                    if(gluniform->size==1) ++glprogram->num_uniform;
                    id++;
                }
                uniform_cache += uniformsize(type)*size;
            }
        }
        DBG(else printf("LIBGL: Warning, getting Uniform #%d info failed with %s\n", i, PrintEnum(e2));)
    }
    free(name);
    // reset uniform cache
    if(glprogram->cache.cap < uniform_cache) {
        glprogram->cache.cap=uniform_cache;
        glprogram->cache.cache = malloc(glprogram->cache.cap);
    }
    memset(glprogram->cache.cache, 0, glprogram->cache.cap);
    //Maybe Sampler uniform should not be initialized to 0, but to -1, to be sure the value is initialized?
    if(glprogram->uniform) {
        uniform_t *m;
        khint_t k;
        kh_foreach(glprogram->uniform, k, m,
            if(m->type == GL_SAMPLER_2D || m->type == GL_SAMPLER_CUBE)
                memset((char*)glprogram->cache.cache+m->cache_offs, 0xff, m->cache_size);
        )
    }

    // Grab all Attrib
    gles_glGetProgramiv(glprogram->id, GL_ACTIVE_ATTRIBUTES, &n);
    gles_glGetProgramiv(glprogram->id, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, &maxsize);
    name = (char*)malloc(maxsize);
    for (int i=0; i<n; i++) {
        DBG(e2=gles_glGetError();)
        DBG(if(e2==GL_NO_ERROR))
        {
            gles_glGetActiveAttrib(glprogram->id, i, maxsize, NULL, &size, &type, name);
            GLint id = gles_glGetAttribLocation(glprogram->id, name);
            if(id!=-1) {
                attribloc_t *glattribloc = NULL;
                k = kh_put(attribloclist, glprogram->attribloc, id, &ret);
                if(ret==0) {
                    // already there
                    glattribloc = kh_value(glprogram->attribloc, k);
                    if(glattribloc->name)
                        free(glattribloc->name);
                } else {
                    glattribloc = kh_value(glprogram->attribloc, k) = malloc(sizeof(attribloc_t));
                }
                memset(glattribloc, 0, sizeof(attribloc_t));
                glattribloc->name = strdup(name);
                glattribloc->glname = builtinAttribGLName(glattribloc->name);
                glattribloc->size = size;
                glattribloc->type = type;
                glattribloc->index = id;
                glattribloc->real_index = i;
                int builtin = builtin_CheckVertexAttrib(glprogram, name, id);
                glprogram->va_size[id] = n_uniform(type); // same as uniform
                DBG(printf(" attrib #%d : \"%s\"%s type=%s size=%d\n", id, glattribloc->name, builtin?" (builtin) ":"", PrintEnum(glattribloc->type), glattribloc->size);)
            }
        }
        DBG(else printf("LIBGL: Warning, getting Attrib #%d info failed with %s\n", i, PrintEnum(e2));)
    }
    free(name);
}

int gl4es_useProgramBinary(GLuint program, int length, GLenum format, const void* binary)
{
    if(hardext.prgbin_n==0)
        return 0;
    DBG(printf("useProgramBinary(%d, %d, %d, %p)\n", program, length, format, binary);)
    CHECK_PROGRAM(int, program)
    noerrorShim();

    clear_program(glprogram);

    LOAD_GLES_OES(glProgramBinary);
    LOAD_GLES2(glGetProgramiv);

    gles_glProgramBinary(glprogram->id, format, binary, length);

    gles_glGetProgramiv(glprogram->id, GL_LINK_STATUS, &glprogram->linked);
    DBG(printf(" link status = %d\n", glprogram->linked);)
    if(glprogram->linked) {
        fill_program(glprogram);
    } else {
        // should DBG the linker error?
        DBG(printf(" Load failed!\n");)
        glprogram->linked = 0;
        errorGL();
    }
    return glprogram->linked;
}

int gl4es_getProgramBinary(GLuint program, int *length, GLenum *format, void** binary)
{
    if(hardext.prgbin_n==0)
        return 0;
    DBG(printf("getProgramBinary(%d, %p, %p, %p)\n", program, length, format, binary);)
    CHECK_PROGRAM(int, program)
    noerrorShim();

    LOAD_GLES_OES(glGetProgramBinary);
    LOAD_GLES2(glGetProgramiv);

    int l;
    gles_glGetProgramiv(glprogram->id, GL_PROGRAM_BINARY_LENGTH_OES, &l);
    if(!l)
        return 0;

    *binary = malloc(l);
    gles_glGetProgramBinary(glprogram->id, l, length, format, *binary);

    return (*length);
}

void APIENTRY_GL4ES gl4es_glGetProgramBinary(GLuint program, GLsizei bufSize, GLsizei *length, GLenum *binaryFormat, void *binary)
{
    if(hardext.prgbin_n==0) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    DBG(printf("glGetProgramBinary(%d, %d, %p, %p, %p)\n", program, bufSize, length, binaryFormat, binary);)
    CHECK_PROGRAM(void, program)
    LOAD_GLES_OES(glGetProgramBinary);
    gles_glGetProgramBinary(glprogram->id, bufSize, length, binaryFormat, binary);
    errorGL();
}

void APIENTRY_GL4ES gl4es_glProgramBinary(GLuint program, GLenum binaryFormat, const void *binary, GLsizei length)
{
    if(hardext.prgbin_n==0) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    DBG(printf("glProgramBinary(%d, %d, %p, %d)\n", program, binaryFormat, binary, length);)
    if(gl4es_useProgramBinary(program, length, binaryFormat, binary))
        noerrorShim();
    else
        errorShim(GL_INVALID_OPERATION);
}

void APIENTRY_GL4ES gl4es_glLinkProgram(GLuint program) {
    DBG(printf("glLinkProgram(%d)\n", program);)
    FLUSH_BEGINEND;
    CHECK_PROGRAM(void, program)
    noerrorShim();

    clear_program(glprogram);

    // check if attached shaders are compatible in term of varying...
    shaderconv_need_t needs = {0};
    needs.need_texcoord = -1;
    // first get the compatible need
    for (int i=0; i<glprogram->attach_size; i++) {
        accumShaderNeeds(glprogram->attach[i], &needs);
    }
    // create one vertex shader if needed!
    if(!glprogram->last_vert) {
        glprogram->default_need = (shaderconv_need_t*)malloc(sizeof(shaderconv_need_t));
        memcpy(glprogram->default_need, &needs, sizeof(shaderconv_need_t));
        glprogram->default_vertex = 1;
        GLenum vtx = gl4es_glCreateShader(GL_VERTEX_SHADER);
        gl4es_glShaderSource(vtx, 1, fpe_VertexShader(&needs, NULL), NULL);
        gl4es_glCompileShader(vtx);
        gl4es_glAttachShader(glprogram->id, vtx);
    }
    // create one fragment shader if needed!
    if(!glprogram->last_frag) {
        glprogram->default_need = (shaderconv_need_t*)malloc(sizeof(shaderconv_need_t));
        memcpy(glprogram->default_need, &needs, sizeof(shaderconv_need_t));
        glprogram->default_fragment = 1;
        GLenum vtx = gl4es_glCreateShader(GL_FRAGMENT_SHADER);
        gl4es_glShaderSource(vtx, 1, fpe_FragmentShader(&needs, NULL), NULL);
        gl4es_glCompileShader(vtx);
        gl4es_glAttachShader(glprogram->id, vtx);
    }
    int compatible = 1;
    // now is everyone ok?
    for (int i=0; i<glprogram->attach_size && compatible; i++) {
        compatible = isShaderCompatible(glprogram->attach[i], &needs);
    }
    // someone is not compatible, redoing shaders...
    if(!compatible) {
        DBG(printf("Need to redo some shaders...\n");)
        for (int i=0; i<glprogram->attach_size; i++) {
            redoShader(glprogram->attach[i], &needs);
        }
    }
    // check if Built-in VA are used, and if so, bind them to their proper location
    if(glprogram->last_vert)
        for (int i=0; i<ATT_MAX; ++i) {
            const char* attribute = hasBuiltinAttrib(glprogram->last_vert->converted, i);
            if(attribute)
                gl4es_glBindAttribLocation(glprogram->id, i, attribute);
        }
    // ok, continue with linking
    LOAD_GLES2(glLinkProgram);
    if(gles_glLinkProgram) {
        LOAD_GLES(glGetError);
        LOAD_GLES2(glGetProgramiv);
        gles_glLinkProgram(glprogram->id);
        GLenum err = gles_glGetError();
        // Get Link Status
        gles_glGetProgramiv(glprogram->id, GL_LINK_STATUS, &glprogram->linked);
        DBG(printf(" link status = %d\n", glprogram->linked);)
        if(glprogram->linked) {
            fill_program(glprogram);
            noerrorShimNoPurge();
        } else {
            // should DBG the linker error?
            DBG(printf(" Link failled!\n");)
            glprogram->linked = 0;
            errorShim(err);
            return;
        }
        
    } else {
        noerrorShim();
    }
    glprogram->linked = 1;
}

void APIENTRY_GL4ES gl4es_glUseProgram(GLuint program) {
    DBG(printf("glUseProgram(%d) old=%d\n", program, glstate->glsl->program);)
    PUSH_IF_COMPILING(glUseProgram);
    if(program==0) {
        glstate->glsl->program=0;
        glstate->glsl->glprogram=NULL;
        return;
    }
    CHECK_PROGRAM(void, program)
    noerrorShim();
    DBG(printf("program id=%d\n", glprogram->id);)

    glstate->glsl->program=glprogram->id;
    glstate->glsl->glprogram=glprogram;
}

void APIENTRY_GL4ES gl4es_glValidateProgram(GLuint program) {
    CHECK_PROGRAM(void, program)
    FLUSH_BEGINEND;
    noerrorShim();

    LOAD_GLES2(glValidateProgram);
    if(gles_glValidateProgram) {
        LOAD_GLES(glGetError);
        LOAD_GLES2(glGetProgramiv);
        gles_glValidateProgram(glprogram->id);
        GLenum err = gles_glGetError();
        gles_glGetProgramiv(glprogram->id, GL_VALIDATE_STATUS, (GLint *) &glprogram->valid_result);
        errorShim(err);
        // TODO: grab all Uniform and Attrib of the program
    } else {
        noerrorShim();
    }
    glprogram->validated = 1;
}


AliasExport(void,glAttachShader,,(GLuint program, GLuint shader));
AliasExport(void,glBindAttribLocation,,(GLuint program, GLuint index, const GLchar *name));
AliasExport_V(GLuint,glCreateProgram);
AliasExport(void,glDeleteProgram,,(GLuint program));
AliasExport(void,glDetachShader,,(GLuint program, GLuint shader));
AliasExport(void,glGetActiveAttrib,,(GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size, GLenum *type, GLchar *name));
AliasExport(void,glGetAttachedShaders,,(GLuint program, GLsizei maxCount, GLsizei *count, GLuint *shaders));
AliasExport(GLint,glGetAttribLocation,,(GLuint program, const GLchar *name));
AliasExport(void,glGetActiveUniform,,(GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size, GLenum *type, GLchar *name));
AliasExport(void,glGetProgramiv,,(GLuint program, GLenum pname, GLint *params));
AliasExport(GLint,glGetUniformLocation,,(GLuint program, const GLchar *name));
AliasExport(GLboolean,glIsProgram,,(GLuint program));
AliasExport(void,glLinkProgram,,(GLuint program));
AliasExport(void,glUseProgram,,(GLuint program));
AliasExport(void,glValidateProgram,,(GLuint program));
AliasExport(void,glGetProgramInfoLog,,(GLuint program, GLsizei maxLength, GLsizei *length, GLchar *infoLog));

AliasExport(void,glGetProgramBinary,,(GLuint program, GLsizei bufSize, GLsizei *length, GLenum *binaryFormat, void *binary));
AliasExport(void,glProgramBinary,,(GLuint program, GLenum binaryFormat, const void *binary, GLsizei length));


// ================ GL_ARB_vertex_shader =================
AliasExport(GLvoid,glBindAttribLocation,ARB,(GLhandleARB programObj, GLuint index, const GLcharARB *name));

AliasExport(GLvoid,glGetActiveAttrib,ARB,(GLhandleARB programObj, GLuint index, GLsizei maxLength, GLsizei *length, GLint *size, GLenum *type, GLcharARB *name));

AliasExport(GLint,glGetAttribLocation,ARB,(GLhandleARB programObj, const GLcharARB *name));

// ============== GL_ARB_shader_objects ==================
// some are not simple alias but wrappers
GLvoid APIENTRY_GL4ES gl4es_glDeleteObject(GLhandleARB obj) {
    if(!obj)
        return;
    // find type of obj (shader or program)
    program_t *glprogram = NULL;
    khint_t k_program;
    {
        khash_t(programlist) *programs = glstate->glsl->programs;
        k_program = kh_get(programlist, programs, obj);
        if (k_program != kh_end(programs))
            glprogram = kh_value(programs, k_program);
    }
    if(glprogram)
        gl4es_glDeleteProgram(obj);
    else
        gl4es_glDeleteShader(obj);
}

GLhandleARB APIENTRY_GL4ES gl4es_glGetHandle(GLenum pname) {
    FLUSH_BEGINEND;
    if(pname!=GL_PROGRAM_OBJECT_ARB) {
        errorShim(GL_INVALID_ENUM);
        return 0;
    }
    return glstate->glsl->program;
}

GLvoid APIENTRY_GL4ES gl4es_glDetachObject(GLhandleARB containerObj, GLhandleARB attachedObj) {
    gl4es_glDetachShader(containerObj, attachedObj);
}

GLhandleARB APIENTRY_GL4ES gl4es_glCreateProgramObject(GLvoid) {
    return gl4es_glCreateProgram();
}

GLvoid APIENTRY_GL4ES gl4es_glAttachObject(GLhandleARB containerObj, GLhandleARB obj) {
    gl4es_glAttachShader(containerObj, obj);
}

GLvoid APIENTRY_GL4ES gl4es_glUseProgramObject(GLhandleARB programObj) {
    gl4es_glUseProgram(programObj);
}

GLvoid APIENTRY_GL4ES gl4es_glGetObjectParameterfv(GLhandleARB obj, GLenum pname, GLfloat *params) {
    if(!obj)
        return;
    // find type of obj (shader or program)
    program_t *glprogram = NULL;
    khint_t k_program;
    {
        khash_t(programlist) *programs = glstate->glsl->programs;
        k_program = kh_get(programlist, programs, obj);
        if (k_program != kh_end(programs))
            glprogram = kh_value(programs, k_program);
    }
    // float, really?
    GLint p[4];
    if(glprogram)
        gl4es_glGetProgramiv(obj, pname, p);
    else
        gl4es_glGetShaderiv(obj, pname, p);

    params[0] = p[0];   // only 1-sized array here, right?
}
GLvoid APIENTRY_GL4ES gl4es_glGetObjectParameteriv(GLhandleARB obj, GLenum pname, GLint *params) {
    if(!obj)
        return;
    // find type of obj (shader or program)
    program_t *glprogram = NULL;
    khint_t k_program;
    {
        khash_t(programlist) *programs = glstate->glsl->programs;
        k_program = kh_get(programlist, programs, obj);
        if (k_program != kh_end(programs))
            glprogram = kh_value(programs, k_program);
    }
    if(glprogram)
        gl4es_glGetProgramiv(obj, pname, params);
    else
        gl4es_glGetShaderiv(obj, pname, params);
    // hack, some programs don't take into account that the length returned by GL_OBJECT_INFO_LOG_LENGTH_ARB exclude the '\0'.. so adding it here
    if(pname==GL_INFO_LOG_LENGTH)
        (*params)++;
}

GLvoid APIENTRY_GL4ES gl4es_glGetInfoLog(GLhandleARB obj, GLsizei maxLength, GLsizei *length, GLcharARB *infoLog) {
    if(!obj)
        return;
    // find type of obj (shader or program)
    program_t *glprogram = NULL;
    khint_t k_program;
    {
        khash_t(programlist) *programs = glstate->glsl->programs;
        k_program = kh_get(programlist, programs, obj);
        if (k_program != kh_end(programs))
            glprogram = kh_value(programs, k_program);
    }
    
    if(glprogram)
        gl4es_glGetProgramInfoLog(obj, maxLength, length, infoLog);
    else
        gl4es_glGetShaderInfoLog(obj, maxLength, length, infoLog);
}

GLvoid APIENTRY_GL4ES gl4es_glGetAttachedObjects(GLhandleARB containerObj, GLsizei maxCount, GLsizei *count, GLhandleARB *obj) {
    gl4es_glGetAttachedShaders(containerObj, maxCount, count, obj);
}

AliasExport(GLvoid,glDeleteObject,ARB,(GLhandleARB obj));
AliasExport(GLhandleARB,glGetHandle,ARB,(GLenum pname));
AliasExport(GLvoid,glDetachObject,ARB,(GLhandleARB containerObj, GLhandleARB attachedObj));
AliasExport(GLhandleARB,glCreateProgramObject,ARB,());
AliasExport(GLvoid,glAttachObject,ARB,(GLhandleARB containerObj, GLhandleARB obj));
AliasExport(GLvoid,glLinkProgram,ARB,(GLhandleARB programObj));
AliasExport(GLvoid,glUseProgramObject,ARB,(GLhandleARB programObj));
AliasExport(GLvoid,glValidateProgram,ARB,(GLhandleARB programObj));
AliasExport(GLvoid,glGetObjectParameterfv,ARB,(GLhandleARB obj, GLenum pname, GLfloat *params));
AliasExport(GLvoid,glGetObjectParameteriv,ARB,(GLhandleARB obj, GLenum pname, GLint *params));
AliasExport(GLvoid,glGetInfoLog,ARB,(GLhandleARB obj, GLsizei maxLength, GLsizei *length, GLcharARB *infoLog));
AliasExport(GLvoid,glGetAttachedObjects,ARB,(GLhandleARB containerObj, GLsizei maxCount, GLsizei *count, GLhandleARB *obj));
AliasExport(GLint,glGetUniformLocation,ARB,(GLhandleARB programObj, const GLcharARB *name));
AliasExport(GLvoid,glGetActiveUniform,ARB,(GLhandleARB programObj, GLuint index, GLsizei maxLength, GLsizei *length, GLint *size, GLenum *type, GLcharARB *name));
