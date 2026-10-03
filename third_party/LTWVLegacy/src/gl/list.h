#ifndef _GL4ES_LIST_H_
#define _GL4ES_LIST_H_

#include <stdbool.h>
#include "khash.h"
#include "../config.h"
#include "wrap/gles.h"
#include "attributes.h"
#include "gles.h"

typedef enum {
	STAGE_NONE = 0,
	STAGE_PUSH,
	STAGE_POP,
	STAGE_GLCALL,
    STAGE_RENDER,
	STAGE_FOG,
    STAGE_POINTPARAM,
    STAGE_MATRIX,
    STAGE_ACTIVETEX,
	STAGE_BINDTEX,
	STAGE_RASTER,
    STAGE_BITMAP,
	STAGE_MATERIAL,
    STAGE_COLOR_MATERIAL,
	STAGE_LIGHT,
	STAGE_LIGHTMODEL,
    STAGE_LINESTIPPLE,
	STAGE_TEXENV,
	STAGE_TEXGEN,
	STAGE_POLYGON,
	STAGE_DRAW,
    STAGE_POSTDRAW,
	STAGE_LAST
} liststage_t;

static int StageExclusive[] = {
	0, 	// STAGE_NONE
	1,	// STAGE_PUSH
	1,  // STAGE_POP
	0,  // STAGE_GLCALL
    1,  // STAGE_RENDER
	1, 	// STAGE_FOG
	1, 	// STAGE_POINTPARAM
    1,  // STAGE_MATRIX
    1,  // STAGE_ACTIVETEX
	1,  // STAGE_BINDTEX
	1,  // STAGE_RASTER
    0,  // STAGE_BITMAP
	0,  // STAGE_MATERIAL
    1,  // STAGE_COLOR_MATERIAL
	0,  // STAGE_LIGHT
	1,  // STAGE_LIGTMODEL
    1,  // STAGE_LINESTIPPLE
	0,  // STAGE_TEXENV
	0,  // STAGE_TEXGEN
	1,  // STAGE_POLYGON
	1,  // STAGE_DRAW
    1,  // STAGE_POSTDRAW   (used for "pending", i.e. post glEnd(), in case a similar glBegin occurs)
    0   // STAGE_LAST
};

typedef struct {
    int face;
    int pname;
    GLfloat color[4];
    int count;
} rendermaterial_t;

typedef struct {
    int which;
    int pname;
    GLfloat color[4];
    int count;
} renderlight_t;

typedef struct {
    int coord;
    int pname;
    GLfloat color[4];
    int count;
} rendertexgen_t;

typedef struct {
    int target;
    int pname;
    GLfloat params[4];
    int count;
} rendertexenv_t;

typedef struct {
	GLfloat	xmove;
	GLfloat ymove;
	GLsizei width;
	GLsizei height;
	GLsizei nwidth;
	GLsizei nheight;
	GLfloat xorig;
	GLfloat yorig;
	GLfloat zoomx;
	GLfloat zoomy;
	GLboolean bitmap;
	GLuint	texture;
    int     *shared;
} rasterlist_t;

KHASH_MAP_DECLARE_INT(material, rendermaterial_t *);
KHASH_MAP_DECLARE_INT(light, renderlight_t *);
KHASH_MAP_DECLARE_INT(texgen, rendertexgen_t *);
KHASH_MAP_DECLARE_INT(texenv, rendertexenv_t *);

typedef struct _call_list_t {
    unsigned long len;
    unsigned long cap;
    packed_call_t **calls;
} call_list_t;

typedef struct {
	GLsizei width;
	GLsizei height;
	GLfloat xorig;
	GLfloat yorig;
	GLfloat	xmove;
	GLfloat ymove;
    GLubyte *bitmap;
} bitmap_list_t;

typedef struct {
    int         count;
    int         cap;
    bitmap_list_t *list;
    int     *shared;
} bitmaps_t;

typedef struct {
    GLenum mode_init;
    int    ilen;
} modeinit_t;

typedef struct _renderlist_t {
    unsigned long len;
    unsigned long ilen;
    unsigned long cap;
    GLenum mode;
    GLenum mode_init;		// initial requested mode
    GLuint name;
    modeinit_t* mode_inits;   // array of requested/len, for the merger
    int     mode_init_cap;
    int     mode_init_len;
    int    mode_dimension;
    GLfloat lastNormal[3];
    GLfloat lastColors[4];
    GLfloat lastSecondaryColors[4];
    GLfloat lastFogCoord;
    int use_glstate;

    int lastColorsSet;

    int*        shared_calls;
    call_list_t calls;
    
    int *shared_arrays;
    GLfloat *vert;
    GLfloat *normal;
    GLfloat *color;
    GLfloat *secondary;
    GLfloat *fogcoord;
    GLfloat *tex[MAX_TEX];
    int      vert_stride;
    int      normal_stride;
    int      color_stride;
    int      secondary_stride;
    int      fogcoord_stride;
    int      tex_stride[MAX_TEX];
    GLuint   vbo_array;
    GLuint   vbo_indices;
    int      use_vbo_array;   // 0=Not evaluated, 1=No, 2=Yes
    int      use_vbo_indices; // same
    GLfloat *vbo_vert;
    GLfloat *vbo_normal;
    GLfloat *vbo_color;
    GLfloat *vbo_secondary;
    GLfloat *vbo_fogcoord;
    GLfloat *vbo_tex[MAX_TEX];
    int *shared_indices;
    GLushort *indices;
    unsigned int indice_cap;
    int maxtex;
    GLenum  merger_mode;
    int     cur_istart;     // used by glBegin/glEnd merger.
	
	rasterlist_t *raster;

    bitmaps_t *bitmaps;
	
	liststage_t	stage;
	
	GLbitfield pushattribute;
	GLboolean  popattribute;
    
    int     render_op;
    GLuint  render_arg;

    int     raster_op;
    GLfloat raster_xyz[3];
    
    int     matrix_op;
    GLfloat matrix_val[16];
    
    int     fog_op;
    GLfloat fog_val[4];

    int     pointparam_op;
    GLfloat pointparam_val[4];

    int     linestipple_op;
    GLuint  linestipple_factor, linestipple_pattern;

    int     post_color;
    GLfloat post_colors[4];
    int     post_normal;
    GLfloat post_normals[3];

    GLushort    *ind_lines;
    int         ind_line;
    GLfloat      *final_colors;

    int         instanceCount;
    
    khash_t(material) *material;
    GLenum  colormat_face;
    GLenum  colormat_mode;
    khash_t(light) *light;
    khash_t(texgen) *texgen;
    khash_t(texenv) *texenv;
    GLfloat	*lightmodel;
    GLenum	lightmodelparam;
    GLenum	polygon_mode;
    GLboolean set_tmu;      // TRUE is glActiveTexture called
    int tmu;             // the current TMU...
    GLuint texture;				
    GLenum target_texture;      
    GLboolean  set_texture;
    struct _renderlist_t *prev;
    struct _renderlist_t *next;
    GLboolean open;
} renderlist_t;

KHASH_MAP_DECLARE_INT(gllisthead, renderlist_t*);

#define DEFAULT_CALL_LIST_CAPACITY 20
#define DEFAULT_RENDER_LIST_CAPACITY 64

int rendermode_dimensions(GLenum mode);
renderlist_t* recycle_renderlist(renderlist_t* list, GLenum mode);
renderlist_t* NewDrawStage(renderlist_t* l, GLenum m);
                
#define NewStage(l, s) if (l->stage+StageExclusive[l->stage] > s) {l = extend_renderlist(l);} l->stage = s

renderlist_t* GetFirst(renderlist_t* list);

#define alloc_sublist(n, cap) \
    (GLfloat *)malloc(n * sizeof(GLfloat) * cap)

#define realloc_sublist(ref, n, cap) \
    if (ref)                         \
        ref = (GLfloat *)realloc(ref, n * sizeof(GLfloat) * cap)

#define realloc_merger_sublist(ref, n, cap) \
        ref = (GLfloat *)realloc(ref, n * sizeof(GLfloat) * cap)

renderlist_t *alloc_renderlist();
renderlist_t *extend_renderlist(renderlist_t *list);
void free_renderlist(renderlist_t *list);
void draw_renderlist(renderlist_t *list);
renderlist_t* end_renderlist(renderlist_t *list);
bool isempty_renderlist(renderlist_t *list);
void resize_renderlist(renderlist_t *list);
renderlist_t *alloc_renderlist();
int mode_needindices(GLenum m);
void resize_indices_renderlist(renderlist_t *list, int n);
void resize_merger_indices(int cap);
int indices_getindicesize(GLenum mode, int len);
void list_add_modeinit(renderlist_t* list, GLenum mode);
void unshared_renderlist(renderlist_t *a, int cap);
void unsharedindices_renderlist(renderlist_t* a, int cap);
void redim_renderlist(renderlist_t *a, int cap);
void prepareadd_renderlist(renderlist_t* a, int size_to_add);
void doadd_renderlist(renderlist_t* a, GLenum mode, GLushort* indices, int count, int size_to_add);

void renderlist_createindices(int ilen, GLushort *indices, int count);
void renderlist_lineloop_lines(GLushort *ind, int len, GLushort *indices, int count);
void renderlist_linestrip_lines(GLushort *ind, int len, GLushort *indices, int count);
void renderlist_trianglestrip_triangles(GLushort *ind, int len, GLushort *indices, int count);
void renderlist_trianglefan_triangles(GLushort *ind, int len, GLushort *indices, int count);
void renderlist_quads_triangles(GLushort *ind, int len, GLushort *indices, int count);

void rlActiveTexture(renderlist_t *list, GLenum texture );
void rlBindTexture(renderlist_t *list, GLenum target, GLuint texture);
void rlColor4f(renderlist_t *list, GLfloat r, GLfloat g, GLfloat b, GLfloat a) FASTMATH;
void rlColor4fv(renderlist_t *list, GLfloat* v) FASTMATH;
void rlMaterialfv(renderlist_t *list, GLenum face, GLenum pname, const GLfloat * params);
void rlLightfv(renderlist_t *list, GLenum which, GLenum pname, const GLfloat * params);
void rlTexGenfv(renderlist_t *list, GLenum coord, GLenum pname, const GLfloat * params);
void rlTexEnvfv(renderlist_t *list, GLenum target, GLenum pname, const GLfloat * params);
void rlTexEnviv(renderlist_t *list, GLenum target, GLenum pname, const GLint * params);
void rlNormal3f(renderlist_t *list, GLfloat x, GLfloat y, GLfloat z) FASTMATH;
void rlNormal3fv(renderlist_t *list, GLfloat* v) FASTMATH;
void rlPushCall(renderlist_t *list, packed_call_t *data);
void rlMultiTexCoord4f(renderlist_t *list, GLenum texture, GLfloat s, GLfloat t, GLfloat r, GLfloat q) FASTMATH;
void rlMultiTexCoord2fv(renderlist_t *list, GLenum texture, GLfloat* v) FASTMATH;
void rlMultiTexCoord4fv(renderlist_t *list, GLenum texture, GLfloat* v) FASTMATH;
void rlVertex4f(renderlist_t *list, GLfloat x, GLfloat y, GLfloat z, GLfloat w) FASTMATH;
void rlVertex3fv(renderlist_t *list, GLfloat* v) FASTMATH;
void rlVertex4fv(renderlist_t *list, GLfloat* v) FASTMATH;
void rlSecondary3f(renderlist_t *list, GLfloat r, GLfloat g, GLfloat b) FASTMATH;
void rlRasterOp(renderlist_t *list, int op, GLfloat x, GLfloat y, GLfloat z) FASTMATH;
void rlFogOp(renderlist_t *list, int op, const GLfloat* v);
void rlPointParamOp(renderlist_t *list, int op, const GLfloat* v);
void rlFogCoordf(renderlist_t *list, GLfloat coord);
void rlEnd(renderlist_t *list);

#endif // _GL4ES_LIST_H_
