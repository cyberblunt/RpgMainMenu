package com.zerotoler.rpgmenu.domain.model.battle

import com.zerotoler.rpgmenu.domain.engine.Vec2

/**
 * Locked launch parameters after the player releases aim.
 */
data class LaunchInput(
    val direction: Vec2,
    val powerCurve01: Float,
    val linearSpeed: Float,
    val angularSpeed: Float,
    val stabilityBonus: Float,
)
