package com.zerotoler.rpgmenu.data.repository

import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import kotlinx.coroutines.flow.Flow

interface PartCatalogRepository {
    fun getAllPartsFlow(): Flow<List<PartBase>>
    fun getPartsByCategoryFlow(category: PartCategory): Flow<List<PartBase>>
    suspend fun getPartById(partId: String): PartBase?
    suspend fun countAll(): Int
}
