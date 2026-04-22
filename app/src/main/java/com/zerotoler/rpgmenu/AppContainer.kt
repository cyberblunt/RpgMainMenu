package com.zerotoler.rpgmenu

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.zerotoler.rpgmenu.data.db.AppDatabase
import com.zerotoler.rpgmenu.data.db.migration.MIGRATION_1_2
import com.zerotoler.rpgmenu.data.opponent.LocalOpponentTeamProvider
import com.zerotoler.rpgmenu.data.opponent.OpponentTeamProvider
import com.zerotoler.rpgmenu.data.repository.BattleSessionRepository
import com.zerotoler.rpgmenu.data.repository.impl.InventoryRepositoryImpl
import com.zerotoler.rpgmenu.data.repository.impl.LoadoutRepositoryImpl
import com.zerotoler.rpgmenu.data.repository.impl.LocalBattleSessionRepository
import com.zerotoler.rpgmenu.data.repository.impl.PartCatalogRepositoryImpl
import com.zerotoler.rpgmenu.data.repository.TeamRepository
import com.zerotoler.rpgmenu.data.repository.impl.PlayerProgressRepositoryImpl
import com.zerotoler.rpgmenu.data.repository.impl.TeamRepositoryImpl
import com.zerotoler.rpgmenu.data.service.local.LocalBattlePassService
import com.zerotoler.rpgmenu.data.service.local.LocalBossService
import com.zerotoler.rpgmenu.data.service.local.LocalEventService
import com.zerotoler.rpgmenu.data.service.local.LocalInventoryService
import com.zerotoler.rpgmenu.data.service.local.LocalLeaderboardService
import com.zerotoler.rpgmenu.data.service.local.LocalLoadoutService
import com.zerotoler.rpgmenu.data.service.local.LocalMailService
import com.zerotoler.rpgmenu.data.service.local.LocalMissionService
import com.zerotoler.rpgmenu.data.service.local.LocalProfileService
import com.zerotoler.rpgmenu.data.service.local.LocalRewardService
import com.zerotoler.rpgmenu.data.service.local.LocalShopService
import com.zerotoler.rpgmenu.data.service.local.LocalSocialService
import com.zerotoler.rpgmenu.data.seed.SeedInitializer
import com.zerotoler.rpgmenu.domain.usecase.ComputeBuildStatsUseCase
import com.zerotoler.rpgmenu.domain.usecase.BuildBattleTopFromSelectedLoadoutUseCase
import com.zerotoler.rpgmenu.domain.usecase.CreateBattleSessionUseCase
import com.zerotoler.rpgmenu.domain.usecase.GetBattleReadyTeamUseCase
import com.zerotoler.rpgmenu.domain.usecase.AutoConfigLoadoutUseCase
import com.zerotoler.rpgmenu.domain.usecase.EquipPartUseCase
import com.zerotoler.rpgmenu.domain.usecase.GetVisiblePartsUseCase
import com.zerotoler.rpgmenu.domain.usecase.OpenChestUseCase
import com.zerotoler.rpgmenu.domain.usecase.RewardGrantUseCase
import com.zerotoler.rpgmenu.domain.usecase.UnlockTalentNodeUseCase
import com.zerotoler.rpgmenu.ui.battle.RealBattleViewModel
import com.zerotoler.rpgmenu.ui.battleprep.BattleSessionResultViewModel
import com.zerotoler.rpgmenu.ui.battleprep.PreBattleSelectionViewModel
import com.zerotoler.rpgmenu.ui.boss.BossHubViewModel
import com.zerotoler.rpgmenu.ui.home.HomeHubViewModel
import com.zerotoler.rpgmenu.ui.shop.ShopViewModel
import com.zerotoler.rpgmenu.ui.parts.PartsInventoryViewModel
import com.zerotoler.rpgmenu.ui.screens.chest.ChestViewModel
import com.zerotoler.rpgmenu.ui.screens.mail.MailViewModel
import com.zerotoler.rpgmenu.ui.screens.ranked.RankedViewModel
import com.zerotoler.rpgmenu.ui.upgrade.TalentTreeViewModel

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "rpg_inventory.db",
    ).addMigrations(MIGRATION_1_2)
        .build()

    private val catalogPartDao = database.catalogPartDao()
    private val playerPartDao = database.playerPartDao()
    private val loadoutDao = database.loadoutDao()
    private val teamDao = database.teamDao()

    val catalogRepository = PartCatalogRepositoryImpl(catalogPartDao)
    val inventoryRepository = InventoryRepositoryImpl(playerPartDao)
    val loadoutRepository = LoadoutRepositoryImpl(loadoutDao)
    val teamRepository: TeamRepository = TeamRepositoryImpl(teamDao)

    val playerProgressRepository = PlayerProgressRepositoryImpl(
        database.walletDao(),
        database.kvStoreDao(),
    )

    val computeBuildStatsUseCase = ComputeBuildStatsUseCase()
    val getVisiblePartsUseCase = GetVisiblePartsUseCase()
    val equipPartUseCase = EquipPartUseCase(
        catalogRepository,
        inventoryRepository,
        loadoutRepository,
    )
    val autoConfigLoadoutUseCase = AutoConfigLoadoutUseCase(
        catalogRepository,
        inventoryRepository,
        loadoutRepository,
    )
    val createBattleSessionUseCase = CreateBattleSessionUseCase()
    val buildBattleTopFromSelectedLoadoutUseCase = BuildBattleTopFromSelectedLoadoutUseCase(
        catalogRepository = catalogRepository,
        computeBuildStatsUseCase = computeBuildStatsUseCase,
    )
    val getBattleReadyTeamUseCase = GetBattleReadyTeamUseCase(
        loadoutRepository,
        catalogRepository,
        computeBuildStatsUseCase,
    )
    val opponentTeamProvider: OpponentTeamProvider = LocalOpponentTeamProvider()
    val battleSessionRepository: BattleSessionRepository = LocalBattleSessionRepository(
        playerProgressRepository = playerProgressRepository,
        getBattleReadyTeam = getBattleReadyTeamUseCase,
        createBattleSession = createBattleSessionUseCase,
        opponentTeamProvider = opponentTeamProvider,
    )
    val rewardGrantUseCase = RewardGrantUseCase(playerProgressRepository, inventoryRepository)
    val unlockTalentNodeUseCase = UnlockTalentNodeUseCase(playerProgressRepository)
    val openChestUseCase = OpenChestUseCase(playerProgressRepository, rewardGrantUseCase)

    val profileService = LocalProfileService(playerProgressRepository)
    val inventoryService = LocalInventoryService(inventoryRepository, catalogRepository)
    val loadoutService = LocalLoadoutService(loadoutRepository)
    val leaderboardService = LocalLeaderboardService(playerProgressRepository)
    val missionService = LocalMissionService(playerProgressRepository)
    val eventService = LocalEventService(playerProgressRepository)
    val battlePassService = LocalBattlePassService(playerProgressRepository)
    val mailService = LocalMailService(playerProgressRepository)
    val rewardService = LocalRewardService(rewardGrantUseCase)
    val shopService = LocalShopService(playerProgressRepository, rewardService)
    val bossService = LocalBossService()
    val socialService = LocalSocialService()

    val seedInitializer = SeedInitializer(
        database,
        appContext.getSharedPreferences(PREFS_APP, Context.MODE_PRIVATE),
    )

    val partsInventoryViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(PartsInventoryViewModel::class.java))
                return PartsInventoryViewModel(
                    catalogRepository = catalogRepository,
                    inventoryRepository = inventoryRepository,
                    loadoutRepository = loadoutRepository,
                    teamRepository = teamRepository,
                    playerProgressRepository = playerProgressRepository,
                    computeBuildStatsUseCase = computeBuildStatsUseCase,
                    getVisiblePartsUseCase = getVisiblePartsUseCase,
                    equipPartUseCase = equipPartUseCase,
                    autoConfigLoadoutUseCase = autoConfigLoadoutUseCase,
                ) as T
            }
        }

    val shopViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(ShopViewModel::class.java))
                return ShopViewModel(
                    shopService = shopService,
                    playerProgressRepository = playerProgressRepository,
                ) as T
            }
        }

    val bossHubViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(BossHubViewModel::class.java))
                return BossHubViewModel(
                    bossService = bossService,
                ) as T
            }
        }

    val homeHubViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(HomeHubViewModel::class.java))
                return HomeHubViewModel(
                    playerProgressRepository = playerProgressRepository,
                ) as T
            }
        }

    val rankedViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(RankedViewModel::class.java))
                return RankedViewModel(
                    leaderboardService = leaderboardService,
                    playerProgressRepository = playerProgressRepository,
                ) as T
            }
        }

    val chestViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(ChestViewModel::class.java))
                return ChestViewModel(
                    playerProgressRepository = playerProgressRepository,
                    openChestUseCase = openChestUseCase,
                ) as T
            }
        }

    val mailViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(MailViewModel::class.java))
                return MailViewModel(
                    mailService = mailService,
                    playerProgressRepository = playerProgressRepository,
                    rewardGrantUseCase = rewardGrantUseCase,
                ) as T
            }
        }

    val talentTreeViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(TalentTreeViewModel::class.java))
                return TalentTreeViewModel(
                    playerProgressRepository = playerProgressRepository,
                    unlockTalentNodeUseCase = unlockTalentNodeUseCase,
                ) as T
            }
        }

    fun preBattleSelectionViewModelFactory(mode: String, opponentToken: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(PreBattleSelectionViewModel::class.java))
                return PreBattleSelectionViewModel(
                    mode = mode,
                    opponentToken = opponentToken,
                    repository = battleSessionRepository,
                ) as T
            }
        }

    val realBattleViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(RealBattleViewModel::class.java))
                return RealBattleViewModel(
                    repository = battleSessionRepository,
                    buildBattleTop = buildBattleTopFromSelectedLoadoutUseCase,
                ) as T
            }
        }

    val battleSessionResultViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(BattleSessionResultViewModel::class.java))
                return BattleSessionResultViewModel(
                    repository = battleSessionRepository,
                ) as T
            }
        }

    companion object {
        private const val PREFS_APP = "rpg_app_prefs"
    }
}
