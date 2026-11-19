package dev.horizon.android

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Keep
interface HorizonInstallCallback {
    fun onProgress(stage: Int, currentBytes: Long, totalBytes: Long)
    fun onComplete(success: Boolean, errorMessage: String?)
}

@Keep
@Suppress("KotlinJniMissingFunction")
object HorizonInstaller {
    private const val TAG = "VoxiumRust"
    private var libraryLoaded = false

    init {
        try {
            System.loadLibrary("horizon_android")
            libraryLoaded = true
        } catch (error: Throwable) {
            Log.e(TAG, "Could not load Starling native runtime", error)
        }
    }

    @JvmStatic private external fun nativeIsReady(homePath: String): Boolean
    @JvmStatic private external fun nativeInstall(homePath: String, callback: HorizonInstallCallback): Boolean

    fun getHorizonHomeDir(context: Context): File =
        File(context.filesDir, ".local/horizon-client/26.3")

    @Throws(IOException::class)
    private fun copyEngineData(context: Context) {
        val target = File(context.filesDir, "engine-data.zip")
        if (target.isFile && target.length() > 0L) return
        val partial = File(context.filesDir, "engine-data.partial")
        try {
            context.assets.open("engine-data.zip").use { input ->
                FileOutputStream(partial).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                    }
                    output.fd.sync()
                }
            }
            if (!partial.renameTo(target)) throw IOException("Unable to install Starling engine data")
        } catch (error: Throwable) {
            partial.delete()
            throw error
        }
    }

    fun isReady(context: Context): Boolean {
        if (!libraryLoaded) return false
        return try {
            copyEngineData(context)
            nativeIsReady(context.filesDir.absolutePath)
        } catch (error: Throwable) {
            Log.e(TAG, "Starling readiness check failed", error)
            false
        }
    }

    fun install(context: Context, callback: HorizonInstallCallback) {
        if (!libraryLoaded) {
            callback.onComplete(false, "Starling's native library is unavailable in this build.")
            return
        }
        try {
            copyEngineData(context)
            getHorizonHomeDir(context).mkdirs()
            if (!nativeInstall(context.filesDir.absolutePath, callback)) {
                callback.onComplete(false, "Starling could not start its first-time installation.")
            }
        } catch (error: Throwable) {
            Log.e(TAG, "Starling installation failed", error)
            callback.onComplete(false, error.message ?: "Unknown installation error")
        }
    }
}
