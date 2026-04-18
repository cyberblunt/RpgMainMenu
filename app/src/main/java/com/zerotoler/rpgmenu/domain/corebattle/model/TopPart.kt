package com.zerotoler.rpgmenu.domain.corebattle.model

/**
 * Parts are immutable "build-time" configuration.
 * Runtime mutable state lives only in [SpinningTop].
 */
sealed interface TopPart {
    val stats: PartStats
    val id: String
}

sealed interface AvatarCore : TopPart
sealed interface AttackRing : TopPart
sealed interface DriverTip : TopPart

