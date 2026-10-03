#ifndef _GLX_GLX_H_
#define _GLX_GLX_H_

#ifndef NOEGL
#include <EGL/egl.h>
#endif
#include <stdbool.h>
#include <stdlib.h>
#ifndef NOX11
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#else
typedef void Display;
typedef int Bool;
#endif
#include <stdio.h>
#include <string.h>

#include "../gl/gl4es.h"

// defines yoinked from Mesa glx.h
#define GLX_VERSION_1_1     1
#define GLX_VERSION_1_2     1
#define GLX_VERSION_1_3     1
#define GLX_VERSION_1_4     1

#define GLX_EXTENSION_NAME   "GLX"

/*
 * Tokens for glXChooseVisual and glXGetConfig:
 */
#define GLX_USE_GL      1
#define GLX_BUFFER_SIZE     2
#define GLX_LEVEL       3
#define GLX_RGBA        4
#define GLX_DOUBLEBUFFER    5
#define GLX_STEREO      6
#define GLX_AUX_BUFFERS     7
#define GLX_RED_SIZE        8
#define GLX_GREEN_SIZE      9
#define GLX_BLUE_SIZE       10
#define GLX_ALPHA_SIZE      11
#define GLX_DEPTH_SIZE      12
#define GLX_STENCIL_SIZE    13
#define GLX_ACCUM_RED_SIZE  14
#define GLX_ACCUM_GREEN_SIZE    15
#define GLX_ACCUM_BLUE_SIZE 16
#define GLX_ACCUM_ALPHA_SIZE    17

/*
 * Error codes returned by glXGetConfig:
 */
#define GLX_BAD_SCREEN      1
#define GLX_BAD_ATTRIBUTE   2
#define GLX_NO_EXTENSION    3
#define GLX_BAD_VISUAL      4
#define GLX_BAD_CONTEXT     5
#define GLX_BAD_VALUE           6
#define GLX_BAD_ENUM        7

/*
 * GLX 1.1 and later:
 */
#define GLX_VENDOR      1
#define GLX_VERSION     2
#define GLX_EXTENSIONS      3

/*
 * GLX 1.3 and later:
 */
#define GLX_CONFIG_CAVEAT       0x20
#define GLX_DONT_CARE           0xFFFFFFFF
#define GLX_X_VISUAL_TYPE       0x22
#define GLX_TRANSPARENT_TYPE        0x23
#define GLX_TRANSPARENT_INDEX_VALUE 0x24
#define GLX_TRANSPARENT_RED_VALUE   0x25
#define GLX_TRANSPARENT_GREEN_VALUE 0x26
#define GLX_TRANSPARENT_BLUE_VALUE  0x27
#define GLX_TRANSPARENT_ALPHA_VALUE 0x28
#define GLX_WINDOW_BIT          0x00000001
#define GLX_PIXMAP_BIT          0x00000002
#define GLX_PBUFFER_BIT         0x00000004
#define GLX_AUX_BUFFERS_BIT     0x00000010
#define GLX_FRONT_LEFT_BUFFER_BIT   0x00000001
#define GLX_FRONT_RIGHT_BUFFER_BIT  0x00000002
#define GLX_BACK_LEFT_BUFFER_BIT    0x00000004
#define GLX_BACK_RIGHT_BUFFER_BIT   0x00000008
#define GLX_DEPTH_BUFFER_BIT        0x00000020
#define GLX_STENCIL_BUFFER_BIT      0x00000040
#define GLX_ACCUM_BUFFER_BIT        0x00000080
#define GLX_NONE            0x8000
#define GLX_SLOW_CONFIG         0x8001
#define GLX_TRUE_COLOR          0x8002
#define GLX_DIRECT_COLOR        0x8003
#define GLX_PSEUDO_COLOR        0x8004
#define GLX_STATIC_COLOR        0x8005
#define GLX_GRAY_SCALE          0x8006
#define GLX_STATIC_GRAY         0x8007
#define GLX_TRANSPARENT_RGB     0x8008
#define GLX_TRANSPARENT_INDEX       0x8009
#define GLX_VISUAL_ID           0x800B
#define GLX_SCREEN          0x800C
#define GLX_NON_CONFORMANT_CONFIG   0x800D
#define GLX_DRAWABLE_TYPE       0x8010
#define GLX_RENDER_TYPE         0x8011
#define GLX_X_RENDERABLE        0x8012
#define GLX_FBCONFIG_ID         0x8013
#define GLX_RGBA_TYPE           0x8014
#define GLX_COLOR_INDEX_TYPE        0x8015
#define GLX_MAX_PBUFFER_WIDTH       0x8016
#define GLX_MAX_PBUFFER_HEIGHT      0x8017
#define GLX_MAX_PBUFFER_PIXELS      0x8018
#define GLX_PRESERVED_CONTENTS      0x801B
#define GLX_LARGEST_PBUFFER     0x801C
#define GLX_WIDTH           0x801D
#define GLX_HEIGHT          0x801E
#define GLX_EVENT_MASK          0x801F
#define GLX_DAMAGED         0x8020
#define GLX_SAVED           0x8021
#define GLX_WINDOW          0x8022
#define GLX_PBUFFER         0x8023
#define GLX_PBUFFER_HEIGHT              0x8040
#define GLX_PBUFFER_WIDTH               0x8041
#define GLX_RGBA_BIT            0x00000001
#define GLX_COLOR_INDEX_BIT     0x00000002
#define GLX_PBUFFER_CLOBBER_MASK    0x08000000

/*
 * GLX 1.4 and later:
 */
#define GLX_SAMPLE_BUFFERS              0x186a0 /*100000*/
#define GLX_SAMPLES                     0x186a1 /*100001*/

#define GLX_FRAMEBUFFER_SRGB_CAPABLE_ARB 0x20B2

#define GLX_SWAP_INTERVAL_EXT           0x20F1
#define GLX_MAX_SWAP_INTERVAL_EXT       0x20F2

#define GLX_CONTEXT_MAJOR_VERSION_ARB           0x2091
#define GLX_CONTEXT_MINOR_VERSION_ARB           0x2092
#define GLX_CONTEXT_FLAGS_ARB                   0x2094
#define GLX_CONTEXT_PROFILE_MASK_ARB            0x9126
#define GLX_CONTEXT_DEBUG_BIT_ARB               0x0001
#define GLX_CONTEXT_FORWARD_COMPATIBLE_BIT_ARB  0x0002
#define GLX_CONTEXT_CORE_PROFILE_BIT_ARB        0x00000001
#define GLX_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB 0x00000002
#define GLX_CONTEXT_ES_PROFILE_BIT_EXT		    0x00000004
#define GLX_CONTEXT_ES2_PROFILE_BIT_EXT		    0x00000004

#ifdef NOX11
typedef int GLXDrawable;    // need 64 / 32bits difs.
#else
typedef XID GLXDrawable;
#endif
#ifndef NOX11
struct __GLXContextRec {
    Display *display;
    GLXDrawable drawable;
    unsigned char direct;
    int currentWritable;
    int currentReadable;
    XID xid;
	EGLSurface eglSurface;
	EGLConfig eglConfigs[64];
    int eglConfigsCount;
    int eglconfigIdx;
	EGLContext eglContext;
    int samples;
    int samplebuffers;
    int depth;
    int stencil;
    int rbits, gbits, bbits, abits;
    int es2only;
    int doublebuff;
    void* glstate;
    struct __GLXContextRec* shared;
    int* shared_eglsurface;
    void* nativewin;
    int contextType;    // 0 = Window, 1 = PBuffer, 2 = PixmapBuffer, 3 = Emulated PixmapBuffer (with PBuffer)
};
typedef struct __GLXContextRec *GLXContext;

typedef XID GLXPbuffer;
typedef XID GLXPixmap;
#endif //NOX11
struct __GLXFBConfigRec {
    int visualType;
    int transparentType;
                                /*    colors are floats scaled to ints */
    int transparentRed, transparentGreen, transparentBlue, transparentAlpha;
    int transparentIndex;

    int visualCaveat;

    int associatedVisualId;
    int screen;

    int drawableType;
    int renderType;

    int maxPbufferWidth, maxPbufferHeight, maxPbufferPixels;
    int optimalPbufferWidth, optimalPbufferHeight;  /* for SGIX_pbuffer */

    int visualSelectGroup;  /* visuals grouped by select priority */
#ifndef NOEGL
    EGLConfig id;
#else
    int       id;
#endif
    unsigned char rgbMode;
    unsigned char colorIndexMode;
    unsigned char doubleBufferMode;
    unsigned char stereoMode;
    unsigned char haveAccumBuffer;
    unsigned char haveDepthBuffer;
    unsigned char haveStencilBuffer;

    /* The number of bits present in various buffers */
    int accumRedBits, accumGreenBits, accumBlueBits, accumAlphaBits;
    int depthBits;
    int stencilBits;
    int indexBits;
    int redBits, greenBits, blueBits, alphaBits;
    unsigned int redMask, greenMask, blueMask, alphaMask;

    unsigned int multiSampleSize; /* Number of samples per pixel (0 if no ms) */

    unsigned int nMultiSampleBuffers; /* Number of available ms buffers */
    int maxAuxBuffers;

    /* frame buffer level */
    int level;

    /* color ranges (for SGI_color_range) */
    unsigned char extendedRange;
    double minRed, maxRed;
    double minGreen, maxGreen;
    double minBlue, maxBlue;
    double minAlpha, maxAlpha;
};

#ifdef NOX11
// Declare stub types
typedef void *XVisualInfo, *GLXContext, *Font, *Window, *Pixmap;
typedef unsigned int GLXPixmap, GLXPbuffer;
#endif

typedef struct __GLXFBConfigRec *GLXFBConfig;
#if !defined(NOX11) || defined(GLX_STUBS)
GLXContext gl4es_glXCreateContext(Display *dpy,
                            XVisualInfo *visual,
                            GLXContext shareList,
                            Bool direct);

GLXContext gl4es_glXCreateContextAttribsARB(Display *display, GLXFBConfig config,
                                      GLXContext share_context, Bool direct,
                                      const int *attrib_list);

void gl4es_glXSwapIntervalEXT(Display *display, int drawable, int interval);
#endif // !defined(NOX11) || defined(GLX_STUBS)
void gl4es_glXSwapInterval(int interval);

// GLX 1.1?
#if !defined(NOX11) || defined(GLX_STUBS)
Bool gl4es_glXIsDirect(Display * display, GLXContext ctx);
Bool gl4es_glXMakeCurrent(Display *display, GLXDrawable drawable, GLXContext context);
Bool gl4es_glXQueryExtension(Display *display, int *errorBase, int *eventBase);
Bool gl4es_glXQueryVersion(Display *display, int *major, int *minor);
const char *gl4es_glXGetClientString(Display *display, int name);
const char *gl4es_glXQueryExtensionsString(Display *display, int screen);
const char *gl4es_glXQueryServerString(Display *display, int screen, int name);
#endif
GLXDrawable gl4es_glXGetCurrentDrawable();
#if !defined(NOX11) || defined(GLX_STUBS)
int gl4es_glXGetConfig(Display *display, XVisualInfo *visual, int attribute, int *value);
void gl4es_glXCopyContext(Display *display, GLXContext src, GLXContext dst, GLuint mask);
void gl4es_glXDestroyContext(Display *display, GLXContext ctx);
void gl4es_glXSwapBuffers(Display *display, GLXDrawable drawable);
void gl4es_glXUseXFont(Font font, int first, int count, int listBase);
#endif // !defined(NOX11) || defined(GLX_STUBS)
void gl4es_glXWaitGL();
void gl4es_glXWaitX();
#if !defined(NOX11) || defined(GLX_STUBS)
XVisualInfo *gl4es_glXChooseVisual(Display *display, int screen, int *attributes);
int gl4es_glXQueryDrawable(Display *dpy, GLXDrawable draw, int attribute,	unsigned int *value);

// GLX 1.2
Display *gl4es_glXGetCurrentDisplay();

// GLX 1.3
GLXContext gl4es_glXGetCurrentContext();
XVisualInfo *gl4es_glXGetVisualFromFBConfig(Display *display, GLXFBConfig config);
GLXFBConfig *gl4es_glXChooseFBConfig(Display *display, int screen, const int *attrib_list, int *count);
GLXFBConfig *gl4es_glXGetFBConfigs(Display *display, int screen, int *count);
int gl4es_glXGetFBConfigAttrib(Display *display, GLXFBConfig config, int attribute, int *value);
int gl4es_glXQueryContext(Display *display, GLXContext ctx, int attribute, int *value);
GLXFBConfig *gl4es_glXChooseFBConfigSGIX(Display *display, int screen, const int *attrib_list, int *count);

Window gl4es_glXCreateWindow(Display *display, GLXFBConfig config, Window win, int *attrib_list);
void gl4es_glXDestroyWindow(Display *display, void *win);

Bool gl4es_glXMakeContextCurrent(Display *display, int drawable, int readable, GLXContext context);
GLXContext gl4es_glXCreateNewContext(Display *display, GLXFBConfig config, int render_type, GLXContext share_list, Bool is_direct);

void gl4es_glXDestroyPbuffer(Display * dpy, GLXPbuffer pbuf);
GLXPbuffer gl4es_glXCreatePbuffer(Display * dpy, GLXFBConfig config, const int * attrib_list);

GLXPixmap gl4es_glXCreatePixmap(Display * dpy, GLXFBConfig config, Pixmap pixmap, const int * attrib_list);
void gl4es_glXDestroyPixmap(Display *display, void *pixmap);
GLXPixmap gl4es_glXCreateGLXPixmap(Display *display, XVisualInfo * visual, Pixmap pixmap);
void gl4es_glXDestroyGLXPixmap(Display *display, void *pixmap);

GLXContext gl4es_glXCreateContextAttribs(Display *dpy, GLXFBConfig config, GLXContext share_context, Bool direct, const int *attrib_list);
#endif // !defined(NOX11) || defined(GLX_STUBS)

#endif // _GLX_GLX_H
