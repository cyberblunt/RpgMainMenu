package com.zerotoler.rpgmenu.ui.screens.ranked

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.navigation.Routes
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun RankedLadderScreen(
    viewModel: RankedViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedBotId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .padding(12.dp),
    ) {
        Text("Super Championship", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text(
            "${state.tierName} · ${state.points} pts · tickets ${state.tickets}",
            color = CyanGlow,
            style = MaterialTheme.typography.bodySmall,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.rows, key = { "${it.rank}-${it.name}" }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (row.isPlayer) YellowAccent.copy(alpha = 0.5f) else CyanGlow.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp),
                        )
                        .background(
                            if (row.isPlayer) PanelBlueBright.copy(alpha = 0.95f) else PanelBlueBright.copy(alpha = 0.65f),
                        )
                        .clickable(enabled = !row.isPlayer) {
                            val bot = com.zerotoler.rpgmenu.data.content.BotRoster.ladder
                                .find { it.displayName == row.name }
                            selectedBotId = bot?.id
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("#${row.rank} ${row.name}", color = TextPrimary)
                        Text(row.title, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("${row.points}", color = YellowAccent)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF263A5E),
                ),
            ) {
                Text("Back")
            }
            Button(
                onClick = {
                    val id = selectedBotId ?: com.zerotoler.rpgmenu.data.content.BotRoster.ladder.first().id
                    navController.navigate(Routes.preBattle("ranked", id))
                },
                enabled = state.tickets > 0,
                modifier = Modifier.weight(1f),
            ) {
                Text("Challenge")
            }
        }
    }
}
