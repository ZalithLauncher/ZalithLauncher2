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

package com.movtery.zalithlauncher.upgrade

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 远端返回的最新版本的启动器的信息，用于与本地启动器版本进行检查并更新
 * @param code 最新启动器的版本号
 * @param version 最新启动器的版本名称
 * @param createdAt 发布时间
 * @param defaultCloudDrive 默认的网盘链接
 * @param cloudDrives 可用的网盘链接
 * @param files 可下载安装包文件
 * @param defaultBody 默认更新日志，当 [bodies] 中没有匹配的语言日志时使用
 * @param bodies 针对不同语言的更新日志列表
 */
@Serializable
data class RemoteData(
    @SerialName("code")
    val code: Int,
    @SerialName("version")
    val version: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("default_cloud_drive")
    val defaultCloudDrive: CloudDrive? = null,
    @SerialName("cloud_drives")
    val cloudDrives: List<CloudDrive> = emptyList(),
    @SerialName("files")
    val files: List<RemoteFile>,
    @SerialName("default_body")
    val defaultBody: RemoteBody,
    @SerialName("bodies")
    val bodies: List<RemoteBody>
) {
    /**
     * 网盘链接，按语言区分
     * @param language 语言标识
     * @param link 网盘链接
     * @param links 同时支持的所有网盘列检
     */
    @Serializable
    data class CloudDrive(
        @SerialName("language")
        val language: String,
        @SerialName("link")
        val link: String,
        @SerialName("links")
        val links: List<Link> = emptyList()
    ) {
        /**
         * 单个支持的网盘链接
         * @param name 网盘名称
         * @param link 网盘分享链接
         */
        @Serializable
        data class Link(
            @SerialName("name")
            val name: String,
            @SerialName("link")
            val link: String
        )
    }

    /**
     * 最新版本的启动器的安装包文件
     * @param fileName 可直接展示的文件名称
     * @param uri 可直接在浏览器下载的链接
     * @param arch 该安装包的架构
     * @param size 该安装包文件的大小 (bytes)
     */
    @Serializable
    data class RemoteFile(
        @SerialName("file_name")
        val fileName: String,
        @SerialName("uri")
        val uri: String,
        @SerialName("arch")
        val arch: Arch,
        @SerialName("size")
        val size: Long = 0L
    ) {
        @Serializable
        enum class Arch {
            @SerialName("all")
            ALL,
            @SerialName("arm")
            ARM,
            @SerialName("arm64")
            ARM64,
            @SerialName("x86")
            X86,
            @SerialName("x86_64")
            X86_64
        }
    }

    /**
     * 最新版本的启动器的更新日志，按语言区分
     * @param language 语言标识
     * @param markdown Markdown 内容
     */
    @Serializable
    data class RemoteBody(
        @SerialName("language")
        val language: String,
        @SerialName("markdown")
        val markdown: String
    )
}
