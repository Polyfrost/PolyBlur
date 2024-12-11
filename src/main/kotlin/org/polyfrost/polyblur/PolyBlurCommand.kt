package org.polyfrost.polyblur

import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Command
import org.polyfrost.oneconfig.utils.v1.dsl.openUI

@Command(PolyBlur.ID)
class PolyBlurCommand {

    @Command
    fun main() {
        PolyBlurConfig.openUI()
    }

}
