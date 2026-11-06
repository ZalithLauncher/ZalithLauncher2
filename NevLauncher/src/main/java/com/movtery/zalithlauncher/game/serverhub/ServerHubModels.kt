package com.movtery.zalithlauncher.game.serverhub

/**
 * Asal data sebuah server di Server Hub
 */
enum class HubSource(val label: String, val url: String?) {
    /** API publik resmi minecraftserver.id (server Indonesia) */
    MSID("minecraftserver.id", "https://minecraftserver.id"),

    /** Halaman daftar minecraft-serverlist.com (server luar) */
    SERVERLIST("minecraft-serverlist.com", "https://minecraft-serverlist.com"),

    /** Halaman daftar minecraft-mp.com (server luar) */
    MINECRAFT_MP("minecraft-mp.com", "https://minecraft-mp.com"),

    /** Server yang ditambahkan sendiri oleh pengguna */
    CUSTOM("Server saya", null)
}

/**
 * Filter lokasi server
 */
enum class HubLocation { ALL, INDONESIA, OVERSEAS }

/**
 * Satu server pada daftar Server Hub
 *
 * @property address alamat Java yang dipakai untuk bergabung (host atau host:port)
 * @property online null bila status belum diketahui
 * @property tags mode permainan, mis. Survival, SkyBlock, OneBlock
 * @property detailUrl halaman server di website sumber (untuk kredit/tautan)
 */
data class HubServer(
    val id: String,
    val name: String,
    val address: String,
    val source: HubSource,
    val isIndonesia: Boolean,
    val online: Boolean? = null,
    val players: Int = 0,
    val maxPlayers: Int = 0,
    val version: String? = null,
    val edition: String? = null,
    val tags: List<String> = emptyList(),
    val iconUrl: String? = null,
    val iconBytes: ByteArray? = null,
    val detailUrl: String? = null
) {
    override fun equals(other: Any?): Boolean = other is HubServer && other.id == id &&
            other.players == players && other.online == online && other.name == name &&
            other.tags == tags && other.version == version

    override fun hashCode(): Int = id.hashCode() * 31 + players
}

/**
 * Hasil muat dari satu sumber
 */
sealed interface HubSourceState {
    data object Loading : HubSourceState
    data class Loaded(val count: Int) : HubSourceState
    data class Failed(val reason: String) : HubSourceState
}

/** Pilihan batas jumlah pemain pada filter "online" */
val HUB_ONLINE_STEPS = listOf(10, 50, 100, 150, 200, 300, 500)

/** Mode permainan yang selalu tersedia pada filter "game", tag tambahan dari sumber ikut ditambahkan */
val HUB_DEFAULT_MODES = listOf(
    "Survival", "SkyBlock", "OneBlock", "Creative", "PvP", "Factions", "Prison",
    "Lifesteal", "SMP", "Minigames", "Roleplay", "Economy", "RPG", "Towny", "Hardcore", "Anarchy"
)

/** Samakan penulisan tag: huruf kecil tanpa spasi/tanda hubung ("One Block" == "oneblock") */
fun normalizeTag(tag: String): String =
    tag.lowercase().filter { it.isLetterOrDigit() }

/** Alamat dibakukan agar server yang sama dari beberapa sumber tidak muncul dua kali */
fun normalizeAddress(address: String): String {
    val a = address.trim().lowercase()
    return if (a.endsWith(":25565")) a.removeSuffix(":25565") else a
}
