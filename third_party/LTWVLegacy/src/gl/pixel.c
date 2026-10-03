#include "pixel.h"
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "enum_info.h"
#include "gl4es.h"
#include "glstate.h"
#include "debug.h"

#ifdef __BIG_ENDIAN__
#define GL_INT8_REV     GL_UNSIGNED_INT_8_8_8_8
#define GL_INT8         GL_UNSIGNED_INT_8_8_8_8_REV
#else
#define GL_INT8_REV     GL_UNSIGNED_INT_8_8_8_8_REV
#define GL_INT8         GL_UNSIGNED_INT_8_8_8_8
#endif

typedef union {
    uint16_t bin;
    struct {
        uint16_t sign:1;
        uint16_t exp:5;
        uint16_t mant:10;
    } x;
} halffloat_t;

typedef union {
    float f;
    uint32_t bin;
    struct {
        uint32_t sign:1;
        uint32_t exp:8;
        uint32_t mant:23;
    } x;
} fullfloat_t;

static const colorlayout_t *get_color_map(GLenum format) {
    #define map(fmt, ...)                               \
        case fmt: {                                     \
        static colorlayout_t layout = {fmt, __VA_ARGS__}; \
        return &layout; }
    switch (format) {
        map(GL_RED, 0, -1, -1, -1, 0);
        map(GL_R,   0, -1, -1, -1, 0);
        map(GL_RG,  0,  1, -1, -1, 0);
        map(GL_RGBA,0,  1, 2, 3, 3);
        map(GL_RGB, 0,  1, 2, -1, 2);
        map(GL_BGRA,2,  1, 0, 3, 3);
        map(GL_BGR, 2,  1, 0, -1, 2);
		map(GL_LUMINANCE_ALPHA, 0, 0, 0, 1, 1);
		map(GL_LUMINANCE, 0, 0, 0, -1, 0);
		map(GL_ALPHA,-1, -1, -1, 0, 0);
        map(GL_DEPTH_COMPONENT, 0, -1, -1, -1, 0);
        map(GL_COLOR_INDEX, 0, 1, 2, 3, 3);
        default:
            printf("LIBGL: unknown pixel format %s\n", PrintEnum(format));
            break;
    }
    static colorlayout_t null = {0};
    return &null;
    #undef map
}

static inline float float_h2f(halffloat_t t)
{
    fullfloat_t tmp;
    tmp.x.sign = t.x.sign;  // copy sign
    if(t.x.exp==0 /*&& t.mant==0*/) {
    // 0 and denormal?
        tmp.x.exp=0;
        tmp.x.mant=0;
    } else if (t.x.exp==31) {
    // Inf / NaN
        tmp.x.exp=255;
        tmp.x.mant=(t.x.mant<<13);
    } else {
        tmp.x.mant=(t.x.mant<<13);
        tmp.x.exp = t.x.exp+0x38;
    }

    return tmp.f;
}

static inline halffloat_t float_f2h(float f)
{
    fullfloat_t tmp;
    halffloat_t ret;
    tmp.f = f;
    ret.x.sign = tmp.x.sign;
    if (tmp.x.exp == 0) {
        // O and denormal
        ret.bin = 0;
    } else if (tmp.x.exp==255) {
        // Inf / NaN
        ret.x.exp = 31;
        ret.x.mant = tmp.x.mant>>13;
    } else if(tmp.x.exp>0x71) {
        // flush to 0
        ret.x.exp = 0;
        ret.x.mant = 0;
    } else if(tmp.x.exp<0x8e) {
        // clamp to max
        ret.x.exp = 30;
        ret.x.mant = 1023;
    } else {
        ret.x.exp = tmp.x.exp - 38;
        ret.x.mant = tmp.x.mant>>13;
    }

    return ret;
}

static inline
bool remap_pixel(const GLvoid *src, GLvoid *dst,
                 const colorlayout_t *src_color, GLenum src_type,
                 const colorlayout_t *dst_color, GLenum dst_type) {

    #define type_case(constant, type, ...)        \
        case constant: {                          \
            const type *s = (const type *)src;    \
            type *d = (type *)dst;                \
            type v = *s;                          \
            __VA_ARGS__                           \
            break;                                \
        }

    #define default(arr, amod, vmod, hmod, key, def) \
        (key >= 0)? hmod(arr[amod key] vmod) : def

    #define carefully(arr, amod, hmod, key, value) \
        if (key >= 0) d[amod key] = hmod(value);

    #define read_each(amod, vmod, hmod)                                 \
        pixel.r = default(s, amod, vmod, hmod, src_color->red, 0.0f);      \
        pixel.g = default(s, amod, vmod, hmod, src_color->green, 0.0f);    \
        pixel.b = default(s, amod, vmod, hmod, src_color->blue, 0.0f);     \
        pixel.a = default(s, amod, vmod, hmod, src_color->alpha, 1.0f);

    #define write_each(amod, vmod, hmod)                         \
        carefully(d, amod, hmod, dst_color->red, pixel.r vmod)   \
        carefully(d, amod, hmod, dst_color->green, pixel.g vmod) \
        carefully(d, amod, hmod, dst_color->blue, pixel.b vmod)  \
        carefully(d, amod, hmod, dst_color->alpha, pixel.a vmod)

    // this pixel stores our intermediate color
    // it will be RGBA and normalized to between (0.0 - 1.0f)
    pixel_t pixel;
    int max_a = src_color->maxv;
    if (src_color->green>max_a) max_a=src_color->green;
    if (src_color->blue>max_a) max_a=src_color->blue;
    if (src_color->alpha>max_a) max_a=src_color->alpha;
    switch (src_type) {
        type_case(GL_DOUBLE, GLdouble, read_each(,,))
        type_case(GL_FLOAT, GLfloat, read_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, read_each(,,float_h2f))
        type_case(GL_BYTE, GLbyte, read_each(, / 128.0f,))
        case GL_INT8_REV:
        type_case(GL_UNSIGNED_BYTE, GLubyte, read_each(, / 255.0f,))
        type_case(GL_UNSIGNED_SHORT, GLubyte, read_each(, / 65535.0f,))
        type_case(GL_INT8, GLubyte, read_each(max_a - , / 255.0f,))
        type_case(GL_UNSIGNED_SHORT_5_6_5_REV, GLushort,
            s = (const GLushort[]) {
                ((v      ) & 0x1f)<<1,
                ((v >>  5) & 0x3f),
                ((v >> 11) & 0x1f)<<1,
            };
            read_each(, / 63.0f,);
        )
        type_case(GL_UNSIGNED_SHORT_1_5_5_5_REV, GLushort,
            s = (const GLushort[]) {
                ((v      ) & 0x1f),
                ((v >>  5) & 0x1f),
                ((v >> 10) & 0x1f),
                ((v >> 15) & 0x01)*31,
            };
            read_each(, / 31.0f,);
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4_REV, GLushort,
            s = (const GLushort[]) {
                ((v       ) & 0x0f),
                ((v >>  4 ) & 0x0f),
                ((v >>  8 ) & 0x0f),
                ((v >> 12 ) & 0x0f)
            };
            read_each(, / 15.0f,);
        )
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            s = (const GLushort[]) {
                ((v >> 11) & 0x1f)<<1,
                ((v >>  5) & 0x3f),
                ((v      ) & 0x1f)<<1,
            };
            read_each(, / 63.0f,);
        )
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            s = (const GLushort[]) {
                ((v >> 11) & 0x1f),
                ((v >>  6) & 0x1f),
                ((v >>  1) & 0x1f),
                ((v      ) & 0x01)*31,
            };
            read_each(, / 31.0f,);
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            s = (const GLushort[]) {
                ((v >> 12) & 0x0f),
                ((v >>  8) & 0x0f),
                ((v >>  4) & 0x0f),
                ((v      ) & 0x0f)
            };
            read_each(, / 15.0f,);
        )
        default:
            // TODO: add glSetError?
            printf("LIBGL: Unsupported source data type: %s\n", PrintEnum(src_type));
            return false;
            break;
    }
    max_a = dst_color->maxv;
    if (dst_color->green>max_a) max_a=dst_color->green;
    if (dst_color->blue>max_a) max_a=dst_color->blue;
    if (dst_color->alpha>max_a) max_a=dst_color->alpha;
    if ((dst_color->red==dst_color->green) && (dst_color->red==dst_color->blue)) {
        // special case
        GLfloat aa = (pixel.r + pixel.g + pixel.b)/3.0f;    //*TODO* find a better formula. real luminance is not just the mean value.
        pixel.r = pixel.g = pixel.b = aa;
    }
    switch (dst_type) {
        type_case(GL_FLOAT, GLfloat, write_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, write_each(,,float_f2h))
        type_case(GL_BYTE, GLbyte, write_each(, * 127.0f,))
        type_case(GL_UNSIGNED_BYTE, GLubyte, write_each(, * 255.0,))
        type_case(GL_UNSIGNED_SHORT, GLushort, write_each(, * 65535.0f,))
        type_case(GL_UNSIGNED_INT, GLuint, write_each(, * (float)0xffffffff,))
        type_case(GL_INT8_REV, GLubyte, write_each(, * 255.0,))
        type_case(GL_INT8, GLubyte, write_each(max_a - , * 255.0,))
        // TODO: force 565 to RGB? then we can change [4] -> 3
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            GLfloat color[4];
            color[dst_color->red] = pixel.r;
            color[dst_color->green] = pixel.g;
            color[dst_color->blue] = pixel.b;
            *d = (((GLuint)(color[0] * 31.0f) & 0x1f) << 11) |
                 (((GLuint)(color[1] * 63.0f) & 0x3f) << 5 ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f)      );
        )
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            GLfloat color[4];
            color[dst_color->red] = pixel.r;
            color[dst_color->green] = pixel.g;
            color[dst_color->blue] = pixel.b;
            color[dst_color->alpha] = pixel.a;
            // TODO: can I macro this or something? it follows a pretty strict form.
            *d = (((GLuint)(color[3]        ) & 0x01)      ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f) << 1 ) |
                 (((GLuint)(color[1] * 31.0f) & 0x1f) << 6 ) |
                 (((GLuint)(color[0] * 31.0f) & 0x1f) << 11);
        )
        type_case(GL_UNSIGNED_SHORT_1_5_5_5_REV, GLushort,
            GLfloat color[4];
            color[dst_color->red] = pixel.r;
            color[dst_color->green] = pixel.g;
            color[dst_color->blue] = pixel.b;
            color[dst_color->alpha] = pixel.a;
            *d = (((GLuint)(color[3]        ) & 0x01) << 15) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f) << 10) |
                 (((GLuint)(color[1] * 31.0f) & 0x1f) << 5 ) |
                 (((GLuint)(color[0] * 31.0f) & 0x1f)      );
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            GLfloat color[4];
            color[dst_color->red] = pixel.r;
            color[dst_color->green] = pixel.g;
            color[dst_color->blue] = pixel.b;
            color[dst_color->alpha] = pixel.a;
            *d = (((GLushort)(color[0] * 15.0f) & 0x0f) << 12) |
                 (((GLushort)(color[1] * 15.0f) & 0x0f) << 8 ) |
                 (((GLushort)(color[2] * 15.0f) & 0x0f) << 4 ) |
                 (((GLushort)(color[3] * 15.0f) & 0x0f)      );
        )
        default:
            printf("LIBGL: Unsupported target data type: %s\n", PrintEnum(dst_type));
            return false;
            break;
    }
    return true;

    #undef type_case
    #undef default
    #undef carefully
    #undef read_each
    #undef write_each
}
static inline
bool transform_pixel(const GLvoid *src, GLvoid *dst,
                 const colorlayout_t *src_color, GLenum src_type,
                 const GLfloat *scale, const GLfloat *bias) {

    #define type_case(constant, type, ...)        \
        case constant: {                          \
            const type *s = (const type *)src;    \
            type *d = (type *)dst;                \
            type v = *s;                          \
            __VA_ARGS__                           \
            break;                                \
        }

    #define default(arr, amod, vmod, hmod, key, def) \
        (amod key) >= 0 ? hmod(arr[amod key] vmod) : def

    #define carefully(arr, amod, hmod, key, value) \
        if ((amod key) >= 0) d[amod key] = hmod(value);

    #define read_each(amod, vmod, hmod)                                 \
        pixel.r = default(s, amod, vmod, hmod, src_color->red, 0.0f);      \
        pixel.g = default(s, amod, vmod, hmod, src_color->green, 0.0f);    \
        pixel.b = default(s, amod, vmod, hmod, src_color->blue, 0.0f);     \
        pixel.a = default(s, amod, vmod, hmod, src_color->alpha, 1.0f);

    #define write_each(amod, vmod, hmod)                         \
        carefully(d, amod, hmod, src_color->red, pixel.r vmod)   \
        carefully(d, amod, hmod, src_color->green, pixel.g vmod) \
        carefully(d, amod, hmod, src_color->blue, pixel.b vmod)  \
        carefully(d, amod, hmod, src_color->alpha, pixel.a vmod)

    #define transformf(pix, number)                         \
        pix=pix*scale[number]+bias[number];   \
        if (pix<0.0) pix=0.0;                               \
        if (pix>1.0) pix=1.0;


    // this pixel stores our intermediate color
    // it will be RGBA and normalized to between (0.0 - 1.0f)
    pixel_t pixel;
    int max_a = src_color->maxv;
    if (src_color->green>max_a) max_a=src_color->green;
    if (src_color->blue>max_a) max_a=src_color->blue;
    if (src_color->alpha>max_a) max_a=src_color->alpha;
    switch (src_type) {
        type_case(GL_DOUBLE, GLdouble, read_each(,,))
        type_case(GL_FLOAT, GLfloat, read_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, read_each(,,float_h2f))
        case GL_INT8_REV:
        type_case(GL_UNSIGNED_BYTE, GLubyte, read_each(, / 255.0f,))
        type_case(GL_UNSIGNED_SHORT, GLushort, read_each(, / 65535.0f,))
        type_case(GL_INT8, GLubyte, read_each(max_a - , / 255.0f,))
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            s = (const GLushort[]) {
                ((v >> 11) & 0x1f)<<1,
                ((v >>  5) & 0x3f),
                ((v      ) & 0x1f)<<1,
            };
            read_each(, / 63.0f,);
        )
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            s = (const GLushort[]) {
                ((v >> 11) & 0x1f),
                ((v >>  6) & 0x1f),
                ((v >>  1) & 0x1f),
                ((v      ) & 0x01)*31,
            };
            read_each(, / 31.0f,);
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            s = (const GLushort[]) {
                ((v >> 12) & 0x0f),
                ((v >>  8) & 0x0f),
                ((v >>  4) & 0x0f),
                ((v      ) & 0x0f)
            };
            read_each(, / 15.0f,);
        )
        default:
            // TODO: add glSetError?
            printf("LIBGL: transform_pixel: Unsupported source data type: %s\n", PrintEnum(src_type));
            return false;
            break;
    }
    transformf(pixel.r, 0);
    transformf(pixel.g, 1);
    transformf(pixel.b, 2);
    transformf(pixel.a, 3);

    switch (src_type) {
        type_case(GL_FLOAT, GLfloat, write_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, write_each(,,float_f2h))
        type_case(GL_UNSIGNED_BYTE, GLubyte, write_each(, * 255.0,))
        type_case(GL_UNSIGNED_SHORT, GLushort, write_each(, / 65535.0f,))
        type_case(GL_INT8_REV, GLubyte, write_each(, * 255.0,))
        type_case(GL_INT8, GLubyte, write_each(max_a - , * 255.0,))
        // TODO: force 565 to RGB? then we can change [4] -> 3
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            *d = (((GLuint)(color[0] * 31.0f) & 0x1f) << 11) |
                 (((GLuint)(color[1] * 63.0f) & 0x3f) << 5 ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f)      );
        )
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            color[src_color->alpha] = pixel.a;
            // TODO: can I macro this or something? it follows a pretty strict form.
            *d = (((GLuint)(color[3]        ) & 0x01)      ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f) << 1 ) |
                 (((GLuint)(color[1] * 31.0f) & 0x1f) << 6 ) |
                 (((GLuint)(color[0] * 31.0f) & 0x1f) << 11);
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            color[src_color->alpha] = pixel.a;
            *d = (((GLushort)(color[0] * 15.0f) & 0x0f) << 12) |
                 (((GLushort)(color[1] * 15.0f) & 0x0f) << 8 ) |
                 (((GLushort)(color[2] * 15.0f) & 0x0f) << 4 ) |
                 (((GLushort)(color[3] * 15.0f) & 0x0f)      );
        )
        default:
            printf("LIBGL: Unsupported target data type: %s\n", PrintEnum(src_type));
            return false;
            break;
    }
    return true;

    #undef transformf
    #undef type_case
    #undef default
    #undef carefully
    #undef read_each
    #undef write_each
}

static inline
bool half_pixel(const GLvoid *src0, const GLvoid *src1,
                 const GLvoid *src2, const GLvoid *src3,
                 GLvoid *dst,
                 const colorlayout_t *src_color, GLenum src_type) {

    #define type_case(constant, type, ...)        \
        case constant: {                          \
            const type *s[4];                     \
            s[0] = (const type *)src0;            \
            s[1] = (const type *)src1;            \
            s[2] = (const type *)src2;            \
            s[3] = (const type *)src3;            \
            type *d = (type *)dst;                \
            type v[4];                            \
            v[0] = *s[0];                         \
            v[1] = *s[1];                         \
            v[2] = *s[2];                         \
            v[3] = *s[3];                         \
            __VA_ARGS__                           \
            break;                                \
        }

    #define default(arr, amod, vmod, hmod, key, def) \
        (amod key) >= 0 ? hmod(arr[amod key] vmod) : def

    #define carefully(arr, amod, hmod, key, value) \
        if ((amod key) >= 0) d[amod key] = hmod(value);

    #define read_i_each(amod, vmod, hmod, i)                                   \
        pix[i].r = default(s[i], amod, vmod, hmod, src_color->red, 0.0f);      \
        pix[i].g = default(s[i], amod, vmod, hmod, src_color->green, 0.0f);    \
        pix[i].b = default(s[i], amod, vmod, hmod, src_color->blue, 0.0f);     \
        pix[i].a = default(s[i], amod, vmod, hmod, src_color->alpha, 1.0f);

    #define read_each(amod, vmod, hmod)   \
        read_i_each(amod, vmod, hmod, 0);   \
        read_i_each(amod, vmod, hmod, 1);   \
        read_i_each(amod, vmod, hmod, 2);   \
        read_i_each(amod, vmod, hmod, 3);

    #define write_each(amod, vmod, hmod)                         \
        carefully(d, amod, hmod, src_color->red, pixel.r vmod)   \
        carefully(d, amod, hmod, src_color->green, pixel.g vmod) \
        carefully(d, amod, hmod, src_color->blue, pixel.b vmod)  \
        carefully(d, amod, hmod, src_color->alpha, pixel.a vmod)

    // this pixel stores our intermediate color
    // it will be RGBA and normalized to between (0.0 - 1.0f)
    pixel_t pix[4], pixel;
    int max_a = src_color->maxv;
    if (src_color->green>max_a) max_a=src_color->green;
    if (src_color->blue>max_a) max_a=src_color->blue;
    if (src_color->alpha>max_a) max_a=src_color->alpha;
    switch (src_type) {
        type_case(GL_DOUBLE, GLdouble, read_each(,,))
        type_case(GL_FLOAT, GLfloat, read_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, read_each(,,float_h2f))
        case GL_INT8_REV:
        type_case(GL_UNSIGNED_BYTE, GLubyte, read_each(, / 255.0f,))
        type_case(GL_UNSIGNED_SHORT, GLushort, read_each(, / 65535.0f,))
        type_case(GL_INT8, GLubyte, read_each(max_a - , / 255.0f,))
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            for (int ii=0; ii<4; ii++) {
                s[ii] = (const GLushort[]) {
                    ((v[ii] >> 11) & 0x1f)<<1,
                    ((v[ii] >>  5) & 0x3f),
                    ((v[ii]      ) & 0x1f)<<1,
                };
                read_i_each(, / 63.0f,, ii);
            };
        )
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            for (int ii=0; ii<4; ii++) {
                s[ii] = (const GLushort[]) {
                    ((v[ii] >> 11) & 0x1f),
                    ((v[ii] >>  6) & 0x1f),
                    ((v[ii] >>  1) & 0x1f),
                    ((v[ii]      ) & 0x01)*31,
                };
                read_i_each(, / 31.0f,, ii);
            };
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            for (int ii=0; ii<4; ii++) {
                s[ii] = (const GLushort[]) {
                    ((v[ii] >> 12) & 0x0f),
                    ((v[ii] >>  8) & 0x0f),
                    ((v[ii] >>  4) & 0x0f),
                    ((v[ii]      ) & 0x0f)
                };
                read_i_each(, / 15.0f,, ii);
            };
        )
        default:
            // TODO: add glSetError?
            printf("LIBGL: half_pixel: Unsupported source data type: %s\n", PrintEnum(src_type));
            return false;
            break;
    }
    pixel.r = (pix[0].r + pix[1].r + pix[2].r + pix[3].r) * 0.25f;
    pixel.g = (pix[0].g + pix[1].g + pix[2].g + pix[3].g) * 0.25f;
    pixel.b = (pix[0].b + pix[1].b + pix[2].b + pix[3].b) * 0.25f;
    pixel.a = (pix[0].a + pix[1].a + pix[2].a + pix[3].a) * 0.25f;

    switch (src_type) {
        type_case(GL_FLOAT, GLfloat, write_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, write_each(,,float_f2h))
        type_case(GL_UNSIGNED_BYTE, GLubyte, write_each(, * 255.0,))
        type_case(GL_UNSIGNED_SHORT, GLushort, write_each(, / 65535.0f,))
        type_case(GL_INT8_REV, GLubyte, write_each(, * 255.0,))
        type_case(GL_INT8, GLubyte, write_each(max_a - , * 255.0,))
       // TODO: force 565 to RGB? then we can change [4] -> 3
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            *d = (((GLuint)(color[0] * 31.0f) & 0x1f) << 11) |
                 (((GLuint)(color[1] * 63.0f) & 0x3f) << 5 ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f)      );
        )
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            color[src_color->alpha] = pixel.a;
            // TODO: can I macro this or something? it follows a pretty strict form.
            *d = (((GLuint)(color[3]        ) & 0x01)      ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f) << 1 ) |
                 (((GLuint)(color[1] * 31.0f) & 0x1f) << 6 ) |
                 (((GLuint)(color[0] * 31.0f) & 0x1f) << 11);
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            color[src_color->alpha] = pixel.a;
            *d = (((GLushort)(color[0] * 15.0f) & 0x0f) << 12) |
                 (((GLushort)(color[1] * 15.0f) & 0x0f) << 8 ) |
                 (((GLushort)(color[2] * 15.0f) & 0x0f) << 4 ) |
                 (((GLushort)(color[3] * 15.0f) & 0x0f)      );
        )
        default:
            printf("LIBGL: half_pixel: Unsupported target data type: %s\n", PrintEnum(src_type));
            return false;
            break;
    }
    return true;

    #undef type_case
    #undef default
    #undef carefully
    #undef read_each
    #undef read_each_i
    #undef write_each
}


static inline
bool quarter_pixel(const GLvoid *src[16],
                 GLvoid *dst,
                 const colorlayout_t *src_color, GLenum src_type) {

    #define type_case(constant, type, ...)        \
        case constant: {                          \
            const type *s[16];                    \
            for (int aa=0; aa<16; aa++)           \
                s[aa] = (const type *)src[aa];    \
            type *d = (type *)dst;                \
            type v[16];                           \
            for (int aa=0; aa<16; aa++)           \
                v[aa] = *s[aa];                   \
            __VA_ARGS__                           \
            break;                                \
        }

    #define default(arr, amod, vmod, hmod, key, def) \
        (amod key) >= 0 ? hmod(arr[amod key] vmod) : def

    #define carefully(arr, amod, hmod, key, value) \
        if ((amod key) >= 0) d[amod key] = hmod(value);

    #define read_i_each(amod, vmod, hmod, i)                                   \
        pix[i].r = default(s[i], amod, vmod, hmod, src_color->red, 0.0f);      \
        pix[i].g = default(s[i], amod, vmod, hmod, src_color->green, 0.0f);    \
        pix[i].b = default(s[i], amod, vmod, hmod, src_color->blue, 0.0f);     \
        pix[i].a = default(s[i], amod, vmod, hmod, src_color->alpha, 1.0f);

    #define read_each(amod, vmod, hmod)   \
        read_i_each(amod, vmod, hmod, 0);   \
        read_i_each(amod, vmod, hmod, 1);   \
        read_i_each(amod, vmod, hmod, 2);   \
        read_i_each(amod, vmod, hmod, 3);   \
        read_i_each(amod, vmod, hmod, 4);   \
        read_i_each(amod, vmod, hmod, 5);   \
        read_i_each(amod, vmod, hmod, 6);   \
        read_i_each(amod, vmod, hmod, 7);   \
        read_i_each(amod, vmod, hmod, 8);   \
        read_i_each(amod, vmod, hmod, 9);   \
        read_i_each(amod, vmod, hmod,10);   \
        read_i_each(amod, vmod, hmod,11);   \
        read_i_each(amod, vmod, hmod,12);   \
        read_i_each(amod, vmod, hmod,13);   \
        read_i_each(amod, vmod, hmod,14);   \
        read_i_each(amod, vmod, hmod,15);   \

    #define write_each(amod, vmod, hmod)                         \
        carefully(d, amod, hmod, src_color->red, pixel.r vmod)   \
        carefully(d, amod, hmod, src_color->green, pixel.g vmod) \
        carefully(d, amod, hmod, src_color->blue, pixel.b vmod)  \
        carefully(d, amod, hmod, src_color->alpha, pixel.a vmod)

    // this pixel stores our intermediate color
    // it will be RGBA and normalized to between (0.0 - 1.0f)
    pixel_t pix[16], pixel;
    int max_a = src_color->maxv;
    if (src_color->green>max_a) max_a=src_color->green;
    if (src_color->blue>max_a) max_a=src_color->blue;
    if (src_color->alpha>max_a) max_a=src_color->alpha;
    switch (src_type) {
        type_case(GL_DOUBLE, GLdouble, read_each(,,))
        type_case(GL_FLOAT, GLfloat, read_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, read_each(,,float_h2f))
        case GL_INT8_REV:
        type_case(GL_UNSIGNED_BYTE, GLubyte, read_each(, / 255.0f,))
        type_case(GL_UNSIGNED_SHORT, GLushort, read_each(, / 65535.0f,))
        type_case(GL_INT8, GLubyte, read_each(max_a - , / 255.0f,))
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            for (int ii=0; ii<4; ii++) {
                s[ii] = (GLushort[]) {
                    ((v[ii] & 0xf800) >>11),
                    ((v[ii] & 0x07c0) >> 6),
                    ((v[ii] & 0x003e) >> 1),
                    ((v[ii] & 1)    )*31,
                };
                read_i_each(, / 31.0f,, ii);
            };
        )
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            for (int ii=0; ii<4; ii++) {
                s[ii] = (GLushort[]) {
                    ((v[ii] & 0xF800) >>11)*2,
                    ((v[ii] & 0x07e0) >> 5),
                    ((v[ii] & 0x001f)     )*2,
                };
                read_i_each(, / 63.0f,, ii);
            };
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            for (int ii=0; ii<16; ii++) {
                s[ii] = (GLushort[]) {
                    ((v[ii] & 0xf000) >> 12),
                    ((v[ii] & 0x0f00) >> 8),
                    ((v[ii] & 0x00f0) >> 4),
                    (v[ii] & 0x000f)
                };
                read_i_each(, / 15.0f,, ii);
            };
        )
        default:
            // TODO: add glSetError?
            printf("LIBGL: quarter_pixel: Unsupported source data type: %s\n", PrintEnum(src_type));
            return false;
            break;
    }
    pixel.r = (pix[0].r + pix[1].r + pix[2].r + pix[3].r + pix[4].r + pix[5].r + pix[6].r + pix[7].r + pix[8].r + pix[9].r + pix[10].r + pix[11].r + pix[12].r + pix[13].r + pix[14].r + pix[15].r) * 0.0625f;
    pixel.g = (pix[0].g + pix[1].g + pix[2].g + pix[3].g + pix[4].g + pix[5].g + pix[6].g + pix[7].g + pix[8].g + pix[9].g + pix[10].g + pix[11].g + pix[12].g + pix[13].g + pix[14].g + pix[15].g) * 0.0625f;
    pixel.b = (pix[0].b + pix[1].b + pix[2].b + pix[3].b + pix[4].b + pix[5].b + pix[6].b + pix[7].b + pix[8].b + pix[9].b + pix[10].b + pix[11].b + pix[12].b + pix[13].b + pix[14].b + pix[15].b) * 0.0625f;
    pixel.a = (pix[0].a + pix[1].a + pix[2].a + pix[3].a + pix[4].a + pix[5].a + pix[6].a + pix[7].a + pix[8].a + pix[9].a + pix[10].a + pix[11].a + pix[12].a + pix[13].a + pix[14].a + pix[15].a) * 0.0625f;

    switch (src_type) {
        type_case(GL_FLOAT, GLfloat, write_each(,,))
        type_case(GL_HALF_FLOAT_OES, halffloat_t, write_each(,,float_f2h))
        type_case(GL_UNSIGNED_BYTE, GLubyte, write_each(, * 255.0,))
        type_case(GL_UNSIGNED_SHORT, GLushort, write_each(, / 65535.0f,))
        type_case(GL_INT8_REV, GLubyte, write_each(, * 255.0,))
        type_case(GL_INT8, GLubyte, write_each(max_a - , * 255.0,))
       // TODO: force 565 to RGB? then we can change [4] -> 3
        type_case(GL_UNSIGNED_SHORT_5_6_5, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            *d = (((GLuint)(color[0] * 31.0f) & 0x1f) << 11) |
                 (((GLuint)(color[1] * 63.0f) & 0x3f) << 5 ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f)      );
        )
        type_case(GL_UNSIGNED_SHORT_5_5_5_1, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            color[src_color->alpha] = pixel.a;
            // TODO: can I macro this or something? it follows a pretty strict form.
            *d = (((GLuint)(color[3]        ) & 0x01)      ) |
                 (((GLuint)(color[2] * 31.0f) & 0x1f) << 1 ) |
                 (((GLuint)(color[1] * 31.0f) & 0x1f) << 6 ) |
                 (((GLuint)(color[0] * 31.0f) & 0x1f) << 11);
        )
        type_case(GL_UNSIGNED_SHORT_4_4_4_4, GLushort,
            GLfloat color[4];
            color[src_color->red] = pixel.r;
            color[src_color->green] = pixel.g;
            color[src_color->blue] = pixel.b;
            color[src_color->alpha] = pixel.a;
            *d = (((GLushort)(color[0] * 15.0f) & 0x0f) << 12) |
                 (((GLushort)(color[1] * 15.0f) & 0x0f) << 8 ) |
                 (((GLushort)(color[2] * 15.0f) & 0x0f) << 4 ) |
                 (((GLushort)(color[3] * 15.0f) & 0x0f)      );
        )
        default:
            printf("LIBGL: quarter_pixel Unsupported target data type: %s\n", PrintEnum(src_type));
            return false;
            break;
    }
    return true;

    #undef type_case
    #undef default
    #undef carefully
    #undef read_each
    #undef read_each_i
    #undef write_each
}

bool pixel_convert(const GLvoid *src, GLvoid **dst,
                   GLuint width, GLuint height,
                   GLenum src_format, GLenum src_type,
                   GLenum dst_format, GLenum dst_type, GLuint stride, GLuint align) {
    const colorlayout_t *src_color, *dst_color;
    GLuint pixels = width * height;
    if(src_type==GL_INT8_REV) src_type=GL_UNSIGNED_BYTE;
    if(dst_type==GL_INT8_REV) dst_type=GL_UNSIGNED_BYTE;
    GLuint dst_size = height * widthalign(width * pixel_sizeof(dst_format, dst_type), align);
    GLuint dst_width2 = widthalign((stride?stride:width) * pixel_sizeof(dst_format, dst_type), align);
    GLuint dst_width = dst_width2 - (width * pixel_sizeof(dst_format, dst_type));
    GLuint src_width = widthalign(width * pixel_sizeof(src_format, src_type), align);
    GLuint src_widthadj = src_width -(width * pixel_sizeof(src_format, src_type));

    //printf("pixel conversion: %ix%i - %s, %s (%d) ==> %s, %s (%d), transform=%i, align=%d, src_width=%d(%d), dst_width=%d(%d)\n", width, height, PrintEnum(src_format), PrintEnum(src_type),pixel_sizeof(src_format, src_type), PrintEnum(dst_format), PrintEnum(dst_type), pixel_sizeof(dst_format, dst_type), raster_need_transform(), align, src_width, src_widthadj, dst_width2, dst_width);
    if(src_type==GL_HALF_FLOAT) src_type=GL_HALF_FLOAT_OES;
    if(dst_type==GL_HALF_FLOAT) dst_type=GL_HALF_FLOAT_OES;

    if ((src_type == dst_type) && (dst_format == src_format)) {
        if (*dst == src)
            return true;
        if (!dst_size || !pixel_sizeof(src_format, src_type)) {
            LOGE("pixel conversion, unknown format size, anticipated abort\n");
            return false;
        }
        if (*dst == NULL)        // alloc dst only if dst==NULL
            *dst = malloc(dst_size);
        if (stride)	// for in-place conversion
			for (int yy=0; yy<height; yy++)
				memcpy((char*)(*dst)+yy*dst_width2, (char*)src+yy*src_width, src_width);
        else
			memcpy(*dst, src, dst_size);
        return true;
    }
    src_color = get_color_map(src_format);
    dst_color = get_color_map(dst_format);
    if (!dst_size || !pixel_sizeof(src_format, src_type)
        || !src_color->type || !dst_color->type) {
        LOGE("pixel conversion, anticipated abort\n");
        return false;
    }
    GLsizei src_stride = pixel_sizeof(src_format, src_type);
    GLsizei dst_stride = pixel_sizeof(dst_format, dst_type);
    if (*dst == src || *dst == NULL)
        *dst = malloc(dst_size);
    uintptr_t src_pos = widthalign((uintptr_t)src, align);
    uintptr_t dst_pos = widthalign((uintptr_t)*dst, align);
    // fast optimized loop for common conversion cases first...
    // TODO: Rewrite that with some Macro, it's obviously doable to simplify the reading (and writing) of all this
    // simple BGRA <-> RGBA / UNSIGNED_BYTE 
    if ((((src_format == GL_BGRA) && (dst_format == GL_RGBA)) || ((src_format == GL_RGBA) && (dst_format == GL_BGRA))) 
        && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        GLuint tmp;
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				tmp = *(const GLuint*)src_pos;
                #ifdef __BIG_ENDIAN__
                *(GLuint*)dst_pos = (tmp&0x00ff00ff) | ((tmp&0x0000ff00)<<16) | ((tmp&0xff000000)>>16);
                #else
				*(GLuint*)dst_pos = (tmp&0xff00ff00) | ((tmp&0x00ff0000)>>16) | ((tmp&0x000000ff)<<16);
                #endif
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGBA or BGRA with GL_INT_8_8_8_8 <-> GL_INT_8_8_8_8_REV
    if((src_format==dst_format) && (src_format==GL_RGBA || src_format==GL_BGRA) && ((src_type==GL_INT8 && dst_type==GL_INT8_REV) || (src_type==GL_INT8_REV && dst_type==GL_INT8))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				((char*)dst_pos)[0] = ((char*)src_pos)[3];
				((char*)dst_pos)[1] = ((char*)src_pos)[2];
				((char*)dst_pos)[2] = ((char*)src_pos)[1];
                ((char*)dst_pos)[3] = ((char*)src_pos)[0];
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    #ifdef __BIG_ENDIAN__
    // RGBA or BGRA with GL_UNSIGNED_INT_8_8_8_8_REV <-> GL_UNSIGNED_BYTE
    if((src_format==dst_format) && (src_format==GL_RGBA || src_format==GL_BGRA) && ((src_type==GL_UNSIGNED_INT_8_8_8_8_REV && dst_type==GL_UNSIGNED_BYTE) || (src_type==GL_UNSIGNED_BYTE && dst_type==GL_UNSIGNED_INT_8_8_8_8_REV))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				((char*)dst_pos)[0] = ((char*)src_pos)[3];
				((char*)dst_pos)[1] = ((char*)src_pos)[2];
				((char*)dst_pos)[2] = ((char*)src_pos)[1];
                ((char*)dst_pos)[3] = ((char*)src_pos)[0];
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    #endif
    // BGRA1555 -> RGBA5551
    if ((src_format == GL_BGRA) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_SHORT_5_5_5_1) && (src_type == GL_UNSIGNED_SHORT_1_5_5_5_REV)) {
      GLushort tmp;
      for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
          // invert 1555/BGRA to 5551/RGBA (0x1f / 0x3e0 / 7c00)
          tmp=*(GLushort*)src_pos;
          *(GLushort*)dst_pos = ((tmp&0x8000)>>15) | ((tmp&0x7fff)<<1);
          src_pos += src_stride;
          dst_pos += dst_stride;
        }
        dst_pos += dst_width;
        src_pos += src_widthadj;
      }
      return true;
    }
    // L -> RGBA
    if ((src_format == GL_LUMINANCE) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				//tmp = *(const GLuint*)src_pos;
                unsigned char* byte_dst = (unsigned char*)dst_pos;
                #ifdef __BIG_ENDIAN__
                byte_dst[1] = byte_dst[2] = byte_dst[3] = *(GLubyte*)src_pos;
                byte_dst[0] = 255;
                #else
                byte_dst[0] = byte_dst[1] = byte_dst[2] = *(GLubyte*)src_pos;
                byte_dst[3] = 255;
                #endif
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // L -> RGB
    if ((src_format == GL_LUMINANCE) && (dst_format == GL_RGB) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				//tmp = *(const GLuint*)src_pos;
                unsigned char* byte_dst = (unsigned char*)dst_pos;
                byte_dst[0] = byte_dst[1] = byte_dst[2] = *(GLubyte*)src_pos;
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGBA -> LA
    if ((src_format == GL_RGBA) && (dst_format == GL_LUMINANCE_ALPHA) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				//tmp = *(const GLuint*)src_pos;
                unsigned char* byte_src = (unsigned char*)src_pos;
                #ifdef __BIG_ENDIAN__
                *(GLushort*)dst_pos = ((((int)byte_src[3])*77 + ((int)byte_src[2])*151 + ((int)byte_src[1])*28)&0xff00)>>8 | (byte_src[0]<<8);
                #else
                *(GLushort*)dst_pos = ((((int)byte_src[0])*77 + ((int)byte_src[1])*151 + ((int)byte_src[2])*28)&0xff00)>>8 | (byte_src[3]<<8);
                #endif
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGRA -> LA
    if ((src_format == GL_BGRA) && (dst_format == GL_LUMINANCE_ALPHA) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				//tmp = *(const GLuint*)src_pos;
                unsigned char* byte_src = (unsigned char*)src_pos;
                #ifdef __BIG_ENDIAN__
                *(GLushort*)dst_pos = ((((int)byte_src[1])*77 + ((int)byte_src[2])*151 + ((int)byte_src[3])*28)&0xff00)>>8 | (byte_src[0]<<8);
                #else
                *(GLushort*)dst_pos = ((((int)byte_src[2])*77 + ((int)byte_src[1])*151 + ((int)byte_src[0])*28)&0xff00)>>8 | (byte_src[3]<<8);
                #endif
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGB(A) -> L
    if (((src_format == GL_RGBA)||(src_format == GL_RGB)) && (dst_format == GL_LUMINANCE) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				//tmp = *(const GLuint*)src_pos;
                unsigned char* byte_src = (unsigned char*)src_pos;
                #ifdef __BIG_ENDIAN__
                *(unsigned char*)dst_pos = (((int)byte_src[3])*77 + ((int)byte_src[2])*151 + ((int)byte_src[1])*28)>>8;
                #else
                *(unsigned char*)dst_pos = (((int)byte_src[0])*77 + ((int)byte_src[1])*151 + ((int)byte_src[2])*28)>>8;
                #endif
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGR(A) -> L
    if (((src_format == GL_BGRA)||(src_format == GL_BGR)) && (dst_format == GL_LUMINANCE) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				//tmp = *(const GLuint*)src_pos;
                unsigned char* byte_src = (unsigned char*)src_pos;
                #ifdef __BIG_ENDIAN__
                *(unsigned char*)dst_pos = (((int)byte_src[1])*77 + ((int)byte_src[2])*151 + ((int)byte_src[3])*28)>>8;
                #else
                *(unsigned char*)dst_pos = (((int)byte_src[2])*77 + ((int)byte_src[1])*151 + ((int)byte_src[0])*28)>>8;
                #endif
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGR(A) -> RGB
    if (((src_format == GL_BGR)||(src_format == GL_BGRA)) && (dst_format == GL_RGB) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				((char*)dst_pos)[0] = ((char*)src_pos)[2];
				((char*)dst_pos)[1] = ((char*)src_pos)[1];
				((char*)dst_pos)[2] = ((char*)src_pos)[0];
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGR -> RGBA
    if (((src_format == GL_BGR)) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				((unsigned char*)dst_pos)[0] = ((unsigned char*)src_pos)[2];
				((unsigned char*)dst_pos)[1] = ((unsigned char*)src_pos)[1];
				((unsigned char*)dst_pos)[2] = ((unsigned char*)src_pos)[0];
                ((unsigned char*)dst_pos)[3] = 255;
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGBA -> RGB
    if ((src_format == GL_RGBA) && (dst_format == GL_RGB) && (dst_type == GL_UNSIGNED_BYTE) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				((char*)dst_pos)[0] = ((char*)src_pos)[0];
				((char*)dst_pos)[1] = ((char*)src_pos)[1];
				((char*)dst_pos)[2] = ((char*)src_pos)[2];
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGB(A) -> RGB565
    if (((src_format == GL_RGB)||(src_format == GL_RGBA)) && (dst_format == GL_RGB) && (dst_type == GL_UNSIGNED_SHORT_5_6_5) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				*(GLushort*)dst_pos = ((GLushort)(((char*)src_pos)[2]&0xf8)>>(3)) | ((GLushort)(((char*)src_pos)[1]&0xfc)<<(5-2)) | ((GLushort)(((char*)src_pos)[0]&0xf8)<<(11-3));
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGR(A) -> RGB565
    if (((src_format == GL_BGR) || (src_format == GL_BGRA)) && (dst_format == GL_RGB) && (dst_type == GL_UNSIGNED_SHORT_5_6_5) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				*(GLushort*)dst_pos = ((GLushort)(((char*)src_pos)[0]&0xf8)>>(3)) | ((GLushort)(((char*)src_pos)[1]&0xfc)<<(5-2)) | ((GLushort)(((char*)src_pos)[2]&0xf8)<<(11-3));
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGBA -> RGBA5551
    if ((src_format == GL_RGBA) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_SHORT_5_5_5_1) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				*(GLushort*)dst_pos = ((GLushort)(((char*)src_pos)[2]&0xf8)>>(3-1)) | ((GLushort)(((char*)src_pos)[1]&0xf8)<<(5-2)) | ((GLushort)(((char*)src_pos)[0]&0xf8)<<(10-2)) | ((GLushort)(((char*)src_pos)[3])?1:0);
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGRA -> RGBA5551
    if ((src_format == GL_BGRA) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_SHORT_5_5_5_1) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				*(GLushort*)dst_pos = ((GLushort)(((char*)src_pos)[0]&0xf8)>>(3-1)) | ((GLushort)(((char*)src_pos)[1]&0xf8)<<(5-2)) | ((GLushort)(((char*)src_pos)[2]&0xf8)<<(10-2)) | ((GLushort)(((char*)src_pos)[3])?1:0);
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGBA -> RGBA4444
    if ((src_format == GL_RGBA) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_SHORT_4_4_4_4) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				*(GLushort*)dst_pos = ((GLushort)(((char*)src_pos)[3]&0xf0))>>(4) | ((GLushort)(((char*)src_pos)[2]&0xf0)) | ((GLushort)(((char*)src_pos)[1]&0xf0))<<(4) | ((GLushort)(((char*)src_pos)[0]&0xf0))<<(8);
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGRA -> RGBA4444
    if ((src_format == GL_BGRA) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_SHORT_4_4_4_4) && ((src_type == GL_UNSIGNED_BYTE))) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				*(GLushort*)dst_pos = ((GLushort)(((char*)src_pos)[3]&0xf0)>>(4)) | ((GLushort)(((char*)src_pos)[0]&0xf0)) | ((GLushort)(((char*)src_pos)[1]&0xf0)<<(4)) | ((GLushort)(((char*)src_pos)[2]&0xf0)<<(8));
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // BGRA4444 -> RGBA 
    if ((src_format == GL_BGRA) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_BYTE) && (src_type == GL_UNSIGNED_SHORT_4_4_4_4_REV)) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
                const GLushort pix = *(GLushort*)src_pos;
                ((char*)dst_pos)[3] = ((pix>>12)&0x0f)<<4;
                ((char*)dst_pos)[2] = ((pix>>8)&0x0f)<<4;
                ((char*)dst_pos)[1] = ((pix>>4)&0x0f)<<4;
                ((char*)dst_pos)[0] = ((pix)&0x0f)<<4;  
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
    // RGBA5551 -> RGBA
    if ((src_format == GL_RGBA) && (dst_format == GL_RGBA) && (dst_type == GL_UNSIGNED_BYTE) && (src_type == GL_UNSIGNED_SHORT_5_5_5_1)) {
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
                const GLushort pix = *(GLushort*)src_pos;
                ((unsigned char*)dst_pos)[0] = ((pix>>11)&0x1f)<<3;
                ((unsigned char*)dst_pos)[1] = ((pix>>6)&0x1f)<<3;
                ((unsigned char*)dst_pos)[2] = ((pix>>1)&0x1f)<<3;
                ((unsigned char*)dst_pos)[3] = ((pix)&0x01)?255:0;  
				src_pos += src_stride;
				dst_pos += dst_stride;
			}
			dst_pos += dst_width;
            src_pos += src_widthadj;
        }
        return true;
    }
	if (! remap_pixel((const GLvoid *)src_pos, (GLvoid *)dst_pos,
					  src_color, src_type, dst_color, dst_type)) {
		// fake convert, to get if it's ok or not
		return false;
	}
    // special case for GL_COLOR_INDEX
    if(src_format==GL_COLOR_INDEX) {
        if(src_type!=GL_UNSIGNED_BYTE)
            return false;   // only unsigned byte for now
        GLubyte tmp[4];
        int idx;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                idx = (((*((GLubyte*)src_pos))<<glstate->raster.index_shift) + glstate->raster.index_offset);
                if (glstate->raster.map_i2i_size-1)
                    idx = glstate->raster.map_i2i[idx&(glstate->raster.map_i2i_size-1)];
                tmp[0] = glstate->raster.map_i2r[idx&(glstate->raster.map_i2r_size-1)];
                tmp[1] = glstate->raster.map_i2g[idx&(glstate->raster.map_i2g_size-1)];
                tmp[2] = glstate->raster.map_i2b[idx&(glstate->raster.map_i2b_size-1)];
                tmp[3] = glstate->raster.map_i2a[idx&(glstate->raster.map_i2a_size-1)];
                remap_pixel((const GLvoid *)tmp, (GLvoid *)dst_pos,
                                src_color, GL_FLOAT, dst_color, dst_type);
                src_pos += src_stride;
                dst_pos += dst_stride;
            }
            dst_pos += dst_width;
            src_pos += src_widthadj;
        }
    } else {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                remap_pixel((const GLvoid *)src_pos, (GLvoid *)dst_pos,
                                src_color, src_type, dst_color, dst_type);
                src_pos += src_stride;
                dst_pos += dst_stride;
            }
            dst_pos += dst_width;
            src_pos += src_widthadj;
        }
    }
	return true;
}

bool pixel_transform(const GLvoid *src, GLvoid **dst,
                   GLuint width, GLuint height,
                   GLenum src_format, GLenum src_type,
                   const GLfloat *scales, const GLfloat *bias)
{
    const colorlayout_t *src_color;
    GLuint pixels = width * height;
    GLuint dst_size = pixels * pixel_sizeof(src_format, src_type);
    src_color = get_color_map(src_format);
    GLsizei src_stride = pixel_sizeof(src_format, src_type);
    if (*dst == src || *dst == NULL)
        *dst = malloc(dst_size);
    uintptr_t src_pos = (uintptr_t)src;
    uintptr_t dst_pos = (uintptr_t)*dst;
	if (! transform_pixel((const GLvoid *)src_pos, (GLvoid *)dst_pos,
					  src_color, src_type, scales, bias)) {
		// fake convert, to get if it's ok or not
		return false;
	}
    for (int aa=0; aa<dst_size; aa++) {
        for (int i = 0; i < pixels; i++) {
            transform_pixel((const GLvoid *)src_pos, (GLvoid *)dst_pos,
                              src_color, src_type, scales, bias);
            src_pos += src_stride;
            dst_pos += src_stride;
        }
        return true;
    }
    return false;
}

bool pixel_scale(const GLvoid *old, GLvoid **new,
                 GLuint width, GLuint height,
                 GLuint new_width, GLuint new_height,
                 GLenum format, GLenum type) {
    GLuint pixel_size;
    GLfloat ratiox, ratioy;
    ratiox = ((float)width)/new_width;
    ratioy = ((float)height)/new_height;
    //printf("scaling %ux%u -> %ux%u (%f/%f)\n", width, height, new_width, new_height, ratiox, ratioy);
    GLvoid *dst;
    uintptr_t src, pos, pixel;

    pixel_size = pixel_sizeof(format, type);
    dst = malloc(pixel_size * new_width * new_height);
    src = (uintptr_t)old;
    pos = (uintptr_t)dst;
    for (int y = 0; y < new_height; y++) {
        int oldy = y*ratioy; if(oldy>=height) oldy=height-1;
        for (int x = 0; x < new_width; x++) {
            int oldx = x*ratiox; if(oldx>=width) oldx=width-1;
            pixel = src + (oldx +
                          oldy * width) * pixel_size;
            memcpy((GLvoid *)pos, (GLvoid *)pixel, pixel_size);
            pos += pixel_size;
        }
    }
    *new = dst;
    return true;
}

bool pixel_halfscale(const GLvoid *old, GLvoid **new,
                 GLuint width, GLuint height,
                 GLenum format, GLenum type) {
    if(!old) {
        *new = NULL;
        return 1;
    }
    GLuint pixel_size, new_width, new_height;
    new_width = width / 2; if(!new_width) ++new_width;
    new_height = height / 2; if(!new_height) ++new_height;
/*    if (new_width*2!=width || new_height*2!=height) {
        printf("LIBGL: halfscaling %ux%u failed\n", width, height);
        return false;
    }*/
    //printf("LIBGL: halfscaling %ux%u -> %ux%u (%s / %s)\n", width, height, new_width, new_height, PrintEnum(format), PrintEnum(type));
    const colorlayout_t *src_color;
    src_color = get_color_map(format);
    GLvoid *dst;
    uintptr_t src, pos, pix0, pix1, pix2, pix3;

    pixel_size = pixel_sizeof(format, type);
    dst = malloc(pixel_size * new_width * new_height);
    src = (uintptr_t)old;
    pos = (uintptr_t)dst;
    const int dx = (width>1)?1:0;
    const int mx = dx + 1;
    const int dy = (height>1)?1:0;
    const int my = dy + 1;
    if(!src_color->type) {
        if(!pixel_size) {
            printf("LIBGL: Cannot halfscale unknown format/type %s/%s\n", PrintEnum(format), PrintEnum(type));
            free(dst);
            return 0;
        }
        for (int y = 0; y < new_height; y++) {
            for (int x = 0; x < new_width; x++) {
                pix0 = src + ((x * mx) +
                            (y * my) * width) * pixel_size;
                // no smart downsize here, the pixel is probably not RGB anyway
                memcpy((void*)pos, (void*)pix0, pixel_size);
                pos += pixel_size;
            }
        }
        *new = dst;
        return 1;
    }
    for (int y = 0; y < new_height; y++) {
        for (int x = 0; x < new_width; x++) {
            pix0 = src + ((x * mx) +
                          (y * my) * width) * pixel_size;
            pix1 = src + ((x * mx + dx) +
                          (y * my) * width) * pixel_size;
            pix2 = src + ((x * mx) +
                          (y * my + dy) * width) * pixel_size;
            pix3 = src + ((x * mx + dx) +
                          (y * my + dy) * width) * pixel_size;
            half_pixel((GLvoid *)pix0, (GLvoid *)pix1, (GLvoid *)pix2, (GLvoid *)pix3, (GLvoid *)pos, src_color, type);
            pos += pixel_size;
        }
    }
    *new = dst;
    return 1;
}

bool pixel_thirdscale(const GLvoid *old, GLvoid **new,
                 GLuint width, GLuint height,
                 GLenum format, GLenum type) {
    GLuint pixel_size, new_width, new_height, dest_size;
    new_width = width / 2; if(!new_width) ++new_width;
    new_height = height / 2; if(!new_height) ++new_height;
    if (new_width*2!=width || new_height*2!=height || format!=GL_RGBA || type!=GL_UNSIGNED_BYTE) {
        //printf("LIBGL: thirdscaling %ux%u failed\n", width, height);
        return false;
    }
//    printf("LIBGL: halfscaling %ux%u -> %ux%u\n", width, height, new_width, new_height);
    const colorlayout_t *src_color;
    src_color = get_color_map(format);
    GLvoid *dst;
    uintptr_t src, pos, pix0, pix1, pix2, pix3;

    pixel_size = pixel_sizeof(format, type);
    dest_size = pixel_sizeof(format, GL_UNSIGNED_SHORT_4_4_4_4);
    dst = malloc(dest_size * new_width * new_height);
    src = (uintptr_t)old;
    pos = (uintptr_t)dst;
    const int dx = (width>1)?1:0;
    const int mx = dx + 1;
    const int dy = (height>1)?1:0;
    const int my = dy + 1;
    GLubyte tmp[4];
    for (int y = 0; y < new_height; y++) {
        for (int x = 0; x < new_width; x++) {
            pix0 = src + ((x * mx) +
                          (y * my) * width) * pixel_size;
            pix1 = src + ((x * mx + dx) +
                          (y * my) * width) * pixel_size;
            pix2 = src + ((x * mx) +
                          (y * my + dy) * width) * pixel_size;
            pix3 = src + ((x * mx + dx) +
                          (y * my + dy) * width) * pixel_size;
            half_pixel((GLvoid *)pix0, (GLvoid *)pix1, (GLvoid *)pix2, (GLvoid *)pix3, (GLvoid *)tmp, src_color, type);
            *((GLushort*)pos) = (((GLushort)tmp[0])&0xf0)<<8 | (((GLushort)tmp[1])&0xf0)<<4 | (((GLushort)tmp[2])&0xf0) | (((GLushort)tmp[3])>>4);
            pos += dest_size;
        }
    }
    *new = dst;
    return true;
}

bool pixel_quarterscale(const GLvoid *old, GLvoid **new,
                 GLuint width, GLuint height,
                 GLenum format, GLenum type) {
    GLuint pixel_size, new_width, new_height;
    new_width = width / 4; if(!new_width) ++new_width;
    new_height = height / 4; if(!new_height) ++new_height;
/*    if (new_width*4!=width || new_height*4!=height) {
        printf("LIBGL: quarterscaling %ux%u failed\n", width, height);
        return false;
    }*/
//    printf("LIBGL: quarterscaling %ux%u -> %ux%u\n", width, height, new_width, new_height);
    const colorlayout_t *src_color;
    src_color = get_color_map(format);
    GLvoid *dst;
    uintptr_t src, pos, pix[16];

    pixel_size = pixel_sizeof(format, type);
    dst = malloc(pixel_size * new_width * new_height);
    src = (uintptr_t)old;
    pos = (uintptr_t)dst;
    const int dxs[4] = {0, width>1?1:0, width>2?2:0, width>3?3:width>1?1:0};
    const int dys[4] = {0, height>1?1:0, height>2?2:0, height>3?3:height>1?1:0};
    if(!src_color->type) {
        if(!pixel_size) {
            printf("LIBGL: Cannot quarterscale unknown format/type %s/%s\n", PrintEnum(format), PrintEnum(type));
            free(dst);
            return 0;
        }
        for (int y = 0; y < new_height; y++) {
            for (int x = 0; x < new_width; x++) {
                pix[0] = src + ((x * 4) +
                            (y * 4) * width) * pixel_size;
                // no smart downsize here, the pixel is probably not RGB anyway
                memcpy((void*)pos, pix, pixel_size);
                pos += pixel_size;
            }
        }
        *new = dst;
        return 1;
    }
    for (int y = 0; y < new_height; y++) {
        for (int x = 0; x < new_width; x++) {
            for (int dx=0; dx<4; dx++) {
                for (int dy=0; dy<4; dy++) {
                    pix[dx+dy*4] = src + ((x * 4 + dxs[dx]) +
                                          (y * 4 + dys[dy]) * width) * pixel_size;
                }
            }
            quarter_pixel((const GLvoid **)pix, (GLvoid *)pos, src_color, type);
            pos += pixel_size;
        }
    }
    *new = dst;
    return true;
}

bool pixel_doublescale(const GLvoid *old, GLvoid **new,
                 GLuint width, GLuint height,
                 GLenum format, GLenum type) {
    if(!old) {
        *new = NULL;
        return true;
    }
    GLuint pixel_size, new_width, new_height;
    new_width = width * 2;
    new_height = height * 2;
    //printf("LIBGL: doublescaling %ux%u -> %ux%u (%s / %s)\n", width, height, new_width, new_height, PrintEnum(format), PrintEnum(type));
    const colorlayout_t *src_color;
    src_color = get_color_map(format);
    GLvoid *dst;
    uintptr_t src, pos, pix0;

    pixel_size = pixel_sizeof(format, type);
    dst = malloc(pixel_size * new_width * new_height);
    src = (uintptr_t)old;
    pos = (uintptr_t)dst;
    const int dx = (width>1)?1:0;
    const int dy = (height>1)?1:0;
    for (int y = 0; y+1 < new_height; y+=2) {
        for (int x = 0; x+1 < new_width; x+=2) {
            pix0 = src + ((x / 2) +
                          (y / 2) * width) * pixel_size;
            memcpy((void*)pos, (void*)pix0, pixel_size);
            memcpy((void*)(pos+new_width*pixel_size), (void*)pix0, pixel_size);
            pos += pixel_size;
            memcpy((void*)pos, (void*)pix0, pixel_size);
            memcpy((void*)(pos+new_width*pixel_size), (void*)pix0, pixel_size);
            pos += pixel_size;
        }
        pos += new_width*pixel_size;
    }
    *new = dst;
    return true;
}

bool pixel_to_ppm(const GLvoid *pixels, GLuint width, GLuint height,
                  GLenum format, GLenum type, GLuint name, GLuint align) {
    // this function should be redone, using write_png from STB for example
    if (! pixels)
        return false;

    GLvoid *src=0;
    char filename[64];
    int size = /*4 * */3 * width * height;
    if (format == GL_RGB && type == GL_UNSIGNED_BYTE) {
        src = (GLvoid*)pixels;
    } else {
        if (! pixel_convert(pixels, (GLvoid **)&src, width, height, format, type, GL_RGB, GL_UNSIGNED_BYTE, 0, align)) {
            return false;
        }
    }

    sprintf(filename, "/tmp/tex.%d.ppm", name);
    FILE *fd = fopen(filename, "w");
    fprintf(fd, "P6 %d %d %d\n", width, height, 255);
    fwrite(src, 1, size, fd);
    fclose(fd);
    return true;
}

static uint8_t srgb_table[256] = {0};
void pixel_srgb_inplace(GLvoid* pixels, GLuint width, GLuint height)
{
    if(!srgb_table[255]) {
        // create table
        for (int i=1; i<256; ++i) {
            srgb_table[i] = floorf(255.f*powf(i/255.f, 1.f/2.2f)+0.5f);
        }
    }
    uint8_t *data = (uint8_t*)pixels;
    int sz = width*height*4;
    for (int i=0; i<sz; ++i)
        data[i] = srgb_table[data[i]];
}
