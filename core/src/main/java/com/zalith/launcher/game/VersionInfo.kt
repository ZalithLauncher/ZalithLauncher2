package com.zalith.launcher.game

import org.json.JSONArray
import org.json.JSONObject

/**
 * Metadados de uma versão do jogo, já interpretados.
 * Suporta cadeias inheritsFrom (vanilla ← fabric/forge/quilt) via
 * ResolvedVersion.
 */
class VersionInfo(val json: JSONObject) {

    val id: String = json.getString("id")
    val type: String = json.optString("type", "release")
    val mainClass: String? = json.optString("mainClass").takeIf { it.isNotEmpty() }
    val inheritsFrom: String? = json.optString("inheritsFrom").takeIf { it.isNotEmpty() }

    class DownloadInfo(val url: String, val sha1: String?, val size: Long)

    val clientDownload: DownloadInfo? = json.optJSONObject("downloads")
        ?.optJSONObject("client")
        ?.let { c ->
            DownloadInfo(
                c.getString("url"),
                c.optString("sha1").takeIf { it.isNotEmpty() },
                c.optLong("size", -1L)
            )
        }

    class AssetIndexInfo(
        val id: String,
        val url: String,
        val sha1: String?,
        val size: Long,
        val totalSize: Long
    )

    val assetIndex: AssetIndexInfo? = json.optJSONObject("assetIndex")?.let { a ->
        AssetIndexInfo(
            a.getString("id"),
            a.getString("url"),
            a.optString("sha1").takeIf { it.isNotEmpty() },
            a.optLong("size", -1L),
            a.optLong("totalSize", -1L)
        )
    }

    /** Campo antigo "assets": "legacy" ou o id do índice. */
    val assetsId: String? = json.optString("assets").takeIf { it.isNotEmpty() }

    val javaMajor: Int? = json.optJSONObject("javaVersion")?.optInt("majorVersion")

    class LogConfig(val id: String, val url: String, val sha1: String?, val size: Long)

    val logConfig: LogConfig? = json.optJSONObject("logging")
        ?.optJSONObject("client")
        ?.let { c ->
            val f = c.optJSONObject("file") ?: return@let null
            LogConfig(
                f.getString("id"),
                f.getString("url"),
                f.optString("sha1").takeIf { it.isNotEmpty() },
                f.optLong("size", -1L)
            )
        }

    class Library(
        val name: String,
        val path: String,
        val url: String,
        val sha1: String?,
        val size: Long,
        val rules: Rules
    )

    val libraries: List<Library> = run {
        val arr: JSONArray = json.optJSONArray("libraries") ?: JSONArray()
        val out = ArrayList<Library>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.getString("name")
            val rules = Rules.parse(o.optJSONArray("rules"))
            val artifact = o.optJSONObject("downloads")?.optJSONObject("artifact")
            val path = artifact?.optString("path")?.takeIf { it.isNotEmpty() }
                ?: mavenPathOf(name)
            val url = artifact?.optString("url")?.takeIf { it.isNotEmpty() }
                ?: run {
                    val base = o.optString("url").takeIf { it.isNotEmpty() }
                        ?: "https://libraries.minecraft.net/"
                    base.trimEnd('/') + "/" + path
                }
            out.add(Library(
                name,
                path,
                url,
                artifact?.optString("sha1")?.takeIf { it.isNotEmpty() },
                artifact?.optLong("size", -1L) ?: -1L,
                rules
            ))
        }
        out
    }

    /** Formato novo de argumentos (arguments.jvm/game), cru. */
    val argumentJson: JSONObject? = json.optJSONObject("arguments")

    /** Formato legado (minecraftArguments), cru. */
    val legacyArguments: String? =
        json.optString("minecraftArguments").takeIf { it.isNotEmpty() }

    /**
     * Cadeia de herança achatada: root (vanilla) primeiro, loader no
     * fim — essa é a ordem de classpath e de acréscimo de argumentos.
     */
    class ResolvedVersion private constructor(val chain: List<VersionInfo>) {

        val rootId: String get() = chain[0].id
        val id: String get() = chain[chain.size - 1].id

        val mainClass: String? get() = chain.lastOrNull { it.mainClass != null }?.mainClass
        val clientDownload: DownloadInfo? get() = chain.firstNotNullOfOrNull { it.clientDownload }
        val assetIndex: AssetIndexInfo? get() = chain.firstNotNullOfOrNull { it.assetIndex }
        val assetsId: String? get() = chain.firstNotNullOfOrNull { it.assetsId }
        val javaMajor: Int? get() = chain.firstNotNullOfOrNull { it.javaMajor }
        val logConfig: LogConfig? get() = chain.firstNotNullOfOrNull { it.logConfig }

        /** Bibliotecas permitidas, na ordem da cadeia — ordem de classpath. */
        fun libraries(features: Map<String, Boolean>): List<Library> {
            val out = ArrayList<Library>()
            for (v in chain) {
                for (lib in v.libraries) {
                    if (lib.rules.allows(features)) out.add(lib)
                }
            }
            return out
        }

        /** Entradas cruas (String ou JSONObject) na ordem root → loader. */
        fun jvmEntries(): List<Any> = collect("jvm")
        fun gameEntries(): List<Any> = collect("game")

        private fun collect(key: String): List<Any> {
            val out = ArrayList<Any>()
            for (v in chain) {
                val arr = v.argumentJson?.optJSONArray(key) ?: continue
                for (i in 0 until arr.length()) out.add(arr.get(i))
            }
            return out
        }

        /** Formato legado: o texto de minecraftArguments da versão base. */
        fun legacyGameArguments(): String? =
            chain.mapNotNull { it.legacyArguments }.lastOrNull()

        companion object {
            fun of(child: VersionInfo, lookup: (String) -> VersionInfo?): ResolvedVersion {
                val chain = ArrayList<VersionInfo>()
                val stack = ArrayDeque<VersionInfo>()
                var current: VersionInfo? = child
                while (current != null) {
                    stack.addFirst(current)
                    val parentId = current.inheritsFrom
                    current = if (parentId != null) lookup(parentId) else null
                }
                while (!stack.isEmpty()) chain.add(stack.removeFirst())
                return ResolvedVersion(chain)
            }
        }
    }

    companion object {
        /** group:artifact:version[:classifier] → caminho maven do jar. */
        fun mavenPathOf(name: String): String {
            val parts = name.split(":")
            val group = parts[0].replace('.', '/')
            val artifact = parts[1]
            val version = parts[2]
            val classifier = if (parts.size > 3) "-" + parts[3] else ""
            return group + "/" + artifact + "/" + version + "/" +
                    artifact + "-" + version + classifier + ".jar"
        }
    }
}
