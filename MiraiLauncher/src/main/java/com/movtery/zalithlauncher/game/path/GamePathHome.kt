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

package com.movtery.zalithlauncher.game.path

import java.io.File

private fun String.replaceSeparator(): String = this.replace("/", File.separator)

/** 当前选择的游戏目录 */
fun getGameHome(): String = GamePathManager.currentPath.value

/** 指定游戏目录下的 versions 文件夹 */
fun getVersionsHome(gameHome: String): String = "${gameHome}/versions".replaceSeparator()

fun getVersionsHome(): String = getVersionsHome(getGameHome())

/** 指定游戏目录下的 libraries 文件夹 */
fun getLibrariesHome(gameHome: String): String = "${gameHome}/libraries".replaceSeparator()

fun getLibrariesHome(): String = getLibrariesHome(getGameHome())

/** 指定游戏目录下的 assets 文件夹 */
fun getAssetsHome(gameHome: String): String = "${gameHome}/assets".replaceSeparator()

fun getAssetsHome(): String = getAssetsHome(getGameHome())

/** 指定游戏目录下的 resources 文件夹 */
fun getResourcesHome(gameHome: String): String = "${gameHome}/resources".replaceSeparator()

fun getResourcesHome(): String = getResourcesHome(getGameHome())