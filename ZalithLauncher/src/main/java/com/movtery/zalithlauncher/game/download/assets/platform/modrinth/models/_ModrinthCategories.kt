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

package com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.ModLoaderDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import kotlinx.parcelize.Parcelize

/**
 * Modrinth 模组加载器类别
 */
@Parcelize
enum class ModrinthModLoaderCategory : ModrinthFacet, ModLoaderDisplayLabel {
    FABRIC {
        override fun facetValue(): String = "fabric"
        override fun getDisplayName(): String = ModLoader.FABRIC.displayName
    },
    FORGE {
        override fun facetValue(): String = "forge"
        override fun getDisplayName(): String = ModLoader.FORGE.displayName
    },
    NEOFORGE {
        override fun facetValue(): String = "neoforge"
        override fun getDisplayName(): String = ModLoader.NEOFORGE.displayName
    },
    QUILT {
        override fun facetValue(): String = "quilt"
        override fun getDisplayName(): String = ModLoader.QUILT.displayName
    },
    BABRIC {
        override fun facetValue(): String = "babric"
        override fun getDisplayName(): String = ModLoader.BABRIC.displayName
    },
    BTA_BABRIC {
        override fun facetValue(): String = "bta-babric"
        override fun getDisplayName(): String = "BTA (Babric)"
    },
//    注入型，非正常情况使用，排除
//    JAVA_AGENT {
//        override fun facetValue(): String = "java-agent"
//        override fun getDisplayName(): String = "Java Agent"
//    },
    LEGACY_FABRIC {
        override fun facetValue(): String = "legacy-fabric"
        override fun getDisplayName(): String = ModLoader.LEGACY_FABRIC.displayName
    },
    LITELOADER {
        override fun facetValue(): String = "liteloader"
        override fun getDisplayName(): String = ModLoader.LITE_LOADER.displayName
    },
    RISUGAMI_MODLOADER {
        override fun facetValue(): String = "modloader"
        override fun getDisplayName(): String = "Risugami's ModLoader"
    },
    NIL_LOADER {
        override fun facetValue(): String = "nilloader"
        override fun getDisplayName(): String = "NilLoader"
    },
    ORNITHE {
        override fun facetValue(): String = "ornithe"
        override fun getDisplayName(): String = "Ornithe"
    },
    RIFT {
        override fun facetValue(): String = "rift"
        override fun getDisplayName(): String = "Rift"
    };

    override fun facetName(): String = "categories"
    override fun index(): Int = this.ordinal
}

/**
 * 可视化筛选器支持的模组加载器
 */
val modrinthModLoaderFilters: List<ModrinthModLoaderCategory> = listOf(
    ModrinthModLoaderCategory.FABRIC,
    ModrinthModLoaderCategory.FORGE,
    ModrinthModLoaderCategory.NEOFORGE,
    ModrinthModLoaderCategory.QUILT
)

/**
 * Modrinth 模组下载类别
 */
enum class ModrinthModCategory : ModrinthFacet, PlatformFilterCode {
    WORLDGEN {
        override fun facetValue(): String = "worldgen"
        override fun getDisplayName(): Int = R.string.download_assets_category_worldgen
    },
    TECHNOLOGY {
        override fun facetValue(): String = "technology"
        override fun getDisplayName(): Int = R.string.download_assets_category_technology
    },
    FOOD {
        override fun facetValue(): String = "food"
        override fun getDisplayName(): Int = R.string.download_assets_category_food
    },
    GAME_MECHANICS {
        override fun facetValue(): String = "game-mechanics"
        override fun getDisplayName(): Int = R.string.download_assets_category_game_mechanics
    },
    TRANSPORT {
        override fun facetValue(): String = "transportation"
        override fun getDisplayName(): Int = R.string.download_assets_category_transport
    },
    STORAGE {
        override fun facetValue(): String = "storage"
        override fun getDisplayName(): Int = R.string.download_assets_category_storage
    },
    MAGIC {
        override fun facetValue(): String = "magic"
        override fun getDisplayName(): Int = R.string.download_assets_category_magic
    },
    ADVENTURE {
        override fun facetValue(): String = "adventure"
        override fun getDisplayName(): Int = R.string.download_assets_category_adventure
    },
    DECORATION {
        override fun facetValue(): String = "decoration"
        override fun getDisplayName(): Int = R.string.download_assets_category_decoration
    },
    MOBS {
        override fun facetValue(): String = "mobs"
        override fun getDisplayName(): Int = R.string.download_assets_category_mobs
    },
    EQUIPMENT {
        override fun facetValue(): String = "equipment"
        override fun getDisplayName(): Int = R.string.download_assets_category_equipment
    },
    OPTIMIZATION {
        override fun facetValue(): String = "optimization"
        override fun getDisplayName(): Int = R.string.download_assets_category_optimization
    },
    SOCIAL {
        override fun facetValue(): String = "social"
        override fun getDisplayName(): Int = R.string.download_assets_category_social
    },
    UTILITY {
        override fun facetValue(): String = "utility"
        override fun getDisplayName(): Int = R.string.download_assets_category_utility
    },
    LIBRARY {
        override fun facetValue(): String = "library"
        override fun getDisplayName(): Int = R.string.download_assets_category_library
    };

    override fun facetName(): String = "categories"
    override fun index(): Int = this.ordinal
}

/**
 * Modrinth 整合包下载类别
 */
enum class ModrinthModpackCategory : ModrinthFacet, PlatformFilterCode {
    MULTIPLAYER {
        override fun facetValue(): String = "multiplayer"
        override fun getDisplayName(): Int = R.string.download_assets_category_multiplayer
    },
    OPTIMIZATION {
        override fun facetValue(): String = "optimization"
        override fun getDisplayName(): Int = R.string.download_assets_category_optimization
    },
    CHALLENGING {
        override fun facetValue(): String = "challenging"
        override fun getDisplayName(): Int = R.string.download_assets_category_challenging
    },
    COMBAT {
        override fun facetValue(): String = "combat"
        override fun getDisplayName(): Int = R.string.download_assets_category_combat
    },
    QUESTS {
        override fun facetValue(): String = "quests"
        override fun getDisplayName(): Int = R.string.download_assets_category_quests
    },
    TECHNOLOGY {
        override fun facetValue(): String = "technology"
        override fun getDisplayName(): Int = R.string.download_assets_category_technology
    },
    MAGIC {
        override fun facetValue(): String = "magic"
        override fun getDisplayName(): Int = R.string.download_assets_category_magic
    },
    ADVENTURE {
        override fun facetValue(): String = "adventure"
        override fun getDisplayName(): Int = R.string.download_assets_category_adventure
    },
    KITCHEN_SINK {
        override fun facetValue(): String = "kitchen-sink"
        override fun getDisplayName(): Int = R.string.download_assets_category_kitchen_sink
    },
    LIGHTWEIGHT {
        override fun facetValue(): String = "lightweight"
        override fun getDisplayName(): Int = R.string.download_assets_category_lightweight
    };

    override fun facetName(): String = "categories"
    override fun index(): Int = this.ordinal
}

/**
 * Modrinth 资源包下载类别
 */
enum class ModrinthResourcePackCategory : ModrinthFacet, PlatformFilterCode {
    COMBAT {
        override fun facetValue(): String = "combat"
        override fun getDisplayName(): Int = R.string.download_assets_category_combat
    },
    DECORATION {
        override fun facetValue(): String = "decoration"
        override fun getDisplayName(): Int = R.string.download_assets_category_decoration
    },
    VANILLA {
        override fun facetValue(): String = "vanilla-like"
        override fun getDisplayName(): Int = R.string.download_assets_category_vanilla
    },
    PHOTO_REALISTIC {
        override fun facetValue(): String = "realistic"
        override fun getDisplayName(): Int = R.string.download_assets_category_photo_realistic
    },
    MOD_SUPPORT {
        override fun facetValue(): String = "modded"
        override fun getDisplayName(): Int = R.string.download_assets_category_mod_support
    };

    override fun facetName(): String = "categories"
    override fun index(): Int = this.ordinal
}

/**
 * Modrinth 光影包下载类别
 */
enum class ModrinthShadersCategory : ModrinthFacet, PlatformFilterCode {
    CARTOON {
        override fun facetValue(): String = "cartoon"
        override fun getDisplayName(): Int = R.string.download_assets_category_cartoon
    },
    CURSED {
        override fun facetValue(): String = "cursed"
        override fun getDisplayName(): Int = R.string.download_assets_category_cursed
    },
    FANTASY {
        override fun facetValue(): String = "fantasy"
        override fun getDisplayName(): Int = R.string.download_assets_category_fantasy
    },
    REALISTIC {
        override fun facetValue(): String = "realistic"
        override fun getDisplayName(): Int = R.string.download_assets_category_photo_realistic
    },
    SEMI_REALISTIC {
        override fun facetValue(): String = "semi-realistic"
        override fun getDisplayName(): Int = R.string.download_assets_category_semi_realistic
    },
    VANILLA {
        override fun facetValue(): String = "vanilla-like"
        override fun getDisplayName(): Int = R.string.download_assets_category_vanilla
    },
    ATMOSPHERE {
        override fun facetValue(): String = "atmosphere"
        override fun getDisplayName(): Int = R.string.download_assets_category_atmosphere
    },
    BLOOM {
        override fun facetValue(): String = "bloom"
        override fun getDisplayName(): Int = R.string.download_assets_category_bloom
    },
    COLORED_LIGHTING {
        override fun facetValue(): String = "colored-lighting"
        override fun getDisplayName(): Int = R.string.download_assets_category_colored_lighting
    },
    FOLIAGE {
        override fun facetValue(): String = "foliage"
        override fun getDisplayName(): Int = R.string.download_assets_category_foliage
    },
    PATH_TRACING {
        override fun facetValue(): String = "path-tracing"
        override fun getDisplayName(): Int = R.string.download_assets_category_path_tracing
    },
    PBR {
        override fun facetValue(): String = "pbr"
        override fun getDisplayName(): Int = R.string.download_assets_category_pbr
    },
    REFLECTIONS {
        override fun facetValue(): String = "reflections"
        override fun getDisplayName(): Int = R.string.download_assets_category_reflections
    },
    SHADOWS {
        override fun facetValue(): String = "shadows"
        override fun getDisplayName(): Int = R.string.download_assets_category_shadows
    },
    POTATO {
        override fun facetValue(): String = "potato"
        override fun getDisplayName(): Int = R.string.download_assets_category_configuration_potato
    },
    LOW {
        override fun facetValue(): String = "low"
        override fun getDisplayName(): Int = R.string.download_assets_category_configuration_low
    },
    MEDIUM {
        override fun facetValue(): String = "medium"
        override fun getDisplayName(): Int = R.string.download_assets_category_configuration_medium
    },
    HIGH {
        override fun facetValue(): String = "high"
        override fun getDisplayName(): Int = R.string.download_assets_category_configuration_high
    },
    SCREENSHOT {
        override fun facetValue(): String = "screenshot"
        override fun getDisplayName(): Int = R.string.download_assets_category_screenshot
    };

    override fun facetName(): String = "categories"
    override fun index(): Int = this.ordinal
}

fun String.mapModrinthCategory(
    classes: PlatformClasses
): PlatformFilterCode? {
    val mapValues = when (classes) {
        PlatformClasses.MOD -> ModrinthModCategory.entries
        PlatformClasses.MOD_PACK -> ModrinthModpackCategory.entries
        PlatformClasses.RESOURCE_PACK -> ModrinthResourcePackCategory.entries
        PlatformClasses.SAVES -> null
        PlatformClasses.SHADERS -> ModrinthShadersCategory.entries
    }
    return mapValues?.find { it.facetValue() == this }
        ?: ModrinthFeatures.entries.find { it.facetValue() == this }
}