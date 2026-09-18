/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.bridge;

import android.content.Context;

import androidx.annotation.Keep;

@Keep
public final class ZLBridge {
    //AWT
    public static final int EVENT_TYPE_CHAR = 1000;
    public static final int EVENT_TYPE_CURSOR_POS = 1003;
    public static final int EVENT_TYPE_KEY = 1005;
    public static final int EVENT_TYPE_MOUSE_BUTTON = 1006;

    public static void sendKey(char keychar, int keycode) {
        // TODO: Android -> AWT keycode mapping
        sendInputData(EVENT_TYPE_KEY, (int) keychar, keycode, 1, 0);
        sendInputData(EVENT_TYPE_KEY, (int) keychar, keycode, 0, 0);
    }

    public static void sendKey(char keychar, int keycode, int state) {
        // TODO: Android -> AWT keycode mapping
        sendInputData(EVENT_TYPE_KEY, (int) keychar, keycode, state, 0);
    }

    public static void sendChar(char keychar){
        sendInputData(EVENT_TYPE_CHAR, (int) keychar, 0, 0, 0);
    }

    public static void sendMousePress(int awtButtons, boolean isDown) {
        sendInputData(EVENT_TYPE_MOUSE_BUTTON, awtButtons, isDown ? 1 : 0, 0, 0);
    }

    public static void sendMousePress(int awtButtons) {
        sendMousePress(awtButtons, true);
        sendMousePress(awtButtons, false);
    }

    public static void sendMousePos(int x, int y) {
        sendInputData(EVENT_TYPE_CURSOR_POS, x, y, 0, 0);
    }

    //Game
    @Keep public static native void initializeGameExitHook();
    @Keep public static native void setupExitMethod(Context context);

    //Launch
    @Keep public static native void setLdLibraryPath(String ldLibraryPath);
    @Keep public static native boolean dlopen(String libPath);

    //Render
    @Keep public static native void setupBridgeWindow(Object surface);
    @Keep public static native void releaseBridgeWindow();
    @Keep public static native void moveWindow(int xOffset, int yOffset);
    @Keep public static native int[] renderAWTScreenFrame();

    //Input
    @Keep public static native void sendInputData(int type, int i1, int i2, int i3, int i4);
    @Keep public static native void clipboardReceived(String data, String mimeTypeSub);

    //Utils
    @Keep public static native int chdir(String path);

    //Narrator (flite)
    /**
     * 返回当前（Dalvik/ART）JavaVM 指针，供游戏 JVM 侧原生代码经环境变量 attach 回安卓运行时
     */
    @Keep
    public static native long getJavaVMPointer();

    /**
     * 为对象创建全局引用并以十六进制字符串返回其地址，配合 {@link #getJavaVMPointer()} 注入环境变量
     */
    @Keep
    public static native String jObjectToString(Object object);

    static {
        NativeLibraryLoader.loadExitHookLib();
        NativeLibraryLoader.loadPojavLib();
        NativeLibraryLoader.loadPojavAWTLib();
    }
}
