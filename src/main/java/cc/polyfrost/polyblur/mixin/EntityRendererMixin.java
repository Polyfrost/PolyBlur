package cc.polyfrost.polyblur.mixin;

import cc.polyfrost.polyblur.blurs.monkey.MonkeyBlur;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = {"renderWorldPass"}, at = @At("HEAD"))
    private void onRenderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        MonkeyBlur.instance.startFrame();
    }

    @Inject(method = {"renderWorldPass"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;updateRenderInfo(Lnet/minecraft/entity/player/EntityPlayer;Z)V", shift = At.Shift.AFTER))
    private void setupCamera(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        MonkeyBlur.instance.setupCamera(partialTicks);
    }

    @Inject(method = {"renderWorldPass"}, at = @At(value = "CONSTANT", args = "stringValue=hand"))
    private void onRenderWorldEnd(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        MonkeyBlur.instance.endFrame();
    }
}
