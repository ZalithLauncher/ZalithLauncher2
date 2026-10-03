#include "shaderconv.h"

#include <stdio.h>
#include "../glx/hardext.h"
#include "debug.h"
#include "fpe_shader.h"
#include "init.h"
#include "preproc.h"
#include "string_utils.h"
#include "shader_hacks.h"
#include "logs.h"

typedef struct {
    const char* glname;
    const char* name;
    const char* type;
    const char* prec;
    int attrib;
} builtin_attrib_t;

const builtin_attrib_t builtin_attrib[] = {
    {"gl_Vertex", "_gl4es_Vertex", "vec4", "highp", ARB_VERTEX},
    {"gl_Color", "_gl4es_Color", "vec4", "lowp", ARB_COLOR},
    {"gl_MultiTexCoord0", "_gl4es_MultiTexCoord0", "vec4", "highp", ARB_MULTITEXCOORD0},
    {"gl_MultiTexCoord1", "_gl4es_MultiTexCoord1", "vec4", "highp", ARB_MULTITEXCOORD1},
    {"gl_MultiTexCoord2", "_gl4es_MultiTexCoord2", "vec4", "highp", ARB_MULTITEXCOORD2},
    {"gl_MultiTexCoord3", "_gl4es_MultiTexCoord3", "vec4", "highp", ARB_MULTITEXCOORD3},
    {"gl_MultiTexCoord4", "_gl4es_MultiTexCoord4", "vec4", "highp", ARB_MULTITEXCOORD4},
    {"gl_MultiTexCoord5", "_gl4es_MultiTexCoord5", "vec4", "highp", ARB_MULTITEXCOORD5},
    {"gl_MultiTexCoord6", "_gl4es_MultiTexCoord6", "vec4", "highp", ARB_MULTITEXCOORD6},
    {"gl_MultiTexCoord7", "_gl4es_MultiTexCoord7", "vec4", "highp", ARB_MULTITEXCOORD7},
    {"gl_MultiTexCoord8", "_gl4es_MultiTexCoord8", "vec4", "highp", ARB_MULTITEXCOORD8},
    {"gl_MultiTexCoord9", "_gl4es_MultiTexCoord9", "vec4", "highp", ARB_MULTITEXCOORD9},
    {"gl_MultiTexCoord10", "_gl4es_MultiTexCoord10", "vec4", "highp", ARB_MULTITEXCOORD10},
    {"gl_MultiTexCoord11", "_gl4es_MultiTexCoord11", "vec4", "highp", ARB_MULTITEXCOORD11},
    {"gl_MultiTexCoord12", "_gl4es_MultiTexCoord12", "vec4", "highp", ARB_MULTITEXCOORD12},
    {"gl_MultiTexCoord13", "_gl4es_MultiTexCoord13", "vec4", "highp", ARB_MULTITEXCOORD13},
    {"gl_MultiTexCoord14", "_gl4es_MultiTexCoord14", "vec4", "highp", ARB_MULTITEXCOORD14},
    {"gl_MultiTexCoord15", "_gl4es_MultiTexCoord15", "vec4", "highp", ARB_MULTITEXCOORD15},
    {"gl_SecondaryColor", "_gl4es_SecondaryColor", "vec4", "lowp", ARB_SECONDARY},
    {"gl_Normal", "_gl4es_Normal", "vec3", "highp", ARB_NORMAL},
    {"gl_FogCoord", "_gl4es_FogCoord", "float", "highp", ARB_FOGCOORD}
};

const builtin_attrib_t builtin_attrib_compressed[] = {
    {"gl_Vertex", "_gl4es_Vertex", "vec4", "highp", COMP_VERTEX},
    {"gl_Color", "_gl4es_Color", "vec4", "lowp", COMP_COLOR},
    {"gl_MultiTexCoord0", "_gl4es_MultiTexCoord0", "vec4", "highp", COMP_MULTITEXCOORD0},
    {"gl_MultiTexCoord1", "_gl4es_MultiTexCoord1", "vec4", "highp", COMP_MULTITEXCOORD1},
    {"gl_MultiTexCoord2", "_gl4es_MultiTexCoord2", "vec4", "highp", COMP_MULTITEXCOORD2},
    {"gl_MultiTexCoord3", "_gl4es_MultiTexCoord3", "vec4", "highp", COMP_MULTITEXCOORD3},
    {"gl_MultiTexCoord4", "_gl4es_MultiTexCoord4", "vec4", "highp", COMP_MULTITEXCOORD4},
    {"gl_MultiTexCoord5", "_gl4es_MultiTexCoord5", "vec4", "highp", COMP_MULTITEXCOORD5},
    {"gl_MultiTexCoord6", "_gl4es_MultiTexCoord6", "vec4", "highp", COMP_MULTITEXCOORD6},
    {"gl_MultiTexCoord7", "_gl4es_MultiTexCoord7", "vec4", "highp", COMP_MULTITEXCOORD7},
    {"gl_MultiTexCoord8", "_gl4es_MultiTexCoord8", "vec4", "highp", COMP_MULTITEXCOORD8},
    {"gl_MultiTexCoord9", "_gl4es_MultiTexCoord9", "vec4", "highp", COMP_MULTITEXCOORD9},
    {"gl_MultiTexCoord10", "_gl4es_MultiTexCoord10", "vec4", "highp", COMP_MULTITEXCOORD10},
    {"gl_MultiTexCoord11", "_gl4es_MultiTexCoord11", "vec4", "highp", COMP_MULTITEXCOORD11},
    {"gl_MultiTexCoord12", "_gl4es_MultiTexCoord12", "vec4", "highp", COMP_MULTITEXCOORD12},
    {"gl_MultiTexCoord13", "_gl4es_MultiTexCoord13", "vec4", "highp", COMP_MULTITEXCOORD13},
    {"gl_MultiTexCoord14", "_gl4es_MultiTexCoord14", "vec4", "highp", COMP_MULTITEXCOORD14},
    {"gl_MultiTexCoord15", "_gl4es_MultiTexCoord15", "vec4", "highp", COMP_MULTITEXCOORD15},
    {"gl_SecondaryColor", "_gl4es_SecondaryColor", "vec4", "lowp", COMP_SECONDARY},
    {"gl_Normal", "_gl4es_Normal", "vec3", "highp", COMP_NORMAL},
    {"gl_FogCoord", "_gl4es_FogCoord", "float", "highp", COMP_FOGCOORD}
};

typedef struct {
    const char* glname;
    const char* name;
    const char* type;
    int   texarray;
    reserved_matrix_t matrix;
} builtin_matrix_t;

const builtin_matrix_t builtin_matrix[] = {
    {"gl_ModelViewMatrixInverseTranspose", "_gl4es_ITModelViewMatrix", "mat4", 0, MAT_MV_IT},
    {"gl_ModelViewMatrixInverse", "_gl4es_IModelViewMatrix", "mat4", 0, MAT_MV_I},
    {"gl_ModelViewMatrixTranspose", "_gl4es_TModelViewMatrix", "mat4", 0, MAT_MV_T},
    {"gl_ModelViewMatrix", "_gl4es_ModelViewMatrix", "mat4", 0, MAT_MV},
    {"gl_ProjectionMatrixInverseTranspose", "_gl4es_ITProjectionMatrix", "mat4", 0, MAT_P_IT},
    {"gl_ProjectionMatrixInverse", "_gl4es_IProjectionMatrix", "mat4", 0, MAT_P_I},
    {"gl_ProjectionMatrixTranspose", "_gl4es_TProjectionMatrix", "mat4", 0, MAT_P_T},
    {"gl_ProjectionMatrix", "_gl4es_ProjectionMatrix", "mat4", 0, MAT_P},
    {"gl_ModelViewProjectionMatrixInverseTranspose", "_gl4es_ITModelViewProjectionMatrix", "mat4", 0, MAT_MVP_IT},
    {"gl_ModelViewProjectionMatrixInverse", "_gl4es_IModelViewProjectionMatrix", "mat4", 0, MAT_MVP_I},
    {"gl_ModelViewProjectionMatrixTranspose", "_gl4es_TModelViewProjectionMatrix", "mat4", 0, MAT_MVP_T},
    {"gl_ModelViewProjectionMatrix", "_gl4es_ModelViewProjectionMatrix", "mat4", 0, MAT_MVP},
    // non standard version to avoid useless array of Matrix Uniform (in case the compiler as issue optimising this)
    {"gl_TextureMatrix_0", "_gl4es_TextureMatrix_0", "mat4", 0, MAT_T0},
    {"gl_TextureMatrix_1", "_gl4es_TextureMatrix_1", "mat4", 0, MAT_T1},
    {"gl_TextureMatrix_2", "_gl4es_TextureMatrix_2", "mat4", 0, MAT_T2},
    {"gl_TextureMatrix_3", "_gl4es_TextureMatrix_3", "mat4", 0, MAT_T3},
    {"gl_TextureMatrix_4", "_gl4es_TextureMatrix_4", "mat4", 0, MAT_T4},
    {"gl_TextureMatrix_5", "_gl4es_TextureMatrix_5", "mat4", 0, MAT_T5},
    {"gl_TextureMatrix_6", "_gl4es_TextureMatrix_6", "mat4", 0, MAT_T6},
    {"gl_TextureMatrix_7", "_gl4es_TextureMatrix_7", "mat4", 0, MAT_T7},
    {"gl_TextureMatrix_8", "_gl4es_TextureMatrix_8", "mat4", 0, MAT_T8},
    {"gl_TextureMatrix_9", "_gl4es_TextureMatrix_9", "mat4", 0, MAT_T9},
    {"gl_TextureMatrix_10", "_gl4es_TextureMatrix_10", "mat4", 0, MAT_T10},
    {"gl_TextureMatrix_11", "_gl4es_TextureMatrix_11", "mat4", 0, MAT_T11},
    {"gl_TextureMatrix_12", "_gl4es_TextureMatrix_12", "mat4", 0, MAT_T12},
    {"gl_TextureMatrix_13", "_gl4es_TextureMatrix_13", "mat4", 0, MAT_T13},
    {"gl_TextureMatrix_14", "_gl4es_TextureMatrix_14", "mat4", 0, MAT_T14},
    {"gl_TextureMatrix_15", "_gl4es_TextureMatrix_15", "mat4", 0, MAT_T15},
    // regular texture matrix
    {"gl_TextureMatrixInverseTranspose", "_gl4es_ITTextureMatrix", "mat4", 1, MAT_T0_IT},
    {"gl_TextureMatrixInverse", "_gl4es_ITextureMatrix", "mat4", 1, MAT_T0_I},
    {"gl_TextureMatrixTranspose", "_gl4es_TTextureMatrix", "mat4", 1, MAT_T0_T},
    {"gl_TextureMatrix", "_gl4es_TextureMatrix", "mat4", 1, MAT_T0},
    {"gl_NormalMatrix", "_gl4es_NormalMatrix", "mat3", 0, MAT_N}
  };

#define STR_HELPER(x) #x
#define STR(x) STR_HELPER(x)
static const char* gl4es_MaxLightsSource =
"#define _gl4es_MaxLights " STR(MAX_LIGHT) "\n";
static const char* gl4es_MaxClipPlanesSource =
"#define _gl4es_MaxClipPlanes " STR(MAX_CLIP_PLANES) "\n";
static const char* gl4es_MaxTextureUnitsSource =
"#define _gl4es_MaxTextureUnits " STR(MAX_TEX) "\n";
static const char* gl4es_MaxTextureCoordsSource =
"#define _gl4es_MaxTextureCoords " STR(MAX_TEX) "\n";
#undef STR
#undef STR_HELPER

static const char* gl4es_LightSourceParametersSource = 
"struct gl_LightSourceParameters\n"
"{\n"
"   vec4 ambient;\n"
"   vec4 diffuse;\n"
"   vec4 specular;\n"
"   vec4 position;\n"
"   vec4 halfVector;\n"   //halfVector = normalize(normalize(position) + vec3(0,0,1) if vbs==FALSE)
"   vec3 spotDirection;\n"
"   float spotExponent;\n"
"   float spotCutoff;\n"
"   float spotCosCutoff;\n"
"   float constantAttenuation;\n"
"   float linearAttenuation;\n"
"   float quadraticAttenuation;\n"
"};\n"
"uniform gl_LightSourceParameters gl_LightSource[gl_MaxLights];\n";

static const char* gl4es_LightModelParametersSource =
"struct gl_LightModelParameters {\n"
"  vec4 ambient;\n"
"};\n"
"uniform gl_LightModelParameters gl_LightModel;\n";

static const char* gl4es_MaterialParametersSource =
"struct gl_MaterialParameters\n"
"{\n"
"   vec4 emission;\n"
"   vec4 ambient;\n"
"   vec4 diffuse;\n"
"   vec4 specular;\n"
"   float shininess;\n"
"};\n"
"uniform gl_MaterialParameters gl_FrontMaterial;\n"
"uniform gl_MaterialParameters gl_BackMaterial;\n";

static const char* gl4es_LightModelProductsSource =
"struct gl_LightModelProducts\n"
"{\n"
"   vec4 sceneColor;\n"
"};\n"
"uniform gl_LightModelProducts gl_FrontLightModelProduct;\n"
"uniform gl_LightModelProducts gl_BackLightModelProduct;\n";

static const char* gl4es_LightProductsSource =
"struct gl_LightProducts\n"
"{\n"
"   vec4 ambient;\n"
"   vec4 diffuse;\n"
"   vec4 specular;\n"
"};\n"
"uniform gl_LightProducts gl_FrontLightProduct[gl_MaxLights];\n"
"uniform gl_LightProducts gl_BackLightProduct[gl_MaxLights];\n";

static const char* gl4es_PointSpriteSource =
"struct gl_PointParameters\n"
"{\n"
"   float size;\n"
"   float sizeMin;\n"
"   float sizeMax;\n"
"   float fadeThresholdSize;\n"
"   float distanceConstantAttenuation;\n"
"   float distanceLinearAttenuation;\n"
"   float distanceQuadraticAttenuation;\n"
"};\n"
"uniform gl_PointParameters gl_Point;\n";

static const char* gl4es_FogParametersSource =
"struct gl_FogParameters {\n"
"    lowp vec4 color;\n"
"    mediump float density;\n"
"    mediump float start;\n"
"    mediump float end;\n"
"    mediump float scale;\n"   // Derived:   1.0 / (end - start) 
"};\n"
"uniform gl_FogParameters gl_Fog;\n";
static const char* gl4es_FogParametersSourceHighp =
"struct gl_FogParameters {\n"
"    lowp vec4 color;\n"
"    mediump float density;\n"
"    highp   float start;\n"
"    highp   float end;\n"
"    highp   float scale;\n"   // Derived:   1.0 / (end - start) 
"};\n"
"uniform gl_FogParameters gl_Fog;\n";

static const char* gl4es_texenvcolorSource =
"uniform vec4 gl_TextureEnvColor[gl_MaxTextureUnits];\n";

static const char* gl4es_texgeneyeSource[4] = {
"uniform vec4 gl_EyePlaneS[gl_MaxTextureCoords];\n",
"uniform vec4 gl_EyePlaneT[gl_MaxTextureCoords];\n",
"uniform vec4 gl_EyePlaneR[gl_MaxTextureCoords];\n",
"uniform vec4 gl_EyePlaneQ[gl_MaxTextureCoords];\n" };

static const char* gl4es_texgenobjSource[4] = {
"uniform vec4 gl_ObjectPlaneS[gl_MaxTextureCoords];\n",
"uniform vec4 gl_ObjectPlaneT[gl_MaxTextureCoords];\n",
"uniform vec4 gl_ObjectPlaneR[gl_MaxTextureCoords];\n",
"uniform vec4 gl_ObjectPlaneQ[gl_MaxTextureCoords];\n" };

static const char* gl4es_clipplanesSource = 
"uniform vec4  gl_ClipPlane[gl_MaxClipPlanes];\n";

static const char* gl4es_normalscaleSource =
"uniform float gl_NormalScale;\n";

static const char* gl4es_instanceID =
"#define GL_ARB_draw_instanced 1\n"
"uniform int _gl4es_InstanceID;\n";

static const char* gl4es_frontColorSource =
"varying lowp vec4 _gl4es_FrontColor;\n";

static const char* gl4es_backColorSource =
"varying lowp vec4 _gl4es_BackColor;\n";

static const char* gl4es_frontSecondaryColorSource =
"varying lowp vec4 _gl4es_FrontSecondaryColor;\n";

static const char* gl4es_backSecondaryColorSource =
"varying lowp vec4 _gl4es_BackSecondaryColor;\n";

static const char* gl4es_texcoordSource =
"varying highp vec4 _gl4es_TexCoord[%d];\n";

static const char* gl4es_texcoordSourceAlt =
"varying highp vec4 _gl4es_TexCoord_%d;\n";

static const char* gl4es_fogcoordSource =
"varying mediump float _gl4es_FogFragCoord;\n";

static const char* gl4es_ftransformSource = 
"\n"
"highp vec4 ftransform() {\n"
" return gl_ModelViewProjectionMatrix * gl_Vertex;\n"
"}\n";

static const char* gl4es_ClipVertex = 
"vec4 gl4es_ClipVertex;\n";

static const char* gl4es_ClipVertexSource = 
"gl4es_ClipVertex";

static const char* gl4es_ClipVertex_clip =
"\nif(any(lessThanEqual(gl4es_ClipVertex.xyz, vec3(-gl4es_ClipVertex.w)))"
" || any(greaterThanEqual(gl4es_ClipVertex.xyz, vec3(gl4es_ClipVertex.w)))) discard;\n";

static const char* gl_TexCoordSource = "gl_TexCoord[";

static const char* gl_TexMatrixSources[] = {
"gl_TextureMatrixInverseTranspose[",
"gl_TextureMatrixInverse[",
"gl_TextureMatrixTranspose[",
"gl_TextureMatrix["
};

static const char* GLESHeader[] = {
  "#version 100\n%sprecision %s float;\nprecision %s int;\n",
  "#version 120\n%sprecision %s float;\nprecision %s int;\n",
  "#version 310 es\n#define attribute in\n#define varying out\n%sprecision %s float;\nprecision %s int;\n",
  "#version 300 es\n#define attribute in\n#define varying out\n%sprecision %s float;\nprecision %s int;\n"
};

static const char* gl4es_transpose =
"mat2 gl4es_transpose(mat2 m) {\n"
" return mat2(m[0][0], m[1][0],\n"
"             m[0][1], m[1][1]);\n"
"}\n"
"mat3 gl4es_transpose(mat3 m) {\n"
" return mat3(m[0][0], m[1][0], m[2][0],\n"
"             m[0][1], m[1][1], m[2][1],\n"
"             m[0][2], m[1][2], m[2][2]);\n"
"}\n"
"mat4 gl4es_transpose(mat4 m) {\n"
" return mat4(m[0][0], m[1][0], m[2][0], m[3][0],\n"
"             m[0][1], m[1][1], m[2][1], m[3][1],\n"
"             m[0][2], m[1][2], m[2][2], m[3][2],\n"
"             m[0][3], m[1][3], m[2][3], m[3][3]);\n"
"}\n";

static const char* HackAltPow = 
"float pow(float f, int a) {\n"
" return pow(f, float(a));\n"
"}\n";
static const char* HackAltMax = 
"float max(float a, int b) {\n"
" return max(a, float(b));\n"
"}\n"
"float max(int a, float b) {\n"
" return max(float(a), b);\n"
"}\n";
static const char* HackAltMin = 
"float min(float a, int b) {\n"
" return min(a, float(b));\n"
"}\n"
"float min(int a, float b) {\n"
" return min(float(a), b);\n"
"}\n";
static const char* HackAltClamp = 
"float clamp(float f, int a, int b) {\n"
" return clamp(f, float(a), float(b));\n"
"}\n"
"float clamp(float f, float a, int b) {\n"
" return clamp(f, a, float(b));\n"
"}\n"
"float clamp(float f, int a, float b) {\n"
" return clamp(f, float(a), b);\n"
"}\n"
"vec2 clamp(vec2 f, int a, int b) {\n"
" return clamp(f, float(a), float(b));\n"
"}\n"
"vec2 clamp(vec2 f, float a, int b) {\n"
" return clamp(f, a, float(b));\n"
"}\n"
"vec2 clamp(vec2 f, int a, float b) {\n"
" return clamp(f, float(a), b);\n"
"}\n"
"vec3 clamp(vec3 f, int a, int b) {\n"
" return clamp(f, float(a), float(b));\n"
"}\n"
"vec3 clamp(vec3 f, float a, int b) {\n"
" return clamp(f, a, float(b));\n"
"}\n"
"vec3 clamp(vec3 f, int a, float b) {\n"
" return clamp(f, float(a), b);\n"
"}\n"
"vec4 clamp(vec4 f, int a, int b) {\n"
" return clamp(f, float(a), float(b));\n"
"}\n"
"vec4 clamp(vec4 f, float a, int b) {\n"
" return clamp(f, a, float(b));\n"
"}\n"
"vec4 clamp(vec4 f, int a, float b) {\n"
" return clamp(f, float(a), b);\n"
"}\n";


static const char* HackAltMod = 
"float mod(float f, int a) {\n"
" return mod(f, float(a));\n"
"}\n"
"vec2 mod(vec2 f, int a) {\n"
" return mod(f, float(a));\n"
"}\n"
"vec3 mod(vec3 f, int a) {\n"
" return mod(f, float(a));\n"
"}\n"
"vec4 mod(vec4 f, int a) {\n"
" return mod(f, float(a));\n"
"}\n";

static const char* texture2DLodAlt =
"vec4 _gl4es_texture2DLod(sampler2D sampler, vec2 coord, float lod) {\n"
" return texture2D(sampler, coord);\n"
"}\n";

static const char* texture2DProjLodAlt =
"vec4 _gl4es_texture2DProjLod(sampler2D sampler, vec3 coord, float lod) {\n"
" return texture2DProj(sampler, coord);\n"
"}\n"
"vec4 _gl4es_texture2DProjLod(sampler2D sampler, vec4 coord, float lod) {\n"
" return texture2DProj(sampler, coord);\n"
"}\n";
static const char* textureCubeLodAlt =
"vec4 _gl4es_textureCubeLod(samplerCube sampler, vec3 coord, float lod) {\n"
" return textureCube(sampler, coord);\n"
"}\n";

static const char* texture2DGradAlt =
"vec4 _gl4es_texture2DGrad(sampler2D sampler, vec2 coord, vec2 dPdx, vec2 dPdy) {\n"
" return texture2D(sampler, coord);\n"
"}\n";

static const char* texture2DProjGradAlt =
"vec4 _gl4es_texture2DProjGrad(sampler2D sampler, vec3 coord, vec2 dPdx, vec2 dPdy) {\n"
" return texture2DProj(sampler, coord);\n"
"}\n"
"vec4 _gl4es_texture2DProjGrad(sampler2D sampler, vec4 coord, vec2 dPdx, vec2 dPdy) {\n"
" return texture2DProj(sampler, coord);\n"
"}\n";
static const char* textureCubeGradAlt =
"vec4 _gl4es_textureCubeGrad(samplerCube sampler, vec3 coord, vec2 dPdx, vec2 dPdy) {\n"
" return textureCube(sampler, coord);\n"
"}\n";


static const char* useEXTDrawBuffers =
"#extension GL_EXT_draw_buffers : enable\n";

static const char* gl_ProgramEnv  = "gl_ProgramEnv";
static const char* gl_ProgramLocal= "gl_ProgramLocal";

static const char* gl_Samplers1D = "gl_Sampler1D_";
static const char* gl_Samplers2D = "gl_Sampler2D_";
static const char* gl_Samplers3D = "gl_Sampler3D_";
static const char* gl_SamplersCube = "gl_SamplerCube_";
static const char* gl4es_Samplers1D = "_gl4es_Sampler1D_";
static const char* gl4es_Samplers2D = "_gl4es_Sampler2D_";
static const char* gl4es_Samplers3D = "_gl4es_Sampler3D_";
static const char* gl4es_SamplersCube = "_gl4es_SamplerCube_";
static const char* gl4es_Samplers1D_uniform = "uniform sampler2D _gl4es_Sampler1D_";
static const char* gl4es_Samplers2D_uniform = "uniform sampler2D _gl4es_Sampler2D_";
static const char* gl4es_Samplers3D_uniform = "uniform sampler2D _gl4es_Sampler3D_";
static const char* gl4es_SamplersCube_uniform = "uniform samplerCube _gl4es_SamplerCube_";

static const char* gl_VertexAttrib = "gl_VertexAttrib_";
static const char* gl4es_VertexAttrib = "_gl4es_VertexAttrib_";

char gl_VA[MAX_VATTRIB][32] = {0};
char gl4es_VA[MAX_VATTRIB][32] = {0};

char* ConvertShader(const char* pEntry, int isVertex, shaderconv_need_t *need)
{
  #define ShadAppend(S) Tmp = gl4es_append(Tmp, &tmpsize, S)

  if(gl_VA[0][0]=='\0') {
    for (int i=0; i<MAX_VATTRIB; ++i) {
      sprintf(gl_VA[i], "%s%d", gl_VertexAttrib, i);
      sprintf(gl4es_VA[i], "%s%d", gl4es_VertexAttrib, i);
    }
  }
  int fpeShader = (strstr(pEntry, fpeshader_signature)!=NULL)?1:0;
  int maskbefore = 4|(isVertex?1:2);
  int maskafter = 8|(isVertex?1:2);
  if((globals4es.dbgshaderconv&maskbefore)==maskbefore) {
    printf("Shader source%s:\n%s\n", fpeShader?" (FPEShader generated)":"", pEntry);
  }
  int comments = globals4es.comments;
  
  char* pBuffer = (char*)pEntry;

  int version120 = 0;
  char* versionString = NULL;
  if(!fpeShader) {
    extensions_t exts;  // dummy...
    exts.cap = exts.size = 0;
    exts.ext = NULL;
    // hacks
    char* pHacked = ShaderHacks(pBuffer);
    // preproc first
    pBuffer = preproc(pHacked, comments, globals4es.shadernogles, &exts, &versionString);
    if(pHacked!=pEntry && pHacked!=pBuffer)
      free(pHacked);
    // now comment all line starting with precision...
    if(strstr(pBuffer, "\nprecision")) {
      int sz = strlen(pBuffer);
      pBuffer = gl4es_inplace_replace(pBuffer, &sz, "\nprecision", "\n//precision");
    }
    // should do something with the extension list...
    if(exts.ext)
      free(exts.ext);
  }

  static shaderconv_need_t dummy_need = {0};
  if(!need) {
    need = &dummy_need;
    need->need_texcoord = -1;
    need->need_clean = 1; // no hack, this is a dummy need structure
  }
  int notexarray = globals4es.notexarray || need->need_notexarray || fpeShader;

  //const char* GLESUseFragHighp = "#extension GL_OES_fragment_precision_high : enable\n"; // is this needed?  
  char GLESFullHeader[512];
  int wanthighp = !fpeShader;
  if(wanthighp && !hardext.highp) wanthighp = 0;
  int versionHeader = 0;
  #if 0
  // support for higher glsl require much more work
  // around some keyword
  // like in/out that depends on the shader being vertex or fragment
  // and a few other little things...
  if(versionString && strcmp(versionString, "120")==0)
     version120 = 1;
  if(version120) {
    if(hardext.glsl120) versionHeader = 1;
    else if(hardext.glsl310es) versionHeader = 2;
    else if(hardext.glsl300es) { versionHeader = 3; /* location on uniform not supported ! */ }
    /* else no location or in / out are supported */
  }
  #endif
  //sprintf(GLESFullHeader, GLESHeader, (wanthighp && hardext.highp==1 && !isVertex)?GLESUseFragHighp:"", (wanthighp)?"highp":"mediump", (wanthighp)?"highp":"mediump");
  sprintf(GLESFullHeader, GLESHeader[versionHeader], "", (wanthighp)?"highp":"mediump", (wanthighp)?"highp":"mediump");

  int tmpsize = strlen(pBuffer)*2+strlen(GLESFullHeader)+100;
  char* Tmp = (char*)calloc(1, tmpsize);
  strcpy(Tmp, pBuffer);

  // and now change the version header, and add default precision
  char* newptr;
  newptr=strstr(Tmp, "#version");
  if (!newptr) {
    Tmp = gl4es_inplace_insert(Tmp, GLESFullHeader, Tmp, &tmpsize);
  } else {
    while(*newptr!=0x0a) newptr++;
    newptr++;
    memmove(Tmp, newptr, strlen(newptr)+1);
    Tmp = gl4es_inplace_insert(Tmp, GLESFullHeader, Tmp, &tmpsize);
  }
  int headline = 3;
  // move all "#extension in header zone"
  while (strstr(Tmp, "#extension") && strstr(Tmp, "#extension")>gl4es_getline(Tmp, headline-2)) {
    char* ext = strstr(Tmp, "#extension");
    size_t l = (uintptr_t)strstr(ext, "\n")-(uintptr_t)ext + sizeof("\n");
#ifndef _MSC_VER
    char e[l];
#else
    char* e = _alloca(l);
#endif
    memset(e, 0, l);
    strncpy(e, ext, l-1);
    Tmp = gl4es_inplace_replace_simple(Tmp, &tmpsize, e, "");
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline-2), e, Tmp, &tmpsize);
    ++headline;
  }
  // check if gl_FragDepth is used
  int fragdepth = (strstr(pBuffer, "gl_FragDepth"))?1:0;
  const char* GLESUseFragDepth = "#extension GL_EXT_frag_depth : enable\n";
  const char* GLESFakeFragDepth = "mediump float fakeFragDepth = 0.0;\n";
  if (fragdepth) {
    /* If #extension is used, it should be placed before the second line of the header. */
    if(hardext.fragdepth)
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 1), GLESUseFragDepth, Tmp, &tmpsize);
    else
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline-1), GLESFakeFragDepth, Tmp, &tmpsize);
    headline++;
  }

  const char* GLESUseShaderNonConstantGlobalInitialzers = "#extension GL_EXT_shader_non_constant_global_initializers : enable\n";
  Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 1), GLESUseShaderNonConstantGlobalInitialzers, Tmp, &tmpsize);
  ++headline;

  Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline-1), "#define GL4ES\n", Tmp, &tmpsize);

  int derivatives = (strstr(pBuffer, "dFdx(") || strstr(pBuffer, "dFdy(") || strstr(pBuffer, "fwidth("))?1:0;
  const char* GLESUseDerivative = "#extension GL_OES_standard_derivatives : enable\n";
  // complete fake value... A better thing should be use....
  const char* GLESFakeDerivative = "float dFdx(float p) {return 0.0001;}\nvec2 dFdx(vec2 p) {return vec2(0.0001);}\nvec3 dFdx(vec3 p) {return vec3(0.0001);}\n"
  "float dFdy(float p) {return 0.0001;}\nvec2 dFdy(vec2 p) {return vec2(0.0001);}\nvec3 dFdy(vec3 p) {return vec3(0.0001);}\n"
  "float fwidth(float p) {return abs(dFdx(p))+abs(dFdy(p));}\nvec2 fwidth(vec2 p) {return abs(dFdx(p))+abs(dFdy(p));}\n"
  "vec3 fwidth(vec3 p) {return abs(dFdx(p))+abs(dFdy(p));}\n";
  if (derivatives) {
    /* If #extension is used, it should be placed before the second line of the header. */
    if(hardext.derivatives)
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 1), GLESUseDerivative, Tmp, &tmpsize);
    else
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline-1), GLESFakeDerivative, Tmp, &tmpsize);
    headline++;
  }
  // check if draw_buffers may be used (no fallback here :( )
  if(hardext.maxdrawbuffers>1 && strstr(pBuffer, "gl_FragData[")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 1), useEXTDrawBuffers, Tmp, &tmpsize);
  }
  // if some functions are used, add some int/float alternative
  if(!fpeShader && !globals4es.nointovlhack) {
    if(strstr(Tmp, "pow(") || strstr(Tmp, "pow (")) {
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), HackAltPow, Tmp, &tmpsize);
    }
    if(strstr(Tmp, "max(") || strstr(Tmp, "max (")) {
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), HackAltMax, Tmp, &tmpsize);
    }
    if(strstr(Tmp, "min(") || strstr(Tmp, "min (")) {
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), HackAltMin, Tmp, &tmpsize);
    }
    if(strstr(Tmp, "clamp(") || strstr(Tmp, "clamp (")) {
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), HackAltClamp, Tmp, &tmpsize);
    }
    if(strstr(Tmp, "mod(") || strstr(Tmp, "mod (")) {
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), HackAltMod, Tmp, &tmpsize);
    }
  }
  if(!isVertex && hardext.shaderlod && 
    (gl4es_find_string(Tmp, "texture2DLod") || gl4es_find_string(Tmp, "texture2DProjLod") 
  || gl4es_find_string(Tmp, "textureCubeLod") 
  || gl4es_find_string(Tmp, "texture2DGradARB") || gl4es_find_string(Tmp, "texture2DProjGradARB")|| gl4es_find_string(Tmp, "textureCubeGradARB") 
  )) {
      const char* GLESUseShaderLod = "#extension GL_EXT_shader_texture_lod : enable\n";
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 1), GLESUseShaderLod, Tmp, &tmpsize);
  }
  if(!isVertex && (gl4es_find_string(Tmp, "texture2DLod"))) {
      if(hardext.shaderlod) {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DLod", "texture2DLodEXT");
      } else {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DLod", "_gl4es_texture2DLod");
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), texture2DLodAlt, Tmp, &tmpsize);
      }
  }
  if(!isVertex && (gl4es_find_string(Tmp, "texture2DProjLod"))) {
      if(hardext.shaderlod) {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DProjLod", "texture2DProjLodEXT");
      } else {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DProjLod", "_gl4es_texture2DProjLod");
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), texture2DProjLodAlt, Tmp, &tmpsize);
      }
  }
  if(!isVertex && (gl4es_find_string(Tmp, "textureCubeLod"))) {
      if(hardext.shaderlod) {
        if(!hardext.cubelod)
          Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "textureCubeLod", "textureCubeLodEXT");
      } else {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "textureCubeLod", "_gl4es_textureCubeLod");
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), textureCubeLodAlt, Tmp, &tmpsize);
      }
  }
  if(!isVertex && (gl4es_find_string(Tmp, "texture2DGradARB"))) {
      if(hardext.shaderlod) {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DGradARB", "texture2DGradEXT");
      } else {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DGradARB", "_gl4es_texture2DGrad");
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), texture2DGradAlt, Tmp, &tmpsize);
      }
  }
  if(!isVertex && (gl4es_find_string(Tmp, "texture2DProjGradARB"))) {
      if(hardext.shaderlod) {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DProjGradARB", "texture2DProjGradEXT");
      } else {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "texture2DProjGradARB", "_gl4es_texture2DProjGrad");
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), texture2DProjGradAlt, Tmp, &tmpsize);
      }
  }
  if(!isVertex && (gl4es_find_string(Tmp, "textureCubeGradARB"))) {
      if(hardext.shaderlod) {
        if(!hardext.cubelod)
          Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "textureCubeGradARB", "textureCubeGradEXT");
      } else {
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "textureCubeGradARB", "_gl4es_textureCubeGrad");
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), textureCubeGradAlt, Tmp, &tmpsize);
      }
  }

  // Some drivers have troubles with "\\\r\n" or "\\\n" sequences on preprocessor macros 
  newptr = Tmp;
  while (*newptr!=0x00) {
    if (*newptr == '\\') {
      if (*(newptr+1) == '\r' && *(newptr+2) == '\n')
        memmove(newptr, newptr+3, strlen(newptr+3)+1);
      else if (*(newptr+1) == '\n')
        memmove(newptr, newptr+2, strlen(newptr+2)+1);
    }

    newptr++;
  }

    // now check to remove trailling "f" after float, as it's not supported too
  newptr = Tmp;
  // simple state machine...
  int state = 0;
  while (*newptr!=0x00) {
    switch(state) {
      case 0:
        if ((*newptr >= '0') && (*newptr <= '9'))
          state = 1;  // integer part
        else if (*newptr == '.')
          state = 2;  // fractional part
        else if ((*newptr==' ') || (*newptr==0x0d) || (*newptr==0x0a) || (*newptr=='-') || (*newptr=='+') || (*newptr=='*') || (*newptr=='/') || (*newptr=='(') || (*newptr==')' || (*newptr=='>') || (*newptr=='<')))
          state = 0; // separator
        else 
          state = 3; // something else
        break;
      case 1: // integer part
        if ((*newptr >= '0') && (*newptr <= '9'))
          state = 1;  // integer part
        else if (*newptr == '.')
          state = 2;  // fractional part
        else if ((*newptr==' ') || (*newptr==0x0d) || (*newptr==0x0a) || (*newptr=='-') || (*newptr=='+') || (*newptr=='*') || (*newptr=='/') || (*newptr=='(') || (*newptr==')' || (*newptr=='>') || (*newptr=='<')))
          state = 0; // separator
        else  if (*newptr == 'f' ) {
          // remove that f
          memmove(newptr, newptr+1, strlen(newptr+1)+1);
          newptr--;
        } else
          state = 3;
          break;
      case 2: // fractionnal part
        if ((*newptr >= '0') && (*newptr <= '9'))
          state = 2;
        else if ((*newptr==' ') || (*newptr==0x0d) || (*newptr==0x0a) || (*newptr=='-') || (*newptr=='+') || (*newptr=='*') || (*newptr=='/') || (*newptr=='(') || (*newptr==')' || (*newptr=='>') || (*newptr=='<')))
          state = 0; // separator
        else  if (*newptr == 'f' ) {
          // remove that f
          memmove(newptr, newptr+1, strlen(newptr+1)+1);
          newptr--;
        } else
          state = 3;
          break;
      case 3:
        if ((*newptr==' ') || (*newptr==0x0d) || (*newptr==0x0a) || (*newptr=='-') || (*newptr=='+') || (*newptr=='*') || (*newptr=='/') || (*newptr=='(') || (*newptr==')' || (*newptr=='>') || (*newptr=='<')))
          state = 0; // separator
        else      
          state = 3;
        break;
    }
    newptr++;
  }
  Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FragDepth", (hardext.fragdepth)?"gl_FragDepthEXT":"fakeFragDepth");
  // builtin attribs
  if(isVertex) {
      // check for ftransform function
      if(strstr(Tmp, "ftransform(")) {
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_ftransformSource, Tmp, &tmpsize);
        // don't increment headline count, as all variying and attributes should be created before
      }
      // check for builtin OpenGL attributes...
      int n = sizeof(builtin_attrib)/sizeof(builtin_attrib_t);
      for (int i=0; i<n; i++) {
          if(strstr(Tmp, builtin_attrib[i].glname)) {
              // ok, this attribute is used
              // replace gl_name by _gl4es_ one
              Tmp = gl4es_inplace_replace(Tmp, &tmpsize, builtin_attrib[i].glname, builtin_attrib[i].name);
              // insert a declaration of it
              char def[100];
              sprintf(def, "attribute %s %s %s;\n", builtin_attrib[i].prec, builtin_attrib[i].type, builtin_attrib[i].name);
              Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline++), def, Tmp, &tmpsize);
          }
      }
      if(strstr(Tmp, gl_VertexAttrib)) {
        // Generic VA from Old Programs
        for (int i=0; i<MAX_VATTRIB; ++i) {
          char A[100];
          if(gl4es_find_string(Tmp, gl_VA[i])) {
            sprintf(A, "attribute highp vec4 %s%d;\n", gl4es_VertexAttrib, i);
            Tmp = gl4es_inplace_replace(Tmp, &tmpsize, gl_VA[i], gl4es_VA[i]);
            Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline++), A, Tmp, &tmpsize);
          }
        }
      }
  }
  // builtin varying
  int nvarying = 0;
  if(strstr(Tmp, "gl_Color") || need->need_color) {
    if(need->need_color<1) need->need_color = 1;
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_Color", (need->need_color==1)?"gl_FrontColor":"(gl_FrontFacing?gl_FrontColor:gl_BackColor)");
  }
  if(strstr(Tmp, "gl_FrontColor") || need->need_color) {
    if(need->need_color<1) need->need_color = 1;
    nvarying+=1;
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_frontColorSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_frontColorSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FrontColor", "_gl4es_FrontColor");
  }
  if(strstr(Tmp, "gl_BackColor") || (need->need_color==2)) {
    need->need_color = 2;
    nvarying+=1;
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_backColorSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_backColorSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_BackColor", "_gl4es_BackColor");
  }
  if(strstr(Tmp, "gl_SecondaryColor") || need->need_secondary) {
    if(need->need_secondary<1) need->need_secondary = 1;
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_SecondaryColor", (need->need_secondary==1)?"gl_FrontSecondaryColor":"(gl_FrontFacing?gl_FrontSecondaryColor:gl_BackSecondaryColor)");
  }
  if(strstr(Tmp, "gl_FrontSecondaryColor") || need->need_secondary) {
    if(need->need_secondary<1) need->need_secondary = 1;
    nvarying+=1;
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_frontSecondaryColorSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_frontSecondaryColorSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FrontSecondaryColor", "_gl4es_FrontSecondaryColor");
  }
  if(strstr(Tmp, "gl_BackSecondaryColor") || (need->need_secondary==2)) {
    need->need_secondary = 2;
    nvarying+=1;
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_backSecondaryColorSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_backSecondaryColorSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_BackSecondaryColor", "_gl4es_BackSecondaryColor");
  }
  if(strstr(Tmp, "gl_FogFragCoord") || need->need_fogcoord) {
    need->need_fogcoord = 1;
    nvarying+=1;
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_fogcoordSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_fogcoordSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FogFragCoord", "_gl4es_FogFragCoord");
  }
  // Get the max_texunit and the calc notexarray
  if(strstr(Tmp, "gl_TexCoord") || need->need_texcoord!=-1) {
    int ntex = need->need_texcoord;
    // Try to determine max gl_TexCoord used
    char* p = Tmp;
    int notexarray_ok = 1;
    while((p=strstr(p, gl_TexCoordSource))) {
      p+=strlen(gl_TexCoordSource);
      if(*p>='0' && *p<='9') {
        int n = (*p) - '0';
        if(p[1]>='0' && p[1]<='9')
          n = n*10 + (p[1] - '0');
        if (ntex<n) ntex = n;
      } else 
        notexarray_ok=0;
    }
    // if failed to determine, take max...
    if (ntex==-1) ntex = hardext.maxtex;
    // check constraint, and switch to notexarray if needed
    if (!notexarray && ntex+nvarying>hardext.maxvarying && !need->need_clean && notexarray_ok) {
      notexarray = 1;
      need->need_notexarray = 1;
    }
    // prefer notexarray...
    if(!isVertex && notexarray_ok && !need->need_clean) {
      notexarray = 1;
      need->need_notexarray = 1;
    }
    // check constraints
    if (!notexarray && ntex+nvarying>hardext.maxvarying) ntex = hardext.maxvarying - nvarying;
    need->need_texcoord = ntex;
    char d[100];
    if(notexarray) {
      for (int k=0; k<ntex+1; k++) {
        char d2[100];
        sprintf(d2, "gl_TexCoord[%d]", k);
        if(strstr(Tmp, d2)) {
          sprintf(d, gl4es_texcoordSourceAlt, k);
          Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), d, Tmp, &tmpsize);
          headline+=gl4es_countline(d);
          sprintf(d, "_gl4es_TexCoord_%d", k);
          Tmp = gl4es_inplace_replace(Tmp, &tmpsize, d2, d);
        }
        // check if texture is there
        sprintf(d2, "_gl4es_TexCoord_%d", k);
        if(strstr(Tmp, d2))
          need->need_texs |= (1<<k);
      }
    } else {
      sprintf(d, gl4es_texcoordSource, ntex+1);
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), d, Tmp, &tmpsize);
      headline+=gl4es_countline(d);
      Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_TexCoord", "_gl4es_TexCoord");
      // set textures as all ntex used
      for (int k=0; k<ntex+1; k++)
        need->need_texs |= (1<<k);
    }
  }

  // builtin matrices work
  {
    if(strstr(Tmp, "transpose(") || strstr(Tmp, "transpose ") || strstr(Tmp, "transpose\t")) {
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_transpose, Tmp, &tmpsize);
      gl4es_inplace_replace(Tmp, &tmpsize, "transpose", "gl4es_transpose");
      // don't increment headline count, as all variying and attributes should be created before
    }
    // check for builtin matrix uniform...
    {
      // first check number of texture matrices used
      int ntex = -1;
      // Try to determine max Texture matrice used, for each transposed inverse or regular...
      for(int i=0; i<4; ++i) {
        char* p = Tmp;
        while((p=strstr(p, gl_TexMatrixSources[i]))) {
          p+=strlen(gl_TexMatrixSources[i]);
          if(*p>='0' && *p<='9') {
            int n = 0;
            while(*p>='0' && *p<='9')
              n = n*10 + (*(p++) - '0');
            
            if (ntex<n) ntex = n;
          }
        }
      }
        
      // if failed to determine, take max...
      if (ntex==-1) ntex = need->need_texcoord; else ++ntex;
      // change gl_TextureMatrix[X] to gl_TextureMatrix_X if possible
      int change_textmat = notexarray;
      if(!change_textmat) {
        change_textmat = 1;
        char* p = Tmp;
        while(change_textmat && (p=strstr(p, "gl_TextureMatrix["))) {
          p += strlen("gl_TextureMatrix[");
          while((*p)>='0' && (*p)<='9') ++p;
          if((*p)!=']')
            change_textmat = 0;
        }
      }
      if(change_textmat) {
        for (int k=0; k<ntex+1; k++) {
          char d[100];
          char d2[100];
          sprintf(d2, "gl_TextureMatrix[%d]", k);
          if(strstr(Tmp, d2)) {
            sprintf(d, "gl_TextureMatrix_%d", k);
            Tmp = gl4es_inplace_replace(Tmp, &tmpsize, d2, d);
          }
        }
      }

      int n = sizeof(builtin_matrix)/sizeof(builtin_matrix_t);
      for (int i=0; i<n; i++) {
          if(strstr(Tmp, builtin_matrix[i].glname)) {
              // ok, this matrix is used
              // replace gl_name by _gl4es_ one
              Tmp = gl4es_inplace_replace(Tmp, &tmpsize, builtin_matrix[i].glname, builtin_matrix[i].name);
              // insert a declaration of it
              char def[100];
              int ishighp = (isVertex || hardext.highp)?1:0;
              if(builtin_matrix[i].matrix == MAT_N) {
                if(need->need_normalmatrix && !hardext.highp)
                  ishighp = 0;
                if(!hardext.highp && !isVertex)
                  need->need_normalmatrix = 1;
              }
              if(builtin_matrix[i].matrix == MAT_MV) {
                if(need->need_mvmatrix && !hardext.highp)
                  ishighp = 0;
                if(!hardext.highp && !isVertex)
                  need->need_mvmatrix = 1;
              }
              if(builtin_matrix[i].matrix == MAT_MVP) {
                if(need->need_mvpmatrix && !hardext.highp)
                  ishighp = 0;
                if(!hardext.highp && !isVertex)
                  need->need_mvpmatrix = 1;
              }
              if(builtin_matrix[i].texarray)
                  sprintf(def, "uniform %s%s %s[%d];\n", (ishighp)?"highp ":"mediump ", builtin_matrix[i].type, builtin_matrix[i].name, ntex);
              else
                  sprintf(def, "uniform %s%s %s;\n", (ishighp)?"highp ":"mediump ", builtin_matrix[i].type, builtin_matrix[i].name);
              Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline++), def, Tmp, &tmpsize);
          }
      }
    }
  }
  // Handling of gl_LightSource[x].halfVector => normalize(gl_LightSource[x].position - gl_Vertex), but what if in the FragShader ?
/*  if(strstr(Tmp, "halfVector"))
  {
    char *p = Tmp;
    while((p=strstr(p, "gl_LightSource["))) {
      char *p2 = strchr(p, ']');
      if (p2 && !strncmp(p2, "].halfVector", strlen("].halfVector"))) {
        // found an occurrence, lets change
        char p3[500];
        strncpy(p3,p, (p2-p)+1); p3[(p2-p)+1]='\0';
        char p4[500], p5[500];
        sprintf(p4, "%s.halfVector", p3);
        sprintf(p5, "normalize(normalize(%s.position.xyz) + vec3(0., 0., 1.))", p3);
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, p4, p5);
        p = Tmp;
      } else
        ++p;
    }
  }*/
  // cleaning up the "centroid" keyword...
  if(strstr(Tmp, "centroid"))
  {
    char *p = Tmp;
    while((p=strstr(p, "centroid"))!=NULL)
    {
      if(p[8]==' ' || p[8]=='\t') { // what next...
        const char* p2 = gl4es_get_next_str(p+8);
        if(strcmp(p2, "uniform")==0 || strcmp(p2, "varying")==0) {
          memset(p, ' ', 8);  // erase the keyword...
        }
      } 
      p+=8;
    }
  }
  
  // check for builtin OpenGL gl_LightSource & friends
  if(strstr(Tmp, "gl_LightSourceParameters") || strstr(Tmp, "gl_LightSource"))
  {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_LightSourceParametersSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_LightSourceParametersSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_LightSourceParameters", "_gl4es_LightSourceParameters");
  }
  if(strstr(Tmp, "gl_LightModelParameters") || strstr(Tmp, "gl_LightModel"))
  {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_LightModelParametersSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_LightModelParametersSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_LightModelParameters", "_gl4es_LightModelParameters");
  }
  if(strstr(Tmp, "gl_LightModelProducts") || strstr(Tmp, "gl_FrontLightModelProduct") || strstr(Tmp, "gl_BackLightModelProduct"))
  {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_LightModelProductsSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_LightModelProductsSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_LightModelProducts", "_gl4es_LightModelProducts");
  }
  if(strstr(Tmp, "gl_LightProducts") || strstr(Tmp, "gl_FrontLightProduct") || strstr(Tmp, "gl_BackLightProduct"))
  {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_LightProductsSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_LightProductsSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_LightProducts", "_gl4es_LightProducts");
  }
  if(strstr(Tmp, "gl_MaterialParameters ") || (strstr(Tmp, "gl_FrontMaterial")) || strstr(Tmp, "gl_BackMaterial"))
  {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_MaterialParametersSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_MaterialParametersSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_MaterialParameters", "_gl4es_MaterialParameters");
  }
  if(strstr(Tmp, "gl_LightSource")) {
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_LightSource", "_gl4es_LightSource");
  }
  if(strstr(Tmp, "gl_LightModel"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_LightModel", "_gl4es_LightModel");
  if(strstr(Tmp, "gl_FrontLightModelProduct"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FrontLightModelProduct", "_gl4es_FrontLightModelProduct");
  if(strstr(Tmp, "gl_BackLightModelProduct"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_BackLightModelProduct", "_gl4es_BackLightModelProduct");
  if(strstr(Tmp, "gl_FrontLightProduct"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FrontLightProduct", "_gl4es_FrontLightProduct");
  if(strstr(Tmp, "gl_BackLightProduct"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_BackLightProduct", "_gl4es_BackLightProduct");
  if(strstr(Tmp, "gl_FrontMaterial"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FrontMaterial", "_gl4es_FrontMaterial");
  if(strstr(Tmp, "gl_BackMaterial"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_BackMaterial", "_gl4es_BackMaterial");
  if(strstr(Tmp, "gl_MaxLights"))
  {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 2), gl4es_MaxLightsSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_MaxLightsSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_MaxLights", "_gl4es_MaxLights");
  }
  if(strstr(Tmp, "gl_NormalScale")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_normalscaleSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_normalscaleSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_NormalScale", "_gl4es_NormalScale");
  }
  if(strstr(Tmp, "gl_InstanceID") || strstr(Tmp, "gl_InstanceIDARB")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_instanceID, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_instanceID);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_InstanceIDARB", "_gl4es_InstanceID");
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_InstanceID", "_gl4es_InstanceID");
  }
  if(strstr(Tmp, "gl_ClipPlane")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_clipplanesSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_clipplanesSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_ClipPlane", "_gl4es_ClipPlane");
  }
  if(strstr(Tmp, "gl_MaxClipPlanes")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 2), gl4es_MaxClipPlanesSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_MaxClipPlanesSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_MaxClipPlanes", "_gl4es_MaxClipPlanes");
  }

  if(strstr(Tmp, "gl_PointParameters") || strstr(Tmp, "gl_Point"))
    {
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_PointSpriteSource, Tmp, &tmpsize);
      headline+=gl4es_countline(gl4es_PointSpriteSource);
      Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_PointParameters", "_gl4es_PointParameters");
    }
  if(strstr(Tmp, "gl_Point"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_Point", "_gl4es_Point");
  if(strstr(Tmp, "gl_FogParameters") || strstr(Tmp, "gl_Fog"))
    {
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), hardext.highp?gl4es_FogParametersSourceHighp:gl4es_FogParametersSource, Tmp, &tmpsize);
      headline+=gl4es_countline(gl4es_FogParametersSource);
      Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_FogParameters", "_gl4es_FogParameters");
    }
  if(strstr(Tmp, "gl_Fog"))
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_Fog", "_gl4es_Fog");
  if(strstr(Tmp, "gl_TextureEnvColor")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texenvcolorSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texenvcolorSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_TextureEnvColor", "_gl4es_TextureEnvColor");
  }
  if(strstr(Tmp, "gl_EyePlaneS")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgeneyeSource[0], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgeneyeSource[0]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_EyePlaneS", "_gl4es_EyePlaneS");
  }
  if(strstr(Tmp, "gl_EyePlaneT")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgeneyeSource[1], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgeneyeSource[1]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_EyePlaneT", "_gl4es_EyePlaneT");
  }
  if(strstr(Tmp, "gl_EyePlaneR")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgeneyeSource[2], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgeneyeSource[2]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_EyePlaneR", "_gl4es_EyePlaneR");
  }
  if(strstr(Tmp, "gl_EyePlaneQ")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgeneyeSource[3], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgeneyeSource[3]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_EyePlaneQ", "_gl4es_EyePlaneQ");
  }
  if(strstr(Tmp, "gl_ObjectPlaneS")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgenobjSource[0], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgenobjSource[0]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_ObjectPlaneS", "_gl4es_ObjectPlaneS");
  }
  if(strstr(Tmp, "gl_ObjectPlaneT")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgenobjSource[1], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgenobjSource[1]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_ObjectPlaneT", "_gl4es_ObjectPlaneT");
  }
  if(strstr(Tmp, "gl_ObjectPlaneR")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgenobjSource[2], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgenobjSource[2]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_ObjectPlaneR", "_gl4es_ObjectPlaneR");
  }
  if(strstr(Tmp, "gl_ObjectPlaneQ")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), gl4es_texgenobjSource[3], Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_texgenobjSource[3]);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_ObjectPlaneQ", "_gl4es_ObjectPlaneQ");
  }

  if(strstr(Tmp, "gl_MaxTextureUnits")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 2), gl4es_MaxTextureUnitsSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_MaxTextureUnitsSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_MaxTextureUnits", "_gl4es_MaxTextureUnits");
  }
  if(strstr(Tmp, "gl_MaxTextureCoords")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 2), gl4es_MaxTextureCoordsSource, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_MaxTextureCoordsSource);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_MaxTextureCoords", "_gl4es_MaxTextureCoords");
  }
  if(strstr(Tmp, "gl_ClipVertex")) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 2), gl4es_ClipVertex, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_ClipVertex);
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "gl_ClipVertex", gl4es_ClipVertexSource);
    need->need_clipvertex = 1;
  } else if(isVertex && need && need->need_clipvertex) {
    Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, 2), gl4es_ClipVertex, Tmp, &tmpsize);
    headline+=gl4es_countline(gl4es_ClipVertex);
    char *p = strchr(gl4es_find_string_nc(Tmp, "main"), '{'); // find the openning curly bracket of main
    if(p) {
      // add regular clipping at start of main
      Tmp = gl4es_inplace_insert(p+1, gl4es_ClipVertex_clip, Tmp, &tmpsize);
    }
  }
  //oldprogram uniforms...
  if(gl4es_find_string(Tmp, gl_ProgramEnv)) {
    // check if array can be removed
    int maxind = -1;
    int noarray_ok = 1;
    char* p = Tmp;
    while(noarray_ok && (p=gl4es_find_string_nc(p, gl_ProgramEnv))) {
      p+=strlen(gl_ProgramEnv);
      if(*p=='[') {
        ++p;
        if(*p>='0' && *p<='9') {
          int n = (*p) - '0';
          if(p[1]>='0' && p[1]<='9')
            n = n*10 + (p[1] - '0');
          if (maxind<n) maxind = n;
        } else 
          noarray_ok=0;
      } else
        noarray_ok=0;
    }
    if(noarray_ok) {
      // ok, so change array to single...
      char F[60], T[60], U[300];
      for(int i=0; i<=maxind; ++i) {
        sprintf(F, "%s[%d]", gl_ProgramEnv, i);
        sprintf(T, "_gl4es_%s_ProgramEnv_%d", isVertex?"Vertex":"Fragment", i);
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, F, T);
        if(gl4es_find_string(Tmp, T)) {
          // add the uniform declaration if needed
          sprintf(U, "uniform vec4 %s;\n", T);
          Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), U, Tmp, &tmpsize);
          headline += 1;
        }
      }
    } else {
      // need the full array...
      char T[60], U[300];
      sprintf(T, "_gl4es_%s_ProgramEnv", isVertex?"Vertex":"Fragment");
      sprintf(U, "uniform vec4 %s[%d];\n", T, isVertex?MAX_VTX_PROG_ENV_PARAMS:MAX_FRG_PROG_ENV_PARAMS);
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), U, Tmp, &tmpsize);
      headline += 1;
      Tmp = gl4es_inplace_replace(Tmp, &tmpsize, gl_ProgramEnv, T);
    }
  }
  if(gl4es_find_string(Tmp, gl_ProgramLocal)) {
    // check if array can be removed
    int maxind = -1;
    int noarray_ok = 1;
    char* p = Tmp;
    while(noarray_ok && (p=gl4es_find_string_nc(p, gl_ProgramLocal))) {
      p+=strlen(gl_ProgramLocal);
      if(*p=='[') {
        ++p;
        if(*p>='0' && *p<='9') {
          int n = (*p) - '0';
          if(p[1]>='0' && p[1]<='9')
            n = n*10 + (p[1] - '0');
          if (maxind<n) maxind = n;
        } else 
          noarray_ok=0;
      } else
        noarray_ok=0;
    }
    if(noarray_ok) {
      // ok, so change array to single...
      char F[60], T[60], U[300];
      for(int i=0; i<=maxind; ++i) {
        sprintf(F, "%s[%d]", gl_ProgramLocal, i);
        sprintf(T, "_gl4es_%s_ProgramLocal_%d", isVertex?"Vertex":"Fragment", i);
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, F, T);
        if(gl4es_find_string(Tmp, T)) {
          // add the uniform declaration if needed
          sprintf(U, "uniform vec4 %s;\n", T);
          Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), U, Tmp, &tmpsize);
          headline += 1;
        }
      }
    } else {
      // need the full array...
      char T[60], U[300];
      sprintf(T, "_gl4es_%s_ProgramLocal", isVertex?"Vertex":"Fragment");
      sprintf(U, "uniform vec4 %s[%d];\n", T, isVertex?MAX_VTX_PROG_LOC_PARAMS:MAX_FRG_PROG_LOC_PARAMS);
      Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), U, Tmp, &tmpsize);
      headline += 1;
      Tmp = gl4es_inplace_replace(Tmp, &tmpsize, gl_ProgramLocal, T);
    }
  }
  #define GO(A) \
  if(strstr(Tmp, gl_Samplers ## A)) {                                   \
    char S[60], D[60], U[60];                                           \
    for(int i=0; i<MAX_TEX; ++i) {                                      \
      sprintf(S, "%s%d", gl_Samplers ## A, i);                          \
      if(gl4es_find_string(Tmp, S)) {                                          \
        sprintf(D, "%s%d", gl4es_Samplers ## A, i);                     \
        sprintf(U, "%s%d;\n", gl4es_Samplers ## A ## _uniform, i);      \
        Tmp = gl4es_inplace_replace(Tmp, &tmpsize, S, D);                      \
        Tmp = gl4es_inplace_insert(gl4es_getline(Tmp, headline), U, Tmp, &tmpsize);  \
        headline += 1;                                                  \
      }                                                                 \
    }                                                                   \
  }
  GO(1D)
  GO(2D)
  GO(3D)
  GO(Cube)
  #undef GO

  // non-square matrix handling
  // the square one first
  if(strstr(Tmp, "mat2x2")) {
    // better to use #define ?
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "mat2x2", "mat2");
  }
  if(strstr(Tmp, "mat3x3")) {
    // better to use #define ?
    Tmp = gl4es_inplace_replace(Tmp, &tmpsize, "mat3x3", "mat3");
  }
  
  // finish
  if((globals4es.dbgshaderconv&maskafter)==maskafter) {
    printf("New Shader source:\n%s\n", Tmp);
  }
  // clean preproc'd source
  if(versionString != NULL)
    free(versionString);
  if(pEntry!=pBuffer)
    free(pBuffer);
  return Tmp;
}

int isBuiltinAttrib(const char* name) {
    int n = sizeof(builtin_attrib)/sizeof(builtin_attrib_t);
    for (int i=0; i<n; i++) {
        if (strcmp(builtin_attrib[i].name, name)==0)
            return builtin_attrib[i].attrib;
    }
    return -1;
}

int isBuiltinMatrix(const char* name) {
    int ret = -1;
    int n = sizeof(builtin_matrix)/sizeof(builtin_matrix_t);
    for (int i=0; i<n && ret==-1; i++) {
        if (strncmp(builtin_matrix[i].name, name, strlen(builtin_matrix[i].name))==0) {
            int l=strlen(builtin_matrix[i].name);
            if(strlen(name)==l 
            || (strlen(name)==l+3 && name[l]=='[' && builtin_matrix[i].texarray)
            || (strlen(name)==l+4 && name[l]=='[' && builtin_matrix[i].texarray)
            ) {
                ret=builtin_matrix[i].matrix;
                if(builtin_matrix[i].texarray) {
                    int n = name[l+1] - '0';
                    if(name[l+2]>='0' && name[l+2]<='9')
                      n = n*10 + name[l+2]-'0';
                    ret+=n*4;
                }
            }
        }
    }
    return ret;
}

const char* hasBuiltinAttrib(const char* vertexShader, int Att) {
    if(!vertexShader) // can happens (like if the shader is a pure GLES2 one)
      return NULL;
    // first search for the string
    const char* ret = NULL;
    if(hardext.maxvattrib>8) {
      int n = sizeof(builtin_attrib)/sizeof(builtin_attrib_t);
      for (int i=0; i<n && !ret; i++) {
          if (builtin_attrib[i].attrib == Att)
              ret = builtin_attrib[i].name;
      }
    } else {
      int n = sizeof(builtin_attrib_compressed)/sizeof(builtin_attrib_t);
      for (int i=0; i<n && !ret; i++) {
          if (builtin_attrib_compressed[i].attrib == Att)
              ret = builtin_attrib_compressed[i].name;
      }
    }
    if (!ret)
      return NULL;
    if(strstr(vertexShader, ret)) // it's here!
      return ret;
    // check for old program generic vertex attribs
    if(strstr(vertexShader, gl4es_VA[Att]))
      return gl4es_VA[Att];
    // nope
    return NULL;
}

const char* builtinAttribGLName(const char* name) {
  // no need to check for compressed array here...
  int n = sizeof(builtin_attrib)/sizeof(builtin_attrib_t);
  for(int i=0; i<n; ++i)
    if(!strcmp(name, builtin_attrib[i].name))
      return builtin_attrib[i].glname;
  if(!strncmp(name, gl4es_VertexAttrib, strlen(gl4es_VertexAttrib))) {
    int l = strlen(gl4es_VertexAttrib);
    int n = 0;
    while(name[l]>='0' && name[l]<='9')
      n = n*10 + name[l++]-'0';
    return gl_VA[n];
  }
  return name;
}

const char* builtinAttribInternalName(const char* name) {
  // no need to check for compressed array here...
  int n = sizeof(builtin_attrib)/sizeof(builtin_attrib_t);
  for(int i=0; i<n; ++i)
    if(!strcmp(name, builtin_attrib[i].glname))
      return builtin_attrib[i].name;
  if(!strncmp(name, gl_VertexAttrib, strlen(gl_VertexAttrib))) {
    int l = strlen(gl_VertexAttrib);
    int n = 0;
    while(name[l]>='0' && name[l]<='9')
      n = n*10 + name[l++]-'0';
    return gl4es_VA[n];
  }
  return name;
}
