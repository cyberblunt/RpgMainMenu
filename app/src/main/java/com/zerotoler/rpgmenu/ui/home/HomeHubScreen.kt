package com.zerotoler.rpgmenu.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zerotoler.rpgmenu.navigation.Routes
import com.zerotoler.rpgmenu.ui.mainmenu.ChatTicker
import com.zerotoler.rpgmenu.ui.mainmenu.HeroStage
import com.zerotoler.rpgmenu.ui.mainmenu.ModeGrid
import com.zerotoler.rpgmenu.ui.mainmenu.TopHud
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd

@Composable
fun HomeHubScreen(
    viewModel: HomeHubViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyBackground, NavyBackgroundEnd),
                ),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopHud(
                playerName = state.displayName,
                goldAmount = state.gold.toString(),
                gemAmount = state.gems.toString(),
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onGoldClick = { navController.navigate(Routes.GOLD) },
                onGoldPlusClick = { navController.navigate(Routes.GOLD_PLUS) },
                onGemsClick = { navController.navigate(Routes.GEMS) },
                onGemsPlusClick = { navController.navigate(Routes.GEMS_PLUS) },
                onMerchantClick = { navController.navigate(Routes.MERCHANT) },
                onEventsClick = { navController.navigate(Routes.EVENTS) },
                onMissionsClick = { navController.navigate(Routes.ACADEMY) },
                onMenuClick = { navController.navigate(Routes.MENU) },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                HeroStage(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .fillMaxWidth(),
                )

                ChatTicker(
                    message = state.assistantLine,
                    onClick = { navController.navigate(Routes.CHAT_TICKER) },
                )

                ModeGrid(
                    onEmpty1 = { navController.navigate(Routes.ACADEMY) },
                    onEmpty2 = { navController.navigate(Routes.BATTLE_PASS) },
                    onEmpty3 = { navController.navigate(Routes.EVENT_MISSIONS) },
                    onEmpty4 = { navController.navigate(Routes.MAIL) },
                    onAdventure = {
                        navController.navigate(Routes.preBattle("practice", "none"))
                    },
                    onSuperChampionship = { navController.navigate(Routes.RANKED_LADDER) },
                    onFreeChest = { navController.navigate(Routes.CHEST) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.34f, fill = true)
                        .heightIn(min = 152.dp, max = 240.dp)
                        .padding(bottom = 4.dp),
                )
            }
        }
    }
}
