package com.zerotoler.rpgmenu.data.seed

import com.zerotoler.rpgmenu.BuildConfig
import com.zerotoler.rpgmenu.domain.model.DebugSeedMode

object DebugInventoryConfig {
    fun currentSeedMode(): DebugSeedMode =
        if (BuildConfig.DEBUG_SEED_UNLOCK_ALL) {
            DebugSeedMode.UNLOCK_ALL_PARTS
        } else {
            DebugSeedMode.STARTER_ONLY
        }
}
