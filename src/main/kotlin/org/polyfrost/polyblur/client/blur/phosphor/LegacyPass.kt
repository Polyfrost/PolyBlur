package org.polyfrost.polyblur.client.blur.phosphor

//? if <1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.shaders.AbstractUniform
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.renderpearl.api.vertex.VertexFormat
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL13
*///?}
//? if =1.21.1 {
/*import com.google.gson.JsonSyntaxException
import net.minecraft.client.renderer.EffectInstance
import org.apache.logging.log4j.LogManager
import java.io.IOException
*///?}
//? if =1.21.4 {
/*import net.minecraft.client.renderer.CompiledShaderProgram
import net.minecraft.client.renderer.ShaderDefines
import net.minecraft.client.renderer.ShaderProgram
import org.polyfrost.polyblur.PolyBlurConstants
*///?}

//? if <1.21.5 {
/*// clip space quad matching the polyblur fullscreen quad vertex shader
object LegacyQuad {
    fun draw() {
        val builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION)
        builder.addVertex(-1f, -1f, 0f)
        builder.addVertex(1f, -1f, 0f)
        builder.addVertex(1f, 1f, 0f)
        builder.addVertex(-1f, 1f, 0f)
        BufferUploader.draw(builder.buildOrThrow())
    }
}

object LegacyPassState {
    fun begin() {
        RenderSystem.colorMask(true, true, true, true)
        RenderSystem.disableBlend()
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
    }

    fun end(mainTarget: RenderTarget) {
        RenderSystem.activeTexture(GL13.GL_TEXTURE0)
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        mainTarget.bindWrite(true)
    }
}
*///?}

//? if =1.21.1 {
/*class LegacyProgram private constructor(private val effect: EffectInstance) {
    fun sampler(name: String, textureId: Int) = effect.setSampler(name) { textureId }

    fun uniform(name: String): AbstractUniform = effect.safeGetUniform(name)

    fun apply() = effect.apply()

    fun clear() = effect.clear()

    companion object {
        private val logger = LogManager.getLogger(LegacyProgram::class.java)
        private val cache = HashMap<String, LegacyProgram?>()

        fun get(name: String): LegacyProgram? {
            if (cache.containsKey(name)) return cache[name]

            val program = try {
                LegacyProgram(EffectInstance(Minecraft.getInstance().resourceManager, "polyblur_$name"))
            } catch (e: IOException) {
                logger.error("Could not load shader polyblur_{}", name, e)
                null
            } catch (e: JsonSyntaxException) {
                logger.error("Could not parse shader polyblur_{}", name, e)
                null
            }

            cache[name] = program
            return program
        }
    }
}
*///?}

//? if =1.21.4 {
/*class LegacyProgram private constructor(private val program: CompiledShaderProgram) {
    fun sampler(name: String, textureId: Int) = program.bindSampler(name, textureId)

    fun uniform(name: String): AbstractUniform = program.safeGetUniform(name)

    fun apply() = program.apply()

    fun clear() = program.clear()

    companion object {
        // the shader manager drops its compiled programs on every resource reload, so the lookup is not cached
        fun get(name: String): LegacyProgram? {
            val shader = Minecraft.getInstance().shaderManager.getProgram(
                ShaderProgram(
                    location(PolyBlurConstants.ID, "post/" + name + "_legacy"),
                    DefaultVertexFormat.POSITION,
                    ShaderDefines.EMPTY
                )
            ) ?: return null
            return LegacyProgram(shader)
        }
    }
}
*///?}
