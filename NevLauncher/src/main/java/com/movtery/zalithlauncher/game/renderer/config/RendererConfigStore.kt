/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.renderer.config

import androidx.annotation.WorkerThread
import com.movtery.zalithlauncher.game.plugin.renderer_v2.data.RendererEnvConfig
import com.movtery.zalithlauncher.game.version.installed.getZalithVersionPath
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.tencent.mmkv.MMKV
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors

private const val TAG = "RendererConfigStore"
private val RENDERER_CONFIG_JSON = Json(GLOBAL_JSON) { encodeDefaults = true }

/**
 * 稀疏配置存储接口，只读写用户显式设置的值，不提供规范默认值
 */
interface RendererConfigStore {
    fun getValue(rendererId: String, entryId: String): ConfigScalar?
    fun putValue(rendererId: String, entryId: String, value: ConfigScalar)
    fun getEnabled(rendererId: String, entryId: String): Boolean?
    fun putEnabled(rendererId: String, entryId: String, enabled: Boolean)
    fun removeEntry(rendererId: String, entryId: String)
    fun removeRenderer(rendererId: String)

    /**
     * 获取该渲染器在存储中已有配置的全部配置项 ID
     */
    fun keys(rendererId: String): Set<String>
}

/**
 * 清理存储中已不在规范内的配置项，插件更新后用于移除失效配置
 * @return 是否移除了配置
 */
fun RendererConfigStore.removeStaleEntries(rendererId: String, spec: RendererConfigSpec): Boolean {
    val validIds = spec.entries.mapTo(mutableSetOf()) { it.id }
    val staleIds = keys(rendererId).filterNot { it in validIds }
    staleIds.forEach { removeEntry(rendererId, it) }
    return staleIds.isNotEmpty()
}

/**
 * 全局 MMKV 配置存储，并在首次使用渲染器时迁移旧 V2 与内置配置
 */
object GlobalRendererConfigStore : RendererConfigStore {
    private val mmkv get() = MMKV.mmkvWithID("RendererConfiguration", MMKV.MULTI_PROCESS_MODE)
    private val legacyMmkv get() = MMKV.mmkvWithID("RendererEnvConfig", MMKV.MULTI_PROCESS_MODE)

    override fun getValue(rendererId: String, entryId: String): ConfigScalar? =
        mmkv.getString(valueKey(rendererId, entryId), null)?.let(::decodeScalar)

    override fun putValue(rendererId: String, entryId: String, value: ConfigScalar) {
        mmkv.putString(valueKey(rendererId, entryId), encodeScalar(value)).apply()
    }

    override fun getEnabled(rendererId: String, entryId: String): Boolean? =
        if (mmkv.containsKey(enabledKey(rendererId, entryId))) {
            mmkv.getBoolean(enabledKey(rendererId, entryId), false)
        } else null

    override fun putEnabled(rendererId: String, entryId: String, enabled: Boolean) {
        mmkv.putBoolean(enabledKey(rendererId, entryId), enabled).apply()
    }

    override fun removeEntry(rendererId: String, entryId: String) {
        mmkv.remove(valueKey(rendererId, entryId))
        mmkv.remove(enabledKey(rendererId, entryId))
    }

    override fun removeRenderer(rendererId: String) {
        val prefix = "$rendererId:"
        mmkv.allKeys()?.filter { it.startsWith(prefix) }?.forEach(mmkv::remove)
    }

    override fun keys(rendererId: String): Set<String> {
        val prefix = "$rendererId:"
        return mmkv.allKeys()?.filter { it.startsWith(prefix) }
            ?.mapTo(mutableSetOf()) { key ->
                key.removePrefix(prefix).removeSuffix(":enabled").removeSuffix(":value")
            }
            .orEmpty()
    }

    @WorkerThread
    @Synchronized
    fun migrateLegacy(rendererId: String, legacyKeys: List<String>) {
        val marker = "migration:$rendererId"
        if (mmkv.getBoolean(marker, false)) return
        runCatching {
            // 迁移旧版 V2 插件的格式的数据
            val oldKeys = legacyMmkv.allKeys().orEmpty()
            legacyKeys.forEach { namespace ->
                oldKeys.filter { it.startsWith("$namespace:") }.forEach { oldKey ->
                    val entryKey = oldKey.removePrefix("$namespace:")
                    if (entryKey.endsWith(":check")) {
                        val entryId = entryKey.removeSuffix(":check")
                        val target = enabledKey(rendererId, entryId)
                        if (!mmkv.containsKey(target)) {
                            mmkv.putBoolean(target, legacyMmkv.getBoolean(oldKey, false)).apply()
                        }
                    } else {
                        val target = valueKey(rendererId, entryKey)
                        if (!mmkv.containsKey(target)) {
                            val value = legacyMmkv.getString(oldKey, null) ?: return@forEach
                            mmkv.putString(target, encodeScalar(ConfigScalar.StringValue(value))).apply()
                        }
                    }
                }
            }
            legacyKeys.forEach { namespace ->
                oldKeys.filter { it.startsWith("$namespace:") }.forEach(legacyMmkv::remove)
            }
            mmkv.putBoolean(marker, true).apply()
        }.onFailure {
            Logger.warning(TAG, "Failed to migrate global renderer configuration for $rendererId", it)
        }
    }

    private fun valueKey(rendererId: String, entryId: String) = "$rendererId:$entryId:value"
    private fun enabledKey(rendererId: String, entryId: String) = "$rendererId:$entryId:enabled"

    private fun encodeScalar(value: ConfigScalar): String = when (value) {
        is ConfigScalar.StringValue -> "s:${value.value}"
        is ConfigScalar.BooleanValue -> "b:${value.value}"
        is ConfigScalar.IntValue -> "i:${value.value}"
        is ConfigScalar.DoubleValue -> "d:${value.value}"
    }

    private fun decodeScalar(value: String): ConfigScalar? {
        if (value.length < 2) return null
        val content = value.substring(2)
        return when (value.substring(0, 2)) {
            "s:" -> ConfigScalar.StringValue(content)
            "b:" -> content.toBooleanStrictOrNull()?.let(ConfigScalar::BooleanValue)
            "i:" -> content.toIntOrNull()?.let(ConfigScalar::IntValue)
            "d:" -> content.toDoubleOrNull()?.let(ConfigScalar::DoubleValue)
            else -> null
        }
    }
}

@Serializable
private data class StoredRendererConfig(
    @SerialName("formatVersion") val formatVersion: Int = 1,
    @SerialName("renderers") val renderers: MutableMap<String, StoredRendererEntries> = mutableMapOf()
)

@Serializable
private data class StoredRendererEntries(
    @SerialName("values") val values: MutableMap<String, JsonElement> = mutableMapOf(),
    @SerialName("enabled") val enabled: MutableMap<String, Boolean> = mutableMapOf()
)

/**
 * 版本目录中的稀疏覆盖存储，负责版本化 JSON 持久化及旧格式迁移。
 * 文件写入在共享单线程中执行，创建新实例前会等待既有写入完成。
 *
 * @param legacyRendererIds 旧版渲染器配置 ID 到当前唯一标识的映射，用于迁移旧格式文件
 */
class VersionRendererConfigStore @WorkerThread constructor(
    versionFolder: File,
    private val legacyRendererIds: Map<String, String>
) : RendererConfigStore {
    private val configFolder = getZalithVersionPath(versionFolder)
    private val configFile = File(configFolder, RendererEnvConfig.CONFIG_FILE_NAME)
    private var config = loadConfig()

    override fun getValue(rendererId: String, entryId: String): ConfigScalar? =
        config.renderers[rendererId]?.values?.get(entryId)?.let { element ->
            // 值类型异常时视为未配置，回退上层默认值
            runCatching { configScalarFromJson(element.jsonPrimitive) }.getOrNull()
        }

    override fun putValue(rendererId: String, entryId: String, value: ConfigScalar) {
        entries(rendererId).values[entryId] = value.toJson()
        persist()
    }

    override fun getEnabled(rendererId: String, entryId: String): Boolean? =
        config.renderers[rendererId]?.enabled?.get(entryId)

    override fun putEnabled(rendererId: String, entryId: String, enabled: Boolean) {
        entries(rendererId).enabled[entryId] = enabled
        persist()
    }

    override fun removeEntry(rendererId: String, entryId: String) {
        val entry = config.renderers[rendererId] ?: return
        entry.values.remove(entryId)
        entry.enabled.remove(entryId)
        cleanup(rendererId)
        persist()
    }

    override fun removeRenderer(rendererId: String) {
        if (config.renderers.remove(rendererId) != null) persist()
    }

    override fun keys(rendererId: String): Set<String> {
        val entry = config.renderers[rendererId] ?: return emptySet()
        return entry.values.keys + entry.enabled.keys
    }

    private fun entries(rendererId: String): StoredRendererEntries =
        config.renderers.getOrPut(rendererId) { StoredRendererEntries() }

    private fun cleanup(rendererId: String) {
        val entry = config.renderers[rendererId] ?: return
        if (entry.values.isEmpty() && entry.enabled.isEmpty()) config.renderers.remove(rendererId)
    }

    private fun loadConfig(): StoredRendererConfig {
        awaitPendingWrites()
        if (!configFile.isFile) return StoredRendererConfig()
        return runCatching {
            val text = configFile.readText()
            val json = RENDERER_CONFIG_JSON.parseToJsonElement(text).jsonObject
            val hasStoredEntries = json["renderers"]?.jsonObject?.values?.any { element ->
                val keys = runCatching { element.jsonObject.keys }.getOrDefault(emptySet())
                "values" in keys || "enabled" in keys
            } == true
            if (json["formatVersion"] != null || hasStoredEntries) {
                val stored = RENDERER_CONFIG_JSON.decodeFromString<StoredRendererConfig>(text)
                if (json["formatVersion"] == null) persistBlocking(stored)
                stored
            } else {
                val migrated = RENDERER_CONFIG_JSON.decodeFromString<RendererEnvConfig>(text).toStoredConfig()
                persistBlocking(migrated)
                migrated
            }
        }.onFailure {
            Logger.warning(TAG, "Failed to read version renderer configuration", it)
        }.getOrDefault(StoredRendererConfig())
    }

    private fun RendererEnvConfig.toStoredConfig(): StoredRendererConfig {
        val migrated = StoredRendererConfig()
        renderers.forEach { (oldId, entries) ->
            val rendererId = legacyRendererIds[oldId] ?: oldId
            val target = migrated.renderers.getOrPut(rendererId) { StoredRendererEntries() }
            entries.envs.forEach { (key, value) -> target.values[key] = JsonPrimitive(value) }
            target.enabled.putAll(entries.checks)
        }
        return migrated
    }

    private fun persist() {
        val snapshot = runCatching { RENDERER_CONFIG_JSON.encodeToString(config) }.getOrElse {
            Logger.warning(TAG, "Failed to serialize version renderer configuration", it)
            return
        }
        writeExecutor.execute { write(snapshot) }
    }

    private fun persistBlocking(value: StoredRendererConfig): Boolean {
        val snapshot = runCatching { RENDERER_CONFIG_JSON.encodeToString(value) }.getOrElse {
            Logger.warning(TAG, "Failed to serialize version renderer configuration", it)
            return false
        }
        return write(snapshot)
    }

    private fun write(snapshot: String): Boolean {
        val tmpFile = File(configFolder, "${RendererEnvConfig.CONFIG_FILE_NAME}.tmp")
        return runCatching {
            if (!configFolder.exists()) configFolder.mkdirs()
            FileOutputStream(tmpFile).use { stream ->
                stream.write(snapshot.toByteArray())
                stream.fd.sync()
            }
            runCatching {
                Files.move(
                    tmpFile.toPath(),
                    configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            }.getOrElse {
                Files.move(tmpFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            true
        }.onFailure {
            tmpFile.delete()
            Logger.warning(TAG, "Failed to save version renderer configuration", it)
        }.getOrDefault(false)
    }

    private fun awaitPendingWrites() {
        try {
            writeExecutor.submit { }.get()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to wait for version renderer configuration writes", e)
        }
    }

    companion object {
        private val writeExecutor = Executors.newSingleThreadExecutor()
    }
}
