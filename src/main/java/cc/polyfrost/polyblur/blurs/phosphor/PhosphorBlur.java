package cc.polyfrost.polyblur.blurs.phosphor;

import cc.polyfrost.oneconfig.events.event.RenderEvent;
import cc.polyfrost.oneconfig.events.event.Stage;
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.polyblur.PolyBlur;
import cc.polyfrost.polyblur.mixin.ShaderGroupAccessor;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class PhosphorBlur {
    private static boolean lastEnabled = false;

    private static final ResourceLocation phosphorBlur = new ResourceLocation("shaders/post/phosphor_motion_blur.json");

    @Subscribe
    private void onRenderTick(RenderEvent event) {
        if (event.stage != Stage.END) {
            return;
        }

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

        if (!isShaderActive() && PolyBlur.instance.config.enabled) {
            //#if FABRIC==1
            //$$ ((GameRendererAccessor) UMinecraft.getMinecraft().gameRenderer).invokeLoadShader(motionBlur);
            //#else
            UMinecraft.getMinecraft().entityRenderer.loadShader(phosphorBlur);
            //#endif

            reloadIntensity();
        } else if (isShaderActive() && !PolyBlur.instance.config.enabled) {
            String name = UMinecraft.getMinecraft().entityRenderer.getShaderGroup().getShaderGroupName();

            // Only stop our specific blur ;)
            if (!name.endsWith("phosphor_motion_blur.json")) {
                return;
            }

            UMinecraft.getMinecraft().entityRenderer.stopUseShader();
        }
    }

    public static void reloadIntensity() {
        try {
            final List<Shader> listShaders = ((ShaderGroupAccessor) UMinecraft.getMinecraft().entityRenderer.getShaderGroup()).getListShaders();

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
        return UMinecraft.getMinecraft().entityRenderer.getShaderGroup() != null
                //#if MC<=11202
                && net.minecraft.client.renderer.OpenGlHelper.shadersSupported
                //#endif
                ;
    }
}
