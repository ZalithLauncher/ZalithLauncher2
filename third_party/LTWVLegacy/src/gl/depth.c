#include "depth.h"

#include "gl4es.h"
#include "glstate.h"
#include "loader.h"

void APIENTRY_GL4ES gl4es_glDepthFunc(GLenum func) {
    if(glstate->list.compiling) {
        PUSH_IF_COMPILING(glDepthFunc);
    }
    noerrorShim();
    if (glstate->depth.func == func)
        return;
    FLUSH_BEGINEND;
    glstate->depth.func = func;
    LOAD_GLES(glDepthFunc);
    errorGL();
    gles_glDepthFunc(func);
}

void APIENTRY_GL4ES gl4es_glDepthMask(GLboolean flag) {
    if(glstate->list.compiling) {
        PUSH_IF_COMPILING(glDepthMask);
    }
    noerrorShim();
    if (glstate->depth.mask == flag)
        return;
    FLUSH_BEGINEND;
    glstate->depth.mask = flag;
    LOAD_GLES(glDepthMask);
    errorGL();
    gles_glDepthMask(flag);
}

GLfloat clamp(GLfloat a) {
    return (a<0.f)?0.f:((a>1.f)?1.f:a);
}

void APIENTRY_GL4ES gl4es_glDepthRangef(GLclampf Near, GLclampf Far) {
    Near = clamp(Near);
    Far = clamp(Far);
    if(glstate->list.compiling) {
        PUSH_IF_COMPILING(glDepthRangef);
    }
    noerrorShim();
    if ((glstate->depth.Near == Near) && (glstate->depth.Far == Far))
        return;
    FLUSH_BEGINEND;
    glstate->depth.Near = Near;
    glstate->depth.Far = Far;
    LOAD_GLES(glDepthRangef);
    errorGL();
    gles_glDepthRangef(Near, Far);
}

void APIENTRY_GL4ES gl4es_glClearDepthf(GLclampf depth) {
    depth = clamp(depth);
    if(glstate->list.compiling) {
        PUSH_IF_COMPILING(glClearDepthf);
    }
    noerrorShim();
    glstate->depth.clear = depth;
    LOAD_GLES(glClearDepthf);
    errorGL();
    gles_glClearDepthf(depth);
}

AliasExport(void,glDepthFunc,,(GLenum func));
AliasExport(void,glDepthMask,,(GLboolean flag));
AliasExport(void,glDepthRangef,,(GLclampf nearVal, GLclampf farVal));
AliasExport(void,glClearDepthf,,(GLclampf depth));

