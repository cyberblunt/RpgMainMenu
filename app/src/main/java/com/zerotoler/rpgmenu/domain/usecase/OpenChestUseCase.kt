package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.seed.AcademyTasks
import com.zerotoler.rpgmenu.domain.engine.rewards.RewardTableProvider
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle

class OpenChestUseCase(
    private val progressRepository: PlayerProgressRepository,
    private val rewardGrantUseCase: RewardGrantUseCase,
) {
    suspend operator fun invoke(clientOpenId: String, count: Int = 1): Result<RewardBundle> {
        if (count < 1) return Result.failure(IllegalArgumentException("Invalid count"))
        val wallet = progressRepository.snapshotWallet()
        if (wallet.chestKeys < count) {
            return Result.failure(IllegalStateException("Not enough keys"))
        }
        if (!progressRepository.tryMarkChestOpenProcessed(clientOpenId)) {
            return Result.failure(IllegalStateException("Already processed"))
        }
        progressRepository.mutateWallet { w ->
            w.copy(chestKeys = w.chestKeys - count)
        }
        val merged = buildBundle(count)
        rewardGrantUseCase(merged)
        progressRepository.updateAcademy { a ->
            val m = a.tasks.toMutableMap()
            val cur = m[AcademyTasks.FIRST_CHEST] ?: com.zerotoler.rpgmenu.domain.model.progress.AcademyTaskEntry()
            m[AcademyTasks.FIRST_CHEST] = cur.copy(current = (cur.current + 1).coerceAtMost(1))
            a.copy(tasks = m)
        }
        return Result.success(merged)
    }

    private fun buildBundle(count: Int): RewardBundle {
        var gold = 0L
        var gems = 0
        var bp = 0
        val parts = mutableListOf<String>()
        val shards = mutableMapOf<String, Int>()
        repeat(count) {
            val b = RewardTableProvider.chestStandardBundle()
            gold += b.gold
            gems += b.gems
            bp += b.battlePassXp
            parts += b.partIds
            b.shardsByPartId.forEach { (k, v) ->
                shards[k] = (shards[k] ?: 0) + v
            }
        }
        return RewardBundle(
            gold = gold,
            gems = gems,
            partIds = parts.distinct(),
            shardsByPartId = shards,
            battlePassXp = bp,
        )
    }
}
