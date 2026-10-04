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
package com.movtery.zalithlauncher.game.download.modpack.platform.multimc

import java.io.File
import java.util.Optional
import java.util.Properties

/**
 * MultiMC 整合包的实例配置
 * 数据结构参考：[HMCL](https://github.com/HMCL-dev/HMCL/blob/bb3d03f/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/multimc/MultiMCInstanceConfiguration.java)
 * @param name 实例名称
 * @param gameVersion 实例的游戏版本
 * @param permGen JVM 永久代内存大小
 * @param wrapperCommand 用于启动 JVM 的命令
 * @param preLaunchCommand 游戏启动前执行的命令
 * @param postExitCommand 游戏退出后执行的命令
 * @param notes 实例描述信息
 * @param javaPath JVM 安装路径
 * @param jvmArgs JVM 启动参数
 * @param isFullscreen 是否以全屏模式启动 Minecraft
 * @param width 游戏窗口的初始宽度
 * @param height 游戏窗口的初始高度
 * @param maxMemory JVM 可分配的最大内存
 * @param minMemory JVM 可分配的最小内存
 * @param joinServerOnLaunch 在启动游戏时自动加入服务器
 * @param isShowConsole 游戏启动时是否显示控制台窗口
 * @param isShowConsoleOnError 游戏崩溃时是否显示控制台窗口
 * @param isAutoCloseConsole 游戏停止时是否自动关闭控制台窗口
 * @param isOverrideMemory 是否强制应用 [maxMemory]、[minMemory]、[permGen] 的内存设置
 * @param isOverrideJavaLocation 是否强制应用 [javaPath] 的 Java 路径设置
 * @param isOverrideJavaArgs 是否强制应用 [jvmArgs] 的 JVM 参数设置
 * @param isOverrideConsole 是否强制应用 [isShowConsole]、[isShowConsoleOnError]、[isAutoCloseConsole] 的控制台设置
 * @param isOverrideCommands 是否强制应用 [preLaunchCommand]、[postExitCommand]、[wrapperCommand] 的命令设置
 * @param isOverrideWindow 是否强制应用 [height]、[width]、[isFullscreen] 的窗口设置
 */
data class MultiMCConfiguration(
    val instanceType: String?, // InstanceType
    val name: String?, // name
    val gameVersion: String?, // IntendedVersion
    val permGen: Int?, // PermGen
    val wrapperCommand: String?, // WrapperCommand
    val preLaunchCommand: String?, // PreLaunchCommand
    val postExitCommand: String?, // PostExitCommand
    val notes: String?, // notes
    val javaPath: String?, // JavaPath
    val jvmArgs: String?, // JvmArgs
    val isFullscreen: Boolean, // LaunchMaximized
    val width: Int?, // MinecraftWinWidth
    val height: Int?, // MinecraftWinHeight
    val maxMemory: Int?, // MaxMemAlloc
    val minMemory: Int?, // MinMemAlloc
    val joinServerOnLaunch: String?, // JoinServerOnLaunchAddress
    val isShowConsole: Boolean, // ShowConsole
    val isShowConsoleOnError: Boolean, // ShowConsoleOnError
    val isAutoCloseConsole: Boolean, // AutoCloseConsole
    val isOverrideMemory: Boolean, // OverrideMemory
    val isOverrideJavaLocation: Boolean, // OverrideJavaLocation
    val isOverrideJavaArgs: Boolean, // OverrideJavaArgs
    val isOverrideConsole: Boolean, // OverrideConsole
    val isOverrideCommands: Boolean, // OverrideCommands
    val isOverrideWindow: Boolean, // OverrideWindow
    val iconKey: String?
) {
    /**
     * @param instanceName 实例的名称
     * @param gameVersion Minecraft 游戏本体版本
     */
    constructor(
        properties: Properties,
        instanceName: String? = null,
        gameVersion: String? = null
    ): this(
        instanceType = readValue(properties, "InstanceType"),
        isAutoCloseConsole = readValue(properties, "AutoCloseConsole").toBoolean(),
        gameVersion = gameVersion ?: readValue(properties, "IntendedVersion"),
        javaPath = readValue(properties, "JavaPath"),
        jvmArgs = readValue(properties, "JvmArgs"),
        isFullscreen = readValue(properties, "LaunchMaximized").toBoolean(),
        maxMemory = readValue(properties, "MaxMemAlloc")?.toIntOrNull(),
        minMemory = readValue(properties, "MinMemAlloc")?.toIntOrNull(),
        joinServerOnLaunch = readValue(properties, "JoinServerOnLaunchAddress"),
        height = readValue(properties, "MinecraftWinHeight")?.toIntOrNull(),
        width = readValue(properties, "MinecraftWinWidth")?.toIntOrNull(),
        isOverrideCommands = readValue(properties, "OverrideCommands").toBoolean(),
        isOverrideConsole = readValue(properties, "OverrideConsole").toBoolean(),
        isOverrideJavaArgs = readValue(properties, "OverrideJavaArgs").toBoolean(),
        isOverrideJavaLocation = readValue(properties, "OverrideJavaLocation").toBoolean(),
        isOverrideMemory = readValue(properties, "OverrideMemory").toBoolean(),
        isOverrideWindow = readValue(properties, "OverrideWindow").toBoolean(),
        permGen = readValue(properties, "PermGen")?.toIntOrNull(),
        postExitCommand = readValue(properties, "PostExitCommand"),
        preLaunchCommand = readValue(properties, "PreLaunchCommand"),
        isShowConsole = readValue(properties, "ShowConsole").toBoolean(),
        isShowConsoleOnError = readValue(properties, "ShowConsoleOnError").toBoolean(),
        wrapperCommand = readValue(properties, "WrapperCommand"),
        name = instanceName ?: readValue(properties, "name"),
        notes = Optional.ofNullable<String?>(readValue(properties, "notes")).orElse(""),
        iconKey = readValue(properties, "iconKey")
    )

    fun toProperties(): Properties {
        val p = Properties()
        if (instanceType != null) p.setProperty("InstanceType", instanceType)
        p.setProperty("AutoCloseConsole", isAutoCloseConsole.toString())
        if (gameVersion != null) p.setProperty("IntendedVersion", gameVersion)
        if (javaPath != null) p.setProperty("JavaPath", javaPath)
        if (jvmArgs != null) p.setProperty("JvmArgs", jvmArgs)
        p.setProperty("LaunchMaximized", isFullscreen.toString())
        if (maxMemory != null) p.setProperty("MaxMemAlloc", maxMemory.toString())
        if (minMemory != null) p.setProperty("MinMemAlloc", minMemory.toString())
        if (height != null) p.setProperty("MinecraftWinHeight", height.toString())
        if (width != null) p.setProperty("MinecraftWinWidth", width.toString())
        p.setProperty("OverrideCommands", isOverrideCommands.toString())
        p.setProperty("OverrideConsole", isOverrideConsole.toString())
        p.setProperty("OverrideJavaArgs", isOverrideJavaArgs.toString())
        p.setProperty("OverrideJavaLocation", isOverrideJavaLocation.toString())
        p.setProperty("OverrideMemory", isOverrideMemory.toString())
        p.setProperty("OverrideWindow", isOverrideWindow.toString())
        if (permGen != null) p.setProperty("PermGen", permGen.toString())
        if (postExitCommand != null) p.setProperty("PostExitCommand", postExitCommand)
        if (preLaunchCommand != null) p.setProperty("PreLaunchCommand", preLaunchCommand)
        p.setProperty("ShowConsole", isShowConsole.toString())
        p.setProperty("ShowConsoleOnError", isShowConsoleOnError.toString())
        if (wrapperCommand != null) p.setProperty("WrapperCommand", wrapperCommand)
        if (name != null) p.setProperty("name", name)
        if (notes != null) p.setProperty("notes", notes)
        if (iconKey != null) p.setProperty("iconKey", iconKey)
        return p
    }
}

private fun readValue(properties: Properties, key: String?): String? {
    val value = properties.getProperty(key) ?: return null

    val l = value.length
    if (l >= 2 && value[0] == '"' && value[l - 1] == ':') {
        return value.take(l - 1)
    }
    return value
}

/**
 * 在整合包中寻找实例配置文件，并尝试解析
 */
fun loadMMCConfigFromPack(root: File): MultiMCConfiguration? {
    //配置文件
    val configuration = File(root, "instance.cfg")
    return if (configuration.exists() && configuration.isFile) {
        val properties = Properties()
        configuration.reader(Charsets.UTF_8).use { isr ->
            properties.load(isr)
        }
        MultiMCConfiguration(properties = properties)
    } else {
        null
    }
}