package com.zerotoler.rpgmenu.domain.corebattle.model

import com.zerotoler.rpgmenu.domain.corebattle.math.Vec2
import kotlin.math.sqrt

/**
 * A runtime simulation body + combatant.
 *
 * Build is immutable (parts + derived totals).
 * Simulation state is mutable (HP/RPM/pos/vel/energy).
 */
class SpinningTop(
    val core: AvatarCore,
    val ring: AttackRing,
    val tip: DriverTip,
    /**
     * Physics body radius used for circle-vs-circle and arena wall collisions.
     *
     * Not part of [PartStats] to keep your requested stat schema minimal.
     * If you don't pass it, we derive a reasonable default from total weight.
     */
    radiusOverride: Float? = null,
) {
    val totalStats: PartStats = core.stats + ring.stats + tip.stats

    /** Collision radius (meters-ish in sim space). */
    val radius: Float = radiusOverride ?: defaultRadiusFromWeight(totalStats.weight)

    /** Mutable simulation state. */
    var currentHp: Float = totalStats.hp
        private set
    var currentRpm: Float = totalStats.rpm
        private set

    val position: Vec2 = Vec2(0f, 0f)
    val velocity: Vec2 = Vec2(0f, 0f)

    /**
     * Energy meter used for special moves/etc. Clamped to [0, 100].
     */
    var energy: Float = 0f
        private set

    fun applyHpDelta(delta: Float) {
        currentHp = (currentHp + delta).coerceIn(0f, totalStats.hp)
    }

    fun applyRpmDelta(delta: Float) {
        currentRpm = (currentRpm + delta).coerceAtLeast(0f)
    }

    fun addEnergy(delta: Float) {
        energy = (energy + delta).coerceIn(0f, 100f)
    }

    fun setEnergy(value: Float) {
        energy = value.coerceIn(0f, 100f)
    }

    fun isBurst(): Boolean = currentHp <= 0f
    fun isSpinFinished(): Boolean = currentRpm <= 0f

    companion object {
        /**
         * Designer-friendly default radius mapping.
         *
         * The engine needs a radius but your required [PartStats] does not contain size.
         * This keeps the core API simple while still making heavier tops slightly larger.
         */
        fun defaultRadiusFromWeight(weight: Float): Float {
            val w = weight.coerceAtLeast(0.01f)
            // sqrt keeps growth sublinear so sizes don't explode with large weights.
            return (0.07f + 0.02f * sqrt(w)).coerceIn(0.06f, 0.16f)
        }
    }
}

private operator fun PartStats.plus(o: PartStats): PartStats =
    PartStats(
        hp = this.hp + o.hp,
        rpm = this.rpm + o.rpm,
        attack = this.attack + o.attack,
        defense = this.defense + o.defense,
        weight = this.weight + o.weight,
    )

