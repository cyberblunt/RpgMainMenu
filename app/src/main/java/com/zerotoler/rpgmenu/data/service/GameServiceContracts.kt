package com.zerotoler.rpgmenu.data.service

import com.zerotoler.rpgmenu.domain.model.Loadout
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PlayerPartState
import com.zerotoler.rpgmenu.domain.model.progress.BattlePassProgress
import com.zerotoler.rpgmenu.domain.model.progress.EventProgress
import com.zerotoler.rpgmenu.domain.model.progress.MailProgress
import com.zerotoler.rpgmenu.domain.model.progress.ProfileProgress
import com.zerotoler.rpgmenu.data.content.BossDefinition
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import com.zerotoler.rpgmenu.domain.model.shop.ShopOffer
import kotlinx.coroutines.flow.Flow

interface ProfileService {
    fun observeProfile(): Flow<ProfileProgress>
    suspend fun updateProfile(transform: (ProfileProgress) -> ProfileProgress)
}

interface InventoryService {
    fun observePlayerParts(): Flow<List<PlayerPartState>>
    fun observeCatalog(): Flow<List<PartBase>>
    suspend fun grantPartOwnership(partId: String): Boolean
}

interface LoadoutService {
    fun observeTeamLoadouts(teamId: String): Flow<List<Loadout>>
    suspend fun getLoadout(teamId: String, slotIndex: Int): Loadout?
}

interface LeaderboardService {
    fun observeRankedBoard(): Flow<List<RankedRow>>
}

data class RankedRow(
    val rank: Int,
    val name: String,
    val title: String,
    val points: Int,
    val isPlayer: Boolean,
)

interface MissionService {
    fun observeAcademyTasks(): Flow<com.zerotoler.rpgmenu.domain.model.progress.AcademyProgress>
}

interface EventService {
    fun observeEvents(): Flow<EventProgress>
    suspend fun updateEvents(transform: (EventProgress) -> EventProgress)
}

interface BattlePassService {
    fun observeBattlePass(): Flow<BattlePassProgress>
    suspend fun updateBattlePass(transform: (BattlePassProgress) -> BattlePassProgress)
}

interface MailService {
    fun observeMail(): Flow<MailProgress>
    suspend fun updateMail(transform: (MailProgress) -> MailProgress)
}

interface RewardService {
    suspend fun grant(bundle: RewardBundle)
}

interface ShopService {
    fun observeOffers(): Flow<List<ShopOffer>>
    suspend fun purchase(offerId: String): ShopPurchaseResult
}

sealed class ShopPurchaseResult {
    data object Success : ShopPurchaseResult()
    data class Failure(val message: String) : ShopPurchaseResult()
}

interface BossService {
    fun observeBosses(): Flow<List<BossDefinition>>
    suspend fun getBoss(bossId: String): BossDefinition?
}

interface SocialService {
    fun observeFriends(): Flow<List<SocialFriendCard>>
}

data class SocialFriendCard(
    val id: String,
    val name: String,
    val statusLine: String,
    val online: Boolean,
)
