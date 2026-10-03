#include "texgen.h"

#include "../glx/hardext.h"
#include "wrap/gl4es.h"
#include "fpe.h"
#include "init.h"
#include "loader.h"
#include "matrix.h"
#include "matvec.h"

//extern void* eglGetProcAddress(const char*);

void APIENTRY_GL4ES gl4es_glTexGeni(GLenum coord, GLenum pname, GLint param) {
    GLfloat params[4] = {0,0,0,0};
    params[0]=param;
    gl4es_glTexGenfv(coord, pname, params);
}

void APIENTRY_GL4ES gl4es_glTexGenfv(GLenum coord, GLenum pname, const GLfloat *param) {
    
    /*
    If pname is GL_TEXTURE_GEN_MODE, then the array must contain
    a single symbolic constant, one of
    GL_OBJECT_LINEAR, GL_EYE_LINEAR, GL_SPHERE_MAP, GL_NORMAL_MAP,
    or GL_REFLECTION_MAP.
    Otherwise, params holds the coefficients for the texture-coordinate
    generation function specified by pname.
    */

    //printf("glTexGenf(%s, %s, %s/%f), texture=%i\n", PrintEnum(coord), PrintEnum(pname), PrintEnum(param[0]), param[0], glstate->texture.active);
    ERROR_IN_BEGIN
    if (glstate->list.active)
        if (glstate->list.compiling) {
            NewStage(glstate->list.active, STAGE_TEXGEN);
            rlTexGenfv(glstate->list.active, coord, pname, param);
            noerrorShim();
            return;
        } else gl4es_flush();

    // pname is in: GL_TEXTURE_GEN_MODE, GL_OBJECT_PLANE, GL_EYE_PLANE
    noerrorShim();
    switch(pname) {
        case GL_TEXTURE_GEN_MODE: {
            int mode = -1;
            int n = glstate->texture.active;
            if(glstate->fpe_state) {
                int p = param[0];
                switch (p) {
                    case GL_OBJECT_LINEAR: mode = FPE_TG_OBJLINEAR; break;
                    case GL_EYE_LINEAR: mode = FPE_TG_EYELINEAR; break;
                    case GL_SPHERE_MAP: mode = FPE_TG_SPHEREMAP; break;
                    case GL_NORMAL_MAP: mode = FPE_TG_NORMALMAP; break;
                    case GL_REFLECTION_MAP: mode = FPE_TG_REFLECMAP; break;
                }
            }
            switch (coord) {
                case GL_S: glstate->texgen[n].S = param[0]; if(mode!=-1) { glstate->fpe_state->texgen[n].texgen_s_mode=mode; } break;
                case GL_T: glstate->texgen[n].T = param[0]; if(mode!=-1) { glstate->fpe_state->texgen[n].texgen_t_mode=mode; } break;
                case GL_R: 
                    if(param[0]==GL_SPHERE_MAP) {
                        errorShim(GL_INVALID_ENUM);
                        return;
                    }
                    glstate->texgen[n].R = param[0]; if(mode!=-1) { glstate->fpe_state->texgen[n].texgen_r_mode=mode; } break;
                case GL_Q: 
                    if(param[0]==GL_REFLECTION_MAP || param[0]==GL_NORMAL_MAP || param[0]==GL_SPHERE_MAP) {
                        errorShim(GL_INVALID_ENUM);
                        return;
                    }
                    glstate->texgen[n].Q = param[0]; if(mode!=-1) { glstate->fpe_state->texgen[n].texgen_q_mode=mode; } break;
                default:
                    errorShim(GL_INVALID_ENUM);
            }
            return;
        }
        case GL_OBJECT_PLANE:
            switch (coord) {
                case GL_S:
                    memcpy(glstate->texgen[glstate->texture.active].S_O, param, 4 * sizeof(GLfloat));
                    break;
                case GL_T:
                    memcpy(glstate->texgen[glstate->texture.active].T_O, param, 4 * sizeof(GLfloat));
                    break;
                case GL_R:
                    memcpy(glstate->texgen[glstate->texture.active].R_O, param, 4 * sizeof(GLfloat));
                    break;
                case GL_Q:
                    memcpy(glstate->texgen[glstate->texture.active].Q_O, param, 4 * sizeof(GLfloat));
                    break;
                default:
                    errorShim(GL_INVALID_ENUM);
            }
            return;
        case GL_EYE_PLANE: {
            // need to transform here
            GLfloat pe[4];
            vector_matrix(param, getInvMVMat(), pe);
            switch (coord) {
                case GL_S:
                    memcpy(glstate->texgen[glstate->texture.active].S_E, pe, 4 * sizeof(GLfloat));
                    break;
                case GL_T:
                    memcpy(glstate->texgen[glstate->texture.active].T_E, pe, 4 * sizeof(GLfloat));
                    break;
                case GL_R:
                    memcpy(glstate->texgen[glstate->texture.active].R_E, pe, 4 * sizeof(GLfloat));
                    break;
                case GL_Q:
                    memcpy(glstate->texgen[glstate->texture.active].Q_E, pe, 4 * sizeof(GLfloat));
                    break;
                default:
                    errorShim(GL_INVALID_ENUM);
                }
            return;
            }
        default:
            errorShim(GL_INVALID_ENUM);
    }
}
void APIENTRY_GL4ES gl4es_glGetTexGenfv(GLenum coord,GLenum pname,GLfloat *params) {
    //FLUSH_BEGINEND;   // no flush on get
    noerrorShim();
	switch(pname) {
		case GL_TEXTURE_GEN_MODE:
			switch (coord) {
				case GL_S: *params = glstate->texgen[glstate->texture.active].S; break;
				case GL_T: *params = glstate->texgen[glstate->texture.active].T; break;
				case GL_R: *params = glstate->texgen[glstate->texture.active].R; break;
				case GL_Q: *params = glstate->texgen[glstate->texture.active].Q; break;
				default: *params = GL_EYE_LINEAR;
			}
			break;
		case GL_OBJECT_PLANE:
			switch (coord) {
				case GL_S:
					memcpy(params, glstate->texgen[glstate->texture.active].S_O, 4 * sizeof(GLfloat));
					break;
				case GL_T:
					memcpy(params, glstate->texgen[glstate->texture.active].T_O, 4 * sizeof(GLfloat));
					break;
				case GL_R:
					memcpy(params, glstate->texgen[glstate->texture.active].R_O, 4 * sizeof(GLfloat));
					break;
				case GL_Q:
					memcpy(params, glstate->texgen[glstate->texture.active].Q_O, 4 * sizeof(GLfloat));
					break;
                default:
                    errorShim(GL_INVALID_ENUM);
			}
            break;
		case GL_EYE_PLANE:
			switch (coord) {
				case GL_S:
					memcpy(params, glstate->texgen[glstate->texture.active].S_E, 4 * sizeof(GLfloat));
					break;
				case GL_T:
					memcpy(params, glstate->texgen[glstate->texture.active].T_E, 4 * sizeof(GLfloat));
					break;
				case GL_R:
					memcpy(params, glstate->texgen[glstate->texture.active].R_E, 4 * sizeof(GLfloat));
					break;
				case GL_Q:
					memcpy(params, glstate->texgen[glstate->texture.active].Q_E, 4 * sizeof(GLfloat));
					break;
                default:
                    errorShim(GL_INVALID_ENUM);
			}
		    break;
        default:
            errorShim(GL_INVALID_ENUM);
	}
}


void dot_loop(const GLfloat *verts, const GLfloat *params, GLfloat *out, GLint count, GLushort *indices) {
    for (int i = 0; i < count; i++) {
	GLushort k = indices?indices[i]:i;
        out[k*4] = dot4(verts+k*4, params);// + params[3];
    }
}

void sphere_loop(const GLfloat *verts, const GLfloat *norm, GLfloat *out, GLint count, GLushort *indices) {
    // based on https://www.opengl.org/wiki/Mathematics_of_glTexGen
/*    if (!norm) {
        printf("LIBGL: GL_SPHERE_MAP without Normals\n");
        return;
    }*/
    // First get the ModelviewMatrix
    GLfloat InvModelview[16];
    matrix_transpose(getInvMVMat(), InvModelview);
    const GLfloat *ModelviewMatrix = getMVMat();
    GLfloat eye[4], eye_norm[4], reflect[4];
    GLfloat a;
    for (int i=0; i<count; i++) {
	GLushort k = indices?indices[i]:i;
        vector_matrix(verts+k*4, ModelviewMatrix, eye);
        vector4_normalize(eye);
        vector3_matrix((norm)?(norm+k*3):glstate->normal, InvModelview, eye_norm);
        vector_normalize(eye_norm);
        a=dot(eye, eye_norm)*2.0f;
        for (int j=0; j<3; j++)
            reflect[j]=eye[j]-eye_norm[j]*a;
        reflect[2]+=1.0f;
        a = 0.5f / sqrtf(dot(reflect, reflect));
        out[k*4+0] = reflect[0]*a + 0.5f;
        out[k*4+1] = reflect[1]*a + 0.5f;
        out[k*4+2] = 0.0f;
        out[k*4+3] = 1.0f;
    }

}

void reflection_loop(const GLfloat *verts, const GLfloat *norm, GLfloat *out, GLint count, GLushort *indices) {
    // based on https://www.opengl.org/wiki/Mathematics_of_glTexGen
/*    if (!norm) {
        printf("LIBGL: GL_REFLECTION_MAP without Normals\n");
        return;
    }*/
    GLfloat InvModelview[16];
    matrix_transpose(InvModelview, getInvMVMat());
    const GLfloat * ModelviewMatrix = getMVMat();
    GLfloat eye[4], eye_norm[4];
    GLfloat a;
    for (int i=0; i<count; i++) {
	GLushort k = indices?indices[i]:i;
        vector_matrix(verts+k*4, ModelviewMatrix, eye);
        vector4_normalize(eye);
        vector3_matrix((norm)?(norm+k*3):glstate->normal, InvModelview, eye_norm);
        vector4_normalize(eye_norm);
        a=dot4(eye, eye_norm)*2.0f;
        out[k*4+0] = eye[0] - eye_norm[0]*a;
        out[k*4+1] = eye[1] - eye_norm[1]*a;
        out[k*4+2] = eye[2] - eye_norm[2]*a;
        out[k*4+3] = 1.0f;
    }

}

void eye_loop(const GLfloat *verts, const GLfloat *param, GLfloat *out, GLint count, GLushort *indices) {
    // based on https://www.opengl.org/wiki/Mathematics_of_glTexGen
    // First get the ModelviewMatrix
    const GLfloat *ModelviewMatrix = getMVMat();
    GLfloat tmp[4];
    for (int i=0; i<count; i++) {
	GLushort k = indices?indices[i]:i;
        matrix_vector(ModelviewMatrix, verts+k*4, tmp);
        out[k*4]=dot4(param, tmp);
    }
}

void eye_loop_dual(const GLfloat *verts, const GLfloat *param1, const GLfloat* param2, GLfloat *out, GLint count, GLushort *indices) {
    // based on https://www.opengl.org/wiki/Mathematics_of_glTexGen
    // First get the ModelviewMatrix
    GLfloat ModelviewMatrix[16], InvModelview[16];
    glGetFloatv(GL_MODELVIEW_MATRIX, InvModelview);
    // column major -> row major
    matrix_transpose(InvModelview, ModelviewMatrix);
    // And get the inverse
    matrix_inverse(ModelviewMatrix, InvModelview);
    GLfloat tmp[4];
    for (int i=0; i<count; i++) {
	GLushort k = indices?indices[i]:i;
        matrix_vector(ModelviewMatrix, verts+k*4, tmp);
        out[k*4+0]=dot4(param1, tmp);
        out[k*4+1]=dot4(param2, tmp);
    }
}

static inline void tex_coord_loop(GLfloat *verts, GLfloat *norm, GLfloat *out, GLint count, GLenum type, GLfloat *param_o, GLfloat *param_e, GLushort *indices) {
    switch (type) {
        case GL_OBJECT_LINEAR:
            dot_loop(verts, param_o, out, count, indices);
            break;
        case GL_EYE_LINEAR:
            eye_loop(verts, param_e, out, count, indices);
            break;
        case GL_SPHERE_MAP:
            //printf("LIBGL: GL_SPHERE_MAP with only 1 TexGen available");  //Broken here
            break;
    }
}

void gen_tex_coords(GLfloat *verts, GLfloat *norm, GLfloat **coords, GLint count, GLint *needclean, int texture, GLushort *indices, GLuint ilen) {
//printf("gen_tex_coords(%p, %p, %p, %d, %p, %d, %p, %d) texgen = S:%s T:%s R:%s Q:%s, enabled:%c%c%c%c, tex=%02X\n", verts, norm, *coords, count, needclean, texture, indices, ilen, (glstate->enable.texgen_s[texture])?PrintEnum(glstate->texgen[texture].S):"-", (glstate->enable.texgen_t[texture])?PrintEnum(glstate->texgen[texture].T):"-", (glstate->enable.texgen_r[texture])?PrintEnum(glstate->texgen[texture].R):"-", (glstate->enable.texgen_q[texture])?PrintEnum(glstate->texgen[texture].Q):"-", (glstate->enable.texgen_s[texture])?'S':'-', (glstate->enable.texgen_t[texture])?'T':'-', (glstate->enable.texgen_r[texture])?'R':'-', (glstate->enable.texgen_q[texture])?'Q':'-', glstate->enable.texture[texture]);
    // TODO: do less work when called from glDrawElements?
    (*needclean) = 0;
    // special case : no texgen but texture activated, create a simple 1 repeated element
    if (!glstate->enable.texgen_s[texture] && !glstate->enable.texgen_t[texture] && !glstate->enable.texgen_r[texture] && !glstate->enable.texgen_q[texture]) {
        if ((*coords)==NULL) 
            *coords = (GLfloat *)malloc(count * 4 * sizeof(GLfloat));
        if (indices)
            for (int i=0; i<ilen; i++) {
                memcpy((*coords)+indices[i]*4, glstate->texcoord[texture], sizeof(GLfloat)*4);
            }
        else
            for (int i=0; i<count*4; i+=4) {
                memcpy((*coords)+i, glstate->texcoord[texture], sizeof(GLfloat)*4);
            }
        return;
    }
    // special case: SPHERE_MAP needs both texgen to make sense
    if ((glstate->enable.texgen_s[texture] && (glstate->texgen[texture].S==GL_SPHERE_MAP)) && (glstate->enable.texgen_t[texture] && (glstate->texgen[texture].T==GL_SPHERE_MAP)))
    {
        if (!IS_TEX2D(glstate->enable.texture[texture]))
            return;
        if ((*coords)==NULL) 
            *coords = (GLfloat *)malloc(count * 4 * sizeof(GLfloat));
        sphere_loop(verts, norm, *coords, (indices)?ilen:count, indices);
        return;
    }
    // special case: REFLECTION_MAP  needs the 3 texgen to make sense
    if ((glstate->enable.texgen_s[texture] && (glstate->texgen[texture].S==GL_REFLECTION_MAP)) 
     && (glstate->enable.texgen_t[texture] && (glstate->texgen[texture].T==GL_REFLECTION_MAP))
     && (glstate->enable.texgen_r[texture] && (glstate->texgen[texture].R==GL_REFLECTION_MAP)))
    {
        if(hardext.cubemap) {
            *needclean=1;
            // setup reflection map!
            GLuint old_tex=glstate->texture.active;
            if (old_tex!=texture) gl4es_glActiveTexture(GL_TEXTURE0 + texture);
            realize_active();
            LOAD_GLES_OES(glTexGeni);
            LOAD_GLES_OES(glTexGenfv);
            LOAD_GLES(glEnable);
            // setup cube map mode
            gles_glTexGeni(GL_TEXTURE_GEN_STR, GL_TEXTURE_GEN_MODE, GL_REFLECTION_MAP);
            // enable texgen
            gles_glEnable(GL_TEXTURE_GEN_STR);
            // check Texture Matrix
            if (!(globals4es.texmat || glstate->texture_matrix[texture]->identity)) {
                LOAD_GLES(glLoadMatrixf);
                GLenum old_mat = glstate->matrix_mode;
                if(old_mat!=GL_TEXTURE) gl4es_glMatrixMode(GL_TEXTURE);
                gles_glLoadMatrixf(getTexMat(texture));
                if(old_mat!=GL_TEXTURE) gl4es_glMatrixMode(old_mat);
            }

            if (old_tex!=texture) gl4es_glActiveTexture(GL_TEXTURE0 + old_tex);
        } else {
            if (!IS_TEX2D(glstate->enable.texture[texture]))
                return;
            if ((*coords)==NULL) 
                *coords = (GLfloat *)malloc(count * 4 * sizeof(GLfloat));
            reflection_loop(verts, norm, *coords, (indices)?ilen:count, indices);
        }
        return;
    }
    // special case: NORMAL_MAP  needs the 3 texgen to make sense
    if ((glstate->enable.texgen_s[texture] && (glstate->texgen[texture].S==GL_NORMAL_MAP)) 
     && (glstate->enable.texgen_t[texture] && (glstate->texgen[texture].T==GL_NORMAL_MAP))
     && (glstate->enable.texgen_r[texture] && (glstate->texgen[texture].R==GL_NORMAL_MAP)))
    {
        *needclean=1;
        // setup normal map!
        GLuint old_tex=glstate->texture.active;
        if (old_tex!=texture) gl4es_glActiveTexture(GL_TEXTURE0 + texture);
        realize_active();
        LOAD_GLES_OES(glTexGeni);
        LOAD_GLES_OES(glTexGenfv);
        LOAD_GLES(glEnable);
        // setup cube map mode
        gles_glTexGeni(GL_TEXTURE_GEN_STR, GL_TEXTURE_GEN_MODE, GL_NORMAL_MAP);
        // enable texgen
        gles_glEnable(GL_TEXTURE_GEN_STR);
        // check Texture Matrix
        if (!(globals4es.texmat || glstate->texture_matrix[texture]->identity)) {
            LOAD_GLES(glLoadMatrixf);
            GLenum old_mat = glstate->matrix_mode;
            if(old_mat!=GL_TEXTURE) gl4es_glMatrixMode(GL_TEXTURE);
            gles_glLoadMatrixf(getTexMat(texture));
            if(old_mat!=GL_TEXTURE) gl4es_glMatrixMode(old_mat);
        }

        if (old_tex!=texture) gl4es_glActiveTexture(GL_TEXTURE0 + old_tex);
            
        return;
    }
    if (!IS_ANYTEX(glstate->enable.texture[texture]))
	return;
    if ((*coords)==NULL) 
        *coords = (GLfloat *)malloc(count * 4 * sizeof(GLfloat));
    if (    (glstate->enable.texgen_s[texture] && glstate->texgen[texture].S==GL_EYE_LINEAR) 
        &&  (glstate->enable.texgen_t[texture] && glstate->texgen[texture].T==GL_EYE_LINEAR) )
    {
        eye_loop_dual(verts, glstate->texgen[texture].S_E, glstate->texgen[texture].T_E, (*coords), (indices)?ilen:count, indices);
    } else {
        if (glstate->enable.texgen_s[texture])
            tex_coord_loop(verts, norm, (*coords), (indices)?ilen:count, glstate->texgen[texture].S, glstate->texgen[texture].S_O, glstate->texgen[texture].S_E, indices);
        if (glstate->enable.texgen_t[texture])
            tex_coord_loop(verts, norm, (*coords)+1, (indices)?ilen:count, glstate->texgen[texture].T, glstate->texgen[texture].T_O, glstate->texgen[texture].T_E, indices);
    }
    if (glstate->enable.texgen_r[texture])
        tex_coord_loop(verts, norm, (*coords)+2, (indices)?ilen:count, glstate->texgen[texture].R, glstate->texgen[texture].R_O, glstate->texgen[texture].R_E, indices);
    else
        for (int i=0; i<((indices)?ilen:count); i++) {
            GLushort k = indices?indices[i]:i;
            (*coords)[k*4+2] = 0.0f;
        }
    if (glstate->enable.texgen_q[texture])
        tex_coord_loop(verts, norm, (*coords)+3, (indices)?ilen:count, glstate->texgen[texture].Q, glstate->texgen[texture].Q_O, glstate->texgen[texture].Q_E, indices);
    else
        for (int i=0; i<((indices)?ilen:count); i++) {
            GLushort k = indices?indices[i]:i;
            (*coords)[k*4+3] = 1.0f;
        }
}

void gen_tex_clean(GLint cleancode, int texture) {
	if (cleancode == 0)
		return;
	if (cleancode == 1) {
		GLuint old_tex=glstate->texture.active;
		LOAD_GLES(glDisable);
		gles_glDisable(GL_TEXTURE_GEN_STR);
        // check Texture Matrix
        if ((hardext.esversion==1) && !(globals4es.texmat || glstate->texture_matrix[texture]->identity)) {
            LOAD_GLES(glLoadIdentity);
            GLenum old_mat = glstate->matrix_mode;
            if(old_mat!=GL_TEXTURE) gl4es_glMatrixMode(GL_TEXTURE);
            gles_glLoadIdentity();
            if(old_mat!=GL_TEXTURE) gl4es_glMatrixMode(old_mat);
        }
		return;
	}
}

void APIENTRY_GL4ES gl4es_glLoadTransposeMatrixf(const GLfloat *m) {
	GLfloat mf[16];
	matrix_transpose(m, mf);
	gl4es_glLoadMatrixf(mf);
    errorGL();
}

void APIENTRY_GL4ES gl4es_glLoadTransposeMatrixd(const GLdouble *m) {
	GLfloat mf[16];
	for (int i=0; i<16; i++)
		mf[i] = m[i];
	gl4es_glLoadTransposeMatrixf(mf);
}

void APIENTRY_GL4ES gl4es_glMultTransposeMatrixd(const GLdouble *m) {
	GLfloat mf[16];
	for (int i=0; i<16; i++)
		mf[i] = m[i];
	gl4es_glMultTransposeMatrixf(mf);
}
void APIENTRY_GL4ES gl4es_glMultTransposeMatrixf(const GLfloat *m) {
	GLfloat mf[16];
	matrix_transpose(m, mf);
	gl4es_glMultMatrixf(mf);
    errorGL();
}

AliasExport(void,glTexGenfv,,(GLenum coord, GLenum pname, const GLfloat *params));
AliasExport(void,glTexGeni,,(GLenum coord, GLenum pname, GLint param));
AliasExport(void,glGetTexGenfv,,(GLenum coord,GLenum pname,GLfloat *params));

AliasExport(void,glLoadTransposeMatrixf,,(const GLfloat *m));
AliasExport(void,glLoadTransposeMatrixd,,(const GLdouble *m));
AliasExport(void,glMultTransposeMatrixd,,(const GLdouble *m));
AliasExport(void,glMultTransposeMatrixf,,(const GLfloat *m));
