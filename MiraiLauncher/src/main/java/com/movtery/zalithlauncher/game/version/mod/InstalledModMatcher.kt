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

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.getCFFilesByFingerprints
import com.movtery.zalithlauncher.game.download.assets.platform.getModrinthVersBySha1
import com.movtery.zalithlauncher.utils.logging.Logger
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "InstalledModMatcher"

/** 单次批量匹配的指纹数量，较小的批次可显著降低单次响应的解析内存开销 */
private const val MATCH_BATCH_SIZE = 25

/** 同时进行的批量匹配请求数量 */
private const val MATCH_PARALLELISM = 4

/**
 * 本地模组指纹的平台匹配结果
 * @param byProject 以平台项目Id为键
 * @param byVersion 以平台版本Id为键
 * @param complete 是否所有指纹分块都匹配成功，存在失败分块时部分指纹未匹配
 */
class MatchedInstalledMods(
    val byProject: Map<String, InstalledMod>,
    val byVersion: Map<String, InstalledMod>,
    val complete: Boolean
)

/**
 * 并发计算模组目录内所有文件的指纹
 */
suspend fun scanModFingerprints(modsDir: File): List<ModFingerprints> =
    withContext(Dispatchers.IO) {
        val files = modsDir.listFiles()
            ?.filter { it.isFile && it.isModFileCandidate() }
            ?: return@withContext emptyList()

        val semaphore = Semaphore(READER_PARALLELISM)
        coroutineScope {
            files.map { file ->
                async {
                    semaphore.withPermit {
                        try {
                            computeModFingerprints(file)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            Logger.warning(TAG, "Failed to compute mod fingerprints: ${file.name}", e)
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

/**
 * 将本地模组指纹与指定平台匹配，得到本地已安装的模组信息
 *
 * 优先读取持久缓存，只对未命中的指纹发起批量查询；
 * 每批取回后立即写入持久缓存并通过[onCollect]增量回调，调用方可实时同步到UI
 *
 * @param onCollect 增量匹配结果回调，全部在互斥锁内按顺序触发，不会并发
 */
suspend fun matchInstalledMods(
    fingerprints: List<ModFingerprints>,
    platform: Platform,
    onCollect: suspend (MatchedInstalledMods) -> Unit = {}
): MatchedInstalledMods {
    val mutex = Mutex()
    val byProject = mutableMapOf<String, InstalledMod>()
    val byVersion = mutableMapOf<String, InstalledMod>()
    if (fingerprints.isEmpty()) {
        return MatchedInstalledMods(byProject, byVersion, complete = true)
    }

    val pending = mutableListOf<InstalledMod>()
    var complete = true

    fun collect(installed: InstalledMod) {
        if (installed.notFound) return
        byProject[installed.projectId] = installed
        byVersion[installed.versionId] = installed
        pending.add(installed)
    }

    //增量上报已匹配的结果
    suspend fun flush() {
        if (pending.isEmpty()) return
        onCollect(
            MatchedInstalledMods(
                byProject = pending.associateBy { it.projectId },
                byVersion = pending.associateBy { it.versionId },
                complete = false
            )
        )
        pending.clear()
    }

    val cache = installedModCache()
    val uncached = mutableListOf<ModFingerprints>()
    mutex.withLock {
        for (print in fingerprints) {
            val cached = cache.decodeParcelable(print.cacheKey(platform), InstalledMod::class.java)
            if (cached != null) collect(cached) else uncached.add(print)
        }
        flush()
    }

    // 并发进行批量查询（上限[MATCH_PARALLELISM]），每批取回后立即持久化并增量上报；
    // 失败的批次不写任何缓存，留待下次重试
    coroutineScope {
        val semaphore = Semaphore(MATCH_PARALLELISM)
        uncached.chunked(MATCH_BATCH_SIZE).map { chunk ->
            async {
                semaphore.withPermit {
                    val fetched = try {
                        when (platform) {
                            Platform.MODRINTH -> getModrinthVersBySha1(chunk.map { it.sha1 })
                            Platform.CURSEFORGE ->
                                getCFFilesByFingerprints(chunk.map { it.murmur2 }).mapKeys { it.key.toString() }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        Logger.warning(TAG, "Failed to match installed mods on platform: $platform", e)
                        null
                    }

                    mutex.withLock {
                        if (fetched == null) {
                            complete = false
                        } else {
                            for (print in chunk) {
                                val installed = fetched[print.fingerprintValue(platform)]?.toInstalledMod()
                                    ?: notFoundMod(platform)
                                cache.encode(print.cacheKey(platform), installed, MMKV.ExpireInDay)
                                collect(installed)
                            }
                            flush()
                        }
                    }
                }
            }
        }.awaitAll()
    }

    val isComplete = mutex.withLock { complete }
    return MatchedInstalledMods(byProject, byVersion, isComplete)
}

/**
 * 可能为模组的文件扩展名（.disabled 后缀的文件已去除后缀再判断）
 */
private val MOD_FILE_EXTENSIONS = setOf("jar", "zip", "litemod")

/**
 * 文件是否可能为模组文件，非模组文件不参与指纹计算
 */
private fun File.isModFileCandidate(): Boolean {
    val extension = if (isDisabled()) {
        File(nameWithoutExtension).extension
    } else {
        extension
    }
    return extension.lowercase() in MOD_FILE_EXTENSIONS
}

/**
 * 指纹在持久缓存中的键，不同平台的指纹相互独立
 */
private fun ModFingerprints.cacheKey(platform: Platform): String =
    "${platform.name}/${fingerprintValue(platform)}"

/**
 * 指纹在对应平台上的匹配键
 */
private fun ModFingerprints.fingerprintValue(platform: Platform): String = when (platform) {
    Platform.MODRINTH -> sha1
    Platform.CURSEFORGE -> murmur2.toString()
}

/**
 * 平台未命中指纹时的负缓存标记
 */
private fun notFoundMod(platform: Platform): InstalledMod = InstalledMod(
    platform = platform,
    projectId = "",
    versionId = "",
    versionName = "",
    notFound = true
)
