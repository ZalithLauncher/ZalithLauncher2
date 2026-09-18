package org.lwjgl.glfw;

import static com.movtery.zalithlauncher.bridge.ZLBridgeStatesKt.CURSOR_DISABLED;
import static com.movtery.zalithlauncher.bridge.ZLBridgeStatesKt.CURSOR_ENABLED;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.movtery.inputmap.keycodes.LwjglGlfwKeycode;
import com.movtery.zalithlauncher.BuildKeys;
import com.movtery.zalithlauncher.bridge.CursorShape;
import com.movtery.zalithlauncher.bridge.LoggerBridge;
import com.movtery.zalithlauncher.bridge.NativeLibraryLoader;
import com.movtery.zalithlauncher.bridge.ZLBridgeStates;
import com.movtery.zalithlauncher.bridge.ZLNativeInvoker;
import com.movtery.zalithlauncher.context.ContextsKt;
import com.movtery.zalithlauncher.game.input.EfficientAndroidLWJGLKeycode;
import com.movtery.zalithlauncher.game.sdl.DirectGamepadEnableHandler;
import com.movtery.zalithlauncher.game.sdl.SdlBridge;
import com.movtery.zalithlauncher.setting.AllSettings;

import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLSurface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.function.Consumer;

import dalvik.annotation.optimization.CriticalNative;

@Keep
public class CallbackBridge {
    private static final int GLFW_IBEAM_CURSOR = 0x36002;
    private static final int GLFW_HAND_CURSOR = 0x36004;
    private static final int GLFW_CROSSHAIR_CURSOR = 0x36003;
    private static final int GLFW_RESIZE_NS_CURSOR = 0x36006;
    private static final int GLFW_RESIZE_EW_CURSOR = 0x36005;
    private static final int GLFW_RESIZE_ALL_CURSOR = 0x36009;
    private static final int GLFW_NOT_ALLOWED_CURSOR = 0x3600A;
    private static final int GLFW_ARROW_CURSOR = 0x36001;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static volatile boolean isGrabbing = false;
    private static final Consumer<Boolean> grabListener = isGrabbing ->
            ZLBridgeStates.changeCursorMode(isGrabbing ? CURSOR_DISABLED : CURSOR_ENABLED);

    private static int cursorShape = GLFW_ARROW_CURSOR;
    private static final Consumer<CursorShape> cursorShapeListener = ZLBridgeStates::changeCursorShape;

    private static void postFrameCallbackDelayed(Choreographer.FrameCallback callback, long delayMillis) {
        MAIN_HANDLER.post(() -> Choreographer.getInstance().postFrameCallbackDelayed(callback, delayMillis));
    }
    
    public static final int CLIPBOARD_COPY = 2000;
    public static final int CLIPBOARD_PASTE = 2001;
    public static final int CLIPBOARD_OPEN = 2002;

    // SDL launcher integration. See the AAMC reference implementation:
    // https://github.com/AngelAuraMC/Amethyst-Android
    // Notification types
    public static final int NOTIF_TYPE_SDL = 0;

    // Notification actions
    public static final int ACTION_INIT_LAUNCHER_INTEGRATION = 0;
    public static final int ACTION_SEND_TEXTBOX_RECT = 1;

    // org.lwjgl.sdl.SDLInit 通过这两个常量调用 nativeNotifyLauncher
    public static final int SDL = NOTIF_TYPE_SDL;
    public static final int INIT = ACTION_INIT_LAUNCHER_INTEGRATION;

    /**
     * 由 JRE 侧（sdl_hook JNI）调用的通知入口。
     * @return 通知是否处理成功
     */
    @SuppressWarnings("unused")
    @Keep
    public static boolean notifyLauncher(int type, int... action) {
        if (action == null || action.length == 0) {
            LoggerBridge.append("ZalithLauncher: SDL notification has no action");
            return false;
        }
        switch (type) {
            case NOTIF_TYPE_SDL:
                if (action[0] == ACTION_INIT_LAUNCHER_INTEGRATION) {
                    if (!SdlBridge.markSdlInitialized()) {
                        return true;
                    }
                    try {
                        LoggerBridge.append("ZalithLauncher: loading SDL3");
                        System.loadLibrary("SDL3");
                        LoggerBridge.append("ZalithLauncher: loading SDL2");
                        System.loadLibrary("SDL2");
                        LoggerBridge.append("ZalithLauncher: setting up SDL JNI");
                        SdlBridge.setupJNI();
                        LoggerBridge.append("ZalithLauncher: binding SDL surface");
                        SdlBridge.setSdlEnabled(true);
                        SDLSurface surface = SDLActivity.getSDLSurface();
                        if (surface != null) {
                            surface.surfaceChanged();
                            if (windowWidth > 0 && windowHeight > 0) {
                                surface.nativeResize(windowWidth, windowHeight);
                            }
                        }
                        LoggerBridge.append("ZalithLauncher: SDL support enabled!");
                        return true;
                    } catch (Throwable e) {
                        SdlBridge.setSdlEnabled(false);
                        SdlBridge.clearSdlInitialized();
                        StringWriter trace = new StringWriter();
                        e.printStackTrace(new PrintWriter(trace));
                        LoggerBridge.append("ZalithLauncher: SDL launcher integration is unavailable:\n" + trace);
                    }
                }
                if (action[0] == ACTION_SEND_TEXTBOX_RECT) {
                    // TODO: 输入框位置同步（后续接入）
                }
        }
        return false;
    }

    /**
     * org.lwjgl.sdl.SDLInit（LWJGL 3.4.1 的 SDL Java 绑定）调用的入口，转发到 {@link #notifyLauncher}。
     * 注意：LWJGL 组件内声明为 native，运行时以本实现为准（避免依赖额外 C 符号）。
     */
    @SuppressWarnings("unused")
    @Keep
    public static void nativeNotifyLauncher(int type, int... action) {
        notifyLauncher(type, action);
    }
    
    public static volatile int windowWidth, windowHeight;
    public static volatile int physicalWidth, physicalHeight;
    public static float mouseX, mouseY, deltaX, deltaY;
    private static int sMouseButtonState = 0;
    public volatile static boolean holdingAlt, holdingCapslock, holdingCtrl,
            holdingNumlock, holdingShift;

    // GLFW direct gamepad 共享缓冲
    public static final ByteBuffer sGamepadButtonBuffer;
    public static final FloatBuffer sGamepadAxisBuffer;
    public static boolean sGamepadDirectInput = false;
    // Use a weak reference here to avoid possibly statically referencing a Context.
    private static @Nullable WeakReference<DirectGamepadEnableHandler> sDirectGamepadEnableHandler;

    public static void putMouseEventWithCoords(int button, float x, float y) {
        sendCursorPos(x, y);
        putMouseEvent(button);
    }

    public static void putMouseEvent(int button) {
        putMouseEvent(button, true);
        postFrameCallbackDelayed(l -> putMouseEvent(button, false), 33);
    }
    
    public static void putMouseEvent(int button, boolean isDown) {
        sendMouseKeycode(button, CallbackBridge.getCurrentMods(), isDown);
    }


    public static void sendCursorPos(float x, float y) {
        mouseX = x;
        mouseY = y;
        deltaX = 0f;
        deltaY = 0f;
        nativeSendCursorPos(mouseX, mouseY);
        if (!SdlBridge.getSdlEnabled()) return;
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_MOVE, x, y, false);
    }

    public static void sendCursorDelta(float x, float y) {
        deltaX = x;
        deltaY = y;
        mouseX += x;
        mouseY += y;
        nativeSendCursorPos(mouseX, mouseY);
        if (!SdlBridge.getSdlEnabled()) return;
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_MOVE, x, y, true);
    }

    public static void sendKeycode(int keycode, char keychar, int scancode, int modifiers, boolean isDown) {
        if (keycode > LwjglGlfwKeycode.GLFW_KEY_UNKNOWN && keycode <= LwjglGlfwKeycode.GLFW_KEY_LAST) {
            nativeSendKey(keycode, scancode, isDown ? 1 : 0, modifiers);
        }
        if (isDown && !Character.isISOControl(keychar)) {
            nativeSendCharMods(keychar, modifiers);
            nativeSendChar(keychar);
        }
        if (!SdlBridge.getSdlEnabled()) return;
        int androidKeycode = EfficientAndroidLWJGLKeycode.getSdlAndroidKeycode(keycode);
        if (androidKeycode == KeyEvent.KEYCODE_UNKNOWN) return;
        try {
            if (isDown) {
                SDLActivity.onNativeKeyDown(androidKeycode);
                // 游戏只在 SDL_EVENT_TEXT_INPUT 里插入字符，仅 KEYDOWN 不会有任何输入
                if (isTextEventChar(keychar, modifiers) && SDLActivity.isSDLTextInputActive()) {
                    SDLActivity.onNativeTextInput(String.valueOf(keychar));
                }
            } else {
                SDLActivity.onNativeKeyUp(androidKeycode);
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean isTextEventChar(char keychar, int modifiers) {
        if (Character.isISOControl(keychar)) return false;
        return (modifiers & LwjglGlfwKeycode.GLFW_MOD_CONTROL) == 0;
    }

    public static void sendChar(char keychar, int modifiers){
        nativeSendCharMods(keychar, modifiers);
        nativeSendChar(keychar);
        if (!SdlBridge.getSdlEnabled()) return;
        SDLActivity.onNativeKeyDown(EfficientAndroidLWJGLKeycode.getAndroidKeycode(keychar));
        SDLActivity.onNativeKeyUp(EfficientAndroidLWJGLKeycode.getAndroidKeycode(keychar));
    }

    public static void sendKeyPress(int keyCode, int modifiers, boolean status) {
        sendKeyPress(keyCode, 0, modifiers, status);
    }

    public static void sendKeyPress(int keyCode, int scancode, int modifiers, boolean status) {
        sendKeyPress(keyCode, '\u0000', scancode, modifiers, status);
    }

    public static void sendKeyPress(int keyCode, char keyChar, int scancode, int modifiers, boolean status) {
        CallbackBridge.sendKeycode(keyCode, keyChar, scancode, modifiers, status);
    }

    public static void sendKeyPress(int keyCode) {
        sendKeyPress(keyCode, CallbackBridge.getCurrentMods(), true);
        sendKeyPress(keyCode, CallbackBridge.getCurrentMods(), false);
    }

    public static void sendMouseButton(int button, boolean status) {
        CallbackBridge.sendMouseKeycode(button, CallbackBridge.getCurrentMods(), status);
    }

    public static void sendMouseKeycode(int button, int modifiers, boolean isDown) {
        // if (isGrabbing()) DEBUG_STRING.append("MouseGrabStrace: " + android.util.Log.getStackTraceString(new Throwable()) + "\n");
        nativeSendMouseButton(button, isDown ? 1 : 0, modifiers);
        // SDL 输入双路（按键状态累积后一次性上报，SDL 需要 MotionEvent.getButtonState()）
        if (!SdlBridge.getSdlEnabled()) return;
        int aKey = -1;
        switch (button) {
            case LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT:
                aKey = MotionEvent.BUTTON_PRIMARY;
                break;
            case LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT:
                aKey = MotionEvent.BUTTON_SECONDARY;
                break;
            case LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE:
                aKey = MotionEvent.BUTTON_TERTIARY;
                break;
            // Yes, back and forward are flipped, for some reason it's just flipped on SDL, don't ask
            case LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_5:
                aKey = MotionEvent.BUTTON_BACK;
                break;
            case LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_4:
                aKey = MotionEvent.BUTTON_FORWARD;
                break;
        }
        if (aKey != -1) {
            if (isDown) {
                sMouseButtonState |= aKey;
            } else {
                sMouseButtonState &= ~aKey;
            }
            SDLActivity.onNativeMouse(sMouseButtonState, isDown ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP, mouseX, mouseY, false);
        }
    }

    public static void sendMouseKeycode(int keycode) {
        sendMouseKeycode(keycode, CallbackBridge.getCurrentMods(), true);
        sendMouseKeycode(keycode, CallbackBridge.getCurrentMods(), false);
    }
    
    public static void sendScroll(double xoffset, double yoffset) {
        nativeSendScroll(xoffset, yoffset);
        // SDL 输入双路
        if (!SdlBridge.getSdlEnabled()) return;
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_SCROLL, (float) xoffset, (float) yoffset, false);
    }

    public static void sendUpdateWindowSize(int w, int h) {
        windowWidth = w;
        windowHeight = h;
        nativeSendScreenSize(w, h);
    }

    public static boolean isGrabbing() {
        // Avoid going through the JNI each time.
        return isGrabbing;
    }

    public static void resetInputState() {
        nativeResetInputState();
        if (SdlBridge.getSdlEnabled() && sMouseButtonState != 0) {
            SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mouseX, mouseY, false);
        }
        deltaX = 0f;
        deltaY = 0f;
        sMouseButtonState = 0;
        holdingAlt = false;
        holdingCapslock = false;
        holdingCtrl = false;
        holdingNumlock = false;
        holdingShift = false;
    }

    // Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    public static @Nullable String accessAndroidClipboard(int type, String copy) {
        ClipboardManager clipboard = (ClipboardManager) ContextsKt.getGlobalContext().getSystemService(Context.CLIPBOARD_SERVICE);
        String result = null;
        switch (type) {
            case CLIPBOARD_COPY:
                ClipData clip = ClipData.newPlainText(BuildKeys.INSTANCE.getLAUNCHER_IDENTIFIER(), copy);
                clipboard.setPrimaryClip(clip);
                break;
            case CLIPBOARD_PASTE:
                if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    result = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                } else {
                    result = "";
                }
                break;
            case CLIPBOARD_OPEN:
                ZLNativeInvoker.openLink(copy);
                break;
        }
        return result;
    }

    public static int getCurrentMods() {
        int currMods = 0;
        if (holdingAlt) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_ALT;
        } if (holdingCapslock) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_CAPS_LOCK;
        } if (holdingCtrl) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_CONTROL;
        } if (holdingNumlock) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_NUM_LOCK;
        } if (holdingShift) {
            currMods |= LwjglGlfwKeycode.GLFW_MOD_SHIFT;
        }
        return currMods;
    }

    public static void setModifiers(int keyCode, boolean isDown){
        switch (keyCode){
            case LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT:
                CallbackBridge.holdingShift = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL:
                CallbackBridge.holdingCtrl = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT:
                CallbackBridge.holdingAlt = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK:
                CallbackBridge.holdingCapslock = isDown;
                return;

            case LwjglGlfwKeycode.GLFW_KEY_NUM_LOCK:
                CallbackBridge.holdingNumlock = isDown;
        }
    }

    public static void setDirectGamepadEnableHandler(@Nullable DirectGamepadEnableHandler handler) {
        sDirectGamepadEnableHandler = new WeakReference<>(handler);
    }

    public static void clearSdlBridgeState() {
        sGamepadDirectInput = false;
        sDirectGamepadEnableHandler = null;
        sMouseButtonState = 0;
        deltaX = 0f;
        deltaY = 0f;
    }

    //Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    private static void onDirectInputEnable() {
        LoggerBridge.append("ZalithLauncher: Direct gamepad input enabled");
        sGamepadDirectInput = true;
        DirectGamepadEnableHandler enableHandler =
                sDirectGamepadEnableHandler == null ? null : sDirectGamepadEnableHandler.get();
        if (enableHandler != null) {
            enableHandler.onDirectGamepadEnabled();
        }
    }

    //Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    private static void onGrabStateChanged(final boolean grabbing) {
        System.out.println("Grab callback received: " + grabbing);
        isGrabbing = grabbing;
        deltaX = 0f;
        deltaY = 0f;
        postFrameCallbackDelayed((time) -> {
            // If the grab re-changed, skip notify process
            if(isGrabbing != grabbing) return;

            System.out.println("Grab changed : " + grabbing);
            synchronized (grabListener) {
                grabListener.accept(isGrabbing);
            }

        }, 16);
    }

    //Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    private static void onCursorShapeChanged(final int shape) {
        cursorShape = shape;
        postFrameCallbackDelayed((time) -> {
            if (cursorShape != shape) return;

            synchronized (cursorShapeListener) {
                CursorShape shape1;
                switch (cursorShape) {
                    // IBeam
                    case GLFW_IBEAM_CURSOR:
                        shape1 = CursorShape.IBeam;
                        break;
                    // Hand
                    case GLFW_HAND_CURSOR:
                        shape1 = CursorShape.Hand;
                        break;
                    // Cross Hair
                    case GLFW_CROSSHAIR_CURSOR:
                        shape1 = CursorShape.CrossHair;
                        break;
                    // Resize NS
                    case GLFW_RESIZE_NS_CURSOR:
                        shape1 = CursorShape.ResizeNS;
                        break;
                    // Resize EW
                    case GLFW_RESIZE_EW_CURSOR:
                        shape1 = CursorShape.ResizeEW;
                        break;
                    // Resize All
                    case GLFW_RESIZE_ALL_CURSOR:
                        shape1 = CursorShape.ResizeAll;
                        break;
                    // Not Allowed
                    case GLFW_NOT_ALLOWED_CURSOR:
                        shape1 = CursorShape.NotAllowed;
                        break;
                    // Arrow
                    case GLFW_ARROW_CURSOR:
                    default:
                        shape1 = CursorShape.Arrow;
                }

                cursorShapeListener.accept(shape1);
            }
        }, 16);
    }

    private static GraphicOutputListener sGraphicOutputListener;
    public static void setGraphicOutputListener(GraphicOutputListener listener) {
        sGraphicOutputListener = listener;
    }

    //Called from JRE side
    @SuppressWarnings("unused")
    @Keep
    private static void onGraphicOutput() {
        if (sGraphicOutputListener != null) {
            sGraphicOutputListener.onGraphicOutput();
        }
    }

    @Keep @CriticalNative public static native void nativeSetUseInputStackQueue(boolean useInputStackQueue);

    @Keep @CriticalNative private static native boolean nativeSendChar(char codepoint);
    // GLFW: GLFWCharModsCallback deprecated, but is Minecraft still use?
    @Keep @CriticalNative private static native boolean nativeSendCharMods(char codepoint, int mods);
    @Keep @CriticalNative private static native void nativeSendKey(int key, int scancode, int action, int mods);
    // private static native void nativeSendCursorEnter(int entered);
    @Keep @CriticalNative private static native void nativeSendCursorPos(float x, float y);
    @Keep @CriticalNative private static native void nativeSendMouseButton(int button, int action, int mods);
    @Keep @CriticalNative private static native void nativeResetInputState();
    @Keep @CriticalNative private static native void nativeSendScroll(double xoffset, double yoffset);
    @Keep @CriticalNative private static native void nativeSendScreenSize(int width, int height);
    @Keep public static native void nativeSetWindowAttrib(int attrib, int value);
    @Keep public static native void nativeSetGrabbing(boolean grab);
    @Keep public static native int getCurrentFps();

    private static native ByteBuffer nativeCreateGamepadButtonBuffer();
    private static native ByteBuffer nativeCreateGamepadAxisBuffer();

    static {
        NativeLibraryLoader.loadPojavLib();
        sGamepadButtonBuffer = nativeCreateGamepadButtonBuffer();
        sGamepadAxisBuffer = nativeCreateGamepadAxisBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
    }
}

