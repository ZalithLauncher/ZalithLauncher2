//
// Created by Vera-Firefly on 28.01.2025.
//
#ifndef BR_LOADER_H
#define BR_LOADER_H

void* load_symbol(void* handle, const char* symbol_name);
void* OSMGetProcAddress(void* handle, const char* symbol_name);
void* GLGetProcAddress(void* handle, const char* symbol_name);

#endif //BR_LOADER_H