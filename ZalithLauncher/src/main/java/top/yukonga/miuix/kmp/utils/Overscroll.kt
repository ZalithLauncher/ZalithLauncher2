// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tracks whether overscroll is active.
 */
class OverScrollState {
    var isOverScrollActive by mutableStateOf(false)
        internal set
}

/**
 * Shared overscroll state for scroll and pull-to-refresh coordination.
 */
val LocalOverScrollState = compositionLocalOf { OverScrollState() }