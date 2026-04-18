package com.zerotoler.rpgmenu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerotoler.rpgmenu.data.db.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE id = :id LIMIT 1")
    fun observeWallet(id: Int = WalletEntity.SINGLETON_ID): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet WHERE id = :id LIMIT 1")
    suspend fun getWallet(id: Int = WalletEntity.SINGLETON_ID): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WalletEntity)
}
