package com.zerotoler.rpgmenu.ui.battle

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.ui.battle.components.BattleTimerTopBar
import com.zerotoler.rpgmenu.ui.battle.components.LaunchPowerMeter
import com.zerotoler.rpgmenu.ui.battle.components.ParticipantHud
import com.zerotoler.rpgmenu.ui.battle.components.ResultOverlay
import com.zerotoler.rpgmenu.ui.battle.components.SuperAbilityButton
import com.zerotoler.rpgmenu.ui.battle.surface.BattleSurface
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun RealBattleScreen(
    viewModel: RealBattleViewModel,
    onContinueToSelection: () -> Unit,
    onSessionComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var paused by remember { mutableStateOf(false) }

    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_PAUSE -> {
                    paused = true
                    viewModel.setPaused(true)
                }
                Lifecycle.Event.ON_RESUME -> {
                    paused = false
                    viewModel.setPaused(false)
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }

    BackHandler {
        viewModel.cancelBattle()
        onContinueToSelection()
    }

    val err = state.fatalError
    if (err != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Battle unavailable", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(err, color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Button(onClick = onContinueToSelection) {
                    Text("Back")
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd))),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .fillMaxWidth(),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                ParticipantHud(
                    name = state.enemyDisplayName,
                    archetype = state.render.enemyArchetype,
                    hp = state.render.enemyHp,
                    hpMax = state.render.enemyHpMax,
                    st = state.render.enemyStam,
                    stMax = state.render.enemyStamMax,
                    alignEnd = false,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                )
                BattleTimerTopBar(phase = state.render.phase, timerSec = state.render.timerSec)
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (state.render.phase != BattlePhase.RESULT) {
                    BattleSurface(
                        engine = state.engine,
                        paused = paused,
                        onSnapshot = viewModel::onSnapshot,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (state.render.phase == BattlePhase.LAUNCH) {
                    LaunchPowerMeter(
                        phase = state.render.phase,
                        oscillator01 = state.render.powerOscillator01,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp, top = 48.dp)
                            .width(28.dp)
                            .height(200.dp),
                    )
                }
                SuperAbilityButton(
                    phase = state.render.phase,
                    meter = state.render.superMeter,
                    abilityActive = state.render.superAbilityActive,
                    remainingSec = state.render.superAbilityRemainingSec,
                    onClick = viewModel::onSuperAbility,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp),
                )
            }

            ParticipantHud(
                name = state.playerDisplayName,
                archetype = state.render.playerArchetype,
                hp = state.render.playerHp,
                hpMax = state.render.playerHpMax,
                st = state.render.playerStam,
                stMax = state.render.playerStamMax,
                alignEnd = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        }

        ResultOverlay(
            phase = state.render.phase,
            outcome = state.render.outcome,
            busy = state.isBusy,
            onContinue = {
                viewModel.confirmResultNavigate(
                    onContinueSelection = onContinueToSelection,
                    onSessionComplete = onSessionComplete,
                )
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
