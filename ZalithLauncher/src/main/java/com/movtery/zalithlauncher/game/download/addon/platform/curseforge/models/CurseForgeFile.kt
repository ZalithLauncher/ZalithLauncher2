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

package com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models

import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeFile.Hash
import com.movtery.zalithlauncher.game.download.addon.platform.mirroredCurseForgeSource
import com.movtery.zalithlauncher.game.download.addon.platform.mirroredPlatformSearcher
import com.movtery.zalithlauncher.game.versioninfo.filterRelease
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.parseInstant
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
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
import java.net.URLEncoder
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

    override suspend fun initFile(currentProjectId: String): Boolean =
        initFileInternal(currentProjectId, strict = false)

    override suspend fun initFileStrict(currentProjectId: String): Boolean =
        initFileInternal(currentProjectId, strict = true)

    private suspend fun initFileInternal(currentProjectId: String, strict: Boolean): Boolean {
        val file = this.takeIf { it.hasUsablePrimaryMetadata() }
        // 文件名或者下载链接为空
        // 单独获取该文件信息
            ?: run {
                val fileId = id.toString()
                if (strict) {
                    try {
                        queryPrimaryFile(currentProjectId, fileId)
                    } catch (e: FileNotFoundException) {
                        Logger.warning("InitCFFile", "Could not query api.curseforge.com for deleted mods: $currentProjectId, $fileId", e)
                        return false
                    } catch (e: ClientRequestException) {
                        // Ktor 将 HTTP 404 抛为该异常，仅 404 可按永久不可用处理
                        if (e.response.status != HttpStatusCode.NotFound) throw e
                        Logger.warning("InitCFFile", "Could not query api.curseforge.com for deleted mods: $currentProjectId, $fileId", e)
                        return false
                    }
                } else {
                    runCatching {
                        queryPrimaryFile(currentProjectId, fileId)
                    }.onFailure { e ->
                        when (e) {
                            is CancellationException -> throw e
                            is FileNotFoundException -> Logger.warning("InitCFFile", "Could not query api.curseforge.com for deleted mods: $currentProjectId, $fileId", e)
                            is IOException, is SerializationException -> Logger.warning("InitCFFile", "Unable to fetch the file name projectID=$currentProjectId, fileID=$fileId", e)
                        }
                    }.getOrNull() ?: return false
                }
            }
        if (!file.hasUsablePrimaryMetadata()) {
            Logger.warning("InitCFFile", "No usable file name or download link, projectID=$currentProjectId, fileID=${file.id}")
            return false
        }
        val link = checkNotNull(file.fixedFileUrl())

        thisPrimaryFile = file
        primaryDownloadUrl = link
        return true
    }

    private suspend fun queryPrimaryFile(currentProjectId: String, fileId: String): CurseForgeFile {
        return mirroredPlatformSearcher(
            searchers = mirroredCurseForgeSource()
        ) { searcher ->
            searcher.getVersion(
                projectID = currentProjectId,
                fileID = fileId
            )
        }.data
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

    override fun platformLoadersStrict(): List<PlatformDisplayLabel> {
        // Any 的显示名为空，严格模式必须按 API 标识解析，才能保留明确的通配语义。
        return thisPrimaryFile.gameVersions.mapNotNull { loaderName ->
            CurseForgeModLoader.entries.find {
                it.apiName.equals(loaderName, ignoreCase = true)
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

/** 文件名非空白且存在下载地址（包括可推导的 CDN 地址）时，无需补全或重新查询。 */
internal fun CurseForgeFile.hasUsablePrimaryMetadata(): Boolean =
    !fileName.isNullOrBlank() && fixedFileUrl() != null

/**
 * 获取修正后的文件下载链接，若下载链接为null，则根据id与文件名称计算下载链接
 */
fun CurseForgeFile.fixedFileUrl(): String? {
    return downloadUrl
        ?: fileName?.let { curseForgeCDNUrl(id, it) }
}

/**
 * 根据 CurseForge 的文件 ID 与文件名称拼出 CDN 下载链接
 */
fun curseForgeCDNUrl(fileId: Int, fileName: String): String {
    val encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
    return "https://edge.forgecdn.net/files/${fileId / 1000}/${fileId % 1000}/$encoded"
}

/**
 * 获取Sha1值
 */
fun CurseForgeFile.getSHA1(): String? {
    return hashes.find { hash ->
        hash.algo == Hash.Algo.SHA1
    }?.value
}