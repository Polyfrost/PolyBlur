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
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.phosphor.FullscreenQuad
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
            .withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            .withFragmentShader(location(PolyBlurConstants.ID, "post/unity_motion_blur_reproject"))
            //? if >=26.2 {
            /*.withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withSampler("DiffuseSampler")
                    .withSampler("VelocitySampler")
                    .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
                    .build()
            )
            *///?}
            //? if <26.2 {
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            //?}
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

    @JvmStatic
    fun render(renderTarget: RenderTarget) {
        val velTarget = VelocityTarget.current ?: return
        if (!WorldCamera.hasPrev) return

        InternalTargetTracker.updateSize(renderTarget.width, renderTarget.height)
        val tempTarget = InternalTargetTracker.target ?: return

        val intensity = (PolyBlurConfig.strength / 10f) * MAX_BLUR
        MotionReprojectUniforms.upload(intensity, PolyBlurConfig.motionBlurSamples, 1f, MotionVelocityPass.MAX_VEL)

        //? if >=26.2 {
        /*val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS)
        *///?}
        //? if <26.2 {
        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        //?}
        val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
        val vertexBuffer = FullscreenQuad.vertexBuffer

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
            //? if >=26.2 {
            /*renderPass.setVertexBuffer(0, vertexBuffer.slice())
            *///?}
            //? if <26.2 {
            renderPass.setVertexBuffer(0, vertexBuffer)
            //?}
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())
            //? if >=1.21.11 {
            /*renderPass.bindTexture("DiffuseSampler", renderTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            renderPass.bindTexture("VelocitySampler", velTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            *///?}
            //? if <1.21.11 {
            renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTextureView()!!)
            renderPass.bindSampler("VelocitySampler", velTarget.getColorTextureView()!!)
            //?}
            renderPass.setUniform("BlurConfig", MotionReprojectUniforms.buffer)
            //? if >=26.2 {
            /*renderPass.drawIndexed(6, 1, 0, 0, 0)
            *///?}
            //? if <26.2 {
            renderPass.drawIndexed(0, 0, 6, 1)
            //?}
        }

        RenderTargetTracker.blit(tempTarget, renderTarget)
    }
}
//?}
