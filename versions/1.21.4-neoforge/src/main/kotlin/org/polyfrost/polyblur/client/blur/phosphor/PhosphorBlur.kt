package org.polyfrost.polyblur.client.blur.phosphor

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import dev.deftu.omnicore.api.client.client
import dev.deftu.omnicore.api.identifierOrThrow
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import kotlin.math.max
import kotlin.math.min

object PhosphorBlur {
    private val LOGGER = LogManager.getLogger(PhosphorBlur::class.java)

    private val LOCATION by lazy { identifierOrThrow(PolyBlurConstants.ID, "phosphor_motion_blur") }

    private val currentBlendFactor: Float
        get() {
            val strength = PolyBlurConfig.strength
            val mapped = (strength / 10f) + 0.1f
            return min(1.0f, max(0.1f, mapped))
        }

    private var prevFramebuffer: RenderTarget? = null
    private var prevWidth = 0
    private var prevHeight = 0

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            client.shaderManager.getPostChain(LOCATION, BlurFramebufferSet.TARGETS)
        } catch (e: JsonSyntaxException) {
            LOGGER.error("Could not load motion blur: ", e)
            null
        }

        if (prevFramebuffer == null || (prevFramebuffer?.viewWidth != renderTarget.viewWidth || prevFramebuffer?.viewHeight != renderTarget.viewHeight)) {
            prevFramebuffer = TextureTarget(renderTarget.viewWidth, renderTarget.viewHeight, false)
            blit(renderTarget, prevFramebuffer!!)
        }

        // Blit the current framebuffer to the previous one
        val prevFramebuffer = prevFramebuffer ?: return

        shader?.setUniform("Strength", currentBlendFactor)

        prevWidth = renderTarget.viewWidth
        prevHeight = renderTarget.viewHeight

        val builder = FrameGraphBuilder()
        val framebufferSet = BlurFramebufferSet(
            builder.importExternal("main", renderTarget),
            builder.importExternal("previous", prevFramebuffer)
        )

        shader?.addToFrame(
            builder,
            renderTarget.viewWidth, renderTarget.viewHeight,
            framebufferSet
        )

        builder.execute(resourcePool)

        blit(renderTarget, prevFramebuffer)
    }

    private fun blit(
        inputTarget: RenderTarget,
        outputTarget: RenderTarget,
    ) {
        inputTarget.bindRead()
        outputTarget.bindWrite(true)
        inputTarget.blitToScreen(0, 0)
        outputTarget.unbindWrite()
        inputTarget.unbindRead()
    }
}
