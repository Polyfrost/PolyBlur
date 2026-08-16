package org.polyfrost.polyblur.client

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.polyblur.PolyBlurConstants

object PolyBlurConfig : KtConfig(
    id = "${PolyBlurConstants.ID}.json",
    title = PolyBlurConstants.NAME,
    category = Category.COMBAT,
    icon = "/assets/polyblur/polyblur_dark.svg"
) {
    var isEnabled by switch(def = true, name = "Enabled", description = "Turns PolyBlur on or off.")
    var blurType by dropdown(
        options = arrayOf("Phosphor", "Unity", "Hybrid"),
        def = 2,
        name = "Blur Type",
        description = "Phosphor smears old frames together. Unity blurs along camera movement. Hybrid uses Unity for the world and Phosphor for your hand."
    )
    var phosphorMode by dropdown(
        options = arrayOf("Weighted Max", "Linear Mix", "Alpha Decay"),
        def = 1,
        name = "Phosphor Mode",
        description = "How each frame blends with the last one. Changes how the trail looks and how long it sticks around."
    )
    var strength by slider(
        min = 1f,
        max = 10f,
        def = 3f,
        name = "Blur Strength",
        description = "How strong the blur is."
    )
    var handBlurStrength by slider(
        min = 1f,
        max = 10f,
        def = 5f,
        name = "Hand Blur Strength",
        description = "How strong the blur is on your held item in Hybrid mode."
    )
    var motionBlurSamples by slider(
        min = 4f,
        max = 32f,
        def = 16f,
        name = "Motion Blur Samples",
        description = "How many samples the blur is built from. Higher is smoother but costs more FPS."
    )

    var velocityBuffer by switch(
        def = true,
        name = "Velocity Buffer",
        description = "Tracks per pixel movement for a more accurate blur. Ignored while Iris shaders are on."
    )
    var translationParallax by switch(
        def = true,
        name = "Translation Parallax",
        description = "Also blurs when you walk around, not only when you turn the camera."
    )
    var blurHand by switch(
        def = true,
        name = "Blur Hand",
        description = "Blurs your held item along with the world."
    )

    init {
        hideIf(::phosphorMode) { blurType != 0 && blurType != 2 }
        hideIf(::handBlurStrength) { blurType != 2 }
        hideIf(::blurHand) { blurType == 2 }
    }
}
