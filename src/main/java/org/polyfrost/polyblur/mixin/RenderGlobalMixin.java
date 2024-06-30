package org.polyfrost.polyblur.mixin;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.polyfrost.polyblur.blurs.monkey.MonkeyBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;bindFramebuffer(Z)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onRenderEntitiesPre(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        MonkeyBlur.instance.bindFb();
    }
}
