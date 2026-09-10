/*
 * 来源：FoldCraftLauncher PR #1821（FCL/src/main/jni/flite/flite_wrapper.c，GPL-3.0）
 * https://github.com/FCL-Team/FoldCraftLauncher/pull/1821
 */
#include <jni.h>

extern int fcl_flite_init(void);
extern float fcl_flite_say(const char *text);

JNIEXPORT jint JNICALL init(void) {
    return fcl_flite_init();
}

JNIEXPORT jfloat JNICALL say(const char *text) {
    return fcl_flite_say(text);
}
