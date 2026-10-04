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

package com.movtery.zalithlauncher.bridge

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.annotation.Keep
import androidx.core.net.toUri
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.utils.killProgress
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.openLink
import java.io.File

private const val TAG = "ZLNativeInvoker"

@Keep
object ZLNativeInvoker {
    @JvmStatic
    var staticLauncher: Launcher? = null

    @Keep
    @JvmStatic
    fun openLink(link: String) {
        (GlobalContext as? Activity)?.let { activity ->
            Logger.info(TAG, "collect link: $link")
            activity.runOnUiThread {
                if (link.startsWith("file:")) {
                    val newLink = formatFilePath(link) ?: return@runOnUiThread
                    Logger.info(TAG, "open link: $newLink")

                    val file = File(newLink)
                    if (link.endsWith('/')) {
                        //可能是一个目录，创建并发起浏览目录请求
                        file.mkdirs()
                        staticLauncher?.openPath(file)
                    } else {
                        shareFile(activity, file)
                        Logger.info(TAG, "In-game Share File: ${file.absolutePath}")
                    }
                } else {
                    activity.openLink(link, "*/*")
                }
            }
        }
    }

    /**
     * 格式化文件路径
     */
    private fun formatFilePath(input: String): String? {
        return try {
            val uri = input.toUri()
            if (uri.scheme == "file") {
                uri.path
            } else {
                null
            }
        } catch (_: Exception) {
            when {
                input.startsWith("file:") -> {
                    input
                        .replace(Regex("^file:/+"), "/")
                        .replace("%20", " ")
                }
                else -> null
            }
        }
    }

    @Keep
    @JvmStatic
    fun querySystemClipboard() {
        (GlobalContext as? Activity)?.let { activity ->
            activity.runOnUiThread {
                val clipData = (activity.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager)?.primaryClip ?: run {
                    ZLBridge.clipboardReceived(null, null)
                    return@runOnUiThread
                }
                val clipItemText = clipData.getItemAt(0).text ?: run {
                    ZLBridge.clipboardReceived(null, null)
                    return@runOnUiThread
                }
                ZLBridge.clipboardReceived(clipItemText.toString(), "plain")
            }
        }
    }

    @Keep
    @JvmStatic
    fun putClipboardData(data: String, mimeType: String) {
        (GlobalContext as? Activity)?.let { activity ->
            activity.runOnUiThread {
                val clipData = when (mimeType) {
                    "text/plain" -> ClipData.newPlainText(BuildKeys.LAUNCHER_IDENTIFIER, data)
                    "text/html" -> ClipData.newHtmlText(BuildKeys.LAUNCHER_IDENTIFIER, data, data)
                    else -> null
                }
                clipData?.let {
                    (activity.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(it)
                }
            }
        }
    }

    @Keep
    @JvmStatic
    fun jvmExit(exitCode: Int, isSignal: Boolean) {
        staticLauncher?.exit()
        staticLauncher?.onExit?.invoke(exitCode, isSignal)
        staticLauncher = null
        killProgress()
    }
}