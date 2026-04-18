package com.zerotoler.rpgmenu.data.content

data class CollectionSetDefinition(
    val id: String,
    val name: String,
    val requiredPartIds: Set<String>,
    val bonusDescription: String,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val staminaBonus: Int = 0,
)

object CollectionSetDefinitions {
    val sets: List<CollectionSetDefinition> = listOf(
        CollectionSetDefinition(
            id = "starter_synergy",
            name = "Starter Synergy",
            requiredPartIds = setOf(
                "cap_enchantress",
                "ring_apex_forge",
                "driver_burst",
            ),
            bonusDescription = "Balanced resonance for the default kit.",
            attackBonus = 10,
            defenseBonus = 10,
            hpBonus = 200,
        ),
        CollectionSetDefinition(
            id = "wiki_caps_trio",
            name = "Archive Trio",
            requiredPartIds = setOf("cap_aiolos", "cap_athena", "cap_thor"),
            bonusDescription = "Mythic caps echo each other.",
            staminaBonus = 20,
            attackBonus = 15,
        ),
    )
}
