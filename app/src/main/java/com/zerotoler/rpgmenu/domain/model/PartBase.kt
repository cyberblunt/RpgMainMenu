package com.zerotoler.rpgmenu.domain.model

data class PartBase(
    val id: String,
    val name: String,
    val category: PartCategory,
    val rarity: Int?,
    val combatType: CombatType,
    val spinDirection: SpinDirection,
    val ringClass: RingClass?,
    val driverClass: DriverClass?,
    val tags: List<String>,
    val stats: PartStats,
)
