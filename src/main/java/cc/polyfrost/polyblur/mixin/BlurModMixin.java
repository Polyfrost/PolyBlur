package cc.polyfrost.polyblur.mixin;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * We want to override Blur's blur so that it overrides our motion blur. Motion blur isn't important, but menu blur is.
 */
@Pseudo
@Mixin(targets = "com.tterrag.blur.Blur", remap = false)
public class BlurModMixin {
    @Dynamic("Blur Mod")
    @Redirect(method = "onGuiChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;isShaderActive()Z", ordinal = 0, remap = true), remap = false)
    private boolean isShaderActive(EntityRenderer er) {
        if (
                        //#if MC<=11202
                        net.minecraft.client.renderer.OpenGlHelper.shadersSupported
                        //#else
                        //$$ true
                        //#endif
                        && UMinecraft.getMinecraft().entityRenderer.getShaderGroup() != null
                        && UMinecraft.getMinecraft().entityRenderer.getShaderGroup().getShaderGroupName().endsWith("phosphor_motion_blur.json")
        ) {
            return false;
        }
        return er.isShaderActive();
    }
}
