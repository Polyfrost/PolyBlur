package org.polyfrost.polyblur.client.blur.phosphor

//? if >1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.RenderTargetDescriptor
import com.mojang.blaze3d.systems.RenderSystem

object WorldSnapshotTracker {
    private var framebufferFactory: RenderTargetDescriptor? = null
    private var prevWidth = -1
    private var prevHeight = -1
    private var internalSnapshot: RenderTarget? = null

    val snapshot: RenderTarget?
        get() = internalSnapshot?.takeIf { it.width == prevWidth && it.height == prevHeight }

    fun capture(sourceTarget: RenderTarget) {
        RenderSystem.assertOnRenderThread()

        updateSize(sourceTarget.width, sourceTarget.height)
        val target = snapshot ?: return
        RenderTargetTracker.blit(sourceTarget, target)
    }

    private fun updateSize(width: Int, height: Int) {
        if (width == prevWidth && height == prevHeight && internalSnapshot != null) {
            return
        }

        if (framebufferFactory == null || framebufferFactory?.width != width || framebufferFactory?.height != height) {
            framebufferFactory = createTargetDescriptor(width, height)
        }

        free()
        internalSnapshot = framebufferFactory?.allocate()
        prevWidth = width
        prevHeight = height
    }

    private fun free() {
        internalSnapshot?.let { framebufferFactory?.free(it) }
        internalSnapshot = null
        prevWidth = -1
        prevHeight = -1
    }
}
//?}
