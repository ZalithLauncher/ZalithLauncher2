#include "logs.h"
#include "init.h"
#include <stdarg.h>
#if defined(ANDROID) && defined(USE_ANDROID_LOG)
#include <android/log.h>
#endif
//----------------------------------------------------------------------------
static const char * const log_prefix="LIBGL: ";
//----------------------------------------------------------------------------
void LogPrintf_NoPrefix(const char *fmt,...)
{
	va_list args;
	va_start(args,fmt);
	#if defined(ANDROID) && defined(USE_ANDROID_LOG)
	__android_log_vprint(ANDROID_LOG_INFO, "LIBGL", fmt, args);
	#else
	vprintf(fmt,args);
	#endif
	va_end(args);
}
//----------------------------------------------------------------------------
void LogFPrintf(FILE *fp,const char *fmt,...)
{
	#ifndef ANDROID
	fprintf(fp,log_prefix);
	#endif
	va_list args;
	va_start(args,fmt);
	#if defined(ANDROID) && defined(USE_ANDROID_LOG)
	// also on logcat
	__android_log_vprint(ANDROID_LOG_INFO, "LIBGL", fmt, args);
	#endif
	vfprintf(fp,fmt,args);
	va_end(args);
}
//----------------------------------------------------------------------------
void LogPrintf(const char *fmt,...)
{
	#ifndef ANDROID
	printf(log_prefix);
	#endif
	va_list args;
	va_start(args,fmt);
	#if defined(ANDROID) && defined(USE_ANDROID_LOG)
	__android_log_vprint(ANDROID_LOG_INFO, "LIBGL", fmt, args);
	#else
	vprintf(fmt,args);
	#endif
	va_end(args);
}
//----------------------------------------------------------------------------
