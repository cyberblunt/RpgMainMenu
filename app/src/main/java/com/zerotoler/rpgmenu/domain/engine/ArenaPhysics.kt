package com.zerotoler.rpgmenu.domain.engine

import com.zerotoler.rpgmenu.domain.model.CombatType
object ArenaPhysics {
    private const val MAX_SPEED = 3.4f
    private const val LINEAR_DRAG = 0.25f
    private const val ANGULAR_DRAG = 0.22f

    fun applyMovementForces(
        v: Vec2,
        pos: Vec2,
        towardEnemy: Vec2,
        archetype: CombatType,
        staminaRatio: Float,
        tilt: Float,
        dt: Float,
    ) {
        val s = staminaRatio.coerceIn(0f, 1f)
        val t = tilt.coerceIn(0f, 1f)
        // When stamina is low, the top can't translate spin into stable motion.
        val controlMul = (1f - 0.55f * t).coerceIn(0.18f, 1f)
        val accelMul = (0.35f + 0.65f * s) * controlMul
        val maxSpeed = MAX_SPEED * (0.45f + 0.55f * s) * (0.95f - 0.35f * t * (1f - s))
        val dragMul = (0.65f + 0.9f * (1f - s)) * (1f + 0.65f * t) // more drag when exhausted + tipped

        var ax = 0f
        var ay = 0f
        when (archetype) {
            CombatType.ATTACK -> {
                ax += towardEnemy.x * 0.45f * accelMul
                ay += towardEnemy.y * 0.45f * accelMul
            }
            CombatType.STAMINA -> {
                ax -= pos.x * 0.18f * accelMul
                ay -= pos.y * 0.18f * accelMul
            }
            CombatType.DEFENSE -> {
                ax -= pos.x * 0.1f * accelMul
                ay -= pos.y * 0.1f * accelMul
            }
            CombatType.BALANCE, CombatType.UNKNOWN -> {
                ax -= pos.x * 0.12f * accelMul
                ay -= pos.y * 0.12f * accelMul
            }
        }

        // Hidden precession/nutation approximation:
        // when tilt + exhaustion are high, add small tangential wobble acceleration.
        val tx = -towardEnemy.y
        val ty = towardEnemy.x
        val wobbleStrength = (0.02f + 0.12f * t) * (1f - s)
        val wobblePhase = (pos.x * 3.9f + pos.y * 4.7f) * 1.8f + t * 6.0f
        val wobble = kotlin.math.sin(wobblePhase).toFloat()
        ax += tx * wobble * wobbleStrength
        ay += ty * wobble * wobbleStrength

        v.x += ax * dt
        v.y += ay * dt
        val sp = kotlin.math.sqrt(v.x * v.x + v.y * v.y)
        if (sp > maxSpeed) {
            val k = maxSpeed / sp
            v.x *= k
            v.y *= k
        }
        val drag = (1f - (LINEAR_DRAG * dragMul * dt)).coerceAtLeast(0.12f)
        v.x *= drag
        v.y *= drag
    }

    fun integratePosition(pos: Vec2, v: Vec2, dt: Float) {
        pos.x += v.x * dt
        pos.y += v.y * dt
    }

    fun integrateRotation(angle: Float, omega: Float, dt: Float): Float {
        var a = angle + omega * dt
        while (a > Math.PI.toFloat() * 2f) a -= (Math.PI.toFloat() * 2f)
        while (a < 0f) a += (Math.PI.toFloat() * 2f)
        return a
    }

    fun applyAngularDrag(
        omega: Float,
        archetype: CombatType,
        staminaRatio: Float,
        tilt: Float,
        dt: Float,
    ): Float {
        val drag = when (archetype) {
            CombatType.STAMINA -> ANGULAR_DRAG * 0.85f
            CombatType.ATTACK -> ANGULAR_DRAG * 1.08f
            else -> ANGULAR_DRAG
        }
        val s = staminaRatio.coerceIn(0f, 1f)
        val staminaDragMul = 0.65f + 1.25f * (1f - s) // exhausted = faster angular slowdown
        val tiltDragMul = 0.85f + 1.25f * tilt.coerceIn(0f, 1f)
        val w = omega * (1f - drag * staminaDragMul * tiltDragMul * dt)
        return w.coerceIn(-56f, 56f)
    }

    fun vectorToward(from: Vec2, to: Vec2): Vec2 {
        val dx = to.x - from.x
        val dy = to.y - from.y
        return Vec2(dx, dy).normalize()
    }

    fun pseudoRandom(seed: Int, salt: Int): Float {
        val x = (seed * 73856093 xor salt * 19349663) and 0x7fffffff
        return (x % 10000) / 10000f
    }
}
