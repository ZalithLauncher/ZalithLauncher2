package com.movtery.zalithlauncher.game.versioninfo

import com.google.gson.JsonParser
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Daftar gambar sampul per versi Minecraft Java (versi -> URL gambar).
 * Sumber: feed catatan rilis yang juga dipakai launcher resmi. Gambar tidak dibundel ke APK,
 * hanya ditampilkan dari server asalnya (dengan cache Coil); bila offline kartu memakai gambar bawaan.
 */
object VersionCoverRepository {
    private const val TAG = "VersionCover"
    private const val BASE_URL = "https://launchercontent.mojang.com"

    private val _covers = MutableStateFlow<Map<String, String>>(emptyMap())
    val covers: StateFlow<Map<String, String>> = _covers

    private val mutex = Mutex()
    private var loaded = false

    /** Muat daftar sekali per sesi aplikasi; bila gagal (mis. offline) akan dicoba lagi saat dipanggil berikutnya. */
    suspend fun ensureLoaded() {
        if (loaded) return
        mutex.withLock {
            if (loaded) return
            runCatching {
                withContext(Dispatchers.IO) {
                    val text = GLOBAL_CLIENT.get("$BASE_URL/v2/javaPatchNotes.json").bodyAsText()
                    val entries = JsonParser.parseString(text).asJsonObject.getAsJsonArray("entries")
                    val map = HashMap<String, String>()
                    entries.forEach { element ->
                        val obj = element.asJsonObject
                        val version = obj.get("version")?.takeIf { !it.isJsonNull }?.asString
                        val path = obj.getAsJsonObject("image")?.get("url")?.takeIf { !it.isJsonNull }?.asString
                        if (version != null && path != null) {
                            map[version] = if (path.startsWith("http")) path else BASE_URL + path
                        }
                    }
                    map
                }
            }.onSuccess {
                _covers.value = it
                loaded = true
            }.onFailure {
                Logger.warning(TAG, "Failed to load version covers", it)
            }
        }
    }
}
