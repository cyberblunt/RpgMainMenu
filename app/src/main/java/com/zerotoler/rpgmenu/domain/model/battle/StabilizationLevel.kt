package com.zerotoler.rpgmenu.domain.model.battle

/**
 * Beyblade-style stabilization target for arena motion.
 *
 * - [CENTER]: stabilizes toward arena center.
 * - [INNER_RING]/[OUTER_RING]: stabilizes toward a circular groove radius.
 */
enum class StabilizationLevel {
    CENTER,
    INNER_RING,
    OUTER_RING,
}

