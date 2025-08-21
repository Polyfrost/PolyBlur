package org.polyfrost.polyblur.client.blur.phosphor

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.render.OmniRenderEnv
import dev.deftu.omnicore.common.OmniIdentifier
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.mixin.client.Mixin_ShaderGroup_ShaderListAccessor
import kotlin.math.max
import kotlin.math.min

object PhosphorBlur {
    private val LOGGER = LogManager.getLogger(PhosphorBlur::class.java)

    private val LOCATION by lazy { OmniIdentifier.create(OmniIdentifier.MINECRAFT_NAMESPACE, "shaders/post/phosphor_motion_blur.json") }

    private val currentBlendFactor: Float
        get() {
            val strength = PolyBlurConfig.strength
            val mapped = (strength / 10f) + 0.1f
            return min(1.0f, max(0.1f, mapped))
        }

    private var shader: ShaderGroup? = null
    private var prevWidth = 0
    private var prevHeight = 0
    private var lastBlendFactor = Float.NaN

    @JvmStatic
    val isActive: Boolean
        get() = OmniRenderEnv.isShaderSupported && shader != null

    @JvmStatic
    fun update() {
        if (!OmniRenderEnv.isShaderSupported) {
            return
        }

        val client = OmniClient.getInstance()
        val mainTarget = client.framebuffer ?: return
        val width = mainTarget.framebufferWidth
        val height = mainTarget.framebufferHeight
        if (width <= 0 || height <= 0) {
            return
        }

        val needsRebuild = shader == null || width != prevWidth || height != prevHeight
        if (!needsRebuild) {
            return
        }

        try {
            LOGGER.info("Invalidating Phosphor shader group, rebuilding with new dimensions: {}x{}", width, height)
            //#if MC >= 1.16.5
            //$$ shader?.close()
            //#else
            shader?.deleteShaderGroup()
            //#endif
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
        group.loadShaderGroup(tickDelta)
    }

    fun maybeUpdateBlendFactor() {
        val blendFactor = currentBlendFactor
        if (blendFactor != lastBlendFactor) {
            forceUpdateBlendFactor(blendFactor)
        }
    }

    private fun buildShader(
        target: Framebuffer,
        width: Int,
        height: Int,
    ): ShaderGroup {
        val client = OmniClient.getInstance()
        return ShaderGroup(client.textureManager, client.resourceManager, target, LOCATION).also { group ->
            group.createBindFramebuffers(width, height)
        }
    }

    private fun forceUpdateBlendFactor(blendFactor: Float = currentBlendFactor) {
        val group = shader ?: return
        val shaders = (group as? Mixin_ShaderGroup_ShaderListAccessor)?.listShaders
        if (shaders == null) {
            LOGGER.debug("PhosphorBlur: shader list not accessible; skipping blend factor update")
            return
        }

        for (pass in shaders) {
            val uniform = pass
                //#if MC >= 1.16.5
                //$$ .effect
                //$$ .getUniform("BlendFactor")
                //#else
                .shaderManager
                .getShaderUniform("BlendFactor")
                //#endif
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
            //#if MC >= 1.16.5
            //$$ shader?.close()
            //#else
            shader?.deleteShaderGroup()
            //#endif
        } catch (_: Throwable) {  }

        shader = null
        prevWidth = 0
        prevHeight = 0
        lastBlendFactor = Float.NaN
    }
}
