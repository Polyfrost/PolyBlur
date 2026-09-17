package org.polyfrost.polyblur.client.blur

//? if >1.21.5 {
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.renderpearl.api.pipeline.RenderPipeline
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
    var failed = false
        private set

    @JvmStatic
    fun run() {
        if (done) return
        if (Minecraft.getInstance().level == null) return
        done = true

        val failures = listOf(
            "motion velocity" to MotionVelocityPass.prewarm(),
            "motion reproject" to MotionBlurReproject.prewarm(),
            "hybrid hand" to HybridHandPhosphor.prewarm(),
            "phosphor" to PhosphorBlur.prewarm(),
            "blit" to RenderTargetTracker.prewarm(),
        ).filterNot { it.second }.map { it.first }

        if (failures.isNotEmpty()) {
            failed = true
            logger.warn("Blur pipelines failed to compile: {}. Blur will not render.", failures)
        }
    }

    internal fun compile(pipeline: RenderPipeline): Boolean =
        //? if >=26.3 {
        RenderSystem.getCompiledPipelineNullable(pipeline) != null
        //?} else {
        /*RenderSystem.getDevice().precompilePipeline(pipeline).isValid()
        *///?}
}
//?}
