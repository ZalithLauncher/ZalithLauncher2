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

package com.movtery.zalithlauncher.game.download.assets.favorites

import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchData
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import java.time.Instant

/**
 * 收藏类型：
 * - [PROJECT] 仅收藏项目本身，不绑定具体版本
 * - [VERSION] 收藏项目的某个具体版本，以平台版本 ID 标识身份
 *
 * 项目收藏与版本收藏完全独立，不做任何级联（收藏项目不收藏版本，删除项目不连带删版本）。
 *
 * 注意：枚举常量名会被 GSON 按名写入持久化 JSON，属于持久化契约的一部分，禁止重命名。
 */
enum class AssetFavoriteType {
    PROJECT,
    VERSION
}

/**
 * 资源收藏项，以 GSON JSON 形式持久化到 MMKV，按 [storageKey] 唯一约束。
 * - [type] 为 [AssetFavoriteType.PROJECT] 时仅收藏项目本身，[versionId] 为 null、[downloadUrl] 为空串；
 * - [type] 为 [AssetFavoriteType.VERSION] 时收藏项目的某个具体版本，[versionId] 必填
 *   （CurseForge 为 fileId，Modrinth 为 versionId，取自 [PlatformVersion.platformId]）。
 * 同一项目最多 1 条 PROJECT 收藏 + N 条 VERSION 收藏（每个版本 ID 一条）。
 *
 * 持久化契约（变更即视为格式演进，需提升 [CURRENT_SCHEMA_VERSION]）：
 * - 各字段的 [SerializedName] 值禁止修改；
 * - [platform]/[classes]/[type]/[releaseType] 的枚举常量名由 GSON 按名序列化，禁止重命名。
 *
 * @property schemaVersion 数据结构版本号，读取时仅接受与 [CURRENT_SCHEMA_VERSION] 一致的条目，不符者跳过
 * @property type 收藏类型
 * @property platform 所属平台
 * @property classes 资源所属类别
 * @property projectId 项目在平台上的 ID
 * @property versionId 版本在平台上的 ID（VERSION 必填，PROJECT 为 null），版本身份标识
 * @property versionName 版本号名称（展示用）
 * @property versionFileName 版本文件名（下载落盘用）
 * @property downloadUrl 平台原始下载直链快照，仅作下载载荷（下载时仍走 mapMCIMMirrorUrls 流程），不参与身份判定
 * @property sha1 版本文件 sha1 值（下载校验用）
 * @property fileSize 版本文件总大小
 * @property gameVersions 版本兼容的游戏主版本列表（展示/快照用）
 * @property loaders 加载器显示名列表（展示/快照用）
 * @property releaseType 版本发布类型
 * @property title 项目标题（展示快照）
 * @property slug 项目别名（展示快照）
 * @property author 项目主要作者（展示快照）
 * @property description 项目描述（展示快照）
 * @property iconUrl 项目图标链接（展示快照）
 * @property savedAt 收藏时间戳（毫秒），同 key 更新时保留首次收藏时间
 */
data class AssetFavorite(
    @SerializedName("schemaVersion")
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    @SerializedName("type")
    val type: AssetFavoriteType,
    @SerializedName("platform")
    val platform: Platform,
    @SerializedName("classes")
    val classes: PlatformClasses,
    @SerializedName("projectId")
    val projectId: String,
    @SerializedName("versionId")
    val versionId: String? = null,
    @SerializedName("versionName")
    val versionName: String? = null,
    @SerializedName("versionFileName")
    val versionFileName: String? = null,
    @SerializedName("downloadUrl")
    val downloadUrl: String = "",
    @SerializedName("sha1")
    val sha1: String? = null,
    @SerializedName("fileSize")
    val fileSize: Long = 0L,
    @SerializedName("gameVersions")
    val gameVersions: List<String> = emptyList(),
    @SerializedName("loaders")
    val loaders: List<String> = emptyList(),
    @SerializedName("releaseType")
    val releaseType: PlatformReleaseType? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("iconUrl")
    val iconUrl: String? = null,
    @SerializedName("savedAt")
    val savedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 当前数据结构版本号，字段增删或语义变化时递增；
         * 读取端按版本号门禁，旧格式条目静默跳过（不做迁移）
         */
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

/** 收藏项 key 的稳定字面量前缀（持久化契约，禁止修改） */
private const val KEY_PREFIX = "asset_favorite"
private const val KEY_SEPARATOR = "|"

/** 收藏类型在 key 中的稳定字面量，不随 [AssetFavoriteType] 枚举重命名而变化（持久化契约，禁止修改） */
private const val TYPE_KEY_PROJECT = "p"
private const val TYPE_KEY_VERSION = "v"

/**
 * 构建收藏项的稳定存储键，格式：
 * `asset_favorite|<platform>|<projectId>|<p|v>|<versionId?>`
 * - platform 段使用 [Platform.name]，该枚举常量名即持久化契约，禁止重命名（重命名会产生孤儿数据）；
 * - 类型段使用稳定字面量 p/v，不受 [AssetFavoriteType] 枚举重命名影响；
 * - versionId 段仅 VERSION 收藏存在，PROJECT 收藏为空串。
 * @param platform 所属平台
 * @param projectId 项目在平台上的 ID
 * @param type 收藏类型
 * @param versionId 版本在平台上的 ID（VERSION 必填）
 */
fun assetFavoriteStorageKey(
    platform: Platform,
    projectId: String,
    type: AssetFavoriteType,
    versionId: String? = null
): String = buildString {
    append(KEY_PREFIX).append(KEY_SEPARATOR)
    append(platform.name).append(KEY_SEPARATOR)
    append(projectId).append(KEY_SEPARATOR)
    append(
        when (type) {
            AssetFavoriteType.PROJECT -> TYPE_KEY_PROJECT
            AssetFavoriteType.VERSION -> TYPE_KEY_VERSION
        }
    ).append(KEY_SEPARATOR)
    append(versionId.orEmpty())
}

/**
 * 收藏项的稳定存储键，详见 [assetFavoriteStorageKey]
 */
val AssetFavorite.storageKey: String
    get() = assetFavoriteStorageKey(platform, projectId, type, versionId)

/**
 * 某项目下全部收藏项（项目收藏 + 所有版本收藏）的 key 公共前缀，供按前缀批量匹配
 */
internal fun assetFavoriteProjectKeyPrefix(platform: Platform, projectId: String): String =
    "$KEY_PREFIX$KEY_SEPARATOR${platform.name}$KEY_SEPARATOR$projectId$KEY_SEPARATOR"

/**
 * 收藏项是否已绑定一个具体可下载的版本（VERSION 且版本 ID 与下载直链均非空）
 */
val AssetFavorite.hasFixedVersion: Boolean
    get() = type == AssetFavoriteType.VERSION && !versionId.isNullOrBlank() && downloadUrl.isNotBlank()

/**
 * 由 [PlatformProject] 构建一个仅收藏项目本身的收藏项（不绑定版本）。
 * @param classes 资源所属类别
 * @param previousSavedAt 仅当"同 key 更新"（同一项目再次收藏）时，传入原收藏项的 savedAt 以保留首次收藏时间；
 * 新收藏必须传 null
 */
fun PlatformProject.toAssetFavorite(
    classes: PlatformClasses,
    previousSavedAt: Long? = null
): AssetFavorite = AssetFavorite(
    type = AssetFavoriteType.PROJECT,
    platform = platform(),
    classes = classes,
    projectId = platformId(),
    title = platformTitle(),
    slug = platformSlug(),
    author = platformAuthor(),
    description = platformSummary(),
    iconUrl = platformIconUrl(),
    loaders = platformModLoaders()?.map { it.getDisplayName() }.orEmpty(),
    savedAt = previousSavedAt ?: System.currentTimeMillis()
)

/**
 * 由 [PlatformSearchData]（平台搜索结果单项）构建一个仅收藏项目本身的收藏项（不绑定版本）。
 * 搜索结果不含项目别名，[AssetFavorite.slug] 记为 null；
 * [AssetFavorite.description] 取 [PlatformSearchData.platformDescription] 快照。
 * @param classes 资源所属类别
 * @param previousSavedAt 仅当"同 key 更新"（同一项目再次收藏）时，传入原收藏项的 savedAt 以保留首次收藏时间；
 * 新收藏必须传 null
 */
fun PlatformSearchData.toAssetFavorite(
    classes: PlatformClasses,
    previousSavedAt: Long? = null
): AssetFavorite = AssetFavorite(
    type = AssetFavoriteType.PROJECT,
    platform = platform(),
    classes = classes,
    projectId = platformId(),
    title = platformTitle(),
    author = platformAuthor(),
    description = platformDescription(),
    iconUrl = platformIconUrl(),
    loaders = platformModLoaders()?.map { it.getDisplayName() }.orEmpty(),
    savedAt = previousSavedAt ?: System.currentTimeMillis()
)

/**
 * 由 [PlatformVersion]（已 initFile）+ 所属 [PlatformProject] 构建一个版本收藏项。
 * 版本身份取 [PlatformVersion.platformId]（CurseForge 为 fileId，Modrinth 为 versionId），
 * [AssetFavorite.downloadUrl] 仅为下载载荷快照，不参与身份判定。
 * @param project 版本所属的项目
 * @param classes 资源所属类别
 * @param previousSavedAt 仅当"同 key 更新"（同一版本再次收藏）时，传入原收藏项的 savedAt 以保留首次收藏时间；
 * 收藏同一项目的其他版本时不得传入，否则会错误继承旧版本收藏的时间戳
 */
fun PlatformVersion.toAssetFavorite(
    project: PlatformProject,
    classes: PlatformClasses,
    previousSavedAt: Long? = null
): AssetFavorite = AssetFavorite(
    type = AssetFavoriteType.VERSION,
    platform = project.platform(),
    classes = classes,
    projectId = project.platformId(),
    versionId = platformId(),
    versionName = platformVersion(),
    versionFileName = platformFileName(),
    downloadUrl = platformDownloadUrl(),
    sha1 = platformSha1(),
    fileSize = platformFileSize(),
    gameVersions = platformGameVersion().toList(),
    loaders = platformLoaders().map { it.getDisplayName() },
    releaseType = platformReleaseType(),
    title = project.platformTitle(),
    slug = project.platformSlug(),
    author = project.platformAuthor(),
    description = project.platformSummary(),
    iconUrl = project.platformIconUrl(),
    savedAt = previousSavedAt ?: System.currentTimeMillis()
)

/**
 * 将收藏项转为 [PlatformVersion] 快照，用于直接复用下载对话框与下载任务链路。
 * 仅对 [hasFixedVersion] 为 true 的版本收藏有意义。
 */
fun AssetFavorite.toPlatformVersion(): PlatformVersion = AssetFavoriteSnapshot(this)

/**
 * 由 [AssetFavorite] 构建的 [PlatformVersion] 快照实现，供收藏列表"直接下载"复用现有对话框。
 *
 * 仅还原下载所需的关键字段（文件名、直链、sha1、大小、游戏版本、加载器等），
 * 不还原依赖项（收藏项不缓存依赖，依赖为空列表）。
 */
private class AssetFavoriteSnapshot(
    private val favorite: AssetFavorite
) : PlatformVersion {
    override suspend fun initFile(currentProjectId: String): Boolean = true

    override fun platform(): Platform = favorite.platform

    //如实返回收藏时记录的平台版本 ID（CurseForge 为 fileId，Modrinth 为 versionId）
    override fun platformId(): String = favorite.versionId.orEmpty()

    override fun platformDisplayName(): String = favorite.versionName ?: favorite.title

    override fun platformFileName(): String = favorite.versionFileName ?: favorite.title

    override fun platformGameVersion(): Array<String> = favorite.gameVersions.toTypedArray()

    override fun platformLoaders(): List<PlatformDisplayLabel> =
        favorite.loaders.map { SnapshotLabel(it) }

    override fun platformReleaseType(): PlatformReleaseType =
        favorite.releaseType ?: PlatformReleaseType.RELEASE

    override fun platformDependencies(): List<PlatformVersion.PlatformDependency> = emptyList()

    override fun platformDownloadCount(): Long = 0L

    override fun platformDownloadUrl(): String = favorite.downloadUrl

    override fun platformDatePublished(): Instant = Instant.ofEpochMilli(favorite.savedAt)

    override fun platformSha1(): String? = favorite.sha1

    override fun platformFileSize(): Long = favorite.fileSize

    override fun platformVersion(): String = favorite.versionName.orEmpty()
}

/**
 * 仅展示用的加载器标签，直接使用 displayName 字符串
 */
private class SnapshotLabel(private val displayName: String) : PlatformDisplayLabel {
    override fun getDisplayName(): String = displayName
    override fun index(): Int = 0
}
