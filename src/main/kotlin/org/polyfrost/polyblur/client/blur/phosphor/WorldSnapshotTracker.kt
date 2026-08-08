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
    private var holdsFrame = false

    private val buffer: RenderTarget?
        get() = internalSnapshot?.takeIf { it.width == prevWidth && it.height == prevHeight }

    val snapshot: RenderTarget?
        get() = if (holdsFrame) buffer else null

    fun ensure(sourceTarget: RenderTarget): RenderTarget? {
        RenderSystem.assertOnRenderThread()

        updateSize(sourceTarget.width, sourceTarget.height)
        return buffer
    }

    fun capture(sourceTarget: RenderTarget): Boolean {
        RenderSystem.assertOnRenderThread()

        val target = ensure(sourceTarget) ?: return markCaptured(false)
        return markCaptured(RenderTargetTracker.blit(sourceTarget, target))
    }

    fun markCaptured(captured: Boolean): Boolean {
        holdsFrame = captured
        return captured
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
        holdsFrame = false
        prevWidth = -1
        prevHeight = -1
    }
}
//?}
