package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.domain.model.battle.BattleOutcome
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun ResultOverlay(
    phase: BattlePhase,
    outcome: BattleOutcome,
    busy: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (phase != BattlePhase.RESULT || outcome == BattleOutcome.NONE) return
    val win = outcome == BattleOutcome.PLAYER_WIN
    val bg = if (win) {
        Brush.verticalGradient(listOf(Color(0xFF1B5E20).copy(alpha = 0.92f), NavyBackground.copy(alpha = 0.95f)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFB71C1C).copy(alpha = 0.92f), NavyBackground.copy(alpha = 0.95f)))
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(bg, RoundedCornerShape(18.dp))
                .padding(horizontal = 28.dp, vertical = 24.dp),
        ) {
            Text(
                if (win) "VICTORY" else "DEFEAT",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onContinue,
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Continue", color = NavyBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}
