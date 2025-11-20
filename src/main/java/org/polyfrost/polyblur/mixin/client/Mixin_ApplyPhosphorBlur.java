package org.polyfrost.polyblur.mixin.client;

import dev.deftu.omnicore.api.client.OmniClient;
import dev.deftu.omnicore.api.client.OmniClientProfiler;
import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 1.21.2
//$$ import com.mojang.blaze3d.resource.CrossFrameResourcePool;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import dev.deftu.omnicore.api.client.OmniClient;
//$$ import net.minecraft.client.Minecraft;
//$$ import org.spongepowered.asm.mixin.Final;
//$$ import org.spongepowered.asm.mixin.Shadow;
//#else
import dev.deftu.omnicore.api.client.render.OmniRenderTicks;
//#endif

//#if MC >= 1.21.1
//$$ import net.minecraft.client.DeltaTracker;
//#endif

@Mixin(EntityRenderer.class)
public class Mixin_ApplyPhosphorBlur {
    //#if MC >= 1.21.2
    //$$ @Shadow private Minecraft minecraft;
    //$$ @Shadow @Final private CrossFrameResourcePool resourcePool;
    //#endif

    //#if MC < 1.16.5
    @Inject(method = "isShaderActive", at = @At("HEAD"), cancellable = true)
    private void polyblur$cancelShaderActive(CallbackInfoReturnable<Boolean> cir) {
        if (!PhosphorBlur.isActive()) {
            return;
        }

        cir.setReturnValue(true);
    }
    //#endif

    @Inject(
            //#if MC >= 1.16.5
            //$$ method = "render",
            //#else
            method = "updateCameraAndRender",
            //#endif

            //#if MC >= 1.21.6
            //$$ at = @At(
            //$$     value = "INVOKE",
            //$$     target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrameNumber()V",
            //$$     shift = At.Shift.AFTER
            //$$ )
            //#elseif MC >= 1.21.2
            //$$ at = @At(
            //$$     value = "INVOKE",
            //$$     target = "Lnet/minecraft/client/gui/DrawContext;draw()V",
            //$$     ordinal = 1
            //$$ )
            //#else
            at = @At("TAIL")
            //#endif
    )
    private void polyblur$applyPhosphorBlur(
            //#if MC >= 1.21.1
            //$$ DeltaTracker deltaTracker,
            //#else
            float tickDelta,
            long nanoTime,
            //#endif
            //#if MC >= 1.16.5
            //$$ boolean isTicking,
            //#endif
            CallbackInfo ci
    ) {
        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }

        //#if MC >= 1.21.2
        //$$ if (!OmniClient.get().isGameLoadFinished() || OmniClient.getWorld() == null) {
        //$$     return;
        //$$ }
        //#endif

        OmniClientProfiler.withProfiler(OmniClient.get(), "polyblur_phosphor_blur", () -> {
            //#if MC >= 1.21.2
            //$$ RenderSystem.resetTextureMatrix();
            //$$ PhosphorBlur.render(this.minecraft.getMainRenderTarget(), this.resourcePool);
            //#else
            PhosphorBlur.update();
            float trueTickDelta = OmniRenderTicks.get();
            PhosphorBlur.render(trueTickDelta);
            //#endif
        });
    }
}
