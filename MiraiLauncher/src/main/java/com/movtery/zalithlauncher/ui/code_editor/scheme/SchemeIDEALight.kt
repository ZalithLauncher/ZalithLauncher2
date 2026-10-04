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

class SchemeIDEALight: EditorColorScheme(false) {
    override fun applyDefault() {
        super.applyDefault()
        setColor(WHOLE_BACKGROUND, 0xFFFFFFFF.toInt())
        setColor(TEXT_NORMAL, 0xFF000000.toInt())
        setColor(CURRENT_LINE, 0xFFF5F8FE.toInt())
        setColor(SELECTED_TEXT_BACKGROUND, 0xFFA6D2FF.toInt())
        setColor(SELECTION_INSERT, 0xFF000000.toInt())
        setColor(LINE_NUMBER_BACKGROUND, 0xFFFFFFFF.toInt())
        setColor(LINE_NUMBER, 0xFFAEB3C2.toInt())
        setColor(LINE_NUMBER_CURRENT, 0xFF767A8A.toInt())
        setColor(LINE_DIVIDER, 0xFFDFE1E5.toInt())
        setColor(BLOCK_LINE, 0xFFEBECF0.toInt())
        setColor(NON_PRINTABLE_CHAR, 0xFF767A8A.toInt())
        setColor(SCROLL_BAR_THUMB, 0xFFA6A6A6.toInt())
        setColor(SCROLL_BAR_THUMB_PRESSED, 0xFF565656.toInt())
        setColor(COMPLETION_WND_BACKGROUND, 0xFFFFFFFF.toInt())

        //语法高亮
        setColor(KEYWORD, 0xFF000033.toInt())
        setColor(COMMENT, 0xFF8C8C8C.toInt())
        setColor(LITERAL, 0xFF006700.toInt())
        setColor(OPERATOR, 0xFF000000.toInt())
        setColor(IDENTIFIER_NAME, 0xFF000000.toInt())
        setColor(IDENTIFIER_VAR, 0xFF871094.toInt())
        setColor(FUNCTION_NAME, 0xFF0066AA.toInt())
        setColor(ANNOTATION, 0xFF9E880D.toInt())
        setColor(HTML_TAG, 0xFF008080.toInt())
        setColor(ATTRIBUTE_NAME, 0xFF174AD4.toInt())
        setColor(ATTRIBUTE_VALUE, 0xFF006700.toInt())

        //问题诊断
        setColor(PROBLEM_ERROR, 0xFFFF6666.toInt())
        setColor(PROBLEM_WARNING, 0xFFF2BF57.toInt())
        setColor(PROBLEM_TYPO, 0xFF7EC482.toInt())

        //其他
        setColor(MATCHED_TEXT_BACKGROUND, 0xFFFCD47E.toInt())
        setColor(HIGHLIGHTED_DELIMITERS_BACKGROUND, 0xFF93D9D9.toInt())
    }
}