package com.zerotoler.rpgmenu.domain.model.battle

import com.zerotoler.rpgmenu.domain.model.DriverClass
import com.zerotoler.rpgmenu.domain.model.RingClass

/**
 * Beyblade-style stabilization target for arena motion.
 *
 * - [CENTER]: stabilizes toward arena center.
 * - [INNER_RING]/[OUTER_RING]: stabilizes toward a circular groove radius.
 *
 * Each top has **exactly one** level, determined only from driver + ring (see [fromDriverAndRing]).
 */
enum class StabilizationLevel {
    CENTER,
    INNER_RING,
    OUTER_RING,
    ;

    companion object {
        /**
         * Single stabilization mode for a top built from driver + weight ring (battle cap does not change this).
         *
         * Rules:
         * - **CENTRAL** driver → [CENTER] only (ring class is ignored).
         * - **CIRCLE** driver + (**INNER** or **BALANCED** ring) → [INNER_RING] only.
         * - **CIRCLE** driver + (**OUTER** or **IMBALANCED** ring) → [OUTER_RING] only.
         *
         * If the driver class is **UNKNOWN**, [CENTER] is used (safe fallback).
         * For **CIRCLE** + **UNKNOWN** ring, [OUTER_RING] is used (non-inner rings default to outer groove).
         */
        fun fromDriverAndRing(driverClass: DriverClass, ringClass: RingClass): StabilizationLevel =
            when (driverClass) {
                DriverClass.CENTRAL -> CENTER
                DriverClass.CIRCLE ->
                    if (ringClass == RingClass.INNER || ringClass == RingClass.BALANCED) {
                        INNER_RING
                    } else {
                        OUTER_RING
                    }
                else -> CENTER
            }
    }
}
