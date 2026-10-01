#include <stdio.h>
#include <stdbool.h>
#include <string.h>

#define STUBFUNC(name) \
static bool trigger_##name = false; \
static void stub_##name() { \
    if(trigger_##name) return; \
    trigger_##name = true; \
    printf("Stub: "#name"\n"); \
}
#include "stubdefs.h"
#undef STUBFUNC

void* resolve_stub(const char* procname) {
#define STUBFUNC(name) \
    if(!strcmp(procname, #name)) {                                \
        return stub_##name;     \
    }
#include "stubdefs.h"
#undef STUBFUNC
    return NULL;
}