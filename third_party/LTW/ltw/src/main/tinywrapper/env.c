//
// Created by maks on 12.07.2025.
//
#include "env.h"
#include <stdlib.h>
#include "libraryinternal.h"

INTERNAL bool env_istrue_d(const char* name, bool _default) {
    const char* env = getenv(name);
    if(env == NULL) return _default;
    return *env == '1';
}

INTERNAL bool env_istrue(const char* name) {
    const char* env = getenv(name);
    return env != NULL && *env == '1';
}