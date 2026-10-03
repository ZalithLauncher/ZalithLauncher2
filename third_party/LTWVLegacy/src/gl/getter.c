#include <gl4eshint.h>
#include "../glx/hardext.h"
#include "debug.h"
#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "loader.h"
#include "light.h"
#include "matvec.h"
#include "texgen.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

GLenum APIENTRY_GL4ES gl4es_glGetError(void) {
    DBG(printf("glGetError(), noerror=%d, type_error=%d shim_error=%s\n", globals4es.noerror, glstate->type_error, PrintEnum(glstate->shim_error));)
    if(globals4es.noerror)
        return GL_NO_ERROR;
	LOAD_GLES(glGetError);
    GLenum err = GL_NO_ERROR;
    if(!glstate->type_error) {
        // check glGetError, forget everything else
        err = gles_glGetError();
        // If no error, then check "shim" error
        if(err==GL_NO_ERROR)
            err = glstate->shim_error;
    } else {
        err = glstate->shim_error;
	}
    if(glstate->type_error==1)
        gles_glGetError();  // purge error log
    glstate->type_error = 2;
    glstate->shim_error = GL_NO_ERROR;

	return err;
}
AliasExport(GLenum,glGetError,,());

void APIENTRY_GL4ES gl4es_glGetPointerv(GLenum pname, GLvoid* *params) {
    DBG(printf("glGetPointerv(%s, %p)\n", PrintEnum(pname), params);)
    noerrorShim();
    switch(pname) {
        case GL_COLOR_ARRAY_POINTER:
            *params = (void*)glstate->vao->vertexattrib[ATT_COLOR].pointer;
            break;
        case GL_EDGE_FLAG_ARRAY_POINTER:
            *params = NULL;
            break;
        case GL_FEEDBACK_BUFFER_POINTER:
            *params = NULL;
            break;
        case GL_INDEX_ARRAY_POINTER:
            *params = NULL;
        case GL_NORMAL_ARRAY_POINTER:
            *params = (void*)glstate->vao->vertexattrib[ATT_NORMAL].pointer;
            break;
        case GL_TEXTURE_COORD_ARRAY_POINTER:
            *params = (void*)glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+glstate->texture.client].pointer;
            break;
        case GL_SELECTION_BUFFER_POINTER:
            *params = glstate->selectbuf.buffer;
            break;
        case GL_VERTEX_ARRAY_POINTER :
            *params = (void*)glstate->vao->vertexattrib[ATT_VERTEX].pointer;
            break;
        case GL_FOG_COORD_ARRAY:
            *params = (void*)glstate->vao->vertexattrib[ATT_FOGCOORD].pointer;
            break;
        case GL_SECONDARY_COLOR_ARRAY:
            *params = (void*)glstate->vao->vertexattrib[ATT_SECONDARY].pointer;
            break;
    default:
            errorShim(GL_INVALID_ENUM);
    }
}
AliasExport(void,glGetPointerv,,(GLenum pname, GLvoid* *params));

void BuildExtensionsList() {
	if(!glstate->extensions) {
		glstate->extensions = (GLubyte*)malloc(5000);	// arbitrary size...
        char *extensions = (char *) glstate->extensions;
		strcpy(extensions,
				"GL_EXT_abgr "
                #ifdef AMIGAOS4
                "GL_MGL_packed_pixels " // same as GL_EXT_packed_pixels, but older, some old Amiga games may check for this
                #endif
                "GL_EXT_packed_pixels "
                "GL_EXT_compiled_vertex_array "
                "GL_EXT_compiled_vertex_arrays " // yes, at least on AmigaOS there are progs which check for this wrong string; reason is an old typo in the MiniGL driver.
                "GL_ARB_vertex_buffer_object "
                "GL_ARB_vertex_array_object "
                "GL_ARB_vertex_buffer "
                "GL_EXT_vertex_array "
                "GL_EXT_secondary_color "
                "GL_ARB_multitexture "
                "GL_ARB_texture_border_clamp "
                "GL_ARB_texture_env_add "
                "GL_EXT_texture_env_add "
                "GL_ARB_texture_env_combine "
                "GL_EXT_texture_env_combine "
                "GL_ARB_texture_env_crossbar "
                "GL_EXT_texture_env_crossbar "
                "GL_ARB_texture_env_dot3 "
                "GL_EXT_texture_env_dot3 "
                "GL_SGIS_generate_mipmap "
                "GL_EXT_draw_range_elements "
                "GL_EXT_bgra "
                "GL_ARB_texture_compression "
                "GL_EXT_texture_compression_s3tc "
                "GL_OES_texture_compression_S3TC "
                "GL_EXT_texture_compression_dxt1 "
                "GL_EXT_texture_compression_dxt3 "
                "GL_EXT_texture_compression_dxt5 "
                "GL_ARB_point_parameters "
                "GL_EXT_point_parameters "
                "GL_EXT_stencil_wrap "
                "GL_SGIS_texture_edge_clamp "
                "GL_EXT_texture_edge_clamp "
                "GL_EXT_direct_state_access "
                "GL_EXT_multi_draw_arrays "
                "GL_SUN_multi_draw_arrays "
                "GL_ARB_multisample "
                "GL_EXT_texture_object "
                "GL_EXT_polygon_offset "
                "GL_GL4ES_hint "
                "GL_ARB_draw_elements_base_vertex "
                "GL_EXT_draw_elements_base_vertex "
                "GL_ARB_map_buffer_range "
                "GL_NV_blend_square "
//                "GL_EXT_blend_logic_op "
				);
        if(!globals4es.notexrect)
            strcat(extensions, "GL_ARB_texture_rectangle ");
        if(globals4es.vabgra)
            strcat(extensions, "GL_ARB_vertex_array_bgra ");
		if(globals4es.npot>=1)
			strcat(extensions, "GL_APPLE_texture_2D_limited_npot ");
		if(globals4es.npot>=2)
			strcat(extensions, "GL_ARB_texture_non_power_of_two ");
        if(hardext.blendcolor)
            strcat(extensions, "GL_EXT_blend_color ");
        if(hardext.blendminmax)
            strcat(extensions, "GL_EXT_blend_minmax ");
        if(hardext.blendeq)
            strcat(extensions, "GL_EXT_blend_equation_separate ");
        if(hardext.blendfunc)
            strcat(extensions, "GL_EXT_blend_func_separate ");
        if(hardext.blendsub)
            strcat(extensions, "GL_EXT_blend_subtract ");
        if(hardext.aniso)
            strcat(extensions, "GL_EXT_texture_filter_anisotropic ");
        if(hardext.mirrored)
            strcat(extensions, "GL_ARB_texture_mirrored_repeat ");
        if(hardext.fbo)
            strcat(extensions,                 
                "GL_ARB_framebuffer_object "
                "GL_EXT_framebuffer_object "
                "GL_EXT_packed_depth_stencil "
                "GL_EXT_framebuffer_blit "
                "GL_ARB_draw_buffers "
                "GL_EXT_draw_buffers2 "
                );
        if(hardext.pointsprite)
            strcat(extensions, "GL_ARB_point_sprite ");
        if(hardext.cubemap) {
            strcat(extensions, "GL_ARB_texture_cube_map ");
            strcat(extensions, "GL_EXT_texture_cube_map ");
        }
        if(hardext.rgtex) {
            strcat(extensions, "GL_EXT_texture_rg ");
            strcat(extensions, "GL_ARB_texture_rg ");
        }
        if(hardext.floattex || (globals4es.floattex==2)) {
            strcat(extensions, "GL_EXT_texture_float ");
            strcat(extensions, "GL_ARB_texture_float ");
        }
        if(hardext.halffloattex || (globals4es.floattex==2)) {
            strcat(extensions, "GL_EXT_texture_half_float ");
        }
        if(hardext.floatfbo || (globals4es.floattex==2)) {
            strcat(extensions, "GL_EXT_color_buffer_float ");
        }
        if(hardext.halffloatfbo || (globals4es.floattex==2)) {
            strcat(extensions, "GL_EXT_color_buffer_half_float ");
        }
        if(hardext.depthtex) {
            strcat(extensions, "GL_EXT_depth_texture ");
            strcat(extensions, "GL_ARB_depth_texture ");
        }
        if(hardext.esversion>1) {
            strcat(extensions, "GL_EXT_fog_coord ");
            strcat(extensions, "GL_EXT_separate_specular_color ");
            strcat(extensions, "GL_EXT_rescale_normal ");
            strcat(extensions, "GL_ARB_ES2_compatibility ");
            strcat(extensions,
                "GL_ARB_fragment_shader "
                "GL_ARB_vertex_shader "
                "GL_ARB_shader_objects "
                "GL_ARB_shading_language_100 "
                "GL_ATI_texture_env_combine3 "
                "GL_ATIX_texture_env_route "
                "GL_NV_texture_env_combine4 "
                "GL_NV_fog_distance "
                "GL_ARB_draw_instanced "
                "GL_ARB_instanced_arrays "
                );
            if(!globals4es.noarbprogram)
                strcat(extensions,
                "GL_ARB_vertex_program "
                "GL_ARB_fragment_program "
                "GL_EXT_program_parameters "
                );
        }
        if(hardext.prgbin_n) {
            strcat(extensions,
            "GL_ARB_get_program_binary "
            );
        }
        char *p = extensions;
        glstate->num_extensions = 0;
        // quickly count extensions. Each one is separated by space...
        while ((p=strchr(p, ' '))) { while(*(p)==' ') ++p; glstate->num_extensions++; }
        // and now split in array of individual extensions
        // TODO: is all this better be moved in glstate?
        glstate->extensions_list = (GLubyte**)calloc(glstate->num_extensions, sizeof(GLubyte*));
        p = extensions;
        for (int i=0; i<glstate->num_extensions; i++) {
            char* p2 = strchr(p, ' ');
            int sz = p2 - p;
            glstate->extensions_list[i] = (GLubyte*)calloc(sz+1, sizeof(GLubyte));
            strncpy((char *) glstate->extensions_list[i], p, sz);
            while(*p2==' ') ++p2;
            p = p2;
        }
    }
}

static char renderer_string[128] = {0};
static char vendor_string[128] = {0};
extern char* gl4es_original_vendor;
extern char* gl4es_original_renderer;

const GLubyte* APIENTRY_GL4ES gl4es_glGetString(GLenum name) {
    DBG(printf("glGetString(%s)\n", PrintEnum(name));)
    errorShim(GL_NO_ERROR);
    switch (name) {
        case GL_VERSION:
            return (GLubyte *)globals4es.version;
        case GL_EXTENSIONS:
            BuildExtensionsList();
            return glstate->extensions;
		case GL_VENDOR:
            if(!vendor_string[0]) {
                snprintf(vendor_string, 127, "GL4ES wrapping %s", gl4es_original_vendor?gl4es_original_vendor:"an unknown hardware");
            }
			return (GLubyte *)vendor_string;
		case GL_RENDERER:
            if(!renderer_string[0]) {
                snprintf(renderer_string, 127, "GL4ES using %s", gl4es_original_renderer?gl4es_original_renderer:"an unknown renderer");
            }
			return (GLubyte *)renderer_string;
		case GL_SHADING_LANGUAGE_VERSION:
            if(globals4es.gl==21)
            return (GLubyte *)"1.20 via gl4es";
            else if(globals4es.gl==20)
                return (GLubyte *)"1.10 via gl4es";
			return (GLubyte *)"";
        case GL_PROGRAM_ERROR_STRING_ARB:
            return (GLubyte*)glstate->glsl->error_msg;
        default:
            if(name&0x10000) {
                LOAD_GLES(glGetString);
                return gles_glGetString(name-0x10000);
            }
			errorShim(GL_INVALID_ENUM);
            return (GLubyte*)"";
    }
}
AliasExport(const GLubyte*,glGetString,,(GLenum name));

#define TOP(A) (glstate->A->stack+(glstate->A->top*16))

static int getter_viewport4(GLfloat *params) {
    if (!glstate->raster.viewport_known)
        return 0;
    params[0] = (GLfloat)glstate->raster.viewport.x;
    params[1] = (GLfloat)glstate->raster.viewport.y;
    params[2] = (GLfloat)glstate->raster.viewport.width;
    params[3] = (GLfloat)glstate->raster.viewport.height;
    return 1;
}

static int getter_scissor4(GLfloat *params) {
    if (!glstate->raster.scissor_known)
        return 0;
    params[0] = (GLfloat)glstate->raster.scissor.x;
    params[1] = (GLfloat)glstate->raster.scissor.y;
    params[2] = (GLfloat)glstate->raster.scissor.width;
    params[3] = (GLfloat)glstate->raster.scissor.height;
    return 1;
}

int gl4es_commonGet(GLenum pname, GLfloat *params) {
    switch (pname) {
        case GL_MAJOR_VERSION:
            *params = globals4es.gl/10;
            break;
        case GL_MINOR_VERSION:
            *params = globals4es.gl%10;
            break;
        case GL_DOUBLEBUFFER:
            *params = 1;    // Fake double buffering...
            break;
        case GL_MAX_ELEMENTS_INDICES:
            *params = 1024;
            break;
        case GL_MAX_ELEMENTS_VERTICES:
			*params = 4096;
            break;
        case GL_NUM_EXTENSIONS:
            BuildExtensionsList();
            *params = glstate->num_extensions;
            break;
        case GL_AUX_BUFFERS:
            *params = 0;
            break;
        case GL_LINE_WIDTH:
            *params = glstate->line_width;
            break;
        case GL_GENERATE_MIPMAP_HINT:
            *params = glstate->mipmap_hint;
            break;
        case GL_MAX_TEXTURE_UNITS:
            *params = hardext.maxtex;
            break;
        case GL_MAX_TEXTURE_COORDS:
            *params = hardext.maxtex;
            break;
        case GL_PACK_ALIGNMENT:
            *params = glstate->texture.pack_align;
            break;
        case GL_UNPACK_ALIGNMENT:
            *params = glstate->texture.unpack_align;
            break;
        case GL_UNPACK_ROW_LENGTH:	
			*params = glstate->texture.unpack_row_length;
			break;
        case GL_UNPACK_SKIP_PIXELS:
			*params = glstate->texture.unpack_skip_pixels;
			break;
        case GL_UNPACK_SKIP_ROWS:
			*params = glstate->texture.unpack_skip_rows;
			break;
        case GL_UNPACK_LSB_FIRST:
			*params = glstate->texture.unpack_lsb_first;
			break;
        case GL_UNPACK_IMAGE_HEIGHT:
            *params = glstate->texture.unpack_image_height;
            break;
        case GL_PACK_ROW_LENGTH:	
			*params = glstate->texture.pack_row_length;
			break;
        case GL_PACK_SKIP_PIXELS:
			*params = glstate->texture.pack_skip_pixels;
			break;
        case GL_PACK_SKIP_ROWS:
			*params = glstate->texture.pack_skip_rows;
			break;
        case GL_PACK_LSB_FIRST:
			*params = glstate->texture.pack_lsb_first;
			break;
        case GL_PACK_IMAGE_HEIGHT:
            *params = glstate->texture.pack_image_height;
            break;
        case GL_UNPACK_SWAP_BYTES:
        case GL_PACK_SWAP_BYTES:
            //Fake, *TODO* ?
			*params = 0;
			break;
        case GL_ZOOM_X:
	        *params = glstate->raster.raster_zoomx;
	        break;
        case GL_ZOOM_Y:
            *params = glstate->raster.raster_zoomy;
            break;
        case GL_RED_SCALE:
            *params = glstate->raster.raster_scale[0];
            break;
        case GL_RED_BIAS:
            *params = glstate->raster.raster_bias[0];
            break;
        case GL_GREEN_SCALE:
        case GL_BLUE_SCALE:
        case GL_ALPHA_SCALE:
            *params = glstate->raster.raster_scale[(pname-GL_GREEN_SCALE)/2+1];
	    break;
        case GL_GREEN_BIAS:
        case GL_BLUE_BIAS:
        case GL_ALPHA_BIAS:
    	    *params = glstate->raster.raster_bias[(pname-GL_GREEN_BIAS)/2+1];
	    break;
        case GL_MAP_COLOR:
            *params = glstate->raster.map_color;
            break;
        case GL_INDEX_SHIFT:
            *params = glstate->raster.index_shift;
            break;
        case GL_INDEX_OFFSET:
            *params = glstate->raster.index_offset;
            break;
        case GL_PIXEL_MAP_S_TO_S_SIZE:
            *params = 1;
            break;
        case GL_PIXEL_MAP_I_TO_I_SIZE:
            *params = glstate->raster.map_i2i_size;
            break;
        case GL_PIXEL_MAP_I_TO_R_SIZE:
            *params = glstate->raster.map_i2r_size;
            break;
        case GL_PIXEL_MAP_I_TO_G_SIZE:
            *params = glstate->raster.map_i2g_size;
            break;
        case GL_PIXEL_MAP_I_TO_B_SIZE:
            *params = glstate->raster.map_i2b_size;
            break;
        case GL_PIXEL_MAP_I_TO_A_SIZE:
            *params = glstate->raster.map_i2a_size;
            break;
        case GL_PIXEL_MAP_R_TO_R_SIZE:
            *params = 1;
            break;
        case GL_PIXEL_MAP_G_TO_G_SIZE:
            *params = 1;
            break;
        case GL_PIXEL_MAP_B_TO_B_SIZE:
            *params = 1;
            break;
        case GL_PIXEL_MAP_A_TO_A_SIZE:
            *params = 1;
            break;
        case GL_MAX_PIXEL_MAP_TABLE:
            *params = MAX_MAP_SIZE;
            break;
        case GL_RENDER_MODE:
            *params = (glstate->render_mode)?glstate->render_mode:GL_RENDER;
            break;
        case GL_NAME_STACK_DEPTH:
            *params = glstate->namestack.top;
            break;
        case GL_MAX_NAME_STACK_DEPTH:
            *params = 1024;
            break;
        case GL_MAX_TEXTURE_IMAGE_UNITS:
            *params = hardext.maxteximage;
            break;
            case GL_MAX_MODELVIEW_STACK_DEPTH:
            *params=MAX_STACK_MODELVIEW;
            break;
        case GL_MAX_PROJECTION_STACK_DEPTH:
            *params=MAX_STACK_PROJECTION;
            break;
        case GL_MAX_TEXTURE_STACK_DEPTH:
            *params=MAX_STACK_TEXTURE;
            break;
        case GL_MAX_PROGRAM_MATRIX_STACK_DEPTH_ARB:
            *params=MAX_STACK_ARB_MATRIX;
            break;
        case GL_MODELVIEW_STACK_DEPTH:
            *params=(glstate->modelview_matrix)?(glstate->modelview_matrix->top+1):1;
            break;
        case GL_PROJECTION_STACK_DEPTH:
            *params=(glstate->projection_matrix)?(glstate->projection_matrix->top+1):1;
            break;
        case GL_TEXTURE_STACK_DEPTH:
            *params=(glstate->texture_matrix)?(glstate->texture_matrix[glstate->texture.active]->top+1):1;
            break;
        case GL_MAX_LIST_NESTING:
            *params=64;	// fake, no limit in fact
            break;
        case GL_TEXTURE_BINDING_1D:
            *params=glstate->texture.bound[glstate->texture.active][ENABLED_TEX1D]?glstate->texture.bound[glstate->texture.active][ENABLED_TEX1D]->glname:0;
            break;
        case GL_TEXTURE_BINDING_2D:
            *params=glstate->texture.bound[glstate->texture.active][ENABLED_TEX2D]?glstate->texture.bound[glstate->texture.active][ENABLED_TEX2D]->glname:0;
            break;
        case GL_TEXTURE_BINDING_3D:
            *params=glstate->texture.bound[glstate->texture.active][ENABLED_TEX3D]?glstate->texture.bound[glstate->texture.active][ENABLED_TEX3D]->glname:0;
            break;
        case GL_TEXTURE_BINDING_RECTANGLE_ARB:
            *params=glstate->texture.bound[glstate->texture.active][ENABLED_TEXTURE_RECTANGLE]?glstate->texture.bound[glstate->texture.active][ENABLED_TEXTURE_RECTANGLE]->glname:0;
            break;
        case GL_TEXTURE_BINDING_CUBE_MAP:
            *params=glstate->texture.bound[glstate->texture.active][ENABLED_CUBE_MAP]?glstate->texture.bound[glstate->texture.active][ENABLED_CUBE_MAP]->glname:0;
            break;
        case  GL_ARRAY_BUFFER_BINDING:
            *params=(glstate->vao->vertex)?glstate->vao->vertex->buffer:0;
            break;
        case  GL_ELEMENT_ARRAY_BUFFER_BINDING:
            *params=(glstate->vao->elements)?glstate->vao->elements->buffer:0;
            break;
        case  GL_PIXEL_PACK_BUFFER_BINDING:
            *params=(glstate->vao->pack)?glstate->vao->pack->buffer:0;
            break;
        case  GL_PIXEL_UNPACK_BUFFER_BINDING:
            *params=(glstate->vao->unpack)?glstate->vao->unpack->buffer:0;
            break;
        case GL_MAX_TEXTURE_MAX_ANISOTROPY:
            if(hardext.aniso)
                *params=hardext.aniso;
            else {
                *params=0;  // still send 0...
                errorShim(GL_INVALID_ENUM);
            }
            break;
        case GL_MAX_COLOR_ATTACHMENTS:
            if(hardext.fbo)
                *params=hardext.maxcolorattach;
            else
                *params=0;
            break;
        case GL_MAX_DRAW_BUFFERS_ARB:
            if(hardext.fbo)
                *params=hardext.maxdrawbuffers;
            else
                *params=0;
            break;
        case GL_MATRIX_MODE:
            *params=glstate->matrix_mode;
            break;
        case GL_LIGHT_MODEL_TWO_SIDE:
            *params=glstate->light.two_side;
            break;
        case GL_FOG_MODE:
            *params=glstate->fog.mode;
            break;
        case GL_FOG_DENSITY:
            *params=glstate->fog.density;
            break;
        case GL_FOG_DISTANCE_MODE_NV:
            *params=glstate->fog.distance;
            break;
        case GL_FOG_START:
            *params=glstate->fog.start;
            break;
        case GL_FOG_END:
            *params=glstate->fog.end;
            break;
        case GL_FOG_INDEX:
            *params=glstate->fog.start;
            break;
        case GL_FOG_COORD_SRC:
            *params=glstate->fog.coord_src;
            break;
        case GL_CURRENT_FOG_COORD:
            *params=glstate->fogcoord[0];
            break;
        case GL_STENCIL_FUNC:
            *params=glstate->stencil.func[0];
            break;
        case GL_STENCIL_VALUE_MASK:
            *params=glstate->stencil.f_mask[0];
            break;
        case GL_STENCIL_REF:
            *params=glstate->stencil.f_ref[0];
            break;
        case GL_STENCIL_BACK_FUNC:
            *params=glstate->stencil.func[1];
            break;
        case GL_STENCIL_BACK_VALUE_MASK:
            *params=glstate->stencil.f_mask[1];
            break;
        case GL_STENCIL_BACK_REF:
            *params=glstate->stencil.f_ref[1];
            break;
        case GL_STENCIL_WRITEMASK:
            *params=glstate->stencil.mask[0];
            break;
        case GL_STENCIL_BACK_WRITEMASK:
            *params=glstate->stencil.mask[1];
            break;
        case GL_STENCIL_FAIL:
            *params=glstate->stencil.sfail[0];
            break;
        case GL_STENCIL_PASS_DEPTH_FAIL:
            *params=glstate->stencil.dpfail[0];
            break;
        case GL_STENCIL_PASS_DEPTH_PASS:
            *params=glstate->stencil.dppass[0];
            break;
        case GL_STENCIL_BACK_FAIL:
            *params=glstate->stencil.sfail[1];
            break;
        case GL_STENCIL_BACK_PASS_DEPTH_FAIL:
            *params=glstate->stencil.dpfail[1];
            break;
        case GL_STENCIL_BACK_PASS_DEPTH_PASS:
            *params=glstate->stencil.dppass[1];
            break;
        case GL_STENCIL_CLEAR_VALUE:
            *params=glstate->stencil.clear;
            break;
        case GL_MAX_TEXTURE_SIZE:
            *params=hardext.maxsize;
            switch(globals4es.texshrink) {
                case 8:
                case 9:
                case 10:
                    *params*=4;
                    break;
                case 11:
                    *params*=2;
                    break;
            }
            break;
        case GL_MAX_RECTANGLE_TEXTURE_SIZE_ARB:
            *params=hardext.maxsize;
            switch(globals4es.texshrink) {
                case 8:
                case 9:
                case 10:
                    *params*=4;
                    break;
                case 11:
                    *params*=2;
                    break;
            }
            break;
        case GL_SHADE_MODEL:
            *params=glstate->shademodel;
            break;
        case GL_ALPHA_TEST_FUNC:
            *params=glstate->alphafunc;
            break;
        case GL_ALPHA_TEST_REF:
            *params=glstate->alpharef;
            break;
        case GL_LOGIC_OP_MODE:
            *params=glstate->logicop;
            break;
        case GL_BLEND_SRC:
        case GL_BLEND_SRC_RGB:
            *params=glstate->blendsfactorrgb;
            break;
        case GL_BLEND_DST:
        case GL_BLEND_DST_RGB:
            *params=glstate->blenddfactorrgb;
            break;
        case GL_BLEND_SRC_ALPHA:
            *params=glstate->blendsfactoralpha;
            break;
        case GL_BLEND_DST_ALPHA:
            *params=glstate->blenddfactoralpha;
            break;
        case GL_MAX_CLIP_PLANES:
            *params=hardext.maxplanes;
            break;
        case GL_MAX_LIGHTS:
            *params=hardext.maxlights;
            break;
        case GL_LIGHTING:
            *params=glstate->enable.lighting;
            break;
        case GL_DEPTH_WRITEMASK:
            *params=glstate->depth.mask;
            break;
        case GL_DEPTH_FUNC:
            *params=glstate->depth.func;
            break;
        case GL_CULL_FACE_MODE:
            *params=glstate->face.cull;
            break;
        case GL_FRONT_FACE:
            *params=glstate->face.front;
            break;
#define GO(n)        case GL_CLIP_PLANE##n: \
            *params=glstate->enable.plane[n]; \
            break
        GO(0);
        GO(1);
        GO(2);
        GO(3);
        GO(4);
        GO(5);
#undef GO
#define GO(n)        case GL_LIGHT##n: \
            *params=glstate->enable.light[n]; \
            break
        GO(0);
        GO(1);
        GO(2);
        GO(3);
        GO(4);
        GO(5);
        GO(6);
        GO(7);
#undef GO
        case GL_POINT_SIZE_MIN:
            *params=glstate->pointsprite.sizeMin;
            break;
        case GL_POINT_SIZE_MAX:
            *params=glstate->pointsprite.sizeMax;
            break;
        case GL_POINT_SIZE:
            *params=glstate->pointsprite.size;
            break;
        case GL_POINT_FADE_THRESHOLD_SIZE:
            *params=glstate->pointsprite.fadeThresholdSize;
            break;
        case GL_POINT_SPRITE_COORD_ORIGIN :
            *params=glstate->pointsprite.coordOrigin;
            break;
        case GL_DRAW_BUFFER:
            *params=GL_FRONT;
            break;
        case GL_READ_FRAMEBUFFER_BINDING:
            *params=glstate->fbo.fbo_read->id;
            break;
        //case GL_FRAMEBUFFER_BINDING:
        case GL_DRAW_FRAMEBUFFER_BINDING:
            *params=glstate->fbo.fbo_draw->id;
            break;
        // shader stuff
        case GL_CURRENT_PROGRAM:
            *params=glstate->glsl->program;
            break;
        // global hints
        case GL_PERSPECTIVE_CORRECTION_HINT:
            if(hardext.esversion==1) return 0; // fall back to actual glGet
            *params=GL_DONT_CARE;
            break;
        case GL_POINT_SMOOTH_HINT:
            if(hardext.esversion==1) return 0; // fall back to actual glGet
            *params=GL_DONT_CARE;
            break;
        case GL_LINE_SMOOTH_HINT:
            if(hardext.esversion==1) return 0; // fall back to actual glGet
            *params=GL_DONT_CARE;
            break;
        case GL_FOG_HINT:
            if(hardext.esversion==1) return 0; // fall back to actual glGet
            *params=GL_DONT_CARE;
            break;
        case GL_TEXTURE_COMPRESSION_HINT:
            *params=GL_DONT_CARE;
            break;
        case GL_CLAMP_READ_COLOR:
            *params=glstate->clamp_read_color;
            break;
        //case GL_MAX_VERTEX_ATTRIBS_ARB:   //same value
        case GL_MAX_VERTEX_ATTRIBS:
            if(hardext.esversion==1)
                *params = 0;
            else
                *params = hardext.maxvattrib;
            break;
        case GL_MAX_PROGRAM_MATRICES_ARB:
            *params = MAX_ARB_MATRIX;
            break;
        case GL_PROGRAM_ERROR_POSITION_ARB:
            *params = glstate->glsl->error_ptr;
            break;
        case GL_SAMPLER_BINDING:
            if(glstate->samplers.sampler[glstate->texture.active])
                *params = glstate->samplers.sampler[glstate->texture.active]->glname;
            else
                *params = 0;
            break;
        // GL4ES special hints
        case GL_SHRINK_HINT_GL4ES:
            *params=globals4es.texshrink;
            break;
        case GL_ALPHAHACK_HINT_GL4ES:
            *params=globals4es.alphahack;
            break;
        case GL_RECYCLEFBO_HINT_GL4ES:
            *params=globals4es.recyclefbo;
            break;
        case GL_MIPMAP_HINT_GL4ES:
            *params=globals4es.automipmap;
            break;
        case GL_TEXDUMP_HINT_GL4ES:
            *params=globals4es.texdump;
            break;
        case GL_COPY_HINT_GL4ES:
            *params=0;  // removed
            break;
        case GL_NOLUMAPHA_HINT_GL4ES:
            *params=globals4es.nolumalpha;
            break;
        case GL_BLENDHACK_HINT_GL4ES:
            *params=globals4es.blendhack;
            break;
        case GL_BATCH_HINT_GL4ES:
            *params=globals4es.maxbatch/100;
            break;
        case GL_NOERROR_HINT_GL4ES:
            *params=globals4es.noerror;
            break;
        case GL_AVOID16BITS_HINT_GL4ES:
            *params=globals4es.avoid16bits;
            break;
        case GL_GAMMA_HINT_GL4ES:
            *params=globals4es.gamma*10.f;
            break;
        default:
            return 0;
    }
    return 1;
}

// glGet
void APIENTRY_GL4ES gl4es_glGetBooleanv(GLenum pname, GLboolean *params) {
    DBG(printf("glGetBooleanv(%s, %p)\n", PrintEnum(pname), params);)
    if (params==NULL) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    /* Only the state gl4es tracks itself has to be intercepted here; anything
     * GLES knows about keeps going straight to the driver, as before. */
    GLfloat fparam;
    if (gl4es_commonGet(pname, &fparam)) {
        noerrorShim();
        *params = (fparam != 0.0f) ? GL_TRUE : GL_FALSE;
        return;
    }
    LOAD_GLES(glGetBooleanv);
    errorGL();
    gles_glGetBooleanv(pname, params);
}
AliasExport(void,glGetBooleanv,,(GLenum pname, GLboolean *params));

void APIENTRY_GL4ES gl4es_glGetIntegerv(GLenum pname, GLint *params) {
    DBG(printf("glGetIntegerv(%s, %p)\n", PrintEnum(pname), params);)
    if (params==NULL) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    GLint dummy;
    LOAD_GLES(glGetIntegerv);
    noerrorShim();
    GLfloat fparam;
    if (gl4es_commonGet(pname, &fparam)) {
        params[0] = fparam;
        return;
    }
    gltexture_t* tex;
    switch (pname) {
        // texture binding only make sense with int
        case GL_TEXTURE_BINDING_1D:
            tex=glstate->texture.bound[glstate->texture.active][ENABLED_TEX1D];
            *params=tex->texture;
            break;
        case GL_TEXTURE_BINDING_2D:
            tex=glstate->texture.bound[glstate->texture.active][ENABLED_TEX2D];
            *params=tex->texture;
            break;
        case GL_TEXTURE_BINDING_3D:
            tex=glstate->texture.bound[glstate->texture.active][ENABLED_TEX3D];
            *params=tex->texture;
            break;
        case GL_TEXTURE_BINDING_CUBE_MAP:
            tex=glstate->texture.bound[glstate->texture.active][ENABLED_CUBE_MAP];
            *params=tex->texture;
            break;
        case GL_TEXTURE_BINDING_RECTANGLE_ARB:
            tex=glstate->texture.bound[glstate->texture.active][ENABLED_TEXTURE_RECTANGLE];
            *params=tex->texture;
            break;
        // arrays...
        case GL_POINT_SIZE_RANGE:
        case GL_ALIASED_POINT_SIZE_RANGE:
            gles_glGetIntegerv(GL_ALIASED_POINT_SIZE_RANGE, params);
            break;
        case GL_NUM_COMPRESSED_TEXTURE_FORMATS:
            gles_glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, params);
            (*params)+=4;	// adding fake DXTc
            break;
        case GL_COMPRESSED_TEXTURE_FORMATS:
            gles_glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, &dummy);
            // get standard ones
            gles_glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS, params);
            // add fake DXTc
            params[dummy++]=GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
            params[dummy++]=GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
            params[dummy++]=GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
            params[dummy++]=GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
            break;
        case GL_LIGHT_MODEL_AMBIENT:
            for (dummy=0; dummy<4; dummy++)
                    params[dummy]=glstate->light.ambient[dummy];
            break;
        case GL_FOG_COLOR:
            for (dummy=0; dummy<4; dummy++)
                params[dummy]=glstate->fog.color[dummy];
            break;
        case GL_CURRENT_COLOR:
            for (dummy=0; dummy<4; dummy++)
                params[dummy]=glstate->color[dummy];
            break;
        case GL_CURRENT_SECONDARY_COLOR:
            for (dummy=0; dummy<4; dummy++)
                params[dummy]=glstate->secondary[dummy];
            break;
        case GL_CURRENT_NORMAL:
            for (dummy=0; dummy<3; dummy++)
                params[dummy]=glstate->normal[dummy];
            break;
        case GL_CURRENT_TEXTURE_COORDS:
            for (dummy=0; dummy<4; dummy++)
                params[dummy]=glstate->texcoord[glstate->texture.active][dummy];
            break;
        case GL_COLOR_WRITEMASK:
            memcpy(params, glstate->colormask, 4*sizeof(GLboolean));
            break;
        case GL_POINT_DISTANCE_ATTENUATION :
            for (dummy=0; dummy<3; dummy++)
                params[dummy]=glstate->pointsprite.distance[dummy];
            break;
        case GL_DEPTH_RANGE:
            params[0] = glstate->depth.Near*2147483647l;
            params[1] = glstate->depth.Far*2147483647l;
            break;
        case GL_VIEWPORT: {
            GLfloat v[4];
            if (getter_viewport4(v)) {
                params[0] = (GLint)v[0];
                params[1] = (GLint)v[1];
                params[2] = (GLint)v[2];
                params[3] = (GLint)v[3];
            } else {
                gles_glGetIntegerv(GL_VIEWPORT, params);
            }
            break;
        }
        case GL_SCISSOR_BOX: {
            GLfloat v[4];
            if (getter_scissor4(v)) {
                params[0] = (GLint)v[0];
                params[1] = (GLint)v[1];
                params[2] = (GLint)v[2];
                params[3] = (GLint)v[3];
            } else {
                gles_glGetIntegerv(GL_SCISSOR_BOX, params);
            }
            break;
        }
        case GL_COLOR_CLEAR_VALUE:
            // colors and depths map [0, 1] onto [0, INT_MAX], like the depth range above
            for (int i=0; i<4; i++)
                params[i] = glstate->clear_color[i]*2147483647.0;
            break;
        case GL_DEPTH_CLEAR_VALUE:
            params[0] = glstate->depth.clear*2147483647.0;
            break;
        default:
            errorGL();
            gles_glGetIntegerv(pname, params);
    }
}
AliasExport(void,glGetIntegerv,,(GLenum pname, GLint *params));

void APIENTRY_GL4ES gl4es_glGetFloatv(GLenum pname, GLfloat *params) {
    DBG(printf("glGetFloatv(%s, %p)\n", PrintEnum(pname), params);)
    LOAD_GLES(glGetFloatv);
    noerrorShim();
    if (gl4es_commonGet(pname, params)) {
        return;
    }

    switch (pname) {
        case GL_POINT_SIZE_RANGE:
        case GL_ALIASED_POINT_SIZE_RANGE:
            gles_glGetFloatv(GL_ALIASED_POINT_SIZE_RANGE, params);
            break;
        case GL_TRANSPOSE_PROJECTION_MATRIX:
            matrix_transpose(TOP(projection_matrix), params);
            break;
        case GL_TRANSPOSE_MODELVIEW_MATRIX:
            matrix_transpose(TOP(modelview_matrix), params);
            break;
        case GL_TRANSPOSE_TEXTURE_MATRIX:
            matrix_transpose(TOP(texture_matrix[glstate->texture.active]), params);
            break;
        case GL_PROJECTION_MATRIX:
            memcpy(params, TOP(projection_matrix), 16*sizeof(GLfloat));
            break;
        case GL_MODELVIEW_MATRIX:
            memcpy(params, TOP(modelview_matrix), 16*sizeof(GLfloat));
            break;
        case GL_TEXTURE_MATRIX:
            memcpy(params, TOP(texture_matrix[glstate->texture.active]), 16*sizeof(GLfloat));
            break;
        case GL_LIGHT_MODEL_AMBIENT:
            memcpy(params, glstate->light.ambient, 4*sizeof(GLfloat));
            break;
        case GL_FOG_COLOR:
            memcpy(params, glstate->fog.color, 4*sizeof(GLfloat));
            break;
        case GL_CURRENT_COLOR:
            memcpy(params, glstate->color, 4*sizeof(GLfloat));
            break;
        case GL_CURRENT_SECONDARY_COLOR:
            memcpy(params, glstate->secondary, 4*sizeof(GLfloat));
            break;
         case GL_CURRENT_NORMAL:
            memcpy(params, glstate->normal, 3*sizeof(GLfloat));
            break;
         case GL_CURRENT_TEXTURE_COORDS:
            memcpy(params, glstate->texcoord[glstate->texture.active], 4*sizeof(GLfloat));
            break;
         case GL_COLOR_WRITEMASK:
            for (int dummy=0; dummy<4; dummy++)
                params[dummy] = glstate->colormask[dummy];
            break;
        case GL_POINT_DISTANCE_ATTENUATION :
            memcpy(params, glstate->pointsprite.distance, 3*sizeof(GLfloat));
            break;
        case GL_DEPTH_RANGE:
            params[0] = glstate->depth.Near;
            params[1] = glstate->depth.Far;
            break;
        case GL_COLOR_CLEAR_VALUE:
            memcpy(params, glstate->clear_color, 4*sizeof(GLfloat));
            break;
        case GL_DEPTH_CLEAR_VALUE:
            params[0] = glstate->depth.clear;
            break;
        case GL_VIEWPORT:
            if (!getter_viewport4(params))
                gles_glGetFloatv(GL_VIEWPORT, params);
            break;
        case GL_SCISSOR_BOX:
            if (!getter_scissor4(params))
                gles_glGetFloatv(GL_SCISSOR_BOX, params);
            break;
        default:
            errorGL();
            gles_glGetFloatv(pname, params);
    }
}
AliasExport(void,glGetFloatv,,(GLenum pname, GLfloat *params));

void APIENTRY_GL4ES gl4es_glGetDoublev(GLenum pname, GLdouble *params) {
    DBG(printf("glGetDoublev(%s, %p)\n", PrintEnum(pname), params);)
    GLfloat tmp[4*4];
    LOAD_GLES(glGetFloatv);
    noerrorShim();
    if (gl4es_commonGet(pname, tmp)) {
        *params = *tmp;
        return;
    }
    switch (pname) {
        case GL_POINT_SIZE_RANGE:
        case GL_ALIASED_POINT_SIZE_RANGE:
            gles_glGetFloatv(GL_ALIASED_POINT_SIZE_RANGE, tmp);
            params[0] = tmp[0]; params[1] = tmp[1];
            break;
        case GL_TRANSPOSE_PROJECTION_MATRIX:
            matrix_transpose(TOP(projection_matrix), tmp);
            for(int i=0; i<16; i++) params[i] = tmp[i];
            break;
        case GL_TRANSPOSE_MODELVIEW_MATRIX:
            matrix_transpose(TOP(modelview_matrix), tmp);
            for(int i=0; i<16; i++) params[i] = tmp[i];
            break;
        case GL_TRANSPOSE_TEXTURE_MATRIX:
            matrix_transpose(TOP(texture_matrix[glstate->texture.active]), tmp);
            for(int i=0; i<16; i++) params[i] = tmp[i];
            break;
        case GL_PROJECTION_MATRIX:
            memcpy(tmp, TOP(projection_matrix), 16*sizeof(GLfloat));
            for(int i=0; i<16; i++) params[i] = tmp[i];
            break;
        case GL_MODELVIEW_MATRIX:
            memcpy(tmp, TOP(modelview_matrix), 16*sizeof(GLfloat));
            for(int i=0; i<16; i++) params[i] = tmp[i];
            break;
        case GL_TEXTURE_MATRIX:
            memcpy(tmp, TOP(texture_matrix[glstate->texture.active]), 16*sizeof(GLfloat));
            for(int i=0; i<16; i++) params[i] = tmp[i];
            break;
        case GL_LIGHT_MODEL_AMBIENT:
            memcpy(tmp, glstate->light.ambient, 4*sizeof(GLfloat));
            for(int i=0; i<4; i++) params[i] = tmp[i];
            break;
        case GL_FOG_COLOR:
            memcpy(tmp, glstate->fog.color, 4*sizeof(GLfloat));
            for(int i=0; i<4; i++) params[i] = tmp[i];
            break;
        case GL_CURRENT_COLOR:
            memcpy(tmp, glstate->color, 4*sizeof(GLfloat));
            for(int i=0; i<4; i++) params[i] = tmp[i];
            break;
        case GL_CURRENT_SECONDARY_COLOR:
            memcpy(tmp, glstate->secondary, 4*sizeof(GLfloat));
            for(int i=0; i<4; i++) params[i] = tmp[i];
            break;
         case GL_CURRENT_NORMAL:
            memcpy(tmp, glstate->normal, 3*sizeof(GLfloat));
            for(int i=0; i<3; i++) params[i] = tmp[i];
            break;
         case GL_CURRENT_TEXTURE_COORDS:
            memcpy(tmp, glstate->texcoord[glstate->texture.active], 4*sizeof(GLfloat));
            for(int i=0; i<4; i++) params[i] = tmp[i];
            break;
         case GL_COLOR_WRITEMASK:
            for (int dummy=0; dummy<4; dummy++)
                params[dummy] = glstate->colormask[dummy];
            break;
        case GL_POINT_DISTANCE_ATTENUATION :
            memcpy(tmp, glstate->pointsprite.distance, 3*sizeof(GLfloat));
            for(int i=0; i<3; i++) params[i] = tmp[i];
            break;
        case GL_DEPTH_RANGE:
            params[0] = glstate->depth.Near;
            params[1] = glstate->depth.Far;
            break;
        case GL_COLOR_CLEAR_VALUE:
            for (int i=0; i<4; i++) params[i] = glstate->clear_color[i];
            break;
        case GL_DEPTH_CLEAR_VALUE:
            params[0] = glstate->depth.clear;
            break;
        case GL_VIEWPORT:
            if (getter_viewport4(tmp)) {
                for (int i=0; i<4; i++) params[i] = tmp[i];
            } else {
                gles_glGetFloatv(GL_VIEWPORT, tmp);
                for (int i=0; i<4; i++) params[i] = tmp[i];
            }
            break;
        case GL_SCISSOR_BOX:
            if (getter_scissor4(tmp)) {
                for (int i=0; i<4; i++) params[i] = tmp[i];
            } else {
                gles_glGetFloatv(GL_SCISSOR_BOX, tmp);
                for (int i=0; i<4; i++) params[i] = tmp[i];
            }
            break;
        default:
            errorGL();
            gles_glGetFloatv(pname, tmp);
            params[0] = tmp[0];
    }
}
AliasExport(void,glGetDoublev,,(GLenum pname, GLdouble *params));

void APIENTRY_GL4ES gl4es_glGetLightfv(GLenum light, GLenum pname, GLfloat * params) {
    DBG(printf("glGetLightfv(%s, %s, %p)\n", PrintEnum(light), PrintEnum(pname), params);)
    const int nl = light-GL_LIGHT0;
    if(nl<0 || nl>=hardext.maxlights) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    switch(pname) {
        case GL_AMBIENT:
            memcpy(params, glstate->light.lights[nl].ambient, 4*sizeof(GLfloat));
            break;
        case GL_DIFFUSE:
            memcpy(params, glstate->light.lights[nl].diffuse, 4*sizeof(GLfloat));
            break;
        case GL_SPECULAR:
            memcpy(params, glstate->light.lights[nl].specular, 4*sizeof(GLfloat));
            break;
        case GL_POSITION:
            memcpy(params, glstate->light.lights[nl].position, 4*sizeof(GLfloat));
            break;
        case GL_SPOT_DIRECTION:
            memcpy(params, glstate->light.lights[nl].spotDirection, 3*sizeof(GLfloat));
            break;
        case GL_SPOT_EXPONENT:
            params[0] = glstate->light.lights[nl].spotExponent;
            break;
        case GL_SPOT_CUTOFF:
            params[0] = glstate->light.lights[nl].spotCutoff;
            break;
        case GL_CONSTANT_ATTENUATION:
            params[0] = glstate->light.lights[nl].constantAttenuation;
            break;
        case GL_LINEAR_ATTENUATION:
            params[0] = glstate->light.lights[nl].linearAttenuation;
            break;
        case GL_QUADRATIC_ATTENUATION:
            params[0] = glstate->light.lights[nl].quadraticAttenuation;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    noerrorShim();
}
AliasExport(void,glGetLightfv,,(GLenum light, GLenum pname, GLfloat * params));

void APIENTRY_GL4ES gl4es_glGetMaterialfv(GLenum face, GLenum pname, GLfloat * params) {
    DBG(printf("glGetMaterialfv(%sn %s, %p)\n", PrintEnum(face), PrintEnum(pname), params);)
    if(face!=GL_FRONT && face!=GL_BACK) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    switch(pname) {
        case GL_AMBIENT:
            if(face==GL_FRONT)
                memcpy(params, glstate->material.front.ambient, 4*sizeof(GLfloat));
            if(face==GL_BACK)
                memcpy(params, glstate->material.back.ambient, 4*sizeof(GLfloat));
            break;
        case GL_DIFFUSE:
            if(face==GL_FRONT)
                memcpy(params, glstate->material.front.diffuse, 4*sizeof(GLfloat));
            if(face==GL_BACK)
                memcpy(params, glstate->material.back.diffuse, 4*sizeof(GLfloat));
            break;
        case GL_SPECULAR:
            if(face==GL_FRONT)
                memcpy(params, glstate->material.front.specular, 4*sizeof(GLfloat));
            if(face==GL_BACK)
                memcpy(params, glstate->material.back.specular, 4*sizeof(GLfloat));
            break;
        case GL_EMISSION:
            if(face==GL_FRONT)
                memcpy(params, glstate->material.front.emission, 4*sizeof(GLfloat));
            if(face==GL_BACK)
                memcpy(params, glstate->material.back.emission, 4*sizeof(GLfloat));
            break;
        case GL_SHININESS:
            if(face==GL_FRONT)
                *params = glstate->material.front.shininess;
            if(face==GL_BACK)
                *params = glstate->material.back.shininess;
            break;
        case GL_COLOR_INDEXES:
            if(face==GL_FRONT) {
                params[0] = glstate->material.front.indexes[0];
                params[1] = glstate->material.front.indexes[1];
                params[2] = glstate->material.front.indexes[2];
            }
            if(face==GL_BACK) {
                params[0] = glstate->material.back.indexes[0];
                params[1] = glstate->material.back.indexes[1];
                params[2] = glstate->material.back.indexes[2];
            }
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    noerrorShim();
}
AliasExport(void,glGetMaterialfv,,(GLenum face, GLenum pname, GLfloat * params));

void APIENTRY_GL4ES gl4es_glGetClipPlanef(GLenum plane, GLfloat * equation)
{
    DBG(printf("glGetClipPlanef(%s, %p)\n", PrintEnum(plane), equation);)
    if(plane<GL_CLIP_PLANE0 || plane>=GL_CLIP_PLANE0+hardext.maxplanes) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    LOAD_GLES2(glGetClipPlanef);
    if(gles_glGetClipPlanef)
    {
        errorGL();
        gles_glGetClipPlanef(plane, equation);
    } else {
        int p = plane-GL_CLIP_PLANE0;
        noerrorShim();
        memcpy(equation, glstate->planes[p], 4*sizeof(GLfloat)); // should return transformed coordinates
    }
}
AliasExport(void,glGetClipPlanef,,(GLenum plane, GLfloat * equation));


const GLubyte* APIENTRY_GL4ES gl4es_glGetStringi(GLenum name, GLuint index) {
    DBG(printf("glGetStringi(%s, %d)\n", PrintEnum(name), index);)
    BuildExtensionsList();
    if (name!=GL_EXTENSIONS) {
        errorShim(GL_INVALID_ENUM);
        return NULL;
    }
    if (index<0 || index>=glstate->num_extensions) {
        errorShim(GL_INVALID_VALUE);
        return NULL;
    }
    return glstate->extensions_list[index];
}
AliasExport(const GLubyte*,glGetStringi,,(GLenum name, GLuint index));

// Some stuff from the ARB_imaging extension
void gl4es_glGetMinmaxParameteriv(GLenum target, GLenum pname, GLint* params)
{
    DBG(printf("unsupported glGetMinmaxParameteriv(%s, %s, %p)\n", PrintEnum(target), PrintEnum(pname), params);)
    errorShim(GL_INVALID_VALUE);
}
AliasExport(void, glGetMinmaxParameteriv,,(GLenum target, GLenum pname, GLint* params));

void gl4es_glGetMinmaxParameterfv(GLenum target, GLenum pname, GLfloat* params)
{
    DBG(printf("unsupported glGetMinmaxParameterfv(%s, %s, %p)\n", PrintEnum(target), PrintEnum(pname), params);)
    errorShim(GL_INVALID_VALUE);
}
AliasExport(void, glGetMinmaxParameterfv,,(GLenum target, GLenum pname, GLfloat* params));
