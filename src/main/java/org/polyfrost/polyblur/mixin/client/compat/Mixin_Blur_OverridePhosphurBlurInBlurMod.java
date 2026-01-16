package org.polyfrost.polyblur.mixin.client.compat;

/**
 * We want to override Blur's blur so that it overrides our motion blur. Motion blur isn't important, but menu blur is.
 */
// TODO: see if its required for Blur+
//@Pseudo
//@Mixin(targets = "com.tterrag.blur.Blur", remap = false)
//public class Mixin_Blur_OverridePhosphurBlurInBlurMod {
//    @Dynamic("Blur Mod")
//    @Redirect(method = "onGuiChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;isShaderActive()Z", ordinal = 0, remap = true), remap = false)
//    private boolean isShaderActive(EntityRenderer er) {
//        if (PhosphorBlur.isActive()) {
//            return false;
//        }
//
//        return er.isShaderActive();
//    }
//}
