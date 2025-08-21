package org.polyfrost.polyblur.client.blur.phosphor

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.common.OmniIdentifier
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig

object PhosphorBlur {
    private val LOGGER = LogManager.getLogger(PhosphorBlur::class.java)

    private val LOCATION by lazy { OmniIdentifier.create(PolyBlurConstants.ID, "phosphor_motion_blur") }

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            OmniClient.getInstance().shaderManager.getPostChain(LOCATION, BlurTargetBundle.TARGETS)
        } catch (e: JsonSyntaxException) {
            LOGGER.error("Could not load motion blur: ", e)
            null
        }

        val prevTarget = RenderTargetTracker.prevTarget
        if (prevTarget == null) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return // wait until second frame so that we can have a comparison
        }

        val builder = FrameGraphBuilder()
        val targetBundle = BlurTargetBundle(
            builder.importExternal("main", renderTarget),
            builder.importExternal("previous", prevTarget)
        )

        shader?.addToFrame(
            builder,
            renderTarget.width, renderTarget.height,
            targetBundle
        ) { renderPass ->
            renderPass.setUniform("Strength", currentBlendFactor)
        }

        builder.execute(resourcePool)

        // Capture frame so that we can compare next frame
        RenderTargetTracker.captureIntoPrevious(renderTarget)
    }
}
