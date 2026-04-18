package com.zerotoler.rpgmenu.domain.model

data class PlayerPartState(
    val partId: String,
    val owned: Boolean,
    val level: Int,
    val shardCount: Int,
    val copies: Int,
    val isFavorite: Boolean,
    val obtainedAtEpochMillis: Long?,
)
