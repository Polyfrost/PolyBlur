package org.polyfrost.polyblur.client.blur.phosphor

import com.google.gson.JsonSyntaxException
import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.common.OmniIdentifier
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.PostEffectProcessor
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.DefaultFramebufferSet
import net.minecraft.client.render.FrameGraphBuilder
import net.minecraft.client.util.Pool
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.mixin.client.Mixin_ShaderGroup_ShaderListAccessor
import kotlin.math.max
import kotlin.math.min

object PhosphorBlur {
    private val LOGGER = LogManager.getLogger(PhosphorBlur::class.java)

    private val LOCATION by lazy { OmniIdentifier.create(PolyBlurConstants.ID, "phosphor_motion_blur") }

    private val currentBlendFactor: Float
        get() {
            val strength = PolyBlurConfig.strength
            val mapped = (strength / 10f) + 0.1f
            return min(1.0f, max(0.1f, mapped))
        }

    private var prevFramebuffer: Framebuffer? = null
    private var prevWidth = 0
    private var prevHeight = 0
    private var lastBlendFactor = Float.NaN

    @JvmStatic
    fun render(renderTarget: Framebuffer, resourcePool: Pool) {
        val shader = try {
            OmniClient.getInstance().shaderLoader.loadPostEffect(LOCATION, DefaultFramebufferSet.MAIN_ONLY)
        } catch (e: JsonSyntaxException) {
            LOGGER.error("Could not load motion blur: ", e)
            null
        }

        if (prevFramebuffer == null || (prevFramebuffer?.viewportWidth != renderTarget.viewportWidth || prevFramebuffer?.viewportHeight != renderTarget.viewportHeight)) {
            prevFramebuffer = SimpleFramebuffer(renderTarget.viewportWidth, renderTarget.viewportHeight, false)
        }

        // Blit the current framebuffer to the previous one
        val prevFramebuffer = prevFramebuffer ?: return
        blit(renderTarget, prevFramebuffer)

        val currentBlendFactor = currentBlendFactor
        if (lastBlendFactor != currentBlendFactor && shader != null) {
            for (pass in (shader as Mixin_ShaderGroup_ShaderListAccessor).listShaders) {
                val uniform = pass.program.getUniform("BlendFactor")
                uniform?.set(currentBlendFactor)
            }

            lastBlendFactor = currentBlendFactor
        }

        prevWidth = renderTarget.viewportWidth
        prevHeight = renderTarget.viewportHeight

        val builder = FrameGraphBuilder()
        shader?.render(
            builder,
            renderTarget.viewportWidth, renderTarget.viewportHeight,
            PostEffectProcessor.FramebufferSet.singleton(
                PostEffectProcessor.MAIN,
                builder.createObjectNode("previous", PhosphorBlur.prevFramebuffer)
            )
        )

        builder.run(resourcePool)
    }

    private fun blit(
        inputTarget: Framebuffer,
        outputTarget: Framebuffer,
    ) {
        inputTarget.beginRead()
        outputTarget.beginWrite(true)
        inputTarget.draw(0, 0)
        outputTarget.endWrite()
        inputTarget.endRead()
    }
}
