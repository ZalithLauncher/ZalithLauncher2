#ifndef _GL4ES_STACK_H_
#define _GL4ES_STACK_H_

#include "gles.h"
#include "state.h"
#include "texture.h"
#include <gl4eshint.h>

#define STACK_SIZE 16

#define GL4ES_HINT_FIRST GL_SHRINK_HINT_GL4ES
#define GL4ES_HINT_LAST (GL_NOERROR_HINT_GL4ES + 1)


typedef struct _glstack_t {
    GLbitfield mask;

    // GL_COLOR_BUFFER_BIT
    GLboolean alpha_test;
    GLint alpha_test_func;
    GLclampf alpha_test_ref;

    GLboolean blend;
    GLint blend_src_func;
    GLint blend_dst_func;
	
    GLboolean dither;

    // point sprite
    GLboolean pointsprite;
    GLint     pscoordreplace[MAX_TEX];

    GLboolean color_logic_op;
    GLint logic_op;

    GLfloat clear_color[4];
    GLfloat color_mask[4];

    // GL_CURRENT_BIT
    GLfloat color[4];
    GLfloat normal[4];
    GLfloat tex[4];
    GLfloat secondary[4];

    // TODO: can only fill this via raster.c
    GLfloat raster_pos[3];
    GLboolean raster_valid;
	GLfloat pixel_scale_bias[4+4];
	GLfloat pixel_zoomx;
	GLfloat pixel_zoomy;
	
    // GL_DEPTH_BUFFER_BIT
    GLboolean depth_test;
    GLint depth_func;
    GLfloat clear_depth;
    GLint depth_mask;

    // GL_ENABLE_BIT
    GLboolean cull_face;
    GLboolean normalize;
    GLboolean polygon_offset_fill;
    GLboolean stencil_test;
    GLuint    tex_enabled[MAX_TEX];
    GLboolean texgen_s[MAX_TEX];
    GLboolean texgen_r[MAX_TEX];
    GLboolean texgen_t[MAX_TEX];
    GLboolean texgen_q[MAX_TEX];
    GLboolean colormaterial;
    GLboolean autonormal;

    // GL_FOG_BIT
    GLboolean fog;
    GLfloat fog_color[4];
    GLfloat fog_density;
    GLfloat fog_start;
    GLfloat fog_end;
    GLint fog_mode;

    // GL_HINT_BIT
    GLint perspective_hint;
    GLint point_smooth_hint;
    GLint line_smooth_hint;
    GLint fog_hint;
    GLint mipmap_hint;
    GLint gles4_hint[GL4ES_HINT_LAST-GL4ES_HINT_FIRST];

    // GL_LIGHTING_BIT
    GLboolean lighting;
    GLboolean *lights_enabled;
    GLfloat *lights;
    GLfloat light_model_ambient[4];
    GLint light_model_two_side;
    GLfloat *materials;
    GLint shade_model;

    // GL_LINE_BIT
    GLboolean line_smooth;
    GLboolean line_stipple;
    GLint line_stipple_factor;
    GLushort line_stipple_pattern;
    GLfloat line_width;

    // GL_LIST_BIT
    GLint list_base;

    // GL_MULTISAMPLE_BIT
    GLboolean multisample;
    GLboolean sample_alpha_to_coverage;
    GLboolean sample_alpha_to_one;
    GLboolean sample_coverage;

    // GL_POINT_BIT
    GLboolean point_smooth;
    GLfloat point_size;
    GLfloat point_size_min;
    GLfloat point_size_max;
    GLfloat point_fade_threshold;
    GLfloat point_distance_attenuation[3];

    // GL_POLYGON_BIT (cull_face & polygon_offset_fill enables shared with GL_ENABLE_BIT)
    GLint cull_face_mode;
    GLint front_face;
    GLenum polygon_mode;

    // TODO: GL_POLYGON_STIPPLE_BIT

    // GL_SCISSOR_BIT
    GLboolean scissor_test;
    GLfloat scissor_box[4];

    // GL_STENCIL_BUFFER_BIT
    GLenum stencil_func;
    GLint  stencil_ref;
    GLuint stencil_mask;
    GLint  stencil_clearvalue;
    GLenum stencil_sfail;
    GLenum stencil_dpfail;
    GLenum stencil_dppass;

    // GL_TEXTURE_BIT
    GLint texture[MAX_TEX][ENABLED_TEXTURE_LAST];
    texgen_state_t texgen[MAX_TEX];
    GLenum texenv_mode[MAX_TEX];
    GLfloat texenv_color[MAX_TEX][4];
    GLint active;

    // GL_TRANSFORM_BIT
	// with Clip Planes...
	GLenum matrix_mode;
	GLboolean normalize_flag;
	GLboolean rescale_normal_flag;
    // GL_VIEWPORT_BIT
	GLint	viewport_size[4];
	GLfloat depth_range[2];
	
    // dynamically-sized shenanigans
    GLboolean *clip_planes_enabled;
    GLfloat *clip_planes;

    // misc
    unsigned int len;
    unsigned int cap;
} glstack_t;

typedef struct _glclientstack_t {
    GLbitfield mask;

    // GL_CLIENT_PIXEL_STORE_BIT
    GLint pack_align;
    GLint unpack_align;
    GLuint unpack_row_length;
    GLuint unpack_skip_pixels;
    GLuint unpack_skip_rows;
    GLuint pack_row_length;
    GLuint pack_skip_pixels;
    GLuint pack_skip_rows;

    // GL_CLIENT_VERTEX_ARRAY_BIT
	GLuint client;
    vertexattrib_t vertexattrib[MAX_VATTRIB];
    unsigned int len;
    unsigned int cap;
} glclientstack_t;

void APIENTRY_GL4ES gl4es_glPushClientAttrib(GLbitfield mask);
void APIENTRY_GL4ES gl4es_glPopClientAttrib(void);
void APIENTRY_GL4ES gl4es_glPushAttrib(GLbitfield mask);
void APIENTRY_GL4ES gl4es_glPopAttrib(void);

#endif // _GL4ES_STACK_H_
