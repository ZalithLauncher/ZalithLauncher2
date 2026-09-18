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

import com.movtery.zalithlauncher.filemanager.logic.ops.DirectoryScanner
import com.movtery.zalithlauncher.filemanager.os.FmLog
import com.movtery.zalithlauncher.filemanager.viewmodel.DirScanUiState
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.Path

private const val TAG = "FmDirectoryScan"

/** 目录属性扫描控制器 */
class DirectoryScanController(
    private val store: FmStateStore,
    private val coroutineScope: CoroutineScope
) {
    private var scanJob: Job? = null

    /** 启动对 [path] 的异步目录扫描 */
    fun startDirectoryScan(path: Path) {
        scanJob?.cancel()
        store.setDirScan(DirScanUiState(running = true, stats = null))
        val job = coroutineScope.launch(Dispatchers.IO) {
            val scanScope = this
            var lastUpdateMs = 0L
            try {
                val stats = DirectoryScanner.scan(
                    root = path,
                    onProgress = { s, _ ->
                        val now = System.currentTimeMillis()
                        if (now - lastUpdateMs >= 500) { // 0.5s 更新一次
                            lastUpdateMs = now
                            store.setDirScan(DirScanUiState(running = true, stats = s))
                        }
                    },
                    checkCancelled = { !scanScope.isActive }
                )
                store.setDirScan(DirScanUiState(running = false, stats = stats))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                FmLog.warn(TAG, "Directory scan failed: $path", e)
                store.setDirScan(DirScanUiState(running = false, stats = null))
            }
        }
        scanJob = job
    }

    fun stopDirectoryScan() {
        scanJob?.cancel()
        scanJob = null
        store.setDirScan(null)
    }
}
