#ifndef __FPE_CACHE_H__
#define __FPE_CACHE_H__

#ifdef _GL4ES_FPE_H_ // there is fpe.h with typedefs already
#   ifndef fpe_state_t 
#   define fpe_state_t fpe_state_t
#   define DO_NOT_FORGET_TO_UNDEF_fpe_state_t
#   endif
#   ifndef fpe_fpe_t 
#   define fpe_fpe_t fpe_fpe_t
#   define DO_NOT_FORGET_TO_UNDEF_fpe_fpe_t
#   endif
#   ifndef kh_fpecachelist_t 
#   define kh_fpecachelist_t kh_fpecachelist_t
#   define DO_NOT_FORGET_TO_UNDEF_kh_fpecachelist_t
#   endif
#   ifndef fpe_cache_t 
#   define fpe_cache_t kh_fpecachelist_t
#   define DO_NOT_FORGET_TO_UNDEF_fpe_cache_t
#   endif
#endif // _GL4ES_FPE_H_

#ifndef fpe_state_t
typedef struct fpe_state_s fpe_state_t;
#endif
typedef fpe_state_t *kh_fpe_t;
#ifndef fpe_fpe_t
typedef struct fpe_fpe_s fpe_fpe_t;
#endif
#ifndef kh_fpecachelist_t
typedef struct kh_fpecachelist_s kh_fpecachelist_t;
#endif
#ifndef fpe_cache_t
#define fpe_cache_t kh_fpecachelist_t
#endif

fpe_cache_t* fpe_NewCache();
void fpe_disposeCache(fpe_cache_t* cache, int freeprog);
fpe_fpe_t *fpe_GetCache(fpe_cache_t *cur, fpe_state_t *state, int fixed);

void fpe_InitPSA(const char* name);
void fpe_FreePSA();
void fpe_readPSA();
void fpe_writePSA();
int fpe_GetProgramPSA(GLuint program, fpe_state_t* state);
void fpe_AddProgramPSA(GLuint program, fpe_state_t* state);

#ifdef DO_NOT_FORGET_TO_UNDEF_fpe_state_t 
#undef fpe_state_t
#undef DO_NOT_FORGET_TO_UNDEF_fpe_state_t
#endif
#ifdef DO_NOT_FORGET_TO_UNDEF_fpe_fpe_t 
#undef fpe_fpe_t
#undef DO_NOT_FORGET_TO_UNDEF_fpe_fpe_t
#endif
#ifdef DO_NOT_FORGET_TO_UNDEF_kh_fpecachelist_t 
#undef kh_fpecachelist_t
#undef DO_NOT_FORGET_TO_UNDEF_kh_fpecachelist_t
#endif
#ifdef DO_NOT_FORGET_TO_UNDEF_fpe_cache_t 
#undef fpe_cache_t
#undef DO_NOT_FORGET_TO_UNDEF_fpe_cache_t
#endif

#endif //__FPE_CACHE_H__