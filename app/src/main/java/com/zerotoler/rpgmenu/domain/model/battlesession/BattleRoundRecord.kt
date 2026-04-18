package com.zerotoler.rpgmenu.domain.model.battlesession

/**
 * One of exactly three rounds in a session.
 */
data class BattleRoundRecord(
    val roundIndex: Int,
    val opponentTop: OpponentBattleTop,
    val selectedPlayerSlotIndex: Int?,
    val result: BattleRoundResult?,
)
