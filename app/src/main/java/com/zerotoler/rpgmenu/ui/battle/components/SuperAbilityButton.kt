package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.TextMuted

@Composable
fun SuperAbilityButton(
    phase: BattlePhase,
    meter: Float,
    abilityActive: Boolean,
    remainingSec: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (phase != BattlePhase.ACTIVE) return
    val ready = meter >= 100f && !abilityActive
    val label = when {
        abilityActive -> "SUPER ${remainingSec.toInt()}s"
        ready -> "SUPER"
        else -> "${meter.toInt()}%"
    }
    val bg = when {
        abilityActive -> Brush.horizontalGradient(
            listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
        )
        ready -> Brush.horizontalGradient(listOf(CyanGlow, Color(0xFF00BCD4)))
        else -> Brush.horizontalGradient(listOf(TextMuted.copy(alpha = 0.4f), TextMuted.copy(alpha = 0.25f)))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = ready) { onClick() }
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (ready || abilityActive) NavyBackground else TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}
