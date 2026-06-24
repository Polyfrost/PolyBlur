package org.polyfrost.polyblur.client.blur.phosphor

//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.RenderTargetDescriptor
//? if >=26.2
//import com.mojang.blaze3d.GpuFormat
//? if >=26.2
//import org.joml.Vector4f

object InternalTargetTracker {
    private var framebufferFactory: RenderTargetDescriptor? = null
    var target: RenderTarget? = null
        private set

    private var prevWidth = -1
    private var prevHeight = -1

    fun updateSize(width: Int, height: Int) {
        if (width == prevWidth && height == prevHeight && target != null) {
            return
        }

        if (framebufferFactory == null || framebufferFactory?.width != width || framebufferFactory?.height != height) {
            framebufferFactory = createTargetDescriptor(width, height)
        }

        free()
        target = framebufferFactory?.allocate()
        prevWidth = width
        prevHeight = height
    }

    fun free() {
        target?.let { framebufferFactory?.free(it) }
        target = null
        prevWidth = -1
        prevHeight = -1
    }
}

fun createTargetDescriptor(width: Int, height: Int): RenderTargetDescriptor =
    //? if >=26.2
    //RenderTargetDescriptor(width, height, false, Vector4f(0f, 0f, 0f, 0f), GpuFormat.RGBA8_UNORM)
    //? if <26.2
    RenderTargetDescriptor(width, height, false, 0)
//?}
