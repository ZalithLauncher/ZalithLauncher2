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

package com.movtery.zalithlauncher.game.plugin.vpl

import android.content.Context
import com.vpl.verifiedpluginload.api.VerifiedPluginLoadRegistry
import com.vpl.verifiedpluginload.model.KeyHash
import com.vpl.verifiedpluginload.model.TrustActionResult
import com.vpl.verifiedpluginload.model.TrustedAuthorInfo
import com.vpl.verifiedpluginload.model.UserTrustSnapshot

/** Builds user-facing trust records without hiding stale or uninstalled entries. */
object PluginTrustManager {

    data class TrustManagementData(
        val authorEntries: List<AuthorTrustEntry>,
        val keyEntries: List<KeyTrustEntry>,
        val recoveredFromCorruption: Boolean
    )

    data class AuthorTrustEntry(
        val authorUuid: String,
        /** Null when the signed list no longer describes this publisher; the record still shows. */
        val author: TrustedAuthorInfo?,
        val affectedPlugins: List<InstalledPlugin>
    ) {
        val displayName: String get() = author?.name ?: authorUuid
    }

    data class KeyTrustEntry(
        val keyHash: KeyHash,
        val affectedPlugins: List<InstalledPlugin>
    )

    data class InstalledPlugin(
        val packageName: String,
        val label: String,
        val versionName: String?,
        val type: PluginType,
        val authorUuid: String?,
        val currentSignatures: List<KeyHash>
    )

    suspend fun load(context: Context): TrustManagementData {
        val vpl = VerifiedPluginLoadRegistry.get(context)
        val installedPlugins = PluginCandidateRepository.allInstalled().map { candidate ->
            val result = vpl.inspectInstalledPackage(candidate.packageName)
            val label = result.packageInfo.applicationLabel?.takeIf { it.isNotBlank() }
                ?: candidate.packageName
            InstalledPlugin(
                packageName = candidate.packageName,
                label = label,
                versionName = result.packageInfo.versionName,
                type = candidate.type,
                authorUuid = result.author?.uuid,
                currentSignatures = result.currentSignatures
            )
        }
        return build(vpl.getUserTrustSnapshot(), vpl.getTrustedAuthors(), installedPlugins)
    }

    internal fun build(
        snapshot: UserTrustSnapshot,
        trustedAuthors: List<TrustedAuthorInfo>,
        installedPlugins: List<InstalledPlugin>
    ): TrustManagementData {
        val authorsByUuid = trustedAuthors.associateBy { it.uuid }

        val authorEntries = snapshot.trustedAuthorUuids.map { authorUuid ->
            AuthorTrustEntry(
                authorUuid = authorUuid,
                author = authorsByUuid[authorUuid],
                affectedPlugins = installedPlugins.filter { it.authorUuid == authorUuid }
            )
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName })

        val keyEntries = snapshot.trustedKeyHashes.map { keyHash ->
            KeyTrustEntry(
                keyHash = keyHash,
                affectedPlugins = installedPlugins.filter { keyHash in it.currentSignatures }
            )
        }.sortedBy { it.keyHash.sha256 }

        return TrustManagementData(authorEntries, keyEntries, snapshot.recoveredFromCorruption)
    }

    suspend fun revokeAuthor(context: Context, authorUuid: String): TrustActionResult =
        VerifiedPluginLoadRegistry.get(context).revokeAuthorTrust(authorUuid)

    suspend fun revokeKey(context: Context, keyHash: KeyHash): TrustActionResult =
        VerifiedPluginLoadRegistry.get(context).revokeKeyHashTrust(keyHash.value)
}
