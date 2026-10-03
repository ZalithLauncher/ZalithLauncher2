package com.movtery.zalithlauncher.ui.screens.main

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

fun wallpaperFile(context: android.content.Context) = File(context.filesDir, "mirai-wallpaper.jpg")

var wallpaperRevision by mutableIntStateOf(0)

private val bundled = listOf(
    "dusk", "blossom", "lake", "peaks", "islands", "portal",
    "cave", "autumn", "ridge", "ocean", "title", "swamp"
)

@Composable
fun WallpaperPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input -> wallpaperFile(context).writeBytes(input.readBytes()) }
            wallpaperRevision++
        }
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Wallpaper", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Import from storage") }
        bundled.forEach { name ->
            val encoded = runCatching { context.assets.open("wallpapers/$name.b64").bufferedReader().readText() }.getOrNull()
            val image = encoded?.let {
                val bytes = Base64.decode(it.replace("\n", ""), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }
            if (image != null && encoded != null) {
                Image(
                    bitmap = image,
                    contentDescription = name,
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).clickable {
                        wallpaperFile(context).writeBytes(Base64.decode(encoded.replace("\n", ""), Base64.DEFAULT))
                        wallpaperRevision++
                    },
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(name.replaceFirstChar { it.uppercase() }, color = Color.White, modifier = Modifier.clickable {
                    encoded?.let { wallpaperFile(context).writeBytes(Base64.decode(it.replace("\n", ""), Base64.DEFAULT)); wallpaperRevision++ }
                })
            }
        }
        Text(if (wallpaperRevision >= 0 && wallpaperFile(context).exists()) "Applied." else "Pick one, or import your own.", color = Color(0xFFD7CFC8))
    }
}
