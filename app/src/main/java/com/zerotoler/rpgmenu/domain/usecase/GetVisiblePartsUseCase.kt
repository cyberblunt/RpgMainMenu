package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.domain.model.InventoryFilterState
import com.zerotoler.rpgmenu.domain.model.InventorySortMode
import com.zerotoler.rpgmenu.domain.model.Loadout
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import com.zerotoler.rpgmenu.domain.model.PlayerPartState
import com.zerotoler.rpgmenu.domain.model.VisibleInventoryItem
import java.util.Locale

class GetVisiblePartsUseCase {
    operator fun invoke(
        parts: List<PartBase>,
        playerById: Map<String, PlayerPartState>,
        filter: InventoryFilterState,
        loadout: Loadout?,
        selectedCategory: PartCategory,
    ): List<VisibleInventoryItem> {
        val q = filter.searchQuery.trim().lowercase(Locale.getDefault())
        val list = parts.filter { it.category == selectedCategory }.filter { part ->
            val state = playerById[part.id] ?: PlayerPartState(
                partId = part.id,
                owned = false,
                level = 1,
                shardCount = 0,
                copies = 0,
                isFavorite = false,
                obtainedAtEpochMillis = null,
            )
            if (filter.showOwnedOnly && !state.owned) return@filter false
            if (q.isNotEmpty() && !part.name.lowercase(Locale.getDefault()).contains(q)) return@filter false
            filter.selectedRarity?.let { r -> if (part.rarity != r) return@filter false }
            filter.selectedCombatType?.let { ct -> if (part.combatType != ct) return@filter false }
            filter.selectedSpinDirection?.let { sd -> if (part.spinDirection != sd) return@filter false }
            true
        }
        val sorted = when (filter.sortMode) {
            InventorySortMode.NAME -> list.sortedBy { it.name.lowercase(Locale.getDefault()) }
            InventorySortMode.RARITY -> list.sortedWith(
                compareBy({ it.rarity ?: Int.MAX_VALUE }, { it.name.lowercase(Locale.getDefault()) }),
            )
            InventorySortMode.LEVEL -> list.sortedByDescending { playerById[it.id]?.level ?: 0 }
            InventorySortMode.ATTACK -> list.sortedByDescending { it.stats.attack ?: Int.MIN_VALUE }
            InventorySortMode.DEFENSE -> list.sortedByDescending { it.stats.defense ?: Int.MIN_VALUE }
            InventorySortMode.STAMINA -> list.sortedByDescending { it.stats.stamina ?: Int.MIN_VALUE }
            InventorySortMode.HEALTH -> list.sortedByDescending { it.stats.health ?: Int.MIN_VALUE }
            InventorySortMode.WEIGHT -> list.sortedByDescending { it.stats.weightGrams ?: Float.MIN_VALUE }
            InventorySortMode.INTERVAL -> list.sortedByDescending { it.stats.intervalSeconds ?: Float.MIN_VALUE }
        }
        return sorted.map { part ->
            val st = playerById[part.id] ?: PlayerPartState(
                partId = part.id,
                owned = false,
                level = 1,
                shardCount = 0,
                copies = 0,
                isFavorite = false,
                obtainedAtEpochMillis = null,
            )
            val equipped = when (part.category) {
                PartCategory.BATTLE_CAP -> loadout?.battleCapId == part.id
                PartCategory.WEIGHT_RING -> loadout?.weightRingId == part.id
                PartCategory.DRIVER -> loadout?.driverId == part.id
            }
            VisibleInventoryItem(
                part = part,
                player = st,
                isEquippedInActiveLoadout = equipped,
            )
        }
    }
}
