/**
 * Created by: artDev
 * Copyright (c) 2025 artDev, SerpentSpirale, CADIndie.
 * For use under LGPL-3.0
 */

#include "proc.h"
#include "egl.h"
#include <string.h>
#include "libraryinternal.h"
//#include <GL/glext.h>

#define GL_TEXTURE_SWIZZLE_RGBA 0x8E46

static void swizzle_process_bgra(GLenum* swizzle) {
    GLenum red_src = swizzle[0];
    GLenum blue_src = swizzle[2];
    swizzle[0] = blue_src;
    swizzle[2] = red_src;
}

static void swizzle_process_endianness(GLenum* swizzle) {
    GLenum orig_swizzle[4];
    memcpy(orig_swizzle, swizzle, 4 * sizeof(GLenum));
    swizzle[0] = orig_swizzle[3];
    swizzle[1] = orig_swizzle[2];
    swizzle[2] = orig_swizzle[1];
    swizzle[3] = orig_swizzle[0];
}

static texture_swizzle_track_t* get_swizzle_track(GLenum target) {
    GLint texture;
    GLenum getter = get_textarget_query_param(target);
    if(getter == 0) return NULL;
    es3_functions.glGetIntegerv(getter, &texture);
    texture_swizzle_track_t* track = unordered_map_get(current_context->texture_swztrack_map, (void*)texture);
    if(track == NULL) {
        track = malloc(sizeof(texture_swizzle_track_t));
        es3_functions.glGetTexParameteriv(target, GL_TEXTURE_SWIZZLE_R, (GLint*)&track->original_swizzle[0]);
        es3_functions.glGetTexParameteriv(target, GL_TEXTURE_SWIZZLE_G, (GLint*)&track->original_swizzle[1]);
        es3_functions.glGetTexParameteriv(target, GL_TEXTURE_SWIZZLE_B, (GLint*)&track->original_swizzle[2]);
        es3_functions.glGetTexParameteriv(target, GL_TEXTURE_SWIZZLE_A, (GLint*)&track->original_swizzle[3]);
        unordered_map_put(current_context->texture_swztrack_map, (void*)texture, track);
    }
    return track;
}

static void apply_swizzles(GLenum target, texture_swizzle_track_t* track) {
    GLenum new_swizzle[4];
    memcpy(new_swizzle, track->original_swizzle, 4 * sizeof(GLenum));
    if(track->goofy_byte_order) swizzle_process_endianness(new_swizzle);
    if(track->upload_bgra) swizzle_process_bgra(new_swizzle);
    es3_functions.glTexParameteri(target, GL_TEXTURE_SWIZZLE_R, new_swizzle[0]);
    es3_functions.glTexParameteri(target, GL_TEXTURE_SWIZZLE_G, new_swizzle[1]);
    es3_functions.glTexParameteri(target, GL_TEXTURE_SWIZZLE_B, new_swizzle[2]);
    es3_functions.glTexParameteri(target, GL_TEXTURE_SWIZZLE_A, new_swizzle[3]);
}

INTERNAL void swizzle_process_upload(GLenum target, GLenum* format, GLenum* type) {
    texture_swizzle_track_t* track = get_swizzle_track(target);
    if(track == NULL) return;
    bool apply_upload_bgra = false;
    bool apply_goofy_order = false;
    if((*format) == GL_BGRA_EXT) {
        apply_upload_bgra = true;
        *format = GL_RGBA;
    }
    if((*type) == 0x8035) {
        apply_goofy_order = true;
        *type = GL_UNSIGNED_BYTE;
    }
    if((*type) == 0x8367) {
        *type = GL_UNSIGNED_BYTE;
    }
    if(apply_goofy_order != track->goofy_byte_order || apply_upload_bgra != track->upload_bgra) {
        track->goofy_byte_order = apply_goofy_order;
        track->upload_bgra = apply_upload_bgra;
        apply_swizzles(target, track);
    }
}

INTERNAL void swizzle_process_swizzle_param(GLenum target, GLenum swizzle_param, const GLenum* swizzle) {
    switch (swizzle_param) {
        case GL_TEXTURE_SWIZZLE_R:
        case GL_TEXTURE_SWIZZLE_G:
        case GL_TEXTURE_SWIZZLE_B:
        case GL_TEXTURE_SWIZZLE_A:
        case GL_TEXTURE_SWIZZLE_RGBA:
            break;
        default:
            return;
    }
    texture_swizzle_track_t* track = get_swizzle_track(target);
    if(track == NULL) return;
    switch(swizzle_param) {
        case GL_TEXTURE_SWIZZLE_R:
        case GL_TEXTURE_SWIZZLE_G:
        case GL_TEXTURE_SWIZZLE_B:
        case GL_TEXTURE_SWIZZLE_A:
            track->original_swizzle[swizzle_param - GL_TEXTURE_SWIZZLE_R] = *swizzle;
            apply_swizzles(target, track);
            break;
        case GL_TEXTURE_SWIZZLE_RGBA:
            memcpy(track->original_swizzle, swizzle, 4 * sizeof(GLenum));
            apply_swizzles(target, track);
            break;
    }
}