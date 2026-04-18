package com.zerotoler.rpgmenu.ui.battle.surface

import android.view.SurfaceHolder
import com.zerotoler.rpgmenu.domain.engine.BattleEngine
import com.zerotoler.rpgmenu.domain.model.battle.BattlePhase
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class BattleSurfaceThread(
    private val holder: SurfaceHolder,
    private val engineRef: AtomicReference<BattleEngine?>,
    private val lastSnapshot: AtomicReference<BattleRenderSnapshot>,
    private val onSnapshot: (BattleRenderSnapshot) -> Unit,
    private val drawFrame: (android.graphics.Canvas, BattleRenderSnapshot) -> Unit,
) : Thread("BattleSurfaceThread") {

    private val running = AtomicBoolean(true)
    private val paused = AtomicBoolean(false)

    fun requestStop() {
        running.set(false)
        interrupt()
    }

    fun setPaused(p: Boolean) {
        paused.set(p)
    }

    override fun run() {
        var lastNano = System.nanoTime()
        var accumulator = 0.0
        val fixedStep = 1.0 / 60.0

        while (running.get()) {
            val eng = engineRef.get()
            if (eng == null) {
                sleepQuiet(16)
                continue
            }

            val now = System.nanoTime()
            val frameDt = ((now - lastNano) / 1e9).coerceAtMost(0.25)
            lastNano = now

            if (!paused.get() && eng.currentPhase() != BattlePhase.RESULT) {
                accumulator += frameDt
                while (accumulator >= fixedStep) {
                    eng.fixedStep(fixedStep.toFloat())
                    accumulator -= fixedStep
                }
            } else if (paused.get()) {
                // Avoid spiral-of-death when resuming.
                accumulator = 0.0
            }

            val snap = eng.buildSnapshot()
            lastSnapshot.set(snap)
            onSnapshot(snap)

            var canvas: android.graphics.Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) drawFrame(canvas, snap)
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }

            sleepQuiet(16)
        }
    }

    private fun sleepQuiet(ms: Long) {
        try {
            sleep(ms)
        } catch (_: InterruptedException) {
        }
    }
}

