package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebufferFactory
import net.minecraft.client.render.VertexFormats
import java.util.OptionalInt

object RenderTargetTracker {
    private val PIPELINE = RenderPipeline.builder()
        .withLocation("phosphor_previous_frame_tracker")
        .withVertexShader("core/blit_screen")
        .withFragmentShader("core/blit_screen")
        .withSampler("InSampler")
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withColorWrite(true, true)
        .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
        .build()

    private var framebufferFactory: SimpleFramebufferFactory? = null
    private var prevWidth = -1
    private var prevHeight = -1

    private var internalPrevTarget: Framebuffer? = null

    val prevTarget: Framebuffer?
        get() = internalPrevTarget?.takeIf { it.viewportWidth == prevWidth && it.viewportHeight == prevHeight }

    fun captureIntoPrevious(sourceTarget: Framebuffer) {
        RenderSystem.assertOnRenderThread()

        updateSize(sourceTarget.viewportWidth, sourceTarget.viewportHeight)
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

        if (framebufferFactory == null || framebufferFactory?.comp_2978 != width || framebufferFactory?.comp_2979 != height) {
            framebufferFactory = SimpleFramebufferFactory(width, height, false, 0)
        }

        free()
        internalPrevTarget = framebufferFactory?.create()
        prevWidth = width
        prevHeight = height

        println("Phosphor: Previous frame tracker updated to size $width x $height\n$internalPrevTarget")
    }

    fun free() {
        internalPrevTarget?.let { framebufferFactory?.close(it) }
        internalPrevTarget = null

        prevWidth = -1
        prevHeight = -1
    }

    private fun blit(srcTarget: Framebuffer, dstTarget: Framebuffer) {
        RenderSystem.assertOnRenderThread()

        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS)
        val indexBuffer = autoStorageIndexBuffer.getIndexBuffer(6)
        val vertexBuffer = RenderSystem.getQuadVertexBuffer()

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            dstTarget.colorAttachment,
            OptionalInt.empty()
        ).use { renderPass ->
            renderPass.setPipeline(PIPELINE)
            renderPass.setVertexBuffer(0, vertexBuffer)
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.indexType)
            renderPass.bindSampler("InSampler", srcTarget.colorAttachment)
            renderPass.drawIndexed(0, 6)
        }
    }
}
