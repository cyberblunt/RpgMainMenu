package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.seed.AcademyTasks
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.domain.engine.rewards.RewardTableProvider
import com.zerotoler.rpgmenu.domain.model.progress.AcademyTaskEntry
import com.zerotoler.rpgmenu.domain.model.progress.EventMissionEntry
import com.zerotoler.rpgmenu.domain.model.progress.MatchHistoryEntry
import java.util.UUID

class ProcessBattleResultUseCase(
    private val progressRepository: PlayerProgressRepository,
    private val rewardGrantUseCase: RewardGrantUseCase,
) {
    suspend operator fun invoke(
        resultId: String,
        won: Boolean,
        mode: String,
        opponentDisplayName: String,
    ) {
        if (!progressRepository.tryMarkBattleResultProcessed(resultId)) return

        val bundle = if (won) {
            RewardTableProvider.battleVictoryBundle(mode)
        } else {
            RewardTableProvider.battleDefeatBundle(mode)
        }
        rewardGrantUseCase(bundle)

        progressRepository.updateAcademy { a ->
            val m = a.tasks.toMutableMap()
            fun bump(taskId: String, target: Int = 1) {
                val cur = m[taskId] ?: AcademyTaskEntry()
                m[taskId] = cur.copy(current = (cur.current + 1).coerceAtMost(target))
            }
            bump(AcademyTasks.FIRST_BATTLE, 1)
            if (mode == "ranked" && won) {
                bump(AcademyTasks.WIN_RANKED, 1)
            }
            a.copy(tasks = m)
        }

        progressRepository.updateEvents { e ->
            val missions = e.missions.toMutableMap()
            val play = missions["evt_play_3"] ?: EventMissionEntry()
            missions["evt_play_3"] = play.copy(progress = (play.progress + 1).coerceAtMost(3))
            if (won) {
                val win = missions["evt_win_1"] ?: EventMissionEntry()
                missions["evt_win_1"] = win.copy(progress = (win.progress + 1).coerceAtMost(1))
            }
            e.copy(missions = missions)
        }

        progressRepository.updateMatchHistory { mh ->
            val entry = MatchHistoryEntry(
                id = UUID.randomUUID().toString(),
                timestampEpochMillis = System.currentTimeMillis(),
                won = won,
                mode = mode,
                opponentName = opponentDisplayName,
            )
            mh.copy(entries = (listOf(entry) + mh.entries).take(25))
        }

        if (mode == "ranked") {
            progressRepository.updateRanked { r ->
                if (won) {
                    r.copy(points = r.points + 28, wins = r.wins + 1)
                } else {
                    r.copy(points = (r.points - 12).coerceAtLeast(0), losses = r.losses + 1)
                }
            }
        }
    }
}
