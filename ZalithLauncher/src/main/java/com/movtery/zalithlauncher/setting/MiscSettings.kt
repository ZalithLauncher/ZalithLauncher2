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

package com.movtery.zalithlauncher.setting

import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByEnum

object MiscSettings : SettingsRegistry() {
    /**
     * 当前选择的账号
     */
    val currentAccount = stringSetting("currentAccount", "")

    /**
     * 当前选择的游戏目录id
     */
    val currentGamePathId = stringSetting("currentGamePathId", GamePathManager.DEFAULT_ID)

    /**
     * 玩家结束运行游戏的次数
     */
    val finishedGame = intSetting("finishedGame", 0)

    /**
     * 是否在打开启动器时，根据特定的运行游戏次数，显示赞助支持弹窗
     */
    val showSponsorship = boolSetting("showSponsorship", true)

    /**
     * 是否启用陶瓦联机
     */
    val enableTerracotta = boolSetting("enableTerracotta", false)

    /**
     * 是否使用自定义 EasyTier 服务器节点
     */
    val enableTerracottaNodes = boolSetting("enableTerracottaNodes", false)

    /**
     * 陶瓦联机：自定义 EasyTier 服务器节点
     */
    val terracottaNodes = stringSetting("terracottaNodes", "")

    /**
     * 搜索模组的初始搜索平台
     */
    val searchModPlatform = enumSetting("searchModPlatform", Platform.CURSEFORGE)

    /**
     * 搜索整合包的初始搜索平台
     */
    val searchModpackPlatform = enumSetting("searchModpackPlatform", Platform.CURSEFORGE)

    /**
     * 搜索资源包的初始搜索平台
     */
    val searchResourcePackPlatform = enumSetting("searchResourcePackPlatform", Platform.CURSEFORGE)

    /**
     * 搜索光影的初始搜索平台
     */
    val searchShadersPlatform = enumSetting("searchShadersPlatform", Platform.CURSEFORGE)

    /**
     * 模组管理屏幕的排序方式
     */
    val modsSortBy = enumSetting("modsSortBy", SortByEnum.FileName)

    /**
     * 模组管理屏幕是否升序排列
     */
    val modsSortAscending = boolSetting("modsSortAscending", true)

    /**
     * 存档管理屏幕的排序方式
     */
    val savesSortBy = enumSetting("savesSortBy", SortByEnum.Name)

    /**
     * 存档管理屏幕是否升序排列
     */
    val savesSortAscending = boolSetting("savesSortAscending", true)

    /**
     * 资源包管理屏幕的排序方式
     */
    val resourcePackSortBy = enumSetting("resourcePackSortBy", SortByEnum.Name)

    /**
     * 资源包管理屏幕是否升序排列
     */
    val resourcePackSortAscending = boolSetting("resourcePackSortAscending", true)

    /**
     * 光影包管理屏幕的排序方式
     */
    val shadersSortBy = enumSetting("shadersSortBy", SortByEnum.FileName)

    /**
     * 光影包管理屏幕是否升序排列
     */
    val shadersSortAscending = boolSetting("shadersSortAscending", true)

    /**
     * 截图管理屏幕的排序方式
     */
    val screenshotsSortBy = enumSetting("screenshotsSortBy", SortByEnum.Name)

    /**
     * 截图管理屏幕是否升序排列
     */
    val screenshotsSortAscending = boolSetting("screenshotsSortAscending", false)
}
