package org.polyfrost.polyblur.client.blur.motion

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import org.polyfrost.polyblur.client.PolyBlurConfig
import kotlin.math.hypot
import kotlin.math.min

object MotionVelocity {
    /** Max smear half-length in UV units at full strength. */
    private const val MAX_BLUR = 0.08f
    const val JITTER = 1.0f

    private var prevYaw = Float.NaN
    private var prevPitch = Float.NaN

    var velX = 0f
        private set
    var velY = 0f
        private set
    var samples = 4f
        private set

    fun reset() {
        prevYaw = Float.NaN
        prevPitch = Float.NaN
        velX = 0f
        velY = 0f
        samples = 4f
    }

    fun update(width: Int, height: Int) {
        val mc = Minecraft.getInstance()
        //? if >=26.2 {
        /*val camera = mc.gameRenderer.mainCamera()
        *///?} else {
        val camera = mc.gameRenderer.mainCamera
        //?}
        //? if >=1.21.11 {
        /*val yaw = camera.yRot()
        val pitch = camera.xRot()
        *///?} else {
        val yaw = camera.yRot
        val pitch = camera.xRot
        //?}

        if (prevYaw.isNaN()) {
            prevYaw = yaw
            prevPitch = pitch
            velX = 0f
            velY = 0f
            samples = 4f
            return
        }

        val dYaw = Mth.wrapDegrees(yaw - prevYaw)
        val dPitch = pitch - prevPitch
        prevYaw = yaw
        prevPitch = pitch

        val fovV = mc.options.fov().get().toInt().toFloat().coerceAtLeast(1f)
        val aspect = if (height > 0) width.toFloat() / height.toFloat() else 1f
        val fovH = fovV * aspect

        var vx = -(dYaw / fovH)
        var vy = (dPitch / fovV)

        val mag = hypot(vx, vy)
        val intensity = (PolyBlurConfig.strength / 10f) * MAX_BLUR
        if (mag > 1e-6f) {
            val scale = min(intensity, mag) / mag // clamp magnitude to the max smear
            vx *= scale
            vy *= scale
        } else {
            vx = 0f
            vy = 0f
        }

        velX = vx
        velY = vy

        val maxSamples = PolyBlurConfig.motionBlurSamples
        val clampedMag = min(mag, intensity)
        samples = (4f + (clampedMag / MAX_BLUR) * (maxSamples - 4f)).coerceIn(4f, maxSamples)
    }
}
