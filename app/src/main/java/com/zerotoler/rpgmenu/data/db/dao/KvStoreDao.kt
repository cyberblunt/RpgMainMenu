package com.zerotoler.rpgmenu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerotoler.rpgmenu.data.db.entity.KvStoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KvStoreDao {
    @Query("SELECT * FROM kv_store WHERE key = :key LIMIT 1")
    fun observe(key: String): Flow<KvStoreEntity?>

    @Query("SELECT * FROM kv_store WHERE key = :key LIMIT 1")
    suspend fun get(key: String): KvStoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KvStoreEntity)
}
