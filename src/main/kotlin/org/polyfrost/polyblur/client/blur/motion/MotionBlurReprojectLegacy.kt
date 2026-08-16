package org.polyfrost.polyblur.client.blur.motion

//? if <1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import org.lwjgl.opengl.GL11
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.phosphor.LegacyBlit
import org.polyfrost.polyblur.client.blur.phosphor.LegacyPassState
import org.polyfrost.polyblur.client.blur.phosphor.LegacyProgram
import org.polyfrost.polyblur.client.blur.phosphor.LegacyQuad
import org.polyfrost.polyblur.client.blur.phosphor.LegacyScratchTarget

// smears the frame along the velocity buffer, either before the hand or after it depending on Blur Hand
object MotionBlurReproject {
    private const val MAX_BLUR = 0.15f

    @JvmStatic
    fun render(renderTarget: RenderTarget): Boolean {
        val velTarget = VelocityTarget.current ?: return false
        if (!WorldCamera.hasPrev) return false

        val program = LegacyProgram.get("motion_reproject") ?: return false

        LegacyScratchTarget.updateSize(renderTarget.width, renderTarget.height)
        val tempTarget = LegacyScratchTarget.target ?: return false

        val intensity = (PolyBlurConfig.strength / 10f) * MAX_BLUR

        program.sampler("DiffuseSampler", renderTarget.colorTextureId)
        program.sampler("VelocitySampler", velTarget.colorTextureId)
        program.uniform("Intensity").set(intensity)
        program.uniform("MaxSamples").set(PolyBlurConfig.motionBlurSamples)
        program.uniform("Jitter").set(1f)
        program.uniform("MaxVel").set(MotionVelocityPass.MAX_VEL)
        program.uniform("InvIntensity").set(1f / maxOf(intensity, 1e-6f))
        program.uniform("MinLen").set(MotionVelocityPass.MAX_VEL * (4f / 255f))
        program.uniform("ScreenW").set(renderTarget.width.toFloat())
        program.uniform("ScreenH").set(renderTarget.height.toFloat())

        val filterMode = renderTarget.filterMode
        renderTarget.setFilterMode(GL11.GL_LINEAR)

        LegacyPassState.begin()
        tempTarget.bindWrite(true)
        program.apply()
        LegacyQuad.draw()
        program.clear()

        renderTarget.setFilterMode(filterMode)
        val blitted = LegacyBlit.blit(tempTarget, renderTarget)
        LegacyPassState.end(renderTarget)
        return blitted
    }
}
*///?}
