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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge

import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.download.assets.platform.AbstractPlatformSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFingerprintsMatches
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeProject
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeVersions
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.isApproved
import com.movtery.zalithlauncher.utils.file.MurmurHash2Incremental
import com.movtery.zalithlauncher.utils.network.httpGetJson
import com.movtery.zalithlauncher.utils.network.httpPostJson
import io.ktor.http.Parameters
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File

class CurseForgeSearcher(
    val api: String = CURSEFORGE_API,
    val apiKey: String? = BuildKeys.CURSEFORGE_API,
    source: String = "Official CurseForge"
): AbstractPlatformSearcher(
    platform = Platform.CURSEFORGE,
    source = source
) {
    override suspend fun searchAssets(
        query: String,
        searchFilter: PlatformSearchFilter,
        platformClasses: PlatformClasses
    ): CurseForgeSearchResult {
        return httpGetJson(
            url = "$api/mods/search",
            headers = listOf("x-api-key" to apiKey),
            parameters = searchFilter.toCurseForgeRequest(
                query = query,
                platformClasses = platformClasses
            ).toParameters()
        )
    }

    override suspend fun getProject(projectID: String): CurseForgeProject {
        val project = httpGetJson<CurseForgeProject>(
            url = "$api/mods/$projectID",
            headers = listOf("x-api-key" to apiKey)
        )
        if (!project.isApproved()) throw NotFoundException("The project {$projectID} is not in a publicly available state.")
        return project
    }

    /**
     * 在 CurseForge 平台获取某项目的某个文件
     */
    suspend fun getVersion(
        projectID: String,
        fileID: String,
    ): CurseForgeVersion {
        return httpGetJson(
            url = "$api/mods/$projectID/files/$fileID",
            headers = listOf("x-api-key" to apiKey)
        )
    }

    /**
     * 在 CurseForge 平台根据分页获取项目的版本列表
     * @param index 开始处
     * @param pageSize 每页请求数量
     */
    suspend fun getVersions(
        projectID: String,
        index: Int = 0,
        pageSize: Int = 100
    ): CurseForgeVersions = httpGetJson(
        url = "$api/mods/$projectID/files",
        headers = listOf("x-api-key" to apiKey),
        parameters = Parameters.build {
            append("index", index.toString())
            append("pageSize", pageSize.toString())
        }
    )

    override suspend fun getVersions(
        projectID: String,
        pageCallback: (chunk: Int, page: Int) -> Unit
    ): List<CurseForgeFile> {
        return getAllVersions(
            pageSize = 50,
            chunkSize = 20,
            maxConcurrent = 10,
            pageCallback = pageCallback,
            checkNotEmpty = { versions ->
                versions.data.isNotEmpty()
            },
            asyncVersions = { index, pageSize ->
                getVersions(
                    projectID = projectID,
                    index = index,
                    pageSize = pageSize,
                )
            },
            processVersions = { versions ->
                val files = versions?.data ?: emptyArray()
                files.toList() to files.size
            }
        )
    }

    override suspend fun getVersionByLocalFile(
        file: File,
        sha1: String
    ): CurseForgeFile? {
        val hash = MurmurHash2Incremental.computeHash(file, byteToSkip = listOf(0x9, 0xa, 0xd, 0x20))
        return httpPostJson<CurseForgeFingerprintsMatches>(
            url = "$api/fingerprints",
            headers = listOf("x-api-key" to apiKey),
            body = mapOf("fingerprints" to listOf(hash))
        ).data.exactMatches
            ?.takeIf { it.isNotEmpty() }
            ?.firstOrNull()
            ?.file
    }
}

/**
 * 持续分页获取项目的所有版本文件，直到全部加载完成
 * @param pageSize 每页请求数量
 * @param chunkSize 一个区间的最大页数
 * @param maxConcurrent 同时最多允许的请求数
 * @param pageCallback 加载每一页时都通过此函数回调
 * @param checkNotEmpty 检查请求内容返回结果不为空
 * @param asyncVersions 异步获取单区块的版本数据
 * @param processVersions 加工返回数据，同时需要返回当前结果实际的页面大小
 */
private suspend fun <E, T> getAllVersions(
    pageSize: Int = 100,
    chunkSize: Int = 10,
    maxConcurrent: Int = 5,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _ , _ -> },
    checkNotEmpty: (E) -> Boolean,
    asyncVersions: suspend (index: Int, pageSize: Int) -> E,
    processVersions: suspend (E?) -> Pair<List<T>, Int>
): List<T> = coroutineScope {
    val allVersions = mutableListOf<T>()
    /** 当前区间编号 */
    var currentChunk = 1
    /** 起始页码 */
    var startPage = 0
    /** 是否已经到达过最后一页，控制是否进入下一区间 */
    var reachedEnd = false

    val semaphore = Semaphore(maxConcurrent)

    while (!reachedEnd) {
        //创建当前区间的任务列表
        val jobs = (0 until chunkSize).map { offset ->
            val pageIndex = startPage + offset
            val index = pageIndex * pageSize

            async {
                semaphore.withPermit {
                    val response = asyncVersions(index, pageSize)
                    //检查当前页返回的结果是否正常
                    //如果是最后一页之后的内容，则这里的列表是空的
                    if (checkNotEmpty(response)) {
                        //有东西，回调即可
                        pageCallback(currentChunk, pageIndex + 1)
                        response
                    } else null
                }
            }
        }

        for ((i, job) in jobs.withIndex()) {
            val (files, realSize) = processVersions(job.await())
            files.takeIf { it.isNotEmpty() }?.let { list ->
                allVersions.addAll(list)
            }

            //少于pageSize，已经是最后一页
            if (realSize < pageSize) {
                reachedEnd = true
                //取消后续页
                for (j in (i + 1) until jobs.size) {
                    jobs[j].cancel()
                }
                break
            }
        }

        //如果没发现最后一页，则进入下一区间
        if (!reachedEnd) {
            startPage += chunkSize
            currentChunk++
        }
    }

    return@coroutineScope allVersions
}