package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.Minecraft;
import org.polyfrost.polyblur.client.blur.monkey.MonkeyBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_Minecraft_InvalidateMonkeyBlur {
    @Inject(method = "updateFramebufferSize", at = @At("TAIL"))
    private void updateFramebufferSize(CallbackInfo ci) {
        MonkeyBlur.invalidate();
    }
}
