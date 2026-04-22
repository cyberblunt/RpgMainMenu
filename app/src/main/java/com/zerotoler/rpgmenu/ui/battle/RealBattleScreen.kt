package com.zerotoler.rpgmenu.ui.battle

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.domain.model.battle.VisualParticle
import com.zerotoler.rpgmenu.ui.battle.components.LaunchPowerMeter
import com.zerotoler.rpgmenu.ui.battle.components.ResultOverlay
import com.zerotoler.rpgmenu.ui.battle.components.SuperAbilityButton
import com.zerotoler.rpgmenu.ui.battle.surface.BattleSurface
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraintsScope

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

    val snap = state.render
    val enemyHpPct = ratio(snap.enemyHp, snap.enemyHpMax)
    val enemyRpmPct = ratio(snap.enemyRpm, snap.enemyRpmMax)
    val playerHpPct = ratio(snap.playerHp, snap.playerHpMax)
    val playerRpmPct = ratio(snap.playerRpm, snap.playerRpmMax)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1B2A)),
    ) {
        if (snap.phase != BattlePhase.RESULT) {
            BattleSurface(
                engine = state.engine,
                paused = paused,
                onSnapshot = viewModel::onSnapshot,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(Modifier.fillMaxSize()) {
            EnemyBattleHudRow(
                displayName = state.enemyDisplayName,
                hpPct = enemyHpPct,
                rpmPct = enemyRpmPct,
                timerSec = snap.timerSec,
            )
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Column(
                    Modifier
                        .padding(start = 16.dp)
                        .fillMaxHeight(),
                ) {
                    BattleStatColumn(
                        attack = snap.enemyAttack,
                        defense = snap.enemyDefense,
                        stamina = snap.enemyStam,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BattleStatColumn(
                        attack = snap.playerAttack,
                        defense = snap.playerDefense,
                        stamina = snap.playerStam,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            PlayerBattleHudRow(
                displayName = state.playerDisplayName,
                hpPct = playerHpPct,
                rpmPct = playerRpmPct,
                autoBattle = state.autoBattle,
                onAutoClick = viewModel::toggleAutoBattle,
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            BattleSparkOverlay(
                snap = snap,
                particles = state.visualParticles,
            )
            if (snap.phase == BattlePhase.LAUNCH) {
                LaunchPowerMeter(
                    phase = snap.phase,
                    oscillator01 = snap.powerOscillator01,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp, top = 48.dp)
                        .width(28.dp)
                        .height(200.dp),
                )
            }
            SuperAbilityButton(
                phase = snap.phase,
                meter = snap.superMeter,
                abilityActive = snap.superAbilityActive,
                remainingSec = snap.superAbilityRemainingSec,
                onClick = viewModel::onSuperAbility,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 120.dp),
            )
        }

        ResultOverlay(
            phase = snap.phase,
            outcome = snap.outcome,
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

@Composable
private fun EnemyBattleHudRow(
    displayName: String,
    hpPct: Float,
    rpmPct: Float,
    timerSec: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(3.dp, Color.Red, CircleShape)
                .background(Color.DarkGray, CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            BattleHudBarRow(label = "RPM", fraction = rpmPct, barColor = Color(0xFFFFC107), barHeight = 10.dp)
            Spacer(modifier = Modifier.height(6.dp))
            BattleHudBarRow(label = "HP", fraction = hpPct, barColor = Color(0xFF4CAF50), barHeight = 14.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("▼", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(displayName, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        Text(
            formatBattleTimer(timerSec),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun PlayerBattleHudRow(
    displayName: String,
    hpPct: Float,
    rpmPct: Float,
    autoBattle: Boolean,
    onAutoClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(3.dp, Color(0xFF00E5FF), CircleShape)
                .background(Color.DarkGray, CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("▲", color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    displayName,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onAutoClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = CircleShape,
                ) {
                    Text(
                        if (autoBattle) "ON" else "AUTO",
                        color = if (autoBattle) Color(0xFF00E5FF) else Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            BattleHudBarRow(label = "HP", fraction = hpPct, barColor = Color(0xFF4CAF50), barHeight = 14.dp)
            Spacer(modifier = Modifier.height(6.dp))
            BattleHudBarRow(label = "RPM", fraction = rpmPct, barColor = Color(0xFFFFC107), barHeight = 10.dp)
        }
    }
}

@Composable
private fun BattleHudBarRow(
    label: String,
    fraction: Float,
    barColor: Color,
    barHeight: androidx.compose.ui.unit.Dp,
) {
    val f = fraction.coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(barHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(f)
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun BattleStatColumn(
    attack: Int,
    defense: Int,
    stamina: Int,
) {
    BattleStatItem(value = attack.toString(), swatch = Color.Red)
    BattleStatItem(value = defense.toString(), swatch = Color.Cyan)
    BattleStatItem(value = stamina.toString(), swatch = Color.Magenta)
}

@Composable
private fun BattleStatItem(value: String, swatch: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(swatch),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.BattleSparkOverlay(
    snap: BattleRenderSnapshot,
    particles: List<VisualParticle>,
) {
    if (particles.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val minSide = kotlin.math.min(size.width, size.height)
        val arenaRadiusPx = minSide * 0.45f * 1.1f * 3.5f / 3f * 0.95f
        val scale = arenaRadiusPx / snap.arenaRadius.coerceAtLeast(0.25f)
        val cx = size.width * 0.5f + snap.screenShakeX
        val cy = size.height * 0.5f + snap.screenShakeY
        for (p in particles) {
            val px = cx + p.x * scale
            val py = cy + p.y * scale
            val life = p.life.coerceIn(0f, 1f)
            val baseA = (p.colorArgb ushr 24) and 0xff
            if (baseA == 0) {
                val alpha = (255 * life).toInt().coerceIn(0, 255)
                val rgb = p.colorArgb and 0x00FFFFFF
                drawCircle(
                    color = androidx.compose.ui.graphics.Color((alpha shl 24) or rgb),
                    radius = (p.size * scale).coerceIn(1.5f, 14f),
                    center = Offset(px, py),
                )
            } else {
                val alpha = (baseA * life).toInt().coerceIn(0, 255)
                val rgb = p.colorArgb and 0x00FFFFFF
                drawCircle(
                    color = androidx.compose.ui.graphics.Color((alpha shl 24) or rgb),
                    radius = (p.size * scale).coerceIn(1.5f, 14f),
                    center = Offset(px, py),
                )
            }
        }
    }
}

private fun ratio(part: Int, max: Int): Float {
    if (max <= 0) return 0f
    return (part / max.toFloat()).coerceIn(0f, 1f)
}

private fun formatBattleTimer(timerSec: Int): String {
    val t = timerSec.coerceAtLeast(0)
    val m = t / 60
    val s = t % 60
    return "%02d:%02d".format(m, s)
}
