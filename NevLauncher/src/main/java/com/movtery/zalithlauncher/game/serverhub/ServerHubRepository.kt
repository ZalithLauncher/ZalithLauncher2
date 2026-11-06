package com.movtery.zalithlauncher.game.serverhub

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.ServerAddress
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ServerHubRepository"
private const val FRESH_MS = 10 * 60 * 1000L
private const val KEY_CUSTOM = "custom"

private data class CustomServerDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("address") val address: String = ""
)

/**
 * Menggabungkan server dari beberapa sumber (minecraftserver.id, minecraft-serverlist.com, minecraft-mp.com)
 * serta server buatan pengguna menjadi satu daftar.
 *
 * Kegagalan satu sumber tidak mempengaruhi sumber lain: statusnya ada di [sourceStates].
 */
object ServerHubRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mmkv: MMKV by lazy { MMKV.mmkvWithID("server_hub") }
    private val gson = Gson()

    private val bySource = MutableStateFlow<Map<HubSource, List<HubServer>>>(emptyMap())
    private val extra = MutableStateFlow<List<HubServer>>(emptyList()) //hasil pencarian / tag dari API

    private val _servers = MutableStateFlow<List<HubServer>>(emptyList())

    /** Daftar gabungan, tanpa duplikat, terurut dari pemain online terbanyak */
    val servers: StateFlow<List<HubServer>> = _servers.asStateFlow()

    private val _modes = MutableStateFlow(HUB_DEFAULT_MODES)

    /** Mode permainan untuk filter "game" */
    val modes: StateFlow<List<String>> = _modes.asStateFlow()

    private val _states = MutableStateFlow<Map<HubSource, HubSourceState>>(emptyMap())
    val sourceStates: StateFlow<Map<HubSource, HubSourceState>> = _states.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var lastLoadAt = 0L
    private var loadJob: Job? = null
    private var remoteJob: Job? = null
    private var customs: List<CustomServerDto> = loadCustoms()

    init {
        publishCustoms(emptyList())
    }

    /**
     * Muat ulang semua sumber
     * @param force abaikan batas kesegaran data (10 menit)
     */
    fun refresh(force: Boolean = false) {
        if (loadJob?.isActive == true) return
        if (!force && System.currentTimeMillis() - lastLoadAt < FRESH_MS && bySource.value.isNotEmpty()) return

        _loading.value = true
        loadJob = scope.launch {
            _states.value = listOf(HubSource.MSID, HubSource.SERVERLIST, HubSource.MINECRAFT_MP)
                .associateWith { HubSourceState.Loading }
            try {
                coroutineScope {
                    listOf(
                        async { loadSource(HubSource.MSID) { fetchMsidServers() } },
                        async { loadSource(HubSource.SERVERLIST) { fetchHtmlSource(HubSource.SERVERLIST) } },
                        async { loadSource(HubSource.MINECRAFT_MP) { fetchHtmlSource(HubSource.MINECRAFT_MP) } },
                        async { loadTags() },
                        async { refreshCustoms() }
                    ).awaitAll()
                }
            } finally {
                lastLoadAt = System.currentTimeMillis()
                _loading.value = false
            }
        }
    }

    private suspend fun loadSource(source: HubSource, fetch: suspend () -> List<HubServer>) {
        try {
            val list = fetch()
            bySource.update { it + (source to list) }
            _states.update { it + (source to HubSourceState.Loaded(list.size)) }
        } catch (e: Throwable) {
            Logger.warning(TAG, "Failed to load ${source.label}", e)
            _states.update { it + (source to HubSourceState.Failed(e.message ?: e.javaClass.simpleName)) }
        }
        rebuild()
    }

    private suspend fun loadTags() {
        runCatching { fetchMsidTags() }.onSuccess { tags ->
            val merged = (HUB_DEFAULT_MODES + tags)
                .distinctBy { normalizeTag(it) }
            _modes.value = merged
        }
    }

    /**
     * Cari ke sumber API (minecraftserver.id) agar server di luar daftar teratas juga ketemu,
     * atau saring menurut mode permainan tertentu. Hasilnya digabung ke daftar.
     */
    fun searchRemote(query: String?, tag: String?) {
        remoteJob?.cancel()
        val q = query?.trim().orEmpty()
        val hasQuery = q.length >= 2
        if (!hasQuery && tag == null) {
            extra.value = emptyList()
            rebuild()
            return
        }
        remoteJob = scope.launch {
            val found = runCatching {
                fetchMsidServers(
                    query = q.takeIf { hasQuery },
                    tag = tag,
                    pages = if (hasQuery) 1 else 2
                )
            }.onFailure { Logger.warning(TAG, "Remote search failed", it) }.getOrDefault(emptyList())
            extra.value = found
            rebuild()
        }
    }

    //region server buatan pengguna

    /**
     * Tambah server sendiri
     * @return null bila berhasil, atau alasan kegagalan (alamat tidak valid / sudah ada)
     */
    fun addCustom(name: String, address: String): String? {
        val addr = address.trim()
        if (addr.isEmpty()) return "empty"
        if (runCatching { ServerAddress.parse(addr) }.isFailure) return "invalid"
        if (customs.any { normalizeAddress(it.address) == normalizeAddress(addr) }) return "exists"
        customs = customs + CustomServerDto(name.trim(), addr)
        saveCustoms()
        publishCustoms(emptyList())
        scope.launch { refreshCustoms() }
        return null
    }

    fun removeCustom(address: String) {
        customs = customs.filterNot { normalizeAddress(it.address) == normalizeAddress(address) }
        saveCustoms()
        bySource.update { it + (HubSource.CUSTOM to (it[HubSource.CUSTOM].orEmpty().filterNot { s ->
            normalizeAddress(s.address) == normalizeAddress(address)
        })) }
        rebuild()
    }

    private suspend fun refreshCustoms() {
        val pinged = coroutineScope {
            customs.map { dto ->
                async { dto to pingHubAddress(dto.address) }
            }.awaitAll()
        }
        publishCustoms(pinged)
    }

    /** Susun daftar server sendiri; [pinged] kosong berarti status belum diketahui */
    private fun publishCustoms(pinged: List<Pair<CustomServerDto, HubServer?>>) {
        val list = customs.map { dto ->
            val result = pinged.firstOrNull { it.first.address == dto.address }?.second
            val name = dto.name.ifBlank { nameFromAddress(dto.address) }
            (result ?: HubServer(
                id = "custom:${normalizeAddress(dto.address)}",
                name = name,
                address = dto.address,
                source = HubSource.CUSTOM,
                isIndonesia = false,
                online = if (pinged.isEmpty()) null else false,
                edition = "Java"
            )).copy(name = name)
        }
        bySource.update { it + (HubSource.CUSTOM to list) }
        rebuild()
    }

    private fun loadCustoms(): List<CustomServerDto> {
        val json = mmkv.decodeString(KEY_CUSTOM, "").orEmpty()
        if (json.isBlank()) return emptyList()
        return runCatching {
            gson.fromJson(json, Array<CustomServerDto>::class.java).orEmpty().filter { it.address.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun saveCustoms() {
        mmkv.encode(KEY_CUSTOM, gson.toJson(customs))
    }

    //endregion

    /** Gabungkan semua sumber: buang duplikat (sumber lebih dulu menang) lalu urutkan menurut pemain */
    private fun rebuild() {
        val order = listOf(HubSource.MSID, HubSource.SERVERLIST, HubSource.MINECRAFT_MP, HubSource.CUSTOM)
        val all = order.flatMap { bySource.value[it].orEmpty() } + extra.value
        val merged = LinkedHashMap<String, HubServer>()
        all.forEach { merged.putIfAbsent(normalizeAddress(it.address), it) }
        _servers.value = merged.values.sortedByDescending { it.players }

        //Tambahkan tag dari server yang dimuat ke daftar mode
        val known = _modes.value.map { normalizeTag(it) }.toHashSet()
        val newTags = merged.values.flatMap { it.tags }
            .filter { it.isNotBlank() && normalizeTag(it) !in known }
            .distinctBy { normalizeTag(it) }
        if (newTags.isNotEmpty()) _modes.update { it + newTags }
    }
}
