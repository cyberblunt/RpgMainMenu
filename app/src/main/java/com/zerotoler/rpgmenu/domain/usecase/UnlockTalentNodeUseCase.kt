package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.content.TalentCatalog
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository

class UnlockTalentNodeUseCase(
    private val progressRepository: PlayerProgressRepository,
) {
    suspend operator fun invoke(nodeId: String): Result<Unit> {
        val def = TalentCatalog.byId(nodeId)
            ?: return Result.failure(IllegalArgumentException("Unknown node"))
        val talent = progressRepository.snapshotTalent()
        if (nodeId in talent.unlockedNodeIds) {
            return Result.failure(IllegalStateException("Already unlocked"))
        }
        if (!def.prerequisiteIds.all { it in talent.unlockedNodeIds }) {
            return Result.failure(IllegalStateException("Prerequisites missing"))
        }
        val wallet = progressRepository.snapshotWallet()
        if (wallet.gold < def.costGold) {
            return Result.failure(IllegalStateException("Not enough gold"))
        }
        progressRepository.mutateWallet { w ->
            w.copy(gold = w.gold - def.costGold)
        }
        progressRepository.updateTalent { t ->
            t.copy(unlockedNodeIds = t.unlockedNodeIds + nodeId)
        }
        return Result.success(Unit)
    }
}
