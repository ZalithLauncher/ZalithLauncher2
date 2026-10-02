package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CaveBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize().blur(18.dp).background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1028), Color(0xFF6B4C9A), Color(0xFFE4572E), Color(0xFF7A1E12))
                )
            )
        )
        Box(Modifier.fillMaxSize().background(Color(0x55120A16)))
        content()
    }
}
