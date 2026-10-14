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

package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer
import com.movtery.zalithlauncher.utils.computeScaledResolutionFactor

/**
 * Minecraft GUI Scale 业务规则计算器
 *
 * 封装 Minecraft 原版 ScaledResolution / Window.calculateScaleFactor 算法与官方缺陷规范：
 * 1. 基础尺度：按 320x240 最小基准栅格计算 Auto 最大缩放或使用 options.txt 中的有效配置值；
 * 2. 旧版本 (< 1.13)：受 MC-124469 影响，Unicode 模式下（显式开启 forceUnicodeFont 或使用中文等 CJK 语言），
 *    为了避免 GNU Unifont 点阵字体半像素模糊走样，大于 1 的奇数缩放倍数会强制递减 1 降为偶数（如 3 -> 2）；
 * 3. 现代版本 (>= 1.13)：受 MC-133435 影响（至今官方未修复），当显式开启 forceUnicodeFont 时，
 *    奇数缩放倍数会被游戏强制递增 1 进位为偶数（如 1 -> 2, 3 -> 4, 5 -> 6）。
 */
object MCGuiScale {

    /**
     * 计算指定渲染尺寸下的有效 GUI 缩放倍数
     */
    fun compute(
        renderWidth: Int,
        renderHeight: Int,
        options: MCOptions = MCOptions,
        version: Version? = options.getVersion()
    ): Int {
        options.checkReload()
        val configuredScale = options.get("guiScale")?.toIntOrNull() ?: 0
        var scale = computeScaledResolutionFactor(renderWidth, renderHeight, configuredScale)

        val mcVer = version?.getVersionInfo()?.minecraftVersion ?: version?.getVersionName()
        val isLowerThan113 = mcVer?.isLowerVer("1.13") ?: false
        val forceUnicodeFont = options.get("forceUnicodeFont") == "true"

        if (isLowerThan113) {
            val lang = options.get("lang")?.lowercase()
            val isCJK = lang != null && (lang.startsWith("zh") || lang.startsWith("ja") || lang.startsWith("ko"))
            if ((forceUnicodeFont || isCJK) && scale % 2 != 0 && scale != 1) {
                scale--
            }
        } else {
            if (forceUnicodeFont && scale % 2 != 0) {
                scale++
            }
        }

        return scale
    }
}

/**
 * 便利函数：读取当前 MCOptions 与 Version 计算当前渲染尺寸下的有效 GUI 缩放倍数
 */
fun currentMCGuiScale(renderWidth: Int, renderHeight: Int): Int =
    MCGuiScale.compute(renderWidth, renderHeight)
