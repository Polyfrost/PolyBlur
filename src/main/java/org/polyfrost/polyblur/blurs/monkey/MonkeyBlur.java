package org.polyfrost.polyblur.blurs.monkey;

import org.polyfrost.polyblur.PolyBlur;
import org.polyfrost.polyblur.optifine.OFConfig;
import org.polyfrost.polyblur.shader.Shader;
import org.polyfrost.polyblur.shader.ShaderLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MonkeyBlur {

    public static final MonkeyBlur instance = new MonkeyBlur();

    private final Minecraft mc = Minecraft.getMinecraft();

    public MonkeyBuffer frameBuffer = null;
    public Shader monkeyblurShader = null;

    private final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projectionInverse = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer modelViewInverse = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer previousProjection = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer previousProjectionInverse = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer previousModelView = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer previousModelViewInverse = BufferUtils.createFloatBuffer(16);

    private float cameraPosX;
    private float cameraPosY;
    private float cameraPosZ;
    private float previousCameraPosX;
    private float previousCameraPosY;
    private float previousCameraPosZ;

    private boolean changedPerspective = false;
    private int thirdPersonViewPrevious = 0;

    private IntBuffer pixelBuffer;
    private int[] pixelValues;

    public void onResolutionChange() {
        if (!isEnabled()) return;
        if (frameBuffer != null) {
            frameBuffer = null;
        }
    }

    public void startFrame() {
        changedPerspective = thirdPersonViewPrevious != mc.gameSettings.thirdPersonView;
        thirdPersonViewPrevious = mc.gameSettings.thirdPersonView;
        if (!isEnabled()) return;

        previousProjection.position(0);
        previousProjection.put(projection).position(0);
        previousProjectionInverse.position(0);
        previousProjectionInverse.put(projectionInverse).position(0);
        previousModelView.position(0);
        previousModelView.put(modelView).position(0);
        previousModelViewInverse.position(0);
        previousModelViewInverse.put(modelViewInverse).position(0);

        previousCameraPosX = cameraPosX;
        previousCameraPosY = cameraPosY;
        previousCameraPosZ = cameraPosZ;

        bindFb();
    }

    public void endFrame() {
        if (changedPerspective) {
            final Entity viewEntity = mc.getRenderViewEntity();
            previousProjection.position(0);
            previousProjection.put(projection).position(0);
            previousProjectionInverse.position(0);
            previousProjectionInverse.put(projectionInverse).position(0);
            previousModelView.position(0);
            previousModelView.put(modelView).position(0);
            previousModelViewInverse.position(0);
            previousModelViewInverse.put(modelViewInverse).position(0);

            cameraPosX = (float) (viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX));
            cameraPosY = (float) (viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY));
            cameraPosZ = (float) (viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ));
            projection.position(0);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
            invertMat4(projection, projectionInverse);
            modelView.position(0);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
            invertMat4(modelView, modelViewInverse);

            previousCameraPosX = cameraPosX;
            previousCameraPosY = cameraPosY;
            previousCameraPosZ = cameraPosZ;
        }
        if (!isEnabled()) return;

        unbindFb();

        if (monkeyblurShader == null) {
            loadShaders();
        }

        monkeyblurShader.bindShader();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.bindTexture(frameBuffer.depthTexture);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);

        monkeyblurShader.setUniform1f("strength", (float) PolyBlur.instance.config.strength / 2);
        monkeyblurShader.setUniform1i("texture", 0);
        monkeyblurShader.setUniform1i("depthtex", 1);
        monkeyblurShader.setUniformMat4("modelViewInverse", modelViewInverse);
        monkeyblurShader.setUniformMat4("projectionInverse", projectionInverse);
        monkeyblurShader.setUniformMat4("previousProjection", previousProjection);
        monkeyblurShader.setUniformMat4("previousModelView", previousModelView);
        monkeyblurShader.setUniform3f("cameraPosition", cameraPosX, cameraPosY, cameraPosZ);
        monkeyblurShader.setUniform3f("previousCameraPosition", previousCameraPosX, previousCameraPosY, previousCameraPosZ);

        GlStateManager.pushMatrix();
        frameBuffer.render();
        GlStateManager.popMatrix();

        monkeyblurShader.unbindShader();
    }

    public void bindFb() {
        if (!isEnabled()) return;

        if (frameBuffer == null) {
            frameBuffer = new MonkeyBuffer(mc.displayWidth, mc.displayHeight);
        }
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, frameBuffer.framebufferObject);
        frameBuffer.clear();
    }

    public void unbindFb() {
        if (!isEnabled()) return;

        if (OpenGlHelper.isFramebufferEnabled()) {
           Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
        } else {
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, 0); // just unbind without binding mc framebuffer if not "supported"
        }
    }

    public void setupCamera(float partialTicks) {
        if (!isEnabled()) return;

        Entity viewEntity = mc.getRenderViewEntity();
        cameraPosX = (float) (viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks);
        cameraPosY = (float) (viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks);
        cameraPosZ = (float) (viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks);

        projection.position(0);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        invertMat4(projection, projectionInverse);
        modelView.position(0);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        invertMat4(modelView, modelViewInverse);
    }

    public void loadShaders() {
        if (monkeyblurShader != null) {
            monkeyblurShader.delete();
        }
        monkeyblurShader = ShaderLoader.loadShader("monkeyblur", true, true);
    }

    static void invertMat4(FloatBuffer in, FloatBuffer out) {
        float[] m = new float[16];
        float[] invOut = new float[16];

        in.position(0);
        in.get(m).position(0);

        invOut[0] =   m[5]*m[10]*m[15] - m[5]*m[11]*m[14] - m[9]*m[6]*m[15]
                + m[9]*m[7]*m[14] + m[13]*m[6]*m[11] - m[13]*m[7]*m[10];
        invOut[4] =  -m[4]*m[10]*m[15] + m[4]*m[11]*m[14] + m[8]*m[6]*m[15]
                - m[8]*m[7]*m[14] - m[12]*m[6]*m[11] + m[12]*m[7]*m[10];
        invOut[8] =   m[4]*m[9]*m[15] - m[4]*m[11]*m[13] - m[8]*m[5]*m[15]
                + m[8]*m[7]*m[13] + m[12]*m[5]*m[11] - m[12]*m[7]*m[9];
        invOut[12] = -m[4]*m[9]*m[14] + m[4]*m[10]*m[13] + m[8]*m[5]*m[14]
                - m[8]*m[6]*m[13] - m[12]*m[5]*m[10] + m[12]*m[6]*m[9];
        invOut[1] =  -m[1]*m[10]*m[15] + m[1]*m[11]*m[14] + m[9]*m[2]*m[15]
                - m[9]*m[3]*m[14] - m[13]*m[2]*m[11] + m[13]*m[3]*m[10];
        invOut[5] =   m[0]*m[10]*m[15] - m[0]*m[11]*m[14] - m[8]*m[2]*m[15]
                + m[8]*m[3]*m[14] + m[12]*m[2]*m[11] - m[12]*m[3]*m[10];
        invOut[9] =  -m[0]*m[9]*m[15] + m[0]*m[11]*m[13] + m[8]*m[1]*m[15]
                - m[8]*m[3]*m[13] - m[12]*m[1]*m[11] + m[12]*m[3]*m[9];
        invOut[13] =  m[0]*m[9]*m[14] - m[0]*m[10]*m[13] - m[8]*m[1]*m[14]
                + m[8]*m[2]*m[13] + m[12]*m[1]*m[10] - m[12]*m[2]*m[9];
        invOut[2] =   m[1]*m[6]*m[15] - m[1]*m[7]*m[14] - m[5]*m[2]*m[15]
                + m[5]*m[3]*m[14] + m[13]*m[2]*m[7] - m[13]*m[3]*m[6];
        invOut[6] =  -m[0]*m[6]*m[15] + m[0]*m[7]*m[14] + m[4]*m[2]*m[15]
                - m[4]*m[3]*m[14] - m[12]*m[2]*m[7] + m[12]*m[3]*m[6];
        invOut[10] =  m[0]*m[5]*m[15] - m[0]*m[7]*m[13] - m[4]*m[1]*m[15]
                + m[4]*m[3]*m[13] + m[12]*m[1]*m[7] - m[12]*m[3]*m[5];
        invOut[14] = -m[0]*m[5]*m[14] + m[0]*m[6]*m[13] + m[4]*m[1]*m[14]
                - m[4]*m[2]*m[13] - m[12]*m[1]*m[6] + m[12]*m[2]*m[5];
        invOut[3] =  -m[1]*m[6]*m[11] + m[1]*m[7]*m[10] + m[5]*m[2]*m[11]
                - m[5]*m[3]*m[10] - m[9]*m[2]*m[7] + m[9]*m[3]*m[6];
        invOut[7] =   m[0]*m[6]*m[11] - m[0]*m[7]*m[10] - m[4]*m[2]*m[11]
                + m[4]*m[3]*m[10] + m[8]*m[2]*m[7] - m[8]*m[3]*m[6];
        invOut[11] = -m[0]*m[5]*m[11] + m[0]*m[7]*m[9] + m[4]*m[1]*m[11]
                - m[4]*m[3]*m[9] - m[8]*m[1]*m[7] + m[8]*m[3]*m[5];
        invOut[15] =  m[0]*m[5]*m[10] - m[0]*m[6]*m[9] - m[4]*m[1]*m[10]
                + m[4]*m[2]*m[9] + m[8]*m[1]*m[6] - m[8]*m[2]*m[5];


        out.position(0);
        out.put(invOut).position(0);
    }

    public boolean isEnabled() {
        return OpenGlHelper.framebufferSupported && PolyBlur.instance.config.enabled && PolyBlur.instance.config.blurMode == 0 && !changedPerspective && !OFConfig.isShaders();
    }

    public void outputFb(int width, int height, int texture) {
        try {

            int i = width * height;

            if (pixelBuffer == null || pixelBuffer.capacity() < i) {
                pixelBuffer = BufferUtils.createIntBuffer(i);
                pixelValues = new int[i];
            }

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();

            GlStateManager.bindTexture(texture);
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) pixelBuffer);

            pixelBuffer.get(pixelValues);
            TextureUtil.processPixelValues(pixelValues, width, height);
            BufferedImage bufferedimage = new BufferedImage(width, height, 1);

            for (int k = 0; k < height; ++k) {
                for (int l = 0; l < width; ++l) {
                    bufferedimage.setRGB(l, k, pixelValues[k * width + l]);
                }
            }

            File file2 = new File("./test.png");

            ImageIO.write(bufferedimage, "png", file2);

            System.out.println(file2.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
