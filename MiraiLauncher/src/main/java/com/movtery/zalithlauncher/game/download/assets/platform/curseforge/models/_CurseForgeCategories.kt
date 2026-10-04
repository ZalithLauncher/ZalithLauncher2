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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode

/**
 * CurseForge类别过滤器
 */
interface CurseForgeCategory {
    /**
     * 转换为CurseForge接受的格式
     */
    fun describe(): String
}

/**
 * CurseForge 模组下载类别
 */
enum class CurseForgeModCategory : CurseForgeCategory, PlatformFilterCode {
    WORLDGEN {
        override fun describe(): String = "406"
        override fun getDisplayName(): Int = R.string.download_assets_category_worldgen
    },
    BIOMES {
        override fun describe(): String = "407"
        override fun getDisplayName(): Int = R.string.download_assets_category_biomes
    },
    DIMENSIONS {
        override fun describe(): String = "410"
        override fun getDisplayName(): Int = R.string.download_assets_category_dimensions
    },
    ORES_RESOURCES {
        override fun describe(): String = "408"
        override fun getDisplayName(): Int = R.string.download_assets_category_ores_resources
    },
    STRUCTURES {
        override fun describe(): String = "409"
        override fun getDisplayName(): Int = R.string.download_assets_category_structures
    },
    TECHNOLOGY {
        override fun describe(): String = "412"
        override fun getDisplayName(): Int = R.string.download_assets_category_technology
    },
    ITEM_FLUID_ENERGY_TRANSPORT {
        override fun describe(): String = "415"
        override fun getDisplayName(): Int = R.string.download_assets_category_item_fluid_energy_transport
    },
    AUTOMATION {
        override fun describe(): String = "4843"
        override fun getDisplayName(): Int = R.string.download_assets_category_automation
    },
    ENERGY {
        override fun describe(): String = "417"
        override fun getDisplayName(): Int = R.string.download_assets_category_energy
    },
    REDSTONE {
        override fun describe(): String = "4558"
        override fun getDisplayName(): Int = R.string.download_assets_category_redstone
    },
    FOOD {
        override fun describe(): String = "436"
        override fun getDisplayName(): Int = R.string.download_assets_category_food
    },
    FARMING {
        override fun describe(): String = "416"
        override fun getDisplayName(): Int = R.string.download_assets_category_farming
    },
    TRANSPORT {
        override fun describe(): String = "414"
        override fun getDisplayName(): Int = R.string.download_assets_category_transport
    },
    STORAGE {
        override fun describe(): String = "420"
        override fun getDisplayName(): Int = R.string.download_assets_category_storage
    },
    MAGIC {
        override fun describe(): String = "419"
        override fun getDisplayName(): Int = R.string.download_assets_category_magic
    },
    ADVENTURE {
        override fun describe(): String = "422"
        override fun getDisplayName(): Int = R.string.download_assets_category_adventure
    },
    DECORATION {
        override fun describe(): String = "424"
        override fun getDisplayName(): Int = R.string.download_assets_category_decoration
    },
    MOBS {
        override fun describe(): String = "411"
        override fun getDisplayName(): Int = R.string.download_assets_category_mobs
    },
    EQUIPMENT {
        override fun describe(): String = "434"
        override fun getDisplayName(): Int = R.string.download_assets_category_equipment
    },
    INFORMATION {
        override fun describe(): String = "423"
        override fun getDisplayName(): Int = R.string.download_assets_category_information
    },
    PERFORMANCE {
        override fun describe(): String = "6814"
        override fun getDisplayName(): Int = R.string.download_assets_category_optimization
    },
    SOCIAL {
        override fun describe(): String = "435"
        override fun getDisplayName(): Int = R.string.download_assets_category_social
    },
    UTILITY {
        override fun describe(): String = "5191"
        override fun getDisplayName(): Int = R.string.download_assets_category_utility
    },
    LIBRARY {
        override fun describe(): String = "421"
        override fun getDisplayName(): Int = R.string.download_assets_category_library
    },
    MISCELLANEOUS {
        override fun describe(): String = "6947"
        override fun getDisplayName(): Int = R.string.download_assets_category_miscellaneous
    };

    override fun index(): Int = this.ordinal
}

/**
 * CurseForge 整合包下载类别
 */
enum class CurseForgeModpackCategory : CurseForgeCategory, PlatformFilterCode {
    MULTIPLAYER {
        override fun describe(): String = "4484"
        override fun getDisplayName(): Int = R.string.download_assets_category_multiplayer
    },
    CHALLENGING {
        override fun describe(): String = "4479"
        override fun getDisplayName(): Int = R.string.download_assets_category_challenging
    },
    COMBAT {
        override fun describe(): String = "4483"
        override fun getDisplayName(): Int = R.string.download_assets_category_combat
    },
    QUESTS {
        override fun describe(): String = "4478"
        override fun getDisplayName(): Int = R.string.download_assets_category_quests
    },
    TECHNOLOGY {
        override fun describe(): String = "4472"
        override fun getDisplayName(): Int = R.string.download_assets_category_technology
    },
    MAGIC {
        override fun describe(): String = "4473"
        override fun getDisplayName(): Int = R.string.download_assets_category_magic
    },
    ADVENTURE {
        override fun describe(): String = "4475"
        override fun getDisplayName(): Int = R.string.download_assets_category_adventure
    },
    EXPLORATION {
        override fun describe(): String = "4476"
        override fun getDisplayName(): Int = R.string.download_assets_category_exploration
    },
    MINI_GAME {
        override fun describe(): String = "4477"
        override fun getDisplayName(): Int = R.string.download_assets_category_mini_game
    },
    SCI_FI {
        override fun describe(): String = "4471"
        override fun getDisplayName(): Int = R.string.download_assets_category_sci_fi
    },
    SKYBLOCK {
        override fun describe(): String = "4736"
        override fun getDisplayName(): Int = R.string.download_assets_category_skyblock
    },
    VANILLA {
        override fun describe(): String = "5128"
        override fun getDisplayName(): Int = R.string.download_assets_category_vanilla
    },
    FTB {
        override fun describe(): String = "4487"
        override fun getDisplayName(): Int = R.string.download_assets_category_ftb
    },
    MAP_BASED {
        override fun describe(): String = "4480"
        override fun getDisplayName(): Int = R.string.download_assets_category_map_based
    },
    LIGHTWEIGHT {
        override fun describe(): String = "4481"
        override fun getDisplayName(): Int = R.string.download_assets_category_lightweight
    },
    EXTRA_LARGE {
        override fun describe(): String = "4482"
        override fun getDisplayName(): Int = R.string.download_assets_category_extra_large
    };

    override fun index(): Int = this.ordinal
}

/**
 * CurseForge 资源包下载类别
 */
enum class CurseForgeResourcePackCategory : CurseForgeCategory, PlatformFilterCode {
    TRADITIONAL {
        override fun describe(): String = "403"
        override fun getDisplayName(): Int = R.string.download_assets_category_traditional
    },
    STEAMPUNK {
        override fun describe(): String = "399"
        override fun getDisplayName(): Int = R.string.download_assets_category_steampunk
    },
    MODERN {
        override fun describe(): String = "401"
        override fun getDisplayName(): Int = R.string.download_assets_category_modern
    },
    PHOTO_REALISTIC {
        override fun describe(): String = "400"
        override fun getDisplayName(): Int = R.string.download_assets_category_photo_realistic
    },
    ANIMATED {
        override fun describe(): String = "404"
        override fun getDisplayName(): Int = R.string.download_assets_category_animated
    },
    MOD_SUPPORT {
        override fun describe(): String = "4465"
        override fun getDisplayName(): Int = R.string.download_assets_category_mod_support
    },
    MISCELLANEOUS {
        override fun describe(): String = "405"
        override fun getDisplayName(): Int = R.string.download_assets_category_miscellaneous
    },
    X_16 {
        override fun describe(): String = "393"
        override fun getDisplayName(): Int = R.string.download_assets_category_16x
    },
    X_32 {
        override fun describe(): String = "394"
        override fun getDisplayName(): Int = R.string.download_assets_category_32x
    },
    X_64 {
        override fun describe(): String = "395"
        override fun getDisplayName(): Int = R.string.download_assets_category_64x
    },
    X_128 {
        override fun describe(): String = "396"
        override fun getDisplayName(): Int = R.string.download_assets_category_128x
    },
    X_256 {
        override fun describe(): String = "397"
        override fun getDisplayName(): Int = R.string.download_assets_category_256x
    },
    X_512 {
        override fun describe(): String = "398"
        override fun getDisplayName(): Int = R.string.download_assets_category_512x
    };

    override fun index(): Int = this.ordinal
}

/**
 * CurseForge 存档下载类别
 */
enum class CurseForgeSavesCategory : CurseForgeCategory, PlatformFilterCode {
    ADVENTURE {
        override fun describe(): String = "253"
        override fun getDisplayName(): Int = R.string.saves_manage_gamemode_adventure
    },
    SURVIVAL {
        override fun describe(): String = "248"
        override fun getDisplayName(): Int = R.string.saves_manage_gamemode_survival
    },
    CREATION {
        override fun describe(): String = "249"
        override fun getDisplayName(): Int = R.string.saves_manage_gamemode_creative
    },
    GAME_MAP {
        override fun describe(): String = "250"
        override fun getDisplayName(): Int = R.string.download_assets_category_game_map
    },
    MODDED_WORLD {
        override fun describe(): String = "4464"
        override fun getDisplayName(): Int = R.string.download_assets_category_modded_world
    },
    PARKOUR {
        override fun describe(): String = "251"
        override fun getDisplayName(): Int = R.string.download_assets_category_parkour
    },
    PUZZLE {
        override fun describe(): String = "252"
        override fun getDisplayName(): Int = R.string.download_assets_category_puzzle
    };

    override fun index(): Int = this.ordinal
}

/**
 * CurseForge 光影包下载类别
 */
enum class CurseForgeShadersCategory : CurseForgeCategory, PlatformFilterCode {
    FANTASY {
        override fun describe(): String = "6554"
        override fun getDisplayName(): Int = R.string.download_assets_category_fantasy
    },
    REALISTIC {
        override fun describe(): String = "6553"
        override fun getDisplayName(): Int = R.string.download_assets_category_photo_realistic
    },
    VANILLA {
        override fun describe(): String = "6555"
        override fun getDisplayName(): Int = R.string.download_assets_category_vanilla
    };

    override fun index(): Int = this.ordinal
}

fun String.mapCurseForgeCategory(classes: PlatformClasses): PlatformFilterCode? {
    val mapValues = when (classes) {
        PlatformClasses.MOD -> CurseForgeModCategory.entries
        PlatformClasses.MOD_PACK -> CurseForgeModpackCategory.entries
        PlatformClasses.RESOURCE_PACK -> CurseForgeResourcePackCategory.entries
        PlatformClasses.SAVES -> CurseForgeSavesCategory.entries
        PlatformClasses.SHADERS -> CurseForgeShadersCategory.entries
    }
    return mapValues.find { it.describe() == this }
}