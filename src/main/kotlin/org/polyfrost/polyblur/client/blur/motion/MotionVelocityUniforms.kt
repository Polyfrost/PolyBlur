package org.polyfrost.polyblur.client.blur.motion

//? if >1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.systems.RenderSystem
import org.joml.Matrix4f
import org.joml.Vector4f

object MotionVelocityUniforms {
    // mat4 (0..63) + vec4 (64..79) + vec4 (80..95) + float (96) -> 112
    private const val SIZE = 112
    private val device get() = RenderSystem.getDevice()

    val buffer: GpuBuffer by lazy {
        device.createBuffer(
            { "MotionVelocity_UBO" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            //? if >=1.21.11
            /*SIZE.toLong()*/
            //? if <1.21.11
            SIZE
        )
    }

    fun upload(reproj: Matrix4f, invRow3: Vector4f, d: Vector4f, maxVel: Float) {
        //? if >=26.2 {
        /*buffer.map(false, true).use { mapped ->
            val bb = mapped.data()
            reproj.get(0, bb)
            invRow3.get(64, bb)
            d.get(80, bb)
            bb.putFloat(96, maxVel)
        }
        *///?} else {
        device.createCommandEncoder().mapBuffer(buffer, false, true).use { mapped ->
            val bb = mapped.data()
            reproj.get(0, bb)
            invRow3.get(64, bb)
            d.get(80, bb)
            bb.putFloat(96, maxVel)
        }
        //?}
    }
}
//?}
