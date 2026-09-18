/*
 * 来源：FoldCraftLauncher PR #1821（FCL/src/main/jni/flite/flite_cmu_us_kal16.c，GPL-3.0）
 * https://github.com/FCL-Team/FoldCraftLauncher/pull/1821
 */
#include <jni.h>

static int dummy_voice;

JNIEXPORT void *JNICALL register_cmu_us_kal16(const char *dir) {
    (void) dir;
    return &dummy_voice;
}
