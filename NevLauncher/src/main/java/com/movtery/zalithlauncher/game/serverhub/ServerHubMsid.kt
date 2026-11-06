package com.movtery.zalithlauncher.game.serverhub

import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.utils.network.ServerAddress
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

private const val MSID_API = "https://api.minecraftserver.id/api/v1/public"
private const val MSID_PAGE_SIZE = 50

//region minecraftserver.id (API publik resmi)

@Serializable
private data class MsidEnvelope(
    val success: Boolean = false,
    val data: MsidList? = null
)

@Serializable
private data class MsidList(
    val servers: List<MsidServer> = emptyList(),
    val totalPages: Int = 1
)

@Serializable
private data class MsidServer(
    val slug: String = "",
    val name: String = "",
    val address: String = "",
    val port: Int? = null,
    val edition: String? = null,
    val status: String? = null,
    val currentPlayers: Int = 0,
    val maxPlayers: Int = 0,
    val version: String? = null,
    val iconUrl: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
private data class MsidTagEnvelope(
    val data: MsidTags? = null
)

@Serializable
private data class MsidTags(
    val tags: List<String> = emptyList()
)

/**
 * Ambil server dari API publik minecraftserver.id (gratis, tanpa kunci, batas 60 permintaan/menit).
 * Server khusus Bedrock dilewati karena launcher ini hanya memainkan Minecraft Java.
 *
 * @param query kata pencarian (min. 2 huruf), null untuk daftar teratas
 * @param tag mode permainan untuk disaring oleh server, null untuk semua
 * @param pages jumlah halaman (50 server per halaman) yang dimuat
 */
suspend fun fetchMsidServers(
    query: String? = null,
    tag: String? = null,
    pages: Int = 3
): List<HubServer> = withContext(Dispatchers.IO) {
    val result = ArrayList<HubServer>()
    var page = 1
    while (page <= pages) {
        val text = GLOBAL_CLIENT.get("$MSID_API/servers") {
            parameter("page", page)
            parameter("limit", MSID_PAGE_SIZE)
            parameter("sort", "players")
            if (!query.isNullOrBlank()) parameter("q", query.trim())
            if (!tag.isNullOrBlank()) parameter("tag", tag)
        }.bodyAsText()

        val envelope = GLOBAL_JSON.decodeFromString(MsidEnvelope.serializer(), text)
        val data = envelope.data ?: break
        data.servers.forEach { s ->
            if (s.slug.isBlank() || s.address.isBlank()) return@forEach
            if (s.edition.equals("BEDROCK", ignoreCase = true)) return@forEach
            val join = if (s.port != null && s.port != ServerAddress.DEFAULT_PORT) {
                "${s.address}:${s.port}"
            } else s.address
            result += HubServer(
                id = "msid:${s.slug}",
                name = s.name.ifBlank { s.slug },
                address = join,
                source = HubSource.MSID,
                isIndonesia = true,
                online = s.status?.equals("ONLINE", ignoreCase = true),
                players = s.currentPlayers,
                maxPlayers = s.maxPlayers,
                version = s.version,
                edition = when (s.edition?.uppercase()) {
                    "CROSS_PLAY" -> "Java & Bedrock"
                    else -> "Java"
                },
                tags = s.tags,
                iconUrl = s.iconUrl,
                detailUrl = "https://minecraftserver.id/id/server/${s.slug}"
            )
        }
        if (page >= data.totalPages) break
        page++
    }
    result
}

/** Daftar tag mode permainan resmi dari minecraftserver.id */
suspend fun fetchMsidTags(): List<String> = withContext(Dispatchers.IO) {
    val text = GLOBAL_CLIENT.get("$MSID_API/tags").bodyAsText()
    GLOBAL_JSON.decodeFromString(MsidTagEnvelope.serializer(), text).data?.tags.orEmpty()
}

//endregion
