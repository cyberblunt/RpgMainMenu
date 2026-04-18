package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.repository.InventoryRepository
import com.zerotoler.rpgmenu.data.repository.LoadoutRepository
import com.zerotoler.rpgmenu.data.repository.PartCatalogRepository
import com.zerotoler.rpgmenu.domain.model.Loadout
import com.zerotoler.rpgmenu.domain.model.PartCategory

class EquipPartUseCase(
    private val catalogRepository: PartCatalogRepository,
    private val inventoryRepository: InventoryRepository,
    private val loadoutRepository: LoadoutRepository,
) {
    suspend operator fun invoke(
        partId: String,
        expectedCategory: PartCategory,
        teamId: String,
        slotIndex: Int,
    ): Result<Unit> {
        val part = catalogRepository.getPartById(partId)
            ?: return Result.failure(IllegalStateException("Unknown part"))
        if (part.category != expectedCategory) {
            return Result.failure(IllegalStateException("Category mismatch"))
        }
        val state = inventoryRepository.getPlayerState(partId)
        if (state == null || !state.owned) {
            return Result.failure(IllegalStateException("Not owned"))
        }
        val base = loadoutRepository.getLoadoutOnce(teamId, slotIndex)
            ?: Loadout(
                teamId = teamId,
                slotIndex = slotIndex,
                name = null,
                battleCapId = null,
                weightRingId = null,
                driverId = null,
            )
        val updated = when (expectedCategory) {
            PartCategory.BATTLE_CAP -> base.copy(battleCapId = partId)
            PartCategory.WEIGHT_RING -> base.copy(weightRingId = partId)
            PartCategory.DRIVER -> base.copy(driverId = partId)
        }
        loadoutRepository.upsertLoadout(updated)
        return Result.success(Unit)
    }
}
