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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.sponsor.SponsorPlanEffect
import com.movtery.zalithlauncher.ui.theme.festivals.FramePacer
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val NANOS_PER_SECOND = 1_000_000_000f
private const val TAU = (PI * 2.0).toFloat()

private val LAPIS_FLOW = Color(0xFFADEBFF)
private val HAT_FLOW = Color(0xFFFFD18A)
private val TOKEN_FLOW = Color(0xFFFFEDB0)
private val SOFT_WHITE = Color(0xFFFFFDF4)
private val FEATHER_WHITE = Color(0xFFFFF9EA)

class SponsorEffectClock internal constructor(
    internal val elapsedNanos: State<Long>
)

@Composable
fun rememberSponsorEffectClock(): SponsorEffectClock {
    val elapsedNanos = remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val framePacer = FramePacer()
        var firstFrameNanos = Long.MIN_VALUE
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (firstFrameNanos == Long.MIN_VALUE) firstFrameNanos = frameNanos
                if (framePacer.onVsync(frameNanos) != null) {
                    elapsedNanos.longValue = frameNanos - firstFrameNanos
                }
            }
        }
    }
    return remember { SponsorEffectClock(elapsedNanos) }
}

private data class SponsorEffectPalette(
    val baseStart: Color,
    val baseEnd: Color,
    val flowPrimary: Color,
    val particle: Color
)

@Composable
private fun sponsorEffectPalette(effect: SponsorPlanEffect): SponsorEffectPalette {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (effect) {
        SponsorPlanEffect.LAPIS -> SponsorEffectPalette(
            baseStart = if (isDark) Color(0xFF304C85) else Color(0xFF466EB8),
            baseEnd = if (isDark) Color(0xFF152B52) else Color(0xFF263F76),
            flowPrimary = LAPIS_FLOW,
            particle = LAPIS_FLOW
        )
        SponsorPlanEffect.RED_CAT_HAT -> SponsorEffectPalette(
            baseStart = if (isDark) Color(0xFF87343E) else Color(0xFFA83E45),
            baseEnd = if (isDark) Color(0xFF481E32) else Color(0xFF702A39),
            flowPrimary = HAT_FLOW,
            particle = FEATHER_WHITE
        )
        SponsorPlanEffect.GOLDEN_TOKEN -> SponsorEffectPalette(
            baseStart = if (isDark) Color(0xFF725A40) else Color(0xFFC7A567),
            baseEnd = if (isDark) Color(0xFF3F3540) else Color(0xFF917344),
            flowPrimary = TOKEN_FLOW,
            particle = SOFT_WHITE
        )
        SponsorPlanEffect.EMERALD -> SponsorEffectPalette(
            baseStart = if (isDark) Color(0xFF26775C) else Color(0xFF379B73),
            baseEnd = if (isDark) Color(0xFF123E37) else Color(0xFF175B45),
            flowPrimary = Color(0xFFB4F4D4),
            particle = SOFT_WHITE
        )
    }
}

private fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    return (maxOf(firstLuminance, secondLuminance) + 0.05f) /
        (minOf(firstLuminance, secondLuminance) + 0.05f)
}

/** 内容色在对比度达标后统一混入的白量，保证文本足够亮眼 */
private const val CONTENT_WHITE_MIX = 0.7f

private fun contentColorFor(baseStart: Color, baseEnd: Color): Color {
    val brightestColor = if (baseStart.luminance() >= baseEnd.luminance()) baseStart else baseEnd
    val darkestColor = if (baseStart.luminance() >= baseEnd.luminance()) baseEnd else baseStart
    val target = if (brightestColor.luminance() < 0.5f) Color.White else Color.Black
    val dominant = if (target == Color.White) brightestColor else darkestColor
    var result = dominant
    var weight = 0f
    while (weight < 1f && contrastRatio(result, brightestColor) < 3f) {
        weight = (weight + 0.1f).coerceAtMost(1f)
        result = lerp(dominant, target, weight)
    }
    return if (target == Color.White) lerp(result, Color.White, CONTENT_WHITE_MIX) else result
}

class SponsorTextEnv(
    val contentColor: Color,
    val outsideColor: Color,
    val cardPath: Path?,
    val outsidePath: Path?,
    val boxCoordinates: State<LayoutCoordinates?>
)

private val LocalSponsorTextEnv = compositionLocalOf { null as SponsorTextEnv? }

@Composable
fun SponsorText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    alpha: Float = 1f
) {
    val env = LocalSponsorTextEnv.current
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    Text(
        text = text,
        style = style,
        color = Color.Transparent,
        onTextLayout = { layout = it },
        modifier = modifier
            .onGloballyPositioned { coordinates = it }
            .drawBehind {
                val result = layout ?: return@drawBehind
                val box = env?.boxCoordinates?.value
                val self = coordinates
                if (env == null || box == null || self == null || env.cardPath == null || env.outsidePath == null) {
                    drawText(result, color = env?.contentColor ?: Color.Black, alpha = alpha)
                    return@drawBehind
                }
                val offset = box.localPositionOf(self, Offset.Zero)
                withTransform({
                    translate(-offset.x, -offset.y)
                    clipPath(env.cardPath)
                }) {
                    drawText(result, color = env.contentColor, alpha = alpha, topLeft = offset)
                }
                withTransform({
                    translate(-offset.x, -offset.y)
                    clipPath(env.outsidePath)
                }) {
                    drawText(result, color = env.outsideColor, alpha = alpha, topLeft = offset)
                }
            }
    )
}

@Composable
fun SponsorText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    alpha: Float = 1f
) {
    SponsorText(AnnotatedString(text), modifier, style, alpha)
}

@Composable
fun SponsorEffectBox(
    effect: SponsorPlanEffect,
    clock: SponsorEffectClock,
    seed: Int,
    outsideColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = sponsorEffectPalette(effect)
    val contentColor = remember(palette) {
        contentColorFor(palette.baseStart, palette.baseEnd)
    }
    val localDensity = LocalDensity.current
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val boxCoordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val textEnv = remember(effect, seed, boxSize, outsideColor, contentColor, localDensity) {
        if (boxSize.width == 0 || boxSize.height == 0) {
            SponsorTextEnv(contentColor, outsideColor, null, null, boxCoordinates)
        } else {
            with(localDensity) {
                val cardPath = createBackgroundPath(
                    effect = effect,
                    width = boxSize.width.toFloat(),
                    height = boxSize.height.toFloat(),
                    cornerRadius = 18.dp.toPx(),
                    biteRadius = 40.dp.toPx(),
                    seed = seed
                )
                val rectPath = Path().apply {
                    addRect(Rect(0f, 0f, boxSize.width.toFloat(), boxSize.height.toFloat()))
                }
                val outsidePath = Path().apply {
                    op(rectPath, cardPath, PathOperation.Difference)
                }
                SponsorTextEnv(contentColor, outsideColor, cardPath, outsidePath, boxCoordinates)
            }
        }
    }
    val tokenPainter = if (effect == SponsorPlanEffect.GOLDEN_TOKEN) {
        painterResource(R.drawable.ic_launcher_foreground)
    } else {
        null
    }
    val featherImage = if (effect == SponsorPlanEffect.RED_CAT_HAT) {
        ImageBitmap.imageResource(R.drawable.img_divine_feather)
    } else {
        null
    }
    val lapisImage = if (effect == SponsorPlanEffect.LAPIS) {
        ImageBitmap.imageResource(R.drawable.img_lapis_lazuli)
    } else {
        null
    }

    CompositionLocalProvider(LocalSponsorTextEnv provides textEnv) {
        Box(
            modifier = modifier
                .onGloballyPositioned { boxCoordinates.value = it }
                .onSizeChanged { boxSize = it }
                .drawWithCache {
            val width = size.width
            val height = size.height
            val phaseOffset = (seed and 0x3FF) * 0.013f
            val clipPath = createBackgroundPath(
                effect = effect,
                width = width,
                height = height,
                cornerRadius = 18.dp.toPx(),
                biteRadius = 40.dp.toPx(),
                seed = seed
            )
            val backgroundBrush = Brush.linearGradient(
                colors = listOf(palette.baseStart, palette.baseEnd),
                start = Offset.Zero,
                end = Offset(width, height * 0.7f)
            )
            val feather = featherImage
            val feathers = if (effect == SponsorPlanEffect.RED_CAT_HAT) createFeathers(seed) else emptyList()
            val lapis = lapisImage
            val lapisMeteors = if (effect == SponsorPlanEffect.LAPIS) {
                createLapisMeteors(seed, density)
            } else {
                emptyList()
            }
            val hasStars = effect == SponsorPlanEffect.RED_CAT_HAT || effect == SponsorPlanEffect.EMERALD
            val starPath = if (hasStars) createStarPath() else null
            val stars = if (hasStars) createStars(seed) else emptyList()
            val streaks = if (effect == SponsorPlanEffect.GOLDEN_TOKEN) {
                createStreaks(seed, density)
            } else {
                emptyList()
            }
            val tokenMaxHeight = 44.dp.toPx()
            val tokenTail = if (effect == SponsorPlanEffect.GOLDEN_TOKEN) {
                TokenTailGeometry(width, height, tokenMaxHeight, palette.flowPrimary)
            } else {
                null
            }
            val cameraMargin = 8.dp.toPx()
            val cameraMaxY = 4.dp.toPx()

            onDrawBehind {
                val time = clock.elapsedNanos.value / NANOS_PER_SECOND + phaseOffset
                clipPath(clipPath) {
                    drawRect(brush = backgroundBrush)

                    when (effect) {
                        SponsorPlanEffect.LAPIS -> {
                            lapis?.let {
                                drawLapisMeteors(it, lapisMeteors, palette.flowPrimary, time, phaseOffset)
                            }
                        }
                        SponsorPlanEffect.RED_CAT_HAT -> {
                            starPath?.let { path ->
                                drawStars(path, stars, palette.particle, time, phaseOffset)
                            }
                            feather?.let {
                                drawFeathers(it, feathers, time, phaseOffset)
                            }
                        }
                        SponsorPlanEffect.GOLDEN_TOKEN -> {
                            tokenPainter?.let { painter ->
                                drawGoldenToken(
                                    painter = painter,
                                    streaks = streaks,
                                    tail = tokenTail,
                                    particleColor = palette.particle,
                                    time = time,
                                    phase = phaseOffset,
                                    tokenMaxHeight = tokenMaxHeight,
                                    cameraMargin = cameraMargin,
                                    cameraMaxY = cameraMaxY
                                )
                            }
                        }
                        SponsorPlanEffect.EMERALD -> {
                            starPath?.let { path ->
                                drawStars(path, stars, palette.particle, time, phaseOffset)
                            }
                        }
                    }
                }
            }
        },
        contentAlignment = Alignment.Center
    ) {
            content()
        }
    }
}

private fun createBackgroundPath(
    effect: SponsorPlanEffect,
    width: Float,
    height: Float,
    cornerRadius: Float,
    biteRadius: Float,
    seed: Int
): Path {
    val rounded = Path().apply {
        addRoundRect(RoundRect(0f, 0f, width, height, cornerRadius, cornerRadius))
    }
    if (effect != SponsorPlanEffect.LAPIS) return rounded

    return Path().apply {
        op(rounded, createLapisBitePath(biteRadius, seed), PathOperation.Difference)
    }
}

private fun createLapisBitePath(biteRadius: Float, seed: Int): Path {
    val random = Random(seed xor 0xB17E)
    val mouthRadius = biteRadius * (0.97f + random.nextFloat() * 0.06f)
    val mouthX = biteRadius * (random.nextFloat() - 0.5f) * 0.04f
    val mouthY = biteRadius * (random.nextFloat() - 0.5f) * 0.04f
    val toothCount = 7 + random.nextInt(2)
    val angleStep = TAU * 0.25f / toothCount

    return Path().apply {
        addOval(
            Rect(
                mouthX - mouthRadius,
                mouthY - mouthRadius,
                mouthX + mouthRadius,
                mouthY + mouthRadius
            )
        )
        repeat(toothCount) { index ->
            val angle = (index + 0.5f) * angleStep +
                (random.nextFloat() - 0.5f) * angleStep * 0.20f
            val toothRadius = biteRadius * (0.075f + random.nextFloat() * 0.035f)
            val centerDistance = mouthRadius + toothRadius * (0.02f + random.nextFloat() * 0.12f)
            val toothX = mouthX + cos(angle) * centerDistance
            val toothY = mouthY + sin(angle) * centerDistance
            addOval(
                Rect(
                    toothX - toothRadius,
                    toothY - toothRadius,
                    toothX + toothRadius,
                    toothY + toothRadius
                )
            )
        }
    }
}

private data class LapisMeteorSpec(
    val duration: Float,
    val phase: Float,
    val scale: Float,
    val tailLength: Float,
    val pathOffset: Float
)

private fun createLapisMeteors(seed: Int, density: Float): List<LapisMeteorSpec> {
    val random = Random(seed xor 0x1A915)
    return List(4 + random.nextInt(3)) {
        LapisMeteorSpec(
            duration = 1.25f + random.nextFloat() * 0.95f,
            phase = random.nextFloat(),
            scale = 0.72f + random.nextFloat() * 0.42f,
            tailLength = (20f + random.nextFloat() * 16f) * density,
            pathOffset = random.nextFloat()
        )
    }
}

private fun DrawScope.drawLapisMeteors(
    image: ImageBitmap,
    meteors: List<LapisMeteorSpec>,
    color: Color,
    time: Float,
    seedPhase: Float
) {
    val pixelScale = max(1f, (14.dp.toPx() / image.width).roundToInt().toFloat())
    val side = image.width * pixelScale
    meteors.forEach { spec ->
        val progress = fraction(time / spec.duration + spec.phase + seedPhase * 0.09f)
        val alpha = smoothStep(0f, 0.06f, progress) *
            (1f - smoothStep(0.94f, 1f, progress))
        if (alpha <= 0.01f) return@forEach

        val meteorSide = side * spec.scale
        val startX = meteorSide + spec.pathOffset * (size.width + size.height)
        val travel = startX + meteorSide
        val centerX = startX - progress * travel
        val centerY = -meteorSide + progress * travel
        val tailX = centerX + spec.tailLength
        val tailY = centerY - spec.tailLength
        val tailDx = tailX - centerX
        val tailDy = tailY - centerY
        val tailDistance = sqrt(tailDx * tailDx + tailDy * tailDy)
        val headWidth = meteorSide * (14f / 16f) * 0.24f
        val perpendicularX = -tailDy / tailDistance
        val perpendicularY = tailDx / tailDistance
        val tailPath = Path().apply {
            moveTo(
                centerX + perpendicularX * headWidth,
                centerY + perpendicularY * headWidth
            )
            lineTo(tailX, tailY)
            lineTo(
                centerX - perpendicularX * headWidth,
                centerY - perpendicularY * headWidth
            )
            close()
        }
        val tailBrush = Brush.linearGradient(
            colorStops = arrayOf(
                0f to color.copy(alpha = 0f),
                0.65f to color.copy(alpha = 0.12f),
                1f to color.copy(alpha = 0.58f)
            ),
            start = Offset(tailX, tailY),
            end = Offset(centerX, centerY)
        )
        drawPath(tailPath, tailBrush, alpha = alpha)

        val dstSide = meteorSide.roundToInt()
        drawImage(
            image = image,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstOffset = IntOffset(
                (centerX - dstSide / 2f).roundToInt(),
                (centerY - dstSide / 2f).roundToInt()
            ),
            dstSize = IntSize(dstSide, dstSide),
            alpha = alpha,
            filterQuality = FilterQuality.None
        )
    }
}

private data class FeatherSpec(
    val x: Float,
    val duration: Float,
    val phase: Float,
    val sway: Float,
    val rotation: Float,
    val scale: Float
)

private fun createFeathers(seed: Int): List<FeatherSpec> {
    val random = Random(seed xor 0xFEA7)
    return List(4 + random.nextInt(5)) {
        FeatherSpec(
            x = 0.04f + random.nextFloat() * 0.92f,
            duration = 4.6f + random.nextFloat() * 3.8f,
            phase = random.nextFloat(),
            sway = 4f + random.nextFloat() * 9f,
            rotation = -32f + random.nextFloat() * 64f,
            scale = 0.68f + random.nextFloat() * 0.50f
        )
    }
}

private fun DrawScope.drawFeathers(
    image: ImageBitmap,
    feathers: List<FeatherSpec>,
    time: Float,
    seedPhase: Float
) {
    val scale = max(1f, (14.dp.toPx() / image.width).roundToInt().toFloat())
    val side = image.width * scale
    feathers.forEach { spec ->
        val cycle = fraction(time / spec.duration + spec.phase + seedPhase * 0.07f)
        if (cycle >= 0.76f) return@forEach

        val progress = cycle / 0.76f
        val centerX = size.width * spec.x + sin(time * 1.65f + spec.phase * TAU) * spec.sway.dp.toPx()
        val centerY = -side / 2f + progress * (size.height + side)
        val rotation = spec.rotation + sin(time * 1.30f + spec.phase * TAU) * 24f
        val alpha = smoothStep(0f, 0.10f, progress) *
            (1f - smoothStep(0.82f, 1f, progress)) * 0.90f
        if (alpha <= 0.01f) return@forEach

        val dstSide = (side * spec.scale).roundToInt()
        withTransform({
            rotate(degrees = rotation, pivot = Offset(centerX, centerY))
        }) {
            drawImage(
                image = image,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(image.width, image.height),
                dstOffset = IntOffset(
                    (centerX - dstSide / 2f).roundToInt(),
                    (centerY - dstSide / 2f).roundToInt()
                ),
                dstSize = IntSize(dstSide, dstSide),
                alpha = alpha,
                filterQuality = FilterQuality.None
            )
        }
    }
}

private data class StarSpec(
    val x: Float,
    val y: Float,
    val radius: Float,
    val duration: Float,
    val phase: Float
)

private fun createStars(seed: Int): List<StarSpec> {
    val random = Random(seed xor 0x57A5)
    return List(5 + random.nextInt(5)) {
        StarSpec(
            x = 0.06f + random.nextFloat() * 0.88f,
            y = 0.16f + random.nextFloat() * 0.68f,
            radius = 2.8f + random.nextFloat() * 1.9f,
            duration = 3.4f + random.nextFloat() * 2.8f,
            phase = random.nextFloat()
        )
    }
}

private fun createStarPath(): Path = Path().apply {
    moveTo(0f, -1f)
    cubicTo(0.10f, -0.28f, 0.28f, -0.10f, 1f, 0f)
    cubicTo(0.28f, 0.10f, 0.10f, 0.28f, 0f, 1f)
    cubicTo(-0.10f, 0.28f, -0.28f, 0.10f, -1f, 0f)
    cubicTo(-0.28f, -0.10f, -0.10f, -0.28f, 0f, -1f)
    close()
}

private fun DrawScope.drawStars(
    path: Path,
    stars: List<StarSpec>,
    color: Color,
    time: Float,
    seedPhase: Float
) {
    stars.forEach { spec ->
        val cycle = fraction(time / spec.duration + spec.phase + seedPhase * 0.05f)
        val entrance = smoothStep(0f, 0.18f, cycle)
        val exit = 1f - smoothStep(0.76f, 1f, cycle)
        val breathing = sin(time * 2.1f + spec.phase * TAU)
        val alpha = entrance * exit * (0.85f + breathing * 0.13f)
        if (alpha <= 0.01f) return@forEach

        val scaleY = when {
            cycle < 0.18f -> lerpFloat(0.16f, 1f, entrance)
            cycle > 0.76f -> lerpFloat(1f, 0.16f, smoothStep(0.76f, 1f, cycle))
            else -> 1f + breathing * 0.045f
        }
        val scaleX = 1f + (1f - scaleY) * 0.42f
        val radius = spec.radius.dp.toPx()

        withTransform({
            translate(size.width * spec.x, size.height * spec.y)
            scale(radius * scaleX, radius * scaleY, Offset.Zero)
        }) {
            drawPath(path = path, color = color, alpha = alpha)
        }
    }
}

private data class StreakSpec(
    val y: Float,
    val length: Float,
    val duration: Float,
    val phase: Float,
    val alpha: Float,
    val strokeWidth: Float
)

private fun createStreaks(seed: Int, density: Float): List<StreakSpec> {
    val random = Random(seed xor 0x601D)
    return List(12) { index ->
        StreakSpec(
            y = (index + 0.5f + random.nextFloat() * 0.35f) / 12f,
            length = (10f + random.nextFloat() * 26f) * density,
            duration = 0.5f + random.nextFloat() * 0.45f,
            phase = random.nextFloat(),
            alpha = 0.35f + random.nextFloat() * 0.38f,
            strokeWidth = (0.7f + random.nextFloat() * 0.8f) * density
        )
    }
}

private class TokenTailGeometry(
    width: Float,
    height: Float,
    tokenMaxHeight: Float,
    color: Color
) {
    val tokenHeight = minOf(height * 0.72f, tokenMaxHeight)
    val tokenWidth = tokenHeight * 0.866f
    val path = Path().apply {
        val endX = -width * 0.12f
        val headX = width * 0.50f - tokenWidth * 0.10f
        moveTo(endX, height * 0.50f)
        cubicTo(width * 0.15f, height * 0.40f,
            headX - width * 0.12f, height * 0.50f - tokenHeight * 0.35f,
            headX, height * 0.50f - tokenHeight * 0.22f)
        lineTo(headX, height * 0.50f + tokenHeight * 0.22f)
        cubicTo(headX - width * 0.12f, height * 0.50f + tokenHeight * 0.35f,
            width * 0.15f, height * 0.60f, endX, height * 0.50f)
        close()
    }
    val brush = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to color.copy(alpha = 0f),
            0.28f to color.copy(alpha = 0.04f),
            0.66f to color.copy(alpha = 0.16f),
            0.90f to color.copy(alpha = 0.36f),
            1f to SOFT_WHITE.copy(alpha = 0.65f)
        ),
        startX = -width * 0.12f,
        endX = width * 0.50f - tokenWidth * 0.10f
    )
    val core = Path().apply {
        moveTo(width * 0.04f, height * 0.50f)
        lineTo(width * 0.50f - tokenWidth * 0.16f, height * 0.50f)
    }
    val coreBrush = Brush.horizontalGradient(
        colors = listOf(Color.Transparent, SOFT_WHITE.copy(alpha = 0.60f)),
        startX = width * 0.04f,
        endX = width * 0.50f - tokenWidth * 0.16f
    )
    val coreStroke = Stroke(width = (height * 0.025f).coerceAtLeast(1f), cap = StrokeCap.Round)
}

private fun DrawScope.drawGoldenToken(
    painter: Painter,
    streaks: List<StreakSpec>,
    tail: TokenTailGeometry?,
    particleColor: Color,
    time: Float,
    phase: Float,
    tokenMaxHeight: Float,
    cameraMargin: Float,
    cameraMaxY: Float
) {
    val visibleHeight = minOf(size.height * 0.72f, tokenMaxHeight)
    val visibleWidth = visibleHeight * 0.866f
    val painterSide = visibleHeight * 2f
    val availableX = ((size.width - visibleWidth) / 2f - cameraMargin).coerceAtLeast(0f)
    // 纵向余量以 cameraMaxY 为保留量，避免安全边距吃掉全部晃动幅度
    val availableY = ((size.height - visibleHeight) / 2f - cameraMaxY).coerceAtLeast(0f)
    val amplitudeX = minOf(size.width * 0.08f, availableX)
    val amplitudeY = minOf(cameraMaxY, availableY)
    val cameraX = sin(time * TAU / 4.1f + phase) * amplitudeX
    val cameraY = sin(time * TAU / 2.9f + phase + 1.3f) * amplitudeY

    withTransform({
        translate(cameraX, cameraY)
    }) {
        streaks.forEach { spec ->
            val progress = fraction(time / spec.duration + spec.phase + phase * 0.03f)
            val margin = spec.length * 1.5f
            val x = size.width + margin - progress * (size.width + margin * 2f)
            val y = size.height * spec.y
            drawLine(
                color = particleColor,
                start = Offset(x, y),
                end = Offset(x + spec.length, y),
                strokeWidth = spec.strokeWidth,
                cap = StrokeCap.Round,
                alpha = spec.alpha
            )
        }

        tail?.let {
            drawPath(it.path, it.brush)
            drawPath(it.core, it.coreBrush, style = it.coreStroke)
        }
        translate(
            left = (size.width - painterSide) / 2f,
            top = (size.height - painterSide) / 2f
        ) {
            with(painter) {
                draw(size = Size(painterSide, painterSide))
            }
        }
    }
}

private fun fraction(value: Float): Float = value - floor(value)

private fun smoothStep(start: Float, end: Float, value: Float): Float {
    val progress = ((value - start) / (end - start)).coerceIn(0f, 1f)
    return progress * progress * (3f - 2f * progress)
}

private fun lerpFloat(start: Float, end: Float, progress: Float): Float {
    return start + (end - start) * progress
}
