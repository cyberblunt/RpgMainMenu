package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun ParticipantHud(
    name: String,
    archetype: CombatType,
    hp: Int,
    hpMax: Int,
    st: Int,
    stMax: Int,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val hpPct = remember(hp, hpMax) { if (hpMax <= 0) 0f else hp / hpMax.toFloat() }
    val stPct = remember(st, stMax) { if (stMax <= 0) 0f else st / stMax.toFloat() }
    val tc = typeColor(archetype)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyBackground.copy(alpha = 0.82f))
            .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        ) {
            if (!alignEnd) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(tc.copy(alpha = 0.45f)),
                )
                Spacer(modifier = Modifier.size(6.dp))
            }
            Column {
                Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(archetype.name, color = TextMuted, fontSize = 9.sp)
            }
            if (alignEnd) {
                Spacer(modifier = Modifier.size(6.dp))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(tc.copy(alpha = 0.45f)),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("HP", color = TextMuted, fontSize = 8.sp)
        LinearProgressIndicator(
            progress = { hpPct.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFFE53935),
            trackColor = Color.White.copy(alpha = 0.08f),
        )
        Text("$hp / $hpMax", color = TextPrimary, fontSize = 9.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("ST", color = TextMuted, fontSize = 8.sp)
        LinearProgressIndicator(
            progress = { stPct.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF43A047),
            trackColor = Color.White.copy(alpha = 0.08f),
        )
        Text("$st / $stMax", color = TextPrimary, fontSize = 9.sp)
    }
}

private fun typeColor(t: CombatType): Color = when (t) {
    CombatType.ATTACK -> Color(0xFFE53935)
    CombatType.DEFENSE -> Color(0xFF1E88E5)
    CombatType.STAMINA -> Color(0xFF43A047)
    CombatType.BALANCE -> Color(0xFF8E24AA)
    CombatType.UNKNOWN -> Color.Gray
}
