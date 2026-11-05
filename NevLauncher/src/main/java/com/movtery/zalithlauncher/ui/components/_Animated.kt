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

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.utils.animation.EaseOutFluent
import com.movtery.zalithlauncher.utils.animation.EnterTranslateEasing
import com.movtery.zalithlauncher.utils.animation.SwapTimeline

/**
 * 子项动画的进入方向
 */
enum class AnimatedDirection {
    Auto, Top, Bottom, Left, Right, None
}

@Stable
class AnimatedContext(
    val progress: State<Float>,
    val alphaProgress: State<Float>,
    val distanceScale: State<Float>,
    val defaultDirection: AnimatedDirection
)

/**
 * 创建独立于容器使用的 [AnimatedContext]，适用于整块内容的进出场动画
 * @param isVisible 进出场状态，true 为进入
 * @param delayMillis 动画延迟(ms)
 */
@Composable
fun rememberAnimatedContext(
    isVisible: Boolean,
    delayMillis: Int = 0
): AnimatedContext = rememberAnimatedContext(
    isVisible = isVisible,
    index = 0,
    extraDelayMillis = delayMillis,
    defaultDirection = AnimatedDirection.Top
)

@Composable
private fun rememberAnimatedContext(
    isVisible: Boolean,
    index: Int,
    extraDelayMillis: Int,
    defaultDirection: AnimatedDirection
): AnimatedContext {
    val progress = swapAnimateProgressAsState(
        isVisible = isVisible,
        index = index,
        extraDelayMillis = extraDelayMillis
    )
    val alphaProgress = swapAnimateAlphaProgressAsState(
        isVisible = isVisible,
        index = index,
        extraDelayMillis = extraDelayMillis
    )
    val distanceScale = swapAnimateDistanceScaleAsState(
        isVisible = isVisible,
        index = index
    )
    return remember(progress, alphaProgress, distanceScale) {
        AnimatedContext(progress, alphaProgress, distanceScale, defaultDirection)
    }
}

/**
 * 对子项应用进出场动画，按方向位移并伴随淡入
 * @param from 进入方向，[AnimatedDirection.Auto] 时使用容器默认方向
 * @param distance 位移距离，按方向取正负号
 * @param fade 是否伴随淡入
 */
fun Modifier.animated(
    ctx: AnimatedContext,
    from: AnimatedDirection = AnimatedDirection.Auto,
    distance: Dp = 40.dp,
    fade: Boolean = true
): Modifier = this.graphicsLayer {
    val progress = ctx.progress.value
    val direction = if (from == AnimatedDirection.Auto) ctx.defaultDirection else from
    if (direction != AnimatedDirection.None) {
        //退场位移距离小于进场：进场滑入全程，退场仅向进入方向溜出一段短距离
        //位移比例随动画平滑过渡，打断反转时从当前值回退，不会因比例硬切换而跳变
        val hiddenOffset = distance.toPx() * ctx.distanceScale.value * (1f - progress)
        when (direction) {
            AnimatedDirection.Top -> {
                translationX = 0f
                translationY = -hiddenOffset
            }
            AnimatedDirection.Bottom -> {
                translationX = 0f
                translationY = hiddenOffset
            }
            AnimatedDirection.Left -> {
                translationX = -hiddenOffset
                translationY = 0f
            }
            AnimatedDirection.Right -> {
                translationX = hiddenOffset
                translationY = 0f
            }
            else -> {
                translationX = 0f
                translationY = 0f
            }
        }
    } else {
        translationX = 0f
        translationY = 0f
    }
    alpha = if (fade) ctx.alphaProgress.value else 1f
}

@Composable
private fun swapAnimateProgressAsState(
    isVisible: Boolean,
    index: Int,
    extraDelayMillis: Int
): State<Float> {
    if (SwapTimeline.isSwapClosed()) return rememberUpdatedState(newValue = 1f)
    val spec: TweenSpec<Float> = if (isVisible) {
        tween(
            durationMillis = SwapTimeline.swapTime(SwapTimeline.ENTER_ITEM),
            delayMillis = SwapTimeline.enterStagger(index) + SwapTimeline.swapTime(extraDelayMillis),
            easing = EnterTranslateEasing
        )
    } else {
        tween(
            durationMillis = SwapTimeline.swapTime(SwapTimeline.EXIT_ITEM),
            delayMillis = SwapTimeline.exitStagger(index),
            easing = LinearEasing
        )
    }
    return swapAnimateProgressAsState(isVisible = isVisible, animationSpec = spec)
}

@Composable
fun swapAnimateProgressAsState(
    isVisible: Boolean,
    animationSpec: DurationBasedAnimationSpec<Float>
): State<Float> {
    if (SwapTimeline.isSwapClosed()) return rememberUpdatedState(newValue = 1f)
    return animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = animationSpec
    )
}

@Composable
private fun swapAnimateAlphaProgressAsState(
    isVisible: Boolean,
    index: Int,
    extraDelayMillis: Int
): State<Float> {
    if (SwapTimeline.isSwapClosed()) return rememberUpdatedState(newValue = 1f)
    val spec: TweenSpec<Float> = if (isVisible) {
        tween(
            durationMillis = SwapTimeline.swapTime(SwapTimeline.ENTER_ALPHA),
            delayMillis = SwapTimeline.enterStagger(index) + SwapTimeline.swapTime(extraDelayMillis),
            easing = EaseOutFluent(2)
        )
    } else {
        tween(
            durationMillis = SwapTimeline.swapTime(SwapTimeline.EXIT_ALPHA),
            delayMillis = SwapTimeline.exitStagger(index),
            easing = LinearEasing
        )
    }
    return swapAnimateAlphaProgressAsState(isVisible = isVisible, animationSpec = spec)
}

@Composable
fun swapAnimateAlphaProgressAsState(
    isVisible: Boolean,
    animationSpec: DurationBasedAnimationSpec<Float>
): State<Float> {
    if (SwapTimeline.isSwapClosed()) return rememberUpdatedState(newValue = 1f)
    return animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = animationSpec
    )
}

@Composable
private fun swapAnimateDistanceScaleAsState(
    isVisible: Boolean,
    index: Int
): State<Float> {
    if (SwapTimeline.isSwapClosed()) return rememberUpdatedState(newValue = 1f)
    val spec: TweenSpec<Float> = if (isVisible) {
        tween(
            durationMillis = SwapTimeline.swapTime(SwapTimeline.ENTER_ITEM),
            delayMillis = SwapTimeline.enterStagger(index),
            easing = LinearEasing
        )
    } else {
        tween(
            durationMillis = SwapTimeline.swapTime(SwapTimeline.EXIT_ITEM),
            delayMillis = SwapTimeline.exitStagger(index),
            easing = LinearEasing
        )
    }
    return animateFloatAsState(
        targetValue = if (isVisible) 1f else SwapTimeline.EXIT_DISTANCE_SCALE,
        animationSpec = spec
    )
}


/**
 * 支持连锁动画效果的垂直布局容器，自动为每个子项应用递增的动画延迟
 * @param isVisible 控制容器内所有子项的动画触发状态
 * @param stagger 是否启用子项连锁错峰
 */
@Composable
fun AnimatedColumn(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    stagger: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable AnimatedColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        AnimatedColumnScopeImpl(this, isVisible, stagger).content()
    }
}

interface AnimatedColumnScope : ColumnScope {
    /**
     * 在 [AnimatedColumn] 中声明一个具有动画效果的子项
     * @param delay 指定当前子项的进场额外延迟时间(ms)
     */
    @Composable
    fun AnimatedItem(
        delay: Int? = null,
        content: @Composable ColumnScope.(ctx: AnimatedContext) -> Unit
    )
}

private class AnimatedColumnScopeImpl(
    columnScope: ColumnScope,
    private val isVisible: Boolean,
    private val stagger: Boolean
) : AnimatedColumnScope, ColumnScope by columnScope {
    private var itemIndex = 0

    @Composable
    override fun AnimatedItem(
        delay: Int?,
        content: @Composable ColumnScope.(ctx: AnimatedContext) -> Unit
    ) {
        val currentIndex = itemIndex++
        val ctx = rememberAnimatedContext(
            isVisible = isVisible,
            index = if (stagger) currentIndex else 0,
            extraDelayMillis = delay ?: 0,
            defaultDirection = AnimatedDirection.Top
        )
        content(ctx)
    }
}


/**
 * 支持连锁动画效果的水平布局容器，自动为每个子项应用递增的动画延迟
 * @param isVisible 控制容器内所有子项的动画触发状态
 * @param stagger 是否启用子项连锁错峰
 */
@Composable
fun AnimatedRow(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    stagger: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    content: @Composable AnimatedRowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        AnimatedRowScopeImpl(this, isVisible, stagger).content()
    }
}

interface AnimatedRowScope : RowScope {
    /**
     * 在 [AnimatedRow] 中声明一个具有动画效果的子项
     * @param delay 指定当前子项的进场额外延迟时间(ms)
     */
    @Composable
    fun AnimatedItem(
        delay: Int? = null,
        content: @Composable RowScope.(ctx: AnimatedContext) -> Unit
    )
}

private class AnimatedRowScopeImpl(
    rowScope: RowScope,
    private val isVisible: Boolean,
    private val stagger: Boolean
) : AnimatedRowScope, RowScope by rowScope {
    private var itemIndex = 0

    @Composable
    override fun AnimatedItem(
        delay: Int?,
        content: @Composable RowScope.(ctx: AnimatedContext) -> Unit
    ) {
        val currentIndex = itemIndex++
        val ctx = rememberAnimatedContext(
            isVisible = isVisible,
            index = if (stagger) currentIndex else 0,
            extraDelayMillis = delay ?: 0,
            defaultDirection = AnimatedDirection.Right
        )
        content(ctx)
    }
}


// Lazy Column


/**
 * 支持连锁动画效果的懒加载垂直列表容器，自动为每个子项应用递增的动画延迟
 * @param isVisible 控制列表内所有子项的动画触发状态
 * @param stagger 是否启用子项连锁错峰
 */
@Composable
fun AnimatedLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    isVisible: Boolean,
    stagger: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: AnimatedLazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        verticalArrangement = verticalArrangement,
        contentPadding = contentPadding
    ) {
        AnimatedLazyListScopeImpl(this, isVisible, stagger).content()
    }
}

interface AnimatedLazyListScope {
    /**
     * 添加单个动画列表项
     * @param delay 指定当前项的进场额外延迟时间(ms)
     */
    fun animatedItem(
        key: Any? = null,
        delay: Int? = null,
        content: @Composable LazyItemScope.(ctx: AnimatedContext) -> Unit
    )

    /**
     * 批量添加动画列表项，每项延迟按列表索引递增
     * @param delay 每项叠加的进场额外延迟时间(ms)
     */
    fun <T> animatedItems(
        items: List<T>,
        delay: Int = 0,
        key: ((item: T) -> Any)? = null,
        content: @Composable LazyItemScope.(index: Int, item: T, ctx: AnimatedContext) -> Unit
    )
}

private class AnimatedLazyListScopeImpl(
    private val lazyListScope: LazyListScope,
    private val isVisible: Boolean,
    private val stagger: Boolean
) : AnimatedLazyListScope {
    private var itemIndex = 0

    override fun animatedItem(
        key: Any?,
        delay: Int?,
        content: @Composable LazyItemScope.(ctx: AnimatedContext) -> Unit
    ) {
        val currentIndex = itemIndex++
        lazyListScope.item(key = key) {
            val ctx = rememberAnimatedContext(
                isVisible = isVisible,
                index = if (stagger) currentIndex else 0,
                extraDelayMillis = delay ?: 0,
                defaultDirection = AnimatedDirection.Top
            )
            content(ctx)
        }
    }

    override fun <T> animatedItems(
        items: List<T>,
        delay: Int,
        key: ((item: T) -> Any)?,
        content: @Composable LazyItemScope.(index: Int, item: T, ctx: AnimatedContext) -> Unit
    ) {
        lazyListScope.itemsIndexed(
            items = items,
            key = if (key != null) { _, item -> key(item) } else null
        ) { index, item ->
            val ctx = rememberAnimatedContext(
                isVisible = isVisible,
                index = if (stagger) index else 0,
                extraDelayMillis = delay,
                defaultDirection = AnimatedDirection.Top
            )
            content(index, item, ctx)
        }
    }
}