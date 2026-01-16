package org.polyfrost.polyblur.mixin.client;

//? if >= 1.21.4 {
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
//?}
import dev.deftu.omnicore.api.client.OmniClient;
import dev.deftu.omnicore.api.client.OmniClientProfiler;
import dev.deftu.omnicore.api.client.render.OmniRenderTicks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class Mixin_ApplyPhosphorBlur {
    //? if >= 1.21.4 {
    @Shadow @Final Minecraft minecraft;
    @Shadow @Final private CrossFrameResourcePool resourcePool;
    //?}

    @Inject(method = "render",
            //? if >= 1.21.8 {
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrameNumber()V",
                shift = At.Shift.AFTER
            )
            //?} else if >= 1.21.4 {
            /*at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/DrawContext;draw()V",
                ordinal = 1
            )
            *///?} else {
            /*at = @At("TAIL")
            *///?}
    )
    private void polyblur$applyPhosphorBlur(DeltaTracker deltaTracker, boolean isTicking, CallbackInfo ci) {
        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }

        //? if >= 1.21.4 {
        if (!OmniClient.get().isGameLoadFinished() || OmniClient.getWorld() == null) {
            return;
        }
        //?}

        OmniClientProfiler.withProfiler(OmniClient.get(), "polyblur_phosphor_blur", () -> {
            //? if >= 1.21.4 {
            RenderSystem.resetTextureMatrix();
            PhosphorBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
            //?} else {
            /*PhosphorBlur.update();
            float trueTickDelta = OmniRenderTicks.get();
            PhosphorBlur.render(trueTickDelta);
            *///?}
        });
    }
}
