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

package com.movtery.zalithlauncher.game.account.microsoft.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XBLRequest(
    @SerialName("Properties")
    val properties: XBLProperties,
    @SerialName("RelyingParty")
    val relyingParty: String,
    @SerialName("TokenType")
    val tokenType: String
)

@Serializable
data class XBLProperties(
    @SerialName("AuthMethod")
    val authMethod: String,
    @SerialName("SiteName")
    val siteName: String,
    @SerialName("RpsTicket")
    val rpsTicket: String
)

@Serializable
data class XSTSRequest(
    @SerialName("Properties")
    val properties: XSTSProperties,
    @SerialName("RelyingParty")
    val relyingParty: String,
    @SerialName("TokenType")
    val tokenType: String
)

@Serializable
data class XSTSProperties(
    @SerialName("SandboxId")
    val sandboxId: String,
    @SerialName("UserTokens")
    val userTokens: List<String>
)

@Serializable
data class XSTSAuthResult(
    @SerialName("token")
    val token: String,
    @SerialName("uhs")
    val uhs: String
)