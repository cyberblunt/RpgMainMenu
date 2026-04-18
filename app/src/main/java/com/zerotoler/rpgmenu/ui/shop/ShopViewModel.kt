package com.zerotoler.rpgmenu.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.service.ShopService
import com.zerotoler.rpgmenu.domain.model.shop.ShopOffer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ShopUiState(
    val offers: List<ShopOffer> = emptyList(),
    val gold: Long = 0L,
    val gems: Long = 0L,
)

class ShopViewModel(
    private val shopService: ShopService,
    private val playerProgressRepository: PlayerProgressRepository,
) : ViewModel() {

    val uiState: StateFlow<ShopUiState> = combine(
        shopService.observeOffers(),
        playerProgressRepository.observeWallet(),
    ) { offers, wallet ->
        ShopUiState(
            offers = offers,
            gold = wallet?.gold ?: 0L,
            gems = wallet?.gems ?: 0L,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopUiState(),
    )
}
