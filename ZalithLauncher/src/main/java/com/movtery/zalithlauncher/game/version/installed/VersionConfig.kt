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

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.support.touch_controller.VibrationHandler
import com.movtery.zalithlauncher.game.version.installed.VersionsManager.getZalithVersionPath
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.getStringNotNull
import java.io.File
import java.io.FileWriter

private const val TAG = "VersionConfig"

@Keep
class VersionConfig(
    @Transient
    private var versionPath: File
) : Parcelable {
    @SerializedName("pinned")
    var pinned: Boolean = false
        private set

    fun setPinnedAndSave(
        value: Boolean,
        applyState: (Boolean) -> Unit
    ) {
        val oldValue = pinned
        applyState(value)

        try {
            this.pinned = value
            saveWithThrowable()
        } catch (e: Exception) {
            this.pinned = oldValue
            applyState(oldValue)
            throw e
        }
    }

    @SerializedName("isolationType")
    var isolationType: SettingState = SettingState.FOLLOW_GLOBAL
        get() = getSettingStateNotNull(field)
    @SerializedName("skipGameIntegrityCheck")
    var skipGameIntegrityCheck: SettingState = SettingState.FOLLOW_GLOBAL
        get() = getSettingStateNotNull(field)
    @SerializedName("javaRuntime")
    var javaRuntime: String = ""
        get() = getStringNotNull(field)
    @SerializedName("jvmArgs")
    var jvmArgs: String = ""
        get() = getStringNotNull(field)
    @SerializedName("renderer")
    var renderer: String = ""
        get() = getStringNotNull(field)
    @SerializedName("driver")
    var driver: String = ""
        get() = getStringNotNull(field)
    @SerializedName("graphicsApi")
    var graphicsApi: GraphicsApi? = null
    @SerializedName("control")
    var control: String = ""
        get() = getStringNotNull(field)
    @SerializedName("customPath")
    var customPath: String = ""
        get() = getStringNotNull(field)
    @SerializedName("customInfo")
    var customInfo: String = ""
        get() = getStringNotNull(field)
    @SerializedName("versionSummary")
    var versionSummary: String = ""
        get() = getStringNotNull(field)
    @SerializedName("serverIp")
    var serverIp: String = ""
        get() = getStringNotNull(field)
    @SerializedName("ramAllocation")
    var ramAllocation: Int = -1
    @SerializedName("touchVibrateDuration")
    var touchVibrateDuration: Int = 100
    @SerializedName("touchVibrateKind")
    var touchVibrateKind: VibrationHandler.VibrateKind? = null

    constructor(
        filePath: File,
        isolationType: SettingState = SettingState.FOLLOW_GLOBAL,
        skipGameIntegrityCheck: SettingState = SettingState.FOLLOW_GLOBAL,
        javaRuntime: String = "",
        jvmArgs: String = "",
        renderer: String = "",
        driver: String = "",
        graphicsApi: GraphicsApi? = null,
        control: String = "",
        customPath: String = "",
        customInfo: String = "",
        versionSummary: String = "",
        serverIp: String = "",
        ramAllocation: Int = -1,
        touchVibrateDuration: Int = 100,
        touchVibrateKind: VibrationHandler.VibrateKind? = null,
    ) : this(filePath) {
        this.isolationType = isolationType
        this.skipGameIntegrityCheck = skipGameIntegrityCheck
        this.javaRuntime = javaRuntime
        this.jvmArgs = jvmArgs
        this.renderer = renderer
        this.driver = driver
        this.graphicsApi = graphicsApi
        this.control = control
        this.customPath = customPath
        this.customInfo = customInfo
        this.versionSummary = versionSummary
        this.serverIp = serverIp
        this.ramAllocation = ramAllocation
        this.touchVibrateDuration = touchVibrateDuration
        this.touchVibrateKind = touchVibrateKind
    }

    fun copy(): VersionConfig = VersionConfig(
        versionPath,
        getSettingStateNotNull(isolationType),
        getSettingStateNotNull(skipGameIntegrityCheck),
        getStringNotNull(javaRuntime),
        getStringNotNull(jvmArgs),
        getStringNotNull(renderer),
        getStringNotNull(driver),
        graphicsApi,
        getStringNotNull(control),
        getStringNotNull(customPath),
        getStringNotNull(customInfo),
        getStringNotNull(versionSummary),
        getStringNotNull(serverIp),
        ramAllocation,
        touchVibrateDuration,
        touchVibrateKind,
    )

    fun save() {
        runCatching {
            saveWithThrowable()
        }.onFailure { e ->
            Logger.error(TAG, "An exception occurred while saving the version configuration.", e)
        }
    }

    @Throws(Throwable::class)
    fun saveWithThrowable() {
        val zalithVersionPath = getZalithVersionPath(versionPath)
        val configFile = File(zalithVersionPath, "version.config")
        if (!zalithVersionPath.exists()) zalithVersionPath.mkdirs()

        FileWriter(configFile, false).use {
            val json = GSON.toJson(this)
            it.write(json)
        }
        Logger.info(TAG, "Saved version configuration: $this")
    }

    fun getVersionPath() = versionPath

    fun setVersionPath(versionPath: File) {
        this.versionPath = versionPath
    }

    fun isIsolation(): Boolean = isolationType.toBoolean(AllSettings.versionIsolation.getValue())

    fun skipGameIntegrityCheck(): Boolean = skipGameIntegrityCheck.toBoolean(AllSettings.skipGameIntegrityCheck.getValue())

    private fun SettingState.toBoolean(global: Boolean) = when(getSettingStateNotNull(this)) {
        SettingState.FOLLOW_GLOBAL -> global
        SettingState.ENABLE -> true
        SettingState.DISABLE -> false
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.run {
            writeString(versionPath.absolutePath)
            writeInt(getSettingStateNotNull(isolationType).ordinal)
            writeInt(getSettingStateNotNull(skipGameIntegrityCheck).ordinal)
            writeString(getStringNotNull(javaRuntime))
            writeString(getStringNotNull(jvmArgs))
            writeString(getStringNotNull(renderer))
            writeString(getStringNotNull(driver))
            writeInt(graphicsApi?.ordinal ?: -1)
            writeString(getStringNotNull(control))
            writeString(getStringNotNull(customPath))
            writeString(getStringNotNull(customInfo))
            writeString(getStringNotNull(versionSummary))
            writeString(getStringNotNull(serverIp))
            writeInt(ramAllocation)
            writeInt(touchVibrateDuration)
            writeInt(touchVibrateKind?.ordinal ?: -1)
        }
    }

    companion object CREATOR : Parcelable.Creator<VersionConfig> {
        override fun createFromParcel(parcel: Parcel): VersionConfig {
            val versionPath = File(parcel.readString().orEmpty())
            val isolationType = SettingState.entries.getOrNull(parcel.readInt()) ?: SettingState.FOLLOW_GLOBAL
            val skipGameIntegrityCheck = SettingState.entries.getOrNull(parcel.readInt()) ?: SettingState.FOLLOW_GLOBAL
            val javaRuntime = parcel.readString().orEmpty()
            val jvmArgs = parcel.readString().orEmpty()
            val renderer = parcel.readString().orEmpty()
            val driver = parcel.readString().orEmpty()
            val graphicsApi = GraphicsApi.entries.getOrNull(parcel.readInt())
            val control = parcel.readString().orEmpty()
            val customPath = parcel.readString().orEmpty()
            val customInfo = parcel.readString().orEmpty()
            val versionSummary = parcel.readString().orEmpty()
            val serverIp = parcel.readString().orEmpty()
            val ramAllocation = parcel.readInt()
            val touchVibrateDuration = parcel.readInt()
            val touchVibrateKind = VibrationHandler.VibrateKind.entries.getOrNull(parcel.readInt())

            return VersionConfig(
                versionPath,
                isolationType,
                skipGameIntegrityCheck,
                javaRuntime,
                jvmArgs,
                renderer,
                driver,
                graphicsApi,
                control,
                customPath,
                customInfo,
                versionSummary,
                serverIp,
                ramAllocation,
                touchVibrateDuration,
                touchVibrateKind,
            )
        }

        override fun newArray(size: Int): Array<VersionConfig?> {
            return arrayOfNulls(size)
        }

        fun parseConfig(versionPath: File): VersionConfig {
            val configFile = File(getZalithVersionPath(versionPath), "version.config")

            return runCatching getConfig@{
                when {
                    configFile.exists() -> {
                        //读取此文件的内容，并解析为VersionConfig
                        val config = GSON.fromJson(configFile.readText(), VersionConfig::class.java)
                        config.setVersionPath(versionPath)
                        config
                    }
                    else -> createNewConfig(versionPath)
                }
            }.onFailure {  e ->
                Logger.error(TAG, "An exception occurred while parsing the version configuration.", e)
            }.getOrElse {
                createNewConfig(versionPath)
            }
        }

        private fun createNewConfig(versionPath: File): VersionConfig {
            val config = VersionConfig(versionPath)
            return config.apply { save() }
        }

        fun createIsolation(versionPath: File): VersionConfig {
            val config = VersionConfig(versionPath)
            config.isolationType = SettingState.ENABLE
            return config
        }
    }
}

enum class SettingState(val textRes: Int) {
    @SerializedName("FOLLOW_GLOBAL")
    FOLLOW_GLOBAL(R.string.generic_follow_global),
    @SerializedName("ENABLE")
    ENABLE(R.string.generic_enable),
    @SerializedName("DISABLE")
    DISABLE(R.string.generic_disable)
}

enum class GraphicsApi(
    val displayName: String,
    val option: String
) {
    /** 默认使用游戏设定 */
    @SerializedName("DEFAULT")
    DEFAULT("", "\"default\""),
    /** 强制切换到OpenGL，覆盖游戏原有设定 */
    @SerializedName("OPENGL")
    OPENGL("OpenGL", "\"opengl\""),
    /** 默认切换到OpenGL，如果游戏有设定过则不覆盖 */
    @SerializedName("DEFAULT_OPENGL")
    DEFAULT_OPENGL(OPENGL.displayName, OPENGL.option),
    /** 强制切换到Vulkan，覆盖游戏原有设定 */
    @SerializedName("VULKAN")
    VULKAN("Vulkan", "\"vulkan\"")
}

private fun getSettingStateNotNull(type: SettingState?) = type ?: SettingState.FOLLOW_GLOBAL
