package org.polyfrost.polyblur.client

import dev.deftu.omnicore.api.client.commands.OmniClientCommands
import dev.deftu.omnicore.api.client.commands.command
import org.polyfrost.oneconfig.utils.v1.dsl.createScreen
import org.polyfrost.polyblur.PolyBlurConstants

object PolyBlurClient {
    fun initialize() {
        PolyBlurConfig.preload()

        OmniClientCommands.command(PolyBlurConstants.ID) {
            runs { ctx ->
                ctx.source.openScreen(PolyBlurConfig.createScreen())
            }
        }
    }
}
