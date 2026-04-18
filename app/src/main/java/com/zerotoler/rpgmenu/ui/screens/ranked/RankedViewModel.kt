package com.zerotoler.rpgmenu.ui.screens.ranked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.service.LeaderboardService
import kotlinx.coroutines.flow.SharingStarted
import com.zerotoler.rpgmenu.data.service.RankedRow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RankedUiState(
    val rows: List<RankedRow>,
    val tickets: Int,
    val tierName: String,
    val points: Int,
)

class RankedViewModel(
    leaderboardService: LeaderboardService,
    playerProgressRepository: PlayerProgressRepository,
) : ViewModel() {

    val uiState: StateFlow<RankedUiState> = combine(
        leaderboardService.observeRankedBoard(),
        playerProgressRepository.observeWallet(),
        playerProgressRepository.observeRanked(),
    ) { rows, wallet, ranked ->
        RankedUiState(
            rows = rows,
            tickets = wallet?.championshipTickets ?: 0,
            tierName = ranked.tierName,
            points = ranked.points,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RankedUiState(emptyList(), 0, "", 0),
    )
}
