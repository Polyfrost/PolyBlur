package org.polyfrost.polyblur.client.blur.phosphor

//? if >1.21.5 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget

import com.mojang.blaze3d.resource.CrossFrameResourcePool
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.renderpearl.api.pipeline.UniformType
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.PhosphorFeedback
import org.polyfrost.polyblur.client.blur.BlurPrewarm
// import org.polyfrost.polyblur.client.blur.BlurProfiler

//? if >=26.2 {
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology
import com.mojang.renderpearl.api.pipeline.BindGroupLayout
import java.util.Optional
//?} else {
/*import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.renderpearl.api.vertex.VertexFormat
import java.util.OptionalInt
*///?}

//? if >=26.1 {
import com.mojang.renderpearl.api.pipeline.ColorTargetState
import com.mojang.renderpearl.api.pipeline.DepthStencilState
import com.mojang.renderpearl.api.pipeline.CompareOp
//?} else
//import com.mojang.blaze3d.platform.DepthTestFunction

//? if >=1.21.11
import org.polyfrost.polyblur.client.blur.phosphor.BlurSampler

object HybridHandPhosphor {
    private val handStrength: Float
        get() = PhosphorFeedback.of(PhosphorBlur.phosphorMode, PolyBlurConfig.handBlurStrength)

    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(location(PolyBlurConstants.ID, "phosphor_hand_pipeline"))
            //? if >=1.21.10 {
            .withVertexShader("core/screenquad")
            //?}
            //? if <1.21.10 {
            /*.withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            *///?}
            .withFragmentShader(location(PolyBlurConstants.ID, "post/phosphor_hand"))
            //? if >=26.2 {
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    //? if >=26.3 {
                    .withUniform("DiffuseSampler", UniformType.COMBINED_IMAGE_SAMPLER)
                    .withUniform("PrevSampler", UniformType.COMBINED_IMAGE_SAMPLER)
                    .withUniform("WorldSampler", UniformType.COMBINED_IMAGE_SAMPLER)
                    //?} else {
                    /*.withSampler("DiffuseSampler")
                    .withSampler("PrevSampler")
                    .withSampler("WorldSampler")
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
            /*.withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(ColorTargetState.DEFAULT)
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
            .withSampler("WorldSampler")
            *///?}
            .build()
    }

    internal fun prewarm() = BlurPrewarm.compile(pipeline)

    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) =
        // BlurProfiler.section("hybrid.hand") { renderInner(renderTarget) }
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

        val worldTarget = WorldSnapshotTracker.snapshot
        if (worldTarget == null) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return
        }

        PhosphorBlurUniforms.upload(handStrength, PhosphorBlur.phosphorMode.toFloat())

        val tempTarget = RenderTargetTracker.writeTarget ?: return

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            { "PolyBlur/HybridHand" },
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
            renderPass.setUniform("PrevSampler", prevTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            renderPass.setUniform("WorldSampler", worldTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            //?} elif >=1.21.11 {
            /*renderPass.bindTexture("DiffuseSampler", renderTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            renderPass.bindTexture("PrevSampler", prevTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            renderPass.bindTexture("WorldSampler", worldTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            *///?}
            //? if <1.21.11 {
            /*renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTextureView()!!)
            renderPass.bindSampler("PrevSampler", prevTarget.getColorTextureView()!!)
            renderPass.bindSampler("WorldSampler", worldTarget.getColorTextureView()!!)
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

//? if =1.8.9 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.pipeline.RenderTarget
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.PostChain
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.FrameClock
import org.polyfrost.polyblur.client.blur.PhosphorFeedback
import java.io.IOException

object HybridHandPhosphor {
    private val logger = LogManager.getLogger(HybridHandPhosphor::class.java)
    private val shaderLocation = location("minecraft", "shaders/post/phosphor_hand.json")

    private var postChain: PostChain? = null
    private var prevWidth = -1
    private var prevHeight = -1
    private var lastFrame = -1L

    private val handStrength: Float
        get() = PhosphorFeedback.of(PhosphorBlur.phosphorMode, PolyBlurConfig.handBlurStrength)

    @JvmStatic
    fun snapshotWorld(renderTarget: RenderTarget) {
        getPostChain(renderTarget)?.processLegacy(0..0)
    }

    @JvmStatic
    fun render(renderTarget: RenderTarget) {
        if (renderTarget.viewWidth != prevWidth || renderTarget.viewHeight != prevHeight) return
        val shader = postChain ?: return
        val bootstrap = FrameClock.frame != lastFrame + 1
        lastFrame = FrameClock.frame
        shader.setUniform("BlendFactor", if (bootstrap) 0f else handStrength)
        shader.setUniform("Mode", PhosphorBlur.phosphorMode.toFloat())
        shader.processLegacy(1..3)
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
                lastFrame = -1L
                prevWidth = renderTarget.viewWidth
                prevHeight = renderTarget.viewHeight
            }
        } catch (e: IOException) {
            logger.error("Could not load hybrid hand blur", e)
            null
        } catch (e: JsonSyntaxException) {
            logger.error("Could not parse hybrid hand blur", e)
            null
        }
    }
}
*///?}
