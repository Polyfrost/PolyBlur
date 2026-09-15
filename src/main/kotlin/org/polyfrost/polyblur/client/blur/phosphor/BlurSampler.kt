package org.polyfrost.polyblur.client.blur.phosphor

//? if >=1.21.11 {
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.renderpearl.api.textures.AddressMode
import com.mojang.renderpearl.api.textures.FilterMode
import com.mojang.renderpearl.api.textures.GpuSampler
import java.util.OptionalDouble

object BlurSampler {
    val linearClamp: GpuSampler by lazy {
        RenderSystem.getDevice().createSampler(
            AddressMode.CLAMP_TO_EDGE,
            AddressMode.CLAMP_TO_EDGE,
            FilterMode.LINEAR,
            FilterMode.LINEAR,
            1,
            OptionalDouble.empty()
        )
    }
}
//?}
