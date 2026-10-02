package com.movtery.zalithlauncher.ui.screens.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

fun wallpaperFile(context: android.content.Context) = File(context.filesDir, "mirai-wallpaper.jpg")

private val bundled = listOf("wall_dusk", "wall_blossom", "wall_lake")

@Composable
fun WallpaperPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input -> wallpaperFile(context).writeBytes(input.readBytes()) }
            revision++
        }
    }
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Wallpaper", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Import from storage") }
        bundled.forEach { name ->
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            if (id != 0) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(context.resources.getIdentifier(name, "drawable", context.packageName).let { if (it != 0) it else id }),
                    contentDescription = name,
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).clickable {
                        context.resources.openRawResource(id).use { wallpaperFile(context).writeBytes(it.readBytes()) }
                        revision++
                    },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(if (wallpaperFile(context).exists()) "Applied. Go home to see it." else "Pick one of the three, or import your own.", color = Color(0xFFD7CFC8))
        if (revision < 0) Text("")
    }
}
