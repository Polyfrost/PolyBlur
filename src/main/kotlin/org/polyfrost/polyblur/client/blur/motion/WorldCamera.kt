package org.polyfrost.polyblur.client.blur.motion

//? if >=1.21.5 {
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.FrameClock
import kotlin.math.abs
import kotlin.math.pow

object WorldCamera {
    private const val VELOCITY_SMOOTHING = 0.35f

    private const val SETTLED_EPSILON = 2f / 255f

    private const val MAX_ENCODED_OFFSET = 0.5f

    private const val ROTATION_EPSILON = 1e-6f
    private const val POSITION_EPSILON = 1e-5f

    private val curVPm = Matrix4f()
    private val prevVPm = Matrix4f()
    private var curX = 0.0; private var curY = 0.0; private var curZ = 0.0
    private var prevX = 0.0; private var prevY = 0.0; private var prevZ = 0.0
    private var captures = 0
    private var velocityBound = MAX_ENCODED_OFFSET

    fun capture(view: Matrix4f, proj: Matrix4f, pos: Vec3) {
        prevVPm.set(curVPm)
        prevX = curX; prevY = curY; prevZ = curZ

        curVPm.set(proj).mul(view)
        curX = pos.x; curY = pos.y; curZ = pos.z

        if (captures < 2) captures++

        updateVelocityBound()
    }

    val hasPrev: Boolean get() = captures >= 2

    val velocitySettled: Boolean
        get() = hasPrev && velocityBound < SETTLED_EPSILON

    private fun updateVelocityBound() {
        if (!isStatic()) {
            velocityBound = MAX_ENCODED_OFFSET
            return
        }
        if (velocityBound < SETTLED_EPSILON) return
        velocityBound *= (1f - VELOCITY_SMOOTHING).pow(FrameClock.decayExponent)
    }

    private fun isStatic(): Boolean {
        if (!hasPrev) return false
        if (!curVPm.equals(prevVPm, ROTATION_EPSILON)) return false
        if (!PolyBlurConfig.translationParallax) return true
        return abs(deltaX) < POSITION_EPSILON &&
                abs(deltaY) < POSITION_EPSILON &&
                abs(deltaZ) < POSITION_EPSILON
    }

    fun invCurVP(dst: Matrix4f): Matrix4f = dst.set(curVPm).invert()
    fun prevVP(dst: Matrix4f): Matrix4f = dst.set(prevVPm)

    val deltaX: Float get() = (curX - prevX).toFloat()
    val deltaY: Float get() = (curY - prevY).toFloat()
    val deltaZ: Float get() = (curZ - prevZ).toFloat()
}
//?}
