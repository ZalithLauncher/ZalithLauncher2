package com.zalith.launcher.game

import org.json.JSONObject
import java.io.File

/**
 * Assets do jogo: índice + objetos com endereçamento por SHA-1
 * (objects/ab/abcdef...), compartilhados entre instâncias — dois
 * mundos/instâncias não baixam sons e texturas duas vezes.
 */
class AssetManager(private val commonDir: File, private val downloader: Downloader) {

    private val assetsDir = File(commonDir, "assets")
    private val indexesDir = File(assetsDir, "indexes")
    private val objectsDir = File(assetsDir, "objects")
    private val resourceBase = "https://resources.download.minecraft.net/"

    class AssetObject(val name: String, val hash: String, val size: Long)

    /** Lê o índice da versão; índices são imutáveis → cache permanente. */
    fun index(resolved: VersionInfo.ResolvedVersion): JSONObject {
        val info = resolved.assetIndex
            ?: throw IllegalArgumentException("versão sem assetIndex (formato pré-1.6?)")
        val file = File(indexesDir, info.id + ".json")
        if (!file.exists() || (info.sha1 != null && downloader.sha1Of(file) != info.sha1)) {
            indexesDir.mkdirs()
            file.writeText(Http.getString(info.url), Charsets.UTF_8)
        }
        return JSONObject(file.readText(Charsets.UTF_8))
    }

    fun objects(index: JSONObject): List<AssetObject> {
        val objs = index.optJSONObject("objects") ?: return emptyList()
        val out = ArrayList<AssetObject>(objs.length())
        val keys = objs.keys()
        while (keys.hasNext()) {
            val name = keys.next()
            val o = objs.optJSONObject(name) ?: continue
            out.add(AssetObject(name, o.getString("hash"), o.optLong("size", -1L)))
        }
        return out
    }

    /**
     * Baixa os objetos que faltam. Incremental: objeto íntegro (sha1
     * conferido) nunca é rebaixado.
     * Retorna (quantos ok, quantos falharam).
     */
    fun downloadAssets(index: JSONObject, listener: Downloader.Listener? = null): Pair<Int, Int> {
        val tasks = ArrayList<Downloader.Task>()
        for (o in objects(index)) {
            val dir = o.hash.substring(0, 2)
            val dest = File(objectsDir, dir + "/" + o.hash)
            tasks.add(Downloader.Task(resourceBase + dir + "/" + o.hash, dest, o.hash, o.size))
        }
        val failed = downloader.downloadAll(tasks, listener)
        return Pair(tasks.size - failed.size, failed.size)
    }

    /** Raiz dos assets — para o placeholder assets_root. */
    fun assetsRoot(): File = assetsDir

    /**
     * Versões antigas esperam assets pelo nome (virtual/legacy) ou
     * copiados em resources/. Materializa o mapeamento uma vez.
     */
    fun materializeVirtual(index: JSONObject, targetDir: File): Boolean {
        if (!index.optBoolean("virtual", false) && !index.optBoolean("map_to_resources", false)) {
            return false
        }
        for (o in objects(index)) {
            val src = File(objectsDir, o.hash.substring(0, 2) + "/" + o.hash)
            if (!src.exists()) continue
            val dest = File(targetDir, o.name)
            dest.parentFile?.mkdirs()
            if (!dest.exists() || dest.length() != src.length()) {
                src.copyTo(dest, overwrite = true)
            }
        }
        return true
    }
}
