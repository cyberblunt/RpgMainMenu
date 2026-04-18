package com.zerotoler.rpgmenu.data.repository

import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundResult
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSession
import kotlinx.coroutines.flow.StateFlow

enum class RoundResolveDestination {
    CONTINUE_TO_NEXT_ROUND,
    SESSION_COMPLETE,
}

interface BattleSessionRepository {
    val session: StateFlow<BattleSession?>

    suspend fun ensureSessionForEntry(mode: String, opponentToken: String)

    suspend fun setSelectedSlot(slotIndex: Int?)

    suspend fun openBattleForCurrentRound()

    suspend fun cancelBattleRound()

    suspend fun resolveCurrentRound(result: BattleRoundResult): RoundResolveDestination

    suspend fun clearSession()
}
