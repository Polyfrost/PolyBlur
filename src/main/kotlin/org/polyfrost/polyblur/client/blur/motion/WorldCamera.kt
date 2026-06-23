package org.polyfrost.polyblur.client.blur.motion

//? if >=1.21.5 {
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

object WorldCamera {
    private val curVPm = Matrix4f()
    private val prevVPm = Matrix4f()
    private var curX = 0.0; private var curY = 0.0; private var curZ = 0.0
    private var prevX = 0.0; private var prevY = 0.0; private var prevZ = 0.0
    private var captures = 0

    fun capture(view: Matrix4f, proj: Matrix4f, pos: Vec3) {
        prevVPm.set(curVPm)
        prevX = curX; prevY = curY; prevZ = curZ

        curVPm.set(proj).mul(view)
        curX = pos.x; curY = pos.y; curZ = pos.z

        if (captures < 2) captures++
    }

    val hasPrev: Boolean get() = captures >= 2

    fun invCurVP(dst: Matrix4f): Matrix4f = dst.set(curVPm).invert()
    fun prevVP(dst: Matrix4f): Matrix4f = dst.set(prevVPm)

    val deltaX: Float get() = (curX - prevX).toFloat()
    val deltaY: Float get() = (curY - prevY).toFloat()
    val deltaZ: Float get() = (curZ - prevZ).toFloat()
}
//?}
