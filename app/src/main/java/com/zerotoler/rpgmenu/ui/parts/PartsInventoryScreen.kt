package com.zerotoler.rpgmenu.ui.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zerotoler.rpgmenu.ui.parts.components.BuildPreview
import com.zerotoler.rpgmenu.ui.parts.components.BuildStatPanel
import com.zerotoler.rpgmenu.ui.parts.components.InventoryToolbar
import com.zerotoler.rpgmenu.ui.parts.components.LoadoutSlotChips
import com.zerotoler.rpgmenu.ui.parts.components.PartCategoryTabs
import com.zerotoler.rpgmenu.ui.parts.components.PartGrid
import com.zerotoler.rpgmenu.ui.parts.components.TeamSwitcherRow
import com.zerotoler.rpgmenu.ui.parts.components.TopInventoryHeader
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun PartsInventoryScreen(
    viewModel: PartsInventoryViewModel,
    onOpenPartDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NavyBackground, NavyBackgroundEnd),
                ),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    TopInventoryHeader(
                        teamDisplayName = state.activeTeamDisplayName,
                        modifier = Modifier.wrapContentWidth(),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TeamSwitcherRow(
                        teams = state.teams,
                        activeTeamId = state.activeTeamId,
                        onSelectTeam = viewModel::selectTeam,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Slots",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    LoadoutSlotChips(
                        summaries = state.loadoutSlotsSummary,
                        activeSlotIndex = state.activeSlotIndex,
                        onSelectSlot = viewModel::selectSlot,
                    )
                }
                BuildPreview(
                    preview = state.buildPreviewState,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                BuildStatPanel(
                    stats = state.buildPreviewState.totalStats,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                PartCategoryTabs(
                    selected = state.selectedCategory,
                    counts = state.categoryCounts,
                    onSelect = viewModel::selectCategory,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                InventoryToolbar(
                    filter = state.filterState,
                    onSearchChange = viewModel::updateSearchQuery,
                    onToggleOwnedOnly = viewModel::toggleOwnedOnly,
                    onRarityChange = viewModel::setRarityFilter,
                    onCombatChange = viewModel::setCombatTypeFilter,
                    onSpinChange = viewModel::setSpinDirectionFilter,
                    onSortChange = viewModel::setSortMode,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            PartGrid(
                visibleItems = state.visibleParts,
                onEquipPart = viewModel::equipPart,
                onOpenPartDetail = onOpenPartDetail,
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth(),
            )
        }
    }

    state.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("OK")
                }
            },
            title = { Text("Notice") },
            text = { Text(msg) },
        )
    }
}
