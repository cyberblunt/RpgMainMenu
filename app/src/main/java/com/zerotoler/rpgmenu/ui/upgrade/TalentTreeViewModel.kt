package com.zerotoler.rpgmenu.ui.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.domain.usecase.UnlockTalentNodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TalentTreeUiState(
    val unlocked: Set<String>,
    val gold: Long,
    val error: String?,
)

class TalentTreeViewModel(
    playerProgressRepository: PlayerProgressRepository,
    private val unlockTalentNodeUseCase: UnlockTalentNodeUseCase,
) : ViewModel() {

    private val _err = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TalentTreeUiState> = combine(
        playerProgressRepository.observeTalent(),
        playerProgressRepository.observeWallet().map { it?.gold ?: 0L },
        _err,
    ) { talent, gold, err ->
        TalentTreeUiState(talent.unlockedNodeIds, gold, err)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TalentTreeUiState(emptySet(), 0L, null),
    )

    fun unlock(nodeId: String) {
        viewModelScope.launch {
            _err.value = null
            unlockTalentNodeUseCase(nodeId).onFailure { _err.value = it.message }
        }
    }

    fun clearError() {
        _err.value = null
    }
}
