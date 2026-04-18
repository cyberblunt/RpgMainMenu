package com.zerotoler.rpgmenu.domain.model

data class InventoryFilterState(
    val searchQuery: String,
    val showOwnedOnly: Boolean,
    val selectedRarity: Int?,
    val selectedCombatType: CombatType?,
    val selectedSpinDirection: SpinDirection?,
    val sortMode: InventorySortMode,
) {
    companion object {
        val Default = InventoryFilterState(
            searchQuery = "",
            showOwnedOnly = false,
            selectedRarity = null,
            selectedCombatType = null,
            selectedSpinDirection = null,
            sortMode = InventorySortMode.NAME,
        )
    }
}
