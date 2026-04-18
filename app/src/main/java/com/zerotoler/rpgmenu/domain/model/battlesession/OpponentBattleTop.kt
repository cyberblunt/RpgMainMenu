package com.zerotoler.rpgmenu.domain.model.battlesession

import com.zerotoler.rpgmenu.domain.model.CombatType

/**
 * Local/mock opponent top for battle prep. Future online layers can map API DTOs here.
 */
data class OpponentBattleTop(
    val id: String,
    val name: String,
    val archetype: CombatType,
    val powerLevelHint: Int,
    val intelLabel: String,
)
