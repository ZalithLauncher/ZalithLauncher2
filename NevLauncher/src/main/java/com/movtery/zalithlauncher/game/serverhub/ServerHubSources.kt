package com.movtery.zalithlauncher.game.serverhub

import com.movtery.zalithlauncher.game.version.multiplayer.pingServer
import com.movtery.zalithlauncher.game.version.multiplayer.resolve
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.ServerAddress
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

private const val TAG = "ServerHubSources"

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

//region minecraft-serverlist.com & minecraft-mp.com (halaman HTML)

/**
 * Kedua website ini tidak menyediakan API daftar server, sehingga halaman publiknya dibaca (robots.txt mengizinkan).
 * Karena susunan HTML-nya bisa berubah, hasil bacaan hanya dipakai sebagai kandidat alamat: setiap alamat
 * diperiksa langsung ke servernya (ping) dan hanya yang benar-benar menjawab sebagai server Minecraft yang ditampilkan.
 * Jumlah pemain, versi, dan ikon diambil dari jawaban server itu sendiri.
 */
suspend fun fetchHtmlSource(source: HubSource): List<HubServer> = withContext(Dispatchers.IO) {
    val url = when (source) {
        HubSource.SERVERLIST -> "https://minecraft-serverlist.com/"
        HubSource.MINECRAFT_MP -> "https://minecraft-mp.com/"
        else -> return@withContext emptyList()
    }
    val html = GLOBAL_CLIENT.get(url).bodyAsText()
    val candidates = extractAddressCandidates(html, ownHost = url.substringAfter("://").trimEnd('/'))
        .take(MAX_CANDIDATES)

    val gate = Semaphore(CONCURRENT_PINGS)
    coroutineScope {
        candidates.map { address ->
            async {
                gate.withPermit { pingCandidate(address, source) }
            }
        }.awaitAll().filterNotNull()
    }
}

private const val MAX_CANDIDATES = 40
private const val CONCURRENT_PINGS = 8
private const val PING_TIMEOUT_MS = 4_000L

private val HOST_REGEX = Regex(
    """(?<![\w@/.-])((?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,24})(?::(\d{2,5}))?(?![\w-])""",
    RegexOption.IGNORE_CASE
)

/** Domain yang jelas bukan server Minecraft (jejaring sosial, CDN, dsb.) */
private val IGNORED_HOST_PARTS = listOf(
    "discord", "youtube", "youtu.be", "twitter", "facebook", "instagram", "tiktok", "twitch",
    "google", "gstatic", "cloudflare", "jsdelivr", "bootstrapcdn", "fontawesome", "jquery",
    "minecraft.net", "mojang", "microsoft", "github", "reddit", "patreon", "paypal", "w3.org",
    "schema.org", "apple.com", "amazon", "wikipedia", "gravatar", "recaptcha", "doubleclick",
    "minecraft-mp.com", "minecraft-serverlist.com", "minecraftserver.id", "namemc", "planetminecraft",
    "curseforge", "modrinth", "spigotmc", "papermc", "bstats", "tebex", "buycraft", "imgur", "ibb.co"
)

private val FILE_EXTENSIONS = setOf(
    "js", "css", "png", "jpg", "jpeg", "gif", "webp", "svg", "ico", "json", "xml", "html", "php",
    "woff", "woff2", "ttf", "map", "txt", "mp4", "webm"
)

internal fun extractAddressCandidates(html: String, ownHost: String): List<String> {
    //Buang skrip, gaya, dan komentar; sisakan teks yang terlihat
    val text = html
        .replace(Regex("""(?is)<(script|style|noscript)[^>]*>.*?</\1>"""), " ")
        .replace(Regex("""(?s)<!--.*?-->"""), " ")
        .replace(Regex("""<[^>]+>"""), "\n")

    val seen = LinkedHashMap<String, String>()
    for (m in HOST_REGEX.findAll(text)) {
        val host = m.groupValues[1].lowercase()
        val port = m.groupValues[2]
        val tld = host.substringAfterLast('.')
        if (tld in FILE_EXTENSIONS) continue
        if (host.contains(ownHost.lowercase())) continue
        if (IGNORED_HOST_PARTS.any { host.contains(it) }) continue
        if (port.isNotEmpty() && port.toIntOrNull()?.let { it in 1..65535 } != true) continue
        val address = if (port.isNotEmpty() && port != "25565") "$host:$port" else host
        seen.putIfAbsent(normalizeAddress(address), address)
    }
    return seen.values.toList()
}

private suspend fun pingCandidate(address: String, source: HubSource): HubServer? {
    val result = runCatching {
        pingServer(ServerAddress.parse(address).resolve(), timeoutMillis = PING_TIMEOUT_MS)
    }.onFailure {
        Logger.debug(TAG, "Not a Minecraft server: $address")
    }.getOrNull() ?: return null

    return HubServer(
        id = "${source.name.lowercase()}:${normalizeAddress(address)}",
        name = nameFromAddress(address),
        address = address,
        source = source,
        isIndonesia = false,
        online = true,
        players = result.status.players.online,
        maxPlayers = result.status.players.max,
        version = result.status.version.name,
        edition = "Java",
        iconBytes = result.status.favicon?.icon,
        detailUrl = source.url
    )
}

/** Nama sementara dari alamat: "play.mosscraft.net" -> "Mosscraft" */
internal fun nameFromAddress(address: String): String {
    val host = address.substringBefore(':')
    val parts = host.split('.').filter { it.isNotEmpty() }
    val core = if (parts.size >= 2) parts[parts.size - 2] else host
    return core.replaceFirstChar { it.uppercase() }
}

//endregion

/**
 * Periksa server buatan pengguna langsung ke servernya
 */
suspend fun pingHubAddress(address: String): HubServer? = withContext(Dispatchers.IO) {
    runCatching {
        pingServer(ServerAddress.parse(address).resolve(), timeoutMillis = 8_000L)
    }.getOrNull()?.let { r ->
        HubServer(
            id = "custom:${normalizeAddress(address)}",
            name = "",
            address = address,
            source = HubSource.CUSTOM,
            isIndonesia = false,
            online = true,
            players = r.status.players.online,
            maxPlayers = r.status.players.max,
            version = r.status.version.name,
            edition = "Java",
            iconBytes = r.status.favicon?.icon
        )
    }
}
