/**
 * Created by: artDev
 * Copyright (c) 2025 artDev, SerpentSpirale, CADIndie.
 * For use under LGPL-3.0
 */

#ifndef POJAVLAUNCHER_PROC_H
#define POJAVLAUNCHER_PROC_H

#include <GLES3/gl32.h>
#include <GLES2/gl2ext.h>
#include <threads.h>

typedef void (*eglMustCastToProperFunctionPointerType)(void);

typedef struct {
#define GLESFUNC(name, type) type name;
#include "es3_functions.h"
#include "es3_extended.h"
#undef GLESFUNC
} es3_functions_t;

extern eglMustCastToProperFunctionPointerType (*host_eglGetProcAddress)(const char *procname);
extern es3_functions_t es3_functions;

#endif //POJAVLAUNCHER_PROC_H
