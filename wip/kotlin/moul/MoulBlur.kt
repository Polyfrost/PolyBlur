package org.polyfrost.polyblur.client.blur.moul

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.render.OmniRenderEnv
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.polyfrost.polyblur.client.PolyBlurConfig
import kotlin.math.min

/**
 * @author Moulberry
 */
object MoulBlur {
    private var primaryFramebuffer: Framebuffer? = null
    private var secondaryFramebuffer: Framebuffer? = null

    @JvmStatic
    fun render() {
        if (
            !OmniRenderEnv.isFramebufferEnabled ||
            PolyBlurConfig.mode != 2 ||
            !PolyBlurConfig.isEnabled
        ) {
            return
        }

        val framebuffer = OmniClient.getInstance().framebuffer
        val width = framebuffer.framebufferWidth
        val height = framebuffer.framebufferHeight

        GlStateManager.matrixMode(GL11.GL_PROJECTION)
        GlStateManager.loadIdentity()
        GlStateManager.ortho(0.0, width.toDouble(), height.toDouble(), 0.0, 1000.0, 3000.0)
        GlStateManager.matrixMode(GL11.GL_MODELVIEW)
        GlStateManager.loadIdentity()
        GlStateManager.translate(0f, 0f, -2000f)
        this.primaryFramebuffer = checkFramebufferSizes(this.primaryFramebuffer, width, height)
        this.secondaryFramebuffer = checkFramebufferSizes(this.secondaryFramebuffer, width, height)
        this.secondaryFramebuffer?.framebufferClear()
        this.primaryFramebuffer?.bindFramebuffer(true)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE)
        GlStateManager.disableLighting()
        GlStateManager.disableFog()
        GlStateManager.disableBlend()
        framebuffer.bindFramebufferTexture()
        GlStateManager.color(1f, 1f, 1f, 1f)
        drawTexturedRectNoBlend(
            x = 0f, y = 0f,
            width = width.toFloat(), height = height.toFloat(),
            uMin = 0f, uMax = 1f,
            vMin = 0f, vMax = 1f,
            filter = GL11.GL_NEAREST
        )

        GlStateManager.enableBlend()
        this.primaryFramebuffer!!.bindFramebufferTexture()
        GlStateManager.color(1f, 1f, 1f, min(0.1f + (PolyBlurConfig.strength * 0.1f), 0.9f))
        drawTexturedRectNoBlend(
            x = 0f, y = 0f,
            width = width.toFloat(), height = height.toFloat(),
            uMin = 0f, uMax = 1f,
            vMin = 0f, vMax = 1f,
            filter = GL11.GL_LINEAR
        )

        framebuffer.bindFramebuffer(true)
        this.secondaryFramebuffer!!.bindFramebufferTexture()
        GlStateManager.color(1f, 1f, 1f, min(0.1f + (PolyBlurConfig.strength * 0.1f), 0.9f) + 1f)
        GlStateManager.enableBlend()
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
        drawTexturedRectNoBlend(
            x = 0f, y = 0f,
            width = width.toFloat(), height = height.toFloat(),
            uMin = 0f, uMax = 1f,
            vMin = 0f, vMax = 1f,
            filter = GL11.GL_LINEAR
        )

        val swapped = this.primaryFramebuffer
        this.primaryFramebuffer = this.secondaryFramebuffer
        this.secondaryFramebuffer = swapped
    }

    private fun checkFramebufferSizes(framebuffer: Framebuffer?, width: Int, height: Int): Framebuffer {
        var framebuffer = framebuffer
        if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            if (framebuffer == null) {
                framebuffer = Framebuffer(width, height, true)
            } else {
                framebuffer.createBindFramebuffer(width, height)
            }

            framebuffer.setFramebufferFilter(GL11.GL_NEAREST)
        }

        return framebuffer
    }

    private fun drawTexturedRectNoBlend(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        uMin: Float,
        uMax: Float,
        vMin: Float,
        vMax: Float,
        filter: Int
    ) {
        GlStateManager.enableTexture2D()
        GL11.glTexParameteri(3553, 10241, filter)
        GL11.glTexParameteri(3553, 10240, filter)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(uMin.toDouble(), vMax.toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(uMax.toDouble(), vMax.toDouble())
            .endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(uMax.toDouble(), vMin.toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(uMin.toDouble(), vMin.toDouble()).endVertex()
        tessellator.draw()
        GL11.glTexParameteri(3553, 10241, 9728)
        GL11.glTexParameteri(3553, 10240, 9728)
    }
}
