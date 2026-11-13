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

package com.movtery.zalithlauncher.sponsor

import com.movtery.zalithlauncher.setting.launcherMMKV

object SponsorPrefs {
    private val mmkv get() = launcherMMKV()

    /**
     * 上次已展示结算报告的上月赞助者月份标识
     */
    var lastReportMonth: String
        get() = mmkv.getString("lastSponsorReportMonth", "") ?: ""
        set(value) {
            mmkv.putString("lastSponsorReportMonth", value)
        }
}
