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
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.game.input.EfficientAndroidLWJGLKeycode
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
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
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.libsdl.app.SDLActivity
import org.lwjgl.glfw.CallbackBridge

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
     * 日志展示状态
     */
    private var logState by mutableStateOfLog()

    override suspend fun execute(
        surface: Surface,
        screenSize: IntSize,
        scope: CoroutineScope
    ) {
        ZLBridge.setupBridgeWindow(surface)

        MCOptions.setup(activity, version)

        MCOptions.apply {
            set("fullscreen", "false")
            set("touchscreen", "false")

            //关闭文本转语音功能
            set("options.narrator", "0")
            set("narrator", "0")

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
    }

    override fun onResume() {
        refreshControls()
        eventViewModel.sendEvent(EventViewModel.Event.Game.OnResume)
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

    override fun sendMouseButton(button: Int, isPressed: Boolean) {
        CallbackBridge.sendMouseButton(button, isPressed)
    }

    @Composable
    override fun ComposableLayout(
        textInputMode: TextInputMode
    ) {
        GameScreen(
            version = version,
            gameHandler = this,
            showGameInfo = showGameInfo,
            onInfoBoxClose = { showGameInfo = false },
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

    private fun refreshControls() {
        ControlManager.refresh()
    }

    init {
        refreshControls()
    }
}