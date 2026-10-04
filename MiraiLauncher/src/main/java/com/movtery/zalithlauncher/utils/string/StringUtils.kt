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

package com.movtery.zalithlauncher.utils.string

import android.util.Base64
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.regex.Pattern

fun shiftString(input: String, direction: ShiftDirection, shiftCount: Int): String {
    if (input.isEmpty()) {
        return input
    }

    //确保位移个数在字符串长度范围内
    val length = input.length
    val shiftCount1 = shiftCount % length
    if (shiftCount1 == 0) {
        return input
    }

    return when (direction) {
        ShiftDirection.LEFT -> input.substring(shiftCount1) + input.substring(0, shiftCount1)
        ShiftDirection.RIGHT -> input.substring(length - shiftCount1) + input.substring(0, length - shiftCount1)
    }
}

fun throwableToString(throwable: Throwable): String {
    val stringWriter = StringWriter()
    PrintWriter(stringWriter).use {
        throwable.printStackTrace(it)
    }
    return stringWriter.toString()
}

fun Throwable.getMessageOrToString(): String {
    return message ?: throwableToString(this)
}

fun decodeBase64(rawValue: String): String {
    val decodedBytes = Base64.decode(rawValue, Base64.DEFAULT)
    return String(decodedBytes, StandardCharsets.UTF_8)
}

fun decodeUnicode(input: String): String {
    val regex = """\\u([0-9a-fA-F]{4})""".toRegex()
    var result = input
    regex.findAll(input).forEach { match ->
        val unicode = match.groupValues[1]
        val char = Character.toChars(unicode.toInt(16))[0]
        result = result.replace(match.value, char.toString())
    }
    return result
}

fun String.toUuid(charset: Charset = Charsets.UTF_8): UUID {
    return UUID.nameUUIDFromBytes(this.toByteArray(charset))
}

fun String.toUuidStr(charset: Charset = Charsets.UTF_8): String {
    return toUuid(charset).toString()
}

/**
 * @return 检查字符串是否为null，如果是那么则返回""，如果不是，则返回字符串本身
 */
fun getStringNotNull(string: String?): String = string ?: ""

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/84aca2e/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L1032-L1039)
 */
fun String.extractUntilCharacter(whatFor: String, terminator: Char): String? {
    var whatForStart = indexOf(whatFor)
    if (whatForStart == -1) return null
    whatForStart += whatFor.length
    val terminatorIndex = indexOf(terminator, whatForStart)
    if (terminatorIndex == -1) return null
    return substring(whatForStart, terminatorIndex)
}

/**
 * 获取字符串指定行的内容
 */
fun String.getLine(line: Int): String? {
    val lines = this.trimIndent().split("\n")
    return if (line in 1..lines.size) lines[line - 1] else null
}

fun String.toSingleLine(replace: String = " "): String = this.replace("\n", replace)

fun insertJSONValueList(args: Array<String>, keyValueMap: Map<String, String>) =
    args.map {
        keyValueMap.entries.fold(it) { acc, (k, v) ->
            acc.replace("\${$k}", v)
        }
    }.toTypedArray()

fun String.splitPreservingQuotes(delimiter: Char = ' '): List<String> {
    val result = mutableListOf<String>()
    val currentPart = StringBuilder()
    var inQuotes = false

    for ((index, c) in withIndex()) {
        when {
            c == '"' && (index == 0 || this[index - 1] != '\\') -> {
                // 切换引号状态（忽略转义引号）
                inQuotes = !inQuotes
            }
            (if (delimiter == ' ') c.isWhitespace() else c == delimiter) && !inQuotes -> {
                // 如果不在引号内且遇到分隔符（默认是任意空白字符），则结束当前部分并添加到结果中
                if (currentPart.isNotEmpty()) {
                    result.add(currentPart.toString())
                    currentPart.clear() // 清空当前部分
                }
            }
            else -> {
                // 将字符添加到当前部分
                currentPart.append(c)
            }
        }
    }

    // 添加最后一部分（如果有的话）
    if (currentPart.isNotEmpty()) {
        result.add(currentPart.toString())
    }

    return result
}

fun String.isSurrounded(prefix: String, suffix: String): Boolean = this.startsWith(prefix) && this.endsWith(suffix)

fun String.toFullUnicode(): String {
    return this.map { "\\u%04x".format(it.code) }.joinToString("")
}

fun String.toUnicodeEscaped(): String {
    return this.flatMap { ch ->
        if (ch.code > 127) {
            val hex = ch.code.toString(16).padStart(4, '0')
            listOf("\\u$hex")
        } else {
            listOf(ch.toString())
        }
    }.joinToString("")
}

/**
 * 过滤掉颜色占位符
 */
fun String.stripColorCodes(): String {
    return replace(Regex("§[0-9a-fk-orA-FK-OR]"), "")
}

fun String.isEmptyOrBlank(): Boolean = this.isEmpty() || this.isBlank()

fun String.isNotEmptyOrBlank(): Boolean = !this.isEmptyOrBlank()

/**
 * 检查一段字符串内是否含有中文字符（中文标点）
 * @return 是否带有中文
 */
fun String?.containsChinese(): Boolean {
    if (this == null || this.isEmpty()) {
        return false
    }

    val pattern = Pattern.compile("[一-龥|！，。（）《》“”？：；【】]")
    val matcher = pattern.matcher(this)
    return matcher.find()
}

/**
 * 修改自源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/942f7b7/HMCLCore/src/main/java/org/jackhuang/hmcl/util/StringUtils.java#L291-L393)
 * 原项目版权归原作者所有，遵循GPL v3协议
 */
fun tokenize(str: String, vars: Map<String, String>? = null): List<String> {
    if (str.isBlank()) return emptyList()
    val variables = vars ?: emptyMap()
    val tokens = mutableListOf<String>()
    val current = StringBuilder()
    var i = 0

    while (i < str.length) {
        when (val c = str[i]) {
            '\'' -> {
                val end = str.indexOf('\'', startIndex = i + 1).takeIf { it >= 0 } ?: str.length
                current.append(str, i + 1, end)
                i = end + 1
            }

            '"' -> {
                i++
                while (i < str.length) {
                    when (val ch = str[i++]) {
                        '"' -> break
                        '`' -> if (i < str.length) {
                            current.append(
                                when (val esc = str[i++]) {
                                    'a' -> '\u0007'
                                    'b' -> '\b'
                                    'f' -> '\u000C'
                                    'n' -> '\n'
                                    'r' -> '\r'
                                    't' -> '\t'
                                    'v' -> '\u000B'
                                    else -> esc
                                }
                            )
                        }
                        '$' -> handleVariable(str, i, variables, current).also { i = it }
                        else -> current.append(ch)
                    }
                }
            }

            '$' -> {
                i = handleVariable(str, i + 1, variables, current)
            }

            ' ' -> {
                if (current.isNotEmpty()) {
                    tokens.add(current.toString())
                    current.clear()
                }
                i++
            }

            else -> {
                current.append(c)
                i++
            }
        }
    }

    if (current.isNotEmpty()) tokens.add(current.toString())
    return tokens
}

private fun handleVariable(
    str: String,
    start: Int,
    vars: Map<String, String>,
    current: StringBuilder
): Int {
    val varEnd = findVarEnd(str, start)
    if (varEnd < 0) { //无效变量格式
        current.append('$')
        return start
    }

    val varName = str.substring(start, varEnd)
    current.append(vars[varName] ?: "$$varName")
    return varEnd
}

private fun findVarEnd(str: String, start: Int): Int {
    if (start >= str.length) return -1
    if (!str[start].isJavaIdentifierStart()) return -1

    var pos = start + 1
    while (pos < str.length && str[pos].isJavaIdentifierPart()) {
        pos++
    }
    return pos
}

/**
     * 修改自源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/942f7b7/HMCLCore/src/main/java/org/jackhuang/hmcl/util/StringUtils.java#L462-L516)
     * 原项目版权归原作者所有，遵循GPL v3协议
     */
    class LevCalculator {
        private var lev: Array<IntArray> = emptyArray()

        constructor()

        constructor(length1: Int, length2: Int) {
            allocate(length1, length2)
        }

        private fun allocate(length1: Int, length2: Int) {
            val rows = length1 + 1
            val cols = length2 + 1

            lev = Array(rows) { i ->
                IntArray(cols).apply {
                    if (i == 0) {
                        indices.forEach { j -> this[j] = j }
                    } else {
                        this[0] = i
                    }
                }
            }
        }

        val length1: Int
            get() = lev.size

        val length2: Int
            get() = lev[0].size

        private fun min(a: Int, b: Int, c: Int) = minOf(a, b, c)

        fun calc(a: CharSequence, b: CharSequence): Int {
            if (lev.isEmpty() || a.length >= lev.size || b.length >= lev[0].size) {
                allocate(a.length, b.length)
            }

            for (i in 1..a.length) {
                for (j in 1..b.length) {
                    lev[i][j] = min(
                        lev[i][j - 1] + 1,
                        lev[i - 1][j] + 1,
                        if (a[i - 1] == b[j - 1])
                            lev[i - 1][j - 1]
                        else
                            lev[i - 1][j - 1] + 1
                    )
                }
            }

            return lev[a.length][b.length]
        }
    }