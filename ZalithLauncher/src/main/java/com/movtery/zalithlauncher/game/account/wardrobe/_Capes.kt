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

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile

/**
 * 空披风，可用来表示不选择、重置披风
 */
val EmptyCape = PlayerProfile.Cape("", "", "", "")

/**
 * @return 披风名称字符串资源
 */
@StringRes
fun PlayerProfile.Cape.capeLocalRes(): Int? {
    if (this == EmptyCape || id.isEmpty()) return R.string.cape_name_none

    val localeRes = when (alias) {
        "Migrator" -> R.string.cape_name_migrator
        "MapMaker" -> R.string.cape_name_mapmaker
        "Moderator" -> R.string.cape_name_moderator
        "Translator-Chinese" -> R.string.cape_name_translator_chinese
        "Translator" -> R.string.cape_name_translator
        "Cobalt" -> R.string.cape_name_cobalt
        "Vanilla" -> R.string.cape_name_vanilla
        "Minecon2011" -> R.string.cape_name_minecon2011
        "Minecon2012" -> R.string.cape_name_minecon2012
        "Minecon2013" -> R.string.cape_name_minecon2013
        "Minecon2015" -> R.string.cape_name_minecon2015
        "Minecon2016" -> R.string.cape_name_minecon2016
        "Cherry Blossom" -> R.string.cape_name_cherry_blossom
        "15th Anniversary" -> R.string.cape_name_15_th_anniversary
        "Purple Heart" -> R.string.cape_name_purple_heart
        "Follower's" -> R.string.cape_name_follower_s
        "MCC 15th Year" -> R.string.cape_name_mcc_15_th_year
        "Minecraft Experience" -> R.string.cape_name_minecraft_experience
        "Mojang Office" -> R.string.cape_name_mojang_office
        "Home" -> R.string.cape_name_home
        "Menace" -> R.string.cape_name_menace
        "Yearn" -> R.string.cape_name_yearn
        "Common" -> R.string.cape_name_common
        "Pan" -> R.string.cape_name_pan
        "Founder's" -> R.string.cape_name_founder_s
        "Copper" -> R.string.cape_name_copper
        "Zombie Horse" -> R.string.cape_name_zombie_horse
        "Builder" -> R.string.cape_name_builder
        "Crafter" -> R.string.cape_name_crafter
        else -> null
    }

    return localeRes
}