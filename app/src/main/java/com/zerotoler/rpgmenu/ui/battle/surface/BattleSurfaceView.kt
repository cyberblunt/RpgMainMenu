package com.zerotoler.rpgmenu.ui.battle.surface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.zerotoler.rpgmenu.domain.engine.BattleEngine
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import java.util.concurrent.atomic.AtomicReference

class BattleSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val renderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.TRANSPARENT }

    private var thread: BattleSurfaceThread? = null
    private var engineRef: AtomicReference<BattleEngine?> = AtomicReference(null)
    private val lastSnapshot = AtomicReference(BattleRenderSnapshot.empty())

    private var onSnapshot: ((BattleRenderSnapshot) -> Unit)? = null

    init {
        holder.addCallback(this)
        // Gameplay should be BEHIND Compose overlays (win/loss, HUD).
        setZOrderOnTop(false)
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
    }

    fun setEngine(engine: BattleEngine?, onSnapshot: ((BattleRenderSnapshot) -> Unit)?) {
        engineRef.set(engine)
        this.onSnapshot = onSnapshot
    }

    fun setPaused(paused: Boolean) {
        thread?.setPaused(paused)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread?.requestStop()
        thread = BattleSurfaceThread(
            holder = holder,
            engineRef = engineRef,
            lastSnapshot = lastSnapshot,
            onSnapshot = { snap -> onSnapshot?.invoke(snap) },
            drawFrame = { canvas, snap -> drawFrame(canvas, snap) },
        ).also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread?.requestStop()
        thread = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eng = engineRef.get() ?: return false
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            eng.beginAim()
            updateAim(eng, event.x, event.y)
            return true
        }
        if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            updateAim(eng, event.x, event.y)
            return true
        }
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            updateAim(eng, event.x, event.y)
            eng.releaseLaunch()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun updateAim(eng: BattleEngine, x: Float, y: Float) {
        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)
        val cx = w * 0.5f
        val cy = h * 0.5f
        eng.updateAimDirection(x - cx, y - cy)
    }

    private fun drawFrame(canvas: Canvas, snap: BattleRenderSnapshot) {
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

        // Simple renderer: arena + tops + aim line (SurfaceView is gameplay-only; HUD remains Compose).
        val w = canvas.width.toFloat().coerceAtLeast(1f)
        val h = canvas.height.toFloat().coerceAtLeast(1f)
        val minSide = kotlin.math.min(w, h)
        val arenaRadiusPx = minSide * 0.48f
        val scale = arenaRadiusPx / snap.arenaRadius.coerceAtLeast(0.25f)
        val cx = w * 0.5f + snap.screenShakeX
        val cy = h * 0.5f + snap.screenShakeY

        // Stabilization levels (visual markers): center dot + inner/outer grooves (red).
        val arenaR = snap.arenaRadius * scale
        val innerR = arenaR * 0.37f
        val outerR = arenaR * 0.74f
        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = (arenaR * 0.04f).coerceIn(6f, 18f)
        renderPaint.color = Color.argb(45, 255, 50, 50)
        canvas.drawCircle(cx, cy, outerR, renderPaint)
        canvas.drawCircle(cx, cy, innerR, renderPaint)
        renderPaint.style = Paint.Style.FILL
        renderPaint.color = Color.argb(55, 255, 50, 50)
        canvas.drawCircle(cx, cy, (arenaR * 0.04f).coerceIn(10f, 18f), renderPaint)

        // Arena boundary
        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = 6f
        renderPaint.color = Color.argb(120, 0, 229, 255)
        canvas.drawCircle(cx, cy, arenaR, renderPaint)

        fun drawTop(px: Float, py: Float, r: Float, color: Int) {
            val x = cx + px * scale
            val y = cy + py * scale
            renderPaint.style = Paint.Style.FILL
            renderPaint.color = color
            canvas.drawCircle(x, y, (r * scale).coerceAtLeast(4f), renderPaint)
        }

        fun drawSpinMarker(px: Float, py: Float, r: Float, angleRad: Float, color: Int) {
            val x = cx + px * scale
            val y = cy + py * scale
            val rr = (r * scale).coerceAtLeast(4f)
            val ex = x + kotlin.math.cos(angleRad) * rr * 0.85f
            val ey = y + kotlin.math.sin(angleRad) * rr * 0.85f
            renderPaint.style = Paint.Style.STROKE
            renderPaint.strokeWidth = (2.5f + rr * 0.08f).coerceIn(2.5f, 7f)
            renderPaint.color = Color.WHITE
            canvas.drawLine(x, y, ex, ey, renderPaint)
            renderPaint.style = Paint.Style.FILL
            renderPaint.color = color
            canvas.drawCircle(x, y, rr, renderPaint)
        }

        drawSpinMarker(snap.enemyX, snap.enemyY, snap.enemyRadius, snap.enemyAngle, Color.argb(220, 224, 64, 251))
        drawSpinMarker(snap.playerX, snap.playerY, snap.playerRadius, snap.playerAngle, Color.argb(220, 0, 229, 255))

        if (snap.phase == com.zerotoler.rpgmenu.domain.model.battle.BattlePhase.LAUNCH && snap.aimActive) {
            val sx = cx + snap.playerX * scale
            val sy = cy + snap.playerY * scale
            val arrowLen = (snap.playerRadius * scale * 5.5f).coerceIn(60f, 240f)
            val ex = sx + snap.aimDirX * arrowLen
            val ey = sy + snap.aimDirY * arrowLen
            renderPaint.style = Paint.Style.STROKE
            renderPaint.strokeWidth = 5f
            renderPaint.color = Color.argb(230, 0, 229, 255)
            canvas.drawLine(sx, sy, ex, ey, renderPaint)
        }
    }
}

