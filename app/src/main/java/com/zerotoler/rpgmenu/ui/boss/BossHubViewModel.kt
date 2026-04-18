package com.zerotoler.rpgmenu.ui.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.content.BossDefinition
import com.zerotoler.rpgmenu.data.service.BossService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BossHubViewModel(
    bossService: BossService,
) : ViewModel() {

    val bosses: StateFlow<List<BossDefinition>> =
        bossService.observeBosses().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
