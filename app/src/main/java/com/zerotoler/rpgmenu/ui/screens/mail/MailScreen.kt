package com.zerotoler.rpgmenu.ui.screens.mail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun MailScreen(
    viewModel: MailViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val mail by viewModel.mailState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(12.dp),
    ) {
        Text("Mail / Notices", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(mail.messages, key = { it.id }) { m ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CyanGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .background(PanelBlueBright.copy(alpha = if (m.read) 0.55f else 0.88f))
                        .padding(12.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(m.title, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        if (!m.read) {
                            Text("NEW", color = YellowAccent, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(m.body, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Button(onClick = { viewModel.markRead(m.id) }) { Text("Read") }
                        if (!m.claimed && (m.goldReward > 0 || m.gemReward > 0)) {
                            Button(onClick = { viewModel.claim(m.id) }) { Text("Claim") }
                        }
                    }
                }
            }
        }
        Button(onClick = { navController.popBackStack() }) { Text("Close") }
    }
}
