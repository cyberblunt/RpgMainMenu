package com.zerotoler.rpgmenu.data.repository.impl

import com.zerotoler.rpgmenu.data.db.dao.TeamDao
import com.zerotoler.rpgmenu.data.mapper.toDomain
import com.zerotoler.rpgmenu.data.repository.TeamRepository
import com.zerotoler.rpgmenu.domain.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TeamRepositoryImpl(
    private val teamDao: TeamDao,
) : TeamRepository {
    override fun getAllTeamsFlow(): Flow<List<Team>> =
        teamDao.getAllFlow().map { list -> list.map { it.toDomain() } }
}
