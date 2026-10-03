#ifndef _GL4ES_DEBUG_H_
#define _GL4ES_DEBUG_H_

#include "gles.h"

const char* PrintEnum(GLenum what);

const char* PrintEGLError(int onlyerror);

void CheckGLError(int fwd);

#endif // _GL4ES_DEBUG_H_
