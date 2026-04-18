package com.zerotoler.rpgmenu.domain.corebattle.math

import kotlin.math.sqrt

/**
 * Minimal 2D vector for a pure Kotlin simulation.
 *
 * Notes:
 * - Mutable for performance (the sim updates many vectors per frame).
 * - Avoids any framework dependencies (no Android/LibGDX vector types).
 */
data class Vec2(var x: Float, var y: Float) {
    fun set(nx: Float, ny: Float): Vec2 {
        x = nx
        y = ny
        return this
    }

    fun set(o: Vec2): Vec2 = set(o.x, o.y)

    fun plus(o: Vec2): Vec2 = Vec2(x + o.x, y + o.y)
    fun minus(o: Vec2): Vec2 = Vec2(x - o.x, y - o.y)
    fun times(s: Float): Vec2 = Vec2(x * s, y * s)

    fun addInPlace(o: Vec2): Vec2 {
        x += o.x
        y += o.y
        return this
    }

    fun subInPlace(o: Vec2): Vec2 {
        x -= o.x
        y -= o.y
        return this
    }

    fun scaleInPlace(s: Float): Vec2 {
        x *= s
        y *= s
        return this
    }

    fun dot(o: Vec2): Float = x * o.x + y * o.y

    fun lengthSq(): Float = x * x + y * y
    fun length(): Float = sqrt(lengthSq())

    fun normalized(eps: Float = 1e-6f): Vec2 {
        val len = length()
        return if (len > eps) Vec2(x / len, y / len) else Vec2(0f, 1f)
    }

    fun normalizeInPlace(eps: Float = 1e-6f): Vec2 {
        val len = length()
        if (len > eps) {
            x /= len
            y /= len
        } else {
            x = 0f
            y = 1f
        }
        return this
    }

    fun distanceTo(o: Vec2): Float = sqrt(distanceSqTo(o))
    fun distanceSqTo(o: Vec2): Float {
        val dx = o.x - x
        val dy = o.y - y
        return dx * dx + dy * dy
    }
}

