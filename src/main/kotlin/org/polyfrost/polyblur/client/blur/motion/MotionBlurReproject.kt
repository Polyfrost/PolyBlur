package org.polyfrost.polyblur.client.blur.motion

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
import org.polyfrost.polyblur.client.blur.phosphor.FullscreenPass
import org.polyfrost.polyblur.client.blur.phosphor.InternalTargetTracker
import org.polyfrost.polyblur.client.blur.phosphor.RenderTargetTracker
import org.polyfrost.polyblur.client.blur.phosphor.location
//? if >=1.21.11
//import org.polyfrost.polyblur.client.blur.phosphor.BlurSampler
//? if >=26.2
//import java.util.Optional
//? if <26.2
import java.util.OptionalInt

/**
 * Pass 2 post-hand pre-GUI
 */
object MotionBlurReproject {
    private const val MAX_BLUR = 0.15f

    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(location(PolyBlurConstants.ID, "unity_motion_blur_reproject_pipeline"))
            //? if >=1.21.10 {
            .withVertexShader("core/screenquad")
            //?}
            //? if <1.21.10 {
            /*.withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            *///?}
            .withFragmentShader(location(PolyBlurConstants.ID, "post/unity_motion_blur_reproject"))
            //? if >=26.2 {
            /*.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withSampler("DiffuseSampler")
                    .withSampler("VelocitySampler")
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
            .withSampler("VelocitySampler")
            //?}
            .build()
    }

    internal fun prewarm() = BlurPrewarm.compile(pipeline)

    @JvmStatic
    @JvmOverloads
    fun render(renderTarget: RenderTarget, outTarget: RenderTarget? = null): Boolean =
        // BlurProfiler.section("motion.reproject") { renderInner(renderTarget, outTarget) }
        renderInner(renderTarget, outTarget)

    private fun renderInner(renderTarget: RenderTarget, outTarget: RenderTarget?): Boolean {
        val velTarget = VelocityTarget.current ?: return false
        if (!WorldCamera.hasPrev) return false

        if (!RenderTargetTracker.isAttachmentInSync(renderTarget)) return false

        val tempTarget = outTarget ?: run {
            InternalTargetTracker.updateSize(renderTarget.width, renderTarget.height)
            InternalTargetTracker.target ?: return false
        }

        val intensity = (PolyBlurConfig.strength / 10f) * MAX_BLUR
        MotionReprojectUniforms.upload(
            intensity,
            PolyBlurConfig.motionBlurSamples,
            1f,
            MotionVelocityPass.MAX_VEL,
            renderTarget.width,
            renderTarget.height
        )

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            { "PolyBlur/MotionReproject" },
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
            renderPass.bindTexture("VelocitySampler", velTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            *///?}
            //? if <1.21.11 {
            renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTextureView()!!)
            renderPass.bindSampler("VelocitySampler", velTarget.getColorTextureView()!!)
            //?}
            renderPass.setUniform("BlurConfig", MotionReprojectUniforms.buffer)
            FullscreenPass.draw(renderPass)
        }

        return RenderTargetTracker.blit(tempTarget, renderTarget)
    }
}
//?}
