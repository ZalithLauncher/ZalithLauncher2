package com.movtery.zalithlauncher.ui.screens.content

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils

private data class McSkin(val name: String, val slim: Boolean = false)

private val featuredSkins = listOf(
    McSkin("Notch"),
    McSkin("jeb_"),
    McSkin("Dinnerbone"),
    McSkin("Dream"),
    McSkin("GeorgeNotFound"),
    McSkin("Sapnap"),
    McSkin("Technoblade"),
    McSkin("TommyInnit"),
    McSkin("Ph1LzA"),
    McSkin("Ranboo"),
    McSkin("Tubbo"),
    McSkin("CaptainSparklez")
)

private fun skinPngUrl(name: String) = "https://minotar.net/skin/$name"
private fun skinPreviewUrl(name: String) = "https://minotar.net/body/$name/128.png"

@Composable
fun McSkinScreen() {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var skins by remember { mutableStateOf(featuredSkins) }
    var selected by remember { mutableStateOf<McSkin?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Pick a skin, then equip it on your offline or Microsoft account.") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("MCSkin", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Online Minecraft skins. Create an offline or Microsoft account first, then download and equip.",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Minecraft username") }
            )
            Button(onClick = {
                val name = query.trim()
                if (name.isNotEmpty()) {
                    skins = listOf(McSkin(name)) + featuredSkins.filter { it.name.equals(name, true).not() }
                    selected = McSkin(name)
                }
            }) { Text("Search") }
        }
        if (account == null || !(account!!.isLocalAccount() || account!!.isMicrosoftAccount())) {
            Text("No offline or Microsoft account selected. Add one in Accounts, then come back.")
        } else {
            Text("Equipping to ${account!!.username}")
        }
        Text(message)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(skins, key = { it.name }) { skin ->
                SkinCard(skin = skin, selected = selected?.name == skin.name, onClick = { selected = skin })
            }
        }
        Button(
            enabled = selected != null && !busy && account != null && (account!!.isLocalAccount() || account!!.isMicrosoftAccount()),
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
            else Text("Download and equip")
        }
    }
}

@Composable
private fun SkinCard(skin: McSkin, selected: Boolean, onClick: () -> Unit) {
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (bitmap != null) {
                Image(bitmap!!.asImageBitmap(), contentDescription = skin.name, modifier = Modifier.size(96.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
            } else {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
            Text(skin.name, maxLines = 1)
        }
    }
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
