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

package com.movtery.zalithlauncher.game.version.mod.update

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.version.mod.ModProject
import com.movtery.zalithlauncher.game.version.mod.RemoteMod
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "ModUpdater"

/**
 * 全自动模组检查更新，自动检查传入的模组列表，检查并获取模组最新版本，匹配现有MC版本、现有模组加载器
 * @param mods                  需要检查并更新的模组列表
 * @param modsDir               当前模组文件夹
 * @param minecraft             MC主版本号，用于版本匹配
 * @param modLoader             模组加载器信息，用于版本匹配
 * @param waitForUserConfirm    等待用户确认更新模组的信息
 *                              如果用户觉得没有问题，须返回`true`；否则返回`false`，安装会取消
 */
class ModUpdater(
    private val mods: List<RemoteMod>,
    private val modsDir: File,
    private val minecraft: String,
    private val modLoader: ModLoader,
    scope: CoroutineScope,
    private val waitForUserConfirm: suspend (List<ModManifest>) -> List<SelectableModManifest>
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 需要检查新版本的模组列表
     */
    val dataList: MutableList<ModData> = mutableListOf()

    /**
     * 需要更新的模组列表
     */
    val allModsUpdate: MutableList<ModManifest> = mutableListOf()

    /**
     * 最终更新的模组列表
     */
    val finalModsUpdate: MutableList<ModManifest> = mutableListOf()

    /**
     * 开始更新所有已选择的模组
     * @param isRunning 正在运行中，拒绝此次更新请求时
     * @param onUpdated 已成功更新所有模组
     * @param onNoModUpdates 没有模组需要被更新时（所有选择的模组都是最新版）
     * @param onCancelled 更新任务被取消时
     * @param onError 更新模组时遇到错误
     */
    fun updateAll(
        isRunning: () -> Unit = {},
        onUpdated: () -> Unit,
        onNoModUpdates: () -> Unit,
        onCancelled: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            //正在更新中，阻止这次更新请求
            isRunning()
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getTaskPhases()
                taskExecutor.addPhases(tasks)
            },
            onComplete = onUpdated,
            onError = { th ->
                if (th is ModUpdateCancelledException) {
                    //用户已取消本次更新
                    onCancelled()
                    return@executePhasesAsync
                }
                if (th is NoModUpdatesAvailableException) {
                    //所有模组都是最新版本，不需要更新
                    onNoModUpdates()
                    return@executePhasesAsync
                }
                onError(th)
            }
        )
    }

    private suspend fun getTaskPhases() = withContext(Dispatchers.IO) {
        dataList.clear()
        allModsUpdate.clear()
        finalModsUpdate.clear()
        val tempModUpdaterDir = PathManager.DIR_CACHE_MOD_UPDATER

        listOf(
            buildPhase {
                //清理缓存
                addTask(
                    id = "ModUpdater.ClearTemp",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModUpdaterDir()
                    //清理后，重新创建缓存目录
                    tempModUpdaterDir.createDirAndLog()
                }

                //过滤模组数据
                addTask(
                    id = "ModUpdater.Filter",
                    title = androidText(R.string.mods_update_task_filter),
                    icon = R.drawable.ic_filter_alt_outlined
                ) { task ->
                    val totalSize = mods.size
                    val needLoad = mutableListOf<RemoteMod>()
                    val readyData = mutableListOf<ModData>()

                    mods.forEachIndexed { index, mod ->
                        val file = mod.localMod.file

                        task.updateProgress((index + 1f) / totalSize)
                        task.updateMessage(androidText(file.nameWithoutExtension))

                        // 过滤不可检查远端的模组
                        if (!mod.localMod.checkRemote) return@forEachIndexed

                        val modFile = mod.remoteFile
                        val project = mod.projectInfo

                        if (modFile != null && project != null) {
                            readyData += ModData(
                                file = file,
                                modFile = modFile,
                                project = project,
                                mcMod = mod.mcMod
                            )
                        } else {
                            needLoad += mod
                        }
                    }

                    val loadedData = if (needLoad.isNotEmpty()) {
                        needLoad.map { mod ->
                            async(Dispatchers.IO) {
                                loadModData(task, mod)
                            }
                        }.awaitAll()
                            .filterNotNull()
                            .also {
                                task.updateMessage(null)
                            }
                    } else {
                        emptyList()
                    }

                    dataList.addAll(readyData)
                    dataList.addAll(loadedData)
                }

                // 检查更新
                addTask(
                    id = "ModUpdater.CheckUpdate",
                    title = androidText(R.string.mods_update_task_check_update),
                    icon = R.drawable.ic_list_alt_check_outlined
                ) { task ->
                    // 最大并发数为 5
                    val semaphore = Semaphore(5)
                    val completedCount = AtomicInteger(0)
                    val totalSize = dataList.size

                    val updateResults = dataList.map { data ->
                        async(Dispatchers.IO) {
                            semaphore.withPermit {
                                // 检查更新
                                val version = data.checkUpdate(minecraft, modLoader)

                                // 线程安全地更新进度条：以完成的数量来计算进度
                                val currentCompleted = completedCount.incrementAndGet()
                                task.updateProgress(currentCompleted.toFloat() / totalSize)
                                task.updateMessage(androidText(data.project.title))

                                // 如果有新版本，返回键值对；否则返回 null
                                if (version != null) data to version else null
                            }
                        }
                    }.awaitAll().filterNotNull() // 等待所有任务完成，并过滤掉不需要更新的 null 结果

                    updateResults.forEach { (data, version) ->
                        allModsUpdate.add(ModManifest(data, version))
                    }

                    if (allModsUpdate.isEmpty()) {
                        //所有模组都是最新版本，无需更新
                        throw NoModUpdatesAvailableException()
                    }
                }

                //等待用户确认模组更新
                addTask(
                    id = "ModUpdater.WaitForUser",
                    title = androidText(R.string.mods_update_task_wait_for_user),
                    icon = R.drawable.ic_schedule_outlined
                ) {
                    val finalList = waitForUserConfirm(allModsUpdate).toFinalList()
                    if (finalList.isEmpty()) {
                        // 用户取消了更新，或用户未选择要更新的模组
                        // 这里抛出取消异常，结束全部任务
                        throw ModUpdateCancelledException()
                    }
                    allModsUpdate.clear()
                    finalModsUpdate.addAll(finalList)
                }

                //下载新版本模组
                addTask(
                    id = "ModUpdater.UpdateMod",
                    title = androidText(R.string.mods_update_task_download)
                ) { task ->
                    val updater = ModVersionUpdater(
                        mods = finalModsUpdate.allNews(),
                        targetDir = tempModUpdaterDir
                    )
                    updater.startDownload(task)
                }

                //替换模组文件
                addTask(
                    id = " ModUpdater.ReplaceMod",
                    title = androidText(R.string.mods_update_task_replace),
                    icon = R.drawable.ic_build_outlined
                ) { task ->
                    val totalCount = finalModsUpdate.size
                    finalModsUpdate.forEachIndexed { index, entry ->
                        val oldMod = entry.data
                        val newVersion = entry.new

                        val oldFile = oldMod.file
                        val newFileName = newVersion.platformFileName()
                        val cacheFile = File(tempModUpdaterDir, newFileName)

                        task.updateProgress((index + 1).toFloat() / totalCount)
                        task.updateMessage(androidText(oldFile.name))

                        //确保所有文件都有效
                        if (modsDir.exists() && oldFile.exists() && cacheFile.exists()) {
                            FileUtils.deleteQuietly(oldFile)
                            val newFile = File(modsDir, newFileName)
                            cacheFile.copyTo(target = newFile, overwrite = true)
                        }
                    }
                }

                //清理缓存
                addTask(
                    id = "ModUpdater.ClearTempEnds",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModUpdaterDir()
                }
            }
        )
    }

    private suspend fun loadModData(
        task: Task,
        mod: RemoteMod
    ): ModData? {
        val file = mod.localMod.file

        val modFile = mod.remoteFile ?: runCatching {
            task.updateMessage(androidText(
                R.string.mods_update_task_loading, file.name
            ))
            mod.loadRemoteFile()
        }.onFailure {
            Logger.warning(TAG, "Failed to load remote mod version", it)
        }.getOrNull() ?: return null

        val project = mod.projectInfo ?: runCatching {
            task.updateMessage(androidText(
                R.string.mods_update_task_loading, file.name
            ))

            val project = getProjectByVersion(modFile.projectId, modFile.platform)
            ModProject(
                id = project.platformId(),
                platform = project.platform(),
                iconUrl = project.platformIconUrl(),
                title = project.platformTitle(),
                slug = project.platformSlug()
            )
        }.onFailure {
            Logger.warning(TAG, "Failed to load remote project", it)
        }.getOrNull() ?: return null

        return ModData(
            file = file,
            modFile = modFile,
            project = project,
            mcMod = mod.mcMod
        )
    }

    fun cancel() {
        taskExecutor.cancel()
    }

    /**
     * 清理临时模组更新缓存目录
     */
    private suspend fun clearTempModUpdaterDir() = withContext(Dispatchers.IO) {
        PathManager.DIR_CACHE_MOD_UPDATER.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            Logger.info(TAG, "Temporary mod updater directory cleared.")
        }
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        Logger.debug(TAG, "Created directory: $this")
        return this
    }
}