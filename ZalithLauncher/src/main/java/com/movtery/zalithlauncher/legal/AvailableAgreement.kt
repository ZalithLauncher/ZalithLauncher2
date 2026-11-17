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

package com.movtery.zalithlauncher.legal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 可用协议数据类
 */
@Serializable
data class AvailableAgreement(
    /** 协议版本 */
    @SerialName("version")
    val version: String,
    @SerialName("files")
    val files: List<DocumentFile>
) {
    @Serializable
    data class DocumentFile(
        /** 生效/执行地区 */
        @SerialName("effective_region")
        val effectiveRegion: String,
        /** 可显示的协议文件 */
        @SerialName("data_list")
        val dataList: List<Data>
    ) {
        @Serializable
        data class Data(
            /** 协议的显示名称/标题 */
            @SerialName("title")
            val title: String,
            /** 协议文件在assets/内的相对路径 */
            @SerialName("path")
            val path: String
        )
    }
}
