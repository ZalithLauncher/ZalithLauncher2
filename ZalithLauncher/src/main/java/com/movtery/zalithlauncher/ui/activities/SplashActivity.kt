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

package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.Components
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.components.UnpackComponentsTask
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.components.jre.UnpackJnaTask
import com.movtery.zalithlauncher.components.jre.UnpackJreTask
import com.movtery.zalithlauncher.context.readAssetFile
import com.movtery.zalithlauncher.legal.AvailableAgreement
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.launcherMMKV
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.splash.SplashOperation
import com.movtery.zalithlauncher.ui.screens.splash.SplashScreen
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.ui.theme.showThemed
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "SplashActivity"

const val EXTRA_IMPORT_ACTION = "EXTRA_IMPORT_ACTION"
const val EXTRA_IMPORT_URI    = "EXTRA_IMPORT_URI"
const val EXTRA_IMPORT_TYPE   = "EXTRA_IMPORT_TYPE"

const val IMPORT_TYPE_MODPACK = "modpack"
const val IMPORT_TYPE_CONTROLS = "controls"
const val IMPORT_TYPE_UNKNOWN = "unknown"

private const val WELCOME_VERSION_KEY = "welcomeVersionKey"
private const val WELCOME_VERSION_VALUE = 1
private const val AGREEMENT_DATA_PATH = "legal/data.json"
private const val ACCEPTED_AGREEMENT_VERSION_KEY = "acceptedAgreementVersion"

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseAppCompatActivity() {
    private val unpackItems: MutableList<InstallableItem> = ArrayList()
    private val finishedTaskCount = AtomicInteger(0)
    private var unpackJob: Job? = null

    private var pendingImportIntent: Intent? = null

    private val backStackViewModel: SplashBackStackViewModel by viewModels()
    private val _availableAgreement = MutableStateFlow<AvailableAgreement?>(null)
    private val availableAgreement = _availableAgreement.asStateFlow()

    private fun switchScreen(screen: SplashOperation) {
        backStackViewModel.splashScreen.backStack.navigateOnce(screen)
    }

    private suspend fun loadAvailableAgreement(): AvailableAgreement? = runCatching {
        withContext(Dispatchers.IO) {
            GLOBAL_JSON.decodeFromString(
                AvailableAgreement.serializer(),
                this@SplashActivity.readAssetFile(AGREEMENT_DATA_PATH)
            )
        }
    }.onFailure {
        Logger.warning(TAG, "Failed to load agreement data", it)
    }.getOrNull()

    private fun requiresAgreementConsent(): Boolean {
        val version = availableAgreement.value?.version ?: return false
        return launcherMMKV().getString(ACCEPTED_AGREEMENT_VERSION_KEY, null) != version
    }

    private fun acceptCurrentAgreement() {
        availableAgreement.value?.version?.let {
            launcherMMKV().putString(ACCEPTED_AGREEMENT_VERSION_KEY, it).apply()
        }
    }

    private fun showAgreementUpdateDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.agreement_update_title)
            .setMessage(R.string.agreement_update_message)
            .setPositiveButton(R.string.generic_confirm) { dialog, _ -> dialog.dismiss() }
            .showThemed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        initUnpackItems()

        setContent {
            ZalithLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor(),
                    contentColor = onBackgroundColor()
                ) {
                    SplashScreen(
                        startAllTask = { startAllTask() },
                        onWelcomeFinished = {
                            launcherMMKV().putInt(WELCOME_VERSION_KEY, WELCOME_VERSION_VALUE).apply()
                            acceptCurrentAgreement()
                            switchScreen(SplashOperation.Loading)
                            checkTasksToMain()
                        },
                        availableAgreement = availableAgreement,
                        unpackItems = unpackItems,
                        screenViewModel = backStackViewModel
                    )
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            switchScreen(SplashOperation.Loading)

            withContext(Dispatchers.IO) {
                checkAllTask()
                val agreement = loadAvailableAgreement()
                _availableAgreement.update { agreement }
            }

            // 检查协议同意情况
            val acceptedAgreementVersion = launcherMMKV().getString(ACCEPTED_AGREEMENT_VERSION_KEY, null)
            val agreementConsentRequired = requiresAgreementConsent()
            if (
                agreementConsentRequired ||
                launcherMMKV().getInt(WELCOME_VERSION_KEY, 0) < WELCOME_VERSION_VALUE
            ) {
                switchScreen(SplashOperation.Welcome)
                // 曾同意过旧版的协议，发布对话框提醒
                if (acceptedAgreementVersion != null && agreementConsentRequired) {
                    showAgreementUpdateDialog()
                }
            } else {
                checkTasksToMain()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        //若依赖未完成，暂存外部导入，待依赖完成后处理
        if (!areAllTasksFinished()) {
            if (isImportIntent(intent) && !isLauncherIntent(intent)) {
                pendingImportIntent = intent
                Logger.info(TAG, "Import intent received but dependencies are not ready, deferring.")
            }
            return
        }

        if (isImportIntent(intent) && !isLauncherIntent(intent)) {
            handleImportIntent(intent)
            finish()
        }
    }

    private fun initUnpackItems() {
        Components.entries.forEach { component ->
            val task = UnpackComponentsTask(this@SplashActivity, component)
            unpackItems.add(
                InstallableItem(
                    component.displayName,
                    getString(component.summary),
                    task
                )
            )
        }
        Jre.entries.forEach { jre ->
            val task = UnpackJreTask(this@SplashActivity, jre)
            if (!task.isCheckFailed()) {
                unpackItems.add(
                    InstallableItem(
                        jre.jreName,
                        getString(jre.summary),
                        task
                    )
                )
            }
        }
        unpackItems.add(
            InstallableItem(
                "JNA",
                getString(R.string.unpack_screen_jna),
                UnpackJnaTask(this@SplashActivity)
            )
        )
        unpackItems.sort()
    }

    private suspend fun checkAllTask() {
        //检查应用 assets 目录
        listAssetsPath("runtimes").forEach { filePath ->
            Logger.info(TAG, "The launcher contains the runtime environment: $filePath")
        }

        unpackItems.forEach { item ->
            val state = item.task.checkState()
            item.updateState(state)
            if (state == InstallableItem.State.FINISHED) {
                finishedTaskCount.incrementAndGet()
            }
        }
    }

    private fun listAssetsPath(root: String): List<String> {
        return buildList {
            val rootFiles = runCatching {
                assets.list(root)?.takeIf { it.isNotEmpty() }
            }.getOrNull()
            if (rootFiles != null) {
                rootFiles.forEach { child ->
                    val childPath = "$root/$child"
                    val childFiles = runCatching {
                        assets.list(childPath)?.takeIf { it.isNotEmpty() }
                    }.getOrNull()

                    if (childFiles != null) {
                        addAll(listAssetsPath(childPath))
                    } else {
                        add(childPath)
                    }
                }
            } else {
                add(root)
            }
        }
    }

    private fun startAllTask() {
        if (unpackJob?.isActive == true) return
        switchScreen(SplashOperation.Unpacking)

        unpackJob = lifecycleScope.launch {
            val jobs = unpackItems
                .filter {
                    val state = it.state.value
                    state == InstallableItem.State.NOT_STARTED ||
                    state == InstallableItem.State.PENDING ||
                    state == InstallableItem.State.FAILED
                }
                .map { item ->
                    launch(Dispatchers.IO) {
                        item.updateState(InstallableItem.State.RUNNING)
                        item.task.updateMessage(null)
                        try {
                            item.task.run()
                            finishedTaskCount.incrementAndGet()
                            item.updateState(InstallableItem.State.FINISHED)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Logger.error(TAG, "Failed to install component: ${item.name}", e)
                            item.task.updateMessage(e.message ?: e.javaClass.simpleName)
                            item.updateState(InstallableItem.State.FAILED)
                        }
                    }
                }
            jobs.joinAll()
            if (areAllTasksFinished()) {
                AllSettings.javaRuntime.apply {
                    //检查并设置默认的Java环境
                    if (getValue().isEmpty()) save(Jre.JRE_8.jreName)
                }
                finishSplash()
            }
        }
    }

    private fun finishSplash() {
        val import = pendingImportIntent
            ?.takeIf { isImportIntent(it) && !isLauncherIntent(it) }
            ?: intent.takeIf { isImportIntent(it) && !isLauncherIntent(it) }

        if (import != null && handleImportIntent(import)) {
            finish()
        } else {
            swapToMain()
        }
    }

    private fun checkTasksToMain() {
        fun run(): Boolean {
            if (!areAllTasksFinished()) return false

            Logger.info(TAG, "All content that needs to be extracted is already the latest version!")

            if (isImportIntent(intent) && !isLauncherIntent(intent)) {
                val success = handleImportIntent(intent)
                if (success) {
                    finish()
                    return true
                }
            }

            swapToMain()
            return true
        }

        val screen = if (run()) {
            SplashOperation.Loading
        } else {
            SplashOperation.Unpacking
        }
        switchScreen(screen)
    }

    private fun swapToMain() {
        val forward = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(forward)
        finish()
    }



    private fun handleImportIntent(source: Intent): Boolean {
        if (!isImportIntent(source)) return false

        val uri: Uri? = when (source.action) {
            Intent.ACTION_SEND -> {
                source.clipData?.getItemAt(0)?.uri
                    ?: source.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            Intent.ACTION_VIEW -> source.data
            else -> null
        }

        if (uri == null) {
            Logger.warning(TAG, "No valid import Uri found")
            return false
        } else {
            try {
                //可持久化访问授权
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                Logger.warning(TAG, "No persistable permission granted for $uri")
            }
        }

        val forward = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra(EXTRA_IMPORT_ACTION, source.action)
            putExtra(EXTRA_IMPORT_URI, uri)
            putExtra(EXTRA_IMPORT_TYPE, resolveImportType(source))
        }

        startActivity(forward)
        return true
    }

    /**
     * 根据 AndroidManifest 内为 activity-alias 配置的 meta-data 来判断导入类型
     */
    private fun resolveImportType(intent: Intent): String {
        val comp = intent.component ?: return IMPORT_TYPE_UNKNOWN
        val info = packageManager.getActivityInfo(comp, PackageManager.GET_META_DATA)
        return info.metaData?.getString("import_type") ?: IMPORT_TYPE_UNKNOWN
    }

    private fun isLauncherIntent(intent: Intent?): Boolean {
        return intent != null && intent.action == Intent.ACTION_MAIN &&
                intent.categories?.contains(Intent.CATEGORY_LAUNCHER) == true
    }

    private fun isImportIntent(intent: Intent?): Boolean {
        val comp = intent?.component ?: return false
        val info = packageManager.getActivityInfo(comp, PackageManager.GET_META_DATA)
        return info.metaData?.getString("import_type") != null
    }

    private fun areAllTasksFinished(): Boolean {
        return finishedTaskCount.get() >= unpackItems.size
    }
}