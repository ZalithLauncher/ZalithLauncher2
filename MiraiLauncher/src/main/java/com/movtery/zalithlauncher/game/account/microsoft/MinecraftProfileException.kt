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

package com.movtery.zalithlauncher.game.account.microsoft

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.BLOCKED_IP
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.FREQUENT
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.PROFILE_NOT_EXISTS
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText

/**
 * Minecraft 配置获取异常
 */
class MinecraftProfileException(val status: ExceptionStatus) : RuntimeException() {
    enum class ExceptionStatus {
        /**
         * 登陆过于频繁
         */
        FREQUENT,

        /**
         * IP 地址被禁止
         */
        BLOCKED_IP,

        /**
         * 未创建配置
         */
        PROFILE_NOT_EXISTS
    }
}

fun MinecraftProfileException.toLocal(): AndroidStringText {
    return androidText(
        when (status) {
            FREQUENT -> R.string.account_logging_frequent
            BLOCKED_IP -> R.string.account_logging_blocked_ip
            PROFILE_NOT_EXISTS -> R.string.account_logging_profile_not_exists
        }
    )
}