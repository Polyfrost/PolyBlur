package org.polyfrost.polyblur.client.blur

object PhosphorFeedback {
    @JvmStatic
    fun of(mode: Int, strength: Float): Float = when (mode) {
        0 -> 0.69f + strength * 0.03f
        else -> strength / 10f
    }.coerceIn(0f, 0.99f)
}
