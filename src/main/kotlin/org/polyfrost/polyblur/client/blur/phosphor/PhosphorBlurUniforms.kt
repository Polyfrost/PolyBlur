package org.polyfrost.polyblur.client.blur.phosphor

//? if >1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.systems.RenderSystem

object PhosphorBlurUniforms {
    private val blockSize = Std140SizeCalculator().putFloat().putFloat().get()
    private val device get() = RenderSystem.getDevice()

    val buffer: GpuBuffer by lazy {
        device.createBuffer(
            { "PhosphorBlur_UBO" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            //? if >=1.21.11
            /*blockSize.toLong()*/
            //? if <1.21.11
            blockSize
        )
    }

    fun upload(strength: Float, mode: Float) {
        //? if >=26.2 {
        /*buffer.map(false, true).use { mapped ->
            mapped.data().putFloat(strength).putFloat(mode)
        }
        *///?} else {
        device.createCommandEncoder().mapBuffer(buffer, false, true).use { mapped ->
            mapped.data().putFloat(strength).putFloat(mode)
        }
        //?}
    }
}
//?}
