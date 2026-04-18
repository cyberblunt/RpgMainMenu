package com.zerotoler.rpgmenu.data.seed

import android.content.SharedPreferences
import com.zerotoler.rpgmenu.data.db.AppDatabase
import com.zerotoler.rpgmenu.data.db.dao.LoadoutDao
import com.zerotoler.rpgmenu.data.db.dao.TeamDao
import com.zerotoler.rpgmenu.data.db.entity.KvStoreEntity
import com.zerotoler.rpgmenu.data.db.entity.LoadoutEntity
import com.zerotoler.rpgmenu.data.db.entity.PlayerPartEntity
import com.zerotoler.rpgmenu.data.db.entity.TeamEntity
import com.zerotoler.rpgmenu.data.mapper.toEntity
import com.zerotoler.rpgmenu.data.repository.ProgressJsonKeys
import com.zerotoler.rpgmenu.data.repository.progressJson
import com.zerotoler.rpgmenu.domain.model.DebugSeedMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString

class SeedInitializer(
    private val database: AppDatabase,
    private val prefs: SharedPreferences,
) {
    suspend fun runIfNeeded() = withContext(Dispatchers.IO) {
        val catalogDao = database.catalogPartDao()
        val playerDao = database.playerPartDao()
        val teamDao = database.teamDao()
        val loadoutDao = database.loadoutDao()
        val kvDao = database.kvStoreDao()

        catalogDao.insertAll(MasterCatalogProvider.allParts().map { it.toEntity() })

        val allCatalogIds = catalogDao.getAllIds()
        val existingPlayerIds = playerDao.getAllIds().toSet()
        val missingPlayerRows = allCatalogIds.filter { it !in existingPlayerIds }
        if (missingPlayerRows.isNotEmpty()) {
            playerDao.insertAll(
                missingPlayerRows.map { partId ->
                    PlayerPartEntity(
                        partId = partId,
                        owned = false,
                        level = 1,
                        shardCount = 0,
                        copies = 1,
                        isFavorite = false,
                        obtainedAtEpochMillis = null,
                    )
                },
            )
        }

        ensureTeamsAndLoadouts(teamDao, loadoutDao)

        if (!prefs.getBoolean(PREF_OWNERSHIP_SEEDED, false)) {
            when (DebugInventoryConfig.currentSeedMode()) {
                DebugSeedMode.UNLOCK_ALL_PARTS -> playerDao.markAllOwned()
                DebugSeedMode.STARTER_ONLY -> {
                    playerDao.markAllUnowned()
                    val ids = StarterSetProvider.starterOwnedPartIds().toList()
                    if (ids.isNotEmpty()) {
                        playerDao.markOwnedIds(ids)
                    }
                }
            }
            prefs.edit().putBoolean(PREF_OWNERSHIP_SEEDED, true).apply()
        }

        suspend fun putIfAbsent(key: String, encode: () -> String) {
            if (kvDao.get(key) == null) {
                kvDao.upsert(KvStoreEntity(key = key, value = encode()))
            }
        }

        putIfAbsent(ProgressJsonKeys.PROFILE) {
            progressJson.encodeToString(GameProgressSeeds.initialProfile())
        }
        putIfAbsent(ProgressJsonKeys.TALENT) {
            progressJson.encodeToString(GameProgressSeeds.initialTalent())
        }
        putIfAbsent(ProgressJsonKeys.COLLECTION) {
            progressJson.encodeToString(GameProgressSeeds.initialCollection())
        }
        putIfAbsent(ProgressJsonKeys.BATTLE_PASS) {
            progressJson.encodeToString(GameProgressSeeds.initialBattlePass())
        }
        putIfAbsent(ProgressJsonKeys.ACADEMY) {
            progressJson.encodeToString(GameProgressSeeds.initialAcademy())
        }
        putIfAbsent(ProgressJsonKeys.EVENTS) {
            progressJson.encodeToString(GameProgressSeeds.initialEvents())
        }
        putIfAbsent(ProgressJsonKeys.MAIL) {
            progressJson.encodeToString(GameProgressSeeds.initialMail())
        }
        putIfAbsent(ProgressJsonKeys.RANKED) {
            progressJson.encodeToString(GameProgressSeeds.initialRanked())
        }
        putIfAbsent(ProgressJsonKeys.MATCH_HISTORY) {
            progressJson.encodeToString(GameProgressSeeds.initialMatchHistory())
        }
        putIfAbsent(ProgressJsonKeys.IDEMPOTENCY) {
            progressJson.encodeToString(GameProgressSeeds.initialIdempotency())
        }
    }

    companion object {
        private const val PREF_OWNERSHIP_SEEDED = "ownership_seeded_v1"

        private val TEAM_ROWS: List<TeamEntity> = listOf(
            TeamEntity(id = "team_a", displayName = "A"),
            TeamEntity(id = "team_b", displayName = "B"),
            TeamEntity(id = "team_c", displayName = "C"),
            TeamEntity(id = "team_d", displayName = "D"),
        )

        suspend fun ensureTeamsAndLoadouts(teamDao: TeamDao, loadoutDao: LoadoutDao) {
            TEAM_ROWS.forEach { teamDao.upsert(it) }
            TEAM_ROWS.forEach { team ->
                repeat(3) { slot ->
                    if (loadoutDao.getLoadout(team.id, slot) == null) {
                        loadoutDao.upsert(
                            LoadoutEntity(
                                teamId = team.id,
                                slotIndex = slot,
                                name = null,
                                battleCapId = null,
                                weightRingId = null,
                                driverId = null,
                            ),
                        )
                    }
                }
            }
        }
    }
}
