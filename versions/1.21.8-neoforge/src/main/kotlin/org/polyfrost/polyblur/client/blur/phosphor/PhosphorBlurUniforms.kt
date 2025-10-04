package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.systems.RenderSystem

object PhosphorBlurUniforms {
    private val BLOCK_SIZE = Std140SizeCalculator().putFloat().get()
    private val device get() = RenderSystem.getDevice()

    val buffer: GpuBuffer by lazy {
        device.createBuffer({ "PhosphorBlur_UBO" }, GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE, BLOCK_SIZE)
    }

    fun upload(blendFactor: Float) {
        device.createCommandEncoder().mapBuffer(buffer, false, true).use { mapped ->
            mapped.data().putFloat(blendFactor)
        }
    }
}
