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

package com.movtery.zalithlauncher.game.download.addon.quick

import com.movtery.zalithlauncher.game.download.addon.favorites.FavoriteEntry
import com.movtery.zalithlauncher.game.download.addon.recognizedPlatformLoaders
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformSearchData
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeData
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeProject
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.getPlatformClassesOrNull
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.ModrinthSearchResult
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.models.mapModrinthCategory
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.models.mapModrinthType
import com.movtery.zalithlauncher.game.version.installed.Version

/**
 * 摘要兼容性
 *
 * 只用于选择对话框提示，最终兼容性由任务内解析的真实文件决定
 */
enum class QuickCompatibility {
    /** 兼容 */
    COMPATIBLE,
    /** 可能不兼容 */
    INCOMPATIBLE,
    /** 未知兼容性 */
    UNKNOWN
}

/**
 * 快捷下载请求，不包含任何需要网络加载的数据。
 *
 * 兼容性摘要仅用于版本选择提示，任务仍会根据真实文件重新判断。
 */
class QuickDownloadRequest internal constructor(
    val platform: Platform,
    val projectId: String,
    val title: String,
    val classes: PlatformClasses,
    private val summary: QuickCompatibilitySummary
) {
    init {
        require(classes != PlatformClasses.MOD_PACK) { "Mod packs do not support quick download." }
        require(classes.versionFolder.folderName.isNotEmpty()) { "The project has no install folder." }
    }

    /** @return 根据本地版本信息给出摘要提示 */
    fun compatibilityWith(version: Version): QuickCompatibility {
        val info = version.getVersionInfo() ?: return QuickCompatibility.UNKNOWN
        return summary.compatibility(
            classes = classes,
            gameVersion = info.minecraftVersion,
            localLoaders = recognizedPlatformLoaders(
                platform = platform,
                targetLoaders = info.loaderInfos.map { it.loader.displayName }
            )
        )
    }

    internal fun compatibilityWith(
        gameVersion: String?,
        localLoaders: List<String>?
    ): QuickCompatibility {
        if (gameVersion == null || localLoaders == null) return QuickCompatibility.UNKNOWN
        return summary.compatibility(classes, gameVersion, localLoaders)
    }
}

/** CurseForge 单个摘要文件的游戏版本与加载器组合，不能跨文件拼接判断 */
internal data class QuickSummaryFile(
    val gameVersion: String,
    /** null 仅表示 API 未提供加载器信息，ANY 才是明确的通配加载器 */
    val loader: CurseForgeModLoader?
)

/** 各平台卡片已有信息的轻量兼容性摘要 */
internal sealed interface QuickCompatibilitySummary {
    fun compatibility(
        classes: PlatformClasses,
        gameVersion: String,
        localLoaders: List<String>
    ): QuickCompatibility

    data object Unknown : QuickCompatibilitySummary {
        override fun compatibility(
            classes: PlatformClasses,
            gameVersion: String,
            localLoaders: List<String>
        ): QuickCompatibility = QuickCompatibility.UNKNOWN
    }

    data class Modrinth(
        val gameVersions: Set<String>?,
        val loaders: Set<String>?
    ) : QuickCompatibilitySummary {
        override fun compatibility(
            classes: PlatformClasses,
            gameVersion: String,
            localLoaders: List<String>
        ): QuickCompatibility {
            val supportedGames = gameVersions ?: return QuickCompatibility.UNKNOWN
            if (gameVersion !in supportedGames) return QuickCompatibility.INCOMPATIBLE
            if (classes != PlatformClasses.MOD) return QuickCompatibility.COMPATIBLE

            val supportedLoaders = loaders ?: return QuickCompatibility.UNKNOWN
            if (supportedLoaders.isEmpty() || localLoaders.isEmpty()) {
                return QuickCompatibility.INCOMPATIBLE
            }
            return if (localLoaders.any { local ->
                supportedLoaders.any { it.equals(local, ignoreCase = true) }
            }) QuickCompatibility.COMPATIBLE else QuickCompatibility.INCOMPATIBLE
        }
    }

    data class CurseForge(
        val files: List<QuickSummaryFile>
    ) : QuickCompatibilitySummary {
        override fun compatibility(
            classes: PlatformClasses,
            gameVersion: String,
            localLoaders: List<String>
        ): QuickCompatibility {
            if (files.isEmpty()) return QuickCompatibility.UNKNOWN
            val matchingFiles = files.filter { it.gameVersion == gameVersion }
            if (matchingFiles.isEmpty()) return QuickCompatibility.INCOMPATIBLE
            if (classes != PlatformClasses.MOD) return QuickCompatibility.COMPATIBLE
            if (localLoaders.isEmpty()) return QuickCompatibility.INCOMPATIBLE

            // 明确匹配优先；没有明确匹配时，缺失的加载器信息才会使结果变为未知。
            return when {
                matchingFiles.any { file ->
                    val loader = file.loader
                    when {
                        loader == CurseForgeModLoader.ANY -> true
                        loader == null -> false
                        else -> localLoaders.any {
                            it.equals(loader.getDisplayName(), ignoreCase = true)
                        }
                    }
                } -> QuickCompatibility.COMPATIBLE
                matchingFiles.any { it.loader == null } -> QuickCompatibility.UNKNOWN
                else -> QuickCompatibility.INCOMPATIBLE
            }
        }
    }
}

/**
 * 使用搜索结果已有字段构造请求
 * @return 整合包返回 null；平台无法提供摘要时使用 [QuickCompatibility.UNKNOWN] 提示。
 */
fun PlatformSearchData.toQuickDownloadRequest(
    defaultClasses: PlatformClasses
): QuickDownloadRequest? {
    val actualClasses = when (this) {
        is ModrinthSearchResult.ModrinthProject -> {
            projectType.mapModrinthType()?.platform ?: defaultClasses
        }
        is CurseForgeData -> {
            getPlatformClassesOrNull() ?: defaultClasses
        }
        else -> defaultClasses
    }
    if (actualClasses == PlatformClasses.MOD_PACK) return null

    val summary = when (this) {
        is ModrinthSearchResult.ModrinthProject -> {
            QuickCompatibilitySummary.Modrinth(
                gameVersions = versions.toSet(),
                loaders = modrinthLoaders(displayCategories ?: categories, actualClasses)
            )
        }
        is CurseForgeData -> {
            QuickCompatibilitySummary.CurseForge(
                files = latestFilesIndexes.map { index ->
                    QuickSummaryFile(index.gameVersion, index.modLoader)
                }
            )
        }
        else -> QuickCompatibilitySummary.Unknown
    }

    return QuickDownloadRequest(
        platform = platform(),
        projectId = platformId(),
        title = platformTitle().ifEmpty { platformId() },
        classes = actualClasses,
        summary = summary
    )
}

/** 使用项目详情摘要构造请求；项目不可用或属于整合包时返回 null。 */
fun PlatformProject.toQuickDownloadRequest(
    defaultClasses: PlatformClasses
): QuickDownloadRequest? {
    if (!platformAvailable()) return null
    val actualClasses = platformClasses(defaultClasses)
    if (actualClasses == PlatformClasses.MOD_PACK) return null

    val summary = when (this) {
        is ModrinthSingleProject -> QuickCompatibilitySummary.Modrinth(
            gameVersions = gameVersions.toSet(),
            loaders = modrinthLoaders(loaders)
        )
        is CurseForgeProject -> QuickCompatibilitySummary.CurseForge(
            files = data.latestFilesIndexes.map { index ->
                QuickSummaryFile(index.gameVersion, index.modLoader)
            }
        )
        else -> QuickCompatibilitySummary.Unknown
    }
    return QuickDownloadRequest(
        platform = platform(),
        projectId = platformId(),
        title = platformTitle().ifEmpty { platformId() },
        classes = actualClasses,
        summary = summary
    )
}

fun FavoriteEntry.toQuickDownloadRequest(): QuickDownloadRequest? {
    if (invalid) return null
    remote?.let {
        // 收藏优先复用远端项目摘要
        return it.toQuickDownloadRequest(project.classes)
    }
    if (project.classes == PlatformClasses.MOD_PACK) return null

    // 只有本地信息时保留请求并将兼容性标为未知
    return QuickDownloadRequest(
        platform = platform,
        projectId = project.projectId,
        title = project.title.ifEmpty { project.projectId },
        classes = project.classes,
        summary = QuickCompatibilitySummary.Unknown
    )
}

/**
 * 将 Modrinth facet 或显示名规整为本地可比较的名称。
 * 内容类别标签会被忽略；存在无法识别的加载器时返回 null，表示摘要兼容性未知。
 */
internal fun modrinthLoaders(
    values: Array<String>?,
    classes: PlatformClasses? = null
): Set<String>? {
    var hasUnknownLoader = false
    val loaders = values?.mapNotNull { value ->
        val loader = ModrinthModLoaderCategory.entries.firstOrNull {
            it.facetValue().equals(value, ignoreCase = true) ||
                    it.getDisplayName().equals(value, ignoreCase = true)
        }
        when {
            loader != null -> loader.getDisplayName()
            // 搜索结果的 categories 同时包含内容标签，这些标签不是未知加载器。
            classes != null && value.mapModrinthCategory(classes) != null -> null
            else -> {
                hasUnknownLoader = true
                null
            }
        }
    } ?: return null
    return loaders.toSet().takeUnless { hasUnknownLoader }
}
