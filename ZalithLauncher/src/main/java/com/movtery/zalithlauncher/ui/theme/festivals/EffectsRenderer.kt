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

package com.movtery.zalithlauncher.ui.theme.festivals

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.BatSwarmSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.FirefliesSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.FireworksSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.HeartsSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.Particle
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.ParticleSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.RainSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.SnowSimulator
import kotlin.math.atan2
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random
import androidx.core.graphics.withTranslation
import androidx.core.graphics.createBitmap

/** 效果绘制器，把对应模拟器的粒子画到画布上 */
interface EffectDrawer {
    /** 深浅主题切换时更新绘制颜色 */
    fun setTheme(isDark: Boolean)
    fun draw(canvas: Canvas, paint: Paint)
}

/** 根据效果种类构建对应的模拟器与绘制器 */
fun createEffect(
    type: FestivalEffectType,
    isDark: Boolean,
    width: Float,
    height: Float,
    density: Float
): Pair<ParticleSimulator, EffectDrawer> {
    val random = Random(System.nanoTime())
    fun buildSimulator(): ParticleSimulator = when (type) {
        FestivalEffectType.RAIN -> RainSimulator(random)
        FestivalEffectType.SNOW -> SnowSimulator(random)
        FestivalEffectType.FIREWORKS -> FireworksSimulator(random)
        FestivalEffectType.FIREWORKS_NATIONAL -> FireworksSimulator(random, grand = true)
        FestivalEffectType.FIREFLIES -> FirefliesSimulator(random)
        FestivalEffectType.BAT_SWARM -> BatSwarmSimulator(random)
        FestivalEffectType.HEARTS -> HeartsSimulator(random)
    }
    val simulator = buildSimulator().also { it.resize(width, height, density) }
    val drawer = when (type) {
        FestivalEffectType.RAIN -> RainDrawer(simulator as RainSimulator)
        FestivalEffectType.SNOW -> SnowDrawer(simulator as SnowSimulator)
        FestivalEffectType.FIREWORKS -> FireworksDrawer(simulator as FireworksSimulator, national = false)
        FestivalEffectType.FIREWORKS_NATIONAL -> FireworksDrawer(simulator as FireworksSimulator, national = true)
        FestivalEffectType.FIREFLIES -> FirefliesDrawer(simulator as FirefliesSimulator)
        FestivalEffectType.BAT_SWARM -> BatDrawer(simulator as BatSwarmSimulator)
        FestivalEffectType.HEARTS -> HeartDrawer(simulator as HeartsSimulator)
    }
    drawer.setTheme(isDark)
    return simulator to drawer
}

private fun applyAlpha(paint: Paint, color: Int, particleAlpha: Float) {
    paint.color = color
    paint.alpha = (particleAlpha * Color.alpha(color)).toInt().coerceIn(0, 255)
}

/** 以 [diameter] 大小在粒子位置绘制柔光点图，透明度取 [alpha] 与画笔颜色 alpha 的乘积 */
private fun drawGlow(
    canvas: Canvas,
    paint: Paint,
    matrix: Matrix,
    glow: Bitmap,
    x: Float,
    y: Float,
    diameter: Float,
    alpha: Float
) {
    paint.style = Paint.Style.FILL
    paint.alpha = (alpha * Color.alpha(paint.color)).toInt().coerceIn(0, 255)
    matrix.setScale(diameter / glow.width, diameter / glow.height)
    matrix.postTranslate(x - diameter / 2f, y - diameter / 2f)
    canvas.drawBitmap(glow, matrix, paint)
    paint.style = Paint.Style.STROKE
}

/** 生成一张边缘柔和的径向渐变圆点图 */
private fun softDotBitmap(size: Int, color: Int, coreStop: Float): Bitmap {
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    val shader = RadialGradient(
        center, center, center,
        intArrayOf(color, color, color and 0x00FFFFFF),
        floatArrayOf(0f, coreStop, 1f),
        Shader.TileMode.CLAMP
    )
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
    canvas.drawCircle(center, center, center, paint)
    return bitmap
}

/** 雨丝与溅花 */
private class RainDrawer(
    private val simulator: RainSimulator
) : EffectDrawer {
    private var streakColor = 0
    private var splashColor = 0

    override fun setTheme(isDark: Boolean) {
        val palette = FestivalPalette.rain(isDark)
        streakColor = palette.streak
        splashColor = palette.splash
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.shader = null
        paint.xfermode = null

        // 屏外粒子只参与模拟，不浪费光栅化
        val width = simulator.width
        val height = simulator.height
        for (particle in simulator.particles) {
            if (particle.x < -CULL_MARGIN || particle.x > width + CULL_MARGIN ||
                particle.y < -CULL_MARGIN - particle.size || particle.y > height + CULL_MARGIN
            ) {
                continue
            }

            if (particle.state == RainSimulator.STATE_DROP) {
                applyAlpha(paint, streakColor, particle.alpha)
                val speed = hypot(particle.vx, particle.vy).coerceAtLeast(1f)
                paint.strokeWidth = (0.5f + particle.variant * 0.7f) * simulator.density
                canvas.drawLine(
                    particle.x, particle.y,
                    particle.x - particle.vx / speed * particle.size,
                    particle.y - particle.vy / speed * particle.size,
                    paint
                )
            } else {
                applyAlpha(paint, splashColor, particle.alpha)
                paint.strokeWidth = particle.size * 2f
                canvas.drawPoint(particle.x, particle.y, paint)
            }
        }
    }

    companion object {
        private const val CULL_MARGIN = 24f
    }
}

/** 雪花 */
private class SnowDrawer(
    private val simulator: SnowSimulator
) : EffectDrawer {
    private var flakeBitmaps: List<Bitmap> = emptyList()
    private var flakeColor = 0xFFFFFFFF.toInt()

    override fun setTheme(isDark: Boolean) {
        flakeColor = FestivalPalette.snow(isDark).flake
        flakeBitmaps.forEach { it.recycle() }
        flakeBitmaps = SnowSimulator.FLAKE_SIZE_DP.map { diameterDp ->
            val px = (diameterDp * simulator.density).toInt().coerceIn(4, 128)
            softDotBitmap(px, flakeColor, coreStop = 0.42f)
        }
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val bitmaps = flakeBitmaps
        if (bitmaps.isEmpty()) return
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.xfermode = null

        val width = simulator.width
        val height = simulator.height
        for (particle in simulator.particles) {
            if (particle.x < -CULL_MARGIN || particle.x > width + CULL_MARGIN ||
                particle.y < -CULL_MARGIN || particle.y > height + CULL_MARGIN
            ) {
                continue
            }
            val bitmap = bitmaps.getOrElse(particle.colorIndex) { bitmaps[0] }
            applyAlpha(paint, flakeColor, particle.alpha)
            canvas.drawBitmap(
                bitmap,
                particle.x - bitmap.width / 2f,
                particle.y - bitmap.height / 2f,
                paint
            )
        }
    }

    companion object {
        private const val CULL_MARGIN = 30f
    }
}

/** 烟花 */
private class FireworksDrawer(
    private val simulator: FireworksSimulator,
    /** 国庆限定红金配色 */
    private val national: Boolean
) : EffectDrawer {
    private val addMode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    private var bursts: List<Int> = emptyList()
    private var flashColor = 0
    private var rocketColor = 0xFFFFFFFF.toInt()
    private var additive = false

    /** 每种爆炸色彩一张柔光点图：粒子数减少后由光晕补偿视觉密度 */
    private var burstGlows: List<Bitmap> = emptyList()
    private var flashGlow: Bitmap? = null
    private var rocketGlow: Bitmap? = null
    private val matrix = Matrix()

    override fun setTheme(isDark: Boolean) {
        val palette = if (national) FestivalPalette.fireworkNational(isDark) else FestivalPalette.firework(isDark)
        bursts = palette.bursts
        flashColor = palette.flash
        rocketColor = palette.rocket
        additive = palette.additive

        burstGlows.forEach { it.recycle() }
        flashGlow?.recycle()
        rocketGlow?.recycle()
        burstGlows = palette.bursts.map { softDotBitmap(48, it, coreStop = 0.28f) }
        flashGlow = softDotBitmap(64, palette.flash, coreStop = 0.24f)
        rocketGlow = softDotBitmap(32, palette.rocket, coreStop = 0.45f)
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.shader = null
        paint.xfermode = if (additive) addMode else null
        paint.isFilterBitmap = true

        val width = simulator.width
        val height = simulator.height
        val rocketGlow = rocketGlow ?: return
        val flashGlow = flashGlow ?: return

        for (particle in simulator.particles) {
            when (particle.state) {
                FireworksSimulator.STATE_ROCKET -> {
                    if (isOffscreen(particle, width, height)) continue
                    applyAlpha(paint, rocketColor, 0.8f)
                    paint.strokeWidth = particle.size * 0.7f
                    canvas.drawLine(particle.px, particle.py, particle.x, particle.y, paint)
                    drawGlow(canvas, paint, matrix, rocketGlow, particle.x, particle.y, particle.size * 3f, 1f)
                }

                FireworksSimulator.STATE_BURST, FireworksSimulator.STATE_SPLIT -> {
                    if (particle.alpha <= NEAR_INVISIBLE_ALPHA || isOffscreen(particle, width, height)) continue
                    val color = bursts.getOrElse(particle.colorIndex) { 0xFFFFFFFF.toInt() }
                    // 短运动拖尾
                    applyAlpha(paint, color, particle.alpha * 0.55f)
                    paint.strokeWidth = particle.size * 0.7f
                    canvas.drawLine(particle.px, particle.py, particle.x, particle.y, paint)
                    // 圆形光点 + 光晕承担视觉密度
                    burstGlows.getOrNull(particle.colorIndex)?.let { glow ->
                        drawGlow(canvas, paint, matrix, glow, particle.x, particle.y, particle.size * GLOW_MULT, particle.alpha)
                    }
                }

                FireworksSimulator.STATE_FLASH -> {
                    val progress = particle.life / particle.maxLife
                    paint.style = Paint.Style.FILL
                    applyAlpha(paint, flashColor, 1f - progress)
                    canvas.drawCircle(particle.x, particle.y, particle.size * (1.6f - progress), paint)
                    drawGlow(
                        canvas, paint, matrix, flashGlow,
                        particle.x, particle.y,
                        particle.size * FLASH_GLOW_MULT * (1.6f - progress),
                        1f - progress
                    )
                    paint.style = Paint.Style.STROKE
                }
            }
        }
        paint.xfermode = null
    }

    private fun isOffscreen(particle: Particle, width: Float, height: Float): Boolean {
        return particle.x < -CULL_MARGIN || particle.x > width + CULL_MARGIN ||
            particle.y < -CULL_MARGIN || particle.y > height + CULL_MARGIN
    }

    companion object {
        private const val CULL_MARGIN = 60f
        private const val NEAR_INVISIBLE_ALPHA = 0.02f

        /** 光点直径相对粒子尺寸的倍数：光晕越大，所需粒子越少 */
        private const val GLOW_MULT = 5.5f
        private const val FLASH_GLOW_MULT = 9f
    }
}

/** 流萤与满月 */
private class FirefliesDrawer(
    private val simulator: FirefliesSimulator
) : EffectDrawer {
    private var coreBitmap: Bitmap? = null
    private var haloBitmap: Bitmap? = null
    private var moonBitmap: Bitmap? = null
    private val matrix = Matrix()

    override fun setTheme(isDark: Boolean) {
        val palette = FestivalPalette.fireflies(isDark)
        coreBitmap?.recycle()
        haloBitmap?.recycle()
        moonBitmap?.recycle()
        coreBitmap = softDotBitmap(32, palette.core, coreStop = 0.5f)
        haloBitmap = softDotBitmap(96, palette.halo, coreStop = 0.12f)
        moonBitmap = moonBitmap(palette)
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val core = coreBitmap ?: return
        val halo = haloBitmap ?: return
        val moon = moonBitmap ?: return
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.xfermode = null

        // 满月：呼吸光晕 + 月盘
        val moonScale = simulator.moonRadius / (MOON_DISC_STOP * moon.width / 2f)
        val moonSize = moon.width * moonScale
        paint.alpha = (simulator.moonGlow * 255f).toInt().coerceIn(0, 255)
        matrix.reset()
        matrix.setScale(moonScale, moonScale)
        matrix.postTranslate(simulator.moonX - moonSize / 2f, simulator.moonY - moonSize / 2f)
        canvas.drawBitmap(moon, matrix, paint)

        // 流萤：先晕后芯
        for (fly in simulator.flies) {
            paint.alpha = (fly.alpha * 255f).toInt().coerceIn(0, 255)

            val haloSize = simulator.density * HALO_DIAMETER_DP
            matrix.reset()
            matrix.setScale(haloSize / halo.width, haloSize / halo.height)
            matrix.postTranslate(fly.x - haloSize / 2f, fly.y - haloSize / 2f)
            canvas.drawBitmap(halo, matrix, paint)

            val coreSize = simulator.density * CORE_DIAMETER_DP
            matrix.reset()
            matrix.setScale(coreSize / core.width, coreSize / core.height)
            matrix.postTranslate(fly.x - coreSize / 2f, fly.y - coreSize / 2f)
            canvas.drawBitmap(core, matrix, paint)
        }
    }

    /** 月盘实心、外围一圈柔光的径向渐变图 */
    private fun moonBitmap(palette: FestivalPalette.Fireflies): Bitmap {
        val size = 256
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val transparent = palette.moonHalo and 0x00FFFFFF
        val shader = RadialGradient(
            center, center, center,
            intArrayOf(palette.moon, palette.moon, palette.moonHalo, transparent),
            floatArrayOf(0f, MOON_DISC_STOP, MOON_DISC_STOP + 0.24f, 1f),
            Shader.TileMode.CLAMP
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
        canvas.drawCircle(center, center, center, paint)
        return bitmap
    }

    companion object {
        private const val CORE_DIAMETER_DP = 2.6f
        private const val HALO_DIAMETER_DP = 8.5f
        private const val MOON_DISC_STOP = 0.18f
    }
}

private class BatDrawer(
    private val simulator: BatSwarmSimulator
) : EffectDrawer {
    private var batColor = 0
    private var wispHalo = 0
    private var wispCore = 0
    private var haloBitmap: Bitmap? = null
    private var coreBitmap: Bitmap? = null
    private val path = Path()
    private val bodyRect = RectF()
    private val matrix = Matrix()
    private var isDark = false

    override fun setTheme(isDark: Boolean) {
        this.isDark = isDark
        val palette = FestivalPalette.halloween(isDark)
        batColor = palette.bat
        wispHalo = palette.wispHalo
        wispCore = palette.wispCore
        haloBitmap?.recycle()
        coreBitmap?.recycle()
        haloBitmap = softDotBitmap(64, palette.wispHalo, coreStop = 0.16f)
        coreBitmap = softDotBitmap(32, palette.wispCore, coreStop = 0.5f)
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.xfermode = null

        val width = simulator.width
        val height = simulator.height
        val halo = haloBitmap ?: return
        val core = coreBitmap ?: return

        drawFlameBand(canvas, paint, width, height)

        for (particle in simulator.particles) {
            when (particle.state) {
                BatSwarmSimulator.STATE_BAT, BatSwarmSimulator.STATE_STARTLED -> {
                    if (isOffscreen(particle, width, height)) continue
                    drawBat(canvas, paint, particle)
                }

                BatSwarmSimulator.STATE_TRAIL -> {
                    applyAlpha(paint, wispCore, particle.alpha * 0.8f)
                    val size = particle.size * 2f
                    matrix.setScale(size / core.width, size / core.height)
                    matrix.postTranslate(particle.x - size / 2f, particle.y - size / 2f)
                    canvas.drawBitmap(core, matrix, paint)
                }

                BatSwarmSimulator.STATE_WISP -> {
                    if (isOffscreen(particle, width, height)) continue
                    drawWisp(canvas, paint, halo, core, particle)
                }

                BatSwarmSimulator.STATE_EMBER -> {
                    if (isOffscreen(particle, width, height)) continue
                    val haloSize = particle.size * 4.6f
                    matrix.setScale(haloSize / halo.width, haloSize / halo.height)
                    matrix.postTranslate(particle.x - haloSize / 2f, particle.y - haloSize / 2f)
                    paint.alpha = (particle.alpha * Color.alpha(wispHalo)).toInt().coerceIn(0, 255)
                    canvas.drawBitmap(halo, matrix, paint)

                    val coreSize = particle.size * 1.3f
                    matrix.setScale(coreSize / core.width, coreSize / core.height)
                    matrix.postTranslate(particle.x - coreSize / 2f, particle.y - coreSize / 2f)
                    paint.alpha = (particle.alpha * 255).toInt().coerceIn(0, 255)
                    canvas.drawBitmap(core, matrix, paint)
                }

                BatSwarmSimulator.STATE_SPARK -> {
                    if (particle.alpha <= 0.02f) continue
                    val speed = hypot(particle.vx, particle.vy).coerceAtLeast(1f)
                    val length = particle.size * 3.2f
                    applyAlpha(paint, wispCore, particle.alpha)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = particle.size
                    paint.strokeCap = Paint.Cap.ROUND
                    canvas.drawLine(
                        particle.x, particle.y,
                        particle.x - particle.vx / speed * length,
                        particle.y - particle.vy / speed * length,
                        paint
                    )
                    paint.style = Paint.Style.FILL
                }
            }
        }
    }

    /**
     * 底部火焰带
     */
    private fun drawFlameBand(canvas: Canvas, paint: Paint, width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val clock = simulator.clock
        val density = simulator.density
        val bandHeight = BAND_HEIGHT_DP * density
        val breathing = 0.85f + 0.15f * sin(clock * 1.3f)

        for (layer in LAYER_SCALES.indices) {
            val scale = LAYER_SCALES[layer]
            val layerHeight = bandHeight * scale
            val color = if (layer == LAYER_SCALES.lastIndex) wispCore else wispHalo
            buildWavePath(path, width, height, layerHeight, clock * LAYER_SPEEDS[layer], layer * 1.7f)

            paint.shader = LinearGradient(
                0f, height, 0f, height - layerHeight,
                color, color and 0x00FFFFFF,
                Shader.TileMode.CLAMP
            )
            paint.alpha = (LAYER_ALPHAS[layer] * breathing * 255f).toInt().coerceIn(0, 255)
            canvas.drawPath(path, paint)

            if (layer == LAYER_SCALES.lastIndex) {
                // 前层叠加滚动的波浪化透明度
                val period = width / BAND_WAVE_COUNT
                paint.shader = LinearGradient(
                    0f, 0f, period, 0f,
                    intArrayOf(color and 0x00FFFFFF, color, color and 0x00FFFFFF),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.REPEAT
                ).apply {
                    val offset = (clock * BAND_WAVE_SPEED_DP * density) % period
                    setLocalMatrix(Matrix().apply { postTranslate(-offset, 0f) })
                }
                paint.alpha = (0.55f * breathing * 255f).toInt().coerceIn(0, 255)
                canvas.drawPath(path, paint)
            }
        }
        paint.alpha = 255
        paint.shader = null
    }

    /** 圆弧波浪轮廓：多频正弦叠加密采样，波峰圆滑；[phase] 错开各层波形 */
    private fun buildWavePath(
        target: Path,
        width: Float,
        height: Float,
        layerHeight: Float,
        time: Float,
        phase: Float
    ) {
        val samples = WAVE_SAMPLES
        val step = width / (samples - 1)
        val baseTop = height - layerHeight * 0.18f

        target.rewind()
        target.moveTo(0f, height)
        target.lineTo(0f, baseTop)
        for (index in 0 until samples) {
            val x = index * step
            val factor = 0.5f +
                0.34f * sin(time + x * 0.9f / (layerHeight / 2f + 24f) + phase) +
                0.16f * sin(time * 1.7f - x * 1.7f / (layerHeight / 2f + 24f) + phase * 2f)
            val top = baseTop - layerHeight * factor.coerceIn(0.04f, 1f)
            target.lineTo(x, top)
        }
        target.lineTo(width, height)
        target.close()
    }

    /** 鬼火 */
    private fun drawWisp(
        canvas: Canvas,
        paint: Paint,
        halo: Bitmap,
        core: Bitmap,
        particle: Particle
    ) {
        val r = particle.size
        val angle = particle.phase
        val pulse = 1f + 0.08f * sin(simulator.clock * 13f + particle.rotation * 7f)

        path.rewind()
        path.moveTo(r * 1.15f * pulse, 0f)
        path.quadTo(r * 0.35f, r * 0.95f, -r * 1.9f, 0f)
        path.quadTo(r * 0.35f, -r * 0.95f, r * 1.15f * pulse, 0f)
        path.close()

        canvas.withTranslation(particle.x, particle.y) {
            rotate(Math.toDegrees(angle.toDouble()).toFloat())
            applyAlpha(paint, wispHalo, particle.alpha)
            drawPath(path, paint)
        }

        // 内芯
        val coreSize = r * 1.1f * pulse
        matrix.setScale(coreSize / core.width, coreSize / core.height)
        matrix.postTranslate(particle.x - coreSize / 2f, particle.y - coreSize / 2f)
        applyAlpha(paint, wispCore, particle.alpha)
        canvas.drawBitmap(core, matrix, paint)

        // 大而淡的外晕
        val haloSize = r * 8f * pulse
        matrix.setScale(haloSize / halo.width, haloSize / halo.height)
        matrix.postTranslate(particle.x - haloSize / 2f, particle.y - haloSize / 2f)
        paint.alpha = (particle.alpha * Color.alpha(wispHalo) * 0.55f).toInt().coerceIn(0, 255)
        canvas.drawBitmap(halo, matrix, paint)
    }

    /** 蝙蝠剪影：身体椭圆 + 两片带指骨凹口的翼膜，翼尖随振翅相位上下摆动 */
    private fun drawBat(canvas: Canvas, paint: Paint, particle: Particle) {
        val halfW = particle.size
        val flap = BatSwarmSimulator.wingAngle(particle)
        val flapY = flap * halfW * 0.55f
        // 随实际纵向速度（斜飞分量 + 起伏）轻微俯仰，飞行更自然
        val vertical = particle.vy + BatSwarmSimulator.bobVelocity(particle, simulator.clock, simulator.density)
        val pitch = Math.toDegrees(atan2(vertical, abs(particle.vx) + 1f).toDouble()).toFloat() * 0.5f

        path.rewind()
        // 左翼（上缘 → 翼尖 → 带两处指骨凹口的下缘）
        path.moveTo(0f, 0f)
        path.lineTo(-halfW * 0.5f, -flapY * 0.7f - halfW * 0.1f)
        path.lineTo(-halfW, -flapY)
        path.lineTo(-halfW * 0.72f, flapY * 0.3f + halfW * 0.22f)
        path.lineTo(-halfW * 0.42f, flapY * 0.2f + halfW * 0.12f)
        path.close()
        // 右翼（镜像）
        path.moveTo(0f, 0f)
        path.lineTo(halfW * 0.5f, -flapY * 0.7f - halfW * 0.1f)
        path.lineTo(halfW, -flapY)
        path.lineTo(halfW * 0.72f, flapY * 0.3f + halfW * 0.22f)
        path.lineTo(halfW * 0.38f, flapY * 0.2f + halfW * 0.12f)
        path.close()
        // 身体
        bodyRect.set(-halfW * 0.14f, -halfW * 0.2f, halfW * 0.14f, halfW * 0.28f)
        path.addOval(bodyRect, Path.Direction.CW)

        applyAlpha(paint, batColor, particle.alpha)
        canvas.withTranslation(particle.x, particle.y) {
            rotate(pitch)
            drawPath(path, paint)
        }
    }

    private fun isOffscreen(particle: Particle, width: Float, height: Float): Boolean {
        val margin = particle.size + CULL_MARGIN
        return particle.x < -margin || particle.x > width + margin ||
            particle.y < -margin || particle.y > height + margin
    }

    companion object {
        private const val CULL_MARGIN = 40f
        private const val BAND_HEIGHT_DP = 36f
        private const val BAND_WAVE_COUNT = 3
        private const val BAND_WAVE_SPEED_DP = 26f
        private const val WAVE_SAMPLES = 44

        /** 三层视差波浪：后层高而淡，前层矮而亮快 */
        private val LAYER_SCALES = floatArrayOf(1f, 0.66f, 0.42f)
        private val LAYER_SPEEDS = floatArrayOf(0.9f, 1.5f, 2.3f)
        private val LAYER_ALPHAS = floatArrayOf(0.30f, 0.55f, 0.95f)
    }
}

private class HeartDrawer(
    private val simulator: HeartsSimulator
) : EffectDrawer {
    private var heartColor = 0
    private val unitHeart = Path().apply {
        moveTo(0f, 0.36f)
        cubicTo(-0.58f, 0.04f, -0.5f, -0.48f, 0f, -0.18f)
        cubicTo(0.5f, -0.48f, 0.58f, 0.04f, 0f, 0.36f)
        close()
    }

    override fun setTheme(isDark: Boolean) {
        heartColor = FestivalPalette.valentines(isDark).heart
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.xfermode = null

        val width = simulator.width
        val height = simulator.height
        for (particle in simulator.particles) {
            val margin = particle.size + CULL_MARGIN
            if (particle.x < -margin || particle.x > width + margin ||
                particle.y < -margin || particle.y > height + margin
            ) {
                continue
            }

            applyAlpha(paint, heartColor, particle.alpha)
            canvas.withTranslation(particle.x, particle.y) {
                rotate(Math.toDegrees(particle.rotation.toDouble()).toFloat())
                scale(particle.size, particle.size)
                drawPath(unitHeart, paint)
            }
        }
    }

    companion object {
        private const val CULL_MARGIN = 30f
    }
}
