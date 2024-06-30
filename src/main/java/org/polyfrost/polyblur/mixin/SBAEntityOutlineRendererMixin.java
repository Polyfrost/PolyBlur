package org.polyfrost.polyblur.mixin;

import net.minecraft.client.renderer.culling.ICamera;
import org.polyfrost.polyblur.blurs.monkey.MonkeyBlur;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer")
public class SBAEntityOutlineRendererMixin {

    @Dynamic("SBA")
    @Inject(method = "renderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;bindFramebuffer(Z)V", ordinal = 3, shift = At.Shift.AFTER, remap = true), remap = false)
    private static void onRenderEntityOutlinesPre(ICamera camera, float partialTicks, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        MonkeyBlur.instance.bindFb();
    }
}
