package com.zerotoler.rpgmenu.data.content

import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.battle.EffectiveBattleStats

data class BossDefinition(
    val id: String,
    val displayName: String,
    val title: String,
    val powerLevel: Int,
    val combatType: CombatType,
    val durationScale: Float = 1.2f,
    val statScale: Float = 1.35f,
) {
    fun toStats(): EffectiveBattleStats {
        val scale = (1f + powerLevel * 0.045f) * statScale
        return EffectiveBattleStats(
            maxHp = 3600f * scale,
            attack = 450f * scale,
            defense = 155f * scale,
            stamina = 36f * scale,
            skillRegenPerSecond = 7f + powerLevel * 0.12f,
            combatType = combatType,
        )
    }
}

object BossCatalog {
    val all: List<BossDefinition> = listOf(
        BossDefinition(
            id = "boss_iron_ring",
            displayName = "Iron Ring Colossus",
            title = "Sector Boss · Defense",
            powerLevel = 24,
            combatType = CombatType.DEFENSE,
            durationScale = 1.25f,
            statScale = 1.42f,
        ),
        BossDefinition(
            id = "boss_crimson_spark",
            displayName = "Crimson Spark",
            title = "Elite Striker",
            powerLevel = 22,
            combatType = CombatType.ATTACK,
            durationScale = 1.15f,
            statScale = 1.38f,
        ),
        BossDefinition(
            id = "boss_eternal_axis",
            displayName = "Eternal Axis",
            title = "Endurance Titan",
            powerLevel = 26,
            combatType = CombatType.STAMINA,
            durationScale = 1.35f,
            statScale = 1.4f,
        ),
        BossDefinition(
            id = "boss_null_vector",
            displayName = "Null Vector",
            title = "Balance Arbiter",
            powerLevel = 25,
            combatType = CombatType.BALANCE,
            durationScale = 1.2f,
            statScale = 1.39f,
        ),
    )

    fun byId(id: String): BossDefinition? = all.find { it.id == id }
}
