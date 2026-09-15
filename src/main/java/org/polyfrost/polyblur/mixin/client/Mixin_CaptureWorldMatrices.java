package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >1.21.5 && <26.1 {
/*import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.BlurSettings;
import org.polyfrost.polyblur.client.blur.motion.HybridWorldPass;
import org.polyfrost.polyblur.client.blur.motion.MotionBlur;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.polyfrost.polyblur.client.blur.phosphor.WorldSnapshotTracker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
//? if >=26.1 {
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.BlurSettings;
import org.polyfrost.polyblur.client.blur.motion.HybridWorldPass;
import org.polyfrost.polyblur.client.blur.motion.MotionBlur;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.polyfrost.polyblur.client.blur.phosphor.WorldSnapshotTracker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}
//? if >=26.1 && <26.2
//import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
//? if =1.21.5 {
/*import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.BlurSettings;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
//? if =1.21.4 {
/*import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.BlurSettings;
import org.polyfrost.polyblur.client.blur.motion.MotionBlur;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
//? if =1.21.1 {
/*import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.BlurSettings;
import org.polyfrost.polyblur.client.blur.motion.MotionBlur;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

@Mixin(LevelRenderer.class)
public class Mixin_CaptureWorldMatrices {
    //? if =1.21.1 {
    /*@Inject(method = "renderLevel", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices11(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        WorldCamera.INSTANCE.capture(frustumMatrix, projectionMatrix, camera.getPosition());
        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        // hybrid falls back to phosphor over the whole frame here, so it always covers the hand
        boolean blurHand = PolyBlurConfig.INSTANCE.getBlurType() == 2 || PolyBlurConfig.INSTANCE.getBlurHand();

        if (PolyBlurConfig.INSTANCE.getBlurType() != 1) {
            if (!blurHand) PhosphorBlur.render(target);
            return;
        }

        if (BlurSettings.getVelocityBuffer()) {
            if (!WorldCamera.INSTANCE.getVelocitySettled()) {
                MotionVelocityPass.run(target);
                // a sharp hand means the blur has to run before the hand is drawn
                if (!blurHand) MotionBlurReproject.render(target);
            }
        } else if (!blurHand) {
            MotionBlur.render(target);
        }
    }
    *///?}

    //? if =1.21.4 {
    /*@Inject(method = "renderLevel", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices14(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        WorldCamera.INSTANCE.capture(frustumMatrix, projectionMatrix, camera.getPosition());
        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        // hybrid falls back to phosphor over the whole frame here, so it always covers the hand
        boolean blurHand = PolyBlurConfig.INSTANCE.getBlurType() == 2 || PolyBlurConfig.INSTANCE.getBlurHand();

        if (PolyBlurConfig.INSTANCE.getBlurType() != 1) {
            if (!blurHand) {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) PhosphorBlur.render(target, pool);
            }
            return;
        }

        if (BlurSettings.getVelocityBuffer()) {
            if (!WorldCamera.INSTANCE.getVelocitySettled()) {
                MotionVelocityPass.run(target);
                // a sharp hand means the blur has to run before the hand is drawn
                if (!blurHand) MotionBlurReproject.render(target);
            }
        } else if (!blurHand) {
            CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
            if (pool != null) MotionBlur.render(target, pool);
        }
    }
    *///?}

    //? if =1.21.5 {
    /*@Inject(method = "renderLevel", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices15(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        WorldCamera.INSTANCE.capture(frustumMatrix, projectionMatrix, camera.getPosition());
        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }
        int blurType = PolyBlurConfig.INSTANCE.getBlurType();
        if (blurType == 0) {
            if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    PhosphorBlur.render(Minecraft.getInstance().getMainRenderTarget(), pool);
                }
            }
            return;
        }
        if (blurType != 1) {
            return;
        }
        if (BlurSettings.getVelocityBuffer() && !WorldCamera.INSTANCE.getVelocitySettled()) {
            MotionVelocityPass.run(Minecraft.getInstance().getMainRenderTarget());
            // sharp hand means blur must run before the hand while global blur stays post gui
            if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
                MotionBlurReproject.render(Minecraft.getInstance().getMainRenderTarget());
            }
        }
    }
    *///?}

    //? if =1.21.8 {
    /*@Inject(method = "renderLevel", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean flag,
            CallbackInfo ci
    ) {
        polyblur$runMotion(frustumMatrix, projectionMatrix, camera);
    }
    *///?}

    //? if >=1.21.10 && <26.1 {
    /*@Inject(method = "renderLevel", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f frustumMatrix,
            Matrix4f poseMatrix,
            Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean flag,
            CallbackInfo ci
    ) {
        polyblur$runMotion(frustumMatrix, projectionMatrix, camera);
    }
    *///?}

    //? if >=26.1 && <26.2 {
    
    /*@Inject(method = "renderLevel", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            CameraRenderState camera,
            Matrix4fc poseMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean flag,
            ChunkSectionsToRender chunkSectionsToRender,
            CallbackInfo ci
    ) {
        polyblur$runMotion(camera);
    }
    *///?}

    //? if >=26.3 {

    @Inject(method = "render", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices(
            GraphicsResourceAllocator allocator,
            boolean renderBlockOutline,
            CameraRenderState camera,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean flag,
            boolean flag2,
            CallbackInfo ci
    ) {
        polyblur$runMotion(camera);
    }
    //?}

    //? if >=26.2 && <26.3 {

    /*@Inject(method = "render", at = @At("RETURN"))
    private void polyblur$captureWorldMatrices(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            CameraRenderState camera,
            Matrix4fc poseMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean flag,
            CallbackInfo ci
    ) {
        polyblur$runMotion(camera);
    }
    *///?}

    //? if >1.21.5 && <26.1 {
    /*private void polyblur$runMotion(Matrix4f view, Matrix4f projection, Camera camera) {
        WorldCamera.INSTANCE.capture(view, projection,
                //? if >=1.21.11 {
                camera.position()
                //?} else {
                /^camera.getPosition()
                ^///?}
        );

        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }

        int blurType = PolyBlurConfig.INSTANCE.getBlurType();

        if (blurType == 0) {
            if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    PhosphorBlur.render(Minecraft.getInstance().getMainRenderTarget(), pool);
                }
            }
            return;
        }

        if (blurType == 2) {
            if (BlurSettings.getVelocityBuffer()) {
                HybridWorldPass.run(Minecraft.getInstance().getMainRenderTarget());
            } else {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    MotionBlur.render(Minecraft.getInstance().getMainRenderTarget(), pool);
                }
                WorldSnapshotTracker.INSTANCE.capture(Minecraft.getInstance().getMainRenderTarget());
            }
            return;
        }

        if (blurType != 1) {
            return;
        }

        if (BlurSettings.getVelocityBuffer()) {
            if (!WorldCamera.INSTANCE.getVelocitySettled()) {
                MotionVelocityPass.run(Minecraft.getInstance().getMainRenderTarget());
            }
        }

        if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
            if (BlurSettings.getVelocityBuffer()) {
                if (!WorldCamera.INSTANCE.getVelocitySettled()) {
                    MotionBlurReproject.render(Minecraft.getInstance().getMainRenderTarget());
                }
            } else {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    MotionBlur.render(Minecraft.getInstance().getMainRenderTarget(), pool);
                }
            }
        }
    }
    *///?}

    //? if >=26.1 {
    private void polyblur$runMotion(CameraRenderState camera) {
        WorldCamera.INSTANCE.capture(camera.viewRotationMatrix, camera.projectionMatrix, camera.pos);

        if (!PolyBlurConfig.INSTANCE.isEnabled()) {
            return;
        }

        RenderTarget mainTarget = ResourcePoolHolder.INSTANCE.getMainTarget();
        if (mainTarget == null) {
            return;
        }

        int blurType = PolyBlurConfig.INSTANCE.getBlurType();

        if (blurType == 0) {
            if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    PhosphorBlur.render(mainTarget, pool);
                }
            }
            return;
        }

        if (blurType == 2) {
            if (BlurSettings.getVelocityBuffer()) {
                HybridWorldPass.run(mainTarget);
            } else {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    MotionBlur.render(mainTarget, pool);
                }
                WorldSnapshotTracker.INSTANCE.capture(mainTarget);
            }
            return;
        }

        if (blurType != 1) {
            return;
        }

        if (BlurSettings.getVelocityBuffer()) {
            if (!WorldCamera.INSTANCE.getVelocitySettled()) {
                MotionVelocityPass.run(mainTarget);
            }
        }

        if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
            if (BlurSettings.getVelocityBuffer()) {
                if (!WorldCamera.INSTANCE.getVelocitySettled()) {
                    MotionBlurReproject.render(mainTarget);
                }
            } else {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    MotionBlur.render(mainTarget, pool);
                }
            }
        }
    }
    //?}
}
