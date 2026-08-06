package org.polyfrost.polyblur.client.blur

//? if >=1.21.5 {
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.compat.IrisCompat

object BlurSettings {
    @JvmStatic
    val velocityBuffer: Boolean
        get() = PolyBlurConfig.velocityBuffer && !IrisCompat.shadersActive
}
//?}
