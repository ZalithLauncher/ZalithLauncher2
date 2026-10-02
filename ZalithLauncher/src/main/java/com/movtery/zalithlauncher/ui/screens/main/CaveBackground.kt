package com.movtery.zalithlauncher.ui.screens.main

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

private const val CAVE = """PLACEHOLDER"""

@Composable
fun CaveBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val image = remember {
        val bytes = Base64.decode(CAVE, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }
    Box(modifier.fillMaxSize()) {
        Image(bitmap = image, contentDescription = null, modifier = Modifier.fillMaxSize().blur(18.dp), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Color(0x55120A16)))
        content()
    }
}
