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

package com.movtery.zalithlauncher.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.petlib.ArmModel
import com.movtery.petlib.PetAnimation
import com.movtery.petlib.PetController
import com.movtery.petlib.PetGazeRouter
import com.movtery.petlib.PetPreview
import com.movtery.petlib.PetTapReaction

/**
 * 设置皮肤，若皮肤数据传 null，则在 assets 中加载默认皮肤
 * @param skin PNG 皮肤数据
 * @param arm 手臂模型类别，为 null 则自动检测类型
 * @param onError 失败时在渲染线程的回调
 */
fun PetController.setSkinDefaultable(
    context: Context,
    skin: ByteArray?,
    arm: ArmModel?,
    onError: ((String) -> Unit)? = null
) {
    val nonNullSkin = skin ?: context.loadDefaultSkin(arm)
    setSkin(nonNullSkin, arm, onError)
}

private fun Context.loadDefaultSkin(arm: ArmModel?): ByteArray = if (arm == ArmModel.Slim) {
    assets.open("skin/alex.png")
} else {
    assets.open("skin/steve.png")
}.use { it.readBytes() }

/** 原生默认取景的保守参考框（模型像素） */
private const val PET_MODEL_FRAME_SIZE = 44f

/**
 * 按 Compose 实际可用尺寸计算相对摄像机距离。
 * 宽或高任一方向受限都会拉远相机。
 */
internal fun calculatePetCameraDistance(width: Int, height: Int, safeAreaPx: Float): Float {
    if (width <= 0 || height <= 0) return 1f
    val marginLimit = minOf(width, height) / 2f
    val margin = if (safeAreaPx.isFinite()) {
        safeAreaPx.coerceIn(0f, marginLimit)
    } else {
        0f
    }
    val availableWidth = (width - 2f * margin).coerceAtLeast(1f)
    val availableHeight = (height - 2f * margin).coerceAtLeast(1f)
    val originalScale = width / PET_MODEL_FRAME_SIZE
    val safeScale = minOf(availableWidth, availableHeight) / PET_MODEL_FRAME_SIZE
    return originalScale / minOf(originalScale, safeScale)
}

/**
 * 玩家模型预览的中介组件：内部组装控制器生命周期、皮肤与披风加载、
 * 全局鼠标视线跟随与手势交互，调用端只需提供渲染数据。
 *
 * @param skin 皮肤数据，null 时使用按臂型区分的默认皮肤
 * @param cape 披风数据，null 时无披风
 * @param arm 手臂模型类别，null 按皮肤自动检测
 * @param yaw 整体偏航角（弧度），参数变化时模型转向该角度
 * @param animation 循环动画，null 不播放
 * @param animationSpeed 动画播放速度倍率
 * @param tapReaction 点按模型命中时的反馈形式
 * @param dragYaw 是否响应拖拽旋转偏航
 * @param gazeEnabled 是否跟随 Activity 全局鼠标位置（窗口坐标）；
 * 独立窗口中的预览与事件泵不同坐标系，应传 false
 * @param safeArea 人物四周预留的安全距离
 */
@Composable
fun PetModelView(
    modifier: Modifier = Modifier,
    skin: ByteArray? = null,
    cape: ByteArray? = null,
    arm: ArmModel? = null,
    yaw: Float = 0f,
    animation: PetAnimation? = PetAnimation.Idle,
    animationSpeed: Float = 1f,
    tapReaction: PetTapReaction = PetTapReaction.Squash,
    dragYaw: Boolean = true,
    gazeEnabled: Boolean = true,
    safeArea: Dp = 8.dp,
) {
    val context = LocalContext.current
    val petController = remember {
        PetController.create(skin ?: context.loadDefaultSkin(arm), arm)
    }
    if (petController == null) {
        Box(modifier)
        return
    }
    DisposableEffect(petController) {
        onDispose { petController.close() }
    }

    LaunchedEffect(petController, skin, arm) {
        petController.setSkinDefaultable(context, skin, arm)
    }
    LaunchedEffect(petController, cape) {
        petController.setCape(cape)
    }
    LaunchedEffect(petController, animation, animationSpeed) {
        if (animation != null) {
            petController.playAnimation(animation, animationSpeed)
        }
    }
    LaunchedEffect(petController, yaw) {
        petController.setModelYaw(yaw)
    }

    var previewBounds by remember { mutableStateOf<Rect?>(null) }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    val safeAreaPx = with(LocalDensity.current) { safeArea.toPx() }
    LaunchedEffect(petController, previewSize, safeAreaPx) {
        petController.setCameraDistance(
            calculatePetCameraDistance(previewSize.width, previewSize.height, safeAreaPx)
        )
    }
    DisposableEffect(petController, gazeEnabled) {
        if (!gazeEnabled) {
            onDispose { }
        } else {
            val gazeTarget = object : PetGazeRouter.Target {
                override fun onCursor(x: Float, y: Float) {
                    val bounds = previewBounds ?: return
                    petController.setGaze(Pair(x - bounds.left, y - bounds.top))
                }

                override fun onGazeExpired() {
                    petController.setGaze(null)
                }
            }
            PetGazeRouter.register(gazeTarget)
            onDispose { PetGazeRouter.unregister(gazeTarget) }
        }
    }

    PetPreview(
        controller = petController,
        modifier = modifier.onGloballyPositioned {
            previewBounds = it.boundsInWindow()
            previewSize = it.size
        },
        tapReaction = tapReaction,
        dragYaw = dragYaw
    )
}
