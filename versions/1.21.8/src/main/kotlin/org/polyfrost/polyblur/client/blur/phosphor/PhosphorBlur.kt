package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import dev.deftu.omnicore.api.locationOrThrow
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import java.util.OptionalInt

object PhosphorBlur {
    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(locationOrThrow(PolyBlurConstants.ID, "phosphor_motion_blur_pipeline"))
            .withVertexShader("core/blit_screen")
            .withFragmentShader(locationOrThrow(PolyBlurConstants.ID, "post/phosphor_motion_blur"))
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorWrite(true, true)
            .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
            .withSampler("DiffuseSampler")
            .withSampler("PrevSampler")
            .build()
    }

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        InternalTargetTracker.updateSize(renderTarget.viewWidth, renderTarget.viewHeight)

        val prevTarget = RenderTargetTracker.prevTarget
        if (prevTarget == null) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return // wait until second frame so that we can have a comparison
        }

        PhosphorBlurUniforms.upload(currentBlendFactor)

        val tempTarget = InternalTargetTracker.target ?: return

        val builder = FrameGraphBuilder()
        val prevNode = builder.importExternal("previous", prevTarget)
        val tempNode = builder.importExternal("phosphor_temp", tempTarget)

        builder.addPass("PolyBlur/Phosphor").apply {
            reads(prevNode)
            readsAndWrites(tempNode)

            executes {
                val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
                val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
                val vertexBuffer = RenderSystem.getQuadVertexBuffer()

                RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    { "PolyBlur/Phosphor" },
                    tempTarget.colorTextureView,
                    OptionalInt.empty()
                ).use { renderPass ->
                    renderPass.setPipeline(pipeline)
                    renderPass.setVertexBuffer(0, vertexBuffer)
                    renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())

                    renderPass.bindSampler("DiffuseSampler", renderTarget.colorTextureView)
                    renderPass.bindSampler("PrevSampler", prevTarget.colorTextureView)

                    renderPass.setUniform("BlurConfig", PhosphorBlurUniforms.buffer)
                    renderPass.drawIndexed(0, 0, 6, 1)
                }
            }
        }

        builder.execute(resourcePool)

        // Capture frame so that we can compare next frame
        RenderTargetTracker.blit(tempTarget, renderTarget)
        RenderTargetTracker.captureIntoPrevious(renderTarget)
    }
}