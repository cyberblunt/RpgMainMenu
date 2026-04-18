package com.zerotoler.rpgmenu.ui.battleprep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.BattleSessionRepository
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundResult
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSession
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreBattleSelectionViewModel(
    private val mode: String,
    private val opponentToken: String,
    private val repository: BattleSessionRepository,
) : ViewModel() {

    val uiState: StateFlow<PreBattleSelectionUiState> = repository.session
        .map { s -> toUi(s) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PreBattleSelectionUiState(
                roundDisplay = "Round — / 3",
                roundIndexOneBased = 1,
                arenaLabel = "—",
                arenaSubtext = "",
                currentOpponent = null,
                opponentRoster = emptyList(),
                playerTops = emptyList(),
                selectedSlot = null,
                usedSlots = emptySet(),
                completedRoundResults = listOf(null, null, null),
                teamFullyConfigured = false,
                canStartBattle = false,
                isLoading = true,
            ),
        )

    init {
        viewModelScope.launch {
            repository.ensureSessionForEntry(mode, opponentToken)
        }
    }

    fun selectPlayerSlot(slotIndex: Int) {
        viewModelScope.launch {
            repository.setSelectedSlot(slotIndex)
        }
    }

    fun startBattle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching { repository.openBattleForCurrentRound() }
                .onSuccess { onSuccess() }
        }
    }

    private fun toUi(s: BattleSession?): PreBattleSelectionUiState {
        if (s == null) {
            return PreBattleSelectionUiState(
                roundDisplay = "Round — / 3",
                roundIndexOneBased = 1,
                arenaLabel = "—",
                arenaSubtext = "",
                currentOpponent = null,
                opponentRoster = emptyList(),
                playerTops = emptyList(),
                selectedSlot = null,
                usedSlots = emptySet(),
                completedRoundResults = listOf(null, null, null),
                teamFullyConfigured = false,
                canStartBattle = false,
                isLoading = true,
            )
        }
        val activeIdx = s.firstUnresolvedRoundIndex()
        val roundOneBased = (activeIdx ?: 2) + 1
        val currentOpp = activeIdx?.let { s.rounds[it].opponentTop }
        val used = s.usedPlayerSlotIndexes()
        val sel = activeIdx?.let { s.rounds[it].selectedPlayerSlotIndex }
        val teamOk = s.playerTops.all { it.isComplete }
        val canStart = teamOk &&
            s.status == BattleSessionStatus.IN_PROGRESS &&
            activeIdx != null &&
            sel != null &&
            sel in 0..2 &&
            s.playerTops.getOrNull(sel)?.isComplete == true &&
            sel !in used

        val markers = s.rounds.map { r ->
            when (r.result) {
                BattleRoundResult.WIN -> true
                BattleRoundResult.LOSS -> false
                null -> null
            }
        }

        return PreBattleSelectionUiState(
            roundDisplay = "Round $roundOneBased / 3",
            roundIndexOneBased = roundOneBased,
            arenaLabel = s.arenaSceneLabel,
            arenaSubtext = s.arenaSceneSubtext,
            currentOpponent = currentOpp,
            opponentRoster = s.opponentTops,
            playerTops = s.playerTops,
            selectedSlot = sel,
            usedSlots = used,
            completedRoundResults = markers,
            teamFullyConfigured = teamOk,
            canStartBattle = canStart,
            isLoading = false,
        )
    }
}
