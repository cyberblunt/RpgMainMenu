package com.zerotoler.rpgmenu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val gold: Long,
    val gems: Long,
    val chestKeys: Int,
    val championshipTickets: Int,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
