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

package com.movtery.zalithlauncher.game.version.installed

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonObject
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.support.touch_controller.VibrationHandler
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.getOrMin
import com.movtery.zalithlauncher.ui.screens.content.elements.QuickPlay
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.zip.ZipFile
import kotlin.math.min

private const val TAG = "Version"

/**
 * Minecraft 版本，由版本名称进行区分
 * @param versionName 版本名称
 * @param versionConfig 独立版本的配置
 * @param versionInfo 版本信息
 * @param isValid 版本的有效性
 * @param versionType 版本的类型
 */
@Keep
@Parcelize
class Version(
    private val versionName: String,
    private val versionConfig: VersionConfig,
    private val versionInfo: VersionInfo?,
    private val isValid: Boolean,
    val versionType: VersionType,
    /**
     * 控制是否将当前账号视为离线账号启动游戏
     */
    var offlineAccountLogin: Boolean = false,
    /**
     * 快速启动
     */
    var quickPlaySingle: QuickPlay? = null,
    /**
     * 启用控制代理
     */
    var enableTouchProxy: Boolean = false
): Parcelable {
    /**
     * 当前版本是否被置顶
     */
    @IgnoredOnParcel
    var pinnedState by mutableStateOf(versionConfig.pinned)
        private set

    /**
     * 设置版本的置顶状态并保存
     */
    fun setPinnedAndSave(value: Boolean) {
        this.versionConfig.setPinnedAndSave(value) { state ->
            this.pinnedState = state
        }
    }

    /**
     * @return 获取版本所属的版本文件夹
     */
    fun getVersionsFolder(): String = getVersionsHome()

    /**
     * @return 获取版本文件夹
     */
    fun getVersionPath(): File = File(getVersionsHome(), versionName)

    /**
     * @return 获取版本名称
     */
    fun getVersionName(): String = getVersionPath().name

    /**
     * @return 获取客户端 jar 文件
     */
    fun getClientJar(): File = File(getVersionPath(), "$versionName.jar")

    /**
     * @return 获取版本隔离配置
     */
    fun getVersionConfig() = versionConfig

    /**
     * @return 获取版本信息
     */
    fun getVersionInfo() = versionInfo

    /**
     * @return 版本描述是否可用
     */
    fun isSummaryValid(): Boolean {
        val summary = versionConfig.versionSummary
        return summary.isNotEmptyOrBlank()
    }

    /**
     * @return 获取版本描述
     */
    fun getVersionSummary(): String {
        if (!isValid()) throw IllegalStateException("The version is invalid!")
        return if (isSummaryValid()) versionConfig.versionSummary else versionInfo!!.getInfoString()
    }

    /**
     * @return 版本的有效性：是否存在版本JSON文件、版本文件夹是否存在
     */
    fun isValid() = isValid && getVersionPath().exists()

    /**
     * @return 是否开启了版本隔离
     */
    fun isIsolation() = versionConfig.isIsolation()

    /**
     * @return 是否跳过游戏完整性检查
     */
    fun skipGameIntegrityCheck() = versionConfig.skipGameIntegrityCheck()

    /**
     * @return 获取版本的游戏文件夹路径（若开启了版本隔离，则路径为版本文件夹）
     */
    fun getGameDir(): File {
        return if (versionConfig.isIsolation()) versionConfig.getVersionPath()
        //未开启版本隔离可以使用自定义路径，如果自定义路径为空（则为未设置），那么返回默认游戏路径（.minecraft/）
        else if (versionConfig.customPath.isNotEmpty()) File(versionConfig.customPath)
        else File(getGameHome())
    }

    private fun String.getValueOrDefault(default: String): String = this.takeIf { it.isNotEmpty() } ?: default

    fun getRenderer(): String = versionConfig.renderer.getValueOrDefault(AllSettings.renderer.getValue())

    fun getDriver(): String = versionConfig.driver.getValueOrDefault(AllSettings.vulkanDriver.getValue())

    fun getGraphicsApi(): GraphicsApi = versionConfig.graphicsApi ?: AllSettings.graphicsApi.getValue()

    fun getControlPath(): File? = versionConfig.control
        .getValueOrDefault(AllSettings.controlLayout.getValue())
        .takeIf { it.isNotEmpty() }
        ?.let { fileName -> File(PathManager.DIR_CONTROL_LAYOUTS, fileName) }

    fun getJavaRuntime(): String = versionConfig.javaRuntime

    fun getJvmArgs(): String = versionConfig.jvmArgs

    fun getCustomInfo(): String = versionConfig.customInfo.getValueOrDefault(AllSettings.versionCustomInfo.getValue())
        .replace("[zl_version]", BuildConfig.VERSION_NAME)

    fun getServerIp(): String? = versionConfig.serverIp.takeIf { it.isNotEmptyOrBlank() }

    fun getRamAllocation(context: Context = GlobalContext): Int = versionConfig.ramAllocation.takeIf { it >= 256 }?.let {
        min(it, getMaxMemoryForSettings(context))
    } ?: AllSettings.ramAllocation.getOrMin()

    fun getTouchVibrateDuration(): Int? = versionConfig.touchVibrateDuration.takeIf { it >= 80 }

    fun getTouchVibrateKind(): VibrationHandler.VibrateKind = versionConfig.touchVibrateKind ?: VibrationHandler.VibrateKind.default
}

/** 26.2-snapshot-1 */
private const val VULKAN_RUNTIME_WORLD_VERSION = 4883

/**
 * 游戏是否带有 Vulkan 后端
 */
suspend fun Version.hasVulkanBackend(): Boolean {
    return withContext(Dispatchers.IO) {
        val clientJar = getClientJar()
        if (!clientJar.exists()) return@withContext false
        runCatching {
            //在客户端中读取数据版本
            ZipFile(clientJar).use { zip ->
                val worldVersion = zip.getEntry("version.json")
                    ?.readText(zip)
                    ?.let { GSON.fromJson(it, JsonObject::class.java) }
                    //https://zh.minecraft.wiki/w/%E7%89%88%E6%9C%AC%E4%BF%A1%E6%81%AF%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F
                    ?.get("world_version")?.asInt
                worldVersion != null && worldVersion >= VULKAN_RUNTIME_WORLD_VERSION
            }
        }.onFailure { e ->
            Logger.warning(TAG, "Unable to determine the data version of this client Jar, possibly due to an outdated version.", e)
        }.getOrDefault(false)
    }
}