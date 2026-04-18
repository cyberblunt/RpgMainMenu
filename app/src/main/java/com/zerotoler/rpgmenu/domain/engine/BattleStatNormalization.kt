package com.zerotoler.rpgmenu.domain.engine

import com.zerotoler.rpgmenu.domain.model.battle.BattleTopStats

/**
 * Temporary gameplay normalization to keep player/enemy in comparable conditions while tuning mechanics.
 * This does not affect navigation/session/result flow; it only adjusts derived numerical battle stats.
 */
object BattleStatNormalization {

    fun equalizeBothForDebug(player: BattleTopStats, enemy: BattleTopStats): Pair<BattleTopStats, BattleTopStats> {
        val avgRadius = ((player.radius + enemy.radius) * 0.5f).coerceAtLeast(0.001f)
        val avgMass = ((player.mass + enemy.mass) * 0.5f).coerceAtLeast(0.001f)

        val avgHp = ((player.maxHealth + enemy.maxHealth) * 0.5f).coerceAtLeast(1f)
        val avgStamina = ((player.maxStamina + enemy.maxStamina) * 0.5f).coerceAtLeast(1f)

        val avgAttack = ((player.attack + enemy.attack) * 0.5f).coerceAtLeast(0f)
        val avgDefense = ((player.defense + enemy.defense) * 0.5f).coerceAtLeast(0f)

        val avgStaminaEfficiency = ((player.staminaEfficiency + enemy.staminaEfficiency) * 0.5f).coerceIn(0.35f, 2.5f)
        val avgBalanceFactor = ((player.balanceFactor + enemy.balanceFactor) * 0.5f).coerceIn(0.35f, 2.5f)
        val avgWallGrip = ((player.wallGrip + enemy.wallGrip) * 0.5f).coerceIn(0.25f, 2.5f)
        val avgCollisionPower = ((player.collisionPower + enemy.collisionPower) * 0.5f).coerceIn(0.25f, 2.5f)

        val pNorm = player.copy(
            radius = avgRadius,
            mass = avgMass,
            maxHealth = avgHp,
            maxStamina = avgStamina,
            attack = avgAttack,
            defense = avgDefense,
            staminaEfficiency = avgStaminaEfficiency,
            balanceFactor = avgBalanceFactor,
            wallGrip = avgWallGrip,
            collisionPower = avgCollisionPower,
        )
        val eNorm = enemy.copy(
            radius = avgRadius,
            mass = avgMass,
            maxHealth = avgHp,
            maxStamina = avgStamina,
            attack = avgAttack,
            defense = avgDefense,
            staminaEfficiency = avgStaminaEfficiency,
            balanceFactor = avgBalanceFactor,
            wallGrip = avgWallGrip,
            collisionPower = avgCollisionPower,
        )

        return pNorm to eNorm
    }
}

