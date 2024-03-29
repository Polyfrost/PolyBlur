package org.polyfrost.polyblur.mixin;

import org.polyfrost.polyblur.blurs.monkey.MonkeyBlur;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "updateFramebufferSize", at = @At("TAIL"))
    private void updateFramebufferSize(CallbackInfo ci) {
        MonkeyBlur.instance.onResolutionChange();
    }
}
