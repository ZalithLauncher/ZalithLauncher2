#ifndef _GL4ES_ARBCONVERTER_H_
#define _GL4ES_ARBCONVERTER_H_

#include <stdint.h>

char* gl4es_convertARB(const char* const code, int vertex, char **error_msg, int *error_ptr);

#endif // _GL4ES_ARBCONVERTER_H_
