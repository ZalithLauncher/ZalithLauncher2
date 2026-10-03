#include "glstate.h"

#include "../glx/hardext.h"
#include "fpe.h"
#include "framebuffers.h"
#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "loader.h"
#include "oldprogram.h"

glstate_t *glstate = NULL;

glstate_t default_glstate = {0};

#define DEFAULT_STATE (void*)(~(uintptr_t)0)

void init_matrix(glstate_t* glstate);

static void free_renderbuffer(glrenderbuffer_t *rend)
{
    LOAD_GLES2_OR_OES(glDeleteRenderbuffers);
    if(!rend || !gles_glDeleteRenderbuffers)
        return;
    if(rend->secondarybuffer)
        gles_glDeleteRenderbuffers(1, &rend->secondarybuffer);
    if(rend->renderbuffer)
        gles_glDeleteRenderbuffers(1, &rend->renderbuffer);
    // the texture will be free by the free of the texture list, as it's referenced there...
    free(rend);
}

static void free_framebuffer(glframebuffer_t *fb)
{
    LOAD_GLES2_OR_OES(glDeleteFramebuffers);
    if(!fb || !gles_glDeleteFramebuffers)
        return;
    if(fb->id)
        gles_glDeleteFramebuffers(1, &fb->id);
    // the texture will be free by the free of the texture list, as it's referenced there...
    free(fb);
}

static void free_texture(gltexture_t *tex)
{
    LOAD_GLES(glDeleteTextures);
    if(!tex || !gles_glDeleteTextures)
        return;
    if(tex->glname)
        gles_glDeleteTextures(1, &tex->glname);
    if(tex->data)
        free(tex->data);
    // renderbuffer linked to this texture will be freed by the free_renderbuffer function.
    free(tex);
}

void CopyGLEShard(void* dst, const void* src)
{
    if(!dst || !src)
        return;
    memcpy(((glstate_t*)dst)->gleshard, ((const glstate_t*)src)->gleshard, sizeof(gleshard_t));
}

void* NewGLState(void* shared_glstate, int es2only) {
    glstate_t *glstate = (shared_glstate!=DEFAULT_STATE)?((glstate_t*)calloc(1, sizeof(glstate_t))):&default_glstate;
#if defined(AMIGAOS4) || defined(__EMSCRIPTEN__)
    int def = 0;
    if(shared_glstate==DEFAULT_STATE)
        def = 1;
#endif
    if(shared_glstate==DEFAULT_STATE)
        shared_glstate=NULL;
    if(shared_glstate) {
        glstate_t* copy_state = (glstate_t*)shared_glstate;
        if(!copy_state->shared_cnt) {
            copy_state->shared_cnt = (int*)malloc(sizeof(int));
            (*copy_state->shared_cnt) = 2;
        } else
            (*copy_state->shared_cnt)++;
        glstate->shared_cnt = copy_state->shared_cnt;
        glstate->headlists = copy_state->headlists;
        glstate->actual_tex2d = copy_state->actual_tex2d;
        glstate->texture.list = copy_state->texture.list;
        glstate->glsl = copy_state->glsl;
        //glstate->gleshard = copy_state->gleshard; // Not shared (at least not the VA)
        glstate->buffers = copy_state->buffers;
        glstate->fpe_cache = copy_state->fpe_cache;
        glstate->fbo.renderbufferlist = copy_state->fbo.renderbufferlist;
        glstate->fbo.default_rb = copy_state->fbo.default_rb;
        glstate->fbo.framebufferlist = copy_state->fbo.framebufferlist;
        glstate->fbo.fbo_0 = copy_state->fbo.fbo_0;
        glstate->fbo.old = copy_state->fbo.old;
        glstate->samplers.samplerlist = copy_state->samplers.samplerlist;
        glstate->queries.querylist = copy_state->queries.querylist;

        glstate->defaultvbo = copy_state->defaultvbo;
    }
    // set all VAValue to default
    GLfloat vadefault[] = {0.0f, 0.0f, 0.0f, 1.0f};
    for (int i=0; i<MAX_VATTRIB; ++i)
        memcpy(glstate->vavalue[i], vadefault, sizeof(vadefault));
    // set shortcut
    glstate->vertex = glstate->vavalue[ATT_VERTEX];
    glstate->color = glstate->vavalue[ATT_COLOR];
    glstate->secondary = glstate->vavalue[ATT_SECONDARY];
    glstate->normal = glstate->vavalue[ATT_NORMAL];
    glstate->fogcoord = glstate->vavalue[ATT_FOGCOORD];
    for (int i=0; i<MAX_TEX; ++i)
        glstate->texcoord[i] = glstate->vavalue[ATT_MULTITEXCOORD0+i];
    // set specifics default
    GLfloat white[] = {1.0f, 1.0f, 1.0f, 1.0f};
    memcpy(glstate->color, white, sizeof(GLfloat)*4);
    glstate->shim_error = GL_NO_ERROR;
    glstate->normal[2] = 1.0f; // default normal is 0/0/1
    glstate->matrix_mode = GL_MODELVIEW;
    
    // add default VBO
    if(!shared_glstate) // TODO: check if default VBO is shared?
    {
        khint_t k;
        int ret;
        khash_t(buff) *list = glstate->buffers = kh_init(buff);
        k = kh_put(buff, list, 0, &ret);
        glbuffer_t *buff = kh_value(list, k) = calloc(1, sizeof(glbuffer_t));
        /*buff->buffer = 0;
        buff->type = 0;
        buff->data = NULL;*/
        buff->usage = GL_STATIC_DRAW;
        //buff->size = 0;
        buff->access = GL_READ_WRITE;
        //buff->mapped = 0;
        glstate->defaultvbo = buff;
    }
    // add default VAO
    {
        khint_t k;
        int ret;
        khash_t(glvao) *list = glstate->vaos = kh_init(glvao);
        k = kh_put(glvao, list, 0, &ret);
        glvao_t *glvao = kh_value(list, k) = malloc(sizeof(glvao_t));
        // new vao is binded to default vbo
        VaoInit(glvao);
        // just put is number
        glvao->array = 0;
        glstate->defaultvao = glvao;
    }
    // initialize gllists
    if(!shared_glstate)
    {
        khint_t k;
        int ret;
        khash_t(gllisthead) *list = glstate->headlists = kh_init(gllisthead);
		k = kh_put(gllisthead, list, 1, &ret);
		kh_del(gllisthead, list, k);
    }
    // actual_tex2d
    if(!shared_glstate)
    {
        glstate->actual_tex2d = (GLuint*)calloc(MAX_TEX, sizeof(GLuint));
    }
    // glsl
    glstate->gleshard = (gleshard_t*)calloc(1, sizeof(gleshard_t)); // Not shared!
    if(!shared_glstate)
    {
        glstate->glsl = (glsl_t*)malloc(sizeof(glsl_t));
        memset(glstate->glsl, 0, sizeof(glsl_t));
        InitOldProgramMap(glstate);
    }
    // Bind defaults...
    glstate->vao = glstate->defaultvao;

    //raster & viewport
    glstate->raster.raster_zoomx=1.0f;
    glstate->raster.raster_zoomy=1.0f;
    glstate->raster.rPos_valid = GL_TRUE;
    glstate->raster.map_i2i_size=1;
    glstate->raster.map_i2r_size=1;
    glstate->raster.map_i2g_size=1;
    glstate->raster.map_i2b_size=1;
    glstate->raster.map_i2a_size=1;
    /*
    glstate->raster.map_s2s_size=1;
    glstate->raster.map_r2r_size=1;
    glstate->raster.map_g2g_size=1;
    glstate->raster.map_b2b_size=1;
    glstate->raster.map_a2a_size=1;
    */

    // pack & unpack alignment
    glstate->texture.pack_align = 4;
    glstate->texture.unpack_align = 4;

    // eval
    glstate->map_grid[0]._2 = 1.0f;
    glstate->map_grid[0].n = 1;
    glstate->map_grid[0].d = 1.0f;
    glstate->map_grid[1]._2 = 1.0f;
    glstate->map_grid[1].n = 1;
    glstate->map_grid[1].d = 1.0f;
    
    // line stipple
    glstate->linestipple.factor = 1;
    glstate->linestipple.pattern = 0xFFFF;
    glstate->linestipple.data = (GLubyte *)malloc(sizeof(GLubyte) * 16);
        memset(glstate->linestipple.data, 0xff, sizeof(GLubyte) * 16);
    glstate->linestipple.texture = 0;    
    
    // fpe
    if(hardext.esversion>1) {
        glstate->fpe_state = (fpe_state_t*)calloc(1, sizeof(fpe_state_t));
        glstate->glsl->es2 = es2only;
        fpe_Init(glstate);
    }

    // init the matrix tracking
    init_matrix(glstate);

    // init the textures
    {
        gltexture_t* tex;
        if(!shared_glstate) {
            int ret;
            khint_t k;
            khash_t(tex) *list = glstate->texture.list;
            list = glstate->texture.list = kh_init(tex);
            // segfaults if we don't do a single put
            k = kh_put(tex, list, 1, &ret);
            kh_del(tex, list, k);
        }
        // now add default "0" texture => no, because tex 0 is not shared....
        /*k = kh_put(tex, list, 0, &ret);
        glstate->texture.zero = tex = kh_value(list, k) = malloc(sizeof(gltexture_t));*/
        glstate->texture.zero = tex = calloc(1, sizeof(gltexture_t));
        tex->adjustxy[0] = tex->adjustxy[1] = 1.f;
        tex->mipmap_auto = (globals4es.automipmap==1);
        tex->mipmap_need = (globals4es.automipmap==1)?1:0;
        tex->streamingID = -1;
        tex->base_level = -1;
        tex->max_level = -1;
        tex->alpha = true;
        init_sampler(&tex->sampler);
        tex->fpe_format = FPE_TEX_RGBA;
        tex->format = GL_RGBA;
        tex->type = GL_UNSIGNED_BYTE;
        tex->inter_format = GL_RGBA;
        tex->inter_type = GL_UNSIGNED_BYTE;
        // now bind that texture on all unit
        for (int itarget=0; itarget<ENABLED_TEXTURE_LAST; ++itarget)
            for (int i=0; i<MAX_TEX; ++i)
                glstate->texture.bound[i][itarget] = tex;
    }


    // init the light tracking
    glstate->light.ambient[0]=glstate->light.ambient[1]=glstate->light.ambient[2]=0.2f;
    glstate->light.ambient[3]=1.0f;
    glstate->light.lights[0].diffuse[0]=
    glstate->light.lights[0].diffuse[1]=
    glstate->light.lights[0].diffuse[2]=
    glstate->light.lights[0].diffuse[3]=1.0f;
    memcpy(glstate->light.lights[0].specular, glstate->light.lights[0].diffuse, 4*sizeof(GLfloat));
    for (int i=0; i<hardext.maxlights; i++) {
        glstate->light.lights[i].ambient[3] = 1.0f;
        glstate->light.lights[i].position[2] = 1.0f;
        glstate->light.lights[i].spotDirection[2] = -1.0f;
        glstate->light.lights[i].spotCutoff = 180;
        glstate->light.lights[i].constantAttenuation = 1;
    }
    // Materials
    glstate->material.front.ambient[0] = 
    glstate->material.front.ambient[1] =
    glstate->material.front.ambient[2] = 0.2f;
    glstate->material.front.ambient[3] = 1.0f;
    glstate->material.front.diffuse[0] = 
    glstate->material.front.diffuse[1] =
    glstate->material.front.diffuse[2] = 0.8f;
    glstate->material.front.diffuse[3] = 1.0f;
    glstate->material.front.specular[3] = 1.0f;
    glstate->material.front.emission[3] = 1.0f;
    glstate->material.front.colormat = GL_AMBIENT_AND_DIFFUSE;
    memcpy(&glstate->material.back, &glstate->material.front, sizeof(material_t));
    // Fog
    glstate->fog.mode = GL_EXP;
    glstate->fog.density = 1.0f;
    glstate->fog.end = 1.0f;
    glstate->fog.coord_src = GL_FRAGMENT_DEPTH;
    glstate->fog.distance = GL_EYE_PLANE_ABSOLUTE_NV;
    // Alpha Func
    glstate->alphafunc = GL_ALWAYS;
    glstate->alpharef = 0.0f;
    // Blend
    glstate->blendsfactorrgb = GL_ONE;
    glstate->blenddfactorrgb = GL_ZERO;
    glstate->blendsfactoralpha = GL_ONE;
    glstate->blenddfactoralpha = GL_ZERO;
    // Depth
    glstate->depth.func = GL_LESS;
    glstate->depth.mask = GL_TRUE;
    glstate->depth.Far  = 1.0f;
    glstate->depth.clear= 1.0f;
    // Line width, mipmap hint
    glstate->line_width = 1.0f;
    glstate->mipmap_hint = GL_DONT_CARE;
    // Face
    glstate->face.cull  = GL_BACK;
    glstate->face.front = GL_CCW;
    // Point Sprite
    glstate->pointsprite.size = 1.0f;
    glstate->pointsprite.sizeMax = 32.0f;   // spec indicate 1., but it seems it's set to hardware limit, so putting 32...
    glstate->pointsprite.fadeThresholdSize = 1.0f;
    glstate->pointsprite.distance[0] = 1.0f;
    glstate->pointsprite.coordOrigin = GL_UPPER_LEFT;
    // Stencil
    glstate->stencil.func[0] = glstate->stencil.func[1] = GL_ALWAYS;
    //glstate->stencil.f_ref[0] = glstate->stencil.f_ref[1] = 0;
    glstate->stencil.f_mask[0] = glstate->stencil.f_mask[1] = ~(GLuint)0;
    glstate->stencil.mask[0] = glstate->stencil.mask[1] = ~(GLuint)0;
    glstate->stencil.sfail[0] = glstate->stencil.sfail[1] = GL_KEEP;
    glstate->stencil.dpfail[0] = glstate->stencil.dpfail[1] = GL_KEEP;
    glstate->stencil.dppass[0] = glstate->stencil.dppass[1] = GL_KEEP;
    // Color Mask
    for(int i=0; i<4; i++)
        glstate->colormask[i] = 1;
    // Raster
    for(int i=0; i<4; i++)
        glstate->raster.raster_scale[i] = 1.0f;
    // ShadeModel
    glstate->shademodel = GL_SMOOTH;
    // TexEnv
    for (int i=0; i<hardext.maxtex; i++) {
        glstate->texenv[i].env.mode = GL_MODULATE;
        glstate->texenv[i].env.rgb_scale = 1.0f;
        glstate->texenv[i].env.alpha_scale = 1.0f;
        glstate->texenv[i].env.src0_rgb = glstate->texenv[i].env.src0_alpha = GL_TEXTURE;
        glstate->texenv[i].env.src1_rgb = glstate->texenv[i].env.src1_alpha = GL_PREVIOUS;
        glstate->texenv[i].env.src2_rgb = glstate->texenv[i].env.src2_alpha = GL_CONSTANT;
        glstate->texenv[i].env.op0_rgb = glstate->texenv[i].env.op1_rgb = GL_SRC_COLOR;
        glstate->texenv[i].env.op2_rgb = glstate->texenv[i].env.op0_alpha = 
        glstate->texenv[i].env.op1_alpha = glstate->texenv[i].env.op2_alpha = GL_SRC_ALPHA;
    }
    // TexGen
    for (int i=0; i<hardext.maxtex; i++) {
        glstate->texgen[i].S = GL_EYE_LINEAR;
        glstate->texgen[i].T = GL_EYE_LINEAR;
        glstate->texgen[i].R = GL_EYE_LINEAR;
        glstate->texgen[i].Q = GL_EYE_LINEAR;
        glstate->texgen[i].S_E[0] = 1.0;
        glstate->texgen[i].S_O[0] = 1.0;
        glstate->texgen[i].T_E[1] = 1.0;
        glstate->texgen[i].T_O[1] = 1.0;
    }

    // fpe
    if(hardext.esversion>1) {
        glstate->fpe_state = (fpe_state_t*)calloc(1, sizeof(fpe_state_t));
        glstate->glsl->es2 = es2only;
        if(!shared_glstate)
            fpe_Init(glstate);
        // some default are not 0...
        for (int i=0; i<MAX_TEX; i++) {
            //TexEnv Combine that are not 0
            glstate->fpe_state->texenv[i].texsrcrgb0 = FPE_SRC_TEXTURE;
            glstate->fpe_state->texenv[i].texsrcalpha0 = FPE_SRC_TEXTURE;
            glstate->fpe_state->texenv[i].texsrcrgb1 = FPE_SRC_PREVIOUS;
            glstate->fpe_state->texenv[i].texsrcalpha1 = FPE_SRC_PREVIOUS;
            glstate->fpe_state->texenv[i].texsrcrgb2 = FPE_SRC_CONSTANT;
            glstate->fpe_state->texenv[i].texsrcalpha2 = FPE_SRC_CONSTANT;
            glstate->fpe_state->texenv[i].texoprgb0 = FPE_OP_SRCCOLOR;
            glstate->fpe_state->texenv[i].texoprgb1 = FPE_OP_SRCCOLOR;
        }
        if(globals4es.shaderblend) {
            glstate->fpe_state->blendsrcrgb = FPE_BLEND_ONE;
            glstate->fpe_state->blendsrcalpha = FPE_BLEND_ONE;
        }
    }

    // GLSL stuff
    if(!shared_glstate)
    {
        khint_t k;
        int ret;
        khash_t(shaderlist) *shaders = glstate->glsl->shaders = kh_init(shaderlist);
		k = kh_put(shaderlist, shaders, 1, &ret);
		kh_del(shaderlist, shaders, k);
        khash_t(programlist) *programs = glstate->glsl->programs = kh_init(programlist);
		k = kh_put(programlist, programs, 1, &ret);
		kh_del(programlist, programs, k);
    }

    // Grab ViewPort & Scissor
#if defined(AMIGAOS4) || defined(__EMSCRIPTEN__)
    if(!def) {// if it's default_glstate, then there is probably no glcontext...
#endif
    LOAD_GLES(glGetIntegerv);
    gles_glGetIntegerv(GL_VIEWPORT, (GLint*)&glstate->raster.viewport);
    gles_glGetIntegerv(GL_SCISSOR_BOX, (GLint*)&glstate->raster.scissor);
    glstate->raster.viewport_known = 1;
    glstate->raster.scissor_known = 1;
#if defined(AMIGAOS4) || defined(__EMSCRIPTEN__)
    }
#endif
    // FBO
    glstate->fbowidth  = glstate->fbo.mainfbo_width  = glstate->raster.viewport.width;
    glstate->fboheight = glstate->fbo.mainfbo_height = glstate->raster.viewport.height;
    glstate->fbo.mainfbo_nwidth = (hardext.npot)?glstate->fbo.mainfbo_width:npot(glstate->fbo.mainfbo_width);
    glstate->fbo.mainfbo_nheight = (hardext.npot)?glstate->fbo.mainfbo_height:npot(glstate->fbo.mainfbo_height);
    // add default Renderbuffer
    if(!shared_glstate)
    {
        khint_t k;
        int ret;
        khash_t(renderbufferlist_t) *list = glstate->fbo.renderbufferlist = kh_init(renderbufferlist_t);
        k = kh_put(renderbufferlist_t, list, 0, &ret);
        glrenderbuffer_t *rend = kh_value(list, k) = calloc(1, sizeof(glrenderbuffer_t));
        glstate->fbo.default_rb = rend;
    }
    // add default Framebuffer
    if(!shared_glstate)
    {
        khint_t k;
        int ret;
        khash_t(framebufferlist_t) *list = glstate->fbo.framebufferlist = kh_init(framebufferlist_t);
        k = kh_put(framebufferlist_t, list, 0, &ret);
        glframebuffer_t *fb = kh_value(list, k) = calloc(1, sizeof(glframebuffer_t));
        fb->width = glstate->fbo.mainfbo_width;
        fb->height = glstate->fbo.mainfbo_height;
        glstate->fbo.fbo_0 = fb;
        if(globals4es.recyclefbo) {
            glstate->fbo.old = (oldfbos_t*)calloc(1, sizeof(oldfbos_t));
        }
    }
    glstate->fbo.current_fb = glstate->fbo.fbo_0;
    glstate->fbo.current_rb = glstate->fbo.default_rb;
    glstate->fbo.fbo_read = glstate->fbo.fbo_0;
    glstate->fbo.fbo_draw = glstate->fbo.fbo_0;
    // Samplers & queries
    if(!shared_glstate)
    {
        glstate->samplers.samplerlist = kh_init(samplerlist_t);
        glstate->queries.querylist = kh_init(queries);
    }
    glstate->queries.start = get_clock();
    // Get the per/context hardware values
    glstate->readf = GL_RGBA;
    glstate->readt = GL_UNSIGNED_BYTE;
#if defined(AMIGAOS4) || defined(__EMSCRIPTEN__)
    if(!def) // if it's default_glstate, then there is probably no glcontext...
    {
    LOAD_GLES(glGetIntegerv);
#endif
    gles_glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_FORMAT_OES, (GLint *) &glstate->readf);
    gles_glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_TYPE_OES, (GLint *) &glstate->readt);
#if defined(AMIGAOS4) || defined(__EMSCRIPTEN__)
    }
#endif
    //printf("LIBGL: Implementation Read is %s/%s\n", PrintEnum(glstate->readf), PrintEnum(glstate->readt));

    // All done
    return (void*)glstate;
}


void DeleteGLState(void* oldstate) {
    glstate_t* state = (glstate_t*)oldstate;
    if(!state) return;
    if(state==&default_glstate) return;
    if(oldstate==DEFAULT_STATE)
        state = &default_glstate;

    if(state->shared_cnt) {
        if(!--(*state->shared_cnt)) {
            free(state->shared_cnt);
            state->shared_cnt = 0;
        }
    }
    if(globals4es.noclean)
        return;
        
    if(glstate == state)
        glstate = (oldstate==DEFAULT_STATE)?NULL:&default_glstate;

    if(!state->shared_cnt)
        free(state->actual_tex2d);
    
    #define free_hashmap(T, N, K, F)        \
    if(state->N)                            \
    {                                       \
        T *m;                               \
        kh_foreach_value(state->N, m,       \
            F(m);                        \
        )                                   \
        kh_destroy(K, state->N);            \
    }
    free_hashmap(glvao_t, vaos, glvao, free);
    if(!state->shared_cnt) {
        free_hashmap(glbuffer_t, buffers, buff, free);
        free_hashmap(gltexture_t, texture.list, tex, free_texture);
        free_hashmap(renderlist_t, headlists, gllisthead, free_renderlist);
        free_hashmap(glrenderbuffer_t, fbo.renderbufferlist, renderbufferlist_t, free_renderbuffer);
        free_hashmap(glframebuffer_t, fbo.framebufferlist, framebufferlist_t, free_framebuffer);
        free_hashmap(glsampler_t, samplers.samplerlist, samplerlist_t, free);
        free_hashmap(glquery_t, queries.querylist, queries, free);
    }
    #undef free_hashmap
    // free texture zero as it's not in the list anymore
    free(state->texture.zero);
    // free eval maps
    #define freemap(dims, name)                              \
    { map_statef_t *m = (map_statef_t *)state->map##dims.name; \
    if (m) {                                                \
        free((void *)m->points);                            \
        free(m);                                            \
    } }
    freemap(1, vertex3); freemap(1, vertex4); freemap(1, index); freemap(1, color4); freemap(1, normal); 
    freemap(1, texture1); freemap(1, texture2); freemap(1, texture3); freemap(1, texture4);   
    freemap(2, vertex3); freemap(2, vertex4); freemap(2, index); freemap(2, color4); freemap(2, normal); 
    freemap(2, texture1); freemap(2, texture2); freemap(2, texture3); freemap(2, texture4);   
    #undef freemap
    // free active list
    if(!state->shared_cnt && state->list.active) free_renderlist(state->list.active);

    // free matrix stack
    #define free_matrix(A)                  \
        if (state->A) {                   \
    	    free(state->A->stack);    \
            free(state->A);               \
        }
	free_matrix(projection_matrix);
	free_matrix(modelview_matrix);
	for (int i=0; i<MAX_TEX; i++)
		free_matrix(texture_matrix[i]);
	free(state->texture_matrix);
	for (int i=0; i<MAX_ARB_MATRIX; i++)
		free_matrix(arb_matrix[i]);
    #undef free_matrix
    // states stack
    if(state->stack)
        free(state->stack);
    if(state->clientStack)
        free(state->clientStack);
    // linestipple
    if(state->linestipple.data)
        free(state->linestipple.data);
    // raster / bitmap
    if(state->raster.data)
        free(state->raster.data);
    if(state->raster.bitmap)
        free(state->raster.bitmap);
    // TODO: delete the "immediate" stuff and bitmap texture?
    // scratch buffer
    if(state->scratch)
        free(state->scratch);
    // merger buffers
    if(state->merger_master)
        free(state->merger_master);
    if(state->merger_secondary)
        free(state->merger_secondary);
    for(int a=0; a<MAX_TEX-2; ++a)
        if(state->merger_tex[a])
            free(state->merger_tex[a]);
    // mainfbo
    if(!state->shared_cnt) {
        if(state->fbo.mainfbo_fbo)
            deleteMainFBO(state);
    }
    // oldfbos
    if(!state->shared_cnt && state->fbo.old) {
        LOAD_GLES2_OR_OES(glDeleteFramebuffers);
        gles_glDeleteFramebuffers(state->fbo.old->nbr, state->fbo.old->fbos);
        free(state->fbo.old->fbos);
        free(state->fbo.old);
    }
    // free blit GLES2 stuff
    if(state->blit) {
        //TODO: check if should delete GL object too
        free(state->blit);
    }
    if(!state->shared_cnt) {
        FreeOldProgramMap(state);
        free(state->glsl);
        if(state->fpe_cache) {
            fpe_Dispose(state);
        }
    }
    free(state->gleshard);  // Not shared!
    // get extensions
    if(state->extensions)
        free(state->extensions);
    if(state->extensions_list) {
        for(int i=0; i<state->num_extensions; ++i)
            free(state->extensions_list[i]);
        free(state->extensions_list);
    }
    // helper texture adjust
    for(int i=0; i<MAX_TEX; ++i) {
        if(state->helper_tex[i])
            free(state->helper_tex[i]);
        if(state->texgened[i])
            free(state->texgened[i]);

    }
    //TODO: free sharderlist and programlist...

    // probably missing some things to free here!

    // all done
    if(oldstate!=DEFAULT_STATE)
        free(state);
    return;
}

void ActivateGLState(void* new_glstate) {
    glstate_t *newstate = (new_glstate)?(glstate_t*)new_glstate:&default_glstate;
    if(glstate == newstate) return;  // same state, nothing to do
    // check if viewport is correct
#if defined(AMIGAOS4) || defined(__EMSCRIPTEN__)
    if(glstate || newstate!=&default_glstate) // avoid getting gles info with no context
#endif
    if(new_glstate && (newstate->raster.viewport.width==0 || newstate->raster.viewport.height==0)) {
        LOAD_GLES(glGetIntegerv);
        gles_glGetIntegerv(GL_VIEWPORT, (GLint*)&newstate->raster.viewport);
        gles_glGetIntegerv(GL_SCISSOR_BOX, (GLint*)&newstate->raster.scissor);
        newstate->raster.viewport_known = 1;
        newstate->raster.scissor_known = 1;
    }
    glstate = newstate;
}

void gl_init() {
	#ifdef GL4ES_COMPILE_FOR_USE_IN_SHARED_LIB
		glstate=NULL;
		memset(&default_glstate,0,sizeof(glstate_t));
	#endif
  (void)NewGLState(DEFAULT_STATE, 0); // automaticaly fill default_glstate
  ActivateGLState(&default_glstate);
}

void gl_close() {
 DeleteGLState(DEFAULT_STATE);
}
