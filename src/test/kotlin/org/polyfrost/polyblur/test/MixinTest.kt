package org.polyfrost.polyblur.test

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.MixinEnvironment.Option
import org.spongepowered.asm.mixin.transformer.IMixinTransformer

/**
 * audits mixins for validity without launching a full minecraft client
 * implementation inspired by Skyblocker https://github.com/SkyblockerMod/Skyblocker
 */
class MixinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupEnvironment() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `mixins load successfully`() {
        val environment = MixinEnvironment.getCurrentEnvironment()
        Assertions.assertInstanceOf(
            IMixinTransformer::class.java,
            environment.activeTransformer,
        )
        // fabric loader enables refmap remapping in dev so mixin retries target selection without
        // descriptors and the audit would pass on selectors that cannot apply in production
        environment.setOption(Option.REFMAP_REMAP, false)
        environment.audit()
    }
}
