package com.zerotoler.rpgmenu.domain.model

data class VisibleInventoryItem(
    val part: PartBase,
    val player: PlayerPartState,
    val isEquippedInActiveLoadout: Boolean,
)
