package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel

@Composable
fun UnpackScreen(
    items: List<InstallableItem>,
    screenViewModel: SplashBackStackViewModel,
    onAgreeClick: () -> Unit = {}
) {
    BaseScreen(screenKey = NormalNavKey.UnpackDeps, currentKey = screenViewModel.splashScreen.currentKey) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UnpackTaskList(items = items, modifier = Modifier.weight(7f).fillMaxHeight())
            ActionMenu(modifier = Modifier.weight(3f).fillMaxHeight(), onAgreeClick = onAgreeClick)
        }
    }
}

@Composable
private fun ActionMenu(modifier: Modifier = Modifier, onAgreeClick: () -> Unit = {}) {
    var installing by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.fillMaxWidth().weight(1f),
            text = stringResource(if (installing) R.string.splash_screen_installing else R.string.splash_screen_unpack_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Button(
            enabled = !installing,
            onClick = { installing = true; onAgreeClick() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x991BD96A),
                contentColor = Color(0xFF06210F),
                disabledContainerColor = Color(0x551BD96A),
                disabledContentColor = Color(0xFF06210F)
            )
        ) {
            Text(stringResource(R.string.splash_screen_agree))
        }
    }
}

@Composable
private fun UnpackTaskList(items: List<InstallableItem>, modifier: Modifier = Modifier) {
    BackgroundCard(modifier = modifier, influencedByBackground = false, shape = MaterialTheme.shapes.extraLarge) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            items(items, key = { it.name }) { item ->
                TaskItem(item = item, modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun TaskItem(item: InstallableItem, modifier: Modifier = Modifier) {
    val state by item.state.collectAsStateWithLifecycle()
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.labelMedium, color = Color.White)
            item.summary?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color(0xFFD7CFC8)) }
        }
        val iconModifier = Modifier.padding(horizontal = 12.dp).size(18.dp)
        when (state) {
            InstallableItem.State.RUNNING -> CircularProgressIndicator(modifier = iconModifier, strokeWidth = 2.dp, color = Color(0xFF1BD96A))
            InstallableItem.State.FINISHED -> Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = iconModifier, tint = Color(0xFF1BD96A))
            InstallableItem.State.PENDING -> Icon(painterResource(R.drawable.ic_update), contentDescription = null, modifier = iconModifier, tint = Color.White)
            else -> Icon(painterResource(R.drawable.ic_folder_zip_outlined), contentDescription = null, modifier = iconModifier, tint = Color.White)
        }
    }
}
