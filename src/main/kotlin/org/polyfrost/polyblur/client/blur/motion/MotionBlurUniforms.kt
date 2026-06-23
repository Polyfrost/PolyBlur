package org.polyfrost.polyblur.client.blur.motion

//? if >1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.systems.RenderSystem

object MotionBlurUniforms {
    // vec2 Velocity (0..7), float Samples (8), float Jitter (12) -> 16 bytes.
    private val blockSize = Std140SizeCalculator().putFloat().putFloat().putFloat().putFloat().get()
    private val device get() = RenderSystem.getDevice()

    val buffer: GpuBuffer by lazy {
        device.createBuffer(
            { "MotionBlur_UBO" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            //? if >=1.21.11
            /*blockSize.toLong()*/
            //? if <1.21.11
            blockSize
        )
    }

    fun upload(velX: Float, velY: Float, samples: Float, jitter: Float) {
        //? if >=26.2 {
        /*buffer.map(false, true).use { mapped ->
            mapped.data().putFloat(velX).putFloat(velY).putFloat(samples).putFloat(jitter)
        }
        *///?} else {
        device.createCommandEncoder().mapBuffer(buffer, false, true).use { mapped ->
            mapped.data().putFloat(velX).putFloat(velY).putFloat(samples).putFloat(jitter)
        }
        //?}
    }
}
//?}
