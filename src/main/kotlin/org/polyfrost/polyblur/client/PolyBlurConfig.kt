package org.polyfrost.polyblur.client

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur

object PolyBlurConfig : KtConfig(
    id = "${PolyBlurConstants.ID}.json",
    title = PolyBlurConstants.NAME,
    category = Category.COMBAT,
    icon = "/assets/polyblur/polyblur_dark.svg"
) {

//    var forceDisableFastRender by switch(def = true, name = "Force Disable Fast Render", description = "Forces OptiFine's Fast Render option to be disabled.")

    var isEnabled by switch(def = true, name = "Enabled")

//    var mode by dropdown(options = arrayOf("Monkey Blur", "Phosphor Blur", "Moulberry Blur"), def = 1, name = "Blur Mode")
    var mode = 1 // Just hard-code it to Phosphor Blur for now. We are leaving the others out temporarily.

    var strength by slider(min = 1f, max = 10f, def = 3f, name = "Blur Strength")

    init {
        //? if 1.21.1 {
        /*addCallback("isEnabled") {
            if (!isEnabled) {
                PhosphorBlur.destroy()
            } else {
                PhosphorBlur.update()
            }
        }

        addCallback("strength") {
            PhosphorBlur.maybeUpdateBlendFactor()
        }
        *///?}
    }

}
