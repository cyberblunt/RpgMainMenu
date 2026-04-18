package com.zerotoler.rpgmenu.ui.battle

import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.domain.engine.BattleEngine

data class RealBattleUiState(
    val render: BattleRenderSnapshot,
    val engine: BattleEngine?,
    val playerDisplayName: String,
    val enemyDisplayName: String,
    val isBusy: Boolean,
    val fatalError: String?,
)
