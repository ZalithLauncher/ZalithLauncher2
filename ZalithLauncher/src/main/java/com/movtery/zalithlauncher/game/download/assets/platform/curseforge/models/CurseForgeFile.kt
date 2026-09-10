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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile.Hash
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredCurseForgeSource
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredPlatformSearcher
import com.movtery.zalithlauncher.game.versioninfo.filterRelease
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.parseInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import java.io.FileNotFoundException
import java.io.IOException
import java.time.Instant

@Serializable
class CurseForgeFile(
    /**
     * 文件 ID
     */
    @SerialName("id")
    val id: Int,

    /**
     * 与此文件所属的项目的相关的游戏 ID
     */
    @SerialName("gameId")
    val gameId: Int,

    /**
     * 项目 ID
     */
    @SerialName("modId")
    val modId: Int,

    /**
     * 文件是否可供下载
     */
    @SerialName("isAvailable")
    val isAvailable: Boolean,

    /**
     * 文件的展示名称
     */
    @SerialName("displayName")
    val displayName: String,

    /**
     * 确切的文件名
     */
    @SerialName("fileName")
    val fileName: String? = null,

    /**
     * 文件的发布类型
     */
    @SerialName("releaseType")
    val releaseType: PlatformReleaseType,

    /**
     * 文件的状态
     */
    @SerialName("fileStatus")
    val fileStatus: Int,

    /**
     * 文件哈希值（即 md5 或 sha1）
     */
    @SerialName("hashes")
    val hashes: Array<Hash>,

    /**
     * 文件的时间戳
     */
    @SerialName("fileDate")
    val fileDate: String,

    /**
     * 文件长度（以字节为单位）
     */
    @SerialName("fileLength")
    val fileLength: Long,

    /**
     * 文件的下载量
     */
    @SerialName("downloadCount")
    val downloadCount: Long,

    /**
     * 文件在硬盘上的大小
     */
    @SerialName("fileSizeOnDisk")
    val fileSizeOnDisk: Long? = null,

    /**
     * 文件的下载 URL
     */
    @SerialName("downloadUrl")
    val downloadUrl: String? = null,

    /**
     * 此文件相关的游戏版本列表
     */
    @SerialName("gameVersions")
    val gameVersions: Array<String>,

    /**
     * 用于按游戏版本排序的元数据
     */
    @SerialName("sortableGameVersions")
    val sortableGameVersions: JsonArray,

    /**
     * 依赖项文件列表
     */
    @SerialName("dependencies")
    val dependencies: Array<Dependency>,

    @SerialName("exposeAsAlternative")
    val exposeAsAlternative: Boolean? = null,

    @SerialName("parentProjectFileId")
    val parentProjectFileId: Int? = null,

    @SerialName("alternateFileId")
    val alternateFileId: Int? = null,

    @SerialName("isServerPack")
    val isServerPack: Boolean? = null,

    @SerialName("serverPackFileId")
    val serverPackFileId: Int? = null,

    @SerialName("isEarlyAccessContent")
    val isEarlyAccessContent: Boolean? = null,

    @SerialName("earlyAccessEndDate")
    val earlyAccessEndDate: String? = null,

    @SerialName("fileFingerprint")
    val fileFingerprint: Long,

    @SerialName("modules")
    val modules: Array<Module>? = null
) : PlatformVersion {
    @Serializable
    class Hash(
        @SerialName("value")
        val value: String,
        @SerialName("algo")
        val algo: Algo
    ) {
        @Serializable(with = Algo.Serializer::class)
        enum class Algo(val code: Int) {
            SHA1(1),
            MD5(2);

            companion object {
                private val map = entries.associateBy { it.code }
                fun fromCode(code: Int): Algo = map[code] ?: error("Unknown algo code: $code")
            }

            object Serializer : KSerializer<Algo> {
                override val descriptor: SerialDescriptor =
                    PrimitiveSerialDescriptor("Algo", PrimitiveKind.INT)

                override fun deserialize(decoder: Decoder): Algo {
                    val code = decoder.decodeInt()
                    return Algo.fromCode(code)
                }

                override fun serialize(encoder: Encoder, value: Algo) {
                    encoder.encodeInt(value.code)
                }
            }
        }
    }

    @Serializable
    class Dependency(
        @SerialName("modId")
        val modId: Int,
        @SerialName("relationType")
        val relationType: PlatformDependencyType
    )

    @Serializable
    class Module(
        @SerialName("name")
        val name: String,
        @SerialName("fingerprint")
        val fingerprint: Long
    )

    /**
     * 该版本的主要文件
     */
    @Transient
    private lateinit var thisPrimaryFile: CurseForgeFile

    /**
     * 主要文件下载链接
     */
    @Transient
    private lateinit var primaryDownloadUrl: String

    override suspend fun initFile(currentProjectId: String): Boolean {
        val file = this.takeIf { it.fileName != null && it.fixedFileUrl() != null }
        // 文件名或者下载链接为空
        // 单独获取该文件信息
            ?: run {
                val fileId = id.toString()
                runCatching {
                    mirroredPlatformSearcher(
                        searchers = mirroredCurseForgeSource()
                    ) { searcher ->
                        searcher.getVersion(
                            projectID = currentProjectId,
                            fileID = fileId
                        )
                    }.data
                }.onFailure { e ->
                    when (e) {
                        is FileNotFoundException -> Logger.warning("InitCFFile", "Could not query api.curseforge.com for deleted mods: $currentProjectId, $fileId", e)
                        is IOException, is SerializationException -> Logger.warning("InitCFFile", "Unable to fetch the file name projectID=$currentProjectId, fileID=$fileId", e)
                    }
                }.getOrNull() ?: return false
            }
        val link = file.fixedFileUrl() ?: run {
            Logger.warning("InitCFFile", "No download link available, projectID=$currentProjectId, fileID=${file.id}")
            return false
        }

        thisPrimaryFile = file
        primaryDownloadUrl = link
        return true
    }

    override fun platform(): Platform = Platform.CURSEFORGE

    override fun platformId(): String = id.toString()

    override fun platformProjectId(): String = modId.toString()

    override fun platformDisplayName(): String = thisPrimaryFile.displayName

    override fun platformFileName(): String = thisPrimaryFile.fileName!!

    override fun platformGameVersion(): Array<String> {
        return thisPrimaryFile.gameVersions.filter { gameVersion ->
            filterRelease(gameVersion)
        }.toTypedArray()
    }

    override fun platformLoaders(): List<PlatformDisplayLabel> {
        return thisPrimaryFile.gameVersions.mapNotNull { loaderName ->
            CurseForgeModLoader.entries.find {
                it.getDisplayName().equals(loaderName, true)
            }
        }
    }

    override fun platformReleaseType(): PlatformReleaseType = thisPrimaryFile.releaseType

    override fun platformDependencies(): List<PlatformVersion.PlatformDependency> {
        return thisPrimaryFile.dependencies.map { dependency ->
            PlatformVersion.PlatformDependency(
                platform = platform(),
                projectId = dependency.modId.toString(),
                type = dependency.relationType
            )
        }
    }

    override fun platformDownloadCount(): Long = thisPrimaryFile.downloadCount

    override fun platformDownloadUrl(): String = primaryDownloadUrl

    override fun platformDatePublished(): Instant = parseInstant(thisPrimaryFile.fileDate)

    override fun platformSha1(): String? = thisPrimaryFile.getSHA1()

    override fun platformFileSize(): Long = thisPrimaryFile.fileLength

    override fun platformVersion(): String = displayName
}

/**
 * 获取修正后的文件下载链接，若下载链接为null，则根据id与文件名称计算下载链接
 */
fun CurseForgeFile.fixedFileUrl(): String? {
    return downloadUrl
        ?: if (fileName != null) {
            "https://edge.forgecdn.net/files/${id / 1000}/${id % 1000}/${fileName}"
        } else {
            null
        }
}

/**
 * 获取Sha1值
 */
fun CurseForgeFile.getSHA1(): String? {
    return hashes.find { hash ->
        hash.algo == Hash.Algo.SHA1
    }?.value
}