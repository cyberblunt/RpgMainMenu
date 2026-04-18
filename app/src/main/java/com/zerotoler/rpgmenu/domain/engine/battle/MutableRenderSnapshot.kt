package com.zerotoler.rpgmenu.domain.engine.battle

/**
 * Arena rendering buffer mutated by [BattleEngine.writeRender]. Thread: single simulation owner.
 * No per-frame heap churn: fixed float arrays for damage floaters.
 */
class MutableRenderSnapshot {
    var playerX: Float = 0f
    var playerY: Float = 0f
    var enemyX: Float = 0f
    var enemyY: Float = 0f
    var playerAngle: Float = 0f
    var enemyAngle: Float = 0f
    var playerSpin: Float = 1f
    var enemySpin: Float = 1f
    var playerHitFlash: Float = 0f
    var enemyHitFlash: Float = 0f
    var floaterCount: Int = 0
    val floaterX: FloatArray = FloatArray(MAX_FLOATERS)
    val floaterY: FloatArray = FloatArray(MAX_FLOATERS)
    val floaterAlpha: FloatArray = FloatArray(MAX_FLOATERS)

    companion object {
        const val MAX_FLOATERS = 16
    }
}
