package com.zerotoler.rpgmenu.ui.battle

import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.domain.model.battle.VisualParticle
import com.zerotoler.rpgmenu.domain.engine.BattleEngine

data class RealBattleUiState(
    val render: BattleRenderSnapshot,
    val engine: BattleEngine?,
    val playerDisplayName: String,
    val enemyDisplayName: String,
    val isBusy: Boolean,
    val fatalError: String?,
    val autoBattle: Boolean = false,
    /** Compose-side sparks (decayed in [RealBattleViewModel.onSnapshot]). */
    val visualParticles: List<VisualParticle> = emptyList(),
)
