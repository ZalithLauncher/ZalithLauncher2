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

package com.movtery.zalithlauncher.ui.screens.game

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.layer_controller.ControlBoxLayout
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.event.EventHandler
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.layout.EmptyControlLayout
import com.movtery.layer_controller.layout.loadLayoutFromFile
import com.movtery.layer_controller.observable.DefaultObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableSpecial
import com.movtery.layer_controller.utils.widgetPosition
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.bridge.ZLNativeInvoker
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeycode
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.keycodes.OPEN_CHAT
import com.movtery.zalithlauncher.game.keycodes.OPEN_CHAT_VALUE
import com.movtery.zalithlauncher.game.keycodes.SPRING
import com.movtery.zalithlauncher.game.keycodes.SPRING_VALUE
import com.movtery.zalithlauncher.game.keycodes.mapToControlEvent
import com.movtery.zalithlauncher.game.keycodes.mapToKeycode
import com.movtery.zalithlauncher.game.launch.handler.GameHandler
import com.movtery.zalithlauncher.game.support.touch_controller.touchControllerInputModifier
import com.movtery.zalithlauncher.game.support.touch_controller.touchControllerTouchModifier
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.setting.enums.toAction
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.ui.control.MinecraftHotbar
import com.movtery.zalithlauncher.ui.control.event.launcherEvent
import com.movtery.zalithlauncher.ui.control.event.lwjglEvent
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadKeyListener
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadStickMovementListener
import com.movtery.zalithlauncher.ui.control.gamepad.SimpleGamepadCapture
import com.movtery.zalithlauncher.ui.control.gyroscope.GyroscopeReader
import com.movtery.zalithlauncher.ui.control.gyroscope.isGyroscopeAvailable
import com.movtery.zalithlauncher.ui.control.hotbarPercentage
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.ui.control.joystick.JoystickDirectionListener
import com.movtery.zalithlauncher.ui.control.joystick.StyleableJoystick
import com.movtery.zalithlauncher.ui.control.joystick.loadJoystickStyle
import com.movtery.zalithlauncher.ui.control.joystick.saveJoystickStyle
import com.movtery.zalithlauncher.ui.control.mouse.SwitchableMouseLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.DraggableGameBall
import com.movtery.zalithlauncher.ui.screens.game.elements.ForceCloseOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.GameMenuSubscreen
import com.movtery.zalithlauncher.ui.screens.game.elements.JoystickManageOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import com.movtery.zalithlauncher.ui.screens.game.elements.LogState
import com.movtery.zalithlauncher.ui.screens.game.elements.ReplacementControlOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.ReplacementControlState
import com.movtery.zalithlauncher.ui.screens.game.elements.SendKeycodeOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.SendKeycodeState
import com.movtery.zalithlauncher.ui.screens.game.multiplayer.TerracottaOperation
import com.movtery.zalithlauncher.ui.screens.game.multiplayer.rememberTerracottaViewModel
import com.movtery.zalithlauncher.ui.screens.main.control_editor.ControlEditor
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import com.movtery.zalithlauncher.viewmodel.JoystickMovementViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.lwjgl.glfw.CallbackBridge
import java.io.File

private class GameViewModel(
    private val version: Version,
    private val onChangeTextInputMode: (TextInputMode?) -> Unit
) : ViewModel() {
    /** 游戏菜单操作状态 */
    var gameMenuState by mutableStateOf(MenuState.NONE)
    /** 游戏菜单-控制设置区域Tab选择的索引 */
    var controlMenuTabIndex by mutableIntStateOf(0)
    /** 强制关闭弹窗操作状态 */
    var forceCloseState by mutableStateOf<ForceCloseOperation>(ForceCloseOperation.None)
    /** 发送键值操作状态 */
    var sendKeycodeState by mutableStateOf<SendKeycodeState>(SendKeycodeState.None)
    /** 更换控制布局操作状态 */
    var replacementControlState by mutableStateOf<ReplacementControlState>(ReplacementControlState.None)
    /** 被控制布局层标记为仅滑动的指针列表 */
    var moveOnlyPointers = mutableSetOf<PointerId>()
    /** 鼠标触摸指针处理层占用指针列表 */
    var occupiedPointers = mutableSetOf<PointerId>()

    /** 游戏内帧率状态 */
    var gameFps by mutableIntStateOf(0)
        private set
    private var fpsJob: Job? = null
    /** 开始帧率捕获 */
    fun startFpsCapture() {
        //开启一个新的协程，每秒更新一次帧率数据
        fpsJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                runCatching {
                    ensureActive()
                }.onFailure {
                    break
                }
                gameFps = CallbackBridge.getCurrentFps()
                delay(1000L)
            }
        }
    }
    /** 停止帧率捕获 */
    fun stopFpsCapture() {
        fpsJob?.cancel()
        fpsJob = null
    }

    /** 启动器默认摇杆样式 */
    var launcherJoystickStyle by mutableStateOf(DefaultObservableJoystickStyle)
    /** 保存启动器默认摇杆样式 */
    fun saveLauncherJoystickStyle(
        onFailed: (Throwable) -> Unit,
        onSuccess: suspend () -> Unit
    ) {
        viewModelScope.launch {
            saveJoystickStyle(
                path = PathManager.DIR_STYLES,
                launcherJoystickStyle,
                onFailed = onFailed,
                onSuccess = onSuccess
            )
        }
    }

    var editorRefresh by mutableIntStateOf(0)
        private set
    /** 可观察的控制布局 */
    var observableLayout by mutableStateOf<ObservableControlLayout?>(null)
        private set
    /** 当前控制布局文件 */
    var currentControlFile by mutableStateOf<File?>(null)
        private set
    /** 控制布局：控件层隐藏状态 */
    var controlLayerHideState by mutableStateOf(HideLayerWhen.None)
        private set

    /** 是否正在编辑布局 */
    var isEditingLayout by mutableStateOf(false)
        private set

    fun switchControlLayer(hideWhen: HideLayerWhen) {
        if (controlLayerHideState != hideWhen) controlLayerHideState = hideWhen
    }

    /** 虚拟鼠标滚动事件处理 */
    val mouseScrollUpEvent = MouseScrollEvent(viewModelScope, 1.0)
    val mouseScrollDownEvent = MouseScrollEvent(viewModelScope, -1.0)

    /** 游戏内消息发送器 */
    val gameTextSender = GameTextSender(viewModelScope)

    /** 控制布局控件点击事件处理器 */
    val eventHandler = EventHandler { event, pressed ->
        onKeyEvent(event, pressed)
    }

    /** 处理控制布局类点击事件 */
    fun onKeyEvent(event: ClickEvent, pressed: Boolean) {
        val key = event.key
        when (event.type) {
            ClickEvent.Type.Key -> {
                lwjglEvent(
                    eventKey = key,
                    isMouse = key.startsWith("GLFW_MOUSE_", false),
                    isPressed = pressed
                )
            }
            ClickEvent.Type.LauncherEvent -> {
                launcherEvent(
                    eventKey = key,
                    isPressed = pressed,
                    onSwitchIME = { onChangeTextInputMode(null) },
                    onSwitchMenu = { switchMenu() },
                    onSingleScrollUp = { mouseScrollUpEvent.scrollSingle() },
                    onSingleScrollDown = { mouseScrollDownEvent.scrollSingle() },
                    onLongScrollUp = { mouseScrollUpEvent.scrollLongPress() },
                    onLongScrollUpCancel = { mouseScrollUpEvent.cancel() },
                    onLongScrollDown = { mouseScrollDownEvent.scrollLongPress() },
                    onLongScrollDownCancel = { mouseScrollDownEvent.cancel() }
                )
            }
            ClickEvent.Type.SendText -> {
                //游戏内文本发送事件
                if (pressed) {
                    val text = event.key
                    val inGame = ZLBridgeStates.cursorMode.value == CURSOR_DISABLED
                    gameTextSender.send(GameTextSender.Data(text, inGame))
                }
                return
            }
            else -> return
        }
    }

    fun replaceControlLayout(layoutFile: File) {
        viewModelScope.launch(Dispatchers.Main) {
            loadControlLayout(layoutFile)
        }
    }

    private val layoutMutex = Mutex()
    suspend fun loadControlLayout(layoutFile: File? = version.getControlPath()) {
        layoutMutex.withLock {
            withContext(Dispatchers.Main) {
                observableLayout = null
                val layout = withContext(Dispatchers.IO) {
                    delay(10L) //刻意等待一会再加载
                    currentControlFile = layoutFile
                    getLayout(layoutFile)
                }
                //将控制布局加载为可供Compose加载的形式
                observableLayout = ObservableControlLayout(layout)
            }
        }
    }

    private fun getLayout(layoutFile: File? = currentControlFile): ControlLayout {
        return layoutFile?.let {
            try {
                loadLayoutFromFile(it)
            } catch (e: Exception) {
                lWarning("Failed to load control layout: $it", e)
                null
            }
        } ?: EmptyControlLayout
    }

    /**
     * 开始编辑控制布局模式
     */
    fun startControlEditor(editorVM: EditorViewModel) {
        if (!isEditingLayout) {
            clearState()
            editorVM.initLayout(getLayout())
            isEditingLayout = true
        }
    }

    /**
     * 退出编辑控制布局模式（如果当前确实正在编辑控制布局）
     */
    fun exitControlEditor() {
        viewModelScope.launch(Dispatchers.Main) {
            if (isEditingLayout) {
                isEditingLayout = false
                loadControlLayout(currentControlFile)
                editorRefresh++
            }
        }
    }

    /**
     * 切换游戏菜单
     */
    fun switchMenu() {
        this.gameMenuState = this.gameMenuState.next()
    }

    /**
     * 清除所有游戏状态
     */
    fun clearState() {
        mouseScrollUpEvent.cancel()
        mouseScrollDownEvent.cancel()
        gameTextSender.cancel()
        onChangeTextInputMode(TextInputMode.DISABLE)
        moveOnlyPointers.clear()
        occupiedPointers.clear()
    }

    init {
        viewModelScope.launch(Dispatchers.Main) {
            loadControlLayout()
            launcherJoystickStyle = loadJoystickStyle(PathManager.DIR_STYLES)
        }
    }

    override fun onCleared() {
        clearState()
    }
}

/**
 * 鼠标滚轮事件管理
 * @param offset 滚轮滚动距离
 */
private class MouseScrollEvent(
    private val scope: CoroutineScope,
    private val offset: Double
) {
    private var mouseScrollJob: Job? = null

    /**
     * 取消滚动事件，并重置状态
     */
    fun cancel() {
        mouseScrollJob?.cancel()
        mouseScrollJob = null
    }

    /**
     * 单击响应一次滚轮滚动事件
     */
    fun scrollSingle() {
        CallbackBridge.sendScroll(0.0, offset)
    }

    /**
     * 长按不间断触发滚轮滚动事件
     */
    fun scrollLongPress() {
        mouseScrollJob?.cancel()
        mouseScrollJob = scope.launch {
            while (true) {
                try {
                    ensureActive()
                    CallbackBridge.sendScroll(0.0, offset)
                    delay(50)
                } catch (_: Exception) {
                    break
                }
            }
            mouseScrollJob = null
        }
    }
}

/**
 * 游戏内消息发送器
 */
private class GameTextSender(private val scope: CoroutineScope) {
    /**
     * @param text 要发送的文本
     * @param inGame 当前是否处于游戏内，如果在游戏中，则会尝试打开聊天栏
     */
    data class Data(
        val text: String,
        val inGame: Boolean
    )

    private var messageChannel: Channel<Data>? = null
    private var job: Job? = null

    fun cancel() {
        job?.cancel()
        messageChannel?.close()
        messageChannel = null
        job = null
    }

    /**
     * 尝试向游戏发送文本（排队发送）
     */
    fun send(data: Data) {
        if (job?.isActive != true || messageChannel == null) {
            job?.cancel()
            messageChannel?.close()

            messageChannel = Channel(Channel.UNLIMITED)
            job = scope.launch {
                messageChannel?.let { channel ->
                    for ((text, inGame) in channel) {
                        sendMessage(text, inGame)
                    }
                }
            }
        }

        messageChannel?.trySend(data)
    }

    private suspend fun sendMessage(text: String, inGame: Boolean) {
        withContext(Dispatchers.Main) {
            fun sendText() {
                for (ch in text) {
                    LWJGLCharSender.sendChar(ch)
                }
            }

            if (inGame) {
                //根据options.txt中的配置，找到打开聊天栏的键
                //如果找不到，则忽略这次事件
                mapToKeycode(OPEN_CHAT, OPEN_CHAT_VALUE)?.let { openChat ->
                    CallbackBridge.sendKeyPress(openChat)
                    delay(50)
                    sendText()
                    delay(50)
                    LWJGLCharSender.sendEnter()
                }
            } else {
                //如果当前不在游戏内，则直接发送文本
                sendText()
            }
        }
    }
}

@Composable
private fun rememberGameViewModel(
    version: Version,
    onChangeTextInputMode: (TextInputMode?) -> Unit
) = viewModel(
    key = version.toString()
) {
    GameViewModel(version, onChangeTextInputMode)
}

@Composable
private fun rememberEditorViewModel(
    key: String
)= viewModel(
    key = key
) {
    EditorViewModel()
}

@Composable
fun GameScreen(
    version: Version,
    gameHandler: GameHandler,
    showGameInfo: Boolean,
    onInfoBoxClose: () -> Unit,
    logState: LogState,
    onLogStateChange: (LogState) -> Unit,
    textInputMode: TextInputMode,
    isTouchProxyEnabled: Boolean,
    onInputAreaRectUpdated: (IntRect?) -> Unit,
    getAccountName: () -> String?,
    eventViewModel: EventViewModel,
    gamepadViewModel: GamepadViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val viewModel = rememberGameViewModel(version) { mode ->
        eventViewModel.sendEvent(EventViewModel.Event.Game.SwitchIme(mode))
    }
    val editorViewModel = rememberEditorViewModel("ControlEditor_Times=${viewModel.editorRefresh}")
    val cursorMode by ZLBridgeStates.cursorMode.collectAsStateWithLifecycle()
    val isGrabbing = remember(cursorMode) {
        cursorMode == CURSOR_DISABLED
    }
    val joystickMovementViewModel: JoystickMovementViewModel = viewModel()
    val terracottaViewModel = rememberTerracottaViewModel(
        keyTag = gameHandler.toString() + "_Terracotta",
        gameHandler = gameHandler,
        eventViewModel = eventViewModel,
        getUserName = getAccountName
    )

    LaunchedEffect(viewModel.isEditingLayout) {
        val state = viewModel.isEditingLayout
        //向VMActivity同步状态，编辑控制布局时，不会继续处理按键事件
        eventViewModel.sendEvent(EventViewModel.Event.Game.KeyHandle(state.not()))
    }

    SendKeycodeOperation(
        operation = viewModel.sendKeycodeState,
        onChange = { viewModel.sendKeycodeState = it },
        lifecycleScope = viewModel.viewModelScope
    )

    ForceCloseOperation(
        operation = viewModel.forceCloseState,
        onChange = { viewModel.forceCloseState = it },
        onForceClose = {
            Terracotta.setWaiting(false)
            ZLNativeInvoker.jvmExit(0, false)
        },
        text = stringResource(R.string.game_menu_option_force_close_text)
    )

    ReplacementControlOperation(
        operation = viewModel.replacementControlState,
        onChange = { viewModel.replacementControlState = it },
        currentLayout = viewModel.currentControlFile,
        replacementControl = { viewModel.replaceControlLayout(it) }
    )

    TerracottaOperation(
        viewModel = terracottaViewModel
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenSize = rememberBoxSize()

        if (!viewModel.isEditingLayout) {
            if (AllSettings.gamepadControl.state && gamepadViewModel.gamepadEngaged) {
                //手柄事件监听
                GamepadKeyListener(
                    gamepadViewModel = gamepadViewModel,
                    isGrabbing = isGrabbing,
                    onKeyEvent = { events, pressed ->
                        events.forEach { event ->
                            viewModel.onKeyEvent(event, pressed)
                        }
                    },
                    onAction = {
                        viewModel.switchControlLayer(HideLayerWhen.WhenGamepad)
                    }
                )

                //手柄摇杆控制移动事件监听
                GamepadStickMovementListener(
                    gamepadViewModel = gamepadViewModel,
                    isGrabbing = isGrabbing,
                    onKeyEvent = { event, pressed ->
                        viewModel.onKeyEvent(event, pressed)
                    }
                )
            }

            //控制布局层
            ControlBoxLayout(
                modifier = Modifier.fillMaxSize(),
                observedLayout = viewModel.observableLayout,
                eventHandler = viewModel.eventHandler,
                checkOccupiedPointers = { viewModel.occupiedPointers.contains(it) },
                opacity = (AllSettings.controlsOpacity.state.toFloat() / 100f).coerceIn(0f, 1f),
                markPointerAsMoveOnly = { viewModel.moveOnlyPointers.add(it) },
                isUsingJoystick = isGrabbing && AllSettings.enableJoystickControl.state,
                isCursorGrabbing = isGrabbing,
                hideLayerWhen = viewModel.controlLayerHideState,
                isDark = isLauncherInDarkTheme()
            ) {
                //虚拟鼠标控制层
                MouseControlLayout(
                    isTouchProxyEnabled = isTouchProxyEnabled,
                    modifier = Modifier.fillMaxSize(),
                    cursorMode = cursorMode,
                    screenSize = screenSize,
                    onInputAreaRectUpdated = onInputAreaRectUpdated,
                    textInputMode = textInputMode,
                    isMoveOnlyPointer = { viewModel.moveOnlyPointers.contains(it) },
                    onOccupiedPointer = { viewModel.occupiedPointers.add(it) },
                    onReleasePointer = {
                        viewModel.occupiedPointers.remove(it)
                        viewModel.moveOnlyPointers.remove(it)
                    },
                    onMouseMoved = { viewModel.switchControlLayer(HideLayerWhen.WhenMouse) },
                    onTouch = { viewModel.switchControlLayer(HideLayerWhen.None) },
                    gamepadViewModel = gamepadViewModel.takeIf { AllSettings.gamepadControl.state }
                )
            }

            //物品栏触发层
            MinecraftHotbar(
                screenSize = screenSize,
                rule = AllSettings.hotbarRule.state,
                widthPercentage = AllSettings.hotbarWidth.state.hotbarPercentage(),
                heightPercentage = AllSettings.hotbarHeight.state.hotbarPercentage(),
                onClickSlot = { keycode ->
                    CallbackBridge.sendKeyPress(keycode)
                },
                isGrabbing = isGrabbing,
                resolutionRatio = AllSettings.resolutionRatio.state,
                onOccupiedPointer = { viewModel.occupiedPointers.add(it) },
                onReleasePointer = { viewModel.occupiedPointers.remove(it) }
            )

            //摇杆控制层
            viewModel.observableLayout?.let { layout ->
                val special by layout.special.collectAsStateWithLifecycle()
                JoystickControlLayout(
                    screenSize = screenSize,
                    isGrabbing = isGrabbing,
                    special = special,
                    defaultStyle = viewModel.launcherJoystickStyle,
                    hideLayerWhen = viewModel.controlLayerHideState,
                    viewModel = joystickMovementViewModel,
                    onKeyEvent = { event, pressed ->
                        viewModel.onKeyEvent(event, pressed)
                    }
                )
            }
        }

        //陀螺仪控制
        val isGyroscopeAvailable = remember(context) {
            isGyroscopeAvailable(context = context)
        }
        if (isGrabbing && isGyroscopeAvailable && AllSettings.gyroscopeControl.state) {
            GyroscopeReader(
                xEvent = { delta ->
                    CallbackBridge.sendCursorDelta(if (AllSettings.gyroscopeInvertX.state) -delta else delta, 0f)
                },
                yEvent = { delta ->
                    CallbackBridge.sendCursorDelta(0f, if (AllSettings.gyroscopeInvertY.state) delta else -delta)
                },
                sampleRate = AllSettings.gyroscopeSampleRate.state,
                smoothing = AllSettings.gyroscopeSmoothing.state,
                smoothingWindow = AllSettings.gyroscopeSmoothingWindow.state,
                sensitivity = AllSettings.gyroscopeSensitivity.state / 100f
            )
        }

        GameInfoBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(all = 16.dp),
            versionName = version.getVersionName(),
            versionInfo = version.getVersionInfo()?.getInfoString(),
            visible = showGameInfo,
            onClose = onInfoBoxClose
        )

        LogBox(
            enableLog = !viewModel.isEditingLayout && logState.value,
            onClose = {
                onLogStateChange(LogState.CLOSE)
            },
            modifier = Modifier.fillMaxSize()
        )

        GameMenuSubscreen(
            state = viewModel.gameMenuState,
            controlMenuTabIndex = viewModel.controlMenuTabIndex,
            onControlMenuTabChange = { viewModel.controlMenuTabIndex = it },
            gamepadViewModel = gamepadViewModel,
            closeScreen = { viewModel.gameMenuState = MenuState.HIDE },
            onForceClose = { viewModel.forceCloseState = ForceCloseOperation.Show },
            onSwitchLog = { onLogStateChange(logState.next()) },
            enableTerracotta = AllSettings.enableTerracotta.state,
            onOpenTerracottaMenu = { terracottaViewModel.openMenu() },
            onRefreshWindowSize = { eventViewModel.sendEvent(EventViewModel.Event.Game.RefreshSize) },
            onInputMethod = {
                eventViewModel.sendEvent(EventViewModel.Event.Game.SwitchIme(null))
            },
            onSendKeycode = { viewModel.sendKeycodeState = SendKeycodeState.ShowDialog },
            onReplacementControl = { viewModel.replacementControlState = ReplacementControlState.Show },
            onManageJoystick = {
                joystickMovementViewModel.operation = JoystickManageOperation.Manage
            },
            onEditLayout = {
                viewModel.startControlEditor(
                    editorVM = editorViewModel
                )
            }
        )

        if (AllSettings.gamepadControl.state) {
            //手柄事件捕获层
            SimpleGamepadCapture(
                gamepadViewModel = gamepadViewModel
            )
        }

        if (viewModel.isEditingLayout) {
            viewModel.currentControlFile?.let {
                ControlEditor(
                    viewModel = editorViewModel,
                    targetFile = it,
                    exit = {
                        viewModel.exitControlEditor()
                    },
                    menuExit = {
                        editorViewModel.showExitEditorDialog(
                            context = context,
                            onExit = {
                                viewModel.exitControlEditor()
                            }
                        )
                    }
                )
            }
        } else {
            if (AllSettings.showMenuBall.state) {
                //在这里根据设置决定是否启用帧率捕获协程
                val showFps = AllSettings.showFPS.state
                DisposableEffect(showFps) {
                    if (showFps) viewModel.startFpsCapture()
                    onDispose {
                        viewModel.stopFpsCapture()
                    }
                }

                val gameFps: Int? = if (showFps) {
                    viewModel.gameFps
                } else {
                    null
                }

                DraggableGameBall(
                    position = AllSettings.menuBallPos.state,
                    onPositionChanged = {
                        AllSettings.menuBallPos.updateState(it)
                    },
                    onSavePos = {
                        AllSettings.menuBallPos.save()
                    },
                    gameFps = gameFps,
                    showMemory = AllSettings.showMemory.state,
                    opened = viewModel.gameMenuState == MenuState.SHOW,
                    alpha = AllSettings.menuBallOpacity.state / 100f,
                    onClick = {
                        viewModel.switchMenu()
                    }
                )
            }
        }
    }

    //摇杆管理状态操作
    //包含覆盖全屏类UI组件，只能放到顶部
    val saveStyleFailed = stringResource(R.string.game_styles_save_failed)
    val savedStyle = stringResource(R.string.generic_saved)
    JoystickManageOperation(
        operation = joystickMovementViewModel.operation,
        onChanged = { joystickMovementViewModel.operation = it },
        launcherJoystick = viewModel.launcherJoystickStyle,
        onSaveStyle = {
            viewModel.saveLauncherJoystickStyle(
                onFailed = { th ->
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = saveStyleFailed,
                            message = th.getMessageOrToString()
                        )
                    )
                },
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, savedStyle, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    )

    LaunchedEffect(Unit) {
        eventViewModel.events
            .filterIsInstance<EventViewModel.Event.Game>()
            .collect { event ->
                when (event) {
                    is EventViewModel.Event.Game.OnBack -> {
                        if (viewModel.isEditingLayout) {
                            //处于控制布局编辑模式
                            editorViewModel.onBackPressed(
                                context = context,
                                onExit = {
                                    viewModel.exitControlEditor()
                                }
                            )
                        } else if (!AllSettings.showMenuBall.getValue()) {
                            viewModel.switchMenu()
                        } else {
                            //按下返回键
                            val event = ClickEvent(
                                type = ClickEvent.Type.Key,
                                key = ControlEventKeycode.GLFW_KEY_ESCAPE
                            )
                            viewModel.onKeyEvent(event, true)
                            delay(10)
                            viewModel.onKeyEvent(event, false)
                        }
                    }
                    is EventViewModel.Event.Game.OnResume -> {
                        viewModel.clearState()
                    }
                    else -> { /*忽略*/ }
                }
            }
    }
}

@Composable
private fun GameInfoBox(
    versionName: String,
    versionInfo: String?,
    visible: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        BackgroundCard(
            modifier = modifier,
            influencedByBackground = false,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row {
                Row(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(vertical = 16.dp)
                        .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    //提示信息
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.game_loading),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.game_loading_version_name, versionName),
                            style = MaterialTheme.typography.labelLarge
                        )
                        versionInfo?.let { info ->
                            Text(
                                text = stringResource(R.string.game_loading_version_info, info),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                IconButton(
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                    onClick = onClose
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.generic_close)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun PreviewGameInfoBox() {
    MaterialExpressiveTheme {
        GameInfoBox(
            versionName = "1.21.11",
            versionInfo = "1.21.11",
            visible = true,
            onClose = {}
        )
    }
}

/**
 * 鼠标控制层
 * @param isTouchProxyEnabled 是否启用控制代理（TouchController模组支持）
 * @param cursorMode 当前鼠标模式
 * @param textInputMode 输入法状态
 * @param isMoveOnlyPointer 检查指针是否被标记为仅处理滑动事件
 * @param onOccupiedPointer 标记指针已被占用
 * @param onReleasePointer 标记指针已被释放
 * @param onMouseMoved 实体鼠标操作时回调
 * @param onTouch 手指触摸操作鼠标层时回调
 */
@Composable
private fun MouseControlLayout(
    isTouchProxyEnabled: Boolean,
    modifier: Modifier = Modifier,
    cursorMode: Int,
    screenSize: IntSize,
    onInputAreaRectUpdated: (IntRect?) -> Unit,
    textInputMode: TextInputMode,
    isMoveOnlyPointer: (PointerId) -> Boolean,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit,
    onMouseMoved: () -> Unit,
    onTouch: () -> Unit,
    gamepadViewModel: GamepadViewModel?
) {
    Box(
        modifier = modifier
            .then(
                if (isTouchProxyEnabled) {
                    Modifier
                        .touchControllerTouchModifier(
                            screenSize = screenSize
                        )
                        .touchControllerInputModifier(
                            screenSize = screenSize,
                            onInputAreaRectUpdated = onInputAreaRectUpdated,
                        )
                } else Modifier
            )
    ) {

        val capturedSpeedFactor = AllSettings.mouseCaptureSensitivity.state / 100f
        val capturedTapMouseAction = AllSettings.gestureTapMouseAction.state.toAction()
        val capturedLongPressMouseAction = AllSettings.gestureLongPressMouseAction.state.toAction()

        SwitchableMouseLayout(
            modifier = Modifier.fillMaxSize(),
            screenSize = screenSize,
            cursorMode = cursorMode,
            onTouch = onTouch,
            onMouse = onMouseMoved,
            gamepadViewModel = gamepadViewModel,
            onTap = { position ->
                CallbackBridge.putMouseEventWithCoords(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), position.x.sumPosition(), position.y.sumPosition())
            },
            onCapturedTap = {
                if (AllSettings.gestureControl.state) {
                    CallbackBridge.putMouseEvent(capturedTapMouseAction)
                }
            },
            onLongPress = {
                CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), true)
            },
            onLongPressEnd = {
                CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), false)
            },
            onCapturedLongPress = {
                if (AllSettings.gestureControl.state) {
                    CallbackBridge.putMouseEvent(capturedLongPressMouseAction, true)
                }
            },
            onCapturedLongPressEnd = {
                if (AllSettings.gestureControl.state) {
                    CallbackBridge.putMouseEvent(capturedLongPressMouseAction, false)
                }
            },
            onPointerMove = { pos ->
                pos.sendPosition()
            },
            onCapturedMove = { delta ->
                CallbackBridge.sendCursorDelta(
                    delta.x * capturedSpeedFactor,
                    delta.y * capturedSpeedFactor
                )
            },
            onMouseScroll = { scroll ->
                CallbackBridge.sendScroll(scroll.x.toDouble(), scroll.y.toDouble())
            },
            onMouseButton = { button, pressed ->
                val code = LWJGLCharSender.getMouseButton(button) ?: return@SwitchableMouseLayout
                CallbackBridge.sendMouseButton(code.toInt(), pressed)
            },
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer
        )
    }
}

private fun Offset.sendPosition() {
    CallbackBridge.sendCursorPos(x.sumPosition(), y.sumPosition())
}

private fun Float.sumPosition(): Float {
    return (this * (AllSettings.resolutionRatio.state / 100f))
}

/**
 * 摇杆控制层
 * @param isGrabbing 当前游戏是否处于抓获鼠标的状态，根据这个判断是否处于游戏中
 * @param special 由控制布局提供的特殊设定，摇杆会根据这里的配置应用样式
 * @param defaultStyle 由启动器提供的摇杆样式，如果控制布局未提供样式，则使用这个
 * @param hideLayerWhen 复用控制布局的隐藏层逻辑，在鼠标、手柄操作时，根据设置判断是否隐藏摇杆
 * @param viewModel 摇杆移动监听 ViewModel
 * @param onKeyEvent 由
 */
@Composable
private fun JoystickControlLayout(
    isGrabbing: Boolean,
    screenSize: IntSize,
    special: ObservableSpecial,
    defaultStyle: ObservableJoystickStyle,
    hideLayerWhen: HideLayerWhen,
    viewModel: JoystickMovementViewModel,
    onKeyEvent: (ClickEvent, pressed: Boolean) -> Unit
) {
    val joystickStyle by special.joystickStyle.collectAsStateWithLifecycle()

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val hideState = when (hideLayerWhen) {
        HideLayerWhen.WhenMouse -> AllSettings.joystickHideWhenMouse.state
        HideLayerWhen.WhenGamepad -> AllSettings.joystickHideWhenGamepad.state
        HideLayerWhen.None -> false
    }

    //仅在游戏中或者管理摇杆状态中，显示摇杆
    if (
        ((isGrabbing && !hideState) || viewModel.operation == JoystickManageOperation.Manage) &&
        AllSettings.enableJoystickControl.state
    ) {
        val size = AllSettings.joystickControlSize.state.dp
        val x = AllSettings.joystickControlX.state
        val y = AllSettings.joystickControlY.state

        val position = remember(screenSize, size, x, y, layoutDirection) {
            val widgetSize = with(density) {
                val pixelSize = size.roundToPx()
                IntSize(
                    width = pixelSize,
                    height = pixelSize
                )
            }

            val originalPosition = widgetPosition(
                xPercentage = x / 10000f,
                yPercentage = y / 10000f,
                widgetSize = widgetSize,
                screenSize = screenSize
            )

            if (layoutDirection == LayoutDirection.Rtl) {
                //fix: RTL布局下需手动反转X轴，避免使用absoluteOffset
                // 不然控件会飞到屏幕外面去 #1034
                val mirroredX = screenSize.width - originalPosition.x - widgetSize.width
                Offset(x = mirroredX, y = originalPosition.y)
            } else {
                originalPosition
            }
        }

        StyleableJoystick(
            modifier = Modifier
                .offset {
                    IntOffset(x = position.x.toInt(), y = position.y.toInt())
                },
            style = if (AllSettings.joystickUseStyleByLayout.state) {
                //启用后，优先使用控制布局提供的样式
                joystickStyle ?: defaultStyle
            } else {
                defaultStyle
            },
            size = size,
            onDirectionChanged = { direction ->
                viewModel.onListen(direction)
            },
            deadZoneRatio = AllSettings.joystickDeadZoneRatio.state / 100f,
            lockThreshold = AllSettings.joystickLockThreshold.state / 100f,
            canLock = AllSettings.joystickControlCanLock.state,
            onCanLock = { lock ->
                if (AllSettings.joystickControlLockSpring.state) {
                    mapToControlEvent(SPRING, SPRING_VALUE)?.let { key ->
                        val event = ClickEvent(
                            type = ClickEvent.Type.Key,
                            key = key
                        )
                        if (lock) {
                            onKeyEvent(event, true)
                        } else {
                            onKeyEvent(event, false)
                        }
                    }
                }
            }
        )
    }

    JoystickDirectionListener(
        viewModel = viewModel,
        isGrabbing = isGrabbing,
        onKeyEvent = { event, pressed ->
            onKeyEvent(event, pressed)
        }
    )
}