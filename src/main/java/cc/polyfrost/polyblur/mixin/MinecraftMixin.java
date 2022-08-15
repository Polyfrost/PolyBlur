package cc.polyfrost.polyblur.mixin;

import cc.polyfrost.polyblur.PolyBlur;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "updateFramebufferSize", at = @At("TAIL"))
    private void updateFramebufferSize(CallbackInfo ci) {
        if (PolyBlur.instance != null) {
            PolyBlur.instance.onResolutionChange();
        }
    }
}
