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
    var strength by slider(min = 1f, max = 10f, def = 3f, name = "Blur Strength")
}
