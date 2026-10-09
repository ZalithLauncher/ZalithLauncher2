/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.content.home.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.multiplayer.pingServer
import com.movtery.zalithlauncher.game.version.multiplayer.resolve
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardDir
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardManager
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardStatus
import com.movtery.zalithlauncher.utils.hasStoragePermission
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.ServerAddress
import com.movtery.zalithlauncher.utils.string.stripColorCodes
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Base64

private const val TAG = "ServerCardManager"

/** 特定版本模式的存储标识 */
private const val MODE_SPECIFIC = "specific"

/** 服务器卡片记录的持久化数据 */
private data class ServerCardRecordDto(
    @SerializedName("cardId")
    val cardId: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("quickLaunchEnabled")
    val quickLaunchEnabled: Boolean = true,
    @SerializedName("versionMode")
    val versionMode: String = "",
    @SerializedName("dirType")
    val dirType: String = "",
    @SerializedName("dirPath")
    val dirPath: String = "",
    @SerializedName("versionName")
    val versionName: String? = null
) {
    fun toRecord() = ServerCardRecord(
        cardId = cardId,
        name = name,
        icon = icon?.let { base64 ->
            runCatching { Base64.getDecoder().decode(base64) }.getOrNull()
        },
        quickLaunchEnabled = quickLaunchEnabled,
        versionMode = if (versionMode == MODE_SPECIFIC) {
            ServerCardVersionMode.SPECIFIC
        } else {
            ServerCardVersionMode.CURRENT
        },
        dir = when {
            versionMode != MODE_SPECIFIC -> null
            dirType == VersionCardManager.DIR_TYPE_CUSTOM -> VersionCardDir.Custom(dirPath)
            else -> VersionCardDir.Default
        },
        versionName = versionName
    )

    companion object {
        fun fromRecord(record: ServerCardRecord) = ServerCardRecordDto(
            cardId = record.cardId,
            name = record.name,
            icon = record.icon?.let { Base64.getEncoder().encodeToString(it) },
            quickLaunchEnabled = record.quickLaunchEnabled,
            versionMode = if (record.versionMode == ServerCardVersionMode.SPECIFIC) {
                MODE_SPECIFIC
            } else {
                "current"
            },
            dirType = when (record.dir) {
                is VersionCardDir.Custom -> VersionCardManager.DIR_TYPE_CUSTOM
                is VersionCardDir.Default -> VersionCardManager.DIR_TYPE_DEFAULT
                null -> ""
            },
            dirPath = (record.dir as? VersionCardDir.Custom)?.path ?: "",
            versionName = record.versionName
        )
    }
}

/**
 * 服务器卡片管理器：
 * 卡片以服务器 IP 字符串为 id，记录自治（名称与图标自持有，独立 ping），
 * 与任何版本的 servers.dat 无运行时耦合。
 */
object ServerCardManager {
    /** 自动刷新的冷却间隔 */
    private const val REFRESH_COOLDOWN_MS = 15_000L

    private const val KEY_RECORDS = "serverCards"

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID("home_server_cards") }
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _cards = MutableStateFlow<List<ServerCardState>>(emptyList())

    /** 全部服务器卡片及其当前状态 */
    val cards: StateFlow<List<ServerCardState>> = _cards.asStateFlow()

    /** 正在 ping 的卡片 id 集合 */
    private val _inFlight = MutableStateFlow<Set<String>>(emptySet())

    /** 正在刷新（ping 进行中）的卡片 id 集合 */
    val inFlightCards: StateFlow<Set<String>> = _inFlight.asStateFlow()

    /** 进行中的 ping 任务 */
    private val pingJobs = mutableMapOf<String, Job>()

    /** 冷却锚点：每张卡上次 ping 完成的时刻 */
    private val lastPingAt = mutableMapOf<String, Long>()

    /** 当前处于可见状态的卡片 id 集合 */
    private var visibleIds: Set<String> = emptySet()

    init {
        _cards.value = loadRecords().map { record ->
            ServerCardState(record, ServerCardPingStatus.Loading)
        }
        //首次解析绑定版本
        recheckVersions()
        //版本列表刷新后同步绑定版本的可用性
        VersionsManager.registerListener {
            recheckVersions()
        }
    }

    /** 指定服务器是否已存在对应卡片 */
    fun hasCard(serverIp: String): Boolean =
        _cards.value.any { it.record.cardId == serverIp }

    /**
     * 为服务器创建主页卡片，已存在同 IP 卡片时不做任何事
     * @param name 服务器名称（颜色代码会被剔除），空白时以 IP 兜底
     * @param serverIp 服务器 IP 字符串，同时作为卡片 id
     * @param icon 初始图标（取自列表条目已缓存的 favicon）
     * @return 是否成功创建
     */
    fun addCard(name: String, serverIp: String, icon: ByteArray? = null): Boolean {
        val ip = serverIp.trim()
        if (ip.isEmpty()) return false
        synchronized(this) {
            if (hasCard(ip)) return false
            val record = ServerCardRecord(
                cardId = ip,
                name = name.stripColorCodes().ifBlank { ip },
                icon = icon
            )
            _cards.update { states ->
                states + ServerCardState(record, ServerCardPingStatus.Loading)
            }
            saveRecords()
        }
        return true
    }

    /** 移除指定卡片 */
    fun removeCard(cardId: String) {
        synchronized(this) {
            if (_cards.value.none { it.record.cardId == cardId }) return
            pingJobs.remove(cardId)?.cancel()
            lastPingAt.remove(cardId)
            _inFlight.update { it - cardId }
            visibleIds -= cardId
            _cards.update { states ->
                states.filterNot { it.record.cardId == cardId }
            }
            saveRecords()
        }
    }

    /** 重命名卡片 */
    fun setCardName(cardId: String, name: String) {
        val trimmed = name.stripColorCodes().trim()
        if (trimmed.isEmpty()) return
        synchronized(this) {
            updateState(cardId) { it.copy(record = it.record.copy(name = trimmed)) } ?: return
            saveRecords()
        }
    }

    /** 设置快速启动按钮的启用状态 */
    fun setQuickLaunchEnabled(cardId: String, enabled: Boolean) {
        synchronized(this) {
            updateState(cardId) { it.copy(record = it.record.copy(quickLaunchEnabled = enabled)) } ?: return
            saveRecords()
        }
    }

    /**
     * 更新版本绑定并重新解析绑定状态
     * @param dir 特定版本所在的游戏目录，仅 SPECIFIC 模式使用
     * @param versionName 绑定的版本名，仅 SPECIFIC 模式使用
     */
    fun setVersionBinding(
        cardId: String,
        mode: ServerCardVersionMode,
        dir: VersionCardDir? = null,
        versionName: String? = null
    ) {
        synchronized(this) {
            updateState(cardId) {
                it.copy(
                    record = it.record.copy(
                        versionMode = mode,
                        dir = dir.takeIf { mode == ServerCardVersionMode.SPECIFIC },
                        versionName = versionName.takeIf { mode == ServerCardVersionMode.SPECIFIC }
                    )
                )
            } ?: return
            saveRecords()
        }
        scope.launch {
            resolveBinding(cardId)
        }
    }

    /**
     * 刷新指定服务器的 ping 数据
     * @param manual 手动刷新：绕过冷却，并取消进行中的刷新后立即重启
     * @param onFinished 刷新结束回调（主线程），参数为是否成功
     */
    fun refresh(
        cardId: String,
        manual: Boolean = false,
        onFinished: ((Boolean) -> Unit)? = null
    ) {
        synchronized(this) {
            if (_cards.value.none { it.record.cardId == cardId }) return
            val existing = pingJobs[cardId]
            if (existing != null) {
                if (!manual) return
                existing.cancel()
            } else if (!manual && System.currentTimeMillis() - (lastPingAt[cardId] ?: 0L) < REFRESH_COOLDOWN_MS) {
                return
            }
            _inFlight.update { it + cardId }
            pingJobs[cardId] = scope.launch {
                val success = ping(cardId)
                onFinished?.let { callback ->
                    withContext(Dispatchers.Main) { callback(success) }
                }
            }
        }
    }

    /**
     * 主页网格上报可见卡片集合的变化：
     * 新进入可见集合的卡片触发一次自动刷新（受冷却约束）
     */
    fun onVisibleCardsChanged(ids: Set<String>) {
        val entered: List<String>
        synchronized(this) {
            entered = ids.filterNot { it in visibleIds }
            visibleIds = ids
        }
        entered.forEach { refresh(it) }
    }

    /**
     * 执行一次 ping 并结算结果
     * @return 返回是否成功
     */
    private suspend fun ping(cardId: String): Boolean {
        val ip = _cards.value.firstOrNull { it.record.cardId == cardId }?.record?.cardId ?: return false

        val result = runCatching {
            pingServer(ServerAddress.parse(ip).resolve())
        }.onFailure { e ->
            Logger.warning(TAG, "Unable to ping server: $ip", e)
        }.getOrNull()

        val current = _cards.value.firstOrNull { it.record.cardId == cardId } ?: return false

        return synchronized(this) {
            //冷却锚点为 ping 完成时刻
            lastPingAt[cardId] = System.currentTimeMillis()
            pingJobs.remove(cardId)
            _inFlight.update { it - cardId }

            when (result) {
                null -> {
                    //静默刷新失败保留旧数据；仅在从未成功时落入 Failed
                    if (current.ping !is ServerCardPingStatus.Loaded) {
                        _cards.update { states ->
                            states.map {
                                if (it.record.cardId == cardId) {
                                    it.copy(ping = ServerCardPingStatus.Failed)
                                } else it
                            }
                        }
                    }
                    false
                }
                else -> {
                    //favicon 与缓存不同时更新记录并落盘
                    val newIcon = result.status.favicon?.icon
                    val changed = newIcon != null && !newIcon.contentEquals(current.record.icon)
                    val newRecord = if (changed) current.record.copy(icon = newIcon) else current.record
                    if (changed) saveRecords()
                    _cards.update { states ->
                        states.map {
                            if (it.record.cardId == cardId) {
                                it.copy(record = newRecord, ping = ServerCardPingStatus.Loaded(result))
                            } else it
                        }
                    }
                    true
                }
            }
        }
    }

    /** 重新解析全部卡片的绑定版本状态 */
    private fun recheckVersions() {
        scope.launch {
            _cards.value.forEach { state ->
                resolveBinding(state.record.cardId)
            }
        }
    }

    /**
     * 解析指定卡片的绑定版本状态并应用：
     * 绑定版本已失效时清除绑定，回到未指定（视为启动当前选中版本）
     */
    private suspend fun resolveBinding(cardId: String) {
        val state = _cards.value.firstOrNull { it.record.cardId == cardId } ?: return
        val status = resolveVersionStatus(state.record)
        when {
            status is VersionCardStatus.Deleted -> {
                synchronized(this) {
                    updateState(cardId) {
                        it.copy(
                            record = it.record.copy(dir = null, versionName = null),
                            versionStatus = null
                        )
                    } ?: return
                    saveRecords()
                }
            }
            status != state.versionStatus -> {
                //状态无变化时不发射，避免触发主页网格的无效重组
                _cards.update { states ->
                    states.map {
                        if (it.record.cardId == cardId) it.copy(versionStatus = status) else it
                    }
                }
            }
        }
    }

    /** 依据记录的绑定信息定位并加载版本，推导绑定版本的可用性状态；未指定绑定时为 null */
    private fun resolveVersionStatus(record: ServerCardRecord): VersionCardStatus? {
        if (record.versionMode != ServerCardVersionMode.SPECIFIC) return null
        val dir = record.dir ?: return null
        val versionName = record.versionName ?: return null
        if (dir is VersionCardDir.Custom && !hasStoragePermission) {
            return VersionCardStatus.Inaccessible
        }
        val gameHome = dir.resolveGameHome()
        if (!File(gameHome).exists()) return VersionCardStatus.Inaccessible
        val version = VersionsManager.loadVersion(gameHome, versionName)
            ?.takeIf { it.isValid() }
            ?: return VersionCardStatus.Deleted
        return VersionCardStatus.Available(version)
    }

    /** 更新指定卡片的状态，未找到卡片时返回 null */
    private fun updateState(cardId: String, transform: (ServerCardState) -> ServerCardState): ServerCardState? {
        var updated: ServerCardState? = null
        _cards.update { states ->
            states.map { state ->
                if (state.record.cardId == cardId) {
                    transform(state).also { updated = it }
                } else state
            }
        }
        return updated
    }

    private fun loadRecords(): List<ServerCardRecord> {
        val json = mmkv.decodeString(KEY_RECORDS, "").orEmpty()
        if (json.isBlank()) return emptyList()
        return runCatching {
            gson.fromJson(json, Array<ServerCardRecordDto>::class.java)
                .orEmpty()
                .map { it.toRecord() }
                .filter { it.cardId.isNotBlank() }
        }.onFailure { e ->
            Logger.error(TAG, "Failed to parse server card records.", e)
        }.getOrDefault(emptyList())
    }

    private fun saveRecords() {
        val json = gson.toJson(_cards.value.map { ServerCardRecordDto.fromRecord(it.record) })
        mmkv.encode(KEY_RECORDS, json)
    }
}
