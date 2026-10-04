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
class AuthRequest(
    @SerializedName("agent")
    @SerialName("agent")
    var agent: Agent,
    @SerializedName("username")
    @SerialName("username")
    var username: String,
    @SerializedName("password")
    @SerialName("password")
    var password: String,
    @SerializedName("clientToken")
    @SerialName("clientToken")
    var clientToken: String,
    @SerializedName("requestUser")
    @SerialName("requestUser")
    var requestUser: Boolean
) {
    @Serializable
    class Agent(
        @SerializedName("name")
        @SerialName("name")
        var name: String,
        @SerializedName("version")
        @SerialName("version")
        var version: Int
    )
}