package com.movtery.guide

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

/**
 * 激活引导流的覆盖层：解析锚点、绘制遮罩与镂空、裁决点击、展示引导内容。
 * 遮罩由唯一的 [Scrim] 层绘制，与步骤内容相互独立
 */
@Composable
internal fun GuideOverlay(
    controller: GuideController,
    state: GuideState.Active,
    registry: GuideRegistry,
    fadeAlpha: Float,
    nextTip: @Composable (NextTip) -> Unit,
    animations: GuideAnimations
) {
    val entry = state.entry

    // 观察注册表版本，解析当前步骤的锚点节点
    val version = registry.version
    val nodes = remember(entry.key, version) { registry.nodesFor(entry.key) }
    val anchors = nodes.map(GuideNode::bounds)

    // 回写锚点，供应用侧通过 GuideState.Active.anchors 观察就绪情况
    LaunchedEffect(state.index, anchors) {
        // 介绍步骤无锚点，不回写
        if (!entry.isIntro) controller.updateAnchors(state.index, anchors)
    }

    // 锚点缺失时，轮询询问容器侧注册的定位能力直到锚点就绪
    // 容器可能比引导步骤更晚组合，故反复询问
    LaunchedEffect(state.index) {
        if (entry.isIntro) return@LaunchedEffect
        var handled = 0
        while (registry.rectsFor(entry.key).isEmpty() && handled < 6) {
            if (registry.requestScroll(entry.key)) handled++
            delay(600.milliseconds)
        }
    }

    var contentRect by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current
    val gapPx = with(density) { GuideDefaults.contentGap.toPx() }
    val paddingPx = with(density) { GuideDefaults.screenPadding.toPx() }
    val layoutDirection = LocalLayoutDirection.current

    // 节点声明的镂空样式优先，未声明时回落引导流全局配置或库默认值
    val holes = nodes.map { node ->
        HoleTarget(
            rect = node.bounds,
            style = HoleStyle(
                radiusPx = with(density) { (node.holeRadius ?: controller.holeRadius).toPx() },
                borderWidthPx = with(density) { (node.holeBorderWidth ?: controller.holeBorderWidth).toPx() },
                borderColor = node.holeBorderColor ?: GuideDefaults.holeBorder
            )
        )
    }

    CompositionLocalProvider(LocalContentColor provides controller.colors.content) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .sharePointerInputWithSiblings()
        ) {
            val containerSize = IntSize(constraints.maxWidth, constraints.maxHeight)
            val anchorsVisible = anchorsVisibleIn(anchors, containerSize)

            // 锚点存在但未完全入视口时，请求滚动容器将其带入视口
            LaunchedEffect(state.index, anchorsVisible) {
                if (!entry.isIntro && anchors.isNotEmpty() && !anchorsVisible) {
                    registry.bringIntoView(entry.key)
                }
            }

            val ready = entry.isIntro || anchors.isNotEmpty()
            // 等待期间不存在引导内容，点击放行区域一并失效
            if (!ready && contentRect != null) contentRect = null

            // 介绍步骤与未入视口的锚点固定居中展示，入视口后按策略求解
            val sideHint = registry.sideHintFor(entry.key)
            val effectivePlacement = when {
                entry.isIntro || !anchorsVisible -> GuidePlacement.Fixed(Alignment.Center)
                else -> resolvePlacement(entry.placement, sideHint, anchors)
            }

            Scrim(
                controller = controller,
                holes = holes,
                ready = ready,
                fadeAlpha = fadeAlpha,
                contentRect = contentRect,
                isIntro = entry.isIntro,
                nodeClick = entry.nodeClick,
                advanceOnScrimClick = entry.advanceOnScrimClick,
                animations = animations,
                onNext = controller::next
            )

            GuideCardContainer(
                controller = controller,
                state = state,
                anchors = anchors,
                placement = effectivePlacement,
                visible = ready,
                fadeAlpha = fadeAlpha,
                animations = animations,
                layoutDirection = layoutDirection,
                gapPx = gapPx,
                paddingPx = paddingPx,
                onPlaced = { rect -> if (contentRect != rect) contentRect = rect }
            )

            // 下一步提示
            val tip: NextTip? = when {
                !entry.showNextTip -> null
                entry.nodeClick == NodeClickMode.PassThrough -> null // 用户点击元素推进，无提示
                state.index == controller.entries.lastIndex -> NextTip.Finish
                entry.advanceOnScrimClick -> NextTip.TapBlank
                else -> null
            }
            NextTipSlot(
                tip = tip,
                anchors = anchors,
                contentRect = contentRect,
                containerSize = containerSize,
                fadeAlpha = fadeAlpha,
                animations = animations,
                content = nextTip
            )
        }
    }
}

/**
 * 遮罩层
 */
@Composable
private fun Scrim(
    controller: GuideController,
    holes: List<HoleTarget>,
    ready: Boolean,
    fadeAlpha: Float,
    contentRect: Rect?,
    isIntro: Boolean,
    nodeClick: NodeClickMode,
    advanceOnScrimClick: Boolean,
    animations: GuideAnimations,
    onNext: () -> Unit
) {
    val currentHoles by rememberUpdatedState(holes)
    val currentReady by rememberUpdatedState(ready)
    val currentContentRect by rememberUpdatedState(contentRect)
    val currentIsIntro by rememberUpdatedState(isIntro)
    val currentNodeClick by rememberUpdatedState(nodeClick)
    val currentAdvanceOnScrim by rememberUpdatedState(advanceOnScrimClick)
    val currentOnNext by rememberUpdatedState(onNext)
    val currentColors by rememberUpdatedState(controller.colors)

    val holeSpec: FiniteAnimationSpec<Float> = if (animations.enabled) {
        spring(
            dampingRatio = animations.spatialSpring.dampingRatio,
            stiffness = animations.spatialSpring.stiffness
        )
    } else {
        snap()
    }
    val colorSpec: FiniteAnimationSpec<Color> = if (animations.enabled) {
        spring(
            dampingRatio = animations.spatialSpring.dampingRatio,
            stiffness = animations.spatialSpring.stiffness
        )
    } else {
        snap()
    }

    val slots = remember { mutableStateListOf<HoleSlot>() }
    SideEffect {
        holes.indices.forEach { index ->
            val existing = slots.find { it.index == index }
            if (existing == null) slots.add(HoleSlot(index))
            else if (existing.exiting) existing.exiting = false
        }
        slots.forEach { slot ->
            if (slot.index !in holes.indices && !slot.exiting) slot.exiting = true
        }
    }

    val animatedHoles = slots.map { slot ->
        key(slot.index) {
            LaunchedEffect(slot.exiting) {
                if (slot.exiting) {
                    slot.scale.animateTo(0f, holeSpec)
                    slots.removeAll { it.index == slot.index }
                } else {
                    slot.scale.animateTo(1f, holeSpec)
                }
            }

            val target = holes.getOrNull(slot.index) ?: slot.lastTarget
            if (target != null) {
                slot.lastTarget = target
                HoleFrame(
                    rect = animateHoleRect(target.rect, holeSpec).scaleAroundCenter(slot.scale.value),
                    style = animateHoleStyle(target.style, holeSpec, colorSpec)
                )
            } else {
                HoleFrame(Rect.Zero, HoleStyle(0f, 0f, Color.Transparent))
            }
        }
    }
    val currentAnimatedHoles by rememberUpdatedState(animatedHoles)

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawBehind {
                val scrim = currentColors.scrim
                drawRect(scrim.copy(alpha = scrim.alpha * fadeAlpha))
                if (currentReady) {
                    currentAnimatedHoles.forEach { hole ->
                        val style = hole.style
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = hole.rect.topLeft,
                            size = hole.rect.size,
                            cornerRadius = CornerRadius(style.radiusPx),
                            blendMode = BlendMode.Clear
                        )
                        if (style.borderWidthPx > 0f) {
                            // 描边整体落在镂空外侧，不侵入被引导组件
                            val border = hole.rect.inflate(style.borderWidthPx / 2f)
                            drawRoundRect(
                                color = style.borderColor,
                                topLeft = border.topLeft,
                                size = border.size,
                                cornerRadius = CornerRadius(style.radiusPx + style.borderWidthPx / 2f),
                                style = Stroke(width = style.borderWidthPx)
                            )
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )
                    val startPos = down.position
                    val hole = currentHoles.firstOrNull { it.rect.contains(startPos) }
                    val inContent = currentContentRect?.contains(startPos) == true

                    // 引导内容自身与放行模式的锚点只观察不消费，其余一律拦截
                    val observeOnly = !currentIsIntro && (inContent ||
                            (hole != null && currentNodeClick == NodeClickMode.PassThrough))
                    if (!observeOnly) down.consume()

                    var pressed = true
                    var isTap = true
                    val slop = viewConfiguration.touchSlop

                    while (pressed) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        var up = false
                        event.changes.forEach { change ->
                            if (change.id == down.id) {
                                if (!change.pressed && change.previousPressed) {
                                    up = true
                                } else if (abs(change.position.x - startPos.x) > slop ||
                                    abs(change.position.y - startPos.y) > slop
                                ) {
                                    isTap = false
                                }
                            }
                            if (!observeOnly) change.consume()
                        }
                        if (up) pressed = false
                    }

                    if (isTap) {
                        when {
                            currentIsIntro -> currentOnNext()
                            inContent -> Unit
                            hole != null -> currentOnNext()
                            currentAdvanceOnScrim -> currentOnNext()
                        }
                    }
                }
            }
    )
}

/**
 * 镂空矩形的弹性随动：锚点位置变化时聚光灯平滑跟随
 */
@Composable
private fun animateHoleRect(target: Rect, spec: FiniteAnimationSpec<Float>): Rect {
    val left by animateFloatAsState(target.left, spec, label = "holeLeft")
    val top by animateFloatAsState(target.top, spec, label = "holeTop")
    val right by animateFloatAsState(target.right, spec, label = "holeRight")
    val bottom by animateFloatAsState(target.bottom, spec, label = "holeBottom")
    return Rect(left, top, right, bottom)
}

@Composable
private fun animateHoleStyle(
    target: HoleStyle,
    spec: FiniteAnimationSpec<Float>,
    colorSpec: FiniteAnimationSpec<Color>
): HoleStyle {
    val radiusPx by animateFloatAsState(target.radiusPx, spec, label = "holeRadius")
    val borderWidthPx by animateFloatAsState(target.borderWidthPx, spec, label = "holeBorderWidth")
    val borderColor by animateColorAsState(target.borderColor, colorSpec, label = "holeBorderColor")
    return HoleStyle(radiusPx, borderWidthPx, borderColor)
}

/**
 * 单个镂空的绘制样式，像素已解析
 */
private data class HoleStyle(
    val radiusPx: Float,
    val borderWidthPx: Float,
    val borderColor: Color
)

/**
 * 单个镂空的动画帧
 */
private data class HoleFrame(val rect: Rect, val style: HoleStyle)

/**
 * 单个镂空的静态目标：锚点矩形与已解析样式
 */
private data class HoleTarget(val rect: Rect, val style: HoleStyle)

/**
 * 洞槽位：按目标序号跟踪入场/出场缩放与最近目标
 */
private class HoleSlot(val index: Int) {
    var exiting by mutableStateOf(false)
    var lastTarget: HoleTarget? = null
    val scale = Animatable(0f)
}

/**
 * 围绕矩形中心缩放，负值按 0 处理，避免出场回弹时翻转
 */
private fun Rect.scaleAroundCenter(scale: Float): Rect {
    val s = scale.coerceAtLeast(0f)
    if (s == 1f) return this
    val c = center
    return Rect(
        lerp(c.x, left, s),
        lerp(c.y, top, s),
        lerp(c.x, right, s),
        lerp(c.y, bottom, s)
    )
}

/**
 * 下一步提示槽位：组合宿主提供的提示布局并负责定位。
 * 位置默认在左下角，被锚点或引导内容占用时求解新位置并弹性随动；
 * 提示出现与隐藏时整体淡入淡出，隐藏动画结束后移除内容
 */
@Composable
private fun NextTipSlot(
    tip: NextTip?,
    anchors: List<Rect>,
    contentRect: Rect?,
    containerSize: IntSize,
    fadeAlpha: Float,
    animations: GuideAnimations,
    content: @Composable (NextTip) -> Unit
) {
    var displayedTip by remember { mutableStateOf<NextTip?>(null) }
    if (tip != null && displayedTip != tip) displayedTip = tip

    val density = LocalDensity.current
    val paddingPx = with(density) { GuideDefaults.nextTipPadding.toPx() }
    val marginPx = with(density) { GuideDefaults.nextTipMargin.toPx() }

    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(tip != null) {
        alphaAnim.animateTo(
            targetValue = if (tip != null) 1f else 0f,
            animationSpec = if (animations.enabled) {
                spring(
                    dampingRatio = animations.fadeSpring.dampingRatio,
                    stiffness = animations.fadeSpring.stiffness
                )
            } else {
                snap()
            }
        )
        if (tip == null) displayedTip = null
    }

    // 首次求解前为 null，此时直接摆放求解结果，避免提示从原点飞入
    val targetState = remember { mutableStateOf<IntOffset?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alphaAnim.value * fadeAlpha }
    ) {
        val animatedOffset = targetState.value?.let { target ->
            animateIntOffsetAsState(
                targetValue = target,
                animationSpec = if (animations.enabled) {
                    spring(
                        dampingRatio = animations.spatialSpring.dampingRatio,
                        stiffness = animations.spatialSpring.stiffness
                    )
                } else {
                    snap()
                },
                label = "nextTipOffset"
            )
        }

        Layout(
            content = { displayedTip?.let { content(it) } }
        ) { measurables, constraints ->
            val loose = Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
            val placeable = measurables.firstOrNull()?.measure(loose)
            val tipSize = IntSize(placeable?.width ?: 0, placeable?.height ?: 0)
            val solved = displayedTip?.let {
                solveNextTip(
                    tipSize = tipSize,
                    containerSize = containerSize,
                    paddingPx = paddingPx,
                    marginPx = marginPx,
                    obstacles = anchors + listOfNotNull(contentRect)
                )
            }
            if (solved != null && targetState.value != solved) targetState.value = solved
            val offset = animatedOffset?.value ?: solved ?: IntOffset.Zero
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable?.place(offset.x, offset.y)
            }
        }
    }
}

/**
 * 引导内容容器，测量内容并按放置策略求解目标位置，
 * 位置以弹性动画随步骤移动，步骤内容切换时淡入淡出
 */
@Composable
private fun GuideCardContainer(
    controller: GuideController,
    state: GuideState.Active,
    anchors: List<Rect>,
    placement: GuidePlacement,
    visible: Boolean,
    fadeAlpha: Float,
    animations: GuideAnimations,
    layoutDirection: LayoutDirection,
    gapPx: Float,
    paddingPx: Float,
    onPlaced: (Rect) -> Unit
) {
    val entry = state.entry
    val sideState = remember { mutableStateOf<GuideSide?>(null) }
    // 首次求解前为 null，此时直接摆放求解结果，避免内容从原点飞入
    val targetState = remember { mutableStateOf<IntOffset?>(null) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (animations.enabled) {
            spring(
                dampingRatio = animations.fadeSpring.dampingRatio,
                stiffness = animations.fadeSpring.stiffness
            )
        } else {
            snap()
        },
        label = "guideContentAlpha"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fadeAlpha }
    ) {
        val containerSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        val scope = remember(anchors, containerSize, controller) {
            GuideScope(anchors, containerSize, sideState, controller)
        }
        val animatedOffset = targetState.value?.let { target ->
            animateIntOffsetAsState(
                targetValue = target,
                animationSpec = if (animations.enabled) {
                    spring(
                        dampingRatio = animations.spatialSpring.dampingRatio,
                        stiffness = animations.spatialSpring.stiffness
                    )
                } else {
                    snap()
                },
                label = "guideCardOffset"
            )
        }

        Layout(
            content = {
                AnimatedContent(
                    targetState = entry,
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha },
                    transitionSpec = {
                        val spec: FiniteAnimationSpec<Float> = if (animations.enabled) {
                            spring(
                                dampingRatio = animations.fadeSpring.dampingRatio,
                                stiffness = animations.fadeSpring.stiffness
                            )
                        } else {
                            snap()
                        }
                        fadeIn(spec) togetherWith fadeOut(spec)
                    },
                    label = "guideCardContent"
                ) { stepEntry -> stepEntry.content(scope) }
            }
        ) { measurables, constraints ->
            val loose = Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
            val placeables = measurables.map { it.measure(loose) }
            val contentSize = IntSize(
                placeables.maxOf { it.width },
                placeables.maxOf { it.height }
            )
            // 锚点未就绪时保持上次求解的位置，仅随 alpha 淡出
            val solved = if (entry.isIntro || anchors.isNotEmpty()) {
                solvePlacement(
                    placement = placement,
                    anchors = anchors,
                    contentSize = contentSize,
                    containerSize = containerSize,
                    layoutDirection = layoutDirection,
                    gapPx = gapPx,
                    paddingPx = paddingPx
                ).also {
                    if (sideState.value != it.side) sideState.value = it.side
                    if (targetState.value != it.offset) targetState.value = it.offset
                    onPlaced(
                        Rect(
                            it.offset.x.toFloat(),
                            it.offset.y.toFloat(),
                            (it.offset.x + contentSize.width).toFloat(),
                            (it.offset.y + contentSize.height).toFloat()
                        )
                    )
                }
            } else {
                null
            }

            val offset = animatedOffset?.value ?: solved?.offset ?: IntOffset.Zero
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEach { it.place(offset.x, offset.y) }
            }
        }
    }
}
