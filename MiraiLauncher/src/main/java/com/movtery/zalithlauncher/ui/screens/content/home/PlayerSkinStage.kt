package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.ui.components.SkinPreview3D

@Composable
fun PlayerSkinStage(modifier: Modifier = Modifier) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val currentAccount = account
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
    val skinFile = remember(currentAccount, refreshWardrobe) { currentAccount?.getSkinFile()?.takeIf { it.exists() } }
    val capeFile = remember(currentAccount, refreshWardrobe) { currentAccount?.getCapeFile()?.takeIf { it.exists() } }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SkinPreview3D(
            modifier = Modifier.width(180.dp).height(240.dp),
            skinFile = skinFile,
            capeFile = capeFile,
            modelType = currentAccount?.skinModelType,
            interactionEnabled = true,
            azimuth = 18,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = currentAccount?.username ?: "No account",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (currentAccount != null) getAccountTypeName(currentAccount) else "Add an account",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
