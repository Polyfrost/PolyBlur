package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.pipeline.RenderTarget
import dev.deftu.omnicore.api.DEFAULT_NAMESPACE
import dev.deftu.omnicore.api.client.client
import dev.deftu.omnicore.api.client.render.GlCapabilities
import dev.deftu.omnicore.api.locationOrThrow
import net.minecraft.client.renderer.PostChain
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.mixin.client.Mixin_ShaderGroup_ShaderListAccessor
import kotlin.math.max
import kotlin.math.min

object PhosphorBlur {
    private val LOGGER = LogManager.getLogger(PhosphorBlur::class.java)

    private val LOCATION by lazy { locationOrThrow(DEFAULT_NAMESPACE, "shaders/post/phosphor_motion_blur.json") }

    private val currentBlendFactor: Float
        get() {
            val strength = PolyBlurConfig.strength
            val mapped = (strength / 10f) + 0.1f
            return min(1.0f, max(0.1f, mapped))
        }

    private var prevWidth = 0
    private var prevHeight = 0


    private var shader: PostChain? = null
    private var lastBlendFactor = Float.NaN

    @JvmStatic
    val isActive: Boolean
        get() = GlCapabilities.isShaderSupported && shader != null

    @JvmStatic
    fun update() {
        if (!GlCapabilities.isShaderSupported) {
            return
        }

        val mainTarget = client.mainRenderTarget ?: return
        val width = mainTarget.width
        val height = mainTarget.height
        if (width <= 0 || height <= 0) {
            return
        }

        val needsRebuild = shader == null || width != prevWidth || height != prevHeight
        if (!needsRebuild) {
            return
        }

        try {
            LOGGER.info("Invalidating Phosphor shader group, rebuilding with new dimensions: {}x{}", width, height)
            shader?.close()
        } catch (_: Throwable) {  }

        try {
            LOGGER.info("Building Phosphor shader group with dimensions: {}x{}", width, height)
            shader = buildShader(target = mainTarget, width = width, height = height)
            LOGGER.info("Phosphor shader group built successfully: $shader")
            prevWidth = width
            prevHeight = height
            forceUpdateBlendFactor()
        } catch (e: Exception) {
            LOGGER.error("Failed to (re)build Phosphor shader", e)
            shader = null
        }
    }

    @JvmStatic
    fun render(tickDelta: Float) {
        if (!isActive) {
            return
        }

        val group = shader ?: return
        maybeUpdateBlendFactor()
        group.process(tickDelta)
    }

    fun maybeUpdateBlendFactor() {
        val blendFactor = currentBlendFactor
        if (blendFactor != lastBlendFactor) {
            forceUpdateBlendFactor(blendFactor)
        }
    }

    private fun buildShader(
        target: RenderTarget,
        width: Int,
        height: Int,
    ): PostChain {
        return PostChain(client.textureManager, client.resourceManager, target, LOCATION).also { group ->
            group.resize(width, height)
        }
    }

    private fun forceUpdateBlendFactor(blendFactor: Float = currentBlendFactor) {
        val group = shader ?: return
        val shaders = (group as? Mixin_ShaderGroup_ShaderListAccessor)?.passes
        if (shaders == null) {
            LOGGER.debug("PhosphorBlur: shader list not accessible; skipping blend factor update")
            return
        }

        for (pass in shaders) {
            val uniform = pass.effect.getUniform("BlendFactor")
            if (uniform == null) {
                LOGGER.debug("PhosphorBlur: uniform 'BlendFactor' missing on pass {}", pass)
                continue
            }

            LOGGER.info("PhosphorBlur: updating blend factor uniform to {}", blendFactor)
            uniform.set(blendFactor)
        }

        lastBlendFactor = blendFactor
    }

    @JvmStatic
    fun destroy() {
        try {
            shader?.close()
        } catch (_: Throwable) {  }

        shader = null
        prevWidth = 0
        prevHeight = 0
        lastBlendFactor = Float.NaN
    }
}
