package org.polyfrost.polyblur.client.blur.phosphor

//? if >1.21.5 {
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
//? if >=26.2
//import com.mojang.blaze3d.PrimitiveTopology
//? if >=26.1
//import com.mojang.blaze3d.pipeline.ColorTargetState
//? if >=26.2
//import com.mojang.blaze3d.pipeline.BindGroupLayout
//? if >=26.1
//import com.mojang.blaze3d.pipeline.DepthStencilState
//? if >=26.1
//import com.mojang.blaze3d.platform.CompareOp
//? if <26.1
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
//? if <26.2
import com.mojang.blaze3d.vertex.DefaultVertexFormat
//? if <26.2
import com.mojang.blaze3d.vertex.VertexFormat
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.BlurPrewarm
// import org.polyfrost.polyblur.client.blur.BlurProfiler
//? if >=1.21.11
//import org.polyfrost.polyblur.client.blur.phosphor.BlurSampler
//? if >=26.2
//import java.util.Optional
//? if <26.2
import java.util.OptionalInt

object HybridHandPhosphor {
    private val handStrength: Float
        get() {
            val s = PolyBlurConfig.handBlurStrength
            val base = when (PhosphorBlur.phosphorMode) {
                0 -> (0.7f + (s / 100f) * 3f - 0.01f).coerceIn(0f, 1f)
                2 -> (s / 10f).coerceIn(0f, 1f)
                else -> ((s / 10f) + 0.1f).coerceIn(0.1f, 0.99f)
            }
            return Math.pow(base.toDouble(), org.polyfrost.polyblur.client.blur.FrameClock.decayExponent.toDouble()).toFloat()
        }

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
            /*.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withSampler("DiffuseSampler")
                    .withSampler("PrevSampler")
                    .withSampler("WorldSampler")
                    .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
                    .build()
            )
            *///?}
            //? if >=1.21.10 && <26.2 {
            .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            //?}
            //? if <1.21.10 {
            /*.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            *///?}
            //? if >=26.1 && <26.2 {
            /*.withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(ColorTargetState.DEFAULT)
            *///?}
            //? if <26.1 {
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorWrite(true, true)
            //?}
            //? if <26.2 {
            .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
            .withSampler("DiffuseSampler")
            .withSampler("PrevSampler")
            .withSampler("WorldSampler")
            //?}
            .build()
    }

    internal fun prewarm() = BlurPrewarm.compile(pipeline)

    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) =
        // BlurProfiler.section("hybrid.hand") { renderInner(renderTarget) }
        renderInner(renderTarget)

    private fun renderInner(renderTarget: RenderTarget) {
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
            /*Optional.empty()
            *///?}
            //? if <26.2 {
            OptionalInt.empty()
            //?}
        ).use { renderPass ->
            renderPass.setPipeline(pipeline)

            //? if >=1.21.11 {
            /*renderPass.bindTexture("DiffuseSampler", renderTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            renderPass.bindTexture("PrevSampler", prevTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            renderPass.bindTexture("WorldSampler", worldTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            *///?}
            //? if <1.21.11 {
            renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTextureView()!!)
            renderPass.bindSampler("PrevSampler", prevTarget.getColorTextureView()!!)
            renderPass.bindSampler("WorldSampler", worldTarget.getColorTextureView()!!)
            //?}

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
