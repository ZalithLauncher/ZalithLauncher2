package com.zalith.launcher.multiplayer.servers

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Lista de servidores persistida em <diretório>/servers.json,
 * com favoritos e o último status conhecido de cada um.
 */
class ServerList(private val storageDir: File) {

    data class Entry(
        val id: String,
        var name: String,
        var host: String,
        var port: Int,
        var favorite: Boolean = false
    )

    data class Status(
        val online: Boolean,
        val motd: String? = null,
        val playersOnline: Int = -1,
        val playersMax: Int = -1,
        val version: String? = null,
        val protocol: Int = -1,
        val latencyMs: Long = -1
    )

    private val file = File(storageDir, "servers.json")
    private val entries = mutableListOf<Entry>()
    private val statuses = mutableMapOf<String, Status>()

    init {
        reload()
    }

    fun reload() {
        synchronized(this) {
            entries.clear()
            statuses.clear()
            if (!file.exists()) return
            val text = file.readText(Charsets.UTF_8)
            if (text.isBlank()) return
            val root = JSONObject(text)
            val arr = root.optJSONArray("servers") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                entries.add(
                    Entry(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        host = o.getString("host"),
                        port = o.optInt("port", 25565),
                        favorite = o.optBoolean("favorite", false)
                    )
                )
            }
            val st = root.optJSONObject("statuses") ?: return
            val keys = st.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val s = st.optJSONObject(id) ?: continue
                statuses[id] = Status(
                    online = s.optBoolean("online", false),
                    motd = s.optString("motd").takeIf { it.isNotEmpty() },
                    playersOnline = s.optInt("playersOnline", -1),
                    playersMax = s.optInt("playersMax", -1),
                    version = s.optString("version").takeIf { it.isNotEmpty() },
                    protocol = s.optInt("protocol", -1),
                    latencyMs = s.optLong("latencyMs", -1)
                )
            }
        }
    }

    /** Favoritos primeiro, depois ordem alfabética. */
    fun list(): List<Entry> = synchronized(this) {
        entries.sortedWith(compareByDescending<Entry> { it.favorite }.thenBy { it.name.lowercase() })
    }

    fun status(id: String): Status? = synchronized(this) { statuses[id] }

    fun add(name: String, host: String, port: Int = 25565): Entry {
        val entry = Entry(newId(), name.trim(), host.trim().ifEmpty { name.trim() }, port)
        synchronized(this) {
            entries.add(entry)
            persist()
        }
        return entry
    }

    fun remove(id: String) {
        synchronized(this) {
            entries.removeAll { it.id == id }
            statuses.remove(id)
            persist()
        }
    }

    fun edit(id: String, name: String, host: String, port: Int): Boolean {
        return synchronized(this) {
            val entry = entries.firstOrNull { it.id == id } ?: return@synchronized false
            entry.name = name.trim()
            entry.host = host.trim()
            entry.port = port
            persist()
            true
        }
    }

    fun toggleFavorite(id: String): Boolean {
        return synchronized(this) {
            val entry = entries.firstOrNull { it.id == id } ?: return@synchronized false
            entry.favorite = !entry.favorite
            persist()
            true
        }
    }

    fun updateStatus(id: String, status: Status) {
        synchronized(this) {
            statuses[id] = status
            persist()
        }
    }

    private fun newId(): String = UUID.randomUUID().toString().substring(0, 8)

    private fun persist() {
        storageDir.mkdirs()
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(JSONObject()
                .put("id", e.id)
                .put("name", e.name)
                .put("host", e.host)
                .put("port", e.port)
                .put("favorite", e.favorite))
        }
        val st = JSONObject()
        statuses.forEach { (id, s) ->
            val o = JSONObject()
                .put("online", s.online)
                .put("playersOnline", s.playersOnline)
                .put("playersMax", s.playersMax)
                .put("protocol", s.protocol)
                .put("latencyMs", s.latencyMs)
            s.motd?.let { o.put("motd", it) }
            s.version?.let { o.put("version", it) }
            st.put(id, o)
        }
        file.writeText(JSONObject()
            .put("servers", arr)
            .put("statuses", st)
            .toString(2), Charsets.UTF_8)
    }
}
