package com.zerotoler.rpgmenu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerotoler.rpgmenu.data.db.entity.CatalogPartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogPartDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(parts: List<CatalogPartEntity>)

    @Query("SELECT * FROM catalog_parts")
    fun getAllPartsFlow(): Flow<List<CatalogPartEntity>>

    @Query("SELECT * FROM catalog_parts WHERE category = :category")
    fun getPartsByCategoryFlow(category: String): Flow<List<CatalogPartEntity>>

    @Query("SELECT * FROM catalog_parts WHERE id = :partId LIMIT 1")
    suspend fun getPartById(partId: String): CatalogPartEntity?

    @Query("SELECT * FROM catalog_parts WHERE id = :partId LIMIT 1")
    fun getPartByIdFlow(partId: String): Flow<CatalogPartEntity?>

    @Query("SELECT COUNT(*) FROM catalog_parts")
    suspend fun countAll(): Int

    @Query("SELECT id FROM catalog_parts")
    suspend fun getAllIds(): List<String>
}
