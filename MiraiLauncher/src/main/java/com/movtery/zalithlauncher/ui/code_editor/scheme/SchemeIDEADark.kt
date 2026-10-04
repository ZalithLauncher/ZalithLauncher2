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

package com.movtery.zalithlauncher.ui.code_editor.scheme

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class SchemeIDEADark: EditorColorScheme(true) {
    override fun applyDefault() {
        super.applyDefault()
        setColor(WHOLE_BACKGROUND, 0xFF191A1C.toInt())
        setColor(TEXT_NORMAL, 0xFFBCBEC4.toInt())
        setColor(CURRENT_LINE, 0xFF1F2024.toInt())
        setColor(SELECTED_TEXT_BACKGROUND, 0xFF3676B8.toInt())
        setColor(SELECTION_INSERT, 0xFFCED0D6.toInt())
        setColor(LINE_NUMBER_BACKGROUND, 0xFF292A2E.toInt())
        setColor(LINE_NUMBER, 0xFF4B5059.toInt())
        setColor(LINE_NUMBER_CURRENT, 0xFFA1A3AB.toInt())
        setColor(LINE_DIVIDER, 0xFF43454A.toInt())
        setColor(BLOCK_LINE, 0xFF323438.toInt())
        setColor(NON_PRINTABLE_CHAR, 0xFF6F737A.toInt())
        setColor(SCROLL_BAR_THUMB, 0xFFA6A6A6.toInt())
        setColor(SCROLL_BAR_THUMB_PRESSED, 0xFF565656.toInt())
        setColor(COMPLETION_WND_BACKGROUND, 0xFF27282B.toInt())

        //语法高亮
        setColor(KEYWORD, 0xFFCF8E6D.toInt())
        setColor(COMMENT, 0xFF7A7E85.toInt())
        setColor(LITERAL, 0xFF6AAB73.toInt())
        setColor(OPERATOR, 0xFFBCBEC4.toInt())
        setColor(IDENTIFIER_NAME, 0xFFBCBEC4.toInt())
        setColor(IDENTIFIER_VAR, 0xFFC77DBB.toInt())
        setColor(FUNCTION_NAME, 0xFF56A8F5.toInt())
        setColor(ANNOTATION, 0xFFB3AE60.toInt())
        setColor(HTML_TAG, 0xFFD5B778.toInt())
        setColor(ATTRIBUTE_NAME, 0xFFBCBEC4.toInt())
        setColor(ATTRIBUTE_VALUE, 0xFF6AAB73.toInt())

        //问题诊断
        setColor(PROBLEM_ERROR, 0xFFFA6675.toInt())
        setColor(PROBLEM_WARNING, 0xFFF2C55C.toInt())
        setColor(PROBLEM_TYPO, 0xFF7EC482.toInt())

        //其他
        setColor(MATCHED_TEXT_BACKGROUND, 0xFF2D543F.toInt())
        setColor(HIGHLIGHTED_DELIMITERS_BACKGROUND, 0xFF43454A.toInt())
    }
}