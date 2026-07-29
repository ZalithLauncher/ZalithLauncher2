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

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText

/**
 * One labelled row in a trust dialog.
 *
 * The label is always a launcher string resource. The value is either a launcher string resource or
 * plain text that has already been through [singleLine]. A plugin-supplied string can therefore only
 * ever become a value, never a heading, so the shape of the dialog is fixed by the launcher no matter
 * what a plugin declares.
 */
data class TrustFact(
    @StringRes val label: Int,
    val value: AndroidStringText?,
    /** Fingerprints get a fixed-pitch face so two of them can be compared by eye. */
    val monospace: Boolean = false
) {
    companion object {
        /** A row whose value came from outside the launcher; bounded and stripped of structure. */
        fun sanitized(@StringRes label: Int, value: String?, maxLength: Int) =
            TrustFact(label, androidText(singleLine(value, maxLength)))

        /** A row whose value is a launcher string resource. */
        fun resource(@StringRes label: Int, @StringRes value: Int) =
            TrustFact(label, androidText(value))

        /** A row whose value is launcher-produced plain text. */
        fun literal(@StringRes label: Int, value: String?, monospace: Boolean = false) =
            TrustFact(label, value?.let { androidText(it) }, monospace)
    }
}

/** A titled group of [TrustFact] rows. The title is a launcher resource, never plugin text. */
data class TrustSection(
    @StringRes val title: Int,
    val facts: List<TrustFact>
) {
    /** A section with nothing to say is not drawn at all. */
    val hasContent: Boolean get() = facts.any { it.value != null }
}

/**
 * Collapses anything a plugin or the signed trust list supplies into a single bounded line.
 *
 * Values reach the dialog as plain text. Any line separator in a plugin-supplied string would let
 * that plugin render text shaped exactly like the launcher's own registered-publisher block, and
 * trailing padding would push the only authentic evidence, the real package name and fingerprint,
 * out of the first viewport while the action button stayed pinned. Collapse every separator and
 * control character, drop the bidi overrides that reorder what is displayed, and cap the length so
 * injected padding cannot scroll the real fingerprint away.
 */
fun singleLine(value: String?, maxLength: Int): String {
    if (value == null) return "-"
    val sanitized = StringBuilder(value.length)
    value.codePoints().forEach { codePoint ->
        when (Character.getType(codePoint)) {
            Character.FORMAT.toInt(),
            Character.CONTROL.toInt(),
            Character.LINE_SEPARATOR.toInt(),
            Character.PARAGRAPH_SEPARATOR.toInt(),
            Character.UNASSIGNED.toInt(),
            Character.PRIVATE_USE.toInt(),
            Character.SURROGATE.toInt() -> sanitized.append(' ')

            else -> sanitized.appendCodePoint(codePoint)
        }
    }
    val collapsed = sanitized.toString().replace(Regex("\\s+"), " ").trim()
    if (collapsed.isEmpty()) return "-"
    return if (collapsed.length <= maxLength) collapsed else collapsed.take(maxLength) + "…"
}

/** Groups a fingerprint so two of them can be compared without counting characters. */
fun formatFingerprint(fingerprint: String?): String? {
    if (fingerprint.isNullOrEmpty()) return null
    return buildString(fingerprint.length + fingerprint.length / 4) {
        var index = 0
        while (index < fingerprint.length) {
            if (index > 0) append(if (index % 32 == 0) '\n' else ' ')
            append(fingerprint, index, minOf(index + 4, fingerprint.length))
            index += 4
        }
    }
}
