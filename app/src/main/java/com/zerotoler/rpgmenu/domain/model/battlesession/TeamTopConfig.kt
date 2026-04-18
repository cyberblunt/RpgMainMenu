package com.zerotoler.rpgmenu.domain.model.battlesession

import com.zerotoler.rpgmenu.domain.model.CombatType

/**
 * Battle-facing projection of a single loadout slot from PARTS — not a second source of truth.
 */
data class TeamTopConfig(
    val slotIndex: Int,
    val battleCapId: String?,
    val weightRingId: String?,
    val driverId: String?,
    val displayName: String,
    val powerScore: Float,
    val dominantCombatType: CombatType,
    val isComplete: Boolean,
)
