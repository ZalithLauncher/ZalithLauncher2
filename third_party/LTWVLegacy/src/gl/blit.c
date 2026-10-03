/* Blit utility function */
#include "blit.h"

#include <math.h>

#include "fpe.h"
#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "loader.h"
#include "logs.h"
#ifdef TEXSTREAM
# ifndef GL_TEXTURE_STREAM_IMG
# define GL_TEXTURE_STREAM_IMG                                   0x8C0D
# endif
#include "../glx/streaming.h"
#endif

// hacky viewport temporary changes
void pushViewport(GLint x, GLint y, GLsizei width, GLsizei height);
void popViewport();

void gl4es_blitTexture_gles1(GLuint texture,
    GLfloat sx, GLfloat sy,
    GLfloat width, GLfloat height, 
    GLfloat nwidth, GLfloat nheight, 
    GLfloat zoomx, GLfloat zoomy, 
    GLfloat vpwidth, GLfloat vpheight, 
    GLfloat x, GLfloat y, GLint mode) {

    LOAD_GLES(glClientActiveTexture);

    GLfloat old_projection[16], old_modelview[16], old_texture[16];

    int customvp = (vpwidth>0.0);
    int drawtexok = (hardext.drawtex) && (zoomx==1.0f) && (zoomy==1.0f);

    GLuint old_cli = glstate->texture.client;
    if (old_cli!=0) gles_glClientActiveTexture(GL_TEXTURE0);

    gl4es_glDisable(GL_LIGHTING);
    gl4es_glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    gl4es_glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    switch (mode) {
        case BLIT_OPAQUE:
            gl4es_glDisable(GL_ALPHA_TEST);
            gl4es_glDisable(GL_BLEND);
            break;
        case BLIT_ALPHA:
			gl4es_glEnable(GL_ALPHA_TEST);
			gl4es_glAlphaFunc(GL_GREATER, 0.0f);
            break;
        case BLIT_COLOR:
            break;
    }

    if(drawtexok) {
        LOAD_GLES_OES(glDrawTexf);
        LOAD_GLES(glTexParameteriv);
        // setup texture first
        int sourceRect[4] = {sx, sy, width, height};
        gles_glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_CROP_RECT_OES, sourceRect);
        // take x/y of ViewPort into account
        GLfloat dx = (customvp)?0.0f:glstate->raster.viewport.x;
        GLfloat dy = (customvp)?0.0f:glstate->raster.viewport.y;
        //TODO: do something with width / height of ViewPort?
        // then draw it
        gles_glDrawTexf(x+dx, y+dy, 0.0f, width, height);
    } else {
        LOAD_GLES(glVertexPointer);
        LOAD_GLES(glTexCoordPointer);
        LOAD_GLES(glDrawArrays);

        GLfloat w2 = 2.0f / (customvp?vpwidth:glstate->raster.viewport.width);
        GLfloat h2 = 2.0f / (customvp?vpheight:glstate->raster.viewport.height);
        GLfloat blit_x1=roundf(x);
        GLfloat blit_x2=roundf(x+width*zoomx);
        GLfloat blit_y1=roundf(y);
        GLfloat blit_y2=roundf(y+height*zoomy);
        GLfloat blit_vert[] = {
            blit_x1*w2-1.0f, blit_y1*h2-1.0f,
            blit_x2*w2-1.0f, blit_y1*h2-1.0f,
            blit_x2*w2-1.0f, blit_y2*h2-1.0f,
            blit_x1*w2-1.0f, blit_y2*h2-1.0f
        };
        GLfloat sw = sx/nwidth;
        GLfloat sh = sy/nheight;
        GLfloat rw = (sx+width)/nwidth;
        GLfloat rh = (sy+height)/nheight;
        GLfloat blit_tex[] = {
            sw, sh,
            rw, sh,
            rw, rh,
            sw, rh
        };

        gl4es_glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT | GL_CLIENT_PIXEL_STORE_BIT);
        gl4es_glGetFloatv(GL_TEXTURE_MATRIX, old_texture);
        gl4es_glGetFloatv(GL_PROJECTION_MATRIX, old_projection);
        gl4es_glGetFloatv(GL_MODELVIEW_MATRIX, old_modelview);
        gl4es_glMatrixMode(GL_TEXTURE);
        gl4es_glLoadIdentity();
        gl4es_glMatrixMode(GL_PROJECTION);
        gl4es_glLoadIdentity();
        gl4es_glMatrixMode(GL_MODELVIEW);
        gl4es_glLoadIdentity();

        if(customvp)
            pushViewport(0,0,vpwidth, vpheight);
        
        fpe_glEnableClientState(GL_VERTEX_ARRAY);
        gles_glVertexPointer(2, GL_FLOAT, 0, blit_vert);
        fpe_glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        gles_glTexCoordPointer(2, GL_FLOAT, 0, blit_tex);
        for (int a=1; a <hardext.maxtex; a++)
            if(glstate->gleshard->vertexattrib[ATT_MULTITEXCOORD0+a].enabled) {
                gles_glClientActiveTexture(GL_TEXTURE0 + a);
                fpe_glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            }
        gles_glClientActiveTexture(GL_TEXTURE0);
        fpe_glDisableClientState(GL_COLOR_ARRAY);
        fpe_glDisableClientState(GL_NORMAL_ARRAY);
        gles_glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        if(customvp)
            popViewport();

        gl4es_glPopClientAttrib();
        gl4es_glMatrixMode(GL_TEXTURE);
        gl4es_glLoadMatrixf(old_texture);
        gl4es_glMatrixMode(GL_MODELVIEW);
        gl4es_glLoadMatrixf(old_modelview);
        gl4es_glMatrixMode(GL_PROJECTION);
        gl4es_glLoadMatrixf(old_projection);
    }

    if (old_cli!=0) gles_glClientActiveTexture(GL_TEXTURE0+old_cli);

}

const char _blit_vsh[] = "#version 100                  \n" \
"attribute highp vec2 aPosition;                        \n" \
"attribute highp vec2 aTexCoord;                        \n" \
"varying mediump vec2 vTexCoord;                        \n" \
"void main(){                                           \n" \
"gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);\n" \
"vTexCoord = aTexCoord;                                 \n" \
"}                                                      \n";

const char _blit_fsh[] = "#version 100                  \n" \
"uniform sampler2D uTex;                                \n" \
"varying mediump vec2 vTexCoord;                        \n" \
"void main(){                                           \n" \
"gl_FragColor = texture2D(uTex, vTexCoord);             \n" \
"}                                                      \n";

const char _blit_vsh_alpha[] = "#version 100            \n" \
"attribute highp vec2 aPosition;                        \n" \
"attribute highp vec2 aTexCoord;                        \n" \
"varying mediump vec2 vTexCoord;                        \n" \
"void main(){                                           \n" \
"gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);\n" \
"vTexCoord = aTexCoord;                                 \n" \
"}                                                      \n";

const char _blit_fsh_alpha[] = "#version 100            \n" \
"uniform sampler2D uTex;                                \n" \
"varying mediump vec2 vTexCoord;                        \n" \
"void main(){                                           \n" \
"lowp vec4 p = texture2D(uTex, vTexCoord);              \n" \
"if (p.a==0.0) discard;                                 \n" \
"gl_FragColor = p;                                      \n" \
"}                                                      \n";

void gl4es_blitTexture_gles2(GLuint texture,
    GLfloat sx, GLfloat sy,
    GLfloat width, GLfloat height, 
    GLfloat nwidth, GLfloat nheight, 
    GLfloat zoomx, GLfloat zoomy, 
    GLfloat vpwidth, GLfloat vpheight, 
    GLfloat x, GLfloat y, GLint mode) {

    LOAD_GLES(glDrawArrays);

    if(!glstate->blit) {
        LOAD_GLES2(glCreateShader);
        LOAD_GLES2(glShaderSource);
        LOAD_GLES2(glCompileShader);
        LOAD_GLES2(glGetShaderiv);
        LOAD_GLES2(glBindAttribLocation);
        LOAD_GLES2(glAttachShader);
        LOAD_GLES2(glCreateProgram);
        LOAD_GLES2(glLinkProgram);
        LOAD_GLES2(glGetProgramiv);
        LOAD_GLES(glGetUniformLocation);
        LOAD_GLES2(glUniform1i);
        LOAD_GLES2(glUseProgram);

        glstate->blit = (glesblit_t*)malloc(sizeof(glesblit_t));
        memset(glstate->blit, 0, sizeof(glesblit_t));

        GLint success;
        const char *src[1];
        src[0] = _blit_fsh;
        glstate->blit->pixelshader = gles_glCreateShader( GL_FRAGMENT_SHADER );
        gles_glShaderSource( glstate->blit->pixelshader, 1, (const char**) src, NULL );
        gles_glCompileShader( glstate->blit->pixelshader );
        gles_glGetShaderiv( glstate->blit->pixelshader, GL_COMPILE_STATUS, &success );
        if (!success)
        {
            LOAD_GLES(glGetShaderInfoLog);
            char log[400];
            gles_glGetShaderInfoLog(glstate->blit->pixelshader, 399, NULL, log);
            SHUT_LOGE("Failed to produce blit fragment shader.\n%s", log);
            free(glstate->blit);
            glstate->blit = NULL;
        }
    
        src[0] = _blit_fsh_alpha;
        glstate->blit->pixelshader_alpha = gles_glCreateShader( GL_FRAGMENT_SHADER );
        gles_glShaderSource( glstate->blit->pixelshader_alpha, 1, (const char**) src, NULL );
        gles_glCompileShader( glstate->blit->pixelshader_alpha );
        gles_glGetShaderiv( glstate->blit->pixelshader_alpha, GL_COMPILE_STATUS, &success );
        if (!success)
        {
            LOAD_GLES(glGetShaderInfoLog);
            char log[400];
            gles_glGetShaderInfoLog(glstate->blit->pixelshader_alpha, 399, NULL, log);
            SHUT_LOGE("Failed to produce blit with alpha fragment shader.\n%s", log);
            free(glstate->blit);
            glstate->blit = NULL;
        }
    
        src[0] = _blit_vsh;
        glstate->blit->vertexshader = gles_glCreateShader( GL_VERTEX_SHADER );
        gles_glShaderSource( glstate->blit->vertexshader, 1, (const char**) src, NULL );
        gles_glCompileShader( glstate->blit->vertexshader );
        gles_glGetShaderiv( glstate->blit->vertexshader, GL_COMPILE_STATUS, &success );
        if( !success )
        {
            LOAD_GLES(glGetShaderInfoLog);
            char log[400];
            gles_glGetShaderInfoLog(glstate->blit->vertexshader, 399, NULL, log);
            SHUT_LOGE("Failed to produce blit vertex shader.\n%s", log);
            free(glstate->blit);
            glstate->blit = NULL;
        }
    
        src[0] = _blit_vsh_alpha;
        glstate->blit->vertexshader_alpha = gles_glCreateShader( GL_VERTEX_SHADER );
        gles_glShaderSource( glstate->blit->vertexshader_alpha, 1, (const char**) src, NULL );
        gles_glCompileShader( glstate->blit->vertexshader_alpha );
        gles_glGetShaderiv( glstate->blit->vertexshader_alpha, GL_COMPILE_STATUS, &success );
        if( !success )
        {
            LOAD_GLES(glGetShaderInfoLog);
            char log[400];
            gles_glGetShaderInfoLog(glstate->blit->vertexshader_alpha, 399, NULL, log);
            SHUT_LOGE("Failed to produce blit with alpha vertex shader.\n%s", log);
            free(glstate->blit);
            glstate->blit = NULL;
        }

        glstate->blit->program = gles_glCreateProgram();
        gles_glBindAttribLocation( glstate->blit->program, 0, "aPosition" );
        gles_glBindAttribLocation( glstate->blit->program, 1, "aTexCoord" );
        gles_glAttachShader( glstate->blit->program, glstate->blit->pixelshader );
        gles_glAttachShader( glstate->blit->program, glstate->blit->vertexshader );
        gles_glLinkProgram( glstate->blit->program );
        gles_glGetProgramiv( glstate->blit->program, GL_LINK_STATUS, &success );
        if( !success )
        {
            SHUT_LOGE("Failed to link blit program.\n");
            free(glstate->blit);
            glstate->blit = NULL;
        }
        GLuint oldprog = glstate->gleshard->program;
        gles_glUseProgram(glstate->blit->program);
        gles_glUniform1i( gles_glGetUniformLocation( glstate->blit->program, "uTex" ), 0 );

        glstate->blit->program_alpha = gles_glCreateProgram();
        gles_glBindAttribLocation( glstate->blit->program_alpha, 0, "aPosition" );
        gles_glBindAttribLocation( glstate->blit->program_alpha, 1, "aTexCoord" );
        gles_glAttachShader( glstate->blit->program_alpha, glstate->blit->pixelshader_alpha );
        gles_glAttachShader( glstate->blit->program_alpha, glstate->blit->vertexshader_alpha );
        gles_glLinkProgram( glstate->blit->program_alpha );
        gles_glGetProgramiv( glstate->blit->program_alpha, GL_LINK_STATUS, &success );
        if( !success )
        {
            SHUT_LOGE("Failed to link blit program.\n");
            free(glstate->blit);
            glstate->blit = NULL;
        }
        gles_glUseProgram(glstate->blit->program_alpha);
        gles_glUniform1i( gles_glGetUniformLocation( glstate->blit->program_alpha, "uTex" ), 0 );
        gles_glUseProgram(oldprog);
    }

    int customvp = (vpwidth>0.0);
    GLfloat w2 = 2.0f / (customvp?vpwidth:glstate->raster.viewport.width);
    GLfloat h2 = 2.0f / (customvp?vpheight:glstate->raster.viewport.height);
    GLfloat blit_x1=roundf(x);
    GLfloat blit_x2=roundf(x+width*zoomx);
    GLfloat blit_y1=roundf(y);
    GLfloat blit_y2=roundf(y+height*zoomy);
    GLfloat *vert = glstate->blit->vert;
    GLfloat *tex = glstate->blit->tex;
    vert[0] = blit_x1*w2-1.0f;  vert[1] = blit_y1*h2-1.0f;
    vert[2] = blit_x2*w2-1.0f;  vert[3] = vert[1];
    vert[4] = vert[2];          vert[5] = blit_y2*h2-1.0f;
    vert[6] = vert[0];          vert[7] = vert[5];
    GLfloat sw = sx/nwidth;
    GLfloat sh = sy/nheight;
    GLfloat rw = (sx+width)/nwidth;
    GLfloat rh = (sy+height)/nheight;
    tex[0] = sw;  tex[1] = sh;
    tex[2] = rw;  tex[3] = sh;
    tex[4] = rw;  tex[5] = rh;
    tex[6] = sw;  tex[7] = rh;
    gl4es_glDisable(GL_BLEND);
    int alpha = 0;
    switch (mode) {
        case BLIT_OPAQUE:
            //gl4es_glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            break;
        case BLIT_ALPHA:
            alpha = 1;
            break;
        case BLIT_COLOR:
            //gl4es_glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            break;
    }

    realize_blitenv(alpha);

    gles_glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
}

void gl4es_blitTexture(GLuint texture, 
    GLfloat sx, GLfloat sy, 
    GLfloat width, GLfloat height, 
    GLfloat nwidth, GLfloat nheight, 
    GLfloat zoomx, GLfloat zoomy, 
    GLfloat vpwidth, GLfloat vpheight, 
    GLfloat x, GLfloat y, GLint mode) {
//printf("blitTexture(%d, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %d) customvp=%d, vp=%d/%d/%d/%d\n", texture, sx, sy, width, height, nwidth, nheight, zoomx, zoomy, vpwidth, vpheight, x, y, mode, (vpwidth>0.0), glstate->raster.viewport.x, glstate->raster.viewport.y, glstate->raster.viewport.width, glstate->raster.viewport.height);
    LOAD_GLES(glBindTexture);
    LOAD_GLES(glActiveTexture);
    LOAD_GLES(glEnable);
    LOAD_GLES(glDisable);

    realize_textures(1);

    gl4es_glPushAttrib(GL_TEXTURE_BIT | GL_ENABLE_BIT | GL_TRANSFORM_BIT | GL_COLOR_BUFFER_BIT | GL_CURRENT_BIT);

    if(glstate->gleshard->active) {
        glstate->gleshard->active = 0;
        gles_glActiveTexture(GL_TEXTURE0);
    }

    GLint depthwrite = glstate->depth.mask;

    gl4es_glDisable(GL_DEPTH_TEST);
    gl4es_glDisable(GL_CULL_FACE);
    gl4es_glDisable(GL_STENCIL_TEST);

    if(depthwrite)
        gl4es_glDepthMask(GL_FALSE);

#ifdef TEXSTREAM
    if(glstate->bound_stream[0] && hardext.esversion==1) {
//printf("TMU%d, turning off Streaming (blit)\n", 0);
        gles_glDisable(GL_TEXTURE_STREAM_IMG);
        DeactivateStreaming();
    }
#endif
    int tmp = glstate->enable.texture[0];

    if(glstate->actual_tex2d[0] != texture)
        gles_glBindTexture(GL_TEXTURE_2D, texture);

    if(hardext.esversion==1) {
        if(!IS_TEX2D(tmp))
            gles_glEnable(GL_TEXTURE_2D);
        if(IS_CUBE_MAP(tmp))
            gles_glDisable(GL_TEXTURE_CUBE_MAP);

        gl4es_blitTexture_gles1(texture, sx, sy, width, height, 
                                nwidth, nheight, zoomx, zoomy, 
                                vpwidth, vpheight, x, y, mode);
        if(!IS_TEX2D(tmp))
            gles_glDisable(GL_TEXTURE_2D);
        if(IS_CUBE_MAP(tmp))
            gles_glEnable(GL_TEXTURE_CUBE_MAP);
    } else {
        gl4es_blitTexture_gles2(texture, sx, sy, width, height, 
            nwidth, nheight, zoomx, zoomy, 
            vpwidth, vpheight, x, y, mode);
    }

    // All the previous states are Pushed / Poped anyway...
#ifdef TEXSTREAM
    if(glstate->bound_stream[0] && hardext.esversion==1) {
//printf("TMU%d, turning ON  Streaming (blit)\n", 0);
        gltexture_t *tex = glstate->texture.bound[0][ENABLED_TEX2D];
        ActivateStreaming(tex->streamingID);
        gles_glEnable(GL_TEXTURE_STREAM_IMG);
    } else
#endif
    if (glstate->actual_tex2d[0] != texture) 
        gles_glBindTexture(GL_TEXTURE_2D, glstate->actual_tex2d[0]);

    if(depthwrite)
        gl4es_glDepthMask(GL_TRUE);

    gl4es_glPopAttrib();
}