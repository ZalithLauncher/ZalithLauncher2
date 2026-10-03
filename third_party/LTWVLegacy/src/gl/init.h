#ifndef _GL4ES_INIT_H_
#define _GL4ES_INIT_H_

#if defined(PANDORA)
#define USE_FBIO 1
#endif

typedef struct _globals4es {
 int nobanner;
 int mergelist;
 int xrefresh;
 int stacktrace;
 int usefb;
 int usegbm;
 int usefbo;
 int recyclefbo;
 int usepbuffer;
 int showfps;
 int vsync;
 int automipmap;
 int texcopydata;
 int tested_env;
 int texshrink;
 int texdump;
 int alphahack;
 int texstream;
 int nolumalpha;
 int blendhack;
 int blendcolor;
 int noerror;
 int npot;
 int defaultwrap;
 int notexrect;
 int queries;
 int silentstub;
 int glx_surface_srgb;
 int nodownsampling;
 int vabgra;
 int nobgra;
 int potframebuffer;
 float gamma;
 int texmat;
 int novaocache;
 int beginend;
 int avoid16bits;
 int avoid24bits;
 int force16bits;
 int nohighp;
 int minbatch;
 int maxbatch;
 int es;
 int gl;
 int usevbo;
 int comments;
 int forcenpot;
 int fbomakecurrent;    // hack to bind/unbind FBO when doing glXMakeCurrent
 int fbounbind;         // hack to bind/unbind fbo if a bind texture is used for drawing
 int fboforcetex;       // force texture attachment for Color0
 int blitfullscreen;    // a "fullscreen" blit to FB0 means SwapBuffers
 int notexarray;
 int nodepthtex;
 int logshader;
 int shadernogles;
 int floattex;
 int glxrecycle;
 int noclean;
 int dbgshaderconv;
 int nopsa;
 int noes2;
 int nointovlhack;
 int noshaderlod;
 int fbo_noalpha;
 int noarbprogram;      // to disable ARB Program
 int glxnative;
 int normalize;         // force normal normalization (workaround a bug)
 int blitfb0;
 int skiptexcopies;
 int shaderblend;
 int deepbind;
 float fbtexscale;
 #ifndef NO_GBM
 char drmcard[50];
 #endif
 char version[50];
} globals4es_t;

extern globals4es_t globals4es;

#endif // _GL4ES_INIT_H_
