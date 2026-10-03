#ifndef _GL_LOOKUP_H_
#define _GL_LOOKUP_H_

#include "attributes.h"

#ifdef DEBUG
#define MAP(func_name, func) \
    if (strcmp(name, func_name) == 0) {printf("%p (%s)\n", (void*)func, #func) ;return (void *)func;}
#else
#define MAP(func_name, func) \
    if (strcmp(name, func_name) == 0) return (void *)func;
#endif

#define EX(func_name) MAP(#func_name, func_name)

#define ARB(func_name) MAP(#func_name "ARB", func_name)

#define EXT(func_name) MAP(#func_name "EXT", func_name)

#define _EX(func_name) MAP(#func_name, gl4es_ ## func_name)

#define _ARB(func_name) MAP(#func_name "ARB", gl4es_ ## func_name)

#define _EXT(func_name) MAP(#func_name "EXT", gl4es_ ## func_name)

#ifndef STUB_FCT
#error STUB_FCT is not defined
#endif

#ifdef DEBUG
#define STUB(func_name)                       \
    if (strcmp(name, #func_name) == 0) {      \
        printf("=> STUB\n");                  \
        if(!globals4es.silentstub) LOGD("GL4ES stub: %s\n", #func_name); \
        return (void *)STUB_FCT;              \
    }
#else
#define STUB(func_name)                       \
    if (strcmp(name, #func_name) == 0) {      \
        if(!globals4es.silentstub) LOGD("GL4ES stub: %s\n", #func_name); \
        return (void *)STUB_FCT;              \
    }
#endif

NonAliasExportDecl(void*,gl4es_GetProcAddress,(const char *name));

#endif //_GL_LOOKUP_H_