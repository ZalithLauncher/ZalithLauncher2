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

package com.movtery.zalithlauncher.crashlogs.analysis

import java.io.File
import java.io.RandomAccessFile

enum class DiagnosisSeverity { CAUSE, INFO }

/**
 * 日志分析结果
 * @param id 诊断编号，界面据此显示本地化文本
 * @param evidence 日志中匹配到的原始行（用于佐证）
 */
data class CrashDiagnosis(
    val id: String,
    val severity: DiagnosisSeverity,
    val evidence: String
)

/**
 * NusaLauncher: 本地日志分析器（离线，基于特征匹配）
 * Penganalisis log lokal berbasis pola. Berjalan tanpa internet dan fokus pada crash khas Android.
 */
object CrashAnalyzer {
    private const val MAX_BYTES = 4L * 1024 * 1024

    private class Rule(
        val id: String,
        val severity: DiagnosisSeverity,
        /** 数值越小优先级越高 */
        val priority: Int,
        val patterns: List<Regex>
    )

    private fun rx(s: String) = Regex(s, RegexOption.IGNORE_CASE)

    private val rules: List<Rule> = listOf(
        Rule("out_of_memory", DiagnosisSeverity.CAUSE, 10, listOf(
            rx("java\\.lang\\.OutOfMemoryError"), rx("Java heap space"), rx("GC overhead limit exceeded")
        )),
        Rule("java_version", DiagnosisSeverity.CAUSE, 11, listOf(
            rx("UnsupportedClassVersionError"), rx("compiled by a more recent version of the Java Runtime")
        )),
        Rule("disk_full", DiagnosisSeverity.CAUSE, 12, listOf(
            rx("No space left on device")
        )),
        Rule("mod_dependency", DiagnosisSeverity.CAUSE, 20, listOf(
            rx("Incompatible mods? found"), rx("Unmet dependenc"), rx("Missing or unsupported mandatory dependencies"),
            rx("Mod resolution failed"), rx("requires (?:any version of |version )?[\\w.-]+ ?(?:\\(|, )?.*(?:which is missing|but none|but only)")
        )),
        Rule("mixin_failure", DiagnosisSeverity.CAUSE, 25, listOf(
            rx("Mixin apply failed"), rx("InvalidInjectionException"), rx("MixinTransformerError"), rx("Mixin transformation of .* failed")
        )),
        Rule("corrupt_file", DiagnosisSeverity.CAUSE, 30, listOf(
            rx("zip END header not found"), rx("invalid LOC header"), rx("ZipException"), rx("Invalid or corrupt jarfile")
        )),
        Rule("main_class_missing", DiagnosisSeverity.CAUSE, 31, listOf(
            rx("Could not find or load main class"), rx("ClassNotFoundException: net\\.minecraft\\.client\\.main\\.Main")
        )),
        Rule("shader_compile", DiagnosisSeverity.CAUSE, 35, listOf(
            rx("Couldn't compile (?:fragment|vertex|geometry) shader"), rx("Failed to load required shader programs"),
            rx("shader.*(?:compile|link).*(?:error|failed)")
        )),
        Rule("opengl_unsupported", DiagnosisSeverity.CAUSE, 36, listOf(
            rx("GLFW error 65543"), rx("GLX: Failed to create context"), rx("does not appear to support OpenGL"),
            rx("Failed to create (?:OpenGL|EGL) context"), rx("OpenGL \\d(?:\\.\\d)? (?:is )?required")
        )),
        Rule("native_crash", DiagnosisSeverity.CAUSE, 40, listOf(
            rx("SIGSEGV"), rx("SIGABRT"), rx("A fatal error has been detected by the Java Runtime Environment"), rx("hs_err_pid")
        )),
        Rule("runtime_load_failed", DiagnosisSeverity.CAUSE, 45, listOf(
            rx("dlopen failed"), rx("UnsatisfiedLinkError"), rx("cannot locate symbol")
        )),
        Rule("low_memory_kill", DiagnosisSeverity.CAUSE, 50, listOf(
            rx("exit code:?\\s*137"), rx("signal 9 \\(SIGKILL\\)"), rx("lowmemorykiller")
        )),
        Rule("offline_auth_normal", DiagnosisSeverity.INFO, 90, listOf(
            rx("InvalidCredentialsException"), rx("Could not authorize you against Realms"), rx("Failed to fetch user properties")
        ))
    )

    fun analyze(log: String): List<CrashDiagnosis> {
        val results = ArrayList<Pair<Int, CrashDiagnosis>>()
        for (rule in rules) {
            for (pattern in rule.patterns) {
                val m = pattern.find(log) ?: continue
                results.add(rule.priority to CrashDiagnosis(rule.id, rule.severity, lineAround(log, m.range.first)))
                break
            }
        }
        results.sortBy { it.first }
        return results.map { it.second }
    }

    /** 最可能的崩溃原因（忽略仅供参考的提示） */
    fun primary(log: String): CrashDiagnosis? =
        analyze(log).firstOrNull { it.severity == DiagnosisSeverity.CAUSE }

    /** 读取日志文件的末尾部分后分析（崩溃信息通常在末尾） */
    fun analyzeFile(file: File): List<CrashDiagnosis> {
        if (!file.exists() || !file.isFile) return emptyList()
        val length = file.length()
        val text = if (length <= MAX_BYTES) {
            file.readText()
        } else {
            RandomAccessFile(file, "r").use { raf ->
                raf.seek(length - MAX_BYTES)
                val buf = ByteArray(MAX_BYTES.toInt())
                raf.readFully(buf)
                String(buf, Charsets.UTF_8)
            }
        }
        return analyze(text)
    }

    private fun lineAround(text: String, index: Int): String {
        val start = text.lastIndexOf('\n', index).let { if (it < 0) 0 else it + 1 }
        val end = text.indexOf('\n', index).let { if (it < 0) text.length else it }
        val line = text.substring(start, end).trim()
        return if (line.length > 200) line.substring(0, 200) + "…" else line
    }
}
