package com.zerotoler.rpgmenu.domain.model.battle

/**
 * Local battle end state before mapping to [com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundResult].
 */
enum class BattleOutcome {
    NONE,
    PLAYER_WIN,
    PLAYER_LOSS,
}
