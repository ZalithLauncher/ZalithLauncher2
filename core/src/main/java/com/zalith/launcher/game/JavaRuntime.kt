package com.zalith.launcher.game

import java.io.File

/**
 * Seleção de runtime Java por versão do jogo.
 * No Android o JRE vem empacotado com o APK (fase 0/runtime) e é
 * extraído/marcado em runtimes/jreN — aqui só resolvemos qual usar.
 */
object JavaRuntimes {

    /** Major exigido: o manifest manda (javaVersion.majorVersion); senão faixa. */
    fun requiredMajor(parsed: Int?, mcVersion: String): Int {
        if (parsed != null && parsed > 0) return parsed
        val v = parseTriple(mcVersion) ?: return 8
        return when {
            v[0] > 1 -> 21
            v[1] > 20 -> 21
            v[1] == 20 && v[2] >= 5 -> 21
            v[1] == 20 -> 17
            v[1] == 19 || v[1] == 18 -> 17
            v[1] == 17 -> 16
            else -> 8
        }
    }

    private fun parseTriple(version: String): IntArray? {
        val clean = version.substringBefore('-').substringBefore('_')
        val parts = clean.split('.')
        if (parts.size < 2) return null
        val major = parts[0].toIntOrNull() ?: return null
        val minor = parts[1].toIntOrNull() ?: return null
        val patch = if (parts.size > 2) parts[2].toIntOrNull() ?: 0 else 0
        return intArrayOf(major, minor, patch)
    }

    fun runtimeDir(baseDir: File, major: Int): File = File(baseDir, "runtimes/jre" + major)

    /** Válido quando tem bin/java e o marcador de instalação. */
    fun isInstalled(baseDir: File, major: Int): Boolean {
        val dir = runtimeDir(baseDir, major)
        return File(dir, "bin/java").exists() && File(dir, ".zl3-jre").exists()
    }

    /** Marca um runtime empacotado (extraído uma vez, na primeira execução). */
    fun markInstalled(baseDir: File, major: Int) {
        val dir = runtimeDir(baseDir, major)
        dir.mkdirs()
        File(dir, ".zl3-jre").writeText("ok", Charsets.UTF_8)
    }

    fun resolve(baseDir: File, major: Int): File {
        if (!isInstalled(baseDir, major)) {
            throw IllegalStateException(
                "runtime Java " + major + " não encontrado em runtimes/jre" + major +
                " — extraia o JRE empacotado antes de iniciar o jogo"
            )
        }
        return runtimeDir(baseDir, major)
    }
}
