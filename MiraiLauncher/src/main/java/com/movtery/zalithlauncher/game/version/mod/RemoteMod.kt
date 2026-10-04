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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionByLocalFile
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcMod
import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.utils.file.calculateFileSha1
import com.movtery.zalithlauncher.utils.logging.Logger
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private const val TAG = "RemoteMod"

class RemoteMod(
    val localMod: LocalMod
) {
    /**
     * 是否正在加载项目信息
     */
    var isLoading by mutableStateOf(false)
        private set

    /**
     * 平台对应的文件
     */
    var remoteFile: ModFile? by mutableStateOf(null)
        private set

    /**
     * 项目信息
     */
    var projectInfo: ModProject? by mutableStateOf(null)
        private set

    /**
     * 项目翻译信息
     */
    var mcMod: ModTranslations.McMod? by mutableStateOf(null)
        private set

    /**
     * 是否已经加载过
     */
    var isLoaded: Boolean = false
        private set

    /**
     * @param loadFromCache 是否从缓存中加载
     */
    suspend fun load(loadFromCache: Boolean) {
        if (loadFromCache && isLoaded) return

        if (!localMod.checkRemote) {
            isLoaded = true
            return
        }

        if (!loadFromCache) {
            remoteFile = null
            projectInfo = null
            mcMod = null
        }

        isLoaded = false
        isLoading = true

        try {
            withContext(Dispatchers.IO) {
                val file = localMod.file
                val modProjectCache = modProjectCache()
                val modFileCache = modFileCache()

                runCatching {
                    //获取文件 sha1，作为缓存的键
                    val sha1 = calculateFileSha1(file)

                    //从缓存加载项目信息
                    val cachedProject = if (loadFromCache) {
                        modProjectCache.decodeParcelable(sha1, ModProject::class.java)
                    } else null

                    //从缓存加载文件信息
                    val cachedFile = if (loadFromCache) {
                        modFileCache.decodeParcelable(sha1, ModFile::class.java)
                    } else null

                    if (loadFromCache && cachedFile != null) {
                        remoteFile = cachedFile
                    } else {
                        loadRemoteFile(sha1)?.let { modFile ->
                            remoteFile = modFile
                            modFileCache.encode(sha1, modFile, MMKV.ExpireInDay)
                        }
                    }

                    if (loadFromCache && cachedProject != null) {
                        projectInfo = cachedProject
                        mcMod = PlatformClasses.MOD.getTranslations().getModBySlugId(cachedProject.slug)
                    } else {
                        ensureActive()
                        remoteFile?.let { modFile ->
                            val project = getProjectByVersion(
                                projectId = modFile.projectId,
                                platform = modFile.platform,
                                printLog = false
                            )
                            val newProjectInfo = ModProject(
                                id = project.platformId(),
                                platform = project.platform(),
                                iconUrl = project.platformIconUrl(),
                                title = project.platformTitle(),
                                slug = project.platformSlug()
                            )

                            projectInfo = newProjectInfo
                            mcMod = project.getMcMod(PlatformClasses.MOD)

                            modProjectCache.encode(sha1, newProjectInfo, MMKV.ExpireInDay)
                        }
                    }

                    isLoaded = true
                }.onFailure { e ->
                    if (e is CancellationException) return@onFailure
                    Logger.warning(TAG, "Failed to load project info for mod: ${file.name}", e)
                }
            }
        } finally {
            isLoading = false
        }
    }

    suspend fun loadRemoteFile(
        sha1: String? = null
    ): ModFile? {
        val file = localMod.file
        val sha10 = sha1 ?: calculateFileSha1(file)
        val version = getVersionByLocalFile(file, sha10)
        return version?.toModFile()
    }

    private fun PlatformVersion.toModFile(): ModFile {
        return when (this) {
            is ModrinthVersion -> {
                ModFile(
                    id = id,
                    projectId = projectId,
                    platform = Platform.MODRINTH,
                    loaders = loaders.mapNotNull { loaderName ->
                        ModrinthModLoaderCategory.entries.find { it.facetValue() == loaderName }
                    }.toTypedArray(),
                    datePublished = datePublished
                )
            }
            is CurseForgeFile -> {
                ModFile(
                    id = id.toString(),
                    projectId = modId.toString(),
                    platform = Platform.CURSEFORGE,
                    loaders = gameVersions.mapNotNull { loaderName ->
                        CurseForgeModLoader.entries.find {
                            it.getDisplayName().equals(loaderName, true)
                        }
                    }.toTypedArray(),
                    datePublished = fileDate
                )
            }
            else -> error("Unknown version type: $this")
        }
    }
}