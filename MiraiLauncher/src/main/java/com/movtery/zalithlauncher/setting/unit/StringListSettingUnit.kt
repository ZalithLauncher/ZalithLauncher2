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

package com.movtery.zalithlauncher.setting.unit

import android.os.Parcelable
import com.movtery.zalithlauncher.setting.launcherMMKV
import kotlinx.parcelize.Parcelize

class StringListSettingUnit(
    key: String,
    defaultValue: List<String>
) : AbstractSettingUnit<List<String>>(
    key = key,
    defaultValue = defaultValue
) {
    override fun getValue(): List<String> {
        val list = launcherMMKV().decodeParcelable(key, ListParcelable::class.java)
        return (list?.value ?: defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: List<String>): List<String> {
        launcherMMKV().encode(key, ListParcelable(v))
        return v
    }
}

@Parcelize
private data class ListParcelable(val value: List<String>) : Parcelable