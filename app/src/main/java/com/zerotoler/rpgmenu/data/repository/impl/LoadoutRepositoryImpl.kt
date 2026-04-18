package com.zerotoler.rpgmenu.data.repository.impl

import com.zerotoler.rpgmenu.data.db.dao.LoadoutDao
import com.zerotoler.rpgmenu.data.mapper.toDomain
import com.zerotoler.rpgmenu.data.mapper.toEntity
import com.zerotoler.rpgmenu.data.repository.LoadoutRepository
import com.zerotoler.rpgmenu.domain.model.Loadout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadoutRepositoryImpl(
    private val loadoutDao: LoadoutDao,
) : LoadoutRepository {
    override fun getLoadoutsForTeamFlow(teamId: String): Flow<List<Loadout>> =
        loadoutDao.getLoadoutsForTeamFlow(teamId).map { list -> list.map { it.toDomain() } }

    override fun getLoadoutFlow(teamId: String, slotIndex: Int): Flow<Loadout?> =
        loadoutDao.getLoadoutFlow(teamId, slotIndex).map { it?.toDomain() }

    override suspend fun getLoadoutOnce(teamId: String, slotIndex: Int): Loadout? =
        loadoutDao.getLoadout(teamId, slotIndex)?.toDomain()

    override suspend fun upsertLoadout(loadout: Loadout) {
        loadoutDao.upsert(loadout.toEntity())
    }

    override suspend fun countAll(): Int = loadoutDao.countAll()
}
