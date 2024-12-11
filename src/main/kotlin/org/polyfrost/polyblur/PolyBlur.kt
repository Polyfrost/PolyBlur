package org.polyfrost.polyblur

//#if FORGE
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
//#else
//$$ import net.fabricmc.api.ClientModInitializer
//#endif

import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.polyblur.blurs.phosphor.PhosphorBlur

//#if FORGE
@Mod(modid = PolyBlur.ID, version = PolyBlur.VERSION, name = PolyBlur.NAME, modLanguageAdapter = "org.polyfrost.oneconfig.utils.v1.forge.KotlinLanguageAdapter")
//#endif
object PolyBlur
    //#if FABRIC
    //$$ : ClientModInitializer
    //#endif
{

    const val ID = "@MOD_ID@"
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"

    fun initialize() {
        CommandManager.registerCommand(PolyBlurCommand())
        EventManager.INSTANCE.register(PhosphorBlur())
    }

    //#if FORGE
    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        initialize()
    }
    //#else
    //$$ override fun onInitializeClient() {
    //$$     initialize()
    //$$ }
    //#endif

}
