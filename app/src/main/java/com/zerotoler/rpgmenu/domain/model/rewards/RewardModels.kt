package com.zerotoler.rpgmenu.domain.model.rewards

data class RewardBundle(
    val gold: Long = 0L,
    val gems: Int = 0,
    val chestKeys: Int = 0,
    val partIds: List<String> = emptyList(),
    val shardsByPartId: Map<String, Int> = emptyMap(),
    val battlePassXp: Int = 0,
)
