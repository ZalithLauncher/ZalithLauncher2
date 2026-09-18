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

package com.movtery.zalithlauncher.game.versioninfo

import com.movtery.zalithlauncher.R
import org.jackhuang.hmcl.util.versioning.GameVersionNumber

/**
 * 所有热门的模组支持版本
 */
val popularVersions = listOf(
    "26.2", "26.1",
    "1.21.11", "1.21.4", "1.21.1",
    "1.20.4", "1.20.1",
    "1.19.4", "1.19.2",
    "1.18.2",
    "1.16.5",
    "1.12.2",
    "1.8.9",
    "1.7.10",
)

/**
 * 愚人节版本类型
 */
enum class AprilFoolsType(
    val summary: Int? = null,
    val urlSuffix: String? = null
) {
    /**
     * [Wiki](https://zh.minecraft.wiki/w/26w14a)
     */
    HerdCraft(R.string.version_summary_fools_herd_craft),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/25w14craftmine)
     */
    CraftMine(R.string.version_summary_fools_creaft_mine),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/24w14potato)
     */
    Potato(R.string.version_summary_fools_potato),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/23w13a_or_b)
     */
    AOrB(R.string.version_summary_fools_a_or_b),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/22w13oneBlockAtATime)
     */
    OneBlockAtATime(R.string.version_summary_fools_one_block_at_time),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/20w14infinite)
     */
    Infinite(R.string.version_summary_fools_infinite),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/Java%E7%89%883D_Shareware_v1.34?variant=zh-cn)
     */
    Minecraft3DShareware(R.string.version_summary_fools_3d_shareware),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/Java%E7%89%881.RV-Pre1?variant=zh-cn)
     */
    TrendyUpdate(R.string.version_summary_fools_trendy_update),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/15w14a)
     */
    TheLoveAndHugsUpdate(R.string.version_summary_fools_the_love_and_hugs_update),
    /**
     * [Wiki](https://zh.minecraft.wiki/w/Java%E7%89%882.0)
     */
    `2_0`(R.string.version_summary_fools_2_0, "2.0")
}

/**
 * 愚人节版本
 */
data class AprilFoolsVersion(
    val version: String,
    val type: AprilFoolsType
)

/**
 * 可供下载的愚人节版本
 */
val allAprilFools = listOf(
    AprilFoolsVersion("26w14a", AprilFoolsType.HerdCraft),
    AprilFoolsVersion("25w14craftmine", AprilFoolsType.CraftMine),
    AprilFoolsVersion("24w14potato", AprilFoolsType.Potato),
    AprilFoolsVersion("23w13a_or_b", AprilFoolsType.AOrB),
    AprilFoolsVersion("22w13oneblockatatime", AprilFoolsType.OneBlockAtATime),
    AprilFoolsVersion("20w14infinite", AprilFoolsType.Infinite),
    AprilFoolsVersion("20w14∞", AprilFoolsType.Infinite),
    AprilFoolsVersion("3D Shareware v1.34", AprilFoolsType.Minecraft3DShareware),
    AprilFoolsVersion("1.RV-Pre1", AprilFoolsType.TrendyUpdate),
    AprilFoolsVersion("15w14a", AprilFoolsType.TheLoveAndHugsUpdate),
    AprilFoolsVersion("2.0_blue", AprilFoolsType.`2_0`),
    AprilFoolsVersion("2.0_red", AprilFoolsType.`2_0`),
    AprilFoolsVersion("2.0_purple", AprilFoolsType.`2_0`),
)

/**
 * 给出的 MC 版本号是否为正式版
 */
fun filterRelease(versionString: String): Boolean {
    return GameVersionNumber.asGameVersion(versionString).isRelease
}