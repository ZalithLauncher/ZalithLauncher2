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

package com.movtery.zalithlauncher.game.version.mod

import com.movtery.zalithlauncher.utils.file.MurmurHash2Incremental
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/** CurseForge 指纹计算时需要剔除的空白字节：\t、\n、\r、空格 */
val CURSEFORGE_FINGERPRINT_SKIP_BYTES = listOf(0x9, 0xa, 0xd, 0x20)

/**
 * 本地模组文件的指纹
 * @param sha1 文件的 SHA-1 值（Modrinth）
 * @param murmur2 文件按 CurseForge 规则剔除空白字节后的 MurmurHash2 值（CurseForge）
 */
class ModFingerprints(
    val sha1: String,
    val murmur2: Long
)

/**
 * 指纹的进程级内存缓存，以文件绝对路径为键
 *
 * 附带文件大小与最后修改时间校验，文件变化时视为失效，避免重复读盘计算
 */
private object ModFingerprintMemoryCache {
    private class Entry(
        val fingerprints: ModFingerprints,
        val lastModified: Long,
        val length: Long
    )

    private val cache = ConcurrentHashMap<String, Entry>()

    fun get(file: File): ModFingerprints? {
        return cache[file.absolutePath]
            ?.takeIf { it.lastModified == file.lastModified() && it.length == file.length() }
            ?.fingerprints
    }

    fun put(file: File, fingerprints: ModFingerprints) {
        cache[file.absolutePath] = Entry(fingerprints, file.lastModified(), file.length())
    }
}

/**
 * 一次性计算模组文件的平台指纹，相同文件直接复用内存缓存
 *
 * CurseForge 指纹算法要求预先得知剔除空白字节后的总长度，
 * 因此第一遍扫描同时计算 SHA-1 与过滤长度，第二遍仅计算 MurmurHash2
 */
suspend fun computeModFingerprints(file: File): ModFingerprints = withContext(Dispatchers.IO) {
    ModFingerprintMemoryCache.get(file)?.let { return@withContext it }

    val digest = MessageDigest.getInstance("SHA-1")
    var filteredLength = 0

    Files.newInputStream(file.toPath()).use { stream ->
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            currentCoroutineContext().ensureActive()
            digest.update(buffer, 0, bytesRead)
            for (i in 0 until bytesRead) {
                if ((buffer[i].toInt() and 0xFF) !in CURSEFORGE_FINGERPRINT_SKIP_BYTES) filteredLength++
            }
        }
    }

    val sha1 = digest.digest().joinToString("") { "%02x".format(it) }
    val murmur2 = MurmurHash2Incremental.computeHash(
        file = file,
        byteToSkip = CURSEFORGE_FINGERPRINT_SKIP_BYTES,
        filteredLength = filteredLength
    )

    ModFingerprints(sha1 = sha1, murmur2 = murmur2).also {
        ModFingerprintMemoryCache.put(file, it)
    }
}
