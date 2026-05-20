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

package com.movtery.zalithlauncher.setting

import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.movtery.layer_controller.data.POSITION_RANGE
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.enums.AppLanguage
import com.movtery.zalithlauncher.setting.enums.DarkMode
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.HomePageType
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.ui.control.HotbarRule
import com.movtery.zalithlauncher.ui.control.gamepad.JoystickMode
import com.movtery.zalithlauncher.ui.control.mouse.CENTER_HOTSPOT
import com.movtery.zalithlauncher.ui.control.mouse.CursorHotspot
import com.movtery.zalithlauncher.ui.control.mouse.LEFT_TOP_HOTSPOT
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import com.movtery.zalithlauncher.utils.animation.TransitionAnimationType

object AllSettings : SettingsRegistry() {
    //Renderer
    /**
     * 全局渲染器
     */
    val renderer = stringSetting("renderer", "")

    /**
     * Vulkan 驱动器
     */
    val vulkanDriver = stringSetting("vulkanDriver", "default turnip")

    /**
     * 图形 API（Minecraft 26.2+）
     */
    val graphicsApi = enumSetting("graphicsApi", GraphicsApi.DEFAULT_OPENGL)

    /**
     * 分辨率
     */
    val resolutionRatio = intSetting("resolutionRatio", 100, 25..300)

    /**
     * 游戏页面全屏化
     */
    val gameFullScreen = boolSetting("gameFullScreen", true)

    /**
     * 使用 SurfaceView 渲染
     */
    val useSurfaceView = boolSetting("useSurfaceView", false)

    /**
     * 持续性能模式
     */
    val sustainedPerformance = boolSetting("sustainedPerformance", false)

    /**
     * 使用系统的 Vulkan 驱动
     */
    val zinkPreferSystemDriver = boolSetting("zinkPreferSystemDriver", false)

    /**
     * Zink 垂直同步
     */
    val vsyncInZink = boolSetting("vsyncInZink", false)

    /**
     * 强制在高性能核心运行
     */
    val bigCoreAffinity = boolSetting("bigCoreAffinity", false)

    /**
     * 启用着色器日志输出
     */
    val dumpShaders = boolSetting("dumpShaders", false)

    //Game
    /**
     * 版本隔离
     */
    val versionIsolation = boolSetting("versionIsolation", true)

    /**
     * 不检查游戏完整性
     */
    val skipGameIntegrityCheck = boolSetting("skipGameIntegrityCheck", false)

    /**
     * 版本自定义信息
     */
    val versionCustomInfo = stringSetting("versionCustomInfo", "${InfoDistributor.LAUNCHER_IDENTIFIER}[zl_version]")

    /**
     * 启动器的Java环境
     */
    val javaRuntime = stringSetting("javaRuntime", "")

    /**
     * 自动选择Java环境
     */
    val autoPickJavaRuntime = boolSetting("autoPickJavaRuntime", true)

    /**
     * 游戏内存分配大小
     */
    val ramAllocation = intSetting("ramAllocation", null, min = 256)

    /**
     * 自定义Jvm启动参数
     */
    val jvmArgs = stringSetting("jvmArgs", "")

    /**
     * 已禁用的原生库插件列表
     */
    val disableNativeLibPlugins = stringListSetting("nativeLibPlugins", emptyList())

    /**
     * 启动游戏时自动展示日志，直到游戏开始渲染
     */
    val showLogAutomatic = boolSetting("showLogAutomatic", false)

    /**
     * 日志字体大小
     */
    val logTextSize = intSetting("logTextSize", 15, 5..20)

    /**
     * 日志缓冲区刷新时间
     */
    val logBufferFlushInterval = intSetting("logBufferFlushInterval", 200, 100..1000)

    //Control
    /**
     * 实体鼠标控制
     */
    val physicalMouseMode = boolSetting("physicalMouseMode", true)

    /**
     * 按键键值，按下按键呼出输入法
     */
    val physicalKeyImeCode = intSetting("physicalKeyImeCode", null)

    /**
     * 隐藏虚拟鼠标
     */
    val hideMouse = boolSetting("hideMouse", false)

    /**
     * 虚拟鼠标大小（Dp）
     */
    val mouseSize = intSetting("mouseSize", 24, 5..50)

    /**
     * 虚拟鼠标箭头热点坐标
     */
    val arrowMouseHotspot = parcelableSetting("arrowMouseHotspot", LEFT_TOP_HOTSPOT)

    /**
     * 虚拟鼠标链接选择热点坐标
     */
    val linkMouseHotspot = parcelableSetting("linkMouseHotspot", CursorHotspot(xPercent = 23, yPercent = 0))

    /**
     * 虚拟鼠标输入选择热点坐标
     */
    val iBeamMouseHotspot = parcelableSetting("iBeamMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标十字热点坐标
     */
    val crossHairMouseHotspot = parcelableSetting("crossHairMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标调整大小（上下）热点坐标
     */
    val resizeNSMouseHotspot = parcelableSetting("resizeNSMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标调整大小（左右）热点坐标
     */
    val resizeEWMouseHotspot = parcelableSetting("resizeEWMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标调整大小（全部方向）热点坐标
     */
    val resizeAllMouseHotspot = parcelableSetting("resizeAllMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标禁止/无效操作热点坐标
     */
    val notAllowedMouseHotspot = parcelableSetting("notAllowedMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标灵敏度
     */
    val cursorSensitivity = intSetting("cursorSensitivity", 100, 25..300)

    /**
     * 被抓获指针移动灵敏度
     */
    val mouseCaptureSensitivity = intSetting("mouseCaptureSensitivity", 100, 25..300)

    /**
     * 虚拟鼠标控制模式
     */
    val mouseControlMode = enumSetting("mouseControlMode", MouseControlMode.SLIDE)

    /**
     * 鼠标控制长按延迟
     */
    val mouseLongPressDelay = intSetting("mouseLongPressDelay", 300, 100..1000)

    /**
     * 是否开启虚拟鼠标点击操作
     */
    val enableMouseClick = boolSetting("enableMouseClick", true)

    /**
     * 是否启用手柄控制
     */
    val gamepadControl = boolSetting("gamepadControl", true)

    /**
     * 摇杆死区缩放
     */
    val gamepadDeadZoneScale = intSetting("gamepadDeadZoneScale", 100, 50..200)

    /**
     * 手柄映射配置
     */
    val gamepadMappingConfig = stringSetting("gamepadMappingConfig", "default")

    /**
     * 摇杆控制模式
     */
    val joystickControlMode = enumSetting("joystickControlMode", JoystickMode.LeftMovement)

    /**
     * 手柄摇杆控制鼠标指针时的灵敏度
     */
    val gamepadCursorSensitivity = intSetting("gamepadCursorSensitivity", 100, 25..300)

    /**
     * 手柄摇杆控制游戏视角时的灵敏度
     */
    val gamepadCameraSensitivity = intSetting("gamepadCameraSensitivity", 100, 25..300)

    /**
     * 手势控制
     */
    val gestureControl = boolSetting("gestureControl", false)

    /**
     * 手势控制点击时触发的鼠标按钮
     */
    val gestureTapMouseAction = enumSetting("gestureTapMouseAction", GestureActionType.MOUSE_RIGHT)

    /**
     * 手势控制长按时触发的鼠标按钮
     */
    val gestureLongPressMouseAction = enumSetting("gestureLongPressMouseAction", GestureActionType.MOUSE_LEFT)

    /**
     * 手势控制长按延迟
     */
    val gestureLongPressDelay = intSetting("gestureLongPressDelay", 300, 100..1000)

    /**
     * 陀螺仪控制
     */
    val gyroscopeControl = boolSetting("gyroscopeControl", false)

    /**
     * 陀螺仪控制灵敏度
     */
    val gyroscopeSensitivity = intSetting("gyroscopeSensitivity", 100, 25..300)

    /**
     * 陀螺仪采样率
     */
    val gyroscopeSampleRate = intSetting("gyroscopeSampleRate", 16, 5..50)

    /**
     * 陀螺仪数值平滑
     */
    val gyroscopeSmoothing = boolSetting("gyroscopeSmoothing", true)

    /**
     * 陀螺仪平滑处理的窗口大小
     */
    val gyroscopeSmoothingWindow = intSetting("gyroscopeSmoothingWindow", 4, 2..10)

    /**
     * 反转 X 轴
     */
    val gyroscopeInvertX = boolSetting("gyroscopeInvertX", false)

    /**
     * 反转 Y 轴
     */
    val gyroscopeInvertY = boolSetting("gyroscopeInvertY", false)

    //Launcher
    /**
     * 颜色主题色
     * Android 12+ 默认动态主题色
     */
    val launcherColorTheme = enumSetting(
        "launcherColorTheme",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ColorThemeType.DYNAMIC
        else ColorThemeType.EMBERMIRE
    )

    /**
     * 自定义颜色主题色
     */
    val launcherCustomColor = intSetting("launcherCustomColor", Color.Blue.toArgb())

    /**
     * 启动器UI深色主题
     */
    val launcherDarkMode = enumSetting("launcherDarkMode", DarkMode.FollowSystem)

    /**
     * 启动器语言
     */
    val launcherLanguage = enumSetting("launcherLanguage", AppLanguage.FOLLOW_SYSTEM)

    /**
     * 启动器部分屏幕全屏
     */
    val launcherFullScreen = boolSetting("launcherFullScreen", true)

    /**
     * 持续型节日彩蛋效果
     */
    val launcherFestivalEffects = boolSetting("launcherFestivalEffects", true)

    /**
     * 动画倍速
     */
    val launcherAnimateSpeed = intSetting("launcherAnimateSpeed", 5, 0..10)

    /**
     * 动画幅度
     */
    val launcherAnimateExtent = intSetting("launcherAnimateExtent", 5, 0..10)

    /**
     * 启动器页面切换动画类型
     */
    val launcherSwapAnimateType = enumSetting("launcherSwapAnimateType", TransitionAnimationType.JELLY_BOUNCE)

    /**
     * 启动器背景元素不透明度
     */
    val launcherBackgroundOpacity = intSetting("launcherBackgroundOpacity", 80, 20..100)

    /**
     * 启动器视频背景音量
     */
    val videoBackgroundVolume = intSetting("videoBackgroundVolume", 0, 0..100)

    /**
     * 启动器主页类型
     */
    val homePageType = enumSetting("homePageType", HomePageType.Blank)

    /**
     * 启动器网络主页下载地址
     */
    val homePageURL = stringSetting("homePageURL", "")

    /**
     * 启动器上次检查更新时，用户选择忽略的版本号
     */
    val lastIgnoredVersion = intSetting("lastIgnoredVersion", null)

    /**
     * 启动器日志保留天数
     */
    val launcherLogRetentionDays = intSetting("launcherLogRetentionDays", 7, 1..14)

    /**
     * 下载版本附加内容镜像源类型
     */
    val fetchModLoaderSource = enumSetting("fetchModLoaderSource", MirrorSourceType.OFFICIAL_FIRST)

    /**
     * 文件下载镜像源类型
     */
    val fileDownloadSource = enumSetting("fileDownloadSource", MirrorSourceType.OFFICIAL_FIRST)

    /**
     * 资源搜索镜像源类型
     */
    val assetSearchSource = enumSetting("assetSearchSource", MirrorSourceType.OFFICIAL_FIRST)

    /**
     * 资源下载镜像源类型
     */
    val assetDownloadSource = enumSetting("assetDownloadSource", MirrorSourceType.OFFICIAL_FIRST)

    //Control
    /**
     * 全局默认控制布局文件名
     */
    val controlLayout = stringSetting("controlLayout", "")

    //Other
    /**
     * 当前选择的账号
     */
    val currentAccount = stringSetting("currentAccount", "")

    /**
     * 当前选择的游戏目录id
     */
    val currentGamePathId = stringSetting("currentGamePathId", GamePathManager.DEFAULT_ID)

    /**
     * 启动器任务菜单是否展开
     */
    val launcherTaskMenuExpanded = boolSetting("launcherTaskMenuExpanded", true)

    /**
     * 在游戏菜单悬浮窗上显示帧率
     */
    val showFPS = boolSetting("showFPS", true)

    /**
     * 在游戏菜单悬浮窗上显示内存
     */
    val showMemory = boolSetting("showMemory", false)

    /**
     * 在游戏画面上展示菜单悬浮窗
     */
    val showMenuBall = boolSetting("showMenuBall", true)

    /**
     * 游戏菜单悬浮窗位置
     */
    val menuBallPos = offsetSetting("menuBallPos", Offset.Zero)

    /**
     * 游戏菜单悬浮窗不透明度
     */
    val menuBallOpacity = intSetting("menuBallOpacity", 100, 20..100)

    /**
     * 快捷栏判定箱计算规则
     */
    val hotbarRule = enumSetting("hotbarRule", HotbarRule.Auto)

    /**
     * 快捷栏宽度百分比
     */
    val hotbarWidth = intSetting("hotbarWidth", 500, 0..1000)

    /**
     * 快捷栏高度百分比
     */
    val hotbarHeight = intSetting("hotbarHeight", 100, 0..1000)

    /**
     * 游戏内控制布局的整体不透明度
     */
    val controlsOpacity = intSetting("controlsOpacity", 100, 0..100)

    /**
     * 控制布局编辑器：是否开启控件吸附功能
     */
    val editorEnableWidgetSnap = boolSetting("editorEnableWidgetSnap", true)

    /**
     * 控制布局编辑器：是否在所有控件层范围内吸附
     */
    val editorSnapInAllLayers = boolSetting("editorSnapInAllLayers", false)

    /**
     * 控制布局编辑器：控件吸附模式
     */
    val editorWidgetSnapMode = enumSetting("editorWidgetSnapMode", SnapMode.FullScreen)

    /**
     * 是否启用陶瓦联机
     */
    val enableTerracotta = boolSetting("enableTerracotta", false)

    /**
     * 陶瓦联机公告版本号
     */
    val terracottaNoticeVer = intSetting("terracottaNoticeVer", -1)

    //特殊样式
    /**
     * 是否在游戏中启用摇杆移动组件
     */
    val enableJoystickControl = boolSetting("enableJoystickControl", false)

    /**
     * 游戏中摇杆移动组件的 X 坐标 0~10000
     */
    val joystickControlX = intSetting("joystickControlX", 5000, POSITION_RANGE)

    /**
     * 游戏中摇杆移动组件的 Y 坐标 0~10000
     */
    val joystickControlY = intSetting("joystickControlY", 5000, POSITION_RANGE)

    /**
     * 游戏中摇杆移动组件的大小 Dp
     */
    val joystickControlSize = intSetting("joystickControlSize", 120, 80..180)

    /**
     * 游戏中摇杆移动组件是否使用控制布局提供的样式
     */
    val joystickUseStyleByLayout = boolSetting("joystickUseStyleByLayout", true)

    /**
     * 游戏中摇杆移动组件是否在使用实体鼠标时隐藏
     */
    val joystickHideWhenMouse = boolSetting("joystickHideWhenMouse", true)

    /**
     * 游戏中摇杆移动组件是否在使用手柄时隐藏
     */
    val joystickHideWhenGamepad = boolSetting("joystickHideWhenGamepad", true)

    /**
     * 摇杆组件死区缩放
     */
    val joystickDeadZoneRatio = intSetting("joystickDeadZoneRatio", 30, 10..50)

    /**
     * 摇杆前进锁判定范围
     */
    val joystickLockThreshold = intSetting("joystickLockThreshold", 30, 5..100)

    /**
     * 游戏中摇杆移动组件是否可锁定
     */
    val joystickControlCanLock = boolSetting("joystickControlCanLock", true)

    /**
     * 游戏中摇杆移动组件在前进锁定时，是否强制疾跑
     */
    val joystickControlLockSpring = boolSetting("joystickControlLockSpring", true)

    /**
     * 上次检查更新的时间戳
     */
    val lastUpgradeCheck = longSetting("lastUpgradeCheck", 0L)

    /**
     * 玩家结束运行游戏的次数
     */
    val finishedGame = intSetting("finishedGame", 0)

    /**
     * 是否在打开启动器时，根据特定的运行游戏次数，显示赞助支持弹窗
     */
    val showSponsorship = boolSetting("showSponsorship", true)

    /**
     * 搜索模组的初始搜索平台
     */
    val searchModPlatform = enumSetting("searchModPlatform", Platform.MODRINTH)

    /**
     * 搜索整合包的初始搜索平台
     */
    val searchModpackPlatform = enumSetting("searchModpackPlatform", Platform.MODRINTH)

    /**
     * 搜索资源包的初始搜索平台
     */
    val searchResourcePackPlatform = enumSetting("searchResourcePackPlatform", Platform.MODRINTH)

    /**
     * 搜索光影的初始搜索平台
     */
    val searchShadersPlatform = enumSetting("searchShadersPlatform", Platform.MODRINTH)

    /**
     * 启动 MC26.2+ 时，自动检查 Vulkan
     */
    val autoVulkanChecker = boolSetting("autoVulkanChecker", true)
}