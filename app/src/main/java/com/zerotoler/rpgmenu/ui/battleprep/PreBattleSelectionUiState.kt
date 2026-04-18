package com.zerotoler.rpgmenu.ui.battleprep

import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundRecord
import com.zerotoler.rpgmenu.domain.model.battlesession.OpponentBattleTop
import com.zerotoler.rpgmenu.domain.model.battlesession.TeamTopConfig

data class PreBattleSelectionUiState(
    val roundDisplay: String,
    val roundIndexOneBased: Int,
    val arenaLabel: String,
    val arenaSubtext: String,
    val currentOpponent: OpponentBattleTop?,
    val opponentRoster: List<OpponentBattleTop>,
    val playerTops: List<TeamTopConfig>,
    val selectedSlot: Int?,
    val usedSlots: Set<Int>,
    val completedRoundResults: List<Boolean?>,
    val teamFullyConfigured: Boolean,
    val canStartBattle: Boolean,
    val isLoading: Boolean,
)
