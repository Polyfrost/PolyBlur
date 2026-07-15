package org.polyfrost.polyblur.client.blur

//? if >1.21.5 {
import org.apache.logging.log4j.LogManager
//? if <26.2
import org.lwjgl.opengl.GL11C

object BlurProfiler {
    private val logger = LogManager.getLogger("PolyBlur/Profiler")

    @JvmStatic
    val enabled: Boolean = flag("polyblur.profile", "POLYBLUR_PROFILE")

    private val syncMode: Boolean = flag("polyblur.profile.sync", "POLYBLUR_PROFILE_SYNC")

    private fun flag(property: String, env: String): Boolean =
        java.lang.Boolean.getBoolean(property) || System.getenv(env) == "1"

    private const val REPORT_INTERVAL_FRAMES = 240

    private class Section {
        var cpuNs = 0L
        var cpuMaxNs = 0L
        var calls = 0
        var beginNs = 0L
    }

    private val sections = LinkedHashMap<String, Section>()
    private var current: Section? = null
    private var depth = 0

    private var frames = 0
    private var frameStartNs = 0L
    private var frameAccumNs = 0L
    private var frameMaxNs = 0L
    private var blitsThisFrame = 0
    private var blitAccum = 0

    @JvmStatic
    fun frameStart() {
        if (!enabled) return
        val now = System.nanoTime()
        if (frameStartNs != 0L) {
            val d = now - frameStartNs
            frameAccumNs += d
            if (d > frameMaxNs) frameMaxNs = d
            frames++
            blitAccum += blitsThisFrame
        }
        blitsThisFrame = 0
        frameStartNs = now
        if (frames >= REPORT_INTERVAL_FRAMES) report()
    }

    @JvmStatic
    fun begin(name: String) {
        if (!enabled) return
        if (depth++ > 0) return
        //? if <26.2
        if (syncMode) GL11C.glFinish()
        val s = sections.getOrPut(name) { Section() }
        current = s
        s.beginNs = System.nanoTime()
    }

    @JvmStatic
    fun end() {
        if (!enabled || --depth > 0) return
        val s = current ?: return
        //? if <26.2
        if (syncMode) GL11C.glFinish()
        val d = System.nanoTime() - s.beginNs
        s.cpuNs += d
        if (d > s.cpuMaxNs) s.cpuMaxNs = d
        s.calls++
        current = null
    }

    inline fun <T> section(name: String, block: () -> T): T {
        begin(name)
        try {
            return block()
        } finally {
            end()
        }
    }

    @JvmStatic
    fun countBlit() {
        if (enabled) blitsThisFrame++
    }

    private fun report() {
        val avgFrameMs = frameAccumNs / frames.toDouble() / 1e6
        val sb = StringBuilder()
        sb.append(
            String.format(
                "%.0f fps | frame %.2fms avg / %.2fms max | blur blits %.1f/frame",
                1000.0 / avgFrameMs, avgFrameMs, frameMaxNs / 1e6, blitAccum / frames.toDouble()
            )
        )
        for ((name, s) in sections) {
            if (s.calls == 0) continue
            sb.append(
                String.format(
                    " || %s x%d cpu %.3f/%.3fms",
                    name, s.calls, s.cpuNs / s.calls.toDouble() / 1e6, s.cpuMaxNs / 1e6
                )
            )
            s.cpuNs = 0
            s.cpuMaxNs = 0
            s.calls = 0
        }
        logger.info(sb.toString())
        frames = 0
        frameAccumNs = 0
        frameMaxNs = 0
        blitAccum = 0
    }
}
//?}
