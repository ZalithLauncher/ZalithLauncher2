//
// Created by serpentspirale on 17/06/23.
//



#ifndef GL4ES_C_WRAPPER_H
#define GL4ES_C_WRAPPER_H

#include "GL/gl.h"

#ifdef __cplusplus
extern "C" {
#endif

char *optimize_shader(char *source, GLenum type, int vGLSLVersion, int vTargetGLSLVersion );

#ifdef __cplusplus
} /* extern C */
#endif


#endif //GL4ES_C_WRAPPER_H
