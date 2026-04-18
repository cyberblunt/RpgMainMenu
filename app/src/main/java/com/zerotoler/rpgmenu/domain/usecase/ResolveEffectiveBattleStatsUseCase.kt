package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.content.CollectionSetDefinitions
import com.zerotoler.rpgmenu.data.content.TalentCatalog
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.battle.EffectiveBattleStats

class ResolveEffectiveBattleStatsUseCase(
    private val computeBuildStatsUseCase: ComputeBuildStatsUseCase,
) {
    operator fun invoke(
        battleCap: PartBase?,
        weightRing: PartBase?,
        driver: PartBase?,
        unlockedTalentNodeIds: Set<String>,
        ownedPartIds: Set<String>,
    ): EffectiveBattleStats {
        val preview = computeBuildStatsUseCase(battleCap, weightRing, driver)
        var hp = (preview.totalStats.health ?: 3000).toFloat()
        var atk = (preview.totalStats.attack ?: 400).toFloat()
        var def = (preview.totalStats.defense ?: 120).toFloat()
        var stamina = (preview.totalStats.stamina ?: 35).toFloat()

        TalentCatalog.nodes.filter { it.id in unlockedTalentNodeIds }.forEach { node ->
            atk += node.attackBonus.toFloat()
            def += node.defenseBonus.toFloat()
            hp += node.hpBonus.toFloat()
            stamina += node.staminaBonus.toFloat()
        }

        CollectionSetDefinitions.sets.forEach { set ->
            if (set.requiredPartIds.all { it in ownedPartIds }) {
                atk += set.attackBonus.toFloat()
                def += set.defenseBonus.toFloat()
                hp += set.hpBonus.toFloat()
                stamina += set.staminaBonus.toFloat()
            }
        }

        val regen = 4f + stamina * 0.08f
        val archetype = when (battleCap?.combatType) {
            CombatType.UNKNOWN, null -> CombatType.BALANCE
            else -> battleCap.combatType
        }
        return EffectiveBattleStats(
            maxHp = hp.coerceAtLeast(200f),
            attack = atk.coerceAtLeast(10f),
            defense = def.coerceAtLeast(1f),
            stamina = stamina.coerceAtLeast(5f),
            skillRegenPerSecond = regen.coerceIn(2f, 25f),
            combatType = archetype,
        )
    }
}
