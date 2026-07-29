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

package com.movtery.zalithlauncher.game.plugin.vpl

import android.content.Context
import android.os.Build
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.ffmpeg.FFmpegPluginManager
import com.movtery.zalithlauncher.game.plugin.natives.NativePlugin
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPlugin
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import com.vpl.verifiedpluginload.api.VerifiedPluginLoad
import com.vpl.verifiedpluginload.api.VerifiedPluginLoadRegistry
import com.vpl.verifiedpluginload.model.PluginLoadAuthorization
import com.vpl.verifiedpluginload.model.PluginTrustStatus
import com.vpl.verifiedpluginload.model.TrustSource
import java.io.File
import java.io.IOException

object PluginNativeLoadGuard {
    private const val TAG = "VerifiedPluginLoad"

    private val ALLOWED_ABIS = setOf("arm64-v8a", "armeabi-v7a", "x86_64")

    /*
     * These values are consumed by native code as library paths, loader configuration, or
     * already-loaded handles.  Letting the user-controlled environment editor replace one
     * would bypass the APK/source-dir authorization immediately above the actual dlopen.
     */
    private val PROTECTED_NATIVE_ENVIRONMENT_VARIABLES = setOf(
        "DLOPEN",
        "DRIVER_PATH",
        "FCL_ENVIRON",
        "FFMPEG_PATH",
        "FCL_NATIVEDIR",
        "GALLIUM_DRIVER",
        "LIBGL_DRIVERS_PATH",
        "LIB_MESA_NAME",
        "MESA_LIBRARY",
        "MESA_LOADER_DRIVER_OVERRIDE",
        "MOD_ANDROID_RUNTIME",
        "POJAVEXEC_EGL",
        "POJAV_ENVIRON",
        "POJAV_NATIVEDIR",
        "RENDERER_HANDLE",
        "TMPDIR",
        "VK_ADD_DRIVER_FILES",
        "VK_ADD_LAYER_PATH",
        "VK_DRIVER_FILES",
        "VK_ICD_FILENAMES",
        "VK_LAYER_PATH",
        "VULKAN_PTR"
    )

    /*
     * Protected variables a plugin may legitimately point at its own verified library directory.
     * The Vulkan loader and the Mesa loader both dlopen whatever these name, so a value outside the
     * declaring APK would let any process that can write that path supply the loaded code.
     */
    private val NATIVE_PATH_ENVIRONMENT_VARIABLES = setOf(
        "DLOPEN",
        "DRIVER_PATH",
        "FFMPEG_PATH",
        "LIBGL_DRIVERS_PATH",
        "LIB_MESA_NAME",
        "MESA_LIBRARY",
        "POJAVEXEC_EGL",
        "VK_ADD_DRIVER_FILES",
        "VK_ADD_LAYER_PATH",
        "VK_DRIVER_FILES",
        "VK_ICD_FILENAMES",
        "VK_LAYER_PATH"
    )

    /*
     * Protected variables the launcher owns outright.  A plugin has no legitimate reason to move the
     * temporary directory, retarget the launcher's own native directory, or forge a handle that
     * native code produces at runtime.  jni/environ/environ.c adopts POJAV_ENVIRON in a constructor
     * as a raw pointer via strtoul, and jni/egl_bridge.c does the same with VULKAN_PTR, so a
     * plugin-supplied value there is dereferenced directly.
     */
    private val LAUNCHER_OWNED_ENVIRONMENT_VARIABLES = setOf(
        "FCL_ENVIRON",
        "FCL_NATIVEDIR",
        "MOD_ANDROID_RUNTIME",
        "POJAV_ENVIRON",
        "POJAV_NATIVEDIR",
        "RENDERER_HANDLE",
        "TMPDIR",
        "VULKAN_PTR"
    )

    /*
     * Protected variables that name a driver rather than a path.  Mesa's loader builds the module it
     * dlopens by concatenating the search directory with this name and does not reject separators, so
     * a name carrying "../" walks straight back out of the directory constrained just above.
     */
    private val DRIVER_NAME_ENVIRONMENT_VARIABLES = setOf(
        "GALLIUM_DRIVER",
        "MESA_LOADER_DRIVER_OVERRIDE"
    )

    private val DRIVER_NAME = Regex("[A-Za-z0-9_+-]{1,64}")

    /*
     * Read-only partitions a passthrough renderer legitimately loads system drivers from.  They are
     * already on the library path built by Launcher, and nothing short of root can write them, so
     * accepting them costs nothing while keeping shared and app-writable storage out of reach.
     */
    private val READ_ONLY_SYSTEM_LIBRARY_ROOTS = setOf(
        "/apex",
        "/odm",
        "/system",
        "/system_ext",
        "/vendor"
    )

    /**
     * Native loading configuration belongs to the verified launch plan, not the global custom
     * environment setting.  This is public so Launcher can apply the same policy at the last
     * point custom environment variables are merged.
     */
    fun isProtectedNativeEnvironmentVariable(key: String?): Boolean {
        return key != null && (key.startsWith("LD_") || key in PROTECTED_NATIVE_ENVIRONMENT_VARIABLES)
    }

    /** A stored per-certificate trust is only active while the launcher setting permits it. */
    fun isExplicitKeyTrustAllowed(trustSource: TrustSource?, allowUntrustedPlugins: Boolean): Boolean {
        return trustSource != TrustSource.KEY || allowUntrustedPlugins
    }

    /**
     * The one parse of a plugin-declared environment entry.  Every consumer must split identically:
     * a guard that authorizes "a.so=b" while the loader dlopens "a" is not a guard at all.  Returns
     * null for an entry no consumer may act on.
     */
    fun parsePluginEnvironmentEntry(entry: String?): Pair<String, String>? {
        if (entry == null) return null
        val separator = entry.indexOf('=')
        if (separator <= 0 || separator == entry.lastIndex) return null
        return entry.substring(0, separator) to entry.substring(separator + 1)
    }

    /** How a plugin is allowed to declare a given variable. The single source of truth for the policy. */
    enum class PluginEnvironmentPolicy { NATIVE_PATH, LAUNCHER_OWNED, DRIVER_NAME_ONLY, UNPROTECTED, UNCLASSIFIED }

    internal fun pluginEnvironmentPolicy(key: String?): PluginEnvironmentPolicy {
        if (isNativePathEnvironmentKey(key)) return PluginEnvironmentPolicy.NATIVE_PATH
        if (key in LAUNCHER_OWNED_ENVIRONMENT_VARIABLES) return PluginEnvironmentPolicy.LAUNCHER_OWNED
        if (key in DRIVER_NAME_ENVIRONMENT_VARIABLES) return PluginEnvironmentPolicy.DRIVER_NAME_ONLY
        if (isProtectedNativeEnvironmentVariable(key)) return PluginEnvironmentPolicy.UNCLASSIFIED
        return PluginEnvironmentPolicy.UNPROTECTED
    }

    internal fun protectedNativeEnvironmentVariablesForTest(): Set<String> =
        PROTECTED_NATIVE_ENVIRONMENT_VARIABLES

    /**
     * A verified plugin still only speaks for its own library directory.  Declaring a protected path
     * outside it, or replacing a variable the launcher owns, would turn one-time plugin trust into a
     * loading path that any other process able to write that location controls.
     */
    internal fun verifyPluginDeclaredEnvironment(
        label: String,
        nativeDirectory: String?,
        key: String,
        value: String?
    ) {
        when (pluginEnvironmentPolicy(key)) {
            PluginEnvironmentPolicy.NATIVE_PATH -> {
                if (value.isNullOrBlank()) {
                    throw IOException("$label declares an empty native path for $key")
                }
                for (entry in value.split(':')) {
                    if (entry.isBlank() || !(pathInside(nativeDirectory, entry) || isReadOnlySystemPath(entry))) {
                        throw IOException("$label environment points outside its installed library directory: $key")
                    }
                }
            }

            PluginEnvironmentPolicy.LAUNCHER_OWNED ->
                throw IOException("$label may not replace the launcher-controlled environment variable $key")

            PluginEnvironmentPolicy.DRIVER_NAME_ONLY -> {
                if (value == null || !DRIVER_NAME.matches(value)) {
                    throw IOException("$label declares $key as something other than a plain driver name")
                }
            }

            PluginEnvironmentPolicy.UNPROTECTED -> return

            // A protected variable reaching here was added without deciding how a plugin may set
            // it. Refuse rather than pass it through to native code.
            PluginEnvironmentPolicy.UNCLASSIFIED ->
                throw IOException("$label declares the unclassified protected environment variable $key")
        }
    }

    private fun isReadOnlySystemPath(path: String): Boolean =
        READ_ONLY_SYSTEM_LIBRARY_ROOTS.any { pathInside(it, path) }

    fun verify(context: Context, authorizations: List<PluginLoadAuthorization>) {
        val vpl = VerifiedPluginLoadRegistry.get(context)
        val allowUntrusted = AllSettings.allowUntrustedPlugins.getValue()

        val renderer = RendererPluginManager.selectedRendererPlugin
        if (renderer != null) {
            verifyPlugin(
                vpl = vpl,
                authorizations = authorizations,
                type = "Renderer",
                packageName = renderer.packageName,
                expectedNativeDirectory = renderer.path,
                allowUntrusted = allowUntrusted
            )
            verifyRendererLibraries(renderer)
        }

        val driver = DriverPluginManager.getDriver()
        if (!driver.isLauncher) {
            verifyPlugin(
                vpl = vpl,
                authorizations = authorizations,
                type = "Vulkan driver",
                packageName = driver.packageName,
                expectedNativeDirectory = driver.path,
                allowUntrusted = allowUntrusted
            )
        } else if (!samePath(driver.path, context.applicationInfo.nativeLibraryDir)) {
            throw IOException("Vulkan driver path is not owned by the launcher or an installed plugin APK")
        }

        for (plugin in NativePluginManager.getCheckedPlugins()) {
            verifyPlugin(
                vpl = vpl,
                authorizations = authorizations,
                type = "Native plugin",
                packageName = plugin.packageName,
                expectedNativeDirectory = plugin.path,
                allowUntrusted = allowUntrusted
            )
            verifyNativePluginEnvironment(plugin)
        }

        if (FFmpegPluginManager.isAvailable) {
            val libraryPath = FFmpegPluginManager.libraryPath
            if (libraryPath != null) {
                verifyPlugin(vpl, authorizations, "FFmpeg", "net.kdt.pojavlaunch.ffmpeg", libraryPath, allowUntrusted)
                requireLibraryInside(
                    nativeDirectory = libraryPath,
                    library = "libffmpeg.so",
                    label = "FFmpeg plugin"
                )
            }
        }
    }

    private fun verifyPlugin(
        vpl: VerifiedPluginLoad,
        authorizations: List<PluginLoadAuthorization>,
        type: String,
        packageName: String,
        expectedNativeDirectory: String,
        allowUntrusted: Boolean
    ) {
        val result = vpl.inspectInstalledPackage(packageName)
        if (result.status != PluginTrustStatus.TRUSTED) {
            throw IOException("$type $packageName is not trusted: ${result.status}")
        }
        if (!isExplicitKeyTrustAllowed(result.trustSource, allowUntrusted)) {
            throw IOException("$type $packageName is trusted only by an individual signature hash while untrusted plugin loading is disabled")
        }

        val apkPath = result.packageInfo.apkPath
        val nativeDirectory = result.packageInfo.nativeLibraryDirectory
        if (!samePath(nativeDirectory, expectedNativeDirectory)) {
            throw IOException("$type native library directory no longer matches its APK package")
        }

        verifySupportedAbi(nativeDirectory)

        val authorized = authorizations.any { auth ->
            auth.packageName == packageName &&
                    samePath(auth.apkPath, apkPath) &&
                    auth.versionCode == result.packageInfo.versionCode &&
                    auth.currentSignatures == result.currentSignatures.toSet()
        }
        if (!authorized) {
            throw IOException("$type has no matching pre-launch verification authorization")
        }

        val hash = result.matchedSignature?.sha256 ?: "unknown"
        Logger.info(
            TAG,
            "$type trusted: package=$packageName, " +
                    "version=${result.packageInfo.versionName}, " +
                    "sha256=$hash, " +
                    "trustListVersion=${result.trustListVersion}"
        )
    }

    private fun verifyRendererLibraries(renderer: RendererPlugin) {
        requireLibraryInside(renderer.path, renderer.glName, "Renderer OpenGL library")
        if (renderer.eglName.isNotEmpty()) {
            requireLibraryInside(
                nativeDirectory = renderer.path,
                library = renderer.eglName,
                label = "Renderer EGL library"
            )
        }
        for (dlopenLib in renderer.dlopen) {
            requireLibraryInside(
                nativeDirectory = renderer.path,
                library = dlopenLib,
                label = "Renderer DLOPEN library"
            )
        }
        for ((key, value) in renderer.env) {
            if (key == "LIB_MESA_NAME" || key == "MESA_LIBRARY") {
                // RendererPluginManager resolves these two against the plugin directory before
                // exposing them, so the stored value is already absolute.
                requireLibraryInside(
                    nativeDirectory = renderer.path,
                    library = value,
                    label = "Renderer Mesa library"
                )
            } else {
                verifyPluginDeclaredEnvironment("Renderer", renderer.path, key, value)
            }
        }
    }

    private fun verifyNativePluginEnvironment(plugin: NativePlugin) {
        for (entry in plugin.envList) {
            val (key, value) = parsePluginEnvironmentEntry(entry) ?: continue
            verifyPluginDeclaredEnvironment("Native plugin", plugin.path, key, value)
        }
    }

    private fun isNativePathEnvironmentKey(key: String?): Boolean {
        return key != null && (key.startsWith("LD_") || key in NATIVE_PATH_ENVIRONMENT_VARIABLES || key == "PATH")
    }

    private fun verifySupportedAbi(nativeDirectory: String?) {
        if (nativeDirectory == null) return
        val deviceSupportsAllowedAbi = Build.SUPPORTED_ABIS.any { it in ALLOWED_ABIS }
        if (!deviceSupportsAllowedAbi) {
            throw IOException("External native plugins are not supported on this device ABI")
        }
        val normalized = nativeDirectory.replace('\\', '/')
        if (normalized.endsWith("/x86") || normalized.contains("/x86/")) {
            throw IOException("x86 external native plugins are not permitted")
        }
    }

    /**
     * Resolves a plugin-declared library against its own directory and refuses anything that lands
     * outside it.  The declared form varies by source: DLOPEN entries and a renderer's glName stay
     * relative, while RendererPluginManager rewrites eglName and LIB_MESA_NAME/MESA_LIBRARY into
     * absolute paths under the plugin directory.  Treating an already-absolute value as relative
     * would re-join it onto the directory and yield a path that cannot exist, so resolve each form
     * as what it is and let the containment check decide.
     */
    internal fun requireLibraryInside(nativeDirectory: String?, library: String, label: String?) {
        if (library.isBlank() || !library.endsWith(".so")) {
            throw IOException("$label is not a shared-library file")
        }
        val target = if (library.startsWith("/")) {
            File(library).canonicalFile
        } else {
            File(nativeDirectory, library).canonicalFile
        }
        if (!pathInside(nativeDirectory, target.path)) {
            throw IOException("$label escapes the installed native library directory")
        }
        if (!target.isFile()) {
            throw IOException("$label is missing from the installed native library directory")
        }
    }

    private fun pathInside(base: String?, path: String?): Boolean {
        if (base == null || path == null) return false
        val baseFile = File(base).getCanonicalFile()
        val targetFile = File(path).getCanonicalFile()
        return targetFile.toPath().startsWith(baseFile.toPath())
    }

    private fun samePath(first: String?, second: String?): Boolean {
        if (first == null || second == null) return false
        return try {
            File(first).canonicalPath == File(second).canonicalPath
        } catch (_: IOException) {
            false
        }
    }
}
