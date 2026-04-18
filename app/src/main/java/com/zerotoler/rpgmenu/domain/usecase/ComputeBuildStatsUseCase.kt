package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.domain.model.BuildPreviewState
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.DriverClass
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartStats
import com.zerotoler.rpgmenu.domain.model.RingClass
import com.zerotoler.rpgmenu.domain.model.SpinDirection

class ComputeBuildStatsUseCase {
    operator fun invoke(
        battleCap: PartBase?,
        weightRing: PartBase?,
        driver: PartBase?,
    ): BuildPreviewState {
        val parts = listOfNotNull(battleCap, weightRing, driver)
        val total = PartStats(
            health = parts.mapNotNull { it.stats.health }.takeIf { it.isNotEmpty() }?.sum(),
            attack = parts.mapNotNull { it.stats.attack }.takeIf { it.isNotEmpty() }?.sum(),
            defense = parts.mapNotNull { it.stats.defense }.takeIf { it.isNotEmpty() }?.sum(),
            stamina = parts.mapNotNull { it.stats.stamina }.takeIf { it.isNotEmpty() }?.sum(),
            intervalSeconds = parts.mapNotNull { it.stats.intervalSeconds }.takeIf { it.isNotEmpty() }
                ?.reduce { a, b -> a + b },
            weightGrams = parts.mapNotNull { it.stats.weightGrams }.takeIf { it.isNotEmpty() }
                ?.reduce { a, b -> a + b },
        )
        return BuildPreviewState(
            battleCap = battleCap,
            weightRing = weightRing,
            driver = driver,
            totalStats = total,
            derivedTags = deriveTags(battleCap, weightRing, driver, parts),
        )
    }

    private fun deriveTags(
        battleCap: PartBase?,
        weightRing: PartBase?,
        driver: PartBase?,
        parts: List<PartBase>,
    ): List<String> {
        val tags = linkedSetOf<String>()
        battleCap?.combatType?.takeIf { it != CombatType.UNKNOWN }?.let {
            tags += "Cap: $it"
        }
        val spin = battleCap?.spinDirection?.takeIf { it != SpinDirection.UNKNOWN }
        spin?.let { tags += "Spin: $it" }
        weightRing?.ringClass?.takeIf { it != RingClass.UNKNOWN }?.let {
            tags += "Ring: $it"
        }
        driver?.driverClass?.takeIf { it != DriverClass.UNKNOWN }?.let {
            tags += "Driver: $it"
        }
        parts.flatMap { it.tags }.forEach { tags += it }
        val bias = dominantCombatBias(parts)
        bias?.let { tags += "Build bias: $it" }
        return tags.toList()
    }

    private fun dominantCombatBias(parts: List<PartBase>): CombatType? {
        if (parts.isEmpty()) return null
        val weights = parts.mapNotNull { p ->
            when (p.combatType) {
                CombatType.ATTACK -> CombatType.ATTACK to 3f
                CombatType.DEFENSE -> CombatType.DEFENSE to 2f
                CombatType.STAMINA -> CombatType.STAMINA to 2f
                CombatType.BALANCE -> CombatType.BALANCE to 1f
                CombatType.UNKNOWN -> null
            }
        }
        if (weights.isEmpty()) return null
        val grouped = weights.groupBy({ it.first }, { it.second })
        val best = grouped.maxByOrNull { (_, v) -> v.sum() }
        return best?.key
    }
}
