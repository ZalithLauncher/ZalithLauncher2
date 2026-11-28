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

    static native void nativeSetCameraDistance(long ptr, float value);

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

    static native long nativeSceneCreate();

    static native void nativeSceneDestroy(long scene);

    static native long nativeModelCreatePlayer(byte[] skin, int arm);

    static native long nativeModelCreateProp(byte[] glb);

    static native long nativeModelCreatePropWithSkin(byte[] glb, byte[] skin, int arm);

    /** 销毁未加入场景的模型句柄 */
    static native void nativeModelDestroy(long model);

    /** 消费模型句柄，返回实例句柄；失败返回 -1 */
    static native int nativeSceneAddModel(long scene, long model);

    static native void nativeSceneRemoveModel(long scene, int id);

    /** 相机模式：0 正交（zoomOrFov = 屏宽覆盖世界单位数），1 透视（zoomOrFov = 垂直 FOV 度数） */
    static native void nativeSceneSetCamera(long scene, float tx, float ty, float tz, float azimuth, float elevation, float distance, int mode, float zoomOrFov);

    static native void nativeSceneSetLighting(long scene, float azimuth, float elevation, float brightness, float softness);

    static native void nativeSceneSetSuperSample(long scene, float value);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneSetSkin(long scene, int id, byte[] skin, int arm);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneSetCape(long scene, int id, byte[] cape);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneSetTexture(long scene, int id, String material, byte[] texture);

    static native void nativeSceneSetPosition(long scene, int id, float x, float y, float z);

    /** 欧拉旋转，单位为度 */
    static native void nativeSceneSetRotation(long scene, int id, float pitch, float yaw, float roll);

    static native void nativeSceneSetScale(long scene, int id, float scale);

    static native void nativeSceneSetOuterLayer(long scene, int id, boolean enabled);

    static native void nativeSceneSetGazeScreen(long scene, int id, float x, float y, boolean hasTarget);

    static native void nativeSceneSetGazeScene(long scene, int id, float x, float y, float z, boolean hasTarget);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneClipPlay(long scene, int id, String name, float speed);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneClipPause(long scene, int id, String name);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneClipStop(long scene, int id, String name);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneClipSetSpeed(long scene, int id, String name, float speed);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneClipSetLoop(long scene, int id, String name, boolean loop);

    /** 返回错误消息，null 表示成功 */
    static native String nativeSceneClipSetWeight(long scene, int id, String name, float weight);

    static native boolean nativeSceneClipIsPlaying(long scene, int id, String name);

    static native void nativeSceneSquashTap(long scene, int id, float now);

    static native void nativeScenePlayInteract(long scene, int id);

    static native void nativeSceneRender(long scene, int width, int height, float dt, Bitmap bitmap);

    /** 返回命中实例句柄，未命中返回 -1 */
    static native int nativeSceneHitTest(long scene, float x, float y);
}
