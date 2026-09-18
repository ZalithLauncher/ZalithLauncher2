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

import com.movtery.zalithlauncher.setting.launcherMMKV

/**
 * 枚举设置单元，将枚举保存到设置配置文件中
 */
class EnumSettingUnit<E : Enum<E>>(
    key: String,
    defaultValue: E,
    private val getEnum: (String) -> E?
) : AbstractSettingUnit<E>(key, defaultValue) {
    override fun getValue(): E {
        val valueString: String = launcherMMKV().getString(key, defaultValue.name)!!
        return (getEnum(valueString) ?: defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: E): E {
        launcherMMKV().putString(key, v.name).apply()
        return v
    }
}

inline fun <reified E : Enum<E>> enumSettingUnit(
    key: String,
    defaultValue: E
): EnumSettingUnit<E> {
    return EnumSettingUnit(
        key = key,
        defaultValue = defaultValue,
        getEnum = { raw -> enumValues<E>().firstOrNull { it.name == raw } }
    )
}
