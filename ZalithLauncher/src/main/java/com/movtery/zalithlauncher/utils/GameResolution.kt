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

package com.movtery.zalithlauncher.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.movtery.zalithlauncher.game.launch.MCOptions
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.ResolutionRule
import kotlin.math.roundToInt

private const val CUSTOM_RESOLUTION_MIN_SCALE = 0.2f    // 20%
private const val CUSTOM_RESOLUTION_MAX_SCALE = 3f      // 300%

/**
 * 自定义分辨率的合法范围
 */
fun customResolutionRange(screenSide: Int): IntRange {
    val min = getDisplayFriendlyRes(
        (screenSide * CUSTOM_RESOLUTION_MIN_SCALE).roundToInt().coerceAtLeast(2),
        1f
    ).coerceAtLeast(2)
    val max = (screenSide * CUSTOM_RESOLUTION_MAX_SCALE).roundToInt()
    return min..max.coerceAtLeast(min)
}

/**
 * 游戏画面的显示布局与坐标换算
 * @param renderSize 游戏渲染分辨率
 * @param displaySize 游戏画面在屏幕上的显示尺寸
 * @param offset 显示区域相对屏幕左上角的偏移（等比缩放居中时的黑边）
 */
data class GameDisplayLayout(
    val renderSize: IntSize,
    val displaySize: IntSize,
    val offset: IntOffset
) {
    val scaleX: Float
        get() = if (renderSize.width > 0) displaySize.width / renderSize.width.toFloat() else 1f

    val scaleY: Float
        get() = if (renderSize.height > 0) displaySize.height / renderSize.height.toFloat() else 1f

    /**
     * 将全屏布局坐标换算为游戏渲染坐标
     */
    fun mapToGame(position: Offset): Offset {
        val sX = if (displaySize.width > 0) renderSize.width / displaySize.width.toFloat() else 1f
        val sY = if (displaySize.height > 0) renderSize.height / displaySize.height.toFloat() else 1f
        return Offset(
            (position.x - offset.x) * sX,
            (position.y - offset.y) * sY
        )
    }

    /**
     * 将游戏渲染坐标换算为屏幕物理坐标
     */
    fun mapToScreen(position: Offset): Offset {
        return Offset(
            offset.x + position.x * scaleX,
            offset.y + position.y * scaleY
        )
    }
}

/**
 * 根据规则计算游戏的渲染分辨率
 * 自定义规则下，宽高限制在屏幕真实尺寸的 20%–300% 以内，
 * 非正数视为未初始化，回退为屏幕尺寸
 */
fun computeGameRenderSize(
    screenSize: IntSize,
    rule: ResolutionRule,
    percentage: Int,
    customWidth: Int,
    customHeight: Int
): IntSize {
    return if (rule == ResolutionRule.CUSTOM) {
        fun fixCustomSide(value: Int, screenSide: Int): Int {
            val sanitized = value.takeIf { it > 0 } ?: screenSide
            return getDisplayFriendlyRes(sanitized.coerceIn(customResolutionRange(screenSide)), 1f)
        }
        IntSize(
            width = fixCustomSide(customWidth, screenSize.width),
            height = fixCustomSide(customHeight, screenSize.height)
        )
    } else {
        val scale = percentage / 100f
        IntSize(
            width = getDisplayFriendlyRes(screenSize.width, scale),
            height = getDisplayFriendlyRes(screenSize.height, scale)
        )
    }
}

/**
 * 读取当前设置，计算游戏的渲染分辨率
 */
fun computeGameRenderSize(screenSize: IntSize): IntSize = computeGameRenderSize(
    screenSize = screenSize,
    rule = AllSettings.resolutionRule.getValue(),
    percentage = AllSettings.resolutionRatio.getValue(),
    customWidth = AllSettings.customResolutionWidth.getValue(),
    customHeight = AllSettings.customResolutionHeight.getValue()
)

/**
 * 可观察版本：任意相关设置变化时重新计算游戏的渲染分辨率
 */
@Composable
fun rememberGameRenderSize(screenSize: IntSize): IntSize {
    val rule = AllSettings.resolutionRule.state
    val percentage = AllSettings.resolutionRatio.state
    val customWidth = AllSettings.customResolutionWidth.state
    val customHeight = AllSettings.customResolutionHeight.state
    return remember(screenSize, rule, percentage, customWidth, customHeight) {
        computeGameRenderSize(screenSize, rule, percentage, customWidth, customHeight)
    }
}

/**
 * 根据规则计算游戏画面的显示布局
 * 百分比规则铺满全屏
 * 自定义规则等比缩放至屏幕可容纳的最大尺寸并居中
 */
fun computeGameDisplayLayout(
    screenSize: IntSize,
    rule: ResolutionRule,
    percentage: Int,
    customWidth: Int,
    customHeight: Int
): GameDisplayLayout {
    val renderSize = computeGameRenderSize(screenSize, rule, percentage, customWidth, customHeight)
    return if (rule == ResolutionRule.CUSTOM) {
        computeGameDisplayLayout(screenSize, renderSize)
    } else {
        GameDisplayLayout(
            renderSize = renderSize,
            displaySize = screenSize,
            offset = IntOffset.Zero
        )
    }
}

/**
 * 读取当前设置，计算游戏画面的显示布局
 */
fun currentGameDisplayLayout(screenSize: IntSize): GameDisplayLayout = computeGameDisplayLayout(
    screenSize = screenSize,
    rule = AllSettings.resolutionRule.getValue(),
    percentage = AllSettings.resolutionRatio.getValue(),
    customWidth = AllSettings.customResolutionWidth.getValue(),
    customHeight = AllSettings.customResolutionHeight.getValue()
)

/**
 * 可观察版本：任意相关设置变化时重新计算游戏画面的显示布局
 */
@Composable
fun rememberGameDisplayLayout(screenSize: IntSize): GameDisplayLayout {
    val rule = AllSettings.resolutionRule.state
    val percentage = AllSettings.resolutionRatio.state
    val customWidth = AllSettings.customResolutionWidth.state
    val customHeight = AllSettings.customResolutionHeight.state
    return remember(screenSize, rule, percentage, customWidth, customHeight) {
        computeGameDisplayLayout(screenSize, rule, percentage, customWidth, customHeight)
    }
}

/**
 * 计算自定义分辨率的显示布局
 */
fun computeGameDisplayLayout(screenSize: IntSize, renderSize: IntSize): GameDisplayLayout {
    val scale = minOf(
        screenSize.width / renderSize.width.toFloat(),
        screenSize.height / renderSize.height.toFloat()
    )
    val displaySize = IntSize(
        width = (renderSize.width * scale).roundToInt().coerceIn(1, screenSize.width),
        height = (renderSize.height * scale).roundToInt().coerceIn(1, screenSize.height)
    )
    return GameDisplayLayout(
        renderSize = renderSize,
        displaySize = displaySize,
        offset = IntOffset(
            x = (screenSize.width - displaySize.width) / 2,
            y = (screenSize.height - displaySize.height) / 2
        )
    )
}

/**
 * 获取设备屏幕的真实宽高（px）
 */
fun getRealScreenSize(context: Context): IntSize {
    val metrics = context.resources.displayMetrics
    return IntSize(metrics.widthPixels, metrics.heightPixels)
}

/**
 * 自定义分辨率尚未初始化时，以设备屏幕真实宽高填充
 */
fun ensureCustomResolutionInitialized(context: Context) {
    if (AllSettings.customResolutionWidth.getValue() > 0 &&
        AllSettings.customResolutionHeight.getValue() > 0
    ) return

    val screenSize = getRealScreenSize(context)
    AllSettings.customResolutionWidth.save(screenSize.width)
    AllSettings.customResolutionHeight.save(screenSize.height)
}

/**
 * 根据渲染分辨率、配置项与旧版字体模式计算 Minecraft 的有效 GUI 缩放倍数 (GUI Scale)
 *
 * 遵循 Minecraft 原版 ScaledResolution 算法规范：
 * 1. 当 configuredGuiScale 在有效范围内（1..maxScale）时使用配置值，否则（0 或超出）自动取 maxScale；
 * 2. 在旧版本（< 1.13）Unicode 模式下（如 CJK 语言环境或开启 forceUnicodeFont），受官方漏洞 MC-124469 影响，
 *    大于 1 的奇数缩放倍数会强制递减 1 降为偶数；1.13-pre6 修复该问题后现代高版本不再降级。
 *
 * @param renderWidth 游戏渲染宽度
 * @param renderHeight 游戏渲染高度
 * @param configuredGuiScale options.txt 中配置的 guiScale（0 表示 Auto）
 * @param isLegacyUnicode 是否处于旧版本 Unicode 强制降级模式 (MC-124469)
 */
fun computeMCGuiScale(
    renderWidth: Int,
    renderHeight: Int,
    configuredGuiScale: Int = 0,
    isLegacyUnicode: Boolean = false
): Int {
    if (renderWidth <= 0 || renderHeight <= 0) return 4
    val maxScale = minOf(renderWidth / 320, renderHeight / 240).coerceAtLeast(1)
    var scale = if (configuredGuiScale in 1..maxScale) {
        configuredGuiScale
    } else {
        maxScale
    }
    if (isLegacyUnicode && scale % 2 != 0 && scale != 1) {
        scale--
    }
    return scale
}

/**
 * 读取当前 MCOptions 配置，计算指定渲染尺寸下的有效 GUI 缩放倍数
 */
fun currentMCGuiScale(renderWidth: Int, renderHeight: Int): Int {
    val configuredScale = MCOptions.get("guiScale")?.toIntOrNull() ?: 0
    return computeMCGuiScale(
        renderWidth = renderWidth,
        renderHeight = renderHeight,
        configuredGuiScale = configuredScale,
        isLegacyUnicode = MCOptions.isLegacyUnicodeScale()
    )
}
