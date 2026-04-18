package com.zerotoler.rpgmenu.ui.root

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.ui.mainmenu.NotificationBadge
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.PanelBlueBright
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent
import com.zerotoler.rpgmenu.ui.theme.YellowAccentDark
import kotlinx.coroutines.launch

@Composable
fun GameRootBottomBar(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    homeBadgeCount: Int? = null,
) {
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage.coerceIn(0, RootPagerPages.COUNT - 1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyBackground, NavyBackgroundEnd),
                ),
            )
            .border(1.dp, CyanGlow.copy(alpha = 0.15f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        RootNavItem(
            label = "SHOP",
            glyph = "$",
            selected = page == RootPagerPages.SHOP,
            showDotBadge = true,
            large = page == RootPagerPages.SHOP,
            emphasizeCenterStyle = false,
            onClick = { scope.launch { pagerState.animateScrollToPage(RootPagerPages.SHOP) } },
        )
        RootNavItem(
            label = "PARTS",
            glyph = "P",
            selected = page == RootPagerPages.PARTS,
            badgeCount = null,
            large = page == RootPagerPages.PARTS,
            emphasizeCenterStyle = false,
            onClick = { scope.launch { pagerState.animateScrollToPage(RootPagerPages.PARTS) } },
        )
        RootNavItem(
            label = "HOME",
            glyph = "H",
            selected = page == RootPagerPages.HOME,
            badgeCount = homeBadgeCount,
            large = page == RootPagerPages.HOME,
            emphasizeCenterStyle = true,
            onClick = { scope.launch { pagerState.animateScrollToPage(RootPagerPages.HOME) } },
        )
        RootNavItem(
            label = "BOSS",
            glyph = "B",
            selected = page == RootPagerPages.BOSS,
            showDotBadge = true,
            large = page == RootPagerPages.BOSS,
            emphasizeCenterStyle = false,
            onClick = { scope.launch { pagerState.animateScrollToPage(RootPagerPages.BOSS) } },
        )
        RootNavItem(
            label = "SOC",
            glyph = "S",
            selected = page == RootPagerPages.SOCIAL,
            showDotBadge = true,
            large = page == RootPagerPages.SOCIAL,
            emphasizeCenterStyle = false,
            onClick = { scope.launch { pagerState.animateScrollToPage(RootPagerPages.SOCIAL) } },
        )
    }
}

@Composable
private fun RootNavItem(
    label: String,
    glyph: String,
    selected: Boolean,
    large: Boolean,
    emphasizeCenterStyle: Boolean,
    onClick: () -> Unit,
    badgeCount: Int? = null,
    showDotBadge: Boolean = false,
) {
    val h = if (large) 52.dp else 40.dp
    val w = when {
        large && emphasizeCenterStyle -> 76.dp
        large -> 64.dp
        else -> 48.dp
    }
    val highlightFill = when {
        selected && emphasizeCenterStyle ->
            Brush.verticalGradient(listOf(YellowAccent, YellowAccentDark))
        selected ->
            Brush.verticalGradient(
                listOf(YellowAccent.copy(alpha = 0.85f), YellowAccentDark),
            )
        else ->
            Brush.verticalGradient(
                listOf(PanelBlueBright, PanelBlueBright.copy(alpha = 0.85f)),
            )
    }
    val glyphColor = if (selected) NavyBackground else CyanGlow

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(
            when {
                large && emphasizeCenterStyle -> 76.dp
                large -> 64.dp
                else -> 48.dp
            },
        ),
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(width = w, height = h)
                    .clip(RoundedCornerShape(10.dp))
                    .background(highlightFill)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) YellowAccent else CyanGlow.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    glyph,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = if (large) 11.sp else 9.sp,
                    color = glyphColor,
                )
            }
            when {
                badgeCount != null && badgeCount > 0 -> {
                    NotificationBadge(
                        modifier = Modifier.align(Alignment.TopEnd),
                        count = badgeCount,
                    )
                }
                showDotBadge -> {
                    NotificationBadge(
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = if (selected) TextPrimary else TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
