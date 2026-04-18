package com.zerotoler.rpgmenu.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.RpgApplication
import com.zerotoler.rpgmenu.navigation.Routes
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun SocialHubScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.applicationContext as RpgApplication
    val friends by app.container.socialService.observeFriends()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("SOCIAL", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text(
            "Friends, guilds, and co-op — local sim until online services arrive.",
            style = MaterialTheme.typography.bodySmall,
            color = CyanGlow,
        )
        Text("Friends", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        friends.forEach { f ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CyanGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .background(PanelBlueBright.copy(alpha = 0.75f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (f.online) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color(0xFF223355),
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        if (f.online) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(f.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                    Text(f.statusLine, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text("Guild / Club — placeholder", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        Text("Invitations — placeholder", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        Text(
            "Open legacy menu",
            color = CyanGlow,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CyanGlow.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .clickable { navController.navigate(Routes.MENU) }
                .padding(12.dp),
        )
    }
}
