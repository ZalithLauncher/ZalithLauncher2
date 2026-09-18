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
import kotlinx.serialization.json.JsonElement

@Serializable
class AuthResult(
    @SerializedName("accessToken")
    @SerialName("accessToken")
    val accessToken: String,
    @SerializedName("clientToken")
    @SerialName("clientToken")
    var clientToken: String,
    @SerializedName("availableProfiles")
    @SerialName("availableProfiles")
    var availableProfiles: List<AvailableProfiles>? = null,
    @SerializedName("user")
    @SerialName("user")
    var user: User? = null,
    @SerializedName("selectedProfile")
    @SerialName("selectedProfile")
    var selectedProfile: SelectedProfile? = null
) {
    @Serializable
    class User(
        @SerializedName("id")
        @SerialName("id")
        var id: String,
        @SerializedName("properties")
        @SerialName("properties")
        var properties: JsonElement? = null
    )

    @Serializable
    class SelectedProfile(
        @SerializedName("id")
        @SerialName("id")
        var id: String,
        @SerializedName("name")
        @SerialName("name")
        var name: String
    )

    @Serializable
    class AvailableProfiles(
        @SerializedName("id")
        @SerialName("id")
        var id: String,
        @SerializedName("name")
        @SerialName("name")
        var name: String
    )
}