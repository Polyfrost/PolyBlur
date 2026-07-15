package org.polyfrost.polyblur.client.blur

//? if >1.21.5 {
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyblur.client.blur.motion.MotionBlurReproject
import org.polyfrost.polyblur.client.blur.motion.MotionVelocityPass
import org.polyfrost.polyblur.client.blur.phosphor.HybridHandPhosphor
import org.polyfrost.polyblur.client.blur.phosphor.PhosphorBlur
import org.polyfrost.polyblur.client.blur.phosphor.RenderTargetTracker

object BlurPrewarm {
    private val logger = LogManager.getLogger("PolyBlur")
    private var done = false

    @JvmStatic
    fun run() {
        if (done) return
        if (Minecraft.getInstance().level == null) return
        done = true

        val failed = listOf(
            "motion velocity" to MotionVelocityPass.prewarm(),
            "motion reproject" to MotionBlurReproject.prewarm(),
            "hybrid hand" to HybridHandPhosphor.prewarm(),
            "phosphor" to PhosphorBlur.prewarm(),
            "blit" to RenderTargetTracker.prewarm(),
        ).filterNot { it.second }.map { it.first }

        if (failed.isNotEmpty()) {
            logger.warn("Blur pipelines failed to compile: {}. Blur will not render.", failed)
        }
    }

    /** Each pass calls this from its own `prewarm`, where its pipeline is in scope. */
    internal fun compile(pipeline: RenderPipeline): Boolean =
        RenderSystem.getDevice().precompilePipeline(pipeline).isValid()
}
//?}
