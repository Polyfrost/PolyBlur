package org.polyfrost.polyblur.client.blur.motion

//? if >1.8.9 && <1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import org.joml.Matrix4f
import org.joml.Vector4f
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.phosphor.LegacyPassState
import org.polyfrost.polyblur.client.blur.phosphor.LegacyProgram
import org.polyfrost.polyblur.client.blur.phosphor.LegacyQuad

object MotionVelocityPass {
    const val MAX_VEL = 0.25f

    private val invCurVP = Matrix4f()
    private val prevVP = Matrix4f()
    private val reproj = Matrix4f()
    private val invRow3 = Vector4f()
    private val dVec = Vector4f()

    @JvmStatic
    fun run(mainTarget: RenderTarget) {
        if (!WorldCamera.hasPrev) return

        val program = LegacyProgram.get("motion_velocity") ?: return

        val velTarget = VelocityTarget.beginFrame(mainTarget.width, mainTarget.height)
        val histTarget = VelocityTarget.history ?: return

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

        program.sampler("DepthSampler", mainTarget.depthTextureId)
        program.sampler("HistorySampler", histTarget.colorTextureId)
        program.uniform("Reproj").set(reproj)
        program.uniform("InvRow3").set(invRow3.x, invRow3.y, invRow3.z, invRow3.w)
        program.uniform("D").set(dVec.x, dVec.y, dVec.z, dVec.w)
        program.uniform("MaxVel").set(MAX_VEL)

        LegacyPassState.begin()
        velTarget.bindWrite(true)
        program.apply()
        LegacyQuad.draw()
        program.clear()
        LegacyPassState.end(mainTarget)
    }
}
*///?}
