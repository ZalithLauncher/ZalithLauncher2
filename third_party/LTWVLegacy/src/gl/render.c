#include "render.h"

#include "array.h"
#include "init.h"
#include "matrix.h"

void push_hit() {
    // push current hit to hit list, and re-init current hit
    if (glstate->selectbuf.hit) {
        if (!glstate->selectbuf.overflow) {
			//Normalize zmin/zmax
			if((glstate->selectbuf.zmaxoverall - glstate->selectbuf.zminoverall)!=0.0f) {
				glstate->selectbuf.zmin = (glstate->selectbuf.zmin-glstate->selectbuf.zminoverall)/(glstate->selectbuf.zmaxoverall - glstate->selectbuf.zminoverall);
				glstate->selectbuf.zmax = (glstate->selectbuf.zmax-glstate->selectbuf.zminoverall)/(glstate->selectbuf.zmaxoverall - glstate->selectbuf.zminoverall);
			}
            int tocopy = glstate->namestack.top + 3;
            if (tocopy+glstate->selectbuf.pos > glstate->selectbuf.size) {
                glstate->selectbuf.overflow = 1;
                tocopy = glstate->selectbuf.size - glstate->selectbuf.pos;
            }
            if(tocopy>0)
                glstate->selectbuf.buffer[glstate->selectbuf.pos+0] = glstate->namestack.top;
            if(tocopy>1)
                glstate->selectbuf.buffer[glstate->selectbuf.pos+1] = (unsigned int)(glstate->selectbuf.zmin * INT_MAX );
            if(tocopy>2)
                glstate->selectbuf.buffer[glstate->selectbuf.pos+2] = (unsigned int)(glstate->selectbuf.zmax * INT_MAX );
            if(tocopy>3)
                memcpy(glstate->selectbuf.buffer + glstate->selectbuf.pos + 3, glstate->namestack.names, (tocopy-3) * sizeof(GLuint));

            glstate->selectbuf.count++;
            glstate->selectbuf.pos += tocopy;
        }
        glstate->selectbuf.hit = 0;
    }
    glstate->selectbuf.zmin = 1e10f;
    glstate->selectbuf.zmax = -1e10f;
    glstate->selectbuf.zminoverall = 1e10f;
    glstate->selectbuf.zmaxoverall = -1e10f;
}


GLint APIENTRY_GL4ES gl4es_glRenderMode(GLenum mode) {
	if(glstate->list.compiling) {errorShim(GL_INVALID_OPERATION); return 0;}
	FLUSH_BEGINEND;

	int ret = 0;
    if ((mode==GL_SELECT) || (mode==GL_RENDER)) {  // missing GL_FEEDBACK
        noerrorShim();
    } else {
        errorShim(GL_INVALID_ENUM);
        return 0;
    }
	if (glstate->render_mode == GL_SELECT) {
        push_hit();
		ret = glstate->selectbuf.count;
    }
	if (mode == GL_SELECT) {
		if (glstate->selectbuf.buffer == NULL)	{// error, cannot use Select Mode without select buffer
            errorShim(GL_INVALID_OPERATION);
			return 0;
        }
		glstate->selectbuf.count = 0;
        glstate->selectbuf.pos = 0;
        glstate->selectbuf.overflow = 0;
        glstate->selectbuf.zmin = 1e10f;
        glstate->selectbuf.zmax = -1e10f;
		glstate->selectbuf.zminoverall = 1e10f;
		glstate->selectbuf.zmaxoverall = -1e10f;
        glstate->selectbuf.hit = 0;
	}
    
	glstate->render_mode = mode;
	return ret;
}

void APIENTRY_GL4ES gl4es_glInitNames(void) {
	if(glstate->list.active) {
		NewStage(glstate->list.active, STAGE_RENDER);
		glstate->list.active->render_op = 1;
		return;
	}
	//TODO list stuffs
	if (glstate->namestack.names == 0) {
		glstate->namestack.names = (GLuint*)malloc(1024*sizeof(GLuint));
	}
	glstate->namestack.top = 0;
    noerrorShim();
}

void APIENTRY_GL4ES gl4es_glPopName(void) {
	FLUSH_BEGINEND;
	if(glstate->list.active) {
		NewStage(glstate->list.active, STAGE_RENDER);
		glstate->list.active->render_op = 2;
		return;
	}
    noerrorShim();
	if (glstate->render_mode != GL_SELECT)
		return;
    push_hit();
	if (glstate->namestack.top>0)
		glstate->namestack.top--;
    else
        errorShim(GL_STACK_UNDERFLOW);
}

void APIENTRY_GL4ES gl4es_glPushName(GLuint name) {
	FLUSH_BEGINEND;
	if(glstate->list.active) {
		NewStage(glstate->list.active, STAGE_RENDER);
		glstate->list.active->render_op = 3;
		glstate->list.active->render_arg = name;
		return;
	}
    noerrorShim();
	if (glstate->render_mode != GL_SELECT)
		return;
	if (glstate->namestack.names==0)
		return;
    push_hit();
	if (glstate->namestack.top < 1024) {
		glstate->namestack.names[glstate->namestack.top++] = name;
	}
}

void APIENTRY_GL4ES gl4es_glLoadName(GLuint name) {
	FLUSH_BEGINEND;
	if(glstate->list.active) {
		NewStage(glstate->list.active, STAGE_RENDER);
		glstate->list.active->render_op = 4;
		glstate->list.active->render_arg = name;
		return;
	}
    noerrorShim();
	if (glstate->render_mode != GL_SELECT)
		return;
	if (glstate->namestack.names == 0)
		return;
    push_hit();
    if (glstate->namestack.top == 0)
        return;
    glstate->namestack.names[glstate->namestack.top-1] = name;
}

void APIENTRY_GL4ES gl4es_glSelectBuffer(GLsizei size, GLuint *buffer) {
    FLUSH_BEGINEND;
		
    noerrorShim();
	glstate->selectbuf.buffer = buffer;
	glstate->selectbuf.size = size;
}

void select_transform(GLfloat *a) {
	/*
	 Transform a[3] using projection and modelview matrix
	*/
	vector_matrix(a, getMVPMat(), a);
	// take "w" into account...
	a[0]/=a[3];
	a[1]/=a[3];
	a[2]/=a[3];
}

GLboolean select_point_in_viewscreen(const GLfloat *a) {
	/* 
	 Return True is point is inside the Viewport
	*/
    if (a[0]>-1.0 && a[0]<1.0 && a[1]>-1.0 && a[1]<1.0) {
	 return true;
    }
    return false;

}

GLboolean select_segment_in_viewscreen(const GLfloat *a, const GLfloat *b) {
	/*
	 Return True is the segment is fully inside viewscreen
	 or cross the viewscreen
	 Viewscreen is  (-1,-1)(+1,+1) 
	 False if completly outside
	*/
	// Fast either point inside viewport
	if (select_point_in_viewscreen(a)) return true;
	if (select_point_in_viewscreen(b)) return true;
	// Using Liang-Barsky algorithm
	GLfloat vx, vy;
	vx=b[0]-a[0];
	vy=b[1]-a[1];
	GLfloat p[4] = {-vx, vx, -vy, vy};
	GLfloat q[4] = {a[0] + 1.0f, +1.0f - a[0], a[1] + 1.0f, +1.0f - a[1]};
	GLfloat u1 = 0.0f;
	GLfloat u2 = 1.0f;

	for (int i=0; i<4; i++) {
		if (p[i] == 0.0f) {
			if (q[i]<0.0f)
				return false;
		} else {
			GLfloat t =q[i] / p[i];
			if (p[i]<0.0) { 
                if(t>u2) return false;
                else if(u1<t)
                    u1 = t;
            }
			else if (p[i]>0.0) {
                if(t<u1) return false;
                else if (u2>t)
                    u2 = t;
            }
		}
	}
	return true;
}

GLboolean select_triangle_in_viewscreen(const GLfloat *a, const GLfloat *b, const GLfloat *c) {
	/*
	 Return True is the triangle is in the viewscreen, or completly include, or include the viewscreen
	*/
	 // Check if any segment intersect the viewscreen (include test if any point is inside the viewscreen)
	 if (select_segment_in_viewscreen(a, b)) return true;
	 if (select_segment_in_viewscreen(b, c)) return true;
	 if (select_segment_in_viewscreen(c, a)) return true;

	 // Now check if the viewscreen is completly inside the triangle
	 #define sign(p1, p2, p3) (p1[0]-p3[0])*(p2[1]-p3[1])-(p2[0]-p3[0])*(p1[1]-p3[1])
	 for (int i=0; i<4; i++) {
	 	GLboolean b1,b2,b3;
	 	GLfloat pt[2];
	 	pt[0] = (i%2)?-1.0f:+1.0f;
	 	pt[1] = (i>2)?-1.0f:+1.0f;
	 	b1 = (sign(pt, a, b))<0.0f;
	 	b2 = (sign(pt, b, c))<0.0f;
	 	b3 = (sign(pt, c, a))<0.0f;
	 	if ((b1==b2) && (b2==b3)) {
	 		return true;
	 	}
	 }
	 #undef sign

	 return false;
}

static void FASTMATH ZMinMax(GLfloat *zmin, GLfloat *zmax, GLfloat *vtx) {
	if (vtx[2]<*zmin) *zmin=vtx[2];
	if (vtx[2]>*zmax) *zmax=vtx[2];
}


void select_glDrawArrays(const vertexattrib_t* vtx, GLenum mode, GLuint first, GLuint count) {
	if (count == 0) return;
	if (vtx->pointer == NULL) return;
	if (glstate->selectbuf.buffer == NULL) return;
	GLfloat *vert = copy_gl_array(vtx->pointer, vtx->type, 
			vtx->size, vtx->stride,
			GL_FLOAT, 4, 0, count+first, NULL);
	GLfloat zmin=1e10f, zmax=-1e10f;
	int found = 0;

	#define FOUND()	{ 				\
		found = 1;					\
		glstate->selectbuf.hit = 1; \
	}

    // transform the points
	for (int i=first; i<count+first; i++) {
		select_transform(vert+i*4);
		ZMinMax(&glstate->selectbuf.zminoverall, &glstate->selectbuf.zmaxoverall, vert+i*4);
    }
    // intersect with screen now
    GLfloat *vert2 = vert + first*4;
	for (int i=0; i<count; i++) {
		switch (mode) {
			case GL_POINTS:
				if (select_point_in_viewscreen(vert2+i*4)) {
					ZMinMax(&zmin, &zmax, vert+i*4);
					FOUND();
				}
				break;
			case GL_LINES:
				if (i%2==1) {
					if (select_segment_in_viewscreen(vert2+(i-1)*4, vert2+i*4)) {
						ZMinMax(&zmin, &zmax, vert+(i-1)*4);
						ZMinMax(&zmin, &zmax, vert+i*4);
						FOUND();
					}
				}
				break;
			case GL_LINE_STRIP:
			case GL_LINE_LOOP:		//FIXME: the last "loop" segment is missing here
				if (i>0) {
					if (select_segment_in_viewscreen(vert2+(i-1)*4, vert2+i*4)) {
						ZMinMax(&zmin, &zmax, vert+(i-1)*4);
						ZMinMax(&zmin, &zmax, vert+i*4);
						FOUND();
					}
				}
				break;
			case GL_TRIANGLES:
				if (i%3==2) {
					if (select_triangle_in_viewscreen(vert2+(i-2)*4, vert2+(i-1)*4, vert2+i*4)) {
						ZMinMax(&zmin, &zmax, vert+(i-2)*4);
						ZMinMax(&zmin, &zmax, vert+(i-1)*4);
						ZMinMax(&zmin, &zmax, vert+i*4);
						FOUND();
					}
				}
				break;
			case GL_TRIANGLE_STRIP:
				if (i>1) {
					if (select_triangle_in_viewscreen(vert2+(i-2)*4, vert2+(i-1)*4, vert2+i*4)) {
						ZMinMax(&zmin, &zmax, vert+(i-2)*4);
						ZMinMax(&zmin, &zmax, vert+(i-1)*4);
						ZMinMax(&zmin, &zmax, vert+i*4);
						FOUND();
					}
				}
				break;
			case GL_TRIANGLE_FAN:
				if (i>1) {
					if (select_triangle_in_viewscreen(vert2, vert2+(i-1)*4, vert2+i*4)) {
						ZMinMax(&zmin, &zmax, vert);
						ZMinMax(&zmin, &zmax, vert+(i-1)*4);
						ZMinMax(&zmin, &zmax, vert+i*4);
						FOUND();
					}
				}
				break;
			default:
				return;		// Should never go there!
		}
	}
	free(vert);
	if(found) {
		if (zmin<glstate->selectbuf.zmin) 	glstate->selectbuf.zmin=zmin;
		if (zmax>glstate->selectbuf.zmax) 	glstate->selectbuf.zmax=zmax;
	}
	#undef FOUND
}

void select_glDrawElements(const vertexattrib_t* vtx, GLenum mode, GLuint count, GLenum type, GLvoid * indices) {
	if (count == 0) return;
	if (vtx->pointer == NULL) return;

	GLushort *sind = (GLushort*)((type==GL_UNSIGNED_SHORT)?indices:NULL);
	GLuint *iind = (GLuint*)((type==GL_UNSIGNED_INT)?indices:NULL);

	GLsizei min, max;
	if(sind)
		getminmax_indices_us(sind, &max, &min, count);
	else
		getminmax_indices_ui(iind, &max, &min, count);
    max++;
	GLfloat *vert = copy_gl_array(vtx->pointer, vtx->type, 
			vtx->size, vtx->stride,
			GL_FLOAT, 4, 0, max, NULL);
	GLfloat zmin=1e10f, zmax=-10e6f;
	int found = 0;
	for (int i=min; i<max; i++) {
		select_transform(vert+i*4);
		ZMinMax(&glstate->selectbuf.zminoverall, &glstate->selectbuf.zmaxoverall, vert+i*4);
	}

	#define FOUND()	{ 				\
		found = 1;					\
		glstate->selectbuf.hit = 1; \
		}

	if(sind) {
		for (int i=0; i<count; i++) {
			switch (mode) {
				case GL_POINTS:
					if (select_point_in_viewscreen(vert+sind[i]*4)) {
						ZMinMax(&zmin, &zmax, vert+sind[i]*4);
						FOUND();
					}
					break;
				case GL_LINES:
					if (i%2==1) {
						if (select_segment_in_viewscreen(vert+sind[(i-1)]*4, vert+sind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+sind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_LINE_STRIP:
				case GL_LINE_LOOP:		//FIXME: the last "loop" segment is missing here
					if (i>0) {
						if (select_segment_in_viewscreen(vert+sind[(i-1)]*4, vert+sind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+sind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_TRIANGLES:
					if (i%3==2) {
						if (select_triangle_in_viewscreen(vert+sind[(i-2)]*4, vert+sind[(i-1)]*4, vert+sind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+sind[i-2]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_TRIANGLE_STRIP:
					if (i>1) {
						if (select_triangle_in_viewscreen(vert+sind[(i-2)]*4, vert+sind[(i-1)]*4, vert+sind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+sind[i-2]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_TRIANGLE_FAN:
					if (i>1) {
						if (select_triangle_in_viewscreen(vert+sind[0]*4, vert+sind[(i-1)]*4, vert+sind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+sind[0]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+sind[i]*4);
							FOUND();
						}	
					}
					break;
				default:
					return;		// Should never go there!
			}
		} 
	} else {
		for (int i=0; i<count; i++) {
			switch (mode) {
				case GL_POINTS:
					if (select_point_in_viewscreen(vert+iind[i]*4)) {
						ZMinMax(&zmin, &zmax, vert+iind[i]*4);
						FOUND();
					}
					break;
				case GL_LINES:
					if (i%2==1) {
						if (select_segment_in_viewscreen(vert+iind[(i-1)]*4, vert+iind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+iind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_LINE_STRIP:
				case GL_LINE_LOOP:		//FIXME: the last "loop" segment is missing here
					if (i>0) {
						if (select_segment_in_viewscreen(vert+iind[(i-1)]*4, vert+iind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+iind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_TRIANGLES:
					if (i%3==2) {
						if (select_triangle_in_viewscreen(vert+iind[(i-2)]*4, vert+iind[(i-1)]*4, vert+iind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+iind[i-2]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_TRIANGLE_STRIP:
					if (i>1) {
						if (select_triangle_in_viewscreen(vert+iind[(i-2)]*4, vert+iind[(i-1)]*4, vert+iind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+iind[i-2]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i]*4);
							FOUND();
						}
					}
					break;
				case GL_TRIANGLE_FAN:
					if (i>1) {
						if (select_triangle_in_viewscreen(vert+iind[0]*4, vert+iind[(i-1)]*4, vert+iind[i]*4)) {
							ZMinMax(&zmin, &zmax, vert+iind[0]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i-1]*4);
							ZMinMax(&zmin, &zmax, vert+iind[i]*4);
							FOUND();
						}
					}
					break;
				default:
					return;		// Should never go there!
			}
		}
	}
	free(vert);
	if(found) {
		if (zmin<glstate->selectbuf.zmin) 	glstate->selectbuf.zmin=zmin;
		if (zmax>glstate->selectbuf.zmax) 	glstate->selectbuf.zmax=zmax;
	}

	#undef FOUND
}

//Direct wrapper
AliasExport(GLint,glRenderMode,,(GLenum mode));
AliasExport_V(void,glInitNames);
AliasExport_V(void,glPopName);
AliasExport(void,glPushName,,(GLuint name));
AliasExport(void,glLoadName,,(GLuint name));
AliasExport(void,glSelectBuffer,,(GLsizei size, GLuint *buffer));
