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

package com.movtery.zalithlauncher.game.launch

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.compose.ui.unit.IntSize
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.activities.runJar
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

open class JvmLauncher(
    private val context: Context,
    private val jvmLaunchInfo: JvmLaunchInfo,
    onExit: (code: Int, isSignal: Boolean) -> Unit,
    openPath: (folder: File) -> Unit
) : Launcher(onExit, openPath) {
    override fun exit() {

    }

    override suspend fun launch(screenSize: IntSize): Int {
        generateLauncherProfiles(jvmLaunchInfo.userHome)
        val (runtime, argList) = getStartupNeeded(screenSize)

        this.runtime = runtime

        return launchJvm(
            context = context,
            jvmArgs = argList,
            userHome = jvmLaunchInfo.userHome ?: GamePathManager.getUserPath(),
            userArgs = if (jvmLaunchInfo.useUserJvm) {
                AllSettings.jvmArgs.getValue()
            } else {
                ""
            },
            screenSize = screenSize,
            useLocalLanguage = false
        )
    }

    override fun chdir(): String {
        return getGameHome()
    }

    override fun getLogFile(): File = File(
        PathManager.DIR_FILES_EXTERNAL, LogName.JVM.fileName
    )

    private fun getStartupNeeded(
        screenSize: IntSize
    ): Pair<Runtime, List<String>> {
        val args = jvmLaunchInfo.jvmArgs.splitPreservingQuotes()

        val runtime = jvmLaunchInfo.jreName?.let { jreName ->
            RuntimesManager.forceReload(jreName)
        } ?: run {
            RuntimesManager.forceReload(AllSettings.javaRuntime.getValue())
        }

        val argList: MutableList<String> = ArrayList(
            getCacioJavaArgs(screenSize, runtime.javaVersion == 8)
        ).apply {
            addAll(args)
        }

        LoggerBridge.appendTitle("Launch JVM")
        LoggerBridge.append("Info: Java arguments: \r\n${argList.joinToString("\r\n")}")

        return Pair(runtime, argList)
    }
}

fun executeJarWithUri(activity: Activity, uri: Uri, jreName: String? = null) {
    runCatching {
        val cacheFile = File(PathManager.DIR_CACHE, "temp-jar.jar")
        activity.contentResolver.openInputStream(uri)?.use { contentStream ->
            FileOutputStream(cacheFile).use { fileOutputStream ->
                contentStream.copyTo(fileOutputStream)
            }
            runJar(activity, cacheFile, jreName, null)
        } ?: throw IOException("Failed to open content stream")
    }.onFailure { e ->
        finalErrorDialog(activity, e.getMessageOrToString())
    }
}

private fun finalErrorDialog(
    activity: Activity,
    error: String,
    onDismiss: () -> Unit = {}
) {
    activity.runOnUiThread {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.generic_error)
            .setMessage(error)
            .setCancelable(false)
            .setPositiveButton(R.string.generic_confirm) { dialog, _ ->
                dialog.dismiss()
                onDismiss()
            }
            .show()
    }
}

private val DEFAULT_LAUNCHER_PROFILES = """{"profiles":{"default":{"lastVersionId":"latest-release"}},"selectedProfile":"default"}""".trimIndent()

/**
 * 写入一个默认的 launcher_profiles.json 文件，不存在将会导致 Forge、NeoForge 等无法正常安装
 */
private fun generateLauncherProfiles(userHome: String?) {
    runCatching {
        File(userHome?.let { "$it/.minecraft" } ?: GamePathManager.currentPath.value, "launcher_profiles.json").run {
            if (!exists()) {
                if (parentFile?.exists() == false) parentFile?.mkdirs()
                if (!createNewFile()) throw IOException("Failed to create launcher_profiles.json file!")
                writeText(DEFAULT_LAUNCHER_PROFILES)
                lInfo("The content has already been written! File Location: $absolutePath")
            }
        }
    }.onFailure {
        lWarning("Unable to generate launcher_profiles.json file!", it)
    }
}