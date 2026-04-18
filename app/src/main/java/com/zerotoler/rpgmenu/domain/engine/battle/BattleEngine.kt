package com.zerotoler.rpgmenu.domain.engine.battle

import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.battle.EffectiveBattleStats
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

enum class BattlePhase {
    Launch,
    Active,
    Result,
}

data class LaunchInput(
    val angleRadians: Float,
    val power01: Float,
)

data class BattleConfig(
    val durationSeconds: Float = 90f,
    val arenaRadius: Float = 1f,
    val topRadius: Float = 0.12f,
    val playerHue: Float = 0.55f,
    val enemyHue: Float = 0.92f,
)

internal data class SimTop(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var hp: Float,
    val maxHp: Float,
    /** Spin stamina 0..1 — drives damage and visual rotation speed */
    var spin: Float,
    var angle: Float,
    var angVel: Float,
    var energy: Float,
    val stats: EffectiveBattleStats,
    val isPlayer: Boolean,
)

/**
 * Pure spin-top arena simulation. No Compose, no allocations on the hot path beyond floater compaction.
 */
class BattleEngine(
    private val config: BattleConfig,
    playerStats: EffectiveBattleStats,
    enemyStats: EffectiveBattleStats,
    private val random: Random = Random.Default,
) {
    var phase: BattlePhase = BattlePhase.Launch
        private set

    var timeLeft: Float = config.durationSeconds
        private set

    val totalTime: Float = config.durationSeconds

    internal val player: SimTop = SimTop(
        x = -0.32f,
        y = 0f,
        vx = 0f,
        vy = 0f,
        hp = playerStats.maxHp,
        maxHp = playerStats.maxHp,
        spin = 1f,
        angle = 0f,
        angVel = 14f,
        energy = 0f,
        stats = playerStats,
        isPlayer = true,
    )

    internal val enemy: SimTop = SimTop(
        x = 0.32f,
        y = 0f,
        vx = 0f,
        vy = 0f,
        hp = enemyStats.maxHp,
        maxHp = enemyStats.maxHp,
        spin = 1f,
        angle = 0f,
        angVel = 13f,
        energy = 0f,
        stats = enemyStats,
        isPlayer = false,
    )

    var playerWon: Boolean? = null
        private set

    var autoSkillsEnabled: Boolean = false

    var paused: Boolean = false
        private set

    private var playerHitFlash = 0f
    private var enemyHitFlash = 0f

    private var botSkillCooldown = 0f

    private val flX = FloatArray(MutableRenderSnapshot.MAX_FLOATERS)
    private val flY = FloatArray(MutableRenderSnapshot.MAX_FLOATERS)
    private val flTtl = FloatArray(MutableRenderSnapshot.MAX_FLOATERS)
    private var flCount = 0

    fun setPaused(value: Boolean) {
        paused = value
    }

    fun applyLaunch(input: LaunchInput) {
        if (phase != BattlePhase.Launch) return
        val power = input.power01.coerceIn(0.08f, 1f)
        val speed = 0.65f + power * 1.55f
        player.vx = cos(input.angleRadians) * speed
        player.vy = sin(input.angleRadians) * speed
        player.spin = (0.72f + power * 0.28f).coerceIn(0.55f, 1f)
        player.angVel = 10f + power * 22f * massFactor(player.stats.combatType)
        val ePower = (0.78f + random.nextFloat() * 0.2f) * power
        enemy.vx = -player.vx * (0.72f + random.nextFloat() * 0.12f)
        enemy.vy = -player.vy * (0.62f + random.nextFloat() * 0.15f)
        enemy.spin = (0.65f + ePower * 0.3f).coerceIn(0.5f, 1f)
        enemy.angVel = 9f + ePower * 18f * massFactor(enemy.stats.combatType)
        phase = BattlePhase.Active
    }

    /**
     * Single fixed-timestep integration. [playerSkillRequested] consumes one skill if energy allows.
     */
    fun fixedStep(dt: Float, playerSkillRequested: Boolean) {
        if (phase == BattlePhase.Result || phase == BattlePhase.Launch) return
        if (paused) {
            updateFloatersOnly(dt)
            return
        }

        timeLeft -= dt
        if (timeLeft <= 0f) {
            timeLeft = 0f
            resolveTimeout()
            return
        }

        botSkillCooldown = (botSkillCooldown - dt).coerceAtLeast(0f)

        applyBotSteering(enemy, player, dt)

        integrateTop(player, dt)
        integrateTop(enemy, dt)

        arenaBounce(player)
        arenaBounce(enemy)

        val dist = hypot((player.x - enemy.x).toDouble(), (player.y - enemy.y).toDouble()).toFloat()
        val touch = config.topRadius * 2.05f
        if (dist < touch && dist > 1e-4f) {
            resolveTopCollision(dt, dist)
        }

        regenEnergy(player, dt)
        regenEnergy(enemy, dt)

        decaySpin(player, dt)
        decaySpin(enemy, dt)

        chipDamage(player, enemy, dt)
        chipDamage(enemy, player, dt)

        val playerAuto = autoSkillsEnabled && player.energy >= 1f && random.nextFloat() < 0.11f
        if ((playerSkillRequested && player.energy >= 1f) || playerAuto) {
            fireSkill(player, enemy)
        }

        tickBotSkill(dt)

        val spinVis = (PI.toFloat() * 2f / 14f)
        player.angle += player.angVel * player.spin * dt * spinVis
        enemy.angle += enemy.angVel * enemy.spin * dt * spinVis

        playerHitFlash = (playerHitFlash - dt).coerceAtLeast(0f)
        enemyHitFlash = (enemyHitFlash - dt).coerceAtLeast(0f)

        updateFloaters(dt)

        if (player.hp <= 0f) {
            player.hp = 0f
            playerWon = false
            phase = BattlePhase.Result
        } else if (enemy.hp <= 0f) {
            enemy.hp = 0f
            playerWon = true
            phase = BattlePhase.Result
        }
    }

    fun toHudState(): BattleHudState =
        BattleHudState(
            phase = phase,
            timeLeft = timeLeft,
            totalTime = totalTime,
            playerHpRatio = (player.hp / player.maxHp).coerceIn(0f, 1f),
            enemyHpRatio = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f),
            playerEnergy = player.energy,
            enemyEnergy = enemy.energy,
            autoBattle = autoSkillsEnabled,
            paused = paused,
            playerWon = playerWon,
        )

    fun writeRender(out: MutableRenderSnapshot) {
        out.playerX = player.x
        out.playerY = player.y
        out.enemyX = enemy.x
        out.enemyY = enemy.y
        out.playerAngle = player.angle
        out.enemyAngle = enemy.angle
        out.playerSpin = player.spin.coerceIn(0f, 1f)
        out.enemySpin = enemy.spin.coerceIn(0f, 1f)
        out.playerHitFlash = playerHitFlash
        out.enemyHitFlash = enemyHitFlash
        val n = flCount.coerceAtMost(MutableRenderSnapshot.MAX_FLOATERS)
        out.floaterCount = n
        var i = 0
        while (i < n) {
            out.floaterX[i] = flX[i]
            out.floaterY[i] = flY[i]
            out.floaterAlpha[i] = (flTtl[i] / 0.75f).coerceIn(0f, 1f)
            i++
        }
    }

    private fun resolveTimeout() {
        playerWon = player.hp > enemy.hp
        phase = BattlePhase.Result
    }

    private fun integrateTop(top: SimTop, dt: Float) {
        val m = massFactor(top.stats.combatType)
        val spinMove = 0.22f + top.spin.coerceIn(0f, 1f) * 0.78f
        top.x += top.vx * dt * spinMove / m
        top.y += top.vy * dt * spinMove / m
        val drag = 0.04f + (1f - top.spin) * 0.06f
        top.vx *= (1f - drag * dt).coerceIn(0.85f, 0.999f)
        top.vy *= (1f - drag * dt).coerceIn(0.85f, 0.999f)
        subtleBank(top, dt)
    }

    private fun subtleBank(top: SimTop, dt: Float) {
        val r = hypot(top.x.toDouble(), top.y.toDouble()).toFloat()
        if (r < 0.01f) return
        val nx = -top.x / r
        val ny = -top.y / r
        val bias = when (top.stats.combatType) {
            CombatType.DEFENSE -> 0.08f
            CombatType.STAMINA -> 0.05f
            CombatType.ATTACK -> -0.03f
            else -> 0.02f
        }
        top.vx += nx * bias * dt
        top.vy += ny * bias * dt
    }

    private fun arenaBounce(top: SimTop) {
        val r = hypot(top.x.toDouble(), top.y.toDouble()).toFloat()
        val maxR = config.arenaRadius - config.topRadius
        if (r > maxR && r > 0.001f) {
            val nx = top.x / r
            val ny = top.y / r
            val dot = top.vx * nx + top.vy * ny
            if (dot > 0) {
                val rest = wallRestitution(top.stats.combatType)
                top.vx -= (1f + rest) * dot * nx
                top.vy -= (1f + rest) * dot * ny
            }
            top.x = nx * maxR
            top.y = ny * maxR
            top.spin = (top.spin - 0.035f * wallRestitution(top.stats.combatType)).coerceAtLeast(0.12f)
            top.angVel *= 0.96f
        }
    }

    private fun resolveTopCollision(dt: Float, dist: Float) {
        val dx = enemy.x - player.x
        val dy = enemy.y - player.y
        val len = dist.coerceAtLeast(1e-3f)
        val nx = dx / len
        val ny = dy / len
        val overlap = config.topRadius * 2.05f - dist
        if (overlap > 0) {
            val sep = overlap * 0.5f
            player.x -= nx * sep
            player.y -= ny * sep
            enemy.x += nx * sep
            enemy.y += ny * sep
        }

        val relVx = player.vx - enemy.vx
        val relVy = player.vy - enemy.vy
        val relAlong = relVx * nx + relVy * ny
        if (relAlong > 0) {
            val e = 0.88f
            val reduced = relAlong * e
            val mp = massFactor(player.stats.combatType)
            val me = massFactor(enemy.stats.combatType)
            val invSum = 1f / (mp + me)
            val impulse = reduced * invSum
            player.vx -= impulse * me * nx
            player.vy -= impulse * me * ny
            enemy.vx += impulse * mp * nx
            enemy.vy += impulse * mp * ny
        }

        val pAttack = player.stats.attack * (0.35f + player.spin * 0.65f)
        val eAttack = enemy.stats.attack * (0.35f + enemy.spin * 0.65f)
        val dmgToEnemy = ((pAttack - enemy.stats.defense * 0.32f) * dt * 22f).coerceAtLeast(0f)
        val dmgToPlayer = ((eAttack - player.stats.defense * 0.32f) * dt * 22f).coerceAtLeast(0f)

        if (dmgToEnemy > 0.35f) {
            enemy.hp -= dmgToEnemy
            enemyHitFlash = 0.14f
            spawnFloater(enemy.x, enemy.y)
        }
        if (dmgToPlayer > 0.35f) {
            player.hp -= dmgToPlayer
            playerHitFlash = 0.14f
            spawnFloater(player.x, player.y)
        }

        val clash = (player.spin + enemy.spin) * 0.02f
        player.spin = (player.spin - clash * 0.55f).coerceAtLeast(0.12f)
        enemy.spin = (enemy.spin - clash * 0.55f).coerceAtLeast(0.12f)
    }

    private fun regenEnergy(top: SimTop, dt: Float) {
        top.energy = (top.energy + top.stats.skillRegenPerSecond * dt * 0.01f).coerceIn(0f, 1f)
    }

    private fun decaySpin(top: SimTop, dt: Float) {
        val rate = spinDecayRate(top.stats.combatType) * (1.1f - top.stats.stamina * 0.002f).coerceIn(0.85f, 1.15f)
        top.spin = (top.spin - dt * rate).coerceIn(0.1f, 1f)
        top.angVel = (top.angVel - dt * 0.35f).coerceIn(4f, 40f)
    }

    private fun chipDamage(attacker: SimTop, target: SimTop, dt: Float) {
        if (attacker.spin < 0.28f) return
        val dist = hypot((attacker.x - target.x).toDouble(), (attacker.y - target.y).toDouble()).toFloat()
        if (dist > config.topRadius * 4.2f) return
        val chip = attacker.stats.attack * 0.028f * dt * attacker.spin
        if (chip <= 0f) return
        target.hp -= chip
    }

    private fun fireSkill(attacker: SimTop, target: SimTop) {
        attacker.energy = 0f
        val raw = attacker.stats.attack * 1.45f + attacker.stats.stamina * 0.55f
        val mitigated = raw - target.stats.defense * 0.38f
        val dmg = mitigated.coerceIn(38f, attacker.maxHp * 0.42f)
        target.hp -= dmg
        val push = 0.42f / massFactor(target.stats.combatType)
        val dx = target.x - attacker.x
        val dy = target.y - attacker.y
        val d = hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(0.02f)
        target.vx += dx / d * push
        target.vy += dy / d * push
        if (target.isPlayer) playerHitFlash = 0.2f else enemyHitFlash = 0.2f
        spawnFloater(target.x, target.y)
    }

    private fun tickBotSkill(dt: Float) {
        if (enemy.energy < 1f) return
        val threshold = when (enemy.stats.combatType) {
            CombatType.ATTACK -> 0.055f
            CombatType.DEFENSE -> 0.028f
            CombatType.STAMINA -> 0.032f
            else -> 0.04f
        }
        if (botSkillCooldown <= 0f && random.nextFloat() < threshold) {
            fireSkill(enemy, player)
            botSkillCooldown = 1.8f + random.nextFloat() * 1.2f
        }
    }

    private fun applyBotSteering(bot: SimTop, target: SimTop, dt: Float) {
        if (!bot.isPlayer) {
            val dx = target.x - bot.x
            val dy = target.y - bot.y
            val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(0.02f)
            val nx = dx / dist
            val ny = dy / dist
            val tx = -ny
            val ty = nx
            val radial = when (bot.stats.combatType) {
                CombatType.ATTACK -> 1.15f
                CombatType.DEFENSE -> -0.35f
                CombatType.STAMINA -> 0.25f
                else -> 0.45f
            }
            val tangent = when (bot.stats.combatType) {
                CombatType.STAMINA -> 0.85f
                CombatType.BALANCE -> 0.4f
                else -> 0.15f
            }
            val str = 0.95f * (0.4f + bot.spin * 0.6f) / massFactor(bot.stats.combatType)
            bot.vx += (nx * radial + tx * tangent) * str * dt
            bot.vy += (ny * radial + ty * tangent) * str * dt
        }
    }

    private fun spawnFloater(x: Float, y: Float) {
        if (flCount >= MutableRenderSnapshot.MAX_FLOATERS) {
            shiftFloatersLeft()
        }
        flX[flCount] = x
        flY[flCount] = y
        flTtl[flCount] = 0.75f
        flCount++
    }

    private fun shiftFloatersLeft() {
        if (flCount < 2) return
        for (i in 1 until flCount) {
            flX[i - 1] = flX[i]
            flY[i - 1] = flY[i]
            flTtl[i - 1] = flTtl[i]
        }
        flCount--
    }

    private fun updateFloaters(dt: Float) {
        var w = 0
        var r = 0
        while (r < flCount) {
            flTtl[r] -= dt
            if (flTtl[r] > 0f) {
                if (w != r) {
                    flX[w] = flX[r]
                    flY[w] = flY[r]
                    flTtl[w] = flTtl[r]
                }
                w++
            }
            r++
        }
        flCount = w
    }

    private fun updateFloatersOnly(dt: Float) = updateFloaters(dt)

    private fun massFactor(ct: CombatType): Float =
        when (ct) {
            CombatType.DEFENSE -> 1.38f
            CombatType.ATTACK -> 0.9f
            CombatType.STAMINA -> 1.05f
            CombatType.BALANCE -> 1f
            CombatType.UNKNOWN -> 1f
        }

    private fun spinDecayRate(ct: CombatType): Float =
        when (ct) {
            CombatType.STAMINA -> 0.0042f
            CombatType.DEFENSE -> 0.0058f
            CombatType.ATTACK -> 0.0095f
            CombatType.BALANCE -> 0.0068f
            CombatType.UNKNOWN -> 0.007f
        }

    private fun wallRestitution(ct: CombatType): Float =
        when (ct) {
            CombatType.DEFENSE -> 0.72f
            CombatType.ATTACK -> 0.95f
            else -> 0.85f
        }
}
