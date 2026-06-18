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

package com.movtery.zalithlauncher.game.download.assets.platform.modrinth

import com.movtery.zalithlauncher.game.download.assets.platform.AbstractPlatformSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.isPublic
import com.movtery.zalithlauncher.utils.network.httpGetJson
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.Parameters
import io.ktor.server.plugins.NotFoundException
import java.io.File

class ModrinthSearcher(
    val api: String = MODRINTH_API,
    source: String = "Official Modrinth"
): AbstractPlatformSearcher(
    platform = Platform.MODRINTH,
    source = source
) {
    override suspend fun searchAssets(
        query: String,
        searchFilter: PlatformSearchFilter,
        platformClasses: PlatformClasses
    ): ModrinthSearchResult {
        return httpGetJson(
            url = "$api/search",
            parameters = searchFilter.toModrinthRequest(
                query = query,
                platformClasses = platformClasses
            ).toParameters()
        )
    }

    override suspend fun getProject(projectID: String): ModrinthSingleProject {
        val project = httpGetJson<ModrinthSingleProject>(
            url = "$api/project/$projectID"
        )
        if (!project.isPublic()) throw NotFoundException("The project {$projectID} is not in a publicly available state.")
        return project
    }

    /**
     * 获取 Modrinth 项目的版本列表（可设置区间）
     * @param pageSize 每页请求数量，null则为获取所有版本
     * @param offset 开始处，null则为获取所有版本
     */
    suspend fun getVersionsChunk(
        projectID: String,
        pageSize: Int? = null,
        offset: Int? = null,
    ): List<ModrinthVersion> {
        return httpGetJson(
            url = "$api/project/$projectID/version",
            parameters = if (pageSize != null && offset != null) {
                Parameters.build {
                    append("limit", pageSize.toString())
                    append("offset", offset.toString())
                    append("include_changelog", "false")
                }
            } else null
        )
    }

    override suspend fun getVersions(
        projectID: String,
        pageCallback: (chunk: Int, page: Int) -> Unit
    ): List<ModrinthVersion> {
        return getVersionsChunk(
            projectID = projectID
        )
    }

    override suspend fun getVersionByLocalFile(
        file: File,
        sha1: String
    ): ModrinthVersion? {
        return try {
            httpGetJson(
                url = "$api/version_file/$sha1",
                parameters = Parameters.build {
                    append("algorithm", "sha1")
                }
            )
        } catch (_: ClientRequestException) {
            null
        }
    }
}