package com.movtery.zalithlauncher.components

import android.content.Context
import android.content.res.AssetManager
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.utils.file.readString
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.yield
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

private const val TAG = "UnpackSingleTask"

abstract class UnpackSingleTask(
    val context: Context,
    val rootDir: File,
    val assetsDirName: String,
    val fileDirName: String,
) : AbstractUnpackTask() {
    private lateinit var am: AssetManager
    private lateinit var versionFile: File
    private lateinit var input: InputStream
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            am = context.assets
            versionFile = File("$rootDir/$fileDirName/version")
            input = am.open("$assetsDirName/version")
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to init asset version. assetsPath=$assetsDirName/version", e)
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun checkState(): InstallableItem.State {
        if (isCheckFailed) return InstallableItem.State.NOT_EXISTS
        if (!versionFile.exists()) return InstallableItem.State.NOT_STARTED
        return runCatching {
            val release1 = input.readString()
            val release2 = FileInputStream(versionFile).use { it.readString() }
            if (release1 != release2) InstallableItem.State.PENDING else InstallableItem.State.FINISHED
        }.getOrElse { InstallableItem.State.NOT_STARTED }
    }

    override suspend fun run() {
        val dir = File(rootDir, fileDirName)
        FileUtils.deleteDirectory(dir)
        copyAssetDirectory(assetsDirName, dir)
        context.copyAssetFile(fileName = "$assetsDirName/version", output = File(dir, "version"), overwrite = true)
        Logger.info(TAG, "$fileDirName: unpacked")
    }

    private suspend fun copyAssetDirectory(assetPath: String, outputDir: File) {
        outputDir.mkdirs()
        val children = am.list(assetPath) ?: emptyArray()
        for (child in children) {
            val childAssetPath = "$assetPath/$child"
            val childOutput = File(outputDir, child)
            if (isAssetDirectory(childAssetPath)) {
                copyAssetDirectory(childAssetPath, childOutput)
            } else {
                context.copyAssetFile(childAssetPath, childOutput, overwrite = true)
                moreProgress(childOutput)
            }
            yield()
        }
    }

    private fun isAssetDirectory(assetPath: String): Boolean {
        return try {
            am.open(assetPath).close()
            false
        } catch (_: IOException) {
            true
        }
    }

    open suspend fun moreProgress(file: File) {}
}
