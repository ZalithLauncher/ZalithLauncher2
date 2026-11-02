/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.components

import com.movtery.zalithlauncher.utils.file.calculateFileSha1
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** assets 内用于校验的清单文件 */
const val ASSET_MANIFEST_FILE = "manifest.json"
/** 旧版 version 校验遗留文件 */
const val LEGACY_VERSION_FILE = "version"

private const val ASSET_MANIFEST_FORMAT = 1
private const val SHA1_ALGORITHM = "SHA-1"
private val SHA1_PATTERN = Regex("[0-9a-f]{40}")
private val manifestJson = Json { ignoreUnknownKeys = false }

/**
 * 组件 assets 的 SHA-1 清单
 * @property formatVersion 清单格式版本
 * @property component 组件在 assets 中的相对路径
 * @property algorithm 文件哈希算法，目前固定为 SHA-1
 * @property files 清单记录的文件
 */
@Serializable
data class AssetManifest(
    val formatVersion: Int,
    val component: String,
    val algorithm: String,
    val files: List<AssetManifestFile>
)

/**
 * @property path 相对于组件 assets 根目录的路径
 * @property size 文件大小(bytes)
 * @property sha1 文件内容的 SHA-1，小写十六进制
 */
@Serializable
data class AssetManifestFile(
    val path: String,
    val size: Long,
    val sha1: String
)

/**
 * 一个待安装文件及其源路径、目标路径和校验信息
 * @property assetPath 源文件相对于组件 assets 根目录的路径
 * @property outputPath 安装后相对于组件目录的路径
 * @property size 预期文件大小(bytes)
 * @property sha1 预期文件内容的 SHA-1，小写十六进制
 */
data class ComponentFile(
    val assetPath: String,
    val outputPath: String,
    val size: Long,
    val sha1: String
)

/**
 * @param outputPath 安装后的相对路径，默认与 assets 路径一致
 * @return 对应的安装文件
 */
fun AssetManifestFile.toComponentFile(outputPath: String = path): ComponentFile {
    validateComponentPath(outputPath)
    return ComponentFile(
        assetPath = path,
        outputPath = outputPath,
        size = size,
        sha1 = sha1
    )
}

/**
 * 解析并校验组件清单
 * @param json 清单 JSON 文本
 * @param expectedComponent 预期的组件 assets 路径
 */
fun parseAssetManifest(json: String, expectedComponent: String): AssetManifest {
    val manifest = manifestJson.decodeFromString(AssetManifest.serializer(), json)
    require(manifest.formatVersion == ASSET_MANIFEST_FORMAT) {
        "Unsupported asset manifest format: ${manifest.formatVersion}"
    }
    require(manifest.component == expectedComponent) {
        "Asset manifest component mismatch: ${manifest.component}"
    }
    require(manifest.algorithm == SHA1_ALGORITHM) {
        "Unsupported asset manifest algorithm: ${manifest.algorithm}"
    }
    require(manifest.files.isNotEmpty()) { "Asset manifest contains no files" }

    val paths = HashSet<String>(manifest.files.size)
    manifest.files.forEach { file ->
        validateComponentPath(file.path)
        require(file.path != ASSET_MANIFEST_FILE && file.path != LEGACY_VERSION_FILE) {
            "Asset manifest metadata must not be listed as a payload: ${file.path}"
        }
        require(paths.add(file.path)) { "Duplicate asset manifest path: ${file.path}" }
        require(file.size >= 0L) { "Invalid file size: ${file.path}" }
        require(SHA1_PATTERN.matches(file.sha1)) { "Invalid SHA-1: ${file.path}" }
    }
    return manifest
}

fun validateComponentFiles(files: List<ComponentFile>) {
    require(files.isNotEmpty()) { "Component contains no installable files" }
    val paths = HashSet<String>(files.size)
    files.forEach { file ->
        validateComponentPath(file.outputPath)
        require(paths.add(file.outputPath)) { "Duplicate component output path: ${file.outputPath}" }
    }
}

/**
 * 校验路径是否为安全的相对路径
 */
fun validateComponentPath(path: String) {
    require(path.isNotEmpty() && !path.startsWith('/') && !path.contains('\\')) {
        "Invalid component path: $path"
    }
    require(path.split('/').none { it.isEmpty() || it == "." || it == ".." }) {
        "Unsafe component path: $path"
    }
}

/**
 * 校验目录中的文件大小、SHA-1 及文件集合是否符合预期
 * @param directory 待校验的组件目录
 * @param files 预期的安装文件
 * @param ignoredFiles 扫描额外文件时忽略的相对路径
 * @return 所有文件均匹配且没有未忽略的额外文件时返回 true
 */
suspend fun verifyComponentDirectory(
    directory: File,
    files: List<ComponentFile>,
    ignoredFiles: Set<String> = emptySet()
): Boolean {
    if (!directory.isDirectory) return false

    files.forEach { expected ->
        val file = File(directory, expected.outputPath)
        if (!file.isFile || file.length() != expected.size) return false
        if (calculateFileSha1(file) != expected.sha1) return false
    }

    val expectedPaths = files.mapTo(HashSet(files.size)) { it.outputPath }
    val actualPaths = directory.walkTopDown()
        .filter { it.isFile }
        .map { it.relativeTo(directory).invariantSeparatorsPath }
        .filter { it !in ignoredFiles }
        .toSet()
    return actualPaths == expectedPaths
}