#include "hint.h"

#include <gl4eshint.h>
#include "init.h"
#include "loader.h"
#include "gl4es.h"
#include "glstate.h"

#ifdef PANDORA
void pandora_set_gamma();
#endif

void APIENTRY_GL4ES gl4es_glHint(GLenum pname, GLenum mode) {
    
    FLUSH_BEGINEND;

    LOAD_GLES(glHint);
    noerrorShim();
    switch(pname) {
        // some Hint are not supported in GLES2, so just ignoring them
        case GL_FOG_HINT:
            if(hardext.esversion>1)
                return;
            gles_glHint(pname, mode);
            break;
        case GL_PERSPECTIVE_CORRECTION_HINT:
            if(hardext.esversion>1)
                return;
            gles_glHint(pname, mode);
            break;
        case GL_LINE_SMOOTH_HINT:
            if(hardext.esversion>1)
                return;
            gles_glHint(pname, mode);
            break;
        case GL_POINT_SMOOTH_HINT:
            if(hardext.esversion>1)
                return;
            gles_glHint(pname, mode);
            break;
        case GL_TEXTURE_COMPRESSION_HINT:   // ignore this one
            // TODO: track setted value
            return;
        // specifics GL4ES Hints
        case GL_SHRINK_HINT_GL4ES:
            if (mode<=11)
                globals4es.texshrink = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_ALPHAHACK_HINT_GL4ES: 
            if (mode<=1)
                globals4es.alphahack = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_RECYCLEFBO_HINT_GL4ES: 
            if (mode<=1)
                globals4es.recyclefbo = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_MIPMAP_HINT_GL4ES: 
            if (mode<=4)
                globals4es.automipmap = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_TEXDUMP_HINT_GL4ES: 
            if (mode<=1)
                globals4es.texdump = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_COPY_HINT_GL4ES: 
            if (mode>1) //removed
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_NOLUMAPHA_HINT_GL4ES: 
            if (mode<=1)
                globals4es.nolumalpha = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_BLENDHACK_HINT_GL4ES: 
            if (mode<=1)
                globals4es.blendhack = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_BATCH_HINT_GL4ES:
            globals4es.minbatch = 0;
            globals4es.maxbatch = mode*100;
            break;
        case GL_NOERROR_HINT_GL4ES: 
            if (mode<=1)
                globals4es.noerror = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_NODOWNSAMPLING_HINT_GL4ES:
            if (mode<=1)
                globals4es.nodownsampling = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_NOVAOCACHE_HINT_GL4ES:
            if (mode<=1)
                globals4es.novaocache = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_BEGINEND_HINT_GL4ES:
            if(mode<=2)
                globals4es.beginend = mode;
            else
                errorShim(GL_INVALID_ENUM); 
        case GL_AVOID16BITS_HINT_GL4ES:
            if (mode<=1)
                globals4es.avoid16bits = mode;
            else
                errorShim(GL_INVALID_ENUM); 
            break;
        case GL_GAMMA_HINT_GL4ES:
            globals4es.gamma = ((float)mode)/10.f;
#ifdef PANDORA
            pandora_set_gamma();
#endif
            break;
        case GL_GENERATE_MIPMAP_HINT:
            if(mode==GL_FASTEST || mode==GL_NICEST || mode==GL_DONT_CARE)
                glstate->mipmap_hint = mode;
            errorGL();
            gles_glHint(pname, mode);
            break;
        default:
            errorGL();
            gles_glHint(pname, mode);
    }
}
AliasExport(void,glHint,,(GLenum pname, GLenum mode));