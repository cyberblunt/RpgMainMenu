package com.zerotoler.rpgmenu.ui.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.RpgApplication
import com.zerotoler.rpgmenu.data.content.CollectionSetDefinitions
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent
import kotlinx.coroutines.flow.map

@Composable
fun CollectionCodexScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.applicationContext as RpgApplication
    val ownedIds by app.container.inventoryRepository.getAllPlayerStatesFlow()
        .map { states -> states.filter { it.owned }.map { it.partId }.toSet() }
        .collectAsStateWithLifecycle(initialValue = emptySet())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(12.dp),
    ) {
        Text("Collection / Codex", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text("Bonuses apply automatically when you own every listed part.", color = CyanGlow, style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CollectionSetDefinitions.sets, key = { it.id }) { set ->
                val ownedCount = set.requiredPartIds.count { it in ownedIds }
                val complete = ownedCount == set.requiredPartIds.size
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PanelBlueBright.copy(alpha = 0.85f), NavyBackgroundEnd),
                            ),
                        )
                        .padding(12.dp),
                ) {
                    Text(set.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Progress $ownedCount / ${set.requiredPartIds.size}",
                        color = if (complete) YellowAccent else TextMuted,
                    )
                    Text(set.bonusDescription, color = CyanGlow, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Back")
        }
    }
}
