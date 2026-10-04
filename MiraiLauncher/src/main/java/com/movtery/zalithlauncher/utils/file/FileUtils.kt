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

package com.movtery.zalithlauncher.utils.file

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.naturalCompare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile as CompressZipFile

private const val TAG = "FileUtils"

fun File.ifExists() = this.takeIf { it.exists() }

fun compareSHA1(file: File, sourceSHA: String?, default: Boolean = false): Boolean {
    if (!file.exists()) return false //文件不存在

    val computedSHA = runCatching {
        FileInputStream(file).use { fis ->
            String(Hex.encodeHex(DigestUtils.sha1(fis)))
        }
    }.getOrElse { e ->
        Logger.info(TAG, "An exception occurred while reading, returning the default value.", e)
        return default
    }

    return sourceSHA?.equals(computedSHA, ignoreCase = true) ?: default
}

suspend fun calculateFileSha1(file: File): String = withContext(Dispatchers.IO) {
    require(file.exists()) { "File does not exist: ${file.absolutePath}" }
    require(file.isFile) { "Path is not a file: ${file.absolutePath}" }

    val digest = MessageDigest.getInstance("SHA-1")
    file.inputStream().use { stream ->
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            ensureActive()
            digest.update(buffer, 0, bytesRead)
        }
    }
    digest.digest().joinToString("") { "%02x".format(it) }
}

@SuppressLint("DefaultLocale")
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB")
    var unitIndex = 0
    var value = bytes.toDouble()
    //循环获取合适的单位
    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024.0
        unitIndex++
    }
    return String.format("%.2f %s", value, units[unitIndex])
}

fun sortWithFileName(o1: File, o2: File): Int {
    val isDir1 = o1.isDirectory
    val isDir2 = o2.isDirectory

    //目录排在前面，文件排在后面
    if (isDir1 && !isDir2) return -1
    if (!isDir1 && isDir2) return 1

    return naturalCompare(o1.name, o2.name)
}

const val INVALID_CHARACTERS_REGEX = "[\\\\/:*?\"<>|\\t\\n]"

@Throws(InvalidFilenameException::class)
fun checkFilenameValidity(str: String) {
    val illegalCharsRegex = INVALID_CHARACTERS_REGEX.toRegex()

    val illegalChars = illegalCharsRegex.findAll(str)
        .map { it.value }
        .distinct()
        .toMutableSet()

    //防止路径穿越
    if (str.contains("..")) {
        throw InvalidFilenameException("Filename contains path traversal sequence '..'", "..")
    }
    if (str.startsWith("/") || str.startsWith("\\")) {
        throw InvalidFilenameException("Filename cannot start with '/' or '\\'", str.first().toString())
    }

    findAllUnsafeUnicodeChars(str).takeIf { it.isNotEmpty() }?.let { chars ->
        illegalChars += chars
    }

    if (illegalChars.isNotEmpty()) {
        throw InvalidFilenameException("The filename contains illegal characters", illegalChars.joinToString(""))
    }

    if (str.length > 255) {
        throw InvalidFilenameException("Invalid filename length", str.length)
    }

    if (str.startsWith(" ") || str.endsWith(" ")) {
        throw InvalidFilenameException("The filename starts or ends with a space", true)
    }
}

/**
 * 在字符串中查找其不安全的Unicode字符（作为文件名使用时）
 * @return 找到的所有不安全的字符
 */
fun findAllUnsafeUnicodeChars(name: String?): List<String> {
    if (name.isNullOrEmpty()) return emptyList()

    val unsafeChars = mutableListOf<String>()
    var i = 0
    while (i < name.length) {
        val cp = name.codePointAt(i)
        if (cp < 0x20 || (cp in 0xD800..0xDFFF) || cp > 0xFFFF) {
            unsafeChars += String(Character.toChars(cp))
        }
        i += Character.charCount(cp)
    }
    return unsafeChars
}

/**
 * Same as ensureDirectorySilently(), but throws an IOException telling why the check failed.
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/e492223/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/FileUtils.java#L61-L71)
 * @throws IOException when the checks fail
 */
@Throws(IOException::class)
fun File.ensureDirectory(): File {
    if (isFile) throw IOException("Target directory is a file, path = $this")
    if (exists()) {
        if (!canWrite()) throw IOException("Target directory is not writable, path = $this")
    } else {
        if (!mkdirs()) throw IOException("Unable to create target directory, path = $this")
    }
    return this
}

/**
 * Same as ensureParentDirectorySilently(), but throws an IOException telling why the check failed.
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/e492223/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/FileUtils.java#L73-L82)
 * @throws IOException when the checks fail
 */
@Throws(IOException::class)
fun File.ensureParentDirectory(): File {
    val parentDir: File = parentFile ?: throw IOException("targetFile does not have a parent, path = $this")
    parentDir.ensureDirectory()
    return this
}

fun File.ensureDirectorySilently(): Boolean {
    if (isFile) return false
    return if (exists()) canWrite()
    else mkdirs()
}

fun File.child(vararg paths: String): File {
    return paths.fold(this) { acc, path ->
        File(acc, path.trim().removeSurrounding("/").removeSurrounding("\\"))
    }
}

fun InputStream.readString(): String {
    return use {
        IOUtils.toString(this, StandardCharsets.UTF_8)
    }
}

fun shareFile(
    context: Context,
    file: File,
    cantProcess: () -> Unit = {}
) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(shareIntent, file.name)
    // 兼容非 Activity 上下文发起分享
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(chooserIntent)
    } catch (_: ActivityNotFoundException) {
        cantProcess()
    }
}

/**
 * 读取压缩包内文件的文本内容
 * @param readSource 使用指定方式读取文本，比如使用 UTF-8 读取
 */
fun ZipFile.readText(
    entryPath: String,
    readSource: BufferedSource.() -> String = {
        readUtf8()
    }
): String = getEntry(entryPath)
    .readText(zip = this, readSource = readSource)

/**
 * 读取压缩包内文件的文本内容
 * @param readSource 使用指定方式读取文本，比如使用 UTF-8 读取
 */
fun ZipEntry.readText(
    zip: ZipFile,
    readSource: BufferedSource.() -> String = {
        readUtf8()
    }
): String {
    return zip.getInputStream(this)
        .source()
        .buffer()
        .use { bufferedSource ->
            bufferedSource.readSource()
        }
}

/**
 * 从ZIP文件中提取指定内部路径下的所有条目到输出目录，保持相对路径结构
 * @param internalPath ZIP文件中的路径前缀（类似目录），留空则解压整个压缩包
 * @param outputDir 目标输出目录（必须为目录）
 * @throws IllegalArgumentException 如果路径不存在或参数无效
 * @throws SecurityException 如果检测到路径穿越攻击
 */
suspend fun ZipFile.extractFromZip(internalPath: String, outputDir: File) {
    val e = entries()
    val iterator = object : Iterator<JavaZipEntryAdapter> {
        override fun hasNext(): Boolean = e.hasMoreElements()
        override fun next(): JavaZipEntryAdapter =
            JavaZipEntryAdapter(e.nextElement())
    }

    extractZipEntries(
        entriesIter = iterator,
        inputStreamProvider = { entry -> getInputStream(entry.entry) },
        internalPath = internalPath,
        outputDir = outputDir
    )
}

/**
 * 从ZIP文件中提取指定内部路径下的所有条目到输出目录，保持相对路径结构
 * @param internalPath ZIP文件中的路径前缀（类似目录），留空则解压整个压缩包
 * @param outputDir 目标输出目录（必须为目录）
 * @throws IllegalArgumentException 如果路径不存在或参数无效
 * @throws SecurityException 如果检测到路径穿越攻击
 */
suspend fun CompressZipFile.extractFromZip(internalPath: String, outputDir: File) {
    val entriesEnum = entries
    val iterator = object : Iterator<CompressZipEntryAdapter> {
        private val it = entriesEnum.iterator()
        override fun hasNext(): Boolean = it.hasNext()
        override fun next(): CompressZipEntryAdapter =
            CompressZipEntryAdapter(it.next() as ZipArchiveEntry)
    }

    extractZipEntries(
        entriesIter = iterator,
        inputStreamProvider = { entry -> getInputStream(entry.entry) },
        internalPath = internalPath,
        outputDir = outputDir
    )
}

/**
 * 抽象核心提取逻辑，适用于任何类型的 ZIP 条目
 */
private suspend fun <T : ZipEntryBase> extractZipEntries(
    entriesIter: Iterator<T>,
    inputStreamProvider: (T) -> InputStream,
    internalPath: String,
    outputDir: File
) {
    require(outputDir.isDirectory || outputDir.mkdirs()) {
        "Output directory does not exist and cannot be created: $outputDir"
    }

    val prefix = when {
        internalPath.isEmpty() -> ""
        internalPath.endsWith("/") -> internalPath
        else -> "$internalPath/"
    }

    val rootPath = outputDir.absoluteFile.toPath().normalize()

    val createdDirs = HashSet<String>()

    withContext(Dispatchers.IO) {
        while (entriesIter.hasNext()) {
            ensureActive()

            val entry = entriesIter.next()
            val name = entry.name

            //忽略非目标目录
            if (!name.startsWith(prefix)) continue

            val relative = name.removePrefix(prefix)
            if (relative.isEmpty()) continue

            //防止路径穿越
            if (relative.contains("../") || relative.contains("..\\")) {
                throw SecurityException("Illegal path traversal detected: $name")
            }

            val targetFile = File(outputDir, relative)
            val targetPath = targetFile.toPath().normalize()

            if (!targetPath.startsWith(rootPath)) {
                throw SecurityException("Illegal path outside output directory: $name")
            }

            if (entry.isDirectory) {
                val absDir = targetFile.absolutePath
                if (createdDirs.add(absDir)) {
                    targetFile.mkdirs()
                }
                continue
            }

            val parent = targetFile.parentFile!!
            val parentPath = parent.absolutePath
            if (createdDirs.add(parentPath)) {
                parent.mkdirs()
            }

            inputStreamProvider(entry).source().use { source ->
                targetFile.sink().buffer().use { sink ->
                    sink.writeAll(source)
                }
            }
        }
    }
}

/**
 * 提取指定ZIP条目到独立文件
 * @param entryPath ZIP文件中的完整条目路径
 * @param outputFile 目标输出文件路径
 * @throws IllegalArgumentException 如果条目不存在或是目录
 * @throws SecurityException 如果输出文件路径不合法
 */
fun ZipFile.extractEntryToFile(entryPath: String, outputFile: File) {
    val entry = getEntry(entryPath) ?: throw IllegalArgumentException("ZIP entry does not exist: $entryPath")
    this.extractEntryToFile(entry, outputFile)
}

/**
 * 提取指定ZIP条目到独立文件
 * @param outputFile 目标输出文件路径
 * @throws IllegalArgumentException 如果条目是目录
 * @throws SecurityException 如果输出文件路径不合法
 */
fun ZipFile.extractEntryToFile(entry: ZipEntry, outputFile: File) {
    require(!entry.isDirectory) { "Cannot extract directory to file: ${entry.name}" }

    outputFile.ensureParentDirectory()

    getInputStream(entry).use { input ->
        outputFile.outputStream().use { output ->
            input.source().buffer().use { source ->
                output.sink().buffer().use { sink ->
                    sink.writeAll(source)
                    sink.flush()
                }
            }
        }
    }
}

/**
 * 压缩指定目录内的文件到压缩包
 * @param outputZipFile 指定压缩包
 * @param preserveFileTime 是否保留原始文件的修改时间
 */
suspend fun zipDirectory(
    sourceDir: File,
    outputZipFile: File,
    preserveFileTime: Boolean = true
) = withContext(Dispatchers.IO) {
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        throw IllegalArgumentException("Source path must be an existing directory")
    }

    ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOut ->
        sourceDir.walkTopDown().filter { it.isFile }.forEach { file ->
            val entryName = file.relativeTo(sourceDir).path.replace("\\", "/")
            val zipEntry = ZipEntry(entryName)
            if (preserveFileTime) {
                zipEntry.time = file.lastModified()
            }
            zipOut.putNextEntry(zipEntry)
            file.inputStream().use { input ->
                input.copyTo(zipOut)
            }
            zipOut.closeEntry()
        }
    }
}

/**
 * 复制目录下的所有内容到目标目录
 */
suspend fun copyDirectoryContents(
    from: File,
    to: File,
    onProgress: ((Float) -> Unit)? = null
) = withContext(Dispatchers.IO) {
    val normalizedFrom = from.absoluteFile.normalize()
    val normalizedTo = to.absoluteFile.normalize()

    val allFiles = mutableListOf<File>()

    normalizedFrom.walkTopDown().forEach { file ->
        ensureActive()
        val targetPath = File(normalizedTo, file.relativeTo(normalizedFrom).path)
        if (file.isDirectory) {
            targetPath.mkdirs()
        } else {
            allFiles.add(file)
        }
    }

    val fileCount = allFiles.size

    if (fileCount == 0) {
        onProgress?.invoke(1.0f)
        return@withContext
    }

    allFiles.forEachIndexed { index, file ->
        ensureActive()
        val targetFile = File(normalizedTo, file.relativeTo(normalizedFrom).path)
        try {
            targetFile.ensureParentDirectory()
            file.copyTo(targetFile, overwrite = true)
            Logger.info(TAG, "copied: ${file.path} -> ${targetFile.path}")
        } catch (e: IOException) {
            Logger.error(TAG, "Failed to copy: ${file.path} -> ${targetFile.path}", e)
        }
        onProgress?.invoke((index + 1).toFloat() / fileCount)
    }
}

/**
 * 以递归的方式，收集一个文件夹内的全部文件
 * @param summitFile 提交获取到的文件
 */
fun collectFiles(
    folder: File,
    summitFile: (File) -> Unit
) {
    if (!folder.exists()) return
    folder.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            collectFiles(file, summitFile)
        } else if (file.isFile) {
            summitFile(file)
        }
    }
}

/**
 * 在[sourceFiles]中找出[targetFiles]中不存在的文件
 */
suspend fun findRedundantFiles(sourceFiles: List<File>, targetFiles: List<File>): List<File> {
    return withContext(Dispatchers.IO) {
        if (targetFiles.isEmpty()) return@withContext sourceFiles
        if (sourceFiles.isEmpty()) return@withContext emptyList()

        val targetPaths = targetFiles.mapTo(
            HashSet(targetFiles.size)
        ) {
            ensureActive()
            it.absolutePath
        }

        sourceFiles.filter { sourceFile ->
            ensureActive()
            sourceFile.absolutePath !in targetPaths
        }
    }
}

/**
 * 定位解压后压缩包的真正根目录
 * @param directory 解压后的初始目录
 * @return 真正的根目录
 */
fun locateRealRoot(directory: File): File {
    require(directory.exists() && directory.isDirectory) { "The directory does not exist or is not a folder" }

    var currentDir = directory
    var shouldContinue = true

    while (shouldContinue) {
        val files = currentDir.listFiles()
        //如果目录为空，直接返回当前目录
        if (files.isNullOrEmpty()) {
            shouldContinue = false
            continue
        }

        //如果目录下有多个项目，说明当前已经是根目录
        if (files.size > 1) {
            shouldContinue = false
            continue
        }

        val file = files[0] //检查当前目录唯一的项目
        //如果唯一的项目不是文件夹，说明当前已经是根目录
        if (!file.isDirectory) {
            shouldContinue = false
            continue
        }

        //如果唯一的项目是文件夹，继续深入
        currentDir = file
    }

    return currentDir
}

/**
 * @return 检查文件是否为 zip jar 压缩包，以是否能够读取为判断标准
 */
fun checkZip(file: File): Boolean {
    return runCatching {
        val zipFile = CompressZipFile.Builder()
            .setFile(file)
            .get()
        zipFile.use { zip ->
            val buffer = ByteArray(8 * 1024)
            val entries = zip.entries
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                zip.getInputStream(entry).use { input ->
                    while (input.read(buffer) != -1) {
                        // 触发 CRC 校验
                    }
                }
            }
        }
        true
    }.getOrDefault(false)
}

/**
 * @return 检查文件是否为 7z 压缩包，以是否能够读取为判断标准
 */
fun check7z(file: File): Boolean {
    return runCatching {
        val sevenZ = SevenZFile.Builder()
            .setFile(file)
            .get()
        sevenZ.use { sevenZ ->
            val buffer = ByteArray(8 * 1024)
            var entry = sevenZ.nextEntry
            while (entry != null) {
                while (sevenZ.read(buffer) > 0) {
                    // 仅流式读取
                }
                entry = sevenZ.nextEntry
            }
        }
        true
    }.getOrDefault(false)
}

/**
 * 检查文件后缀是否符合要求，不符合则抛出异常
 */
fun File.checkExtensionOrThrow(extensions: List<String>) {
    if (extension !in extensions) {
        throw IOException("File extension {$extension} is not supported")
    }
}

/**
 * 检查文件后缀是否符合要求，不符合则抛出异常
 */
fun String.checkExtensionOrThrow(extensions: List<String>) {
    val extension = substringAfterLast(".")
    if (extension !in extensions) {
        throw IOException("File extension {$extension} is not supported")
    }
}