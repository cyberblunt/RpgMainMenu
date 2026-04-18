package com.zerotoler.rpgmenu.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeHubUiState(
    val displayName: String,
    val gold: Long,
    val gems: Long,
    val chestKeys: Int,
    val assistantLine: String,
    val unreadMail: Int,
    val eventMissionsOpen: Int,
)

class HomeHubViewModel(
    playerProgressRepository: PlayerProgressRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeHubUiState> = combine(
        playerProgressRepository.observeProfile(),
        playerProgressRepository.observeWallet(),
        playerProgressRepository.observeMail(),
        playerProgressRepository.observeEvents(),
    ) { profile, wallet, mail, events ->
        val unread = mail.messages.count { !it.read }
        val evtOpen = events.missions.values.count { !it.claimed && it.progress > 0 }
        HomeHubUiState(
            displayName = profile.displayName,
            gold = wallet?.gold ?: 0L,
            gems = wallet?.gems ?: 0L,
            chestKeys = wallet?.chestKeys ?: 0,
            assistantLine = profile.assistantLine,
            unreadMail = unread,
            eventMissionsOpen = evtOpen,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeHubUiState(
            displayName = "…",
            gold = 0L,
            gems = 0L,
            chestKeys = 0,
            assistantLine = "",
            unreadMail = 0,
            eventMissionsOpen = 0,
        ),
    )
}
