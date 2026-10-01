/**
 * \file errors.c
 * Mesa debugging and error handling functions.
 */

/*
 * Mesa 3-D graphics library
 *
 * Copyright (C) 1999-2007  Brian Paul   All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */


#include <stdarg.h>
#include <stdio.h>
#include "errors.h"
#include "enums.h"

#include "context.h"
#include "debug_output.h"
#include "../../util/log.h"


/**
 * When a new type of error is recorded, print a message describing
 * previous errors which were accumulated.
 */
static void
flush_delayed_errors( struct gl_context *ctx )
{
    char s[MAX_DEBUG_MESSAGE_LENGTH];
    /*
    if (ctx->ErrorDebugCount) {
        snprintf(s, MAX_DEBUG_MESSAGE_LENGTH, "%d similar %s errors",
                 ctx->ErrorDebugCount,
                 _mesa_enum_to_string(ctx->ErrorValue));

        output_if_debug(MESA_LOG_ERROR, s);

        ctx->ErrorDebugCount = 0;
    }
    */
}

static GLboolean
should_output(struct gl_context *ctx, GLenum error, const char *fmtString)
{
    return GL_TRUE;
    /*
    static GLint debug = -1;

    if (debug == -1) {
        const char *debugEnv = getenv("MESA_DEBUG");

#ifndef NDEBUG
        if (debugEnv && strstr(debugEnv, "silent"))
            debug = GL_FALSE;
        else
            debug = GL_TRUE;
#else
        if (debugEnv)
         debug = GL_TRUE;
      else
         debug = GL_FALSE;
#endif
    }

    if (debug) {
        if (ctx->ErrorValue != error ||
            ctx->ErrorDebugFmtString != fmtString) {
            flush_delayed_errors( ctx );
            ctx->ErrorDebugFmtString = fmtString;
            ctx->ErrorDebugCount = 0;
            return GL_TRUE;
        }
        ctx->ErrorDebugCount++;
    }
    return GL_FALSE;
     */
}


void
_mesa_gl_vdebugf(struct gl_context *ctx,
                 GLuint *id,
                 enum mesa_debug_source source,
                 enum mesa_debug_type type,
                 enum mesa_debug_severity severity,
                 const char *fmtString,
                 va_list args)
{
    char s[MAX_DEBUG_MESSAGE_LENGTH];
    int len;

    _mesa_debug_get_id(id);

    len = vsnprintf(s, MAX_DEBUG_MESSAGE_LENGTH, fmtString, args);
    if (len >= MAX_DEBUG_MESSAGE_LENGTH)
        /* message was truncated */
        len = MAX_DEBUG_MESSAGE_LENGTH - 1;

    _mesa_log_msg(ctx, source, type, *id, severity, len, s);
}


void
_mesa_gl_debugf(struct gl_context *ctx,
                GLuint *id,
                enum mesa_debug_source source,
                enum mesa_debug_type type,
                enum mesa_debug_severity severity,
                const char *fmtString, ...)
{
    va_list args;
    va_start(args, fmtString);
    _mesa_gl_vdebugf(ctx, id, source, type, severity, fmtString, args);
    va_end(args);
}


void
_mesa_error_no_memory(const char *caller)
{
    //GET_CURRENT_CONTEXT(ctx);
    //_mesa_error(ctx, GL_OUT_OF_MEMORY, "out of memory in %s", caller);
}

/**
 * Report an internal implementation problem.
 * Prints the message to stderr via fprintf().
 *
 * \param ctx GL context.
 * \param fmtString problem description string.
 */
void
_mesa_problem( const struct gl_context *ctx, const char *fmtString, ... )
{
    va_list args;
    char str[MAX_DEBUG_MESSAGE_LENGTH];
    static int numCalls = 0;

    (void) ctx;

    if (numCalls < 50) {
        numCalls++;

        va_start( args, fmtString );
        vsnprintf( str, MAX_DEBUG_MESSAGE_LENGTH, fmtString, args );
        va_end( args );
        fprintf(stderr, "Mesa "  " implementation error: %s\n",
                str);
        fprintf(stderr, "Please report at " "\n");
    }
}

/**
 * Report debug information from the shader compiler via GL_ARB_debug_output.
 *
 * \param ctx GL context.
 * \param type The namespace to which this message belongs.
 * \param id The message ID within the given namespace.
 * \param msg The message to output. Must be null-terminated.
 */
void
_mesa_shader_debug(struct gl_context *ctx, GLenum type, GLuint *id,
                   const char *msg)
{
    enum mesa_debug_source source = MESA_DEBUG_SOURCE_SHADER_COMPILER;
    enum mesa_debug_severity severity = MESA_DEBUG_SEVERITY_HIGH;
    int len;

    _mesa_debug_get_id(id);

    len = strlen(msg);

    /* Truncate the message if necessary. */
    if (len >= MAX_DEBUG_MESSAGE_LENGTH)
        len = MAX_DEBUG_MESSAGE_LENGTH - 1;

    _mesa_log_msg(ctx, source, type, *id, severity, len, msg);
}
