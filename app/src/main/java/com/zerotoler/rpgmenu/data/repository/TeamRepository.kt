package com.zerotoler.rpgmenu.data.repository

import com.zerotoler.rpgmenu.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getAllTeamsFlow(): Flow<List<Team>>
}
