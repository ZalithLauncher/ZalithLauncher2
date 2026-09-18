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

package com.movtery.zalithlauncher.game.version.export

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import java.io.File

/**
 * 导出整合包时需要的所有信息
 * @param name 用户指定要导出的整合包名称
 * @param summary 用户指定的整合包描述
 * @param author 整合包作者名称
 * @param version 用户指定的整合包版本
 * @param mcVersion Minecraft 版本
 * @param loader 该版本所加载的模组加载器
 * @param selectedFiles 用户选定的要导出的文件
 * @param minMemory 用户指定的整合包最小内存大小
 * @param maxMemory 用户指定的整合包最大内存大小
 * @param jvmArgs 游戏参数
 * @param javaArgs Java虚拟机参数
 * @param fileApi 整合包下载链接前缀
 * @param url 整合包官方网站
 * @param forceUpdate 强制更新整合包
 * @param packType 导出整合包的类型
 * @param packModrinth 是否打包Modrinth的远程资源
 * @param packCurseForge 是否打包CurseForge的远程资源
 */
data class ExportInfo(
    val gamePath: File,
    val name: String = "",
    val summary: String? = null,
    val author: String = "",
    val version: String = "",
    val mcVersion: String = "",
    val loader: LoaderVersion? = null,
    val selectedFiles: List<File> = emptyList(),
    val minMemory: Int = 0,
    val maxMemory: Int = 0,
    val jvmArgs: String = "",
    val javaArgs: String = "",
    val fileApi: String? = null,
    val url: String = "",
    val forceUpdate: Boolean = false,
    val packType: PackType = PackType.Modrinth,
    val packModrinth: Boolean = false,
    val packCurseForge: Boolean = false
) {
    /**
     * 模组加载器信息
     * @param version 加载器版本
     */
    data class LoaderVersion(
        val loader: ModLoader,
        val version: String
    )
}
