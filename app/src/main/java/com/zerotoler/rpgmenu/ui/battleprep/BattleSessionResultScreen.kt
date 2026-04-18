package com.zerotoler.rpgmenu.ui.battleprep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundResult
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun BattleSessionResultScreen(
    viewModel: BattleSessionResultViewModel,
    onFinish: () -> Unit,
    onRetry: (mode: String, opponentToken: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val s = state.session

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Session Result",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Wins: ${state.wins}   Losses: ${state.losses}",
            color = CyanGlow,
            style = MaterialTheme.typography.titleMedium,
        )
        if (s != null) {
            s.rounds.forEach { r ->
                val resLabel = when (r.result) {
                    BattleRoundResult.WIN -> "WIN"
                    BattleRoundResult.LOSS -> "LOSS"
                    null -> "—"
                }
                val playerName = r.selectedPlayerSlotIndex?.let { idx ->
                    s.playerTops.getOrNull(idx)?.displayName
                } ?: "—"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CyanGlow.copy(alpha = 0.08f),
                            RoundedCornerShape(10.dp),
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Round ${r.roundIndex + 1}", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("vs ${r.opponentTop.name}", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text("Your top: $playerName", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(resLabel, color = if (r.result == BattleRoundResult.WIN) CyanGlow else TextMuted, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text("No session data.", color = TextMuted)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.clearSession(onFinish)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Finish", color = NavyBackground, fontWeight = FontWeight.Bold)
        }
        val mode = s?.mode.orEmpty()
        val token = s?.opponentToken.orEmpty()
        Button(
            onClick = {
                viewModel.prepareRetry(mode, token) {
                    onRetry(mode, token)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanGlow.copy(alpha = 0.35f)),
            enabled = s != null,
        ) {
            Text("Retry session", color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}
