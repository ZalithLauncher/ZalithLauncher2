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
import com.materialkolor.PaletteStyle
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSortField
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.setting.enums.ActionMenuSide
import com.movtery.zalithlauncher.setting.enums.AppLanguage
import com.movtery.zalithlauncher.setting.enums.BackgroundBlur
import com.movtery.zalithlauncher.setting.enums.ChromaMode
import com.movtery.zalithlauncher.setting.enums.DarkMode
import com.movtery.zalithlauncher.setting.enums.FpsDisplayMode
import com.movtery.zalithlauncher.setting.enums.GamepadInputMode
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.MainScreenMode
import com.movtery.zalithlauncher.setting.enums.AccountTypeDisplayMode
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.setting.enums.ResolutionRule
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
     * Turnip 驱动下载源（GitHub owner/repo）
     */
    val turnipRepo = stringSetting("turnipRepo", "K11MCH1/AdrenoToolsDrivers")

    /**
     * 图形 API（Minecraft 26.2+）
     */
    val graphicsApi = enumSetting("graphicsApi", GraphicsApi.DEFAULT_OPENGL)

    /**
     * 分辨率
     */
    val resolutionRatio = intSetting("resolutionRatio", 70, 25..300)

    /**
     * 分辨率规则
     */
    val resolutionRule = enumSetting("resolutionRule", ResolutionRule.PERCENTAGE)

    /**
     * 自定义分辨率宽度，0 表示尚未初始化
     */
    val customResolutionWidth = intSetting("customResolutionWidth", 0)

    /**
     * 自定义分辨率高度，0 表示尚未初始化
     */
    val customResolutionHeight = intSetting("customResolutionHeight", 0)

    /**
     * 游戏页面全屏化
     */
    val gameFullScreen = boolSetting("gameFullScreen", true)

    /**
     * 使用 SurfaceView 渲染
     */
    val useSurfaceView = boolSetting("useSurfaceView", false)

    /**
     * Kopper Zink uyarısını bir daha gösterme
     */
    val surfaceViewKopperWarningDontShow = boolSetting("surfaceViewKopperWarningDontShow", false)

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
     * Frame generation (swapchain frame doubling)
     */
    val frameGeneration = boolSetting("frameGeneration", false)

    /**
     * Enable FPS limit
     */
    val fpsLimitEnabled = boolSetting("fpsLimitEnabled", false)

    /**
     * FPS limit value
     */
    val fpsLimit = intSetting("fpsLimit", 60, 15..240)

    /**
     * Force running on performance (big) CPU cores
     */
    val bigCoreAffinity = boolSetting("bigCoreAffinity", false)

    /**
     * Enable shader dump logging
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
    val versionCustomInfo = stringSetting("versionCustomInfo", "${BuildKeys.LAUNCHER_IDENTIFIER}[zl_version]")

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
     * 自动内存分配
     */
    val autoRamAllocation = boolSetting("autoRamAllocation", false)

    /**
     * 自动内存分配模式（仅在 [autoRamAllocation] 为 true 时有意义）
     * "static"  = one-time total-RAM lookup table (original behaviour, set once)
     * "dynamic" = live free RAM minus safety headroom, refreshed every 5 s (default)
     * "maximum" = live free RAM, no headroom, refreshed every 5 s
     */
    val autoRamAllocationMode = stringSetting("autoRamAllocationMode", "dynamic")

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
     * 游戏加载时隐藏控制布局
     */
    val hideControlsDuringLoading = boolSetting("hideControlsDuringLoading", true)

    /**
     * 禁用加载弹出提示
     */
    val disableLoadingPopup = boolSetting("disableLoadingPopup", false)

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
     * SDL 下是否允许自动唤起输入法
     */
    val sdlAutoShowIme = boolSetting("sdlAutoShowIme", true)

    /**
     * 手柄输入模式（映射虚拟按键 / SDL 直通）
     */
    val gamepadInputMode = enumSetting("gamepadInputMode", GamepadInputMode.Mapped)

    /**
     * 是否已完成手柄输入模式的选择询问
     */
    val gamepadInputModePrompted = boolSetting("gamepadInputModePrompted", false)

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
     * Launcher theme: use the fixed Mirai green palette by default so the interface
     * remains consistent across Android versions and device wallpapers.
     */
    val launcherColorTheme = enumSetting(
        "launcherColorTheme",
        ColorThemeType.MIRAI
    )

    /**
     * 自定义颜色主题色
     */
    val launcherCustomColor = intSetting("launcherCustomColor", Color.Blue.toArgb())

    /**
     * 自定义颜色配色风格
     */
    val launcherCustomPaletteStyle = enumSetting("launcherCustomPaletteStyle", PaletteStyle.TonalSpot)

    /**
     * 启动器UI深色主题
     */
    val launcherDarkMode = enumSetting("launcherDarkMode", DarkMode.Enable)

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
     * Show the Version Profile indicator pill above the selected version card.
     */
    val showVersionProfileIndicator = boolSetting("showVersionProfileIndicator", false)

    /**
     * Quick Access 面板快捷方式（有序列表，存储快捷方式ID）
     * 默认顺序：FPS、文件管理器、版本管理、控制布局
     */
    val quickAccessShortcuts = stringListSetting(
        "quickAccessShortcuts",
        listOf("fps", "file_manager", "versions", "controls")
    )

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
     * 主界面操作菜单的停泊侧
     */
    val launcherActionMenuSide = enumSetting("launcherActionMenuSide", ActionMenuSide.END)

    /**
     * 启动器背景元素不透明度
     */
    val launcherBackgroundOpacity = intSetting("launcherBackgroundOpacity", 60, 20..100)

    /**
     * 启动器视频背景音量
     */
    val videoBackgroundVolume = intSetting("videoBackgroundVolume", 0, 0..100)

    /**
     * 启动器背景模糊效果
     */
    val backgroundBlur = intSetting("backgroundBlur", 0, 0..40)

    /**
     * 启动器背景模糊效果类型
     */
    val backgroundBlurType = enumSetting("backgroundBlurType", BackgroundBlur.Background)

    /**
     * 版本列表视图模式：0=列表(LIST), 1=网格(GRID)
     */
    val versionViewMode = intSetting("versionViewMode", 0, 0..2)

    /**
     * 启动器主屏幕布局模式：Default（简洁，默认）/ Advanced（完整）
     */
    val mainScreenMode = enumSetting("mainScreenMode", MainScreenMode.Default)

    /**
     * 启动器上次检查更新时，用户选择忽略的版本号
     */
    val lastIgnoredVersion = intSetting("lastIgnoredVersion", null)

    /**
     * 启动器日志保留天数
     */
    val launcherLogRetentionDays = intSetting("launcherLogRetentionDays", 7, 1..14)

    /**
     * 游戏内容下载源
     */
    val gameDownloadSource = enumSetting(
        "gameDownloadSource",
        MirrorSourceType.AUTO,
        MirrorSourceType.LEGACY_NAMES
    )

    /**
     * 资源平台下载源
     */
    val assetPlatformSource = enumSetting(
        "assetPlatformSource",
        MirrorSourceType.AUTO,
        MirrorSourceType.LEGACY_NAMES
    )

    /**
     * 是否使用旧版版本选择器（仅正式版，无搜索框）
     */
    val classicVersionPicker = boolSetting("classicVersionPicker", true)

    /**
     * 自动选择下载内容（主开关）
     * 开启时，搜索下载内容时若只有一个已安装版本则自动预选游戏版本
     */
    val autoSelectDownloadContent = boolSetting("autoSelectDownloadContent", true)
    /** 自动选择 — 整合包 */
    val autoSelectModpacks = boolSetting("autoSelectModpacks", true)
    /** 自动选择 — 模组 */
    val autoSelectMods = boolSetting("autoSelectMods", true)
    /** 自动选择 — 资源包 */
    val autoSelectResourcePacks = boolSetting("autoSelectResourcePacks", true)
    /** 自动选择 — 光影 */
    val autoSelectShaderPacks = boolSetting("autoSelectShaderPacks", true)
    /** 自动选择 — 存档 */
    val autoSelectSaves = boolSetting("autoSelectSaves", true)
    /**
     * Migration flag: true once the auto-select sub-settings have been reset to their defaults
     * after the feature was repurposed from auto-scrolling to game-version auto-selection.
     */
    val autoSelectGameVersionMigrationDone = boolSetting("autoSelectGameVersionMigrationDone", false)

    /**
     * 主屏幕账号卡片：是否显示账号类型文字（默认关闭）
     */
    val showAccountType = boolSetting("showAccountType", false)

    /**
     * 主屏幕账号卡片：账号类型文字显示模式
     */
    val accountTypeDisplayMode = enumSetting("accountTypeDisplayMode", AccountTypeDisplayMode.HideAfterTimeout)

    //Control
    /**
     * 全局默认控制布局文件名
     */
    val controlLayout = stringSetting("controlLayout", "")
    val legacyControlLayout = stringSetting("legacyControlLayout", "")
    /**
     * Active control type: "zalith2" or "legacy"
     */
    val controlType = stringSetting("controlType", "zalith2")

    /** Legacy (ZL1 backport) button snapping — snap control buttons to a grid when editing */
    val buttonSnapping = boolSetting("buttonSnapping", false)

    /** Legacy (ZL1 backport) button snapping distance in dp */
    val buttonSnappingDistance = intSetting("buttonSnappingDistance", 8, 1..64)

    /** Legacy (ZL1 backport) global button scale percentage (25–200 %) */
    val buttonScale = intSetting("buttonscale", 100, 25..200)

    /** Legacy (ZL1 backport) whether button labels are shown in all-caps */
    val buttonAllCaps = boolSetting("buttonAllCaps", false)

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
     * Default is false so the Task Menu starts collapsed when a task is first minimized.
     */
    val launcherTaskMenuExpanded = boolSetting("launcherTaskMenuExpanded", false)

    /**
     * 在游戏菜单悬浮窗上显示帧率
     */
    val showFPS = boolSetting("showFPS", true)

    /**
     * 游戏内帧率的展示模式
     */
    val fpsDisplayMode = enumSetting("fpsDisplayMode", FpsDisplayMode.NUMBER)

    /**
     * 在游戏菜单悬浮窗上显示内存
     */
    val showMemory = boolSetting("showMemory", false)

    /**
     * 内存显示模式：显示已分配JVM内存 (Allocated) 还是系统总内存 (System)
     */
    val memoryDisplayMode = enumSetting("memoryDisplayMode", com.movtery.zalithlauncher.setting.enums.MemoryDisplayMode.System)

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
     * 快捷栏双击与副手交换物品
     */
    val hotbarDoubleClick = boolSetting("hotbarDoubleClick", true)

    /**
     * 快捷栏长按丢弃所选物品
     */
    val hotbarLongClick = boolSetting("hotbarLongClick", true)

    /**
     * 快捷栏长按快捷栏触发延迟
     */
    val hotbarLongClickDelay = intSetting("hotbarLongClickDelay", 300, 100..1000)

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
     * 是否使用自定义 EasyTier 服务器节点
     */
    val enableTerracottaNodes = boolSetting("enableTerracottaNodes", false)

    /**
     * 陶瓦联机：自定义 EasyTier 服务器节点
     */
    val terracottaNodes = stringSetting("terracottaNodes", "")

    /**
     * 陶瓦联机公告版本号
     */
    val terracottaNoticeVer = intSetting("terracottaNoticeVer", -1)

    /**
     * 上次检查更新的时间戳
     */
    val lastUpgradeCheck = longSetting("lastUpgradeCheck", 0L)

    /**
     * 是否已接受非官方声明
     */
    val disclaimerAccepted = boolSetting("disclaimerAccepted", false)

    /**
     * 是否已完成首次启动的主屏幕模式选择
     * false = 尚未选择（首次启动显示引导弹窗）
     * true  = 已选择，不再显示
     */
    val mainScreenModeSelected = boolSetting("mainScreenModeSelected", false)

    /**
     * 玩家结束运行游戏的次数
     */
    val finishedGame = intSetting("finishedGame", 0)

    /**
     * 玩家在模拟器中运行游戏的总时长（毫秒）
     */
    val playTime = longSetting("playTime", 0L)

    /**
     * 彩虹（Chroma）用户名特效模式
     */
    val chromaMode = enumSetting("chromaMode", ChromaMode.NONE)

    /**
     * 是否显示设置导出/导入提示
     */
    val showSettingsTip = boolSetting("showSettingsTip", true)

    /**
     * 是否在打开启动器时，根据特定的运行游戏次数，显示赞助支持弹窗
     */
    val showSponsorship = boolSetting("showSponsorship", true)

    /**
     * 搜索模组的初始搜索平台
     */
    val searchModPlatform = enumSetting("searchModPlatform", Platform.CURSEFORGE)

    /**
     * 搜索整合包的初始搜索平台
     */
    val searchModpackPlatform = enumSetting("searchModpackPlatform", Platform.CURSEFORGE)

    /**
     * 搜索资源包的初始搜索平台
     */
    val searchResourcePackPlatform = enumSetting("searchResourcePackPlatform", Platform.CURSEFORGE)

    /**
     * 搜索光影的初始搜索平台
     */
    val searchShadersPlatform = enumSetting("searchShadersPlatform", Platform.CURSEFORGE)
    // ------- 下载页过滤器持久化 (Issue #22) -------

    /** 搜索模组：排序方式 */
    val searchModSortField = enumSetting("searchModSortField", PlatformSortField.RELEVANCE)
    /** 搜索模组：游戏版本（空字符串代表不筛选） */
    val searchModGameVersion = stringSetting("searchModGameVersion", "")
    /** 搜索模组：已选分类（序列化为字符串列表） */
    val searchModCategories = stringListSetting("searchModCategories", emptyList())
    /** 搜索模组：模组加载器（空字符串代表不筛选） */
    val searchModModLoader = stringSetting("searchModModLoader", "")

    /** 搜索整合包：排序方式 */
    val searchModpackSortField = enumSetting("searchModpackSortField", PlatformSortField.RELEVANCE)
    /** 搜索整合包：游戏版本（空字符串代表不筛选） */
    val searchModpackGameVersion = stringSetting("searchModpackGameVersion", "")
    /** 搜索整合包：已选分类（序列化为字符串列表） */
    val searchModpackCategories = stringListSetting("searchModpackCategories", emptyList())
    /** 搜索整合包：模组加载器（空字符串代表不筛选） */
    val searchModpackModLoader = stringSetting("searchModpackModLoader", "")

    /** 搜索资源包：排序方式 */
    val searchResourcePackSortField = enumSetting("searchResourcePackSortField", PlatformSortField.RELEVANCE)
    /** 搜索资源包：游戏版本（空字符串代表不筛选） */
    val searchResourcePackGameVersion = stringSetting("searchResourcePackGameVersion", "")
    /** 搜索资源包：已选分类（序列化为字符串列表） */
    val searchResourcePackCategories = stringListSetting("searchResourcePackCategories", emptyList())

    /** 搜索光影：排序方式 */
    val searchShadersSortField = enumSetting("searchShadersSortField", PlatformSortField.RELEVANCE)
    /** 搜索光影：游戏版本（空字符串代表不筛选） */
    val searchShadersGameVersion = stringSetting("searchShadersGameVersion", "")
    /** 搜索光影：已选分类（序列化为字符串列表） */
    val searchShadersCategories = stringListSetting("searchShadersCategories", emptyList())

    /**
     * 搜索模组时保存的过滤器状态（JSON）
     */
    val searchModFilter = stringSetting("searchModFilter", "")

    /**
     * 搜索整合包时保存的过滤器状态（JSON）
     */
    val searchModpackFilter = stringSetting("searchModpackFilter", "")

    /**
     * 搜索资源包时保存的过滤器状态（JSON）
     */
    val searchResourcePackFilter = stringSetting("searchResourcePackFilter", "")

    /**
     * 搜索光影时保存的过滤器状态（JSON）
     */
    val searchShadersFilter = stringSetting("searchShadersFilter", "")

    /**
     * 搜索存档时保存的过滤器状态（JSON）
     */
    val searchSavesFilter = stringSetting("searchSavesFilter", "")

    /**
     * 启动 MC26.2+ 时，自动检查 Vulkan
     */
    val autoVulkanChecker = boolSetting("autoVulkanChecker", true)

    //FSR
    val fsrEnabled = boolSetting("fsrEnabled", false)
    val fsrQuality = intSetting("fsrQuality", 2, 1..4) // 1=UltraQuality, 2=Quality, 3=Balanced, 4=Performance

    // ZL1 Legacy Backport setting (named to avoid JVM signature clash with getDisableGestures)
    val zl1DisableGestures = boolSetting("zl1_disableGestures", false)

    // ZL1 Legacy Backport @JvmStatic accessors — callable as AllSettings.xxx() from Java
    @JvmStatic fun getGyroSmoothing() = gyroscopeSmoothing
    @JvmStatic fun getGyroSampleRate() = gyroscopeSampleRate
    @JvmStatic fun getMouseScale() = mouseSize
    @JvmStatic fun getMouseSpeed() = mouseCaptureSensitivity
    @JvmStatic fun getDisableGestures() = zl1DisableGestures
    @JvmStatic fun getDeadZoneScale() = gamepadDeadZoneScale
}