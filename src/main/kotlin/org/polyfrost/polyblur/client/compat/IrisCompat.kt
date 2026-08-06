package org.polyfrost.polyblur.client.compat

import org.polyfrost.polyblur.client.blur.FrameClock
import java.lang.reflect.Method

object IrisCompat {
    private val probe: Probe? by lazy { resolve() }

    private var cachedFrame = -1L
    private var cached = false

    @JvmStatic
    val shadersActive: Boolean
        get() {
            val probe = probe ?: return false
            val frame = FrameClock.frame
            if (frame != cachedFrame) {
                cached = probe.query()
                cachedFrame = frame
            }
            return cached
        }

    private fun resolve(): Probe? = try {
        val apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi")
        val instance = apiClass.getMethod("getInstance").invoke(null)
        Probe(apiClass.getMethod("isShaderPackInUse"), instance)
    } catch (_: Throwable) {
        null
    }

    private class Probe(private val method: Method, private val instance: Any) {
        fun query(): Boolean = try {
            method.invoke(instance) as Boolean
        } catch (_: Throwable) {
            false
        }
    }
}
