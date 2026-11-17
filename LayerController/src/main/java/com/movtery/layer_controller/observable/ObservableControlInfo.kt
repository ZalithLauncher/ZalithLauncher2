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

package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.layout.ControlLayout

class ObservableControlInfo(
    private val info: ControlLayout.Info
) : Packable<ControlLayout.Info> {
    val name = ObservableTranslatableString(info.name)
    val author = ObservableTranslatableString(info.author)
    val description = ObservableTranslatableString(info.description)

    var versionCode by mutableIntStateOf(info.versionCode)
    var versionName by mutableStateOf(info.versionName)

    /**
     * 重置版本名称
     */
    fun resetVersionName() {
        versionName = info.versionName
    }

    override fun pack(): ControlLayout.Info {
        return ControlLayout.Info(
            name = name.pack(),
            author = author.pack(),
            description = description.pack(),
            versionCode = versionCode,
            versionName = versionName
        )
    }

    override fun isModified(): Boolean {
        return this.info.isModified(pack())
    }
}