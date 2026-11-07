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

package com.movtery.zalithlauncher.game.renderer.renderers

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.ConfigurableRenderer
import com.movtery.zalithlauncher.game.renderer.config.ConfigScalar
import com.movtery.zalithlauncher.game.renderer.config.ConfigTarget
import com.movtery.zalithlauncher.game.renderer.config.ResolvedRendererConfig
import com.movtery.zalithlauncher.game.renderer.config.rendererConfigSpec
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private const val TAG = "MobileGluesRenderer"

object MobileGluesRenderer : ConfigurableRenderer {
    override val configSpec = rendererConfigSpec {
        fixedEnv(
            "LIBGL_ES" to "3",
            "LIBGL_EGL" to "libmobileglues.so"
        )
        switch(
            id = "sfpewLayer",
            title = R.string.renderer_config_mg_sfpew_title,
            description = R.string.renderer_config_mg_sfpew_desc,
            default = true,
            target = ConfigTarget.SfpewLayer
        )
        switch(
            id = "MG_COUNT_LAUNCH",
            title = R.string.renderer_config_mg_count_launch_title,
            description = R.string.renderer_config_mg_count_launch_desc,
            default = true,
            target = ConfigTarget.Environment("MG_COUNT_LAUNCH")
        )
        choice(
            id = "enableANGLE",
            title = R.string.renderer_config_mg_angle_title,
            description = R.string.renderer_config_mg_angle_desc,
            default = 0
        ) {
            option(0, R.string.renderer_config_mg_angle_disable_when_possible)
            option(1, R.string.renderer_config_mg_angle_enable_when_possible)
            option(2, R.string.renderer_config_mg_angle_force_disable)
            option(3, R.string.renderer_config_mg_angle_force_enable)
        }
        choice(
            id = "enableNoError",
            title = R.string.renderer_config_mg_noerror_title,
            description = R.string.renderer_config_mg_noerror_desc,
            default = 0
        ) {
            option(0, R.string.renderer_config_mg_noerror_auto)
            option(1, R.string.renderer_config_mg_noerror_off)
            option(2, R.string.renderer_config_mg_noerror_shader)
            option(3, R.string.renderer_config_mg_noerror_framebuffer)
        }
        orderedChoice(
            id = "multidrawOrder",
            title = R.string.renderer_config_mg_multidraw_order_title,
            description = R.string.renderer_config_mg_multidraw_order_desc,
            default = listOf(
                "native", "multiindirect", "multibasevertex", "multiarrays",
                "indirect", "basevertex", "unroll", "compute"
            )
        ) {
            option(
                "native",
                R.string.renderer_config_mg_multidraw_native,
                R.string.renderer_config_mg_multidraw_native_desc
            )
            option("multiindirect", "MultiIndirect")
            option("multibasevertex", "MultiBaseVertex")
            option("multiarrays", "MultiArrays")
            option("indirect", "Indirect")
            option("basevertex", "BaseVertex")
            option("unroll", R.string.renderer_config_mg_multidraw_unroll)
            option("compute", R.string.renderer_config_mg_multidraw_compute)
        }
        orderedChoice(
            id = "multidrawOrderArrays",
            title = "glMultiDrawArrays",
            description = R.string.renderer_config_mg_multidraw_exception_desc,
            default = listOf("multiarrays", "multiindirect", "unroll"),
            defaultEnabled = false,
            inheritFrom = "multidrawOrder",
            nativeValue = "multiarrays"
        ) {
            option("multiarrays", "MultiArrays")
            option("multiindirect", "MultiIndirect")
            option("unroll", R.string.renderer_config_mg_multidraw_unroll)
        }
        orderedChoice(
            id = "multidrawOrderElements",
            title = "glMultiDrawElements",
            description = R.string.renderer_config_mg_multidraw_exception_desc,
            default = listOf("multiarrays", "multiindirect", "multibasevertex", "indirect", "unroll"),
            defaultEnabled = false,
            inheritFrom = "multidrawOrder",
            nativeValue = "multiarrays"
        ) {
            option("multiarrays", "MultiArrays")
            option("multiindirect", "MultiIndirect")
            option("multibasevertex", "MultiBaseVertex")
            option("indirect", "Indirect")
            option("unroll", R.string.renderer_config_mg_multidraw_unroll)
        }
        orderedChoice(
            id = "multidrawOrderElementsBaseVertex",
            title = "glMultiDrawElementsBaseVertex",
            description = R.string.renderer_config_mg_multidraw_exception_desc,
            default = listOf(
                "multibasevertex", "multiindirect", "indirect", "basevertex", "unroll", "compute"
            ),
            defaultEnabled = false,
            inheritFrom = "multidrawOrder",
            nativeValue = "multibasevertex"
        ) {
            option("multibasevertex", "MultiBaseVertex")
            option("multiindirect", "MultiIndirect")
            option("indirect", "Indirect")
            option("basevertex", "BaseVertex")
            option("unroll", R.string.renderer_config_mg_multidraw_unroll)
            option("compute", R.string.renderer_config_mg_multidraw_compute)
        }
        orderedChoice(
            id = "multidrawOrderArraysIndirect",
            title = "glMultiDrawArraysIndirect",
            description = R.string.renderer_config_mg_multidraw_exception_desc,
            default = listOf("multiindirect", "indirect"),
            defaultEnabled = false,
            inheritFrom = "multidrawOrder",
            nativeValue = "multiindirect"
        ) {
            option("multiindirect", "MultiIndirect")
            option("indirect", "Indirect")
        }
        orderedChoice(
            id = "multidrawOrderElementsIndirect",
            title = "glMultiDrawElementsIndirect",
            description = R.string.renderer_config_mg_multidraw_exception_desc,
            default = listOf("multiindirect", "indirect"),
            defaultEnabled = false,
            inheritFrom = "multidrawOrder",
            nativeValue = "multiindirect"
        ) {
            option("multiindirect", "MultiIndirect")
            option("indirect", "Indirect")
        }
        switch(
            id = "angleDepthClearFixMode",
            title = R.string.renderer_config_mg_depthfix_title,
            description = R.string.renderer_config_mg_depthfix_desc,
            default = false,
            target = ConfigTarget.JsonField("angleDepthClearFixMode")
        )
        choice(
            id = "customGLVersion",
            title = R.string.renderer_config_mg_glversion_title,
            description = R.string.renderer_config_mg_glversion_desc,
            default = 0
        ) {
            option(0, R.string.renderer_config_mg_glversion_automatic)
            option(46, "OpenGL 4.6")
            option(45, "OpenGL 4.5")
            option(44, "OpenGL 4.4")
            option(43, "OpenGL 4.3")
            option(42, "OpenGL 4.2")
            option(41, "OpenGL 4.1")
            option(40, "OpenGL 4.0")
            option(33, "OpenGL 3.3")
            option(32, "OpenGL 3.2")
        }
        number(
            id = "maxGlslCacheSize",
            title = R.string.renderer_config_mg_glslcache_title,
            description = R.string.renderer_config_mg_glslcache_desc,
            defaultValue = ConfigScalar.IntValue(32),
            emptyValue = ConfigScalar.IntValue(-1),
            target = ConfigTarget.JsonField("maxGlslCacheSize")
        )
        switch(
            id = "enableExtTimerQuery",
            title = R.string.renderer_config_mg_ext_timer_title,
            description = R.string.renderer_config_mg_ext_timer_desc,
            default = true,
            target = ConfigTarget.JsonField("enableExtTimerQuery")
        )
        switch(
            id = "enableExtComputeShader",
            title = R.string.renderer_config_mg_ext_compute_title,
            description = R.string.renderer_config_mg_ext_compute_desc,
            default = false,
            target = ConfigTarget.JsonField("enableExtComputeShader")
        )
        switch(
            id = "enableExtDirectStateAccess",
            title = R.string.renderer_config_mg_ext_dsa_title,
            description = R.string.renderer_config_mg_ext_dsa_desc,
            default = false,
            target = ConfigTarget.JsonField("enableExtDirectStateAccess")
        )
        choice(
            id = "fsr1Setting",
            title = R.string.renderer_config_mg_fsr1_title,
            description = R.string.renderer_config_mg_fsr1_desc,
            default = 0
        ) {
            option(0, R.string.generic_disable)
            option(1, R.string.renderer_config_mg_fsr1_ultra_quality)
            option(2, R.string.renderer_config_mg_fsr1_quality)
            option(3, R.string.renderer_config_mg_fsr1_balanced)
            option(4, R.string.renderer_config_mg_fsr1_performance)
        }
    }

    override val legacyConfigKeys: List<String> = listOf("builtin:mobileglues")

    override fun getRendererId(): String = "opengles3_mobileglues"

    override fun getUniqueIdentifier(): String = "acaf8100-bbaf-4d68-b30d-38bf42fcd640"

    override fun supportsSfpewByDefault(): Boolean = true

    override fun getRendererName(): String = "MobileGlues"

    override fun getRendererSummary(): String = "OpenGL 4.0"

    override fun getConfiguredEnv(config: ResolvedRendererConfig): Map<String, String> {
        val mgDir = File(PathManager.DIR_FILES_PRIVATE, "mobileglues")
        writeMobileGluesConfig(mgDir, config.jsonFields)
        return config.environment + ("MG_DIR_PATH" to mgDir.absolutePath)
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libmobileglues.so"

    override fun getRendererEGL(): String = "libmobileglues.so"

    private fun writeMobileGluesConfig(mgDir: File, fields: Map<String, ConfigScalar>) {
        val managedKeysFile = File(mgDir, "launcher-keys")
        val previousKeys = if (managedKeysFile.isFile) {
            runCatching { managedKeysFile.readLines().filterTo(mutableSetOf()) { it.isNotBlank() } }
                .getOrElse {
                    Logger.warning(TAG, "Failed to read the launcher-managed MobileGlues keys.", it)
                    emptySet()
                }
        } else emptySet()
        val currentKeys = fields.keys
        if (currentKeys.isEmpty() && previousKeys.isEmpty()) return

        val configFile = File(mgDir, "config.json")
        val oldConfig = if (configFile.isFile) {
            runCatching { configFile.readText() }.getOrNull()
        } else null
        val json = runCatching { oldConfig?.let(::JSONObject) ?: JSONObject() }.getOrElse { JSONObject() }

        runCatching {
            previousKeys.filter { it !in currentKeys }.forEach(json::remove)
            fields.forEach { (key, value) ->
                when (value) {
                    is ConfigScalar.StringValue -> json.put(key, value.value)
                    is ConfigScalar.BooleanValue -> json.put(key, value.value)
                    is ConfigScalar.IntValue -> json.put(key, value.value)
                    is ConfigScalar.DoubleValue -> json.put(key, value.value)
                }
            }
        }.onFailure {
            Logger.warning(TAG, "Failed to merge the MobileGlues configuration.", it)
            return
        }

        val newConfig = json.toString()
        if (newConfig != oldConfig && !writeFileAtomically(configFile, newConfig)) return
        if (currentKeys != previousKeys) {
            if (currentKeys.isEmpty()) {
                if (managedKeysFile.exists() && !managedKeysFile.delete()) {
                    Logger.warning(TAG, "Failed to clear the launcher-managed MobileGlues keys.")
                }
            } else {
                writeFileAtomically(managedKeysFile, currentKeys.sorted().joinToString("\n"))
            }
        }
    }

    private fun writeFileAtomically(file: File, content: String): Boolean {
        val tmpFile = File(file.absoluteFile.parentFile, "${file.name}.tmp")
        return runCatching {
            val parent = requireNotNull(file.absoluteFile.parentFile)
            if (!parent.isDirectory) parent.mkdirs()
            FileOutputStream(tmpFile).use { stream ->
                stream.write(content.toByteArray())
                stream.fd.sync()
            }
            runCatching {
                Files.move(
                    tmpFile.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            }.getOrElse {
                Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            true
        }.onFailure {
            tmpFile.delete()
            Logger.warning(TAG, "Failed to write ${file.name}.", it)
        }.getOrDefault(false)
    }
}
