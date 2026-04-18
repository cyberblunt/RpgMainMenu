package com.zerotoler.rpgmenu.ui.boss

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.data.content.BossDefinition
import com.zerotoler.rpgmenu.navigation.Routes
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun BossHubScreen(
    viewModel: BossHubViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val bosses by viewModel.bosses.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .padding(16.dp),
    ) {
        Text("Boss battles", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            "Offline bosses — same arena engine, tuned stats and rewards.",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            items(bosses, key = { it.id }) { b ->
                BossCard(boss = b, onEnter = {
                    navController.navigate(Routes.preBattle("boss", b.id))
                })
            }
        }
    }
}

@Composable
private fun BossCard(boss: BossDefinition, onEnter: () -> Unit) {
    val stats = boss.toStats()
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A1F33),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(boss.displayName, color = YellowAccent, style = MaterialTheme.typography.titleSmall)
            Text(boss.title, color = CyanGlow, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Power ${boss.powerLevel} · HP ~${stats.maxHp.toInt()} · ATK ~${stats.attack.toInt()}",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onEnter,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B2B)),
            ) {
                Text("Open battle placeholder")
            }
        }
    }
}
