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

package com.movtery.zalithlauncher.filemanager.viewmodel.controllers

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.config.FmConfig
import com.movtery.zalithlauncher.filemanager.events.FileManagerEvent
import com.movtery.zalithlauncher.filemanager.events.FileManagerEventBus
import com.movtery.zalithlauncher.filemanager.logic.AccessScope
import com.movtery.zalithlauncher.filemanager.logic.FileManagerLogic
import com.movtery.zalithlauncher.filemanager.logic.FmResult
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.logic.entry.FmListResult
import com.movtery.zalithlauncher.filemanager.os.FmLog
import com.movtery.zalithlauncher.filemanager.viewmodel.FmSnackbar
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import com.movtery.zalithlauncher.filemanager.viewmodel.RawList
import com.movtery.zalithlauncher.filemanager.viewmodel.SortConfig
import com.movtery.zalithlauncher.filemanager.viewmodel.applyVisibility
import com.movtery.zalithlauncher.filemanager.viewmodel.entryPathKey
import com.movtery.zalithlauncher.filemanager.viewmodel.persist
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "FmBrowse"

/** 浏览任务被其它任务占用时的重试间隔 */
private const val BUSY_RETRY_DELAY_MS = 150L

/**
 * 浏览 / 导航控制器，负责目录列表刷新与导航历史管理
 * 并集中管理目录刷新这一条协程任务的生命周期
 */
class BrowseController(
    private val logic: FileManagerLogic,
    private val scope: AccessScope,
    private val store: FmStateStore,
    private val coroutineScope: CoroutineScope
) {
    private val refreshLock = Any()

    private var refreshJob: Job? = null

    /** 待处理的最新刷新目标：快速导航时中间目标被覆盖合并（仅允许在 [refreshLock] 内访问） */
    private var pendingTarget: Path? = null

    /**
     * 请求刷新目录：合并到目标流水线，最新目标必定被处理。
     * @param target 目标目录；null 表示刷新当前路径
     */
    fun refreshDir(target: Path? = null) {
        val t = target ?: store.history.currentPath
        synchronized(refreshLock) {
            pendingTarget = t
            store.updateState {
                it.copy(currentDir = t, refreshing = true)
            }
            if (refreshJob?.isActive == true) return
            refreshJob = coroutineScope.launch(Dispatchers.IO) { refreshPipeline() }
        }
    }

    /** 操作出错后刷新当前目录内容，使列表反映磁盘真实状态 */
    fun refreshCurrentDir() = refreshDir(store.history.currentPath)

    /** 目标刷新流水线：顺序处理刷新期间累积的最新目标。 */
    private suspend fun refreshPipeline() {
        while (true) {
            val target = takePendingTarget()
            if (target == null) {
                val shouldExit = synchronized(refreshLock) {
                    if (pendingTarget == null) {
                        refreshJob = null
                        true
                    } else {
                        false
                    }
                }
                if (shouldExit) {
                    store.updateState {
                        it.copy(refreshing = false)
                    }
                    return
                }
                continue
            }
            try {
                refreshOnce(target)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                FmLog.warn(TAG, "refresh pipeline error for $target", e)
                store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_error_browse_failed)))
            }
        }
    }

    /** 原子取出最新待刷新目标 */
    private fun takePendingTarget(): Path? = synchronized(refreshLock) {
        val t = pendingTarget
        pendingTarget = null
        t
    }

    private suspend fun refreshOnce(target: Path) {
        FmLog.info(TAG, "refreshList target=$target")
        // 任务被占用时等待重试，动画由 UI 层按内容状态驱动，流水线只负责数据
        val result = browseAwaitingMutex(target) ?: return

        if (target != store.history.currentPath) {
            FmLog.info(TAG, "browse result stale, drop: $target")
            return
        }

        when (result) {
            is FmResult.Ok -> {
                // 应用前二次校验，等待 / 应用间隙用户可能已导航走，过期结果不写入状态
                if (result.value.currentDir == store.history.currentPath) {
                    FmLog.info(TAG, "browse ok: entries=${result.value.entries.size}, current=${result.value.currentDir}")
                    updateList(result.value)
                } else {
                    FmLog.info(TAG, "browse result stale at apply, drop: ${result.value.currentDir}")
                }
            }
            is FmResult.Failed -> {
                FmLog.warn(TAG, "browse failed", result.error)
                store.emitSnackbar(FmSnackbar(result.error.message ?: store.stringResolver(R.string.fm_error_browse_failed)))
                // 导航失败，恢复显示最后成功的目录，
                // 避免路径栏停留在失效路径、内容与路径不一致
                val lastGood = store.stateValue().rawList?.currentDir
                if (lastGood != null && target != lastGood) {
                    FmLog.info(TAG, "browse failed for navigation target, revert to: $lastGood")
                    store.updateState { it.copy(currentDir = lastGood) }
                }
            }
            FmResult.Cancelled -> {
                FmLog.warn(TAG, "browse cancelled")
            }
            FmResult.Rejected -> {
                FmLog.warn(TAG, "browse rejected (busy)")
                store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
            }
        }
    }

    private suspend fun browseAwaitingMutex(target: Path): FmResult<FmListResult>? {
        var result = logic.browse(target)
        while (result is FmResult.Rejected && currentCoroutineContext().isActive) {
            if (target != store.history.currentPath) {
                FmLog.info(TAG, "browse abandoned (target changed): $target")
                return null
            }
            delay(BUSY_RETRY_DELAY_MS.milliseconds)
            result = logic.browse(target)
        }
        return result
    }

    /** 导航到目录（计入历史） */
    fun navigateTo(path: Path) {
        val safe = logic.validateTarget(path) ?: run {
            store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_error_invalid_target)))
            return
        }
        store.clearSelectionAndExitMulti()
        store.history.navigate(safe)
        syncNavState()
        refreshDir(safe)
    }

    /** 点击目录条目进入目录 */
    fun enterDirectory(entry: FmEntry) {
        if (!entry.isDirectory) return
        navigateTo(entry.path)
    }

    /**
     * 后退
     * @return true 表示已移动
     */
    fun back(): Boolean {
        val p = store.history.back() ?: return false
        store.clearSelectionAndExitMulti()
        syncNavState()
        refreshDir(p)
        return true
    }

    /**
     * 前进
     * @return true 表示已移动
     */
    fun forward(): Boolean {
        val p = store.history.forward() ?: return false
        store.clearSelectionAndExitMulti()
        syncNavState()
        refreshDir(p)
        return true
    }

    /**
     * 返回上一级目录
     * @return true 表示已移动
     */
    fun goParent(): Boolean {
        val parent = store.history.currentPath.parent ?: return false
        if (!scope.isUnder(parent)) return false
        navigateTo(parent)
        return true
    }

    /**
     * 跳转目录
     * 目标限制在可访问范围内，越界或不存在时提示并拒绝
     */
    fun jumpTo(targetInput: String) {
        val candidate = Paths.get(targetInput).normalize().toAbsolutePath()
        val safe = logic.validateTarget(candidate)
        if (safe == null) {
            store.emitError(store.stringResolver(R.string.fm_error_out_of_scope))
            return
        }
        navigateTo(safe)
    }

    /**
     * 提交跳转目录
     * 校验失败时返回 false
     * @return 跳转是否发起成功
     */
    fun submitJump(targetInput: String): Boolean {
        val candidate = try {
            Paths.get(targetInput).normalize().toAbsolutePath()
        } catch (_: Exception) {
            store.emitError(store.stringResolver(R.string.fm_jump_invalid))
            return false
        }
        if (logic.validateTarget(candidate) == null) {
            store.emitError(store.stringResolver(R.string.fm_jump_invalid))
            return false
        }
        jumpTo(candidate.toString())
        return true
    }

    /** 选择某条搜索结果：跳转到其所在目录并定位该条目 */
    fun navigateToSearchHit(hitPath: Path) {
        val target = hitPath.parent ?: store.history.currentPath
        val safe = logic.validateTarget(target) ?: return
        store.clearSelectionAndExitMulti()
        store.history.navigate(safe)
        syncNavState()
        store.updateState { it.copy(locateHighlightPath = hitPath) }
        store.dismissDialog()
        refreshDir(safe)
    }

    /** 派发文件系统变更事件并刷新当前目录 */
    fun notifyFileChanged(event: FileManagerEvent) {
        FileManagerEventBus.dispatch(event)
        refreshCurrentDir()
    }

    /**
     * 目录被删除后清理导航历史
     */
    fun pruneHistory(deleted: Path) {
        store.history.pruneDeleted(deleted)
        syncNavState()
    }

    /** 应用排序配置并重算可见列表 */
    fun setSortConfig(config: SortConfig) {
        config.persist()
        val newVisible = applyVisibility(store.stateValue().rawList?.entries ?: emptyList(), config, store.stateValue().showHidden)
        store.updateState { it.copy(sortConfig = config, visibleEntries = newVisible) }
        recountVisible(newVisible)
        reconcileSelectionWith(newVisible)
    }

    /** 切换“显示隐藏文件”开关并重算可见列表 */
    fun toggleHidden() {
        val show = !store.stateValue().showHidden
        FmConfig.setShowHidden(show)
        val newVisible = applyVisibility(store.stateValue().rawList?.entries ?: emptyList(), store.stateValue().sortConfig, show)
        store.updateState { it.copy(showHidden = show, visibleEntries = newVisible) }
        recountVisible(newVisible)
        reconcileSelectionWith(newVisible)
    }

    private fun syncNavState() {
        store.updateState {
            it.copy(
                canNavigateBack = store.history.canBack,
                canNavigateForward = store.history.canForward
            )
        }
    }

    private fun updateList(result: FmListResult) {
        val raw = RawList.of(result)
        val visible = applyVisibility(raw.entries, store.stateValue().sortConfig, store.stateValue().showHidden)
        // 保留选中集合中仍存在的项（按路径字符串比较，规避 Path 实例 hashCode 差异）
        val present = visible.mapTo(mutableSetOf()) { entryPathKey(it) }
        val newSelection = store.selection.intersect(present)
        store.setSelection(newSelection, newSelection.isNotEmpty())
        store.updateState {
            it.copy(
                rawList = raw,
                visibleEntries = visible
            )
        }
        recountVisible(visible)
    }

    private fun recountVisible(visible: List<FmEntry>) {
        val folders = visible.count { it.isDirectory }
        val files = visible.count { it.isFile }
        store.updateState {
            it.copy(folderCount = folders, fileCount = files)
        }
    }

    private fun reconcileSelectionWith(visible: List<FmEntry>) {
        val present = visible.mapTo(mutableSetOf()) { entryPathKey(it) }
        if (store.selection.any { it !in present }) {
            val newSelection = store.selection.intersect(present)
            store.setSelection(newSelection, newSelection.isNotEmpty())
        }
    }
}
