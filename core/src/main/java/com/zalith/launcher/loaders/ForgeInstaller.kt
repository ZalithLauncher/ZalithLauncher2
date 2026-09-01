package com.zalith.launcher.loaders

import com.zalith.launcher.game.Downloader
import com.zalith.launcher.game.Http
import com.zalith.launcher.game.VersionInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Instalação do Forge: baixa o installer, abre como zip, extrai
 * install_profile.json + version.json e materializa as bibliotecas.
 *
 * Os "processors" do Forge (pós-processamento que exige executar jars
 * numa JVM — binpatches etc.) ficam para a fase de runtime/ponte JNI;
 * o resultado avisa quando estão pendentes.
 */
class ForgeInstaller(
    private val commonDir: File,
    private val downloader: Downloader
) {
    private val promotionsUrl =
        "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json"
    private val mavenBase = "https://maven.minecraftforge.net"

    class ForgeResult(val versionInfo: VersionInfo, val processorsPending: Boolean)

    /** Build promovido: "1.20.1-recommended" → "47.2.0". */
    fun promotion(gameVersion: String, channel: String = "recommended"): String? {
        val promos = Http.getJson(promotionsUrl).optJSONObject("promos") ?: return null
        val value = promos.optString(gameVersion + "-" + channel)
        return value.takeIf { it.isNotEmpty() }
    }

    fun install(gameVersion: String, forgeVersion: String, versionDir: File): ForgeResult {
        val jar = downloadInstaller(gameVersion, forgeVersion)

        val profile: JSONObject
        val versionJson: JSONObject
        ZipFile(jar).use { zip ->
            val profileEntry = zip.getEntry("install_profile.json")
                ?: throw IllegalStateException("installer sem install_profile.json")
            profile = readZipJson(zip, profileEntry)
            // profile v2 aponta o json; v1 usa version.json fixo
            val jsonPath = profile.optString("json").takeIf { it.isNotEmpty() } ?: "version.json"
            val versionEntry = zip.getEntry(jsonPath)
                ?: throw IllegalStateException("installer sem " + jsonPath)
            versionJson = readZipJson(zip, versionEntry)
        }

        val versionId = versionJson.getString("id")
        versionDir.mkdirs()
        File(versionDir, versionId + ".json").writeText(versionJson.toString(2), Charsets.UTF_8)

        // bibliotecas declaradas no profile → repositório comum
        val arr: JSONArray = profile.optJSONArray("libraries") ?: JSONArray()
        val libDir = File(commonDir, "libraries")
        val tasks = ArrayList<Downloader.Task>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.getString("name")
            val artifact = o.optJSONObject("downloads")?.optJSONObject("artifact")
            val path = artifact?.optString("path")?.takeIf { it.isNotEmpty() }
                ?: VersionInfo.mavenPathOf(name)
            val url = artifact?.optString("url")?.takeIf { it.isNotEmpty() }
                ?: run {
                    val base = o.optString("url").takeIf { it.isNotEmpty() } ?: mavenBase
                    base.trimEnd('/') + "/" + path
                }
            tasks.add(Downloader.Task(url, File(libDir, path)))
        }
        downloader.downloadAll(tasks)

        val processors = profile.optJSONArray("processors")
        return ForgeResult(VersionInfo(versionJson), processors != null && processors.length() > 0)
    }

    private fun downloadInstaller(gameVersion: String, forgeVersion: String): File {
        val name = "forge-" + gameVersion + "-" + forgeVersion + "-installer.jar"
        val dest = File(commonDir, "installers/" + name)
        val task = Downloader.Task(
            mavenBase + "/net/minecraftforge/forge/" + gameVersion + "-" + forgeVersion + "/" + name,
            dest
        )
        if (!downloader.download(task)) {
            throw IllegalStateException("não consegui baixar o installer do forge " + forgeVersion)
        }
        return dest
    }

    private fun readZipJson(zip: ZipFile, entry: ZipEntry): JSONObject {
        val text = zip.getInputStream(entry).bufferedReader().use { it.readText() }
        return JSONObject(text)
    }
}
