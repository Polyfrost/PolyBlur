package org.polyfrost.polyblur.client.blur

object PhosphorFeedback {
    private fun base(mode: Int, strength: Float): Float = when (mode) {
        0 -> (0.7f + (strength / 100f) * 3f - 0.01f).coerceIn(0f, 1f)
        2 -> (strength / 10f).coerceIn(0f, 1f)
        else -> ((strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)
    }

    @JvmStatic
    fun of(mode: Int, strength: Float): Float {
        val decayed = Math.pow(base(mode, strength).toDouble(), FrameClock.decayExponent.toDouble()).toFloat()
        return if (mode == 1) decayed.coerceAtMost(0.95f) else decayed
    }
}
