package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.domain.model.PartStats
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun BuildStatPanel(
    stats: PartStats,
    derivedTags: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val spinStr = stats.intervalSeconds?.let { String.format("%.1fs", it) } ?: "--"
    val wtStr = stats.weightGrams?.let { String.format("%.1fg", it) } ?: "--"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xE61A2332))
            .border(1.dp, CyanGlow.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1.4f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TwoColStats(wtStr, spinStr, "Weight", "Time")
            TwoColStats(stats.health?.toString() ?: "--", stats.defense?.toString() ?: "--", "HP", "DEF")
            TwoColStats(stats.attack?.toString() ?: "--", stats.stamina?.toString() ?: "--", "ATK", "STA")
        }
        Column(
            modifier = Modifier.weight(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val tags = derivedTags.take(4)
            if (tags.isEmpty()) {
                repeat(4) { i ->
                    SkillOrbPlaceholder(index = i)
                }
            } else {
                tags.forEach { t ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2C3F55))
                                .border(1.dp, CyanGlow.copy(alpha = 0.35f), CircleShape),
                        )
                        Text(
                            text = t.take(12),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 7.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
        Text("⌄", color = Color(0xFF64B5F6), fontSize = 16.sp, modifier = Modifier.padding(end = 2.dp))
    }
}

@Composable
private fun TwoColStats(leftVal: String, rightVal: String, leftLabel: String, rightLabel: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatMini(leftLabel, leftVal, Modifier.weight(1f))
        StatMini(rightLabel, rightVal, Modifier.weight(1f))
    }
}

@Composable
private fun StatMini(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontSize = 13.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun SkillOrbPlaceholder(index: Int) {
    val hue = listOf(0xFFFFD54F, 0xFF4FC3F7, 0xFFCE93D8, 0xFF81C784)[index % 4]
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(hue).copy(alpha = 0.35f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
    )
}
