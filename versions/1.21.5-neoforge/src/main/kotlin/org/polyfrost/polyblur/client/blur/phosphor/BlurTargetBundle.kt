package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.ResourceHandle
import dev.deftu.omnicore.common.OmniIdentifier
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.client.renderer.PostChain
import net.minecraft.resources.ResourceLocation
import org.polyfrost.polyblur.PolyBlurConstants

class BlurTargetBundle(
    mainHandle: ResourceHandle<RenderTarget>,
    prevHandle: ResourceHandle<RenderTarget>
) : PostChain.TargetBundle {
    companion object {
        private val MAIN_LOCATION = LevelTargetBundle.MAIN_TARGET_ID
        private val PREVIOUS_LOCATION = OmniIdentifier.create(PolyBlurConstants.ID, "previous")

        val TARGETS = LevelTargetBundle.MAIN_TARGETS + listOf(PREVIOUS_LOCATION)
    }

    private var mainTarget: ResourceHandle<RenderTarget> = mainHandle
    private var prevTarget: ResourceHandle<RenderTarget> = prevHandle

    override fun replace(location: ResourceLocation, newHandle: ResourceHandle<RenderTarget>) {
        when (location) {
            MAIN_LOCATION -> mainTarget = newHandle
            PREVIOUS_LOCATION -> prevTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $location")
        }
    }

    override fun get(arg: ResourceLocation): ResourceHandle<RenderTarget> {
        return when (arg) {
            MAIN_LOCATION -> mainTarget
            PREVIOUS_LOCATION -> prevTarget
            else -> throw IllegalArgumentException("Unknown location: $arg")
        }
    }
}
