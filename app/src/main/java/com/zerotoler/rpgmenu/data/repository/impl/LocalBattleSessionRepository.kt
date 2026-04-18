package com.zerotoler.rpgmenu.data.repository.impl

import com.zerotoler.rpgmenu.data.opponent.OpponentTeamProvider
import com.zerotoler.rpgmenu.data.repository.BattleSessionRepository
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.repository.RoundResolveDestination
import com.zerotoler.rpgmenu.data.seed.StarterSetProvider
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundRecord
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundResult
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSession
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSessionStatus
import com.zerotoler.rpgmenu.domain.usecase.CreateBattleSessionUseCase
import com.zerotoler.rpgmenu.domain.usecase.GetBattleReadyTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocalBattleSessionRepository(
    private val playerProgressRepository: PlayerProgressRepository,
    private val getBattleReadyTeam: GetBattleReadyTeamUseCase,
    private val createBattleSession: CreateBattleSessionUseCase,
    private val opponentTeamProvider: OpponentTeamProvider,
) : BattleSessionRepository {

    private val mutex = Mutex()
    private val _session = MutableStateFlow<BattleSession?>(null)
    override val session: StateFlow<BattleSession?> = _session.asStateFlow()

    override suspend fun ensureSessionForEntry(mode: String, opponentToken: String) = mutex.withLock {
        val resolvedTeamId = resolveActiveTeamId()
        val cur = _session.value
        val needsNew = when {
            cur == null -> true
            cur.status == BattleSessionStatus.COMPLETED -> true
            cur.mode != mode || cur.opponentToken != opponentToken -> true
            cur.teamId != resolvedTeamId -> true
            else -> false
        }
        if (!needsNew) return@withLock

        val playerTops = getBattleReadyTeam(resolvedTeamId)
        val opponents = opponentTeamProvider.opponentTeamForSession(mode, opponentToken)
        _session.value = createBattleSession(
            teamId = resolvedTeamId,
            mode = mode,
            opponentToken = opponentToken,
            playerTops = playerTops,
            opponentTops = opponents,
        )
    }

    override suspend fun setSelectedSlot(slotIndex: Int?) = mutex.withLock {
        val s = _session.value ?: return@withLock
        if (s.status != BattleSessionStatus.IN_PROGRESS) return@withLock
        val idx = s.firstUnresolvedRoundIndex() ?: return@withLock
        val round = s.rounds[idx]
        if (round.result != null) return@withLock
        val safeSlot = slotIndex?.takeIf { it in 0..2 }?.takeUnless { slot ->
            val top = s.playerTops.getOrNull(slot)
            top == null || !top.isComplete || slot in s.usedPlayerSlotIndexes()
        }
        _session.value = s.updateRound(idx) { it.copy(selectedPlayerSlotIndex = safeSlot) }
    }

    override suspend fun openBattleForCurrentRound() = mutex.withLock {
        val s = _session.value ?: error("No session")
        require(s.playerTops.all { it.isComplete }) { "Team incomplete" }
        val idx = s.firstUnresolvedRoundIndex() ?: error("No open round")
        val round = s.rounds[idx]
        val slot = round.selectedPlayerSlotIndex ?: error("No top selected")
        val top = s.playerTops.getOrNull(slot) ?: error("Invalid slot")
        require(top.isComplete) { "Selected top is incomplete" }
        require(slot !in s.usedPlayerSlotIndexes()) { "Top already used" }
        _session.value = s.copy(activeBattleRoundIndex = idx)
    }

    override suspend fun cancelBattleRound() = mutex.withLock {
        val s = _session.value ?: return@withLock
        _session.value = s.copy(activeBattleRoundIndex = null)
    }

    override suspend fun resolveCurrentRound(result: BattleRoundResult): RoundResolveDestination = mutex.withLock {
        val s = _session.value ?: error("No session")
        val ph = s.activeBattleRoundIndex ?: error("Not in battle round")
        val round = s.rounds[ph]
        require(round.result == null) { "Round already resolved" }
        val slot = round.selectedPlayerSlotIndex ?: error("No selection")
        val updatedRound = round.copy(result = result)
        val newRounds = s.rounds.toMutableList().also { it[ph] = updatedRound }
        val allDone = newRounds.all { it.result != null }
        val newStatus = if (allDone) BattleSessionStatus.COMPLETED else BattleSessionStatus.IN_PROGRESS
        _session.value = s.copy(
            rounds = newRounds,
            status = newStatus,
            activeBattleRoundIndex = null,
        )
        if (allDone) RoundResolveDestination.SESSION_COMPLETE else RoundResolveDestination.CONTINUE_TO_NEXT_ROUND
    }

    override suspend fun clearSession() = mutex.withLock {
        _session.value = null
    }

    private fun BattleSession.updateRound(index: Int, block: (BattleRoundRecord) -> BattleRoundRecord): BattleSession {
        val r = rounds.toMutableList()
        r[index] = block(r[index])
        return copy(rounds = r)
    }

    private suspend fun resolveActiveTeamId(): String {
        val id = playerProgressRepository.snapshotProfile().activeTeamId
        return id.takeIf { it.isNotBlank() } ?: StarterSetProvider.DEFAULT_TEAM_ID
    }
}
