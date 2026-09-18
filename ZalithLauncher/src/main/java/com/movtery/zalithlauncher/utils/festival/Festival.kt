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

package com.movtery.zalithlauncher.utils.festival

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.utils.checkDate
import com.movtery.zalithlauncher.utils.checkDateRange
import com.movtery.zalithlauncher.utils.festival.Festival.entries
import com.xhinliang.lunarcalendar.LunarCalendar
import java.time.LocalDate

enum class Festival(
    val isChinese: Boolean,
    val isInternational: Boolean,
    @field:StringRes
    val textRes: Int
) {
    /**
     * 新年
     */
    NEW_YEAR(
        isChinese = true,
        isInternational = true,
        textRes = R.string.festivals_new_year
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return date.checkDate(1, 1)
        }
    },

    /**
     * 春节
     */
    SPRING_FESTIVAL(
        isChinese = true,
        isInternational = false,
        textRes = R.string.festivals_spring
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return lunarCalendar.checkDate(1, 1)
        }
    },

    /**
     * 清明节
     */
    QING_MING(
        isChinese = true,
        isInternational = false,
        textRes = R.string.festivals_qing_ming
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return date.checkDateRange(4, 4..6)
        }
    },

    /**
     * 端午节
     */
    DRAGON_BOAT(
        isChinese = true,
        isInternational = false,
        textRes = R.string.festivals_dragon_boat
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return lunarCalendar.checkDate(5, 5)
        }
    },

    /**
     * 中秋节
     */
    MID_AUTUMN(
        isChinese = true,
        isInternational = false,
        textRes = R.string.festivals_mid_autumn
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return lunarCalendar.checkDate(8, 15)
        }
    },

    /**
     * 国庆节
     */
    NATIONAL_DAY(
        isChinese = true,
        isInternational = false,
        textRes = R.string.festivals_national_day
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return date.checkDateRange(10, 1..7)
        }
    },

    /**
     * 愚人节
     */
    APRIL_FOOLS(
        isChinese = false,
        isInternational = true,
        textRes = R.string.festivals_april_fools
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return date.checkDate(4, 1)
        }
    },

    /**
     * 圣诞节
     */
    CHRISTMAS(
        isChinese = false,
        isInternational = true,
        textRes = R.string.festivals_christmas
    ) {
        override fun isToday(
            date: LocalDate,
            lunarCalendar: LunarCalendar
        ): Boolean {
            return date.checkDateRange(12, 24..25)
        }
    };

    abstract fun isToday(date: LocalDate, lunarCalendar: LunarCalendar): Boolean
}


/**
 * 获取今天的节日列表
 * @param containsChinese 是否包含中国节日
 */
fun getTodayFestivals(
    date: LocalDate = LocalDate.now(),
    containsChinese: Boolean
): List<Festival> {
    val lunarCalendar = LunarCalendar.obtainCalendar(date.year, date.monthValue, date.dayOfMonth)
    return entries.filter { festival ->
        val type = if (containsChinese) {
            festival.isChinese || festival.isInternational
        } else {
            festival.isInternational
        }
        type && festival.isToday(date, lunarCalendar)
    }
}