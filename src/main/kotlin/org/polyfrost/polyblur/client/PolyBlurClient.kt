package org.polyfrost.polyblur.client

import dev.deftu.omnicore.client.OmniClientCommands
import dev.deftu.omnicore.client.OmniClientCommands.command
import dev.deftu.omnicore.client.OmniClientCommands.does
import org.polyfrost.oneconfig.utils.v1.dsl.openUI
import org.polyfrost.polyblur.PolyBlurConstants

object PolyBlurClient {
    fun initialize() {
        PolyBlurConfig.preload()

        OmniClientCommands.command(PolyBlurConstants.ID) {
            does {
                PolyBlurConfig.openUI()
                1
            }
        }
    }
}
