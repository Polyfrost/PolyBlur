package org.polyfrost.polyblur.client.blur.motion

//? if >1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.systems.RenderSystem
import org.joml.Matrix4f
import org.joml.Vector4f

object MotionVelocityUniforms {
    // std140 layout mat4 at 0 vec4 at 64 vec4 at 80 then floats MaxVel 96 TimeScale 100 ZZeroToOne 104 Alpha 108
    private const val SIZE = 112
    private val device get() = RenderSystem.getDevice()

    val buffer: GpuBuffer by lazy {
        device.createBuffer(
            { "MotionVelocity_UBO" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            //? if >=1.21.11
            SIZE.toLong()
            //? if <1.21.11
            //SIZE
        )
    }

    fun upload(reproj: Matrix4f, invRow3: Vector4f, d: Vector4f, maxVel: Float, timeScale: Float, zZeroToOne: Float, alpha: Float) {
        //? if >=26.2 {
        buffer.map(false, true).use { mapped ->
            val bb = mapped.data()
            reproj.get(0, bb)
            invRow3.get(64, bb)
            d.get(80, bb)
            bb.putFloat(96, maxVel)
            bb.putFloat(100, timeScale)
            bb.putFloat(104, zZeroToOne)
            bb.putFloat(108, alpha)
        }
        //?} else {
        /*device.createCommandEncoder().mapBuffer(buffer, false, true).use { mapped ->
            val bb = mapped.data()
            reproj.get(0, bb)
            invRow3.get(64, bb)
            d.get(80, bb)
            bb.putFloat(96, maxVel)
            bb.putFloat(100, timeScale)
            bb.putFloat(104, zZeroToOne)
            bb.putFloat(108, alpha)
        }
        *///?}
    }
}
//?}
