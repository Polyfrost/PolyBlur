package org.polyfrost.polyblur.test

//? if >1.8.9
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
//? if =1.8.9 {
/*import net.fabricmc.loader.api.FabricLoader
import net.ornithemc.osl.entrypoints.api.ModInitializer
*///?}
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
            //? if >1.8.9 {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            //?} else {
            /*FabricLoader.getInstance().invokeEntrypoints(
                ModInitializer.ENTRYPOINT_KEY,
                ModInitializer::class.java,
                ModInitializer::init,
            )
            Bootstrap.init()
            *///?}
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
