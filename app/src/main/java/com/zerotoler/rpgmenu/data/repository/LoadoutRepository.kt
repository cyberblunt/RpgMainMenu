package com.zerotoler.rpgmenu.data.repository

import com.zerotoler.rpgmenu.domain.model.Loadout
import kotlinx.coroutines.flow.Flow

interface LoadoutRepository {
    fun getLoadoutsForTeamFlow(teamId: String): Flow<List<Loadout>>
    fun getLoadoutFlow(teamId: String, slotIndex: Int): Flow<Loadout?>
    suspend fun getLoadoutOnce(teamId: String, slotIndex: Int): Loadout?
    suspend fun upsertLoadout(loadout: Loadout)
    suspend fun countAll(): Int
}
