package com.zalith.launcher.game

import com.zalith.launcher.multiplayer.accounts.Account
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Argumentos finais (JVM e jogo) a partir da versão resolvida +
 * conta (fase 1) + instância. Cobre o formato novo (arguments) e o
 * legado (minecraftArguments). Montagem pura — nenhuma rede.
 */
class LaunchArguments(
    private val resolved: VersionInfo.ResolvedVersion,
    private val account: Account,
    private val gameDir: File,
    private val assetsRoot: File,
    private val nativesDir: File,
    private val libraryDir: File,
    private val clientJar: File,
    private val logConfigFile: File?,
    private val customWidth: Int = 0,
    private val customHeight: Int = 0
) {
    companion object {
        const val LAUNCHER_NAME = "ZalithLauncher3"
        const val LAUNCHER_VERSION = "0.1.0"
        private const val CLIENT_ID = "zl3-client"
    }

    /** Placeholder do vanilla: cifrão + chave entre chaves. */
    private fun ph(name: String): String = '$' + "{" + name + "}"

    private fun features(): Map<String, Boolean> {
        val f = HashMap<String, Boolean>()
        f["is_demo_user"] = false
        f["has_custom_resolution"] = customWidth > 0 && customHeight > 0
        f["has_quick_plays_singleplayer"] = false
        f["has_quick_plays_multiplayer"] = false
        f["has_quick_plays_realms"] = false
        f["is_quick_play_singleplayer"] = false
        f["is_quick_play_multiplayer"] = false
        f["is_quick_play_realms"] = false
        return f
    }

    /** Classpath: bibliotecas permitidas na ordem da cadeia + cliente. */
    fun classpath(): List<File> {
        val files = ArrayList<File>()
        for (lib in resolved.libraries(features())) {
            files.add(File(libraryDir, lib.path))
        }
        files.add(clientJar)
        return files
    }

    fun mainClass(): String = resolved.mainClass ?: "net.minecraft.client.main.Main"

    private fun replacements(): Map<String, String> {
        val ms = account as? Account.Microsoft
        val r = HashMap<String, String>()
        r[ph("auth_player_name")] = account.username
        r[ph("auth_uuid")] = account.uuid
        r[ph("auth_access_token")] = ms?.accessToken ?: "0"
        r[ph("auth_session")] = ms?.accessToken ?: "0"
        r[ph("user_type")] = if (ms != null) "msa" else "mojang"
        r[ph("auth_xuid")] = ms?.xuid ?: ""
        r[ph("clientid")] = CLIENT_ID
        r[ph("user_properties")] = "{}"
        r[ph("version_name")] = resolved.id
        r[ph("game_directory")] = gameDir.absolutePath
        r[ph("assets_root")] = assetsRoot.absolutePath
        r[ph("assets_index_name")] = resolved.assetsId ?: resolved.assetIndex?.id ?: "legacy"
        r[ph("natives_directory")] = nativesDir.absolutePath
        r[ph("library_directory")] = libraryDir.absolutePath
        r[ph("classpath_separator")] = File.pathSeparator
        r[ph("launcher_name")] = LAUNCHER_NAME
        r[ph("launcher_version")] = LAUNCHER_VERSION
        r[ph("log_path")] = logConfigFile?.absolutePath ?: ""
        r[ph("classpath")] = classpath().joinToString(File.pathSeparator)
        r[ph("resolution_width")] = (if (customWidth > 0) customWidth else 854).toString()
        r[ph("resolution_height")] = (if (customHeight > 0) customHeight else 480).toString()
        return r
    }

    /** Argumentos de JVM prontos (com -cp garantido). */
    fun jvmArgs(): List<String> {
        val out = ArrayList<String>()
        for (entry in resolved.jvmEntries()) {
            when (entry) {
                is String -> out.add(entry)
                is JSONObject -> {
                    val rules = Rules.parse(entry.optJSONArray("rules"))
                    if (!rules.allows(features())) continue
                    appendValue(out, entry.get("value"))
                }
            }
        }
        // legado: sem arguments.jvm, garantimos o classpath na mão
        if (out.isEmpty()) {
            out.add("-cp")
            out.add(classpath().joinToString(File.pathSeparator))
        }
        return replaceAll(out)
    }

    /** Argumentos do jogo prontos. */
    fun gameArgs(): List<String> {
        val out = ArrayList<String>()
        val legacy = resolved.legacyGameArguments()
        if (legacy != null) {
            for (piece in legacy.split(" ")) {
                if (piece.isNotEmpty()) out.add(piece)
            }
        } else {
            for (entry in resolved.gameEntries()) {
                when (entry) {
                    is String -> out.add(entry)
                    is JSONObject -> {
                        val rules = Rules.parse(entry.optJSONArray("rules"))
                        if (!rules.allows(features())) continue
                        appendValue(out, entry.get("value"))
                    }
                }
            }
        }
        return replaceAll(out)
    }

    private fun appendValue(out: MutableList<String>, value: Any) {
        when (value) {
            is String -> out.add(value)
            is JSONArray -> for (i in 0 until value.length()) out.add(value.getString(i))
        }
    }

    private fun replaceAll(args: List<String>): List<String> {
        val map = replacements()
        return args.map { arg ->
            var a = arg
            for ((k, v) in map) a = a.replace(k, v)
            a
        }
    }
}
