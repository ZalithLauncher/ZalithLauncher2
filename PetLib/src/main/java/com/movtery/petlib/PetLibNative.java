/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.petlib;

import android.graphics.Bitmap;

/**
 * PetLib 原生接口，静态方法与 libpetlib 的 JNI 符号一一对应。
 *
 * <p>加载失败不向外抛出（Compose 预览、缺 ABI 的 so 等环境必现失败）：
 * 可用性经 {@link #isAvailable()} 查询，失败原因经 {@link #loadError()} 获取。
 * 失败属进程级不可恢复状态，加载只尝试一次。
 */
final class PetLibNative {
    private static final boolean available;
    private static final Throwable loadError;

    static {
        Throwable error = null;
        try {
            System.loadLibrary("petlib");
        } catch (Throwable t) {
            error = t;
        }
        loadError = error;
        available = error == null;
    }

    private PetLibNative() {
    }

    /** 原生库是否加载成功；false 时所有 native 方法均不可调用 */
    static boolean isAvailable() {
        return available;
    }

    /** 加载失败的原因，仅当 {@link #isAvailable()} 为 false 时非 null */
    static Throwable loadError() {
        return loadError;
    }

    static native long nativeCreate(byte[] skin, int arm);

    static native void nativeDestroy(long ptr);

    static native String nativeSetSkin(long ptr, byte[] skin, int arm);

    static native String nativeSetCape(long ptr, byte[] cape);

    static native void nativeSetLighting(long ptr, float azimuth, float elevation, float brightness, float softness);

    static native void nativeSetSuperSample(long ptr, float value);

    static native void nativeSetOuterLayer(long ptr, boolean enabled);

    static native void nativeSetTau(long ptr, float gaze, float yaw, float anim, float recenter);

    static native void nativeSetHeadRange(long ptr, float x, float y);

    static native void nativeSetSquashDepth(long ptr, float depth);

    static native void nativeSetGaze(long ptr, float x, float y, boolean hasTarget);

    static native void nativeSetYaw(long ptr, float yaw);

    static native float nativeGetYaw(long ptr);

    static native void nativePlayAnimation(long ptr, int id, float speed, boolean headFollow);

    static native void nativeStopAnimation(long ptr);

    static native void nativeSquashTap(long ptr, float now);

    static native void nativePlaySwing(long ptr, float now);

    static native void nativePlayInteract(long ptr);

    /**
     * 渲染一帧，结果直写 bitmap 的像素内存（RGBA_8888、行连续、尺寸匹配）
     */
    static native void nativeRender(long ptr, int width, int height, float now, float dt, Bitmap bitmap);

    static native boolean nativeHitTest(long ptr, float x, float y);
}
