package org.polyfrost.polyblur.client.blur.motion

//? if >1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.systems.RenderSystem

object MotionReprojectUniforms {
    // 4 floats -> 16 bytes
    private const val SIZE = 16
    private val device get() = RenderSystem.getDevice()

    val buffer: GpuBuffer by lazy {
        device.createBuffer(
            { "MotionReproject_UBO" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            //? if >=1.21.11
            /*SIZE.toLong()*/
            //? if <1.21.11
            SIZE
        )
    }

    fun upload(intensity: Float, maxSamples: Float, jitter: Float, maxVel: Float) {
        //? if >=26.2 {
        /*buffer.map(false, true).use { mapped ->
            val bb = mapped.data()
            bb.putFloat(0, intensity)
            bb.putFloat(4, maxSamples)
            bb.putFloat(8, jitter)
            bb.putFloat(12, maxVel)
        }
        *///?} else {
        device.createCommandEncoder().mapBuffer(buffer, false, true).use { mapped ->
            val bb = mapped.data()
            bb.putFloat(0, intensity)
            bb.putFloat(4, maxSamples)
            bb.putFloat(8, jitter)
            bb.putFloat(12, maxVel)
        }
        //?}
    }
}
//?}
