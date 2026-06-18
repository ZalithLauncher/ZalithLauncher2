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

import android.util.Log
import com.movtery.zalithlauncher.utils.file.UnpackZipException
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "AllModReader"

const val READER_PARALLELISM = 8

class AllModReader(val modsDir: File) {
    val resultsMutex = Mutex()

    private fun <T> scanFiles(
        pack: (LocalMod) -> T
    ): List<ReadTask<T>> {
        val files = modsDir.listFiles()?.filter { !it.isDirectory } ?: return emptyList()
        return files.map { file ->
            ReadTask(file, pack)
        }
    }

    /**
     * 异步读取所有模组文件，将其包装为支持同步远端项目数据的对象
     */
    suspend fun readAllForRemote(): List<RemoteMod> = withContext(Dispatchers.IO) {
        //扫描文件，封装任务
        val results = readAllMods { RemoteMod(localMod = it) }
        return@withContext results.sortedBy { it.localMod.file.name }
    }

    /**
     * 异步读取所有模组文件，获取所有本地模组信息
     */
    suspend fun readAllLocals(): List<LocalMod> = withContext(Dispatchers.IO) {
        readAllMods { it }
    }

    private suspend fun <T> readAllMods(
        pack: (LocalMod) -> T
    ): List<T> = withContext(Dispatchers.IO) {
        //扫描文件，封装任务
        val tasks = scanFiles(pack)

        buildList {
            val taskChannel = Channel<ReadTask<T>>(Channel.UNLIMITED)

            val workers = List(READER_PARALLELISM) {
                launch(Dispatchers.IO) {
                    for (task in taskChannel) {
                        val mod = task.execute()
                        resultsMutex.withLock {
                            add(mod)
                        }
                    }
                }
            }

            tasks.forEach { taskChannel.send(it) }
            taskChannel.close()

            workers.joinAll()
        }
    }

    private class ReadTask<T>(
        private val file: File,
        private val pack: (LocalMod) -> T
    ) {
        suspend fun execute(): T {
            try {
                currentCoroutineContext().ensureActive()

                val extension = if (file.isDisabled()) {
                    File(file.nameWithoutExtension).extension
                } else {
                    file.extension
                }

                return MOD_READERS[extension]?.firstNotNullOfOrNull { reader ->
                    runCatching {
                        pack(
                            reader.fromLocal(file)
                        )
                    }.onFailure { e ->
                        if (e !is UnpackZipException) Log.d(TAG, "Exception encountered while parsing the mod", e)
                    }.getOrNull()
                    //返回null，继续使用下一个解析器
                } ?: throw IllegalArgumentException("No matching reader for extension: $extension")
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> throw e
                    else -> {
                        Logger.warning(TAG, "Failed to read mod: $file", e)
                        return pack(
                            createNotMod(file)
                        )
                    }
                }
            }
        }
    }
}