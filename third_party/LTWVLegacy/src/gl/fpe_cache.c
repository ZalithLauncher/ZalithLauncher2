#include <stdio.h>
#include <string.h>

#include "../glx/hardext.h"
#include "init.h"
#include "logs.h"
#include "debug.h"
#include "program.h"

#include "fpe.h"


#define fpe_state_t fpe_state_t
#define fpe_fpe_t fpe_fpe_t
#define kh_fpecachelist_t kh_fpecachelist_t
#include "fpe_cache.h"
#undef fpe_state_t
#undef fpe_fpe_t
#undef kh_fpecachelist_t

#ifndef fpe_cache_t
#   define fpe_cache_t kh_fpecachelist_t
#endif

//#define DEBUG
#ifdef DEBUG
#pragma GCC optimize 0
#define DBG(a) a
#else
#define DBG(a)
#endif

static const char PSA_SIGN[] = "GL4ES PrecompiledShaderArchive";
#define CACHE_VERSION 112

static kh_inline khint_t _hash_fpe(fpe_state_t *p)
{
    const char* s = (const char*)p;
    khint_t h = (khint_t)*s;
    for (int i=1 ; i<sizeof(fpe_state_t); ++i) h = (h << 5) - h + (khint_t)*(++s);
    return h;
}

#define kh_fpe_hash_func(key) _hash_fpe(key)

#define kh_fpe_hash_equal(a, b) (memcmp(a, b, sizeof(fpe_state_t)) == 0)

#define KHASH_MAP_INIT_FPE(name, khval_t)                                \
    KHASH_INIT(name, kh_fpe_t, khval_t, 1, kh_fpe_hash_func, kh_fpe_hash_equal)

#define kh_fpecachelist_t kh_fpecachelist_s
KHASH_MAP_INIT_FPE(fpecachelist, fpe_fpe_t *);
#undef kh_fpecachelist_t

// ********* Cache handling *********

fpe_cache_t* fpe_NewCache() {
    khash_t(fpecachelist) *cache = kh_init(fpecachelist);
    return cache;
}

void fpe_disposeCache(fpe_cache_t* cache, int freeprog) {
    if(!cache) return;
    fpe_fpe_t *m;
    kh_foreach_value(cache, m, 
        if(freeprog) {
            if(m->glprogram)
                gl4es_glDeleteProgram(m->glprogram->id);
        }
        free(m);
    )
    kh_destroy(fpecachelist, cache);
}

fpe_fpe_t *fpe_GetCache(fpe_cache_t *cur, fpe_state_t *state, int fixed) {
    khint_t k;
    int r;

    k = kh_get(fpecachelist, cur, state);
    if(k != kh_end(cur)) {
        return kh_value(cur, k);
    } else {
        fpe_fpe_t *n = (fpe_fpe_t*)calloc(1, sizeof(fpe_fpe_t));
        memcpy(&n->state, state, sizeof(fpe_state_t));
        k = kh_put(fpecachelist, cur, &n->state, &r);
        kh_value(cur, k) = n;
        return n;
    }
}

typedef struct psa_s {
    fpe_state_t state;
    GLenum      format;
    int         size;
    void*       prog;
} psa_t;

KHASH_MAP_INIT_FPE(psalist, psa_t *);

// Precompiled Shader Archive
typedef struct gl4es_psa_s {
    int             size;
    int             dirty;
    kh_psalist_t*   cache;    
} gl4es_psa_t;

static gl4es_psa_t *psa = NULL;
static char *psa_name = NULL;

void fpe_readPSA()
{
    if(!psa || !psa_name)
        return;
    FILE *f = fopen(psa_name, "rb");
    if(!f)
        return;
    char tmp[sizeof(PSA_SIGN)];
    if(fread(tmp, sizeof(PSA_SIGN), 1, f)!=1) {
        fclose(f);
        return; //to short
    }
    if(strcmp(tmp, PSA_SIGN)!=0) {
        fclose(f);
        return; // bad signature
    }
    int version = 0;
    if(fread(&version, sizeof(version), 1, f)!=1) {
        fclose(f);
        return;
    }
    if(version!=CACHE_VERSION) {
        fclose(f);
        return; // unsupported version
    }
    int sz_fpe = 0;
    if(fread(&sz_fpe, sizeof(sz_fpe), 1, f)!=1) {
        fclose(f);
        return;
    }
    if(sz_fpe!=sizeof(fpe_state_t)) {
        fclose(f);
        return; // maybe try to adapt instead?
    }
    int n = 0;
    if(fread(&n, sizeof(n), 1, f)!=1) {
        fclose(f);
        return;
    }
    for (int i=0; i<n; ++i) {
        psa_t *p = (psa_t*)calloc(1, sizeof(psa_t));
        if(fread(&p->state, sizeof(p->state), 1, f)!=1) {
            free(p);
            fclose(f);
            return;
        }
        if(fread(&p->format, sizeof(p->format), 1, f)!=1) {
            free(p);
            fclose(f);
            return;
        }
        if(fread(&p->size, sizeof(p->size), 1, f)!=1) {
            free(p);
            fclose(f);
            return;
        }
        p->prog = malloc(p->size);
        if(fread(p->prog, p->size, 1, f)!=1) {
            free(p->prog);
            free(p);
            fclose(f);
            return;
        }
        int ret;
        khint_t k = kh_put(psalist, psa->cache, &p->state, &ret);
        kh_value(psa->cache, k) = p;
        psa->size = kh_size(psa->cache);
    }
    fclose(f);
    SHUT_LOGD("Loaded a PSA with %d Precompiled Programs\n", psa->size);
}

void fpe_writePSA()
{
    if(!psa || !psa_name)
        return;
    if(!psa->dirty)
        return; // no need
    FILE *f = fopen(psa_name, "wb");
    if(!f)
        return;
    if(fwrite(PSA_SIGN, sizeof(PSA_SIGN), 1, f)!=1) {
        fclose(f);
        return; //to short
    }
    int version = CACHE_VERSION;
    if(fwrite(&version, sizeof(version), 1, f)!=1) {
        fclose(f);
        return;
    }
    int sz_fpe = sizeof(fpe_state_t);
    if(fwrite(&sz_fpe, sizeof(sz_fpe), 1, f)!=1) {
        fclose(f);
        return;
    }
    if(fwrite(&psa->size, sizeof(psa->size), 1, f)!=1) {
        fclose(f);
        return;
    }
    psa_t *p;
    kh_foreach_value(psa->cache, p, 
        if(fwrite(&p->state, sizeof(p->state), 1, f)!=1) {
            fclose(f);
            return;
        }
        if(fwrite(&p->format, sizeof(p->format), 1, f)!=1) {
            fclose(f);
            return;
        }
        if(fwrite(&p->size, sizeof(p->size), 1, f)!=1) {
            fclose(f);
            return;
        }
        if(fwrite(p->prog, p->size, 1, f)!=1) {
            fclose(f);
            return;
        }
    );
    fclose(f);
    SHUT_LOGD("Saved a PSA with %d Precompiled Programs\n", psa->size);
}

void fpe_InitPSA(const char* name)
{
    if(psa)
        return; // already inited
    psa = (gl4es_psa_t*)calloc(1, sizeof(gl4es_psa_t));
    psa->cache = kh_init(psalist);
    psa_name = strdup(name);
}

void fpe_FreePSA()
{
    if(!psa)
        return; // nothing to init
    
    psa_t *m;
    kh_foreach_value(psa->cache, m, 
        free(m->prog);
        free(m);
    )
    kh_destroy(psalist, psa->cache);

    free(psa);
    psa = NULL;
    free(psa_name);
    psa_name = NULL;
}

int fpe_GetProgramPSA(GLuint program, fpe_state_t* state)
{
    if(!psa)
        return 0;
    // if state contains custom vertex of fragment shader, then ignore
    if(state->vertex_prg_enable || state->fragment_prg_enable)
        return 0;
    khint_t k = kh_get(psalist, psa->cache, state);
    if(k==kh_end(psa->cache))
        return 0; // not here
    psa_t *p = kh_value(psa->cache, k);
    // try to load...
    return gl4es_useProgramBinary(program, p->size, p->format, p->prog);
}

void fpe_AddProgramPSA(GLuint program, fpe_state_t* state)
{
    if(!psa)
        return;
    // if state contains custom vertex of fragment shader, then ignore
    if(state->vertex_prg_enable || state->fragment_prg_enable)
        return;
    psa->dirty = 1;
    psa_t *p = (psa_t*)calloc(1, sizeof(psa_t));
    memcpy(&p->state, state, sizeof(p->state));

    int l = gl4es_getProgramBinary(program, &p->size, &p->format, &p->prog);
    if(l==0) { // there was an error...
        free(p->prog);
        free(p);
        return;
    }
    // add program
    int ret;
    khint_t k = kh_put(psalist, psa->cache, &p->state, &ret);
    if(!ret) {
        psa_t *p2 = kh_value(psa->cache, k);
        free(p2->prog);
        p2->prog = NULL;
        free(p2);
    }
    kh_value(psa->cache, k) = p;
    // all done
    psa->size = kh_size(psa->cache);
}