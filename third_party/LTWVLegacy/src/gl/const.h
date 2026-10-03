#ifndef _GL4ES_CONST_H_
#define _GL4ES_CONST_H_

#define GL_QUADS      7
#define GL_QUAD_STRIP 8
#define GL_POLYGON    9

#define GL_MAJOR_VERSION            0x821B
#define GL_MINOR_VERSION            0x821C
#define GL_DOUBLEBUFFER				0x0C32
/* texture mapping */
#define GL_TEXTURE_ENV              0x2300
#define GL_TEXTURE_ENV_MODE         0x2200
#define GL_TEXTURE_1D               0x0DE0
#define GL_TEXTURE_2D               0x0DE1
#define GL_TEXTURE_3D               0x806F
#define GL_TEXTURE_WRAP_S           0x2802
#define GL_TEXTURE_WRAP_T           0x2803
#define GL_TEXTURE_WRAP_R           0x8072
#define GL_TEXTURE_MAG_FILTER       0x2800
#define GL_TEXTURE_MIN_FILTER       0x2801
#define GL_TEXTURE_ENV_COLOR        0x2201
#define GL_TEXTURE_GEN_S            0x0C60
#define GL_TEXTURE_GEN_T            0x0C61
#define GL_TEXTURE_GEN_MODE         0x2500
#define GL_TEXTURE_BORDER_COLOR     0x1004
#define GL_TEXTURE_BORDER           0x1005
#define GL_TEXTURE_WIDTH            0x1000
#define GL_TEXTURE_HEIGHT           0x1001
#define GL_TEXTURE_BORDER           0x1005
#define GL_TEXTURE_COMPONENTS       0x1003
#define GL_TEXTURE_RED_SIZE         0x805C
#define GL_TEXTURE_GREEN_SIZE       0x805D
#define GL_TEXTURE_BLUE_SIZE        0x805E
#define GL_TEXTURE_ALPHA_SIZE       0x805F
#define GL_TEXTURE_LUMINANCE_SIZE   0x8060
#define GL_TEXTURE_INTENSITY_SIZE   0x8061
#define GL_NEAREST_MIPMAP_NEAREST   0x2700
#define GL_NEAREST_MIPMAP_LINEAR    0x2702
#define GL_LINEAR_MIPMAP_NEAREST    0x2701
#define GL_LINEAR_MIPMAP_LINEAR     0x2703
#define GL_OBJECT_LINEAR            0x2401
#define GL_OBJECT_PLANE             0x2501
#define GL_EYE_LINEAR               0x2400
#define GL_EYE_PLANE                0x2502
#define GL_SPHERE_MAP               0x2402
#define GL_NORMAL_MAP               0x8511
#define GL_REFLECTION_MAP			0x8512
#define GL_DECAL                    0x2101
#define GL_MODULATE                 0x2100
#define GL_NEAREST                  0x2600
#define GL_REPEAT                   0x2901
#define GL_CLAMP                    0x2900
#define GL_S                        0x2000
#define GL_T                        0x2001
#define GL_R                        0x2002
#define GL_Q                        0x2003
#define GL_TEXTURE_GEN_R            0x0C62
#define GL_TEXTURE_GEN_Q            0x0C63
#define GL_PROXY_TEXTURE_1D         0x8063
#define GL_PROXY_TEXTURE_2D         0x8064
#define GL_PROXY_TEXTURE_3D         0x8070
#define GL_TEXTURE_MIN_LOD          0x813A
#define GL_TEXTURE_MAX_LOD          0x813B
#define GL_TEXTURE_FILTER_CONTROL   0x8500
#define GL_TEXTURE_LOD_BIAS         0x8501
#define GL_TEXTURE_CUBE_MAP         0x8513
#define GL_TEXTURE_GEN_STR          0x8D60
#define GL_CLAMP_TO_BORDER          0x812D
#define GL_MAX_TEXTURE_COORDS       0x8871

#define GL_TEXTURE_COMPARE_MODE     0x884C

// GL_ARB_point_sprite
#define GL_POINT_SPRITE             0x8861
#define GL_COORD_REPLACE            0x8862

// GL_ARB_texture_rectangle
#define GL_TEXTURE_RECTANGLE_ARB          0x84F5
#define GL_TEXTURE_BINDING_RECTANGLE_ARB  0x84F6
#define GL_PROXY_TEXTURE_RECTANGLE_ARB    0x84F7
#define GL_MAX_RECTANGLE_TEXTURE_SIZE_ARB 0x84F8

#define GL_LINE_SMOOTH            0x0B20
#define GL_LINE_STIPPLE           0x0B24
#define GL_LINE_STIPPLE_PATTERN   0x0B25
#define GL_LINE_STIPPLE_REPEAT    0x0B26
#define GL_LINE_WIDTH             0x0B21
#define GL_LINE_WIDTH_GRANULARITY 0x0B23
#define GL_LINE_WIDTH_RANGE       0x0B22

#define GL_OBJECT_LINEAR    0x2401
#define GL_OBJECT_PLANE     0x2501
#define GL_EYE_LINEAR       0x2400
#define GL_EYE_PLANE        0x2502
#define GL_SPHERE_MAP       0x2402

#define GL_CURRENT_BIT          0x00001
#define GL_POINT_BIT            0x00002
#define GL_LINE_BIT             0x00004
#define GL_POLYGON_BIT          0x00008
#define GL_POLYGON_STIPPLE_BIT  0x00010
#define GL_PIXEL_MODE_BIT       0x00020
#define GL_LIGHTING_BIT         0x00040
#define GL_FOG_BIT              0x00080
// some of these are already defined in GLES
// #define GL_DEPTH_BUFFER_BIT     0x00100
#define GL_ACCUM_BUFFER_BIT     0x00200
// #define GL_STENCIL_BUFFER_BIT   0x00400
#define GL_VIEWPORT_BIT         0x00800
#define GL_TRANSFORM_BIT        0x01000
#define GL_ENABLE_BIT           0x02000
// #define GL_COLOR_BUFFER_BIT     0x04000
#define GL_HINT_BIT             0x08000
#define GL_EVAL_BIT             0x10000
#define GL_LIST_BIT             0x20000
#define GL_TEXTURE_BIT          0x40000
#define GL_SCISSOR_BIT          0x80000
#define GL_ALL_ATTRIB_BITS      0xFFFFF
#define GL_MULTISAMPLE_BIT      0x20000000

#define GL_CLIENT_PIXEL_STORE_BIT  0x00000001
#define GL_CLIENT_VERTEX_ARRAY_BIT 0x00000002
#define GL_ALL_CLIENT_ATTRIB_BITS  0xFFFFFFFF
#define GL_CLIENT_ALL_ATTRIB_BITS  0xFFFFFFFF

// secondary color
#define GL_SECONDARY_COLOR_ARRAY_SIZE     0x845A
#define GL_SECONDARY_COLOR_ARRAY_TYPE     0x845B
#define GL_SECONDARY_COLOR_ARRAY_STRIDE   0x845C
#define GL_SECONDARY_COLOR_ARRAY_POINTER  0x845D
#define GL_SECONDARY_COLOR_ARRAY          0x845E
#define GL_SECONDARY_COLOR_ARRAY_BUFFER_BINDING 0x889C
#define GL_COLOR_SUM                      0x8458
#define GL_CURRENT_SECONDARY_COLOR        0x8459


// pixel formats
#define GL_COLOR_INDEX                 0x1900
#define GL_RED                         0x1903
#define GL_R3_G3_B2                    0x2A10
#define GL_RG                          0x8227
#define GL_BGR                         0x80E0
#define GL_BGRA                        0x80E1
#define GL_UNSIGNED_BYTE_3_3_2         0x8032
#define GL_UNSIGNED_BYTE_2_3_3_REV     0x8362
#define GL_UNSIGNED_SHORT_5_6_5        0x8363
#define GL_UNSIGNED_SHORT_5_6_5_REV    0x8364
#define GL_UNSIGNED_SHORT_4_4_4_4      0x8033
#define GL_UNSIGNED_SHORT_4_4_4_4_REV  0x8365
#define GL_UNSIGNED_SHORT_5_5_5_1      0x8034
#define GL_UNSIGNED_SHORT_1_5_5_5_REV  0x8366
#define GL_UNSIGNED_INT_8_8_8_8        0x8035
#define GL_UNSIGNED_INT_8_8_8_8_REV    0x8367
#define GL_UNSIGNED_INT_10_10_10_2     0x8036
#define GL_UNSIGNED_INT_2_10_10_10_REV 0x8368
#define GL_R8                          0x8229
#define GL_RGB8                        0x8051
#define GL_RGB5                        0x8050
#define GL_RGB4                        0x804F
#define GL_RGBA8                       0x8058
#define GL_RGBA16                      0x805B
#define GL_ALPHA4				       0x803B
#define GL_ALPHA8				       0x803C
#define GL_ALPHA12				       0x803D
#define GL_ALPHA16				       0x803E
#define GL_ALPHA16F                    0x881C
#define GL_ALPHA32F                    0x8816
#define GL_LUMINANCE4				   0x803F
#define GL_LUMINANCE8				   0x8040
#define GL_LUMINANCE12				   0x8041
#define GL_LUMINANCE16				   0x8042
#define GL_LUMINANCE16F                0x881E
#define GL_LUMINANCE32F                0x8818
#define GL_LUMINANCE4_ALPHA4		   0x8043
#define GL_LUMINANCE6_ALPHA2		   0x8044
#define GL_LUMINANCE8_ALPHA8		   0x8045
#define GL_LUMINANCE12_ALPHA4		   0x8046
#define GL_LUMINANCE12_ALPHA12		   0x8047
#define GL_LUMINANCE16_ALPHA16		   0x8048
#define GL_LUMINANCE_ALPHA16F          0x881F
#define GL_LUMINANCE_ALPHA32F          0x8819
#define GL_INTENSITY				   0x8049
#define GL_INTENSITY4				   0x804A
#define GL_INTENSITY8				   0x804B
#define GL_INTENSITY12				   0x804C
#define GL_INTENSITY16				   0x804D
#define GL_INTENSITY16F                0x881D
#define GL_INTENSITY32F                0x8817
#define GL_R16F                        0x822D
#define GL_RGB10_A2                    0x8059
#define GL_RGBA16F                     0x881A
#define GL_RGB16F                      0x881B
#define GL_RGBA32F                     0x8814
#define GL_RGB32F                      0x8815
#define GL_LUMINANCE16_ALPHA16         0x8048
#define GL_RGB16                       0x8054
#define GL_RGBA4                       0x8056
#define GL_RGB5_A1                     0x8057
#define GL_COMPRESSED_ALPHA			   0x84E9
#define GL_COMPRESSED_LUMINANCE		   0x84EA
#define GL_COMPRESSED_LUMINANCE_ALPHA  0x84EB
#define GL_COMPRESSED_INTENSITY		   0x84EC
#define GL_COMPRESSED_RGB			   0x84ED
#define GL_COMPRESSED_RGBA			   0x84EE
#define GL_COMPRESSED_TEXTURE_FORMATS  0x86A3
#define GL_HALF_FLOAT_OES              0x8D61
#define GL_RGB565                      0x8D62

// types
#define GL_BYTE                 0x1400
#define GL_UNSIGNED_BYTE        0x1401
#define GL_SHORT                0x1402
#define GL_UNSIGNED_SHORT       0x1403
#define GL_INT                  0x1404
#define GL_UNSIGNED_INT         0x1405
#define GL_FLOAT                0x1406
#define GL_2_BYTES              0x1407
#define GL_3_BYTES              0x1408
#define GL_4_BYTES              0x1409
#define GL_DOUBLE               0x140A
#define GL_HALF_FLOAT           0x140B
#define GL_BITMAP               0x1A00

#define GL_COMPILE              0x1300
#define GL_COMPILE_AND_EXECUTE  0x1301

// fog
#define GL_FOG                  0x0B60
#define GL_FOG_MODE             0x0B65
#define GL_FOG_DENSITY          0x0B62
#define GL_FOG_COLOR            0x0B66
#define GL_FOG_INDEX            0x0B61
#define GL_FOG_START            0x0B63
#define GL_FOG_END              0x0B64
#define GL_LINEAR               0x2601
#define GL_EXP                  0x0800
#define GL_EXP2                 0x0801
#define GL_FOG_COORDINATE_SOURCE 0x8450
#define GL_FOG_COORD_SRC        GL_FOG_COORDINATE_SOURCE
#define GL_FRAGMENT_DEPTH       0x8452
#define GL_FOG_COORDINATE       0x8451
#define GL_FOG_COORD            GL_FOG_COORDINATE
#define GL_CURRENT_FOG_COORD              0x8453
#define GL_FOG_COORD_ARRAY_TYPE           0x8454
#define GL_FOG_COORD_ARRAY_STRIDE         0x8455
#define GL_FOG_COORD_ARRAY_POINTER        0x8456
#define GL_FOG_COORD_ARRAY                0x8457
#define GL_FOG_COORD_ARRAY_BUFFER_BINDING 0x889D

// lighting
#define GL_LIGHTING             0x0B50
#define GL_LIGHT0               0x4000
#define GL_LIGHT1               0x4001
#define GL_LIGHT2               0x4002
#define GL_LIGHT3               0x4003
#define GL_LIGHT4               0x4004
#define GL_LIGHT5               0x4005
#define GL_LIGHT6               0x4006
#define GL_LIGHT7               0x4007
#define GL_SPOT_EXPONENT        0x1205
#define GL_SPOT_CUTOFF          0x1206
#define GL_CONSTANT_ATTENUATION 0x1207
#define GL_LINEAR_ATTENUATION   0x1208
#define GL_QUADRATIC_ATTENUATION 0x1209
#define GL_AMBIENT              0x1200
#define GL_DIFFUSE              0x1201
#define GL_SPECULAR             0x1202
#define GL_SHININESS            0x1601
#define GL_EMISSION             0x1600
#define GL_POSITION             0x1203
#define GL_SPOT_DIRECTION       0x1204
#define GL_AMBIENT_AND_DIFFUSE  0x1602
#define GL_COLOR_INDEXES        0x1603
#define GL_LIGHT_MODEL_TWO_SIDE 0x0B52
#define GL_LIGHT_MODEL_LOCAL_VIEWER 0x0B51
#define GL_LIGHT_MODEL_AMBIENT  0x0B53
#define GL_FRONT_AND_BACK       0x0408
#define GL_SHADE_MODEL          0x0B54
#define GL_FLAT                 0x1D00
#define GL_SMOOTH               0x1D01
#define GL_COLOR_MATERIAL       0x0B57
#define GL_COLOR_MATERIAL_FACE  0x0B55
#define GL_COLOR_MATERIAL_PARAMETER 0x0B56
#define GL_NORMALIZE            0x0BA1
#define GL_DRAW_BUFFER          0x0C01
#define GL_LIGHT_MODEL_COLOR_CONTROL	0x81F8
#define GL_SINGLE_COLOR		    0x81F9
#define GL_SEPARATE_SPECULAR_COLOR		0x81FA

// stencil
#define GL_STENCIL_BACK_FUNC              0x8800
#define GL_STENCIL_BACK_VALUE_MASK        0x8CA4
#define GL_STENCIL_BACK_REF               0x8CA3
#define GL_STENCIL_BACK_WRITEMASK         0x8CA5
#define GL_STENCIL_BACK_FAIL              0x8801
#define GL_STENCIL_BACK_PASS_DEPTH_FAIL   0x8802
#define GL_STENCIL_BACK_PASS_DEPTH_PASS   0x8803

// pixel transfer
#define GL_MAP_COLOR             0x0D10
#define GL_MAP_STENCIL           0x0D11
#define GL_INDEX_SHIFT           0x0D12
#define GL_INDEX_OFFSET          0x0D13
#define GL_RED_SCALE             0x0D14
#define GL_RED_BIAS              0x0D15
#define GL_GREEN_SCALE           0x0D18
#define GL_GREEN_BIAS            0x0D19
#define GL_BLUE_SCALE            0x0D1A
#define GL_BLUE_BIAS             0x0D1B
#define GL_ALPHA_SCALE           0x0D1C
#define GL_ALPHA_BIAS            0x0D1D
#define GL_DEPTH_SCALE           0x0D1E
#define GL_DEPTH_BIAS            0x0D1F
#define GL_PIXEL_MAP_S_TO_S_SIZE 0x0CB1
#define GL_PIXEL_MAP_I_TO_I_SIZE 0x0CB0
#define GL_PIXEL_MAP_I_TO_R_SIZE 0x0CB2
#define GL_PIXEL_MAP_I_TO_G_SIZE 0x0CB3
#define GL_PIXEL_MAP_I_TO_B_SIZE 0x0CB4
#define GL_PIXEL_MAP_I_TO_A_SIZE 0x0CB5
#define GL_PIXEL_MAP_R_TO_R_SIZE 0x0CB6
#define GL_PIXEL_MAP_G_TO_G_SIZE 0x0CB7
#define GL_PIXEL_MAP_B_TO_B_SIZE 0x0CB8
#define GL_PIXEL_MAP_A_TO_A_SIZE 0x0CB9
#define GL_PIXEL_MAP_S_TO_S      0x0C71
#define GL_PIXEL_MAP_I_TO_I      0x0C70
#define GL_PIXEL_MAP_I_TO_R      0x0C72
#define GL_PIXEL_MAP_I_TO_G      0x0C73
#define GL_PIXEL_MAP_I_TO_B      0x0C74
#define GL_PIXEL_MAP_I_TO_A      0x0C75
#define GL_PIXEL_MAP_R_TO_R      0x0C76
#define GL_PIXEL_MAP_G_TO_G      0x0C77
#define GL_PIXEL_MAP_B_TO_B      0x0C78
#define GL_PIXEL_MAP_A_TO_A      0x0C79
#define GL_PACK_ALIGNMENT        0x0D05
#define GL_PACK_LSB_FIRST        0x0D01
#define GL_PACK_ROW_LENGTH       0x0D02
#define GL_PACK_SKIP_PIXELS      0x0D04
#define GL_PACK_SKIP_ROWS        0x0D03
#define GL_PACK_SWAP_BYTES       0x0D00
#define GL_UNPACK_ALIGNMENT      0x0CF5
#define GL_UNPACK_LSB_FIRST      0x0CF1
#define GL_UNPACK_ROW_LENGTH     0x0CF2
#define GL_UNPACK_SKIP_PIXELS    0x0CF4
#define GL_UNPACK_SKIP_ROWS      0x0CF3
#define GL_UNPACK_SWAP_BYTES     0x0CF0
#define GL_UNPACK_IMAGE_HEIGHT   0x806E
#define GL_PACK_IMAGE_HEIGHT     0x806C
#define GL_ZOOM_X                0x0D16
#define GL_ZOOM_Y                0x0D17
#define GL_MAX_PIXEL_MAP_TABLE   0x0D34
#define GL_TEXTURE_BASE_LEVEL    0x813C

// blending
#define GL_BLEND                 0x0BE2
#define GL_BLEND_SRC             0x0BE1
#define GL_BLEND_DST             0x0BE0
#define GL_SRC_COLOR             0x0300
#define GL_ONE_MINUS_SRC_COLOR   0x0301
#define GL_SRC_ALPHA             0x0302
#define GL_ONE_MINUS_SRC_ALPHA   0x0303
#define GL_DST_ALPHA             0x0304
#define GL_ONE_MINUS_DST_ALPHA   0x0305
#define GL_DST_COLOR             0x0306
#define GL_ONE_MINUS_DST_COLOR   0x0307
#define GL_SRC_ALPHA_SATURATE    0x0308
#define GL_CONSTANT_COLOR        0x8001
#define GL_ONE_MINUS_CONSTANT_COLOR       0x8002
#define GL_CONSTANT_ALPHA        0x8003
#define GL_ONE_MINUS_CONSTANT_ALPHA       0x8004
#define GL_DOT3_RGB_EXT         0x8740
#define GL_DOT3_RGBA_EXT        0x8741
#define GL_BLEND_DST_RGB        0x80C8
#define GL_BLEND_SRC_RGB        0x80C9
#define GL_BLEND_DST_ALPHA      0x80CA
#define GL_BLEND_SRC_ALPHA      0x80CB

// glGet
#define GL_AUX_BUFFERS           0x0C00
#define GL_MAX_ELEMENTS_VERTICES 0x80E8
#define GL_MAX_ELEMENTS_INDICES  0x80E9
#define GL_POINT_SIZE_RANGE		 0x0B12
#define GL_RENDER_MODE           0x0C40
#define GL_NAME_STACK_DEPTH      0x0D70
#define GL_MAX_NAME_STACK_DEPTH  0x0D37
#define GL_MAX_TEXTURE_IMAGE_UNITS 0x8872
#define GL_TRANSPOSE_MODELVIEW_MATRIX           0x84E3
#define GL_TRANSPOSE_PROJECTION_MATRIX          0x84E4
#define GL_TRANSPOSE_TEXTURE_MATRIX             0x84E5
#define GL_TRANSPOSE_COLOR_MATRIX               0x84E6
#define GL_INDEX_ARRAY_POINTER                  0x8091
#define GL_EDGE_FLAG_ARRAY_POINTER              0x8093
#define GL_FEEDBACK_BUFFER_POINTER              0x0DF0
#define GL_SELECTION_BUFFER_POINTER             0x0DF3

// evaluators
#define GL_AUTO_NORMAL           0x0D80
#define GL_MAP1_COLOR_4          0x0D90
#define GL_MAP1_INDEX            0x0D91
#define GL_MAP1_NORMAL           0x0D92
#define GL_MAP1_TEXTURE_COORD_1  0x0D93
#define GL_MAP1_TEXTURE_COORD_2  0x0D94
#define GL_MAP1_TEXTURE_COORD_3  0x0D95
#define GL_MAP1_TEXTURE_COORD_4  0x0D96
#define GL_MAP1_VERTEX_3         0x0D97
#define GL_MAP1_VERTEX_4         0x0D98
#define GL_MAP2_COLOR_4          0x0DB0
#define GL_MAP2_INDEX            0x0DB1
#define GL_MAP2_NORMAL           0x0DB2
#define GL_MAP2_TEXTURE_COORD_1  0x0DB3
#define GL_MAP2_TEXTURE_COORD_2  0x0DB4
#define GL_MAP2_TEXTURE_COORD_3  0x0DB5
#define GL_MAP2_TEXTURE_COORD_4  0x0DB6
#define GL_MAP2_VERTEX_3         0x0DB7
#define GL_MAP2_VERTEX_4         0x0DB8
#define GL_MAP1_GRID_DOMAIN      0x0DD0
#define GL_MAP1_GRID_SEGMENTS    0x0DD1
#define GL_MAP2_GRID_DOMAIN      0x0DD2
#define GL_MAP2_GRID_SEGMENTS    0x0DD3
#define GL_COEFF                 0x0A00
#define GL_ORDER                 0x0A01
#define GL_DOMAIN                0x0A02
#define GL_MAX_LIST_NESTING      0x0B31

/* polygons */
#define GL_POINT                 0x1B00
#define GL_LINE                  0x1B01
#define GL_FILL                  0x1B02
#define GL_CW                    0x0900
#define GL_CCW                   0x0901
#define GL_FRONT                 0x0404
#define GL_BACK                  0x0405
#define GL_POLYGON_MODE          0x0B40
#define GL_POLYGON_SMOOTH        0x0B41
#define GL_POLYGON_STIPPLE       0x0B42
#define GL_EDGE_FLAG             0x0B43
#define GL_CULL_FACE             0x0B44
#define GL_CULL_FACE_MODE        0x0B45
#define GL_FRONT_FACE            0x0B46
#define GL_POLYGON_OFFSET_FACTOR 0x8038
#define GL_POLYGON_OFFSET_UNITS  0x2A00
#define GL_POLYGON_OFFSET_POINT  0x2A01
#define GL_POLYGON_OFFSET_LINE   0x2A02

/* Shader Source */
#define GL_SHADING_LANGUAGE_VERSION       0x8B8C
#define GL_COMPILE_STATUS                 0x8B81
#define GL_INFO_LOG_LENGTH                0x8B84
#define GL_SHADER_SOURCE_LENGTH           0x8B88
#define GL_SHADER_COMPILER                0x8DFA

/* Shader Binary */
#define GL_SHADER_BINARY_FORMATS          0x8DF8
#define GL_NUM_SHADER_BINARY_FORMATS      0x8DF9

/* Shader Precision-Specified Types */
#define GL_LOW_FLOAT                      0x8DF0
#define GL_MEDIUM_FLOAT                   0x8DF1
#define GL_HIGH_FLOAT                     0x8DF2
#define GL_LOW_INT                        0x8DF3
#define GL_MEDIUM_INT                     0x8DF4
#define GL_HIGH_INT                       0x8DF5

/* Texture Parameters */
#define GL_TEXTURE_INTERNAL_FORMAT	      0x1003
#define GL_TEXTURE_RED_SIZE			      0x805C
#define GL_TEXTURE_GREEN_SIZE			  0x805D
#define GL_TEXTURE_BLUE_SIZE			  0x805E
#define GL_TEXTURE_ALPHA_SIZE			  0x805F
#define GL_TEXTURE_LUMINANCE_SIZE		  0x8060
#define GL_TEXTURE_INTENSITY_SIZE		  0x8061
#define GL_TEXTURE_RED_TYPE               0x8C10
#define GL_TEXTURE_GREEN_TYPE             0x8C11
#define GL_TEXTURE_BLUE_TYPE              0x8C12
#define GL_TEXTURE_ALPHA_TYPE             0x8C13
#define GL_TEXTURE_DEPTH_TYPE             0x8C16
#define GL_TEXTURE_DEPTH				  0x8071
#define GL_TEXTURE_DEPTH_SIZE             0x884A
#define GL_TEXTURE_COMPRESSED_IMAGE_SIZE  0x86A0
#define GL_TEXTURE_COMPRESSED			  0x86A1
#define GL_TEXTURE_MAX_LEVEL			  0x813D

/* Compressed Textures */
#define GL_COMPRESSED_RGB_S3TC_DXT1_EXT      0x83F0
#define GL_COMPRESSED_RGBA_S3TC_DXT1_EXT     0x83F1
#define GL_COMPRESSED_RGBA_S3TC_DXT3_EXT     0x83F2
#define GL_COMPRESSED_RGBA_S3TC_DXT5_EXT     0x83F3
#define GL_TEXTURE_COMPRESSION_HINT          0x84EF

/* S3TC with sRGB */
#define GL_COMPRESSED_SRGB_S3TC_DXT1_EXT        0x8C4C
#define GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT  0x8C4D
#define GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT  0x8C4E
#define GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT  0x8C4F


/* Render Mode */
#define GL_SELECT                         0x1c02
#define GL_RENDER                         0x1C00

/* Interleaved Array */
#define GL_V2F					0x2A20
#define GL_V3F					0x2A21
#define GL_C4UB_V2F				0x2A22
#define GL_C4UB_V3F				0x2A23
#define GL_C3F_V3F				0x2A24
#define GL_N3F_V3F				0x2A25
#define GL_C4F_N3F_V3F				0x2A26
#define GL_T2F_V3F				0x2A27
#define GL_T4F_V4F				0x2A28
#define GL_T2F_C4UB_V3F				0x2A29
#define GL_T2F_C3F_V3F				0x2A2A
#define GL_T2F_N3F_V3F				0x2A2B
#define GL_T2F_C4F_N3F_V3F			0x2A2C
#define GL_T4F_C4F_N3F_V4F			0x2A2D

/* Buffers Array */
#define GL_BUFFER_SIZE                    0x8764
#define GL_BUFFER_USAGE                   0x8765
#define GL_QUERY_COUNTER_BITS             0x8864
#define GL_CURRENT_QUERY                  0x8865
#define GL_QUERY_RESULT                   0x8866
#define GL_QUERY_RESULT_AVAILABLE         0x8867
#define GL_ARRAY_BUFFER                   0x8892
#define GL_ELEMENT_ARRAY_BUFFER           0x8893
#define GL_ARRAY_BUFFER_BINDING           0x8894
#define GL_ELEMENT_ARRAY_BUFFER_BINDING   0x8895
#define GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING 0x889F
#define GL_READ_ONLY                      0x88B8
#define GL_WRITE_ONLY                     0x88B9
#define GL_READ_WRITE                     0x88BA
#define GL_BUFFER_ACCESS                  0x88BB
#define GL_BUFFER_MAPPED                  0x88BC
#define GL_BUFFER_MAP_POINTER             0x88BD
#define GL_STREAM_DRAW                    0x88E0
#define GL_STREAM_READ                    0x88E1
#define GL_STREAM_COPY                    0x88E2
#define GL_STATIC_DRAW                    0x88E4
#define GL_STATIC_READ                    0x88E5
#define GL_STATIC_COPY                    0x88E6
#define GL_DYNAMIC_DRAW                   0x88E8
#define GL_DYNAMIC_READ                   0x88E9
#define GL_DYNAMIC_COPY                   0x88EA
#define GL_SRC1_ALPHA                     0x8589
#define GL_VERTEX_ARRAY_BUFFER_BINDING    0x8896
#define GL_NORMAL_ARRAY_BUFFER_BINDING    0x8897
#define GL_COLOR_ARRAY_BUFFER_BINDING     0x8898
#define GL_INDEX_ARRAY_BUFFER_BINDING     0x8899
#define GL_TEXTURE_COORD_ARRAY_BUFFER_BINDING 0x889A
#define GL_EDGE_FLAG_ARRAY_BUFFER_BINDING 0x889B
#define GL_SECONDARY_COLOR_ARRAY_BUFFER_BINDING 0x889C
#define GL_FOG_COORDINATE_ARRAY_BUFFER_BINDING 0x889D
#define GL_WEIGHT_ARRAY_BUFFER_BINDING    0x889E
#define GL_MAP_READ_BIT                   0x0001
#define GL_MAP_WRITE_BIT                  0x0002
#define GL_BUFFER_ACCESS_FLAGS            0x911F
#define GL_BUFFER_MAP_LENGTH              0x9120
#define GL_BUFFER_MAP_OFFSET              0x9121
#define GL_READ_ONLY                      0x88B8
#define GL_WRITE_ONLY                     0x88B9
#define GL_READ_WRITE                     0x88BA
#define GL_PIXEL_PACK_BUFFER              0x88EB
#define GL_PIXEL_UNPACK_BUFFER            0x88EC
#define GL_PIXEL_UNPACK_BUFFER_BINDING	  0x88EF
#define GL_PIXEL_PACK_BUFFER_BINDING      0x88ED
#define GL_CURRENT_VERTEX_ATTRIB          0x8626
#define GL_MAP_PERSISTENT_BIT             0x00000040
#define GL_QUERY_BUFFER_BINDING_AMD       0x9193
#define GL_COPY_READ_BUFFER_BINDING       0x8F36
#define GL_COPY_READ_BUFFER               GL_COPY_READ_BUFFER_BINDING
#define GL_COPY_WRITE_BUFFER_BINDING      0x8F37
#define GL_COPY_WRITE_BUFFER              GL_COPY_WRITE_BUFFER_BINDING


/* Framebuffers */
#define GL_INVALID_FRAMEBUFFER_OPERATION  0x0506
#define GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING 0x8210
#define GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE 0x8211
#define GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE 0x8212
#define GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE 0x8213
#define GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE 0x8214
#define GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE 0x8215
#define GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE 0x8216
#define GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE 0x8217
#define GL_FRAMEBUFFER_DEFAULT            0x8218
#define GL_FRAMEBUFFER_UNDEFINED          0x8219
#define GL_DEPTH_STENCIL_ATTACHMENT       0x821A
#define GL_MAX_RENDERBUFFER_SIZE          0x84E8
#define GL_DEPTH_STENCIL                  0x84F9
#define GL_UNSIGNED_INT_24_8              0x84FA
#define GL_DEPTH24_STENCIL8               0x88F0
#define GL_TEXTURE_STENCIL_SIZE           0x88F1
#define GL_TEXTURE_RED_TYPE               0x8C10
#define GL_TEXTURE_GREEN_TYPE             0x8C11
#define GL_TEXTURE_BLUE_TYPE              0x8C12
#define GL_TEXTURE_ALPHA_TYPE             0x8C13
#define GL_TEXTURE_DEPTH_TYPE             0x8C16
#define GL_UNSIGNED_NORMALIZED            0x8C17
#define GL_FRAMEBUFFER_BINDING            0x8CA6
#define GL_DRAW_FRAMEBUFFER_BINDING       GL_FRAMEBUFFER_BINDING
#define GL_RENDERBUFFER_BINDING           0x8CA7
#define GL_READ_FRAMEBUFFER               0x8CA8
#define GL_DRAW_FRAMEBUFFER               0x8CA9
#define GL_READ_FRAMEBUFFER_BINDING       0x8CAA
#define GL_RENDERBUFFER_SAMPLES           0x8CAB
#define GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE 0x8CD0
#define GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME 0x8CD1
#define GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL 0x8CD2
#define GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE 0x8CD3
#define GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER 0x8CD4
#define GL_FRAMEBUFFER_COMPLETE           0x8CD5
#define GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT 0x8CD6
#define GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT 0x8CD7
#define GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER 0x8CDB
#define GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER 0x8CDC
#define GL_FRAMEBUFFER_UNSUPPORTED        0x8CDD
#define GL_MAX_COLOR_ATTACHMENTS          0x8CDF
#define GL_COLOR_ATTACHMENT0              0x8CE0
#define GL_COLOR_ATTACHMENT1              0x8CE1
#define GL_COLOR_ATTACHMENT2              0x8CE2
#define GL_COLOR_ATTACHMENT3              0x8CE3
#define GL_COLOR_ATTACHMENT4              0x8CE4
#define GL_COLOR_ATTACHMENT5              0x8CE5
#define GL_COLOR_ATTACHMENT6              0x8CE6
#define GL_COLOR_ATTACHMENT7              0x8CE7
#define GL_COLOR_ATTACHMENT8              0x8CE8
#define GL_COLOR_ATTACHMENT9              0x8CE9
#define GL_COLOR_ATTACHMENT10             0x8CEA
#define GL_COLOR_ATTACHMENT11             0x8CEB
#define GL_COLOR_ATTACHMENT12             0x8CEC
#define GL_COLOR_ATTACHMENT13             0x8CED
#define GL_COLOR_ATTACHMENT14             0x8CEE
#define GL_COLOR_ATTACHMENT15             0x8CEF
#define GL_DEPTH_ATTACHMENT               0x8D00
#define GL_STENCIL_ATTACHMENT             0x8D20
#define GL_FRAMEBUFFER                    0x8D40
#define GL_RENDERBUFFER                   0x8D41
#define GL_RENDERBUFFER_WIDTH             0x8D42
#define GL_RENDERBUFFER_HEIGHT            0x8D43
#define GL_RENDERBUFFER_INTERNAL_FORMAT   0x8D44
#define GL_STENCIL_INDEX1                 0x8D46
#define GL_STENCIL_INDEX4                 0x8D47
#define GL_STENCIL_INDEX8                 0x8D48
#define GL_STENCIL_INDEX16                0x8D49
#define GL_RENDERBUFFER_RED_SIZE          0x8D50
#define GL_RENDERBUFFER_GREEN_SIZE        0x8D51
#define GL_RENDERBUFFER_BLUE_SIZE         0x8D52
#define GL_RENDERBUFFER_ALPHA_SIZE        0x8D53
#define GL_RENDERBUFFER_DEPTH_SIZE        0x8D54
#define GL_RENDERBUFFER_STENCIL_SIZE      0x8D55
#define GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE 0x8D56
#define GL_MAX_SAMPLES                    0x8D57
#define GL_INDEX                          0x8222
#define GL_TEXTURE_LUMINANCE_TYPE         0x8C14
#define GL_TEXTURE_INTENSITY_TYPE         0x8C15
#define GL_DEPTH_COMPONENT32              0x81A7
#define GL_DEPTH_COMPONENT24              0x81A6
#define GL_DEPTH_COMPONENT16              0x81A5
#define GL_DEPTH_COMPONENT                0x1902
#define GL_MAX_DRAW_BUFFERS_ARB           0x8824

// point sprite extension
#define GL_POINT_SPRITE_COORD_ORIGIN      0x8CA0
#define GL_LOWER_LEFT                     0x8CA1
#define GL_UPPER_LEFT                     0x8CA2
#define GL_PROGRAM_POINT_SIZE             0x8642

// clear buffer
#define GL_COLOR                          0x1800
#define GL_DEPTH                          0x1801
#define GL_STENCIL                        0x1802


// direct state
#define GL_MATRIX0_ARB                    0x88C0
#define GL_PROGRAM_MATRIX_EXT             0x8E2D
#define GL_TRANSPOSE_PROGRAM_MATRIX_EXT   0x8E2E
#define GL_PROGRAM_MATRIX_STACK_DEPTH_EXT 0x8E2F
#define GL_CURRENT_MATRIX_STACK_DEPTH_ARB 0x8640
#define GL_TRANSPOSE_CURRENT_MATRIX_ARB   0x88B7
#define GL_CURRENT_MATRIX_ARB             0x8641
#define GL_TEXTURE_BUFFER_FORMAT_EXT      0x8C2E
#define GL_TEXTURE_BUFFER_DATA_STORE_BINDING_EXT 0x8C2D
#define GL_TEXTURE_BINDING_CUBE_MAP		  0x8514
#define GL_TEXTURE_BINDING_BUFFER_EXT     0x8C2C
#define GL_TEXTURE_BINDING_1D			  0x8068
#define GL_TEXTURE_BINDING_2D			  0x8069
#define GL_TEXTURE_BINDING_3D             0x806A
#define GL_TEXTURE_BINDING_2D_ARRAY       0x8C1D
#define GL_TEXTURE_BINDING_1D_ARRAY       0x8C1C
#define GL_CURRENT_RASTER_TEXTURE_COORDS  0x0B06
#define GL_TEXTURE_COORD_ARRAY_COUNT      0x808B

// cube mapping
#define GL_TEXTURE_CUBE_MAP               0x8513
#define GL_TEXTURE_BINDING_CUBE_MAP       0x8514
#define GL_TEXTURE_CUBE_MAP_POSITIVE_X    0x8515
#define GL_TEXTURE_CUBE_MAP_NEGATIVE_X    0x8516
#define GL_TEXTURE_CUBE_MAP_POSITIVE_Y    0x8517
#define GL_TEXTURE_CUBE_MAP_NEGATIVE_Y    0x8518
#define GL_TEXTURE_CUBE_MAP_POSITIVE_Z    0x8519
#define GL_TEXTURE_CUBE_MAP_NEGATIVE_Z    0x851A
#define GL_PROXY_TEXTURE_CUBE_MAP         0x851B
#define GL_AX_CUBE_MAP_TEXTURE_SIZE       0x851C

// Shaders
#define GL_FRAGMENT_SHADER                  0x8B30
#define GL_VERTEX_SHADER                    0x8B31
#define GL_MAX_VERTEX_ATTRIBS               0x8869
#define GL_MAX_VERTEX_UNIFORM_VECTORS       0x8DFB
#define GL_MAX_VARYING_VECTORS              0x8DFC
#define GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS 0x8B4D
#define GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS   0x8B4C
#define GL_MAX_TEXTURE_IMAGE_UNITS          0x8872
#define GL_MAX_FRAGMENT_UNIFORM_VECTORS     0x8DFD
#define GL_SHADER_TYPE                      0x8B4F
#define GL_DELETE_STATUS                    0x8B80
#define GL_LINK_STATUS                      0x8B82
#define GL_VALIDATE_STATUS                  0x8B83
#define GL_ATTACHED_SHADERS                 0x8B85
#define GL_ACTIVE_UNIFORMS                  0x8B86
#define GL_ACTIVE_UNIFORM_MAX_LENGTH        0x8B87
#define GL_ACTIVE_ATTRIBUTES                0x8B89
#define GL_ACTIVE_ATTRIBUTE_MAX_LENGTH      0x8B8A
#define GL_SHADING_LANGUAGE_VERSION         0x8B8C
#define GL_CURRENT_PROGRAM                  0x8B8D
// Shaders extensions
#define GL_PROGRAM_BINARY_LENGTH            0x8741
#define GL_NUM_PROGRAM_BINARY_FORMATS       0x87FE
#define GL_PROGRAM_BINARY_FORMATS           0x87FF
#define GL_PROGRAM_OBJECT_ARB               0x8B40

// Vertex Arrays
#define GL_VERTEX_ATTRIB_ARRAY_ENABLED        0x8622
#define GL_VERTEX_ATTRIB_ARRAY_SIZE           0x8623
#define GL_VERTEX_ATTRIB_ARRAY_STRIDE         0x8624
#define GL_VERTEX_ATTRIB_ARRAY_TYPE           0x8625
#define GL_VERTEX_ATTRIB_ARRAY_NORMALIZED     0x886A
#define GL_VERTEX_ATTRIB_ARRAY_POINTER        0x8645
#define GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING 0x889F

// Uniform Types
#define GL_FLOAT_VEC2                     0x8B50
#define GL_FLOAT_VEC3                     0x8B51
#define GL_FLOAT_VEC4                     0x8B52
#define GL_INT_VEC2                       0x8B53
#define GL_INT_VEC3                       0x8B54
#define GL_INT_VEC4                       0x8B55
#define GL_BOOL                           0x8B56
#define GL_BOOL_VEC2                      0x8B57
#define GL_BOOL_VEC3                      0x8B58
#define GL_BOOL_VEC4                      0x8B59
#define GL_FLOAT_MAT2                     0x8B5A
#define GL_FLOAT_MAT3                     0x8B5B
#define GL_FLOAT_MAT4                     0x8B5C
#define GL_SAMPLER_2D                     0x8B5E
#define GL_SAMPLER_CUBE                   0x8B60

// Getter
#define GL_NUM_EXTENSIONS                 0x821D

// Anisotropic
#define GL_TEXTURE_MAX_ANISOTROPY         0x84FE
#define GL_MAX_TEXTURE_MAX_ANISOTROPY     0x84FF

// ATI_texture_env_combine3
#define GL_MODULATE_ADD_ATI                     0x8744
#define GL_MODULATE_SIGNED_ADD_ATI              0x8745
#define GL_MODULATE_SUBTRACT_ATI                0x8746

// ATIX_texture_env_route
#define GL_SECONDARY_COLOR_ATIX                 0x8747
#define GL_TEXTURE_OUTPUT_RGB_ATIX              0x8748
#define GL_TEXTURE_OUTPUT_ALPHA_ATIX            0x8749

//GL_NV_texture_env_combine4
#define GL_COMBINE4                             0x8503
#define GL_SRC3_RGB                             0x8583
#define GL_SRC3_ALPHA                           0x858B
#define GL_OPERAND3_RGB                         0x8593
#define GL_OPERAND3_ALPHA                       0x859B

//GL_NV_fog_distance
#define GL_FOG_DISTANCE_MODE_NV                 0x855A
#define GL_EYE_RADIAL_NV                        0x855B
#define GL_EYE_PLANE_ABSOLUTE_NV                0x855C

//GL_ARB_instanced_arrays
#define GL_VERTEX_ATTRIB_ARRAY_DIVISOR          0x88FE

//GL_OES_get_program_binary
#define GL_PROGRAM_BINARY_LENGTH_OES            0x8741
#define GL_NUM_PROGRAM_BINARY_FORMATS_OES       0x87FE
#define GL_PROGRAM_BINARY_FORMATS_OES           0x87FF

//Clamp color
#define GL_CLAMP_READ_COLOR                     0x891C

//GL_ARB_vertex_program
#define GL_MATRIX0_ARB                                  0x88C0
#define GL_MATRIX1_ARB                                  0x88C1
#define GL_MATRIX2_ARB                                  0x88C2
#define GL_MATRIX3_ARB                                  0x88C3
#define GL_MATRIX4_ARB                                  0x88C4
#define GL_MATRIX5_ARB                                  0x88C5
#define GL_MATRIX6_ARB                                  0x88C6
#define GL_MATRIX7_ARB                                  0x88C7
#define GL_MATRIX8_ARB                                  0x88C8
#define GL_MATRIX9_ARB                                  0x88C9
#define GL_MATRIX10_ARB                                 0x88CA
#define GL_MATRIX11_ARB                                 0x88CB
#define GL_MATRIX12_ARB                                 0x88CC
#define GL_MATRIX13_ARB                                 0x88CD
#define GL_MATRIX14_ARB                                 0x88CE
#define GL_MATRIX15_ARB                                 0x88CF
#define GL_MATRIX16_ARB                                 0x88D0
#define GL_MATRIX17_ARB                                 0x88D1
#define GL_MATRIX18_ARB                                 0x88D2
#define GL_MATRIX19_ARB                                 0x88D3
#define GL_MATRIX20_ARB                                 0x88D4
#define GL_MATRIX21_ARB                                 0x88D5
#define GL_MATRIX22_ARB                                 0x88D6
#define GL_MATRIX23_ARB                                 0x88D7
#define GL_MATRIX24_ARB                                 0x88D8
#define GL_MATRIX25_ARB                                 0x88D9
#define GL_MATRIX26_ARB                                 0x88DA
#define GL_MATRIX27_ARB                                 0x88DB
#define GL_MATRIX28_ARB                                 0x88DC
#define GL_MATRIX29_ARB                                 0x88DD
#define GL_MATRIX30_ARB                                 0x88DE
#define GL_MATRIX31_ARB                                 0x88DF

#define GL_PROGRAM_ERROR_STRING_ARB                     0x8874

#define GL_PROGRAM_ERROR_POSITION_ARB                   0x864B
#define GL_CURRENT_MATRIX_ARB                           0x8641
#define GL_TRANSPOSE_CURRENT_MATRIX_ARB                 0x88B7
#define GL_CURRENT_MATRIX_STACK_DEPTH_ARB               0x8640
#define GL_MAX_VERTEX_ATTRIBS_ARB                       0x8869
#define GL_MAX_PROGRAM_MATRICES_ARB                     0x862F
#define GL_MAX_PROGRAM_MATRIX_STACK_DEPTH_ARB           0x862E

#define GL_PROGRAM_STRING_ARB                           0x8628

#define GL_PROGRAM_LENGTH_ARB                           0x8627
#define GL_PROGRAM_FORMAT_ARB                           0x8876
#define GL_PROGRAM_BINDING_ARB                          0x8677
#define GL_PROGRAM_INSTRUCTIONS_ARB                     0x88A0
#define GL_MAX_PROGRAM_INSTRUCTIONS_ARB                 0x88A1
#define GL_PROGRAM_NATIVE_INSTRUCTIONS_ARB              0x88A2
#define GL_MAX_PROGRAM_NATIVE_INSTRUCTIONS_ARB          0x88A3
#define GL_PROGRAM_TEMPORARIES_ARB                      0x88A4
#define GL_MAX_PROGRAM_TEMPORARIES_ARB                  0x88A5
#define GL_PROGRAM_NATIVE_TEMPORARIES_ARB               0x88A6
#define GL_MAX_PROGRAM_NATIVE_TEMPORARIES_ARB           0x88A7
#define GL_PROGRAM_PARAMETERS_ARB                       0x88A8
#define GL_MAX_PROGRAM_PARAMETERS_ARB                   0x88A9
#define GL_PROGRAM_NATIVE_PARAMETERS_ARB                0x88AA
#define GL_MAX_PROGRAM_NATIVE_PARAMETERS_ARB            0x88AB
#define GL_PROGRAM_ATTRIBS_ARB                          0x88AC
#define GL_MAX_PROGRAM_ATTRIBS_ARB                      0x88AD
#define GL_PROGRAM_NATIVE_ATTRIBS_ARB                   0x88AE
#define GL_MAX_PROGRAM_NATIVE_ATTRIBS_ARB               0x88AF
#define GL_PROGRAM_ADDRESS_REGISTERS_ARB                0x88B0
#define GL_MAX_PROGRAM_ADDRESS_REGISTERS_ARB            0x88B1
#define GL_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB         0x88B2
#define GL_MAX_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB     0x88B3
#define GL_MAX_PROGRAM_LOCAL_PARAMETERS_ARB             0x88B4
#define GL_MAX_PROGRAM_ENV_PARAMETERS_ARB               0x88B5
#define GL_PROGRAM_UNDER_NATIVE_LIMITS_ARB              0x88B6

#define GL_VERTEX_ATTRIB_ARRAY_POINTER_ARB              0x8645

#define GL_VERTEX_ATTRIB_ARRAY_ENABLED_ARB              0x8622
#define GL_VERTEX_ATTRIB_ARRAY_SIZE_ARB                 0x8623
#define GL_VERTEX_ATTRIB_ARRAY_STRIDE_ARB               0x8624
#define GL_VERTEX_ATTRIB_ARRAY_TYPE_ARB                 0x8625
#define GL_VERTEX_ATTRIB_ARRAY_NORMALIZED_ARB           0x886A
#define GL_CURRENT_VERTEX_ATTRIB_ARB                    0x8626

#define GL_PROGRAM_FORMAT_ASCII_ARB                     0x8875

#define GL_VERTEX_PROGRAM_POINT_SIZE_ARB                0x8642
#define GL_VERTEX_PROGRAM_TWO_SIDE_ARB                  0x8643
#define GL_COLOR_SUM_ARB                                0x8458

#define GL_VERTEX_PROGRAM_ARB                           0x8620

// ARB_fragment_program 

#define GL_MAX_TEXTURE_COORDS_ARB                       0x8871
#define GL_MAX_TEXTURE_IMAGE_UNITS_ARB                  0x8872

#define GL_PROGRAM_ALU_INSTRUCTIONS_ARB                 0x8805
#define GL_PROGRAM_TEX_INSTRUCTIONS_ARB                 0x8806
#define GL_PROGRAM_TEX_INDIRECTIONS_ARB                 0x8807
#define GL_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB          0x8808
#define GL_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB          0x8809
#define GL_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB          0x880A
#define GL_MAX_PROGRAM_ALU_INSTRUCTIONS_ARB             0x880B
#define GL_MAX_PROGRAM_TEX_INSTRUCTIONS_ARB             0x880C
#define GL_MAX_PROGRAM_TEX_INDIRECTIONS_ARB             0x880D
#define GL_MAX_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB      0x880E
#define GL_MAX_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB      0x880F
#define GL_MAX_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB      0x8810

#define GL_FRAGMENT_PROGRAM_ARB                         0x8804

// EXT_draw_buffers

#define GL_MAX_COLOR_ATTACHMENTS_EXT                    0x8CDF

// Sampler
#define GL_SAMPLER_BINDING                              0x8919
#define GL_TEXTURE_COMPARE_FUNC                         0x884D
#define GL_COMPARE_REF_TO_TEXTURE                       0x884E
#define GL_NONE                                         0x0

// Queries
#define GL_TIME_ELAPSED                                 0x88BF
#define GL_TIMESTAMP                                    0x8E28
#define GL_SAMPLES_PASSED                               0x8914
#define GL_ANY_SAMPLES_PASSED                           0x8C2F
#define GL_ANY_SAMPLES_PASSED_CONSERVATIVE              0x8D6A
#define GL_PRIMITIVES_GENERATED                         0x8C87
#define GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN        0x8C88
#define GL_QUERY_RESULT_NO_WAIT                         0x9194

// Blend
#define GL_FUNC_ADD                                     0x8006
#define GL_FUNC_SUBTRACT                                0x800A
#define GL_FUNC_REVERSE_SUBTRACT                        0x800B
#define GL_MIN                                          0x8007
#define GL_MAX                                          0x8008

#endif // _GL4ES_CONST_H_
