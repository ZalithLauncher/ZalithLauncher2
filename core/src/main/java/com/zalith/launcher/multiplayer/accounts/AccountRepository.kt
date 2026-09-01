package com.zalith.launcher.multiplayer.accounts

import org.json.JSONObject
import java.io.File

/**
 * Contas persistidas em <diretório>/accounts.json.
 * IO pequeno (um arquivo, poucas contas) — sincronização simples basta.
 */
class AccountRepository(private val storageDir: File) {

    private val file = File(storageDir, "accounts.json")
    private val lock = Any()

    private val accounts = mutableListOf<Account>()
    private var activeUuid: String? = null

    init {
        reload()
    }

    fun reload() {
        synchronized(lock) {
            accounts.clear()
            if (file.exists()) {
                val text = file.readText(Charsets.UTF_8)
                if (text.isNotBlank()) {
                    val root = JSONObject(text)
                    val arr = root.optJSONArray("accounts")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            accounts.add(Account.fromJson(arr.getJSONObject(i)))
                        }
                    }
                    activeUuid = root.optString("active").takeIf { it.isNotEmpty() }
                }
            }
        }
    }

    fun list(): List<Account> = synchronized(lock) { accounts.toList() }

    fun active(): Account? = synchronized(lock) {
        accounts.firstOrNull { it.uuid == activeUuid } ?: accounts.firstOrNull()
    }

    fun add(account: Account) {
        synchronized(lock) {
            accounts.removeAll { it.uuid == account.uuid }
            accounts.add(account)
            if (activeUuid == null) activeUuid = account.uuid
            persist()
        }
    }

    fun remove(uuid: String) {
        synchronized(lock) {
            accounts.removeAll { it.uuid == uuid }
            if (accounts.isEmpty()) activeUuid = null
            persist()
        }
    }

    fun setActive(uuid: String): Boolean {
        return synchronized(lock) {
            val exists = accounts.any { it.uuid == uuid }
            if (exists) {
                activeUuid = uuid
                persist()
            }
            exists
        }
    }

    fun update(account: Account): Boolean {
        return synchronized(lock) {
            val idx = accounts.indexOfFirst { it.uuid == account.uuid }
            if (idx >= 0) {
                accounts[idx] = account
                persist()
                true
            } else {
                false
            }
        }
    }

    private fun persist() {
        storageDir.mkdirs()
        val root = JSONObject()
            .put("accounts", Account.listToJson(accounts))
            .put("active", activeUuid ?: JSONObject.NULL)
        file.writeText(root.toString(2), Charsets.UTF_8)
    }
}
