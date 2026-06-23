package org.polyfrost.polyblur.client.blur.motion

//? if =1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import org.joml.Matrix4f
import org.joml.Vector4f
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.phosphor.FullscreenQuad
import org.polyfrost.polyblur.client.blur.phosphor.location
import java.util.OptionalInt

object MotionVelocityPass {
    const val MAX_VEL = 0.25f

    private val invCurVP = Matrix4f()
    private val prevVP = Matrix4f()
    private val reproj = Matrix4f()
    private val invRow3 = Vector4f()
    private val dVec = Vector4f()

    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(location(PolyBlurConstants.ID, "motion_velocity_pipeline"))
            .withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            .withFragmentShader(location(PolyBlurConstants.ID, "post/motion_velocity_uniform"))
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorWrite(true, true)
            .withUniform("Reproj", UniformType.MATRIX4X4)
            .withUniform("InvRow3", UniformType.VEC4)
            .withUniform("D", UniformType.VEC4)
            .withUniform("MaxVel", UniformType.FLOAT)
            .withSampler("DepthSampler")
            .build()
    }

    @JvmStatic
    fun run(mainTarget: RenderTarget) {
        if (!WorldCamera.hasPrev) return

        val velTarget = VelocityTarget.get(mainTarget.width, mainTarget.height)

        WorldCamera.invCurVP(invCurVP)
        WorldCamera.prevVP(prevVP)
        reproj.set(prevVP).mul(invCurVP)
        invCurVP.getRow(3, invRow3)
        if (PolyBlurConfig.translationParallax) {
            dVec.set(WorldCamera.deltaX, WorldCamera.deltaY, WorldCamera.deltaZ, 0f)
            prevVP.transform(dVec)
        } else {
            dVec.set(0f, 0f, 0f, 0f)
        }

        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
        val vertexBuffer = FullscreenQuad.vertexBuffer

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            velTarget.getColorTexture()!!,
            OptionalInt.empty()
        ).use { renderPass ->
            renderPass.setPipeline(pipeline)
            renderPass.setVertexBuffer(0, vertexBuffer)
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())
            renderPass.bindSampler("DepthSampler", mainTarget.getDepthTexture()!!)
            renderPass.setUniform("Reproj", reproj)
            renderPass.setUniform("InvRow3", invRow3.x, invRow3.y, invRow3.z, invRow3.w)
            renderPass.setUniform("D", dVec.x, dVec.y, dVec.z, dVec.w)
            renderPass.setUniform("MaxVel", MAX_VEL)
            renderPass.drawIndexed(0, 6)
        }
    }
}
*///?}
