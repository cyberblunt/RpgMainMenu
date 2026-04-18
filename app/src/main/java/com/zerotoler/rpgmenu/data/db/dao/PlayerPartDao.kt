package com.zerotoler.rpgmenu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerotoler.rpgmenu.data.db.entity.PlayerPartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerPartDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(states: List<PlayerPartEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: PlayerPartEntity)

    @Query("SELECT * FROM player_parts")
    fun getAllFlow(): Flow<List<PlayerPartEntity>>

    @Query("SELECT * FROM player_parts WHERE partId = :partId LIMIT 1")
    fun getByIdFlow(partId: String): Flow<PlayerPartEntity?>

    @Query("SELECT * FROM player_parts WHERE partId = :partId LIMIT 1")
    suspend fun getById(partId: String): PlayerPartEntity?

    @Query("SELECT COUNT(*) FROM player_parts")
    suspend fun countAll(): Int

    @Query("SELECT partId FROM player_parts")
    suspend fun getAllIds(): List<String>

    @Query("UPDATE player_parts SET owned = :owned WHERE partId = :partId")
    suspend fun markOwned(partId: String, owned: Boolean)

    @Query("UPDATE player_parts SET shardCount = :shardCount WHERE partId = :partId")
    suspend fun updateShardCount(partId: String, shardCount: Int)

    @Query("UPDATE player_parts SET level = :level WHERE partId = :partId")
    suspend fun updateLevel(partId: String, level: Int)

    @Query("UPDATE player_parts SET owned = 1")
    suspend fun markAllOwned()

    @Query("UPDATE player_parts SET owned = 0")
    suspend fun markAllUnowned()

    @Query("UPDATE player_parts SET owned = 1 WHERE partId IN (:ids)")
    suspend fun markOwnedIds(ids: List<String>)

    @Query("UPDATE player_parts SET owned = 0 WHERE partId IN (:ids)")
    suspend fun markUnownedIds(ids: List<String>)
}
