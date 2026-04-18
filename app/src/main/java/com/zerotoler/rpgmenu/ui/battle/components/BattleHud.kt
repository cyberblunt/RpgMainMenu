package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.TextMuted

@Composable
fun BattleTimerTopBar(
    phase: BattlePhase,
    timerSec: Int,
    modifier: Modifier = Modifier,
) {
    val hideTimer = phase == BattlePhase.LAUNCH
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            "AUTO",
            color = TextMuted.copy(alpha = if (hideTimer) 0f else 0.45f),
            fontSize = 10.sp,
        )
        Text(
            "  ${timerSec}s",
            color = CyanGlow.copy(alpha = if (hideTimer) 0f else 1f),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}
