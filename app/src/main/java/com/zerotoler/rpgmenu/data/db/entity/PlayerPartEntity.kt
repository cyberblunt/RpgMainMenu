package com.zerotoler.rpgmenu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_parts")
data class PlayerPartEntity(
    @PrimaryKey val partId: String,
    val owned: Boolean,
    val level: Int,
    val shardCount: Int,
    val copies: Int,
    val isFavorite: Boolean,
    val obtainedAtEpochMillis: Long?,
)
