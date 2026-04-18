package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.ui.theme.CyanGlow

@Composable
fun LaunchDirectionOverlay(
    snapshot: BattleRenderSnapshot,
    centerX: Float,
    centerY: Float,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    if (snapshot.phase != BattlePhase.LAUNCH || !snapshot.aimActive) return
    Canvas(modifier.fillMaxSize()) {
        val po = Offset(centerX + snapshot.playerX * scale, centerY + snapshot.playerY * scale)
        val pr = snapshot.playerRadius * scale
        val arrowLen = (pr * 5.5f).coerceIn(60f, 240f)
        val ex = po.x + snapshot.aimDirX * arrowLen
        val ey = po.y + snapshot.aimDirY * arrowLen
        val path = Path().apply {
            moveTo(po.x, po.y)
            lineTo(ex, ey)
        }
        drawPath(
            path = path,
            color = CyanGlow.copy(alpha = 0.95f),
            style = Stroke(width = 5f),
        )
        val dotR = (pr * 0.18f).coerceIn(5f, 18f)
        drawCircle(Color.White.copy(alpha = 0.22f), dotR, po)
    }
}
