package org.polyfrost.polyblur.mixin.client;

import dev.deftu.omnicore.client.render.OmniGameRendering;
import dev.deftu.omnicore.common.OmniProfiler;
import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.1
//$$ import net.minecraft.client.DeltaTracker;
//#endif

//#if MC >= 1.16.5
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif

@Mixin(EntityRenderer.class)
public class Mixin_EntityRenderer_PhosphorBlur {
    @Inject(
            //#if MC >= 1.16.5
            //$$ method = "renderLevel",
            //#else
            method = "renderWorld",
            //#endif
            at = @At("TAIL")
    )
    private void polyblur$applyPhosphorBlur(
            //#if MC >= 1.21.1
            //$$ DeltaTracker deltaTracker,
            //#else
            float tickDelta,
            long finishTimeNano,
            //#endif
            //#if MC >= 1.16.5 && MC < 1.20.6
            //$$ PoseStack poseStack,
            //#endif
            CallbackInfo ci
    ) {
        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }

        OmniProfiler.withProfiler("polyblur_phosphor_blur", () -> {
            PhosphorBlur.update();
            float trueTickDelta = OmniGameRendering.getTickDelta(true);
            PhosphorBlur.render(trueTickDelta);
        });
    }
}
