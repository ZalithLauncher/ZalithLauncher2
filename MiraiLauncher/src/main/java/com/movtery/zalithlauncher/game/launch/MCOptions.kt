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

package com.movtery.zalithlauncher.game.launch

import android.content.Context
import android.os.Build
import android.os.FileObserver
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "MCOptions"

object MCOptions {
    private val lock = Any()
    private val parameterMap = ConcurrentHashMap<String, String>()
    private var fileObserver: FileObserver? = null
    private lateinit var version: Version

    private val _refreshKey = MutableStateFlow(false)
    /** options.txt 文件刷新 */
    val refreshKey = _refreshKey.asStateFlow()

    /**
     * 初始化 Minecraft 选项配置
     */
    fun setup(context: Context, version: Version) {
        this.version = version
        synchronized(lock) {
            parameterMap.clear()
            fileObserver?.stopWatching()

            val optionsFile = getOptionsFile()
            optionsFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
            if (!optionsFile.exists()) {
                optionsFile.createWithDefaults(context)
            }

            loadInternal()
            setupFileObserver()
        }
    }

    private fun File.createWithDefaults(context: Context) {
        runCatching {
            context.copyAssetFile(
                "game/options.txt",
                parentFile?.absolutePath ?: return,
                false
            )
        }.onFailure {
            Logger.warning(TAG, "Failed to unpack options.txt!", it)
        }
    }

    private fun loadInternal() {
        val optionsFile = getOptionsFile()
        runCatching {
            val newMap = optionsFile.readLines()
                .mapNotNull { line ->
                    line.indexOf(':').takeIf { it > 0 }?.let { idx ->
                        line.take(idx) to line.substring(idx + 1)
                    }
                }.toMap()

            parameterMap.clear()
            parameterMap.putAll(newMap)

            _refreshKey.update { it.not() }
        }.onFailure {
            Logger.warning(TAG, "Failed to load options!", it)
        }
    }

    fun set(key: String, value: String) = parameterMap.put(key, value)

    fun set(key: String, value: List<String>) {
        set(key, value.joinToString(prefix = "[", postfix = "]") { "\"$it\"" })
    }

    fun get(key: String): String? = parameterMap[key]

    fun getAsList(key: String): List<String> {
        val raw = get(key) ?: return emptyList()

        return raw
            .removeSurrounding("[", "]")
            .takeIf { it.isNotBlank() }
            ?.splitPreservingQuotes(',')
            ?.map { it.trim() }
            ?: emptyList()
    }

    fun containsKey(key: String): Boolean = parameterMap.containsKey(key)

    fun save() {
        synchronized(lock) {
            getOptionsFile().takeIf { it.exists() }?.let { file ->
                try {
                    fileObserver?.stopWatching()
                    writeFileAtomically(file)
                } finally {
                    fileObserver?.startWatching()
                }
            }
        }
    }

    private fun writeFileAtomically(targetFile: File) {
        val tempFile = File(targetFile.parent, "${targetFile.name}.tmp").apply {
            deleteOnExit()
        }

        runCatching {
            tempFile.writeText(
                parameterMap.entries.joinToString("\n") { "${it.key}:${it.value}" }
            )
            tempFile.renameTo(targetFile)
        }.onFailure {
            Logger.error(TAG, "Failed to save options.txt!", it)
            tempFile.delete()
        }
    }

    private fun getOptionsFile() = File(version.getGameDir(), "options.txt")

    private fun setupFileObserver() {
        fileObserver?.stopWatching()
        fileObserver = createPlatformFileObserver().apply {
            startWatching()
        }
    }

    @Suppress("DEPRECATION")
    private fun createPlatformFileObserver(): FileObserver {
        val observeTarget = getOptionsFile()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(observeTarget, MODIFY) {
                override fun onEvent(event: Int, path: String?) {
                    if (event and MODIFY != 0) {
                        synchronized(lock) {
                            loadInternal()
                        }
                    }
                }
            }
        } else {
            object : FileObserver(observeTarget.absolutePath, MODIFY) {
                override fun onEvent(event: Int, path: String?) {
                    if (event and MODIFY != 0) {
                        synchronized(lock) {
                            loadInternal()
                        }
                    }
                }
            }
        }
    }
}