package com.zerotoler.rpgmenu.ui.battleprep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.battlesession.TeamTopConfig
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.RedBadge
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent
import com.zerotoler.rpgmenu.ui.theme.YellowAccentDark

@Composable
fun PreBattleSelectionScreen(
    viewModel: PreBattleSelectionViewModel,
    onNavigateToBattle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var autoBattle by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd))),
    ) {
        SciFiGridBackground(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "← Back",
                    color = CyanGlow,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBack)
                        .padding(8.dp),
                )
                Text(
                    state.roundDisplay,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoundPip(1, state.completedRoundResults.getOrNull(0))
                RoundPip(2, state.completedRoundResults.getOrNull(1))
                RoundPip(3, state.completedRoundResults.getOrNull(2))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.28f),
                ) {
                    Text("Current scene", color = TextMuted, fontSize = 11.sp)
                    Text(state.arenaLabel, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(state.arenaSubtext, color = CyanGlow.copy(alpha = 0.7f), fontSize = 10.sp)
                }
                TypeMatchupTriangle(modifier = Modifier.weight(0.32f))
                Column(
                    modifier = Modifier.weight(0.38f),
                ) {
                    Text("Enemy intel", color = RedBadge, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    EnemyIntelPanel(roster = state.opponentRoster)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Enemy dispatch", color = RedBadge, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            CenterEnemyCard(
                opponentName = state.currentOpponent?.name ?: "—",
                archetype = state.currentOpponent?.archetype,
            )
            if (!state.teamFullyConfigured) {
                Spacer(modifier = Modifier.height(10.dp))
                IncompleteTeamBanner()
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "Select your top",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.playerTops.forEach { top ->
                    val used = top.slotIndex in state.usedSlots
                    val selected = top.slotIndex == state.selectedSlot
                    val selectable = top.isComplete && !used && state.teamFullyConfigured
                    PlayerTopCard(
                        top = top,
                        selected = selected,
                        used = used,
                        selectable = selectable,
                        onClick = { if (selectable) viewModel.selectPlayerSlot(top.slotIndex) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (state.canStartBattle) YellowAccent else Color.Gray.copy(alpha = 0.35f),
                                    YellowAccentDark.copy(alpha = if (state.canStartBattle) 1f else 0.4f),
                                ),
                            ),
                        )
                        .clickable(enabled = state.canStartBattle) {
                            viewModel.startBattle(onNavigateToBattle)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "START BATTLE",
                        color = NavyBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto launch", color = TextMuted, fontSize = 11.sp)
                    Switch(
                        checked = autoBattle,
                        onCheckedChange = { autoBattle = it },
                        enabled = false,
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SciFiGridBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val step = 24.dp.toPx()
        val c = Color(0xFF00EAFF).copy(alpha = 0.06f)
        var x = 0f
        while (x < size.width) {
            drawLine(c, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(c, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}

@Composable
private fun RoundPip(index: Int, result: Boolean?) {
    val bg = when (result) {
        true -> Color(0xFF2E7D32).copy(alpha = 0.5f)
        false -> Color(0xFFC62828).copy(alpha = 0.45f)
        null -> PanelBlueBright.copy(alpha = 0.4f)
    }
    val label = when (result) {
        true -> "W"
        false -> "L"
        null -> "$index"
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, CyanGlow.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TypeMatchupTriangle(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Type chart", color = TextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(modifier = Modifier.size(100.dp)) {
            val r = size.minDimension * 0.38f
            val c = Offset(size.width / 2f, size.height * 0.22f)
            val bl = Offset(size.width * 0.18f, size.height * 0.78f)
            val br = Offset(size.width * 0.82f, size.height * 0.78f)
            val path = Path().apply {
                moveTo(c.x, c.y)
                lineTo(bl.x, bl.y)
                lineTo(br.x, br.y)
                close()
            }
            drawPath(path, color = Color.White.copy(alpha = 0.08f))
            drawPath(path, color = CyanGlow.copy(alpha = 0.35f), style = Stroke(width = 2f))
            drawCircle(Color(0xFFE53935), r * 0.35f, c)
            drawCircle(Color(0xFF43A047), r * 0.35f, bl)
            drawCircle(Color(0xFF1E88E5), r * 0.35f, br)
        }
        Text("Atk > Stm > Def > Atk", color = TextMuted, fontSize = 9.sp)
    }
}

@Composable
private fun EnemyIntelPanel(roster: List<com.zerotoler.rpgmenu.domain.model.battlesession.OpponentBattleTop>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, RedBadge.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .background(PanelBlueBright.copy(alpha = 0.45f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        roster.forEach { o ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(typeColor(o.archetype).copy(alpha = 0.35f)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(o.name, color = TextPrimary, fontSize = 11.sp, maxLines = 1)
                    Text(o.intelLabel, color = TextMuted, fontSize = 9.sp)
                }
            }
        }
        Text("Scout: inactive", color = TextMuted.copy(alpha = 0.6f), fontSize = 9.sp)
    }
}

@Composable
private fun CenterEnemyCard(opponentName: String, archetype: CombatType?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF6A1B9A).copy(alpha = 0.85f), Color(0xFFE91E63).copy(alpha = 0.55f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("?", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Text(opponentName, color = TextPrimary, fontWeight = FontWeight.Medium)
            archetype?.let {
                Text(it.name, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun IncompleteTeamBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RedBadge.copy(alpha = 0.2f))
            .border(1.dp, RedBadge.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(10.dp),
    ) {
        Text(
            "Configure all three loadout slots in PARTS (Battle Cap, Weight Ring, Driver) before battling.",
            color = TextPrimary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PlayerTopCard(
    top: TeamTopConfig,
    selected: Boolean,
    used: Boolean,
    selectable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = when {
        selected -> Brush.linearGradient(listOf(CyanGlow, Color(0xFF00EAFF)))
        else -> Brush.linearGradient(listOf(CyanGlow.copy(alpha = 0.15f), CyanGlow.copy(alpha = 0.08f)))
    }
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(PanelBlueBright, NavyBackgroundEnd)))
            .border(width = if (selected) 3.dp else 1.dp, brush = border, shape = RoundedCornerShape(12.dp))
            .clickable(enabled = selectable, onClick = onClick),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(typeColor(top.dominantCombatType).copy(alpha = 0.6f)),
                )
                Text(
                    String.format("%.1f", top.powerScore),
                    color = CyanGlow,
                    fontSize = 10.sp,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyBackground.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("◆", color = TextPrimary.copy(alpha = if (top.isComplete) 1f else 0.35f), fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                top.displayName,
                color = if (top.isComplete) TextPrimary else TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
            )
            if (!top.isComplete) {
                Text("Incomplete", color = RedBadge, fontSize = 9.sp)
            }
        }
        if (used) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("USED", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun typeColor(t: CombatType): Color = when (t) {
    CombatType.ATTACK -> Color(0xFFE53935)
    CombatType.DEFENSE -> Color(0xFF1E88E5)
    CombatType.STAMINA -> Color(0xFF43A047)
    CombatType.BALANCE -> Color(0xFF8E24AA)
    CombatType.UNKNOWN -> Color.Gray
}
