package org.polyfrost.polyblur.client.blur.motion

//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
//? if >=26.2
//import com.mojang.blaze3d.GpuFormat
//? if <1.21.11
import com.mojang.blaze3d.textures.FilterMode

/**
 * Double-buffered velocity target. The velocity pass reads the previous frame's
 * result ([history]) to temporally smooth the freshly computed velocity, so the
 * two buffers are ping-ponged every frame.
 */
object VelocityTarget {
    private var targetA: TextureTarget? = null
    private var targetB: TextureTarget? = null
    private var w = -1
    private var h = -1
    private var parity = false

    private fun half(v: Int): Int = maxOf(1, (v + 1) / 2)

    private fun create(width: Int, height: Int): TextureTarget {
        val target =
        //? if >=26.2 {
        /*TextureTarget("PolyBlur Velocity", width, height, false, GpuFormat.RGBA8_UNORM)
        *///?} else {
        TextureTarget("PolyBlur Velocity", width, height, false)
        //?}
        //? if <1.21.11
        target.setFilterMode(FilterMode.LINEAR)
        return target
    }

    /**
     * Swaps the buffers and returns the one to render this frame's velocity into.
     * Must be called exactly once per frame, before [current] or [history] are used.
     */
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

    /** The buffer written this frame; sampled by the blur pass. */
    val current: RenderTarget? get() = if (parity) targetA else targetB

    /** The buffer written last frame; sampled by the velocity pass for smoothing. */
    val history: RenderTarget? get() = if (parity) targetB else targetA
}
//?}
