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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun CaveBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val file = wallpaperFile(context)
    val image = remember(file.exists(), file.lastModified()) {
        val bytes = if (file.exists()) file.readBytes() else runCatching {
            val encoded = listOf("cave-a.b64", "cave-b.b64", "cave-c.b64").joinToString("") {
                context.assets.open("wallpapers/$it").bufferedReader().readText()
            }
            Base64.decode(encoded.replace("\n", ""), Base64.DEFAULT)
        }.getOrNull()
        bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }
    Box(modifier.fillMaxSize()) {
        if (image != null) Image(image, contentDescription = null, modifier = Modifier.fillMaxSize().blur(8.dp), contentScale = ContentScale.Crop)
        else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A1248), Color(0xFFE26A8A), Color(0xFF5A2A78)))))
        Box(Modifier.fillMaxSize().background(Color(0x33120A16)))
        content()
    }
}
