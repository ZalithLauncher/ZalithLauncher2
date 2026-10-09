/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Mirai Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.home

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.cardgrid.state.CardGridState
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionType
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import org.jackhuang.hmcl.util.versioning.GameVersionNumber

private const val MAX_HOME_LIBRARY_CARDS = 15

private enum class LibraryFilter {
    ALL,
    PINNED,
    VANILLA,
    MODDED,
    OTHER
}

private enum class LibrarySort {
    NAME,
    GAME_VERSION,
    LAST_PLAYED
}

@Composable
fun MiraiHomeDashboard(
    gridState: CardGridState,
    onLaunch: (Version?) -> Unit,
    onExploreContent: () -> Unit,
    onCreateInstance: () -> Unit,
    onManageVersions: () -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    modifier: Modifier = Modifier
) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
    val installedVersions = remember(versions) {
        versions.filter { it.isValid() }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }
    var selectedSort by rememberSaveable { mutableStateOf(LibrarySort.NAME) }

    // Keep this list backed by the launcher's real selection, pin state, and game logs.
    // Never synthesize recent instances when the installation has no play history.
    val jumpInVersions = buildList {
        currentVersion?.takeIf { it.isValid() }?.let(::add)
        installedVersions
            .filter { it.pinnedState }
            .sortedByDescending(::lastPlayedAt)
            .forEach(::add)
        installedVersions
            .filter { lastPlayedAt(it) > 0L }
            .sortedByDescending(::lastPlayedAt)
            .forEach(::add)
    }.distinctBy { it.getVersionName() }.take(3)

    val query = searchQuery.trim()
    val matchingVersions = installedVersions.filter { version ->
        val matchesQuery = query.isEmpty() || sequenceOf(
            version.getVersionName(),
            versionSummary(version)
        ).any { it.contains(query, ignoreCase = true) }
        matchesQuery && matchesFilter(version, selectedFilter)
    }
    val filteredVersions = when (selectedSort) {
        LibrarySort.NAME -> matchingVersions.sortedBy { it.getVersionName().lowercase() }
        LibrarySort.GAME_VERSION -> matchingVersions.sortedWith { first, second ->
            val firstVersion = first.getVersionInfo()?.minecraftVersion ?: first.getVersionName()
            val secondVersion = second.getVersionInfo()?.minecraftVersion ?: second.getVersionName()
            val versionOrder = GameVersionNumber.compare(firstVersion, secondVersion)
            if (versionOrder != 0) -versionOrder
            else first.getVersionName().compareTo(second.getVersionName(), ignoreCase = true)
        }
        LibrarySort.LAST_PLAYED -> matchingVersions.sortedWith(
            compareByDescending<Version> { lastPlayedAt(it) }
                .thenBy { it.getVersionName().lowercase() }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item(key = "jump-in") {
            JumpInSection(
                versions = jumpInVersions,
                currentVersionName = currentVersion?.getVersionName(),
                isRefreshing = isRefreshing,
                onLaunch = onLaunch,
                onOpenVersionSettings = onOpenVersionSettings
            )
        }

        item(key = "library") {
            LibrarySection(
                versions = installedVersions,
                filteredVersions = filteredVersions,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                selectedSort = selectedSort,
                onSortChange = { selectedSort = it },
                isRefreshing = isRefreshing,
                onExploreContent = onExploreContent,
                onCreateInstance = onCreateInstance,
                onManageVersions = onManageVersions,
                onClearFilters = {
                    searchQuery = ""
                    selectedFilter = LibraryFilter.ALL
                    selectedSort = LibrarySort.NAME
                },
                onLaunch = onLaunch,
                onOpenVersionSettings = onOpenVersionSettings
            )
        }

        // Keep the existing customizable home cards available below the library.
        item(key = "home-cards") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.home_cards_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.home_cards_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    HomeGrid(state = gridState, isVisible = true, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun JumpInSection(
    versions: List<Version>,
    currentVersionName: String?,
    isRefreshing: Boolean,
    onLaunch: (Version?) -> Unit,
    onOpenVersionSettings: (Version) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_jump_in),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.home_jump_in_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.ic_keyboard_arrow_up else R.drawable.ic_keyboard_arrow_down
                    ),
                    contentDescription = stringResource(
                        if (expanded) R.string.generic_collapse else R.string.generic_expand
                    )
                )
            }
        }

        if (expanded) {
            when {
                versions.isNotEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    versions.forEach { version ->
                        QuickLaunchRow(
                            version = version,
                            isSelected = version.getVersionName() == currentVersionName,
                            onLaunch = { onLaunch(version) },
                            onOpenSettings = { onOpenVersionSettings(version) }
                        )
                    }
                }
                isRefreshing -> DashboardPlaceholder(
                    title = stringResource(R.string.generic_loading),
                    text = stringResource(R.string.home_versions_refreshing)
                )
                else -> DashboardPlaceholder(
                    title = stringResource(R.string.home_empty_jump_in_title),
                    text = stringResource(R.string.home_empty_jump_in_summary)
                )
            }
        }
    }
}

@Composable
private fun QuickLaunchRow(
    version: Version,
    isSelected: Boolean,
    onLaunch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
        onClick = onOpenSettings
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionIconImage(version = version, modifier = Modifier.size(44.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = version.getVersionName(),
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSelected || version.pinnedState) {
                        StatusChip(
                            text = stringResource(
                                if (isSelected) R.string.home_badge_selected else R.string.home_badge_pinned
                            )
                        )
                    }
                }
                Text(
                    text = versionSummary(version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LastPlayedLabel(version = version)
            }

            IconButton(
                onClick = onLaunch,
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                    contentDescription = stringResource(R.string.main_launch_game),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = stringResource(R.string.versions_manage_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibrarySection(
    versions: List<Version>,
    filteredVersions: List<Version>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: LibraryFilter,
    onFilterChange: (LibraryFilter) -> Unit,
    selectedSort: LibrarySort,
    onSortChange: (LibrarySort) -> Unit,
    isRefreshing: Boolean,
    onExploreContent: () -> Unit,
    onCreateInstance: () -> Unit,
    onManageVersions: () -> Unit,
    onClearFilters: () -> Unit,
    onLaunch: (Version?) -> Unit,
    onOpenVersionSettings: (Version) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_library),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.home_installed_version_count, versions.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onCreateInstance,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(stringResource(R.string.home_new_instance), maxLines = 1)
            }
        }

        if (versions.isNotEmpty()) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.home_search_instances),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.home_clear_search)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterMenu(
                    selectedFilter = selectedFilter,
                    onFilterChange = onFilterChange
                )
                SortMenu(
                    selectedSort = selectedSort,
                    onSortChange = onSortChange
                )
                TextButton(
                    onClick = onManageVersions,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dashboard_filled),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(stringResource(R.string.home_view_all), maxLines = 1)
                }
            }
        }

        when {
            versions.isEmpty() && isRefreshing -> DashboardPlaceholder(
                title = stringResource(R.string.generic_loading),
                text = stringResource(R.string.home_versions_refreshing)
            )
            versions.isEmpty() -> EmptyLibraryCard(onCreateInstance = onCreateInstance)
            filteredVersions.isEmpty() -> BackgroundCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.home_no_matching_instances),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.generic_no_matching_items),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onClearFilters) {
                        Text(stringResource(R.string.home_clear_filters))
                    }
                }
            }
            else -> {
                val visibleVersions = filteredVersions.take(MAX_HOME_LIBRARY_CARDS)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val gap = 10.dp
                    val columns = ((maxWidth + gap) / (116.dp + gap))
                        .toInt()
                        .coerceIn(2, 5)
                    val cardWidth = (maxWidth - gap * (columns - 1)) / columns

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        maxItemsInEachRow = columns,
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        verticalArrangement = Arrangement.spacedBy(gap)
                    ) {
                        visibleVersions.forEach { version ->
                            LibraryVersionCard(
                                modifier = Modifier.width(cardWidth),
                                version = version,
                                onLaunch = { onLaunch(version) },
                                onOpenSettings = { onOpenVersionSettings(version) }
                            )
                        }
                    }
                }

                if (filteredVersions.size > visibleVersions.size) {
                    TextButton(
                        modifier = Modifier.align(Alignment.End),
                        onClick = onManageVersions
                    ) {
                        Text(stringResource(R.string.home_view_all_versions, filteredVersions.size))
                    }
                }
            }
        }

        if (versions.isNotEmpty()) {
            OutlinedButton(
                onClick = onExploreContent,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.home_explore_content))
            }
        }
    }
}

@Composable
private fun FilterMenu(
    selectedFilter: LibraryFilter,
    onFilterChange: (LibraryFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_filter_alt_outlined),
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(filterLabel(selectedFilter), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LibraryFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filterLabel(filter)) },
                    leadingIcon = {
                        if (filter == selectedFilter) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        onFilterChange(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SortMenu(
    selectedSort: LibrarySort,
    onSortChange: (LibrarySort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sort),
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(sortLabel(selectedSort), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LibrarySort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = { Text(sortLabel(sort)) },
                    leadingIcon = {
                        if (sort == selectedSort) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        onSortChange(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun filterLabel(filter: LibraryFilter): String = when (filter) {
    LibraryFilter.ALL -> stringResource(R.string.home_filter_all)
    LibraryFilter.PINNED -> stringResource(R.string.home_filter_pinned)
    LibraryFilter.VANILLA -> stringResource(R.string.versions_manage_category_vanilla)
    LibraryFilter.MODDED -> stringResource(R.string.versions_manage_category_modloader)
    LibraryFilter.OTHER -> stringResource(R.string.home_filter_other)
}

@Composable
private fun sortLabel(sort: LibrarySort): String = when (sort) {
    LibrarySort.NAME -> stringResource(R.string.home_sort_name)
    LibrarySort.GAME_VERSION -> stringResource(R.string.home_sort_game_version)
    LibrarySort.LAST_PLAYED -> stringResource(R.string.home_sort_last_played)
}

private fun matchesFilter(version: Version, filter: LibraryFilter): Boolean = when (filter) {
    LibraryFilter.ALL -> true
    LibraryFilter.PINNED -> version.pinnedState
    LibraryFilter.VANILLA -> version.versionType == VersionType.VANILLA
    LibraryFilter.MODDED -> version.versionType == VersionType.MODLOADERS
    LibraryFilter.OTHER -> version.versionType == VersionType.UNKNOWN
}

@Composable
private fun LibraryVersionCard(
    version: Version,
    onLaunch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackgroundCard(
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        onClick = onOpenSettings
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                VersionIconImage(
                    version = version,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
                if (version.pinnedState) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pinned_filled),
                        contentDescription = stringResource(R.string.home_badge_pinned),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(17.dp)
                    )
                }
                IconButton(
                    onClick = onLaunch,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.main_launch_game),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Text(
                text = version.getVersionName(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = versionSummary(version),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LastPlayedLabel(version: Version) {
    val lastPlayed = lastPlayedAt(version)
    val text = if (lastPlayed > 0L) {
        val relative = DateUtils.getRelativeTimeSpanString(
            lastPlayed,
            System.currentTimeMillis(),
            DateUtils.SECOND_IN_MILLIS
        ).toString()
        stringResource(R.string.home_last_played, relative)
    } else {
        stringResource(R.string.home_never_played)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private fun lastPlayedAt(version: Version): Long {
    val latestLog = version.getLatestLog()
    return if (latestLog.isFile) latestLog.lastModified().takeIf { it > 0L } ?: 0L else 0L
}

private fun versionSummary(version: Version): String {
    val info = version.getVersionInfo() ?: return version.getVersionName()
    val loader = info.primaryLoader?.let { "${it.loader.displayName} ${it.version}" }
    return listOfNotNull(info.minecraftVersion, loader).joinToString(" · ")
}

@Composable
private fun EmptyLibraryCard(onCreateInstance: () -> Unit) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.home_empty_library_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.home_empty_library_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onCreateInstance) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.home_new_instance))
            }
        }
    }
}

@Composable
private fun DashboardPlaceholder(title: String, text: String) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
