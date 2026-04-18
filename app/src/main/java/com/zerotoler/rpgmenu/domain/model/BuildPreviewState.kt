package com.zerotoler.rpgmenu.domain.model

data class BuildPreviewState(
    val battleCap: PartBase?,
    val weightRing: PartBase?,
    val driver: PartBase?,
    val totalStats: PartStats,
    val derivedTags: List<String>,
)
