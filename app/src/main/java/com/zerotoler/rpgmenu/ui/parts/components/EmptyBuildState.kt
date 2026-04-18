package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.theme.TextMuted

@Composable
fun EmptyBuildState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "No parts equipped",
            style = MaterialTheme.typography.titleSmall,
            color = TextMuted,
        )
        Text(
            "Pick owned parts from the grid below.",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
