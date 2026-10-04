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

package com.movtery.zalithlauncher.game.input;

import static org.lwjgl.glfw.CallbackBridge.sendKeyPress;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.movtery.inputmap.keycodes.LwjglGlfwKeycode;

import org.lwjgl.glfw.CallbackBridge;

import java.util.Arrays;

/**
 * <a href="https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/EfficientAndroidLWJGLKeycode.java">Modified from PojavLauncher</a>
 */
public class EfficientAndroidLWJGLKeycode {

    //This old version of this class was using an ArrayMap, a generic Key -> Value data structure.
    //The key being the android keycode from a KeyEvent
    //The value its LWJGL equivalent.
    private static final int KEYCODE_COUNT = 112;
    private static final int[] sAndroidKeycodes = new int[KEYCODE_COUNT];
    private static final short[] sLwjglKeycodes = new short[KEYCODE_COUNT];
    private static int mTmpCount = 0;

    static {
        /*  BINARY SEARCH IS PERFORMED ON THE androidKeycodes ARRAY !
            WHEN ADDING A MAPPING, ADD IT SO THE androidKeycodes ARRAY STAYS SORTED ! */
        // Mapping Android Keycodes to LWJGL Keycodes
        add(KeyEvent.KEYCODE_UNKNOWN, LwjglGlfwKeycode.GLFW_KEY_UNKNOWN);
        add(KeyEvent.KEYCODE_HOME, LwjglGlfwKeycode.GLFW_KEY_HOME);
        // Escape key
        add(KeyEvent.KEYCODE_BACK, LwjglGlfwKeycode.GLFW_KEY_ESCAPE);

        // 0-9 keys
        add(KeyEvent.KEYCODE_0, LwjglGlfwKeycode.GLFW_KEY_0); //7
        add(KeyEvent.KEYCODE_1, LwjglGlfwKeycode.GLFW_KEY_1);
        add(KeyEvent.KEYCODE_2, LwjglGlfwKeycode.GLFW_KEY_2);
        add(KeyEvent.KEYCODE_3, LwjglGlfwKeycode.GLFW_KEY_3);
        add(KeyEvent.KEYCODE_4, LwjglGlfwKeycode.GLFW_KEY_4);
        add(KeyEvent.KEYCODE_5, LwjglGlfwKeycode.GLFW_KEY_5);
        add(KeyEvent.KEYCODE_6, LwjglGlfwKeycode.GLFW_KEY_6);
        add(KeyEvent.KEYCODE_7, LwjglGlfwKeycode.GLFW_KEY_7);
        add(KeyEvent.KEYCODE_8, LwjglGlfwKeycode.GLFW_KEY_8);
        add(KeyEvent.KEYCODE_9, LwjglGlfwKeycode.GLFW_KEY_9); //16

        add(KeyEvent.KEYCODE_POUND, LwjglGlfwKeycode.GLFW_KEY_3);

        // Arrow keys
        add(KeyEvent.KEYCODE_DPAD_UP, LwjglGlfwKeycode.GLFW_KEY_UP); //19
        add(KeyEvent.KEYCODE_DPAD_DOWN, LwjglGlfwKeycode.GLFW_KEY_DOWN);
        add(KeyEvent.KEYCODE_DPAD_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT);
        add(KeyEvent.KEYCODE_DPAD_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT); //22

        // A-Z keys
        add(KeyEvent.KEYCODE_A, LwjglGlfwKeycode.GLFW_KEY_A); //29
        add(KeyEvent.KEYCODE_B, LwjglGlfwKeycode.GLFW_KEY_B);
        add(KeyEvent.KEYCODE_C, LwjglGlfwKeycode.GLFW_KEY_C);
        add(KeyEvent.KEYCODE_D, LwjglGlfwKeycode.GLFW_KEY_D);
        add(KeyEvent.KEYCODE_E, LwjglGlfwKeycode.GLFW_KEY_E);
        add(KeyEvent.KEYCODE_F, LwjglGlfwKeycode.GLFW_KEY_F);
        add(KeyEvent.KEYCODE_G, LwjglGlfwKeycode.GLFW_KEY_G);
        add(KeyEvent.KEYCODE_H, LwjglGlfwKeycode.GLFW_KEY_H);
        add(KeyEvent.KEYCODE_I, LwjglGlfwKeycode.GLFW_KEY_I);
        add(KeyEvent.KEYCODE_J, LwjglGlfwKeycode.GLFW_KEY_J);
        add(KeyEvent.KEYCODE_K, LwjglGlfwKeycode.GLFW_KEY_K);
        add(KeyEvent.KEYCODE_L, LwjglGlfwKeycode.GLFW_KEY_L);
        add(KeyEvent.KEYCODE_M, LwjglGlfwKeycode.GLFW_KEY_M);
        add(KeyEvent.KEYCODE_N, LwjglGlfwKeycode.GLFW_KEY_N);
        add(KeyEvent.KEYCODE_O, LwjglGlfwKeycode.GLFW_KEY_O);
        add(KeyEvent.KEYCODE_P, LwjglGlfwKeycode.GLFW_KEY_P);
        add(KeyEvent.KEYCODE_Q, LwjglGlfwKeycode.GLFW_KEY_Q);
        add(KeyEvent.KEYCODE_R, LwjglGlfwKeycode.GLFW_KEY_R);
        add(KeyEvent.KEYCODE_S, LwjglGlfwKeycode.GLFW_KEY_S);
        add(KeyEvent.KEYCODE_T, LwjglGlfwKeycode.GLFW_KEY_T);
        add(KeyEvent.KEYCODE_U, LwjglGlfwKeycode.GLFW_KEY_U);
        add(KeyEvent.KEYCODE_V, LwjglGlfwKeycode.GLFW_KEY_V);
        add(KeyEvent.KEYCODE_W, LwjglGlfwKeycode.GLFW_KEY_W);
        add(KeyEvent.KEYCODE_X, LwjglGlfwKeycode.GLFW_KEY_X);
        add(KeyEvent.KEYCODE_Y, LwjglGlfwKeycode.GLFW_KEY_Y);
        add(KeyEvent.KEYCODE_Z, LwjglGlfwKeycode.GLFW_KEY_Z); //54


        add(KeyEvent.KEYCODE_COMMA, LwjglGlfwKeycode.GLFW_KEY_COMMA);
        add(KeyEvent.KEYCODE_PERIOD, LwjglGlfwKeycode.GLFW_KEY_PERIOD);

        // Alt keys
        add(KeyEvent.KEYCODE_ALT_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT);
        add(KeyEvent.KEYCODE_ALT_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT_ALT);

        // Shift keys
        add(KeyEvent.KEYCODE_SHIFT_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT);
        add(KeyEvent.KEYCODE_SHIFT_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT_SHIFT);

        add(KeyEvent.KEYCODE_TAB, LwjglGlfwKeycode.GLFW_KEY_TAB);
        add(KeyEvent.KEYCODE_SPACE, LwjglGlfwKeycode.GLFW_KEY_SPACE);
        add(KeyEvent.KEYCODE_ENTER, LwjglGlfwKeycode.GLFW_KEY_ENTER); //66
        add(KeyEvent.KEYCODE_DEL, LwjglGlfwKeycode.GLFW_KEY_BACKSPACE); // Backspace
        add(KeyEvent.KEYCODE_GRAVE, LwjglGlfwKeycode.GLFW_KEY_GRAVE_ACCENT);
        add(KeyEvent.KEYCODE_MINUS, LwjglGlfwKeycode.GLFW_KEY_MINUS);
        add(KeyEvent.KEYCODE_EQUALS, LwjglGlfwKeycode.GLFW_KEY_EQUAL);
        add(KeyEvent.KEYCODE_LEFT_BRACKET, LwjglGlfwKeycode.GLFW_KEY_LEFT_BRACKET);
        add(KeyEvent.KEYCODE_RIGHT_BRACKET, LwjglGlfwKeycode.GLFW_KEY_RIGHT_BRACKET);
        add(KeyEvent.KEYCODE_BACKSLASH, LwjglGlfwKeycode.GLFW_KEY_BACKSLASH);
        add(KeyEvent.KEYCODE_SEMICOLON, LwjglGlfwKeycode.GLFW_KEY_SEMICOLON); //74
        add(KeyEvent.KEYCODE_APOSTROPHE, LwjglGlfwKeycode.GLFW_KEY_APOSTROPHE);
        add(KeyEvent.KEYCODE_SLASH, LwjglGlfwKeycode.GLFW_KEY_SLASH); //76
        add(KeyEvent.KEYCODE_AT, LwjglGlfwKeycode.GLFW_KEY_2);
        add(KeyEvent.KEYCODE_PLUS, LwjglGlfwKeycode.GLFW_KEY_KP_ADD);
        add(KeyEvent.KEYCODE_MENU, LwjglGlfwKeycode.GLFW_KEY_MENU);

        // Page keys
        add(KeyEvent.KEYCODE_PAGE_UP, LwjglGlfwKeycode.GLFW_KEY_PAGE_UP); //92
        add(KeyEvent.KEYCODE_PAGE_DOWN, LwjglGlfwKeycode.GLFW_KEY_PAGE_DOWN);

        add(KeyEvent.KEYCODE_ESCAPE, LwjglGlfwKeycode.GLFW_KEY_ESCAPE);
        add(KeyEvent.KEYCODE_FORWARD_DEL, LwjglGlfwKeycode.GLFW_KEY_DELETE);

        // Control keys
        add(KeyEvent.KEYCODE_CTRL_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL);
        add(KeyEvent.KEYCODE_CTRL_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT_CONTROL);

        add(KeyEvent.KEYCODE_CAPS_LOCK, LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK);
        add(KeyEvent.KEYCODE_SCROLL_LOCK, LwjglGlfwKeycode.GLFW_KEY_SCROLL_LOCK);
        add(KeyEvent.KEYCODE_META_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT_SUPER);
        add(KeyEvent.KEYCODE_META_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT_SUPER);
        add(KeyEvent.KEYCODE_SYSRQ, LwjglGlfwKeycode.GLFW_KEY_PRINT_SCREEN);
        add(KeyEvent.KEYCODE_BREAK, LwjglGlfwKeycode.GLFW_KEY_PAUSE);
        add(KeyEvent.KEYCODE_MOVE_HOME, LwjglGlfwKeycode.GLFW_KEY_HOME);
        add(KeyEvent.KEYCODE_MOVE_END, LwjglGlfwKeycode.GLFW_KEY_END);
        add(KeyEvent.KEYCODE_INSERT, LwjglGlfwKeycode.GLFW_KEY_INSERT);


        // Fn keys
        add(KeyEvent.KEYCODE_F1, LwjglGlfwKeycode.GLFW_KEY_F1); //131
        add(KeyEvent.KEYCODE_F2, LwjglGlfwKeycode.GLFW_KEY_F2);
        add(KeyEvent.KEYCODE_F3, LwjglGlfwKeycode.GLFW_KEY_F3);
        add(KeyEvent.KEYCODE_F4, LwjglGlfwKeycode.GLFW_KEY_F4);
        add(KeyEvent.KEYCODE_F5, LwjglGlfwKeycode.GLFW_KEY_F5);
        add(KeyEvent.KEYCODE_F6, LwjglGlfwKeycode.GLFW_KEY_F6);
        add(KeyEvent.KEYCODE_F7, LwjglGlfwKeycode.GLFW_KEY_F7);
        add(KeyEvent.KEYCODE_F8, LwjglGlfwKeycode.GLFW_KEY_F8);
        add(KeyEvent.KEYCODE_F9, LwjglGlfwKeycode.GLFW_KEY_F9);
        add(KeyEvent.KEYCODE_F10, LwjglGlfwKeycode.GLFW_KEY_F10);
        add(KeyEvent.KEYCODE_F11, LwjglGlfwKeycode.GLFW_KEY_F11);
        add(KeyEvent.KEYCODE_F12, LwjglGlfwKeycode.GLFW_KEY_F12); //142

        // Num keys
        add(KeyEvent.KEYCODE_NUM_LOCK, LwjglGlfwKeycode.GLFW_KEY_NUM_LOCK); //143
        add(KeyEvent.KEYCODE_NUMPAD_0, LwjglGlfwKeycode.GLFW_KEY_KP_0);
        add(KeyEvent.KEYCODE_NUMPAD_1, LwjglGlfwKeycode.GLFW_KEY_KP_1);
        add(KeyEvent.KEYCODE_NUMPAD_2, LwjglGlfwKeycode.GLFW_KEY_KP_2);
        add(KeyEvent.KEYCODE_NUMPAD_3, LwjglGlfwKeycode.GLFW_KEY_KP_3);
        add(KeyEvent.KEYCODE_NUMPAD_4, LwjglGlfwKeycode.GLFW_KEY_KP_4);
        add(KeyEvent.KEYCODE_NUMPAD_5, LwjglGlfwKeycode.GLFW_KEY_KP_5);
        add(KeyEvent.KEYCODE_NUMPAD_6, LwjglGlfwKeycode.GLFW_KEY_KP_6);
        add(KeyEvent.KEYCODE_NUMPAD_7, LwjglGlfwKeycode.GLFW_KEY_KP_7);
        add(KeyEvent.KEYCODE_NUMPAD_8, LwjglGlfwKeycode.GLFW_KEY_KP_8);
        add(KeyEvent.KEYCODE_NUMPAD_9, LwjglGlfwKeycode.GLFW_KEY_KP_9);
        add(KeyEvent.KEYCODE_NUMPAD_DIVIDE, LwjglGlfwKeycode.GLFW_KEY_KP_DIVIDE);
        add(KeyEvent.KEYCODE_NUMPAD_MULTIPLY, LwjglGlfwKeycode.GLFW_KEY_KP_MULTIPLY);
        add(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, LwjglGlfwKeycode.GLFW_KEY_KP_SUBTRACT);
        add(KeyEvent.KEYCODE_NUMPAD_ADD, LwjglGlfwKeycode.GLFW_KEY_KP_ADD);
        add(KeyEvent.KEYCODE_NUMPAD_DOT, LwjglGlfwKeycode.GLFW_KEY_KP_DECIMAL);
        add(KeyEvent.KEYCODE_NUMPAD_COMMA, LwjglGlfwKeycode.GLFW_KEY_COMMA);
        add(KeyEvent.KEYCODE_NUMPAD_ENTER, LwjglGlfwKeycode.GLFW_KEY_KP_ENTER);
        add(KeyEvent.KEYCODE_NUMPAD_EQUALS, LwjglGlfwKeycode.GLFW_KEY_KP_EQUAL); //161

    }

    public static boolean containsIndex(int index){
        return index >= 0;
    }

    public static void execKey(KeyEvent keyEvent, int valueIndex) {
        //valueIndex points to where the value is stored in the array.
        CallbackBridge.holdingAlt = keyEvent.isAltPressed();
        CallbackBridge.holdingCapslock = keyEvent.isCapsLockOn();
        CallbackBridge.holdingCtrl = keyEvent.isCtrlPressed();
        CallbackBridge.holdingNumlock = keyEvent.isNumLockOn();
        CallbackBridge.holdingShift = keyEvent.isShiftPressed();

        char key = (char)(keyEvent.getUnicodeChar() != 0 ? keyEvent.getUnicodeChar() : '\u0000');
        sendKeyPress(
                getValueByIndex(valueIndex),
                key,
                keyEvent.getScanCode(),
                CallbackBridge.getCurrentMods(),
                keyEvent.getAction() == KeyEvent.ACTION_DOWN);
    }

    public static void execKeyIndex(int index){
        //Send a quick key press.
        sendKeyPress(getValueByIndex(index));
    }

    public static short getValueByIndex(int index) {
        return sLwjglKeycodes[index];
    }

    public static int getIndexByKey(int key){
        return Arrays.binarySearch(sAndroidKeycodes, key);
    }

    /** @return the index at which the key is in the array, searching linearly */
    public static int getIndexByValue(int lwjglKey) {
        //You should avoid using this function on performance critical areas
        for (int i = 0; i < sLwjglKeycodes.length; i++) {
            if(sLwjglKeycodes[i] == lwjglKey) return i;
        }
        return -1;
    }

    private static void add(int androidKeycode, short LWJGLKeycode){
        sAndroidKeycodes[mTmpCount] = androidKeycode;
        sLwjglKeycodes[mTmpCount] = LWJGLKeycode;

        mTmpCount ++;
    }

    private static final KeyCharacterMap mKcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    private static final char[] buffer = new char[1];

    /**
     * Takes a GLFW keycode and returns equivalent android keycode.
     */
    public static int getAndroidKeycode(int lwjglGlfwKeycode){
        if (lwjglGlfwKeycode == LwjglGlfwKeycode.GLFW_KEY_2) return KeyEvent.KEYCODE_2;
        if (lwjglGlfwKeycode == LwjglGlfwKeycode.GLFW_KEY_3) return KeyEvent.KEYCODE_3;
        int index = getIndexByValue(lwjglGlfwKeycode);
        return index >= 0 && index < sAndroidKeycodes.length
                ? sAndroidKeycodes[index]
                : KeyEvent.KEYCODE_UNKNOWN;
    }

    public static int getSdlAndroidKeycode(int lwjglGlfwKeycode) {
        return switch (lwjglGlfwKeycode) {
            case LwjglGlfwKeycode.GLFW_KEY_ESCAPE -> KeyEvent.KEYCODE_ESCAPE;
            case LwjglGlfwKeycode.GLFW_KEY_HOME -> KeyEvent.KEYCODE_MOVE_HOME;
            case LwjglGlfwKeycode.GLFW_KEY_END -> KeyEvent.KEYCODE_MOVE_END;
            case LwjglGlfwKeycode.GLFW_KEY_KP_ADD -> KeyEvent.KEYCODE_NUMPAD_ADD;
            case LwjglGlfwKeycode.GLFW_KEY_KP_DECIMAL -> KeyEvent.KEYCODE_NUMPAD_DOT;
            case LwjglGlfwKeycode.GLFW_KEY_KP_ENTER -> KeyEvent.KEYCODE_NUMPAD_ENTER;
            case LwjglGlfwKeycode.GLFW_KEY_DELETE -> KeyEvent.KEYCODE_FORWARD_DEL;
            case LwjglGlfwKeycode.GLFW_KEY_KP_EQUAL -> KeyEvent.KEYCODE_NUMPAD_EQUALS;
            case LwjglGlfwKeycode.GLFW_KEY_LEFT_SUPER -> KeyEvent.KEYCODE_META_LEFT;
            case LwjglGlfwKeycode.GLFW_KEY_RIGHT_SUPER -> KeyEvent.KEYCODE_META_RIGHT;
            case LwjglGlfwKeycode.GLFW_KEY_MENU -> KeyEvent.KEYCODE_MENU;
            default -> getAndroidKeycode(lwjglGlfwKeycode);
        };
    }

    /**
     * Takes a char and returns equivalent android keycode.
     */
    public static int getAndroidKeycode(char c){
        buffer[0] = c;
        KeyEvent[] events = mKcm.getEvents(buffer);
        return events != null && events.length > 0
                ? events[0].getKeyCode()
                : KeyEvent.KEYCODE_UNKNOWN;
    }
}
