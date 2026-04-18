package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.VisibleInventoryItem
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.PanelBlue
import com.zerotoler.rpgmenu.ui.theme.PurpleAccent
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun PartCard(
    item: VisibleInventoryItem,
    onEquip: () -> Unit,
    onOpenDetail: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val p = item.part
    val st = item.player
    val enabled = st.owned
    val rarityTint = when (p.rarity) {
        5 -> PurpleAccent.copy(alpha = 0.35f)
        4 -> Color(0xFFFF8F00).copy(alpha = 0.25f)
        3 -> Color(0xFF1565C0).copy(alpha = 0.28f)
        else -> PanelBlue.copy(alpha = 0.7f)
    }
    val borderCol = when {
        item.isEquippedInActiveLoadout -> YellowAccent
        else -> CyanGlow.copy(alpha = 0.2f)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(rarityTint, PanelBlue.copy(alpha = 0.95f)),
                ),
            )
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onEquip,
            )
            .alpha(if (enabled) 1f else 0.45f)
            .padding(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RowMeta(p.rarity, p.combatType.name, Modifier.weight(1f))
            if (onOpenDetail != null) {
                Text(
                    "i",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, CyanGlow.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onOpenDetail,
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        Text(
            p.name,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "Lv.${st.level}",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                p.category.name.take(3),
                style = MaterialTheme.typography.labelSmall,
                color = CyanGlow,
            )
        }
        val shardDenom = (st.level + 1).coerceAtLeast(2)
        LinearProgressIndicator(
            progress = { (st.shardCount % shardDenom).toFloat() / shardDenom.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = CyanGlow,
            trackColor = Color(0xFF0A1628),
        )
        Text(
            "${st.shardCount}/$shardDenom",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (!enabled) {
            Text("LOCKED", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        if (item.isEquippedInActiveLoadout) {
            Text("EQUIPPED", style = MaterialTheme.typography.labelSmall, color = YellowAccent)
        }
    }
}

@Composable
private fun RowMeta(rarity: Int?, combat: String, modifier: Modifier = Modifier) {
    Text(
        "R:${rarity ?: "-"} · $combat",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        maxLines = 1,
        modifier = modifier,
    )
}
