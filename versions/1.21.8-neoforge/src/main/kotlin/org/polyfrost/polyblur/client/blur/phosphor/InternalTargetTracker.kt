package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.RenderTargetDescriptor

object InternalTargetTracker {
    var framebufferFactory: RenderTargetDescriptor? = null
        private set

    var target: RenderTarget? = null
        private set

    private var prevWidth = -1
    private var prevHeight = -1

    fun updateSize(width: Int, height: Int) {
        if (
            width == prevWidth &&
            height == prevHeight &&
            target != null
        ) {
            return
        }

        if (framebufferFactory == null || framebufferFactory?.width != width || framebufferFactory?.height != height) {
            framebufferFactory = RenderTargetDescriptor(width, height, false, 0)
        }

        free()
        target = framebufferFactory?.allocate()
        prevWidth = width
        prevHeight = height

        println("Phosphor: Internal target tracker updated to size $width x $height\n$target")
    }

    fun free() {
        target?.let { framebufferFactory?.free(it) }
        target = null

        prevWidth = -1
        prevHeight = -1
    }
}
