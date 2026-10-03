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

package com.movtery.zalithlauncher.ui.screens.content.versions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersions
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.models.LaunchFor
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionInfo
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedLazyColumn
import com.movtery.zalithlauncher.ui.components.TextTransition
import com.movtery.zalithlauncher.ui.components.influencedByBackgroundColor
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.game.AddonList
import com.movtery.zalithlauncher.ui.screens.content.download.game.AddonState
import com.movtery.zalithlauncher.ui.screens.content.download.game.CleanroomList
import com.movtery.zalithlauncher.ui.screens.content.download.game.CurrentAddon
import com.movtery.zalithlauncher.ui.screens.content.download.game.FabricList
import com.movtery.zalithlauncher.ui.screens.content.download.game.ForgeList
import com.movtery.zalithlauncher.ui.screens.content.download.game.LegacyFabricList
import com.movtery.zalithlauncher.ui.screens.content.download.game.LoaderVerSupports
import com.movtery.zalithlauncher.ui.screens.content.download.game.NeoForgeList
import com.movtery.zalithlauncher.ui.screens.content.download.game.OptiFineList
import com.movtery.zalithlauncher.ui.screens.content.download.game.QuiltList
import com.movtery.zalithlauncher.ui.screens.content.download.game.SelectGameVersionHost
import com.movtery.zalithlauncher.ui.screens.content.download.game.SelectGameVersionScreen
import com.movtery.zalithlauncher.ui.screens.content.download.game.isOptiFineCompatibleWithForge
import com.movtery.zalithlauncher.ui.screens.content.download.game.rememberLoaderVerSupports
import com.movtery.zalithlauncher.ui.screens.content.download.game.runWithState
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTitledNavBackStack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModifyDiffs
import com.movtery.zalithlauncher.viewmodel.ModifyOperation
import com.movtery.zalithlauncher.viewmodel.ModifyPayload
import com.movtery.zalithlauncher.viewmodel.ModifyVersionViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "ModifyVersionScreen"

@Composable
fun ModifyVersionScreen(
    viewModel: ModifyVersionViewModel,
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    version: Version,
    eventViewModel: EventViewModel,
    onModify: (ModifyPayload) -> Unit
) {
    val backStack = rememberTitledNavBackStack(NormalNavKey.Versions.ModifyVersion)

    //用户选择的目标 Minecraft 版本；null 表示未变更
    //宿主在嵌套导航期间持续组合，选择结果跨主页与选择屏存活，退出修改标签页时销毁
    var selectedGameVersion by remember { mutableStateOf<String?>(null) }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = { onBack(backStack) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = rememberTransitionSpec(),
        popTransitionSpec = rememberTransitionSpec(),
        entryProvider = entryProvider {
            entry<NormalNavKey.Versions.ModifyVersion> {
                ModifyVersionContent(
                    mainScreenKey = mainScreenKey,
                    versionsScreenKey = versionsScreenKey,
                    version = version,
                    modifyViewModel = viewModel,
                    selectedGameVersion = selectedGameVersion,
                    onSelectGameVersion = {
                        backStack.navigateTo(NormalNavKey.Versions.SelectGameVersion)
                    },
                    onModify = onModify
                )
            }
            entry<NormalNavKey.Versions.SelectGameVersion> {
                SelectGameVersionScreen(
                    host = SelectGameVersionHost.VersionSettings(
                        mainScreenKey = mainScreenKey,
                        versionSettingsScreenKey = versionsScreenKey
                    ),
                    eventViewModel = eventViewModel
                ) { selected ->
                    selectedGameVersion = selected
                    onBack(backStack)
                }
            }
        }
    )
}

@Composable
private fun ModifyVersionContent(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    version: Version,
    modifyViewModel: ModifyVersionViewModel,
    selectedGameVersion: String?,
    onSelectGameVersion: () -> Unit = {},
    onModify: (ModifyPayload) -> Unit
) {
    val versionInfo = remember(version) {
        version.getVersionInfo() ?: error("Version information is unavailable: ${version.getVersionName()}")
    }
    val originalGameVersion = versionInfo.minecraftVersion
    val targetGameVersion = selectedGameVersion ?: originalGameVersion

    val loaderSupports = rememberLoaderVerSupports(targetGameVersion)
    val installedLoaders = rememberInstalledLoaders(version, versionInfo)

    val addonsVM = viewModel(
        key = version.toString() + "_ModifyVersion_" + targetGameVersion
    ) {
        ModifyAddonsViewModel(
            gameVersion = targetGameVersion,
            originalGameVersion = originalGameVersion,
            installedLoaders = installedLoaders,
            loaderSupports = loaderSupports
        )
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ModifyVersion, versionsScreenKey, false)
    ) { isVisible ->
        val scrollState = rememberLazyListState()
        AnimatedLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nonInteractiveScrollbar(
                    state = scrollState.scrollIndicatorState!!,
                    orientation = Orientation.Vertical,
                ),
            isVisible = isVisible,
            contentPadding = PaddingValues(all = 12.dp),
            state = scrollState,
        ) { scope ->
            animatedItem(scope) { yOffset ->
                MCVersionRow(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    current = originalGameVersion,
                    selected = targetGameVersion,
                    onClick = onSelectGameVersion
                )
            }

            // Minecraft 版本变更，给出提示
            if (targetGameVersion != originalGameVersion) {
                animatedItem(scope) { yOffset ->
                    ModifyTipItem(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        title = stringResource(R.string.versions_modify_mc_changed_title),
                        text = stringResource(R.string.versions_modify_mc_changed_tip)
                    )
                }
            }

            //未识别的已安装组件提示
            val unrecognizedInstalled = if (targetGameVersion == originalGameVersion) {
                // 切换 Minecraft 版本后视为移除，不再提示
                installedLoaders.filter { it.loader in addonsVM.unmatchedLoaders }
            } else {
                emptyList()
            }
            if (unrecognizedInstalled.isNotEmpty()) {
                animatedItem(scope) { yOffset ->
                    ModifyTipItem(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        title = stringResource(R.string.versions_modify_unrecognized_title),
                        text = unrecognizedInstalled.joinToString("、") { "${it.loader.displayName} ${it.version}" }
                    )
                }
            }
    
            animatedItem(scope) { yOffset ->
                OptiFineList(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    currentAddon = addonsVM.currentAddon,
                    addonList = addonsVM.addonList,
                    onValueChanged = { addonsVM.updateDiffs() },
                    onReload = { addonsVM.reloadOptiFine() }
                )
            }
    
            animatedItem(scope) { yOffset ->
                ForgeList(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    currentAddon = addonsVM.currentAddon,
                    addonList = addonsVM.addonList,
                    onValueChanged = { addonsVM.updateDiffs() },
                    onReload = { addonsVM.reloadForge() }
                )
            }
    
            if (loaderSupports.isNeoForgeSupports) {
                animatedItem(scope) { yOffset ->
                    NeoForgeList(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        currentAddon = addonsVM.currentAddon,
                        addonList = addonsVM.addonList,
                        onValueChanged = { addonsVM.updateDiffs() },
                        onReload = { addonsVM.reloadNeoForge() }
                    )
                }
            }
    
            if (loaderSupports.isCleanroomSupports) {
                animatedItem(scope) { yOffset ->
                    CleanroomList(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        currentAddon = addonsVM.currentAddon,
                        addonList = addonsVM.addonList,
                        onValueChanged = { addonsVM.updateDiffs() },
                        onReload = { addonsVM.reloadCleanroom() }
                    )
                }
            }
    
            if (loaderSupports.isFabricSupports) {
                animatedItem(scope) { yOffset ->
                    FabricList(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        currentAddon = addonsVM.currentAddon,
                        addonList = addonsVM.addonList,
                        onValueChanged = { addonsVM.updateDiffs() },
                        onReload = { addonsVM.reloadFabric() }
                    )
                }
            }
    
            if (loaderSupports.isLegacyFabricSupports) {
                animatedItem(scope) { yOffset ->
                    LegacyFabricList(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        currentAddon = addonsVM.currentAddon,
                        addonList = addonsVM.addonList,
                        onValueChanged = { addonsVM.updateDiffs() },
                        onReload = { addonsVM.reloadLegacyFabric() }
                    )
                }
            }
    
            if (loaderSupports.isQuiltSupports) {
                animatedItem(scope) { yOffset ->
                    QuiltList(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        currentAddon = addonsVM.currentAddon,
                        addonList = addonsVM.addonList,
                        onValueChanged = { addonsVM.updateDiffs() },
                        onReload = { addonsVM.reloadQuilt() }
                    )
                }
            }
    
            animatedItem(scope) { yOffset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        enabled = addonsVM.currentDiffs != null && modifyViewModel.installOperation is ModifyOperation.None,
                        onClick = {
                            val diffs = addonsVM.currentDiffs ?: return@Button
                            onModify(
                                ModifyPayload(
                                    info = GameDownloadInfo(
                                        gameVersion = targetGameVersion,
                                        customVersionName = version.getVersionName(),
                                        optifine = addonsVM.currentAddon.optifineVersion.value,
                                        forge = addonsVM.currentAddon.forgeVersion.value,
                                        neoforge = addonsVM.currentAddon.neoforgeVersion.value
                                            .takeIf { loaderSupports.isNeoForgeSupports },
                                        fabric = addonsVM.currentAddon.fabricVersion.value
                                            .takeIf { loaderSupports.isFabricSupports },
                                        legacyFabric = addonsVM.currentAddon.legacyFabricVersion.value
                                            .takeIf { loaderSupports.isLegacyFabricSupports },
                                        quilt = addonsVM.currentAddon.quiltVersion.value
                                            .takeIf { loaderSupports.isQuiltSupports },
                                        cleanroom = addonsVM.currentAddon.cleanroomVersion.value
                                            .takeIf { loaderSupports.isCleanroomSupports }
                                    ),
                                    currentVersion = version,
                                    diffs = diffs
                                )
                            )
                        }
                    ) {
                        Text(text = stringResource(R.string.versions_modify_start))
                    }
                }
            }
        }
    }
}

/**
 * 修改版本的附加内容加载器
 */
private class ModifyAddonsViewModel(
    /** 选定的目标 Minecraft 版本 */
    private val gameVersion: String,
    /** 版本当前的 Minecraft 版本 */
    private val originalGameVersion: String,
    /** 当前已安装的加载器信息 */
    private val installedLoaders: List<VersionInfo.LoaderInfo>,
    /** 加载器支持情况，决定初始化时拉取哪些加载器的版本列表 */
    loaderSupports: LoaderVerSupports
) : ViewModel() {
    val addonList = AddonList()
    val currentAddon = CurrentAddon()

    /** 当前修改内容，无变更时为 null */
    var currentDiffs by mutableStateOf<ModifyDiffs?>(null)
        private set

    /** 无法与版本列表匹配的已安装加载器，用户未主动选择新版本时保留原样 */
    var unmatchedLoaders by mutableStateOf<Set<ModLoader>>(emptySet())
        private set

    /** 已加载版本列表、等待预选的加载器（列表加载失败也计入） */
    private val pendingPreselects = linkedMapOf<ModLoader, () -> Unit>()

    /** 已完成预选尝试的加载器 */
    private val attemptedPreselects = mutableSetOf<ModLoader>()

    /** 会加载版本列表的加载器；其余已安装加载器（如 LiteLoader）无法参与预选 */
    private val reloadableLoaders = buildSet {
        add(ModLoader.OPTIFINE)
        add(ModLoader.FORGE)
        if (loaderSupports.isNeoForgeSupports) add(ModLoader.NEOFORGE)
        if (loaderSupports.isFabricSupports) add(ModLoader.FABRIC)
        if (loaderSupports.isLegacyFabricSupports) add(ModLoader.LEGACY_FABRIC)
        if (loaderSupports.isQuiltSupports) add(ModLoader.QUILT)
        if (loaderSupports.isCleanroomSupports) add(ModLoader.CLEANROOM)
    }

    private fun findInstalled(loader: ModLoader): VersionInfo.LoaderInfo? {
        return installedLoaders.firstOrNull { it.loader == loader }
    }

    private fun selectedVersionOf(loader: ModLoader): String? = when (loader) {
        ModLoader.OPTIFINE -> currentAddon.optifineVersion.value?.getAddonVersion()
        ModLoader.FORGE -> currentAddon.forgeVersion.value?.getAddonVersion()
        ModLoader.NEOFORGE -> currentAddon.neoforgeVersion.value?.getAddonVersion()
        ModLoader.FABRIC -> currentAddon.fabricVersion.value?.getAddonVersion()
        ModLoader.LEGACY_FABRIC -> currentAddon.legacyFabricVersion.value?.getAddonVersion()
        ModLoader.QUILT -> currentAddon.quiltVersion.value?.getAddonVersion()
        ModLoader.CLEANROOM -> currentAddon.cleanroomVersion.value?.getAddonVersion()
        else -> null
    }

    /**
     * 对比当前安装信息与用户的选择，生成变更内容
     */
    fun updateDiffs() {
        val diffs = buildList {
            if (gameVersion != originalGameVersion) {
                add(ModifyDiffs.McChange(original = originalGameVersion, updateTo = gameVersion))
            }

            //已安装加载器的变更情况
            //切换了 Minecraft 版本后旧加载器几乎必然无法匹配，未选择即视为移除，不再保留原样
            val keepUnmatched = gameVersion == originalGameVersion
            installedLoaders.forEach { installed ->
                val selected = selectedVersionOf(installed.loader)
                if (installed.loader !in attemptedPreselects) return@forEach
                //未识别的加载器默认保留原样，仅在用户主动选择非等价的新版本时生成变更
                if (keepUnmatched && installed.loader in unmatchedLoaders) {
                    if (selected != null && !selectedEqualsInstalled(installed, selected)) {
                        add(
                            ModifyDiffs.LoaderChange(
                                modloader = installed.loader,
                                original = installed.version,
                                updateTo = selected
                            )
                        )
                    }
                    return@forEach
                }
                when {
                    selected == null -> add(ModifyDiffs.LoaderRemove(modloader = installed.loader))
                    !selectedEqualsInstalled(installed, selected) -> add(
                        ModifyDiffs.LoaderChange(
                            modloader = installed.loader,
                            original = installed.version,
                            updateTo = selected
                        )
                    )
                }
            }

            //新安装的加载器
            listOf(
                ModLoader.OPTIFINE, ModLoader.FORGE, ModLoader.NEOFORGE,
                ModLoader.FABRIC, ModLoader.LEGACY_FABRIC, ModLoader.QUILT, ModLoader.CLEANROOM
            ).forEach { loader ->
                if (installedLoaders.none { it.loader == loader }) {
                    selectedVersionOf(loader)?.let { version ->
                        add(ModifyDiffs.LoaderInstall(modloader = loader, version = version))
                    }
                }
            }
        }

        currentDiffs = diffs.takeIf { it.isNotEmpty() }?.let { ModifyDiffs(list = it) }
    }

    /**
     * 判断选中的版本与已安装版本是否等价
     * OptiFine 的已安装信息可能来自库坐标或旧版短版本串，需要与版本列表条目做等价匹配
     */
    private fun selectedEqualsInstalled(
        installed: VersionInfo.LoaderInfo,
        selected: String
    ): Boolean {
        if (selected == installed.version) return true
        if (installed.loader != ModLoader.OPTIFINE) return false
        return addonList.optifineList?.any {
            it.getAddonVersion() == selected && it.matchesInstalledVersion(installed.version)
        } == true
    }

    /**
     * 独立加载单个加载器的版本列表，加载完成后刷新初始化状态与变更内容
     */
    private fun <T> launchAddonReload(
        updateState: (AddonState) -> Unit,
        fetch: suspend () -> T?,
        afterLoaded: (T?) -> Unit
    ) {
        viewModelScope.launch {
            runWithState(updateState, fetch).also { versions ->
                afterLoaded(versions)
                updateDiffs()
            }
        }
    }

    /**
     * 登记已加载版本列表的加载器，等待按优先级执行预选
     */
    private fun enqueuePreselect(loader: ModLoader, action: () -> Unit) {
        pendingPreselects[loader] = action
        attemptedPreselects.remove(loader) //重新加载后允许再次尝试
        runPendingPreselects()
    }

    /**
     * 按主加载器优先级依次执行预选,主加载器先完成预选，其余加载器为它让位
     * 更高优先级的加载器列表尚未加载完成时，低优先级的等待，保证预选结果确定
     */
    private fun runPendingPreselects() {
        installedLoaders
            .sortedBy {
                VersionInfo.PRIMARY_PRIORITY.indexOf(it.loader).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE
            }
            .forEach { installed ->
                val loader = installed.loader
                if (loader in attemptedPreselects) return@forEach
                val action = pendingPreselects[loader]
                if (action == null) {
                    if (loader in reloadableLoaders) return //更高优先级的列表仍在加载，等待
                    //无法加载版本列表的加载器，直接视为未识别
                    attemptedPreselects.add(loader)
                    unmatchedLoaders = unmatchedLoaders + loader
                    return@forEach
                }
                attemptedPreselects.add(loader)
                action()
            }
    }

    /**
     * 预选已安装的加载器版本
     * 仅在当前行未选择版本、通过校验且与已选择的其他加载器全部兼容时才填入；
     * 无法匹配时将加载器标记为未识别，修改时默认保留原样
     */
    private fun <T : AddonVersion> preselectInstalled(
        state: MutableState<T?>,
        loader: ModLoader,
        versions: List<T>?,
        installedVersion: String,
        matcher: (T, String) -> Boolean = { version, installed -> version.isVersion(installed) },
        validator: (T) -> Boolean = { true }
    ) {
        if (state.value != null) return

        val candidate = versions?.find { matcher(it, installedVersion) }
        if (
            candidate == null ||
            !validator(candidate) ||
            !currentAddon.isCompatibleWithSelection(candidate, loader, addonList)
        ) {
            unmatchedLoaders = unmatchedLoaders + loader
            return
        }

        state.value = candidate
        unmatchedLoaders = unmatchedLoaders - loader
    }

    fun reloadOptiFine() {
        launchAddonReload(
            { currentAddon.optifineState = it },
            { OptiFineVersions.fetchOptiFineList(gameVersion = gameVersion) }
        ) { versions ->
            addonList.optifineList = versions
            enqueuePreselect(ModLoader.OPTIFINE) {
                findInstalled(ModLoader.OPTIFINE)?.let { installed ->
                    preselectInstalled(
                        state = currentAddon.optifineVersion,
                        loader = ModLoader.OPTIFINE,
                        versions = versions,
                        installedVersion = installed.version,
                        matcher = { version, installed -> version.matchesInstalledVersion(installed) },
                        //与已选 Forge 成对校验兼容性
                        validator = { version ->
                            currentAddon.forgeVersion.value?.let { forge ->
                                isOptiFineCompatibleWithForge(version, forge)
                            } ?: true
                        }
                    )
                }
            }
        }
    }

    fun reloadForge() {
        launchAddonReload(
            { currentAddon.forgeState = it },
            { ForgeVersions.fetchForgeList(gameVersion) }
        ) { versions ->
            addonList.forgeList = versions
            enqueuePreselect(ModLoader.FORGE) {
                findInstalled(ModLoader.FORGE)?.let { installed ->
                    preselectInstalled(
                        state = currentAddon.forgeVersion,
                        loader = ModLoader.FORGE,
                        versions = versions,
                        installedVersion = installed.version,
                        //与已选 OptiFine 成对校验兼容性
                        validator = { version ->
                            currentAddon.optifineVersion.value?.let { optifine ->
                                isOptiFineCompatibleWithForge(optifine, version)
                            } ?: true
                        }
                    )
                }
            }
        }
    }

    fun reloadNeoForge() {
        launchAddonReload(
            { currentAddon.neoforgeState = it },
            { NeoForgeVersions.fetchNeoForgeList(gameVersion = gameVersion) }
        ) { versions ->
            addonList.neoforgeList = versions
            enqueuePreselect(ModLoader.NEOFORGE) {
                findInstalled(ModLoader.NEOFORGE)?.let { installed ->
                    preselectInstalled(currentAddon.neoforgeVersion, ModLoader.NEOFORGE, versions, installed.version)
                }
            }
        }
    }

    fun reloadFabric() {
        launchAddonReload(
            { currentAddon.fabricState = it },
            { FabricVersions.fetchFabricLoaderList(gameVersion) }
        ) { versions ->
            addonList.fabricList = versions
            enqueuePreselect(ModLoader.FABRIC) {
                findInstalled(ModLoader.FABRIC)?.let { installed ->
                    preselectInstalled(currentAddon.fabricVersion, ModLoader.FABRIC, versions, installed.version)
                }
            }
        }
    }

    fun reloadLegacyFabric() {
        launchAddonReload(
            { currentAddon.legacyFabricState = it },
            { LegacyFabricVersions.fetchFabricLoaderList(gameVersion) }
        ) { versions ->
            addonList.legacyFabricList = versions
            enqueuePreselect(ModLoader.LEGACY_FABRIC) {
                findInstalled(ModLoader.LEGACY_FABRIC)?.let { installed ->
                    preselectInstalled(currentAddon.legacyFabricVersion, ModLoader.LEGACY_FABRIC, versions, installed.version)
                }
            }
        }
    }

    fun reloadQuilt() {
        launchAddonReload(
            { currentAddon.quiltState = it },
            { QuiltVersions.fetchQuiltLoaderList(gameVersion) }
        ) { versions ->
            addonList.quiltList = versions
            enqueuePreselect(ModLoader.QUILT) {
                findInstalled(ModLoader.QUILT)?.let { installed ->
                    preselectInstalled(currentAddon.quiltVersion, ModLoader.QUILT, versions, installed.version)
                }
            }
        }
    }

    fun reloadCleanroom() {
        launchAddonReload(
            { currentAddon.cleanroomState = it },
            { CleanroomVersions.fetchLoaderList(gameVersion) }
        ) { versions ->
            addonList.cleanroomList = versions
            enqueuePreselect(ModLoader.CLEANROOM) {
                findInstalled(ModLoader.CLEANROOM)?.let { installed ->
                    preselectInstalled(currentAddon.cleanroomVersion, ModLoader.CLEANROOM, versions, installed.version)
                }
            }
        }
    }

    init {
        //不会被加载版本列表的已安装加载器，直接标记为未识别，避免阻塞其他加载器的预选
        installedLoaders.forEach { installed ->
            if (installed.loader !in reloadableLoaders) {
                attemptedPreselects.add(installed.loader)
                unmatchedLoaders = unmatchedLoaders + installed.loader
            }
        }
        reloadOptiFine()
        reloadForge()
        if (loaderSupports.isNeoForgeSupports) {
            reloadNeoForge()
        }
        if (loaderSupports.isFabricSupports) {
            reloadFabric()
        }
        if (loaderSupports.isLegacyFabricSupports) {
            reloadLegacyFabric()
        }
        if (loaderSupports.isQuiltSupports) {
            reloadQuilt()
        }
        if (loaderSupports.isCleanroomSupports) {
            reloadCleanroom()
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * @return 从版本解析出当前已安装的全部加载器
 */
@Composable
private fun rememberInstalledLoaders(
    version: Version,
    versionInfo: VersionInfo
): List<VersionInfo.LoaderInfo> = remember(version) {
    buildList {
        runCatching {
            val jsonFile = File(version.getVersionPath(), "${version.getVersionName()}.json")
            GSON.fromJson(jsonFile.readText(), LaunchFor::class.java)
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to parse the launchFor info of the version.", e)
        }.getOrNull()?.infos?.forEach { info ->
            if (info.name.equals("Minecraft", ignoreCase = true)) return@forEach
            ModLoader.entries.find { it.displayName.equals(info.name, ignoreCase = true) }?.let { loader ->
                add(VersionInfo.LoaderInfo(loader = loader, version = info.version))
            }
        }
        versionInfo.loaderInfos.forEach { loaderInfo ->
            if (none { it.loader == loaderInfo.loader }) {
                add(loaderInfo)
            }
        }
    }.distinctBy { it.loader }
}

/**
 * Minecraft 版本行
 */
@Composable
private fun MCVersionRow(
    modifier: Modifier = Modifier,
    current: String,
    selected: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = cardColor(),
        contentColor = onCardColor()
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                modifier = Modifier.size(34.dp),
                painter = painterResource(R.drawable.img_minecraft),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(all = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Minecraft",
                    style = MaterialTheme.typography.titleSmall
                )
                if (selected == current) {
                    Text(
                        text = current,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    TextTransition(
                        from = current,
                        to = selected
                    )
                }
            }
            Icon(
                modifier = Modifier.size(34.dp),
                painter = painterResource(R.drawable.ic_arrow_right_rounded),
                contentDescription = stringResource(R.string.versions_modify_select_mc)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

/**
 * 修改版本的提醒条目
 */
@Composable
private fun ModifyTipItem(
    title: String,
    text: String,
    modifier: Modifier = Modifier
) {
    val color = influencedByBackgroundColor(
        color = MaterialTheme.colorScheme.secondaryContainer,
        enabled = true
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = color,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier
                .backgroundGlass(AllSettings.backgroundBlur.state, color)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_info_outlined),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}