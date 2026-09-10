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

package com.movtery.zalithlauncher.notification

import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import com.movtery.zalithlauncher.R

enum class NotificationChannelData(
    val channelId: String,
    val channelName: (Context) -> String,
    val channelDescription: ((Context) -> String)?,
    val level: Int,
    val showBadge: Boolean = true
) {
    /**
     * Jvm 任务服务
     */
    JVM_SERVICE_CHANNEL("jvm.service", { it.getString(R.string.notification_data_jvm_service_name) }, null, IMPORTANCE_LOW),

    /**
     * JVM 保活服务
     */
    GAME_SERVICE_CHANNEL("game.service", { it.getString(R.string.notification_jvm_running_name) }, null, IMPORTANCE_LOW),

    /**
     * 任务保活服务
     */
    TASK_SERVICE_CHANNEL("task.service", { it.getString(R.string.notification_task_service_name) }, null, IMPORTANCE_LOW),

    /**
     * 陶瓦联机 VPN 状态显示服务
     */
    TERRACOTTA_VPN_CHANNEL("terracotta_vpn_channel", { "Terracotta VPN" }, { it.getString(R.string.terracotta_terracotta) }, IMPORTANCE_LOW, false)
}