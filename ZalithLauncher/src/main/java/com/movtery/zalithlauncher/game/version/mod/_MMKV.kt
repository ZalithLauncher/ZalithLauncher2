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

package com.movtery.zalithlauncher.game.version.mod

import com.tencent.mmkv.MMKV

/**
 * 模组项目缓存 MMKV，文件 HASH 值对应项目
 */
fun modProjectCache(): MMKV = MMKV.mmkvWithID("ModProjectHashMapper", MMKV.MULTI_PROCESS_MODE)
/**
 * 模组版本文件缓存 MMKV，文件 HASH 值对应平台文件
 */
fun modFileCache(): MMKV = MMKV.mmkvWithID("ModFileHashMapper", MMKV.MULTI_PROCESS_MODE)
/**
 * 本地模组安装信息缓存 MMKV，平台:指纹 值对应安装信息
 */
fun installedModCache(): MMKV = MMKV.mmkvWithID("InstalledModHashMapper", MMKV.MULTI_PROCESS_MODE)
