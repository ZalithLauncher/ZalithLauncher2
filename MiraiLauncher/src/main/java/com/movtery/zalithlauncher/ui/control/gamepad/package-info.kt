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

@file:Suppress("unused")

/**
 * 部分实现思路参考自 [G-Mapper for Android](https://github.com/Mathias-Boulay/android_gamepad_remapper/)，
 * 该项目采用 GNU Lesser General Public License v3.0（LGPL-3.0）授权。
 *
 * 本项目基于 GNU General Public License v3.0（GPL-3.0）发布。
 *
 * 说明：
 * - 启动器使用 Jetpack Compose 构建 UI，无法直接集成原库，
 *   因此参考其核心逻辑并以 Kotlin 与 Compose 风格重新实现。
 * - 原项目的部分算法与结构在保留设计意图的前提下进行了简化与重构。
 * - 本项目未直接包含或链接原项目的源代码。
 */

package com.movtery.zalithlauncher.ui.control.gamepad
