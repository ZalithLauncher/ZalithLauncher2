package com.zalith.launcher.game

import org.json.JSONObject
import java.io.File

/**
 * Manifest de versões do Mojang (piston-meta) com cache local:
 * lista 1h, json de versão 24h — listagem relâmpago e menos rede.
 */
class VersionManifest(private val baseDir: File) {

    class VersionMeta(
        val id: String,
        val type: String,        // release, snapshot, old_beta, old_alpha
        val url: String,
        val releaseTime: String
    )

    private val manifestsDir = File(baseDir, "manifests")
    private val versionsDir = File(baseDir, "manifests/versions")
    private var cache: List<VersionMeta>? = null
    private var latestRelease: String? = null
    private var latestSnapshot: String? = null

    /** Lista completa (releaseTime descendente, como no manifest). */
    fun list(forceRefresh: Boolean = false): List<VersionMeta> {
        val cached = cache
        if (cached != null && !forceRefresh) return cached

        val file = File(manifestsDir, "version_manifest_v2.json")
        val fresh = file.exists() &&
                System.currentTimeMillis() - file.lastModified() < MANIFEST_TTL_MS
        val json = if (fresh && !forceRefresh) {
            JSONObject(file.readText(Charsets.UTF_8))
        } else {
            val text = Http.getString(MANIFEST_URL)
            manifestsDir.mkdirs()
            file.writeText(text, Charsets.UTF_8)
            JSONObject(text)
        }

        val arr = json.getJSONArray("versions")
        val out = ArrayList<VersionMeta>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(VersionMeta(
                o.getString("id"),
                o.getString("type"),
                o.getString("url"),
                o.getString("releaseTime")
            ))
        }
        val latest = json.optJSONObject("latest")
        latestRelease = latest?.optString("release")
        latestSnapshot = latest?.optString("snapshot")
        cache = out
        return out
    }

    fun releases(): List<VersionMeta> = list().filter { it.type == "release" }

    fun find(id: String): VersionMeta? = list().firstOrNull { it.id == id }

    fun latestReleaseId(): String? {
        list()
        return latestRelease
    }

    /** JSON de uma versão específica, com cache 24h. */
    fun versionJson(id: String, forceRefresh: Boolean = false): JSONObject {
        val file = File(versionsDir, id + ".json")
        val fresh = file.exists() &&
                System.currentTimeMillis() - file.lastModified() < VERSION_TTL_MS
        if (fresh && !forceRefresh) return JSONObject(file.readText(Charsets.UTF_8))
        val meta = find(id) ?: throw IllegalArgumentException("versão desconhecida: " + id)
        val text = Http.getString(meta.url)
        versionsDir.mkdirs()
        file.writeText(text, Charsets.UTF_8)
        return JSONObject(text)
    }

    companion object {
        private const val MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
        private const val MANIFEST_TTL_MS = 60L * 60L * 1000L       // 1h
        private const val VERSION_TTL_MS = 24L * 60L * 60L * 1000L  // 24h
    }
}
