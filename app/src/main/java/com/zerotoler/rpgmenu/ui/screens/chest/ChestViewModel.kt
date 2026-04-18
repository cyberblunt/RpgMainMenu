package com.zerotoler.rpgmenu.ui.screens.chest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import com.zerotoler.rpgmenu.domain.usecase.OpenChestUseCase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChestUiState(
    val keys: Int,
    val lastSummary: String?,
    val error: String?,
)

class ChestViewModel(
    playerProgressRepository: PlayerProgressRepository,
    private val openChestUseCase: OpenChestUseCase,
) : ViewModel() {

    private val _last = MutableStateFlow<String?>(null)
    private val _err = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChestUiState> = combine(
        playerProgressRepository.observeWallet().map { it?.chestKeys ?: 0 },
        _last,
        _err,
    ) { keys, last, err ->
        ChestUiState(keys = keys, lastSummary = last, error = err)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChestUiState(0, null, null),
    )

    fun openOne() {
        viewModelScope.launch {
            _err.value = null
            val id = UUID.randomUUID().toString()
            openChestUseCase(id, 1).fold(
                onSuccess = { b -> _last.value = summarize(b) },
                onFailure = { e -> _err.value = e.message },
            )
        }
    }

    fun openFive() {
        viewModelScope.launch {
            _err.value = null
            val id = UUID.randomUUID().toString()
            openChestUseCase(id, 5).fold(
                onSuccess = { b -> _last.value = summarize(b) },
                onFailure = { e -> _err.value = e.message },
            )
        }
    }

    fun clearError() {
        _err.value = null
    }

    private fun summarize(b: RewardBundle): String =
        buildString {
            append("Gold +${b.gold}")
            if (b.gems > 0) append(" · Gems +${b.gems}")
            if (b.partIds.isNotEmpty()) append(" · Parts: ${b.partIds.joinToString()}")
            if (b.shardsByPartId.isNotEmpty()) append(" · Shards granted")
            append(" · BP +${b.battlePassXp}")
        }
}
