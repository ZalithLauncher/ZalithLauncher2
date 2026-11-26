package com.movtery.zalithlauncher

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Configuration
import android.os.Process
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.kyant.fishnet.Fishnet
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.context.refreshContext
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.keepalive.TaskKeepAlive
import com.movtery.zalithlauncher.path.IMAGE_LOADER_CLIENT
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.loadAllSettings
import com.movtery.zalithlauncher.ui.activities.showFatalError
import com.movtery.zalithlauncher.ui.activities.showLauncherCrash
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.TLSSecurity
import com.movtery.zalithlauncher.utils.writeCrashFile
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath
import kotlin.properties.Delegates

@HiltAndroidApp
class ZLApplication : Application(), SingletonImageLoader.Factory {
    companion object {
        @JvmStatic
        var DEVICE_ARCHITECTURE by Delegates.notNull<Int>()
    }

    override fun onCreate() {
        GlobalContext = this
        TLSSecurity.install()
        refreshContext(this)
        TaskKeepAlive.initialize(this)

        Thread.setDefaultUncaughtExceptionHandler { _, th ->
            try {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("ZenCrash", Log.getStackTraceString(th)))
            } catch (_: Exception) {}

            TaskSystem.stopAll()
            val throwable = if (th is SplashException) th.cause!! else th
            Logger.error("Startup", "An exception occurred", throwable)

            writeCrashFile(file = PathManager.FILE_CRASH_REPORT, throwable = throwable) { t ->
                Logger.error("AppCrash", "Failed saving crash report", t)
            }

            showLauncherCrash(this@ZLApplication, throwable, th !is SplashException)
            Process.killProcess(Process.myPid())
        }

        super.onCreate()
        runCatching {
            Fishnet.init(this, PathManager.DIR_NATIVE_LOGS.absolutePath)
            MMKV.initialize(this)
            loadAllSettings(this)
            Logger.initialize(this)
            initializeData()
            PathManager.DIR_FILES_PRIVATE = getDir("files", MODE_PRIVATE)
            DEVICE_ARCHITECTURE = Architecture.getDeviceArchitecture()
            if (Architecture.isx86Device() && Architecture.is32BitsDevice) {
                val originalJNIDirectory = applicationInfo.nativeLibraryDir
                applicationInfo.nativeLibraryDir = originalJNIDirectory.take(originalJNIDirectory.lastIndexOf("/")) + "/x86"
            }
        }.onFailure { launchTh ->
            writeCrashFile(file = PathManager.FILE_CRASH_REPORT, throwable = launchTh) {}
            showFatalError(this, launchTh)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshContext(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(20L * 1024 * 1024)
                    .weakReferencesEnabled(true)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .maxSizeBytes(512L * 1024 * 1024)
                    .directory(PathManager.DIR_IMAGE_CACHE.toOkioPath())
                    .build()
            }
            .components {
                add(KtorNetworkFetcherFactory(IMAGE_LOADER_CLIENT))
                add(GifDecoder.Factory())
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }

    private fun initializeData() {
        AccountsManager.initialize(this)
        GamePathManager.initialize(this)
    }
}
