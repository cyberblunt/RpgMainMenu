package com.zerotoler.rpgmenu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerotoler.rpgmenu.data.db.entity.LoadoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoadoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(loadout: LoadoutEntity)

    @Query("SELECT * FROM loadouts WHERE teamId = :teamId ORDER BY slotIndex ASC")
    fun getLoadoutsForTeamFlow(teamId: String): Flow<List<LoadoutEntity>>

    @Query(
        "SELECT * FROM loadouts WHERE teamId = :teamId AND slotIndex = :slotIndex LIMIT 1",
    )
    suspend fun getLoadout(teamId: String, slotIndex: Int): LoadoutEntity?

    @Query(
        "SELECT * FROM loadouts WHERE teamId = :teamId AND slotIndex = :slotIndex LIMIT 1",
    )
    fun getLoadoutFlow(teamId: String, slotIndex: Int): Flow<LoadoutEntity?>

    @Query("SELECT COUNT(*) FROM loadouts")
    suspend fun countAll(): Int
}
