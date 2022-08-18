package cc.polyfrost.polyblur.mixin;

import cc.polyfrost.polyblur.blurs.phosphor.EntityRendererHook;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin_PhosphorBlur implements EntityRendererHook {
    @Shadow private ShaderGroup theShaderGroup;
    private ShaderGroup phosphorShader;

    @Inject(method = "isShaderActive", at = @At("HEAD"), cancellable = true)
    private void onIsShaderActive(CallbackInfoReturnable<Boolean> cir) {
        if (phosphorShader != null && OpenGlHelper.shadersSupported) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getShaderGroup", at = @At("HEAD"), cancellable = true)
    private void onGetShaderGroup(CallbackInfoReturnable<ShaderGroup> cir) {
        if (phosphorShader != null && OpenGlHelper.shadersSupported && theShaderGroup == null) {
            cir.setReturnValue(phosphorShader);
        }
    }

    @Inject(method = "updateShaderGroupSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;createBindEntityOutlineFbs(II)V"))
    private void updatePhosphor(int width, int height, CallbackInfo ci) {
        if (phosphorShader != null) {
            phosphorShader.createBindFramebuffers(width, height);
        }
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntityOutlineFramebuffer()V", shift = At.Shift.AFTER))
    private void renderPhosphor(float partialTicks, long nanoTime, CallbackInfo ci) {
        if (this.phosphorShader != null) {
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            this.phosphorShader.loadShaderGroup(partialTicks);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public ShaderGroup getPhosphorShader() {
        return phosphorShader;
    }

    @Override
    public void setPhosphorShader(ShaderGroup phosphorShader) {
        this.phosphorShader = phosphorShader;
    }
}
