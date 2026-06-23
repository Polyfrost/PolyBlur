package org.polyfrost.polyblur.client.blur.phosphor

//? if =1.21.1 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.pipeline.RenderTarget
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.PostChain
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.client.PolyBlurConfig
import java.io.IOException

object PhosphorBlur {
    private val logger = LogManager.getLogger(PhosphorBlur::class.java)
    private val shaderLocation = location("minecraft", "shaders/post/phosphor_motion_blur.json")

    private var postChain: PostChain? = null
    private var prevWidth = -1
    private var prevHeight = -1

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: RenderTarget) {
        val shader = getPostChain(renderTarget) ?: return
        shader.setUniform("BlendFactor", currentBlendFactor)
        shader.process(0f)
    }

    private fun getPostChain(renderTarget: RenderTarget): PostChain? {
        if (postChain != null && renderTarget.viewWidth == prevWidth && renderTarget.viewHeight == prevHeight) {
            return postChain
        }

        postChain?.close()
        postChain = null

        return try {
            val minecraft = Minecraft.getInstance()
            PostChain(minecraft.textureManager, minecraft.resourceManager, renderTarget, shaderLocation).also {
                it.resize(renderTarget.viewWidth, renderTarget.viewHeight)
                postChain = it
                prevWidth = renderTarget.viewWidth
                prevHeight = renderTarget.viewHeight
            }
        } catch (e: IOException) {
            logger.error("Could not load motion blur", e)
            null
        } catch (e: JsonSyntaxException) {
            logger.error("Could not parse motion blur", e)
            null
        }
    }
}
*///?}

//? if =1.21.4 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig

object PhosphorBlur {
    private val logger = LogManager.getLogger(PhosphorBlur::class.java)
    private val shaderLocation by lazy { location(PolyBlurConstants.ID, "phosphor_motion_blur") }

    private var prevFramebuffer: RenderTarget? = null

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            Minecraft.getInstance().shaderManager.getPostChain(shaderLocation, BlurFramebufferSet.TARGETS)
        } catch (e: JsonSyntaxException) {
            logger.error("Could not load motion blur", e)
            null
        }

        if (prevFramebuffer == null || prevFramebuffer?.viewWidth != renderTarget.viewWidth || prevFramebuffer?.viewHeight != renderTarget.viewHeight) {
            prevFramebuffer = TextureTarget(renderTarget.viewWidth, renderTarget.viewHeight, false)
            blit(renderTarget, prevFramebuffer ?: return)
        }

        val prevFramebuffer = prevFramebuffer ?: return
        shader?.setUniform("Strength", currentBlendFactor)

        val builder = FrameGraphBuilder()
        val framebufferSet = BlurFramebufferSet(
            builder.importExternal("main", renderTarget),
            builder.importExternal("previous", prevFramebuffer)
        )

        shader?.addToFrame(
            builder,
            renderTarget.viewWidth,
            renderTarget.viewHeight,
            framebufferSet
        )

        builder.execute(resourcePool)
        blit(renderTarget, prevFramebuffer)
    }

    private fun blit(inputTarget: RenderTarget, outputTarget: RenderTarget) {
        inputTarget.bindRead()
        outputTarget.bindWrite(true)
        inputTarget.blitToScreen(0, 0)
        outputTarget.unbindWrite()
        inputTarget.unbindRead()
    }
}
*///?}

//? if =1.21.5 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig

object PhosphorBlur {
    private val logger = LogManager.getLogger(PhosphorBlur::class.java)
    private val shaderLocation by lazy { location(PolyBlurConstants.ID, "phosphor_motion_blur") }

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            Minecraft.getInstance().shaderManager.getPostChain(shaderLocation, BlurTargetBundle.TARGETS)
        } catch (e: JsonSyntaxException) {
            logger.error("Could not load motion blur", e)
            null
        }

        val prevTarget = RenderTargetTracker.prevTarget
        if (prevTarget == null) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return
        }

        val builder = FrameGraphBuilder()
        val targetBundle = BlurTargetBundle(
            builder.importExternal("main", renderTarget),
            builder.importExternal("previous", prevTarget)
        )

        shader?.addToFrame(
            builder,
            renderTarget.viewWidth,
            renderTarget.viewHeight,
            targetBundle
        ) { renderPass ->
            renderPass.setUniform("Strength", currentBlendFactor)
        }

        builder.execute(resourcePool)
        RenderTargetTracker.captureIntoPrevious(renderTarget)
    }
}
*///?}

//? if >1.21.5 {
import com.mojang.blaze3d.framegraph.FrameGraphBuilder
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
//? if >=26.2
/*import com.mojang.blaze3d.PrimitiveTopology*/
//? if >=26.1
/*import com.mojang.blaze3d.pipeline.ColorTargetState*/
//? if >=26.2
/*import com.mojang.blaze3d.pipeline.BindGroupLayout*/
//? if >=26.1
/*import com.mojang.blaze3d.pipeline.DepthStencilState*/
//? if >=26.1
/*import com.mojang.blaze3d.platform.CompareOp*/
//? if <26.1
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.PolyBlurConfig
//? if >=26.2
/*import java.util.Optional*/
//? if <26.2
import java.util.OptionalInt

object PhosphorBlur {
    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(location(PolyBlurConstants.ID, "phosphor_motion_blur_pipeline"))
            .withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            .withFragmentShader(location(PolyBlurConstants.ID, "post/phosphor_motion_blur"))
            //? if >=26.2 {
            /*.withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withSampler("DiffuseSampler")
                    .withSampler("PrevSampler")
                    .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
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
            .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
            .withSampler("DiffuseSampler")
            .withSampler("PrevSampler")
            //?}
            .build()
    }

    @JvmStatic
    val currentBlendFactor: Float
        get() = ((PolyBlurConfig.strength / 10f) + 0.1f).coerceIn(0.1f, 0.99f)

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        InternalTargetTracker.updateSize(renderTarget.width, renderTarget.height)

        val prevTarget = RenderTargetTracker.prevTarget
        if (prevTarget == null) {
            RenderTargetTracker.captureIntoPrevious(renderTarget)
            return
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
                //? if >=26.2 {
                /*val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS)*/
                //?}
                //? if <26.2 {
                val autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
                //?}
                val indexBuffer = autoStorageIndexBuffer.getBuffer(6)
                val vertexBuffer = FullscreenQuad.vertexBuffer

                RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    { "PolyBlur/Phosphor" },
                    tempTarget.getColorTextureView()!!,
                    //? if >=26.2 {
                    /*Optional.empty()*/
                    //?}
                    //? if <26.2 {
                    OptionalInt.empty()
                    //?}
                ).use { renderPass ->
                    renderPass.setPipeline(pipeline)
                    //? if >=26.2 {
                    /*renderPass.setVertexBuffer(0, vertexBuffer.slice())*/
                    //?}
                    //? if <26.2 {
                    renderPass.setVertexBuffer(0, vertexBuffer)
                    //?}
                    renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type())

                    //? if >=1.21.11 {
                    /*renderPass.bindTexture("DiffuseSampler", renderTarget.getColorTextureView()!!, BlurSampler.linearClamp)*/
                    //?}
                    //? if >=1.21.11 {
                    /*renderPass.bindTexture("PrevSampler", prevTarget.getColorTextureView()!!, BlurSampler.linearClamp)*/
                    //?}
                    //? if <1.21.11 {
                    renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTextureView()!!)
                    renderPass.bindSampler("PrevSampler", prevTarget.getColorTextureView()!!)
                    //?}

                    renderPass.setUniform("BlurConfig", PhosphorBlurUniforms.buffer)
                    //? if >=26.2 {
                    /*renderPass.drawIndexed(6, 1, 0, 0, 0)*/
                    //?}
                    //? if <26.2 {
                    renderPass.drawIndexed(0, 0, 6, 1)
                    //?}
                }
            }
        }

        builder.execute(resourcePool)

        RenderTargetTracker.blit(tempTarget, renderTarget)
        RenderTargetTracker.captureIntoPrevious(renderTarget)
    }
}
//?}
