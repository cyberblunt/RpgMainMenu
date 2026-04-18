package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.BuildPreviewState
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun BuildTagChips(
    preview: BuildPreviewState,
    modifier: Modifier = Modifier,
) {
    val tags = preview.derivedTags.ifEmpty {
        listOf("Passive slots", "Tags expand with future systems")
    }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.take(8).forEach { tag ->
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelBlueBright.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
