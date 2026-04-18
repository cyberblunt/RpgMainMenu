package com.zerotoler.rpgmenu.ui.challenge

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
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.navigation.Routes
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun ChallengeHubScreen(
    navController: NavHostController,
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
        Text("CHALLENGE", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text(
            "Gateway to offline battles and ladders.",
            style = MaterialTheme.typography.bodySmall,
            color = CyanGlow,
        )
        ChallengeTile("Practice Battle", "Launch with your active loadout.") {
            navController.navigate(Routes.preBattle("practice", "none"))
        }
        ChallengeTile("Super Championship", "Ranked ladder vs local rivals.") {
            navController.navigate(Routes.RANKED_LADDER)
        }
        ChallengeTile("Battle Pass", "Seasonal track progress.") {
            navController.navigate(Routes.BATTLE_PASS)
        }
        ChallengeTile("Event Missions", "Limited-time objectives.") {
            navController.navigate(Routes.EVENT_MISSIONS)
        }
        ChallengeTile("Event Board", "Bingo-style reward cells.") {
            navController.navigate(Routes.EVENT_BOARD)
        }
        ChallengeTile("Recent results", "Opens match history in profile flow.") {
            navController.navigate(Routes.MENU)
        }
    }
}

@Composable
private fun ChallengeTile(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(PanelBlueBright.copy(alpha = 0.88f), NavyBackgroundEnd),
                ),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CyanGlow)
    }
}
