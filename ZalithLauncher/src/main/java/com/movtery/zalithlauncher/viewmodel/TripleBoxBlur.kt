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

package com.movtery.zalithlauncher.viewmodel

import kotlin.math.roundToInt

/** 三次盒式模糊近似高斯模糊；直接在调用方复用的像素数组上工作。 */
internal object TripleBoxBlur {
    fun blurInPlace(
        pixels: IntArray,
        temp: IntArray,
        width: Int,
        height: Int,
        radiusPx: Float,
        workspace: IntArray? = null,
        checkpoint: () -> Unit = {}
    ) {
        require(width > 0 && height > 0) { "Image size must be positive" }
        require(pixels.size >= width * height && temp.size >= width * height) {
            "Pixel buffers are smaller than the image"
        }
        val radius = sanitizeRadius(width, height, radiusPx)
        if (radius < 1) return

        val window = radius * 2 + 1
        val divisionOffset = width * 4
        val requiredWorkspace = divisionOffset + window * 255 + 1
        val work = workspace ?: IntArray(requiredWorkspace)
        require(work.size >= requiredWorkspace) { "Workspace is smaller than the blur working set" }

        //同一半径用于全部 6 个 pass，预先消掉每像素 4 次整数除法。
        for (sum in 0..window * 255) {
            work[divisionOffset + sum] = sum / window
        }

        repeat(3) {
            boxBlurHorizontal(pixels, temp, width, height, radius, work, checkpoint)
            boxBlurVertical(temp, pixels, width, height, radius, work, checkpoint)
        }
    }

    /** 长期运行的捕获管线据此复用工作区，避免每帧制造 GC 压力。 */
    fun workspaceSize(width: Int, height: Int, radiusPx: Float): Int {
        val radius = sanitizeRadius(width, height, radiusPx)
        if (radius < 1) return 0
        return width * 4 + (radius * 2 + 1) * 255 + 1
    }

    private fun sanitizeRadius(width: Int, height: Int, radiusPx: Float): Int =
        radiusPx.roundToInt()
            .coerceIn(1, 25)
            .coerceAtMost(minOf(width, height) - 1)

    private fun boxBlurHorizontal(
        src: IntArray,
        dst: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        work: IntArray,
        checkpoint: () -> Unit
    ) {
        val table = width * 4
        for (y in 0 until height) {
            if ((y and 31) == 0) checkpoint()
            val row = y * width
            var sumA = 0
            var sumR = 0
            var sumG = 0
            var sumB = 0
            for (i in -radius..radius) {
                val p = src[row + i.coerceIn(0, width - 1)]
                sumA += p ushr 24 and 0xFF
                sumR += p ushr 16 and 0xFF
                sumG += p ushr 8 and 0xFF
                sumB += p and 0xFF
            }
            for (x in 0 until width) {
                dst[row + x] = (work[table + sumA] shl 24) or
                    (work[table + sumR] shl 16) or
                    (work[table + sumG] shl 8) or
                    work[table + sumB]
                val outP = src[row + (x - radius).coerceIn(0, width - 1)]
                val inP = src[row + (x + radius + 1).coerceIn(0, width - 1)]
                sumA += (inP ushr 24 and 0xFF) - (outP ushr 24 and 0xFF)
                sumR += (inP ushr 16 and 0xFF) - (outP ushr 16 and 0xFF)
                sumG += (inP ushr 8 and 0xFF) - (outP ushr 8 and 0xFF)
                sumB += (inP and 0xFF) - (outP and 0xFF)
            }
        }
    }

    /**
     * 按行输出并维护每列滑动和，源、目标及前缀和都连续访问。
     * 与逐列跨行扫描相比，这种调度对 CPU 缓存和预取更友好。
     */
    private fun boxBlurVertical(
        src: IntArray,
        dst: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        work: IntArray,
        checkpoint: () -> Unit
    ) {
        val alpha = 0
        val red = width
        val green = width * 2
        val blue = width * 3
        val table = width * 4
        work.fill(0, 0, table)

        //初始化也按行读取，避免一开始就对整列做跨行扫描。
        for (i in -radius..radius) {
            val row = i.coerceIn(0, height - 1) * width
            for (x in 0 until width) {
                val p = src[row + x]
                work[alpha + x] += p ushr 24 and 0xFF
                work[red + x] += p ushr 16 and 0xFF
                work[green + x] += p ushr 8 and 0xFF
                work[blue + x] += p and 0xFF
            }
        }

        for (y in 0 until height) {
            if ((y and 31) == 0) checkpoint()
            val dstRow = y * width
            val outRow = (y - radius).coerceIn(0, height - 1) * width
            val inRow = (y + radius + 1).coerceIn(0, height - 1) * width
            for (x in 0 until width) {
                val a = alpha + x
                val r = red + x
                val g = green + x
                val b = blue + x
                dst[dstRow + x] = (work[table + work[a]] shl 24) or
                    (work[table + work[r]] shl 16) or
                    (work[table + work[g]] shl 8) or
                    work[table + work[b]]
                val outP = src[outRow + x]
                val inP = src[inRow + x]
                work[a] += (inP ushr 24 and 0xFF) - (outP ushr 24 and 0xFF)
                work[r] += (inP ushr 16 and 0xFF) - (outP ushr 16 and 0xFF)
                work[g] += (inP ushr 8 and 0xFF) - (outP ushr 8 and 0xFF)
                work[b] += (inP and 0xFF) - (outP and 0xFF)
            }
        }
    }
}
