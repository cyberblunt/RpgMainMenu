package com.zerotoler.rpgmenu.data.repository.impl

import com.zerotoler.rpgmenu.data.db.dao.KvStoreDao
import com.zerotoler.rpgmenu.data.db.dao.WalletDao
import com.zerotoler.rpgmenu.data.db.entity.KvStoreEntity
import com.zerotoler.rpgmenu.data.db.entity.WalletEntity
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.repository.ProgressJsonKeys
import com.zerotoler.rpgmenu.data.repository.academyDefault
import com.zerotoler.rpgmenu.data.repository.battlePassDefault
import com.zerotoler.rpgmenu.data.repository.collectionDefault
import com.zerotoler.rpgmenu.data.repository.decodeOrDefault
import com.zerotoler.rpgmenu.data.repository.eventsDefault
import com.zerotoler.rpgmenu.data.repository.idempotencyDefault
import com.zerotoler.rpgmenu.data.repository.mailDefault
import com.zerotoler.rpgmenu.data.repository.matchHistoryDefault
import com.zerotoler.rpgmenu.data.repository.profileDefault
import com.zerotoler.rpgmenu.data.repository.progressJson
import com.zerotoler.rpgmenu.data.repository.rankedDefault
import com.zerotoler.rpgmenu.data.repository.talentDefault
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString

class PlayerProgressRepositoryImpl(
    private val walletDao: WalletDao,
    private val kvStoreDao: KvStoreDao,
) : PlayerProgressRepository {

    private val mutex = Mutex()

    override fun observeWallet(): Flow<WalletEntity?> = walletDao.observeWallet()

    override suspend fun snapshotWallet(): WalletEntity {
        mutex.withLock {
            return walletDao.getWallet()
                ?: WalletEntity(
                    id = WalletEntity.SINGLETON_ID,
                    gold = 0L,
                    gems = 0,
                    chestKeys = 0,
                    championshipTickets = 0,
                )
        }
    }

    override suspend fun snapshotTalent(): TalentProgress {
        mutex.withLock {
            return readTalent()
        }
    }

    override suspend fun snapshotProfile(): ProfileProgress {
        mutex.withLock {
            return readProfile()
        }
    }

    override suspend fun ensureWalletRow() {
        mutex.withLock {
            if (walletDao.getWallet() == null) {
                walletDao.upsert(
                    WalletEntity(
                        id = WalletEntity.SINGLETON_ID,
                        gold = 5000L,
                        gems = 120,
                        chestKeys = 3,
                        championshipTickets = 5,
                    ),
                )
            }
        }
    }

    override suspend fun mutateWallet(transform: (WalletEntity) -> WalletEntity) {
        mutex.withLock {
            val current = walletDao.getWallet()
                ?: WalletEntity(
                    id = WalletEntity.SINGLETON_ID,
                    gold = 0L,
                    gems = 0,
                    chestKeys = 0,
                    championshipTickets = 0,
                )
            walletDao.upsert(transform(current))
        }
    }

    override fun observeProfile(): Flow<ProfileProgress> =
        kvStoreDao.observe(ProgressJsonKeys.PROFILE).map { row ->
            progressJson.decodeOrDefault(row?.value, profileDefault())
        }

    override suspend fun updateProfile(transform: (ProfileProgress) -> ProfileProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.PROFILE, progressJson.encodeToString(transform(readProfile())))
        }
    }

    override fun observeTalent(): Flow<TalentProgress> =
        kvStoreDao.observe(ProgressJsonKeys.TALENT).map { row ->
            progressJson.decodeOrDefault(row?.value, talentDefault())
        }

    override suspend fun updateTalent(transform: (TalentProgress) -> TalentProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.TALENT, progressJson.encodeToString(transform(readTalent())))
        }
    }

    override fun observeCollection(): Flow<CollectionProgress> =
        kvStoreDao.observe(ProgressJsonKeys.COLLECTION).map { row ->
            progressJson.decodeOrDefault(row?.value, collectionDefault())
        }

    override suspend fun updateCollection(transform: (CollectionProgress) -> CollectionProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.COLLECTION, progressJson.encodeToString(transform(readCollection())))
        }
    }

    override fun observeBattlePass(): Flow<BattlePassProgress> =
        kvStoreDao.observe(ProgressJsonKeys.BATTLE_PASS).map { row ->
            progressJson.decodeOrDefault(row?.value, battlePassDefault())
        }

    override suspend fun updateBattlePass(transform: (BattlePassProgress) -> BattlePassProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.BATTLE_PASS, progressJson.encodeToString(transform(readBattlePass())))
        }
    }

    override fun observeAcademy(): Flow<AcademyProgress> =
        kvStoreDao.observe(ProgressJsonKeys.ACADEMY).map { row ->
            progressJson.decodeOrDefault(row?.value, academyDefault())
        }

    override suspend fun updateAcademy(transform: (AcademyProgress) -> AcademyProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.ACADEMY, progressJson.encodeToString(transform(readAcademy())))
        }
    }

    override fun observeEvents(): Flow<EventProgress> =
        kvStoreDao.observe(ProgressJsonKeys.EVENTS).map { row ->
            progressJson.decodeOrDefault(row?.value, eventsDefault())
        }

    override suspend fun updateEvents(transform: (EventProgress) -> EventProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.EVENTS, progressJson.encodeToString(transform(readEvents())))
        }
    }

    override fun observeMail(): Flow<MailProgress> =
        kvStoreDao.observe(ProgressJsonKeys.MAIL).map { row ->
            progressJson.decodeOrDefault(row?.value, mailDefault())
        }

    override suspend fun updateMail(transform: (MailProgress) -> MailProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.MAIL, progressJson.encodeToString(transform(readMail())))
        }
    }

    override fun observeRanked(): Flow<RankedProgress> =
        kvStoreDao.observe(ProgressJsonKeys.RANKED).map { row ->
            progressJson.decodeOrDefault(row?.value, rankedDefault())
        }

    override suspend fun updateRanked(transform: (RankedProgress) -> RankedProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.RANKED, progressJson.encodeToString(transform(readRanked())))
        }
    }

    override fun observeMatchHistory(): Flow<MatchHistoryProgress> =
        kvStoreDao.observe(ProgressJsonKeys.MATCH_HISTORY).map { row ->
            progressJson.decodeOrDefault(row?.value, matchHistoryDefault())
        }

    override suspend fun updateMatchHistory(transform: (MatchHistoryProgress) -> MatchHistoryProgress) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.MATCH_HISTORY, progressJson.encodeToString(transform(readMatchHistory())))
        }
    }

    override fun observeIdempotency(): Flow<IdempotencyLedger> =
        kvStoreDao.observe(ProgressJsonKeys.IDEMPOTENCY).map { row ->
            progressJson.decodeOrDefault(row?.value, idempotencyDefault())
        }

    override suspend fun updateIdempotency(transform: (IdempotencyLedger) -> IdempotencyLedger) {
        mutex.withLock {
            writeJson(ProgressJsonKeys.IDEMPOTENCY, progressJson.encodeToString(transform(readIdempotency())))
        }
    }

    override suspend fun runTransaction(block: suspend PlayerProgressRepository.() -> Unit) {
        mutex.withLock {
            block()
        }
    }

    override suspend fun tryMarkBattleResultProcessed(resultId: String): Boolean {
        mutex.withLock {
            val cur = readIdempotency()
            if (resultId in cur.processedBattleResultIds) return false
            writeJson(
                ProgressJsonKeys.IDEMPOTENCY,
                progressJson.encodeToString(
                    cur.copy(processedBattleResultIds = cur.processedBattleResultIds + resultId),
                ),
            )
            return true
        }
    }

    override suspend fun tryMarkChestOpenProcessed(openId: String): Boolean {
        mutex.withLock {
            val cur = readIdempotency()
            if (openId in cur.processedChestOpenIds) return false
            writeJson(
                ProgressJsonKeys.IDEMPOTENCY,
                progressJson.encodeToString(
                    cur.copy(processedChestOpenIds = cur.processedChestOpenIds + openId),
                ),
            )
            return true
        }
    }

    override suspend fun tryMarkMailClaimProcessed(claimId: String): Boolean {
        mutex.withLock {
            val cur = readIdempotency()
            if (claimId in cur.processedMailClaimIds) return false
            writeJson(
                ProgressJsonKeys.IDEMPOTENCY,
                progressJson.encodeToString(
                    cur.copy(processedMailClaimIds = cur.processedMailClaimIds + claimId),
                ),
            )
            return true
        }
    }

    private suspend fun readProfile(): ProfileProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.PROFILE)?.value, profileDefault())

    private suspend fun readTalent(): TalentProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.TALENT)?.value, talentDefault())

    private suspend fun readCollection(): CollectionProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.COLLECTION)?.value, collectionDefault())

    private suspend fun readBattlePass(): BattlePassProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.BATTLE_PASS)?.value, battlePassDefault())

    private suspend fun readAcademy(): AcademyProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.ACADEMY)?.value, academyDefault())

    private suspend fun readEvents(): EventProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.EVENTS)?.value, eventsDefault())

    private suspend fun readMail(): MailProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.MAIL)?.value, mailDefault())

    private suspend fun readRanked(): RankedProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.RANKED)?.value, rankedDefault())

    private suspend fun readMatchHistory(): MatchHistoryProgress =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.MATCH_HISTORY)?.value, matchHistoryDefault())

    private suspend fun readIdempotency(): IdempotencyLedger =
        progressJson.decodeOrDefault(kvStoreDao.get(ProgressJsonKeys.IDEMPOTENCY)?.value, idempotencyDefault())

    private suspend fun writeJson(key: String, json: String) {
        kvStoreDao.upsert(KvStoreEntity(key = key, value = json))
    }
}
