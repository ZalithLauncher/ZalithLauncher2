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
#include "stb_dxt_104.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#else
#define DBG(a)
#endif

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

GLboolean isDXTc(GLenum format) {
    switch (format) {
        case GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
        case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:
        case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
        case GL_COMPRESSED_SRGB_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT:
            return 1;
    }
    return 0;
}

GLboolean isDXTcSRGB(GLenum format) {
    switch (format) {
        case GL_COMPRESSED_SRGB_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT:
            return 1;
    }
    return 0;
}

static GLboolean isDXTcAlpha(GLenum format) {
    switch (format) {
        case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:
        case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT:
            return 1;
    }
    return 0;
}

GLboolean isNotCompressed(GLenum format) {
    switch(format) {
        case GL_RGBA:
        case GL_RGB:
        case GL_RGBA8:
        case GL_RGB8:
        case GL_RGB5:
        case GL_RGB565:
            return true;
    }
    return false;
}

GLenum compressedMinMipmap(GLenum param)
{
    switch(param) {
        case GL_NEAREST_MIPMAP_NEAREST:
        case GL_NEAREST_MIPMAP_LINEAR:
            return GL_NEAREST;
        case GL_LINEAR_MIPMAP_NEAREST:
        case GL_LINEAR_MIPMAP_LINEAR:
            return GL_LINEAR;
        default:
            return param;
    }
}

GLvoid *uncompressDXTc(GLsizei width, GLsizei height, GLenum format, GLsizei imageSize, int transparent0, int* simpleAlpha, int* complexAlpha, const GLvoid *data) {
    // uncompress a DXTc image
    // get pixel size of uncompressed image => fixed RGBA
    int pixelsize = 4;
/*	if (format==COMPRESSED_RGB_S3TC_DXT1_EXT)
        pixelsize = 3;*/
    // check with the size of the input data stream if the stream is in fact uncompressed
    if (imageSize == width*height*pixelsize || data==NULL) {
        // uncompressed stream
        return (GLvoid*)data;
    }
    // alloc memory
    GLvoid *pixels = malloc(((width+3)&~3)*((height+3)&~3)*pixelsize);
    // uncompress loop
    int blocksize;
    switch (format) {
        case GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_S3TC_DXT1_EXT:
        case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT:
            blocksize = 8;
            break;
        case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT:
        case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT:
            blocksize = 16;
            break;
    }
    uintptr_t src = (uintptr_t) data;
    for (int y=0; y<height; y+=4) {
        for (int x=0; x<width; x+=4) {
            switch(format) {
                case GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
                case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:
                case GL_COMPRESSED_SRGB_S3TC_DXT1_EXT:
                case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT:
                    DecompressBlockDXT1(x, y, width, (uint8_t*)src, transparent0, simpleAlpha, complexAlpha, pixels);
                    break;
                case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
                case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT:
                    DecompressBlockDXT3(x, y, width, (uint8_t*)src, transparent0, simpleAlpha, complexAlpha, pixels);
                    break;
                case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
                case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT:
                    DecompressBlockDXT5(x, y, width, (uint8_t*)src, transparent0, simpleAlpha, complexAlpha, pixels);
                    break;
            }
            src+=blocksize;
        }
    }
    return pixels;
}

void APIENTRY_GL4ES gl4es_glCompressedTexImage2D(GLenum target, GLint level, GLenum internalformat,
                            GLsizei width, GLsizei height, GLint border,
                            GLsizei imageSize, const GLvoid *data) 
{
    const GLuint itarget = what_target(target);
    const GLuint rtarget = map_tex_target(target);
    if (target == GL_PROXY_TEXTURE_2D) {
        glstate->proxy_width = (width>hardext.maxsize)?0:width;
        glstate->proxy_height = (height>hardext.maxsize)?0:height;
        return;
    }
    FLUSH_BEGINEND;

    // actually bound if targeting shared TEX2D
    realize_bound(glstate->texture.active, target);

    gltexture_t* bound = glstate->texture.bound[glstate->texture.active][itarget]; 
    DBG(printf("glCompressedTexImage2D on target=%s:%p, level=%d with size(%i,%i), internalformat=%s, imagesize=%i, upackbuffer=%p data=%p\n", PrintEnum(target), bound, level, width, height, PrintEnum(internalformat), imageSize, glstate->vao->unpack?glstate->vao->unpack->data:0, data);)
    // hack...
    if (internalformat==GL_RGBA8)
        internalformat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
    // test if internalformat is not a compressed one
    if ((width<=0) || (height<=0)) {
        noerrorShim();
        return; // nothing to do...
    }

    if(level && (globals4es.automipmap==3)) {
        noerrorShim();
        return; //nothing, mipmap ignored...
    }
    
    glbuffer_t *unpack = glstate->vao->unpack;
    glstate->vao->unpack = NULL;
    GLvoid *datab = (GLvoid*)data;
    if (unpack)
        datab = (char*) + (uintptr_t)unpack->data;
    
    GLenum format = GL_RGBA;
    GLenum type = GL_UNSIGNED_BYTE;
        
    if (isDXTc(internalformat)) {
        if(level && bound->mipmap_auto==1)
            return; // nothing to do
        GLvoid *pixels, *half;
        pixels = half = NULL;
        bound->alpha = (internalformat==GL_COMPRESSED_RGB_S3TC_DXT1_EXT || internalformat==GL_COMPRESSED_SRGB_S3TC_DXT1_EXT)?0:1;
        if(globals4es.nodownsampling==1) {  // will be removed soon, avoid16bits is better
            format = GL_RGBA;
            type = GL_UNSIGNED_BYTE;
        } else {
            if(globals4es.avoid16bits) {
                format = GL_RGBA;
                type = GL_UNSIGNED_BYTE;
            } else {
                format = (internalformat==GL_COMPRESSED_RGB_S3TC_DXT1_EXT || internalformat==GL_COMPRESSED_SRGB_S3TC_DXT1_EXT)?GL_RGB:GL_RGBA;
                type = (internalformat==GL_COMPRESSED_RGB_S3TC_DXT1_EXT || internalformat==GL_COMPRESSED_SRGB_S3TC_DXT1_EXT)?GL_UNSIGNED_SHORT_5_6_5:((internalformat==GL_COMPRESSED_RGBA_S3TC_DXT1_EXT)?GL_UNSIGNED_SHORT_5_5_5_1:GL_UNSIGNED_SHORT_4_4_4_4);
                if(level && bound->valid) {
                    // don't mix type/format along mipmap...
                    format = bound->format;
                    type = bound->type;
                }
            }
        }
        int srgb = isDXTcSRGB(internalformat);
        int simpleAlpha = 0;
        int complexAlpha = 0;
        int transparent0 = (internalformat==GL_COMPRESSED_RGBA_S3TC_DXT1_EXT || internalformat==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT)?1:0;
        if (datab) {
            if ((width&3) || (height&3)) {	// can happens :(
                GLvoid *tmp;
                GLsizei nw=width;
                GLsizei nh=height;
                if (nw<4) nw = 4;
                if (nh<4) nh = 4;
                tmp = uncompressDXTc(nw, nh, internalformat, imageSize, transparent0, &simpleAlpha, &complexAlpha, datab);
                pixels = malloc(4*width*height);
                // crop
                for (int y=0; y<height; y++)
                    memcpy((char*)pixels+y*width*4, (char*)tmp+y*nw*4, width*4);
                free(tmp);
            } else {
                pixels = uncompressDXTc(width, height, internalformat, imageSize, transparent0, &simpleAlpha, &complexAlpha, datab);
            }
            if(srgb)
                pixel_srgb_inplace(pixels, width, height);
            // automaticaly reduce the pixel size
            half=pixels;
            if(!globals4es.nodownsampling && !globals4es.avoid16bits) {
                if(type!=GL_UNSIGNED_BYTE) {
                    // packed, recheck status of alpha & complex alpha...
                    if(simpleAlpha && !complexAlpha) {
                        format = GL_RGBA;
                        type = GL_UNSIGNED_SHORT_5_5_5_1;
                    } else if(complexAlpha || simpleAlpha) {
                        format = GL_RGBA;
                        type = GL_UNSIGNED_SHORT_4_4_4_4;
                    } else {
                        format = GL_RGB;
                        type = GL_UNSIGNED_SHORT_5_6_5;
                    }
                }
                if(level && bound->valid) {
                    // don't mix type/format along mipmap...
                    format = bound->format;
                    type = bound->type;
                }
                if (!pixel_convert(pixels, &half, width, height, GL_RGBA, GL_UNSIGNED_BYTE, format, type, 0, glstate->texture.unpack_align)) {
                    format = GL_RGBA;
                    type = GL_UNSIGNED_BYTE;
                }
            }
        } else {
            if(isDXTcAlpha(internalformat)) {
                simpleAlpha = complexAlpha = 1;
            }
        }
        int oldalign;
        gl4es_glGetIntegerv(GL_UNPACK_ALIGNMENT, &oldalign);
        if (oldalign!=1) 
            gl4es_glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        GLenum new_intformat = (format==GL_RGBA)?GL_COMPRESSED_RGBA:GL_COMPRESSED_RGB;
        if(level && bound && bound->valid)
            new_intformat = (bound->format==GL_RGB)?GL_COMPRESSED_RGB:GL_COMPRESSED_RGBA;
        DBG(printf(" => internalformat=%s (Alpha=%d/%d), %dx%d %s/%s\n\n", PrintEnum(new_intformat), simpleAlpha, complexAlpha, width, height, PrintEnum(format), PrintEnum(type));)
        gl4es_glTexImage2D(target, level, new_intformat, width, height, border, format, type, half);
        // re-update bounded texture info, but not format and type
        bound->alpha = (simpleAlpha||complexAlpha)?1:0;
        bound->compressed = 1;
        bound->wanted_internal = bound->internalformat = internalformat;
        bound->valid = 1;
        if(level) {
            // not automipmap yet? then set it...
            bound->mipmap_need = 1;
            // and upload higher level here...
            int leveln = level, nww=width, nhh=height;
            void *ndata = pixels;
            while(nww!=1 || nhh!=1) {
                GLvoid *out = ndata;
                if(half) {  // half can be null if no data...
                    pixel_halfscale(ndata, &out, nww, nhh, GL_RGBA, GL_UNSIGNED_BYTE);
                    if (out != ndata && ndata!=pixels)
                        free(ndata);
                    ndata = out;
                }
                nww = nlevel(nww, 1);
                nhh = nlevel(nhh, 1);
                if(half)
                    pixel_convert(ndata, &out, nww, nhh, GL_RGBA, GL_UNSIGNED_BYTE, format, type, 0, 1);
                ++leveln;
                gl4es_glTexImage2D(target, leveln, new_intformat, nww, nhh, border, format, type, out);
                if(out!=ndata)
                    free(out);
            }
            bound->mipmap_auto = 1;
        }

        if (oldalign!=1) 
            gl4es_glPixelStorei(GL_UNPACK_ALIGNMENT, oldalign);
        if (half!=pixels)
            free(half);
        if (pixels!=datab)
            free(pixels);
    } else {
        LOAD_GLES(glCompressedTexImage2D);
        bound->alpha = 1;
        bound->format = internalformat;
        bound->type = GL_UNSIGNED_BYTE;
        bound->wanted_internal = bound->internalformat = internalformat;
        bound->compressed = 1;
        bound->valid = 1;
        if (glstate->fpe_state && glstate->fpe_bound_changed < glstate->texture.active+1)
            glstate->fpe_bound_changed = glstate->texture.active+1;
        gles_glCompressedTexImage2D(rtarget, level, internalformat, width, height, border, imageSize, datab);
        errorGL();
    }
    glstate->vao->unpack = unpack;
}

void APIENTRY_GL4ES gl4es_glCompressedTexSubImage2D(GLenum target, GLint level, GLint xoffset, GLint yoffset,
                               GLsizei width, GLsizei height, GLenum format, 
                               GLsizei imageSize, const GLvoid *data) 
{
    const GLuint itarget = what_target(target);
    FLUSH_BEGINEND;

    // actually bound if targeting shared TEX2D
    realize_bound(glstate->texture.active, target);

    gltexture_t *bound = glstate->texture.bound[glstate->texture.active][itarget];
    DBG(printf("glCompressedTexSubImage2D with unpack_row_length(%i), level=%d, size(%i,%i), pos(%i,%i) and skip={%i,%i}, internalformat=%s, imagesize=%i, data=%p, bound=%p, bound:%s/%s\n", glstate->texture.unpack_row_length, level, width, height, xoffset, yoffset, glstate->texture.unpack_skip_pixels, glstate->texture.unpack_skip_rows, PrintEnum(format), imageSize, data, bound, bound?PrintEnum(bound->format):"nil", bound?PrintEnum(bound->type):"nil");)
    glbuffer_t *unpack = glstate->vao->unpack;
    glstate->vao->unpack = NULL;
    GLvoid *datab = (GLvoid*)data;
    if (unpack)
        datab = (char*)datab + (uintptr_t)unpack->data;
    LOAD_GLES(glCompressedTexSubImage2D);
    errorGL();
    int simpleAlpha = 0;
    int complexAlpha = 0;
    int transparent0 = (format==GL_COMPRESSED_RGBA_S3TC_DXT1_EXT || format==GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT)?1:0;
    if (isDXTc(format)) {
        if(level) {
            noerrorShim();
            return;
        }
        int srgb = isDXTcSRGB(format);
        GLvoid *pixels;
        if ((width&3) || (height&3)) {	// can happens :(
            GLvoid *tmp;
            GLsizei nw=width;
            GLsizei nh=height;
            if (nw<4) nw = 4;
            if (nh<4) nh = 4;
            tmp = uncompressDXTc(nw, nh, format, imageSize, transparent0, &simpleAlpha, &complexAlpha, datab);
            pixels = malloc(4*width*height);
            // crop
            for (int y=0; y<height; y++)
                memcpy((char*)pixels+y*width*4, (char*)tmp+y*nw*4, width*4);
            free(tmp);
        } else {
            pixels = uncompressDXTc(width, height, format, imageSize, transparent0, &simpleAlpha, &complexAlpha, datab);
        }
        if(srgb)
            pixel_srgb_inplace(pixels, width, height);
        GLvoid *half=pixels;
        #if 0
        pixel_thirdscale(pixels, &half, width, height, GL_RGBA, GL_UNSIGNED_BYTE);
        int oldalign;
        gl4es_glGetIntegerv(GL_UNPACK_ALIGNMENT, &oldalign);
        if (oldalign!=1) gl4es_glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        gl4es_glTexSubImage2D(target, level, xoffset/2, yoffset/2, width/2, height/2, GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4, half);
        if (oldalign!=1) gl4es_glPixelStorei(GL_UNPACK_ALIGNMENT, oldalign);
        #else
        DBG(printf(" [%d] => (Alpha=%d/%d), %dx%d %s/%s\n\n", bound->glname, simpleAlpha, complexAlpha, width, height, PrintEnum(bound->format), PrintEnum(bound->type));)
        gl4es_glTexSubImage2D(target, level, xoffset, yoffset, width, height, GL_RGBA, GL_UNSIGNED_BYTE, half);
        #endif
        if (half!=pixels)
            free(half);
        if (pixels!=datab)
            free(pixels);
    } else {
        gles_glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, datab);
    }
}

void APIENTRY_GL4ES gl4es_glGetCompressedTexImage(GLenum target, GLint lod, GLvoid *img) {
    //FLUSH_BEGINEND;   //no need on get

    const GLuint itarget = what_target(target); 
    gltexture_t* bound = glstate->texture.bound[glstate->texture.active][itarget];
    DBG(printf("glGetCompressedTexImage(%s, %i, %p), bound=%p, size=%dx%d, bound->orig_internal=%s/wanted_internal=%s\n", PrintEnum(target), lod, img, bound, bound?bound->width:0, bound?bound->height:0, (bound)?PrintEnum(bound->orig_internal):"nil", (bound)?PrintEnum(bound->wanted_internal):"nil");)
    errorShim(GL_INVALID_OPERATION);
    if(bound->orig_internal!=GL_COMPRESSED_RGB && bound->orig_internal!=GL_COMPRESSED_RGBA)
        return;
    int width = nlevel(bound->width,lod);
    int height = nlevel(bound->height,lod);
    int w = nlevel(width,2); w<<=2;
    int h = nlevel(height,2); h<<=2;

    int alpha = (bound->orig_internal==GL_COMPRESSED_RGBA)?1:0;
    int dxt1 = (bound->wanted_internal==GL_COMPRESSED_RGBA_S3TC_DXT1_EXT || bound->wanted_internal==GL_COMPRESSED_RGB_S3TC_DXT1_EXT)?1:0;   // Add SRGB variant?

    int ralpha = (alpha && !dxt1)?1:0;

    glbuffer_t *unpack = glstate->vao->unpack;
    glbuffer_t *pack = glstate->vao->pack;
    glstate->vao->unpack = NULL;
    glstate->vao->pack = NULL;
    GLvoid *datab = (GLvoid*)img;
    if (pack)
        datab = (char*)datab + (uintptr_t)pack->data;

    // alloc the memory for source image and grab the file
    GLuint *src = (GLuint*)malloc(width*height*4);
    gl4es_glGetTexImage(target, lod, GL_RGBA, GL_UNSIGNED_BYTE, (GLvoid*)src);
    GLuint tmp[4*4]; //this is the 4x4 block
    for (int y = 0; y < h; y+=4)
        for (int x = 0; x < w; x+=4) {
            GLuint col = 0;
            for (int i=0; i<16; i++) {
                if(x+(i%4)<width && y+(i/4)<height)
                    col = src[x+(i%4)+(y+(i/4))*width];
                tmp[i] = col;
                if(alpha && dxt1) {
                    // change transparent to RGB = 0
                    for (int i=0; i<16; ++i)
                        if((tmp[i]&0xff000000)!=0xff000000)
                            tmp[i] = 0;
                }
            }
            stb_compress_dxt_block((unsigned char*)datab, (const unsigned char*)tmp, ralpha, STB_DXT_NORMAL);
            datab = (char*)datab + 8*(ralpha+1);
    }
    free(src);

    glstate->vao->unpack = unpack;
    glstate->vao->pack = pack;
    noerrorShim();
    return;
}

void APIENTRY_GL4ES gl4es_glCompressedTexImage1D(GLenum target, GLint level, GLenum internalformat,
                            GLsizei width, GLint border,
                            GLsizei imageSize, const GLvoid *data) {
                                
    gl4es_glCompressedTexImage2D(target, level, internalformat, width, 1, border, imageSize, data);
}

void APIENTRY_GL4ES gl4es_glCompressedTexImage3D(GLenum target, GLint level, GLenum internalformat,
                            GLsizei width, GLsizei height, GLsizei depth, GLint border,
                            GLsizei imageSize, const GLvoid *data) {
                                
    gl4es_glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
}

void APIENTRY_GL4ES gl4es_glCompressedTexSubImage1D(GLenum target, GLint level, GLint xoffset,
                               GLsizei width, GLenum format, 
                               GLsizei imageSize, const GLvoid *data) {

    gl4es_glCompressedTexSubImage2D(target, level, xoffset, 0, width, 1, format, imageSize, data);
}
void APIENTRY_GL4ES gl4es_glCompressedTexSubImage3D(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,
                               GLsizei width, GLsizei height, GLsizei depth, GLenum format, 
                               GLsizei imageSize, const GLvoid *data) {

    gl4es_glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
}

//Direct wrapper
AliasExport(void,glCompressedTexImage2D,,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexImage1D,,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexImage3D,,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLsizei depth, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage2D,,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage1D,,(GLenum target, GLint level, GLint xoffset, GLsizei width, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage3D,,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glGetCompressedTexImage,,(GLenum target, GLint lod, GLvoid *img));

//EXT mapper
AliasExport(void,glCompressedTexImage2D,EXT,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexImage1D,EXT,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexImage3D,EXT,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLsizei depth, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage2D,EXT,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage1D,EXT,(GLenum target, GLint level, GLint xoffset, GLsizei width, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage3D,EXT,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glGetCompressedTexImage,EXT,(GLenum target, GLint lod, GLvoid *img));

//ARB mapper
AliasExport(void,glCompressedTexImage2D,ARB,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexImage1D,ARB,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexImage3D,ARB,(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height, GLsizei depth, GLint border, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage2D,ARB,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage1D,ARB,(GLenum target, GLint level, GLint xoffset, GLsizei width, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glCompressedTexSubImage3D,ARB,(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLsizei imageSize, const GLvoid *data));
AliasExport(void,glGetCompressedTexImage,ARB,(GLenum target, GLint lod, GLvoid *img));

