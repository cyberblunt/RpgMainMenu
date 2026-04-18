package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.repository.InventoryRepository
import com.zerotoler.rpgmenu.data.repository.LoadoutRepository
import com.zerotoler.rpgmenu.data.repository.PartCatalogRepository
import com.zerotoler.rpgmenu.domain.model.AutoConfigProfile
import com.zerotoler.rpgmenu.domain.model.Loadout
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import kotlinx.coroutines.flow.first

class AutoConfigLoadoutUseCase(
    private val catalogRepository: PartCatalogRepository,
    private val inventoryRepository: InventoryRepository,
    private val loadoutRepository: LoadoutRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        slotIndex: Int,
        profile: AutoConfigProfile = AutoConfigProfile.BALANCE,
    ) {
        val allParts = catalogRepository.getAllPartsFlow().first()
        val ownedIds = inventoryRepository.getAllPlayerStatesFlow().first()
            .filter { it.owned }
            .map { it.partId }
            .toSet()
        if (ownedIds.isEmpty()) return

        val ownedParts = allParts.filter { it.id in ownedIds }
        val cap = pickBest(ownedParts.filter { it.category == PartCategory.BATTLE_CAP }, profile)
        val ring = pickBest(ownedParts.filter { it.category == PartCategory.WEIGHT_RING }, profile)
        val drv = pickBest(ownedParts.filter { it.category == PartCategory.DRIVER }, profile)

        val base = loadoutRepository.getLoadoutOnce(teamId, slotIndex)
            ?: Loadout(teamId, slotIndex, null, null, null, null)
        loadoutRepository.upsertLoadout(
            base.copy(
                battleCapId = cap?.id ?: base.battleCapId,
                weightRingId = ring?.id ?: base.weightRingId,
                driverId = drv?.id ?: base.driverId,
            ),
        )
    }

    private fun pickBest(candidates: List<PartBase>, profile: AutoConfigProfile): PartBase? {
        if (candidates.isEmpty()) return null
        return candidates.maxByOrNull { score(it, profile) }
    }

    private fun score(p: PartBase, profile: AutoConfigProfile): Float {
        val h = (p.stats.health ?: 0).toFloat()
        val a = (p.stats.attack ?: 0).toFloat()
        val d = (p.stats.defense ?: 0).toFloat()
        val s = (p.stats.stamina ?: 0).toFloat()
        val iv = p.stats.intervalSeconds ?: 0f
        return when (profile) {
            AutoConfigProfile.ATTACK -> a * 3f + s + d * 0.5f
            AutoConfigProfile.DEFENSE -> d * 3f + h * 0.4f + a * 0.3f
            AutoConfigProfile.STAMINA -> s * 3f + iv * 10f + a * 0.2f
            AutoConfigProfile.BALANCE -> a + d + s + h * 0.1f + iv * 2f
        }
    }
}
