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

package com.movtery.zalithlauncher.ui.screens.game.elements.log_parser

import androidx.compose.ui.graphics.Color

/**
 * 日志等级识别规则
 * @param identifiers 所有可识别的标识符
 * @param color 文本颜色
 * @param backgroundColor 背景颜色，可不设置
 */
data class LogLevelRule(
    val identifiers: List<String>,
    val textColor: Color,
    val backgroundColor: Color? = null
)

val INFO = LogLevelRule(
    identifiers = listOf("INFO", "Info"),
    textColor = Color.White,
    backgroundColor = Color(0xFF447152)
)

val ERROR = LogLevelRule(
    identifiers = listOf("ERROR", "Error"),
    textColor = Color(0xFF6AAB73),
    backgroundColor = null
)

val DEBUG = LogLevelRule(
    identifiers = listOf("DEBUG", "Debug"),
    textColor = Color.White,
    backgroundColor = Color(0xFF43698D)
)

val WARN = LogLevelRule(
    identifiers = listOf("WARN", "Warn"),
    textColor = Color.White,
    backgroundColor = Color(0xFF656E76)
)