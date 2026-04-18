package com.zerotoler.rpgmenu.data.repository

import com.zerotoler.rpgmenu.domain.model.PlayerPartState
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllPlayerStatesFlow(): Flow<List<PlayerPartState>>
    suspend fun getPlayerState(partId: String): PlayerPartState?
    suspend fun grantOwnership(partId: String): Boolean
    suspend fun addShards(partId: String, delta: Int)
}
