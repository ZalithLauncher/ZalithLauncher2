#ifndef _GL4ES_BUFFERS_H_
#define _GL4ES_BUFFERS_H_

#include "khash.h"
#include "../config.h"
#include "gles.h"

// VBO *****************
typedef struct {
    GLuint      buffer;
    GLuint      real_buffer;
    GLenum      type;
    GLsizeiptr  size;
    GLenum      usage;
    GLint		access;
    int			mapped;
    int         ranged;
    GLintptr    offset;
    GLsizeiptr  length;
    GLvoid     *data;
} glbuffer_t;

KHASH_MAP_DECLARE_INT(buff, glbuffer_t *);

void APIENTRY_GL4ES gl4es_glGenBuffers(GLsizei n, GLuint * buffers);
void APIENTRY_GL4ES gl4es_glBindBuffer(GLenum target, GLuint buffer);
void APIENTRY_GL4ES gl4es_glBufferData(GLenum target, GLsizeiptr size, const GLvoid * data, GLenum usage);
void APIENTRY_GL4ES gl4es_glBufferSubData(GLenum target, GLintptr offset, GLsizeiptr size, const GLvoid * data);
void APIENTRY_GL4ES gl4es_glDeleteBuffers(GLsizei n, const GLuint * buffers);
GLboolean APIENTRY_GL4ES gl4es_glIsBuffer(GLuint buffer);
void APIENTRY_GL4ES gl4es_glGetBufferParameteriv(GLenum target, GLenum value, GLint * data);

void* APIENTRY_GL4ES gl4es_glMapBuffer(GLenum target, GLenum access);
GLboolean APIENTRY_GL4ES gl4es_glUnmapBuffer(GLenum target);
void APIENTRY_GL4ES gl4es_glGetBufferPointerv(GLenum target, GLenum pname, GLvoid ** params);
void APIENTRY_GL4ES gl4es_glGetBufferSubData(GLenum target, GLintptr offset, GLsizeiptr size, GLvoid * data);

void* APIENTRY_GLES gl4es_glMapBufferRange(GLenum target, GLintptr offset, GLsizeiptr length, GLbitfield access);
void APIENTRY_GL4ES gl4es_glFlushMappedBufferRange(GLenum target, GLintptr offset, GLsizeiptr length);

void APIENTRY_GL4ES gl4es_glCopyBufferSubData(GLenum readTarget, GLenum writeTarget, GLintptr readOffset, GLintptr writeOffset, GLsizeiptr size);

void APIENTRY_GL4ES glGenBuffers(GLsizei n, GLuint * buffers);
void APIENTRY_GL4ES glBindBuffer(GLenum target, GLuint buffer);
void APIENTRY_GL4ES glBufferData(GLenum target, GLsizeiptr size, const GLvoid * data, GLenum usage);
void APIENTRY_GL4ES glBufferSubData(GLenum target, GLintptr offset, GLsizeiptr size, const GLvoid * data);
void APIENTRY_GL4ES glDeleteBuffers(GLsizei n, const GLuint * buffers);
GLboolean APIENTRY_GL4ES glIsBuffer(GLuint buffer);
void APIENTRY_GL4ES glGetBufferParameteriv(GLenum target, GLenum value, GLint * data);
void* APIENTRY_GL4ES glMapBuffer(GLenum target, GLenum access);
GLboolean APIENTRY_GL4ES glUnmapBuffer(GLenum target);
void APIENTRY_GL4ES glGetBufferPointerv(GLenum target, GLenum pname, GLvoid ** params);
void APIENTRY_GL4ES glGetBufferSubData(GLenum target, GLintptr offset, GLsizeiptr size, GLvoid * data);

void* APIENTRY_GL4ES glMapBufferRange(GLenum target, GLintptr offset, GLsizeiptr length, GLbitfield access);
void APIENTRY_GL4ES glFlushMappedBufferRange(GLenum target, GLintptr offset, GLsizeiptr length);

void APIENTRY_GL4ES glGenBuffersARB(GLsizei n, GLuint * buffers);
void APIENTRY_GL4ES glBindBufferARB(GLenum target, GLuint buffer);
void APIENTRY_GL4ES glBufferDataARB(GLenum target, GLsizeiptr size, const GLvoid * data, GLenum usage);
void APIENTRY_GL4ES glBufferSubDataARB(GLenum target, GLintptr offset, GLsizeiptr size, const GLvoid * data);
void APIENTRY_GL4ES glDeleteBuffersARB(GLsizei n, const GLuint * buffers);
GLboolean APIENTRY_GL4ES glIsBufferARB(GLuint buffer);
void APIENTRY_GL4ES glGetBufferParameterivARB(GLenum target, GLenum value, GLint * data);
void* APIENTRY_GL4ES glMapBufferARB(GLenum target, GLenum access);
GLboolean APIENTRY_GL4ES glUnmapBufferARB(GLenum target);
void APIENTRY_GL4ES glGetBufferSubDataARB(GLenum target, GLintptr offset, GLsizeiptr size, GLvoid * data);
void APIENTRY_GL4ES glGetBufferPointervARB(GLenum target, GLenum pname, GLvoid ** params);

// internal actual BindBuffer with cache
void bindBuffer(GLenum target, GLuint buffer);
// delete a single buffer (and erase it from the cache)
void deleteSingleBuffer(GLuint buffer);
// unbound all buffer
void unboundBuffers();
// update wanted Index Buffer
GLuint wantBufferIndex(GLuint buffer);
// Bind the wanted index buffer if needed
void realize_bufferIndex();


// Pointer..... ****** => map them in vertexattrib (even with GLES1.1). So no more pointer_state_t, use vertexattrib_t
// and map .enabled to .vaarray

// This map correspond to ARB_vertex_program map
typedef enum {
    ARB_VERTEX = 0,
    //ARB_WEIGHT
    ARB_NORMAL = 2,
    ARB_COLOR,
    ARB_SECONDARY,
    ARB_FOGCOORD,
    // 6 and 7 are nothing specifics
    ARB_MULTITEXCOORD0 = 8,
    ARB_MULTITEXCOORD1,
    ARB_MULTITEXCOORD2,
    ARB_MULTITEXCOORD3,
    ARB_MULTITEXCOORD4,
    ARB_MULTITEXCOORD5,
    ARB_MULTITEXCOORD6,
    ARB_MULTITEXCOORD7,
    ARB_MULTITEXCOORD8,
    ARB_MULTITEXCOORD9,
    ARB_MULTITEXCOORD10,
    ARB_MULTITEXCOORD11,
    ARB_MULTITEXCOORD12,
    ARB_MULTITEXCOORD13,
    ARB_MULTITEXCOORD14,
    ARB_MULTITEXCOORD15,
    //ARB_POINTSIZE,   //this one is supported by GLES hardware
    ARB_MAX
} reserved_attrib_t;

// This map correspond tries to fit as much as possible in 8 VA
typedef enum {
    COMP_VERTEX = 0,
    COMP_FOGCOORD,
    COMP_NORMAL,
    COMP_COLOR,
    COMP_SECONDARY,
    COMP_MULTITEXCOORD0,
    COMP_MULTITEXCOORD1,
    COMP_MULTITEXCOORD2,
    COMP_MULTITEXCOORD3,
    COMP_MULTITEXCOORD4,
    COMP_MULTITEXCOORD5,
    COMP_MULTITEXCOORD6,
    COMP_MULTITEXCOORD7,
    COMP_MULTITEXCOORD8,
    COMP_MULTITEXCOORD9,
    COMP_MULTITEXCOORD10,
    COMP_MULTITEXCOORD11,
    COMP_MULTITEXCOORD12,
    COMP_MULTITEXCOORD13,
    COMP_MULTITEXCOORD14,
    COMP_MULTITEXCOORD15,
    //COMP_WEIGHT
    //COMP_POINTSIZE,   //this one is supported by GLES hardware
    COMP_MAX
} compressed_attrib_t;

#define ATT_VERTEX ((hardext.maxvattrib>8)?ARB_VERTEX:COMP_VERTEX)
#define ATT_NORMAL ((hardext.maxvattrib>8)?ARB_NORMAL:COMP_NORMAL)
#define ATT_COLOR ((hardext.maxvattrib>8)?ARB_COLOR:COMP_COLOR)
#define ATT_SECONDARY ((hardext.maxvattrib>8)?ARB_SECONDARY:COMP_SECONDARY)
#define ATT_FOGCOORD ((hardext.maxvattrib>8)?ARB_FOGCOORD:COMP_FOGCOORD)
#define ATT_MULTITEXCOORD0 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD0:COMP_MULTITEXCOORD0)
#define ATT_MULTITEXCOORD1 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD1:COMP_MULTITEXCOORD1)
#define ATT_MULTITEXCOORD2 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD2:COMP_MULTITEXCOORD2)
#define ATT_MULTITEXCOORD3 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD3:COMP_MULTITEXCOORD3)
#define ATT_MULTITEXCOORD4 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD4:COMP_MULTITEXCOORD4)
#define ATT_MULTITEXCOORD5 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD5:COMP_MULTITEXCOORD5)
#define ATT_MULTITEXCOORD6 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD6:COMP_MULTITEXCOORD6)
#define ATT_MULTITEXCOORD7 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD7:COMP_MULTITEXCOORD7)
#define ATT_MULTITEXCOORD8 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD8:COMP_MULTITEXCOORD8)
#define ATT_MULTITEXCOORD9 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD9:COMP_MULTITEXCOORD9)
#define ATT_MULTITEXCOORD10 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD10:COMP_MULTITEXCOORD10)
#define ATT_MULTITEXCOORD11 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD11:COMP_MULTITEXCOORD11)
#define ATT_MULTITEXCOORD12 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD12:COMP_MULTITEXCOORD12)
#define ATT_MULTITEXCOORD13 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD13:COMP_MULTITEXCOORD13)
#define ATT_MULTITEXCOORD14 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD14:COMP_MULTITEXCOORD14)
#define ATT_MULTITEXCOORD15 ((hardext.maxvattrib>8)?ARB_MULTITEXCOORD15:COMP_MULTITEXCOORD15)
#define ATT_MAX ARB_MAX

#define NB_VA (ATT_MAX)

// glLockArrays should lock all arrays, but Quake3 do change values on Color and Textures UV, so limit Lock to 3D" Coords arrays
#define NB_LOCKVA (ATT_NORMAL+1)

// Vertex Attrib.. ***
typedef struct {
    GLint           size;
    GLenum          type;
    GLsizei         stride;
    const GLvoid*   pointer;
    int             enabled;
    int             normalized;
    glbuffer_t      *buffer;    // reference buffer
    GLint           divisor;
    GLuint          real_buffer;    // If there is a real VBO binded
    const GLvoid*   real_pointer;   // the pointer related to real VBO
    int             integer;
} vertexattrib_t;

typedef struct {
    GLfloat        *ptr;
    vertexattrib_t state;
    int            enabled;
} pointer_cache_t;

// VAO ****************
typedef struct {
    GLuint           array;
    // buffer state
    glbuffer_t *vertex;
    glbuffer_t *elements;
    glbuffer_t *pack;
    glbuffer_t *unpack;
    // locked arrays
    int       locked;
    GLsizei   count;
    GLint     first;
    int       locked_mapped[NB_VA];

    int maxtex; // upper limit enabled tex_coord
    // VAO optimisation: keep a shared copy of the digested datas (unless the vao is the default one)
    int *shared_arrays;
    pointer_cache_t vert;
    pointer_cache_t normal;
    pointer_cache_t color;
    pointer_cache_t secondary;
    pointer_cache_t fog;
    pointer_cache_t tex[MAX_TEX];
    int cache_count;
    // Vertex Attrib
    vertexattrib_t  vertexattrib[MAX_VATTRIB];
    // TODO: Cache VA also?
} glvao_t;

void VaoSharedClear(glvao_t *vao);
void VaoInit(glvao_t *vao);

KHASH_MAP_DECLARE_INT(glvao, glvao_t*);

void APIENTRY_GL4ES gl4es_glGenVertexArrays(GLsizei n, GLuint *arrays);
void APIENTRY_GL4ES gl4es_glBindVertexArray(GLuint array);
void APIENTRY_GL4ES gl4es_glDeleteVertexArrays(GLsizei n, const GLuint *arrays);
GLboolean APIENTRY_GL4ES gl4es_glIsVertexArray(GLuint array);

void APIENTRY_GL4ES glGenVertexArrays(GLsizei n, GLuint *arrays);
void APIENTRY_GL4ES glBindVertexArray(GLuint array);
void APIENTRY_GL4ES glDeleteVertexArrays(GLsizei n, const GLuint *arrays);
GLboolean APIENTRY_GL4ES glIsVertexArray(GLuint array);

void APIENTRY_GL4ES gl4es_glNamedBufferData(GLuint buffer, GLsizeiptr size, const GLvoid * data, GLenum usage);
void APIENTRY_GL4ES gl4es_glNamedBufferSubData(GLuint buffer, GLintptr offset, GLsizeiptr size, const GLvoid * data);
void APIENTRY_GL4ES gl4es_glGetNamedBufferParameteriv(GLuint buffer, GLenum value, GLint * data);
void* APIENTRY_GL4ES gl4es_glMapNamedBuffer(GLuint buffer, GLenum access);
GLboolean APIENTRY_GL4ES gl4es_glUnmapNamedBuffer(GLuint buffer);
void APIENTRY_GL4ES gl4es_glGetNamedBufferSubData(GLuint buffer, GLintptr offset, GLsizeiptr size, GLvoid * data);
void APIENTRY_GL4ES gl4es_glGetNamedBufferPointerv(GLuint buffer, GLenum pname, GLvoid ** params);

#endif // _GL4ES_BUFFERS_H_
