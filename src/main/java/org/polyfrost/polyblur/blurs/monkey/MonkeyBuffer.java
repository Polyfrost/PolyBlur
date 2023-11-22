package org.polyfrost.polyblur.blurs.monkey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class MonkeyBuffer {

    public int width;
    public int height;
    public int framebufferObject;
    public int framebufferTexture;
    public int depthTexture;

    public MonkeyBuffer(int width, int height) {
        this.width = width;
        this.height = height;

        createFramebuffer(width, height);
        checkFramebufferComplete();
    }

    public void createFramebuffer(int width, int height) {
        this.width = width;
        this.height = height;

        framebufferObject = OpenGlHelper.glGenFramebuffers();
        framebufferTexture = TextureUtil.glGenTextures();
        depthTexture = TextureUtil.glGenTextures();

        setFramebufferFilter(9728);

        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject);

        GlStateManager.bindTexture(framebufferTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, framebufferTexture, 0);

        GlStateManager.bindTexture(depthTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);

        GlStateManager.clearColor(1f, 0f, 0f, 1f);
        GlStateManager.clearDepth(1f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void render() {
        GlStateManager.disableDepth();
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.viewport(0, 0, width, height);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GlStateManager.bindTexture(framebufferTexture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferBuilder = tessellator.getWorldRenderer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferBuilder.pos(0, height, 0).tex(0, 0).color(255, 255, 255, 0).endVertex();
        bufferBuilder.pos(width, height, 0).tex(1, 0).color(255, 255, 255, 255).endVertex();
        bufferBuilder.pos(width, 0, 0).tex(1, 1).color(255, 255, 255, 255).endVertex();
        bufferBuilder.pos(0, 0, 0).tex(0, 1).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.bindTexture(0);
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1, 1, 1, 1);
    }

    public void checkFramebufferComplete()
    {
        int i = OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER);

        if (i != OpenGlHelper.GL_FRAMEBUFFER_COMPLETE)
        {
            if (i == OpenGlHelper.GL_FB_INCOMPLETE_ATTACHMENT)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            }
            else if (i == OpenGlHelper.GL_FB_INCOMPLETE_MISS_ATTACH)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            }
            else if (i == OpenGlHelper.GL_FB_INCOMPLETE_DRAW_BUFFER)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            }
            else if (i == OpenGlHelper.GL_FB_INCOMPLETE_READ_BUFFER)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            }
            else
            {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
            }
        }
    }

    public void clear() {
        GlStateManager.clearColor(1f, 0f, 0f, 1f);
        GlStateManager.clearDepth(1f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void setFramebufferFilter(int filter)
    {
        if (OpenGlHelper.framebufferSupported && Minecraft.getMinecraft().gameSettings.fboEnable) {
            GlStateManager.bindTexture(framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
            GlStateManager.bindTexture(depthTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
            //GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10496.0F);
            //GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10496.0F);
            GlStateManager.bindTexture(0);
        }
    }
}
