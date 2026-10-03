#ifndef _GL4ES_ATTRIBUTES_H_
#define _GL4ES_ATTRIBUTES_H_

#ifndef EXPORT
 #if defined(__EMSCRIPTEN__) || defined(__APPLE__)
   #define EXPORT
 #elif defined(STATICLIB)
   #define EXPORT
 #elif defined(_WIN32) || defined(__CYGWIN__)
   #define EXPORT __declspec(dllexport)
 #elif defined(__GNUC__)
   #define EXPORT __attribute__((visibility("default")))
 #else
   #define EXPORT
 #endif
#endif // EXPORT

#ifndef _STR
 #define _STR(n) #n
#endif
#ifndef _MNG
 #if defined(_WIN32) && !defined(_WIN64)
  #ifdef _MSC_VER
   #define _MNG(NAME,SUF) "_" _STR(NAME) "@" SUF
  #elif defined(__GNUC__)
   #define _MNG(NAME,SUF) _STR(NAME) "@" SUF
  #endif
 #endif
 #ifdef _MNG
  #define _STM(NAME,DEF) _MNG(NAME,_VCR(_SUF1 DEF))
  #define _SUFF(...) _VCR(_SUF1 __VA_ARGS__)
  #define _SU2F(...) _VCR(_SUF2 __VA_ARGS__)
  #define _S2F1(...) _VCR(_SU21 __VA_ARGS__)
  #define _SUF1(...) \
   _VCS(_SUFS,(0,##__VA_ARGS__,100,96,92,88,84,80,76,72,68,64,60,56,52,\
    48,44,40,36,32,28,24,20,16,12,8,4,0))
  #define _SUF2(...) \
   _VCS(_SUFS,(0,##__VA_ARGS__,200,192,184,176,168,160,152,144,136,128,120,\
    112,104,96,88,80,72,64,56,48,40,32,24,16,8,0))
  #define _SU21(...) \
   _VCS(_SUFS,(0,##__VA_ARGS__,196,188,180,172,164,156,148,140,132,124,116,\
    108,100,92,84,76,68,60,52,44,36,28,20,12,4))
  #define _SUFS(a0,a1,a2,a3,a4,a5,a6,a7,a8,a9,\
    a10,a11,a12,a13,a14,a15,a16,a17,a18,a19,\
    a20,a21,a22,a23,a24,a25,n,...) #n
  #define _VCS(a,b) a b
  #define _VCR(n) n
 #endif
#endif
#ifndef _MNG
 #define _MNG(NAME,SUF) _STR(NAME)
 #define _STM(NAME,DEF) _STR(NAME)
#endif

#ifndef AliasDecl
 #ifdef __GNUC__
  #define AliasDecl(RET,NAME,DEF,OLD) \
   RET APIENTRY_GL4ES NAME DEF __attribute__((alias(_STM(OLD,DEF))))
 #elif defined(_MSC_VER)
  #define AliasDecl(RET,NAME,DEF,OLD) \
      __pragma(comment(linker, "/ALTERNATENAME:" _STM(NAME,DEF) "=" _STM(OLD,DEF))) \
      RET APIENTRY_GL4ES NAME DEF
 #else
  #define AliasDecl(RET,NAME,DEF,OLD) \
      RET APIENTRY_GL4ES NAME DEF
 #endif
#endif // AliasDecl

#ifndef AliasExport
 #if !defined(__EMSCRIPTEN__) && !defined(__APPLE__)
  #ifdef __GNUC__
   #define _AliasExport_(RET,ENM,DEF,INM,SUF) EXPORT \
      RET APIENTRY_GL4ES ENM DEF __attribute__((alias(_MNG(gl4es_##INM,SUF))))
   #define NonAliasExportDecl(RET,NAME,DEF) EXPORT \
      RET APIENTRY_GL4ES NAME DEF
  #elif defined(_MSC_VER)
   #define _DIR(DIR,ENM,INM,OP) \
       __pragma(comment(linker, "/" DIR ":" ENM OP INM))
   #ifdef STATICLIB
    #define _EXP(ENM,SUF)
   #elif !defined(_WIN64) && (_MSC_VER >= 1911 || defined(LINK_ULINK))
    #define _EXP(ENM,SUF) _DIR("EXPORT",_MNG(ENM,SUF),_STR(ENM),",exportas,")
   #else
    #ifndef _WIN64
      /* Unfortunatelly, old MS-link can create undecorated dll-names
       * with decorated library names only when use DEF-file.
       * Create stdcall-alias.*/
      #define _DOPEXP(ENM,SUF) _DIR("EXPORT",_MNG(ENM,SUF),,)
    #else
      #define _DOPEXP(ENM,SUF)
    #endif
    #define _EXP(ENM,SUF) _DIR("EXPORT",_STR(ENM),_MNG(ENM,SUF),"=") \
      _DOPEXP(ENM,SUF)
   #endif
   #define _DECL(ENM,INM,SUF) \
       _EXP(ENM,SUF) _DIR("ALTERNATENAME",_MNG(ENM,SUF),_MNG(INM,SUF), "=")
   #define _AliasExport_(RET,ENM,DEF,INM,SUF) _DECL(ENM,gl4es_##INM,SUF) \
       RET APIENTRY_GL4ES ENM DEF
   #define NonAliasExportDecl(RET,NAME,DEF) _EXP(NAME,_SUFF(DEF)) \
      RET APIENTRY_GL4ES NAME DEF
  #endif
  #define AliasExport(RET,NAME,X,DEF) \
      _AliasExport_(RET,NAME##X,DEF,NAME,_SUFF(DEF))
  #define AliasExport_A(RET,NAME,X,DEF,INM) \
      _AliasExport_(RET,NAME##X,DEF,INM,_SUFF(DEF))
  #define AliasExport_D(RET,NAME,X,DEF) \
      _AliasExport_(RET,NAME##X,DEF,NAME,_SU2F(DEF))
  #define AliasExport_D_1(RET,NAME,X,DEF) \
      _AliasExport_(RET,NAME##X,DEF,NAME,_S2F1(DEF))
  #define AliasExport_M(RET,NAME,X,DEF,SUF) \
      _AliasExport_(RET,NAME##X,DEF,NAME,_STR(SUF))
  #define AliasExport_V(RET,NAME) \
      _AliasExport_(RET,NAME,(void),NAME,"0")
 #endif
 #ifndef _AliasExport_
  #define AliasExport(RET,NAME,X,DEF)       RET NAME##X DEF
  #define AliasExport_A(RET,NAME,X,DEF,INM) AliasExport(RET,NAME,X,DEF)
  #define AliasExport_D(RET,NAME,X,DEF)     AliasExport(RET,NAME,X,DEF)
  #define AliasExport_D_1(RET,NAME,X,DEF)   AliasExport(RET,NAME,X,DEF)
  #define AliasExport_M(RET,NAME,X,DEF,SUF) AliasExport(RET,NAME,X,DEF)
  #define AliasExport_V(RET,NAME)           AliasExport(RET,NAME,,(void))
  #define NonAliasExportDecl(RET,NAME,DEF)  RET NAME DEF
 #endif
 #define AliasExport_1(RET,NAME,X,DEF) AliasExport(RET,NAME,X,DEF)
#endif // AliasExport

// ensure APIENTRY used from platform if it defined, win32 should use STDCALL conversion
#include <GLES/glplatform.h>
#ifndef NOEGL
#include <EGL/eglplatform.h>
#endif

#ifndef APIENTRY
# if defined(_WIN32) || defined(__MINGW32__)
#  define APIENTRY __stdcall
# else
#  define APIENTRY
# endif
#endif

#ifndef SOFTFLOAT
#ifdef __arm__
#define SOFTFLOAT __attribute__((pcs("aapcs")))
#else
#define SOFTFLOAT
#endif
#endif // SOFTFLOAT

#ifndef APIENTRY_GLES
#ifdef GLES_SOFTFLOAT
#define APIENTRY_GLES APIENTRY SOFTFLOAT
#else
#define APIENTRY_GLES APIENTRY
#endif
#endif // APIENTRY_GLES


#ifndef APIENTRY_GL4ES
#ifdef GL_SOFTFLOAT
#define APIENTRY_GL4ES APIENTRY SOFTFLOAT
#else
#define APIENTRY_GL4ES APIENTRY
#endif
#endif //APIENTRY_GL4ES

#ifndef FASTMATH
#ifdef __GNUC__
 #ifdef __arm__
  #ifdef __ARM_PCS_VFP
   //#warning Arm Hardfloat detected
   #define FASTMATH
  #else // __ARM_PCS_VFP
   #if defined(__ARM_FP) && defined(PANDORA)
    //#warning Arm SoftFP detected
    #define FASTMATH __attribute__((pcs("aapcs-vfp")))
   #else // defined(__ARM_FP) && defined(PANDORA)
	//#warning Arm no FP detected
	#define FASTMATH
   #endif // defined(__ARM_FP) && defined(PANDORA)
  #endif // __ARM_PCS_VFP
 #else // __arm__
  #define FASTMATH
 #endif // __arm__
#else // __GNUC__
 #define FASTMATH
#endif // __GNUC__
#endif // FASTMATH

#endif // _GL4ES_ATTRIBUTES_H_
