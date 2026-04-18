package com.zerotoler.rpgmenu.domain.model.shop

import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle

enum class ShopOfferCategory {
    Featured,
    Chest,
    Parts,
}

data class ShopOffer(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceGold: Long,
    val priceGems: Int = 0,
    val bundle: RewardBundle,
    val category: ShopOfferCategory,
)
