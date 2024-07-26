package org.polyfrost.polyblur;

import org.polyfrost.oneconfig.api.commands.v1.CommandManager;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.polyblur.blurs.phosphor.PhosphorBlur;
import org.polyfrost.polyblur.commands.PolyBlurCommand;
import org.polyfrost.polyblur.config.PolyBlurConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
        modid = "@ID@"
)
public class PolyBlur {
    @Mod.Instance
    public static PolyBlur instance;

    public final PolyBlurConfig config = new PolyBlurConfig();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        CommandManager.registerCommand(new PolyBlurCommand());
        EventManager.INSTANCE.register(new PhosphorBlur());
    }
}
