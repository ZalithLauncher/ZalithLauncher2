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

package com.movtery.zalithlauncher.utils.logging

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * NusaLauncher: 游戏日志归档（纯 JVM，不依赖 Android，便于单元测试）
 * Menyimpan log setiap sesi Minecraft saat pemain keluar, agar tidak tertimpa sesi berikutnya.
 *
 * Nama file: NusaLogs-yyyy-MM-dd-HH.mm.<Renderer>.log
 */
object GameLogArchiver {
    const val RETENTION_DAYS = 30
    private const val PREFIX = "NusaLogs-"
    private const val EXT = ".log"
    private const val MAX_RENDERER_LENGTH = 40

    /** 渲染器名称中只保留文件名安全字符 */
    fun sanitizeRenderer(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9._-]"), "")
        return if (cleaned.isBlank()) "UnknownRenderer" else cleaned.take(MAX_RENDERER_LENGTH)
    }

    /** @param counter 同一分钟同一渲染器重复时的序号（1 = 不加后缀） */
    fun fileName(time: Date, renderer: String, counter: Int = 1): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd-HH.mm", Locale.US).format(time)
        val suffix = if (counter <= 1) "" else "-$counter"
        return "$PREFIX$stamp.${sanitizeRenderer(renderer)}$suffix$EXT"
    }

    fun isArchiveName(name: String): Boolean = name.startsWith(PREFIX) && name.endsWith(EXT)

    fun buildHeader(info: Map<String, String>, now: Date = Date()): String {
        val saved = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(now)
        return buildString {
            append("==================== NusaLauncher Session ====================\n")
            append("Saved at: $saved\n")
            for ((key, value) in info) append("$key: $value\n")
            append("==============================================================\n")
        }
    }

    /**
     * 将 [source] 复制为归档文件（头部附带会话信息）。
     * @return 归档文件；源文件不存在或为空时返回 null
     */
    fun archive(source: File, targetDir: File, renderer: String, header: String, now: Date = Date()): File? {
        if (!source.isFile || source.length() == 0L) return null
        if (!targetDir.isDirectory && !targetDir.mkdirs()) return null

        var counter = 1
        var target: File
        var created: Boolean
        do {
            target = File(targetDir, fileName(now, renderer, counter))
            created = target.createNewFile()
            counter++
        } while (!created && counter < 1000)
        if (!created) return null

        FileOutputStream(target).use { out ->
            out.write(header.toByteArray(Charsets.UTF_8))
            source.inputStream().use { it.copyTo(out) }
        }
        target.setLastModified(now.time)
        return target
    }

    /** 删除超过保留天数的归档，只处理 NusaLogs-*.log 文件。@return 删除数量 */
    fun cleanup(dir: File, retentionDays: Int = RETENTION_DAYS, nowMillis: Long = System.currentTimeMillis()): Int {
        val cutoff = nowMillis - retentionDays * 86400000L
        var deleted = 0
        dir.listFiles()?.forEach { f ->
            if (f.isFile && isArchiveName(f.name) && f.lastModified() < cutoff && f.delete()) deleted++
        }
        return deleted
    }
}
