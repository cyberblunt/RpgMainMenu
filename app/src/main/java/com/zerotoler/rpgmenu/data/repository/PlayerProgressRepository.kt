package com.zerotoler.rpgmenu.data.repository

import com.zerotoler.rpgmenu.data.db.entity.WalletEntity
import com.zerotoler.rpgmenu.domain.model.progress.AcademyProgress
import com.zerotoler.rpgmenu.domain.model.progress.BattlePassProgress
import com.zerotoler.rpgmenu.domain.model.progress.CollectionProgress
import com.zerotoler.rpgmenu.domain.model.progress.EventProgress
import com.zerotoler.rpgmenu.domain.model.progress.IdempotencyLedger
import com.zerotoler.rpgmenu.domain.model.progress.MailProgress
import com.zerotoler.rpgmenu.domain.model.progress.MatchHistoryProgress
import com.zerotoler.rpgmenu.domain.model.progress.ProfileProgress
import com.zerotoler.rpgmenu.domain.model.progress.RankedProgress
import com.zerotoler.rpgmenu.domain.model.progress.TalentProgress
import kotlinx.coroutines.flow.Flow

interface PlayerProgressRepository {
    fun observeWallet(): Flow<WalletEntity?>
    suspend fun snapshotWallet(): WalletEntity
    suspend fun snapshotTalent(): TalentProgress
    suspend fun snapshotProfile(): ProfileProgress
    suspend fun ensureWalletRow()
    suspend fun mutateWallet(transform: (WalletEntity) -> WalletEntity)

    fun observeProfile(): Flow<ProfileProgress>
    suspend fun updateProfile(transform: (ProfileProgress) -> ProfileProgress)

    fun observeTalent(): Flow<TalentProgress>
    suspend fun updateTalent(transform: (TalentProgress) -> TalentProgress)

    fun observeCollection(): Flow<CollectionProgress>
    suspend fun updateCollection(transform: (CollectionProgress) -> CollectionProgress)

    fun observeBattlePass(): Flow<BattlePassProgress>
    suspend fun updateBattlePass(transform: (BattlePassProgress) -> BattlePassProgress)

    fun observeAcademy(): Flow<AcademyProgress>
    suspend fun updateAcademy(transform: (AcademyProgress) -> AcademyProgress)

    fun observeEvents(): Flow<EventProgress>
    suspend fun updateEvents(transform: (EventProgress) -> EventProgress)

    fun observeMail(): Flow<MailProgress>
    suspend fun updateMail(transform: (MailProgress) -> MailProgress)

    fun observeRanked(): Flow<RankedProgress>
    suspend fun updateRanked(transform: (RankedProgress) -> RankedProgress)

    fun observeMatchHistory(): Flow<MatchHistoryProgress>
    suspend fun updateMatchHistory(transform: (MatchHistoryProgress) -> MatchHistoryProgress)

    fun observeIdempotency(): Flow<IdempotencyLedger>
    suspend fun updateIdempotency(transform: (IdempotencyLedger) -> IdempotencyLedger)

    suspend fun runTransaction(block: suspend PlayerProgressRepository.() -> Unit)

    suspend fun tryMarkBattleResultProcessed(resultId: String): Boolean
    suspend fun tryMarkChestOpenProcessed(openId: String): Boolean
    suspend fun tryMarkMailClaimProcessed(claimId: String): Boolean
}
