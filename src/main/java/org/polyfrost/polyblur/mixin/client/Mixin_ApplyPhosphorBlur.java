package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >1.21.1
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
//? if >=26.2
/*import com.mojang.blaze3d.pipeline.RenderTarget;*/
//? if <1.21.11
import com.mojang.blaze3d.systems.RenderSystem;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class Mixin_ApplyPhosphorBlur {
    @Shadow private Minecraft minecraft;
    //? if >1.21.1
    @Shadow @Final private CrossFrameResourcePool resourcePool;
    //? if >=26.2
    /*@Shadow @Final private RenderTarget mainRenderTarget;*/

    @Inject(
            method = "render",
            //? if =1.21.1 {
            /*at = @At("TAIL")
            *///?} elif >=1.21.4 && <1.21.8 {
            /*at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
            *///?} elif >=26.1 {
            /*at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;endFrame()V",
                    shift = At.Shift.AFTER
            )
            *///?} else {
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrameNumber()V",
                    shift = At.Shift.AFTER
            )
            //?}
    )
    private void polyblur$applyPhosphorBlur(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        if (!PolyBlurConfig.INSTANCE.isEnabled() || this.minecraft.level == null || this.minecraft.getConnection() == null) {
            return;
        }

        //? if <1.21.11
        RenderSystem.resetTextureMatrix();
        //? if =1.21.1 {
        /*PhosphorBlur.render(this.minecraft.getMainRenderTarget());
        *///?} elif >=26.2 {
        /*PhosphorBlur.render(this.mainRenderTarget, this.resourcePool);
        *///?} else {
        PhosphorBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
        //?}
    }
}
