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

package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jakewharton.processphoenix.ProcessPhoenix
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.coroutine.DataBridge
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.input.AWTCharSender
import com.movtery.zalithlauncher.game.input.CharacterSenderStrategy
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.launch.GameLauncher
import com.movtery.zalithlauncher.game.launch.GameService
import com.movtery.zalithlauncher.game.launch.JvmLaunchInfo
import com.movtery.zalithlauncher.game.launch.JvmLauncher
import com.movtery.zalithlauncher.game.launch.LaunchConfig
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.game.launch.handler.AbstractHandler
import com.movtery.zalithlauncher.game.launch.handler.GameHandler
import com.movtery.zalithlauncher.game.launch.handler.HandlerType
import com.movtery.zalithlauncher.game.launch.handler.JVMHandler
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.plugin.PluginLoader
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.terracotta.TerracottaVPNService
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.base.ObserveFullScreenSetting
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.ui.control.input.HidableInputLayout
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.ui.screens.game.elements.OpenFolderLayer
import com.movtery.zalithlauncher.ui.screens.game.elements.OpenFolderOperation
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.utils.device.PhysicalMouseChecker
import com.movtery.zalithlauncher.utils.getDisplayFriendlyRes
import com.movtery.zalithlauncher.utils.getParcelableSafely
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import android.graphics.Color as NativeColor


private const val INTENT_RUN_GAME = "BUNDLE_RUN_GAME"
private const val INTENT_RUN_JAR = "INTENT_RUN_JAR"
private const val INTENT_GAME_CONFIG = "INTENT_GAME_CONFIG"
private const val INTENT_JAR_INFO = "INTENT_JAR_INFO"

data class LaunchSession(
    val launcher: Launcher,
    val handler: AbstractHandler,
    val inputSender: CharacterSenderStrategy
)

/**
 * 一些关键状态须在此存放
 */
class VMViewModel : ViewModel() {
    var isRunning = false

    /**
     * 是否允许VMActivity处理按键
     */
    var keyHandle = true

    val screenSizeBridge = DataBridge<IntSize>()
    var screenSize: IntSize = IntSize.Zero

    private val _onConfigurationChanged = MutableStateFlow(false)
    val onConfigurationChanged = _onConfigurationChanged.asStateFlow()

    fun onConfigurationChanged(value: Boolean = true) {
        _onConfigurationChanged.update { value }
    }

    var sender: CharacterSenderStrategy = LWJGLCharSender
        private set

    private val _openFolderOperation = MutableStateFlow<OpenFolderOperation>(OpenFolderOperation.None)
    /** 启动器内浏览目录（将文件导入该目录） */
    val openFolderOperation = _openFolderOperation.asStateFlow()

    /** 关闭浏览目录 */
    fun clearFolder() {
        _openFolderOperation.update {
            OpenFolderOperation.None
        }
    }

    private var _session: LaunchSession? = null
    val session: LaunchSession
        get() = _session ?: error("LaunchSession not initialized")

    fun initSession(
        activity: VMActivity,
        bundle: Bundle,
        errorViewModel: ErrorViewModel,
        eventViewModel: EventViewModel,
        gamepadViewModel: GamepadViewModel,
        exitListener: (Int, Boolean) -> Unit,
    ) {
        if (_session != null) return

        _session = when {
            bundle.getBoolean(INTENT_RUN_GAME) -> {
                val config: LaunchConfig = bundle.getParcelableSafely(INTENT_GAME_CONFIG, LaunchConfig::class.java)
                    ?: throw IllegalStateException("No launch config has been set.")

                val launcher = GameLauncher(
                    activity = activity,
                    config = config,
                    onExit = { code, isSignal ->
                        if (code == 0) {
                            val finishedCount = AllSettings.finishedGame.getValue()
                            if (finishedCount < Int.MAX_VALUE)  {
                                AllSettings.finishedGame.save(finishedCount + 1)
                            }
                        }
                        exitListener(code, isSignal)
                    },
                    openPath = { folder ->
                        _openFolderOperation.update {
                            OpenFolderOperation.OpenFolder(folder)
                        }
                    }
                )

                sender = LWJGLCharSender

                LaunchSession(
                    launcher = launcher,
                    handler = GameHandler(
                        activity = activity,
                        config = config,
                        errorViewModel = errorViewModel,
                        eventViewModel = eventViewModel,
                        gamepadViewModel = gamepadViewModel,
                        gameLauncher = launcher
                    ) { code ->
                        exitListener(code, false)
                    },
                    inputSender = LWJGLCharSender
                )
            }
            bundle.getBoolean(INTENT_RUN_JAR) -> {
                val jvmLaunchInfo: JvmLaunchInfo = bundle.getParcelableSafely(INTENT_JAR_INFO, JvmLaunchInfo::class.java)
                    ?: throw IllegalStateException("No launch jar info has been set.")

                val launcher = JvmLauncher(
                    context = activity,
                    jvmLaunchInfo = jvmLaunchInfo,
                    onExit = exitListener,
                    openPath = { folder ->
                        _openFolderOperation.update {
                            OpenFolderOperation.OpenFolder(folder)
                        }
                    }
                )

                sender = AWTCharSender

                LaunchSession(
                    launcher = launcher,
                    handler = JVMHandler(
                        jvmLauncher = launcher,
                        errorViewModel = errorViewModel,
                        eventViewModel = eventViewModel,
                    ) { code ->
                        exitListener(code, false)
                    },
                    inputSender = AWTCharSender
                )
            }
            else -> error("Unknown VM launch mode")
        }
    }

    /**
     * 当前输入法开启状态
     */
    var textInputMode by mutableStateOf(TextInputMode.DISABLE)

    fun disableInputMode() {
        if (textInputMode == TextInputMode.ENABLE) textInputMode = TextInputMode.DISABLE
    }


    /**
     * 直接发送文本到游戏
     */
    private fun String.sendText() {
        forEach { char ->
            sender.sendChar(char)
        }
    }

    fun sendInputText(text: String) {
        text.sendText()
    }

    fun sendBackspace() {
        sender.sendBackspace()
    }

    fun sendEnder() {
        sender.sendEnter()
    }

    /**
     * 仅处理特殊按键
     */
    fun handleSpecialKey(keyEvent: KeyEvent) {
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_ENTER -> {
                //忽略掉删除事件，避免状态不同步
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> sender.sendLeft()
            KeyEvent.KEYCODE_DPAD_RIGHT -> sender.sendRight()
            KeyEvent.KEYCODE_DPAD_UP -> sender.sendUp()
            KeyEvent.KEYCODE_DPAD_DOWN -> sender.sendDown()

            KeyEvent.KEYCODE_TAB -> sender.sendTab()

            else -> sender.sendOther(keyEvent)
        }
    }

    /**
     * 返回这个按键事件是否允许被处理
     */
    fun keyCanHandle(keyEvent: KeyEvent): Boolean {
        val keyCode = keyEvent.keyCode
        //因为输入法选区时会发出Shift键的事件，但同步为游戏内的文本进行选区会比较复杂
        //比如选区时没法拿到当前输入框选择了哪些文本，极容易导致输入框与游戏内的文本出现状态差异
        //这类比较打破预期的情况应该尽量避免，所以应该忽略Shift
        val isShift = keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
        //避免处理Ctrl，大部分输入法不支持处理这个，而在游戏内可能会影响到指针位置
        val isCtrl = keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT
        return !isShift && !isCtrl
    }
}

class VMActivity : BaseAppCompatActivity(), SurfaceTextureListener, SurfaceHolder.Callback {
    override fun isIgnoreNotch(): Boolean = AllSettings.gameFullScreen.getValue()

    private val errorViewModel: ErrorViewModel by viewModels()

    private val eventViewModel: EventViewModel by viewModels()
    /**
     * 手柄状态存储 ViewModel
     */
    private val gamepadViewModel: GamepadViewModel by viewModels()

    private val vmViewModel: VMViewModel by viewModels()

    private var applySizeToSurface: ((width: Int, height: Int) -> Unit)? = null

    private inline fun <T> withHandler(block: AbstractHandler.() -> T): T {
        return vmViewModel.session.handler.block()
    }

    private inline fun <T> withLauncher(block: Launcher.() -> T): T {
        return vmViewModel.session.launcher.block()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //加载渲染器
        Renderers.init()
        //加载插件
        PluginLoader.loadAllPlugins(this, false)
        refreshData()

        //初始化物理鼠标连接检查器
        PhysicalMouseChecker.initChecker(this)

        //启动前台服务，防止后台网络中断
        startForegroundService(Intent(this, GameService::class.java))

        val bundle = intent.extras ?: throw IllegalStateException("Unknown VM launch state!")

        vmViewModel.initSession(
            activity = this,
            bundle = bundle,
            errorViewModel = errorViewModel,
            eventViewModel = eventViewModel,
            gamepadViewModel = gamepadViewModel,
            exitListener = { exitCode: Int, isSignal: Boolean ->
                stopAllService()
                if (exitCode != 0) {
                    val logPath = withLauncher {
                        getLogFile().absolutePath
                    }
                    showExitMessage(this@VMActivity, exitCode, isSignal, logPath)
                } else {
                    //重启启动器
                    ProcessPhoenix.triggerRebirth(this@VMActivity)
                }
            }
        )

        //设置画面渲染输出回调
        CallbackBridge.setGraphicOutputListener {
            withHandler { onGraphicOutput() }
        }

        window?.apply {
            setBackgroundDrawable(NativeColor.BLACK.toDrawable())
            if (AllSettings.sustainedPerformance.getValue()) {
                setSustainedPerformanceMode(true)
            }
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 防止系统息屏
        }

        val logFile = withLauncher { getLogFile() }
        logFile.parentFile?.mkdirs() // 换过一次日志文件路径，此处创建父目录是必要的
        if (!logFile.exists() && !logFile.createNewFile()) throw IOException("Failed to create a new log file")
        LoggerBridge.start(logFile.absolutePath)

        //错误信息展示
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.errorEvents.collect { tm ->
                    errorViewModel.showErrorDialog(
                        context = this@VMActivity,
                        tm = tm
                    )
                }
            }
        }

        lifecycleScope.launch {
            //开始接收事件
            eventViewModel.events.collect { event ->
                when (event) {
                    is EventViewModel.Event.Game.RefreshSize -> {
                        vmViewModel.onConfigurationChanged()
                    }
                    is EventViewModel.Event.Game.SwitchIme -> {
                        vmViewModel.textInputMode = event.mode ?: vmViewModel.textInputMode.switch()
                    }
                    is EventViewModel.Event.Game.KeyHandle -> {
                        vmViewModel.keyHandle = event.handle
                    }
                    else -> { /* Ignore */ }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (vmViewModel.textInputMode == TextInputMode.ENABLE) {
                    //那应该是想退出输入框了
                    vmViewModel.disableInputMode()
                    return
                }
                if (!vmViewModel.keyHandle) return

                eventViewModel.sendEvent(EventViewModel.Event.Game.OnBack)
            }
        })

        //关闭菜单之后，每次启动游戏都提醒，防止部分人误触了不知道怎么解决 >:(
        if (!AllSettings.showMenuBall.getValue()) {
            Toast.makeText(
                this@VMActivity,
                getString(R.string.game_menu_option_show_menu_hided),
                Toast.LENGTH_LONG
            ).show()
        }

        setContent {
            ZalithLauncherTheme {
                ObserveFullScreenSetting(AllSettings.gameFullScreen.state)
                Screen {
                    withHandler {
                        ComposableLayout(vmViewModel.textInputMode)
                    }

                    if (vmViewModel.textInputMode == TextInputMode.ENABLE) {
                        //输入栏控制区域
                        HidableInputLayout(
                            onSend = { text ->
                                vmViewModel.sendInputText(text)
                            },
                            onBackspace = {
                                vmViewModel.sendBackspace()
                            },
                            onEnter = {
                                vmViewModel.sendEnder()
                            },
                            onClose = {
                                vmViewModel.textInputMode = TextInputMode.DISABLE
                            }
                        )
                    }

                    //鼠标变更为抓获模式时，应该关闭输入框
                    val cursorMode by ZLBridgeStates.cursorMode.collectAsStateWithLifecycle()
                    LaunchedEffect(cursorMode) {
                        if (cursorMode == CURSOR_DISABLED) vmViewModel.disableInputMode()
                    }

                    val operation by vmViewModel.openFolderOperation.collectAsStateWithLifecycle()
                    OpenFolderLayer(
                        modifier = Modifier.fillMaxSize(),
                        operation = operation,
                        requestClose = {
                            vmViewModel.clearFolder()
                        },
                        lifecycleScope = lifecycleScope
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        withHandler { onResume() }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_FOCUSED, 1)
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onPause() {
        super.onPause()
        withHandler { onPause() }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_FOCUSED, 0)
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onStart() {
        super.onStart()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onStop() {
        super.onStop()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_FOCUSED, if (hasFocus) 0 else 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        vmViewModel.onConfigurationChanged()
    }

    override fun onPostResume() {
        super.onPostResume()
        lifecycleScope.launch {
            if (vmViewModel.isRunning) {
                delay(50L.milliseconds)
                withContext(Dispatchers.Main) {
                    refreshWindowSize(screenSize = vmViewModel.screenSize)
                }
            }
        }
    }

    private fun refreshWindowSize(
        screenSize: IntSize
    ): IntSize {
        fun getDisplayPixels(pixels: Int): Int {
            return withHandler {
                when (type) {
                    HandlerType.GAME -> getDisplayFriendlyRes(pixels, AllSettings.resolutionRatio.getValue().toFloat() / 100f)
                    HandlerType.JVM -> getDisplayFriendlyRes(pixels, 0.8f)
                }
            }
        }

        val windowWidth = getDisplayPixels(screenSize.width)
        val windowHeight = getDisplayPixels(screenSize.height)
        applySizeToSurface?.invoke(windowWidth, windowHeight)
        ZLBridgeStates.onWindowChange()
        CallbackBridge.sendUpdateWindowSize(windowWidth, windowHeight)

        return IntSize(windowWidth, windowHeight)
    }

    override fun onDestroy() {
        stopAllService()
        withHandler { onDestroy() }
        super.onDestroy()
    }

    private fun stopAllService() {
        stopService(Intent(this, GameService::class.java))
        if (TerracottaVPNService.isRunning()) {
            val vpnIntent = Intent(this, TerracottaVPNService::class.java).apply {
                action = TerracottaVPNService.ACTION_STOP
            }
            startForegroundService(vpnIntent)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!vmViewModel.keyHandle) return super.dispatchKeyEvent(event)

        val isPressed = event.action == KeyEvent.ACTION_DOWN

        val code = AllSettings.physicalKeyImeCode.state
        if (isPressed && code != null && event.keyCode == code) {
            //用户按下了绑定呼出输入法的按键
            //开启或关闭输入法
            vmViewModel.textInputMode = vmViewModel.textInputMode.switch()
            return true
        }
        if (vmViewModel.textInputMode == TextInputMode.ENABLE) {
            if (isPressed && !vmViewModel.keyCanHandle(event)) {
                return super.dispatchKeyEvent(event)
            }

            if (isPressed) {
                vmViewModel.handleSpecialKey(event)
            }

            if (event.keyCode == KeyEvent.KEYCODE_TAB) {
                //对于Tab键，为了避免选中其他的组件，这里应该直接拦截
                return true
            }
            //在输入文本的时候，应该避免继续处理按键事件
            //否则输入法的一些功能键会失效
            return super.dispatchKeyEvent(event)
        }
        event.device?.let {
            val source = event.source
            if (source and InputDevice.SOURCE_MOUSE_RELATIVE == InputDevice.SOURCE_MOUSE_RELATIVE ||
                source and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE) {

                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    //一些系统会将鼠标右键当成KEYCODE_BACK来处理，需要在这里进行拦截
                    //然后发送真实的鼠标右键
                    withHandler { sendMouseRight(isPressed) }
                    return false
                }
            }
        }
        withHandler {
            if (shouldIgnoreKeyEvent(event)) {
                return super.dispatchKeyEvent(event)
            }
        }
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (vmViewModel.isRunning) {
            ZLBridge.setupBridgeWindow(Surface(surface))
            return
        }
        vmViewModel.isRunning = true

        withHandler { mIsSurfaceDestroyed = false }
        lifecycleScope.launch(Dispatchers.Default) {
            val screenSize = vmViewModel.screenSizeBridge.awaitData()
            val currentSize = refreshWindowSize(screenSize = screenSize)
            withHandler {
                execute(
                    surface = Surface(surface),
                    screenSize = currentSize,
                    scope = lifecycleScope
                )
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        withHandler { mIsSurfaceDestroyed = true }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val surface = holder.surface
        if (vmViewModel.isRunning) {
            ZLBridge.setupBridgeWindow(surface)
            return
        }
        vmViewModel.isRunning = true

        withHandler { mIsSurfaceDestroyed = false }
        lifecycleScope.launch(Dispatchers.Default) {
            val screenSize = vmViewModel.screenSizeBridge.awaitData()
            val currentSize = refreshWindowSize(screenSize = screenSize)
            withHandler {
                execute(
                    surface = surface,
                    screenSize = currentSize,
                    scope = lifecycleScope
                )
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        withHandler { mIsSurfaceDestroyed = true }
    }

    @Composable
    private fun Screen(
        content: @Composable () -> Unit = {}
    ) {
        val imeInsets = WindowInsets.ime
        val inputArea by withHandler { inputArea }.collectAsStateWithLifecycle()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val screenSize = rememberBoxSize()

            val changed by vmViewModel.onConfigurationChanged.collectAsStateWithLifecycle()
            LaunchedEffect(screenSize, changed) {
                vmViewModel.screenSize = screenSize
                vmViewModel.screenSizeBridge.provideData(screenSize)
                if (changed) {
                    refreshWindowSize(screenSize = screenSize)
                    vmViewModel.onConfigurationChanged(false)
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .absoluteOffset {
                        val area = inputArea ?: return@absoluteOffset IntOffset.Zero
                        val imeHeight = imeInsets.getBottom(this@absoluteOffset)
                        val bottomDistance = screenSize.height - area.bottom
                        val bottomPadding = (imeHeight - bottomDistance).coerceAtLeast(0)
                        IntOffset(0, -bottomPadding)
                    },
                factory = { context ->
                    if (AllSettings.useSurfaceView.getValue()) {
                        //使用 SurfaceView 渲染
                        SurfaceView(context).apply {
                            holder.addCallback(this@VMActivity)
                        }.also { view ->
                            applySizeToSurface = { width, height ->
                                view.holder.setFixedSize(width, height)
                            }
                        }
                    } else {
                        TextureView(context).apply {
                            isOpaque = true
                            alpha = 1.0f

                            surfaceTextureListener = this@VMActivity
                        }.also { view ->
                            applySizeToSurface = { width, height ->
                                view.surfaceTexture?.setDefaultBufferSize(width, height)
                            }
                        }
                    }
                }
            )

            content()
        }
    }
}

/**
 * 让VMActivity进入运行游戏模式
 * @param version 指定版本
 */
fun runGame(
    context: Context,
    version: Version,
    account: Account,
) {
    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(INTENT_RUN_GAME, true)
        putExtra(INTENT_GAME_CONFIG, LaunchConfig(version, account))
    }
    context.startActivity(intent)
}

/**
 * 让VMActivity进入运行Jar模式
 * @param jarFile 指定 jar 文件
 * @param jreName 指定使用的 Java 环境，null 则为自动选择
 * @param customArgs 指定 jvm 参数
 */
fun runJar(
    context: Context,
    jarFile: File,
    jreName: String? = null,
    customArgs: String? = null
) {
    RuntimesManager.getExactJreName(8) ?: run {
        Toast.makeText(context, R.string.multirt_no_java_8, Toast.LENGTH_SHORT).show()
        return
    }

    val jvmArgsPrefix = customArgs?.let { "$it " } ?: ""
    val jvmArgs = "$jvmArgsPrefix-jar ${jarFile.absolutePath}"

    val jvmLaunchInfo = JvmLaunchInfo(
        jvmArgs = jvmArgs,
        jreName = jreName
    )

    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(INTENT_RUN_JAR, true)
        putExtra(INTENT_JAR_INFO, jvmLaunchInfo)
    }
    context.startActivity(intent)
}