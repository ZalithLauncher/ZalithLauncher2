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

import com.movtery.zalithlauncher.game.plugin.vpl.PluginNativeLoadGuard.PluginEnvironmentPolicy
import com.vpl.verifiedpluginload.model.TrustSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.nio.file.Files

class PluginNativeLoadGuardTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun customEnvironmentCannotOverrideNativeLoadInputs() {
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("LD_LIBRARY_PATH"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("LD_PRELOAD"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("POJAVEXEC_EGL"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("DRIVER_PATH"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("VK_ICD_FILENAMES"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("TMPDIR"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("POJAV_NATIVEDIR"))
    }

    @Test
    fun unrelatedCustomEnvironmentRemainsSupported() {
        assertFalse(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("JAVA_TOOL_OPTIONS"))
        assertFalse(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("POJAV_RENDERER"))
        assertFalse(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("CUSTOM_GAME_FLAG"))
    }

    @Test
    fun explicitKeyTrustRequiresTheUntrustedPluginSetting() {
        assertFalse(PluginNativeLoadGuard.isExplicitKeyTrustAllowed(TrustSource.KEY, false))
        assertTrue(PluginNativeLoadGuard.isExplicitKeyTrustAllowed(TrustSource.KEY, true))
        assertTrue(PluginNativeLoadGuard.isExplicitKeyTrustAllowed(TrustSource.AUTHOR, false))
        assertTrue(PluginNativeLoadGuard.isExplicitKeyTrustAllowed(null, false))
    }

    @Test
    fun pluginDeclaredPathMayStayInsideItsOwnLibraryDirectory() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")
        val inside = File(pluginDirectory, "vulkan.json").path

        PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
            "Renderer", pluginDirectory.path, "VK_ICD_FILENAMES", inside
        )
        PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
            "Renderer", pluginDirectory.path, "LD_LIBRARY_PATH", pluginDirectory.path
        )
    }

    @Test
    fun pluginDeclaredPathCannotEscapeItsOwnLibraryDirectory() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")
        val shared = temporaryFolder.newFolder("shared-storage")

        for (key in listOf(
            "VK_ICD_FILENAMES", "VK_LAYER_PATH", "VK_ADD_LAYER_PATH", "VK_DRIVER_FILES",
            "VK_ADD_DRIVER_FILES", "LIBGL_DRIVERS_PATH", "LD_PRELOAD", "LD_LIBRARY_PATH", "PATH"
        )) {
            val value = File(shared, "evil.so").path
            assertThrows(key, IOException::class.java) {
                PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                    "Renderer", pluginDirectory.path, key, value
                )
            }
        }
    }

    @Test
    fun pluginDeclaredPathCannotSmuggleAnOutsideEntryIntoAPathList() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")
        val shared = temporaryFolder.newFolder("shared-storage")

        assertThrows(IOException::class.java) {
            PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                "Renderer",
                pluginDirectory.path,
                "LD_LIBRARY_PATH",
                pluginDirectory.path + ":" + shared.path
            )
        }
    }

    @Test
    fun pluginCannotReplaceLauncherOwnedVariables() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")

        for (key in listOf(
            "TMPDIR", "FCL_NATIVEDIR", "POJAV_NATIVEDIR", "MOD_ANDROID_RUNTIME",
            "RENDERER_HANDLE", "VULKAN_PTR"
        )) {
            assertThrows(key, IOException::class.java) {
                PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                    "Native plugin", pluginDirectory.path, key, pluginDirectory.path
                )
            }
        }
    }

    @Test
    fun passthroughRendererMayStillPointAtReadOnlySystemDrivers() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")

        PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
            "Renderer", pluginDirectory.path, "LIBGL_DRIVERS_PATH", "/vendor/lib64/egl"
        )
        PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
            "Renderer",
            pluginDirectory.path,
            "LD_LIBRARY_PATH",
            pluginDirectory.path + ":/system/lib64:/vendor/lib64/hw"
        )
    }

    @Test
    fun aSystemLookalikePathIsNotTreatedAsASystemPath() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")

        assertThrows(IOException::class.java) {
            PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                "Renderer", pluginDirectory.path, "LIBGL_DRIVERS_PATH", "/systemx/lib64"
            )
        }
        assertThrows(IOException::class.java) {
            PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                "Renderer", pluginDirectory.path, "LIBGL_DRIVERS_PATH", "/vendor/../sdcard/lib"
            )
        }
    }

    @Test
    fun aSymlinkOutOfThePluginDirectoryDoesNotPassAsAnInsidePath() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")
        val shared = temporaryFolder.newFolder("shared-storage")
        val escape = File(pluginDirectory, "drivers")
        try {
            Files.createSymbolicLink(escape.toPath(), shared.toPath())
        } catch (_: UnsupportedOperationException) {
            return // The filesystem under test cannot express this; the check itself is unchanged.
        } catch (_: IOException) {
            return
        }

        assertThrows(IOException::class.java) {
            PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                "Renderer", pluginDirectory.path, "LIBGL_DRIVERS_PATH", escape.path
            )
        }
    }

    @Test
    fun pluginKeepsUnrelatedTuningVariables() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")

        PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
            "Renderer", pluginDirectory.path, "LIBGL_ES", "3"
        )
        PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
            "Renderer", pluginDirectory.path, "POJAV_RENDERER", "opengles3"
        )
    }

    @Test
    fun aDriverNameIsAcceptedOnlyAsABareName() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")

        for (key in listOf("MESA_LOADER_DRIVER_OVERRIDE", "GALLIUM_DRIVER")) {
            PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                "Renderer", pluginDirectory.path, key, "zink"
            )

            // Mesa concatenates this into "<search dir>/<name>_dri.so" without rejecting separators,
            // so a name carrying a traversal escapes the directory constrained by LIBGL_DRIVERS_PATH.
            for (escape in listOf(
                "../../../../../../sdcard/payload", "/sdcard/payload", "a/b", "zink x", ""
            )) {
                assertThrows("$key <- $escape", IOException::class.java) {
                    PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                        "Renderer", pluginDirectory.path, key, escape
                    )
                }
            }
        }
    }

    @Test
    fun pointerCarryingVariablesAreLauncherOwned() {
        // jni/environ/environ.c adopts POJAV_ENVIRON as a raw pointer via strtoul in a constructor,
        // and jni/egl_bridge.c does the same with VULKAN_PTR, so no caller may supply one.
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("POJAV_ENVIRON"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("FCL_ENVIRON"))
        assertTrue(PluginNativeLoadGuard.isProtectedNativeEnvironmentVariable("VULKAN_PTR"))
    }

    @Test
    fun everyProtectedVariableIsClassifiedForPlugins() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")
        // A protected variable that belongs to no bucket would be silently passed through to native
        // code. Assert that no such variable exists by requiring each one to be actively rejected
        // when a plugin declares an escaping value for it.
        for (key in PluginNativeLoadGuard.protectedNativeEnvironmentVariablesForTest()) {
            assertThrows("unclassified protected variable: $key", IOException::class.java) {
                PluginNativeLoadGuard.verifyPluginDeclaredEnvironment(
                    "Renderer", pluginDirectory.path, key, "/sdcard/attacker/payload"
                )
            }
        }
    }

    @Test
    fun theProtectedSetIsPartitionedByExactlyOnePluginPolicy() {
        // The runtime backstop throws on an unclassified protected variable; this keeps the partition
        // total by construction, so a newly protected variable fails here rather than at launch.
        for (key in PluginNativeLoadGuard.protectedNativeEnvironmentVariablesForTest()) {
            assertNotEquals(
                "unclassified protected variable: $key",
                PluginEnvironmentPolicy.UNCLASSIFIED,
                PluginNativeLoadGuard.pluginEnvironmentPolicy(key)
            )
            assertNotEquals(
                "protected variable treated as unprotected: $key",
                PluginEnvironmentPolicy.UNPROTECTED,
                PluginNativeLoadGuard.pluginEnvironmentPolicy(key)
            )
        }
        assertEquals(
            PluginEnvironmentPolicy.NATIVE_PATH,
            PluginNativeLoadGuard.pluginEnvironmentPolicy("LD_PRELOAD")
        )
        assertEquals(
            PluginEnvironmentPolicy.UNPROTECTED,
            PluginNativeLoadGuard.pluginEnvironmentPolicy("LIBGL_ES")
        )
    }

    @Test
    fun everyConsumerParsesAPluginEntryIdentically() {
        // The guard authorized the value half; the loader and the exporter must see the same string.
        assertEquals(
            "DLOPEN" to "a.so=b",
            PluginNativeLoadGuard.parsePluginEnvironmentEntry("DLOPEN=a.so=b")
        )
        assertEquals(
            "LIBGL_ES" to "3",
            PluginNativeLoadGuard.parsePluginEnvironmentEntry("LIBGL_ES=3")
        )
        assertNull(PluginNativeLoadGuard.parsePluginEnvironmentEntry("DLOPEN"))
        assertNull(PluginNativeLoadGuard.parsePluginEnvironmentEntry("DLOPEN="))
        assertNull(PluginNativeLoadGuard.parsePluginEnvironmentEntry("=value"))
        assertNull(PluginNativeLoadGuard.parsePluginEnvironmentEntry(null))
    }

    @Test
    fun anAlreadyAbsolutePluginLibraryIsNotRejoinedOntoItsDirectory() {
        // RendererPluginManager rewrites eglName and LIB_MESA_NAME/MESA_LIBRARY into absolute paths
        // under the plugin directory. Re-joining such a value onto the directory yields a path that
        // can never exist, so the check would refuse every Mesa renderer plugin.
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")
        val library = File(pluginDirectory, "libOSMesa.so")
        library.writeBytes(ByteArray(0))

        PluginNativeLoadGuard.requireLibraryInside(
            pluginDirectory.path, library.path, "Renderer Mesa library"
        )
        PluginNativeLoadGuard.requireLibraryInside(
            pluginDirectory.path, "libOSMesa.so", "Renderer Mesa library"
        )
    }

    @Test
    fun anAbsolutePluginLibraryOutsideItsDirectoryIsStillRefused() {
        val pluginDirectory = temporaryFolder.newFolder("plugin-lib")
        val shared = temporaryFolder.newFolder("shared-storage")
        val outside = File(shared, "libEvil.so")
        outside.writeBytes(ByteArray(0))

        assertThrows(IOException::class.java) {
            PluginNativeLoadGuard.requireLibraryInside(
                pluginDirectory.path, outside.path, "Renderer Mesa library"
            )
        }
    }
}
