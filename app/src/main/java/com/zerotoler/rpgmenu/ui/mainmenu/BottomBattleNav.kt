package com.zerotoler.rpgmenu.ui.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.RpgMainMenuTheme
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent
import com.zerotoler.rpgmenu.ui.theme.YellowAccentDark

@Composable
fun BottomBattleNav(
    onNav1: () -> Unit,
    onNav2: () -> Unit,
    onBattle: () -> Unit,
    onNav4: () -> Unit,
    onNav5: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyBackground, NavyBackgroundEnd),
                ),
            )
            .border(1.dp, CyanGlow.copy(alpha = 0.15f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomNavIconItem(
            glyph = "bag",
            label = "Nav1",
            onClick = onNav1,
            badgeCount = 33,
        )
        BottomNavIconItem(
            glyph = "eye",
            label = "Nav2",
            onClick = onNav2,
        )
        BattleNavCenterItem(onClick = onBattle)
        BottomNavIconItem(
            glyph = "mob",
            label = "Nav4",
            onClick = onNav4,
            showDotBadge = true,
        )
        BottomNavIconItem(
            glyph = "fr",
            label = "Nav5",
            onClick = onNav5,
            showDotBadge = true,
        )
    }
}

@Composable
private fun BottomNavIconItem(
    glyph: String,
    label: String,
    onClick: () -> Unit,
    badgeCount: Int? = null,
    showDotBadge: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBlueBright)
                    .border(1.dp, CyanGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                NavGlyphPlaceholder(glyph)
            }
            when {
                badgeCount != null && badgeCount > 0 -> {
                    NotificationBadge(
                        modifier = Modifier.align(Alignment.TopEnd),
                        count = badgeCount,
                    )
                }
                showDotBadge -> {
                    NotificationBadge(
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NavGlyphPlaceholder(kind: String) {
    val (a, b) = when (kind) {
        "bag" -> "M" to "1"
        "eye" -> "O" to "·"
        "mob" -> "V" to "~"
        else -> "N" to "5"
    }
    Text(
        text = "$a$b",
        style = MaterialTheme.typography.labelSmall,
        color = CyanGlow,
    )
}

@Composable
private fun BattleNavCenterItem(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(56.dp)
                .width(88.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(YellowAccent, YellowAccentDark),
                    ),
                )
                .border(1.dp, YellowAccentDark.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "B",
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyBackgroundEnd,
                )
                Text(
                    text = "BATTLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NavyBackgroundEnd,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Battle",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
        )
    }
}
