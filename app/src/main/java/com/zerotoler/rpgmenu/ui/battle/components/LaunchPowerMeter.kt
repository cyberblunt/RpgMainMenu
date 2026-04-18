package com.zerotoler.rpgmenu.ui.battle.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.TextMuted

@Composable
fun LaunchPowerMeter(
    phase: BattlePhase,
    oscillator01: Float,
    modifier: Modifier = Modifier,
) {
    if (phase != BattlePhase.LAUNCH) return
    Canvas(
        modifier = modifier
            .width(22.dp)
            .fillMaxSize(),
    ) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(
                    TextMuted.copy(alpha = 0.35f),
                    CyanGlow.copy(alpha = 0.55f),
                    TextMuted.copy(alpha = 0.35f),
                ),
            ),
            cornerRadius = CornerRadius(6f, 6f),
            size = Size(w, h),
        )
        val center = h * 0.5f
        val y = oscillator01.coerceIn(0f, 1f) * h
        val strength = 1f - kotlin.math.abs(2f * oscillator01 - 1f)
        val indH = 10f + strength * 8f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(2f, (y - indH * 0.5f).coerceIn(0f, h - indH)),
            size = Size(w - 4f, indH),
            cornerRadius = CornerRadius(4f, 4f),
        )
        drawLine(
            color = CyanGlow.copy(alpha = 0.9f),
            start = Offset(0f, center),
            end = Offset(w, center),
            strokeWidth = 2f,
        )
    }
}
