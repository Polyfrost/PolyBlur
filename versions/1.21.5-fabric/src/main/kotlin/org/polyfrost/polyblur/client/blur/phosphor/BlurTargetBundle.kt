package org.polyfrost.polyblur.client.blur.phosphor

import dev.deftu.omnicore.api.identifierOrThrow
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.PostEffectProcessor
import net.minecraft.client.render.DefaultFramebufferSet
import net.minecraft.client.util.Handle
import net.minecraft.util.Identifier
import org.polyfrost.polyblur.PolyBlurConstants
import kotlin.collections.plus

class BlurTargetBundle(
    mainHandle: Handle<Framebuffer>,
    prevHandle: Handle<Framebuffer>
) : PostEffectProcessor.FramebufferSet {
    companion object {
        private val MAIN_LOCATION = DefaultFramebufferSet.MAIN
        private val PREVIOUS_LOCATION = identifierOrThrow(PolyBlurConstants.ID, "previous")

        val TARGETS = DefaultFramebufferSet.MAIN_ONLY + listOf(PREVIOUS_LOCATION)
    }

    private var mainTarget: Handle<Framebuffer> = mainHandle
    private var prevTarget: Handle<Framebuffer> = prevHandle

    override fun set(location: Identifier, newHandle: Handle<Framebuffer>) {
        when (location) {
            MAIN_LOCATION -> mainTarget = newHandle
            PREVIOUS_LOCATION -> prevTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $location")
        }
    }

    override fun get(arg: Identifier): Handle<Framebuffer> {
        return when (arg) {
            MAIN_LOCATION -> mainTarget
            PREVIOUS_LOCATION -> prevTarget
            else -> throw IllegalArgumentException("Unknown location: $arg")
        }
    }
}
