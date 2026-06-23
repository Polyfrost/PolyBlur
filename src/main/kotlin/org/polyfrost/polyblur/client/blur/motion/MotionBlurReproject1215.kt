package org.polyfrost.polyblur.client.blur.motion

//? if =1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
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
import java.util.OptionalInt

/** 1.21.5 pass no UBO. */
object MotionBlurReproject {
    private const val MAX_BLUR = 0.15f

    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(location(PolyBlurConstants.ID, "unity_motion_blur_reproject_pipeline"))
            .withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            .withFragmentShader(location(PolyBlurConstants.ID, "post/unity_motion_blur_reproject_uniform"))
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorWrite(true, true)
            .withUniform("Intensity", UniformType.FLOAT)
            .withUniform("MaxSamples", UniformType.FLOAT)
            .withUniform("Jitter", UniformType.FLOAT)
            .withUniform("MaxVel", UniformType.FLOAT)
            .withSampler("DiffuseSampler")
            .withSampler("VelocitySampler")
            .build()
    }

    @JvmStatic
    fun render(renderTarget: RenderTarget) {
        val velTarget = VelocityTarget.current ?: return
        if (!WorldCamera.hasPrev) return

        InternalTargetTracker.updateSize(renderTarget.width, renderTarget.height)
        val tempTarget = InternalTargetTracker.target ?: return

        val intensity = (PolyBlurConfig.strength / 10f) * MAX_BLUR

        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
        val vertexBuffer = FullscreenQuad.vertexBuffer

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            tempTarget.getColorTexture()!!,
            OptionalInt.empty()
        ).use { renderPass ->
            renderPass.setPipeline(pipeline)
            renderPass.setVertexBuffer(0, vertexBuffer)
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())
            renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTexture()!!)
            renderPass.bindSampler("VelocitySampler", velTarget.getColorTexture()!!)
            renderPass.setUniform("Intensity", intensity)
            renderPass.setUniform("MaxSamples", PolyBlurConfig.motionBlurSamples)
            renderPass.setUniform("Jitter", 1f)
            renderPass.setUniform("MaxVel", MotionVelocityPass.MAX_VEL)
            renderPass.drawIndexed(0, 6)
        }

        RenderTargetTracker.blit(tempTarget, renderTarget)
    }
}
*///?}
