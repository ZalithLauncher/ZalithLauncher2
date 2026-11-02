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

package com.movtery.zalithlauncher.ui.screens.main.crashlogs

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.crashlogs.analysis.CrashDiagnosis

/**
 * NusaLauncher: 诊断编号对应的界面文本
 */
data class DiagnosisTexts(
    @StringRes val title: Int,
    @StringRes val hint: Int
)

fun CrashDiagnosis.texts(): DiagnosisTexts? = when (id) {
        "out_of_memory" -> DiagnosisTexts(R.string.crash_diag_out_of_memory_title, R.string.crash_diag_out_of_memory_hint)
        "java_version" -> DiagnosisTexts(R.string.crash_diag_java_version_title, R.string.crash_diag_java_version_hint)
        "disk_full" -> DiagnosisTexts(R.string.crash_diag_disk_full_title, R.string.crash_diag_disk_full_hint)
        "mod_dependency" -> DiagnosisTexts(R.string.crash_diag_mod_dependency_title, R.string.crash_diag_mod_dependency_hint)
        "mixin_failure" -> DiagnosisTexts(R.string.crash_diag_mixin_failure_title, R.string.crash_diag_mixin_failure_hint)
        "corrupt_file" -> DiagnosisTexts(R.string.crash_diag_corrupt_file_title, R.string.crash_diag_corrupt_file_hint)
        "main_class_missing" -> DiagnosisTexts(R.string.crash_diag_main_class_missing_title, R.string.crash_diag_main_class_missing_hint)
        "shader_compile" -> DiagnosisTexts(R.string.crash_diag_shader_compile_title, R.string.crash_diag_shader_compile_hint)
        "opengl_unsupported" -> DiagnosisTexts(R.string.crash_diag_opengl_unsupported_title, R.string.crash_diag_opengl_unsupported_hint)
        "native_crash" -> DiagnosisTexts(R.string.crash_diag_native_crash_title, R.string.crash_diag_native_crash_hint)
        "runtime_load_failed" -> DiagnosisTexts(R.string.crash_diag_runtime_load_failed_title, R.string.crash_diag_runtime_load_failed_hint)
        "low_memory_kill" -> DiagnosisTexts(R.string.crash_diag_low_memory_kill_title, R.string.crash_diag_low_memory_kill_hint)
        else -> null
}
