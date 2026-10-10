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
// 参考 Fold Craft Launcher （https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCL/src/main/java/com/mio/controlconverter/CcDirection.kt）

package com.movtery.zalithlauncher.layoutconverter

import com.google.gson.JsonObject

/**
 * 方向控件转换（对应 cc.py fcl_direction_rect_to_zl_grid / direction_to_zl_joystick /
 * zl_joystick_to_fcl_direction 等）。
 */
object CcDirection {

    /** ZL JoystickData -> FCL ControlDirection 的 baseInfo。 */
    fun makeDirectionBaseInfoFromZl(joystick: JsonObject, layerVisibility: String?): JsonObject {
        val sizeTypeRaw = joystick.opt("sizeType")?.let { CcJson.toStringV(it) }
        val sizeType = (sizeTypeRaw ?: "Percentage").lowercase()
        val fclSizeType: String
        val absolute: Long
        val percentage: Long
        if (sizeType == "dp" || sizeType == "dip" || sizeType == "absolute") {
            fclSizeType = "ABSOLUTE"
            absolute = Math.max(5, CcUtils.clampInt(joystick.opt("sizeDp"), 50))
            percentage = 300
        } else {
            fclSizeType = "PERCENTAGE"
            absolute = Math.max(5, CcUtils.clampInt(joystick.opt("sizeDp"), 50))
            percentage = Math.max(100, Math.min(1000, CcUtils.clampInt(joystick.opt("sizePercentage"), 2500) / 10))
        }
        return CcJson.obj(
            "visibilityType" to CcJson.str(
                CcUtils.visibilityZlToFcl(
                    CcUtils.firstTruthyStr(joystick.opt("visibilityType"), if (layerVisibility != null) CcJson.str(layerVisibility) else null)
                )
            ),
            "xPosition" to CcJson.inum(CcUtils.scalePositionToFcl(joystick.optObj("position")?.opt("x"))),
            "yPosition" to CcJson.inum(CcUtils.scalePositionToFcl(joystick.optObj("position")?.opt("y"))),
            "sizeType" to CcJson.str(fclSizeType),
            "absoluteWidth" to CcJson.inum(absolute),
            "absoluteHeight" to CcJson.inum(absolute),
            "percentageWidth" to CcJson.obj("reference" to CcJson.str("SCREEN_HEIGHT"), "size" to CcJson.inum(percentage)),
            "percentageHeight" to CcJson.obj("reference" to CcJson.str("SCREEN_HEIGHT"), "size" to CcJson.inum(percentage)),
        )
    }

    /** ZL JoystickData -> FCL ControlDirection（ROCKER）。 */
    fun zlJoystickToFclDirection(
        ctx: CcContext,
        joystick: JsonObject,
        layerVisibility: String?,
        strict: Boolean,
        styleName: String,
    ): JsonObject? {
        val original = metaOriginal(joystick, "fcl", "direction")
        if (original != null) {
            val restored = overlaySharedFieldsFclDirection(ctx, original, joystick, layerVisibility)
            val originId = CcUtils.firstTruthyStr(
                joystick.opt("uuid"),
                restored.opt("id"),
            ) ?: ctx.fclId()
            return setMeta(restored, makeMeta("zl", "joystick", originId, joystick))
        }

        val directionEvents = joystick.optObj("directionEvents") ?: JsonObject()
        fun keycodesFor(name: String): List<Long> {
            val keycodes = mutableListOf<Long>()
            for (event in directionEvents.optArr(name) ?: emptyList()) {
                val obj = event.asObjOrNull() ?: continue
                if (CcJson.toStringV(obj.opt("type")) != "key") continue
                val keycode = CcUtils.convertKeyToFcl(ctx, CcJson.toStringV(obj.opt("key")), strict)
                keycodes.add(keycode)
            }
            return keycodes
        }

        val directionObj = CcJson.obj(
            "id" to CcJson.str(
                CcUtils.firstTruthyStr(joystick.opt("uuid")) ?: ctx.fclId()
            ),
            "baseInfo" to makeDirectionBaseInfoFromZl(joystick, layerVisibility),
            "event" to CcJson.obj(
                "upKeycode" to arrOfNums(keycodesFor("north")),
                "downKeycode" to arrOfNums(keycodesFor("south")),
                "leftKeycode" to arrOfNums(keycodesFor("west")),
                "rightKeycode" to arrOfNums(keycodesFor("east")),
            ),
            "style" to CcJson.str(styleName),
        )
        return setMeta(
            directionObj,
            makeMeta("zl", "joystick", CcUtils.firstTruthyStr(joystick.opt("uuid"), directionObj.opt("id")), joystick)
        )
    }

    private fun arrOfNums(values: List<Long>): com.google.gson.JsonArray {
        val arr = com.google.gson.JsonArray()
        for (v in values) arr.add(CcJson.inum(v))
        return arr
    }

    /** meta 恢复路径：用当前摇杆几何覆写原 FCL direction 的共享字段。 */
    fun overlaySharedFieldsFclDirection(
        ctx: CcContext,
        original: JsonObject,
        joystick: JsonObject,
        layerVisibility: String?,
    ): JsonObject {
        val restored = original.deepCopy().asJsonObject
        restored.add(
            "id",
            CcJson.str(
                CcUtils.firstTruthyStr(joystick.opt("uuid"), restored.opt("id")) ?: ctx.fclId()
            ),
        )
        restored.add("baseInfo", makeDirectionBaseInfoFromZl(joystick, layerVisibility))
        return restored
    }
}
