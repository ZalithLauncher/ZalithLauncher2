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

package com.movtery.zalithlauncher.game.account.wardrobe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.alpha
import com.movtery.zalithlauncher.utils.image.isColorMatch
import com.movtery.zalithlauncher.utils.image.recycleIfLarge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun legacyStrFill(str: String, code: Char, length: Int): String {
    return if (str.length > length) {
        str.take(length)
    } else {
        str.padEnd(length, code).drop(str.length) + str
    }
}

private fun getLocalUuid(name: String): String {
    val lenHex = name.length.toString(16)
    val lengthPart = legacyStrFill(lenHex, '0', 16)

    val hashCode = name.hashCode().toLong() and 0xFFFFFFFFL
    val hashHex = hashCode.toString(16)
    val hashPart = legacyStrFill(hashHex, '0', 16) //确保最长16位

    return buildString(34) {
        append(lengthPart.take(12))
        append('3')
        append(lengthPart.substring(13, 16))
        append('9')
        append(hashPart.take(15))
    }
}

/**
 * 根据皮肤模型类型，生成 profileId
 */
fun getLocalUUIDWithSkinModel(userName: String, skinModelType: SkinModelType): String {
    val baseUuid = getLocalUuid(userName)
    if (skinModelType == SkinModelType.NONE) return baseUuid

    val prefix = baseUuid.take(27)
    val a = baseUuid[7].digitToInt(16)
    val b = baseUuid[15].digitToInt(16)
    val c = baseUuid[23].digitToInt(16)

    var suffix = baseUuid.substring(27).toLong(16)
    val maxSuffix = 0xFFFFFL

    repeat(maxSuffix.toInt() + 1) {
        val currentD = (suffix and 0xFL).toInt()
        if ((a xor b xor c xor currentD) % 2 == skinModelType.targetParity) {
            return prefix + suffix.toString(16).padStart(5, '0').uppercase()
        }
        suffix = if (suffix == maxSuffix) 0L else suffix + 1
    }

    return prefix + suffix.toString(16).padStart(5, '0').uppercase()
}

/**
 * 检查皮肤像素合法性，Minecraft仅支持使用64x64或64x32像素的皮肤
 */
suspend fun validateSkinFile(skinFile: File): Boolean {
    return withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(skinFile.absolutePath, options)
        options.isDualLayerSkin() || options.isClassicSkin()
    }
}

/**
 * 是否为双层皮肤：64x64
 */
fun BitmapFactory.Options.isDualLayerSkin(): Boolean {
    return outWidth == 64 && outHeight == 64
}

/**
 * 是否为经典皮肤（单层皮肤），早期皮肤类型，双手、双腿的贴图是分别共用的
 * 64x32
 */
fun BitmapFactory.Options.isClassicSkin(): Boolean {
    return outWidth == 64 && outHeight == 32
}

/**
 * 检查皮肤是否为纤细（Alex）模型
 */
suspend fun File.isSlimModel(): Boolean = withContext(Dispatchers.IO) {
    val options = BitmapFactory.Options()
    val bitmap = BitmapFactory.decodeFile(absolutePath, options) ?: return@withContext false
    try {
        if (options.isClassicSkin()) {
            //旧版单层皮肤不支持细臂
            false
        } else {
            val rightHand = bitmap.isTransparent(50..51, 16..19)
            val rightArm = bitmap.isTransparent(54..55, 20..31)

            val leftHand = bitmap.isTransparent(42..43, 48..51)
            val leftArm = bitmap.isTransparent(46..47, 52..63)

            rightHand && rightArm && leftHand && leftArm
        }
    } catch (_: Exception) {
        false
    } finally {
        bitmap.recycleIfLarge()
    }
}

private fun Bitmap.isTransparent(xRange: IntRange, yRange: IntRange): Boolean {
    return isColorMatch(
        xRange = xRange,
        yRange = yRange,
        predicate = { color, _, _ ->
            color.alpha == 0
        },
        requireAll = true
    )
}
