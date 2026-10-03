#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "loader.h"
#include "debug.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

static void gl_changetex(int n)
{
    if(glstate->bound_changed < n+1)
        glstate->bound_changed = n+1;
}

static void fpe_changeplane(int n, bool enable)
{
    glstate->fpe = NULL;
    if(enable)
        glstate->fpe_state->plane |= 1<<n;
    else
        glstate->fpe_state->plane &= ~(1<<n);
}
static void fpe_changelight(int n, bool enable)
{
    glstate->fpe = NULL;
    if(enable)
        glstate->fpe_state->light |= 1<<n;
    else
        glstate->fpe_state->light &= ~(1<<n);
}
static void fpe_changetex(int n)
{
    if(glstate->fpe_bound_changed < n+1)
        glstate->fpe_bound_changed = n+1;
}
#define generate_changetexgen(C) \
static void fpe_changetexgen_##C(int n, bool enable) \
{ \
    glstate->fpe_state->texgen[n].texgen_##C = enable?1:0; \
}
generate_changetexgen(s)
generate_changetexgen(t)
generate_changetexgen(r)
generate_changetexgen(q)
#undef generate_changetexgen

void change_vao_texcoord(int tmu, bool enable) 
{
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+tmu].enabled = enable;
    if(enable) {
        if(glstate->vao->maxtex<tmu+1) glstate->vao->maxtex=tmu+1;
    } else {
        if(glstate->vao->maxtex==tmu+1) {
            glstate->vao->maxtex=tmu;
        }
    }
}

#ifndef GL_TEXTURE_STREAM_IMG  
#define GL_TEXTURE_STREAM_IMG                                   0x8C0D     
#endif

static void proxy_glEnable(GLenum cap, bool enable, void (APIENTRY_GLES *next)(GLenum)) {
    #define proxy_GO(constant, name) \
        case constant: if(glstate->enable.name != enable) {FLUSH_BEGINEND; glstate->enable.name = enable; next(cap);} break
    #define proxy_GOFPE(constant, name, fct) \
        case constant: if(glstate->enable.name != enable) {FLUSH_BEGINEND; glstate->enable.name = enable; if(glstate->fpe_state) { fct; } else next(cap);} break
    #define GO(constant, name) \
        case constant: if(glstate->list.pending && glstate->enable.name!=enable) gl4es_flush(); glstate->enable.name = enable; break;
    #define GONF(constant, name) \
        case constant: glstate->enable.name = enable; break;
    #define GOFPE(constant, name, fct) \
        case constant: if(glstate->list.pending && glstate->enable.name!=enable) gl4es_flush(); glstate->enable.name = enable; if(glstate->fpe_state) { fct; } break;
    #define proxy_clientGO(constant, name) \
        case constant: if (glstate->vao->name != enable) {glstate->vao->name = enable; next(cap);} break
    #define clientGO(constant, name) \
        case constant: glstate->vao->name = enable; break;
    #define clientGO_proxyFPE(constant, name) \
        case constant: glstate->vao->name = enable; if(glstate->fpe_state) { next(cap);} break;
    // Alpha Hack
    if (globals4es.alphahack && (cap==GL_ALPHA_TEST) && enable) {
        if (!glstate->texture.bound[glstate->texture.active][ENABLED_TEX2D]->alpha)
            enable = false;
    }
    noerrorShim();
#ifdef TEXSTREAM
    if (cap==GL_TEXTURE_STREAM_IMG) {
        FLUSH_BEGINEND;
        if(enable)
            glstate->enable.texture[glstate->texture.active] |= (1<<ENABLED_TEX2D);
        else
            glstate->enable.texture[glstate->texture.active] &= ~(1<<ENABLED_TEX2D);
        gl_changetex(glstate->texture.active);
        if(glstate->fpe_state)
            fpe_changetex(glstate->texture.active);
        else
            next(cap);
        return;
    }
#endif

    switch (cap) {
        GO(GL_AUTO_NORMAL, auto_normal);
        proxy_GOFPE(GL_ALPHA_TEST, alpha_test,glstate->fpe_state->alphatest=enable);
        proxy_GOFPE(GL_FOG, fog, glstate->fpe_state->fog=enable);
        case GL_BLEND: if(glstate->enable.blend != enable) {FLUSH_BEGINEND; glstate->enable.blend = enable; if(glstate->fpe_state && globals4es.shaderblend) { glstate->fpe_state->blend_enable = enable; } else next(cap);} break;
        proxy_GO(GL_CULL_FACE, cull_face);
        proxy_GO(GL_DEPTH_TEST, depth_test);
        proxy_GO(GL_STENCIL_TEST, stencil_test);
        // texgen
        GOFPE(GL_TEXTURE_GEN_S, texgen_s[glstate->texture.active], fpe_changetexgen_s(glstate->texture.active, enable)); //TODO: FPE stuffs
        GOFPE(GL_TEXTURE_GEN_T, texgen_t[glstate->texture.active], fpe_changetexgen_t(glstate->texture.active, enable));
        GOFPE(GL_TEXTURE_GEN_R, texgen_r[glstate->texture.active], fpe_changetexgen_r(glstate->texture.active, enable));
        GOFPE(GL_TEXTURE_GEN_Q, texgen_q[glstate->texture.active], fpe_changetexgen_q(glstate->texture.active, enable));
        GO(GL_LINE_STIPPLE, line_stipple);

        // clip plane
        proxy_GOFPE(GL_CLIP_PLANE0, plane[0], fpe_changeplane(0, enable));
        proxy_GOFPE(GL_CLIP_PLANE1, plane[1], fpe_changeplane(1, enable));
        proxy_GOFPE(GL_CLIP_PLANE2, plane[2], fpe_changeplane(2, enable));
        proxy_GOFPE(GL_CLIP_PLANE3, plane[3], fpe_changeplane(3, enable));
        proxy_GOFPE(GL_CLIP_PLANE4, plane[4], fpe_changeplane(4, enable));
        proxy_GOFPE(GL_CLIP_PLANE5, plane[5], fpe_changeplane(5, enable));

        // lights
        proxy_GOFPE(GL_LIGHT0, light[0], fpe_changelight(0, enable));
        proxy_GOFPE(GL_LIGHT1, light[1], fpe_changelight(1, enable));
        proxy_GOFPE(GL_LIGHT2, light[2], fpe_changelight(2, enable));
        proxy_GOFPE(GL_LIGHT3, light[3], fpe_changelight(3, enable));
        proxy_GOFPE(GL_LIGHT4, light[4], fpe_changelight(4, enable));
        proxy_GOFPE(GL_LIGHT5, light[5], fpe_changelight(5, enable));
        proxy_GOFPE(GL_LIGHT6, light[6], fpe_changelight(6, enable));
        proxy_GOFPE(GL_LIGHT7, light[7], fpe_changelight(7, enable));
        proxy_GOFPE(GL_LIGHTING, lighting, glstate->fpe_state->lighting=enable);
        proxy_GOFPE(GL_NORMALIZE, normalize, glstate->fpe_state->normalize=enable);
        proxy_GOFPE(GL_RESCALE_NORMAL, normal_rescale, glstate->fpe_state->rescaling=enable);
        proxy_GOFPE(GL_COLOR_MATERIAL, color_material, glstate->fpe_state->color_material=enable);

        // point sprite
        proxy_GOFPE(GL_POINT_SPRITE, pointsprite, glstate->fpe_state->pointsprite=enable); // TODO: plugin fpe stuffs
        proxy_GO(GL_PROGRAM_POINT_SIZE, point_size);

        // Smooth and multisample (todo: do somthing with fpe?)
        proxy_GOFPE(GL_MULTISAMPLE, multisample, );
        proxy_GOFPE(GL_SAMPLE_COVERAGE, sample_coverage, );
        proxy_GOFPE(GL_SAMPLE_ALPHA_TO_COVERAGE, sample_alpha_to_coverage, );
        proxy_GOFPE(GL_SAMPLE_ALPHA_TO_ONE, sample_alpha_to_one, );
        proxy_GOFPE(GL_POINT_SMOOTH, point_smooth, );
        proxy_GOFPE(GL_LINE_SMOOTH, line_smooth, );

        proxy_GO(GL_POLYGON_OFFSET_FILL, polyfill_offset);

        // color logic op
        proxy_GOFPE(GL_COLOR_LOGIC_OP, color_logic_op, );
        
        // Secondary color
        GOFPE(GL_COLOR_SUM, color_sum, glstate->fpe_state->colorsum = enable);
        //cannot use clientGO_proxyFPE here, has the ClientArray are really enabled / disabled elsewhere in fact (inside glDraw or list_draw)
        clientGO(GL_SECONDARY_COLOR_ARRAY, vertexattrib[ATT_SECONDARY].enabled);
        clientGO(GL_FOG_COORD_ARRAY, vertexattrib[ATT_FOGCOORD].enabled);
    
        // for glDrawArrays
        clientGO(GL_VERTEX_ARRAY, vertexattrib[ATT_VERTEX].enabled);
        clientGO(GL_NORMAL_ARRAY, vertexattrib[ATT_NORMAL].enabled);
        clientGO(GL_COLOR_ARRAY, vertexattrib[ATT_COLOR].enabled);
        case GL_TEXTURE_COORD_ARRAY: change_vao_texcoord(glstate->texture.client, enable); break;

        // map eval
        GONF(GL_MAP1_COLOR_4 , map1_color4);
        GONF(GL_MAP1_INDEX , map1_index);
        GONF(GL_MAP1_NORMAL , map1_normal);
        GONF(GL_MAP1_TEXTURE_COORD_1 , map1_texture1);
        GONF(GL_MAP1_TEXTURE_COORD_2 , map1_texture2);
        GONF(GL_MAP1_TEXTURE_COORD_3 , map1_texture3);
        GONF(GL_MAP1_TEXTURE_COORD_4 , map1_texture4);
        GONF(GL_MAP1_VERTEX_3 , map1_vertex3);
        GONF(GL_MAP1_VERTEX_4 , map1_vertex4);
        GONF(GL_MAP2_COLOR_4 , map2_color4);
        GONF(GL_MAP2_INDEX , map2_index);
        GONF(GL_MAP2_NORMAL , map2_normal);
        GONF(GL_MAP2_TEXTURE_COORD_1 , map2_texture1);
        GONF(GL_MAP2_TEXTURE_COORD_2 , map2_texture2);
        GONF(GL_MAP2_TEXTURE_COORD_3 , map2_texture3);
        GONF(GL_MAP2_TEXTURE_COORD_4 , map2_texture4);
        GONF(GL_MAP2_VERTEX_3 , map2_vertex3);
        GONF(GL_MAP2_VERTEX_4 , map2_vertex4);

        // ARB_vertex_program
        proxy_GOFPE(GL_VERTEX_PROGRAM_ARB, vertex_arb, glstate->fpe_state->vertex_prg_enable=enable);
        GONF(GL_VERTEX_PROGRAM_TWO_SIDE_ARB, vertex_two_side_arb);
        // ARB_fragment_program
        proxy_GOFPE(GL_FRAGMENT_PROGRAM_ARB, fragment_arb, glstate->fpe_state->fragment_prg_enable=enable);
        
        // Texture 1D and 3D
        case GL_TEXTURE_1D:
            FLUSH_BEGINEND; 
            if(enable)
                glstate->enable.texture[glstate->texture.active] |= (1<<ENABLED_TEX1D);
            else
                glstate->enable.texture[glstate->texture.active] &= ~(1<<ENABLED_TEX1D);
            gl_changetex(glstate->texture.active);
            if(glstate->fpe_state)
                fpe_changetex(glstate->texture.active);
            break;
        case GL_TEXTURE_2D:
            if(glstate->list.pending && ((glstate->enable.texture[glstate->texture.active]>>ENABLED_TEX2D)&1)!=enable) gl4es_flush();
            if(enable == ((glstate->enable.texture[glstate->texture.active]>>ENABLED_TEX2D)&1))
                return; // no change
            if(enable)
                glstate->enable.texture[glstate->texture.active] |= (1<<ENABLED_TEX2D);
            else
                glstate->enable.texture[glstate->texture.active] &= ~(1<<ENABLED_TEX2D);
            gl_changetex(glstate->texture.active);
            if(glstate->fpe_state)
                fpe_changetex(glstate->texture.active);
            else {
                realize_active();
                next(cap);
            }
            break;
        case GL_TEXTURE_3D:
            FLUSH_BEGINEND; 
            if(enable)
                glstate->enable.texture[glstate->texture.active] |= (1<<ENABLED_TEX3D);
            else
                glstate->enable.texture[glstate->texture.active] &= ~(1<<ENABLED_TEX3D);
            gl_changetex(glstate->texture.active);
            if(glstate->fpe_state)
                fpe_changetex(glstate->texture.active);
            break;
        case GL_TEXTURE_RECTANGLE_ARB:
            FLUSH_BEGINEND; 
            if(enable)
                glstate->enable.texture[glstate->texture.active] |= (1<<ENABLED_TEXTURE_RECTANGLE);
            else
                glstate->enable.texture[glstate->texture.active] &= ~(1<<ENABLED_TEXTURE_RECTANGLE);
            gl_changetex(glstate->texture.active);
            if(glstate->fpe_state)
                fpe_changetex(glstate->texture.active);
            break;
        case GL_TEXTURE_CUBE_MAP:
            FLUSH_BEGINEND; 
            if(enable)
                glstate->enable.texture[glstate->texture.active] |= (1<<ENABLED_CUBE_MAP);
            else
                glstate->enable.texture[glstate->texture.active] &= ~(1<<ENABLED_CUBE_MAP);
            gl_changetex(glstate->texture.active);
            if(glstate->fpe_state)
                fpe_changetex(glstate->texture.active);
            else {
                realize_active();
                next(cap);
            }
            break;

        
        default: errorGL(); FLUSH_BEGINEND; realize_active(); next(cap); break;
    }
    #undef proxy_GO
    #undef GO
    #undef proxy_clientGO
    #undef clientGO
}

void APIENTRY_GL4ES gl4es_glEnable(GLenum cap) {
    DBG(printf("glEnable(%s), glstate->list.pending=%d\n", PrintEnum(cap), glstate->list.pending);)
    if(!glstate->list.pending) {
        PUSH_IF_COMPILING(glEnable)
    }
#ifdef TEXSTREAM00
    if (globals4es.texstream && (cap==GL_TEXTURE_2D)) {
        if (glstate->texture.bound[glstate->texture.active][ENABLED_TEX2D]->streamed)
            cap = GL_TEXTURE_STREAM_IMG;
    }
    if (globals4es.texstream && (cap==GL_TEXTURE_RECTANGLE_ARB)) {
        if (glstate->texture.bound[glstate->texture.active][ENABLED_TEXTURE_RECTANGLE]->streamed)
            cap = GL_TEXTURE_STREAM_IMG;
    }
#endif
    LOAD_GLES(glEnable);
    proxy_glEnable(cap, true, gles_glEnable);
}
AliasExport(void,glEnable,,(GLenum cap));

void APIENTRY_GL4ES gl4es_glDisable(GLenum cap) {
    DBG(printf("glDisable(%s), glstate->list.pending=%d\n", PrintEnum(cap), glstate->list.pending);)
    if(!glstate->list.pending) {
        PUSH_IF_COMPILING(glDisable)
    }
        
#ifdef TEXSTREAM00
    if (globals4es.texstream && (cap==GL_TEXTURE_2D)) {
        if (glstate->texture.bound[glstate->texture.active][ENABLED_TEX2D]->streamed)
            cap = GL_TEXTURE_STREAM_IMG;
    }
    if (globals4es.texstream && (cap==GL_TEXTURE_RECTANGLE_ARB)) {
        if (glstate->texture.bound[glstate->texture.active][ENABLED_TEXTURE_RECTANGLE]->streamed)
            cap = GL_TEXTURE_STREAM_IMG;
    }
#endif
    LOAD_GLES(glDisable);
    proxy_glEnable(cap, false, gles_glDisable);
}
AliasExport(void,glDisable,,(GLenum cap));

void APIENTRY_GL4ES gl4es_glEnableClientState(GLenum cap) {
    DBG(printf("glEnableClientState(%s), list.begin=%dn", PrintEnum(cap), glstate->list.begin);)
    ERROR_IN_BEGIN
    // should flush for now... to be optimized later!
    /*if (glstate->list.active && !glstate->list.compiling && !glstate->list.pending)
        gl4es_flush();*/
    proxy_glEnable(cap, true, fpe_glEnableClientState);
}
AliasExport(void,glEnableClientState,,(GLenum cap));

void APIENTRY_GL4ES gl4es_glDisableClientState(GLenum cap) {
    DBG(printf("glDisableClientState(%s), list.begin=%d\n", PrintEnum(cap), glstate->list.begin);)
    ERROR_IN_BEGIN
    // should flush for now... to be optimized later!
    /*if (glstate->list.active && !glstate->list.compiling && !glstate->list.pending)
        gl4es_flush();*/
    proxy_glEnable(cap, false, fpe_glDisableClientState);
}
AliasExport(void,glDisableClientState,,(GLenum cap));


#define isenabled(what, where) \
    case what: return glstate->enable.where
#define clientisenabled(what, where) \
    case what: return glstate->vao->where
    
GLboolean APIENTRY_GL4ES gl4es_glIsEnabled(GLenum cap) {
    DBG(printf("glIsEnabed(%s), list.begin=%d, list.compiling=%d, list.pending=%d\n", PrintEnum(cap), glstate->list.begin, glstate->list.compiling, glstate->list.pending);)
    if(glstate->list.begin) {errorShim(GL_INVALID_OPERATION); return GL_FALSE;}
    if(glstate->list.compiling) {errorShim(GL_INVALID_OPERATION); return GL_FALSE;}
    // should flush for now... but no need if it's just a pending list...
    if (glstate->list.active && !glstate->list.pending)
        gl4es_flush();
    GLboolean gles_glIsEnabled(glIsEnabled_ARG_EXPAND); //LOAD_GLES(glIsEnabled);
    noerrorShim();
    switch (cap) {
        isenabled(GL_AUTO_NORMAL, auto_normal);
        isenabled(GL_ALPHA_TEST, alpha_test);
        isenabled(GL_FOG, fog);
        isenabled(GL_BLEND, blend);
        isenabled(GL_CULL_FACE, cull_face);
        isenabled(GL_DEPTH_TEST, depth_test);
        isenabled(GL_STENCIL_TEST, stencil_test);
        isenabled(GL_LINE_STIPPLE, line_stipple);
        isenabled(GL_TEXTURE_GEN_S, texgen_s[glstate->texture.active]);
        isenabled(GL_TEXTURE_GEN_T, texgen_t[glstate->texture.active]);
        isenabled(GL_TEXTURE_GEN_R, texgen_r[glstate->texture.active]);
        isenabled(GL_TEXTURE_GEN_Q, texgen_q[glstate->texture.active]);
        isenabled(GL_COLOR_MATERIAL, color_material);
        isenabled(GL_COLOR_SUM, color_sum);
        isenabled(GL_POINT_SPRITE, pointsprite);
        isenabled(GL_PROGRAM_POINT_SIZE, point_size);
        isenabled(GL_CLIP_PLANE0, plane[0]);
        isenabled(GL_CLIP_PLANE1, plane[1]);
        isenabled(GL_CLIP_PLANE2, plane[2]);
        isenabled(GL_CLIP_PLANE3, plane[3]);
        isenabled(GL_CLIP_PLANE4, plane[4]);
        isenabled(GL_CLIP_PLANE5, plane[5]);
        isenabled(GL_LIGHT0, light[0]);
        isenabled(GL_LIGHT1, light[1]);
        isenabled(GL_LIGHT2, light[2]);
        isenabled(GL_LIGHT3, light[3]);
        isenabled(GL_LIGHT4, light[4]);
        isenabled(GL_LIGHT5, light[5]);
        isenabled(GL_LIGHT6, light[6]);
        isenabled(GL_LIGHT7, light[7]);
        isenabled(GL_LIGHTING, lighting);
        isenabled(GL_MULTISAMPLE, multisample);
        isenabled(GL_SAMPLE_COVERAGE, sample_coverage);
        isenabled(GL_SAMPLE_ALPHA_TO_COVERAGE, sample_alpha_to_coverage);
        isenabled(GL_SAMPLE_ALPHA_TO_ONE, sample_alpha_to_one);
        isenabled(GL_POINT_SMOOTH, point_smooth);
        isenabled(GL_LINE_SMOOTH, line_smooth);
        isenabled(GL_POLYGON_OFFSET_FILL, polyfill_offset);
        isenabled(GL_COLOR_LOGIC_OP, color_logic_op);
        clientisenabled(GL_SECONDARY_COLOR_ARRAY, vertexattrib[ATT_SECONDARY].enabled);
        clientisenabled(GL_FOG_COORD_ARRAY, vertexattrib[ATT_FOGCOORD].enabled);
        case GL_TEXTURE_1D: return glstate->enable.texture[glstate->texture.active]&(1<<ENABLED_TEX1D);
        case GL_TEXTURE_2D: return glstate->enable.texture[glstate->texture.active]&(1<<ENABLED_TEX2D);
        case GL_TEXTURE_3D: return glstate->enable.texture[glstate->texture.active]&(1<<ENABLED_TEX3D);
        case GL_TEXTURE_CUBE_MAP: return glstate->enable.texture[glstate->texture.active]&(1<<ENABLED_CUBE_MAP);
        clientisenabled(GL_VERTEX_ARRAY, vertexattrib[ATT_VERTEX].enabled);
        clientisenabled(GL_NORMAL_ARRAY, vertexattrib[ATT_NORMAL].enabled);
        clientisenabled(GL_COLOR_ARRAY, vertexattrib[ATT_COLOR].enabled);
        clientisenabled(GL_TEXTURE_COORD_ARRAY, vertexattrib[ATT_MULTITEXCOORD0+glstate->texture.client].enabled);
        isenabled(GL_NORMALIZE, normalize);
        isenabled(GL_RESCALE_NORMAL, normal_rescale);
        isenabled(GL_MAP1_COLOR_4, map1_color4);
        isenabled(GL_MAP1_INDEX, map1_index);
        isenabled(GL_MAP1_NORMAL, map1_normal);
        isenabled(GL_MAP1_TEXTURE_COORD_1, map1_texture1);
        isenabled(GL_MAP1_TEXTURE_COORD_2, map1_texture2);
        isenabled(GL_MAP1_TEXTURE_COORD_3, map1_texture3);
        isenabled(GL_MAP1_TEXTURE_COORD_4, map1_texture4);
        isenabled(GL_MAP1_VERTEX_3, map1_vertex3);
        isenabled(GL_MAP1_VERTEX_4, map1_vertex4);
        isenabled(GL_MAP2_COLOR_4, map2_color4);
        isenabled(GL_MAP2_INDEX, map2_index);
        isenabled(GL_MAP2_NORMAL, map2_normal);
        isenabled(GL_MAP2_TEXTURE_COORD_1, map2_texture1);
        isenabled(GL_MAP2_TEXTURE_COORD_2, map2_texture2);
        isenabled(GL_MAP2_TEXTURE_COORD_3, map2_texture3);
        isenabled(GL_MAP2_TEXTURE_COORD_4, map2_texture4);
        isenabled(GL_MAP2_VERTEX_3, map2_vertex3);
        isenabled(GL_MAP2_VERTEX_4, map2_vertex4);
        // ARB_vertex_program
        isenabled(GL_VERTEX_PROGRAM_ARB, vertex_arb);
        isenabled(GL_VERTEX_PROGRAM_TWO_SIDE_ARB, vertex_two_side_arb);
        default:
            errorGL();
            return gles_glIsEnabled(cap);
    }
}
#undef isenabled
#undef clientisenabled
AliasExport(GLboolean,glIsEnabled,,(GLenum cap));
