#ifndef _GL4ES_TEXTURE_H_
#define _GL4ES_TEXTURE_H_

#include "buffers.h"
#include "const.h"
#include "gles.h"

void APIENTRY_GL4ES gl4es_glTexImage2D(GLenum target, GLint level, GLint internalFormat,
                  GLsizei width, GLsizei height, GLint border,
                  GLenum format, GLenum type, const GLvoid *data);

void APIENTRY_GL4ES gl4es_glTexImage1D(GLenum target, GLint level, GLint internalFormat,
                  GLsizei width, GLint border,
                  GLenum format, GLenum type, const GLvoid *data);

void APIENTRY_GL4ES gl4es_glTexImage3D(GLenum target, GLint level, GLint internalFormat,
                  GLsizei width, GLsizei height, GLsizei depth,
                  GLint border, GLenum format, GLenum type, const GLvoid *data);

void APIENTRY_GL4ES gl4es_glTexSubImage2D(GLenum target, GLint level, GLint xoffset, GLint yoffset,
                     GLsizei width, GLsizei height, GLenum format, GLenum type,
                     const GLvoid *data);
                     
void APIENTRY_GL4ES gl4es_glTexSubImage1D(GLenum target, GLint level, GLint xoffset,
                     GLsizei width, GLenum format, GLenum type,
                     const GLvoid *data);
                     
void APIENTRY_GL4ES gl4es_glTexSubImage3D(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, 
                     GLsizei width, GLsizei height, GLsizei depth, GLenum format,
                     GLenum type, const GLvoid *data);

void APIENTRY_GL4ES gl4es_glBindTexture(GLenum target, GLuint texture);
void APIENTRY_GL4ES gl4es_glGenTextures(GLsizei n, GLuint * textures);
void APIENTRY_GL4ES gl4es_glDeleteTextures(GLsizei n, const GLuint * textures);
void APIENTRY_GL4ES gl4es_glTexParameteri(GLenum target, GLenum pname, GLint param);
void APIENTRY_GL4ES gl4es_glTexParameterf(GLenum target, GLenum pname, GLfloat param);
GLboolean APIENTRY_GL4ES gl4es_glAreTexturesResident(GLsizei n, const GLuint *textures, GLboolean *residences);
void APIENTRY_GL4ES gl4es_glGetTexLevelParameteriv(GLenum target, GLint level, GLenum pname, GLint *params);
void APIENTRY_GL4ES gl4es_glGetTexImage(GLenum target, GLint level, GLenum format, GLenum type, GLvoid * img);
void APIENTRY_GL4ES gl4es_glReadPixels(GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, GLvoid * data);

void APIENTRY_GL4ES gl4es_glCompressedTexImage2D(GLenum target, GLint level, GLenum internalformat,
							GLsizei width, GLsizei height, GLint border,
							GLsizei imageSize, const GLvoid *data);
void APIENTRY_GL4ES gl4es_glCompressedTexImage1D(GLenum target, GLint level, GLenum internalformat,
							GLsizei width, GLint border,
							GLsizei imageSize, const GLvoid *data);
void APIENTRY_GL4ES gl4es_glCompressedTexImage3D(GLenum target, GLint level, GLenum internalformat,
							GLsizei width, GLsizei height, GLsizei depth, GLint border,
							GLsizei imageSize, const GLvoid *data);
							
void APIENTRY_GL4ES gl4es_glCompressedTexSubImage2D(GLenum target, GLint level, GLint xoffset, GLint yoffset,
							   GLsizei width, GLsizei height, GLenum format, 
							   GLsizei imageSize, const GLvoid *data);
void APIENTRY_GL4ES gl4es_glCompressedTexSubImage1D(GLenum target, GLint level, GLint xoffset,
							   GLsizei width, GLenum format, 
							   GLsizei imageSize, const GLvoid *data);
void APIENTRY_GL4ES gl4es_glCompressedTexSubImage3D(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,
							   GLsizei width, GLsizei height, GLsizei depth, GLenum format, 
							   GLsizei imageSize, const GLvoid *data);
                               
void APIENTRY_GL4ES gl4es_glGetCompressedTexImage(GLenum target, GLint lod, GLvoid *img);

                               
void APIENTRY_GL4ES gl4es_glCopyTexImage1D(GLenum target,  GLint level,  GLenum internalformat,  GLint x,  GLint y,  
								GLsizei width,  GLint border);

void APIENTRY_GL4ES gl4es_glCopyTexImage2D(GLenum target,  GLint level,  GLenum internalformat,  GLint x,  GLint y,  
								GLsizei width,  GLsizei height,  GLint border);

void APIENTRY_GL4ES gl4es_glCopyTexSubImage2D(GLenum target, GLint level, GLint xoffset, GLint yoffset,
                                GLint x, GLint y, GLsizei width, GLsizei height);
                                
void APIENTRY_GL4ES gl4es_glCopyTexSubImage1D(GLenum target, GLint level, GLint xoffset, GLint x, GLint y,
                                GLsizei width);

void APIENTRY_GL4ES gl4es_glCopyTexSubImage3D(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,
                                GLint x, GLint y, GLsizei width, GLsizei height);

void APIENTRY_GL4ES gl4es_glTexStorage1D(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width);
void APIENTRY_GL4ES gl4es_glTexStorage2D(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height);
void APIENTRY_GL4ES gl4es_glTexStorage3D(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height, GLsizei depth);

void tex_coord_rect_arb(GLfloat *tex, int stride, GLsizei len,
                        GLsizei width, GLsizei height);
void tex_coord_npot(GLfloat *tex, GLsizei len,
                    GLsizei width, GLsizei height,
                    GLsizei nwidth, GLsizei nheight);

void tex_coord_matrix(GLfloat *tex, GLsizei len, const GLfloat* mat);

int npot(int n);

typedef enum {
    ENABLED_TEX1D = 0,
    ENABLED_TEX2D,
    ENABLED_TEX3D,
    ENABLED_TEXTURE_RECTANGLE,
    ENABLED_CUBE_MAP,
    ENABLED_TEXTURE_LAST
} texture_enabled_t;

typedef struct {
    GLuint glname;
    GLenum target;
	GLenum min_filter;
	GLenum mag_filter;
    GLenum wrap_s;
    GLenum wrap_t;
    GLenum wrap_r;
    GLenum compare;
    GLenum func;
    GLfloat min_lod;
    GLfloat max_lod;
    GLfloat border_color[4];
} glsampler_t;

typedef struct {
    GLuint texture;
    GLuint glname;
    GLenum target;
    GLsizei width;
    GLsizei height;
    GLsizei nwidth;
    GLsizei nheight;
    GLboolean mipmap_auto;
    GLboolean mipmap_need;
    GLboolean mipmap_done;
    int base_level;
    int max_level;
    GLenum  format;
    GLenum  type;
    GLenum  wanted_internal;
    GLenum  orig_internal;
    GLenum  internalformat;
    GLenum  inter_format, inter_type;
    int aniso;
    int shrink;
    GLboolean alpha;
    GLboolean compressed;
    GLboolean streamed;
    int valid;
	int	streamingID;
    int fpe_format; // tracking simplified internal format for FPE
    int npot;
    int adjust; // flag if width/height has to be adjusted
    float adjustxy[2];  // adjust factor
    int useratio;
    float ratiox, ratioy;
    int   binded_fbo;   // non zero if binded to an fbo
    GLenum binded_attachment; // What binding (texture, depth, stencil, depth_stencil)
    GLuint renderdepth; // in case renderbuffer where used instead...
    GLuint renderstencil;
    int     trace;  // internal use for debug
    GLvoid *data;	// in case we want to keep a copy of it (it that case, always RGBA/GL_UNSIGNED_BYTE
    glsampler_t sampler;    // internal sampler if not superseded by glBindSampler
    glsampler_t actual;     // actual sampler
    float fbtex_ratio; // Lower rendering resolution
} gltexture_t;

KHASH_MAP_DECLARE_INT(tex, gltexture_t *);

static inline GLenum map_tex_target(GLenum target) {
    switch (target) {
        case GL_TEXTURE_1D:
        case GL_TEXTURE_3D:
        case GL_TEXTURE_RECTANGLE_ARB:
            target = GL_TEXTURE_2D;
            break;
        case GL_PROXY_TEXTURE_1D:
        case GL_PROXY_TEXTURE_3D:
        case GL_PROXY_TEXTURE_RECTANGLE_ARB:
            target = GL_PROXY_TEXTURE_2D;
            break;
    }
    return target;
}
gltexture_t* gl4es_getTexture(GLenum target, GLuint texture);
gltexture_t* gl4es_getCurrentTexture(GLenum target);

static inline GLuint what_target(GLenum target) {
    switch(target) {
        case GL_TEXTURE_1D:
            return ENABLED_TEX1D;
        case GL_TEXTURE_3D:
            return ENABLED_TEX3D;
        case GL_TEXTURE_CUBE_MAP:
        case GL_TEXTURE_CUBE_MAP_POSITIVE_X:
        case GL_TEXTURE_CUBE_MAP_NEGATIVE_X:
        case GL_TEXTURE_CUBE_MAP_POSITIVE_Y:
        case GL_TEXTURE_CUBE_MAP_NEGATIVE_Y:
        case GL_TEXTURE_CUBE_MAP_POSITIVE_Z:
        case GL_TEXTURE_CUBE_MAP_NEGATIVE_Z:
            return ENABLED_CUBE_MAP;
        case GL_TEXTURE_RECTANGLE_ARB:
            return ENABLED_TEXTURE_RECTANGLE;
        case GL_TEXTURE_2D:
        default:
            return ENABLED_TEX2D;
    }
}
static inline GLenum to_target(GLuint itarget) {
    switch(itarget) {
        case ENABLED_TEX1D:
            return GL_TEXTURE_1D;
        case ENABLED_TEX3D:
            return GL_TEXTURE_3D;
        case ENABLED_TEXTURE_RECTANGLE:
            return GL_TEXTURE_RECTANGLE_ARB;
        case ENABLED_CUBE_MAP:
            return GL_TEXTURE_CUBE_MAP;
        case ENABLED_TEX2D:
        default:
            return GL_TEXTURE_2D;
    }
}
#define IS_TEX1D(T) (T&(1<<ENABLED_TEX1D))
#define IS_TEX2D(T) (T&(1<<ENABLED_TEX2D))
#define IS_TEX3D(T) (T&(1<<ENABLED_TEX3D))
#define IS_TEXTURE_RECTANGLE(T) (T&(1<<ENABLED_TEXTURE_RECTANGLE))
#define IS_ANYTEX(T) (T&((1<<ENABLED_TEX2D)|(1<<ENABLED_TEX1D)|(1<<ENABLED_TEX3D)|(1<<ENABLED_TEXTURE_RECTANGLE)))
#define IS_CUBE_MAP(T) (T&(1<<ENABLED_CUBE_MAP))

static inline GLint get_target(GLuint enabled) {
    if(!enabled)
        return -1;
    GLint itarget = ENABLED_TEX2D;
    if(enabled!=(1<<itarget))
        for (int j=0; j<ENABLED_TEXTURE_LAST; j++)
            if(enabled & (1<<j))
                itarget = j;
    return itarget;
}

void APIENTRY_GL4ES gl4es_glActiveTexture( GLenum texture );
void APIENTRY_GL4ES gl4es_glClientActiveTexture( GLenum texture );
void APIENTRY_GL4ES gl4es_glMultiTexCoord2f( GLenum target, GLfloat s, GLfloat t );
GLboolean APIENTRY_GL4ES gl4es_glIsTexture( GLuint texture );

int  tex_setup_needchange(GLuint itarget);
void tex_setup_texcoord(GLuint len, int changes, GLuint texture, vertexattrib_t* ptr);

int wrap_npot(GLenum wrap);
int minmag_npot(GLenum mag);
GLenum minmag_forcenpot(GLenum filt);
GLenum minmag_float(GLenum filt);
GLboolean isDXTc(GLenum format);

void realize_bound(int TMU, GLenum target);
void realize_1texture(GLenum target, int TMU, gltexture_t* tex, glsampler_t* sampler);
void realize_textures(int drawing);
void realize_active();

// defined in samplers.c
// return 0 if pname not handled, 1 if ok (or ok with error)
int samplerParameterfv(glsampler_t* sampler, GLenum pname, const GLfloat *param);
// return 0 if pname not handled, 1 if ok (or ok with error)
int getSamplerParameterfv(glsampler_t* sampler, GLenum pname, GLfloat *params);

void init_sampler(glsampler_t* sampler);

#endif // _GL4ES_TEXTURE_H_
