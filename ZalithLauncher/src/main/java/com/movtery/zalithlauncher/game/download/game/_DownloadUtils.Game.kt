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

package com.movtery.zalithlauncher.game.download.game

import com.google.gson.JsonObject
import com.movtery.zalithlauncher.game.download.game.models.LibraryComponents
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.utils.file.ensureDirectory
import com.movtery.zalithlauncher.utils.json.parseToJson
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

private const val TAG = "DownloadGame"

fun GameManifest.isOldVersion(): Boolean = !minecraftArguments.isNullOrEmpty()

/**
 * 尝试读取指定路径的文件为一个JsonObject对象
 * 若格式不正确则抛出异常
 */
fun String?.getJsonOrNull(tag: String): JsonObject? {
    return this?.let { path ->
        val text: String = File(path).takeIf { it.exists() && it.isFile }?.readText() ?: run {
            Logger.warning(TAG, "The $tag json file is invalid!")
            return@let null
        }
        if (!text.startsWith("{")) {
            Logger.warning(TAG, "The $tag JSON is invalid, first part of the content: ${text.take(1000)}")
            return@let null
        }
        text.parseToJson()
    }
}

/**
 * 复制jar、json文件到临时游戏目录，作为安装ModLoader的环境
 * @param sourceGameFolder 源游戏目录
 * @param sourceVersion 源游戏版本名
 * @param destinationGameFolder 要复制到的游戏目录
 * @param targetVersion 要复制为的版本名称
 * @param filesToCopy 指定要复制的文件的后缀名
 */
fun copyVanillaFiles(
    sourceGameFolder: File,
    sourceVersion: String,
    destinationGameFolder: File,
    targetVersion: String,
    filesToCopy: List<String> = listOf(".json", ".jar")
) {
    val sourceDir = File(sourceGameFolder, "versions/$sourceVersion").ensureDirectory()
    val destinationDir = File(destinationGameFolder, "versions/$targetVersion").ensureDirectory()

    for (extension in filesToCopy) {
        val sourceFile = File(sourceDir, "$sourceVersion$extension")
        val destinationFile = File(destinationDir, "$targetVersion$extension")

        if (!destinationFile.exists() && sourceFile.exists()) {
            sourceFile.copyTo(destinationFile)
        }
    }
}

/**
 * 根据提供的原始库名称生成对应的本地路径。
 * @param original 库的原始名称，例如 `groupId:artifactId:version`
 * @param baseFolder 基础文件夹路径，作为文件路径前缀，为 null 则不连接
 */
fun getLibraryPath(
    original: String,
    baseFolder: String? = null
): String {
    val components = parseLibraryComponents(original)

    // 处理 OptiFine 特殊情况
    if (isOptiFineLibrary(components.groupId, components.artifactId, components.version)) {
        val specialPath = handleOptiFineSpecialCase(
            baseFolder = baseFolder,
            groupId = components.groupId,
            artifactId = components.artifactId,
            version = components.version
        )
        if (specialPath != null) return specialPath
    }

    val groupIdPath = components.groupId.replace(".", File.separator)
    val classifierSuffix = if (!components.classifier.isNullOrEmpty()) "-${components.classifier}" else ""
    val jarName = "${components.artifactId}-${components.version}$classifierSuffix.jar"

    return listOfNotNull(
        baseFolder?.let { "$it/libraries" },
        groupIdPath,
        components.artifactId,
        components.version,
        jarName
    ).joinToString(File.separator)
}

/**
 * 解析原始库名称字符串为组件（groupId、artifactId、version）
 */
fun parseLibraryComponents(original: String): LibraryComponents {
    val components = original.split(":")
    require(components.size >= 3) { "Invalid library name: $original" }
    return LibraryComponents(
        groupId = components[0],
        artifactId = components[1],
        version = components[2],
        classifier = components.getOrNull(3)
    )
}

private fun buildArtifactPath(
    baseFolder: String?,
    groupId: String,
    artifactId: String,
    version: String,
    jarName: String
): String = buildString {
    baseFolder?.let { append(it).append(File.separator).append("libraries").append(File.separator) }
    append(listOf(groupId, artifactId, version).joinToString(File.separator))
    append(File.separator).append(jarName)
}

private fun isOptiFineLibrary(
    groupId: String,
    artifactId: String,
    version: String
) = groupId == "optifine" && artifactId == "OptiFine" && version.startsWith("1.")

private fun handleOptiFineSpecialCase(
    baseFolder: String?,
    groupId: String,
    artifactId: String,
    version: String
): String? {
    val (major, minor) = parseOptiFineVersion(version)
    if (!shouldUseInstaller(major, minor)) return null

    val installerJarName = "$artifactId-$version-installer.jar"
    val installerPath = buildArtifactPath(baseFolder, groupId, artifactId, version, installerJarName)

    return installerPath.takeIf { File(it).exists() }
}

private fun parseOptiFineVersion(version: String): Pair<Int, Int> {
    val parts = version.split(".", "_")
    return Pair(
        parts.getOrNull(1)?.toIntOrNull() ?: 0,
        parts.getOrNull(2)?.toIntOrNull() ?: 0
    )
}

private fun shouldUseInstaller(major: Int, minor: Int) =
    major == 12 || (major == 20 && minor >= 4) || major >= 21
