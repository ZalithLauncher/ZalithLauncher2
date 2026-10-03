#ifndef _GL4ES_PROGRAM_H_
#define _GL4ES_PROGRAM_H_

#include "gles.h"
#include "buffers.h"
#include "shader.h"
#include "uniform.h"

typedef struct {
    GLuint      index;
    GLint       real_index;
    GLenum      type;
    int         size;
    char*       name;
    const char* glname;
} attribloc_t;
KHASH_MAP_DECLARE_INT(attribloclist, attribloc_t *);

#ifndef _GL4ES_OLD_PROGRAM_H_
typedef struct shader_s             shader_t;
#endif
//typedef struct shaderconv_need_s    shaderconv_need_t;

typedef enum {
    MAT_MV = 0,
    MAT_MV_I,
    MAT_MV_T,
    MAT_MV_IT,
    MAT_P,
    MAT_P_I,
    MAT_P_T,
    MAT_P_IT,
    MAT_MVP,
    MAT_MVP_I,
    MAT_MVP_T,
    MAT_MVP_IT,
    MAT_T0,
    MAT_T0_I,
    MAT_T0_T,
    MAT_T0_IT,
    MAT_T1,
    MAT_T1_I,
    MAT_T1_T,
    MAT_T1_IT,
    MAT_T2,
    MAT_T2_I,
    MAT_T2_T,
    MAT_T2_IT,
    MAT_T3,
    MAT_T3_I,
    MAT_T3_T,
    MAT_T3_IT,
    MAT_T4,
    MAT_T4_I,
    MAT_T4_T,
    MAT_T4_IT,
    MAT_T5,
    MAT_T5_I,
    MAT_T5_T,
    MAT_T5_IT,
    MAT_T6,
    MAT_T6_I,
    MAT_T6_T,
    MAT_T6_IT,
    MAT_T7,
    MAT_T7_I,
    MAT_T7_T,
    MAT_T7_IT,
    MAT_T8,
    MAT_T8_I,
    MAT_T8_T,
    MAT_T8_IT,
    MAT_T9,
    MAT_T9_I,
    MAT_T9_T,
    MAT_T9_IT,
    MAT_T10,
    MAT_T10_I,
    MAT_T10_T,
    MAT_T10_IT,
    MAT_T11,
    MAT_T11_I,
    MAT_T11_T,
    MAT_T11_IT,
    MAT_T12,
    MAT_T12_I,
    MAT_T12_T,
    MAT_T12_IT,
    MAT_T13,
    MAT_T13_I,
    MAT_T13_T,
    MAT_T13_IT,
    MAT_T14,
    MAT_T14_I,
    MAT_T14_T,
    MAT_T14_IT,
    MAT_T15,
    MAT_T15_I,
    MAT_T15_T,
    MAT_T15_IT,
    MAT_N,
    MAT_MAX
} reserved_matrix_t;

typedef struct {
    GLuint          internal_id; // internal id of the uniform
    GLuint          id;     // glsl id of the uniform
    GLenum          type;   // type of the uniform (GL_VERTEX or GL_FRAGMENT)
    GLint           size;
    char*           name; // original source of the uniform
    int             builtin;
    uintptr_t       cache_offs;
    int             cache_size; // this is GLsizeof(type)*size
    uintptr_t       parent_offs;    // in case the uniform is from a fpe custom program
    int             parent_size;    // 0 means not found in parent... like for builtin
} uniform_t;

KHASH_MAP_DECLARE_INT(uniformlist, uniform_t *);

typedef struct {
    void*           cache;  // buffer of the uniform size
    int             cap;    // capacity of the cache
    int             size;   // next available free space in the cache
} uniformcache_t;

typedef struct {
    int         has;
    GLint       ambient; //vec4
    GLint       diffuse; //vec4
    GLint       specular; //vec4
    GLint       position; //vec4
    GLint       halfVector; //vec4
    GLint       spotDirection; //vec3
    GLint       spotExponent; //float
    GLint       spotCutoff; //float
    GLint       spotCosCutoff; //float
    GLfloat     oldspotCutoff;  // cache to avoid recalc of spotCutoff
    GLfloat     oldspotCosCutoff; //float
    GLint       constantAttenuation; //float
    GLint       linearAttenuation; //float
    GLint       quadraticAttenuation; //float
} builtin_lightsource_t;

typedef struct {
    GLint       ambient;
} builtin_lightmodel_t;
   

typedef struct {
    int         has;
    GLint       emission; //vec4
    GLint       ambient; //vec4
    GLint       diffuse; //vec4
    GLint       specular; //vec4
    GLint       shininess; //float
    GLint       alpha;  // float, fpe only, derived: diffuse[3]
} builtin_material_t;

typedef struct {
    GLint       sceneColor; //vec4
} builtin_lightmodelproducts_t;

typedef struct {
    int         has;
    GLint       ambient; //vec4
    GLint       diffuse; //vec4
    GLint       specular; //vec4
} builtin_lightproducts_t;

typedef struct {
    int         has;
    GLint       size;
    GLint       sizeMin;
    GLint       sizeMax;
    GLint       fadeThresholdSize;
    GLint       distanceConstantAttenuation;
    GLint       distanceLinearAttenuation;
    GLint       distanceQuadraticAttenuation;
} builtin_pointsprite_t;

typedef struct {
    int         has;
    GLint       color;
    GLint       density;
    GLint       start;
    GLint       end;
    GLint       scale;
} builtin_fog_t;

// this need to be as texture_enabled_t, but with 0 as nothing
typedef enum {
    TU_NONE = 0,
    TU_TEX1D,
    TU_TEX2D,
    TU_TEX3D,
    TU_RECTANGLE,
    TU_CUBE
} texunit_type;

typedef struct {
    GLint           id;
    texunit_type    type;
    int             req_tu; //requested TU
    int             act_tu; // actual (can be different from req)
} texunit_t;

typedef struct {
    GLuint          id;     // internal id of the shader
    int             linked;
    int             validated;
    GLenum          valid_result;
    int             attach_cap;
    int             attach_size;
    GLuint          *attach;
    struct shader_s        *last_vert;
    struct shader_s        *last_frag;
    int             default_vertex;
    int             default_fragment;
    shaderconv_need_t *default_need;    // filled only if default_vertex or default_fragment is used
    int             va_size[MAX_VATTRIB];
    khash_t(attribloclist)     *attribloc;
    khash_t(uniformlist) *uniform;
    int             num_uniform;
    uniformcache_t  cache;
    // builtin attrib
    int                             has_builtin_attrib;
    GLint                           builtin_attrib[ATT_MAX];
    // builtin uniform
    int                             has_builtin_matrix;
    GLint                           builtin_matrix[MAT_MAX];
    int                             has_builtin_light;
    builtin_lightsource_t           builtin_lights[MAX_LIGHT];
    builtin_lightmodel_t            builtin_lightmodel;
    builtin_material_t              builtin_material[2];
    builtin_lightmodelproducts_t    builtin_lightmodelprod[2];
    builtin_lightproducts_t         builtin_lightprod[2][MAX_LIGHT];
    GLint                           builtin_normalrescale;
    GLint                           builtin_clipplanes[MAX_CLIP_PLANES];
    int                             has_builtin_clipplanes;
    builtin_pointsprite_t           builtin_pointsprite;
    GLint                           builtin_texenvcolor[MAX_TEX];
    int                             has_builtin_texenv;
    GLint                           builtin_eye[4][MAX_TEX];
    GLint                           builtin_obj[4][MAX_TEX];
    int                             has_builtin_texgen;
    builtin_fog_t                   builtin_fog;
    GLint                           builtin_instanceID;
    // fpe uniform
    GLint                           fpe_alpharef;
    int                             has_fpe;
    GLint                           builtin_texsampler[MAX_TEX];
    int                             has_builtin_texsampler;
    GLint                           builtin_texenvrgbscale[MAX_TEX];
    GLint                           builtin_texenvalphascale[MAX_TEX];
    GLint                           builtin_texadjust[MAX_TEX];
    int                             has_builtin_texadjust;
    texunit_t                       texunits[MAX_TEX];
    int                             has_builtin_blendcolor;
    GLint                           builtin_blendcolor;
    // ARB_shaders (oldprograms) uniforms
    int                             has_vtx_progenv;
    GLint                           vtx_progenv[MAX_VTX_PROG_ENV_PARAMS];
    int                             has_vtx_progloc;
    GLint                           vtx_progloc[MAX_VTX_PROG_LOC_PARAMS];
    int                             has_frg_progenv;
    GLint                           frg_progenv[MAX_FRG_PROG_ENV_PARAMS];
    int                             has_frg_progloc;
    GLint                           frg_progloc[MAX_FRG_PROG_LOC_PARAMS];
    int                             has_samplers1d;
    GLint                           samplers1d[MAX_TEX];
    int                             has_samplers2d;
    GLint                           samplers2d[MAX_TEX];
    int                             has_samplers3d;
    GLint                           samplers3d[MAX_TEX];
    int                             has_samplersCube;
    GLint                           samplersCube[MAX_TEX];
    // that will be an fpe_cache_t*
    void*                           fpe_cache;
} program_t;

KHASH_MAP_DECLARE_INT(programlist, program_t *);

void deleteProgram(program_t *glprogram, khint_t k_program);

void APIENTRY_GL4ES gl4es_glAttachShader(GLuint program, GLuint shader);
void APIENTRY_GL4ES gl4es_glBindAttribLocation(GLuint program, GLuint index, const GLchar *name);
GLuint APIENTRY_GL4ES gl4es_glCreateProgram(void);
void APIENTRY_GL4ES gl4es_glDeleteProgram(GLuint program);
void APIENTRY_GL4ES gl4es_glDetachShader(GLuint program, GLuint shader);
void APIENTRY_GL4ES gl4es_glGetActiveAttrib(GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size, GLenum *type, GLchar *name);
void APIENTRY_GL4ES gl4es_glGetActiveUniform(GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size, GLenum *type, GLchar *name);
void APIENTRY_GL4ES gl4es_glGetAttachedShaders(GLuint program, GLsizei maxCount, GLsizei *count, GLuint *shaders);
GLint APIENTRY_GL4ES gl4es_glGetAttribLocation(GLuint program, const GLchar *name);
void APIENTRY_GL4ES gl4es_glGetProgramInfoLog(GLuint program, GLsizei maxLength, GLsizei *length, GLchar *infoLog);
void APIENTRY_GL4ES gl4es_glGetProgramiv(GLuint program, GLenum pname, GLint *params);
GLint APIENTRY_GL4ES gl4es_glGetUniformLocation(GLuint program, const GLchar *name);
GLboolean APIENTRY_GL4ES gl4es_glIsProgram(GLuint program);
void APIENTRY_GL4ES gl4es_glLinkProgram(GLuint program);
void APIENTRY_GL4ES gl4es_glUseProgram(GLuint program);
void APIENTRY_GL4ES gl4es_glValidateProgram(GLuint program);

int gl4es_useProgramBinary(GLuint program, int length, GLenum format, const void* binary);    // internal
int gl4es_getProgramBinary(GLuint program, int *length, GLenum *format, void** binary);    // internal

#define CHECK_PROGRAM(type, program) \
    if(!program) { \
        noerrorShim(); \
        return (type)0; \
    } \
    program_t *glprogram = NULL; \
    khint_t k_##program; \
    { \
        khash_t(programlist) *programs = glstate->glsl->programs; \
        k_##program = kh_get(programlist, programs, program); \
        if (k_##program != kh_end(programs)) \
            glprogram = kh_value(programs, k_##program); \
    } \
    if(!glprogram) { \
        errorShim(GL_INVALID_OPERATION); \
        return (type)0; \
    }

#define APPLY_PROGRAM(prg, glprg) \
    if(glstate->gleshard->program != prg) {  \
        glstate->gleshard->program = prg;    \
        glstate->gleshard->glprogram = glprg;\
        LOAD_GLES2(glUseProgram);           \
        if(gles_glUseProgram)               \
            gles_glUseProgram(prg);         \
    }

void GoUniformfv(program_t *glprogram, GLint location, int size, int count, const GLfloat *value);
void GoUniformiv(program_t *glprogram, GLint location, int size, int count, const GLint *value);
void GoUniformMatrix2fv(program_t *glprogram, GLint location, GLsizei count, GLboolean transpose, const GLfloat *value);
void GoUniformMatrix3fv(program_t *glprogram, GLint location, GLsizei count, GLboolean transpose, const GLfloat *value);
void GoUniformMatrix4fv(program_t *glprogram, GLint location, GLsizei count, GLboolean transpose, const GLfloat *value);
int GetUniformi(program_t *glprogram, GLint location);
const char* GetUniformName(program_t *glprogram, GLint location);

GLvoid APIENTRY_GL4ES glBindAttribLocationARB(GLhandleARB programObj, GLuint index, const GLcharARB *name);
GLvoid APIENTRY_GL4ES glGetActiveAttribARB(GLhandleARB programObj, GLuint index, GLsizei maxLength, GLsizei *length, GLint *size, GLenum *type, GLcharARB *name);
GLint APIENTRY_GL4ES glGetAttribLocationARB(GLhandleARB programObj, const GLcharARB *name);

// ============== GL_ARB_shader_objects ==================
GLvoid APIENTRY_GL4ES gl4es_glDeleteObject(GLhandleARB obj);
GLhandleARB APIENTRY_GL4ES gl4es_glGetHandle(GLenum pname);
GLvoid APIENTRY_GL4ES gl4es_glDetachObject(GLhandleARB containerObj, GLhandleARB attachedObj);
GLhandleARB APIENTRY_GL4ES gl4es_glCreateProgramObject(GLvoid);
GLvoid APIENTRY_GL4ES gl4es_glAttachObject(GLhandleARB containerObj, GLhandleARB obj);
GLvoid APIENTRY_GL4ES gl4es_glUseProgramObject(GLhandleARB programObj);
GLvoid APIENTRY_GL4ES gl4es_glGetObjectParameterfv(GLhandleARB obj, GLenum pname, GLfloat *params);
GLvoid APIENTRY_GL4ES gl4es_glGetObjectParameteriv(GLhandleARB obj, GLenum pname, GLint *params);
GLvoid APIENTRY_GL4ES gl4es_glGetInfoLog(GLhandleARB obj, GLsizei maxLength, GLsizei *length, GLcharARB *infoLog);
GLvoid APIENTRY_GL4ES gl4es_glGetAttachedObjects(GLhandleARB containerObj, GLsizei maxCount, GLsizei *count, GLhandleARB *obj);

GLvoid APIENTRY_GL4ES glDeleteObjectARB(GLhandleARB obj);
GLhandleARB APIENTRY_GL4ES glGetHandleARB(GLenum pname);
GLvoid APIENTRY_GL4ES glDetachObjectARB(GLhandleARB containerObj, GLhandleARB attachedObj);
GLhandleARB APIENTRY_GL4ES glCreateShaderObjectARB(GLenum shaderType);
GLvoid APIENTRY_GL4ES glShaderSourceARB(GLhandleARB shaderObj, GLsizei count, const GLcharARB **string, const GLint *length);
GLvoid APIENTRY_GL4ES glCompileShaderARB(GLhandleARB shaderObj);
GLhandleARB APIENTRY_GL4ES glCreateProgramObjectARB(GLvoid);
GLvoid APIENTRY_GL4ES glAttachObjectARB(GLhandleARB containerObj, GLhandleARB obj);
GLvoid APIENTRY_GL4ES glLinkProgramARB(GLhandleARB programObj);
GLvoid APIENTRY_GL4ES glUseProgramObjectARB(GLhandleARB programObj);
GLvoid APIENTRY_GL4ES glValidateProgramARB(GLhandleARB programObj);
GLvoid APIENTRY_GL4ES glGetObjectParameterfvARB(GLhandleARB obj, GLenum pname, GLfloat *params);
GLvoid APIENTRY_GL4ES glGetObjectParameterivARB(GLhandleARB obj, GLenum pname, GLint *params);
GLvoid APIENTRY_GL4ES glGetInfoLogARB(GLhandleARB obj, GLsizei maxLength, GLsizei *length, GLcharARB *infoLog);
GLvoid APIENTRY_GL4ES glGetAttachedObjectsARB(GLhandleARB containerObj, GLsizei maxCount, GLsizei *count, GLhandleARB *obj);
GLint APIENTRY_GL4ES glGetUniformLocationARB(GLhandleARB programObj, const GLcharARB *name);
GLvoid APIENTRY_GL4ES glGetActiveUniformARB(GLhandleARB programObj, GLuint index, GLsizei maxLength, GLsizei *length, GLint *size, GLenum *type, GLcharARB *name);
GLvoid APIENTRY_GL4ES glGetUniformfvARB(GLhandleARB programObj, GLint location, GLfloat *params);
GLvoid APIENTRY_GL4ES glGetUniformivARB(GLhandleARB programObj, GLint location, GLint *params);
GLvoid APIENTRY_GL4ES glGetShaderSourceARB(GLhandleARB obj, GLsizei maxLength, GLsizei *length, GLcharARB *source);

#endif // _GL4ES_PROGRAM_H_
