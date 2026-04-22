package com.zerotoler.rpgmenu.ui.battle.surface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
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
    /** Full-canvas fill matching Compose battle gradient (not transparent — SurfaceView would show black). */
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var bgShaderW = -1f
    private var bgShaderH = -1f

    private var thread: BattleSurfaceThread? = null
    private var engineRef: AtomicReference<BattleEngine?> = AtomicReference(null)
    private val lastSnapshot = AtomicReference(BattleRenderSnapshot.empty())

    private var onSnapshot: ((BattleRenderSnapshot) -> Unit)? = null

    init {
        holder.addCallback(this)
        // Gameplay should be BEHIND Compose overlays (win/loss, HUD).
        setZOrderOnTop(false)
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
        setBackgroundColor(Color.TRANSPARENT)
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
        val w = canvas.width.toFloat().coerceAtLeast(1f)
        val h = canvas.height.toFloat().coerceAtLeast(1f)
        if (w != bgShaderW || h != bgShaderH) {
            bgShaderW = w
            bgShaderH = h
            bgPaint.shader = LinearGradient(
                0f,
                0f,
                0f,
                h,
                Color.parseColor("#0A1628"),
                Color.parseColor("#050D18"),
                Shader.TileMode.CLAMP,
            )
        }
        bgPaint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Simple renderer: arena + tops + aim line (SurfaceView is gameplay-only; HUD remains Compose).
        val minSide = kotlin.math.min(w, h)
        // Matches [BattleEngine.arenaRadius] (sim ↔ pixels).
        val arenaRadiusPx = minSide * 0.45f * 1.1f * 3.5f / 3f * 0.95f
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

        if (snap.particles.isNotEmpty()) {
            renderPaint.style = Paint.Style.FILL
            for (pt in snap.particles) {
                val px = cx + pt.x * scale
                val py = cy + pt.y * scale
                val life = pt.life.coerceIn(0f, 1f)
                val baseA = (pt.colorArgb ushr 24) and 0xff
                val alpha = (baseA * life).toInt().coerceIn(0, 255)
                val rgb = pt.colorArgb and 0x00FFFFFF
                renderPaint.color = (alpha shl 24) or rgb
                val pr = (pt.size * scale).coerceIn(1.5f, 18f)
                canvas.drawCircle(px, py, pr, renderPaint)
            }
        }

        fun drawTopBeyblade(px: Float, py: Float, r: Float, angleRad: Float, colorArgb: Int, attacking: Boolean) {
            val topX = cx + px * scale
            val topY = cy + py * scale
            val radiusPx = (r * scale).coerceAtLeast(4f)
            canvas.save()
            canvas.translate(topX, topY)
            canvas.rotate(Math.toDegrees(angleRad.toDouble()).toFloat())
            renderPaint.style = Paint.Style.FILL
            renderPaint.color = Color.parseColor("#222222")
            canvas.drawCircle(0f, 0f, radiusPx, renderPaint)
            renderPaint.style = Paint.Style.STROKE
            renderPaint.strokeWidth = 8f
            renderPaint.color = colorArgb
            canvas.drawCircle(0f, 0f, radiusPx, renderPaint)
            renderPaint.color = Color.parseColor("#666666")
            renderPaint.strokeWidth = 4f
            canvas.drawLine(-radiusPx, 0f, radiusPx, 0f, renderPaint)
            canvas.drawLine(0f, -radiusPx, 0f, radiusPx, renderPaint)
            renderPaint.style = Paint.Style.FILL
            renderPaint.color = if (attacking) Color.WHITE else colorArgb
            val centerR = (12f * scale).coerceIn(3f, 22f)
            canvas.drawCircle(0f, 0f, centerR, renderPaint)
            renderPaint.color = Color.WHITE
            canvas.drawCircle(radiusPx - 8f, 0f, 4f, renderPaint)
            canvas.restore()
        }

        drawTopBeyblade(
            snap.enemyX, snap.enemyY, snap.enemyRadius, snap.enemyAngle,
            Color.argb(220, 224, 64, 251), snap.enemyAttacking,
        )
        drawTopBeyblade(
            snap.playerX, snap.playerY, snap.playerRadius, snap.playerAngle,
            Color.argb(220, 0, 229, 255), snap.playerAttacking,
        )

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

