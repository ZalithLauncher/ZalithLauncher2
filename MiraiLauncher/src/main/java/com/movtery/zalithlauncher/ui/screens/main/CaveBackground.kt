package com.movtery.zalithlauncher.ui.screens.main

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Key of the currently cached [CaveBackground] bitmap. */
private var caveWallpaperCacheKey: String? = null
/** Process-wide cached wallpaper bitmap: decoded once off Main, shared by every screen. */
private var caveWallpaperCacheBitmap: ImageBitmap? = null

@Composable
fun CaveBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val file = wallpaperFile(context)
    val revision = wallpaperRevision
    //Decode off Main and cache process-wide: the old remember{} decoded the full
    //JPEG on the UI thread on every visit, freezing tab switches for hundreds of ms.
    val cacheKey = remember(revision, file.exists(), file.lastModified()) {
        "$revision|${file.exists()}|${file.lastModified()}"
    }
    var image by remember(cacheKey) { mutableStateOf(caveWallpaperCacheBitmap.takeIf { caveWallpaperCacheKey == cacheKey }) }
    LaunchedEffect(cacheKey) {
        if (caveWallpaperCacheKey == cacheKey && caveWallpaperCacheBitmap != null) {
            image = caveWallpaperCacheBitmap
        } else {
            val decoded = withContext(Dispatchers.IO) {
                val bytes = if (file.exists()) file.readBytes() else runCatching {
                    context.assets.open("wallpapers/wp_01_lush_caves.jpg").readBytes()
                }.getOrNull()
                bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
            }
            caveWallpaperCacheKey = cacheKey
            caveWallpaperCacheBitmap = decoded
            image = decoded
        }
    }
    Box(modifier.fillMaxSize()) {
        val bitmap = image
        if (bitmap != null) Image(bitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF16181C), Color(0xFF21242B)))))
        content()
    }
}
