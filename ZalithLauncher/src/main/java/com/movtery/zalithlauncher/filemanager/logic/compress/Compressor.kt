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

package com.movtery.zalithlauncher.filemanager.logic.compress

import com.movtery.zalithlauncher.filemanager.logic.ops.FilePermissions
import com.movtery.zalithlauncher.filemanager.os.FmLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZMethod
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

private const val TAG = "FmCompress"
private const val BUFFER_SIZE = 64 * 1024

object Compressor {
    /**
     * 执行压缩，将一组条目打包为 ZIP / 7Z / TAR 压缩包
     * @param sources 待压缩条目
     * @param output 输出压缩包路径（已含正确后缀）
     * @param options 压缩参数
     * @param onProgress 进度回调（completed, total, currentName）
     * @param checkCancel 取消检查；返回 true 表示取消
     * @return 输出路径与总条目数
     */
    suspend fun compress(
        sources: List<Path>,
        output: Path,
        options: CompressOptions,
        onProgress: (completed: Int, total: Int, currentName: String?) -> Unit,
        checkCancel: () -> Boolean,
        onBytes: (bytesDone: Long, bytesTotal: Long) -> Unit = { _, _ -> }
    ): CompressSummary {
        require(sources.isNotEmpty()) { "No sources to compress" }

        val total = countFiles(sources)
        val bytesTotal = totalBytes(sources)
        val context = Context(options, total, onProgress, checkCancel, onBytes, bytesTotal)

        when (options.format) {
            CompressFormat.ZIP -> zip4j(sources, output, context)
            CompressFormat.SEVEN_Z -> sevenZ(sources, output, context)
            CompressFormat.TAR -> tar(sources, output, context)
        }
        FilePermissions.apply(output)
        return CompressSummary(output, total)
    }

    private suspend fun zip4j(
        sources: List<Path>,
        output: Path,
        ctx: Context
    ) {
        val password = ctx.options.password
        val zip = if (password != null) {
            ZipFile(output.toFile(), password.toCharArray())
        } else {
            ZipFile(output.toFile())
        }
        zip.use {
            for (source in sources) {
                ctx.check()
                val name = source.fileName?.toString() ?: "entry"
                addZipPath(it, source, name, ctx)
            }
        }
    }

    private suspend fun addZipPath(
        zip: ZipFile,
        source: Path,
        entryRoot: String,
        ctx: Context
    ) {
        val attrs = withContext(Dispatchers.IO) {
            Files.readAttributes(
                source,
                BasicFileAttributes::class.java,
                LinkOption.NOFOLLOW_LINKS
            )
        }
        if (attrs.isSymbolicLink) {
            FmLog.info(TAG, "Skip symlink during compress: $source")
            return
        }
        withContext(Dispatchers.IO) {
            if (attrs.isDirectory) {
                zip.addStream(
                    ByteArrayInputStream(
                        ByteArray(0)),
                    buildZipParams(ctx, "$entryRoot/")
                )
                val children = sortedChildren(source)
                for (child in children) {
                    ctx.check()
                    addZipPath(zip, child, "$entryRoot/${child.fileName}", ctx)
                }
            } else {
                Files.newInputStream(source).use { input ->
                    zip.addStream(
                        input,
                        buildZipParams(ctx, entryRoot)
                    )
                }
                ctx.bytes(
                    runCatching {
                        attrs.size()
                    }.getOrDefault(0L).toInt()
                )
                ctx.tick(entryRoot)
            }
        }
    }

    private fun buildZipParams(
        ctx: Context,
        fileNameInZip: String
    ): ZipParameters {
        val password = ctx.options.password
        return ZipParameters().apply {
            this.fileNameInZip = fileNameInZip
            compressionMethod = if (ctx.options.method == CompressMethod.STORE) {
                CompressionMethod.STORE
            } else {
                CompressionMethod.DEFLATE
            }

            val defaultLevel = if (ctx.options.method == CompressMethod.STORE) 0 else 5
            compressionLevel = toZipLevel(ctx.options.level ?: defaultLevel)
            isEncryptFiles = password != null

            if (password != null) {
                encryptionMethod = EncryptionMethod.AES
                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
            }
        }
    }

    private suspend fun sevenZ(
        sources: List<Path>,
        output: Path,
        ctx: Context
    ) {
        if (ctx.options.password != null) {
            // 带密码走自定义 solid 写入器，使读取端只需一次密钥派生
            SolidSevenZWriter.write(
                sources = sources,
                output = output,
                options = ctx.options,
                total = ctx.total,
                bytesTotal = ctx.progressBytesTotal,
                onProgress = ctx::reportProgress,
                checkCancel = ctx::cancelRequested,
                onBytes = ctx::reportBytes
            )
            return
        }
        SevenZOutputFile(output.toFile()).use { out ->
            val method = if (ctx.options.method == CompressMethod.BZIP2) {
                SevenZMethod.BZIP2
            } else {
                SevenZMethod.LZMA2
            }
            out.setContentMethods(
                listOf(SevenZMethodConfiguration(method, dictionarySize(ctx.options.level)))
            )
            for (source in sources) {
                ctx.check()
                val name = source.fileName?.toString() ?: "entry"
                addSevenZPath(
                    sevenZ = out,
                    source = source,
                    entryRoot = name,
                    ctx = ctx
                )
            }
            out.finish()
        }
    }

    private suspend fun addSevenZPath(
        sevenZ: SevenZOutputFile,
        source: Path,
        entryRoot: String,
        ctx: Context
    ) {
        val attrs = withContext(Dispatchers.IO) {
            Files.readAttributes(
                source,
                BasicFileAttributes::class.java,
                LinkOption.NOFOLLOW_LINKS
            )
        }
        if (attrs.isSymbolicLink) {
            FmLog.info(TAG, "Skip symlink during compress: $source")
            return
        }
        withContext(Dispatchers.IO) {
            if (attrs.isDirectory) {
                val dirEntry = SevenZArchiveEntry()
                dirEntry.name = "$entryRoot/"
                dirEntry.isDirectory = true
                sevenZ.putArchiveEntry(dirEntry)
                sevenZ.closeArchiveEntry()
                for (child in sortedChildren(source)) {
                    ctx.check()
                    addSevenZPath(
                        sevenZ = sevenZ,
                        source = child,
                        entryRoot = "$entryRoot/${child.fileName}",
                        ctx = ctx
                    )
                }
            } else {
                val entry = sevenZ.createArchiveEntry(source.toFile(), entryRoot)
                sevenZ.putArchiveEntry(entry)
                Files.newInputStream(source).use { input ->
                    val buf = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        sevenZ.write(buf, 0, n)
                        ctx.bytes(n)
                    }
                }
                sevenZ.closeArchiveEntry()
                ctx.tick(entryRoot)
            }
        }
    }

    private suspend fun tar(
        sources: List<Path>,
        output: Path,
        ctx: Context
    ) {
        withContext(Dispatchers.IO) {
            Files.newOutputStream(output).use { fos ->
                TarArchiveOutputStream(BufferedOutputStream(fos)).use { tarOut ->
                    // TAR 长文件名处理格式：GNU / POSIX
                    val gnu = ctx.options.method == CompressMethod.TAR_GNU
                    tarOut.setLongFileMode(
                        if (gnu) TarArchiveOutputStream.LONGFILE_GNU
                        else TarArchiveOutputStream.LONGFILE_POSIX
                    )
                    if (!gnu) {
                        tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX)
                    }
                    for (source in sources) {
                        ctx.check()
                        val name = source.fileName?.toString() ?: "entry"
                        addTarPath(tarOut, source, name, ctx)
                    }
                    tarOut.finish()
                }
            }
        }
    }

    private suspend fun addTarPath(
        tarOut: TarArchiveOutputStream,
        source: Path,
        entryRoot: String,
        ctx: Context
    ) {
        val attrs = withContext(Dispatchers.IO) {
            Files.readAttributes(
                source,
                BasicFileAttributes::class.java,
                LinkOption.NOFOLLOW_LINKS
            )
        }
        if (attrs.isSymbolicLink) {
            FmLog.info(TAG, "Skip symlink during compress: $source")
            return
        }

        withContext(Dispatchers.IO) {
            if (attrs.isDirectory) {
                tarOut.putArchiveEntry(TarArchiveEntry("$entryRoot/"))
                tarOut.closeArchiveEntry()
                for (child in sortedChildren(source)) {
                    ctx.check()
                    addTarPath(tarOut, child, "$entryRoot/${child.fileName}", ctx)
                }
            } else {
                val entry = TarArchiveEntry(entryRoot)
                entry.size = attrs.size()
                tarOut.putArchiveEntry(entry)
                Files.newInputStream(source).use { input ->
                    val buf = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        tarOut.write(buf, 0, n)
                        ctx.bytes(n)
                    }
                }
                tarOut.closeArchiveEntry()
                ctx.tick(entryRoot)
            }
        }
    }

    private suspend fun sortedChildren(dir: Path): List<Path> = withContext(Dispatchers.IO) {
        runCatching {
            Files.newDirectoryStream(dir).use { stream ->
                stream.toList()
            }
        }.getOrElse {
            emptyList()
        }.sortedBy {
            it.fileName.toString()
        }
    }

    private fun countFiles(sources: List<Path>): Int {
        var count = 0
        for (source in sources) {
            val attrs = runCatching {
                Files.readAttributes(
                    source,
                    BasicFileAttributes::class.java,
                    LinkOption.NOFOLLOW_LINKS
                )
            }.getOrNull() ?: continue

            if (attrs.isSymbolicLink) continue
            if (attrs.isDirectory) {
                Files.walkFileTree(source, emptySet(), Int.MAX_VALUE, object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        if (attrs.isSymbolicLink) return FileVisitResult.CONTINUE
                        count++
                        return FileVisitResult.CONTINUE
                    }
                })
            } else {
                count++
            }
        }
        return count
    }

    private fun dictionarySize(level: Int?): Int {
        val l = level?.coerceIn(1, 9) ?: 5
        return 1 shl (18 + l.coerceAtMost(7)) // 256KB ~ 32MB
    }

    private fun toZipLevel(level: Int): CompressionLevel {
        val enum = CompressionLevel.entries.getOrNull(level.coerceIn(0, 9))
        return enum ?: CompressionLevel.NORMAL
    }

    private fun totalBytes(sources: List<Path>): Long {
        var total = 0L
        for (source in sources) {
            val attrs = runCatching {
                Files.readAttributes(source, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
            }.getOrNull() ?: continue
            if (attrs.isSymbolicLink) continue
            if (attrs.isDirectory) {
                Files.walkFileTree(source, emptySet(), Int.MAX_VALUE, object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        if (attrs.isRegularFile) total += attrs.size()
                        return FileVisitResult.CONTINUE
                    }
                })
            } else {
                total += attrs.size()
            }
        }
        return total
    }

    private class Context(
        val options: CompressOptions,
        val total: Int,
        private val onProgress: (Int, Int, String?) -> Unit,
        private val checkCancel: () -> Boolean,
        private val onBytes: (Long, Long) -> Unit,
        private val bytesTotal: Long
    ) {
        private var completed = 0
        private var bytesDone = 0L

        fun check() {
            if (checkCancel()) throw CancellationException()
        }

        fun tick(name: String?) {
            completed++
            onProgress(completed, total, name)
        }

        fun bytes(n: Int) {
            if (n > 0) {
                bytesDone += n
                onBytes(bytesDone, bytesTotal)
            }
        }

        fun reportProgress(c: Int, t: Int, n: String?) = onProgress(c, t, n)
        fun cancelRequested(): Boolean = checkCancel()
        fun reportBytes(done: Long, total: Long) = onBytes(done, total)
        val progressBytesTotal: Long get() = bytesTotal
    }
}