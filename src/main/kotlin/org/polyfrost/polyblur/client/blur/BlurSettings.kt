package org.polyfrost.polyblur.client.blur

import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.compat.IrisCompat

object BlurSettings {
    @JvmStatic
    val velocityBuffer: Boolean
        get() = PolyBlurConfig.velocityBuffer && !IrisCompat.shadersActive
}
