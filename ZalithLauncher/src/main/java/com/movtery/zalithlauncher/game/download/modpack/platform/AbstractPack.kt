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

package com.movtery.zalithlauncher.game.download.modpack.platform

import android.content.Context
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TaskLogOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

/**
 * 整合包任务流构建器
 * @param platform 整合包格式（平台）
 */
abstract class AbstractPack(
    val platform: PackPlatform
) {
    /**
     * 获取最终安装的客户端版本名称（由用户编辑的）
     * 在安装结束后会尝试直接设定为这个版本
     */
    abstract fun getFinalClientName(): String

    /**
     * 构建安装任务阶段，在这里下载依赖文件、解压整合包内部的文件等
     * @param scope 在有生命周期管理的scope中执行安装任务
     * @param versionFolder 临时游戏版本文件夹，用于安装游戏文件
     * @param waitForVersionName 等待用户输入版本名称
     * @param addPhases 添加下一安装阶段
     * @param onClearTemp 已完成安装，开始清理缓存
     * @param logOutputHolder 安装 JVM 日志输出的容器
     */
    abstract fun buildTaskPhases(
        context: Context,
        scope: CoroutineScope,
        versionFolder: File,
        waitForVersionName: suspend (name: String) -> String,
        addPhases: (List<TaskFlowExecutor.TaskPhase>) -> Unit,
        onClearTemp: suspend () -> Unit,
        logOutputHolder: MutableStateFlow<TaskLogOutput?> = MutableStateFlow(null)
    ): List<TaskFlowExecutor.TaskPhase>
}