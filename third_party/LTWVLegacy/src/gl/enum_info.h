#ifndef _GL4ES_ENUM_INFO_H
#define _GL4ES_ENUM_INFO_H

#include <stdbool.h>

#include "const.h"
#include "debug.h"
#include "logs.h"

static const GLsizei gl_sizeof(GLenum type) {
    // types
    switch (type) {
        case GL_DOUBLE:
            return 8;
        case GL_FLOAT:
        case GL_INT:
        case GL_UNSIGNED_INT:
        case GL_UNSIGNED_INT_10_10_10_2:
        case GL_UNSIGNED_INT_2_10_10_10_REV:
        case GL_UNSIGNED_INT_8_8_8_8:
        case GL_UNSIGNED_INT_8_8_8_8_REV:
        case GL_UNSIGNED_INT_24_8:
        case GL_4_BYTES:
            return 4;
        case GL_3_BYTES:
            return 3;
        case GL_LUMINANCE_ALPHA:
        case GL_SHORT:
        case GL_HALF_FLOAT:
        case GL_HALF_FLOAT_OES:
        case GL_UNSIGNED_SHORT:
        case GL_UNSIGNED_SHORT_1_5_5_5_REV:
        case GL_UNSIGNED_SHORT_4_4_4_4:
        case GL_UNSIGNED_SHORT_4_4_4_4_REV:
        case GL_UNSIGNED_SHORT_5_5_5_1:
        case GL_UNSIGNED_SHORT_5_6_5:
        case GL_UNSIGNED_SHORT_5_6_5_REV:
        case GL_2_BYTES:
            return 2;
		case GL_ALPHA:
        case GL_LUMINANCE:
        case GL_BYTE:
        case GL_UNSIGNED_BYTE:
        case GL_UNSIGNED_BYTE_2_3_3_REV:
        case GL_UNSIGNED_BYTE_3_3_2:
        case GL_DEPTH_COMPONENT:
        case GL_COLOR_INDEX:
            return 1;
    }
    // formats
    LOGD("Unsupported pixel data type: %s\n", PrintEnum(type));
    return 0;
}

static const GLuint gl_max_value(GLenum type) {
    switch (type) {
        // float/double only make sense on tex/color
        // be careful about using this
        case GL_DOUBLE:
        case GL_FLOAT:
        case GL_HALF_FLOAT:
        case GL_HALF_FLOAT_OES:
            return 1;
        case GL_BYTE:           return 127;
        case GL_UNSIGNED_BYTE:  return 255;
        case GL_SHORT:          return 32767;
        case GL_UNSIGNED_SHORT: return 65535;
        case GL_INT:            return 2147483647;
        case GL_UNSIGNED_INT:   return 4294967295;
    }
    LOGD("unknown gl max value type: %s\n", PrintEnum(type));
    return 0;
}

static const GLboolean is_type_packed(GLenum type) {
    switch (type) {
        case GL_4_BYTES:
        case GL_3_BYTES:
        case GL_2_BYTES:
        case GL_UNSIGNED_BYTE_2_3_3_REV:
        case GL_UNSIGNED_BYTE_3_3_2:
        case GL_UNSIGNED_INT_10_10_10_2:
        case GL_UNSIGNED_INT_2_10_10_10_REV:
        case GL_UNSIGNED_INT_8_8_8_8:
        case GL_UNSIGNED_INT_8_8_8_8_REV:
        case GL_UNSIGNED_SHORT_1_5_5_5_REV:
        case GL_UNSIGNED_SHORT_4_4_4_4:
        case GL_UNSIGNED_SHORT_4_4_4_4_REV:
        case GL_UNSIGNED_SHORT_5_5_5_1:
        case GL_UNSIGNED_SHORT_5_6_5:
        case GL_UNSIGNED_SHORT_5_6_5_REV:
        case GL_DEPTH_STENCIL:
            return true;
    }
    return false;
}

static const GLsizei pixel_sizeof(GLenum format, GLenum type) {
    GLsizei width = 0;
    switch (format) {
        case GL_R:
        case GL_RED:
		case GL_ALPHA:
		case GL_LUMINANCE:
        case GL_DEPTH_COMPONENT:
        case GL_DEPTH_STENCIL:
        case GL_COLOR_INDEX:
            width = 1;
            break;
        case GL_RG:
		case GL_LUMINANCE_ALPHA:
            width = 2;
            break;
        case GL_RGB:
        case GL_BGR:
        case GL_RGB8:
            width = 3;
            break;
        case GL_RGBA:
        case GL_BGRA:
        case GL_RGBA8:
            width = 4;
            break;
        default:
            LOGD("unsupported pixel format %s\n", PrintEnum(format));
            return 0;
    }

    if (is_type_packed(type))
        width = 1;

    return width * gl_sizeof(type);
}

static const GLboolean pixel_hasalpha(GLenum format) {
    switch (format) {
	case GL_ALPHA:
    case GL_LUMINANCE_ALPHA:
    case GL_RGBA:
    case GL_BGRA:
    case GL_RGBA8:
    case GL_COLOR_INDEX:
	    return true;
	case GL_RED:
	case GL_LUMINANCE:
    case GL_RG:
    case GL_RGB:
    case GL_BGR:
    case GL_RGB8:
    case GL_DEPTH_COMPONENT:
    case GL_DEPTH_STENCIL:
	    return false;
        default:
            return true;
    }
}

static inline const GLboolean valid_vertex_type(GLenum type) {
    switch (type) {
        case GL_UNSIGNED_BYTE:
        case GL_BYTE:
        case GL_FIXED:
        case GL_FLOAT:
        case GL_SHORT:
            return true;
        default:
            return false;
    }
}

#endif // _GL4ES_ENUM_INFO_H