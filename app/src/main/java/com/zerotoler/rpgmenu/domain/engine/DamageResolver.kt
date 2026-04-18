package com.zerotoler.rpgmenu.domain.engine

import com.zerotoler.rpgmenu.domain.model.CombatType

object DamageResolver {
    // Defense mitigation uses a division model so HP loss stays reliable across stat ranges.
    const val DEFENSE_SCALE = 0.12f
    const val STAMINA_IMPACT_SCALE = 0.11f
    const val WALL_DRAIN_SCALE = 0.2f
    const val WALL_DAMAGE_SCALE = 0.09f
    private const val ATTACK_DAMAGE_SCALE = 0.05f
    private const val WALL_DEFENSE_SCALE = 0.015f

    fun topVsTop(
        impactSpeed: Float,
        attackerCollisionPower: Float,
        attackerAttack: Float,
        defenderDefense: Float,
        defenderStaminaEfficiency: Float,
        attackerType: CombatType,
        defenderType: CombatType,
        defenderTilt: Float,
    ): Pair<Float, Float> {
        if (impactSpeed <= 0.01f) return 0f to 0f
        val burstMul = when (attackerType) {
            CombatType.ATTACK -> 1.18f
            CombatType.DEFENSE -> 0.92f
            CombatType.STAMINA -> 0.88f
            CombatType.BALANCE -> 1f
            CombatType.UNKNOWN -> 1f
        }
        val recvMul = when (defenderType) {
            CombatType.DEFENSE -> 0.85f
            CombatType.STAMINA -> 0.95f
            CombatType.ATTACK -> 1.05f
            CombatType.BALANCE -> 1f
            CombatType.UNKNOWN -> 1f
        }

        // Compute raw damage from impact and attack, then mitigate by defender defense.
        val damageBase = impactSpeed * attackerCollisionPower * burstMul + attackerAttack * ATTACK_DAMAGE_SCALE
        val mitigation = 1f + defenderDefense * DEFENSE_SCALE * recvMul
        val tiltVuln = 1f + defenderTilt.coerceIn(0f, 1f) * 0.14f
        val effective = (damageBase / mitigation * tiltVuln).coerceAtLeast(0f)

        // Stamina loss is tied to the impact magnitude.
        val staminaDrain =
            (impactSpeed * STAMINA_IMPACT_SCALE / defenderStaminaEfficiency.coerceAtLeast(0.35f)) *
                (1f + defenderTilt.coerceIn(0f, 1f) * 0.35f)
        return effective to staminaDrain
    }

    fun wallImpact(
        wallImpactSpeed: Float,
        defense: Float,
        tilt: Float,
    ): Pair<Float, Float> {
        if (wallImpactSpeed <= 0.05f) return 0f to wallImpactSpeed * WALL_DRAIN_SCALE * (1f + tilt.coerceIn(0f, 1f) * 0.35f)
        val drain = wallImpactSpeed * WALL_DRAIN_SCALE * (1f + tilt.coerceIn(0f, 1f) * 0.45f)
        // Wall damage should exist but remain modest so HP still matters more than walls.
        val dmg = (wallImpactSpeed * WALL_DAMAGE_SCALE - defense * WALL_DEFENSE_SCALE).coerceAtLeast(0f)
        return dmg to drain
    }

    fun passiveDrain(
        speed: Float,
        spin: Float,
        archetype: CombatType,
        stabilityBonus: Float,
        tilt: Float,
    ): Float {
        // Stamina drains faster when the top is losing motion (slow & low spin).
        // This makes stamina a meaningful "keep-spinning" resource instead of a decorative timer.
        val speedNorm = (speed.coerceIn(0f, 3.6f)) / 3.6f
        val spinNorm = (kotlin.math.abs(spin).coerceIn(0f, 40f)) / 40f
        val tiltNorm = tilt.coerceIn(0f, 1f)
        val motion = 0.50f * speedNorm + 0.50f * spinNorm
        val missing = (1f - motion).coerceIn(0f, 1f) + tiltNorm * (0.25f + 0.25f * (1f - speedNorm))
        val missingClamped = missing.coerceIn(0f, 1f)

        val base = 0.07f + missingClamped * 0.38f
        val typeMul = when (archetype) {
            CombatType.ATTACK -> 1.05f
            CombatType.DEFENSE -> 0.92f
            CombatType.STAMINA -> 0.88f
            CombatType.BALANCE, CombatType.UNKNOWN -> 1f
        }
        // StabilityBonus slightly reduces drain for the launcher.
        val tiltMul = 1f + tiltNorm * 0.25f
        return (base * typeMul * tiltMul - stabilityBonus * 0.35f).coerceIn(0.01f, 1.2f)
    }
}
