package com.zerotoler.rpgmenu.domain.engine.battle

data class BattleHudState(
    val phase: BattlePhase,
    val timeLeft: Float,
    val totalTime: Float,
    val playerHpRatio: Float,
    val enemyHpRatio: Float,
    val playerEnergy: Float,
    val enemyEnergy: Float,
    val autoBattle: Boolean,
    val paused: Boolean,
    val playerWon: Boolean?,
)

data class BattleSessionMeta(
    val mode: String,
    val opponentName: String,
    val opponentTitle: String,
    val error: String?,
    val resultId: String?,
    val resultConsumed: Boolean,
)
