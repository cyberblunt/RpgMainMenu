package com.zerotoler.rpgmenu.data.opponent

import com.zerotoler.rpgmenu.domain.model.battlesession.OpponentBattleTop

/**
 * Supplies opponent roster for a battle session. Swap implementation for online/boss variants later.
 */
fun interface OpponentTeamProvider {
    suspend fun opponentTeamForSession(mode: String, opponentToken: String): List<OpponentBattleTop>
}
