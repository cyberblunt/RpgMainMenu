package com.zerotoler.rpgmenu.domain.engine

import kotlin.math.sqrt

data class Vec2(var x: Float, var y: Float) {
    fun set(o: Vec2): Vec2 {
        x = o.x
        y = o.y
        return this
    }

    fun set(nx: Float, ny: Float): Vec2 {
        x = nx
        y = ny
        return this
    }

    fun addScaled(o: Vec2, s: Float): Vec2 {
        x += o.x * s
        y += o.y * s
        return this
    }

    fun length(): Float = sqrt(x * x + y * y)

    fun normalize(): Vec2 {
        val l = length()
        if (l > 1e-6f) {
            x /= l
            y /= l
        } else {
            x = 0f
            y = 1f
        }
        return this
    }

    fun dot(o: Vec2): Float = x * o.x + y * o.y

    companion object {
        fun normalized(dx: Float, dy: Float): Vec2 {
            val v = Vec2(dx, dy)
            return v.normalize()
        }
    }
}
