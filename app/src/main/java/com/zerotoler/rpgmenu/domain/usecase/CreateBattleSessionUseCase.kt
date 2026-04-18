package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundRecord
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSession
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleSessionStatus
import com.zerotoler.rpgmenu.domain.model.battlesession.OpponentBattleTop
import com.zerotoler.rpgmenu.domain.model.battlesession.TeamTopConfig

class CreateBattleSessionUseCase {
    operator fun invoke(
        teamId: String,
        mode: String,
        opponentToken: String,
        playerTops: List<TeamTopConfig>,
        opponentTops: List<OpponentBattleTop>,
    ): BattleSession {
        val rounds = opponentTops.mapIndexed { index, opp ->
            BattleRoundRecord(
                roundIndex = index,
                opponentTop = opp,
                selectedPlayerSlotIndex = null,
                result = null,
            )
        }
        return BattleSession(
            teamId = teamId,
            mode = mode,
            opponentToken = opponentToken,
            arenaSceneLabel = "Classic Blue",
            arenaSceneSubtext = "Standard arena · local mock opponents",
            playerTops = playerTops,
            opponentTops = opponentTops,
            rounds = rounds,
            status = BattleSessionStatus.IN_PROGRESS,
            activeBattleRoundIndex = null,
        )
    }
}
