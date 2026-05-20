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

package com.movtery.zalithlauncher.utils.device

import androidx.annotation.Keep
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import org.apache.commons.io.FileUtils
import java.io.File

@Keep
data class VulkanCapabilities(
    val apiVersionMajor: Int,
    val apiVersionMinor: Int,
    val apiVersionPatch: Int,
    val extensions: List<String>,
    val features: Map<String, Boolean>
) {
    /** Vulkan 版本字符串 */
    val versionString: String
        get() = "$apiVersionMajor.$apiVersionMinor.$apiVersionPatch"

    /** 检查 Vulkan 版本是否至少为 1.2 */
    val isVersionSupported: Boolean
        get() = apiVersionMajor > 1 || (apiVersionMajor == 1 && apiVersionMinor >= 2)

    /** 返回设备缺失的必要扩展 */
    val missingExtensions: List<String>
        get() = REQUIRED_EXTENSIONS.filter { it !in extensions }

    /** 返回设备不支持的必要功能 */
    val missingFeatures: List<String>
        get() = REQUIRED_FEATURES.filter { features[it] != true }

    /** 设备是否满足所有需求 */
    val isAllSupported: Boolean
        get() = isVersionSupported && missingExtensions.isEmpty() && missingFeatures.isEmpty()

    companion object {
        val REQUIRED_EXTENSIONS = listOf(
            "VK_KHR_dynamic_rendering",
            "VK_KHR_push_descriptor",
            "VK_KHR_synchronization2",
            "VK_EXT_vertex_attribute_divisor",
            "VK_KHR_swapchain"
        )

        val REQUIRED_FEATURES = listOf(
            "multiDrawIndirect",
            "fillModeNonSolid",
            "samplerAnisotropy",
            "shaderDrawParameters",
            "timelineSemaphore",
            "hostQueryReset",
            "synchronization2",
            "dynamicRendering",
            "vertexAttributeInstanceRateDivisor"
        )
    }
}

@Keep
fun interface VulkanLogCallback {
    fun log(level: String, message: String)
}

object VulkanChecker {
    init {
        try {
            System.loadLibrary("vulkan_check")
            nativeSetLogCallback { level, msg ->
                when (level) {
                    "INFO" -> lInfo(msg)
                    "WARN" -> lWarning(msg)
                    "ERROR" -> lError(msg)
                    else -> lDebug(msg)
                }
            }
        } catch (e: UnsatisfiedLinkError) {
            lError("Failed to load vulkan_check library", e)
        }
    }

    /**
     * 查询系统 Vulkan 支持情况
     * @return 如果不支持 Vulkan 或初始化失败，返回 null
     */
    fun checkCapabilities(
        driverPath: String?,
        nativeDir: String?,
        cacheDir: String?
    ): VulkanCapabilities? {
        return try {
            nativeCheckVulkan(
                driverPath = driverPath,
                nativeDir = nativeDir,
                cacheDir = cacheDir,
            )?.also { caps ->
                lInfo("Vulkan version: ${caps.versionString}")
                lInfo("Version >= 1.2: ${caps.isVersionSupported}")
                if (caps.missingExtensions.isNotEmpty()) {
                    lWarning("Missing required extensions: ${caps.missingExtensions}")
                }
                if (caps.missingFeatures.isNotEmpty()) {
                    lWarning("Missing required features: ${caps.missingFeatures}")
                }
                lInfo("All requirements satisfied: ${caps.isAllSupported}")
            }
        } catch (e: UnsatisfiedLinkError) {
            lError("Native library or method not found", e)
            null
        } catch (e: Exception) {
            lError("Native check failed", e)
            null
        } finally {
            if (nativeDir != null && cacheDir != null) {
                FileUtils.deleteQuietly(File(cacheDir))
            }
        }
    }

    @JvmStatic
    private external fun nativeSetLogCallback(callback: VulkanLogCallback)

    @JvmStatic
    private external fun nativeCheckVulkan(
        driverPath: String?,
        nativeDir: String?,
        cacheDir: String?
    ): VulkanCapabilities?
}
