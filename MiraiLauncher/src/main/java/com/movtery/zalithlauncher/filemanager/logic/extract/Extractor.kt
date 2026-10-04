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

package com.movtery.zalithlauncher.filemanager.logic.extract

import com.movtery.zalithlauncher.filemanager.logic.entry.ArchiveType
import com.movtery.zalithlauncher.filemanager.logic.ops.FilePermissions
import com.movtery.zalithlauncher.filemanager.os.FmLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.apache.commons.compress.PasswordRequiredException
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths

private const val TAG = "FmExtract"
private const val BUFFER_SIZE = 64 * 1024

/** 文件存在，但不是可识别的压缩包格式（或压缩包已损坏无法解析） */
class NotArchiveException(message: String) : Exception(message)

object Extractor {
    /**
     * 将压缩包全部条目解压到目标目录
     * @param archive 压缩包路径
     * @param outputDir 目标目录
     * @param options 解压选项
     * @param onProgress 进度回调（completed, total, currentName）
     * @param checkCancel 取消检查；返回 true 表示取消
     * @return 解压结果汇总
     */
    suspend fun extract(
        archive: Path,
        outputDir: Path,
        options: ExtractOptions,
        onProgress: (completed: Int, total: Int, currentName: String?) -> Unit,
        checkCancel: () -> Boolean,
        onBytes: (bytesDone: Long, bytesTotal: Long) -> Unit = { _, _ -> }
    ): ExtractSummary {
        // 按内容探测候选格式并依次尝试，失败换下一种
        for (type in detectTypes(archive)) {
            try {
                return extractWithType(
                    type = type,
                    archive = archive,
                    outputDir = outputDir,
                    options = options,
                    onProgress = onProgress,
                    checkCancel = checkCancel,
                    onBytes = onBytes
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e is ArchivePasswordException) throw e
                if (isPasswordError(e)) throw passwordException(options, e)
                FmLog.warn(TAG, "Extract failed as $type, trying next format", e)
            }
        }
        // 所有候选格式均失败，文件存在时统一提示“不是有效的压缩包”，
        throw if (Files.exists(archive, LinkOption.NOFOLLOW_LINKS)) {
            NotArchiveException(archive.toString())
        } else {
            NoSuchFileException(archive.toString())
        }
    }

    private suspend fun extractWithType(
        type: ArchiveType,
        archive: Path,
        outputDir: Path,
        options: ExtractOptions,
        onProgress: (Int, Int, String?) -> Unit,
        checkCancel: () -> Boolean,
        onBytes: (Long, Long) -> Unit
    ): ExtractSummary {
        val total = when (type) {
            ArchiveType.ZIP -> countZip(archive, options.password)
            ArchiveType.SEVEN_Z -> countSevenZ(archive, options.password)
            ArchiveType.TAR -> countTar(archive)
        }
        var completed = 0
        var bytesDone = 0L
        val bytesTotal = runCatching { Files.size(archive) }.getOrDefault(0L)
        fun report(name: String?) = onProgress(completed, total, name)
        fun countBytes(n: Int) {
            if (n > 0) {
                bytesDone += n
                onBytes(bytesDone, bytesTotal)
            }
        }

        when (type) {
            ArchiveType.ZIP -> {
                extractZip(
                    archive, outputDir, options, ::report, checkCancel,
                    setCompleted = { completed = it }, onBytesN = ::countBytes
                )
            }
            ArchiveType.SEVEN_Z -> {
                extractSevenZ(
                    archive, outputDir, options, ::report, checkCancel,
                    setCompleted = { completed = it }, onBytesN = ::countBytes
                )
            }
            ArchiveType.TAR -> {
                extractTar(
                    archive, outputDir, ::report, checkCancel,
                    setCompleted = { completed = it }, onBytesN = ::countBytes
                )
            }
        }
        return ExtractSummary(total)
    }

    /**
     * 依据文件内容魔数探测候选格式
     * 无法识别时返回 ZIP / 7Z / TAR 的兜底顺序
     */
    private suspend fun detectTypes(archive: Path): List<ArchiveType> {
        sniffType(archive)?.let {
            return listOf(it)
        }
        return listOf(ArchiveType.ZIP, ArchiveType.SEVEN_Z, ArchiveType.TAR)
    }

    private suspend fun sniffType(archive: Path): ArchiveType? {
        val header = runCatching {
            withContext(Dispatchers.IO) {
                Files.newInputStream(archive).use { input ->
                    val buf = ByteArray(8)
                    var read = 0
                    while (read < buf.size) {
                        val n = input.read(buf, read, buf.size - read)
                        if (n < 0) break
                        read += n
                    }
                    buf.copyOf(read)
                }
            }
        }.getOrNull() ?: return null

        fun byte(i: Int): Byte = if (i < header.size) {
            header[i]
        } else {
            (-1).toByte()
        }

        return when {
            byte(0) == 'P'.code.toByte() && byte(1) == 'K'.code.toByte() &&
                    ((byte(2) == 0x03.toByte() && byte(3) == 0x04.toByte()) ||
                            (byte(2) == 0x05.toByte() && byte(3) == 0x06.toByte()) ||
                            (byte(2) == 0x07.toByte() && byte(3) == 0x08.toByte())) -> {
                ArchiveType.ZIP
            }

            byte(0) == '7'.code.toByte() && byte(1) == 'z'.code.toByte() &&
                    byte(2) == 0xBC.toByte() && byte(3) == 0xAF.toByte() &&
                    byte(4) == 0x27.toByte() && byte(5) == 0x1C.toByte() -> {
                ArchiveType.SEVEN_Z
            }

            byte(0) == 0x1F.toByte() && byte(1) == 0x8B.toByte() -> {
                ArchiveType.TAR     // gzip（tar.gz）
            }

            byte(0) == 'B'.code.toByte() && byte(1) == 'Z'.code.toByte() &&
                    byte(2) == 'h'.code.toByte() -> {
                ArchiveType.TAR     // bzip2（tar.bz2）
            }

            byte(0) == 0xFD.toByte() && byte(1) == '7'.code.toByte() && byte(2) == 'z'.code.toByte() &&
                    byte(3) == 'X'.code.toByte() && byte(4) == 'Z'.code.toByte() &&
                    byte(5) == 0x00.toByte() -> {
                ArchiveType.TAR     // xz（tar.xz）
            }

            else -> null
        }
    }

    /**
     * 判断文件头部是否具有 ZIP 签名
     */
    private fun isZipSignature(archive: Path): Boolean = runCatching {
        Files.newInputStream(archive).use { input ->
            val buf = ByteArray(4)
            var read = 0
            while (read < buf.size) {
                val n = input.read(buf, read, buf.size - read)
                if (n < 0) break
                read += n
            }
            read >= 4 && buf[0] == 'P'.code.toByte() && buf[1] == 'K'.code.toByte() &&
                (buf[2] == 0x03.toByte() || buf[2] == 0x05.toByte() || buf[2] == 0x07.toByte()) &&
                buf[3] == 0x04.toByte()
        }
    }.getOrDefault(false)

    private fun countZip(archive: Path, password: String?): Int {
        return runCatching {
            val zip = if (password != null) {
                ZipFile(archive.toFile(), password.toCharArray())
            } else {
                ZipFile(archive.toFile())
            }
            zip.use { it.fileHeaders.count { !it.isDirectory } }
        }.getOrDefault(0)
    }

    private suspend fun extractZip(
        archive: Path,
        outputDir: Path,
        options: ExtractOptions,
        report: (String?) -> Unit,
        checkCancel: () -> Boolean,
        setCompleted: (Int) -> Unit,
        onBytesN: (Int) -> Unit
    ) {
        var completed = 0
        val zip = try {
            if (options.password != null) {
                ZipFile(archive.toFile(), options.password.toCharArray())
            } else {
                ZipFile(archive.toFile())
            }
        } catch (e: Exception) {
            if (isPasswordError(e)) throw passwordException(options, e)
            throw e
        }
        zip.use { z ->
            // zip4j 对无 ZIP 签名的内容（如空文件、纯文本）会宽容地视为空压缩包，
            // 此处校验签名：不是 zip 时让位给后续格式尝试，最终提示“不是有效的压缩包”
            if (z.fileHeaders.isEmpty() && !isZipSignature(archive)) {
                throw ZipException("Zip headers not found. Probably not a zip file")
            }
            withContext(Dispatchers.IO) {
                for (header in z.fileHeaders) {
                    checkCancel()
                    val name = header.fileName
                    val relative = safeRelative(name) ?: continue
                    val target = outputDir.resolve(relative)
                    if (header.isDirectory) {
                        Files.createDirectories(target)
                        FilePermissions.apply(target)
                        continue
                    }
                    Files.createDirectories(target.parent)
                    val input = try {
                        z.getInputStream(header)
                    } catch (e: Exception) {
                        // 密码缺失 / 错误：抛出异常而非静默跳过
                        if (isPasswordError(e)) throw passwordException(options, e)
                        FmLog.warn(TAG, "Skip unreadable zip entry: $name", e)
                        continue
                    }
                    input.use { ins ->
                        BufferedOutputStream(Files.newOutputStream(target)).use { out ->
                            copyBuffer(ins, out, checkCancel, onBytesN)
                        }
                    }
                    FilePermissions.apply(target)
                    completed++
                    setCompleted(completed)
                    report(name)
                }
            }
        }
    }


    private suspend fun countSevenZ(archive: Path, password: String?): Int {
        return runCatching {
            val sevenZ = SevenZFile.builder()
                .setFile(archive.toFile())
                .apply {
                    if (password != null) {
                        setPassword(password.toCharArray())
                    }
                }.get()
            sevenZ.use { z ->
                var count = 0
                while (true) {
                    val entry = withContext(Dispatchers.IO) {
                        z.getNextEntry()
                    } ?: break
                    if (!entry.isDirectory) count++
                }
                count
            }
        }.getOrDefault(0)
    }

    private suspend fun extractSevenZ(
        archive: Path,
        outputDir: Path,
        options: ExtractOptions,
        report: (String?) -> Unit,
        checkCancel: () -> Boolean,
        setCompleted: (Int) -> Unit,
        onBytesN: (Int) -> Unit
    ) {
        var completed = 0
        val sevenZ = try {
            SevenZFile.builder()
                .setFile(archive.toFile())
                .apply {
                    if (options.password != null) {
                        setPassword(options.password.toCharArray())
                    }
                }.get()
        } catch (e: Exception) {
            if (isPasswordError(e)) throw passwordException(options, e)
            throw e
        }
        sevenZ.use { sz ->
            withContext(Dispatchers.IO) {
                while (true) {
                    checkCancel()
                    val entry = try {
                        sz.getNextEntry() ?: break
                    } catch (e: Exception) {
                        // 密码缺失 / 错误：抛出异常而非静默跳过
                        if (isPasswordError(e)) throw passwordException(options, e)
                        throw e
                    }
                    try {
                        extractSevenZEntry(sz, entry, outputDir, checkCancel, onBytesN)
                    } catch (e: Exception) {
                        if (isPasswordError(e)) throw passwordException(options, e)
                        throw e
                    }
                    if (!entry.isDirectory) {
                        completed++
                        setCompleted(completed)
                        report(entry.name)
                    }
                }
            }
        }
    }

    private suspend fun extractSevenZEntry(
        sevenZ: SevenZFile,
        entry: SevenZArchiveEntry,
        outputDir: Path,
        checkCancel: () -> Boolean,
        onBytesN: (Int) -> Unit
    ) {
        val relative = safeRelative(entry.name) ?: return
        val target = outputDir.resolve(relative)

        if (entry.isDirectory) {
            withContext(Dispatchers.IO) {
                Files.createDirectories(target)
            }
            FilePermissions.apply(target)
            return
        }

        withContext(Dispatchers.IO) {
            Files.createDirectories(target.parent)
            val input = sevenZ.getInputStream(entry)
            input.use { ins ->
                BufferedOutputStream(Files.newOutputStream(target)).use { out ->
                    copyBuffer(ins, out, checkCancel, onBytesN)
                }
            }
        }
        FilePermissions.apply(target)
    }


    private suspend fun countTar(archive: Path): Int = withContext(Dispatchers.IO) {
        runCatching {
            openTarInput(archive).use { input ->
                TarArchiveInputStream(input).use { tar ->
                    var count = 0
                    while (true) {
                        val entry = tar.nextEntry ?: break
                        if (!entry.isDirectory) count++
                    }
                    count
                }
            }
        }.getOrDefault(0)
    }

    private suspend fun extractTar(
        archive: Path,
        outputDir: Path,
        report: (String?) -> Unit,
        checkCancel: () -> Boolean,
        setCompleted: (Int) -> Unit,
        onBytesN: (Int) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            var completed = 0

            openTarInput(archive).use { input ->
                TarArchiveInputStream(input).use { tar ->
                    while (true) {
                        checkCancel()
                        val entry = tar.nextEntry ?: break
                        val relative = safeRelative(entry.name) ?: continue
                        val target = outputDir.resolve(relative)
                        if (entry.isDirectory) {
                            Files.createDirectories(target)
                            FilePermissions.apply(target)
                            continue
                        }
                        Files.createDirectories(target.parent)
                        BufferedOutputStream(Files.newOutputStream(target)).use { out ->
                            copyBuffer(tar, out, checkCancel, onBytesN)
                        }
                        FilePermissions.apply(target)
                        completed++
                        setCompleted(completed)
                        report(entry.name)
                    }
                }
            }
        }
    }

    /** 打开 tar 输入流：自动解包 gzip / bzip2 / xz 压缩，原始 tar 直接透传。 */
    private fun openTarInput(archive: Path): InputStream {
        val buffered = BufferedInputStream(Files.newInputStream(archive))
        return try {
            CompressorStreamFactory().createCompressorInputStream(buffered)
        } catch (e: Exception) {
            runCatching { buffered.close() }
            throw e
        }
    }


    /**
     * 读取压缩包顶层条目名。
     */
    suspend fun topLevelNames(archive: Path): List<String> = withContext(Dispatchers.IO) {
        for (type in detectTypes(archive)) {
            collectTopLevelNames(type, archive)?.let { return@withContext it }
        }
        emptyList()
    }

    /** 按指定格式读取顶层条目名；格式不匹配时返回 null。 */
    private fun collectTopLevelNames(type: ArchiveType, archive: Path): List<String>? {
        val names = LinkedHashSet<String>()
        val ok = when (type) {
            ArchiveType.ZIP -> runCatching {
                ZipFile(archive.toFile()).use { zip ->
                    for (h in zip.fileHeaders) {
                        topLevelOf(h.fileName)?.let(names::add)
                    }
                }
            }.isSuccess
            ArchiveType.SEVEN_Z -> runCatching {
                SevenZFile(archive.toFile()).use { z ->
                    while (true) {
                        val e = z.getNextEntry() ?: break
                        topLevelOf(e.name)?.let(names::add)
                    }
                }
            }.isSuccess
            ArchiveType.TAR -> runCatching {
                openTarInput(archive).use { input ->
                    TarArchiveInputStream(input).use { tar ->
                        while (true) {
                            val e = tar.nextEntry ?: break
                            topLevelOf(e.name)?.let(names::add)
                        }
                    }
                }
            }.isSuccess
        }
        return if (ok) names.toList() else null
    }

    private fun topLevelOf(entryName: String): String? {
        val cleaned = entryName.replace('\\', '/').trim('/')
        if (cleaned.isBlank()) return null
        val idx = cleaned.indexOf('/')
        val first = if (idx < 0) cleaned else cleaned.substring(0, idx)
        return first.takeIf { it.isNotBlank() && it != "." && it != ".." }
    }

    private fun safeRelative(entryName: String): Path? {
        val normalizedName = entryName.replace('\\', '/')
        if (normalizedName.isBlank() || normalizedName.startsWith("/") || normalizedName.startsWith("\\")) return null
        val path = runCatching { Paths.get(normalizedName) }.getOrNull() ?: return null
        if (path.isAbsolute) return null
        val normalized = path.normalize()
        if (normalized.startsWith("..")) return null
        if (normalized.nameCount == 0) return null
        return normalized
    }

    private fun copyBuffer(
        input: InputStream,
        output: OutputStream,
        checkCancel: () -> Boolean,
        onBytesN: (Int) -> Unit
    ) {
        val buf = ByteArray(BUFFER_SIZE)
        while (true) {
            checkCancel()
            val n = input.read(buf)
            if (n < 0) break
            output.write(buf, 0, n)
            onBytesN(n)
        }
    }

    private fun passwordException(options: ExtractOptions, cause: Throwable): ArchivePasswordException {
        val type = if (options.password != null) {
            ArchivePasswordException.Type.WRONG
        } else {
            ArchivePasswordException.Type.REQUIRED
        }
        return ArchivePasswordException(type, cause = cause)
    }

    private fun isPasswordError(e: Throwable): Boolean {
        if (e is ArchivePasswordException) return true
        if (e is PasswordRequiredException) return true
        if (e is ZipException && e.type == ZipException.Type.WRONG_PASSWORD) return true
        val message = e.message ?: ""
        return message.contains("password", ignoreCase = true)
    }
}