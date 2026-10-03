#include "matrix.h"

#include "../glx/hardext.h"
#include "debug.h"
#include "fpe.h"
#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "loader.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

void alloc_matrix(matrixstack_t **matrixstack, int depth) {
	*matrixstack = (matrixstack_t*)malloc(sizeof(matrixstack_t));
	(*matrixstack)->top = 0;
	(*matrixstack)->identity = 0;
	(*matrixstack)->stack = (GLfloat*)malloc(sizeof(GLfloat)*depth*16);
}

#define TOP(A) (glstate->A->stack+(glstate->A->top*16))

static GLfloat* update_current_mat() {
	switch(glstate->matrix_mode) {
		case GL_MODELVIEW:
			return TOP(modelview_matrix);
		case GL_PROJECTION:
			return TOP(projection_matrix);
		case GL_TEXTURE:
			return TOP(texture_matrix[glstate->texture.active]);
		default:
			if(glstate->matrix_mode>=GL_MATRIX0_ARB && glstate->matrix_mode<GL_MATRIX0_ARB+MAX_ARB_MATRIX)
				return TOP(arb_matrix[glstate->matrix_mode-GL_MATRIX0_ARB]);
			return NULL;
	}
}

static int update_current_identity(int I) {
	switch(glstate->matrix_mode) {
		case GL_MODELVIEW:
			return glstate->modelview_matrix->identity = (I)?1:is_identity(TOP(modelview_matrix));
		case GL_PROJECTION:
			return glstate->projection_matrix->identity = (I)?1:is_identity(TOP(projection_matrix));
		case GL_TEXTURE:
			return glstate->texture_matrix[glstate->texture.active]->identity = (I)?1:is_identity(TOP(texture_matrix[glstate->texture.active]));
		default:
			if(glstate->matrix_mode>=GL_MATRIX0_ARB && glstate->matrix_mode<GL_MATRIX0_ARB+MAX_ARB_MATRIX)
				return glstate->arb_matrix[glstate->matrix_mode-GL_MATRIX0_ARB]->identity = (I)?1:is_identity(TOP(arb_matrix[glstate->matrix_mode-GL_MATRIX0_ARB]));
		return 0;
	}
}

static int send_to_hardware() {
	if(hardext.esversion>1)
		return 0;
	switch(glstate->matrix_mode) {
		case GL_PROJECTION:
			return 1;
		case GL_MODELVIEW:
			return 1;
		case GL_TEXTURE:
			return (globals4es.texmat)?1:0;
		/*default:
			if(glstate->matrix_mode>=GL_MATRIX0_ARB && glstate->matrix_mode<GL_MATRIX0_ARB+MAX_ARB_MATRIX)
				return 0;*/
	}
	return 0;
}

void init_matrix(glstate_t* glstate) {
DBG(printf("init_matrix(%p)\n", glstate);)
    alloc_matrix(&glstate->projection_matrix, MAX_STACK_PROJECTION);
    set_identity(TOP(projection_matrix));
	glstate->projection_matrix->identity = 1;
    alloc_matrix(&glstate->modelview_matrix, MAX_STACK_MODELVIEW);
    set_identity(TOP(modelview_matrix));
	glstate->modelview_matrix->identity = 1;
	glstate->texture_matrix = (matrixstack_t**)malloc(sizeof(matrixstack_t*)*MAX_TEX);
	glstate->arb_matrix = (matrixstack_t**)malloc(sizeof(matrixstack_t*)*MAX_ARB_MATRIX);
	set_identity(glstate->mvp_matrix);
	glstate->mvp_matrix_dirty = 0;
	set_identity(glstate->inv_mv_matrix);
	glstate->inv_mv_matrix_dirty = 0;
	// no identity function for 3x3 matrix
	memset(glstate->normal_matrix, 0, 9*sizeof(GLfloat));
	glstate->normal_matrix[0] = glstate->normal_matrix[4] = glstate->normal_matrix[8] = 1.0f;
	glstate->normal_matrix_dirty = 1;
    for (int i=0; i<MAX_TEX; i++) {
        alloc_matrix(&glstate->texture_matrix[i], MAX_STACK_TEXTURE);
        set_identity(TOP(texture_matrix[i]));
		glstate->texture_matrix[i]->identity = 1;
    }
    for (int i=0; i<MAX_ARB_MATRIX; i++) {
        alloc_matrix(&glstate->arb_matrix[i], MAX_STACK_ARB_MATRIX);
        set_identity(TOP(arb_matrix[i]));
		glstate->arb_matrix[i]->identity = 1;
    }
}

void set_fpe_textureidentity() {
	if(glstate->texture_matrix[glstate->texture.active]->identity)	// inverted in fpe flags
		glstate->fpe_state->texture[glstate->texture.active].texmat = 0;
	else
		glstate->fpe_state->texture[glstate->texture.active].texmat = 1;
}

void APIENTRY_GL4ES gl4es_glMatrixMode(GLenum mode) {
DBG(printf("glMatrixMode(%s), list=%p\n", PrintEnum(mode), glstate->list.active);)
	noerrorShim();
	if (glstate->list.active && glstate->list.pending && glstate->matrix_mode==GL_MODELVIEW && mode==GL_MODELVIEW) {
		return;	// nothing to do...
	}
	PUSH_IF_COMPILING(glMatrixMode);

	if(!((mode==GL_MODELVIEW) || (mode==GL_PROJECTION) || (mode==GL_TEXTURE) || (mode>=GL_MATRIX0_ARB && mode<(GL_MATRIX0_ARB+MAX_ARB_MATRIX)))) {
		errorShim(GL_INVALID_ENUM);
		return;
	}
    if(glstate->matrix_mode != mode) {
			glstate->matrix_mode = mode;
			LOAD_GLES_FPE(glMatrixMode);
			gles_glMatrixMode(mode);
    }
}

void APIENTRY_GL4ES gl4es_glPushMatrix(void) {
DBG(printf("glPushMatrix(), list=%p\n", glstate->list.active);)
	if (glstate->list.active && !glstate->list.pending) {
		PUSH_IF_COMPILING(glPushMatrix);
	}
	// get matrix mode
	GLint matrix_mode = glstate->matrix_mode;
	noerrorShim();
	// go...
	switch(matrix_mode) {
		#define P(A, B) if(glstate->A->top+1<MAX_STACK_##B) { \
			memcpy(TOP(A)+16, TOP(A), 16*sizeof(GLfloat)); \
			glstate->A->top++; \
		} else errorShim(GL_STACK_OVERFLOW)
		case GL_PROJECTION:
			P(projection_matrix, PROJECTION);
			break;
		case GL_MODELVIEW:
			P(modelview_matrix, MODELVIEW);
			break;
		case GL_TEXTURE:
			P(texture_matrix[glstate->texture.active], TEXTURE);
			break;
		default:
			if(glstate->matrix_mode>=GL_MATRIX0_ARB && glstate->matrix_mode<GL_MATRIX0_ARB+MAX_ARB_MATRIX) {
				P(arb_matrix[glstate->matrix_mode-GL_MATRIX0_ARB], ARB_MATRIX);
			} else {
				//Warning?
				errorShim(GL_INVALID_OPERATION);
				//LOGE("PushMatrix with Unrecognise matrix mode (0x%04X)\n", matrix_mode);
				//gles_glPushMatrix();
			}
		#undef P
	}
}

void APIENTRY_GL4ES gl4es_glPopMatrix(void) {
DBG(printf("glPopMatrix(), list=%p\n", glstate->list.active);)
	if (glstate->list.active 
	 && !(glstate->list.compiling)
	 && (globals4es.beginend) 
	 && glstate->matrix_mode==GL_MODELVIEW
	 && !(glstate->polygon_mode==GL_LINE) 
	 && glstate->list.pending) {
		// check if pop'd matrix is the same as actual...
		if(memcmp(TOP(modelview_matrix)-16, TOP(modelview_matrix), 16*sizeof(GLfloat))==0) {
			--glstate->modelview_matrix->top;
			return;
		}
	}
	PUSH_IF_COMPILING(glPopMatrix);
	// get matrix mode
	GLint matrix_mode = glstate->matrix_mode;
	// go...
	noerrorShim();
	switch(matrix_mode) {
		#define P(A) if(glstate->A->top) { \
			--glstate->A->top; \
			glstate->A->identity = is_identity(update_current_mat()); \
			if (send_to_hardware()) {LOAD_GLES(glLoadMatrixf); gles_glLoadMatrixf(update_current_mat()); } \
		} else errorShim(GL_STACK_UNDERFLOW)
		case GL_PROJECTION:
			P(projection_matrix);
			glstate->mvp_matrix_dirty = 1;
			break;
		case GL_MODELVIEW:
			P(modelview_matrix);
			glstate->mvp_matrix_dirty = 1;
			glstate->inv_mv_matrix_dirty = 1;
			glstate->normal_matrix_dirty = 1;
			break;
		case GL_TEXTURE:
			P(texture_matrix[glstate->texture.active]);
			if(glstate->fpe_state)
				set_fpe_textureidentity();
			break;
		default:
			if(glstate->matrix_mode>=GL_MATRIX0_ARB && glstate->matrix_mode<GL_MATRIX0_ARB+MAX_ARB_MATRIX) {
				P(arb_matrix[glstate->matrix_mode-GL_MATRIX0_ARB]);
			} else {
				//Warning?
				errorShim(GL_INVALID_OPERATION);
				//LOGE("PopMatrix with Unrecognise matrix mode (0x%04X)\n", matrix_mode);
				//gles_glPopMatrix();
			}
		#undef P
	}
}

void APIENTRY_GL4ES gl4es_glLoadMatrixf(const GLfloat * m) {
DBG(printf("glLoadMatrix(%f, %f, %f, %f, %f, %f, %f...), list=%p\n", m[0], m[1], m[2], m[3], m[4], m[5], m[6], glstate->list.active);)
	if (glstate->list.active) {
		if(glstate->list.pending) gl4es_flush();
		else {
			NewStage(glstate->list.active, STAGE_MATRIX);
			glstate->list.active->matrix_op = 1;
			memcpy(glstate->list.active->matrix_val, m, 16*sizeof(GLfloat));
			return;
		}
	}
	memcpy(update_current_mat(), m, 16*sizeof(GLfloat));
	const int id = update_current_identity(0);
	if(glstate->matrix_mode==GL_MODELVIEW)
		glstate->normal_matrix_dirty = glstate->inv_mv_matrix_dirty = 1;
	if(glstate->matrix_mode==GL_MODELVIEW || glstate->matrix_mode==GL_PROJECTION)
		glstate->mvp_matrix_dirty = 1;
	else if((glstate->matrix_mode==GL_TEXTURE) && glstate->fpe_state)
		set_fpe_textureidentity();
    if(send_to_hardware()) {
		LOAD_GLES(glLoadMatrixf);
		LOAD_GLES(glLoadIdentity);
		if(id) gles_glLoadIdentity();	// in case the driver as some special optimisations
		else gles_glLoadMatrixf(m);
	}
}

void APIENTRY_GL4ES gl4es_glMultMatrixf(const GLfloat * m) {
DBG(printf("glMultMatrix(%f, %f, %f, %f, %f, %f, %f...), list=%p\n", m[0], m[1], m[2], m[3], m[4], m[5], m[6], glstate->list.active);)
	if (glstate->list.active) {
		if(glstate->list.pending) gl4es_flush();
		else {
			if(glstate->list.active->stage == STAGE_MATRIX) {
				// multiply the matrix mith the current one....
				matrix_mul(glstate->list.active->matrix_val, m, glstate->list.active->matrix_val);
				return;
			}
			NewStage(glstate->list.active, STAGE_MATRIX);
			glstate->list.active->matrix_op = 2;
			memcpy(glstate->list.active->matrix_val, m, 16*sizeof(GLfloat));
			return;
		}
	}
	GLfloat *current_mat = update_current_mat();
	matrix_mul(current_mat, m, current_mat);
	const int id = update_current_identity(0);
	if(glstate->matrix_mode==GL_MODELVIEW)
		glstate->normal_matrix_dirty = glstate->inv_mv_matrix_dirty = 1;
	if(glstate->matrix_mode==GL_MODELVIEW || glstate->matrix_mode==GL_PROJECTION)
		glstate->mvp_matrix_dirty = 1;
	else if((glstate->matrix_mode==GL_TEXTURE) && glstate->fpe_state)
		set_fpe_textureidentity();
	DBG(printf(" => (%f, %f, %f, %f, %f, %f, %f...)\n", current_mat[0], current_mat[1], current_mat[2], current_mat[3], current_mat[4], current_mat[5], current_mat[6]);)
	if(send_to_hardware()) {
		LOAD_GLES(glLoadMatrixf);
		LOAD_GLES(glLoadIdentity);
		if(id) gles_glLoadIdentity();	// in case the driver as some special optimisations
		else gles_glLoadMatrixf(current_mat);
	}
}

void APIENTRY_GL4ES gl4es_glLoadIdentity(void) {
DBG(printf("glLoadIdentity(), list=%p\n", glstate->list.active);)
	if (glstate->list.active) {
		if(glstate->list.pending) gl4es_flush();
		else {
			NewStage(glstate->list.active, STAGE_MATRIX);
			glstate->list.active->matrix_op = 1;
			set_identity(glstate->list.active->matrix_val);
			return;
		}
	}
	set_identity(update_current_mat());
	update_current_identity(1);
	if(glstate->matrix_mode==GL_MODELVIEW)
		glstate->normal_matrix_dirty = glstate->inv_mv_matrix_dirty = 1;
	if(glstate->matrix_mode==GL_MODELVIEW || glstate->matrix_mode==GL_PROJECTION)
		glstate->mvp_matrix_dirty = 1;
	else if((glstate->matrix_mode==GL_TEXTURE) && glstate->fpe_state)
		set_fpe_textureidentity();
	if(send_to_hardware()) {
		LOAD_GLES(glLoadIdentity);
		gles_glLoadIdentity();
	}
}

void APIENTRY_GL4ES gl4es_glTranslatef(GLfloat x, GLfloat y, GLfloat z) {
DBG(printf("glTranslatef(%f, %f, %f), list=%p\n", x, y, z, glstate->list.active);)
	// create a translation matrix than multiply it...
	GLfloat tmp[16];
	set_identity(tmp);
	tmp[12+0] = x;
	tmp[12+1] = y;
	tmp[12+2] = z;
	gl4es_glMultMatrixf(tmp);
}

void APIENTRY_GL4ES gl4es_glScalef(GLfloat x, GLfloat y, GLfloat z) {
DBG(printf("glScalef(%f, %f, %f), list=%p\n", x, y, z, glstate->list.active);)
	// create a scale matrix than multiply it...
	GLfloat tmp[16];
	memset(tmp, 0, 16*sizeof(GLfloat));
	tmp[0+0] = x;
	tmp[1+4] = y;
	tmp[2+8] = z;
	tmp[3+12] = 1.0f;
	gl4es_glMultMatrixf(tmp);
}

void APIENTRY_GL4ES gl4es_glRotatef(GLfloat angle, GLfloat x, GLfloat y, GLfloat z) {
DBG(printf("glRotatef(%f, %f, %f, %f), list=%p\n", angle, x, y, z, glstate->list.active);)
	// create a rotation matrix than multiply it...
	GLfloat tmp[16];
	memset(tmp, 0, 16*sizeof(GLfloat));
	if((x==0 && y==0 && z==0) || angle==0)
		return;	// nothing to do
	// normalize x y z
	GLfloat l = 1.0f/sqrtf(x*x+y*y+z*z);
	x=x*l; y=y*l; z=z*l;
	// calculate sin/cos
	angle*=3.1415926535f/180.f;
	const GLfloat s=sinf(angle); 
	const GLfloat c=cosf(angle);
	const GLfloat c1 = 1-c;
	//build the matrix
	tmp[0+0] = x*x*c1+c;   tmp[0+4] = x*y*c1-z*s; tmp[0+8] = x*z*c1+y*s;
	tmp[1+0] = y*x*c1+z*s; tmp[1+4] = y*y*c1+c;   tmp[1+8] = y*z*c1-x*s;
	tmp[2+0] = x*z*c1-y*s; tmp[2+4] = y*z*c1+x*s; tmp[2+8] = z*z*c1+c;

	tmp[3+12] = 1.0f;
	// done...
	gl4es_glMultMatrixf(tmp);
}

void APIENTRY_GL4ES gl4es_glOrthof(GLfloat left, GLfloat right, GLfloat bottom, GLfloat top, GLfloat nearVal, GLfloat farVal) {
DBG(printf("glOrthof(%f, %f, %f, %f, %f, %f), list=%p\n", left, right, top, bottom, nearVal, farVal, glstate->list.active);)
	GLfloat tmp[16];
	memset(tmp, 0, 16*sizeof(GLfloat));

	tmp[0+0] = 2.0f/(right-left);     tmp[0+12] = -(right+left)/(right-left);
	tmp[1+4] = 2.0f/(top-bottom);     tmp[1+12] = -(top+bottom)/(top-bottom);
	tmp[2+8] =-2.0f/(farVal-nearVal); tmp[2+12] = -(farVal+nearVal)/(farVal-nearVal);
	                                  tmp[3+12] = 1.0f;

	gl4es_glMultMatrixf(tmp);
}

void APIENTRY_GL4ES gl4es_glFrustumf(GLfloat left,	GLfloat right, GLfloat bottom, GLfloat top,	GLfloat nearVal, GLfloat farVal) {
DBG(printf("glFrustumf(%f, %f, %f, %f, %f, %f) list=%p\n", left, right, top, bottom, nearVal, farVal, glstate->list.active);)
	GLfloat tmp[16];
	memset(tmp, 0, 16*sizeof(GLfloat));

	tmp[0+0] = 2.0f*nearVal/(right-left);	tmp[0+8] = (right+left)/(right-left);
	tmp[1+4] = 2.0f*nearVal/(top-bottom);   tmp[1+8] = (top+bottom)/(top-bottom);
											tmp[2+8] =-(farVal+nearVal)/(farVal-nearVal); tmp[2+12] =-2.0f*farVal*nearVal/(farVal-nearVal);
	                                  		tmp[3+8] = -1.0f;

	gl4es_glMultMatrixf(tmp);
}

AliasExport(void,glMatrixMode,,(GLenum mode));
AliasExport_V(void,glPushMatrix);
AliasExport_V(void,glPopMatrix);
AliasExport(void,glLoadMatrixf,,(const GLfloat * m));
AliasExport(void,glMultMatrixf,,(const GLfloat * m));
AliasExport_V(void,glLoadIdentity);
AliasExport(void,glTranslatef,,(GLfloat x, GLfloat y, GLfloat z));
AliasExport(void,glScalef,,(GLfloat x, GLfloat y, GLfloat z));
AliasExport(void,glRotatef,,(GLfloat angle, GLfloat x, GLfloat y, GLfloat z));
AliasExport(void,glOrthof,,(GLfloat left, GLfloat right, GLfloat bottom, GLfloat top, GLfloat nearVal, GLfloat farVal));
AliasExport(void,glFrustumf,,(GLfloat left, GLfloat right, GLfloat bottom, GLfloat top, GLfloat nearVal, GLfloat farVal));
