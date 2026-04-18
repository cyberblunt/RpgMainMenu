package com.zerotoler.rpgmenu.domain.engine

import com.zerotoler.rpgmenu.domain.model.battle.LaunchInput
import kotlin.math.abs

object LaunchResolver {

    fun oscillationToPowerCurve(normalized01: Float): Float {
        val n = normalized01.coerceIn(0f, 1f)
        return 1f - abs(2f * n - 1f)
    }

    fun buildLaunch(
        direction: Vec2,
        powerCurve01: Float,
        minLinear: Float,
        maxLinear: Float,
        minSpin: Float,
        maxSpin: Float,
    ): LaunchInput {
        val pc = powerCurve01.coerceIn(0f, 1f)
        val dir = Vec2(direction.x, direction.y).normalize()
        val lin = minLinear + pc * (maxLinear - minLinear)
        val spin = minSpin + pc * (maxSpin - minSpin)
        val stability = pc * 0.06f - (1f - pc) * 0.025f
        return LaunchInput(
            direction = dir,
            powerCurve01 = pc,
            linearSpeed = lin,
            angularSpeed = spin,
            stabilityBonus = stability,
        )
    }
}
