package com.movtery.zalithlauncher.ui.screens.content

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.serverhub.HUB_ONLINE_STEPS
import com.movtery.zalithlauncher.game.serverhub.HubLocation
import com.movtery.zalithlauncher.game.serverhub.HubServer
import com.movtery.zalithlauncher.game.serverhub.HubSource
import com.movtery.zalithlauncher.game.serverhub.HubSourceState
import com.movtery.zalithlauncher.game.serverhub.ServerHubRepository
import com.movtery.zalithlauncher.game.serverhub.normalizeAddress
import com.movtery.zalithlauncher.game.serverhub.normalizeTag
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.home.server.ServerCardManager
import com.movtery.zalithlauncher.ui.screens.content.home.server.ServerCardPingStatus
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.delay

private val GreenOnline = Color(0xFF3DDC84)
private val RedOffline = Color(0xFFE5484D)
private val GreyUnknown = Color(0xFF8A8A8A)

/**
 * Server Hub: jelajahi server dari beberapa website, simpan favorit, lalu main sekali ketuk.
 *
 * @param onQuickPlay dipanggil dengan alamat server untuk langsung meluncurkan game dan bergabung
 */
@Composable
fun ServerHubScreen(
    backScreenViewModel: ScreenBackStackViewModel,
    onQuickPlay: (Version?, String) -> Unit
) {
    BaseScreen(
        screenKey = NormalNavKey.ServerHub,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { _ ->
        ServerHubContent(onQuickPlay = onQuickPlay)
    }
}

@Composable
private fun ServerHubContent(
    onQuickPlay: (Version?, String) -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val servers by ServerHubRepository.servers.collectAsState()
    val modes by ServerHubRepository.modes.collectAsState()
    val states by ServerHubRepository.sourceStates.collectAsState()
    val loading by ServerHubRepository.loading.collectAsState()
    val cards by ServerCardManager.cards.collectAsState()

    var query by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(HubLocation.ALL) }
    var onlineMin by remember { mutableStateOf<Int?>(null) }
    var mode by remember { mutableStateOf<String?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { ServerHubRepository.refresh() }

    //Pencarian dan mode juga diteruskan ke API agar server di luar daftar teratas ikut ketemu
    LaunchedEffect(query, mode) {
        delay(450)
        ServerHubRepository.searchRemote(query, mode)
    }

    val favoriteIds = remember(cards) { cards.map { normalizeAddress(it.record.cardId) }.toSet() }

    val visible = remember(servers, cards, query, location, onlineMin, mode, favoritesOnly) {
        val base = if (favoritesOnly) {
            //Favorit = kartu server di Beranda; data terbaru dari daftar bila ada, bila tidak dari ping kartu
            cards.map { card ->
                val key = normalizeAddress(card.record.cardId)
                servers.firstOrNull { normalizeAddress(it.address) == key } ?: run {
                    val loaded = (card.ping as? ServerCardPingStatus.Loaded)?.result?.status
                    HubServer(
                        id = "fav:$key",
                        name = card.record.name,
                        address = card.record.cardId,
                        source = HubSource.CUSTOM,
                        isIndonesia = false,
                        online = when (card.ping) {
                            is ServerCardPingStatus.Loaded -> true
                            is ServerCardPingStatus.Failed -> false
                            else -> null
                        },
                        players = loaded?.players?.online ?: 0,
                        maxPlayers = loaded?.players?.max ?: 0,
                        version = loaded?.version?.name,
                        iconBytes = card.record.icon
                    )
                }
            }
        } else servers

        val q = query.trim()
        base.filter { s ->
            (q.isEmpty() || s.name.contains(q, ignoreCase = true) || s.address.contains(q, ignoreCase = true)) &&
                    (s.source == HubSource.CUSTOM || when (location) {
                        HubLocation.ALL -> true
                        HubLocation.INDONESIA -> s.isIndonesia
                        HubLocation.OVERSEAS -> !s.isIndonesia
                    }) &&
                    (onlineMin == null || s.players >= onlineMin!!) &&
                    (mode == null || s.tags.any { normalizeTag(it) == normalizeTag(mode!!) })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.server_hub_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.server_hub_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        //Baris atas: Cari | lokasi | online | game | Favorit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                shape = CircleShape,
                placeholder = { Text(stringResource(R.string.server_hub_search_hint)) }
            )

            val allText = stringResource(R.string.server_hub_all)
            FilterDropdown(
                label = when (location) {
                    HubLocation.ALL -> stringResource(R.string.server_hub_filter_location)
                    HubLocation.INDONESIA -> stringResource(R.string.server_hub_location_indonesia)
                    HubLocation.OVERSEAS -> stringResource(R.string.server_hub_location_overseas)
                },
                options = listOf(
                    allText to HubLocation.ALL,
                    stringResource(R.string.server_hub_location_indonesia) to HubLocation.INDONESIA,
                    stringResource(R.string.server_hub_location_overseas) to HubLocation.OVERSEAS
                ),
                onSelect = { location = it }
            )
            FilterDropdown(
                label = onlineMin?.let { "$it+" } ?: stringResource(R.string.server_hub_filter_online),
                options = listOf<Pair<String, Int?>>(allText to null) +
                        HUB_ONLINE_STEPS.map { "$it+" to it },
                onSelect = { onlineMin = it }
            )
            FilterDropdown(
                label = mode ?: stringResource(R.string.server_hub_filter_game),
                options = listOf<Pair<String, String?>>(allText to null) + modes.map { it to it },
                onSelect = { mode = it }
            )
            OutlinedButton(
                onClick = { favoritesOnly = !favoritesOnly },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (favoritesOnly) 2.dp else 1.dp,
                    color = if (favoritesOnly) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            ) {
                Text(stringResource(R.string.server_hub_favorites, cards.size), maxLines = 1)
            }
            OutlinedButton(
                onClick = { showAdd = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.server_hub_add)
                )
            }
        }

        //Sumber yang gagal dimuat diberi tahu, sumber lain tetap tampil
        val failed = states.filterValues { it is HubSourceState.Failed }.keys
        if (failed.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = stringResource(R.string.server_hub_source_failed, failed.joinToString { it.label }),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (visible.isEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(
                        when {
                            loading -> R.string.server_hub_loading
                            favoritesOnly -> R.string.server_hub_empty_favorites
                            else -> R.string.server_hub_empty
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 250.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(visible, key = { it.id }) { server ->
                        val isFavorite = normalizeAddress(server.address) in favoriteIds
                        ServerHubCard(
                            server = server,
                            isFavorite = isFavorite,
                            onToggleFavorite = {
                                if (isFavorite) {
                                    ServerCardManager.removeCard(server.address)
                                } else {
                                    ServerCardManager.addCard(server.name, server.address, server.iconBytes)
                                }
                            },
                            onPlay = { onQuickPlay(null, server.address) },
                            onRemove = if (server.source == HubSource.CUSTOM && server.id.startsWith("custom:")) {
                                { ServerHubRepository.removeCustom(server.address) }
                            } else null,
                            onOpenSource = server.detailUrl?.let { url -> { uriHandler.openUri(url) } }
                        )
                    }
                }
            }
        }

        //Kredit sumber data (wajib untuk API minecraftserver.id)
        Text(
            modifier = Modifier.padding(vertical = 4.dp),
            text = stringResource(R.string.server_hub_credit),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (showAdd) {
        AddServerDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, address ->
                val error = ServerHubRepository.addCustom(name, address)
                if (error == null) {
                    showAdd = false
                    Toast.makeText(context, R.string.server_hub_add_done, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        if (error == "exists") R.string.server_hub_add_exists else R.string.server_hub_add_invalid,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

/** Tombol filter bergaya garis tepi dengan menu turun */
@Composable
private fun <T> FilterDropdown(
    label: String,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = label, maxLines = 1)
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (text, value) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ServerHubCard(
    server: HubServer,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit,
    onRemove: (() -> Unit)?,
    onOpenSource: (() -> Unit)?
) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth(),
        influencedByBackground = false,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ServerIcon(server)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (server.online) {
                                        true -> GreenOnline
                                        false -> RedOffline
                                        null -> GreyUnknown
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (server.online) {
                                true -> stringResource(R.string.server_hub_status_online)
                                false -> stringResource(R.string.server_hub_status_offline)
                                null -> stringResource(R.string.server_hub_status_unknown)
                            } + if (server.online == true && server.maxPlayers > 0) {
                                "  ·  ${server.players}/${server.maxPlayers}"
                            } else "",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        painter = painterResource(
                            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined
                        ),
                        contentDescription = stringResource(R.string.server_hub_filter_favorite),
                        tint = if (isFavorite) RedOffline else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = server.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val chips = buildList {
                server.edition?.let { add(it) }
                server.version?.takeIf { it.isNotBlank() }?.let { add(it.take(16)) }
                addAll(server.tags.take(3))
            }
            if (chips.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    chips.take(4).forEach { Chip(it) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPlay,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = stringResource(R.string.server_hub_play)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (onOpenSource != null) {
                    TextButton(onClick = onOpenSource, contentPadding = PaddingValues(horizontal = 6.dp)) {
                        Text(
                            text = server.source.label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (onRemove != null) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_outlined),
                            contentDescription = stringResource(R.string.generic_delete)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Text(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
    )
}

@Composable
private fun ServerIcon(server: HubServer) {
    val modifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(10.dp))
    val fallback = painterResource(R.drawable.ic_unknown_icon)
    when {
        server.iconBytes != null -> AsyncImage(
            model = server.iconBytes,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = fallback,
            error = fallback,
            modifier = modifier
        )
        server.iconUrl != null -> AsyncImage(
            model = server.iconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = fallback,
            error = fallback,
            modifier = modifier
        )
        else -> Image(
            painter = fallback,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
private fun AddServerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.server_hub_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.server_hub_add_name)) }
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.server_hub_add_address)) },
                    placeholder = { Text("play.contoh.com:25565") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, address) }) {
                Text(stringResource(R.string.generic_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_cancel))
            }
        }
    )
}
