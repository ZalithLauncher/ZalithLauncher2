package com.zalith.launcher.multiplayer.accounts

import java.util.UUID

/**
 * UUID offline oficial do Minecraft.
 *
 * O servidor vanilla em modo offline deriva o UUID assim:
 * versão 3 (MD5) dos bytes de "OfflinePlayer:<nome>".
 * Usar a mesma regra garante que whitelist, permissões e skins
 * calculadas por plugins casem com o UUID do launcher.
 */
object OfflineUuid {

    fun fromUsername(username: String): UUID =
        UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).toByteArray(Charsets.UTF_8))
}
