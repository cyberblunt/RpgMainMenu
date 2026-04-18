package com.zerotoler.rpgmenu.domain.engine

import kotlin.math.sqrt

object CollisionResolver {

    data class TopBody(
        val pos: Vec2,
        val vel: Vec2,
        val radius: Float,
        val mass: Float,
    )

    data class TopCollisionResult(
        val impactSpeed: Float,
        val newOmegaA: Float,
        val newOmegaB: Float,
        val slipSpeed: Float,
    )

    data class WallImpactResult(
        val impactSpeed: Float,
        val newOmega: Float,
        val slipSpeed: Float,
    )

    /**
     * Separates overlap and applies inelastic impulse along normal. Returns impact speed along normal (pre-resolution magnitude).
     */
    fun resolveCircleCircle(
        a: TopBody,
        b: TopBody,
        restitution: Float,
        omegaA: Float,
        omegaB: Float,
        inertiaA: Float,
        inertiaB: Float,
        frictionMu: Float,
    ): TopCollisionResult {
        val dx = b.pos.x - a.pos.x
        val dy = b.pos.y - a.pos.y
        var dist = sqrt(dx * dx + dy * dy)
        val minDist = a.radius + b.radius
        if (dist < 1e-5f) {
            dist = 1e-5f
        }
        val nx = dx / dist
        val ny = dy / dist
        val overlap = minDist - dist
        if (overlap <= 0f) {
            return TopCollisionResult(
                impactSpeed = 0f,
                newOmegaA = omegaA,
                newOmegaB = omegaB,
                slipSpeed = 0f,
            )
        }
        val invMassA = 1f / a.mass.coerceAtLeast(0.01f)
        val invMassB = 1f / b.mass.coerceAtLeast(0.01f)
        // Penetration correction: slightly under-correct to avoid jitter/sticking.
        val corr = overlap / (invMassA + invMassB) * 0.92f
        a.pos.x -= nx * corr * invMassA
        a.pos.y -= ny * corr * invMassA
        b.pos.x += nx * corr * invMassB
        b.pos.y += ny * corr * invMassB

        val rvx = b.vel.x - a.vel.x
        val rvy = b.vel.y - a.vel.y
        val velAlongNormal = rvx * nx + rvy * ny
        val tx = -ny
        val ty = nx
        val velAlongT = rvx * tx + rvy * ty
        val relTang = velAlongT + omegaB * b.radius - omegaA * a.radius

        if (velAlongNormal > 0f) {
            // They are separating; we still did overlap correction above but avoid impulse coupling jitter.
            return TopCollisionResult(
                impactSpeed = 0f,
                newOmegaA = omegaA,
                newOmegaB = omegaB,
                slipSpeed = kotlin.math.abs(relTang),
            )
        }

        val jN = -(1f + restitution) * velAlongNormal / (invMassA + invMassB)
        val ix = jN * nx
        val iy = jN * ny
        a.vel.x -= ix * invMassA
        a.vel.y -= iy * invMassA
        b.vel.x += ix * invMassB
        b.vel.y += iy * invMassB

        // Tangential friction impulse: couples spin with contact tangential slip.
        val invInertiaA = 1f / inertiaA.coerceAtLeast(1e-4f)
        val invInertiaB = 1f / inertiaB.coerceAtLeast(1e-4f)
        val effectiveMassT =
            (invMassA + invMassB) + (a.radius * a.radius) * invInertiaA + (b.radius * b.radius) * invInertiaB

        val jTUnclamped = -relTang / effectiveMassT
        val jNAbs = kotlin.math.abs(jN)
        val mu = frictionMu.coerceIn(0f, 0.5f)
        val jTMax = mu * jNAbs
        val jT = jTUnclamped.coerceIn(-jTMax, jTMax)

        // Apply tangential impulse to linear velocities.
        a.vel.x -= tx * jT * invMassA
        a.vel.y -= ty * jT * invMassA
        b.vel.x += tx * jT * invMassB
        b.vel.y += ty * jT * invMassB

        // Apply tangential impulse to angular velocities.
        val newOmegaA = omegaA - (a.radius * jT) * invInertiaA
        val newOmegaB = omegaB + (b.radius * jT) * invInertiaB

        return TopCollisionResult(
            impactSpeed = (-velAlongNormal).coerceAtLeast(0f),
            newOmegaA = newOmegaA,
            newOmegaB = newOmegaB,
            slipSpeed = kotlin.math.abs(relTang),
        )
    }

    fun resolveArenaWall(
        pos: Vec2,
        vel: Vec2,
        radius: Float,
        mass: Float,
        omega: Float,
        arenaRadius: Float,
        restitution: Float,
        frictionMu: Float,
    ): WallImpactResult {
        val d = sqrt(pos.x * pos.x + pos.y * pos.y)
        val maxR = (arenaRadius - radius).coerceAtLeast(0.01f)
        if (d <= 1e-5f || d <= maxR) return WallImpactResult(0f, omega, 0f)
        val nx = pos.x / d
        val ny = pos.y / d
        val vnOut = vel.x * nx + vel.y * ny
        val impact = kotlin.math.abs(vnOut)
        pos.x = nx * maxR
        pos.y = ny * maxR
        if (vnOut > 0f) {
            val e = restitution.coerceIn(0.2f, 0.85f)
            val tx = -ny
            val ty = nx
            val velAlongT = vel.x * tx + vel.y * ty
            val relTang = velAlongT + omega * radius

            // Normal bounce.
            vel.x -= (1f + e) * vnOut * nx
            vel.y -= (1f + e) * vnOut * ny

            // Tangential friction capped similarly to Coulomb friction.
            val invMass = 1f / mass.coerceAtLeast(0.01f)
            val inertia = 0.5f * mass * radius * radius
            val invInertia = 1f / inertia.coerceAtLeast(1e-4f)
            val effectiveMassT = invMass + (radius * radius) * invInertia

            val jNAbs = (1f + e) * impact * mass
            val mu = frictionMu.coerceIn(0f, 0.5f)
            val jTMax = mu * jNAbs

            val jTUnclamped = -relTang / effectiveMassT
            val jT = jTUnclamped.coerceIn(-jTMax, jTMax)

            vel.x -= tx * jT * invMass
            vel.y -= ty * jT * invMass
            val newOmega = omega - (radius * jT) * invInertia

            return WallImpactResult(
                impactSpeed = impact,
                newOmega = newOmega,
                slipSpeed = kotlin.math.abs(relTang),
            )
        }
        return WallImpactResult(
            impactSpeed = impact,
            newOmega = omega,
            slipSpeed = 0f,
        )
    }
}
