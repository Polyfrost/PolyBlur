package org.polyfrost.polyblur.mixin;

import org.polyfrost.polyblur.blurs.phosphor.EntityRendererHook;
import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.universal.UMinecraft;
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
                        && ((EntityRendererHook) UMinecraft.getMinecraft().entityRenderer).getPhosphorShader() != null
        ) {
            return false;
        }
        return er.isShaderActive();
    }
}
