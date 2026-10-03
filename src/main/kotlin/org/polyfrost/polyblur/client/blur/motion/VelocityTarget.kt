package org.polyfrost.polyblur.client.blur.motion

//? if >1.8.9 {
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget

//? if >=26.2
import com.mojang.renderpearl.api.GpuFormat

//? if >=1.21.5 && <1.21.11
//import com.mojang.renderpearl.api.textures.FilterMode

//? if <1.21.5
//import org.lwjgl.opengl.GL11

//? if =1.21.1
//import net.minecraft.client.Minecraft

/** two buffers ping ponged every frame so the pass can read last frame velocity for temporal smoothing */
object VelocityTarget {
    private var targetA: TextureTarget? = null
    private var targetB: TextureTarget? = null
    private var w = -1
    private var h = -1
    private var parity = false

    private fun half(v: Int): Int = maxOf(1, (v + 1) / 2)

    private fun create(width: Int, height: Int): TextureTarget {
        val target =
        //? if >=26.3 {
        TextureTarget("PolyBlur Velocity", width, height, GpuFormat.RGBA8_UNORM, null)
        //?} elif >=26.2 {
        /*TextureTarget("PolyBlur Velocity", width, height, false, GpuFormat.RGBA8_UNORM)
        *///?} elif >=1.21.5 {
        /*TextureTarget("PolyBlur Velocity", width, height, false)
        *///?} elif =1.21.4 {
        /*TextureTarget(width, height, false)
        *///?} else {
        /*TextureTarget(width, height, false, Minecraft.ON_OSX)
        *///?}
        //? if >=1.21.5 && <1.21.11
        //target.setFilterMode(FilterMode.LINEAR)
        //? if <1.21.5 {
        /*target.setFilterMode(GL11.GL_LINEAR)
        // zero velocity encodes to the middle of the range, so the history buffer must not start black
        target.setClearColor(0.5f, 0.5f, 0f, 1f)
        *///?}
        //? if =1.21.1
        //target.clear(Minecraft.ON_OSX)
        //? if =1.21.4
        //target.clear()
        return target
    }

    /** must be called exactly once per frame before current or history are used */
    fun beginFrame(mainWidth: Int, mainHeight: Int): RenderTarget {
        val width = half(mainWidth)
        val height = half(mainHeight)
        if (targetA == null || width != w || height != h) {
            targetA?.destroyBuffers()
            targetB?.destroyBuffers()
            targetA = create(width, height)
            targetB = create(width, height)
            w = width
            h = height
        }
        parity = !parity
        return if (parity) targetA!! else targetB!!
    }

    val current: RenderTarget? get() = if (parity) targetA else targetB

    val history: RenderTarget? get() = if (parity) targetB else targetA
}
//?}
