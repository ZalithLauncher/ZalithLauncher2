package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer

/**
 * Chooses an existing wrapper. This is not a new renderer.
 * 1.8-1.16.4 prefers GL4ES, then VirGL.
 * 1.17 and newer prefers LTW, then Zink.
 * A saved instance id wins when that wrapper is actually available.
 */
object RendererPicker {
    const val GL4ES = "8b52d82d-8f6d-4d3a-a767-dc93f8b72fc7"
    const val VIRGL = "a3ccc1fe-de3f-4a81-8c45-2485181b63b3"
    const val LTW = "a0a34376-5f5c-4be3-96d3-8c5afbbaf5bb"
    const val ZINK = "0fa435e2-46df-45c9-906c-b29606aaef00"

    data class Choice(
        val identifier: String,
        val reason: String,
        val automatic: Boolean,
    )

    fun pick(
        mcVersion: String,
        manualIdentifier: String,
        available: Set<String>,
    ): Choice {
        val manual = manualIdentifier.trim()
        if (manual.isNotEmpty()) {
            if (manual in available) {
                return Choice(manual, "instance override", automatic = false)
            }
            val fallback = automatic(mcVersion, available)
            return fallback.copy(reason = "instance override missing, ${fallback.reason}")
        }
        return automatic(mcVersion, available)
    }

    private fun automatic(mcVersion: String, available: Set<String>): Choice {
        val legacy = mcVersion.isBlank() || mcVersion.isLowerVer("1.17")
        val order = if (legacy) listOf(GL4ES, VIRGL) else listOf(LTW, ZINK)
        val picked = order.firstOrNull { it in available } ?: available.firstOrNull()
        val reason = when {
            picked == null -> "no renderer available"
            picked == order.first() -> if (legacy) "legacy version, GL4ES" else "1.17 or newer, LTW"
            picked in order -> if (legacy) "GL4ES unavailable, VirGL" else "LTW unavailable, Zink"
            else -> "preferred wrappers unavailable, first loaded renderer"
        }
        return Choice(picked.orEmpty(), reason, automatic = true)
    }
}
