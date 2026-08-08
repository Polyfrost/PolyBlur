package org.polyfrost.polyblur.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.polyfrost.polyblur.client.blur.phosphor.BlitPlan
import org.polyfrost.polyblur.client.blur.phosphor.planBlit

class BlitPlanTest {

    @Test
    fun `matching attachments copy`() {
        assertEquals(
            BlitPlan.COPY,
            planBlit(
                srcWidth = 1920, srcHeight = 1080, srcTextureWidth = 1920, srcTextureHeight = 1080,
                dstWidth = 1920, dstHeight = 1080, dstTextureWidth = 1920, dstTextureHeight = 1080,
            )
        )
    }

    @Test
    fun `differently sized but consistent attachments draw`() {
        assertEquals(
            BlitPlan.DRAW,
            planBlit(
                srcWidth = 1920, srcHeight = 1080, srcTextureWidth = 1920, srcTextureHeight = 1080,
                dstWidth = 960, dstHeight = 540, dstTextureWidth = 960, dstTextureHeight = 540,
            )
        )
    }

    /** The crash: VulkanMod's 10x10 placeholder swapchain framebuffer behind a 1920x1080 main target. */
    @Test
    fun `destination smaller than it reports is skipped`() {
        assertEquals(
            BlitPlan.SKIP,
            planBlit(
                srcWidth = 1920, srcHeight = 1080, srcTextureWidth = 1920, srcTextureHeight = 1080,
                dstWidth = 1920, dstHeight = 1080, dstTextureWidth = 10, dstTextureHeight = 10,
            )
        )
    }

    @Test
    fun `source smaller than it reports is skipped`() {
        assertEquals(
            BlitPlan.SKIP,
            planBlit(
                srcWidth = 1920, srcHeight = 1080, srcTextureWidth = 10, srcTextureHeight = 10,
                dstWidth = 1920, dstHeight = 1080, dstTextureWidth = 1920, dstTextureHeight = 1080,
            )
        )
    }

    @Test
    fun `attachment larger than it reports is skipped`() {
        assertEquals(
            BlitPlan.SKIP,
            planBlit(
                srcWidth = 1920, srcHeight = 1080, srcTextureWidth = 1920, srcTextureHeight = 1080,
                dstWidth = 1280, dstHeight = 720, dstTextureWidth = 1920, dstTextureHeight = 1080,
            )
        )
    }

    @Test
    fun `degenerate extents are skipped`() {
        assertEquals(
            BlitPlan.SKIP,
            planBlit(
                srcWidth = 0, srcHeight = 0, srcTextureWidth = 0, srcTextureHeight = 0,
                dstWidth = 0, dstHeight = 0, dstTextureWidth = 0, dstTextureHeight = 0,
            )
        )
    }
}
