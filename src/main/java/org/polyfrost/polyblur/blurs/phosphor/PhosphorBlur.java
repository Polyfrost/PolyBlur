package org.polyfrost.polyblur.blurs.phosphor;

import org.polyfrost.oneconfig.api.event.v1.events.RenderEvent;
import org.polyfrost.oneconfig.api.event.v1.invoke.impl.Subscribe;
import org.polyfrost.polyblur.PolyBlur;
import org.polyfrost.polyblur.mixin.ShaderGroupAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;
import org.polyfrost.universal.UMinecraft;
import org.polyfrost.universal.UResolution;

import java.io.IOException;
import java.util.List;

public class PhosphorBlur {
    private static boolean lastEnabled = false;

    private static final ResourceLocation phosphorBlur = new ResourceLocation("shaders/post/phosphor_motion_blur.json");

    @Subscribe
    private void onRenderTick(RenderEvent.End event) {
        // Only update the shader if one is active
        if (!isShaderActive() || lastEnabled != PolyBlur.instance.config.enabled) {
            lastEnabled = PolyBlur.instance.config.enabled;
            if (PolyBlur.instance.config.blurMode == 1) {
                reloadBlur();
            }
        }
    }

    public static void reloadBlur() {
        if (UMinecraft.getWorld() == null) {
            return;
        }

        if (!isShaderActive() && PolyBlur.instance.config.enabled && PolyBlur.instance.config.blurMode == 1) {
            try {
                final ShaderGroup phosphorBlurShader = new ShaderGroup(Minecraft.getMinecraft().getTextureManager(), Minecraft.getMinecraft().getResourceManager(), Minecraft.getMinecraft().getFramebuffer(), phosphorBlur);
                phosphorBlurShader.createBindFramebuffers(UResolution.getWindowWidth(), UResolution.getWindowHeight());
                ((EntityRendererHook) Minecraft.getMinecraft().entityRenderer).setPhosphorShader(phosphorBlurShader);
                reloadIntensity();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isShaderActive() && (!PolyBlur.instance.config.enabled || PolyBlur.instance.config.blurMode != 1)) {
            final EntityRendererHook entityRenderer = (EntityRendererHook) UMinecraft.getMinecraft().entityRenderer;
            if (entityRenderer.getPhosphorShader() != null) {
                entityRenderer.getPhosphorShader().deleteShaderGroup();
            }

            entityRenderer.setPhosphorShader(null);
        }
    }

    public static void reloadIntensity() {
        try {
            final List<Shader> listShaders = ((ShaderGroupAccessor) ((EntityRendererHook) UMinecraft.getMinecraft().entityRenderer).getPhosphorShader()).getListShaders();

            if (listShaders == null) {
                return;
            }

            for (Shader shader : listShaders) {
                ShaderUniform su = shader.getShaderManager().getShaderUniform("Weight");

                if (su == null) {
                    continue;
                }

                su.set(Math.max(Math.min(1 - ((float) PolyBlur.instance.config.strength / 10) + 0.1F, 1.0F), 0.1F));
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isShaderActive() {
        return ((EntityRendererHook) UMinecraft.getMinecraft().entityRenderer).getPhosphorShader() != null
                //#if MC<=11202
                && net.minecraft.client.renderer.OpenGlHelper.shadersSupported
                //#endif
                ;
    }
}
