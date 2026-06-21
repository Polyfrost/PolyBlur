package org.polyfrost.polyblur.client.blur.phosphor

//? if =1.21.4 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.ResourceHandle
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.client.renderer.PostChain
import net.minecraft.resources.ResourceLocation
import org.polyfrost.polyblur.PolyBlurConstants

class BlurFramebufferSet(
    mainHandle: ResourceHandle<RenderTarget>,
    prevHandle: ResourceHandle<RenderTarget>
) : PostChain.TargetBundle {
    companion object {
        val MAIN_LOCATION: ResourceLocation = PostChain.MAIN_TARGET_ID
        val PREVIOUS_LOCATION: ResourceLocation = location(PolyBlurConstants.ID, "previous")
        val TARGETS = LevelTargetBundle.MAIN_TARGETS + listOf(PREVIOUS_LOCATION)
    }

    private var mainTarget = mainHandle
    private var prevTarget = prevHandle

    override fun replace(identifier: ResourceLocation, newHandle: ResourceHandle<RenderTarget>) {
        when (identifier) {
            MAIN_LOCATION -> mainTarget = newHandle
            PREVIOUS_LOCATION -> prevTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $identifier")
        }
    }

    override fun get(identifier: ResourceLocation): ResourceHandle<RenderTarget> =
        when (identifier) {
            MAIN_LOCATION -> mainTarget
            PREVIOUS_LOCATION -> prevTarget
            else -> throw IllegalArgumentException("Unknown location: $identifier")
        }
}
*///?}

//? if =1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.ResourceHandle
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.client.renderer.PostChain
import net.minecraft.resources.ResourceLocation
import org.polyfrost.polyblur.PolyBlurConstants

class BlurTargetBundle(
    mainHandle: ResourceHandle<RenderTarget>,
    prevHandle: ResourceHandle<RenderTarget>
) : PostChain.TargetBundle {
    companion object {
        private val MAIN_LOCATION: ResourceLocation = LevelTargetBundle.MAIN_TARGET_ID
        private val PREVIOUS_LOCATION: ResourceLocation = location(PolyBlurConstants.ID, "previous")
        val TARGETS = LevelTargetBundle.MAIN_TARGETS + listOf(PREVIOUS_LOCATION)
    }

    private var mainTarget = mainHandle
    private var prevTarget = prevHandle

    override fun replace(location: ResourceLocation, newHandle: ResourceHandle<RenderTarget>) {
        when (location) {
            MAIN_LOCATION -> mainTarget = newHandle
            PREVIOUS_LOCATION -> prevTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $location")
        }
    }

    override fun get(location: ResourceLocation): ResourceHandle<RenderTarget> =
        when (location) {
            MAIN_LOCATION -> mainTarget
            PREVIOUS_LOCATION -> prevTarget
            else -> throw IllegalArgumentException("Unknown location: $location")
        }
}
*///?}
