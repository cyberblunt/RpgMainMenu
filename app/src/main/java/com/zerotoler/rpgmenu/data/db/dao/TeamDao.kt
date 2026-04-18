package com.zerotoler.rpgmenu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerotoler.rpgmenu.data.db.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(teams: List<TeamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(team: TeamEntity)

    @Query("SELECT * FROM teams ORDER BY id ASC")
    fun getAllFlow(): Flow<List<TeamEntity>>

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun countAll(): Int
}
