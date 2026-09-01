package com.zalith.launcher.loaders

import com.zalith.launcher.game.Downloader
import com.zalith.launcher.game.Http
import com.zalith.launcher.game.VersionInfo
import org.json.JSONArray
import java.io.File

/**
 * Instalação de Fabric e Quilt: ambos expõem um "profile json" pronto
 * (meta .../versions/loader/<jogo>/<loader>/profile/json) que já é um
 * version json com inheritsFrom da vanilla. O installer grava esse
 * json na instância e baixa as bibliotecas do loader. Nada de baixar
 * o installer oficial do Fabric: o meta faz o trabalho.
 */
class FabricLikeInstaller(
    private val commonDir: File,
    private val downloader: Downloader
) {

    enum class Flavor(val metaBase: String, val mavenBase: String) {
        FABRIC("https://meta.fabricmc.net/v2", "https://maven.fabricmc.net"),
        QUILT("https://meta.quiltmc.org/v3", "https://maven.quiltmc.org/repository")
    }

    class LoaderVersion(val version: String, val stable: Boolean)

    /** Loaders compatíveis com a versão do jogo (mais recente primeiro). */
    fun loaderVersions(flavor: Flavor, gameVersion: String): List<LoaderVersion> {
        val arr = JSONArray(Http.getString(flavor.metaBase + "/versions/loader/" + gameVersion))
        val out = ArrayList<LoaderVersion>(arr.length())
        for (i in 0 until arr.length()) {
            val loader = arr.getJSONObject(i).optJSONObject("loader") ?: continue
            out.add(LoaderVersion(loader.getString("version"), loader.optBoolean("stable", false)))
        }
        return out
    }

    /** Instala na instância e devolve o VersionInfo do loader. */
    fun install(
        flavor: Flavor,
        gameVersion: String,
        loaderVersion: String,
        versionDir: File
    ): VersionInfo {
        val profile = Http.getJson(
            flavor.metaBase + "/versions/loader/" + gameVersion + "/" + loaderVersion + "/profile/json"
        )
        val versionId = profile.getString("id")

        versionDir.mkdirs()
        File(versionDir, versionId + ".json").writeText(profile.toString(2), Charsets.UTF_8)

        val info = VersionInfo(profile)
        val libDir = File(commonDir, "libraries")
        val tasks = ArrayList<Downloader.Task>()
        for (lib in info.libraries) {
            tasks.add(Downloader.Task(lib.url, File(libDir, lib.path), lib.sha1, lib.size))
        }
        val failed = downloader.downloadAll(tasks)
        if (failed.isNotEmpty()) {
            throw IllegalStateException(
                "falha ao baixar " + failed.size + " bibliotecas do " + flavor
            )
        }
        return info
    }
}
