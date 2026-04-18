package com.zerotoler.rpgmenu.data.content

import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.battle.EffectiveBattleStats

data class BotDefinition(
    val id: String,
    val displayName: String,
    val title: String,
    val powerLevel: Int,
    val combatType: CombatType = CombatType.BALANCE,
) {
    fun toStats(): EffectiveBattleStats {
        val scale = 1f + powerLevel * 0.04f
        return EffectiveBattleStats(
            maxHp = 3200f * scale,
            attack = 420f * scale,
            defense = 130f * scale,
            stamina = 32f * scale,
            skillRegenPerSecond = 6f + powerLevel * 0.1f,
            combatType = combatType,
        )
    }
}

object BotRoster {
    val ladder: List<BotDefinition> = listOf(
        BotDefinition("bot_01", "Kai Drifter", "Rookie III", 3, CombatType.BALANCE),
        BotDefinition("bot_02", "Mira Pulse", "Rookie II", 5, CombatType.STAMINA),
        BotDefinition("bot_03", "Juno Vector", "Rookie I", 7, CombatType.ATTACK),
        BotDefinition("bot_04", "Soren Axis", "Bronze V", 10, CombatType.DEFENSE),
        BotDefinition("bot_05", "Lyra Nova", "Bronze IV", 12, CombatType.BALANCE),
        BotDefinition("bot_06", "Orin Helix", "Bronze III", 14, CombatType.STAMINA),
        BotDefinition("bot_07", "Vex Striker", "Silver Edge", 18, CombatType.ATTACK),
        BotDefinition("bot_08", "Nia Flux", "Silver Core", 22, CombatType.DEFENSE),
    )

    fun byId(id: String): BotDefinition? = ladder.find { it.id == id }

    fun randomPractice(random: kotlin.random.Random = kotlin.random.Random.Default): BotDefinition =
        ladder.random(random)
}
