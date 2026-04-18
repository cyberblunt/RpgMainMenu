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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.GemGreen
import com.zerotoler.rpgmenu.ui.theme.GoldTint
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.PanelBlue
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.RpgMainMenuTheme
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun TopHud(
    playerName: String,
    goldAmount: String,
    gemAmount: String,
    onProfileClick: () -> Unit,
    onGoldClick: () -> Unit,
    onGoldPlusClick: () -> Unit,
    onGemsClick: () -> Unit,
    onGemsPlusClick: () -> Unit,
    onMerchantClick: () -> Unit,
    onEventsClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ProfileBlock(
                playerName = playerName,
                onClick = onProfileClick,
                modifier = Modifier.weight(1f, fill = false),
            )
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CurrencyChip(
                    labelLeft = "●",
                    tintGold = true,
                    value = goldAmount,
                    onChipClick = onGoldClick,
                    onPlusClick = onGoldPlusClick,
                )
                CurrencyChip(
                    labelLeft = "◆",
                    tintGold = false,
                    value = gemAmount,
                    onChipClick = onGemsClick,
                    onPlusClick = onGemsPlusClick,
                )
            }
            Row(
                modifier = Modifier.weight(1f, fill = false),
                horizontalArrangement = Arrangement.End,
            ) {
                ShortcutButton(icon = "★", label = "Merchant", onClick = onMerchantClick)
                Spacer(modifier = Modifier.width(4.dp))
                ShortcutButton(
                    icon = "◇",
                    label = "Events",
                    onClick = onEventsClick,
                    showBadge = true,
                )
                Spacer(modifier = Modifier.width(4.dp))
                ShortcutButton(
                    icon = "△",
                    label = "Missions",
                    onClick = onMissionsClick,
                    showBadge = true,
                )
                Spacer(modifier = Modifier.width(4.dp))
                ShortcutButton(icon = "≡", label = "Menu", onClick = onMenuClick)
            }
        }
    }
}

@Composable
fun ProfileBlock(
    playerName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PanelBlueBright, PanelBlue),
                    ),
                )
                .border(1.dp, CyanGlow.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(":", color = CyanGlow, style = MaterialTheme.typography.titleMedium)
        }
        Column(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .rotate(-8f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E5AA8))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        "GP 15",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { 0.55f },
                    modifier = Modifier
                        .height(6.dp)
                        .width(72.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GemGreen,
                    trackColor = PanelBlue,
                )
            }
        }
    }
}

@Composable
fun CurrencyChip(
    labelLeft: String,
    tintGold: Boolean,
    value: String,
    onChipClick: () -> Unit,
    onPlusClick: () -> Unit,
) {
    val accent = if (tintGold) GoldTint else GemGreen
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PanelBlue.copy(alpha = 0.92f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = labelLeft,
            color = accent,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onChipClick)
                .padding(4.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onChipClick)
                .padding(horizontal = 4.dp),
        )
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(YellowAccent)
                .clickable(onClick = onPlusClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "+",
                style = MaterialTheme.typography.titleMedium,
                color = NavyBackground,
            )
        }
    }
}

@Composable
fun ShortcutButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    showBadge: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PanelBlueBright)
                    .border(1.dp, CyanGlow.copy(alpha = 0.25f), CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, color = CyanGlow, style = MaterialTheme.typography.labelSmall)
            }
            if (showBadge) {
                NotificationBadge()
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun TopHudPreview() {
    RpgMainMenuTheme {
        Box(modifier = Modifier.background(NavyBackground)) {
            TopHud(
                playerName = "LEO-GGRAON",
                goldAmount = "180625",
                gemAmount = "2973",
                onProfileClick = {},
                onGoldClick = {},
                onGoldPlusClick = {},
                onGemsClick = {},
                onGemsPlusClick = {},
                onMerchantClick = {},
                onEventsClick = {},
                onMissionsClick = {},
                onMenuClick = {},
            )
        }
    }
}
