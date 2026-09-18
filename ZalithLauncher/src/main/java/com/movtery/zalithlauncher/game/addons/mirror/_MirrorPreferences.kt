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

package com.movtery.zalithlauncher.game.addons.mirror

import com.movtery.zalithlauncher.setting.enums.MirrorSourceType

/** AUTO 折算后的生效初始排序方向 */
enum class MirrorPriority {
    OFFICIAL_FIRST,
    MIRROR_FIRST
}

/**
 * 把三档设置折算成初始顺序：自动档依据是否中国大陆静态判定，
 * 失败换源由下载引擎在运行时自适应，无需网络探测。
 */
fun resolveMirrorPriority(source: MirrorSourceType, mainland: Boolean): MirrorPriority =
    when {
        source == MirrorSourceType.OFFICIAL_FIRST -> MirrorPriority.OFFICIAL_FIRST
        source == MirrorSourceType.MIRROR_FIRST -> MirrorPriority.MIRROR_FIRST
        mainland -> MirrorPriority.MIRROR_FIRST
        else -> MirrorPriority.OFFICIAL_FIRST
    }

/** 按生效偏好产出有序候选列表；镜像链接不存在时仅保留官方源 */
fun orderCandidates(official: String, mirror: String?, priority: MirrorPriority): List<String> =
    when (priority) {
        MirrorPriority.OFFICIAL_FIRST -> listOfNotNull(official, mirror)
        MirrorPriority.MIRROR_FIRST -> listOfNotNull(mirror, official)
    }
