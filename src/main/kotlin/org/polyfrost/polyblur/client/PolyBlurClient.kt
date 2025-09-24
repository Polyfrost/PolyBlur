package org.polyfrost.polyblur.client

import com.mojang.brigadier.Command
import dev.deftu.omnicore.api.client.commands.OmniClientCommands
import dev.deftu.omnicore.api.client.commands.command
import org.polyfrost.oneconfig.utils.v1.dsl.openUI
import org.polyfrost.polyblur.PolyBlurConstants

object PolyBlurClient {
    fun initialize() {
        PolyBlurConfig.preload()

        OmniClientCommands.command(PolyBlurConstants.ID) {
            runs {
                PolyBlurConfig.openUI()
                Command.SINGLE_SUCCESS
            }
        }
    }
}
