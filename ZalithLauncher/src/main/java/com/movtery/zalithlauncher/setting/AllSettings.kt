package com.movtery.zalithlauncher.setting

import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.setting.enums.ActionMenuSide
import com.movtery.zalithlauncher.setting.enums.AppLanguage
import com.movtery.zalithlauncher.setting.enums.BackgroundBlur
import com.movtery.zalithlauncher.setting.enums.DarkMode
import com.movtery.zalithlauncher.setting.enums.GamepadInputMode
import com.movtery.zalithlauncher.setting.enums.GestureActionType
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
    val renderer = stringSetting("renderer", "")
    val vulkanDriver = stringSetting("vulkanDriver", "default turnip")
    val graphicsApi = enumSetting("graphicsApi", GraphicsApi.DEFAULT_OPENGL)
    val resolutionRatio = intSetting("resolutionRatio", 70, 25..300)
    val resolutionRule = enumSetting("resolutionRule", ResolutionRule.PERCENTAGE)
    val customResolutionWidth = intSetting("customResolutionWidth", 0)
    val customResolutionHeight = intSetting("customResolutionHeight", 0)
    val gameFullScreen = boolSetting("gameFullScreen", true)
    val useSurfaceView = boolSetting("useSurfaceView", true)
    val sustainedPerformance = boolSetting("sustainedPerformance", false)
    val zinkPreferSystemDriver = boolSetting("zinkPreferSystemDriver", false)
    val vsyncInZink = boolSetting("vsyncInZink", false)
    val dumpShaders = boolSetting("dumpShaders", false)
    val versionIsolation = boolSetting("versionIsolation", true)
    val skipGameIntegrityCheck = boolSetting("skipGameIntegrityCheck", false)
    val versionCustomInfo = stringSetting("versionCustomInfo", "${BuildKeys.LAUNCHER_IDENTIFIER}[zl_version]")
    val javaRuntime = stringSetting("javaRuntime", "")
    val autoPickJavaRuntime = boolSetting("autoPickJavaRuntime", true)
    val ramAllocation = intSetting("ramAllocation", null, min = 256)
    val jvmArgs = stringSetting("jvmArgs", "")
    val disableNativeLibPlugins = stringListSetting("nativeLibPlugins", emptyList())
    val showLogAutomatic = boolSetting("showLogAutomatic", false)
    val logTextSize = intSetting("logTextSize", 15, 5..20)
    val logBufferFlushInterval = intSetting("logBufferFlushInterval", 200, 100..1000)
    val physicalMouseMode = boolSetting("physicalMouseMode", true)
    val physicalKeyImeCode = intSetting("physicalKeyImeCode", null)
    val hideMouse = boolSetting("hideMouse", false)
    val mouseSize = intSetting("mouseSize", 24, 5..50)
    val arrowMouseHotspot = parcelableSetting("arrowMouseHotspot", LEFT_TOP_HOTSPOT)
    val linkMouseHotspot = parcelableSetting("linkMouseHotspot", CursorHotspot(xPercent = 23, yPercent = 0))
    val iBeamMouseHotspot = parcelableSetting("iBeamMouseHotspot", CENTER_HOTSPOT)
    val crossHairMouseHotspot = parcelableSetting("crossHairMouseHotspot", CENTER_HOTSPOT)
    val resizeNSMouseHotspot = parcelableSetting("resizeNSMouseHotspot", CENTER_HOTSPOT)
    val resizeEWMouseHotspot = parcelableSetting("resizeEWMouseHotspot", CENTER_HOTSPOT)
    val resizeAllMouseHotspot = parcelableSetting("resizeAllMouseHotspot", CENTER_HOTSPOT)
    val notAllowedMouseHotspot = parcelableSetting("notAllowedMouseHotspot", CENTER_HOTSPOT)
    val cursorSensitivity = intSetting("cursorSensitivity", 100, 25..300)
    val mouseCaptureSensitivity = intSetting("mouseCaptureSensitivity", 100, 25..300)
    val mouseControlMode = enumSetting("mouseControlMode", MouseControlMode.SLIDE)
    val mouseLongPressDelay = intSetting("mouseLongPressDelay", 300, 100..1000)
    val enableMouseClick = boolSetting("enableMouseClick", true)
    val gamepadControl = boolSetting("gamepadControl", true)
    val sdlAutoShowIme = boolSetting("sdlAutoShowIme", true)
    val gamepadInputMode = enumSetting("gamepadInputMode", GamepadInputMode.Mapped)
    val gamepadInputModePrompted = boolSetting("gamepadInputModePrompted", false)
    val gamepadDeadZoneScale = intSetting("gamepadDeadZoneScale", 100, 50..200)
    val gamepadMappingConfig = stringSetting("gamepadMappingConfig", "default")
    val joystickControlMode = enumSetting("joystickControlMode", JoystickMode.LeftMovement)
    val gamepadCursorSensitivity = intSetting("gamepadCursorSensitivity", 100, 25..300)
    val gamepadCameraSensitivity = intSetting("gamepadCameraSensitivity", 100, 25..300)
    val gestureControl = boolSetting("gestureControl", false)
    val gestureTapMouseAction = enumSetting("gestureTapMouseAction", GestureActionType.MOUSE_RIGHT)
    val gestureLongPressMouseAction = enumSetting("gestureLongPressMouseAction", GestureActionType.MOUSE_LEFT)
    val gestureLongPressDelay = intSetting("gestureLongPressDelay", 300, 100..1000)
    val gyroscopeControl = boolSetting("gyroscopeControl", false)
    val gyroscopeSensitivity = intSetting("gyroscopeSensitivity", 100, 25..300)
    val gyroscopeSampleRate = intSetting("gyroscopeSampleRate", 16, 5..50)
    val gyroscopeSmoothing = boolSetting("gyroscopeSmoothing", true)
    val gyroscopeSmoothingWindow = intSetting("gyroscopeSmoothingWindow", 4, 2..10)
    val gyroscopeInvertX = boolSetting("gyroscopeInvertX", false)
    val gyroscopeInvertY = boolSetting("gyroscopeInvertY", false)
    val launcherColorTheme = enumSetting("launcherColorTheme", ColorThemeType.MIRAI)
    val launcherCustomColor = intSetting("launcherCustomColor", Color.Blue.toArgb())
    val launcherCustomPaletteStyle = enumSetting("launcherCustomPaletteStyle", PaletteStyle.TonalSpot)
    val launcherDarkMode = enumSetting("launcherDarkMode", DarkMode.Enable)
    val launcherLanguage = enumSetting("launcherLanguage", AppLanguage.FOLLOW_SYSTEM)
    val launcherFullScreen = boolSetting("launcherFullScreen", true)
    val launcherFestivalEffects = boolSetting("launcherFestivalEffects", false)
    val launcherAnimateSpeed = intSetting("launcherAnimateSpeed", 3, 0..10)
    val launcherAnimateExtent = intSetting("launcherAnimateExtent", 3, 0..10)
    val launcherSwapAnimateType = enumSetting("launcherSwapAnimateType", TransitionAnimationType.JELLY_BOUNCE)
    val launcherActionMenuSide = enumSetting("launcherActionMenuSide", ActionMenuSide.END)
    val launcherBackgroundOpacity = intSetting("launcherBackgroundOpacity", 60, 20..100)
    val videoBackgroundVolume = intSetting("videoBackgroundVolume", 0, 0..100)
    val backgroundBlur = intSetting("backgroundBlur", 0, 0..40)
    val backgroundBlurType = enumSetting("backgroundBlurType", BackgroundBlur.Background)
    val lastIgnoredVersion = intSetting("lastIgnoredVersion", null)
    val launcherLogRetentionDays = intSetting("launcherLogRetentionDays", 7, 1..14)
    val gameDownloadSource = enumSetting("gameDownloadSource", MirrorSourceType.AUTO, MirrorSourceType.LEGACY_NAMES)
    val assetPlatformSource = enumSetting("assetPlatformSource", MirrorSourceType.AUTO, MirrorSourceType.LEGACY_NAMES)
    val controlLayout = stringSetting("controlLayout", "")
    val currentAccount = stringSetting("currentAccount", "")
    val currentGamePathId = stringSetting("currentGamePathId", GamePathManager.DEFAULT_ID)
    val launcherTaskMenuExpanded = boolSetting("launcherTaskMenuExpanded", false)
    val showFPS = boolSetting("showFPS", true)
    val showMemory = boolSetting("showMemory", false)
    val showMenuBall = boolSetting("showMenuBall", true)
    val menuBallPos = offsetSetting("menuBallPos", Offset.Zero)
    val menuBallOpacity = intSetting("menuBallOpacity", 100, 20..100)
    val hotbarRule = enumSetting("hotbarRule", HotbarRule.Auto)
    val hotbarWidth = intSetting("hotbarWidth", 500, 0..1000)
    val hotbarHeight = intSetting("hotbarHeight", 100, 0..1000)
    val hotbarDoubleClick = boolSetting("hotbarDoubleClick", true)
    val hotbarLongClick = boolSetting("hotbarLongClick", true)
    val hotbarLongClickDelay = intSetting("hotbarLongClickDelay", 300, 100..1000)
    val controlsOpacity = intSetting("controlsOpacity", 100, 0..100)
    val editorEnableWidgetSnap = boolSetting("editorEnableWidgetSnap", true)
    val editorSnapInAllLayers = boolSetting("editorSnapInAllLayers", false)
    val editorWidgetSnapMode = enumSetting("editorWidgetSnapMode", SnapMode.FullScreen)
    val enableTerracotta = boolSetting("enableTerracotta", false)
    val enableTerracottaNodes = boolSetting("enableTerracottaNodes", false)
    val terracottaNodes = stringSetting("terracottaNodes", "")
    val terracottaNoticeVer = intSetting("terracottaNoticeVer", -1)
    val lastUpgradeCheck = longSetting("lastUpgradeCheck", 0L)
    val finishedGame = intSetting("finishedGame", 0)
    val showSponsorship = boolSetting("showSponsorship", true)
    val searchModPlatform = enumSetting("searchModPlatform", Platform.CURSEFORGE)
    val searchModpackPlatform = enumSetting("searchModpackPlatform", Platform.CURSEFORGE)
    val searchResourcePackPlatform = enumSetting("searchResourcePackPlatform", Platform.CURSEFORGE)
    val searchShadersPlatform = enumSetting("searchShadersPlatform", Platform.CURSEFORGE)
    val miraiQuietMode = boolSetting("miraiQuietMode", false)
    val miraiVulkanFailCount = intSetting("miraiVulkanFailCount", 0)
}
