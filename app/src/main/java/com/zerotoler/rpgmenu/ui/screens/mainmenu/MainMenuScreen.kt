package com.zerotoler.rpgmenu.ui.screens.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.ui.mainmenu.BottomBattleNav
import com.zerotoler.rpgmenu.ui.mainmenu.ChatTicker
import com.zerotoler.rpgmenu.ui.mainmenu.HeroStage
import com.zerotoler.rpgmenu.ui.mainmenu.ModeGrid
import com.zerotoler.rpgmenu.ui.mainmenu.TopHud
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.RpgMainMenuTheme

@Composable
fun MainMenuScreen(
    showBottomBar: Boolean = true,
    onOpenProfile: () -> Unit,
    onOpenGold: () -> Unit,
    onOpenGoldPlus: () -> Unit,
    onOpenGems: () -> Unit,
    onOpenGemsPlus: () -> Unit,
    onOpenMerchant: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenFreeChest: () -> Unit,
    onOpenEmptySlot1: () -> Unit,
    onOpenEmptySlot2: () -> Unit,
    onOpenEmptySlot3: () -> Unit,
    onOpenEmptySlot4: () -> Unit,
    onOpenAdventure: () -> Unit,
    onOpenSuperChampionship: () -> Unit,
    onOpenBottomNav1: () -> Unit,
    onOpenBottomNav2: () -> Unit,
    onOpenBattle: () -> Unit,
    onOpenBottomNav4: () -> Unit,
    onOpenBottomNav5: () -> Unit,
    onOpenChatTicker: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyBackground, NavyBackgroundEnd),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHud(
                playerName = "LEO-GGRAON",
                goldAmount = "180625",
                gemAmount = "2973",
                onProfileClick = onOpenProfile,
                onGoldClick = onOpenGold,
                onGoldPlusClick = onOpenGoldPlus,
                onGemsClick = onOpenGems,
                onGemsPlusClick = onOpenGemsPlus,
                onMerchantClick = onOpenMerchant,
                onEventsClick = onOpenEvents,
                onMissionsClick = onOpenMissions,
                onMenuClick = onOpenMenu,
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
                    message = "deeplysorry is unstoppable in the Survival Mode Novice Arena, and " +
                        "the crowd cannot look away.",
                    onClick = onOpenChatTicker,
                )

                ModeGrid(
                    onEmpty1 = onOpenEmptySlot1,
                    onEmpty2 = onOpenEmptySlot2,
                    onEmpty3 = onOpenEmptySlot3,
                    onEmpty4 = onOpenEmptySlot4,
                    onAdventure = onOpenAdventure,
                    onSuperChampionship = onOpenSuperChampionship,
                    onFreeChest = onOpenFreeChest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.34f, fill = true)
                        .heightIn(min = 152.dp, max = 240.dp)
                        .padding(bottom = 4.dp),
                )
            }

            if (showBottomBar) {
                BottomBattleNav(
                    onNav1 = onOpenBottomNav1,
                    onNav2 = onOpenBottomNav2,
                    onBattle = onOpenBattle,
                    onNav4 = onOpenBottomNav4,
                    onNav5 = onOpenBottomNav5,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800, widthDp = 400)
@Composable
private fun MainMenuScreenPreview() {
    RpgMainMenuTheme {
        MainMenuScreen(
            onOpenProfile = {},
            onOpenGold = {},
            onOpenGoldPlus = {},
            onOpenGems = {},
            onOpenGemsPlus = {},
            onOpenMerchant = {},
            onOpenEvents = {},
            onOpenMissions = {},
            onOpenMenu = {},
            onOpenFreeChest = {},
            onOpenEmptySlot1 = {},
            onOpenEmptySlot2 = {},
            onOpenEmptySlot3 = {},
            onOpenEmptySlot4 = {},
            onOpenAdventure = {},
            onOpenSuperChampionship = {},
            onOpenBottomNav1 = {},
            onOpenBottomNav2 = {},
            onOpenBattle = {},
            onOpenBottomNav4 = {},
            onOpenBottomNav5 = {},
            onOpenChatTicker = {},
        )
    }
}
