package org.polyfrost.polyblur.client.blur.motion

//? if >1.21.5 {
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
//? if >=26.2
/*import com.mojang.blaze3d.PrimitiveTopology*/
//? if >=26.1
/*import com.mojang.blaze3d.pipeline.ColorTargetState*/
//? if >=26.2
/*import com.mojang.blaze3d.pipeline.BindGroupLayout*/
//? if >=26.1
/*import com.mojang.blaze3d.pipeline.DepthStencilState*/
//? if >=26.1
/*import com.mojang.blaze3d.platform.CompareOp*/
//? if <26.1
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
//? if >=1.21.11
/*import org.polyfrost.polyblur.client.blur.phosphor.BlurSampler*/
//? if >=26.2
/*import java.util.Optional*/
//? if <26.2
import java.util.OptionalInt

/**
 * pre-gui pass 1
 */
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
            .withFragmentShader(location(PolyBlurConstants.ID, "post/motion_velocity"))
            //? if >=26.2 {
            /*.withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withSampler("DepthSampler")
                    .withUniform("VelocityConfig", UniformType.UNIFORM_BUFFER)
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
            .withUniform("VelocityConfig", UniformType.UNIFORM_BUFFER)
            .withSampler("DepthSampler")
            //?}
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
        MotionVelocityUniforms.upload(reproj, invRow3, dVec, MAX_VEL)

        //? if >=26.2 {
        /*val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS)*/
        //?}
        //? if <26.2 {
        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        //?}
        val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
        val vertexBuffer = FullscreenQuad.vertexBuffer

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            { "PolyBlur/MotionVelocity" },
            velTarget.getColorTextureView()!!,
            //? if >=26.2 {
            /*Optional.empty()*/
            //?}
            //? if <26.2 {
            OptionalInt.empty()
            //?}
        ).use { renderPass ->
            renderPass.setPipeline(pipeline)
            //? if >=26.2 {
            /*renderPass.setVertexBuffer(0, vertexBuffer.slice())*/
            //?}
            //? if <26.2 {
            renderPass.setVertexBuffer(0, vertexBuffer)
            //?}
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())
            //? if >=1.21.11 {
            /*renderPass.bindTexture("DepthSampler", mainTarget.getDepthTextureView()!!, BlurSampler.linearClamp)*/
            //?}
            //? if <1.21.11 {
            renderPass.bindSampler("DepthSampler", mainTarget.getDepthTextureView()!!)
            //?}
            renderPass.setUniform("VelocityConfig", MotionVelocityUniforms.buffer)
            //? if >=26.2 {
            /*renderPass.drawIndexed(6, 1, 0, 0, 0)*/
            //?}
            //? if <26.2 {
            renderPass.drawIndexed(0, 0, 6, 1)
            //?}
        }
    }
}
//?}
