package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >1.21.5 && <26.1 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.motion.MotionBlur;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}
//? if >=26.1 {
/*import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.motion.MotionBlur;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.ResourcePoolHolder;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
//? if >=26.1 && <26.2
/*import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;*/
//? if =1.21.5 {
/*import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.polyfrost.polyblur.client.PolyBlurConfig;
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject;
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass;
import org.polyfrost.polyblur.client.blur.motion.WorldCamera;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

@Mixin(LevelRenderer.class)
public class Mixin_CaptureWorldMatrices {
    //? if =1.21.5 {
    /*// 1.21.5 renderLevel: (alloc, delta, bool, Camera, GameRenderer, Matrix4f view, Matrix4f proj).
    @Inject(method = "renderLevel", at = @At("RETURN"))
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
        if (!PolyBlurConfig.INSTANCE.isEnabled() || PolyBlurConfig.INSTANCE.getBlurType() != 1) {
            return;
        }
        if (PolyBlurConfig.INSTANCE.getVelocityBuffer()) {
            MotionVelocityPass.run(Minecraft.getInstance().getMainRenderTarget());
            // Hand sharp -> blur pre-hand. (1.21.5 global blur stays post-GUI.)
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
    @Inject(method = "renderLevel", at = @At("RETURN"))
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
    //?}

    //? if >=26.1 && <26.2 {
    /*
    @Inject(method = "renderLevel", at = @At("RETURN"))
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

    //? if >=26.2 {
    /*
    @Inject(method = "render", at = @At("RETURN"))
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
    private void polyblur$runMotion(Matrix4f view, Matrix4f projection, Camera camera) {
        WorldCamera.INSTANCE.capture(view, projection,
                //? if >=1.21.11 {
                /*camera.position()*/
                //?} else {
                camera.getPosition()
                //?}
        );

        if (!PolyBlurConfig.INSTANCE.isEnabled() || PolyBlurConfig.INSTANCE.getBlurType() != 1) {
            return;
        }

        if (PolyBlurConfig.INSTANCE.getVelocityBuffer()) {
            MotionVelocityPass.run(Minecraft.getInstance().getMainRenderTarget());
        }

        if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
            if (PolyBlurConfig.INSTANCE.getVelocityBuffer()) {
                MotionBlurReproject.render(Minecraft.getInstance().getMainRenderTarget());
            } else {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    MotionBlur.render(Minecraft.getInstance().getMainRenderTarget(), pool);
                }
            }
        }
    }
    //?}

    //? if >=26.1 {
    /*private void polyblur$runMotion(CameraRenderState camera) {
        WorldCamera.INSTANCE.capture(camera.viewRotationMatrix, camera.projectionMatrix, camera.pos);

        if (!PolyBlurConfig.INSTANCE.isEnabled() || PolyBlurConfig.INSTANCE.getBlurType() != 1) {
            return;
        }

        RenderTarget mainTarget = ResourcePoolHolder.INSTANCE.getMainTarget();
        if (mainTarget == null) {
            return;
        }

        if (PolyBlurConfig.INSTANCE.getVelocityBuffer()) {
            MotionVelocityPass.run(mainTarget);
        }

        if (!PolyBlurConfig.INSTANCE.getBlurHand()) {
            if (PolyBlurConfig.INSTANCE.getVelocityBuffer()) {
                MotionBlurReproject.render(mainTarget);
            } else {
                CrossFrameResourcePool pool = ResourcePoolHolder.INSTANCE.getPool();
                if (pool != null) {
                    MotionBlur.render(mainTarget, pool);
                }
            }
        }
    }
    *///?}
}
