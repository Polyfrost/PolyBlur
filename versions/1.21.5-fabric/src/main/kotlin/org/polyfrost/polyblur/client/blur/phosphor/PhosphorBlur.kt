package org.polyfrost.polyblur.client.blur.phosphor

import com.google.gson.JsonSyntaxException
import dev.deftu.omnicore.api.client.client
import dev.deftu.omnicore.api.identifierOrThrow
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.render.FrameGraphBuilder
import net.minecraft.client.util.Pool
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig

object PhosphorBlur {
    private val LOGGER = LogManager.getLogger(PhosphorBlur::class.java)

    private val LOCATION by lazy { identifierOrThrow(PolyBlurConstants.ID, "phosphor_motion_blur") }

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: Framebuffer, resourcePool: Pool) {
        val shader = try {
            client.shaderLoader.loadPostEffect(LOCATION, BlurTargetBundle.TARGETS)
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
            builder.createObjectNode("main", renderTarget),
            builder.createObjectNode("previous", prevTarget)
        )

        shader?.render(
            builder,
            renderTarget.viewportWidth, renderTarget.viewportHeight,
            targetBundle
        ) { renderPass ->
            renderPass.setUniform("Strength", currentBlendFactor)
        }

        builder.run(resourcePool)

        // Capture frame so that we can compare next frame
        RenderTargetTracker.captureIntoPrevious(renderTarget)
    }
}
