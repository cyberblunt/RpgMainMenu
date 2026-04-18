package com.zerotoler.rpgmenu.domain.model.battle

import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.DriverClass
import com.zerotoler.rpgmenu.domain.model.RingClass

enum class TopOwner {
    PLAYER,
    ENEMY,
}

/**
 * Authoritative combat projection for one top (from PARTS loadout or opponent config).
 */
data class BattleTopStats(
    val id: String,
    val owner: TopOwner,
    val displayName: String,
    val archetype: CombatType,
    /**
     * Part metadata needed for deterministic Beyblade-style motion logic.
     * These are *build-time* properties, persisted in the battle projection only.
     */
    val ringClass: RingClass = RingClass.UNKNOWN,
    val driverClass: DriverClass = DriverClass.UNKNOWN,
    /** Stabilization target derived from [driverClass] + [ringClass]. */
    val stabilizationLevel: StabilizationLevel = StabilizationLevel.INNER_RING,
    /** Physical weight in grams (clamped to gameplay-supported range). */
    val weightGrams: Float = 45f,
    val radius: Float,
    val mass: Float,
    val maxHealth: Float,
    val maxStamina: Float,
    val attack: Float,
    val defense: Float,
    val staminaEfficiency: Float,
    val balanceFactor: Float,
    val wallGrip: Float,
    val collisionPower: Float,
)
