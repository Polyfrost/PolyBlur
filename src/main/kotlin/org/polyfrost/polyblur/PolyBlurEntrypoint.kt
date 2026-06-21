package org.polyfrost.polyblur

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.polyblur.client.PolyBlurClient

class PolyBlurEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        PolyBlurClient.initialize()
    }
}
