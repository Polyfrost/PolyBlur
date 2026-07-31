package org.polyfrost.polyblur.client.blur.motion

//? if >1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.systems.RenderSystem

object MotionReprojectUniforms {
    // 8 floats -> 32 bytes
    private const val SIZE = 32
    private val device get() = RenderSystem.getDevice()

    val buffer: GpuBuffer by lazy {
        device.createBuffer(
            { "MotionReproject_UBO" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            //? if >=1.21.11
            //SIZE.toLong()
            //? if <1.21.11
            SIZE
        )
    }

    fun upload(intensity: Float, maxSamples: Float, jitter: Float, maxVel: Float, width: Int, height: Int) {
        val invIntensity = 1f / maxOf(intensity, 1e-6f)
        val minLen = maxVel * (4f / 255f)
        //? if >=26.2 {
        /*buffer.map(false, true).use { mapped ->
            val bb = mapped.data()
            bb.putFloat(0, intensity)
            bb.putFloat(4, maxSamples)
            bb.putFloat(8, jitter)
            bb.putFloat(12, maxVel)
            bb.putFloat(16, invIntensity)
            bb.putFloat(20, minLen)
            bb.putFloat(24, width.toFloat())
            bb.putFloat(28, height.toFloat())
        }
        *///?} else {
        device.createCommandEncoder().mapBuffer(buffer, false, true).use { mapped ->
            val bb = mapped.data()
            bb.putFloat(0, intensity)
            bb.putFloat(4, maxSamples)
            bb.putFloat(8, jitter)
            bb.putFloat(12, maxVel)
            bb.putFloat(16, invIntensity)
            bb.putFloat(20, minLen)
            bb.putFloat(24, width.toFloat())
            bb.putFloat(28, height.toFloat())
        }
        //?}
    }
}
//?}
