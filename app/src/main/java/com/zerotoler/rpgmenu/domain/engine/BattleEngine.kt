package com.zerotoler.rpgmenu.domain.engine

import android.graphics.Color as AndroidColor
import com.zerotoler.rpgmenu.domain.model.battle.BattleOutcome
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.domain.model.battle.BattleParticle
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.domain.model.battle.BattleTopStats
import com.zerotoler.rpgmenu.domain.model.battle.CollisionEffect
import com.zerotoler.rpgmenu.domain.model.battle.FloatingImpact
import com.zerotoler.rpgmenu.domain.model.battle.Particle
import com.zerotoler.rpgmenu.domain.model.battle.StabilizationLevel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import org.dyn4j.collision.CategoryFilter
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
    /** Normalized arena radius: base ring scale, then −5% visual/physics tuning. */
    private val arenaRadius = 1.1 * 3.5 / 3.0 * 0.95
    /** HTML reference arena radius (px); maps constants to this sim space without resizing tops. */
    private val htmlArenaRadius = 350.0
    private val scaleFactor = arenaRadius / htmlArenaRadius
    private val referenceHz = 60.0
    private val world: World<Body> = World()

    private val playerBody: Body
    private val enemyBody: Body

    /** Linear scale on [BattleTopStats.radius] (0.7× base, 5× boost, prior tweaks, then +15% diameter). */
    private val topRadiusScale = 0.7 * 5.0 * 0.75 * 0.7 * 1.15

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

    // --- JS-reference behavior constants (time-based, stable at 60Hz) ---
    private val attackDurationSec = 1.0
    /** Low friction, scaled per second for 60Hz reference (matches HTML-style drift). */
    private val globalFrictionPerSec = 0.998
    /** Strong friction exponent base when over speed cap (per-second at 60Hz). */
    private val overspeedFrictionPerSec = 0.94
    /** Speed cap baseline in sim units (HTML: 11.2 px/frame @60Hz, scaled to arena). */
    private val baseMaxSpeed = 11.2 * scaleFactor * referenceHz
    private val attackSpeedCapMul = 1.5
    private val attackBurstMul = 2.0
    /** Magnetism as acceleration (m/s²): starts ~5.14, ramps by ~7.2 over attack window (HTML-derived). */
    private val spinSpeedRef = 55.0 // "full spin" reference (rad/s)
    private val staminaDrainPerSecAtFullSpin = 2.8 // tuned: ~35s for maxStamina~100 at high spin
    /** Keep attack-level speed cap briefly after dash ends so linear clamp + stamina caps don't eat momentum. */
    private val postAttackSpeedCapRelaxSec = 0.85

    // --- Lightweight visual effects buffers (only allocate on events) ---
    private val effects = ArrayList<CollisionEffect>(32)
    private val impacts = ArrayList<FloatingImpact>(16)
    private val particles = ArrayList<Particle>(96)
    private val maxParticles = 120

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

    /** Optional hook for UI (e.g. extra Compose particles). */
    var onBattleCollision: ((worldX: Double, worldY: Double, relativeSpeed: Float, isAttackClash: Boolean) -> Unit)? =
        null

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

        val linearSpeed = lerp(0.5, 1.92, aimPower01)
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
        val elinear = lerp(0.45, 1.75, epow)
        val espin = lerp(14.0, 48.0, epow)
        enemyBody.applyImpulse(Vector2(edir.x * enemyBase.mass.toDouble() * elinear, edir.y * enemyBase.mass.toDouble() * elinear))
        enemyBody.setAngularVelocity(espin)
        seedMotionPeaksFromBody(enemyBody)

        (playerBody.userData as? TopRuntime)?.let { rt ->
            rt.spinSpeed = (abs(spin) / spinSpeedRef * 1.5).coerceIn(0.35, 2.4)
        }
        (enemyBody.userData as? TopRuntime)?.let { rt ->
            rt.spinSpeed = (abs(espin) / spinSpeedRef * 1.5).coerceIn(0.35, 2.4)
        }

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

        // Arcade steering + attacks apply as velocity changes *before* integration so Dyn4j does not
        // double-integrate the same impulses. Top–top overlap uses manual HTML-style resolution (no Dyn4j contact).
        manageAttacks(d)
        applyMagnetismAndStabilization(playerBody, enemyBody, d)
        applyMagnetismAndStabilization(enemyBody, playerBody, d)

        world.step(1)

        // Hard arena boundary bounce to prevent rim "gliding"
        enforceArenaBounce(playerBody, playerRadius())
        enforceArenaBounce(enemyBody, enemyRadius())
        applyManualTopContact()
        tickEffects(d.toFloat())
        drainStaminaFromSpin(d)
        updatePhysicsAndStamina(playerBody, d)
        updatePhysicsAndStamina(enemyBody, d)
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
        val particlesSnapshot =
            if (particles.isEmpty()) {
                emptyList()
            } else {
                particles.map {
                    BattleParticle(
                        it.x.toFloat(),
                        it.y.toFloat(),
                        min(1.0, it.life).toFloat(),
                        it.size.toFloat(),
                        it.color,
                    )
                }
            }

        val pr = playerBody.userData as? TopRuntime
        val er = enemyBody.userData as? TopRuntime
        val spin01P = ((pr?.spinSpeed ?: (pOmega / spinSpeedRef * 1.5)) / 1.5).toFloat().coerceIn(0f, 1.15f)
        val spin01E = ((er?.spinSpeed ?: (eOmega / spinSpeedRef * 1.5)) / 1.5).toFloat().coerceIn(0f, 1.15f)
        val pRpmMax = playerBase.maxStamina.toInt().coerceAtLeast(1)
        val eRpmMax = enemyBase.maxStamina.toInt().coerceAtLeast(1)
        val playerRpm = (spin01P * pRpmMax).roundToInt().coerceIn(0, pRpmMax + (pRpmMax * 0.15f).roundToInt())
        val enemyRpm = (spin01E * eRpmMax).roundToInt().coerceIn(0, eRpmMax + (eRpmMax * 0.15f).roundToInt())

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
            particles = particlesSnapshot,
            playerRpm = playerRpm,
            enemyRpm = enemyRpm,
            playerRpmMax = pRpmMax,
            enemyRpmMax = eRpmMax,
            playerAttacking = pr?.isAttacking == true,
            enemyAttacking = er?.isAttacking == true,
            playerAttack = playerBase.attack.roundToInt().coerceAtLeast(0),
            playerDefense = playerBase.defense.roundToInt().coerceAtLeast(0),
            playerWeightGrams = playerBase.weightGrams.roundToInt(),
            enemyAttack = enemyBase.attack.roundToInt().coerceAtLeast(0),
            enemyDefense = enemyBase.defense.roundToInt().coerceAtLeast(0),
            enemyWeightGrams = enemyBase.weightGrams.roundToInt(),
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

    /**
     * HTML-style overlap: separate centers, elastic bounce with [bounceForce], HP on cooldown, spin tradeoff.
     * Dyn4j top–top collision is disabled via [CategoryFilter]; this runs after [world.step].
     */
    private fun applyManualTopContact() {
        if (battlePhase != BattlePhase.ACTIVE) return
        val t1 = playerBody
        val t2 = enemyBody
        val rt1 = t1.userData as? TopRuntime ?: return
        val rt2 = t2.userData as? TopRuntime ?: return
        if (!t1.isEnabled || !t2.isEnabled) return

        val p1 = t1.transform.translation
        val p2 = t2.transform.translation
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1e-9)
        val r1 = playerRadius()
        val r2 = enemyRadius()
        val minDist = r1 + r2
        if (dist >= minDist) return

        val nx = dx / dist
        val ny = dy / dist

        val clashAttack = rt1.isAttacking || rt2.isAttacking
        cancelAttack(rt1)
        cancelAttack(rt2)

        val overlap = minDist - dist
        t1.translate(-nx * overlap / 2.0, -ny * overlap / 2.0)
        t2.translate(nx * overlap / 2.0, ny * overlap / 2.0)

        val v1 = t1.linearVelocity
        val v2 = t2.linearVelocity
        val kx = v1.x - v2.x
        val ky = v1.y - v2.y
        val relativeSpeed = sqrt(kx * kx + ky * ky)

        val m1 = t1.mass.mass.coerceAtLeast(0.05)
        val m2 = t2.mass.mass.coerceAtLeast(0.05)
        val p = 2.0 * (nx * kx + ny * ky) / (m1 + m2)
        val bounceForce = 1.05
        t1.setLinearVelocity(
            (v1.x - p * m2 * nx) * bounceForce,
            (v1.y - p * m2 * ny) * bounceForce,
        )
        t2.setLinearVelocity(
            (v2.x + p * m1 * nx) * bounceForce,
            (v2.y + p * m1 * ny) * bounceForce,
        )

        val impactSpeed = relativeSpeed / (scaleFactor * referenceHz).coerceAtLeast(1e-6)
        createSparks(
            p1.x + nx * r1,
            p1.y + ny * r1,
            AndroidColor.argb(255, 255, 255, 255),
            impactSpeed * 0.8 + 0.5,
        )

        if (relativeSpeed >= 0.02) {
            val nowMs = System.currentTimeMillis()
            val key = pairKey(rt1.id, rt2.id)
            val last = lastDamageMsByPair[key] ?: Long.MIN_VALUE
            if (nowMs - last >= damageCooldownMs) {
                lastDamageMsByPair[key] = nowMs
                val baseDamage = relativeSpeed * 100.0
                val def1 = rt1.defenseStat.coerceAtLeast(1.0)
                val def2 = rt2.defenseStat.coerceAtLeast(1.0)
                rt1.currentHp = (rt1.currentHp - baseDamage * (rt2.attackStat / def1)).coerceAtLeast(0.0)
                rt2.currentHp = (rt2.currentHp - baseDamage * (rt1.attackStat / def2)).coerceAtLeast(0.0)
            }
        }

        if (rt1.spinSpeed > rt2.spinSpeed) {
            rt2.spinSpeed = (rt2.spinSpeed - 0.05).coerceAtLeast(0.0)
        } else {
            rt1.spinSpeed = (rt1.spinSpeed - 0.05).coerceAtLeast(0.0)
        }

        onBattleCollision?.invoke(
            p1.x + nx * r1,
            p1.y + ny * r1,
            relativeSpeed.toFloat().coerceIn(0.1f, 8f),
            clashAttack,
        )

        syncFromRuntime()
        if (rt1.currentHp <= 0.0) killTop(t1)
        if (rt2.currentHp <= 0.0) killTop(t2)
    }

    private fun updatePhysicsAndStamina(body: Body, dt: Double) {
        if (!body.isEnabled) return
        val rt = body.userData as? TopRuntime ?: return
        val baseMaxAllowed = baseMaxSpeed * (rt.spinSpeed / 1.5)
        var maxAllowedSpeed = baseMaxAllowed.coerceAtLeast(0.12)
        if (rt.isAttacking) maxAllowedSpeed *= attackSpeedCapMul
        if (simTimeSec < rt.linearSpeedCapBoostUntilSec) {
            maxAllowedSpeed = maxOf(maxAllowedSpeed, baseMaxAllowed * attackSpeedCapMul)
        }

        val vel = body.linearVelocity
        val sp = sqrt(vel.x * vel.x + vel.y * vel.y)
        val inRingGroove = isRingGrooveStabilizing(body)
        if (sp > maxAllowedSpeed && maxAllowedSpeed > 0.1) {
            body.setLinearVelocity(
                vel.x * overspeedFrictionPerSec.pow(dt * referenceHz),
                vel.y * overspeedFrictionPerSec.pow(dt * referenceHz),
            )
        } else if (!inRingGroove) {
            body.setLinearVelocity(
                vel.x * globalFrictionPerSec.pow(dt * referenceHz),
                vel.y * globalFrictionPerSec.pow(dt * referenceHz),
            )
        }
        val vel2 = body.linearVelocity
        val sp2 = sqrt(vel2.x * vel2.x + vel2.y * vel2.y)
        if (sp2 > maxAllowedSpeed && maxAllowedSpeed > 1e-6) {
            val k = maxAllowedSpeed / sp2
            body.setLinearVelocity(vel2.x * k, vel2.y * k)
        }

        rt.spinSpeed -= 0.009 * dt
        if (rt.spinSpeed <= 0.0) {
            rt.spinSpeed = 0.0
            val pos = body.transform.translation
            val c =
                if (body === playerBody) {
                    AndroidColor.argb(255, 0, 229, 255)
                } else {
                    AndroidColor.argb(255, 224, 64, 251)
                }
            createSparks(pos.x, pos.y, c, 2.0)
            killTop(body)
        }
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

    /** True when inner/outer ring stabilization is actively steering (not in the center pocket). */
    private fun isRingGrooveStabilizing(body: Body): Boolean {
        val rt = body.userData as? TopRuntime ?: return false
        if (rt.stabilization != StabilizationLevel.INNER_RING && rt.stabilization != StabilizationLevel.OUTER_RING) {
            return false
        }
        val p = body.transform.translation
        val distCenter = sqrt(p.x * p.x + p.y * p.y).coerceAtLeast(1e-12)
        val topRadius = (body.fixtures.firstOrNull()?.shape as? Circle)?.radius ?: return false
        if (distCenter < topRadius * 1.2) return false
        if (distCenter <= 0.1 * scaleFactor) return false
        return true
    }

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

        val e = 1.05
        var vx = body.linearVelocity.x
        var vy = body.linearVelocity.y
        val vn2 = vx * nx + vy * ny

        if (vn2 > 0.0) {
            vx -= (1.0 + e) * vn2 * nx
            vy -= (1.0 + e) * vn2 * ny
            if (pastRing) {
                val vnAfter = vx * nx + vy * ny
                val tx = -ny
                val ty = nx
                val vt = vx * tx + vy * ty
                val grip = (rt?.wallGrip ?: 1.0).coerceIn(0.75, 1.35)
                val massN = (rt?.mass ?: 1.0).coerceIn(0.65, 2.1)
                val tangentialRetain =
                    (0.55 + 0.12 * (grip - 0.75) / 0.60 + 0.05 * (massN - 0.65) / 1.45).coerceIn(0.45, 0.72)
                vx = nx * vnAfter + tx * vt * tangentialRetain
                vy = ny * vnAfter + ty * vt * tangentialRetain
            }
            body.setLinearVelocity(vx, vy)
            if (pastRing && rt != null) {
                rt.spinSpeed = (rt.spinSpeed - 0.005).coerceAtLeast(0.0)
                val tp = body.transform.translation
                createSparks(tp.x + nx * radius, tp.y + ny * radius, AndroidColor.argb(255, 0, 255, 255), 1.5)
            }
        } else if (pastRing) {
            body.setLinearVelocity(vx, vy)
        }

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
            this.restitution = 1.05
            // category=1, mask=2 → no top–top Dyn4j contact; overlaps resolved in [applyManualTopContact].
            this.filter = CategoryFilter(1, 2)
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
            attackStartTimeMs = 0L,
            attackTargetBody = null,
            spinSpeed = 1.5,
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
        /** Seconds until next attack attempt (HTML: 6..10s). */
        var attackTimerSec: Double,
        var attackStartTimeMs: Long,
        /** Dash target (1v1); null when not attacking. */
        var attackTargetBody: Body?,
        /** Arcade spin scalar (HTML-style decay via [updatePhysicsAndStamina]). */
        var spinSpeed: Double,
        /** Peak linear/angular speeds for stamina coupling (updated every frame while enabled). */
        var peakLinearSpeed: Double,
        var peakAngularSpeed: Double,
        /** Sim-time until which spin-based speed cap stays at post-attack headroom (see [postAttackSpeedCapRelaxSec]). */
        var linearSpeedCapBoostUntilSec: Double,
    )

    private fun initialAttackTimerSec(idHash: Int): Double =
        6.0 + pseudoRandom(sessionSeed xor idHash, 97) * 4.0

    private fun nextAttackTimerSec(rt: TopRuntime, salt: Int): Double =
        6.0 + pseudoRandom(sessionSeed xor rt.id.toInt(), salt) * 4.0

    private fun manageAttacks(dt: Double) {
        if (battlePhase != BattlePhase.ACTIVE) return
        val pr = playerBody.userData as? TopRuntime ?: return
        val er = enemyBody.userData as? TopRuntime ?: return
        val pAlive = playerBody.isEnabled
        val eAlive = enemyBody.isEnabled
        if (!pAlive || !eAlive) {
            cancelAttack(pr)
            cancelAttack(er)
            return
        }

        fun endAttackIfExpired(rt: TopRuntime) {
            if (!rt.isAttacking) return
            val elapsed = (System.currentTimeMillis() - rt.attackStartTimeMs) / 1000.0
            if (elapsed >= attackDurationSec) {
                rt.isAttacking = false
                rt.attackTargetBody = null
                rt.attackStartTimeMs = 0L
                rt.linearSpeedCapBoostUntilSec = simTimeSec + postAttackSpeedCapRelaxSec
            }
        }
        endAttackIfExpired(pr)
        endAttackIfExpired(er)

        val roundOver = pr.hasAttackedThisRound && !pr.isAttacking && er.hasAttackedThisRound && !er.isAttacking
        if (roundOver) {
            pr.hasAttackedThisRound = false
            er.hasAttackedThisRound = false
            pr.attackTimerSec = nextAttackTimerSec(pr, salt = 101)
            er.attackTimerSec = nextAttackTimerSec(er, salt = 103)
            return
        }

        if (!pr.isAttacking && !pr.hasAttackedThisRound) {
            pr.attackTimerSec -= dt
            if (pr.attackTimerSec <= 0.0 && enemyBody.isEnabled) {
                pr.isAttacking = true
                pr.hasAttackedThisRound = true
                pr.attackStartTimeMs = System.currentTimeMillis()
                pr.attackTargetBody = enemyBody
                val v = playerBody.linearVelocity
                playerBody.setLinearVelocity(v.x * attackBurstMul, v.y * attackBurstMul)
                emitAttackTrail(playerBody.transform.translation.x, playerBody.transform.translation.y, intensity = 0.9f)
                emitParticleBurst(
                    x = playerBody.transform.translation.x,
                    y = playerBody.transform.translation.y,
                    colorArgb = AndroidColor.argb(230, 255, 255, 255),
                    count = 18,
                    speedMul = 1.35,
                    salt = 4200,
                )
            }
        }
        if (!er.isAttacking && !er.hasAttackedThisRound) {
            er.attackTimerSec -= dt
            if (er.attackTimerSec <= 0.0 && playerBody.isEnabled) {
                er.isAttacking = true
                er.hasAttackedThisRound = true
                er.attackStartTimeMs = System.currentTimeMillis()
                er.attackTargetBody = playerBody
                val v = enemyBody.linearVelocity
                enemyBody.setLinearVelocity(v.x * attackBurstMul, v.y * attackBurstMul)
                emitAttackTrail(enemyBody.transform.translation.x, enemyBody.transform.translation.y, intensity = 0.9f)
                emitParticleBurst(
                    x = enemyBody.transform.translation.x,
                    y = enemyBody.transform.translation.y,
                    colorArgb = AndroidColor.argb(230, 255, 255, 255),
                    count = 18,
                    speedMul = 1.35,
                    salt = 4201,
                )
            }
        }
    }

    private fun cancelAttack(rt: TopRuntime) {
        val wasAttacking = rt.isAttacking
        rt.isAttacking = false
        rt.attackTargetBody = null
        rt.attackStartTimeMs = 0L
        if (wasAttacking) {
            rt.linearSpeedCapBoostUntilSec = simTimeSec + postAttackSpeedCapRelaxSec
        }
    }

    private fun applyMagnetismAndStabilization(self: Body, enemy: Body, dt: Double) {
        if (!self.isEnabled) return
        val rt = self.userData as? TopRuntime ?: return
        val et = enemy.userData as? TopRuntime

        val targetBody = rt.attackTargetBody
        if (rt.isAttacking && enemy.isEnabled && et != null && targetBody != null && targetBody.isEnabled) {
            val p = self.transform.translation
            val q = targetBody.transform.translation
            val dxT = q.x - p.x
            val dyT = q.y - p.y
            val distT = sqrt(dxT * dxT + dyT * dyT).coerceAtLeast(1e-9)
            val nxT = dxT / distT
            val nyT = dyT / distT
            val elapsedSecs = (System.currentTimeMillis() - rt.attackStartTimeMs) / 1000.0
            val progress = (elapsedSecs / attackDurationSec).coerceIn(0.0, 1.0)
            val magnetismForceHtml = 0.5 + progress * 0.7
            // HTML adds this per frame at 60Hz; continuous acceleration = delta-per-frame * 3600.
            val accelMagnetism = magnetismForceHtml * scaleFactor * 3600.0
            val v = self.linearVelocity
            self.setLinearVelocity(
                v.x + nxT * accelMagnetism * dt,
                v.y + nyT * accelMagnetism * dt,
            )
            createSparks(p.x, p.y, AndroidColor.argb(255, 255, 69, 0), 0.5)
        }

        val stabMul = if (rt.isAttacking) 0.25 else 1.0
        applyStabilizationForces(self = self, rt = rt, dt = dt, mul = stabMul)
    }

    private fun applyStabilizationForces(self: Body, rt: TopRuntime, dt: Double, mul: Double) {
        val p = self.transform.translation
        val distCenter = sqrt(p.x * p.x + p.y * p.y).coerceAtLeast(1e-12)
        val nx = p.x / distCenter
        val ny = p.y / distCenter
        val topRadius = (self.fixtures.firstOrNull()?.shape as? Circle)?.radius ?: 0.12
        var vx = self.linearVelocity.x
        var vy = self.linearVelocity.y

        when (rt.stabilization) {
            StabilizationLevel.CENTER -> {
                if (distCenter > 0.1 * scaleFactor) {
                    // HTML: -= nx * dist * 0.001 per frame → accel = 0.001 * 3600 * dist = 3.6 * dist
                    val accelCenter = distCenter * 3.6 * mul
                    vx -= nx * accelCenter * dt
                    vy -= ny * accelCenter * dt
                    // HTML: += (rand-0.5) * 0.05 * scale per frame → 0.05 * 3600 = 180
                    val driftAccel = 180.0 * scaleFactor * mul
                    vx += (pseudoRandom(rt.id.toInt() xor tickCount.toInt(), 17) - 0.5) * driftAccel * dt
                    vy += (pseudoRandom(rt.id.toInt() xor tickCount.toInt(), 23) - 0.5) * driftAccel * dt
                    val fr = 0.98.pow(dt * referenceHz)
                    self.setLinearVelocity(vx * fr, vy * fr)
                }
            }

            StabilizationLevel.INNER_RING, StabilizationLevel.OUTER_RING -> {
                val centerAreaRadius = topRadius * 1.2
                if (distCenter >= centerAreaRadius && distCenter > 0.1 * scaleFactor) {
                    val targetLevel =
                        if (rt.stabilization == StabilizationLevel.INNER_RING) {
                            130.0 * scaleFactor
                        } else {
                            260.0 * scaleFactor
                        }
                    // HTML radial term * 3600: 0.0004 * 3600 = 1.44
                    val accelRadial = (targetLevel - distCenter) * 1.44 * mul
                    vx += nx * accelRadial * dt
                    vy += ny * accelRadial * dt

                    val desiredOrbitSpeed = 7.0 * scaleFactor * referenceHz
                    val tangentialX = -ny * rt.orbitDirection.toDouble()
                    val tangentialY = nx * rt.orbitDirection.toDouble()
                    val currentTangential = vx * tangentialX + vy * tangentialY
                    // HTML 0.1 per frame → 0.1 * 3600 = 360
                    val accelOrbit = 360.0 * scaleFactor * mul
                    if (currentTangential < desiredOrbitSpeed * 0.9) {
                        vx += tangentialX * accelOrbit * dt
                        vy += tangentialY * accelOrbit * dt
                    } else if (currentTangential > desiredOrbitSpeed * 1.1) {
                        vx -= tangentialX * accelOrbit * 0.5 * dt
                        vy -= tangentialY * accelOrbit * 0.5 * dt
                    }

                    val currentSpeed = sqrt(vx * vx + vy * vy)
                    val accelCentripetal = (currentSpeed * currentSpeed) / distCenter
                    vx -= nx * accelCentripetal * mul * dt
                    vy -= ny * accelCentripetal * mul * dt
                    self.setLinearVelocity(vx, vy)
                }
            }
        }
    }

    private fun createSparks(px: Double, py: Double, colorArgb: Int, intensity: Double) {
        val count = ((pseudoRandom(sessionSeed xor tickCount.toInt(), 3101) * 15.0 + 15.0) * intensity)
            .toInt()
            .coerceIn(1, 55)
        repeat(count) { i ->
            if (particles.size >= maxParticles) return
            val u1 = pseudoRandom(sessionSeed xor tickCount.toInt() xor i, 3102 + i)
            val u2 = pseudoRandom(sessionSeed xor tickCount.toInt() xor i, 4102 + i)
            val u3 = pseudoRandom(sessionSeed xor tickCount.toInt() xor i, 5102 + i)
            val angle = u1 * PI * 2.0
            val speed = u2 * 5.0 * intensity * scaleFactor * referenceHz
            val decay = u3 * 0.04 + 0.01
            val sz = (pseudoRandom(sessionSeed, 6102 + i) * 4.0 + 1.0) * scaleFactor
            particles.add(
                Particle(
                    x = px,
                    y = py,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 1.0,
                    decay = decay,
                    size = sz,
                    color = colorArgb,
                ),
            )
        }
    }

    private fun emitParticleBurst(
        x: Double,
        y: Double,
        colorArgb: Int,
        count: Int,
        speedMul: Double,
        salt: Int,
    ) {
        val c = count.coerceIn(0, 28)
        repeat(c) { i ->
            if (particles.size >= maxParticles) return
            val u1 = pseudoRandom(sessionSeed xor tickCount.toInt(), salt + i * 5)
            val u2 = pseudoRandom(sessionSeed xor tickCount.toInt(), salt + i * 5 + 1)
            val ang = u1 * PI * 2.0
            val speed = (0.018 + u2 * 0.09) * speedMul * scaleFactor * referenceHz * 0.35
            val decay = (0.008 + pseudoRandom(sessionSeed, salt + i + 400) * 0.04).coerceIn(0.006, 0.055)
            val sz = (0.006 + pseudoRandom(sessionSeed, salt + i + 800) * 0.014).coerceIn(0.004, 0.028)
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(ang) * speed,
                    vy = sin(ang) * speed,
                    life = 0.85 + pseudoRandom(sessionSeed, salt + i + 900) * 0.35,
                    decay = decay,
                    size = sz,
                    color = colorArgb,
                ),
            )
        }
    }

    private fun emitImpactSparks(x: Double, y: Double, intensity: Float) {
        val it = intensity.coerceIn(0.1f, 8.0f)
        if (effects.size < 40) {
            effects += CollisionEffect(x = x.toFloat(), y = y.toFloat(), intensity = it, age = 0f)
        }
        createSparks(
            x,
            y,
            AndroidColor.argb(220, 255, 255, 255),
            (0.35 + it * 0.18).toDouble().coerceIn(0.2, 2.4),
        )
    }

    private fun emitAttackTrail(x: Double, y: Double, intensity: Float) {
        if (effects.size >= 40) return
        effects += CollisionEffect(x = x.toFloat(), y = y.toFloat(), intensity = intensity.coerceIn(0.1f, 2.0f), age = 0f)
    }

    /**
     * [Particle.vx]/[Particle.vy] are sim units per second. Decay matches HTML “per frame” fade at 60Hz.
     */
    private fun updateParticles(dt: Double) {
        if (particles.isEmpty()) return
        val frames = dt * referenceHz
        var w = 0
        for (i in particles.indices) {
            val p = particles[i]
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= p.decay * frames
            if (p.life > 0.0) {
                if (w != i) particles[w] = p
                w++
            }
        }
        if (w < particles.size) particles.subList(w, particles.size).clear()
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
        updateParticles(dt.toDouble())
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
        val rt = body.userData as? TopRuntime
        if (rt != null) {
            rt.currentHp = 0.0
            rt.currentStamina = 0.0
        }
        syncFromRuntime()
        body.setLinearVelocity(0.0, 0.0)
        body.setAngularVelocity(0.0)
        body.setEnabled(false)
    }

}
