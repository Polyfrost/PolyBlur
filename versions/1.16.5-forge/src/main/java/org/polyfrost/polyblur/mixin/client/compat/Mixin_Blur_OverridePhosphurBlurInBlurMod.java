package org.polyfrost.polyblur.mixin.client.compat;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
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
public class Mixin_Blur_OverridePhosphurBlurInBlurMod {
    @Dynamic("Blur Mod")
    @Redirect(method = "onGuiChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getShaderGroup()Z", ordinal = 0, remap = true), remap = false)
    private PostChain isShaderActive(GameRenderer er) {
        if (PhosphorBlur.isActive()) {
            return null;
        }

        return er.currentEffect();
    }
}
