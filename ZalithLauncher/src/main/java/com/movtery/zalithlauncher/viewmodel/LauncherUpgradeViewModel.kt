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

package com.movtery.zalithlauncher.viewmodel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.path.URL_PROJECT_INFO
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.ui.upgrade.UpgradeDialog
import com.movtery.zalithlauncher.ui.upgrade.UpgradeFilesDialog
import com.movtery.zalithlauncher.upgrade.GithubContentApi
import com.movtery.zalithlauncher.upgrade.RemoteData
import com.movtery.zalithlauncher.upgrade.TooFrequentOperationException
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.withRetry
import com.movtery.zalithlauncher.utils.string.decodeBase64
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "LauncherUpgradeVM"

sealed interface LauncherUpgradeOperation {
    data object None : LauncherUpgradeOperation
    /** 已检查到启动器存在新版本，展示更新信息 */
    data class Upgrade(val data: RemoteData) : LauncherUpgradeOperation
    /** 选择要安装的安装包文件 */
    data class SelectApk(val data: RemoteData) : LauncherUpgradeOperation
    /** 打开网盘分享 */
    data class OpenCloudDrive(val cloudDrive: RemoteData.CloudDrive) : LauncherUpgradeOperation
}

/**
 * 最新版本的信息获取源
 */
private const val LATEST_VERSION = "latest_version_md.json"
private const val LATEST_API_URL = "$URL_PROJECT_INFO/$LATEST_VERSION"
private const val LATEST_API_CHINESE_URL = "https://repo.miawa.cn/zalith-info/v2/$LATEST_VERSION"

/**
 * 用于记录启动器更新 ViewModel
 */
class LauncherUpgradeViewModel: ViewModel() {
    var operation by mutableStateOf<LauncherUpgradeOperation>(LauncherUpgradeOperation.None)

    private val checkMutex = Mutex()

    /**
     * 检查是否在限频时间内
     * @param time 限频时间（毫秒）
     * @param lastCheckTime 上次检查的时间戳
     */
    private fun isWithinRateLimit(
        time: Long,
        lastCheckTime: Long
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        if (lastCheckTime > currentTime) {
            //用户调整到了未来的时间，无法正常判断
            //直接允许进行检查
            return false
        }
        return currentTime - lastCheckTime < time
    }

    /**
     * 更新最后一次检查的时间
     */
    private fun updateLastCheckTime() {
        AllSettings.lastUpgradeCheck.save(System.currentTimeMillis())
    }

    /**
     * 在启动时，快速完成所有的检查
     */
    fun checkOnAppStart(
        onIsLatest: suspend () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (
                isWithinRateLimit(
                    time = TimeUnit.HOURS.toMillis(1L),
                    lastCheckTime = AllSettings.lastUpgradeCheck.getValue()
                )
            ) {
                Logger.info(TAG, "App start check: Within rate limit, skipping")
                return@launch
            }

            val data = fetchRemoteData()
            if (data != null) {
                checkForUpgrade(
                    data = data,
                    lastIgnored = AllSettings.lastIgnoredVersion.getValue(),
                    ignoreDismissedVersions = true, //启动时检查忽略用户已忽略的版本
                    onUpgrade = { data ->
                        operation = LauncherUpgradeOperation.Upgrade(data)
                    },
                    onIsLatest = onIsLatest
                )
            }
            updateLastCheckTime()
        }
    }

    /**
     * 用户在设置内手动点击检查更新
     * @param onInProgress 准备检查更新
     * @param onIsLatest 当前启动器是最新版
     */
    suspend fun checkManually(
        onInProgress: suspend () -> Unit = {},
        onIsLatest: suspend () -> Unit = {}
    ): Boolean {
        return checkMutex.withLock {
            if (
                isWithinRateLimit(
                    time = TimeUnit.SECONDS.toMillis(5L),
                    lastCheckTime = AllSettings.lastUpgradeCheck.getValue()
                )
            ) throw TooFrequentOperationException()

            onInProgress()

            val data = fetchRemoteData()
            if (data != null) {
                checkForUpgrade(
                    data = data,
                    lastIgnored = AllSettings.lastIgnoredVersion.getValue(),
                    ignoreDismissedVersions = false,
                    onUpgrade = { data ->
                        operation = LauncherUpgradeOperation.Upgrade(data)
                    },
                    onIsLatest = onIsLatest
                )
            }
            updateLastCheckTime()
            data != null
        }
    }

    /**
     * 从远端获取最新的启动器信息
     */
    private suspend fun fetchRemoteData(): RemoteData? {
        return withContext(Dispatchers.IO) {
            runCatching {
                withRetry(logTag = "LauncherUpgrade", maxRetries = 2) {
                    //获取最新的启动器信息
                    val api = GLOBAL_CLIENT.get(LATEST_API_URL).safeBodyAsJson<GithubContentApi>()
                    //需要Base64解密
                    val contentString = decodeBase64(api.content)
                    GLOBAL_JSON.decodeFromString(RemoteData.serializer(), contentString)
                }
            }.getOrElse { e ->
                if (Locale.getDefault().language == "zh") {
                    runCatching {
                        Logger.info(TAG, "Check for updates in the Chinese region.")
                        //在中国地区，可能因为无法访问 Github API 导致获取更新信息失败
                        withRetry(logTag = "LauncherUpgrade_Chinese", maxRetries = 2) {
                            GLOBAL_CLIENT.get(LATEST_API_CHINESE_URL).safeBodyAsJson<RemoteData>()
                        }
                    }.getOrElse { e ->
                        Logger.warning(TAG, "Failed to check for launcher upgrade!", e)
                        null
                    }
                } else {
                    Logger.warning(TAG, "Failed to check for launcher upgrade!", e)
                    null
                }
            }
        }
    }

    /**
     * 检查启动器是否需要更新
     * @param lastIgnored 上次弹出更新弹窗时，用户所忽略的版本号
     * @param ignoreDismissedVersions 是否忽略用户已忽略的版本
     * @param onUpgrade 发现需要更新时调用
     * @param onIsLatest 当前已是最新版本时
     */
    private suspend fun checkForUpgrade(
        data: RemoteData,
        lastIgnored: Int?,
        ignoreDismissedVersions: Boolean,
        onUpgrade: suspend (RemoteData) -> Unit,
        onIsLatest: suspend () -> Unit = {}
    ) {
        val currentVersionCode = BuildConfig.VERSION_CODE
        if (currentVersionCode < data.code) {
            //启动器为旧版本
            when {
                ignoreDismissedVersions && lastIgnored == data.code -> {
                    //忽略这次更新
                    Logger.info(TAG, "Launcher update detected: $currentVersionCode -> ${data.code}, but ignored by user")
                }
                else -> {
                    //弹出更新弹窗
                    Logger.info(TAG, "Launcher update detected: $currentVersionCode -> ${data.code}, dialog shown to user")
                    onUpgrade(data)
                }
            }
        } else {
            Logger.info(TAG, "Launcher is running the latest version: $currentVersionCode")
            onIsLatest()
        }
    }
}

@Composable
fun LauncherUpgradeOperation(
    operation: LauncherUpgradeOperation,
    onChanged: (LauncherUpgradeOperation) -> Unit,
    onIgnoredClick: (code: Int) -> Unit,
    onLinkClick: (String) -> Unit
) {
    when (operation) {
        is LauncherUpgradeOperation.None -> {}
        is LauncherUpgradeOperation.Upgrade -> {
            UpgradeDialog(
                data = operation.data,
                onDismissRequest = {
                    onChanged(LauncherUpgradeOperation.None)
                },
                onFilesClick = {
                    onChanged(LauncherUpgradeOperation.SelectApk(operation.data))
                },
                onIgnored = {
                    onIgnoredClick(operation.data.code)
                },
                onLinkClick = onLinkClick,
                onCloudDriveClick = { cloudDrive ->
                    onChanged(LauncherUpgradeOperation.OpenCloudDrive(cloudDrive))
                }
            )
        }
        is LauncherUpgradeOperation.SelectApk -> {
            UpgradeFilesDialog(
                data = operation.data,
                onDismissRequest = {
                    onChanged(LauncherUpgradeOperation.None)
                },
                onFileSelected = { file ->
                    onLinkClick(file.uri)
                    onChanged(LauncherUpgradeOperation.None)
                }
            )
        }
        is LauncherUpgradeOperation.OpenCloudDrive -> {
            val current by remember(operation) {
                mutableStateOf<RemoteData.CloudDrive.Link?>(null)
            }
            SimpleListDialog(
                title = stringResource(R.string.upgrade_cloud_drive),
                items = operation.cloudDrive.links,
                onItemSelected = { link ->
                    onLinkClick(link.link)
                },
                onDismissRequest = {
                    onChanged(LauncherUpgradeOperation.None)
                },
                current = current,
                itemLayout = { item, isCurrent, onClick ->
                    CloudDriveLayout(
                        link = item,
                        selected = isCurrent,
                        onClick = onClick
                    )
                },
                showConfirm = true,
                confirmText = {
                    MarqueeText(text = stringResource(R.string.generic_confirm))
                }
            )
        }
    }
}


@Composable
private fun CloudDriveLayout(
    link: RemoteData.CloudDrive.Link,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Column(
            modifier = Modifier.alpha(if (enabled) 1.0f else DisabledAlpha),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //网盘名称
            MarqueeText(
                modifier = Modifier.fillMaxWidth(),
                text = link.name,
                style = MaterialTheme.typography.labelMedium
            )
            //网盘链接
            MarqueeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.7f),
                text = link.link,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}