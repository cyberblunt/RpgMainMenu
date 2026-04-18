package com.zerotoler.rpgmenu.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.RpgApplication
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import com.zerotoler.rpgmenu.navigation.Routes
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun EventsHubScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Events", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Button(onClick = { navController.navigate(Routes.EVENT_MISSIONS) }) { Text("Event Missions") }
        Button(onClick = { navController.navigate(Routes.EVENT_BOARD) }) { Text("Event Board") }
        Button(onClick = { navController.popBackStack() }) { Text("Back") }
    }
}

@Composable
fun EventMissionsScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as RpgApplication
    val events by app.container.eventService.observeEvents().collectAsStateWithLifecycle(
        initialValue = com.zerotoler.rpgmenu.domain.model.progress.EventProgress(),
    )
    val scope = rememberCoroutineScope()

    fun target(id: String): Int = when (id) {
        "evt_win_1" -> 1
        "evt_play_3" -> 3
        else -> 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Event Missions", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        events.missions.forEach { (id, m) ->
            val t = target(id)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .background(PanelBlueBright.copy(alpha = 0.8f))
                    .padding(12.dp),
            ) {
                Text(id, color = TextPrimary)
                Text("Progress ${m.progress}/$t", color = CyanGlow)
                Button(
                    enabled = m.progress >= t && !m.claimed,
                    onClick = {
                        scope.launch {
                            app.container.eventService.updateEvents { e ->
                                val map = e.missions.toMutableMap()
                                val cur = map[id] ?: return@updateEvents e
                                if (cur.claimed || cur.progress < t) return@updateEvents e
                                map[id] = cur.copy(claimed = true)
                                e.copy(missions = map)
                            }
                            app.container.rewardService.grant(RewardBundle(gold = 250L, gems = 2, battlePassXp = 30))
                        }
                    },
                ) {
                    Text(if (m.claimed) "Claimed" else "Claim")
                }
            }
        }
        Button(onClick = { navController.popBackStack() }) { Text("Back") }
    }
}

@Composable
fun EventBoardScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as RpgApplication
    val events by app.container.eventService.observeEvents().collectAsStateWithLifecycle(
        initialValue = com.zerotoler.rpgmenu.domain.model.progress.EventProgress(),
    )
    val scope = rememberCoroutineScope()
    val cells = (0 until 9).toList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(12.dp),
    ) {
        Text("Event Board", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(cells) { idx ->
                val claimed = idx in events.boardCellsClaimed
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, CyanGlow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .background(PanelBlueBright.copy(alpha = 0.75f))
                        .padding(12.dp),
                ) {
                    Text("Cell $idx", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                    Button(
                        enabled = !claimed,
                        onClick = {
                            scope.launch {
                                app.container.eventService.updateEvents { e ->
                                    if (idx in e.boardCellsClaimed) return@updateEvents e
                                    e.copy(boardCellsClaimed = e.boardCellsClaimed + idx)
                                }
                                app.container.rewardService.grant(RewardBundle(gold = 120L, battlePassXp = 15))
                            }
                        },
                    ) {
                        Text(if (claimed) "Done" else "Claim")
                    }
                }
            }
        }
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 8.dp)) { Text("Back") }
    }
}
