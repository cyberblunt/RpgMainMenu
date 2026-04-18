package com.zerotoler.rpgmenu.data.opponent

import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.battlesession.OpponentBattleTop

/**
 * Local mock opponent team — exactly three tops for a standard session.
 */
class LocalOpponentTeamProvider : OpponentTeamProvider {
    override suspend fun opponentTeamForSession(mode: String, opponentToken: String): List<OpponentBattleTop> {
        // Future: branch on mode/token for bosses; keep deterministic mock for now.
        return listOf(
            OpponentBattleTop(
                id = "opp_${opponentToken}_r1",
                name = "Crimson Striker",
                archetype = CombatType.ATTACK,
                powerLevelHint = 12,
                intelLabel = "Aggressive",
            ),
            OpponentBattleTop(
                id = "opp_${opponentToken}_r2",
                name = "Azure Guard",
                archetype = CombatType.DEFENSE,
                powerLevelHint = 11,
                intelLabel = "Wall",
            ),
            OpponentBattleTop(
                id = "opp_${opponentToken}_r3",
                name = "Violet Drift",
                archetype = CombatType.STAMINA,
                powerLevelHint = 10,
                intelLabel = "Endurance",
            ),
        )
    }
}
