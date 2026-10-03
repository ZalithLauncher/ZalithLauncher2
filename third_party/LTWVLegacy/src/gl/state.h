#ifndef _GL4ES_STATE_H_
#define _GL4ES_STATE_H_

#include "khash.h"
#include "buffers.h"
#include "eval.h"
#include "gles.h"
#include "list.h"
#include "program.h"
#include "raster.h"
#include "shader.h"
#include "texenv.h"
#include "texture.h"
#include "oldprogram.h"

typedef struct {
    GLboolean line_stipple,
              auto_normal,
              normalize,
              normal_rescale,
              lighting,
              alpha_test,
              fog,
              color_material,
              blend,
              cull_face,
              color_sum,
              depth_test,
              stencil_test,
              pointsprite,
              point_size,
              texgen_s[MAX_TEX],
              texgen_t[MAX_TEX],
              texgen_r[MAX_TEX],
              texgen_q[MAX_TEX],
              plane[MAX_CLIP_PLANES],
              light[MAX_LIGHT],
              map1_color4,
              map1_index,
              map1_normal,
              map1_texture1,
              map1_texture2,
              map1_texture3,
              map1_texture4,
              map1_vertex3,
              map1_vertex4,
              map2_color4,
              map2_index,
              map2_normal,
              map2_texture1,
              map2_texture2,
              map2_texture3,
              map2_texture4,
              map2_vertex3,
              map2_vertex4,
              color_logic_op,
              line_smooth,
              point_smooth,
              polyfill_offset,
              vertex_arb,
              vertex_two_side_arb,
              fragment_arb,
              multisample,
              sample_coverage,
              sample_alpha_to_one,
              sample_alpha_to_coverage;
    GLuint    texture[MAX_TEX]; // flag
} enable_state_t;

typedef struct {
    GLenum S;
    GLenum T;
    GLenum R;
    GLenum Q;
    GLfloat S_E[4]; // Eye Plane
    GLfloat T_E[4];
    GLfloat R_E[4];
    GLfloat Q_E[4];
    GLfloat S_O[4]; // Object Plane
    GLfloat T_O[4];
    GLfloat R_O[4];
    GLfloat Q_O[4];
} texgen_state_t;

typedef struct {
    texenv_t        env;
    texfilter_t     filter;
} texenv_state_t;

typedef struct {
    GLuint unpack_row_length,
           unpack_skip_pixels,
           unpack_skip_rows,
           unpack_image_height;
    GLboolean unpack_lsb_first;
    // TODO: use those values
    GLuint pack_row_length,
           pack_skip_pixels,
           pack_skip_rows,
           pack_image_height;
    GLuint  pack_align,
            unpack_align;            
    GLboolean pack_lsb_first;
    gltexture_t *bound[MAX_TEX][ENABLED_TEXTURE_LAST];  //TODO: this should be shared
    gltexture_t *zero;  // this is texture 0...
    GLboolean pscoordreplace[MAX_TEX];
    khash_t(tex) *list;     // this is shared among glstate
    GLuint active;	// active texture
	GLuint client;	// client active texture
} texture_state_t;

typedef struct {
    renderlist_t *active;
    GLboolean compiling;
    GLboolean pending;
    GLboolean begin;
    GLuint base;
    GLuint name;
    GLenum mode;

    GLuint count;
    GLuint cap;
} displaylist_state_t;

typedef struct {
    rasterpos_t rPos;
    GLboolean   rPos_valid;     /* 0 once glRasterPos clips or w<=0 */
                                /* TODO: expose as GL_CURRENT_RASTER_POSITION_VALID */
    viewport_t viewport;
    viewport_t scissor;
    int         viewport_known; /* 1 after glViewport or an init query; width==0 is legal */
    int         scissor_known;  /* 1 after glScissor or an init query */
    GLfloat raster_scale[4];
    GLfloat raster_bias[4];
    GLfloat raster_zoomx;
    GLfloat raster_zoomy;
    GLint index_shift;
    GLint index_offset;
    int     map_color;
    int     map_i2i_size;
    int     map_i2r_size;
    int     map_i2g_size;
    int     map_i2b_size;
    int     map_i2a_size;
    /*
    int     map_s2s_size;
    int     map_r2r_size;
    int     map_g2g_size;
    int     map_b2b_size;
    int     map_a2a_size;
    */
    GLuint  map_i2i[MAX_MAP_SIZE];
    GLubyte map_i2r[MAX_MAP_SIZE];
    GLubyte map_i2g[MAX_MAP_SIZE];
    GLubyte map_i2b[MAX_MAP_SIZE];
    GLubyte map_i2a[MAX_MAP_SIZE];
    /*
    GLuint  map_s2s[MAX_MAP_SIZE];   
    GLubyte map_r2r[MAX_MAP_SIZE];
    GLubyte map_g2g[MAX_MAP_SIZE];
    GLubyte map_b2b[MAX_MAP_SIZE];
    GLubyte map_a2a[MAX_MAP_SIZE];
    */
    GLubyte *data;
    rasterlist_t immediate;
    GLsizei raster_width;
    GLsizei raster_height;
    GLsizei raster_nwidth;
    GLsizei raster_nheight;
    GLint	raster_x1, raster_x2, raster_y1, raster_y2;
    // bitmap specific datas
    int     bm_drawing; // flag if some bitmap are there
    int     bm_x1, bm_y1;
    int     bm_x2, bm_y2;
    GLubyte *bitmap;
    GLsizei bm_alloc;
    GLsizei bm_width, bm_height;
    GLuint  bm_texture;
    int     bm_tnwidth, bm_tnheight;

} raster_state_t;


typedef struct {
    map_state_t *vertex3,
                *vertex4,
                *index,
                *color4,
                *normal,
                *texture1,
                *texture2,
                *texture3,
                *texture4;
} map_states_t;

typedef struct {
	int		top;
	GLuint	*names;
} namestack_t;

typedef struct {
	GLuint  count;
    GLuint *buffer;
    GLuint  size;
    GLfloat zmin;
    GLfloat zmax;
    GLfloat zminoverall;
    GLfloat zmaxoverall;
    GLuint  overflow;
    GLuint  pos;
    GLboolean  hit;
} selectbuf_t;

typedef struct {
	int		top;
    int     identity;
	GLfloat	*stack;
} matrixstack_t;

typedef struct glsl_s {
    float                  vtx_env_params[MAX_VTX_PROG_ENV_PARAMS*4];  // ARB_vertex_program Program Env Parameters
    float                  frg_env_params[MAX_FRG_PROG_ENV_PARAMS*4];  // ARB_fragment_program Program Env Parameters
    khash_t(shaderlist)    *shaders;
    khash_t(programlist)   *programs;
    GLuint                 program;
    program_t              *glprogram;
    int                    es2; // context is es2
    // old ARB_vertex_program & ARB_fragment_program states
    khash_t(oldprograms)   *oldprograms;
    int                    error_ptr;   // error position from last "Old" program compile
    char*                  error_msg;   // error msg from last "Old" program compile
    oldprogram_t           *vtx_prog;
    oldprogram_t           *frg_prog;
} glsl_t;

typedef struct {
    GLuint          program;
    program_t       *glprogram;
    GLuint          active; // active texture (is it shared?)
    vertexattrib_t  vertexattrib[MAX_VATTRIB];
    GLfloat         vavalue[MAX_VATTRIB][4];
} gleshard_t;

typedef struct {
    GLuint          vertexshader;
    GLuint          pixelshader;
    GLuint          vertexshader_alpha;
    GLuint          pixelshader_alpha;
    GLuint          program;
    GLuint          program_alpha;
    GLfloat         vert[8], tex[8];
} glesblit_t;

typedef struct {
    char*           shadersource; // scrach buffer for fpe shader construction
    int             shadersize;
} fpestatus_t;

typedef struct {
    GLint factor;
    GLushort pattern;
    GLubyte *data;
    GLuint texture;
} linestipple_t;

// FBO structures
typedef struct {
    GLuint      renderbuffer;
    GLenum      attachment;         // can be color0 or depth_stencil for example
    GLuint      secondarybuffer;    // secondary renderbuffer, if depth_stencil is not possible for example
    GLuint      secondarytexture;   // the texture, in case of a color0 attachement...
    GLenum      format;             // requested format
    GLenum      actual;             // actual formal
    int         width;
    int         height;
} glrenderbuffer_t;

KHASH_MAP_DECLARE_INT(renderbufferlist_t, glrenderbuffer_t *);

typedef struct {
    GLuint id;
    GLenum target;
    GLuint color[MAX_DRAW_BUFFERS];   // attachement_color0..9
    GLuint depth;
    GLuint stencil;
    GLuint t_color[MAX_DRAW_BUFFERS]; // type for attachement_0 (GL_NONE, GL_TEXTUREXX, GL_RENDERBUFFER)
    GLuint t_depth;
    GLuint t_stencil;
    int    l_color[MAX_DRAW_BUFFERS]; // level of attachment
    int    l_depth;
    int    l_stencil;
    int    width;
    int    height;
    GLenum read_format;
    GLenum read_type;
    int    n_draw;
    GLenum drawbuff[MAX_DRAW_BUFFERS];    //TODO: define a MAX_DRAWBUFF?
} glframebuffer_t;

typedef struct {
    GLuint *fbos;
    int     nbr;
    int     cap;
} oldfbos_t;

KHASH_MAP_DECLARE_INT(framebufferlist_t, glframebuffer_t *);

typedef struct {
    khash_t(renderbufferlist_t) *renderbufferlist;
    glrenderbuffer_t *default_rb;
    glrenderbuffer_t *current_rb;
    
    GLuint mainfbo_fbo; // The MainFBO
    GLuint mainfbo_tex; // Texture Attachment
    GLuint mainfbo_dep; // Depth attachment
    GLuint mainfbo_ste; // Stencil attachement
    int mainfbo_width;
    int mainfbo_height;
    int mainfbo_nwidth;
    int mainfbo_nheight;
    
    khash_t(framebufferlist_t) *framebufferlist;
    glframebuffer_t *fbo_0;
    glframebuffer_t *fbo_read;
    glframebuffer_t *fbo_draw;
    glframebuffer_t *current_fb;

    GLenum fb_status;
    int    internal;

    oldfbos_t   *old;
} fbo_t;

typedef struct {
    GLenum      func;
    GLboolean   mask;
    GLfloat     Near, Far;
    GLfloat     clear;
} depth_state_t;

typedef struct {
    GLenum      cull;
    GLenum      front;
} face_state_t;

//glsampler_t define in texture.h

KHASH_MAP_DECLARE_INT(samplerlist_t, glsampler_t *);

typedef struct {
    khash_t(samplerlist_t) *samplerlist;
    glsampler_t*    sampler[MAX_TEX];
    GLuint          last_sampler;
} samplers_t;

// queries

typedef struct {
    GLuint id;
    GLenum target;
    int num;
    int active;
    GLuint start;
} glquery_t;

KHASH_MAP_DECLARE_INT(queries, glquery_t *)

typedef struct {
    khash_t(queries) *querylist;
    GLuint          last_query;
    unsigned long long start;
} queries_t;

typedef struct {
    GLuint  array;
    GLuint  index;
    GLuint  want_index;
    int     used;
} bind_buffers_t;


#endif // _GL4ES_STATE_H_
