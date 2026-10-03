#include "texture.h"

#include "../glx/hardext.h"
#include "../glx/streaming.h"
#include "array.h"
#include "blit.h"
#include "decompress.h"
#include "debug.h"
#include "enum_info.h"
#include "fpe.h"
#include "framebuffers.h"
#include "gles.h"
#include "init.h"
#include "loader.h"
#include "matrix.h"
#include "pixel.h"
#include "raster.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

// 3d stubs
void APIENTRY_GL4ES gl4es_glTexImage3D(GLenum target, GLint level, GLint internalFormat,
                  GLsizei width, GLsizei height, GLsizei depth, GLint border,
                  GLenum format, GLenum type, const GLvoid *data) {

    // TODO: maybe too naive to force GL_TEXTURE_2D here?
    gl4es_glTexImage2D(GL_TEXTURE_3D, level, internalFormat, width, height,
                 border, format, type, data);
}
void APIENTRY_GL4ES gl4es_glTexSubImage3D(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, 
                     GLsizei width, GLsizei height, GLsizei depth, GLenum format,
                     GLenum type, const GLvoid *data) {

    gl4es_glTexSubImage2D(GL_TEXTURE_3D, level, xoffset, yoffset,
                    width, height, format, type, data);
}

void APIENTRY_GL4ES gl4es_glTexStorage3D(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height, GLsizei depth)
{
    DBG(printf("glTexStorage3D(%s, %d, %s, %d, %d, %d)\n", PrintEnum(target), levels, PrintEnum(internalformat), width, height, depth);)
    gl4es_glTexImage3D(target, 0, internalformat, width, height, depth, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
}

void APIENTRY_GL4ES gl4es_glCopyTexSubImage3D(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,
                                GLint x, GLint y, GLsizei width, GLsizei height) {
    gl4es_glCopyTexSubImage2D(GL_TEXTURE_3D, level, xoffset, yoffset, x, y, width, height);
}


//Direct wrapper
AliasExport(void,glTexImage3D,,(GLenum target, GLint level, GLint internalFormat, GLsizei width, GLsizei height, GLsizei depth, GLint border, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glTexImage3D,EXT,(GLenum target, GLint level, GLint internalFormat, GLsizei width, GLsizei height, GLsizei depth, GLint border, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glTexSubImage3D,,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,  GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glCopyTexSubImage3D,,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLint x, GLint y, GLsizei width, GLsizei height));

//EXT mapper
AliasExport(void,glTexSubImage3D,EXT,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,  GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glCopyTexSubImage3D,EXT,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLint x, GLint y, GLsizei width, GLsizei height));

//ARB mapper
AliasExport(void,glTexSubImage3D,ARB,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,  GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glCopyTexSubImage3D,ARB,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLint x, GLint y, GLsizei width, GLsizei height));

// TexStorage
AliasExport(void,glTexStorage3D,,(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height, GLsizei depth));
