package org.polyfrost.polyblur.mixin.client.compat;

//#if FORGE && MC <= 1.12.2
import cc.polyfrost.oneconfig.internal.gui.impl.BlurHandlerImpl;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * We want to override OneConfig's blur so that it overrides our motion blur. Motion blur isn't important, but menu blur is.
 */
@Pseudo
@Mixin(targets = "cc.polyfrost.oneconfig.internal.gui.impl.BlurHandlerImpl", remap = false)
public abstract class Mixin_BlurHandlerImpl_OverridePhosphorBlurInOneConfigV0 {
    @Shadow abstract boolean isShaderActive();

    @Dynamic("OneConfig")
    @Redirect(method = "reloadBlur", at = @At(value = "INVOKE", target = "Lcc/polyfrost/oneconfig/internal/gui/impl/BlurHandlerImpl;isShaderActive()Z", ordinal = 0))
    private boolean redirectShaderActive(BlurHandlerImpl a) { // works without any params in 0.7.11 but in 0.8 things got stricter
        if (PhosphorBlur.isActive()) {
            return false;
        }

        return isShaderActive();
    }
}
//#endif
