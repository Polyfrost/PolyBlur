package org.polyfrost.polyblur.client.blur

object FrameClock {
    const val REF_FPS = 240f

    private var lastNs = 0L

    var dt = 1f / REF_FPS
        private set

    val timeScale: Float
        get() = (1f / REF_FPS) / dt

    val decayExponent: Float
        get() = dt * REF_FPS

    @JvmStatic
    fun tick() {
        val now = System.nanoTime()
        if (lastNs != 0L) {
            dt = ((now - lastNs) / 1_000_000_000.0).toFloat().coerceIn(0.001f, 0.1f)
        }
        lastNs = now
    }
}
