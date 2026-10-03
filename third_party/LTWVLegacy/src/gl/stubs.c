#include "gl4es.h"
#include "attributes.h"

#define STUB errorShim(GL_INVALID_VALUE);

NonAliasExportDecl(void,glClampColorARB,(GLenum target, GLenum clamp)){STUB}
