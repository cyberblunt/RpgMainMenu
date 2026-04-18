package com.zerotoler.rpgmenu.domain.model

data class Loadout(
    val teamId: String,
    val slotIndex: Int,
    val name: String?,
    val battleCapId: String?,
    val weightRingId: String?,
    val driverId: String?,
)
