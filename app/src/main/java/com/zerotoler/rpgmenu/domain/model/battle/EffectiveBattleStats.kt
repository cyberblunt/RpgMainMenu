package com.zerotoler.rpgmenu.domain.model.battle

import com.zerotoler.rpgmenu.domain.model.CombatType

data class EffectiveBattleStats(
    val maxHp: Float,
    val attack: Float,
    val defense: Float,
    val stamina: Float,
    val skillRegenPerSecond: Float,
    val combatType: CombatType = CombatType.BALANCE,
)
