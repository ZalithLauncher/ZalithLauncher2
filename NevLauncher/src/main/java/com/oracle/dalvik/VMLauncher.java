package com.oracle.dalvik;

import androidx.annotation.Keep;

@Keep
public final class VMLauncher {
	private VMLauncher() {
	}
    @Keep public static native int launchJVM(String[] args);
}
