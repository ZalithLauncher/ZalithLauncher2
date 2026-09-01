package com.zalith.launcher.multiplayer.accounts

import java.io.File

/**
 * Skins locais para contas offline: valida a assinatura PNG e
 * guarda em <diretório>/skins/<uuid>.png. A UI aplica na instância.
 */
class LocalSkinStore(storageDir: File) {

    private val dir = File(storageDir, "skins")
    private val pngMagic = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

    /** Retorna false se não for um PNG válido. */
    fun save(accountUuid: String, pngBytes: ByteArray): Boolean {
        if (pngBytes.size < 8) return false
        if (!pngBytes.copyOfRange(0, 8).contentEquals(pngMagic)) return false
        dir.mkdirs()
        File(dir, accountUuid + ".png").writeBytes(pngBytes)
        return true
    }

    fun load(accountUuid: String): ByteArray? {
        val f = File(dir, accountUuid + ".png")
        return if (f.exists()) f.readBytes() else null
    }

    fun clear(accountUuid: String) {
        File(dir, accountUuid + ".png").delete()
    }
}
