package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.PartStats
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.PanelBlue
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun BuildStatPanel(
    stats: PartStats,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBlue.copy(alpha = 0.9f))
            .border(1.dp, CyanGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("TOTAL STATS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCell("HP", stats.health?.toString(), Modifier.weight(1f))
            StatCell("ATK", stats.attack?.toString(), Modifier.weight(1f))
            StatCell("DEF", stats.defense?.toString(), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCell("STA", stats.stamina?.toString(), Modifier.weight(1f))
            StatCell("INT", stats.intervalSeconds?.let { String.format("%.1fs", it) }, Modifier.weight(1f))
            StatCell("WT", stats.weightGrams?.let { String.format("%.1fg", it) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            text = value ?: "--",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            textAlign = TextAlign.End,
        )
    }
}
