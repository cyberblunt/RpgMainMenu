package com.zerotoler.rpgmenu.ui.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun PartEnhanceScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        Text("Enhancement / Potential", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text(
            "Use the Parts tab to view shards and levels per card. Full upgrade loops hook here later.",
            style = MaterialTheme.typography.bodyMedium,
            color = CyanGlow,
        )
        Button(onClick = { navController.popBackStack() }) { Text("Back") }
    }
}
