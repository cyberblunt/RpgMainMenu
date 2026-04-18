package com.zerotoler.rpgmenu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_parts")
data class CatalogPartEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val rarity: Int?,
    val combatType: String,
    val spinDirection: String,
    val ringClass: String?,
    val driverClass: String?,
    val tagsJson: String,
    val health: Int?,
    val attack: Int?,
    val defense: Int?,
    val stamina: Int?,
    val intervalSeconds: Float?,
    val weightGrams: Float?,
)
