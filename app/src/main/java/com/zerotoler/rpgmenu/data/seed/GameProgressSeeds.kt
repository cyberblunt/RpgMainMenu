package com.zerotoler.rpgmenu.data.seed

import com.zerotoler.rpgmenu.domain.model.progress.AcademyProgress
import com.zerotoler.rpgmenu.domain.model.progress.AcademyTaskEntry
import com.zerotoler.rpgmenu.domain.model.progress.BattlePassProgress
import com.zerotoler.rpgmenu.domain.model.progress.CollectionProgress
import com.zerotoler.rpgmenu.domain.model.progress.EventMissionEntry
import com.zerotoler.rpgmenu.domain.model.progress.EventProgress
import com.zerotoler.rpgmenu.domain.model.progress.IdempotencyLedger
import com.zerotoler.rpgmenu.domain.model.progress.MailEntry
import com.zerotoler.rpgmenu.domain.model.progress.MailProgress
import com.zerotoler.rpgmenu.domain.model.progress.MatchHistoryProgress
import com.zerotoler.rpgmenu.domain.model.progress.ProfileProgress
import com.zerotoler.rpgmenu.domain.model.progress.RankedProgress
import com.zerotoler.rpgmenu.domain.model.progress.TalentProgress

object GameProgressSeeds {
    fun initialProfile(): ProfileProgress = ProfileProgress()

    fun initialTalent(): TalentProgress = TalentProgress()

    fun initialCollection(): CollectionProgress = CollectionProgress()

    fun initialBattlePass(): BattlePassProgress = BattlePassProgress()

    fun initialAcademy(): AcademyProgress =
        AcademyProgress(
            tasks = mapOf(
                AcademyTasks.FIRST_BATTLE to AcademyTaskEntry(),
                AcademyTasks.FIRST_CHEST to AcademyTaskEntry(),
                AcademyTasks.EQUIP_PART to AcademyTaskEntry(),
                AcademyTasks.WIN_RANKED to AcademyTaskEntry(),
            ),
        )

    fun initialEvents(): EventProgress =
        EventProgress(
            missions = mapOf(
                "evt_win_1" to EventMissionEntry(),
                "evt_play_3" to EventMissionEntry(),
            ),
        )

    fun initialMail(): MailProgress =
        MailProgress(
            messages = listOf(
                MailEntry(
                    id = "mail_welcome",
                    title = "Welcome, Blader",
                    body = "Your local arena link is online. Claim your launch stipend.",
                    read = false,
                    claimed = false,
                    goldReward = 500L,
                    gemReward = 10,
                ),
                MailEntry(
                    id = "mail_patch",
                    title = "Maintenance Notice",
                    body = "Offline build: all rivals are training sims until live service lands.",
                    read = false,
                    claimed = false,
                    goldReward = 0L,
                    gemReward = 0,
                ),
            ),
        )

    fun initialRanked(): RankedProgress = RankedProgress()

    fun initialMatchHistory(): MatchHistoryProgress = MatchHistoryProgress()

    fun initialIdempotency(): IdempotencyLedger = IdempotencyLedger()
}

object AcademyTasks {
    const val FIRST_BATTLE = "first_battle"
    const val FIRST_CHEST = "first_chest"
    const val EQUIP_PART = "equip_part"
    const val WIN_RANKED = "win_ranked"
}
