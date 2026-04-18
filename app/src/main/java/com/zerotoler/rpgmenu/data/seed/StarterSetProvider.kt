package com.zerotoler.rpgmenu.data.seed

/**
 * Central definition of the minimal owned set for [com.zerotoler.rpgmenu.domain.model.DebugSeedMode.STARTER_ONLY].
 * Matches a valid three-part build (cap + ring + driver).
 */
object StarterSetProvider {
    const val DEFAULT_TEAM_ID: String = "team_a"

    val ALL_TEAM_IDS: List<String> = listOf("team_a", "team_b", "team_c", "team_d")

    fun starterOwnedPartIds(): Set<String> =
        setOf(
            "cap_enchantress",
            "ring_apex_forge",
            "driver_burst",
        )
}
