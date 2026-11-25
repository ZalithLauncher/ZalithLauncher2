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

package com.movtery.zalithlauncher.game.download.addon

import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.version.installed.Version

/**
 * 根据远端版本声明和本地游戏版本信息判断兼容性。
 *
 * 该入口负责从 [target] 读取 Minecraft 版本及加载器显示名，再执行统一的兼容性判断。
 * 无法读取本地版本信息时，严格模组判断返回 `false`，其他模式保留宽松行为并返回 `true`。
 * 判断结果仅表示元数据兼容，不代表依赖完整或文件一定能够安装。
 *
 * @param target 待检查的本地游戏版本。
 * @param classes 远端资源类别；加载器规则仅适用于 [PlatformClasses.MOD]。
 * @param requireCompatibleModLoader 是否要求模组加载器信息完整且明确兼容，对非模组无效。
 * @return 在当前判断模式下，远端版本声明是否与本地版本兼容。
 */
fun PlatformVersion.isCompatibleWith(
    target: Version,
    classes: PlatformClasses,
    requireCompatibleModLoader: Boolean = false
): Boolean {
    val info = target.getVersionInfo()
    return isCompatibleWith(
        gameVersion = info?.minecraftVersion,
        targetLoaders = info?.loaderInfos?.map { it.loader.displayName },
        classes = classes,
        requireCompatibleModLoader = requireCompatibleModLoader
    )
}

/**
 * 使用调用方提供的本地版本快照，判断远端版本声明是否兼容。
 *
 * 本地信息完整时，首先要求 Minecraft 游戏版本匹配；非模组通过版本检查后不再检查加载器。
 * 对模组采用以下规则：
 * - 普通模式：双方都至少有一项可识别加载器时，仍要求其中一项匹配；本地信息不可用，
 *   或本地、远端任意一方完全没有可识别加载器时，为保持旧行为而按兼容处理。
 * - 严格模式：本地版本与加载器信息必须可用，本地必须包含平台可识别的实际加载器，
 *   远端也必须明确声明兼容的加载器；信息缺失、未声明或不匹配均返回 `false`。
 *
 * 严格模式下，CurseForge `Any` 可匹配任意本地实际加载器，但仍要求游戏版本匹配，
 * 且不能将空白、API 条目或 CurseForge `Any` 本身当成本地实际加载器。
 * 严格判断使用 [PlatformVersion.platformLoadersStrict]，普通判断使用 [PlatformVersion.platformLoaders]。
 *
 * @param gameVersion 本地 Minecraft 版本；`null` 表示该信息不可用。
 * @param targetLoaders 本地加载器显示名；`null` 表示加载器信息不可用，空列表表示未检测到加载器。
 * @param classes 远端资源类别；只有模组会进入加载器判断。
 * @param requireCompatibleModLoader 是否启用严格模组加载器判断，对非模组无效。
 * @return 是否符合所选模式的元数据兼容规则。
 */
fun PlatformVersion.isCompatibleWith(
    gameVersion: String?,
    targetLoaders: List<String>?,
    classes: PlatformClasses,
    requireCompatibleModLoader: Boolean = false
): Boolean {
    val strictModLoader = classes == PlatformClasses.MOD && requireCompatibleModLoader
    if (gameVersion == null || targetLoaders == null) return !strictModLoader
    if (gameVersion !in platformGameVersion()) return false

    if (classes != PlatformClasses.MOD) return true

    // 仅保留目标平台认识的本地加载器
    val recognizedTargetLoaders = recognizedPlatformLoaders(platform(), targetLoaders)
    if (recognizedTargetLoaders.isEmpty()) return !strictModLoader
    val versionLoaders = if (strictModLoader) platformLoadersStrict() else platformLoaders()

    // Any 只豁免远端具体加载器匹配，不能绕过前面对本地实际加载器的要求。
    if (
        strictModLoader && platform() == Platform.CURSEFORGE &&
        versionLoaders.any { it == CurseForgeModLoader.ANY }
    ) return true
    val versionLoaderNames = versionLoaders.mapNotNull { loader ->
        loader.getDisplayName().takeIf { it.isNotBlank() }
    }
    if (versionLoaderNames.isEmpty()) return !strictModLoader
    return recognizedTargetLoaders.any { target ->
        versionLoaderNames.any { it.equals(target, ignoreCase = true) }
    }
}

/**
 * 将本地加载器显示名转换为指定平台可比较的规范显示名。
 *
 * 该函数只识别 [platform] 对应枚举中的加载器，输入应是本地版本信息中的显示名，
 * 而不是 Modrinth facet 或 CurseForge API 标识。空白、无法识别的名称以及
 * [CurseForgeModLoader.ANY] 都会被排除，避免将非实际加载器用于严格匹配。
 * 返回值保留输入顺序，不去除重复项。
 *
 * @param platform 远端版本所属平台，决定使用哪套加载器枚举解释本地名称。
 * @param targetLoaders 从本地游戏版本信息中读取的原始加载器显示名。
 * @return 平台可识别加载器的规范显示名；没有可识别项时返回空列表。
 */
fun recognizedPlatformLoaders(
    platform: Platform,
    targetLoaders: List<String>
): List<String> = targetLoaders.mapNotNull { target ->
    if (target.isBlank()) return@mapNotNull null
    when (platform) {
        Platform.MODRINTH -> ModrinthModLoaderCategory.entries.firstOrNull {
            it.getDisplayName().equals(target, ignoreCase = true)
        }
        Platform.CURSEFORGE -> CurseForgeModLoader.entries.firstOrNull {
            it.getDisplayName().equals(target, ignoreCase = true)
        }
    }?.takeUnless { it == CurseForgeModLoader.ANY }?.getDisplayName()
}

