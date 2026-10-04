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

package com.movtery.zalithlauncher.game.download.game.models

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion

/**
 * 将 OptiFine 版本转换为 LaunchFor Info
 */
fun OptiFineVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.realVersion,
        name = ModLoader.OPTIFINE.displayName
    )
}

/**
 * 将 Forge Like 版本转换为 LaunchFor Info
 */
fun ForgeLikeVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.versionName,
        name = this.loaderName
    )
}

/**
 * 将 Fabric Like 版本转换为 LaunchFor Info
 */
fun FabricLikeVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.version,
        name = this.loaderName
    )
}