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

package com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.utils.string.parseInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.Instant

@Serializable
class ModrinthVersion(
    /** 版本的显示名称 */
    @SerialName("name")
    val name: String,

    /** 版本号 */
    @SerialName("version_number")
    val versionNumber: String,

    /**
     * 版本的变更日志
     *
     * **完全不使用**，但防止有的作者会把这个东西整的老大了，
     * 导致启动器OOM异常，比如模组 [Crash Assistant](https://modrinth.com/mod/crash-assistant)，
     * 所以此字段用不序列化也不应该被代码使用
     */
    @Transient
    @SerialName("changelog")
    @Deprecated("不应该被代码使用！")
    val changelog: String? = null,

    /** 此版本所依赖的特定项目版本的列表 */
    @SerialName("dependencies")
    val dependencies: Array<Dependency>,

    /** 支持的游戏版本 */
    @SerialName("game_versions")
    val gameVersions: Array<String>,

    /** 该版本的发布渠道 */
    @SerialName("version_type")
    val versionType: PlatformReleaseType,

    /** 该版本支持的模组加载器。对于资源包，使用“minecraft” */
    @SerialName("loaders")
    val loaders: Array<String>,

    /** 该版本是否为推荐版本 */
    @SerialName("featured")
    val featured: Boolean,

    @SerialName("status")
    val status: String,

    @SerialName("requested_status")
    val requestedStatus: String? = null,

    /** 版本的ID，以 base62 字符串编码 */
    @SerialName("id")
    val id: String,

    /** 该版本所属项目的ID */
    @SerialName("project_id")
    val projectId: String,

    /** 发布该版本的作者ID */
    @SerialName("author_id")
    val authorId: String,

    @SerialName("date_published")
    val datePublished: String,

    /** 该版本的下载次数 */
    @SerialName("downloads")
    val downloads: Long,

    /** 该版本更新日志的链接。始终为 null，仅为兼容旧版本而保留 */
    @SerialName("changelog_url")
    val changelogUrl: String? = null,

    /** 该版本可下载文件的列表 */
    @SerialName("files")
    val files: Array<ModrinthFile>
) : PlatformVersion {
    @Serializable
    class Dependency(
        /** 这个版本所依赖的版本的ID */
        @SerialName("version_id")
        val versionId: String? = null,

        /** 这个版本所依赖的项目的ID */
        @SerialName("project_id")
        val projectId: String? = null,

        /** 依赖项的文件名，主要用于在模组包中显示外部依赖项 */
        @SerialName("file_name")
        val fileName: String? = null,

        /** 该版本的依赖类型 */
        @SerialName("dependency_type")
        val dependencyType: PlatformDependencyType
    )

    /**
     * 该版本的主要文件
     */
    @Transient
    private lateinit var thisPrimaryFile: ModrinthFile

    override suspend fun initFile(currentProjectId: String): Boolean {
        val file = files.getPrimary() ?: return false
        thisPrimaryFile = file
        return true
    }

    override fun platform(): Platform = Platform.MODRINTH

    override fun platformId(): String = id

    override fun platformDisplayName(): String = name

    override fun platformFileName(): String = thisPrimaryFile.fileName

    override fun platformGameVersion(): Array<String> = gameVersions

    override fun platformLoaders(): List<PlatformDisplayLabel> = loaders.mapNotNull { loaderName ->
        ModrinthModLoaderCategory.entries.find { category ->
            category.facetValue() == loaderName
        }
    }

    override fun platformReleaseType(): PlatformReleaseType = versionType

    override fun platformDependencies(): List<PlatformVersion.PlatformDependency> = dependencies.mapNotNull { dependency ->
        PlatformVersion.PlatformDependency(
            platform = platform(),
            //若未提供项目Id，则判定其无效或被删除，跳过该依赖
            projectId = dependency.projectId ?: return@mapNotNull null,
            type = dependency.dependencyType
        )
    }

    override fun platformDownloadCount(): Long = downloads

    override fun platformDownloadUrl(): String = thisPrimaryFile.url

    override fun platformDatePublished(): Instant = parseInstant(datePublished)

    override fun platformSha1(): String = thisPrimaryFile.hashes.sha1

    override fun platformFileSize(): Long = thisPrimaryFile.size

    override fun platformVersion(): String = versionNumber
}