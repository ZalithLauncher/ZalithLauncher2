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

package com.movtery.zalithlauncher.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.os.Build
import android.os.Process
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.logging.Logger
import com.xhinliang.lunarcalendar.LunarCalendar
import java.io.File
import java.io.PrintStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

private const val TAG = "LocalUtils"

val GSON = GsonBuilder().setPrettyPrinting().create()

const val DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"

/**
 * 格式化时间戳
 */
fun formatDate(
    date: Date,
    pattern: String = DEFAULT_DATE_PATTERN,
    locale: Locale = Locale.getDefault(),
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val formatter = try {
        SimpleDateFormat(pattern, locale)
    } catch (e: IllegalArgumentException) {
        Logger.warning(TAG, "Encountered an illegal format string while initializing time string formatting: $pattern", e)
        SimpleDateFormat(DEFAULT_DATE_PATTERN, locale)
    }
    formatter.timeZone = timeZone
    return formatter.format(date)
}

/**
 * 格式化时间戳
 */
fun formatDate(
    timestamp: Long,
    pattern: String = DEFAULT_DATE_PATTERN,
    locale: Locale = Locale.getDefault(),
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    return formatDate(
        date = Date(timestamp),
        pattern = pattern,
        locale = locale,
        timeZone = timeZone
    )
}

/**
 * 格式化时间戳
 */
fun formatDate(
    input: String,
    pattern: String = DEFAULT_DATE_PATTERN,
    locale: Locale = Locale.getDefault(),
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    val formatter = try {
        DateTimeFormatter.ofPattern(pattern)
    } catch (e: IllegalArgumentException) {
        Logger.warning(TAG, "Encountered an illegal format string while initializing time string formatting: $pattern", e)
        DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)
    }
    return formatter
        .withLocale(locale)
        .withZone(zoneId)
        .format(
        OffsetDateTime.parse(input).toZonedDateTime()
    )
}

/**
 * 获取 xx 时间前 格式的字符串
 */
fun getTimeAgo(
    context: Context,
    dateString: String,
    inputFormat: String = "yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'"
): String {
    val formatter = DateTimeFormatter.ofPattern(inputFormat, Locale.getDefault())
        .withZone(ZoneOffset.UTC)

    val pastInstant = try {
        Instant.from(formatter.parse(dateString))
    } catch (_: DateTimeParseException) {
        try {
            Instant.parse(dateString)
        } catch (_: DateTimeParseException) {
            return ""
        }
    }

    return getTimeAgo(context, pastInstant)
}

/**
 * 获取 xx 时间前 格式的字符串
 */
fun getTimeAgo(
    context: Context,
    pastInstant: Instant
): String {
    val now = Instant.now()
    if (pastInstant.isAfter(now)) return context.getString(R.string.just_now)

    val pastZoned = pastInstant.atZone(ZoneId.systemDefault())
    val nowZoned = now.atZone(ZoneId.systemDefault())

    val years = ChronoUnit.YEARS.between(pastZoned, nowZoned)
    if (years > 0) return context.getString(R.string.years_ago, years)

    val months = ChronoUnit.MONTHS.between(pastZoned, nowZoned)
    if (months > 0) return context.getString(R.string.months_ago, months)

    val duration = Duration.between(pastInstant, now)
    val days = duration.toDays()
    if (days > 0) return context.getString(R.string.days_ago, days)

    val hours = duration.toHours()
    if (hours > 0) return context.getString(R.string.hours_ago, hours)

    val minutes = duration.toMinutes()
    if (minutes > 0) return context.getString(R.string.minutes_ago, minutes)

    return context.getString(R.string.just_now)
}

/**
 * 检查是否为给定的日期
 */
fun LocalDate.checkDate(month: Int, day: Int): Boolean {
    return monthValue == month && dayOfMonth == day
}

/**
 * 检查是否为给定的日期范围
 */
fun LocalDate.checkDateRange(month: Int, dayRange: IntRange): Boolean {
    return monthValue == month && dayOfMonth in dayRange
}

/**
 * 检查是否为给定的日期（农历）
 */
fun LunarCalendar.checkDate(month: Int, day: Int): Boolean {
    return lunar.month == month && lunar.day == day
}

/**
 * 检查是否为给定的日期范围（农历）
 */
fun LunarCalendar.checkDateRange(month: Int, dayRange: IntRange): Boolean {
    return lunar.month == month && lunar.day in dayRange
}

/**
 * 获取简单的语言标签
 */
fun Locale.toLangTag(): String {
    return language + "_" + country.lowercase()
}

/**
 * 检查语言标签是否与当前系统匹配
 * 支持此类格式："zh_cn", "en_us", "zh", "en"
 */
fun Locale.compareLangTag(
    targetTag: String
): Boolean {
    return if (targetTag.contains("_")) {
        toLangTag() == targetTag
    } else {
        language == targetTag
    }
}

fun copyText(label: String?, text: String?, context: Context, showToast: Boolean = true) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    if (showToast) {
        Toast.makeText(context, context.getString(R.string.generic_copied), Toast.LENGTH_SHORT).show()
    }
}

fun getDisplayFriendlyRes(displaySideRes: Int, scaling: Float): Int {
    var display = (displaySideRes * scaling).toInt()
    if (display % 2 != 0) display--
    return display
}

fun isAdrenoGPU(): Boolean {
    val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
        Logger.error(TAG, "Failed to get EGL display")
        return false
    }

    if (!EGL14.eglInitialize(eglDisplay, null, 0, null, 0)) {
        Logger.error(TAG, "Failed to initialize EGL")
        return false
    }

    val eglAttributes = intArrayOf(
        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
        EGL14.EGL_NONE
    )

    val configs = arrayOfNulls<EGLConfig>(1)
    val numConfigs = IntArray(1)
    if (!EGL14.eglChooseConfig(
            eglDisplay,
            eglAttributes,
            0,
            configs,
            0,
            1,
            numConfigs,
            0
        ) || numConfigs[0] == 0
    ) {
        EGL14.eglTerminate(eglDisplay)
        Logger.error(TAG, "Failed to choose an EGL config")
        return false
    }

    val contextAttributes = intArrayOf(
        EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
        EGL14.EGL_NONE
    )

    val context = EGL14.eglCreateContext(
        eglDisplay,
        configs[0]!!,
        EGL14.EGL_NO_CONTEXT,
        contextAttributes,
        0
    )
    if (context == EGL14.EGL_NO_CONTEXT) {
        EGL14.eglTerminate(eglDisplay)
        Logger.error(TAG, "Failed to create EGL context")
        return false
    }

    if (!EGL14.eglMakeCurrent(
            eglDisplay,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            context
        )
    ) {
        EGL14.eglDestroyContext(eglDisplay, context)
        EGL14.eglTerminate(eglDisplay)
        Logger.error(TAG, "Failed to make EGL context current")
        return false
    }

    val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
    val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
    val isAdreno = vendor != null && renderer != null &&
            vendor.equals("Qualcomm", ignoreCase = true) &&
            renderer.contains("adreno", ignoreCase = true)

    // Cleanup
    EGL14.eglMakeCurrent(
        eglDisplay,
        EGL14.EGL_NO_SURFACE,
        EGL14.EGL_NO_SURFACE,
        EGL14.EGL_NO_CONTEXT
    )
    EGL14.eglDestroyContext(eglDisplay, context)
    EGL14.eglTerminate(eglDisplay)

    Logger.debug(TAG, "Running on Adreno GPU: $isAdreno")
    return isAdreno
}

fun killProgress() {
    runCatching {
        Process.killProcess(Process.myPid())
    }.onFailure {
        Logger.error(TAG, "Could not enable System.exit() method!", it)
    }
}

fun formatNumberByLocale(context: Context, number: Long): String {
    val locale = context.resources.configuration.locales.get(0)

    return when {
        isSimplifiedChinese(locale) || isTraditionalChinese(locale) -> formatChineseNumber(number)
        else -> formatNonChineseNumber(number)
    }
}

private fun isSimplifiedChinese(locale: Locale): Boolean {
    return locale.language == "zh" && (locale.country == "CN" || locale.script == "Hans")
}

private fun isTraditionalChinese(locale: Locale): Boolean {
    return locale.language == "zh" && (
            locale.country == "TW" || locale.country == "HK" || locale.script == "Hant"
            )
}

private fun formatChineseNumber(number: Long): String {
    return when {
        number < 10_000 -> number.toString()
        number < 100_000_000 -> {
            val value = number / 10_000.0
            formatWithUnit(value, "万")
        }
        else -> {
            val value = number / 100_000_000.0
            formatWithUnit(value, "亿")
        }
    }
}

private fun formatNonChineseNumber(number: Long): String {
    return when {
        number < 1_000 -> number.toString()
        number < 1_000_000 -> {
            val value = number / 1_000.0
            formatWithUnit(value, "K")
        }
        number < 1_000_000_000 -> {
            val value = number / 1_000_000.0
            formatWithUnit(value, "M")
        }
        else -> {
            val value = number / 1_000_000_000.0
            formatWithUnit(value, "B")
        }
    }
}

private fun formatWithUnit(value: Double, unit: String): String {
    val displayValue = if (value < 10) {
        String.format(Locale.US, "%.1f", value)
    } else {
        floor(value).toInt().toString()
    }
    return "$displayValue$unit"
}

fun isChinaMainland(): Boolean {
    val timeZone = TimeZone.getDefault()

    if (
        timeZone.id in listOf(
            "Asia/Shanghai",
            "Asia/Chongqing",//历史遗留
            "Asia/Urumqi"
        )
    ) return true

    val offsetMillis = timeZone.getOffset(System.currentTimeMillis())
    val isUtcPlus8 = offsetMillis == 8 * 60 * 60 * 1000

    if (!isUtcPlus8) return false

    //应用内支持修改语言，不能再以语言来进行判断
    return /*Locale.getDefault().country.equals("CN", ignoreCase = true)*/false
}

/**
 * 检查当前环境是否为中文环境
 */
fun isChinese(context: Context? = null): Boolean {
    //检查默认区域设置
    val defaultLocale = Locale.getDefault()
    if (isChineseLocale(defaultLocale)) {
        return true
    }

    //检查实际区域设置
    val resources = context?.resources
    return resources != null && isChineseLocale(resources.configuration.locales[0])
}

/**
 * 判断单个Locale是否为中文环境
 */
fun isChineseLocale(locale: Locale): Boolean {
    val language = locale.language.lowercase(Locale.ROOT)
    if (language != "zh") return false

    val country = locale.country.uppercase(Locale.ROOT)
    return country in setOf(
        "CN", //中国大陆
        "TW", //台湾
        "HK", //香港
        "MO", //澳门
        "SG", //新加坡
        "MY"  //马来西亚
    )
}

fun isInGreaterChina(): Boolean {
    return isChinaTimeZone() && Locale.getDefault().language == "zh"
}

/**
 * 判断当前时区是否属于中国
 */
private fun isChinaTimeZone(): Boolean {
    return when (TimeZone.getDefault().id) {
        "Asia/Shanghai",
        "Asia/Chongqing",//历史遗留
        "Asia/Hong_Kong",
        "Asia/Macao",
        "Asia/Taipei",
        "Asia/Urumqi" -> true
        else -> false
    }
}

fun printLauncherInfo(
    println: (String) -> Unit
) {
    println("▷ Device: ${Build.PRODUCT} ${Build.MODEL}")
    println("▷ Arch: ${Architecture.archAsString(Architecture.getDeviceArchitecture())}")
    println("▷ Android Version: ${Build.VERSION.RELEASE}")
    println("▷ Launcher Version: ${BuildConfig.VERSION_NAME}, build: ${BuildKeys.BUILD_ARCH}")
}

/**
 * 将崩溃报告写入指定文件
 */
fun writeCrashFile(
    file: File,
    throwable: Throwable,
    onFailure: (Throwable) -> Unit
) {
    runCatching {
        PrintStream(file).use { stream ->
            stream.println("================ ${BuildKeys.LAUNCHER_IDENTIFIER} Crash Report ================")
            stream.println("▷ Time: ${DateFormat.getDateTimeInstance().format(Date())}")
            printLauncherInfo { stream.println(it) }
            stream.println("===================== Crash Stack Trace =====================")
            stream.println(Log.getStackTraceString(throwable))
        }
    }.onFailure(onFailure)
}

fun formatKeyCode(code: Int): String {
    val rawString = KeyEvent.keyCodeToString(code)

    fun formatAsReadableText(input: String): String {
        return input.split("_")
            .joinToString(" ") { word ->
                when (word) {
                    //保留常见缩写的大写
                    "UI", "TV", "API", "NFC", "GPS" -> word
                    else -> word.lowercase()
                        .replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                }
            }
    }

    return when {
        rawString.startsWith("KEYCODE_") -> {
            val s1 = rawString.removePrefix("KEYCODE_")
            when (s1) {
                "DEL" -> "Backspace"
                "FORWARD_DEL" -> "Delete"
                "GRAVE" -> "`"
                "MINUS" -> "-"
                "EQUALS" -> "="
                "LEFT_BRACKET" -> "["
                "RIGHT_BRACKET" -> "]"
                "BACKSLASH" -> "\\"
                "SEMICOLON" -> ";"
                "APOSTROPHE" -> "'"
                "COMMA" -> ","
                "PERIOD" -> "."
                "SLASH" -> "/"
                "NUMPAD_0" -> "Numpad 0"
                "NUMPAD_1" -> "Numpad 1"
                "NUMPAD_2" -> "Numpad 2"
                "NUMPAD_3" -> "Numpad 3"
                "NUMPAD_4" -> "Numpad 4"
                "NUMPAD_5" -> "Numpad 5"
                "NUMPAD_6" -> "Numpad 6"
                "NUMPAD_7" -> "Numpad 7"
                "NUMPAD_8" -> "Numpad 8"
                "NUMPAD_9" -> "Numpad 9"
                "NUMPAD_DIVIDE" -> "Numpad /"
                "NUMPAD_MULTIPLY" -> "Numpad *"
                "NUMPAD_SUBTRACT" -> "Numpad -"
                "NUMPAD_ADD" -> "Numpad +"
                "NUMPAD_DOT" -> "Numpad ."
                "NUMPAD_COMMA" -> "Numpad ,"
                "NUMPAD_ENTER" -> "Numpad Enter"
                "NUMPAD_EQUALS" -> "Numpad ="
                "NUMPAD_LEFT_PAREN" -> "Numpad ("
                "NUMPAD_RIGHT_PAREN" -> "Numpad )"
                "CTRL_LEFT" -> "Left Ctrl"
                "CTRL_RIGHT" -> "Right Ctrl"
                "SHIFT_LEFT" -> "Left Shift"
                "SHIFT_RIGHT" -> "Right Shift"
                "ALT_LEFT" -> "Left Alt"
                "ALT_RIGHT" -> "Right Alt"
                "META_LEFT" -> "Left Meta"
                "META_RIGHT" -> "Right Meta"
                "CAPS_LOCK" -> "Caps Lock"
                "SCROLL_LOCK" -> "Scroll Lock"
                "NUM_LOCK" -> "Num Lock"
                "PAGE_UP" -> "Page Up"
                "PAGE_DOWN" -> "Page Down"
                "MEDIA_PLAY_PAUSE" -> "Play/Pause"
                "MEDIA_STOP" -> "Stop"
                else -> null
            } ?: formatAsReadableText(s1)
        }

        //未知按键
        rawString.startsWith("0x") -> "Key ${rawString.uppercase()}"

        else -> {
            val prefix = when {
                rawString.startsWith("FLAG_") -> "FLAG_"
                rawString.startsWith("ACTION_") -> "ACTION_"
                rawString.startsWith("META_") -> "META_"
                else -> null
            }

            prefix?.let {
                formatAsReadableText(rawString.removePrefix(it))
            } ?: formatAsReadableText(rawString)
        }
    }
}