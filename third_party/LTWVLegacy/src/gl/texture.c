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

#ifndef GL_TEXTURE_STREAM_IMG  
#define GL_TEXTURE_STREAM_IMG                                   0x8C0D     
#endif
#ifdef TEXSTREAM
#include <EGL/egl.h>
#include <EGL/eglext.h>
#endif

// expand non-power-of-two sizes
// TODO: what does this do to repeating textures?
int npot(int n) {
    if (n == 0) return 0;

    int i = 1;
    while (i < n) i <<= 1;
    return i;
}

static int inline nlevel(int size, int level) {
    if(size) {
        size>>=level;
        if(!size) size=1;
    }
    return size;
}

// return the max level for that WxH size
static int inline maxlevel(int w, int h) {
    int mlevel = 0;
    while(w!=1 || h!=1) {
        w>>=1; h>>=1; 
        if(!w) w=1;
        if(!h) h=1;
        ++mlevel;
    }
    return mlevel;
}

static int is_fake_compressed_rgb(GLenum internalformat)
{
    if(internalformat==GL_COMPRESSED_RGB) return 1;
    if(internalformat==GL_COMPRESSED_RGB_S3TC_DXT1_EXT) return 1;
    if(internalformat==GL_COMPRESSED_SRGB_S3TC_DXT1_EXT) return 1;
    return 0;
}
static int is_fake_compressed_rgba(GLenum internalformat)
{
    if(internalformat==GL_COMPRESSED_RGBA) return 1;
    if(internalformat==GL_COMPRESSED_RGBA_S3TC_DXT1_EXT) return 1;
    if(internalformat==GL_COMPRESSED_RGBA_S3TC_DXT3_EXT) return 1;
    if(internalformat==GL_COMPRESSED_RGBA_S3TC_DXT5_EXT) return 1;
    if(internalformat==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT) return 1;
    if(internalformat==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT) return 1;
    if(internalformat==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT) return 1;
    return 0;
}

void internal2format_type(GLenum internalformat, GLenum *format, GLenum *type)
{
    switch(internalformat) {
        case GL_RED:
        case GL_R8:
        case GL_R:
            if(!hardext.rgtex) {
                *format = GL_RGB;
                *type = GL_UNSIGNED_BYTE;
            } else {
                *format = GL_RED;
                *type = GL_UNSIGNED_BYTE;
            }
            break;
        case GL_RG:
            if(!hardext.rgtex) {
                *format = GL_RGB;
                *type = GL_UNSIGNED_BYTE;
            } else {
                *format = GL_RG;
                *type = GL_UNSIGNED_BYTE;
            }
            break;
        case GL_COMPRESSED_ALPHA:
        case GL_ALPHA:
            *format = GL_ALPHA;
            *type = GL_UNSIGNED_BYTE;
            break;
        case 1: // is this here or with GL_RED?
        case GL_COMPRESSED_LUMINANCE:
        case GL_LUMINANCE:
            *format = GL_LUMINANCE;
            *type = GL_UNSIGNED_BYTE;
            break;
        case 2:
        case GL_COMPRESSED_LUMINANCE_ALPHA:
        case GL_LUMINANCE8_ALPHA8:
        case GL_LUMINANCE_ALPHA:
            if(globals4es.nolumalpha) {
                *format = GL_RGBA;
                *type = GL_UNSIGNED_BYTE;
            } else {
                *format = GL_LUMINANCE_ALPHA;
                *type = GL_UNSIGNED_BYTE;
            }
            break;
        case GL_RGB5:
        case GL_RGB565:
            *format = GL_RGB;
            *type = GL_UNSIGNED_SHORT_5_6_5;
            break;
        case GL_RGB:
            if(globals4es.avoid24bits)
                *format = GL_RGBA;
            else
                *format = GL_RGB;
            *type = GL_UNSIGNED_BYTE;
            break;
        case GL_RGB5_A1:
            *format = GL_RGBA;
            *type = GL_UNSIGNED_SHORT_5_5_5_1;
            break;
        case GL_RGBA4:
            *format = GL_RGBA;
            *type = GL_UNSIGNED_SHORT_4_4_4_4;
            break;
        case GL_RGBA:
            *format = GL_RGBA;
            *type = GL_UNSIGNED_BYTE;
            break;
        case GL_BGRA:
            if(hardext.bgra8888)
                *format = GL_BGRA;
            else
                *format = GL_RGBA;
            *type = GL_UNSIGNED_BYTE;
            break;
        case GL_DEPTH_COMPONENT:
            *format = GL_DEPTH_COMPONENT;
            if (*type != GL_UNSIGNED_SHORT) {
                *type = (hardext.depth24)?GL_UNSIGNED_INT:GL_UNSIGNED_SHORT;
            }
            break;
        case GL_DEPTH_STENCIL:
        case GL_DEPTH24_STENCIL8:
            *format = GL_DEPTH_STENCIL;
            *type = GL_UNSIGNED_INT_24_8;
            break;
        case GL_R16F:
            if(!hardext.rgtex)
                *format = GL_RGB;
        else
            *format = GL_RED;
        if(!hardext.halffloattex)
            *type = GL_UNSIGNED_BYTE;
        else
            *type = GL_HALF_FLOAT_OES;
            break;
        case GL_RGBA16F:
            *format = GL_RGBA;
            *type = (hardext.halffloattex)?GL_HALF_FLOAT_OES:GL_UNSIGNED_BYTE;
            break;
        case GL_RGBA32F:
            *format = GL_RGBA;
            *type = (hardext.floattex)?GL_FLOAT:GL_UNSIGNED_BYTE;
            break;
        case GL_RGB16F:
            *format = GL_RGB;
            *type = (hardext.halffloattex)?GL_HALF_FLOAT_OES:GL_UNSIGNED_BYTE;
            break;
        case GL_RGB32F:
            *format = GL_RGB;
            *type = (hardext.floattex)?GL_FLOAT:GL_UNSIGNED_BYTE;
            break;
        default:
            printf("LIBGL: Warning, unknown Internalformat (%s)\n", PrintEnum(internalformat));
            *format = GL_RGBA;
            *type = GL_UNSIGNED_BYTE;
            break;
    }
}

static void *swizzle_texture(GLsizei width, GLsizei height,
                             GLenum *format, GLenum *type,
                             GLenum intermediaryformat, GLenum internalformat,
                             const GLvoid *data, gltexture_t *bound) {
    int convert = 0;
    GLenum dest_format = GL_RGBA;
    GLenum dest_type = GL_UNSIGNED_BYTE;
    int check = 1;
    // compressed format are not handled here, so mask them....
    if (is_fake_compressed_rgb(intermediaryformat)) intermediaryformat=GL_RGB;
    if (is_fake_compressed_rgba(intermediaryformat)) intermediaryformat=GL_RGBA;
    if (is_fake_compressed_rgb(internalformat)) internalformat=GL_RGB;
    if (is_fake_compressed_rgba(internalformat)) internalformat=GL_RGBA;
    if (intermediaryformat==GL_COMPRESSED_LUMINANCE) intermediaryformat=GL_LUMINANCE;
    if (internalformat==GL_COMPRESSED_LUMINANCE) internalformat=GL_LUMINANCE;

    if(*format != intermediaryformat || intermediaryformat!=internalformat) {
        internal2format_type(intermediaryformat, &dest_format, &dest_type);
        convert = 1;
        check = 0;
    } else {
        if((*type)==GL_HALF_FLOAT) (*type) = GL_HALF_FLOAT_OES;    //the define is different between GL and GLES...
        switch (*format) {
            case GL_R:
            case GL_RED:
                if(!hardext.rgtex) {
                    dest_format = GL_RGB;
                    convert = 1;
                } else
                    dest_format = GL_RED;
                break;
            case GL_RG:
                if(!hardext.rgtex) {
                    dest_format = GL_RGB;
                    convert = 1;
                } else
                    dest_format = GL_RG;
                break;
            case GL_COMPRESSED_LUMINANCE:
                *format = GL_LUMINANCE;
            case GL_LUMINANCE:
                dest_format = GL_LUMINANCE;
                break;
            case GL_LUMINANCE16F:
                dest_format = GL_LUMINANCE;
                if(hardext.halffloattex) {
                    dest_type = GL_HALF_FLOAT_OES;
                    check = 0;
                }
                break;
            case GL_LUMINANCE32F:
                dest_format = GL_LUMINANCE;
                if(hardext.floattex) {
                    dest_type = GL_FLOAT;
                    check = 0;
                }
                break;
            case GL_RGB:
                dest_format = GL_RGB;
                break;
            case GL_COMPRESSED_ALPHA:
                *format = GL_ALPHA;
            case GL_ALPHA:
                dest_format = GL_ALPHA;
                break;
            case GL_ALPHA16F:
                dest_format = GL_ALPHA;
                if(hardext.halffloattex) {
                    dest_type = GL_HALF_FLOAT_OES;
                    check = 0;
                }
                break;
            case GL_ALPHA32F:
                dest_format = GL_ALPHA;
                if(hardext.floattex) {
                    dest_type = GL_FLOAT;
                    check = 0;
                }
                break;
            case GL_RGBA:
                break;
            case GL_LUMINANCE8_ALPHA8:
            case GL_COMPRESSED_LUMINANCE_ALPHA:
                if(globals4es.nolumalpha)
                    convert = 1;
                else {
                    dest_format = GL_LUMINANCE_ALPHA;
                    *format = GL_LUMINANCE_ALPHA;
                }
                break;
            case GL_LUMINANCE_ALPHA:
                if(globals4es.nolumalpha)
                    convert = 1;
                else
                    dest_format = GL_LUMINANCE_ALPHA;
                break;
            case GL_LUMINANCE_ALPHA16F:
                if(globals4es.nolumalpha)
                    convert = 1;
                else
                    dest_format = GL_LUMINANCE_ALPHA;
                if(hardext.halffloattex) {
                    dest_type = GL_HALF_FLOAT_OES;
                    check = 0;
                }
                break;
            case GL_LUMINANCE_ALPHA32F:
                if(globals4es.nolumalpha)
                    convert = 1;
                else
                    dest_format = GL_LUMINANCE_ALPHA;
                if(hardext.floattex) {
                    dest_type = GL_FLOAT;
                    check = 0;
                }
                break;
            // vvvvv all this are internal formats, so it should not happens
            case GL_RGB5:
            case GL_RGB565:
                dest_format = GL_RGB;
                dest_type = GL_UNSIGNED_SHORT_5_6_5;
                convert = 1;
                check = 0;
                break;
            case GL_RGB8:
                dest_format = GL_RGB;
                *format = GL_RGB;
                break;
            case GL_RGBA4:
                dest_format = GL_RGBA;
                dest_type = GL_UNSIGNED_SHORT_4_4_4_4;
                *format = GL_RGBA;
                check = 0;
                break;
            case GL_RGBA8:
                dest_format = GL_RGBA;
                *format = GL_RGBA;
                break;
            case GL_BGRA:
                if(hardext.bgra8888 && ((*type)==GL_UNSIGNED_BYTE || (*type)==GL_FLOAT || (*type)==GL_HALF_FLOAT ||
                #ifdef __BIG_ENDIAN__
                    (((*type)==GL_UNSIGNED_INT_8_8_8_8_REV) && hardext.rgba8888rev)
                #else
                    (((*type)==GL_UNSIGNED_INT_8_8_8_8) && hardext.rgba8888)
                #endif
                )) 
                {
                    dest_format = GL_BGRA;
                    //*format = GL_BGRA;
                } else {
                    convert = 1;
                    if(hardext.bgra8888 && 
                    #ifdef __BIG_ENDIAN__
                        (*type==GL_UNSIGNED_INT_8_8_8_8_REV)
                    #else
                        (*type==GL_UNSIGNED_INT_8_8_8_8)
                    #endif
                    ) {
                        //*format = GL_BGRA;    //only type needs conversion
                        dest_format = GL_BGRA;
                        check = 0;
                    }
                }
                break;
            case GL_DEPTH24_STENCIL8:
            case GL_DEPTH_STENCIL:
                if(hardext.depthtex && hardext.depthstencil) {
                    *format = dest_format = GL_DEPTH_STENCIL;
                    dest_type = GL_UNSIGNED_INT_24_8;
                    check = 0;
                } else convert = 1;
                break;
            case GL_DEPTH_COMPONENT:
            case GL_DEPTH_COMPONENT16:
            case GL_DEPTH_COMPONENT24:
            case GL_DEPTH_COMPONENT32:
                if(hardext.depthtex) {
                    if(dest_type==GL_UNSIGNED_BYTE) {
                        dest_type=(*format==GL_DEPTH_COMPONENT32 || *format==GL_DEPTH_COMPONENT24)?GL_UNSIGNED_INT:GL_UNSIGNED_SHORT;
                        convert = 1;
                    }
                    *format = dest_format = GL_DEPTH_COMPONENT;
                    check = 0;
                } else
                    convert = 1;
                break;
            case GL_STENCIL_INDEX8:
                if(hardext.stenciltex)
                    *format = dest_format = GL_STENCIL_INDEX8;
                else
                    convert = 1;
                break;
            default:
                convert = 1;
                break;
        }
        if(check)
        switch (*type) {
            case GL_UNSIGNED_SHORT_4_4_4_4_REV:
                if(dest_format==GL_RGBA)
                    dest_type = GL_UNSIGNED_SHORT_4_4_4_4;
                convert = 1;
                break;
            case GL_UNSIGNED_SHORT_4_4_4_4:
                if(dest_format==GL_RGBA)
                    dest_type = GL_UNSIGNED_SHORT_4_4_4_4;
                else
                    convert = 1;
                break;
            case GL_UNSIGNED_SHORT_1_5_5_5_REV:
                if(!hardext.rgba1555rev) {
                    if(dest_format==GL_RGBA)
                        dest_type = GL_UNSIGNED_SHORT_5_5_5_1;
                    convert = 1;
                }
                break;
            case GL_UNSIGNED_SHORT_5_5_5_1:
                if(dest_format==GL_RGBA)
                    dest_type = GL_UNSIGNED_SHORT_5_5_5_1;
                else
                    convert = 1;
                break;
            case GL_UNSIGNED_SHORT_5_6_5_REV:
                if (dest_format==GL_RGB)
                    dest_type = GL_UNSIGNED_SHORT_5_6_5;
                convert = 1;
                break;
            case GL_UNSIGNED_SHORT_5_6_5:
                if (dest_format==GL_RGB)
                    dest_type = GL_UNSIGNED_SHORT_5_6_5;
                else
                    convert = 1;
                break;
            #ifdef __BIG_ENDIAN__
            case GL_UNSIGNED_INT_8_8_8_8:
            #else
            case GL_UNSIGNED_INT_8_8_8_8_REV:
            #endif
                *type = GL_UNSIGNED_BYTE;
                // fall through
            case GL_UNSIGNED_BYTE:
                if(dest_format==GL_RGB && globals4es.avoid24bits) {
                    dest_format = GL_RGBA;
                    convert = 1;
                }
                break;
            #ifdef __BIG_ENDIAN__
            case GL_UNSIGNED_INT_8_8_8_8_REV:
                if(!hardext.rgba8888rev) {
                    dest_type = GL_UNSIGNED_BYTE;
                    convert = 1;
                }
                break;
            #else
            case GL_UNSIGNED_INT_8_8_8_8:
                if(!hardext.rgba8888) {
                    dest_type = GL_UNSIGNED_BYTE;
                    convert = 1;
                }
                break;
            #endif
            case GL_UNSIGNED_INT_24_8:
                if(hardext.depthtex && hardext.depthstencil) {
                    dest_type = GL_UNSIGNED_INT_24_8;
                } else {
                    *type = GL_UNSIGNED_BYTE;   // will probably do nothing good!
                    convert = 1;
                }
                break;
            case GL_FLOAT:
                if(hardext.floattex)
                    dest_type = GL_FLOAT;
                else
                    convert = 1;
                break;
            case GL_HALF_FLOAT:
            case GL_HALF_FLOAT_OES:
                if(hardext.halffloattex)
                    dest_type = GL_HALF_FLOAT_OES;
                else
                    convert = 1;
                break;
            default:
                convert = 1;
                break;
        }
    }
    if (data) {
        if (convert) {
            GLvoid *pixels = (GLvoid *)data;
            bound->inter_format = dest_format;
            bound->format = dest_format;
            bound->inter_type = dest_type;
            bound->type = dest_type;
            if (! pixel_convert(data, &pixels, width, height,
                                *format, *type, dest_format, dest_type, 0, glstate->texture.unpack_align)) {
                printf("LIBGL: swizzle error: (%s, %s -> %s, %s)\n",
                    PrintEnum(*format), PrintEnum(*type), PrintEnum(dest_format), PrintEnum(dest_type));
                return NULL;
            }
            *type = dest_type;
            *format = dest_format;
            if(dest_format!=internalformat) {
                GLvoid *pix2 = (GLvoid *)pixels;
                internal2format_type(internalformat, &dest_format, &dest_type);
                bound->format = dest_format;
                bound->type = dest_type;
                if (! pixel_convert(pixels, &pix2, width, height,
                                *format, *type, dest_format, dest_type, 0, glstate->texture.unpack_align)) {
                    printf("LIBGL: swizzle error: (%s, %s -> %s, %s)\n",
                        PrintEnum(dest_format), PrintEnum(dest_type), PrintEnum(internalformat), PrintEnum(dest_type));
                    return NULL;
                }
                if(pix2!=pixels) {
                    if (pixels!=data)
                        free(pixels);
                    pixels = pix2;
                }
                *type = dest_type;
                *format = dest_format;
            }
            GLvoid *pix2 = pixels;
            if (raster_need_transform())
                if (!pixel_transform(data, &pixels, width, height,
                                *format, *type, glstate->raster.raster_scale, glstate->raster.raster_bias)) {
                    printf("LIBGL: swizzle/convert error: (%s, %s -> %s, %s)\n",
                        PrintEnum(*format), PrintEnum(*type), PrintEnum(dest_format), PrintEnum(dest_type));
                    pix2 = pixels;
                }
            if (pix2!=pixels && pixels!=data)
                free(pixels);
            return pix2;
        } else {
            bound->inter_format = dest_format;
            bound->format = dest_format;
            bound->inter_type = dest_type;
            bound->type = dest_type;
        }
    } else {
        bound->inter_format = dest_format;
        bound->inter_type = dest_type;
        if (convert) {
            internal2format_type(internalformat, &dest_format, &dest_type); // in case they are differents
            *type = dest_type;
            *format = dest_format;
        }
        bound->format = dest_format;
        bound->type = dest_type;
    }
    return (void *)data;
}

GLenum swizzle_internalformat(GLenum *internalformat, GLenum format, GLenum type) {
    GLenum ret = *internalformat;
    GLenum sret;
    switch(*internalformat) {
        case GL_RED:
        case GL_R:
        case GL_R8:
            if(!hardext.rgtex) {
                ret = GL_RGB; sret = GL_RGB;
            } else
                sret = GL_RED;
            break;
        case GL_RG:
            if(!hardext.rgtex) {
                ret = GL_RGB; sret = GL_RGB;
            } else
                sret = GL_RG;
            break;
        case GL_RGB565:
            ret=GL_RGB5;
        case GL_RGB5:
            sret = GL_RGB5;
            break;
        case GL_RGB:
             if(globals4es.avoid16bits==0 && format==GL_RGB && type==GL_UNSIGNED_SHORT_5_6_5) {
                sret = ret = GL_RGB5;
                break;
            }
        case GL_RGB8:
        case GL_BGR:
        case GL_RGB16:
        case GL_RGB16F:
        case GL_RGB32F:
        case 3: 
            ret = GL_RGB; sret = GL_RGB; 
            break;
        case GL_RGBA4:
            sret = GL_RGBA4;
            break;
        case GL_RGB5_A1:
            sret = GL_RGB5_A1;
            break;
        case GL_RGBA:
            if(globals4es.avoid16bits==0 && format==GL_RGBA && type==GL_UNSIGNED_SHORT_5_5_5_1) {
                sret = ret = GL_RGB5_A1;
                break;
            }
            if(globals4es.avoid16bits==0 && format==GL_RGBA && type==GL_UNSIGNED_SHORT_4_4_4_4) {
                sret = ret = GL_RGBA4;
                break;
            }
            if(format==GL_BGRA && hardext.bgra8888) {
                sret = ret = GL_BGRA;
            }
        case GL_RGBA8:
        case GL_RGBA16:
        case GL_RGBA16F:
        case GL_RGBA32F:
        case GL_RGB10_A2:
        case 4: 
            if(format==GL_BGRA && hardext.bgra8888) {
                ret = GL_BGRA; sret = GL_BGRA; 
            } else {
                ret = GL_RGBA; sret = GL_RGBA; 
            }
            break;
        case GL_ALPHA32F:
        case GL_ALPHA16F:
        case GL_ALPHA8:
        case GL_ALPHA:
            ret = GL_ALPHA; sret = GL_ALPHA;
            break;
        case 1:
        case GL_LUMINANCE32F:
        case GL_LUMINANCE16F:
        case GL_LUMINANCE8:
        case GL_LUMINANCE16:
        case GL_LUMINANCE:
            if(format==GL_RED && hardext.rgtex) {
                ret = GL_RED; sret = GL_RED;    
            } else {
                ret = GL_LUMINANCE; sret = GL_LUMINANCE;
            }
            break;
        case 2: 
        case GL_LUMINANCE4_ALPHA4:
        case GL_LUMINANCE8_ALPHA8:
        case GL_LUMINANCE16_ALPHA16:
        case GL_LUMINANCE_ALPHA32F:
        case GL_LUMINANCE_ALPHA16F:
        case GL_LUMINANCE_ALPHA:
            ret = GL_LUMINANCE_ALPHA;
            if (globals4es.nolumalpha)
                sret = GL_RGBA;
            else
                sret = GL_LUMINANCE_ALPHA;
            break;
        // compressed format...
        case GL_COMPRESSED_ALPHA:
            ret = GL_ALPHA;//GL_COMPRESSED_RGBA;
            sret = GL_ALPHA;
            break;
        case GL_COMPRESSED_LUMINANCE:
            ret = GL_LUMINANCE;//GL_COMPRESSED_RGB;
            sret = GL_LUMINANCE;
            break;
        case GL_COMPRESSED_LUMINANCE_ALPHA:
            if (globals4es.nolumalpha) {
                ret = GL_COMPRESSED_RGBA;
                sret = GL_RGBA;
            } else {
                ret = GL_LUMINANCE_ALPHA;
                sret = GL_LUMINANCE_ALPHA;
            }
            break;
        case GL_COMPRESSED_RGB:
            sret = GL_RGB;
            break;
        case GL_COMPRESSED_RGBA:
            sret = GL_RGBA;
            break;
        case GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_S3TC_DXT1_EXT:  // should be sRGB...
            ret = GL_COMPRESSED_RGB;
            sret = GL_RGB;
            break;
        case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:  // not good...
        case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:  // not good, but there is no DXT3 compressor
        case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT:
            ret = GL_COMPRESSED_RGBA;
            sret = GL_RGBA;
            break;
        case GL_BGRA:
            if(hardext.bgra8888) {
                ret = GL_BGRA;
                sret = GL_BGRA;
            } else {
                ret = GL_RGBA;
                sret = GL_RGBA;
            }
            break;
        case GL_DEPTH_COMPONENT:
            if(hardext.depthtex) {
                sret = ret = GL_DEPTH_COMPONENT;
            } else {
                sret = ret = GL_RGBA;
            }
            break;
        case GL_DEPTH_COMPONENT16:
            if(hardext.depthtex) {
                sret = ret = GL_DEPTH_COMPONENT;
            } else {
                sret = ret = GL_RGBA;
            }
            break;
        case GL_DEPTH_COMPONENT32:
            if(hardext.depthtex) {
                sret = ret = GL_DEPTH_COMPONENT;
            } else {
                sret = ret = GL_RGBA;
            }
            break;
        case GL_DEPTH24_STENCIL8:
            if(hardext.depthtex) {
                sret = ret = GL_DEPTH_STENCIL;
            } else {
                sret = ret = GL_RGBA;
            }
            break;
        case GL_STENCIL_INDEX8:
            if(hardext.stenciltex) {
                sret = ret = GL_STENCIL_INDEX8;
            } else {
                sret = ret = (hardext.rgtex)?GL_RED:GL_LUMINANCE;
            }
            break;
        default:
            ret = GL_RGBA;
            sret = GL_RGBA;
            break;
        // Default...RGBA / RGBA will be fine....
    }
    *internalformat = ret;
    return sret;
}

static int get_shrinklevel(int width, int height, int level) {
    int shrink = 0;
    int mipwidth = width << level;
    int mipheight = height << level;
    switch(globals4es.texshrink) {
        case 0: // nothing
            break;
        case 1: //everything / 2
            if ((mipwidth > 1) && (mipheight > 1)) {
                shrink = 1;
            }
            break;
        case 2: //only > 512 /2
        case 7: //only > 512 /2 , but not for empty texture
            if (((mipwidth%2==0) && (mipheight%2==0)) && (
                ((mipwidth > 512) && (mipheight > 8)) || ((mipheight > 512) && (mipwidth > 8))) ) {
                shrink = 1;
            }
            break;
        case 3: //only > 256 /2
            if (((mipwidth%2==0) && (mipheight%2==0)) && (
                ((mipwidth > 256) && (mipheight > 8)) || ((mipheight > 256) && (mipwidth > 8))) ) {
                shrink = 1;
            }
            break;
        case 4: //only > 256 /2, >=1024 /4
            if (((mipwidth%4==0) && (mipheight%4==0)) && (
                ((mipwidth > 256) && (mipheight > 8)) || ((mipheight > 256) && (mipwidth > 8))) ) {
                if ((mipwidth>=1024) || (mipheight>=1024)) {
                    shrink = 2;
                } else {
                    shrink = 1;
                }
            }
            break;
        case 5: //every > 256 is downscaled to 256, but not for empty texture
            if (((mipwidth%4==0) && (mipheight%4==0)) && (
                ((mipwidth > 256) && (mipheight > 8)) || ((mipheight > 256) && (mipwidth > 8))) ) {
                if ((mipwidth>256) || (mipheight>256)) {
                    while (((mipwidth > 256) && (mipheight > 4)) || ((mipheight > 256) && (mipwidth > 4))) {
                        width /= 2;
                        height /= 2;
                        mipwidth /= 2;
                        mipheight /= 2;
                        shrink+=1;
                    }
                } else {
                    shrink = 1;
                }
            }
            break;
        case 6: //only > 128 /2, >=512 is downscaled to 256, but not for empty texture
            if (((mipwidth%2==0) && (mipheight%2==0)) && (
                ((mipwidth > 128) && (mipheight > 8)) || ((mipheight > 128) && (mipwidth > 8))) ) {
                if ((mipwidth>=512) || (mipheight>=512)) {
                    while (((mipwidth > 256) && (mipheight > 8)) || ((mipheight > 256) && (mipwidth > 8))) {
                        width /= 2;
                        height /= 2;
                        mipwidth /= 2;
                        mipheight /= 2;
                        shrink+=1;
                    }
                } else {
                    shrink = 1;
                }
            }
            break;
        case 8: //advertise *4 max texture size, but >2048 are shrinked to 2048
            if ((mipwidth>hardext.maxsize*2) || (mipheight>hardext.maxsize*2)) {
                shrink=2;
            } else
            if ((mipwidth>hardext.maxsize) || (mipheight>hardext.maxsize)) {
                shrink=1;
            }
            break;
        case 9: //advertise 8192 max texture size, but >4096 are quadshrinked and >512 are shrinked, but not for empty texture
            if ((mipwidth>hardext.maxsize*2) || (mipheight>hardext.maxsize*2)) {
                shrink=2;
            } else
            if (mipwidth>(hardext.maxsize>>2) || mipheight>(hardext.maxsize>>2)) {
                shrink=1;
            }
            break;
        case 10://advertise 8192 max texture size, but >2048 are quadshrinked and >512 are shrinked, but not for empty texture
            if ((mipwidth>hardext.maxsize) || (mipheight>hardext.maxsize)) {
                shrink=2;
            } else
            if ((mipwidth>(hardext.maxsize>>2)) || (mipheight>(hardext.maxsize>>2))) {
                shrink=1;
            }
            break;
        case 11:// scale down to maxres any dimension > maxres
            if(mipwidth>hardext.maxsize || mipheight>hardext.maxsize)
                shrink=1;
            break;
        }
        
    return shrink;
} 

int wrap_npot(GLenum wrap) {
    switch(wrap) {
        case 0: return (globals4es.defaultwrap)?1:0;
        case GL_CLAMP:
        case GL_CLAMP_TO_EDGE:
        case GL_CLAMP_TO_BORDER:
            return 1;
    }
    return 0;
}
int minmag_npot(GLenum mag) {
    switch(mag) {
        case 0: return 0;   // default is not good
        case GL_NEAREST:
        case GL_LINEAR:
            return 1;
    }
    return 0;
}

GLenum minmag_forcenpot(GLenum filt) {
    switch(filt) {
        case GL_LINEAR:
        case GL_LINEAR_MIPMAP_NEAREST:
        case GL_LINEAR_MIPMAP_LINEAR:
            return GL_LINEAR;
        /*case 0:
        case GL_NEAREST:
        case GL_NEAREST_MIPMAP_NEATEST:
        case GL_NEAREST_MIPMAP_LINEAR:*/
        default:
            return GL_NEAREST;
    }
}
GLenum wrap_forcenpot(GLenum wrap) {
    switch(wrap) {
        case 0: return GL_CLAMP_TO_EDGE;
        case GL_CLAMP:
        case GL_CLAMP_TO_EDGE:
        case GL_CLAMP_TO_BORDER:
            return wrap;
        /*case GL_MIRROR_CLAMP_TO_EDGE_EXT:
            return wrap;*/
    }
    return GL_CLAMP_TO_EDGE;
}

GLenum minmag_float(GLenum filt) {
    switch(filt) {
        case GL_LINEAR:
            return GL_NEAREST;
        case GL_LINEAR_MIPMAP_NEAREST:
        case GL_LINEAR_MIPMAP_LINEAR:
        case GL_NEAREST_MIPMAP_LINEAR:
            return GL_NEAREST_MIPMAP_NEAREST;
        default:
            return filt;
    }
}

void APIENTRY_GL4ES gl4es_glTexImage2D(GLenum target, GLint level, GLint internalformat,
                  GLsizei width, GLsizei height, GLint border,
                  GLenum format, GLenum type, const GLvoid *data) {
    DBG(printf("glTexImage2D on target=%s with unpack_row_length(%i), size(%i,%i) and skip(%i,%i), format(internal)=%s(%s), type=%s, data=%p, level=%i (mipmap_need=%i, mipmap_auto=%i, base_level=%i, max_level=%i) => texture=%u (streamed=%i), glstate->list.compiling=%d\n", PrintEnum(target), glstate->texture.unpack_row_length, width, height, glstate->texture.unpack_skip_pixels, glstate->texture.unpack_skip_rows, PrintEnum(format), (internalformat==3)?"3":(internalformat==4?"4":PrintEnum(internalformat)), PrintEnum(type), data, level, glstate->texture.bound[glstate->texture.active][what_target(target)]->mipmap_need, glstate->texture.bound[glstate->texture.active][what_target(target)]->mipmap_auto, glstate->texture.bound[glstate->texture.active][what_target(target)]->base_level, glstate->texture.bound[glstate->texture.active][what_target(target)]->max_level, glstate->texture.bound[glstate->texture.active][what_target(target)]->texture, glstate->texture.bound[glstate->texture.active][what_target(target)]->streamed, glstate->list.compiling);)
    
    if(data==NULL && (internalformat == GL_RGB16F || internalformat == GL_RGBA16F))
    internal2format_type(internalformat, &format, &type);

    if(internalformat == GL_R16F ) internal2format_type(internalformat, &format, &type);

    if(data==NULL && (internalformat == GL_RED || internalformat == GL_RGB))
        internal2format_type(internalformat, &format, &type);
    
    // proxy case
    const GLuint itarget = what_target(target);
    const GLuint rtarget = map_tex_target(target);
    LOAD_GLES(glTexImage2D);
    LOAD_GLES(glTexSubImage2D);
    void gles_glTexParameteri(glTexParameteri_ARG_EXPAND); //LOAD_GLES(glTexParameteri);

    if(globals4es.force16bits) {
        if(internalformat==GL_RGBA || internalformat==4 || internalformat==GL_RGBA8)
            internalformat = GL_RGBA4;
        else if(internalformat==GL_RGB || internalformat==3 || internalformat==GL_RGB8)
            internalformat = GL_RGB5;
    }

    if (rtarget == GL_PROXY_TEXTURE_2D) {
        int max1=hardext.maxsize;
        glstate->proxy_width = ((width<<level)>max1)?0:width;
        glstate->proxy_height = ((height<<level)>max1)?0:height;
        glstate->proxy_intformat = swizzle_internalformat((GLenum *) &internalformat, format, type);
        return;
    }
    // actually bound if targeting shared TEX2D
    realize_bound(glstate->texture.active, target);

    if (glstate->list.pending) {
        gl4es_flush();
    } else {
        PUSH_IF_COMPILING(glTexImage2D);
    }

#ifdef __BIG_ENDIAN__
    if(type==GL_UNSIGNED_INT_8_8_8_8)
#else
    if(type==GL_UNSIGNED_INT_8_8_8_8_REV)
#endif
        type = GL_UNSIGNED_BYTE;

    if(type==GL_HALF_FLOAT)
        type = GL_HALF_FLOAT_OES;

    /*if(format==GL_COMPRESSED_LUMINANCE)
        format = GL_RGB;*/    // Danger from the Deep does that. 
        //That's odd, probably a bug (line 453 of src/texture.cpp, it should be interformat instead of format)

    GLvoid *datab = (GLvoid*)data;
    
    if (glstate->vao->unpack)
        datab = (char*)datab + (uintptr_t)glstate->vao->unpack->data;
        
    GLvoid *pixels = (GLvoid *)datab;
    border = 0;    //TODO: something?
    noerrorShim();

    gltexture_t *bound = glstate->texture.bound[glstate->texture.active][itarget];

    //Special case when resizing an attached to FBO texture, that is attached to depth and/or stencil => resizing is specific then
    if(bound->binded_fbo && (bound->binded_attachment==GL_DEPTH_ATTACHMENT || bound->binded_attachment==GL_STENCIL_ATTACHMENT || bound->binded_attachment==GL_DEPTH_STENCIL_ATTACHMENT))
    {
        // non null data should be handled, but need to convert then...
        if(data!=NULL) 
            printf("LIBGL: Warning, Depth/stencil texture resized and with data\n");
        // get new size...
        GLsizei nheight = (hardext.npot)?height:npot(height);
        GLsizei nwidth = (hardext.npot)?width:npot(width);
        bound->npot = (nheight!=height || nwidth!=width);
        bound->nwidth = nwidth;
        bound->nheight = nheight;
        bound->width = width;
        bound->height = height;
        //resize depth texture of renderbuffer?
        if(bound->binded_attachment==GL_DEPTH_ATTACHMENT || bound->binded_attachment==GL_DEPTH_STENCIL_ATTACHMENT)
        {
            if(bound->renderdepth) {
                gl4es_glBindRenderbuffer(GL_RENDERBUFFER, bound->renderdepth);
                gl4es_glRenderbufferStorage(GL_RENDERBUFFER, (bound->binded_attachment==GL_DEPTH_ATTACHMENT)?GL_DEPTH_COMPONENT16:GL_DEPTH24_STENCIL8, nwidth, nheight);
                gl4es_glBindRenderbuffer(GL_RENDERBUFFER, 0);
            } else {
                gles_glTexImage2D(GL_TEXTURE_2D, 0, bound->format, bound->nwidth, bound->nheight, 0, bound->format, bound->type, NULL);
            }
        }
        if((bound->binded_attachment==GL_STENCIL_ATTACHMENT || bound->binded_attachment==GL_DEPTH_STENCIL_ATTACHMENT) && bound->renderstencil)
        {
            gl4es_glBindRenderbuffer(GL_RENDERBUFFER, bound->renderstencil);
            gl4es_glRenderbufferStorage(GL_RENDERBUFFER, GL_STENCIL_INDEX8, nwidth, nheight);
            gl4es_glBindRenderbuffer(GL_RENDERBUFFER, 0);
        }
        // all done, exit
        errorGL();
        return;
    }

    if(target == GL_TEXTURE_RECTANGLE_ARB) {
        // change sampler state
        bound->sampler.min_filter = minmag_forcenpot(bound->sampler.min_filter);
        bound->sampler.wrap_s = wrap_forcenpot(bound->sampler.wrap_s);
        bound->sampler.wrap_t = wrap_forcenpot(bound->sampler.wrap_t);
        bound->sampler.wrap_r = wrap_forcenpot(bound->sampler.wrap_r);
    }


    bound->alpha = pixel_hasalpha(format);
    // fpe internal format tracking
    if(glstate->fpe_state) {
        switch (internalformat) {
            case GL_COMPRESSED_ALPHA:
            case GL_ALPHA4:
            case GL_ALPHA8:
            case GL_ALPHA16:
            case GL_ALPHA16F:
            case GL_ALPHA32F:
            case GL_ALPHA:
                bound->fpe_format = FPE_TEX_ALPHA;
                break;
            case 1:
            case GL_COMPRESSED_LUMINANCE:
            case GL_LUMINANCE4:
            case GL_LUMINANCE8:
            case GL_LUMINANCE16:
            case GL_LUMINANCE16F:
            case GL_LUMINANCE32F:
            case GL_LUMINANCE:
                bound->fpe_format = FPE_TEX_LUM;
                break;
            case 2:
            case GL_COMPRESSED_LUMINANCE_ALPHA:
            case GL_LUMINANCE4_ALPHA4:
            case GL_LUMINANCE8_ALPHA8:
            case GL_LUMINANCE16_ALPHA16:
            case GL_LUMINANCE_ALPHA16F:
            case GL_LUMINANCE_ALPHA32F:
            case GL_LUMINANCE_ALPHA:
                bound->fpe_format = FPE_TEX_LUM_ALPHA;
                break;
            case GL_COMPRESSED_INTENSITY:
            case GL_INTENSITY8:
            case GL_INTENSITY16:
            case GL_INTENSITY16F:
            case GL_INTENSITY32F:
            case GL_INTENSITY:
                bound->fpe_format = FPE_TEX_INTENSITY;
                break;
            case 3:
            case GL_RED:
            case GL_RG:
            case GL_RGB:
            case GL_RGB5:
            case GL_RGB565:
            case GL_RGB8:
            case GL_RGB16:
            case GL_RGB16F:
            case GL_RGB32F:
            case GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
            case GL_COMPRESSED_SRGB_S3TC_DXT1_EXT:
            case GL_COMPRESSED_RGB:
                bound->fpe_format = FPE_TEX_RGB;
                break;
            /*case GL_DEPTH_COMPONENT:
            case GL_DEPTH_COMPONENT16:
            case GL_DEPTH_COMPONENT24:
            case GL_DEPTH_COMPONENT32:
            case GL_DEPTH_STENCIL:
            case GL_DEPTH24_STENCIL8:
                bound->fpe_format = FPE_TEX_COMPONENT;*/
            default:
                bound->fpe_format = FPE_TEX_RGBA;
        }
    }
    if (globals4es.automipmap) {
        if (level>0)
            if ((globals4es.automipmap==1) || (globals4es.automipmap==3) || bound->mipmap_need) {
                return;            // has been handled by auto_mipmap
            }
            else if(globals4es.automipmap==2)
                bound->mipmap_need = 1;
    }
    if(level>0 && (bound->npot && globals4es.forcenpot))
        return;         // no mipmap...
    if (level==0 || !bound->valid) {
        bound->wanted_internal = internalformat;    // save it before transformation
    }
    GLenum new_format = swizzle_internalformat((GLenum *) &internalformat, format, type);
    if (level==0 || !bound->valid) {
        bound->orig_internal = internalformat;
        bound->internalformat = new_format;
    }
    // shrink checking
    int mipwidth = width << level;
    int mipheight = height << level;
    int shrink = 0;
    if(!bound->valid)
        bound->shrink = shrink = get_shrinklevel(width, height, level);
    else
        shrink = bound->shrink;

    if(((width>>shrink)==0) && ((height>>shrink)==0)) return;   // nothing to do
    if (datab) {

        // implements GL_UNPACK_ROW_LENGTH
        if ((glstate->texture.unpack_row_length && glstate->texture.unpack_row_length != width) 
        || glstate->texture.unpack_skip_pixels || glstate->texture.unpack_skip_rows) {
            int imgWidth, pixelSize, dstWidth;
            pixelSize = pixel_sizeof(format, type);
            imgWidth = ((glstate->texture.unpack_row_length)? glstate->texture.unpack_row_length:width) * pixelSize;
            GLubyte *dst = (GLubyte *)malloc(width * height * pixelSize);
            pixels = (GLvoid *)dst;
            dstWidth = width * pixelSize;
            const GLubyte *src = (GLubyte *)datab;
            src += glstate->texture.unpack_skip_pixels * pixelSize + glstate->texture.unpack_skip_rows * imgWidth;
            for (int y = height; y; --y) {
                memcpy(dst, src, dstWidth);
                src += imgWidth;
                dst += dstWidth;
            }
        }

        GLvoid *old = pixels;
        pixels = (GLvoid *)swizzle_texture(width, height, &format, &type, internalformat, new_format, old, bound);
        if (old != pixels && old != datab) {
            free(old);
        }

        if (bound->shrink!=0) {
            switch(globals4es.texshrink) {
            case 0: // nothing ???
                break;
            case 1: //everything / 2
            case 11:
                if ((mipwidth > 1) && (mipheight > 1)) {
                    GLvoid *out = pixels;
                    GLfloat ratiox, ratioy;
                    int newwidth = mipwidth;
                    int newheight = mipheight;
                    if(globals4es.texshrink==11) {
                        if (mipwidth>hardext.maxsize)
                            newwidth = hardext.maxsize;
                        if (mipheight>hardext.maxsize)
                            newheight = hardext.maxsize;
                    } else {
                        newwidth = mipwidth / 2;
                        newheight = mipheight / 2;
                        if(!newwidth) newwidth=1;
                        if(!newheight) newheight=1;
                    }
                    if(level && bound->valid) {
                        // don't recalculate ratio...
                        ratiox = bound->ratiox;
                        ratioy = bound->ratioy;
                    } else {
                        ratiox = newwidth/((float)mipwidth);
                        ratioy = newheight/((float)mipheight);
                    }
                    newwidth = width * ratiox;
                    newheight = height * ratioy;
                    if(ratiox==0.5f && ratioy==0.5f && npot(width)==width && npot(height)==height) {
                        // prefer the fast and clean way first
                        pixel_halfscale(pixels, &out, width, height, format, type);
                    } else {
                        bound->ratiox = ratiox;
                        bound->ratioy = ratioy;
                        bound->useratio = 1;
                        pixel_scale(pixels, &out, width, height, newwidth, newheight, format, type);
                    }
                    if (out != pixels && pixels!=datab)
                        free(pixels);
                    pixels = out;
                    width = newwidth;
                    height = newheight;
                    bound->shrink = 1;
                }
                break;
            default:
                if(!bound->valid)
                    bound->ratiox = bound->ratioy = 1.0f;
                while(shrink) {
                    int toshrink = (shrink>1)?2:1;
                    GLvoid *out = pixels;
                    if(toshrink==1) {
                        pixel_halfscale(pixels, &out, width, height, format, type);
                        if(!bound->valid)
                            { bound->ratiox *= 0.5f; bound->ratioy *= 0.5f; }
                    } else {
                        pixel_quarterscale(pixels, &out, width, height, format, type);
                        if(!bound->valid)
                            { bound->ratiox *= 0.25f; bound->ratioy *= 0.25f; }
                    }
                    if (out != pixels && pixels!=datab)
                        free(pixels);
                    pixels = out;
                    width = nlevel(width, toshrink);
                    height = nlevel(height, toshrink);
                    shrink-=toshrink;
                }
            }
        }
        
        if (globals4es.texdump) {
            pixel_to_ppm(pixels, width, height, format, type, bound->texture, glstate->texture.pack_align);
        }
    } else {
#ifdef TEXSTREAM
        if (globals4es.texstream && (target==GL_TEXTURE_2D || target==GL_TEXTURE_RECTANGLE_ARB) && (width>=256 && height>=224) && 
        ((internalformat==GL_RGB) || (internalformat==3) || (internalformat==GL_RGB8) || (internalformat==GL_BGR) || (internalformat==GL_RGB5) || (internalformat==GL_RGB565)) || (globals4es.texstream==2) ) {
            bound->streamingID = AddStreamed(width, height, bound->texture);
            if (bound->streamingID>-1) {    // success
                bound->shrink = 0;  // no shrink on Stream texture
                bound->streamed = true;
                glsampler_t *sampler = glstate->samplers.sampler[glstate->texture.active];
                if(!sampler)
                    sampler = &bound->sampler;
                ApplyFilterID(bound->streamingID, sampler->min_filter, sampler->mag_filter);
                GLboolean tmp = IS_ANYTEX(glstate->enable.texture[glstate->texture.active]);
                LOAD_GLES(glDisable);
                LOAD_GLES(glEnable);
                if (tmp)
                    gles_glDisable(GL_TEXTURE_2D);
                ActivateStreaming(bound->streamingID);    //Activate the newly created texture
                format = GL_RGB;
                type = GL_UNSIGNED_SHORT_5_6_5;
                if (tmp)
                    gles_glEnable(GL_TEXTURE_STREAM_IMG);
                }
                glstate->bound_stream[glstate->texture.active] = 1;
        }
#endif
        if (!bound->streamed)
            swizzle_texture(width, height, &format, &type, internalformat, new_format, NULL, bound);    // convert format even if data is NULL
        if (bound->shrink!=0) {
            switch(globals4es.texshrink) {
            case 1: //everything / 2
            case 11:
                if ((mipwidth > 1) && (mipheight > 1)) {
                    GLfloat ratiox, ratioy;
                    if(globals4es.texshrink==11) {
                        if (mipwidth>hardext.maxsize)
                            ratiox = hardext.maxsize/((float)mipwidth);
                        else
                            ratiox = 1.0f;
                        if (mipheight>hardext.maxsize)
                            ratioy = hardext.maxsize/((float)mipheight);
                        else
                            ratioy = 1.0f;
                    } else 
                        ratiox = ratioy = 0.5;
                    bound->ratiox = ratiox;
                    bound->ratioy = ratioy;
                    bound->useratio = 1;
                    int newwidth = width*bound->ratiox;
                    int newheight = height*bound->ratioy;
                    if(globals4es.texshrink==11 && newwidth>hardext.maxsize) newwidth=hardext.maxsize; // in case of some rounding error
                    if(globals4es.texshrink==11 && newheight>hardext.maxsize) newheight=hardext.maxsize; // in case of some rounding error
                    width = newwidth;
                    height = newheight;
                    bound->shrink = 1;
                }
                break;
            default:
                bound->ratiox = bound->ratioy = 1.0f;
                while(shrink) {
                    int toshrink = (shrink>1)?2:1;
                    if(toshrink==1) {
                        bound->ratiox *= 0.5f; bound->ratioy *= 0.5f;
                    } else {
                        bound->ratiox *= 0.25f; bound->ratioy *= 0.25f;
                    }
                    width = nlevel(width, toshrink);
                    height = nlevel(height, toshrink);
                    shrink-=toshrink;
                }
            }
        }
    }
    
    /* TODO:
    GL_INVALID_VALUE is generated if border is not 0.
    GL_INVALID_OPERATION is generated if type is
    GL_UNSIGNED_SHORT_5_6_5 and format is not GL_RGB.
    
    GL_INVALID_OPERATION is generated if type is one of
    GL_UNSIGNED_SHORT_4_4_4_4, or GL_UNSIGNED_SHORT_5_5_5_1
    and format is not GL_RGBA.
    */

    int limitednpot = 0;
    {
        GLsizei nheight = (hardext.npot==3)?height:npot(height);
        GLsizei nwidth = (hardext.npot==3)?width:npot(width);
        bound->npot = (nheight!=height || nwidth!=width);    // hardware that fully support NPOT doesn't care anyway
        if(bound->npot) {
            if(target==GL_TEXTURE_RECTANGLE_ARB && hardext.npot)
                limitednpot=1;
            else if(hardext.npot==1 && (
                    (bound->base_level<=0 && bound->max_level==0) 
                 || (globals4es.automipmap==3) 
                 || (globals4es.automipmap==4 && width!=height) 
                 || (globals4es.forcenpot==1) ) 
                 && (wrap_npot(bound->sampler.wrap_s) && wrap_npot(bound->sampler.wrap_t)) )
                 limitednpot=1;
            else if(hardext.esversion>1 && hardext.npot==1 
                && (!bound->mipmap_auto || !minmag_npot(bound->sampler.min_filter) || !minmag_npot(bound->sampler.mag_filter)) 
                && (wrap_npot(bound->sampler.wrap_s) && wrap_npot(bound->sampler.wrap_t)) )
                limitednpot=1;
            else if(hardext.esversion>1 && hardext.npot==2
                && (wrap_npot(bound->sampler.wrap_s) && wrap_npot(bound->sampler.wrap_t)) )
                limitednpot=1;

            if(limitednpot) {
                nwidth = width;
                nheight = height;
            }
        }
#ifdef PANDORA
#define NO_1x1
#endif
#ifdef NO_1x1
        #define MIN_SIZE 2
        if(level==0 && hardext.esversion==1) {
            if(nwidth < MIN_SIZE && nheight < MIN_SIZE) {nwidth=MIN_SIZE; nheight=MIN_SIZE;}
        }
        #undef MIN_SIZE
#endif
        if (globals4es.texstream && bound->streamed) {
            nwidth = width;
            nheight = height;
        }

        if(bound->npot) {
            if(limitednpot && rtarget==GL_TEXTURE_2D) {
                bound->sampler.wrap_t = bound->sampler.wrap_s = GL_CLAMP_TO_EDGE;
            } else if (!wrap_npot(bound->sampler.wrap_s) || !wrap_npot(bound->sampler.wrap_t)) {
                // resize to npot boundaries (not ideal if the wrap value is change after upload of the texture)
                if(level==0 || bound->width==0) {
                    nwidth =  npot(width);
                    nheight = npot(height);
                } else {
                    nwidth =  npot(nlevel(bound->width, level));
                    nheight = npot(nlevel(bound->height, level));
                }
                float ratiox, ratioy;
                ratiox = ((float)nwidth)/width;
                ratioy = ((float)nheight)/height;

                GLvoid *out = pixels;
                if(pixels)
                    pixel_scale(pixels, &out, width, height, nwidth, nheight, format, type);
                if (out != pixels && pixels!=datab)
                    free(pixels);
                pixels = out;
                width = nwidth;
                height = nheight;
                limitednpot = 0;

                if(level==0) {
                    if(!bound->useratio) {
                        bound->useratio = 1;
                        if(bound->ratiox==0.f) bound->ratiox = bound->ratioy = 1.0f;
                    }
                    bound->ratiox *= ratiox;
                    bound->ratioy *= ratioy;
                    bound->npot = 0;
                    bound->shrink = 1;
                }
            }
        }
        if ((globals4es.automipmap==4) && (nwidth!=nheight))
            bound->mipmap_auto = 0;

        if (level == 0) {
            bound->width = width;
            bound->height = height;
            bound->nwidth = nwidth;
            bound->nheight = nheight;
            if(target==GL_TEXTURE_RECTANGLE_ARB && hardext.esversion==2) {
                bound->adjust = 0;  // because this test is used in a lot of places
                bound->adjustxy[0] = 1.0f/width;
                bound->adjustxy[1] = 1.0f/height;
            } else {
                // TEXTURE_RECTANGLE could be mutualize with npot texture probably
                bound->adjust = (width!=nwidth || height!=nheight);
                bound->adjustxy[0] = (float)width / nwidth;
                bound->adjustxy[1] = (float)height / nheight;
            }
            bound->compressed = 0;
            bound->valid = 1;
        }

        int callgeneratemipmap = 0;
        if (!(globals4es.texstream && bound->streamed)) {
            if ((target!=GL_TEXTURE_RECTANGLE_ARB) && (globals4es.automipmap!=3) && (bound->mipmap_need || bound->mipmap_auto) && !(bound->npot && hardext.npot<2) && (bound->max_level==-1)) {
                if(hardext.esversion<2)
                    gles_glTexParameteri( rtarget, GL_GENERATE_MIPMAP, GL_TRUE );
                else
                    callgeneratemipmap = 1;
            } else {
                //if(target!=GL_TEXTURE_RECTANGLE_ARB) gles_glTexParameteri( rtarget, GL_GENERATE_MIPMAP, GL_FALSE );
                if ((itarget!=ENABLED_CUBE_MAP && target!=GL_TEXTURE_RECTANGLE_ARB) 
                && (bound->mipmap_need || globals4es.automipmap==3)) {
                    // remove the need for mipmap...
                    bound->mipmap_need = 0;
                    gl4es_glTexParameteri(rtarget, GL_TEXTURE_MIN_FILTER, bound->sampler.min_filter); // forcing min_filter to be recomputed
                }
            }
            
            if (height != nheight || width != nwidth) {
                errorGL();
                gles_glTexImage2D(rtarget, level, format, nwidth, nheight, border,
                                format, type, NULL);
                DBG(CheckGLError(1);)
                if (pixels) {
                    gles_glTexSubImage2D(rtarget, level, 0, 0, width, height,
                                        format, type, pixels);
                                        DBG(CheckGLError(1);)
                }
#ifdef NO_1x1
                if(level==0 && (width==1 && height==1 && pixels)) {
                    // complete the texture, juste in case it use GL_REPEAT
                    // also, don't keep the fact we have resized, the non-adjusted coordinates will work (as the texture is enlarged)
                    if(width==1) {gles_glTexSubImage2D(rtarget, level, 1, 0, width, height, format, type, pixels); nwidth=1;}
                    if(height==1) {gles_glTexSubImage2D(rtarget, level, 0, 1, width, height, format, type, pixels); nheight=1;}
                    if(width==1 && height==1) {   // create a manual mipmap just in case_state
                        gles_glTexSubImage2D(rtarget, level, 1, 1, width, height, format, type, pixels);
                        gles_glTexImage2D(rtarget, 1, format, 1, 1, 0, format, type, pixels);
                    }
                }
#endif
            } else {
                errorGL();
                gles_glTexImage2D(rtarget, level, format, width, height, border,
                                format, type, pixels);
                DBG(CheckGLError(1);)
            }
            // check if base_level is set... and calculate lower level mipmap
            if(bound->base_level == level && !(bound->max_level==level && level==0)) {
                int leveln = level, nw = width, nh = height, nww=nwidth, nhh=nheight;
                int pot = (nh==nhh && nw==nww);
                void *ndata = pixels;
                while(leveln) {
                    if(pixels) {
                        GLvoid *out = ndata;
                        pixel_doublescale(ndata, &out, nw, nh, format, type);
                        if (out != ndata && ndata!=pixels)
                            free(ndata);
                        ndata = out;
                    }
                    nw <<= 1;
                    nh <<= 1;
                    nww <<= 1;
                    nhh <<= 1;
                    --leveln;
                    gles_glTexImage2D(rtarget, leveln, format, nww, nhh, border,
                                    format, type, (pot)?ndata:NULL);
                    if(!pot && pixels) gles_glTexSubImage2D(rtarget, leveln, 0, 0, nw, nh,
                                        format, type, ndata);
                }
                if (ndata!=pixels)
                    free(ndata);
            }
            if(globals4es.automipmap==5 && !level)
                bound->mipmap_done = 0;
            // check if max_level is set... and calculate higher level mipmap
            if(((bound->max_level == level && (level || bound->mipmap_need)) || (callgeneratemipmap && level==0) || (globals4es.automipmap==5 && level && !bound->mipmap_done)) && !(bound->max_level==bound->base_level && bound->max_level==0)) {
                if(globals4es.automipmap==5 && level==1)
                    bound->mipmap_done = 1;
                int leveln = level, nw = nwidth, nh = nheight, nww=width, nhh=height;
                int pot = (nh==nhh && nw==nww);
                void *ndata = pixels;
                while(nw!=1 || nh!=1) {
                    if(pixels) {
                        GLvoid *out = ndata;
                        pixel_halfscale(ndata, &out, nww, nhh, format, type);
                        if (out != ndata && ndata!=pixels)
                            free(ndata);
                        ndata = out;
                    }
                    nw = nlevel(nw, 1);
                    nh = nlevel(nh, 1);
                    nww = nlevel(nww, 1);
                    nhh = nlevel(nhh, 1);
                    ++leveln;
                    gles_glTexImage2D(rtarget, leveln, format, nw, nh, border,
                                    format, type, (pot)?ndata:NULL);
                    if(!pot && pixels) gles_glTexSubImage2D(rtarget, leveln, 0, 0, nww, nhh,
                                        format, type, ndata);
                }
                if (ndata!=pixels)
                    free(ndata);
            }
        /*if (bound && bound->mipmap_need && !bound->mipmap_auto && (globals4es.automipmap!=3))
            gles_glTexParameteri( rtarget, GL_GENERATE_MIPMAP, GL_FALSE );*/
        } else {
            if (pixels)
                gl4es_glTexSubImage2D(rtarget, level, 0, 0, width, height, format, type, pixels);    // (should never happens) updload the 1st data...
        }
        if(bound->mipmap_need && !bound->mipmap_done) {
            if(bound->mipmap_auto)
                bound->mipmap_done = 1;
            if(level == maxlevel(bound->width, bound->height))
                bound->mipmap_done = 1;
        }
    }
    if ((target==GL_TEXTURE_2D) && globals4es.texcopydata && ((globals4es.texstream && !bound->streamed) || !globals4es.texstream)) {
        if (bound->data) 
            bound->data=realloc(bound->data, width*height*4);
        else 
            bound->data = malloc(width*height*4);
        if (datab) {
            if (!pixel_convert(pixels, &bound->data, width, height, format, type, GL_RGBA, GL_UNSIGNED_BYTE, 0, glstate->texture.unpack_align))
                printf("LIBGL: Error on pixel_convert when TEXCOPY in glTexImage2D\n");
        } else {
        //memset(bound->data, 0, width*height*4);
        }
    }
    if (pixels != datab) {
        free(pixels);
    }
    // update max bound to be sure "sampler" is applied
    if(glstate->bound_changed<glstate->texture.active+1)
        glstate->bound_changed = glstate->texture.active+1;
}

void APIENTRY_GL4ES gl4es_glTexSubImage2D(GLenum target, GLint level, GLint xoffset, GLint yoffset,
                     GLsizei width, GLsizei height, GLenum format, GLenum type,
                     const GLvoid *data) {

    if (glstate->list.pending) {
        gl4es_flush();
    } else {
        PUSH_IF_COMPILING(glTexSubImage2D);
    }
    realize_bound(glstate->texture.active, target);

#ifdef __BIG_ENDIAN__
    if(type==GL_UNSIGNED_INT_8_8_8_8)
#else
    if(type==GL_UNSIGNED_INT_8_8_8_8_REV)
#endif
        type = GL_UNSIGNED_BYTE;

    
    GLvoid *datab = (GLvoid*)data;
    if (glstate->vao->unpack)
        datab = (char*)datab + (uintptr_t)glstate->vao->unpack->data;
    GLvoid *pixels = (GLvoid*)datab;

    const GLuint itarget = what_target(target);
    const GLuint rtarget = map_tex_target(target);

    LOAD_GLES(glTexSubImage2D);
    void gles_glTexParameteri(glTexParameteri_ARG_EXPAND); //LOAD_GLES(glTexParameteri);
    noerrorShim();
    DBG(printf("glTexSubImage2D on target=%s with unpack_row_length(%d), size(%d,%d), pos(%d,%d) and skip={%d,%d}, format=%s, type=%s, level=%d(base=%d, max=%d), mipmap={need=%d, auto=%d}, texture=%u, data=%p(vao=%p)\n", PrintEnum(target), glstate->texture.unpack_row_length, width, height, xoffset, yoffset, glstate->texture.unpack_skip_pixels, glstate->texture.unpack_skip_rows, PrintEnum(format), PrintEnum(type), level, glstate->texture.bound[glstate->texture.active][itarget]->base_level, glstate->texture.bound[glstate->texture.active][itarget]->max_level, glstate->texture.bound[glstate->texture.active][itarget]->mipmap_need, glstate->texture.bound[glstate->texture.active][itarget]->mipmap_auto, glstate->texture.bound[glstate->texture.active][itarget]->texture, data, glstate->vao->unpack);)
    if (width==0 || height==0) {
        return;
    }
    
    gltexture_t *bound = glstate->texture.bound[glstate->texture.active][itarget];
    if (globals4es.automipmap) {
        if (level>0)
            if ((globals4es.automipmap==1) || (globals4es.automipmap==3) || bound->mipmap_need) {
                return;            // has been handled by auto_mipmap
            }
            else
                bound->mipmap_need = 1;
    } else
    if(level && bound->mipmap_auto)
        return;

    if ((glstate->texture.unpack_row_length && glstate->texture.unpack_row_length != width) || glstate->texture.unpack_skip_pixels || glstate->texture.unpack_skip_rows) {
        int imgWidth, pixelSize, dstWidth;
        pixelSize = pixel_sizeof(format, type);
        imgWidth = ((glstate->texture.unpack_row_length)? glstate->texture.unpack_row_length:width) * pixelSize;
        GLubyte *dst = (GLubyte *)malloc(width * height * pixelSize);
        pixels = (GLvoid *)dst;
        dstWidth = width * pixelSize;
        const GLubyte *src = (GLubyte *)datab;
        src += glstate->texture.unpack_skip_pixels * pixelSize + glstate->texture.unpack_skip_rows * imgWidth;
        for (int y = height; y; --y) {
            memcpy(dst, src, dstWidth);
            src += imgWidth;
            dst += dstWidth;
        }
    }
    
    GLvoid *old = pixels;
#ifdef TEXSTREAM
    if (globals4es.texstream && (bound->streamed)) {
        // Optimisation, let's do convert directly to the right place...
        GLvoid *tmp = GetStreamingBuffer(bound->streamingID);
        tmp += (yoffset*bound->width+xoffset)*2;
        if (! pixel_convert(old, &tmp, width, height,
                        format, type, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, bound->width, glstate->texture.unpack_align)) {
            printf("LIBGL: swizzle error: (%#4x, %#4x -> GL_RGB, UNSIGNED_SHORT_5_6_5)\n",
                        format, type);
        }
        format = GL_RGB;
        type = GL_UNSIGNED_SHORT_5_6_5;
    } else  
#endif
    {
        if (!pixel_convert(old, &pixels, width, height, format, type, bound->inter_format, bound->inter_type, 0, glstate->texture.unpack_align)) {
            printf("LIBGL: Error in pixel_convert while glTexSubImage2D\n");
        } else {
            format = bound->inter_format;
            type = bound->inter_type;
            if(bound->inter_format!=bound->format || bound->inter_type!=bound->type) {
                GLvoid* pix2 = pixels;
                if (!pixel_convert(pixels, &pix2, width, height, format, type, bound->format, bound->type, 0, glstate->texture.unpack_align)) {
                    printf("LIBGL: Error in pixel_convert while glTexSubImage2D\n");
                }
                if (pixels != pix2 && pixels != old)
                    free(pixels);
                pixels = pix2;
                format = bound->format;
                type = bound->type;
            }
        }
        
    }
    if (old != pixels && old != datab)
        free(old);

    if (bound->shrink || bound->useratio) {
        // special case for width/height == 1
        if (width==1)
            width+=(xoffset%2);
        if (height==1)
            height+=(yoffset%2);
        if ((width==1) || (height==1)) {
            // nothing to do...
            if (pixels != datab)
                free((GLvoid *)pixels);
            return;
        }
        // ok, now standard cases....
        old = pixels;
        if(bound->useratio) {
            xoffset *= bound->ratiox;
            yoffset *= bound->ratioy;
            int newwidth = width * bound->ratiox;
            int newheight = height * bound->ratioy;
            pixel_scale(pixels, &old, width, height, newwidth, newheight, format, type);
            width = newwidth;
            height = newheight;
            if (old != pixels && pixels!=datab)
                free(pixels);
            pixels = old;
        } else {
            xoffset /= 2*bound->shrink;
            yoffset /= 2*bound->shrink;
            int shrink = bound->shrink;
            while(shrink) {
                int toshrink = (shrink>1)?2:1;
                GLvoid *out = pixels;
                if(toshrink==1)
                    pixel_halfscale(pixels, &old, width, height, format, type);
                else
                    pixel_quarterscale(pixels, &old, width, height, format, type);
                if (old != pixels && pixels!=datab)
                    free(pixels);
                pixels = old;
                width = nlevel(width, toshrink);
                height = nlevel(height, toshrink);
                shrink-=toshrink;
            }
        }
    }

    if (globals4es.texdump) {
        pixel_to_ppm(pixels, width, height, format, type, bound->texture, glstate->texture.pack_align);
    }

    int callgeneratemipmap = 0;
    if ((target!=GL_TEXTURE_RECTANGLE_ARB) && (bound->mipmap_need || bound->mipmap_auto)) {
        if(hardext.esversion<2) {
            //gles_glTexParameteri( rtarget, GL_GENERATE_MIPMAP, GL_TRUE );
        } else
            callgeneratemipmap = 1;
    }
    
    if (globals4es.texstream && bound->streamed) {
/*    // copy the texture to the buffer
    void* tmp = GetStreamingBuffer(bound->streamingID);
    for (int yy=0; yy<height; yy++) {
        memcpy(tmp+((yy+yoffset)*bound->width+xoffset)*2, pixels+(yy*width)*2, width*2);
    }*/
    } else {
        errorGL();
        gles_glTexSubImage2D(rtarget, level, xoffset, yoffset,
                     width, height, format, type, pixels);
        DBG(CheckGLError(1);)
        // check if base_level is set... and calculate lower level mipmap
        if(bound->base_level == level && !(bound->max_level==level && level==0)) {
            int leveln = level, nw = width, nh = height, xx=xoffset, yy=yoffset;
            void *ndata = pixels;
            while(leveln) {
                if(pixels) {
                    GLvoid *out = ndata;
                    pixel_doublescale(ndata, &out, nw, nh, format, type);
                    if (out != ndata && ndata!=pixels)
                        free(ndata);
                    ndata = out;
                }
                nw <<= 1;
                nh <<= 1;
                xx <<= 1;
                yy <<= 1;
                --leveln;
                gles_glTexSubImage2D(rtarget, leveln, xx, yy, nw, nh,
                                    format, type, ndata);
            }
            if (ndata!=pixels)
                free(ndata);
        }
        // check if max_level is set... and calculate higher level mipmap
        int genmipmap = 0;
        if(((bound->max_level == level) && (level || bound->mipmap_need)))
            genmipmap = 1;
        if(callgeneratemipmap && ((level==0) || (level==bound->max_level)))
            genmipmap = 1;
        if((bound->max_level==bound->base_level) && (bound->base_level==0))
            genmipmap = 0;
        if(genmipmap && (globals4es.automipmap!=3)) {
            int leveln = level, nw = width, nh = height, xx=xoffset, yy=yoffset;
            void *ndata = pixels;
            while(nw!=1 || nh!=1) {
                if(pixels) {
                    GLvoid *out = ndata;
                    pixel_halfscale(ndata, &out, nw, nh, format, type);
                    if (out != ndata && ndata!=pixels)
                        free(ndata);
                    ndata = out;
                }
                nw = nlevel(nw, 1);
                nh = nlevel(nh, 1);
                xx = xx>>1;
                yy = yy>>1;
                ++leveln;
                gles_glTexSubImage2D(rtarget, leveln, xx, yy, nw, nh,
                                    format, type, ndata);
            }
            if (ndata!=pixels)
                free(ndata);
        }
    }

    /*if (bound->mipmap_need && !bound->mipmap_auto && (globals4es.automipmap!=3) && (!globals4es.texstream || (globals4es.texstream && !bound->streamed)))
        gles_glTexParameteri( rtarget, GL_GENERATE_MIPMAP, GL_FALSE );*/

    if ((target==GL_TEXTURE_2D) && globals4es.texcopydata && ((globals4es.texstream && !bound->streamed) || !globals4es.texstream)) {
    //printf("*texcopy* glTexSubImage2D, xy=%i,%i, size=%i,%i=>%i,%i, format=%s, type=%s, tex=%u\n", xoffset, yoffset, width, height, bound->width, bound->height, PrintEnum(format), PrintEnum(type), bound->glname);
        GLvoid * tmp = (char*)bound->data + (yoffset*bound->width + xoffset)*4;
        if (!pixel_convert(pixels, &tmp, width, height, format, type, GL_RGBA, GL_UNSIGNED_BYTE, bound->width, glstate->texture.unpack_align))
            printf("LIBGL: Error on pixel_convert while TEXCOPY in glTexSubImage2D\n");
    }

    if (pixels != datab)
        free((GLvoid *)pixels);
}

// 1d stubs
void APIENTRY_GL4ES gl4es_glTexImage1D(GLenum target, GLint level, GLint internalFormat,
                  GLsizei width, GLint border,
                  GLenum format, GLenum type, const GLvoid *data) {

    // TODO: maybe too naive to force GL_TEXTURE_2D here?
    gl4es_glTexImage2D(GL_TEXTURE_1D, level, internalFormat, width, 1,
                 border, format, type, data);
}
void APIENTRY_GL4ES gl4es_glTexSubImage1D(GLenum target, GLint level, GLint xoffset,
                     GLsizei width, GLenum format, GLenum type,
                     const GLvoid *data) {

    gl4es_glTexSubImage2D(GL_TEXTURE_1D, level, xoffset, 0,
                    width, 1, format, type, data);
}

GLboolean APIENTRY_GL4ES gl4es_glIsTexture(GLuint texture) {
    DBG(printf("glIsTexture(%d):", texture);)
    if(!glstate) {DBG(printf("GL_FALSE\n");) return GL_FALSE;}
    noerrorShim();
    if (!texture) {
        DBG(printf("%s\n", glstate->texture.zero->valid?"GL_TRUE":"GL_FALSE");)
        return glstate->texture.zero->valid;
    }
    khint_t k;
    khash_t(tex) *list = glstate->texture.list;
    if (! list) {
        DBG(printf("GL_FALSE\n");)
        return GL_FALSE;
    }
    k = kh_get(tex, list, texture);
    gltexture_t *tex = NULL;
    if (k == kh_end(list)) {
        DBG(printf("GL_FALSE\n");)
        return GL_FALSE;
    }
    DBG(printf("GL_TRUE\n");)
    return GL_TRUE;
}

void APIENTRY_GL4ES gl4es_glTexStorage1D(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width)
{
    DBG(printf("glTexStorage1D(%s, %d, %s, %d)\n", PrintEnum(target), levels, PrintEnum(internalformat), width);)
    gl4es_glTexImage1D(target, 0, internalformat, width, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
}
void APIENTRY_GL4ES gl4es_glTexStorage2D(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height)
{
    // (could be implemented in GLES3.0)
    DBG(printf("glTexStorage2D(%s, %d, %s, %d, %d)\n", PrintEnum(target), levels, PrintEnum(internalformat), width, height);)
    if(!levels) {
        noerrorShim();
        return;
    }
    if((internalformat==GL_COMPRESSED_RGB_S3TC_DXT1_EXT || internalformat==GL_COMPRESSED_SRGB_S3TC_DXT1_EXT) 
     && !globals4es.avoid16bits)
        gl4es_glTexImage2D(target, 0, internalformat, width, height, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, NULL);
    else if(((internalformat==GL_COMPRESSED_RGBA_S3TC_DXT1_EXT || internalformat==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT)) 
     && !globals4es.avoid16bits)
        gl4es_glTexImage2D(target, 0, internalformat, width, height, 0, GL_RGBA, GL_UNSIGNED_SHORT_5_5_5_1, NULL);
    else if((internalformat==GL_COMPRESSED_RGBA_S3TC_DXT3_EXT || internalformat==GL_COMPRESSED_RGBA_S3TC_DXT5_EXT 
          || internalformat==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT || internalformat==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT) 
     && !globals4es.avoid16bits)
        gl4es_glTexImage2D(target, 0, internalformat, width, height, 0, GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4, NULL);
    else
        gl4es_glTexImage2D(target, 0, internalformat, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);


    int mlevel = maxlevel(width, height);
    gltexture_t *bound = gl4es_getCurrentTexture(target);
    if(levels>1 && isDXTc(internalformat)) {
        // no mipmap will be uploaded, but they will be calculated from level 0
        bound->mipmap_need = 1;
        bound->mipmap_auto = 1;
        for (int i=1; i<=mlevel; ++i)
            gl4es_glTexImage2D(target, i, internalformat, nlevel(width, i), nlevel(height, i), 0, bound->format, bound->type, NULL);
        noerrorShim();
        return;
    }
    // no more compressed format here...
    if(mlevel>levels-1) {
        bound->max_level = levels-1;
        if(levels>1 && globals4es.automipmap!=3)
            bound->mipmap_need = 1;
    }

    for (int i=1; i<levels; ++i)
        gl4es_glTexImage2D(target, i, internalformat, nlevel(width, i), nlevel(height, i), 0, bound->format, bound->type, NULL);

    noerrorShim();
}


//Direct wrapper
AliasExport(void,glTexImage2D,,(GLenum target, GLint level, GLint internalFormat, GLsizei width, GLsizei height, GLint border, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glTexImage1D,,(GLenum target, GLint level, GLint internalFormat, GLsizei width, GLint border, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glTexSubImage2D,,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glTexSubImage1D,,(GLenum target, GLint level, GLint xoffset, GLsizei width, GLenum format, GLenum type, const GLvoid *data));
AliasExport(GLboolean,glIsTexture,,( GLuint texture ));

// TexStorage
AliasExport(void,glTexStorage1D,,(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width));
AliasExport(void,glTexStorage2D,,(GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height));
