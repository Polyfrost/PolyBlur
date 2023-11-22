package org.polyfrost.polyblur.mixin;

import org.polyfrost.polyblur.PolyBlur;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Literally just disable fast render if PolyBlur is enabled.
 * We already implement our own framebuffer with MonkeyBlur, so what's the point of keeping fast render around?
 */
@Pseudo
@Mixin(targets = "Config", remap = false)
public class OptifineConfigMixin {
    @Dynamic("OptiFine")
    @Inject(method = "isFastRender", at = @At("HEAD"), cancellable = true)
    private static void cancelFastRender(CallbackInfoReturnable<Boolean> cir) {
        if (PolyBlur.instance != null && PolyBlur.instance.config.enabled && PolyBlur.instance.config.forceDisableFastRender) {
            cir.setReturnValue(false);
        }
    }
}
