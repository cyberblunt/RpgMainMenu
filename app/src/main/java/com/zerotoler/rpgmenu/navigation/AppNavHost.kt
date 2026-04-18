package com.zerotoler.rpgmenu.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zerotoler.rpgmenu.RpgApplication
import com.zerotoler.rpgmenu.ui.battle.RealBattleScreen
import com.zerotoler.rpgmenu.ui.battle.RealBattleViewModel
import com.zerotoler.rpgmenu.ui.battleprep.BattleSessionResultScreen
import com.zerotoler.rpgmenu.ui.battleprep.BattleSessionResultViewModel
import com.zerotoler.rpgmenu.ui.battleprep.PreBattleSelectionScreen
import com.zerotoler.rpgmenu.ui.battleprep.PreBattleSelectionViewModel
import com.zerotoler.rpgmenu.ui.parts.PartDetailScreen
import com.zerotoler.rpgmenu.ui.root.AppRootScaffold
import com.zerotoler.rpgmenu.ui.screens.PlaceholderScreen
import com.zerotoler.rpgmenu.ui.screens.academy.AcademyScreen
import com.zerotoler.rpgmenu.ui.screens.battlepass.BattlePassScreen
import com.zerotoler.rpgmenu.ui.screens.chest.ChestScreen
import com.zerotoler.rpgmenu.ui.screens.chest.ChestViewModel
import com.zerotoler.rpgmenu.ui.screens.events.EventBoardScreen
import com.zerotoler.rpgmenu.ui.screens.events.EventMissionsScreen
import com.zerotoler.rpgmenu.ui.screens.events.EventsHubScreen
import com.zerotoler.rpgmenu.ui.screens.mail.MailScreen
import com.zerotoler.rpgmenu.ui.screens.mail.MailViewModel
import com.zerotoler.rpgmenu.ui.screens.mainmenu.MainMenuScreen
import com.zerotoler.rpgmenu.ui.screens.ranked.RankedLadderScreen
import com.zerotoler.rpgmenu.ui.screens.ranked.RankedViewModel
import com.zerotoler.rpgmenu.ui.screens.splash.SplashScreen
import com.zerotoler.rpgmenu.ui.upgrade.CollectionCodexScreen
import com.zerotoler.rpgmenu.ui.upgrade.PartEnhanceScreen
import com.zerotoler.rpgmenu.ui.upgrade.TalentTreeScreen
import com.zerotoler.rpgmenu.ui.upgrade.TalentTreeViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val app = LocalContext.current.applicationContext as RpgApplication

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.ROOT) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ROOT) {
            AppRootScaffold(navController = navController)
        }

        composable(Routes.MAIN) {
            MainMenuScreen(
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenGold = { navController.navigate(Routes.GOLD) },
                onOpenGoldPlus = { navController.navigate(Routes.GOLD_PLUS) },
                onOpenGems = { navController.navigate(Routes.GEMS) },
                onOpenGemsPlus = { navController.navigate(Routes.GEMS_PLUS) },
                onOpenMerchant = { navController.navigate(Routes.MERCHANT) },
                onOpenEvents = { navController.navigate(Routes.EVENTS) },
                onOpenMissions = { navController.navigate(Routes.ACADEMY) },
                onOpenMenu = { navController.navigate(Routes.MENU) },
                onOpenFreeChest = { navController.navigate(Routes.CHEST) },
                onOpenEmptySlot1 = { navController.navigate(Routes.EMPTY_SLOT_1) },
                onOpenEmptySlot2 = { navController.navigate(Routes.EMPTY_SLOT_2) },
                onOpenEmptySlot3 = { navController.navigate(Routes.EMPTY_SLOT_3) },
                onOpenEmptySlot4 = { navController.navigate(Routes.EMPTY_SLOT_4) },
                onOpenAdventure = { navController.navigate(Routes.preBattle("practice", "none")) },
                onOpenSuperChampionship = { navController.navigate(Routes.RANKED_LADDER) },
                onOpenBottomNav1 = { navController.navigate(Routes.BOTTOM_NAV_1) },
                onOpenBottomNav2 = { navController.navigate(Routes.BOTTOM_NAV_2) },
                onOpenBattle = { navController.navigate(Routes.preBattle("practice", "none")) },
                onOpenBottomNav4 = { navController.navigate(Routes.BOTTOM_NAV_4) },
                onOpenBottomNav5 = { navController.navigate(Routes.BOTTOM_NAV_5) },
                onOpenChatTicker = { navController.navigate(Routes.CHAT_TICKER) },
            )
        }

        composable(Routes.PROFILE) {
            PlaceholderScreen(title = "Profile", onBack = { navController.popBackStack() })
        }
        composable(Routes.GOLD) {
            PlaceholderScreen(title = "Gold", onBack = { navController.popBackStack() })
        }
        composable(Routes.GOLD_PLUS) {
            PlaceholderScreen(title = "Gold Plus", onBack = { navController.popBackStack() })
        }
        composable(Routes.GEMS) {
            PlaceholderScreen(title = "Gems", onBack = { navController.popBackStack() })
        }
        composable(Routes.GEMS_PLUS) {
            PlaceholderScreen(title = "Gems Plus", onBack = { navController.popBackStack() })
        }
        composable(Routes.MERCHANT) {
            PlaceholderScreen(title = "Merchant", onBack = { navController.popBackStack() })
        }
        composable(Routes.EVENTS) {
            EventsHubScreen(navController = navController)
        }
        composable(Routes.EVENT_MISSIONS) {
            EventMissionsScreen(navController = navController)
        }
        composable(Routes.EVENT_BOARD) {
            EventBoardScreen(navController = navController)
        }
        composable(Routes.MISSIONS) {
            PlaceholderScreen(title = "Missions", onBack = { navController.popBackStack() })
        }
        composable(Routes.MENU) {
            PlaceholderScreen(title = "System Menu", onBack = { navController.popBackStack() })
        }
        composable(Routes.CHAT_TICKER) {
            PlaceholderScreen(
                title = "System / Chat",
                description = "Feed or chat room would open here.",
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.FREE_CHEST) {
            PlaceholderScreen(title = "Free Chest", onBack = { navController.popBackStack() })
        }
        composable(Routes.CHEST) {
            val vm: ChestViewModel = viewModel(factory = app.container.chestViewModelFactory)
            ChestScreen(viewModel = vm, navController = navController)
        }
        composable(Routes.EMPTY_SLOT_1) {
            PlaceholderScreen(title = "Empty Slot 1", onBack = { navController.popBackStack() })
        }
        composable(Routes.EMPTY_SLOT_2) {
            PlaceholderScreen(title = "Empty Slot 2", onBack = { navController.popBackStack() })
        }
        composable(Routes.EMPTY_SLOT_3) {
            PlaceholderScreen(title = "Empty Slot 3", onBack = { navController.popBackStack() })
        }
        composable(Routes.EMPTY_SLOT_4) {
            PlaceholderScreen(title = "Empty Slot 4", onBack = { navController.popBackStack() })
        }
        composable(Routes.ADVENTURE) {
            PlaceholderScreen(title = "Adventure", onBack = { navController.popBackStack() })
        }
        composable(Routes.SUPER_CHAMPIONSHIP) {
            PlaceholderScreen(
                title = "Super Championship",
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.RANKED_LADDER) {
            val vm: RankedViewModel = viewModel(factory = app.container.rankedViewModelFactory)
            RankedLadderScreen(viewModel = vm, navController = navController)
        }
        composable(Routes.BOTTOM_NAV_1) {
            PlaceholderScreen(title = "Bottom Nav 1", onBack = { navController.popBackStack() })
        }
        composable(Routes.BOTTOM_NAV_2) {
            PlaceholderScreen(title = "Bottom Nav 2", onBack = { navController.popBackStack() })
        }
        composable(Routes.BOTTOM_NAV_BATTLE) {
            PlaceholderScreen(title = "Battle", onBack = { navController.popBackStack() })
        }
        composable(Routes.BOTTOM_NAV_4) {
            PlaceholderScreen(title = "Bottom Nav 4", onBack = { navController.popBackStack() })
        }
        composable(Routes.BOTTOM_NAV_5) {
            PlaceholderScreen(title = "Bottom Nav 5", onBack = { navController.popBackStack() })
        }

        composable(Routes.MAIL) {
            val vm: MailViewModel = viewModel(factory = app.container.mailViewModelFactory)
            MailScreen(viewModel = vm, navController = navController)
        }
        composable(Routes.ACADEMY) {
            AcademyScreen(navController = navController)
        }
        composable(Routes.BATTLE_PASS) {
            BattlePassScreen(navController = navController)
        }
        composable(Routes.TALENT_TREE) {
            val vm: TalentTreeViewModel = viewModel(factory = app.container.talentTreeViewModelFactory)
            TalentTreeScreen(viewModel = vm, navController = navController)
        }
        composable(Routes.COLLECTION_CODEX) {
            CollectionCodexScreen(navController = navController)
        }
        composable(Routes.PART_ENHANCE) {
            PartEnhanceScreen(navController = navController)
        }

        composable(
            route = "${Routes.PART_DETAIL}/{partId}",
            arguments = listOf(navArgument("partId") { type = NavType.StringType }),
        ) { entry ->
            val enc = entry.arguments?.getString("partId").orEmpty()
            val id = URLDecoder.decode(enc, StandardCharsets.UTF_8.toString())
            PartDetailScreen(partId = id, navController = navController)
        }

        composable(
            route = Routes.PRE_BATTLE_SELECTION,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("opponentToken") { type = NavType.StringType },
            ),
        ) { entry ->
            val modeRaw = entry.arguments?.getString("mode").orEmpty()
            val tokenRaw = entry.arguments?.getString("opponentToken").orEmpty()
            val mode = URLDecoder.decode(modeRaw, StandardCharsets.UTF_8.toString())
            val opponentToken = URLDecoder.decode(tokenRaw, StandardCharsets.UTF_8.toString())
            val vm: PreBattleSelectionViewModel = viewModel(
                key = "pre|$mode|$opponentToken",
                factory = app.container.preBattleSelectionViewModelFactory(mode, opponentToken),
            )
            PreBattleSelectionScreen(
                viewModel = vm,
                onNavigateToBattle = { navController.navigate(Routes.BATTLE) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.BATTLE) {
            val vm: RealBattleViewModel = viewModel(factory = app.container.realBattleViewModelFactory)
            RealBattleScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = vm,
                onContinueToSelection = { navController.popBackStack() },
                onSessionComplete = {
                    val sess = app.container.battleSessionRepository.session.value
                    val m = sess?.mode.orEmpty()
                    val t = sess?.opponentToken.orEmpty()
                    navController.navigate(Routes.BATTLE_SESSION_RESULT) {
                        popUpTo(Routes.preBattle(m, t)) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.BATTLE_SESSION_RESULT) {
            val vm: BattleSessionResultViewModel = viewModel(factory = app.container.battleSessionResultViewModelFactory)
            BattleSessionResultScreen(
                viewModel = vm,
                onFinish = { navController.popBackStack() },
                onRetry = { mode, token ->
                    navController.navigate(Routes.preBattle(mode, token)) {
                        popUpTo(Routes.BATTLE_SESSION_RESULT) { inclusive = true }
                    }
                },
            )
        }
    }
}
