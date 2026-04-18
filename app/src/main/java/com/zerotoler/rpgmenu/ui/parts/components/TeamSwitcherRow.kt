package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.Team
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun TeamSwitcherRow(
    teams: List<Team>,
    activeTeamId: String,
    onSelectTeam: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        teams.forEach { team ->
            val selected = team.id == activeTeamId
            val borderColor = if (selected) YellowAccent else CyanGlow.copy(alpha = 0.35f)
            Row(
                modifier = Modifier
                    .size(width = 36.dp, height = 34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) PanelBlueBright else PanelBlueBright.copy(alpha = 0.65f))
                    .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable { onSelectTeam(team.id) }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = team.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
