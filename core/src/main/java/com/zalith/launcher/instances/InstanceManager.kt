package com.zalith.launcher.instances

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Instâncias isoladas: cada uma com seu .minecraft (saves, mods,
 * config, logs). Bibliotecas e assets ficam no repositório comum
 * endereçado por conteúdo — deduplicados entre instâncias (decisão
 * de otimização: isolamento sem duplicar centenas de MB de jars).
 */
class InstanceManager(private val baseDir: File) {

    enum class LoaderType(val id: String) {
        NONE("none"), FABRIC("fabric"), FORGE("forge"), QUILT("quilt");

        companion object {
            fun fromId(id: String): LoaderType =
                values().firstOrNull { it.id == id } ?: NONE
        }
    }

    class Instance(
        val id: String,
        var name: String,
        var vanillaVersion: String,
        var loaderType: LoaderType,
        var loaderVersion: String?,
        var javaOverrideMajor: Int? = null,
        var created: Long = System.currentTimeMillis(),
        var lastPlayed: Long = 0L
    )

    private val file = File(baseDir, "instances.json")
    private val lock = Any()
    private val instances = mutableListOf<Instance>()

    init { reload() }

    fun reload() {
        synchronized(lock) {
            instances.clear()
            if (!file.exists()) return
            val text = file.readText(Charsets.UTF_8)
            if (text.isBlank()) return
            val root = JSONObject(text)
            val arr = root.optJSONArray("instances") ?: return
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val loaderVersion =
                    if (o.isNull("loaderVersion")) null else o.optString("loaderVersion")
                val javaOverride =
                    if (o.isNull("javaOverrideMajor")) null else o.optInt("javaOverrideMajor")
                instances.add(Instance(
                    o.getString("id"),
                    o.getString("name"),
                    o.getString("vanillaVersion"),
                    LoaderType.fromId(o.optString("loaderType", "none")),
                    loaderVersion,
                    javaOverride,
                    o.optLong("created", System.currentTimeMillis()),
                    o.optLong("lastPlayed", 0L)
                ))
            }
        }
    }

    fun list(): List<Instance> = synchronized(lock) {
        instances.sortedBy { it.name.lowercase() }
    }

    fun get(id: String): Instance? = synchronized(lock) {
        instances.firstOrNull { it.id == id }
    }

    fun create(
        name: String,
        vanillaVersion: String,
        loader: LoaderType = LoaderType.NONE,
        loaderVersion: String? = null
    ): Instance {
        val inst = Instance(newId(), name.trim(), vanillaVersion, loader, loaderVersion)
        synchronized(lock) {
            instances.add(inst)
            persist()
        }
        for (sub in GAME_SUBDIRS) File(gameDir(inst), sub).mkdirs()
        return inst
    }

    fun remove(id: String) {
        val inst: Instance
        synchronized(lock) {
            inst = instances.firstOrNull { it.id == id } ?: return
            instances.remove(inst)
            persist()
        }
        File(baseDir, "instances/" + inst.id).deleteRecursively()
    }

    fun markPlayed(id: String) {
        synchronized(lock) {
            val inst = instances.firstOrNull { it.id == id } ?: return
            inst.lastPlayed = System.currentTimeMillis()
            persist()
        }
    }

    /** Persiste edições feitas numa instância obtida por get(). */
    fun save(instance: Instance) {
        synchronized(lock) { persist() }
    }

    fun gameDir(instance: Instance): File =
        File(baseDir, "instances/" + instance.id + "/.minecraft")

    fun versionDir(instance: Instance): File =
        File(baseDir, "instances/" + instance.id + "/versions")

    fun commonDir(): File = File(baseDir, "common")

    private fun newId(): String = UUID.randomUUID().toString().substring(0, 8)

    private fun persist() {
        baseDir.mkdirs()
        val arr = JSONArray()
        for (i in instances) {
            arr.put(JSONObject()
                .put("id", i.id)
                .put("name", i.name)
                .put("vanillaVersion", i.vanillaVersion)
                .put("loaderType", i.loaderType.id)
                .put("loaderVersion", i.loaderVersion ?: JSONObject.NULL)
                .put("javaOverrideMajor", i.javaOverrideMajor ?: JSONObject.NULL)
                .put("created", i.created)
                .put("lastPlayed", i.lastPlayed))
        }
        file.writeText(JSONObject().put("instances", arr).toString(2), Charsets.UTF_8)
    }

    companion object {
        private val GAME_SUBDIRS = arrayOf(
            "saves", "mods", "config", "resourcepacks", "shaderpacks", "logs", "crash-reports"
        )
    }
}
