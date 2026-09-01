package com.zalith.launcher.game

import com.zalith.launcher.instances.InstanceManager
import org.json.JSONObject
import java.io.File

/**
 * Orquestrador: deixa uma instância pronta para iniciar.
 * resolve a cadeia de versões → bibliotecas → cliente → log4j → assets.
 * Tudo incremental: só baixa o que falta ou divergiu.
 */
class GameSetup(
    private val baseDir: File,
    private val manifest: VersionManifest,
    private val downloader: Downloader
) {
    class SetupReport(
        val resolved: VersionInfo.ResolvedVersion,
        val clientJar: File,
        val logConfigFile: File?,
        val assetsOk: Int,
        val assetsFailed: Int,
        val librariesFailed: Int
    ) {
        val ok: Boolean
            get() = assetsFailed == 0 && librariesFailed == 0 && clientJar.exists()
    }

    private val common = File(baseDir, "common")
    private val assets = AssetManager(common, downloader)

    fun versionDirOf(instance: InstanceManager.Instance): File =
        File(baseDir, "instances/" + instance.id + "/versions")

    /**
     * Cadeia da instância: vanilla + json do loader gravado pelo
     * installer (procura por inheritsFrom == versão vanilla).
     */
    fun resolve(instance: InstanceManager.Instance): VersionInfo.ResolvedVersion {
        val vanilla = VersionInfo(manifest.versionJson(instance.vanillaVersion))
        var child: VersionInfo? = null
        if (instance.loaderType != InstanceManager.LoaderType.NONE) {
            val dir = versionDirOf(instance)
            val files = dir.listFiles() ?: arrayOf<File>()
            for (f in files) {
                if (!f.name.endsWith(".json")) continue
                val info = VersionInfo(JSONObject(f.readText(Charsets.UTF_8)))
                if (info.inheritsFrom == instance.vanillaVersion) {
                    child = info
                    break
                }
            }
            if (child == null) {
                throw IllegalStateException(
                    "instância declara loader " + instance.loaderType +
                    " mas não tem json de versão — rode o installer primeiro"
                )
            }
        }
        return VersionInfo.ResolvedVersion.of(child ?: vanilla) { id ->
            VersionInfo(manifest.versionJson(id))
        }
    }

    /** Prepara tudo. Bloqueante — fora da main thread. */
    fun prepare(
        instance: InstanceManager.Instance,
        listener: Downloader.Listener? = null
    ): SetupReport {
        val resolved = resolve(instance)

        // 1. bibliotecas no repositório comum (dedupe por caminho maven)
        val libDir = File(common, "libraries")
        val libTasks = ArrayList<Downloader.Task>()
        for (lib in resolved.libraries(emptyMap())) {
            libTasks.add(Downloader.Task(lib.url, File(libDir, lib.path), lib.sha1, lib.size))
        }
        val failedLibs = downloader.downloadAll(libTasks, listener)

        // 2. jar do cliente
        val download = resolved.clientDownload
            ?: throw IllegalStateException("versão sem download do cliente")
        val clientJar = File(common, "clients/" + resolved.rootId + ".jar")
        downloader.download(
            Downloader.Task(download.url, clientJar, download.sha1, download.size), listener
        )

        // 3. log4j config (pós-log4shell)
        var logFile: File? = null
        resolved.logConfig?.let { lc ->
            val f = File(common, "log-configs/" + lc.id)
            if (downloader.download(Downloader.Task(lc.url, f, lc.sha1, lc.size), listener)) {
                logFile = f
            }
        }

        // 4. assets + layout virtual legado
        val index = assets.index(resolved)
        val result = assets.downloadAssets(index, listener)
        if (index.optBoolean("virtual", false)) {
            assets.materializeVirtual(index, File(common, "assets/virtual/legacy"))
        }

        return SetupReport(
            resolved, clientJar, logFile,
            result.first, result.second, failedLibs.size
        )
    }
}
