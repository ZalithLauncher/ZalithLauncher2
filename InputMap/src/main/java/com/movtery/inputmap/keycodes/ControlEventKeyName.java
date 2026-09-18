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

import static com.movtery.inputmap.keycodes.ControlEventKeycode.*;

import androidx.annotation.Nullable;

public class ControlEventKeyName {
    /**
     * 利用Java语言switch的性能，快速匹配键对应的显示名称
     */
    public static @Nullable String getNameByKey(String key) {
        return switch (key) {
            case GLFW_KEY_SPACE -> "Space";
            case GLFW_KEY_APOSTROPHE -> "'";
            case GLFW_KEY_COMMA -> ",";
            case GLFW_KEY_MINUS -> "-";
            case GLFW_KEY_PERIOD -> ".";
            case GLFW_KEY_SLASH -> "/";
            case GLFW_KEY_0 -> "0";
            case GLFW_KEY_1 -> "1";
            case GLFW_KEY_2 -> "2";
            case GLFW_KEY_3 -> "3";
            case GLFW_KEY_4 -> "4";
            case GLFW_KEY_5 -> "5";
            case GLFW_KEY_6 -> "6";
            case GLFW_KEY_7 -> "7";
            case GLFW_KEY_8 -> "8";
            case GLFW_KEY_9 -> "9";
            case GLFW_KEY_SEMICOLON -> ";";
            case GLFW_KEY_EQUAL -> "+";
            case GLFW_KEY_A -> "A";
            case GLFW_KEY_B -> "B";
            case GLFW_KEY_C -> "C";
            case GLFW_KEY_D -> "D";
            case GLFW_KEY_E -> "E";
            case GLFW_KEY_F -> "F";
            case GLFW_KEY_G -> "G";
            case GLFW_KEY_H -> "H";
            case GLFW_KEY_I -> "I";
            case GLFW_KEY_J -> "J";
            case GLFW_KEY_K -> "K";
            case GLFW_KEY_L -> "L";
            case GLFW_KEY_M -> "M";
            case GLFW_KEY_N -> "N";
            case GLFW_KEY_O -> "O";
            case GLFW_KEY_P -> "P";
            case GLFW_KEY_Q -> "Q";
            case GLFW_KEY_R -> "R";
            case GLFW_KEY_S -> "S";
            case GLFW_KEY_T -> "T";
            case GLFW_KEY_U -> "U";
            case GLFW_KEY_V -> "V";
            case GLFW_KEY_W -> "W";
            case GLFW_KEY_X -> "X";
            case GLFW_KEY_Y -> "Y";
            case GLFW_KEY_Z -> "Z";
            case GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW_KEY_BACKSLASH -> "\\";
            case GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW_KEY_ESCAPE -> "Esc";
            case GLFW_KEY_ENTER -> "Enter";
            case GLFW_KEY_TAB -> "Tab";
            case GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW_KEY_INSERT -> "Insert";
            case GLFW_KEY_DELETE -> "Delete";
            case GLFW_KEY_RIGHT -> "→";
            case GLFW_KEY_LEFT -> "←";
            case GLFW_KEY_DOWN -> "↓";
            case GLFW_KEY_UP -> "↑";
            case GLFW_KEY_PAGE_UP -> "Page Up";
            case GLFW_KEY_PAGE_DOWN -> "Page Down";
            case GLFW_KEY_HOME -> "Home";
            case GLFW_KEY_END -> "End";
            case GLFW_KEY_CAPS_LOCK -> "Caps Lock";
            case GLFW_KEY_SCROLL_LOCK -> "Scroll Lock";
            case GLFW_KEY_NUM_LOCK -> "Num lock";
            case GLFW_KEY_PRINT_SCREEN -> "Print Screen";
            case GLFW_KEY_PAUSE -> "Pause";
            case GLFW_KEY_F1 -> "F1";
            case GLFW_KEY_F2 -> "F2";
            case GLFW_KEY_F3 -> "F3";
            case GLFW_KEY_F4 -> "F4";
            case GLFW_KEY_F5 -> "F5";
            case GLFW_KEY_F6 -> "F6";
            case GLFW_KEY_F7 -> "F7";
            case GLFW_KEY_F8 -> "F8";
            case GLFW_KEY_F9 -> "F9";
            case GLFW_KEY_F10 -> "F10";
            case GLFW_KEY_F11 -> "F11";
            case GLFW_KEY_F12 -> "F12";
            case GLFW_KEY_F13 -> "F13";
            case GLFW_KEY_F14 -> "F14";
            case GLFW_KEY_F15 -> "F15";
            case GLFW_KEY_F16 -> "F16";
            case GLFW_KEY_F17 -> "F17";
            case GLFW_KEY_F18 -> "F18";
            case GLFW_KEY_F19 -> "F19";
            case GLFW_KEY_F20 -> "F20";
            case GLFW_KEY_F21 -> "F21";
            case GLFW_KEY_F22 -> "F22";
            case GLFW_KEY_F23 -> "F23";
            case GLFW_KEY_F24 -> "F24";
            case GLFW_KEY_F25 -> "F25";
            case GLFW_KEY_KP_0 -> "Num 0";
            case GLFW_KEY_KP_1 -> "Num 1";
            case GLFW_KEY_KP_2 -> "Num 2";
            case GLFW_KEY_KP_3 -> "Num 3";
            case GLFW_KEY_KP_4 -> "Num 4";
            case GLFW_KEY_KP_5 -> "Num 5";
            case GLFW_KEY_KP_6 -> "Num 6";
            case GLFW_KEY_KP_7 -> "Num 7";
            case GLFW_KEY_KP_8 -> "Num 8";
            case GLFW_KEY_KP_9 -> "Num 9";
            case GLFW_KEY_KP_DECIMAL -> "Num .";
            case GLFW_KEY_KP_DIVIDE -> "Num /";
            case GLFW_KEY_KP_MULTIPLY -> "Num *";
            case GLFW_KEY_KP_SUBTRACT -> "Num -";
            case GLFW_KEY_KP_ADD -> "Num +";
            case GLFW_KEY_KP_ENTER -> "Num Enter";
            case GLFW_KEY_LEFT_SHIFT -> "Left Shift";
            case GLFW_KEY_LEFT_CONTROL -> "Left Control";
            case GLFW_KEY_LEFT_ALT -> "Left Alt";
            case GLFW_KEY_RIGHT_SHIFT -> "Right Shift";
            case GLFW_KEY_RIGHT_CONTROL -> "Right Ctrl";
            case GLFW_KEY_RIGHT_ALT -> "Right Alt";
            default -> null;
        };
    }
}
