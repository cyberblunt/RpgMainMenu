package com.zerotoler.rpgmenu.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "loadouts",
    primaryKeys = ["teamId", "slotIndex"],
)
data class LoadoutEntity(
    val teamId: String,
    val slotIndex: Int,
    val name: String?,
    val battleCapId: String?,
    val weightRingId: String?,
    val driverId: String?,
)
