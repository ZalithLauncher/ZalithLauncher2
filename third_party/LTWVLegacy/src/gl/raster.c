#include "raster.h"

#include <math.h>

#include "../glx/hardext.h"
#include "blit.h"
#include "debug.h"
#include "gl4es.h"
#include "glstate.h"
#include "init.h"
#include "list.h"
#include "loader.h"
#include "matvec.h"
#include "pixel.h"

#undef min
#undef max
#define min(a, b)	((a)<b)?(a):(b)
#define max(a, b)	((a)>(b))?(a):(b)

void APIENTRY_GL4ES gl4es_glRasterPos3f(GLfloat x, GLfloat y, GLfloat z) {
    if (glstate->list.active)
        if (glstate->list.compiling) {
		NewStage(glstate->list.active, STAGE_RASTER);
		rlRasterOp(glstate->list.active, 1, x, y, z);
		noerrorShim();
		return;
	} else gl4es_flush();

    // Transform xyz coordinates with current modelview and projection matrix...
    GLfloat glmatrix[16], projection[16], modelview[16];
    GLfloat t[4], transl[4] = {x, y, z, 1.0f};
    gl4es_glGetFloatv(GL_PROJECTION_MATRIX, glmatrix);
    matrix_transpose(glmatrix, projection);
    gl4es_glGetFloatv(GL_MODELVIEW_MATRIX, glmatrix);
    matrix_transpose(glmatrix, modelview);
    matrix_vector(modelview, transl, t);
    matrix_vector(projection, t, transl);
    /* The raster position is valid only inside the clip volume,
     * -w <= x, y, z <= w; outside it, glBitmap must draw nothing rather than
     * keep using the previous position. Non-finite input spreads NaN through
     * matrix_vector (0 * inf and 0 * NaN are NaN), which isnan(w) catches. */
    GLfloat w = transl[3];
    if (isnan(w) || w <= 0.0f || fabsf(transl[0]) > w
        || fabsf(transl[1]) > w || fabsf(transl[2]) > w) {
      glstate->raster.rPos_valid = GL_FALSE;
      return;
    }
    GLfloat invw = 1.0f / w;    /* |x| <= w, so x * invw can't overflow */
    GLfloat w2, h2;
    w2=glstate->raster.viewport.width/2.0f;
    h2=glstate->raster.viewport.height/2.0f;
    glstate->raster.rPos.x = transl[0]*invw*w2+w2;
    glstate->raster.rPos.y = transl[1]*invw*h2+h2;
    glstate->raster.rPos.z = transl[2]*invw*0.5f+0.5f;
    glstate->raster.rPos_valid = GL_TRUE;
}
#if !defined(NO_EGL) && !defined(NOX11)
void refreshMainFBO();
#endif
void APIENTRY_GL4ES gl4es_glWindowPos3f(GLfloat x, GLfloat y, GLfloat z) {
    if (glstate->list.active)
        if (glstate->list.compiling) {
			NewStage(glstate->list.active, STAGE_RASTER);
			rlRasterOp(glstate->list.active, 2, x, y, z);
			noerrorShim();
			return;
		} else gl4es_flush();

    if (x >= 0 && y >= 0 && z >= 0) {
      glstate->raster.rPos.x = x;
      glstate->raster.rPos.y = y;
      glstate->raster.rPos.z = z;
      glstate->raster.rPos_valid = GL_TRUE;
    } else {
      // TODO: glWindowPos never invalidates per spec; negative coords should still draw, clipped
      glstate->raster.rPos_valid = GL_FALSE;
    }
}

void APIENTRY_GL4ES gl4es_glViewport(GLint x, GLint y, GLsizei width, GLsizei height) {
	if (glstate->fbo.current_fb->id != 0) {
    	gltexture_t *tex = gl4es_getTexture(glstate->fbo.current_fb->t_color[0], glstate->fbo.current_fb->color[0]);
		if (tex->fbtex_ratio > 0.0f) {
			width *= tex->fbtex_ratio;
			height *= tex->fbtex_ratio;
			x *= tex->fbtex_ratio;
			y *= tex->fbtex_ratio;
		}
	}

	if(!glstate->list.pending) 
		PUSH_IF_COMPILING(glViewport);
	if(	glstate->raster.viewport.x!=x || 
		glstate->raster.viewport.y!=y || 
		glstate->raster.viewport.width!=width || 
		glstate->raster.viewport.height!=height) 
	{
		FLUSH_BEGINEND;
		if (glstate->raster.bm_drawing)	bitmap_flush();
    	LOAD_GLES(glViewport);
		gles_glViewport(x, y, width, height);
    if (x >= 0 && y >= 0 && width >= 0 && height >= 0) {
		  glstate->raster.viewport.x = x;
		  glstate->raster.viewport.y = y;
		  glstate->raster.viewport.width = width;
		  glstate->raster.viewport.height = height;
		  glstate->raster.viewport_known = 1;
    }
#if !defined(NO_EGL) && !defined(NOX11)
		if(!globals4es.usefbo && !globals4es.usefb && glstate->fbo.fbo_draw->id==0) {
			// check if underlying EGL surface change dimension, and reflect that to main fbo size
			refreshMainFBO();
		}
#endif
	}
}

void APIENTRY_GL4ES gl4es_glScissor(GLint x, GLint y, GLsizei width, GLsizei height) {
	if (glstate->fbo.current_fb->id != 0) {
    	gltexture_t *tex = gl4es_getTexture(glstate->fbo.current_fb->t_color[0], glstate->fbo.current_fb->color[0]);
		if (tex->fbtex_ratio > 0.0f) {
			width *= tex->fbtex_ratio;
			height *= tex->fbtex_ratio;
			x *= tex->fbtex_ratio;
			y *= tex->fbtex_ratio;
		}
	}

	if(!glstate->list.pending) 
	    PUSH_IF_COMPILING(glScissor);
#ifdef AMIGAOS4
	if(x<0) {width+=x; x=0;}
	if(y<0) {height+=y; y=0;}
	if(x+width>glstate->raster.viewport.width+glstate->raster.viewport.x) width = glstate->raster.viewport.width+glstate->raster.viewport.x - x;
	if(y+height>glstate->raster.viewport.height+glstate->raster.viewport.y) height = glstate->raster.viewport.height+glstate->raster.viewport.y - y;
#endif
	if(	glstate->raster.scissor.x!=x || 
		glstate->raster.scissor.y!=y || 
		glstate->raster.scissor.width!=width || 
		glstate->raster.scissor.height!=height) 
	{
		FLUSH_BEGINEND;
		if (glstate->raster.bm_drawing) bitmap_flush();
    	LOAD_GLES(glScissor);
		gles_glScissor(x, y, width, height);

    if (x >= 0 && y >= 0 && width >= 0 && height >= 0) {
		  glstate->raster.scissor.x = x;
		  glstate->raster.scissor.y = y;
		  glstate->raster.scissor.width = width;
		  glstate->raster.scissor.height = height;
		  glstate->raster.scissor_known = 1;
    }
	}
}

// hacky viewport temporary changes
void pushViewport(GLint x, GLint y, GLsizei width, GLsizei height) {
    LOAD_GLES(glViewport);
    gles_glViewport(x, y, width, height);
}
void popViewport() {
    LOAD_GLES(glViewport);
    gles_glViewport(glstate->raster.viewport.x, glstate->raster.viewport.y, glstate->raster.viewport.width, glstate->raster.viewport.height);
}


void APIENTRY_GL4ES gl4es_glPixelZoom(GLfloat xfactor, GLfloat yfactor) {
    if (glstate->list.active)
        if (glstate->list.compiling) {
		NewStage(glstate->list.active, STAGE_RASTER);
		rlRasterOp(glstate->list.active, 3, xfactor, yfactor, 0.0f);
		noerrorShim();
		return;
	} else gl4es_flush();

    glstate->raster.raster_zoomx = xfactor;
    glstate->raster.raster_zoomy = yfactor;
//printf("LIBGL: glPixelZoom(%f, %f)\n", xfactor, yfactor);
}

void APIENTRY_GL4ES gl4es_glPixelTransferf(GLenum pname, GLfloat param) {
    if (glstate->list.active)
        if (glstate->list.compiling) {
		NewStage(glstate->list.active, STAGE_RASTER);
		rlRasterOp(glstate->list.active, pname|0x10000, param, 0.0f, 0.0f);
		noerrorShim();
		return;
	} else gl4es_flush();

//printf("LIBGL: glPixelTransferf(%04x, %f)\n", pname, param);
    switch(pname) {
	case GL_RED_SCALE:
		glstate->raster.raster_scale[0]=param;
		break;
	case GL_RED_BIAS:
		glstate->raster.raster_bias[0]=param;
		break;
	case GL_GREEN_SCALE:
	case GL_BLUE_SCALE:
	case GL_ALPHA_SCALE:
		glstate->raster.raster_scale[(pname-GL_GREEN_SCALE)/2+1]=param;
		break;
	case GL_GREEN_BIAS:
	case GL_BLUE_BIAS:
	case GL_ALPHA_BIAS:
		glstate->raster.raster_bias[(pname-GL_GREEN_BIAS)/2+1]=param;
		break;
	case GL_INDEX_SHIFT:
		glstate->raster.index_shift=param;
		break;
	case GL_INDEX_OFFSET:
		glstate->raster.index_offset=param;
		break;
	case GL_MAP_COLOR:
		glstate->raster.map_color=param?1:0;
		break;
	/*default:
		printf("LIBGL: stubbed glPixelTransferf(%04x, %f)\n", pname, param);*/
	// the other...
    }
}


void init_raster(int width, int height) {
	int w, h;
	w=(hardext.npot>0)?width:npot(width);
	h=(hardext.npot>0)?height:npot(height);
	if (glstate->raster.data) {
		if ((glstate->raster.raster_nwidth<width) || (glstate->raster.raster_nheight<height)) {
			free(glstate->raster.data);
			glstate->raster.data = NULL;
		}
	}
    if (!glstate->raster.data) {
        glstate->raster.data = (GLubyte *)malloc(4 * w * h * sizeof(GLubyte)); // no need to memset to 0
		glstate->raster.raster_nwidth = w; glstate->raster.raster_nheight = h;
	}
	glstate->raster.raster_x1 = 0; glstate->raster.raster_y1 = 0;
	glstate->raster.raster_x2 = width; glstate->raster.raster_y2 = height;
	glstate->raster.raster_width = width; glstate->raster.raster_height = height;
}

GLubyte raster_transform(GLubyte pix, GLubyte number) {
	GLfloat a = (GLfloat)pix*(1.0f/255.0f);
	a=a*glstate->raster.raster_scale[number]+glstate->raster.raster_bias[number];
	if (a<0.0f) return 0;
	if (a>1.0f) return 255;
	return (GLubyte)(a*255.0f);
}
GLfloat FASTMATH raster_transformf(GLfloat pix, GLubyte number) {
	pix=pix*glstate->raster.raster_scale[number]+glstate->raster.raster_bias[number];
	if (pix<0.0f) return 0.0f;
	if (pix>1.0f) return 1.0f;
	return pix;
}

int raster_need_transform() {
	for (int i=0; i<4; i++) if (glstate->raster.raster_scale[i]!=1.0f) return 1;
	for (int i=0; i<4; i++) if (glstate->raster.raster_bias[i]!=0.0f) return 1;
	return 0;
}

void raster_to_texture(rasterlist_t *r)
{
	renderlist_t *old_list = glstate->list.active;
	if (old_list) glstate->list.active = NULL;		// deactivate list...
	GLboolean compiling = glstate->list.compiling;
	glstate->list.compiling = false;
    gl4es_glPushAttrib(GL_TEXTURE_BIT | GL_ENABLE_BIT );
	GLuint old_tex_unit, old_tex;
	old_tex_unit = glstate->texture.active;
	if (old_tex_unit!=0) gl4es_glActiveTexture(GL_TEXTURE0);
	old_tex = glstate->texture.bound[0][ENABLED_TEX2D]->glname;

	gl4es_glPixelStorei(GL_PACK_ALIGNMENT, 1);
    gl4es_glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    gl4es_glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
    gl4es_glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
    gl4es_glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
	gl4es_glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
	
	if(r->texture==0) {
		gl4es_glEnable(GL_TEXTURE_2D);
		gl4es_glGenTextures(1, &r->texture);
		gl4es_glBindTexture(GL_TEXTURE_2D, r->texture);

		gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0); // this is to be sure texture is not npot'ed ...
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);	// ... if not needed
		gl4es_glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, glstate->raster.raster_nwidth, glstate->raster.raster_nheight,
			0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
	} else {
		gl4es_glBindTexture(GL_TEXTURE_2D, r->texture);
	}
	gl4es_glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, glstate->raster.raster_width, glstate->raster.raster_height,
		GL_RGBA, GL_UNSIGNED_BYTE, glstate->raster.data);

	r->width = glstate->raster.raster_width;
	r->height = glstate->raster.raster_height;
	r->nwidth = glstate->raster.raster_nwidth;
	r->nheight = glstate->raster.raster_nheight;
	gl4es_glBindTexture(GL_TEXTURE_2D, old_tex);
	if (old_tex_unit!=0) 
		gl4es_glActiveTexture(old_tex_unit+GL_TEXTURE0);
	gl4es_glPopAttrib();
	if (old_list) glstate->list.active = old_list;
	glstate->list.compiling = compiling;
}

void bitmap_flush() {
	if(!glstate->raster.bm_drawing)
		return;
	// draw actual bitmap
	int old_tex_unit = glstate->texture.active;
	if(old_tex_unit)
		gl4es_glActiveTexture(GL_TEXTURE0);

	GLuint old_tex = glstate->texture.bound[0][ENABLED_TEX2D]->glname;
	GLuint old_active = glstate->enable.texture[0];

	if(IS_TEX1D(old_active)) gl4es_glDisable(GL_TEXTURE_1D);
	if(!IS_TEX2D(old_active)) gl4es_glEnable(GL_TEXTURE_2D);
	if(IS_TEX3D(old_active)) gl4es_glDisable(GL_TEXTURE_3D);
	if(IS_TEXTURE_RECTANGLE(old_active)) gl4es_glDisable(GL_TEXTURE_RECTANGLE_ARB);
	if(IS_CUBE_MAP(old_active)) gl4es_glDisable(GL_TEXTURE_CUBE_MAP);

	if(!glstate->raster.bm_texture) {
		gl4es_glGenTextures(1, &glstate->raster.bm_texture);
		gl4es_glBindTexture(GL_TEXTURE_2D, glstate->raster.bm_texture);

		gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		gl4es_glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0); // this is to be sure texture is not npot'ed ...
		gl4es_glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);	// ... if not needed
	} else {
		gl4es_glBindTexture(GL_TEXTURE_2D, glstate->raster.bm_texture);
	}
	if (glstate->raster.bm_tnwidth < glstate->raster.bm_width || glstate->raster.bm_tnheight < glstate->raster.bm_height) {
		glstate->raster.bm_tnwidth = (hardext.npot)?glstate->raster.bm_width:npot(glstate->raster.bm_width);
		glstate->raster.bm_tnheight = (hardext.npot)?glstate->raster.bm_height:npot(glstate->raster.bm_height);
		gl4es_glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, glstate->raster.bm_tnwidth, glstate->raster.bm_tnheight, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
	}
	int sx = glstate->raster.bm_x1;
	int ex = glstate->raster.bm_x2-glstate->raster.bm_x1;
	int sy = glstate->raster.bm_y1;
	int ey = glstate->raster.bm_y2-glstate->raster.bm_y1;
	if(sx==0 && sy==0 && ex==glstate->raster.bm_width && ey==glstate->raster.bm_height) {
		gl4es_glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, glstate->raster.bm_width, glstate->raster.bm_height, GL_RGBA, GL_UNSIGNED_BYTE, glstate->raster.bitmap);
	} else {
		int alloc = 4*ex*ey;
		gl4es_scratch(alloc);
		for (int i=0; i<ey; i++)
			memcpy((char*)glstate->scratch+4*i*ex, (char*)glstate->raster.bitmap+4*(sx+(sy+i)*glstate->raster.bm_width), ex*4);
		gl4es_glTexSubImage2D(GL_TEXTURE_2D, 0, sx, sy, ex, ey, GL_RGBA, GL_UNSIGNED_BYTE, glstate->scratch);
	}

	gl4es_blitTexture(
		glstate->raster.bm_texture, 
		sx, sy,
		ex, ey,
		glstate->raster.bm_tnwidth , glstate->raster.bm_tnheight,
		1.f, 1.f, 
		0, 0,	//vp is default here
		sx, sy,
		BLIT_ALPHA
	);

	glstate->raster.bm_drawing = 0;
	/* Keep the CPU-side bitmap buffer transparent between batches without
	 * clearing the entire viewport for every run of glyphs.  Only this dirty
	 * rectangle can contain non-zero pixels: the buffer is fully initialized
	 * when allocated, and every completed batch is cleared here after its
	 * upload and blit have consumed the pixels. */
	for (int i=0; i<ey; i++)
		memset((char*)glstate->raster.bitmap+4*(sx+(sy+i)*glstate->raster.bm_width), 0, 4*ex);

	if(IS_TEX1D(old_active)) gl4es_glEnable(GL_TEXTURE_1D);
	if(!IS_TEX2D(old_active)) gl4es_glDisable(GL_TEXTURE_2D);
	if(IS_TEX3D(old_active)) gl4es_glEnable(GL_TEXTURE_3D);
	if(IS_TEXTURE_RECTANGLE(old_active)) gl4es_glEnable(GL_TEXTURE_RECTANGLE_ARB);
	if(IS_CUBE_MAP(old_active)) gl4es_glEnable(GL_TEXTURE_CUBE_MAP);

	if (old_tex!=glstate->raster.bm_texture)
		gl4es_glBindTexture(GL_TEXTURE_2D, old_tex);

	if(old_tex_unit)
		gl4es_glActiveTexture(GL_TEXTURE0 + old_tex_unit);
}


void APIENTRY_GL4ES gl4es_glBitmap(GLsizei width, GLsizei height, GLfloat xorig, GLfloat yorig,
              GLfloat xmove, GLfloat ymove, const GLubyte *bitmap) {
/*printf("glBitmap, xy={%f, %f}, xyorig={%f, %f}, size={%u, %u}, zoom={%f, %f}, viewport={%i, %i, %i, %i}\n", 	
	glstate->raster.rPos.x, glstate->raster.rPos.y, xorig, yorig, width, height, glstate->raster.raster_zoomx, glstate->raster.raster_zoomy, glstate->raster.viewport.x, glstate->raster.viewport.y, glstate->raster.viewport.width, glstate->raster.viewport.height);*/
    // TODO: negative width/height mirrors bitmap?
	noerrorShim();
	FLUSH_BEGINEND;
	if(glstate->list.active) {
		renderlist_t* list = glstate->list.active;
		if (!list->bitmaps) {
			list->bitmaps = (bitmaps_t*)malloc(sizeof(bitmaps_t));
			memset(list->bitmaps, 0, sizeof(bitmaps_t));
			list->bitmaps->shared = (int*)malloc(sizeof(int)); 
            *list->bitmaps->shared = 0;
		}
		if(list->bitmaps->count == list->bitmaps->cap) {
			list->bitmaps->cap += 8;
			list->bitmaps->list = (bitmap_list_t*)realloc(list->bitmaps->list, list->bitmaps->cap*sizeof(bitmap_list_t));
		}
		bitmap_list_t *l = &list->bitmaps->list[list->bitmaps->count++];
		l->width = width;
		l->height = height;
		l->xorig = xorig;
		l->yorig = yorig;
		l->xmove = xmove;
		l->ymove = ymove;
		int sz = ((width+7)/8)*height;
		l->bitmap = (GLubyte*)malloc(sz);
		memcpy(l->bitmap, bitmap, sz);
		return;
	}
	// Invalid raster position (see gl4es_glRasterPos3f): ignore the bitmap,
	// including the xmove/ymove advance of the raster position.
	if(!glstate->raster.rPos_valid)
		return;
  if (((!width && !height) || (bitmap==0)) && glstate->raster.rPos.x + xmove >= 0 && glstate->raster.rPos.y + ymove >= 0) {
		  glstate->raster.rPos.x += xmove;
		  glstate->raster.rPos.y += ymove;
      return;
  }

	// get start/end of drawed pixel
	float zoomx = glstate->raster.raster_zoomx;
	float zoomy = glstate->raster.raster_zoomy;
	int sx, sy, ex, ey;
	sx = 0;
	sy = 0;
	ex = width*zoomx;
	ey = height*zoomy;

	int rx = glstate->raster.rPos.x-xorig;
	int ry = glstate->raster.rPos.y-yorig;

	if (rx+sx<0) sx = -rx;
	if (ry+sy<0) sy = -ry;
	if (rx+ex>glstate->raster.viewport.width) ex = glstate->raster.viewport.width-rx;
	if (ry+ey>glstate->raster.viewport.height) ey = glstate->raster.viewport.height-ry;
	if (sx>=ex || sy>=ey)	// nothing to draw, no changes
		return;
	// create/realloc buffer if needed
	int bitmap_reallocated = 0;
	if(glstate->raster.bm_alloc < glstate->raster.viewport.width*glstate->raster.viewport.height*4) {
		if(glstate->raster.bitmap)
			free(glstate->raster.bitmap);
		glstate->raster.bm_alloc = glstate->raster.viewport.width*glstate->raster.viewport.height*4;
		glstate->raster.bitmap = (GLubyte*)malloc(glstate->raster.bm_alloc);
		if(!glstate->raster.bitmap) {
			glstate->raster.bm_alloc = 0;
			return;
		}
		bitmap_reallocated = 1;
	}
	/* A new allocation has no prior dirty rectangle (and the old buffer
	 * is gone), so always zero it — including realloc-while-drawing. */
	if(bitmap_reallocated) {
		memset(glstate->raster.bitmap, 0, glstate->raster.bm_alloc);
		glstate->raster.bm_width = glstate->raster.viewport.width;
		glstate->raster.bm_height = glstate->raster.viewport.height;
		glstate->raster.bm_x1 = glstate->raster.bm_width;
		glstate->raster.bm_y1 = glstate->raster.bm_height;
		glstate->raster.bm_x2 = 0;
		glstate->raster.bm_y2 = 0;
	} else if(!glstate->raster.bm_drawing) {
		glstate->raster.bm_width = glstate->raster.viewport.width;
		glstate->raster.bm_height = glstate->raster.viewport.height;
		glstate->raster.bm_x1 = glstate->raster.bm_width;
		glstate->raster.bm_y1 = glstate->raster.bm_height;
		glstate->raster.bm_x2 = 0;
		glstate->raster.bm_y2 = 0;
	}
	glstate->raster.bm_x1 = min(glstate->raster.bm_x1, rx+sx);
	glstate->raster.bm_y1 = min(glstate->raster.bm_y1, ry+sy);
	glstate->raster.bm_x2 = max(glstate->raster.bm_x2, rx+ex);
	glstate->raster.bm_y2 = max(glstate->raster.bm_y2, ry+ey);
	// draw the scaled bitmap
	int pixtrans=raster_need_transform();
    const GLubyte *from;
    GLubyte *to;
	GLubyte col[4];
	for (int i=0; i<4; i++)
		col[i] = glstate->color[i]*255.f;
    // copy to pixel data
	if (pixtrans) {
        for (int y = sy; y < ey; ++y) {
			int by = floor(y/zoomy);
            from = bitmap + (by * ((width+7)/8));
			to = glstate->raster.bitmap + 4 * (GLint)(rx+sx+((ry+y) * glstate->raster.bm_width));
            for (int x = sx; x < ex; ++x) {
				int bx = floor(x/zoomx);
                GLubyte b = from[(bx / 8)];
                int p = (b & (1 << (7 - (bx % 8)))) ? 1 : 0;
                // r, g, b, a
				*to++ = raster_transform(col[0]*p, 0);
				*to++ = raster_transform(col[1]*p, 1);
				*to++ = raster_transform(col[2]*p, 2);
				*to++ = raster_transform(col[3]*p, 3);
			}
        }
	} else {
        for (int y = sy; y < ey; ++y) {
			int by = floor(y/zoomy);
            from = bitmap + (by * ((width+7)/8));
			to = glstate->raster.bitmap + 4 * (GLint)(rx+sx+((ry+y) * glstate->raster.bm_width));
            for (int x = sx; x < ex; ++x) {
				int bx = floor(x/zoomx);
                GLubyte b = from[(bx / 8)];
                int p = (b & (1 << (7 - (bx % 8)))) ? 1 : 0;
				*to++ = col[0]*p;
				*to++ = col[1]*p;
				*to++ = col[2]*p;
				*to++ = col[3]*p;
			}
        }
    }

	// finished, move...
  if ((glstate->raster.rPos.x + xmove) >= 0 && (glstate->raster.rPos.y + ymove) >= 0) {
	  glstate->raster.rPos.x += xmove;
	  glstate->raster.rPos.y += ymove;
  }
	// draw in buffer...
	glstate->raster.bm_drawing = 1;
}

void APIENTRY_GL4ES gl4es_glDrawPixels(GLsizei width, GLsizei height, GLenum format,
                  GLenum type, const GLvoid *data) {
    GLubyte *pixels, *from, *to;
    GLvoid *dst = NULL;

	if(type==GL_BITMAP) {
		gl4es_glBitmap(width, height, 0, 0, 0, 0, data);
		return;
	}

    noerrorShim();
	FLUSH_BEGINEND;
	// TODO: no rPos_valid check (gl4es_glBitmap has one): an invalid raster position
	// still draws at the last valid one. Fix in render_raster_list(), which list replay also calls.

    if (glstate->raster.bm_drawing) bitmap_flush();

/*printf("glDrawPixels, xy={%f, %f}, size={%i, %i}, format=%s, type=%s, zoom={%f, %f}, viewport={%i, %i, %i, %i}\n", 	
	glstate->raster.rPos.x, glstate->raster.rPos.y, width, height, PrintEnum(format), PrintEnum(type), glstate->raster.raster_zoomx, glstate->raster.raster_zoomy, glstate->raster.viewport.x, glstate->raster.viewport.y, glstate->raster.viewport.width, glstate->raster.viewport.height);*/
	// check of unsuported format...
	if ((format == GL_STENCIL_INDEX) || (format == GL_DEPTH_COMPONENT)) {
        errorShim(GL_INVALID_ENUM);
		return;
    }

    init_raster(width, height);

	GLsizei bmp_width = (glstate->texture.unpack_row_length)?glstate->texture.unpack_row_length:width;

    if (! pixel_convert(data, &dst, bmp_width, height,
                        format, type, GL_RGBA, GL_UNSIGNED_BYTE, 0, 1)) {	// pack_align is forced to 1 when drawing
        return;
    }
					  
    pixels = (GLubyte *)dst;
	int pixtrans=raster_need_transform();

    if (pixtrans) {
        for (int y = 0; y < height; y++) {
            to = glstate->raster.data + 4 * (GLint)(y * glstate->raster.raster_width);
            from = pixels + 4 * (glstate->texture.unpack_skip_pixels + (y + glstate->texture.unpack_skip_rows) * bmp_width);
            for (int x = 0; x < width; x++) {
				*to++ = raster_transform(*from++, 0);
				*to++ = raster_transform(*from++, 1);
				*to++ = raster_transform(*from++, 2);
				*to++ = raster_transform(*from++, 3);
            }
        }
	} else {
        for (int y = 0; y < height; y++) {
            to = glstate->raster.data + 4 * (GLint)(y * glstate->raster.raster_width);
            from = pixels + 4 * (glstate->texture.unpack_skip_pixels + (y + glstate->texture.unpack_skip_rows) * bmp_width);
            for (int x = 0; x < width; x++) {
				*to++ = *from++;
				*to++ = *from++;
				*to++ = *from++;
				*to++ = *from++;
			}
		}
	}
	if (pixels != data)
        free(pixels);
	
    rasterlist_t *r;
	if (glstate->list.active) {
		NewStage(glstate->list.active, STAGE_RASTER);
		r = glstate->list.active->raster = (rasterlist_t*)malloc(sizeof(rasterlist_t));
		memset(r, 0, sizeof(rasterlist_t));
        r->shared = (int*)malloc(sizeof(int));
        *r->shared = 0;
	} else {
		r = &glstate->raster.immediate;
		if(r->texture && (width>r->nwidth || height>r->nheight)) {
			gl4es_glDeleteTextures(1, &r->texture);
			r->texture = 0;
		}
	}
	raster_to_texture(r);
	r->xmove = 0;
	r->ymove = 0;
	r->xorig = 0;
	r->yorig = 0;
	r->bitmap = false;
	r->zoomx = glstate->raster.raster_zoomx;
	r->zoomy = glstate->raster.raster_zoomy;
	if (!(glstate->list.active)) {
		render_raster_list(r);
/*		gles_glDeleteTextures(1, &r->texture);
		r->texture = 0;*/
	}
}

void render_raster_list(rasterlist_t* rast) {
	// TODO: glDrawPixels ignores rPos_valid; skip the blit (and the move) when it is false
//printf("render_raster_list, rast->x/y=%f/%f rast->width/height=%i/%i, rPos.x/y/z=%f/%f/%f, rast->zoomxy=%f/%f raster->texture=%u\n", rast->xorig, rast->yorig, rast->width, rast->height, glstate->raster.rPos.x, glstate->raster.rPos.y, glstate->raster.rPos.z, rast->zoomx, rast->zoomy, rast->texture);
	if (rast->texture)
		gl4es_blitTexture(
			rast->texture, 
			0.f, 0.f,
			rast->width , rast->height,
			rast->nwidth, rast->nheight,
			rast->zoomx, rast->zoomy, 
			0, 0,	//vp is default here
			glstate->raster.rPos.x-rast->xorig, glstate->raster.rPos.y-rast->yorig,
			(rast->bitmap)?BLIT_ALPHA:BLIT_COLOR
		);
  if (glstate->raster.rPos.x + rast->xmove >= 0 && glstate->raster.rPos.y + rast->ymove >= 0) {
    glstate->raster.rPos.x += rast->xmove;
    glstate->raster.rPos.y += rast->ymove;
  }
}

int map_pixelmap(GLenum map, int* wf, int** size, void** array) {
	*wf = 1;
	switch (map) {
		case GL_PIXEL_MAP_I_TO_I:
			*wf = 0;
			*array = (void*)glstate->raster.map_i2i;
			*size = &glstate->raster.map_i2i_size;
			break;
		case GL_PIXEL_MAP_I_TO_R:
			*array = (void*)glstate->raster.map_i2r;
			*size = &glstate->raster.map_i2r_size;
			break;
		case GL_PIXEL_MAP_I_TO_G:
			*array = (void*)glstate->raster.map_i2g;
			*size = &glstate->raster.map_i2g_size;
			break;
		case GL_PIXEL_MAP_I_TO_B:
			*array = (void*)glstate->raster.map_i2b;
			*size = &glstate->raster.map_i2b_size;
			break;
		case GL_PIXEL_MAP_I_TO_A:
			*array = (void*)glstate->raster.map_i2a;
			*size = &glstate->raster.map_i2a_size;
			break;
		case GL_PIXEL_MAP_S_TO_S:
		case GL_PIXEL_MAP_R_TO_R:
		case GL_PIXEL_MAP_G_TO_G:
		case GL_PIXEL_MAP_B_TO_B:
		case GL_PIXEL_MAP_A_TO_A:
			// not handled
			noerrorShim();
			return 0;
		default:
			errorShim(GL_INVALID_ENUM);
			return 0;
	}
	return 1;
}

void APIENTRY_GL4ES gl4es_glPixelMapfv(GLenum map, GLsizei mapsize, const GLfloat *values) {
	if(mapsize>MAX_MAP_SIZE) {
		errorShim(GL_INVALID_VALUE);
		return;
	}
	int need_pot = 0;
	if(map==GL_PIXEL_MAP_I_TO_I || map==GL_PIXEL_MAP_S_TO_S 
		|| map==GL_PIXEL_MAP_I_TO_R || map==GL_PIXEL_MAP_I_TO_G
		|| map==GL_PIXEL_MAP_I_TO_B || map==GL_PIXEL_MAP_I_TO_A)
		need_pot=1;
	if(need_pot && npot(mapsize)!=mapsize) {
		errorShim(GL_INVALID_VALUE);
		return;
	}
	int wf = 1;
	void* array = NULL;
	int* size = NULL;
	if(!map_pixelmap(map, &wf, &size, &array))
		return;
	noerrorShim();
	if(wf) {
		GLfloat *p = (GLfloat*)array;
		for (int i=0; i<mapsize; i++)
			p[i] = values[i]*255.0f;
	} else {
		GLuint *p = (GLuint*)array;
		for (int i=0; i<mapsize; i++)
			p[i] = values[i];
	}
	*size = mapsize;
}
void APIENTRY_GL4ES gl4es_glPixelMapuiv(GLenum map,GLsizei mapsize, const GLuint *values) {
	if(mapsize>MAX_MAP_SIZE) {
		errorShim(GL_INVALID_VALUE);
		return;
	}
	int need_pot = 0;
	if(map==GL_PIXEL_MAP_I_TO_I || map==GL_PIXEL_MAP_S_TO_S 
		|| map==GL_PIXEL_MAP_I_TO_R || map==GL_PIXEL_MAP_I_TO_G
		|| map==GL_PIXEL_MAP_I_TO_B || map==GL_PIXEL_MAP_I_TO_A)
		need_pot=1;
	if(need_pot && npot(mapsize)!=mapsize) {
		errorShim(GL_INVALID_VALUE);
		return;
	}
	int wf = 1;
	void* array = NULL;
	int* size = NULL;
	if(!map_pixelmap(map, &wf, &size, &array))
		return;
	noerrorShim();
	if(wf) {
		GLfloat *p = (GLfloat*)array;
		for (int i=0; i<mapsize; i++)
			p[i] = values[i]>>24;
	} else {
		GLuint *p = (GLuint*)array;
		for (int i=0; i<mapsize; i++)
			p[i] = values[i];
	}
	*size = mapsize;
}

void APIENTRY_GL4ES gl4es_glPixelMapusv(GLenum map,GLsizei mapsize, const GLushort *values) {
	if(mapsize>MAX_MAP_SIZE) {
		errorShim(GL_INVALID_VALUE);
		return;
	}
	int need_pot = 0;
	if(map==GL_PIXEL_MAP_I_TO_I || map==GL_PIXEL_MAP_S_TO_S 
		|| map==GL_PIXEL_MAP_I_TO_R || map==GL_PIXEL_MAP_I_TO_G
		|| map==GL_PIXEL_MAP_I_TO_B || map==GL_PIXEL_MAP_I_TO_A)
		need_pot=1;
	if(need_pot && npot(mapsize)!=mapsize) {
		errorShim(GL_INVALID_VALUE);
		return;
	}
	int wf = 1;
	void* array = NULL;
	int* size = NULL;
	if(!map_pixelmap(map, &wf, &size, &array))
		return;
	noerrorShim();
	if(wf) {
		GLfloat *p = (GLfloat*)array;
		for (int i=0; i<mapsize; i++)
			p[i] = values[i]>>8;
	} else {
		GLuint *p = (GLuint*)array;
		for (int i=0; i<mapsize; i++)
			p[i] = values[i];
	}
	*size = mapsize;
}
void APIENTRY_GL4ES gl4es_glGetPixelMapfv(GLenum map, GLfloat *data) {
	int wf = 1;
	void* array = NULL;
	int* size = NULL;
	if(!map_pixelmap(map, &wf, &size, &array))
		return;
	noerrorShim();
	if(wf) {
		GLfloat *p = (GLfloat*)array;
		for (int i=0; i<*size; i++)
			data[i] = p[i]/255.0f;
	} else {
		GLuint *p = (GLuint*)array;
		for (int i=0; i<*size; i++)
			data[i] = p[i];
	}
}
void APIENTRY_GL4ES gl4es_glGetPixelMapuiv(GLenum map, GLuint *data) {
	int wf = 1;
	void* array = NULL;
	int* size = NULL;
	if(!map_pixelmap(map, &wf, &size, &array))
		return;
	noerrorShim();
	if(wf) {
		GLfloat *p = (GLfloat*)array;
		for (int i=0; i<*size; i++)
			data[i] = ((GLuint)(p[i]))<<24;
	} else {
		GLuint *p = (GLuint*)array;
		for (int i=0; i<*size; i++)
			data[i] = p[i];
	}
}
void APIENTRY_GL4ES gl4es_glGetPixelMapusv(GLenum map, GLushort *data) {
	int wf = 1;
	void* array = NULL;
	int* size = NULL;
	if(!map_pixelmap(map, &wf, &size, &array))
		return;
	noerrorShim();
	if(wf) {
		GLfloat *p = (GLfloat*)array;
		for (int i=0; i<*size; i++)
			data[i] = ((GLuint)(p[i]))<<8;
	} else {
		GLuint *p = (GLuint*)array;
		for (int i=0; i<*size; i++)
			data[i] = p[i];
	}
}


//Direct wrapper
AliasExport(void,glBitmap,,(GLsizei width, GLsizei height, GLfloat xorig, GLfloat yorig, GLfloat xmove, GLfloat ymove, const GLubyte *bitmap));
AliasExport(void,glDrawPixels,,(GLsizei width, GLsizei height, GLenum format, GLenum type, const GLvoid *data));
AliasExport(void,glRasterPos3f,,(GLfloat x, GLfloat y, GLfloat z));
AliasExport(void,glWindowPos3f,,(GLfloat x, GLfloat y, GLfloat z));
AliasExport(void,glViewport,,(GLint x, GLint y, GLsizei width, GLsizei height));
AliasExport(void,glScissor,,(GLint x, GLint y, GLsizei width, GLsizei height));
AliasExport(void,glPixelZoom,,(GLfloat xfactor, GLfloat yfactor));
AliasExport(void,glPixelTransferf,,(GLenum pname, GLfloat param));
AliasExport(void,glPixelMapfv,,(GLenum map, GLsizei mapsize, const GLfloat *values));
AliasExport(void,glPixelMapuiv,,(GLenum map,GLsizei mapsize, const GLuint *values));
AliasExport(void,glPixelMapusv,,(GLenum map,GLsizei mapsize, const GLushort *values));
AliasExport(void,glGetPixelMapfv,,(GLenum map, GLfloat *data));
AliasExport(void,glGetPixelMapuiv,,(GLenum map, GLuint *data));
AliasExport(void,glGetPixelMapusv,,(GLenum map, GLushort *data));
