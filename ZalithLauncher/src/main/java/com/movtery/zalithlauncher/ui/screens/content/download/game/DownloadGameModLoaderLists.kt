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

package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.ResponseTooShortException
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion
import com.movtery.zalithlauncher.game.version.installed.utils.isBiggerVer
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.toLocal
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

private const val TAG = "ModLoaderLists"

class AddonList {
    //版本列表
    var optifineList by mutableStateOf<List<OptiFineVersion>?>(null)
    var forgeList by mutableStateOf<List<ForgeVersion>?>(null)
    var neoforgeList by mutableStateOf<List<NeoForgeVersion>?>(null)
    var fabricList by mutableStateOf<List<FabricVersion>?>(null)
    var fabricAPIList by mutableStateOf<List<ModVersion>?>(null)
    var legacyFabricList by mutableStateOf<List<LegacyFabricVersion>?>(null)
    var legacyFabricAPIList by mutableStateOf<List<ModVersion>?>(null)
    var quiltList by mutableStateOf<List<QuiltVersion>?>(null)
    var quiltAPIList by mutableStateOf<List<ModVersion>?>(null)
    var cleanroomList by mutableStateOf<List<CleanroomVersion>?>(null)
}

class CurrentAddon {
    //当前选择版本
    var optifineVersion = mutableStateOf<OptiFineVersion?>(null)
    var forgeVersion = mutableStateOf<ForgeVersion?>(null)
    var neoforgeVersion = mutableStateOf<NeoForgeVersion?>(null)
    var fabricVersion = mutableStateOf<FabricVersion?>(null)
    var fabricAPIVersion = mutableStateOf<ModVersion?>(null)
    var legacyFabricVersion = mutableStateOf<LegacyFabricVersion?>(null)
    var legacyFabricAPIVersion = mutableStateOf<ModVersion?>(null)
    var quiltVersion = mutableStateOf<QuiltVersion?>(null)
    var quiltAPIVersion = mutableStateOf<ModVersion?>(null)
    var cleanroomVersion = mutableStateOf<CleanroomVersion?>(null)

    //加载状态
    var optifineState by mutableStateOf<AddonState>(AddonState.None)
    var forgeState by mutableStateOf<AddonState>(AddonState.None)
    var neoforgeState by mutableStateOf<AddonState>(AddonState.None)
    var fabricState by mutableStateOf<AddonState>(AddonState.None)
    var fabricAPIState by mutableStateOf<AddonState>(AddonState.None)
    var legacyFabricState by mutableStateOf<AddonState>(AddonState.None)
    var legacyFabricAPIState by mutableStateOf<AddonState>(AddonState.None)
    var quiltState by mutableStateOf<AddonState>(AddonState.None)
    var quiltAPIState by mutableStateOf<AddonState>(AddonState.None)
    var cleanroomState by mutableStateOf<AddonState>(AddonState.None)

    //不兼容列表 利用Set集合不可重复
    var incompatibleWithOptiFine = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithForge = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithNeoForge = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithFabric = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithFabricAPI = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithLegacyFabric = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithLegacyFabricAPI = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithQuilt = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithQuiltAPI = mutableStateOf<Set<ModLoader>>(emptySet())
    var incompatibleWithCleanroom = mutableStateOf<Set<ModLoader>>(emptySet())




    /**
     * 将 API模组与其对应的模组加载器关联起来
     */
    private val apiToPrimary = mapOf(
        ModLoader.FABRIC_API to ModLoader.FABRIC,
        ModLoader.QUILT_API to ModLoader.QUILT,
        ModLoader.LEGACY_FABRIC_API to ModLoader.LEGACY_FABRIC
    )

    private val primaryLoaders = setOf(
        ModLoader.OPTIFINE,
        ModLoader.FORGE,
        ModLoader.NEOFORGE,
        ModLoader.FABRIC,
        ModLoader.QUILT,
        ModLoader.CLEANROOM,
        ModLoader.LEGACY_FABRIC
    )

    private data class LoaderState<T : Any>(
        val loader: ModLoader,
        val versionState: MutableState<T?>,
        val incompatibleState: MutableState<Set<ModLoader>>
    )

    private val allLoaders = listOf(
        LoaderState(ModLoader.OPTIFINE, optifineVersion, incompatibleWithOptiFine),
        LoaderState(ModLoader.FORGE, forgeVersion, incompatibleWithForge),
        LoaderState(ModLoader.NEOFORGE, neoforgeVersion, incompatibleWithNeoForge),
        LoaderState(ModLoader.FABRIC, fabricVersion, incompatibleWithFabric),
        LoaderState(ModLoader.FABRIC_API, fabricAPIVersion, incompatibleWithFabricAPI),
        LoaderState(ModLoader.LEGACY_FABRIC, legacyFabricVersion, incompatibleWithLegacyFabric),
        LoaderState(ModLoader.LEGACY_FABRIC_API, legacyFabricAPIVersion, incompatibleWithLegacyFabricAPI),
        LoaderState(ModLoader.QUILT, quiltVersion, incompatibleWithQuilt),
        LoaderState(ModLoader.QUILT_API, quiltAPIVersion, incompatibleWithQuiltAPI),
        LoaderState(ModLoader.CLEANROOM, cleanroomVersion, incompatibleWithCleanroom)
    )

    private val loaderMap = allLoaders.associateBy { it.loader }

    fun updateIncompatibleState(
        thisLoader: ModLoader,
        addonList: AddonList
    ) {
        val thisVer = allLoaders.find { it.loader == thisLoader }!!.versionState.value

        val currentVersions = loaderMap.mapValues { it.value.versionState.value }

        val clearedPrimaries = buildSet {
            if (thisVer == null) {
                add(thisLoader)
                return@buildSet
            }
            allLoaders.forEach { state ->
                if (state.loader == thisLoader) return@forEach
                if (state.versionState.value != null) {
                    if (
                        areMutuallyExclusive(
                            currentVersions[thisLoader],
                            thisLoader,
                            state.loader,
                            addonList
                        )
                    ) {
                        state.versionState.value = null
                        add(state.loader)
                    }
                }
            }
        }

        if (clearedPrimaries.isNotEmpty()) {
            apiToPrimary.forEach { (api, primary) ->
                if (primary in clearedPrimaries) {
                    loaderMap[api]?.versionState?.value = null
                }
            }
        }

        val finalVersions = loaderMap.mapValues { it.value.versionState.value }

        loaderMap.values.forEach { targetState ->
            val incompatible = buildSet {
                finalVersions.forEach { (otherLoader, otherVersion) ->
                    if (otherLoader == targetState.loader || otherVersion == null) {
                        return@forEach
                    }
                    if (
                        areMutuallyExclusive(
                            finalVersions[targetState.loader],
                            targetState.loader,
                            otherLoader,
                            addonList
                        )
                    ) {
                        add(otherLoader)
                    }
                }
            }

            targetState.incompatibleState.value = incompatible
        }
    }

    /**
     * 判断两个加载器之间是否不兼容
     */
    private fun areMutuallyExclusive(
        version: AddonVersion?,
        thisLoader: ModLoader,
        otherLoader: ModLoader,
        addonList: AddonList
    ): Boolean {
        if (thisLoader == otherLoader) return false

        if (thisLoader == ModLoader.OPTIFINE && otherLoader == ModLoader.FORGE) {
            val optifine = version as? OptiFineVersion ?: return false
            return !isOptiFineCompatibleWithForgeList(optifine, addonList.forgeList)
        }

        if (thisLoader == ModLoader.FORGE && otherLoader == ModLoader.OPTIFINE) {
            val forge = version as? ForgeVersion ?: return false
            return !isForgeCompatibleWithOptiFineList(forge, addonList.optifineList)
        }

        return when {
            thisLoader in primaryLoaders && otherLoader in primaryLoaders -> true
            thisLoader in primaryLoaders && otherLoader.isApiMod ->
                apiToPrimary[otherLoader] != thisLoader

            thisLoader.isApiMod && otherLoader in primaryLoaders ->
                apiToPrimary[thisLoader] != otherLoader

            thisLoader.isApiMod && otherLoader.isApiMod ->
                apiToPrimary[thisLoader] != apiToPrimary[otherLoader]
            else -> false
        }
    }
}

/**
 * 加载器对于Minecraft版本的支持情况信息
 */
data class LoaderVerSupports(
    val isNeoForgeSupports: Boolean,
    val isFabricSupports: Boolean,
    val isQuiltSupports: Boolean,
    val isCleanroomSupports: Boolean,
    val isLegacyFabricSupports: Boolean,
)

@Composable
fun rememberLoaderVerSupports(mcVer: String) = remember(mcVer) {
    val fabricLike = mcVer.isBiggerVer("1.13.2")
    LoaderVerSupports(
        isNeoForgeSupports = mcVer.isBiggerVer("1.20"),
        isFabricSupports = fabricLike,
        isQuiltSupports = fabricLike,
        isCleanroomSupports = mcVer == "1.12.2",
        isLegacyFabricSupports = !fabricLike
    )
}

/**
 * 在 ViewModel 中运行任务并更新附加内容的状态
 */
suspend fun <T> ViewModel.runWithState(
    updateState: (AddonState) -> Unit,
    block: suspend () -> T?
): T? {
    updateState(AddonState.Loading)
    return runCatching {
        block().also {
            updateState(AddonState.None)
        }
    }.onFailure { e ->
        val state = when (e) {
            is ResponseTooShortException -> {
                //忽略，判定为不可用
                AddonState.None
            }
            is HttpRequestTimeoutException -> AddonState.Error(androidText(R.string.error_timeout))
            is UnknownHostException, is UnresolvedAddressException -> {
                AddonState.Error(androidText(R.string.error_network_unreachable))
            }
            is ConnectException -> {
                AddonState.Error(androidText(R.string.error_connection_failed))
            }
            is SerializationException -> {
                AddonState.Error(androidText(R.string.error_parse_failed))
            }
            is ResponseException -> AddonState.Error(e.toLocal())
            else -> {
                Logger.error(TAG, "An unknown exception was caught!", e)
                AddonState.Error(
                    androidText(e.localizedMessage ?: e.message ?: e::class.qualifiedName ?: "Unknown error")
                )
            }
        }
        updateState(state)
    }.getOrNull()
}

@Composable
fun OptiFineList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    addonList: AddonList,
    error: String? = null,
    onValueChanged: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.optifineVersion
    val forgeVersion by currentAddon.forgeVersion
    val incompatibleSet by currentAddon.incompatibleWithOptiFine

    val items = remember(addonList.optifineList, forgeVersion) {
        addonList.optifineList?.filter { version ->
            forgeVersion?.let { fv ->
                isOptiFineCompatibleWithForge(version, fv)
            } ?: true
        }
    }

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.optifineState,
        title = ModLoader.OPTIFINE.displayName,
        error = error,
        iconPainter = painterResource(R.drawable.img_loader_optifine),
        items = items,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.OPTIFINE, addonList)
        },
        triggerCheckIncompatible = arrayOf(currentAddon.forgeState),
        getItemText = { it.displayName },
        summary = { OptiFineVersionSummary(it) },
        onValueChange = { version0 ->
            version = version0
            onValueChanged()
        },
        onReload = onReload
    )
}

@Composable
fun ForgeList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    addonList: AddonList,
    error: String? = null,
    onValueChanged: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.forgeVersion
    val optifineVersion by currentAddon.optifineVersion
    val incompatibleSet by currentAddon.incompatibleWithForge

    val items = addonList.forgeList?.filter { version ->
        //选择 OptiFine 之后，根据 OptiFine 需求的 Forge 版本进行过滤
        optifineVersion?.let { ofv ->
            isOptiFineCompatibleWithForge(ofv, version)
        } ?: true
    }

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.forgeState,
        title = ModLoader.FORGE.displayName,
        iconPainter = painterResource(R.drawable.img_anvil),
        items = items,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.FORGE, addonList)
        },
        triggerCheckIncompatible = arrayOf(currentAddon.optifineState),
        error = error ?: checkForgeCompatibilityError(addonList.forgeList),
        getItemText = { it.versionName },
        summary = { ForgeVersionSummary(it) },
        onValueChange = { version0 ->
            version = version0
            onValueChanged()
        },
        onReload = onReload
    )
}

@Composable
fun NeoForgeList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    addonList: AddonList,
    error: String? = null,
    onValueChanged: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.neoforgeVersion
    val incompatibleSet by currentAddon.incompatibleWithNeoForge

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.neoforgeState,
        title = ModLoader.NEOFORGE.displayName,
        error = error,
        iconPainter = painterResource(R.drawable.img_loader_neoforge),
        items = addonList.neoforgeList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.NEOFORGE, addonList)
        },
        getItemText = { it.versionName },
        summary = { NeoForgeSummary(it) },
        onValueChange = { version0 ->
            version = version0
            onValueChanged()
        },
        onReload = onReload
    )
}

@Composable
fun FabricList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    addonList: AddonList,
    error: String? = null,
    onValueChanged: (FabricVersion?) -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.fabricVersion
    val incompatibleSet by currentAddon.incompatibleWithFabric

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.fabricState,
        title = ModLoader.FABRIC.displayName,
        error = error,
        iconPainter = painterResource(R.drawable.img_loader_fabric),
        items = addonList.fabricList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.FABRIC, addonList)
        },
        getItemText = { it.version },
        summary = { FabricLikeSummary(it) },
        onValueChange = { version0 ->
            version = version0
            onValueChanged(version0)
        },
        onReload = onReload
    )
}

@Composable
fun FabricAPIList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    requestString: String = stringResource(R.string.download_game_addon_request_addon, ModLoader.FABRIC.displayName),
    addonList: AddonList,
    error: String? = null,
    onValueChanged: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.fabricAPIVersion
    val fabricVersion by currentAddon.fabricVersion
    val incompatibleSet by currentAddon.incompatibleWithFabricAPI

    val unSelectedFabric = remember(fabricVersion) {
        when {
            fabricVersion == null -> {
                version = null
                requestString
            }
            else -> null
        }
    }

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.fabricAPIState,
        title = ModLoader.FABRIC_API.displayName,
        iconPainter = painterResource(R.drawable.img_loader_fabric),
        items = addonList.fabricAPIList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.FABRIC_API, addonList)
        },
        error = error ?: unSelectedFabric,
        getItemText = { it.displayName },
        summary = { ModSummary(it) },
        onValueChange = { version0 ->
            version = version0
            onValueChanged()
        },
        onReload = onReload
    )
}

@Composable
fun LegacyFabricList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    addonList: AddonList,
    error: String? = null,
    onValueChanged: (LegacyFabricVersion?) -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.legacyFabricVersion
    val incompatibleSet by currentAddon.incompatibleWithLegacyFabric

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.legacyFabricState,
        title = ModLoader.LEGACY_FABRIC.displayName,
        error = error,
        iconPainter = painterResource(R.drawable.img_loader_legacy_fabric),
        items = addonList.legacyFabricList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.LEGACY_FABRIC, addonList)
        },
        getItemText = { it.version },
        summary = { FabricLikeSummary(it) },
        onValueChange = { version0 ->
            version = version0
            onValueChanged(version0)
        },
        onReload = onReload
    )
}

@Composable
fun LegacyFabricAPIList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    requestString: String = stringResource(R.string.download_game_addon_request_addon, ModLoader.LEGACY_FABRIC.displayName),
    addonList: AddonList,
    error: String? = null,
    onValueChanged: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.legacyFabricAPIVersion
    val legacyFabricVersion by currentAddon.legacyFabricVersion
    val incompatibleSet by currentAddon.incompatibleWithLegacyFabricAPI

    val unSelectedFabric = remember(legacyFabricVersion) {
        when {
            legacyFabricVersion == null -> {
                version = null
                requestString
            }
            else -> null
        }
    }

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.legacyFabricAPIState,
        title = ModLoader.LEGACY_FABRIC_API.displayName,
        iconPainter = painterResource(R.drawable.img_loader_legacy_fabric),
        items = addonList.legacyFabricAPIList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.LEGACY_FABRIC_API, addonList)
        },
        error = error ?: unSelectedFabric,
        getItemText = { it.displayName },
        summary = { ModSummary(it) },
        onValueChange = { version0 ->
            version = version0
            onValueChanged()
        },
        onReload = onReload
    )
}

@Composable
fun QuiltList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    addonList: AddonList,
    error: String? = null,
    onValueChanged: (QuiltVersion?) -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.quiltVersion
    val incompatibleSet by currentAddon.incompatibleWithQuilt

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.quiltState,
        title = ModLoader.QUILT.displayName,
        error = error,
        iconPainter = painterResource(R.drawable.img_loader_quilt),
        items = addonList.quiltList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.QUILT, addonList)
        },
        getItemText = { it.version },
        summary = { FabricLikeSummary(it) },
        onValueChange =  { version0 ->
            version = version0
            onValueChanged(version0)
        },
        onReload = onReload
    )
}

@Composable
fun QuiltAPIList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    requestString: String = stringResource(R.string.download_game_addon_request_addon, ModLoader.QUILT.displayName),
    addonList: AddonList,
    error: String? = null,
    onValueChanged: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.quiltAPIVersion
    val quiltVersion by currentAddon.quiltVersion
    val incompatibleSet by currentAddon.incompatibleWithQuiltAPI

    val unSelectedQuilt = remember(quiltVersion) {
        when {
            quiltVersion == null -> {
                version = null
                requestString
            }
            else -> null
        }
    }

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.quiltAPIState,
        title = ModLoader.QUILT_API.displayName,
        iconPainter = painterResource(R.drawable.img_loader_quilt),
        items = addonList.quiltAPIList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.QUILT_API, addonList)
        },
        error = error ?: unSelectedQuilt,
        getItemText = { it.displayName },
        summary = { ModSummary(it) },
        onValueChange =  { version0 ->
            version = version0
            onValueChanged()
        },
        onReload = onReload
    )
}

@Composable
fun CleanroomList(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    addonList: AddonList,
    error: String? = null,
    onValueChanged: (CleanroomVersion?) -> Unit = {},
    onReload: () -> Unit = {}
) {
    var version by currentAddon.cleanroomVersion
    val incompatibleSet by currentAddon.incompatibleWithCleanroom

    AddonListLayout(
        modifier = modifier,
        state = currentAddon.cleanroomState,
        title = ModLoader.CLEANROOM.displayName,
        error = error,
        iconPainter = painterResource(R.drawable.img_loader_cleanroom),
        items = addonList.cleanroomList,
        current = version,
        incompatibleSet = incompatibleSet,
        checkIncompatible = {
            currentAddon.updateIncompatibleState(ModLoader.CLEANROOM, addonList)
        },
        getItemText = { it.version },
        summary = { CleanroomSummary(it) },
        onValueChange =  { version0 ->
            version = version0
            onValueChanged(version0)
        },
        onReload = onReload
    )
}

private fun isOptiFineCompatibleWithForge(
    optifine: OptiFineVersion,
    forge: ForgeVersion
): Boolean = optifine.forgeVersion?.let {
    //空字符串表示兼容所有
    it.isEmpty() || forge.forgeBuildVersion.compareOptiFineRequired(it)
} ?: false //没有声明需要的 Forge 版本，视为不兼容

private fun isOptiFineCompatibleWithForgeList(
    optifine: OptiFineVersion,
    forgeList: List<ForgeVersion>?
): Boolean {
    //没有声明需要的 Forge 版本，视为不兼容
    val requiredVersion = optifine.forgeVersion ?: return false
    return when {
        requiredVersion.isEmpty() -> true //为空则表示不要求，兼容
        else -> forgeList?.any {
            it.forgeBuildVersion.compareOptiFineRequired(requiredVersion)
        } == true
    }
}

private fun isForgeCompatibleWithOptiFineList(
    forge: ForgeVersion,
    optifineList: List<OptiFineVersion>?
): Boolean {
    val forgeVersion = forge.forgeBuildVersion

    optifineList?.forEach { optifine ->
        val ofVersion = optifine.forgeVersion ?: return@forEach //null: 不兼容，跳过
        if (ofVersion.isEmpty()) return true    //空字符串表示兼容所有
        if (forgeVersion.compareOptiFineRequired(ofVersion)) return true
    }

    return false //没有匹配项
}

@Composable
private fun checkForgeCompatibilityError(
    forgeList: List<ForgeVersion>?
): String? {
    return when {
        forgeList == null -> null //保持默认的“不可用”
        forgeList.any { forgeVersion -> forgeVersion.category == "universal" || forgeVersion.category == "client" } -> {
            //跳过无法自动安装的版本
            stringResource(R.string.download_game_addon_not_installable)
        }
        else -> null
    }
}