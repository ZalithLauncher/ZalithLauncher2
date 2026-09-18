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

public class MinecraftKeyBindingMapper {
    /**
     * Minecraft 绑定按键映射表
     *
     * @param keybinding 绑定的按键
     * @return 对应的GLFW按键
     */
    public static @Nullable Short getGlfwKeycode(String keybinding) {
        switch (keybinding) {
            case "key.keyboard.unknown": return LwjglGlfwKeycode.GLFW_KEY_UNKNOWN;
            case "key.mouse.left": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT;
            case "key.mouse.right": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT;
            case "key.mouse.middle": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE;
            case "key.mouse.4": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_4;
            case "key.mouse.5": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_5;
            case "key.mouse.6": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_6;
            case "key.mouse.7": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_7;
            case "key.mouse.8": return LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_8;
            case "key.keyboard.0": return LwjglGlfwKeycode.GLFW_KEY_0;
            case "key.keyboard.1": return LwjglGlfwKeycode.GLFW_KEY_1;
            case "key.keyboard.2": return LwjglGlfwKeycode.GLFW_KEY_2;
            case "key.keyboard.3": return LwjglGlfwKeycode.GLFW_KEY_3;
            case "key.keyboard.4": return LwjglGlfwKeycode.GLFW_KEY_4;
            case "key.keyboard.5": return LwjglGlfwKeycode.GLFW_KEY_5;
            case "key.keyboard.6": return LwjglGlfwKeycode.GLFW_KEY_6;
            case "key.keyboard.7": return LwjglGlfwKeycode.GLFW_KEY_7;
            case "key.keyboard.8": return LwjglGlfwKeycode.GLFW_KEY_8;
            case "key.keyboard.9": return LwjglGlfwKeycode.GLFW_KEY_9;
            case "key.keyboard.a": return LwjglGlfwKeycode.GLFW_KEY_A;
            case "key.keyboard.b": return LwjglGlfwKeycode.GLFW_KEY_B;
            case "key.keyboard.c": return LwjglGlfwKeycode.GLFW_KEY_C;
            case "key.keyboard.d": return LwjglGlfwKeycode.GLFW_KEY_D;
            case "key.keyboard.e": return LwjglGlfwKeycode.GLFW_KEY_E;
            case "key.keyboard.f": return LwjglGlfwKeycode.GLFW_KEY_F;
            case "key.keyboard.g": return LwjglGlfwKeycode.GLFW_KEY_G;
            case "key.keyboard.h": return LwjglGlfwKeycode.GLFW_KEY_H;
            case "key.keyboard.i": return LwjglGlfwKeycode.GLFW_KEY_I;
            case "key.keyboard.j": return LwjglGlfwKeycode.GLFW_KEY_J;
            case "key.keyboard.k": return LwjglGlfwKeycode.GLFW_KEY_K;
            case "key.keyboard.l": return LwjglGlfwKeycode.GLFW_KEY_L;
            case "key.keyboard.m": return LwjglGlfwKeycode.GLFW_KEY_M;
            case "key.keyboard.n": return LwjglGlfwKeycode.GLFW_KEY_N;
            case "key.keyboard.o": return LwjglGlfwKeycode.GLFW_KEY_O;
            case "key.keyboard.p": return LwjglGlfwKeycode.GLFW_KEY_P;
            case "key.keyboard.q": return LwjglGlfwKeycode.GLFW_KEY_Q;
            case "key.keyboard.r": return LwjglGlfwKeycode.GLFW_KEY_R;
            case "key.keyboard.s": return LwjglGlfwKeycode.GLFW_KEY_S;
            case "key.keyboard.t": return LwjglGlfwKeycode.GLFW_KEY_T;
            case "key.keyboard.u": return LwjglGlfwKeycode.GLFW_KEY_U;
            case "key.keyboard.v": return LwjglGlfwKeycode.GLFW_KEY_V;
            case "key.keyboard.w": return LwjglGlfwKeycode.GLFW_KEY_W;
            case "key.keyboard.x": return LwjglGlfwKeycode.GLFW_KEY_X;
            case "key.keyboard.y": return LwjglGlfwKeycode.GLFW_KEY_Y;
            case "key.keyboard.z": return LwjglGlfwKeycode.GLFW_KEY_Z;
            case "key.keyboard.f1": return LwjglGlfwKeycode.GLFW_KEY_F1;
            case "key.keyboard.f2": return LwjglGlfwKeycode.GLFW_KEY_F2;
            case "key.keyboard.f3": return LwjglGlfwKeycode.GLFW_KEY_F3;
            case "key.keyboard.f4": return LwjglGlfwKeycode.GLFW_KEY_F4;
            case "key.keyboard.f5": return LwjglGlfwKeycode.GLFW_KEY_F5;
            case "key.keyboard.f6": return LwjglGlfwKeycode.GLFW_KEY_F6;
            case "key.keyboard.f7": return LwjglGlfwKeycode.GLFW_KEY_F7;
            case "key.keyboard.f8": return LwjglGlfwKeycode.GLFW_KEY_F8;
            case "key.keyboard.f9": return LwjglGlfwKeycode.GLFW_KEY_F9;
            case "key.keyboard.f10": return LwjglGlfwKeycode.GLFW_KEY_F10;
            case "key.keyboard.f11": return LwjglGlfwKeycode.GLFW_KEY_F11;
            case "key.keyboard.f12": return LwjglGlfwKeycode.GLFW_KEY_F12;
            case "key.keyboard.f13": return LwjglGlfwKeycode.GLFW_KEY_F13;
            case "key.keyboard.f14": return LwjglGlfwKeycode.GLFW_KEY_F14;
            case "key.keyboard.f15": return LwjglGlfwKeycode.GLFW_KEY_F15;
            case "key.keyboard.f16": return LwjglGlfwKeycode.GLFW_KEY_F16;
            case "key.keyboard.f17": return LwjglGlfwKeycode.GLFW_KEY_F17;
            case "key.keyboard.f18": return LwjglGlfwKeycode.GLFW_KEY_F18;
            case "key.keyboard.f19": return LwjglGlfwKeycode.GLFW_KEY_F19;
            case "key.keyboard.f20": return LwjglGlfwKeycode.GLFW_KEY_F20;
            case "key.keyboard.f21": return LwjglGlfwKeycode.GLFW_KEY_F21;
            case "key.keyboard.f22": return LwjglGlfwKeycode.GLFW_KEY_F22;
            case "key.keyboard.f23": return LwjglGlfwKeycode.GLFW_KEY_F23;
            case "key.keyboard.f24": return LwjglGlfwKeycode.GLFW_KEY_F24;
            case "key.keyboard.f25": return LwjglGlfwKeycode.GLFW_KEY_F25;
            case "key.keyboard.num.lock": return LwjglGlfwKeycode.GLFW_KEY_NUM_LOCK;
            case "key.keyboard.keypad.0": return LwjglGlfwKeycode.GLFW_KEY_KP_0;
            case "key.keyboard.keypad.1": return LwjglGlfwKeycode.GLFW_KEY_KP_1;
            case "key.keyboard.keypad.2": return LwjglGlfwKeycode.GLFW_KEY_KP_2;
            case "key.keyboard.keypad.3": return LwjglGlfwKeycode.GLFW_KEY_KP_3;
            case "key.keyboard.keypad.4": return LwjglGlfwKeycode.GLFW_KEY_KP_4;
            case "key.keyboard.keypad.5": return LwjglGlfwKeycode.GLFW_KEY_KP_5;
            case "key.keyboard.keypad.6": return LwjglGlfwKeycode.GLFW_KEY_KP_6;
            case "key.keyboard.keypad.7": return LwjglGlfwKeycode.GLFW_KEY_KP_7;
            case "key.keyboard.keypad.8": return LwjglGlfwKeycode.GLFW_KEY_KP_8;
            case "key.keyboard.keypad.9": return LwjglGlfwKeycode.GLFW_KEY_KP_9;
            case "key.keyboard.keypad.add": return LwjglGlfwKeycode.GLFW_KEY_KP_ADD;
            case "key.keyboard.keypad.decimal": return LwjglGlfwKeycode.GLFW_KEY_KP_DECIMAL;
            case "key.keyboard.keypad.enter": return LwjglGlfwKeycode.GLFW_KEY_KP_ENTER;
            case "key.keyboard.keypad.equal": return LwjglGlfwKeycode.GLFW_KEY_KP_EQUAL;
            case "key.keyboard.keypad.multiply": return LwjglGlfwKeycode.GLFW_KEY_KP_MULTIPLY;
            case "key.keyboard.keypad.divide": return LwjglGlfwKeycode.GLFW_KEY_KP_DIVIDE;
            case "key.keyboard.keypad.subtract": return LwjglGlfwKeycode.GLFW_KEY_KP_SUBTRACT;
            case "key.keyboard.down": return LwjglGlfwKeycode.GLFW_KEY_DOWN;
            case "key.keyboard.left": return LwjglGlfwKeycode.GLFW_KEY_LEFT;
            case "key.keyboard.right": return LwjglGlfwKeycode.GLFW_KEY_RIGHT;
            case "key.keyboard.up": return LwjglGlfwKeycode.GLFW_KEY_UP;
            case "key.keyboard.apostrophe": return LwjglGlfwKeycode.GLFW_KEY_APOSTROPHE;
            case "key.keyboard.backslash": return LwjglGlfwKeycode.GLFW_KEY_BACKSLASH;
            case "key.keyboard.comma": return LwjglGlfwKeycode.GLFW_KEY_COMMA;
            case "key.keyboard.equal": return LwjglGlfwKeycode.GLFW_KEY_EQUAL;
            case "key.keyboard.grave.accent": return LwjglGlfwKeycode.GLFW_KEY_GRAVE_ACCENT;
            case "key.keyboard.left.bracket": return LwjglGlfwKeycode.GLFW_KEY_LEFT_BRACKET;
            case "key.keyboard.minus": return LwjglGlfwKeycode.GLFW_KEY_MINUS;
            case "key.keyboard.period": return LwjglGlfwKeycode.GLFW_KEY_PERIOD;
            case "key.keyboard.right.bracket": return LwjglGlfwKeycode.GLFW_KEY_RIGHT_BRACKET;
            case "key.keyboard.semicolon": return LwjglGlfwKeycode.GLFW_KEY_SEMICOLON;
            case "key.keyboard.slash": return LwjglGlfwKeycode.GLFW_KEY_SLASH;
            case "key.keyboard.space": return LwjglGlfwKeycode.GLFW_KEY_SPACE;
            case "key.keyboard.tab": return LwjglGlfwKeycode.GLFW_KEY_TAB;
            case "key.keyboard.left.alt": return LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT;
            case "key.keyboard.left.control": return LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL;
            case "key.keyboard.left.shift": return LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT;
            case "key.keyboard.left.win": return LwjglGlfwKeycode.GLFW_KEY_LEFT_SUPER;
            case "key.keyboard.right.alt": return LwjglGlfwKeycode.GLFW_KEY_RIGHT_ALT;
            case "key.keyboard.right.control": return LwjglGlfwKeycode.GLFW_KEY_RIGHT_CONTROL;
            case "key.keyboard.right.shift": return LwjglGlfwKeycode.GLFW_KEY_RIGHT_SHIFT;
            case "key.keyboard.right.win": return LwjglGlfwKeycode.GLFW_KEY_RIGHT_SUPER;
            case "key.keyboard.enter": return LwjglGlfwKeycode.GLFW_KEY_ENTER;
            case "key.keyboard.escape": return LwjglGlfwKeycode.GLFW_KEY_ESCAPE;
            case "key.keyboard.backspace": return LwjglGlfwKeycode.GLFW_KEY_BACKSPACE;
            case "key.keyboard.delete": return LwjglGlfwKeycode.GLFW_KEY_DELETE;
            case "key.keyboard.end": return LwjglGlfwKeycode.GLFW_KEY_END;
            case "key.keyboard.home": return LwjglGlfwKeycode.GLFW_KEY_HOME;
            case "key.keyboard.insert": return LwjglGlfwKeycode.GLFW_KEY_INSERT;
            case "key.keyboard.page.down": return LwjglGlfwKeycode.GLFW_KEY_PAGE_DOWN;
            case "key.keyboard.page.up": return LwjglGlfwKeycode.GLFW_KEY_PAGE_UP;
            case "key.keyboard.caps.lock": return LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK;
            case "key.keyboard.pause": return LwjglGlfwKeycode.GLFW_KEY_PAUSE;
            case "key.keyboard.scroll.lock": return LwjglGlfwKeycode.GLFW_KEY_SCROLL_LOCK;
            case "key.keyboard.menu": return LwjglGlfwKeycode.GLFW_KEY_MENU;
            case "key.keyboard.print.screen": return LwjglGlfwKeycode.GLFW_KEY_PRINT_SCREEN;
            case "key.keyboard.world.1": return LwjglGlfwKeycode.GLFW_KEY_WORLD_1;
            case "key.keyboard.world.2": return LwjglGlfwKeycode.GLFW_KEY_WORLD_2;
            default: return null;
        }
    }

    /**
     * Minecraft 绑定按键映射表
     *
     * @param keybinding 绑定的按键
     * @return 对应的控制事件标识
     */
    public static @Nullable String getControlEvent(String keybinding) {
        switch (keybinding) {
            case "key.keyboard.unknown": return ControlEventKeycode.GLFW_KEY_UNKNOWN;
            case "key.mouse.left": return ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT;
            case "key.mouse.right": return ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT;
            case "key.mouse.middle": return ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE;
            case "key.mouse.4": return ControlEventKeycode.GLFW_MOUSE_BUTTON_4;
            case "key.mouse.5": return ControlEventKeycode.GLFW_MOUSE_BUTTON_5;
            case "key.mouse.6": return ControlEventKeycode.GLFW_MOUSE_BUTTON_6;
            case "key.mouse.7": return ControlEventKeycode.GLFW_MOUSE_BUTTON_7;
            case "key.mouse.8": return ControlEventKeycode.GLFW_MOUSE_BUTTON_8;
            case "key.keyboard.0": return ControlEventKeycode.GLFW_KEY_0;
            case "key.keyboard.1": return ControlEventKeycode.GLFW_KEY_1;
            case "key.keyboard.2": return ControlEventKeycode.GLFW_KEY_2;
            case "key.keyboard.3": return ControlEventKeycode.GLFW_KEY_3;
            case "key.keyboard.4": return ControlEventKeycode.GLFW_KEY_4;
            case "key.keyboard.5": return ControlEventKeycode.GLFW_KEY_5;
            case "key.keyboard.6": return ControlEventKeycode.GLFW_KEY_6;
            case "key.keyboard.7": return ControlEventKeycode.GLFW_KEY_7;
            case "key.keyboard.8": return ControlEventKeycode.GLFW_KEY_8;
            case "key.keyboard.9": return ControlEventKeycode.GLFW_KEY_9;
            case "key.keyboard.a": return ControlEventKeycode.GLFW_KEY_A;
            case "key.keyboard.b": return ControlEventKeycode.GLFW_KEY_B;
            case "key.keyboard.c": return ControlEventKeycode.GLFW_KEY_C;
            case "key.keyboard.d": return ControlEventKeycode.GLFW_KEY_D;
            case "key.keyboard.e": return ControlEventKeycode.GLFW_KEY_E;
            case "key.keyboard.f": return ControlEventKeycode.GLFW_KEY_F;
            case "key.keyboard.g": return ControlEventKeycode.GLFW_KEY_G;
            case "key.keyboard.h": return ControlEventKeycode.GLFW_KEY_H;
            case "key.keyboard.i": return ControlEventKeycode.GLFW_KEY_I;
            case "key.keyboard.j": return ControlEventKeycode.GLFW_KEY_J;
            case "key.keyboard.k": return ControlEventKeycode.GLFW_KEY_K;
            case "key.keyboard.l": return ControlEventKeycode.GLFW_KEY_L;
            case "key.keyboard.m": return ControlEventKeycode.GLFW_KEY_M;
            case "key.keyboard.n": return ControlEventKeycode.GLFW_KEY_N;
            case "key.keyboard.o": return ControlEventKeycode.GLFW_KEY_O;
            case "key.keyboard.p": return ControlEventKeycode.GLFW_KEY_P;
            case "key.keyboard.q": return ControlEventKeycode.GLFW_KEY_Q;
            case "key.keyboard.r": return ControlEventKeycode.GLFW_KEY_R;
            case "key.keyboard.s": return ControlEventKeycode.GLFW_KEY_S;
            case "key.keyboard.t": return ControlEventKeycode.GLFW_KEY_T;
            case "key.keyboard.u": return ControlEventKeycode.GLFW_KEY_U;
            case "key.keyboard.v": return ControlEventKeycode.GLFW_KEY_V;
            case "key.keyboard.w": return ControlEventKeycode.GLFW_KEY_W;
            case "key.keyboard.x": return ControlEventKeycode.GLFW_KEY_X;
            case "key.keyboard.y": return ControlEventKeycode.GLFW_KEY_Y;
            case "key.keyboard.z": return ControlEventKeycode.GLFW_KEY_Z;
            case "key.keyboard.f1": return ControlEventKeycode.GLFW_KEY_F1;
            case "key.keyboard.f2": return ControlEventKeycode.GLFW_KEY_F2;
            case "key.keyboard.f3": return ControlEventKeycode.GLFW_KEY_F3;
            case "key.keyboard.f4": return ControlEventKeycode.GLFW_KEY_F4;
            case "key.keyboard.f5": return ControlEventKeycode.GLFW_KEY_F5;
            case "key.keyboard.f6": return ControlEventKeycode.GLFW_KEY_F6;
            case "key.keyboard.f7": return ControlEventKeycode.GLFW_KEY_F7;
            case "key.keyboard.f8": return ControlEventKeycode.GLFW_KEY_F8;
            case "key.keyboard.f9": return ControlEventKeycode.GLFW_KEY_F9;
            case "key.keyboard.f10": return ControlEventKeycode.GLFW_KEY_F10;
            case "key.keyboard.f11": return ControlEventKeycode.GLFW_KEY_F11;
            case "key.keyboard.f12": return ControlEventKeycode.GLFW_KEY_F12;
            case "key.keyboard.f13": return ControlEventKeycode.GLFW_KEY_F13;
            case "key.keyboard.f14": return ControlEventKeycode.GLFW_KEY_F14;
            case "key.keyboard.f15": return ControlEventKeycode.GLFW_KEY_F15;
            case "key.keyboard.f16": return ControlEventKeycode.GLFW_KEY_F16;
            case "key.keyboard.f17": return ControlEventKeycode.GLFW_KEY_F17;
            case "key.keyboard.f18": return ControlEventKeycode.GLFW_KEY_F18;
            case "key.keyboard.f19": return ControlEventKeycode.GLFW_KEY_F19;
            case "key.keyboard.f20": return ControlEventKeycode.GLFW_KEY_F20;
            case "key.keyboard.f21": return ControlEventKeycode.GLFW_KEY_F21;
            case "key.keyboard.f22": return ControlEventKeycode.GLFW_KEY_F22;
            case "key.keyboard.f23": return ControlEventKeycode.GLFW_KEY_F23;
            case "key.keyboard.f24": return ControlEventKeycode.GLFW_KEY_F24;
            case "key.keyboard.f25": return ControlEventKeycode.GLFW_KEY_F25;
            case "key.keyboard.num.lock": return ControlEventKeycode.GLFW_KEY_NUM_LOCK;
            case "key.keyboard.keypad.0": return ControlEventKeycode.GLFW_KEY_KP_0;
            case "key.keyboard.keypad.1": return ControlEventKeycode.GLFW_KEY_KP_1;
            case "key.keyboard.keypad.2": return ControlEventKeycode.GLFW_KEY_KP_2;
            case "key.keyboard.keypad.3": return ControlEventKeycode.GLFW_KEY_KP_3;
            case "key.keyboard.keypad.4": return ControlEventKeycode.GLFW_KEY_KP_4;
            case "key.keyboard.keypad.5": return ControlEventKeycode.GLFW_KEY_KP_5;
            case "key.keyboard.keypad.6": return ControlEventKeycode.GLFW_KEY_KP_6;
            case "key.keyboard.keypad.7": return ControlEventKeycode.GLFW_KEY_KP_7;
            case "key.keyboard.keypad.8": return ControlEventKeycode.GLFW_KEY_KP_8;
            case "key.keyboard.keypad.9": return ControlEventKeycode.GLFW_KEY_KP_9;
            case "key.keyboard.keypad.add": return ControlEventKeycode.GLFW_KEY_KP_ADD;
            case "key.keyboard.keypad.decimal": return ControlEventKeycode.GLFW_KEY_KP_DECIMAL;
            case "key.keyboard.keypad.enter": return ControlEventKeycode.GLFW_KEY_KP_ENTER;
            case "key.keyboard.keypad.equal": return ControlEventKeycode.GLFW_KEY_KP_EQUAL;
            case "key.keyboard.keypad.multiply": return ControlEventKeycode.GLFW_KEY_KP_MULTIPLY;
            case "key.keyboard.keypad.divide": return ControlEventKeycode.GLFW_KEY_KP_DIVIDE;
            case "key.keyboard.keypad.subtract": return ControlEventKeycode.GLFW_KEY_KP_SUBTRACT;
            case "key.keyboard.down": return ControlEventKeycode.GLFW_KEY_DOWN;
            case "key.keyboard.left": return ControlEventKeycode.GLFW_KEY_LEFT;
            case "key.keyboard.right": return ControlEventKeycode.GLFW_KEY_RIGHT;
            case "key.keyboard.up": return ControlEventKeycode.GLFW_KEY_UP;
            case "key.keyboard.apostrophe": return ControlEventKeycode.GLFW_KEY_APOSTROPHE;
            case "key.keyboard.backslash": return ControlEventKeycode.GLFW_KEY_BACKSLASH;
            case "key.keyboard.comma": return ControlEventKeycode.GLFW_KEY_COMMA;
            case "key.keyboard.equal": return ControlEventKeycode.GLFW_KEY_EQUAL;
            case "key.keyboard.grave.accent": return ControlEventKeycode.GLFW_KEY_GRAVE_ACCENT;
            case "key.keyboard.left.bracket": return ControlEventKeycode.GLFW_KEY_LEFT_BRACKET;
            case "key.keyboard.minus": return ControlEventKeycode.GLFW_KEY_MINUS;
            case "key.keyboard.period": return ControlEventKeycode.GLFW_KEY_PERIOD;
            case "key.keyboard.right.bracket": return ControlEventKeycode.GLFW_KEY_RIGHT_BRACKET;
            case "key.keyboard.semicolon": return ControlEventKeycode.GLFW_KEY_SEMICOLON;
            case "key.keyboard.slash": return ControlEventKeycode.GLFW_KEY_SLASH;
            case "key.keyboard.space": return ControlEventKeycode.GLFW_KEY_SPACE;
            case "key.keyboard.tab": return ControlEventKeycode.GLFW_KEY_TAB;
            case "key.keyboard.left.alt": return ControlEventKeycode.GLFW_KEY_LEFT_ALT;
            case "key.keyboard.left.control": return ControlEventKeycode.GLFW_KEY_LEFT_CONTROL;
            case "key.keyboard.left.shift": return ControlEventKeycode.GLFW_KEY_LEFT_SHIFT;
            case "key.keyboard.left.win": return ControlEventKeycode.GLFW_KEY_LEFT_SUPER;
            case "key.keyboard.right.alt": return ControlEventKeycode.GLFW_KEY_RIGHT_ALT;
            case "key.keyboard.right.control": return ControlEventKeycode.GLFW_KEY_RIGHT_CONTROL;
            case "key.keyboard.right.shift": return ControlEventKeycode.GLFW_KEY_RIGHT_SHIFT;
            case "key.keyboard.right.win": return ControlEventKeycode.GLFW_KEY_RIGHT_SUPER;
            case "key.keyboard.enter": return ControlEventKeycode.GLFW_KEY_ENTER;
            case "key.keyboard.escape": return ControlEventKeycode.GLFW_KEY_ESCAPE;
            case "key.keyboard.backspace": return ControlEventKeycode.GLFW_KEY_BACKSPACE;
            case "key.keyboard.delete": return ControlEventKeycode.GLFW_KEY_DELETE;
            case "key.keyboard.end": return ControlEventKeycode.GLFW_KEY_END;
            case "key.keyboard.home": return ControlEventKeycode.GLFW_KEY_HOME;
            case "key.keyboard.insert": return ControlEventKeycode.GLFW_KEY_INSERT;
            case "key.keyboard.page.down": return ControlEventKeycode.GLFW_KEY_PAGE_DOWN;
            case "key.keyboard.page.up": return ControlEventKeycode.GLFW_KEY_PAGE_UP;
            case "key.keyboard.caps.lock": return ControlEventKeycode.GLFW_KEY_CAPS_LOCK;
            case "key.keyboard.pause": return ControlEventKeycode.GLFW_KEY_PAUSE;
            case "key.keyboard.scroll.lock": return ControlEventKeycode.GLFW_KEY_SCROLL_LOCK;
            case "key.keyboard.menu": return ControlEventKeycode.GLFW_KEY_MENU;
            case "key.keyboard.print.screen": return ControlEventKeycode.GLFW_KEY_PRINT_SCREEN;
            case "key.keyboard.world.1": return ControlEventKeycode.GLFW_KEY_WORLD_1;
            case "key.keyboard.world.2": return ControlEventKeycode.GLFW_KEY_WORLD_2;
            default: return null;
        }
    }
}
