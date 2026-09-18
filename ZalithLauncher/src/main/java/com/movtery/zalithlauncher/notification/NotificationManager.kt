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

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationManager {
    /**
     * 初始化通知，初始化通知渠道（频道）
     */
    fun initManager(context: Context) {
        NotificationChannelData.entries.forEach { data ->
            createNotificationChannel(context, data)
        }
    }

    /**
     * 尝试检查通知权限是否开启，安卓 13 以下可能没法 100% 确定
     */
    fun checkNotificationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            //对一些魔改系统可能有效，但不能100%确定
            //所以在安卓 13 以下，尽量还是以默认不能使用通知对待
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            //SDK 33 以上有统一规范，不过实在是遇到那种傻逼系统，也没办法了说是
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_DENIED
        }
    }

    fun createNotificationChannel(context: Context, channelData: NotificationChannelData) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelData.channelId, channelData.channelName(context), channelData.level).apply {
            channelData.channelDescription?.invoke(context)?.let { desc ->
                description = desc
            }
            setShowBadge(channelData.showBadge)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * 跳转到通知设置页，出现异常则仅跳转到详细设置页
     */
    fun openNotificationSettings(context: Context) {
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
            context.startActivity(intent)
        } catch (e: Exception) {
            //如果出现异常，跳转到应用的详细设置页面
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(intent)
        }
    }
}