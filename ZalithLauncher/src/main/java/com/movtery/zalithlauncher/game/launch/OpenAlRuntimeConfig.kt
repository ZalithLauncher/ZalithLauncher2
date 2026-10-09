/*
 * Zalith Launcher 2
 * Copyright (C) 2026 Aerix Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.launch

import java.io.File

/** OpenAL Soft settings shared by the bundled ABI-matched native and LWJGL. */
internal object OpenAlRuntimeConfig {
    const val CONF_ASSET_PATH = "openal/alsoft.conf"
    const val OPENSL_DRIVER = "opensl"

    private val driversLine = Regex("(?m)^\\s*drivers\\s*=\\s*(.+?)\\s*$", RegexOption.IGNORE_CASE)

    /**
     * Install the packaged OpenAL configuration into app-private storage. The native OpenAL
     * library can read this path before the Java game starts, while APK assets are not files.
     */
    fun installConfig(target: File, configText: String): File {
        val drivers = configuredDrivers(configText)
        require(OPENSL_DRIVER in drivers.split(',').map { it.trim() }) {
            "The bundled OpenAL configuration must include the OpenSL ES driver"
        }

        val output = target.absoluteFile
        val parent = output.parentFile
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw IllegalStateException("Unable to create OpenAL config directory: ${parent.absolutePath}")
        }
        if (!output.isFile || output.readText() != configText) {
            output.writeText(configText)
        }
        return output
    }

    /** Keep ALSOFT_DRIVERS identical to the driver list in the file named by ALSOFT_CONF. */
    fun environment(configFile: File, configText: String): Map<String, String> = mapOf(
        "ALSOFT_CONF" to configFile.absolutePath,
        "ALSOFT_DRIVERS" to configuredDrivers(configText),
    )

    /** Resolve OpenAL from Android's nativeLibraryDir, which is ABI-specific to this process. */
    fun lwjglOpenAlProperty(nativeLibraryDirectory: String): String =
        "-Dorg.lwjgl.openal.libname=${File(nativeLibraryDirectory, "libopenal.so").absolutePath}"

    private fun configuredDrivers(configText: String): String =
        driversLine.find(configText)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("The OpenAL config is missing a [general] drivers entry")
}
