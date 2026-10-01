/**
 * Created by: artDev
 * Copyright (c) 2025 artDev, SerpentSpirale, CADIndie.
 * For use under LGPL-3.0
 */

#include "proc.h"
#include "egl.h"
#include <string.h>
#include <stdio.h>

static framebuffer_t* get_framebuffer(GLenum target) {
    GLuint fb;
    switch (target) {
        case GL_FRAMEBUFFER:
        case GL_DRAW_FRAMEBUFFER: fb = current_context->draw_framebuffer; break;
        case GL_READ_FRAMEBUFFER: fb = current_context->read_framebuffer; break;
    }
    return unordered_map_get(current_context->framebuffer_map, (void*)fb);
}

static GLuint get_attachment_idx(GLenum attachment) {
    if(attachment == GL_DEPTH_ATTACHMENT ||
        attachment == GL_STENCIL_ATTACHMENT ||
        attachment == GL_DEPTH_STENCIL_ATTACHMENT ||
        attachment == GL_NONE) return -1;
    GLuint idx = attachment - GL_COLOR_ATTACHMENT0;
    if(idx >= current_context->max_drawbuffers) return -1;
    return idx;
}

static GLenum map_attachment(framebuffer_t* framebuffer, GLenum attachment) {
    for(GLsizei i = 0; i < framebuffer->nbuffers; i++) {
        if(framebuffer->virt_drawbuffers[i] == attachment) {
            return i + GL_COLOR_ATTACHMENT0;
        }
    }
    return GL_NONE;
}

void rebind_framebuffer(GLenum target, framebuffer_t *framebuffer, GLenum virt_attachment) {
    GLuint virt_index = get_attachment_idx(virt_attachment);
    if(virt_index == -1) return;
    GLenum phys_attachment = map_attachment(framebuffer, virt_attachment);
    if(phys_attachment == GL_NONE) return;
    switch (framebuffer->color_targets[virt_index]) {
        case GL_NONE:
            es3_functions.glFramebufferRenderbuffer(target, phys_attachment, GL_RENDERBUFFER, 0);
            break;
        case GL_RENDERBUFFER:
            es3_functions.glFramebufferRenderbuffer(target, phys_attachment, GL_RENDERBUFFER, framebuffer->color_objects[virt_index]);
            break;
        case GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER:
            es3_functions.glFramebufferTextureLayer(target, phys_attachment,
                                                    framebuffer->color_objects[virt_index],
                                                    framebuffer->color_levels[virt_index],
                                                    framebuffer->color_layers[virt_index]);
            break;
        default:
            es3_functions.glFramebufferTexture2D(target, phys_attachment,
                                                 framebuffer->color_targets[virt_index],
                                                 framebuffer->color_objects[virt_index],
                                                 framebuffer->color_levels[virt_index]);
            break;
    }
}

void glClearBufferiv( 	GLenum buffer,
                         GLint drawBuffer,
                         const GLint * value) {
    framebuffer_t *framebuffer = get_framebuffer(GL_DRAW_FRAMEBUFFER);
    if(framebuffer && buffer == GL_COLOR) {
        GLenum attachment = map_attachment(framebuffer, GL_COLOR_ATTACHMENT0 + drawBuffer);
        drawBuffer = attachment - GL_COLOR_ATTACHMENT0;
    }
    es3_functions.glClearBufferiv(buffer, drawBuffer, value);
}

void glClearBufferuiv( 	GLenum buffer,
                          GLint drawBuffer,
                          const GLuint * value) {
    framebuffer_t *framebuffer = get_framebuffer(GL_DRAW_FRAMEBUFFER);
    if(framebuffer && buffer == GL_COLOR) {
        GLenum attachment = map_attachment(framebuffer, GL_COLOR_ATTACHMENT0 + drawBuffer);
        drawBuffer = attachment - GL_COLOR_ATTACHMENT0;
    }
    es3_functions.glClearBufferuiv(buffer, drawBuffer, value);
}

void glClearBufferfv( 	GLenum buffer,
                         GLint drawBuffer,
                         const GLfloat * value) {
    framebuffer_t *framebuffer = get_framebuffer(GL_DRAW_FRAMEBUFFER);
    if(framebuffer && buffer == GL_COLOR) {
        GLenum attachment = map_attachment(framebuffer, GL_COLOR_ATTACHMENT0 + drawBuffer);
        drawBuffer = attachment - GL_COLOR_ATTACHMENT0;
    }
    es3_functions.glClearBufferfv(buffer, drawBuffer, value);
}

void glDrawBuffers(GLsizei n, const GLenum* buffers) {
    if(!current_context) return;
    framebuffer_t *framebuffer = get_framebuffer(GL_DRAW_FRAMEBUFFER);
    if(!framebuffer) {
        es3_functions.glDrawBuffers(n, buffers);
        return;
    }
    framebuffer->nbuffers = n;
    memcpy(framebuffer->virt_drawbuffers, buffers, n * sizeof(GLenum));
    GLenum phys_drawbuffers[n];
    for(GLsizei i = 0; i < n; i++) {
        GLenum buffer = buffers[i];
        rebind_framebuffer(GL_DRAW_FRAMEBUFFER, framebuffer, buffer);
        if(buffer != GL_NONE) phys_drawbuffers[i] = GL_COLOR_ATTACHMENT0+i;
        else phys_drawbuffers[i] = GL_NONE;
    }
    es3_functions.glDrawBuffers(n, phys_drawbuffers);
}

void glDrawBuffer(GLenum buffer) {
    glDrawBuffers(1, &buffer);
}

GLenum glCheckFramebufferStatus( 	GLenum target) {
    if(!current_context) return GL_FRAMEBUFFER_UNDEFINED;
    GLenum framebuffer_status = es3_functions.glCheckFramebufferStatus(target);
    if(framebuffer_status == GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
        framebuffer_t *framebuffer = get_framebuffer(target);
        for(GLint i = 0; i < MAX_FBTARGETS; i++) {
            // At least one color target found, means we just optimized out all color targets on the physical device
            // This will come back to normal after a call to `glDrawBuffers` if only the secondary buffers are in use.
            if(framebuffer->color_targets[i] != GL_NONE || framebuffer->color_objects[i] != 0) return GL_FRAMEBUFFER_COMPLETE;
        }
        return GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
    }
    return framebuffer_status;
}

void glFramebufferTexture2D( 	GLenum target,
                                GLenum attachment,
                                GLenum textarget,
                                GLuint texture,
                                GLint level) {
    if(!current_context) return;
    framebuffer_t *framebuffer = get_framebuffer(target);
    GLuint attachment_idx = get_attachment_idx(attachment);
    if(!framebuffer || attachment_idx == -1) {
        es3_functions.glFramebufferTexture2D(target, attachment, textarget, texture, level);
        return;
    }
    if(texture == 0) {
        framebuffer->color_targets[attachment_idx] = GL_NONE;
        goto rebind;
    }
    framebuffer->color_targets[attachment_idx] = textarget;
    framebuffer->color_objects[attachment_idx] = texture;
    framebuffer->color_levels[attachment_idx] = level;
    rebind:
    rebind_framebuffer(target, framebuffer, attachment);
}

void glFramebufferTextureLayer( 	GLenum target,
                                GLenum attachment,
                                GLuint texture,
                                GLint level, GLint layer) {
    if(!current_context) return;
    framebuffer_t *framebuffer = get_framebuffer(target);
    GLuint attachment_idx = get_attachment_idx(attachment);
    if(!framebuffer || attachment_idx == -1) {
        es3_functions.glFramebufferTextureLayer(target, attachment, texture, level, layer);
        return;
    }
    if(texture == 0) {
        framebuffer->color_targets[attachment_idx] = GL_NONE;
        goto rebind;
    }
    // I know that this is not a real target, we just need something to put there for the if conditions
    framebuffer->color_targets[attachment_idx] = GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER;
    framebuffer->color_objects[attachment_idx] = texture;
    framebuffer->color_levels[attachment_idx] = level;
    framebuffer->color_layers[attachment_idx] = layer;
    rebind:
    rebind_framebuffer(target, framebuffer, attachment);
}

void glFramebufferRenderbuffer( 	GLenum target,
                                   GLenum attachment,
                                   GLenum renderbuffertarget,
                                   GLuint renderbuffer) {
    if(!current_context) return;
    framebuffer_t *framebuffer = get_framebuffer(target);
    GLuint attachment_idx = get_attachment_idx(attachment);
    if(!framebuffer || attachment_idx == -1) {
        es3_functions.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
        return;
    }
    if(renderbuffer == 0) {
        framebuffer->color_targets[attachment_idx] = GL_NONE;
        goto rebind;
    }
    framebuffer->color_targets[attachment_idx] = renderbuffertarget;
    framebuffer->color_objects[attachment_idx] = renderbuffer;
    rebind:
    rebind_framebuffer(target, framebuffer, attachment);
}


void glGetFramebufferAttachmentParameteriv(GLenum target,
                                           GLenum attachment,
                                           GLenum pname,
                                           GLint *params) {
    if(!current_context) return;
    framebuffer_t *framebuffer = get_framebuffer(target);
    GLuint attachment_idx = get_attachment_idx(attachment);
    if(!framebuffer || attachment_idx == -1) {
        es3_functions.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
        return;
    }
    GLenum fb_target = framebuffer->color_targets[attachment_idx];
    switch (pname) {
        case GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE:
            switch (fb_target) {
                case GL_NONE: *params = GL_NONE; break;
                case GL_RENDERBUFFER: *params = GL_RENDERBUFFER; break;
                default:
                    *params = GL_TEXTURE;
                    break;
            }
            break;
        case GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME:
            if(fb_target == GL_NONE) { *params = 0; break; }
            *params = (GLint)framebuffer->color_objects[attachment_idx];
            break;
        case GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL:
            if(fb_target == GL_NONE || fb_target == GL_RENDERBUFFER) { *params = 0; break; }
            *params = framebuffer->color_levels[attachment_idx];
            break;
        case GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER:
            if(fb_target != GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER) { *params = 0; break; }
            *params = framebuffer->color_layers[attachment_idx];
            break;
        case GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE:
            // Check if the cube map face is within the range of possible cube map face targets. Check GLES3/gl3.h to see why.
            if(fb_target < GL_TEXTURE_CUBE_MAP_POSITIVE_X || fb_target > GL_TEXTURE_CUBE_MAP_NEGATIVE_Z) { *params = 0; break; }
            // The doc does not spacify how this should be sent, so ill just send the enum
            *params = fb_target;
            break;
        case GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE:
        case GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE:
        case GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE:
        case GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE:
        case GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE:
        case GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING:
            printf("LTW: parameter %x is not implemented yet\n", pname);
            *params = 0;
            break;
        default:
            printf("LTW: parameter %x is not supported\n", pname);
            *params = 0;
            break;
    }
}

void glGenFramebuffers(GLsizei n, GLuint* framebuffers) {
    if(!current_context) return;
    es3_functions.glGenFramebuffers(n, framebuffers);
    framebuffer_t* fb;
    for(GLsizei i = 0; i < n; i++) {
        fb = calloc(1, sizeof(framebuffer_t));
        fb->nbuffers = 1;
        fb->virt_drawbuffers[0] = GL_COLOR_ATTACHMENT0;
        unordered_map_put(current_context->framebuffer_map, (void*)framebuffers[i], fb);
    }
}

void glDeleteFramebuffers(GLsizei n, const GLuint* framebuffers) {
    if(!current_context) return;
    es3_functions.glDeleteFramebuffers(n, framebuffers);
    framebuffer_t* fb;
    for(GLsizei i = 0; i < n; i++) {
        fb = unordered_map_remove(current_context->framebuffer_map, (void*)framebuffers[i]);
        if(fb == NULL) continue;
        free(fb);
    }
}

void glBindFramebuffer(GLenum target, GLuint framebuffer) {
    if(!current_context) return;
    es3_functions.glBindFramebuffer(target, framebuffer);
    switch (target) {
        case GL_FRAMEBUFFER:
            current_context->read_framebuffer = current_context->draw_framebuffer = framebuffer;
            break;
        case GL_READ_FRAMEBUFFER:
            current_context->read_framebuffer = framebuffer;
            break;
        case GL_DRAW_FRAMEBUFFER:
            current_context->draw_framebuffer = framebuffer;
            break;
    }
}
