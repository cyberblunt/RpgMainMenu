package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.PartCategory
import com.zerotoler.rpgmenu.ui.mainmenu.NotificationBadge
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun PartCategoryTabs(
    selected: PartCategory,
    counts: Map<PartCategory, Int>,
    onSelect: (PartCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PartCategory.entries.forEach { cat ->
            val label = when (cat) {
                PartCategory.BATTLE_CAP -> "Battle Cap"
                PartCategory.WEIGHT_RING -> "Weight Ring"
                PartCategory.DRIVER -> "Driver"
            }
            val count = counts[cat] ?: 0
            val sel = selected == cat
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (sel) YellowAccent.copy(alpha = 0.35f) else PanelBlueBright)
                    .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .clickable { onSelect(cat) }
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        maxLines = 2,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    NotificationBadge(
                        modifier = Modifier.align(Alignment.TopEnd),
                        count = count.coerceAtMost(99),
                    )
                }
            }
        }
    }
}
