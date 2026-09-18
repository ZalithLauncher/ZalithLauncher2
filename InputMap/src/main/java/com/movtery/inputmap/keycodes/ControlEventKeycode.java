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

package com.movtery.inputmap.keycodes;

import androidx.annotation.Nullable;

public class ControlEventKeycode {
    /**
     * 利用Java语言switch的性能，快速匹配点击事件对应的键值
     * @param event 点击事件
     * @return 匹配到的键值，若为null则未匹配到
     */
    public static @Nullable Short getKeycodeFromEvent(String event) {
        switch (event) {
            case GLFW_KEY_UNKNOWN: return LwjglGlfwKeycode.GLFW_KEY_UNKNOWN;
            case GLFW_KEY_SPACE: return LwjglGlfwKeycode.GLFW_KEY_SPACE;
            case GLFW_KEY_APOSTROPHE: return LwjglGlfwKeycode.GLFW_KEY_APOSTROPHE;
            case GLFW_KEY_COMMA: return LwjglGlfwKeycode.GLFW_KEY_COMMA;
            case GLFW_KEY_MINUS: return LwjglGlfwKeycode.GLFW_KEY_MINUS;
            case GLFW_KEY_PERIOD: return LwjglGlfwKeycode.GLFW_KEY_PERIOD;
            case GLFW_KEY_SLASH: return LwjglGlfwKeycode.GLFW_KEY_SLASH;
            case GLFW_KEY_0: return LwjglGlfwKeycode.GLFW_KEY_0;
            case GLFW_KEY_1: return LwjglGlfwKeycode.GLFW_KEY_1;
            case GLFW_KEY_2: return LwjglGlfwKeycode.GLFW_KEY_2;
            case GLFW_KEY_3: return LwjglGlfwKeycode.GLFW_KEY_3;
            case GLFW_KEY_4: return LwjglGlfwKeycode.GLFW_KEY_4;
            case GLFW_KEY_5: return LwjglGlfwKeycode.GLFW_KEY_5;
            case GLFW_KEY_6: return LwjglGlfwKeycode.GLFW_KEY_6;
            case GLFW_KEY_7: return LwjglGlfwKeycode.GLFW_KEY_7;
            case GLFW_KEY_8: return LwjglGlfwKeycode.GLFW_KEY_8;
            case GLFW_KEY_9: return LwjglGlfwKeycode.GLFW_KEY_9;
            case GLFW_KEY_SEMICOLON: return LwjglGlfwKeycode.GLFW_KEY_SEMICOLON;
            case GLFW_KEY_EQUAL: return LwjglGlfwKeycode.GLFW_KEY_EQUAL;
            case GLFW_KEY_A: return LwjglGlfwKeycode.GLFW_KEY_A;
            case GLFW_KEY_B: return LwjglGlfwKeycode.GLFW_KEY_B;
            case GLFW_KEY_C: return LwjglGlfwKeycode.GLFW_KEY_C;
            case GLFW_KEY_D: return LwjglGlfwKeycode.GLFW_KEY_D;
            case GLFW_KEY_E: return LwjglGlfwKeycode.GLFW_KEY_E;
            case GLFW_KEY_F: return LwjglGlfwKeycode.GLFW_KEY_F;
            case GLFW_KEY_G: return LwjglGlfwKeycode.GLFW_KEY_G;
            case GLFW_KEY_H: return LwjglGlfwKeycode.GLFW_KEY_H;
            case GLFW_KEY_I: return LwjglGlfwKeycode.GLFW_KEY_I;
            case GLFW_KEY_J: return LwjglGlfwKeycode.GLFW_KEY_J;
            case GLFW_KEY_K: return LwjglGlfwKeycode.GLFW_KEY_K;
            case GLFW_KEY_L: return LwjglGlfwKeycode.GLFW_KEY_L;
            case GLFW_KEY_M: return LwjglGlfwKeycode.GLFW_KEY_M;
            case GLFW_KEY_N: return LwjglGlfwKeycode.GLFW_KEY_N;
            case GLFW_KEY_O: return LwjglGlfwKeycode.GLFW_KEY_O;
            case GLFW_KEY_P: return LwjglGlfwKeycode.GLFW_KEY_P;
            case GLFW_KEY_Q: return LwjglGlfwKeycode.GLFW_KEY_Q;
            case GLFW_KEY_R: return LwjglGlfwKeycode.GLFW_KEY_R;
            case GLFW_KEY_S: return LwjglGlfwKeycode.GLFW_KEY_S;
            case GLFW_KEY_T: return LwjglGlfwKeycode.GLFW_KEY_T;
            case GLFW_KEY_U: return LwjglGlfwKeycode.GLFW_KEY_U;
            case GLFW_KEY_V: return LwjglGlfwKeycode.GLFW_KEY_V;
            case GLFW_KEY_W: return LwjglGlfwKeycode.GLFW_KEY_W;
            case GLFW_KEY_X: return LwjglGlfwKeycode.GLFW_KEY_X;
            case GLFW_KEY_Y: return LwjglGlfwKeycode.GLFW_KEY_Y;
            case GLFW_KEY_Z: return LwjglGlfwKeycode.GLFW_KEY_Z;
            case GLFW_KEY_LEFT_BRACKET: return LwjglGlfwKeycode.GLFW_KEY_LEFT_BRACKET;
            case GLFW_KEY_BACKSLASH: return LwjglGlfwKeycode.GLFW_KEY_BACKSLASH;
            case GLFW_KEY_RIGHT_BRACKET: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_BRACKET;
            case GLFW_KEY_GRAVE_ACCENT: return LwjglGlfwKeycode.GLFW_KEY_GRAVE_ACCENT;
            case GLFW_KEY_WORLD_1: return LwjglGlfwKeycode.GLFW_KEY_WORLD_1;
            case GLFW_KEY_WORLD_2: return LwjglGlfwKeycode.GLFW_KEY_WORLD_2;
            case GLFW_KEY_ESCAPE: return LwjglGlfwKeycode.GLFW_KEY_ESCAPE;
            case GLFW_KEY_ENTER: return LwjglGlfwKeycode.GLFW_KEY_ENTER;
            case GLFW_KEY_TAB: return LwjglGlfwKeycode.GLFW_KEY_TAB;
            case GLFW_KEY_BACKSPACE: return LwjglGlfwKeycode.GLFW_KEY_BACKSPACE;
            case GLFW_KEY_INSERT: return LwjglGlfwKeycode.GLFW_KEY_INSERT;
            case GLFW_KEY_DELETE: return LwjglGlfwKeycode.GLFW_KEY_DELETE;
            case GLFW_KEY_RIGHT: return LwjglGlfwKeycode.GLFW_KEY_RIGHT;
            case GLFW_KEY_LEFT: return LwjglGlfwKeycode.GLFW_KEY_LEFT;
            case GLFW_KEY_DOWN: return LwjglGlfwKeycode.GLFW_KEY_DOWN;
            case GLFW_KEY_UP: return LwjglGlfwKeycode.GLFW_KEY_UP;
            case GLFW_KEY_PAGE_UP: return LwjglGlfwKeycode.GLFW_KEY_PAGE_UP;
            case GLFW_KEY_PAGE_DOWN: return LwjglGlfwKeycode.GLFW_KEY_PAGE_DOWN;
            case GLFW_KEY_HOME: return LwjglGlfwKeycode.GLFW_KEY_HOME;
            case GLFW_KEY_END: return LwjglGlfwKeycode.GLFW_KEY_END;
            case GLFW_KEY_CAPS_LOCK: return LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK;
            case GLFW_KEY_SCROLL_LOCK: return LwjglGlfwKeycode.GLFW_KEY_SCROLL_LOCK;
            case GLFW_KEY_NUM_LOCK: return LwjglGlfwKeycode.GLFW_KEY_NUM_LOCK;
            case GLFW_KEY_PRINT_SCREEN: return LwjglGlfwKeycode.GLFW_KEY_PRINT_SCREEN;
            case GLFW_KEY_PAUSE: return LwjglGlfwKeycode.GLFW_KEY_PAUSE;
            case GLFW_KEY_F1: return LwjglGlfwKeycode.GLFW_KEY_F1;
            case GLFW_KEY_F2: return LwjglGlfwKeycode.GLFW_KEY_F2;
            case GLFW_KEY_F3: return LwjglGlfwKeycode.GLFW_KEY_F3;
            case GLFW_KEY_F4: return LwjglGlfwKeycode.GLFW_KEY_F4;
            case GLFW_KEY_F5: return LwjglGlfwKeycode.GLFW_KEY_F5;
            case GLFW_KEY_F6: return LwjglGlfwKeycode.GLFW_KEY_F6;
            case GLFW_KEY_F7: return LwjglGlfwKeycode.GLFW_KEY_F7;
            case GLFW_KEY_F8: return LwjglGlfwKeycode.GLFW_KEY_F8;
            case GLFW_KEY_F9: return LwjglGlfwKeycode.GLFW_KEY_F9;
            case GLFW_KEY_F10: return LwjglGlfwKeycode.GLFW_KEY_F10;
            case GLFW_KEY_F11: return LwjglGlfwKeycode.GLFW_KEY_F11;
            case GLFW_KEY_F12: return LwjglGlfwKeycode.GLFW_KEY_F12;
            case GLFW_KEY_F13: return LwjglGlfwKeycode.GLFW_KEY_F13;
            case GLFW_KEY_F14: return LwjglGlfwKeycode.GLFW_KEY_F14;
            case GLFW_KEY_F15: return LwjglGlfwKeycode.GLFW_KEY_F15;
            case GLFW_KEY_F16: return LwjglGlfwKeycode.GLFW_KEY_F16;
            case GLFW_KEY_F17: return LwjglGlfwKeycode.GLFW_KEY_F17;
            case GLFW_KEY_F18: return LwjglGlfwKeycode.GLFW_KEY_F18;
            case GLFW_KEY_F19: return LwjglGlfwKeycode.GLFW_KEY_F19;
            case GLFW_KEY_F20: return LwjglGlfwKeycode.GLFW_KEY_F20;
            case GLFW_KEY_F21: return LwjglGlfwKeycode.GLFW_KEY_F21;
            case GLFW_KEY_F22: return LwjglGlfwKeycode.GLFW_KEY_F22;
            case GLFW_KEY_F23: return LwjglGlfwKeycode.GLFW_KEY_F23;
            case GLFW_KEY_F24: return LwjglGlfwKeycode.GLFW_KEY_F24;
            case GLFW_KEY_F25: return LwjglGlfwKeycode.GLFW_KEY_F25;
            case GLFW_KEY_KP_0: return LwjglGlfwKeycode.GLFW_KEY_KP_0;
            case GLFW_KEY_KP_1: return LwjglGlfwKeycode.GLFW_KEY_KP_1;
            case GLFW_KEY_KP_2: return LwjglGlfwKeycode.GLFW_KEY_KP_2;
            case GLFW_KEY_KP_3: return LwjglGlfwKeycode.GLFW_KEY_KP_3;
            case GLFW_KEY_KP_4: return LwjglGlfwKeycode.GLFW_KEY_KP_4;
            case GLFW_KEY_KP_5: return LwjglGlfwKeycode.GLFW_KEY_KP_5;
            case GLFW_KEY_KP_6: return LwjglGlfwKeycode.GLFW_KEY_KP_6;
            case GLFW_KEY_KP_7: return LwjglGlfwKeycode.GLFW_KEY_KP_7;
            case GLFW_KEY_KP_8: return LwjglGlfwKeycode.GLFW_KEY_KP_8;
            case GLFW_KEY_KP_9: return LwjglGlfwKeycode.GLFW_KEY_KP_9;
            case GLFW_KEY_KP_DECIMAL: return LwjglGlfwKeycode.GLFW_KEY_KP_DECIMAL;
            case GLFW_KEY_KP_DIVIDE: return LwjglGlfwKeycode.GLFW_KEY_KP_DIVIDE;
            case GLFW_KEY_KP_MULTIPLY: return LwjglGlfwKeycode.GLFW_KEY_KP_MULTIPLY;
            case GLFW_KEY_KP_SUBTRACT: return LwjglGlfwKeycode.GLFW_KEY_KP_SUBTRACT;
            case GLFW_KEY_KP_ADD: return LwjglGlfwKeycode.GLFW_KEY_KP_ADD;
            case GLFW_KEY_KP_ENTER: return LwjglGlfwKeycode.GLFW_KEY_KP_ENTER;
            case GLFW_KEY_KP_EQUAL: return LwjglGlfwKeycode.GLFW_KEY_KP_EQUAL;
            case GLFW_KEY_LEFT_SHIFT: return LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT;
            case GLFW_KEY_LEFT_CONTROL: return LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL;
            case GLFW_KEY_LEFT_ALT: return LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT;
            case GLFW_KEY_LEFT_SUPER: return LwjglGlfwKeycode.GLFW_KEY_LEFT_SUPER;
            case GLFW_KEY_RIGHT_SHIFT: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_SHIFT;
            case GLFW_KEY_RIGHT_CONTROL: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_CONTROL;
            case GLFW_KEY_RIGHT_ALT: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_ALT;
            case GLFW_KEY_RIGHT_SUPER: return LwjglGlfwKeycode.GLFW_KEY_RIGHT_SUPER;
            case GLFW_KEY_MENU: return LwjglGlfwKeycode.GLFW_KEY_MENU;
            case GLFW_KEY_LAST: return LwjglGlfwKeycode.GLFW_KEY_LAST;
            case GLFW_MOD_SHIFT: return LwjglGlfwKeycode.GLFW_MOD_SHIFT;
            case GLFW_MOD_CONTROL: return LwjglGlfwKeycode.GLFW_MOD_CONTROL;
            case GLFW_MOD_ALT: return LwjglGlfwKeycode.GLFW_MOD_ALT;
            case GLFW_MOD_SUPER: return LwjglGlfwKeycode.GLFW_MOD_SUPER;
            case GLFW_MOD_CAPS_LOCK: return LwjglGlfwKeycode.GLFW_MOD_CAPS_LOCK;
            case GLFW_MOD_NUM_LOCK: return LwjglGlfwKeycode.GLFW_MOD_NUM_LOCK;
            case GLFW_MOUSE_BUTTON_1: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_1;
            case GLFW_MOUSE_BUTTON_2: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_2;
            case GLFW_MOUSE_BUTTON_3: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_3;
            case GLFW_MOUSE_BUTTON_4: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_4;
            case GLFW_MOUSE_BUTTON_5: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_5;
            case GLFW_MOUSE_BUTTON_6: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_6;
            case GLFW_MOUSE_BUTTON_7: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_7;
            case GLFW_MOUSE_BUTTON_8: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_8;
            case GLFW_MOUSE_BUTTON_LAST: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LAST;
            case GLFW_MOUSE_BUTTON_LEFT: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT;
            case GLFW_MOUSE_BUTTON_RIGHT: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT;
            case GLFW_MOUSE_BUTTON_MIDDLE: return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE;
            default:
                return null;
        }
    }

    // Printable keys
    public static final String GLFW_KEY_UNKNOWN = "GLFW_KEY_UNKNOWN";
    public static final String GLFW_KEY_SPACE = "GLFW_KEY_SPACE";
    public static final String GLFW_KEY_APOSTROPHE = "GLFW_KEY_APOSTROPHE";
    public static final String GLFW_KEY_COMMA = "GLFW_KEY_COMMA";
    public static final String GLFW_KEY_MINUS = "GLFW_KEY_MINUS";
    public static final String GLFW_KEY_PERIOD = "GLFW_KEY_PERIOD";
    public static final String GLFW_KEY_SLASH = "GLFW_KEY_SLASH";
    public static final String GLFW_KEY_0 = "GLFW_KEY_0";
    public static final String GLFW_KEY_1 = "GLFW_KEY_1";
    public static final String GLFW_KEY_2 = "GLFW_KEY_2";
    public static final String GLFW_KEY_3 = "GLFW_KEY_3";
    public static final String GLFW_KEY_4 = "GLFW_KEY_4";
    public static final String GLFW_KEY_5 = "GLFW_KEY_5";
    public static final String GLFW_KEY_6 = "GLFW_KEY_6";
    public static final String GLFW_KEY_7 = "GLFW_KEY_7";
    public static final String GLFW_KEY_8 = "GLFW_KEY_8";
    public static final String GLFW_KEY_9 = "GLFW_KEY_9";
    public static final String GLFW_KEY_SEMICOLON = "GLFW_KEY_SEMICOLON";
    public static final String GLFW_KEY_EQUAL = "GLFW_KEY_EQUAL";
    public static final String GLFW_KEY_A = "GLFW_KEY_A";
    public static final String GLFW_KEY_B = "GLFW_KEY_B";
    public static final String GLFW_KEY_C = "GLFW_KEY_C";
    public static final String GLFW_KEY_D = "GLFW_KEY_D";
    public static final String GLFW_KEY_E = "GLFW_KEY_E";
    public static final String GLFW_KEY_F = "GLFW_KEY_F";
    public static final String GLFW_KEY_G = "GLFW_KEY_G";
    public static final String GLFW_KEY_H = "GLFW_KEY_H";
    public static final String GLFW_KEY_I = "GLFW_KEY_I";
    public static final String GLFW_KEY_J = "GLFW_KEY_J";
    public static final String GLFW_KEY_K = "GLFW_KEY_K";
    public static final String GLFW_KEY_L = "GLFW_KEY_L";
    public static final String GLFW_KEY_M = "GLFW_KEY_M";
    public static final String GLFW_KEY_N = "GLFW_KEY_N";
    public static final String GLFW_KEY_O = "GLFW_KEY_O";
    public static final String GLFW_KEY_P = "GLFW_KEY_P";
    public static final String GLFW_KEY_Q = "GLFW_KEY_Q";
    public static final String GLFW_KEY_R = "GLFW_KEY_R";
    public static final String GLFW_KEY_S = "GLFW_KEY_S";
    public static final String GLFW_KEY_T = "GLFW_KEY_T";
    public static final String GLFW_KEY_U = "GLFW_KEY_U";
    public static final String GLFW_KEY_V = "GLFW_KEY_V";
    public static final String GLFW_KEY_W = "GLFW_KEY_W";
    public static final String GLFW_KEY_X = "GLFW_KEY_X";
    public static final String GLFW_KEY_Y = "GLFW_KEY_Y";
    public static final String GLFW_KEY_Z = "GLFW_KEY_Z";
    public static final String GLFW_KEY_LEFT_BRACKET = "GLFW_KEY_LEFT_BRACKET";
    public static final String GLFW_KEY_BACKSLASH = "GLFW_KEY_BACKSLASH";
    public static final String GLFW_KEY_RIGHT_BRACKET = "GLFW_KEY_RIGHT_BRACKET";
    public static final String GLFW_KEY_GRAVE_ACCENT = "GLFW_KEY_GRAVE_ACCENT";
    public static final String GLFW_KEY_WORLD_1 = "GLFW_KEY_WORLD_1";
    public static final String GLFW_KEY_WORLD_2 = "GLFW_KEY_WORLD_2";

    // Function keys
    public static final String GLFW_KEY_ESCAPE = "GLFW_KEY_ESCAPE";
    public static final String GLFW_KEY_ENTER = "GLFW_KEY_ENTER";
    public static final String GLFW_KEY_TAB = "GLFW_KEY_TAB";
    public static final String GLFW_KEY_BACKSPACE = "GLFW_KEY_BACKSPACE";
    public static final String GLFW_KEY_INSERT = "GLFW_KEY_INSERT";
    public static final String GLFW_KEY_DELETE = "GLFW_KEY_DELETE";
    public static final String GLFW_KEY_RIGHT = "GLFW_KEY_RIGHT";
    public static final String GLFW_KEY_LEFT = "GLFW_KEY_LEFT";
    public static final String GLFW_KEY_DOWN = "GLFW_KEY_DOWN";
    public static final String GLFW_KEY_UP = "GLFW_KEY_UP";
    public static final String GLFW_KEY_PAGE_UP = "GLFW_KEY_PAGE_UP";
    public static final String GLFW_KEY_PAGE_DOWN = "GLFW_KEY_PAGE_DOWN";
    public static final String GLFW_KEY_HOME = "GLFW_KEY_HOME";
    public static final String GLFW_KEY_END = "GLFW_KEY_END";
    public static final String GLFW_KEY_CAPS_LOCK = "GLFW_KEY_CAPS_LOCK";
    public static final String GLFW_KEY_SCROLL_LOCK = "GLFW_KEY_SCROLL_LOCK";
    public static final String GLFW_KEY_NUM_LOCK = "GLFW_KEY_NUM_LOCK";
    public static final String GLFW_KEY_PRINT_SCREEN = "GLFW_KEY_PRINT_SCREEN";
    public static final String GLFW_KEY_PAUSE = "GLFW_KEY_PAUSE";
    public static final String GLFW_KEY_F1 = "GLFW_KEY_F1";
    public static final String GLFW_KEY_F2 = "GLFW_KEY_F2";
    public static final String GLFW_KEY_F3 = "GLFW_KEY_F3";
    public static final String GLFW_KEY_F4 = "GLFW_KEY_F4";
    public static final String GLFW_KEY_F5 = "GLFW_KEY_F5";
    public static final String GLFW_KEY_F6 = "GLFW_KEY_F6";
    public static final String GLFW_KEY_F7 = "GLFW_KEY_F7";
    public static final String GLFW_KEY_F8 = "GLFW_KEY_F8";
    public static final String GLFW_KEY_F9 = "GLFW_KEY_F9";
    public static final String GLFW_KEY_F10 = "GLFW_KEY_F10";
    public static final String GLFW_KEY_F11 = "GLFW_KEY_F11";
    public static final String GLFW_KEY_F12 = "GLFW_KEY_F12";
    public static final String GLFW_KEY_F13 = "GLFW_KEY_F13";
    public static final String GLFW_KEY_F14 = "GLFW_KEY_F14";
    public static final String GLFW_KEY_F15 = "GLFW_KEY_F15";
    public static final String GLFW_KEY_F16 = "GLFW_KEY_F16";
    public static final String GLFW_KEY_F17 = "GLFW_KEY_F17";
    public static final String GLFW_KEY_F18 = "GLFW_KEY_F18";
    public static final String GLFW_KEY_F19 = "GLFW_KEY_F19";
    public static final String GLFW_KEY_F20 = "GLFW_KEY_F20";
    public static final String GLFW_KEY_F21 = "GLFW_KEY_F21";
    public static final String GLFW_KEY_F22 = "GLFW_KEY_F22";
    public static final String GLFW_KEY_F23 = "GLFW_KEY_F23";
    public static final String GLFW_KEY_F24 = "GLFW_KEY_F24";
    public static final String GLFW_KEY_F25 = "GLFW_KEY_F25";
    public static final String GLFW_KEY_KP_0 = "GLFW_KEY_KP_0";
    public static final String GLFW_KEY_KP_1 = "GLFW_KEY_KP_1";
    public static final String GLFW_KEY_KP_2 = "GLFW_KEY_KP_2";
    public static final String GLFW_KEY_KP_3 = "GLFW_KEY_KP_3";
    public static final String GLFW_KEY_KP_4 = "GLFW_KEY_KP_4";
    public static final String GLFW_KEY_KP_5 = "GLFW_KEY_KP_5";
    public static final String GLFW_KEY_KP_6 = "GLFW_KEY_KP_6";
    public static final String GLFW_KEY_KP_7 = "GLFW_KEY_KP_7";
    public static final String GLFW_KEY_KP_8 = "GLFW_KEY_KP_8";
    public static final String GLFW_KEY_KP_9 = "GLFW_KEY_KP_9";
    public static final String GLFW_KEY_KP_DECIMAL = "GLFW_KEY_KP_DECIMAL";
    public static final String GLFW_KEY_KP_DIVIDE = "GLFW_KEY_KP_DIVIDE";
    public static final String GLFW_KEY_KP_MULTIPLY = "GLFW_KEY_KP_MULTIPLY";
    public static final String GLFW_KEY_KP_SUBTRACT = "GLFW_KEY_KP_SUBTRACT";
    public static final String GLFW_KEY_KP_ADD = "GLFW_KEY_KP_ADD";
    public static final String GLFW_KEY_KP_ENTER = "GLFW_KEY_KP_ENTER";
    public static final String GLFW_KEY_KP_EQUAL = "GLFW_KEY_KP_EQUAL";
    public static final String GLFW_KEY_LEFT_SHIFT = "GLFW_KEY_LEFT_SHIFT";
    public static final String GLFW_KEY_LEFT_CONTROL = "GLFW_KEY_LEFT_CONTROL";
    public static final String GLFW_KEY_LEFT_ALT = "GLFW_KEY_LEFT_ALT";
    public static final String GLFW_KEY_LEFT_SUPER = "GLFW_KEY_LEFT_SUPER";
    public static final String GLFW_KEY_RIGHT_SHIFT = "GLFW_KEY_RIGHT_SHIFT";
    public static final String GLFW_KEY_RIGHT_CONTROL = "GLFW_KEY_RIGHT_CONTROL";
    public static final String GLFW_KEY_RIGHT_ALT = "GLFW_KEY_RIGHT_ALT";
    public static final String GLFW_KEY_RIGHT_SUPER = "GLFW_KEY_RIGHT_SUPER";
    public static final String GLFW_KEY_MENU = "GLFW_KEY_MENU";
    public static final String GLFW_KEY_LAST = "GLFW_KEY_LAST";

    // Modifier keys
    public static final String GLFW_MOD_SHIFT = "GLFW_MOD_SHIFT";
    public static final String GLFW_MOD_CONTROL = "GLFW_MOD_CONTROL";
    public static final String GLFW_MOD_ALT = "GLFW_MOD_ALT";
    public static final String GLFW_MOD_SUPER = "GLFW_MOD_SUPER";
    public static final String GLFW_MOD_CAPS_LOCK = "GLFW_MOD_CAPS_LOCK";
    public static final String GLFW_MOD_NUM_LOCK = "GLFW_MOD_NUM_LOCK";

    // Mouse buttons
    public static final String GLFW_MOUSE_BUTTON_1 = "GLFW_MOUSE_BUTTON_1";
    public static final String GLFW_MOUSE_BUTTON_2 = "GLFW_MOUSE_BUTTON_2";
    public static final String GLFW_MOUSE_BUTTON_3 = "GLFW_MOUSE_BUTTON_3";
    public static final String GLFW_MOUSE_BUTTON_4 = "GLFW_MOUSE_BUTTON_4";
    public static final String GLFW_MOUSE_BUTTON_5 = "GLFW_MOUSE_BUTTON_5";
    public static final String GLFW_MOUSE_BUTTON_6 = "GLFW_MOUSE_BUTTON_6";
    public static final String GLFW_MOUSE_BUTTON_7 = "GLFW_MOUSE_BUTTON_7";
    public static final String GLFW_MOUSE_BUTTON_8 = "GLFW_MOUSE_BUTTON_8";
    public static final String GLFW_MOUSE_BUTTON_LAST = "GLFW_MOUSE_BUTTON_LAST";
    public static final String GLFW_MOUSE_BUTTON_LEFT = "GLFW_MOUSE_BUTTON_LEFT";
    public static final String GLFW_MOUSE_BUTTON_RIGHT = "GLFW_MOUSE_BUTTON_RIGHT";
    public static final String GLFW_MOUSE_BUTTON_MIDDLE = "GLFW_MOUSE_BUTTON_MIDDLE";
}