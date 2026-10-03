#include "light.h"

#include "../glx/hardext.h"
#include "fpe.h"
#include "loader.h"
#include "matrix.h"
#include "matvec.h"

void APIENTRY_GL4ES gl4es_glLightModelf(GLenum pname, GLfloat param) {
//printf("%sglLightModelf(%04X, %.2f)\n", (state.list.compiling)?"list":"", pname, param);
    ERROR_IN_BEGIN
    if(glstate->list.active) 
        if ((glstate->list.compiling)) {
            GLfloat dummy[4];
            dummy[0]=param;
            gl4es_glLightModelfv(pname, dummy);
            return;
        } else gl4es_flush();
    switch (pname) {
        case GL_LIGHT_MODEL_TWO_SIDE:
            errorGL();
            glstate->light.two_side = param;
            if(glstate->fpe_state)
                glstate->fpe_state->twosided = param;
			break;
        case GL_LIGHT_MODEL_COLOR_CONTROL:
            if(param!=GL_SINGLE_COLOR && param!=GL_SEPARATE_SPECULAR_COLOR ) {
                errorShim(GL_INVALID_VALUE);
                return;
            } else {
                GLboolean value = (param==GL_SEPARATE_SPECULAR_COLOR);
                if(glstate->light.separate_specular == value) {
                    noerrorShim();
                    return;
                }
                glstate->light.separate_specular=value;
                if(glstate->fpe_state)
                    glstate->fpe_state->light_separate=value;
            }
            return; // NOT Supported in GLES 1.1 anyway
        case GL_LIGHT_MODEL_LOCAL_VIEWER:
            {
                GLboolean value = (param!=0.0);
                if(glstate->light.local_viewer == value) {
                    noerrorShim();
                    return;
                }
                glstate->light.local_viewer=value;
                if(glstate->fpe_state)
                    glstate->fpe_state->light_localviewer=value;
            }
            return; // NOT Supported in GLES 1.1 anyway
        case GL_LIGHT_MODEL_AMBIENT:
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    LOAD_GLES_FPE(glLightModelf);
    gles_glLightModelf(pname, param);
}

void APIENTRY_GL4ES gl4es_glLightModelfv(GLenum pname, const GLfloat* params) {
//printf("%sglLightModelfv(%04X, [%.2f, %.2f, %.2f, %.2f])\n", (state.list.compiling)?"list":"", pname, params[0], params[1], params[2], params[3]);
    ERROR_IN_BEGIN
    if(glstate->list.active)
        if ((glstate->list.compiling)) {
            NewStage(glstate->list.active, STAGE_LIGHTMODEL);
    /*		if (glstate->list.active->lightmodel)
                glstate->list.active = extend_renderlist(glstate->list.active);*/
            glstate->list.active->lightmodelparam = pname;
            glstate->list.active->lightmodel = (GLfloat*)malloc(4*sizeof(GLfloat));
            int sz = 4;
            if(pname==GL_LIGHT_MODEL_TWO_SIDE || pname==GL_LIGHT_MODEL_COLOR_CONTROL || pname==GL_LIGHT_MODEL_LOCAL_VIEWER)
                sz=1;
            memcpy(glstate->list.active->lightmodel, params, sz*sizeof(GLfloat));
            noerrorShim();
            return;
        } else gl4es_flush();
    switch (pname) {
        case GL_LIGHT_MODEL_AMBIENT:
            if(memcmp(glstate->light.ambient, params, 4*sizeof(GLfloat))==0) {
                noerrorShim();
                return;
            }
            errorGL();
            memcpy(glstate->light.ambient, params, 4*sizeof(GLfloat));
            break;
        case GL_LIGHT_MODEL_TWO_SIDE:
            if(glstate->light.two_side == params[0]) {
                noerrorShim();
                return;
            }
            errorGL();
            glstate->light.two_side = params[0];
            if(glstate->fpe_state)
                glstate->fpe_state->twosided = params[0];
        break;
        case GL_LIGHT_MODEL_COLOR_CONTROL:
            if(params[0]!=GL_SINGLE_COLOR && params[0]!=GL_SEPARATE_SPECULAR_COLOR ) {
                errorShim(GL_INVALID_VALUE);
                return;
            } else {
                GLboolean value = (params[0]==GL_SEPARATE_SPECULAR_COLOR);
                if(glstate->light.separate_specular == value) {
                    noerrorShim();
                    return;
                }
                glstate->light.separate_specular=value;
                if(glstate->fpe_state)
                    glstate->fpe_state->light_separate=value;
            }
            return; // NOT Supported in GLES 1.1 anyway
        case GL_LIGHT_MODEL_LOCAL_VIEWER:
            {
                GLboolean value = (params[0]!=0.0);
                if(glstate->light.local_viewer == value) {
                    noerrorShim();
                    return;
                }
                glstate->light.local_viewer=value;
                if(glstate->fpe_state)
                    glstate->fpe_state->light_localviewer=value;
            }
            return; // NOT Supported in GLES 1.1 anyway
        default:
            errorShim(GL_INVALID_ENUM);
            return;
    }
    LOAD_GLES_FPE(glLightModelfv);
    gles_glLightModelfv(pname, params);
}

void APIENTRY_GL4ES gl4es_glLightfv(GLenum light, GLenum pname, const GLfloat* params) {
//printf("%sglLightfv(%s, %s, %p=[%.2f, %.2f, %.2f, %.2f])\n", (glstate->list.compiling)?"list":"", PrintEnum(light), PrintEnum(pname), params, (params)?params[0]:0.0f, (params)?params[1]:0.0f, (params)?params[2]:0.0f, (params)?params[3]:0.0f);
    const int nl = light-GL_LIGHT0;
    if(nl<0 || nl>=hardext.maxlights) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    ERROR_IN_BEGIN
    if(glstate->list.active)
        if (glstate->list.compiling) {
            NewStage(glstate->list.active, STAGE_LIGHT);
            rlLightfv(glstate->list.active, light, pname, params);
            noerrorShim();
            return;
        } else gl4es_flush();

    GLfloat tmp[4];
    noerrorShim();
    switch(pname) {
        case GL_AMBIENT:
            if(memcmp(glstate->light.lights[nl].ambient, params, 4*sizeof(GLfloat))==0)
                return;
            memcpy(glstate->light.lights[nl].ambient, params, 4*sizeof(GLfloat));
            break;
        case GL_DIFFUSE:
            if(memcmp(glstate->light.lights[nl].diffuse, params, 4*sizeof(GLfloat))==0)
                return;
            memcpy(glstate->light.lights[nl].diffuse, params, 4*sizeof(GLfloat));
            break;
        case GL_SPECULAR:
            if(memcmp(glstate->light.lights[nl].specular, params, 4*sizeof(GLfloat))==0)
                return;
            memcpy(glstate->light.lights[nl].specular, params, 4*sizeof(GLfloat));
            break;
        case GL_POSITION:
            vector_matrix(params, getMVMat(), tmp);
            if(memcmp(glstate->light.lights[nl].position, tmp, 4*sizeof(GLfloat))==0)
                return;
            memcpy(glstate->light.lights[nl].position, tmp, 4*sizeof(GLfloat));
            if(glstate->fpe_state) {
                int dir = (tmp[3]!=0.f);
                if (dir) {
                    glstate->fpe_state->light_direction |= (1<<nl);
                } else {
                    glstate->fpe_state->light_direction &= ~(1<<nl);
                }
            }
            break;
        case GL_SPOT_DIRECTION:
            vector3_matrix4(params,getMVMat(), tmp);
            if(memcmp(glstate->light.lights[nl].spotDirection, tmp, 3*sizeof(GLfloat))==0)
                return;
            memcpy(glstate->light.lights[nl].spotDirection, tmp, 3*sizeof(GLfloat));
            break;
        case GL_SPOT_EXPONENT:
            if(params[0]<0 || params[0]>128) {
                errorShim(GL_INVALID_VALUE);
                return;
            }
            if(glstate->light.lights[nl].spotExponent == params[0])
                return;
            glstate->light.lights[nl].spotExponent = params[0];
            break;
        case GL_SPOT_CUTOFF:
            if(params[0]<0.f || (params[0]>90.0f && params[0]!=180.f)) {
                errorShim(GL_INVALID_VALUE);
                return;
            }
            if(glstate->light.lights[nl].spotCutoff == params[0])
                return;
            glstate->light.lights[nl].spotCutoff = params[0];
            if(glstate->fpe_state) {
                int dir = (params[0]!=180.f);
                if (dir) {
                    glstate->fpe_state->light_cutoff180 |= (1<<nl);
                } else {
                    glstate->fpe_state->light_cutoff180 &= ~(1<<nl);
                }
            }
            break;
        case GL_CONSTANT_ATTENUATION:
            if(params[0]<0) {
                errorShim(GL_INVALID_VALUE);
                return;
            }
            if(glstate->light.lights[nl].constantAttenuation == params[0])
                return;
            glstate->light.lights[nl].constantAttenuation = params[0];
            break;
        case GL_LINEAR_ATTENUATION:
            if(params[0]<0) {
                errorShim(GL_INVALID_VALUE);
                return;
            }
            if(glstate->light.lights[nl].linearAttenuation == params[0])
                return;
            glstate->light.lights[nl].linearAttenuation = params[0];
            break;
        case GL_QUADRATIC_ATTENUATION:
            if(params[0]<0) {
                errorShim(GL_INVALID_VALUE);
                return;
            }
            if(glstate->light.lights[nl].quadraticAttenuation == params[0])
                return;
            glstate->light.lights[nl].quadraticAttenuation = params[0];
            break;
    }
    LOAD_GLES_FPE(glLightfv);
    gles_glLightfv(light, pname, params);
    errorGL();
}

void APIENTRY_GL4ES gl4es_glLightf(GLenum light, GLenum pname, GLfloat param) {
	GLfloat dummy[4];
	dummy[0]=param;
	gl4es_glLightfv(light, pname, dummy);
}

void APIENTRY_GL4ES gl4es_glMaterialfv(GLenum face, GLenum pname, const GLfloat *params) {
    if(glstate->list.active)
        if(glstate->list.begin) {
            // if a glMaterialfv is called inside a glBegin/glEnd block
            // then use rlMaterial to store in current list the material wanted
            // as if the material was asked before the glBegin()
            // It's not real behaviour, but it's better then nothing
                rlMaterialfv(glstate->list.active, face, pname, params);
                noerrorShim();
                return;
        } else {
            if (glstate->list.compiling) {
                NewStage(glstate->list.active, STAGE_MATERIAL);
                rlMaterialfv(glstate->list.active, face, pname, params);
                noerrorShim();
                return;
            } else gl4es_flush();
        }
    if(face!=GL_FRONT_AND_BACK && face!=GL_FRONT && face!=GL_BACK) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    switch(pname) {
        case GL_AMBIENT:
            if(face==GL_FRONT_AND_BACK || face==GL_FRONT)
                memcpy(glstate->material.front.ambient, params, 4*sizeof(GLfloat));
            if(face==GL_FRONT_AND_BACK || face==GL_BACK)
                memcpy(glstate->material.back.ambient, params, 4*sizeof(GLfloat));
            break;
        case GL_DIFFUSE:
            if(face==GL_FRONT_AND_BACK || face==GL_FRONT)
                memcpy(glstate->material.front.diffuse, params, 4*sizeof(GLfloat));
            if(face==GL_FRONT_AND_BACK || face==GL_BACK)
                memcpy(glstate->material.back.diffuse, params, 4*sizeof(GLfloat));
            break;
        case GL_SPECULAR:
            if(face==GL_FRONT_AND_BACK || face==GL_FRONT)
                memcpy(glstate->material.front.specular, params, 4*sizeof(GLfloat));
            if(face==GL_FRONT_AND_BACK || face==GL_BACK)
                memcpy(glstate->material.back.specular, params, 4*sizeof(GLfloat));
            break;
        case GL_EMISSION:
            if(face==GL_FRONT_AND_BACK || face==GL_FRONT)
                memcpy(glstate->material.front.emission, params, 4*sizeof(GLfloat));
            if(face==GL_FRONT_AND_BACK || face==GL_BACK)
                memcpy(glstate->material.back.emission, params, 4*sizeof(GLfloat));
            break;
        case GL_AMBIENT_AND_DIFFUSE:
            if(face==GL_FRONT_AND_BACK || face==GL_FRONT) {
                memcpy(glstate->material.front.ambient, params, 4*sizeof(GLfloat));
                memcpy(glstate->material.front.diffuse, params, 4*sizeof(GLfloat));
            }
            if(face==GL_FRONT_AND_BACK || face==GL_BACK) {
                memcpy(glstate->material.back.ambient, params, 4*sizeof(GLfloat));
                memcpy(glstate->material.back.diffuse, params, 4*sizeof(GLfloat));
            }
            break;
        case GL_SHININESS:
            if(*params<0.0f || *params>128.0f) {
                errorShim(GL_INVALID_VALUE);
                return;
            }
            if(face==GL_FRONT_AND_BACK || face==GL_FRONT) {
                glstate->material.front.shininess = *params;
                if(glstate->fpe_state)
                    glstate->fpe_state->cm_front_nullexp=(*params<=0.0)?0:1;
            }
            if(face==GL_FRONT_AND_BACK || face==GL_BACK) {
                glstate->material.back.shininess = *params;
                if(glstate->fpe_state)
                    glstate->fpe_state->cm_back_nullexp=(*params<=0.0)?0:1;
            }
            break;
        case GL_COLOR_INDEXES:
            if(face==GL_FRONT_AND_BACK || face==GL_FRONT) {
                glstate->material.front.indexes[0] = params[0];
                glstate->material.front.indexes[1] = params[1];
                glstate->material.front.indexes[2] = params[2];
            }
            if(face==GL_FRONT_AND_BACK || face==GL_BACK) {
                glstate->material.back.indexes[0] = params[0];
                glstate->material.back.indexes[1] = params[1];
                glstate->material.back.indexes[2] = params[2];
            }
            break;
    }

    if(face==GL_BACK && hardext.esversion==1) { // lets ignore GL_BACK in GLES 1.1
        noerrorShim();
        return;
    }
    LOAD_GLES_FPE(glMaterialfv);
    gles_glMaterialfv(GL_FRONT_AND_BACK, pname, params);
    errorGL();
}

void APIENTRY_GL4ES gl4es_glMaterialf(GLenum face, GLenum pname, GLfloat param) {
    ERROR_IN_BEGIN
    if(glstate->list.active)
        if (glstate->list.compiling) {
            GLfloat params[4];
            memset(params, 0, 4*sizeof(GLfloat));
            params[0] = param;
            NewStage(glstate->list.active, STAGE_MATERIAL);
            rlMaterialfv(glstate->list.active, face, pname, params);
            noerrorShim();
            return;
        }

    if(face!=GL_FRONT_AND_BACK && face!=GL_FRONT && face!=GL_BACK) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    if(pname!=GL_SHININESS) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    if(param<0.0f || param>128.0f) {
        errorShim(GL_INVALID_VALUE);
        return;
    }
    if(face==GL_FRONT_AND_BACK || face==GL_FRONT) {
        if(glstate->material.front.shininess == param)
            return;
        glstate->material.front.shininess = param;
    }
    if(face==GL_FRONT_AND_BACK || face==GL_BACK) {
        if(glstate->material.back.shininess == param)
            return;
        glstate->material.back.shininess = param;
    }

    if(face==GL_BACK && hardext.esversion==1) { // lets ignore GL_BACK in GLES 1.1
        noerrorShim();
        return;
    }
    FLUSH_BEGINEND;
    LOAD_GLES_FPE(glMaterialf);
    gles_glMaterialf(GL_FRONT_AND_BACK, pname, param);
    errorGL();
}

void APIENTRY_GL4ES gl4es_glColorMaterial(GLenum face, GLenum mode) {
    ERROR_IN_BEGIN
    if(glstate->list.active)
        if (glstate->list.compiling) {
            NewStage(glstate->list.active, STAGE_COLOR_MATERIAL);
            glstate->list.active->colormat_face = face;
            glstate->list.active->colormat_mode = mode;
            noerrorShim();
            return;
        } else gl4es_flush();

    if(face!=GL_FRONT_AND_BACK && face!=GL_FRONT && face!=GL_BACK) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    if(mode!=GL_EMISSION && mode!=GL_AMBIENT && mode!=GL_DIFFUSE && mode!=GL_SPECULAR && mode!=GL_AMBIENT_AND_DIFFUSE) {
        errorShim(GL_INVALID_ENUM);
        return;
    }
    if(face==GL_FRONT_AND_BACK || face==GL_FRONT)
        glstate->material.front.colormat = mode;
    if(face==GL_FRONT_AND_BACK || face==GL_BACK)
        glstate->material.back.colormat = mode;
    if(glstate->fpe_state) {
        int value = FPE_CM_AMBIENTDIFFUSE;
        switch(mode) {
            case GL_EMISSION: value = FPE_CM_EMISSION; break;
            case GL_AMBIENT: value=FPE_CM_AMBIENT; break;
            case GL_DIFFUSE: value=FPE_CM_DIFFUSE; break;
            case GL_SPECULAR: value=FPE_CM_SPECULAR; break;
        }
        if(face==GL_FRONT_AND_BACK || face==GL_FRONT) {
            glstate->fpe_state->cm_front_mode = value;
        }
        if(face==GL_FRONT_AND_BACK || face==GL_BACK) {
            glstate->fpe_state->cm_back_mode = value;
        }
    }
    noerrorShim();
}

AliasExport(void,glLightModelf,,(GLenum pname, GLfloat param));
AliasExport(void,glLightModelfv,,(GLenum pname, const GLfloat* params));
AliasExport(void,glLightfv,,(GLenum light, GLenum pname, const GLfloat* params));
AliasExport(void,glLightf,,(GLenum light, GLenum pname, GLfloat param));
AliasExport(void,glMaterialfv,,(GLenum face, GLenum pname, const GLfloat *params));
AliasExport(void,glMaterialf,,(GLenum face, GLenum pname, GLfloat param));
AliasExport(void,glColorMaterial,,(GLenum face, GLenum mode));
