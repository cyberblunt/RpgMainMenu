package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    rpm: Int,
    rpmMax: Int,
    st: Int,
    stMax: Int,
    attack: Int,
    defense: Int,
    weightGrams: Int,
    reverseBars: Boolean,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val hpPct = remember(hp, hpMax) { if (hpMax <= 0) 0f else hp / hpMax.toFloat() }
    val rpmPct = remember(rpm, rpmMax) { if (rpmMax <= 0) 0f else rpm / rpmMax.toFloat() }
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "A $attack   D $defense   ${weightGrams}g",
            color = TextMuted,
            fontSize = 9.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (reverseBars) {
            HudProgressBar(label = "RPM", fraction = rpmPct, color = Color(0xFFFFC107), reverse = true)
            Spacer(modifier = Modifier.height(4.dp))
            HudProgressBar(label = "HP", fraction = hpPct, color = Color(0xFF4CAF50), reverse = true)
            Spacer(modifier = Modifier.height(4.dp))
            HudProgressBar(label = "ST", fraction = stPct, color = Color(0xFF43A047), reverse = true)
        } else {
            HudProgressBar(label = "HP", fraction = hpPct, color = Color(0xFF4CAF50), reverse = false)
            Spacer(modifier = Modifier.height(4.dp))
            HudProgressBar(label = "RPM", fraction = rpmPct, color = Color(0xFFFFC107), reverse = false)
            Spacer(modifier = Modifier.height(4.dp))
            HudProgressBar(label = "ST", fraction = stPct, color = Color(0xFF43A047), reverse = false)
        }
    }
}

@Composable
private fun HudProgressBar(
    label: String,
    fraction: Float,
    color: Color,
    reverse: Boolean,
) {
    val f = fraction.coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (!reverse) {
            Text(label, color = TextMuted, fontSize = 8.sp, modifier = Modifier.padding(end = 4.dp))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.08f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(f)
                    .align(if (reverse) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(color),
            )
        }
        if (reverse) {
            Text(label, color = TextMuted, fontSize = 8.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

private fun typeColor(t: CombatType): Color = when (t) {
    CombatType.ATTACK -> Color(0xFFE53935)
    CombatType.DEFENSE -> Color(0xFF1E88E5)
    CombatType.STAMINA -> Color(0xFF43A047)
    CombatType.BALANCE -> Color(0xFF8E24AA)
    CombatType.UNKNOWN -> Color.Gray
}
