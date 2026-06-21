package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.polyblur.client.blur.moul.MoulBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class Mixin_EntityRenderer_MoulBlur {
    @Inject(method = {"renderWorldPass"}, at = @At("TAIL"))
    private void onRenderWorldEndMB(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        MoulBlur.render();
    }
}
