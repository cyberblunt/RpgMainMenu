package com.zerotoler.rpgmenu.ui.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zerotoler.rpgmenu.ui.parts.components.BuildStatPanel
import com.zerotoler.rpgmenu.ui.parts.components.ExplodedBuildPreview
import com.zerotoler.rpgmenu.ui.parts.components.InventoryToolbar
import com.zerotoler.rpgmenu.ui.parts.components.PartCard
import com.zerotoler.rpgmenu.ui.parts.components.PartCategoryTabs
import com.zerotoler.rpgmenu.ui.parts.components.PartsProfileCurrencyBar
import com.zerotoler.rpgmenu.ui.parts.components.PartsTeamSlotsHeader
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd

@Composable
fun PartsInventoryScreen(
    viewModel: PartsInventoryViewModel,
    onOpenPartDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rows = state.visibleParts.chunked(4)

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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PartsProfileCurrencyBar(
                    displayName = state.playerDisplayName,
                    level = state.playerLevel,
                    gold = state.gold,
                    gems = state.gems,
                )
                PartsTeamSlotsHeader(
                    teams = state.teams,
                    activeTeamId = state.activeTeamId,
                    onSelectTeam = viewModel::selectTeam,
                    loadoutSlotsSummary = state.loadoutSlotsSummary,
                    activeSlotIndex = state.activeSlotIndex,
                    onSelectSlot = viewModel::selectSlot,
                    onAutoConfig = viewModel::runAutoConfig,
                )
                ExplodedBuildPreview(
                    preview = state.buildPreviewState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                )
                BuildStatPanel(
                    stats = state.buildPreviewState.totalStats,
                    derivedTags = state.buildPreviewState.derivedTags,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        PartCategoryTabs(
                            selected = state.selectedCategory,
                            counts = state.ownedCategoryCounts,
                            onSelect = viewModel::selectCategory,
                            modifier = Modifier.weight(1f),
                        )
                        CatalogIconChip("≡", onClick = { /* reserved: filter sheet */ })
                        CatalogIconChip("⇄", onClick = { /* reserved: compare */ })
                    }
                }
                item {
                    InventoryToolbar(
                        filter = state.filterState,
                        onSearchChange = viewModel::updateSearchQuery,
                        onToggleOwnedOnly = viewModel::toggleOwnedOnly,
                        onRarityChange = viewModel::setRarityFilter,
                        onCombatChange = viewModel::setCombatTypeFilter,
                        onSpinChange = viewModel::setSpinDirectionFilter,
                        onSortChange = viewModel::setSortMode,
                    )
                }
                items(
                    count = rows.size,
                    key = { index -> rows[index].joinToString("|") { it.part.id } },
                ) { index ->
                    val row = rows[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        row.forEach { item ->
                            PartCard(
                                item = item,
                                onEquip = { viewModel.equipPart(item.part.id) },
                                onOpenDetail = onOpenPartDetail?.let { cb -> { cb(item.part.id) } },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(4 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
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

@Composable
private fun CatalogIconChip(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(34.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E4A8C))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color(0xFFB8D4FF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
