package com.zerotoler.rpgmenu.domain.model.battle

import com.zerotoler.rpgmenu.domain.engine.Vec2

/**
 * Mutable simulation-facing state for one top (mirrors fields advanced by [com.zerotoler.rpgmenu.domain.engine.BattleEngine]).
 */
data class BattleTopRuntimeState(
    val stats: BattleTopStats,
    val position: Vec2,
    val velocity: Vec2,
    var rotationAngle: Float,
    var angularSpeed: Float,
    var health: Float,
    var stamina: Float,
    var superMeter: Float,
    var activeAbilityState: Boolean,
)
