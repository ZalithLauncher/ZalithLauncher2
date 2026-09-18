/*
 * V3 input bridge implementation.
 *
 * Status:
 * - Active development
 * - Works with some bugs:
 *  + Modded versions gives broken stuff..
 *
 * 
 * - Implements glfwSetCursorPos() to handle grab camera pos correctly.
 */
 
#include <assert.h>
#include <dlfcn.h>
#include <jni.h>
#include <libgen.h>
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#include <math.h>

#include "logger/logger.h"
#include "utils.h"
#include "environ/environ.h"

#define EVENT_TYPE_CHAR 1000
#define EVENT_TYPE_CHAR_MODS 1001
#define EVENT_TYPE_CURSOR_ENTER 1002
#define EVENT_TYPE_KEY 1005
#define EVENT_TYPE_MOUSE_BUTTON 1006
#define EVENT_TYPE_SCROLL 1007

static void registerFunctions(JNIEnv *env);


JNIEXPORT jboolean JNICALL Java_org_lwjgl_glfw_CallbackBridge_nativeNotifyLauncher(JNIEnv* env, __attribute__((unused)) jclass clazz, jint type, jintArray action) {
    TRY_ATTACH_ENV(dvm_env, pojav_environ->dalvikJavaVMPtr, "nativeNotifyLauncher failed!\n",);
    jboolean result = (*dvm_env)->CallStaticBooleanMethod(dvm_env, pojav_environ->bridgeClazz,
                                                          pojav_environ->method_notifyLauncher, type, convertIntArrayJVM(env, dvm_env, action));
    if ((*dvm_env)->ExceptionCheck(dvm_env)) {
        (*dvm_env)->ExceptionDescribe(dvm_env);
        (*dvm_env)->ExceptionClear(dvm_env);
        return JNI_FALSE;
    }
    return result;
}

jint JNI_OnLoad(JavaVM* vm, __attribute__((unused)) void* reserved) {
    if (pojav_environ->dalvikJavaVMPtr == NULL) {
        LOG_TO_I("<%s> %s", "Native", "Saving DVM environ...");
        //Save dalvik global JavaVM pointer
        pojav_environ->dalvikJavaVMPtr = vm;
        (*vm)->GetEnv(vm, (void**) &pojav_environ->dalvikJNIEnvPtr_ANDROID, JNI_VERSION_1_4);
        pojav_environ->bridgeClazz = (*pojav_environ->dalvikJNIEnvPtr_ANDROID)->NewGlobalRef(pojav_environ->dalvikJNIEnvPtr_ANDROID,(*pojav_environ->dalvikJNIEnvPtr_ANDROID) ->FindClass(pojav_environ->dalvikJNIEnvPtr_ANDROID,"org/lwjgl/glfw/CallbackBridge"));
        pojav_environ->method_accessAndroidClipboard = (*pojav_environ->dalvikJNIEnvPtr_ANDROID)->GetStaticMethodID(pojav_environ->dalvikJNIEnvPtr_ANDROID, pojav_environ->bridgeClazz, "accessAndroidClipboard", "(ILjava/lang/String;)Ljava/lang/String;");
        pojav_environ->method_onGrabStateChanged = (*pojav_environ->dalvikJNIEnvPtr_ANDROID)->GetStaticMethodID(pojav_environ->dalvikJNIEnvPtr_ANDROID, pojav_environ->bridgeClazz, "onGrabStateChanged", "(Z)V");
        pojav_environ->method_onCursorShapeChanged = (*pojav_environ->dalvikJNIEnvPtr_ANDROID)->GetStaticMethodID(pojav_environ->dalvikJNIEnvPtr_ANDROID, pojav_environ->bridgeClazz, "onCursorShapeChanged", "(I)V");
        pojav_environ->method_onGraphicOutput = (*pojav_environ->dalvikJNIEnvPtr_ANDROID)->GetStaticMethodID(pojav_environ->dalvikJNIEnvPtr_ANDROID, pojav_environ->bridgeClazz, "onGraphicOutput", "()V");
        pojav_environ->method_onDirectInputEnable = (*pojav_environ->dalvikJNIEnvPtr_ANDROID)->GetStaticMethodID(pojav_environ->dalvikJNIEnvPtr_ANDROID, pojav_environ->bridgeClazz, "onDirectInputEnable", "()V");
        pojav_environ->method_notifyLauncher = (*pojav_environ->dalvikJNIEnvPtr_ANDROID)->GetStaticMethodID(pojav_environ->dalvikJNIEnvPtr_ANDROID, pojav_environ->bridgeClazz, "notifyLauncher", "(I[I)Z");
        pojav_environ->isUseStackQueueCall = JNI_FALSE;
    } else if (pojav_environ->dalvikJavaVMPtr != vm) {
        LOG_TO_I("<%s> %s", "Native", "Saving JVM environ...");
        pojav_environ->runtimeJavaVMPtr = vm;
        (*vm)->GetEnv(vm, (void**) &pojav_environ->runtimeJNIEnvPtr_JRE, JNI_VERSION_1_4);
        hookExec();
        installLwjglDlopenHook();
        installEMUIIteratorMititgation();
    }

    if(pojav_environ->dalvikJavaVMPtr == vm) {
        //perform in all DVM instances, not only during first ever set up
        JNIEnv *env;
        (*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4);
        registerFunctions(env);
    }
    pojav_environ->isGrabbing = JNI_FALSE;
    
    return JNI_VERSION_1_4;
}

/**
 * 解析 GLFW 桥所需的类、方法 ID 与按键缓冲，供 GLFW.<clinit> 与渲染线程按需调用。
 *
 * 必须在 libpojavexec 完成加载后运行，不能放进 JNI_OnLoad：此时 GLFW 类尚未初始化，
 * FindClass 会触发其 <clinit>，进而再次 System.loadLibrary("pojavexec")，重入尚未返回的
 * JNI_OnLoad 导致崩溃。
 * 只能使用调用线程自己的 JNIEnv（传入的 env，必要时经 GetEnv 获取）：缓存下来的
 * runtimeJNIEnvPtr_JRE 属于最先加载 pojavexec 的线程，模组可能在其它线程上更早触发
 * GLFW 类初始化，跨线程复用该指针属于未定义行为，会导致 FindClass 失败。
 * 可安全重复调用：已初始化时直接返回；单次尝试内任一成员解析失败都不会写入全局状态，
 * 留待下次在有效线程上重试。
 */
jboolean ensureGlfwNativeBridgeInitialized(JNIEnv *env) {
    if (pojav_environ->vmGlfwClass != NULL) {
        return JNI_TRUE;
    }
    if (env == NULL && pojav_environ->runtimeJavaVMPtr != NULL) {
        (*pojav_environ->runtimeJavaVMPtr)->GetEnv(
                pojav_environ->runtimeJavaVMPtr, (void **) &env, JNI_VERSION_1_4);
    }
    if (env == NULL) {
        LOG_TO_E("<%s> %s", "Native", "nativeInitializeGLFWNativeBridge: no game JVM environ available!");
        return JNI_FALSE;
    }

    jclass glfwClass = (*env)->FindClass(env, "org/lwjgl/glfw/GLFW");
    if (glfwClass == NULL) {
        if ((*env)->ExceptionCheck(env)) (*env)->ExceptionClear(env);
        LOG_TO_E("<%s> %s", "Native", "nativeInitializeGLFWNativeBridge: failed to find org.lwjgl.glfw.GLFW");
        return JNI_FALSE;
    }

    jmethodID method_glftSetWindowAttrib = (*env)->GetStaticMethodID(env, glfwClass, "glfwSetWindowAttrib", "(JII)V");
    jmethodID method_internalWindowSizeChanged = (*env)->GetStaticMethodID(env, glfwClass, "internalWindowSizeChanged", "(J)V");
    jmethodID method_internalChangeMonitorSize = (*env)->GetStaticMethodID(env, glfwClass, "internalChangeMonitorSize", "(II)V");
    jfieldID field_keyDownBuffer = (*env)->GetStaticFieldID(env, glfwClass, "keyDownBuffer", "Ljava/nio/ByteBuffer;");
    jfieldID field_mouseDownBuffer = (*env)->GetStaticFieldID(env, glfwClass, "mouseDownBuffer", "Ljava/nio/ByteBuffer;");
    if ((*env)->ExceptionCheck(env)) {
        LOG_TO_E("<%s> %s", "Native", "nativeInitializeGLFWNativeBridge: failed to resolve GLFW bridge members");
        (*env)->ExceptionClear(env);
        (*env)->DeleteLocalRef(env, glfwClass);
        return JNI_FALSE;
    }

    jobject keyDownBufferJ = (*env)->GetStaticObjectField(env, glfwClass, field_keyDownBuffer);
    jobject mouseDownBufferJ = (*env)->GetStaticObjectField(env, glfwClass, field_mouseDownBuffer);
    jbyte *keyDownBuffer = (*env)->GetDirectBufferAddress(env, keyDownBufferJ);
    jbyte *mouseDownBuffer = (*env)->GetDirectBufferAddress(env, mouseDownBufferJ);
    (*env)->DeleteLocalRef(env, keyDownBufferJ);
    (*env)->DeleteLocalRef(env, mouseDownBufferJ);

    // 全部成员解析成功后才写入全局状态，避免留下半初始化的桥
    pojav_environ->vmGlfwClass = (*env)->NewGlobalRef(env, glfwClass);
    (*env)->DeleteLocalRef(env, glfwClass);
    pojav_environ->method_glftSetWindowAttrib = method_glftSetWindowAttrib;
    pojav_environ->method_internalWindowSizeChanged = method_internalWindowSizeChanged;
    pojav_environ->method_internalChangeMonitorSize = method_internalChangeMonitorSize;
    pojav_environ->keyDownBuffer = keyDownBuffer;
    pojav_environ->mouseDownBuffer = mouseDownBuffer;
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_org_lwjgl_glfw_GLFW_nativeInitializeGLFWNativeBridge(JNIEnv* env, __attribute__((unused)) jclass clazz) {
    ensureGlfwNativeBridgeInitialized(env);
}

#define ADD_CALLBACK_WWIN(NAME) \
JNIEXPORT jlong JNICALL Java_org_lwjgl_glfw_GLFW_nglfwSet##NAME##Callback(JNIEnv * env, jclass cls, jlong window, jlong callbackptr) { \
    void** oldCallback = (void**) &pojav_environ->GLFW_invoke_##NAME; \
    pojav_environ->GLFW_invoke_##NAME = (GLFW_invoke_##NAME##_func*) (uintptr_t) callbackptr; \
    return (jlong) (uintptr_t) *oldCallback; \
}

ADD_CALLBACK_WWIN(Char)
ADD_CALLBACK_WWIN(CharMods)
ADD_CALLBACK_WWIN(CursorEnter)
ADD_CALLBACK_WWIN(CursorPos)
ADD_CALLBACK_WWIN(FramebufferSize)
ADD_CALLBACK_WWIN(Key)
ADD_CALLBACK_WWIN(MouseButton)
ADD_CALLBACK_WWIN(Scroll)
ADD_CALLBACK_WWIN(WindowSize)

#undef ADD_CALLBACK_WWIN

void updateMonitorSize(int width, int height) {
    if (pojav_environ->glfwThreadVmEnv == NULL || pojav_environ->vmGlfwClass == NULL) {
        LOG_TO_E("<%s> %s", "Native", "updateMonitorSize: GLFW bridge is not initialized, skipped");
        return;
    }
    (*pojav_environ->glfwThreadVmEnv)->CallStaticVoidMethod(
            pojav_environ->glfwThreadVmEnv, pojav_environ->vmGlfwClass,
            pojav_environ->method_internalChangeMonitorSize, width, height);
}

void updateWindowSize(void *window) {
    if (pojav_environ->glfwThreadVmEnv == NULL || pojav_environ->vmGlfwClass == NULL) {
        LOG_TO_E("<%s> %s", "Native", "updateWindowSize: GLFW bridge is not initialized, skipped");
        return;
    }
    (*pojav_environ->glfwThreadVmEnv)->CallStaticVoidMethod(
            pojav_environ->glfwThreadVmEnv, pojav_environ->vmGlfwClass,
            pojav_environ->method_internalWindowSizeChanged, (jlong) window);
}

void pojavPumpEvents(void* window) {
    if(pojav_environ->shouldUpdateMouse) {
        pojav_environ->GLFW_invoke_CursorPos(window, floor(pojav_environ->cursorX),
                                             floor(pojav_environ->cursorY));
    }
    if (pojav_environ->shouldUpdateMonitorSize) {
        updateWindowSize(window);
    }

    size_t index = pojav_environ->outEventIndex;
    size_t targetIndex = pojav_environ->outTargetIndex;

    while (targetIndex != index) {
        GLFWInputEvent event = pojav_environ->events[index];
        switch (event.type) {
            case EVENT_TYPE_CHAR:
                if(pojav_environ->GLFW_invoke_Char) pojav_environ->GLFW_invoke_Char(window, event.i1);
                break;
            case EVENT_TYPE_CHAR_MODS:
                if(pojav_environ->GLFW_invoke_CharMods) pojav_environ->GLFW_invoke_CharMods(window, event.i1, event.i2);
                break;
            case EVENT_TYPE_KEY:
                if(pojav_environ->GLFW_invoke_Key) pojav_environ->GLFW_invoke_Key(window, event.i1, event.i2, event.i3, event.i4);
                break;
            case EVENT_TYPE_MOUSE_BUTTON:
                if(pojav_environ->GLFW_invoke_MouseButton) pojav_environ->GLFW_invoke_MouseButton(window, event.i1, event.i2, event.i3);
                break;
            case EVENT_TYPE_CURSOR_ENTER:
                if(pojav_environ->GLFW_invoke_CursorEnter) pojav_environ->GLFW_invoke_CursorEnter(window, event.i1);
                break;
            case EVENT_TYPE_SCROLL:
                if(pojav_environ->GLFW_invoke_Scroll) pojav_environ->GLFW_invoke_Scroll(window, event.i1, event.i2);
                break;
        }

        index++;
        if (index >= EVENT_WINDOW_SIZE)
            index -= EVENT_WINDOW_SIZE;
    }

    // The out target index is updated by the rewinder
}

/** Prepare the library for sending out callbacks to all windows */
void pojavStartPumping() {
    size_t counter = atomic_load_explicit(&pojav_environ->eventCounter, memory_order_acquire);
    size_t index = pojav_environ->outEventIndex;

    unsigned targetIndex = index + counter;
    if (targetIndex >= EVENT_WINDOW_SIZE)
        targetIndex -= EVENT_WINDOW_SIZE;

    // Only accessed by one unique thread, no need for atomic store
    pojav_environ->inEventCount = counter;
    pojav_environ->outTargetIndex = targetIndex;

    //PumpEvents is called for every window, so this logic should be there in order to correctly distribute events to all windows.
    if((pojav_environ->cLastX != pojav_environ->cursorX || pojav_environ->cLastY != pojav_environ->cursorY) && pojav_environ->GLFW_invoke_CursorPos) {
        pojav_environ->cLastX = pojav_environ->cursorX;
        pojav_environ->cLastY = pojav_environ->cursorY;
        pojav_environ->shouldUpdateMouse = true;
    }
    if (pojav_environ->shouldUpdateMonitorSize) {
        // Perform a monitor size update here to avoid doing it on every single window
        updateMonitorSize(pojav_environ->savedWidth, pojav_environ->savedHeight);
        // Mark the monitor size as consumed (since GLFW was made aware of it)
        pojav_environ->monitorSizeConsumed = true;
    }
}

/** Prepare the library for the next round of new events */
void pojavStopPumping() {
    pojav_environ->outEventIndex = pojav_environ->outTargetIndex;

    // New events may have arrived while pumping, so remove only the difference before the start and end of execution
    atomic_fetch_sub_explicit(&pojav_environ->eventCounter, pojav_environ->inEventCount, memory_order_acquire);
    // Make sure the next frame won't send mouse updates if it's unnecessary
    pojav_environ->shouldUpdateMouse = false;
    // Only reset the update flag if the monitor size was consumed by pojavStartPumping. This
    // will delay the update to next frame if it had occured between pojavStartPumping and pojavStopPumping,
    // but it's better than not having it apply at all
    if (pojav_environ->shouldUpdateMonitorSize && pojav_environ->monitorSizeConsumed) {
        pojav_environ->shouldUpdateMonitorSize = false;
        pojav_environ->monitorSizeConsumed = false;
    }
}

JNIEXPORT void JNICALL
Java_org_lwjgl_glfw_GLFW_nglfwGetCursorPos(JNIEnv *env, __attribute__((unused)) jclass clazz, __attribute__((unused)) jlong window, jobject xpos,
                                          jobject ypos) {
    *(double*)(*env)->GetDirectBufferAddress(env, xpos) = pojav_environ->cursorX;
    *(double*)(*env)->GetDirectBufferAddress(env, ypos) = pojav_environ->cursorY;
}

JNIEXPORT void JNICALL JavaCritical_org_lwjgl_glfw_GLFW_nglfwGetCursorPosA(__attribute__((unused)) jlong window, jint lengthx, jdouble* xpos, jint lengthy, jdouble* ypos) {
    *xpos = pojav_environ->cursorX;
    *ypos = pojav_environ->cursorY;
}

JNIEXPORT void JNICALL
Java_org_lwjgl_glfw_GLFW_nglfwGetCursorPosA(JNIEnv *env, __attribute__((unused)) jclass clazz, __attribute__((unused)) jlong window,
                                            jdoubleArray xpos, jdoubleArray ypos) {
    (*env)->SetDoubleArrayRegion(env, xpos, 0,1, &pojav_environ->cursorX);
    (*env)->SetDoubleArrayRegion(env, ypos, 0,1, &pojav_environ->cursorY);
}

JNIEXPORT void JNICALL JavaCritical_org_lwjgl_glfw_GLFW_glfwSetCursorPos(__attribute__((unused)) jlong window, jdouble xpos,
                                                                         jdouble ypos) {
    pojav_environ->cLastX = pojav_environ->cursorX = xpos;
    pojav_environ->cLastY = pojav_environ->cursorY = ypos;
}

JNIEXPORT void JNICALL
Java_org_lwjgl_glfw_GLFW_glfwSetCursorPos(__attribute__((unused)) JNIEnv *env, __attribute__((unused)) jclass clazz, __attribute__((unused)) jlong window, jdouble xpos,
                                          jdouble ypos) {
    JavaCritical_org_lwjgl_glfw_GLFW_glfwSetCursorPos(window, xpos, ypos);
}



void sendData(int type, int i1, int i2, int i3, int i4) {
    GLFWInputEvent *event = &pojav_environ->events[pojav_environ->inEventIndex];
    event->type = type;
    event->i1 = i1;
    event->i2 = i2;
    event->i3 = i3;
    event->i4 = i4;

    if (++pojav_environ->inEventIndex >= EVENT_WINDOW_SIZE)
        pojav_environ->inEventIndex -= EVENT_WINDOW_SIZE;

    atomic_fetch_add_explicit(&pojav_environ->eventCounter, 1, memory_order_acquire);
}

/**
 * This function is meant as a substitute for SharedLibraryUtil.getLibraryPath() that just returns 0
 * (thus making the parent Java function return null). This is done to avoid using the LWJGL's default function,
 * which will hang the crappy EMUI linker by dlopen()ing inside of dl_iterate_phdr().
 * @return 0, to make the parent Java function return null immediately.
 * For reference: https://github.com/PojavLauncherTeam/lwjgl3/blob/fix_huawei_hang/modules/lwjgl/core/src/main/java/org/lwjgl/system/SharedLibraryUtil.java
 */
jint getLibraryPath_fix(__attribute__((unused)) JNIEnv *env,
                        __attribute__((unused)) jclass class,
                        __attribute__((unused)) jlong pLibAddress,
                        __attribute__((unused)) jlong sOutAddress,
                        __attribute__((unused)) jint bufSize){
    return 0;
}

/**
 * Install the linker hang mitigation that is meant to prevent linker hangs on old EMUI firmware.
 */
void installEMUIIteratorMititgation() {
    if(getenv("POJAV_EMUI_ITERATOR_MITIGATE") == NULL) return;
    LOG_TO_I("<%s> %s", "EMUIIteratorFix", "Installing...");
    JNIEnv* env = pojav_environ->runtimeJNIEnvPtr_JRE;
    jclass sharedLibraryUtil = (*env)->FindClass(env, "org/lwjgl/system/SharedLibraryUtil");
    if(sharedLibraryUtil == NULL) {
        LOG_TO_E("<%s> %s", "EMUIIteratorFix", "Failed to find the target class");
        (*env)->ExceptionClear(env);
        return;
    }
    JNINativeMethod getLibraryPathMethod[] = {
            {"getLibraryPath", "(JJI)I", &getLibraryPath_fix}
    };
    if((*env)->RegisterNatives(env, sharedLibraryUtil, getLibraryPathMethod, 1) != 0) {
        LOG_TO_E("<%s> %s", "EMUIIteratorFix", "Failed to register the mitigation method");
        (*env)->ExceptionClear(env);
    }
}

void critical_set_stackqueue(jboolean use_input_stack_queue) {
    pojav_environ->isUseStackQueueCall = (int) use_input_stack_queue;
}

void noncritical_set_stackqueue(__attribute__((unused)) JNIEnv *env, __attribute__((unused)) jclass clazz, jboolean use_input_stack_queue) {
    critical_set_stackqueue(use_input_stack_queue);
}

JNIEXPORT jstring JNICALL Java_org_lwjgl_glfw_CallbackBridge_nativeClipboard(JNIEnv* env, __attribute__((unused)) jclass clazz, jint action, jbyteArray copySrc) {
#ifdef DEBUG
    LOGD("Debug: Clipboard access is going on\n", pojav_environ->isUseStackQueueCall);
#endif

    JNIEnv *dalvikEnv = NULL;
    jboolean detachedBefore = (*pojav_environ->dalvikJavaVMPtr)->GetEnv(pojav_environ->dalvikJavaVMPtr, (void **) &dalvikEnv, JNI_VERSION_1_4) == JNI_EDETACHED;
    if (detachedBefore) {
        (*pojav_environ->dalvikJavaVMPtr)->AttachCurrentThread(pojav_environ->dalvikJavaVMPtr, &dalvikEnv, NULL);
    }
    assert(dalvikEnv != NULL);
    assert(pojav_environ->bridgeClazz != NULL);
    
    LOG_TO_D("Clipboard: Converting string");
    char *copySrcC;
    jstring copyDst = NULL;
    if (copySrc) {
        copySrcC = (char *)((*env)->GetByteArrayElements(env, copySrc, NULL));
        copyDst = (*dalvikEnv)->NewStringUTF(dalvikEnv, copySrcC);
    }

    LOG_TO_D("Clipboard: Calling 2nd");
    jstring pasteDst = convertStringJVM(dalvikEnv, env, (jstring) (*dalvikEnv)->CallStaticObjectMethod(dalvikEnv, pojav_environ->bridgeClazz, pojav_environ->method_accessAndroidClipboard, action, copyDst));

    if (copySrc) {
        (*dalvikEnv)->DeleteLocalRef(dalvikEnv, copyDst);
        (*env)->ReleaseByteArrayElements(env, copySrc, (jbyte *)copySrcC, 0);
    }
    if (detachedBefore) {
        (*pojav_environ->dalvikJavaVMPtr)->DetachCurrentThread(pojav_environ->dalvikJavaVMPtr);
    }
    return pasteDst;
}

JNIEXPORT jboolean JNICALL JavaCritical_org_lwjgl_glfw_CallbackBridge_nativeSetInputReady(jboolean inputReady) {
#ifdef DEBUG
    LOGD("Debug: Changing input state, isReady=%d, pojav_environ->isUseStackQueueCall=%d\n", inputReady, pojav_environ->isUseStackQueueCall);
#endif
    LOG_TO_I("<%s> %s: %i", "NativeInput", "Input ready", inputReady);
    pojav_environ->isInputReady = inputReady;
    return pojav_environ->isUseStackQueueCall;
}

JNIEXPORT jboolean JNICALL Java_org_lwjgl_glfw_CallbackBridge_nativeSetInputReady(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jboolean inputReady) {
    return JavaCritical_org_lwjgl_glfw_CallbackBridge_nativeSetInputReady(inputReady);
}

JNIEXPORT void JNICALL Java_org_lwjgl_glfw_CallbackBridge_nativeSetGrabbing(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jboolean grabbing) {
    TRY_ATTACH_ENV(dvm_env, pojav_environ->dalvikJavaVMPtr, "nativeSetGrabbing failed!\n", return;);
    (*dvm_env)->CallStaticVoidMethod(dvm_env, pojav_environ->bridgeClazz, pojav_environ->method_onGrabStateChanged, grabbing);
    pojav_environ->isGrabbing = grabbing;
}

JNIEXPORT jlong JNICALL
Java_org_lwjgl_glfw_GLFW_internalGetGamepadDataPointer(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz) {
    return (jlong)(intptr_t)&pojav_environ->gamepadState;
}

JNIEXPORT jboolean JNICALL
Java_org_lwjgl_glfw_CallbackBridge_nativeEnableGamepadDirectInput(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz) {
    TRY_ATTACH_ENV(dvm_env, pojav_environ->dalvikJavaVMPtr, "nativeEnableGamepadDirectInput failed!\n", return JNI_FALSE;);
    (*dvm_env)->CallStaticVoidMethod(dvm_env, pojav_environ->bridgeClazz, pojav_environ->method_onDirectInputEnable);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_movtery_zalithlauncher_game_sdl_SdlBridge_initializeControllerSubsystems(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz) {
    typedef int (*SDL_Init_Func)(unsigned int flags);
    void* handle = dlopen("libSDL3.so", RTLD_NOW);
    if (handle == NULL) {
        LOG_TO_E("<%s> %s", "SDL", "initializeControllerSubsystems: libSDL3.so dlopen failed");
        return;
    }
    SDL_Init_Func SDL_Init = (SDL_Init_Func) dlsym(handle, "SDL_Init");
    if (SDL_Init == NULL) {
        LOG_TO_E("<%s> %s", "SDL", "initializeControllerSubsystems: SDL_Init not found");
        return;
    }
    // SDL3: SDL_INIT_GAMEPAD=0x2000 | SDL_INIT_JOYSTICK=0x200 | SDL_INIT_EVENTS=0x4000
    SDL_Init(0x2000u | 0x200u | 0x4000u);
    LOG_TO_I("<%s> %s", "SDL", "initializeControllerSubsystems: SDL controller subsystems initialized");
}

// --- SDL 文本输入通道（启动器代管，供启动器侧显式唤起输入法） ---
// 参考 Fold Craft Launcher（https://github.com/FCL-Team/FoldCraftLauncher/blob/cacd666292d9646ceb622c39b82123d8671e5b41/FCL/src/main/jni/input_bridge_v3.c）

typedef struct SDL_Window SDL_Window;
typedef uint32_t SDL_PropertiesID;
typedef void (*SDL_MainThreadCallback)(void *userdata);
typedef bool (*sdlStartTextInput_t)(SDL_Window *, SDL_PropertiesID);
typedef bool (*sdlStopTextInput_t)(SDL_Window *);
typedef bool (*sdlRunOnMainThread_t)(SDL_MainThreadCallback, void *, bool);

// SDL 主窗口指针，由 exithook/sdl_hook.c 在窗口创建/销毁时同步（两库不反链接，经导出函数回填）
static SDL_Window *sSdlPrimaryWindow = NULL;

void sdlBridgeSetPrimaryWindow(struct SDL_Window *window) {
    sSdlPrimaryWindow = window;
}

static void sdlTextInputMainThreadCallback(void *userdata) {
    void *handle = dlopen("libSDL3.so", RTLD_NOW);
    if (handle == NULL) return;
    SDL_Window *window = sSdlPrimaryWindow;
    if (window == NULL) return;
    if (userdata != NULL) {
        sdlStartTextInput_t start = (sdlStartTextInput_t) dlsym(handle, "SDL_StartTextInput");
        if (start != NULL) start(window, 0);
        else LOG_TO_E("<%s> %s", "SDL", "sdlTextInputMainThreadCallback: SDL_StartTextInput not found");
    } else {
        sdlStopTextInput_t stop = (sdlStopTextInput_t) dlsym(handle, "SDL_StopTextInput");
        if (stop != NULL) stop(window);
        else LOG_TO_E("<%s> %s", "SDL", "sdlTextInputMainThreadCallback: SDL_StopTextInput not found");
    }
}

JNIEXPORT jboolean JNICALL
Java_com_movtery_zalithlauncher_game_sdl_SdlBridge_isSdlRenderActive(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz) {
    // SDL 渲染路径以首个 SDL 窗口创建为标志；仅手柄子系统初始化 SDL（MC 26.2 挂 Controlify）时无窗口
    return sSdlPrimaryWindow != NULL ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_movtery_zalithlauncher_game_sdl_SdlBridge_setNativeTextInputActive(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jboolean active) {
    if (sSdlPrimaryWindow == NULL) {
        LOG_TO_W("<%s> %s", "SDL", "setNativeTextInputActive: no SDL window (SDL render path inactive)");
        return JNI_FALSE;
    }
    void *handle = dlopen("libSDL3.so", RTLD_NOW);
    if (handle == NULL) {
        LOG_TO_E("<%s> %s", "SDL", "setNativeTextInputActive: libSDL3.so dlopen failed");
        return JNI_FALSE;
    }
    sdlRunOnMainThread_t runOnMain = (sdlRunOnMainThread_t) dlsym(handle, "SDL_RunOnMainThread");
    if (runOnMain == NULL) {
        LOG_TO_E("<%s> %s", "SDL", "setNativeTextInputActive: SDL_RunOnMainThread not found");
        return JNI_FALSE;
    }
    // SDL3 文本输入 API 要求主线程，经 SDL_RunOnMainThread 投递；不等待完成，激活结果由
    // SDL 回调 showTextInput 异步镜像回 Java 层
    bool result = runOnMain(sdlTextInputMainThreadCallback, (void *) (intptr_t) (active ? 1 : 0), JNI_FALSE);
    if (!result) {
        LOG_TO_E("<%s> %s", "SDL", "setNativeTextInputActive: SDL_RunOnMainThread dispatch failed");
    }
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobject JNICALL
Java_org_lwjgl_glfw_CallbackBridge_nativeCreateGamepadButtonBuffer(JNIEnv* env, __attribute__((unused)) jclass clazz) {
    return (*env)->NewDirectByteBuffer(env, pojav_environ->gamepadState.buttons, sizeof(pojav_environ->gamepadState.buttons));
}

JNIEXPORT jobject JNICALL
Java_org_lwjgl_glfw_CallbackBridge_nativeCreateGamepadAxisBuffer(JNIEnv* env, __attribute__((unused)) jclass clazz) {
    return (*env)->NewDirectByteBuffer(env, pojav_environ->gamepadState.axes, sizeof(pojav_environ->gamepadState.axes));
}

JNIEXPORT void JNICALL
Java_org_lwjgl_glfw_CallbackBridge_nativeSetCursorShape(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jint shape) {
    JNIEnv *dalvikEnv = NULL;
    jboolean detachedBefore = (*pojav_environ->dalvikJavaVMPtr)->GetEnv(pojav_environ->dalvikJavaVMPtr, (void **) &dalvikEnv, JNI_VERSION_1_4) == JNI_EDETACHED;
    if (detachedBefore) {
        (*pojav_environ->dalvikJavaVMPtr)->AttachCurrentThread(pojav_environ->dalvikJavaVMPtr, &dalvikEnv, NULL);
    }
    if (dalvikEnv != NULL) {
        (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, pojav_environ->bridgeClazz, pojav_environ->method_onCursorShapeChanged, shape);
        if (detachedBefore) {
            (*pojav_environ->dalvikJavaVMPtr)->DetachCurrentThread(pojav_environ->dalvikJavaVMPtr);
        }
    }
}

jboolean critical_send_char(jchar codepoint) {
    if (pojav_environ->GLFW_invoke_Char && pojav_environ->isInputReady) {
        if (pojav_environ->isUseStackQueueCall) {
            sendData(EVENT_TYPE_CHAR, codepoint, 0, 0, 0);
        } else {
            pojav_environ->GLFW_invoke_Char((void*) pojav_environ->showingWindow, (unsigned int) codepoint);
        }
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

jboolean noncritical_send_char(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jchar codepoint) {
    return critical_send_char(codepoint);
}

jboolean critical_send_char_mods(jchar codepoint, jint mods) {
    if (pojav_environ->GLFW_invoke_CharMods && pojav_environ->isInputReady) {
        if (pojav_environ->isUseStackQueueCall) {
            sendData(EVENT_TYPE_CHAR_MODS, (int) codepoint, mods, 0, 0);
        } else {
            pojav_environ->GLFW_invoke_CharMods((void*) pojav_environ->showingWindow, codepoint, mods);
        }
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

jboolean noncritical_send_char_mods(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jchar codepoint, jint mods) {
    return critical_send_char_mods(codepoint, mods);
}
/*
JNIEXPORT void JNICALL Java_org_lwjgl_glfw_CallbackBridge_nativeSendCursorEnter(JNIEnv* env, jclass clazz, jint entered) {
    if (pojav_environ->GLFW_invoke_CursorEnter && pojav_environ->isInputReady) {
        pojav_environ->GLFW_invoke_CursorEnter(pojav_environ->showingWindow, entered);
    }
}
*/

void critical_send_cursor_pos(jfloat x, jfloat y) {
#ifdef DEBUG
    LOGD("Sending cursor position \n");
#endif
    if (pojav_environ->GLFW_invoke_CursorPos && pojav_environ->isInputReady) {
#ifdef DEBUG
        LOGD("pojav_environ->GLFW_invoke_CursorPos && pojav_environ->isInputReady \n");
#endif
        if (!pojav_environ->isCursorEntered) {
            if (pojav_environ->GLFW_invoke_CursorEnter) {
                pojav_environ->isCursorEntered = true;
                if (pojav_environ->isUseStackQueueCall) {
                    sendData(EVENT_TYPE_CURSOR_ENTER, 1, 0, 0, 0);
                } else {
                    pojav_environ->GLFW_invoke_CursorEnter((void*) pojav_environ->showingWindow, 1);
                }
            }
        }

        if (!pojav_environ->isUseStackQueueCall) {
            pojav_environ->GLFW_invoke_CursorPos((void*) pojav_environ->showingWindow, (double) (x), (double) (y));
        } else {
            pojav_environ->cursorX = x;
            pojav_environ->cursorY = y;
        }
    }
}

void noncritical_send_cursor_pos(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz,  jfloat x, jfloat y) {
    critical_send_cursor_pos(x, y);
}
#define max(a,b) \
   ({ __typeof__ (a) _a = (a); \
       __typeof__ (b) _b = (b); \
     _a > _b ? _a : _b; })
void critical_send_key(jint key, jint scancode, jint action, jint mods) {
    if (pojav_environ->GLFW_invoke_Key && pojav_environ->isInputReady) {
        if (key >= 31 && key <= 348 && pojav_environ->keyDownBuffer != NULL) {
            pojav_environ->keyDownBuffer[key - 31] = (jbyte) action;
        }
        if (pojav_environ->isUseStackQueueCall) {
            sendData(EVENT_TYPE_KEY, key, scancode, action, mods);
        } else {
            pojav_environ->GLFW_invoke_Key((void*) pojav_environ->showingWindow, key, scancode, action, mods);
        }
    }
}
void noncritical_send_key(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jint key, jint scancode, jint action, jint mods) {
    critical_send_key(key, scancode, action, mods);
}

void critical_send_mouse_button(jint button, jint action, jint mods) {
    if (pojav_environ->GLFW_invoke_MouseButton && pojav_environ->isInputReady) {
        if (button >= 0 && button < 8 && pojav_environ->mouseDownBuffer != NULL) {
            pojav_environ->mouseDownBuffer[button] = (jbyte) action;
        }
        if (pojav_environ->isUseStackQueueCall) {
            sendData(EVENT_TYPE_MOUSE_BUTTON, button, action, mods, 0);
        } else {
            pojav_environ->GLFW_invoke_MouseButton((void*) pojav_environ->showingWindow, button, action, mods);
        }
    }
}

void noncritical_send_mouse_button(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jint button, jint action, jint mods) {
    critical_send_mouse_button(button, action, mods);
}

void critical_reset_input_state(void) {
    if (pojav_environ->keyDownBuffer != NULL) {
        memset(pojav_environ->keyDownBuffer, 0, 318);
    }
    if (pojav_environ->mouseDownBuffer != NULL) {
        memset(pojav_environ->mouseDownBuffer, 0, 8);
    }
}

void noncritical_reset_input_state(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz) {
    critical_reset_input_state();
}

void critical_send_screen_size(jint width, jint height) {
    pojav_environ->savedWidth = width;
    pojav_environ->savedHeight = height;
    // Even if there was call to pojavStartPumping that consumed the size, this call
    // might happen right after it (or right before pojavStopPumping)
    // So unmark the size as "consumed"
    pojav_environ->monitorSizeConsumed = false;
    pojav_environ->shouldUpdateMonitorSize = true;
    // Don't use the direct updates for screen dimensions.
    // This is done to ensure that we have predictable conditions to correctly call
    // updateMonitorSize() and updateWindowSize() while on the render thread with an attached
    // JNIEnv.
}

void noncritical_send_screen_size(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jint width, jint height) {
    critical_send_screen_size(width, height);
}

void critical_send_scroll(jdouble xoffset, jdouble yoffset) {
    if (pojav_environ->GLFW_invoke_Scroll && pojav_environ->isInputReady) {
        if (pojav_environ->isUseStackQueueCall) {
            sendData(EVENT_TYPE_SCROLL, (int)xoffset, (int)yoffset, 0, 0);
        } else {
            pojav_environ->GLFW_invoke_Scroll((void*) pojav_environ->showingWindow, (double) xoffset, (double) yoffset);
        }
    }
}

void noncritical_send_scroll(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jdouble xoffset, jdouble yoffset) {
    critical_send_scroll(xoffset, yoffset);
}


JNIEXPORT void JNICALL Java_org_lwjgl_glfw_GLFW_nglfwSetShowingWindow(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jlong window) {
    pojav_environ->showingWindow = (long) window;
}

JNIEXPORT void JNICALL Java_org_lwjgl_glfw_CallbackBridge_nativeSetWindowAttrib(__attribute__((unused)) JNIEnv* env, __attribute__((unused)) jclass clazz, jint attrib, jint value) {
    // Check for stack queue no longer necessary here as the JVM crash's origin is resolved
    if (!pojav_environ->showingWindow) {
        // If the window is not shown, there is nothing to do yet.
        return;
    }
    if (pojav_environ->vmGlfwClass == NULL || pojav_environ->method_glftSetWindowAttrib == NULL) {
        LOG_TO_E("<%s> %s", "Native", "nativeSetWindowAttrib: GLFW bridge is not initialized, skipped");
        return;
    }

    // We cannot use pojav_environ->runtimeJNIEnvPtr_JRE here because that environment is attached
    // on the thread that loaded pojavexec (which is the thread that first references the GLFW class)
    // But this method is only called from the Android UI thread
    // Technically the better solution would be to have a permanently attached env pointer stored
    // in environ for the Android UI thread but this is the only place that uses it
    // (very rarely, only in lifecycle callbacks) so i dont care
    JavaVM* jvm = pojav_environ->runtimeJavaVMPtr;
    JNIEnv *jvm_env = NULL;
    jint env_result = (*jvm)->GetEnv(jvm, (void**)&jvm_env, JNI_VERSION_1_4);
    if(env_result == JNI_EDETACHED) {
        env_result = (*jvm)->AttachCurrentThread(jvm, &jvm_env, NULL);
    }
    if(env_result != JNI_OK) {
        printf("input_bridge nativeSetWindowAttrib() JNI call failed: %i\n", env_result);
        return;
    }
    (*jvm_env)->CallStaticVoidMethod(
            jvm_env, pojav_environ->vmGlfwClass,
            pojav_environ->method_glftSetWindowAttrib,
            (jlong) pojav_environ->showingWindow, attrib, value
    );

    // Attaching every time is annoying, so stick the attachment to the Android GUI thread around
}
const static JNINativeMethod critical_fcns[] = {
        {"nativeSetUseInputStackQueue", "(Z)V", critical_set_stackqueue},
        {"nativeSendChar", "(C)Z", critical_send_char},
        {"nativeSendCharMods", "(CI)Z", critical_send_char_mods},
        {"nativeSendKey", "(IIII)V", critical_send_key},
        {"nativeSendCursorPos", "(FF)V", critical_send_cursor_pos},
        {"nativeSendMouseButton", "(III)V", critical_send_mouse_button},
        {"nativeResetInputState", "()V", critical_reset_input_state},
        {"nativeSendScroll", "(DD)V", critical_send_scroll},
        {"nativeSendScreenSize", "(II)V", critical_send_screen_size}
};

const static JNINativeMethod noncritical_fcns[] = {
        {"nativeSetUseInputStackQueue", "(Z)V", noncritical_set_stackqueue},
        {"nativeSendChar", "(C)Z", noncritical_send_char},
        {"nativeSendCharMods", "(CI)Z", noncritical_send_char_mods},
        {"nativeSendKey", "(IIII)V", noncritical_send_key},
        {"nativeSendCursorPos", "(FF)V", noncritical_send_cursor_pos},
        {"nativeSendMouseButton", "(III)V", noncritical_send_mouse_button},
        {"nativeResetInputState", "()V", noncritical_reset_input_state},
        {"nativeSendScroll", "(DD)V", noncritical_send_scroll},
        {"nativeSendScreenSize", "(II)V", noncritical_send_screen_size}
};


static bool criticalNativeAvailable;

void dvm_testCriticalNative(void* arg0, void* arg1, void* arg2, void* arg3) {
    if(arg0 != 0 && arg2 == 0 && arg3 == 0) {
        criticalNativeAvailable = false;
    }else if (arg0 == 0 && arg1 == 0){
        criticalNativeAvailable = true;
    }else {
        criticalNativeAvailable = false; // just to be safe
    }
}

static bool tryCriticalNative(JNIEnv *env) {
    static const JNINativeMethod testJNIMethod[] = {
            { "testCriticalNative", "(II)V", dvm_testCriticalNative}
    };
    jclass criticalNativeTest = (*env)->FindClass(env, "com/movtery/zalithlauncher/game/input/CriticalNativeTest");
    if(criticalNativeTest == NULL) {
        LOG_TO_D("No CriticalNativeTest class found !");
        (*env)->ExceptionClear(env);
        return false;
    }
    jmethodID criticalNativeTestMethod = (*env)->GetStaticMethodID(env, criticalNativeTest, "invokeTest", "()V");
    (*env)->RegisterNatives(env, criticalNativeTest, testJNIMethod, 1);
    (*env)->CallStaticVoidMethod(env, criticalNativeTest, criticalNativeTestMethod);
    (*env)->UnregisterNatives(env, criticalNativeTest);
    return criticalNativeAvailable;
}

static void registerFunctions(JNIEnv *env) {
    bool use_critical_cc = tryCriticalNative(env);
    jclass bridge_class = (*env)->FindClass(env, "org/lwjgl/glfw/CallbackBridge");
    if (use_critical_cc) {
        LOG_TO_I("CriticalNative is available. Enjoy the 4.6x times faster input!");
    } else {
        LOG_TO_I("CriticalNative is not available. Upgrade, maybe?");
    }
    (*env)->RegisterNatives(env,
                            bridge_class,
                            use_critical_cc ? critical_fcns : noncritical_fcns,
                            sizeof(critical_fcns) / sizeof(critical_fcns[0]));
}
