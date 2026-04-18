package com.zerotoler.rpgmenu.ui.root

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.distinctUntilChanged
import com.zerotoler.rpgmenu.RpgApplication
import com.zerotoler.rpgmenu.navigation.Routes
import com.zerotoler.rpgmenu.ui.boss.BossHubScreen
import com.zerotoler.rpgmenu.ui.boss.BossHubViewModel
import com.zerotoler.rpgmenu.ui.home.HomeHubScreen
import com.zerotoler.rpgmenu.ui.home.HomeHubViewModel
import com.zerotoler.rpgmenu.ui.parts.PartsInventoryScreen
import com.zerotoler.rpgmenu.ui.shop.ShopScreen
import com.zerotoler.rpgmenu.ui.shop.ShopViewModel
import com.zerotoler.rpgmenu.ui.social.SocialHubScreen

@Composable
fun AppRootScaffold(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.applicationContext as RpgApplication
    val activity = LocalContext.current as ComponentActivity
    val rootEntry = requireNotNull(navController.currentBackStackEntry) { "Root back stack entry required" }
    val savedPager = rootEntry.savedStateHandle

    val startPage = resolveRootStartPage(savedPager)
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { RootPagerPages.COUNT },
    )
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val safe = page.coerceIn(0, RootPagerPages.COUNT - 1)
                savedPager[KEY_ROOT_PAGE_V6] = safe
            }
    }

    val partsViewModel: com.zerotoler.rpgmenu.ui.parts.PartsInventoryViewModel = viewModel(
        factory = app.container.partsInventoryViewModelFactory,
    )

    val shopViewModel: ShopViewModel = viewModel(
        factory = app.container.shopViewModelFactory,
    )

    val bossHubViewModel: BossHubViewModel = viewModel(
        factory = app.container.bossHubViewModelFactory,
    )

    val homeViewModel: HomeHubViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = app.container.homeHubViewModelFactory,
    )
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val homeBadge =
        (homeState.unreadMail + homeState.eventMissionsOpen).takeIf { it > 0 }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        HorizontalPager(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            state = pagerState,
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
            reverseLayout = false,
        ) { page ->
            when (page) {
                RootPagerPages.SHOP ->
                    ShopScreen(viewModel = shopViewModel)
                RootPagerPages.PARTS ->
                    PartsInventoryScreen(
                        viewModel = partsViewModel,
                        onOpenPartDetail = { id ->
                            navController.navigate(Routes.partDetail(id))
                        },
                    )
                RootPagerPages.HOME ->
                    HomeHubScreen(
                        viewModel = homeViewModel,
                        navController = navController,
                    )
                RootPagerPages.BOSS ->
                    BossHubScreen(
                        viewModel = bossHubViewModel,
                        navController = navController,
                    )
                RootPagerPages.SOCIAL ->
                    SocialHubScreen(navController = navController)
            }
        }
        GameRootBottomBar(
            pagerState = pagerState,
            homeBadgeCount = homeBadge,
        )
    }
}

private fun resolveRootStartPage(savedPager: androidx.lifecycle.SavedStateHandle): Int {
    val v6 = savedPager.get<Int>(KEY_ROOT_PAGE_V6)
    if (v6 != null) return v6.coerceIn(0, RootPagerPages.COUNT - 1)

    val v5 = savedPager.get<Int>(KEY_ROOT_PAGE_V5)
    if (v5 != null) {
        val migrated = when (v5) {
            0 -> 1
            1 -> 0
            else -> v5
        }.coerceIn(0, RootPagerPages.COUNT - 1)
        savedPager[KEY_ROOT_PAGE_V6] = migrated
        return migrated
    }

    val v4 = savedPager.get<Int>(KEY_ROOT_PAGE_V4)
    if (v4 != null) {
        val mapped = mapSevenTabToFive(v4)
        savedPager[KEY_ROOT_PAGE_V6] = mapped
        return mapped
    }

    val v3 = savedPager.get<Int>(KEY_ROOT_PAGE_V3)
    if (v3 != null) {
        val mapped = mapLegacyFiveTabToFive(v3)
        savedPager[KEY_ROOT_PAGE_V6] = mapped
        return mapped
    }

    val v2 = savedPager.get<Int>(KEY_ROOT_PAGE_V2)
    if (v2 != null) {
        val mapped = when (v2) {
            0 -> RootPagerPages.HOME
            1 -> RootPagerPages.PARTS
            2 -> RootPagerPages.HOME
            3 -> RootPagerPages.HOME
            4 -> RootPagerPages.SOCIAL
            else -> v2.coerceIn(0, RootPagerPages.COUNT - 1)
        }
        savedPager[KEY_ROOT_PAGE_V6] = mapped
        return mapped
    }

    val legacy = savedPager.get<Int>(KEY_ROOT_PAGE_LEGACY)
    return when (legacy) {
        null -> RootPagerPages.HOME
        0 -> RootPagerPages.HOME
        1 -> RootPagerPages.PARTS
        else -> legacy.coerceIn(0, RootPagerPages.COUNT - 1)
    }
}

/** v4 order: PARTS, UPGRADE, SHOP, HOME, BOSS, CHALLENGE, SOCIAL */
private fun mapSevenTabToFive(v4: Int): Int =
    when (v4) {
        0 -> RootPagerPages.PARTS
        1 -> RootPagerPages.HOME
        2 -> RootPagerPages.SHOP
        3 -> RootPagerPages.HOME
        4 -> RootPagerPages.BOSS
        5 -> RootPagerPages.HOME
        6 -> RootPagerPages.SOCIAL
        else -> v4.coerceIn(0, RootPagerPages.COUNT - 1)
    }

/** v3 order: PARTS, UPGRADE, HOME, CHALLENGE, SOCIAL */
private fun mapLegacyFiveTabToFive(v3: Int): Int =
    when (v3) {
        0 -> RootPagerPages.PARTS
        1 -> RootPagerPages.HOME
        2 -> RootPagerPages.HOME
        3 -> RootPagerPages.HOME
        4 -> RootPagerPages.SOCIAL
        else -> v3.coerceIn(0, RootPagerPages.COUNT - 1)
    }

private const val KEY_ROOT_PAGE_V6 = "root_pager_tab_v6"
private const val KEY_ROOT_PAGE_V5 = "root_pager_tab_v5"
private const val KEY_ROOT_PAGE_V4 = "root_pager_tab_v4"
private const val KEY_ROOT_PAGE_V3 = "root_pager_tab_v3"
private const val KEY_ROOT_PAGE_V2 = "root_pager_tab_v2"
private const val KEY_ROOT_PAGE_LEGACY = "root_pager_page"
