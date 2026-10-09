/**
 * Created by: artDev
 * Copyright (c) 2025 artDev, SerpentSpirale, CADIndie.
 * For use under LGPL-3.0
 */
#include "proc.h"
#include "egl.h"
#include <stdbool.h>
#include <stdio.h>
#include "swizzle.h"
void buffer_copier_init(context_t* context) {
    framebuffer_copier_t* copier = &context->framebuffer_copier;
    while(es3_functions.glGetError() != 0) {}
    es3_functions.glGenTextures(1, &copier->temp_texture);
    es3_functions.glGenFramebuffers(1, &copier->tempfb);
    es3_functions.glGenFramebuffers(1, &copier->destfb);
    es3_functions.glBindTexture(GL_TEXTURE_2D, copier->temp_texture);
    es3_functions.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    es3_functions.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    es3_functions.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    es3_functions.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    GLenum error = es3_functions.glGetError();
    if(error != 0) {
        printf("LTW: error while initializing buffer-copier: %x\n", error);
        return;
    }
    copier->ready = true;
}

static void buffer_copier_store(GLint x, GLint y, GLsizei w, GLsizei h) {
    framebuffer_copier_t* copier = &current_context->framebuffer_copier;
    if(!copier->ready) return;
    GLint current_texbind;
    es3_functions.glGetIntegerv(GL_TEXTURE_BINDING_2D, &current_texbind);
    es3_functions.glBindTexture(GL_TEXTURE_2D, copier->temp_texture);
    es3_functions.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, w, h, 0, GL_DEPTH_COMPONENT, GL_FLOAT, NULL);
    es3_functions.glBindTexture(GL_TEXTURE_2D, current_texbind);
    es3_functions.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, copier->tempfb);
    es3_functions.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, copier->temp_texture, 0);
    es3_functions.glBlitFramebuffer(x, y, x+w, y+h, 0, 0, w, h, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
    es3_functions.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, current_context->draw_framebuffer);
}

static void buffer_copier_release(GLenum target, GLint level, GLint x, GLint y, GLsizei w, GLsizei h) {
    framebuffer_copier_t* copier = &current_context->framebuffer_copier;
    if(!copier->ready) return;
    GLint current_texbind;
    GLenum target_query = get_textarget_query_param(target);
    if(target_query == GL_NONE) return;
    es3_functions.glGetIntegerv(target_query, &current_texbind);
    es3_functions.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, copier->destfb);
    es3_functions.glBindFramebuffer(GL_READ_FRAMEBUFFER, copier->tempfb);
    es3_functions.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, target, current_texbind, level);
    es3_functions.glBlitFramebuffer(0, 0, w, h, x, y, x+w, y+h, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
    es3_functions.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, current_context->draw_framebuffer);
    es3_functions.glBindFramebuffer(GL_READ_FRAMEBUFFER, current_context->read_framebuffer);
}

void glGetTexImage( 	GLenum target,
                       GLint level,
                       GLenum format,
                       GLenum type,
                       void * pixels) {
    if(!current_context) return;
    if(!current_context->es31) goto unsupported_esver;
    if(format != GL_RGBA && format != GL_RGBA_INTEGER && type != GL_UNSIGNED_BYTE && type != GL_UNSIGNED_INT && type != GL_INT && type != GL_FLOAT) goto unsupported;
    framebuffer_copier_t* copier = &current_context->framebuffer_copier;
    GLint texture;
    es3_functions.glGetIntegerv(get_textarget_query_param(target), &texture);
    es3_functions.glBindFramebuffer(GL_READ_FRAMEBUFFER, copier->tempfb);
    es3_functions.glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, target, texture, level);
    GLint w, h;
    es3_functions.glGetTexLevelParameteriv(target, level, GL_TEXTURE_WIDTH, &w);
    es3_functions.glGetTexLevelParameteriv(target, level, GL_TEXTURE_HEIGHT, &h);
    es3_functions.glReadPixels(0, 0, w, h, format, type, pixels);
    es3_functions.glFramebufferRenderbuffer(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, 0);
    return;
    unsupported_esver:
    printf("LTW: glGetTexImage only supported on OpenGL ES 3.1");
    return;
    unsupported:
    printf("LTW: unsupported parameters for glGetTexImage");
}

void glReadPixels(GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, GLvoid * data) {
    if(!current_context) return;
    if(format == GL_DEPTH_COMPONENT) {
        framebuffer_copier_t* copier = &current_context->framebuffer_copier;
        copier->depthData = data;
        copier->depthWidth = width;
        copier->depthHeight = height;
        buffer_copier_store(x, y, width, height);
        return;
    }
    es3_functions.glReadPixels(x, y, width, height, format, type, data);
}

void glTexSubImage2D(GLenum target,
                     GLint level,
                     GLint xoffset,
                     GLint yoffset,
                     GLsizei width,
                     GLsizei height,
                     GLenum format,
                     GLenum type,
                     const void * data) {
    if(!current_context) return;
    swizzle_process_upload(target, &format, &type);
    if(format == GL_DEPTH_COMPONENT) {
        framebuffer_copier_t* copier = &current_context->framebuffer_copier;
        if(width == copier->depthWidth && height == copier->depthHeight && copier->depthData == data) {
            buffer_copier_release(target, level, xoffset, yoffset, width, height);
            return;
        }
    }
    es3_functions.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, data);
}

void texture_blit_framebuffer(GLenum target,
                              GLint level,
                              GLint xoffset,
                              GLint yoffset,
                              GLint x,
                              GLint y,
                              GLsizei width,
                              GLsizei height,
                              bool depth) {
    framebuffer_copier_t* copier = &current_context->framebuffer_copier;
    if(!copier->ready) return;

    GLenum fb_attachment;
    GLbitfield fb_blit_bit;
    if(depth) {
        fb_attachment = GL_DEPTH_ATTACHMENT;
        fb_blit_bit = GL_DEPTH_BUFFER_BIT;
    }else {
        fb_attachment = GL_COLOR_ATTACHMENT0;
        fb_blit_bit = GL_COLOR_BUFFER_BIT;
    }

    GLint texture;
    es3_functions.glGetIntegerv(get_textarget_query_param(target), &texture);
    es3_functions.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, copier->destfb);
    es3_functions.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, fb_attachment, target, texture, level);
    es3_functions.glBlitFramebuffer(x, y, width+x, height+y, xoffset, yoffset, width+xoffset, height+yoffset, fb_blit_bit, GL_NEAREST);
    es3_functions.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, current_context->draw_framebuffer);
}

void glCopyTexSubImage2D(GLenum target,
                         GLint level,
                         GLint xoffset,
                         GLint yoffset,
                         GLint x,
                         GLint y,
                         GLsizei width,
                         GLsizei height) {
    if(current_context->es31) {
        GLint depthtype;
        es3_functions.glGetTexLevelParameteriv(target, level, GL_TEXTURE_DEPTH_TYPE, &depthtype);
        if(depthtype != GL_NONE) {
            texture_blit_framebuffer(target, level, xoffset, yoffset, x, y, width, height, true);
        }else {
            texture_blit_framebuffer(target, level, xoffset, yoffset, x, y, width, height, false);
        }
    } else {
        es3_functions.glGetError();
        es3_functions.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
        // The QCOM driver is a pathological liar and emits wrong GL errors. Abuse this to decide when we actually need to at least try copying depth.
        if(es3_functions.glGetError() == GL_INVALID_OPERATION) {
            texture_blit_framebuffer(target, level, xoffset, yoffset, x, y, width, height, true);
        }
    }

}
