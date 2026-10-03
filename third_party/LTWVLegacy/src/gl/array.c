#include "array.h"

#include "debug.h"
#include "enum_info.h"
#include "glcase.h"
#include "light.h"
#include "state.h"

static int valid_copy_range(GLsizei skip, GLsizei count) {
    return count > 0 && skip >= 0 && skip < count;
}

GLvoid *copy_gl_array(const GLvoid *src,
                      GLenum from, GLsizei width, GLsizei stride,
                      GLenum to, GLsizei to_width, GLsizei skip, GLsizei count, void* dst) {
    if (!src || !valid_copy_range(skip, count))
        return NULL;
						  
    if (! stride)
        stride = width * gl_sizeof(from);

    const char *unknown_str = "LIBGL: copy_gl_array -> unknown type: %x\n";
    if(!dst) {
        dst = malloc((count-skip) * to_width * gl_sizeof(to));
    }
    GLsizei from_size = gl_sizeof(from) * width;
    if (to_width < width) {
/*        printf("Warning: copy_gl_array: %i < %i\n", to_width, width);
        return NULL;*/
        width = to_width;
    }
    // quick path if just copying same to same and no specific stride involved
    if(stride == (to_width*gl_sizeof(to)) && width==to_width && from==to) {
        memcpy(dst, (char*)src + stride*skip, (count-skip) * to_width * gl_sizeof(to));
        return dst;
    }
						  
    // if stride is weird, we need to be able to arbitrarily shift src
    // so we leave it in a uintptr_t and cast after incrementing
    uintptr_t in = (uintptr_t)src;
    in += stride*skip;
    if (from == to && to_width >= width) {
        GL_TYPE_SWITCH(out, dst, to,
            for (int i = skip; i < count; i++) {
                memcpy(out, (GLvoid *)in, from_size);
                for (int j = width; j < to_width; j++) {
                    out[j] = 0;
                }
                out += to_width;
                in += stride;
            },
            default:
                printf(unknown_str, from);
                return NULL;
        )
    } else {
        GL_TYPE_SWITCH(out, dst, to,
            for (int i = skip; i < count; i++) {
                GL_TYPE_SWITCH(input, in, from,
                    for (int j = 0; j < width; j++) {
                        out[j] = input[j];
                    }
                    for (int j = width; j < to_width; j++) {
                        out[j] = 0;
                    }
                    out += to_width;
                    in += stride;
                ,
                    default:
                        printf(unknown_str, from);
                        return NULL;
                )
            },
            default:
                printf(unknown_str, to);
                return NULL;
        )
    }

    return dst;
}

GLvoid *copy_gl_array_texcoord(const GLvoid *src,
                      GLenum from, GLsizei width, GLsizei stride,
                      GLsizei to_width, GLsizei skip, GLsizei count, void* dst) {
    if (!src || !valid_copy_range(skip, count))
        return NULL;
						  
    if (! stride)
        stride = width * gl_sizeof(from);

    static const char *unknown_str = "LIBGL: copy_gl_array -> unknown type: %x\n";
    if(!dst)
        dst = malloc((count-skip) * to_width * gl_sizeof(GL_FLOAT));
    GLsizei from_size = gl_sizeof(from) * width;
    GLsizei to_elem = gl_sizeof(GL_FLOAT);
    uintptr_t in = (uintptr_t)src;
    in += stride*skip;
    GLfloat* out = (GLfloat*)dst;
    if (from == GL_FLOAT && to_width >= width) {
        for (int i = skip; i < count; i++) {
            GLfloat* input = (GLfloat*)in;
            //memcpy(out, (GLvoid *)in, from_size);   // which one is faster ?
            for (int j = 0; j < width; j++) {
                out[j] = input[j];
            }
            for (int j = width; j < to_width; j++) {
                if(j==3)
                    out[j] = 1.0f;
                else
                    out[j] = 0.0f;
            }
            out += to_width;
            in += stride;
        }
    } else {
        for (int i = skip; i < count; i++) {
            GL_TYPE_SWITCH(input, in, from,
                for (int j = 0; j < width; j++) {
                    out[j] = input[j];
                }
                for (int j = width; j < to_width; j++) {
                    if(j==3)
                        out[j] = 1.0f;
                    else
                        out[j] = 0.0f;
                }
                out += to_width;
                in += stride;
            ,
                default:
                    printf(unknown_str, from);
                    return NULL;
            )
        }
    }

    return dst;
}

GLvoid *copy_gl_array_quickconvert(const GLvoid *src,
                      GLenum from, GLsizei stride,
                      GLsizei skip, GLsizei count, void* dest) {
    if (!src || !valid_copy_range(skip, count))
        return NULL;

    if (! stride)
        stride = 4 * gl_sizeof(from);
    const char *unknown_str = "LIBGL: copy_gl_array_quickconvert -> unknown type: %x\n";
    GLvoid *dst = (dest)?dest:malloc((count-skip) * 4 * gl_sizeof(GL_FLOAT));

    uintptr_t in = (uintptr_t)src;
    in += stride*skip;
    int j;
    
    GLfloat *out = (GLfloat*)dst;
    GL_TYPE_SWITCH2(input, in, from,
        const GLfloat maxf = 1.0f/gl_max_value(from);
        for (int i = skip; i < count; i++)
        ,
                for (j = 0; j < 4; j++) {
                    out[j] = ((GLfloat)input[j])*maxf;
                }
                out += 4;
                in += stride;
        ,
        default:
                printf(unknown_str, from);
                return NULL;
    )
    return dst;
}

GLvoid *copy_gl_array_convert(const GLvoid *src,
                      GLenum from, GLsizei width, GLsizei stride,
                      GLenum to, GLsizei to_width, GLsizei skip, GLsizei count, GLvoid* filler, void* dst) {
    if (!src || !valid_copy_range(skip, count))
        return NULL;

    if (! stride)
        stride = width * gl_sizeof(from);
    GLsizei from_size = gl_sizeof(from) * width;

    if(to==from && width==to_width && stride==(to_width * gl_sizeof(to))) {
        if(!dst) dst = malloc((count-skip) * stride);
        memcpy(dst, (char*)src+stride*skip, (count-skip)*stride);
        return dst;
    }

    if(to==GL_FLOAT && width==to_width && width==4)
        return copy_gl_array_quickconvert(src, from, stride, skip, count, dst);
    if(to==GL_FLOAT && from==GL_FLOAT)
        if(width<4 || *(GLfloat*)filler==1.0f)    // no need to convert
            return copy_gl_array_texcoord(src, from, width, stride, to_width, skip, count, dst);
        else
            return copy_gl_array(src, from, width, stride, to, to_width, skip, count, dst);

    const char *unknown_str = "LIBGL: copy_gl_array_convert -> unknown type: %x\n";
    if(!dst) dst = malloc((count-skip) * to_width * gl_sizeof(to));
    if (to_width < width) {
        /*printf("Warning: copy_gl_array: %i < %i\n", to_width, width);
        return NULL;*/
        width = to_width;
    }

    // if stride is weird, we need to be able to arbitrarily shift src
    // so we leave it in a uintptr_t and cast after incrementing
    uintptr_t in = (uintptr_t)src;
    in += stride*skip;
    int j;
    if (from == to && to_width >= width) {
        GL_TYPE_SWITCH(out, dst, to,
            for (int i = skip; i < count; i++) {
                memcpy(out, (GLvoid *)in, from_size);
                for (j = width; j < to_width-1; j++) {
					out[j]=0;
                }
                for (; j < to_width; j++) {
					memcpy(&out[j], filler, gl_sizeof(to));
                }
                out += to_width;
                in += stride;
            },
            default:
                printf(unknown_str, from);
                return NULL;
        )
    } else {
        GL_TYPE_SWITCH_MAX(out, dst, to,
            GL_TYPE_SWITCH2(input, in, from,
                const GLuint maxf = gl_max_value(from);
                for (int i = skip; i < count; i++)
                ,
                    for (j = 0; j < width; j++) {
                        out[j] = input[j]*maxv/maxf;
                    }
                    for (; j < to_width-1; j++) {
                        out[j]=0;
                    }
                    for (; j < to_width; j++) {
                        memcpy(&out[j], filler, gl_sizeof(to));
                    }
                    out += to_width;
                    in += stride;
            ,
                    default:
                        printf(unknown_str, from);
                        return NULL;
            ),
            default:
                printf(unknown_str, to);
                return NULL;
        )
    }

    return dst;
}

GLvoid *copy_gl_pointer(vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
	float filler = 0.0f;
    return copy_gl_array_convert((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_FLOAT, width, skip, count, &filler, NULL);
}
GLvoid *copy_gl_pointer_color(vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
	float filler = 1.0f;
    return copy_gl_array_convert((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_FLOAT, width, skip, count, &filler, NULL);
}
GLvoid *copy_gl_pointer_bytecolor(vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
	GLubyte filler = 255;
    return copy_gl_array_convert((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_UNSIGNED_BYTE, width, skip, count, &filler, NULL);
}

GLvoid *copy_gl_pointer_raw(vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
    return copy_gl_array((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_FLOAT, width, skip, count, NULL);
}

GLvoid *copy_gl_pointer_tex(vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
    //float filler = 1.0f;
    return copy_gl_array_texcoord((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         /*GL_FLOAT,*/ width, skip, count, /*&filler,*/ NULL);
}

void copy_gl_pointer_noalloc(void* dest, vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
    float filler = 0.0f;
    copy_gl_array_convert((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_FLOAT, width, skip, count, &filler, dest);
}
void copy_gl_pointer_color_noalloc(void* dest, vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
	float filler = 1.0f;
    copy_gl_array_convert((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_FLOAT, width, skip, count, &filler, dest);
}
void copy_gl_pointer_bytecolor_noalloc(void* dest, vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
	GLubyte filler = 255;
    copy_gl_array_convert((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_UNSIGNED_BYTE, width, skip, count, &filler, dest);
}
void copy_gl_pointer_raw_noalloc(void* dest, vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
    copy_gl_array((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         GL_FLOAT, width, skip, count, dest);
}
void copy_gl_pointer_tex_noalloc(void* dest, vertexattrib_t *ptr, GLsizei width, GLsizei skip, GLsizei count) {
    //float filler = 1.0f;
    copy_gl_array_texcoord((const GLvoid*)((uintptr_t)ptr->pointer+(uintptr_t)ptr->real_pointer), ptr->type, ptr->size, ptr->stride,
                         /*GL_FLOAT,*/ width, skip, count, /*&filler,*/ dest);
}

GLfloat *gl_pointer_index(vertexattrib_t *p, GLint index) {
    static GLfloat buf[4];
    GLsizei size = gl_sizeof(p->type);
    GLsizei stride = p->stride ? p->stride : (size * p->size);
    uintptr_t ptr = (uintptr_t)p->pointer+(uintptr_t)p->real_pointer + (stride * index);

    GL_TYPE_SWITCH(src, ptr, p->type,
        for (int i = 0; i < p->size; i++) {
            buf[i] = src[i];
        }
        // zero anything not set by the pointer
        for (int i = p->size; i < 4; i++) {
            buf[i] = 0;
        },
        default:
            printf("LIBGL: unknown pointer type: 0x%x\n", p->type);
    )
    return buf;
}


GLfloat *copy_eval_double1(GLenum target, GLint ustride, GLint uorder,
    const GLdouble *src) {
 
    GLsizei width = get_map_width(target);
    GLfloat* out;

    if(!src || !width)
        return NULL;

    out = malloc(uorder*width*sizeof(GLfloat));

    GLfloat *p = out;

    for (int i=0; i<uorder; i++, src+=ustride)
      for (int k=0; k<width; k++)
        *p++ = src[k];
    
    return out;
}

GLfloat *copy_eval_float1(GLenum target, GLint ustride, GLint uorder,
    const GLfloat *src) {
 
    GLsizei width = get_map_width(target);
    GLfloat* out;

    if(!src || !width)
        return NULL;

    out = malloc(uorder*width*sizeof(GLfloat));

    GLfloat *p = out;

    for (int i=0; i<uorder; i++, src+=ustride)
      for (int k=0; k<width; k++)
        *p++ = src[k];
    
    return out;
}

GLfloat *copy_eval_double2(GLenum target, GLint ustride, GLint uorder,
                          GLint vstride, GLint vorder,
                          const GLdouble *src) {
    /* Additional memory is allocated to be used by the horner and
    * de Casteljau evaluation schemes.*/
    GLsizei width = get_map_width(target);
    GLfloat* out;

    if(!src || !width)
        return NULL;

    int dsize = (uorder == 2 && vorder == 2)? 0 : uorder*vorder;
    int hsize = (uorder > vorder ? uorder : vorder)*width;
 
    if(hsize>dsize)
      out = malloc((uorder*vorder*width+hsize)*sizeof(GLfloat));
    else
      out = malloc((uorder*vorder*width+dsize)*sizeof(GLfloat));
 
    int uinc = ustride - vorder*vstride;
    GLfloat* p = out;
 
    for (int i=0; i<uorder; i++, src += uinc)
      for (int j=0; j<vorder; j++, src += vstride)
        for (int k=0; k<width; k++)
          *p++ = src[k];
 
    return out;
}
GLfloat *copy_eval_float2(GLenum target, GLint ustride, GLint uorder,
    GLint vstride, GLint vorder,
    const GLfloat *src) {
    /* Additional memory is allocated to be used by the horner and
    * de Casteljau evaluation schemes.*/
    GLsizei width = get_map_width(target);
    GLfloat* out;

    if(!src || !width)
        return NULL;
    
    int dsize = (uorder == 2 && vorder == 2)? 0 : uorder*vorder;
    int hsize = (uorder > vorder ? uorder : vorder)*width;

    if(hsize>dsize)
        out = malloc((uorder*vorder*width+hsize)*sizeof(GLfloat));
    else
        out = malloc((uorder*vorder*width+dsize)*sizeof(GLfloat));

    int uinc = ustride - vorder*vstride;
    GLfloat* p = out;

    for (int i=0; i<uorder; i++, src += uinc)
      for (int j=0; j<vorder; j++, src += vstride)
        for (int k=0; k<width; k++)
          *p++ = src[k];

    return out;
}

void getminmax_indices_us(const GLushort *indices, GLsizei *max, GLsizei *min, GLsizei count) {
    if (!count) return;
    *max = indices[0];
    *min = indices[0];
    for (int i = 1; i < count; i++) {
        GLsizei n = indices[i];
        if( n < *min) *min = n;
        if (n > *max) *max = n;
    }
}
void normalize_indices_us(GLushort *indices, GLsizei *max, GLsizei *min, GLsizei count) {
    getminmax_indices_us(indices, max, min, count);
    for (int i = 0; i < count; i++) {
        indices[i] -= *min;
    }
}

void getminmax_indices_ui(const GLuint *indices, GLsizei *max, GLsizei *min, GLsizei count) {
    if (!count) return;
    *max = indices[0];
    *min = indices[0];
    for (int i = 1; i < count; i++) {
        GLsizei n = indices[i];
        if( n < *min) *min = n;
        if (n > *max) *max = n;
    }
}
void normalize_indices_ui(GLuint *indices, GLsizei *max, GLsizei *min, GLsizei count) {
    getminmax_indices_ui(indices, max, min, count);
    for (int i = 0; i < count; i++) {
        indices[i] -= *min;
    }
}

void *copy_gl_array_bgra(void* dest, const void *ptr, GLint stride, GLsizei width, GLsizei skip, GLsizei count) {
	// this one only convert from BGRA (unsigned byte) to RGBA FLOAT
    GLubyte* src = (GLubyte*)ptr;

    if (!src || !valid_copy_range(skip, count))
        return NULL;
						  
    if (! stride)
        stride = 4;

    if(!dest)
        dest = malloc(4*sizeof(GLfloat)*(count-skip));
    GLfloat* dst = dest;
    src += skip*(stride);

    static const float d = 1.0f/255.0f;
    for (int i=skip; i<count; i++) {
        #if defined(__ARM_NEON__) && !defined(__APPLE__)
        int lsrc = *(int*)src;
        lsrc = (lsrc&0xff00ff00) | ((lsrc&0x00ff0000)>>16) | ((lsrc&0x000000ff)<<16);
        asm volatile (
        "vmov           s12, %1              \n\t"   // because you cannot vmovl.u8 d6, s11
        "vmovl.u8       q3, d6               \n\t"   // Expand to 16-bit (so unsetuped s13 is expanded in d7)
        "vmovl.u16      q3, d6               \n\t"   // Expand to 32-bit, ignoring expanded d7
        "vcvt.f32.u32   q3, q3               \n\t"   // Convert to float
        "vmul.f32       q3, q3, %y2          \n\t"   // Normalize
        "vst1.f32       {q3}, [%0]!          \n\t"   // Store, next
        :"+r"(dst) :"r"(lsrc), "w"(d)
        : "q3", "memory"
        );
        #else
        const GLubyte b = src[0], g = src[1], r = src[2], a = src[3];
        *dst++ = r*d;
        *dst++ = g*d;
        *dst++ = b*d;
        *dst++ = a*d;
        #endif
        src+=stride;
    }
    return dest;
}

GLvoid *copy_gl_pointer_color_bgra(const void *ptr, GLint stride, GLsizei width, GLsizei skip, GLsizei count) {
    return copy_gl_array_bgra(NULL, ptr, stride, width, skip, count);
}

void copy_gl_pointer_color_bgra_noalloc(void* dest, const void *ptr, GLint stride, GLsizei width, GLsizei skip, GLsizei count)
{
    copy_gl_array_bgra(dest, ptr, stride, width, skip, count);
}
