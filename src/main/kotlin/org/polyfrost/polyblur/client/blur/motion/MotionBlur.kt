package org.polyfrost.polyblur.client.blur.motion

//? if =1.21.1 {
/*import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.pipeline.RenderTarget
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.PostChain
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.client.blur.phosphor.location
import java.io.IOException

object MotionBlur {
    private val logger = LogManager.getLogger(MotionBlur::class.java)
    private val shaderLocation = location("minecraft", "shaders/post/unity_motion_blur.json")

    private var postChain: PostChain? = null
    private var prevWidth = -1
    private var prevHeight = -1

    @JvmStatic
    fun render(renderTarget: RenderTarget) {
        val shader = getPostChain(renderTarget) ?: return
        MotionVelocity.update(renderTarget.viewWidth, renderTarget.viewHeight)
        shader.setUniform("VelocityX", MotionVelocity.velX)
        shader.setUniform("VelocityY", MotionVelocity.velY)
        shader.setUniform("Samples", MotionVelocity.samples)
        shader.setUniform("Jitter", MotionVelocity.JITTER)
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
import com.mojang.blaze3d.resource.CrossFrameResourcePool
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.PolyBlurConstants
import org.polyfrost.polyblur.client.blur.phosphor.location

object MotionBlur {
    private val logger = LogManager.getLogger(MotionBlur::class.java)
    private val shaderLocation by lazy { location(PolyBlurConstants.ID, "unity_motion_blur") }

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            Minecraft.getInstance().shaderManager.getPostChain(shaderLocation, MotionTargetBundle.TARGETS)
        } catch (e: JsonSyntaxException) {
            logger.error("Could not load motion blur", e)
            null
        }

        MotionVelocity.update(renderTarget.viewWidth, renderTarget.viewHeight)
        shader?.setUniform("VelocityX", MotionVelocity.velX)
        shader?.setUniform("VelocityY", MotionVelocity.velY)
        shader?.setUniform("Samples", MotionVelocity.samples)
        shader?.setUniform("Jitter", MotionVelocity.JITTER)

        val builder = FrameGraphBuilder()
        val targetBundle = MotionTargetBundle(builder.importExternal("main", renderTarget))

        shader?.addToFrame(
            builder,
            renderTarget.viewWidth,
            renderTarget.viewHeight,
            targetBundle
        )

        builder.execute(resourcePool)
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
import org.polyfrost.polyblur.client.blur.phosphor.location

object MotionBlur {
    private val logger = LogManager.getLogger(MotionBlur::class.java)
    private val shaderLocation by lazy { location(PolyBlurConstants.ID, "unity_motion_blur") }

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        val shader = try {
            Minecraft.getInstance().shaderManager.getPostChain(shaderLocation, MotionTargetBundle.TARGETS)
        } catch (e: JsonSyntaxException) {
            logger.error("Could not load motion blur", e)
            null
        }

        MotionVelocity.update(renderTarget.viewWidth, renderTarget.viewHeight)

        val builder = FrameGraphBuilder()
        val targetBundle = MotionTargetBundle(builder.importExternal("main", renderTarget))

        shader?.addToFrame(
            builder,
            renderTarget.viewWidth,
            renderTarget.viewHeight,
            targetBundle
        ) { renderPass ->
            renderPass.setUniform("VelocityX", MotionVelocity.velX)
            renderPass.setUniform("VelocityY", MotionVelocity.velY)
            renderPass.setUniform("Samples", MotionVelocity.samples)
            renderPass.setUniform("Jitter", MotionVelocity.JITTER)
        }

        builder.execute(resourcePool)
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
import org.polyfrost.polyblur.client.blur.phosphor.FullscreenQuad
import org.polyfrost.polyblur.client.blur.phosphor.InternalTargetTracker
import org.polyfrost.polyblur.client.blur.phosphor.RenderTargetTracker
import org.polyfrost.polyblur.client.blur.phosphor.location
//? if >=1.21.11
/*import org.polyfrost.polyblur.client.blur.phosphor.BlurSampler*/
//? if >=26.2
/*import java.util.Optional*/
//? if <26.2
import java.util.OptionalInt

object MotionBlur {
    private val pipeline by lazy {
        RenderPipeline.builder()
            .withLocation(location(PolyBlurConstants.ID, "unity_motion_blur_pipeline"))
            .withVertexShader(location(PolyBlurConstants.ID, "core/fullscreen_quad"))
            .withFragmentShader(location(PolyBlurConstants.ID, "post/unity_motion_blur"))
            //? if >=26.2 {
            /*.withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withSampler("DiffuseSampler")
                    .withUniform("MotionBlurConfig", UniformType.UNIFORM_BUFFER)
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
            .withUniform("MotionBlurConfig", UniformType.UNIFORM_BUFFER)
            .withSampler("DiffuseSampler")
            //?}
            .build()
    }

    @JvmStatic
    fun render(renderTarget: RenderTarget, resourcePool: CrossFrameResourcePool) {
        InternalTargetTracker.updateSize(renderTarget.width, renderTarget.height)

        val tempTarget = InternalTargetTracker.target ?: return

        MotionVelocity.update(renderTarget.width, renderTarget.height)
        MotionBlurUniforms.upload(
            MotionVelocity.velX,
            MotionVelocity.velY,
            MotionVelocity.samples,
            MotionVelocity.JITTER
        )

        val builder = FrameGraphBuilder()
        val tempNode = builder.importExternal("motion_temp", tempTarget)

        builder.addPass("PolyBlur/Motion").apply {
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
                    { "PolyBlur/Motion" },
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
                    //? if <1.21.11 {
                    renderPass.bindSampler("DiffuseSampler", renderTarget.getColorTextureView()!!)
                    //?}

                    renderPass.setUniform("MotionBlurConfig", MotionBlurUniforms.buffer)
                    //? if >=26.2 {
                    /*renderPass.drawIndexed(0, 0, 6, 1, 0)*/
                    //?}
                    //? if <26.2 {
                    renderPass.drawIndexed(0, 0, 6, 1)
                    //?}
                }
            }
        }

        builder.execute(resourcePool)

        RenderTargetTracker.blit(tempTarget, renderTarget)
    }
}
//?}
