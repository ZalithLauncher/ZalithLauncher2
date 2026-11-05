//
// Created by Vera-Firefly on 28.01.2025.
//
#include <string.h>
#include <stdio.h>
#include <dlfcn.h>
#include "br_loader.h"
#include "egl_loader.h"
#include "osmesa_loader.h"

__eglMustCastToProperFunctionPointerType (*eglGetProcAddress_p) (const char *procname);
void* (*OSMesaGetProcAddress_p)(const char* funcName);

void* load_symbol(void* handle, const char* symbol_name) {
    void* symbol = dlsym(handle, symbol_name);
    if (!symbol)
        fprintf(stderr, "Error[Load Symbol]: Failed to load symbol '%s': %s\n", symbol_name, dlerror());

    return symbol;
}

void* OSMGetProcAddress(void* handle, const char* symbol_name) {
    OSMesaGetProcAddress_p = load_symbol(handle, "OSMesaGetProcAddress");
    if (OSMesaGetProcAddress_p)
    {
        void* symbol = OSMesaGetProcAddress_p(symbol_name);
        if (symbol)
        {
            return symbol;
        }
        fprintf(stderr, "Error[OSM Loader]: 'OSMesaGetProcAddress' could not find symbol '%s'.\n", symbol_name);
    }
    return load_symbol(handle, symbol_name);
}

void* GLGetProcAddress(void* handle, const char* symbol_name) {
    eglGetProcAddress_p = load_symbol(handle, "eglGetProcAddress");
    if(eglGetProcAddress_p)
    {
        void* symbol = (void*) eglGetProcAddress_p(symbol_name);
        if (symbol)
        {
            return symbol;
        }
        fprintf(stderr, "Error[GL Loader]: 'eglGetProcAddress' could not find symbol '%s'.\n", symbol_name);
    }
    return load_symbol(handle, symbol_name);
}
