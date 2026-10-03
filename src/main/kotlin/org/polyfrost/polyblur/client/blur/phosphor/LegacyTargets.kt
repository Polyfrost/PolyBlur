package org.polyfrost.polyblur.client.blur.phosphor

//? if >1.8.9 && <1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.renderpearl.backend.opengl.GlStateManager
import org.lwjgl.opengl.GL30

//? if =1.21.1
//import net.minecraft.client.Minecraft

// scratch buffer the blur passes render into before the result is copied back over the main target
object LegacyScratchTarget {
    private var buffer: TextureTarget? = null
    private var prevWidth = -1
    private var prevHeight = -1

    val target: RenderTarget?
        get() = buffer

    fun updateSize(width: Int, height: Int) {
        if (width == prevWidth && height == prevHeight && buffer != null) {
            return
        }

        free()
        buffer = newLegacyTarget(width, height)
        prevWidth = width
        prevHeight = height
    }

    fun free() {
        buffer?.destroyBuffers()
        buffer = null
        prevWidth = -1
        prevHeight = -1
    }
}

// framebuffer copy that leaves the destination bound for writing
object LegacyBlit {
    fun blit(srcTarget: RenderTarget, dstTarget: RenderTarget): Boolean {
        RenderSystem.assertOnRenderThread()

        if (srcTarget.width <= 0 || srcTarget.height <= 0 || dstTarget.width <= 0 || dstTarget.height <= 0) {
            return false
        }

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, srcTarget.frameBufferId)
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dstTarget.frameBufferId)
        GlStateManager._glBlitFrameBuffer(
            0, 0, srcTarget.width, srcTarget.height,
            0, 0, dstTarget.width, dstTarget.height,
            GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST
        )
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, dstTarget.frameBufferId)
        return true
    }
}

//? if =1.21.1 {
/*private fun newLegacyTarget(width: Int, height: Int): TextureTarget =
    TextureTarget(width, height, false, Minecraft.ON_OSX)
*///?}

//? if =1.21.4 {
/*private fun newLegacyTarget(width: Int, height: Int): TextureTarget =
    TextureTarget(width, height, false)
*///?}
*///?}
