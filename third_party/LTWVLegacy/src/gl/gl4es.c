#include "gl4es.h"

#if defined(AMIGAOS4) || (defined(NOX11) && defined(NOEGL) && !defined(_WIN32))
#include <sys/time.h>
#endif // defined(AMIGAOS4) || (defined(NOX11) && defined(NOEGL)

#include "../config.h"
#include "../glx/hardext.h"
#include "wrap/gl4es.h"
#include "array.h"
#include "debug.h"
#include "enum_info.h"
#include "fpe.h"
#include "framebuffers.h"
#include "glstate.h"
#include "init.h"
#include "loader.h"
#include "matrix.h"
#include "buffers.h"
#ifdef _WIN32
#ifdef _WINBASE_
#define GSM_CAST(c) ((LPFILETIME)c)
#else
__declspec(dllimport)
void __stdcall GetSystemTimeAsFileTime(unsigned __int64*);
#define GSM_CAST(c) ((__int64*)c)
#endif
#endif

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

int adjust_vertices(GLenum mode, int nb) {
    switch (mode) {
        case GL_POINTS:
            return nb;
        case GL_LINES: // 2 points per elements
            return nb-(nb%2);
        case GL_LINE_STRIP: // at least 2 points
        case GL_LINE_LOOP:
            return (nb>1)?nb:0;
        case GL_TRIANGLES:  // 3 points per elements
            return nb-(nb%3);
        case GL_TRIANGLE_FAN:
        case GL_TRIANGLE_STRIP: // at least 3 points
            return (nb>2)?nb:0;
        case GL_QUADS:  // 4 points per elements
            return nb-(nb%4);
        case GL_QUAD_STRIP: // at least 4, the 2 per elements
            return (nb>4)?(nb-(nb%2)):0;
        case GL_POLYGON:   // at least 3
            return (nb>2)?nb:0;
        default:
            return nb;  // meh?
    }
}

#undef client_state
#define clone_gl_pointer(t, s, n)\
    t.size = s; t.type = type; t.stride = stride; t.pointer = (void*)((char*)pointer + (uintptr_t)((glstate->vao->vertex)?glstate->vao->vertex->data:0));\
    t.real_buffer=(glstate->vao->vertex)?glstate->vao->vertex->real_buffer:0; t.real_pointer=(glstate->vao->vertex)?pointer:0;   \
    t.normalized=n; t.divisor=0
#define break_lockarrays(t)\
    if(glstate->vao->vertexattrib[t].real_buffer && glstate->vao->locked_mapped[t]) {    \
        glstate->vao->vertexattrib[t].real_buffer = 0; \
        glstate->vao->locked_mapped[t] = 0; \
    }

void APIENTRY_GL4ES gl4es_glVertexPointer(GLint size, GLenum type,
                     GLsizei stride, const GLvoid *pointer) {
    DBG(printf("glVertexPointer(%d, %s, %d, %p)\n", size, PrintEnum(type), stride, pointer);)
    if(size<1 || size>4) {
        errorShim(GL_INVALID_VALUE);
		return;
    }
    noerrorShimNoPurge();
    break_lockarrays(ATT_VERTEX);
    clone_gl_pointer(glstate->vao->vertexattrib[ATT_VERTEX], size, GL_FALSE);
}
void APIENTRY_GL4ES gl4es_glColorPointer(GLint size, GLenum type,
                     GLsizei stride, const GLvoid *pointer) {
    DBG((size>4)?printf("glColorPointer(%d, %s, %d, %p)\n", size, PrintEnum(type), stride, pointer):printf("glColorPointer(%s, %s, %d, %p)\n", PrintEnum(size), PrintEnum(type), stride, pointer);)
	if (!((size>0 && size<=4) || (size==GL_BGRA && type==GL_UNSIGNED_BYTE))) {
        errorShim(GL_INVALID_VALUE);
		return;
    }
    noerrorShimNoPurge();
    break_lockarrays(ATT_COLOR);
    clone_gl_pointer(glstate->vao->vertexattrib[ATT_COLOR], size, (type==GL_FLOAT)?GL_FALSE:GL_TRUE);
}
void APIENTRY_GL4ES gl4es_glNormalPointer(GLenum type, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("glNormalPointer(%s, %d, %p)\n", PrintEnum(type), stride, pointer);)
    noerrorShimNoPurge();
    break_lockarrays(ATT_NORMAL);
    clone_gl_pointer(glstate->vao->vertexattrib[ATT_NORMAL], 3, GL_FALSE);
}
void APIENTRY_GL4ES gl4es_glTexCoordPointer(GLint size, GLenum type,
                     GLsizei stride, const GLvoid *pointer) {
    DBG(printf("glTexCoordPointer(%d, %s, %d, %p), texture.client=%d\n", size, PrintEnum(type), stride, pointer, glstate->texture.client);)
    if(size<1 || size>4) {
        errorShim(GL_INVALID_VALUE);
		return;
    }
    noerrorShimNoPurge();
    break_lockarrays(ATT_MULTITEXCOORD0+glstate->texture.client);
    clone_gl_pointer(glstate->vao->vertexattrib[ATT_MULTITEXCOORD0+glstate->texture.client], size, GL_FALSE);
}
void APIENTRY_GL4ES gl4es_glSecondaryColorPointer(GLint size, GLenum type, 
					GLsizei stride, const GLvoid *pointer) {
    DBG((size>4)?printf("glSecondaryColorPointer(%d, %s, %d, %p)\n", size, PrintEnum(type), stride, pointer):printf("glSecondaryColorPointer(%s, %s, %d, %p)\n", PrintEnum(size), PrintEnum(type), stride, pointer);)
	if (!(size==3 || (size==GL_BGRA && type==GL_UNSIGNED_BYTE))) {
        errorShim(GL_INVALID_VALUE);
		return;		// Size must be 3...
    }
    break_lockarrays(ATT_SECONDARY);
    clone_gl_pointer(glstate->vao->vertexattrib[ATT_SECONDARY], size, (type==GL_FLOAT)?GL_FALSE:GL_TRUE);
    noerrorShimNoPurge();
}
void APIENTRY_GL4ES gl4es_glFogCoordPointer(GLenum type, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("glFogCoordPointer(%s, %d, %p)\n", PrintEnum(type), stride, pointer);)
    if(type==1 && stride==GL_FLOAT) {
        type = GL_FLOAT;
        stride = 0; // mistake found in some version of openglide...
    }
    break_lockarrays(ATT_FOGCOORD);
    clone_gl_pointer(glstate->vao->vertexattrib[ATT_FOGCOORD], 1, (type==GL_FLOAT)?GL_FALSE:GL_TRUE);
    noerrorShimNoPurge();
}

#undef clone_gl_pointer

AliasExport(void,glVertexPointer,,(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer));
AliasExport(void,glColorPointer,,(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer));
AliasExport(void,glNormalPointer,,(GLenum type, GLsizei stride, const GLvoid *pointer));
AliasExport(void,glTexCoordPointer,,(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer));
AliasExport(void,glSecondaryColorPointer,,(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer));
AliasExport(void,glFogCoordPointer,,(GLenum type, GLsizei stride, const GLvoid *pointer));
AliasExport(void,glSecondaryColorPointer,EXT,(GLint size, GLenum type, GLsizei stride, const GLvoid *pointer));
AliasExport(void,glFogCoordPointer,EXT,(GLenum type, GLsizei stride, const GLvoid *pointer));


void APIENTRY_GL4ES gl4es_glInterleavedArrays(GLenum format, GLsizei stride, const GLvoid *pointer) {
    DBG(printf("glInterleavedArrays(%s, %d, %p)\n", PrintEnum(format), stride, pointer);)
    uintptr_t ptr = (uintptr_t)pointer;
    // element lengths
    GLsizei tex=0, color=0, normal=0, vert=0;
    // element formats
    GLenum tf, cf, nf, vf;
    tf = cf = nf = vf = GL_FLOAT;
    noerrorShim();
    switch (format) {
        case GL_V2F: vert = 2; break;
        case GL_V3F: vert = 3; break;
        case GL_C4UB_V2F:
            color = 4; cf = GL_UNSIGNED_BYTE;
            vert = 2;
            break;
        case GL_C4UB_V3F:
            color = 4; cf = GL_UNSIGNED_BYTE;
            vert = 3;
            break;
        case GL_C3F_V3F:
            color = 3;
            vert = 4;
            break;
        case GL_N3F_V3F:
            normal = 3;
            vert = 3;
            break;
        case GL_C4F_N3F_V3F:
            color = 4;
            normal = 3;
            vert = 3;
            break;
        case GL_T2F_V3F:
            tex = 2;
            vert = 3;
            break;
        case GL_T4F_V4F:
            tex = 4;
            vert = 4;
            break;
        case GL_T2F_C4UB_V3F:
            tex = 2;
            color = 4; cf = GL_UNSIGNED_BYTE;
            vert = 3;
            break;
        case GL_T2F_C3F_V3F:
            tex = 2;
            color = 3;
            vert = 3;
            break;
        case GL_T2F_N3F_V3F:
            tex = 2;
            normal = 3;
            vert = 3;
            break;
        case GL_T2F_C4F_N3F_V3F:
            tex = 2;
            color = 4;
            normal = 3;
            vert = 3;
            break;
        case GL_T4F_C4F_N3F_V4F:
            tex = 4;
            color = 4;
            normal = 3;
            vert = 4;
            break;
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    if (!stride)
        stride = tex * gl_sizeof(tf) +
                 color * gl_sizeof(cf) +
                 normal * gl_sizeof(nf) +
                 vert * gl_sizeof(vf);
    if (tex) {
		gl4es_glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        gl4es_glTexCoordPointer(tex, tf, stride, (GLvoid *)ptr);
        ptr += tex * gl_sizeof(tf);
    }
    if (color) {
		gl4es_glEnableClientState(GL_COLOR_ARRAY);
        gl4es_glColorPointer(color, cf, stride, (GLvoid *)ptr);
        ptr += color * gl_sizeof(cf);
    }
    if (normal) {
		gl4es_glEnableClientState(GL_NORMAL_ARRAY);
        gl4es_glNormalPointer(nf, stride, (GLvoid *)ptr);
        ptr += normal * gl_sizeof(nf);
    }
    if (vert) {
		gl4es_glEnableClientState(GL_VERTEX_ARRAY);
        gl4es_glVertexPointer(vert, vf, stride, (GLvoid *)ptr);
    }
}
AliasExport(void,glInterleavedArrays,,(GLenum format, GLsizei stride, const GLvoid *pointer));

// immediate mode functions
void APIENTRY_GL4ES gl4es_glBegin(GLenum mode) {
    glstate->list.begin = 1;
    if (!glstate->list.active)
        glstate->list.active = alloc_renderlist();
    // small optim... continue a render command if possible
    glstate->list.active = NewDrawStage(glstate->list.active, mode);
    glstate->list.pending = 0;
    glstate->list.active->use_vbo_array = 2;
    noerrorShimNoPurge();	// TODO, check Enum validity
}
AliasExport(void,glBegin,,(GLenum mode));

void APIENTRY_GL4ES gl4es_glEnd(void) {
    if (!glstate->list.active) return;
    glstate->list.begin = 0;
    // check if TEXTUREx is activate and no TexCoord (or texgen), in that case, create a dummy one base on glstate->..
    for (int a=0; a<hardext.maxtex; a++)
		if ((hardext.esversion==1) && glstate->enable.texture[a] && ((glstate->list.active->tex[a]==0) && !(glstate->enable.texgen_s[a] || glstate->texture.pscoordreplace[a])))
			rlMultiTexCoord4f(glstate->list.active, GL_TEXTURE0+a, glstate->texcoord[a][0], glstate->texcoord[a][1], glstate->texcoord[a][2], glstate->texcoord[a][3]);
    rlEnd(glstate->list.active); // end the list now
    // render if we're not in a display list
    int withColor = 0;
    if(glstate->list.compiling) {
        glstate->list.active = extend_renderlist(glstate->list.active);
    } else {
        if (!globals4es.beginend /*|| (glstate->polygon_mode==GL_LINE)*/) {
            renderlist_t *mylist = glstate->list.active;
            withColor = (mylist->color!=NULL);
            glstate->list.active = NULL;
            mylist = end_renderlist(mylist);
            draw_renderlist(mylist);
            free_renderlist(mylist);
        } else {
            withColor = (glstate->list.active->color!=NULL);
            glstate->list.pending = 1;
            NewStage(glstate->list.active, STAGE_POSTDRAW);
        }
    }
    if(withColor)
        gl4es_glColor4f(glstate->color[0], glstate->color[1], glstate->color[2], glstate->color[3]);
    noerrorShim();
}
AliasExport(void,glEnd,,());

void APIENTRY_GL4ES gl4es_glNormal3f(GLfloat nx, GLfloat ny, GLfloat nz) {
    if (glstate->list.active) {
        if (glstate->list.active->stage != STAGE_DRAW) {
            if (glstate->list.compiling && glstate->list.active) {
                glstate->list.active->lastNormal[0] = nx; glstate->list.active->lastNormal[1] = ny; glstate->list.active->lastNormal[2] = nz;
            } else if (glstate->list.pending && glstate->list.active->stage==STAGE_POSTDRAW) {
                glstate->list.active->post_normals[0] = nx; glstate->list.active->post_normals[1] = ny;
                glstate->list.active->post_normals[2] = nz;
                glstate->list.active->post_normal = 1;
                return;                
            }

            PUSH_IF_COMPILING(glNormal3f);
        } else {
            rlNormal3f(glstate->list.active, nx, ny, nz);
            glstate->list.active->lastNormal[0] = nx; glstate->list.active->lastNormal[1] = ny; glstate->list.active->lastNormal[2] = nz;
            noerrorShimNoPurge();
        }
    }
    else {
        LOAD_GLES_FPE(glNormal3f);
        errorGL();
        gles_glNormal3f(nx, ny, nz);
    }
    glstate->normal[0] = nx; glstate->normal[1] = ny; glstate->normal[2] = nz;
}
AliasExport(void,glNormal3f,,(GLfloat nx, GLfloat ny, GLfloat nz));

void APIENTRY_GL4ES gl4es_glNormal3fv(GLfloat* v) {
    if (glstate->list.active) {
        if (glstate->list.active->stage != STAGE_DRAW) {
            if (glstate->list.compiling && glstate->list.active) {
                memcpy(glstate->list.active->lastNormal, v, 3*sizeof(GLfloat));
            } else if (glstate->list.pending && glstate->list.active->stage==STAGE_POSTDRAW) {
                memcpy(glstate->list.active->post_normals, v, 3*sizeof(GLfloat));
                glstate->list.active->post_normal = 1;
                return;                
            }

            if (!glstate->list.pending)
                return gl4es_glNormal3f(v[0], v[1], v[2]);  // this will put the call on the stack in the current list
        } else {
            rlNormal3fv(glstate->list.active, v);
            memcpy(glstate->list.active->lastNormal, v, 3*sizeof(GLfloat));
            noerrorShimNoPurge();
        }
    }
    else {
        LOAD_GLES_FPE(glNormal3f);
        errorGL();
        gles_glNormal3f(v[0], v[1], v[2]);
    }
    memcpy(glstate->normal, v, 3*sizeof(GLfloat));
}
AliasExport(void,glNormal3fv,,(GLfloat* v));

void APIENTRY_GL4ES gl4es_glVertex4f(GLfloat x, GLfloat y, GLfloat z, GLfloat w) {
    if (glstate->list.active) {
        rlVertex4f(glstate->list.active, x, y, z, w);
        noerrorShimNoPurge();
    } else {
        glstate->vertex[0]=x; glstate->vertex[1]=y; glstate->vertex[2]=z; glstate->vertex[3]=w;
    }
}
AliasExport(void,glVertex4f,,(GLfloat x, GLfloat y, GLfloat z, GLfloat w));

void APIENTRY_GL4ES gl4es_glVertex3fv(GLfloat* v) {
    if (glstate->list.active) {
        rlVertex3fv(glstate->list.active, v);
        noerrorShimNoPurge();
    } else {
        memcpy(glstate->vertex, v, 3*sizeof(GLfloat));
        glstate->vertex[3]=1.f;
    }
}
AliasExport(void,glVertex3fv,,(GLfloat* v));

void APIENTRY_GL4ES gl4es_glVertex4fv(GLfloat* v) {
    if (glstate->list.active) {
        rlVertex4fv(glstate->list.active, v);
        noerrorShimNoPurge();
    } else {
        memcpy(glstate->vertex, v, 3*sizeof(GLfloat));
        glstate->vertex[3]=1.f;
    }
}
AliasExport(void,glVertex4fv,,(GLfloat* v));

void APIENTRY_GL4ES gl4es_glColor4f(GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha) {
    if (glstate->list.active) {
        if (glstate->list.active->stage != STAGE_DRAW) {
            if (glstate->list.compiling || glstate->list.active->stage<STAGE_DRAW) {
                glstate->list.active->lastColors[0] = red; glstate->list.active->lastColors[1] = green;
                glstate->list.active->lastColors[2] = blue; glstate->list.active->lastColors[3] = alpha;
                glstate->list.active->lastColorsSet = 1;
            }
            else if (glstate->list.pending && glstate->list.active->stage==STAGE_POSTDRAW) {
                glstate->list.active->post_colors[0] = red; glstate->list.active->post_colors[1] = green;
                glstate->list.active->post_colors[2] = blue; glstate->list.active->post_colors[3] = alpha;
                glstate->list.active->post_color = 1;
                return;
            }
            PUSH_IF_COMPILING(glColor4f);
        } else {
            rlColor4f(glstate->list.active, red, green, blue, alpha);
            noerrorShimNoPurge();
        }
    } else {
        LOAD_GLES_FPE(glColor4f);
        errorGL();
        gles_glColor4f(red, green, blue, alpha);
    }
    // change the state last thing
    glstate->color[0] = red; glstate->color[1] = green;
    glstate->color[2] = blue; glstate->color[3] = alpha;
}
AliasExport(void,glColor4f,,(GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha));

void APIENTRY_GL4ES gl4es_glColor4fv(GLfloat* v) {
    if (glstate->list.active) {
        if (glstate->list.active->stage != STAGE_DRAW) {
            if (glstate->list.compiling || glstate->list.active->stage<STAGE_DRAW) {
                memcpy(glstate->list.active->lastColors, v, 4*sizeof(GLfloat));
                glstate->list.active->lastColorsSet = 1;
            }
            else if (glstate->list.pending && glstate->list.active->stage==STAGE_POSTDRAW) {
                memcpy(glstate->list.active->post_colors, v, 4*sizeof(GLfloat));
                glstate->list.active->post_color = 1;
                return;
            }
            if (!glstate->list.pending)
                return gl4es_glColor4f(v[0], v[1], v[2], v[3]);
        } else {
            rlColor4fv(glstate->list.active, v);
            noerrorShimNoPurge();
        }
    } else {
        LOAD_GLES_FPE(glColor4f);
        errorGL();
        gles_glColor4f(v[0], v[1], v[2], v[3]);
    }
    // change the state last thing
    memcpy(glstate->color, v, 4*sizeof(GLfloat));
}
AliasExport(void,glColor4fv,,(GLfloat* v));

void APIENTRY_GL4ES gl4es_glSecondaryColor3f(GLfloat r, GLfloat g, GLfloat b) {
    if (glstate->list.active) {
        if(glstate->list.pending)
            gl4es_flush();
        else
        {
            rlSecondary3f(glstate->list.active, r, g, b);
            glstate->list.active->lastSecondaryColors[0] = r; glstate->list.active->lastSecondaryColors[1] = g;
            glstate->list.active->lastSecondaryColors[2] = b;
        }
        noerrorShimNoPurge();
    } else {
        noerrorShimNoPurge();
    }
    // change the state last thing
    glstate->secondary[0] = r; glstate->secondary[1] = g;
    glstate->secondary[2] = b;
}
AliasExport(void,glSecondaryColor3f,,(GLfloat r, GLfloat g, GLfloat b));
AliasExport(void,glSecondaryColor3f,EXT,(GLfloat r, GLfloat g, GLfloat b));


void APIENTRY_GL4ES gl4es_glTexCoord4f(GLfloat s, GLfloat t, GLfloat r, GLfloat q) {
    if (glstate->list.active) {
        if(glstate->list.pending)
            gl4es_flush();
        else {
            // test if called between glBegin / glEnd but Texture is not active and not using a program. In that case, ignore the call
            if(hardext.esversion==1 || glstate->glsl->program || (glstate->list.begin && (glstate->list.compiling || glstate->enable.texture[0])))
                rlMultiTexCoord4f(glstate->list.active, GL_TEXTURE0, s, t, r, q);
        }
    }
    noerrorShimNoPurge();
    glstate->texcoord[0][0] = s; glstate->texcoord[0][1] = t;
    glstate->texcoord[0][2] = r; glstate->texcoord[0][3] = q;
}
AliasExport(void,glTexCoord4f,,(GLfloat s, GLfloat t, GLfloat r, GLfloat q));

void APIENTRY_GL4ES gl4es_glMultiTexCoord4f(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q) {
	// TODO, error if target is unsuported texture....
    if (glstate->list.active) {
        if(glstate->list.pending)
            gl4es_flush();
        else {
            // test if called between glBegin / glEnd but Texture is not active. In that case, ignore the call
            if(hardext.esversion==1 || (glstate->list.begin && (glstate->list.compiling || glstate->enable.texture[target-GL_TEXTURE0])))
                rlMultiTexCoord4f(glstate->list.active, target, s, t, r, q);
        }
    }
    noerrorShimNoPurge();
    glstate->texcoord[target-GL_TEXTURE0][0] = s; glstate->texcoord[target-GL_TEXTURE0][1] = t;
    glstate->texcoord[target-GL_TEXTURE0][2] = r; glstate->texcoord[target-GL_TEXTURE0][3] = q;
}
AliasExport(void,glMultiTexCoord4f,,(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q));
AliasExport(void,glMultiTexCoord4f,ARB,(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q));

void APIENTRY_GL4ES gl4es_glMultiTexCoord2fv(GLenum target, GLfloat* v) {
	// TODO, error if target is unsuported texture....
    if (glstate->list.active) {
        if(glstate->list.pending)
            gl4es_flush();
        else {
            // test if called between glBegin / glEnd but Texture is not active. In that case, ignore the call
            if(hardext.esversion==1 || (glstate->list.begin && (glstate->list.compiling || glstate->enable.texture[target-GL_TEXTURE0])))
                rlMultiTexCoord2fv(glstate->list.active, target, v);
        }
    }
    noerrorShimNoPurge();
    memcpy(glstate->texcoord[target-GL_TEXTURE0], v, 2*sizeof(GLfloat));
    glstate->texcoord[target-GL_TEXTURE0][2] = 0.f; glstate->texcoord[target-GL_TEXTURE0][3] = 1.f;
}
AliasExport(void,glMultiTexCoord2fv,,(GLenum target, GLfloat* v));
AliasExport(void,glMultiTexCoord2fv,ARB,(GLenum target, GLfloat* v));

void APIENTRY_GL4ES gl4es_glMultiTexCoord4fv(GLenum target, GLfloat* v) {
	// TODO, error if target is unsuported texture....
    if (glstate->list.active) {
        if(glstate->list.pending)
            gl4es_flush();
        else {
            // test if called between glBegin / glEnd but Texture is not active. In that case, ignore the call
            if(hardext.esversion==1 || (glstate->list.begin && (glstate->list.compiling || glstate->enable.texture[target-GL_TEXTURE0])))
                rlMultiTexCoord4fv(glstate->list.active, target, v);
        }
    }
    noerrorShimNoPurge();
    memcpy(glstate->texcoord[target-GL_TEXTURE0], v, 4*sizeof(GLfloat));
}
AliasExport(void,glMultiTexCoord4fv,,(GLenum target, GLfloat* v));
AliasExport(void,glMultiTexCoord4fv,ARB,(GLenum target, GLfloat* v));

void APIENTRY_GL4ES gl4es_glArrayElement(GLint i) {
    GLfloat *v;
    vertexattrib_t *p;
    glvao_t* vao = glstate->vao;
    int stride, size;
    p = &vao->vertexattrib[ATT_COLOR];
    if (p->enabled) {
        size = p->size; stride = p->stride;
        // special fast case for easy stuff...
        if(p->type==GL_FLOAT) {
            if(stride)
                v = (GLfloat*)(((uintptr_t)p->pointer)+i*stride);
            else
                v = ((GLfloat*)p->pointer)+i*size;
            if(size==3)
                gl4es_glColor3fv(v);
            else
                gl4es_glColor4fv(v);
        } else if(p->type==GL_UNSIGNED_BYTE) {
            GLubyte *b;
            if(stride)
                b = (GLubyte*)(((uintptr_t)p->pointer)+i*stride);
            else
                b = ((GLubyte*)p->pointer)+i*size;
            if(size==3)
                gl4es_glColor3ubv(b);
            else
                gl4es_glColor4ubv(b);
        } else {
            v = gl_pointer_index(p, i);
            GLfloat scale = 1.0f/gl_max_value(p->type);
            // color[3] defaults to 1.0f
            if (size < 4)
                v[3] = 1.0f;

            // scale color coordinates to a 0 - 1.0 range
            for (int i = 0; i < size; i++) {
                v[i] *= scale;
            }
            gl4es_glColor4fv(v);
        }
    }
    p = &vao->vertexattrib[ATT_SECONDARY];
    if (p->enabled) {
        v = gl_pointer_index(p, i);
        GLfloat scale = 1.0f/gl_max_value(p->type);

        // scale color coordinates to a 0 - 1.0 range
        for (int i = 0; i < p->size; i++) {
            v[i] *= scale;
        }
        gl4es_glSecondaryColor3fv(v);
    }
    p = &vao->vertexattrib[ATT_NORMAL];
    if (p->enabled) {
        // special fast case for easy stuff...
        if(p->type==GL_FLOAT) {
            size = p->size; stride = p->stride;
            if(stride)
                v = (GLfloat*)(((uintptr_t)p->pointer)+i*stride);
            else
                v = ((GLfloat*)p->pointer)+i*size;
        } else {
            v = gl_pointer_index(p, i);
        }
        gl4es_glNormal3fv(v);
    }
    p = &vao->vertexattrib[ATT_MULTITEXCOORD0];
    if (p->enabled) {
        size = p->size; stride = p->stride;
        // special fast case for easy stuff...
        if(p->type==GL_FLOAT) {
            if(stride)
                v = (GLfloat*)(((uintptr_t)p->pointer)+i*stride);
            else
                v = ((GLfloat*)p->pointer)+i*size;
        } else {
            v = gl_pointer_index(p, i);
        }
        if (size<4)
            gl4es_glTexCoord2fv(v);
        else
            gl4es_glTexCoord4fv(v);
    }
    for (int a=1; a<vao->maxtex; a++) {
        p = &vao->vertexattrib[ATT_MULTITEXCOORD0+a];
	    if (p->enabled) {
            size = p->size; stride = p->stride;
            // special fast case for easy stuff...
            if(p->type==GL_FLOAT) {
            if(p->stride)
                v = (GLfloat*)(((uintptr_t)p->pointer)+i*p->stride);
            else
                v = ((GLfloat*)p->pointer)+i*p->size;
            } else {
                v = gl_pointer_index(p, i);
            }
            if (p->size<4)
                gl4es_glMultiTexCoord2fv(GL_TEXTURE0+a, v);
            else
                gl4es_glMultiTexCoord4fv(GL_TEXTURE0+a, v);
	    }
    }
    p = &vao->vertexattrib[ATT_VERTEX];
    if (p->enabled) {
        // special fast case for easy stuff...
        if(p->type==GL_FLOAT) {
            if(p->stride)
                v = (GLfloat*)(((uintptr_t)p->pointer)+i*p->stride);
            else
                v = ((GLfloat*)p->pointer)+i*p->size;
        } else {
            v = gl_pointer_index(p, i);
        }
        if (p->size == 4) {
            gl4es_glVertex4fv(v);
        } else if (p->size == 3) {
            gl4es_glVertex3fv(v);
        } else {
            gl4es_glVertex2fv(v);
        }
    }
}
AliasExport(void,glArrayElement,,(GLint i));
AliasExport(void,glArrayElement,EXT,(GLint i));

// TODO: between a lock and unlock, I can assume the array pointers are unchanged
// so I can build a renderlist_t on the first call and hold onto it
// maybe I need a way to call a renderlist_t with (first, count)
void APIENTRY_GL4ES gl4es_glLockArrays(GLint first, GLsizei count) {
    if(glstate->vao->locked) {
        errorShim(GL_INVALID_OPERATION);
        return;
    }
    glstate->vao->locked = 1;
    glstate->vao->first = first;
    glstate->vao->count = count;
    noerrorShim();
}
AliasExport(void,glLockArrays,EXT,(GLint first, GLsizei count));
void APIENTRY_GL4ES gl4es_glUnlockArrays(void) {
    if(globals4es.usevbo>1 && glstate->vao->locked==globals4es.usevbo) UnBuffer();
    glstate->vao->locked = 0;

    noerrorShim();
}
AliasExport(void,glUnlockArrays,EXT,());

void ToBuffer(int first, int count) {
    // this is hacky. Only the fpe VA should be treated here (but then, the consistancy check is a bit more difficult to do)
    if(count<13)
        return; // no VBO for smallest ones (4 triangles)
    glstate->vao->locked = globals4es.usevbo;
    // Strategy: compile only VA that are interleaved. So only 1 "Buffer" is compiled. Out of the buffer VA are not compiled
    // That should works with Quake3 engine that expect only Vertices array to be Compiled, but still allow to build more complex arrays
    for (int i=0; i<NB_VA; i++)
        if(glstate->vao->vertexattrib[i].enabled && (!valid_vertex_type(glstate->vao->vertexattrib[i].type) || glstate->vao->vertexattrib[i].real_buffer)) {
            return;
        }
    // try to see if there is a master index....
    uintptr_t master = (uintptr_t)glstate->vao->vertexattrib[ATT_VERTEX].pointer;
    int stride = glstate->vao->vertexattrib[ATT_VERTEX].stride;
    if(stride)
    for (int i=ATT_VERTEX+1; i<NB_VA; i++) {
        if(glstate->vao->vertexattrib[i].enabled) {
            uintptr_t p = (uintptr_t)glstate->vao->vertexattrib[i].pointer;
            int nstride = glstate->vao->vertexattrib[i].stride;
            if(stride && stride==nstride) {
                if ((p>master-stride) && (p<master+stride)) {
                    if(p<master) master = p;
                }
            }
        }
    }
    if(!stride) stride = gl_sizeof(glstate->vao->vertexattrib[ATT_VERTEX].type)*glstate->vao->vertexattrib[ATT_VERTEX].size;
    memset(glstate->vao->locked_mapped, 0, sizeof(glstate->vao->locked_mapped));
    // ok, now we have a "master", let's count the required size
    int total = stride * count;
    // now allocate (if needed) the buffer and bind it
    uintptr_t ptr = 0;
    // move "master" data if there
    #if 0
    LOAD_GLES(glGenBuffers);
    LOAD_GLES(glBufferData);
    LOAD_GLES(glBindBuffer);
    if(!glstate->scratch_vertex)
        gles_glGenBuffers(1, &glstate->scratch_vertex);
    glstate->scratch_vertex_size = stride*count;
    bindBuffer(GL_ARRAY_BUFFER, glstate->scratch_vertex);
    gles_glBufferData(GL_ARRAY_BUFFER, stride*count, (void*)(master+first*stride), GL_STREAM_DRAW);
    #else
    LOAD_GLES(glBufferSubData);
    gl4es_scratch_vertex(total);    // alloc if needed and bind scratch vertex buffer
    gles_glBufferSubData(GL_ARRAY_BUFFER, ptr, stride*count, (void*)(master+first*stride));
    #endif
    for (int i=0; i<NB_VA; i++) {
        if(glstate->vao->vertexattrib[i].enabled) {
            uintptr_t p = (uintptr_t)glstate->vao->vertexattrib[i].pointer;
            if(p>=master && p<master+stride) {
                glstate->vao->vertexattrib[i].real_pointer = (void*)(p-master - first*stride);
                glstate->vao->vertexattrib[i].real_buffer = glstate->scratch_vertex;
                glstate->vao->locked_mapped[i] = 1;
            }
        }
    }
//printf("BindBuffers (fist=%d, count=%d) vertex = %p %sx%d (%d)\n", first, count, glstate->vao->vertexattrib[ATT_VERTEX].real_pointer, PrintEnum(glstate->vao->vertexattrib[ATT_VERTEX].type), glstate->vao->vertexattrib[ATT_VERTEX].size, glstate->vao->vertexattrib[ATT_VERTEX].stride);
    // unbind the buffer
    gl4es_use_scratch_vertex(0);
}

void UnBuffer()
{
    for (int i=0; i<NB_VA; i++)
        if(glstate->vao->locked_mapped[i]) {
            glstate->vao->vertexattrib[i].real_buffer = 0;
            glstate->vao->vertexattrib[i].real_pointer = NULL;
            glstate->vao->locked_mapped[i] = 0;
        }
}


// display lists

static renderlist_t *gl4es_glGetList(GLuint list) {
    khint_t k;
    khash_t(gllisthead) *lists = glstate->headlists;
    k = kh_get(gllisthead, lists, list);
    if (k != kh_end(lists))
        return kh_value(lists, k);
    return NULL;
}

GLuint APIENTRY_GL4ES gl4es_glGenLists(GLsizei range) {
	if (range<0) {
		errorShim(GL_INVALID_VALUE);
		return 0;
	}
    noerrorShimNoPurge();
    if(range==0) {
        return 0;
    }
   	khint_t k;
   	int ret;
	khash_t(gllisthead) *lists = glstate->headlists;
    int start = glstate->list.count;
    glstate->list.count += range;

    // check start -> start+range-1 is all free !
    int ok = 0;
    do {
        ok = 1;
        for (int i = 1; i <= range && ok; i++) {
            if(gl4es_glGetList(start+i)) {
                ok = 0;
                start += i;
                glstate->list.count += i;
            }
        }
    } while(!ok);

    for (int i = 1; i <= range; i++) {
        k = kh_get(gllisthead, lists, start+i);
        if (k == kh_end(lists)){
            k = kh_put(gllisthead, lists, start+i, &ret);
            kh_value(lists, k) = NULL;  // create an empty gllist
        }
    }
    return start + 1;
}
AliasExport(GLuint,glGenLists,,(GLsizei range));


void APIENTRY_GL4ES gl4es_glNewList(GLuint list, GLenum mode) {
    ERROR_IN_BEGIN
	if (list==0) {
        errorShim(GL_INVALID_VALUE);
		return;
    }
    
    if (glstate->raster.bm_drawing) bitmap_flush();
    FLUSH_BEGINEND;

    if(glstate->list.compiling) {
        // already doing a list
        errorShim(GL_INVALID_OPERATION);
        return;
    }

    noerrorShimNoPurge();
    {
        khint_t k;
        int ret;
        khash_t(gllisthead) *lists = glstate->headlists;
        k = kh_get(gllisthead, lists, list);
        if (k == kh_end(lists)){
            k = kh_put(gllisthead, lists, list, &ret);
            kh_value(lists, k) = NULL;
        }
    }

    glstate->list.name = list;
    glstate->list.mode = mode;
    // TODO: if glstate->list.active is already defined, we probably need to clean up here
    glstate->list.active = alloc_renderlist();
    glstate->list.compiling = true;
}
AliasExport(void,glNewList,,(GLuint list, GLenum mode));

void APIENTRY_GL4ES gl4es_glEndList(void) {
    GLuint list = glstate->list.name;
    khash_t(gllisthead) *lists = glstate->headlists;
    khint_t k;
    {
        int ret;
        k = kh_get(gllisthead, lists, list);
        if (k == kh_end(lists)){
            k = kh_put(gllisthead, lists, list, &ret);
            kh_value(lists, k) = NULL;
        }
    }
    if (glstate->list.compiling) {
	// Free the previous list if it exist...
        free_renderlist(kh_value(lists, k));
        renderlist_t* l = kh_value(lists, k) = GetFirst(glstate->list.active);
        // set name
        while(l) {
            l->name = list;
            l = l->next;
        }
        glstate->list.compiling = false;
        end_renderlist(glstate->list.active);
        glstate->list.active = NULL;

        if (glstate->list.mode == GL_COMPILE_AND_EXECUTE) {
        	noerrorShim();
            gl4es_glCallList(list);
        } else
        	noerrorShimNoPurge();
    } else
    	noerrorShim();
}
AliasExport(void,glEndList,,());

renderlist_t* append_calllist(renderlist_t *list, renderlist_t *a);
void APIENTRY_GL4ES gl4es_glCallList(GLuint list) {
	noerrorShim();
    if (glstate->list.active) {
        glstate->list.active = append_calllist(glstate->list.active, gl4es_glGetList(list));
		return;
	}
    // TODO: the output of this call can be compiled into another display list
    renderlist_t *l = gl4es_glGetList(list);
    if (l)
        draw_renderlist(l);
}
AliasExport(void,glCallList,,(GLuint list));

void APIENTRY_GL4ES glPushCall(void *call) {
    if (glstate->list.active) {
		NewStage(glstate->list.active, STAGE_GLCALL);
        rlPushCall(glstate->list.active, call);
    }
}

void APIENTRY_GL4ES gl4es_glCallLists(GLsizei n, GLenum type, const GLvoid *lists) {
    #define call(name, type) \
        case name: gl4es_glCallList(((type *)lists)[i] + glstate->list.base); break

    // seriously wtf
    #define call_bytes(name, stride)                             \
        case name:                                               \
            l = (GLubyte *)lists;                                \
            list = 0;                                            \
            for (j = 0; j < stride; j++) {                       \
                list += *(l + (i * stride + j)) << (stride - j); \
            }                                                    \
            gl4es_glCallList(list + glstate->list.base);                  \
            break

    if (glstate->raster.bm_drawing) bitmap_flush();
    FLUSH_BEGINEND;
    unsigned int j;
    GLsizei i;
    GLuint list;
    GLubyte *l;
    for (i = 0; i < n; i++) {
        switch (type) {
            call(GL_BYTE, GLbyte);
            call(GL_UNSIGNED_BYTE, GLubyte);
            call(GL_SHORT, GLshort);
            call(GL_UNSIGNED_SHORT, GLushort);
            call(GL_INT, GLint);
            call(GL_UNSIGNED_INT, GLuint);
            call(GL_FLOAT, GLfloat);
            call_bytes(GL_2_BYTES, 2);
            call_bytes(GL_3_BYTES, 3);
            call_bytes(GL_4_BYTES, 4);
        }
    }
    #undef call
    #undef call_bytes
}
AliasExport(void,glCallLists,,(GLsizei n, GLenum type, const GLvoid *lists));

void APIENTRY_GL4ES gl4es_glDeleteList(GLuint list) {

    renderlist_t *gllist = NULL;
    {
        khint_t k;
        khash_t(gllisthead) *lists = glstate->headlists;
        k = kh_get(gllisthead, lists, list);
        renderlist_t *gllist = NULL;
        if (k != kh_end(lists)){
            gllist = kh_value(lists, k);
            free_renderlist(gllist);
            kh_del(gllisthead, lists, k);
        }
    }
}

void APIENTRY_GL4ES gl4es_glDeleteLists(GLuint list, GLsizei range) {
	noerrorShimNoPurge();
    for (int i = 0; i < range; i++) {
        gl4es_glDeleteList(list+i);
    }
}
AliasExport(void,glDeleteLists,,(GLuint list, GLsizei range));

void APIENTRY_GL4ES gl4es_glListBase(GLuint base) {
	noerrorShimNoPurge();
    glstate->list.base = base;
}
AliasExport(void,glListBase,,(GLuint base));

GLboolean APIENTRY_GL4ES gl4es_glIsList(GLuint list) {
	noerrorShimNoPurge();
    if(!list)
        return GL_FALSE;
    khint_t k;
    khash_t(gllisthead) *lists = glstate->headlists;
    k = kh_get(gllisthead, lists, list);
    if (k != kh_end(lists))
        return GL_TRUE;
    return GL_FALSE;
}
AliasExport(GLboolean,glIsList,,(GLuint list));

void APIENTRY_GL4ES gl4es_glPolygonMode(GLenum face, GLenum mode) {
    ERROR_IN_BEGIN
	noerrorShimNoPurge();
	if (face == GL_FRONT)
		face = GL_FRONT_AND_BACK;   //TODO, better handle all this
	if (face == GL_BACK)
		return;		//TODO, handle face enum for polygon mode != GL_FILL
    if (glstate->list.active)
        if (glstate->list.compiling) {
            NewStage(glstate->list.active, STAGE_POLYGON);
            glstate->list.active->polygon_mode = mode;
            return;
        }
        else gl4es_flush();
    switch(mode) {
	case GL_LINE:
	case GL_POINT:
		glstate->polygon_mode = mode;
		break;
	case GL_FILL:
		glstate->polygon_mode = 0;
		break;
	default:
		glstate->polygon_mode = 0;
    }
}
AliasExport(void,glPolygonMode,,(GLenum face, GLenum mode));


void gl4es_flush() {
    if(glstate->list.compiling)
        return;
    // flush internal list
    renderlist_t *mylist = glstate->list.active?extend_renderlist(glstate->list.active):NULL;
    if (mylist) {
        glstate->list.active = NULL;
        glstate->list.pending = 0;
        mylist = end_renderlist(mylist);
        draw_renderlist(mylist);
        free_renderlist(mylist);
    }
    glstate->list.active = NULL;
}

#ifndef NOX11
extern void BlitEmulatedPixmap(int win);
#endif
void APIENTRY_GL4ES gl4es_glFlush(void) {
	LOAD_GLES(glFlush);
    
    realize_textures(0);
    FLUSH_BEGINEND;
    if (glstate->raster.bm_drawing) bitmap_flush();
    
    gles_glFlush();
    errorGL();

#ifndef NOX11
    if(glstate->emulatedPixmap && !glstate->emulatedWin)
        BlitEmulatedPixmap(0);
#endif
}
AliasExport_V(void,glFlush);

void APIENTRY_GL4ES gl4es_glFinish(void) {
	LOAD_GLES(glFinish);
    
    realize_textures(0);
    FLUSH_BEGINEND;
    if (glstate->raster.bm_drawing) bitmap_flush();
    
    gles_glFinish();
    errorGL();
}
AliasExport_V(void,glFinish);

void APIENTRY_GL4ES gl4es_glIndexPointer(GLenum type, GLsizei stride, const GLvoid * pointer) {
    static bool warning = false;
    if(!warning) {
        LOGD("Warning, stubbed glIndexPointer\n");
        warning = true;
    }
}
AliasExport(void,glIndexPointer,,(GLenum type, GLsizei stride, const GLvoid * pointer));

void APIENTRY_GL4ES gl4es_glEdgeFlagPointer(GLsizei stride, const GLvoid * pointer) {
    static bool warning = false;
    if(!warning) {
        LOGD("Warning, stubbed glEdgeFlagPointer\n");
        warning = true;
    }
}
AliasExport(void,glEdgeFlagPointer,,(GLsizei stride, const GLvoid * pointer));



void APIENTRY_GL4ES gl4es_glShadeModel(GLenum mode) {
    if(mode!=GL_SMOOTH && mode!=GL_FLAT) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    PUSH_IF_COMPILING(glShadeModel);
    noerrorShim();
    if(mode==glstate->shademodel)
        return;
    glstate->shademodel = mode;
    LOAD_GLES2(glShadeModel);
    if(gles_glShadeModel) {
        errorGL();
        gles_glShadeModel(mode);
    }
}
AliasExport(void,glShadeModel,,(GLenum mode));

void APIENTRY_GL4ES gl4es_glAlphaFunc(GLenum func, GLclampf ref) {
    PUSH_IF_COMPILING(glAlphaFunc);
    noerrorShim();
    if(ref<0.0f) ref = 0.0f;
    if(ref>1.0f) ref = 1.0f;
    if(glstate->alphafunc==func && glstate->alpharef==ref)
        return;
    if(func!=GL_NEVER && func!=GL_LESS && func!=GL_EQUAL
        && func!=GL_LEQUAL && func!=GL_GREATER && func!=GL_NOTEQUAL
        && func!=GL_ALWAYS && func!=GL_GEQUAL) {
            errorShim(GL_INVALID_ENUM);
            return;
    }
    glstate->alphafunc = func;
    glstate->alpharef = ref;
    LOAD_GLES_FPE(glAlphaFunc);
    if(gles_glAlphaFunc) {
        errorGL();
        gles_glAlphaFunc(func, ref);
    }
}
AliasExport(void,glAlphaFunc,,(GLenum func, GLclampf ref));

void APIENTRY_GL4ES gl4es_glLogicOp(GLenum opcode) {
    PUSH_IF_COMPILING(glLogicOp);
    noerrorShim();
    if(glstate->logicop==opcode)
        return;
    // TODO: test if opcode is valid
    glstate->logicop = opcode;
    LOAD_GLES2(glLogicOp);
    if(gles_glLogicOp) {
        errorGL();
        gles_glLogicOp(opcode);
    }
}
AliasExport(void,glLogicOp,,(GLenum opcode));

void APIENTRY_GL4ES gl4es_glColorMask(GLboolean red, GLboolean green, GLboolean blue, GLboolean alpha) {
    PUSH_IF_COMPILING(glColorMask);
    if(glstate->colormask[0]==red && glstate->colormask[1]==green && glstate->colormask[2]==blue && glstate->colormask[3]==alpha) {
        noerrorShim();
        return;
    }
    glstate->colormask[0]=red;
    glstate->colormask[1]=green;
    glstate->colormask[2]=blue;
    glstate->colormask[3]=alpha;
    LOAD_GLES(glColorMask);
    gles_glColorMask(red, green, blue, alpha);
}
AliasExport(void,glColorMask,,(GLboolean red, GLboolean green, GLboolean blue, GLboolean alpha));

void APIENTRY_GL4ES gl4es_glClear(GLbitfield mask) {
    PUSH_IF_COMPILING(glClear);

    mask &= GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT;
    LOAD_GLES(glClear);
    gles_glClear(mask);
}
AliasExport(void,glClear,,(GLbitfield mask));

void APIENTRY_GL4ES gl4es_glClampColor(GLenum target, GLenum clamp)
{
    // TODO: test valid clamp values?
    // Not to be executed in list
    if(target==GL_CLAMP_READ_COLOR) {
        glstate->clamp_read_color = clamp;
        noerrorShimNoPurge();
    } else {
        errorShim(GL_INVALID_ENUM);
    }
}
AliasExport(void,glClampColor,,(GLenum target, GLenum clamp));

void gl4es_scratch(int alloc) {
    if(glstate->scratch_alloc<alloc) {
        if(glstate->scratch)
            free(glstate->scratch);
        glstate->scratch = malloc(alloc);
        glstate->scratch_alloc = alloc;
    }
}

void gl4es_scratch_vertex(int alloc) {
    LOAD_GLES(glBufferData);
    LOAD_GLES(glGenBuffers);
    if(!glstate->scratch_vertex) {
        gles_glGenBuffers(1, &glstate->scratch_vertex);
    }
    if(glstate->scratch_vertex_size < alloc) {
#ifdef AMIGAOS4
        LOAD_GLES(glDeleteBuffers);
        GLuint old_buffer = glstate->scratch_vertex;
        gles_glGenBuffers(1, &glstate->scratch_vertex);
        deleteSingleBuffer(old_buffer);
#endif
        bindBuffer(GL_ARRAY_BUFFER, glstate->scratch_vertex);
        gles_glBufferData(GL_ARRAY_BUFFER, alloc, NULL, GL_STREAM_DRAW);
        glstate->scratch_vertex_size = alloc;
    } else
        bindBuffer(GL_ARRAY_BUFFER, glstate->scratch_vertex);
}

void gl4es_use_scratch_vertex(int use) {
    bindBuffer(GL_ARRAY_BUFFER, use?glstate->scratch_vertex:0);
}

void gl4es_scratch_indices(int alloc) {
    LOAD_GLES(glBufferData);
    LOAD_GLES(glGenBuffers);
    if(!glstate->scratch_indices) {
        gles_glGenBuffers(1, &glstate->scratch_indices);
    }
    bindBuffer(GL_ELEMENT_ARRAY_BUFFER, glstate->scratch_indices);
    if(glstate->scratch_indices_size < alloc) {
        gles_glBufferData(GL_ELEMENT_ARRAY_BUFFER, alloc, NULL, GL_DYNAMIC_DRAW);
        glstate->scratch_indices_size = alloc;
    }
}

void gl4es_use_scratch_indices(int use) {
    bindBuffer(GL_ELEMENT_ARRAY_BUFFER, use?glstate->scratch_indices:0);
}

#if defined(AMIGAOS4) || (defined(NOX11) && defined(NOEGL))
#ifdef AMIGAOS4
void amiga_pre_swap()
#else
NonAliasExportDecl(void,gl4es_pre_swap,())
#endif
{
    if (glstate->list.active) gl4es_flush();
    if (glstate->raster.bm_drawing) bitmap_flush();

    if (globals4es.usefbo) {
        unbindMainFBO();
        blitMainFBO(0, 0, 0, 0);
        // blit the main_fbo before swap
    }
}

void show_fps() {
    if (globals4es.showfps) 
    {
        // framerate counter
        static float avg, fps = 0;
        static int frame1, last_frame, frame, now, current_frames;
#ifndef _WIN32
        struct timeval out;
        gettimeofday(&out, NULL);
        now = out.tv_sec;
#else
        unsigned __int64 ft;
        GetSystemTimeAsFileTime(GSM_CAST(&ft));
        now = (unsigned)((ft / 10000000) - 11644473600ull); // 2unixtime
#endif
        frame++;
        current_frames++;

        if (frame == 1) {
            frame1 = now;
        } else if (frame1 < now) {
            if (last_frame < now) {
                float change = current_frames / (float)(now - last_frame);
                float weight = 0.7f;
                if (! fps) {
                    fps = change;
                } else {
                    fps = (1 - weight) * fps + weight * change;
                }
                current_frames = 0;

                avg = frame / (float)(now - frame1);
                printf("LIBGL: fps: %.2f, avg: %.2f\n", fps, avg);
            }
        }
        last_frame = now;
    }
}


#ifdef AMIGAOS4
void amiga_post_swap()
#else
NonAliasExportDecl(void,gl4es_post_swap,())
#endif
{
		show_fps();

    // If drawing in fbo, rebind it...
    if (globals4es.usefbo) {
        bindMainFBO();
    }
}
#endif
