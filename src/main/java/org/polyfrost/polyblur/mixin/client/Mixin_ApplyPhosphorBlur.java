package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >1.21.1
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
//? if >=1.21.5
import com.mojang.blaze3d.pipeline.RenderTarget;
//? if <1.21.11
import com.mojang.blaze3d.systems.RenderSystem;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.polyfrost.polyblur.client.blur.motion.MotionBlur;
//? if >=1.21.5
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
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
        boolean useMotion = PolyBlurConfig.INSTANCE.getBlurType() == 1;
        //? if =1.21.1 {
        /*if (useMotion) MotionBlur.render(this.minecraft.getMainRenderTarget());
        else PhosphorBlur.render(this.minecraft.getMainRenderTarget());
        *///?} elif >=26.2 {
        /*
        if (!useMotion) PhosphorBlur.render(this.mainRenderTarget, this.resourcePool);
        *///?} elif >1.21.5 && <26.2 {
        if (!useMotion) PhosphorBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
        //?} elif =1.21.5 {
        
        /*if (useMotion && !PolyBlurConfig.INSTANCE.getVelocityBuffer()) MotionBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
        else if (!useMotion) PhosphorBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
        *///?} else {
        /*if (useMotion) MotionBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
        else PhosphorBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
        *///?}
    }

    //? if >=1.21.5 {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void polyblur$applyReprojectBlur(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        if (!PolyBlurConfig.INSTANCE.isEnabled() || this.minecraft.level == null || this.minecraft.getConnection() == null) {
            return;
        }
        if (PolyBlurConfig.INSTANCE.getBlurType() != 1) return;
        if (!PolyBlurConfig.INSTANCE.getBlurHand()) return;
        //? if >=26.2 {
        /*RenderTarget target = this.mainRenderTarget;*/
        //?} else {
        RenderTarget target = this.minecraft.getMainRenderTarget();
        //?}
        if (PolyBlurConfig.INSTANCE.getVelocityBuffer()) {
            MotionBlurReproject.render(target);
        }
        //? if >1.21.5 {
        else {
            MotionBlur.render(target, this.resourcePool);
        }
        //?}
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void polyblur$stashResourcePool(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder.INSTANCE.setPool(this.resourcePool);
        //? if >=26.2 {
        /*org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder.INSTANCE.setMainTarget(this.mainRenderTarget);*/
        //?} else {
        org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder.INSTANCE.setMainTarget(this.minecraft.getMainRenderTarget());
        //?}
    }
    //?}
}
