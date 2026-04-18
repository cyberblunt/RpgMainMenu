package com.zerotoler.rpgmenu.ui.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.GemGreen
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.PanelBlue
import com.zerotoler.rpgmenu.ui.theme.PurpleAccent
import com.zerotoler.rpgmenu.ui.theme.PurpleAccentDeep
import com.zerotoler.rpgmenu.ui.theme.RpgMainMenuTheme
import com.zerotoler.rpgmenu.ui.theme.SuperChampionshipPanel
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun ModeGrid(
    onEmpty1: () -> Unit,
    onEmpty2: () -> Unit,
    onEmpty3: () -> Unit,
    onEmpty4: () -> Unit,
    onAdventure: () -> Unit,
    onSuperChampionship: () -> Unit,
    onFreeChest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EmptySlotCard(
                    label = "EMPTY",
                    onClick = onEmpty1,
                    modifier = Modifier.weight(1f),
                )
                EmptySlotCard(
                    label = "EMPTY",
                    onClick = onEmpty2,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EmptySlotCard(
                    label = "EMPTY",
                    onClick = onEmpty3,
                    modifier = Modifier.weight(1f),
                )
                EmptySlotCard(
                    label = "EMPTY",
                    onClick = onEmpty4,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd,
            ) {
                FreeChestButton(onClick = onFreeChest)
            }
            ModeCard(
                title = "ADVENTURE",
                onClick = onAdventure,
                brush = Brush.horizontalGradient(
                    listOf(PurpleAccentDeep, PurpleAccent, PurpleAccentDeep),
                ),
                accentBorder = PurpleAccent,
                showLeftAccent = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            ModeCard(
                title = "SUPER CHAMPIONSHIP",
                onClick = onSuperChampionship,
                brush = Brush.verticalGradient(
                    listOf(SuperChampionshipPanel, PanelBlue),
                ),
                accentBorder = CyanGlow.copy(alpha = 0.4f),
                emblem = { SuperChampionshipEmblem() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
fun EmptySlotCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBlue)
            .border(1.dp, CyanGlow.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ModeCard(
    title: String,
    onClick: () -> Unit,
    brush: Brush,
    accentBorder: Color,
    modifier: Modifier = Modifier,
    showLeftAccent: Boolean = false,
    emblem: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, accentBorder.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .background(brush)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        if (showLeftAccent) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.22f)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyanGlow.copy(alpha = 0.12f)),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
            if (emblem != null) {
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Box(modifier = Modifier.height(48.dp)) {
                    emblem()
                }
            }
        }
    }
}

@Composable
private fun SuperChampionshipEmblem() {
    Box(
        modifier = Modifier
            .height(44.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.radialGradient(
                    listOf(YellowAccent.copy(alpha = 0.35f), Color.Transparent),
                ),
            )
            .border(1.dp, YellowAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "I",
            style = MaterialTheme.typography.headlineLarge,
            color = CyanGlow,
        )
    }
}

@Composable
fun FreeChestButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 4.dp, bottom = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .width(76.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(GemGreen, GemGreen.copy(alpha = 0.75f)),
                    ),
                )
                .border(1.dp, CyanGlow.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "CHEST",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF04261C),
            )
        }
        Text(
            text = "Free Chest",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary.copy(alpha = 0.85f),
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Preview(showBackground = true, heightDp = 360, widthDp = 400)
@Composable
private fun ModeGridPreview() {
    RpgMainMenuTheme {
        Box(
            modifier = Modifier
                .background(NavyBackground)
                .height(220.dp),
        ) {
            ModeGrid(
                onEmpty1 = {},
                onEmpty2 = {},
                onEmpty3 = {},
                onEmpty4 = {},
                onAdventure = {},
                onSuperChampionship = {},
                onFreeChest = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
