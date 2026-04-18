package com.zerotoler.rpgmenu.ui.parts

import com.zerotoler.rpgmenu.domain.model.BuildPreviewState
import com.zerotoler.rpgmenu.domain.model.Team
import com.zerotoler.rpgmenu.domain.model.InventoryFilterState
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import com.zerotoler.rpgmenu.domain.model.VisibleInventoryItem

data class LoadoutSlotSummary(
    val slotIndex: Int,
    val equippedPartCount: Int,
)

data class PartsInventoryUiState(
    val activeTeamId: String,
    /** Short label from teams table (e.g. A–D). */
    val activeTeamDisplayName: String,
    val activeSlotIndex: Int,
    val selectedCategory: PartCategory,
    val filterState: InventoryFilterState,
    val visibleParts: List<VisibleInventoryItem>,
    val equippedBattleCap: PartBase?,
    val equippedWeightRing: PartBase?,
    val equippedDriver: PartBase?,
    val buildPreviewState: BuildPreviewState,
    val loadoutSlotsSummary: List<LoadoutSlotSummary>,
    val categoryCounts: Map<PartCategory, Int>,
    val teams: List<Team>,
    val isLoading: Boolean,
    val isEmptyInventory: Boolean,
    val errorMessage: String?,
) {
    companion object {
        fun initial(teamId: String) = PartsInventoryUiState(
            activeTeamId = teamId,
            activeTeamDisplayName = "—",
            activeSlotIndex = 0,
            selectedCategory = PartCategory.BATTLE_CAP,
            filterState = InventoryFilterState.Default,
            visibleParts = emptyList(),
            equippedBattleCap = null,
            equippedWeightRing = null,
            equippedDriver = null,
            buildPreviewState = BuildPreviewState(
                battleCap = null,
                weightRing = null,
                driver = null,
                totalStats = com.zerotoler.rpgmenu.domain.model.PartStats(null, null, null, null, null, null),
                derivedTags = emptyList(),
            ),
            loadoutSlotsSummary = (0 until 3).map { LoadoutSlotSummary(it, 0) },
            categoryCounts = PartCategory.entries.associateWith { 0 },
            teams = emptyList(),
            isLoading = true,
            isEmptyInventory = false,
            errorMessage = null,
        )
    }
}
