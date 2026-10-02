package com.movtery.zalithlauncher.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
fun LauncherBack(onDashboard: () -> Unit) {
    BackHandler(enabled = true, onBack = onDashboard)
}
