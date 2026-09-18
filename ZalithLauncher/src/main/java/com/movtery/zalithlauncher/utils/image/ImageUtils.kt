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

package com.movtery.zalithlauncher.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

private const val TAG = "ImageUtils"

/**
 * 将 [Drawable] 转换为 [Bitmap]
 * 如果该 Drawable 已经是 [BitmapDrawable] 且其内部 Bitmap 不为 null，则直接返回该 Bitmap
 * 否则渲染到一个新的 Bitmap 上
 */
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable && this.bitmap != null) {
        return this.bitmap
    }

    val width = if (intrinsicWidth > 0) intrinsicWidth else 1
    val height = if (intrinsicHeight > 0) intrinsicHeight else 1

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)

    return bitmap
}

/**
 * 遍历 Bitmap 的指定区域并执行谓词判断
 *
 * @param xRange X 轴坐标范围
 * @param yRange Y 轴坐标范围
 * @param predicate 谓词函数，参数为 (颜色值, x, y)
 * @param requireAll 是否要求所有像素都满足谓词，默认为 false (任一满足即返回 true)
 * @return 是否满足条件
 */
inline fun Bitmap.isColorMatch(
    xRange: IntRange,
    yRange: IntRange,
    predicate: (color: Int, x: Int, y: Int) -> Boolean,
    requireAll: Boolean = false
): Boolean {
    val width = this.width
    val height = this.height

    for (x in xRange) {
        if (x !in 0 until width) continue
        for (y in yRange) {
            if (y !in 0 until height) continue
            val match = predicate(this[x, y], x, y)
            if (requireAll) {
                if (!match) return false
            } else {
                if (match) return true
            }
        }
    }
    return requireAll
}

/**
 * 如果 Bitmap 大于指定阈值，则回收它
 */
fun Bitmap?.recycleIfLarge(thresholdBytes: Int = 8 * 1024 * 1024) {
    this ?: return

    if (!isRecycled && byteCount >= thresholdBytes) {
        recycle()
    }
}

/**
 * 尝试判断文件是否为一个图片
 */
fun File.isImageFile(): Boolean {
    if (!this.exists()) return false

    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(this.absolutePath, options)
        options.outWidth > 0 && options.outHeight > 0
    } catch (e: Exception) {
        Logger.warning(TAG,
            "An exception occurred while trying to determine if ${this.absolutePath} is an image.",
            e
        )
        false
    }
}