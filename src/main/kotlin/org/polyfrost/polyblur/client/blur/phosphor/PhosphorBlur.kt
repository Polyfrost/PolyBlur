package org.polyfrost.polyblur.client.blur.phosphor

//? if =1.21.1 || =1.8.9 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.pipeline.RenderTarget
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.PostChain
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.PhosphorFeedback
import java.io.IOException
//? if =1.8.9 {
/^import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.render.platform.GLX
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14
import org.polyfrost.polyblur.client.blur.FrameClock
import org.polyfrost.polyblur.mixin.client.PostChainAccessor
^///?}

//? if =1.8.9 {
/^object PhosphorBlur {
    private var previous: RenderTarget? = null
    private var lastFrame = -1L

    @JvmStatic
    val phosphorMode: Int
        get() = PolyBlurConfig.phosphorMode

    @JvmStatic
    val currentStrength: Float
        get() = PhosphorFeedback.of(phosphorMode, PolyBlurConfig.strength)

    @JvmStatic
    fun render(renderTarget: RenderTarget) {
        val width = renderTarget.viewWidth
        val height = renderTarget.viewHeight
        val resized = previous.let { it == null || it.viewWidth != width || it.viewHeight != height }
        if (resized) {
            previous?.destroyBuffers()
            previous = RenderTarget(width, height, false)
        }
        val prev = previous!!
        val continuous = !resized && FrameClock.frame - lastFrame in 0..1
        lastFrame = FrameClock.frame

        renderTarget.bindWrite(true)
        blendPrevious(prev, if (continuous) currentStrength.coerceIn(0f, 0.99f) else 0f)
        GlStateManager.bindTexture(prev.colorTextureId)
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height)
    }

    // also runs on restart frames with no feedback, so main always ends up with alpha 1 like the post chain left it
    private fun blendPrevious(prev: RenderTarget, feedback: Float) {
        for (unit in MAX_SAMPLER_UNIT downTo 1) {
            GlStateManager.activeTexture(GLX.GL_TEXTURE0 + unit)
            GlStateManager.disableTexture()
        }
        GlStateManager.activeTexture(GLX.GL_TEXTURE0)
        GlStateManager.enableTexture()
        GlStateManager.bindTexture(prev.colorTextureId)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.disableDepthTest()
        GlStateManager.depthMask(false)
        GlStateManager.disableAlphaTest()
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE1_RGB, GL13.GL_PRIMARY_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL13.GL_PRIMARY_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
        GL14.glBlendEquation(if (phosphorMode == 0) GL14.GL_MAX else GL14.GL_FUNC_ADD)
        if (phosphorMode == 0) {
            GlStateManager.color4f(feedback, feedback, feedback, 1f)
        } else {
            GlStateManager.color4f(1f, 1f, 1f, 1f)
            GL14.glBlendColor(0f, 0f, 0f, if (phosphorMode == 2) feedback * 0.95f else feedback)
            GlStateManager.blendFuncSeparate(
                GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO
            )
        }
        for (mode in MATRIX_MODES) {
            GlStateManager.matrixMode(mode)
            GlStateManager.pushMatrix()
            GlStateManager.loadIdentity()
        }

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f(-1f, -1f)
        GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f(1f, -1f)
        GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f(1f, 1f)
        GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f(-1f, 1f)
        GL11.glEnd()

        for (mode in MATRIX_MODES.reversedArray()) {
            GlStateManager.matrixMode(mode)
            GlStateManager.popMatrix()
        }
        GlStateManager.matrixMode(GL11.GL_MODELVIEW)
        GL14.glBlendEquation(GL14.GL_FUNC_ADD)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.color4f(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
        GlStateManager.enableAlphaTest()
        GlStateManager.depthMask(true)
        GlStateManager.enableDepthTest()
        GlStateManager.colorMask(true, true, true, true)
    }

    private val MATRIX_MODES = intArrayOf(GL11.GL_TEXTURE, GL11.GL_PROJECTION, GL11.GL_MODELVIEW)
}
^///?} else {
/^object PhosphorBlur {
    private val logger = LogManager.getLogger(PhosphorBlur::class.java)
    private val shaderLocation = location("minecraft", "shaders/post/phosphor_motion_blur.json")

    private var postChain: PostChain? = null
    private var prevWidth = -1
    private var prevHeight = -1

    @JvmStatic
    val phosphorMode: Int
        get() = PolyBlurConfig.phosphorMode

    @JvmStatic
    val currentStrength: Float
        get() = PhosphorFeedback.of(phosphorMode, PolyBlurConfig.strength)

    @JvmStatic
    fun render(renderTarget: RenderTarget) {
        val shader = getPostChain(renderTarget) ?: return
        shader.setUniform("BlendFactor", currentStrength)
        shader.setUniform("Mode", phosphorMode.toFloat())
        shader.process(0f)
    }

    private fun getPostChain(renderTarget: RenderTarget): PostChain? {
        if (postChain != null && renderTarget.viewWidth == prevWidth && renderTarget.viewHeight == prevHeight) {
            return postChain
        }

        postChain?.close()
        postChain = null

        return try {
            val minecraft = Minecraft.getInstance()
            PostChain(minecraft.textureManager, minecraft.resourceManager, renderTarget, shaderLocation).also {
                it.resize(renderTarget.viewWidth, renderTarget.viewHeight)
                postChain = it
                prevWidth = renderTarget.viewWidth
                prevHeight = renderTarget.viewHeight
            }
        } catch (e: IOException) {
            logger.error("Could not load motion blur", e)
            null
        } catch (e: JsonSyntaxException) {
            logger.error("Could not parse motion blur", e)
            null
        }
    }
}
^///?}

//? if =1.8.9 {
/^fun PostChain.setUniform(name: String, value: Float) {
    for (pass in (this as PostChainAccessor).passes) {
        pass.effect.getUniform(name)?.set(value)
    }
}

private const val MAX_SAMPLER_UNIT = 2

fun PostChain.processLegacy(passes: IntRange? = null) {
    GlStateManager.matrixMode(GL11.GL_TEXTURE)
    GlStateManager.pushMatrix()
    GlStateManager.loadIdentity()
    if (passes == null) process(0f) else (this as PostChainAccessor).passes.slice(passes).forEach { it.process(0f) }
    GlStateManager.popMatrix()
    GlStateManager.matrixMode(GL11.GL_MODELVIEW)
    for (unit in MAX_SAMPLER_UNIT downTo 1) {
        GlStateManager.activeTexture(GLX.GL_TEXTURE0 + unit)
        GlStateManager.disableTexture()
    }
    GlStateManager.activeTexture(GLX.GL_TEXTURE0)
    GlStateManager.enableTexture()
    GlStateManager.color4f(1f, 1f, 1f, 1f)
    GlStateManager.disableBlend()
    GlStateManager.enableDepthTest()
    GlStateManager.enableAlphaTest()
    Minecraft.getInstance().mainRenderTarget.bindWrite(true)
}
^///?}
*///?}

//? if =1.21.4 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.PhosphorFeedback

object PhosphorBlur {
    private val logger = LogManager.getLogger(PhosphorBlur::class.java)
    private val shaderLocation by lazy { location(PolyBlurConstants.ID, "phosphor_motion_blur") }

    private var prevFramebuffer: RenderTarget? = null

    @JvmStatic
    val phosphorMode: Int
        get() = PolyBlurConfig.phosphorMode

    @JvmStatic
    val currentStrength: Float
        get() = PhosphorFeedback.of(phosphorMode, PolyBlurConfig.strength)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            Minecraft.getInstance().shaderManager.getPostChain(shaderLocation, BlurFramebufferSet.TARGETS)
        } catch (e: JsonSyntaxException) {
            logger.error("Could not load motion blur", e)
            null
        }

        if (prevFramebuffer == null || prevFramebuffer?.viewWidth != renderTarget.viewWidth || prevFramebuffer?.viewHeight != renderTarget.viewHeight) {
            prevFramebuffer = TextureTarget(renderTarget.viewWidth, renderTarget.viewHeight, false)
            blit(renderTarget, prevFramebuffer ?: return)
        }

        val prevFramebuffer = prevFramebuffer ?: return
        shader?.setUniform("Strength", currentStrength)
        shader?.setUniform("Mode", phosphorMode.toFloat())

        val builder = FrameGraphBuilder()
        val framebufferSet = BlurFramebufferSet(
            builder.importExternal("main", renderTarget),
            builder.importExternal("previous", prevFramebuffer)
        )

        shader?.addToFrame(
            builder,
            renderTarget.viewWidth,
            renderTarget.viewHeight,
            framebufferSet
        )

        builder.execute(resourcePool)
        blit(renderTarget, prevFramebuffer)
    }

    private fun blit(inputTarget: RenderTarget, outputTarget: RenderTarget) {
        inputTarget.bindRead()
        outputTarget.bindWrite(true)
        inputTarget.blitToScreen(outputTarget.width, outputTarget.height)
        outputTarget.unbindWrite()
        inputTarget.unbindRead()
    }
}
*///?}

//? if =1.21.5 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.PhosphorFeedback

object PhosphorBlur {
    private val logger = LogManager.getLogger(PhosphorBlur::class.java)
    private val shaderLocation by lazy { location(PolyBlurConstants.ID, "phosphor_motion_blur") }

    @JvmStatic
    val phosphorMode: Int
        get() = PolyBlurConfig.phosphorMode

    @JvmStatic
    val currentStrength: Float
        get() = PhosphorFeedback.of(phosphorMode, PolyBlurConfig.strength)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            Minecraft.getInstance().shaderManager.getPostChain(shaderLocation, BlurTargetBundle.TARGETS)
        } catch (e: JsonSyntaxException) {
            logger.error("Could not load motion blur", e)
            null
        }

        val prevTarget = RenderTargetTracker.prevTarget
        if (prevTarget == null) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return
        }

        val builder = FrameGraphBuilder()
        val targetBundle = BlurTargetBundle(
            builder.importExternal("main", renderTarget),
            builder.importExternal("previous", prevTarget)
        )

        shader?.addToFrame(
            builder,
            renderTarget.viewWidth,
            renderTarget.viewHeight,
            targetBundle
        ) { renderPass ->
            renderPass.setUniform("Strength", currentStrength)
            renderPass.setUniform("Mode", phosphorMode.toFloat())
        }

        builder.execute(resourcePool)
        RenderTargetTracker.captureIntoPrevious(renderTarget)
    }
}
*///?}

//? if >1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.renderpearl.api.pipeline.RenderPipeline
import com.mojang.renderpearl.api.pipeline.UniformType
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.PhosphorFeedback
import org.polyfrost.polyblur.client.blur.BlurPrewarm
// import org.polyfrost.polyblur.client.blur.BlurProfiler

//? if >=26.2 {
import com.mojang.renderpearl.api.pipeline.BindGroupLayout
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology
import java.util.Optional
//?} else {
/*import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.renderpearl.api.vertex.VertexFormat
import java.util.OptionalInt
*///?}

//? if >=26.1 {
import com.mojang.renderpearl.api.pipeline.ColorTargetState
//?} else
//import com.mojang.blaze3d.platform.DepthTestFunction

object PhosphorBlur {
    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(location(PolyBlurConstants.ID, "phosphor_motion_blur_pipeline"))
            //? if >=1.21.10 {
            .withVertexShader("core/screenquad")
            //?}
            //? if <1.21.10 {
            /*.withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            *///?}
            .withFragmentShader(location(PolyBlurConstants.ID, "post/phosphor_motion_blur"))
            //? if >=26.2 {
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    //? if >=26.3 {
                    .withUniform("DiffuseSampler", UniformType.COMBINED_IMAGE_SAMPLER)
                    .withUniform("PrevSampler", UniformType.COMBINED_IMAGE_SAMPLER)
                    //?} else {
                    /*.withSampler("DiffuseSampler")
                    .withSampler("PrevSampler")
                    *///?}
                    .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
                    .build()
            )
            //?}
            //? if >=1.21.10 && <26.2 {
            /*.withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            *///?}
            //? if <1.21.10 {
            /*.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            *///?}
            //? if >=26.1 && <26.2 {
            /*.withColorTargetState(ColorTargetState.DEFAULT)
            *///?}
            //? if <26.1 {
            /*.withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorWrite(true, true)
            *///?}
            //? if <26.2 {
            /*.withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
            .withSampler("DiffuseSampler")
            .withSampler("PrevSampler")
            *///?}
            .build()
    }

    @JvmStatic
    val phosphorMode: Int
        get() = PolyBlurConfig.phosphorMode

    @JvmStatic
    val currentStrength: Float
        get() = PhosphorFeedback.of(phosphorMode, PolyBlurConfig.strength)

    internal fun prewarm() = BlurPrewarm.compile(pipeline)

    @JvmStatic
    // the pool is only used by the 1.21.4/1.21.5 variants, kept so the mixin call sites stay shared
    @Suppress("UNUSED_PARAMETER")
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) =
        // BlurProfiler.section("phosphor") { renderInner(renderTarget) }
        renderInner(renderTarget)

    private fun renderInner(renderTarget: RenderTarget) {
        if (BlurPrewarm.failed) return
        if (!RenderTargetTracker.isAttachmentInSync(renderTarget)) {
            RenderTargetTracker.requireBootstrap()
            return
        }

        RenderTargetTracker.ensureSize(renderTarget.width, renderTarget.height)

        val prevTarget = RenderTargetTracker.prevTarget
        if (prevTarget == null || RenderTargetTracker.needsBootstrap) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return
        }

        PhosphorBlurUniforms.upload(currentStrength, phosphorMode.toFloat())

        val tempTarget = RenderTargetTracker.writeTarget ?: return

        // all targets here are allocated with a color attachment
        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            { "PolyBlur/Phosphor" },
            tempTarget.getColorTextureView()!!,
            //? if >=26.2 {
            Optional.empty()
            //?}
            //? if <26.2 {
            /*OptionalInt.empty()
            *///?}
        ).use { renderPass ->
            //? if >=26.3 {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline))
            //?} else {
            /*renderPass.setPipeline(pipeline)
            *///?}

            //? if >=26.3 {
            renderPass.setUniform("DiffuseSampler", renderTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            //?} elif >=1.21.11 {
            /*renderPass.bindTexture("DiffuseSampler", renderTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            *///?}
            //? if >=26.3 {
            renderPass.setUniform("PrevSampler", prevTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            //?} elif >=1.21.11 {
            /*renderPass.bindTexture("PrevSampler", prevTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            *///?} else {
            /*renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTextureView()!!)
            renderPass.bindSampler("PrevSampler", prevTarget.getColorTextureView()!!)
            *///?}

            renderPass.setUniform("BlurConfig", PhosphorBlurUniforms.buffer)
            FullscreenPass.draw(renderPass)
        }

        if (RenderTargetTracker.blit(tempTarget, renderTarget)) {
            RenderTargetTracker.swap()
        } else {
            RenderTargetTracker.requireBootstrap()
        }
    }
}
//?}
