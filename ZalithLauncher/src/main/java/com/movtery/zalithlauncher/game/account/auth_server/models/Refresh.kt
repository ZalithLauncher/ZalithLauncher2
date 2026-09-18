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

package com.movtery.zalithlauncher.game.account.auth_server.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Refresh(
    @SerializedName("selectedProfile")
    @SerialName("selectedProfile")
    var selectedProfile: SelectedProfile? = null,
    @SerializedName("accessToken")
    @SerialName("accessToken")
    var accessToken: String,
    @SerializedName("clientToken")
    @SerialName("clientToken")
    var clientToken: String
) {
    @Serializable
    class SelectedProfile(
        @SerializedName("name")
        @SerialName("name")
        var name: String,
        @SerializedName("id")
        @SerialName("id")
        var id: String
    )
}