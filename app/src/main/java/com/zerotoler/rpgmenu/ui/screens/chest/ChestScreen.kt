package com.zerotoler.rpgmenu.ui.screens.chest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun ChestScreen(
    viewModel: ChestViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Chest Terminal", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text("Keys: ${state.keys}", color = CyanGlow)
        Button(onClick = { viewModel.openOne() }, modifier = Modifier.fillMaxWidth()) {
            Text("Open x1")
        }
        Button(onClick = { viewModel.openFive() }, modifier = Modifier.fillMaxWidth()) {
            Text("Open x5")
        }
        state.lastSummary?.let {
            Text("Last reveal: $it", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { navController.popBackStack() }) {
            Text("Close")
        }
    }

    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            },
            title = { Text("Chest") },
            text = { Text(msg) },
        )
    }
}
