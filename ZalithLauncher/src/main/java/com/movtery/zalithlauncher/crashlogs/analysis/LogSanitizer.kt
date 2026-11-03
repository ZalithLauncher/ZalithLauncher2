/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.crashlogs.analysis

/**
 * NusaLauncher: 上传日志前，对敏感信息进行脱敏
 * Menyensor data sensitif dari log sebelum diunggah ke layanan pihak ketiga (mclo.gs).
 */
object LogSanitizer {
    private val jwt = Regex("eyJ[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]*")

    private val tokenAssignment = Regex(
        "(access[_-]?token|refresh[_-]?token|bearer|authorization)([\"':=\\s]+)([A-Za-z0-9._~+/=-]{16,})",
        RegexOption.IGNORE_CASE
    )

    /** 启动参数：参数名在一行，值在下一行（或同一行） */
    private val sensitiveArg = Regex(
        "(--(?:accessToken|uuid|xuid|clientId)[ \\t]*\\r?\\n?[ \\t]*(?:▷[ \\t]*)?)([^\\r\\n]+)"
    )

    private val email = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}")

    private val ipv4 = Regex("\\b(?:(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)\\b")

    fun sanitize(log: String): String {
        var out = log
        out = sensitiveArg.replace(out) { m -> m.groupValues[1] + "<hidden>" }
        out = jwt.replace(out, "<token>")
        out = tokenAssignment.replace(out) { m -> m.groupValues[1] + m.groupValues[2] + "<token>" }
        out = email.replace(out, "<email>")
        out = ipv4.replace(out) { m ->
            val ip = m.value
            if (ip == "0.0.0.0" || ip.startsWith("127.")) ip else "<ip>"
        }
        return out
    }
}
