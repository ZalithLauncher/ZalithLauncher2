#include "list.h"

#include "../glx/hardext.h"
#include "const.h"
#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "loader.h"
#include "buffers.h"

// KH Map implementation
KHASH_MAP_IMPL_INT(material, rendermaterial_t *);
KHASH_MAP_IMPL_INT(light, renderlight_t *);
KHASH_MAP_IMPL_INT(texgen, rendertexgen_t *);
KHASH_MAP_IMPL_INT(texenv, rendertexenv_t *);
KHASH_MAP_IMPL_INT(gllisthead, renderlist_t*);

renderlist_t *alloc_renderlist() {

    renderlist_t *list = (renderlist_t *)malloc(sizeof(renderlist_t));
    memset(list, 0, sizeof(renderlist_t));
    list->cap = DEFAULT_RENDER_LIST_CAPACITY;
    list->matrix_val[0] = list->matrix_val[5] = list->matrix_val[10] = 
                          list->matrix_val[15] = 1.0f;
    list->lightmodelparam = GL_LIGHT_MODEL_AMBIENT;
    list->target_texture = GL_TEXTURE_2D;
    list->tmu = glstate->texture.active;

    memcpy(list->lastNormal, glstate->normal, 3*sizeof(GLfloat));
    memcpy(list->lastSecondaryColors, glstate->secondary, 3*sizeof(GLfloat));
    memcpy(list->lastColors, glstate->color, 4*sizeof(GLfloat));
    memcpy(&list->lastFogCoord, glstate->fogcoord, 1*sizeof(GLfloat));

    list->instanceCount = 1;

    list->open = true;
    return list;
}

bool ispurerender_renderlist(renderlist_t *list) {
    // return true if renderlist contains only rendering command, no state changes
    if (list->calls.len)
        return false;
    if (list->matrix_op)
        return false;
    if (list->raster_op)
        return false;
    if (list->raster)
        return false;
    if (list->bitmaps)
        return false;
    if (list->pushattribute)
        return false;
    if (list->popattribute)
        return false;
    if (list->material || list->colormat_face || list->light || list->lightmodel || list->texgen || list->texenv)
        return false;
    if (list->fog_op)
        return false;
    if (list->instanceCount!=1)
        return false;
    if (list->pointparam_op)
        return false;
    if (list->mode_init == 0)
        return false;
    if (list->ind_lines || list->final_colors)
        return false;
    if (list->set_texture || list->set_tmu)
        return false;
    
    return true;
}

bool isempty_renderlist(renderlist_t *list) {
    return (list->stage == STAGE_NONE);
}

int rendermode_dimensions(GLenum mode) {
    // return 1 for points, 2 for any lines, 3 for any triangles, 4 for any Quad and 5 for polygon
    switch (mode) {
        case GL_POINTS:
            return 1;
        case GL_LINES:
        case GL_LINE_LOOP:
        case GL_LINE_STRIP:
            return 2;
        case GL_TRIANGLES:
        case GL_TRIANGLE_FAN:
        case GL_TRIANGLE_STRIP:
            return 3;
        case GL_QUADS:
        case GL_QUAD_STRIP:
            return 3;   // It's 4, but we use only triangles...
        case GL_POLYGON:
            return 3;   // It's 5, but we use only triangles too.
    }
    return 0;
}

bool islistscompatible_renderlist(renderlist_t *a, renderlist_t *b) {
    if (!globals4es.mergelist || !a)
        return false;

    // check if 2 "pure rendering" list are compatible for merge
    if (a->mode_init != b->mode_init) {
        int a_mode = a->mode_dimension;
        int b_mode = b->mode_dimension;
        if ((a_mode == 0) || (b_mode == 0))
            return false;       // undetermined is not good
        if (a_mode == 4) a_mode = 3; // quads are handled as triangles
        if (b_mode == 4) b_mode = 3;
        if (a_mode != b_mode)
            return false;
    }
    if(a->use_vbo_array==2 || b->use_vbo_array==2)
        return false;
    if(a->use_vbo_indices==2 || b->use_vbo_indices==2)
        return false;
    if(!a->open || !b->open)
        return false;
    if(a->use_glstate)
        return false;   //TODO: Handle this case
/*    if ((a->indices==NULL) != (b->indices==NULL))
        return false;*/
    if (a->polygon_mode != b->polygon_mode)
        return false;
    if ((a->vert==NULL) != (b->vert==NULL))
        return false;
    if ((a->normal==NULL) != (b->normal==NULL))
        return false;
    if ((a->color==NULL) != (b->color==NULL))
        return false;
    if ((a->secondary==NULL) != (b->secondary==NULL))
        return false;
    if ((a->fogcoord==NULL) != (b->fogcoord==NULL))
        return false;
    // check the textures
    if(a->maxtex!=b->maxtex)
        return false;
    for (int i=0; i<a->maxtex; i++)
        if ((a->tex[i]==NULL) != (b->tex[i]==NULL))
            return false;
    if ((a->set_texture==b->set_texture) && ((a->texture != b->texture) || (a->target_texture != b->target_texture)))
        return false;
    if (!a->set_texture && b->set_texture)
        return false;
    // post color is only important if b as no color pointer...
    if(a->post_color && b->color==NULL)
        return false;
    // same for post_normal
    if(a->post_normal && b->normal==NULL)
        return false;
    // check instanceCount (maybe it would be better to just check if both are == 1)
    if(a->instanceCount != b->instanceCount)
        return false;
        
    // Check the size of a list, if it"s too big, don't merge...
    if ((a->len+b->len)>60000)
        return false;
    if ((a->ilen+b->ilen)>60000)
        return false;
    
    return true;
}

void renderlist_createindices(int ilen, GLushort *indices, int count) {
    for (int i = 0; i<ilen; i++) {
        indices[i] = i+count;
    }
}

#define vind(a) (((ind)?ind[(a)]:(a))+count)

void renderlist_lineloop_lines(GLushort *ind, int len, GLushort *indices, int count) {
    int ilen = len*2;  // new size is 2* + return

    if(len>1) {
        int k=0;
        indices[k++] = vind(0);
        indices[k++] = vind(1);
        for (int i = 2; i<len; i++) {
            indices[k++] = vind(i-1);
            indices[k++] = vind(i);
    }
    // go back to initial point
        indices[k++] = indices[(len-1)*2-1];
        indices[k++] = indices[0];
    }
}

void renderlist_linestrip_lines(GLushort *ind, int len, GLushort *indices, int count) {
    int ilen = len*2-2;  // new size is 2*
    if (ilen<1) ilen=0;
    if(len>1) {
        int k=0;
        indices[k++] = vind(0);
        indices[k++] = vind(1);
        for (int i = 2; i<len; i++) {
            indices[k++] = vind(i-1);
            indices[k++] = vind(i);
        }
    }
}

void renderlist_trianglestrip_triangles(GLushort *ind, int len, GLushort *indices, int count) {
    int ilen = (len-2)*3;  
    if (ilen<0) ilen=0;
    for (int i = 2; i<len; i++) {
        indices[(i-2)*3+(i%2)] = vind(i-2);
        indices[(i-2)*3+1-(i%2)] = vind(i-1);
        indices[(i-2)*3+2] = vind(i);
    }
}

void renderlist_trianglefan_triangles(GLushort *ind, int len, GLushort *indices, int count) {
    int ilen = (len-2)*3;  
    if (ilen<0) ilen=0;
    for (int i = 2; i<len; i++) {
        indices[(i-2)*3+0] = vind(0);
        indices[(i-2)*3+1] = vind(i-1);
        indices[(i-2)*3+2] = vind(i);
    }
}

void renderlist_quads_triangles(GLushort *ind, int len, GLushort *indices, int count) {
    // len must be a multiple of 4 !
    len &= ~3;  // discard extra vertex...
    int ilen = len*3/2;
    for (int i=0, j=0; i+3<len; i+=4, j+=6) {
        indices[j+0] = vind(i+0);
        indices[j+1] = vind(i+1);
        indices[j+2] = vind(i+2);

        indices[j+3] = vind(i+0);
        indices[j+4] = vind(i+2);
        indices[j+5] = vind(i+3);
    }
}
#undef vind
#define vind(a) ((ind)?ind[(a)]:(a))
void renderlist_quads2triangles(renderlist_t *a) {
    GLushort *ind = a->indices;
    int len = (ind)? a->ilen:a->len;
    // len must be a multiple of 4 !
    len &= ~3;  // discard extra vertex...
    int ilen = len*3/2;
    if(a->use_glstate) {
        if(ind) {//need to copy first...
            ind = (GLushort*)malloc(len*sizeof(GLushort));
            memcpy(ind, glstate->merger_indices, len*sizeof(GLushort));
            a->shared_indices = NULL;   // should not be needed
        }
        resize_merger_indices(ilen);
        a->indices = glstate->merger_indices;
    } else
        a->indices = (GLushort*)malloc(ilen*sizeof(GLushort));

    for (int i=0, j=0; i+3<len; i+=4, j+=6) {
        a->indices[j+0] = vind(i+0);
        a->indices[j+1] = vind(i+1);
        a->indices[j+2] = vind(i+2);

        a->indices[j+3] = vind(i+0);
        a->indices[j+4] = vind(i+2);
        a->indices[j+5] = vind(i+3);
    }
    a->ilen = ilen;
    if (ind) {
        if (!a->shared_indices || ((*a->shared_indices)--)==0)  {
            free(ind); 
            free(a->shared_indices);
        }
        a->shared_indices = NULL; // unshared list
    }
    a->mode = GL_TRIANGLES;
}
#undef vind

int indices_getindicesize(GLenum mode, int len) {
    int ilen_a;
    switch (mode) {
        case GL_LINE_LOOP:
            ilen_a = len*2;
            if (ilen_a<0) ilen_a=1; // special borked case...
            break;
        case GL_LINE_STRIP:
            ilen_a = (len*2)-2;
            if (ilen_a<0) ilen_a=1; // special borked case...
            break;
        case GL_QUAD_STRIP:
        case GL_TRIANGLE_STRIP:
        case GL_TRIANGLE_FAN:
        case GL_POLYGON:
            ilen_a = (len-2)*3;
            if (ilen_a<0) ilen_a=1; // special borked case...
            break;
        case GL_QUADS:
            ilen_a = ((len&~3)*3)/2;
            break;
        default:
            ilen_a = len;
            break;
    }
    return ilen_a;
}
int renderlist_getindicesize(renderlist_t *a) {
    int ilen_a = indices_getindicesize(a->mode, ((a->indices)? a->ilen:a->len));
    return ilen_a;
}
int mode_needindices(GLenum m) {
    switch(m) {
        case GL_LINE_LOOP:
        case GL_LINE_STRIP:
        case GL_TRIANGLE_FAN:
        case GL_TRIANGLE_STRIP:
        case GL_QUAD_STRIP:
        case GL_POLYGON:
        //case GL_QUADS:  // not needed, but will appens anyway
            return 1;
        default:
            return 0;
    }
}

void list_add_modeinit(renderlist_t* list, GLenum mode) {
    if (list->mode_init_len+1 >= list->mode_init_cap) {
        list->mode_init_cap+=128;
        list->mode_inits = (modeinit_t*)realloc(list->mode_inits, list->mode_init_cap*sizeof(modeinit_t));
    }
    list->mode_inits[list->mode_init_len].mode_init = mode;
    list->mode_inits[list->mode_init_len++].ilen = list->indices?list->ilen:(list->cur_istart?list->cur_istart:list->len);
}

void unshared_renderlist(renderlist_t *a, int cap) {
    if(a->shared_arrays && ((*a->shared_arrays)--)>0) {
        a->cap = cap;
        GLfloat *tmp;
        tmp = a->vert;
        if (tmp) {
            a->vert = alloc_sublist(4, cap);
            memcpy(a->vert, tmp, 4*a->len*sizeof(GLfloat));
        }
        tmp = a->normal;
        if (tmp) {
            a->normal = alloc_sublist(3, cap);
            memcpy(a->normal, tmp, 3*a->len*sizeof(GLfloat));
        }
        tmp = a->color;
        if (tmp) {
            a->color = alloc_sublist(4, cap);
            memcpy(a->color, tmp, 4*a->len*sizeof(GLfloat));
        }
        tmp = a->secondary;
        if (tmp) {
            a->secondary = alloc_sublist(4, cap);
            memcpy(a->secondary, tmp, 4*a->len*sizeof(GLfloat));
        }
        tmp = a->fogcoord;
        if (tmp) {
            a->fogcoord = alloc_sublist(1, cap);
            memcpy(a->fogcoord, tmp, 1*a->len*sizeof(GLfloat));
        }
        for (int i=0; i<a->maxtex; i++) {
            tmp = a->tex[i];
            if (tmp) {
                a->tex[i] = alloc_sublist(4, cap);
                memcpy(a->tex[i], tmp, 4*a->len*sizeof(GLfloat));
            }
        }
    }
    if(a->shared_arrays && (*a->shared_arrays)==0) {
        free(a->shared_arrays); 
        a->shared_arrays=NULL;
    }
}

void unsharedindices_renderlist(renderlist_t* a, int cap)
{
    if (a->shared_indices && ((*a->shared_indices)--)>0) {
        if (a->indices) {
            GLushort* tmpi = a->indices;
            a->indice_cap = cap;
            if (a->indice_cap > 48) a->indice_cap = ((a->indice_cap+512)>>9)<<9;
            a->indices = (GLushort*)malloc(a->indice_cap*sizeof(GLushort));
            memcpy(a->indices, tmpi, a->ilen*sizeof(GLushort));
        }
    } 
    if(a->shared_indices && (*a->shared_indices)==0) {
        free(a->shared_indices); 
        a->shared_indices=0;
    }
}

void redim_renderlist(renderlist_t *a, int cap) {
    if (a->cap < cap) {
        a->cap = cap;
        realloc_sublist(a->vert, 4, cap);
        realloc_sublist(a->normal, 3, cap);
        realloc_sublist(a->color, 4, cap);
        realloc_sublist(a->secondary, 4, cap);
        realloc_sublist(a->fogcoord, 1, cap);
        for (int i=0; i<a->maxtex; i++)
            realloc_sublist(a->tex[i], 4, cap);
    }
}

void prepareadd_renderlist(renderlist_t* a, int size_to_add)
{
    // alloc or realloc a->indices first...
    int capindices = renderlist_getindicesize(a)+size_to_add;
    if (capindices > 48) capindices = ((capindices+512)>>9)<<9;
    #define alloc_a_indices                                      \
    newind=(GLushort*)malloc(capindices*sizeof(GLushort))
    #define copy_a_indices                                       \
    if (a->indices) free(a->indices);                            \
    a->indices = newind;                                         \
    a->indice_cap = capindices
    // check if "a" needs to be converted
    int ilen_a = renderlist_getindicesize(a);
    GLushort *newind=NULL;
    switch (a->mode) {
        case GL_LINE_LOOP:
            alloc_a_indices;
            renderlist_lineloop_lines(a->indices, a->indices?a->ilen:a->len, newind, 0);
            a->mode = GL_LINES;
            copy_a_indices;
            break;
        case GL_LINE_STRIP:
            alloc_a_indices;
            renderlist_linestrip_lines(a->indices, a->indices?a->ilen:a->len, newind, 0);
            a->mode = GL_LINES;
            copy_a_indices;
            break;
        case GL_QUAD_STRIP:
        case GL_TRIANGLE_STRIP:
            alloc_a_indices;
            renderlist_trianglestrip_triangles(a->indices, a->indices?a->ilen:a->len, newind, 0);
            a->mode = GL_TRIANGLES;
            copy_a_indices;
            break;
        case GL_TRIANGLE_FAN:
        case GL_POLYGON:
            alloc_a_indices;
            renderlist_trianglefan_triangles(a->indices, a->indices?a->ilen:a->len, newind, 0);
            a->mode = GL_TRIANGLES;
            copy_a_indices;
            break;
        case GL_QUADS:
            alloc_a_indices;
            renderlist_quads_triangles(a->indices, a->indices?a->ilen:a->len, newind, 0);
            a->mode = GL_TRIANGLES;
            copy_a_indices;
            break;
        default:
            if (!a->indices) {
                // no a->indices, must alloc and fill one
                alloc_a_indices;
                renderlist_createindices(a->len, newind, 0);
                copy_a_indices;
            } else {
                // a->indices already exist, just check if need to adjust its size
                if (a->indice_cap < capindices) {
                    a->indices = (GLushort*)realloc(a->indices, capindices*sizeof(GLushort));
                    a->indice_cap = capindices;
                }
            }
            break;
    }
    #undef copy_a_indices
    #undef alloc_a_indices

    a->ilen = ilen_a;
}

void doadd_renderlist(renderlist_t* a, GLenum mode, GLushort* indices, int count, int size_to_add)
{
    // then append b
    switch (mode) {
        case GL_LINE_LOOP:
            renderlist_lineloop_lines(indices, count, a->indices + a->ilen, a->len);
            break;
        case GL_LINE_STRIP:
            renderlist_linestrip_lines(indices, count, a->indices + a->ilen, a->len);
            break;
        case GL_QUAD_STRIP:
        case GL_TRIANGLE_STRIP:
            renderlist_trianglestrip_triangles(indices, count, a->indices + a->ilen, a->len);
            break;
        case GL_POLYGON:
        case GL_TRIANGLE_FAN:
            renderlist_trianglefan_triangles(indices, count, a->indices + a->ilen, a->len);
            break;
        case GL_QUADS:
            renderlist_quads_triangles(indices, count, a->indices + a->ilen, a->len);
            break;
        default:
            // no transform here, just take (or create) the indice list as-is
            if (!indices) {
                // append a newly created indice list
                renderlist_createindices(count, a->indices + a->ilen, a->len);
            } else {
                // append existing one
                GLushort* newind = a->indices+a->ilen;
                for(int i=0; i<count; i++)
                    newind[i] = indices[i]+a->len;
            }
            break;
    }
    a->ilen += size_to_add;
}

void append_renderlist(renderlist_t *a, renderlist_t *b) {
    // append all draw elements of b in a
    // check the final indice size of a and b
    int ilen_a = a->ilen;
    int ilen_b = b->ilen;
    // lets append all the arrays
    unsigned long cap = a->cap;
    if (a->len + b->len >= cap) cap += b->cap + DEFAULT_RENDER_LIST_CAPACITY;
    // Unshare if shared (shared array are not used for now)
    unshared_renderlist(a, cap);
    redim_renderlist(a, cap);
    unsharedindices_renderlist(a, ((ilen_a)?ilen_a:a->len) + ((ilen_b)?ilen_b:b->len));
    // append arrays
    if (a->vert) memcpy(a->vert+a->len*4, b->vert, b->len*4*sizeof(GLfloat));
    if (a->normal) memcpy(a->normal+a->len*3, b->normal, b->len*3*sizeof(GLfloat));
    if (a->color) memcpy(a->color+a->len*4, b->color, b->len*4*sizeof(GLfloat));
    if (a->secondary) memcpy(a->secondary+a->len*4, b->secondary, b->len*4*sizeof(GLfloat));
    if (a->fogcoord) memcpy(a->fogcoord+a->len*1, b->fogcoord, b->len*1*sizeof(GLfloat));
    for (int i=0; i<a->maxtex; i++)
        if (a->tex[i]) memcpy(a->tex[i]+a->len*4, b->tex[i], b->len*4*sizeof(GLfloat));
    // indices
    if(!a->mode_inits) list_add_modeinit(a, a->mode_init);
    if (ilen_a || ilen_b || mode_needindices(a->mode) || mode_needindices(b->mode) 
        || (a->mode!=b->mode && (a->mode==GL_QUADS || b->mode==GL_QUADS)) )
    {
        // alloc or realloc a->indices first...
        ilen_b = renderlist_getindicesize(b);
        prepareadd_renderlist(a, ilen_b);
        // then append b
        doadd_renderlist(a, b->mode, b->indices, b->indices?b->ilen:b->len, ilen_b);
    }
    // lenghts
    a->len += b->len;
    if(a->mode_inits) list_add_modeinit(a, b->mode);
    // copy the lastColors if needed
    if(b->lastColorsSet) {
        a->lastColorsSet = 1;
        memcpy(a->lastColors, b->lastColors, 4*sizeof(GLfloat));
    }
    if(b->post_color) {
        a->post_color = 1;
        memcpy(a->post_colors, b->post_colors, 4*sizeof(GLfloat));
    }
    if(b->post_normal) {
        a->post_normal = 1;
        memcpy(a->post_normals, b->post_normals, 3*sizeof(GLfloat));
    }
    //all done
    a->stage = STAGE_DRAW;  // just in case
    return;
}
void adjust_renderlist(renderlist_t *list);

renderlist_t *extend_renderlist(renderlist_t *list) {
    if ((list->prev!=NULL) && ispurerender_renderlist(list) && islistscompatible_renderlist(list->prev, list)) {
        // append list!
        append_renderlist(list->prev, list);
        renderlist_t *new = alloc_renderlist();
        new->prev = list->prev;
        list->prev->next = new;
        // just in case
        memcpy(new->lastNormal, list->lastNormal, 3*sizeof(GLfloat));
        memcpy(new->lastSecondaryColors, list->lastSecondaryColors, 3*sizeof(GLfloat));
        memcpy(new->lastColors, list->lastColors, 4*sizeof(GLfloat));
        new->lastColorsSet = list->lastColorsSet;
        // detach
        list->prev = NULL;
        // free list now
        free_renderlist(list);
        return new;
    } else {
        renderlist_t *new = alloc_renderlist();
        list->next = new;
        new->prev = list;
        new->tmu = list->tmu;
        // copy local state
        memcpy(new->lastNormal, list->lastNormal, 3*sizeof(GLfloat));
        memcpy(new->lastSecondaryColors, list->lastSecondaryColors, 3*sizeof(GLfloat));
        memcpy(new->lastColors, (list->post_color)?list->post_colors:list->lastColors, 4*sizeof(GLfloat));
        new->lastColorsSet = (list->post_color || list->lastColorsSet)?1:0;
        return new;
    }
}

renderlist_t* append_calllist(renderlist_t *list, renderlist_t *a)
{
    // go to end of list
    while(list->next) list = list->next;
    while(a) {
        if(ispurerender_renderlist(a) && islistscompatible_renderlist(list, a)) {
            // append list!
            append_renderlist(list, a);
        } else {
            // create a new appended list
            renderlist_t *new = alloc_renderlist();
            // prepared shared stuff...
            if(a->len && !a->shared_arrays) {
                a->shared_arrays = (int*)malloc(sizeof(int));
                *a->shared_arrays = 0;
            }
            if(a->ilen && !a->shared_indices) {
                a->shared_indices = (int*)malloc(sizeof(int));
                *a->shared_indices = 0;
            }
            if(a->calls.len && !a->shared_calls) {
                a->shared_calls = (int*)malloc(sizeof(int));
                *a->shared_calls = 0;
            }
            // batch copy first
            memcpy(new, a, sizeof(renderlist_t));
            list->next = new;
            new->prev = list;
            // ok, now on new list
            list = new;
            // copy the many list arrays
            if (list->calls.len > 0) {
                ++(*list->shared_calls);
                /*
                list->calls.calls = (packed_call_t**)malloc(sizeof(packed_call_t*)*a->calls.cap);
                for (int i = 0; i < list->calls.len; i++) {
                    list->calls.calls[i] = glCopyPackedCall(a->calls.calls[i]);
                }*/
            }
            if(list->len) {
                ++(*list->shared_arrays);
            }
            if(list->ilen) {
                ++(*list->shared_indices);
            }
            #define PROCESS(W, T, C) if(list->W) { \
                    list->W = kh_init(W);   \
                    T *m, *m2;      \
                    khint_t k;      \
                    int ret;        \
                    kh_foreach_value(a->W, m,   \
                        k = kh_put(W, list->W, C, &ret);    \
                        m2= kh_value(list->W, k) = malloc(sizeof(T));   \
                        memcpy(m2, m, sizeof(T));           \
                    );       \
                }
            PROCESS(material, rendermaterial_t, m->pname);
            PROCESS(light, renderlight_t, m->pname | ((m->which-GL_LIGHT0)<<16));
            PROCESS(texgen, rendertexgen_t, m->pname | ((m->coord-GL_S)<<16));
            PROCESS(texenv, rendertexenv_t, m->pname | ((m->target)<<16));
            #undef PROCESS
            if (list->lightmodel) {
                list->lightmodel = (GLfloat*)malloc(4*sizeof(GLfloat));
                memcpy(list->lightmodel, a->lightmodel, 4*sizeof(GLfloat));
            }
            if (list->raster) {
                (*list->raster->shared)++;
            }
            if (list->bitmaps) {
                (*list->bitmaps->shared)++;
            }
        }
        a = a->next;
    }
    return list;
}

void free_renderlist(renderlist_t *list) {
	// test if list is NULL
	if (list == NULL)
		return;
    // we want the first list in the chain
    while (list->prev)
        list = list->prev;

    renderlist_t *next;
    do {
        if(list->mode_inits)
            free(list->mode_inits);
        if ((list->calls.len > 0) && (!list->shared_calls || ((*list->shared_calls)--)==0)) {
            if(list->shared_calls) free(list->shared_calls);
            for (int i = 0; i < list->calls.len; i++) {
                free(list->calls.calls[i]);
            }
            free(list->calls.calls);
        }
        int a;
        if(!list->use_glstate) {
            if (!list->shared_arrays || ((*list->shared_arrays)--)==0) {
                if (list->shared_arrays) free(list->shared_arrays);
                if (list->vert) free(list->vert);
                if (list->normal) free(list->normal);
                if (list->color) free(list->color);
                if (list->secondary) free(list->secondary);
                if (list->fogcoord) free(list->fogcoord);
                for (a=0; a<list->maxtex; a++)
                    if (list->tex[a]) free(list->tex[a]);
            }
            if (!list->shared_indices || ((*list->shared_indices)--)==0) {
                if (list->shared_indices) free(list->shared_indices);
                if (list->indices)
                    free(list->indices);
            }
        } else
            glstate->merger_used = 0;

        if (list->material) {
            rendermaterial_t *m;
            kh_foreach_value(list->material, m,
                free(m);
            )
            kh_destroy(material, list->material);
        }
        if (list->light) {
            renderlight_t *m;
            kh_foreach_value(list->light, m,
                free(m);
            )
            kh_destroy(light, list->light);
        }
        if (list->texgen) {
            rendertexgen_t *m;
            kh_foreach_value(list->texgen, m,
                free(m);
            )
            kh_destroy(texgen, list->texgen);
        }
        if (list->texenv) {
            rendertexenv_t *m;
            kh_foreach_value(list->texenv, m,
                free(m);
            )
            kh_destroy(texenv, list->texenv);
        }
        if (list->lightmodel)
			free(list->lightmodel);
			
        if (list->raster && !((*list->raster->shared)--)) {
			if (list->raster->texture)
				gl4es_glDeleteTextures(1, &list->raster->texture);
            free(list->raster->shared);
			free(list->raster);
		}

        if(list->bitmaps && !((*list->bitmaps->shared)--)) {
            for(int i=0; i<list->bitmaps->count; i++)
                if(list->bitmaps->list[i].bitmap)
                    free(list->bitmaps->list[i].bitmap);
            free(list->bitmaps->list);
            free(list->bitmaps->shared);
            free(list->bitmaps);
        }
        
        if(list->ind_lines)
            free(list->ind_lines);
        if(list->final_colors)
            free(list->final_colors);
        if(list->vbo_array)
            deleteSingleBuffer(list->vbo_array);
        if(list->vbo_indices)
            deleteSingleBuffer(list->vbo_indices);

        next = list->next;
        free(list);
    } while ((list = next));
}

void resize_renderlist(renderlist_t *list) {
    if (list->use_glstate) {
        if (list->len >= glstate->merger_cap) {
            glstate->merger_cap += DEFAULT_RENDER_LIST_CAPACITY*8;
            realloc_merger_sublist(glstate->merger_master, 4*5, glstate->merger_cap);
            realloc_sublist(glstate->merger_secondary, 4, glstate->merger_cap);
            for (int a=2; a<list->maxtex; a++)
                realloc_sublist(glstate->merger_tex[a-2], 4, glstate->merger_cap);
            if(list->vert) list->vert = glstate->merger_master;
            if(list->normal) list->normal = glstate->merger_master+4+4+2*4;
            if(list->color) list->color = glstate->merger_master+4;
            if(list->tex[0]) list->tex[0] = glstate->merger_master+4+4;
            if(list->tex[1]) list->tex[1] = glstate->merger_master+4+4+4;
            if(list->fogcoord) list->fogcoord = glstate->merger_master+4+4+2*4+3;
            if(list->secondary) list->secondary = glstate->merger_secondary;
            for (int a=2; a<list->maxtex; a++)
                if(list->tex[a]) list->tex[a] = glstate->merger_tex[a-2];
        }
    } else {
        if (list->len >= list->cap) {
            list->cap += DEFAULT_RENDER_LIST_CAPACITY*8;
            realloc_sublist(list->vert, 4, list->cap);
            realloc_sublist(list->normal, 3, list->cap);
            realloc_sublist(list->color, 4, list->cap);
            realloc_sublist(list->secondary, 4, list->cap);
            realloc_sublist(list->fogcoord, 1, list->cap);
            for (int a=0; a<list->maxtex; a++)
            realloc_sublist(list->tex[a], 4, list->cap);
        }
    }
}

void resize_merger_indices(int cap) {
    if(cap<glstate->merger_indice_cap)
        return;
    glstate->merger_indice_cap = ((glstate->merger_indice_cap+cap+512)>>9)<<9;
    glstate->merger_indices = (GLushort*)realloc(glstate->merger_indices, glstate->merger_indice_cap*sizeof(GLushort));
}

void resize_indices_renderlist(renderlist_t *list, int n) {
    if (!list->indices || list->shared_indices)
        return;
    if (list->use_glstate) {
        resize_merger_indices(list->ilen+n);
        list->indices = glstate->merger_indices;
    } else {
        if(list->ilen+n<list->indice_cap)
            return;
        list->indice_cap = ((list->indice_cap+n+511)>>9)<<9;
        list->indices = (GLushort*)realloc(list->indices, list->indice_cap*sizeof(GLushort));
    }
}

void adjust_renderlist(renderlist_t *list) {
    if (! list->open)
        return;

    list->stage = STAGE_LAST;
    list->open = false;
    if(hardext.esversion==1)    // no more adjustment needed for ES2
    for (int a=0; a<list->maxtex; a++) {
        const GLint itarget = get_target(glstate->enable.texture[a]);
	    gltexture_t *bound = (itarget>=0)?glstate->texture.bound[a][itarget]:NULL;
        // in case of Texture bounding inside a list
        if (list->set_texture && (list->tmu == a))
            bound = gl4es_getTexture(list->target_texture, list->texture);
	    // GL_ARB_texture_rectangle
        if ((list->tex[a]) && (itarget == ENABLED_TEXTURE_RECTANGLE) && (bound)) {
            tex_coord_rect_arb(list->tex[a], list->tex_stride[a]>>2, list->len, bound->width, bound->height);
        }
    }
}

renderlist_t* end_renderlist(renderlist_t *list) {
    if (!list || ! list->open)
        return list;

    adjust_renderlist(list);
    
    switch (list->mode) {
        case GL_QUADS:
			if (((list->indices) && (list->ilen==4)) || ((list->indices==NULL) && (list->len==4))) {
				list->mode = GL_TRIANGLE_FAN;
			} else {
                renderlist_quads2triangles(list);
			}
            break;
        case GL_POLYGON:
            list->mode = GL_TRIANGLE_FAN;
            break;
        case GL_QUAD_STRIP:
            list->mode = GL_TRIANGLE_STRIP;
            break;
    }
    if(list->prev && isempty_renderlist(list)) {
        renderlist_t *p = list;
        list = list->prev;
        list->next = NULL;
        p->prev = NULL;
        free_renderlist(p);
    }
    
    return list;
}

renderlist_t* recycle_renderlist(renderlist_t *list, GLenum mode) {
    if(isempty_renderlist(list) || (ispurerender_renderlist(list) && list->len==0)) {
        list->mode_init = mode;
        list->mode = mode;
        list->stage=STAGE_DRAW;
        if (list->post_color) {
            list->post_color = 0;
            rlColor4f(list, list->post_colors[0], list->post_colors[1], list->post_colors[2], list->post_colors[3]);
        }
        if (list->post_normal) {
            list->post_normal = 0;
            rlNormal3f(list, list->post_normals[0], list->post_normals[1], list->post_normals[2]);
        }
        return list;    // recycling...
    }
    // check if pending color...
    if (list->post_color) {
        list->post_color = 0;
        rlColor4f(list, list->post_colors[0], list->post_colors[1], list->post_colors[2], list->post_colors[3]);
    }
    if (list->post_normal) {
        list->post_normal = 0;
        rlNormal3f(list, list->post_normals[0], list->post_normals[1], list->post_normals[2]);
    }
    // check if list needs to be converted to triangles / lines...
    GLushort *indices = NULL;
#define pre_expand   \
            if(list->use_glstate) {\
                resize_merger_indices(renderlist_getindicesize(list)); indices = glstate->merger_indices;\
            } else {\
                list->indice_cap = renderlist_getindicesize(list);\
                indices = (GLushort*)malloc(sizeof(GLushort)*list->indice_cap);\
            }
#define post_expand  \
            list->ilen = renderlist_getindicesize(list);\
            list->indices = indices

    list->merger_mode = mode;
    switch(list->mode) {
        case GL_LINES:
            if(mode!=GL_LINES && !list->indices) {
                pre_expand;
                renderlist_createindices(list->len, indices, 0);
                post_expand;
            }
            break;
        case GL_LINE_LOOP:
            pre_expand;
            renderlist_lineloop_lines(list->indices, list->indices?list->ilen:list->len, indices, 0);
            post_expand;
            list->mode=GL_LINES;
            break;
        case GL_LINE_STRIP:
            pre_expand;
            renderlist_linestrip_lines(list->indices, list->indices?list->ilen:list->len, indices, 0);
            post_expand;
            list->mode=GL_LINES;
            break;
        case GL_TRIANGLES:
            if(mode!=GL_TRIANGLES && !list->indices) {
                pre_expand;
                renderlist_createindices(list->len, indices, 0);
                post_expand;
            }
            break;
        case GL_POLYGON:
        case GL_TRIANGLE_FAN:
            pre_expand;
            renderlist_trianglefan_triangles(list->indices, list->indices?list->ilen:list->len, indices, 0);
            post_expand;
            list->mode=GL_TRIANGLES;
            break;
        case GL_QUAD_STRIP:
        case GL_TRIANGLE_STRIP:
            pre_expand;
            renderlist_trianglestrip_triangles(list->indices, list->indices?list->ilen:list->len, indices, 0);
            post_expand;
            list->mode=GL_TRIANGLES;
            break;
        case GL_QUADS:
            pre_expand;
            renderlist_quads_triangles(list->indices, list->indices?list->ilen:list->len, indices, 0);
            post_expand;
            list->mode=GL_TRIANGLES;
            break;
    }
#undef pre_expand
#undef post_expand
    list->cur_istart = list->len;
    // All done
    list->stage=STAGE_DRAW;

    return list;
}
