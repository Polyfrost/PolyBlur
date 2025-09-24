package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.ResourceHandle
import dev.deftu.omnicore.api.identifierOrThrow
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.client.renderer.PostChain
import net.minecraft.resources.ResourceLocation
import org.polyfrost.polyblur.PolyBlurConstants
import kotlin.collections.plus

class BlurFramebufferSet(
    mainHandle: ResourceHandle<RenderTarget>,
    prevHandle: ResourceHandle<RenderTarget>
) : PostChain.TargetBundle {
    companion object {
        val MAIN_LOCATION = PostChain.MAIN_TARGET_ID
        val PREVIOUS_LOCATION = identifierOrThrow(PolyBlurConstants.ID, "previous")

        val TARGETS = LevelTargetBundle.MAIN_TARGETS + listOf(PREVIOUS_LOCATION)
    }

    private var mainTarget: ResourceHandle<RenderTarget> = mainHandle
    private var prevTarget: ResourceHandle<RenderTarget> = prevHandle

    override fun replace(identifier: ResourceLocation, newHandle: ResourceHandle<RenderTarget>) {
        when (identifier) {
            MAIN_LOCATION -> mainTarget = newHandle
            PREVIOUS_LOCATION -> prevTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $identifier")
        }
    }

    override fun get(identifier: ResourceLocation): ResourceHandle<RenderTarget> {
        return when (identifier) {
            MAIN_LOCATION -> mainTarget
            PREVIOUS_LOCATION -> prevTarget
            else -> throw IllegalArgumentException("Unknown location: $identifier")
        }
    }
}
