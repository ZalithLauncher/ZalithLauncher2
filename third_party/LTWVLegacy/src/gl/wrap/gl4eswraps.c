#include "gl4es.h"

#include "../texgen.h"
#include "../debug.h"
#include "stub.h"

#define constDoubleToFloat(a, size) \
    GLfloat s[size];                \
    int i;                          \
    for (i = 0; i < size; i++) {    \
        s[i] = a[i];                \
    }

// naive wrappers

void APIENTRY_GL4ES gl4es_glClearDepth(GLdouble depth) {
    gl4es_glClearDepthf(depth);
}
void APIENTRY_GL4ES gl4es_glClipPlane(GLenum plane, const GLdouble *equation) {
    constDoubleToFloat(equation, 4);
    gl4es_glClipPlanef(plane, s);
}
void APIENTRY_GL4ES gl4es_glDepthRange(GLdouble nearVal, GLdouble farVal) {
    gl4es_glDepthRangef(nearVal, farVal);
}
void APIENTRY_GL4ES gl4es_glFogi(GLenum pname, GLint param) {
    gl4es_glFogf(pname, param);
}
void APIENTRY_GL4ES gl4es_glFogiv(GLenum pname, GLint *iparams) {
    switch (pname) {
        case GL_FOG_DENSITY:
        case GL_FOG_START:
        case GL_FOG_END:
        case GL_FOG_MODE:
        case GL_FOG_INDEX: 
        case GL_FOG_COORD_SRC:
        {
            gl4es_glFogf(pname, *iparams);
            break;
        }
        case GL_FOG_COLOR: {
            GLfloat params[4];
            for (int i = 0; i < 4; i++) {
                params[i] = (iparams[i]>>16)*1.0f/32767.f;
            }
            gl4es_glFogfv(pname, params);
            break;
        }
    }
}
void APIENTRY_GL4ES gl4es_glGetTexGendv(GLenum coord,GLenum pname,GLdouble *params) {
	GLfloat fparams[4];
	gl4es_glGetTexGenfv(coord, pname, fparams);
	if (pname==GL_TEXTURE_GEN_MODE) *params=fparams[0];
	else for (int i=0; i<4; i++) params[i]=fparams[i];
}
void APIENTRY_GL4ES gl4es_glGetTexGeniv(GLenum coord,GLenum pname,GLint *params) {
	GLfloat fparams[4];
	gl4es_glGetTexGenfv(coord, pname, fparams);
	if (pname==GL_TEXTURE_GEN_MODE) *params=fparams[0];
	else for (int i=0; i<4; i++) params[i]=fparams[i];
}
void APIENTRY_GL4ES gl4es_glGetMaterialiv(GLenum face, GLenum pname, GLint * params) {
	GLfloat fparams[4];
	gl4es_glGetMaterialfv(face, pname, fparams);
	if (pname==GL_SHININESS) *params=fparams[0];
	else {
        if (pname==GL_COLOR_INDEXES)
            for (int i=0; i<3; i++) params[i]=fparams[i];
        else
            for (int i=0; i<4; i++) params[i]=((int)fparams[i]*32767)<<16;
    }
}
void APIENTRY_GL4ES gl4es_glGetLightiv(GLenum light, GLenum pname, GLint * params) {
	GLfloat fparams[4];
	gl4es_glGetLightfv(light, pname, fparams);
	int n=4;
    switch(pname) {
        case GL_SPOT_EXPONENT:
        case GL_SPOT_CUTOFF:
        case GL_CONSTANT_ATTENUATION:
        case GL_LINEAR_ATTENUATION:
        case GL_QUADRATIC_ATTENUATION:
             n=1;
             break;
	    case GL_SPOT_DIRECTION:
             n=3;
             break;
    }
    if(pname==GL_AMBIENT || pname==GL_DIFFUSE || pname==GL_SPECULAR)
        for (int i=0; i<n; i++) params[i]=((int)fparams[i]*32767)<<16;
    else
	    for (int i=0; i<n; i++) params[i]=fparams[i];
}
void APIENTRY_GL4ES gl4es_glGetTexLevelParameteriv(GLenum target, GLint level, GLenum pname, GLint *params) {
	GLfloat fparams[4];
	gl4es_glGetTexLevelParameterfv(target, level, pname, fparams);
    if(pname==GL_TEXTURE_BORDER_COLOR) {
        for(int i=0; i<4; ++i)
            params[i] = fparams[i];
    } else
	    (*params)=fparams[0];
	return;
}
void APIENTRY_GL4ES gl4es_glGetClipPlane(GLenum plane, GLdouble *equation) {
	GLfloat fparams[4];
	gl4es_glGetClipPlanef(plane, fparams);
	for (int i=0; i<4; i++) equation[i]=fparams[i];
}

void APIENTRY_GL4ES gl4es_glFrustum(GLdouble left, GLdouble right, GLdouble bottom,
             GLdouble top, GLdouble Near, GLdouble Far) {
    gl4es_glFrustumf(left, right, bottom, top, Near, Far);
}
void APIENTRY_GL4ES gl4es_glPixelStoref(GLenum pname, GLfloat param) {
    gl4es_glPixelStorei(pname, param);
}
void APIENTRY_GL4ES gl4es_glLighti(GLenum light, GLenum pname, GLint param) {
    gl4es_glLightf(light, pname, param);
}
void APIENTRY_GL4ES gl4es_glPixelTransferi(GLenum pname, GLint param) {
	gl4es_glPixelTransferf(pname, param);	
}

void APIENTRY_GL4ES gl4es_glLightiv(GLenum light, GLenum pname, GLint *iparams) {
    GLfloat params[4];
    switch (pname) {
        case GL_AMBIENT:
        case GL_DIFFUSE:
        case GL_SPECULAR:
            for (int i = 0; i < 4; i++) {
                params[i] = (iparams[i]>>16)*(1.0f/32767.f);
            }
            gl4es_glLightfv(light, pname, params);
            break;
        case GL_POSITION:
            for (int i = 0; i < 4; i++) {
                params[i] = iparams[i];
            }
            gl4es_glLightfv(light, pname, params);
            break;
        case GL_SPOT_DIRECTION:
            for (int i = 0; i < 4; i++) {
                params[i] = iparams[i];
            }
            gl4es_glLightfv(light, pname, params);
            break;
        case GL_SPOT_EXPONENT:
        case GL_SPOT_CUTOFF:
        case GL_CONSTANT_ATTENUATION:
        case GL_LINEAR_ATTENUATION:
        case GL_QUADRATIC_ATTENUATION: {
            gl4es_glLightf(light, pname, *iparams);
            break;
        }
    }
}

void APIENTRY_GL4ES gl4es_glLightModeli(GLenum pname, GLint param) {
    gl4es_glLightModelf(pname, param);
}
void APIENTRY_GL4ES gl4es_glLightModeliv(GLenum pname, GLint *iparams) {
    switch (pname) {
        case GL_LIGHT_MODEL_AMBIENT: {
            GLfloat params[4];
            for (int i = 0; i < 4; i++) {
                params[i] = (iparams[i]>>16)*1.f/32767.f;
            }
            gl4es_glLightModelfv(pname, params);
            break;
        }
        case GL_LIGHT_MODEL_LOCAL_VIEWER:
        case GL_LIGHT_MODEL_TWO_SIDE: {
            gl4es_glLightModelf(pname, *iparams);
            break;
        }
    }
}

void APIENTRY_GL4ES gl4es_glMateriali(GLenum face, GLenum pname, GLint param) {
    gl4es_glMaterialf(face, pname, param);
}
void APIENTRY_GL4ES gl4es_glMaterialiv(GLenum face, GLenum pname, GLint *iparams) {
    //printf("glMaterialiv(%04X, %04X, [%i,...]\n", face, pname, iparams[0]);
    switch (pname) {
        case GL_AMBIENT: 
		case GL_DIFFUSE:
		case GL_SPECULAR:
		case GL_EMISSION:
        case GL_AMBIENT_AND_DIFFUSE:
		{
            GLfloat params[4];
            for (int i = 0; i < 4; i++) {
                params[i] = (iparams[i]>>16)*1.f/32767.f;
            }
            gl4es_glMaterialfv(face, pname, params);
            break;
        }
		case GL_SHININESS:
		{
            gl4es_glMaterialf(face, pname, *iparams);
            break;
        }
		case GL_COLOR_INDEXES:
		{
            GLfloat params[3];
            for (int i = 0; i < 3; i++) {
                params[i] = iparams[i];
            }
            gl4es_glMaterialfv(face, pname, params);
            break;
        }
    }
}

/*
void APIENTRY_GL4ES glMultiTexCoord2f(GLenum target, GLfloat s, GLfloat t) {
    glMultiTexCoord4f(target, s, t, 0.0f, 1.0f);
}
* */
void APIENTRY_GL4ES gl4es_glMultiTexCoord1f(GLenum target, GLfloat s) {
     gl4es_glMultiTexCoord4f(target, s, 0, 0, 1);
}
void APIENTRY_GL4ES gl4es_glMultiTexCoord1fv(GLenum target, GLfloat *t) {
     gl4es_glMultiTexCoord4f(target, t[0], 0, 0, 1);
}
void APIENTRY_GL4ES gl4es_glMultiTexCoord2f(GLenum target, GLfloat s, GLfloat t) {
     gl4es_glMultiTexCoord4f(target, s, t, 0, 1);
}
void APIENTRY_GL4ES gl4es_glMultiTexCoord3f(GLenum target, GLfloat s, GLfloat t, GLfloat r) {
     gl4es_glMultiTexCoord4f(target, s, t, r, 1);
}
/*void glMultiTexCoord4f(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q) {
     glMultiTexCoord2f(target, s, t);
}*/
/*void APIENTRY_GL4ES gl4es_glMultiTexCoord2fv(GLenum target, GLfloat *t) {
     gl4es_glMultiTexCoord4f(target, t[0], t[1], 0, 1);
}*/
void APIENTRY_GL4ES gl4es_glMultiTexCoord3fv(GLenum target, GLfloat *t) {
     gl4es_glMultiTexCoord4f(target, t[0], t[1], t[2], 1);
}
/*void APIENTRY_GL4ES gl4es_glMultiTexCoord4fv(GLenum target, GLfloat *t) {
     gl4es_glMultiTexCoord4f(target, t[0], t[1], t[2], t[3]);
}*/
/*
void glBlendFuncSeparateEXT (GLenum sfactorRGB, GLenum dfactorRGB, GLenum sfactorAlpha, GLenum dfactorAlpha) {
	glBlendFuncSeparate (sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
}
*/
void APIENTRY_GL4ES gl4es_glOrtho(GLdouble left, GLdouble right, GLdouble bottom,
             GLdouble top, GLdouble Near, GLdouble Far) {
    gl4es_glOrthof(left, right, bottom, top, Near, Far);
}

// OES wrappers

void APIENTRY_GL4ES glClearDepthfOES(GLfloat depth) {
    gl4es_glClearDepthf(depth);
}
void APIENTRY_GL4ES glClipPlanefOES(GLenum plane, const GLfloat *equation) {
    gl4es_glClipPlanef(plane, equation);
}
void APIENTRY_GL4ES glDepthRangefOES(GLclampf Near, GLclampf Far) {
    gl4es_glDepthRangef(Near, Far);
}
void APIENTRY_GL4ES glFrustumfOES(GLfloat left, GLfloat right, GLfloat bottom,
                   GLfloat top, GLfloat Near, GLfloat Far) {
    gl4es_glFrustumf(left, right, bottom, top, Near, Far);
}
void APIENTRY_GL4ES glGetClipPlanefOES(GLenum pname, GLfloat equation[4]) {
    gl4es_glGetClipPlanef(pname, equation);
}
void APIENTRY_GL4ES glOrthofOES(GLfloat left, GLfloat right, GLfloat bottom,
                 GLfloat top, GLfloat Near, GLfloat Far) {
    gl4es_glOrthof(left, right, bottom, top, Near, Far);
}

// glRect

#define GL_RECT(suffix, type)                                       \
    void APIENTRY_GL4ES gl4es_glRect##suffix(type x1, type y1, type x2, type y2) { \
        gl4es_glBegin(GL_QUADS);                                    \
        gl4es_glVertex2##suffix(x1, y1);                            \
        gl4es_glVertex2##suffix(x2, y1);                            \
        gl4es_glVertex2##suffix(x2, y2);                            \
        gl4es_glVertex2##suffix(x1, y2);                            \
		gl4es_glEnd();											    \
    }                                                               \
    void APIENTRY_GL4ES gl4es_glRect##suffix##v(const type *v1, const type *v2) {  \
        gl4es_glRect##suffix(v1[0], v1[1], v2[0], v2[1]);           \
    }

GL_RECT(d, GLdouble)
GL_RECT(f, GLfloat)
GL_RECT(i, GLint)
GL_RECT(s, GLshort)
#undef GL_RECT

// basic thunking

#define THUNK(suffix, type, invmax)                            \
/* colors */                                                \
void APIENTRY_GL4ES gl4es_glColor3##suffix(type r, type g, type b) {             \
    gl4es_glColor4f(r*invmax, g*invmax, b*invmax, 1.0f);                   \
}                                                           \
void APIENTRY_GL4ES gl4es_glColor4##suffix(type r, type g, type b, type a) {     \
    gl4es_glColor4f(r*invmax, g*invmax, b*invmax, a*invmax);                  \
}                                                           \
void APIENTRY_GL4ES gl4es_glColor3##suffix##v(const type *v) {                   \
    gl4es_glColor4f(v[0]*invmax, v[1]*invmax, v[2]*invmax, 1.0f);          \
}                                                           \
void APIENTRY_GL4ES gl4es_glColor4##suffix##v(const type *v) {                   \
    gl4es_glColor4f(v[0]*invmax, v[1]*invmax, v[2]*invmax, v[3]*invmax);      \
}                                                           \
void APIENTRY_GL4ES gl4es_glSecondaryColor3##suffix(type r, type g, type b) {    \
    gl4es_glSecondaryColor3f(r*invmax, g*invmax, b*invmax);                \
}                                                           \
void APIENTRY_GL4ES gl4es_glSecondaryColor3##suffix##v(const type *v) {          \
    gl4es_glSecondaryColor3f(v[0]*invmax, v[1]*invmax, v[2]*invmax);       \
}                                                           \
/* index */                                                 \
void APIENTRY_GL4ES gl4es_glIndex##suffix(type c) {                              \
    gl4es_glIndexf(c);                                            \
}                                                           \
void APIENTRY_GL4ES gl4es_glIndex##suffix##v(const type *c) {                    \
    gl4es_glIndexf(c[0]);                                         \
}                                                           \
/* normal */                                                \
void APIENTRY_GL4ES gl4es_glNormal3##suffix(type x, type y, type z) {            \
    gl4es_glNormal3f(x, y, z);                                    \
}                                                           \
void APIENTRY_GL4ES gl4es_glNormal3##suffix##v(const type *v) {                  \
    gl4es_glNormal3f(v[0], v[1], v[2]);                           \
}                                                           \
/* raster */                                                \
void APIENTRY_GL4ES gl4es_glRasterPos2##suffix(type x, type y) {                 \
    gl4es_glRasterPos3f(x, y, 0);                                 \
}                                                           \
void APIENTRY_GL4ES gl4es_glRasterPos2##suffix##v(type *v) {                     \
    gl4es_glRasterPos3f(v[0], v[1], 0);                           \
}                                                           \
void APIENTRY_GL4ES gl4es_glRasterPos3##suffix(type x, type y, type z) {         \
    gl4es_glRasterPos3f(x, y, z);                                 \
}                                                           \
void APIENTRY_GL4ES gl4es_glRasterPos3##suffix##v(type *v) {                     \
    gl4es_glRasterPos3f(v[0], v[1], v[2]);                        \
}                                                           \
void APIENTRY_GL4ES gl4es_glRasterPos4##suffix(type x, type y, type z, type w) { \
    gl4es_glRasterPos4f(x, y, z, w);                              \
}                                                           \
void APIENTRY_GL4ES gl4es_glRasterPos4##suffix##v(type *v) {                     \
    gl4es_glRasterPos4f(v[0], v[1], v[2], v[3]);                  \
}                                                           \
void APIENTRY_GL4ES gl4es_glWindowPos2##suffix(type x, type y) {                 \
    gl4es_glWindowPos3f(x, y, 0);                                 \
}                                                           \
void APIENTRY_GL4ES gl4es_glWindowPos2##suffix##v(type *v) {                     \
    gl4es_glWindowPos3f(v[0], v[1], 0);                           \
}                                                           \
void APIENTRY_GL4ES gl4es_glWindowPos3##suffix(type x, type y, type z) {         \
    gl4es_glWindowPos3f(x, y, z);                                 \
}                                                           \
void APIENTRY_GL4ES gl4es_glWindowPos3##suffix##v(type *v) {                     \
    gl4es_glWindowPos3f(v[0], v[1], v[2]);                        \
}                                                           \
/* vertex */                                                \
void APIENTRY_GL4ES gl4es_glVertex2##suffix(type x, type y) {                    \
    gl4es_glVertex4f(x, y, 0, 1);                                 \
}                                                           \
void APIENTRY_GL4ES gl4es_glVertex2##suffix##v(type *v) {                        \
    gl4es_glVertex4f(v[0], v[1], 0 ,1);                           \
}                                                           \
void APIENTRY_GL4ES gl4es_glVertex3##suffix(type x, type y, type z) {            \
    gl4es_glVertex4f(x, y, z, 1);                                 \
}                                                           \
void APIENTRY_GL4ES gl4es_glVertex3##suffix##v(type *v) {                        \
    gl4es_glVertex4f(v[0], v[1], v[2], 1);                        \
}                                                           \
void APIENTRY_GL4ES gl4es_glVertex4##suffix(type r, type g, type b, type w) {    \
    gl4es_glVertex4f(r, g, b, w);                                 \
}                                                           \
void APIENTRY_GL4ES gl4es_glVertex4##suffix##v(type *v) {                        \
    gl4es_glVertex4f(v[0], v[1], v[2], v[3]);                     \
}                                                           \
/* texture */                                               \
void APIENTRY_GL4ES gl4es_glTexCoord1##suffix(type s) {                          \
    gl4es_glTexCoord4f(s, 0, 0, 1);                               \
}                                                           \
void APIENTRY_GL4ES gl4es_glTexCoord1##suffix##v(type *t) {                      \
    gl4es_glTexCoord4f(t[0], 0, 0, 1);                            \
}                                                           \
void APIENTRY_GL4ES gl4es_glTexCoord2##suffix(type s, type t) {                  \
    gl4es_glTexCoord4f(s, t, 0, 1);                               \
}                                                           \
void APIENTRY_GL4ES gl4es_glTexCoord2##suffix##v(type *t) {                      \
    gl4es_glTexCoord4f(t[0], t[1], 0, 1);                         \
}                                                           \
void APIENTRY_GL4ES gl4es_glTexCoord3##suffix(type s, type t, type r) {          \
    gl4es_glTexCoord4f(s, t, r, 1);                               \
}                                                           \
void APIENTRY_GL4ES gl4es_glTexCoord3##suffix##v(type *t) {                      \
    gl4es_glTexCoord4f(t[0], t[1], t[2], 1);                      \
}                                                           \
void APIENTRY_GL4ES gl4es_glTexCoord4##suffix(type s, type t, type r, type q) {  \
    gl4es_glTexCoord4f(s, t, r, q);                               \
}                                                           \
void APIENTRY_GL4ES gl4es_glTexCoord4##suffix##v(type *t) {                      \
    gl4es_glTexCoord4f(t[0], t[1], t[2], t[3]);                    \
}															\
/* multi-texture */                                         \
void APIENTRY_GL4ES gl4es_glMultiTexCoord1##suffix(GLenum target, type s) {      \
    gl4es_glMultiTexCoord4f(target, s, 0, 0, 1);                        \
}                                                           \
void APIENTRY_GL4ES gl4es_glMultiTexCoord1##suffix##v(GLenum target, type *t) {  \
    gl4es_glMultiTexCoord4f(target, t[0], 0, 0, 1);                    \
}                                                           \
void APIENTRY_GL4ES gl4es_glMultiTexCoord2##suffix(GLenum target, type s, type t) {           \
    gl4es_glMultiTexCoord4f(target, s, t, 0, 1);                               \
}                                                                        \
void APIENTRY_GL4ES gl4es_glMultiTexCoord2##suffix##v(GLenum target, type *t) {               \
    gl4es_glMultiTexCoord4f(target, t[0], t[1], 0, 1);                         \
}                                                                        \
void APIENTRY_GL4ES gl4es_glMultiTexCoord3##suffix(GLenum target, type s, type t, type r) {   \
    gl4es_glMultiTexCoord4f(target, s, t, r, 1);                               \
}                                                                        \
void APIENTRY_GL4ES gl4es_glMultiTexCoord3##suffix##v(GLenum target, type *t) {               \
    gl4es_glMultiTexCoord4f(target, t[0], t[1], t[2], 1);                      \
}                                                                        \
void APIENTRY_GL4ES gl4es_glMultiTexCoord4##suffix(GLenum target, type s, type t, type r, type q) {  \
    gl4es_glMultiTexCoord4f(target, s, t, r, q);                               \
}                                                                        \
void APIENTRY_GL4ES gl4es_glMultiTexCoord4##suffix##v(GLenum target, type *t) {               \
    gl4es_glMultiTexCoord4f(target, t[0], t[1], t[2], t[3]);                   \
}


THUNK(b, GLbyte, (1.0f/(float)CHAR_MAX))
THUNK(d, GLdouble, 1.0f)
THUNK(i, GLint, (1.0f/(float)INT_MAX))
THUNK(s, GLshort, (1.0f/(float)SHRT_MAX))
THUNK(ub, GLubyte, (1.0f/(float)UCHAR_MAX))
THUNK(ui, GLuint, (1.0f/(float)UINT_MAX))
THUNK(us, GLushort, (1.0f/(float)USHRT_MAX))

#undef THUNK

// manually defined float wrappers, because we don't autowrap float functions

// color
void APIENTRY_GL4ES gl4es_glColor3f(GLfloat r, GLfloat g, GLfloat b) {
    gl4es_glColor4f(r, g, b, 1.0f);
}
void APIENTRY_GL4ES gl4es_glColor3fv(GLfloat *c) {
    gl4es_glColor4f(c[0], c[1], c[2], 1.0f);
}
/*void APIENTRY_GL4ES gl4es_glColor4fv(GLfloat *c) {
    gl4es_glColor4f(c[0], c[1], c[2], c[3]);
}*/
void APIENTRY_GL4ES gl4es_glIndexfv(const GLfloat *c) {
    gl4es_glIndexf(*c);
}
void APIENTRY_GL4ES gl4es_glSecondaryColor3fv(const GLfloat *v) {
    gl4es_glSecondaryColor3f(v[0], v[1], v[2]);
}
AliasExport(void,glSecondaryColor3fv,EXT,(GLfloat *t));


// raster
void APIENTRY_GL4ES gl4es_glRasterPos2f(GLfloat x, GLfloat y) {
    gl4es_glRasterPos3f(x, y, 0.0f);
}
void APIENTRY_GL4ES gl4es_glRasterPos2fv(const GLfloat *v) {
    gl4es_glRasterPos3f(v[0], v[1], 0.0f);
}
void APIENTRY_GL4ES gl4es_glRasterPos3fv(const GLfloat *v) {
    gl4es_glRasterPos3f(v[0], v[1], v[2]);
}
void APIENTRY_GL4ES gl4es_glRasterPos4f(GLfloat x, GLfloat y, GLfloat z, GLfloat w) {
    gl4es_glRasterPos3f(x/w, y/w, z/w);
}
void APIENTRY_GL4ES gl4es_glRasterPos4fv(const GLfloat *v) {
    gl4es_glRasterPos4f(v[0], v[1], v[2], v[3]);
}
void APIENTRY_GL4ES gl4es_glWindowPos2f(GLfloat x, GLfloat y) {
    gl4es_glWindowPos3f(x, y, 0.0f);
}
void APIENTRY_GL4ES gl4es_glWindowPos2fv(const GLfloat *v) {
    gl4es_glWindowPos3f(v[0], v[1], 0.0f);
}
void APIENTRY_GL4ES gl4es_glWindowPos3fv(const GLfloat *v) {
    gl4es_glWindowPos3f(v[0], v[1], v[2]);
}

// eval
void APIENTRY_GL4ES gl4es_glEvalCoord1d(GLdouble u) {
    gl4es_glEvalCoord1f(u);
}

void APIENTRY_GL4ES gl4es_glEvalCoord2d(GLdouble u, GLdouble v) {
    gl4es_glEvalCoord2f(u, v);
}

void APIENTRY_GL4ES gl4es_glEvalCoord1fv(GLfloat *v) {
    gl4es_glEvalCoord1f(v[0]);
}

void APIENTRY_GL4ES gl4es_glEvalCoord1dv(GLdouble *v) {
    gl4es_glEvalCoord1d(v[0]);
}

void APIENTRY_GL4ES gl4es_glEvalCoord2fv(GLfloat *v) {
    gl4es_glEvalCoord2f(v[0], v[1]);
}

void APIENTRY_GL4ES gl4es_glEvalCoord2dv(GLdouble *v) {
    gl4es_glEvalCoord2d(v[0], v[1]);
}

void APIENTRY_GL4ES gl4es_glMapGrid1d(GLint un, GLdouble u1, GLdouble u2) {
    gl4es_glMapGrid1f(un, u1, u2);
}

void APIENTRY_GL4ES gl4es_glMapGrid2d(GLint un, GLdouble u1, GLdouble u2,
                 GLint vn, GLdouble v1, GLdouble v2) {
    gl4es_glMapGrid2f(un, u1, u2, vn, v1, v2);
}

// matrix
void APIENTRY_GL4ES gl4es_glLoadMatrixd(const GLdouble *m) {
    constDoubleToFloat(m, 16);
    gl4es_glLoadMatrixf(s);
}
void APIENTRY_GL4ES gl4es_glMultMatrixd(const GLdouble *m) {
    constDoubleToFloat(m, 16);
    gl4es_glMultMatrixf(s);
}

// normal
/*void APIENTRY_GL4ES gl4es_glNormal3fv(GLfloat *v) {
    gl4es_glNormal3f(v[0], v[1], v[2]);
}*/

// textures
void APIENTRY_GL4ES gl4es_glTexCoord1f(GLfloat s) {
    gl4es_glTexCoord4f(s, 0, 0, 1);
}
void APIENTRY_GL4ES gl4es_glTexCoord1fv(GLfloat *t) {
    gl4es_glTexCoord4f(t[0], 0, 0, 1);
}
void APIENTRY_GL4ES gl4es_glTexCoord2f(GLfloat s, GLfloat t) {
    gl4es_glTexCoord4f(s, t, 0, 1);
}
void APIENTRY_GL4ES gl4es_glTexCoord2fv(GLfloat *t) {
//    gl4es_glTexCoord4f(t[0], t[1], 0, 1);
    gl4es_glMultiTexCoord2fv(GL_TEXTURE0, t);
}
void APIENTRY_GL4ES gl4es_glTexCoord3f(GLfloat s, GLfloat t, GLfloat r) {
    gl4es_glTexCoord4f(s, t, r, 1);
}
void APIENTRY_GL4ES gl4es_glTexCoord3fv(GLfloat *t) {
    gl4es_glTexCoord4f(t[0], t[1], t[2], 1);
}
/*void glTexCoord4f(GLfloat s, GLfloat t, GLfloat r, GLfloat q) {
    glTexCoord2f(s, t);
}*/
void APIENTRY_GL4ES gl4es_glTexCoord4fv(GLfloat *t) {
    gl4es_glTexCoord4f(t[0], t[1], t[2], t[3]);
}

// texgen
void APIENTRY_GL4ES gl4es_glTexGend(GLenum coord, GLenum pname, GLdouble param) {
    gl4es_glTexGenf(coord, pname, param);
}
void APIENTRY_GL4ES gl4es_glTexGenf(GLenum coord, GLenum pname, GLfloat param) {
    GLfloat params[4] = {0,0,0,0};
    params[0] = param;
    gl4es_glTexGenfv(coord, pname, params);
}
void APIENTRY_GL4ES gl4es_glTexGendv(GLenum coord, GLenum pname, const GLdouble *params) {
    GLfloat tmp[4];
    tmp[0]=params[0];
    if ((pname==GL_OBJECT_PLANE) || (pname==GL_EYE_PLANE))
		for (int i=1; i<4; i++)
			tmp[i]=params[i];
    gl4es_glTexGenfv(coord, pname, tmp);
}
void APIENTRY_GL4ES gl4es_glTexGeniv(GLenum coord, GLenum pname, const GLint *params) {
    GLfloat tmp[4];
    tmp[0]=params[0];
    if ((pname==GL_OBJECT_PLANE) || (pname==GL_EYE_PLANE))
		for (int i=1; i<4; i++)
			tmp[i]=params[i];
    gl4es_glTexGenfv(coord, pname, tmp);
}

// transforms
void APIENTRY_GL4ES gl4es_glRotated(GLdouble angle, GLdouble x, GLdouble y, GLdouble z) {
    gl4es_glRotatef(angle, x, y, z);
}
void APIENTRY_GL4ES gl4es_glScaled(GLdouble x, GLdouble y, GLdouble z) {
    gl4es_glScalef(x, y, z);
}
void APIENTRY_GL4ES gl4es_glTranslated(GLdouble x, GLdouble y, GLdouble z) {
    gl4es_glTranslatef(x, y, z);
}

// vertex
void APIENTRY_GL4ES gl4es_glVertex2f(GLfloat x, GLfloat y) {
    gl4es_glVertex4f(x, y, 0, 1);
}
void APIENTRY_GL4ES gl4es_glVertex2fv(GLfloat *v) {
    gl4es_glVertex4f(v[0], v[1], 0, 1);
}
/*void APIENTRY_GL4ES gl4es_glVertex3fv(GLfloat *v) {
    gl4es_glVertex4f(v[0], v[1], v[2], 1);
}*/
void APIENTRY_GL4ES gl4es_glVertex3f(GLfloat r, GLfloat g, GLfloat b) {
    gl4es_glVertex4f(r, g, b, 1);
}
/*void APIENTRY_GL4ES gl4es_glVertex4fv(GLfloat *v) {
    gl4es_glVertex4f(v[0], v[1], v[2], v[3]);
}*/

void APIENTRY_GL4ES gl4es_glBlendEquationSeparatei(GLuint buf, GLenum modeRGB, GLenum modeAlpha) {
	// ignore buf is better than nothing...
	// TODO: handle buf
	gl4es_glBlendEquationSeparate(modeRGB, modeAlpha);
}

void APIENTRY_GL4ES gl4es_glBlendFuncSeparatei(GLuint buf, GLenum srcRGB, GLenum dstRGB, GLenum srcAlpha, GLenum dstAlpha) {
	// ignore buf is better than nothing..
	// TODO: handle buf
	gl4es_glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
}

#undef constDoubleToFloat

void APIENTRY_GL4ES gl4es_glGetTexParameterfv(GLenum target, GLenum pname, GLfloat * params) {
    gl4es_glGetTexLevelParameterfv(target, 0, pname, params);
}
 
void APIENTRY_GL4ES gl4es_glGetTexParameteriv(GLenum target, GLenum pname, GLint * params) {
    gl4es_glGetTexLevelParameteriv(target, 0, pname, params);
}


// Samples stuff
#include "../loader.h"
void APIENTRY_GL4ES gl4es_glSampleCoverage(GLclampf value, GLboolean invert) {
    LOAD_GLES(glSampleCoverage);
    PUSH_IF_COMPILING(glSampleCoverage)
    gles_glSampleCoverage(value, invert);
}
AliasExport(void,glSampleCoverage,,(GLclampf value, GLboolean invert));
AliasExport(void,glSampleCoverage,ARB,(GLclampf value, GLboolean invert));

// VertexArray stuff
void APIENTRY_GL4ES gl4es_glVertexAttrib1f (GLuint index, GLfloat v0) { GLfloat f[4] = {0,0,0,1}; f[0] =v0; gl4es_glVertexAttrib4fv(index, f); };
void APIENTRY_GL4ES gl4es_glVertexAttrib2f (GLuint index, GLfloat v0, GLfloat v1) { GLfloat f[4] = {0,0,0,1}; f[0] =v0; f[1]=v1; gl4es_glVertexAttrib4fv(index, f); };
void APIENTRY_GL4ES gl4es_glVertexAttrib3f (GLuint index, GLfloat v0, GLfloat v1, GLfloat v2) { GLfloat f[4] = {0,0,0,1}; f[0] =v0; f[1]=v1; f[2]=v2; gl4es_glVertexAttrib4fv(index, f); };
void APIENTRY_GL4ES gl4es_glVertexAttrib1fv (GLuint index, const GLfloat *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib2fv (GLuint index, const GLfloat *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; f[1]=v[1]; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib3fv (GLuint index, const GLfloat *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; f[1]=v[1]; f[2]=v[2]; gl4es_glVertexAttrib4fv(index, f); }; \
AliasExport(void,glVertexAttrib1f,, (GLuint index, GLfloat v0));
AliasExport(void,glVertexAttrib2f,, (GLuint index, GLfloat v0, GLfloat v1));
AliasExport(void,glVertexAttrib3f,, (GLuint index, GLfloat v0, GLfloat v1, GLfloat v2));
AliasExport(void,glVertexAttrib1fv,, (GLuint index, const GLfloat *v));
AliasExport(void,glVertexAttrib2fv,, (GLuint index, const GLfloat *v));
AliasExport(void,glVertexAttrib3fv,, (GLuint index, const GLfloat *v));
#define THUNK(suffix, type, M2) \
void APIENTRY_GL4ES gl4es_glVertexAttrib1##suffix (GLuint index, type v0) { GLfloat f[4] = {0,0,0,1}; f[0] =v0; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib2##suffix (GLuint index, type v0, type v1) { GLfloat f[4] = {0,0,0,1}; f[0] =v0; f[1]=v1; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib3##suffix (GLuint index, type v0, type v1, type v2) { GLfloat f[4] = {0,0,0,1}; f[0] =v0; f[1]=v1; f[2]=v2; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib4##suffix (GLuint index, type v0, type v1, type v2, type v3) { GLfloat f[4] = {0,0,0,1}; f[0] =v0; f[1]=v1; f[2]=v2; f[3]=v3; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib1##suffix##v (GLuint index, const type *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib2##suffix##v (GLuint index, const type *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; f[1]=v[1]; gl4es_glVertexAttrib4fv(index, f); }; \
void APIENTRY_GL4ES gl4es_glVertexAttrib3##suffix##v (GLuint index, const type *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; f[1]=v[1]; f[2]=v[2]; gl4es_glVertexAttrib4fv(index, f); }; \
AliasExport##M2##_1(void,glVertexAttrib1##suffix,, (GLuint index, type v0)); \
AliasExport##M2##_1(void,glVertexAttrib2##suffix,, (GLuint index, type v0, type v1)); \
AliasExport##M2##_1(void,glVertexAttrib3##suffix,, (GLuint index, type v0, type v1, type v2)); \
AliasExport##M2##_1(void,glVertexAttrib4##suffix,, (GLuint index, type v0, type v1, type v2, type v3)); \
AliasExport(void,glVertexAttrib1##suffix##v,, (GLuint index, const type *v)); \
AliasExport(void,glVertexAttrib2##suffix##v,, (GLuint index, const type *v)); \
AliasExport(void,glVertexAttrib3##suffix##v,, (GLuint index, const type *v))
THUNK(s, GLshort, );
THUNK(d, GLdouble, _D);
#undef THUNK
void APIENTRY_GL4ES gl4es_glVertexAttrib4dv (GLuint index, const GLdouble *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; f[1]=v[1]; f[2]=v[2]; f[3]=v[3]; gl4es_glVertexAttrib4fv(index, f); };
AliasExport(void,glVertexAttrib4dv,, (GLuint index, const GLdouble *v));

#define THUNK(suffix, type, norm) \
void APIENTRY_GL4ES gl4es_glVertexAttrib4##suffix##v (GLuint index, const type *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]; f[1]=v[1]; f[2]=v[2]; f[3]=v[3]; gl4es_glVertexAttrib4fv(index, f); }; \
AliasExport(void,glVertexAttrib4##suffix##v,, (GLuint index, const type *v)); \
void APIENTRY_GL4ES gl4es_glVertexAttrib4N##suffix##v (GLuint index, const type *v) { GLfloat f[4] = {0,0,0,1}; f[0] =v[0]/norm; f[1]=v[1]/norm; f[2]=v[2]/norm; f[3]=v[3]/norm; gl4es_glVertexAttrib4fv(index, f); }; \
AliasExport(void,glVertexAttrib4N##suffix##v,, (GLuint index, const type *v));
THUNK(b, GLbyte, 127.0f);
THUNK(ub, GLubyte, 255.0f);
THUNK(s, GLshort, 32767.0f);
THUNK(us, GLushort, 65535.0f);
THUNK(i, GLint, 2147483647.0f);
THUNK(ui, GLuint, 4294967295.0f);
#undef THUNK
void APIENTRY_GL4ES gl4es_glVertexAttrib4Nub(GLuint index, GLubyte v0, GLubyte v1, GLubyte v2, GLubyte v3) {GLfloat f[4] = {0,0,0,1}; f[0] =v0/255.f; f[1]=v1/255.f; f[2]=v2/255.f; f[3]=v3/255.f; gl4es_glVertexAttrib4fv(index, f); };
AliasExport(void,glVertexAttrib4Nub,,(GLuint index, GLubyte v0, GLubyte v1, GLubyte v2, GLubyte v3));

// ============= GL_ARB_vertex_shader =================
AliasExport(GLvoid,glVertexAttrib1f,ARB,(GLuint index, GLfloat v0));
AliasExport(GLvoid,glVertexAttrib1s,ARB,(GLuint index, GLshort v0));
AliasExport_D_1(GLvoid,glVertexAttrib1d,ARB,(GLuint index, GLdouble v0));
AliasExport(GLvoid,glVertexAttrib2f,ARB,(GLuint index, GLfloat v0, GLfloat v1));
AliasExport(GLvoid,glVertexAttrib2s,ARB,(GLuint index, GLshort v0, GLshort v1));
AliasExport_D_1(GLvoid,glVertexAttrib2d,ARB,(GLuint index, GLdouble v0, GLdouble v1));
AliasExport(GLvoid,glVertexAttrib3f,ARB,(GLuint index, GLfloat v0, GLfloat v1, GLfloat v2));
AliasExport(GLvoid,glVertexAttrib3s,ARB,(GLuint index, GLshort v0, GLshort v1, GLshort v2));
AliasExport_D_1(GLvoid,glVertexAttrib3d,ARB,(GLuint index, GLdouble v0, GLdouble v1, GLdouble v2));
AliasExport(GLvoid,glVertexAttrib4s,ARB,(GLuint index, GLshort v0, GLshort v1, GLshort v2, GLshort v3));
AliasExport_D_1(GLvoid,glVertexAttrib4d,ARB,(GLuint index, GLdouble v0, GLdouble v1, GLdouble v2, GLdouble v3));
AliasExport(GLvoid,glVertexAttrib4Nub,ARB,(GLuint index, GLubyte x, GLubyte y, GLubyte z, GLubyte w));

AliasExport(GLvoid,glVertexAttrib1fv,ARB,(GLuint index, const GLfloat *v));
AliasExport(GLvoid,glVertexAttrib1sv,ARB,(GLuint index, const GLshort *v));
AliasExport(GLvoid,glVertexAttrib1dv,ARB,(GLuint index, const GLdouble *v));
AliasExport(GLvoid,glVertexAttrib2fv,ARB,(GLuint index, const GLfloat *v));
AliasExport(GLvoid,glVertexAttrib2sv,ARB,(GLuint index, const GLshort *v));
AliasExport(GLvoid,glVertexAttrib2dv,ARB,(GLuint index, const GLdouble *v));
AliasExport(GLvoid,glVertexAttrib3fv,ARB,(GLuint index, const GLfloat *v));
AliasExport(GLvoid,glVertexAttrib3sv,ARB,(GLuint index, const GLshort *v));
AliasExport(GLvoid,glVertexAttrib3dv,ARB,(GLuint index, const GLdouble *v));
AliasExport(GLvoid,glVertexAttrib4sv,ARB,(GLuint index, const GLshort *v));
AliasExport(GLvoid,glVertexAttrib4dv,ARB,(GLuint index, const GLdouble *v));
AliasExport(GLvoid,glVertexAttrib4iv,ARB,(GLuint index, const GLint *v));
AliasExport(GLvoid,glVertexAttrib4bv,ARB,(GLuint index, const GLbyte *v));

AliasExport(GLvoid,glVertexAttrib4ubv,ARB,(GLuint index, const GLubyte *v));
AliasExport(GLvoid,glVertexAttrib4usv,ARB,(GLuint index, const GLushort *v));
AliasExport(GLvoid,glVertexAttrib4uiv,ARB,(GLuint index, const GLuint *v));

AliasExport(GLvoid,glVertexAttrib4Nbv,ARB,(GLuint index, const GLbyte *v));
AliasExport(GLvoid,glVertexAttrib4Nsv,ARB,(GLuint index, const GLshort *v));
AliasExport(GLvoid,glVertexAttrib4Niv,ARB,(GLuint index, const GLint *v));
AliasExport(GLvoid,glVertexAttrib4Nubv,ARB,(GLuint index, const GLubyte *v));
AliasExport(GLvoid,glVertexAttrib4Nusv,ARB,(GLuint index, const GLushort *v));
AliasExport(GLvoid,glVertexAttrib4Nuiv,ARB,(GLuint index, const GLuint *v));


//Direct wrapper
AliasExport_D(void,glClearDepth,,(GLdouble depth));
AliasExport(void,glClipPlane,,(GLenum plane, const GLdouble *equation));
AliasExport_D(void,glDepthRange,,(GLdouble nearVal, GLdouble farVal));
AliasExport(void,glFogi,,(GLenum pname, GLint param));
AliasExport(void,glFogiv,,(GLenum pname, GLint *params));
AliasExport_D(void,glFrustum,,(GLdouble left, GLdouble right, GLdouble bottom, GLdouble top, GLdouble Near, GLdouble Far));
AliasExport(void,glLighti,,(GLenum light, GLenum pname, GLint param));
AliasExport(void,glLightiv,,(GLenum light, GLenum pname, GLint *iparams));
AliasExport(void,glLightModeli,,(GLenum pname, GLint param));
AliasExport(void,glLightModeliv,,(GLenum pname, GLint *iparams));
AliasExport(void,glMateriali,,(GLenum face, GLenum pname, GLint param));
AliasExport(void,glMaterialiv,,(GLenum face, GLenum pname, GLint *param));
AliasExport_D(void,glOrtho,,(GLdouble left, GLdouble right, GLdouble bottom, GLdouble top, GLdouble Near, GLdouble Far));
AliasExport(void,glGetMaterialiv,,(GLenum face, GLenum pname, GLint * params));
AliasExport(void,glGetLightiv,,(GLenum light, GLenum pname, GLint * params));
AliasExport(void,glGetClipPlane,,(GLenum plane, GLdouble *equation));
//AliasExport(void,glDrawRangeElements,,(GLenum mode,GLuint start,GLuint end,GLsizei count,GLenum type,const void *indices));
AliasExport(void,glColor3f,,(GLfloat r, GLfloat g, GLfloat b));
AliasExport(void,glColor3fv,,(GLfloat *c));
//AliasExport(void,glColor4fv,,(GLfloat *c));
AliasExport(void,glIndexfv,,(const GLfloat *c));
AliasExport(void,glSecondaryColor3fv,,(const GLfloat *v));
AliasExport(void,glRasterPos2f,,(GLfloat x, GLfloat y));
AliasExport(void,glRasterPos2fv,,(const GLfloat *v));
AliasExport(void,glRasterPos3fv,,(const GLfloat *v));
AliasExport(void,glRasterPos4f,,(GLfloat x, GLfloat y, GLfloat z, GLfloat w));
AliasExport(void,glRasterPos4fv,,(const GLfloat *v));
AliasExport(void,glWindowPos2f,,(GLfloat x, GLfloat y));
AliasExport(void,glWindowPos2fv,,(const GLfloat *v));
AliasExport(void,glWindowPos3fv,,(const GLfloat *v));
AliasExport(void,glPixelStoref,,(GLenum pname, GLfloat param));
AliasExport(void,glGetTexGendv,,(GLenum coord,GLenum pname,GLdouble *params));
AliasExport(void,glGetTexGeniv,,(GLenum coord,GLenum pname,GLint *params));
AliasExport(void,glPixelTransferi,,(GLenum pname, GLint param));
AliasExport_D(void,glEvalCoord1d,,(GLdouble u));
AliasExport(void,glEvalCoord1dv,,(GLdouble *v));
AliasExport(void,glEvalCoord1fv,,(GLfloat *v));
AliasExport_D(void,glEvalCoord2d,,(GLdouble u, GLdouble v));
AliasExport(void,glEvalCoord2dv,,(GLdouble *v));
AliasExport(void,glEvalCoord2fv,,(GLfloat *v));
AliasExport_D_1(void,glMapGrid1d,,(GLint un, GLdouble u1, GLdouble u2));
AliasExport_M(void,glMapGrid2d,,(GLint un, GLdouble u1, GLdouble u2, GLint vn, GLdouble v1, GLdouble v2),40);
AliasExport(void,glLoadMatrixd,,(const GLdouble *m));
AliasExport(void,glMultMatrixd,,(const GLdouble *m));
//AliasExport(void,glNormal3fv,,(GLfloat *v));

// rect
#define GL_RECT(suffix, type, M2)                                \
    AliasExport##M2(void,glRect##suffix,,(type x1, type y1, type x2, type y2)); \
    AliasExport(void,glRect##suffix##v,,(const type *v1, const type *v2));

GL_RECT(d, GLdouble, _D)
GL_RECT(f, GLfloat, )
GL_RECT(i, GLint, )
GL_RECT(s, GLshort, )
#undef GL_RECT

AliasExport(void,glTexCoord1f,,(GLfloat s));
AliasExport(void,glTexCoord1fv,,(GLfloat *t));
AliasExport(void,glTexCoord2f,,(GLfloat s, GLfloat t));
AliasExport(void,glTexCoord2fv,,(GLfloat *t));
AliasExport(void,glTexCoord3f,,(GLfloat s, GLfloat t, GLfloat r));
AliasExport(void,glTexCoord3fv,,(GLfloat *t));
AliasExport(void,glTexCoord4fv,,(GLfloat *t));
AliasExport(void,glMultiTexCoord1f,,(GLenum target, GLfloat s));
AliasExport(void,glMultiTexCoord1fv,,(GLenum target, GLfloat *t));
AliasExport(void,glMultiTexCoord2f,,(GLenum target, GLfloat s, GLfloat t));
//AliasExport(void,glMultiTexCoord2fv,,(GLenum target, GLfloat *t));
AliasExport(void,glMultiTexCoord3f,,(GLenum target, GLfloat s, GLfloat t, GLfloat r));
AliasExport(void,glMultiTexCoord3fv,,(GLenum target, GLfloat *t));
//AliasExport(void,glMultiTexCoord4fv,,(GLenum target, GLfloat *t));
AliasExport(void,glGetTexLevelParameteriv,,(GLenum target, GLint level, GLenum pname, GLfloat *params));
AliasExport_M(void,glTexGend,,(GLenum coord, GLenum pname, GLdouble param),16);
AliasExport(void,glTexGenf,,(GLenum coord, GLenum pname, GLfloat param));
AliasExport(void,glTexGendv,,(GLenum coord, GLenum pname, const GLdouble *params));
AliasExport(void,glTexGeniv,,(GLenum coord, GLenum pname, const GLint *params));
AliasExport_D(void,glRotated,,(GLdouble angle, GLdouble x, GLdouble y, GLdouble z));
AliasExport_D(void,glScaled,,(GLdouble x, GLdouble y, GLdouble z));
AliasExport_D(void,glTranslated,,(GLdouble x, GLdouble y, GLdouble z));
AliasExport(void,glVertex2f,,(GLfloat x, GLfloat y));
AliasExport(void,glVertex2fv,,(GLfloat *v));
AliasExport(void,glVertex3f,,(GLfloat r, GLfloat g, GLfloat b));
/*AliasExport(void,glVertex3fv,,(GLfloat *v));
AliasExport(void,glVertex4fv,,(GLfloat *v));*/

// basic thunking

#define THUNK(suffix, type, M2)                                \
AliasExport(void,glColor3##suffix##v,,(const type *v));               \
AliasExport##M2(void,glColor3##suffix,,(type r, type g, type b));         \
AliasExport(void,glColor4##suffix##v,,(const type *v));               \
AliasExport##M2(void,glColor4##suffix,,(type r, type g, type b, type a)); \
AliasExport(void,glSecondaryColor3##suffix##v,,(const type *v));      \
AliasExport##M2(void,glSecondaryColor3##suffix,,(type r, type g, type b));\
AliasExport(void,glIndex##suffix##v,,(const type *c));                \
AliasExport##M2(void,glIndex##suffix,,(type c));                          \
AliasExport(void,glNormal3##suffix##v,,(const type *v));              \
AliasExport##M2(void,glNormal3##suffix,,(type x, type y, type z));        \
AliasExport(void,glRasterPos2##suffix##v,,(type *v));                 \
AliasExport##M2(void,glRasterPos2##suffix,,(type x, type y));             \
AliasExport(void,glRasterPos3##suffix##v,,(type *v));                 \
AliasExport##M2(void,glRasterPos3##suffix,,(type x, type y, type z));     \
AliasExport(void,glRasterPos4##suffix##v,,(type *v));                 \
AliasExport##M2(void,glRasterPos4##suffix,,(type x, type y, type z, type w));\
AliasExport(void,glWindowPos2##suffix##v,,(type *v));                 \
AliasExport##M2(void,glWindowPos2##suffix,,(type x, type y));             \
AliasExport(void,glWindowPos3##suffix##v,,(type *v));                 \
AliasExport##M2(void,glWindowPos3##suffix,,(type x, type y, type z));     \
AliasExport(void,glVertex2##suffix##v,,(type *v));                    \
AliasExport##M2(void,glVertex2##suffix,,(type x, type y));                \
AliasExport(void,glVertex3##suffix##v,,(type *v));                    \
AliasExport##M2(void,glVertex3##suffix,,(type x, type y, type z));        \
AliasExport##M2(void,glVertex4##suffix,,(type x, type y, type z, type w));\
AliasExport(void,glVertex4##suffix##v,,(type *v));                    \
AliasExport##M2(void,glTexCoord1##suffix,,(type s));                      \
AliasExport(void,glTexCoord1##suffix##v,,(type *t));                  \
AliasExport##M2(void,glTexCoord2##suffix,,(type s, type t));              \
AliasExport(void,glTexCoord2##suffix##v,,(type *t));                  \
AliasExport##M2(void,glTexCoord3##suffix,,(type s, type t, type r));      \
AliasExport(void,glTexCoord3##suffix##v,,(type *t));                  \
AliasExport##M2(void,glTexCoord4##suffix,,(type s, type t, type r, type q));          \
AliasExport(void,glTexCoord4##suffix##v,,(type *t));                              \
AliasExport##M2##_1(void,glMultiTexCoord1##suffix,,(GLenum target, type s));              \
AliasExport(void,glMultiTexCoord1##suffix##v,,(GLenum target, type *t));          \
AliasExport##M2##_1(void,glMultiTexCoord2##suffix,,(GLenum target, type s, type t));      \
AliasExport(void,glMultiTexCoord2##suffix##v,,(GLenum target, type *t));          \
AliasExport##M2##_1(void,glMultiTexCoord3##suffix,,(GLenum target, type s, type t, type r));\
AliasExport(void,glMultiTexCoord3##suffix##v,,(GLenum target, type *t));          \
AliasExport##M2##_1(void,glMultiTexCoord4##suffix,,(GLenum target, type s, type t, type r, type q));\
AliasExport(void,glMultiTexCoord4##suffix##v,,(GLenum target, type *t));          \
AliasExport##M2##_1(void,glMultiTexCoord1##suffix,ARB,(GLenum target, type s));         \
AliasExport(void,glMultiTexCoord1##suffix##v,ARB,(GLenum target, type *t));       \
AliasExport##M2##_1(void,glMultiTexCoord2##suffix,ARB,(GLenum target, type s, type t)); \
AliasExport(void,glMultiTexCoord2##suffix##v,ARB,(GLenum target, type *t));       \
AliasExport##M2##_1(void,glMultiTexCoord3##suffix,ARB,(GLenum target, type s, type t, type r));\
AliasExport(void,glMultiTexCoord3##suffix##v,ARB,(GLenum target, type *t));       \
AliasExport##M2##_1(void,glMultiTexCoord4##suffix,ARB,(GLenum target, type s, type t, type r, type q));\
AliasExport(void,glMultiTexCoord4##suffix##v,ARB,(GLenum target, type *t));

THUNK(b, GLbyte, )
THUNK(d, GLdouble, _D)
THUNK(i, GLint, )
THUNK(s, GLshort, )
THUNK(ub, GLubyte, )
THUNK(ui, GLuint, )
THUNK(us, GLushort, )
#undef THUNK

AliasExport(void,glMultiTexCoord1f,ARB,(GLenum target, GLfloat s));
AliasExport(void,glMultiTexCoord2f,ARB,(GLenum target, GLfloat s, GLfloat t));
AliasExport(void,glMultiTexCoord3f,ARB,(GLenum target, GLfloat s, GLfloat t, GLfloat r));
AliasExport(void,glMultiTexCoord1fv,ARB,(GLenum target, GLfloat *t));
//AliasExport(void,glMultiTexCoord2fv,ARB,(GLenum target, GLfloat *t));
AliasExport(void,glMultiTexCoord3fv,ARB,(GLenum target, GLfloat *t));
//AliasExport(void,glMultiTexCoord4fv,ARB,(GLenum target, GLfloat *t));
//AliasExport(void,glDrawRangeElements,EXT,(GLenum mode,GLuint start,GLuint end,GLsizei count,GLenum type,const void *indices));

AliasExport(void,glGetTexParameterfv,,(GLenum target, GLenum pname, GLfloat * params));
AliasExport(void,glGetTexParameteriv,,(GLenum target, GLenum pname, GLint * params));
