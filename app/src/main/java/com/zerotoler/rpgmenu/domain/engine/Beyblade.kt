package com.zerotoler.rpgmenu.domain.engine

import kotlin.math.hypot
import kotlin.math.min
import kotlin.random.Random

class Beyblade(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
) {

    val radius = 60f
    val mass = 1f

    var spinSpeed: Float
    var angle = 0f
    var isAlive = true

    val LEVEL_1 = 0f
    val LEVEL_2 = 130f
    val LEVEL_3 = 260f

    val targetLevel: Float
    val orbitDirection: Float

    var isAttacking = false
    var hasAttackedThisRound = false
    var attackTimer = Random.nextInt(360, 600)

    var attackTarget: Beyblade? = null
    var attackStartTime = 0L
    val attackDuration = 1000L

    val CENTER_AREA_RADIUS = radius * 1.2f

    init {
        val levels = listOf(LEVEL_1, LEVEL_2, LEVEL_3)
        targetLevel = levels.random()
        orbitDirection = if (Random.nextBoolean()) 1f else -1f

        val launchSpeed = hypot(vx, vy)
        spinSpeed = min(launchSpeed * 0.9f, 1.5f)
        if (spinSpeed < 0.3f) spinSpeed = 0.8f
    }

    fun update(all: List<Beyblade>, centerX: Float, centerY: Float) {
        if (!isAlive) return

        val dxCenter = x - centerX
        val dyCenter = y - centerY
        val distCenter = hypot(dxCenter, dyCenter)

        val enemies = all.filter { it != this && it.isAlive }

        // --- ATTACK INIT ---
        if (enemies.isEmpty()) {
            isAttacking = false
            attackTarget = null
        }
        if (isAttacking && (attackTarget?.isAlive != true)) {
            isAttacking = false
            attackTarget = null
        }

        if (!hasAttackedThisRound && !isAttacking && enemies.isNotEmpty()) {
            attackTimer--
            if (attackTimer <= 0) {
                isAttacking = true
                hasAttackedThisRound = true
                attackStartTime = System.currentTimeMillis()

                attackTarget = enemies.minByOrNull {
                    hypot(it.x - x, it.y - y)
                }

                vx *= 2f
                vy *= 2f
            }
        }

        // --- SPEED LIMIT ---
        val baseMaxSpeed = 11.2f
        val spinRatio = spinSpeed / 1.5f
        var maxSpeed = baseMaxSpeed * spinRatio

        if (isAttacking) maxSpeed *= 1.5f

        // --- ATTACK MOVEMENT ---
        attackTarget?.let { target ->
            if (isAttacking && target.isAlive) {

                val dx = target.x - x
                val dy = target.y - y
                val dist = hypot(dx, dy)

                if (dist > 0) {
                    val nx = dx / dist
                    val ny = dy / dist

                    val elapsed = (System.currentTimeMillis() - attackStartTime).toFloat()
                    val progress = min(elapsed / attackDuration, 1f)

                    val force = 0.5f + progress * 0.7f

                    vx += nx * force
                    vy += ny * force
                }
            }
        }

        val stabilizationMultiplier = if (isAttacking) 0.25f else 1f

        // --- CENTER ---
        if (targetLevel == LEVEL_1) {
            if (distCenter > 0.1f) {
                val nx = dxCenter / distCenter
                val ny = dyCenter / distCenter

                val pull = distCenter * 0.001f * stabilizationMultiplier

                vx -= nx * pull
                vy -= ny * pull

                vx += (Random.nextFloat() - 0.5f) * 0.05f
                vy += (Random.nextFloat() - 0.5f) * 0.05f

                vx *= 0.98f
                vy *= 0.98f
            }
        } else {
            val inCenter = distCenter < CENTER_AREA_RADIUS

            if (!inCenter && distCenter > 0.1f) {
                val nx = dxCenter / distCenter
                val ny = dyCenter / distCenter

                val diff = targetLevel - distCenter
                val radial = diff * 0.0004f * stabilizationMultiplier

                vx += nx * radial
                vy += ny * radial

                val tangentialX = -ny * orbitDirection
                val tangentialY = nx * orbitDirection

                val desiredSpeed = 7f
                val currentTan = vx * tangentialX + vy * tangentialY

                val accel = 0.1f * stabilizationMultiplier

                if (currentTan < desiredSpeed * 0.9f) {
                    vx += tangentialX * accel
                    vy += tangentialY * accel
                } else if (currentTan > desiredSpeed * 1.1f) {
                    vx -= tangentialX * accel * 0.5f
                    vy -= tangentialY * accel * 0.5f
                }

                if (distCenter > 5f) {
                    val speed = hypot(vx, vy)
                    val centripetal = (speed * speed) / distCenter

                    vx -= nx * centripetal * stabilizationMultiplier
                    vy -= ny * centripetal * stabilizationMultiplier
                }
            }
        }

        // --- MOVE ---
        x += vx
        y += vy

        // --- SPIN DECAY ---
        spinSpeed -= 0.00015f
        if (spinSpeed <= 0f) {
            spinSpeed = 0f
            isAlive = false
        }

        angle += spinSpeed

        val speed = hypot(vx, vy)

        if (speed > maxSpeed && maxSpeed > 0.1f) {
            vx *= 0.94f
            vy *= 0.94f
        } else {
            vx *= 0.998f
            vy *= 0.998f
        }
    }
}

object BeybladePhysics {
    private const val BOUNCE_MULTIPLIER = 1.05f
    private const val WALL_RESTITUTION = 0.92f
    private const val WALL_SPIN_PENALTY = 0.0025f
    private const val COLLISION_WEAK_SPIN_PENALTY = 0.0018f

    fun step(
        tops: List<Beyblade>,
        centerX: Float,
        centerY: Float,
        arenaRadius: Float,
    ) {
        for (i in tops.indices) {
            tops[i].update(tops, centerX, centerY)
        }
        resolveTopTopCollisions(tops)
        resolveArenaBounds(tops, centerX, centerY, arenaRadius)
    }

    fun resolveTopTopCollisions(tops: List<Beyblade>) {
        val n = tops.size
        var i = 0
        while (i < n) {
            val a = tops[i]
            if (!a.isAlive) {
                i++
                continue
            }
            var j = i + 1
            while (j < n) {
                val b = tops[j]
                if (!b.isAlive) {
                    j++
                    continue
                }

                val dx = b.x - a.x
                val dy = b.y - a.y
                val dist = hypot(dx, dy)
                val minDist = a.radius + b.radius
                if (dist > 0f && dist < minDist) {
                    val nx = dx / dist
                    val ny = dy / dist

                    // --- RESOLVE OVERLAP ---
                    val overlap = minDist - dist
                    val invMassA = 1f / a.mass
                    val invMassB = 1f / b.mass
                    val corr = overlap / (invMassA + invMassB)
                    a.x -= nx * corr * invMassA
                    a.y -= ny * corr * invMassA
                    b.x += nx * corr * invMassB
                    b.y += ny * corr * invMassB

                    // --- IMPULSE (ELASTIC) ---
                    val rvx = b.vx - a.vx
                    val rvy = b.vy - a.vy
                    val velAlongNormal = rvx * nx + rvy * ny
                    if (velAlongNormal < 0f) {
                        val e = BOUNCE_MULTIPLIER
                        val jn = -(1f + e) * velAlongNormal / (invMassA + invMassB)
                        val ix = jn * nx
                        val iy = jn * ny
                        a.vx -= ix * invMassA
                        a.vy -= iy * invMassA
                        b.vx += ix * invMassB
                        b.vy += iy * invMassB

                        // Cancel attacks on collision.
                        cancelAttack(a)
                        cancelAttack(b)

                        // Reduce spin of weaker top.
                        val impact = -velAlongNormal
                        if (a.spinSpeed < b.spinSpeed) {
                            a.spinSpeed = (a.spinSpeed - impact * COLLISION_WEAK_SPIN_PENALTY).coerceAtLeast(0f)
                        } else if (b.spinSpeed < a.spinSpeed) {
                            b.spinSpeed = (b.spinSpeed - impact * COLLISION_WEAK_SPIN_PENALTY).coerceAtLeast(0f)
                        } else {
                            a.spinSpeed = (a.spinSpeed - impact * (COLLISION_WEAK_SPIN_PENALTY * 0.5f)).coerceAtLeast(0f)
                            b.spinSpeed = (b.spinSpeed - impact * (COLLISION_WEAK_SPIN_PENALTY * 0.5f)).coerceAtLeast(0f)
                        }

                        if (a.spinSpeed <= 0f) a.isAlive = false
                        if (b.spinSpeed <= 0f) b.isAlive = false
                    }
                }
                j++
            }
            i++
        }
    }

    fun resolveArenaBounds(
        tops: List<Beyblade>,
        centerX: Float,
        centerY: Float,
        arenaRadius: Float,
    ) {
        for (k in tops.indices) {
            val t = tops[k]
            if (!t.isAlive) continue

            val dx = t.x - centerX
            val dy = t.y - centerY
            val dist = hypot(dx, dy)
            val maxR = arenaRadius - t.radius
            if (dist <= 0f || dist <= maxR) continue

            val nx = dx / dist
            val ny = dy / dist

            // Snap back inside.
            t.x = centerX + nx * maxR
            t.y = centerY + ny * maxR

            // Reflect velocity if moving outward.
            val vn = t.vx * nx + t.vy * ny
            if (vn > 0f) {
                t.vx -= (1f + WALL_RESTITUTION) * vn * nx
                t.vy -= (1f + WALL_RESTITUTION) * vn * ny
                t.spinSpeed -= vn * WALL_SPIN_PENALTY
                if (t.spinSpeed <= 0f) {
                    t.spinSpeed = 0f
                    t.isAlive = false
                }
                cancelAttack(t)
            }
        }
    }

    private fun cancelAttack(t: Beyblade) {
        t.isAttacking = false
        t.attackTarget = null
    }
}

