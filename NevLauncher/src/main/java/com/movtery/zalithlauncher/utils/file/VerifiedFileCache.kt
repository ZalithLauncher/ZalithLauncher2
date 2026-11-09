package com.movtery.zalithlauncher.utils.file

import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache hasil verifikasi SHA-1 file game.
 *
 * Tanpa cache, setiap kali menekan Play launcher menghitung ulang SHA-1 ribuan file
 * (aset, library) sebelum game dijalankan, dan itu memperlambat waktu menuju logo Mojang.
 * File yang ukuran dan waktu modifikasinya sama dengan saat terakhir lolos verifikasi
 * dianggap masih valid, jadi tidak perlu di-hash lagi. Jika file berubah (diperbarui,
 * rusak, atau diganti), ukuran/waktunya berbeda dan file akan diverifikasi penuh lagi.
 */
object VerifiedFileCache {
    private const val TAG = "VerifiedFileCache"

    private class Entry(val size: Long, val modified: Long, val sha1: String)

    private val entries = ConcurrentHashMap<String, Entry>()
    private val lock = Any()

    @Volatile
    private var loaded = false

    @Volatile
    private var dirty = false

    private fun cacheFile() = File(PathManager.DIR_FILES_PRIVATE, "verified_files.cache")

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(lock) {
            if (loaded) return
            runCatching {
                val file = cacheFile()
                if (file.isFile) {
                    file.useLines { lines ->
                        lines.forEach { line ->
                            val parts = line.split('\t', limit = 4)
                            if (parts.size == 4) {
                                val size = parts[0].toLongOrNull() ?: return@forEach
                                val modified = parts[1].toLongOrNull() ?: return@forEach
                                entries[parts[3]] = Entry(size, modified, parts[2])
                            }
                        }
                    }
                }
            }.onFailure {
                Logger.warning(TAG, "Failed to load verified file cache, starting empty", it)
                entries.clear()
            }
            loaded = true
        }
    }

    /** true bila file ini sudah pernah lolos verifikasi [sha1] dan tidak berubah sejak itu */
    fun isVerified(file: File, sha1: String): Boolean {
        ensureLoaded()
        val entry = entries[file.absolutePath] ?: return false
        if (!entry.sha1.equals(sha1, ignoreCase = true)) return false
        return entry.size == file.length() && entry.modified == file.lastModified()
    }

    /** Catat bahwa [file] baru saja lolos verifikasi [sha1] */
    fun markVerified(file: File, sha1: String) {
        ensureLoaded()
        val size = file.length()
        val modified = file.lastModified()
        if (!file.exists()) return
        entries[file.absolutePath] = Entry(size, modified, sha1.lowercase())
        dirty = true
    }

    fun forget(file: File) {
        ensureLoaded()
        if (entries.remove(file.absolutePath) != null) dirty = true
    }

    /** Simpan ke disk bila ada perubahan. Gagal menyimpan tidak berdampak apa pun selain verifikasi ulang. */
    fun saveIfDirty() {
        if (!loaded || !dirty) return
        synchronized(lock) {
            if (!dirty) return
            runCatching {
                val target = cacheFile()
                val temp = File(target.parentFile, target.name + ".tmp")
                temp.bufferedWriter().use { writer ->
                    entries.forEach { (path, entry) ->
                        writer.write("${entry.size}\t${entry.modified}\t${entry.sha1}\t$path")
                        writer.newLine()
                    }
                }
                if (!temp.renameTo(target)) {
                    target.delete()
                    temp.renameTo(target)
                }
                dirty = false
            }.onFailure {
                Logger.warning(TAG, "Failed to save verified file cache", it)
            }
        }
    }
}
