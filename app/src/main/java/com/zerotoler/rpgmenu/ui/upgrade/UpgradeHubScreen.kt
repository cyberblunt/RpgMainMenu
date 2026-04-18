package com.zerotoler.rpgmenu.ui.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun UpgradeHubScreen(
    onTalent: () -> Unit,
    onCollection: () -> Unit,
    onEnhance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("UPGRADE", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text(
            "Progression systems — talents, codex sets, and enhancement.",
            style = MaterialTheme.typography.bodySmall,
            color = CyanGlow,
        )
        UpgradeTile("Talent Tree", "Unlock passive chassis bonuses.", onTalent)
        UpgradeTile("Collection / Codex", "Set bonuses from owned parts.", onCollection)
        UpgradeTile("Enhancement / Potential", "Shards, levels, and future fusion.", onEnhance)
    }
}

@Composable
private fun UpgradeTile(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(PanelBlueBright.copy(alpha = 0.9f), NavyBackgroundEnd),
                ),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CyanGlow)
    }
}
