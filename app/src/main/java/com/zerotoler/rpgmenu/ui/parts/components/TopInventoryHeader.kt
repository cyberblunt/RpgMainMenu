package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.PanelBlue
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun TopInventoryHeader(
    teamDisplayName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(PanelBlue, PanelBlue.copy(alpha = 0.85f)),
                ),
            )
            .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("LOADOUT", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
                teamDisplayName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 24.sp,
                ),
                color = TextPrimary,
            )
        }
    }
}
