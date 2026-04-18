package com.zerotoler.rpgmenu.domain.engine

import com.zerotoler.rpgmenu.domain.model.battle.BattleOutcome
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.domain.model.battle.BattleTopStats
import com.zerotoler.rpgmenu.domain.model.battle.CollisionEffect
import com.zerotoler.rpgmenu.domain.model.battle.FloatingImpact
import com.zerotoler.rpgmenu.domain.model.battle.StabilizationLevel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World
import org.dyn4j.dynamics.ContinuousDetectionMode

/**
 * Dyn4j-based top-down battle simulation.
 *
 * Physics is stepped at a fixed timestep externally (60Hz). Rendering reads a lightweight snapshot.
 */
class BattleEngine(
    private val playerBase: BattleTopStats,
    private val enemyBase: BattleTopStats,
    private val sessionSeed: Int,
    private val roundIndex: Int,
) {
    private val arenaRadius = 1.0
    private val world: World<Body> = World()

    private val playerBody: Body
    private val enemyBody: Body

    private val topRadiusScale = 0.7

    private var battlePhase = BattlePhase.LAUNCH
    private var battleOutcome = BattleOutcome.NONE
    private var battleTimeSec = 90.0

    private var aiming = false
    private var aimDir = Vector2(0.0, -1.0)
    private var aimPower01 = 0.0
    private var aimOscTimeSec = 0.0
    private val aimOscPeriodSec = 1.25

    private var tickCount = 0L

    private var playerHp = playerBase.maxHealth.toDouble()
    private var enemyHp = enemyBase.maxHealth.toDouble()
    private var playerStam = playerBase.maxStamina.toDouble()
    private var enemyStam = enemyBase.maxStamina.toDouble()

    // Tracks absolute simulation time for i-frames
    private var simTimeSec = 0.0

    // Damage i-frames to prevent micro-frame shredding during sustained contact
    private val lastDamageMsByPair = HashMap<Long, Long>(8)
    private val damageCooldownMs = 300L

    /** Линейная скорость не срезается стаминой, пока доля стамины выше этого порога (10%). */
    private val staminaLinearMotionThreshold = 0.1

    /** Вращение: прежняя логика — срез только когда стамина не выше 40% от макс. */
    private val staminaSpinMotionThreshold = 0.4

    // --- Beyblade-style stabilization rings (relative to arena radius) ---
    private val innerRingRadius = arenaRadius * 0.37
    private val outerRingRadius = arenaRadius * 0.74

    // --- JS-reference behavior constants (time-based, stable at 60Hz) ---
    private val attackDurationSec = 1.0
    private val globalFriction = 0.998
    /** After overspeed, scale velocity toward cap (preserves direction); avoids repeated 0.94 "spiral" drain. */
    private val overspeedSoftClamp = true
    private val baseMaxSpeed = 3.2 // sim-space; launch speed is ~[0.9..3.2]
    private val attackSpeedCapMul = 1.5
    private val attackBurstMul = 2.0
    private val attackMagnetismBase = 0.5
    private val attackMagnetismRamp = 0.7
    private val stabilizationWeakenDuringAttack = 0.25
    private val spinSpeedRef = 55.0 // "full spin" reference (rad/s)
    private val staminaDrainPerSecAtFullSpin = 2.8 // tuned: ~35s for maxStamina~100 at high spin
    private val collisionDamagePerSpeed = 12.0 // JS: impactSpeed * 0.5 (pixel units); scaled for sim-space
    private val collisionDamageMin = 1.0
    private val collisionSpinPenalty = 3.0 // rad/s subtracted from the slower top on impact

    /** Inner/outer ring groove stabilization: +70% force vs previous tuning ([CENTER] unchanged). */
    private val ringStabilizationForceMul = 1.7

    /** Keep attack-level speed cap briefly after dash ends so linear clamp + stamina caps don't eat momentum. */
    private val postAttackSpeedCapRelaxSec = 0.85

    // --- Lightweight visual effects buffers (only allocate on events) ---
    private val effects = ArrayList<CollisionEffect>(32)
    private val impacts = ArrayList<FloatingImpact>(16)

    init {
        world.gravity = Vector2(0.0, 0.0)
        world.settings.setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY)
        world.settings.setStepFrequency(1.0 / 60.0)

        // No physical wall body: segment rings cause tangential friction / rim-sliding.
        // Arena is enforced in enforceArenaBounce() after each step.

        val maxScaledR = max(playerBase.radius, enemyBase.radius).toDouble() * topRadiusScale
        val spawnY = (arenaRadius - maxScaledR).coerceAtLeast(0.10) * 0.78
        playerBody = createTopBody(playerBase, 0.0, spawnY)
        enemyBody = createTopBody(enemyBase, 0.0, -spawnY)

        world.addBody(playerBody)
        world.addBody(enemyBody)
    }

    fun currentPhase(): BattlePhase = battlePhase
    fun currentOutcome(): BattleOutcome = battleOutcome

    fun beginAim() {
        if (battlePhase != BattlePhase.LAUNCH) return
        aiming = true
        aimOscTimeSec = 0.0
    }

    fun updateAimDirection(dx: Float, dy: Float) {
        if (!aiming || battlePhase != BattlePhase.LAUNCH) return
        val x = dx.toDouble()
        val y = dy.toDouble()
        val len = sqrt(x * x + y * y)
        if (len < 1e-6) return
        // Launch is opposite to drag/pull vector.
        aimDir = Vector2(-x / len, -y / len)
    }

    fun releaseLaunch() {
        if (battlePhase != BattlePhase.LAUNCH || !aiming) return
        aiming = false

        val linearSpeed = lerp(1.0, 3.2, aimPower01)
        val spin = lerp(18.0, 55.0, aimPower01) // rad/s-ish; used as angular velocity directly

        playerBody.applyImpulse(
            Vector2(
                aimDir.x * playerBase.mass.toDouble() * linearSpeed,
                aimDir.y * playerBase.mass.toDouble() * linearSpeed,
            ),
        )
        playerBody.setAngularVelocity(spin)
        seedMotionPeaksFromBody(playerBody)

        // Enemy auto-launch with deterministic variation.
        val seed = sessionSeed xor (roundIndex * 131) xor 0x9E3779B9.toInt()
        val edx = (pseudoRandom(seed, 1) - 0.5) * 0.55
        val edir = run {
            val lx = edx
            val ly = 1.0
            val ll = sqrt(lx * lx + ly * ly).coerceAtLeast(1e-9)
            Vector2(lx / ll, ly / ll)
        }
        val epow = (0.35 + pseudoRandom(seed, 2) * 0.55).coerceIn(0.0, 1.0)
        val elinear = lerp(0.9, 2.7, epow)
        val espin = lerp(14.0, 48.0, epow)
        enemyBody.applyImpulse(Vector2(edir.x * enemyBase.mass.toDouble() * elinear, edir.y * enemyBase.mass.toDouble() * elinear))
        enemyBody.setAngularVelocity(espin)
        seedMotionPeaksFromBody(enemyBody)

        battlePhase = BattlePhase.ACTIVE
    }

    fun activateSuperAbility(): Boolean = false

    fun fixedStep(dt: Float) {
        val d = dt.toDouble().coerceIn(0.0, 0.05)
        tickCount++
        simTimeSec += d

        if (battlePhase == BattlePhase.LAUNCH) {
            battleTimeSec = 90.0
            if (aiming) {
                aimOscTimeSec += d
                aimPower01 = triangle01(t = aimOscTimeSec, period = aimOscPeriodSec)
            }
            return
        }
        if (battlePhase == BattlePhase.RESULT) return

        battleTimeSec -= d

        // Physics step (fixed 60Hz externally, but we still clamp).
        world.step(1)

        // Beyblade-style behavior layers on top of Dyn4j resolution.
        manageAttacks(d)
        applyAttackAndStabilization(playerBody, enemyBody, d)
        applyAttackAndStabilization(enemyBody, playerBody, d)

        // Hard arena boundary bounce to prevent rim "gliding"
        enforceArenaBounce(playerBody, playerRadius())
        enforceArenaBounce(enemyBody, enemyRadius())
        tickEffects(d.toFloat())
        drainStaminaFromSpin(d)
        applyHpDamageIfContact()
        // После расхода стамины (дрейн + удары) привязываем скорости к стамине
        applyStaminaMotionCoupling(playerBody, playerStam, playerBase.maxStamina.toDouble())
        applyStaminaMotionCoupling(enemyBody, enemyStam, enemyBase.maxStamina.toDouble())
        resolveBattleEnd()
    }

    fun buildSnapshot(): BattleRenderSnapshot {
        val p = playerBody.transform.translation
        val e = enemyBody.transform.translation
        val pAngle = wrapAngle(playerBody.transform.rotation.toRadians())
        val eAngle = wrapAngle(enemyBody.transform.rotation.toRadians())

        val pOmega = abs(playerBody.angularVelocity)
        val eOmega = abs(enemyBody.angularVelocity)

        val effectsSnapshot = if (effects.isEmpty()) emptyList() else effects.toList()
        val impactsSnapshot = if (impacts.isEmpty()) emptyList() else impacts.toList()

        return BattleRenderSnapshot(
            tick = tickCount,
            phase = battlePhase,
            outcome = battleOutcome,
            arenaCenterX = 0f,
            arenaCenterY = 0f,
            arenaRadius = arenaRadius.toFloat(),
            showLaunchers = battlePhase == BattlePhase.LAUNCH,
            launcherPlayerX = 0f,
            launcherPlayerY = p.y.toFloat(),
            launcherEnemyX = 0f,
            launcherEnemyY = e.y.toFloat(),
            playerX = p.x.toFloat(),
            playerY = p.y.toFloat(),
            playerAngle = pAngle.toFloat(),
            playerRadius = (playerBase.radius * topRadiusScale).toFloat(),
            enemyX = e.x.toFloat(),
            enemyY = e.y.toFloat(),
            enemyAngle = eAngle.toFloat(),
            enemyRadius = (enemyBase.radius * topRadiusScale).toFloat(),
            aimActive = aiming,
            aimDirX = aimDir.x.toFloat(),
            aimDirY = aimDir.y.toFloat(),
            powerOscillator01 = aimPower01.toFloat(),
            playerHp = playerHp.toInt().coerceAtLeast(0),
            playerHpMax = playerBase.maxHealth.toInt().coerceAtLeast(1),
            playerStam = playerStam.roundToInt().coerceIn(0, playerBase.maxStamina.toInt().coerceAtLeast(1)),
            playerStamMax = playerBase.maxStamina.toInt().coerceAtLeast(1),
            enemyHp = enemyHp.toInt().coerceAtLeast(0),
            enemyHpMax = enemyBase.maxHealth.toInt().coerceAtLeast(1),
            enemyStam = enemyStam.roundToInt().coerceIn(0, enemyBase.maxStamina.toInt().coerceAtLeast(1)),
            enemyStamMax = enemyBase.maxStamina.toInt().coerceAtLeast(1),
            timerSec = battleTimeSec.toInt().coerceAtLeast(0),
            superMeter = 0f,
            superAbilityActive = false,
            superAbilityRemainingSec = 0f,
            collisionFlashA = 0f,
            collisionFlashB = 0f,
            screenShakeX = 0f,
            screenShakeY = 0f,
            effects = effectsSnapshot,
            impacts = impactsSnapshot,
            playerArchetype = playerBase.archetype,
            enemyArchetype = enemyBase.archetype,
        )
    }

    private fun linearStaminaScale(s01: Double): Double {
        if (s01 <= 0.0) return 0.0
        return if (s01 > staminaLinearMotionThreshold) 1.0 else s01 / staminaLinearMotionThreshold
    }

    private fun spinStaminaScale(s01: Double): Double {
        if (s01 <= 0.0) return 0.0
        return if (s01 > staminaSpinMotionThreshold) 1.0 else s01 / staminaSpinMotionThreshold
    }

    private fun seedMotionPeaksFromBody(body: Body) {
        val rt = body.userData as? TopRuntime ?: return
        val v = body.linearVelocity
        val sp = sqrt(v.x * v.x + v.y * v.y)
        val aw = abs(body.angularVelocity)
        if (sp > rt.peakLinearSpeed) rt.peakLinearSpeed = sp
        if (aw > rt.peakAngularSpeed) rt.peakAngularSpeed = aw
    }

    /**
     * Линейно: без среза по стамине, пока s01 > 10%; ниже — пик × (s/10%).
     * Вращение: без среза, пока s01 > 40%; ниже — пик × (s/40%), как раньше. Остановка только при stam = 0.
     */
    private fun applyStaminaMotionCoupling(body: Body, currentStam: Double, maxStam: Double) {
        if (!body.isEnabled) return
        val rt = body.userData as? TopRuntime ?: return
        val maxS = maxStam.coerceAtLeast(1e-9)
        val stam = currentStam.coerceIn(0.0, maxS)
        val s01 = stam / maxS
        rt.currentStamina = stam

        if (stam <= 0.0) {
            body.setLinearVelocity(0.0, 0.0)
            body.setAngularVelocity(0.0)
            return
        }

        val v = body.linearVelocity
        val sp = sqrt(v.x * v.x + v.y * v.y)
        val w = body.angularVelocity
        val aw = abs(w)

        // Always grow peaks while alive: gating on stamina made post-attack / burst speeds exceed stale caps,
        // so stamina coupling shredded movement right when stabilization needed it most.
        if (sp > rt.peakLinearSpeed) rt.peakLinearSpeed = sp
        if (aw > rt.peakAngularSpeed) rt.peakAngularSpeed = aw

        val linScale = linearStaminaScale(s01)
        val spinScale = spinStaminaScale(s01)
        val baseLin = rt.peakLinearSpeed.coerceAtLeast(0.9)
        val baseAng = rt.peakAngularSpeed.coerceAtLeast(8.0)
        val capLin = baseLin * linScale
        val capAng = baseAng * spinScale

        if (linScale < 1.0 - 1e-12) {
            if (sp > capLin && capLin > 1e-9) {
                val k = capLin / sp
                body.setLinearVelocity(v.x * k, v.y * k)
            } else if (capLin > 1e-6 && sp < capLin * 0.88) {
                val a = body.transform.rotation.toRadians()
                val dirX: Double
                val dirY: Double
                if (sp > 1e-5) {
                    dirX = v.x / sp
                    dirY = v.y / sp
                } else {
                    dirX = -sin(a)
                    dirY = cos(a)
                }
                body.setLinearVelocity(dirX * capLin, dirY * capLin)
            }
        }

        val wLin = body.angularVelocity
        val awLin = abs(wLin)
        if (spinScale < 1.0 - 1e-12) {
            if (awLin > capAng && capAng > 1e-9) {
                body.setAngularVelocity(if (wLin >= 0.0) capAng else -capAng)
            } else if (capAng > 1e-6 && awLin < capAng * 0.88) {
                val sgn = when {
                    abs(wLin) > 1e-5 -> if (wLin >= 0.0) 1.0 else -1.0
                    else -> 1.0
                }
                body.setAngularVelocity(sgn * capAng)
            }
        }
    }

    /**
     * JS reference: stamina decays over time, linked to spin speed (not translation speed).
     * We implement it in sim-time using Dyn4j's angular velocity as "spinSpeed".
     */
    private fun drainStaminaFromSpin(dt: Double) {
        val pr = playerBody.userData as? TopRuntime
        val er = enemyBody.userData as? TopRuntime
        if (pr != null && playerBody.isEnabled) {
            val spin = abs(playerBody.angularVelocity)
            val spin01 = (spin / spinSpeedRef).coerceIn(0.0, 1.0)
            val drain = staminaDrainPerSecAtFullSpin * spin01
            pr.currentStamina = (pr.currentStamina - drain * dt).coerceAtLeast(0.0)
            playerStam = pr.currentStamina
            if (pr.currentStamina <= 0.0) killTop(playerBody)
        }
        if (er != null && enemyBody.isEnabled) {
            val spin = abs(enemyBody.angularVelocity)
            val spin01 = (spin / spinSpeedRef).coerceIn(0.0, 1.0)
            val drain = staminaDrainPerSecAtFullSpin * spin01
            er.currentStamina = (er.currentStamina - drain * dt).coerceAtLeast(0.0)
            enemyStam = er.currentStamina
            if (er.currentStamina <= 0.0) killTop(enemyBody)
        }
    }

    private fun applyHpDamageIfContact() {
        if (battlePhase != BattlePhase.ACTIVE) return
        if (!playerBody.isEnabled || !enemyBody.isEnabled) return

        // Cheap contact test: distance threshold in sim space.
        val pPos = playerBody.transform.translation
        val ePos = enemyBody.transform.translation
        val dx = ePos.x - pPos.x
        val dy = ePos.y - pPos.y
        val dist2 = dx * dx + dy * dy
        val minDist = (playerBase.radius.toDouble() * topRadiusScale) + (enemyBase.radius.toDouble() * topRadiusScale)
        if (dist2 > (minDist * 1.04) * (minDist * 1.04)) return

        val pr = playerBody.userData as? TopRuntime ?: return
        val er = enemyBody.userData as? TopRuntime ?: return

        // Collisions interrupt attacks immediately (required mechanic).
        if (pr.isAttacking || er.isAttacking) {
            cancelAttack(pr)
            cancelAttack(er)
        }

        val nowMs = System.currentTimeMillis()
        val key = pairKey(pr.id, er.id)
        val last = lastDamageMsByPair[key] ?: Long.MIN_VALUE
        if (nowMs - last < damageCooldownMs) return
        lastDamageMsByPair[key] = nowMs

        val va = playerBody.linearVelocity
        val vb = enemyBody.linearVelocity
        val rvx = va.x - vb.x
        val rvy = va.y - vb.y
        val relativeSpeed = sqrt(rvx * rvx + rvy * rvy)

        // JS reference: HP decreases on collision, scaling with impact speed.
        // We keep i-frames (damageCooldownMs) to avoid micro-frame shredding, but use speed-only damage.
        val damage = (relativeSpeed * collisionDamagePerSpeed).coerceAtLeast(collisionDamageMin)
        er.currentHp -= damage
        pr.currentHp -= damage

        // Small stamina hit on impacts; keeps "big crashes" meaningful without dominating spin decay.
        val staminaHit = (relativeSpeed * 1.8).coerceIn(0.0, 8.0)
        er.currentStamina = (er.currentStamina - staminaHit).coerceIn(0.0, er.maxStamina)
        pr.currentStamina = (pr.currentStamina - staminaHit).coerceIn(0.0, pr.maxStamina)

        // Small post-impact "wobble" to reflect heavy-vs-light deviation:
        // light gets pushed more; heavy gets a minor deflection.
        val totalMass = (pr.mass + er.mass).coerceAtLeast(1e-6)
        val prMass01 = (pr.mass / totalMass).coerceIn(0.05, 0.95)
        val erMass01 = 1.0 - prMass01
        val dist = sqrt(dist2).coerceAtLeast(1e-9)
        val nx = dx / dist
        val ny = dy / dist
        val impulseScale = (0.025 + relativeSpeed * 0.008).coerceIn(0.0, 0.12)
        // Light receives more of the impulse.
        val toEnemy = impulseScale * (0.35 + 0.9 * prMass01)
        val toPlayer = impulseScale * (0.35 + 0.9 * erMass01)
        enemyBody.applyImpulse(Vector2(nx * toEnemy, ny * toEnemy))
        playerBody.applyImpulse(Vector2(-nx * toPlayer, -ny * toPlayer))

        emitImpactSparks(x = (pPos.x + ePos.x) * 0.5, y = (pPos.y + ePos.y) * 0.5, intensity = relativeSpeed.toFloat())

        // JS reference: spin penalty on collision to the weaker/slower-spinning top.
        val pOmega = abs(playerBody.angularVelocity)
        val eOmega = abs(enemyBody.angularVelocity)
        if (pOmega > eOmega) {
            val newAbs = (eOmega - collisionSpinPenalty).coerceAtLeast(0.0)
            enemyBody.setAngularVelocity(if (enemyBody.angularVelocity >= 0.0) newAbs else -newAbs)
        } else {
            val newAbs = (pOmega - collisionSpinPenalty).coerceAtLeast(0.0)
            playerBody.setAngularVelocity(if (playerBody.angularVelocity >= 0.0) newAbs else -newAbs)
        }

        syncFromRuntime()
        if (pr.currentHp <= 0.0) killTop(playerBody)
        if (er.currentHp <= 0.0) killTop(enemyBody)
    }

    private fun resolveBattleEnd() {
        if (battlePhase != BattlePhase.ACTIVE) return

        // Round ends ONLY on HP or stamina reaching 0.
        val pLose = playerHp <= 0.0 || playerStam <= 0.0
        val eLose = enemyHp <= 0.0 || enemyStam <= 0.0

        when {
            pLose && eLose -> {
                battleOutcome = if (playerHp >= enemyHp) BattleOutcome.PLAYER_WIN else BattleOutcome.PLAYER_LOSS
                battlePhase = BattlePhase.RESULT
            }
            pLose -> {
                battleOutcome = BattleOutcome.PLAYER_LOSS
                battlePhase = BattlePhase.RESULT
            }
            eLose -> {
                battleOutcome = BattleOutcome.PLAYER_WIN
                battlePhase = BattlePhase.RESULT
            }
            battleTimeSec <= 0.0 -> {
                battleOutcome = if (playerHp >= enemyHp) BattleOutcome.PLAYER_WIN else BattleOutcome.PLAYER_LOSS
                battlePhase = BattlePhase.RESULT
            }
            else -> Unit
        }
    }

    private fun playerRadius(): Double = playerBase.radius.toDouble() * topRadiusScale
    private fun enemyRadius(): Double = enemyBase.radius.toDouble() * topRadiusScale

    private fun enforceArenaBounce(body: Body, radius: Double) {
        if (!body.isEnabled) return
        val rt = body.userData as? TopRuntime
        val t = body.transform.translation
        val dx = t.x
        val dy = t.y
        val d2 = dx * dx + dy * dy
        val maxR = (arenaRadius - radius).coerceAtLeast(0.02)
        val d = sqrt(d2).coerceAtLeast(1e-12)
        // Near-rim band: correct sliding along an invisible wall
        val shell = (radius * 0.08).coerceAtMost(arenaRadius * 0.04).coerceAtLeast(0.02)
        val nx = dx / d
        val ny = dy / d
        val v = body.linearVelocity
        val vn = v.x * nx + v.y * ny
        val inShell = d >= maxR - shell
        val pastRing = d > maxR

        if (!inShell && !pastRing) return

        // If we're merely near the rim (inside the shell) but not moving outward, do nothing.
        // Otherwise OUTER_RING stabilization gets "braked" every frame, losing speed without actual wall contact.
        if (!pastRing && vn <= 0.0) return

        // Keep center inside playable radius (small inset avoids numeric chatter)
        val inset = (radius * 0.02).coerceAtMost(maxR * 0.01).coerceAtLeast(1e-4)
        val targetD = (maxR - inset).coerceAtLeast(0.01)
        if (pastRing && d > targetD) {
            body.translate(nx * (targetD - d), ny * (targetD - d))
        }

        val e = 0.92
        var vx = body.linearVelocity.x
        var vy = body.linearVelocity.y
        val vn2 = vx * nx + vy * ny

        // Bounce on outward impacts. Only strip tangential slip on true penetration (pastRing),
        // otherwise a clean boundary hit would "dead-stop" due to over-aggressive tangential damping.
        if (vn2 > 0.0) {
            // Bounce if moving outward through the boundary
            vx -= (1.0 + e) * vn2 * nx
            vy -= (1.0 + e) * vn2 * ny

            if (pastRing) {
                // Strip some tangential slip only when we actually penetrated the ring.
                val vnAfter = vx * nx + vy * ny
                val tx = -ny
                val ty = nx
                val vt = vx * tx + vy * ty
                // Higher wallGrip (and heavier weight) retains a bit more tangential velocity.
                val grip = (rt?.wallGrip ?: 1.0).coerceIn(0.75, 1.35)
                val massN = (rt?.mass ?: 1.0).coerceIn(0.65, 2.1)
                val tangentialRetain =
                    (0.55 + 0.12 * (grip - 0.75) / 0.60 + 0.05 * (massN - 0.65) / 1.45).coerceIn(0.45, 0.72)
                vx = nx * vnAfter + tx * vt * tangentialRetain
                vy = ny * vnAfter + ty * vt * tangentialRetain
            }

            body.setLinearVelocity(vx, vy)
        } else if (pastRing) {
            // If we got pushed slightly past the ring but are already moving inward, just keep velocity.
            body.setLinearVelocity(vx, vy)
        }

        // Wall hit costs some stamina/spin. Kept modest to avoid wall-dominant outcomes.
        if (pastRing && rt != null && vn2 > 0.0) {
            val impact = vn2
            val spinPenalty = (0.22 + impact * 0.08) * (2.1 / rt.mass.coerceAtLeast(0.65))
            body.setAngularVelocity(body.angularVelocity * (1.0 - spinPenalty.coerceIn(0.02, 0.10)))
            rt.currentStamina = (rt.currentStamina - (0.35 + impact * 0.9)).coerceAtLeast(0.0)
            emitImpactSparks(x = t.x, y = t.y, intensity = (impact * 7.0).toFloat())
        }
    }

    private fun createTopBody(stats: BattleTopStats, x: Double, y: Double): Body {
        val b = Body()
        val radius = (stats.radius.toDouble() * topRadiusScale).coerceAtLeast(0.02)
        val circle = Circle(radius)
        val area = PI * radius * radius
        val density = (stats.mass.toDouble() / area).coerceAtLeast(0.05)
        val fx = BodyFixture(circle).apply {
            this.density = density
            this.friction = 0.45
            this.restitution = 0.62
        }
        b.addFixture(fx)
        b.setMass(MassType.NORMAL)
        b.translate(x, y)
        b.setBullet(true)
        // Замедление только через стамину, не через внутренний damping Dyn4j
        b.setLinearDamping(0.0)
        b.setAngularDamping(0.0)
        b.userData = TopRuntime(
            id = stats.id.hashCode().toLong(),
            maxHp = stats.maxHealth.toDouble(),
            currentHp = stats.maxHealth.toDouble(),
            maxStamina = stats.maxStamina.toDouble(),
            currentStamina = stats.maxStamina.toDouble(),
            balanceFactor = stats.balanceFactor.toDouble().coerceIn(0.65, 1.35),
            attackStat = stats.attack.toDouble().coerceAtLeast(0.01),
            defenseStat = stats.defense.toDouble().coerceAtLeast(0.0),
            mass = stats.mass.toDouble().coerceAtLeast(0.01),
            weightGrams = stats.weightGrams.toDouble().coerceIn(32.5, 54.5),
            wallGrip = stats.wallGrip.toDouble().coerceIn(0.75, 1.35),
            stabilization = stats.stabilizationLevel,
            orbitDirection = if (pseudoRandom(sessionSeed xor stats.id.hashCode(), 11) > 0.5) 1 else -1,
            hasAttackedThisRound = false,
            isAttacking = false,
            attackTimerSec = initialAttackTimerSec(stats.id.hashCode()),
            attackElapsedSec = 0.0,
            peakLinearSpeed = 0.0,
            peakAngularSpeed = 0.0,
            linearSpeedCapBoostUntilSec = 0.0,
        )
        return b
    }

    private fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t.coerceIn(0.0, 1.0)

    private fun pseudoRandom(seed: Int, salt: Int): Double {
        val x = (seed * 73856093 xor salt * 19349663) and 0x7fffffff
        return (x % 10000) / 10000.0
    }

    private fun wrapAngle(rad: Double): Double {
        var a = rad
        val twoPi = 2.0 * PI
        while (a > twoPi) a -= twoPi
        while (a < 0.0) a += twoPi
        return a
    }

    private fun max(a: Float, b: Float): Float = if (a > b) a else b

    /**
     * Side-to-side constant-speed oscillator in [0..1] (triangle wave).
     * t: seconds, period: seconds for a full back-and-forth cycle.
     */
    private fun triangle01(t: Double, period: Double): Double {
        val p = period.coerceAtLeast(1e-6)
        val u = (t % p) / p // [0..1)
        return if (u < 0.5) (u * 2.0) else (2.0 - u * 2.0)
    }

    private data class TopRuntime(
        val id: Long,
        var maxHp: Double,
        var currentHp: Double,
        var maxStamina: Double,
        var currentStamina: Double,
        /** Build-time stability factor (higher = steadier orbit/center settling). */
        var balanceFactor: Double,
        var attackStat: Double,
        var defenseStat: Double,
        var mass: Double,
        var weightGrams: Double,
        var wallGrip: Double,
        var stabilization: StabilizationLevel,
        /** +1 or -1 */
        var orbitDirection: Int,
        var hasAttackedThisRound: Boolean,
        var isAttacking: Boolean,
        var attackTimerSec: Double,
        var attackElapsedSec: Double,
        /** Peak linear/angular speeds for stamina coupling (updated every frame while enabled). */
        var peakLinearSpeed: Double,
        var peakAngularSpeed: Double,
        /** Sim-time until which spin-based speed cap stays at post-attack headroom (see [postAttackSpeedCapRelaxSec]). */
        var linearSpeedCapBoostUntilSec: Double,
    )

    private fun initialAttackTimerSec(idHash: Int): Double {
        // Deterministic 6..10s per top per round seed.
        val r = pseudoRandom(sessionSeed xor idHash, 97).coerceIn(0.0, 1.0)
        return 6.0 + r * 4.0
    }

    private fun nextAttackTimerSec(rt: TopRuntime, salt: Int): Double {
        val r = pseudoRandom((sessionSeed xor rt.id.toInt()), salt).coerceIn(0.0, 1.0)
        return 6.0 + r * 4.0
    }

    private fun manageAttacks(dt: Double) {
        if (battlePhase != BattlePhase.ACTIVE) return
        val pr = playerBody.userData as? TopRuntime ?: return
        val er = enemyBody.userData as? TopRuntime ?: return
        if (!playerBody.isEnabled || !enemyBody.isEnabled) {
            cancelAttack(pr)
            cancelAttack(er)
            return
        }
        val roundOver = pr.hasAttackedThisRound && !pr.isAttacking && er.hasAttackedThisRound && !er.isAttacking
        if (roundOver) {
            pr.hasAttackedThisRound = false
            er.hasAttackedThisRound = false
            pr.attackTimerSec = nextAttackTimerSec(pr, salt = 101)
            er.attackTimerSec = nextAttackTimerSec(er, salt = 103)
        } else {
            // Tick down only if not already attacking
            if (!pr.isAttacking && !pr.hasAttackedThisRound) pr.attackTimerSec -= dt
            if (!er.isAttacking && !er.hasAttackedThisRound) er.attackTimerSec -= dt
        }
    }

    private fun cancelAttack(rt: TopRuntime) {
        val wasAttacking = rt.isAttacking
        rt.isAttacking = false
        rt.attackElapsedSec = 0.0
        if (wasAttacking) {
            rt.linearSpeedCapBoostUntilSec = simTimeSec + postAttackSpeedCapRelaxSec
        }
    }

    private fun applyAttackAndStabilization(self: Body, enemy: Body, dt: Double) {
        if (!self.isEnabled) return
        val rt = self.userData as? TopRuntime ?: return
        val et = enemy.userData as? TopRuntime

        // Attack initiation (periodic).
        if (!rt.hasAttackedThisRound && !rt.isAttacking && enemy.isEnabled && et != null && rt.attackTimerSec <= 0.0) {
            rt.isAttacking = true
            rt.hasAttackedThisRound = true
            rt.attackElapsedSec = 0.0
            // Burst dash (interruptible by collision handling).
            val v = self.linearVelocity
            self.setLinearVelocity(v.x * attackBurstMul, v.y * attackBurstMul)
            emitAttackTrail(self.transform.translation.x, self.transform.translation.y, intensity = 0.9f)
        }

        val omegaAbs = abs(self.angularVelocity)
        val spinRatio = (omegaAbs / spinSpeedRef).coerceIn(0.0, 1.0)
        // JS: baseMaxSpeed * spinRatio; keep a small floor so exhausted tops still drift a bit.
        val baseSpinCap = (baseMaxSpeed * spinRatio).coerceAtLeast(0.25)
        var maxAllowedSpeed = baseSpinCap
        if (rt.isAttacking) {
            maxAllowedSpeed *= attackSpeedCapMul
        } else if (simTimeSec < rt.linearSpeedCapBoostUntilSec) {
            // Same headroom as during attack so velocity built by dash + magnetism is not stripped the next frames.
            maxAllowedSpeed = baseSpinCap * attackSpeedCapMul
        }

        // Attack magnetism (toward nearest enemy; 1v1 => enemy).
        if (rt.isAttacking && enemy.isEnabled && et != null) {
            rt.attackElapsedSec += dt
            val p = self.transform.translation
            val q = enemy.transform.translation
            val dx = q.x - p.x
            val dy = q.y - p.y
            val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1e-9)
            val nx = dx / d
            val ny = dy / d
            val progress = (rt.attackElapsedSec / attackDurationSec).coerceIn(0.0, 1.0)
            val magnetism = (attackMagnetismBase + progress * attackMagnetismRamp)
            self.setLinearVelocity(
                self.linearVelocity.x + nx * magnetism,
                self.linearVelocity.y + ny * magnetism,
            )
            if (tickCount % 3L == 0L) {
                emitAttackTrail(p.x, p.y, intensity = (0.45f + 0.55f * progress.toFloat()))
            }
            // End attack after duration.
            if (rt.attackElapsedSec >= attackDurationSec) {
                rt.isAttacking = false
                rt.attackElapsedSec = 0.0
                rt.linearSpeedCapBoostUntilSec = simTimeSec + postAttackSpeedCapRelaxSec
            }
        }

        // Stabilization always applies, but is weakened during attack.
        val stabilizationMul = if (rt.isAttacking) stabilizationWeakenDuringAttack else 1.0
        applyStabilizationForces(self = self, rt = rt, dt = dt, mul = stabilizationMul)

        // If still over cap after stabilization, scale to cap (direction preserved). Avoid 0.94-per-frame drain.
        val lv = self.linearVelocity
        val sp = sqrt(lv.x * lv.x + lv.y * lv.y)
        if (sp > maxAllowedSpeed && maxAllowedSpeed > 1e-6) {
            if (overspeedSoftClamp) {
                val k = maxAllowedSpeed / sp
                self.setLinearVelocity(lv.x * k, lv.y * k)
            } else {
                self.setLinearVelocity(lv.x * 0.94, lv.y * 0.94)
            }
        } else {
            self.setLinearVelocity(lv.x * globalFriction, lv.y * globalFriction)
        }
    }

    private fun applyStabilizationForces(self: Body, rt: TopRuntime, dt: Double, mul: Double) {
        val p = self.transform.translation
        val dx = p.x
        val dy = p.y
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1e-9)
        val nx = dx / dist
        val ny = dy / dist
        val radius = (self.fixtures.firstOrNull()?.shape as? Circle)?.radius ?: 0.12
        val inCenterDisableZone = dist < radius * 1.2

        // Higher balanceFactor => steadier stabilization; stamina also affects how well the driver "holds a line".
        val stam01 = (rt.currentStamina / rt.maxStamina.coerceAtLeast(1e-9)).coerceIn(0.0, 1.0)
        val balance = rt.balanceFactor.coerceIn(0.65, 1.35)
        val strength = (mul * (0.82 + 0.18 * stam01) * (0.85 + 0.20 * (balance - 0.65) / 0.70)).coerceIn(0.15, 1.35)

        when (rt.stabilization) {
            StabilizationLevel.CENTER -> {
                // Pull toward center + small deterministic drift (wobble).
                val centerPull = dist * 2.05 * strength
                val v = self.linearVelocity
                var vx = v.x - nx * centerPull * dt
                var vy = v.y - ny * centerPull * dt

                // More balanced tops wobble less.
                val drift = (0.22 * strength * (1.15 - 0.35 * (balance - 0.65) / 0.70)).coerceAtLeast(0.05)
                val rx = pseudoRandom((rt.id.toInt() xor tickCount.toInt()), 7) - 0.5
                val ry = pseudoRandom((rt.id.toInt() xor tickCount.toInt()), 9) - 0.5
                vx += rx * drift * dt
                vy += ry * drift * dt

                // Mild settling friction.
                self.setLinearVelocity(vx * 0.98, vy * 0.98)
            }

            StabilizationLevel.INNER_RING, StabilizationLevel.OUTER_RING -> {
                if (inCenterDisableZone) return
                val ringStrength = strength * ringStabilizationForceMul
                val rawTargetR = when (rt.stabilization) {
                    StabilizationLevel.INNER_RING -> innerRingRadius
                    StabilizationLevel.OUTER_RING -> outerRingRadius
                    else -> innerRingRadius
                }
                // Keep groove safely away from the hard rim for large tops; otherwise OUTER_RING degenerates
                // into repeated wall bounces (looks like "stuck to border" instead of circular trajectory).
                val maxCenterR = (arenaRadius - radius).coerceAtLeast(0.10)
                // Match (and exceed) the rim "shell" width used by enforceArenaBounce to avoid entering its brake zone.
                val rimShell = (radius * 0.08).coerceAtMost(arenaRadius * 0.04).coerceAtLeast(0.02)
                val rimClearance =
                    maxOf(
                        (radius * 0.45),
                        (rimShell * 2.5),
                        (arenaRadius * 0.03),
                        0.02,
                    )
                val targetR = rawTargetR.coerceAtMost((maxCenterR - rimClearance).coerceAtLeast(0.12))

                val radiusDiff = targetR - dist
                // If we overshoot outward past the groove, pull back harder to prevent rim grazing.
                val overshoot = (dist - targetR).coerceAtLeast(0.0)
                val overshootMul = (1.0 + (overshoot / rimClearance.coerceAtLeast(1e-6)).coerceIn(0.0, 1.5) * 0.85)
                val radialPull = radiusDiff * 1.35 * ringStrength * overshootMul

                val v = self.linearVelocity
                var vx = v.x + nx * radialPull * dt
                var vy = v.y + ny * radialPull * dt

                val tangentialX = -ny * rt.orbitDirection.toDouble()
                val tangentialY = nx * rt.orbitDirection.toDouble()
                val currentTangentialVelocity = vx * tangentialX + vy * tangentialY

                // Orbit controller: kill radial drift, preserve tangential motion.
                // This is what makes the stabilized path a clean circle around arena center.
                val currentRadialVelocity = vx * nx + vy * ny
                // Ring motion: target cruise speed unchanged; corrective accelerations use [ringStrength].
                val desiredOrbitSpeed =
                    (1.40 + 0.65 * strength) * (0.92 + 0.12 * (balance - 0.65) / 0.70)
                val speedSq = vx * vx + vy * vy
                val tangentialSpeedSq = (speedSq - currentRadialVelocity * currentRadialVelocity).coerceAtLeast(0.0)
                val tangentialSpeed = sqrt(tangentialSpeedSq)
                // At high tangential speed, radial error grows quickly — scale groove damping up (not using total v² for centripetal).
                val highSpeedMul =
                    (1.0 + (tangentialSpeed / (desiredOrbitSpeed * 1.85 + 1e-6)).coerceIn(0.0, 2.8) * 0.42)
                val radialDamp =
                    when (rt.stabilization) {
                        StabilizationLevel.OUTER_RING -> 9.5
                        else -> 7.0
                    } * ringStrength * highSpeedMul
                vx -= nx * currentRadialVelocity * radialDamp * dt
                vy -= ny * currentRadialVelocity * radialDamp * dt

                val orbitAccel =
                    (0.95 * ringStrength) * (1.08 - 0.18 * (balance - 0.65) / 0.70) * highSpeedMul

                if (currentTangentialVelocity < desiredOrbitSpeed * 0.9) {
                    vx += tangentialX * orbitAccel * dt
                    vy += tangentialY * orbitAccel * dt
                } else if (currentTangentialVelocity > desiredOrbitSpeed * 1.1) {
                    vx -= tangentialX * orbitAccel * 0.5 * dt
                    vy -= tangentialY * orbitAccel * 0.5 * dt
                }

                // Centripetal correction: tangential speed only, from velocity *after* groove + tangent updates.
                if (dist > 0.05) {
                    val vRad = vx * nx + vy * ny
                    val tanSq = (vx * vx + vy * vy - vRad * vRad).coerceAtLeast(0.0)
                    val centripetal = tanSq / dist
                    // Balanced tops need less centripetal damping to avoid "wobble braking".
                    val k = (0.12 * ringStrength) * (1.10 - 0.22 * (balance - 0.65) / 0.70)
                    vx -= nx * centripetal * k * dt
                    vy -= ny * centripetal * k * dt
                }

                self.setLinearVelocity(vx, vy)
            }
        }
    }

    private fun emitImpactSparks(x: Double, y: Double, intensity: Float) {
        if (effects.size >= 40) return
        val it = intensity.coerceIn(0.1f, 8.0f)
        effects += CollisionEffect(x = x.toFloat(), y = y.toFloat(), intensity = it, age = 0f)
    }

    private fun emitAttackTrail(x: Double, y: Double, intensity: Float) {
        if (effects.size >= 40) return
        effects += CollisionEffect(x = x.toFloat(), y = y.toFloat(), intensity = intensity.coerceIn(0.1f, 2.0f), age = 0f)
    }

    private fun tickEffects(dt: Float) {
        if (effects.isNotEmpty()) {
            var w = 0
            for (i in 0 until effects.size) {
                val e = effects[i]
                val age = e.age + dt
                if (age < 0.35f) {
                    effects[w] = e.copy(age = age)
                    w++
                }
            }
            if (w < effects.size) effects.subList(w, effects.size).clear()
        }
        if (impacts.isNotEmpty()) {
            var w = 0
            for (i in 0 until impacts.size) {
                val im = impacts[i]
                val age = im.age + dt
                if (age < 0.75f) {
                    impacts[w] = im.copy(age = age)
                    w++
                }
            }
            if (w < impacts.size) impacts.subList(w, impacts.size).clear()
        }
    }

    private fun pairKey(a: Long, b: Long): Long {
        val lo = if (a < b) a else b
        val hi = if (a < b) b else a
        return (lo shl 32) xor (hi and 0xffffffffL)
    }

    private fun syncFromRuntime() {
        val pr = playerBody.userData as? TopRuntime
        val er = enemyBody.userData as? TopRuntime
        if (pr != null) {
            playerHp = pr.currentHp.coerceIn(0.0, pr.maxHp)
            playerStam = pr.currentStamina.coerceIn(0.0, pr.maxStamina)
        }
        if (er != null) {
            enemyHp = er.currentHp.coerceIn(0.0, er.maxHp)
            enemyStam = er.currentStamina.coerceIn(0.0, er.maxStamina)
        }
    }

    private fun killTop(body: Body) {
        body.setLinearVelocity(0.0, 0.0)
        body.setAngularVelocity(0.0)
        body.setEnabled(false)
        // Burst particle effect TODO: hook when you add effects back to snapshot
    }

}
