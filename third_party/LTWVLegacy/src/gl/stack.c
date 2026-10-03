#include <stdio.h>

#include "stack.h"

#include "../glx/hardext.h"
#include "wrap/gl4es.h"
#include "line.h"
#include "matrix.h"
#include "debug.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

void APIENTRY_GL4ES gl4es_glPushAttrib(GLbitfield mask) {
    DBG(printf("glPushAttrib(0x%04X)\n", mask);)
    realize_textures(0);
    noerrorShim();
    if (glstate->list.active)
        if (glstate->list.compiling) {
            NewStage(glstate->list.active, STAGE_PUSH);
            glstate->list.active->pushattribute = mask;
            return;
        } else gl4es_flush();

    if (glstate->stack == NULL) {
        glstate->stack = (glstack_t *)malloc(STACK_SIZE * sizeof(glstack_t));
        glstate->stack->len = 0;
        glstate->stack->cap = STACK_SIZE;
    } else if (glstate->stack->len == glstate->stack->cap) {
        glstate->stack->cap += STACK_SIZE;
        glstate->stack = (glstack_t *)realloc(glstate->stack, glstate->stack->cap * sizeof(glstack_t));
    }

    glstack_t *cur = glstate->stack + glstate->stack->len;
    cur->mask = mask;
    cur->clip_planes_enabled = NULL;
    cur->clip_planes = NULL;
    cur->lights_enabled = NULL;
    cur->lights = NULL;
    cur->materials = NULL;

    // TODO: GL_ACCUM_BUFFER_BIT

    // TODO: will tracking these myself be much faster than glGet?
    if (mask & GL_COLOR_BUFFER_BIT) {
        cur->alpha_test = gl4es_glIsEnabled(GL_ALPHA_TEST);
        gl4es_glGetIntegerv(GL_ALPHA_TEST_FUNC, &cur->alpha_test_func);
        gl4es_glGetFloatv(GL_ALPHA_TEST_REF, &cur->alpha_test_ref);

        cur->blend = gl4es_glIsEnabled(GL_BLEND);
        gl4es_glGetIntegerv(GL_BLEND_SRC, &cur->blend_src_func);
        gl4es_glGetIntegerv(GL_BLEND_DST, &cur->blend_dst_func);

        cur->dither = gl4es_glIsEnabled(GL_DITHER);
        cur->color_logic_op = gl4es_glIsEnabled(GL_COLOR_LOGIC_OP);
        gl4es_glGetIntegerv(GL_LOGIC_OP_MODE, &cur->logic_op);

        gl4es_glGetFloatv(GL_COLOR_CLEAR_VALUE, cur->clear_color);
        gl4es_glGetFloatv(GL_COLOR_WRITEMASK, cur->color_mask);
    }

    if (mask & GL_CURRENT_BIT) {
        gl4es_glGetFloatv(GL_CURRENT_COLOR, cur->color);
        gl4es_glGetFloatv(GL_CURRENT_NORMAL, cur->normal);
        gl4es_glGetFloatv(GL_CURRENT_TEXTURE_COORDS, cur->tex);
    }

    if (mask & GL_DEPTH_BUFFER_BIT) {
        cur->depth_test = gl4es_glIsEnabled(GL_DEPTH_TEST);
        gl4es_glGetIntegerv(GL_DEPTH_FUNC, &cur->depth_func);
        gl4es_glGetFloatv(GL_DEPTH_CLEAR_VALUE, &cur->clear_depth);
        gl4es_glGetIntegerv(GL_DEPTH_WRITEMASK, &cur->depth_mask);
    }

    if (mask & GL_ENABLE_BIT) {
        int i;

        cur->alpha_test = gl4es_glIsEnabled(GL_ALPHA_TEST);
        cur->autonormal = gl4es_glIsEnabled(GL_AUTO_NORMAL);
        cur->blend = gl4es_glIsEnabled(GL_BLEND);
        
        cur->clip_planes_enabled = (GLboolean *)malloc(hardext.maxplanes * sizeof(GLboolean));
        for (i = 0; i < hardext.maxplanes; i++) {
            *(cur->clip_planes_enabled + i) = gl4es_glIsEnabled(GL_CLIP_PLANE0 + i);
        }

        cur->colormaterial = gl4es_glIsEnabled(GL_COLOR_MATERIAL);
        cur->cull_face = gl4es_glIsEnabled(GL_CULL_FACE);
        cur->depth_test = gl4es_glIsEnabled(GL_DEPTH_TEST);
        cur->dither = gl4es_glIsEnabled(GL_DITHER);
        cur->fog = gl4es_glIsEnabled(GL_FOG);

        cur->lights_enabled = (GLboolean *)malloc(hardext.maxlights * sizeof(GLboolean));
        for (i = 0; i < hardext.maxlights; i++) {
            *(cur->lights_enabled + i) = gl4es_glIsEnabled(GL_LIGHT0 + i);
        }

        cur->lighting = gl4es_glIsEnabled(GL_LIGHTING);
        cur->line_smooth = gl4es_glIsEnabled(GL_LINE_SMOOTH);
        cur->line_stipple = gl4es_glIsEnabled(GL_LINE_STIPPLE);
        cur->color_logic_op = gl4es_glIsEnabled(GL_COLOR_LOGIC_OP);
        //TODO: GL_INDEX_LOGIC_OP
        //TODO: GL_MAP1_x
        //TODO: GL_MAP2_x
        cur->multisample = gl4es_glIsEnabled(GL_MULTISAMPLE);
        cur->normalize = gl4es_glIsEnabled(GL_NORMALIZE);
        cur->point_smooth = gl4es_glIsEnabled(GL_POINT_SMOOTH);
        //TODO: GL_POLYGON_OFFSET_LINE
        cur->polygon_offset_fill = gl4es_glIsEnabled(GL_POLYGON_OFFSET_FILL);
        //TODO: GL_POLYGON_OFFSET_POINT
        //TODO: GL_POLYGON_SMOOTH
        //TODO: GL_POLYGON_STIPPLE
        cur->sample_alpha_to_coverage = gl4es_glIsEnabled(GL_SAMPLE_ALPHA_TO_COVERAGE);
        cur->sample_alpha_to_one = gl4es_glIsEnabled(GL_SAMPLE_ALPHA_TO_ONE);
        cur->sample_coverage = gl4es_glIsEnabled(GL_SAMPLE_COVERAGE);
        cur->scissor_test = gl4es_glIsEnabled(GL_SCISSOR_TEST);
        cur->stencil_test = gl4es_glIsEnabled(GL_STENCIL_TEST);
        int a;
        for (a=0; a<hardext.maxtex; a++) {
            cur->tex_enabled[a] = glstate->enable.texture[a];
            cur->texgen_s[a] = glstate->enable.texgen_s[a];
            cur->texgen_r[a] = glstate->enable.texgen_r[a];
            cur->texgen_t[a] = glstate->enable.texgen_t[a];
            cur->texgen_q[a] = glstate->enable.texgen_q[a];
        }
        cur->pointsprite = gl4es_glIsEnabled(GL_POINT_SPRITE);
    }

    // TODO: GL_EVAL_BIT

    if (mask & GL_FOG_BIT) {
        cur->fog = gl4es_glIsEnabled(GL_FOG);
        gl4es_glGetFloatv(GL_FOG_COLOR, cur->fog_color);
        gl4es_glGetFloatv(GL_FOG_DENSITY, &cur->fog_density);
        gl4es_glGetFloatv(GL_FOG_START, &cur->fog_start);
        gl4es_glGetFloatv(GL_FOG_END, &cur->fog_end);
        gl4es_glGetIntegerv(GL_FOG_MODE, &cur->fog_mode);
    }

    if (mask & GL_HINT_BIT) {
        gl4es_glGetIntegerv(GL_PERSPECTIVE_CORRECTION_HINT, &cur->perspective_hint);
        gl4es_glGetIntegerv(GL_POINT_SMOOTH_HINT, &cur->point_smooth_hint);
        gl4es_glGetIntegerv(GL_LINE_SMOOTH_HINT, &cur->line_smooth_hint);
        gl4es_glGetIntegerv(GL_FOG_HINT, &cur->fog_hint);
        gl4es_glGetIntegerv(GL_GENERATE_MIPMAP_HINT, &cur->mipmap_hint);
        for (int i=GL4ES_HINT_FIRST; i<GL4ES_HINT_LAST; i++)
            gl4es_glGetIntegerv(i, &cur->gles4_hint[i-GL4ES_HINT_FIRST]);
    }

    if (mask & GL_LIGHTING_BIT) {
        cur->lighting = gl4es_glIsEnabled(GL_LIGHTING);
        gl4es_glGetFloatv(GL_LIGHT_MODEL_AMBIENT, cur->light_model_ambient);
        gl4es_glGetIntegerv(GL_LIGHT_MODEL_TWO_SIDE, &cur->light_model_two_side);

        int i;
        int j=0;
        cur->lights_enabled = (GLboolean *)malloc(hardext.maxlights * sizeof(GLboolean));
        cur->lights = (GLfloat *)malloc(hardext.maxlights * sizeof(GLfloat)*(10*4));
        for (i = 0; i < hardext.maxlights; i++) {
            *(cur->lights_enabled + i) = gl4es_glIsEnabled(GL_LIGHT0 + i);
            #define L(A) gl4es_glGetLightfv(GL_LIGHT0 + i, A, cur->lights+j); j+=4
            L(GL_AMBIENT);
            L(GL_DIFFUSE);
            L(GL_SPECULAR);
            L(GL_POSITION); 
            L(GL_SPOT_CUTOFF);
            L(GL_SPOT_DIRECTION);
            L(GL_SPOT_EXPONENT);
            L(GL_CONSTANT_ATTENUATION);
            L(GL_LINEAR_ATTENUATION);
            L(GL_QUADRATIC_ATTENUATION);
            #undef L
        }
        j=0;
        cur->materials = (GLfloat *)malloc(2 * sizeof(GLfloat)*(5*4));
        memset(cur->materials, 0, 2 * sizeof(GLfloat)*(5*4));
        #define M(A) gl4es_glGetMaterialfv(GL_BACK, A, cur->materials+j); j+=4; gl4es_glGetMaterialfv(GL_FRONT, A, cur->materials+j); j+=4
        M(GL_AMBIENT); M(GL_DIFFUSE); M(GL_SPECULAR); M(GL_EMISSION); M(GL_SHININESS);  // handle both face at some point?
        #undef M
        gl4es_glGetIntegerv(GL_SHADE_MODEL, &cur->shade_model);
    }

    if (mask & GL_LINE_BIT) {
        cur->line_smooth = gl4es_glIsEnabled(GL_LINE_SMOOTH);
        cur->line_stipple = gl4es_glIsEnabled(GL_LINE_STIPPLE);
        cur->line_stipple_factor = glstate->linestipple.factor;
        cur->line_stipple_pattern = glstate->linestipple.pattern;
        gl4es_glGetFloatv(GL_LINE_WIDTH, &cur->line_width);
    }

	// GL_LIST_BIT
    if (mask & GL_LIST_BIT) {
        cur->list_base = glstate->list.base;
    }

    if (mask & GL_MULTISAMPLE_BIT) {
        cur->multisample = gl4es_glIsEnabled(GL_MULTISAMPLE);
        cur->sample_alpha_to_coverage = gl4es_glIsEnabled(GL_SAMPLE_ALPHA_TO_COVERAGE);
        cur->sample_alpha_to_one = gl4es_glIsEnabled(GL_SAMPLE_ALPHA_TO_ONE);
        cur->sample_coverage = gl4es_glIsEnabled(GL_SAMPLE_COVERAGE);
    }

    // GL_PIXEL_MODE_BIT
	if (mask & GL_PIXEL_MODE_BIT) {
		GLenum pixel_name[] = {GL_RED_BIAS, GL_RED_SCALE, GL_GREEN_BIAS, GL_GREEN_SCALE, GL_BLUE_BIAS, GL_BLUE_SCALE, GL_ALPHA_BIAS, GL_ALPHA_SCALE};
		int i;
		for (i=0; i<8; i++) 
			gl4es_glGetFloatv(pixel_name[i], &cur->pixel_scale_bias[i]);
        //TODO: GL_DEPTH_BIAS & GL_DEPTH_SCALE (probably difficult)
        //TODO: GL_INDEX_OFFEST & GL_INDEX_SHIFT
        //TODO: GL_MAP_COLOR & GL_MAP_STENCIL (probably difficult too)
		gl4es_glGetFloatv(GL_ZOOM_X, &cur->pixel_zoomx);
		gl4es_glGetFloatv(GL_ZOOM_Y, &cur->pixel_zoomy);
	}
	
    if (mask & GL_POINT_BIT) {
        cur->point_smooth = gl4es_glIsEnabled(GL_POINT_SMOOTH);
        gl4es_glGetFloatv(GL_POINT_SIZE, &cur->point_size);
        cur->point_size_min = glstate->pointsprite.sizeMin;
        cur->point_size_max = glstate->pointsprite.sizeMax;
        cur->point_fade_threshold = glstate->pointsprite.fadeThresholdSize;
        memcpy(cur->point_distance_attenuation, glstate->pointsprite.distance, 3*sizeof(GLfloat));
        if(hardext.pointsprite) {
            cur->pointsprite = gl4es_glIsEnabled(GL_POINT_SPRITE);
            int a;
            for (a=0; a<hardext.maxtex; a++) {
                cur->pscoordreplace[a] = glstate->texture.pscoordreplace[a];
            }
        }
    }

    if (mask & GL_POLYGON_BIT) {
        cur->cull_face = gl4es_glIsEnabled(GL_CULL_FACE);
        gl4es_glGetIntegerv(GL_CULL_FACE_MODE, &cur->cull_face_mode);
        gl4es_glGetIntegerv(GL_FRONT_FACE, &cur->front_face);
        cur->polygon_mode = glstate->polygon_mode;  // 0 means GL_FILL here
        cur->polygon_offset_fill = gl4es_glIsEnabled(GL_POLYGON_OFFSET_FILL);
        //TODO: GL_POLYGON_SMOOTH & GL_POLYGON_STIPPLE enables (not tracked)
        //TODO: GL_POLYGON_OFFSET_LINE/POINT enables, offset factor & units (not shadowed)
    }
    // TODO: GL_POLYGON_STIPPLE_BIT

    if (mask & GL_SCISSOR_BIT) {
        cur->scissor_test = gl4es_glIsEnabled(GL_SCISSOR_TEST);
        gl4es_glGetFloatv(GL_SCISSOR_BOX, cur->scissor_box);
    }

    // TODO: GL_STENCIL_BUFFER_BIT on both faces
    if (mask & GL_STENCIL_BUFFER_BIT) {
        cur->stencil_test = gl4es_glIsEnabled(GL_STENCIL_TEST);
        gl4es_glGetIntegerv(GL_STENCIL_FUNC, (GLint *) &cur->stencil_func);
        gl4es_glGetIntegerv(GL_STENCIL_VALUE_MASK, (GLint *) &cur->stencil_mask);
        gl4es_glGetIntegerv(GL_STENCIL_REF, (GLint *) &cur->stencil_ref);
        //TODO: glStencilFuncSeperate
        
        //TODO: Stencil value mask
        gl4es_glGetIntegerv(GL_STENCIL_FAIL, (GLint *) &cur->stencil_sfail);
        gl4es_glGetIntegerv(GL_STENCIL_PASS_DEPTH_FAIL, (GLint *) &cur->stencil_dpfail);
        gl4es_glGetIntegerv(GL_STENCIL_PASS_DEPTH_PASS, (GLint *) &cur->stencil_dppass);
        //TODO: glStencilOpSeparate

        gl4es_glGetIntegerv(GL_STENCIL_CLEAR_VALUE, &cur->stencil_clearvalue);
        //TODO: Stencil buffer writemask
    }
    // GL_TEXTURE_BIT - TODO: incomplete
    if (mask & GL_TEXTURE_BIT) {
        cur->active=glstate->texture.active;
        int a;
        for (a=0; a<hardext.maxtex; a++) {
            cur->texgen_r[a] = glstate->enable.texgen_r[a];
            cur->texgen_s[a] = glstate->enable.texgen_s[a];
            cur->texgen_t[a] = glstate->enable.texgen_t[a];
            cur->texgen_q[a] = glstate->enable.texgen_q[a];
            cur->texgen[a] = glstate->texgen[a];   // all mode and planes per texture in 1 line
            cur->texenv_mode[a] = glstate->texenv[a].env.mode;
            memcpy(cur->texenv_color[a], glstate->texenv[a].env.color, 4*sizeof(GLfloat));
            for (int j=0; j<ENABLED_TEXTURE_LAST; j++)
	            cur->texture[a][j] = glstate->texture.bound[a][j]->texture;
        }
        //glActiveTexture(GL_TEXTURE0+cur->active);
    }

    // GL_TRANSFORM_BIT
    if (mask & GL_TRANSFORM_BIT) {
		if (!(mask & GL_ENABLE_BIT)) {
			int i;
			cur->clip_planes_enabled = (GLboolean *)malloc(hardext.maxplanes * sizeof(GLboolean));
			for (i = 0; i < hardext.maxplanes; i++) {
				*(cur->clip_planes_enabled + i) = gl4es_glIsEnabled(GL_CLIP_PLANE0 + i);
			}
		}
		gl4es_glGetIntegerv(GL_MATRIX_MODE, (GLint *) &cur->matrix_mode);
		cur->rescale_normal_flag = gl4es_glIsEnabled(GL_RESCALE_NORMAL);
		cur->normalize_flag = gl4es_glIsEnabled(GL_NORMALIZE);
		// eye-space equations, as stored by gl4es_glClipPlanef's ES2 path
		cur->clip_planes = (GLfloat *)malloc(hardext.maxplanes * 4 * sizeof(GLfloat));
		for (int i = 0; i < hardext.maxplanes; i++)
			memcpy(cur->clip_planes + i*4, glstate->planes[i], 4*sizeof(GLfloat));
	}
    // GL_VIEWPORT_BIT
    if (mask & GL_VIEWPORT_BIT) {
		gl4es_glGetIntegerv(GL_VIEWPORT, cur->viewport_size);
		gl4es_glGetFloatv(GL_DEPTH_RANGE, cur->depth_range);
	}
		
    glstate->stack->len++;
}

void APIENTRY_GL4ES gl4es_glPushClientAttrib(GLbitfield mask) {
    DBG(printf("glPushClientAttrib(0x%04X)\n", mask);)
    noerrorShim();
    if (glstate->clientStack == NULL) {
        glstate->clientStack = (glclientstack_t *)malloc(STACK_SIZE * sizeof(glclientstack_t));
        glstate->clientStack->len = 0;
        glstate->clientStack->cap = STACK_SIZE;
    } else if (glstate->clientStack->len == glstate->clientStack->cap) {
        glstate->clientStack->cap += STACK_SIZE;
        glstate->clientStack = (glclientstack_t *)realloc(glstate->clientStack, glstate->clientStack->cap * sizeof(glclientstack_t));
    }

    glclientstack_t *cur = glstate->clientStack + glstate->clientStack->len;
    cur->mask = mask;

    if (mask & GL_CLIENT_PIXEL_STORE_BIT) {
        gl4es_glGetIntegerv(GL_PACK_ALIGNMENT, &cur->pack_align);
        gl4es_glGetIntegerv(GL_UNPACK_ALIGNMENT, &cur->unpack_align);
        cur->unpack_row_length = glstate->texture.unpack_row_length;
        cur->unpack_skip_pixels = glstate->texture.unpack_skip_pixels;
        cur->unpack_skip_rows = glstate->texture.unpack_skip_rows;
        cur->pack_row_length = glstate->texture.pack_row_length;
        cur->pack_skip_pixels = glstate->texture.pack_skip_pixels;
        cur->pack_skip_rows = glstate->texture.pack_skip_rows;
    }

    if (mask & GL_CLIENT_VERTEX_ARRAY_BIT) {
        memcpy(cur->vertexattrib, glstate->vao->vertexattrib, sizeof(glstate->vao->vertexattrib));
        cur->client = glstate->texture.client;
    }

    glstate->clientStack->len++;
}

#define maybe_free(x) \
    if (x) free(x)

#define enable_disable(pname, enabled) \
    if (enabled) gl4es_glEnable(pname);      \
    else gl4es_glDisable(pname)

#define v2(c) c[0], c[1]
#define v3(c) v2(c), c[2]
#define v4(c) v3(c), c[3]

void APIENTRY_GL4ES gl4es_glPopAttrib(void) {
DBG(printf("glPopAttrib()\n");)
    noerrorShim();
    if (glstate->list.active)
        if (glstate->list.compiling) {
            NewStage(glstate->list.active, STAGE_POP);
		    glstate->list.active->popattribute = true;
		    return;
        } else gl4es_flush();

    if (glstate->stack == NULL || glstate->stack->len == 0) {
        errorShim(GL_STACK_UNDERFLOW);
        return;
    }

    glstack_t *cur = glstate->stack + glstate->stack->len-1;

    if (cur->mask & GL_COLOR_BUFFER_BIT) {
        enable_disable(GL_ALPHA_TEST, cur->alpha_test);
        gl4es_glAlphaFunc(cur->alpha_test_func, cur->alpha_test_ref);

        enable_disable(GL_BLEND, cur->blend);
        gl4es_glBlendFunc(cur->blend_src_func, cur->blend_dst_func);

        enable_disable(GL_DITHER, cur->dither);
        enable_disable(GL_COLOR_LOGIC_OP, cur->color_logic_op);
        gl4es_glLogicOp(cur->logic_op);

        gl4es_glClearColor(v4(cur->clear_color));
        gl4es_glColorMask(v4(cur->color_mask));
    }

    if (cur->mask & GL_CURRENT_BIT) {
        gl4es_glColor4f(v4(cur->color));
        gl4es_glNormal3f(v3(cur->normal));
        gl4es_glTexCoord4f(v4(cur->tex));
    }

    if (cur->mask & GL_DEPTH_BUFFER_BIT) {
        enable_disable(GL_DEPTH_TEST, cur->depth_test);
        gl4es_glDepthFunc(cur->depth_func);
        gl4es_glClearDepth(cur->clear_depth);
        gl4es_glDepthMask(cur->depth_mask);
    }

    if (cur->mask & GL_ENABLE_BIT) {
        int i;

        enable_disable(GL_ALPHA_TEST, cur->alpha_test);
        enable_disable(GL_AUTO_NORMAL, cur->autonormal);
        enable_disable(GL_BLEND, cur->blend);

        for (i = 0; i < hardext.maxplanes; i++) {
            enable_disable(GL_CLIP_PLANE0 + i, *(cur->clip_planes_enabled + i));
        }

        enable_disable(GL_COLOR_MATERIAL, cur->colormaterial);
        enable_disable(GL_CULL_FACE, cur->cull_face);
        enable_disable(GL_DEPTH_TEST, cur->depth_test);
        enable_disable(GL_DITHER, cur->dither);
        enable_disable(GL_FOG, cur->fog);

        for (i = 0; i < hardext.maxlights; i++) {
            enable_disable(GL_LIGHT0 + i, *(cur->lights_enabled + i));
        }

        enable_disable(GL_LIGHTING, cur->lighting);
        enable_disable(GL_LINE_SMOOTH, cur->line_smooth);
        enable_disable(GL_LINE_STIPPLE, cur->line_stipple);
        enable_disable(GL_COLOR_LOGIC_OP, cur->color_logic_op);
        //TODO: GL_INDEX_LOGIC_OP
        //TODO: GL_MAP1_x
        //TODO: GL_MAP2_x
        enable_disable(GL_MULTISAMPLE, cur->multisample);
        enable_disable(GL_NORMALIZE, cur->normalize);
        enable_disable(GL_POINT_SMOOTH, cur->point_smooth);
        //TODO: GL_POLYGON_OFFSET_LINE
        enable_disable(GL_POLYGON_OFFSET_FILL, cur->polygon_offset_fill);
        //TODO: GL_POLYGON_OFFSET_POINT
        //TODO: GL_POLYGON_SMOOTH
        //TODO: GL_POLYGON_STIPPLE
        enable_disable(GL_SAMPLE_ALPHA_TO_COVERAGE, cur->sample_alpha_to_coverage);
        enable_disable(GL_SAMPLE_ALPHA_TO_ONE, cur->sample_alpha_to_one);
        enable_disable(GL_SAMPLE_COVERAGE, cur->sample_coverage);
        enable_disable(GL_SCISSOR_TEST, cur->scissor_test);
        enable_disable(GL_STENCIL_TEST, cur->stencil_test);
        enable_disable(GL_POINT_SPRITE, cur->pointsprite);
        int a;
        int old_tex = glstate->texture.active;
        for (a=0; a<hardext.maxtex; a++) {
            if(glstate->enable.texture[a] != cur->tex_enabled[a]) {
                for (int j=0; j<ENABLED_TEXTURE_LAST; j++) {
                    const GLuint t = cur->tex_enabled[a] & (1<<j);
                    if ((glstate->enable.texture[a] & (1<<j)) != t) {
                        if(glstate->texture.active!=a)
                            gl4es_glActiveTexture(GL_TEXTURE0+a);
                        enable_disable(to_target(j), t); 
                    }
                }
            }
            glstate->enable.texgen_r[a] = cur->texgen_r[a];
            glstate->enable.texgen_s[a] = cur->texgen_s[a];
            glstate->enable.texgen_t[a] = cur->texgen_t[a];
            glstate->enable.texgen_q[a] = cur->texgen_q[a];
        }
        if (glstate->texture.active != old_tex) gl4es_glActiveTexture(GL_TEXTURE0+old_tex);
    }

    if (cur->mask & GL_FOG_BIT) {
        enable_disable(GL_FOG, cur->fog);
        gl4es_glFogfv(GL_FOG_COLOR, cur->fog_color);
        gl4es_glFogf(GL_FOG_DENSITY, cur->fog_density);
        gl4es_glFogf(GL_FOG_START, cur->fog_start);
        gl4es_glFogf(GL_FOG_END, cur->fog_end);
        gl4es_glFogf(GL_FOG_MODE, cur->fog_mode);
    }

    if (cur->mask & GL_HINT_BIT) {
        gl4es_glHint(GL_PERSPECTIVE_CORRECTION_HINT, cur->perspective_hint);
        gl4es_glHint(GL_POINT_SMOOTH_HINT, cur->point_smooth_hint);
        gl4es_glHint(GL_LINE_SMOOTH_HINT, cur->line_smooth_hint);
        gl4es_glHint(GL_FOG_HINT, cur->fog_hint);
        gl4es_glHint(GL_GENERATE_MIPMAP_HINT, cur->mipmap_hint);
        for (int i=GL4ES_HINT_FIRST; i<GL4ES_HINT_LAST; i++)
            gl4es_glHint(i, cur->gles4_hint[i-GL4ES_HINT_FIRST]);
    }

    if (cur->mask & GL_LIGHTING_BIT) {
        enable_disable(GL_LIGHTING, cur->lighting);
        gl4es_glLightModelfv(GL_LIGHT_MODEL_AMBIENT, cur->light_model_ambient);
        gl4es_glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, cur->light_model_two_side);

        int i;
        int j=0;
        int old_matrixmode = glstate->matrix_mode;
        // Light position / direction is transformed. So load identity in modelview to restore correct stuff
        int identity = is_identity(getMVMat());
        if(!identity) {
            if(old_matrixmode != GL_MODELVIEW) gl4es_glMatrixMode(GL_MODELVIEW);
            gl4es_glPushMatrix();
            gl4es_glLoadIdentity();
        }
        for (i = 0; i < hardext.maxlights; i++) {
            enable_disable(GL_LIGHT0 + i, *(cur->lights_enabled + i));
            #define L(A) gl4es_glLightfv(GL_LIGHT0 + i, A, cur->lights+j); j+=4
            L(GL_AMBIENT);
            L(GL_DIFFUSE);
            L(GL_SPECULAR);
            L(GL_POSITION); 
            L(GL_SPOT_CUTOFF);
            L(GL_SPOT_DIRECTION);
            L(GL_SPOT_EXPONENT);
            L(GL_CONSTANT_ATTENUATION);
            L(GL_LINEAR_ATTENUATION);
            L(GL_QUADRATIC_ATTENUATION);
            #undef L
        }
        if(!identity) {
            gl4es_glPopMatrix();
            if(old_matrixmode != GL_MODELVIEW) gl4es_glMatrixMode(old_matrixmode);
        }
        j=0;
        #define M(A) if(memcmp(cur->materials+j, cur->materials+j+4, 4*sizeof(GLfloat))==0) \
            {gl4es_glMaterialfv(GL_FRONT_AND_BACK, A, cur->materials+j); j+=8;} \
            else \
            {gl4es_glMaterialfv(GL_BACK, A, cur->materials+j); j+=4; gl4es_glMaterialfv(GL_FRONT, A, cur->materials+j); j+=4;}
        M(GL_AMBIENT); M(GL_DIFFUSE); M(GL_SPECULAR); M(GL_EMISSION); M(GL_SHININESS);  // handle both face at some point?
        #undef M

        gl4es_glShadeModel(cur->shade_model);
    }

	// GL_LIST_BIT
    if (cur->mask & GL_LIST_BIT) {
        gl4es_glListBase(cur->list_base);
    }

    if (cur->mask & GL_LINE_BIT) {
        enable_disable(GL_LINE_SMOOTH, cur->line_smooth);
        enable_disable(GL_LINE_STIPPLE, cur->line_stipple);
        // only on a change: the first gl4es_glLineStipple call creates the
        // stipple emulation texture, even for the default factor/pattern
        if (cur->line_stipple_factor != glstate->linestipple.factor
         || cur->line_stipple_pattern != glstate->linestipple.pattern)
            gl4es_glLineStipple(cur->line_stipple_factor, cur->line_stipple_pattern);
        gl4es_glLineWidth(cur->line_width);
    }

    if (cur->mask & GL_MULTISAMPLE_BIT) {
        enable_disable(GL_MULTISAMPLE, cur->multisample);
        enable_disable(GL_SAMPLE_ALPHA_TO_COVERAGE, cur->sample_alpha_to_coverage);
        enable_disable(GL_SAMPLE_ALPHA_TO_ONE, cur->sample_alpha_to_one);
        enable_disable(GL_SAMPLE_COVERAGE, cur->sample_coverage);
    }

    if (cur->mask & GL_POINT_BIT) {
        enable_disable(GL_POINT_SMOOTH, cur->point_smooth);
        gl4es_glPointSize(cur->point_size);
        gl4es_glPointParameterf(GL_POINT_SIZE_MIN, cur->point_size_min);
        gl4es_glPointParameterf(GL_POINT_SIZE_MAX, cur->point_size_max);
        gl4es_glPointParameterf(GL_POINT_FADE_THRESHOLD_SIZE, cur->point_fade_threshold);
        gl4es_glPointParameterfv(GL_POINT_DISTANCE_ATTENUATION, cur->point_distance_attenuation);
        if(hardext.pointsprite) {
            enable_disable(GL_POINT_SPRITE, cur->pointsprite);
            int old_tex = glstate->texture.active;
            int a;
            for (a=0; a<hardext.maxtex; a++) {
                if(glstate->texture.pscoordreplace[a]!=cur->pscoordreplace[a]) {
                    if(glstate->texture.active!=a)
                        gl4es_glActiveTexture(GL_TEXTURE0+a);
                    gl4es_glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, cur->pscoordreplace[a]);
                }
            }
            if (glstate->texture.active!= old_tex) gl4es_glActiveTexture(GL_TEXTURE0+old_tex);
        }
    }

    if (cur->mask & GL_POLYGON_BIT) {
        enable_disable(GL_CULL_FACE, cur->cull_face);
        gl4es_glCullFace(cur->cull_face_mode);
        gl4es_glFrontFace(cur->front_face);
        gl4es_glPolygonMode(GL_FRONT_AND_BACK, cur->polygon_mode?cur->polygon_mode:GL_FILL);
        enable_disable(GL_POLYGON_OFFSET_FILL, cur->polygon_offset_fill);
    }

    if (cur->mask & GL_SCISSOR_BIT) {
        enable_disable(GL_SCISSOR_TEST, cur->scissor_test);
        gl4es_glScissor(v4(cur->scissor_box));
    }

    if (cur->mask & GL_STENCIL_BUFFER_BIT) {
        enable_disable(GL_STENCIL_TEST, cur->stencil_test);
        gl4es_glStencilFunc(cur->stencil_func, cur->stencil_ref, cur->stencil_mask);
        //TODO: Stencil value mask
        gl4es_glStencilOp(cur->stencil_sfail, cur->stencil_dpfail, cur->stencil_dppass);
        gl4es_glClearStencil(cur->stencil_clearvalue);
        //TODO: Stencil buffer writemask
    }

    if (cur->mask & GL_TEXTURE_BIT) {
        int old_tex = glstate->texture.active;
        int a;
        //TODO: Enable bit for the 4 texture coordinates
        for (a=0; a<hardext.maxtex; a++) {
            glstate->enable.texgen_r[a] = cur->texgen_r[a];
            glstate->enable.texgen_s[a] = cur->texgen_s[a];
            glstate->enable.texgen_t[a] = cur->texgen_t[a];
            glstate->enable.texgen_q[a] = cur->texgen_q[a];
            glstate->texgen[a] = cur->texgen[a];   // all mode and planes per texture in 1 line
            // restore through gl4es_glTexEnv* (not a struct copy): they also
            // refresh the fpe_state bits the shader generator reads
            if (cur->texenv_mode[a] != glstate->texenv[a].env.mode) {
                if(glstate->texture.active!=a)
                    gl4es_glActiveTexture(GL_TEXTURE0+a);
                gl4es_glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, cur->texenv_mode[a]);
            }
            if (memcmp(cur->texenv_color[a], glstate->texenv[a].env.color, 4*sizeof(GLfloat)) != 0) {
                if(glstate->texture.active!=a)
                    gl4es_glActiveTexture(GL_TEXTURE0+a);
                gl4es_glTexEnvfv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, cur->texenv_color[a]);
            }
            for (int j=0; j<ENABLED_TEXTURE_LAST; j++)
                if (cur->texture[a][j] != glstate->texture.bound[a][j]->texture) {
                    if(glstate->texture.active!=a)
                        gl4es_glActiveTexture(GL_TEXTURE0+a);
                    gl4es_glBindTexture(to_target(j), cur->texture[a][j]);
                }
        }
        if (glstate->texture.active!= old_tex) gl4es_glActiveTexture(GL_TEXTURE0+old_tex);
    }
    
	if (cur->mask & GL_PIXEL_MODE_BIT) {
		GLenum pixel_name[] = {GL_RED_BIAS, GL_RED_SCALE, GL_GREEN_BIAS, GL_GREEN_SCALE, GL_BLUE_BIAS, GL_BLUE_SCALE, GL_ALPHA_BIAS, GL_ALPHA_SCALE};
		int i;
		for (i=0; i<8; i++) 
			gl4es_glPixelTransferf(pixel_name[i], cur->pixel_scale_bias[i]);
        //TODO: GL_DEPTH_BIAS & GL_DEPTH_SCALE (probably difficult)
        //TODO: GL_INDEX_OFFEST & GL_INDEX_SHIFT
        //TODO: GL_MAP_COLOR & GL_MAP_STENCIL (probably difficult too)
		gl4es_glPixelZoom(cur->pixel_zoomx, cur->pixel_zoomy);
	}

	if (cur->mask & GL_TRANSFORM_BIT) {
		if (!(cur->mask & GL_ENABLE_BIT)) {
			int i;
			for (i = 0; i < hardext.maxplanes; i++) {
				enable_disable(GL_CLIP_PLANE0 + i, *(cur->clip_planes_enabled + i));
			}
		}
		gl4es_glMatrixMode(cur->matrix_mode);
		enable_disable(GL_NORMALIZE, cur->normalize_flag);		
		enable_disable(GL_RESCALE_NORMAL, cur->rescale_normal_flag);		
		if (cur->clip_planes) {
			// restore the stored eye-space equations directly: going through
			// gl4es_glClipPlanef would re-transform by the current modelview
			for (int i = 0; i < hardext.maxplanes; i++)
				memcpy(glstate->planes[i], cur->clip_planes + i*4, 4*sizeof(GLfloat));
		}
	}

    if (cur->mask & GL_VIEWPORT_BIT) {
		gl4es_glViewport(cur->viewport_size[0], cur->viewport_size[1], cur->viewport_size[2], cur->viewport_size[3]);
		gl4es_glDepthRangef(cur->depth_range[0], cur->depth_range[1]);
	}
	
    maybe_free(cur->clip_planes_enabled);
    maybe_free(cur->clip_planes);
    maybe_free(cur->lights_enabled);
    maybe_free(cur->lights);
    maybe_free(cur->materials);
    glstate->stack->len--;
}

#undef enable_disable
#define enable_disable(pname, enabled)             \
    if (enabled) gl4es_glEnableClientState(pname);       \
    else gl4es_glDisableClientState(pname)

void APIENTRY_GL4ES gl4es_glPopClientAttrib(void) {
    DBG(printf("glPopClientAttrib()\n");)
    noerrorShim();
	//LOAD_GLES(glVertexPointer);
	//LOAD_GLES(glColorPointer);
	//LOAD_GLES(glNormalPointer);
	//LOAD_GLES(glTexCoordPointer);

    if (glstate->clientStack == NULL || glstate->clientStack->len == 0) {
        errorShim(GL_STACK_UNDERFLOW);
        return;
    }

    glclientstack_t *cur = glstate->clientStack + glstate->clientStack->len-1;
    if (cur->mask & GL_CLIENT_PIXEL_STORE_BIT) {
        gl4es_glPixelStorei(GL_PACK_ALIGNMENT, cur->pack_align);
        gl4es_glPixelStorei(GL_UNPACK_ALIGNMENT, cur->unpack_align);
        gl4es_glPixelStorei(GL_UNPACK_ROW_LENGTH, cur->unpack_row_length);
        gl4es_glPixelStorei(GL_UNPACK_SKIP_PIXELS, cur->unpack_skip_pixels);
        gl4es_glPixelStorei(GL_UNPACK_SKIP_ROWS, cur->unpack_skip_rows);
        gl4es_glPixelStorei(GL_PACK_ROW_LENGTH, cur->pack_row_length);
        gl4es_glPixelStorei(GL_PACK_SKIP_PIXELS, cur->pack_skip_pixels);
        gl4es_glPixelStorei(GL_PACK_SKIP_ROWS, cur->pack_skip_rows);
    }

    if (cur->mask & GL_CLIENT_VERTEX_ARRAY_BIT) {
        memcpy(glstate->vao->vertexattrib, cur->vertexattrib, sizeof(glstate->vao->vertexattrib));
		if (glstate->texture.client != cur->client) gl4es_glClientActiveTexture(GL_TEXTURE0+cur->client);
    }

    glstate->clientStack->len--;
}

#undef maybe_free
#undef enable_disable
#undef v2
#undef v3
#undef v4

//Direct wrapper
AliasExport(void,glPushClientAttrib,,(GLbitfield mask));
AliasExport_V(void,glPopClientAttrib);
AliasExport(void,glPushAttrib,,(GLbitfield mask));
AliasExport_V(void,glPopAttrib);