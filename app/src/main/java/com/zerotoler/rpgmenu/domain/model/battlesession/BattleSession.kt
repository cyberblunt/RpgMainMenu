package com.zerotoler.rpgmenu.domain.model.battlesession

import java.util.UUID

/**
 * In-memory battle session: 3 rounds, each vs one opponent top, player picks 3 distinct team tops.
 */
data class BattleSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val teamId: String,
    val mode: String,
    val opponentToken: String,
    val arenaSceneLabel: String,
    val arenaSceneSubtext: String,
    val playerTops: List<TeamTopConfig>,
    val opponentTops: List<OpponentBattleTop>,
    val rounds: List<BattleRoundRecord>,
    val status: BattleSessionStatus,
    /** When non-null, a battle round is active (player navigated from pre-battle into combat). */
    val activeBattleRoundIndex: Int?,
) {
    init {
        require(playerTops.size == 3) { "Battle session expects 3 player tops." }
        require(opponentTops.size == 3) { "Battle session expects 3 opponent tops." }
        require(rounds.size == 3) { "Battle session expects 3 rounds." }
    }

    fun usedPlayerSlotIndexes(): Set<Int> =
        rounds.mapNotNull { r -> r.selectedPlayerSlotIndex.takeIf { r.result != null } }.toSet()

    fun firstUnresolvedRoundIndex(): Int? =
        rounds.indexOfFirst { it.result == null }.takeIf { it >= 0 }

    fun allRoundsResolved(): Boolean = rounds.all { it.result != null }
}
