#include <jni.h>
#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "logger/logger.h"

#include "utils.h"

typedef int (*Main_Function_t)(int, char**);
typedef void (*android_update_LD_LIBRARY_PATH_t)(char*);

long shared_awt_surface;

char** convert_to_char_array(JNIEnv *env, jobjectArray jstringArray) {
	int num_rows = (*env)->GetArrayLength(env, jstringArray);
	char **cArray = (char **) malloc(num_rows * sizeof(char*));
	jstring row;
	
	for (int i = 0; i < num_rows; i++) {
		row = (jstring) (*env)->GetObjectArrayElement(env, jstringArray, i);
		cArray[i] = (char*)(*env)->GetStringUTFChars(env, row, 0);
    }
	
    return cArray;
}

jobjectArray convert_from_char_array(JNIEnv *env, char **charArray, int num_rows) {
	jobjectArray resultArr = (*env)->NewObjectArray(env, num_rows, (*env)->FindClass(env, "java/lang/String"), NULL);
	jstring row;
	
	for (int i = 0; i < num_rows; i++) {
		row = (jstring) (*env)->NewStringUTF(env, charArray[i]);
		(*env)->SetObjectArrayElement(env, resultArr, i, row);
    }

	return resultArr;
}

void free_char_array(JNIEnv *env, jobjectArray jstringArray, const char **charArray) {
	int num_rows = (*env)->GetArrayLength(env, jstringArray);
	jstring row;
	
	for (int i = 0; i < num_rows; i++) {
		row = (jstring) (*env)->GetObjectArrayElement(env, jstringArray, i);
		(*env)->ReleaseStringUTFChars(env, row, charArray[i]);
	}
}

jstring convertStringJVM(JNIEnv* srcEnv, JNIEnv* dstEnv, jstring srcStr) {
    if (srcStr == NULL) {
        return NULL;
    }
    
    const char* srcStrC = (*srcEnv)->GetStringUTFChars(srcEnv, srcStr, 0);
    jstring dstStr = (*dstEnv)->NewStringUTF(dstEnv, srcStrC);
	(*srcEnv)->ReleaseStringUTFChars(srcEnv, srcStr, srcStrC);
    return dstStr;
}

JNIEXPORT jlong JNICALL Java_android_view_Surface_nativeGetBridgeSurfaceAWT(JNIEnv *env, jclass clazz) {
	return (jlong) shared_awt_surface;
}

JNIEXPORT jint JNICALL Java_android_os_OpenJDKNativeRegister_nativeRegisterNatives(JNIEnv *env, jclass clazz, jstring registerSymbol) {
	const char *register_symbol_c = (*env)->GetStringUTFChars(env, registerSymbol, 0);
	void *symbol = dlsym(RTLD_DEFAULT, register_symbol_c);
	if (symbol == NULL) {
		printf("dlsym %s failed: %s\n", register_symbol_c, dlerror());
		return -1;
	}
	
	int (*registerNativesForClass)(JNIEnv*) = symbol;
	int result = registerNativesForClass(env);
	(*env)->ReleaseStringUTFChars(env, registerSymbol, register_symbol_c);
	
	return (jint) result;
}

JNIEXPORT void JNICALL Java_com_movtery_zalithlauncher_bridge_ZLBridge_setLdLibraryPath(JNIEnv *env, jclass clazz, jstring ldLibraryPath) {
	// jclass exception_cls = (*env)->FindClass(env, "java/lang/UnsatisfiedLinkError");
	
	android_update_LD_LIBRARY_PATH_t android_update_LD_LIBRARY_PATH;
	
	void *libdl_handle = dlopen("libdl.so", RTLD_LAZY);
	void *updateLdLibPath = dlsym(libdl_handle, "android_update_LD_LIBRARY_PATH");
	if (updateLdLibPath == NULL) {
		updateLdLibPath = dlsym(libdl_handle, "__loader_android_update_LD_LIBRARY_PATH");
		if (updateLdLibPath == NULL) {
			char *dl_error_c = dlerror();
			LOG_TO_E("Error getting symbol android_update_LD_LIBRARY_PATH: %s", dl_error_c);
			// (*env)->ThrowNew(env, exception_cls, dl_error_c);
		}
	}
	
	android_update_LD_LIBRARY_PATH = (android_update_LD_LIBRARY_PATH_t) updateLdLibPath;
	const char* ldLibPathUtf = (*env)->GetStringUTFChars(env, ldLibraryPath, 0);
	android_update_LD_LIBRARY_PATH(ldLibPathUtf);
	(*env)->ReleaseStringUTFChars(env, ldLibraryPath, ldLibPathUtf);
}

JNIEXPORT jboolean JNICALL Java_com_movtery_zalithlauncher_bridge_ZLBridge_dlopen(JNIEnv *env, jclass clazz, jstring name) {
	const char *nameUtf = (*env)->GetStringUTFChars(env, name, 0);
	void* handle = dlopen(nameUtf, RTLD_GLOBAL | RTLD_LAZY);
	if (!handle) {
		LOG_TO_E("DLOPEN: %s , failed ( %s )", nameUtf, dlerror());
	} else {
		LOG_TO_D("DLOPEN: %s , success", nameUtf);
	}
	(*env)->ReleaseStringUTFChars(env, name, nameUtf);
	return handle != NULL;
}

JNIEXPORT jlong JNICALL Java_com_movtery_zalithlauncher_bridge_ZLBridge_getJavaVMPointer(JNIEnv *env, jclass clazz) {
	if (pojav_environ->dalvikJavaVMPtr == NULL) return 0;
	return (jlong) (intptr_t) pojav_environ->dalvikJavaVMPtr;
}

JNIEXPORT jstring JNICALL Java_com_movtery_zalithlauncher_bridge_ZLBridge_jObjectToString(JNIEnv *env, jclass clazz, jobject object) {
	if (object == NULL) return NULL;
	jobject global_ref = (*env)->NewGlobalRef(env, object);
	if (global_ref == NULL) return NULL;

	char buf[32];
	snprintf(buf, sizeof(buf), "%llx", (unsigned long long) (uintptr_t) global_ref);
	return (*env)->NewStringUTF(env, buf);
}

JNIEXPORT jint JNICALL Java_com_movtery_zalithlauncher_bridge_ZLBridge_chdir(JNIEnv *env, jclass clazz, jstring nameStr) {
	const char *name = (*env)->GetStringUTFChars(env, nameStr, NULL);
	int retval = chdir(name);
	(*env)->ReleaseStringUTFChars(env, nameStr, name);
	return retval;
}


JNIEnv* get_attached_env(JavaVM* jvm) {
    if (jvm == NULL) return NULL;
    JNIEnv *env = NULL;
    jint status = (*jvm)->GetEnv(jvm, (void**)&env, JNI_VERSION_1_4);
    if (status == JNI_OK) return env;
    if (status == JNI_EDETACHED) {
        if ((*jvm)->AttachCurrentThread(jvm, (void**)&env, NULL) == JNI_OK) {
            return env;
        }
    }
    return NULL;
}

bool notifyLauncher(JNIEnv *dvm_env, int type, int actions[], int len) {
    jintArray actionArray = (*dvm_env)->NewIntArray(dvm_env, len);
    (*dvm_env)->SetIntArrayRegion(dvm_env, actionArray, 0, len, actions);
    return (*dvm_env)->CallStaticBooleanMethod(dvm_env, pojav_environ->bridgeClazz,
            pojav_environ->method_notifyLauncher, type, actionArray);
}

jintArray convertIntArrayJVM(JNIEnv* srcEnv, JNIEnv* dstEnv, jintArray srcIntArray) {
	if (srcIntArray == NULL) {
		return NULL;
	}

	jsize len = (*srcEnv)->GetArrayLength(srcEnv, srcIntArray);
	jint* srcPtr = (*srcEnv)->GetIntArrayElements(srcEnv, srcIntArray, NULL);

	jintArray dstIntArray = (*dstEnv)->NewIntArray(dstEnv, len);
	(*dstEnv)->SetIntArrayRegion(dstEnv, dstIntArray, 0, len, srcPtr);

	(*srcEnv)->ReleaseIntArrayElements(srcEnv, srcIntArray, srcPtr, JNI_ABORT);

	return dstIntArray;
}

