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
// 参考 Fold Craft Launcher （https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCL/src/main/java/com/mio/controlconverter/CcConverter.kt）

package com.movtery.zalithlauncher.layoutconverter

import com.google.gson.JsonObject

/**
 * control-converter Kotlin 版入口（语义基准 cc.py）。
 *
 * FCL -> ZL2 默认参数与原 Go JNI 一致：
 * includeDirections=false, strict=false, aspect=16/9, lossless=true, absoluteAsPercentage=false。
 */
object CcConverter {

    /** cc.py detect_format。 */
    fun detectFormat(data: JsonObject): String = when {
        data.hasKey("layers") && data.hasKey("editorVersion") -> "zl"
        data.hasKey("viewGroups") && data.hasKey("controllerVersion") -> "fcl"
        else -> throw IllegalArgumentException("cannot detect input format; use zl2fcl or fcl2zl explicitly")
    }

    /** ZL2 控制布局 -> FCL 控制布局。 */
    fun convertZlToFcl(
        data: JsonObject,
        strict: Boolean = false,
        stripMeta: Boolean = false,
        deterministic: Boolean = false,
    ): JsonObject {
        val ctx = CcContext()
        ctx.deterministic = deterministic
        val result = CcZlToFcl.zlToFcl(ctx, data, strict)
        return if (stripMeta) stripConverterMeta(result).asJsonObject else result
    }

    /** 输入文本是否为 ZL2 布局（用于导入自动识别）。 */
    fun isZl2Layout(jsonText: String): Boolean = runCatching {
        detectFormat(CcJson.loadJson(jsonText)) == "zl"
    }.getOrDefault(false)

    /** 输入文本是否为 FCL 控件布局。 */
    fun isFclLayout(jsonText: String): Boolean = runCatching {
        detectFormat(CcJson.loadJson(jsonText)) == "fcl"
    }.getOrDefault(false)
}
