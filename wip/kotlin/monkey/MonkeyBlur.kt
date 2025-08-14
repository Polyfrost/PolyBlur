package org.polyfrost.polyblur.client.blur.monkey

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.OmniPerspective
import dev.deftu.omnicore.client.render.OmniMatrixStack
import dev.deftu.omnicore.client.render.OmniRenderEnv
import dev.deftu.omnicore.client.render.OmniResolution
import dev.deftu.omnicore.client.render.OmniTextureManager
import dev.deftu.omnicore.client.render.framebuffer.ManagedFramebuffer
import dev.deftu.omnicore.client.render.texture.GpuTexture
import dev.deftu.omnicore.client.shaders.OmniShader
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.InitializationEvent
import org.polyfrost.polyblur.client.PolyBlurConfig
import org.polyfrost.polyblur.client.blur.PolyBlurShaders
import java.nio.FloatBuffer
import kotlin.math.abs

object MonkeyBlur {
    private val isActive: Boolean
        get() = OmniRenderEnv.isShaderSupported && PolyBlurConfig.isEnabled && PolyBlurConfig.mode == 0

    private lateinit var shader: OmniShader

    private var framebuffer: ManagedFramebuffer? = null
    private var unbindFunction: (() -> Unit)? = null

    private val proj = allocFloat16()
    private val mv = allocFloat16()
    private val projInv = allocFloat16()
    private val mvInv = allocFloat16()
    private val prevProj = allocFloat16()
    private val prevMv = allocFloat16()

    private var camX = 0f;
    private var camY = 0f;
    private var camZ = 0f
    private var prevCamX = 0f;
    private var prevCamY = 0f;
    private var prevCamZ = 0f

    private var changedPerspective = false
    private var prevPerspective: OmniPerspective
        get() = OmniPerspective.currentPerspective
        set(value) { OmniPerspective.currentPerspective = value }
    private var firstFrame = true

    fun initialize() {
        eventHandler<InitializationEvent> {
            shader = PolyBlurShaders.loadShader("monkeyblur")
        }
    }

    fun createFramebuffer(
        width: Int = OmniResolution.viewportWidth,
        height: Int = OmniResolution.viewportHeight
    ) {
        val current = framebuffer
        if (current == null) {
            framebuffer = ManagedFramebuffer(width, height, GpuTexture.TextureFormat.RGBA8, GpuTexture.TextureFormat.DEPTH24_STENCIL8)
        } else if (current.width != width || current.height != height) {
            current.resize(width, height)
        }
    }

    @JvmStatic
    fun startFrame() {
        val currentPerspective = OmniPerspective.currentPerspective
        changedPerspective = prevPerspective != currentPerspective
        prevPerspective = currentPerspective

        copyMat(prevProj, proj)
        copyMat(prevMv, mv)
        prevCamX = camX
        prevCamY = camY
        prevCamZ = camZ

        createFramebuffer()
        framebuffer?.clearColor(0f, 0f, 0f, 0f)
        framebuffer?.clearDepthStencil(1.0, 0)
        unbindFunction = framebuffer?.bind()
    }

    @JvmStatic
    fun handleCamera(tickDelta: Float) {
        sampleCamera(tickDelta)
        fetchMatrices(proj, mv)
        invert4x4(proj, projInv)
        invert4x4(mv, mvInv)

        if (firstFrame) {
            copyMat(prevProj, proj)
            copyMat(prevMv, mv)

            prevCamX = camX
            prevCamY = camY
            prevCamZ = camZ

            firstFrame = false
        }
    }

    @JvmStatic
    fun endFrame() {
        unbindFunction?.invoke()

        val framebuffer = framebuffer ?: return
        val isSkippedPass = changedPerspective || !isActive
        val width = OmniResolution.viewportWidth.toFloat()
        val height = OmniResolution.viewportHeight.toFloat()
        val stack = OmniMatrixStack()
        if (!isSkippedPass) {
            shader.bind()

            OmniTextureManager.configureTextureUnit(1) {
                OmniTextureManager.bindTexture(framebuffer.depthStencilTexture.id)
            }

            shader.getSamplerUniform("texture").setValue(0)
            shader.getSamplerUniform("depthtex").setValue(1)
            shader.getVecUniform("strength").setValue(PolyBlurConfig.strength)
            shader.getVec3Uniform("cameraPosition").setValue(camX, camY, camZ)
            shader.getVec3Uniform("previousCameraPosition").setValue(prevCamX, prevCamY, prevCamZ)
            shader.getMatrixUniform("modelViewInverse").setValue(toArray16(mvInv))
            shader.getMatrixUniform("projectionInverse").setValue(toArray16(projInv))
            shader.getMatrixUniform("previousModelView").setValue(toArray16(prevMv))
            shader.getMatrixUniform("previousProjection").setValue(toArray16(prevProj))

            println("MonkeyBlur: Rendering with blur shader")
            framebuffer.drawColorTexture(stack, 0f, 0f, width, height, 0xFFFFFF)
            shader.unbind()
        } else {
            // Just render the framebuffer as a full-screen quad with no blur applied
            framebuffer.drawColorTexture(stack, 0f, 0f, width, height, 0xFFFFFF)
        }
    }

    @JvmStatic
    fun invalidate() {
        framebuffer?.close()
        framebuffer = null
    }

    private fun copyMat(dst: FloatBuffer, src: FloatBuffer) {
        dst.clear()
        src.position(0)
        dst.put(src)
        dst.flip()
        src.position(0)
    }

    private fun sampleCamera(tickDelta: Float) {
        val mc = OmniClient.getInstance()
        //#if MC <= 1.12.2
        val entity = mc.renderViewEntity ?: return
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * tickDelta
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * tickDelta
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * tickDelta
        //#else
        //$$ var entity = mc.cameraEntity ?: mc.player ?: return
        //$$ val x = entity.xOld + (entity.x - entity.xOld) * tickDelta
        //$$ val y = entity.yOld + (entity.y - entity.yOld) * tickDelta
        //$$ val z = entity.zOld + (entity.z - entity.zOld) * tickDelta
        //#endif

        camX = x.toFloat()
        camY = y.toFloat()
        camZ = z.toFloat()
    }

    private fun fetchMatrices(outProj: FloatBuffer, outMv: FloatBuffer) {
        //#if MC >= 1.16.5
        //$$ val projectionMatrix = RenderSystem.getProjectionMatrix()
        //$$ val modelViewMatrix = RenderSystem.getModelViewMatrix()
        //$$
        //#if MC >= 1.20.2
        //$$ val tempProj = FloatArray(16)
        //$$ projectionMatrix.get(tempProj)
        //$$ outProj.position(0)
        //$$ outProj.put(tempProj).position(0)
        //$$
        //$$ val tempMv = FloatArray(16)
        //$$ modelViewMatrix.get(tempMv)
        //$$ outMv.position(0)
        //$$ outMv.put(tempMv).position(0)
        //#else
        //$$ val tempProj = FloatArray(16)
        //$$ projectionMatrix.store(tempProj)
        //$$ outProj.position(0)
        //$$ outProj.put(tempProj).position(0)
        //$$
        //$$ val tempMv = FloatArray(16)
        //$$ modelViewMatrix.store(tempMv)
        //$$ outMv.position(0)
        //$$ outMv.put(tempMv).position(0)
        //#endif
        //#else
        outProj.position(0);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, outProj)

        outMv.position(0);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,  outMv)
        //#endif
    }

    private fun invert4x4(input: FloatBuffer, output: FloatBuffer) {
        val m = FloatArray(16)
        input.position(0)
        input.get(m).position(0)
        val inv = FloatArray(16)

        inv[0] =  m[5]*m[10]*m[15]-m[5]*m[11]*m[14]-m[9]*m[6]*m[15]+m[9]*m[7]*m[14]+m[13]*m[6]*m[11]-m[13]*m[7]*m[10]
        inv[4] = -m[4]*m[10]*m[15]+m[4]*m[11]*m[14]+m[8]*m[6]*m[15]-m[8]*m[7]*m[14]-m[12]*m[6]*m[11]+m[12]*m[7]*m[10]
        inv[8] =  m[4]*m[9]*m[15]-m[4]*m[11]*m[13]-m[8]*m[5]*m[15]+m[8]*m[7]*m[13]+m[12]*m[5]*m[11]-m[12]*m[7]*m[9]
        inv[12]= -m[4]*m[9]*m[14]+m[4]*m[10]*m[13]+m[8]*m[5]*m[14]-m[8]*m[6]*m[13]-m[12]*m[5]*m[10]+m[12]*m[6]*m[9]
        inv[1] = -m[1]*m[10]*m[15]+m[1]*m[11]*m[14]+m[9]*m[2]*m[15]-m[9]*m[3]*m[14]-m[13]*m[2]*m[11]+m[13]*m[3]*m[10]
        inv[5] =  m[0]*m[10]*m[15]-m[0]*m[11]*m[14]-m[8]*m[2]*m[15]+m[8]*m[3]*m[14]+m[12]*m[2]*m[11]-m[12]*m[3]*m[10]
        inv[9] = -m[0]*m[9]*m[15]+m[0]*m[11]*m[13]+m[8]*m[1]*m[15]-m[8]*m[3]*m[13]-m[12]*m[1]*m[11]+m[12]*m[3]*m[9]
        inv[13]=  m[0]*m[9]*m[14]-m[0]*m[10]*m[13]-m[8]*m[1]*m[14]+m[8]*m[2]*m[13]+m[12]*m[1]*m[10]-m[12]*m[2]*m[9]
        inv[2] =  m[1]*m[6]*m[15]-m[1]*m[7]*m[14]-m[5]*m[2]*m[15]+m[5]*m[3]*m[14]+m[13]*m[2]*m[7]-m[13]*m[3]*m[6]
        inv[6] = -m[0]*m[6]*m[15]+m[0]*m[7]*m[14]+m[4]*m[2]*m[15]-m[4]*m[3]*m[14]-m[12]*m[2]*m[7]+m[12]*m[3]*m[6]
        inv[10]=  m[0]*m[5]*m[15]-m[0]*m[7]*m[13]-m[4]*m[1]*m[15]+m[4]*m[3]*m[13]+m[12]*m[1]*m[7]-m[12]*m[3]*m[5]
        inv[14]= -m[0]*m[5]*m[14]+m[0]*m[6]*m[13]+m[4]*m[1]*m[14]-m[4]*m[2]*m[13]-m[12]*m[1]*m[6]+m[12]*m[2]*m[5]
        inv[3] = -m[1]*m[6]*m[11]+m[1]*m[7]*m[10]+m[5]*m[2]*m[11]-m[5]*m[3]*m[10]-m[9]*m[2]*m[7]+m[9]*m[3]*m[6]
        inv[7] =  m[0]*m[6]*m[11]-m[0]*m[7]*m[10]-m[4]*m[2]*m[11]+m[4]*m[3]*m[10]+m[8]*m[2]*m[7]-m[8]*m[3]*m[6]
        inv[11]= -m[0]*m[5]*m[11]+m[0]*m[7]*m[9]+m[4]*m[1]*m[11]-m[4]*m[3]*m[9]-m[8]*m[1]*m[7]+m[8]*m[3]*m[5]
        inv[15]=  m[0]*m[5]*m[10]-m[0]*m[6]*m[9]-m[4]*m[1]*m[10]+m[4]*m[2]*m[9]+m[8]*m[1]*m[6]-m[8]*m[2]*m[5]

        val det = m[0]*inv[0] + m[1]*inv[4] + m[2]*inv[8] + m[3]*inv[12]
        if (abs(det) < 1e-8f) {
            // Identity fallback to avoid NaNs on degenerate matrices
            output.position(0)
            output.put(floatArrayOf(
                1f,0f,0f,0f, 0f,1f,0f,0f, 0f,0f,1f,0f, 0f,0f,0f,1f
            ))
            output.position(0)
            return
        }

        val invDet = 1f / det
        for (i in 0 until 16) {
            inv[i] *= invDet
        }

        output.position(0); output.put(inv); output.position(0)
    }

    private fun allocFloat16(): FloatBuffer {
        //#if MC >= 1.16.5
        //$$ return ByteBuffer.allocateDirect(16 * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer()
        //#else
        return BufferUtils.createFloatBuffer(16)
        //#endif
    }

    private fun toArray16(buffer: FloatBuffer): FloatArray {
        val out = FloatArray(16)
        buffer.position(0)
        buffer.get(out)
        buffer.position(0)
        return out
    }
}
