package com.zerotoler.rpgmenu.ui.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.RpgApplication
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun PartDetailScreen(
    partId: String,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.applicationContext as RpgApplication
    var part by remember { mutableStateOf<PartBase?>(null) }
    LaunchedEffect(partId) {
        part = app.container.catalogRepository.getPartById(partId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        Text("Part Intel", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        val p = part
        if (p == null) {
            Text("Loading…", color = TextMuted)
        } else {
            Text(p.name, color = CyanGlow, style = MaterialTheme.typography.titleLarge)
            Text("${p.category} · R${p.rarity}", color = TextMuted)
            Text("ATK ${p.stats.attack} DEF ${p.stats.defense} HP ${p.stats.health}", color = TextPrimary)
            Text("STA ${p.stats.stamina} · WT ${p.stats.weightGrams}", color = TextMuted)
            p.tags.forEach { Text("#$it", color = CyanGlow, style = MaterialTheme.typography.labelSmall) }
        }
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Back")
        }
    }
}
