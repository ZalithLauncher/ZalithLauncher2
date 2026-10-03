#ifndef _GLX_STREAMING_H_
#define _GLX_STREAMING_H_

#ifdef TEXSTREAM
#include "../gl/gl4es.h"

#ifndef GL_APIENTRYP
#define GL_APIENTRYP
#endif
#ifndef GL_IMG_texture_stream
#define GL_TEXTURE_STREAM_IMG                                   0x8C0D     
#define GL_TEXTURE_NUM_STREAM_DEVICES_IMG                       0x8C0E     
#define GL_TEXTURE_STREAM_DEVICE_WIDTH_IMG                      0x8C0F
#define GL_TEXTURE_STREAM_DEVICE_HEIGHT_IMG                     0x8EA0     
#define GL_TEXTURE_STREAM_DEVICE_FORMAT_IMG                     0x8EA1      
#define GL_TEXTURE_STREAM_DEVICE_NUM_BUFFERS_IMG                0x8EA2     
typedef void (GL_APIENTRYP PFNGLTEXBINDSTREAMIMGPROC) (GLint device, GLint deviceoffset);
typedef const GLubyte *(GL_APIENTRYP PFNGLGETTEXSTREAMDEVICENAMEIMGPROC) (GLenum target);
typedef void (GL_APIENTRYP PFNGLGETTEXSTREAMDEVICEATTRIBUTEIVIMGPROC) (GLenum target, GLenum pname, GLint *params);
#define GL_IMG_texture_stream 1
#endif

extern PFNGLTEXBINDSTREAMIMGPROC *glTexBindStreamIMG;
extern PFNGLGETTEXSTREAMDEVICEATTRIBUTEIVIMGPROC *glGetTexAttrIMG;
extern PFNGLGETTEXSTREAMDEVICENAMEIMGPROC *glGetTexDeviceIMG;

extern int gl_stream;		//0 if no streaming not 0 if streaming available

// Function to start the Streaming texture Cache. Return 0 if failed, non-0 if OK.
int InitStreamingCache();
// Function to get a Streaming buffer address
void* GetStreamingBuffer(int buff);
// Function to add a new texture of size Width*Height, with fake Texture ID "ID". Return the StreamingID or -1 if failed.
int AddStreamed(int width, int height, unsigned int ID);
// Function to free a streamed texture ID
void FreeStreamed(int ID);
// Function to find a StreamingID with a TextureID. G-1 if not found
int FindTexID(unsigned int ID);
// Function to apply Min& Mag filter to Streaming texture
void ApplyFilterID(int ID, GLenum min_filter, GLenum mag_filter);
// Function to activate the Steaming texture ID on current tex...
void ActivateStreaming(int ID);
// Function to deactivate the Streaming texture on current tex...
void DeactivateStreaming();
#endif // TEXSTREAM
#endif // _GLX_STREAMING_H_
