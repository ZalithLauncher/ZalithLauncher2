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
// 参考 Fold Craft Launcher （https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCL/src/main/java/com/mio/controlconverter/CcStyles.kt）

package com.movtery.zalithlauncher.layoutconverter

import com.google.gson.JsonObject

/**
 * 样式转换（对应 cc.py zl_styles_to_fcl / fcl_styles_to_zl / 摇杆样式互转）。
 */
object CcStyles {

    fun styleNameForZlStyle(ctx: CcContext, baseName: String, uuidValue: String): String {
        val suffix = if (uuidValue.isNotEmpty()) uuidValue.take(6) else ctx.shortId().take(6)
        return "ZL $baseName $suffix"
    }

    /** ZL 按钮样式 -> FCL buttonStyles；返回 (结果列表, zl uuid -> fcl 名称)。 */
    fun zlStylesToFcl(ctx: CcContext, styles: List<JsonObject>): Pair<List<JsonObject>, MutableMap<String, String>> {
        val result = mutableListOf<JsonObject>()
        val mapping = linkedMapOf<String, String>()
        val used = LinkedHashSet<String>()

        for (style in styles) {
            val uuidValue = CcJson.toStringV(style.opt("uuid"))
            val baseName = run {
                val n = style.opt("name")
                if (CcUtils.pyTruthy(n)) CcJson.toStringV(n) else if (uuidValue.isNotEmpty()) uuidValue else "Style"
            }
            var name = styleNameForZlStyle(ctx, baseName, uuidValue)
            var suffix = 2
            while (name in used) {
                name = styleNameForZlStyle(ctx, baseName, uuidValue) + "_" + suffix
                suffix++
            }
            used.add(name)
            if (uuidValue.isNotEmpty()) mapping[uuidValue] = name

            val light = style.optObj("lightStyle") ?: JsonObject()
            val alpha = light.opt("alpha")
            val pressedAlpha = light.opt("pressedAlpha")
            result.add(
                CcJson.obj(
                    "name" to CcJson.str(name),
                    "textColor" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("contentColor"), -1, alpha)),
                    "textSize" to CcJson.inum(CcUtils.clampInt(light.opt("fontSize"), 12)),
                    "strokeColor" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("borderColor"), -12303292, alpha)),
                    "strokeWidth" to CcJson.inum(CcUtils.clampInt(light.opt("borderWidth"), 1) * 10),
                    "cornerRadius" to CcJson.inum(CcUtils.zlShapeToFclRadius(light.optObj("borderRadius"))),
                    "fillColor" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("backgroundColor"), 0, alpha)),
                    "textColorPressed" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("pressedContentColor"), -1, pressedAlpha)),
                    "textSizePressed" to CcJson.inum(
                        CcUtils.clampInt(light.opt("pressedFontSize"), CcUtils.clampInt(light.opt("fontSize"), 12))
                    ),
                    "strokeColorPressed" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("pressedBorderColor"), -12303292, pressedAlpha)),
                    "strokeWidthPressed" to CcJson.inum(
                        CcUtils.clampInt(light.opt("pressedBorderWidth"), CcUtils.clampInt(light.opt("borderWidth"), 1)) * 10
                    ),
                    "cornerRadiusPressed" to CcJson.inum(CcUtils.zlShapeToFclRadius(light.optObj("pressedBorderRadius"))),
                    "fillColorPressed" to CcJson.inum(
                        CcUtils.zlColorToFcl(light.opt("pressedBackgroundColor"), -3355444, pressedAlpha)
                    ),
                )
            )
        }

        if (result.isEmpty()) {
            result.add(CcConstants.defaultZlFallbackFclStyle())
        } else if (result.none { CcJson.toStringV(it.opt("name")) == "ZL Native Default" }) {
            result.add(0, CcConstants.defaultZlFallbackFclStyle())
        }
        return Pair(result, mapping)
    }

    /** ZL 摇杆样式 -> FCL rockerStyle（fcl_rocker_style_to_zl_joystick 的逆）。 */
    fun zlJoystickStyleToFclRocker(style: JsonObject): JsonObject {
        val light = style.optObj("lightStyle") ?: JsonObject()
        val joystickSize = CcUtils.clampRange(light.opt("joystickSize"), 0.0, 1.0, 0.5)
        val alpha = light.opt("alpha")
        return CcJson.obj(
            "rockerSize" to CcJson.inum(Math.max(100, Math.min(1000, CcUtils.pyRound(joystickSize * 1000.0)))),
            "bgCornerRadius" to CcJson.inum(Math.max(0, Math.min(500, CcUtils.clampInt(light.opt("backgroundShape"), 50) * 10))),
            "bgStrokeWidth" to CcJson.inum(Math.max(0, Math.min(500, CcUtils.clampInt(light.opt("borderWidthRatio"), 0) * 10))),
            "bgStrokeColor" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("borderColor"), -12303292, alpha)),
            "bgFillColor" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("backgroundColor"), 0, alpha)),
            "rockerCornerRadius" to CcJson.inum(Math.max(0, Math.min(500, CcUtils.clampInt(light.opt("joystickShape"), 50) * 10))),
            "rockerStrokeWidth" to CcJson.inum(10),
            "rockerStrokeColor" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("joystickColor"), -12303292, alpha)),
            "rockerFillColor" to CcJson.inum(CcUtils.zlColorToFcl(light.opt("joystickColor"), -7829368, alpha)),
        )
    }

    /** 两个 FCL rockerStyle 语义相等比较（忽略 rockerStrokeColor/Width）。 */
    fun fclRockerStyleMatches(a: JsonObject?, b: JsonObject?): Boolean {
        val comparable = arrayOf(
            "rockerSize", "bgCornerRadius", "bgStrokeWidth", "bgStrokeColor",
            "bgFillColor", "rockerCornerRadius", "rockerFillColor",
        )
        for (key in comparable) {
            val av = a?.opt(key)
            val bv = b?.opt(key)
            if (!CcJson.jsonEquals(av, bv)) return false
        }
        return true
    }

    /**
     * ZL 摇杆样式 -> FCL ROCKER 方向样式。
     * 返回 Pair(要追加的样式列表, 摇杆样式 uuid -> FCL 样式名)。
     */
    fun zlJoystickStylesToFclDirectionStyles(
        ctx: CcContext,
        joystickStyles: List<JsonObject>,
        existingStyles: List<JsonObject>,
    ): Pair<List<JsonObject>, MutableMap<String, String>> {
        val result = mutableListOf<JsonObject>()
        val mapping = linkedMapOf<String, String>()
        val usedNames = LinkedHashSet<String>()
        for (style in existingStyles) usedNames.add(CcJson.toStringV(style.opt("name")))
        val existingByName = linkedMapOf<String, JsonObject>()
        for (style in existingStyles) {
            if (CcJson.toStringV(style.opt("styleType")) == "ROCKER") {
                existingByName[CcJson.toStringV(style.opt("name"))] = style
            }
        }
        val existingRockers = existingByName.values.toList()
        val defaultButtonStyle = CcConstants.defaultFclDirectionStyle().optObj("buttonStyle") ?: JsonObject()

        for (style in joystickStyles) {
            val uuidValue = CcJson.toStringV(style.opt("uuid"))
            val baseName = run {
                val n = style.opt("name")
                if (CcUtils.pyTruthy(n)) CcJson.toStringV(n) else if (uuidValue.isNotEmpty()) uuidValue else "Joystick"
            }
            val convertedRocker = zlJoystickStyleToFclRocker(style)
            var matchedName = ""
            val originalStyle = metaOriginal(style, "fcl", "directionStyle")
            if (originalStyle != null) {
                val candidate = CcJson.toStringV(originalStyle.opt("name"))
                if (existingByName.containsKey(candidate)) matchedName = candidate
            }
            if (matchedName.isEmpty()) {
                for (existing in existingRockers) {
                    if (fclRockerStyleMatches(convertedRocker, existing.optObj("rockerStyle"))) {
                        matchedName = CcJson.toStringV(existing.opt("name"))
                        break
                    }
                }
            }
            if (matchedName.isNotEmpty()) {
                if (uuidValue.isNotEmpty()) mapping[uuidValue] = matchedName
                continue
            }
            var name = styleNameForZlStyle(ctx, baseName, uuidValue)
            var suffix = 2
            while (name in usedNames) {
                name = styleNameForZlStyle(ctx, baseName, uuidValue) + "_" + suffix
                suffix++
            }
            usedNames.add(name)
            if (uuidValue.isNotEmpty()) mapping[uuidValue] = name
            result.add(
                CcJson.obj(
                    "name" to CcJson.str(name),
                    "styleType" to CcJson.str("ROCKER"),
                    "buttonStyle" to defaultButtonStyle.deepCopy(),
                    "rockerStyle" to convertedRocker,
                )
            )
        }
        return Pair(result, mapping)
    }
}
