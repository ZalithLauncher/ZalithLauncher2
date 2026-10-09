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

package com.movtery.zalithlauncher.ui.screens.content.home.server

import com.movtery.zalithlauncher.game.version.multiplayer.ServerPingResult
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardDir
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardStatus

/** 快速启动使用的游戏版本模式 */
enum class ServerCardVersionMode {
    /** 启动时使用当前选中的版本 */
    CURRENT,
    /** 启动时使用记录绑定的特定版本 */
    SPECIFIC
}

/**
 * 服务器卡片的持久化记录
 * @param cardId 卡片 id，即服务器 IP 字符串
 * @param name 服务器名称，添加时自服务器列表条目复制，此后卡片自治
 * @param icon 服务器图标缓存（ping 得到的 favicon）
 * @param quickLaunchEnabled 是否在卡片上显示快速启动按钮
 * @param versionMode 快速启动的版本模式
 * @param dir 绑定版本所在的游戏目录，仅 SPECIFIC 模式使用
 * @param versionName 绑定的版本名，仅 SPECIFIC 模式使用
 */
data class ServerCardRecord(
    val cardId: String,
    val name: String,
    val icon: ByteArray? = null,
    val quickLaunchEnabled: Boolean = true,
    val versionMode: ServerCardVersionMode = ServerCardVersionMode.CURRENT,
    val dir: VersionCardDir? = null,
    val versionName: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerCardRecord

        return cardId == other.cardId &&
                name == other.name &&
                icon.contentEquals(other.icon) &&
                quickLaunchEnabled == other.quickLaunchEnabled &&
                versionMode == other.versionMode &&
                dir == other.dir &&
                versionName == other.versionName
    }

    override fun hashCode(): Int {
        var result = cardId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        result = 31 * result + quickLaunchEnabled.hashCode()
        result = 31 * result + versionMode.hashCode()
        result = 31 * result + (dir?.hashCode() ?: 0)
        result = 31 * result + (versionName?.hashCode() ?: 0)
        return result
    }
}

/** 服务器卡片的 ping 状态；Loading 与 Failed 仅在尚无可展示数据时出现 */
sealed interface ServerCardPingStatus {
    /** 尚未完成首次 ping */
    data object Loading : ServerCardPingStatus
    /** ping 成功 */
    data class Loaded(val result: ServerPingResult) : ServerCardPingStatus
    /** 从未成功连接 */
    data object Failed : ServerCardPingStatus
}

/** 服务器卡片的完整状态 */
data class ServerCardState(
    val record: ServerCardRecord,
    val ping: ServerCardPingStatus,
    /** 绑定版本的可用性状态，仅 SPECIFIC 模式非空 */
    val versionStatus: VersionCardStatus? = null
)
