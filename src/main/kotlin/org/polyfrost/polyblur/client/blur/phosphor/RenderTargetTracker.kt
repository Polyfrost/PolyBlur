package org.polyfrost.polyblur.client.blur.phosphor

//? if =1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.resource.RenderTargetDescriptor
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import java.util.OptionalInt

object RenderTargetTracker {
    private val pipeline = RenderPipeline.builder()
        .withLocation("polyblur_previous_frame_tracker")
        .withVertexShader("core/blit_screen")
        .withFragmentShader("core/blit_screen")
        .withSampler("InSampler")
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withColorWrite(true, true)
        .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
        .build()

    private var framebufferFactory: RenderTargetDescriptor? = null
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

    private fun updateSize(width: Int, height: Int) {
        if (width == prevWidth && height == prevHeight && internalPrevTarget != null) {
            return
        }

        if (framebufferFactory == null || framebufferFactory?.width != width || framebufferFactory?.height != height) {
            framebufferFactory = RenderTargetDescriptor(width, height, false, 0)
        }

        free()
        internalPrevTarget = framebufferFactory?.allocate()
        prevWidth = width
        prevHeight = height
    }

    private fun free() {
        internalPrevTarget?.let { framebufferFactory?.free(it) }
        internalPrevTarget = null
        prevWidth = -1
        prevHeight = -1
    }

    fun blit(srcTarget: RenderTarget, dstTarget: RenderTarget) {
        RenderSystem.assertOnRenderThread()

        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
        val vertexBuffer = FullscreenQuad.vertexBuffer

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            dstTarget.getColorTexture()!!,
            OptionalInt.empty()
        ).use { renderPass ->
            renderPass.setPipeline(pipeline)
            renderPass.setVertexBuffer(0, vertexBuffer)
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())
            renderPass.bindSampler("InSampler", srcTarget.getColorTexture()!!)
            renderPass.drawIndexed(0, 6)
        }
    }
}
*///?}

//? if >1.21.5 {
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
//? if >=26.2
//import com.mojang.blaze3d.PrimitiveTopology
//? if >=26.1
//import com.mojang.blaze3d.pipeline.ColorTargetState
//? if >=26.2
//import com.mojang.blaze3d.pipeline.BindGroupLayout
//? if >=26.1
//import com.mojang.blaze3d.pipeline.DepthStencilState
//? if >=26.1
//import com.mojang.blaze3d.platform.CompareOp
//? if <26.1
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.resource.RenderTargetDescriptor
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import org.polyfrost.polyblur.client.blur.BlurPrewarm
import org.polyfrost.polyblur.client.blur.BlurProfiler
//? if >=26.2
//import java.util.Optional
//? if <26.2
import java.util.OptionalInt

object RenderTargetTracker {
    private val pipeline = RenderPipeline.builder()
        .withLocation(location("polyblur", "previous_frame_tracker"))
        .withVertexShader(location("polyblur", "core/fullscreen_quad"))
        .withFragmentShader("core/blit_screen")
        //? if >=26.2 {
        /*.withVertexBinding(0, DefaultVertexFormat.POSITION)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withDepthStencilState(Optional.empty())
        .withColorTargetState(ColorTargetState.DEFAULT)
        .withBindGroupLayout(
            BindGroupLayout.builder()
                .withSampler("InSampler")
                .build()
        )
        *///?}
        //? if <26.2 {
        .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
        //?}
        //? if >=26.1 && <26.2 {
        /*.withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(ColorTargetState.DEFAULT)
        *///?}
        //? if <26.1 {
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withColorWrite(true, true)
        //?}
        //? if <26.2 {
        .withSampler("InSampler")
        //?}
        .build()

    private var framebufferFactory: RenderTargetDescriptor? = null
    private var prevWidth = -1
    private var prevHeight = -1
    private var targetA: RenderTarget? = null
    private var targetB: RenderTarget? = null
    private var parity = false
    private var bootstrap = true

    private fun sized(target: RenderTarget?): RenderTarget? =
        target?.takeIf { it.width == prevWidth && it.height == prevHeight }

    val prevTarget: RenderTarget?
        get() = sized(if (parity) targetB else targetA)

    val writeTarget: RenderTarget?
        get() = sized(if (parity) targetA else targetB)

    fun swap() {
        parity = !parity
    }

    val needsBootstrap: Boolean
        get() = bootstrap

    internal fun prewarm() = BlurPrewarm.compile(pipeline)

    fun ensureSize(width: Int, height: Int) = updateSize(width, height)

    fun captureIntoPrevious(sourceTarget: RenderTarget) {
        RenderSystem.assertOnRenderThread()

        updateSize(sourceTarget.width, sourceTarget.height)
        val currentTarget = prevTarget ?: return
        blit(sourceTarget, currentTarget)
        bootstrap = false
    }

    private fun updateSize(width: Int, height: Int) {
        if (width == prevWidth && height == prevHeight && targetA != null && targetB != null) {
            return
        }

        if (framebufferFactory == null || framebufferFactory?.width != width || framebufferFactory?.height != height) {
            framebufferFactory = createTargetDescriptor(width, height)
        }

        free()
        targetA = framebufferFactory?.allocate()
        targetB = framebufferFactory?.allocate()
        prevWidth = width
        prevHeight = height
        bootstrap = true
    }

    private fun free() {
        targetA?.let { framebufferFactory?.free(it) }
        targetB?.let { framebufferFactory?.free(it) }
        targetA = null
        targetB = null
        prevWidth = -1
        prevHeight = -1
    }

    fun blit(srcTarget: RenderTarget, dstTarget: RenderTarget) {
        RenderSystem.assertOnRenderThread()
        BlurProfiler.countBlit()

        if (srcTarget.width == dstTarget.width && srcTarget.height == dstTarget.height) {
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                srcTarget.getColorTexture()!!, dstTarget.getColorTexture()!!,
                0, 0, 0, 0, 0, srcTarget.width, srcTarget.height
            )
            return
        }

        //? if >=26.2 {
        /*val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS)
        *///?}
        //? if <26.2 {
        val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        //?}
        val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
        val vertexBuffer = FullscreenQuad.vertexBuffer

        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            { "PolyBlur/Previous Frame Tracker Blit" },
            dstTarget.getColorTextureView()!!,
            //? if >=26.2 {
            /*Optional.empty()
            *///?}
            //? if <26.2 {
            OptionalInt.empty()
            //?}
        ).use { renderPass ->
            renderPass.setPipeline(pipeline)
            //? if >=26.2 {
            /*renderPass.setVertexBuffer(0, vertexBuffer.slice())
            *///?}
            //? if <26.2 {
            renderPass.setVertexBuffer(0, vertexBuffer)
            //?}
            renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())
            //? if >=1.21.11 {
            /*renderPass.bindTexture("InSampler", srcTarget.getColorTextureView()!!, BlurSampler.linearClamp)
            *///?}
            //? if <1.21.11 {
            renderPass.bindSampler("InSampler", srcTarget.getColorTextureView()!!)
            //?}
            //? if >=26.2 {
            /*renderPass.drawIndexed(6, 1, 0, 0, 0)
            *///?}
            //? if <26.2 {
            renderPass.drawIndexed(0, 0, 6, 1)
            //?}
        }
    }
}
//?}
