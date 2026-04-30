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

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeProject(
    @SerialName("data")
    val data: CurseForgeData
): PlatformProject {
    override fun platform(): Platform = Platform.CURSEFORGE

    override fun platformId(): String = data.id.toString()

    override fun platformClasses(defaultClasses: PlatformClasses): PlatformClasses {
        return data.getPlatformClassesOrNull() ?: defaultClasses
    }

    override fun platformSlug(): String = data.slug

    override fun platformIconUrl(): String? = data.logo?.url

    override fun platformTitle(): String = data.name

    override fun platformSummary(): String = data.summary

    override fun platformAuthor(): String = data.authors[0].name

    override fun platformDownloadCount(): Long = data.downloadCount

    override fun platformUrls(defaultClasses: PlatformClasses): PlatformProject.Urls {
        val classes = data.getPlatformClassesOrNull() ?: defaultClasses
        return PlatformProject.Urls(
            projectUrl = "https://www.curseforge.com/minecraft/${classes.curseforge.slug}/${data.slug}",
            sourceUrl = data.links.sourceUrl,
            issuesUrl = data.links.issuesUrl,
            wikiUrl = data.links.wikiUrl
        )
    }

    override fun platformScreenshots(): List<PlatformProject.Screenshot> {
        return data.screenshots.map { asset ->
            PlatformProject.Screenshot(
                imageUrl = asset.url,
                title = asset.title,
                description = asset.description
            )
        }
    }
}