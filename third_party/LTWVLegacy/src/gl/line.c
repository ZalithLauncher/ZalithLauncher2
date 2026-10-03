#include "line.h"
#include <stdio.h>

#include "debug.h"
#include "gl4es.h"
#include "glstate.h"
#include "list.h"
#include "matrix.h"
#include "matvec.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

void APIENTRY_GL4ES gl4es_glLineStipple(GLuint factor, GLushort pattern) {
    DBG(printf("glLineStipple(%d, 0x%04X)\n", factor, pattern);)
    if(glstate->list.active) {
        if (glstate->list.compiling) {
            NewStage(glstate->list.active, STAGE_LINESTIPPLE);
            glstate->list.active->linestipple_op = 1;
            glstate->list.active->linestipple_factor = factor;
            glstate->list.active->linestipple_pattern = pattern;
            return;
        } else gl4es_flush();
    }
    if(factor<1) factor = 1;
    if(factor>256) factor = 256;
    if(pattern!=glstate->linestipple.pattern || factor!=glstate->linestipple.factor || !glstate->linestipple.texture) {
        glstate->linestipple.factor = factor;
        glstate->linestipple.pattern = pattern;
        for (int i = 0; i < 16; i++) {
            glstate->linestipple.data[i] = ((pattern >> i) & 1) ? 255 : 0;
        }

        // "Push" current Texture0 binding
        GLuint old_act = glstate->texture.active;
        if(old_act)
            gl4es_glActiveTexture(GL_TEXTURE0);
        GLuint old_tex = glstate->texture.bound[0][ENABLED_TEX2D]->texture;
        // create / update stipple texture
        if (! glstate->linestipple.texture) {
            gl4es_glGenTextures(1, &glstate->linestipple.texture);
            gl4es_glBindTexture(GL_TEXTURE_2D, glstate->linestipple.texture);
            gl4es_glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
            gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4es_glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA,
                16, 1, 0, GL_ALPHA, GL_UNSIGNED_BYTE, glstate->linestipple.data);
        } else {
            gl4es_glBindTexture(GL_TEXTURE_2D, glstate->linestipple.texture);
            gl4es_glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 16, 1, 
                GL_ALPHA, GL_UNSIGNED_BYTE, glstate->linestipple.data);
        }
        // "Pop" texture 0 binding
        gl4es_glBindTexture(GL_TEXTURE_2D, old_tex);
        if(old_act)
            gl4es_glActiveTexture(GL_TEXTURE0+old_act);
        // all done
        noerrorShim();
    }
}
AliasExport(void,glLineStipple,,(GLuint factor, GLushort pattern));

void bind_stipple_tex() {
    gl4es_glBindTexture(GL_TEXTURE_2D, glstate->linestipple.texture);
}

GLfloat *gen_stipple_tex_coords(GLfloat *vert, GLushort *sindices, modeinit_t *modes, int stride, int length, GLfloat* noalloctex) {
    DBG(printf("Generate stripple tex (stride=%d, noalloctex=%p) length=%d:", stride, noalloctex, length);)
    // generate our texture coords
    GLfloat *tex = noalloctex?noalloctex:(GLfloat *)malloc(modes[length-1].ilen * 4 * sizeof(GLfloat));
    GLfloat *texPos = tex;
    GLfloat *vertPos = vert;

    GLfloat x1, x2, y1, y2;
    GLfloat oldlen, len;
    const GLfloat* mvp = getMVPMat();
    GLfloat v[4];
    GLfloat w = (GLfloat)glstate->raster.viewport.width;
    GLfloat h = (GLfloat)glstate->raster.viewport.height;
    if(stride==0) stride = 4; else stride/=sizeof(GLfloat);
    int texstride = noalloctex?stride:4;
    // projected coordinates here, and transform to screen pixel using viewport
    // because projected coordinates are from -1. to +1., w and h are to be divided by 2...
    w*=0.5f; h*=0.5f;
    int i=0;
    for (int k=0; k<length; k++) {
        GLenum mode = modes[k].mode_init;
        DBG(printf("[%s->%d] ", PrintEnum(mode), modes[k].ilen);)
        oldlen = len = 0.f;
        if(modes[k].ilen<2)
            continue;
        if(mode==GL_LINES || length>1)  // always line when multiple lines were merged
            for (; i < modes[k].ilen; i+=2) {
                if(sindices)
                    vertPos = vert+stride*sindices[i];
                vector_matrix(vertPos, mvp, v);
                if(sindices)
                    vertPos = vert+stride*sindices[i+1];
                else
                    vertPos+=stride;
                // need to take "w" component into account...
                if(v[3]==0.0f) {
                    x1=v[0]*w; y1=v[1]*h;
                } else {
                    x1=(v[0]/v[3])*w; y1=(v[1]/v[3])*h;
                }
                vector_matrix(vertPos, mvp, v);
                vertPos+=stride;
                if(v[3]==0.0f) {
                    x2=v[0]*w; y2=v[1]*h;
                } else {
                    x2=(v[0]/v[3])*w; y2=(v[1]/v[3])*h;
                }
                oldlen = len;
                len += sqrtf((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)) / (glstate->linestipple.factor * 16.f);
                DBG(printf("%f->%f (%f,%f -> %f,%f)\t", oldlen, len, x1, y1, x2, y2);)
                if(sindices)
                    texPos = tex+texstride*sindices[i+0];   // it gets written 2*, but that should be ok, it's the same value
                memset(texPos, 0, 4*sizeof(GLfloat));
                texPos[0] = oldlen; texPos[3] = 1.0f;
                if(sindices)
                    texPos = tex+texstride*sindices[i+1];
                else
                    texPos+=texstride;
                memset(texPos, 0, 4*sizeof(GLfloat));
                texPos[0] = len; texPos[3] = 1.0f;
                texPos+=texstride;
            }
        else { // GL_LINE_STRIP and GL_LINE_LOOPS works the same here 
                // (well, last segment, the "loop" one, will look strange, but I will not add a vertex for that)
            if(sindices)
                vertPos = vert+stride*sindices[i];
            vector_matrix(vertPos, mvp, v);
            x2=(v[0]/v[3])*w; y2=(v[1]/v[3])*h;
            vertPos+=stride;
            DBG(printf("%f\t", len);)
            memset(texPos, 0, 4*sizeof(GLfloat));
            texPos[0] = len; texPos[3] = 1.0f;
            texPos+=texstride;
            ++i;
            for (; i < modes[k].ilen; i++) {
                x1 = x2; y1 = y2;
                if(sindices)
                    vertPos = vert+stride*sindices[i];
                vector_matrix(vertPos, mvp, v);
                vertPos+=stride;
                x2=(v[0]/v[3])*w; y2=(v[1]/v[3])*h;
                len += sqrtf((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)) / (glstate->linestipple.factor * 16.f);
                DBG(printf("->%f\t", len);)
                if(sindices)
                    texPos = tex+texstride*sindices[i];
                memset(texPos, 0, 4*sizeof(GLfloat));
                texPos[0] = len; texPos[3] = 1.0f;
                texPos+=texstride;
            }
        }
    }
    DBG(printf("\n");)
    return tex;
}
