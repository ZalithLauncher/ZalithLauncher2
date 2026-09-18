package com.movtery.zalithlauncher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SdlCursorRegistry {
    private static final Map<Long, Integer> HANDLE_MAP = new ConcurrentHashMap<>();

    private SdlCursorRegistry() {
        throw new UnsupportedOperationException();
    }

    public static final int GLFW_ARROW_CURSOR          = 0x00036001;
    public static final int GLFW_IBEAM_CURSOR          = 0x00036002;
    public static final int GLFW_CROSSHAIR_CURSOR      = 0x00036003;
    public static final int GLFW_HAND_CURSOR           = 0x00036004;
    public static final int GLFW_RESIZE_EW_CURSOR      = 0x00036005;
    public static final int GLFW_RESIZE_NS_CURSOR      = 0x00036006;
    public static final int GLFW_RESIZE_ALL_CURSOR     = 0x00036009;
    public static final int GLFW_NOT_ALLOWED_CURSOR    = 0x0003600A;

    public static void register(long handle, int sdlSystemCursor) {
        if (handle != 0) HANDLE_MAP.put(handle, sdlSystemCursor);
    }

    public static void unregister(long handle) {
        if (handle != 0) HANDLE_MAP.remove(handle);
    }

    public static int toGlfwShape(long handle) {
        Integer sdlShape = HANDLE_MAP.get(handle);
        if (sdlShape == null) return -1;
        return toGlfwShape(sdlShape);
    }

    public static int toGlfwShape(int sdlSystemCursor) {
        switch (sdlSystemCursor) {
            case 1: return GLFW_IBEAM_CURSOR;        // SDL_SYSTEM_CURSOR_TEXT
            case 3: return GLFW_CROSSHAIR_CURSOR;    // SDL_SYSTEM_CURSOR_CROSSHAIR
            case 11: return GLFW_HAND_CURSOR;        // SDL_SYSTEM_CURSOR_POINTER
            case 7: return GLFW_RESIZE_EW_CURSOR;    // SDL_SYSTEM_CURSOR_EW_RESIZE
            case 8: return GLFW_RESIZE_NS_CURSOR;    // SDL_SYSTEM_CURSOR_NS_RESIZE
            case 9: return GLFW_RESIZE_ALL_CURSOR;   // SDL_SYSTEM_CURSOR_MOVE
            case 10: return GLFW_NOT_ALLOWED_CURSOR; // SDL_SYSTEM_CURSOR_NOT_ALLOWED
            default: return GLFW_ARROW_CURSOR;       // DEFAULT/WAIT/PROGRESS/单边 resize 等
        }
    }
}
