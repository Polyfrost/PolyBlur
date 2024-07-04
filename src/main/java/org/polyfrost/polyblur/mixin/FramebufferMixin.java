package org.polyfrost.polyblur.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.polyfrost.polyblur.blurs.monkey.MonkeyBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Framebuffer.class)
public class FramebufferMixin {

    @Unique private final Framebuffer polyBlur$self = (Framebuffer) (Object) this;

    @Inject(method = "bindFramebuffer", at = @At("TAIL"))
    private void onBindFramebuffer(boolean p_147610_1_, CallbackInfo ci) {
        if (MonkeyBlur.instance.drawingBuffer && polyBlur$self == Minecraft.getMinecraft().getFramebuffer()) {
            MonkeyBlur.instance.bindFb();
        }
    }
}
