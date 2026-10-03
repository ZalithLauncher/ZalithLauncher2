#include "debug.h"

#include <string.h>
#include "wrap/gles.h"
#include "const.h"
#include "loader.h"
#include "logs.h"
#include "gl4es.h"
#include "gles.h"

#define p(a) \
    case a: return #a

const char* PrintEnum(GLenum what) {
    static char fallback[64];
    switch(what)
    {
        // error
        p(GL_INVALID_ENUM);
        p(GL_INVALID_VALUE);
        p(GL_INVALID_OPERATION);
        // target
        p(GL_TEXTURE_1D);
        p(GL_TEXTURE_2D);
        p(GL_TEXTURE_3D);
        p(GL_TEXTURE_RECTANGLE_ARB);
        p(GL_FRAMEBUFFER);
        p(GL_RENDERBUFFER);
        p(GL_PROXY_TEXTURE_1D);
        p(GL_PROXY_TEXTURE_2D);
        p(GL_PROXY_TEXTURE_3D);
        p(GL_READ_FRAMEBUFFER);
        p(GL_DRAW_FRAMEBUFFER);
        p(GL_TEXTURE_CUBE_MAP);
        p(GL_TEXTURE_CUBE_MAP_POSITIVE_X);
        p(GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        p(GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
        p(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
        p(GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        p(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
        // format
        p(GL_COLOR_INDEX);
        p(GL_RED);
        p(GL_R);
        p(GL_R8);
        p(GL_R3_G3_B2);
        p(GL_RGB);
        p(GL_BGR);
        p(GL_RGB8);
        p(GL_RGB5);
        p(GL_RGB16);
        p(GL_RGB16F);
        p(GL_RGB32F);
        p(GL_BGRA);
        p(GL_RGBA);
        p(GL_RGBA4);
        p(GL_RGB5_A1);
        p(GL_RGB10_A2);
        p(GL_RGBA8);
        p(GL_RGBA16);
        p(GL_RGBA16F);
        p(GL_RGBA32F);
        p(GL_COMPRESSED_RGB_S3TC_DXT1_EXT);
        p(GL_COMPRESSED_RGBA_S3TC_DXT1_EXT);
        p(GL_COMPRESSED_RGBA_S3TC_DXT3_EXT);
        p(GL_COMPRESSED_RGBA_S3TC_DXT5_EXT);
        p(GL_LUMINANCE8_ALPHA8);
        p(GL_LUMINANCE_ALPHA);
        p(GL_LUMINANCE4_ALPHA4);
        p(GL_LUMINANCE16_ALPHA16);
        p(GL_LUMINANCE_ALPHA16F);
        p(GL_LUMINANCE_ALPHA32F);
        p(GL_LUMINANCE);
        p(GL_ALPHA);
        p(GL_LUMINANCE8);
        p(GL_LUMINANCE16);
        p(GL_LUMINANCE16F);
        p(GL_LUMINANCE32F);
        p(GL_ALPHA8);
        p(GL_ALPHA16F);
        p(GL_ALPHA32F);
        p(GL_COMPRESSED_ALPHA);
        p(GL_COMPRESSED_LUMINANCE);
        p(GL_COMPRESSED_LUMINANCE_ALPHA);
        p(GL_COMPRESSED_RGB);
        p(GL_COMPRESSED_RGBA);
        p(GL_HALF_FLOAT);
        p(GL_INTENSITY);
        p(GL_INTENSITY8);
        p(GL_INTENSITY16F);
        p(GL_INTENSITY32F);
        p(GL_DEPTH_STENCIL);
        // type
        p(GL_BYTE);
        p(GL_UNSIGNED_BYTE);
        p(GL_UNSIGNED_BYTE_2_3_3_REV);
        p(GL_UNSIGNED_BYTE_3_3_2);
        p(GL_UNSIGNED_INT);
        p(GL_UNSIGNED_SHORT);
        p(GL_UNSIGNED_SHORT_5_5_5_1);
        p(GL_UNSIGNED_SHORT_1_5_5_5_REV);
        p(GL_UNSIGNED_SHORT_4_4_4_4);
        p(GL_UNSIGNED_SHORT_4_4_4_4_REV);
        p(GL_UNSIGNED_SHORT_5_6_5);
        p(GL_UNSIGNED_SHORT_5_6_5_REV);
        p(GL_UNSIGNED_INT_8_8_8_8_REV);
        p(GL_UNSIGNED_INT_8_8_8_8);
        p(GL_FLOAT);
        p(GL_DOUBLE);
        p(GL_UNSIGNED_INT_24_8);
        // texture infos
        p(GL_TEXTURE_WIDTH);
        p(GL_TEXTURE_HEIGHT);
        p(GL_TEXTURE_COMPRESSED);
        p(GL_TEXTURE_BORDER);
        p(GL_TEXTURE_INTERNAL_FORMAT);
        p(GL_MAX_TEXTURE_SIZE);
        p(GL_MAX_TEXTURE_COORDS);
        // texture pack/unpack
        p(GL_UNPACK_ALIGNMENT);
        p(GL_UNPACK_ROW_LENGTH);
        p(GL_UNPACK_SKIP_PIXELS);
        p(GL_UNPACK_SKIP_ROWS);
        p(GL_PACK_ALIGNMENT);
        p(GL_PACK_ROW_LENGTH);
        p(GL_PACK_SKIP_PIXELS);
        p(GL_PACK_SKIP_ROWS);
        // framebuffer
        p(GL_COLOR_ATTACHMENT0);
        p(GL_COLOR_ATTACHMENT1);
        p(GL_COLOR_ATTACHMENT2);
        p(GL_COLOR_ATTACHMENT3);
        p(GL_COLOR_ATTACHMENT4);
        p(GL_DEPTH_ATTACHMENT);
        p(GL_DEPTH_STENCIL_ATTACHMENT);
        p(GL_STENCIL_ATTACHMENT);
        p(GL_DEPTH_COMPONENT);
        p(GL_DEPTH24_STENCIL8);
        p(GL_MAX_DRAW_BUFFERS_ARB);
        p(GL_DRAW_FRAMEBUFFER_BINDING);
        p(GL_READ_FRAMEBUFFER_BINDING);
        // VBO
        p(GL_STATIC_DRAW);
        p(GL_STREAM_DRAW);
        p(GL_READ_WRITE);
        p(GL_ARRAY_BUFFER);
        p(GL_ELEMENT_ARRAY_BUFFER);
        p(GL_PIXEL_PACK_BUFFER);
        p(GL_PIXEL_UNPACK_BUFFER);
        p(GL_WRITE_ONLY);
        // Texture
        p(GL_TEXTURE0);
        p(GL_TEXTURE1);
        p(GL_TEXTURE2);
        p(GL_TEXTURE3);
        p(GL_TEXTURE4);
        p(GL_TEXTURE5);
        p(GL_TEXTURE6);
        p(GL_TEXTURE7);
        p(GL_TEXTURE8);
        p(GL_TEXTURE9);
        p(GL_TEXTURE10);
        p(GL_TEXTURE11);
        p(GL_TEXTURE12);
        p(GL_TEXTURE13);
        p(GL_TEXTURE14);
        p(GL_TEXTURE15);
        p(GL_TEXTURE_WRAP_S);
        p(GL_TEXTURE_WRAP_T);
        p(GL_LINEAR);
        p(GL_NEAREST);
        p(GL_NEAREST_MIPMAP_NEAREST);
        p(GL_NEAREST_MIPMAP_LINEAR);
        p(GL_LINEAR_MIPMAP_NEAREST);
        p(GL_LINEAR_MIPMAP_LINEAR);
        p(GL_TEXTURE_MAX_LEVEL);
        p(GL_TEXTURE_BASE_LEVEL);
        p(GL_TEXTURE_MIN_FILTER);
        p(GL_TEXTURE_MAG_FILTER);
        p(GL_CLAMP_TO_EDGE);
        p(GL_REPEAT);
        p(GL_MIRRORED_REPEAT_OES);
        p(GL_GENERATE_MIPMAP);
        // mode
        p(GL_POINTS);
        p(GL_LINES);
        p(GL_LINE_LOOP);
        p(GL_LINE_STRIP);
        p(GL_TRIANGLES);
        p(GL_TRIANGLE_STRIP);
        p(GL_TRIANGLE_FAN);
        p(GL_QUADS);
        p(GL_QUAD_STRIP);
        p(GL_POLYGON);
        // texgen
        p(GL_S);
        p(GL_T);
        p(GL_Q);
        p(GL_TEXTURE_GEN_MODE);
        p(GL_OBJECT_LINEAR);
        p(GL_EYE_LINEAR);
        p(GL_SPHERE_MAP);
        p(GL_NORMAL_MAP);
        p(GL_REFLECTION_MAP);
        p(GL_TEXTURE_GEN_S);
        p(GL_TEXTURE_GEN_T);
        p(GL_TEXTURE_GEN_R);
        p(GL_TEXTURE_GEN_Q);
        // matrix mode
        p(GL_PROJECTION);
        p(GL_MODELVIEW);
        p(GL_TEXTURE);
        // blend
        p(GL_SRC_ALPHA);
        p(GL_DST_ALPHA);
        p(GL_ONE_MINUS_SRC_ALPHA);
        p(GL_ONE_MINUS_DST_ALPHA);
        // lights
        p(GL_LIGHT0);
        p(GL_LIGHT1);
        p(GL_LIGHT2);
        p(GL_LIGHT3);
        p(GL_LIGHT4);
        p(GL_LIGHT5);
        p(GL_LIGHT6);
        p(GL_LIGHT7);
        p(GL_AMBIENT);
        p(GL_DIFFUSE);
        p(GL_SPECULAR);
        p(GL_POSITION);
        p(GL_SPOT_DIRECTION);
        p(GL_SPOT_EXPONENT);
        p(GL_SPOT_CUTOFF);
        p(GL_CONSTANT_ATTENUATION);
        p(GL_LINEAR_ATTENUATION);
        p(GL_QUADRATIC_ATTENUATION);
        // Misc enabled
        p(GL_LIGHTING);
        p(GL_NORMALIZE);
        p(GL_CULL_FACE);
        p(GL_DEPTH_TEST);
        p(GL_RESCALE_NORMAL);
        p(GL_ALPHA_TEST);
        p(GL_ALPHA_TEST_FUNC);
        p(GL_BLEND);
        p(GL_BLEND_SRC);
        p(GL_BLEND_DST);
        p(GL_LOGIC_OP_MODE);
        p(GL_SCISSOR_TEST);
        p(GL_STENCIL_TEST);
        // uniform type
        p(GL_FLOAT_VEC2);
        p(GL_FLOAT_VEC3);
        p(GL_FLOAT_VEC4);
        p(GL_INT_VEC2);
        p(GL_INT_VEC3);
        p(GL_INT_VEC4);
        p(GL_BOOL);
        p(GL_BOOL_VEC2);
        p(GL_BOOL_VEC3);
        p(GL_BOOL_VEC4);
        p(GL_FLOAT_MAT2);
        p(GL_FLOAT_MAT3);
        p(GL_FLOAT_MAT4);
        p(GL_SAMPLER_2D);
        p(GL_SAMPLER_CUBE);
        // Shaders
        p(GL_FRAGMENT_SHADER);
        p(GL_VERTEX_SHADER);
        p(GL_MAX_VERTEX_ATTRIBS);
        p(GL_MAX_VERTEX_UNIFORM_VECTORS);
        p(GL_MAX_VARYING_VECTORS);
        p(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
        p(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
        p(GL_MAX_TEXTURE_IMAGE_UNITS);
        p(GL_MAX_FRAGMENT_UNIFORM_VECTORS);
        p(GL_SHADER_TYPE);
        p(GL_DELETE_STATUS);
        p(GL_LINK_STATUS);
        p(GL_COMPILE_STATUS);
        p(GL_VALIDATE_STATUS);
        p(GL_ATTACHED_SHADERS);
        p(GL_ACTIVE_UNIFORMS);
        p(GL_ACTIVE_UNIFORM_MAX_LENGTH);
        p(GL_ACTIVE_ATTRIBUTES);
        p(GL_ACTIVE_ATTRIBUTE_MAX_LENGTH);
        p(GL_SHADING_LANGUAGE_VERSION);
        p(GL_CURRENT_PROGRAM);
        p(GL_PROGRAM_BINARY_LENGTH);
        p(GL_NUM_PROGRAM_BINARY_FORMATS);
        p(GL_PROGRAM_BINARY_FORMATS);
        p(GL_INFO_LOG_LENGTH);
        // Client State
        p(GL_VERTEX_ARRAY);
        p(GL_COLOR_ARRAY);
        p(GL_NORMAL_ARRAY);
        p(GL_TEXTURE_COORD_ARRAY);
        p(GL_SECONDARY_COLOR_ARRAY);
        p(GL_FOG_COORD_ARRAY);
        // TexEnv
        p(GL_POINT_SPRITE);
        p(GL_COORD_REPLACE);
        p(GL_TEXTURE_FILTER_CONTROL);
        p(GL_TEXTURE_LOD_BIAS);
        p(GL_TEXTURE_ENV);
        p(GL_TEXTURE_ENV_MODE);
        p(GL_MODULATE);
        p(GL_ADD);
        p(GL_DECAL);
        p(GL_REPLACE);
        p(GL_COMBINE);
        p(GL_COMBINE_RGB);
        p(GL_ADD_SIGNED);
        p(GL_INTERPOLATE);
        p(GL_SUBTRACT);
        p(GL_DOT3_RGB);
        p(GL_DOT3_RGBA);
        p(GL_COMBINE_ALPHA);
        p(GL_SRC0_RGB);
        p(GL_SRC1_RGB);
        p(GL_SRC2_RGB);
        p(GL_CONSTANT);
        p(GL_PRIMARY_COLOR);
        p(GL_PREVIOUS);
        p(GL_SRC0_ALPHA);
        p(GL_SRC1_ALPHA);
        p(GL_SRC2_ALPHA);
        p(GL_OPERAND0_RGB);
        p(GL_OPERAND1_RGB);
        p(GL_OPERAND2_RGB);
        p(GL_SRC_COLOR);
        p(GL_ONE_MINUS_SRC_COLOR);
        p(GL_OPERAND0_ALPHA);
        p(GL_OPERAND1_ALPHA);
        p(GL_OPERAND2_ALPHA);
        p(GL_RGB_SCALE);
        p(GL_ALPHA_SCALE);
        p(GL_TEXTURE_ENV_COLOR);
        // misc
        p(GL_NUM_EXTENSIONS);
        // ARB_program
        p(GL_VERTEX_PROGRAM_ARB);
        p(GL_FRAGMENT_PROGRAM_ARB);
        p(GL_PROGRAM_FORMAT_ASCII_ARB);
        p(GL_PROGRAM_LENGTH_ARB);
        p(GL_PROGRAM_FORMAT_ARB);
        p(GL_PROGRAM_BINDING_ARB);
        p(GL_MAX_PROGRAM_LOCAL_PARAMETERS_ARB);
        p(GL_MAX_PROGRAM_ENV_PARAMETERS_ARB);
        p(GL_MAX_PROGRAM_NATIVE_ATTRIBS_ARB);
        p(GL_MAX_PROGRAM_ATTRIBS_ARB);
        p(GL_MAX_PROGRAM_NATIVE_INSTRUCTIONS_ARB);
        p(GL_MAX_PROGRAM_INSTRUCTIONS_ARB);
        p(GL_MAX_PROGRAM_NATIVE_TEMPORARIES_ARB);
        p(GL_MAX_PROGRAM_TEMPORARIES_ARB);
        p(GL_MAX_PROGRAM_NATIVE_PARAMETERS_ARB);
        p(GL_MAX_PROGRAM_PARAMETERS_ARB);
        p(GL_MAX_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB);
        p(GL_MAX_PROGRAM_ADDRESS_REGISTERS_ARB);
        p(GL_MAX_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB);
        p(GL_MAX_PROGRAM_ALU_INSTRUCTIONS_ARB);
        p(GL_MAX_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB);
        p(GL_MAX_PROGRAM_TEX_INSTRUCTIONS_ARB);
        p(GL_MAX_PROGRAM_TEX_INDIRECTIONS_ARB);
        p(GL_MAX_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB);
        p(GL_PROGRAM_UNDER_NATIVE_LIMITS_ARB);
        p(GL_PROGRAM_NATIVE_INSTRUCTIONS_ARB);
        p(GL_PROGRAM_INSTRUCTIONS_ARB);
        p(GL_PROGRAM_NATIVE_TEMPORARIES_ARB);
        p(GL_PROGRAM_TEMPORARIES_ARB);
        p(GL_PROGRAM_NATIVE_PARAMETERS_ARB);
        p(GL_PROGRAM_PARAMETERS_ARB);
        p(GL_PROGRAM_NATIVE_ATTRIBS_ARB);
        p(GL_PROGRAM_ATTRIBS_ARB);
        p(GL_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB);
        p(GL_PROGRAM_ADDRESS_REGISTERS_ARB);
        p(GL_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB);
        p(GL_PROGRAM_ALU_INSTRUCTIONS_ARB);
        p(GL_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB);
        p(GL_PROGRAM_TEX_INSTRUCTIONS_ARB);
        p(GL_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB);
        p(GL_PROGRAM_TEX_INDIRECTIONS_ARB);
        default:
            sprintf(fallback, "0x%04X", what);
    }
    return fallback;
}

const char* PrintEGLError(int onlyerror) {
#ifdef NOEGL
    return "";
#else
    LOAD_EGL(eglGetError);
    static char fallback[64];
    GLenum what = egl_eglGetError();
    if(onlyerror && what==EGL_SUCCESS)
        return NULL;
    switch(what)
    {
        p(EGL_SUCCESS);
        p(EGL_NOT_INITIALIZED);
        p(EGL_BAD_ACCESS);
        p(EGL_BAD_ALLOC);
        p(EGL_BAD_ATTRIBUTE);
        p(EGL_BAD_CONTEXT);
        p(EGL_BAD_CONFIG);
        p(EGL_BAD_CURRENT_SURFACE);
        p(EGL_BAD_DISPLAY);
        p(EGL_BAD_SURFACE);
        p(EGL_BAD_MATCH);
        p(EGL_BAD_PARAMETER);
        p(EGL_BAD_NATIVE_PIXMAP);
        p(EGL_BAD_NATIVE_WINDOW);
        p(EGL_CONTEXT_LOST);
        default:
            sprintf(fallback, "0x%04X", what);
    }
    return fallback;
#endif
}

void CheckGLError(int fwd) {
    LOAD_GLES(glGetError);
    GLenum err=gles_glGetError();
    if(err!=GL_NO_ERROR) {
        printf("LIBGL: glGetError(): %s\n", PrintEnum(err));
        if(fwd)
            errorShim(err);
    }
}