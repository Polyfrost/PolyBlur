package org.polyfrost.polyblur.mixin.client.compat;

import org.polyfrost.polyblur.client.PolyBlurConfig;
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
public class Mixin_Config_DisableOptiFineFastRender {
    @Dynamic("OptiFine")
    @Inject(method = "isFastRender", at = @At("HEAD"), cancellable = true)
    private static void cancelFastRender(CallbackInfoReturnable<Boolean> cir) {
        if (PolyBlurConfig.INSTANCE.isEnabled() && PolyBlurConfig.INSTANCE.getForceDisableFastRender()) {
            cir.setReturnValue(false);
        }
    }
}
