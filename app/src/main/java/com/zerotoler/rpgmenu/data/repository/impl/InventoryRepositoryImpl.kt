package com.zerotoler.rpgmenu.data.repository.impl

import com.zerotoler.rpgmenu.data.db.dao.PlayerPartDao
import com.zerotoler.rpgmenu.data.mapper.toDomain
import com.zerotoler.rpgmenu.data.repository.InventoryRepository
import com.zerotoler.rpgmenu.domain.model.PlayerPartState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepositoryImpl(
    private val playerPartDao: PlayerPartDao,
) : InventoryRepository {
    override fun getAllPlayerStatesFlow(): Flow<List<PlayerPartState>> =
        playerPartDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getPlayerState(partId: String): PlayerPartState? =
        playerPartDao.getById(partId)?.toDomain()

    override suspend fun grantOwnership(partId: String): Boolean {
        val row = playerPartDao.getById(partId) ?: return false
        playerPartDao.upsert(
            row.copy(
                owned = true,
                obtainedAtEpochMillis = row.obtainedAtEpochMillis ?: System.currentTimeMillis(),
            ),
        )
        return true
    }

    override suspend fun addShards(partId: String, delta: Int) {
        val row = playerPartDao.getById(partId) ?: return
        playerPartDao.updateShardCount(partId, (row.shardCount + delta).coerceAtLeast(0))
    }
}
