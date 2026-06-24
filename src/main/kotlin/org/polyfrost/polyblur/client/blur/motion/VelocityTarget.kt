package org.polyfrost.polyblur.client.blur.motion

//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
//? if >=26.2
//import com.mojang.blaze3d.GpuFormat

object VelocityTarget {
    private var target: TextureTarget? = null
    private var w = -1
    private var h = -1

    fun get(width: Int, height: Int): RenderTarget {
        if (target == null || width != w || height != h) {
            target?.destroyBuffers()
            target =
                //? if >=26.2 {
                /*TextureTarget("PolyBlur Velocity", width, height, false, GpuFormat.RGBA8_UNORM)
                *///?} else {
                TextureTarget("PolyBlur Velocity", width, height, false)
                //?}
            w = width
            h = height
        }
        return target!!
    }

    val current: RenderTarget? get() = target
}
//?}
