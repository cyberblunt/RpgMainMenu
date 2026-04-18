package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.repository.InventoryRepository
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle

class RewardGrantUseCase(
    private val progressRepository: PlayerProgressRepository,
    private val inventoryRepository: InventoryRepository,
) {
    suspend operator fun invoke(bundle: RewardBundle) {
        if (bundle.gold != 0L || bundle.gems != 0 || bundle.chestKeys != 0) {
            progressRepository.mutateWallet { w ->
                w.copy(
                    gold = (w.gold + bundle.gold).coerceAtLeast(0L),
                    gems = (w.gems + bundle.gems).coerceAtLeast(0),
                    chestKeys = (w.chestKeys + bundle.chestKeys).coerceAtLeast(0),
                )
            }
        }
        bundle.partIds.forEach { partId ->
            inventoryRepository.grantOwnership(partId)
        }
        bundle.shardsByPartId.forEach { (partId, amt) ->
            if (amt != 0) {
                inventoryRepository.addShards(partId, amt)
            }
        }
        if (bundle.battlePassXp > 0) {
            progressRepository.updateBattlePass { bp ->
                bp.copy(xp = bp.xp + bundle.battlePassXp)
            }
        }
    }
}
