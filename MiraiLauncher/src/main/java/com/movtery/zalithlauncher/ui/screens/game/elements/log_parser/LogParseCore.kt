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

/**
 * 日志解析的共用逻辑
 */
object LogParseCore {
    private val timePatterns = listOf(
        Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z"""), // ISO-8601
        Regex("""\d{4}-\d{2}-\d{2} \d{1,2}:\d{2}:\d{2}"""),
        Regex("""\d{1,2}:\d{2}:\d{2}(?:\.\d{1,3})?""")
    )

    private val allLevelRules by lazy {
        listOf(INFO, ERROR, DEBUG, WARN)
    }

    fun matchTime(text: String, start: Int): String? {
        for (r in timePatterns) {
            val m = r.find(text, start)
            if (m != null && m.range.first == start) return m.value
        }
        return null
    }

    fun matchWebLink(text: String, start: Int): String? {
        if (
            !text.startsWith("http://", start) &&
            !text.startsWith("https://", start)
        ) {
            return null
        }

        var i = start
        val n = text.length

        while (i < n) {
            val c = text[i]
            if (
                c.isWhitespace() ||
                c == '"' ||
                c == '\'' ||
                c == '<' ||
                c == '>' ||
                c == '(' ||
                c == ')'
            ) {
                break
            }
            i++
        }

        return if (i > start) {
            text.substring(start, i)
        } else {
            null
        }
    }

    fun findClosingQuote(text: String, start: Int, quote: Char): Int {
        var i = start + 1
        while (i < text.length) {
            when {
                // 防跨行
                text[i] == '\n' -> return -1
                text[i] == quote && text[i - 1] != '\\' -> return i
            }
            i++
        }
        return -1
    }

    fun isLogLevel(text: String, start: Int): Boolean =
        start < text.length && text[start].isLetter() && (start == 0 || isWordBoundary(text[start - 1]))

    fun matchLogLevel(text: String, start: Int): String? {
        for (rule in allLevelRules) {
            for (id in rule.identifiers) {
                if (text.regionMatches(start, id, 0, id.length)) {
                    val end = start + id.length
                    if (end == text.length || isWordBoundary(text[end])) {
                        return id
                    }
                }
            }
        }
        return null
    }

    fun findLevelRule(level: String): LogLevelRule? =
        allLevelRules.firstOrNull {
            it.identifiers.any { id -> id.equals(level, true) }
        }

    fun scanNumericSegment(text: String, start: Int): String? {
        var i = start
        while (i < text.length && text[i].isDigit()) i++
        if (i == start) return null
        return text.substring(start, i)
    }

    fun isIndependentNumber(text: String, start: Int, length: Int): Boolean {
        val end = start + length
        val before = if (start == 0) ' ' else text[start - 1]
        val after = if (end >= text.length) ' ' else text[end]

        return !before.isLetterOrDigit() && !after.isLetterOrDigit()
    }

    fun scanPackageName(text: String, start: Int): String? {
        val n = text.length
        var i = start

        val before = if (start == 0) ' ' else text[start - 1]
        if (before != ' ') return null

        if (i >= n || !text[i].isLetter()) return null

        var lastDotIndex = -1
        var dotCount = 0

        while (i < n) {
            val c = text[i]
            when {
                c.isLetterOrDigit() -> i++
                c == '.' -> {
                    if (lastDotIndex == i - 1) return null
                    dotCount++
                    lastDotIndex = i
                    i++
                    if (i >= n || !text[i].isLetter()) return null
                }
                else -> break
            }
        }

        if (dotCount == 0) return null

        val end = i
        val after = if (end >= n) ' ' else text[end]
        if (after != ' ') return null

        return text.substring(start, end)
    }

    fun isStackTraceStart(text: String, offset: Int = 0): Boolean =
        text.startsWith("Caused by:", offset) ||
        text.startsWith("\tat ", offset) ||
        text.startsWith("    at ", offset) ||
        isExceptionDeclaration(text, offset)

    fun isStackTraceLine(text: String, offset: Int = 0): Boolean =
        text.startsWith("\tat ", offset) ||
        text.startsWith("    at ", offset) ||
        text.startsWith("Caused by:", offset)

    fun isExceptionDeclaration(text: String, start: Int): Boolean {
        var i = start
        while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '.')) i++
        if (i <= start) return false
        val name = text.substring(start, i)
        return name.endsWith("Exception") || name.endsWith("Error")
    }

    fun isWordBoundary(c: Char): Boolean = !(c.isLetterOrDigit() || c == '_')

    fun isLineStart(text: String, i: Int): Boolean =
        i == 0 || text[i - 1] == '\n'
}
