#ifndef _GL4ES_FPE_H_
#define _GL4ES_FPE_H_

/*
  This is the FPE : Fixed Pipeline Emulator

  It task is to create a shader that emulate the fixed pipeline.

  So based on the current GL State, it will either take a shader from the cache or create a new one
  For conveniance, the relevant GL states element are condenced in a bitfield packed fpe_state_t structure

*/

#include "gles.h"
#include "program.h"

#define FPE_FOG_EXP    0
#define FPE_FOG_EXP2   1
#define FPE_FOG_LINEAR 2

#define FPE_FOG_SRC_DEPTH 0
#define FPE_FOG_SRC_COORD 1

#define FPE_FOG_DIST_PLANE_ABS  0
#define FPE_FOG_DIST_PLANE      1
#define FPE_FOG_DIST_RADIAL     2

#define FPE_TEX_OFF  0
#define FPE_TEX_2D   1
#define FPE_TEX_RECT 2
#define FPE_TEX_3D   3
#define FPE_TEX_CUBE 4
#define FPE_TEX_STRM 5

#define FPE_ALWAYS   0
#define FPE_NEVER    1
#define FPE_LESS     2
#define FPE_EQUAL    3
#define FPE_LEQUAL   4
#define FPE_GREATER  5
#define FPE_NOTEQUAL 6
#define FPE_GEQUAL   7

#define FPE_CM_AMBIENTDIFFUSE 0
#define FPE_CM_EMISSION       1
#define FPE_CM_AMBIENT        2
#define FPE_CM_DIFFUSE        3
#define FPE_CM_SPECULAR       4

#define FPE_MODULATE          0
#define FPE_ADD               1
#define FPE_DECAL             2
#define FPE_BLEND             3
#define FPE_REPLACE           4
#define FPE_COMBINE           5
#define FPE_COMBINE4          6

#define FPE_CR_REPLACE        0
#define FPE_CR_MODULATE       1
#define FPE_CR_ADD            2
#define FPE_CR_ADD_SIGNED     3
#define FPE_CR_INTERPOLATE    4
#define FPE_CR_SUBTRACT       5
#define FPE_CR_DOT3_RGB       6
#define FPE_CR_DOT3_RGBA      7
#define FPE_CR_MOD_ADD        8
#define FPE_CR_MOD_ADD_SIGNED 9
#define FPE_CR_MOD_SUB        10

#define FPE_SRC_TEXTURE        0
#define FPE_SRC_TEXTURE0       1
#define FPE_SRC_TEXTURE1       2
#define FPE_SRC_TEXTURE2       3
#define FPE_SRC_TEXTURE3       4
#define FPE_SRC_TEXTURE4       5
#define FPE_SRC_TEXTURE5       6
#define FPE_SRC_TEXTURE6       7
#define FPE_SRC_TEXTURE7       8
#define FPE_SRC_TEXTURE8       9
#define FPE_SRC_TEXTURE9       10
#define FPE_SRC_TEXTURE10      11
#define FPE_SRC_TEXTURE11      12
#define FPE_SRC_TEXTURE12      13
#define FPE_SRC_TEXTURE13      14
#define FPE_SRC_TEXTURE14      15
#define FPE_SRC_TEXTURE15      16
#define FPE_SRC_CONSTANT       17
#define FPE_SRC_PRIMARY_COLOR  18
#define FPE_SRC_PREVIOUS       19
#define FPE_SRC_ZERO           20
#define FPE_SRC_ONE            21
#define FPE_SRC_SECONDARY_COLOR 22

#define FPE_OP_ALPHA           0
#define FPE_OP_MINUSALPHA      1
#define FPE_OP_SRCCOLOR        2
#define FPE_OP_MINUSCOLOR      3

#define FPE_TEX_RGBA           0
#define FPE_TEX_RGB            1
#define FPE_TEX_INTENSITY      2
#define FPE_TEX_LUM_ALPHA      3
#define FPE_TEX_LUM            4
#define FPE_TEX_ALPHA          5
#define FPE_TEX_DEPTH          6

#define FPE_TG_EYELINEAR       0
#define FPE_TG_OBJLINEAR       1
#define FPE_TG_SPHEREMAP       2
#define FPE_TG_NORMALMAP       3
#define FPE_TG_REFLECMAP       4
#define FPE_TG_NONE            5  // dummy, to help fpe

#define FPE_BLEND_ZERO                      0
#define FPE_BLEND_ONE                       1
#define FPE_BLEND_SRC_COLOR                 2
#define FPE_BLEND_ONE_MINUS_SRC_COLOR       3
#define FPE_BLEND_DST_COLOR                 4
#define FPE_BLEND_ONE_MINUS_DST_COLOR       5
#define FPE_BLEND_SRC_ALPHA                 6
#define FPE_BLEND_ONE_MINUS_SRC_ALPHA       7
#define FPE_BLEND_DST_ALPHA                 8
#define FPE_BLEND_ONE_MINUS_DST_ALPHA       9
#define FPE_BLEND_CONSTANT_COLOR            10
#define FPE_BLEND_ONE_MINUS_CONSTANT_COLOR  11
#define FPE_BLEND_CONSTANT_ALPHA            12
#define FPE_BLEND_ONE_MINUS_CONSTANT_ALPHA  13
#define FPE_BLEND_SRC_ALPHA_SATURATE        14

#define FPE_BLENDEQ_FUNC_ADD                0
#define FPE_BLENDEQ_FUNC_SUBTRACT           1
#define FPE_BLENDEQ_FUNC_REVERSE_SUBTRACT   2
#define FPE_BLENDEQ_MIN                     3
#define FPE_BLENDEQ_MAX                     4

typedef struct fpe_texgen_s {
  unsigned int texgen_s:1;              // texgen S enabled on 1 bit
  unsigned int texgen_s_mode:3;         // texgen S on 3 bits
  unsigned int texgen_t:1;              // texgen T enabled on 1 bit
  unsigned int texgen_t_mode:3;         // texgen T on 3 bits
  unsigned int texgen_r:1;              // texgen R enabled on 1 bit
  unsigned int texgen_r_mode:3;         // texgen R on 3 bits
  unsigned int texgen_q:1;              // texgen Q enabled on 1 bit
  unsigned int texgen_q_mode:3;         // texgen Q on 3 bits
} fpe_texgen_t;

typedef struct fpe_texture_s {
  unsigned int texmat:1;                // flags if texture matrix is not identity
  unsigned int textype:3;               // textures type stored on 3 bits
  unsigned int texadjust:1;             // flags if texture need adjustement
  unsigned int texformat:3;             // textures (simplified) internal format on 3 bits
} fpe_texture_t;

typedef struct fpe_texenv_s {
  unsigned int texenv:4;                // texenv flag
  unsigned int texoprgb0:2;             // texenv src op (OPERATION_n_RGB is 2 bits)
  unsigned int texoprgb1:2;             // texenv src op (OPERATION_n_RGB is 2 bits)
  unsigned int texoprgb2:2;             // texenv src op (OPERATION_n_RGB is 2 bits)
  unsigned int texoprgb3:2;             // texenv src op (OPERATION_n_RGB is 2 bits)
  unsigned int texopalpha0:1;           // texenv src op (OPERATION_n_ALPHA is 1 bits)
  unsigned int texopalpha1:1;           // texenv src op (OPERATION_n_ALPHA is 1 bits)
  unsigned int texopalpha2:1;           // texenv src op (OPERATION_n_ALPHA is 1 bits)
  unsigned int texopalpha3:1;           // texenv src op (OPERATION_n_ALPHA is 1 bits)
  unsigned int texsrcrgb0:5;            // texenv src rgb n
  unsigned int texsrcrgb1:5;            // texenv src rgb n
  unsigned int texsrcrgb2:5;            // texenv src rgb n
  unsigned int texsrcrgb3:5;            // texenv src rgb n
  unsigned int texsrcalpha0:5;          // texenv src alpha n
  unsigned int texsrcalpha1:5;          // texenv src alpha n
  unsigned int texsrcalpha2:5;          // texenv src alpha n
  unsigned int texsrcalpha3:5;          // texenv src alpha n
  unsigned int texrgbscale:1;           // flag if RGB_SCALE for texture is != 1.0
  unsigned int texalphascale:1;         // flag if ALPHA_SCALE for texture is != 1.0
  unsigned int dummy:6;                 // align sturture to 64bits...
} fpe_texenv_t;

#pragma pack(1)
typedef struct fpe_state_s {
    fpe_texture_t texture[MAX_TEX];      // 16 texture flags
    fpe_texgen_t texgen[MAX_TEX];        // 16 texgen flags
    fpe_texenv_t texenv[MAX_TEX];        // 16 texture env flags
    uint8_t texcombine[MAX_TEX];         // 16 texture combined (RGB as lower 4 bits, A as higher 4 bits)
    uint8_t light;                       // 8 lights packed
    uint8_t light_cutoff180;             // 8 lights cutoff!=180 flags
    uint8_t light_direction;             // 8 lights position[3].w==0 flags
    unsigned int plane:6;                // 6 planes packed
    unsigned int fogmode:2;              // fog mode
    unsigned int fogdist:2;              // fog distance mode
    unsigned int fogsource:1;            // fog source
    unsigned int fog:1;                  // Fog enabled or not
    unsigned int colorsum:1;             // secondary color enabled
    unsigned int lighting:1;             // global lighting enabled
    unsigned int normalize:1;            // normalization
    unsigned int rescaling:1;            // rescale normal
    unsigned int alphafunc:3;            // alpha functions
    unsigned int alphatest:1;            // alpha test
    unsigned int twosided:1;             // lightmodel: two sided
    unsigned int color_material:1;       // color material enabled
    unsigned int cm_front_mode:3;        // front color material mode
    unsigned int cm_back_mode:3;         // back color material mode
    unsigned int cm_front_nullexp:1;     // front material shininess is 0
    unsigned int cm_back_nullexp:1;      // back material shininess is 0
    unsigned int light_separate:1;       // light separate specular color
    unsigned int light_localviewer:1;    // light local viewer
    unsigned int point:1;                // point rendering
    unsigned int pointsprite:1;          // point sprite rendering
    unsigned int pointsprite_coord:1;    // point sprite coord replace
    unsigned int pointsprite_upper:1;    // if coord is upper left and not lower left
    unsigned int vertex_prg_enable:1;    // if vertex program is enabled
    unsigned int fragment_prg_enable:1;  // if fragment program is enabled
    unsigned int blend_enable:1;
    unsigned int blendsrcrgb:4;
    unsigned int blendsrcalpha:4;
    unsigned int blenddstrgb:4;
    unsigned int blenddstalpha:4;
    unsigned int blendeqrgb:3;
    unsigned int blendeqalpha:3;
    uint16_t     vertex_prg_id;          // Id of vertex program currently binded (0 most of the time), 16bits is more than enough...
    uint16_t     fragment_prg_id;        // Id of fragment program currently binded (0 most of the time)
} fpe_state_t;
#pragma pack()

typedef struct fpe_fpe_s {
  GLuint  frag, vert, prog;   // shader info
  fpe_state_t state;          // state relevant to the current fpe program
  program_t *glprogram;
} fpe_fpe_t;

#ifndef kh_fpecachelist_t
typedef struct kh_fpecachelist_s kh_fpecachelist_t;
#endif
#define fpe_cache_t kh_fpecachelist_t

typedef struct scratch_s {
    void*       scratch[16];
    int         size;
} scratch_t;
void free_scratch(scratch_t* scratch);

fpe_fpe_t *fpe_GetCache(fpe_cache_t *cur, fpe_state_t *state, int fixed);
void fpe_disposeCache(fpe_cache_t* cache, int freeprog);

// fpe is gles export replacement, so should use same caling conversion
void APIENTRY_GLES fpe_glEnableClientState(GLenum cap);
void APIENTRY_GLES fpe_glDisableClientState(GLenum cap);
void APIENTRY_GLES fpe_glMultiTexCoord4f(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q);
void APIENTRY_GLES fpe_glSecondaryColorPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GLES fpe_glVertexPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GLES fpe_glColorPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GLES fpe_glNormalPointer(GLenum type, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GLES fpe_glTexCoordPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GLES fpe_glTexCoordPointerTMU(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer, int TMU);
void APIENTRY_GLES fpe_glFogCoordPointer(GLenum type, GLsizei stride, const GLvoid *pointer);
void APIENTRY_GLES fpe_glEnable(GLenum cap);
void APIENTRY_GLES fpe_glDisable(GLenum cap);
void APIENTRY_GLES fpe_glDrawArrays(GLenum mode, GLint first, GLsizei count);
void APIENTRY_GLES fpe_glDrawElements(GLenum mode, GLsizei count, GLenum type, const GLvoid *indices);
void APIENTRY_GLES fpe_glDrawElements(GLenum mode, GLsizei count, GLenum type, const GLvoid *indices);
void APIENTRY_GLES fpe_glDrawArraysInstanced(GLenum mode, GLint first, GLsizei count, GLsizei primcount);
void APIENTRY_GLES fpe_glDrawElementsInstanced(GLenum mode, GLsizei count, GLenum type, const GLvoid *indices, GLsizei primcount);
void APIENTRY_GLES fpe_glColor4f(GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha);
void APIENTRY_GLES fpe_glNormal3f(GLfloat nx, GLfloat ny, GLfloat nz);
void APIENTRY_GLES fpe_glClientActiveTexture(GLenum texture);
void APIENTRY_GLES fpe_glFogfv(GLenum pname, const GLfloat* params);
void APIENTRY_GLES fpe_glAlphaFunc(GLenum func, GLclampf ref);

void APIENTRY_GLES fpe_glMatrixMode(GLenum mode);

void APIENTRY_GLES fpe_glLightModelf(GLenum pname, GLfloat param);
void APIENTRY_GLES fpe_glLightModelfv(GLenum pname, const GLfloat* params);
void APIENTRY_GLES fpe_glLightfv(GLenum light, GLenum pname, const GLfloat* params);
void APIENTRY_GLES fpe_glMaterialfv(GLenum face, GLenum pname, const GLfloat *params);
void APIENTRY_GLES fpe_glMaterialf(GLenum face, GLenum pname, const GLfloat param);

void APIENTRY_GLES fpe_glPointParameterfv(GLenum pname, const GLfloat * params);
void APIENTRY_GLES fpe_glPointSize(GLfloat size);

void builtin_Init(program_t *glprogram);
int builtin_CheckUniform(program_t *glprogram, char* name, GLint id, int size);
int builtin_CheckVertexAttrib(program_t *glprogram, char* name, GLint id);

void realize_glenv(int ispoint, int first, int count, GLenum type, const void* indices, scratch_t* scratch);
void realize_blitenv(int alpha);

#endif // _GL4ES_FPE_H_
