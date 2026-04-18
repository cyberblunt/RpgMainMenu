package com.zerotoler.rpgmenu.ui.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.theme.ChatStripBg
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.RpgMainMenuTheme
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun ChatTicker(
    message: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ChatStripBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(NavyBackground.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "C",
                style = MaterialTheme.typography.titleMedium,
                color = CyanGlow,
            )
        }
        Text(
            text = message,
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
                .basicMarquee(),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun ChatTickerPreview() {
    RpgMainMenuTheme {
        Box(modifier = Modifier.background(NavyBackground)) {
            ChatTicker(
                message = "player_one is unstoppable in the Survival Mode Novice Arena, and the crowd goes wild!",
                onClick = {},
            )
        }
    }
}
