#include "list.h"

#include "../glx/hardext.h"
#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "matrix.h"

static inline void rlVertexCommon(renderlist_t *list, int idx, int l) {
    if(list->use_glstate) {
        resize_renderlist(list);
        if (!list->vert)    list->vert = glstate->merger_master;
        if (list->normal)   memcpy(list->normal + idx, list->lastNormal, sizeof(GLfloat) * 3);
        if (list->fogcoord) memcpy(list->fogcoord + idx, glstate->fogcoord, sizeof(GLfloat) * 1);
    } else {
        if (!list->vert)    list->vert = alloc_sublist(4, list->cap); 
        else                resize_renderlist(list);
        if (list->normal)   memcpy(list->normal + (l * 3), list->lastNormal, sizeof(GLfloat) * 3);
        if (list->fogcoord) memcpy(list->fogcoord + (l * 1), glstate->fogcoord, sizeof(GLfloat) * 1);
    }
    // common part
    if (list->color)    memcpy(list->color + idx, list->lastColors, sizeof(GLfloat) * 4);
    if (list->secondary)    memcpy(list->secondary + (l * 4), glstate->secondary, sizeof(GLfloat) * 4);
    if (list->tex[0])   memcpy(list->tex[0] + idx, glstate->texcoord[0], sizeof(GLfloat) * 4);
    if (list->tex[1])   memcpy(list->tex[1] + idx, glstate->texcoord[1], sizeof(GLfloat) * 4);
    for (int a=2; a<list->maxtex; a++)
        if (list->tex[a])   memcpy(list->tex[a] + (l * 4), glstate->texcoord[a], sizeof(GLfloat) * 4);
}

void FASTMATH rlVertex4f(renderlist_t *list, GLfloat x, GLfloat y, GLfloat z, GLfloat w) {
    const int idx = (list->use_glstate)?(list->len * 5*4):(list->len * 4);
    rlVertexCommon(list, idx, list->len);
    ++list->len;

    GLfloat * const vert = list->vert + idx;
    vert[0] = x; vert[1] = y; vert[2] = z; vert[3] = w;
}
void FASTMATH rlVertex3fv(renderlist_t *list, GLfloat* v) {
    const int idx = (list->use_glstate)?(list->len * 5*4):(list->len * 4);
    rlVertexCommon(list, idx, list->len);

    GLfloat * const vert = list->vert + idx;
    ++list->len;
    memcpy(vert, v, 3*sizeof(GLfloat));
    vert[3] = 1.f;
}
void FASTMATH rlVertex4fv(renderlist_t *list, GLfloat* v) {
    const int idx = (list->use_glstate)?(list->len * 5*4):(list->len * 4);
    rlVertexCommon(list, idx, list->len);

    GLfloat * const vert = list->vert + idx;
    ++list->len;
    memcpy(vert, v, 4*sizeof(GLfloat));
}

void rlEnd(renderlist_t *list) {
    // adjust number of vertex, to remove extra vertex
    int adj = list->len - list->cur_istart;
    adj -= adjust_vertices(list->merger_mode?list->merger_mode:list->mode_init, adj);
    //printf("rlEnd(%d), indices=%p, mode_init_len=%d, len/cur_istart(adj)/ilen=%d/%d(%d)/%d, mode/mode_init=%s/%s, merger_mode=%s\n", list, list->indices, list->mode_init_len, list->len, list->cur_istart, adj, list->ilen, PrintEnum(list->mode), PrintEnum(list->mode_init), list->merger_mode?PrintEnum(list->merger_mode):"none");
    list->len -= adj;
    if(!list->mode_inits && list->cur_istart) list_add_modeinit(list, list->mode_init);
    if(list->indices && list->merger_mode && list->len-list->cur_istart) {
        // also feed the indices...
        int istart = list->cur_istart;
        int ivert = 0;
        int len = list->len-istart;
        resize_indices_renderlist(list, indices_getindicesize(list->merger_mode, len));
        switch (list->merger_mode) {
            case GL_LINE_STRIP:
                if(len>1) {
                    list->indices[list->ilen++]=istart+(ivert);
                    list->indices[list->ilen++]=istart+(++ivert);
                    for (int i=2; i<len; i++) {
                        list->indices[list->ilen++]=istart+(ivert);
                        list->indices[list->ilen++]=istart+(++ivert);
                    }
                }
                break;
            case GL_LINE_LOOP:
                if(len>1) {
                    list->indices[list->ilen++]=istart+(ivert++);
                    list->indices[list->ilen++]=istart+(ivert++);
                    for (int i=istart+2; i<list->len; i++) {
                        list->indices[list->ilen++]=istart+(ivert-1);
                        list->indices[list->ilen++]=istart+(ivert++);
                    }
                    list->indices[list->ilen++]=istart+(ivert-1);
                    list->indices[list->ilen++]=istart;
                }
                break;
            case GL_POLYGON:
            case GL_TRIANGLE_FAN:
                if(len>2) {
                    list->indices[list->ilen++]=istart+(ivert++);
                    list->indices[list->ilen++]=istart+(ivert++);
                    list->indices[list->ilen++]=istart+(ivert++);
                }
                for (int i=istart+3; i<list->len; i++) {
                        // add a new triangle for each new point
                        list->indices[list->ilen++]=istart;
                        list->indices[list->ilen++]=istart+(ivert-1);
                        list->indices[list->ilen++]=istart+(ivert++);
                    }
                break;
            case GL_QUAD_STRIP:
            case GL_TRIANGLE_STRIP:
                if(len>2) {
                    list->indices[list->ilen++]=istart+(ivert++);
                    list->indices[list->ilen++]=istart+(ivert++);
                    list->indices[list->ilen++]=istart+(ivert++);
                }
                for (int i=istart+3; i<list->len; i++) {
                        // add a new triangle for each new point
                        list->indices[list->ilen++]=istart+(ivert-((ivert%2)?1:2));
                        list->indices[list->ilen++]=istart+(ivert-((ivert%2)?2:1));
                        list->indices[list->ilen++]=istart+(ivert++);
                    }
                break;
            case GL_QUADS:
                if(len>3)
                for (int i=istart; i+3<list->len; i+=4) {
                    list->indices[list->ilen++]=i+0;
                    list->indices[list->ilen++]=i+1;
                    list->indices[list->ilen++]=i+2;

                    list->indices[list->ilen++]=i+0;
                    list->indices[list->ilen++]=i+2;
                    list->indices[list->ilen++]=i+3;
                }
                break;
            default:
                for (int i=istart; i<list->len; i++)
                    list->indices[list->ilen++]=i;
                break;
        }
    }
    list->cur_istart = 0;
    if(list->mode_inits) list_add_modeinit(list, list->merger_mode?list->merger_mode:list->mode_init);
    if(list->color)
        memcpy(glstate->color, list->lastColors, 4*sizeof(GLfloat));
    if(list->normal)
        memcpy(glstate->normal, list->lastNormal, 3*sizeof(GLfloat));
}

static inline void rlNormalCommon(renderlist_t *list) {
    if (list->normal == NULL) {
        const int stride = (list->use_glstate)?(5*4):3;
        if(list->use_glstate) {
            list->normal = glstate->merger_master+4+4+2*4;
        } else {
            list->normal = alloc_sublist(3, list->cap);
        }
        // catch up
        for (int i = 0; i < list->len; i++) {
            GLfloat *normal = (list->normal + (i * stride));
            memcpy(normal, list->lastNormal, sizeof(GLfloat) * 3);
        }
    }
}
void FASTMATH rlNormal3f(renderlist_t *list, GLfloat x, GLfloat y, GLfloat z) {
    rlNormalCommon(list);

    GLfloat *normal = list->lastNormal;
    normal[0] = x; normal[1] = y; normal[2] = z;
}
void FASTMATH rlNormal3fv(renderlist_t *list, GLfloat*v) {
    rlNormalCommon(list);
    
    memcpy(list->lastNormal, v, 3*sizeof(GLfloat));
}

static inline void rlColorCommon(renderlist_t *list) {
    if (list->color == NULL) {
        list->lastColorsSet = 1;
        const int stride = (list->use_glstate)?(5*4):4;
        if(list->use_glstate) {
            list->color = glstate->merger_master+4;
        } else {
            list->color = alloc_sublist(4, list->cap);
        }
        // catch up
        for (int i = 0; i < list->len; i++) {
            GLfloat *color = (list->color + (i * stride));
            memcpy(color,list->lastColors, sizeof(GLfloat) * 4);
        }
    }
}
void FASTMATH rlColor4f(renderlist_t *list, GLfloat r, GLfloat g, GLfloat b, GLfloat a) {
    rlColorCommon(list);

    GLfloat *color = list->lastColors;
    color[0] = r; color[1] = g; color[2] = b; color[3] = a;
}
void FASTMATH rlColor4fv(renderlist_t *list, GLfloat* v) {
    rlColorCommon(list);
    
    memcpy(list->lastColors, v, 4*sizeof(GLfloat));
}

void FASTMATH rlSecondary3f(renderlist_t *list, GLfloat r, GLfloat g, GLfloat b) {
    if (list->secondary == NULL) {
        if(list->use_glstate) {
            if(!glstate->merger_secondary)
                glstate->merger_secondary = (GLfloat*)malloc(sizeof(GLfloat)*4*glstate->merger_cap);
            list->secondary = glstate->merger_secondary;
        } else {
            list->secondary = alloc_sublist(4, list->cap);
        }
        // catch up
        GLfloat *secondary = list->secondary;
        for (int i = 0; i < list->len; i++) {
            memcpy(secondary, list->lastSecondaryColors, sizeof(GLfloat) * 4);
            secondary += 4;
        }
    }

    GLfloat *color = glstate->secondary;
    color[0] = r; color[1] = g; color[2] = b; color[3] = 0.0f;
}

void rlMaterialfv(renderlist_t *list, GLenum face, GLenum pname, const GLfloat * params) {
    rendermaterial_t *m;
    khash_t(material) *map;
    khint_t k;
    int ret;
    if (! list->material) {
        list->material = map = kh_init(material);
        // segfaults if we don't do a single put
        kh_put(material, map, 1, &ret);
        kh_del(material, map, 1);
    } else {
        map = list->material;
    }

    int iface = (face==GL_FRONT)?0:((face==GL_BACK)?1:2);
    int key = pname | (iface<<16);
    k = kh_get(material, map, key);
    if (k == kh_end(map)) {
        k = kh_put(material, map, key, &ret);
        m = kh_value(map, k) = malloc(sizeof(rendermaterial_t));
    } else {
        m = kh_value(map, k);
    }

    m->face = face;
    m->pname = pname;
    int sz=4;
    if(pname==GL_SHININESS || pname==GL_COLOR_INDEXES) sz=1;
    memcpy(m->color, params, sz*sizeof(GLfloat));
}

void rlLightfv(renderlist_t *list, GLenum which, GLenum pname, const GLfloat * params) {
    renderlight_t *m;
    khash_t(light) *map;
    khint_t k;
    int ret;
    if (! list->light) {
        list->light = map = kh_init(light);
        // segfaults if we don't do a single put
        kh_put(light, map, 1, &ret);
        kh_del(light, map, 1);
    } else {
        map = list->light;
    }

	int key = pname | ((which-GL_LIGHT0)<<16);
    k = kh_get(light, map, key);
    if (k == kh_end(map)) {
        k = kh_put(light, map, key, &ret);
        m = kh_value(map, k) = malloc(sizeof(renderlight_t));
    } else {
        m = kh_value(map, k);
    }

    m->which = which;
    m->pname = pname;
    int sz=4;
    if(pname==GL_SPOT_DIRECTION) sz=3;
    if(pname==GL_SPOT_EXPONENT || pname==GL_SPOT_CUTOFF 
        || pname==GL_CONSTANT_ATTENUATION || pname==GL_LINEAR_ATTENUATION
        || pname==GL_QUADRATIC_ATTENUATION) sz=1;
    memcpy(m->color, params, sz*sizeof(GLfloat));
}

void rlTexGenfv(renderlist_t *list, GLenum coord, GLenum pname, const GLfloat * params) {
    rendertexgen_t *m;
    khash_t(texgen) *map;
    khint_t k;
    int ret;
    if (! list->texgen) {
        list->texgen = map = kh_init(texgen);
        // segfaults if we don't do a single put
        kh_put(texgen, map, 1, &ret);
        kh_del(texgen, map, 1);
    } else {
        map = list->texgen;
    }

	int key = pname | ((coord-GL_S)<<16);
    k = kh_get(texgen, map, key);
    if (k == kh_end(map)) {
        k = kh_put(texgen, map, key, &ret);
        m = kh_value(map, k) = malloc(sizeof(rendertexgen_t));
    } else {
        m = kh_value(map, k);
    }

    m->coord = coord;
    m->pname = pname;
    memcpy(m->color, params, 4*sizeof(GLfloat));
}

void rlMultiTexCoordCommon(renderlist_t* list, int tmu) {
    if (list->tex[tmu] == NULL) {
        if (list->maxtex<tmu+1) list->maxtex = tmu+1;
        const int stride = (list->use_glstate && tmu<2)?(5*4):4;
        if(list->use_glstate) {
            if(tmu<2)
                list->tex[tmu] = glstate->merger_master+4+4+tmu*4;
            else {
                if(!glstate->merger_tex[tmu-2])
                    glstate->merger_tex[tmu-2] = (GLfloat*)malloc(sizeof(GLfloat)*4*glstate->merger_cap);
                list->tex[tmu] = glstate->merger_tex[tmu-2];
            }
        } else {
            list->tex[tmu] = alloc_sublist(4, list->cap);
        }
        // catch up
        GLfloat *tex = list->tex[tmu];
        for (int i = 0; i < list->len; i++) {
            memcpy(tex, glstate->texcoord[tmu], sizeof(GLfloat) * 4);
            tex += stride;
        }
    }
}
void FASTMATH rlMultiTexCoord4f(renderlist_t *list, GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q) {
    const int tmu = target - GL_TEXTURE0;
    rlMultiTexCoordCommon(list, tmu);

    GLfloat *tex = glstate->texcoord[tmu];
    tex[0] = s; tex[1] = t;
    tex[2] = r; tex[3] = q;
}
void FASTMATH rlMultiTexCoord2fv(renderlist_t *list, GLenum target, GLfloat* v) {
    const int tmu = target - GL_TEXTURE0;
    rlMultiTexCoordCommon(list, tmu);

    GLfloat *tex = glstate->texcoord[tmu];
    memcpy(tex, v, 2*sizeof(GLfloat));
    tex[2] = 0.f; tex[3] = 1.f;
}
void FASTMATH rlMultiTexCoord4fv(renderlist_t *list, GLenum target, GLfloat* v) {
    const int tmu = target - GL_TEXTURE0;
    rlMultiTexCoordCommon(list, tmu);

    GLfloat *tex = glstate->texcoord[tmu];
    memcpy(tex, v, 4*sizeof(GLfloat));
}

void FASTMATH rlFogCoordf(renderlist_t *list, GLfloat coord) {
    if (list->fogcoord == NULL) {
        const int stride = (list->use_glstate)?(5*4):1;
        if(list->use_glstate) {
            list->fogcoord = glstate->merger_master+4+4+2*4+3;
        } else {
            list->fogcoord = alloc_sublist(1, list->cap);
        }
        // catch up
        GLfloat *fog = list->fogcoord;
        for (int i = 0; i < list->len; i++) {
            memcpy(fog, glstate->fogcoord, sizeof(GLfloat) * 1);
            fog+=stride;
        }
    }
    glstate->fogcoord[0] = coord;
}

void rlActiveTexture(renderlist_t *list, GLenum texture ) {
    list->set_tmu = true;
    list->tmu = texture - GL_TEXTURE0;
    if(list->maxtex < list->tmu+1) list->maxtex = list->tmu+1;
}

void rlBindTexture(renderlist_t *list, GLenum target, GLuint texture) {
    list->texture = texture;
    list->target_texture = target;
    list->set_texture = true;
}

void rlRasterOp(renderlist_t *list, int op, GLfloat x, GLfloat y, GLfloat z) {
    list->raster_op = op;
    list->raster_xyz[0] = x;
    list->raster_xyz[1] = y;
    list->raster_xyz[2] = z;
}

void rlFogOp(renderlist_t *list, int op, const GLfloat* v) {
    int n = 1;
    if (op==GL_FOG_COLOR) n = 4;
    list->fog_op = op;
    list->fog_val[0] = v[0];
    if (n>1) list->fog_val[1] = v[1];
    if (n>2) list->fog_val[2] = v[2];
    if (n>3) list->fog_val[3] = v[3];
}

void rlPointParamOp(renderlist_t *list, int op, const GLfloat* v) {
    list->pointparam_op = op;
    list->pointparam_val[0] = v[0];
    list->pointparam_val[1] = v[1];
    list->pointparam_val[2] = v[2];
    list->pointparam_val[3] = v[3];
}

void rlPushCall(renderlist_t *list, packed_call_t *data) {
    call_list_t *cl = &list->calls;
    if (!cl->calls) {
        cl->cap = DEFAULT_CALL_LIST_CAPACITY;
        cl->calls = malloc(DEFAULT_CALL_LIST_CAPACITY * sizeof(void*));
    } else if (list->calls.len == list->calls.cap) {
        cl->cap += DEFAULT_CALL_LIST_CAPACITY;
        cl->calls = realloc(cl->calls, cl->cap * sizeof(void*));
    }
    cl->calls[cl->len++] = data;
}

renderlist_t* GetFirst(renderlist_t* list) {
    while(list->prev)
        list = list->prev;
    return list;
}

void rlTexEnvfv(renderlist_t *list, GLenum target, GLenum pname, const GLfloat * params) {
    int n = 1;
    switch(target) {
        case GL_POINT_SPRITE: n = 1; break;
        default:
            switch(pname) {
                case GL_TEXTURE_ENV_MODE: n=1; break;
                case GL_TEXTURE_ENV_COLOR: n=4; break;
                case GL_TEXTURE_LOD_BIAS: n=1; break;
            }
    }
    rendertexenv_t *m;
    khash_t(texenv) *map;
    khint_t k;
    int ret;
    if (! list->texenv) {
        list->texenv = map = kh_init(texenv);
        // segfaults if we don't do a single put
        kh_put(texenv, map, 1, &ret);
        kh_del(texenv, map, 1);
    } else {
        map = list->texenv;
    }

	int key = pname | ((target)<<16);
    k = kh_get(texenv, map, key);
    if (k == kh_end(map)) {
        k = kh_put(texenv, map, key, &ret);
        m = kh_value(map, k) = malloc(sizeof(rendertexenv_t));
    } else {
        m = kh_value(map, k);
    }

    m->target = target;
    m->pname = pname;
    memcpy(m->params, params, n*sizeof(GLfloat));
}
void rlTexEnviv(renderlist_t *list, GLenum target, GLenum pname, const GLint * params) {
    GLfloat fparams[4];
    int n = 1;
    switch(target) {
        case GL_POINT_SPRITE: n = 1; break;
        default:
            switch(pname) {
                case GL_TEXTURE_ENV_MODE: n=1; break;
                case GL_TEXTURE_ENV_COLOR: n=4; break;
                case GL_TEXTURE_LOD_BIAS: n=1; break;
            }
    }
    for (int i=0; i<n; i++) fparams[i] = params[i];
    rlTexEnvfv(list, target, pname, fparams);
}

#undef alloc_sublist
#undef realloc_sublist

renderlist_t* NewDrawStage(renderlist_t* l, GLenum m) {
    if(globals4es.mergelist && !glstate->polygon_mode
        && ((isempty_renderlist(l) && l->prev && l->prev->open && l->prev->mode==m && l->prev->mode_init==m)
            || (l->stage==STAGE_POSTDRAW && l->open))
        && ((l->mode_dimension==rendermode_dimensions(m) && l->mode_dimension>0)))
    {
        return recycle_renderlist(l, m);
    } else {
        // if we are just in glBegin/glEnd merger, then purge the list...
        if(!glstate->list.compiling && glstate->list.pending) {
            glstate->list.active = NULL;
            l = end_renderlist(l);
            draw_renderlist(l);
            free_renderlist(l);
            l = alloc_renderlist();
            NewStage(l, STAGE_DRAW);
        } else {
            NewStage(l, STAGE_DRAW);
        }
        l->mode=m;
        l->mode_init=m;
        l->mode_dimension = rendermode_dimensions(m);
        if(!glstate->merger_used && !glstate->list.compiling
            && !(glstate->render_mode==GL_SELECT || glstate->polygon_mode==GL_LINE || glstate->polygon_mode == GL_POINT)) 
        {
            l->vert_stride=sizeof(GLfloat)*4*5;
            l->color_stride=sizeof(GLfloat)*4*5;
            l->tex_stride[0]=sizeof(GLfloat)*4*5;
            l->tex_stride[1]=sizeof(GLfloat)*4*5;
            l->normal_stride=sizeof(GLfloat)*4*5;
            l->fogcoord_stride=sizeof(GLfloat)*4*5;
            l->use_glstate=1;
            glstate->merger_used=1;
        }
        return l;
    }
}