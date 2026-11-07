/*
 * Nev Launcher
 * Perpustakaan cape (jubah) lokal untuk lemari pakaian, oleh NestMC.
 *
 * Cape disimpan sebagai file PNG di folder cape akun dengan nama "local_<nama>.png",
 * sehingga bisa dipakai ulang oleh daftar cape yang sudah ada (pratinjau dan pemilih).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.account.wardrobe

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile
import com.movtery.zalithlauncher.path.PathManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object CapeLibrary {
    /** Awalan id/nama file untuk cape lokal agar tidak bentrok dengan cache cape Microsoft */
    const val ID_PREFIX = "local_"

    /** Folder assets tempat cape bawaan (PNG) bisa ditaruh dan otomatis masuk perpustakaan */
    private const val ASSET_DIR = "capes"

    private const val MAX_BYTES = 2L * 1024 * 1024
    private const val MAX_NAME_LENGTH = 40
    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    private val libraryDir: File get() = PathManager.DIR_ACCOUNT_CAPE

    /**
     * Daftar cape lokal, diurutkan berdasarkan nama.
     */
    fun list(): List<PlayerProfile.Cape> {
        val files = libraryDir.listFiles { file ->
            file.isFile && file.name.startsWith(ID_PREFIX) && file.name.endsWith(".png")
        } ?: return emptyList()

        return files
            .map { file ->
                val id = file.name.removeSuffix(".png")
                PlayerProfile.Cape(
                    id = id,
                    state = "INACTIVE",
                    url = "",
                    alias = id.removePrefix(ID_PREFIX).replace('_', ' ')
                )
            }
            .sortedBy { it.alias.lowercase() }
    }

    /**
     * Cari cape di perpustakaan yang isinya sama dengan file cape akun.
     * Dipakai untuk menandai cape yang sedang dipakai.
     */
    fun findMatching(accountCape: File): PlayerProfile.Cape? {
        if (!accountCape.exists()) return null
        val bytes = runCatching { accountCape.readBytes() }.getOrNull() ?: return null
        return list().firstOrNull { cape ->
            val file = File(libraryDir, "${cape.id}.png")
            file.length() == bytes.size.toLong() &&
                    runCatching { file.readBytes().contentEquals(bytes) }.getOrDefault(false)
        }
    }

    /**
     * Salin cape bawaan dari assets/capes ke perpustakaan (hanya yang belum ada).
     */
    suspend fun seedFromAssets(context: Context) = withContext(Dispatchers.IO) {
        val names = runCatching { context.assets.list(ASSET_DIR) }.getOrNull().orEmpty()
        names.filter { it.endsWith(".png", ignoreCase = true) }.forEach { assetName ->
            runCatching {
                val bytes = context.assets.open("$ASSET_DIR/$assetName").use { it.readBytes() }
                saveBytes(assetName.substringBeforeLast('.'), bytes, skipIfNameExists = true)
            }
        }
    }

    /**
     * Impor satu file cape dari pemilih file.
     * @return cape yang tersimpan, atau gagal bila bukan gambar cape yang valid
     */
    suspend fun importFromUri(context: Context, uri: Uri): Result<PlayerProfile.Cape> =
        withContext(Dispatchers.IO) {
            runCatching {
                val displayName = context.contentResolver
                    .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                    ?.substringBeforeLast('.')
                    ?: "cape"

                val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(MAX_BYTES.toInt() + 1)
                    var total = 0
                    while (total < buffer.size) {
                        val read = input.read(buffer, total, buffer.size - total)
                        if (read < 0) break
                        total += read
                    }
                    if (total > MAX_BYTES) throw IllegalArgumentException("Cape file is too large")
                    buffer.copyOf(total)
                } ?: throw IllegalArgumentException("Unable to open cape file")

                saveBytes(displayName, bytes, skipIfNameExists = false)
            }
        }

    /**
     * Simpan bytes PNG sebagai cape lokal. Bila isi yang sama sudah ada, cape lama dipakai.
     */
    private fun saveBytes(
        rawName: String,
        bytes: ByteArray,
        skipIfNameExists: Boolean
    ): PlayerProfile.Cape {
        require(isValidCape(bytes)) { "Not a valid cape image" }

        libraryDir.mkdirs()

        val baseName = sanitizeName(rawName)

        // Isi sama persis sudah ada: tidak perlu duplikat
        list().firstOrNull { cape ->
            val file = File(libraryDir, "${cape.id}.png")
            file.length() == bytes.size.toLong() &&
                    runCatching { file.readBytes().contentEquals(bytes) }.getOrDefault(false)
        }?.let { return it }

        var id = "$ID_PREFIX$baseName"
        var file = File(libraryDir, "$id.png")
        if (file.exists()) {
            if (skipIfNameExists) {
                // Nama sama dari assets dan isinya berbeda: anggap sudah pernah diimpor
                return PlayerProfile.Cape(id, "INACTIVE", "", baseName.replace('_', ' '))
            }
            var index = 2
            while (file.exists()) {
                id = "$ID_PREFIX${baseName}_$index"
                file = File(libraryDir, "$id.png")
                index++
            }
        }

        file.writeBytes(bytes)
        return PlayerProfile.Cape(
            id = id,
            state = "INACTIVE",
            url = "",
            alias = id.removePrefix(ID_PREFIX).replace('_', ' ')
        )
    }

    private fun sanitizeName(raw: String): String {
        val cleaned = raw
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .trim()
            .replace(Regex("\\s+"), "_")
            .take(MAX_NAME_LENGTH)
        return cleaned.ifEmpty { "cape" }
    }

    /**
     * Cape valid: PNG, ukuran 64x32 atau 22x17 beserta kelipatannya (cape HD).
     */
    private fun isValidCape(bytes: ByteArray): Boolean {
        if (bytes.size < PNG_SIGNATURE.size) return false
        for (i in PNG_SIGNATURE.indices) {
            if (bytes[i] != PNG_SIGNATURE[i]) return false
        }

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0 || width > 2048) return false

        val isStandard = width == height * 2
        val isLegacy = width * 17 == height * 22
        return isStandard || isLegacy
    }
}
