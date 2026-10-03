#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include "string_utils.h"

typedef struct hack_s {
    char* sign;
    int   n;
    char* next[5];
} hack_t;

static const hack_t gl4es_hacks[] = {
// this is for Guacamelee (yep, there is a lot of hacks, only int -> float conversions)
// 1
{"float edgeGlow = step ( 0.2 , pow ( clamp ( ( dot ( vec2 ( 1 * sign ( v_texcoord3 . z ) , 1 ) , normalize ( quadCoord . xy - 0.5 ) ) - 0.4 + depth * 2.0 ) , 0.0  , 1.0  ) , 25 ) ) ;",
1,
{"float edgeGlow = step ( 0.2 , pow ( clamp ( ( dot ( vec2 ( 1.0 * sign ( v_texcoord3 . z ) , 1.0 ) , normalize ( quadCoord . xy - 0.5 ) ) - 0.4 + depth * 2.0 ) , 0.0  , 1.0  ) , 25.0 ) ) ;"}},
// 2
{"float litfire = max ( dot ( normalize ( drops1 . rgb ) , normalize ( vec3 ( - 1 , 0 , pow ( max ( 1.0 - ocoord . x , 0.0 ) , 9 ) ) ) ) , 0 ) ;",
1,
{"float litfire = max ( dot ( normalize ( drops1 . rgb ) , normalize ( vec3 ( - 1.0 , 0.0 , pow ( max ( 1.0 - ocoord . x , 0.0 ) , 9.0 ) ) ) ) , 0.0 ) ;"}},
// 3
{"if ( ( normalizedDepth ) < 0.0  ) discard ; ;\nif ( depth < 0 )",
1,
{"if ( ( normalizedDepth ) < 0.0  ) discard ; ;\nif ( depth < 0.0 )"}},
// 4
{"gl_FragColor . rgba += glowHit ;\nif ( depth < 0 )",
1,
{"gl_FragColor . rgba += glowHit ;\nif ( depth < 0.0 )"}},
// 5
{"gl_FragColor . a *= pow ( clamp ( ( depth + 1 ) , 0.0  , 1.0  ) , 70 ) ;",
1,
{"gl_FragColor . a *= pow ( clamp ( ( depth + 1.0 ) , 0.0  , 1.0  ) , 70.0 ) ;"}},
// 6
{"if ( floor ( in_texcoord0 . y ) != 0 )",
1,
{"if ( floor ( in_texcoord0 . y ) != 0.0 )"}},
// 7
{"if ( in_position0 . y < 0 )",
1,
{"if ( in_position0 . y < 0.0 )"}},
// 8
{"if ( in_position0 . x < 0 )",
1,
{"if ( in_position0 . x < 0.0 )"}},
// 9
{"branchB . y = 0 ;",
1,
{"branchB . y = 0.0 ;"}},
// 10
{"branchB . x = 0 ;",
1,
{"branchB . x = 0.0 ;"}},

// this is for Battle Block Theater
// 1
{"   if(texColor.w == 0)\n       gl_FragColor = texColor;",
1,
{"   if(texColor.w == 0.0)\n       gl_FragColor = texColor;"}},
// 2
{"if(dist1 > 0)       {           float lightVal = (1-dist1) * light1Luminosity;",
1,
{"if(dist1 > 0.0)       {           float lightVal = (1.0-dist1) * light1Luminosity;"}},
// 3
{"float lightVal = 0;",
1,
{"float lightVal = 0.0;"}},
// 4
{"       if(dist1 > 0)\n"
"       {\n"
"			if(dist1 > 1)\n"
"				dist1 = 1;\n",
1,{
"       if(dist1 > 0.0)\n"
"       {\n"
"			if(dist1 > 1.0)\n"
"				dist1 = 1.0;\n"}},
// 5
{"lightVal += (1-dist1) * light1Luminosity;",
1,{"lightVal += (1.0-dist1) * light1Luminosity;"}},
// 6
{"lightVal += (1-dist1) * light2Luminosity;",
1,{"lightVal += (1.0-dist1) * light2Luminosity;"}},
// 7
{"lightVal += (1-dist1) * light3Luminosity;",
1,{"lightVal += (1.0-dist1) * light3Luminosity;"}},
// 8
{"if(lightVal > 1)\n"
"			lightVal = 1;",
1,{
"if(lightVal > 1.0)\n"
"			lightVal = 1.0;"}},
// 9
{"if(lightVal > 1)\n"
"           lightVal = 1;", // space and tabs make a difference...
1,{
"if(lightVal > 1.0)\n"
"           lightVal = 1.0;"}},

// For Night of the Zombie / Irrlicht 1.9.0
{"gl_FragColor = (sample*(1-grayScaleFactor)) + (gray*grayScaleFactor);",
1,{"gl_FragColor = (sample*(1.0-grayScaleFactor)) + (gray*grayScaleFactor);"}},

// For Knytt Underground
{"vec2 val = texture_coordinate1+coeff*2*(i/float(iterations-1.0) - 0.5);",
1,{"vec2 val = texture_coordinate1+coeff*2.0*(float(i)/float(iterations-1) - 0.5);"}},

{"    b /= iterations;",
1,{"    b /= float(iterations);"}},

// For Antichamber
{"attribute vec4 _Un_AttrPosition0;\n"
"vec4 Un_AttrPosition0 = _Un_AttrPosition0;\n",
1, {"attribute vec4 _Un_AttrPosition0;\n"
"#define Un_AttrPosition0 _Un_AttrPosition0\n"}},

{"attribute vec4 _Un_AttrColor0;\n"
"vec4 Un_AttrColor0 = _Un_AttrColor0;\n",
1, {"attribute vec4 _Un_AttrColor0;\n"
"#define Un_AttrColor0 _Un_AttrColor0\n"}},

{"attribute vec4 _Un_AttrColor1;\n"
"vec4 Un_AttrColor1 = _Un_AttrColor1;\n",
1, {"attribute vec4 _Un_AttrColor1;\n"
"#define Un_AttrColor1 _Un_AttrColor1\n"}},

{"attribute vec4 _Un_AttrTangent0;\n"
"vec4 Un_AttrTangent0 = _Un_AttrTangent0;\n",
1, {"attribute vec4 _Un_AttrTangent0;\n"
"#define Un_AttrTangent0 _Un_AttrTangent0\n"}},

{"attribute vec4 _Un_AttrNormal0;\n"
"vec4 Un_AttrNormal0 = _Un_AttrNormal0;\n",
1, {"attribute vec4 _Un_AttrNormal0;\n"
"#define Un_AttrNormal0 _Un_AttrNormal0\n"}},

{"attribute vec4 _Un_AttrBlendIndices0;\n"
"vec4 Un_AttrBlendIndices0 = _Un_AttrBlendIndices0;\n",
1, {"attribute vec4 _Un_AttrBlendIndices0;\n"
"#define Un_AttrBlendIndices0 _Un_AttrBlendIndices0\n"}},

{"attribute vec4 _Un_AttrBlendWeight0;\n"
"vec4 Un_AttrBlendWeight0 = _Un_AttrBlendWeight0;\n",
1, {"attribute vec4 _Un_AttrBlendWeight0;\n"
"#define Un_AttrBlendWeight0 _Un_AttrBlendWeight0\n"}},

{"attribute vec4 _Un_AttrBinormal0;\n"
"vec4 Un_AttrBinormal0 = _Un_AttrBinormal0;\n",
1, {"attribute vec4 _Un_AttrBinormal0;\n"
"#define Un_AttrBinormal0 _Un_AttrBinormal0\n"}},

{"attribute vec4 _Un_AttrTexCoord0;\n"
"vec4 Un_AttrTexCoord0 = _Un_AttrTexCoord0;\n",
1, {"attribute vec4 _Un_AttrTexCoord0;\n"
"#define Un_AttrTexCoord0 _Un_AttrTexCoord0\n"}},

{"attribute vec4 _Un_AttrTexCoord1;\n"
"vec4 Un_AttrTexCoord1 = _Un_AttrTexCoord1;\n",
1, {"attribute vec4 _Un_AttrTexCoord1;\n"
"#define Un_AttrTexCoord1 _Un_AttrTexCoord1\n"}},

{"attribute vec4 _Un_AttrTexCoord2;\n"
"vec4 Un_AttrTexCoord2 = _Un_AttrTexCoord2;\n",
1, {"attribute vec4 _Un_AttrTexCoord2;\n"
"#define Un_AttrTexCoord2 _Un_AttrTexCoord2\n"}},

{"attribute vec4 _Un_AttrTexCoord3;\n"
"vec4 Un_AttrTexCoord3 = _Un_AttrTexCoord3;\n",
1, {"attribute vec4 _Un_AttrTexCoord3;\n"
"#define Un_AttrTexCoord3 _Un_AttrTexCoord3\n"}},

{"attribute vec4 _Un_AttrTexCoord4;\n"
"vec4 Un_AttrTexCoord4 = _Un_AttrTexCoord4;\n",
1, {"attribute vec4 _Un_AttrTexCoord4;\n"
"#define Un_AttrTexCoord4 _Un_AttrTexCoord4\n"}},

{"attribute vec4 _Un_AttrTexCoord5;\n"
"vec4 Un_AttrTexCoord5 = _Un_AttrTexCoord5;\n",
1, {"attribute vec4 _Un_AttrTexCoord5;\n"
"#define Un_AttrTexCoord5 _Un_AttrTexCoord5\n"}},

{"attribute vec4 _Un_AttrTexCoord6;\n"
"vec4 Un_AttrTexCoord6 = _Un_AttrTexCoord6;\n",
1, {"attribute vec4 _Un_AttrTexCoord6;\n"
"#define Un_AttrTexCoord6 _Un_AttrTexCoord6\n"}},

{"attribute vec4 _Un_AttrTexCoord7;\n"
"vec4 Un_AttrTexCoord7 = _Un_AttrTexCoord7;\n",
1, {"attribute vec4 _Un_AttrTexCoord7;\n"
"#define Un_AttrTexCoord7 _Un_AttrTexCoord7\n"}},

// for IcewindDale
{"uniform highp \tvec2 \t\tuTcScale;",
1, {"uniform mediump vec2 \t\tuTcScale;"}},

// for OpenMW
{"uniform bool simpleWater = false;",
1, {"uniform bool simpleWater;"}},

// for Lethal League
{"uniform vec4 Color = vec4(1.0, 1.0, 1.0, 1.0);",
1, {"uniform vec4 Color;"}},

// for ioQuake3
{"float c[5] = float[5](1.0, 0.9238795325, 0.7071067812, 0.3826834324, 0.0);",
1, {"float c[5]; c[0]=1.0; c[1]=0.9238795325; c[2]=0.7071067812; c[3]=0.3826834324; c[4]=0.0;"}},

{"float c[7] = float[7](1.0, 0.9659258263, 0.8660254038, 0.7071067812, 0.5, 0.2588190451, 0.0);",
1, {"float c[7]; c[0]=1.0; c[1]=0.9659258263; c[2]=0.8660254038; c[3]=0.7071067812; c[4]=0.5; c[5]=0.2588190451; c[6]=0.0;"}},

{"float scale = 2.0 / r_shadowMapSize;",
1, {"float scale = 2.0 / float(r_shadowMapSize);"}},

{"vec2 poissonDisc[9] = vec2[9](\n"
"vec2(-0.7055767, 0.196515),    vec2(0.3524343, -0.7791386),\n"
"vec2(0.2391056, 0.9189604),    vec2(-0.07580382, -0.09224417),\n"
"vec2(0.5784913, -0.002528916), vec2(0.192888, 0.4064181),\n"
"vec2(-0.6335801, -0.5247476),  vec2(-0.5579782, 0.7491854),\n"
"vec2(0.7320465, 0.6317794)\n"
");\n",
3, {
"vec2 poissonDisc[9];\n",

"void main()\n"
"{\n",
"void main()\n"
"{\n"
"poissonDisc[0] = vec2(-0.7055767, 0.196515);  poissonDisc[1] = vec2(0.3524343, -0.7791386);\n"
"poissonDisc[2] = vec2(0.2391056, 0.9189604);  poissonDisc[3] = vec2(-0.07580382, -0.09224417);\n"
"poissonDisc[4] = vec2(0.5784913, -0.002528916);poissonDisc[5]= vec2(0.192888, 0.4064181);\n"
"poissonDisc[6] = vec2(-0.6335801, -0.5247476);poissonDisc[7] = vec2(-0.5579782, 0.7491854);\n"
"poissonDisc[8] = vec2(0.7320465, 0.6317794);\n"
}},

{"float result = 0;",
1, {"float result = 0.0;"}},

{"//float gauss[5] = float[5](0.30, 0.23, 0.097, 0.024, 0.0033);\n"
"float gauss[4] = float[4](0.40, 0.24, 0.054, 0.0044);\n"
"//float gauss[3] = float[3](0.60, 0.19, 0.0066);\n"
"#define GAUSS_SIZE 4\n",
3, {
"//float gauss[5] = float[5](0.30, 0.23, 0.097, 0.024, 0.0033);\n"
"float gauss[4];\n"
"//float gauss[3] = float[3](0.60, 0.19, 0.0066);\n"
"#define GAUSS_SIZE 4\n",

"void main()\n"
"{\n",
"void main()\n"
"{\n"
"        gauss[0]=0.40; gauss[1]=0.24; gauss[2]=0.054; gauss[3]=0.0044;\n"
}},

{"vec2 offset = direction * j;",
1,{"vec2 offset = direction * float(j);"}},

// for Silver
{"#version 140\r\n"
"\r\n"
"out vec2 var_uv;\r\n"
"in vec4 sg3d_position0;\r\n"
"void main()\r\n"
"{\r\n"
"\tgl_Position = sg3d_position0;\r\n"
"\tvar_uv=sg3d_position0.xy*0.5+0.5;\r\n"
"}\r\n",
1, {
"#version 120\n"
"\n"
"varying vec2 var_uv;\n"
"attribute vec4 sg3d_position0;\n"
"void main()\n"
"{\n"
"\tgl_Position = sg3d_position0;\n"
"\tvar_uv=sg3d_position0.xy*0.5+0.5;\n"
"}\n"
}},

{"#version 140\r\n"
"\r\n"
"in vec2 var_uv;\r\n"
"out vec4 var_out;\r\n"
"uniform vec4 var_param;\r\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE;\r\n"
"void main()\r\n"
"{\r\n"
"\tvar_out = texture(SG3D_TEXTURE_DIFFUSE, var_uv)*var_param;\r\n"
"}\r\n",
1, {"#version 120\n"
"\n"
"varying vec2 var_uv;\n"
"#define var_out gl_FragColor\n"
"uniform vec4 var_param;\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE;\n"
"void main()\n"
"{\n"
"\tvar_out = texture2D(SG3D_TEXTURE_DIFFUSE, var_uv)*var_param;\n"
"}\n"
}},

{"#version 140\r\n"
"\r\n"
"in vec2 var_uv;\r\n"
"out vec4 var_out;\r\n"
"uniform vec4 var_param;\r\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE;\r\n"
"void main()\r\n"
"{\r\n"
"\tvar_out = (texture(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy*-7.0)*55.0+\r\n"
"\t\t\ttexture(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy*-3.0)*330.0+\r\n"
"\t\t\ttexture(SG3D_TEXTURE_DIFFUSE, var_uv)*252.0+\r\n"
"\t\t\ttexture(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy* 3.0)*330.0+\r\n"
"\t\t\ttexture(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy* 7.0)*55.0)/1022.0;\r\n"
"}\r\n",
1, {"#version 120\n"
"\n"
"varying vec2 var_uv;\n"
"#define var_out gl_FragColor\n"
"uniform vec4 var_param;\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE;\n"
"void main()\n"
"{\n"
"\tvar_out = (texture2D(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy*-7.0)*55.0+\n"
"\t\t\ttexture2D(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy*-3.0)*330.0+\n"
"\t\t\ttexture2D(SG3D_TEXTURE_DIFFUSE, var_uv)*252.0+\n"
"\t\t\ttexture2D(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy* 3.0)*330.0+\n"
"\t\t\ttexture2D(SG3D_TEXTURE_DIFFUSE, var_uv+var_param.xy* 7.0)*55.0)/1022.0;\n"
"}\n"
}},

{"#version 140\r\n"
"\r\n"
"in vec2 var_uv;\r\n"
"out vec4 var_out;\r\n"
"uniform vec4 var_param;\r\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE0;\r\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE1;\r\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE2;\r\n"
"\r\n"
"const mat3 yuv2rgb =   mat3(1,0,1.596,1,-0.391,-0.813,1,2.018,0);\r\n"
"\r\n"
"void main()\r\n"
"{\r\n"
"\tfloat y=texture(SG3D_TEXTURE_DIFFUSE0, var_uv).x;\r\n"
"\tfloat u=texture(SG3D_TEXTURE_DIFFUSE1, var_uv).x;\r\n"
"\tfloat v=texture(SG3D_TEXTURE_DIFFUSE2, var_uv).x;\r\n"
"\r\n"
"\tvec3 rgb=vec3(1.1643*(y-0.0625), u-0.5, v-0.5)*yuv2rgb;\r\n"
"\tvar_out = vec4(rgb, 1.0)*var_param;\r\n"
"}\r\n",
1, {"#version 120\n"
"\n"
"varying vec2 var_uv;\n"
"#define var_out gl_FragColor\n"
"uniform vec4 var_param;\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE0;\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE1;\n"
"uniform sampler2D SG3D_TEXTURE_DIFFUSE2;\n"
"\n"
"const mat3 yuv2rgb =   mat3(1.0,0.0,1.596,1.0,-0.391,-0.813,1.0,2.018,0.0);\n"
"\n"
"void main()\n"
"{\n"
"\tfloat y=texture2D(SG3D_TEXTURE_DIFFUSE0, var_uv).x;\n"
"\tfloat u=texture2D(SG3D_TEXTURE_DIFFUSE1, var_uv).x;\n"
"\tfloat v=texture2D(SG3D_TEXTURE_DIFFUSE2, var_uv).x;\n"
"\n"
"\tvec3 rgb=vec3(1.1643*(y-0.0625), u-0.5, v-0.5)*yuv2rgb;\n"
"\tvar_out = vec4(rgb, 1.0)*var_param;\n"
"}\n"
}},

// for Eldritch
{"float\tFogOffsetU\t= 0.5f / FogTexSize;\r\n"
"float\tFogScaleU\t= ( FogTexSize - 1.0f ) / FogTexSize;\r\n",
3, {"float   FogOffsetU;\r\n"
"float   FogScaleU;\r\n",
"void main()\r\n"
"{\r\n",
"void main()\r\n"
"{\r\n"
"\tFogOffsetU = 0.5 / FogTexSize;\r\n"
"\tFogScaleU  = ( FogTexSize - 1.0 ) / FogTexSize;\r\n"
}},
// for KiCAD
{"        float derivative   = length( dFdx( tex ) ) * u_fontTextureWidth / 4;\n",
1, {"        float derivative   = length( dFdx( tex ) ) * float(u_fontTextureWidth) / 4.0;\n"}},
{"        delta = vec4( 0, 2 * pixelR, 0, 0 );\n",
1, {"        delta = vec4( 0, 2.0 * pixelR, 0, 0 );\n"}},
// for Iconoclasts
// Disable hotspot shaders 1
#ifdef GOA_CLONE
{
    "void main()\n"
    "{\n"
    "    vec4 p = texture2D(texture, texture_coordinate0);\n"
    "    float factor = (0.5 - p.r) * magnification;\n"
    "    vec2 t = texture_coordinate1;\n"
    "    t.x += (t.x - hotspotX) * factor;\n"
    "    t.y += (t.y - hotspotY) * factor;\n"
    "    gl_FragColor = sample_backtex(background_texture, t) * gl_Color;\n"
    "}",
    1,
    {
    "void main() { discard; }"
    }
},
// Disable hotspot shaders 2
{
    "void main()\n"
    "{\n"
    "    vec4 p = texture2D(texture, texture_coordinate0) * gl_Color;\n"
    "    if (p.a != 0.0) {\n"
    "        float zoomFactor = (0.0 - (p.r + p.g + p.b) / 3.0) * magnification;\n"
    "        vec2 t = texture_coordinate1;\n"
    "        t.x += (t.x - hotspotX + offx / texture_size.x) * zoomFactor;\n"
    "        t.y += (t.y - hotspotY + offy / texture_size.y) * zoomFactor;\n"
    "        gl_FragColor = sample_backtex(background_texture, t);\n"
    "        return;\n"
    "    }\n"
    "    gl_FragColor = p;\n"
    "}",
    1,
    {
    "void main() { discard; }"
    }
},
// Disable hotspot shaders 3
{
    "void main()\n"
    "{\n"
    "    vec4 p = texture2D(texture, texture_coordinate0) * gl_Color;\n"
    "    float factor = (0.0 - (p.r + p.g + p.b) / 3.0) * magnification;\n"
    "    vec2 t = background_offset.xy - texture_coordinate1 * \n"
    "                                    (background_offset.xy * 2.0 - vec2(1.0));\n"
    "    t.x += (t.x - hotspotX) * factor;\n"
    "    t.y += (t.y - hotspotY) * factor;\n"
    "    gl_FragColor = sample_backtex(background_texture, t);\n"
    "}",
    1,
    {"void main() { discard; }"}
}
#endif
};

// For Stellaris
static const char* gl4es_sign_1[] = {
"if (Data.Type == 1)",
"if (Data.BlendMode == 0)",
};
static const char* gl4es_hacks_1[] = {
"if (Data.Type == 1)",
"if (Data.Type == 1.0)",

"if (Data.Type == 2)",
"if (Data.Type == 2.0)",

"if (Data.Type == 3)",
"if (Data.Type == 3.0)",

"if (Data.BlendMode == 0)",
"if (Data.BlendMode == 0.0)",

"if (Data.BlendMode == 1)",
"if (Data.BlendMode == 1.0)",

"if (Data.BlendMode == 2)",
"if (Data.BlendMode == 2.0)",

"Out.vMaskingTexCoord = saturate(v.vTexCoord * 1000);",
"Out.vMaskingTexCoord = saturate(v.vTexCoord * 1000.0);",

"float vTime = 0.9 - saturate( (Time - AnimationTime) * 4 );",
"float vTime = 0.9 - saturate( (Time - AnimationTime) * 4.0 );",

"float vTime = 0.9 - saturate( (Time - AnimationTime) * 16 );",
"float vTime = 0.9 - saturate( (Time - AnimationTime) * 16.0 );",
};

// For Psychonauts
static const char* gl4es_sign_2[] = {
"vec4 ps_t3 = gl_TexCoord[3];",
"vec4 ps_t2 = gl_TexCoord[2];",
"vec4 ps_t1 = gl_TexCoord[1];",
"vec4 ps_t0 = gl_TexCoord[0];",
};

static const char* gl4es_sign_2_main = 
"void main()\n"
"{\n";


static const char* gl4es_hacks_2_1[] = {
"vec4 ps_t3;",
"vec4 ps_t2;",
"vec4 ps_t1;",
"vec4 ps_t0;",
};

static const char* gl4es_hacks_2_2[] = {
"\tps_t3 = gl_TexCoord[3];",
"\tps_t2 = gl_TexCoord[2];",
"\tps_t1 = gl_TexCoord[1];",
"\tps_t0 = gl_TexCoord[0];",
};

static char* ShaderHacks_1(char* shader, char* Tmp, int* tmpsize)
{
    // check for all signature first
    for (int i=0; i<sizeof(gl4es_sign_1)/sizeof(gl4es_sign_1[0]); i++)
        if(!strstr(Tmp, gl4es_sign_1[i]))
            return Tmp;
    // Do the replace
    for (int i=0; i<sizeof(gl4es_hacks_1)/sizeof(gl4es_hacks_1[0]); i+=2)
        if(strstr(Tmp, gl4es_hacks_1[i])) {
            if(Tmp==shader) {Tmp = (char*)malloc(*tmpsize); strcpy(Tmp, shader);}   // hacking!
            Tmp = gl4es_inplace_replace_simple(Tmp, tmpsize, gl4es_hacks_1[i], gl4es_hacks_1[i+1]);
        }
    return Tmp;
}

static char* ShaderHacks_2_1(char* shader, char* Tmp, int* tmpsize, int i)
{
    char* p = strstr(Tmp, gl4es_sign_2[i]);
    if(!p) return Tmp;  // not found
    char* m = strstr(Tmp, gl4es_sign_2_main);
    if(!m) return Tmp;  // main signature not found
    if((uintptr_t)p > (uintptr_t)m) return Tmp; // main is before, aborting...
    // ok, instance found, insert main line...
    if(Tmp==shader) {Tmp = (char*)malloc(*tmpsize); strcpy(Tmp, shader); m = strstr(Tmp, gl4es_sign_2_main);}   // hacking!
    m += strlen(gl4es_sign_2_main);
    Tmp = gl4es_inplace_insert(m, gl4es_hacks_2_2[i], Tmp, tmpsize);
    Tmp = gl4es_inplace_replace_simple(Tmp, tmpsize, gl4es_sign_2[i], gl4es_hacks_2_1[i]);
    return Tmp;
}

static char* ShaderHacks_2(char* shader, char* Tmp, int* tmpsize)
{
    // check for each signature
    for (int i=0; i<sizeof(gl4es_sign_2)/sizeof(gl4es_sign_2[0]); i++)
        Tmp = ShaderHacks_2_1(shader, Tmp, tmpsize, i);
    return Tmp;
}

char* ShaderHacks(char* shader)
{
    char* Tmp = shader;
    int tmpsize = strlen(Tmp)+10;
    // specific hacks
    Tmp = ShaderHacks_1(shader, Tmp, &tmpsize);
    Tmp = ShaderHacks_2(shader, Tmp, &tmpsize);
    // generic
    for (int i=0; i<sizeof(gl4es_hacks)/sizeof(gl4es_hacks[0]); ++i) {
        char* f = gl4es_hacks[i].sign;
        int n = gl4es_hacks[i].n;
        if(strstr(Tmp, f)) {
            if(Tmp==shader) {Tmp = (char*)malloc(tmpsize); strcpy(Tmp, shader);}   // hacking!
            for (int j=0; j<n; j+=2) {
                if(j) f = gl4es_hacks[i].next[j-1];
                Tmp = gl4es_inplace_replace_simple(Tmp, &tmpsize, f, gl4es_hacks[i].next[j]);
            }
        }
    }
    return Tmp;
}
