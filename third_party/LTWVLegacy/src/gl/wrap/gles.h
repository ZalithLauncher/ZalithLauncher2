#include "../gles.h"

#ifndef GLESWRAP_H
#define GLESWRAP_H

typedef struct {
    int format;
    void *func;
    void *args;
} packed_call_t;

typedef struct {
    int func;
    void *args;
} indexed_call_t;

enum FORMAT {
    FORMAT_void_GLenum,
    FORMAT_void_GLenum_GLclampf,
    FORMAT_void_GLenum_GLclampx,
    FORMAT_void_GLuint_GLuint,
    FORMAT_void_GLuint_GLuint_const_GLchar___GENPT__,
    FORMAT_void_GLenum_GLuint,
    FORMAT_void_GLclampf_GLclampf_GLclampf_GLclampf,
    FORMAT_void_GLenum_GLenum,
    FORMAT_void_GLenum_GLenum_GLenum_GLenum,
    FORMAT_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum,
    FORMAT_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__,
    FORMAT_GLenum_GLenum,
    FORMAT_void_GLbitfield,
    FORMAT_void_GLclampx_GLclampx_GLclampx_GLclampx,
    FORMAT_void_GLclampf,
    FORMAT_void_GLclampx,
    FORMAT_void_GLint,
    FORMAT_void_GLenum_const_GLfloat___GENPT__,
    FORMAT_void_GLenum_const_GLfixed___GENPT__,
    FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLubyte_GLubyte_GLubyte_GLubyte,
    FORMAT_void_GLfixed_GLfixed_GLfixed_GLfixed,
    FORMAT_void_GLboolean_GLboolean_GLboolean_GLboolean,
    FORMAT_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__,
    FORMAT_void_GLuint,
    FORMAT_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__,
    FORMAT_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__,
    FORMAT_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint,
    FORMAT_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei,
    FORMAT_GLuint,
    FORMAT_GLuint_GLenum,
    FORMAT_void_GLsizei_const_GLuint___GENPT__,
    FORMAT_void_GLsizei_GLuint___GENPT__,
    FORMAT_void_GLboolean,
    FORMAT_void_GLclampf_GLclampf,
    FORMAT_void_GLclampx_GLclampx,
    FORMAT_void_GLenum_GLint_GLsizei,
    FORMAT_void_GLsizei_const_GLenum___GENPT__,
    FORMAT_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__,
    FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLint_GLint_GLint_GLint_GLint,
    FORMAT_void,
    FORMAT_void_GLenum_GLsizei_const_GLvoid___GENPT__,
    FORMAT_void_GLfloat,
    FORMAT_void_const_GLfloat___GENPT__,
    FORMAT_void_GLenum_GLfloat,
    FORMAT_void_GLenum_GLfixed,
    FORMAT_void_GLenum_GLenum_GLenum_GLuint,
    FORMAT_void_GLenum_GLenum_GLenum_GLuint_GLint,
    FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed,
    FORMAT_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__,
    FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__,
    FORMAT_GLint_GLuint_const_GLchar___GENPT__,
    FORMAT_void_GLenum_GLboolean___GENPT__,
    FORMAT_void_GLenum_GLenum_GLint___GENPT__,
    FORMAT_void_GLenum_GLfloat___GENPT__,
    FORMAT_void_GLenum_GLfixed___GENPT__,
    FORMAT_GLenum,
    FORMAT_void_GLenum_GLenum_GLenum_GLint___GENPT__,
    FORMAT_void_GLenum_GLint___GENPT__,
    FORMAT_void_GLenum_GLenum_GLfloat___GENPT__,
    FORMAT_void_GLenum_GLenum_GLfixed___GENPT__,
    FORMAT_void_GLenum_GLvoid___GENPT____GENPT__,
    FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__,
    FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__,
    FORMAT_void_GLuint_GLenum_GLint___GENPT__,
    FORMAT_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__,
    FORMAT_const_GLubyte___GENPT___GLenum,
    FORMAT_void_GLuint_GLint_GLfloat___GENPT__,
    FORMAT_void_GLuint_GLint_GLint___GENPT__,
    FORMAT_void_GLuint_GLenum_GLvoid___GENPT____GENPT__,
    FORMAT_void_GLuint_GLenum_GLfloat___GENPT__,
    FORMAT_GLboolean_GLuint,
    FORMAT_GLboolean_GLenum,
    FORMAT_void_GLenum_GLenum_GLfloat,
    FORMAT_void_GLenum_GLenum_const_GLfloat___GENPT__,
    FORMAT_void_GLenum_GLenum_GLfixed,
    FORMAT_void_GLenum_GLenum_const_GLfixed___GENPT__,
    FORMAT_void_GLfixed,
    FORMAT_void_const_GLfixed___GENPT__,
    FORMAT_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei,
    FORMAT_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei,
    FORMAT_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed,
    FORMAT_void_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLfixed_GLfixed_GLfixed,
    FORMAT_void_GLenum_GLint,
    FORMAT_void_GLfloat_GLfloat,
    FORMAT_void_GLfixed_GLfixed,
    FORMAT_void_GLuint_GLenum_const_GLvoid___GENPT___GLint,
    FORMAT_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__,
    FORMAT_void_GLenum_GLenum_GLsizei_GLsizei,
    FORMAT_void_GLclampf_GLboolean,
    FORMAT_void_GLclampx_GLboolean,
    FORMAT_void_GLint_GLint_GLsizei_GLsizei,
    FORMAT_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei,
    FORMAT_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__,
    FORMAT_void_GLenum_GLint_GLuint,
    FORMAT_void_GLenum_GLenum_GLint_GLuint,
    FORMAT_void_GLenum_GLenum_GLenum,
    FORMAT_void_GLenum_GLenum_GLint,
    FORMAT_void_GLenum_GLenum_const_GLint___GENPT__,
    FORMAT_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__,
    FORMAT_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__,
    FORMAT_void_GLint_GLfloat,
    FORMAT_void_GLint_GLsizei_const_GLfloat___GENPT__,
    FORMAT_void_GLint_GLint,
    FORMAT_void_GLint_GLsizei_const_GLint___GENPT__,
    FORMAT_void_GLint_GLfloat_GLfloat,
    FORMAT_void_GLint_GLint_GLint,
    FORMAT_void_GLint_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLint_GLint_GLint_GLint,
    FORMAT_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__,
    FORMAT_void_GLuint_GLfloat,
    FORMAT_void_GLuint_const_GLfloat___GENPT__,
    FORMAT_void_GLuint_GLfloat_GLfloat,
    FORMAT_void_GLuint_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat,
    FORMAT_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__,
};

typedef void (APIENTRY*FUNC_void_GLenum)(GLenum texture);
typedef struct {
    GLenum a1;
} ARGS_void_GLenum;
typedef struct {
    int format;
    FUNC_void_GLenum func;
    ARGS_void_GLenum args;
} PACKED_void_GLenum;
typedef struct {
    int func;
    ARGS_void_GLenum args;
} INDEXED_void_GLenum;
typedef void (APIENTRY*FUNC_void_GLenum_GLclampf)(GLenum func, GLclampf ref);
typedef struct {
    GLenum a1;
    GLclampf a2;
} ARGS_void_GLenum_GLclampf;
typedef struct {
    int format;
    FUNC_void_GLenum_GLclampf func;
    ARGS_void_GLenum_GLclampf args;
} PACKED_void_GLenum_GLclampf;
typedef struct {
    int func;
    ARGS_void_GLenum_GLclampf args;
} INDEXED_void_GLenum_GLclampf;
typedef void (APIENTRY*FUNC_void_GLenum_GLclampx)(GLenum func, GLclampx ref);
typedef struct {
    GLenum a1;
    GLclampx a2;
} ARGS_void_GLenum_GLclampx;
typedef struct {
    int format;
    FUNC_void_GLenum_GLclampx func;
    ARGS_void_GLenum_GLclampx args;
} PACKED_void_GLenum_GLclampx;
typedef struct {
    int func;
    ARGS_void_GLenum_GLclampx args;
} INDEXED_void_GLenum_GLclampx;
typedef void (APIENTRY*FUNC_void_GLuint_GLuint)(GLuint program, GLuint shader);
typedef struct {
    GLuint a1;
    GLuint a2;
} ARGS_void_GLuint_GLuint;
typedef struct {
    int format;
    FUNC_void_GLuint_GLuint func;
    ARGS_void_GLuint_GLuint args;
} PACKED_void_GLuint_GLuint;
typedef struct {
    int func;
    ARGS_void_GLuint_GLuint args;
} INDEXED_void_GLuint_GLuint;
typedef void (APIENTRY*FUNC_void_GLuint_GLuint_const_GLchar___GENPT__)(GLuint program, GLuint index, const GLchar * name);
typedef struct {
    GLuint a1;
    GLuint a2;
    GLchar * a3;
} ARGS_void_GLuint_GLuint_const_GLchar___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLuint_const_GLchar___GENPT__ func;
    ARGS_void_GLuint_GLuint_const_GLchar___GENPT__ args;
} PACKED_void_GLuint_GLuint_const_GLchar___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLuint_const_GLchar___GENPT__ args;
} INDEXED_void_GLuint_GLuint_const_GLchar___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLuint)(GLenum target, GLuint buffer);
typedef struct {
    GLenum a1;
    GLuint a2;
} ARGS_void_GLenum_GLuint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLuint func;
    ARGS_void_GLenum_GLuint args;
} PACKED_void_GLenum_GLuint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLuint args;
} INDEXED_void_GLenum_GLuint;
typedef void (APIENTRY*FUNC_void_GLclampf_GLclampf_GLclampf_GLclampf)(GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha);
typedef struct {
    GLclampf a1;
    GLclampf a2;
    GLclampf a3;
    GLclampf a4;
} ARGS_void_GLclampf_GLclampf_GLclampf_GLclampf;
typedef struct {
    int format;
    FUNC_void_GLclampf_GLclampf_GLclampf_GLclampf func;
    ARGS_void_GLclampf_GLclampf_GLclampf_GLclampf args;
} PACKED_void_GLclampf_GLclampf_GLclampf_GLclampf;
typedef struct {
    int func;
    ARGS_void_GLclampf_GLclampf_GLclampf_GLclampf args;
} INDEXED_void_GLclampf_GLclampf_GLclampf_GLclampf;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum)(GLenum modeRGB, GLenum modeA);
typedef struct {
    GLenum a1;
    GLenum a2;
} ARGS_void_GLenum_GLenum;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum func;
    ARGS_void_GLenum_GLenum args;
} PACKED_void_GLenum_GLenum;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum args;
} INDEXED_void_GLenum_GLenum;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLenum_GLenum)(GLenum sfactorRGB, GLenum dfactorRGB, GLenum sfactorAlpha, GLenum dfactorAlpha);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLenum a3;
    GLenum a4;
} ARGS_void_GLenum_GLenum_GLenum_GLenum;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLenum_GLenum func;
    ARGS_void_GLenum_GLenum_GLenum_GLenum args;
} PACKED_void_GLenum_GLenum_GLenum_GLenum;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLenum_GLenum args;
} INDEXED_void_GLenum_GLenum_GLenum_GLenum;
typedef void (APIENTRY*FUNC_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum)(GLenum target, GLsizeiptr size, const GLvoid * data, GLenum usage);
typedef struct {
    GLenum a1;
    GLsizeiptr a2;
    GLvoid * a3;
    GLenum a4;
} ARGS_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum;
typedef struct {
    int format;
    FUNC_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum func;
    ARGS_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum args;
} PACKED_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum;
typedef struct {
    int func;
    ARGS_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum args;
} INDEXED_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum;
typedef void (APIENTRY*FUNC_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__)(GLenum target, GLintptr offset, GLsizeiptr size, const GLvoid * data);
typedef struct {
    GLenum a1;
    GLintptr a2;
    GLsizeiptr a3;
    GLvoid * a4;
} ARGS_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__ func;
    ARGS_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__ args;
} PACKED_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__ args;
} INDEXED_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__;
typedef GLenum (APIENTRY*FUNC_GLenum_GLenum)(GLenum target);
typedef struct {
    GLenum a1;
} ARGS_GLenum_GLenum;
typedef struct {
    int format;
    FUNC_GLenum_GLenum func;
    ARGS_GLenum_GLenum args;
} PACKED_GLenum_GLenum;
typedef struct {
    int func;
    ARGS_GLenum_GLenum args;
} INDEXED_GLenum_GLenum;
typedef void (APIENTRY*FUNC_void_GLbitfield)(GLbitfield mask);
typedef struct {
    GLbitfield a1;
} ARGS_void_GLbitfield;
typedef struct {
    int format;
    FUNC_void_GLbitfield func;
    ARGS_void_GLbitfield args;
} PACKED_void_GLbitfield;
typedef struct {
    int func;
    ARGS_void_GLbitfield args;
} INDEXED_void_GLbitfield;
typedef void (APIENTRY*FUNC_void_GLclampx_GLclampx_GLclampx_GLclampx)(GLclampx red, GLclampx green, GLclampx blue, GLclampx alpha);
typedef struct {
    GLclampx a1;
    GLclampx a2;
    GLclampx a3;
    GLclampx a4;
} ARGS_void_GLclampx_GLclampx_GLclampx_GLclampx;
typedef struct {
    int format;
    FUNC_void_GLclampx_GLclampx_GLclampx_GLclampx func;
    ARGS_void_GLclampx_GLclampx_GLclampx_GLclampx args;
} PACKED_void_GLclampx_GLclampx_GLclampx_GLclampx;
typedef struct {
    int func;
    ARGS_void_GLclampx_GLclampx_GLclampx_GLclampx args;
} INDEXED_void_GLclampx_GLclampx_GLclampx_GLclampx;
typedef void (APIENTRY*FUNC_void_GLclampf)(GLclampf depth);
typedef struct {
    GLclampf a1;
} ARGS_void_GLclampf;
typedef struct {
    int format;
    FUNC_void_GLclampf func;
    ARGS_void_GLclampf args;
} PACKED_void_GLclampf;
typedef struct {
    int func;
    ARGS_void_GLclampf args;
} INDEXED_void_GLclampf;
typedef void (APIENTRY*FUNC_void_GLclampx)(GLclampx depth);
typedef struct {
    GLclampx a1;
} ARGS_void_GLclampx;
typedef struct {
    int format;
    FUNC_void_GLclampx func;
    ARGS_void_GLclampx args;
} PACKED_void_GLclampx;
typedef struct {
    int func;
    ARGS_void_GLclampx args;
} INDEXED_void_GLclampx;
typedef void (APIENTRY*FUNC_void_GLint)(GLint s);
typedef struct {
    GLint a1;
} ARGS_void_GLint;
typedef struct {
    int format;
    FUNC_void_GLint func;
    ARGS_void_GLint args;
} PACKED_void_GLint;
typedef struct {
    int func;
    ARGS_void_GLint args;
} INDEXED_void_GLint;
typedef void (APIENTRY*FUNC_void_GLenum_const_GLfloat___GENPT__)(GLenum plane, const GLfloat * equation);
typedef struct {
    GLenum a1;
    GLfloat * a2;
} ARGS_void_GLenum_const_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_const_GLfloat___GENPT__ func;
    ARGS_void_GLenum_const_GLfloat___GENPT__ args;
} PACKED_void_GLenum_const_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_const_GLfloat___GENPT__ args;
} INDEXED_void_GLenum_const_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_const_GLfixed___GENPT__)(GLenum plane, const GLfixed * equation);
typedef struct {
    GLenum a1;
    GLfixed * a2;
} ARGS_void_GLenum_const_GLfixed___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_const_GLfixed___GENPT__ func;
    ARGS_void_GLenum_const_GLfixed___GENPT__ args;
} PACKED_void_GLenum_const_GLfixed___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_const_GLfixed___GENPT__ args;
} INDEXED_void_GLenum_const_GLfixed___GENPT__;
typedef void (APIENTRY*FUNC_void_GLfloat_GLfloat_GLfloat_GLfloat)(GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha);
typedef struct {
    GLfloat a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
} ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLfloat_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLubyte_GLubyte_GLubyte_GLubyte)(GLubyte red, GLubyte green, GLubyte blue, GLubyte alpha);
typedef struct {
    GLubyte a1;
    GLubyte a2;
    GLubyte a3;
    GLubyte a4;
} ARGS_void_GLubyte_GLubyte_GLubyte_GLubyte;
typedef struct {
    int format;
    FUNC_void_GLubyte_GLubyte_GLubyte_GLubyte func;
    ARGS_void_GLubyte_GLubyte_GLubyte_GLubyte args;
} PACKED_void_GLubyte_GLubyte_GLubyte_GLubyte;
typedef struct {
    int func;
    ARGS_void_GLubyte_GLubyte_GLubyte_GLubyte args;
} INDEXED_void_GLubyte_GLubyte_GLubyte_GLubyte;
typedef void (APIENTRY*FUNC_void_GLfixed_GLfixed_GLfixed_GLfixed)(GLfixed red, GLfixed green, GLfixed blue, GLfixed alpha);
typedef struct {
    GLfixed a1;
    GLfixed a2;
    GLfixed a3;
    GLfixed a4;
} ARGS_void_GLfixed_GLfixed_GLfixed_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLfixed_GLfixed_GLfixed_GLfixed func;
    ARGS_void_GLfixed_GLfixed_GLfixed_GLfixed args;
} PACKED_void_GLfixed_GLfixed_GLfixed_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLfixed_GLfixed_GLfixed_GLfixed args;
} INDEXED_void_GLfixed_GLfixed_GLfixed_GLfixed;
typedef void (APIENTRY*FUNC_void_GLboolean_GLboolean_GLboolean_GLboolean)(GLboolean red, GLboolean green, GLboolean blue, GLboolean alpha);
typedef struct {
    GLboolean a1;
    GLboolean a2;
    GLboolean a3;
    GLboolean a4;
} ARGS_void_GLboolean_GLboolean_GLboolean_GLboolean;
typedef struct {
    int format;
    FUNC_void_GLboolean_GLboolean_GLboolean_GLboolean func;
    ARGS_void_GLboolean_GLboolean_GLboolean_GLboolean args;
} PACKED_void_GLboolean_GLboolean_GLboolean_GLboolean;
typedef struct {
    int func;
    ARGS_void_GLboolean_GLboolean_GLboolean_GLboolean args;
} INDEXED_void_GLboolean_GLboolean_GLboolean_GLboolean;
typedef void (APIENTRY*FUNC_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__)(GLint size, GLenum type, GLsizei stride, const GLvoid * pointer);
typedef struct {
    GLint a1;
    GLenum a2;
    GLsizei a3;
    GLvoid * a4;
} ARGS_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__ func;
    ARGS_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__ args;
} PACKED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__ args;
} INDEXED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint)(GLuint shader);
typedef struct {
    GLuint a1;
} ARGS_void_GLuint;
typedef struct {
    int format;
    FUNC_void_GLuint func;
    ARGS_void_GLuint args;
} PACKED_void_GLuint;
typedef struct {
    int func;
    ARGS_void_GLuint args;
} INDEXED_void_GLuint;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__)(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLint border, GLsizei imageSize, const GLvoid * data);
typedef struct {
    GLenum a1;
    GLint a2;
    GLenum a3;
    GLsizei a4;
    GLsizei a5;
    GLint a6;
    GLsizei a7;
    GLvoid * a8;
} ARGS_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__ func;
    ARGS_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__ args;
} PACKED_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__ args;
} INDEXED_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLsizei imageSize, const GLvoid * data);
typedef struct {
    GLenum a1;
    GLint a2;
    GLint a3;
    GLint a4;
    GLsizei a5;
    GLsizei a6;
    GLenum a7;
    GLsizei a8;
    GLvoid * a9;
} ARGS_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__ func;
    ARGS_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__ args;
} PACKED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__ args;
} INDEXED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint)(GLenum target, GLint level, GLenum internalformat, GLint x, GLint y, GLsizei width, GLsizei height, GLint border);
typedef struct {
    GLenum a1;
    GLint a2;
    GLenum a3;
    GLint a4;
    GLint a5;
    GLsizei a6;
    GLsizei a7;
    GLint a8;
} ARGS_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint func;
    ARGS_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint args;
} PACKED_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint args;
} INDEXED_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint x, GLint y, GLsizei width, GLsizei height);
typedef struct {
    GLenum a1;
    GLint a2;
    GLint a3;
    GLint a4;
    GLint a5;
    GLint a6;
    GLsizei a7;
    GLsizei a8;
} ARGS_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei func;
    ARGS_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei args;
} PACKED_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei args;
} INDEXED_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei;
typedef GLuint (APIENTRY*FUNC_GLuint)();
typedef struct {
    int format;
    FUNC_GLuint func;
} PACKED_GLuint;
typedef struct {
    int func;
} INDEXED_GLuint;
typedef GLuint (APIENTRY*FUNC_GLuint_GLenum)(GLenum type);
typedef struct {
    GLenum a1;
} ARGS_GLuint_GLenum;
typedef struct {
    int format;
    FUNC_GLuint_GLenum func;
    ARGS_GLuint_GLenum args;
} PACKED_GLuint_GLenum;
typedef struct {
    int func;
    ARGS_GLuint_GLenum args;
} INDEXED_GLuint_GLenum;
typedef void (APIENTRY*FUNC_void_GLsizei_const_GLuint___GENPT__)(GLsizei n, const GLuint * buffer);
typedef struct {
    GLsizei a1;
    GLuint * a2;
} ARGS_void_GLsizei_const_GLuint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLsizei_const_GLuint___GENPT__ func;
    ARGS_void_GLsizei_const_GLuint___GENPT__ args;
} PACKED_void_GLsizei_const_GLuint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLsizei_const_GLuint___GENPT__ args;
} INDEXED_void_GLsizei_const_GLuint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLsizei_GLuint___GENPT__)(GLsizei n, GLuint * framebuffers);
typedef struct {
    GLsizei a1;
    GLuint * a2;
} ARGS_void_GLsizei_GLuint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLsizei_GLuint___GENPT__ func;
    ARGS_void_GLsizei_GLuint___GENPT__ args;
} PACKED_void_GLsizei_GLuint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLsizei_GLuint___GENPT__ args;
} INDEXED_void_GLsizei_GLuint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLboolean)(GLboolean flag);
typedef struct {
    GLboolean a1;
} ARGS_void_GLboolean;
typedef struct {
    int format;
    FUNC_void_GLboolean func;
    ARGS_void_GLboolean args;
} PACKED_void_GLboolean;
typedef struct {
    int func;
    ARGS_void_GLboolean args;
} INDEXED_void_GLboolean;
typedef void (APIENTRY*FUNC_void_GLclampf_GLclampf)(GLclampf Near, GLclampf Far);
typedef struct {
    GLclampf a1;
    GLclampf a2;
} ARGS_void_GLclampf_GLclampf;
typedef struct {
    int format;
    FUNC_void_GLclampf_GLclampf func;
    ARGS_void_GLclampf_GLclampf args;
} PACKED_void_GLclampf_GLclampf;
typedef struct {
    int func;
    ARGS_void_GLclampf_GLclampf args;
} INDEXED_void_GLclampf_GLclampf;
typedef void (APIENTRY*FUNC_void_GLclampx_GLclampx)(GLclampx Near, GLclampx Far);
typedef struct {
    GLclampx a1;
    GLclampx a2;
} ARGS_void_GLclampx_GLclampx;
typedef struct {
    int format;
    FUNC_void_GLclampx_GLclampx func;
    ARGS_void_GLclampx_GLclampx args;
} PACKED_void_GLclampx_GLclampx;
typedef struct {
    int func;
    ARGS_void_GLclampx_GLclampx args;
} INDEXED_void_GLclampx_GLclampx;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLsizei)(GLenum mode, GLint first, GLsizei count);
typedef struct {
    GLenum a1;
    GLint a2;
    GLsizei a3;
} ARGS_void_GLenum_GLint_GLsizei;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLsizei func;
    ARGS_void_GLenum_GLint_GLsizei args;
} PACKED_void_GLenum_GLint_GLsizei;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLsizei args;
} INDEXED_void_GLenum_GLint_GLsizei;
typedef void (APIENTRY*FUNC_void_GLsizei_const_GLenum___GENPT__)(GLsizei n, const GLenum * bufs);
typedef struct {
    GLsizei a1;
    GLenum * a2;
} ARGS_void_GLsizei_const_GLenum___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLsizei_const_GLenum___GENPT__ func;
    ARGS_void_GLsizei_const_GLenum___GENPT__ args;
} PACKED_void_GLsizei_const_GLenum___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLsizei_const_GLenum___GENPT__ args;
} INDEXED_void_GLsizei_const_GLenum___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__)(GLenum mode, GLsizei count, GLenum type, const GLvoid * indices);
typedef struct {
    GLenum a1;
    GLsizei a2;
    GLenum a3;
    GLvoid * a4;
} ARGS_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__ func;
    ARGS_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__ args;
} PACKED_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__ args;
} INDEXED_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat)(GLfloat x, GLfloat y, GLfloat z, GLfloat width, GLfloat height);
typedef struct {
    GLfloat a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
    GLfloat a5;
} ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLint_GLint_GLint_GLint_GLint)(GLint x, GLint y, GLint z, GLint width, GLint height);
typedef struct {
    GLint a1;
    GLint a2;
    GLint a3;
    GLint a4;
    GLint a5;
} ARGS_void_GLint_GLint_GLint_GLint_GLint;
typedef struct {
    int format;
    FUNC_void_GLint_GLint_GLint_GLint_GLint func;
    ARGS_void_GLint_GLint_GLint_GLint_GLint args;
} PACKED_void_GLint_GLint_GLint_GLint_GLint;
typedef struct {
    int func;
    ARGS_void_GLint_GLint_GLint_GLint_GLint args;
} INDEXED_void_GLint_GLint_GLint_GLint_GLint;
typedef void (APIENTRY*FUNC_void)();
typedef struct {
    int format;
    FUNC_void func;
} PACKED_void;
typedef struct {
    int func;
} INDEXED_void;
typedef void (APIENTRY*FUNC_void_GLenum_GLsizei_const_GLvoid___GENPT__)(GLenum type, GLsizei stride, const GLvoid * pointer);
typedef struct {
    GLenum a1;
    GLsizei a2;
    GLvoid * a3;
} ARGS_void_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLsizei_const_GLvoid___GENPT__ func;
    ARGS_void_GLenum_GLsizei_const_GLvoid___GENPT__ args;
} PACKED_void_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLsizei_const_GLvoid___GENPT__ args;
} INDEXED_void_GLenum_GLsizei_const_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLfloat)(GLfloat coord);
typedef struct {
    GLfloat a1;
} ARGS_void_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLfloat func;
    ARGS_void_GLfloat args;
} PACKED_void_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLfloat args;
} INDEXED_void_GLfloat;
typedef void (APIENTRY*FUNC_void_const_GLfloat___GENPT__)(const GLfloat * coord);
typedef struct {
    GLfloat * a1;
} ARGS_void_const_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_const_GLfloat___GENPT__ func;
    ARGS_void_const_GLfloat___GENPT__ args;
} PACKED_void_const_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_const_GLfloat___GENPT__ args;
} INDEXED_void_const_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLfloat)(GLenum pname, GLfloat param);
typedef struct {
    GLenum a1;
    GLfloat a2;
} ARGS_void_GLenum_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLenum_GLfloat func;
    ARGS_void_GLenum_GLfloat args;
} PACKED_void_GLenum_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLenum_GLfloat args;
} INDEXED_void_GLenum_GLfloat;
typedef void (APIENTRY*FUNC_void_GLenum_GLfixed)(GLenum pname, GLfixed param);
typedef struct {
    GLenum a1;
    GLfixed a2;
} ARGS_void_GLenum_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLenum_GLfixed func;
    ARGS_void_GLenum_GLfixed args;
} PACKED_void_GLenum_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLenum_GLfixed args;
} INDEXED_void_GLenum_GLfixed;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLenum_GLuint)(GLenum target, GLenum attachment, GLenum renderbuffertarget, GLuint renderbuffer);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLenum a3;
    GLuint a4;
} ARGS_void_GLenum_GLenum_GLenum_GLuint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLenum_GLuint func;
    ARGS_void_GLenum_GLenum_GLenum_GLuint args;
} PACKED_void_GLenum_GLenum_GLenum_GLuint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLenum_GLuint args;
} INDEXED_void_GLenum_GLenum_GLenum_GLuint;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLenum_GLuint_GLint)(GLenum target, GLenum attachment, GLenum textarget, GLuint texture, GLint level);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLenum a3;
    GLuint a4;
    GLint a5;
} ARGS_void_GLenum_GLenum_GLenum_GLuint_GLint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLenum_GLuint_GLint func;
    ARGS_void_GLenum_GLenum_GLenum_GLuint_GLint args;
} PACKED_void_GLenum_GLenum_GLenum_GLuint_GLint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLenum_GLuint_GLint args;
} INDEXED_void_GLenum_GLenum_GLenum_GLuint_GLint;
typedef void (APIENTRY*FUNC_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat)(GLfloat left, GLfloat right, GLfloat bottom, GLfloat top, GLfloat Near, GLfloat Far);
typedef struct {
    GLfloat a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
    GLfloat a5;
    GLfloat a6;
} ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed)(GLfixed left, GLfixed right, GLfixed bottom, GLfixed top, GLfixed Near, GLfixed Far);
typedef struct {
    GLfixed a1;
    GLfixed a2;
    GLfixed a3;
    GLfixed a4;
    GLfixed a5;
    GLfixed a6;
} ARGS_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed func;
    ARGS_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed args;
} PACKED_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed args;
} INDEXED_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed;
typedef void (APIENTRY*FUNC_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__)(GLuint program, GLuint index, GLsizei bufSize, GLsizei * length, GLint * size, GLenum * type, GLchar * name);
typedef struct {
    GLuint a1;
    GLuint a2;
    GLsizei a3;
    GLsizei * a4;
    GLint * a5;
    GLenum * a6;
    GLchar * a7;
} ARGS_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__ func;
    ARGS_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__ args;
} PACKED_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__ args;
} INDEXED_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__)(GLuint program, GLsizei maxCount, GLsizei * count, GLuint * obj);
typedef struct {
    GLuint a1;
    GLsizei a2;
    GLsizei * a3;
    GLuint * a4;
} ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__ func;
    ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__ args;
} PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__ args;
} INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__;
typedef GLint (APIENTRY*FUNC_GLint_GLuint_const_GLchar___GENPT__)(GLuint program, const GLchar * name);
typedef struct {
    GLuint a1;
    GLchar * a2;
} ARGS_GLint_GLuint_const_GLchar___GENPT__;
typedef struct {
    int format;
    FUNC_GLint_GLuint_const_GLchar___GENPT__ func;
    ARGS_GLint_GLuint_const_GLchar___GENPT__ args;
} PACKED_GLint_GLuint_const_GLchar___GENPT__;
typedef struct {
    int func;
    ARGS_GLint_GLuint_const_GLchar___GENPT__ args;
} INDEXED_GLint_GLuint_const_GLchar___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLboolean___GENPT__)(GLenum pname, GLboolean * params);
typedef struct {
    GLenum a1;
    GLboolean * a2;
} ARGS_void_GLenum_GLboolean___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLboolean___GENPT__ func;
    ARGS_void_GLenum_GLboolean___GENPT__ args;
} PACKED_void_GLenum_GLboolean___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLboolean___GENPT__ args;
} INDEXED_void_GLenum_GLboolean___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLint___GENPT__)(GLenum target, GLenum pname, GLint * params);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLint * a3;
} ARGS_void_GLenum_GLenum_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLint___GENPT__ func;
    ARGS_void_GLenum_GLenum_GLint___GENPT__ args;
} PACKED_void_GLenum_GLenum_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLint___GENPT__ args;
} INDEXED_void_GLenum_GLenum_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLfloat___GENPT__)(GLenum plane, GLfloat * equation);
typedef struct {
    GLenum a1;
    GLfloat * a2;
} ARGS_void_GLenum_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLfloat___GENPT__ func;
    ARGS_void_GLenum_GLfloat___GENPT__ args;
} PACKED_void_GLenum_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLfloat___GENPT__ args;
} INDEXED_void_GLenum_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLfixed___GENPT__)(GLenum plane, GLfixed * equation);
typedef struct {
    GLenum a1;
    GLfixed * a2;
} ARGS_void_GLenum_GLfixed___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLfixed___GENPT__ func;
    ARGS_void_GLenum_GLfixed___GENPT__ args;
} PACKED_void_GLenum_GLfixed___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLfixed___GENPT__ args;
} INDEXED_void_GLenum_GLfixed___GENPT__;
typedef GLenum (APIENTRY*FUNC_GLenum)();
typedef struct {
    int format;
    FUNC_GLenum func;
} PACKED_GLenum;
typedef struct {
    int func;
} INDEXED_GLenum;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLenum_GLint___GENPT__)(GLenum target, GLenum attachment, GLenum pname, GLint * params);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLenum a3;
    GLint * a4;
} ARGS_void_GLenum_GLenum_GLenum_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLenum_GLint___GENPT__ func;
    ARGS_void_GLenum_GLenum_GLenum_GLint___GENPT__ args;
} PACKED_void_GLenum_GLenum_GLenum_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLenum_GLint___GENPT__ args;
} INDEXED_void_GLenum_GLenum_GLenum_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLint___GENPT__)(GLenum pname, GLint * params);
typedef struct {
    GLenum a1;
    GLint * a2;
} ARGS_void_GLenum_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint___GENPT__ func;
    ARGS_void_GLenum_GLint___GENPT__ args;
} PACKED_void_GLenum_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint___GENPT__ args;
} INDEXED_void_GLenum_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLfloat___GENPT__)(GLenum light, GLenum pname, GLfloat * params);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLfloat * a3;
} ARGS_void_GLenum_GLenum_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLfloat___GENPT__ func;
    ARGS_void_GLenum_GLenum_GLfloat___GENPT__ args;
} PACKED_void_GLenum_GLenum_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLfloat___GENPT__ args;
} INDEXED_void_GLenum_GLenum_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLfixed___GENPT__)(GLenum light, GLenum pname, GLfixed * params);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLfixed * a3;
} ARGS_void_GLenum_GLenum_GLfixed___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLfixed___GENPT__ func;
    ARGS_void_GLenum_GLenum_GLfixed___GENPT__ args;
} PACKED_void_GLenum_GLenum_GLfixed___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLfixed___GENPT__ args;
} INDEXED_void_GLenum_GLenum_GLfixed___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLvoid___GENPT____GENPT__)(GLenum pname, GLvoid ** params);
typedef struct {
    GLenum a1;
    GLvoid ** a2;
} ARGS_void_GLenum_GLvoid___GENPT____GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLvoid___GENPT____GENPT__ func;
    ARGS_void_GLenum_GLvoid___GENPT____GENPT__ args;
} PACKED_void_GLenum_GLvoid___GENPT____GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLvoid___GENPT____GENPT__ args;
} INDEXED_void_GLenum_GLvoid___GENPT____GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__)(GLuint program, GLsizei bufSize, GLsizei * length, GLenum * binaryFormat, GLvoid * binary);
typedef struct {
    GLuint a1;
    GLsizei a2;
    GLsizei * a3;
    GLenum * a4;
    GLvoid * a5;
} ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__ func;
    ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__ args;
} PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__ args;
} INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__)(GLuint program, GLsizei bufSize, GLsizei * length, GLchar * infoLog);
typedef struct {
    GLuint a1;
    GLsizei a2;
    GLsizei * a3;
    GLchar * a4;
} ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__ func;
    ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__ args;
} PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__ args;
} INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLenum_GLint___GENPT__)(GLuint program, GLenum pname, GLint * params);
typedef struct {
    GLuint a1;
    GLenum a2;
    GLint * a3;
} ARGS_void_GLuint_GLenum_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLenum_GLint___GENPT__ func;
    ARGS_void_GLuint_GLenum_GLint___GENPT__ args;
} PACKED_void_GLuint_GLenum_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLenum_GLint___GENPT__ args;
} INDEXED_void_GLuint_GLenum_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__)(GLenum shadertype, GLenum precisiontype, GLint * range, GLint * precision);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLint * a3;
    GLint * a4;
} ARGS_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__ func;
    ARGS_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__ args;
} PACKED_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__ args;
} INDEXED_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__;
typedef const GLubyte * (APIENTRY*FUNC_const_GLubyte___GENPT___GLenum)(GLenum name);
typedef struct {
    GLenum a1;
} ARGS_const_GLubyte___GENPT___GLenum;
typedef struct {
    int format;
    FUNC_const_GLubyte___GENPT___GLenum func;
    ARGS_const_GLubyte___GENPT___GLenum args;
} PACKED_const_GLubyte___GENPT___GLenum;
typedef struct {
    int func;
    ARGS_const_GLubyte___GENPT___GLenum args;
} INDEXED_const_GLubyte___GENPT___GLenum;
typedef void (APIENTRY*FUNC_void_GLuint_GLint_GLfloat___GENPT__)(GLuint program, GLint location, GLfloat * params);
typedef struct {
    GLuint a1;
    GLint a2;
    GLfloat * a3;
} ARGS_void_GLuint_GLint_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLint_GLfloat___GENPT__ func;
    ARGS_void_GLuint_GLint_GLfloat___GENPT__ args;
} PACKED_void_GLuint_GLint_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLint_GLfloat___GENPT__ args;
} INDEXED_void_GLuint_GLint_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLint_GLint___GENPT__)(GLuint program, GLint location, GLint * params);
typedef struct {
    GLuint a1;
    GLint a2;
    GLint * a3;
} ARGS_void_GLuint_GLint_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLint_GLint___GENPT__ func;
    ARGS_void_GLuint_GLint_GLint___GENPT__ args;
} PACKED_void_GLuint_GLint_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLint_GLint___GENPT__ args;
} INDEXED_void_GLuint_GLint_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLenum_GLvoid___GENPT____GENPT__)(GLuint index, GLenum pname, GLvoid ** pointer);
typedef struct {
    GLuint a1;
    GLenum a2;
    GLvoid ** a3;
} ARGS_void_GLuint_GLenum_GLvoid___GENPT____GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLenum_GLvoid___GENPT____GENPT__ func;
    ARGS_void_GLuint_GLenum_GLvoid___GENPT____GENPT__ args;
} PACKED_void_GLuint_GLenum_GLvoid___GENPT____GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLenum_GLvoid___GENPT____GENPT__ args;
} INDEXED_void_GLuint_GLenum_GLvoid___GENPT____GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLenum_GLfloat___GENPT__)(GLuint index, GLenum pname, GLfloat * params);
typedef struct {
    GLuint a1;
    GLenum a2;
    GLfloat * a3;
} ARGS_void_GLuint_GLenum_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLenum_GLfloat___GENPT__ func;
    ARGS_void_GLuint_GLenum_GLfloat___GENPT__ args;
} PACKED_void_GLuint_GLenum_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLenum_GLfloat___GENPT__ args;
} INDEXED_void_GLuint_GLenum_GLfloat___GENPT__;
typedef GLboolean (APIENTRY*FUNC_GLboolean_GLuint)(GLuint buffer);
typedef struct {
    GLuint a1;
} ARGS_GLboolean_GLuint;
typedef struct {
    int format;
    FUNC_GLboolean_GLuint func;
    ARGS_GLboolean_GLuint args;
} PACKED_GLboolean_GLuint;
typedef struct {
    int func;
    ARGS_GLboolean_GLuint args;
} INDEXED_GLboolean_GLuint;
typedef GLboolean (APIENTRY*FUNC_GLboolean_GLenum)(GLenum cap);
typedef struct {
    GLenum a1;
} ARGS_GLboolean_GLenum;
typedef struct {
    int format;
    FUNC_GLboolean_GLenum func;
    ARGS_GLboolean_GLenum args;
} PACKED_GLboolean_GLenum;
typedef struct {
    int func;
    ARGS_GLboolean_GLenum args;
} INDEXED_GLboolean_GLenum;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLfloat)(GLenum light, GLenum pname, GLfloat param);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLfloat a3;
} ARGS_void_GLenum_GLenum_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLfloat func;
    ARGS_void_GLenum_GLenum_GLfloat args;
} PACKED_void_GLenum_GLenum_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLfloat args;
} INDEXED_void_GLenum_GLenum_GLfloat;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_const_GLfloat___GENPT__)(GLenum light, GLenum pname, const GLfloat * params);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLfloat * a3;
} ARGS_void_GLenum_GLenum_const_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_const_GLfloat___GENPT__ func;
    ARGS_void_GLenum_GLenum_const_GLfloat___GENPT__ args;
} PACKED_void_GLenum_GLenum_const_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_const_GLfloat___GENPT__ args;
} INDEXED_void_GLenum_GLenum_const_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLfixed)(GLenum light, GLenum pname, GLfixed param);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLfixed a3;
} ARGS_void_GLenum_GLenum_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLfixed func;
    ARGS_void_GLenum_GLenum_GLfixed args;
} PACKED_void_GLenum_GLenum_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLfixed args;
} INDEXED_void_GLenum_GLenum_GLfixed;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_const_GLfixed___GENPT__)(GLenum light, GLenum pname, const GLfixed * params);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLfixed * a3;
} ARGS_void_GLenum_GLenum_const_GLfixed___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_const_GLfixed___GENPT__ func;
    ARGS_void_GLenum_GLenum_const_GLfixed___GENPT__ args;
} PACKED_void_GLenum_GLenum_const_GLfixed___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_const_GLfixed___GENPT__ args;
} INDEXED_void_GLenum_GLenum_const_GLfixed___GENPT__;
typedef void (APIENTRY*FUNC_void_GLfixed)(GLfixed width);
typedef struct {
    GLfixed a1;
} ARGS_void_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLfixed func;
    ARGS_void_GLfixed args;
} PACKED_void_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLfixed args;
} INDEXED_void_GLfixed;
typedef void (APIENTRY*FUNC_void_const_GLfixed___GENPT__)(const GLfixed * m);
typedef struct {
    GLfixed * a1;
} ARGS_void_const_GLfixed___GENPT__;
typedef struct {
    int format;
    FUNC_void_const_GLfixed___GENPT__ func;
    ARGS_void_const_GLfixed___GENPT__ args;
} PACKED_void_const_GLfixed___GENPT__;
typedef struct {
    int func;
    ARGS_void_const_GLfixed___GENPT__ args;
} INDEXED_void_const_GLfixed___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei)(GLenum mode, const GLint * first, const GLsizei * count, GLsizei primcount);
typedef struct {
    GLenum a1;
    GLint * a2;
    GLsizei * a3;
    GLsizei a4;
} ARGS_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei;
typedef struct {
    int format;
    FUNC_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei func;
    ARGS_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei args;
} PACKED_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei;
typedef struct {
    int func;
    ARGS_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei args;
} INDEXED_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei;
typedef void (APIENTRY*FUNC_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei)(GLenum mode, GLsizei * count, GLenum type, const void * const * indices, GLsizei primcount);
typedef struct {
    GLenum a1;
    GLsizei * a2;
    GLenum a3;
    const void * const * a4;
    GLsizei a5;
} ARGS_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei;
typedef struct {
    int format;
    FUNC_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei func;
    ARGS_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei args;
} PACKED_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei;
typedef struct {
    int func;
    ARGS_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei args;
} INDEXED_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei;
typedef void (APIENTRY*FUNC_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat)(GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q);
typedef struct {
    GLenum a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
    GLfloat a5;
} ARGS_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed)(GLenum target, GLfixed s, GLfixed t, GLfixed r, GLfixed q);
typedef struct {
    GLenum a1;
    GLfixed a2;
    GLfixed a3;
    GLfixed a4;
    GLfixed a5;
} ARGS_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed func;
    ARGS_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed args;
} PACKED_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed args;
} INDEXED_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed;
typedef void (APIENTRY*FUNC_void_GLfloat_GLfloat_GLfloat)(GLfloat nx, GLfloat ny, GLfloat nz);
typedef struct {
    GLfloat a1;
    GLfloat a2;
    GLfloat a3;
} ARGS_void_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLfixed_GLfixed_GLfixed)(GLfixed nx, GLfixed ny, GLfixed nz);
typedef struct {
    GLfixed a1;
    GLfixed a2;
    GLfixed a3;
} ARGS_void_GLfixed_GLfixed_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLfixed_GLfixed_GLfixed func;
    ARGS_void_GLfixed_GLfixed_GLfixed args;
} PACKED_void_GLfixed_GLfixed_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLfixed_GLfixed_GLfixed args;
} INDEXED_void_GLfixed_GLfixed_GLfixed;
typedef void (APIENTRY*FUNC_void_GLenum_GLint)(GLenum pname, GLint param);
typedef struct {
    GLenum a1;
    GLint a2;
} ARGS_void_GLenum_GLint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint func;
    ARGS_void_GLenum_GLint args;
} PACKED_void_GLenum_GLint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint args;
} INDEXED_void_GLenum_GLint;
typedef void (APIENTRY*FUNC_void_GLfloat_GLfloat)(GLfloat factor, GLfloat units);
typedef struct {
    GLfloat a1;
    GLfloat a2;
} ARGS_void_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLfloat_GLfloat func;
    ARGS_void_GLfloat_GLfloat args;
} PACKED_void_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLfloat_GLfloat args;
} INDEXED_void_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLfixed_GLfixed)(GLfixed factor, GLfixed units);
typedef struct {
    GLfixed a1;
    GLfixed a2;
} ARGS_void_GLfixed_GLfixed;
typedef struct {
    int format;
    FUNC_void_GLfixed_GLfixed func;
    ARGS_void_GLfixed_GLfixed args;
} PACKED_void_GLfixed_GLfixed;
typedef struct {
    int func;
    ARGS_void_GLfixed_GLfixed args;
} INDEXED_void_GLfixed_GLfixed;
typedef void (APIENTRY*FUNC_void_GLuint_GLenum_const_GLvoid___GENPT___GLint)(GLuint program, GLenum binaryFormat, const GLvoid * binary, GLint length);
typedef struct {
    GLuint a1;
    GLenum a2;
    GLvoid * a3;
    GLint a4;
} ARGS_void_GLuint_GLenum_const_GLvoid___GENPT___GLint;
typedef struct {
    int format;
    FUNC_void_GLuint_GLenum_const_GLvoid___GENPT___GLint func;
    ARGS_void_GLuint_GLenum_const_GLvoid___GENPT___GLint args;
} PACKED_void_GLuint_GLenum_const_GLvoid___GENPT___GLint;
typedef struct {
    int func;
    ARGS_void_GLuint_GLenum_const_GLvoid___GENPT___GLint args;
} INDEXED_void_GLuint_GLenum_const_GLvoid___GENPT___GLint;
typedef void (APIENTRY*FUNC_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__)(GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, GLvoid * pixels);
typedef struct {
    GLint a1;
    GLint a2;
    GLsizei a3;
    GLsizei a4;
    GLenum a5;
    GLenum a6;
    GLvoid * a7;
} ARGS_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__ func;
    ARGS_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__ args;
} PACKED_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__ args;
} INDEXED_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLsizei_GLsizei)(GLenum target, GLenum internalformat, GLsizei width, GLsizei height);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLsizei a3;
    GLsizei a4;
} ARGS_void_GLenum_GLenum_GLsizei_GLsizei;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLsizei_GLsizei func;
    ARGS_void_GLenum_GLenum_GLsizei_GLsizei args;
} PACKED_void_GLenum_GLenum_GLsizei_GLsizei;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLsizei_GLsizei args;
} INDEXED_void_GLenum_GLenum_GLsizei_GLsizei;
typedef void (APIENTRY*FUNC_void_GLclampf_GLboolean)(GLclampf value, GLboolean invert);
typedef struct {
    GLclampf a1;
    GLboolean a2;
} ARGS_void_GLclampf_GLboolean;
typedef struct {
    int format;
    FUNC_void_GLclampf_GLboolean func;
    ARGS_void_GLclampf_GLboolean args;
} PACKED_void_GLclampf_GLboolean;
typedef struct {
    int func;
    ARGS_void_GLclampf_GLboolean args;
} INDEXED_void_GLclampf_GLboolean;
typedef void (APIENTRY*FUNC_void_GLclampx_GLboolean)(GLclampx value, GLboolean invert);
typedef struct {
    GLclampx a1;
    GLboolean a2;
} ARGS_void_GLclampx_GLboolean;
typedef struct {
    int format;
    FUNC_void_GLclampx_GLboolean func;
    ARGS_void_GLclampx_GLboolean args;
} PACKED_void_GLclampx_GLboolean;
typedef struct {
    int func;
    ARGS_void_GLclampx_GLboolean args;
} INDEXED_void_GLclampx_GLboolean;
typedef void (APIENTRY*FUNC_void_GLint_GLint_GLsizei_GLsizei)(GLint x, GLint y, GLsizei width, GLsizei height);
typedef struct {
    GLint a1;
    GLint a2;
    GLsizei a3;
    GLsizei a4;
} ARGS_void_GLint_GLint_GLsizei_GLsizei;
typedef struct {
    int format;
    FUNC_void_GLint_GLint_GLsizei_GLsizei func;
    ARGS_void_GLint_GLint_GLsizei_GLsizei args;
} PACKED_void_GLint_GLint_GLsizei_GLsizei;
typedef struct {
    int func;
    ARGS_void_GLint_GLint_GLsizei_GLsizei args;
} INDEXED_void_GLint_GLint_GLsizei_GLsizei;
typedef void (APIENTRY*FUNC_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei)(GLsizei n, const GLuint * shaders, GLenum binaryformat, const GLvoid * binary, GLsizei length);
typedef struct {
    GLsizei a1;
    GLuint * a2;
    GLenum a3;
    GLvoid * a4;
    GLsizei a5;
} ARGS_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei;
typedef struct {
    int format;
    FUNC_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei func;
    ARGS_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei args;
} PACKED_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei;
typedef struct {
    int func;
    ARGS_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei args;
} INDEXED_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei;
typedef void (APIENTRY*FUNC_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__)(GLuint shader, GLsizei count, const GLchar * const * string, const GLint * length);
typedef struct {
    GLuint a1;
    GLsizei a2;
    const GLchar * const * a3;
    GLint * a4;
} ARGS_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__ func;
    ARGS_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__ args;
} PACKED_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__ args;
} INDEXED_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLuint)(GLenum func, GLint ref, GLuint mask);
typedef struct {
    GLenum a1;
    GLint a2;
    GLuint a3;
} ARGS_void_GLenum_GLint_GLuint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLuint func;
    ARGS_void_GLenum_GLint_GLuint args;
} PACKED_void_GLenum_GLint_GLuint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLuint args;
} INDEXED_void_GLenum_GLint_GLuint;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLint_GLuint)(GLenum face, GLenum func, GLint ref, GLuint mask);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLint a3;
    GLuint a4;
} ARGS_void_GLenum_GLenum_GLint_GLuint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLint_GLuint func;
    ARGS_void_GLenum_GLenum_GLint_GLuint args;
} PACKED_void_GLenum_GLenum_GLint_GLuint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLint_GLuint args;
} INDEXED_void_GLenum_GLenum_GLint_GLuint;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLenum)(GLenum fail, GLenum zfail, GLenum zpass);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLenum a3;
} ARGS_void_GLenum_GLenum_GLenum;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLenum func;
    ARGS_void_GLenum_GLenum_GLenum args;
} PACKED_void_GLenum_GLenum_GLenum;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLenum args;
} INDEXED_void_GLenum_GLenum_GLenum;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_GLint)(GLenum target, GLenum pname, GLint param);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLint a3;
} ARGS_void_GLenum_GLenum_GLint;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_GLint func;
    ARGS_void_GLenum_GLenum_GLint args;
} PACKED_void_GLenum_GLenum_GLint;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_GLint args;
} INDEXED_void_GLenum_GLenum_GLint;
typedef void (APIENTRY*FUNC_void_GLenum_GLenum_const_GLint___GENPT__)(GLenum target, GLenum pname, const GLint * params);
typedef struct {
    GLenum a1;
    GLenum a2;
    GLint * a3;
} ARGS_void_GLenum_GLenum_const_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLenum_const_GLint___GENPT__ func;
    ARGS_void_GLenum_GLenum_const_GLint___GENPT__ args;
} PACKED_void_GLenum_GLenum_const_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLenum_const_GLint___GENPT__ args;
} INDEXED_void_GLenum_GLenum_const_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__)(GLenum target, GLint level, GLint internalformat, GLsizei width, GLsizei height, GLint border, GLenum format, GLenum type, const GLvoid * data);
typedef struct {
    GLenum a1;
    GLint a2;
    GLint a3;
    GLsizei a4;
    GLsizei a5;
    GLint a6;
    GLenum a7;
    GLenum a8;
    GLvoid * a9;
} ARGS_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__ func;
    ARGS_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__ args;
} PACKED_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__ args;
} INDEXED_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLenum type, const GLvoid * data);
typedef struct {
    GLenum a1;
    GLint a2;
    GLint a3;
    GLint a4;
    GLsizei a5;
    GLsizei a6;
    GLenum a7;
    GLenum a8;
    GLvoid * a9;
} ARGS_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__ func;
    ARGS_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__ args;
} PACKED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__ args;
} INDEXED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__;
typedef void (APIENTRY*FUNC_void_GLint_GLfloat)(GLint location, GLfloat v0);
typedef struct {
    GLint a1;
    GLfloat a2;
} ARGS_void_GLint_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLint_GLfloat func;
    ARGS_void_GLint_GLfloat args;
} PACKED_void_GLint_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLint_GLfloat args;
} INDEXED_void_GLint_GLfloat;
typedef void (APIENTRY*FUNC_void_GLint_GLsizei_const_GLfloat___GENPT__)(GLint location, GLsizei count, const GLfloat * value);
typedef struct {
    GLint a1;
    GLsizei a2;
    GLfloat * a3;
} ARGS_void_GLint_GLsizei_const_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLint_GLsizei_const_GLfloat___GENPT__ func;
    ARGS_void_GLint_GLsizei_const_GLfloat___GENPT__ args;
} PACKED_void_GLint_GLsizei_const_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLint_GLsizei_const_GLfloat___GENPT__ args;
} INDEXED_void_GLint_GLsizei_const_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLint_GLint)(GLint location, GLint v0);
typedef struct {
    GLint a1;
    GLint a2;
} ARGS_void_GLint_GLint;
typedef struct {
    int format;
    FUNC_void_GLint_GLint func;
    ARGS_void_GLint_GLint args;
} PACKED_void_GLint_GLint;
typedef struct {
    int func;
    ARGS_void_GLint_GLint args;
} INDEXED_void_GLint_GLint;
typedef void (APIENTRY*FUNC_void_GLint_GLsizei_const_GLint___GENPT__)(GLint location, GLsizei count, const GLint * value);
typedef struct {
    GLint a1;
    GLsizei a2;
    GLint * a3;
} ARGS_void_GLint_GLsizei_const_GLint___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLint_GLsizei_const_GLint___GENPT__ func;
    ARGS_void_GLint_GLsizei_const_GLint___GENPT__ args;
} PACKED_void_GLint_GLsizei_const_GLint___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLint_GLsizei_const_GLint___GENPT__ args;
} INDEXED_void_GLint_GLsizei_const_GLint___GENPT__;
typedef void (APIENTRY*FUNC_void_GLint_GLfloat_GLfloat)(GLint location, GLfloat v0, GLfloat v1);
typedef struct {
    GLint a1;
    GLfloat a2;
    GLfloat a3;
} ARGS_void_GLint_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLint_GLfloat_GLfloat func;
    ARGS_void_GLint_GLfloat_GLfloat args;
} PACKED_void_GLint_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLint_GLfloat_GLfloat args;
} INDEXED_void_GLint_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLint_GLint_GLint)(GLint location, GLint v0, GLint v1);
typedef struct {
    GLint a1;
    GLint a2;
    GLint a3;
} ARGS_void_GLint_GLint_GLint;
typedef struct {
    int format;
    FUNC_void_GLint_GLint_GLint func;
    ARGS_void_GLint_GLint_GLint args;
} PACKED_void_GLint_GLint_GLint;
typedef struct {
    int func;
    ARGS_void_GLint_GLint_GLint args;
} INDEXED_void_GLint_GLint_GLint;
typedef void (APIENTRY*FUNC_void_GLint_GLfloat_GLfloat_GLfloat)(GLint location, GLfloat v0, GLfloat v1, GLfloat v2);
typedef struct {
    GLint a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
} ARGS_void_GLint_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLint_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLint_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLint_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLint_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLint_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLint_GLint_GLint_GLint)(GLint location, GLint v0, GLint v1, GLint v2);
typedef struct {
    GLint a1;
    GLint a2;
    GLint a3;
    GLint a4;
} ARGS_void_GLint_GLint_GLint_GLint;
typedef struct {
    int format;
    FUNC_void_GLint_GLint_GLint_GLint func;
    ARGS_void_GLint_GLint_GLint_GLint args;
} PACKED_void_GLint_GLint_GLint_GLint;
typedef struct {
    int func;
    ARGS_void_GLint_GLint_GLint_GLint args;
} INDEXED_void_GLint_GLint_GLint_GLint;
typedef void (APIENTRY*FUNC_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat)(GLint location, GLfloat v0, GLfloat v1, GLfloat v2, GLfloat v3);
typedef struct {
    GLint a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
    GLfloat a5;
} ARGS_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__)(GLint location, GLsizei count, GLboolean transpose, const GLfloat * value);
typedef struct {
    GLint a1;
    GLsizei a2;
    GLboolean a3;
    GLfloat * a4;
} ARGS_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__ func;
    ARGS_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__ args;
} PACKED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__ args;
} INDEXED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLfloat)(GLuint index, GLfloat x);
typedef struct {
    GLuint a1;
    GLfloat a2;
} ARGS_void_GLuint_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLuint_GLfloat func;
    ARGS_void_GLuint_GLfloat args;
} PACKED_void_GLuint_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLuint_GLfloat args;
} INDEXED_void_GLuint_GLfloat;
typedef void (APIENTRY*FUNC_void_GLuint_const_GLfloat___GENPT__)(GLuint index, const GLfloat * v);
typedef struct {
    GLuint a1;
    GLfloat * a2;
} ARGS_void_GLuint_const_GLfloat___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_const_GLfloat___GENPT__ func;
    ARGS_void_GLuint_const_GLfloat___GENPT__ args;
} PACKED_void_GLuint_const_GLfloat___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_const_GLfloat___GENPT__ args;
} INDEXED_void_GLuint_const_GLfloat___GENPT__;
typedef void (APIENTRY*FUNC_void_GLuint_GLfloat_GLfloat)(GLuint index, GLfloat x, GLfloat y);
typedef struct {
    GLuint a1;
    GLfloat a2;
    GLfloat a3;
} ARGS_void_GLuint_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLuint_GLfloat_GLfloat func;
    ARGS_void_GLuint_GLfloat_GLfloat args;
} PACKED_void_GLuint_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLuint_GLfloat_GLfloat args;
} INDEXED_void_GLuint_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLuint_GLfloat_GLfloat_GLfloat)(GLuint index, GLfloat x, GLfloat y, GLfloat z);
typedef struct {
    GLuint a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
} ARGS_void_GLuint_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLuint_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLuint_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLuint_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLuint_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLuint_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat)(GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w);
typedef struct {
    GLuint a1;
    GLfloat a2;
    GLfloat a3;
    GLfloat a4;
    GLfloat a5;
} ARGS_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int format;
    FUNC_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat func;
    ARGS_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat args;
} PACKED_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat;
typedef struct {
    int func;
    ARGS_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat args;
} INDEXED_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat;
typedef void (APIENTRY*FUNC_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__)(GLuint index, GLint size, GLenum type, GLboolean normalized, GLsizei stride, const GLvoid * pointer);
typedef struct {
    GLuint a1;
    GLint a2;
    GLenum a3;
    GLboolean a4;
    GLsizei a5;
    GLvoid * a6;
} ARGS_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int format;
    FUNC_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__ func;
    ARGS_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__ args;
} PACKED_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__;
typedef struct {
    int func;
    ARGS_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__ args;
} INDEXED_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__;

extern void APIENTRY_GL4ES glPushCall(void *data);
void APIENTRY_GL4ES glPackedCall(const packed_call_t *packed);
void APIENTRY_GL4ES glIndexedCall(const indexed_call_t *packed, void *ret_v);
packed_call_t* APIENTRY_GL4ES glCopyPackedCall(const packed_call_t *packed);

#define glActiveTexture_INDEX 1
#define glActiveTexture_RETURN void
#define glActiveTexture_ARG_NAMES texture
#define glActiveTexture_ARG_EXPAND GLenum texture
#define glActiveTexture_PACKED PACKED_void_GLenum
#define glActiveTexture_INDEXED INDEXED_void_GLenum
#define glActiveTexture_FORMAT FORMAT_void_GLenum
#define glAlphaFunc_INDEX 2
#define glAlphaFunc_RETURN void
#define glAlphaFunc_ARG_NAMES func, ref
#define glAlphaFunc_ARG_EXPAND GLenum func, GLclampf ref
#define glAlphaFunc_PACKED PACKED_void_GLenum_GLclampf
#define glAlphaFunc_INDEXED INDEXED_void_GLenum_GLclampf
#define glAlphaFunc_FORMAT FORMAT_void_GLenum_GLclampf
#define glAlphaFuncx_INDEX 3
#define glAlphaFuncx_RETURN void
#define glAlphaFuncx_ARG_NAMES func, ref
#define glAlphaFuncx_ARG_EXPAND GLenum func, GLclampx ref
#define glAlphaFuncx_PACKED PACKED_void_GLenum_GLclampx
#define glAlphaFuncx_INDEXED INDEXED_void_GLenum_GLclampx
#define glAlphaFuncx_FORMAT FORMAT_void_GLenum_GLclampx
#define glAttachShader_INDEX 4
#define glAttachShader_RETURN void
#define glAttachShader_ARG_NAMES program, shader
#define glAttachShader_ARG_EXPAND GLuint program, GLuint shader
#define glAttachShader_PACKED PACKED_void_GLuint_GLuint
#define glAttachShader_INDEXED INDEXED_void_GLuint_GLuint
#define glAttachShader_FORMAT FORMAT_void_GLuint_GLuint
#define glBindAttribLocation_INDEX 5
#define glBindAttribLocation_RETURN void
#define glBindAttribLocation_ARG_NAMES program, index, name
#define glBindAttribLocation_ARG_EXPAND GLuint program, GLuint index, const GLchar * name
#define glBindAttribLocation_PACKED PACKED_void_GLuint_GLuint_const_GLchar___GENPT__
#define glBindAttribLocation_INDEXED INDEXED_void_GLuint_GLuint_const_GLchar___GENPT__
#define glBindAttribLocation_FORMAT FORMAT_void_GLuint_GLuint_const_GLchar___GENPT__
#define glBindBuffer_INDEX 6
#define glBindBuffer_RETURN void
#define glBindBuffer_ARG_NAMES target, buffer
#define glBindBuffer_ARG_EXPAND GLenum target, GLuint buffer
#define glBindBuffer_PACKED PACKED_void_GLenum_GLuint
#define glBindBuffer_INDEXED INDEXED_void_GLenum_GLuint
#define glBindBuffer_FORMAT FORMAT_void_GLenum_GLuint
#define glBindFramebuffer_INDEX 7
#define glBindFramebuffer_RETURN void
#define glBindFramebuffer_ARG_NAMES target, framebuffer
#define glBindFramebuffer_ARG_EXPAND GLenum target, GLuint framebuffer
#define glBindFramebuffer_PACKED PACKED_void_GLenum_GLuint
#define glBindFramebuffer_INDEXED INDEXED_void_GLenum_GLuint
#define glBindFramebuffer_FORMAT FORMAT_void_GLenum_GLuint
#define glBindRenderbuffer_INDEX 8
#define glBindRenderbuffer_RETURN void
#define glBindRenderbuffer_ARG_NAMES target, renderbuffer
#define glBindRenderbuffer_ARG_EXPAND GLenum target, GLuint renderbuffer
#define glBindRenderbuffer_PACKED PACKED_void_GLenum_GLuint
#define glBindRenderbuffer_INDEXED INDEXED_void_GLenum_GLuint
#define glBindRenderbuffer_FORMAT FORMAT_void_GLenum_GLuint
#define glBindTexture_INDEX 9
#define glBindTexture_RETURN void
#define glBindTexture_ARG_NAMES target, texture
#define glBindTexture_ARG_EXPAND GLenum target, GLuint texture
#define glBindTexture_PACKED PACKED_void_GLenum_GLuint
#define glBindTexture_INDEXED INDEXED_void_GLenum_GLuint
#define glBindTexture_FORMAT FORMAT_void_GLenum_GLuint
#define glBlendColor_INDEX 10
#define glBlendColor_RETURN void
#define glBlendColor_ARG_NAMES red, green, blue, alpha
#define glBlendColor_ARG_EXPAND GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha
#define glBlendColor_PACKED PACKED_void_GLclampf_GLclampf_GLclampf_GLclampf
#define glBlendColor_INDEXED INDEXED_void_GLclampf_GLclampf_GLclampf_GLclampf
#define glBlendColor_FORMAT FORMAT_void_GLclampf_GLclampf_GLclampf_GLclampf
#define glBlendEquation_INDEX 11
#define glBlendEquation_RETURN void
#define glBlendEquation_ARG_NAMES mode
#define glBlendEquation_ARG_EXPAND GLenum mode
#define glBlendEquation_PACKED PACKED_void_GLenum
#define glBlendEquation_INDEXED INDEXED_void_GLenum
#define glBlendEquation_FORMAT FORMAT_void_GLenum
#define glBlendEquationSeparate_INDEX 12
#define glBlendEquationSeparate_RETURN void
#define glBlendEquationSeparate_ARG_NAMES modeRGB, modeA
#define glBlendEquationSeparate_ARG_EXPAND GLenum modeRGB, GLenum modeA
#define glBlendEquationSeparate_PACKED PACKED_void_GLenum_GLenum
#define glBlendEquationSeparate_INDEXED INDEXED_void_GLenum_GLenum
#define glBlendEquationSeparate_FORMAT FORMAT_void_GLenum_GLenum
#define glBlendFunc_INDEX 13
#define glBlendFunc_RETURN void
#define glBlendFunc_ARG_NAMES sfactor, dfactor
#define glBlendFunc_ARG_EXPAND GLenum sfactor, GLenum dfactor
#define glBlendFunc_PACKED PACKED_void_GLenum_GLenum
#define glBlendFunc_INDEXED INDEXED_void_GLenum_GLenum
#define glBlendFunc_FORMAT FORMAT_void_GLenum_GLenum
#define glBlendFuncSeparate_INDEX 14
#define glBlendFuncSeparate_RETURN void
#define glBlendFuncSeparate_ARG_NAMES sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha
#define glBlendFuncSeparate_ARG_EXPAND GLenum sfactorRGB, GLenum dfactorRGB, GLenum sfactorAlpha, GLenum dfactorAlpha
#define glBlendFuncSeparate_PACKED PACKED_void_GLenum_GLenum_GLenum_GLenum
#define glBlendFuncSeparate_INDEXED INDEXED_void_GLenum_GLenum_GLenum_GLenum
#define glBlendFuncSeparate_FORMAT FORMAT_void_GLenum_GLenum_GLenum_GLenum
#define glBufferData_INDEX 15
#define glBufferData_RETURN void
#define glBufferData_ARG_NAMES target, size, data, usage
#define glBufferData_ARG_EXPAND GLenum target, GLsizeiptr size, const GLvoid * data, GLenum usage
#define glBufferData_PACKED PACKED_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum
#define glBufferData_INDEXED INDEXED_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum
#define glBufferData_FORMAT FORMAT_void_GLenum_GLsizeiptr_const_GLvoid___GENPT___GLenum
#define glBufferSubData_INDEX 16
#define glBufferSubData_RETURN void
#define glBufferSubData_ARG_NAMES target, offset, size, data
#define glBufferSubData_ARG_EXPAND GLenum target, GLintptr offset, GLsizeiptr size, const GLvoid * data
#define glBufferSubData_PACKED PACKED_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__
#define glBufferSubData_INDEXED INDEXED_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__
#define glBufferSubData_FORMAT FORMAT_void_GLenum_GLintptr_GLsizeiptr_const_GLvoid___GENPT__
#define glCheckFramebufferStatus_INDEX 17
#define glCheckFramebufferStatus_RETURN GLenum
#define glCheckFramebufferStatus_ARG_NAMES target
#define glCheckFramebufferStatus_ARG_EXPAND GLenum target
#define glCheckFramebufferStatus_PACKED PACKED_GLenum_GLenum
#define glCheckFramebufferStatus_INDEXED INDEXED_GLenum_GLenum
#define glCheckFramebufferStatus_FORMAT FORMAT_GLenum_GLenum
#define glClear_INDEX 18
#define glClear_RETURN void
#define glClear_ARG_NAMES mask
#define glClear_ARG_EXPAND GLbitfield mask
#define glClear_PACKED PACKED_void_GLbitfield
#define glClear_INDEXED INDEXED_void_GLbitfield
#define glClear_FORMAT FORMAT_void_GLbitfield
#define glClearColor_INDEX 19
#define glClearColor_RETURN void
#define glClearColor_ARG_NAMES red, green, blue, alpha
#define glClearColor_ARG_EXPAND GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha
#define glClearColor_PACKED PACKED_void_GLclampf_GLclampf_GLclampf_GLclampf
#define glClearColor_INDEXED INDEXED_void_GLclampf_GLclampf_GLclampf_GLclampf
#define glClearColor_FORMAT FORMAT_void_GLclampf_GLclampf_GLclampf_GLclampf
#define glClearColorx_INDEX 20
#define glClearColorx_RETURN void
#define glClearColorx_ARG_NAMES red, green, blue, alpha
#define glClearColorx_ARG_EXPAND GLclampx red, GLclampx green, GLclampx blue, GLclampx alpha
#define glClearColorx_PACKED PACKED_void_GLclampx_GLclampx_GLclampx_GLclampx
#define glClearColorx_INDEXED INDEXED_void_GLclampx_GLclampx_GLclampx_GLclampx
#define glClearColorx_FORMAT FORMAT_void_GLclampx_GLclampx_GLclampx_GLclampx
#define glClearDepthf_INDEX 21
#define glClearDepthf_RETURN void
#define glClearDepthf_ARG_NAMES depth
#define glClearDepthf_ARG_EXPAND GLclampf depth
#define glClearDepthf_PACKED PACKED_void_GLclampf
#define glClearDepthf_INDEXED INDEXED_void_GLclampf
#define glClearDepthf_FORMAT FORMAT_void_GLclampf
#define glClearDepthx_INDEX 22
#define glClearDepthx_RETURN void
#define glClearDepthx_ARG_NAMES depth
#define glClearDepthx_ARG_EXPAND GLclampx depth
#define glClearDepthx_PACKED PACKED_void_GLclampx
#define glClearDepthx_INDEXED INDEXED_void_GLclampx
#define glClearDepthx_FORMAT FORMAT_void_GLclampx
#define glClearStencil_INDEX 23
#define glClearStencil_RETURN void
#define glClearStencil_ARG_NAMES s
#define glClearStencil_ARG_EXPAND GLint s
#define glClearStencil_PACKED PACKED_void_GLint
#define glClearStencil_INDEXED INDEXED_void_GLint
#define glClearStencil_FORMAT FORMAT_void_GLint
#define glClientActiveTexture_INDEX 24
#define glClientActiveTexture_RETURN void
#define glClientActiveTexture_ARG_NAMES texture
#define glClientActiveTexture_ARG_EXPAND GLenum texture
#define glClientActiveTexture_PACKED PACKED_void_GLenum
#define glClientActiveTexture_INDEXED INDEXED_void_GLenum
#define glClientActiveTexture_FORMAT FORMAT_void_GLenum
#define glClipPlanef_INDEX 25
#define glClipPlanef_RETURN void
#define glClipPlanef_ARG_NAMES plane, equation
#define glClipPlanef_ARG_EXPAND GLenum plane, const GLfloat * equation
#define glClipPlanef_PACKED PACKED_void_GLenum_const_GLfloat___GENPT__
#define glClipPlanef_INDEXED INDEXED_void_GLenum_const_GLfloat___GENPT__
#define glClipPlanef_FORMAT FORMAT_void_GLenum_const_GLfloat___GENPT__
#define glClipPlanex_INDEX 26
#define glClipPlanex_RETURN void
#define glClipPlanex_ARG_NAMES plane, equation
#define glClipPlanex_ARG_EXPAND GLenum plane, const GLfixed * equation
#define glClipPlanex_PACKED PACKED_void_GLenum_const_GLfixed___GENPT__
#define glClipPlanex_INDEXED INDEXED_void_GLenum_const_GLfixed___GENPT__
#define glClipPlanex_FORMAT FORMAT_void_GLenum_const_GLfixed___GENPT__
#define glColor4f_INDEX 27
#define glColor4f_RETURN void
#define glColor4f_ARG_NAMES red, green, blue, alpha
#define glColor4f_ARG_EXPAND GLfloat red, GLfloat green, GLfloat blue, GLfloat alpha
#define glColor4f_PACKED PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat
#define glColor4f_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat
#define glColor4f_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat
#define glColor4ub_INDEX 28
#define glColor4ub_RETURN void
#define glColor4ub_ARG_NAMES red, green, blue, alpha
#define glColor4ub_ARG_EXPAND GLubyte red, GLubyte green, GLubyte blue, GLubyte alpha
#define glColor4ub_PACKED PACKED_void_GLubyte_GLubyte_GLubyte_GLubyte
#define glColor4ub_INDEXED INDEXED_void_GLubyte_GLubyte_GLubyte_GLubyte
#define glColor4ub_FORMAT FORMAT_void_GLubyte_GLubyte_GLubyte_GLubyte
#define glColor4x_INDEX 29
#define glColor4x_RETURN void
#define glColor4x_ARG_NAMES red, green, blue, alpha
#define glColor4x_ARG_EXPAND GLfixed red, GLfixed green, GLfixed blue, GLfixed alpha
#define glColor4x_PACKED PACKED_void_GLfixed_GLfixed_GLfixed_GLfixed
#define glColor4x_INDEXED INDEXED_void_GLfixed_GLfixed_GLfixed_GLfixed
#define glColor4x_FORMAT FORMAT_void_GLfixed_GLfixed_GLfixed_GLfixed
#define glColorMask_INDEX 30
#define glColorMask_RETURN void
#define glColorMask_ARG_NAMES red, green, blue, alpha
#define glColorMask_ARG_EXPAND GLboolean red, GLboolean green, GLboolean blue, GLboolean alpha
#define glColorMask_PACKED PACKED_void_GLboolean_GLboolean_GLboolean_GLboolean
#define glColorMask_INDEXED INDEXED_void_GLboolean_GLboolean_GLboolean_GLboolean
#define glColorMask_FORMAT FORMAT_void_GLboolean_GLboolean_GLboolean_GLboolean
#define glColorPointer_INDEX 31
#define glColorPointer_RETURN void
#define glColorPointer_ARG_NAMES size, type, stride, pointer
#define glColorPointer_ARG_EXPAND GLint size, GLenum type, GLsizei stride, const GLvoid * pointer
#define glColorPointer_PACKED PACKED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glColorPointer_INDEXED INDEXED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glColorPointer_FORMAT FORMAT_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glCompileShader_INDEX 32
#define glCompileShader_RETURN void
#define glCompileShader_ARG_NAMES shader
#define glCompileShader_ARG_EXPAND GLuint shader
#define glCompileShader_PACKED PACKED_void_GLuint
#define glCompileShader_INDEXED INDEXED_void_GLuint
#define glCompileShader_FORMAT FORMAT_void_GLuint
#define glCompressedTexImage2D_INDEX 33
#define glCompressedTexImage2D_RETURN void
#define glCompressedTexImage2D_ARG_NAMES target, level, internalformat, width, height, border, imageSize, data
#define glCompressedTexImage2D_ARG_EXPAND GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLint border, GLsizei imageSize, const GLvoid * data
#define glCompressedTexImage2D_PACKED PACKED_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__
#define glCompressedTexImage2D_INDEXED INDEXED_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__
#define glCompressedTexImage2D_FORMAT FORMAT_void_GLenum_GLint_GLenum_GLsizei_GLsizei_GLint_GLsizei_const_GLvoid___GENPT__
#define glCompressedTexSubImage2D_INDEX 34
#define glCompressedTexSubImage2D_RETURN void
#define glCompressedTexSubImage2D_ARG_NAMES target, level, xoffset, yoffset, width, height, format, imageSize, data
#define glCompressedTexSubImage2D_ARG_EXPAND GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLsizei imageSize, const GLvoid * data
#define glCompressedTexSubImage2D_PACKED PACKED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__
#define glCompressedTexSubImage2D_INDEXED INDEXED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__
#define glCompressedTexSubImage2D_FORMAT FORMAT_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLsizei_const_GLvoid___GENPT__
#define glCopyTexImage2D_INDEX 35
#define glCopyTexImage2D_RETURN void
#define glCopyTexImage2D_ARG_NAMES target, level, internalformat, x, y, width, height, border
#define glCopyTexImage2D_ARG_EXPAND GLenum target, GLint level, GLenum internalformat, GLint x, GLint y, GLsizei width, GLsizei height, GLint border
#define glCopyTexImage2D_PACKED PACKED_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint
#define glCopyTexImage2D_INDEXED INDEXED_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint
#define glCopyTexImage2D_FORMAT FORMAT_void_GLenum_GLint_GLenum_GLint_GLint_GLsizei_GLsizei_GLint
#define glCopyTexSubImage2D_INDEX 36
#define glCopyTexSubImage2D_RETURN void
#define glCopyTexSubImage2D_ARG_NAMES target, level, xoffset, yoffset, x, y, width, height
#define glCopyTexSubImage2D_ARG_EXPAND GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint x, GLint y, GLsizei width, GLsizei height
#define glCopyTexSubImage2D_PACKED PACKED_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei
#define glCopyTexSubImage2D_INDEXED INDEXED_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei
#define glCopyTexSubImage2D_FORMAT FORMAT_void_GLenum_GLint_GLint_GLint_GLint_GLint_GLsizei_GLsizei
#define glCreateProgram_INDEX 37
#define glCreateProgram_RETURN GLuint
#define glCreateProgram_ARG_NAMES 
#define glCreateProgram_ARG_EXPAND void
#define glCreateProgram_PACKED PACKED_GLuint
#define glCreateProgram_INDEXED INDEXED_GLuint
#define glCreateProgram_FORMAT FORMAT_GLuint
#define glCreateShader_INDEX 38
#define glCreateShader_RETURN GLuint
#define glCreateShader_ARG_NAMES type
#define glCreateShader_ARG_EXPAND GLenum type
#define glCreateShader_PACKED PACKED_GLuint_GLenum
#define glCreateShader_INDEXED INDEXED_GLuint_GLenum
#define glCreateShader_FORMAT FORMAT_GLuint_GLenum
#define glCullFace_INDEX 39
#define glCullFace_RETURN void
#define glCullFace_ARG_NAMES mode
#define glCullFace_ARG_EXPAND GLenum mode
#define glCullFace_PACKED PACKED_void_GLenum
#define glCullFace_INDEXED INDEXED_void_GLenum
#define glCullFace_FORMAT FORMAT_void_GLenum
#define glDeleteBuffers_INDEX 40
#define glDeleteBuffers_RETURN void
#define glDeleteBuffers_ARG_NAMES n, buffer
#define glDeleteBuffers_ARG_EXPAND GLsizei n, const GLuint * buffer
#define glDeleteBuffers_PACKED PACKED_void_GLsizei_const_GLuint___GENPT__
#define glDeleteBuffers_INDEXED INDEXED_void_GLsizei_const_GLuint___GENPT__
#define glDeleteBuffers_FORMAT FORMAT_void_GLsizei_const_GLuint___GENPT__
#define glDeleteFramebuffers_INDEX 41
#define glDeleteFramebuffers_RETURN void
#define glDeleteFramebuffers_ARG_NAMES n, framebuffers
#define glDeleteFramebuffers_ARG_EXPAND GLsizei n, GLuint * framebuffers
#define glDeleteFramebuffers_PACKED PACKED_void_GLsizei_GLuint___GENPT__
#define glDeleteFramebuffers_INDEXED INDEXED_void_GLsizei_GLuint___GENPT__
#define glDeleteFramebuffers_FORMAT FORMAT_void_GLsizei_GLuint___GENPT__
#define glDeleteProgram_INDEX 42
#define glDeleteProgram_RETURN void
#define glDeleteProgram_ARG_NAMES program
#define glDeleteProgram_ARG_EXPAND GLuint program
#define glDeleteProgram_PACKED PACKED_void_GLuint
#define glDeleteProgram_INDEXED INDEXED_void_GLuint
#define glDeleteProgram_FORMAT FORMAT_void_GLuint
#define glDeleteRenderbuffers_INDEX 43
#define glDeleteRenderbuffers_RETURN void
#define glDeleteRenderbuffers_ARG_NAMES n, renderbuffers
#define glDeleteRenderbuffers_ARG_EXPAND GLsizei n, GLuint * renderbuffers
#define glDeleteRenderbuffers_PACKED PACKED_void_GLsizei_GLuint___GENPT__
#define glDeleteRenderbuffers_INDEXED INDEXED_void_GLsizei_GLuint___GENPT__
#define glDeleteRenderbuffers_FORMAT FORMAT_void_GLsizei_GLuint___GENPT__
#define glDeleteShader_INDEX 44
#define glDeleteShader_RETURN void
#define glDeleteShader_ARG_NAMES shader
#define glDeleteShader_ARG_EXPAND GLuint shader
#define glDeleteShader_PACKED PACKED_void_GLuint
#define glDeleteShader_INDEXED INDEXED_void_GLuint
#define glDeleteShader_FORMAT FORMAT_void_GLuint
#define glDeleteTextures_INDEX 45
#define glDeleteTextures_RETURN void
#define glDeleteTextures_ARG_NAMES n, textures
#define glDeleteTextures_ARG_EXPAND GLsizei n, const GLuint * textures
#define glDeleteTextures_PACKED PACKED_void_GLsizei_const_GLuint___GENPT__
#define glDeleteTextures_INDEXED INDEXED_void_GLsizei_const_GLuint___GENPT__
#define glDeleteTextures_FORMAT FORMAT_void_GLsizei_const_GLuint___GENPT__
#define glDepthFunc_INDEX 46
#define glDepthFunc_RETURN void
#define glDepthFunc_ARG_NAMES func
#define glDepthFunc_ARG_EXPAND GLenum func
#define glDepthFunc_PACKED PACKED_void_GLenum
#define glDepthFunc_INDEXED INDEXED_void_GLenum
#define glDepthFunc_FORMAT FORMAT_void_GLenum
#define glDepthMask_INDEX 47
#define glDepthMask_RETURN void
#define glDepthMask_ARG_NAMES flag
#define glDepthMask_ARG_EXPAND GLboolean flag
#define glDepthMask_PACKED PACKED_void_GLboolean
#define glDepthMask_INDEXED INDEXED_void_GLboolean
#define glDepthMask_FORMAT FORMAT_void_GLboolean
#define glDepthRangef_INDEX 48
#define glDepthRangef_RETURN void
#define glDepthRangef_ARG_NAMES Near, Far
#define glDepthRangef_ARG_EXPAND GLclampf Near, GLclampf Far
#define glDepthRangef_PACKED PACKED_void_GLclampf_GLclampf
#define glDepthRangef_INDEXED INDEXED_void_GLclampf_GLclampf
#define glDepthRangef_FORMAT FORMAT_void_GLclampf_GLclampf
#define glDepthRangex_INDEX 49
#define glDepthRangex_RETURN void
#define glDepthRangex_ARG_NAMES Near, Far
#define glDepthRangex_ARG_EXPAND GLclampx Near, GLclampx Far
#define glDepthRangex_PACKED PACKED_void_GLclampx_GLclampx
#define glDepthRangex_INDEXED INDEXED_void_GLclampx_GLclampx
#define glDepthRangex_FORMAT FORMAT_void_GLclampx_GLclampx
#define glDetachShader_INDEX 50
#define glDetachShader_RETURN void
#define glDetachShader_ARG_NAMES program, shader
#define glDetachShader_ARG_EXPAND GLuint program, GLuint shader
#define glDetachShader_PACKED PACKED_void_GLuint_GLuint
#define glDetachShader_INDEXED INDEXED_void_GLuint_GLuint
#define glDetachShader_FORMAT FORMAT_void_GLuint_GLuint
#define glDisable_INDEX 51
#define glDisable_RETURN void
#define glDisable_ARG_NAMES cap
#define glDisable_ARG_EXPAND GLenum cap
#define glDisable_PACKED PACKED_void_GLenum
#define glDisable_INDEXED INDEXED_void_GLenum
#define glDisable_FORMAT FORMAT_void_GLenum
#define glDisableClientState_INDEX 52
#define glDisableClientState_RETURN void
#define glDisableClientState_ARG_NAMES array
#define glDisableClientState_ARG_EXPAND GLenum array
#define glDisableClientState_PACKED PACKED_void_GLenum
#define glDisableClientState_INDEXED INDEXED_void_GLenum
#define glDisableClientState_FORMAT FORMAT_void_GLenum
#define glDisableVertexAttribArray_INDEX 53
#define glDisableVertexAttribArray_RETURN void
#define glDisableVertexAttribArray_ARG_NAMES index
#define glDisableVertexAttribArray_ARG_EXPAND GLuint index
#define glDisableVertexAttribArray_PACKED PACKED_void_GLuint
#define glDisableVertexAttribArray_INDEXED INDEXED_void_GLuint
#define glDisableVertexAttribArray_FORMAT FORMAT_void_GLuint
#define glDrawArrays_INDEX 54
#define glDrawArrays_RETURN void
#define glDrawArrays_ARG_NAMES mode, first, count
#define glDrawArrays_ARG_EXPAND GLenum mode, GLint first, GLsizei count
#define glDrawArrays_PACKED PACKED_void_GLenum_GLint_GLsizei
#define glDrawArrays_INDEXED INDEXED_void_GLenum_GLint_GLsizei
#define glDrawArrays_FORMAT FORMAT_void_GLenum_GLint_GLsizei
#define glDrawBuffers_INDEX 55
#define glDrawBuffers_RETURN void
#define glDrawBuffers_ARG_NAMES n, bufs
#define glDrawBuffers_ARG_EXPAND GLsizei n, const GLenum * bufs
#define glDrawBuffers_PACKED PACKED_void_GLsizei_const_GLenum___GENPT__
#define glDrawBuffers_INDEXED INDEXED_void_GLsizei_const_GLenum___GENPT__
#define glDrawBuffers_FORMAT FORMAT_void_GLsizei_const_GLenum___GENPT__
#define glDrawElements_INDEX 56
#define glDrawElements_RETURN void
#define glDrawElements_ARG_NAMES mode, count, type, indices
#define glDrawElements_ARG_EXPAND GLenum mode, GLsizei count, GLenum type, const GLvoid * indices
#define glDrawElements_PACKED PACKED_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__
#define glDrawElements_INDEXED INDEXED_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__
#define glDrawElements_FORMAT FORMAT_void_GLenum_GLsizei_GLenum_const_GLvoid___GENPT__
#define glDrawTexf_INDEX 57
#define glDrawTexf_RETURN void
#define glDrawTexf_ARG_NAMES x, y, z, width, height
#define glDrawTexf_ARG_EXPAND GLfloat x, GLfloat y, GLfloat z, GLfloat width, GLfloat height
#define glDrawTexf_PACKED PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glDrawTexf_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glDrawTexf_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glDrawTexi_INDEX 58
#define glDrawTexi_RETURN void
#define glDrawTexi_ARG_NAMES x, y, z, width, height
#define glDrawTexi_ARG_EXPAND GLint x, GLint y, GLint z, GLint width, GLint height
#define glDrawTexi_PACKED PACKED_void_GLint_GLint_GLint_GLint_GLint
#define glDrawTexi_INDEXED INDEXED_void_GLint_GLint_GLint_GLint_GLint
#define glDrawTexi_FORMAT FORMAT_void_GLint_GLint_GLint_GLint_GLint
#define glEnable_INDEX 59
#define glEnable_RETURN void
#define glEnable_ARG_NAMES cap
#define glEnable_ARG_EXPAND GLenum cap
#define glEnable_PACKED PACKED_void_GLenum
#define glEnable_INDEXED INDEXED_void_GLenum
#define glEnable_FORMAT FORMAT_void_GLenum
#define glEnableClientState_INDEX 60
#define glEnableClientState_RETURN void
#define glEnableClientState_ARG_NAMES array
#define glEnableClientState_ARG_EXPAND GLenum array
#define glEnableClientState_PACKED PACKED_void_GLenum
#define glEnableClientState_INDEXED INDEXED_void_GLenum
#define glEnableClientState_FORMAT FORMAT_void_GLenum
#define glEnableVertexAttribArray_INDEX 61
#define glEnableVertexAttribArray_RETURN void
#define glEnableVertexAttribArray_ARG_NAMES index
#define glEnableVertexAttribArray_ARG_EXPAND GLuint index
#define glEnableVertexAttribArray_PACKED PACKED_void_GLuint
#define glEnableVertexAttribArray_INDEXED INDEXED_void_GLuint
#define glEnableVertexAttribArray_FORMAT FORMAT_void_GLuint
#define glFinish_INDEX 62
#define glFinish_RETURN void
#define glFinish_ARG_NAMES 
#define glFinish_ARG_EXPAND void
#define glFinish_PACKED PACKED_void
#define glFinish_INDEXED INDEXED_void
#define glFinish_FORMAT FORMAT_void
#define glFlush_INDEX 63
#define glFlush_RETURN void
#define glFlush_ARG_NAMES 
#define glFlush_ARG_EXPAND void
#define glFlush_PACKED PACKED_void
#define glFlush_INDEXED INDEXED_void
#define glFlush_FORMAT FORMAT_void
#define glFogCoordPointer_INDEX 64
#define glFogCoordPointer_RETURN void
#define glFogCoordPointer_ARG_NAMES type, stride, pointer
#define glFogCoordPointer_ARG_EXPAND GLenum type, GLsizei stride, const GLvoid * pointer
#define glFogCoordPointer_PACKED PACKED_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glFogCoordPointer_INDEXED INDEXED_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glFogCoordPointer_FORMAT FORMAT_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glFogCoordf_INDEX 65
#define glFogCoordf_RETURN void
#define glFogCoordf_ARG_NAMES coord
#define glFogCoordf_ARG_EXPAND GLfloat coord
#define glFogCoordf_PACKED PACKED_void_GLfloat
#define glFogCoordf_INDEXED INDEXED_void_GLfloat
#define glFogCoordf_FORMAT FORMAT_void_GLfloat
#define glFogCoordfv_INDEX 66
#define glFogCoordfv_RETURN void
#define glFogCoordfv_ARG_NAMES coord
#define glFogCoordfv_ARG_EXPAND const GLfloat * coord
#define glFogCoordfv_PACKED PACKED_void_const_GLfloat___GENPT__
#define glFogCoordfv_INDEXED INDEXED_void_const_GLfloat___GENPT__
#define glFogCoordfv_FORMAT FORMAT_void_const_GLfloat___GENPT__
#define glFogf_INDEX 67
#define glFogf_RETURN void
#define glFogf_ARG_NAMES pname, param
#define glFogf_ARG_EXPAND GLenum pname, GLfloat param
#define glFogf_PACKED PACKED_void_GLenum_GLfloat
#define glFogf_INDEXED INDEXED_void_GLenum_GLfloat
#define glFogf_FORMAT FORMAT_void_GLenum_GLfloat
#define glFogfv_INDEX 68
#define glFogfv_RETURN void
#define glFogfv_ARG_NAMES pname, params
#define glFogfv_ARG_EXPAND GLenum pname, const GLfloat * params
#define glFogfv_PACKED PACKED_void_GLenum_const_GLfloat___GENPT__
#define glFogfv_INDEXED INDEXED_void_GLenum_const_GLfloat___GENPT__
#define glFogfv_FORMAT FORMAT_void_GLenum_const_GLfloat___GENPT__
#define glFogx_INDEX 69
#define glFogx_RETURN void
#define glFogx_ARG_NAMES pname, param
#define glFogx_ARG_EXPAND GLenum pname, GLfixed param
#define glFogx_PACKED PACKED_void_GLenum_GLfixed
#define glFogx_INDEXED INDEXED_void_GLenum_GLfixed
#define glFogx_FORMAT FORMAT_void_GLenum_GLfixed
#define glFogxv_INDEX 70
#define glFogxv_RETURN void
#define glFogxv_ARG_NAMES pname, params
#define glFogxv_ARG_EXPAND GLenum pname, const GLfixed * params
#define glFogxv_PACKED PACKED_void_GLenum_const_GLfixed___GENPT__
#define glFogxv_INDEXED INDEXED_void_GLenum_const_GLfixed___GENPT__
#define glFogxv_FORMAT FORMAT_void_GLenum_const_GLfixed___GENPT__
#define glFramebufferRenderbuffer_INDEX 71
#define glFramebufferRenderbuffer_RETURN void
#define glFramebufferRenderbuffer_ARG_NAMES target, attachment, renderbuffertarget, renderbuffer
#define glFramebufferRenderbuffer_ARG_EXPAND GLenum target, GLenum attachment, GLenum renderbuffertarget, GLuint renderbuffer
#define glFramebufferRenderbuffer_PACKED PACKED_void_GLenum_GLenum_GLenum_GLuint
#define glFramebufferRenderbuffer_INDEXED INDEXED_void_GLenum_GLenum_GLenum_GLuint
#define glFramebufferRenderbuffer_FORMAT FORMAT_void_GLenum_GLenum_GLenum_GLuint
#define glFramebufferTexture2D_INDEX 72
#define glFramebufferTexture2D_RETURN void
#define glFramebufferTexture2D_ARG_NAMES target, attachment, textarget, texture, level
#define glFramebufferTexture2D_ARG_EXPAND GLenum target, GLenum attachment, GLenum textarget, GLuint texture, GLint level
#define glFramebufferTexture2D_PACKED PACKED_void_GLenum_GLenum_GLenum_GLuint_GLint
#define glFramebufferTexture2D_INDEXED INDEXED_void_GLenum_GLenum_GLenum_GLuint_GLint
#define glFramebufferTexture2D_FORMAT FORMAT_void_GLenum_GLenum_GLenum_GLuint_GLint
#define glFrontFace_INDEX 73
#define glFrontFace_RETURN void
#define glFrontFace_ARG_NAMES mode
#define glFrontFace_ARG_EXPAND GLenum mode
#define glFrontFace_PACKED PACKED_void_GLenum
#define glFrontFace_INDEXED INDEXED_void_GLenum
#define glFrontFace_FORMAT FORMAT_void_GLenum
#define glFrustumf_INDEX 74
#define glFrustumf_RETURN void
#define glFrustumf_ARG_NAMES left, right, bottom, top, Near, Far
#define glFrustumf_ARG_EXPAND GLfloat left, GLfloat right, GLfloat bottom, GLfloat top, GLfloat Near, GLfloat Far
#define glFrustumf_PACKED PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glFrustumf_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glFrustumf_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glFrustumx_INDEX 75
#define glFrustumx_RETURN void
#define glFrustumx_ARG_NAMES left, right, bottom, top, Near, Far
#define glFrustumx_ARG_EXPAND GLfixed left, GLfixed right, GLfixed bottom, GLfixed top, GLfixed Near, GLfixed Far
#define glFrustumx_PACKED PACKED_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed
#define glFrustumx_INDEXED INDEXED_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed
#define glFrustumx_FORMAT FORMAT_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed
#define glGenBuffers_INDEX 76
#define glGenBuffers_RETURN void
#define glGenBuffers_ARG_NAMES n, buffer
#define glGenBuffers_ARG_EXPAND GLsizei n, GLuint * buffer
#define glGenBuffers_PACKED PACKED_void_GLsizei_GLuint___GENPT__
#define glGenBuffers_INDEXED INDEXED_void_GLsizei_GLuint___GENPT__
#define glGenBuffers_FORMAT FORMAT_void_GLsizei_GLuint___GENPT__
#define glGenFramebuffers_INDEX 77
#define glGenFramebuffers_RETURN void
#define glGenFramebuffers_ARG_NAMES n, ids
#define glGenFramebuffers_ARG_EXPAND GLsizei n, GLuint * ids
#define glGenFramebuffers_PACKED PACKED_void_GLsizei_GLuint___GENPT__
#define glGenFramebuffers_INDEXED INDEXED_void_GLsizei_GLuint___GENPT__
#define glGenFramebuffers_FORMAT FORMAT_void_GLsizei_GLuint___GENPT__
#define glGenRenderbuffers_INDEX 78
#define glGenRenderbuffers_RETURN void
#define glGenRenderbuffers_ARG_NAMES n, renderbuffers
#define glGenRenderbuffers_ARG_EXPAND GLsizei n, GLuint * renderbuffers
#define glGenRenderbuffers_PACKED PACKED_void_GLsizei_GLuint___GENPT__
#define glGenRenderbuffers_INDEXED INDEXED_void_GLsizei_GLuint___GENPT__
#define glGenRenderbuffers_FORMAT FORMAT_void_GLsizei_GLuint___GENPT__
#define glGenTextures_INDEX 79
#define glGenTextures_RETURN void
#define glGenTextures_ARG_NAMES n, textures
#define glGenTextures_ARG_EXPAND GLsizei n, GLuint * textures
#define glGenTextures_PACKED PACKED_void_GLsizei_GLuint___GENPT__
#define glGenTextures_INDEXED INDEXED_void_GLsizei_GLuint___GENPT__
#define glGenTextures_FORMAT FORMAT_void_GLsizei_GLuint___GENPT__
#define glGenerateMipmap_INDEX 80
#define glGenerateMipmap_RETURN void
#define glGenerateMipmap_ARG_NAMES target
#define glGenerateMipmap_ARG_EXPAND GLenum target
#define glGenerateMipmap_PACKED PACKED_void_GLenum
#define glGenerateMipmap_INDEXED INDEXED_void_GLenum
#define glGenerateMipmap_FORMAT FORMAT_void_GLenum
#define glGetActiveAttrib_INDEX 81
#define glGetActiveAttrib_RETURN void
#define glGetActiveAttrib_ARG_NAMES program, index, bufSize, length, size, type, name
#define glGetActiveAttrib_ARG_EXPAND GLuint program, GLuint index, GLsizei bufSize, GLsizei * length, GLint * size, GLenum * type, GLchar * name
#define glGetActiveAttrib_PACKED PACKED_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__
#define glGetActiveAttrib_INDEXED INDEXED_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__
#define glGetActiveAttrib_FORMAT FORMAT_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__
#define glGetActiveUniform_INDEX 82
#define glGetActiveUniform_RETURN void
#define glGetActiveUniform_ARG_NAMES program, index, bufSize, length, size, type, name
#define glGetActiveUniform_ARG_EXPAND GLuint program, GLuint index, GLsizei bufSize, GLsizei * length, GLint * size, GLenum * type, GLchar * name
#define glGetActiveUniform_PACKED PACKED_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__
#define glGetActiveUniform_INDEXED INDEXED_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__
#define glGetActiveUniform_FORMAT FORMAT_void_GLuint_GLuint_GLsizei_GLsizei___GENPT___GLint___GENPT___GLenum___GENPT___GLchar___GENPT__
#define glGetAttachedShaders_INDEX 83
#define glGetAttachedShaders_RETURN void
#define glGetAttachedShaders_ARG_NAMES program, maxCount, count, obj
#define glGetAttachedShaders_ARG_EXPAND GLuint program, GLsizei maxCount, GLsizei * count, GLuint * obj
#define glGetAttachedShaders_PACKED PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__
#define glGetAttachedShaders_INDEXED INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__
#define glGetAttachedShaders_FORMAT FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLuint___GENPT__
#define glGetAttribLocation_INDEX 84
#define glGetAttribLocation_RETURN GLint
#define glGetAttribLocation_ARG_NAMES program, name
#define glGetAttribLocation_ARG_EXPAND GLuint program, const GLchar * name
#define glGetAttribLocation_PACKED PACKED_GLint_GLuint_const_GLchar___GENPT__
#define glGetAttribLocation_INDEXED INDEXED_GLint_GLuint_const_GLchar___GENPT__
#define glGetAttribLocation_FORMAT FORMAT_GLint_GLuint_const_GLchar___GENPT__
#define glGetBooleanv_INDEX 85
#define glGetBooleanv_RETURN void
#define glGetBooleanv_ARG_NAMES pname, params
#define glGetBooleanv_ARG_EXPAND GLenum pname, GLboolean * params
#define glGetBooleanv_PACKED PACKED_void_GLenum_GLboolean___GENPT__
#define glGetBooleanv_INDEXED INDEXED_void_GLenum_GLboolean___GENPT__
#define glGetBooleanv_FORMAT FORMAT_void_GLenum_GLboolean___GENPT__
#define glGetBufferParameteriv_INDEX 86
#define glGetBufferParameteriv_RETURN void
#define glGetBufferParameteriv_ARG_NAMES target, pname, params
#define glGetBufferParameteriv_ARG_EXPAND GLenum target, GLenum pname, GLint * params
#define glGetBufferParameteriv_PACKED PACKED_void_GLenum_GLenum_GLint___GENPT__
#define glGetBufferParameteriv_INDEXED INDEXED_void_GLenum_GLenum_GLint___GENPT__
#define glGetBufferParameteriv_FORMAT FORMAT_void_GLenum_GLenum_GLint___GENPT__
#define glGetClipPlanef_INDEX 87
#define glGetClipPlanef_RETURN void
#define glGetClipPlanef_ARG_NAMES plane, equation
#define glGetClipPlanef_ARG_EXPAND GLenum plane, GLfloat * equation
#define glGetClipPlanef_PACKED PACKED_void_GLenum_GLfloat___GENPT__
#define glGetClipPlanef_INDEXED INDEXED_void_GLenum_GLfloat___GENPT__
#define glGetClipPlanef_FORMAT FORMAT_void_GLenum_GLfloat___GENPT__
#define glGetClipPlanex_INDEX 88
#define glGetClipPlanex_RETURN void
#define glGetClipPlanex_ARG_NAMES plane, equation
#define glGetClipPlanex_ARG_EXPAND GLenum plane, GLfixed * equation
#define glGetClipPlanex_PACKED PACKED_void_GLenum_GLfixed___GENPT__
#define glGetClipPlanex_INDEXED INDEXED_void_GLenum_GLfixed___GENPT__
#define glGetClipPlanex_FORMAT FORMAT_void_GLenum_GLfixed___GENPT__
#define glGetError_INDEX 89
#define glGetError_RETURN GLenum
#define glGetError_ARG_NAMES 
#define glGetError_ARG_EXPAND void
#define glGetError_PACKED PACKED_GLenum
#define glGetError_INDEXED INDEXED_GLenum
#define glGetError_FORMAT FORMAT_GLenum
#define glGetFixedv_INDEX 90
#define glGetFixedv_RETURN void
#define glGetFixedv_ARG_NAMES pname, params
#define glGetFixedv_ARG_EXPAND GLenum pname, GLfixed * params
#define glGetFixedv_PACKED PACKED_void_GLenum_GLfixed___GENPT__
#define glGetFixedv_INDEXED INDEXED_void_GLenum_GLfixed___GENPT__
#define glGetFixedv_FORMAT FORMAT_void_GLenum_GLfixed___GENPT__
#define glGetFloatv_INDEX 91
#define glGetFloatv_RETURN void
#define glGetFloatv_ARG_NAMES pname, params
#define glGetFloatv_ARG_EXPAND GLenum pname, GLfloat * params
#define glGetFloatv_PACKED PACKED_void_GLenum_GLfloat___GENPT__
#define glGetFloatv_INDEXED INDEXED_void_GLenum_GLfloat___GENPT__
#define glGetFloatv_FORMAT FORMAT_void_GLenum_GLfloat___GENPT__
#define glGetFramebufferAttachmentParameteriv_INDEX 92
#define glGetFramebufferAttachmentParameteriv_RETURN void
#define glGetFramebufferAttachmentParameteriv_ARG_NAMES target, attachment, pname, params
#define glGetFramebufferAttachmentParameteriv_ARG_EXPAND GLenum target, GLenum attachment, GLenum pname, GLint * params
#define glGetFramebufferAttachmentParameteriv_PACKED PACKED_void_GLenum_GLenum_GLenum_GLint___GENPT__
#define glGetFramebufferAttachmentParameteriv_INDEXED INDEXED_void_GLenum_GLenum_GLenum_GLint___GENPT__
#define glGetFramebufferAttachmentParameteriv_FORMAT FORMAT_void_GLenum_GLenum_GLenum_GLint___GENPT__
#define glGetIntegerv_INDEX 93
#define glGetIntegerv_RETURN void
#define glGetIntegerv_ARG_NAMES pname, params
#define glGetIntegerv_ARG_EXPAND GLenum pname, GLint * params
#define glGetIntegerv_PACKED PACKED_void_GLenum_GLint___GENPT__
#define glGetIntegerv_INDEXED INDEXED_void_GLenum_GLint___GENPT__
#define glGetIntegerv_FORMAT FORMAT_void_GLenum_GLint___GENPT__
#define glGetLightfv_INDEX 94
#define glGetLightfv_RETURN void
#define glGetLightfv_ARG_NAMES light, pname, params
#define glGetLightfv_ARG_EXPAND GLenum light, GLenum pname, GLfloat * params
#define glGetLightfv_PACKED PACKED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetLightfv_INDEXED INDEXED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetLightfv_FORMAT FORMAT_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetLightxv_INDEX 95
#define glGetLightxv_RETURN void
#define glGetLightxv_ARG_NAMES light, pname, params
#define glGetLightxv_ARG_EXPAND GLenum light, GLenum pname, GLfixed * params
#define glGetLightxv_PACKED PACKED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetLightxv_INDEXED INDEXED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetLightxv_FORMAT FORMAT_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetMaterialfv_INDEX 96
#define glGetMaterialfv_RETURN void
#define glGetMaterialfv_ARG_NAMES face, pname, params
#define glGetMaterialfv_ARG_EXPAND GLenum face, GLenum pname, GLfloat * params
#define glGetMaterialfv_PACKED PACKED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetMaterialfv_INDEXED INDEXED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetMaterialfv_FORMAT FORMAT_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetMaterialxv_INDEX 97
#define glGetMaterialxv_RETURN void
#define glGetMaterialxv_ARG_NAMES face, pname, params
#define glGetMaterialxv_ARG_EXPAND GLenum face, GLenum pname, GLfixed * params
#define glGetMaterialxv_PACKED PACKED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetMaterialxv_INDEXED INDEXED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetMaterialxv_FORMAT FORMAT_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetPointerv_INDEX 98
#define glGetPointerv_RETURN void
#define glGetPointerv_ARG_NAMES pname, params
#define glGetPointerv_ARG_EXPAND GLenum pname, GLvoid ** params
#define glGetPointerv_PACKED PACKED_void_GLenum_GLvoid___GENPT____GENPT__
#define glGetPointerv_INDEXED INDEXED_void_GLenum_GLvoid___GENPT____GENPT__
#define glGetPointerv_FORMAT FORMAT_void_GLenum_GLvoid___GENPT____GENPT__
#define glGetProgramBinary_INDEX 99
#define glGetProgramBinary_RETURN void
#define glGetProgramBinary_ARG_NAMES program, bufSize, length, binaryFormat, binary
#define glGetProgramBinary_ARG_EXPAND GLuint program, GLsizei bufSize, GLsizei * length, GLenum * binaryFormat, GLvoid * binary
#define glGetProgramBinary_PACKED PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__
#define glGetProgramBinary_INDEXED INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__
#define glGetProgramBinary_FORMAT FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLenum___GENPT___GLvoid___GENPT__
#define glGetProgramInfoLog_INDEX 100
#define glGetProgramInfoLog_RETURN void
#define glGetProgramInfoLog_ARG_NAMES program, bufSize, length, infoLog
#define glGetProgramInfoLog_ARG_EXPAND GLuint program, GLsizei bufSize, GLsizei * length, GLchar * infoLog
#define glGetProgramInfoLog_PACKED PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetProgramInfoLog_INDEXED INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetProgramInfoLog_FORMAT FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetProgramiv_INDEX 101
#define glGetProgramiv_RETURN void
#define glGetProgramiv_ARG_NAMES program, pname, params
#define glGetProgramiv_ARG_EXPAND GLuint program, GLenum pname, GLint * params
#define glGetProgramiv_PACKED PACKED_void_GLuint_GLenum_GLint___GENPT__
#define glGetProgramiv_INDEXED INDEXED_void_GLuint_GLenum_GLint___GENPT__
#define glGetProgramiv_FORMAT FORMAT_void_GLuint_GLenum_GLint___GENPT__
#define glGetRenderbufferParameteriv_INDEX 102
#define glGetRenderbufferParameteriv_RETURN void
#define glGetRenderbufferParameteriv_ARG_NAMES target, pname, params
#define glGetRenderbufferParameteriv_ARG_EXPAND GLenum target, GLenum pname, GLint * params
#define glGetRenderbufferParameteriv_PACKED PACKED_void_GLenum_GLenum_GLint___GENPT__
#define glGetRenderbufferParameteriv_INDEXED INDEXED_void_GLenum_GLenum_GLint___GENPT__
#define glGetRenderbufferParameteriv_FORMAT FORMAT_void_GLenum_GLenum_GLint___GENPT__
#define glGetShaderInfoLog_INDEX 103
#define glGetShaderInfoLog_RETURN void
#define glGetShaderInfoLog_ARG_NAMES shader, bufSize, length, infoLog
#define glGetShaderInfoLog_ARG_EXPAND GLuint shader, GLsizei bufSize, GLsizei * length, GLchar * infoLog
#define glGetShaderInfoLog_PACKED PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetShaderInfoLog_INDEXED INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetShaderInfoLog_FORMAT FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetShaderPrecisionFormat_INDEX 104
#define glGetShaderPrecisionFormat_RETURN void
#define glGetShaderPrecisionFormat_ARG_NAMES shadertype, precisiontype, range, precision
#define glGetShaderPrecisionFormat_ARG_EXPAND GLenum shadertype, GLenum precisiontype, GLint * range, GLint * precision
#define glGetShaderPrecisionFormat_PACKED PACKED_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__
#define glGetShaderPrecisionFormat_INDEXED INDEXED_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__
#define glGetShaderPrecisionFormat_FORMAT FORMAT_void_GLenum_GLenum_GLint___GENPT___GLint___GENPT__
#define glGetShaderSource_INDEX 105
#define glGetShaderSource_RETURN void
#define glGetShaderSource_ARG_NAMES shader, bufSize, length, source
#define glGetShaderSource_ARG_EXPAND GLuint shader, GLsizei bufSize, GLsizei * length, GLchar * source
#define glGetShaderSource_PACKED PACKED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetShaderSource_INDEXED INDEXED_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetShaderSource_FORMAT FORMAT_void_GLuint_GLsizei_GLsizei___GENPT___GLchar___GENPT__
#define glGetShaderiv_INDEX 106
#define glGetShaderiv_RETURN void
#define glGetShaderiv_ARG_NAMES shader, pname, params
#define glGetShaderiv_ARG_EXPAND GLuint shader, GLenum pname, GLint * params
#define glGetShaderiv_PACKED PACKED_void_GLuint_GLenum_GLint___GENPT__
#define glGetShaderiv_INDEXED INDEXED_void_GLuint_GLenum_GLint___GENPT__
#define glGetShaderiv_FORMAT FORMAT_void_GLuint_GLenum_GLint___GENPT__
#define glGetString_INDEX 107
#define glGetString_RETURN const GLubyte *
#define glGetString_ARG_NAMES name
#define glGetString_ARG_EXPAND GLenum name
#define glGetString_PACKED PACKED_const_GLubyte___GENPT___GLenum
#define glGetString_INDEXED INDEXED_const_GLubyte___GENPT___GLenum
#define glGetString_FORMAT FORMAT_const_GLubyte___GENPT___GLenum
#define glGetTexEnvfv_INDEX 108
#define glGetTexEnvfv_RETURN void
#define glGetTexEnvfv_ARG_NAMES target, pname, params
#define glGetTexEnvfv_ARG_EXPAND GLenum target, GLenum pname, GLfloat * params
#define glGetTexEnvfv_PACKED PACKED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetTexEnvfv_INDEXED INDEXED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetTexEnvfv_FORMAT FORMAT_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetTexEnviv_INDEX 109
#define glGetTexEnviv_RETURN void
#define glGetTexEnviv_ARG_NAMES target, pname, params
#define glGetTexEnviv_ARG_EXPAND GLenum target, GLenum pname, GLint * params
#define glGetTexEnviv_PACKED PACKED_void_GLenum_GLenum_GLint___GENPT__
#define glGetTexEnviv_INDEXED INDEXED_void_GLenum_GLenum_GLint___GENPT__
#define glGetTexEnviv_FORMAT FORMAT_void_GLenum_GLenum_GLint___GENPT__
#define glGetTexEnvxv_INDEX 110
#define glGetTexEnvxv_RETURN void
#define glGetTexEnvxv_ARG_NAMES target, pname, params
#define glGetTexEnvxv_ARG_EXPAND GLenum target, GLenum pname, GLfixed * params
#define glGetTexEnvxv_PACKED PACKED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetTexEnvxv_INDEXED INDEXED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetTexEnvxv_FORMAT FORMAT_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetTexParameterfv_INDEX 111
#define glGetTexParameterfv_RETURN void
#define glGetTexParameterfv_ARG_NAMES target, pname, params
#define glGetTexParameterfv_ARG_EXPAND GLenum target, GLenum pname, GLfloat * params
#define glGetTexParameterfv_PACKED PACKED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetTexParameterfv_INDEXED INDEXED_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetTexParameterfv_FORMAT FORMAT_void_GLenum_GLenum_GLfloat___GENPT__
#define glGetTexParameteriv_INDEX 112
#define glGetTexParameteriv_RETURN void
#define glGetTexParameteriv_ARG_NAMES target, pname, params
#define glGetTexParameteriv_ARG_EXPAND GLenum target, GLenum pname, GLint * params
#define glGetTexParameteriv_PACKED PACKED_void_GLenum_GLenum_GLint___GENPT__
#define glGetTexParameteriv_INDEXED INDEXED_void_GLenum_GLenum_GLint___GENPT__
#define glGetTexParameteriv_FORMAT FORMAT_void_GLenum_GLenum_GLint___GENPT__
#define glGetTexParameterxv_INDEX 113
#define glGetTexParameterxv_RETURN void
#define glGetTexParameterxv_ARG_NAMES target, pname, params
#define glGetTexParameterxv_ARG_EXPAND GLenum target, GLenum pname, GLfixed * params
#define glGetTexParameterxv_PACKED PACKED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetTexParameterxv_INDEXED INDEXED_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetTexParameterxv_FORMAT FORMAT_void_GLenum_GLenum_GLfixed___GENPT__
#define glGetUniformLocation_INDEX 114
#define glGetUniformLocation_RETURN GLint
#define glGetUniformLocation_ARG_NAMES program, name
#define glGetUniformLocation_ARG_EXPAND GLuint program, const GLchar * name
#define glGetUniformLocation_PACKED PACKED_GLint_GLuint_const_GLchar___GENPT__
#define glGetUniformLocation_INDEXED INDEXED_GLint_GLuint_const_GLchar___GENPT__
#define glGetUniformLocation_FORMAT FORMAT_GLint_GLuint_const_GLchar___GENPT__
#define glGetUniformfv_INDEX 115
#define glGetUniformfv_RETURN void
#define glGetUniformfv_ARG_NAMES program, location, params
#define glGetUniformfv_ARG_EXPAND GLuint program, GLint location, GLfloat * params
#define glGetUniformfv_PACKED PACKED_void_GLuint_GLint_GLfloat___GENPT__
#define glGetUniformfv_INDEXED INDEXED_void_GLuint_GLint_GLfloat___GENPT__
#define glGetUniformfv_FORMAT FORMAT_void_GLuint_GLint_GLfloat___GENPT__
#define glGetUniformiv_INDEX 116
#define glGetUniformiv_RETURN void
#define glGetUniformiv_ARG_NAMES program, location, params
#define glGetUniformiv_ARG_EXPAND GLuint program, GLint location, GLint * params
#define glGetUniformiv_PACKED PACKED_void_GLuint_GLint_GLint___GENPT__
#define glGetUniformiv_INDEXED INDEXED_void_GLuint_GLint_GLint___GENPT__
#define glGetUniformiv_FORMAT FORMAT_void_GLuint_GLint_GLint___GENPT__
#define glGetVertexAttribPointerv_INDEX 117
#define glGetVertexAttribPointerv_RETURN void
#define glGetVertexAttribPointerv_ARG_NAMES index, pname, pointer
#define glGetVertexAttribPointerv_ARG_EXPAND GLuint index, GLenum pname, GLvoid ** pointer
#define glGetVertexAttribPointerv_PACKED PACKED_void_GLuint_GLenum_GLvoid___GENPT____GENPT__
#define glGetVertexAttribPointerv_INDEXED INDEXED_void_GLuint_GLenum_GLvoid___GENPT____GENPT__
#define glGetVertexAttribPointerv_FORMAT FORMAT_void_GLuint_GLenum_GLvoid___GENPT____GENPT__
#define glGetVertexAttribfv_INDEX 118
#define glGetVertexAttribfv_RETURN void
#define glGetVertexAttribfv_ARG_NAMES index, pname, params
#define glGetVertexAttribfv_ARG_EXPAND GLuint index, GLenum pname, GLfloat * params
#define glGetVertexAttribfv_PACKED PACKED_void_GLuint_GLenum_GLfloat___GENPT__
#define glGetVertexAttribfv_INDEXED INDEXED_void_GLuint_GLenum_GLfloat___GENPT__
#define glGetVertexAttribfv_FORMAT FORMAT_void_GLuint_GLenum_GLfloat___GENPT__
#define glGetVertexAttribiv_INDEX 119
#define glGetVertexAttribiv_RETURN void
#define glGetVertexAttribiv_ARG_NAMES index, pname, params
#define glGetVertexAttribiv_ARG_EXPAND GLuint index, GLenum pname, GLint * params
#define glGetVertexAttribiv_PACKED PACKED_void_GLuint_GLenum_GLint___GENPT__
#define glGetVertexAttribiv_INDEXED INDEXED_void_GLuint_GLenum_GLint___GENPT__
#define glGetVertexAttribiv_FORMAT FORMAT_void_GLuint_GLenum_GLint___GENPT__
#define glHint_INDEX 120
#define glHint_RETURN void
#define glHint_ARG_NAMES target, mode
#define glHint_ARG_EXPAND GLenum target, GLenum mode
#define glHint_PACKED PACKED_void_GLenum_GLenum
#define glHint_INDEXED INDEXED_void_GLenum_GLenum
#define glHint_FORMAT FORMAT_void_GLenum_GLenum
#define glIsBuffer_INDEX 121
#define glIsBuffer_RETURN GLboolean
#define glIsBuffer_ARG_NAMES buffer
#define glIsBuffer_ARG_EXPAND GLuint buffer
#define glIsBuffer_PACKED PACKED_GLboolean_GLuint
#define glIsBuffer_INDEXED INDEXED_GLboolean_GLuint
#define glIsBuffer_FORMAT FORMAT_GLboolean_GLuint
#define glIsEnabled_INDEX 122
#define glIsEnabled_RETURN GLboolean
#define glIsEnabled_ARG_NAMES cap
#define glIsEnabled_ARG_EXPAND GLenum cap
#define glIsEnabled_PACKED PACKED_GLboolean_GLenum
#define glIsEnabled_INDEXED INDEXED_GLboolean_GLenum
#define glIsEnabled_FORMAT FORMAT_GLboolean_GLenum
#define glIsFramebuffer_INDEX 123
#define glIsFramebuffer_RETURN GLboolean
#define glIsFramebuffer_ARG_NAMES framebuffer
#define glIsFramebuffer_ARG_EXPAND GLuint framebuffer
#define glIsFramebuffer_PACKED PACKED_GLboolean_GLuint
#define glIsFramebuffer_INDEXED INDEXED_GLboolean_GLuint
#define glIsFramebuffer_FORMAT FORMAT_GLboolean_GLuint
#define glIsProgram_INDEX 124
#define glIsProgram_RETURN GLboolean
#define glIsProgram_ARG_NAMES program
#define glIsProgram_ARG_EXPAND GLuint program
#define glIsProgram_PACKED PACKED_GLboolean_GLuint
#define glIsProgram_INDEXED INDEXED_GLboolean_GLuint
#define glIsProgram_FORMAT FORMAT_GLboolean_GLuint
#define glIsRenderbuffer_INDEX 125
#define glIsRenderbuffer_RETURN GLboolean
#define glIsRenderbuffer_ARG_NAMES renderbuffer
#define glIsRenderbuffer_ARG_EXPAND GLuint renderbuffer
#define glIsRenderbuffer_PACKED PACKED_GLboolean_GLuint
#define glIsRenderbuffer_INDEXED INDEXED_GLboolean_GLuint
#define glIsRenderbuffer_FORMAT FORMAT_GLboolean_GLuint
#define glIsShader_INDEX 126
#define glIsShader_RETURN GLboolean
#define glIsShader_ARG_NAMES shader
#define glIsShader_ARG_EXPAND GLuint shader
#define glIsShader_PACKED PACKED_GLboolean_GLuint
#define glIsShader_INDEXED INDEXED_GLboolean_GLuint
#define glIsShader_FORMAT FORMAT_GLboolean_GLuint
#define glIsTexture_INDEX 127
#define glIsTexture_RETURN GLboolean
#define glIsTexture_ARG_NAMES texture
#define glIsTexture_ARG_EXPAND GLuint texture
#define glIsTexture_PACKED PACKED_GLboolean_GLuint
#define glIsTexture_INDEXED INDEXED_GLboolean_GLuint
#define glIsTexture_FORMAT FORMAT_GLboolean_GLuint
#define glLightModelf_INDEX 128
#define glLightModelf_RETURN void
#define glLightModelf_ARG_NAMES pname, param
#define glLightModelf_ARG_EXPAND GLenum pname, GLfloat param
#define glLightModelf_PACKED PACKED_void_GLenum_GLfloat
#define glLightModelf_INDEXED INDEXED_void_GLenum_GLfloat
#define glLightModelf_FORMAT FORMAT_void_GLenum_GLfloat
#define glLightModelfv_INDEX 129
#define glLightModelfv_RETURN void
#define glLightModelfv_ARG_NAMES pname, params
#define glLightModelfv_ARG_EXPAND GLenum pname, const GLfloat * params
#define glLightModelfv_PACKED PACKED_void_GLenum_const_GLfloat___GENPT__
#define glLightModelfv_INDEXED INDEXED_void_GLenum_const_GLfloat___GENPT__
#define glLightModelfv_FORMAT FORMAT_void_GLenum_const_GLfloat___GENPT__
#define glLightModelx_INDEX 130
#define glLightModelx_RETURN void
#define glLightModelx_ARG_NAMES pname, param
#define glLightModelx_ARG_EXPAND GLenum pname, GLfixed param
#define glLightModelx_PACKED PACKED_void_GLenum_GLfixed
#define glLightModelx_INDEXED INDEXED_void_GLenum_GLfixed
#define glLightModelx_FORMAT FORMAT_void_GLenum_GLfixed
#define glLightModelxv_INDEX 131
#define glLightModelxv_RETURN void
#define glLightModelxv_ARG_NAMES pname, params
#define glLightModelxv_ARG_EXPAND GLenum pname, const GLfixed * params
#define glLightModelxv_PACKED PACKED_void_GLenum_const_GLfixed___GENPT__
#define glLightModelxv_INDEXED INDEXED_void_GLenum_const_GLfixed___GENPT__
#define glLightModelxv_FORMAT FORMAT_void_GLenum_const_GLfixed___GENPT__
#define glLightf_INDEX 132
#define glLightf_RETURN void
#define glLightf_ARG_NAMES light, pname, param
#define glLightf_ARG_EXPAND GLenum light, GLenum pname, GLfloat param
#define glLightf_PACKED PACKED_void_GLenum_GLenum_GLfloat
#define glLightf_INDEXED INDEXED_void_GLenum_GLenum_GLfloat
#define glLightf_FORMAT FORMAT_void_GLenum_GLenum_GLfloat
#define glLightfv_INDEX 133
#define glLightfv_RETURN void
#define glLightfv_ARG_NAMES light, pname, params
#define glLightfv_ARG_EXPAND GLenum light, GLenum pname, const GLfloat * params
#define glLightfv_PACKED PACKED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glLightfv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glLightfv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glLightx_INDEX 134
#define glLightx_RETURN void
#define glLightx_ARG_NAMES light, pname, param
#define glLightx_ARG_EXPAND GLenum light, GLenum pname, GLfixed param
#define glLightx_PACKED PACKED_void_GLenum_GLenum_GLfixed
#define glLightx_INDEXED INDEXED_void_GLenum_GLenum_GLfixed
#define glLightx_FORMAT FORMAT_void_GLenum_GLenum_GLfixed
#define glLightxv_INDEX 135
#define glLightxv_RETURN void
#define glLightxv_ARG_NAMES light, pname, params
#define glLightxv_ARG_EXPAND GLenum light, GLenum pname, const GLfixed * params
#define glLightxv_PACKED PACKED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glLightxv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glLightxv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glLineWidth_INDEX 136
#define glLineWidth_RETURN void
#define glLineWidth_ARG_NAMES width
#define glLineWidth_ARG_EXPAND GLfloat width
#define glLineWidth_PACKED PACKED_void_GLfloat
#define glLineWidth_INDEXED INDEXED_void_GLfloat
#define glLineWidth_FORMAT FORMAT_void_GLfloat
#define glLineWidthx_INDEX 137
#define glLineWidthx_RETURN void
#define glLineWidthx_ARG_NAMES width
#define glLineWidthx_ARG_EXPAND GLfixed width
#define glLineWidthx_PACKED PACKED_void_GLfixed
#define glLineWidthx_INDEXED INDEXED_void_GLfixed
#define glLineWidthx_FORMAT FORMAT_void_GLfixed
#define glLinkProgram_INDEX 138
#define glLinkProgram_RETURN void
#define glLinkProgram_ARG_NAMES program
#define glLinkProgram_ARG_EXPAND GLuint program
#define glLinkProgram_PACKED PACKED_void_GLuint
#define glLinkProgram_INDEXED INDEXED_void_GLuint
#define glLinkProgram_FORMAT FORMAT_void_GLuint
#define glLoadIdentity_INDEX 139
#define glLoadIdentity_RETURN void
#define glLoadIdentity_ARG_NAMES 
#define glLoadIdentity_ARG_EXPAND void
#define glLoadIdentity_PACKED PACKED_void
#define glLoadIdentity_INDEXED INDEXED_void
#define glLoadIdentity_FORMAT FORMAT_void
#define glLoadMatrixf_INDEX 140
#define glLoadMatrixf_RETURN void
#define glLoadMatrixf_ARG_NAMES m
#define glLoadMatrixf_ARG_EXPAND const GLfloat * m
#define glLoadMatrixf_PACKED PACKED_void_const_GLfloat___GENPT__
#define glLoadMatrixf_INDEXED INDEXED_void_const_GLfloat___GENPT__
#define glLoadMatrixf_FORMAT FORMAT_void_const_GLfloat___GENPT__
#define glLoadMatrixx_INDEX 141
#define glLoadMatrixx_RETURN void
#define glLoadMatrixx_ARG_NAMES m
#define glLoadMatrixx_ARG_EXPAND const GLfixed * m
#define glLoadMatrixx_PACKED PACKED_void_const_GLfixed___GENPT__
#define glLoadMatrixx_INDEXED INDEXED_void_const_GLfixed___GENPT__
#define glLoadMatrixx_FORMAT FORMAT_void_const_GLfixed___GENPT__
#define glLogicOp_INDEX 142
#define glLogicOp_RETURN void
#define glLogicOp_ARG_NAMES opcode
#define glLogicOp_ARG_EXPAND GLenum opcode
#define glLogicOp_PACKED PACKED_void_GLenum
#define glLogicOp_INDEXED INDEXED_void_GLenum
#define glLogicOp_FORMAT FORMAT_void_GLenum
#define glMaterialf_INDEX 143
#define glMaterialf_RETURN void
#define glMaterialf_ARG_NAMES face, pname, param
#define glMaterialf_ARG_EXPAND GLenum face, GLenum pname, GLfloat param
#define glMaterialf_PACKED PACKED_void_GLenum_GLenum_GLfloat
#define glMaterialf_INDEXED INDEXED_void_GLenum_GLenum_GLfloat
#define glMaterialf_FORMAT FORMAT_void_GLenum_GLenum_GLfloat
#define glMaterialfv_INDEX 144
#define glMaterialfv_RETURN void
#define glMaterialfv_ARG_NAMES face, pname, params
#define glMaterialfv_ARG_EXPAND GLenum face, GLenum pname, const GLfloat * params
#define glMaterialfv_PACKED PACKED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glMaterialfv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glMaterialfv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glMaterialx_INDEX 145
#define glMaterialx_RETURN void
#define glMaterialx_ARG_NAMES face, pname, param
#define glMaterialx_ARG_EXPAND GLenum face, GLenum pname, GLfixed param
#define glMaterialx_PACKED PACKED_void_GLenum_GLenum_GLfixed
#define glMaterialx_INDEXED INDEXED_void_GLenum_GLenum_GLfixed
#define glMaterialx_FORMAT FORMAT_void_GLenum_GLenum_GLfixed
#define glMaterialxv_INDEX 146
#define glMaterialxv_RETURN void
#define glMaterialxv_ARG_NAMES face, pname, params
#define glMaterialxv_ARG_EXPAND GLenum face, GLenum pname, const GLfixed * params
#define glMaterialxv_PACKED PACKED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glMaterialxv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glMaterialxv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glMatrixMode_INDEX 147
#define glMatrixMode_RETURN void
#define glMatrixMode_ARG_NAMES mode
#define glMatrixMode_ARG_EXPAND GLenum mode
#define glMatrixMode_PACKED PACKED_void_GLenum
#define glMatrixMode_INDEXED INDEXED_void_GLenum
#define glMatrixMode_FORMAT FORMAT_void_GLenum
#define glMultMatrixf_INDEX 148
#define glMultMatrixf_RETURN void
#define glMultMatrixf_ARG_NAMES m
#define glMultMatrixf_ARG_EXPAND const GLfloat * m
#define glMultMatrixf_PACKED PACKED_void_const_GLfloat___GENPT__
#define glMultMatrixf_INDEXED INDEXED_void_const_GLfloat___GENPT__
#define glMultMatrixf_FORMAT FORMAT_void_const_GLfloat___GENPT__
#define glMultMatrixx_INDEX 149
#define glMultMatrixx_RETURN void
#define glMultMatrixx_ARG_NAMES m
#define glMultMatrixx_ARG_EXPAND const GLfixed * m
#define glMultMatrixx_PACKED PACKED_void_const_GLfixed___GENPT__
#define glMultMatrixx_INDEXED INDEXED_void_const_GLfixed___GENPT__
#define glMultMatrixx_FORMAT FORMAT_void_const_GLfixed___GENPT__
#define glMultiDrawArrays_INDEX 150
#define glMultiDrawArrays_RETURN void
#define glMultiDrawArrays_ARG_NAMES mode, first, count, primcount
#define glMultiDrawArrays_ARG_EXPAND GLenum mode, const GLint * first, const GLsizei * count, GLsizei primcount
#define glMultiDrawArrays_PACKED PACKED_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei
#define glMultiDrawArrays_INDEXED INDEXED_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei
#define glMultiDrawArrays_FORMAT FORMAT_void_GLenum_const_GLint___GENPT___const_GLsizei___GENPT___GLsizei
#define glMultiDrawElements_INDEX 151
#define glMultiDrawElements_RETURN void
#define glMultiDrawElements_ARG_NAMES mode, count, type, indices, primcount
#define glMultiDrawElements_ARG_EXPAND GLenum mode, GLsizei * count, GLenum type, const void * const * indices, GLsizei primcount
#define glMultiDrawElements_PACKED PACKED_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei
#define glMultiDrawElements_INDEXED INDEXED_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei
#define glMultiDrawElements_FORMAT FORMAT_void_GLenum_GLsizei___GENPT___GLenum_const_void___GENPT___const___GENPT___GLsizei
#define glMultiTexCoord4f_INDEX 152
#define glMultiTexCoord4f_RETURN void
#define glMultiTexCoord4f_ARG_NAMES target, s, t, r, q
#define glMultiTexCoord4f_ARG_EXPAND GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q
#define glMultiTexCoord4f_PACKED PACKED_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat
#define glMultiTexCoord4f_INDEXED INDEXED_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat
#define glMultiTexCoord4f_FORMAT FORMAT_void_GLenum_GLfloat_GLfloat_GLfloat_GLfloat
#define glMultiTexCoord4x_INDEX 153
#define glMultiTexCoord4x_RETURN void
#define glMultiTexCoord4x_ARG_NAMES target, s, t, r, q
#define glMultiTexCoord4x_ARG_EXPAND GLenum target, GLfixed s, GLfixed t, GLfixed r, GLfixed q
#define glMultiTexCoord4x_PACKED PACKED_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed
#define glMultiTexCoord4x_INDEXED INDEXED_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed
#define glMultiTexCoord4x_FORMAT FORMAT_void_GLenum_GLfixed_GLfixed_GLfixed_GLfixed
#define glNormal3f_INDEX 154
#define glNormal3f_RETURN void
#define glNormal3f_ARG_NAMES nx, ny, nz
#define glNormal3f_ARG_EXPAND GLfloat nx, GLfloat ny, GLfloat nz
#define glNormal3f_PACKED PACKED_void_GLfloat_GLfloat_GLfloat
#define glNormal3f_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat
#define glNormal3f_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat
#define glNormal3x_INDEX 155
#define glNormal3x_RETURN void
#define glNormal3x_ARG_NAMES nx, ny, nz
#define glNormal3x_ARG_EXPAND GLfixed nx, GLfixed ny, GLfixed nz
#define glNormal3x_PACKED PACKED_void_GLfixed_GLfixed_GLfixed
#define glNormal3x_INDEXED INDEXED_void_GLfixed_GLfixed_GLfixed
#define glNormal3x_FORMAT FORMAT_void_GLfixed_GLfixed_GLfixed
#define glNormalPointer_INDEX 156
#define glNormalPointer_RETURN void
#define glNormalPointer_ARG_NAMES type, stride, pointer
#define glNormalPointer_ARG_EXPAND GLenum type, GLsizei stride, const GLvoid * pointer
#define glNormalPointer_PACKED PACKED_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glNormalPointer_INDEXED INDEXED_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glNormalPointer_FORMAT FORMAT_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glOrthof_INDEX 157
#define glOrthof_RETURN void
#define glOrthof_ARG_NAMES left, right, bottom, top, Near, Far
#define glOrthof_ARG_EXPAND GLfloat left, GLfloat right, GLfloat bottom, GLfloat top, GLfloat Near, GLfloat Far
#define glOrthof_PACKED PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glOrthof_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glOrthof_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat_GLfloat
#define glOrthox_INDEX 158
#define glOrthox_RETURN void
#define glOrthox_ARG_NAMES left, right, bottom, top, Near, Far
#define glOrthox_ARG_EXPAND GLfixed left, GLfixed right, GLfixed bottom, GLfixed top, GLfixed Near, GLfixed Far
#define glOrthox_PACKED PACKED_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed
#define glOrthox_INDEXED INDEXED_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed
#define glOrthox_FORMAT FORMAT_void_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed_GLfixed
#define glPixelStorei_INDEX 159
#define glPixelStorei_RETURN void
#define glPixelStorei_ARG_NAMES pname, param
#define glPixelStorei_ARG_EXPAND GLenum pname, GLint param
#define glPixelStorei_PACKED PACKED_void_GLenum_GLint
#define glPixelStorei_INDEXED INDEXED_void_GLenum_GLint
#define glPixelStorei_FORMAT FORMAT_void_GLenum_GLint
#define glPointParameterf_INDEX 160
#define glPointParameterf_RETURN void
#define glPointParameterf_ARG_NAMES pname, param
#define glPointParameterf_ARG_EXPAND GLenum pname, GLfloat param
#define glPointParameterf_PACKED PACKED_void_GLenum_GLfloat
#define glPointParameterf_INDEXED INDEXED_void_GLenum_GLfloat
#define glPointParameterf_FORMAT FORMAT_void_GLenum_GLfloat
#define glPointParameterfv_INDEX 161
#define glPointParameterfv_RETURN void
#define glPointParameterfv_ARG_NAMES pname, params
#define glPointParameterfv_ARG_EXPAND GLenum pname, const GLfloat * params
#define glPointParameterfv_PACKED PACKED_void_GLenum_const_GLfloat___GENPT__
#define glPointParameterfv_INDEXED INDEXED_void_GLenum_const_GLfloat___GENPT__
#define glPointParameterfv_FORMAT FORMAT_void_GLenum_const_GLfloat___GENPT__
#define glPointParameterx_INDEX 162
#define glPointParameterx_RETURN void
#define glPointParameterx_ARG_NAMES pname, param
#define glPointParameterx_ARG_EXPAND GLenum pname, GLfixed param
#define glPointParameterx_PACKED PACKED_void_GLenum_GLfixed
#define glPointParameterx_INDEXED INDEXED_void_GLenum_GLfixed
#define glPointParameterx_FORMAT FORMAT_void_GLenum_GLfixed
#define glPointParameterxv_INDEX 163
#define glPointParameterxv_RETURN void
#define glPointParameterxv_ARG_NAMES pname, params
#define glPointParameterxv_ARG_EXPAND GLenum pname, const GLfixed * params
#define glPointParameterxv_PACKED PACKED_void_GLenum_const_GLfixed___GENPT__
#define glPointParameterxv_INDEXED INDEXED_void_GLenum_const_GLfixed___GENPT__
#define glPointParameterxv_FORMAT FORMAT_void_GLenum_const_GLfixed___GENPT__
#define glPointSize_INDEX 164
#define glPointSize_RETURN void
#define glPointSize_ARG_NAMES size
#define glPointSize_ARG_EXPAND GLfloat size
#define glPointSize_PACKED PACKED_void_GLfloat
#define glPointSize_INDEXED INDEXED_void_GLfloat
#define glPointSize_FORMAT FORMAT_void_GLfloat
#define glPointSizePointerOES_INDEX 165
#define glPointSizePointerOES_RETURN void
#define glPointSizePointerOES_ARG_NAMES type, stride, pointer
#define glPointSizePointerOES_ARG_EXPAND GLenum type, GLsizei stride, const GLvoid * pointer
#define glPointSizePointerOES_PACKED PACKED_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glPointSizePointerOES_INDEXED INDEXED_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glPointSizePointerOES_FORMAT FORMAT_void_GLenum_GLsizei_const_GLvoid___GENPT__
#define glPointSizex_INDEX 166
#define glPointSizex_RETURN void
#define glPointSizex_ARG_NAMES size
#define glPointSizex_ARG_EXPAND GLfixed size
#define glPointSizex_PACKED PACKED_void_GLfixed
#define glPointSizex_INDEXED INDEXED_void_GLfixed
#define glPointSizex_FORMAT FORMAT_void_GLfixed
#define glPolygonOffset_INDEX 167
#define glPolygonOffset_RETURN void
#define glPolygonOffset_ARG_NAMES factor, units
#define glPolygonOffset_ARG_EXPAND GLfloat factor, GLfloat units
#define glPolygonOffset_PACKED PACKED_void_GLfloat_GLfloat
#define glPolygonOffset_INDEXED INDEXED_void_GLfloat_GLfloat
#define glPolygonOffset_FORMAT FORMAT_void_GLfloat_GLfloat
#define glPolygonOffsetx_INDEX 168
#define glPolygonOffsetx_RETURN void
#define glPolygonOffsetx_ARG_NAMES factor, units
#define glPolygonOffsetx_ARG_EXPAND GLfixed factor, GLfixed units
#define glPolygonOffsetx_PACKED PACKED_void_GLfixed_GLfixed
#define glPolygonOffsetx_INDEXED INDEXED_void_GLfixed_GLfixed
#define glPolygonOffsetx_FORMAT FORMAT_void_GLfixed_GLfixed
#define glPopMatrix_INDEX 169
#define glPopMatrix_RETURN void
#define glPopMatrix_ARG_NAMES 
#define glPopMatrix_ARG_EXPAND void
#define glPopMatrix_PACKED PACKED_void
#define glPopMatrix_INDEXED INDEXED_void
#define glPopMatrix_FORMAT FORMAT_void
#define glProgramBinary_INDEX 170
#define glProgramBinary_RETURN void
#define glProgramBinary_ARG_NAMES program, binaryFormat, binary, length
#define glProgramBinary_ARG_EXPAND GLuint program, GLenum binaryFormat, const GLvoid * binary, GLint length
#define glProgramBinary_PACKED PACKED_void_GLuint_GLenum_const_GLvoid___GENPT___GLint
#define glProgramBinary_INDEXED INDEXED_void_GLuint_GLenum_const_GLvoid___GENPT___GLint
#define glProgramBinary_FORMAT FORMAT_void_GLuint_GLenum_const_GLvoid___GENPT___GLint
#define glPushMatrix_INDEX 171
#define glPushMatrix_RETURN void
#define glPushMatrix_ARG_NAMES 
#define glPushMatrix_ARG_EXPAND void
#define glPushMatrix_PACKED PACKED_void
#define glPushMatrix_INDEXED INDEXED_void
#define glPushMatrix_FORMAT FORMAT_void
#define glReadPixels_INDEX 172
#define glReadPixels_RETURN void
#define glReadPixels_ARG_NAMES x, y, width, height, format, type, pixels
#define glReadPixels_ARG_EXPAND GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, GLvoid * pixels
#define glReadPixels_PACKED PACKED_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__
#define glReadPixels_INDEXED INDEXED_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__
#define glReadPixels_FORMAT FORMAT_void_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_GLvoid___GENPT__
#define glReleaseShaderCompiler_INDEX 173
#define glReleaseShaderCompiler_RETURN void
#define glReleaseShaderCompiler_ARG_NAMES 
#define glReleaseShaderCompiler_ARG_EXPAND void
#define glReleaseShaderCompiler_PACKED PACKED_void
#define glReleaseShaderCompiler_INDEXED INDEXED_void
#define glReleaseShaderCompiler_FORMAT FORMAT_void
#define glRenderbufferStorage_INDEX 174
#define glRenderbufferStorage_RETURN void
#define glRenderbufferStorage_ARG_NAMES target, internalformat, width, height
#define glRenderbufferStorage_ARG_EXPAND GLenum target, GLenum internalformat, GLsizei width, GLsizei height
#define glRenderbufferStorage_PACKED PACKED_void_GLenum_GLenum_GLsizei_GLsizei
#define glRenderbufferStorage_INDEXED INDEXED_void_GLenum_GLenum_GLsizei_GLsizei
#define glRenderbufferStorage_FORMAT FORMAT_void_GLenum_GLenum_GLsizei_GLsizei
#define glRotatef_INDEX 175
#define glRotatef_RETURN void
#define glRotatef_ARG_NAMES angle, x, y, z
#define glRotatef_ARG_EXPAND GLfloat angle, GLfloat x, GLfloat y, GLfloat z
#define glRotatef_PACKED PACKED_void_GLfloat_GLfloat_GLfloat_GLfloat
#define glRotatef_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat_GLfloat
#define glRotatef_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat_GLfloat
#define glRotatex_INDEX 176
#define glRotatex_RETURN void
#define glRotatex_ARG_NAMES angle, x, y, z
#define glRotatex_ARG_EXPAND GLfixed angle, GLfixed x, GLfixed y, GLfixed z
#define glRotatex_PACKED PACKED_void_GLfixed_GLfixed_GLfixed_GLfixed
#define glRotatex_INDEXED INDEXED_void_GLfixed_GLfixed_GLfixed_GLfixed
#define glRotatex_FORMAT FORMAT_void_GLfixed_GLfixed_GLfixed_GLfixed
#define glSampleCoverage_INDEX 177
#define glSampleCoverage_RETURN void
#define glSampleCoverage_ARG_NAMES value, invert
#define glSampleCoverage_ARG_EXPAND GLclampf value, GLboolean invert
#define glSampleCoverage_PACKED PACKED_void_GLclampf_GLboolean
#define glSampleCoverage_INDEXED INDEXED_void_GLclampf_GLboolean
#define glSampleCoverage_FORMAT FORMAT_void_GLclampf_GLboolean
#define glSampleCoveragex_INDEX 178
#define glSampleCoveragex_RETURN void
#define glSampleCoveragex_ARG_NAMES value, invert
#define glSampleCoveragex_ARG_EXPAND GLclampx value, GLboolean invert
#define glSampleCoveragex_PACKED PACKED_void_GLclampx_GLboolean
#define glSampleCoveragex_INDEXED INDEXED_void_GLclampx_GLboolean
#define glSampleCoveragex_FORMAT FORMAT_void_GLclampx_GLboolean
#define glScalef_INDEX 179
#define glScalef_RETURN void
#define glScalef_ARG_NAMES x, y, z
#define glScalef_ARG_EXPAND GLfloat x, GLfloat y, GLfloat z
#define glScalef_PACKED PACKED_void_GLfloat_GLfloat_GLfloat
#define glScalef_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat
#define glScalef_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat
#define glScalex_INDEX 180
#define glScalex_RETURN void
#define glScalex_ARG_NAMES x, y, z
#define glScalex_ARG_EXPAND GLfixed x, GLfixed y, GLfixed z
#define glScalex_PACKED PACKED_void_GLfixed_GLfixed_GLfixed
#define glScalex_INDEXED INDEXED_void_GLfixed_GLfixed_GLfixed
#define glScalex_FORMAT FORMAT_void_GLfixed_GLfixed_GLfixed
#define glScissor_INDEX 181
#define glScissor_RETURN void
#define glScissor_ARG_NAMES x, y, width, height
#define glScissor_ARG_EXPAND GLint x, GLint y, GLsizei width, GLsizei height
#define glScissor_PACKED PACKED_void_GLint_GLint_GLsizei_GLsizei
#define glScissor_INDEXED INDEXED_void_GLint_GLint_GLsizei_GLsizei
#define glScissor_FORMAT FORMAT_void_GLint_GLint_GLsizei_GLsizei
#define glShadeModel_INDEX 182
#define glShadeModel_RETURN void
#define glShadeModel_ARG_NAMES mode
#define glShadeModel_ARG_EXPAND GLenum mode
#define glShadeModel_PACKED PACKED_void_GLenum
#define glShadeModel_INDEXED INDEXED_void_GLenum
#define glShadeModel_FORMAT FORMAT_void_GLenum
#define glShaderBinary_INDEX 183
#define glShaderBinary_RETURN void
#define glShaderBinary_ARG_NAMES n, shaders, binaryformat, binary, length
#define glShaderBinary_ARG_EXPAND GLsizei n, const GLuint * shaders, GLenum binaryformat, const GLvoid * binary, GLsizei length
#define glShaderBinary_PACKED PACKED_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei
#define glShaderBinary_INDEXED INDEXED_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei
#define glShaderBinary_FORMAT FORMAT_void_GLsizei_const_GLuint___GENPT___GLenum_const_GLvoid___GENPT___GLsizei
#define glShaderSource_INDEX 184
#define glShaderSource_RETURN void
#define glShaderSource_ARG_NAMES shader, count, string, length
#define glShaderSource_ARG_EXPAND GLuint shader, GLsizei count, const GLchar * const * string, const GLint * length
#define glShaderSource_PACKED PACKED_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__
#define glShaderSource_INDEXED INDEXED_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__
#define glShaderSource_FORMAT FORMAT_void_GLuint_GLsizei_const_GLchar___GENPT___const___GENPT___const_GLint___GENPT__
#define glStencilFunc_INDEX 185
#define glStencilFunc_RETURN void
#define glStencilFunc_ARG_NAMES func, ref, mask
#define glStencilFunc_ARG_EXPAND GLenum func, GLint ref, GLuint mask
#define glStencilFunc_PACKED PACKED_void_GLenum_GLint_GLuint
#define glStencilFunc_INDEXED INDEXED_void_GLenum_GLint_GLuint
#define glStencilFunc_FORMAT FORMAT_void_GLenum_GLint_GLuint
#define glStencilFuncSeparate_INDEX 186
#define glStencilFuncSeparate_RETURN void
#define glStencilFuncSeparate_ARG_NAMES face, func, ref, mask
#define glStencilFuncSeparate_ARG_EXPAND GLenum face, GLenum func, GLint ref, GLuint mask
#define glStencilFuncSeparate_PACKED PACKED_void_GLenum_GLenum_GLint_GLuint
#define glStencilFuncSeparate_INDEXED INDEXED_void_GLenum_GLenum_GLint_GLuint
#define glStencilFuncSeparate_FORMAT FORMAT_void_GLenum_GLenum_GLint_GLuint
#define glStencilMask_INDEX 187
#define glStencilMask_RETURN void
#define glStencilMask_ARG_NAMES mask
#define glStencilMask_ARG_EXPAND GLuint mask
#define glStencilMask_PACKED PACKED_void_GLuint
#define glStencilMask_INDEXED INDEXED_void_GLuint
#define glStencilMask_FORMAT FORMAT_void_GLuint
#define glStencilMaskSeparate_INDEX 188
#define glStencilMaskSeparate_RETURN void
#define glStencilMaskSeparate_ARG_NAMES face, mask
#define glStencilMaskSeparate_ARG_EXPAND GLenum face, GLuint mask
#define glStencilMaskSeparate_PACKED PACKED_void_GLenum_GLuint
#define glStencilMaskSeparate_INDEXED INDEXED_void_GLenum_GLuint
#define glStencilMaskSeparate_FORMAT FORMAT_void_GLenum_GLuint
#define glStencilOp_INDEX 189
#define glStencilOp_RETURN void
#define glStencilOp_ARG_NAMES fail, zfail, zpass
#define glStencilOp_ARG_EXPAND GLenum fail, GLenum zfail, GLenum zpass
#define glStencilOp_PACKED PACKED_void_GLenum_GLenum_GLenum
#define glStencilOp_INDEXED INDEXED_void_GLenum_GLenum_GLenum
#define glStencilOp_FORMAT FORMAT_void_GLenum_GLenum_GLenum
#define glStencilOpSeparate_INDEX 190
#define glStencilOpSeparate_RETURN void
#define glStencilOpSeparate_ARG_NAMES face, sfail, zfail, zpass
#define glStencilOpSeparate_ARG_EXPAND GLenum face, GLenum sfail, GLenum zfail, GLenum zpass
#define glStencilOpSeparate_PACKED PACKED_void_GLenum_GLenum_GLenum_GLenum
#define glStencilOpSeparate_INDEXED INDEXED_void_GLenum_GLenum_GLenum_GLenum
#define glStencilOpSeparate_FORMAT FORMAT_void_GLenum_GLenum_GLenum_GLenum
#define glTexCoordPointer_INDEX 191
#define glTexCoordPointer_RETURN void
#define glTexCoordPointer_ARG_NAMES size, type, stride, pointer
#define glTexCoordPointer_ARG_EXPAND GLint size, GLenum type, GLsizei stride, const GLvoid * pointer
#define glTexCoordPointer_PACKED PACKED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glTexCoordPointer_INDEXED INDEXED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glTexCoordPointer_FORMAT FORMAT_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glTexEnvf_INDEX 192
#define glTexEnvf_RETURN void
#define glTexEnvf_ARG_NAMES target, pname, param
#define glTexEnvf_ARG_EXPAND GLenum target, GLenum pname, GLfloat param
#define glTexEnvf_PACKED PACKED_void_GLenum_GLenum_GLfloat
#define glTexEnvf_INDEXED INDEXED_void_GLenum_GLenum_GLfloat
#define glTexEnvf_FORMAT FORMAT_void_GLenum_GLenum_GLfloat
#define glTexEnvfv_INDEX 193
#define glTexEnvfv_RETURN void
#define glTexEnvfv_ARG_NAMES target, pname, params
#define glTexEnvfv_ARG_EXPAND GLenum target, GLenum pname, const GLfloat * params
#define glTexEnvfv_PACKED PACKED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexEnvfv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexEnvfv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexEnvi_INDEX 194
#define glTexEnvi_RETURN void
#define glTexEnvi_ARG_NAMES target, pname, param
#define glTexEnvi_ARG_EXPAND GLenum target, GLenum pname, GLint param
#define glTexEnvi_PACKED PACKED_void_GLenum_GLenum_GLint
#define glTexEnvi_INDEXED INDEXED_void_GLenum_GLenum_GLint
#define glTexEnvi_FORMAT FORMAT_void_GLenum_GLenum_GLint
#define glTexEnviv_INDEX 195
#define glTexEnviv_RETURN void
#define glTexEnviv_ARG_NAMES target, pname, params
#define glTexEnviv_ARG_EXPAND GLenum target, GLenum pname, const GLint * params
#define glTexEnviv_PACKED PACKED_void_GLenum_GLenum_const_GLint___GENPT__
#define glTexEnviv_INDEXED INDEXED_void_GLenum_GLenum_const_GLint___GENPT__
#define glTexEnviv_FORMAT FORMAT_void_GLenum_GLenum_const_GLint___GENPT__
#define glTexEnvx_INDEX 196
#define glTexEnvx_RETURN void
#define glTexEnvx_ARG_NAMES target, pname, param
#define glTexEnvx_ARG_EXPAND GLenum target, GLenum pname, GLfixed param
#define glTexEnvx_PACKED PACKED_void_GLenum_GLenum_GLfixed
#define glTexEnvx_INDEXED INDEXED_void_GLenum_GLenum_GLfixed
#define glTexEnvx_FORMAT FORMAT_void_GLenum_GLenum_GLfixed
#define glTexEnvxv_INDEX 197
#define glTexEnvxv_RETURN void
#define glTexEnvxv_ARG_NAMES target, pname, params
#define glTexEnvxv_ARG_EXPAND GLenum target, GLenum pname, const GLfixed * params
#define glTexEnvxv_PACKED PACKED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glTexEnvxv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glTexEnvxv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glTexGenfv_INDEX 198
#define glTexGenfv_RETURN void
#define glTexGenfv_ARG_NAMES coord, pname, params
#define glTexGenfv_ARG_EXPAND GLenum coord, GLenum pname, const GLfloat * params
#define glTexGenfv_PACKED PACKED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexGenfv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexGenfv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexGeni_INDEX 199
#define glTexGeni_RETURN void
#define glTexGeni_ARG_NAMES coord, pname, param
#define glTexGeni_ARG_EXPAND GLenum coord, GLenum pname, GLint param
#define glTexGeni_PACKED PACKED_void_GLenum_GLenum_GLint
#define glTexGeni_INDEXED INDEXED_void_GLenum_GLenum_GLint
#define glTexGeni_FORMAT FORMAT_void_GLenum_GLenum_GLint
#define glTexImage2D_INDEX 200
#define glTexImage2D_RETURN void
#define glTexImage2D_ARG_NAMES target, level, internalformat, width, height, border, format, type, data
#define glTexImage2D_ARG_EXPAND GLenum target, GLint level, GLint internalformat, GLsizei width, GLsizei height, GLint border, GLenum format, GLenum type, const GLvoid * data
#define glTexImage2D_PACKED PACKED_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__
#define glTexImage2D_INDEXED INDEXED_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__
#define glTexImage2D_FORMAT FORMAT_void_GLenum_GLint_GLint_GLsizei_GLsizei_GLint_GLenum_GLenum_const_GLvoid___GENPT__
#define glTexParameterf_INDEX 201
#define glTexParameterf_RETURN void
#define glTexParameterf_ARG_NAMES target, pname, param
#define glTexParameterf_ARG_EXPAND GLenum target, GLenum pname, GLfloat param
#define glTexParameterf_PACKED PACKED_void_GLenum_GLenum_GLfloat
#define glTexParameterf_INDEXED INDEXED_void_GLenum_GLenum_GLfloat
#define glTexParameterf_FORMAT FORMAT_void_GLenum_GLenum_GLfloat
#define glTexParameterfv_INDEX 202
#define glTexParameterfv_RETURN void
#define glTexParameterfv_ARG_NAMES target, pname, params
#define glTexParameterfv_ARG_EXPAND GLenum target, GLenum pname, const GLfloat * params
#define glTexParameterfv_PACKED PACKED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexParameterfv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexParameterfv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfloat___GENPT__
#define glTexParameteri_INDEX 203
#define glTexParameteri_RETURN void
#define glTexParameteri_ARG_NAMES target, pname, param
#define glTexParameteri_ARG_EXPAND GLenum target, GLenum pname, GLint param
#define glTexParameteri_PACKED PACKED_void_GLenum_GLenum_GLint
#define glTexParameteri_INDEXED INDEXED_void_GLenum_GLenum_GLint
#define glTexParameteri_FORMAT FORMAT_void_GLenum_GLenum_GLint
#define glTexParameteriv_INDEX 204
#define glTexParameteriv_RETURN void
#define glTexParameteriv_ARG_NAMES target, pname, params
#define glTexParameteriv_ARG_EXPAND GLenum target, GLenum pname, const GLint * params
#define glTexParameteriv_PACKED PACKED_void_GLenum_GLenum_const_GLint___GENPT__
#define glTexParameteriv_INDEXED INDEXED_void_GLenum_GLenum_const_GLint___GENPT__
#define glTexParameteriv_FORMAT FORMAT_void_GLenum_GLenum_const_GLint___GENPT__
#define glTexParameterx_INDEX 205
#define glTexParameterx_RETURN void
#define glTexParameterx_ARG_NAMES target, pname, param
#define glTexParameterx_ARG_EXPAND GLenum target, GLenum pname, GLfixed param
#define glTexParameterx_PACKED PACKED_void_GLenum_GLenum_GLfixed
#define glTexParameterx_INDEXED INDEXED_void_GLenum_GLenum_GLfixed
#define glTexParameterx_FORMAT FORMAT_void_GLenum_GLenum_GLfixed
#define glTexParameterxv_INDEX 206
#define glTexParameterxv_RETURN void
#define glTexParameterxv_ARG_NAMES target, pname, params
#define glTexParameterxv_ARG_EXPAND GLenum target, GLenum pname, const GLfixed * params
#define glTexParameterxv_PACKED PACKED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glTexParameterxv_INDEXED INDEXED_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glTexParameterxv_FORMAT FORMAT_void_GLenum_GLenum_const_GLfixed___GENPT__
#define glTexSubImage2D_INDEX 207
#define glTexSubImage2D_RETURN void
#define glTexSubImage2D_ARG_NAMES target, level, xoffset, yoffset, width, height, format, type, data
#define glTexSubImage2D_ARG_EXPAND GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLenum type, const GLvoid * data
#define glTexSubImage2D_PACKED PACKED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__
#define glTexSubImage2D_INDEXED INDEXED_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__
#define glTexSubImage2D_FORMAT FORMAT_void_GLenum_GLint_GLint_GLint_GLsizei_GLsizei_GLenum_GLenum_const_GLvoid___GENPT__
#define glTranslatef_INDEX 208
#define glTranslatef_RETURN void
#define glTranslatef_ARG_NAMES x, y, z
#define glTranslatef_ARG_EXPAND GLfloat x, GLfloat y, GLfloat z
#define glTranslatef_PACKED PACKED_void_GLfloat_GLfloat_GLfloat
#define glTranslatef_INDEXED INDEXED_void_GLfloat_GLfloat_GLfloat
#define glTranslatef_FORMAT FORMAT_void_GLfloat_GLfloat_GLfloat
#define glTranslatex_INDEX 209
#define glTranslatex_RETURN void
#define glTranslatex_ARG_NAMES x, y, z
#define glTranslatex_ARG_EXPAND GLfixed x, GLfixed y, GLfixed z
#define glTranslatex_PACKED PACKED_void_GLfixed_GLfixed_GLfixed
#define glTranslatex_INDEXED INDEXED_void_GLfixed_GLfixed_GLfixed
#define glTranslatex_FORMAT FORMAT_void_GLfixed_GLfixed_GLfixed
#define glUniform1f_INDEX 210
#define glUniform1f_RETURN void
#define glUniform1f_ARG_NAMES location, v0
#define glUniform1f_ARG_EXPAND GLint location, GLfloat v0
#define glUniform1f_PACKED PACKED_void_GLint_GLfloat
#define glUniform1f_INDEXED INDEXED_void_GLint_GLfloat
#define glUniform1f_FORMAT FORMAT_void_GLint_GLfloat
#define glUniform1fv_INDEX 211
#define glUniform1fv_RETURN void
#define glUniform1fv_ARG_NAMES location, count, value
#define glUniform1fv_ARG_EXPAND GLint location, GLsizei count, const GLfloat * value
#define glUniform1fv_PACKED PACKED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform1fv_INDEXED INDEXED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform1fv_FORMAT FORMAT_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform1i_INDEX 212
#define glUniform1i_RETURN void
#define glUniform1i_ARG_NAMES location, v0
#define glUniform1i_ARG_EXPAND GLint location, GLint v0
#define glUniform1i_PACKED PACKED_void_GLint_GLint
#define glUniform1i_INDEXED INDEXED_void_GLint_GLint
#define glUniform1i_FORMAT FORMAT_void_GLint_GLint
#define glUniform1iv_INDEX 213
#define glUniform1iv_RETURN void
#define glUniform1iv_ARG_NAMES location, count, value
#define glUniform1iv_ARG_EXPAND GLint location, GLsizei count, const GLint * value
#define glUniform1iv_PACKED PACKED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform1iv_INDEXED INDEXED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform1iv_FORMAT FORMAT_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform2f_INDEX 214
#define glUniform2f_RETURN void
#define glUniform2f_ARG_NAMES location, v0, v1
#define glUniform2f_ARG_EXPAND GLint location, GLfloat v0, GLfloat v1
#define glUniform2f_PACKED PACKED_void_GLint_GLfloat_GLfloat
#define glUniform2f_INDEXED INDEXED_void_GLint_GLfloat_GLfloat
#define glUniform2f_FORMAT FORMAT_void_GLint_GLfloat_GLfloat
#define glUniform2fv_INDEX 215
#define glUniform2fv_RETURN void
#define glUniform2fv_ARG_NAMES location, count, value
#define glUniform2fv_ARG_EXPAND GLint location, GLsizei count, const GLfloat * value
#define glUniform2fv_PACKED PACKED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform2fv_INDEXED INDEXED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform2fv_FORMAT FORMAT_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform2i_INDEX 216
#define glUniform2i_RETURN void
#define glUniform2i_ARG_NAMES location, v0, v1
#define glUniform2i_ARG_EXPAND GLint location, GLint v0, GLint v1
#define glUniform2i_PACKED PACKED_void_GLint_GLint_GLint
#define glUniform2i_INDEXED INDEXED_void_GLint_GLint_GLint
#define glUniform2i_FORMAT FORMAT_void_GLint_GLint_GLint
#define glUniform2iv_INDEX 217
#define glUniform2iv_RETURN void
#define glUniform2iv_ARG_NAMES location, count, value
#define glUniform2iv_ARG_EXPAND GLint location, GLsizei count, const GLint * value
#define glUniform2iv_PACKED PACKED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform2iv_INDEXED INDEXED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform2iv_FORMAT FORMAT_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform3f_INDEX 218
#define glUniform3f_RETURN void
#define glUniform3f_ARG_NAMES location, v0, v1, v2
#define glUniform3f_ARG_EXPAND GLint location, GLfloat v0, GLfloat v1, GLfloat v2
#define glUniform3f_PACKED PACKED_void_GLint_GLfloat_GLfloat_GLfloat
#define glUniform3f_INDEXED INDEXED_void_GLint_GLfloat_GLfloat_GLfloat
#define glUniform3f_FORMAT FORMAT_void_GLint_GLfloat_GLfloat_GLfloat
#define glUniform3fv_INDEX 219
#define glUniform3fv_RETURN void
#define glUniform3fv_ARG_NAMES location, count, value
#define glUniform3fv_ARG_EXPAND GLint location, GLsizei count, const GLfloat * value
#define glUniform3fv_PACKED PACKED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform3fv_INDEXED INDEXED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform3fv_FORMAT FORMAT_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform3i_INDEX 220
#define glUniform3i_RETURN void
#define glUniform3i_ARG_NAMES location, v0, v1, v2
#define glUniform3i_ARG_EXPAND GLint location, GLint v0, GLint v1, GLint v2
#define glUniform3i_PACKED PACKED_void_GLint_GLint_GLint_GLint
#define glUniform3i_INDEXED INDEXED_void_GLint_GLint_GLint_GLint
#define glUniform3i_FORMAT FORMAT_void_GLint_GLint_GLint_GLint
#define glUniform3iv_INDEX 221
#define glUniform3iv_RETURN void
#define glUniform3iv_ARG_NAMES location, count, value
#define glUniform3iv_ARG_EXPAND GLint location, GLsizei count, const GLint * value
#define glUniform3iv_PACKED PACKED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform3iv_INDEXED INDEXED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform3iv_FORMAT FORMAT_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform4f_INDEX 222
#define glUniform4f_RETURN void
#define glUniform4f_ARG_NAMES location, v0, v1, v2, v3
#define glUniform4f_ARG_EXPAND GLint location, GLfloat v0, GLfloat v1, GLfloat v2, GLfloat v3
#define glUniform4f_PACKED PACKED_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat
#define glUniform4f_INDEXED INDEXED_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat
#define glUniform4f_FORMAT FORMAT_void_GLint_GLfloat_GLfloat_GLfloat_GLfloat
#define glUniform4fv_INDEX 223
#define glUniform4fv_RETURN void
#define glUniform4fv_ARG_NAMES location, count, value
#define glUniform4fv_ARG_EXPAND GLint location, GLsizei count, const GLfloat * value
#define glUniform4fv_PACKED PACKED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform4fv_INDEXED INDEXED_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform4fv_FORMAT FORMAT_void_GLint_GLsizei_const_GLfloat___GENPT__
#define glUniform4i_INDEX 224
#define glUniform4i_RETURN void
#define glUniform4i_ARG_NAMES location, v0, v1, v2, v3
#define glUniform4i_ARG_EXPAND GLint location, GLint v0, GLint v1, GLint v2, GLint v3
#define glUniform4i_PACKED PACKED_void_GLint_GLint_GLint_GLint_GLint
#define glUniform4i_INDEXED INDEXED_void_GLint_GLint_GLint_GLint_GLint
#define glUniform4i_FORMAT FORMAT_void_GLint_GLint_GLint_GLint_GLint
#define glUniform4iv_INDEX 225
#define glUniform4iv_RETURN void
#define glUniform4iv_ARG_NAMES location, count, value
#define glUniform4iv_ARG_EXPAND GLint location, GLsizei count, const GLint * value
#define glUniform4iv_PACKED PACKED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform4iv_INDEXED INDEXED_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniform4iv_FORMAT FORMAT_void_GLint_GLsizei_const_GLint___GENPT__
#define glUniformMatrix2fv_INDEX 226
#define glUniformMatrix2fv_RETURN void
#define glUniformMatrix2fv_ARG_NAMES location, count, transpose, value
#define glUniformMatrix2fv_ARG_EXPAND GLint location, GLsizei count, GLboolean transpose, const GLfloat * value
#define glUniformMatrix2fv_PACKED PACKED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix2fv_INDEXED INDEXED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix2fv_FORMAT FORMAT_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix3fv_INDEX 227
#define glUniformMatrix3fv_RETURN void
#define glUniformMatrix3fv_ARG_NAMES location, count, transpose, value
#define glUniformMatrix3fv_ARG_EXPAND GLint location, GLsizei count, GLboolean transpose, const GLfloat * value
#define glUniformMatrix3fv_PACKED PACKED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix3fv_INDEXED INDEXED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix3fv_FORMAT FORMAT_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix4fv_INDEX 228
#define glUniformMatrix4fv_RETURN void
#define glUniformMatrix4fv_ARG_NAMES location, count, transpose, value
#define glUniformMatrix4fv_ARG_EXPAND GLint location, GLsizei count, GLboolean transpose, const GLfloat * value
#define glUniformMatrix4fv_PACKED PACKED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix4fv_INDEXED INDEXED_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUniformMatrix4fv_FORMAT FORMAT_void_GLint_GLsizei_GLboolean_const_GLfloat___GENPT__
#define glUseProgram_INDEX 229
#define glUseProgram_RETURN void
#define glUseProgram_ARG_NAMES program
#define glUseProgram_ARG_EXPAND GLuint program
#define glUseProgram_PACKED PACKED_void_GLuint
#define glUseProgram_INDEXED INDEXED_void_GLuint
#define glUseProgram_FORMAT FORMAT_void_GLuint
#define glValidateProgram_INDEX 230
#define glValidateProgram_RETURN void
#define glValidateProgram_ARG_NAMES program
#define glValidateProgram_ARG_EXPAND GLuint program
#define glValidateProgram_PACKED PACKED_void_GLuint
#define glValidateProgram_INDEXED INDEXED_void_GLuint
#define glValidateProgram_FORMAT FORMAT_void_GLuint
#define glVertexAttrib1f_INDEX 231
#define glVertexAttrib1f_RETURN void
#define glVertexAttrib1f_ARG_NAMES index, x
#define glVertexAttrib1f_ARG_EXPAND GLuint index, GLfloat x
#define glVertexAttrib1f_PACKED PACKED_void_GLuint_GLfloat
#define glVertexAttrib1f_INDEXED INDEXED_void_GLuint_GLfloat
#define glVertexAttrib1f_FORMAT FORMAT_void_GLuint_GLfloat
#define glVertexAttrib1fv_INDEX 232
#define glVertexAttrib1fv_RETURN void
#define glVertexAttrib1fv_ARG_NAMES index, v
#define glVertexAttrib1fv_ARG_EXPAND GLuint index, const GLfloat * v
#define glVertexAttrib1fv_PACKED PACKED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib1fv_INDEXED INDEXED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib1fv_FORMAT FORMAT_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib2f_INDEX 233
#define glVertexAttrib2f_RETURN void
#define glVertexAttrib2f_ARG_NAMES index, x, y
#define glVertexAttrib2f_ARG_EXPAND GLuint index, GLfloat x, GLfloat y
#define glVertexAttrib2f_PACKED PACKED_void_GLuint_GLfloat_GLfloat
#define glVertexAttrib2f_INDEXED INDEXED_void_GLuint_GLfloat_GLfloat
#define glVertexAttrib2f_FORMAT FORMAT_void_GLuint_GLfloat_GLfloat
#define glVertexAttrib2fv_INDEX 234
#define glVertexAttrib2fv_RETURN void
#define glVertexAttrib2fv_ARG_NAMES index, v
#define glVertexAttrib2fv_ARG_EXPAND GLuint index, const GLfloat * v
#define glVertexAttrib2fv_PACKED PACKED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib2fv_INDEXED INDEXED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib2fv_FORMAT FORMAT_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib3f_INDEX 235
#define glVertexAttrib3f_RETURN void
#define glVertexAttrib3f_ARG_NAMES index, x, y, z
#define glVertexAttrib3f_ARG_EXPAND GLuint index, GLfloat x, GLfloat y, GLfloat z
#define glVertexAttrib3f_PACKED PACKED_void_GLuint_GLfloat_GLfloat_GLfloat
#define glVertexAttrib3f_INDEXED INDEXED_void_GLuint_GLfloat_GLfloat_GLfloat
#define glVertexAttrib3f_FORMAT FORMAT_void_GLuint_GLfloat_GLfloat_GLfloat
#define glVertexAttrib3fv_INDEX 236
#define glVertexAttrib3fv_RETURN void
#define glVertexAttrib3fv_ARG_NAMES index, v
#define glVertexAttrib3fv_ARG_EXPAND GLuint index, const GLfloat * v
#define glVertexAttrib3fv_PACKED PACKED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib3fv_INDEXED INDEXED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib3fv_FORMAT FORMAT_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib4f_INDEX 237
#define glVertexAttrib4f_RETURN void
#define glVertexAttrib4f_ARG_NAMES index, x, y, z, w
#define glVertexAttrib4f_ARG_EXPAND GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w
#define glVertexAttrib4f_PACKED PACKED_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat
#define glVertexAttrib4f_INDEXED INDEXED_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat
#define glVertexAttrib4f_FORMAT FORMAT_void_GLuint_GLfloat_GLfloat_GLfloat_GLfloat
#define glVertexAttrib4fv_INDEX 238
#define glVertexAttrib4fv_RETURN void
#define glVertexAttrib4fv_ARG_NAMES index, v
#define glVertexAttrib4fv_ARG_EXPAND GLuint index, const GLfloat * v
#define glVertexAttrib4fv_PACKED PACKED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib4fv_INDEXED INDEXED_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttrib4fv_FORMAT FORMAT_void_GLuint_const_GLfloat___GENPT__
#define glVertexAttribPointer_INDEX 239
#define glVertexAttribPointer_RETURN void
#define glVertexAttribPointer_ARG_NAMES index, size, type, normalized, stride, pointer
#define glVertexAttribPointer_ARG_EXPAND GLuint index, GLint size, GLenum type, GLboolean normalized, GLsizei stride, const GLvoid * pointer
#define glVertexAttribPointer_PACKED PACKED_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__
#define glVertexAttribPointer_INDEXED INDEXED_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__
#define glVertexAttribPointer_FORMAT FORMAT_void_GLuint_GLint_GLenum_GLboolean_GLsizei_const_GLvoid___GENPT__
#define glVertexPointer_INDEX 240
#define glVertexPointer_RETURN void
#define glVertexPointer_ARG_NAMES size, type, stride, pointer
#define glVertexPointer_ARG_EXPAND GLint size, GLenum type, GLsizei stride, const GLvoid * pointer
#define glVertexPointer_PACKED PACKED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glVertexPointer_INDEXED INDEXED_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glVertexPointer_FORMAT FORMAT_void_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glViewport_INDEX 241
#define glViewport_RETURN void
#define glViewport_ARG_NAMES x, y, width, height
#define glViewport_ARG_EXPAND GLint x, GLint y, GLsizei width, GLsizei height
#define glViewport_PACKED PACKED_void_GLint_GLint_GLsizei_GLsizei
#define glViewport_INDEXED INDEXED_void_GLint_GLint_GLsizei_GLsizei
#define glViewport_FORMAT FORMAT_void_GLint_GLint_GLsizei_GLsizei

#define glVertexAttribIPointer_INDEX 242
#define glVertexAttribIPointer_RETURN void
#define glVertexAttribIPointer_ARG_NAMES index, size, type, stride, pointer
#define glVertexAttribIPointer_ARG_EXPAND GLuint index, GLint size, GLenum type, GLsizei stride, const GLvoid * pointer
#define glVertexAttribIPointer_PACKED PACKED_void_GLuint_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glVertexAttribIPointer_INDEXED INDEXED_void_GLuint_GLint_GLenum_GLsizei_const_GLvoid___GENPT__
#define glVertexAttribIPointer_FORMAT FORMAT_void_GLuint_GLint_GLenum_GLsizei_const_GLvoid___GENPT__

void APIENTRY_GL4ES gl4es_glActiveTexture(glActiveTexture_ARG_EXPAND);
typedef void (APIENTRY_GLES * glActiveTexture_PTR)(glActiveTexture_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glAlphaFunc(glAlphaFunc_ARG_EXPAND);
typedef void (APIENTRY_GLES * glAlphaFunc_PTR)(glAlphaFunc_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glAlphaFuncx(glAlphaFuncx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glAlphaFuncx_PTR)(glAlphaFuncx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glAttachShader(glAttachShader_ARG_EXPAND);
typedef void (APIENTRY_GLES * glAttachShader_PTR)(glAttachShader_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBindAttribLocation(glBindAttribLocation_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBindAttribLocation_PTR)(glBindAttribLocation_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBindBuffer(glBindBuffer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBindBuffer_PTR)(glBindBuffer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBindFramebuffer(glBindFramebuffer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBindFramebuffer_PTR)(glBindFramebuffer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBindRenderbuffer(glBindRenderbuffer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBindRenderbuffer_PTR)(glBindRenderbuffer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBindTexture(glBindTexture_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBindTexture_PTR)(glBindTexture_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBlendColor(glBlendColor_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBlendColor_PTR)(glBlendColor_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBlendEquation(glBlendEquation_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBlendEquation_PTR)(glBlendEquation_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBlendEquationSeparate(glBlendEquationSeparate_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBlendEquationSeparate_PTR)(glBlendEquationSeparate_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBlendFunc(glBlendFunc_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBlendFunc_PTR)(glBlendFunc_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBlendFuncSeparate(glBlendFuncSeparate_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBlendFuncSeparate_PTR)(glBlendFuncSeparate_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBufferData(glBufferData_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBufferData_PTR)(glBufferData_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glBufferSubData(glBufferSubData_ARG_EXPAND);
typedef void (APIENTRY_GLES * glBufferSubData_PTR)(glBufferSubData_ARG_EXPAND);
GLenum APIENTRY_GL4ES gl4es_glCheckFramebufferStatus(glCheckFramebufferStatus_ARG_EXPAND);
typedef GLenum (APIENTRY_GLES * glCheckFramebufferStatus_PTR)(glCheckFramebufferStatus_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClear(glClear_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClear_PTR)(glClear_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClearColor(glClearColor_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClearColor_PTR)(glClearColor_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClearColorx(glClearColorx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClearColorx_PTR)(glClearColorx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClearDepthf(glClearDepthf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClearDepthf_PTR)(glClearDepthf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClearDepthx(glClearDepthx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClearDepthx_PTR)(glClearDepthx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClearStencil(glClearStencil_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClearStencil_PTR)(glClearStencil_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClientActiveTexture(glClientActiveTexture_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClientActiveTexture_PTR)(glClientActiveTexture_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClipPlanef(glClipPlanef_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClipPlanef_PTR)(glClipPlanef_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glClipPlanex(glClipPlanex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glClipPlanex_PTR)(glClipPlanex_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glColor4f(glColor4f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glColor4f_PTR)(glColor4f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glColor4ub(glColor4ub_ARG_EXPAND);
typedef void (APIENTRY_GLES * glColor4ub_PTR)(glColor4ub_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glColor4x(glColor4x_ARG_EXPAND);
typedef void (APIENTRY_GLES * glColor4x_PTR)(glColor4x_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glColorMask(glColorMask_ARG_EXPAND);
typedef void (APIENTRY_GLES * glColorMask_PTR)(glColorMask_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glColorPointer(glColorPointer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glColorPointer_PTR)(glColorPointer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glCompileShader(glCompileShader_ARG_EXPAND);
typedef void (APIENTRY_GLES * glCompileShader_PTR)(glCompileShader_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glCompressedTexImage2D(glCompressedTexImage2D_ARG_EXPAND);
typedef void (APIENTRY_GLES * glCompressedTexImage2D_PTR)(glCompressedTexImage2D_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glCompressedTexSubImage2D(glCompressedTexSubImage2D_ARG_EXPAND);
typedef void (APIENTRY_GLES * glCompressedTexSubImage2D_PTR)(glCompressedTexSubImage2D_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glCopyTexImage2D(glCopyTexImage2D_ARG_EXPAND);
typedef void (APIENTRY_GLES * glCopyTexImage2D_PTR)(glCopyTexImage2D_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glCopyTexSubImage2D(glCopyTexSubImage2D_ARG_EXPAND);
typedef void (APIENTRY_GLES * glCopyTexSubImage2D_PTR)(glCopyTexSubImage2D_ARG_EXPAND);
GLuint APIENTRY_GL4ES gl4es_glCreateProgram(glCreateProgram_ARG_EXPAND);
typedef GLuint (APIENTRY_GLES * glCreateProgram_PTR)(glCreateProgram_ARG_EXPAND);
GLuint APIENTRY_GL4ES gl4es_glCreateShader(glCreateShader_ARG_EXPAND);
typedef GLuint (APIENTRY_GLES * glCreateShader_PTR)(glCreateShader_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glCullFace(glCullFace_ARG_EXPAND);
typedef void (APIENTRY_GLES * glCullFace_PTR)(glCullFace_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDeleteBuffers(glDeleteBuffers_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDeleteBuffers_PTR)(glDeleteBuffers_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDeleteFramebuffers(glDeleteFramebuffers_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDeleteFramebuffers_PTR)(glDeleteFramebuffers_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDeleteProgram(glDeleteProgram_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDeleteProgram_PTR)(glDeleteProgram_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDeleteRenderbuffers(glDeleteRenderbuffers_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDeleteRenderbuffers_PTR)(glDeleteRenderbuffers_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDeleteShader(glDeleteShader_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDeleteShader_PTR)(glDeleteShader_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDeleteTextures(glDeleteTextures_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDeleteTextures_PTR)(glDeleteTextures_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDepthFunc(glDepthFunc_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDepthFunc_PTR)(glDepthFunc_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDepthMask(glDepthMask_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDepthMask_PTR)(glDepthMask_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDepthRangef(glDepthRangef_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDepthRangef_PTR)(glDepthRangef_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDepthRangex(glDepthRangex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDepthRangex_PTR)(glDepthRangex_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDetachShader(glDetachShader_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDetachShader_PTR)(glDetachShader_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDisable(glDisable_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDisable_PTR)(glDisable_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDisableClientState(glDisableClientState_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDisableClientState_PTR)(glDisableClientState_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDisableVertexAttribArray(glDisableVertexAttribArray_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDisableVertexAttribArray_PTR)(glDisableVertexAttribArray_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDrawArrays(glDrawArrays_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDrawArrays_PTR)(glDrawArrays_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDrawBuffers(glDrawBuffers_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDrawBuffers_PTR)(glDrawBuffers_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDrawElements(glDrawElements_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDrawElements_PTR)(glDrawElements_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDrawTexf(glDrawTexf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDrawTexf_PTR)(glDrawTexf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glDrawTexi(glDrawTexi_ARG_EXPAND);
typedef void (APIENTRY_GLES * glDrawTexi_PTR)(glDrawTexi_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glEnable(glEnable_ARG_EXPAND);
typedef void (APIENTRY_GLES * glEnable_PTR)(glEnable_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glEnableClientState(glEnableClientState_ARG_EXPAND);
typedef void (APIENTRY_GLES * glEnableClientState_PTR)(glEnableClientState_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glEnableVertexAttribArray(glEnableVertexAttribArray_ARG_EXPAND);
typedef void (APIENTRY_GLES * glEnableVertexAttribArray_PTR)(glEnableVertexAttribArray_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFinish(glFinish_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFinish_PTR)(glFinish_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFlush(glFlush_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFlush_PTR)(glFlush_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFogCoordPointer(glFogCoordPointer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFogCoordPointer_PTR)(glFogCoordPointer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFogCoordf(glFogCoordf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFogCoordf_PTR)(glFogCoordf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFogCoordfv(glFogCoordfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFogCoordfv_PTR)(glFogCoordfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFogf(glFogf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFogf_PTR)(glFogf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFogfv(glFogfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFogfv_PTR)(glFogfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFogx(glFogx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFogx_PTR)(glFogx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFogxv(glFogxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFogxv_PTR)(glFogxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFramebufferRenderbuffer(glFramebufferRenderbuffer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFramebufferRenderbuffer_PTR)(glFramebufferRenderbuffer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFramebufferTexture2D(glFramebufferTexture2D_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFramebufferTexture2D_PTR)(glFramebufferTexture2D_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFrontFace(glFrontFace_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFrontFace_PTR)(glFrontFace_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFrustumf(glFrustumf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFrustumf_PTR)(glFrustumf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glFrustumx(glFrustumx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glFrustumx_PTR)(glFrustumx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGenBuffers(glGenBuffers_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGenBuffers_PTR)(glGenBuffers_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGenFramebuffers(glGenFramebuffers_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGenFramebuffers_PTR)(glGenFramebuffers_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGenRenderbuffers(glGenRenderbuffers_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGenRenderbuffers_PTR)(glGenRenderbuffers_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGenTextures(glGenTextures_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGenTextures_PTR)(glGenTextures_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGenerateMipmap(glGenerateMipmap_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGenerateMipmap_PTR)(glGenerateMipmap_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetActiveAttrib(glGetActiveAttrib_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetActiveAttrib_PTR)(glGetActiveAttrib_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetActiveUniform(glGetActiveUniform_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetActiveUniform_PTR)(glGetActiveUniform_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetAttachedShaders(glGetAttachedShaders_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetAttachedShaders_PTR)(glGetAttachedShaders_ARG_EXPAND);
GLint APIENTRY_GL4ES gl4es_glGetAttribLocation(glGetAttribLocation_ARG_EXPAND);
typedef GLint (APIENTRY_GLES * glGetAttribLocation_PTR)(glGetAttribLocation_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetBooleanv(glGetBooleanv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetBooleanv_PTR)(glGetBooleanv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetBufferParameteriv(glGetBufferParameteriv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetBufferParameteriv_PTR)(glGetBufferParameteriv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetClipPlanef(glGetClipPlanef_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetClipPlanef_PTR)(glGetClipPlanef_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetClipPlanex(glGetClipPlanex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetClipPlanex_PTR)(glGetClipPlanex_ARG_EXPAND);
GLenum APIENTRY_GL4ES gl4es_glGetError(glGetError_ARG_EXPAND);
typedef GLenum (APIENTRY_GLES * glGetError_PTR)(glGetError_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetFixedv(glGetFixedv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetFixedv_PTR)(glGetFixedv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetFloatv(glGetFloatv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetFloatv_PTR)(glGetFloatv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetFramebufferAttachmentParameteriv(glGetFramebufferAttachmentParameteriv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetFramebufferAttachmentParameteriv_PTR)(glGetFramebufferAttachmentParameteriv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetIntegerv(glGetIntegerv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetIntegerv_PTR)(glGetIntegerv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetLightfv(glGetLightfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetLightfv_PTR)(glGetLightfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetLightxv(glGetLightxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetLightxv_PTR)(glGetLightxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetMaterialfv(glGetMaterialfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetMaterialfv_PTR)(glGetMaterialfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetMaterialxv(glGetMaterialxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetMaterialxv_PTR)(glGetMaterialxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetPointerv(glGetPointerv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetPointerv_PTR)(glGetPointerv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetProgramBinary(glGetProgramBinary_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetProgramBinary_PTR)(glGetProgramBinary_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetProgramInfoLog(glGetProgramInfoLog_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetProgramInfoLog_PTR)(glGetProgramInfoLog_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetProgramiv(glGetProgramiv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetProgramiv_PTR)(glGetProgramiv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetRenderbufferParameteriv(glGetRenderbufferParameteriv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetRenderbufferParameteriv_PTR)(glGetRenderbufferParameteriv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetShaderInfoLog(glGetShaderInfoLog_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetShaderInfoLog_PTR)(glGetShaderInfoLog_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetShaderPrecisionFormat(glGetShaderPrecisionFormat_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetShaderPrecisionFormat_PTR)(glGetShaderPrecisionFormat_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetShaderSource(glGetShaderSource_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetShaderSource_PTR)(glGetShaderSource_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetShaderiv(glGetShaderiv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetShaderiv_PTR)(glGetShaderiv_ARG_EXPAND);
const GLubyte * APIENTRY_GL4ES gl4es_glGetString(glGetString_ARG_EXPAND);
typedef const GLubyte * (APIENTRY_GLES * glGetString_PTR)(glGetString_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetTexEnvfv(glGetTexEnvfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetTexEnvfv_PTR)(glGetTexEnvfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetTexEnviv(glGetTexEnviv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetTexEnviv_PTR)(glGetTexEnviv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetTexEnvxv(glGetTexEnvxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetTexEnvxv_PTR)(glGetTexEnvxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetTexParameterfv(glGetTexParameterfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetTexParameterfv_PTR)(glGetTexParameterfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetTexParameteriv(glGetTexParameteriv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetTexParameteriv_PTR)(glGetTexParameteriv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetTexParameterxv(glGetTexParameterxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetTexParameterxv_PTR)(glGetTexParameterxv_ARG_EXPAND);
GLint APIENTRY_GL4ES gl4es_glGetUniformLocation(glGetUniformLocation_ARG_EXPAND);
typedef GLint (APIENTRY_GLES * glGetUniformLocation_PTR)(glGetUniformLocation_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetUniformfv(glGetUniformfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetUniformfv_PTR)(glGetUniformfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetUniformiv(glGetUniformiv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetUniformiv_PTR)(glGetUniformiv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetVertexAttribPointerv(glGetVertexAttribPointerv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetVertexAttribPointerv_PTR)(glGetVertexAttribPointerv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetVertexAttribfv(glGetVertexAttribfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetVertexAttribfv_PTR)(glGetVertexAttribfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glGetVertexAttribiv(glGetVertexAttribiv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glGetVertexAttribiv_PTR)(glGetVertexAttribiv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glHint(glHint_ARG_EXPAND);
typedef void (APIENTRY_GLES * glHint_PTR)(glHint_ARG_EXPAND);
GLboolean APIENTRY_GL4ES gl4es_glIsBuffer(glIsBuffer_ARG_EXPAND);
typedef GLboolean (APIENTRY_GLES * glIsBuffer_PTR)(glIsBuffer_ARG_EXPAND);
GLboolean APIENTRY_GL4ES gl4es_glIsEnabled(glIsEnabled_ARG_EXPAND);
typedef GLboolean (APIENTRY_GLES * glIsEnabled_PTR)(glIsEnabled_ARG_EXPAND);
GLboolean APIENTRY_GL4ES gl4es_glIsFramebuffer(glIsFramebuffer_ARG_EXPAND);
typedef GLboolean (APIENTRY_GLES * glIsFramebuffer_PTR)(glIsFramebuffer_ARG_EXPAND);
GLboolean APIENTRY_GL4ES gl4es_glIsProgram(glIsProgram_ARG_EXPAND);
typedef GLboolean (APIENTRY_GLES * glIsProgram_PTR)(glIsProgram_ARG_EXPAND);
GLboolean APIENTRY_GL4ES gl4es_glIsRenderbuffer(glIsRenderbuffer_ARG_EXPAND);
typedef GLboolean (APIENTRY_GLES * glIsRenderbuffer_PTR)(glIsRenderbuffer_ARG_EXPAND);
GLboolean APIENTRY_GL4ES gl4es_glIsShader(glIsShader_ARG_EXPAND);
typedef GLboolean (APIENTRY_GLES * glIsShader_PTR)(glIsShader_ARG_EXPAND);
GLboolean APIENTRY_GL4ES gl4es_glIsTexture(glIsTexture_ARG_EXPAND);
typedef GLboolean (APIENTRY_GLES * glIsTexture_PTR)(glIsTexture_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightModelf(glLightModelf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightModelf_PTR)(glLightModelf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightModelfv(glLightModelfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightModelfv_PTR)(glLightModelfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightModelx(glLightModelx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightModelx_PTR)(glLightModelx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightModelxv(glLightModelxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightModelxv_PTR)(glLightModelxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightf(glLightf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightf_PTR)(glLightf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightfv(glLightfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightfv_PTR)(glLightfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightx(glLightx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightx_PTR)(glLightx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLightxv(glLightxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLightxv_PTR)(glLightxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLineWidth(glLineWidth_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLineWidth_PTR)(glLineWidth_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLineWidthx(glLineWidthx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLineWidthx_PTR)(glLineWidthx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLinkProgram(glLinkProgram_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLinkProgram_PTR)(glLinkProgram_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLoadIdentity(glLoadIdentity_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLoadIdentity_PTR)(glLoadIdentity_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLoadMatrixf(glLoadMatrixf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLoadMatrixf_PTR)(glLoadMatrixf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLoadMatrixx(glLoadMatrixx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLoadMatrixx_PTR)(glLoadMatrixx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glLogicOp(glLogicOp_ARG_EXPAND);
typedef void (APIENTRY_GLES * glLogicOp_PTR)(glLogicOp_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMaterialf(glMaterialf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMaterialf_PTR)(glMaterialf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMaterialfv(glMaterialfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMaterialfv_PTR)(glMaterialfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMaterialx(glMaterialx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMaterialx_PTR)(glMaterialx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMaterialxv(glMaterialxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMaterialxv_PTR)(glMaterialxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMatrixMode(glMatrixMode_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMatrixMode_PTR)(glMatrixMode_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMultMatrixf(glMultMatrixf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMultMatrixf_PTR)(glMultMatrixf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMultMatrixx(glMultMatrixx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMultMatrixx_PTR)(glMultMatrixx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMultiDrawArrays(glMultiDrawArrays_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMultiDrawArrays_PTR)(glMultiDrawArrays_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMultiDrawElements(glMultiDrawElements_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMultiDrawElements_PTR)(glMultiDrawElements_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMultiTexCoord4f(glMultiTexCoord4f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMultiTexCoord4f_PTR)(glMultiTexCoord4f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glMultiTexCoord4x(glMultiTexCoord4x_ARG_EXPAND);
typedef void (APIENTRY_GLES * glMultiTexCoord4x_PTR)(glMultiTexCoord4x_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glNormal3f(glNormal3f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glNormal3f_PTR)(glNormal3f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glNormal3x(glNormal3x_ARG_EXPAND);
typedef void (APIENTRY_GLES * glNormal3x_PTR)(glNormal3x_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glNormalPointer(glNormalPointer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glNormalPointer_PTR)(glNormalPointer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glOrthof(glOrthof_ARG_EXPAND);
typedef void (APIENTRY_GLES * glOrthof_PTR)(glOrthof_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glOrthox(glOrthox_ARG_EXPAND);
typedef void (APIENTRY_GLES * glOrthox_PTR)(glOrthox_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPixelStorei(glPixelStorei_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPixelStorei_PTR)(glPixelStorei_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPointParameterf(glPointParameterf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPointParameterf_PTR)(glPointParameterf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPointParameterfv(glPointParameterfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPointParameterfv_PTR)(glPointParameterfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPointParameterx(glPointParameterx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPointParameterx_PTR)(glPointParameterx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPointParameterxv(glPointParameterxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPointParameterxv_PTR)(glPointParameterxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPointSize(glPointSize_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPointSize_PTR)(glPointSize_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPointSizePointerOES(glPointSizePointerOES_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPointSizePointerOES_PTR)(glPointSizePointerOES_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPointSizex(glPointSizex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPointSizex_PTR)(glPointSizex_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPolygonOffset(glPolygonOffset_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPolygonOffset_PTR)(glPolygonOffset_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPolygonOffsetx(glPolygonOffsetx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPolygonOffsetx_PTR)(glPolygonOffsetx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPopMatrix(glPopMatrix_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPopMatrix_PTR)(glPopMatrix_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glProgramBinary(glProgramBinary_ARG_EXPAND);
typedef void (APIENTRY_GLES * glProgramBinary_PTR)(glProgramBinary_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glPushMatrix(glPushMatrix_ARG_EXPAND);
typedef void (APIENTRY_GLES * glPushMatrix_PTR)(glPushMatrix_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glReadPixels(glReadPixels_ARG_EXPAND);
typedef void (APIENTRY_GLES * glReadPixels_PTR)(glReadPixels_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glReleaseShaderCompiler(glReleaseShaderCompiler_ARG_EXPAND);
typedef void (APIENTRY_GLES * glReleaseShaderCompiler_PTR)(glReleaseShaderCompiler_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glRenderbufferStorage(glRenderbufferStorage_ARG_EXPAND);
typedef void (APIENTRY_GLES * glRenderbufferStorage_PTR)(glRenderbufferStorage_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glRotatef(glRotatef_ARG_EXPAND);
typedef void (APIENTRY_GLES * glRotatef_PTR)(glRotatef_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glRotatex(glRotatex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glRotatex_PTR)(glRotatex_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glSampleCoverage(glSampleCoverage_ARG_EXPAND);
typedef void (APIENTRY_GLES * glSampleCoverage_PTR)(glSampleCoverage_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glSampleCoveragex(glSampleCoveragex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glSampleCoveragex_PTR)(glSampleCoveragex_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glScalef(glScalef_ARG_EXPAND);
typedef void (APIENTRY_GLES * glScalef_PTR)(glScalef_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glScalex(glScalex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glScalex_PTR)(glScalex_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glScissor(glScissor_ARG_EXPAND);
typedef void (APIENTRY_GLES * glScissor_PTR)(glScissor_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glShadeModel(glShadeModel_ARG_EXPAND);
typedef void (APIENTRY_GLES * glShadeModel_PTR)(glShadeModel_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glShaderBinary(glShaderBinary_ARG_EXPAND);
typedef void (APIENTRY_GLES * glShaderBinary_PTR)(glShaderBinary_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glShaderSource(glShaderSource_ARG_EXPAND);
typedef void (APIENTRY_GLES * glShaderSource_PTR)(glShaderSource_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glStencilFunc(glStencilFunc_ARG_EXPAND);
typedef void (APIENTRY_GLES * glStencilFunc_PTR)(glStencilFunc_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glStencilFuncSeparate(glStencilFuncSeparate_ARG_EXPAND);
typedef void (APIENTRY_GLES * glStencilFuncSeparate_PTR)(glStencilFuncSeparate_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glStencilMask(glStencilMask_ARG_EXPAND);
typedef void (APIENTRY_GLES * glStencilMask_PTR)(glStencilMask_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glStencilMaskSeparate(glStencilMaskSeparate_ARG_EXPAND);
typedef void (APIENTRY_GLES * glStencilMaskSeparate_PTR)(glStencilMaskSeparate_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glStencilOp(glStencilOp_ARG_EXPAND);
typedef void (APIENTRY_GLES * glStencilOp_PTR)(glStencilOp_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glStencilOpSeparate(glStencilOpSeparate_ARG_EXPAND);
typedef void (APIENTRY_GLES * glStencilOpSeparate_PTR)(glStencilOpSeparate_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexCoordPointer(glTexCoordPointer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexCoordPointer_PTR)(glTexCoordPointer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexEnvf(glTexEnvf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexEnvf_PTR)(glTexEnvf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexEnvfv(glTexEnvfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexEnvfv_PTR)(glTexEnvfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexEnvi(glTexEnvi_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexEnvi_PTR)(glTexEnvi_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexEnviv(glTexEnviv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexEnviv_PTR)(glTexEnviv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexEnvx(glTexEnvx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexEnvx_PTR)(glTexEnvx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexEnvxv(glTexEnvxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexEnvxv_PTR)(glTexEnvxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexGenfv(glTexGenfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexGenfv_PTR)(glTexGenfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexGeni(glTexGeni_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexGeni_PTR)(glTexGeni_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexImage2D(glTexImage2D_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexImage2D_PTR)(glTexImage2D_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexParameterf(glTexParameterf_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexParameterf_PTR)(glTexParameterf_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexParameterfv(glTexParameterfv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexParameterfv_PTR)(glTexParameterfv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexParameteri(glTexParameteri_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexParameteri_PTR)(glTexParameteri_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexParameteriv(glTexParameteriv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexParameteriv_PTR)(glTexParameteriv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexParameterx(glTexParameterx_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexParameterx_PTR)(glTexParameterx_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexParameterxv(glTexParameterxv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexParameterxv_PTR)(glTexParameterxv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTexSubImage2D(glTexSubImage2D_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTexSubImage2D_PTR)(glTexSubImage2D_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTranslatef(glTranslatef_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTranslatef_PTR)(glTranslatef_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glTranslatex(glTranslatex_ARG_EXPAND);
typedef void (APIENTRY_GLES * glTranslatex_PTR)(glTranslatex_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform1f(glUniform1f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform1f_PTR)(glUniform1f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform1fv(glUniform1fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform1fv_PTR)(glUniform1fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform1i(glUniform1i_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform1i_PTR)(glUniform1i_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform1iv(glUniform1iv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform1iv_PTR)(glUniform1iv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform2f(glUniform2f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform2f_PTR)(glUniform2f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform2fv(glUniform2fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform2fv_PTR)(glUniform2fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform2i(glUniform2i_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform2i_PTR)(glUniform2i_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform2iv(glUniform2iv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform2iv_PTR)(glUniform2iv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform3f(glUniform3f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform3f_PTR)(glUniform3f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform3fv(glUniform3fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform3fv_PTR)(glUniform3fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform3i(glUniform3i_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform3i_PTR)(glUniform3i_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform3iv(glUniform3iv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform3iv_PTR)(glUniform3iv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform4f(glUniform4f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform4f_PTR)(glUniform4f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform4fv(glUniform4fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform4fv_PTR)(glUniform4fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform4i(glUniform4i_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform4i_PTR)(glUniform4i_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniform4iv(glUniform4iv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniform4iv_PTR)(glUniform4iv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniformMatrix2fv(glUniformMatrix2fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniformMatrix2fv_PTR)(glUniformMatrix2fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniformMatrix3fv(glUniformMatrix3fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniformMatrix3fv_PTR)(glUniformMatrix3fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUniformMatrix4fv(glUniformMatrix4fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUniformMatrix4fv_PTR)(glUniformMatrix4fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glUseProgram(glUseProgram_ARG_EXPAND);
typedef void (APIENTRY_GLES * glUseProgram_PTR)(glUseProgram_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glValidateProgram(glValidateProgram_ARG_EXPAND);
typedef void (APIENTRY_GLES * glValidateProgram_PTR)(glValidateProgram_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib1f(glVertexAttrib1f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib1f_PTR)(glVertexAttrib1f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib1fv(glVertexAttrib1fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib1fv_PTR)(glVertexAttrib1fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib2f(glVertexAttrib2f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib2f_PTR)(glVertexAttrib2f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib2fv(glVertexAttrib2fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib2fv_PTR)(glVertexAttrib2fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib3f(glVertexAttrib3f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib3f_PTR)(glVertexAttrib3f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib3fv(glVertexAttrib3fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib3fv_PTR)(glVertexAttrib3fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib4f(glVertexAttrib4f_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib4f_PTR)(glVertexAttrib4f_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttrib4fv(glVertexAttrib4fv_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttrib4fv_PTR)(glVertexAttrib4fv_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexAttribPointer(glVertexAttribPointer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttribPointer_PTR)(glVertexAttribPointer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glVertexPointer(glVertexPointer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexPointer_PTR)(glVertexPointer_ARG_EXPAND);
void APIENTRY_GL4ES gl4es_glViewport(glViewport_ARG_EXPAND);
typedef void (APIENTRY_GLES * glViewport_PTR)(glViewport_ARG_EXPAND);

void APIENTRY_GL4ES gl4es_glVertexAttribIPointer(glVertexAttribIPointer_ARG_EXPAND);
typedef void (APIENTRY_GLES * glVertexAttribIPointer_PTR)(glVertexAttribIPointer_ARG_EXPAND);



#ifndef direct_glActiveTexture
#define push_glActiveTexture(texture) { \
    glActiveTexture_PACKED *packed_data = malloc(sizeof(glActiveTexture_PACKED)); \
    packed_data->format = glActiveTexture_FORMAT; \
    packed_data->func = gl4es_glActiveTexture; \
    packed_data->args.a1 = (GLenum)texture; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glAlphaFunc
#define push_glAlphaFunc(func, ref) { \
    glAlphaFunc_PACKED *packed_data = malloc(sizeof(glAlphaFunc_PACKED)); \
    packed_data->format = glAlphaFunc_FORMAT; \
    packed_data->func = gl4es_glAlphaFunc; \
    packed_data->args.a1 = (GLenum)func; \
    packed_data->args.a2 = (GLclampf)ref; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glAlphaFuncx
#define push_glAlphaFuncx(func, ref) { \
    glAlphaFuncx_PACKED *packed_data = malloc(sizeof(glAlphaFuncx_PACKED)); \
    packed_data->format = glAlphaFuncx_FORMAT; \
    packed_data->func = gl4es_glAlphaFuncx; \
    packed_data->args.a1 = (GLenum)func; \
    packed_data->args.a2 = (GLclampx)ref; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glAttachShader
#define push_glAttachShader(program, shader) { \
    glAttachShader_PACKED *packed_data = malloc(sizeof(glAttachShader_PACKED)); \
    packed_data->format = glAttachShader_FORMAT; \
    packed_data->func = gl4es_glAttachShader; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLuint)shader; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBindAttribLocation
#define push_glBindAttribLocation(program, index, name) { \
    glBindAttribLocation_PACKED *packed_data = malloc(sizeof(glBindAttribLocation_PACKED)); \
    packed_data->format = glBindAttribLocation_FORMAT; \
    packed_data->func = gl4es_glBindAttribLocation; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLuint)index; \
    packed_data->args.a3 = (GLchar *)name; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBindBuffer
#define push_glBindBuffer(target, buffer) { \
    glBindBuffer_PACKED *packed_data = malloc(sizeof(glBindBuffer_PACKED)); \
    packed_data->format = glBindBuffer_FORMAT; \
    packed_data->func = gl4es_glBindBuffer; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLuint)buffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBindFramebuffer
#define push_glBindFramebuffer(target, framebuffer) { \
    glBindFramebuffer_PACKED *packed_data = malloc(sizeof(glBindFramebuffer_PACKED)); \
    packed_data->format = glBindFramebuffer_FORMAT; \
    packed_data->func = gl4es_glBindFramebuffer; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLuint)framebuffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBindRenderbuffer
#define push_glBindRenderbuffer(target, renderbuffer) { \
    glBindRenderbuffer_PACKED *packed_data = malloc(sizeof(glBindRenderbuffer_PACKED)); \
    packed_data->format = glBindRenderbuffer_FORMAT; \
    packed_data->func = gl4es_glBindRenderbuffer; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLuint)renderbuffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBindTexture
#define push_glBindTexture(target, texture) { \
    glBindTexture_PACKED *packed_data = malloc(sizeof(glBindTexture_PACKED)); \
    packed_data->format = glBindTexture_FORMAT; \
    packed_data->func = gl4es_glBindTexture; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLuint)texture; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBlendColor
#define push_glBlendColor(red, green, blue, alpha) { \
    glBlendColor_PACKED *packed_data = malloc(sizeof(glBlendColor_PACKED)); \
    packed_data->format = glBlendColor_FORMAT; \
    packed_data->func = gl4es_glBlendColor; \
    packed_data->args.a1 = (GLclampf)red; \
    packed_data->args.a2 = (GLclampf)green; \
    packed_data->args.a3 = (GLclampf)blue; \
    packed_data->args.a4 = (GLclampf)alpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBlendEquation
#define push_glBlendEquation(mode) { \
    glBlendEquation_PACKED *packed_data = malloc(sizeof(glBlendEquation_PACKED)); \
    packed_data->format = glBlendEquation_FORMAT; \
    packed_data->func = gl4es_glBlendEquation; \
    packed_data->args.a1 = (GLenum)mode; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBlendEquationSeparate
#define push_glBlendEquationSeparate(modeRGB, modeA) { \
    glBlendEquationSeparate_PACKED *packed_data = malloc(sizeof(glBlendEquationSeparate_PACKED)); \
    packed_data->format = glBlendEquationSeparate_FORMAT; \
    packed_data->func = gl4es_glBlendEquationSeparate; \
    packed_data->args.a1 = (GLenum)modeRGB; \
    packed_data->args.a2 = (GLenum)modeA; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBlendFunc
#define push_glBlendFunc(sfactor, dfactor) { \
    glBlendFunc_PACKED *packed_data = malloc(sizeof(glBlendFunc_PACKED)); \
    packed_data->format = glBlendFunc_FORMAT; \
    packed_data->func = gl4es_glBlendFunc; \
    packed_data->args.a1 = (GLenum)sfactor; \
    packed_data->args.a2 = (GLenum)dfactor; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBlendFuncSeparate
#define push_glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha) { \
    glBlendFuncSeparate_PACKED *packed_data = malloc(sizeof(glBlendFuncSeparate_PACKED)); \
    packed_data->format = glBlendFuncSeparate_FORMAT; \
    packed_data->func = gl4es_glBlendFuncSeparate; \
    packed_data->args.a1 = (GLenum)sfactorRGB; \
    packed_data->args.a2 = (GLenum)dfactorRGB; \
    packed_data->args.a3 = (GLenum)sfactorAlpha; \
    packed_data->args.a4 = (GLenum)dfactorAlpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBufferData
#define push_glBufferData(target, size, data, usage) { \
    glBufferData_PACKED *packed_data = malloc(sizeof(glBufferData_PACKED)); \
    packed_data->format = glBufferData_FORMAT; \
    packed_data->func = gl4es_glBufferData; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLsizeiptr)size; \
    packed_data->args.a3 = (GLvoid *)data; \
    packed_data->args.a4 = (GLenum)usage; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glBufferSubData
#define push_glBufferSubData(target, offset, size, data) { \
    glBufferSubData_PACKED *packed_data = malloc(sizeof(glBufferSubData_PACKED)); \
    packed_data->format = glBufferSubData_FORMAT; \
    packed_data->func = gl4es_glBufferSubData; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLintptr)offset; \
    packed_data->args.a3 = (GLsizeiptr)size; \
    packed_data->args.a4 = (GLvoid *)data; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCheckFramebufferStatus
#define push_glCheckFramebufferStatus(target) { \
    glCheckFramebufferStatus_PACKED *packed_data = malloc(sizeof(glCheckFramebufferStatus_PACKED)); \
    packed_data->format = glCheckFramebufferStatus_FORMAT; \
    packed_data->func = gl4es_glCheckFramebufferStatus; \
    packed_data->args.a1 = (GLenum)target; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClear
#define push_glClear(mask) { \
    glClear_PACKED *packed_data = malloc(sizeof(glClear_PACKED)); \
    packed_data->format = glClear_FORMAT; \
    packed_data->func = gl4es_glClear; \
    packed_data->args.a1 = (GLbitfield)mask; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClearColor
#define push_glClearColor(red, green, blue, alpha) { \
    glClearColor_PACKED *packed_data = malloc(sizeof(glClearColor_PACKED)); \
    packed_data->format = glClearColor_FORMAT; \
    packed_data->func = gl4es_glClearColor; \
    packed_data->args.a1 = (GLclampf)red; \
    packed_data->args.a2 = (GLclampf)green; \
    packed_data->args.a3 = (GLclampf)blue; \
    packed_data->args.a4 = (GLclampf)alpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClearColorx
#define push_glClearColorx(red, green, blue, alpha) { \
    glClearColorx_PACKED *packed_data = malloc(sizeof(glClearColorx_PACKED)); \
    packed_data->format = glClearColorx_FORMAT; \
    packed_data->func = gl4es_glClearColorx; \
    packed_data->args.a1 = (GLclampx)red; \
    packed_data->args.a2 = (GLclampx)green; \
    packed_data->args.a3 = (GLclampx)blue; \
    packed_data->args.a4 = (GLclampx)alpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClearDepthf
#define push_glClearDepthf(depth) { \
    glClearDepthf_PACKED *packed_data = malloc(sizeof(glClearDepthf_PACKED)); \
    packed_data->format = glClearDepthf_FORMAT; \
    packed_data->func = gl4es_glClearDepthf; \
    packed_data->args.a1 = (GLclampf)depth; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClearDepthx
#define push_glClearDepthx(depth) { \
    glClearDepthx_PACKED *packed_data = malloc(sizeof(glClearDepthx_PACKED)); \
    packed_data->format = glClearDepthx_FORMAT; \
    packed_data->func = gl4es_glClearDepthx; \
    packed_data->args.a1 = (GLclampx)depth; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClearStencil
#define push_glClearStencil(s) { \
    glClearStencil_PACKED *packed_data = malloc(sizeof(glClearStencil_PACKED)); \
    packed_data->format = glClearStencil_FORMAT; \
    packed_data->func = gl4es_glClearStencil; \
    packed_data->args.a1 = (GLint)s; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClientActiveTexture
#define push_glClientActiveTexture(texture) { \
    glClientActiveTexture_PACKED *packed_data = malloc(sizeof(glClientActiveTexture_PACKED)); \
    packed_data->format = glClientActiveTexture_FORMAT; \
    packed_data->func = gl4es_glClientActiveTexture; \
    packed_data->args.a1 = (GLenum)texture; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClipPlanef
#define push_glClipPlanef(plane, equation) { \
    glClipPlanef_PACKED *packed_data = malloc(sizeof(glClipPlanef_PACKED)); \
    packed_data->format = glClipPlanef_FORMAT; \
    packed_data->func = gl4es_glClipPlanef; \
    packed_data->args.a1 = (GLenum)plane; \
    packed_data->args.a2 = (GLfloat *)equation; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glClipPlanex
#define push_glClipPlanex(plane, equation) { \
    glClipPlanex_PACKED *packed_data = malloc(sizeof(glClipPlanex_PACKED)); \
    packed_data->format = glClipPlanex_FORMAT; \
    packed_data->func = gl4es_glClipPlanex; \
    packed_data->args.a1 = (GLenum)plane; \
    packed_data->args.a2 = (GLfixed *)equation; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glColor4f
#define push_glColor4f(red, green, blue, alpha) { \
    glColor4f_PACKED *packed_data = malloc(sizeof(glColor4f_PACKED)); \
    packed_data->format = glColor4f_FORMAT; \
    packed_data->func = gl4es_glColor4f; \
    packed_data->args.a1 = (GLfloat)red; \
    packed_data->args.a2 = (GLfloat)green; \
    packed_data->args.a3 = (GLfloat)blue; \
    packed_data->args.a4 = (GLfloat)alpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glColor4ub
#define push_glColor4ub(red, green, blue, alpha) { \
    glColor4ub_PACKED *packed_data = malloc(sizeof(glColor4ub_PACKED)); \
    packed_data->format = glColor4ub_FORMAT; \
    packed_data->func = gl4es_glColor4ub; \
    packed_data->args.a1 = (GLubyte)red; \
    packed_data->args.a2 = (GLubyte)green; \
    packed_data->args.a3 = (GLubyte)blue; \
    packed_data->args.a4 = (GLubyte)alpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glColor4x
#define push_glColor4x(red, green, blue, alpha) { \
    glColor4x_PACKED *packed_data = malloc(sizeof(glColor4x_PACKED)); \
    packed_data->format = glColor4x_FORMAT; \
    packed_data->func = gl4es_glColor4x; \
    packed_data->args.a1 = (GLfixed)red; \
    packed_data->args.a2 = (GLfixed)green; \
    packed_data->args.a3 = (GLfixed)blue; \
    packed_data->args.a4 = (GLfixed)alpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glColorMask
#define push_glColorMask(red, green, blue, alpha) { \
    glColorMask_PACKED *packed_data = malloc(sizeof(glColorMask_PACKED)); \
    packed_data->format = glColorMask_FORMAT; \
    packed_data->func = gl4es_glColorMask; \
    packed_data->args.a1 = (GLboolean)red; \
    packed_data->args.a2 = (GLboolean)green; \
    packed_data->args.a3 = (GLboolean)blue; \
    packed_data->args.a4 = (GLboolean)alpha; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glColorPointer
#define push_glColorPointer(size, type, stride, pointer) { \
    glColorPointer_PACKED *packed_data = malloc(sizeof(glColorPointer_PACKED)); \
    packed_data->format = glColorPointer_FORMAT; \
    packed_data->func = gl4es_glColorPointer; \
    packed_data->args.a1 = (GLint)size; \
    packed_data->args.a2 = (GLenum)type; \
    packed_data->args.a3 = (GLsizei)stride; \
    packed_data->args.a4 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCompileShader
#define push_glCompileShader(shader) { \
    glCompileShader_PACKED *packed_data = malloc(sizeof(glCompileShader_PACKED)); \
    packed_data->format = glCompileShader_FORMAT; \
    packed_data->func = gl4es_glCompileShader; \
    packed_data->args.a1 = (GLuint)shader; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCompressedTexImage2D
#define push_glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data) { \
    glCompressedTexImage2D_PACKED *packed_data = malloc(sizeof(glCompressedTexImage2D_PACKED)); \
    packed_data->format = glCompressedTexImage2D_FORMAT; \
    packed_data->func = gl4es_glCompressedTexImage2D; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLint)level; \
    packed_data->args.a3 = (GLenum)internalformat; \
    packed_data->args.a4 = (GLsizei)width; \
    packed_data->args.a5 = (GLsizei)height; \
    packed_data->args.a6 = (GLint)border; \
    packed_data->args.a7 = (GLsizei)imageSize; \
    packed_data->args.a8 = (GLvoid *)data; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCompressedTexSubImage2D
#define push_glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data) { \
    glCompressedTexSubImage2D_PACKED *packed_data = malloc(sizeof(glCompressedTexSubImage2D_PACKED)); \
    packed_data->format = glCompressedTexSubImage2D_FORMAT; \
    packed_data->func = gl4es_glCompressedTexSubImage2D; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLint)level; \
    packed_data->args.a3 = (GLint)xoffset; \
    packed_data->args.a4 = (GLint)yoffset; \
    packed_data->args.a5 = (GLsizei)width; \
    packed_data->args.a6 = (GLsizei)height; \
    packed_data->args.a7 = (GLenum)format; \
    packed_data->args.a8 = (GLsizei)imageSize; \
    packed_data->args.a9 = (GLvoid *)data; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCopyTexImage2D
#define push_glCopyTexImage2D(target, level, internalformat, x, y, width, height, border) { \
    glCopyTexImage2D_PACKED *packed_data = malloc(sizeof(glCopyTexImage2D_PACKED)); \
    packed_data->format = glCopyTexImage2D_FORMAT; \
    packed_data->func = gl4es_glCopyTexImage2D; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLint)level; \
    packed_data->args.a3 = (GLenum)internalformat; \
    packed_data->args.a4 = (GLint)x; \
    packed_data->args.a5 = (GLint)y; \
    packed_data->args.a6 = (GLsizei)width; \
    packed_data->args.a7 = (GLsizei)height; \
    packed_data->args.a8 = (GLint)border; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCopyTexSubImage2D
#define push_glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height) { \
    glCopyTexSubImage2D_PACKED *packed_data = malloc(sizeof(glCopyTexSubImage2D_PACKED)); \
    packed_data->format = glCopyTexSubImage2D_FORMAT; \
    packed_data->func = gl4es_glCopyTexSubImage2D; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLint)level; \
    packed_data->args.a3 = (GLint)xoffset; \
    packed_data->args.a4 = (GLint)yoffset; \
    packed_data->args.a5 = (GLint)x; \
    packed_data->args.a6 = (GLint)y; \
    packed_data->args.a7 = (GLsizei)width; \
    packed_data->args.a8 = (GLsizei)height; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCreateProgram
#define push_glCreateProgram() { \
    glCreateProgram_PACKED *packed_data = malloc(sizeof(glCreateProgram_PACKED)); \
    packed_data->format = glCreateProgram_FORMAT; \
    packed_data->func = gl4es_glCreateProgram; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCreateShader
#define push_glCreateShader(type) { \
    glCreateShader_PACKED *packed_data = malloc(sizeof(glCreateShader_PACKED)); \
    packed_data->format = glCreateShader_FORMAT; \
    packed_data->func = gl4es_glCreateShader; \
    packed_data->args.a1 = (GLenum)type; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glCullFace
#define push_glCullFace(mode) { \
    glCullFace_PACKED *packed_data = malloc(sizeof(glCullFace_PACKED)); \
    packed_data->format = glCullFace_FORMAT; \
    packed_data->func = gl4es_glCullFace; \
    packed_data->args.a1 = (GLenum)mode; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDeleteBuffers
#define push_glDeleteBuffers(n, buffer) { \
    glDeleteBuffers_PACKED *packed_data = malloc(sizeof(glDeleteBuffers_PACKED)); \
    packed_data->format = glDeleteBuffers_FORMAT; \
    packed_data->func = gl4es_glDeleteBuffers; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)buffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDeleteFramebuffers
#define push_glDeleteFramebuffers(n, framebuffers) { \
    glDeleteFramebuffers_PACKED *packed_data = malloc(sizeof(glDeleteFramebuffers_PACKED)); \
    packed_data->format = glDeleteFramebuffers_FORMAT; \
    packed_data->func = gl4es_glDeleteFramebuffers; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)framebuffers; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDeleteProgram
#define push_glDeleteProgram(program) { \
    glDeleteProgram_PACKED *packed_data = malloc(sizeof(glDeleteProgram_PACKED)); \
    packed_data->format = glDeleteProgram_FORMAT; \
    packed_data->func = gl4es_glDeleteProgram; \
    packed_data->args.a1 = (GLuint)program; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDeleteRenderbuffers
#define push_glDeleteRenderbuffers(n, renderbuffers) { \
    glDeleteRenderbuffers_PACKED *packed_data = malloc(sizeof(glDeleteRenderbuffers_PACKED)); \
    packed_data->format = glDeleteRenderbuffers_FORMAT; \
    packed_data->func = gl4es_glDeleteRenderbuffers; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)renderbuffers; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDeleteShader
#define push_glDeleteShader(shader) { \
    glDeleteShader_PACKED *packed_data = malloc(sizeof(glDeleteShader_PACKED)); \
    packed_data->format = glDeleteShader_FORMAT; \
    packed_data->func = gl4es_glDeleteShader; \
    packed_data->args.a1 = (GLuint)shader; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDeleteTextures
#define push_glDeleteTextures(n, textures) { \
    glDeleteTextures_PACKED *packed_data = malloc(sizeof(glDeleteTextures_PACKED)); \
    packed_data->format = glDeleteTextures_FORMAT; \
    packed_data->func = gl4es_glDeleteTextures; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)textures; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDepthFunc
#define push_glDepthFunc(func) { \
    glDepthFunc_PACKED *packed_data = malloc(sizeof(glDepthFunc_PACKED)); \
    packed_data->format = glDepthFunc_FORMAT; \
    packed_data->func = gl4es_glDepthFunc; \
    packed_data->args.a1 = (GLenum)func; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDepthMask
#define push_glDepthMask(flag) { \
    glDepthMask_PACKED *packed_data = malloc(sizeof(glDepthMask_PACKED)); \
    packed_data->format = glDepthMask_FORMAT; \
    packed_data->func = gl4es_glDepthMask; \
    packed_data->args.a1 = (GLboolean)flag; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDepthRangef
#define push_glDepthRangef(Near, Far) { \
    glDepthRangef_PACKED *packed_data = malloc(sizeof(glDepthRangef_PACKED)); \
    packed_data->format = glDepthRangef_FORMAT; \
    packed_data->func = gl4es_glDepthRangef; \
    packed_data->args.a1 = (GLclampf)Near; \
    packed_data->args.a2 = (GLclampf)Far; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDepthRangex
#define push_glDepthRangex(Near, Far) { \
    glDepthRangex_PACKED *packed_data = malloc(sizeof(glDepthRangex_PACKED)); \
    packed_data->format = glDepthRangex_FORMAT; \
    packed_data->func = gl4es_glDepthRangex; \
    packed_data->args.a1 = (GLclampx)Near; \
    packed_data->args.a2 = (GLclampx)Far; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDetachShader
#define push_glDetachShader(program, shader) { \
    glDetachShader_PACKED *packed_data = malloc(sizeof(glDetachShader_PACKED)); \
    packed_data->format = glDetachShader_FORMAT; \
    packed_data->func = gl4es_glDetachShader; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLuint)shader; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDisable
#define push_glDisable(cap) { \
    glDisable_PACKED *packed_data = malloc(sizeof(glDisable_PACKED)); \
    packed_data->format = glDisable_FORMAT; \
    packed_data->func = gl4es_glDisable; \
    packed_data->args.a1 = (GLenum)cap; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDisableClientState
#define push_glDisableClientState(array) { \
    glDisableClientState_PACKED *packed_data = malloc(sizeof(glDisableClientState_PACKED)); \
    packed_data->format = glDisableClientState_FORMAT; \
    packed_data->func = gl4es_glDisableClientState; \
    packed_data->args.a1 = (GLenum)array; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDisableVertexAttribArray
#define push_glDisableVertexAttribArray(index) { \
    glDisableVertexAttribArray_PACKED *packed_data = malloc(sizeof(glDisableVertexAttribArray_PACKED)); \
    packed_data->format = glDisableVertexAttribArray_FORMAT; \
    packed_data->func = gl4es_glDisableVertexAttribArray; \
    packed_data->args.a1 = (GLuint)index; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDrawArrays
#define push_glDrawArrays(mode, first, count) { \
    glDrawArrays_PACKED *packed_data = malloc(sizeof(glDrawArrays_PACKED)); \
    packed_data->format = glDrawArrays_FORMAT; \
    packed_data->func = gl4es_glDrawArrays; \
    packed_data->args.a1 = (GLenum)mode; \
    packed_data->args.a2 = (GLint)first; \
    packed_data->args.a3 = (GLsizei)count; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDrawBuffers
#define push_glDrawBuffers(n, bufs) { \
    glDrawBuffers_PACKED *packed_data = malloc(sizeof(glDrawBuffers_PACKED)); \
    packed_data->format = glDrawBuffers_FORMAT; \
    packed_data->func = gl4es_glDrawBuffers; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLenum *)bufs; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDrawElements
#define push_glDrawElements(mode, count, type, indices) { \
    glDrawElements_PACKED *packed_data = malloc(sizeof(glDrawElements_PACKED)); \
    packed_data->format = glDrawElements_FORMAT; \
    packed_data->func = gl4es_glDrawElements; \
    packed_data->args.a1 = (GLenum)mode; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLenum)type; \
    packed_data->args.a4 = (GLvoid *)indices; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDrawTexf
#define push_glDrawTexf(x, y, z, width, height) { \
    glDrawTexf_PACKED *packed_data = malloc(sizeof(glDrawTexf_PACKED)); \
    packed_data->format = glDrawTexf_FORMAT; \
    packed_data->func = gl4es_glDrawTexf; \
    packed_data->args.a1 = (GLfloat)x; \
    packed_data->args.a2 = (GLfloat)y; \
    packed_data->args.a3 = (GLfloat)z; \
    packed_data->args.a4 = (GLfloat)width; \
    packed_data->args.a5 = (GLfloat)height; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glDrawTexi
#define push_glDrawTexi(x, y, z, width, height) { \
    glDrawTexi_PACKED *packed_data = malloc(sizeof(glDrawTexi_PACKED)); \
    packed_data->format = glDrawTexi_FORMAT; \
    packed_data->func = gl4es_glDrawTexi; \
    packed_data->args.a1 = (GLint)x; \
    packed_data->args.a2 = (GLint)y; \
    packed_data->args.a3 = (GLint)z; \
    packed_data->args.a4 = (GLint)width; \
    packed_data->args.a5 = (GLint)height; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glEnable
#define push_glEnable(cap) { \
    glEnable_PACKED *packed_data = malloc(sizeof(glEnable_PACKED)); \
    packed_data->format = glEnable_FORMAT; \
    packed_data->func = gl4es_glEnable; \
    packed_data->args.a1 = (GLenum)cap; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glEnableClientState
#define push_glEnableClientState(array) { \
    glEnableClientState_PACKED *packed_data = malloc(sizeof(glEnableClientState_PACKED)); \
    packed_data->format = glEnableClientState_FORMAT; \
    packed_data->func = gl4es_glEnableClientState; \
    packed_data->args.a1 = (GLenum)array; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glEnableVertexAttribArray
#define push_glEnableVertexAttribArray(index) { \
    glEnableVertexAttribArray_PACKED *packed_data = malloc(sizeof(glEnableVertexAttribArray_PACKED)); \
    packed_data->format = glEnableVertexAttribArray_FORMAT; \
    packed_data->func = gl4es_glEnableVertexAttribArray; \
    packed_data->args.a1 = (GLuint)index; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFinish
#define push_glFinish() { \
    glFinish_PACKED *packed_data = malloc(sizeof(glFinish_PACKED)); \
    packed_data->format = glFinish_FORMAT; \
    packed_data->func = gl4es_glFinish; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFlush
#define push_glFlush() { \
    glFlush_PACKED *packed_data = malloc(sizeof(glFlush_PACKED)); \
    packed_data->format = glFlush_FORMAT; \
    packed_data->func = gl4es_glFlush; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFogCoordPointer
#define push_glFogCoordPointer(type, stride, pointer) { \
    glFogCoordPointer_PACKED *packed_data = malloc(sizeof(glFogCoordPointer_PACKED)); \
    packed_data->format = glFogCoordPointer_FORMAT; \
    packed_data->func = gl4es_glFogCoordPointer; \
    packed_data->args.a1 = (GLenum)type; \
    packed_data->args.a2 = (GLsizei)stride; \
    packed_data->args.a3 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFogCoordf
#define push_glFogCoordf(coord) { \
    glFogCoordf_PACKED *packed_data = malloc(sizeof(glFogCoordf_PACKED)); \
    packed_data->format = glFogCoordf_FORMAT; \
    packed_data->func = gl4es_glFogCoordf; \
    packed_data->args.a1 = (GLfloat)coord; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFogCoordfv
#define push_glFogCoordfv(coord) { \
    glFogCoordfv_PACKED *packed_data = malloc(sizeof(glFogCoordfv_PACKED)); \
    packed_data->format = glFogCoordfv_FORMAT; \
    packed_data->func = gl4es_glFogCoordfv; \
    packed_data->args.a1 = (GLfloat *)coord; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFogf
#define push_glFogf(pname, param) { \
    glFogf_PACKED *packed_data = malloc(sizeof(glFogf_PACKED)); \
    packed_data->format = glFogf_FORMAT; \
    packed_data->func = gl4es_glFogf; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfloat)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFogfv
#define push_glFogfv(pname, params) { \
    glFogfv_PACKED *packed_data = malloc(sizeof(glFogfv_PACKED)); \
    packed_data->format = glFogfv_FORMAT; \
    packed_data->func = gl4es_glFogfv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFogx
#define push_glFogx(pname, param) { \
    glFogx_PACKED *packed_data = malloc(sizeof(glFogx_PACKED)); \
    packed_data->format = glFogx_FORMAT; \
    packed_data->func = gl4es_glFogx; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfixed)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFogxv
#define push_glFogxv(pname, params) { \
    glFogxv_PACKED *packed_data = malloc(sizeof(glFogxv_PACKED)); \
    packed_data->format = glFogxv_FORMAT; \
    packed_data->func = gl4es_glFogxv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFramebufferRenderbuffer
#define push_glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer) { \
    glFramebufferRenderbuffer_PACKED *packed_data = malloc(sizeof(glFramebufferRenderbuffer_PACKED)); \
    packed_data->format = glFramebufferRenderbuffer_FORMAT; \
    packed_data->func = gl4es_glFramebufferRenderbuffer; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)attachment; \
    packed_data->args.a3 = (GLenum)renderbuffertarget; \
    packed_data->args.a4 = (GLuint)renderbuffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFramebufferTexture2D
#define push_glFramebufferTexture2D(target, attachment, textarget, texture, level) { \
    glFramebufferTexture2D_PACKED *packed_data = malloc(sizeof(glFramebufferTexture2D_PACKED)); \
    packed_data->format = glFramebufferTexture2D_FORMAT; \
    packed_data->func = gl4es_glFramebufferTexture2D; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)attachment; \
    packed_data->args.a3 = (GLenum)textarget; \
    packed_data->args.a4 = (GLuint)texture; \
    packed_data->args.a5 = (GLint)level; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFrontFace
#define push_glFrontFace(mode) { \
    glFrontFace_PACKED *packed_data = malloc(sizeof(glFrontFace_PACKED)); \
    packed_data->format = glFrontFace_FORMAT; \
    packed_data->func = gl4es_glFrontFace; \
    packed_data->args.a1 = (GLenum)mode; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFrustumf
#define push_glFrustumf(left, right, bottom, top, Near, Far) { \
    glFrustumf_PACKED *packed_data = malloc(sizeof(glFrustumf_PACKED)); \
    packed_data->format = glFrustumf_FORMAT; \
    packed_data->func = gl4es_glFrustumf; \
    packed_data->args.a1 = (GLfloat)left; \
    packed_data->args.a2 = (GLfloat)right; \
    packed_data->args.a3 = (GLfloat)bottom; \
    packed_data->args.a4 = (GLfloat)top; \
    packed_data->args.a5 = (GLfloat)Near; \
    packed_data->args.a6 = (GLfloat)Far; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glFrustumx
#define push_glFrustumx(left, right, bottom, top, Near, Far) { \
    glFrustumx_PACKED *packed_data = malloc(sizeof(glFrustumx_PACKED)); \
    packed_data->format = glFrustumx_FORMAT; \
    packed_data->func = gl4es_glFrustumx; \
    packed_data->args.a1 = (GLfixed)left; \
    packed_data->args.a2 = (GLfixed)right; \
    packed_data->args.a3 = (GLfixed)bottom; \
    packed_data->args.a4 = (GLfixed)top; \
    packed_data->args.a5 = (GLfixed)Near; \
    packed_data->args.a6 = (GLfixed)Far; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGenBuffers
#define push_glGenBuffers(n, buffer) { \
    glGenBuffers_PACKED *packed_data = malloc(sizeof(glGenBuffers_PACKED)); \
    packed_data->format = glGenBuffers_FORMAT; \
    packed_data->func = gl4es_glGenBuffers; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)buffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGenFramebuffers
#define push_glGenFramebuffers(n, ids) { \
    glGenFramebuffers_PACKED *packed_data = malloc(sizeof(glGenFramebuffers_PACKED)); \
    packed_data->format = glGenFramebuffers_FORMAT; \
    packed_data->func = gl4es_glGenFramebuffers; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)ids; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGenRenderbuffers
#define push_glGenRenderbuffers(n, renderbuffers) { \
    glGenRenderbuffers_PACKED *packed_data = malloc(sizeof(glGenRenderbuffers_PACKED)); \
    packed_data->format = glGenRenderbuffers_FORMAT; \
    packed_data->func = gl4es_glGenRenderbuffers; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)renderbuffers; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGenTextures
#define push_glGenTextures(n, textures) { \
    glGenTextures_PACKED *packed_data = malloc(sizeof(glGenTextures_PACKED)); \
    packed_data->format = glGenTextures_FORMAT; \
    packed_data->func = gl4es_glGenTextures; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)textures; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGenerateMipmap
#define push_glGenerateMipmap(target) { \
    glGenerateMipmap_PACKED *packed_data = malloc(sizeof(glGenerateMipmap_PACKED)); \
    packed_data->format = glGenerateMipmap_FORMAT; \
    packed_data->func = gl4es_glGenerateMipmap; \
    packed_data->args.a1 = (GLenum)target; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetActiveAttrib
#define push_glGetActiveAttrib(program, index, bufSize, length, size, type, name) { \
    glGetActiveAttrib_PACKED *packed_data = malloc(sizeof(glGetActiveAttrib_PACKED)); \
    packed_data->format = glGetActiveAttrib_FORMAT; \
    packed_data->func = gl4es_glGetActiveAttrib; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLuint)index; \
    packed_data->args.a3 = (GLsizei)bufSize; \
    packed_data->args.a4 = (GLsizei *)length; \
    packed_data->args.a5 = (GLint *)size; \
    packed_data->args.a6 = (GLenum *)type; \
    packed_data->args.a7 = (GLchar *)name; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetActiveUniform
#define push_glGetActiveUniform(program, index, bufSize, length, size, type, name) { \
    glGetActiveUniform_PACKED *packed_data = malloc(sizeof(glGetActiveUniform_PACKED)); \
    packed_data->format = glGetActiveUniform_FORMAT; \
    packed_data->func = gl4es_glGetActiveUniform; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLuint)index; \
    packed_data->args.a3 = (GLsizei)bufSize; \
    packed_data->args.a4 = (GLsizei *)length; \
    packed_data->args.a5 = (GLint *)size; \
    packed_data->args.a6 = (GLenum *)type; \
    packed_data->args.a7 = (GLchar *)name; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetAttachedShaders
#define push_glGetAttachedShaders(program, maxCount, count, obj) { \
    glGetAttachedShaders_PACKED *packed_data = malloc(sizeof(glGetAttachedShaders_PACKED)); \
    packed_data->format = glGetAttachedShaders_FORMAT; \
    packed_data->func = gl4es_glGetAttachedShaders; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLsizei)maxCount; \
    packed_data->args.a3 = (GLsizei *)count; \
    packed_data->args.a4 = (GLuint *)obj; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetAttribLocation
#define push_glGetAttribLocation(program, name) { \
    glGetAttribLocation_PACKED *packed_data = malloc(sizeof(glGetAttribLocation_PACKED)); \
    packed_data->format = glGetAttribLocation_FORMAT; \
    packed_data->func = gl4es_glGetAttribLocation; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLchar *)name; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetBooleanv
#define push_glGetBooleanv(pname, params) { \
    glGetBooleanv_PACKED *packed_data = malloc(sizeof(glGetBooleanv_PACKED)); \
    packed_data->format = glGetBooleanv_FORMAT; \
    packed_data->func = gl4es_glGetBooleanv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLboolean *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetBufferParameteriv
#define push_glGetBufferParameteriv(target, pname, params) { \
    glGetBufferParameteriv_PACKED *packed_data = malloc(sizeof(glGetBufferParameteriv_PACKED)); \
    packed_data->format = glGetBufferParameteriv_FORMAT; \
    packed_data->func = gl4es_glGetBufferParameteriv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetClipPlanef
#define push_glGetClipPlanef(plane, equation) { \
    glGetClipPlanef_PACKED *packed_data = malloc(sizeof(glGetClipPlanef_PACKED)); \
    packed_data->format = glGetClipPlanef_FORMAT; \
    packed_data->func = gl4es_glGetClipPlanef; \
    packed_data->args.a1 = (GLenum)plane; \
    packed_data->args.a2 = (GLfloat *)equation; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetClipPlanex
#define push_glGetClipPlanex(plane, equation) { \
    glGetClipPlanex_PACKED *packed_data = malloc(sizeof(glGetClipPlanex_PACKED)); \
    packed_data->format = glGetClipPlanex_FORMAT; \
    packed_data->func = gl4es_glGetClipPlanex; \
    packed_data->args.a1 = (GLenum)plane; \
    packed_data->args.a2 = (GLfixed *)equation; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetError
#define push_glGetError() { \
    glGetError_PACKED *packed_data = malloc(sizeof(glGetError_PACKED)); \
    packed_data->format = glGetError_FORMAT; \
    packed_data->func = gl4es_glGetError; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetFixedv
#define push_glGetFixedv(pname, params) { \
    glGetFixedv_PACKED *packed_data = malloc(sizeof(glGetFixedv_PACKED)); \
    packed_data->format = glGetFixedv_FORMAT; \
    packed_data->func = gl4es_glGetFixedv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetFloatv
#define push_glGetFloatv(pname, params) { \
    glGetFloatv_PACKED *packed_data = malloc(sizeof(glGetFloatv_PACKED)); \
    packed_data->format = glGetFloatv_FORMAT; \
    packed_data->func = gl4es_glGetFloatv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetFramebufferAttachmentParameteriv
#define push_glGetFramebufferAttachmentParameteriv(target, attachment, pname, params) { \
    glGetFramebufferAttachmentParameteriv_PACKED *packed_data = malloc(sizeof(glGetFramebufferAttachmentParameteriv_PACKED)); \
    packed_data->format = glGetFramebufferAttachmentParameteriv_FORMAT; \
    packed_data->func = gl4es_glGetFramebufferAttachmentParameteriv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)attachment; \
    packed_data->args.a3 = (GLenum)pname; \
    packed_data->args.a4 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetIntegerv
#define push_glGetIntegerv(pname, params) { \
    glGetIntegerv_PACKED *packed_data = malloc(sizeof(glGetIntegerv_PACKED)); \
    packed_data->format = glGetIntegerv_FORMAT; \
    packed_data->func = gl4es_glGetIntegerv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetLightfv
#define push_glGetLightfv(light, pname, params) { \
    glGetLightfv_PACKED *packed_data = malloc(sizeof(glGetLightfv_PACKED)); \
    packed_data->format = glGetLightfv_FORMAT; \
    packed_data->func = gl4es_glGetLightfv; \
    packed_data->args.a1 = (GLenum)light; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetLightxv
#define push_glGetLightxv(light, pname, params) { \
    glGetLightxv_PACKED *packed_data = malloc(sizeof(glGetLightxv_PACKED)); \
    packed_data->format = glGetLightxv_FORMAT; \
    packed_data->func = gl4es_glGetLightxv; \
    packed_data->args.a1 = (GLenum)light; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetMaterialfv
#define push_glGetMaterialfv(face, pname, params) { \
    glGetMaterialfv_PACKED *packed_data = malloc(sizeof(glGetMaterialfv_PACKED)); \
    packed_data->format = glGetMaterialfv_FORMAT; \
    packed_data->func = gl4es_glGetMaterialfv; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetMaterialxv
#define push_glGetMaterialxv(face, pname, params) { \
    glGetMaterialxv_PACKED *packed_data = malloc(sizeof(glGetMaterialxv_PACKED)); \
    packed_data->format = glGetMaterialxv_FORMAT; \
    packed_data->func = gl4es_glGetMaterialxv; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetPointerv
#define push_glGetPointerv(pname, params) { \
    glGetPointerv_PACKED *packed_data = malloc(sizeof(glGetPointerv_PACKED)); \
    packed_data->format = glGetPointerv_FORMAT; \
    packed_data->func = gl4es_glGetPointerv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLvoid **)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetProgramBinary
#define push_glGetProgramBinary(program, bufSize, length, binaryFormat, binary) { \
    glGetProgramBinary_PACKED *packed_data = malloc(sizeof(glGetProgramBinary_PACKED)); \
    packed_data->format = glGetProgramBinary_FORMAT; \
    packed_data->func = gl4es_glGetProgramBinary; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLsizei)bufSize; \
    packed_data->args.a3 = (GLsizei *)length; \
    packed_data->args.a4 = (GLenum *)binaryFormat; \
    packed_data->args.a5 = (GLvoid *)binary; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetProgramInfoLog
#define push_glGetProgramInfoLog(program, bufSize, length, infoLog) { \
    glGetProgramInfoLog_PACKED *packed_data = malloc(sizeof(glGetProgramInfoLog_PACKED)); \
    packed_data->format = glGetProgramInfoLog_FORMAT; \
    packed_data->func = gl4es_glGetProgramInfoLog; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLsizei)bufSize; \
    packed_data->args.a3 = (GLsizei *)length; \
    packed_data->args.a4 = (GLchar *)infoLog; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetProgramiv
#define push_glGetProgramiv(program, pname, params) { \
    glGetProgramiv_PACKED *packed_data = malloc(sizeof(glGetProgramiv_PACKED)); \
    packed_data->format = glGetProgramiv_FORMAT; \
    packed_data->func = gl4es_glGetProgramiv; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetRenderbufferParameteriv
#define push_glGetRenderbufferParameteriv(target, pname, params) { \
    glGetRenderbufferParameteriv_PACKED *packed_data = malloc(sizeof(glGetRenderbufferParameteriv_PACKED)); \
    packed_data->format = glGetRenderbufferParameteriv_FORMAT; \
    packed_data->func = gl4es_glGetRenderbufferParameteriv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetShaderInfoLog
#define push_glGetShaderInfoLog(shader, bufSize, length, infoLog) { \
    glGetShaderInfoLog_PACKED *packed_data = malloc(sizeof(glGetShaderInfoLog_PACKED)); \
    packed_data->format = glGetShaderInfoLog_FORMAT; \
    packed_data->func = gl4es_glGetShaderInfoLog; \
    packed_data->args.a1 = (GLuint)shader; \
    packed_data->args.a2 = (GLsizei)bufSize; \
    packed_data->args.a3 = (GLsizei *)length; \
    packed_data->args.a4 = (GLchar *)infoLog; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetShaderPrecisionFormat
#define push_glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision) { \
    glGetShaderPrecisionFormat_PACKED *packed_data = malloc(sizeof(glGetShaderPrecisionFormat_PACKED)); \
    packed_data->format = glGetShaderPrecisionFormat_FORMAT; \
    packed_data->func = gl4es_glGetShaderPrecisionFormat; \
    packed_data->args.a1 = (GLenum)shadertype; \
    packed_data->args.a2 = (GLenum)precisiontype; \
    packed_data->args.a3 = (GLint *)range; \
    packed_data->args.a4 = (GLint *)precision; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetShaderSource
#define push_glGetShaderSource(shader, bufSize, length, source) { \
    glGetShaderSource_PACKED *packed_data = malloc(sizeof(glGetShaderSource_PACKED)); \
    packed_data->format = glGetShaderSource_FORMAT; \
    packed_data->func = gl4es_glGetShaderSource; \
    packed_data->args.a1 = (GLuint)shader; \
    packed_data->args.a2 = (GLsizei)bufSize; \
    packed_data->args.a3 = (GLsizei *)length; \
    packed_data->args.a4 = (GLchar *)source; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetShaderiv
#define push_glGetShaderiv(shader, pname, params) { \
    glGetShaderiv_PACKED *packed_data = malloc(sizeof(glGetShaderiv_PACKED)); \
    packed_data->format = glGetShaderiv_FORMAT; \
    packed_data->func = gl4es_glGetShaderiv; \
    packed_data->args.a1 = (GLuint)shader; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetString
#define push_glGetString(name) { \
    glGetString_PACKED *packed_data = malloc(sizeof(glGetString_PACKED)); \
    packed_data->format = glGetString_FORMAT; \
    packed_data->func = gl4es_glGetString; \
    packed_data->args.a1 = (GLenum)name; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetTexEnvfv
#define push_glGetTexEnvfv(target, pname, params) { \
    glGetTexEnvfv_PACKED *packed_data = malloc(sizeof(glGetTexEnvfv_PACKED)); \
    packed_data->format = glGetTexEnvfv_FORMAT; \
    packed_data->func = gl4es_glGetTexEnvfv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetTexEnviv
#define push_glGetTexEnviv(target, pname, params) { \
    glGetTexEnviv_PACKED *packed_data = malloc(sizeof(glGetTexEnviv_PACKED)); \
    packed_data->format = glGetTexEnviv_FORMAT; \
    packed_data->func = gl4es_glGetTexEnviv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetTexEnvxv
#define push_glGetTexEnvxv(target, pname, params) { \
    glGetTexEnvxv_PACKED *packed_data = malloc(sizeof(glGetTexEnvxv_PACKED)); \
    packed_data->format = glGetTexEnvxv_FORMAT; \
    packed_data->func = gl4es_glGetTexEnvxv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetTexParameterfv
#define push_glGetTexParameterfv(target, pname, params) { \
    glGetTexParameterfv_PACKED *packed_data = malloc(sizeof(glGetTexParameterfv_PACKED)); \
    packed_data->format = glGetTexParameterfv_FORMAT; \
    packed_data->func = gl4es_glGetTexParameterfv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetTexParameteriv
#define push_glGetTexParameteriv(target, pname, params) { \
    glGetTexParameteriv_PACKED *packed_data = malloc(sizeof(glGetTexParameteriv_PACKED)); \
    packed_data->format = glGetTexParameteriv_FORMAT; \
    packed_data->func = gl4es_glGetTexParameteriv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetTexParameterxv
#define push_glGetTexParameterxv(target, pname, params) { \
    glGetTexParameterxv_PACKED *packed_data = malloc(sizeof(glGetTexParameterxv_PACKED)); \
    packed_data->format = glGetTexParameterxv_FORMAT; \
    packed_data->func = gl4es_glGetTexParameterxv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetUniformLocation
#define push_glGetUniformLocation(program, name) { \
    glGetUniformLocation_PACKED *packed_data = malloc(sizeof(glGetUniformLocation_PACKED)); \
    packed_data->format = glGetUniformLocation_FORMAT; \
    packed_data->func = gl4es_glGetUniformLocation; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLchar *)name; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetUniformfv
#define push_glGetUniformfv(program, location, params) { \
    glGetUniformfv_PACKED *packed_data = malloc(sizeof(glGetUniformfv_PACKED)); \
    packed_data->format = glGetUniformfv_FORMAT; \
    packed_data->func = gl4es_glGetUniformfv; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLint)location; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetUniformiv
#define push_glGetUniformiv(program, location, params) { \
    glGetUniformiv_PACKED *packed_data = malloc(sizeof(glGetUniformiv_PACKED)); \
    packed_data->format = glGetUniformiv_FORMAT; \
    packed_data->func = gl4es_glGetUniformiv; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLint)location; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetVertexAttribPointerv
#define push_glGetVertexAttribPointerv(index, pname, pointer) { \
    glGetVertexAttribPointerv_PACKED *packed_data = malloc(sizeof(glGetVertexAttribPointerv_PACKED)); \
    packed_data->format = glGetVertexAttribPointerv_FORMAT; \
    packed_data->func = gl4es_glGetVertexAttribPointerv; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLvoid **)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetVertexAttribfv
#define push_glGetVertexAttribfv(index, pname, params) { \
    glGetVertexAttribfv_PACKED *packed_data = malloc(sizeof(glGetVertexAttribfv_PACKED)); \
    packed_data->format = glGetVertexAttribfv_FORMAT; \
    packed_data->func = gl4es_glGetVertexAttribfv; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glGetVertexAttribiv
#define push_glGetVertexAttribiv(index, pname, params) { \
    glGetVertexAttribiv_PACKED *packed_data = malloc(sizeof(glGetVertexAttribiv_PACKED)); \
    packed_data->format = glGetVertexAttribiv_FORMAT; \
    packed_data->func = gl4es_glGetVertexAttribiv; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glHint
#define push_glHint(target, mode) { \
    glHint_PACKED *packed_data = malloc(sizeof(glHint_PACKED)); \
    packed_data->format = glHint_FORMAT; \
    packed_data->func = gl4es_glHint; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)mode; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glIsBuffer
#define push_glIsBuffer(buffer) { \
    glIsBuffer_PACKED *packed_data = malloc(sizeof(glIsBuffer_PACKED)); \
    packed_data->format = glIsBuffer_FORMAT; \
    packed_data->func = gl4es_glIsBuffer; \
    packed_data->args.a1 = (GLuint)buffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glIsEnabled
#define push_glIsEnabled(cap) { \
    glIsEnabled_PACKED *packed_data = malloc(sizeof(glIsEnabled_PACKED)); \
    packed_data->format = glIsEnabled_FORMAT; \
    packed_data->func = gl4es_glIsEnabled; \
    packed_data->args.a1 = (GLenum)cap; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glIsFramebuffer
#define push_glIsFramebuffer(framebuffer) { \
    glIsFramebuffer_PACKED *packed_data = malloc(sizeof(glIsFramebuffer_PACKED)); \
    packed_data->format = glIsFramebuffer_FORMAT; \
    packed_data->func = gl4es_glIsFramebuffer; \
    packed_data->args.a1 = (GLuint)framebuffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glIsProgram
#define push_glIsProgram(program) { \
    glIsProgram_PACKED *packed_data = malloc(sizeof(glIsProgram_PACKED)); \
    packed_data->format = glIsProgram_FORMAT; \
    packed_data->func = gl4es_glIsProgram; \
    packed_data->args.a1 = (GLuint)program; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glIsRenderbuffer
#define push_glIsRenderbuffer(renderbuffer) { \
    glIsRenderbuffer_PACKED *packed_data = malloc(sizeof(glIsRenderbuffer_PACKED)); \
    packed_data->format = glIsRenderbuffer_FORMAT; \
    packed_data->func = gl4es_glIsRenderbuffer; \
    packed_data->args.a1 = (GLuint)renderbuffer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glIsShader
#define push_glIsShader(shader) { \
    glIsShader_PACKED *packed_data = malloc(sizeof(glIsShader_PACKED)); \
    packed_data->format = glIsShader_FORMAT; \
    packed_data->func = gl4es_glIsShader; \
    packed_data->args.a1 = (GLuint)shader; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glIsTexture
#define push_glIsTexture(texture) { \
    glIsTexture_PACKED *packed_data = malloc(sizeof(glIsTexture_PACKED)); \
    packed_data->format = glIsTexture_FORMAT; \
    packed_data->func = gl4es_glIsTexture; \
    packed_data->args.a1 = (GLuint)texture; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightModelf
#define push_glLightModelf(pname, param) { \
    glLightModelf_PACKED *packed_data = malloc(sizeof(glLightModelf_PACKED)); \
    packed_data->format = glLightModelf_FORMAT; \
    packed_data->func = gl4es_glLightModelf; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfloat)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightModelfv
#define push_glLightModelfv(pname, params) { \
    glLightModelfv_PACKED *packed_data = malloc(sizeof(glLightModelfv_PACKED)); \
    packed_data->format = glLightModelfv_FORMAT; \
    packed_data->func = gl4es_glLightModelfv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightModelx
#define push_glLightModelx(pname, param) { \
    glLightModelx_PACKED *packed_data = malloc(sizeof(glLightModelx_PACKED)); \
    packed_data->format = glLightModelx_FORMAT; \
    packed_data->func = gl4es_glLightModelx; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfixed)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightModelxv
#define push_glLightModelxv(pname, params) { \
    glLightModelxv_PACKED *packed_data = malloc(sizeof(glLightModelxv_PACKED)); \
    packed_data->format = glLightModelxv_FORMAT; \
    packed_data->func = gl4es_glLightModelxv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightf
#define push_glLightf(light, pname, param) { \
    glLightf_PACKED *packed_data = malloc(sizeof(glLightf_PACKED)); \
    packed_data->format = glLightf_FORMAT; \
    packed_data->func = gl4es_glLightf; \
    packed_data->args.a1 = (GLenum)light; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightfv
#define push_glLightfv(light, pname, params) { \
    glLightfv_PACKED *packed_data = malloc(sizeof(glLightfv_PACKED)); \
    packed_data->format = glLightfv_FORMAT; \
    packed_data->func = gl4es_glLightfv; \
    packed_data->args.a1 = (GLenum)light; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightx
#define push_glLightx(light, pname, param) { \
    glLightx_PACKED *packed_data = malloc(sizeof(glLightx_PACKED)); \
    packed_data->format = glLightx_FORMAT; \
    packed_data->func = gl4es_glLightx; \
    packed_data->args.a1 = (GLenum)light; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLightxv
#define push_glLightxv(light, pname, params) { \
    glLightxv_PACKED *packed_data = malloc(sizeof(glLightxv_PACKED)); \
    packed_data->format = glLightxv_FORMAT; \
    packed_data->func = gl4es_glLightxv; \
    packed_data->args.a1 = (GLenum)light; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLineWidth
#define push_glLineWidth(width) { \
    glLineWidth_PACKED *packed_data = malloc(sizeof(glLineWidth_PACKED)); \
    packed_data->format = glLineWidth_FORMAT; \
    packed_data->func = gl4es_glLineWidth; \
    packed_data->args.a1 = (GLfloat)width; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLineWidthx
#define push_glLineWidthx(width) { \
    glLineWidthx_PACKED *packed_data = malloc(sizeof(glLineWidthx_PACKED)); \
    packed_data->format = glLineWidthx_FORMAT; \
    packed_data->func = gl4es_glLineWidthx; \
    packed_data->args.a1 = (GLfixed)width; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLinkProgram
#define push_glLinkProgram(program) { \
    glLinkProgram_PACKED *packed_data = malloc(sizeof(glLinkProgram_PACKED)); \
    packed_data->format = glLinkProgram_FORMAT; \
    packed_data->func = gl4es_glLinkProgram; \
    packed_data->args.a1 = (GLuint)program; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLoadIdentity
#define push_glLoadIdentity() { \
    glLoadIdentity_PACKED *packed_data = malloc(sizeof(glLoadIdentity_PACKED)); \
    packed_data->format = glLoadIdentity_FORMAT; \
    packed_data->func = gl4es_glLoadIdentity; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLoadMatrixf
#define push_glLoadMatrixf(m) { \
    glLoadMatrixf_PACKED *packed_data = malloc(sizeof(glLoadMatrixf_PACKED)); \
    packed_data->format = glLoadMatrixf_FORMAT; \
    packed_data->func = gl4es_glLoadMatrixf; \
    packed_data->args.a1 = (GLfloat *)m; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLoadMatrixx
#define push_glLoadMatrixx(m) { \
    glLoadMatrixx_PACKED *packed_data = malloc(sizeof(glLoadMatrixx_PACKED)); \
    packed_data->format = glLoadMatrixx_FORMAT; \
    packed_data->func = gl4es_glLoadMatrixx; \
    packed_data->args.a1 = (GLfixed *)m; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glLogicOp
#define push_glLogicOp(opcode) { \
    glLogicOp_PACKED *packed_data = malloc(sizeof(glLogicOp_PACKED)); \
    packed_data->format = glLogicOp_FORMAT; \
    packed_data->func = gl4es_glLogicOp; \
    packed_data->args.a1 = (GLenum)opcode; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMaterialf
#define push_glMaterialf(face, pname, param) { \
    glMaterialf_PACKED *packed_data = malloc(sizeof(glMaterialf_PACKED)); \
    packed_data->format = glMaterialf_FORMAT; \
    packed_data->func = gl4es_glMaterialf; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMaterialfv
#define push_glMaterialfv(face, pname, params) { \
    glMaterialfv_PACKED *packed_data = malloc(sizeof(glMaterialfv_PACKED)); \
    packed_data->format = glMaterialfv_FORMAT; \
    packed_data->func = gl4es_glMaterialfv; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMaterialx
#define push_glMaterialx(face, pname, param) { \
    glMaterialx_PACKED *packed_data = malloc(sizeof(glMaterialx_PACKED)); \
    packed_data->format = glMaterialx_FORMAT; \
    packed_data->func = gl4es_glMaterialx; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMaterialxv
#define push_glMaterialxv(face, pname, params) { \
    glMaterialxv_PACKED *packed_data = malloc(sizeof(glMaterialxv_PACKED)); \
    packed_data->format = glMaterialxv_FORMAT; \
    packed_data->func = gl4es_glMaterialxv; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMatrixMode
#define push_glMatrixMode(mode) { \
    glMatrixMode_PACKED *packed_data = malloc(sizeof(glMatrixMode_PACKED)); \
    packed_data->format = glMatrixMode_FORMAT; \
    packed_data->func = gl4es_glMatrixMode; \
    packed_data->args.a1 = (GLenum)mode; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMultMatrixf
#define push_glMultMatrixf(m) { \
    glMultMatrixf_PACKED *packed_data = malloc(sizeof(glMultMatrixf_PACKED)); \
    packed_data->format = glMultMatrixf_FORMAT; \
    packed_data->func = gl4es_glMultMatrixf; \
    packed_data->args.a1 = (GLfloat *)m; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMultMatrixx
#define push_glMultMatrixx(m) { \
    glMultMatrixx_PACKED *packed_data = malloc(sizeof(glMultMatrixx_PACKED)); \
    packed_data->format = glMultMatrixx_FORMAT; \
    packed_data->func = gl4es_glMultMatrixx; \
    packed_data->args.a1 = (GLfixed *)m; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMultiDrawArrays
#define push_glMultiDrawArrays(mode, first, count, primcount) { \
    glMultiDrawArrays_PACKED *packed_data = malloc(sizeof(glMultiDrawArrays_PACKED)); \
    packed_data->format = glMultiDrawArrays_FORMAT; \
    packed_data->func = gl4es_glMultiDrawArrays; \
    packed_data->args.a1 = (GLenum)mode; \
    packed_data->args.a2 = (GLint *)first; \
    packed_data->args.a3 = (GLsizei *)count; \
    packed_data->args.a4 = (GLsizei)primcount; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMultiDrawElements
#define push_glMultiDrawElements(mode, count, type, indices, primcount) { \
    glMultiDrawElements_PACKED *packed_data = malloc(sizeof(glMultiDrawElements_PACKED)); \
    packed_data->format = glMultiDrawElements_FORMAT; \
    packed_data->func = gl4es_glMultiDrawElements; \
    packed_data->args.a1 = (GLenum)mode; \
    packed_data->args.a2 = (GLsizei *)count; \
    packed_data->args.a3 = (GLenum)type; \
    packed_data->args.a4 = (void * *)indices; \
    packed_data->args.a5 = (GLsizei)primcount; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMultiTexCoord4f
#define push_glMultiTexCoord4f(target, s, t, r, q) { \
    glMultiTexCoord4f_PACKED *packed_data = malloc(sizeof(glMultiTexCoord4f_PACKED)); \
    packed_data->format = glMultiTexCoord4f_FORMAT; \
    packed_data->func = gl4es_glMultiTexCoord4f; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLfloat)s; \
    packed_data->args.a3 = (GLfloat)t; \
    packed_data->args.a4 = (GLfloat)r; \
    packed_data->args.a5 = (GLfloat)q; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glMultiTexCoord4x
#define push_glMultiTexCoord4x(target, s, t, r, q) { \
    glMultiTexCoord4x_PACKED *packed_data = malloc(sizeof(glMultiTexCoord4x_PACKED)); \
    packed_data->format = glMultiTexCoord4x_FORMAT; \
    packed_data->func = gl4es_glMultiTexCoord4x; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLfixed)s; \
    packed_data->args.a3 = (GLfixed)t; \
    packed_data->args.a4 = (GLfixed)r; \
    packed_data->args.a5 = (GLfixed)q; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glNormal3f
#define push_glNormal3f(nx, ny, nz) { \
    glNormal3f_PACKED *packed_data = malloc(sizeof(glNormal3f_PACKED)); \
    packed_data->format = glNormal3f_FORMAT; \
    packed_data->func = gl4es_glNormal3f; \
    packed_data->args.a1 = (GLfloat)nx; \
    packed_data->args.a2 = (GLfloat)ny; \
    packed_data->args.a3 = (GLfloat)nz; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glNormal3x
#define push_glNormal3x(nx, ny, nz) { \
    glNormal3x_PACKED *packed_data = malloc(sizeof(glNormal3x_PACKED)); \
    packed_data->format = glNormal3x_FORMAT; \
    packed_data->func = gl4es_glNormal3x; \
    packed_data->args.a1 = (GLfixed)nx; \
    packed_data->args.a2 = (GLfixed)ny; \
    packed_data->args.a3 = (GLfixed)nz; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glNormalPointer
#define push_glNormalPointer(type, stride, pointer) { \
    glNormalPointer_PACKED *packed_data = malloc(sizeof(glNormalPointer_PACKED)); \
    packed_data->format = glNormalPointer_FORMAT; \
    packed_data->func = gl4es_glNormalPointer; \
    packed_data->args.a1 = (GLenum)type; \
    packed_data->args.a2 = (GLsizei)stride; \
    packed_data->args.a3 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glOrthof
#define push_glOrthof(left, right, bottom, top, Near, Far) { \
    glOrthof_PACKED *packed_data = malloc(sizeof(glOrthof_PACKED)); \
    packed_data->format = glOrthof_FORMAT; \
    packed_data->func = gl4es_glOrthof; \
    packed_data->args.a1 = (GLfloat)left; \
    packed_data->args.a2 = (GLfloat)right; \
    packed_data->args.a3 = (GLfloat)bottom; \
    packed_data->args.a4 = (GLfloat)top; \
    packed_data->args.a5 = (GLfloat)Near; \
    packed_data->args.a6 = (GLfloat)Far; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glOrthox
#define push_glOrthox(left, right, bottom, top, Near, Far) { \
    glOrthox_PACKED *packed_data = malloc(sizeof(glOrthox_PACKED)); \
    packed_data->format = glOrthox_FORMAT; \
    packed_data->func = gl4es_glOrthox; \
    packed_data->args.a1 = (GLfixed)left; \
    packed_data->args.a2 = (GLfixed)right; \
    packed_data->args.a3 = (GLfixed)bottom; \
    packed_data->args.a4 = (GLfixed)top; \
    packed_data->args.a5 = (GLfixed)Near; \
    packed_data->args.a6 = (GLfixed)Far; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPixelStorei
#define push_glPixelStorei(pname, param) { \
    glPixelStorei_PACKED *packed_data = malloc(sizeof(glPixelStorei_PACKED)); \
    packed_data->format = glPixelStorei_FORMAT; \
    packed_data->func = gl4es_glPixelStorei; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLint)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPointParameterf
#define push_glPointParameterf(pname, param) { \
    glPointParameterf_PACKED *packed_data = malloc(sizeof(glPointParameterf_PACKED)); \
    packed_data->format = glPointParameterf_FORMAT; \
    packed_data->func = gl4es_glPointParameterf; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfloat)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPointParameterfv
#define push_glPointParameterfv(pname, params) { \
    glPointParameterfv_PACKED *packed_data = malloc(sizeof(glPointParameterfv_PACKED)); \
    packed_data->format = glPointParameterfv_FORMAT; \
    packed_data->func = gl4es_glPointParameterfv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPointParameterx
#define push_glPointParameterx(pname, param) { \
    glPointParameterx_PACKED *packed_data = malloc(sizeof(glPointParameterx_PACKED)); \
    packed_data->format = glPointParameterx_FORMAT; \
    packed_data->func = gl4es_glPointParameterx; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfixed)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPointParameterxv
#define push_glPointParameterxv(pname, params) { \
    glPointParameterxv_PACKED *packed_data = malloc(sizeof(glPointParameterxv_PACKED)); \
    packed_data->format = glPointParameterxv_FORMAT; \
    packed_data->func = gl4es_glPointParameterxv; \
    packed_data->args.a1 = (GLenum)pname; \
    packed_data->args.a2 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPointSize
#define push_glPointSize(size) { \
    glPointSize_PACKED *packed_data = malloc(sizeof(glPointSize_PACKED)); \
    packed_data->format = glPointSize_FORMAT; \
    packed_data->func = gl4es_glPointSize; \
    packed_data->args.a1 = (GLfloat)size; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPointSizePointerOES
#define push_glPointSizePointerOES(type, stride, pointer) { \
    glPointSizePointerOES_PACKED *packed_data = malloc(sizeof(glPointSizePointerOES_PACKED)); \
    packed_data->format = glPointSizePointerOES_FORMAT; \
    packed_data->func = gl4es_glPointSizePointerOES; \
    packed_data->args.a1 = (GLenum)type; \
    packed_data->args.a2 = (GLsizei)stride; \
    packed_data->args.a3 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPointSizex
#define push_glPointSizex(size) { \
    glPointSizex_PACKED *packed_data = malloc(sizeof(glPointSizex_PACKED)); \
    packed_data->format = glPointSizex_FORMAT; \
    packed_data->func = gl4es_glPointSizex; \
    packed_data->args.a1 = (GLfixed)size; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPolygonOffset
#define push_glPolygonOffset(factor, units) { \
    glPolygonOffset_PACKED *packed_data = malloc(sizeof(glPolygonOffset_PACKED)); \
    packed_data->format = glPolygonOffset_FORMAT; \
    packed_data->func = gl4es_glPolygonOffset; \
    packed_data->args.a1 = (GLfloat)factor; \
    packed_data->args.a2 = (GLfloat)units; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPolygonOffsetx
#define push_glPolygonOffsetx(factor, units) { \
    glPolygonOffsetx_PACKED *packed_data = malloc(sizeof(glPolygonOffsetx_PACKED)); \
    packed_data->format = glPolygonOffsetx_FORMAT; \
    packed_data->func = gl4es_glPolygonOffsetx; \
    packed_data->args.a1 = (GLfixed)factor; \
    packed_data->args.a2 = (GLfixed)units; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPopMatrix
#define push_glPopMatrix() { \
    glPopMatrix_PACKED *packed_data = malloc(sizeof(glPopMatrix_PACKED)); \
    packed_data->format = glPopMatrix_FORMAT; \
    packed_data->func = gl4es_glPopMatrix; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glProgramBinary
#define push_glProgramBinary(program, binaryFormat, binary, length) { \
    glProgramBinary_PACKED *packed_data = malloc(sizeof(glProgramBinary_PACKED)); \
    packed_data->format = glProgramBinary_FORMAT; \
    packed_data->func = gl4es_glProgramBinary; \
    packed_data->args.a1 = (GLuint)program; \
    packed_data->args.a2 = (GLenum)binaryFormat; \
    packed_data->args.a3 = (GLvoid *)binary; \
    packed_data->args.a4 = (GLint)length; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glPushMatrix
#define push_glPushMatrix() { \
    glPushMatrix_PACKED *packed_data = malloc(sizeof(glPushMatrix_PACKED)); \
    packed_data->format = glPushMatrix_FORMAT; \
    packed_data->func = gl4es_glPushMatrix; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glReadPixels
#define push_glReadPixels(x, y, width, height, format, type, pixels) { \
    glReadPixels_PACKED *packed_data = malloc(sizeof(glReadPixels_PACKED)); \
    packed_data->format = glReadPixels_FORMAT; \
    packed_data->func = gl4es_glReadPixels; \
    packed_data->args.a1 = (GLint)x; \
    packed_data->args.a2 = (GLint)y; \
    packed_data->args.a3 = (GLsizei)width; \
    packed_data->args.a4 = (GLsizei)height; \
    packed_data->args.a5 = (GLenum)format; \
    packed_data->args.a6 = (GLenum)type; \
    packed_data->args.a7 = (GLvoid *)pixels; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glReleaseShaderCompiler
#define push_glReleaseShaderCompiler() { \
    glReleaseShaderCompiler_PACKED *packed_data = malloc(sizeof(glReleaseShaderCompiler_PACKED)); \
    packed_data->format = glReleaseShaderCompiler_FORMAT; \
    packed_data->func = gl4es_glReleaseShaderCompiler; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glRenderbufferStorage
#define push_glRenderbufferStorage(target, internalformat, width, height) { \
    glRenderbufferStorage_PACKED *packed_data = malloc(sizeof(glRenderbufferStorage_PACKED)); \
    packed_data->format = glRenderbufferStorage_FORMAT; \
    packed_data->func = gl4es_glRenderbufferStorage; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)internalformat; \
    packed_data->args.a3 = (GLsizei)width; \
    packed_data->args.a4 = (GLsizei)height; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glRotatef
#define push_glRotatef(angle, x, y, z) { \
    glRotatef_PACKED *packed_data = malloc(sizeof(glRotatef_PACKED)); \
    packed_data->format = glRotatef_FORMAT; \
    packed_data->func = gl4es_glRotatef; \
    packed_data->args.a1 = (GLfloat)angle; \
    packed_data->args.a2 = (GLfloat)x; \
    packed_data->args.a3 = (GLfloat)y; \
    packed_data->args.a4 = (GLfloat)z; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glRotatex
#define push_glRotatex(angle, x, y, z) { \
    glRotatex_PACKED *packed_data = malloc(sizeof(glRotatex_PACKED)); \
    packed_data->format = glRotatex_FORMAT; \
    packed_data->func = gl4es_glRotatex; \
    packed_data->args.a1 = (GLfixed)angle; \
    packed_data->args.a2 = (GLfixed)x; \
    packed_data->args.a3 = (GLfixed)y; \
    packed_data->args.a4 = (GLfixed)z; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glSampleCoverage
#define push_glSampleCoverage(value, invert) { \
    glSampleCoverage_PACKED *packed_data = malloc(sizeof(glSampleCoverage_PACKED)); \
    packed_data->format = glSampleCoverage_FORMAT; \
    packed_data->func = gl4es_glSampleCoverage; \
    packed_data->args.a1 = (GLclampf)value; \
    packed_data->args.a2 = (GLboolean)invert; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glSampleCoveragex
#define push_glSampleCoveragex(value, invert) { \
    glSampleCoveragex_PACKED *packed_data = malloc(sizeof(glSampleCoveragex_PACKED)); \
    packed_data->format = glSampleCoveragex_FORMAT; \
    packed_data->func = gl4es_glSampleCoveragex; \
    packed_data->args.a1 = (GLclampx)value; \
    packed_data->args.a2 = (GLboolean)invert; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glScalef
#define push_glScalef(x, y, z) { \
    glScalef_PACKED *packed_data = malloc(sizeof(glScalef_PACKED)); \
    packed_data->format = glScalef_FORMAT; \
    packed_data->func = gl4es_glScalef; \
    packed_data->args.a1 = (GLfloat)x; \
    packed_data->args.a2 = (GLfloat)y; \
    packed_data->args.a3 = (GLfloat)z; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glScalex
#define push_glScalex(x, y, z) { \
    glScalex_PACKED *packed_data = malloc(sizeof(glScalex_PACKED)); \
    packed_data->format = glScalex_FORMAT; \
    packed_data->func = gl4es_glScalex; \
    packed_data->args.a1 = (GLfixed)x; \
    packed_data->args.a2 = (GLfixed)y; \
    packed_data->args.a3 = (GLfixed)z; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glScissor
#define push_glScissor(x, y, width, height) { \
    glScissor_PACKED *packed_data = malloc(sizeof(glScissor_PACKED)); \
    packed_data->format = glScissor_FORMAT; \
    packed_data->func = gl4es_glScissor; \
    packed_data->args.a1 = (GLint)x; \
    packed_data->args.a2 = (GLint)y; \
    packed_data->args.a3 = (GLsizei)width; \
    packed_data->args.a4 = (GLsizei)height; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glShadeModel
#define push_glShadeModel(mode) { \
    glShadeModel_PACKED *packed_data = malloc(sizeof(glShadeModel_PACKED)); \
    packed_data->format = glShadeModel_FORMAT; \
    packed_data->func = gl4es_glShadeModel; \
    packed_data->args.a1 = (GLenum)mode; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glShaderBinary
#define push_glShaderBinary(n, shaders, binaryformat, binary, length) { \
    glShaderBinary_PACKED *packed_data = malloc(sizeof(glShaderBinary_PACKED)); \
    packed_data->format = glShaderBinary_FORMAT; \
    packed_data->func = gl4es_glShaderBinary; \
    packed_data->args.a1 = (GLsizei)n; \
    packed_data->args.a2 = (GLuint *)shaders; \
    packed_data->args.a3 = (GLenum)binaryformat; \
    packed_data->args.a4 = (GLvoid *)binary; \
    packed_data->args.a5 = (GLsizei)length; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glShaderSource
#define push_glShaderSource(shader, count, string, length) { \
    glShaderSource_PACKED *packed_data = malloc(sizeof(glShaderSource_PACKED)); \
    packed_data->format = glShaderSource_FORMAT; \
    packed_data->func = gl4es_glShaderSource; \
    packed_data->args.a1 = (GLuint)shader; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLchar * *)string; \
    packed_data->args.a4 = (GLint *)length; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glStencilFunc
#define push_glStencilFunc(func, ref, mask) { \
    glStencilFunc_PACKED *packed_data = malloc(sizeof(glStencilFunc_PACKED)); \
    packed_data->format = glStencilFunc_FORMAT; \
    packed_data->func = gl4es_glStencilFunc; \
    packed_data->args.a1 = (GLenum)func; \
    packed_data->args.a2 = (GLint)ref; \
    packed_data->args.a3 = (GLuint)mask; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glStencilFuncSeparate
#define push_glStencilFuncSeparate(face, func, ref, mask) { \
    glStencilFuncSeparate_PACKED *packed_data = malloc(sizeof(glStencilFuncSeparate_PACKED)); \
    packed_data->format = glStencilFuncSeparate_FORMAT; \
    packed_data->func = gl4es_glStencilFuncSeparate; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)func; \
    packed_data->args.a3 = (GLint)ref; \
    packed_data->args.a4 = (GLuint)mask; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glStencilMask
#define push_glStencilMask(mask) { \
    glStencilMask_PACKED *packed_data = malloc(sizeof(glStencilMask_PACKED)); \
    packed_data->format = glStencilMask_FORMAT; \
    packed_data->func = gl4es_glStencilMask; \
    packed_data->args.a1 = (GLuint)mask; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glStencilMaskSeparate
#define push_glStencilMaskSeparate(face, mask) { \
    glStencilMaskSeparate_PACKED *packed_data = malloc(sizeof(glStencilMaskSeparate_PACKED)); \
    packed_data->format = glStencilMaskSeparate_FORMAT; \
    packed_data->func = gl4es_glStencilMaskSeparate; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLuint)mask; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glStencilOp
#define push_glStencilOp(fail, zfail, zpass) { \
    glStencilOp_PACKED *packed_data = malloc(sizeof(glStencilOp_PACKED)); \
    packed_data->format = glStencilOp_FORMAT; \
    packed_data->func = gl4es_glStencilOp; \
    packed_data->args.a1 = (GLenum)fail; \
    packed_data->args.a2 = (GLenum)zfail; \
    packed_data->args.a3 = (GLenum)zpass; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glStencilOpSeparate
#define push_glStencilOpSeparate(face, sfail, zfail, zpass) { \
    glStencilOpSeparate_PACKED *packed_data = malloc(sizeof(glStencilOpSeparate_PACKED)); \
    packed_data->format = glStencilOpSeparate_FORMAT; \
    packed_data->func = gl4es_glStencilOpSeparate; \
    packed_data->args.a1 = (GLenum)face; \
    packed_data->args.a2 = (GLenum)sfail; \
    packed_data->args.a3 = (GLenum)zfail; \
    packed_data->args.a4 = (GLenum)zpass; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexCoordPointer
#define push_glTexCoordPointer(size, type, stride, pointer) { \
    glTexCoordPointer_PACKED *packed_data = malloc(sizeof(glTexCoordPointer_PACKED)); \
    packed_data->format = glTexCoordPointer_FORMAT; \
    packed_data->func = gl4es_glTexCoordPointer; \
    packed_data->args.a1 = (GLint)size; \
    packed_data->args.a2 = (GLenum)type; \
    packed_data->args.a3 = (GLsizei)stride; \
    packed_data->args.a4 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexEnvf
#define push_glTexEnvf(target, pname, param) { \
    glTexEnvf_PACKED *packed_data = malloc(sizeof(glTexEnvf_PACKED)); \
    packed_data->format = glTexEnvf_FORMAT; \
    packed_data->func = gl4es_glTexEnvf; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexEnvfv
#define push_glTexEnvfv(target, pname, params) { \
    glTexEnvfv_PACKED *packed_data = malloc(sizeof(glTexEnvfv_PACKED)); \
    packed_data->format = glTexEnvfv_FORMAT; \
    packed_data->func = gl4es_glTexEnvfv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexEnvi
#define push_glTexEnvi(target, pname, param) { \
    glTexEnvi_PACKED *packed_data = malloc(sizeof(glTexEnvi_PACKED)); \
    packed_data->format = glTexEnvi_FORMAT; \
    packed_data->func = gl4es_glTexEnvi; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexEnviv
#define push_glTexEnviv(target, pname, params) { \
    glTexEnviv_PACKED *packed_data = malloc(sizeof(glTexEnviv_PACKED)); \
    packed_data->format = glTexEnviv_FORMAT; \
    packed_data->func = gl4es_glTexEnviv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexEnvx
#define push_glTexEnvx(target, pname, param) { \
    glTexEnvx_PACKED *packed_data = malloc(sizeof(glTexEnvx_PACKED)); \
    packed_data->format = glTexEnvx_FORMAT; \
    packed_data->func = gl4es_glTexEnvx; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexEnvxv
#define push_glTexEnvxv(target, pname, params) { \
    glTexEnvxv_PACKED *packed_data = malloc(sizeof(glTexEnvxv_PACKED)); \
    packed_data->format = glTexEnvxv_FORMAT; \
    packed_data->func = gl4es_glTexEnvxv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexGenfv
#define push_glTexGenfv(coord, pname, params) { \
    glTexGenfv_PACKED *packed_data = malloc(sizeof(glTexGenfv_PACKED)); \
    packed_data->format = glTexGenfv_FORMAT; \
    packed_data->func = gl4es_glTexGenfv; \
    packed_data->args.a1 = (GLenum)coord; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexGeni
#define push_glTexGeni(coord, pname, param) { \
    glTexGeni_PACKED *packed_data = malloc(sizeof(glTexGeni_PACKED)); \
    packed_data->format = glTexGeni_FORMAT; \
    packed_data->func = gl4es_glTexGeni; \
    packed_data->args.a1 = (GLenum)coord; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexImage2D
#define push_glTexImage2D(target, level, internalformat, width, height, border, format, type, data) { \
    glTexImage2D_PACKED *packed_data = malloc(sizeof(glTexImage2D_PACKED)); \
    packed_data->format = glTexImage2D_FORMAT; \
    packed_data->func = gl4es_glTexImage2D; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLint)level; \
    packed_data->args.a3 = (GLint)internalformat; \
    packed_data->args.a4 = (GLsizei)width; \
    packed_data->args.a5 = (GLsizei)height; \
    packed_data->args.a6 = (GLint)border; \
    packed_data->args.a7 = (GLenum)format; \
    packed_data->args.a8 = (GLenum)type; \
    packed_data->args.a9 = (GLvoid *)data; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexParameterf
#define push_glTexParameterf(target, pname, param) { \
    glTexParameterf_PACKED *packed_data = malloc(sizeof(glTexParameterf_PACKED)); \
    packed_data->format = glTexParameterf_FORMAT; \
    packed_data->func = gl4es_glTexParameterf; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexParameterfv
#define push_glTexParameterfv(target, pname, params) { \
    glTexParameterfv_PACKED *packed_data = malloc(sizeof(glTexParameterfv_PACKED)); \
    packed_data->format = glTexParameterfv_FORMAT; \
    packed_data->func = gl4es_glTexParameterfv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfloat *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexParameteri
#define push_glTexParameteri(target, pname, param) { \
    glTexParameteri_PACKED *packed_data = malloc(sizeof(glTexParameteri_PACKED)); \
    packed_data->format = glTexParameteri_FORMAT; \
    packed_data->func = gl4es_glTexParameteri; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexParameteriv
#define push_glTexParameteriv(target, pname, params) { \
    glTexParameteriv_PACKED *packed_data = malloc(sizeof(glTexParameteriv_PACKED)); \
    packed_data->format = glTexParameteriv_FORMAT; \
    packed_data->func = gl4es_glTexParameteriv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLint *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexParameterx
#define push_glTexParameterx(target, pname, param) { \
    glTexParameterx_PACKED *packed_data = malloc(sizeof(glTexParameterx_PACKED)); \
    packed_data->format = glTexParameterx_FORMAT; \
    packed_data->func = gl4es_glTexParameterx; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed)param; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexParameterxv
#define push_glTexParameterxv(target, pname, params) { \
    glTexParameterxv_PACKED *packed_data = malloc(sizeof(glTexParameterxv_PACKED)); \
    packed_data->format = glTexParameterxv_FORMAT; \
    packed_data->func = gl4es_glTexParameterxv; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLenum)pname; \
    packed_data->args.a3 = (GLfixed *)params; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTexSubImage2D
#define push_glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, data) { \
    glTexSubImage2D_PACKED *packed_data = malloc(sizeof(glTexSubImage2D_PACKED)); \
    packed_data->format = glTexSubImage2D_FORMAT; \
    packed_data->func = gl4es_glTexSubImage2D; \
    packed_data->args.a1 = (GLenum)target; \
    packed_data->args.a2 = (GLint)level; \
    packed_data->args.a3 = (GLint)xoffset; \
    packed_data->args.a4 = (GLint)yoffset; \
    packed_data->args.a5 = (GLsizei)width; \
    packed_data->args.a6 = (GLsizei)height; \
    packed_data->args.a7 = (GLenum)format; \
    packed_data->args.a8 = (GLenum)type; \
    packed_data->args.a9 = (GLvoid *)data; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTranslatef
#define push_glTranslatef(x, y, z) { \
    glTranslatef_PACKED *packed_data = malloc(sizeof(glTranslatef_PACKED)); \
    packed_data->format = glTranslatef_FORMAT; \
    packed_data->func = gl4es_glTranslatef; \
    packed_data->args.a1 = (GLfloat)x; \
    packed_data->args.a2 = (GLfloat)y; \
    packed_data->args.a3 = (GLfloat)z; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glTranslatex
#define push_glTranslatex(x, y, z) { \
    glTranslatex_PACKED *packed_data = malloc(sizeof(glTranslatex_PACKED)); \
    packed_data->format = glTranslatex_FORMAT; \
    packed_data->func = gl4es_glTranslatex; \
    packed_data->args.a1 = (GLfixed)x; \
    packed_data->args.a2 = (GLfixed)y; \
    packed_data->args.a3 = (GLfixed)z; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform1f
#define push_glUniform1f(location, v0) { \
    glUniform1f_PACKED *packed_data = malloc(sizeof(glUniform1f_PACKED)); \
    packed_data->format = glUniform1f_FORMAT; \
    packed_data->func = gl4es_glUniform1f; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLfloat)v0; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform1fv
#define push_glUniform1fv(location, count, value) { \
    glUniform1fv_PACKED *packed_data = malloc(sizeof(glUniform1fv_PACKED)); \
    packed_data->format = glUniform1fv_FORMAT; \
    packed_data->func = gl4es_glUniform1fv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLfloat *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform1i
#define push_glUniform1i(location, v0) { \
    glUniform1i_PACKED *packed_data = malloc(sizeof(glUniform1i_PACKED)); \
    packed_data->format = glUniform1i_FORMAT; \
    packed_data->func = gl4es_glUniform1i; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLint)v0; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform1iv
#define push_glUniform1iv(location, count, value) { \
    glUniform1iv_PACKED *packed_data = malloc(sizeof(glUniform1iv_PACKED)); \
    packed_data->format = glUniform1iv_FORMAT; \
    packed_data->func = gl4es_glUniform1iv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLint *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform2f
#define push_glUniform2f(location, v0, v1) { \
    glUniform2f_PACKED *packed_data = malloc(sizeof(glUniform2f_PACKED)); \
    packed_data->format = glUniform2f_FORMAT; \
    packed_data->func = gl4es_glUniform2f; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLfloat)v0; \
    packed_data->args.a3 = (GLfloat)v1; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform2fv
#define push_glUniform2fv(location, count, value) { \
    glUniform2fv_PACKED *packed_data = malloc(sizeof(glUniform2fv_PACKED)); \
    packed_data->format = glUniform2fv_FORMAT; \
    packed_data->func = gl4es_glUniform2fv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLfloat *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform2i
#define push_glUniform2i(location, v0, v1) { \
    glUniform2i_PACKED *packed_data = malloc(sizeof(glUniform2i_PACKED)); \
    packed_data->format = glUniform2i_FORMAT; \
    packed_data->func = gl4es_glUniform2i; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLint)v0; \
    packed_data->args.a3 = (GLint)v1; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform2iv
#define push_glUniform2iv(location, count, value) { \
    glUniform2iv_PACKED *packed_data = malloc(sizeof(glUniform2iv_PACKED)); \
    packed_data->format = glUniform2iv_FORMAT; \
    packed_data->func = gl4es_glUniform2iv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLint *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform3f
#define push_glUniform3f(location, v0, v1, v2) { \
    glUniform3f_PACKED *packed_data = malloc(sizeof(glUniform3f_PACKED)); \
    packed_data->format = glUniform3f_FORMAT; \
    packed_data->func = gl4es_glUniform3f; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLfloat)v0; \
    packed_data->args.a3 = (GLfloat)v1; \
    packed_data->args.a4 = (GLfloat)v2; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform3fv
#define push_glUniform3fv(location, count, value) { \
    glUniform3fv_PACKED *packed_data = malloc(sizeof(glUniform3fv_PACKED)); \
    packed_data->format = glUniform3fv_FORMAT; \
    packed_data->func = gl4es_glUniform3fv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLfloat *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform3i
#define push_glUniform3i(location, v0, v1, v2) { \
    glUniform3i_PACKED *packed_data = malloc(sizeof(glUniform3i_PACKED)); \
    packed_data->format = glUniform3i_FORMAT; \
    packed_data->func = gl4es_glUniform3i; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLint)v0; \
    packed_data->args.a3 = (GLint)v1; \
    packed_data->args.a4 = (GLint)v2; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform3iv
#define push_glUniform3iv(location, count, value) { \
    glUniform3iv_PACKED *packed_data = malloc(sizeof(glUniform3iv_PACKED)); \
    packed_data->format = glUniform3iv_FORMAT; \
    packed_data->func = gl4es_glUniform3iv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLint *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform4f
#define push_glUniform4f(location, v0, v1, v2, v3) { \
    glUniform4f_PACKED *packed_data = malloc(sizeof(glUniform4f_PACKED)); \
    packed_data->format = glUniform4f_FORMAT; \
    packed_data->func = gl4es_glUniform4f; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLfloat)v0; \
    packed_data->args.a3 = (GLfloat)v1; \
    packed_data->args.a4 = (GLfloat)v2; \
    packed_data->args.a5 = (GLfloat)v3; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform4fv
#define push_glUniform4fv(location, count, value) { \
    glUniform4fv_PACKED *packed_data = malloc(sizeof(glUniform4fv_PACKED)); \
    packed_data->format = glUniform4fv_FORMAT; \
    packed_data->func = gl4es_glUniform4fv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLfloat *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform4i
#define push_glUniform4i(location, v0, v1, v2, v3) { \
    glUniform4i_PACKED *packed_data = malloc(sizeof(glUniform4i_PACKED)); \
    packed_data->format = glUniform4i_FORMAT; \
    packed_data->func = gl4es_glUniform4i; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLint)v0; \
    packed_data->args.a3 = (GLint)v1; \
    packed_data->args.a4 = (GLint)v2; \
    packed_data->args.a5 = (GLint)v3; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniform4iv
#define push_glUniform4iv(location, count, value) { \
    glUniform4iv_PACKED *packed_data = malloc(sizeof(glUniform4iv_PACKED)); \
    packed_data->format = glUniform4iv_FORMAT; \
    packed_data->func = gl4es_glUniform4iv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLint *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniformMatrix2fv
#define push_glUniformMatrix2fv(location, count, transpose, value) { \
    glUniformMatrix2fv_PACKED *packed_data = malloc(sizeof(glUniformMatrix2fv_PACKED)); \
    packed_data->format = glUniformMatrix2fv_FORMAT; \
    packed_data->func = gl4es_glUniformMatrix2fv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLboolean)transpose; \
    packed_data->args.a4 = (GLfloat *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniformMatrix3fv
#define push_glUniformMatrix3fv(location, count, transpose, value) { \
    glUniformMatrix3fv_PACKED *packed_data = malloc(sizeof(glUniformMatrix3fv_PACKED)); \
    packed_data->format = glUniformMatrix3fv_FORMAT; \
    packed_data->func = gl4es_glUniformMatrix3fv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLboolean)transpose; \
    packed_data->args.a4 = (GLfloat *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUniformMatrix4fv
#define push_glUniformMatrix4fv(location, count, transpose, value) { \
    glUniformMatrix4fv_PACKED *packed_data = malloc(sizeof(glUniformMatrix4fv_PACKED)); \
    packed_data->format = glUniformMatrix4fv_FORMAT; \
    packed_data->func = gl4es_glUniformMatrix4fv; \
    packed_data->args.a1 = (GLint)location; \
    packed_data->args.a2 = (GLsizei)count; \
    packed_data->args.a3 = (GLboolean)transpose; \
    packed_data->args.a4 = (GLfloat *)value; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glUseProgram
#define push_glUseProgram(program) { \
    glUseProgram_PACKED *packed_data = malloc(sizeof(glUseProgram_PACKED)); \
    packed_data->format = glUseProgram_FORMAT; \
    packed_data->func = gl4es_glUseProgram; \
    packed_data->args.a1 = (GLuint)program; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glValidateProgram
#define push_glValidateProgram(program) { \
    glValidateProgram_PACKED *packed_data = malloc(sizeof(glValidateProgram_PACKED)); \
    packed_data->format = glValidateProgram_FORMAT; \
    packed_data->func = gl4es_glValidateProgram; \
    packed_data->args.a1 = (GLuint)program; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib1f
#define push_glVertexAttrib1f(index, x) { \
    glVertexAttrib1f_PACKED *packed_data = malloc(sizeof(glVertexAttrib1f_PACKED)); \
    packed_data->format = glVertexAttrib1f_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib1f; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat)x; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib1fv
#define push_glVertexAttrib1fv(index, v) { \
    glVertexAttrib1fv_PACKED *packed_data = malloc(sizeof(glVertexAttrib1fv_PACKED)); \
    packed_data->format = glVertexAttrib1fv_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib1fv; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat *)v; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib2f
#define push_glVertexAttrib2f(index, x, y) { \
    glVertexAttrib2f_PACKED *packed_data = malloc(sizeof(glVertexAttrib2f_PACKED)); \
    packed_data->format = glVertexAttrib2f_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib2f; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat)x; \
    packed_data->args.a3 = (GLfloat)y; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib2fv
#define push_glVertexAttrib2fv(index, v) { \
    glVertexAttrib2fv_PACKED *packed_data = malloc(sizeof(glVertexAttrib2fv_PACKED)); \
    packed_data->format = glVertexAttrib2fv_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib2fv; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat *)v; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib3f
#define push_glVertexAttrib3f(index, x, y, z) { \
    glVertexAttrib3f_PACKED *packed_data = malloc(sizeof(glVertexAttrib3f_PACKED)); \
    packed_data->format = glVertexAttrib3f_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib3f; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat)x; \
    packed_data->args.a3 = (GLfloat)y; \
    packed_data->args.a4 = (GLfloat)z; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib3fv
#define push_glVertexAttrib3fv(index, v) { \
    glVertexAttrib3fv_PACKED *packed_data = malloc(sizeof(glVertexAttrib3fv_PACKED)); \
    packed_data->format = glVertexAttrib3fv_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib3fv; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat *)v; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib4f
#define push_glVertexAttrib4f(index, x, y, z, w) { \
    glVertexAttrib4f_PACKED *packed_data = malloc(sizeof(glVertexAttrib4f_PACKED)); \
    packed_data->format = glVertexAttrib4f_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib4f; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat)x; \
    packed_data->args.a3 = (GLfloat)y; \
    packed_data->args.a4 = (GLfloat)z; \
    packed_data->args.a5 = (GLfloat)w; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttrib4fv
#define push_glVertexAttrib4fv(index, v) { \
    glVertexAttrib4fv_PACKED *packed_data = malloc(sizeof(glVertexAttrib4fv_PACKED)); \
    packed_data->format = glVertexAttrib4fv_FORMAT; \
    packed_data->func = gl4es_glVertexAttrib4fv; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLfloat *)v; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttribPointer
#define push_glVertexAttribPointer(index, size, type, normalized, stride, pointer) { \
    glVertexAttribPointer_PACKED *packed_data = malloc(sizeof(glVertexAttribPointer_PACKED)); \
    packed_data->format = glVertexAttribPointer_FORMAT; \
    packed_data->func = gl4es_glVertexAttribPointer; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLint)size; \
    packed_data->args.a3 = (GLenum)type; \
    packed_data->args.a4 = (GLboolean)normalized; \
    packed_data->args.a5 = (GLsizei)stride; \
    packed_data->args.a6 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexAttribIPointer
#define push_glVertexAttribIPointer(index, size, type, stride, pointer) { \
    glVertexAttribIPointer_PACKED *packed_data = malloc(sizeof(glVertexAttribIPointer_PACKED)); \
    packed_data->format = glVertexAttribIPointer_FORMAT; \
    packed_data->func = gl4es_glVertexAttribIPointer; \
    packed_data->args.a1 = (GLuint)index; \
    packed_data->args.a2 = (GLint)size; \
    packed_data->args.a3 = (GLenum)type; \
    packed_data->args.a4 = (GLsizei)stride; \
    packed_data->args.a5 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glVertexPointer
#define push_glVertexPointer(size, type, stride, pointer) { \
    glVertexPointer_PACKED *packed_data = malloc(sizeof(glVertexPointer_PACKED)); \
    packed_data->format = glVertexPointer_FORMAT; \
    packed_data->func = gl4es_glVertexPointer; \
    packed_data->args.a1 = (GLint)size; \
    packed_data->args.a2 = (GLenum)type; \
    packed_data->args.a3 = (GLsizei)stride; \
    packed_data->args.a4 = (GLvoid *)pointer; \
    glPushCall((void *)packed_data); \
}
#endif
#ifndef direct_glViewport
#define push_glViewport(x, y, width, height) { \
    glViewport_PACKED *packed_data = malloc(sizeof(glViewport_PACKED)); \
    packed_data->format = glViewport_FORMAT; \
    packed_data->func = gl4es_glViewport; \
    packed_data->args.a1 = (GLint)x; \
    packed_data->args.a2 = (GLint)y; \
    packed_data->args.a3 = (GLsizei)width; \
    packed_data->args.a4 = (GLsizei)height; \
    glPushCall((void *)packed_data); \
}
#endif
#endif
