package com.zerotoler.rpgmenu.domain.model.progress

import kotlinx.serialization.Serializable

@Serializable
data class ProfileProgress(
    val displayName: String = "Blader",
    val level: Int = 1,
    val exp: Int = 0,
    /** Which squad's loadouts are edited in PARTS (`team_a` … `team_d`). */
    val activeTeamId: String = "team_a",
    val activeLoadoutSlot: Int = 0,
    val assistantLine: String = "Commander, your build is charged and ready.",
)

@Serializable
data class TalentProgress(
    val unlockedNodeIds: Set<String> = emptySet(),
)

@Serializable
data class CollectionProgress(
    val trackedSetCompletion: Map<String, Int> = emptyMap(),
)

@Serializable
data class BattlePassProgress(
    val seasonId: String = "season_01",
    val xp: Int = 0,
    val claimedTiers: Set<Int> = emptySet(),
)

@Serializable
data class AcademyTaskEntry(
    val current: Int = 0,
    val claimed: Boolean = false,
)

@Serializable
data class AcademyProgress(
    val chapterId: String = "novice",
    val tasks: Map<String, AcademyTaskEntry> = emptyMap(),
)

@Serializable
data class EventMissionEntry(
    val progress: Int = 0,
    val claimed: Boolean = false,
)

@Serializable
data class EventProgress(
    val activeEventId: String = "starter_rally",
    val missions: Map<String, EventMissionEntry> = emptyMap(),
    val boardCellsClaimed: Set<Int> = emptySet(),
)

@Serializable
data class MailEntry(
    val id: String,
    val title: String,
    val body: String,
    val read: Boolean = false,
    val claimed: Boolean = false,
    val goldReward: Long = 0L,
    val gemReward: Int = 0,
)

@Serializable
data class MailProgress(
    val messages: List<MailEntry> = emptyList(),
)

@Serializable
data class RankedProgress(
    val tierName: String = "Bronze III",
    val points: Int = 120,
    val wins: Int = 0,
    val losses: Int = 0,
)

@Serializable
data class MatchHistoryEntry(
    val id: String,
    val timestampEpochMillis: Long,
    val won: Boolean,
    val mode: String,
    val opponentName: String,
)

@Serializable
data class MatchHistoryProgress(
    val entries: List<MatchHistoryEntry> = emptyList(),
)

@Serializable
data class IdempotencyLedger(
    val processedBattleResultIds: Set<String> = emptySet(),
    val processedChestOpenIds: Set<String> = emptySet(),
    val processedMailClaimIds: Set<String> = emptySet(),
)
