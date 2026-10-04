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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.search

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModpackCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.curseForgeModLoaderFilters
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFeatures
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModpackCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.modrinthModLoaderFilters
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.BaseFilterLayout
import com.movtery.zalithlauncher.viewmodel.AllSupportPackDisplay
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportOperation
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen

private sealed interface SelectUriOperation {
    data object None : SelectUriOperation
    /** 警告用户整合包兼容性问题 */
    data object Warning : SelectUriOperation
}

private class ModpackViewModel: ViewModel() {
    var selectOperation by mutableStateOf<SelectUriOperation>(SelectUriOperation.None)
}

@Composable
private fun rememberModpackViewModel(): ModpackViewModel {
    return viewModel(
        key = NormalNavKey.SearchModPack.toString() + "_importer"
    ) {
        ModpackViewModel()
    }
}

@Composable
fun SearchModPackScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadModPackScreenKey: TitledNavKey,
    downloadModPackScreenCurrentKey: TitledNavKey?,
    viewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    val initialPlatform = remember {
        AllSettings.searchModpackPlatform.getValue()
    }

    val context = LocalContext.current
    val modpackViewModel = rememberModpackViewModel()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uri ->
            viewModel.import(
                context = context,
                uri = uri,
                onStart = {
                    eventViewModel.sendKeepScreen(true)
                },
                onStop = {
                    eventViewModel.sendKeepScreen(false)
                }
            )
        }
    }

    SelectUriOperation(
        operation = modpackViewModel.selectOperation,
        onChanged = { modpackViewModel.selectOperation = it },
        selectedUri = {
            //允许导入任意文件，在导入整合包的流程中会对文件进行判断
            filePicker.launch("*/*")
        }
    )

    SearchAssetsScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadModPackScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchModPack,
        currentKey = downloadModPackScreenCurrentKey,
        platformClasses = PlatformClasses.MOD_PACK,
        initialPlatform = initialPlatform,
        onPlatformChange = {
            AllSettings.searchModpackPlatform.save(it)
        },
        getCategories = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> CurseForgeModpackCategory.entries
                Platform.MODRINTH -> ModrinthModpackCategory.entries
            }
        },
        enableModLoader = true,
        getModloaders = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> curseForgeModLoaderFilters
                Platform.MODRINTH -> modrinthModLoaderFilters
            }
        },
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthModpackCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeModpackCategory.entries.find { it.describe() == string }
                }
            }
        },
        swapToDownload = swapToDownload,
        extraFilter = {
            //新增导入整合包按钮
            item {
                BaseFilterLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { role = Role.Button },
                    onClick = {
                        if (viewModel.importOperation == ModpackImportOperation.None) {
                            //先警告用户关于整合包的兼容性问题
                            modpackViewModel.selectOperation = SelectUriOperation.Warning
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MarqueeText(
                            text = stringResource(R.string.import_modpack),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SelectUriOperation(
    operation: SelectUriOperation,
    onChanged: (SelectUriOperation) -> Unit,
    selectedUri: () -> Unit
) {
    when (operation) {
        is SelectUriOperation.None -> {}
        is SelectUriOperation.Warning -> {
            //警告整合包的兼容性（免责声明）
            SimpleAlertDialog(
                title = stringResource(R.string.generic_tip),
                text = {
                    Text(text = stringResource(R.string.import_modpack_tip))

                    Spacer(Modifier.height(8.dp))
                    AllSupportPackDisplay(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))

                    Text(text = stringResource(R.string.download_modpack_warning1))
                    Text(text = stringResource(R.string.download_modpack_warning2))

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.download_modpack_warning3),
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmText = stringResource(R.string.generic_import),
                onCancel = {
                    onChanged(SelectUriOperation.None)
                },
                onConfirm = {
                    onChanged(SelectUriOperation.None)
                    selectedUri()
                }
            )
        }
    }
}