package com.movtery.zalithlauncher.game.serverhub

import com.movtery.zalithlauncher.game.version.multiplayer.pingServer
import com.movtery.zalithlauncher.game.version.multiplayer.resolve
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.ServerAddress
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

private const val TAG = "ServerHubSources"

/*
 * Sumber HTML: minecraftservers.org dan minecraft-mp.com (minecraftserver.id memakai API resmi di ServerHubMsid.kt).
 * minecraft.buzz sengaja TIDAK dibaca: ketentuan layanannya melarang spider/crawl/scrape.
 *
 * Keduanya tidak menyediakan API daftar server, jadi halaman publiknya dibaca (robots.txt mengizinkan halaman daftar).
 * Karena susunan HTML bisa berubah, hasil bacaan hanya dipakai sebagai kandidat alamat: setiap alamat diperiksa
 * langsung ke servernya (ping) dan hanya yang benar-benar menjawab sebagai server Minecraft yang ditampilkan.
 * Jumlah pemain, versi, dan ikon diambil dari jawaban server itu sendiri.
 */

private const val MAX_CANDIDATES_PER_PAGE = 30
private const val CONCURRENT_PINGS = 16
private const val PING_TIMEOUT_MS = 3_000L

/** Satu halaman daftar yang dibaca: [url], serta penanda lokasi/mode untuk server di dalamnya */
private data class ListPage(
    val url: String,
    val indonesia: Boolean = false,
    val tag: String? = null
)

/** Halaman daftar yang dibaca per sumber (teratas dan, bila ada, halaman Indonesia) */
private fun defaultPages(source: HubSource): List<ListPage> = when (source) {
    HubSource.MINECRAFTSERVERS_ORG -> listOf(
        ListPage("https://minecraftservers.org/"),
        ListPage("https://minecraftservers.org/index/2")
    )
    HubSource.MINECRAFT_MP -> listOf(
        ListPage("https://minecraft-mp.com/")
    )
    //MSID memakai API resmi (ServerHubMsid.kt), CUSTOM tidak dibaca dari website
    HubSource.MSID, HubSource.CUSTOM -> emptyList()
}

/** Halaman mode permainan (mis. Survival, SkyBlock) per sumber; sumber tanpa pola yang dikenal dilewati */
private fun tagPages(source: HubSource, tag: String): List<ListPage> {
    val slug = normalizeTag(tag)
    return when (source) {
        HubSource.MINECRAFTSERVERS_ORG -> listOf(ListPage("https://minecraftservers.org/type/$slug", tag = tag))
        else -> emptyList()
    }
}

/**
 * Baca daftar server dari [source]
 *
 * @param tag mode permainan; bila diisi, halaman kategori yang dibaca, bukan daftar teratas
 * @param onProgress dipanggil setiap satu halaman selesai diperiksa, berisi seluruh hasil sejauh ini
 */
suspend fun fetchHubSource(
    source: HubSource,
    tag: String? = null,
    onProgress: suspend (List<HubServer>) -> Unit
) = withContext(Dispatchers.IO) {
    val pages = if (tag == null) defaultPages(source) else tagPages(source, tag)
    val found = LinkedHashMap<String, HubServer>()
    var lastError: Throwable? = null

    for (page in pages) {
        val html = try {
            GLOBAL_CLIENT.get(page.url).bodyAsText()
        } catch (e: Throwable) {
            Logger.warning(TAG, "Failed to read ${page.url}", e)
            lastError = e
            continue
        }
        val ownHost = page.url.substringAfter("://").substringBefore('/')
        val candidates = extractAddressCandidates(html, ownHost)
            .filter { normalizeAddress(it) !in found }
            .take(MAX_CANDIDATES_PER_PAGE)

        val gate = Semaphore(CONCURRENT_PINGS)
        val pinged = coroutineScope {
            candidates.map { address ->
                async { gate.withPermit { pingCandidate(address, source, page) } }
            }.awaitAll().filterNotNull()
        }
        pinged.forEach { found.putIfAbsent(normalizeAddress(it.address), it) }
        onProgress(found.values.toList())
    }

    //Semua halaman gagal dibaca: laporkan sebagai kegagalan sumber
    if (found.isEmpty() && lastError != null) throw lastError
}

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
    "minecraft-mp.com", "minecraftservers.org", "minecraft.buzz", "namemc", "planetminecraft",
    "curseforge", "modrinth", "spigotmc", "papermc", "bstats", "tebex", "buycraft", "imgur", "ibb.co"
)

private val FILE_EXTENSIONS = setOf(
    "js", "css", "png", "jpg", "jpeg", "gif", "webp", "svg", "ico", "json", "xml", "html", "php",
    "woff", "woff2", "ttf", "map", "txt", "mp4", "webm"
)

/** Akhiran domain milik Indonesia */
private val INDONESIA_SUFFIXES = listOf(".id", ".co.id", ".my.id", ".biz.id", ".web.id", ".sch.id", ".or.id", ".net.id")

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

private suspend fun pingCandidate(address: String, source: HubSource, page: ListPage): HubServer? {
    val result = runCatching {
        pingServer(ServerAddress.parse(address).resolve(), timeoutMillis = PING_TIMEOUT_MS)
    }.onFailure {
        Logger.debug(TAG, "Not a Minecraft server: $address")
    }.getOrNull() ?: return null

    val host = address.substringBefore(':')
    return HubServer(
        id = "${source.name.lowercase()}:${normalizeAddress(address)}",
        name = nameFromAddress(address),
        address = address,
        source = source,
        isIndonesia = page.indonesia || INDONESIA_SUFFIXES.any { host.endsWith(it) },
        online = true,
        players = result.status.players.online,
        maxPlayers = result.status.players.max,
        version = result.status.version.name,
        edition = "Java",
        tags = listOfNotNull(page.tag),
        iconBytes = result.status.favicon?.icon,
        detailUrl = source.url
    )
}

private val SECOND_LEVEL_LABELS = setOf("co", "my", "biz", "web", "net", "or", "ac", "sch", "com", "org")

/** Nama sementara dari alamat: "play.mosscraft.net" -> "Mosscraft", "mc.nama.my.id" -> "Nama" */
internal fun nameFromAddress(address: String): String {
    val host = address.substringBefore(':')
    val parts = host.split('.').filter { it.isNotEmpty() }
    val core = when {
        parts.size >= 3 && parts[parts.size - 2] in SECOND_LEVEL_LABELS -> parts[parts.size - 3]
        parts.size >= 2 -> parts[parts.size - 2]
        else -> host
    }
    return core.replace('-', ' ').replace('_', ' ')
        .split(' ').filter { it.isNotEmpty() }
        .joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
        .ifEmpty { host }
}

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
