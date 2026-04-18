package com.zerotoler.rpgmenu.domain.model.battle

data class CollisionEffect(
    val x: Float,
    val y: Float,
    val intensity: Float,
    val age: Float,
)

data class FloatingImpact(
    val x: Float,
    val y: Float,
    val amount: Int,
    val age: Float,
)
