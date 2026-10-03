
#ifndef _GL4ESINCLUDE_INIT_H_
#define _GL4ESINCLUDE_INIT_H_

#ifndef APIENTRY_GL4ES
# if defined(_WIN32) && !defined(_WIN32_WCE) && !defined(__SCITECH_SNAP__)
#  define APIENTRY_GL4ES __stdcall
# else
#  define APIENTRY_GL4ES
# endif
#endif

#ifdef __cplusplus
extern "C" {
#endif

// set driver GetProcAddress implementation. required for hardext detection with NOEGL or when loader is disabled
void set_getprocaddress(void *(APIENTRY_GL4ES *new_proc_address)(const char *));
// reguired with NOEGL
void set_getmainfbsize(void (APIENTRY_GL4ES *new_getMainFBSize)(int* width, int* height));
// do this before any GL calls if init constructors are disabled.
void initialize_gl4es(void);
// do this to uninitialize GL4ES if init constructors are disabled.
void close_gl4es(void);
// wrapped GetProcAddress
void* APIENTRY_GL4ES gl4es_GetProcAddress(const char *name);

#ifdef __cplusplus
}
#endif

#endif
