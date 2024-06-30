package org.polyfrost.polyblur.mixin;

import org.polyfrost.polyblur.blurs.monkey.MonkeyBlur;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "at.hannibal2.skyhanni.utils.EntityOutlineRenderer")
public class SkyHanniEntityOutlineRendererMixin {

    @Dynamic("SkyHanni")
    @Inject(method = "renderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;bindFramebuffer(Z)V", ordinal = 3, shift = At.Shift.AFTER, remap = true), remap = false)
    private void onRenderEntityOutlinesPre(CallbackInfo ci) {
        MonkeyBlur.instance.bindFb();
    }
}
