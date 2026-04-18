package com.zerotoler.rpgmenu.data.service.local

import com.zerotoler.rpgmenu.data.content.BossCatalog
import com.zerotoler.rpgmenu.data.content.BossDefinition
import com.zerotoler.rpgmenu.data.content.BotRoster
import com.zerotoler.rpgmenu.data.repository.InventoryRepository
import com.zerotoler.rpgmenu.data.repository.LoadoutRepository
import com.zerotoler.rpgmenu.data.repository.PartCatalogRepository
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.service.BossService
import com.zerotoler.rpgmenu.data.service.BattlePassService
import com.zerotoler.rpgmenu.data.service.EventService
import com.zerotoler.rpgmenu.data.service.InventoryService
import com.zerotoler.rpgmenu.data.service.LeaderboardService
import com.zerotoler.rpgmenu.data.service.LoadoutService
import com.zerotoler.rpgmenu.data.service.MailService
import com.zerotoler.rpgmenu.data.service.MissionService
import com.zerotoler.rpgmenu.data.service.ProfileService
import com.zerotoler.rpgmenu.data.service.RankedRow
import com.zerotoler.rpgmenu.data.service.RewardService
import com.zerotoler.rpgmenu.data.service.ShopPurchaseResult
import com.zerotoler.rpgmenu.data.service.ShopService
import com.zerotoler.rpgmenu.data.service.SocialFriendCard
import com.zerotoler.rpgmenu.data.service.SocialService
import com.zerotoler.rpgmenu.domain.model.Loadout
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PlayerPartState
import com.zerotoler.rpgmenu.domain.model.progress.ProfileProgress
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import com.zerotoler.rpgmenu.domain.model.shop.ShopOffer
import com.zerotoler.rpgmenu.domain.model.shop.ShopOfferCategory
import com.zerotoler.rpgmenu.domain.usecase.RewardGrantUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
class LocalProfileService(
    private val progressRepository: PlayerProgressRepository,
) : ProfileService {
    override fun observeProfile(): Flow<ProfileProgress> = progressRepository.observeProfile()
    override suspend fun updateProfile(transform: (ProfileProgress) -> ProfileProgress) {
        progressRepository.updateProfile(transform)
    }
}

class LocalInventoryService(
    private val inventoryRepository: InventoryRepository,
    private val catalogRepository: PartCatalogRepository,
) : InventoryService {
    override fun observePlayerParts(): Flow<List<PlayerPartState>> =
        inventoryRepository.getAllPlayerStatesFlow()

    override fun observeCatalog(): Flow<List<PartBase>> = catalogRepository.getAllPartsFlow()

    override suspend fun grantPartOwnership(partId: String): Boolean =
        inventoryRepository.grantOwnership(partId)
}

class LocalLoadoutService(
    private val loadoutRepository: LoadoutRepository,
) : LoadoutService {
    override fun observeTeamLoadouts(teamId: String): Flow<List<Loadout>> =
        loadoutRepository.getLoadoutsForTeamFlow(teamId)

    override suspend fun getLoadout(teamId: String, slotIndex: Int): Loadout? =
        loadoutRepository.getLoadoutOnce(teamId, slotIndex)
}

class LocalLeaderboardService(
    private val progressRepository: PlayerProgressRepository,
) : LeaderboardService {
    override fun observeRankedBoard(): Flow<List<RankedRow>> =
        combine(
            progressRepository.observeProfile(),
            progressRepository.observeRanked(),
        ) { profile, ranked ->
            val botRows = BotRoster.ladder.mapIndexed { index, b ->
                RankedRow(
                    rank = index + 1,
                    name = b.displayName,
                    title = b.title,
                    points = 210 - index * 13,
                    isPlayer = false,
                )
            }
            val playerRow = RankedRow(
                rank = 1,
                name = profile.displayName,
                title = ranked.tierName,
                points = ranked.points,
                isPlayer = true,
            )
            (botRows + playerRow)
                .sortedByDescending { it.points }
                .mapIndexed { idx, row -> row.copy(rank = idx + 1) }
                .take(12)
        }
}

class LocalMissionService(
    private val progressRepository: PlayerProgressRepository,
) : MissionService {
    override fun observeAcademyTasks() = progressRepository.observeAcademy()
}

class LocalEventService(
    private val progressRepository: PlayerProgressRepository,
) : EventService {
    override fun observeEvents() = progressRepository.observeEvents()
    override suspend fun updateEvents(transform: (com.zerotoler.rpgmenu.domain.model.progress.EventProgress) -> com.zerotoler.rpgmenu.domain.model.progress.EventProgress) {
        progressRepository.updateEvents(transform)
    }
}

class LocalBattlePassService(
    private val progressRepository: PlayerProgressRepository,
) : BattlePassService {
    override fun observeBattlePass() = progressRepository.observeBattlePass()
    override suspend fun updateBattlePass(transform: (com.zerotoler.rpgmenu.domain.model.progress.BattlePassProgress) -> com.zerotoler.rpgmenu.domain.model.progress.BattlePassProgress) {
        progressRepository.updateBattlePass(transform)
    }
}

class LocalMailService(
    private val progressRepository: PlayerProgressRepository,
) : MailService {
    override fun observeMail() = progressRepository.observeMail()
    override suspend fun updateMail(transform: (com.zerotoler.rpgmenu.domain.model.progress.MailProgress) -> com.zerotoler.rpgmenu.domain.model.progress.MailProgress) {
        progressRepository.updateMail(transform)
    }
}

class LocalRewardService(
    private val rewardGrantUseCase: RewardGrantUseCase,
) : RewardService {
    override suspend fun grant(bundle: RewardBundle) {
        rewardGrantUseCase(bundle)
    }
}

class LocalShopService(
    private val progressRepository: PlayerProgressRepository,
    private val rewardService: RewardService,
) : ShopService {
    private val offers: List<ShopOffer> = listOf(
        ShopOffer(
            id = "shop_feat_bundle",
            title = "Featured Starter Cache",
            subtitle = "Gold, gems, and BP XP",
            priceGold = 0L,
            priceGems = 40,
            bundle = RewardBundle(gold = 2500L, gems = 5, battlePassXp = 200),
            category = ShopOfferCategory.Featured,
        ),
        ShopOffer(
            id = "shop_gold_satchel",
            title = "Gold Satchel",
            subtitle = "Reliable coin stack",
            priceGold = 800L,
            bundle = RewardBundle(gold = 3200L, battlePassXp = 40),
            category = ShopOfferCategory.Featured,
        ),
        ShopOffer(
            id = "shop_premium_chest",
            title = "Premium Chest",
            subtitle = "Keys + roll table rewards",
            priceGold = 1500L,
            bundle = RewardBundle(
                gold = 600L,
                gems = 2,
                chestKeys = 2,
                battlePassXp = 60,
                shardsByPartId = mapOf("cap_aiolos" to 6),
            ),
            category = ShopOfferCategory.Chest,
        ),
        ShopOffer(
            id = "shop_parts_pack",
            title = "Parts Surplus",
            subtitle = "Shards for workshop",
            priceGold = 950L,
            bundle = RewardBundle(
                gold = 400L,
                shardsByPartId = mapOf(
                    "ring_spread" to 4,
                    "driver_agility" to 4,
                ),
                battlePassXp = 35,
            ),
            category = ShopOfferCategory.Parts,
        ),
        ShopOffer(
            id = "shop_resource_box",
            title = "Resource Box",
            subtitle = "Mixed crafting materials",
            priceGold = 600L,
            bundle = RewardBundle(
                gold = 900L,
                gems = 1,
                battlePassXp = 25,
            ),
            category = ShopOfferCategory.Parts,
        ),
    )

    override fun observeOffers(): Flow<List<ShopOffer>> = flowOf(offers)

    override suspend fun purchase(offerId: String): ShopPurchaseResult {
        val offer = offers.find { it.id == offerId }
            ?: return ShopPurchaseResult.Failure("Unknown offer")
        val wallet = progressRepository.snapshotWallet()
        if (offer.priceGold > 0L && wallet.gold < offer.priceGold) {
            return ShopPurchaseResult.Failure("Not enough gold")
        }
        if (offer.priceGems > 0 && wallet.gems < offer.priceGems) {
            return ShopPurchaseResult.Failure("Not enough gems")
        }
        if (offer.priceGold > 0L || offer.priceGems > 0) {
            progressRepository.mutateWallet { w ->
                w.copy(
                    gold = w.gold - offer.priceGold,
                    gems = w.gems - offer.priceGems,
                )
            }
        }
        rewardService.grant(offer.bundle)
        return ShopPurchaseResult.Success
    }
}

class LocalBossService : BossService {
    override fun observeBosses(): Flow<List<BossDefinition>> = flowOf(BossCatalog.all)

    override suspend fun getBoss(bossId: String): BossDefinition? = BossCatalog.byId(bossId)
}

class LocalSocialService : SocialService {
    override fun observeFriends(): Flow<List<SocialFriendCard>> =
        flowOf(
            listOf(
                SocialFriendCard("f1", "Astra", "In lobby · Sim", online = true),
                SocialFriendCard("f2", "Cobalt", "Idle", online = false),
                SocialFriendCard("f3", "Miko", "Sparring", online = true),
            ),
        )
}
