package org.polyfrost.polyblur.client.blur.motion

//? if =1.21.4 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.ResourceHandle
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.client.renderer.PostChain
import net.minecraft.resources.ResourceLocation

class MotionTargetBundle(
    mainHandle: ResourceHandle<RenderTarget>
) : PostChain.TargetBundle {
    companion object {
        val MAIN_LOCATION: ResourceLocation = PostChain.MAIN_TARGET_ID
        val TARGETS = LevelTargetBundle.MAIN_TARGETS
    }

    private var mainTarget = mainHandle

    override fun replace(identifier: ResourceLocation, newHandle: ResourceHandle<RenderTarget>) {
        when (identifier) {
            MAIN_LOCATION -> mainTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $identifier")
        }
    }

    override fun get(identifier: ResourceLocation): ResourceHandle<RenderTarget> =
        when (identifier) {
            MAIN_LOCATION -> mainTarget
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

class MotionTargetBundle(
    mainHandle: ResourceHandle<RenderTarget>
) : PostChain.TargetBundle {
    companion object {
        private val MAIN_LOCATION: ResourceLocation = LevelTargetBundle.MAIN_TARGET_ID
        val TARGETS = LevelTargetBundle.MAIN_TARGETS
    }

    private var mainTarget = mainHandle

    override fun replace(location: ResourceLocation, newHandle: ResourceHandle<RenderTarget>) {
        when (location) {
            MAIN_LOCATION -> mainTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $location")
        }
    }

    override fun get(location: ResourceLocation): ResourceHandle<RenderTarget> =
        when (location) {
            MAIN_LOCATION -> mainTarget
            else -> throw IllegalArgumentException("Unknown location: $location")
        }
}
*///?}
