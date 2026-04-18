package com.zerotoler.rpgmenu.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zerotoler.rpgmenu.domain.model.shop.ShopOffer
import com.zerotoler.rpgmenu.domain.model.shop.ShopOfferCategory
import com.zerotoler.rpgmenu.ui.theme.CyanGlow
import com.zerotoler.rpgmenu.ui.theme.NavyBackground
import com.zerotoler.rpgmenu.ui.theme.NavyBackgroundEnd
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary
import com.zerotoler.rpgmenu.ui.theme.YellowAccent

@Composable
fun ShopScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBackground, NavyBackgroundEnd)))
            .padding(16.dp),
    ) {
        Text("Shop", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text(
            "Catalog preview only — purchasing is disabled in this build.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Gold ${state.gold}", color = YellowAccent, style = MaterialTheme.typography.titleMedium)
            Text("Gems ${state.gems}", color = CyanGlow, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text("Featured", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            }
            items(state.offers.filter { it.category == ShopOfferCategory.Featured }, key = { it.id }) { o ->
                OfferCardPreview(o)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Chests & boxes", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            }
            items(state.offers.filter { it.category == ShopOfferCategory.Chest }, key = { it.id }) { o ->
                OfferCardPreview(o)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Parts & resources", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            }
            items(state.offers.filter { it.category == ShopOfferCategory.Parts }, key = { it.id }) { o ->
                OfferCardPreview(o)
            }
        }
    }
}

@Composable
private fun OfferCardPreview(offer: ShopOffer) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF152238),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(offer.title, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            Text(offer.subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val price = buildString {
                    if (offer.priceGold > 0) append("${offer.priceGold} gold")
                    if (offer.priceGold > 0 && offer.priceGems > 0) append(" · ")
                    if (offer.priceGems > 0) append("${offer.priceGems} gems")
                    if (offer.priceGold == 0L && offer.priceGems == 0) append("Free")
                }
                Text(price, color = YellowAccent, style = MaterialTheme.typography.labelMedium)
                Text(
                    "Unavailable",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                )
            }
        }
    }
}
