//
// Created by whbex on 25.11.2025.
//

#include <GL/gl.h>
#include <GL/glext.h>
#include "egl.h"
#include "proc.h"

#define CTX_CHECK() if (!current_context) return;

// Minecraft uses these only for timer queries
void glGetQueryObjecti64v(GLuint id, GLenum pname, int64_t* params){
    CTX_CHECK();
    // May be not needed, added just in case
    if(!current_context->timer_query){
        *params = 1;
        return;
    }
   es3_functions.glGetQueryObjecti64vEXT(id, pname, params);
}

void glGetQueryObjectui64v(GLuint id, GLenum pname, uint64_t* params){
    CTX_CHECK();
    if(!current_context->timer_query){
        *params = 1;
        return;
    }
    es3_functions.glGetQueryObjectui64vEXT(id, pname, params);
}
void glQueryCounter(GLuint id, GLenum target){
    if(!current_context || !current_context->timer_query)
        return;
    es3_functions.glQueryCounterEXT(id, target);
}

// Moved from main.c
void glGetQueryObjectiv( 	GLuint id,
                            GLenum name,
                            GLint * params) {
    CTX_CHECK();
    // This is not recommended but i don't care
    es3_functions.glGetQueryObjectuiv(id, name, (GLuint*)params);
}