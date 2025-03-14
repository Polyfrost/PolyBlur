package org.polyfrost.polyblur.blurs.phosphor;

import dev.deftu.omnicore.client.OmniClient;
import dev.deftu.omnicore.client.render.OmniResolution;
import org.polyfrost.oneconfig.api.event.v1.events.RenderEvent;
import org.polyfrost.oneconfig.api.event.v1.invoke.impl.Subscribe;
import org.polyfrost.polyblur.PolyBlurConfig;
import org.polyfrost.polyblur.mixin.ShaderGroupAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;

public class PhosphorBlur {

    private static boolean lastEnabled = false;

    private static final ResourceLocation phosphorBlur = new ResourceLocation("shaders/post/phosphor_motion_blur.json");

    @Subscribe
    private void onRenderTick(RenderEvent.Post event) {
        // Only update the shader if one is active
        if (!isShaderActive() || lastEnabled != PolyBlurConfig.INSTANCE.getEnabled()) {
            lastEnabled = PolyBlurConfig.INSTANCE.getEnabled();
            if (PolyBlurConfig.INSTANCE.getMode() == 1) {
                reloadBlur();
            }
        }
    }

    public static void reloadBlur() {
        if (!OmniClient.getHasWorld()) {
            return;
        }

        if (!isShaderActive() && PolyBlurConfig.INSTANCE.getEnabled() && PolyBlurConfig.INSTANCE.getMode() == 1) {
            try {
                final ShaderGroup phosphorBlurShader = new ShaderGroup(Minecraft.getMinecraft().getTextureManager(), Minecraft.getMinecraft().getResourceManager(), Minecraft.getMinecraft().getFramebuffer(), phosphorBlur);
                phosphorBlurShader.createBindFramebuffers(OmniResolution.getScreenWidth(), OmniResolution.getScreenHeight());
                ((EntityRendererHook) Minecraft.getMinecraft().entityRenderer).setPhosphorShader(phosphorBlurShader);
                reloadIntensity();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isShaderActive() && (!PolyBlurConfig.INSTANCE.getEnabled() || PolyBlurConfig.INSTANCE.getMode() != 1)) {
            final EntityRendererHook entityRenderer = (EntityRendererHook) OmniClient.getInstance().entityRenderer;
            if (entityRenderer.getPhosphorShader() != null) {
                entityRenderer.getPhosphorShader().deleteShaderGroup();
            }

            entityRenderer.setPhosphorShader(null);
        }
    }

    public static void reloadIntensity() {
        try {
            final List<Shader> listShaders = ((ShaderGroupAccessor) ((EntityRendererHook) OmniClient.getInstance().entityRenderer).getPhosphorShader()).getListShaders();

            if (listShaders == null) {
                return;
            }

            for (Shader shader : listShaders) {
                ShaderUniform su = shader.getShaderManager().getShaderUniform("Weight");

                if (su == null) {
                    continue;
                }

                su.set(Math.max(Math.min(1 - ((float) PolyBlurConfig.INSTANCE.getStrength() / 10) + 0.1F, 1.0F), 0.1F));
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isShaderActive() {
        return ((EntityRendererHook) OmniClient.getInstance().entityRenderer).getPhosphorShader() != null
                //#if MC<=11202
                && net.minecraft.client.renderer.OpenGlHelper.shadersSupported
                //#endif
                ;
    }

}
