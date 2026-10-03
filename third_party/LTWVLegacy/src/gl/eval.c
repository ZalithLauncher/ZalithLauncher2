// TODO: glIsEnabled(), glGetMap()
// TODO: GL_AUTO_NORMAL

#include "eval.h"

#include "math/eval.h"
#include "wrap/gl4es.h"
#include "array.h"
#include "logs.h"
#include "matvec.h"

static inline map_state_t **get_map_pointer(GLenum target) {
    switch (target) {
        case GL_MAP1_COLOR_4:         return &glstate->map1.color4;
        case GL_MAP1_INDEX:           return &glstate->map1.index;
        case GL_MAP1_TEXTURE_COORD_1: return &glstate->map1.texture1;
        case GL_MAP1_TEXTURE_COORD_2: return &glstate->map1.texture2;
        case GL_MAP1_TEXTURE_COORD_3: return &glstate->map1.texture3;
        case GL_MAP1_TEXTURE_COORD_4: return &glstate->map1.texture4;
        case GL_MAP1_VERTEX_3:        return &glstate->map1.vertex3;
        case GL_MAP1_VERTEX_4:        return &glstate->map1.vertex4;
        case GL_MAP2_COLOR_4:         return &glstate->map2.color4;
        case GL_MAP2_INDEX:           return &glstate->map2.index;
        case GL_MAP2_TEXTURE_COORD_1: return &glstate->map2.texture1;
        case GL_MAP2_TEXTURE_COORD_2: return &glstate->map2.texture2;
        case GL_MAP2_TEXTURE_COORD_3: return &glstate->map2.texture3;
        case GL_MAP2_TEXTURE_COORD_4: return &glstate->map2.texture4;
        case GL_MAP2_VERTEX_3:        return &glstate->map2.vertex3;
        case GL_MAP2_VERTEX_4:        return &glstate->map2.vertex4;
        default:
            LOGE("unknown glMap target 0x%x\n", target);
    }
    return NULL;
}

#define set_map_coords(n)         \
    map->n._1 = n##1;             \
    map->n._2 = n##2;             \
    map->n.d = 1.0/(n##2 - n##1); \
    map->n.order = n##order;

#define case_state(dims, magic, name)                           \
    case magic: {                                               \
        map->width = get_map_width(magic);                      \
        map_statef_t *m = (map_statef_t *)glstate->map##dims.name; \
        if (m) {                                                \
            free((void *)m->points);                            \
            free(m);                                            \
        }                                                       \
        glstate->map##dims.name = (map_state_t *)map;           \
        break;                                                  \
    }

#define map_switch(dims)                                                \
    switch (target) {                                                   \
        case_state(dims, GL_MAP##dims##_COLOR_4, color4);               \
        case_state(dims, GL_MAP##dims##_INDEX, index);                  \
        case_state(dims, GL_MAP##dims##_NORMAL, normal);                \
        case_state(dims, GL_MAP##dims##_TEXTURE_COORD_1, texture1);     \
        case_state(dims, GL_MAP##dims##_TEXTURE_COORD_2, texture2);     \
        case_state(dims, GL_MAP##dims##_TEXTURE_COORD_3, texture3);     \
        case_state(dims, GL_MAP##dims##_TEXTURE_COORD_4, texture4);     \
        case_state(dims, GL_MAP##dims##_VERTEX_3, vertex3);             \
        case_state(dims, GL_MAP##dims##_VERTEX_4, vertex4);             \
    }

void APIENTRY_GL4ES gl4es_glMap1d(GLenum target, GLdouble u1, GLdouble u2,
             GLint ustride, GLint uorder, const GLdouble *points) {
    noerrorShim();
    map_statef_t *map = malloc(sizeof(map_statef_t));
    map->type = GL_FLOAT; map->dims = 1;
    set_map_coords(u);
    map_switch(1);
    map->points = copy_eval_double1(target, ustride, uorder, points);
}

void APIENTRY_GL4ES gl4es_glMap1f(GLenum target, GLfloat u1, GLfloat u2,
             GLint ustride, GLint uorder, const GLfloat *points) {
    noerrorShim();
    map_statef_t *map = malloc(sizeof(map_statef_t));
    map->type = GL_FLOAT; map->dims = 1;
    set_map_coords(u);
    map_switch(1);
    map->points = copy_eval_float1(target, ustride, uorder, points);
}

void APIENTRY_GL4ES gl4es_glMap2d(GLenum target, GLdouble u1, GLdouble u2,
             GLint ustride, GLint uorder, GLdouble v1, GLdouble v2,
             GLint vstride, GLint vorder, const GLdouble *points) {
    noerrorShim();
    map_statef_t *map = malloc(sizeof(map_statef_t));
    map->type = GL_FLOAT; map->dims = 2;
    set_map_coords(u);
    set_map_coords(v);
    map_switch(2);
    map->points = copy_eval_double2(target, ustride, uorder, vstride, vorder, points);
}

void APIENTRY_GL4ES gl4es_glMap2f(GLenum target, GLfloat u1, GLfloat u2,
             GLint ustride, GLint uorder, GLfloat v1, GLfloat v2,
             GLint vstride, GLint vorder, const GLfloat *points) {
    noerrorShim();
    map_statef_t *map = malloc(sizeof(map_statef_t));
    map->type = GL_FLOAT; map->dims = 2;
    set_map_coords(u);
    set_map_coords(v);
    map_switch(2);
    map->points = copy_eval_float2(target, ustride, uorder, vstride, vorder, points);;
}

#undef set_map_coords
#undef case_state
#undef map_switch

#define p_map(d, name, func, code)                    \
    if (glstate->map##d.name && glstate->enable.map##d##_##name) {    \
        map_state_t *_map = glstate->map##d.name;         \
        if (_map->type == GL_DOUBLE) {                \
            map_stated_t *map = (map_stated_t *)_map; \
            LOGE("double: not implemented\n");      \
        } else if (_map->type == GL_FLOAT) {          \
            map_statef_t *map = (map_statef_t *)_map; \
            code                                      \
            gl4es_##func##v(out);                     \
        }                                             \
    }

#define iter_maps(d, code)                             \
    p_map(d, color4, glColor4f, code);                 \
    p_map(d, index, glIndexf, code);                   \
    if(!glstate->enable.auto_normal)                   \
    p_map(d, normal, glNormal3f, code);                \
    p_map(d, texture4, glTexCoord4f, code)             \
    else                                               \
    p_map(d, texture3, glTexCoord3f, code)             \
    else                                               \
    p_map(d, texture2, glTexCoord2f, code)             \
    else                                               \
    p_map(d, texture1, glTexCoord1f, code);            \
    p_map(d, vertex4, glVertex4f, code)                \
    else                                               \
    p_map(d, vertex3, glVertex3f, code);

void APIENTRY_GL4ES gl4es_glEvalCoord1f(GLfloat u) {
    noerrorShim();
    GLfloat out[4];                           \
    iter_maps(1,
        GLfloat uu = (u - map->u._1) * map->u.d;
        _math_horner_bezier_curve((GLfloat*)map->points, out, uu, map->width, map->u.order);
    )
}

void APIENTRY_GL4ES gl4es_glEvalCoord2f(GLfloat u, GLfloat v) {
    noerrorShim();
    GLfloat out[4];                           \
    iter_maps(2,
        GLfloat uu = (u - map->u._1) * map->u.d;
        GLfloat vv = (v - map->v._1) * map->v.d;
        if(glstate->enable.auto_normal && (map == (map_statef_t *)glstate->map2.vertex3 || map == (map_statef_t *)glstate->map2.vertex4)) {
            GLfloat norm[3];
            GLfloat du[4];
            GLfloat dv[4];
            memset(out, 0, 3*sizeof(GLfloat)); out[3] = 1.0f;
            _math_de_casteljau_surf((GLfloat*)map->points, out, du, dv, uu, vv,
                                    map->width, map->u.order, map->v.order);
            if(map->width == 4) {
                du[0] = du[0]*out[3] - du[3]*out[0];
                du[1] = du[1]*out[3] - du[3]*out[1];
                du[2] = du[2]*out[3] - du[3]*out[2];
                dv[0] = dv[0]*out[3] - dv[3]*out[0];
                dv[1] = dv[1]*out[3] - dv[3]*out[1];
                dv[2] = dv[2]*out[3] - dv[3]*out[2];
            }
            cross3(du, dv, norm);
            vector_normalize(norm);
            gl4es_glNormal3fv(norm);
        } else
            _math_horner_bezier_surf((GLfloat*)map->points, out, uu, vv,
                                     map->width, map->u.order, map->v.order);
    )
}

#undef p_map
#undef iter_maps

void APIENTRY_GL4ES gl4es_glMapGrid1f(GLint un, GLfloat u1, GLfloat u2) {
    if(un<1) {
        errorShim(GL_INVALID_VALUE);
        return;
    }
    if(glstate->list.begin) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }

    noerrorShim();

   glstate->map_grid[0].n = un;
   glstate->map_grid[0]._1 = u1;
   glstate->map_grid[0]._2 = u2;
}

void APIENTRY_GL4ES gl4es_glMapGrid2f(GLint un, GLfloat u1, GLfloat u2,
                 GLint vn, GLfloat v1, GLfloat v2) {

     if((un<1) || (vn<1)) {
         errorShim(GL_INVALID_VALUE);
         return;
     }
     if(glstate->list.begin) {
         errorShim(GL_INVALID_OPERATION);
         return;
     }

     noerrorShim();

    glstate->map_grid[0].n = un;
    glstate->map_grid[0]._1 = u1;
    glstate->map_grid[0]._2 = u2;
    glstate->map_grid[0].d = (glstate->map_grid[0]._2 - glstate->map_grid[0]._1)/glstate->map_grid[0].n;
    glstate->map_grid[1].n = vn;
    glstate->map_grid[1]._1 = v1;
    glstate->map_grid[1]._2 = v2;
    glstate->map_grid[1].d = (glstate->map_grid[1]._2 - glstate->map_grid[1]._1)/glstate->map_grid[1].n;
}

static inline GLenum eval_mesh_prep(GLenum mode) {
    if ((!glstate->map2.vertex4) && (!glstate->map2.vertex3)) {
        return 0;
    }

    switch (mode) {
        case GL_POINT: return GL_POINTS;
        case GL_LINE: return GL_LINE_STRIP;
        case GL_FILL: return GL_TRIANGLE_STRIP;
        case 0: return 1;
        default:
            LOGE("unknown glEvalMesh mode: %x\n", mode);
            return 0;
    }
}

void APIENTRY_GL4ES gl4es_glEvalMesh1(GLenum mode, GLint i1, GLint i2) {
    GLenum renderMode = eval_mesh_prep(mode);
    if (! renderMode) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    
    noerrorShim();
    GLfloat u, du, u1;
    du = glstate->map_grid[0].d;
    u1 = glstate->map_grid[0]._1 + du*i1;
    GLint i;
    gl4es_glBegin(renderMode);
    for (u = u1, i = i1; i <= i2; i++, u += du) {
        gl4es_glEvalCoord1f(u);
    }
    gl4es_glEnd();
}

void APIENTRY_GL4ES gl4es_glEvalMesh2(GLenum mode, GLint i1, GLint i2, GLint j1, GLint j2) {
    GLenum renderMode = eval_mesh_prep(mode);
    if (! renderMode) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    
    noerrorShim();
    GLfloat u, du, u1, v, dv, v1;
    du = glstate->map_grid[0].d;
    dv = glstate->map_grid[1].d;
    u1 = glstate->map_grid[0]._1 + du*i1;
    v1 = glstate->map_grid[1]._1 + dv*j1;
    GLint i, j;
    if(mode==GL_FILL) {
        for (v = v1, j = j1; j <= j2-1; j++, v += dv) {
            gl4es_glBegin(renderMode);
            for (u = u1, i = i1; i <= i2; i++, u += du) {
                gl4es_glEvalCoord2f(u, v);
                gl4es_glEvalCoord2f(u, v + dv);
            }
            gl4es_glEnd();
        }
    } else {
        for (v = v1, j = j1; j <= j2; j++, v += dv) {
            gl4es_glBegin(renderMode);
            for (u = u1, i = i1; i <= i2; i++, u += du) {
                gl4es_glEvalCoord2f(u, v);
            }
            gl4es_glEnd();
        }
        if (mode == GL_LINE) {
            gl4es_glBegin(renderMode);
            for (u = u1, i = i1; i <= i2; i++, u += du) {
                for (v = v1, j = j1; j <= j2; j++, v += dv) {
                    gl4es_glEvalCoord2f(u, v);
                }
            }
            gl4es_glEnd();
        }
    }
}

void APIENTRY_GL4ES gl4es_glEvalPoint1(GLint i) {
    gl4es_glEvalCoord1f(glstate->map_grid[0]._1 + glstate->map_grid[0].d*i);
}

void APIENTRY_GL4ES gl4es_glEvalPoint2(GLint i, GLint j) {
    gl4es_glEvalCoord2f(glstate->map_grid[0]._1 + glstate->map_grid[0].d*i, glstate->map_grid[1]._1 + glstate->map_grid[1].d*j);
}

#define GL_GET_MAP(t, type)                                        \
void APIENTRY_GL4ES gl4es_glGetMap##t##v(GLenum target, GLenum query, type *v) { \
    noerrorShim();                                                 \
    map_statef_t *map = *(map_statef_t **)get_map_pointer(target); \
    if (map) {                                                     \
        switch (query) {                                           \
            case GL_COEFF: {                                       \
                const GLfloat *points = map->points;               \
                for (int i = 0; i < map->u.order; i++) {           \
                    if (map->dims == 2) {                          \
                        for (int j = 0; j < map->v.order; j++) {   \
                            *v++ = *points++;                      \
                        }                                          \
                    } else {                                       \
                        *v++ = *points++;                          \
                    }                                              \
                }                                                  \
                return;                                            \
            }                                                      \
            case GL_ORDER:                                         \
                *v++ = map->u.order;                               \
                if (map->dims == 2)                                \
                    *v++ = map->v.order;                           \
                return;                                            \
            case GL_DOMAIN:                                        \
                *v++ = map->u._1;                                  \
                *v++ = map->u._2;                                  \
                if (map->dims == 2) {                              \
                    *v++ = map->u._1;                              \
                    *v++ = map->u._2;                              \
                }                                                  \
                return;                                            \
        }                                                          \
    }                                                              \
}

GL_GET_MAP(i, GLint)
GL_GET_MAP(f, GLfloat)
GL_GET_MAP(d, GLdouble)

#undef GL_GET_MAP

//Direct wrapper
AliasExport_M(void,glMap1d,,(GLenum target, GLdouble u1, GLdouble u2, GLint stride, GLint order, const GLdouble *points),32);
AliasExport(void,glMap1f,,(GLenum target, GLfloat u1, GLfloat u2, GLint stride, GLint order, const GLfloat *points));
AliasExport_M(void,glMap2d,,(GLenum target, GLdouble u1, GLdouble u2, GLint ustride, GLint uorder, GLdouble v1, GLdouble v2, GLint vstride, GLint vorder, const GLdouble *points),56);
AliasExport(void,glMap2f,,(GLenum target, GLfloat u1, GLfloat u2, GLint ustride, GLint uorder, GLfloat v1, GLfloat v2, GLint vstride, GLint vorder, const GLfloat *points));
AliasExport(void,glEvalCoord1f,,(GLfloat u));
AliasExport(void,glEvalCoord2f,,(GLfloat u, GLfloat v));
AliasExport(void,glEvalMesh1,,(GLenum mode, GLint i1, GLint i2));
AliasExport(void,glEvalMesh2,,(GLenum mode, GLint i1, GLint i2, GLint j1, GLint j2));
AliasExport(void,glEvalPoint1,,(GLint i));
AliasExport(void,glEvalPoint2,,(GLint i, GLint j));
AliasExport(void,glMapGrid1f,,(GLint un, GLfloat u1, GLfloat u2));
AliasExport(void,glMapGrid2f,,(GLint un, GLfloat u1, GLfloat u2, GLint vn, GLfloat v1, GLfloat v2));
AliasExport(void,glGetMapdv,,(GLenum target, GLenum query, GLdouble *v));
AliasExport(void,glGetMapfv,,(GLenum target, GLenum query, GLfloat *v));
AliasExport(void,glGetMapiv,,(GLenum target, GLenum query, GLint *v));
