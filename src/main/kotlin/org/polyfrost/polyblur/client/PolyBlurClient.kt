package org.polyfrost.polyblur.client

import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.utils.v1.dsl.addDefaultCommand
import org.polyfrost.polyblur.PolyBlurConstants

object PolyBlurClient {
    fun initialize() {
        PolyBlurConfig.preload()
        CommandManager.register(PolyBlurConfig.addDefaultCommand(PolyBlurConstants.ID))
    }
}
