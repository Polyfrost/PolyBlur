package org.polyfrost.polyblur.client.blur.phosphor

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import dev.deftu.omnicore.api.DEFAULT_NAMESPACE
import dev.deftu.omnicore.api.identifierOrThrow
import org.lwjgl.system.MemoryStack
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
import java.util.OptionalInt

object PhosphorBlur {
    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(identifierOrThrow(PolyBlurConstants.ID, "phosphor_motion_blur_pipeline"))
            .withVertexShader(identifierOrThrow(DEFAULT_NAMESPACE, "post/sobel.vsh"))
            .withFragmentShader(identifierOrThrow(PolyBlurConstants.ID, "post/phosphor_motion_blur.frag"))
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
            .withSampler("DiffuseSampler")
            .withSampler("PrevSampler")
            .build()
    }

    private val ubo: GpuBuffer
        get() {
            return MemoryStack.stackPush().use { memoryStack ->
                val builder = Std140Builder.onStack(memoryStack, Std140SizeCalculator().putFloat().get())
                builder.putFloat(currentBlendFactor)

                println("Phosphor: Current blend factor: $currentBlendFactor")
                RenderSystem.getDevice().createBuffer(
                    { "PhosphorBlur_UBO" },
                    GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST,
                    builder.get()
                )
            }
        }

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val prevTarget = RenderTargetTracker.prevTarget
        if (prevTarget == null) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return // wait until second frame so that we can have a comparison
        }

        val builder = FrameGraphBuilder()
        val mainNode = builder.importExternal("main", renderTarget)
        val prevNode = builder.importExternal("previous", prevTarget)
        builder.addPass("PolyBlur/Phosphor").apply {
            readsAndWrites(mainNode)
            reads(prevNode)

            executes {
                RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    { "PolyBlur/Phosphor" },
                    renderTarget.colorTextureView,
                    OptionalInt.empty()
                ).use { renderPass ->
                    renderPass.setPipeline(pipeline)
                    RenderSystem.bindDefaultUniforms(renderPass)

                    val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
                    val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
                    val vertexBuffer = RenderSystem.getQuadVertexBuffer()
                    renderPass.setVertexBuffer(0, vertexBuffer)
                    renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())

                    renderPass.bindSampler("DiffuseSampler", mainNode.get().colorTextureView)
                    renderPass.bindSampler("PrevSampler", prevNode.get().colorTextureView)

                    renderPass.setUniform("BlurConfig", ubo)
                    renderPass.drawIndexed(0, 0, 6, 1)
                }
            }
        }

        builder.execute(resourcePool)

        // Capture frame so that we can compare next frame
        RenderTargetTracker.captureIntoPrevious(renderTarget)
    }
}
