package org.polyfrost.polyblur

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.polyblur.blurs.phosphor.PhosphorBlur

object PolyBlurConfig : KtConfig(
    id = "${PolyBlur.ID}.json",
    title = PolyBlur.NAME,
    category = Category.COMBAT,
    icon = "/polyblur_dark.svg"
) {

    var forceDisableFastRender by switch(def = true, name = "Force Disable Fast Render", description = "Forces OptiFine's Fast Render option to be disabled.")

    var enabled by switch(def = true, name = "Enabled")

    var mode by dropdown(options = arrayOf("Monkey Blur", "Phospor Blur", "Moulberry Blur"), def = 1, name = "Blur Mode")

    var strength by slider(min = 1f, max = 10f, def = 3f, name = "Blur Strength")

    init {
        addCallback("strength") {
            if (mode == 1) {
                PhosphorBlur.reloadBlur()
            }
        }

        addCallback("mode", PhosphorBlur::reloadIntensity)
    }

}
