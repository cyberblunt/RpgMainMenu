package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.BuildPreviewState
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.PanelBlue
import com.zerotoler.rpgmenu.ui.theme.PurpleAccent
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun BuildPreview(
    preview: BuildPreviewState,
    modifier: Modifier = Modifier,
) {
    val cap = preview.battleCap
    val ring = preview.weightRing
    val drv = preview.driver
    val hasAny = cap != null || ring != null || drv != null

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PanelBlue.copy(alpha = 0.9f), Color(0xFF0A1628)),
                ),
            )
            .border(1.dp, CyanGlow.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.matchParentSize()) {
                val c = Offset(size.width * 0.5f, size.height * 0.45f)
                drawCircle(
                    color = CyanGlow.copy(alpha = 0.12f),
                    radius = size.minDimension * 0.35f,
                    center = c,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!hasAny) {
                    EmptyBuildState(Modifier.fillMaxWidth())
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LayerPill("Cap", cap, Modifier.weight(1f), YellowAccent)
                        LayerPill("Ring", ring, Modifier.weight(1f), PurpleAccent)
                        LayerPill("Driver", drv, Modifier.weight(1f), CyanGlow)
                    }
                    BuildTagChips(
                        preview = preview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerPill(
    label: String,
    part: PartBase?,
    modifier: Modifier = Modifier,
    accent: Color,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1F36))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            part?.name?.take(10) ?: "--",
            style = MaterialTheme.typography.labelSmall,
            color = if (part != null) TextPrimary else TextMuted,
            maxLines = 1,
        )
    }
}
