package com.zerotoler.rpgmenu.domain.engine

object SuperMeterResolver {
    private const val MAX = 100f
    private const val DAMAGE_TO_METER = 0.85f

    fun addFromDamageDealt(current: Float, damageDealt: Float): Float =
        (current + damageDealt * DAMAGE_TO_METER).coerceIn(0f, MAX)

    fun tickAbilityActive(remainingSec: Float, dt: Float): Pair<Boolean, Float> {
        if (remainingSec <= 0f) return false to 0f
        val next = remainingSec - dt
        return if (next <= 0f) false to 0f else true to next
    }
}
