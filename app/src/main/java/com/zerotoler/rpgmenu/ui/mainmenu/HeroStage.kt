package com.zerotoler.rpgmenu.ui.mainmenu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.PanelBlue
import com.zerotoler.rpgmenu.ui.theme.PurpleAccent
import com.zerotoler.rpgmenu.ui.theme.RpgMainMenuTheme
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.YellowAccent
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HeroStage(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize(),
    ) {
        val glyphSize = maxWidth * 0.92f
        val heroMaxH = maxHeight.coerceAtLeast(120.dp)
        val heroCardWidth = (maxWidth * 0.38f).coerceIn(100.dp, 160.dp)

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height * 0.52f)
                val r = glyphSize.toPx() / 2f
                for (i in 0 until 3) {
                    drawCircle(
                        color = CyanGlow.copy(alpha = 0.08f + i * 0.04f),
                        radius = r * (0.55f + i * 0.18f),
                        center = c,
                        style = Stroke(width = 2f),
                    )
                }
                val segments = 12
                for (i in 0 until segments) {
                    val a = (i * 360f / segments) * (Math.PI / 180f)
                    val inner = r * 0.35f
                    val outer = r * 0.48f
                    val sx = c.x + cos(a).toFloat() * inner
                    val sy = c.y + sin(a).toFloat() * inner
                    val ex = c.x + cos(a).toFloat() * outer
                    val ey = c.y + sin(a).toFloat() * outer
                    drawLine(
                        color = CyanGlow.copy(alpha = 0.25f),
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round,
                    )
                }
                drawArc(
                    color = CyanGlow.copy(alpha = 0.35f),
                    startAngle = -90f,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(c.x - r * 0.55f, c.y - r * 0.55f),
                    size = Size(r * 1.1f, r * 1.1f),
                    style = Stroke(width = 3f),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-4).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .height((heroMaxH * 0.58f).coerceAtMost(200.dp).coerceAtLeast(100.dp))
                        .width(heroCardWidth)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    PanelBlue.copy(alpha = 0.85f),
                                    Color(0xFF0D1F36),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "HERO",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextMuted,
                    )
                    Text(
                        "Placeholder",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .offset(y = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingOrb(color = CyanGlow)
                Spacer(modifier = Modifier.width(6.dp))
                FloatingOrb(color = YellowAccent)
                Spacer(modifier = Modifier.width(6.dp))
                FloatingOrb(color = PurpleAccent)
            }
        }
    }
}

@Composable
private fun FloatingOrb(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 22.dp, height = 32.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.15f)),
                ),
                shape = RoundedCornerShape(50),
            ),
    )
}

@Preview(showBackground = true, heightDp = 320, widthDp = 400)
@Composable
private fun HeroStagePreview() {
    RpgMainMenuTheme {
        Box(
            modifier = Modifier
                .background(NavyBackground)
                .fillMaxWidth()
                .height(280.dp),
        ) {
            HeroStage(modifier = Modifier.fillMaxSize())
        }
    }
}
