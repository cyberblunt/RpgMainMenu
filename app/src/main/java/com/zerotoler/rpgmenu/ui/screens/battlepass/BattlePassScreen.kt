package com.zerotoler.rpgmenu.ui.screens.battlepass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.RpgApplication
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent
import kotlinx.coroutines.launch

@Composable
fun BattlePassScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as RpgApplication
    val bp by app.container.battlePassService.observeBattlePass().collectAsStateWithLifecycle(
        initialValue = com.zerotoler.rpgmenu.domain.model.progress.BattlePassProgress(),
    )
    val scope = rememberCoroutineScope()
    val tiers = (1..12).toList()
    val xpPerTier = 150

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        Text("Battle Pass · ${bp.seasonId}", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text("XP ${bp.xp}", color = YellowAccent)
        val into = (bp.xp % xpPerTier).toFloat() / xpPerTier
        LinearProgressIndicator(
            progress = { into },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            color = CyanGlow,
            trackColor = Color(0xFF1A2744),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(tiers) { tier ->
                val need = tier * xpPerTier
                val unlocked = bp.xp >= need
                val claimed = tier in bp.claimedTiers
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CyanGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .background(PanelBlueBright.copy(alpha = if (unlocked) 0.9f else 0.5f))
                        .padding(12.dp),
                ) {
                    Text("T$tier", color = TextPrimary)
                    Button(
                        enabled = unlocked && !claimed,
                        onClick = {
                            scope.launch {
                                app.container.battlePassService.updateBattlePass { cur ->
                                    cur.copy(claimedTiers = cur.claimedTiers + tier)
                                }
                                app.container.rewardService.grant(
                                    RewardBundle(gold = 150L * tier, gems = 1, chestKeys = if (tier % 4 == 0) 1 else 0),
                                )
                            }
                        },
                    ) {
                        Text(
                            when {
                                claimed -> "Claimed"
                                unlocked -> "Claim"
                                else -> "Locked"
                            },
                        )
                    }
                }
            }
        }
        Button(onClick = { navController.popBackStack() }) { Text("Back") }
    }
}
