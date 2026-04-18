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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.data.content.TalentCatalog
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun TalentTreeScreen(
    viewModel: TalentTreeViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(12.dp),
    ) {
        Text("Talent Tree", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text("Gold: ${state.gold}", color = YellowAccent)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(TalentCatalog.nodes, key = { it.id }) { node ->
                val unlocked = node.id in state.unlocked
                val prereqOk = node.prerequisiteIds.all { it in state.unlocked }
                val canUnlock = !unlocked && prereqOk && state.gold >= node.costGold
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
                    Text(node.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(node.description, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Text("Cost ${node.costGold} gold", color = CyanGlow, style = MaterialTheme.typography.labelSmall)
                    when {
                        unlocked -> Text("Unlocked", color = YellowAccent)
                        canUnlock -> Button(onClick = { viewModel.unlock(node.id) }) { Text("Unlock") }
                        else -> Text("Locked", color = TextMuted)
                    }
                }
            }
        }
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Back")
        }
    }

    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = { TextButton(onClick = viewModel::clearError) { Text("OK") } },
            title = { Text("Talent") },
            text = { Text(msg) },
        )
    }
}
