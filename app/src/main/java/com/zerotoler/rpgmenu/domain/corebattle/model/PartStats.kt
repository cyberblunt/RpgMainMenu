package com.zerotoler.rpgmenu.domain.corebattle.model

/**
 * Immutable part contribution to a top's totals.
 *
 * All values are intended to be designer-tunable and unit-agnostic.
 * The simulation uses them consistently (delta-time based), so they
 * behave the same regardless of frame rate.
 */
data class PartStats(
    val hp: Float,
    val rpm: Float,
    val attack: Float,
    val defense: Float,
    val weight: Float,
)

