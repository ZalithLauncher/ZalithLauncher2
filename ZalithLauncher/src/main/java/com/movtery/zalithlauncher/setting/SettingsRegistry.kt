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

package com.movtery.zalithlauncher.setting

import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.DEFAULT_FLOAT_RANGE
import com.movtery.zalithlauncher.setting.unit.DEFAULT_INT_RANGE
import com.movtery.zalithlauncher.setting.unit.DEFAULT_LONG_RANGE
import com.movtery.zalithlauncher.setting.unit.FloatSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.LongSettingUnit
import com.movtery.zalithlauncher.setting.unit.NullableIntSettingUnit
import com.movtery.zalithlauncher.setting.unit.OffsetSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringListSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.setting.unit.enumSettingUnit
import com.movtery.zalithlauncher.setting.unit.parcelableSettingUnit

abstract class SettingsRegistry {
    protected val refreshableList = mutableListOf<AbstractSettingUnit<*>>()

    fun reloadAll() = refreshableList.forEach { it.init() }

    protected fun boolSetting(key: String, def: Boolean) =
        BooleanSettingUnit(key, def).also { refreshableList.add(it) }

    protected fun intSetting(key: String, def: Int, valueRange: IntRange = DEFAULT_INT_RANGE) =
        IntSettingUnit(key, def, valueRange).also { refreshableList.add(it) }

    protected fun intSetting(key: String, def: Int, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) =
        IntSettingUnit(key, def, min..max).also { refreshableList.add(it) }

    protected fun intSetting(key: String, def: Int?, valueRange: IntRange = DEFAULT_INT_RANGE) =
        NullableIntSettingUnit(key, def, valueRange).also { refreshableList.add(it) }

    protected fun intSetting(key: String, def: Int?, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) =
        NullableIntSettingUnit(key, def, min..max).also { refreshableList.add(it) }

    protected fun floatSetting(key: String, def: Float, valueRange: ClosedFloatingPointRange<Float> = DEFAULT_FLOAT_RANGE) =
        FloatSettingUnit(key, def, valueRange).also { refreshableList.add(it) }

    protected fun floatSetting(key: String, def: Float, min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE) =
        FloatSettingUnit(key, def, min..max).also { refreshableList.add(it) }

    protected fun longSetting(key: String, def: Long, valueRange: LongRange = DEFAULT_LONG_RANGE) =
        LongSettingUnit(key, def, valueRange).also { refreshableList.add(it) }

    protected fun longSetting(key: String, def: Long, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE) =
        LongSettingUnit(key, def, min..max).also { refreshableList.add(it) }

    protected fun offsetSetting(key: String, def: Offset) =
        OffsetSettingUnit(key, def).also { refreshableList.add(it) }

    protected fun stringSetting(key: String, def: String) =
        StringSettingUnit(key, def).also { refreshableList.add(it) }

    protected fun stringListSetting(key: String, def: List<String>) =
        StringListSettingUnit(key, def).also { refreshableList.add(it) }

    protected inline fun <reified E : Enum<E>> enumSetting(key: String, def: E) =
        enumSettingUnit(key, def).also { refreshableList.add(it) }

    protected inline fun <reified E: Parcelable> parcelableSetting(key: String, def: E) =
        parcelableSettingUnit(key, def).also { refreshableList.add(it) }
}