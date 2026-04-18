package com.zerotoler.rpgmenu.domain.engine.rewards

import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import kotlin.random.Random

object RewardTableProvider {
    fun battleVictoryBundle(mode: String, random: Random = Random.Default): RewardBundle {
        val goldBase = when (mode) {
            "ranked" -> 800L
            "practice" -> 350L
            "boss" -> 1200L
            else -> 500L
        }
        val variance = random.nextLong(-80, 120)
        val gems = when {
            mode == "boss" && random.nextFloat() < 0.5f -> random.nextInt(2, 6)
            random.nextFloat() < 0.35f -> random.nextInt(1, 4)
            else -> 0
        }
        return RewardBundle(
            gold = (goldBase + variance).coerceAtLeast(50),
            gems = gems,
            battlePassXp = when (mode) {
                "ranked" -> 120
                "boss" -> 140
                else -> 80
            },
        )
    }

    fun battleDefeatBundle(mode: String): RewardBundle =
        RewardBundle(
            gold = when (mode) {
                "ranked" -> 120L
                "boss" -> 200L
                else -> 80L
            },
            battlePassXp = if (mode == "boss") 45 else 30,
        )

    fun chestStandardBundle(random: Random = Random.Default): RewardBundle {
        val roll = random.nextFloat()
        val partRoll = when {
            roll < 0.15f -> "ring_spread"
            roll < 0.3f -> "driver_agility"
            else -> ""
        }
        val parts = if (partRoll.isNotBlank()) listOf(partRoll) else emptyList()
        return RewardBundle(
            gold = random.nextLong(200, 900),
            gems = random.nextInt(0, 3),
            partIds = parts,
            shardsByPartId = if (parts.isEmpty()) {
                mapOf("cap_aiolos" to random.nextInt(2, 8))
            } else {
                emptyMap()
            },
            battlePassXp = 40,
        )
    }
}
