package org.polyfrost.polyblur.client

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.polyblur.PolyBlurConstants

object PolyBlurConfig : KtConfig(
    id = "${PolyBlurConstants.ID}.json",
    title = PolyBlurConstants.NAME,
    category = Category.COMBAT,
    icon = "/assets/polyblur/polyblur_dark.svg"
) {
    var isEnabled by switch(def = true, name = "Enabled")
    var blurType by dropdown(options = arrayOf("Phosphor", "Unity"), def = 1, name = "Blur Type")
    var strength by slider(min = 1f, max = 10f, def = 3f, name = "Blur Strength")
    var motionBlurSamples by slider(min = 4f, max = 32f, def = 12f, name = "Motion Blur Samples")

    //? if >=1.21.5 {
    var velocityBuffer by switch(def = true, name = "Velocity Buffer")
    var translationParallax by switch(def = true, name = "Translation Parallax")
    var blurHand by switch(def = true, name = "Blur Hand")
    //?}
}
