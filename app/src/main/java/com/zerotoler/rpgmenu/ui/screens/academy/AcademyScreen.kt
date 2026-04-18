package com.zerotoler.rpgmenu.ui.screens.academy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.zerotoler.rpgmenu.data.seed.AcademyTasks
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun AcademyScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as RpgApplication
    val academy by app.container.missionService.observeAcademyTasks().collectAsStateWithLifecycle(
        initialValue = com.zerotoler.rpgmenu.domain.model.progress.AcademyProgress(),
    )
    val scope = rememberCoroutineScope()

    fun label(id: String) = when (id) {
        AcademyTasks.FIRST_BATTLE -> "Complete your first battle"
        AcademyTasks.FIRST_CHEST -> "Open a chest"
        AcademyTasks.EQUIP_PART -> "Equip any part"
        AcademyTasks.WIN_RANKED -> "Win a ranked match"
        else -> id
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Academy", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        academy.tasks.forEach { (id, task) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CyanGlow.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .background(PanelBlueBright.copy(alpha = 0.82f))
                    .padding(12.dp),
            ) {
                Text(label(id), color = TextPrimary)
                Text("Progress ${task.current}/1", color = CyanGlow, style = MaterialTheme.typography.labelSmall)
                Button(
                    enabled = task.current >= 1 && !task.claimed,
                    onClick = {
                        scope.launch {
                            app.container.playerProgressRepository.updateAcademy { a ->
                                val m = a.tasks.toMutableMap()
                                val cur = m[id] ?: return@updateAcademy a
                                if (cur.claimed || cur.current < 1) return@updateAcademy a
                                m[id] = cur.copy(claimed = true)
                                a.copy(tasks = m)
                            }
                            app.container.rewardService.grant(RewardBundle(gold = 400L, gems = 3, battlePassXp = 50))
                        }
                    },
                ) {
                    Text(if (task.claimed) "Claimed" else "Claim reward")
                }
            }
        }
        Button(onClick = { navController.popBackStack() }) { Text("Back") }
    }
}
