package com.movtery.zalithlauncher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CursorRegistry {
    private static final Map<Long, Integer> CURSOR_MAP = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> SHAPE_MAP = new ConcurrentHashMap<>();

    private static final AtomicLong NEXT_ID = new AtomicLong(4L);

    public static final int GLFW_ARROW_CURSOR = 0x00036001;

    public static final long DEFAULT_CURSOR;

    static {
        DEFAULT_CURSOR = registerCursor(GLFW_ARROW_CURSOR);
    }

    public static long registerCursor(int glfwShape) {
        if (SHAPE_MAP.containsKey(glfwShape)) {
            return SHAPE_MAP.get(glfwShape);
        }

        long id = NEXT_ID.getAndIncrement();
        CURSOR_MAP.put(id, glfwShape);
        SHAPE_MAP.put(glfwShape, id);
        return id;
    }

    public static int getShape(long cursor) {
        return CURSOR_MAP.getOrDefault(cursor, GLFW_ARROW_CURSOR);
    }

    public static long getDefaultCursor() {
        return DEFAULT_CURSOR;
    }
}
