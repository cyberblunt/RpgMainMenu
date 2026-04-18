package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import kotlin.math.PI

/**
 * Fills the parent [modifier] and draws the arena using the parent’s size (via [BoxWithConstraints]).
 * Scale is derived so the arena stays large (~48% of min side, min ~140dp radius) even if layout is tight.
 */
@Composable
fun BattleArena(
    snapshot: BattleRenderSnapshot,
    onAimStart: () -> Unit,
    onAimMove: (Float, Float) -> Unit,
    onAimEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val minArenaRadiusPx = with(density) { 140.dp.toPx() }

    BoxWithConstraints(modifier) {
        val w = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val h = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val minPx = kotlin.math.min(w, h)
        val arenaRadiusPx = kotlin.math.max(minPx * 0.48f, minArenaRadiusPx)
        val scale = arenaRadiusPx / snapshot.arenaRadius.coerceAtLeast(0.25f)
        val cx = w * 0.5f
        val cy = h * 0.5f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(snapshot.phase, cx, cy) {
                    if (snapshot.phase != BattlePhase.LAUNCH) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        onAimStart()
                        onAimMove(down.position.x - cx, down.position.y - cy)
                        while (true) {
                            // Use the default pointer event pass available in this Compose version.
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            onAimMove(change.position.x - cx, change.position.y - cy)
                            change.consume()
                        }
                        onAimEnd()
                    }
                },
        ) {
            val shake = Offset(snapshot.screenShakeX, snapshot.screenShakeY)
            translate(shake.x, shake.y) {
                val arenaR = scale * snapshot.arenaRadius
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0D47A1).copy(alpha = 0.45f),
                            Color(0xFF01579B).copy(alpha = 0.85f),
                        ),
                        center = Offset(cx, cy),
                        radius = arenaR * 1.05f,
                    ),
                    radius = arenaR,
                    center = Offset(cx, cy),
                )
                drawCircle(
                    color = CyanGlow.copy(alpha = 0.35f),
                    radius = arenaR,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
                )

                // Stabilization levels (visual markers): center dot + inner/outer grooves (red).
                val innerR = arenaR * 0.37f
                val outerR = arenaR * 0.74f
                val grooveStroke = (arenaR * 0.04f).coerceIn(6f, 18f)
                val grooveColor = Color(0xFFFF3232).copy(alpha = 0.15f)
                drawCircle(
                    color = grooveColor,
                    radius = outerR,
                    center = Offset(cx, cy),
                    style = Stroke(width = grooveStroke),
                )
                drawCircle(
                    color = grooveColor,
                    radius = innerR,
                    center = Offset(cx, cy),
                    style = Stroke(width = grooveStroke),
                )
                drawCircle(
                    color = Color(0xFFFF3232).copy(alpha = 0.20f),
                    radius = (arenaR * 0.04f).coerceIn(10f, 18f),
                    center = Offset(cx, cy),
                )

                if (snapshot.showLaunchers) {
                    val lp = Offset(cx + snapshot.launcherPlayerX * scale, cy + snapshot.launcherPlayerY * scale)
                    val le = Offset(cx + snapshot.launcherEnemyX * scale, cy + snapshot.launcherEnemyY * scale)

                    val pr = snapshot.playerRadius * scale
                    val er = snapshot.enemyRadius * scale
                    val lpW = (pr * 3.5f).coerceAtLeast(40f)
                    val lpH = (pr * 1.25f).coerceAtLeast(14f)
                    val leW = (er * 3.5f).coerceAtLeast(40f)
                    val leH = (er * 1.25f).coerceAtLeast(14f)
                    val prCorner = (lpH * 0.35f).coerceIn(4f, 10f)
                    val erCorner = (leH * 0.35f).coerceIn(4f, 10f)
                    drawRoundRect(
                        color = Color(0xFF37474F).copy(alpha = 0.75f),
                        topLeft = Offset(lp.x - lpW / 2f, lp.y - lpH / 2f),
                        size = androidx.compose.ui.geometry.Size(lpW, lpH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(prCorner, prCorner),
                    )
                    drawRoundRect(
                        color = Color(0xFF37474F).copy(alpha = 0.75f),
                        topLeft = Offset(le.x - leW / 2f, le.y - leH / 2f),
                        size = androidx.compose.ui.geometry.Size(leW, leH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(erCorner, erCorner),
                    )
                }

                if (snapshot.phase == BattlePhase.LAUNCH && snapshot.aimActive) {
                    val po = Offset(cx + snapshot.playerX * scale, cy + snapshot.playerY * scale)
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

                fun drawTop(px: Float, py: Float, angleRad: Float, radius: Float, color: Color, flash: Float) {
                    val o = Offset(cx + px * scale, cy + py * scale)
                    val r = (radius * scale).coerceAtLeast(4f)
                    drawCircle(
                        color = color.copy(alpha = 0.35f + flash * 0.35f),
                        // Keep aura tight enough so bigger tops don't clip against the arena wall.
                        radius = r * 1.08f,
                        center = o,
                    )
                    rotate(angleRad * (180f / PI.toFloat()), o) {
                        drawCircle(color = color, radius = r, center = o)
                        val strokeW = (2.4f + r * 0.14f).coerceIn(2.4f, 8f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.85f),
                            start = o,
                            end = Offset(o.x + r * 0.78f, o.y),
                            strokeWidth = strokeW,
                        )
                    }
                }

                val pColor = Color(0xFF00E5FF)
                val eColor = Color(0xFFE040FB)
                drawTop(
                    snapshot.enemyX, snapshot.enemyY, snapshot.enemyAngle,
                    snapshot.enemyRadius, eColor, snapshot.collisionFlashB,
                )
                drawTop(
                    snapshot.playerX, snapshot.playerY, snapshot.playerAngle,
                    snapshot.playerRadius, pColor, snapshot.collisionFlashA,
                )

                for (e in snapshot.effects) {
                    val eo = Offset(cx + e.x * scale, cy + e.y * scale)
                    drawCircle(
                        color = Color.White.copy(alpha = e.intensity * 0.5f * e.age),
                        radius = 12f * e.intensity,
                        center = eo,
                    )
                }

                for (im in snapshot.impacts) {
                    val io = Offset(cx + im.x * scale, cy + im.y * scale)
                    drawCircle(color = Color.Yellow.copy(alpha = 0.35f * im.age), radius = 8f, center = io)
                }
            }
        }
    }
}
