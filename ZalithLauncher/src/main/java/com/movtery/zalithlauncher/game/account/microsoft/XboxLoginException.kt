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
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.BANNED
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.BLOCKED_REGION
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.NOT_ACCEPTED_SERVICE
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.REACHED_PLAYTIME_LIMIT
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.REQUIRES_PROOF_OF_AGE
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.RESTRICTED
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.UNDERAGE
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.UNREGISTERED
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText

/**
 * Xbox 登陆出现的各种异常
 */
class XboxLoginException(val status: ExceptionStatus) : RuntimeException() {
    enum class ExceptionStatus {
        /**
         * 被封禁
         */
        BANNED,

        /**
         * 受到限制
         */
        RESTRICTED,

        /**
         * 未创建个人资料
         */
        UNREGISTERED,

        /**
         * 未同意服务条款
         */
        NOT_ACCEPTED_SERVICE,

        /**
         * 地区被禁止登陆
         */
        BLOCKED_REGION,

        /**
         * 未提供年龄证明
         */
        REQUIRES_PROOF_OF_AGE,

        /**
         * 达到游玩时间限制
         */
        REACHED_PLAYTIME_LIMIT,

        /**
         * 未成年
         */
        UNDERAGE
    }
}

fun XboxLoginException.toLocal(): AndroidStringText {
    return androidText(
        when(status) {
            BANNED -> R.string.account_logging_xbox_banned
            RESTRICTED -> R.string.account_logging_xbox_restricted
            UNREGISTERED -> R.string.account_logging_xbox_unregistered
            NOT_ACCEPTED_SERVICE -> R.string.account_logging_xbox_not_accepted_service
            BLOCKED_REGION -> R.string.account_logging_xbox_blocked_region
            REQUIRES_PROOF_OF_AGE -> R.string.account_logging_xbox_requires_proof_of_age
            REACHED_PLAYTIME_LIMIT -> R.string.account_logging_xbox_reached_playtime_limit
            UNDERAGE -> R.string.account_logging_xbox_underage
        }
    )
}