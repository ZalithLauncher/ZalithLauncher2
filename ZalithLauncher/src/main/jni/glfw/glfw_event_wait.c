#include "event_wait.h"
#include "glfw_event_wait.h"
#include "environ/environ.h"
#include <jni.h>

static struct gw_event_wait input_events;

void gw_glfw_event_input(void) {
    (void)gw_events_post(&input_events);
}

void gw_glfw_event_pump_begin(void) {
    gw_events_begin_pump(&input_events);
}

void gw_glfw_event_pump_end(void) {
    gw_events_end_pump(&input_events);
}

JNIEXPORT void JNICALL Java_org_lwjgl_glfw_GLFW_gwResetEventWait(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    gw_events_reset(&input_events);
    if (atomic_load_explicit(&pojav_environ->eventCounter, memory_order_acquire) ||
        pojav_environ->shouldUpdateMonitorSize)
        gw_glfw_event_input();
}

JNIEXPORT void JNICALL Java_org_lwjgl_glfw_GLFW_gwStopEventWait(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    gw_events_stop(&input_events);
}

JNIEXPORT jint JNICALL Java_org_lwjgl_glfw_GLFW_gwPostEmptyEvent(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    return gw_events_post(&input_events);
}

JNIEXPORT jint JNICALL Java_org_lwjgl_glfw_GLFW_gwWaitEvents(JNIEnv *env, jclass cls,
                                                             jdouble seconds, jboolean timed) {
    (void)env;
    (void)cls;
    int result = gw_events_wait(&input_events, seconds, timed);
    return result == GW_EVENT_TIMEOUT ? GW_EVENT_READY : result;
}
