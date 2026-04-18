package com.zerotoler.rpgmenu.ui.battleprep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.BattleSessionRepository
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundResult
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BattleSessionResultUiState(
    val session: BattleSession?,
    val wins: Int,
    val losses: Int,
)

class BattleSessionResultViewModel(
    private val repository: BattleSessionRepository,
) : ViewModel() {

    val uiState: StateFlow<BattleSessionResultUiState> = repository.session
        .map { s ->
            val wins = s?.rounds?.count { it.result == BattleRoundResult.WIN } ?: 0
            val losses = s?.rounds?.count { it.result == BattleRoundResult.LOSS } ?: 0
            BattleSessionResultUiState(session = s, wins = wins, losses = losses)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BattleSessionResultUiState(null, 0, 0),
        )

    fun clearSession(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearSession()
            onDone()
        }
    }

    fun prepareRetry(mode: String, opponentToken: String, onReady: () -> Unit) {
        viewModelScope.launch {
            repository.clearSession()
            repository.ensureSessionForEntry(mode, opponentToken)
            onReady()
        }
    }
}
