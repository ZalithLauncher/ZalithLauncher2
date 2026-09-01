package com.zalith.launcher.game

import org.json.JSONArray

/**
 * Motor de regras do manifest do Mojang: decide se uma biblioteca ou
 * argumento se aplica ao ambiente atual.
 *
 * No Android reportamos os.name = "linux" (kernel compartilhado) e o
 * arch cru (aarch64 e afins) — de propósito não casa com x86/amd64,
 * então natives de desktop não são baixados: o runtime próprio (fase 0)
 * fornece os binários ARM.
 */
class Rules private constructor(private val entries: List<Entry>) {

    private class Entry(val allow: Boolean, val os: OsRule?, val features: Map<String, Boolean>?)

    private class OsRule(val name: String?, val arch: String?, val versionRegex: String?)

    companion object {
        val CURRENT_OS = "linux"
        val CURRENT_ARCH: String = System.getProperty("os.arch") ?: ""

        fun parse(json: JSONArray?): Rules {
            val list = ArrayList<Entry>()
            if (json == null) return Rules(list)
            for (i in 0 until json.length()) {
                val o = json.getJSONObject(i)
                val osObj = o.optJSONObject("os")
                val os = if (osObj != null) OsRule(
                    osObj.optString("name").takeIf { it.isNotEmpty() },
                    osObj.optString("arch").takeIf { it.isNotEmpty() },
                    osObj.optString("version").takeIf { it.isNotEmpty() }
                ) else null
                val features = HashMap<String, Boolean>()
                val featObj = o.optJSONObject("features")
                if (featObj != null) {
                    val keys = featObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        features[k] = featObj.optBoolean(k, false)
                    }
                }
                list.add(Entry(
                    o.optString("action") == "allow",
                    os,
                    if (features.isEmpty()) null else features
                ))
            }
            return Rules(list)
        }
    }

    /**
     * Sem regras: sempre permitido.
     * Com regras: a última que casa decide; nenhuma casa = negado.
     */
    fun allows(features: Map<String, Boolean>): Boolean {
        if (entries.isEmpty()) return true
        var matched = false
        var allow = false
        for (e in entries) {
            if (matches(e, features)) {
                matched = true
                allow = e.allow
            }
        }
        return matched && allow
    }

    private fun matches(e: Entry, features: Map<String, Boolean>): Boolean {
        val os = e.os
        if (os != null) {
            if (os.name != null && os.name != CURRENT_OS) return false
            if (os.arch != null && os.arch != CURRENT_ARCH) return false
            // regex de versão de SO mira desktops (windows/osx); não há como casar aqui
            if (os.versionRegex != null) return false
        }
        e.features?.let { required ->
            for ((k, v) in required) {
                if (features[k] != v) return false
            }
        }
        return true
    }
}
