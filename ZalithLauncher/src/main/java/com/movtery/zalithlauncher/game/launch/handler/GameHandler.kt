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

package com.movtery.zalithlauncher.game.launch.handler

import android.app.Activity
import android.view.KeyEvent
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.game.control.legacy.LegacyControlManager
import com.movtery.zalithlauncher.game.input.EfficientAndroidLWJGLKeycode
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.recorder.GameRecorder
import com.movtery.zalithlauncher.game.launch.GameLauncher
import com.movtery.zalithlauncher.game.launch.LaunchConfig
import com.movtery.zalithlauncher.game.launch.MCOptions
import com.movtery.zalithlauncher.game.launch.loadLanguage
import com.movtery.zalithlauncher.game.sdl.SdlBridge
import com.movtery.zalithlauncher.game.sdl.handleGamepadKeyEvent
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GamepadInputMode
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.ui.control.gamepad.isGamepadKeyEvent
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.ui.screens.game.GameScreen
import com.movtery.zalithlauncher.ui.screens.game.elements.LogState
import com.movtery.zalithlauncher.ui.screens.game.elements.mutableStateOfLog
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.libsdl.app.SDLActivity
import org.lwjgl.glfw.CallbackBridge

private const val TAG = "GameHandler"

/** options.txt key for Minecraft's music volume */
private const val MUSIC_VOLUME_KEY = "soundCategory_music"

/** Value used to mute music while the launcher is backgrounded */
private const val MUSIC_MUTED_VALUE = "0.0"

class GameHandler(
    val activity: Activity,
    config: LaunchConfig,
    errorViewModel: ErrorViewModel,
    eventViewModel: EventViewModel,
    private val gamepadViewModel: GamepadViewModel,
    gameLauncher: GameLauncher,
    onExit: (code: Int) -> Unit
) : AbstractHandler(
    type = HandlerType.GAME,
    errorViewModel = errorViewModel,
    eventViewModel = eventViewModel,
    launcher = gameLauncher,
    onExit = onExit
) {
    private val version = config.version
    private val account = config.account

    private val _inputArea = MutableStateFlow<IntRect?>(null)
    override val inputArea = _inputArea.asStateFlow()

    private var isGameRendering = false
    private var showGameInfo by mutableStateOf(true)

    /**
     * Stores the user's original soundCategory_music value for the current session.
     * Set in onPause() and cleared after restoration in onResume().
     * Never written to disk — only held in memory for the current lifecycle transition.
     */
    private var savedMusicVolume: String? = null

    /**
     * Dedicated coroutine scope for async music mute/restore file I/O.
     * Uses Dispatchers.IO to avoid blocking the Android UI thread.
     * SupervisorJob ensures a failure here does not cancel unrelated launcher coroutines.
     */
    private val musicMuteScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 日志展示状态
     */
    private var logState by mutableStateOfLog()

    override suspend fun execute(
        surface: Surface,
        screenSize: IntSize,
        scope: CoroutineScope
    ) {
        ZLBridge.setupBridgeWindow(surface)
        if (AllSettings.fpsLimitEnabled.getValue()) {
            ZLBridge.fpsLimitSet(AllSettings.fpsLimit.getValue())
        }

        MCOptions.setup(activity, version)

        MCOptions.apply {
            set("fullscreen", "false")
            set("touchscreen", "false")

//            //关闭文本转语音功能
//            set("options.narrator", "0")
//            set("narrator", "0")

            if (version.getVersionInfo()!!.minecraftVersion.isLowerVer("1.13")) {
                //fix: 牢版本按键事件
                //shift + w -> 87 错误的触发了F11，切换全屏
                set("key_key.fullscreen", "0")
                //输入字符@ -> 64 错误的触发了F6，触发“开始/停止直播”
                set("key_key.streamStartStop", "0")
                set("key_key.streamPauseUnpause", "0")
            }

            set("overrideWidth", screenSize.width.toString())
            set("overrideHeight", screenSize.height.toString())

            val graphicsApi = version.getGraphicsApi()
            val graphicsOption = "preferredGraphicsBackend"
            when (graphicsApi) {
                GraphicsApi.DEFAULT, GraphicsApi.DEFAULT_OPENGL -> {
                    if (!containsKey(graphicsOption)) {
                        set(graphicsOption, graphicsApi.option)
                    }
                }
                else -> set(graphicsOption, graphicsApi.option)
            }

            loadLanguage(version.getVersionInfo()!!.minecraftVersion)
            save()
        }

        super.execute(surface, screenSize, scope)
    }

    override fun onPause() {
        // The MediaProjection consent dialog is part of the recording start flow, not
        // the user genuinely leaving the game.  Suppress music muting so that the
        // game audio is not interrupted (and no F3+T restore fires on resume).
        // savedMusicVolume stays null, so onResume() naturally skips the restore.
        if (GameRecorder.isConsentPending) return

        // Mute Minecraft's ambient background music when the launcher is backgrounded.
        // Integrates into the same lifecycle hook used for automatic game pausing.
        // All file I/O runs on Dispatchers.IO to avoid blocking the Android UI thread.
        musicMuteScope.launch {
            try {
                // Read soundCategory_music from the active instance's options.txt via MCOptions.
                // MCOptions resolves the path using the active Version's game directory —
                // no path is hardcoded here.
                val currentVolume = MCOptions.get(MUSIC_VOLUME_KEY)

                // Only proceed if a valid, non-zero volume is present to preserve
                if (!currentVolume.isNullOrEmpty() && currentVolume != MUSIC_MUTED_VALUE) {
                    // Store the user's original music volume for this session
                    savedMusicVolume = currentVolume

                    // Replace soundCategory_music with 0.0 — all other options.txt
                    // entries are left completely untouched by MCOptions.save()
                    MCOptions.set(MUSIC_VOLUME_KEY, MUSIC_MUTED_VALUE)
                    MCOptions.save()

                    // Trigger Minecraft to reload the updated configuration using the
                    // existing native input wrapper (CallbackBridge), so the muted
                    // volume takes effect in the running game immediately
                    triggerConfigReload()
                }
            } catch (e: Exception) {
                // Gracefully handle missing options.txt, corrupted config, parse errors,
                // and permission failures. The launcher continues functioning normally
                // even if music muting fails.
                Logger.warning(TAG, "Failed to mute background music on pause", e)
            }
        }
    }

    override fun onResume() {
        // Refresh controls as part of the existing resume flow
        refreshControls()
        eventViewModel.sendEvent(EventViewModel.Event.Game.OnResume)

        // Restore the user's original music volume after returning from background.
        // Only runs if a volume was saved during the corresponding onPause().
        val volumeToRestore = savedMusicVolume ?: return
        musicMuteScope.launch {
            try {
                // Write the previously saved music volume back into options.txt.
                // Preserves the user's original preference — no permanent modification.
                MCOptions.set(MUSIC_VOLUME_KEY, volumeToRestore)
                MCOptions.save()
                savedMusicVolume = null

                // Brief delay to allow the game to fully resume its rendering loop
                // before injecting the configuration reload key sequence
                delay(300L)

                // Trigger Minecraft to reload the updated configuration using the
                // existing native input wrapper, restoring the original music volume
                // at runtime via the same mechanism used during onPause()
                triggerConfigReload()
            } catch (e: Exception) {
                // Gracefully handle any I/O or parsing failure during volume restoration.
                // The launcher continues normally even if the restore fails.
                Logger.warning(TAG, "Failed to restore music volume on resume", e)
            }
        }
    }

    override fun onDestroy() {
        Terracotta.setWaiting(false)
    }

    override fun onGraphicOutput() {
        if (!isGameRendering) {
            isGameRendering = true
            showGameInfo = false
            //游戏已经开始渲染，如果日志状态为渲染前显示，则在这里关闭日志
            if (logState == LogState.SHOW_BEFORE_LOADING) {
                logState = LogState.CLOSE
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun shouldIgnoreKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && (event.flags and KeyEvent.FLAG_CANCELED) != 0) return false

        if (event.isGamepadKeyEvent()) {
            if (AllSettings.gamepadControl.state && gamepadViewModel.checkModePrompt()) {
                //模式选择询问完成前，吞掉所有手柄按键输入
                return false
            }
            if (AllSettings.gamepadControl.state && AllSettings.gamepadInputMode.state == GamepadInputMode.SdlDirect) {
                //SDL 直通模式下，按键原样交给 SDL native
                if (SdlBridge.sdlEnabled) {
                    //让控制布局等注册方感知到手柄使用中
                    gamepadViewModel.notifyActivity()
                    handleGamepadKeyEvent(event)
                    try {
                        SDLActivity.handleKeyEvent(null, event.keyCode, event, null)
                    } catch (_: UnsatisfiedLinkError) {
                        //SDL native 未就绪时忽略
                    }
                }
                return false
            }
            return if (AllSettings.gamepadControl.state) {
                //开启时，提前发送事件，在UI层处理（或重映射）
                gamepadViewModel.sendKeyEvent(event)
                false
            } else {
                //已禁用手柄控制，避免继续向下被当作键盘事件进行处理
                if (AllSettings.showMenuBall.state) {
                    //开启游戏菜单悬浮窗时，完全无响应
                    false
                } else {
                    true
                }
            }
        }
        //已在VMActivity绑定onBackPressedDispatcher，这里不应该继续向下处理
        if (event.keyCode == KeyEvent.KEYCODE_BACK) return true

        if ((event.flags and KeyEvent.FLAG_SOFT_KEYBOARD) == KeyEvent.FLAG_SOFT_KEYBOARD) {
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                LWJGLCharSender.sendEnter()
                return false
            }
            //输入法产生的字符类按键事件不能转发给游戏
            if (event.getUnicodeChar() != 0) return true
        }

        EfficientAndroidLWJGLKeycode.getIndexByKey(event.keyCode).takeIf { it >= 0 }?.let { index ->
            EfficientAndroidLWJGLKeycode.execKey(event, index)
            return false
        }

        return when (event.keyCode) {
            KeyEvent.KEYCODE_UNKNOWN,
            KeyEvent.ACTION_MULTIPLE,
            KeyEvent.ACTION_UP
                 -> false

            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP
                 -> true

            else -> (event.flags and KeyEvent.FLAG_FALLBACK) != KeyEvent.FLAG_FALLBACK
        }
    }

    override fun sendMouseRight(isPressed: Boolean) {
        CallbackBridge.sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT.toInt(), isPressed)
    }

    @Composable
    override fun ComposableLayout(
        textInputMode: TextInputMode
    ) {
        GameScreen(
            version = version,
            gameHandler = this,
            showGameInfo = showGameInfo,
            logState = logState,
            onLogStateChange = { logState = it },
            textInputMode = textInputMode,
            isTouchProxyEnabled = version.enableTouchProxy,
            onInputAreaRectUpdated = { _inputArea.value = it },
            getAccountName = { account.username },
            eventViewModel = eventViewModel,
            gamepadViewModel = gamepadViewModel,
        )
    }

    /**
     * Triggers Minecraft to reload its options configuration using the existing
     * native input wrapper (CallbackBridge). Dispatches the F3+T key sequence via
     * the same JNI bridge used throughout the launcher for all in-game input.
     *
     * F3+T is Minecraft's standard mechanism to reload resources and re-read
     * options.txt, which causes the SoundManager to immediately apply the updated
     * soundCategory_music value at runtime.
     *
     * Reuses the existing CallbackBridge.sendKeyPress() rather than duplicating
     * any native input logic. This is the same entry point used by all other
     * launcher-side key injection (chat open, controls, etc.).
     *
     * Based on the PojavLauncher-style activity architecture where the JNI bridge
     * is the single interface between the Android side and the running LWJGL/GLFW game.
     */
    private fun triggerConfigReload() {
        // Hold F3, tap T, then release both — the standard Minecraft reload sequence
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_F3.toInt(), 0, true)
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_T.toInt(), 0, true)
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_T.toInt(), 0, false)
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_F3.toInt(), 0, false)
    }

    private fun refreshControls() {
        ControlManager.refresh()
        LegacyControlManager.refresh()
    }

    init {
        refreshControls()
    }
}