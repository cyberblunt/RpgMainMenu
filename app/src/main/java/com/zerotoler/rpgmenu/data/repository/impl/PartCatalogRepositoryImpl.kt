package com.zerotoler.rpgmenu.data.repository.impl

import com.zerotoler.rpgmenu.data.db.dao.CatalogPartDao
import com.zerotoler.rpgmenu.data.mapper.toDomain
import com.zerotoler.rpgmenu.data.repository.PartCatalogRepository
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PartCatalogRepositoryImpl(
    private val catalogPartDao: CatalogPartDao,
) : PartCatalogRepository {
    override fun getAllPartsFlow(): Flow<List<PartBase>> =
        catalogPartDao.getAllPartsFlow().map { list -> list.map { it.toDomain() } }

    override fun getPartsByCategoryFlow(category: PartCategory): Flow<List<PartBase>> =
        catalogPartDao.getPartsByCategoryFlow(category.name).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getPartById(partId: String): PartBase? =
        catalogPartDao.getPartById(partId)?.toDomain()

    override suspend fun countAll(): Int = catalogPartDao.countAll()
}
