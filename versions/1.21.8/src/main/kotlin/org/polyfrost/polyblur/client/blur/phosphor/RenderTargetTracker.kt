package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.resource.RenderTargetDescriptor
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import java.util.OptionalInt

object RenderTargetTracker {
    private val pipeline = RenderPipeline.builder()
        .withLocation("phosphor_previous_frame_tracker")
        .withVertexShader("core/blit_screen")
        .withFragmentShader("core/blit_screen")
        .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withColorWrite(true, true)
        .withSampler("InSampler")
        .build()

    var framebufferFactory: RenderTargetDescriptor? = null
        private set
    private var prevWidth = -1
    private var prevHeight = -1

    private var internalPrevTarget: RenderTarget? = null

    val prevTarget: RenderTarget?
        get() = internalPrevTarget?.takeIf { it.viewWidth == prevWidth && it.viewHeight == prevHeight }

    fun captureIntoPrevious(sourceTarget: RenderTarget) {
        RenderSystem.assertOnRenderThread()

        updateSize(sourceTarget.viewWidth, sourceTarget.viewHeight)
        val currentTarget = prevTarget ?: return
        blit(sourceTarget, currentTarget)
    }

    fun updateSize(width: Int, height: Int) {
        if (
            width == prevWidth &&
            height == prevHeight &&
            internalPrevTarget != null
        ) {
            return
        }

        if (framebufferFactory == null || framebufferFactory?.width != width || framebufferFactory?.height != height) {
            framebufferFactory = RenderTargetDescriptor(width, height, false, 0)
        }

        free()
        internalPrevTarget = framebufferFactory?.allocate()
        prevWidth = width
        prevHeight = height

        println("Phosphor: Previous frame tracker updated to size $width x $height\n$internalPrevTarget")
    }

    fun free() {
        internalPrevTarget?.let { framebufferFactory?.free(it) }
        internalPrevTarget = null

        prevWidth = -1
        prevHeight = -1
    }

    fun blit(srcTarget: RenderTarget, dstTarget: RenderTarget) {
        RenderSystem.assertOnRenderThread()

        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
        val vertexBuffer = RenderSystem.getQuadVertexBuffer()

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            { "Phosphor: Previous Frame Tracker Blit" },
            dstTarget.colorTextureView,
            OptionalInt.empty()
        ).use { renderPass ->
            renderPass.setPipeline(pipeline)
            renderPass.setVertexBuffer(0, vertexBuffer)
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())
            renderPass.bindSampler("InSampler", srcTarget.colorTextureView)
            renderPass.drawIndexed(0, 0, 6, 1)
        }
    }
}