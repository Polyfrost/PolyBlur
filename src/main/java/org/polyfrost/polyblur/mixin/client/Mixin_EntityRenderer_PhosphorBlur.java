package org.polyfrost.polyblur.mixin.client;

import dev.deftu.omnicore.common.OmniProfiler;
import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.2
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import dev.deftu.omnicore.client.OmniClient;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.util.Pool;
//$$ import org.spongepowered.asm.mixin.Final;
//$$ import org.spongepowered.asm.mixin.Shadow;
//#else
import dev.deftu.omnicore.client.render.OmniGameRendering;
//#endif

//#if MC >= 1.21.1
//$$ import net.minecraft.client.DeltaTracker;
//#endif

@Mixin(EntityRenderer.class)
public class Mixin_EntityRenderer_PhosphorBlur {
    //#if MC >= 1.21.2
    //$$ @Shadow private MinecraftClient client;
    //$$ @Shadow @Final private Pool pool;
    //#endif

    @Inject(
            //#if MC >= 1.16.5
            //$$ method = "render",
            //#else
            method = "updateCameraAndRender",
            //#endif

            //#if MC >= 1.21.2
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
        //$$ if (!OmniClient.getInstance().isFinishedLoading() || !OmniClient.hasWorld()) {
        //$$     return;
        //$$ }
        //#endif

        OmniProfiler.withProfiler("polyblur_phosphor_blur", () -> {
            //#if MC >= 1.21.2
            //$$ RenderSystem.resetTextureMatrix();
            //$$ PhosphorBlur.render(this.client.getFramebuffer(), this.pool);
            //#else
            PhosphorBlur.update();
            float trueTickDelta = OmniGameRendering.getTickDelta(true);
            PhosphorBlur.render(trueTickDelta);
            //#endif
        });
    }
}
