package com.movtery.zalithlauncher.ui.screens.content

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.microsoft.MINECRAFT_SERVICES_URL
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.yggdrasil.uploadSkin
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils

private data class McSkin(val name: String, val slim: Boolean = false)

private val featuredSkins = listOf(
    "Notch", "jeb_", "Dinnerbone", "Dream", "GeorgeNotFound", "Sapnap", "Technoblade", "TommyInnit",
    "Ph1LzA", "Ranboo", "Tubbo", "CaptainSparklez", "Alex", "Steve", "Mumbo", "Grian",
    "GoodTimesWithScar", "Xisuma", "Iskall85", "FalseSymmetry", "LDShadowLady", "Smallishbeans",
    "Etho", "VintageBeef", "BdoubleO100", "ZombieCleo", "Docm77", "Tango", "ImpulseSV", "Skizzleman"
).map { McSkin(it) }

private fun skinPngUrl(name: String) = "https://minotar.net/skin/$name"
private fun skinPreviewUrl(name: String) = "https://minotar.net/avatar/$name/64.png"

@Composable
fun McSkinScreen() {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(featuredSkins) }
    var visible by remember { mutableIntStateOf(4) }
    var selected by remember { mutableStateOf<McSkin?>(null) }
    var searching by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val shown by remember { derivedStateOf { results.take(visible) } }

    LaunchedEffect(listState, results, visible) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { last -> if (last >= visible - 1 && visible < results.size) visible += 4 }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Minecraft username") }
            )
            Button(
                enabled = !searching,
                onClick = {
                    val name = query.trim()
                    if (name.isEmpty()) return@Button
                    scope.launch {
                        searching = true
                        val found = runCatching { lookupSkins(name) }.getOrElse { emptyList() }
                        results = found
                        visible = 4
                        selected = null
                        message = if (found.isEmpty()) "No skin found for $name." else ""
                        searching = false
                    }
                }
            ) { Text(if (searching) "..." else "Search") }
        }
        if (message.isNotEmpty()) Text(message, style = MaterialTheme.typography.bodySmall)
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(shown, key = { it.name }) { skin ->
                SkinOption(skin, selected?.name == skin.name, Modifier.fillParentMaxHeight(0.25f)) { selected = skin }
            }
            if (visible < results.size) {
                item {
                    Box(Modifier.fillMaxWidth().height(36.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
        Button(
            enabled = selected != null && !busy && account != null && (account!!.isLocalAccount() || account!!.isMicrosoftAccount()),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val skin = selected ?: return@Button
                val target = account ?: return@Button
                scope.launch {
                    busy = true
                    message = "Downloading ${skin.name}..."
                    val result = runCatching { equipSkin(target, skin) }
                    message = result.fold(
                        onSuccess = { "Equipped ${skin.name} on ${target.username}." },
                        onFailure = { "Could not equip ${skin.name}: ${it.message ?: it.javaClass.simpleName}" }
                    )
                    busy = false
                }
            }
        ) {
            if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text(if (selected == null) "Choose a skin" else "Download ${selected!!.name}")
        }
    }
}

@Composable
private fun SkinOption(skin: McSkin, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var bitmap by remember(skin.name) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(skin.name) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val bytes = GLOBAL_CLIENT.get(skinPreviewUrl(skin.name)).bodyAsBytes()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.getOrNull()
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (bitmap != null) {
                Image(bitmap!!.asImageBitmap(), contentDescription = skin.name, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
            } else {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            }
            Text(skin.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
    }
}

private suspend fun lookupSkins(query: String): List<McSkin> = withContext(Dispatchers.IO) {
    val name = query.trim()
    val exact = runCatching {
        val response = GLOBAL_CLIENT.get("https://api.mojang.com/users/profiles/minecraft/$name")
        if (response.status.isSuccess()) {
            val body = response.bodyAsBytes().decodeToString()
            val official = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
            official?.let { McSkin(it) }
        } else null
    }.getOrNull()
    val featured = featuredSkins.filter { it.name.contains(name, ignoreCase = true) && it.name != exact?.name }
    listOfNotNull(exact) + featured
}

private suspend fun equipSkin(account: Account, skin: McSkin) = withContext(Dispatchers.IO) {
    val bytes = GLOBAL_CLIENT.get(skinPngUrl(skin.name)).bodyAsBytes()
    require(bytes.size > 32) { "Skin download was empty" }
    val file = account.getSkinFile()
    FileUtils.forceMkdir(file.parentFile)
    file.writeBytes(bytes)
    account.skinModelType = if (skin.slim) SkinModelType.ALEX else SkinModelType.STEVE
    if (account.isMicrosoftAccount()) {
        uploadSkin(MINECRAFT_SERVICES_URL, account.accessToken, file, account.skinModelType)
    }
    AccountsManager.suspendSaveAccount(account)
    AccountsManager.refreshWardrobe()
}
