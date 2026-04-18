package com.zerotoler.rpgmenu.data.content

data class TalentNodeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val costGold: Long,
    val prerequisiteIds: Set<String>,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val staminaBonus: Int = 0,
    val hpBonus: Int = 0,
)

object TalentCatalog {
    val nodes: List<TalentNodeDefinition> = listOf(
        TalentNodeDefinition(
            id = "core_1",
            title = "Gyro Core I",
            description = "Stabilizes launch vectors.",
            costGold = 200L,
            prerequisiteIds = emptySet(),
            attackBonus = 5,
        ),
        TalentNodeDefinition(
            id = "core_2",
            title = "Gyro Core II",
            description = "Adds rotational inertia.",
            costGold = 400L,
            prerequisiteIds = setOf("core_1"),
            defenseBonus = 8,
        ),
        TalentNodeDefinition(
            id = "edge_1",
            title = "Edge Polish",
            description = "Sharper contact windows.",
            costGold = 350L,
            prerequisiteIds = emptySet(),
            attackBonus = 12,
        ),
        TalentNodeDefinition(
            id = "shell_1",
            title = "Shell Weave",
            description = "Reinforced outer layer.",
            costGold = 500L,
            prerequisiteIds = setOf("core_1"),
            hpBonus = 120,
        ),
        TalentNodeDefinition(
            id = "flux_1",
            title = "Flux Channel",
            description = "Improves skill energy gain.",
            costGold = 600L,
            prerequisiteIds = setOf("core_2", "edge_1"),
            staminaBonus = 15,
        ),
    )

    fun byId(id: String): TalentNodeDefinition? = nodes.find { it.id == id }
}
