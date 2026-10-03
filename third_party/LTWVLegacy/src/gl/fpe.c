#include "../glx/hardext.h"
#include "array.h"
#include "debug.h"
#include "enum_info.h"
#include "fpe_shader.h"
#include "glcase.h"
#include "init.h"
#include "loader.h"
#include "matrix.h"
#include "matvec.h"
#include "program.h"
#include "shaderconv.h"

#include "fpe.h"

#define fpe_state_t fpe_state_t
#define fpe_fpe_t fpe_fpe_t
#define kh_fpecachelist_t kh_fpecachelist_t
#include "fpe_cache.h"
#undef fpe_state_t
#undef fpe_fpe_t
#undef kh_fpecachelist_t

#ifndef fpe_cache_t
#   define fpe_cache_t kh_fpecachelist_t
#endif

//#define DEBUG
#ifdef DEBUG
#pragma GCC optimize 0
#define DBG(a) a
#else
#define DBG(a)
#endif

void free_scratch(scratch_t* scratch) {
    for(int i=0; i<scratch->size; ++i)
        free(scratch->scratch[i]);
}

void fpe_Init(glstate_t *glstate) {
    // initialize cache
    glstate->fpe_cache = fpe_NewCache();
}

void fpe_Dispose(glstate_t *glstate) {
    fpe_disposeCache(glstate->fpe_cache, 0);
    glstate->fpe_cache = NULL;
}

void APIENTRY_GL4ES fpe_ReleventState_DefaultVertex(fpe_state_t *dest, fpe_state_t *src, shaderconv_need_t* need)
{
    // filter out some non relevant state (like texture stuff if texture is disabled)
    memcpy(dest, src, sizeof(fpe_state_t));
    // alpha test
    if(!dest->alphatest) {
        dest->alphafunc = FPE_ALWAYS;
    }
    // lighting
    if(!dest->lighting) {
        dest->light = 0;
        dest->light_cutoff180 = 0;
        dest->light_direction = 0;
        dest->twosided = 0;
        dest->color_material = 0;
        dest->cm_front_mode = 0;
        dest->cm_back_mode = 0;
        dest->cm_front_nullexp = 0;
        dest->cm_back_nullexp = 0;
        dest->light_separate = 0;
        dest->light_localviewer = 0;
    } else {
        // indiviual lights
        for (int i=0; i<8; i++) {
            if(((dest->light>>i)&1)==0) {
                dest->light_cutoff180 &= ~(1<<i);
                dest->light_direction &= ~(1<<i);
            }            
        }
    }
    // texturing
    // individual textures
    for (int i=0; i<MAX_TEX; i++) {
        if(!(need->need_texs&(1<<i))) { // texture is off
            dest->texture[i].texmat = 0;
            dest->texture[i].texformat = 0;
            dest->texture[i].texadjust = 0;
            dest->texgen[i].texgen_s = 0;
            dest->texgen[i].texgen_s_mode = 0;
            dest->texgen[i].texgen_t = 0;
            dest->texgen[i].texgen_t_mode = 0;
            dest->texgen[i].texgen_r = 0;
            dest->texgen[i].texgen_r_mode = 0;
            dest->texgen[i].texgen_q = 0;
            dest->texgen[i].texgen_q_mode = 0;
            dest->texenv[i].texrgbscale = 0;
            dest->texenv[i].texalphascale = 0;
        } else {    // texture is on
            if (dest->texgen[i].texgen_s==0)
                dest->texgen[i].texgen_s_mode = 0;
            if (dest->texgen[i].texgen_t==0)
                dest->texgen[i].texgen_t_mode = 0;
            if (dest->texgen[i].texgen_r==0)
                dest->texgen[i].texgen_r_mode = 0;
            if (dest->texgen[i].texgen_q==0)
                dest->texgen[i].texgen_q_mode = 0;
        }
        if((dest->texenv[i].texenv < FPE_COMBINE) || (dest->texture[i].textype==0)) {
            dest->texcombine[i] = 0;
            dest->texenv[i].texsrcrgb0 = 0;
            dest->texenv[i].texsrcalpha0 = 0;
            dest->texenv[i].texoprgb0 = 0;
            dest->texenv[i].texopalpha0 = 0;
            dest->texenv[i].texsrcrgb1 = 0;
            dest->texenv[i].texsrcalpha1 = 0;
            dest->texenv[i].texoprgb1 = 0;
            dest->texenv[i].texopalpha1 = 0;
            dest->texenv[i].texsrcrgb2 = 0;
            dest->texenv[i].texsrcalpha2 = 0;
            dest->texenv[i].texoprgb2 = 0;
            dest->texenv[i].texopalpha2 = 0;
        } else if(dest->texenv[i].texenv != FPE_COMBINE4) {
            dest->texenv[i].texsrcrgb3 = 0;
            dest->texenv[i].texsrcalpha3 = 0;
            dest->texenv[i].texoprgb3 = 0;
            dest->texenv[i].texopalpha3 = 0;
        }
    }
    if(dest->fog && dest->fogsource==FPE_FOG_SRC_COORD)
        dest->fogdist = 0;
    if(!need->need_fogcoord) {
        dest->fogmode = 0;
        dest->fogsource = 0;
        dest->fogdist = 0;
    }
    if(!dest->point)
        dest->pointsprite = 0;
    if(!dest->pointsprite) {
        dest->pointsprite_upper = 0;
        dest->pointsprite_coord = 0;
    }
    if(!dest->blend_enable) {
        dest->blendsrcrgb = 0;
        dest->blenddstrgb = 0;
        dest->blendsrcalpha = 0;
        dest->blenddstalpha = 0;
        dest->blendeqrgb = 0;
        dest->blendeqalpha = 0;
    }
    // ARB_vertex_program and ARB_fragment_program
    dest->vertex_prg_id = 0;    // it's a default vertex program...
    if(!dest->fragment_prg_enable)
        dest->fragment_prg_id = 0;
}

void APIENTRY_GL4ES fpe_ReleventState(fpe_state_t *dest, fpe_state_t *src, int fixed)
{
    // filter out some non relevant state (like texture stuff if texture is disabled)
    memcpy(dest, src, sizeof(fpe_state_t));
    // alpha test
    if(!dest->alphatest) {
        dest->alphafunc = FPE_ALWAYS;
    }
    // lighting
    if(!fixed || !dest->lighting) {
        dest->light = 0;
        dest->light_cutoff180 = 0;
        dest->light_direction = 0;
        dest->twosided = 0;
        dest->color_material = 0;
        dest->cm_front_mode = 0;
        dest->cm_back_mode = 0;
        dest->cm_front_nullexp = 0;
        dest->cm_back_nullexp = 0;
        dest->light_separate = 0;
        dest->light_localviewer = 0;
    } else {
        // indiviual lights
        for (int i=0; i<8; i++) {
            if(((dest->light>>i)&1)==0) {
                dest->light_cutoff180 &= ~(1<<i);
                dest->light_direction &= ~(1<<i);
            }            
        }
    }
    // texturing
    // individual textures
    for (int i=0; i<MAX_TEX; i++) {
        if(dest->texture[i].textype==0) { // texture is off
            dest->texture[i].texmat = 0;
            dest->texture[i].texformat = 0;
            dest->texture[i].texadjust = 0;
            dest->texgen[i].texgen_s = 0;
            dest->texgen[i].texgen_s_mode = 0;
            dest->texgen[i].texgen_t = 0;
            dest->texgen[i].texgen_t_mode = 0;
            dest->texgen[i].texgen_r = 0;
            dest->texgen[i].texgen_r_mode = 0;
            dest->texgen[i].texgen_q = 0;
            dest->texgen[i].texgen_q_mode = 0;
            dest->texenv[i].texrgbscale = 0;
            dest->texenv[i].texalphascale = 0;
        } else {    // texture is on
            if (dest->texgen[i].texgen_s==0)
                dest->texgen[i].texgen_s_mode = 0;
            if (dest->texgen[i].texgen_t==0)
                dest->texgen[i].texgen_t_mode = 0;
            if (dest->texgen[i].texgen_r==0)
                dest->texgen[i].texgen_r_mode = 0;
            if (dest->texgen[i].texgen_q==0)
                dest->texgen[i].texgen_q_mode = 0;
        }
        if((dest->texenv[i].texenv < FPE_COMBINE) || (dest->texture[i].textype==0)) {
            dest->texcombine[i] = 0;
            dest->texenv[i].texsrcrgb0 = 0;
            dest->texenv[i].texsrcalpha0 = 0;
            dest->texenv[i].texoprgb0 = 0;
            dest->texenv[i].texopalpha0 = 0;
            dest->texenv[i].texsrcrgb1 = 0;
            dest->texenv[i].texsrcalpha1 = 0;
            dest->texenv[i].texoprgb1 = 0;
            dest->texenv[i].texopalpha1 = 0;
            dest->texenv[i].texsrcrgb2 = 0;
            dest->texenv[i].texsrcalpha2 = 0;
            dest->texenv[i].texoprgb2 = 0;
            dest->texenv[i].texopalpha2 = 0;
        } else if(dest->texenv[i].texenv != FPE_COMBINE4) {
            dest->texenv[i].texsrcrgb3 = 0;
            dest->texenv[i].texsrcalpha3 = 0;
            dest->texenv[i].texoprgb3 = 0;
            dest->texenv[i].texopalpha3 = 0;
        }
    }
    if(dest->fog && dest->fogsource==FPE_FOG_SRC_COORD)
        dest->fogdist = 0;
    if(!fixed || !dest->fog) {
        dest->fogmode = 0;
        dest->fogsource = 0;
        dest->fogdist = 0;
    }
    if(!fixed || !dest->point)
        dest->pointsprite = 0;
    if(!fixed || !dest->pointsprite) {
        dest->pointsprite_upper = 0;
        dest->pointsprite_coord = 0;
    }
    // ARB_vertex_program and ARB_fragment_program
    if(!fixed || !dest->vertex_prg_enable)
        dest->vertex_prg_id = 0;
    if(!fixed || !dest->fragment_prg_enable)
        dest->fragment_prg_id = 0;

    if(!fixed) {
        for(int i=0; i<MAX_TEX; ++i) {
            dest->texture[i].texmat = 0;
            dest->texture[i].texadjust = 0;
            dest->texture[i].textype = 0;
        }
        dest->colorsum = 0;
        dest->normalize = 0;
        dest->rescaling = 0;

        dest->lighting = 0;
        dest->fog = 0;
        dest->point = 0;

        dest->vertex_prg_enable = 0;
        dest->fragment_prg_enable = 0;
    }
    if(!fixed || !dest->blend_enable) {
        dest->blendsrcrgb = 0;
        dest->blenddstrgb = 0;
        dest->blendsrcalpha = 0;
        dest->blenddstalpha = 0;
        dest->blendeqrgb = 0;
        dest->blendeqalpha = 0;
    }
}

int APIENTRY_GL4ES fpe_IsEmpty(fpe_state_t *state) {
    uint8_t* p = (uint8_t*)state;
    for (int i=0; i<sizeof(fpe_state_t); ++i)
        if(p[i])
            return 0;
    return 1;
}

uniform_t* findUniform(khash_t(uniformlist) *uniforms, const char* name)
{
    uniform_t *m;
    khint_t k;
    kh_foreach(uniforms, k, m,
        if(!strcmp(m->name, name))
            return m;
    )
    return NULL;

}
// ********* Old Program binding Handling *********
void APIENTRY_GL4ES fpe_oldprogram(fpe_state_t* state) {
    LOAD_GLES2(glGetShaderInfoLog);
    LOAD_GLES2(glGetProgramInfoLog);
    GLint status;
    // There is an old program (either vtx or frg or both)
    oldprogram_t* old_vtx = getOldProgram(state->vertex_prg_id);
    oldprogram_t* old_frg = getOldProgram(state->fragment_prg_id);

    glstate->fpe->vert = gl4es_glCreateShader(GL_VERTEX_SHADER);
    if(state->vertex_prg_id) {
        gl4es_glShaderSource(glstate->fpe->vert, 1, fpe_CustomVertexShader(old_vtx->shader->source, state, state->fragment_prg_id?0:1), NULL);
        gl4es_glCompileShader(glstate->fpe->vert);
        gl4es_glGetShaderiv(glstate->fpe->vert, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gles_glGetShaderInfoLog(glstate->fpe->vert, 1000, NULL, buff);
            if(globals4es.logshader)
                printf("LIBGL: FPE ARB Vertex program compile failed: ARB source is\n%s\n=======\nGLSL source is\n%s\nError is: %s\n", old_vtx->string, old_vtx->shader->source, buff);
            else
                printf("LIBGL: FPE ARB Vertex program compile failed: %s\n", buff);
        }
        getShader(glstate->fpe->vert)->old = old_vtx;
    } else {
        // use fragment need to build default vertex shader
        gl4es_glShaderSource(glstate->fpe->vert, 1, fpe_VertexShader(&old_frg->shader->need, state), NULL);
        gl4es_glCompileShader(glstate->fpe->vert);
        gl4es_glGetShaderiv(glstate->fpe->vert, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gles_glGetShaderInfoLog(glstate->fpe->vert, 1000, NULL, buff);
            printf("LIBGL: FPE ARB Default Vertex program compile failed: %s\n", buff);
        }
    }
    gl4es_glAttachShader(glstate->fpe->prog, glstate->fpe->vert);
    glstate->fpe->frag = gl4es_glCreateShader(GL_FRAGMENT_SHADER);
    if(state->fragment_prg_id) {
        gl4es_glShaderSource(glstate->fpe->frag, 1, fpe_CustomFragmentShader(old_frg->shader->source, state), NULL);
        gl4es_glCompileShader(glstate->fpe->frag);
        gl4es_glGetShaderiv(glstate->fpe->frag, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gles_glGetShaderInfoLog(glstate->fpe->frag, 1000, NULL, buff);
            if(globals4es.logshader)
                printf("LIBGL: FPE ARB Fragment program compile failed: ARB source is\n%s\n=======\nGLSL source is\n%s\nError is: %s\n", old_frg->string, old_frg->shader->source, buff);
            else
                printf("LIBGL: FPE ARB Fragment program compile failed: %s\n", buff);
        }
        getShader(glstate->fpe->frag)->old = old_frg;
    } else {
        // use vertex need to build default fragment shader
        gl4es_glShaderSource(glstate->fpe->frag, 1, fpe_FragmentShader(&old_vtx->shader->need, state), NULL);
        gl4es_glCompileShader(glstate->fpe->frag);
        gl4es_glGetShaderiv(glstate->fpe->frag, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gles_glGetShaderInfoLog(glstate->fpe->frag, 1000, NULL, buff);
            printf("LIBGL: FPE ARB Default Fragment program compile failed: %s\n", buff);
        }
    }
    gl4es_glAttachShader(glstate->fpe->prog, glstate->fpe->frag);
    // Ok, and now link the program
    gl4es_glLinkProgram(glstate->fpe->prog);
    gl4es_glGetProgramiv(glstate->fpe->prog, GL_LINK_STATUS, &status);
    if(status!=GL_TRUE) {
        char buff[1000];
        gles_glGetProgramInfoLog(glstate->fpe->prog, 1000, NULL, buff);
        if(globals4es.logshader)
            printf("LIBGL: FPE ARB Program link failed: %s\n with vertex %s%s%s%s%s and fragment %s%s%s%s%s\n", 
                buff, 
                state->vertex_prg_id?"custom:\n":"default", state->vertex_prg_id?old_vtx->string:"", state->vertex_prg_id?"\nconverted:\n":"", state->vertex_prg_id?old_vtx->shader->source:"", state->vertex_prg_id?"\n":"", 
                state->fragment_prg_id?"custom:\n":"default", state->fragment_prg_id?old_frg->string:"", state->fragment_prg_id?"\nconverted:\n":"", state->fragment_prg_id?old_frg->shader->source:"", state->fragment_prg_id?"\n":"");
        else
            printf("LIBGL: FPE ARB Program link failed: %s\n", buff);
    }
    DBG(printf("Created program %d, with vertex=%d (old=%d) fragment=%d (old=%d), alpha=%d/%d\n", glstate->fpe->prog, glstate->fpe->vert, state->vertex_prg_id, glstate->fpe->frag, state->fragment_prg_id, state->alphatest, state->alphafunc);)
}

// ********* Shader stuffs handling *********
void APIENTRY_GL4ES fpe_program(int ispoint) {
    glstate->fpe_state->point = ispoint;
    fpe_state_t state;
    fpe_ReleventState(&state, glstate->fpe_state, 1);
    if(glstate->fpe==NULL || memcmp(&glstate->fpe->state, &state, sizeof(fpe_state_t))) {
        // get cached fpe (or new one)
        glstate->fpe = fpe_GetCache(glstate->fpe_cache, &state, 1);
    }   
    if(glstate->fpe->glprogram==NULL) {
        glstate->fpe->prog = gl4es_glCreateProgram();
        DBG(int from_psa = 1;)
        if(fpe_GetProgramPSA(glstate->fpe->prog, &state)==0) {
            DBG(from_psa = 0;)
            if(state.vertex_prg_id || state.fragment_prg_id) {
                fpe_oldprogram(&state);
            } else {
                LOAD_GLES2(glGetShaderInfoLog);
                LOAD_GLES2(glGetProgramInfoLog);
                GLint status;
                // no old program, using regular FPE
                glstate->fpe->vert = gl4es_glCreateShader(GL_VERTEX_SHADER);
                gl4es_glShaderSource(glstate->fpe->vert, 1, fpe_VertexShader(NULL, glstate->fpe_state), NULL);
                gl4es_glCompileShader(glstate->fpe->vert);
                gl4es_glGetShaderiv(glstate->fpe->vert, GL_COMPILE_STATUS, &status);
                if(status!=GL_TRUE) {
                    char buff[1000];
                    gles_glGetShaderInfoLog(glstate->fpe->vert, 1000, NULL, buff);
                    if(globals4es.logshader)
                        printf("LIBGL: FPE Vertex shader compile failed: source is\n%s\n\nError is: %s\n", fpe_VertexShader(NULL, glstate->fpe_state)[0], buff);
                    else
                        printf("LIBGL: FPE Vertex shader compile failed: %s\n", buff);
                }
                glstate->fpe->frag = gl4es_glCreateShader(GL_FRAGMENT_SHADER);
                gl4es_glShaderSource(glstate->fpe->frag, 1, fpe_FragmentShader(NULL, glstate->fpe_state), NULL);
                gl4es_glCompileShader(glstate->fpe->frag);
                gl4es_glGetShaderiv(glstate->fpe->frag, GL_COMPILE_STATUS, &status);
                if(status!=GL_TRUE) {
                    char buff[1000];
                    gles_glGetShaderInfoLog(glstate->fpe->frag, 1000, NULL, buff);
                    if(globals4es.logshader)
                        printf("LIBGL: FPE Fragment shader compile failed: source is\n%s\n\nError is: %s\n", fpe_FragmentShader(NULL, glstate->fpe_state)[0], buff);
                    else
                        printf("LIBGL: FPE Fragment shader compile failed: %s\n", buff);
                }
                // program is already created
                gl4es_glAttachShader(glstate->fpe->prog, glstate->fpe->vert);
                gl4es_glAttachShader(glstate->fpe->prog, glstate->fpe->frag);
                gl4es_glLinkProgram(glstate->fpe->prog);
                gl4es_glGetProgramiv(glstate->fpe->prog, GL_LINK_STATUS, &status);
                if(status!=GL_TRUE) {
                    char buff[1000];
                    gles_glGetProgramInfoLog(glstate->fpe->prog, 1000, NULL, buff);
                    if(globals4es.logshader) {
                        printf("LIBGL: FPE Program link failed: source of vertex shader is\n%s\n\n", fpe_VertexShader(NULL, glstate->fpe_state)[0]);
                        printf("source of fragment shader is \n%s\n\nError is: %s\n", fpe_FragmentShader(NULL, glstate->fpe_state)[0], buff);
                    } else
                        printf("LIBGL: FPE Program link failed: %s\n", buff);
                }
                fpe_AddProgramPSA(glstate->fpe->prog, &state);
            }
        }
        // now find the program
        {
            khint_t k_program;
            khash_t(programlist) *programs = glstate->glsl->programs;
            k_program = kh_get(programlist, programs, glstate->fpe->prog);
            if (k_program != kh_end(programs))
                glstate->fpe->glprogram = kh_value(programs, k_program);
        }
        // all done
        DBG(printf("%s FPE shader : %d(%p)\n", from_psa?"Using Precomp":"Creating", glstate->fpe->prog, glstate->fpe->glprogram);)
    }
}

program_t* APIENTRY_GL4ES fpe_CustomShader(program_t* glprogram, fpe_state_t* state)
{
    // state is not empty and glprogram already has some cache (it may be empty, but kh'thingy is initialized)
    // TODO: what if program is composed of more then 1 vertex or fragment shader?
    fpe_fpe_t *fpe = fpe_GetCache((fpe_cache_t*)glprogram->fpe_cache, state, 0);
    if(fpe->glprogram==NULL) {
        GLint status;
        fpe->vert = gl4es_glCreateShader(GL_VERTEX_SHADER);
        gl4es_glShaderSource(fpe->vert, 1, fpe_CustomVertexShader(glprogram->last_vert->source, state, 0), NULL);
        gl4es_glCompileShader(fpe->vert);
        gl4es_glGetShaderiv(fpe->vert, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gl4es_glGetShaderInfoLog(fpe->vert, 1000, NULL, buff);
            printf("LIBGL: FPE Custom Vertex shader compile failed: %s\n", buff);
            return glprogram;   // fallback to non-customized custom program..
        }
        fpe->frag = gl4es_glCreateShader(GL_FRAGMENT_SHADER);
        gl4es_glShaderSource(fpe->frag, 1, fpe_CustomFragmentShader(glprogram->last_frag->source, state), NULL);
        gl4es_glCompileShader(fpe->frag);
        gl4es_glGetShaderiv(fpe->frag, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gl4es_glGetShaderInfoLog(fpe->frag, 1000, NULL, buff);
            printf("LIBGL: FPE Custom Fragment shader compile failed: %s\n", buff);
            return glprogram;   // fallback to non-customized custom program..
        }
        fpe->prog = gl4es_glCreateProgram();
        gl4es_glAttachShader(fpe->prog, fpe->vert);
        gl4es_glAttachShader(fpe->prog, fpe->frag);
        // re-run the BindAttribLocation if any
        {
            attribloc_t *al;
            LOAD_GLES2(glBindAttribLocation);   // using real one to avoid overwriting of attribloc...
            kh_foreach_value(glprogram->attribloc, al,
                gles_glBindAttribLocation(fpe->prog, al->index, al->name);
            );
        }
        gl4es_glLinkProgram(fpe->prog);
        gl4es_glGetProgramiv(fpe->prog, GL_LINK_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gl4es_glGetProgramInfoLog(fpe->prog, 1000, NULL, buff);
            printf("LIBGL: FPE Custom Program link failed: %s\n", buff);
            return glprogram;   // fallback to non-customized custom program..
        }
        // now find the program
        khint_t k_program;
        {
            khash_t(programlist) *programs = glstate->glsl->programs;
            k_program = kh_get(programlist, programs, fpe->prog);
            if (k_program != kh_end(programs))
                fpe->glprogram = kh_value(programs, k_program);
        }
        // adjust the uniforms to point to father cache...
        {
            khash_t(uniformlist) *father_uniforms = glprogram->uniform;
            khash_t(uniformlist) *uniforms = fpe->glprogram->uniform;
            uniform_t *m, *n;
            khint_t k;
            kh_foreach(uniforms, k, m,
                if(!m->builtin) {
                    n = findUniform(father_uniforms, m->name);
                    if(n) {
                        m->parent_offs = n->cache_offs;
                        m->parent_size = n->cache_size;
                    }
                }
            )
        }
        // all done
        DBG(printf("creating FPE Custom Program : %d(%p)\n", fpe->prog, fpe->glprogram);)
    }

    return fpe->glprogram;
}

program_t* APIENTRY_GL4ES fpe_CustomShader_DefaultVertex(program_t* glprogram, fpe_state_t* state_vertex)
{
    // state is not empty and glprogram already has some cache (it may be empty, but kh'thingy is initialized)
    // TODO: what if program is composed of more then 1 vertex or fragment shader?
    fpe_fpe_t *fpe = fpe_GetCache((fpe_cache_t*)glprogram->fpe_cache, state_vertex, 0);
    if(fpe->glprogram==NULL) {
        GLint status;
        fpe->vert = gl4es_glCreateShader(GL_VERTEX_SHADER);
        gl4es_glShaderSource(fpe->vert, 1, fpe_VertexShader(glprogram->default_need, state_vertex), NULL);
        gl4es_glCompileShader(fpe->vert);
        gl4es_glGetShaderiv(fpe->vert, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gl4es_glGetShaderInfoLog(fpe->vert, 1000, NULL, buff);
            printf("LIBGL: FPE Default Vertex shader compile failed: %s\n", buff);
            return glprogram;   // fallback to non-customized custom program..
        }
        fpe->frag = gl4es_glCreateShader(GL_FRAGMENT_SHADER);
        gl4es_glShaderSource(fpe->frag, 1, fpe_CustomFragmentShader(glprogram->last_frag->source, state_vertex), NULL);
        gl4es_glCompileShader(fpe->frag);
        gl4es_glGetShaderiv(fpe->frag, GL_COMPILE_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gl4es_glGetShaderInfoLog(fpe->frag, 1000, NULL, buff);
            printf("LIBGL: FPE Custom Fragment shader compile failed: %s\n", buff);
            return glprogram;   // fallback to non-customized custom program..
        }
        fpe->prog = gl4es_glCreateProgram();
        gl4es_glAttachShader(fpe->prog, fpe->vert);
        gl4es_glAttachShader(fpe->prog, fpe->frag);
        // re-run the BindAttribLocation if any
        {
            attribloc_t *al;
            LOAD_GLES2(glBindAttribLocation);   // using real one to avoid overwriting of attribloc...
            kh_foreach_value(glprogram->attribloc, al,
                gles_glBindAttribLocation(fpe->prog, al->index, al->name);
            );
        }
        gl4es_glLinkProgram(fpe->prog);
        gl4es_glGetProgramiv(fpe->prog, GL_LINK_STATUS, &status);
        if(status!=GL_TRUE) {
            char buff[1000];
            gl4es_glGetProgramInfoLog(fpe->prog, 1000, NULL, buff);
            printf("LIBGL: FPE Custom Program with Default Vertex link failed: %s\n", buff);
            return glprogram;   // fallback to non-customized custom program..
        }
        // now find the program
        khint_t k_program;
        {
            khash_t(programlist) *programs = glstate->glsl->programs;
            k_program = kh_get(programlist, programs, fpe->prog);
            if (k_program != kh_end(programs))
                fpe->glprogram = kh_value(programs, k_program);
        }
        // adjust the uniforms to point to father cache...
        {
            khash_t(uniformlist) *father_uniforms = glprogram->uniform;
            khash_t(uniformlist) *uniforms = fpe->glprogram->uniform;
            uniform_t *m, *n;
            khint_t k;
            kh_foreach(uniforms, k, m,
                if(!m->builtin) {
                    n = findUniform(father_uniforms, m->name);
                    if(n) {
                        m->parent_offs = n->cache_offs;
                        m->parent_size = n->cache_size;
                    }
                }
            )
        }
        // all done
        DBG(printf("creating FPE Custom Program : %d(%p)\n", fpe->prog, fpe->glprogram);)
    }

    return fpe->glprogram;
}

void APIENTRY_GL4ES fpe_SyncUniforms(uniformcache_t *cache, program_t* glprogram) {
    //TODO: Optimize this...
    khash_t(uniformlist) *uniforms = glprogram->uniform;
    uniform_t *m;
    khint_t k;
    DBG(int cnt = 0;)
    // don't use m->size, as each element has it's own uniform...
    kh_foreach(uniforms, k, m,
        if(m->parent_size) {
            DBG(++cnt;)
            switch(m->type) {
                case GL_FLOAT:
                case GL_FLOAT_VEC2:
                case GL_FLOAT_VEC3:
                case GL_FLOAT_VEC4:
                    GoUniformfv(glprogram, m->id, n_uniform(m->type), 1, (GLfloat*)((uintptr_t)cache->cache+m->parent_offs));
                    break;
                case GL_SAMPLER_2D:
                case GL_SAMPLER_CUBE:
                case GL_INT:
                case GL_INT_VEC2:
                case GL_INT_VEC3:
                case GL_INT_VEC4:
                case GL_BOOL:
                case GL_BOOL_VEC2:
                case GL_BOOL_VEC3:
                case GL_BOOL_VEC4:
                    GoUniformiv(glprogram, m->id, n_uniform(m->type), 1, (GLint*)((uintptr_t)cache->cache+m->parent_offs));
                    break;
                case GL_FLOAT_MAT2:
                    GoUniformMatrix2fv(glprogram, m->id, 1, false, (GLfloat*)((uintptr_t)cache->cache+m->parent_offs));
                    break;
                case GL_FLOAT_MAT3:
                    GoUniformMatrix3fv(glprogram, m->id, 1, false, (GLfloat*)((uintptr_t)cache->cache+m->parent_offs));
                    break;
                case GL_FLOAT_MAT4:
                    GoUniformMatrix4fv(glprogram, m->id, 1, false, (GLfloat*)((uintptr_t)cache->cache+m->parent_offs));
                    break;
                default:
                    printf("LIBGL: Warning, sync uniform on father/son program with unknown uniform type %s\n", PrintEnum(m->type));
            }
        }
    );
    DBG(printf("Uniform sync'd with %d and father (%d uniforms)\n", glprogram->id, cnt);)
}
// ********* Fixed Pipeling function wrapper *********

void APIENTRY_GL4ES fpe_glClientActiveTexture(GLenum texture) {
    DBG(printf("fpe_glClientActiveTexture(%s)\n", PrintEnum(texture));)
}

void APIENTRY_GL4ES fpe_EnableDisableClientState(GLenum cap, GLboolean val) {
    int att = -1;
        switch(cap) {
        case GL_VERTEX_ARRAY:
            att = ATT_VERTEX;
            break;
        case GL_COLOR_ARRAY:
            att = ATT_COLOR;
            break;
        case GL_NORMAL_ARRAY:
            att = ATT_NORMAL;
            break;
        case GL_TEXTURE_COORD_ARRAY:
            att = ATT_MULTITEXCOORD0+glstate->texture.client;
            break;
        case GL_SECONDARY_COLOR_ARRAY:
            att = ATT_SECONDARY;
            break;
        case GL_FOG_COORD_ARRAY:
            att = ATT_FOGCOORD;
            break;
        default:
            return; //???
    }
    if(hardext.esversion==1) {
        // actually send that to GLES1.1 hardware!
        if(glstate->gleshard->vertexattrib[att].enabled!=val) {
            glstate->gleshard->vertexattrib[att].enabled=val;
            LOAD_GLES(glEnableClientState);
            LOAD_GLES(glDisableClientState);
            if(val)
                gles_glEnableClientState(cap);
            else
                gles_glDisableClientState(cap);
        }
    } else {
DBG(printf("glstate->vao->vertexattrib[%d].enabled (was %d) = %d (hardware=%d)\n", att, glstate->vao->vertexattrib[att].enabled, val, glstate->gleshard->vertexattrib[att].enabled);)
        glstate->vao->vertexattrib[att].enabled = val;
    }
}

void APIENTRY_GL4ES fpe_glEnableClientState(GLenum cap) {
    DBG(printf("fpe_glEnableClientState(%s)\n", PrintEnum(cap));)
    fpe_EnableDisableClientState(cap, GL_TRUE);
}

void APIENTRY_GL4ES fpe_glDisableClientState(GLenum cap) {
    DBG(printf("fpe_glDisableClientState(%s)\n", PrintEnum(cap));)
    fpe_EnableDisableClientState(cap, GL_FALSE);
}

void APIENTRY_GL4ES fpe_glMultiTexCoord4f(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q) {
}

void APIENTRY_GL4ES fpe_glSecondaryColorPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("fpe_glSecondaryColorPointer(%d, %s, %d, %p)\n", size, PrintEnum(type), stride, pointer);)
    glstate->vao->vertexattrib[ATT_SECONDARY].size = size;
    glstate->vao->vertexattrib[ATT_SECONDARY].type = type;
    glstate->vao->vertexattrib[ATT_SECONDARY].stride = stride;
    glstate->vao->vertexattrib[ATT_SECONDARY].pointer = pointer;
    glstate->vao->vertexattrib[ATT_SECONDARY].divisor = 0;
    glstate->vao->vertexattrib[ATT_SECONDARY].normalized = (type==GL_FLOAT)?GL_FALSE:GL_TRUE;
    glstate->vao->vertexattrib[ATT_SECONDARY].real_buffer = 0;
    glstate->vao->vertexattrib[ATT_SECONDARY].real_pointer = 0;
    glstate->vao->vertexattrib[ATT_SECONDARY].buffer = glstate->vao->vertex;

}

void APIENTRY_GL4ES fpe_glVertexPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("fpe_glVertexPointer(%d, %s, %d, %p), vertex_buffer=%p\n", size, PrintEnum(type), stride, pointer, glstate->vao->vertex);)
    glstate->vao->vertexattrib[ATT_VERTEX].size = size;
    glstate->vao->vertexattrib[ATT_VERTEX].type = type;
    glstate->vao->vertexattrib[ATT_VERTEX].stride = stride;
    glstate->vao->vertexattrib[ATT_VERTEX].pointer = pointer;
    glstate->vao->vertexattrib[ATT_VERTEX].divisor = 0;
    glstate->vao->vertexattrib[ATT_VERTEX].normalized = GL_FALSE;
    glstate->vao->vertexattrib[ATT_VERTEX].real_buffer = 0;
    glstate->vao->vertexattrib[ATT_VERTEX].real_pointer = 0;
    glstate->vao->vertexattrib[ATT_VERTEX].buffer = glstate->vao->vertex;
}

void APIENTRY_GL4ES fpe_glColorPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("fpe_glColorPointer(%d, %s, %d, %p)\n", size, PrintEnum(type), stride, pointer);)
    glstate->vao->vertexattrib[ATT_COLOR].size = size;
    glstate->vao->vertexattrib[ATT_COLOR].type = type;
    glstate->vao->vertexattrib[ATT_COLOR].stride = stride;
    glstate->vao->vertexattrib[ATT_COLOR].pointer = pointer;
    glstate->vao->vertexattrib[ATT_COLOR].divisor = 0;
    glstate->vao->vertexattrib[ATT_COLOR].normalized = (type==GL_FLOAT)?GL_FALSE:GL_TRUE;
    glstate->vao->vertexattrib[ATT_COLOR].real_buffer = 0;
    glstate->vao->vertexattrib[ATT_COLOR].real_pointer = 0;
    glstate->vao->vertexattrib[ATT_COLOR].buffer = glstate->vao->vertex;
}

void APIENTRY_GL4ES fpe_glNormalPointer(GLenum type, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("fpe_glNormalPointer(%s, %d, %p)\n", PrintEnum(type), stride, pointer);)
    glstate->vao->vertexattrib[ATT_NORMAL].size = 3;
    glstate->vao->vertexattrib[ATT_NORMAL].type = type;
    glstate->vao->vertexattrib[ATT_NORMAL].stride = stride;
    glstate->vao->vertexattrib[ATT_NORMAL].pointer = pointer;
    glstate->vao->vertexattrib[ATT_NORMAL].divisor = 0;
    glstate->vao->vertexattrib[ATT_NORMAL].normalized = (type==GL_FLOAT)?GL_FALSE:GL_TRUE;
    glstate->vao->vertexattrib[ATT_NORMAL].real_buffer = 0;
    glstate->vao->vertexattrib[ATT_NORMAL].real_pointer = 0;
    glstate->vao->vertexattrib[ATT_NORMAL].buffer = glstate->vao->vertex;
}

void APIENTRY_GL4ES fpe_glTexCoordPointer(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer) {
    fpe_glTexCoordPointerTMU(size, type, stride, pointer, glstate->texture.client);
}

void APIENTRY_GL4ES fpe_glTexCoordPointerTMU(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer, int TMU) {
    DBG(printf("fpe_glTexCoordPointer(%d, %s, %d, %p) on tmu=%d\n", size, PrintEnum(type), stride, pointer, TMU);)
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].size = size;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].type = type;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].stride = stride;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].pointer = pointer;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].divisor = 0;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].normalized = GL_FALSE;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].real_buffer = 0;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].real_pointer = 0;
    glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+TMU].buffer = glstate->vao->vertex;
}

void APIENTRY_GL4ES fpe_glFogCoordPointer(GLenum type, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("fpe_glFogPointer(%s, %d, %p)\n", PrintEnum(type), stride, pointer);)
    glstate->vao->vertexattrib[ATT_FOGCOORD].size = 1;
    glstate->vao->vertexattrib[ATT_FOGCOORD].type = type;
    glstate->vao->vertexattrib[ATT_FOGCOORD].stride = stride;
    glstate->vao->vertexattrib[ATT_FOGCOORD].pointer = pointer;
    glstate->vao->vertexattrib[ATT_FOGCOORD].divisor = 0;
    glstate->vao->vertexattrib[ATT_FOGCOORD].normalized = (type==GL_FLOAT)?GL_FALSE:GL_TRUE;
    glstate->vao->vertexattrib[ATT_FOGCOORD].real_buffer = 0;
    glstate->vao->vertexattrib[ATT_FOGCOORD].real_pointer = 0;
    glstate->vao->vertexattrib[ATT_FOGCOORD].buffer = glstate->vao->vertex;
}

void APIENTRY_GL4ES fpe_glEnable(GLenum cap) {
    gl4es_glEnable(cap);    // may reset fpe curent program
}
void APIENTRY_GL4ES fpe_glDisable(GLenum cap) {
    gl4es_glDisable(cap);   // may reset fpe curent program
}

void APIENTRY_GL4ES fpe_glColor4f(GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha) {
    noerrorShim();
}

void APIENTRY_GL4ES fpe_glNormal3f(GLfloat nx, GLfloat ny, GLfloat nz) {
    noerrorShim();
}

void APIENTRY_GL4ES fpe_glDrawArrays(GLenum mode, GLint first, GLsizei count) {
    DBG(printf("fpe_glDrawArrays(%s, %d, %d), program=%d, instanceID=%u\n", PrintEnum(mode), first, count, glstate->glsl->program, glstate->instanceID);)
    scratch_t scratch = {0};
    realize_glenv(mode==GL_POINTS, first, count, 0, NULL, &scratch);
    LOAD_GLES(glDrawArrays);
    gles_glDrawArrays(mode, first, count);
    free_scratch(&scratch);
}

void APIENTRY_GL4ES fpe_glDrawElements(GLenum mode, GLsizei count, GLenum type, const GLvoid *indices) {
    DBG(printf("fpe_glDrawElements(%s, %d, %s, %p), program=%d, instanceID=%u\n", PrintEnum(mode), count, PrintEnum(type), indices, glstate->glsl->program, glstate->instanceID);)
    scratch_t scratch = {0};
    realize_glenv(mode==GL_POINTS, 0, count, type, indices, &scratch);
    LOAD_GLES(glDrawElements);
    int use_vbo = 0;
    if(glstate->vao->elements && glstate->vao->elements->real_buffer && indices>=glstate->vao->elements->data && indices<=((void*)((char*)glstate->vao->elements->data+glstate->vao->elements->size))) {
        use_vbo = 1;
        bindBuffer(GL_ELEMENT_ARRAY_BUFFER, glstate->vao->elements->real_buffer);
        indices = (GLvoid*)((uintptr_t)indices - (uintptr_t)(glstate->vao->elements->data));
        DBG(printf("Using VBO %d for indices\n", glstate->vao->elements->real_buffer);)
    }
    realize_bufferIndex();
    gles_glDrawElements(mode, count, type, indices);
    if(use_vbo)
        wantBufferIndex(0);
    free_scratch(&scratch);
}
void APIENTRY_GL4ES fpe_glDrawArraysInstanced(GLenum mode, GLint first, GLsizei count, GLsizei primcount) {
    DBG(printf("fpe_glDrawArraysInstanced(%s, %d, %d, %d), program=%d\n", PrintEnum(mode), first, count, primcount, glstate->glsl->program);)
    LOAD_GLES(glDrawArrays);
    LOAD_GLES2(glVertexAttrib4fv);
    scratch_t scratch = {0};
    GLfloat tmp[4] = {0.0f, 0.0f, 0.0f, 1.0f};
    realize_glenv(mode==GL_POINTS, first, count, 0, NULL, &scratch);
    program_t *glprogram = glstate->gleshard->glprogram;
    for (GLint id=0; id<primcount; ++id) {
        GoUniformiv(glprogram, glprogram->builtin_instanceID, 1, 1, &id);
        for(int i=0; i<hardext.maxvattrib; i++) 
        if(glprogram->va_size[i])   // only check used VA...
        {
            vertexattrib_t *w = &glstate->vao->vertexattrib[i];
            if(w->divisor && w->enabled) {
                char* current = (char*)((uintptr_t)w->pointer + ((w->buffer)?(uintptr_t)w->buffer->data:0));
                int stride=w->stride;
                if(!stride) stride=gl_sizeof(w->type)*w->size;
                current += (id/w->divisor) * stride;
                if(w->type==GL_FLOAT) {
                    if(w->size!=4) {
                        memcpy(tmp, current, sizeof(GLfloat)*w->size);
                        current = (char*)tmp;
                    }
                } else {
                    if(w->type == GL_DOUBLE || !w->normalized) {
                        for(int k=0; k<w->size; ++k) {
                            GL_TYPE_SWITCH(input, current, w->type,
                                tmp[k] = input[k];
                            ,)
                        }
                    } else {
                        for(int k=0; k<w->size; ++k) {
                            GL_TYPE_SWITCH_MAX(input, current, w->type,
                                tmp[k] = (float)input[k]/(float)maxv;
                            ,)
                        }
                    }
                    current = (char*)tmp;
                }
                if(memcmp(glstate->gleshard->vavalue[i], current, 4*sizeof(GLfloat))) {
                    memcpy(glstate->gleshard->vavalue[i], current, 4*sizeof(GLfloat));
                    gles_glVertexAttrib4fv(i, glstate->gleshard->vavalue[i]);
                }
            }
        }
        gles_glDrawArrays(mode, first, count);
    }
    free_scratch(&scratch);
}
void APIENTRY_GL4ES fpe_glDrawElementsInstanced(GLenum mode, GLsizei count, GLenum type, const GLvoid *indices, GLsizei primcount) {
    DBG(printf("fpe_glDrawElementsInstanced(%s, %d, %s, %p, %d), program=%d\n", PrintEnum(mode), count, PrintEnum(type), indices, primcount, glstate->glsl->program);)
    LOAD_GLES(glDrawElements);
    LOAD_GLES2(glVertexAttrib4fv);
    scratch_t scratch = {0};
    realize_glenv(mode==GL_POINTS, 0, count, type, indices, &scratch);
    program_t *glprogram = glstate->gleshard->glprogram;
    int use_vbo = 0;
    void* inds;
    GLfloat tmp[4] = {0.0f, 0.0f, 0.0f, 1.0f};
    if(glstate->vao->elements && glstate->vao->elements->real_buffer && indices>=glstate->vao->elements->data && indices<=((void*)((char*)glstate->vao->elements->data+glstate->vao->elements->size))) {
        use_vbo = 1;
        bindBuffer(GL_ELEMENT_ARRAY_BUFFER, glstate->vao->elements->real_buffer);
        inds = (void*)((uintptr_t)indices - (uintptr_t)(glstate->vao->elements->data));
    } else {
        inds = (void*)indices;
        bindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    //realize_bufferIndex();    // not useful here
    for (GLint id=0; id<primcount; ++id) {
        GoUniformiv(glprogram, glprogram->builtin_instanceID, 1, 1, &id);
        for(int i=0; i<hardext.maxvattrib; i++) 
        if(glprogram->va_size[i])   // only check used VA...
        {
            vertexattrib_t *w = &glstate->vao->vertexattrib[i];
            if(w->divisor && w->enabled) {
                char* current = (char*)((uintptr_t)w->pointer + ((w->buffer)?(uintptr_t)w->buffer->data:0));
                int stride=w->stride;
                if(!stride) stride=gl_sizeof(w->type)*w->size;
                current += (id/w->divisor) * stride;
                if(w->type==GL_FLOAT) {
                    if(w->size!=4) {
                        memcpy(tmp, current, sizeof(GLfloat)*w->size);
                        current = (char*)tmp;
                    }
                } else {
                    if(w->type == GL_DOUBLE || !w->normalized) {
                        for(int k=0; k<w->size; ++k) {
                            GL_TYPE_SWITCH(input, current, w->type,
                                tmp[k] = input[k];
                            ,)
                        }
                    } else {
                        for(int k=0; k<w->size; ++k) {
                            GL_TYPE_SWITCH_MAX(input, current, w->type,
                                tmp[k] = (float)input[k]/(float)maxv;
                            ,)
                        }
                    }
                    current = (char*)tmp;
                }
                if(memcmp(glstate->gleshard->vavalue[i], current, 4*sizeof(GLfloat))) {
                    memcpy(glstate->gleshard->vavalue[i], current, 4*sizeof(GLfloat));
                    gles_glVertexAttrib4fv(i, glstate->gleshard->vavalue[i]);
                }
            }
        }
        gles_glDrawElements(mode, count, type, inds);
    }
    if(use_vbo)
        wantBufferIndex(0);
    free_scratch(&scratch);
}


void APIENTRY_GL4ES fpe_glMatrixMode(GLenum mode) {
    noerrorShim();
}

void APIENTRY_GL4ES fpe_glLightModelf(GLenum pname, GLfloat param) {
    noerrorShim();
}
void APIENTRY_GL4ES fpe_glLightModelfv(GLenum pname, const GLfloat* params) {
    noerrorShim();
}
void APIENTRY_GL4ES fpe_glLightfv(GLenum light, GLenum pname, const GLfloat* params) {
    noerrorShim();
}
void APIENTRY_GL4ES fpe_glMaterialfv(GLenum face, GLenum pname, const GLfloat *params) {
    noerrorShim();
}
void APIENTRY_GL4ES fpe_glMaterialf(GLenum face, GLenum pname, const GLfloat param) {
    if(face==GL_FRONT_AND_BACK || face==GL_FRONT) {
        glstate->fpe_state->cm_front_nullexp=(param<=0.0)?0:1;
    }
    if(face==GL_FRONT_AND_BACK || face==GL_BACK) {
        glstate->fpe_state->cm_back_nullexp=(param<=0.0)?0:1;
    }
    noerrorShim();
}

void APIENTRY_GL4ES fpe_glFogfv(GLenum pname, const GLfloat* params) {
    noerrorShim();
    if(pname==GL_FOG_MODE) {
        int p = *params;
        switch(p) {
            case GL_EXP: glstate->fpe_state->fogmode = FPE_FOG_EXP; break;
            case GL_EXP2: glstate->fpe_state->fogmode = FPE_FOG_EXP2; break;
            case GL_LINEAR: glstate->fpe_state->fogmode = FPE_FOG_LINEAR; break;
            default: errorShim(GL_INVALID_ENUM);
        }
    } else if (pname==GL_FOG_COORDINATE_SOURCE) {
        int p = *params;
        switch(p) {
            case GL_FRAGMENT_DEPTH: glstate->fpe_state->fogsource = FPE_FOG_SRC_DEPTH; break;
            case GL_FOG_COORD: glstate->fpe_state->fogsource = FPE_FOG_SRC_COORD; break;
            default: errorShim(GL_INVALID_ENUM);
        }
    } else if (pname==GL_FOG_DISTANCE_MODE_NV) {
        int p = *params;
        switch(p) {
            case GL_EYE_PLANE_ABSOLUTE_NV: glstate->fpe_state->fogdist = FPE_FOG_DIST_PLANE_ABS; break;
            case GL_EYE_PLANE: glstate->fpe_state->fogdist = FPE_FOG_DIST_PLANE; break;
            case GL_EYE_RADIAL_NV: glstate->fpe_state->fogdist = FPE_FOG_DIST_RADIAL; break;
            default: errorShim(GL_INVALID_ENUM);
        }
    }
}

void APIENTRY_GL4ES fpe_glPointParameterfv(GLenum pname, const GLfloat * params) {
    noerrorShim();
}
void APIENTRY_GL4ES fpe_glPointSize(GLfloat size) {
    noerrorShim();
}

void APIENTRY_GL4ES fpe_glAlphaFunc(GLenum func, GLclampf ref) {
    noerrorShim();
    int f = FPE_ALWAYS;
    switch(func) {
        case GL_NEVER: f=FPE_NEVER; break;
        case GL_LESS: f=FPE_LESS; break;
        case GL_EQUAL: f=FPE_EQUAL; break;
        case GL_LEQUAL: f=FPE_LEQUAL; break;
        case GL_GREATER: f=FPE_GREATER; break;
        case GL_NOTEQUAL: f=FPE_NOTEQUAL; break;
        case GL_GEQUAL: f=FPE_GEQUAL; break;
    }
    if(glstate->fpe_state->alphafunc != f) {
        glstate->fpe = NULL;
        glstate->fpe_state->alphafunc = f;
    }
}


// ********* Realize GLES Environnements *********

int fpe_gettexture(int TMU) {
    int state=glstate->enable.texture[TMU];
    int target = -1;
    #define GO(A) if(IS_##A(state) && glstate->texture.bound[TMU][ENABLED_##A]->valid) target = ENABLED_##A
    GO(CUBE_MAP);
    else GO(TEX3D);
    else GO(TEXTURE_RECTANGLE);
    else GO(TEX2D);
    else GO(TEX1D);
    #undef GO
    return target;
}

void realize_glenv(int ispoint, int first, int count, GLenum type, const void* indices, scratch_t* scratch) {
    // the handling of GL_BGRA size of GL_DOUBLE using 1 scratch in not ideal, and a waste when dealing with Buffers
    // TODO: have the scratch buffer part of the VBO, and tag it dirty when buffer is changed (or always dirty for VBO 0)
    if(hardext.esversion==1) return;
    LOAD_GLES2(glEnableVertexAttribArray)
    LOAD_GLES2(glDisableVertexAttribArray);
    LOAD_GLES2(glVertexAttribPointer);
    LOAD_GLES2(glVertexAttribIPointer);
    LOAD_GLES2(glVertexAttrib4fv);
    LOAD_GLES2(glUseProgram);
    // update texture state for fpe only
    if(glstate->fpe_bound_changed && !glstate->glsl->program) {
        for(int i=0; i<glstate->fpe_bound_changed; i++) {
            glstate->fpe_state->texture[i].texformat = 0;
            glstate->fpe_state->texture[i].texadjust = 0;
            // disable texture unit, in that case (binded texture iconsts not valid)
            glstate->fpe_state->texture[i].textype = 0;
            int texunit = fpe_gettexture(i);
            gltexture_t* tex = (texunit==-1)?NULL:glstate->texture.bound[i][texunit];
            if(tex && tex->valid) {
                int fmt;
                if(texunit==ENABLED_CUBE_MAP) fmt = FPE_TEX_CUBE;
                else {
#ifdef TEXSTREAM
                    if(tex->streamingID!=-1)
                        fmt = FPE_TEX_STRM;
                    else
#endif
                    if(texunit==ENABLED_TEXTURE_RECTANGLE) fmt = FPE_TEX_RECT;
                    else if(texunit==ENABLED_TEX3D) fmt = FPE_TEX_3D;
                    else fmt = FPE_TEX_2D;
                }
                glstate->fpe_state->texture[i].texformat = tex->fpe_format;
                glstate->fpe_state->texture[i].texadjust = tex->adjust;
                if(texunit==ENABLED_TEXTURE_RECTANGLE) glstate->fpe_state->texture[i].texadjust = 1;
                glstate->fpe_state->texture[i].textype = fmt;
            }
        }
        glstate->fpe_bound_changed = 0;
    }
    // activate program if needed
    if(gl4es_glIsProgram(glstate->glsl->program)) {
        // but first, check if some fixedpipeline state (like GL_ALPHA_TEST) need to alter the original program
        fpe_state_t state;
        fpe_ReleventState(&state, glstate->fpe_state, 0);
        GLuint program = glstate->glsl->program;
        program_t *glprogram = glstate->glsl->glprogram;
        if(glprogram->default_vertex) {
            fpe_state_t vertex_state;
            fpe_ReleventState_DefaultVertex(&vertex_state, glstate->fpe_state, glprogram->default_need);
            if(!glprogram->fpe_cache)
                glprogram->fpe_cache = fpe_NewCache();
            glprogram = fpe_CustomShader_DefaultVertex(glprogram, &vertex_state);    // fetch from cache if exist or create it
            program = glprogram->id;
        } else if(!fpe_IsEmpty(&state))
        {
            // need to create a new program for that...
            DBG(printf("GLSL program %d need customization => ", program);)
            if(!glprogram->fpe_cache)
                glprogram->fpe_cache = fpe_NewCache();
            glprogram = fpe_CustomShader(glprogram, &state);    // fetch from cache if exist or create it
            program = glprogram->id;
            DBG(printf("%d\n", program);)
        }
        if(glstate->gleshard->program != program)
        {
            glstate->gleshard->program = program;
            glstate->gleshard->glprogram = glprogram;
            if (gl4es_glIsProgram(glstate->gleshard->program)) {
              gles_glUseProgram(glstate->gleshard->program);
              DBG(printf("Use GLSL program %d\n", glstate->gleshard->program);)
            }
        }
        // synchronize uniforms with parent!
        if(glprogram != glstate->glsl->glprogram)
            fpe_SyncUniforms(&glstate->glsl->glprogram->cache, glprogram);
    } else {
        fpe_program(ispoint);
        if(glstate->gleshard->program != glstate->fpe->prog)
        {
            glstate->gleshard->program = glstate->fpe->prog;
            glstate->gleshard->glprogram = glstate->fpe->glprogram;
            if (gl4es_glIsProgram(glstate->gleshard->program)) {
              gles_glUseProgram(glstate->gleshard->program);
              DBG(printf("Use FPE program %d\n", glstate->gleshard->program);)
            }
        }
    }
    program_t *glprogram = glstate->gleshard->glprogram;
    // Texture Unit managements
    int need_hackfbo = 0;
    int tu_idx = 0;
    while(tu_idx<MAX_TEX && glprogram->texunits[tu_idx].type) {
        // grab the uniform value
        glprogram->texunits[tu_idx].req_tu = GetUniformi(glprogram, glprogram->texunits[tu_idx].id);
        glprogram->texunits[tu_idx].act_tu = glprogram->texunits[tu_idx].req_tu;
        ++tu_idx;
    }
    if(globals4es.fbounbind && glstate->fbo.current_fb->id) {
        // check if need to unbind/bind fbo because texture is both attached and used
        tu_idx = 0;
        int need = 0;
        gltexture_t * tex = NULL;
        while(tu_idx<MAX_TEX && glprogram->texunits[tu_idx].type && !need) {
            tex = glstate->texture.bound[glprogram->texunits[tu_idx].req_tu][glprogram->texunits[tu_idx].type - 1];
            if(tex && tex->binded_fbo==glstate->fbo.current_fb->id) {
                DBG(printf("Texture %d is used on Uniform %s (%d) and binded on FBO %d Attachement=%s\n", tex->glname, GetUniformName(glprogram, glprogram->texunits[tu_idx].id), glprogram->texunits[tu_idx].id, glstate->fbo.current_fb->id, PrintEnum(tex->binded_attachment));)
                need = 1;
            }
            ++tu_idx;
        }
        if(need && tex) {
            DBG(printf("LIBGL: Need to Bind/Unbind FBO!");)
            LOAD_GLES2_OR_OES(glBindFramebuffer);
            LOAD_GLES2_OR_OES(glFramebufferTexture2D);
            //gles_glFramebufferTexture2D(GL_FRAMEBUFFER, tex->binded_attachment, GL_TEXTURE_2D, 0, 0);
            gles_glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gles_glBindFramebuffer(GL_FRAMEBUFFER, glstate->fbo.current_fb->id);
            //gles_glFramebufferTexture2D(GL_FRAMEBUFFER, tex->binded_attachment, GL_TEXTURE_2D, tex->glname, 0);
        }
    }
    // setup fixed pipeline builtin matrix uniform if needed
    if(glprogram->has_builtin_matrix)
    {
        if(glprogram->builtin_matrix[MAT_MVP]!=-1 || glprogram->builtin_matrix[MAT_MVP_I]!=-1
            || glprogram->builtin_matrix[MAT_MVP_T]!=-1 || glprogram->builtin_matrix[MAT_MVP_IT]!=-1)
        {
            GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MVP], 1, GL_FALSE, getMVPMat());
            GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MVP_T], 1, GL_TRUE, getMVPMat());
            if(glprogram->builtin_matrix[MAT_MVP_I]!=-1 || glprogram->builtin_matrix[MAT_MVP_IT]!=-1) {
                GLfloat invmat[16];
                matrix_inverse(getMVPMat(), invmat);
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MVP_I], 1, GL_FALSE, invmat);
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MVP_IT], 1, GL_TRUE, invmat);
            }
        }
        if(glprogram->builtin_matrix[MAT_MV]!=-1 || glprogram->builtin_matrix[MAT_MV_I]!=-1
            || glprogram->builtin_matrix[MAT_MV_T]!=-1 || glprogram->builtin_matrix[MAT_MV_IT]!=-1)
        {
            GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MV], 1, GL_FALSE, getMVMat());
            GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MV_T], 1, GL_TRUE, getMVMat());
            if(glprogram->builtin_matrix[MAT_MV_I]!=-1 || glprogram->builtin_matrix[MAT_MV_IT]!=-1) {
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MV_I], 1, GL_FALSE, getInvMVMat());
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_MV_IT], 1, GL_TRUE, getInvMVMat());
            }
        }
        if(glprogram->builtin_matrix[MAT_P]!=-1 || glprogram->builtin_matrix[MAT_P_I]!=-1
            || glprogram->builtin_matrix[MAT_P_T]!=-1 || glprogram->builtin_matrix[MAT_P_IT]!=-1)
        {
            GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_P], 1, GL_FALSE, getPMat());
            GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_P_T], 1, GL_TRUE, getPMat());
            if(glprogram->builtin_matrix[MAT_P_I]!=-1 || glprogram->builtin_matrix[MAT_P_IT]!=-1) {
                GLfloat invmat[16];
                matrix_inverse(getPMat(), invmat);
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_P_I], 1, GL_FALSE, invmat);
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_P_IT], 1, GL_TRUE, invmat);
            }
        }
        //Normal matrix (mat3 version of transpose(inverse(gl_ModelViewMatrix)))
        if(glprogram->builtin_matrix[MAT_N]!=-1 || glprogram->builtin_normalrescale!=-1)
        {
            if(glprogram->builtin_normalrescale!=-1 && !glstate->fpe_state->rescaling)
            {
                float tmp = 1.0f;
                GoUniformfv(glprogram, glprogram->builtin_normalrescale, 1, 1, &tmp);
            }
            if(glprogram->builtin_matrix[MAT_N]!=-1)
            {
                GoUniformMatrix3fv(glprogram, glprogram->builtin_matrix[MAT_N], 1, GL_FALSE, getNormalMat());
            }
            if((glprogram->builtin_normalrescale!=-1 && glstate->fpe_state->rescaling))
            {
                if(glprogram->builtin_normalrescale!=-1) {
                    const float *invmat = getInvMVMat();
                    float tmp = 1.0f/sqrtf(invmat[3*4+1]*invmat[3*4+1]+invmat[3*4+2]*invmat[3*4+2]+invmat[3*4+3]*invmat[3*4+3]);
                    GoUniformfv(glprogram, glprogram->builtin_normalrescale, 1, 1, &tmp);
                }
            }
        }
        //Texture matrices
        for (int i=0; i<MAX_TEX; i++) {
            if(glprogram->builtin_matrix[MAT_T0+i*4]!=-1 || glprogram->builtin_matrix[MAT_T0_I+i*4]!=-1
                || glprogram->builtin_matrix[MAT_T0_T+i*4]!=-1 || glprogram->builtin_matrix[MAT_T0_IT+i*4]!=-1)
            {
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_T0+i*4], 1, GL_FALSE, getTexMat(i));
                GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_T0_T+i*4], 1, GL_TRUE, getTexMat(i));
                if(glprogram->builtin_matrix[MAT_T0_I+i*4]!=-1 || glprogram->builtin_matrix[MAT_T0_IT+i*4]!=-1) {
                    GLfloat invmat[16];
                    matrix_inverse(getTexMat(i), invmat);
                    GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_T0_I+i*4], 1, GL_FALSE, invmat);
                    GoUniformMatrix4fv(glprogram, glprogram->builtin_matrix[MAT_T0_IT+i*4], 1, GL_TRUE, invmat);
                }
            }
        }
    }
    // set light and material if needed
    if(glprogram->has_builtin_light)
    {
        for (int i=0; i<MAX_LIGHT; i++) {
            if(glprogram->builtin_lights[i].has) {
               GoUniformfv(glprogram, glprogram->builtin_lights[i].ambient, 4, 1, glstate->light.lights[i].ambient);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].diffuse, 4, 1, glstate->light.lights[i].diffuse);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].specular, 4, 1, glstate->light.lights[i].specular);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].position, 4, 1, glstate->light.lights[i].position);
               if(glprogram->builtin_lights[i].halfVector!=-1) { // not sure of this, isn't gl_Vertex need to be used at some point?
                GLfloat tmp[4];
                memcpy(tmp, glstate->light.lights[i].position, 4*sizeof(GLfloat));
                vector4_normalize(tmp);
                if(!glstate->light.local_viewer) {
                    tmp[2]+=1.f;
                    vector4_normalize(tmp);
                }
                GoUniformfv(glprogram, glprogram->builtin_lights[i].halfVector, 1, 1, tmp);
               }
               GoUniformfv(glprogram, glprogram->builtin_lights[i].spotDirection, 3, 1, glstate->light.lights[i].spotDirection);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].spotExponent, 1, 1, &glstate->light.lights[i].spotExponent);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].spotCutoff, 1, 1, &glstate->light.lights[i].spotCutoff);
               if(!memcmp(&glprogram->builtin_lights[i].oldspotCutoff, &glstate->light.lights[i].spotCutoff, sizeof(GLfloat)))
               {
                    memcpy(&glprogram->builtin_lights[i].oldspotCutoff, &glstate->light.lights[i].spotCutoff, sizeof(GLfloat));
                    glprogram->builtin_lights[i].oldspotCosCutoff = cosf(glstate->light.lights[i].spotCutoff*3.1415926535f/180.0f);
               }
               GoUniformfv(glprogram, glprogram->builtin_lights[i].spotCosCutoff, 1, 1, &glprogram->builtin_lights[i].oldspotCosCutoff);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].constantAttenuation, 1, 1, &glstate->light.lights[i].constantAttenuation);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].linearAttenuation, 1, 1, &glstate->light.lights[i].linearAttenuation);
               GoUniformfv(glprogram, glprogram->builtin_lights[i].quadraticAttenuation, 1, 1, &glstate->light.lights[i].quadraticAttenuation);
            }
            if(glprogram->builtin_lightprod[0][i].has) {
                GLfloat tmp[4];
                vector4_mult(glstate->material.front.ambient, glstate->light.lights[i].ambient, tmp); //TODO: Check that
                GoUniformfv(glprogram, glprogram->builtin_lightprod[0][i].ambient, 4, 1, tmp);
                vector4_mult(glstate->material.front.diffuse, glstate->light.lights[i].diffuse, tmp);
                GoUniformfv(glprogram, glprogram->builtin_lightprod[0][i].diffuse, 4, 1, tmp);
                vector4_mult(glstate->material.front.specular, glstate->light.lights[i].specular, tmp);
                GoUniformfv(glprogram, glprogram->builtin_lightprod[0][i].specular, 4, 1, tmp);
            }
            if(glprogram->builtin_lightprod[1][i].has) {
                GLfloat tmp[4];
                vector4_mult(glstate->material.back.ambient, glstate->light.lights[i].ambient, tmp); //TODO: Check that
                GoUniformfv(glprogram, glprogram->builtin_lightprod[1][i].ambient, 4, 1, tmp);
                vector4_mult(glstate->material.back.diffuse, glstate->light.lights[i].diffuse, tmp);
                GoUniformfv(glprogram, glprogram->builtin_lightprod[1][i].diffuse, 4, 1, tmp);
                vector4_mult(glstate->material.back.specular, glstate->light.lights[i].specular, tmp);
                GoUniformfv(glprogram, glprogram->builtin_lightprod[1][i].specular, 4, 1, tmp);
            }
        }
        if(glprogram->builtin_lightmodel.ambient!=-1) {
            GoUniformfv(glprogram, glprogram->builtin_lightmodel.ambient, 4, 1, glstate->light.ambient);
        }
        if(glprogram->builtin_material[0].has) {
            GoUniformfv(glprogram, glprogram->builtin_material[0].emission, 4, 1, glstate->material.front.emission);
            GoUniformfv(glprogram, glprogram->builtin_material[0].ambient, 4, 1, glstate->material.front.ambient);
            GoUniformfv(glprogram, glprogram->builtin_material[0].diffuse, 4, 1, glstate->material.front.diffuse);
            GoUniformfv(glprogram, glprogram->builtin_material[0].specular, 4, 1, glstate->material.front.specular);
            GoUniformfv(glprogram, glprogram->builtin_material[0].shininess, 1, 1, &glstate->material.front.shininess);
            GoUniformfv(glprogram, glprogram->builtin_material[0].alpha, 1, 1, &glstate->material.front.diffuse[3]);
        }
        if(glprogram->builtin_material[1].has) {
            GoUniformfv(glprogram, glprogram->builtin_material[1].emission, 4, 1, glstate->material.back.emission);
            GoUniformfv(glprogram, glprogram->builtin_material[1].ambient, 4, 1, glstate->material.back.ambient);
            GoUniformfv(glprogram, glprogram->builtin_material[1].diffuse, 4, 1, glstate->material.back.diffuse);
            GoUniformfv(glprogram, glprogram->builtin_material[1].specular, 4, 1, glstate->material.back.specular);
            GoUniformfv(glprogram, glprogram->builtin_material[1].shininess, 1, 1, &glstate->material.back.shininess);
            GoUniformfv(glprogram, glprogram->builtin_material[1].alpha, 1, 1, &glstate->material.back.diffuse[3]);
        }
        if(glprogram->builtin_lightmodelprod[0].sceneColor!=-1) {
            GLfloat tmp[4];
            vector4_mult(glstate->material.front.ambient, glstate->light.ambient, tmp);  //TODO: check that
            vector4_add(tmp, glstate->material.front.emission, tmp);
            GoUniformfv(glprogram, glprogram->builtin_lightmodelprod[0].sceneColor, 4, 1, tmp);
        }
        if(glprogram->builtin_lightmodelprod[1].sceneColor!=-1) {
            GLfloat tmp[4];
            vector4_mult(glstate->material.back.ambient, glstate->light.ambient, tmp);  //TODO: check that
            vector4_add(tmp, glstate->material.back.emission, tmp);
            GoUniformfv(glprogram, glprogram->builtin_lightmodelprod[1].sceneColor, 4, 1, tmp);
        }
    }
    // Instance ID
    if(glprogram->builtin_instanceID!=-1)
    {
        GoUniformiv(glprogram, glprogram->builtin_instanceID, 1, 1, &glstate->instanceID);
    }
    // fog parameters
    if(glprogram->builtin_fog.has)
    {
        GoUniformfv(glprogram, glprogram->builtin_fog.color, 4, 1, glstate->fog.color);
        GoUniformfv(glprogram, glprogram->builtin_fog.density, 1, 1, &glstate->fog.density);
        GoUniformfv(glprogram, glprogram->builtin_fog.start, 1, 1, &glstate->fog.start);
        GoUniformfv(glprogram, glprogram->builtin_fog.end, 1, 1, &glstate->fog.end);
        if(glprogram->builtin_fog.scale!=-1) {
            GLfloat tmp = 1.f/(glstate->fog.end - glstate->fog.start);
            GoUniformfv(glprogram, glprogram->builtin_fog.scale, 1, 1, &tmp);
        }
    }
    // clip planes
    if(glprogram->has_builtin_clipplanes)
    {
        for (int i=0; i<hardext.maxplanes; i++) {
            GoUniformfv(glprogram, glprogram->builtin_clipplanes[i], 4, 1, glstate->planes[i]);
        }
    }
    // check point sprite if needed
    if(glprogram->builtin_pointsprite.has)
    {
        GoUniformfv(glprogram, glprogram->builtin_pointsprite.size, 1, 1, &glstate->pointsprite.size);
        GoUniformfv(glprogram, glprogram->builtin_pointsprite.sizeMin, 1, 1, &glstate->pointsprite.sizeMin);
        GoUniformfv(glprogram, glprogram->builtin_pointsprite.sizeMax, 1, 1, &glstate->pointsprite.sizeMax);
        GoUniformfv(glprogram, glprogram->builtin_pointsprite.fadeThresholdSize, 1, 1, &glstate->pointsprite.fadeThresholdSize);
        GoUniformfv(glprogram, glprogram->builtin_pointsprite.distanceConstantAttenuation, 1, 1, glstate->pointsprite.distance+0);
        GoUniformfv(glprogram, glprogram->builtin_pointsprite.distanceLinearAttenuation, 1, 1, glstate->pointsprite.distance+1);
        GoUniformfv(glprogram, glprogram->builtin_pointsprite.distanceQuadraticAttenuation, 1, 1, glstate->pointsprite.distance+2);
    }
    // texenv
    if(glprogram->has_builtin_texenv)
    {
        for (int i=0; i<hardext.maxtex; i++) {
            GoUniformfv(glprogram, glprogram->builtin_texenvcolor[i], 4, 1, glstate->texenv[i].env.color);
            // specific FPE
            GoUniformfv(glprogram, glprogram->builtin_texenvrgbscale[i], 1, 1, &glstate->texenv[i].env.rgb_scale);
            GoUniformfv(glprogram, glprogram->builtin_texenvalphascale[i], 1, 1, &glstate->texenv[i].env.alpha_scale);
        }
    }
    // texgen
    if(glprogram->has_builtin_texgen)
    {
        for (int i=0; i<hardext.maxtex; i++) {
            GoUniformfv(glprogram, glprogram->builtin_eye[0][i], 4, 1, glstate->texgen[i].S_E);
            GoUniformfv(glprogram, glprogram->builtin_eye[1][i], 4, 1, glstate->texgen[i].T_E);
            GoUniformfv(glprogram, glprogram->builtin_eye[2][i], 4, 1, glstate->texgen[i].R_E);
            GoUniformfv(glprogram, glprogram->builtin_eye[3][i], 4, 1, glstate->texgen[i].Q_E);
            GoUniformfv(glprogram, glprogram->builtin_obj[0][i], 4, 1, glstate->texgen[i].S_O);
            GoUniformfv(glprogram, glprogram->builtin_obj[1][i], 4, 1, glstate->texgen[i].T_O);
            GoUniformfv(glprogram, glprogram->builtin_obj[2][i], 4, 1, glstate->texgen[i].R_O);
            GoUniformfv(glprogram, glprogram->builtin_obj[3][i], 4, 1, glstate->texgen[i].Q_O);
        }
    }
    // fpe
    if(glprogram->fpe_alpharef!=-1)
    {
        float alpharef = floorf(glstate->alpharef*255.f);
        GoUniformfv(glprogram, glprogram->fpe_alpharef, 1, 1, &alpharef);
    }
    if(glprogram->has_builtin_texsampler)
    {
        for (int i=0; i<hardext.maxtex; i++)
            GoUniformiv(glprogram, glprogram->builtin_texsampler[i], 1, 1, &i); // very basic stuff here, but sampler needs to be a uniform...
    }
    if(glprogram->has_builtin_texadjust)
    {
        for (int i=0; i<hardext.maxtex; i++) {
            int tt = fpe_gettexture(i);
            gltexture_t* tex = (tt==-1)?NULL:glstate->texture.bound[i][tt];
            if(tex && tex->valid)
                GoUniformfv(glprogram, glprogram->builtin_texadjust[i], 2, 1, tex->adjustxy);
        }
    }
    // oldprograms
    if(glprogram->last_vert && glprogram->last_vert->old) {
        if(glprogram->has_vtx_progenv) {
            for (int i=0; i<MAX_VTX_PROG_ENV_PARAMS; ++i)
                GoUniformfv(glprogram, glprogram->vtx_progenv[i], 4, 1, glstate->glsl->vtx_env_params+i*4);
        }
        if(glprogram->has_vtx_progloc) {
            for (int i=0; i<MAX_VTX_PROG_LOC_PARAMS; ++i)
                GoUniformfv(glprogram, glprogram->vtx_progloc[i], 4, 1, glprogram->last_vert->old->prog_local_params+i*4);
        }
    }
    if(glprogram->last_frag && glprogram->last_frag->old) {
        if(glprogram->has_frg_progenv) {
            for (int i=0; i<MAX_FRG_PROG_ENV_PARAMS; ++i)
                GoUniformfv(glprogram, glprogram->frg_progenv[i], 4, 1, glstate->glsl->frg_env_params+i*4);
        }
        if(glprogram->has_frg_progloc) {
            for (int i=0; i<MAX_FRG_PROG_LOC_PARAMS; ++i)
                GoUniformfv(glprogram, glprogram->frg_progloc[i], 4, 1, glprogram->last_frag->old->prog_local_params+i*4);
        }
        #define GO(A)   \
        if(glprogram->has_samplers ## A) {                                      \
            for (int i=0; i<MAX_TEX; ++i)                                       \
                GoUniformiv(glprogram, glprogram->samplers ## A [i], 1, 1, &i); \
        }
        GO(1d)
        GO(2d)
        GO(3d)
        GO(Cube)
        #undef GO
    }
    // set VertexAttrib if needed
    for(int i=0; i<hardext.maxvattrib; i++) 
    if(glprogram->va_size[i])   // only check used VA...
    {
        vertexattrib_t *v = &glstate->gleshard->vertexattrib[i];
        vertexattrib_t *w = &glstate->vao->vertexattrib[i];
        int enabled = w->enabled;
        int dirty = 0;
        if(enabled && !w->buffer && !w->pointer) {
            DBG(printf("Warning: VA %d Enabled with buffer:0 and NULL pointer, disabling\n", i));
            enabled = 0;
        }
        // enable / disable Array if needed
        if(v->enabled != enabled || (v->enabled && w->divisor)) {
            dirty = 1;
            v->enabled = (w->divisor)?0:enabled;
            DBG(printf("VertexAttribArray[%d]:%s, divisor=%d\n", i, (enabled)?"Enable":"Disable", w->divisor);)
            if(v->enabled)
                gles_glEnableVertexAttribArray(i);
            else
                gles_glDisableVertexAttribArray(i);
        }
        // check if new value has to be sent to hardware
        if(v->enabled) {
            // array case
            void * ptr = (void*)((uintptr_t)w->pointer + ((w->buffer)?(uintptr_t)w->buffer->data:0));
            if(dirty || v->size!=w->size || v->type!=w->type || v->normalized!=w->normalized 
                || v->stride!=w->stride || v->buffer!=w->buffer || (w->real_buffer==0 && v->pointer!=ptr)
                || v->real_buffer!=w->real_buffer || (w->real_buffer!=0 && v->real_pointer != w->real_pointer) 
                || w->real_buffer!=glstate->bind_buffer.array) {
                if((w->size==GL_BGRA || w->type==GL_DOUBLE) && scratch->size<8) { 
                    // need to adjust, so first need the min/max (a shame as I already must have that somewhere)
                    int imin, imax;
                    if(type==0) {
                        imin = first; imax = count;
                    } else {
                        if(type==GL_UNSIGNED_INT)
                            getminmax_indices_ui(indices, &imax, &imin, count);
                        else
                            getminmax_indices_us(indices, &imax, &imin, count);
                        ++imax;
                    }
                    if(w->size==GL_BGRA) {
                        v->size = 4;
                        v->type = GL_FLOAT;
                        v->normalized = 0;
                        v->integer = 0;
                        v->pointer = scratch->scratch[scratch->size++] = copy_gl_pointer_color_bgra(ptr, w->stride, 4, imin, imax);
                        v->pointer = (char*)v->pointer - imin*4*sizeof(GLfloat);   // adjust for min...
                        v->stride = 0;
                        v->buffer = NULL;
                        v->real_buffer = 0;
                    } else if (w->type == GL_DOUBLE) {
                        // TODO
                        static int warn = 1;
                        if(warn) {
                            printf("LIBGL: VertexAttribArray using GL_DOUBLE unimplemented!\n");
                            warn=0;
                        }
                    }
                } else {
                    v->size = w->size;
                    v->type = w->type;
                    v->normalized = w->normalized;
                    v->integer = w->integer;
                    v->stride = w->stride;
                    v->real_buffer = w->real_buffer;
                    v->real_pointer = w->real_pointer;
                    v->pointer = (v->real_buffer)?v->real_pointer:ptr;
                    v->buffer = w->buffer; // buffer is unused here
                }
                DBG(printf("using Buffer %d\n", v->real_buffer);)
                bindBuffer(GL_ARRAY_BUFFER, v->real_buffer);

                if (v->integer) {
                    if(gles_glVertexAttribIPointer)
                        gles_glVertexAttribIPointer(i, v->size, v->type, v->stride, v->pointer);
                    else
                        gles_glVertexAttribPointer(i, v->size, v->type, 0, v->stride, v->pointer);
                    DBG(printf("glVertexAttribIPointer(%d, %d, %s, %d, %p)\n", i, v->size, PrintEnum(v->type), v->stride, v->pointer);)
                }
                else {
                    gles_glVertexAttribPointer(i, v->size, v->type, v->normalized, v->stride, v->pointer);
                    DBG(printf("glVertexAttribPointer(%d, %d, %s, %d, %d, %p)\n", i, v->size, PrintEnum(v->type), v->normalized, v->stride, v->pointer);)
                }
            }
        } else {
            // single value case
            char* current = (char*)glstate->vavalue[i];
            GLfloat tmp[4] = {0.0f, 0.0f, 0.0f, 1.0f};
            if(w->divisor && w->enabled) {
                current = (char*)((uintptr_t)w->pointer + ((w->buffer)?(uintptr_t)w->buffer->data:0));
                int stride=w->stride;
                if(!stride) stride=gl_sizeof(w->type)*w->size;
                current += (glstate->instanceID/w->divisor) * stride;
                if(w->type==GL_FLOAT) {
                    if(w->size!=4) {
                        memcpy(tmp, current, sizeof(GLfloat)*w->size);
                        current = (char*)tmp;
                    }
                } else {
                    if(w->type == GL_DOUBLE || !w->normalized) {
                        for(int k=0; k<w->size; ++k) {
                            GL_TYPE_SWITCH(input, current, w->type,
                                tmp[k] = input[k];
                            ,)
                        }
                    } else {
                        for(int k=0; k<w->size; ++k) {
                            GL_TYPE_SWITCH_MAX(input, current, w->type,
                                tmp[k] = (float)input[k]/(float)maxv;
                            ,)
                        }
                    }
                    current = (char*)tmp;
                }
            }
            if(dirty || memcmp(glstate->gleshard->vavalue[i], current, 4*sizeof(GLfloat))) {
                memcpy(glstate->gleshard->vavalue[i], current, 4*sizeof(GLfloat));
                gles_glVertexAttrib4fv(i, glstate->gleshard->vavalue[i]);
                DBG(printf("glVertexAttrib4fv(%d, %p) => (%f, %f, %f, %f)\n", i, glstate->gleshard->vavalue[i], glstate->gleshard->vavalue[i][0], glstate->gleshard->vavalue[i][1], glstate->gleshard->vavalue[i][2], glstate->gleshard->vavalue[i][3]);)
            }
        }
    } else {
        // disable VAArray, to be on the safe side
        vertexattrib_t *v = &glstate->gleshard->vertexattrib[i];
        if(v->enabled) {
            v->enabled = 0;
            DBG(printf("VertexAttribArray[%d]:%s\n", i, "Disable");)
            gles_glDisableVertexAttribArray(i);
        }
    }
}

void realize_blitenv(int alpha) {
    DBG(printf("realize_blitenv(%d)\n", alpha);)
    LOAD_GLES2(glUseProgram);
    if(glstate->gleshard->program != ((alpha)?glstate->blit->program_alpha:glstate->blit->program)) {
        glstate->gleshard->program = ((alpha)?glstate->blit->program_alpha:glstate->blit->program);
        gles_glUseProgram(glstate->gleshard->program);
    }
    // set VertexAttrib if needed
    unboundBuffers();
    for(int i=0; i<hardext.maxvattrib; i++) {
        vertexattrib_t *v = &glstate->gleshard->vertexattrib[i];
        // enable / disable Array if needed
        if(v->enabled != ((i<2)?1:0)) {
            LOAD_GLES2(glEnableVertexAttribArray)
            LOAD_GLES2(glDisableVertexAttribArray);
            v->enabled = ((i<2)?1:0);
            if(v->enabled)
                gles_glEnableVertexAttribArray(i);
            else
                gles_glDisableVertexAttribArray(i);
        }
        // check if new value has to be sent to hardware
        if(i<2) {
            // array case
            if(v->size!=2 || v->type!=GL_FLOAT || v->normalized!=0 
                || v->stride!=0 || v->pointer!=((i==0)?glstate->blit->vert:glstate->blit->tex) 
                || v->buffer!=0) {
                v->size = 2;
                v->type = GL_FLOAT;
                v->normalized = 0;
                v->stride = 0;
                v->pointer = ((i==0)?glstate->blit->vert:glstate->blit->tex);
                v->buffer = 0;
                v->real_buffer = 0;
                LOAD_GLES2(glVertexAttribPointer);
                gles_glVertexAttribPointer(i, v->size, v->type, v->normalized, v->stride, v->pointer);
            }
        }
    }
}

// ********* Builtin GL Uniform, VertexAttrib and co *********

void builtin_Init(program_t *glprogram) {
    // initialise emulated builtin matrix uniform to -1
    for (int i=0; i<MAT_MAX; i++)
        glprogram->builtin_matrix[i] = -1;
    for (int i=0; i<MAX_LIGHT; i++) {
        glprogram->builtin_lights[i].ambient = -1;
        glprogram->builtin_lights[i].diffuse = -1;
        glprogram->builtin_lights[i].specular = -1;
        glprogram->builtin_lights[i].position = -1;
        glprogram->builtin_lights[i].halfVector = -1;
        glprogram->builtin_lights[i].spotDirection = -1;
        glprogram->builtin_lights[i].spotExponent = -1;
        glprogram->builtin_lights[i].spotCutoff = -1;
        glprogram->builtin_lights[i].spotCosCutoff = -1;
        glprogram->builtin_lights[i].constantAttenuation = -1;
        glprogram->builtin_lights[i].linearAttenuation = -1;
        glprogram->builtin_lights[i].quadraticAttenuation = -1;
    }
    glprogram->builtin_lightmodel.ambient = -1;
    for (int i=0; i<2; i++) { // 0:Front, 1:Back
        glprogram->builtin_material[i].emission = -1;
        glprogram->builtin_material[i].ambient = -1;
        glprogram->builtin_material[i].diffuse = -1;
        glprogram->builtin_material[i].specular = -1;
        glprogram->builtin_material[i].shininess = -1;
        glprogram->builtin_material[i].alpha = -1;
        
        glprogram->builtin_lightmodelprod[i].sceneColor = -1;

        for (int j=0; j<MAX_LIGHT; j++) {
            glprogram->builtin_lightprod[i][j].ambient = -1;
            glprogram->builtin_lightprod[i][j].diffuse = -1;
            glprogram->builtin_lightprod[i][j].specular = -1;
        }
    }
    glprogram->builtin_normalrescale = -1;
    glprogram->builtin_instanceID = -1;
    for (int i=0; i<MAX_CLIP_PLANES; i++)
        glprogram->builtin_clipplanes[i] = -1;
    glprogram->builtin_pointsprite.size = -1;
    glprogram->builtin_pointsprite.sizeMin = -1;
    glprogram->builtin_pointsprite.sizeMax = -1;
    glprogram->builtin_pointsprite.fadeThresholdSize = -1;
    glprogram->builtin_pointsprite.distanceConstantAttenuation = -1;
    glprogram->builtin_pointsprite.distanceLinearAttenuation = -1;
    glprogram->builtin_pointsprite.distanceQuadraticAttenuation = -1;
    for (int i=0; i<MAX_TEX; i++) {
        glprogram->builtin_texenvcolor[i] = -1;
        glprogram->builtin_texenvrgbscale[i] = -1;
        glprogram->builtin_texenvalphascale[i] = -1;
        for (int j=0; j<4; j++) {
            glprogram->builtin_eye[j][i] = -1;
            glprogram->builtin_obj[j][i] = -1;
        }
        glprogram->builtin_texsampler[i] = -1;
        glprogram->builtin_texadjust[i] = -1;
    }
    glprogram->builtin_fog.color = -1;
    glprogram->builtin_fog.density = -1;
    glprogram->builtin_fog.start = -1;
    glprogram->builtin_fog.end = -1;
    glprogram->builtin_fog.scale = -1;
    glprogram->builtin_blendcolor = -1;
    // fpe uniform
    glprogram->fpe_alpharef = -1;
    // initialise emulated builtin attrib to -1
    for (int i=0; i<ATT_MAX; i++)
        glprogram->builtin_attrib[i] = -1;
    // oldprograms
    for (int i=0; i<MAX_VTX_PROG_ENV_PARAMS; ++i)
        glprogram->vtx_progenv[i] = -1;
    for (int i=0; i<MAX_VTX_PROG_LOC_PARAMS; ++i)
        glprogram->vtx_progloc[i] = -1;
    for (int i=0; i<MAX_FRG_PROG_ENV_PARAMS; ++i)
        glprogram->frg_progenv[i] = -1;
    for (int i=0; i<MAX_FRG_PROG_LOC_PARAMS; ++i)
        glprogram->frg_progloc[i] = -1;
    for (int i=0; i<MAX_TEX; ++i)
        glprogram->samplers1d[i] = glprogram->samplers2d[i] = glprogram->samplers3d[i] = glprogram->samplersCube[i] = -1;
}

const char* gl4es_code = "_gl4es_";
const char* lightsource_code = "_gl4es_LightSource[";
const char* lightsource_fpe_code = "_gl4es_LightSource_";
const char* lightmodel_code = "_gl4es_LightModel.";
const char* frontmaterial_code = "_gl4es_FrontMaterial";
const char* backmaterial_code = "_gl4es_BackMaterial";
const char* frontmaterial_fpe_code = "_gl4es_FrontMaterial_shininess";
const char* backmaterial_fpe_code = "_gl4es_BackMaterial_shininess";
const char* frontlightmodelprod_code = "_gl4es_FrontLightModelProduct";
const char* backlightmodelprod_code = "_gl4es_BackLightModelProduct";
const char* frontlightprod_code = "_gl4es_FrontLightProduct[";
const char* backlightprod_code = "_gl4es_BackLightProduct[";
const char* frontlightprod_fpe_code = "_gl4es_FrontLightProduct_";
const char* backlightprod_fpe_code = "_gl4es_BackLightProduct_";
const char* normalrescale_code = "_gl4es_NormalScale";
const char* instanceID_code = "_gl4es_InstanceID";
const char* clipplanes_code = "_gl4es_ClipPlane[";
const char* clipplanes_fpe_code = "_gl4es_ClipPlane_";
const char* point_code = "_gl4es_Point";
const char* texenvcolor_code = "_gl4es_TextureEnvColor[";
const char* texenvcolor_fpe_code = "_gl4es_TextureEnvColor_";
const char* texenvcolor_noa_code = "_gl4es_TextureEnvColor";
const char* texgeneyestart_code = "_gl4es_EyePlane";
const char* texgeneye_code = "_gl4es_EyePlane%c[";
const char* texgeneye_fpe_code = "_gl4es_EyePlane%c_";
const char* texgeneye_noa_code = "_gl4es_EyePlane%c";
const char* texgenobjstart_code = "_gl4es_ObjectPlane";
const char* texgenobj_code = "_gl4es_ObjectPlane%c[";
const char* texgenobj_fpe_code = "_gl4es_ObjectPlane%c_";
const char* texgenobj_noa_code = "_gl4es_ObjectPlane%c";
const char texgenCoords[4] = {'S', 'T', 'R', 'Q'};
const char* alpharef_code = "_gl4es_AlphaRef";
const char* fpetexSampler_code = "_gl4es_TexSampler_";
const char* fpetexenvRGBScale_code = "_gl4es_TexEnvRGBScale_";
const char* fpetexenvAlphaScale_code = "_gl4es_TexEnvAlphaScale_";
const char* fpetexAdjust_code = "_gl4es_TexAdjust_";
const char* fog_code = "_gl4es_Fog.";
const char* vtx_progenv_noa = "_gl4es_Vertex_ProgramEnv_";
const char* vtx_progenv_arr = "_gl4es_Vertex_ProgramEnv[";
const char* vtx_progloc_noa = "_gl4es_Vertex_ProgramLocal_";
const char* vtx_progloc_arr = "_gl4es_Vertex_ProgramLocal[";
const char* frg_progenv_noa = "_gl4es_Fragment_ProgramEnv_";
const char* frg_progenv_arr = "_gl4es_Fragment_ProgramEnv[";
const char* frg_progloc_noa = "_gl4es_Fragment_ProgramLocal_";
const char* frg_progloc_arr = "_gl4es_Fragment_ProgramLocal[";
const char* samplers1d_noa = "_gl4es_Sampler1D_";
const char* samplers2d_noa = "_gl4es_Sampler2D_";
const char* samplers3d_noa = "_gl4es_Sampler3D_";
const char* samplersCube_noa = "_gl4es_SamplerCube_";
const char* blend_color_code = "_gl4es_BlendColor";
int builtin_CheckUniform(program_t *glprogram, char* name, GLint id, int size) {
    if(strncmp(name, gl4es_code, strlen(gl4es_code)))
        return 0;   // doesn't start with "_gl4es_", no need to look further
    int builtin = isBuiltinMatrix(name);
    // check matrices
    if(builtin!=-1) {
        glprogram->builtin_matrix[builtin] = id;
        glprogram->has_builtin_matrix = 1;
        return 1;
    }
    // lightsource
    if(strncmp(name, lightsource_code, strlen(lightsource_code))==0 || strncmp(name, lightsource_fpe_code, strlen(lightsource_fpe_code))==0) {
        // it's a light! grab it's number - also, fpe or not fpe is the same length. The fpe version avoids the array...
        int n = name[strlen(lightsource_code)]-'0';   // only 8 light, so this works
        if(n>=0 && n<hardext.maxlights) {
            if(strstr(name, ".ambient")) glprogram->builtin_lights[n].ambient = id;
            else if(strstr(name, ".diffuse")) glprogram->builtin_lights[n].diffuse = id;
            else if(strstr(name, ".specular")) glprogram->builtin_lights[n].specular = id;
            else if(strstr(name, ".position")) glprogram->builtin_lights[n].position = id;
            else if(strstr(name, ".halfVector")) glprogram->builtin_lights[n].halfVector = id;
            else if(strstr(name, ".spotDirection")) glprogram->builtin_lights[n].spotDirection = id;
            else if(strstr(name, ".spotExponent")) glprogram->builtin_lights[n].spotExponent = id;
            else if(strstr(name, ".spotCutoff")) glprogram->builtin_lights[n].spotCutoff = id;
            else if(strstr(name, ".spotCosCutoff")) glprogram->builtin_lights[n].spotCosCutoff = id;
            else if(strstr(name, ".constantAttenuation")) glprogram->builtin_lights[n].constantAttenuation = id;
            else if(strstr(name, ".linearAttenuation")) glprogram->builtin_lights[n].linearAttenuation = id;
            else if(strstr(name, ".quadraticAttenuation")) glprogram->builtin_lights[n].quadraticAttenuation = id;
            glprogram->has_builtin_light = 1;
            glprogram->builtin_lights[n].has = 1;
            return 1;
        }
    }
    if(strncmp(name, lightmodel_code, strlen(lightmodel_code))==0)
    {
        // it's a Light Model
        if(strstr(name, "ambient")) glprogram->builtin_lightmodel.ambient = id;
        glprogram->has_builtin_light = 1;
        return 1;
    }
    if(strncmp(name, frontmaterial_code, strlen(frontmaterial_code))==0 
        || strncmp(name, backmaterial_code, strlen(backmaterial_code))==0)
    {
        // it's a material
        int n=(strncmp(name, frontmaterial_code, strlen(frontmaterial_code))==0)?0:1;
        if(strstr(name, ".emission")) glprogram->builtin_material[n].emission = id;
        else if(strstr(name, ".ambient")) glprogram->builtin_material[n].ambient = id;
        else if(strstr(name, ".diffuse")) glprogram->builtin_material[n].diffuse = id;
        else if(strstr(name, ".specular")) glprogram->builtin_material[n].specular = id;
        else if(strstr(name, ".shininess")) glprogram->builtin_material[n].shininess = id;
        else if(strstr(name, "_shininess")) glprogram->builtin_material[n].shininess = id;
        else if(strstr(name, "_alpha")) glprogram->builtin_material[n].alpha = id;
        glprogram->has_builtin_light = 1;
        glprogram->builtin_material[n].has = 1;
        return 1;
    }
    if(strncmp(name, frontlightmodelprod_code, strlen(frontlightmodelprod_code))==0 
    || strncmp(name, backlightmodelprod_code, strlen(backlightmodelprod_code))==0)
    {
        // it's a front light model product
        int n=(strncmp(name, frontlightmodelprod_code, strlen(frontlightmodelprod_code))==0)?0:1;
        if(strstr(name, ".sceneColor")) glprogram->builtin_lightmodelprod[n].sceneColor = id;
        glprogram->has_builtin_light = 1;
        return 1;
    }
    if(strncmp(name, frontlightprod_code, strlen(frontlightprod_code))==0 
    || strncmp(name, backlightprod_code, strlen(backlightprod_code))==0
    || strncmp(name, frontlightprod_fpe_code, strlen(frontlightprod_fpe_code))==0 
    || strncmp(name, backlightprod_fpe_code, strlen(backlightprod_fpe_code))==0
    )
    {
        // it's a material
        int i=(strncmp(name, frontlightprod_code, strlen(frontlightprod_code))==0 || strncmp(name, frontlightprod_fpe_code, strlen(frontlightprod_fpe_code))==0)?0:1;
        int n = name[strlen(i?backlightprod_code:frontlightprod_code)]-'0';   // only 8 light, so this works
        if(n>=0 && n<hardext.maxlights) {
            if(strstr(name, ".ambient")) glprogram->builtin_lightprod[i][n].ambient = id;
            else if(strstr(name, ".diffuse")) glprogram->builtin_lightprod[i][n].diffuse = id;
            else if(strstr(name, ".specular")) glprogram->builtin_lightprod[i][n].specular = id;
            glprogram->has_builtin_light = 1;
            glprogram->builtin_lightprod[i][n].has = 1;
            return 1;
        }
    }
    if(strncmp(name, normalrescale_code, strlen(normalrescale_code))==0)
    {
        glprogram->builtin_normalrescale = id;
        glprogram->has_builtin_matrix = 1;  // this is in the matrix block
        return 1;
    }
    if(strncmp(name, instanceID_code, strlen(instanceID_code))==0)
    {
        glprogram->builtin_instanceID = id;
        return 1;
    }
    if(strncmp(name, clipplanes_code, strlen(clipplanes_code))==0) {
        // it a clip plane! grab it's number
        int n = name[strlen(clipplanes_code)]-'0';   // only 6 clip planes, so this works
        if(n>=0 && n<hardext.maxplanes) {
            glprogram->builtin_clipplanes[n] = id;
            glprogram->has_builtin_clipplanes = 1;
            return 1;
        }
    }
    if(strncmp(name, clipplanes_fpe_code, strlen(clipplanes_fpe_code))==0) {
        // it an fpe clip plane! grab it's number
        int n = name[strlen(clipplanes_fpe_code)]-'0';   // only 6 clip planes, so this works
        if(n>=0 && n<hardext.maxplanes) {
            glprogram->builtin_clipplanes[n] = id;
            glprogram->has_builtin_clipplanes = 1;
            return 1;
        }
    }
    if(strncmp(name, fog_code, strlen(fog_code))==0)
    {
        // it's a Fog parameter
        if(strstr(name, "color")) glprogram->builtin_fog.color = id;
        else if(strstr(name, "density")) glprogram->builtin_fog.density = id;
        else if(strstr(name, "start")) glprogram->builtin_fog.start = id;
        else if(strstr(name, "end")) glprogram->builtin_fog.end = id;
        else if(strstr(name, "scale")) glprogram->builtin_fog.scale = id;
        glprogram->builtin_fog.has = 1;
        return 1;
    }
    if(strncmp(name, point_code, strlen(point_code))==0)
    {
        // it's a Point parameter
        if(strstr(name, ".sizeMin")) glprogram->builtin_pointsprite.sizeMin = id;
        else if(strstr(name, ".sizeMax")) glprogram->builtin_pointsprite.sizeMax = id;
        else if(strstr(name, ".size")) glprogram->builtin_pointsprite.size = id;
        else if(strstr(name, ".fadeThresholdSize")) glprogram->builtin_pointsprite.fadeThresholdSize = id;
        else if(strstr(name, ".distanceConstantAttenuation")) glprogram->builtin_pointsprite.distanceConstantAttenuation = id;
        else if(strstr(name, ".distanceLinearAttenuation")) glprogram->builtin_pointsprite.distanceLinearAttenuation = id;
        else if(strstr(name, ".distanceQuadraticAttenuation")) glprogram->builtin_pointsprite.distanceQuadraticAttenuation = id;
        glprogram->builtin_pointsprite.has = 1;
        return 1;
    }
    if(strncmp(name, texenvcolor_code, strlen(texenvcolor_code))==0) {
        // it a TexEnvColor! grab it's number
        int l = strlen(texenvcolor_code);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        if(n>=0 && n<hardext.maxtex) {
            glprogram->builtin_texenvcolor[n] = id;
            glprogram->has_builtin_texenv = 1;
            return 1;
        }
    }
    /*if(strcmp(name, texenvcolor_noa_code)==0) {
        // it a TexEnvColor, without the array, so full size
        for (int n=0; n<size; n++)
            glprogram->builtin_texenvcolor[n] = id;
        glprogram->has_builtin_texenv = 1;
        return 1;
    }*/
    if(strncmp(name, texgeneyestart_code, strlen(texgeneyestart_code))==0) {
        for (int i=0; i<4; i++) {
            char tmp[100];
            sprintf(tmp, texgeneye_code, texgenCoords[i]);
            if(strncmp(name, tmp, strlen(tmp))==0) {
                // it a TexGen Eye Plane! grab it's number
                int l = strlen(tmp);
                int n = name[l]-'0';
                if(name[l+1]>='0' && name[l+1]<='9')
                    n = n*10 + name[l+1]-'0';
                if(n>=0 && n<hardext.maxtex) {
                    glprogram->builtin_eye[i][n] = id;
                    glprogram->has_builtin_texgen = 1;
                    return 1;
                }
            }
            sprintf(tmp, texgeneye_fpe_code, texgenCoords[i]);
            if(strncmp(name, tmp, strlen(tmp))==0) {
                // it a TexGen Eye Plane! grab it's number
                int l = strlen(tmp);
                int n = name[l]-'0';
                if(name[l+1]>='0' && name[l+1]<='9')
                    n = n*10 + name[l+1]-'0';
                if(n>=0 && n<hardext.maxtex) {
                    glprogram->builtin_eye[i][n] = id;
                    glprogram->has_builtin_texgen = 1;
                    return 1;
                }
            }
            /*sprintf(tmp, texgeneye_noa_code, texgenCoords[i]);
            if(strcmp(name, tmp)==0) {
                // it a TexGen Eye Plane without the array
                for (int n=0; n<size; n++)
                    glprogram->builtin_eye[i][n] = id;
                glprogram->has_builtin_texgen = 1;
                return 1;
            }*/
        }
    }
    if(strncmp(name, texgenobjstart_code, strlen(texgenobjstart_code))==0) {
        for (int i=0; i<4; i++) {
            char tmp[100];
            sprintf(tmp, texgenobj_code, texgenCoords[i]);
            if(strncmp(name, tmp, strlen(tmp))==0) {
                // it a TexGen Object Plane! grab it's number
                int l = strlen(tmp);
                int n = name[l]-'0';
                if(name[l+1]>='0' && name[l+1]<='9')
                    n = n*10 + name[l+1]-'0';
                if(n>=0 && n<hardext.maxtex) {
                    glprogram->builtin_obj[i][n] = id;
                    glprogram->has_builtin_texgen = 1;
                    return 1;
                }
            }
            sprintf(tmp, texgenobj_fpe_code, texgenCoords[i]);
            if(strncmp(name, tmp, strlen(tmp))==0) {
                // it a TexGen Object Plane! grab it's number
                int l = strlen(tmp);
                int n = name[l]-'0';
                if(name[l+1]>='0' && name[l+1]<='9')
                    n = n*10 + name[l+1]-'0';
                if(n>=0 && n<hardext.maxtex) {
                    glprogram->builtin_obj[i][n] = id;
                    glprogram->has_builtin_texgen = 1;
                    return 1;
                }
            }
            /*sprintf(tmp, texgenobj_noa_code, texgenCoords[i]);
            if(strcmp(name, tmp)==0) {
                // it a TexGen Object Plane without the array
                for (int n=0; n<size; n++)
                    glprogram->builtin_obj[i][n] = id;
                glprogram->has_builtin_texgen = 1;
                return 1;
            }*/
        }
    }
    // fpe specials
    // alpha ref
    if(strcmp(name, alpharef_code)==0) {
        glprogram->fpe_alpharef = id;
        glprogram->has_fpe = 1;
        return 1;
    }
    // texture sampler
    if(strncmp(name, fpetexSampler_code, strlen(fpetexSampler_code))==0) {
        // it a Texture Sampler! grab it's number
        int l = strlen(fpetexSampler_code);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        if(n>=0 && n<hardext.maxtex) {
            glprogram->builtin_texsampler[n] = id;
            glprogram->has_builtin_texsampler = 1;
            return 1;
        }
    }
    // texture env color
    if(strncmp(name, texenvcolor_fpe_code, strlen(texenvcolor_fpe_code))==0) {
        // it a Texture env color
        int l = strlen(texenvcolor_fpe_code);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        if(n>=0 && n<hardext.maxtex) {
            glprogram->builtin_texenvcolor[n] = id;
            glprogram->has_builtin_texenv = 1;
            return 1;
        }
    }
    // texture env rgb/alpha scale
    if(strncmp(name, fpetexenvRGBScale_code, strlen(fpetexenvRGBScale_code))==0) {
        // it a Texture env color
        int l = strlen(fpetexenvRGBScale_code);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        if(n>=0 && n<hardext.maxtex) {
            glprogram->builtin_texenvrgbscale[n] = id;
            glprogram->has_builtin_texenv = 1;
            return 1;
        }
    }
    if(strncmp(name, fpetexenvAlphaScale_code, strlen(fpetexenvAlphaScale_code))==0) {
        // it a Texture env color
        int l = strlen(fpetexenvAlphaScale_code);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        if(n>=0 && n<hardext.maxtex) {
            glprogram->builtin_texenvalphascale[n] = id;
            glprogram->has_builtin_texenv = 1;
            return 1;
        }
    }
    if(strncmp(name, fpetexAdjust_code, strlen(fpetexAdjust_code))==0) {
        // it a Texture env color
        int l = strlen(fpetexAdjust_code);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        if(n>=0 && n<hardext.maxtex) {
            glprogram->builtin_texadjust[n] = id;
            glprogram->has_builtin_texadjust = 1;
            return 1;
        }
    }
    // blend color
    if(strncmp(name, blend_color_code, strlen(blend_color_code))==0) {
        glprogram->builtin_blendcolor = id;
        glprogram->has_builtin_blendcolor = 1;
        return 1;
    }
    // oldprogram
    if(strncmp(name, vtx_progenv_arr, strlen(vtx_progenv_arr))==0) {
        int l = strlen(vtx_progenv_arr);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_vtx_progenv = 1;
        for (int i=0; i<n; ++i)
            glprogram->vtx_progenv[i] = id+i;
        return 1;
    }
    if(strncmp(name, vtx_progenv_noa, strlen(vtx_progenv_noa))==0) {
        int l = strlen(vtx_progenv_noa);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_vtx_progenv = 1;
        glprogram->vtx_progenv[n] = id;
        return 1;
    }
    if(strncmp(name, vtx_progloc_arr, strlen(vtx_progloc_arr))==0) {
        int l = strlen(vtx_progloc_arr);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_vtx_progloc = 1;
        for (int i=0; i<n; ++i)
            glprogram->vtx_progloc[i] = id+i;
        return 1;
    }
    if(strncmp(name, vtx_progloc_noa, strlen(vtx_progloc_noa))==0) {
        int l = strlen(vtx_progloc_noa);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_vtx_progloc = 1;
        glprogram->vtx_progloc[n] = id;
        return 1;
    }
    if(strncmp(name, frg_progenv_arr, strlen(frg_progenv_arr))==0) {
        int l = strlen(frg_progenv_arr);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_frg_progenv = 1;
        for (int i=0; i<n; ++i)
            glprogram->frg_progenv[i] = id+i;
        return 1;
    }
    if(strncmp(name, frg_progenv_noa, strlen(frg_progenv_noa))==0) {
        int l = strlen(frg_progenv_noa);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_frg_progenv = 1;
        glprogram->frg_progenv[n] = id;
        return 1;
    }
    if(strncmp(name, frg_progloc_arr, strlen(frg_progloc_arr))==0) {
        int l = strlen(frg_progloc_arr);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_frg_progloc = 1;
        for (int i=0; i<n; ++i)
            glprogram->frg_progloc[i] = id+i;
        return 1;
    }
    if(strncmp(name, frg_progloc_noa, strlen(frg_progloc_noa))==0) {
        int l = strlen(frg_progloc_noa);
        int n = name[l]-'0';
        if(name[l+1]>='0' && name[l+1]<='9')
            n = n*10 + name[l+1]-'0';
        glprogram->has_frg_progloc = 1;
        glprogram->frg_progloc[n] = id;
        return 1;
    }
    #define GO(A)   \
    if(strncmp(name, samplers ## A ## _noa, strlen(samplers ## A ## _noa))==0) {    \
        int l = strlen(samplers ## A ## _noa);                                      \
        int n = name[l]-'0';                                                        \
        if(name[l+1]>='0' && name[l+1]<='9')                                        \
            n = n*10 + name[l+1]-'0';                                               \
        glprogram->has_samplers ## A = 1;                                           \
        glprogram->samplers ## A [n] = id;                                          \
        return 1;                                                                   \
    }
    GO(1d)
    GO(2d)
    GO(3d)
    GO(Cube)
    #undef GO

    return 0;
}

int builtin_CheckVertexAttrib(program_t *glprogram, char* name, GLint id) {
    if(strncmp(name, gl4es_code, strlen(gl4es_code)))
        return 0;   // doesn't start with "_gl4es_", no need to look further
    int builtin = isBuiltinAttrib(name);
    if(builtin!=-1) {
        glprogram->builtin_attrib[builtin] = id;
        glprogram->has_builtin_attrib = 1;
        return 1;
    }
    return 0;
}
