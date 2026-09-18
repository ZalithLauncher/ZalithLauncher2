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
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode

enum class ModrinthFeatures : ModrinthFacet, PlatformFilterCode {
    AUDIO {
        override fun facetValue(): String = "audio"
        override fun getDisplayName(): Int = R.string.download_assets_features_audio
    },
    BLOCKS {
        override fun facetValue(): String = "blocks"
        override fun getDisplayName(): Int = R.string.download_assets_features_blocks
    },
    CORE_SHADERS {
        override fun facetValue(): String = "core-shaders"
        override fun getDisplayName(): Int = R.string.download_assets_features_core_shaders
    },
    ENTITIES {
        override fun facetValue(): String = "entities"
        override fun getDisplayName(): Int = R.string.download_assets_features_entities
    },
    ENVIRONMENT {
        override fun facetValue(): String = "environment"
        override fun getDisplayName(): Int = R.string.download_assets_features_environment
    },
    FONTS {
        override fun facetValue(): String = "fonts"
        override fun getDisplayName(): Int = R.string.download_assets_features_fonts
    },
    GUI {
        override fun facetValue(): String = "gui"
        override fun getDisplayName(): Int = R.string.download_assets_features_gui
    },
    ITEMS {
        override fun facetValue(): String = "items"
        override fun getDisplayName(): Int = R.string.download_assets_features_items
    },
    LOCALE {
        override fun facetValue(): String = "locale"
        override fun getDisplayName(): Int = R.string.download_assets_features_locale
    },
    MODELS {
        override fun facetValue(): String = "models"
        override fun getDisplayName(): Int = R.string.download_assets_features_models
    };

    override fun facetName(): String = "features"
    override fun index(): Int = this.ordinal
}